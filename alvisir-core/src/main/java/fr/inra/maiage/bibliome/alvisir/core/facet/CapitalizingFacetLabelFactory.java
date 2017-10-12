package fr.inra.maiage.bibliome.alvisir.core.facet;

public class CapitalizingFacetLabelFactory implements FacetLabelFactory {
	private final FacetLabelFactory facetLabelFactory;

	public CapitalizingFacetLabelFactory(FacetLabelFactory facetLabelFactory) {
		super();
		this.facetLabelFactory = facetLabelFactory;
	}

	@Override
	public String getFacetLabel(String text) throws Exception {
		String raw = facetLabelFactory.getFacetLabel(text);
		StringBuilder result = new StringBuilder();
		boolean sow = true;
		for (int i = 0; i < raw.length(); ++i) {
			char c = raw.charAt(i);
			if (sow) {
				c = Character.toUpperCase(c);
			}
			result.append(c);
			sow = Character.isWhitespace(c);
		}
		return result.toString();
	}
}
