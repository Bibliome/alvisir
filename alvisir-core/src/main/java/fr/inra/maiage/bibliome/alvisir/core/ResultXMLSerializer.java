package fr.inra.maiage.bibliome.alvisir.core;

import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.logging.LogRecord;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.lucene.search.Query;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import fr.inra.maiage.bibliome.alvisir.core.expand.ExpansionResult;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.AnyMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.CompositeExpansionExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.MatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.NearMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.PhraseMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.PrefixMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.RelationMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.TermMatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.facet.FacetCategory;
import fr.inra.maiage.bibliome.alvisir.core.facet.FacetSpecification;
import fr.inra.maiage.bibliome.alvisir.core.facet.FacetTerm;
import fr.inra.maiage.bibliome.alvisir.core.index.IndexGlobalAttributes;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRQueryNodeDOMConverter;
import fr.inra.maiage.bibliome.alvisir.core.snippet.DocumentSnippet;
import fr.inra.maiage.bibliome.alvisir.core.snippet.FieldSnippet;
import fr.inra.maiage.bibliome.alvisir.core.snippet.FragmentSelector;
import fr.inra.maiage.bibliome.alvisir.core.snippet.Highlight;
import fr.inra.maiage.bibliome.util.Timer;
import fr.inra.maiage.bibliome.util.fragments.DOMBuilderFragmentTagIterator;
import fr.inra.maiage.bibliome.util.fragments.Fragment;
import fr.inra.maiage.bibliome.util.fragments.FragmentComparator;
import fr.inra.maiage.bibliome.util.fragments.FragmentTag;
import fr.inra.maiage.bibliome.util.fragments.FragmentTagIterator;
import fr.inra.maiage.bibliome.util.fragments.SimpleFragment;
import fr.inra.maiage.bibliome.util.mappers.ParamMapper;
import fr.inra.maiage.bibliome.util.xml.XMLUtils;

/**
 * Serializes search result as a XML.
 * @author rbossy
 *
 */
public class ResultXMLSerializer {
//	private final String[] roleNames;
	private final Document originalDoc;
	private Transformer transformer;
	private Result target;
	private Properties params;
	
	/**
	 * Creates a result XML serializer using the specified DOM document.
	 * @param originalDoc DOM document.
	 */
	public ResultXMLSerializer(Document originalDoc) {
		super();
		this.originalDoc = originalDoc;
	}
	
	public ResultXMLSerializer(DocumentBuilder docBuilder) {
		this(docBuilder.newDocument());
	}
	
	public ResultXMLSerializer(DocumentBuilderFactory docBuilderFactory) throws ParserConfigurationException {
		this(docBuilderFactory.newDocumentBuilder());
	}
	
	public ResultXMLSerializer() throws ParserConfigurationException {
		this(DocumentBuilderFactory.newInstance());
	}
	
	public Document getOriginalDoc() {
		return originalDoc;
	}

	public Transformer getTransformer() {
		return transformer;
	}

	public Result getTarget() {
		return target;
	}

	public Properties getParams() {
		return params;
	}

	public void setTransformer(Transformer transformer) {
		this.transformer = transformer;
	}
	
	public void setTransformer(String xslPath) throws TransformerConfigurationException {
		Source xslSource = new StreamSource(xslPath);
		XMLUtils.transformerFactory.newTransformer(xslSource);
		setTransformer(XMLUtils.transformerFactory.newTransformer(xslSource));
	}

	public void setTarget(Result target) {
		this.target = target;
	}
	
	public void setTarget(Document doc) {
		setTarget(new DOMResult(doc));
	}
	
	public void setTarget(OutputStream out) {
		setTarget(new StreamResult(out));
	}

	public void setParams(Properties params) {
		this.params = params;
	}
	
	public void setParams(String paramsPath) throws IOException {
		setParams(new Properties());
		Reader reader = new FileReader(paramsPath);
		params.load(reader);
	}

