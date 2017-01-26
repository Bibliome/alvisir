package fr.inra.mig_bibliome.alvisir.core.snippet;

import org.bibliome.util.fragments.Fragment;
import org.bibliome.util.fragments.SimpleFragment;

/**
 * Standard fragment builders.
 * @author rbossy
 *
 */
public enum StandardFragmentBuilder implements FragmentBuilder {
	/**
	 * Fragment builder that does not create any fragment at all.
	 */
	SILENT {
		@Override
		protected void createFragments(FieldSnippet fieldSnippet) {
		}

		@Override
		public void createFragments(DocumentSnippet doc, String fieldName) {
		}
	},

	/**
	 * Fragment builder that creates a fragment that covers the whole field conents.
	 */
	WHOLE {
		@Override
		protected void createFragments(FieldSnippet fieldSnippet) {
			if (fieldSnippet.isMandatory() || !fieldSnippet.getHighlights().isEmpty()) {
				int fragmentSize = getFragmentSize(fieldSnippet);
				Fragment frag = new SimpleFragment(0, fragmentSize);
				fieldSnippet.addFragment(frag);
			}
		}
		
		private int getFragmentSize(FieldSnippet fieldSnippet) {
			String fieldValue = fieldSnippet.getFieldValue();
			if (fieldValue == null) {
				return Integer.MAX_VALUE;
			}
			return fieldValue.length();
		}

		@Override
		public void createFragments(DocumentSnippet doc, String fieldName) {
			for (FieldSnippet fieldSnippet : doc.getFieldSnippets()) {
				if (fieldSnippet.getFieldName().equals(fieldName)) {
					Fragment frag = new SimpleFragment(0, Integer.MAX_VALUE);
					fieldSnippet.addFragment(frag);
				}
			}
		}
	};
	
	/**
	 * Creates fragments in the specified field snippet.
	 * @param fieldSnippet field snippet.
	 */
	protected abstract void createFragments(FieldSnippet fieldSnippet);
}
