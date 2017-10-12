package fr.inra.maiage.bibliome.alvisir.core.query;

/**
 * Prefix query node.
 * @author rbossy
 *
 */
public class AlvisIRPrefixQueryNode implements AlvisIRQueryNode {
	private final String field;
	private final String prefix;
	
	/**
	 * Creates a prefix query node.
	 * @param field field name
	 * @param prefix term prefix
	 */
	public AlvisIRPrefixQueryNode(String field, String prefix) {
		super();
		this.field = field;
		this.prefix = prefix;
	}

	/**
	 * Returns the field name.
	 * @return
	 */
	public String getField() {
		return field;
	}

	/**
	 * Returns the term prefix.
	 * @return
	 */
	public String getPrefix() {
		return prefix;
	}

	@Override
	public <R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X {
		return visitor.visit(this, param);
	}
}
