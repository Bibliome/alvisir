package fr.inra.mig_bibliome.alvisir.core;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.bibliome.util.Timer;
import org.bibliome.util.fragments.Fragment;
import org.xml.sax.SAXException;

import fr.inra.mig_bibliome.alvisir.core.expand.AlvisIRQueryNodeExpander;
import fr.inra.mig_bibliome.alvisir.core.expand.AndQueryNodeExpander;
import fr.inra.mig_bibliome.alvisir.core.expand.ExpanderException;
import fr.inra.mig_bibliome.alvisir.core.expand.ExpansionResult;
import fr.inra.mig_bibliome.alvisir.core.expand.TextExpander;
import fr.inra.mig_bibliome.alvisir.core.expand.explanation.MatchExplanation;
import fr.inra.mig_bibliome.alvisir.core.facet.FacetCategory;
import fr.inra.mig_bibliome.alvisir.core.facet.FacetCollector;
import fr.inra.mig_bibliome.alvisir.core.facet.FacetSpecification;
import fr.inra.mig_bibliome.alvisir.core.index.IndexGlobalAttributes;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRAndQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRAnyQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRNearQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRNoExpansionQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIROrQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRPhraseQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRPrefixQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryFields;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNodeQueryConverter;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNodeVisitor;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRRelationQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRTermQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.parser.ParseException;
import fr.inra.mig_bibliome.alvisir.core.query.parser.QueryParser;
import fr.inra.mig_bibliome.alvisir.core.snippet.DocumentSnippet;
import fr.inra.mig_bibliome.alvisir.core.snippet.FieldSnippet;
import fr.inra.mig_bibliome.alvisir.core.snippet.FieldValueBuilder;
import fr.inra.mig_bibliome.alvisir.core.snippet.FragmentBuilder;
import fr.inra.mig_bibliome.alvisir.core.snippet.Highlight;
import fr.inra.mig_bibliome.alvisir.core.snippet.HighlightSelector;
import fr.inra.mig_bibliome.alvisir.core.snippet.IndexFieldValueBuilder;

/**
 * Search and retrieve object.
 * @author rbossy
 *
 */
public class SearchResult {
	private final SearchConfig searchConfig;
	private final Timer<AlvisIRTimerCategory> timer = new Timer<AlvisIRTimerCategory>("alvisir", AlvisIRTimerCategory.INSTANCE);
	private final List<LogRecord> messages = new ArrayList<LogRecord>();
	private final boolean recordDebug;
	private final Logger logger = Logger.getAnonymousLogger();
	private ExpansionResult expansionResult;
	private int totalHits;
	private DocumentSnippet[] docSnippets;
	private Collection<FacetCategory> globalFacets;
	private String queryString;
	private AlvisIRQueryNode originalQueryNode;
	private AlvisIRQueryNode queryNode;

	public SearchResult(SearchConfig searchConfig, boolean recordDebug) {
		super();
		this.searchConfig = checkSearchConfigFieldNames(searchConfig);
		this.recordDebug = recordDebug;
    	logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(logHandler);
		timer.start();
	}
	
	public SearchResult(String searchConfigPath, boolean recordDebug) {
		this.recordDebug = recordDebug;
    	logger.setLevel(Level.ALL);
        logger.setUseParentHandlers(false);
        logger.addHandler(logHandler);
        SearchConfig sc = null;
        try {
			sc = SearchConfigXMLSerializer.getSearch(searchConfigPath);
		}
        catch (IOException|SAXException|ParserConfigurationException|ExpanderException|SearchConfigException e) {
        	logger.log(Level.SEVERE, "server configuration error", e);
		}
    	this.searchConfig = checkSearchConfigFieldNames(sc);
        timer.start();
	}
	
	public SearchResult(SearchConfig searchConfig) {
		this(searchConfig, false);
	}

	public SearchResult(String searchConfigPath) {
		this(searchConfigPath, false);
	}
	
