package fr.inra.maiage.bibliome.alvisir.core.index;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.IndexWriter;

import fr.inra.maiage.bibliome.util.Iterators;

/**
 * Document indexer.
 * @author rbossy
 *
 */
public class DocumentsIndexer {
	/**
	 * Index documents.
	 * @param indexOptions index options.
	 * @param indexWriter target index.
	 * @param docs documents.
	 * @throws IOException
	 */
	public static <D,F,T,R> void index(IndexWriter indexWriter, IndexGlobalAttributes globalAttributes, AlvisIRIndexedDocuments<D,F,T,R> docs) throws IOException {
		Collection<String> allowedFieldNames = globalAttributes.getFieldNames();
		for (D doc : Iterators.loop(docs.getDocumentInstances())) {
			Document luceneDoc = new Document();
			for (AlvisIRIndexedFields<F,T,R> indexedFields : Iterators.loop(docs.getIndexedFields(doc))) {
				NormalizationOptions normalizationOptions = indexedFields.getNormalizationOptions();
				for (F field : Iterators.loop(indexedFields.getFieldInstances())) {
					String fieldName = indexedFields.getFieldName(field);
					if (!allowedFieldNames.contains(fieldName)) {
						throw new RuntimeException("field name " + fieldName + " is not allowed");
					}
					String fieldValue = indexedFields.getFieldValue(field);
//					System.err.println("fieldName = " + fieldName);
//					System.err.println("fieldValue = " + fieldValue);					
					Field luceneField = new Field(fieldName, fieldValue, Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS);
					byte fieldInstance = 0;
					Fieldable[] sameNameFields = luceneDoc.getFieldables(fieldName);
					if (sameNameFields != null) {
						fieldInstance = (byte) sameNameFields.length;
					}
					TokenStream sourceTokenStream = new AlvisIRTokenStream<>(indexedFields, field, globalAttributes.getTokenPositionGap(), fieldInstance);
					TokenStream tokenStream = normalizationOptions.getTokenStream(sourceTokenStream);
					luceneField.setTokenStream(tokenStream);
					luceneDoc.add(luceneField);
				}
			}
			indexWriter.addDocument(luceneDoc);
		}
	}
	
	public static void recordGlobalIndexAttributes(IndexWriter indexWriter, IndexGlobalAttributes attr) throws IOException {
		if (indexWriter.numDocs() != 0) {
			throw new RuntimeException("global index attributes should be recorded before indexing any document (" + indexWriter.numDocs() + ")");
		}
		Document doc = new Document();
		recordTokenPositionGap(doc, attr.getTokenPositionGap());
		recordNames(doc, IndexGlobalAttributes.FieldName.FIELD_NAMES, attr.getFieldNames());
		recordNames(doc, IndexGlobalAttributes.FieldName.ROLE_NAMES, attr.getAllRoleNames());
		recordNames(doc, IndexGlobalAttributes.FieldName.PROPERTY_KEYS, attr.getPropertyKeys());
		recordNames(doc, IndexGlobalAttributes.FieldName.RELATION_NAMES, attr.getAllRelationNames());
		recordNames(doc, IndexGlobalAttributes.FieldName.CREATION_DATE, attr.getCreationDate());
		indexWriter.addDocument(doc);
	}
	
	private static void recordTokenPositionGap(Document doc, int tokenPositionGap) {
		String value = Integer.toString(tokenPositionGap);
		Field field = new Field(IndexGlobalAttributes.FieldName.TOKEN_POSITION_GAP, value, Field.Store.YES, Field.Index.NOT_ANALYZED_NO_NORMS, Field.TermVector.NO);
		doc.add(field);
	}
	
	private static void recordNames(Document doc, String fieldName, String... names) {
		if (names == null) {
			return;
		}
		recordNames(doc, fieldName, Arrays.asList(names));
	}

	private static void recordNames(Document doc, String fieldName, Collection<String> names) {
		if (names == null) {
			return;
		}
		for (String s : names) {
			Field f = new Field(fieldName, s, Field.Store.YES, Field.Index.NO, Field.TermVector.NO);
			doc.add(f);
		}
	}
}
