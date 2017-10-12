package fr.inra.maiage.bibliome.alvisir.core.expand.index;

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

/**
 * Insert expansions from a TSV file.
 * Before calling indexExpansions(), the source must be specified with setSource().
 * Expansions: each group of lines with the same canonical.
 * Annotation type: specified by setType() or the value in the column specified by setTypeColumn().
 * Canonical: value of the column specified by setCanonicalTermColumn(), with the prefix specified by setPrefix().
 * Label: value of the column specified by setLabelColumn().
 * Synonyms: value of the column specified by setSynonymColumn().
 * @author rbossy
 *
 */
public class SortedVerticalExpanderIndexer extends ExpanderIndexer {
	private SourceStream source;
	private int synonymColumn = 0;
	private int canonicalTermColumn = 1;
	private int labelColumn = 2;
	private String prefix = "";
	private String suffix = "";
	private int typeColumn = 3;
	private String type = null;

	private String currentCanonicalTerm = null;
	private Document currentDocument = null;

	public SortedVerticalExpanderIndexer() {
		super();
	}

	@Override
	public void indexExpansions(Logger logger, IndexWriter indexWriter) throws IOException {
		currentCanonicalTerm = null;
		currentDocument = null;
		fileLines.setLogger(logger);
		for (BufferedReader r : Iterators.loop(source.getBufferedReaders())) {
			logger.info("reading '" + source.getStreamName(r) + "'");
			fileLines.process(r, indexWriter);
		}
	}

	private final FileLines<IndexWriter> fileLines = new FileLines<IndexWriter>() {
		@Override
		public void processEntry(IndexWriter data, int lineno, List<String> entry) throws InvalidFileLineEntry {
			try {
				updateCurrentDocument(data, entry);
			}
			catch (IOException e) {
				throw new InvalidFileLineEntry(e.getMessage(), entry, lineno);
			}
			String synonym = entry.get(synonymColumn);
			addSynonym(currentDocument, synonym);
		}

		private void updateCurrentDocument(IndexWriter data, List<String> entry) throws IOException {
			String canonicalTerm = entry.get(canonicalTermColumn);
			if (canonicalTerm.equals(currentCanonicalTerm)) {
				return;
			}
			if (currentDocument != null) {
				data.addDocument(currentDocument);
			}
			String label = entry.get(labelColumn);
			String type = SortedVerticalExpanderIndexer.this.type == null ? entry.get(typeColumn) : SortedVerticalExpanderIndexer.this.type;
			currentDocument = createExpansionDocument(prefix + canonicalTerm + suffix, label, type);
			currentCanonicalTerm = canonicalTerm;
		}
	};
	
	public SourceStream getSource() {
		return source;
	}

	public int getSynonymColumn() {
		return synonymColumn;
	}

	public int getCanonicalTermColumn() {
		return canonicalTermColumn;
	}

	public int getLabelColumn() {
		return labelColumn;
	}

	public String getPrefix() {
		return prefix;
	}

	public int getTypeColumn() {
		return typeColumn;
	}

	public String getType() {
		return type;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}

	public void setSource(SourceStream source) {
		this.source = source;
	}

	public void setSynonymColumn(int synonymColumn) {
		if (synonymColumn < 0) {
			throw new IllegalArgumentException();
		}
		this.synonymColumn = synonymColumn;
	}

	public void setCanonicalTermColumn(int canonicalTermColumn) {
		if (canonicalTermColumn < 0) {
			throw new IllegalArgumentException();
		}
		this.canonicalTermColumn = canonicalTermColumn;
	}

	public void setLabelColumn(int labelColumn) {
		if (labelColumn < 0) {
			throw new IllegalArgumentException();
		}
		this.labelColumn = labelColumn;
	}

	public void setPrefix(String prefix) {
		if (prefix == null) {
			throw new NullPointerException();
		}
		this.prefix = prefix;
	}

	public void setTypeColumn(int typeColumn) {
		if (typeColumn < 0) {
			throw new IllegalArgumentException();
		}
		this.typeColumn = typeColumn;
	}

	public void setType(String type) {
		this.type = type;
	}
}