	private SearchConfig checkSearchConfigFieldNames(SearchConfig searchConfig) {
		if (searchConfig == null) {
			return null;
		}
		try {
			searchConfig.checkFieldNames();
			return searchConfig;
		}
		catch (SearchConfigException e) {
			logger.log(Level.SEVERE, "server-side configuration error", e);
			return null;
		}
	}
	
	private final Handler logHandler = new StreamHandler(System.err, new SimpleFormatter()) {
		@Override
		public void publish(LogRecord record) {
			Level level = record.getLevel();
			if (!recordDebug && level.intValue() < Level.INFO.intValue()) {
				super.publish(record);
			}
			else {
				messages.add(record);
			}
			Throwable thr = record.getThrown();
			if (thr != null) {
				thr.printStackTrace();
			}
		}

		@Override
		public void flush() {
			super.flush();
		}

		@Override
		public void close() throws SecurityException {
		}
	};

	public Timer<AlvisIRTimerCategory> getTimer() {
		return timer;
	}

	/**
	 * Returns the document snippets, null if the search has not been performed.
	 * @return the document snippets, null if the search has not been performed.
	 */
	public DocumentSnippet[] getDocSnippets() {
		return docSnippets;
	}

	/**
	 * Returns the global facets, null if the search has not been performed.
	 * @return the global facets, null if the search has not been performed.
	 */
	public Collection<FacetCategory> getGlobalFacets() {
		return globalFacets;
	}

	/**
	 * Returns the number of hits, undefined if the search has not been performed.
	 * @return the number of hits, undefined if the search has not been performed.
	 */
	public int getTotalHits() {
		return totalHits;
	}

	public ExpansionResult getExpansionResult() {
		return expansionResult;
	}

	@SuppressWarnings("unused")
	private void debugSnippets(String step) {
		System.err.println(step);
		for (DocumentSnippet docSnippet : docSnippets) {
			System.err.println("  doc = " + docSnippet.getDocId());
			for (FieldSnippet fieldSnippet : docSnippet.getFieldSnippets()) {
				System.err.println("    field = " + fieldSnippet.getFieldName() + "/" + fieldSnippet.getFieldInstance());
				for (Fragment frag : fieldSnippet.getFragments()) {
					System.err.println("      fragment = " + frag);
				}
				for (Highlight h : fieldSnippet.getHighlights()) {
					System.err.println("      " + h);
				}
			}
		}
	}
	
	public void search(AlvisIRQueryNode originalQueryNode) {
		if (searchConfig == null) {
			return;
		}
//		System.err.println("checking field and relation names");
		checkFieldAndRelationNames(originalQueryNode);
		this.originalQueryNode = originalQueryNode;
		try {
//			System.err.println("expanding query");
			expandQuery();
//			System.err.println("searching");
			TopDocs topDocs = doSearch();
//			System.err.println("building snippets");
			buildSnippets(topDocs);
//			System.err.println("building facets");
			buildFacets(topDocs);
//			System.err.println("done");
		}
		catch (IOException e) {
			logger.log(Level.SEVERE, "server-side error", e);
		}
		
		if (docSnippets != null) {
			Arrays.sort(docSnippets, SNIPPET_COMPARATOR_BY_SCORE);
		}
	}
	
	public void search(String queryString) {
		if (queryString == null) {
			return;
		}
		this.queryString = queryString.trim();
		if (this.queryString.isEmpty()) {
			return;
		}
		try {
			Reader reader = new StringReader(queryString);
			QueryParser parser = new QueryParser(reader);
//			System.err.println("parsing query");
			AlvisIRQueryNode queryNode = parser.query(searchConfig.getDefaultFieldName());
			search(queryNode);
		}
		catch (ParseException e) {
			logger.log(Level.WARNING, "query syntax error", e);
		}
	}

