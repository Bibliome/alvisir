package fr.inra.mig_bibliome.alvisir.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Disjunction query node.
 * @author rbossy
 *
 */
public class AlvisIROrQueryNode implements AlvisIRQueryNode {
	private final List<AlvisIRQueryNode> clauses = new ArrayList<AlvisIRQueryNode>();
	
	/**
	 * Creates a disjunction query node.
	 */
	public AlvisIROrQueryNode() {
		super();
	}

	/**
	 * Returns all clauses.
	 * @return
	 */
	public List<AlvisIRQueryNode> getClauses() {
		return Collections.unmodifiableList(clauses);
	}

	/**
	 * Adds a clause.
	 * @param qn
	 */
	public void addClause(AlvisIRQueryNode qn) {
		clauses.add(qn);
	}
	
	@Override
	public <R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X {
		return visitor.visit(this, param);
	}
}
