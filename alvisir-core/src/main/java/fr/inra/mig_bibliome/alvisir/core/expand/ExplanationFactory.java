package fr.inra.mig_bibliome.alvisir.core.expand;

import java.io.IOException;
import java.util.Arrays;

import fr.inra.mig_bibliome.alvisir.core.expand.explanation.MatchExplanation;
import fr.inra.mig_bibliome.alvisir.core.expand.index.ExpanderIndexerException;

/**
 * Match explanation factory.
 * Expander indexes yields a match explanation factory for a given annotation type type.
 * @author rbossy
 *
 */
public abstract class ExplanationFactory {
	private final String type;
	private int maxSynonyms = Integer.MAX_VALUE;
	
	/**
	 * Creates a match explanation factory for the specified annotation type.
	 * @param type
	 */
	protected ExplanationFactory(String type) {
		super();
		this.type = type;
	}

	/**
	 * Creates a match explanation.
	 * @param doc document to which belong explanation bits of the returned explanation.
	 * @param fieldName name of the field.
	 * @param canonicalText canonical text.
	 * @return
	 * @throws IOException
	 * @throws ExpanderException 
	 */
	public MatchExplanation getExplanation(ExpanderOptions expanderOptions, TextExpander textExpander, String fieldName, TextExpansionResult textExpansionResult) throws IOException, ExpanderException {
		MatchExplanation result = doGetExplanation(expanderOptions, textExpander, fieldName, textExpansionResult.getCanonical());
		result.setType(type);
		result.setLabel(textExpansionResult.getLabel());
		addSynonymsExplanationBits(result, textExpansionResult.getSynonyms());
		return result;
	}
	
	private void addSynonymsExplanationBits(MatchExplanation explanation, String[] synonyms) {
		int n = Math.min(synonyms.length, maxSynonyms);
		if (synonyms.length > n) {
			explanation.setMoreSynonyms(synonyms.length - n);
			explanation.setSynonyms(Arrays.copyOf(synonyms, n));
		}
		else {
			explanation.setSynonyms(synonyms);
		}
	}

	/**
	 * Effectively creates a match explanation.
	 * @param doc document to which belong explanation bits of the returned explanation.
	 * @param fieldName name of the field.
	 * @param canonicalText canonical text.
	 * @return
	 * @throws IOException
	 * @throws ExpanderException 
	 */
	protected abstract MatchExplanation doGetExplanation(ExpanderOptions expanderOptions, TextExpander textExpander, String fieldName, String canonicalText) throws IOException, ExpanderException;
	
	public void setProperty(String key, String value) throws ExpanderIndexerException {
		if (key.equals("max-synonyms")) {
			maxSynonyms = Integer.parseInt(value);
		}
		else {
			doSetProperty(key, value);
		}
	}
	
	protected abstract void doSetProperty(String key, String value) throws ExpanderIndexerException;
}
