package fr.inra.maiage.bibliome.alvisir.core.facet;

import fr.inra.maiage.bibliome.alvisir.core.query.parser.QueryParserUtils;

public enum FacetSubQueryType {
	TERM {
		@Override
		public String getSubQuery(String field, String text) {
			return field + "=" + QueryParserUtils.quoteTerm(text);
		}

		@Override
		public String toString() {
			return "term";
		}
	},
	
	RAW {
		@Override
		public String getSubQuery(String field, String text) {
			return field + "=(" + QueryParserUtils.quotePhrase(text) + ")";
		}
	},
	
	PHRASE {
		@Override
		public String getSubQuery(String field, String text) {
			return field + "=\"" + QueryParserUtils.quotePhrase(text) + "\"";
		}

		@Override
		public String toString() {
			return "phrase";
		}
	},
	
	PREFIX {
		@Override
		public String getSubQuery(String field, String text) {
			return field + "=" + QueryParserUtils.quoteTerm(text) + "*";
		}

		@Override
		public String toString() {
			return "prefix";
		}
	};

	public abstract String getSubQuery(String field, String text);
}
