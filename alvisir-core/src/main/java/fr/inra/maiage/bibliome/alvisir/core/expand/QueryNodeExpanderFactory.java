package fr.inra.maiage.bibliome.alvisir.core.expand;

public enum QueryNodeExpanderFactory {
	BASIC {
		@Override
		public AlvisIRQueryNodeExpander createAlvisIRQueryNodeExpander(TextExpander textExpander) {
			return new BasicQueryNodeExpander(textExpander);
		}
	},
	
	ADVANCED {
		@Override
		public AlvisIRQueryNodeExpander createAlvisIRQueryNodeExpander(TextExpander textExpander) {
			return new AdvancedQueryNodeExpander(textExpander);
		}
	};
	
	public abstract AlvisIRQueryNodeExpander createAlvisIRQueryNodeExpander(TextExpander textExpander);
}
