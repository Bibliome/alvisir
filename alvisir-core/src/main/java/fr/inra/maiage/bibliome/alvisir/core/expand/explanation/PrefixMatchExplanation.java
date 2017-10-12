package fr.inra.maiage.bibliome.alvisir.core.expand.explanation;

import java.util.Arrays;

import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRPrefixQueryNode;
import fr.inra.maiage.bibliome.alvisir.core.query.AlvisIRQueryNode;

/**
 * Match explanation for prefix queries.
 * @author rbossy
 *
 */
public class PrefixMatchExplanation extends MatchExplanation {
	private final String prefix;
	private final String normalizedPrefix;
	private String[] subpaths;
	private String[] subpathLabels;
	private int moreSubpaths = 0;
	
	public PrefixMatchExplanation(String fieldName, String prefix, String normalizedPrefix) {
		super(fieldName);
		this.prefix = prefix;
		this.normalizedPrefix = normalizedPrefix;
	}
	
	public PrefixMatchExplanation(AlvisIRPrefixQueryNode prefixQueryNode, String normalizedPrefix) {
		this(prefixQueryNode.getField(), prefixQueryNode.getPrefix(), normalizedPrefix);
	}

	@Override
	public AlvisIRQueryNode getQueryNode() {
		return new AlvisIRPrefixQueryNode(getFieldName(), normalizedPrefix);
	}

	@Override
	protected PayloadDecoder getPayloadDecoder() {
		return new MatchExplanationPayloadDecoder();
	}

	public String getPrefix() {
		return prefix;
	}

	public String getNormalizedPrefix() {
		return normalizedPrefix;
	}

	public boolean hasSubpaths() {
		return subpaths != null && subpathLabels != null;
	}
	
	public String[] getSubpaths() {
		return subpaths;
	}

	public String[] getSubpathLabels() {
		return subpathLabels;
	}

	public boolean hasMoreSubpaths() {
		return moreSubpaths > 0;
	}
	
	public int getMoreSubpaths() {
		return moreSubpaths;
	}

	public void setSubpaths(String[] subpaths, String[] labels) {
		if (subpaths.length != labels.length) {
			throw new IllegalArgumentException();
		}
		this.subpaths = subpaths;
		this.subpathLabels = labels;
	}

	public void setMoreSubpaths(int moreSubpaths) {
		this.moreSubpaths = moreSubpaths;
	}

	@Override
	public String toString() {
		return "PrefixMatchExplanation(" + prefix + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + moreSubpaths;
		result = prime * result + ((normalizedPrefix == null) ? 0 : normalizedPrefix.hashCode());
		result = prime * result + ((prefix == null) ? 0 : prefix.hashCode());
		result = prime * result + Arrays.hashCode(subpathLabels);
		result = prime * result + Arrays.hashCode(subpaths);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PrefixMatchExplanation other = (PrefixMatchExplanation) obj;
		if (moreSubpaths != other.moreSubpaths)
			return false;
		if (normalizedPrefix == null) {
			if (other.normalizedPrefix != null)
				return false;
		}
		else if (!normalizedPrefix.equals(other.normalizedPrefix))
			return false;
		if (prefix == null) {
			if (other.prefix != null)
				return false;
		}
		else if (!prefix.equals(other.prefix))
			return false;
		if (!Arrays.equals(subpathLabels, other.subpathLabels))
			return false;
		if (!Arrays.equals(subpaths, other.subpaths))
			return false;
		return true;
	}
}
