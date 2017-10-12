package fr.inra.maiage.bibliome.alvisir.core;


public enum ExampleCategory {
	TERM("term"),
	PHRASE("phrase"),
	PREFIX("prefix"),
	AND("and"),
	OR("or"),
	NOT("not"),
	GROUP("group"),
	FIELD("field"),
	NEAR("near"),
	RELATION("relation");

	public final String tag;
	
	private ExampleCategory(String tag) {
		this.tag = tag;
	}
	
	public static ExampleCategory getCategory(String tag) {
		for (ExampleCategory cat : values()) {
			if (cat.tag.equals(tag)) {
				return cat;
			}
		}
		return null;
	}
}
