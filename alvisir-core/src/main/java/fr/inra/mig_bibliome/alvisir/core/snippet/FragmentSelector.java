package fr.inra.mig_bibliome.alvisir.core.snippet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import fr.inra.maiage.bibliome.util.fragments.DataFragment;
import fr.inra.maiage.bibliome.util.fragments.Fragment;
import fr.inra.maiage.bibliome.util.fragments.FragmentComparator;
import fr.inra.mig_bibliome.alvisir.core.expand.explanation.MatchExplanation;

/**
 * Selects fragments.
 * @author rbossy
 *
 */
public class FragmentSelector {
	/**
	 * Select fragments in the document snippets.
	 * Current strategy is greedy: retain the fragment containing the most diverse set of explanations.
	 * Iterate until there are no fragments left or no explanations left.
	 * @param docSnippet
	 */
	private static void selectFragments(DocumentSnippet docSnippet) {
		Collection<MatchExplanation> unexplained = getMatchExplanations(docSnippet);
		Map<Fragment,Collection<MatchExplanation>> fragmentExplains = getFragmentExplains(docSnippet);
		Collection<Fragment> retain = getMandatoryFragments(docSnippet);
		for (Fragment frag : retain) {
			setSeenFragment(frag, fragmentExplains, unexplained);
		}
		while (!unexplained.isEmpty()) {
			Map.Entry<Fragment,Collection<MatchExplanation>> explainsMost = getExplainsMost(fragmentExplains, unexplained);
			if (explainsMost == null) {
				break;
			}
			Fragment frag = explainsMost.getKey();
			retain.add(frag);
			setSeenFragment(frag, fragmentExplains, unexplained);
		}
		for (FieldSnippet fieldSnippet : docSnippet.getFieldSnippets()) {
			fieldSnippet.retainFragments(retain);
		}
	}

	private static void setSeenFragment(Fragment frag, Map<Fragment,Collection<MatchExplanation>> fragmentExplains, Collection<MatchExplanation> unexplained) {
		Collection<MatchExplanation> explained = fragmentExplains.get(frag);
		if (explained != null) {
			unexplained.removeAll(explained);
			fragmentExplains.remove(frag);
		}
	}
	
	/**
	 * Returns a map from fragments to match explanations of highlights contained in the key.
	 * @param docSnippet
	 * @return
	 */
	private static Map<Fragment,Collection<MatchExplanation>> getFragmentExplains(DocumentSnippet docSnippet) {
		Map<Fragment,Collection<MatchExplanation>> result = new HashMap<Fragment,Collection<MatchExplanation>>();
		Map<Fragment,Collection<Highlight>> fragmentHighlights = getFragmentHighlights(docSnippet);
		for (Map.Entry<Fragment,Collection<Highlight>> e : fragmentHighlights.entrySet()) {
			Collection<MatchExplanation> c = new HashSet<MatchExplanation>();
			for (Highlight h : e.getValue()) {
				c.add(h.getExplanation());
			}
			result.put(e.getKey(), c);
		}
		return result;
	}

	/**
	 * Returns a map from fragments to the highlights contained in the key.
	 * @param docSnippet
	 * @return
	 */
	private static Map<Fragment,Collection<Highlight>> getFragmentHighlights(DocumentSnippet docSnippet) {
		Map<Fragment,Collection<Highlight>> result = new HashMap<Fragment,Collection<Highlight>>();
		for (FieldSnippet fieldSnippet : docSnippet.getFieldSnippets()) {
			fillFragmentHighlights(result, fieldSnippet);
		}
		return result;
	}
	
	/**
	 * Returns a map of fragments to the contained highlights in the specified snippet.
	 * @param fieldSnippet field snippet.
	 * @return
	 */
	public static Map<Fragment,Collection<Highlight>> getFragmentHighlights(FieldSnippet fieldSnippet) {
		Map<Fragment,Collection<Highlight>> result = new HashMap<Fragment,Collection<Highlight>>();
		for (Fragment frag : fieldSnippet.getFragments()) {
			Collection<Highlight> highlights = new ArrayList<Highlight>();
			for (Highlight h : fieldSnippet.getHighlights()) {
				if (isInside(frag, h)) {
					highlights.add(h);
				}
			}
			result.put(frag, highlights);
		}
		return result;
	}
	
	/**
	 * Returns true if the specified highlight is fully inside the specified fragment.
	 * @param frag
	 * @param h
	 * @return
	 */
	private static boolean isInside(Fragment frag, Highlight h) {
		for (Fragment hf : h.getFragments()) {
			if (hf.getStart() < frag.getStart()) {
				return false;
			}
			if (hf.getEnd() > frag.getEnd()) {
				return false;
			}
		}
		return true;
	}

