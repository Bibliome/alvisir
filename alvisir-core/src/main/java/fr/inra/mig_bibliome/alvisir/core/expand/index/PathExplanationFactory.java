package fr.inra.mig_bibliome.alvisir.core.expand.index;

import java.io.IOException;
import java.util.Iterator;
import java.util.NavigableSet;
import java.util.TreeSet;

import fr.inra.mig_bibliome.alvisir.core.expand.ExpanderException;
import fr.inra.mig_bibliome.alvisir.core.expand.ExpanderOptions;
import fr.inra.mig_bibliome.alvisir.core.expand.ExplanationFactory;
import fr.inra.mig_bibliome.alvisir.core.expand.TextExpander;
import fr.inra.mig_bibliome.alvisir.core.expand.TextExpansionResult;
import fr.inra.mig_bibliome.alvisir.core.expand.explanation.MatchExplanation;
import fr.inra.mig_bibliome.alvisir.core.expand.explanation.PrefixMatchExplanation;
import fr.inra.mig_bibliome.alvisir.core.index.NormalizationOptions;

/**
 * Explanation factory for path searches.
 * This factory creates PrefixQueryMatchExplanation objects.
 * @author rbossy
 *
 */
class PathExplanationFactory extends ExplanationFactory {
	private final IndexBasedTextExpander expander;
	private final char separator;
	private int maxSubpaths = Integer.MAX_VALUE;
	private int maxDepth = -1;
	
	PathExplanationFactory(IndexBasedTextExpander expander, String type, char separator) {
		super(type);
		this.expander = expander;
		this.separator = separator;
	}
	
	private static final class SubpathBit implements Comparable<SubpathBit> {
		private final String subPath;
		private final String label;
		
		private SubpathBit(TextExpansionResult textExpansionResult) {
			subPath = textExpansionResult.getCanonical();
			label = textExpansionResult.getLabel();
		}

		@Override
		public int compareTo(SubpathBit o) {
			int pr = Integer.compare(subPath.length(), o.subPath.length());
			if (pr != 0) {
				return pr;
			}
			return label.compareTo(o.label);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((label == null) ? 0 : label.hashCode());
			result = prime * result + ((subPath == null) ? 0 : subPath.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof SubpathBit))
				return false;
			SubpathBit other = (SubpathBit) obj;
			if (label == null) {
				if (other.label != null)
					return false;
			} else if (!label.equals(other.label))
				return false;
			if (subPath == null) {
				if (other.subPath != null)
					return false;
			} else if (!subPath.equals(other.subPath))
				return false;
			return true;
		}
	}

	@Override
	protected MatchExplanation doGetExplanation(ExpanderOptions expanderOptions, TextExpander textExpander, String fieldName, String canonicalText) throws IOException, ExpanderException {
		NormalizationOptions normalizationOptions = expanderOptions.getNormalizationOptions(fieldName);
		String normalizedText = normalizationOptions.normalize(canonicalText);
		PrefixMatchExplanation result = new PrefixMatchExplanation(fieldName, canonicalText, normalizedText);
		TextExpansionResult textExpansionResult;
		if (maxDepth > 0) {
			textExpansionResult = expander.searchLimitedDepthSubPaths(normalizedText, maxDepth, separator);
		}
		else {
			textExpansionResult = expander.searchSubPaths(normalizedText);
		}
		NavigableSet<SubpathBit> subPathBits = new TreeSet<SubpathBit>();
		if (textExpansionResult.any()) {
			do {
				if (!canonicalText.equals(textExpansionResult.getCanonical())) {
					SubpathBit subPathBit = new SubpathBit(textExpansionResult);
					subPathBits.add(subPathBit);
					while (subPathBits.size() > maxSubpaths) {
						subPathBits.pollLast();
					}
				}
			} while (textExpansionResult.next());
		}
		int len = subPathBits.size();
		String[] subpaths = new String[len];
		String[] labels = new String[len];
		Iterator<SubpathBit> spbit = subPathBits.iterator();
		for (int i = 0; i < len; ++i) {
			SubpathBit subPathBit = spbit.next();
			subpaths[i] = subPathBit.subPath;
			labels[i] = subPathBit.label;
		}
		result.setSubpaths(subpaths, labels);
		return result;
	}

	@Override
	protected void doSetProperty(String key, String value) throws ExpanderIndexerException {
		switch (key) {
			case "max-sub-paths":
				maxSubpaths = Integer.parseInt(value);
				break;
			case "max-depth":
				maxDepth = Integer.parseInt(value);
				break;
			default:
				throw new ExpanderIndexerException("unhandled property: " + key);
		}
	}
}
