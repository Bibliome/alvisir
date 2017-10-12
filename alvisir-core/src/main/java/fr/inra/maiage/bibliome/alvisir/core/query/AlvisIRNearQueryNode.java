package fr.inra.maiage.bibliome.alvisir.core.query;

/**
 * Ordered proximity query node.
 * @author rbossy
 *
 */
public class AlvisIRNearQueryNode implements AlvisIRQueryNode {
	private final String field;
	private final int slop;
	private final AlvisIRQueryNode left;
	private final AlvisIRQueryNode right;

	/**
	 * Creates a proximity query node.
	 * @param field field name
	 * @param slop proximity slop
	 * @param left left query node
	 * @param right right query node
	 */
	public AlvisIRNearQueryNode(String field, int slop, AlvisIRQueryNode left, AlvisIRQueryNode right) {
		super();
		this.field = field;
		this.slop = slop;
		this.left = left;
		this.right = right;
	}

	/**
	 * Returns the field name of this query node.
	 * @return
	 */
	public String getField() {
		return field;
	}

	/**
	 * Returns the proximity slop.
	 * @return
	 */
	public int getSlop() {
		return slop;
	}
	
	/**
	 * Returns the left query node.
	 * @return
	 */
	public AlvisIRQueryNode getLeft() {
		return left;
	}

	/**
	 * Returns the right query node.
	 * @return
	 */
	public AlvisIRQueryNode getRight() {
		return right;
	}

	@Override
	public <R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X {
		return visitor.visit(this, param);
	}
}
