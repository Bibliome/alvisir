package fr.inra.mig_bibliome.alvisir.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Boolean conjunction query node.
 * @author rbossy
 *
 */
public class AlvisIRAndQueryNode implements AlvisIRQueryNode {
	private final List<Clause> clauses = new ArrayList<Clause>();
	
	/**
	 * Creates a new conjunction query node.
	 */
	public AlvisIRAndQueryNode() {
		super();
	}

	@Override
	public <R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X {
		return visitor.visit(this, param);
	}
	
	/**
	 * Returns all clauses.
	 * @return all clauses (unmodifiable).
	 */
	public List<Clause> getClauses() {
		return Collections.unmodifiableList(clauses);
	}
	
	/**
	 * Adds a new clause.
	 * @param op operator
	 * @param qn query node
	 */
	public void addClause(Operator op, AlvisIRQueryNode qn) {
		clauses.add(new Clause(op, qn));
	}

	/**
	 * Conjunction operator.
	 * @author rbossy
	 *
	 */
	public static enum Operator {
		/** Regular AND */
		AND,
		
		/** AND NOT */
		BUT;
	}
	
	/**
	 * Conjunction clause.
	 * @author rbossy
	 *
	 */
	public static final class Clause {
		private final Operator operator;
		private final AlvisIRQueryNode queryNode;
		
		private Clause(Operator operator, AlvisIRQueryNode queryNode) {
			super();
			this.operator = operator;
			this.queryNode = queryNode;
		}

		/**
		 * Returns this clause operator.
		 * @return
		 */
		public Operator getOperator() {
			return operator;
		}

		/**
		 * Returns this clause query node.
		 * @return
		 */
		public AlvisIRQueryNode getQueryNode() {
			return queryNode;
		}
	}
}
