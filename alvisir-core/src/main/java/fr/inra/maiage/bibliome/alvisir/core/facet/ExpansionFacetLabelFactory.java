package fr.inra.maiage.bibliome.alvisir.core.facet;

import fr.inra.maiage.bibliome.alvisir.core.expand.TextExpander;
import fr.inra.maiage.bibliome.alvisir.core.expand.TextExpansionResult;

public class ExpansionFacetLabelFactory implements FacetLabelFactory {
	private final TextExpander textExpander;

	public ExpansionFacetLabelFactory(TextExpander textExpander) {
		super();
		this.textExpander = textExpander;
	}

	@Override
	public String getFacetLabel(String text) throws Exception {
		TextExpansionResult textExpansionResult = textExpander.searchCanonical(text);
		String label;
		if (textExpansionResult.any()) {
			label = textExpansionResult.getLabel();
		}
		else {
			label = text;
		}
		return label;
	}
}
