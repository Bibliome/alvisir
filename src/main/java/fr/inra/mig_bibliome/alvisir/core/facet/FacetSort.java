package fr.inra.mig_bibliome.alvisir.core.facet;

import java.util.Comparator;

/**
 * Facet term comparator.
 * @author rbossy
 *
 */
public enum FacetSort implements Comparator<FacetTerm> {
	/**
	 * Sort facet terms by frequency (descending)
	 */
	TERM_FREQUENCY {
		@Override
		long getCriterion(FacetTerm info) {
			return info.get();
		}
	},
	
	/**
	 * Sort facet terms by document frequency (descending)
	 */
	DOCUMENT_FREQUENCY {
		@Override
		long getCriterion(FacetTerm info) {
			return info.getDocFreq();
		}
	};

	abstract long getCriterion(FacetTerm info);

	@Override
	public int compare(FacetTerm a, FacetTerm b) {
		return Long.compare(getCriterion(b), getCriterion(a));
	}
}
