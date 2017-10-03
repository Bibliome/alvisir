package fr.inra.mig_bibliome.alvisir.core;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.bibliome.util.Strings;
import org.bibliome.util.xml.XMLUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.SAXException;

import fr.inra.mig_bibliome.alvisir.core.expand.ExpanderException;
import fr.inra.mig_bibliome.alvisir.core.expand.NullTextExpander;
import fr.inra.mig_bibliome.alvisir.core.expand.QueryNodeExpanderFactory;
import fr.inra.mig_bibliome.alvisir.core.expand.TextExpander;
import fr.inra.mig_bibliome.alvisir.core.expand.index.IndexBasedTextExpander;
import fr.inra.mig_bibliome.alvisir.core.facet.CapitalizingFacetLabelFactory;
import fr.inra.mig_bibliome.alvisir.core.facet.ExpansionFacetLabelFactory;
import fr.inra.mig_bibliome.alvisir.core.facet.FacetLabelFactory;
import fr.inra.mig_bibliome.alvisir.core.facet.FacetSort;
import fr.inra.mig_bibliome.alvisir.core.facet.FacetSpecification;
import fr.inra.mig_bibliome.alvisir.core.facet.FacetSubQueryType;
import fr.inra.mig_bibliome.alvisir.core.facet.LowerCaseFacetLabelFactory;
import fr.inra.mig_bibliome.alvisir.core.facet.PrefixFacetSpecification;
import fr.inra.mig_bibliome.alvisir.core.facet.RegexGroupFacetLabelFactory;
import fr.inra.mig_bibliome.alvisir.core.facet.UpperCaseFacetLabelFactory;
import fr.inra.mig_bibliome.alvisir.core.index.NormalizationOptions;

/**
 * Search configuration XML serializer.
 * @author rbossy
 *
 */
