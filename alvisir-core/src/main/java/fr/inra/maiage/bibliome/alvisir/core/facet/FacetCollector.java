package fr.inra.maiage.bibliome.alvisir.core.facet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermVectorMapper;
import org.apache.lucene.index.TermVectorOffsetInfo;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import fr.inra.maiage.bibliome.alvisir.core.FieldOptions;
import fr.inra.maiage.bibliome.alvisir.core.snippet.DocumentSnippet;

public class FacetCollector extends TermVectorMapper {
	private final FieldOptions fieldOptions;
	private final Collection<FacetCategory> globalFacets;
	private final Map<Integer,Collection<FacetCategory>> docFacets = new HashMap<Integer,Collection<FacetCategory>>();
	private int currentDoc;
	private final Collection<FacetCategory> activeFacets = new ArrayList<FacetCategory>();
	
	public FacetCollector(FieldOptions fieldOptions, Collection<FacetCategory> globalFacets, DocumentSnippet[] docSnippets) {
		this.fieldOptions = fieldOptions;
		this.globalFacets = globalFacets;
		for (DocumentSnippet doc : docSnippets) {
			int docId = doc.getDocId();
			Collection<FacetCategory> facets = doc.getFacets();
			docFacets.put(docId, facets);
		}
	}

	public void collectFacets(IndexReader indexReader, TopDocs topDocs) throws IOException {
		for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
//			System.err.println("scoreDoc = " + scoreDoc);
			currentDoc = scoreDoc.doc;
			indexReader.getTermFreqVector(currentDoc, this);
		}
	}

	@Override
	public void setExpectations(String field, int numTerms, boolean storeOffsets, boolean storePositions) {
//		System.err.println("currentDoc = " + currentDoc);
//		System.err.println("field = " + field);
		activeFacets.clear();
//		System.err.println("docFacets.keys() = " + docFacets.keySet());
		if (docFacets.containsKey(currentDoc)) {
			Collection<FacetCategory> currentDocFacets = docFacets.get(currentDoc);
//			System.err.println("currentDocFacets = " + currentDocFacets);
			activateFacets(currentDocFacets, field);
		}
//		System.err.println("activeFacets = " + activeFacets);
		activateFacets(globalFacets, field);
//		System.err.println("activeFacets = " + activeFacets);
	}
	
	private void activateFacets(Collection<FacetCategory> facets, String field) {
//		System.err.println("field = " + field);
		for (FacetCategory f : facets) {
			String facetField = f.getSpecification().getFieldName();
			for (String actualField : fieldOptions.getFields(facetField)) {
				if (field.equals(actualField)) {
					activeFacets.add(f);
					break;
				}
			}
		}
	}

	@Override
	public void map(String term, int frequency, TermVectorOffsetInfo[] offsets, int[] positions) {
		try {
			for (FacetCategory fc : activeFacets) {
				FacetSpecification fs = fc.getSpecification();
				FacetLabelFactory flf = fs.getLabelFactory();
				if (fc.getSpecification().accept(term)) {
					FacetTerm ft = fc.safeGet(term);
					if (ft.get() == 0) {
						String label = flf.getFacetLabel(term);
						ft.setLabel(label);
					}
					ft.incr(frequency);
					ft.incrDocFreq(currentDoc);
				}
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}