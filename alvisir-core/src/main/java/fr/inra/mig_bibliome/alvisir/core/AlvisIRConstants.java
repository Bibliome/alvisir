package fr.inra.mig_bibliome.alvisir.core;

/**
 * String constants.
 * EXPANDER_FIELD_*: names of fields in expander indexes.
 * EXPLANATION_KEY_*: names of elements in explanation bits.
 * XML_RESULT_*: names of elements in XML serialized search results.
 * XML_SEARCH_*: names of elements in XML serialized search options.
 * @author rbossy
 *
 */
public class AlvisIRConstants {
	/**
	 * XML namespace in AlvsIR protocols.
	 */
	public static final String EXPANDER_FIELD_SYNONYMS = "synonyms";
	public static final String EXPANDER_FIELD_TYPE = "type";
	public static final String EXPANDER_FIELD_CANONICAL = "canonical";
	public static final String EXPANDER_FIELD_LABEL = "label";
	
	public static final String EXPLANATION_KEY_SYNONYM = "synonym";
	public static final String EXPLANATION_KEY_EXPLANATIONS = "explanations";
	public static final String EXPLANATION_KEY_EXPLANATION = "explanation";
	public static final String EXPLANATION_KEY_CLASS = "class";
	public static final String EXPLANATION_KEY_PRODUCTIVITY = "productivity";
	public static final String EXPLANATION_KEY_FIELD = "field";
	public static final String EXPLANATION_KEY_SLOP = "slop";
	public static final String EXPLANATION_KEY_TEXTS = "texts";
	public static final String EXPLANATION_KEY_TERM = "term";
	public static final String EXPLANATION_KEY_PREFIX = "prefix";
	public static final String EXPLANATION_KEY_TEXT = "text";
	public static final String EXPLANATION_KEY_NORMALIZED = "normalized";
	public static final String EXPLANATION_KEY_LABEL = "label";
	public static final String EXPLANATION_KEY_TYPE = "type";
	public static final String EXPLANATION_KEY_PATHS = "path";
	public static final String EXPLANATION_KEY_PATH_LABEL = "label";
	public static final String EXPLANATION_KEY_SUBPATH = "sub-path";
	public static final String EXPLANATION_KEY_MORE_SYNONYMS = "more-synonyms";
	public static final String EXPLANATION_KEY_MORE_SUBPATHS = "more-sub-paths";
	public static final String EXPLANATION_KEY_RELATION = "relation";
	
	public static final String XML_RESULT_RESULT = "search-result";
	public static final String XML_RESULT_HITS = "hits";
	public static final String XML_RESULT_MESSAGES = "messages";
	public static final String XML_RESULT_MESSAGE = "message";
	public static final String XML_RESULT_MESSAGE_LEVEL = "level";
	public static final String XML_RESULT_MESSAGE_TEXT = "text";
	public static final String XML_RESULT_MESSAGE_THROWN = "exception";
	public static final String XML_RESULT_EXPLANATIONS = "query-expansion";
	public static final String XML_RESULT_EXPLANATION_ID = "id";
	public static final String XML_RESULT_SNIPPETS = "snippets";
	public static final String XML_RESULT_DOCUMENT_SNIPPET = "doc";
	public static final String XML_RESULT_DOCUMENT_SCORE = "score";
	public static final String XML_RESULT_FIELD_SNIPPET = "field";
	public static final String XML_RESULT_FIELD_NAME = "name";
	public static final String XML_RESULT_FIELD_ORD = "ord";
	public static final String XML_RESULT_HIGHLIGHT = "highlight";
	public static final String XML_RESULT_HIGHLIGHT_ANNOTATION = "annotation";
	public static final String XML_RESULT_HIGHLIGHT_ORD = "ord";
	public static final String XML_RESULT_HIGHLIGHT_EXPLANATION = "explanation";
	public static final String XML_RESULT_HIGHLIGHT_ARG = "arg";
	public static final String XML_RESULT_HIGHLIGHT_ARG_ROLE = "role";
	public static final String XML_RESULT_HIGHLIGHT_ARG_REF = "ref";
	public static final String XML_RESULT_HIGHLIGHT_PROP = "prop";
	public static final String XML_RESULT_HIGHLIGHT_PROP_KEY = "key";
	public static final String XML_RESULT_HIGHLIGHT_PROP_VALUE = "value";
	public static final String XML_RESULT_FRAMENT = "fragment";
	public static final String XML_RESULT_FACETS = "facets";
	public static final String XML_RESULT_FACET = "facet";
	public static final String XML_RESULT_FACET_NAME = "name";
	public static final String XML_RESULT_FACET_FIELD = "field";
	public static final String XML_RESULT_FACET_QUERY_FIELD = "query-field";
	public static final String XML_RESULT_FACET_QUERY_TYPE = "query-type";
	public static final String XML_RESULT_FACET_LABEL_QUERY = "label-query";
	public static final String XML_RESULT_FACET_TERM = "term";
	public static final String XML_RESULT_FACET_CANONICAL = "canonical";
	public static final String XML_RESULT_FACET_LABEL = "label";
	public static final String XML_RESULT_FACET_COUNT = "count";
	public static final String XML_RESULT_FACET_DOCS = "docs";
	public static final String XML_RESULT_FACET_SUB_QUERY = "sub-query";
	public static final String XML_RESULT_TIMER = "timer";
	public static final String XML_RESULT_TIMER_NAME = "name";
	public static final String XML_RESULT_TIMER_PATH = "path";
	public static final String XML_RESULT_TIMER_TIME = "time";
	public static final String XML_RESULT_TIMER_REST = "rest";
	public static final String XML_RESULT_HELP = "help";
	public static final String XML_RESULT_EXAMPLES = "examples";
	public static final String XML_RESULT_HELP_RELATIONS = "relations";
	public static final String XML_RESULT_HELP_ARG = "arg";
	public static final String XML_RESULT_HELP_FIELD_NAMES = "field-names";
	public static final String XML_RESULT_FIELD = "field";
	public static final String XML_RESULT_HELP_FIELD_ALIAS = "alias";
	public static final String XML_RESULT_HELP_FIELD_ALIAS_NAME = "name";
	public static final String XML_RESULT_HELP_DEFAULT_FIELD = "default-field";

