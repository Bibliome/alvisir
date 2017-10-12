package fr.inra.maiage.bibliome.alvisir.core.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Phrase query node.
 * @author rbossy
 *
 */
public class AlvisIRPhraseQueryNode implements AlvisIRQueryNode {
	private final String field;
	private final int slop;
	private final List<String> texts = new ArrayList<String>();
	
	/**
	 * Creates a phrase query node with the specified field and slop.
	 * @param field field name
	 * @param slop phrase slop.
	 */
	public AlvisIRPhraseQueryNode(String field, int slop) {
		super();
		this.field = field;
		this.slop = slop;
	}

	/**
	 * Returns the field name.
	 * @return
	 */
	public String getField() {
		return field;
	}

	/**
	 * Returns the term slop.
	 * @return
	 */
	public int getSlop() {
		return slop;
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

	@Override
	public <R,P,X extends Exception> R accept(AlvisIRQueryNodeVisitor<R,P,X> visitor, P param) throws X {
		return visitor.visit(this, param);
	}
}
