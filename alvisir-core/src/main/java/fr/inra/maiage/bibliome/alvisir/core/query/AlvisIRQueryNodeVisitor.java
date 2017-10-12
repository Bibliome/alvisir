package fr.inra.maiage.bibliome.alvisir.core.query;

public interface AlvisIRQueryNodeVisitor<R,P,X extends Exception> {
	R visit(AlvisIRTermQueryNode termQueryNode, P param) throws X;
	R visit(AlvisIRPhraseQueryNode phraseQueryNode, P param) throws X;
	R visit(AlvisIRPrefixQueryNode prefixQueryNode, P param) throws X;
	R visit(AlvisIRAndQueryNode andQueryNode, P param) throws X;
	R visit(AlvisIROrQueryNode orQueryNode, P param) throws X;
	R visit(AlvisIRNearQueryNode nearQueryNode, P param) throws X;
	R visit(AlvisIRRelationQueryNode relationQueryNode, P param) throws X;
	R visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, P param) throws X;
	R visit(AlvisIRAnyQueryNode anyQueryNode, P param) throws X;
	R visit(AlvisIRTermListQueryNode termListQueryNode, P param) throws X;
}
