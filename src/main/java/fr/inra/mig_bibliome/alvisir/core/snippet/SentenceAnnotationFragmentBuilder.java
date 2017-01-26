package fr.inra.mig_bibliome.alvisir.core.snippet;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.lucene.search.spans.Spans;
import org.bibliome.util.fragments.Fragment;
import org.bibliome.util.fragments.SimpleFragment;

import fr.inra.mig_bibliome.alvisir.core.expand.explanation.PayloadDecoder;

/**
 * Fragment builder based on indexed annotations that represent sentences.
 * @author rbossy
 *
 */
public class SentenceAnnotationFragmentBuilder implements FragmentBuilder {
	private final IndexReader indexReader;
	private final String sentenceAnnotation;
	
	/**
	 * Creates a sentence annotation fragment builder.
	 * @param indexReader index containing the annotation sentences.
	 * @param sentenceAnnotation text of sentence annotation terms.
	 */
	public SentenceAnnotationFragmentBuilder(IndexReader indexReader, String sentenceAnnotation) {
		super();
		this.indexReader = indexReader;
		this.sentenceAnnotation = sentenceAnnotation;
	}

	@Override
	public void createFragments(DocumentSnippet doc, String fieldName) throws IOException {
		SentenceAnnotationFragmentBuilderPayloadDecoder decoder = new SentenceAnnotationFragmentBuilderPayloadDecoder();
		decoder.setFieldName(fieldName);
		Term sentenceTerm = new Term(fieldName, sentenceAnnotation);
		SpanQuery query = new SpanTermQuery(sentenceTerm);
		Spans spans = query.getSpans(indexReader);
		decoder.decode(new DocumentSnippet[] { doc }, spans);
	}
	
	private static final class SentenceAnnotationFragmentBuilderPayloadDecoder extends PayloadDecoder {
		private FieldSnippet fieldSnippet;

		@Override
		protected void handleArgument(int role, int argId) {
			// do nothing
		}

		@Override
		protected void handleMatch() {
			// do nothing	
		}

		@Override
		protected void handleProperty(int key, String value) {
			// do nothing
		}

		@Override
		protected void handleToken(int id, int fieldInstance) {
			fieldSnippet = getDocSnippet().getSnippet(getFieldName(), fieldInstance);
		}

		@Override
		protected void handleFragment(int fieldInstance, int start, int end) {
			if (fieldSnippet != null) {
				Fragment frag = new SimpleFragment(start, end);
				fieldSnippet.addFragment(frag);
			}
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((indexReader == null) ? 0 : indexReader.hashCode());
		result = prime * result + ((sentenceAnnotation == null) ? 0 : sentenceAnnotation.hashCode());
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
		SentenceAnnotationFragmentBuilder other = (SentenceAnnotationFragmentBuilder) obj;
		if (indexReader == null) {
			if (other.indexReader != null)
				return false;
		}
		else if (!indexReader.equals(other.indexReader))
			return false;
		if (sentenceAnnotation == null) {
			if (other.sentenceAnnotation != null)
				return false;
		}
		else if (!sentenceAnnotation.equals(other.sentenceAnnotation))
			return false;
		return true;
	}
}
