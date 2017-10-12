package fr.inra.maiage.bibliome.alvisir.core.expand.index;

import java.io.IOException;

import fr.inra.maiage.bibliome.alvisir.core.expand.ExpanderOptions;
import fr.inra.maiage.bibliome.alvisir.core.expand.ExplanationFactory;
import fr.inra.maiage.bibliome.alvisir.core.expand.TextExpander;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.MatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.TermMatchExplanation;

/**
 * Explanation factory for exact searches.
 * This factory creates TermQueryMatchExplanation objects.
 * @author rbossy
 *
 */
class TermExplanationFactory extends ExplanationFactory {
	TermExplanationFactory(String type) {
		super(type);
	}

	@Override
	protected MatchExplanation doGetExplanation(ExpanderOptions expanderOptions, TextExpander textExpander, String fieldName, String canonicalText) throws IOException {
		return new TermMatchExplanation(fieldName, canonicalText, canonicalText);
	}

	@Override
	protected void doSetProperty(String key, String value) throws ExpanderIndexerException {
		throw new ExpanderIndexerException("unhandled property: " + key);
	}
}
