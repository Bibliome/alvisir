package fr.inra.mig_bibliome.alvisir.core.snippet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.spans.SpanMultiTermQueryWrapper;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.Spans;

import fr.inra.maiage.bibliome.util.fragments.Fragment;
import fr.inra.maiage.bibliome.util.fragments.FragmentCollection;
import fr.inra.maiage.bibliome.util.fragments.SimpleFragment;
import fr.inra.mig_bibliome.alvisir.core.AlvisIRIndex;
import fr.inra.mig_bibliome.alvisir.core.SearchConfig;
import fr.inra.mig_bibliome.alvisir.core.expand.explanation.MatchExplanation;
import fr.inra.mig_bibliome.alvisir.core.expand.explanation.PayloadDecoder;
import fr.inra.mig_bibliome.alvisir.core.expand.explanation.RelationMatchExplanation;

public class HighlightSelector {
	private final SearchConfig searchConfig;

	public HighlightSelector(SearchConfig searchConfig) {
		super();
		this.searchConfig = searchConfig;
	}

	public void selectHighlights(DocumentSnippet doc, String fieldName) throws IOException {
		List<Cluster> clusters = aggregateClusters(doc, fieldName);
//		System.err.println("doc.getDocId() = " + doc.getDocId());
//		System.err.println("fieldName = " + fieldName);
		Collection<Highlight> highlights = new ArrayList<Highlight>();
		while (purgeClusters(clusters)) {
//			System.err.println("clusters.size() = " + clusters.size());
			Cluster best = clusters.get(0);
			for (Highlight h : best.highlights) {
				h.setSelected(true);
			}
			highlights.addAll(best.highlights);
			Collection<MatchExplanation> explained = new ArrayList<MatchExplanation>(best.explanations);
//			System.err.println("explained = " + explained);
			updateClustersExplanations(clusters, explained);
//			System.err.println("updated");
		}
//		System.err.println("selecting referenced highlights");
		Map<Integer,MatchExplanation> missing = selectReferencedHighlights(doc, highlights);
//		System.err.println("rescueing " + missing.size() + " missing arguments");
		rescueMissingArguments(missing, doc);
//		System.err.println("done selectHighlights");
	}

	private static final class RescuePayloadDecoder extends PayloadDecoder {
		private final Map<Integer,MatchExplanation> missing;
		private Highlight currentHighlight;
		
		private RescuePayloadDecoder(Map<Integer,MatchExplanation> missing) {
			super();
			this.missing = missing;
		}

		@Override
		protected void handleArgument(int role, int argId) {
			// do nothing;
		}
		
		@Override
		protected void handleProperty(int key, String value) {
			if (currentHighlight != null) {
				currentHighlight.addProperty(key, value);
			}
		}

		@Override
		protected void handleMatch() {
			// do nothing
		}

		@Override
		protected void handleToken(int id, int fieldInstance) {
			if (missing.containsKey(id)) {
				currentHighlight = new Highlight(id, missing.remove(id));
				currentHighlight.setSelected(true);
				FieldSnippet field = getDocSnippet().getSnippet(getFieldName(), fieldInstance);
				field.addHighlight(currentHighlight);
			}
			else {
				currentHighlight = null;
			}
		}
		
		@Override
		protected void handleFragment(int fieldInstance, int start, int end) {
			if (currentHighlight != null) {
				Fragment frag = new SimpleFragment(start, end);
				currentHighlight.addFragment(frag);
			}
		}
	}
	
	private void rescueMissingArguments(Map<Integer,MatchExplanation> missing, DocumentSnippet doc) throws IOException {
		AlvisIRIndex index = searchConfig.getIndex();
		IndexReader indexReader = index.getIndexReader();
		Collection<String> fieldNames = index.getGlobalAttributes().getFieldNames();
		RescuePayloadDecoder decoder = new RescuePayloadDecoder(missing);
		for (String field : fieldNames) {
			if (missing.isEmpty()) {
				return;
			}
			Term term = new Term(field);
			PrefixQuery prefixQuery = new PrefixQuery(term);
			SpanQuery spanQuery = new SpanMultiTermQueryWrapper<PrefixQuery>(prefixQuery);
			SpanQuery rewrittenQuery = (SpanQuery) spanQuery.rewrite(indexReader);
			Spans spans = rewrittenQuery.getSpans(indexReader);
			decoder.setFieldName(field);
			decoder.decode(new DocumentSnippet[] { doc }, spans);
		}
	}

	private static Map<Integer,MatchExplanation> selectReferencedHighlights(DocumentSnippet doc, Collection<Highlight> highlights) {
		Map<Integer,MatchExplanation> result = new HashMap<Integer,MatchExplanation>();
		for (Highlight h : highlights) {
			if (h.isRelation()) {
				RelationMatchExplanation explanation = (RelationMatchExplanation) h.getExplanation();
				MatchExplanation defaultExplanation = explanation.getLeftExplanations().get(0);
				Map<Integer,Integer> args = h.getArguments();
				for (Integer r : args.values()) {
					Highlight a = doc.getHighlight(r);
					if (a == null) {
						result.put(r, defaultExplanation);
						defaultExplanation = explanation.getRightExplanations().get(0);
					}
					else {
						a.setSelected(true);
					}
				}
			}
		}
		return result;
	}