	private static void fillFragmentHighlights(Map<Fragment,Collection<Highlight>> fragmentExplains, FieldSnippet fieldSnippet) {
		Collection<Fragment> fragments = fieldSnippet.getFragments();
		if (fragments.isEmpty()) {
			return;
		}
		Collection<DataFragment<Highlight>> highlightFragments = getHighlightFragments(fieldSnippet);
		if (highlightFragments.isEmpty()) {
			return;
		}
		Iterator<Fragment> fragmentIt = fragments.iterator();
		Iterator<DataFragment<Highlight>> highlightIt = highlightFragments.iterator();
		Fragment fragment = fragmentIt.next();
		DataFragment<Highlight> highlight = highlightIt.next();
		Collection<Highlight> explains = new HashSet<Highlight>();
		while (true) {
			if (fragment.getStart() < highlight.getEnd() && highlight.getStart() < fragment.getEnd()) {
				explains.add(highlight.getValue());
			}
			if (highlight.getEnd() <= fragment.getEnd()) {
				if (highlightIt.hasNext()) {
					highlight = highlightIt.next();
				}
				else {
					if (!explains.isEmpty()) {
						fragmentExplains.put(fragment, explains);
					}
					break;
				}
			}
			if (fragment.getEnd() <= highlight.getEnd()) {
				if (!explains.isEmpty()) {
					fragmentExplains.put(fragment, explains);
				}
				if (fragmentIt.hasNext()) {
					fragment = fragmentIt.next();
					explains = new HashSet<Highlight>();
				}
				else {
					break;
				}
			}
		}
	}
	
	private static final Comparator<DataFragment<Highlight>> HIGHLIGHT_FRAGMENT_COMPARATOR = new FragmentComparator<DataFragment<Highlight>>() {
		@Override
		public int compare(DataFragment<Highlight> o1, DataFragment<Highlight> o2) {
			int r = super.compare(o1, o2);
			if (r == 0) {
				return o1.getValue().hashCode() - o2.getValue().hashCode();
			}
			return r;
		}
		
	};

	private static Collection<DataFragment<Highlight>> getHighlightFragments(FieldSnippet fieldSnippet) {
		Collection<DataFragment<Highlight>> result = new TreeSet<DataFragment<Highlight>>(HIGHLIGHT_FRAGMENT_COMPARATOR);
		for (Highlight h : fieldSnippet.getHighlights()) {
			for (Fragment frag : h.getFragments()) {
				DataFragment<Highlight> dataFrag = new DataFragment<Highlight>(frag.getStart(), frag.getEnd(), h);
				result.add(dataFrag);
			}
		}
		return result;
	}

	private static Map.Entry<Fragment,Collection<MatchExplanation>> getExplainsMost(Map<Fragment,Collection<MatchExplanation>> fragmentExplains, Collection<MatchExplanation> unexplained) {
		Map.Entry<Fragment,Collection<MatchExplanation>> result = null;
		int nbest = -1;
		for (Map.Entry<Fragment,Collection<MatchExplanation>> e : fragmentExplains.entrySet()) {
			Collection<MatchExplanation> explains = e.getValue();
			explains.retainAll(unexplained);
			int n = explains.size();
			if (n > nbest) {
				nbest = n;
				result = e;
			}
		}
		return result;
	}

	/**
	 * Returns all fragments in mandatory field snippets.
	 * @param docSnippet
	 * @return
	 */
	private static Collection<Fragment> getMandatoryFragments(DocumentSnippet docSnippet) {
		Collection<Fragment> result = new HashSet<Fragment>();
		for (FieldSnippet fieldSnippet : docSnippet.getFieldSnippets()) {
			if (fieldSnippet.isMandatory()) {
				result.addAll(fieldSnippet.getFragments());
			}
		}
		return result;
	}

	/**
	 * Returns all different match explanations of highlights present in this document snippet.
	 * @param docSnippet
	 * @return
	 */
	private static Set<MatchExplanation> getMatchExplanations(DocumentSnippet docSnippet) {
		Set<MatchExplanation> result = new HashSet<MatchExplanation>();
		for (FieldSnippet fieldSnippet : docSnippet.getFieldSnippets()) {
			for (Highlight h : fieldSnippet.getHighlights()) {
				result.add(h.getExplanation());
			}
		}
		return result;
	}
	
	/**
	 * Select field snippets.
	 * Current strategy is: keep field snippets that contain a fragment.
	 * @param docSnippet
	 */
	private static void selectFieldSnippets(DocumentSnippet docSnippet) {
		Collection<FieldSnippet> retain = new HashSet<FieldSnippet>();
		for (FieldSnippet fieldSnippet : docSnippet.getFieldSnippets()) {
			if (!fieldSnippet.getFragments().isEmpty()) {
				retain.add(fieldSnippet);
			}
		}
		docSnippet.retainFieldSnippets(retain);
	}
	
	/**
	 * Select snippets to be displayed.
	 * Current strategy is: retain highlights fully contained in a single fragment.
	 * @param docSnippet
	 */
	private static void selectHighlights(DocumentSnippet docSnippet) {
		Map<Fragment,Collection<Highlight>> fragmentHighlights = getFragmentHighlights(docSnippet);
		Collection<Highlight> retain = new HashSet<Highlight>();
		for (Collection<Highlight> c : fragmentHighlights.values()) {
			retain.addAll(c);
		}
		for (FieldSnippet fieldSnippet : docSnippet.getFieldSnippets()) {
			fieldSnippet.retainHighlights(retain);
		}
	}
	
	/**
	 * Removes some field snippets, fragments and highlights in the specified document snippets.
	 * Mandatory field snippets are not removed.
	 * After a call to this method, the document snippet contains the minimum number of fragments with the maximum number of different match explanations.
	 * @param docSnippets document snippets to clean.
	 */
	public static void cleanSnippets(DocumentSnippet[] docSnippets) {
		for (DocumentSnippet docSnippet : docSnippets) {
			selectFragments(docSnippet);
			selectFieldSnippets(docSnippet);
			selectHighlights(docSnippet);
		}
	}
}
