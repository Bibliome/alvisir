package fr.inra.maiage.bibliome.alvisir.core.snippet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import fr.inra.maiage.bibliome.util.fragments.Fragment;
import fr.inra.maiage.bibliome.util.fragments.FragmentCollection;
import fr.inra.maiage.bibliome.util.fragments.SimpleMutableFragment;

/**
 * Snippet for a field.
 * @author rbossy
 *
 */
public class FieldSnippet {
	private final FieldReference fieldReference;
	private final FragmentCollection<Fragment> fragments = new FragmentCollection<Fragment>();
	private final Map<Integer,Highlight> highlights = new HashMap<Integer,Highlight>();
	private String fieldValue;
	private boolean mandatory;
	
	FieldSnippet(FieldReference fieldReference) {
		super();
		this.fieldReference = fieldReference;
	}
	
	/**
	 * Returns the field reference of this field snippet.
	 * @return the field reference of this field snippet.
	 */
	public FieldReference getFieldReference() {
		return fieldReference;
	}
	
	/**
	 * Returns the field name of this field snippet.
	 * @return the field name of this field snippet.
	 */
	public String getFieldName() {
		return fieldReference.getFieldName();
	}
	
	/**
	 * Returns the field instance number of this field snippet.
	 * @return the field instance number of this field snippet.
	 */
	public int getFieldInstance() {
		return fieldReference.getFieldInstance();
	}
	
	/**
	 * Returns the displayed fragments.
	 * @return the displayed fragments.
	 */
	public Collection<Fragment> getFragments() {
		return Collections.unmodifiableCollection(fragments);
	}
	
	/**
	 * Remove fragments that are NOT in the specified collection.
	 * @param retain fragments to retain in this field snippet.
	 */
	public void retainFragments(Collection<Fragment> retain) {
		fragments.retainAll(retain);
	}
	
	/**
	 * Adds the specified fragment.
	 * @param fragment fragment.
	 */
	public void addFragment(Fragment fragment) {
		fragments.add(fragment);
	}
	
	/**
	 * Returns the highlighted text in this field snippet.
	 * @return the highlighted text in this field snippet.
	 */
	public Collection<Highlight> getHighlights() {
		return Collections.unmodifiableCollection(highlights.values());
	}
	
	public boolean hasHighlight(int id) {
		return highlights.containsKey(id);
	}
	
	public Highlight getHighlight(int id) {
		if (!hasHighlight(id)) {
			throw new RuntimeException("unknown highlight id: " + id);
		}
		return highlights.get(id);
	}
	
	/**
	 * Adds the specified highlight.
	 * @param highlight highlight.
	 */
	public void addHighlight(Highlight highlight) {
		highlights.put(highlight.getIdentifier(), highlight);
	}
	
	/**
	 * Removes highlights that are NOT in the specified collection.
	 * @param retain hilights to retain in this field snippet.
	 */
	public void retainHighlights(Collection<Highlight> retain) {
		Collection<Highlight> highlights = this.highlights.values();
		highlights.retainAll(retain);
	}

	/**
	 * Returns the field contents value.
	 * @return the field contents value.
	 */
	public String getFieldValue() {
		return fieldValue;
	}

	/**
	 * Returns either this field snippet is mandatory.
	 * @return either this field snippet is mandatory.
	 */
	public boolean isMandatory() {
		return mandatory;
	}

	/**
	 * Sets the field contents value.
	 * @param fieldValue contents value.
	 */
	public void setFieldValue(String fieldValue) {
		this.fieldValue = fieldValue;
	}

	/**
	 * Sets either this field snippet is mandatory.
	 * @param mandatory mandatory status.
	 */
	public void setMandatory(boolean mandatory) {
		this.mandatory = mandatory;
	}

	@Override
	public String toString() {
		return "field snippet: " + fieldReference + (mandatory ? " [mandatory]" : "");
	}
	
	public void retainFragmentsWithSelectedHighlights() {
		if (mandatory) {
			return;
		}
		FragmentCollection<Fragment> selectedFragments = getSelectedHighlightFragments();
		Collection<Fragment> newFragments = new ArrayList<Fragment>();
		SimpleMutableFragment current = null;
		for (Fragment frag : fragments) {
			if (selectedFragments.someContained(frag)) {
				if (current == null) {
					current = new SimpleMutableFragment(frag.getStart(), frag.getEnd());
					newFragments.add(current);
				}
				else {
					current.setStart(Math.min(current.getStart(), frag.getStart()));
					current.setEnd(Math.max(current.getEnd(), frag.getEnd()));
				}
			}
			else {
				current = null;
			}
		}
		fragments.clear();
		fragments.addAll(newFragments);
	}
	
	private FragmentCollection<Fragment> getSelectedHighlightFragments() {
		FragmentCollection<Fragment> result = new FragmentCollection<Fragment>();
		for (Highlight h : getHighlights()) {
			if (h.isSelected()) {
				result.addAll(h.getFragments());
			}
		}
		return result;
	}
	
	public void retainHighlightsInFragments() {
		Collection<Highlight> retained = new HashSet<Highlight>();
		for (Highlight h : getHighlights()) {
			if (inFragment(h)) {
				retained.add(h);
			}
		}
		Collection<Highlight> highlights = this.highlights.values();
		highlights.retainAll(retained);
	}

	private boolean inFragment(Highlight h) {
		for (Fragment f : h.getFragments()) {
			if (fragments.someContains(f)) {
				return true;
			}
		}
		return false;
	}
}
