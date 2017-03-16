package fr.inra.mig_bibliome.alvisir.core.expand.explanation;

import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;
import org.bibliome.util.fragments.Fragment;
import org.bibliome.util.fragments.SimpleFragment;
import org.bibliome.util.fragments.SimpleMutableFragment;

import fr.inra.mig_bibliome.alvisir.core.AlvisIRIndex;
import fr.inra.mig_bibliome.alvisir.core.FieldOptions;
import fr.inra.mig_bibliome.alvisir.core.SearchConfig;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNode;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNodeQueryConverter;
import fr.inra.mig_bibliome.alvisir.core.query.AlvisIRQueryNodeSpanQueryConverter;
import fr.inra.mig_bibliome.alvisir.core.snippet.DocumentSnippet;
import fr.inra.mig_bibliome.alvisir.core.snippet.FieldSnippet;
import fr.inra.mig_bibliome.alvisir.core.snippet.Highlight;

/**
 * Match explanation.
 * @author rbossy
 *
 */
public abstract class MatchExplanation {
	/**
	 * Element containing explanations for this match.
	 */
	private final String fieldName;
	private String type;
	private String label;
	private String[] synonyms;
	private int moreSynonyms = 0;
	private int productivity = 0;

	/**
	 * Creates a match explanation.
	 * @param doc documents to which belong explanation bits.
	 * @param fieldName name of the field for this match.
	 */
	protected MatchExplanation(String fieldName) {
		super();
		this.fieldName = fieldName;
	}
	
	public SpanQuery getSpanQuery(int tokenPositionGap, String fieldName) {
		return AlvisIRQueryNodeSpanQueryConverter.convert(tokenPositionGap, fieldName, getQueryNode());
	}

	protected abstract PayloadDecoder getPayloadDecoder();
	
	/**
	 * Creates highlights objects in the specified document snippets.
	 * @param tokenPositionGap
	 * @param reader index containing the target documents.
	 * @param docSnippets document snippets.
	 * @throws IOException
	 */
	public void createHighlights(AlvisIRIndex index, FieldOptions fieldOptions, DocumentSnippet[] docSnippets) throws IOException {
		PayloadDecoder decoder = getPayloadDecoder();
		for (String f : fieldOptions.getFields(fieldName)) {
			SpanQuery query = getSpanQuery(index.getGlobalAttributes().getTokenPositionGap(), f);
			IndexReader reader = index.getIndexReader();
			SpanQuery rewritten = (SpanQuery) query.rewrite(reader);
			Spans spans = rewritten.getSpans(reader);
			decoder.setFieldName(f);
			decoder.decode(docSnippets, spans);
		}
	}
	
	/**
	 * A payload decoder that creates highlights.
	 * @author rbossy
	 *
	 */
	protected final class MatchExplanationPayloadDecoder extends PayloadDecoder {
		private Highlight highlight;
		
		protected MatchExplanationPayloadDecoder() {
			super();
		}

		@Override
		protected void handleMatch() {
		}

		@Override
		protected void handleToken(int id, int fieldInstance) {
			highlight = new Highlight(id, MatchExplanation.this);
			FieldSnippet fieldSnippet = getDocSnippet().ensureSnippet(getFieldName(), fieldInstance);
			fieldSnippet.addHighlight(highlight);
		}

		@Override
		protected void handleFragment(int fieldInstance, int start, int end) {
			Fragment fragment = new SimpleFragment(start, end);
			highlight.addFragment(fragment);
		}

		@Override
		protected void handleArgument(int role, int argId) {
			highlight.addArgument(role, argId);
		}

		@Override
		protected void handleProperty(int key, String value) {
			highlight.addProperty(key, value);
		}
	}

	protected final class MergedHighlightMatchExplanationPayloadDecoder extends PayloadDecoder {
		private Highlight highlight;
		private SimpleMutableFragment fragment;
		private int fieldInstance = -1;
		
		protected MergedHighlightMatchExplanationPayloadDecoder() {
			super();
		}

		@Override
		protected void handleMatch() {
			fieldInstance = -1;
		}

		@Override
		protected void handleToken(int id, int fieldInstance) {
			if (fieldInstance != this.fieldInstance) {
				this.fieldInstance = fieldInstance;
				highlight = new Highlight(MatchExplanation.this);
				fragment = new SimpleMutableFragment(Integer.MAX_VALUE, 0);
				highlight.addFragment(fragment);
				FieldSnippet fieldSnippet = getDocSnippet().ensureSnippet(getFieldName(), fieldInstance);
				fieldSnippet.addHighlight(highlight);
			}
		}

		@Override
		protected void handleFragment(int fieldInstance, int start, int end) {
			if (start < fragment.getStart()) {
				fragment.setStart(start);
			}
			if (end > fragment.getEnd()) {
				fragment.setEnd(end);
			}
		}

		@Override
		protected void handleArgument(int role, int argId) {
			highlight.addArgument(role, argId);
		}

		@Override
		protected void handleProperty(int key, String value) {
			highlight.addProperty(key, value);
		}
	}
	
	public abstract AlvisIRQueryNode getQueryNode();

	public String getFieldName() {
		return fieldName;
	}

	public boolean hasType() {
		return type != null;
	}
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	public boolean hasLabel() {
		return label != null;
	}

	public String getLabel() {
		return label;
	}
	
	public boolean hasSynonyms() {
		return synonyms != null;
	}

	public String[] getSynonyms() {
		return synonyms;
	}
	
	public boolean hasMoreSynonyms() {
		return moreSynonyms > 0;
	}

	public int getMoreSynonyms() {
		return moreSynonyms;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setSynonyms(String[] synonyms) {
		this.synonyms = synonyms;
	}

	public void setMoreSynonyms(int moreSynonyms) {
		this.moreSynonyms = moreSynonyms;
	}

	public int getProductivity() {
		return productivity;
	}

	public int computeProductivity(AlvisIRIndex index, SearchConfig searchConfig) throws IOException {
		Query query = AlvisIRQueryNodeQueryConverter.convert(index.getGlobalAttributes().getTokenPositionGap(), searchConfig, getQueryNode());
		TotalHitCountCollector collector = new TotalHitCountCollector();
		index.getIndexSearcher().search(query, collector);
		productivity = collector.getTotalHits();
		return productivity;
	}
}
