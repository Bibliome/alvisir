package fr.inra.maiage.bibliome.alvisir.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Boolean conjunction query node where all operands are term queries on the same field.
 * @author rbossy
 *
 */
public class AlvisIRTermListQueryNode implements AlvisIRQueryNode {
	private final AlvisIRAndQueryNode.Operator operator;
	private final String field;
	private final List<String> texts = new ArrayList<String>();
	
	/**
	 * Creates a phrase query node with the specified field and slop.
	 * @param field field name
	 * @param slop phrase slop.
	 */
	public AlvisIRTermListQueryNode(AlvisIRAndQueryNode.Operator operator, String field) {
		super();
		this.operator = operator;
		this.field = field;
	}

	public AlvisIRAndQueryNode.Operator getOperator() {
		return operator;
	}

	/**
	 * Returns the field name.
	 * @return
	 */
	public String getField() {
		return field;
	}

	/**
	 * Returns the texts of terms in the phrase.
	 * @return
	 */
	public List<String> getTexts() {
		return Collections.unmodifiableList(texts);
	}
	
	/**
	 * Appends a term text to this phrase query node.
	 * @param text
	 */
	public void addText(String text) {
		texts.add(text);
	}
	
	public AlvisIRAndQueryNode toAndQueryNode() {
		AlvisIRAndQueryNode result = new AlvisIRAndQueryNode();
		for (String t : texts) {
			result.addClause(operator, new AlvisIRTermQueryNode(field, t));
		}
		return result;
	}

	@Override
	public <R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X {
		return visitor.visit(this, param);
	}
}
