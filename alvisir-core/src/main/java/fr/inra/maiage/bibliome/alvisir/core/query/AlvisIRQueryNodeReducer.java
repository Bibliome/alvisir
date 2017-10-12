package fr.inra.maiage.bibliome.alvisir.core.query;

import java.util.List;

import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRAndQueryNode.Clause;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRAndQueryNode.Operator;

public enum AlvisIRQueryNodeReducer implements AlvisIRQueryNodeVisitor<AlvisIRQueryNode,Void,RuntimeException> {
	INSTANCE {
		@Override
		public AlvisIRQueryNode visit(AlvisIRTermQueryNode termQueryNode, Void param) {
			return termQueryNode;
		}

		@Override
		public AlvisIRQueryNode visit(AlvisIRPhraseQueryNode phraseQueryNode, Void param) {
			return phraseQueryNode;
		}

		@Override
		public AlvisIRQueryNode visit(AlvisIRPrefixQueryNode prefixQueryNode, Void param) {
			return prefixQueryNode;
		}

		@Override
		public AlvisIRQueryNode visit(AlvisIRAndQueryNode andQueryNode, Void param) {
			AlvisIRAndQueryNode result = new AlvisIRAndQueryNode();
			AndQueryNodeReducer andQueryNodeReducer = new AndQueryNodeReducer(result);
			for (Clause clause : andQueryNode.getClauses()) {
				Operator op = clause.getOperator();
				AlvisIRQueryNode qn = clause.getQueryNode().accept(this, param);
				qn.accept(andQueryNodeReducer, op);
			}
			andQueryNodeReducer.finishTermList();
			List<Clause> clauses = result.getClauses();
			if (clauses.size() == 1) {
				return clauses.get(0).getQueryNode();
			}
			return result;
		}

		@Override
		public AlvisIRQueryNode visit(AlvisIROrQueryNode orQueryNode, Void param) {
			List<AlvisIRQueryNode> clauses = orQueryNode.getClauses();
			if (clauses.size() == 1) {
				return clauses.get(0).accept(this, param);
			}
			AlvisIROrQueryNode result = new AlvisIROrQueryNode();
			for (AlvisIRQueryNode clause : clauses) {
				clause.accept(this, param).accept(orQueryNodeReducer, result);
			}
			return result;
		}

		@Override
		public AlvisIRQueryNode visit(AlvisIRNearQueryNode nearQueryNode, Void param) {
			AlvisIRQueryNode left = nearQueryNode.getLeft().accept(this, param);
			AlvisIRQueryNode right = nearQueryNode.getRight().accept(this, param);
			return new AlvisIRNearQueryNode(nearQueryNode.getField(), nearQueryNode.getSlop(), left, right);
		}

		@Override
		public AlvisIRQueryNode visit(AlvisIRRelationQueryNode relationQueryNode, Void param) {
			AlvisIRQueryNode left = relationQueryNode.getLeft().accept(this, param);
			AlvisIRQueryNode right = relationQueryNode.getRight().accept(this, param);
			return new AlvisIRRelationQueryNode(relationQueryNode.getField(), relationQueryNode.getRelation(), left, right);
		}

		@Override
		public AlvisIRQueryNode visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, Void param) {
			AlvisIRQueryNode qn = noExpansionQueryNode.getQueryNode().accept(this, param);
			return new AlvisIRNoExpansionQueryNode(qn);
		}

		@Override
		public AlvisIRQueryNode visit(AlvisIRAnyQueryNode anyQueryNode, Void param) {
			return anyQueryNode;
		}

