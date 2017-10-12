package fr.inra.maiage.bibliome.alvisir.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import fr.inra.maiage.bibliome.alvisir.core.expand.ExpanderOptions;
import fr.inra.maiage.bibliome.alvisir.core.expand.QueryNodeExpanderFactory;
import fr.inra.maiage.bibliome.alvisir.core.expand.TextExpander;
import fr.inra.maiage.bibliome.alvisir.core.facet.FacetSpecification;
import fr.inra.maiage.bibliome.alvisir.core.index.NormalizationOptions;
import fr.inra.maiage.bibliome.alvisir.core.snippet.FragmentBuilder;
import fr.inra.maiage.bibliome.alvisir.core.snippet.SentenceAnnotationFragmentBuilder;
import fr.inra.maiage.bibliome.alvisir.core.snippet.StandardFragmentBuilder;

/**
 * Search and retrieve object.
 * @author rbossy
 *
 */
public class SearchConfig implements ExpanderOptions, FieldOptions {
	private AlvisIRIndex index;
	private TextExpander textExpander;
	private QueryNodeExpanderFactory queryNodeExpanderFactory = QueryNodeExpanderFactory.BASIC;
	private int snippetCount;
	private int startSnippet;
	private int facetCount;
	private final Set<String> mandatoryFields = new HashSet<String>();
	private final Map<String,FragmentBuilder> fragmentBuilders = new HashMap<String,FragmentBuilder>();
	private final Collection<FacetSpecification> globalFacetSpecifications = new ArrayList<FacetSpecification>();
	private final Collection<FacetSpecification> docFacetSpecifications = new ArrayList<FacetSpecification>();
	private final Map<String,String[]> fieldAliases = new HashMap<String,String[]>();
	private NormalizationOptions defaultNormalizationOptions = NormalizationOptions.DEFAULT;
	private final Map<String,NormalizationOptions> fieldNormalizationOptions = new HashMap<String,NormalizationOptions>();
	private String defaultFieldName;
	private int highlightClusterRadius = 100;
	private final Collection<Example> examples = new ArrayList<Example>();
	
	public SearchConfig() {
	}
	
	void checkFieldNames() throws SearchConfigException {
		Collection<String> fieldNames = index.getGlobalAttributes().getFieldNames();
		Collection<String> fieldAndAliases = getFieldNamesAndAliases();
		checkFieldNames(fieldNames, mandatoryFields);
		checkFieldNames(fieldNames, fragmentBuilders.keySet());
		for (Map.Entry<String,String[]> e : fieldAliases.entrySet()) {
			String alias = e.getKey();
			if (fieldNames.contains(alias)) {
				throw new SearchConfigException("alias " + alias + " redefines field");
			}
			checkFieldNames(fieldNames, Arrays.asList(e.getValue()));
		}
		//
		checkFacetFieldNames(fieldAndAliases, globalFacetSpecifications);
		checkFacetFieldNames(fieldAndAliases, docFacetSpecifications);
		checkFieldNames(fieldNames, fieldNormalizationOptions.keySet());
		checkFieldName(fieldAndAliases, defaultFieldName);
	}
	
	Collection<String> getFieldNamesAndAliases() {
		Collection<String> result = new HashSet<String>(index.getGlobalAttributes().getFieldNames());
		result.addAll(fieldAliases.keySet());
		return result;
	}

	private static void checkFieldName(Collection<String> allowed, String fieldName) throws SearchConfigException {
		if (!allowed.contains(fieldName)) {
			throw new SearchConfigException("field " + fieldName + " is not in this index");
		}
	}
	
	private static void checkFieldNames(Collection<String> allowed, Collection<String> fieldNames) throws SearchConfigException {
		for (String field : fieldNames) {
			checkFieldName(allowed, field);
		}
	}
	
	private static void checkFacetFieldNames(Collection<String> allowed, Collection<FacetSpecification> facetSpecifications) throws SearchConfigException {
		for (FacetSpecification spec : facetSpecifications) {
			checkFieldName(allowed, spec.getFieldName());
		}
	}
	
	/**
	 * Adds a document facet with the specified specification.
	 * @param spec facet specification.
	 */
	public void addDocumentFacetSpecification(FacetSpecification spec) {
		docFacetSpecifications.add(spec);
	}

	/**
	 * Adds a global facet with the specified specification.
	 * @param spec facet specification.
	 */
	public void addGlobalFacetSpecification(FacetSpecification spec) {
		globalFacetSpecifications.add(spec);
	}

	/**
	 * Adds a field alias.
	 * @param alias alias name.
	 * @param fieldNames indexed fields.
	 */
	public void addFieldAlias(String alias, String... fieldNames) {
		fieldAliases.put(alias, fieldNames);
	}

	/**
	 * Adds a mandatory field snippet.
	 * @param fieldName name of field.
	 */
	public void addMandatoryField(String fieldName) {
		mandatoryFields.add(fieldName);
	}
	
	public NormalizationOptions getDefaultNormalizationOptions() {
		return defaultNormalizationOptions;
	}

	/**
	 * Returns the specifications of document facets.
	 * @return the specifications of document facets.
	 */
	public Collection<FacetSpecification> getDocFacetSpecifications() {
		return Collections.unmodifiableCollection(docFacetSpecifications);
	}

