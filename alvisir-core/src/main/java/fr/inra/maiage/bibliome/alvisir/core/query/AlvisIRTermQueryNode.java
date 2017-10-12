package fr.inra.maiage.bibliome.alvisir.core.query;

/**
 * Term query node.
 * @author rbossy
 *
 */
public class AlvisIRTermQueryNode implements AlvisIRQueryNode {
	private final String field;
	private final String text;
	
	/**
	 * Creates a term query node.
	 * @param field field name
	 * @param text term text
	 */
	public AlvisIRTermQueryNode(String field, String text) {
		super();
		this.field = field;
		this.text = text;
	}

	/**
	 * Returns the field name.
	 * @return
	 */
	public String getField() {
		return field;
	}

	/**
	 * Returns the term text.
	 * @return
	 */
	public String getText() {
		return text;
	}

	@Override
	public <R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X {
		return visitor.visit(this, param);
	}
}
