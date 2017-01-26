package fr.inra.mig_bibliome.alvisir.core.facet;

public enum NullFacetLabelFactory implements FacetLabelFactory {
	INSTANCE;

	@Override
	public String getFacetLabel(String text) throws Exception {
		return text;
	}
}
