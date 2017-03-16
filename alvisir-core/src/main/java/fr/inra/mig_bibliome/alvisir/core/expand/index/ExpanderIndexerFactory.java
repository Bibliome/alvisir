package fr.inra.mig_bibliome.alvisir.core.expand.index;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.bibliome.util.Files;
import org.bibliome.util.FlushedStreamHandler;
import org.bibliome.util.StandardFormatter;
import org.bibliome.util.Strings;
import org.bibliome.util.streams.CompressionFilter;
import org.bibliome.util.streams.PatternFileFilter;
import org.bibliome.util.streams.SourceStream;
import org.bibliome.util.streams.StreamFactory;
import org.bibliome.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import fr.inra.mig_bibliome.alvisir.core.AlvisIRConstants;
import fr.inra.mig_bibliome.alvisir.core.index.NormalizationOptions;

/**
 * Creates an expander index from a DOM structure.
 * @author rbossy
 *
 */
public class ExpanderIndexerFactory {
	/**
	 * Creates an expander indexer from the specified XML file.
	 * @param path path to the XML file.
	 * @return
	 * @throws SAXException
	 * @throws IOException
	 * @throws ParserConfigurationException
	 * @throws URISyntaxException
	 * @throws OBOParseException
	 * @throws ExpanderIndexerException 
	 */
	public static ExpanderIndexer getExpanderIndexer(String path) throws SAXException, IOException, ParserConfigurationException, ExpanderIndexerException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
		Document doc = docBuilder.parse(path);
		return getExpanderIndexer(doc);
	}
	
	/**
	 * Creates an expander indexer from the specified DOM document.
	 * @param doc
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws OBOParseException
	 * @throws ExpanderIndexerException 
	 */
	public static ExpanderIndexer getExpanderIndexer(Document doc) throws IOException, ExpanderIndexerException {
		return getExpanderIndexer(doc.getDocumentElement(), NormalizationOptions.DEFAULT);
	}
	
	/**
	 * Creates an expander indexer from the specified element.
	 * @param elt
	 * @return
	 * @throws IOException
	 * @throws URISyntaxException
	 * @throws OBOParseException
	 * @throws ExpanderIndexerException 
	 */
	public static ExpanderIndexer getExpanderIndexer(Element elt, NormalizationOptions normalizationOptions) throws IOException, ExpanderIndexerException {
		if (elt.hasAttribute(AlvisIRConstants.XML_EXPANDER_NORMALIZATION)) {
			normalizationOptions = NormalizationOptions.NONE;
			String normsStr = elt.getAttribute(AlvisIRConstants.XML_EXPANDER_NORMALIZATION);
			List<String> norms = Strings.splitAndTrim(normsStr, ',', -1);
			for (String norm : norms) {
				normalizationOptions = NormalizationOptions.getFilter(norm, normalizationOptions);
				if (normalizationOptions == null) {
					throw new ExpanderIndexerException("unknown normalization option: " + norm);
				}
			}
		}
		String tag = elt.getTagName();
		switch (tag) {
			case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL:
				return getSortedHorizontal(elt, normalizationOptions);
			case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL:
				return getSortedVertical(elt, normalizationOptions);
			case AlvisIRConstants.XML_EXPANDER_INDEXER_OBO:
				return getOBO(elt, normalizationOptions);
			case AlvisIRConstants.XML_EXPANDER_INDEXER_COMPOUND:
				return getCompound(elt, normalizationOptions);
			default:
				throw new ExpanderIndexerException("unknown expander type '" + tag + "'");
		}
	}
	
	private static OBOExpanderIndexer getOBO(Element elt, NormalizationOptions normalizationOptions) throws IOException, ExpanderIndexerException {
		OBOExpanderIndexer result = new OBOExpanderIndexer();
		result.setNormalizationOptions(normalizationOptions);
		if (elt.hasAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_SOURCE)) {
			result.parse(elt.getAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_SOURCE));
		}
		if (elt.hasAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_PREFIX)) {
			result.setPrefix(elt.getAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_PREFIX));
		}
		if (elt.hasAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_TYPE)) {
			result.setType(elt.getAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_TYPE));
		}
		for (Element child : XMLUtils.childrenElements(elt)) {
			String tag = child.getTagName();
			String content = child.getTextContent();
			switch (tag) {
				case AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_SOURCE:
					result.parse(content);
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_PREFIX:
					result.setPrefix(content);
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_TYPE:
					result.setType(content);
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_JSON_PROPERTY:
					if (!child.hasAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_JSON_ROOT)) {
						throw new ExpanderIndexerException("missing root ID for JSON property");
					}
					result.setJSONPropertyName(content, child.getAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_OBO_JSON_ROOT));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_PROPERTY:
					addProperty(result, child, content);
					break;
			}
		}
		return result;
	}

	private static SortedHorizontalExpanderIndexer getSortedHorizontal(Element elt, NormalizationOptions normalizationOptions) throws IOException, ExpanderIndexerException {
		SortedHorizontalExpanderIndexer result = new SortedHorizontalExpanderIndexer();
		result.setNormalizationOptions(normalizationOptions);
		if (elt.hasAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_SOURCE)) {
			result.setSource(getSource(elt, elt.getAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_SOURCE)));
		}
		result.setCanonicalTermColumn(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_CANONICAL, 1));
		result.setLabelColumn(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_LABEL, 2));
		result.setPrefix(XMLUtils.getAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_PREFIX, ""));
		result.setSuffix(XMLUtils.getAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_SUFFIX, ""));
		result.setType(XMLUtils.getAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_TYPE, null));
		result.setTypeColumn(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_TYPE_COLUMN, 3));
		result.setFirstSynonymColumn(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_FIRST_SYNONYM, 3));
		for (Element child : XMLUtils.childrenElements(elt)) {
			String tag = child.getTagName();
			String content = child.getTextContent();
			switch (tag) {
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_SOURCE:
					result.setSource(getSource(child));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_FIRST_SYNONYM:
					result.setFirstSynonymColumn(Integer.parseInt(content));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_CANONICAL:
					result.setCanonicalTermColumn(Integer.parseInt(content));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_LABEL:
					result.setLabelColumn(Integer.parseInt(content));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_PREFIX:
					result.setPrefix(content);
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_SUFFIX:
					result.setSuffix(content);
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_TYPE:
					result.setType(content);
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_TYPE_COLUMN:
					result.setTypeColumn(Integer.parseInt(content));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_PROPERTY:
					addProperty(result, child, content);
					break;
			}
		}
		return result;
	}
	
	private static SortedVerticalExpanderIndexer getSortedVertical(Element elt, NormalizationOptions normalizationOptions) throws IOException, ExpanderIndexerException {
		SortedVerticalExpanderIndexer result = new SortedVerticalExpanderIndexer();
		result.setNormalizationOptions(normalizationOptions);
		if (elt.hasAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_SOURCE)) {
			result.setSource(getSource(elt, elt.getAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_SOURCE)));
		}
		result.setSynonymColumn(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_SYNONYM, 0));
		result.setCanonicalTermColumn(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_CANONICAL, 1));
		result.setLabelColumn(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_LABEL, 2));
		result.setPrefix(XMLUtils.getAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_PREFIX, ""));
		result.setSuffix(XMLUtils.getAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_SUFFIX, ""));
		result.setType(XMLUtils.getAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_TYPE, null));
		result.setTypeColumn(XMLUtils.getIntegerAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_TYPE_COLUMN, 3));
		for (Element child : XMLUtils.childrenElements(elt)) {
			String tag = child.getTagName();
			String content = child.getTextContent();
			switch (tag) {
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_SOURCE:
					result.setSource(getSource(child));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_SYNONYM:
					result.setSynonymColumn(Integer.parseInt(content));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_CANONICAL:
					result.setCanonicalTermColumn(Integer.parseInt(content));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_LABEL:
					result.setLabelColumn(Integer.parseInt(content));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_PREFIX:
					result.setPrefix(content);
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_SUFFIX:
					result.setSuffix(content);
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_TYPE:
					result.setType(content);
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_SORTED_VERTICAL_TYPE_COLUMN:
					result.setTypeColumn(Integer.parseInt(content));
					break;
				case AlvisIRConstants.XML_EXPANDER_INDEXER_PROPERTY:
					addProperty(result, child, content);
					break;
			}
		}
		return result;
	}
	
	private static void addProperty(ExpanderIndexer expanderIndexer, Element elt, String value) throws ExpanderIndexerException {
		if (!elt.hasAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_PROPERTY_NAME)) {
			throw new ExpanderIndexerException("missing property name");
		}
		String name = elt.getAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_PROPERTY_NAME);
		expanderIndexer.addProperty(name, value);
	}

	private static SourceStream getSource(Element elt) throws IOException, ExpanderIndexerException {
		return getSource(elt, elt.getTextContent());
	}

	private static SourceStream getSource(Element elt, String textContent) throws IOException, ExpanderIndexerException {
		String charset = XMLUtils.getAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SOURCE_CHARSET, "UTF-8");
		boolean recursive = XMLUtils.getBooleanAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SOURCE_RECURSIVE, false);
		Pattern pattern = null;
		if (elt.hasAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_SOURCE_FILTER)) {
			pattern = Pattern.compile(elt.getAttribute(AlvisIRConstants.XML_EXPANDER_INDEXER_SOURCE_FILTER));
		}
		boolean fullNameFilter = XMLUtils.getBooleanAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SOURCE_FULL_NAME_FILTER, false);
		boolean wholeMatch = XMLUtils.getBooleanAttribute(elt, AlvisIRConstants.XML_EXPANDER_INDEXER_SOURCE_FILTER_WHOLE_MATCH, false);
		FileFilter filter = new PatternFileFilter(pattern, fullNameFilter, wholeMatch);
		CompressionFilter compressionFilter = CompressionFilter.NONE;
		try {
			StreamFactory sf = new StreamFactory();
			sf.setCharset(charset);
			sf.setRecursive(recursive);
			sf.setFilter(filter);
			sf.setCompressionFilter(compressionFilter);
			return sf.getSourceStream(textContent);
		}
		catch (URISyntaxException e) {
			throw new ExpanderIndexerException(e);
		}
	}

	private static CompoundExpanderIndexer getCompound(Element elt, NormalizationOptions normalizationOptions) throws IOException, ExpanderIndexerException {
		CompoundExpanderIndexer result = new CompoundExpanderIndexer();
		result.setNormalizationOptions(normalizationOptions);
		for (Element child : XMLUtils.childrenElements(elt)) {
			result.addExpanderIndexer(getExpanderIndexer(child, normalizationOptions));
		}
		return result;
	}
	
	private static IndexWriter openIndexWriter(Directory dir) throws IOException {
		IndexWriterConfig indexConfig = new IndexWriterConfig(Version.LUCENE_36, null);
		indexConfig.setOpenMode(OpenMode.CREATE);
		return new IndexWriter(dir, indexConfig);	
	}

	private static Logger getLogger() {
		Logger result = Logger.getAnonymousLogger();
		result.setUseParentHandlers(false);
		Handler handler = new FlushedStreamHandler(System.err, new StandardFormatter());
		result.addHandler(handler);
		return result;
	}

	/**
	 * Creates an expander index, arguments: INDEX_DIR XML_FILE
	 * @param args
	 * @throws IOException
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws URISyntaxException
	 * @throws OBOParseException
	 * @throws ExpanderIndexerException 
	 */
	public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException, ExpanderIndexerException {
		Logger logger = getLogger();
		String indexPath = args[0];
		File indexFile = new File(indexPath);
		Directory indexDir = FSDirectory.open(indexFile);
		if (IndexReader.indexExists(indexDir)) {
			logger.warning("clearing previous index: " + indexFile.getAbsolutePath());
			Files.deleteDir(indexFile);
		}
		try (IndexWriter indexWriter = openIndexWriter(indexDir)) {
			String indexSpec = args[1];
			ExpanderIndexer expanderIndexer = getExpanderIndexer(indexSpec);
			expanderIndexer.indexExpansions(logger, indexWriter);
			expanderIndexer.recordProperties(logger, indexWriter);
		}
	}
}
