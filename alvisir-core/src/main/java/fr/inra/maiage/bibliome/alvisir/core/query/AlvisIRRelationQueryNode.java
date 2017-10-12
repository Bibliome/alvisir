package fr.inra.maiage.bibliome.alvisir.core.query;

public class AlvisIRRelationQueryNode implements AlvisIRQueryNode {
	private final String field;
	private final String relation;
	private final AlvisIRQueryNode left;
	private final AlvisIRQueryNode right;
	
	public AlvisIRRelationQueryNode(String field, String relation, AlvisIRQueryNode left, AlvisIRQueryNode right) {
		super();
		this.field = field;
		this.relation = relation;
		this.left = left;
		this.right = right;
	}

	@Override
	public <R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X {
		return visitor.visit(this, param);
	}

	public String getField() {
		return field;
	}

	public String getRelation() {
		return relation;
	}

	public AlvisIRQueryNode getLeft() {
		return left;
	}

	public AlvisIRQueryNode getRight() {
		return right;
	}
}
