package fr.inra.mig_bibliome.alvisir.core;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import fr.inra.mig_bibliome.alvisir.core.index.IndexGlobalAttributes;

public class AlvisIRIndex {
	private final IndexGlobalAttributes globalAttributes;
	private final IndexSearcher indexSearcher;
	
	public AlvisIRIndex(String indexPath) throws IOException {
		super();
		Directory indexDir = FSDirectory.open(new File(indexPath));
		IndexReader indexReader = IndexReader.open(indexDir);
		globalAttributes = new IndexGlobalAttributes(indexReader);
		indexSearcher = new IndexSearcher(indexReader);
	}

	public IndexSearcher getIndexSearcher() {
		return indexSearcher;
	}
	
	public IndexReader getIndexReader() {
		return indexSearcher.getIndexReader();
	}

	public IndexGlobalAttributes getGlobalAttributes() {
		return globalAttributes;
	}
}
