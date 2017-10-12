package fr.inra.maiage.bibliome.alvisir.core.query;

public enum AlvisIRAnyQueryNode implements AlvisIRQueryNode {
	INSTANCE;

	@Override
	public <R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X {
		return visitor.visit(this, param);
	}
}