	private void expandQuery() throws IOException {
		Timer<AlvisIRTimerCategory> expanderTimer = timer.newChild("query-expansion", AlvisIRTimerCategory.INSTANCE);
		expanderTimer.start();
		AlvisIRIndex index = searchConfig.getIndex();
		TextExpander textExpander = searchConfig.getTextExpander();
		AndQueryNodeExpander andQueryNodeExpander = searchConfig.getAndQueryNodeExpander();
		AlvisIRQueryNodeExpander queryNodeExpander = new AlvisIRQueryNodeExpander(textExpander, andQueryNodeExpander);
		expansionResult = queryNodeExpander.expandQuery(searchConfig, searchConfig, originalQueryNode);
		for (MatchExplanation expl : expansionResult.getExplanations()) {
			expl.computeProductivity(index, searchConfig);
		}
		expanderTimer.stop();
	}
	
	private TopDocs doSearch() throws IOException {
		Timer<AlvisIRTimerCategory> searchTimer = timer.newChild("search", AlvisIRTimerCategory.INSTANCE);
		searchTimer.start();
		int facetCount = searchConfig.getFacetCount();
		int snippetCount = searchConfig.getSnippetCount();
		int startSnippet = searchConfig.getStartSnippet();
		int n = Math.max(facetCount, snippetCount + startSnippet);
		AlvisIRIndex index = searchConfig.getIndex();
		IndexSearcher indexSearcher = index.getIndexSearcher();
		this.queryNode = expansionResult.getQueryNode();
		Query query = AlvisIRQueryNodeQueryConverter.convert(index.getGlobalAttributes().getTokenPositionGap(), searchConfig, queryNode);
		//System.err.println("query = " + query);
		TopDocs topDocs = indexSearcher.search(query, n);
		totalHits = topDocs.totalHits;
		if (totalHits == 0) {
			logger.warning("no results");
		}
//		System.err.println("totalHits = " + totalHits);
//		System.err.println("topDocs.scoreDocs" + Arrays.deepToString(topDocs.scoreDocs));
		searchTimer.stop();
		return topDocs;
	}

	private void buildSnippets(TopDocs topDocs) throws IOException {
		Timer<AlvisIRTimerCategory> snippetsTimer = timer.newChild("snippets", AlvisIRTimerCategory.INSTANCE);
		snippetsTimer.start();
		
		AlvisIRIndex index = searchConfig.getIndex();
		IndexSearcher indexSearcher = index.getIndexSearcher();
		
		// create snippets
		createDocumentSnippets(searchConfig, topDocs);
		//debugSnippets("DOC SNIPS");
		FieldValueBuilder fieldValueBuilder = new IndexFieldValueBuilder(indexSearcher.getIndexReader());
		for (DocumentSnippet doc : docSnippets) {
			fieldValueBuilder.fetchMandatoryFieldValues(searchConfig.getMandatoryFields(), doc);
		}
		//debugSnippets("MANDATORY FIELDS");
		
		// create highlights
		for (MatchExplanation expl : expansionResult.getExplanations()) {
		    //System.err.println("expl = " + expl);
			expl.createHighlights(index, searchConfig, docSnippets);
		}
//		debugSnippets("HIGHLIGHTS");
		
		// select highlights
		HighlightSelector selector = new HighlightSelector(searchConfig);
		Set<String> fields = AlvisIRQueryFields.collectQueryFields(searchConfig, originalQueryNode);
		fields.addAll(searchConfig.getMandatoryFields());
		Map<String,FragmentBuilder> fragmentBuilders = searchConfig.getFragmentBuilders();
		for (DocumentSnippet doc : docSnippets) {
//			System.err.println("doc = " + doc);
			for (String fieldName : fields) {
				selector.selectHighlights(doc, fieldName);
//				debugSnippets("SELECTED " + doc + "/" + fieldName);
				FragmentBuilder fb = fragmentBuilders.get(fieldName);
//				System.err.println("fieldName = " + fieldName);
//				System.err.println("fb = " + fb);
				fb.createFragments(doc, fieldName);
//				debugSnippets("FRAGMENTS " + doc + "/" + fieldName);
			}
			for (FieldSnippet field : doc.getFieldSnippets()) {
				field.retainFragmentsWithSelectedHighlights();
				field.retainHighlightsInFragments();
			}
			//debugSnippets("PURGED HIGHLIGHTS");
			doc.retainFieldSnippetsWithHighlights();
		}
		//debugSnippets("PURGED");
		
		// get field values
		for (DocumentSnippet docSnippet : docSnippets) {
			fieldValueBuilder.fetchFieldValues(docSnippet);
		}

		snippetsTimer.stop();
	}
	
