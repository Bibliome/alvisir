package fr.inra.mig_bibliome.alvisir.core.snippet;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

import org.bibliome.util.fragments.DataFragment;
import org.bibliome.util.fragments.Fragment;
import org.bibliome.util.fragments.FragmentComparator;
import org.bibliome.util.fragments.Fragments;
import org.bibliome.util.fragments.SimpleFragment;

import fr.inra.mig_bibliome.alvisir.core.expand.explanation.MatchExplanation;

/**
 * A match highlight in a field snippet.
 * @author rbossy
 *
 */
public class Highlight {
	private final MatchExplanation explanation;
	private final NavigableSet<Fragment> fragments = new TreeSet<Fragment>(new FragmentComparator<Fragment>());
	private final Map<Integer,Integer> arguments = new LinkedHashMap<Integer,Integer>();
	private final Map<Integer,String> properties = new LinkedHashMap<Integer,String>();
	private final int identifier;
	private boolean selected = false;

	/**
	 * Creates a match highlight with the specified explanation. 
	 * @param explanation match explanation.
	 */
	public Highlight(int identifier, MatchExplanation explanation) {
		super();
		this.identifier = identifier;
		this.explanation = explanation;
	}
	
	public Highlight(MatchExplanation explanation) {
		super();
		this.identifier = -System.identityHashCode(this);
		this.explanation = explanation;
	}

	/**
	 * Returns this highlight explanation.
	 * @return this highlight explanation.
	 */
	public MatchExplanation getExplanation() {
		return explanation;
	}

	/**
	 * Returns this highlight offsets.
	 * @return this highlight offsets.
	 */
	public Collection<Fragment> getFragments() {
		return Collections.unmodifiableCollection(fragments);
	}
	
	/**
	 * Adds the specified offsets in this highlight.
	 * @param fragment offsets.
	 */
	public void addFragment(Fragment fragment) {
		fragments.add(fragment);
	}

	void fillHighlightFragments(Collection<DataFragment<Highlight>> highlightFragments) {
		for (Fragment f : fragments) {
			highlightFragments.add(new DataFragment<Highlight>(f.getStart(), f.getEnd(), this));
		}
	}
	
	void cropFragments(Fragment fragment) {
		if (fragments.isEmpty()) {
			return;
		}
		for (Fragment f = fragments.first(); f != null; f = fragments.higher(f)) {
			if (Fragments.includes(fragment, f)) {
				continue;
			}
			fragments.remove(f);
			if (!Fragments.overlaps(fragment, f)) {
				continue;
			}
			f = new SimpleFragment(Math.max(fragment.getStart(), f.getStart()), Math.min(fragment.getEnd(), f.getEnd()));
			fragments.add(f);
		}
	}

	public int getIdentifier() {
		return identifier;
	}
	
	public void addArgument(int role, int argId) {
		arguments.put(role, argId);
	}

	public void addProperty(int key, String value) {
		properties.put(key, value);
	}
	
	public boolean isRelation() {
		return !arguments.isEmpty();
	}
	
	public boolean hasProperties() {
		return !properties.isEmpty();
	}
	
	public Map<Integer,Integer> getArguments() {
		return Collections.unmodifiableMap(arguments);
	}
	
	public Map<Integer,String> getProperties() {
		return Collections.unmodifiableMap(properties);
	}

	public Map<String,Highlight> getArguments(FieldSnippet field, String[] roleNames) {
		Map<String,Highlight> result = new LinkedHashMap<String,Highlight>();
		for (Map.Entry<Integer,Integer> e : arguments.entrySet()) {
			int roleId = e.getKey();
			String role = getRole(roleNames, roleId);
			int hId = e.getValue();
			Highlight h = field.getHighlight(hId);
			result.put(role, h);
		}
		return result;
	}

	public Map<String,CharSequence> getProperties(String[] propertyKeys) {
		Map<String,CharSequence> result = new LinkedHashMap<String,CharSequence>();
		for (Map.Entry<Integer,String> e : properties.entrySet()) {
			int keyId = e.getKey();
			String key = getRole(propertyKeys, keyId);
			CharSequence value = e.getValue();
			result.put(key, value);
		}
		return result;
	}
	
	private static String getRole(String[] roleNames, int roleId) {
		if (roleId < 0 || roleId >= roleNames.length) {
			return Integer.toString(roleId);
		}
		return roleNames[roleId];
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Highlight:");
		sb.append(identifier);
		if (selected) {
			sb.append('*');
		}
		sb.append(" [");
		boolean notFirst = false;
		for (Fragment f : fragments) {
			if (notFirst)
				sb.append(',');
			else
				notFirst = true;
			sb.append(f.getStart());
			sb.append('-');
			sb.append(f.getEnd());
		}
		sb.append("] ");
		sb.append(explanation);
		return sb.toString();
	}
}
