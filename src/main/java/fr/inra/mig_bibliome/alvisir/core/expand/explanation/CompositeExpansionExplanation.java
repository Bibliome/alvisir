package fr.inra.mig_bibliome.alvisir.core.expand.explanation;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import fr.inra.mig_bibliome.alvisir.core.AlvisIRIndex;
import fr.inra.mig_bibliome.alvisir.core.SearchConfig;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIROrQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNode;

/**
 * Match explanation for a disjunction of explanations.
 * @author rbossy
 *
 */
public class CompositeExpansionExplanation extends MatchExplanation {
	private final List<MatchExplanation> explanations;

	public CompositeExpansionExplanation(String fieldName, List<MatchExplanation> explanations) {
		super(fieldName);
		this.explanations = explanations;
	}
	
	@Override
	public AlvisIRQueryNode getQueryNode() {
		AlvisIROrQueryNode result = new AlvisIROrQueryNode();
		for (MatchExplanation ex : explanations) {
			result.addClause(ex.getQueryNode());
		}
		return result;
	}

	public List<MatchExplanation> getexplanations() {
		return Collections.unmodifiableList(explanations);
	}

	@Override
	protected PayloadDecoder getPayloadDecoder() {
		return new MatchExplanationPayloadDecoder();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("CompositeExpansionExplanation(");
		boolean notFirst = false;
		for (MatchExplanation e : explanations) {
			if (notFirst)
				sb.append(", ");
			else
				notFirst = true;
			sb.append(e);
		}
		return sb.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((explanations == null) ? 0 : explanations.hashCode());
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
		CompositeExpansionExplanation other = (CompositeExpansionExplanation) obj;
		if (explanations == null) {
			if (other.explanations != null)
				return false;
		}
		else if (!explanations.equals(other.explanations))
			return false;
		return true;
	}

	@Override
	public int computeProductivity(AlvisIRIndex index, SearchConfig searchConfig) throws IOException {
		for (MatchExplanation expl : explanations) {
			expl.computeProductivity(index, searchConfig);
		}
		return super.computeProductivity(index, searchConfig);
	}
}
