package fr.inra.mig_bibliome.alvisir.core.query;

import java.util.List;

import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRAndQueryNode.Clause;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRAndQueryNode.Operator;

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
			List<Clause> clauses = andQueryNode.getClauses();
			if (clauses.size() == 1) {
				return clauses.get(0).getQueryNode().accept(this, param);
			}
			AlvisIRAndQueryNode result = new AlvisIRAndQueryNode();
			for (Clause clause : clauses) {
				Operator op = clause.getOperator();
				AlvisIRQueryNode qn = clause.getQueryNode().accept(this, param);
				switch (op) {
					case AND:
						qn.accept(andQueryNodeReducer, result);
						break;
					case BUT:
						result.addClause(op, qn.accept(this, param));
						break;
				}
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
	};

	private static final AlvisIRQueryNodeVisitor<Void,AlvisIRAndQueryNode,RuntimeException> andQueryNodeReducer = new AlvisIRQueryNodeVisitor<Void,AlvisIRAndQueryNode,RuntimeException>() {
		@Override
		public Void visit(AlvisIRTermQueryNode termQueryNode, AlvisIRAndQueryNode param) {
			param.addClause(Operator.AND, termQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRPhraseQueryNode phraseQueryNode, AlvisIRAndQueryNode param) {
			param.addClause(Operator.AND, phraseQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRPrefixQueryNode prefixQueryNode, AlvisIRAndQueryNode param) {
			param.addClause(Operator.AND, prefixQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRAndQueryNode andQueryNode, AlvisIRAndQueryNode param) {
			for (Clause clause : andQueryNode.getClauses()) {
				param.addClause(clause.getOperator(), clause.getQueryNode());
			}
			return null;
		}

		@Override
		public Void visit(AlvisIROrQueryNode orQueryNode, AlvisIRAndQueryNode param) {
			param.addClause(Operator.AND, orQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRNearQueryNode nearQueryNode, AlvisIRAndQueryNode param) {
			param.addClause(Operator.AND, nearQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRRelationQueryNode relationQueryNode, AlvisIRAndQueryNode param) {
			param.addClause(Operator.AND, relationQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRNoExpansionQueryNode noExpansionQueryNode, AlvisIRAndQueryNode param) {
			param.addClause(Operator.AND, noExpansionQueryNode);
			return null;
		}

		@Override
		public Void visit(AlvisIRAnyQueryNode anyQueryNode, AlvisIRAndQueryNode param) {
			param.addClause(Operator.AND, anyQueryNode);
			return null;
		}
	};
	
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
	};
	
	public static AlvisIRQueryNode reduce(AlvisIRQueryNode queryNode) {
		return queryNode.accept(INSTANCE, null);
	}
}