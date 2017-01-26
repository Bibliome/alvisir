package fr.inra.mig_bibliome.alvisir.core.expand;

import java.util.List;

import fr.inra.mig_bibliome.alvisir.core.FieldOptions;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRAndQueryNode;

public enum SimpleAndQueryNodeExpander implements AndQueryNodeExpander {
	INSTANCE {
		@Override
		public ExpansionResult expandAndClauses(FieldOptions fieldOptions, ExpanderOptions expanderOptions, AlvisIRQueryNodeExpander queryNodeExpander, List<AlvisIRAndQueryNode.Clause> clauses) {
			AlvisIRAndQueryNode queryNode = new AlvisIRAndQueryNode();
			ExpansionResult result = new ExpansionResult();
			for (AlvisIRAndQueryNode.Clause clause : clauses) {
				ExpansionResult r = queryNodeExpander.expandQuery(fieldOptions, expanderOptions, clause.getQueryNode());
				result.mergeExplanations(r);
				queryNode.addClause(clause.getOperator(), r.getQueryNode());
			}
			result.setQueryNode(queryNode);
			return result;
		}
	};
}
