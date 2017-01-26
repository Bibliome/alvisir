package fr.inra.mig_bibliome.alvisir.core.expand.explanation;

import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRNearQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNode;

public class NearMatchExplanation extends MatchExplanation {
	private final int slop;
	private final AlvisIRQueryNode left;
	private final AlvisIRQueryNode right;

	public NearMatchExplanation(String fieldName, int slop, AlvisIRQueryNode left, AlvisIRQueryNode right) {
		super(fieldName);
		this.slop = slop;
		this.left = left;
		this.right = right;
	}

	@Override
	public AlvisIRQueryNode getQueryNode() {
		return new AlvisIRNearQueryNode(getFieldName(), slop, left, right);
	}

	@Override
	protected PayloadDecoder getPayloadDecoder() {
		return new MergedHighlightMatchExplanationPayloadDecoder();
	}

	@Override
	public String toString() {
		return "NearMatchExplanation[" + slop + "](" + left + ", " + right + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
		result = prime * result + slop;
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
		NearMatchExplanation other = (NearMatchExplanation) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		}
		else if (!left.equals(other.left))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		}
		else if (!right.equals(other.right))
			return false;
		if (slop != other.slop)
			return false;
		return true;
	}
}
