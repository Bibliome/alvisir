package fr.inra.mig_bibliome.alvisir.core.query;


/**
 * AST node representing a query.
 * @author rbossy
 *
 */
public interface AlvisIRQueryNode {
	<R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X;
}
