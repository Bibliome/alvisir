package fr.inra.mig_bibliome.alvisir.core.query;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

import fr.inra.mig_bibliome.alvisir.core.FieldOptions;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRAndQueryNode.Operator;

/**
 * Converter form query node to lucene query with field options given as parameter.
 * @author rbossy
 *
 */
public class AlvisIRQueryNodeQueryConverter implements AlvisIRQueryNodeVisitor<Query,FieldOptions,RuntimeException> {
	private final AlvisIRAtomicQueryNodeQueryConverter atomicQueryNodeConverter;
	private final AlvisIRQueryNodeSpanQueryConverter queryNodeSpanConverter;
	
	private AlvisIRQueryNodeQueryConverter(int tokenPositionGap) {
		atomicQueryNodeConverter = new AlvisIRAtomicQueryNodeQueryConverter(tokenPositionGap);
		queryNodeSpanConverter = new AlvisIRQueryNodeSpanQueryConverter(tokenPositionGap);
	}
	
	@Override
	public Query visit(AlvisIRTermQueryNode termQueryNode, FieldOptions param) {
		return convertAtomicQueryNode(termQueryNode, termQueryNode.getField(), param, atomicQueryNodeConverter);
	}

	@Override
	public Query visit(AlvisIRPhraseQueryNode phraseQueryNode, FieldOptions param) {
		return convertAtomicQueryNode(phraseQueryNode, phraseQueryNode.getField(), param, atomicQueryNodeConverter);
	}

	@Override
	public Query visit(AlvisIRPrefixQueryNode prefixQueryNode, FieldOptions param) {
		return convertAtomicQueryNode(prefixQueryNode, prefixQueryNode.getField(), param, atomicQueryNodeConverter);
	}

	@Override
	public Query visit(AlvisIRRelationQueryNode relationQueryNode, FieldOptions param) {
		return convertAtomicQueryNode(relationQueryNode, relationQueryNode.getField(), param, atomicQueryNodeConverter);
	}

	@Override
	public Query visit(AlvisIRAndQueryNode andQueryNode, FieldOptions param) {
		BooleanQuery result = new BooleanQuery();
		for (AlvisIRAndQueryNode.Clause clause : andQueryNode.getClauses()) {
			Query q = clause.getQueryNode().accept(this, param);
			BooleanClause.Occur occ = getOccur(clause.getOperator());
			result.add(new BooleanClause(q, occ));
		}
		return result;
	}

	@Override
	public Query visit(AlvisIROrQueryNode orQueryNode, FieldOptions param) {
		BooleanQuery result = createDisjunction();
		for (AlvisIRQueryNode qn : orQueryNode.getClauses()) {
			Query q = qn.accept(this, param);
			addDisjunctionClause(result, q);
		}
		return result;
	}

	@Override
	public Query visit(AlvisIRNearQueryNode nearQueryNode, FieldOptions param) {
		return convertAtomicQueryNode(nearQueryNode, nearQueryNode.getField(), param, queryNodeSpanConverter);
	}
	
	@Override
	public Query visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, FieldOptions param) {
		return noExpansionQueryNode.getQueryNode().accept(this, param);
	}

	@Override
	public Query visit(AlvisIRAnyQueryNode noExpansionQueryNode, FieldOptions param) {
		return new MatchAllDocsQuery();
	}

	private static Query convertAtomicQueryNode(AlvisIRQueryNode qn, String fieldName, FieldOptions fieldOptions, AlvisIRQueryNodeVisitor<? extends Query,String,RuntimeException> visitor) {
		String[] fields = fieldOptions.getFields(fieldName);
		if (fields.length == 1) {
			return qn.accept(visitor, fields[0]);
		}
		BooleanQuery result = createDisjunction();
		for (String field : fields) {
			Query q = qn.accept(visitor, field);
			addDisjunctionClause(result, q);
		}
		return result;
	}

	private static void addDisjunctionClause(BooleanQuery disjunction, Query q) {
		disjunction.add(new BooleanClause(q, BooleanClause.Occur.SHOULD));
	}

	private static BooleanQuery createDisjunction() {
		BooleanQuery result = new BooleanQuery();
		result.setMinimumNumberShouldMatch(1);
		return result;
	}

	private static BooleanClause.Occur getOccur(Operator operator) {
		switch (operator) {
			case AND: return BooleanClause.Occur.MUST;
			case BUT: return BooleanClause.Occur.MUST_NOT;
		}
		return BooleanClause.Occur.MUST;
	}
	
	/**
	 * Converts the specified query node to a lucence query.
	 * @param tokenPositionGap
	 * @param fieldOptions
	 * @param queryNode
	 * @return
	 */
	public static Query convert(int tokenPositionGap, FieldOptions fieldOptions, AlvisIRQueryNode queryNode) {
		return AlvisIRQueryNodeReducer.reduce(queryNode).accept(new AlvisIRQueryNodeQueryConverter(tokenPositionGap), fieldOptions);
	}
}
