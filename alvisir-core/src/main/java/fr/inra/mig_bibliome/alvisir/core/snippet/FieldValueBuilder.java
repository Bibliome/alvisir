package fr.inra.mig_bibliome.alvisir.core.snippet;

import java.io.IOException;
import java.util.Set;

/**
 * Objects of this interface retrieves field contents values for specified document snippets.
 * @author rbossy
 *
 */
public interface FieldValueBuilder {
	/**
	 * Retrieves field contents values for field snippets.
	 * @param docSnippet document snippets.
	 * @throws IOException
	 */
	void fetchFieldValues(DocumentSnippet docSnippet) throws IOException;
	void fetchMandatoryFieldValues(Set<String> mandatoryFields, DocumentSnippet docSnippet) throws IOException;
}
