package fr.inra.mig_bibliome.alvisir.core;

public final class Example {
	private final ExampleCategory category;
	private final String queryString;
	
	public Example(ExampleCategory category, String queryString) {
		super();
		this.category = category;
		this.queryString = queryString;
	}

	public ExampleCategory getCategory() {
		return category;
	}

	public String getQueryString() {
		return queryString;
	}
}