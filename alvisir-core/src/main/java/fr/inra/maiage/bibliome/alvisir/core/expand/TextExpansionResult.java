package fr.inra.maiage.bibliome.alvisir.core.expand;

import java.io.IOException;

/**
 * Objects of this class represent the results of the expansion of a text.
 * This is an iterator object that must be positioned at its creation.
 * A typical use would be:
 * <code>
 * TextExpansionResult ter = ...
 * if (ter.any()) {
 *   do {
 *      System.err.println(ter.getLabel());
 *   } while (ter.next());
 * }
 * </code>
 * @author rbossy
 *
 */
public interface TextExpansionResult {
	/**
	 * Returns either this result has at least one match.
	 * @return either this result has at least one match
	 */
	boolean any();
	
	/**
	 * Moves to the next match.
	 * @return true if there is a next match
	 * @throws IOException
	 * @throws ExpanderException
	 */
	boolean next() throws IOException, ExpanderException;
	
	/**
	 * Returns the expansion type of the current match.
	 * @return
	 */
	String getType();
	
	/**
	 * Returns the canonical text of the current match.
	 * @return
	 */
	String getCanonical();
	
	/**
	 * Returns all the synonyms of the current match.
	 * @return
	 */
	String[] getSynonyms();
	
	/**
	 * Returns the label of the current match.
	 * @return
	 */
	String getLabel();
}
