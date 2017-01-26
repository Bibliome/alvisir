package fr.inra.mig_bibliome.alvisir.core.facet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bibliome.util.count.Stats;

/**
 * Search facet category.
 * @author rbossy
 *
 */
public class FacetCategory extends Stats<String,FacetTerm> {
	private final FacetSpecification specification;

	/**
	 * Creates a facet category with the specified specification.
	 * @param specification
	 */
	public FacetCategory(FacetSpecification specification) {
		super(new HashMap<String,FacetTerm>());
		this.specification = specification;
	}

	/**
	 * Returns the specification for this facet category.
	 * @return
	 */
	public FacetSpecification getSpecification() {
		return specification;
	}

	@Override
	protected FacetTerm defaultValue(String key) {
		return new FacetTerm(key);
	}

	/**
	 * Returns all terms in this facet category.
	 * @return
	 */
	public List<FacetTerm> getTerms() {
		List<FacetTerm> result = new ArrayList<FacetTerm>(values());
		specification.sortAndApplyLimits(result);
		return result;
	}
}