	public void serialize(SearchResult searchResult) throws TransformerException {
		resultToDOM(searchResult, true);
		if (transformer == null) {
			return;
		}
		if (params != null) {
			for (String name : params.stringPropertyNames()) {
				String value = params.getProperty(name);
				transformer.setParameter(name, value);
			}
		}
		Source source = new DOMSource(originalDoc);
		transformer.transform(source, target);
	}

	private void resultToDOM(SearchResult searchResult, boolean help) throws DOMException {
		Map<MatchExplanation,String> enumExpl = enumerateExplanations(searchResult);
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_RESULT);
		if (help) {
			result.appendChild(helpToDOM(searchResult));
		}
		result.appendChild(timerToDOM(searchResult.getTimer()));
		result.appendChild(messagesToDOM(searchResult.getMessages()));
		result.appendChild(queriesToDOM(searchResult));
		if (!enumExpl.isEmpty()) {
			result.appendChild(hitsToDOM(searchResult));
			result.appendChild(explanationsToDOM(enumExpl));
			result.appendChild(snippetsToDOM(searchResult, enumExpl, searchResult.getDocSnippets()));
			result.appendChild(facetsToDOM(originalDoc, searchResult.getGlobalFacets()));
		}
		originalDoc.appendChild(result);
	}
	
	
	
	
	
	
	private Element helpToDOM(SearchResult searchResult) {
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_HELP);
		SearchConfig searchConfig = searchResult.getSearchConfig();
		IndexGlobalAttributes globalAttributes = searchConfig.getIndex().getGlobalAttributes();
		result.appendChild(fieldNamesHelpToDOM(searchConfig));
		result.appendChild(relationsHelpToDOM(globalAttributes));
		result.appendChild(examplesToDOM(searchConfig.getExamples()));
		return result;
	}

	private Element examplesToDOM(Collection<Example> examples) {
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_EXAMPLES);
		for (Example ex : examples) {
			ExampleCategory cat = ex.getCategory();
			Element elt = originalDoc.createElement(cat.name().toLowerCase());
			elt.setTextContent(ex.getQueryString());
			result.appendChild(elt);
		}
		return result;
	}

	private Element relationsHelpToDOM(IndexGlobalAttributes globalAttributes) {
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_HELP_RELATIONS);
		for (Map.Entry<String,String[]> e : globalAttributes.getRelationInfo().entrySet()) {
			String rel = e.getKey();
			Element elt = originalDoc.createElement(rel);
			for (String role : e.getValue()) {
				Element r = originalDoc.createElement(AlvisIRConstants.XML_RESULT_HELP_ARG);
				r.setTextContent(role);
				elt.appendChild(r);
			}
			result.appendChild(elt);
		}
		return result;
	}

	private Element fieldNamesHelpToDOM(SearchConfig searchConfig) {
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_HELP_FIELD_NAMES);
		for (String fieldName : searchConfig.getIndex().getGlobalAttributes().getFieldNames()) {
			Element elt = originalDoc.createElement(AlvisIRConstants.XML_RESULT_FIELD);
			elt.setTextContent(fieldName);
			result.appendChild(elt);
		}
		for (Map.Entry<String,String[]> e : searchConfig.getFieldAliases().entrySet()) {
			String alias = e.getKey();
			Element elt = originalDoc.createElement(AlvisIRConstants.XML_RESULT_HELP_FIELD_ALIAS);
			elt.setAttribute(AlvisIRConstants.XML_RESULT_HELP_FIELD_ALIAS_NAME, alias);
			for (String field : e.getValue()) {
				Element f = originalDoc.createElement(AlvisIRConstants.XML_RESULT_FIELD);
				f.setTextContent(field);
				elt.appendChild(f);
			}
			result.appendChild(elt);
		}
		Element elt = originalDoc.createElement(AlvisIRConstants.XML_RESULT_HELP_DEFAULT_FIELD);
		elt.setTextContent(searchConfig.getDefaultFieldName());
		result.appendChild(elt);
		return result;
	}

	private Element messagesToDOM(List<LogRecord> messages) {
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_MESSAGES);
		for (LogRecord record : messages) {
			result.appendChild(messageToDOM(record));
		}
		return result;
	}

	private Element messageToDOM(LogRecord record) {
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_MESSAGE);
		result.setAttribute(AlvisIRConstants.XML_RESULT_MESSAGE_LEVEL, record.getLevel().getName());
		Element contents = originalDoc.createElement(AlvisIRConstants.XML_RESULT_MESSAGE_TEXT);
		contents.setTextContent(record.getMessage());
		result.appendChild(contents);
		Throwable thr = record.getThrown();
		if (thr != null) {
			result.appendChild(throwableToDOM(thr));
		}
		return result;
	}

	private Element throwableToDOM(Throwable thr) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		for (Throwable t = thr; t != null; t = t.getCause()) {
			t.printStackTrace(pw);
		}
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_MESSAGE_THROWN);
		result.setTextContent(sw.toString());
		return result;
	}

	private Element queriesToDOM(SearchResult searchResult) throws DOMException {
		Element result = originalDoc.createElement("query");
		String queryString = searchResult.getQueryString();
		if (queryString != null) {
			result.appendChild(queryStringToDOM(queryString));
		}
		SearchConfig searchConfig = searchResult.getSearchConfig();
		if (searchConfig != null) {
			String defaultFieldName = searchConfig.getDefaultFieldName();
			result.setAttribute("default-field", defaultFieldName);
			result.appendChild(queryNodeToDOM(defaultFieldName, searchResult.getOriginalQueryNode(), "original-query"));
			result.appendChild(queryNodeToDOM(defaultFieldName, searchResult.getQueryNode(), "expanded-query"));
			Query luceneQuery = searchResult.getLuceneQuery();
			XMLUtils.createElement(originalDoc, result, 0, "lucene-query", luceneQuery == null ? "" : luceneQuery.toString());
		}
		return result;
	}

	private Element queryNodeToDOM(String defaultFieldName, AlvisIRQueryNode queryNode, String elementName) {
		Element result = originalDoc.createElement(elementName);
		if (queryNode != null) {
			Element elt = queryNode.accept(new AlvisIRQueryNodeDOMConverter(defaultFieldName), originalDoc);
			result.appendChild(elt);
		}
		return result;
	}

	private Element queryStringToDOM(String queryString) {
		Element result = originalDoc.createElement("query-string");
		result.setTextContent(queryString);
		return result;
	}

	private static final String formatTime(long t) {
		return String.format("%6.2f", ((double) t) / 1000000);
	}
	
	private Element timerToDOM(Timer<AlvisIRTimerCategory> timer) {
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_TIMER);
		result.setAttribute(AlvisIRConstants.XML_RESULT_TIMER_NAME, timer.getName());
		result.setAttribute(AlvisIRConstants.XML_RESULT_TIMER_PATH, timer.getPath());
		long t = timer.getTime();
		result.setAttribute(AlvisIRConstants.XML_RESULT_TIMER_TIME, formatTime(t));
		long ct = timer.getChildrenTime();
		result.setAttribute(AlvisIRConstants.XML_RESULT_TIMER_REST, formatTime(t - ct));
		for (Timer<AlvisIRTimerCategory> child : timer.getChildren()) {
			Element childElement = timerToDOM(child);
			result.appendChild(childElement);
		}
		return result;
	}

	private Element hitsToDOM(SearchResult searchResult) {
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_HITS);
		SearchConfig searchConfig = searchResult.getSearchConfig();
		result.setAttribute("total", Integer.toString(searchResult.getTotalHits()));
		result.setAttribute("first", Integer.toString(searchConfig.getStartSnippet()));
		result.setAttribute("snippets", Integer.toString(searchResult.getDocSnippets().length));
		result.setAttribute("max-snippets", Integer.toString(searchConfig.getSnippetCount()));
		return result;
	}

	private static Map<MatchExplanation,String> enumerateExplanations(SearchResult searchResult) {
		ExpansionResult expansionResult = searchResult.getExpansionResult();
		if (expansionResult == null) {
			return Collections.emptyMap();
		}
		Collection<MatchExplanation> explanations = expansionResult.getExplanations();
		Map<MatchExplanation,String> result = new LinkedHashMap<MatchExplanation,String>();
		int near = 0;
		int not_near = 0;
		for (MatchExplanation expl : explanations) {
			String id;
			if (expl instanceof NearMatchExplanation) {
				id = "near_" + (near++);
			}
			else {
				id = Integer.toString(not_near++);
			}
			result.put(expl, id);
		}
		return result;
	}
	
	private Element explanationsToDOM(Map<MatchExplanation,String> enumExpl) {
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_EXPLANATIONS);
		for (MatchExplanation expl : enumExpl.keySet()) {
			if (!(expl instanceof NearMatchExplanation)) {
				result.appendChild(explanationBitsToDOM(enumExpl, expl));
			}
		}
		return result;
	}
	
	private Element explanationBitsToDOM(Map<MatchExplanation,String> enumExpl, MatchExplanation expl) {
		Element result = originalDoc.createElement(AlvisIRConstants.EXPLANATION_KEY_EXPLANATION);
		result.setAttribute(AlvisIRConstants.XML_RESULT_EXPLANATION_ID, enumExpl.get(expl));
		result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_FIELD, expl.getFieldName());
		result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_CLASS, expl.getClass().getCanonicalName());
		result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_PRODUCTIVITY, Integer.toString(expl.getProductivity()));
		if (expl.hasLabel()) {
			result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_LABEL, expl.getLabel());
		}
		if (expl.hasType()) {
			result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_TYPE, expl.getType());
		}
		if (expl.hasMoreSynonyms()) {
			Element mse = originalDoc.createElement(AlvisIRConstants.EXPLANATION_KEY_MORE_SYNONYMS);
			mse.setTextContent(Integer.toString(expl.getMoreSynonyms()));
			result.appendChild(mse);
		}
		if (expl.hasSynonyms()) {
			for (String syn : expl.getSynonyms()) {
				Element se = originalDoc.createElement(AlvisIRConstants.EXPLANATION_KEY_SYNONYM);
				se.setTextContent(syn);
				result.appendChild(se);
			}
		}
		if (expl instanceof CompositeExpansionExplanation) {
			explanationBitsToDOM(enumExpl, (CompositeExpansionExplanation) expl, result);
		}
		else if (expl instanceof PhraseMatchExplanation) {
			explanationBitsToDOM((PhraseMatchExplanation) expl, result);
		}
		else if (expl instanceof PrefixMatchExplanation) {
			explanationBitsToDOM((PrefixMatchExplanation) expl, result);
		}
		else if (expl instanceof TermMatchExplanation) {
			explanationBitsToDOM((TermMatchExplanation) expl, result);
		}
		else if (expl instanceof RelationMatchExplanation) {
			explanationBitsToDOM((RelationMatchExplanation) expl, result);
		}
		else if (expl instanceof AnyMatchExplanation) {
			// nothing to be done
		}
		else {
			throw new RuntimeException("unhandled explnation: " + expl);
		}
		return result;
	}

	private static void explanationBitsToDOM(TermMatchExplanation expl, Element result) {
		result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_TEXT, expl.getText());
		result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_NORMALIZED, expl.getNormalizedText());
	}

	private void explanationBitsToDOM(PrefixMatchExplanation expl, Element result) {
		result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_PREFIX, expl.getPrefix());
		result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_NORMALIZED, expl.getNormalizedPrefix());
		if (expl.hasMoreSubpaths()) {
			Element mse = originalDoc.createElement(AlvisIRConstants.EXPLANATION_KEY_MORE_SUBPATHS);
			mse.setTextContent(Integer.toString(expl.getMoreSubpaths()));
			result.appendChild(mse);
		}
		if (expl.hasSubpaths()) {
			String[] subpaths = expl.getSubpaths();
			String[] subpathLabels = expl.getSubpathLabels();
			for (int i = 0; i < subpaths.length; ++i) {
				String subpath = subpaths[i];
				String subpathLabel = subpathLabels[i];
				Element se = originalDoc.createElement(AlvisIRConstants.EXPLANATION_KEY_SUBPATH);
				se.setAttribute(AlvisIRConstants.EXPLANATION_KEY_PATH_LABEL, subpathLabel);
				se.setTextContent(subpath);
				result.appendChild(se);
			}
		}
	}

	private void explanationBitsToDOM(PhraseMatchExplanation expl, Element result) {
		result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_SLOP, Integer.toString(expl.getSlop()));
		String[] texts = expl.getTexts();
		String[] normalizedTexts = expl.getNormalizedTexts();
		for (int i = 0; i < texts.length; ++i) {
			String text = texts[i];
			String normalizedText = normalizedTexts[i];
			Element te = originalDoc.createElement(AlvisIRConstants.EXPLANATION_KEY_TERM);
			te.setAttribute(AlvisIRConstants.EXPLANATION_KEY_NORMALIZED, normalizedText);
			te.setTextContent(text);
			result.appendChild(te);
		}
	}

	private static void explanationBitsToDOM(RelationMatchExplanation expl, Element result) {
		result.setAttribute(AlvisIRConstants.EXPLANATION_KEY_RELATION, expl.getRelation());
	}

	private void explanationBitsToDOM(Map<MatchExplanation,String> enumExpl, CompositeExpansionExplanation expl, Element result) {
		for (MatchExplanation child : expl.getexplanations()) {
			Element ce = explanationBitsToDOM(enumExpl, child);
			result.appendChild(ce);
		}
	}

	private Element snippetsToDOM(SearchResult searchResult, Map<MatchExplanation,String> enumExpl, DocumentSnippet[] docSnippets) {
		Element result = originalDoc.createElement(AlvisIRConstants.XML_RESULT_SNIPPETS);
		for (DocumentSnippet docSnippet : docSnippets) {
			result.appendChild(docSnippetToDOM(searchResult, originalDoc, enumExpl, docSnippet));
		}
		return result;
	}

	private static Element docSnippetToDOM(SearchResult searchResult, Document doc, Map<MatchExplanation,String> enumExpl, DocumentSnippet docSnippet) {
		Element result = doc.createElement(AlvisIRConstants.XML_RESULT_DOCUMENT_SNIPPET);
		result.setAttribute(AlvisIRConstants.XML_RESULT_DOCUMENT_SCORE, Float.toString(docSnippet.getScore()));
		for (FieldSnippet fieldSnippet : docSnippet.getFieldSnippets()) {
			result.appendChild(fieldSnippetToDOM(searchResult, doc, enumExpl, fieldSnippet));
		}
		result.appendChild(facetsToDOM(doc, docSnippet.getFacets()));
		return result;
	}

	private static Element fieldSnippetToDOM(SearchResult searchResult, Document doc, Map<MatchExplanation,String> enumExpl, FieldSnippet fieldSnippet) {
		Element result = doc.createElement(AlvisIRConstants.XML_RESULT_FIELD_SNIPPET);
		result.setAttribute(AlvisIRConstants.XML_RESULT_FIELD_NAME, fieldSnippet.getFieldName());
		result.setAttribute(AlvisIRConstants.XML_RESULT_FIELD_ORD, Integer.toString(fieldSnippet.getFieldInstance()));
		String contents = fieldSnippet.getFieldValue();
		SearchConfig searchConfig = searchResult.getSearchConfig();
		String[] roleNames = searchConfig.getIndex().getGlobalAttributes().getAllRoleNames();
		String[] keyNames = searchConfig.getIndex().getGlobalAttributes().getPropertyKeys();
		HighlightFragmentElementBuilder elementBuilder = new HighlightFragmentElementBuilder(roleNames, keyNames, enumExpl);
		Map<Fragment,Collection<Highlight>> fragmentHighlightMap = FragmentSelector.getFragmentHighlights(fieldSnippet);
		for (Fragment frag : fieldSnippet.getFragments()) {
			Collection<Highlight> highlights;
			if (fragmentHighlightMap.containsKey(frag)) {
				highlights = fragmentHighlightMap.get(frag);
			}
			else {
				highlights = Collections.emptyList();
			}
			int start = frag.getStart();
			int end = Math.min(frag.getEnd(), contents.length());
			String fragmentContents = contents.substring(start, end);
			Element elt = fragmentToDOM(doc, fragmentContents, highlights, elementBuilder, start);
			result.appendChild(elt);
		}
		return result;
	}

	private static final class HighlightFragmentElementBuilder implements ParamMapper<HighlightFragment,Element,Document> {
		private final String[] roleNames;
		private final String[] keyNames;
		private final Map<MatchExplanation,String> explanationMap;
		
		private HighlightFragmentElementBuilder(String[] roleNames, String[] keyNames, Map<MatchExplanation,String> explanationMap) {
			super();
			this.roleNames = roleNames;
			this.keyNames = keyNames;
			this.explanationMap = explanationMap;
		}

		@Override
		public Element map(HighlightFragment x, Document param) {
			Element result = param.createElement(AlvisIRConstants.XML_RESULT_HIGHLIGHT);
			result.setAttribute(AlvisIRConstants.XML_RESULT_HIGHLIGHT_ANNOTATION, Integer.toString(x.highlight.getIdentifier()));
			result.setAttribute(AlvisIRConstants.XML_RESULT_HIGHLIGHT_ORD, Integer.toString(x.ord));
			result.setAttribute(AlvisIRConstants.XML_RESULT_HIGHLIGHT_EXPLANATION, explanationMap.get(x.highlight.getExplanation()));
			if (x.ord == 0 && x.highlight.isRelation()) {
				for (Map.Entry<Integer,Integer> e : x.highlight.getArguments().entrySet()) {
					int roleId = e.getKey();
					String role;
					if (roleId >= 0 && roleId < roleNames.length) {
						role = roleNames[roleId];
					}
					else {
						role = Integer.toString(roleId);
					}
					String value = e.getValue().toString();
					Element argElt = param.createElement(AlvisIRConstants.XML_RESULT_HIGHLIGHT_ARG);
					argElt.setAttribute(AlvisIRConstants.XML_RESULT_HIGHLIGHT_ARG_ROLE, role);
					argElt.setAttribute(AlvisIRConstants.XML_RESULT_HIGHLIGHT_ARG_REF, value);
					result.appendChild(argElt);
				}
			}
			if (x.highlight.hasProperties()) {
				for (Map.Entry<Integer,String> e : x.highlight.getProperties().entrySet()) {
					int keyId = e.getKey();
					String key;
					if (keyId >= 0 && keyId < keyNames.length) {
						key = keyNames[keyId];
					}
					else {
						key = Integer.toString(keyId);
					}
					String value = e.getValue();
					Element argElt = param.createElement(AlvisIRConstants.XML_RESULT_HIGHLIGHT_PROP);
					argElt.setAttribute(AlvisIRConstants.XML_RESULT_HIGHLIGHT_PROP_KEY, key);
					argElt.setAttribute(AlvisIRConstants.XML_RESULT_HIGHLIGHT_PROP_VALUE, value);
					result.appendChild(argElt);
				}
			}
			return result;
		}
	}
	
	private static final class HighlightFragment extends SimpleFragment {
		private final Highlight highlight;
		private final int ord;
		
		private HighlightFragment(Highlight highlight, Fragment frag, int ord) {
			super(frag.getStart(), frag.getEnd());
			this.highlight = highlight;
			this.ord = ord;
		}
	}

	private static final Comparator<HighlightFragment> HIGHLIGHT_FRAGMENT_COMPARATOR = new FragmentComparator<HighlightFragment>() {
		@Override
		public int compare(HighlightFragment o1, HighlightFragment o2) {
			int r = super.compare(o1, o2);
			if (r == 0) {
				return o1.highlight.hashCode() - o2.highlight.hashCode();
			}
			return r;
		}
	};
	
	private static Element fragmentToDOM(Document doc, String contents, Collection<Highlight> highlights, HighlightFragmentElementBuilder elementBuilder, int offset) {
		Element result = doc.createElement(AlvisIRConstants.XML_RESULT_FRAMENT);
		Collection<HighlightFragment> highlightFragments = getHighlightFragments(highlights);
		FragmentTagIterator<String,HighlightFragment> tagIterator = new DOMBuilderFragmentTagIterator<HighlightFragment>(elementBuilder, doc, result);
		FragmentTag.iterateFragments(tagIterator , contents, highlightFragments, offset);
		return result;
	}
	
	private static Collection<HighlightFragment> getHighlightFragments(Collection<Highlight> highlights) {
		Collection<HighlightFragment> hilightFragments = new TreeSet<HighlightFragment>(HIGHLIGHT_FRAGMENT_COMPARATOR);
		for (Highlight h : highlights) {
			int ord = 0;
			for (Fragment frag : h.getFragments()) {
				HighlightFragment hf = new HighlightFragment(h, frag, ord++);
				hilightFragments.add(hf);
			}
		}
		return hilightFragments;
	}

	
	
	

	private static Element facetsToDOM(Document doc, Collection<FacetCategory> facets) {
		Element result = doc.createElement(AlvisIRConstants.XML_RESULT_FACETS);
		for (FacetCategory facet : facets) {
			result.appendChild(facetToDOM(doc, facet));
		}
		return result;
	}

	private static Element facetToDOM(Document doc, FacetCategory facet) {
		Element result = doc.createElement(AlvisIRConstants.XML_RESULT_FACET);
		FacetSpecification spec = facet.getSpecification();
		result.setAttribute(AlvisIRConstants.XML_RESULT_FACET_NAME, spec.getName());
		result.setAttribute(AlvisIRConstants.XML_RESULT_FACET_FIELD, spec.getFieldName());
		result.setAttribute(AlvisIRConstants.XML_RESULT_FACET_QUERY_FIELD, spec.getQueryFieldName());
		result.setAttribute(AlvisIRConstants.XML_RESULT_FACET_QUERY_TYPE, spec.getSubQueryType().toString());
		result.setAttribute(AlvisIRConstants.XML_RESULT_FACET_LABEL_QUERY, Boolean.toString(spec.isLabelQuery()));
		for (FacetTerm info : facet.getTerms()) {
			result.appendChild(facetInfoToDOM(doc, spec, info));
		}
		return result;
	}

	private static Element facetInfoToDOM(Document doc, FacetSpecification spec, FacetTerm info) {
		Element result = doc.createElement(AlvisIRConstants.XML_RESULT_FACET_TERM);
		result.setAttribute(AlvisIRConstants.XML_RESULT_FACET_CANONICAL, info.getTerm());
		result.setAttribute(AlvisIRConstants.XML_RESULT_FACET_LABEL, info.getLabel());
		result.setAttribute(AlvisIRConstants.XML_RESULT_FACET_COUNT, Long.toString(info.get()));
		result.setAttribute(AlvisIRConstants.XML_RESULT_FACET_DOCS, Long.toString(info.getDocFreq()));
		result.setAttribute(AlvisIRConstants.XML_RESULT_FACET_SUB_QUERY, spec.getSubQuery(info));
		return result;
	}
}
