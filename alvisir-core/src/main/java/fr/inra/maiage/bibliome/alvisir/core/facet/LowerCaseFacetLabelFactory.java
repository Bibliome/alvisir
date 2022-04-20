package fr.inra.maiage.bibliome.alvisir.core.facet;

public class LowerCaseFacetLabelFactory implements FacetLabelFactory {
	private final FacetLabelFactory facetLabelFactory;

	public LowerCaseFacetLabelFactory(FacetLabelFactory facetLabelFactory) {
		super();
		this.facetLabelFactory = facetLabelFactory;
	}

	@Override
	public String getFacetLabel(String text) throws Exception {
		String facetLabel = facetLabelFactory.getFacetLabel(text);
		if (facetLabel == null) {
			return null;
		}
		return facetLabel.toLowerCase();
	}
}