	public static final String XML_SEARCH_INDEX_DIR = "index";
	public static final String XML_SEARCH_EXPANDER_INDEX_DIR = "expander-index";
	public static final String XML_SEARCH_DEFAULT_FIELD = "default-field";
	public static final String XML_SEARCH_QUERY_EXPANSION = "query-expansion";
	public static final String XML_SEARCH_QUERY_EXPANSION_BASIC = "basic";
	public static final String XML_SEARCH_QUERY_EXPANSION_ADVANCED = "advanced";
	public static final String XML_SEARCH_EXPANSION_TYPE = "type";
	public static final String XML_SEARCH_NORMALIZATION = "normalization";
	public static final String XML_SEARCH_FIELD = "field";
	public static final String XML_SEARCH_LOWER_CASE = "lower-case";
	public static final String XML_SEARCH_ASCII_FOLDING = "ascii-folding";
	public static final String XML_SEARCH_FIELD_ALIAS = "field-alias";
	public static final String XML_SEARCH_FIELD_FRAGMENTS = "field-fragments";
	public static final String XML_SEARCH_MANDATORY_FIELD = "mandatory-field";
	public static final String XML_SEARCH_GLOBAL_FACET = "global-facet";
	public static final String XML_SEARCH_DOCUMENT_FACET = "document-facet";
	public static final String XML_SEARCH_COUNTS = "search-counts";
	public static final String XML_SEARCH_COUNTS_FACETS = "facets";
	public static final String XML_SEARCH_COUNTS_SNIPPETS = "snippets";
	public static final String XML_SEARCH_COUNTS_START = "start";
	public static final String XML_SEARCH_FACET_NAME = "name";
	public static final String XML_SEARCH_FACET_PREFIX = "term-prefix";
	public static final String XML_SEARCH_FACET_FIELD = "field";
	public static final String XML_SEARCH_LABEL_REGEXP = "regexp";
	public static final String XML_SEARCH_LABEL_GROUP = "group";
	public static final String XML_SEARCH_LABEL_CAPITALIZE = "capitalize";
	public static final String XML_SEARCH_LABEL_UPPER_CASE = "upper-case";
	public static final String XML_SEARCH_LABEL_LOWER_CASE = "lower-case";
	public static final String XML_SEARCH_FACET_QUERY_FIELD = "query-field";
	public static final String XML_SEARCH_FACET_SUB_QUERY_TYPE = "query-type";
	public static final String XML_SEARCH_FACET_LABEL_QUERY = "label-query";
	public static final String XML_SEARCH_FACET_SORT = "sort";
	public static final String XML_SEARCH_FACET_SORT_TERM_FREQUENCY = "term";
	public static final String XML_SEARCH_FACET_SORT_DOCUMENT_FREQUENCY = "document";
	public static final String XML_SEARCH_FACET_CUTOFF = "cutoff";
	public static final String XML_SEARCH_MAX_FACETS = "max-facets";
	public static final String XML_SEARCH_FRAGMENTS_FIELD = "field";
	public static final String XML_SEARCH_FRAGMENTS_BUILDER = "fragments";
	public static final String XML_SEARCH_FRAGMENTS_BUILDER_SILENT = "silent";
	public static final String XML_SEARCH_FRAGMENTS_BUILDER_WHOLE = "whole";
	public static final String XML_SEARCH_FRAGMENTS_BUILDER_SENTENCE = "sentence";
	public static final String XML_SEARCH_FRAGMENTS_BUILDER_SENTENCE_ANNOTATION = "annotation";
	public static final String XML_SEARCH_FIELD_ALIAS_NAME = "alias";
	public static final String XML_SEARCH_EXPANSION_FACTORY = "expansion";
	public static final String XML_SEARCH_HIGHLIHT_CLUSTER_RADIUS = "highlight-cluster-radius";
	public static final String XML_SEARCH_EXAMPLES = "examples";

