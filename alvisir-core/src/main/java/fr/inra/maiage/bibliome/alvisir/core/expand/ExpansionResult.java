package fr.inra.maiage.bibliome.alvisir.core.expand;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.MatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRQueryNode;

/**
 * Objects of this class represent the result of a query expansion.
 * The expansion result contains the expanded query and a list of explanations.
 * @author rbossy
 *
 */
public class ExpansionResult {
	private AlvisIRQueryNode queryNode;
	private final List<MatchExplanation> explanations = new ArrayList<MatchExplanation>();

	/**
	 * Creates an expansion result.
	 */
	public ExpansionResult() {
		super();
	}
	
	/**
	 * Creates an expansion result with the specified query and explanation.
	 * @param queryNode
	 * @param explanation
	 */
	public ExpansionResult(AlvisIRQueryNode queryNode, MatchExplanation explanation) {
		this.queryNode = queryNode;
		explanations.add(explanation);
	}
	
	/**
	 * Returns all explanations for this expansion result.
	 * @return all explanations for this expansion result
	 */
	public List<MatchExplanation> getExplanations() {
		return Collections.unmodifiableList(explanations);
	}

	/**
	 * Returns the expanded query.
	 * @return the expanded query
	 */
	public AlvisIRQueryNode getQueryNode() {
		return queryNode;
	}

	/**
	 * Sets the expanded query.
	 * @param queryNode the expanded query
	 */
	public void setQueryNode(AlvisIRQueryNode queryNode) {
		this.queryNode = queryNode;
	}
	
	/**
	 * Adds the specified explanation to this expansion result.
	 * @param explanation
	 */
	public void addExplanation(MatchExplanation explanation) {
		explanations.add(explanation);
	}
	
	/**
	 * Adds all specified explanations to this expansion result.
	 * @param explanations
	 */
	public void addExplanations(Collection<MatchExplanation> explanations) {
		this.explanations.addAll(explanations);
	}
	
	/**
	 * Adds all explanations of the specified expansion result to this expansion result.
	 * This is equivalent to <code>addExplanations(result.explanations)</code>.
	 * @param result
	 */
	public void mergeExplanations(ExpansionResult result) {
		addExplanations(result.explanations);
	}
}
