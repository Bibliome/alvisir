package fr.inra.maiage.bibliome.alvisir.core.facet;

import fr.inra.maiage.bibliome.util.count.Count;

/**
 * Term collected in a facet.
 * @author rbossy
 *
 */
public final class FacetTerm extends Count {
	private final String term;
	private String label;
	private long docFreq;
	private int lastSeenDoc = -1;
	
	FacetTerm(String term) {
		super();
		this.term = term;
	}

	/**
	 * Returns the label of the term.
	 * @return the label of the term.
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets the label of this term.
	 * @param label
	 */
	void setLabel(String label) {
		this.label = label;
	}

	/**
	 * Returns the collected term.
	 * @return the collected term.
	 */
	public String getTerm() {
		return term;
	}
	
	/**
	 * Returns the document frequency of the term.
	 * @return the document frequency of the term.
	 */
	public long getDocFreq() {
		return docFreq;
	}
	
	void incrDocFreq(int doc) {
		if (doc != lastSeenDoc) {
			docFreq++;
			lastSeenDoc = doc;
		}
	}
}
