package fr.inra.mig_bibliome.alvisir.core.expand.index;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixTermEnum;
import org.apache.lucene.search.SingleTermEnum;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.regex.JavaUtilRegexCapabilities;
import org.apache.lucene.search.regex.RegexCapabilities;
import org.apache.lucene.search.regex.RegexTermEnum;

import fr.inra.mig_bibliome.alvisir.core.AlvisIRConstants;
import fr.inra.mig_bibliome.alvisir.core.expand.CompoundTextExpansionResult;
import fr.inra.mig_bibliome.alvisir.core.expand.ExpanderException;
import fr.inra.mig_bibliome.alvisir.core.expand.ExplanationFactory;
import fr.inra.mig_bibliome.alvisir.core.expand.TextExpander;
import fr.inra.mig_bibliome.alvisir.core.expand.TextExpansionResult;

public class IndexBasedTextExpander implements TextExpander {
	public static final String FIELD_PROPERTY = "__PROPERTY";

	private final IndexReader reader;
	private final Map<String,ExplanationFactory> explanationFactories = new HashMap<String,ExplanationFactory>();

	public IndexBasedTextExpander(IndexReader reader) {
		super();
		this.reader = reader;
	}

	@Override
	public TextExpansionResult searchExpansion(String text) throws IOException {
		TextExpansionResult synonyms = createTextExpansionResult(AlvisIRConstants.EXPANDER_FIELD_SYNONYMS, text);
		TextExpansionResult canonical = createTextExpansionResult(AlvisIRConstants.EXPANDER_FIELD_CANONICAL, text);
		return new CompoundTextExpansionResult(synonyms, canonical);
	}
	
	private TextExpansionResult createTextExpansionResult(String fieldName, String text) throws IOException {
		Term term = new Term(fieldName, text);
		TermDocs termDocs = reader.termDocs(term);
		return new TermDocsExpansionResult(termDocs);
	}

	TextExpansionResult searchSubPaths(String prefix) throws IOException, ExpanderException {
//		System.err.println("prefix = " + prefix);
		Term prefixTerm = getCanonicalTerm(prefix);
		TermEnum termEnum = new PrefixTermEnum(reader, prefixTerm);
		return new TermEnumExpansionResult(termEnum);
	}
	
	TextExpansionResult searchLimitedDepthSubPaths(String prefix, int depth, char separator) throws IOException, ExpanderException {
		String sep = Pattern.quote(Character.toString(separator));
		String pattern = Pattern.quote(prefix) + "(?:" + "[^" + sep + "]*" + sep + "){0," + depth + "}$";
//		System.err.println("pattern = " + pattern);
		Term regexTerm = getCanonicalTerm(pattern);
		TermEnum termEnum = new RegexTermEnum(reader, regexTerm, new PrefixRegexCapabilities(prefix));
		return new TermEnumExpansionResult(termEnum);
	}
	
	private static final class PrefixRegexCapabilities implements RegexCapabilities {
		private final RegexCapabilities delegate;
		private final String prefix;
		
		private PrefixRegexCapabilities(RegexCapabilities delegate, String prefix) {
			super();
			this.delegate = delegate;
			this.prefix = prefix;
		}
		
		private PrefixRegexCapabilities(String prefix) {
			this(new JavaUtilRegexCapabilities(), prefix);
		}

		@Override
		public void compile(String pattern) {
			delegate.compile(pattern);
		}

		@Override
		public boolean match(String s) {
			return delegate.match(s);
		}

		@Override
		public String prefix() {
			return prefix;
		}
	}

	private static Term getCanonicalTerm(String text) {
		return new Term(AlvisIRConstants.EXPANDER_FIELD_CANONICAL, text);
	}

	@Override
	public TextExpansionResult searchCanonical(String text) throws IOException, ExpanderException {
		Term term = getCanonicalTerm(text);
//		System.err.println("term = " + term);
		TermEnum termEnum = new SingleTermEnum(reader, term);
		return new TermEnumExpansionResult(termEnum);
	}

	private final class TermEnumExpansionResult extends IndexBasedTextExpansionResult {
		private final TermEnum termEnum;

		private TermEnumExpansionResult(TermEnum termEnum) throws IOException, ExpanderException {
			super();
			this.termEnum = termEnum;
			if (termEnum.term() != null) {
				updateDoc();
			}
		}
		
