package fr.inra.maiage.bibliome.alvisir.core.expand;

import fr.inra.maiage.bibliome.alvisir.core.facet.FacetSubQueryType;

public enum QueryNodeExpanderFactory {
	BASIC {
		@Override
		public AlvisIRQueryNodeExpander createAlvisIRQueryNodeExpander(TextExpander textExpander) {
			return new BasicQueryNodeExpander(textExpander);
		}

		@Override
		public FacetSubQueryType getDefaultQueryNodeExpanderFactory() {
			return FacetSubQueryType.PHRASE;
		}
	},
	
	ADVANCED {
		@Override
		public AlvisIRQueryNodeExpander createAlvisIRQueryNodeExpander(TextExpander textExpander) {
			return new AdvancedQueryNodeExpander(textExpander);
		}

		@Override
		public FacetSubQueryType getDefaultQueryNodeExpanderFactory() {
			return FacetSubQueryType.RAW;
		}
	};
	
	public abstract AlvisIRQueryNodeExpander createAlvisIRQueryNodeExpander(TextExpander textExpander);
	
	public abstract FacetSubQueryType getDefaultQueryNodeExpanderFactory();
}