public class SearchConfigXMLSerializer {
	/**
	 * Returns a fresh search object using settings in the specified XML file.
	 * @param path path to the XML file.
	 * @return
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws ExpanderException 
	 * @throws SearchConfigException 
	 */
	public static final SearchConfig getSearch(String path) throws IOException, SAXException, ParserConfigurationException, ExpanderException, SearchConfigException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(path);
		String basedir = new File(path).getParent();
		return getSearch(basedir, doc);
	}

	/**
	 * Returns a fresh search object using settings in the specified DOM document.
	 * @param doc DOM document.
	 * @return
	 * @throws IOException
	 * @throws ExpanderException 
	 * @throws SearchConfigException 
	 */
	public static final SearchConfig getSearch(String basedir, Document doc) throws IOException, ExpanderException, SearchConfigException {
		return getSearch(basedir, doc.getDocumentElement());
	}
	
	private static void checkAttribute(Element elt, String attribute) throws SearchConfigException {
		if (!elt.hasAttribute(attribute)) {
			throw new SearchConfigException("missing attribute " + attribute + " in " + elt.getTagName());
		}
	}
	
	private static String getFileAttribute(String basedir, Element elt, String attribute) {
		String value = elt.getAttribute(attribute);
		File f = new File(value);
		if (!f.isAbsolute()) {
			return new File(basedir, value).getAbsolutePath();
		}
		return value;
	}

	/**
	 * Returns a fresh search object using settings in the specified DOM element.
	 * @param elt DOM element.
	 * @return
	 * @throws IOException
	 * @throws ExpanderException 
	 * @throws SearchConfigException 
	 */
	public static final SearchConfig getSearch(String basedir, Element elt) throws IOException, ExpanderException, SearchConfigException {
		SearchConfig result = new SearchConfig();
		checkAttribute(elt, AlvisIRConstants.XML_SEARCH_INDEX_DIR);
		checkAttribute(elt, AlvisIRConstants.XML_SEARCH_DEFAULT_FIELD);
		AlvisIRIndex index = new AlvisIRIndex(getFileAttribute(basedir, elt, AlvisIRConstants.XML_SEARCH_INDEX_DIR));
		TextExpander textExpander = NullTextExpander.INSTANCE;
		if (elt.hasAttribute(AlvisIRConstants.XML_SEARCH_EXPANDER_INDEX_DIR)) {
			IndexReader expanderIndexReader = openIndexReader(getFileAttribute(basedir, elt, AlvisIRConstants.XML_SEARCH_EXPANDER_INDEX_DIR));
			textExpander = new IndexBasedTextExpander(expanderIndexReader);
		}
		result.setTextExpander(textExpander);
		result.setIndex(index);
		result.setDefaultFieldName(elt.getAttribute(AlvisIRConstants.XML_SEARCH_DEFAULT_FIELD));
		if (elt.hasAttribute(AlvisIRConstants.XML_SEARCH_QUERY_EXPANSION)) {
			String queryExpansion = elt.getAttribute(AlvisIRConstants.XML_SEARCH_QUERY_EXPANSION);
			switch (queryExpansion) {
				case AlvisIRConstants.XML_SEARCH_QUERY_EXPANSION_BASIC:
					result.setQueryNodeExpanderFactory(QueryNodeExpanderFactory.BASIC);
					break;
				case AlvisIRConstants.XML_SEARCH_QUERY_EXPANSION_ADVANCED:
					result.setQueryNodeExpanderFactory(QueryNodeExpanderFactory.ADVANCED);
					break;
				default:
					throw new SearchConfigException("unknown search query expansion: " + queryExpansion + " (should be 'basic' or 'advanced')");
			}
		}
		readXMLConfig(result, elt);
		return result;
	}
	
	private static final IndexReader openIndexReader(String path) throws IOException {
		Directory indexDir = FSDirectory.open(new File(path));
		return IndexReader.open(indexDir);
	}

	/**
	 * Set settings to the specified search object from the specified DOM element.
	 * @param search search object.
	 * @param elt DOM element.
	 * @throws ExpanderException 
	 * @throws SearchConfigException 
	 */
	public static void readXMLConfig(SearchConfig search, Element elt) throws ExpanderException, SearchConfigException {
		for (Element child : XMLUtils.childrenElements(elt)) {
			String name = child.getTagName();
			switch (name) {
				case AlvisIRConstants.XML_SEARCH_EXPANSION_FACTORY:
					readXMLExpansionType(search, child);
					break;
				case AlvisIRConstants.XML_SEARCH_FIELD_ALIAS:
					readXMLFieldAlias(search, child);
					break;
				case AlvisIRConstants.XML_SEARCH_NORMALIZATION:
					readNormalizationOptions(search, child);
					break;
				case AlvisIRConstants.XML_SEARCH_FIELD_FRAGMENTS:
					readXMLFieldFragment(search, child);
					break;
				case AlvisIRConstants.XML_SEARCH_MANDATORY_FIELD:
					readXMLMandatoryField(search, child);
					break;
				case AlvisIRConstants.XML_SEARCH_GLOBAL_FACET:
					search.addGlobalFacetSpecification(readXMLFacetSpecification(search, child));
					break;
				case AlvisIRConstants.XML_SEARCH_DOCUMENT_FACET:
					search.addDocumentFacetSpecification(readXMLFacetSpecification(search, child));
					break;
				case AlvisIRConstants.XML_SEARCH_COUNTS:
					readXMLSearchCount(search, child);
					break;
				case AlvisIRConstants.XML_SEARCH_HIGHLIHT_CLUSTER_RADIUS:
					search.setHighlightClusterRadius(readXMLHighlightClusterRadius(child));
					break;
				case AlvisIRConstants.XML_SEARCH_EXAMPLES:
					readXMLExamples(search, child);
					break;
				default:
					throw new SearchConfigException("unsupported element " + name);
			}
		}
	}

