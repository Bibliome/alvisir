package fr.inra.maiage.bibliome.alvisir.core.facet;

public enum NullFacetLabelFactory implements FacetLabelFactory {
	INSTANCE;

	@Override
	public String getFacetLabel(String text) throws Exception {
		return null;
	}
}
