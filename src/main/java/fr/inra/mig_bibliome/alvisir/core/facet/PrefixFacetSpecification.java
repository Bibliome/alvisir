package fr.inra.mig_bibliome.alvisir.core.facet;

/**
 * Facet specification that accepts terms that start with a prefix specified to the constructor.
 * @author rbossy
 *
 */
public class PrefixFacetSpecification extends FacetSpecification {
	private final String termPrefix;

	/**
	 * Creates a prefix facet specification.
	 * @param name facet category name
	 * @param fieldName name of the field of terms
	 * @param sort sort criterion
	 * @param cutoff minimal score for a facet term
	 * @param maxTerms maximal number of facet terms
	 * @param termPrefix prefix of facet terms
	 */
	public PrefixFacetSpecification(String name, String fieldName, FacetLabelFactory labelFactory, String queryFieldName, FacetSubQueryType subQueryType, boolean labelQuery, FacetSort sort, int cutoff, int maxFacets, String termPrefix) {
		super(name, fieldName, labelFactory, queryFieldName, subQueryType, labelQuery, sort, cutoff, maxFacets);
		this.termPrefix = termPrefix;
	}

	@Override
	public boolean accept(String x) {
		return x.startsWith(termPrefix);
	}
}