	/**
	 * Default token position gap.
	 */
	public static final int DEFAULT_TOKEN_POSITION_GAP = 256;

	public static final String XML_EXPANDER_NORMALIZATION = "normalization";
	public static final String XML_EXPANDER_INDEXER_PROPERTY = "property";
	public static final String XML_EXPANDER_INDEXER_PROPERTY_NAME = "name";
	public static final String XML_EXPANDER_INDEXER_COMPOUND = "compound";
	public static final String XML_EXPANDER_INDEXER_OBO = "obo";
	public static final String XML_EXPANDER_INDEXER_OBO_SOURCE = "source";
	public static final String XML_EXPANDER_INDEXER_OBO_PREFIX = "prefix";
	public static final String XML_EXPANDER_INDEXER_OBO_TYPE = "type";
	public static final String XML_EXPANDER_INDEXER_OBO_JSON_PROPERTY = "json-property";
	public static final String XML_EXPANDER_INDEXER_OBO_JSON_ROOT = "root-id";
	public static final String XML_EXPANDER_INDEXER_SORTED_VERTICAL = "sorted-vertical";
	public static final String XML_EXPANDER_INDEXER_SORTED_VERTICAL_SOURCE = "source";
	public static final String XML_EXPANDER_INDEXER_SORTED_VERTICAL_SYNONYM = "synonym";
	public static final String XML_EXPANDER_INDEXER_SORTED_VERTICAL_CANONICAL = "canonical";
	public static final String XML_EXPANDER_INDEXER_SORTED_VERTICAL_LABEL = "label";
	public static final String XML_EXPANDER_INDEXER_SORTED_VERTICAL_PREFIX = "prefix";
	public static final String XML_EXPANDER_INDEXER_SORTED_VERTICAL_SUFFIX = "suffix";
	public static final String XML_EXPANDER_INDEXER_SORTED_VERTICAL_TYPE = "type";
	public static final String XML_EXPANDER_INDEXER_SORTED_VERTICAL_TYPE_COLUMN = "type-column";
	public static final String XML_EXPANDER_INDEXER_SORTED_HORIZONTAL = "sorted-horizontal";
	public static final String XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_SOURCE = "source";
	public static final String XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_FIRST_SYNONYM = "first-synonym";
	public static final String XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_CANONICAL = "canonical";
	public static final String XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_LABEL = "label";
	public static final String XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_PREFIX = "prefix";
	public static final String XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_SUFFIX = "suffix";
	public static final String XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_TYPE = "type";
	public static final String XML_EXPANDER_INDEXER_SORTED_HORIZONTAL_TYPE_COLUMN = "type-column";
	public static final String XML_EXPANDER_INDEXER_SOURCE_CHARSET = "charset";
	public static final String XML_EXPANDER_INDEXER_SOURCE_RECURSIVE = "recursive";
	public static final String XML_EXPANDER_INDEXER_SOURCE_FILTER = "filter";
	public static final String XML_EXPANDER_INDEXER_SOURCE_FULL_NAME_FILTER = "full-name-filter";
	public static final String XML_EXPANDER_INDEXER_SOURCE_FILTER_WHOLE_MATCH = "filter-whole-match";

	public static final String XML_QUERY_FIELD = "field";
	public static final String XML_QUERY_PHRASE = "phrase";
	public static final String XML_QUERY_PREFIX = "prefix";
	public static final String XML_QUERY_TERM = "term";
	public static final String XML_QUERY_OR = "or";
	public static final String XML_QUERY_AND = "and";
	public static final String XML_QUERY_OPERATOR = "operator";
	public static final String XML_QUERY_OPERATOR_AND = "and";
	public static final String XML_QUERY_OPERATOR_BUT = "but";
	public static final String XML_QUERY_SLOP = "slop";
	public static final String XML_QUERY_NEAR = "near";
	public static final String XML_QUERY_RELATION = "relation";
	public static final String XML_QUERY_NO_EXPANSION = "no-expansion";
	public static final String XML_QUERY_ANY = "any";
}
