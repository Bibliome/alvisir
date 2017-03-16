package fr.inra.mig_bibliome.alvisir.core.expand;

import java.util.List;

import fr.inra.mig_bibliome.alvisir.core.FieldOptions;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRAndQueryNode.Clause;

/**
 * Objects of this class are responsible for the expansion of an AND query.
 * @author rbossy
 *
 */
public interface AndQueryNodeExpander {
	/**
	 * Expands the specified AND query.
	 * @param fieldOptions field options
	 * @param expanderOptions expander options
	 * @param queryNodeExpander expander for sub-queries
	 * @param clauses clauses of the AND query
	 * @return
	 */
	ExpansionResult expandAndClauses(FieldOptions fieldOptions, ExpanderOptions expanderOptions, AlvisIRQueryNodeExpander queryNodeExpander, List<Clause> clauses);
}
