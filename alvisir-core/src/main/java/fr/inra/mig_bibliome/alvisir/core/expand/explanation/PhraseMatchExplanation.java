package fr.inra.mig_bibliome.alvisir.core.expand.explanation;

import java.util.Arrays;
import java.util.List;

import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRPhraseQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNode;

/**
 * Match explanation for phrase queries.
 * @author rbossy
 *
 */
public class PhraseMatchExplanation extends MatchExplanation {
	private final int slop;
	private final String[] texts;
	private final String[] normalizedTexts;
	
	/**
	 * Creates a phrase match explanation.
	 * @param doc document to which belong explanation bits.
	 * @param fieldName field of the phrase query.
	 * @param texts texts of the phrase query.
	 * @param slop slop of the phrase query, this is the user slop.
	 */
	public PhraseMatchExplanation(String fieldName, String[] texts, int slop, String[] normalizedTexts) {
		super(fieldName);
		this.slop = slop;
		this.texts = texts;
		this.normalizedTexts = normalizedTexts;
	}
	
	public PhraseMatchExplanation(String fieldName, List<String> texts, int slop, String[] normalizedTexts) {
		this(fieldName, texts.toArray(new String[texts.size()]), slop, normalizedTexts);
	}
	
	public PhraseMatchExplanation(AlvisIRPhraseQueryNode phraseQueryNode, String[] normalizedTexts) {
		this(phraseQueryNode.getField(), phraseQueryNode.getTexts(), phraseQueryNode.getSlop(), normalizedTexts);
	}

	@Override
	public AlvisIRQueryNode getQueryNode() {
		AlvisIRPhraseQueryNode result = new AlvisIRPhraseQueryNode(getFieldName(), slop);
		for (String text : normalizedTexts) {
			result.addText(text);
		}
		return result;
	}

	@Override
	protected PayloadDecoder getPayloadDecoder() {
		if (slop <= 1) {
			return new MergedHighlightMatchExplanationPayloadDecoder();
		}
		return new MatchExplanationPayloadDecoder();
	}

	public int getSlop() {
		return slop;
	}

	public String[] getTexts() {
		return texts;
	}

	public String[] getNormalizedTexts() {
		return normalizedTexts;
	}

	@Override
	public String toString() {
		return "PhraseMatchExplanation[" + slop + "](" + Arrays.deepToString(texts) + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(normalizedTexts);
		result = prime * result + slop;
		result = prime * result + Arrays.hashCode(texts);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PhraseMatchExplanation other = (PhraseMatchExplanation) obj;
		if (!Arrays.equals(normalizedTexts, other.normalizedTexts))
			return false;
		if (slop != other.slop)
			return false;
		if (!Arrays.equals(texts, other.texts))
			return false;
		return true;
	}
}