	private List<Cluster> aggregateClusters(DocumentSnippet doc, String fieldName) {
		Set<MatchExplanation> matchExplanations = doc.getAllMatchExplanations();
		int explanationCount = matchExplanations.size();
		int maxSpan = searchConfig.getHighlightClusterRadius() * explanationCount;
		
		List<Cluster> result = new ArrayList<Cluster>();
		Collection<Cluster> current = new ArrayList<Cluster>();
		for (FieldSnippet f : doc.getFieldSnippets()) {
			if (!f.getFieldName().equals(fieldName)) {
				continue;
			}
			for (Highlight h : f.getHighlights()) {
				Iterator<Cluster> currentIt = current.iterator();
				while (currentIt.hasNext()) {
					Cluster cluster = currentIt.next();
					ClusterAgglutinationStatus status = cluster.addHighlight(h, maxSpan);
					switch (status) {
						case DISTANT:
							currentIt.remove();
							result.add(cluster);
							break;
						case REDUNDANT:
							break;
						case OKAY:
							if (cluster.getExplanationCount() == explanationCount) {
								currentIt.remove();
								result.add(cluster);
							}
							break;
					}
				}
				Cluster cluster = new Cluster(h);
				current.add(cluster);
			}
			result.addAll(current);
			current.clear();
		}
		return result;
	}

	private enum ClusterAgglutinationStatus {
		DISTANT,
		REDUNDANT,
		OKAY;
	}

	private static final class Cluster {
		private final Collection<Highlight> highlights = new ArrayList<Highlight>();
		private final FragmentCollection<Fragment> hFragments = new FragmentCollection<Fragment>();
		private final Set<MatchExplanation> explanations = new HashSet<MatchExplanation>();

		private Cluster(Highlight h) {
			super();
			highlights.add(h);
			explanations.add(h.getExplanation());
			hFragments.addAll(h.getFragments());
		}
		
		private int getExplanationCount() {
			return explanations.size();
		}
		
		private int getSpan() {
			return hFragments.get(hFragments.size() - 1).getStart() - hFragments.get(0).getStart();
		}
		
		private ClusterAgglutinationStatus addHighlight(Highlight h, int maxSpan) {
			Collection<Fragment> frags = h.getFragments();
			Iterator<Fragment> clusterIt = hFragments.iterator();
			Iterator<Fragment> highlightIt = frags.iterator();
			Fragment clusterCurrent = clusterIt.next();
			Fragment highlightCurrent = highlightIt.next();
			while (true) {
				int d = Math.max(clusterCurrent.getStart(), highlightCurrent.getStart()) - Math.min(clusterCurrent.getEnd(), highlightCurrent.getEnd());
				if (d <= maxSpan) {
					MatchExplanation expl = h.getExplanation();
					if (explanations.contains(expl)) {
						return ClusterAgglutinationStatus.REDUNDANT;
					}
					highlights.add(h);
					explanations.add(expl);
					hFragments.addAll(frags);
					return ClusterAgglutinationStatus.OKAY;
				}
				if (clusterCurrent.getStart() < highlightCurrent.getStart()) {
					if (clusterIt.hasNext()) {
						clusterCurrent = clusterIt.next();
						continue;
					}
					break;
				}
				if (highlightIt.hasNext()) {
					highlightCurrent = highlightIt.next();
					continue;
				}
				break;
			}
			return ClusterAgglutinationStatus.DISTANT;
		}
		
		private static final Comparator<Cluster> COMPARATOR = new Comparator<Cluster>() {
			@Override
			public int compare(Cluster o1, Cluster o2) {
				int r = Integer.compare(o2.getExplanationCount(), o1.getExplanationCount());
				if (r != 0)
					return r;
				r = Integer.compare(o1.getSpan(), o2.getSpan());
				if (r != 0)
					return r;
				r = Integer.compare(o1.hFragments.get(0).getStart(), o2.hFragments.get(0).getStart());
				if (r != 0)
					return r;
				return 0;
			}
		};
	}

	private static boolean purgeClusters(List<Cluster> clusters) {
//		System.err.println("purging");
//		System.err.println("sorting");
		Collections.sort(clusters, Cluster.COMPARATOR);
//		System.err.println("searching redundant");
		int r = searchFirstRedundant(clusters);
		List<Cluster> redundant = clusters.subList(r, clusters.size());
//		System.err.println("clearing");
		redundant.clear();
//		System.err.println("done purging");
		return !clusters.isEmpty();
	}
	
	private static int searchFirstRedundant(List<Cluster> clusters) {
		int lo = 0;
		int hi = clusters.size();
		while (lo < hi) {
            int mid = (lo + hi) / 2;
            Cluster midCluster = clusters.get(mid);
            int size = midCluster.explanations.size();
            if (size > 0)
                lo = mid + 1;
            else
                hi = mid;
		}
		return lo;
	}

	private static void updateClustersExplanations(List<Cluster> clusters, Collection<MatchExplanation> explained) {
		for (Cluster c : clusters) {
			c.explanations.removeAll(explained);
		}		
	}
}