//	private static void readNormalizationOptions(SearchConfig search, Element elt) throws SearchConfigException {
//		checkAttribute(elt, AlvisIRConstants.XML_SEARCH_LOWER_CASE);
//		checkAttribute(elt, AlvisIRConstants.XML_SEARCH_ASCII_FOLDING);
//		boolean lowerCase = XMLUtils.getBooleanAttribute(elt, AlvisIRConstants.XML_SEARCH_LOWER_CASE, true);
//		boolean asciiFolding = XMLUtils.getBooleanAttribute(elt, AlvisIRConstants.XML_SEARCH_ASCII_FOLDING, true);
//		NormalizationOptions normalizationOptions = new NormalizationOptions(lowerCase, asciiFolding);
//		if (elt.hasAttribute(AlvisIRConstants.XML_SEARCH_FIELD)) {
//			String fieldName = elt.getAttribute(AlvisIRConstants.XML_SEARCH_FIELD);
//			search.setNormalizationOptions(fieldName, normalizationOptions);
//		}
//		else {
//			search.setDefaultNormalizationOptions(normalizationOptions);
//		}
//	}
	
	private static void readXMLExamples(SearchConfig search, Element elt) throws SearchConfigException {
		for (Element child : XMLUtils.childrenElements(elt)) {
			search.addExample(readXMLExample(child));
		}
	}

	private static Example readXMLExample(Element child) throws SearchConfigException {
		ExampleCategory category = ExampleCategory.getCategory(child.getTagName());
		if (category == null) {
			throw new SearchConfigException("unknown example category: " + child.getTagName());
		}
		String queryString = child.getTextContent();
		return new Example(category, queryString);
	}

	private static int readXMLHighlightClusterRadius(Element child) throws SearchConfigException {
		String sValue = child.getTextContent();
		try {
			return Integer.parseInt(sValue);
		}
		catch (NumberFormatException e) {
			throw new SearchConfigException(e);
		}
	}

	private static void readNormalizationOptions(SearchConfig search, Element elt) throws SearchConfigException {
		NormalizationOptions result = NormalizationOptions.NONE;
		List<String> norms = Strings.splitAndTrim(elt.getTextContent(), ',', -1);
		for (String norm : norms) {
			result = NormalizationOptions.getFilter(norm, result);
			if (result == null) {
				throw new SearchConfigException("unknown normalization option: " + norm);
			}
		}
		if (elt.hasAttribute(AlvisIRConstants.XML_SEARCH_FIELD)) {
			String fieldName = elt.getAttribute(AlvisIRConstants.XML_SEARCH_FIELD);
//			System.err.println("fieldName = " + fieldName);
			search.setNormalizationOptions(fieldName, result);
		}
		else {
			search.setDefaultNormalizationOptions(result);
		}
//		System.err.println("result = " + result);
	}
	
	private static void readXMLSearchCount(SearchConfig search, Element elt) {
		search.setFacetCount(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_SEARCH_COUNTS_FACETS, 100));
		search.setSnippetCount(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_SEARCH_COUNTS_SNIPPETS, 10));
		search.setStartSnippet(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_SEARCH_COUNTS_START, 0));
	}

	private static FacetSpecification readXMLFacetSpecification(SearchConfig config, Element elt) throws SearchConfigException {
		checkAttribute(elt, AlvisIRConstants.XML_SEARCH_FACET_NAME);
		checkAttribute(elt, AlvisIRConstants.XML_SEARCH_FACET_FIELD);
		checkAttribute(elt, AlvisIRConstants.XML_SEARCH_FACET_PREFIX);
		String name = elt.getAttribute(AlvisIRConstants.XML_SEARCH_FACET_NAME);
		String fieldName = elt.getAttribute(AlvisIRConstants.XML_SEARCH_FACET_FIELD);
		FacetLabelFactory labelFactory = getFacetLabelFactory(config, elt);
//		System.err.println("labelFactory = " + labelFactory);
		String queryFieldName = XMLUtils.getAttribute(elt, AlvisIRConstants.XML_SEARCH_FACET_QUERY_FIELD, fieldName);
		FacetSubQueryType subQueryType = getFacetSubQueryType(elt);
//		System.err.println("subQueryType = " + subQueryType);
		boolean labelQuery = XMLUtils.getBooleanAttribute(elt, AlvisIRConstants.XML_SEARCH_FACET_LABEL_QUERY, false);
		String prefix = elt.getAttribute(AlvisIRConstants.XML_SEARCH_FACET_PREFIX);
		FacetSort sort = getFacetSort(elt);
		int cutoff = XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_SEARCH_FACET_CUTOFF, 0);
		int maxFacets = XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_SEARCH_MAX_FACETS, Integer.MAX_VALUE);
		return new PrefixFacetSpecification(name, fieldName, labelFactory, queryFieldName, subQueryType, labelQuery, sort, cutoff, maxFacets, prefix);
	}
	
	private static final FacetLabelFactory getFacetLabelFactory(SearchConfig search, Element elt) {
		FacetLabelFactory labelFactory;
		boolean defaultCapitalize;
		if (elt.hasAttribute(AlvisIRConstants.XML_SEARCH_LABEL_REGEXP)) {
			String sPattern = elt.getAttribute(AlvisIRConstants.XML_SEARCH_LABEL_REGEXP);
			Pattern pattern = Pattern.compile(sPattern);
			int labelGroup = XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_SEARCH_LABEL_GROUP, 0);
			labelFactory = new RegexGroupFacetLabelFactory(pattern, labelGroup);
			defaultCapitalize = true;
		}
		else {
			labelFactory = new ExpansionFacetLabelFactory(search.getTextExpander());
			defaultCapitalize = false;
		}
		if (XMLUtils.getBooleanAttribute(elt, AlvisIRConstants.XML_SEARCH_LABEL_CAPITALIZE, defaultCapitalize)) {
			labelFactory = new CapitalizingFacetLabelFactory(labelFactory);
		}
		if (XMLUtils.getBooleanAttribute(elt, AlvisIRConstants.XML_SEARCH_LABEL_UPPER_CASE, defaultCapitalize)) {
			labelFactory = new UpperCaseFacetLabelFactory(labelFactory);
		}
		if (XMLUtils.getBooleanAttribute(elt, AlvisIRConstants.XML_SEARCH_LABEL_LOWER_CASE, defaultCapitalize)) {
			labelFactory = new LowerCaseFacetLabelFactory(labelFactory);
		}
		return labelFactory;
	}

	private static final FacetSubQueryType getFacetSubQueryType(Element elt) {
		if (!elt.hasAttribute(AlvisIRConstants.XML_SEARCH_FACET_SUB_QUERY_TYPE)) {
			return FacetSubQueryType.PHRASE;
		}
		String s = elt.getAttribute(AlvisIRConstants.XML_SEARCH_FACET_SUB_QUERY_TYPE);
		switch (s.toLowerCase()) {
		case "term": return FacetSubQueryType.TERM;
		case "phrase": return FacetSubQueryType.PHRASE;
		case "prefix": return FacetSubQueryType.PREFIX;
		case "raw": return FacetSubQueryType.RAW;
		}
		return FacetSubQueryType.PHRASE;
	}
	
	private static final FacetSort getFacetSort(Element elt) throws SearchConfigException {
		if (!elt.hasAttribute(AlvisIRConstants.XML_SEARCH_FACET_SORT)) {
			return FacetSort.TERM_FREQUENCY;
		}
		String value = elt.getAttribute(AlvisIRConstants.XML_SEARCH_FACET_SORT);
		switch (value) {
			case AlvisIRConstants.XML_SEARCH_FACET_SORT_TERM_FREQUENCY:
				return FacetSort.TERM_FREQUENCY;
			case AlvisIRConstants.XML_SEARCH_FACET_SORT_DOCUMENT_FREQUENCY:
				return FacetSort.DOCUMENT_FREQUENCY;
			default:
				throw new SearchConfigException("unknown facet sort: " + value);
		}
	}

	private static void readXMLMandatoryField(SearchConfig search, Element elt) {
		String fieldName = elt.getTextContent();
		search.addMandatoryField(fieldName);
		search.setWholeSnippetField(fieldName);
	}

	private static void readXMLFieldFragment(SearchConfig search, Element elt) throws SearchConfigException {
		checkAttribute(elt, AlvisIRConstants.XML_SEARCH_FRAGMENTS_FIELD);
		checkAttribute(elt, AlvisIRConstants.XML_SEARCH_FRAGMENTS_BUILDER);
		String fieldName = elt.getAttribute(AlvisIRConstants.XML_SEARCH_FRAGMENTS_FIELD);
		if (search.getMandatoryFields().contains(fieldName)) {
			return;
		}
		String builder = elt.getAttribute(AlvisIRConstants.XML_SEARCH_FRAGMENTS_BUILDER);
		switch (builder) {
			case AlvisIRConstants.XML_SEARCH_FRAGMENTS_BUILDER_SILENT:
				search.setSilentField(fieldName);
				break;
			case AlvisIRConstants.XML_SEARCH_FRAGMENTS_BUILDER_WHOLE:
				search.setWholeSnippetField(fieldName);
				break;
			case AlvisIRConstants.XML_SEARCH_FRAGMENTS_BUILDER_SENTENCE:
				checkAttribute(elt, AlvisIRConstants.XML_SEARCH_FRAGMENTS_BUILDER_SENTENCE_ANNOTATION);
				search.setSentenceSnippetField(fieldName, elt.getAttribute(AlvisIRConstants.XML_SEARCH_FRAGMENTS_BUILDER_SENTENCE_ANNOTATION));
				break;
			default:
				throw new SearchConfigException("unknown fragment builder: " + builder);
		}
	}

	private static void readXMLFieldAlias(SearchConfig search, Element elt) throws SearchConfigException {
		checkAttribute(elt, AlvisIRConstants.XML_SEARCH_FIELD_ALIAS_NAME);
		String alias = elt.getAttribute(AlvisIRConstants.XML_SEARCH_FIELD_ALIAS_NAME);
		List<Element> children = XMLUtils.childrenElements(elt);
		String fieldNames[] = new String[children.size()];
		for (int i = 0; i < fieldNames.length; ++i) {
			fieldNames[i] = children.get(i).getTextContent();
		}
		search.addFieldAlias(alias, fieldNames);
	}

	private static void readXMLExpansionType(SearchConfig search, Element elt) throws ExpanderException, SearchConfigException {
		String type = null;
		Properties properties = new Properties();
		NamedNodeMap attributes = elt.getAttributes();
		int n = attributes.getLength();
		for (int i = 0; i < n; ++i) {
			Attr attr = (Attr) attributes.item(i);
			String name = attr.getName();
			if (name.equals(AlvisIRConstants.XML_SEARCH_EXPANSION_TYPE)) {
				type = attr.getValue();
				continue;
			}
			properties.setProperty(name, attr.getValue());
		}
		if (type == null) {
			throw new SearchConfigException("missing expansion type specification");
		}
		TextExpander textExpander = search.getTextExpander();
		textExpander.setExplanationFactory(type, properties);
	}
}