	private void buildFacets(TopDocs topDocs) throws IOException {
		Timer<AlvisIRTimerCategory> facetsTimer = timer.newChild("facets", AlvisIRTimerCategory.INSTANCE);
		facetsTimer.start();
		FacetCollector facetCollector = initFacetCollector(searchConfig);
		facetCollector.collectFacets(searchConfig.getIndex().getIndexReader(), topDocs);
		facetsTimer.stop();
	}

	private FacetCollector initFacetCollector(SearchConfig searchConfig) {
		Collection<FacetSpecification> docFacetSpecifications = searchConfig.getDocFacetSpecifications();
		for (DocumentSnippet docSnippet : docSnippets) {
			for (FacetSpecification spec : docFacetSpecifications) {
				docSnippet.addFacet(spec);
			}
		}
		Collection<FacetSpecification> globalFacetSpecifications = searchConfig.getGlobalFacetSpecifications();
		globalFacets = new ArrayList<FacetCategory>(globalFacetSpecifications.size());
		for (FacetSpecification spec : globalFacetSpecifications) {
			globalFacets.add(new FacetCategory(spec));
		}
		return new FacetCollector(searchConfig, globalFacets, docSnippets);
	}
	
	private void createDocumentSnippets(SearchConfig searchConfig, TopDocs topDocs) {
		int snippetCount = searchConfig.getSnippetCount();
		int startSnippet = searchConfig.getStartSnippet();
		int n = Math.min(snippetCount, Math.max(0, topDocs.totalHits - startSnippet));
		docSnippets = new DocumentSnippet[n];
		for (int i = 0; i < docSnippets.length; ++i) {
			docSnippets[i] = new DocumentSnippet(topDocs.scoreDocs[startSnippet + i]);
		}
		Arrays.sort(docSnippets, SNIPPET_COMPARATOR_BY_ID);
	}

	private static final Comparator<DocumentSnippet> SNIPPET_COMPARATOR_BY_ID = new Comparator<DocumentSnippet>() {
		@Override
		public int compare(DocumentSnippet o1, DocumentSnippet o2) {
			return Integer.compare(o1.getDocId(), o2.getDocId());
		}
	};

	private static final Comparator<DocumentSnippet> SNIPPET_COMPARATOR_BY_SCORE = new Comparator<DocumentSnippet>() {
		@Override
		public int compare(DocumentSnippet o1, DocumentSnippet o2) {
			return Float.compare(o2.getScore(), o1.getScore());
		}
	};

	public String getQueryString() {
		return queryString;
	}

	public SearchConfig getSearchConfig() {
		return searchConfig;
	}

	public AlvisIRQueryNode getOriginalQueryNode() {
		return originalQueryNode;
	}
	
	public AlvisIRQueryNode getQueryNode() {
		return queryNode;
	}

	public List<LogRecord> getMessages() {
		return Collections.unmodifiableList(messages);
	}

	public Logger getLogger() {
		return logger;
	}
	
	private boolean checkFieldAndRelationNames(AlvisIRQueryNode query) {
		Collection<String> allowedFields = searchConfig.getFieldNamesAndAliases();
		boolean result = query.accept(checkQueryFieldNames, allowedFields);
		AlvisIRIndex index = searchConfig.getIndex();
		IndexGlobalAttributes globalAttributes = index.getGlobalAttributes();
		Map<String,String[]> relationInfo = globalAttributes.getRelationInfo();
		Collection<String> allowedRelations = relationInfo == null ? null : relationInfo.keySet();
		return query.accept(checkRelationNames, allowedRelations) && result;
	}