		private void updateDoc() throws IOException, ExpanderException {
			Term term = termEnum.term();
//			System.err.println("term = " + term);
			try (TermDocs termDocs = reader.termDocs(term)) {
				if (!termDocs.next()) {
					throw new ExpanderException("no expansion for term: " + term);
				}
				int docId = termDocs.doc();
//				System.err.println("docId = " + docId);
				doc = reader.document(docId);
			}
		}

		@Override
		public boolean next() throws IOException, ExpanderException {
			if (termEnum.next()) {
				updateDoc();
				return true;
			}
			termEnum.close();
			return false;
		}
	}
	
	private final class TermDocsExpansionResult extends IndexBasedTextExpansionResult {
		private final TermDocs termDocs;
		
		private TermDocsExpansionResult(TermDocs termDocs) throws IOException {
			super();
			this.termDocs = termDocs;
			next();
		}

		@Override
		public boolean next() throws IOException {
			if (termDocs.next()) {
				int docId = termDocs.doc();
				doc = reader.document(docId);
				return true;
			}
			doc = null;
			return false;
		}
	}
	
	private static abstract class IndexBasedTextExpansionResult implements TextExpansionResult {
		protected Document doc;

		protected IndexBasedTextExpansionResult() {
			super();
		}

		@Override
		public boolean any() {
			return doc != null;
		}

		@Override
		public String getType() {
			return doc.get(AlvisIRConstants.EXPANDER_FIELD_TYPE);
		}

		@Override
		public String getCanonical() {
			return doc.get(AlvisIRConstants.EXPANDER_FIELD_CANONICAL);
		}

		@Override
		public String[] getSynonyms() {
			return doc.getValues(AlvisIRConstants.EXPANDER_FIELD_SYNONYMS);
		}

		@Override
		public String getLabel() {
			return doc.get(AlvisIRConstants.EXPANDER_FIELD_LABEL);
		}
	}

	@Override
	public ExplanationFactory getExplanationFactory(String type) throws ExpanderException {
		if (explanationFactories.containsKey(type)) {
			return explanationFactories.get(type);
		}
		throw new ExpanderException("unhandled expansion type: " + type);
	}

	@Override
	public void setExplanationFactory(String type, Properties properties) throws ExpanderException {
		ExplanationFactory factory = ensureExplanationFactory(type, properties);
		setExplanationFactoryProperties(factory, properties);
	}
	
	private ExplanationFactory ensureExplanationFactory(String type, Properties props) throws ExpanderIndexerException {
		if (props.containsKey("method")) {
			String method = props.getProperty("method");
			ExplanationFactory result = newExplanationFactory(props, method, type);
			explanationFactories.put(type, result);
			return result;
		}
		if (explanationFactories.containsKey(type)) {
			return explanationFactories.get(type);
		}
		throw new ExpanderIndexerException("unhandled expansion type: " + type);
	}
	
	private static void setExplanationFactoryProperties(ExplanationFactory factory, Properties props) throws ExpanderIndexerException {
		for (Object key0 : props.keySet()) {
			String key = (String) key0;
			if (key.equals("method")) {
				continue;
			}
			factory.setProperty(key, props.getProperty(key));
		}
	}
	
	private ExplanationFactory newExplanationFactory(Properties props, String method, String type) throws ExpanderIndexerException {
		switch (method) {
		case "term":
			return new TermExplanationFactory(type);
		case "path":
			String separator = props.getProperty("separator", "/");
			return new PathExplanationFactory(this, type, separator.charAt(0));
		default:
			throw new ExpanderIndexerException("unknown explanation method: " + method);
		}
	}

	@Override
	public String getProperty(String key) throws IOException {
		Term term = new Term(FIELD_PROPERTY, key);
		TermQuery termQuery = new TermQuery(term);
		try (IndexSearcher searcher = new IndexSearcher(reader)) {
			TopDocs topDocs = searcher.search(termQuery, 1);
			if (topDocs.totalHits < 1) {
				return null;
			}
			Document doc = reader.document(topDocs.scoreDocs[0].doc);
			return doc.get(FIELD_PROPERTY);
		}
	}
}
