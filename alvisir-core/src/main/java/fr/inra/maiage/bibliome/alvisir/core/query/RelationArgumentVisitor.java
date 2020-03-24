package fr.inra.maiage.bibliome.alvisir.core.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.WildcardQuery;

final class RelationArgumentVisitor implements AlvisIRQueryNodeVisitor<Void,StringBuilder,RuntimeException> {
	@Override
	public Void visit(AlvisIRTermQueryNode termQueryNode, StringBuilder param) {
		param.append(termQueryNode.getText());
		return null;
	}

	@Override
	public Void visit(AlvisIRPhraseQueryNode phraseQueryNode, StringBuilder param) {
		param.append("___PHRASE_AS_RELATION_ARGUMENT___");
		return null;
//		throw new RuntimeException();
	}

	@Override
	public Void visit(AlvisIRPrefixQueryNode prefixQueryNode, StringBuilder param) {
		param.append(prefixQueryNode.getPrefix());
		param.append('*');
		return null;
	}

	@Override
	public Void visit(AlvisIRAndQueryNode andQueryNode, StringBuilder param) {
		param.append("___CONJUNCTION_AS_RELATION_ARGUMENT___");
		return null;
//		throw new RuntimeException();
	}

	@Override
	public Void visit(AlvisIROrQueryNode orQueryNode, StringBuilder param) {
		orQueryNode.getClauses().get(0).accept(this, param);
		return null;
//	XXX	throw new RuntimeException();
	}

	@Override
	public Void visit(AlvisIRNearQueryNode nearQueryNode, StringBuilder param) {
		param.append("___NEAR_AS_RELATION_ARGUMENT___");
		return null;
//		throw new RuntimeException();
	}

	@Override
	public Void visit(AlvisIRRelationQueryNode relationQueryNode, StringBuilder param) {
		param.append("___RELATION_AS_RELATION_ARGUMENT___");
		return null;
//		throw new RuntimeException();
	}
	
	@Override
	public Void visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, StringBuilder param) {
		noExpansionQueryNode.getQueryNode().accept(this, param);
		return null;
	}

	@Override
	public Void visit(AlvisIRAnyQueryNode anyQueryNode, StringBuilder param) {
		param.append('*');
		return null;
	}

	@Override
	public Void visit(AlvisIRTermListQueryNode termListQueryNode, StringBuilder param) throws RuntimeException {
		param.append("___TERM_LIST_AS_RELATION_ARGUMENT___");
		return null;
	}

	static WildcardQuery getWildcardQuery(AlvisIRRelationQueryNode relationQueryNode, String fieldName) {
		StringBuilder sb = new StringBuilder();
		sb.append('{');
		sb.append(relationQueryNode.getRelation());
		sb.append('}');
		RelationArgumentVisitor visitor = new RelationArgumentVisitor();
		AlvisIRQueryNodeReducer.reduce(relationQueryNode.getLeft()).accept(visitor, sb);
		sb.append('~');
		AlvisIRQueryNodeReducer.reduce(relationQueryNode.getRight()).accept(visitor, sb);
//		System.err.println("sb = " + sb);
		Term term = new Term(fieldName, sb.toString());
		return new WildcardQuery(term);
	}
}
