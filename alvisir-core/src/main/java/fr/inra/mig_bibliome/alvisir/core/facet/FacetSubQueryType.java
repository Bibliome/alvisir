package fr.inra.mig_bibliome.alvisir.core.facet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.inra.mig_bibliome.alvisir.core.query.parser.QueryParserUtils;

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
