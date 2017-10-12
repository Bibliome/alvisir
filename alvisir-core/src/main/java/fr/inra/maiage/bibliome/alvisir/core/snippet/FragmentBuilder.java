package fr.inra.maiage.bibliome.alvisir.core.snippet;

import java.io.IOException;

/**
 * Objects of this interface create displayed fragments in field snippets.
 * @author rbossy
 *
 */
public interface FragmentBuilder {
	void createFragments(DocumentSnippet doc, String fieldName) throws IOException;
}