	private AlvisIRQueryNodeVisitor<Boolean,Collection<String>,RuntimeException> checkRelationNames = new AlvisIRQueryNodeVisitor<Boolean,Collection<String>,RuntimeException>() {
		@Override
		public Boolean visit(AlvisIRTermQueryNode termQueryNode, Collection<String> param) {
			return true;
		}

		@Override
		public Boolean visit(AlvisIRPhraseQueryNode phraseQueryNode, Collection<String> param) {
			return true;
		}

		@Override
		public Boolean visit(AlvisIRPrefixQueryNode prefixQueryNode, Collection<String> param) {
			return true;
		}

		@Override
		public Boolean visit(AlvisIRAndQueryNode andQueryNode, Collection<String> param) {
			boolean result = true;
			for (AlvisIRAndQueryNode.Clause clause : andQueryNode.getClauses()) {
				AlvisIRQueryNode qn = clause.getQueryNode();
				result = qn.accept(this, param) && result;
			}
			return result;
		}

		@Override
		public Boolean visit(AlvisIROrQueryNode orQueryNode, Collection<String> param) {
			boolean result = true;
			for (AlvisIRQueryNode clause : orQueryNode.getClauses()) {
				result = clause.accept(this, param) && result;
			}
			return result;
		}

		@Override
		public Boolean visit(AlvisIRNearQueryNode nearQueryNode, Collection<String> param) {
			return true;
		}

		@Override
		public Boolean visit(AlvisIRRelationQueryNode relationQueryNode, Collection<String> param) {
			String rel = relationQueryNode.getRelation();
			if (!param.contains(rel)) {
				logger.warning("unknown relation " + rel);
				return false;
			}
			return true;
		}

		@Override
		public Boolean visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, Collection<String> param) {
			return noExpansionQueryNode.getQueryNode().accept(this, param);
		}

		@Override
		public Boolean visit(AlvisIRAnyQueryNode anyQueryNode, Collection<String> param) {
			return true;
		}
	};

	private AlvisIRQueryNodeVisitor<Boolean,Collection<String>,RuntimeException> checkQueryFieldNames = new AlvisIRQueryNodeVisitor<Boolean,Collection<String>,RuntimeException>() {
		private boolean checkFieldName(Collection<String> allowed, String field) {
			if (allowed == null) {
				return true;
			}
			if (!allowed.contains(field)) {
				logger.warning("unknown field " + field);
				return false;
			}
			return true;
		}

		@Override
		public Boolean visit(AlvisIRTermQueryNode termQueryNode, Collection<String> param) {
			return checkFieldName(param, termQueryNode.getField());
		}

		@Override
		public Boolean visit(AlvisIRPhraseQueryNode phraseQueryNode, Collection<String> param) {
			return checkFieldName(param, phraseQueryNode.getField());
		}

		@Override
		public Boolean visit(AlvisIRPrefixQueryNode prefixQueryNode, Collection<String> param) {
			return checkFieldName(param, prefixQueryNode.getField());
		}

		@Override
		public Boolean visit(AlvisIRAndQueryNode andQueryNode, Collection<String> param) {
			boolean result = true;
			for (AlvisIRAndQueryNode.Clause clause : andQueryNode.getClauses()) {
				AlvisIRQueryNode qn = clause.getQueryNode();
				result = qn.accept(this, param) && result;
			}
			return result;
		}

		@Override
		public Boolean visit(AlvisIROrQueryNode orQueryNode, Collection<String> param) {
			boolean result = true;
			for (AlvisIRQueryNode clause : orQueryNode.getClauses()) {
				result = clause.accept(this, param) && result;
			}
			return result;
		}

		@Override
		public Boolean visit(AlvisIRNearQueryNode nearQueryNode, Collection<String> param) {
			return checkFieldName(param, nearQueryNode.getField());
		}

		@Override
		public Boolean visit(AlvisIRRelationQueryNode relationQueryNode, Collection<String> param) {
			boolean result = relationQueryNode.getLeft().accept(this, param);
			return relationQueryNode.getRight().accept(this, param) && result;
		}

		@Override
		public Boolean visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, Collection<String> param) {
			return noExpansionQueryNode.getQueryNode().accept(this, param);
		}

		@Override
		public Boolean visit(AlvisIRAnyQueryNode anyQueryNode, Collection<String> param) {
			return true;
		}
	};
}
