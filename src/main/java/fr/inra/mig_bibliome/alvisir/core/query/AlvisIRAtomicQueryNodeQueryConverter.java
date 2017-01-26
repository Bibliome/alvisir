package fr.inra.mig_bibliome.alvisir.core.query;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

/**
 * Converter from an atomic query node to a regular lucene query.
 * If this visitor visits a non-atomic query node (conjunction, disjunction or proximity), then it raises RuntimeException.
 * @author rbossy
 *
 */
class AlvisIRAtomicQueryNodeQueryConverter extends AbstractAlvisIRQueryNodeQueryConverter<Query,RuntimeException> {
	AlvisIRAtomicQueryNodeQueryConverter(int tokenPositionGap) {
		super(tokenPositionGap);
	}

	@Override
	public Query visit(AlvisIRTermQueryNode termQueryNode, String param) {
		return new TermQuery(new Term(param, termQueryNode.getText()));
	}

	@Override
	public Query visit(AlvisIRPhraseQueryNode phraseQueryNode, String param) {
		PhraseQuery result = new PhraseQuery();
		result.setSlop(getRealSlop(phraseQueryNode.getSlop()));
		for (String text : phraseQueryNode.getTexts()) {
			result.add(new Term(param, text));
		}
		return result;
	}

	@Override
	public Query visit(AlvisIRPrefixQueryNode prefixQueryNode, String param) {
		PrefixQuery result = new PrefixQuery(new Term(param, prefixQueryNode.getPrefix()));
//		result.setRewriteMethod(MultiTermQuery.SCORING_BOOLEAN_QUERY_REWRITE);
		return result;
	}

	@Override
	public Query visit(AlvisIRAndQueryNode andQueryNode, String param) {
		throw new RuntimeException();
	}

	@Override
	public Query visit(AlvisIROrQueryNode orQueryNode, String param) {
		throw new RuntimeException();
	}

	@Override
	public Query visit(AlvisIRNearQueryNode nearQueryNode, String param) {
		throw new RuntimeException();
	}

	@Override
	public Query visit(AlvisIRRelationQueryNode relationQueryNode, String param) {
		return RelationArgumentVisitor.getWildcardQuery(relationQueryNode, param);
	}
	
	@Override
	public Query visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, String param) {
		return noExpansionQueryNode.getQueryNode().accept(this, param);
	}

	@Override
	public Query visit(AlvisIRAnyQueryNode anyQueryNode, String param) {
		return new MatchAllDocsQuery();
	}
}