	/**
	 * Returns the number of documents to scan for collecting facet terms.
	 * @return the number of documents to scan for collecting facet terms.
	 */
	public int getFacetCount() {
		return facetCount;
	}

	@Override
	public String[] getFields(String fieldName) {
		if (fieldAliases.containsKey(fieldName)) {
			return fieldAliases.get(fieldName);
		}
		return new String[] { fieldName };
	}
	
	/**
	 * Returns a map of field names to fragment builders.
	 * @return
	 */
	public Map<String,FragmentBuilder> getFragmentBuilders() {
		return Collections.unmodifiableMap(fragmentBuilders);
	}

	/**
	 * Returns the specifications of global facets.
	 * @return the specifications of global facets.
	 */
	public Collection<FacetSpecification> getGlobalFacetSpecifications() {
		return Collections.unmodifiableCollection(globalFacetSpecifications);
	}

	public AlvisIRIndex getIndex() {
		return index;
	}

	/**
	 * Returns the names of mandatory field snippets.
	 * @return the names of mandatory field snippets.
	 */
	public Set<String> getMandatoryFields() {
		return Collections.unmodifiableSet(mandatoryFields);
	}
	
	@Override
	public NormalizationOptions getNormalizationOptions(String fieldName) {
		if (fieldNormalizationOptions.containsKey(fieldName)) {
			return fieldNormalizationOptions.get(fieldName);
		}
		for (Map.Entry<String,String[]> e : fieldAliases.entrySet()) {
			String alias = e.getKey();
			if (fieldNormalizationOptions.containsKey(alias)) {
				for (String f : e.getValue()) {
					if (f.equals(fieldName)) {
						return fieldNormalizationOptions.get(alias);
					}
				}
			}
		}
		return defaultNormalizationOptions;
	}
	
	/**
	 * Returns the number of document snippets to build.
	 * @return the number of document snippets to build.
	 */
	public int getSnippetCount() {
		return snippetCount;
	}

	/**
	 * Returns the ordinal of the first snippet to build.
	 * @return the ordinal of the first snippet to build.
	 */
	public int getStartSnippet() {
		return startSnippet;
	}
	
	public TextExpander getTextExpander() {
		return textExpander;
	}

	public QueryNodeExpanderFactory getQueryNodeExpanderFactory() {
		return queryNodeExpanderFactory;
	}

	

	public String getDefaultFieldName() {
		return defaultFieldName;
	}

	public void setDefaultFieldName(String defaultFieldName) {
		this.defaultFieldName = defaultFieldName;
	}
	
	public void setFacetCount(int facetCount) {
		this.facetCount = facetCount;
	}

	/**
	 * Sets the specified field name as silent.
	 * @param fieldName name of the field.
	 */
	public void setSilentField(String fieldName) {
		fragmentBuilders.put(fieldName, StandardFragmentBuilder.SILENT);
	}

	public void setSnippetCount(int snippetCount) {
		this.snippetCount = snippetCount;
	}

	public void setStartSnippet(int startSnippet) {
		this.startSnippet = startSnippet;
	}
		
	/**
	 * Sets the specified field as whole.
	 * @param fieldName name of the field.
	 */
	public void setWholeSnippetField(String fieldName) {
		fragmentBuilders.put(fieldName, StandardFragmentBuilder.WHOLE);
	}

	public void setTextExpander(TextExpander textExpander) {
		this.textExpander = textExpander;
	}

	public void setQueryNodeExpanderFactory(QueryNodeExpanderFactory queryNodeExpanderFactory) {
		this.queryNodeExpanderFactory = queryNodeExpanderFactory;
	}
	
	
	
	public void setDefaultNormalizationOptions(NormalizationOptions defaultNormalizationOptions) {
		this.defaultNormalizationOptions = defaultNormalizationOptions;
	}

	public void setNormalizationOptions(String fieldName, NormalizationOptions normalizationOptions) {
		if (normalizationOptions == null) {
			fieldNormalizationOptions.remove(fieldName);
		}
		else {
			fieldNormalizationOptions.put(fieldName, normalizationOptions);
		}
	}

	/**
	 * Sets the specified field as sentence fragments.
	 * @param fieldName name of the field.
	 * @param sentenceAnnotation text of sentence annotations.
	 */
	public void setSentenceSnippetField(String fieldName, String sentenceAnnotation) {
		FragmentBuilder fragmentBuilder = new SentenceAnnotationFragmentBuilder(index.getIndexReader(), sentenceAnnotation);
		fragmentBuilders.put(fieldName, fragmentBuilder);
	}

	public void setIndex(AlvisIRIndex index) {
		this.index = index;
	}

	public Map<String,String[]> getFieldAliases() {
		return Collections.unmodifiableMap(fieldAliases);
	}

	public int getHighlightClusterRadius() {
		return highlightClusterRadius;
	}

	public void setHighlightClusterRadius(int highlightClusterRadius) {
		this.highlightClusterRadius = highlightClusterRadius;
	}

	public Collection<Example> getExamples() {
		return Collections.unmodifiableCollection(examples);
	}
	
	public void addExample(Example ex) {
		examples.add(ex);
	}
	
	public void addExample(ExampleCategory category, String queryString) {
		addExample(new Example(category, queryString));
	}
}
