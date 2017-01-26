package fr.inra.mig_bibliome.alvisir.core.index;

import java.util.Iterator;

/**
 * Document generator.
 * @author rbossy
 *
 * @param <D> Document object type.
 * @param <F> Field object type.
 * @param <T> Token object type.
 * @param <R> Fragment object type.
 */
public interface AlvisIRIndexedDocuments<D,F,T,R> {
	/**
	 * Returns the documents to be indexed.
	 * @return the documents to be indexed.
	 */
	Iterator<D> getDocumentInstances();
	
	/**
	 * Returns the field generators for the specified document.
	 * @param doc document.
	 * @return
	 */
	Iterator<AlvisIRIndexedFields<F,T,R>> getIndexedFields(D doc);
}
