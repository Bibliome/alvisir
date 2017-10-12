package fr.inra.maiage.bibliome.alvisir.core.snippet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;

import org.apache.lucene.search.ScoreDoc;

import fr.inra.maiage.bibliome.alvisir.core.expand.explanation.MatchExplanation;
import fr.inra.maiage.bibliome.alvisir.core.facet.FacetCategory;
import fr.inra.maiage.bibliome.alvisir.core.facet.FacetSpecification;
import fr.inra.maiage.bibliome.util.defaultmap.DefaultMap;

/**
 * Snippet for a document.
 * @author rbossy
 *
 */
public class DocumentSnippet {
	private final int docId;
	private final float score;
	private final DefaultMap<FieldReference,FieldSnippet> fieldSnippets = new DefaultMap<FieldReference,FieldSnippet>(true, new TreeMap<FieldReference,FieldSnippet>()) {
		@Override
		protected FieldSnippet defaultValue(FieldReference key) {
			return new FieldSnippet(key);
		}
	};
	private final Collection<FacetCategory> facets = new ArrayList<FacetCategory>();
	
	private DocumentSnippet(int docId, float score) {
		super();
		this.docId = docId;
		this.score = score;
	}
	
	/**
	 * Creates a new document snippet with the specified document score.
	 * @param scoreDoc document score.
	 */
	public DocumentSnippet(ScoreDoc scoreDoc) {
		this(scoreDoc.doc, scoreDoc.score);
	}

	/**
	 * Returns the document identifier (lucene index identifier).
	 * @return the document identifier (lucene index identifier).
	 */
	public int getDocId() {
		return docId;
	}

	/**
	 * Returns the document score.
	 * @return the document score.
	 */
	public float getScore() {
		return score;
	}

	/**
	 * Returns the field snippets.
	 * @return the field snippets.
	 */
	public Collection<FieldSnippet> getFieldSnippets() {
		return Collections.unmodifiableCollection(fieldSnippets.values());
	}
	
	/**
	 * Returns the field snippet for the specified field reference, creates it if it does not exist.
	 * @param ref field reference.
	 * @return the field snippet for the specified field reference, creates it if it does not exist.
	 */
	public FieldSnippet ensureSnippet(FieldReference ref) {
		return fieldSnippets.safeGet(ref);
	}
	
	/**
	 * Returns the field snippet with the specified name and instance number, creates it if it does not exist.
	 * @param fieldName name of the field.
	 * @param fieldInstance instance number of the field.
	 * @return the field snippet with the specified name and instance number, creates it if it does not exist.
	 */
	public FieldSnippet ensureSnippet(String fieldName, int fieldInstance) {
		return ensureSnippet(new FieldReference(fieldName, fieldInstance));
	}
	
	/**
	 * Returns the field snippet for the specified field reference, null if it does not exist.
	 * @param ref field reference.
	 * @return the field snippet for the specified field reference, null if it does not exist.
	 */
	public FieldSnippet getSnippet(FieldReference ref) {
		return fieldSnippets.get(ref);
	}
	
	/**
	 * Returns the field snippet with the specified name and instance number, null if it does not exist.
	 * @param fieldName field name.
	 * @param fieldInstance field instance number.
	 * @return the field snippet with the specified name and instance number, null if it does not exist.
	 */
	public FieldSnippet getSnippet(String fieldName, int fieldInstance) {
		return getSnippet(new FieldReference(fieldName, fieldInstance));
	}

	/**
	 * Returns the facets for this document snippet.
	 * @return
	 */
	public Collection<FacetCategory> getFacets() {
		return Collections.unmodifiableCollection(facets);
	}
	
	/**
	 * Adds a facet with the specified facet specification to this document snippet.
	 * @param spec facet specification.
	 */
	public void addFacet(FacetSpecification spec) {
		facets.add(new FacetCategory(spec));
	}
	
	/**
	 * Removes field snippets that are NOT in the specified collection.
	 * @param retain field snippets to retain in this document snippet.
	 */
	public void retainFieldSnippets(Collection<FieldSnippet> retain) {
		Collection<FieldSnippet> fieldSnippets = this.fieldSnippets.values();
		fieldSnippets.retainAll(retain);
	}

	public Set<MatchExplanation> getAllMatchExplanations() {
		Set<MatchExplanation> result = new HashSet<MatchExplanation>();
		for (FieldSnippet field : fieldSnippets.values()) {
			for (Highlight h : field.getHighlights()) {
				result.add(h.getExplanation());
			}
		}
		return result;
	}
	
	public Highlight getHighlight(int id) {
		for (FieldSnippet f : getFieldSnippets()) {
			if (f.hasHighlight(id))
				return f.getHighlight(id);
		}
		return null;
	}

	public void retainFieldSnippetsWithHighlights() {
		Collection<FieldSnippet> fieldSnippets = this.fieldSnippets.values();
		Iterator<FieldSnippet> fieldIt = fieldSnippets.iterator();
		while (fieldIt.hasNext()) {
			FieldSnippet field = fieldIt.next();
			if ((!field.isMandatory()) && field.getHighlights().isEmpty()) {
//				System.err.println("REMOVE field = " + field);
				fieldIt.remove();
			}
		}
	}
}
