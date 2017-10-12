package fr.inra.maiage.bibliome.alvisir.core.query;

public class AlvisIRNoExpansionQueryNode implements AlvisIRQueryNode {
	private final AlvisIRQueryNode queryNode;

	public AlvisIRNoExpansionQueryNode(AlvisIRQueryNode queryNode) {
		super();
		this.queryNode = queryNode;
	}

	public AlvisIRQueryNode getQueryNode() {
		return queryNode;
	}

	@Override
	public <R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X {
		return visitor.visit(this, param);
	}
}
