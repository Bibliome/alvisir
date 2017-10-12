package fr.inra.mig_bibliome.alvisir.core.facet;

import java.util.Collections;
import java.util.List;

import fr.inra.maiage.bibliome.util.filters.Filter;

/**
 * Facet specification.
 * @author rbossy
 *
 */
public abstract class FacetSpecification implements Filter<String> {
	private final String name;
	private final String fieldName;
	private final FacetLabelFactory labelFactory;
	private final String queryFieldName;
	private final FacetSubQueryType subQueryType;
	private final boolean labelQuery;
	private final FacetSort sort;
	private final int cutoff;
	private final int maxTerms;

	/**
	 * Creates a facet specification.
	 * @param name facet category name
	 * @param fieldName name of the field of terms
	 * @param sort sort criterion
	 * @param cutoff minimal score for a facet term
	 * @param maxTerms maximal number of facet terms
	 */
	protected FacetSpecification(String name, String fieldName, FacetLabelFactory labelFactory, String queryFieldName, FacetSubQueryType subQueryType, boolean labelQuery, FacetSort sort, int cutoff, int maxTerms) {
		super();
		this.name = name;
		this.fieldName = fieldName;
		this.labelFactory = labelFactory;
		this.queryFieldName = queryFieldName;
		this.subQueryType = subQueryType;
		this.labelQuery = labelQuery;
		this.sort = sort;
		this.cutoff = cutoff;
		this.maxTerms = maxTerms;
	}

	/**
	 * Returns the facet category name.
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * Returns the field name of facet terms.
	 * @return
	 */
	public String getFieldName() {
		return fieldName;
	}

	public FacetLabelFactory getLabelFactory() {
		return labelFactory;
	}

	public String getQueryFieldName() {
		return queryFieldName;
	}

	public FacetSubQueryType getSubQueryType() {
		return subQueryType;
	}

	public boolean isLabelQuery() {
		return labelQuery;
	}

	/**
	 * Returns the sort criterion of facet terms.
	 * @return
	 */
	public FacetSort getSort() {
		return sort;
	}

	/**
	 * Returns the minimal score of facet terms.
	 * @return
	 */
	public int getCutoff() {
		return cutoff;
	}

	/**
	 * Returns the maximal number of facet terms.
	 * @return
	 */
	public int getMaxTerms() {
		return maxTerms;
	}
	
	/**
	 * Sort the specified list of facet terms and crop the specified list according to limits provided to the constructor.
	 * @param list
	 */
	public void sortAndApplyLimits(List<FacetTerm> list) {
		int len = list.size();
		if (len <= 1) {
			return;
		}
		Collections.sort(list, sort);
		cropList(list, maxTerms);
		FacetTerm last = list.get(list.size() - 1);
		long lowest = sort.getCriterion(last);
		if (cutoff > lowest) {
			int tailIndex = getTailIndex(list);
			cropList(list, tailIndex);
		}
	}
	
	public String getSubQuery(FacetTerm facetTerm) {
		return subQueryType.getSubQuery(queryFieldName, labelQuery ? facetTerm.getLabel() : facetTerm.getTerm());
	}
	
	private static void cropList(List<FacetTerm> list, int newLen) {
		int len = list.size();
		if (len > newLen) {
			List<FacetTerm> subList = list.subList(newLen, len);
			subList.clear();
		}
	}
	
	private int getTailIndex(List<FacetTerm> list) {
		int lo = 0;
		int hi = list.size();
		while (lo < hi) {
			int mid = (lo + hi) / 2;
			FacetTerm info = list.get(mid);
			long value = sort.getCriterion(info);
			if (value >= cutoff) {
				lo = mid + 1;
			}
			else {
				hi = mid;
			}
		}
		return lo;
	}
}
