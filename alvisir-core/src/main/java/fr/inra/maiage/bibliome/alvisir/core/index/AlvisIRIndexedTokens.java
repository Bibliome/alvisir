package fr.inra.maiage.bibliome.alvisir.core.index;

import java.util.Iterator;
import java.util.Map;

/**
 * Token or annotation generator.
 * @author rbossy
 *
 * @param <T> Token object type.
 * @param <R> Fragment object type.
 */
public interface AlvisIRIndexedTokens<T,R> {
	/**
	 * Returns the tokens or annotations to be indexed.
	 * @return the tokens or annotations to be indexed.
	 */
	Iterator<T> getTokenInstances();
	
	/**
	 * Returns the fragment generator for the specified token.
	 * @param token token.
	 * @return the fragment generator for the specified token.
	 */
	AlvisIRTokenFragments<R> getTokenFragments(T token);
	
	/**
	 * Returns the text of the specified token.
	 * @param token token.
	 * @return the text of the specified token.
	 */
	String getTokenText(T token);
	
	int getTokenIdentifier(T token);
	
	Map<Integer,Integer> getRelationArguments(T token);
	
	Map<Integer,String> getProperties(T token);
}
