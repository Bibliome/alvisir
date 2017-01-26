package fr.inra.mig_bibliome.alvisir.core.facet;

public class UpperCaseFacetLabelFactory implements FacetLabelFactory {
	private final FacetLabelFactory facetLabelFactory;

	public UpperCaseFacetLabelFactory(FacetLabelFactory facetLabelFactory) {
		super();
		this.facetLabelFactory = facetLabelFactory;
	}

	@Override
	public String getFacetLabel(String text) throws Exception {
		String facetLabel = facetLabelFactory.getFacetLabel(text);
		return facetLabel.toUpperCase();
	}
}
