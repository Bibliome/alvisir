package fr.inra.maiage.bibliome.alvisir.core.expand.explanation;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.spans.SpanTermQuery;

import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRTermQueryNode;

/**
 * Match explanations for term queries.
 * @author rbossy
 *
 */
public class TermMatchExplanation extends MatchExplanation {
	private final String text;
	private final String normalizedText;
	
	public TermMatchExplanation(String fieldName, String text, String normalizedText) {
		super(fieldName);
		this.text = text;
		this.normalizedText = normalizedText;
	}
	
	public TermMatchExplanation(Term term, String normalizedText) {
		this(term.field(), term.text(), normalizedText);
	}

	public TermMatchExplanation(TermQuery query, String normalizedText) {
		this(query.getTerm(), normalizedText);
	}

	public TermMatchExplanation(SpanTermQuery query, String normalizedText) {
		this(query.getTerm(), normalizedText);
	}

	public TermMatchExplanation(AlvisIRTermQueryNode qn, String normalizedText) {
		this(qn.getField(), qn.getText(), normalizedText);
	}

	@Override
	public AlvisIRQueryNode getQueryNode() {
		return new AlvisIRTermQueryNode(getFieldName(), normalizedText);
	}

	@Override
	protected PayloadDecoder getPayloadDecoder() {
		return new MatchExplanationPayloadDecoder();
	}

	public String getText() {
		return text;
	}

	public String getNormalizedText() {
		return normalizedText;
	}

	@Override
	public String toString() {
		return "TermMatchExplanation(" + text + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((normalizedText == null) ? 0 : normalizedText.hashCode());
		result = prime * result + ((text == null) ? 0 : text.hashCode());
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
		TermMatchExplanation other = (TermMatchExplanation) obj;
		if (normalizedText == null) {
			if (other.normalizedText != null)
				return false;
		}
		else if (!normalizedText.equals(other.normalizedText))
			return false;
		if (text == null) {
			if (other.text != null)
				return false;
		}
		else if (!text.equals(other.text))
			return false;
		return true;
	}
}
