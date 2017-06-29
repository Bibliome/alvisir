package fr.inra.mig_bibliome.alvisir.core.expand;

import fr.inra.mig_bibliome.alvisir.core.FieldOptions;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNode;

public abstract class AlvisIRQueryNodeExpander {
	protected final TextExpander textExpander;

	protected AlvisIRQueryNodeExpander(TextExpander textExpander) {
		super();
		this.textExpander = textExpander;
	}

	public abstract ExpansionResult expandQuery(FieldOptions fieldOptions, ExpanderOptions expanderOptions, AlvisIRQueryNode originalQuery);
}
