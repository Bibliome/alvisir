package fr.inra.mig_bibliome.alvisir.core.expand.index;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.miscellaneous.SingleTokenTokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;

import fr.inra.maiage.bibliome.util.Strings;
import fr.inra.mig_bibliome.alvisir.core.AlvisIRConstants;
import fr.inra.mig_bibliome.alvisir.core.index.NormalizationOptions;

/**
 * Insert expansions into an expander index.
 * @author rbossy
 *
 */
public abstract class ExpanderIndexer {
	private final Map<String,String> properties = new HashMap<String,String>();
	private NormalizationOptions normalizationOptions = NormalizationOptions.DEFAULT;
	private CompoundExpanderIndexer parent = null;
	
	public NormalizationOptions getNormalizationOptions() {
		return normalizationOptions;
	}

	public void setNormalizationOptions(NormalizationOptions normalizationOptions) {
		this.normalizationOptions = normalizationOptions;
	}

	/**
	 * Insert expansions into an expander index.
	 * @param logger logger.
	 * @param indexWriter expander index.
	 * @throws IOException
	 * @throws ExpanderIndexerException 
	 */
	public abstract void indexExpansions(Logger logger, IndexWriter indexWriter) throws IOException, ExpanderIndexerException;

	public void recordProperties(Logger logger, IndexWriter indexWriter) throws IOException {
		for (Map.Entry<String,String> e : properties.entrySet()) {
			String name = e.getKey();
			String value = e.getValue();
			recordProperty(logger, indexWriter, name, value);
		}
	}
	
	public void addProperty(String name, String value) throws ExpanderIndexerException {
		if (properties.containsKey(name)) {
			throw new ExpanderIndexerException("duplicate property: " + name);
		}
		properties.put(name, value);
		if (parent != null) {
			parent.addProperty(name, value);
		}
	}
	
	private static void recordProperty(Logger logger, IndexWriter indexWriter, String name, String value) throws IOException {
		logger.info("recording property: " + name);
		org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
		Field field = new Field(IndexBasedTextExpander.FIELD_PROPERTY, value, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO);
		Token token = new Token();
		token.append(name);
		TokenStream stream = new SingleTokenTokenStream(token);
		field.setTokenStream(stream);
		doc.add(field);
		indexWriter.addDocument(doc);
	}

	protected Document createExpansionDocument(String canonical, String label, String type) {
		Document result = new Document();
		Field canonicalField = new Field(AlvisIRConstants.EXPANDER_FIELD_CANONICAL, normalizationOptions.normalize(canonical), Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO);
		Field labelField = new Field(AlvisIRConstants.EXPANDER_FIELD_LABEL, label, Field.Store.YES, Field.Index.NO, Field.TermVector.NO);
		Field typeField = new Field(AlvisIRConstants.EXPANDER_FIELD_TYPE, type, Field.Store.YES, Field.Index.NO, Field.TermVector.NO);
		result.add(canonicalField);
		result.add(labelField);
		result.add(typeField);
//		System.err.println("canonical = " + canonical);
//		System.err.println("  label = " + label);
//		System.err.println("  type = " + type);
		return result;
	}
	
	protected void addSynonym(Document doc, String synonym) {
//		System.err.println("  synonym = " + synonym);
		Field synonymField = new Field(AlvisIRConstants.EXPANDER_FIELD_SYNONYMS, synonym, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO);
		Collection<String> tokenizedSynonym = Strings.split(synonym, ' ', -1);
		Collection<String> normalizedTokenizedSynonym = normalizationOptions.normalize(tokenizedSynonym);
		String normalizedSynonym = Strings.join(normalizedTokenizedSynonym, ' ');
//		TokenStream tokenStream = normalizationOptions.getTokenStream(normalizedSynonym);
		TokenStream tokenStream = NormalizationOptions.NONE.getTokenStream(normalizedSynonym);
		synonymField.setTokenStream(tokenStream);
		doc.add(synonymField);
//		System.err.println("  normalizedSynonym = " + normalizedSynonym);
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public CompoundExpanderIndexer getParent() {
		return parent;
	}

	void setParent(CompoundExpanderIndexer parent) {
		this.parent = parent;
	}
}
