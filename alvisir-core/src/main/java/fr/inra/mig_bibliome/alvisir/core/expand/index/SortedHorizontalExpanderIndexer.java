package fr.inra.mig_bibliome.alvisir.core.expand.index;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import fr.inra.maiage.bibliome.util.Iterators;
import fr.inra.maiage.bibliome.util.filelines.FileLines;
import fr.inra.maiage.bibliome.util.filelines.InvalidFileLineEntry;
import fr.inra.maiage.bibliome.util.streams.SourceStream;

public class SortedHorizontalExpanderIndexer extends ExpanderIndexer {
	private SourceStream source;
	private int canonicalTermColumn = 0;
	private int labelColumn = 1;
	private int firstSynonymColumn = 2;
	private String prefix = "";
	private String suffix = "";
	private int typeColumn = 3;
	private String type = null;

	public SortedHorizontalExpanderIndexer() {
		super();
	}

	@Override
	public void indexExpansions(Logger logger, IndexWriter indexWriter) throws IOException, ExpanderIndexerException {
		// TODO Auto-generated method stub
		fileLines.setLogger(logger);
		for (BufferedReader r : Iterators.loop(source.getBufferedReaders())) {
			logger.info("reading '" + source.getStreamName(r) + "'");
			fileLines.process(r, indexWriter);
		}
	}
	
	private final FileLines<IndexWriter> fileLines = new FileLines<IndexWriter>() {
		@Override
		public void processEntry(IndexWriter data, int lineno, List<String> entry) throws InvalidFileLineEntry {
			String canonical = prefix + entry.get(canonicalTermColumn) + suffix;
			String label = entry.get(labelColumn);
			String type = SortedHorizontalExpanderIndexer.this.type == null ? entry.get(typeColumn) : SortedHorizontalExpanderIndexer.this.type;
			Document doc = createExpansionDocument(canonical, label, type);
			addSynonym(doc, label);
			for (int i = firstSynonymColumn; i < entry.size(); ++i) {
				String synonym = entry.get(firstSynonymColumn);
				addSynonym(doc, synonym);
			}
		}
	};

	public SourceStream getSource() {
		return source;
	}

	public int getCanonicalTermColumn() {
		return canonicalTermColumn;
	}

	public int getLabelColumn() {
		return labelColumn;
	}

	public int getFirstSynonymColumn() {
		return firstSynonymColumn;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public int getTypeColumn() {
		return typeColumn;
	}

	public String getType() {
		return type;
	}

	public void setSource(SourceStream source) {
		this.source = source;
	}

	public void setCanonicalTermColumn(int canonicalTermColumn) {
		this.canonicalTermColumn = canonicalTermColumn;
	}

	public void setLabelColumn(int labelColumn) {
		this.labelColumn = labelColumn;
	}

	public void setFirstSynonymColumn(int firstSynonymColumn) {
		this.firstSynonymColumn = firstSynonymColumn;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public void setTypeColumn(int typeColumn) {
		this.typeColumn = typeColumn;
	}

	public void setType(String type) {
		this.type = type;
	}
}
