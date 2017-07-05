package fr.inra.mig_bibliome.alvisir.core.query;

import java.util.LinkedHashSet;
import java.util.Set;

import fr.inra.mig_bibliome.alvisir.core.SearchConfig;

public enum AlvisIRQueryFields implements AlvisIRQueryNodeVisitor<Set<String>,Set<String>,RuntimeException> {
	INSTANCE;
	
	@Override
	public Set<String> visit(AlvisIRTermQueryNode termQueryNode, Set<String> param) {
		param.add(termQueryNode.getField());
		return param;
	}

	@Override
	public Set<String> visit(AlvisIRPhraseQueryNode phraseQueryNode, Set<String> param) {
		param.add(phraseQueryNode.getField());
		return param;
	}

	@Override
	public Set<String> visit(AlvisIRPrefixQueryNode prefixQueryNode, Set<String> param) {
		param.add(prefixQueryNode.getField());
		return param;
	}

	@Override
	public Set<String> visit(AlvisIRAndQueryNode andQueryNode, Set<String> param) {
		for (AlvisIRAndQueryNode.Clause clause : andQueryNode.getClauses()) {
			AlvisIRQueryNode node = clause.getQueryNode();
			node.accept(this, param);
		}
		return param;
	}

	@Override
	public Set<String> visit(AlvisIROrQueryNode orQueryNode, Set<String> param) {
		for (AlvisIRQueryNode node : orQueryNode.getClauses()) {
			node.accept(this, param);
		}
		return param;
	}

	@Override
	public Set<String> visit(AlvisIRNearQueryNode nearQueryNode, Set<String> param) {
		nearQueryNode.getLeft().accept(this, param);
		nearQueryNode.getRight().accept(this, param);
		return param;
	}
	
	@Override
	public Set<String> visit(AlvisIRRelationQueryNode relationQueryNode, Set<String> param) {
		relationQueryNode.getLeft().accept(this, param);
		relationQueryNode.getRight().accept(this, param);
		return param;
	}

	@Override
	public Set<String> visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, Set<String> param) {
		return noExpansionQueryNode.getQueryNode().accept(this, param);
	}

	@Override
	public Set<String> visit(AlvisIRAnyQueryNode noExpansionQueryNode, Set<String> param) {
		return param;
	}

	@Override
	public Set<String> visit(AlvisIRTermListQueryNode termListQueryNode, Set<String> param) throws RuntimeException {
		param.add(termListQueryNode.getField());
		return param;
	}

	public static Set<String> collectQueryFields(AlvisIRQueryNode queryNode) {
		return queryNode.accept(INSTANCE, new LinkedHashSet<String>());
	}
	
	public static Set<String> collectQueryFields(SearchConfig config, AlvisIRQueryNode queryNode) {
		Set<String> result = new LinkedHashSet<String>();
		for (String fieldName : collectQueryFields(queryNode)) {
			for (String f : config.getFields(fieldName)) {
				result.add(f);
			}
		}
		return result;
	}
}
