package fr.inra.maiage.bibliome.alvisir.core.query;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanOrQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;

import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRAndQueryNode.Clause;

/**
 * Converts a query node into a lucene span query
 * @author rbossy
 *
 */
public class AlvisIRQueryNodeSpanQueryConverter extends AbstractAlvisIRQueryNodeQueryConverter<SpanQuery,RuntimeException> {
	AlvisIRQueryNodeSpanQueryConverter(int tokenPositionGap) {
		super(tokenPositionGap);
	}

	@Override
	public SpanQuery visit(AlvisIRTermQueryNode termQueryNode, String param) {
		return new SpanTermQuery(new Term(param, termQueryNode.getText()));
	}

	@Override
	public SpanQuery visit(AlvisIRPhraseQueryNode phraseQueryNode, String param) {
		List<String> texts = phraseQueryNode.getTexts();
		SpanQuery[] clauses = new SpanQuery[texts.size()];
		for (int i = 0; i < clauses.length; ++i) {
			clauses[i] = new SpanTermQuery(new Term(param, texts.get(i)));
		}
		return new SpanNearQuery(clauses, getRealSlop(phraseQueryNode.getSlop()), true);
	}

	@Override
	public SpanQuery visit(AlvisIRPrefixQueryNode prefixQueryNode, String param) {
		PrefixQuery prefixQuery = new PrefixQuery(new Term(param, prefixQueryNode.getPrefix()));
		return new SpanMultiTermQueryWrapper<PrefixQuery>(prefixQuery);
	}

	@Override
	public SpanQuery visit(AlvisIRAndQueryNode andQueryNode, String param) {
		List<Clause> clauseNodes = andQueryNode.getClauses();
		SpanQuery[] clauses = new SpanQuery[clauseNodes.size()];
		for (int i = 0; i < clauses.length; ++i) {
			Clause c = clauseNodes.get(i);
			if (c.getOperator() == AlvisIRAndQueryNode.Operator.BUT) {
				throw new RuntimeException("negation is not allowed inside near queries");
			}
			clauses[i] = c.getQueryNode().accept(this, param);
		}
		return new SpanNearQuery(clauses, Integer.MAX_VALUE, false);
	}
	
	@Override
	public SpanQuery visit(AlvisIROrQueryNode orQueryNode, String param) {
		List<AlvisIRQueryNode> clauseNodes = orQueryNode.getClauses();
		SpanQuery[] clauses = new SpanQuery[clauseNodes.size()];
		for (int i = 0; i < clauses.length; ++i) {
			clauses[i] = clauseNodes.get(i).accept(this, param);
		}
		return new SpanOrQuery(clauses );
	}

	@Override
	public SpanQuery visit(AlvisIRNearQueryNode nearQueryNode, String param) {
		SpanQuery left = nearQueryNode.getLeft().accept(this, param);
		SpanQuery right = nearQueryNode.getRight().accept(this, param);
		return new SpanNearQuery(new SpanQuery[] { left, right }, getRealSlop(nearQueryNode.getSlop()), true);
	}
	
	@Override
	public SpanQuery visit(AlvisIRRelationQueryNode relationQueryNode, String param) {
		WildcardQuery wcq = RelationArgumentVisitor.getWildcardQuery(relationQueryNode, param);
		return new SpanMultiTermQueryWrapper<WildcardQuery>(wcq);
	}

	@Override
	public SpanQuery visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, String param) {
		return noExpansionQueryNode.getQueryNode().accept(this, param);
	}

	@Override
	public SpanQuery visit(AlvisIRTermListQueryNode termListQueryNode, String param) throws RuntimeException {
		return termListQueryNode.toAndQueryNode().accept(this, param);
	}

	@Override
	public SpanQuery visit(AlvisIRAnyQueryNode anyQueryNode, String param) {
		return nullSpanQuery;
	}
	
	private static final Spans nullSpans = new Spans() {
		@Override
		public boolean next() throws IOException {
			return false;
		}

		@Override
		public boolean skipTo(int target) throws IOException {
			return false;
		}

		@Override
		public int doc() {
			return 0;
		}

		@Override
		public int start() {
			return 0;
		}

		@Override
		public int end() {
			return 0;
		}

		@Override
		public Collection<byte[]> getPayload() throws IOException {
			return null;
		}

		@Override
		public boolean isPayloadAvailable() {
			return false;
		}
	};
	
	@SuppressWarnings("serial")
	private static final SpanQuery nullSpanQuery = new SpanQuery() {
		@Override
		public String toString(String field) {
			return field + ":*";
		}
		
		@Override
		public Spans getSpans(IndexReader reader) throws IOException {
			return nullSpans;
		}
		
		@Override
		public String getField() {
			return null;
		}
	};

	/**
	 * Converts the specified query node into a lucene span query.
	 * @param tokenPositionGap
	 * @param fieldName
	 * @param queryNode
	 * @return
	 */
	public static SpanQuery convert(int tokenPositionGap, String fieldName, AlvisIRQueryNode queryNode) {
		return queryNode.accept(new AlvisIRQueryNodeSpanQueryConverter(tokenPositionGap), fieldName);
	}
}
