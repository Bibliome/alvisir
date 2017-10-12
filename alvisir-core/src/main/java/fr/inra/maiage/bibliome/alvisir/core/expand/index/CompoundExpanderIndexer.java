package fr.inra.maiage.bibliome.alvisir.core.expand.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.apache.lucene.index.IndexWriter;

/**
 * Expander indexer that contains references to several expander indexers.
 * @author rbossy
 *
 */
public class CompoundExpanderIndexer extends ExpanderIndexer {
	private final Collection<ExpanderIndexer> expanderIndexers = new ArrayList<ExpanderIndexer>();

	public CompoundExpanderIndexer() {
		super();
	}

	@Override
	public void indexExpansions(Logger logger, IndexWriter indexWriter) throws IOException, ExpanderIndexerException {
		for (ExpanderIndexer ei : expanderIndexers) {
			ei.indexExpansions(logger, indexWriter);
		}
	}
	
	/**
	 * Appends an expander indexer.
	 * @param ei
	 */
	public void addExpanderIndexer(ExpanderIndexer ei) {
		expanderIndexers.add(ei);
		ei.setParent(this);
	}
}
