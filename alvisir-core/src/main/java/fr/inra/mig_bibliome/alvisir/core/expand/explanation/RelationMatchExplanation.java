package fr.inra.mig_bibliome.alvisir.core.expand.explanation;

import java.util.Collections;
import java.util.List;

import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRRelationQueryNode;

public class RelationMatchExplanation extends MatchExplanation {
	private final String relation;
	private final String normalizedRelation;
	private final AlvisIRQueryNode left;
	private final AlvisIRQueryNode right;
	private final List<MatchExplanation> leftExplanations;
	private final List<MatchExplanation> rightExplanations;
	
	public RelationMatchExplanation(String fieldName, String relation, String normalizedRelation, List<MatchExplanation> leftExplanations, AlvisIRQueryNode left, List<MatchExplanation> rightExplanations, AlvisIRQueryNode right) {
		super(fieldName);
		this.relation = relation;
		this.normalizedRelation = normalizedRelation;
		this.left = left;
		this.right = right;
		this.leftExplanations = leftExplanations;
		this.rightExplanations = rightExplanations;
	}

	@Override
	protected PayloadDecoder getPayloadDecoder() {
		return new MatchExplanationPayloadDecoder();
	}

	@Override
	public AlvisIRQueryNode getQueryNode() {
		return new AlvisIRRelationQueryNode(getFieldName(), relation, left, right);
	}

	@Override
	public String toString() {
		return "RelationMatchExplanation[" + relation + "](" + left + ", " + right + ")";
	}

	public String getRelation() {
		return relation;
	}

	public String getNormalizedRelation() {
		return normalizedRelation;
	}

	public AlvisIRQueryNode getLeft() {
		return left;
	}

	public AlvisIRQueryNode getRight() {
		return right;
	}

	public List<MatchExplanation> getLeftExplanations() {
		return Collections.unmodifiableList(leftExplanations);
	}

	public List<MatchExplanation> getRightExplanations() {
		return Collections.unmodifiableList(rightExplanations);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((left == null) ? 0 : left.hashCode());
		result = prime * result + ((relation == null) ? 0 : relation.hashCode());
		result = prime * result + ((right == null) ? 0 : right.hashCode());
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
		RelationMatchExplanation other = (RelationMatchExplanation) obj;
		if (left == null) {
			if (other.left != null)
				return false;
		}
		else if (!left.equals(other.left))
			return false;
		if (relation == null) {
			if (other.relation != null)
				return false;
		}
		else if (!relation.equals(other.relation))
			return false;
		if (right == null) {
			if (other.right != null)
				return false;
		}
		else if (!right.equals(other.right))
			return false;
		return true;
	}
}
