package fr.inra.mig_bibliome.alvisir.core.index;

import java.util.Collection;

/**
 * Fragment generator.
 * @author rbossy
 *
 * @param <R> Fragment object type.
 */
public interface AlvisIRTokenFragments<R> {
	/**
	 * Returns the fragments.
	 * @return the fragments.
	 */
	Collection<R> getTokenFragments();
	
	/**
	 * Returns the start offset of the specified fragment.
	 * @param fragment fragment.
	 * @return the start offset of the specified fragment.
	 */
	int getStart(R fragment);
	
	/**
	 * Returns the end offset of the specified fragment.
	 * @param fragment fragment.
	 * @return the end offset of the specified fragment.
	 */
	int getEnd(R fragment);
}