		@Override
		public AlvisIRQueryNode visit(AlvisIRTermListQueryNode termListQueryNode, Void param) throws RuntimeException {
			return termListQueryNode;
		}
	};
	
	private static class AndQueryNodeReducer implements AlvisIRQueryNodeVisitor<Void,Operator,RuntimeException> {
		private final AlvisIRAndQueryNode andQueryNode;
		private AlvisIRTermListQueryNode termListQueryNode = null;
		
		private AndQueryNodeReducer(AlvisIRAndQueryNode andQueryNode) {
			super();
			this.andQueryNode = andQueryNode;
		}

		@Override
		public Void visit(AlvisIRTermQueryNode termQueryNode, Operator param) throws RuntimeException {
			if (termListQueryNode == null) {
				startTermList(termQueryNode, param);
			}
			else {
				if (param.equals(termListQueryNode.getOperator()) && termQueryNode.getField().equals(termListQueryNode.getField())) {
					termListQueryNode.addText(termQueryNode.getText());
				}
				else {
					finishTermList();
					startTermList(termQueryNode, param);
				}
			}
			return null;
		}
		
		private void startTermList(AlvisIRTermQueryNode termQueryNode, Operator operator) {
			termListQueryNode = new AlvisIRTermListQueryNode(operator, termQueryNode.getField());
			termListQueryNode.addText(termQueryNode.getText());
		}
		
		private void finishTermList() {
			if (termListQueryNode == null) {
				return;
			}
			List<String> texts = termListQueryNode.getTexts();
			if (texts.size() == 1) {
				andQueryNode.addClause(termListQueryNode.getOperator(), new AlvisIRTermQueryNode(termListQueryNode.getField(), texts.get(0)));
			}
			else {
				andQueryNode.addClause(termListQueryNode.getOperator(), termListQueryNode);
			}
			termListQueryNode = null;
		}

		@Override
		public Void visit(AlvisIRPhraseQueryNode phraseQueryNode, Operator param) throws RuntimeException {
			finishTermList();
			andQueryNode.addClause(param, phraseQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRPrefixQueryNode prefixQueryNode, Operator param) throws RuntimeException {
			finishTermList();
			andQueryNode.addClause(param, prefixQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRAndQueryNode andQueryNode, Operator param) throws RuntimeException {
			finishTermList();
			this.andQueryNode.addClause(param, andQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIROrQueryNode orQueryNode, Operator param) throws RuntimeException {
			finishTermList();
			andQueryNode.addClause(param, orQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRNearQueryNode nearQueryNode, Operator param) throws RuntimeException {
			finishTermList();
			andQueryNode.addClause(param, nearQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRRelationQueryNode relationQueryNode, Operator param) throws RuntimeException {
			finishTermList();
			andQueryNode.addClause(param, relationQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, Operator param) throws RuntimeException {
			finishTermList();
			andQueryNode.addClause(param, noExpansionQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRAnyQueryNode anyQueryNode, Operator param) throws RuntimeException {
			finishTermList();
			andQueryNode.addClause(param, anyQueryNode);//XXX
			return null;
		}

		@Override
		public Void visit(AlvisIRTermListQueryNode termListQueryNode, Operator param) throws RuntimeException {
			andQueryNode.addClause(param, termListQueryNode);
			return null;
		}
	}

	private static final AlvisIRQueryNodeVisitor<Void,AlvisIROrQueryNode,RuntimeException> orQueryNodeReducer = new AlvisIRQueryNodeVisitor<Void,AlvisIROrQueryNode,RuntimeException>() {
		@Override
		public Void visit(AlvisIRTermQueryNode termQueryNode, AlvisIROrQueryNode param) {
			param.addClause(termQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRPhraseQueryNode phraseQueryNode, AlvisIROrQueryNode param) {
			param.addClause(phraseQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRPrefixQueryNode prefixQueryNode, AlvisIROrQueryNode param) {
			param.addClause(prefixQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRAndQueryNode andQueryNode, AlvisIROrQueryNode param) {
			param.addClause(andQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIROrQueryNode orQueryNode, AlvisIROrQueryNode param) {
			for (AlvisIRQueryNode clause : orQueryNode.getClauses()) {
				clause.accept(this, param);
			}
			return null;
		}

		@Override
		public Void visit(AlvisIRNearQueryNode nearQueryNode, AlvisIROrQueryNode param) {
			param.addClause(nearQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRRelationQueryNode relationQueryNode, AlvisIROrQueryNode param) {
			param.addClause(relationQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, AlvisIROrQueryNode param) {
			param.addClause(noExpansionQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRAnyQueryNode anyQueryNode, AlvisIROrQueryNode param) {
			param.addClause(anyQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRTermListQueryNode termListQueryNode, AlvisIROrQueryNode param) throws RuntimeException {
			param.addClause(termListQueryNode);
			return null;
		}
	};
	
	public static AlvisIRQueryNode reduce(AlvisIRQueryNode queryNode) {
		return queryNode.accept(INSTANCE, null);
	}
}
