package fr.inra.mig_bibliome.alvisir.core.snippet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import fr.inra.maiage.bibliome.util.defaultmap.DefaultMap;

/**
 * Field value builder that uses the lucene index stored fields.
 * @author rbossy
 *
 */
public class IndexFieldValueBuilder implements FieldValueBuilder {
	private final IndexReader indexReader;

	/**
	 * Creates a index field value builder using the specified index.
	 * @param indexReader index.
	 */
	public IndexFieldValueBuilder(IndexReader indexReader) {
		super();
		this.indexReader = indexReader;
	}
	
	private static final class FieldValueCache extends DefaultMap<String,String[]> {
		private final Document doc;
		
		private FieldValueCache(Document doc) {
			super(true, new HashMap<String,String[]>());
			this.doc = doc;
		}

		@Override
		protected String[] defaultValue(String key) {
			return doc.getValues(key);
		}
	}
	
	@Override
	public void fetchFieldValues(DocumentSnippet docSnippet) throws IOException {
		Document doc = indexReader.document(docSnippet.getDocId());
		DefaultMap<String,String[]> cache = new FieldValueCache(doc);
		for (FieldSnippet fieldSnippet : docSnippet.getFieldSnippets()) {
			String name = fieldSnippet.getFieldName();
			String[] values = cache.safeGet(name);
			fieldSnippet.setFieldValue(values[fieldSnippet.getFieldInstance()]);
		}
	}
	
	@Override
	public void fetchMandatoryFieldValues(Set<String> mandatoryFields, DocumentSnippet docSnippet) throws IOException {
		Document doc = indexReader.document(docSnippet.getDocId());
		for (String name : mandatoryFields) {
			String[] values = doc.getValues(name);
			for (int i = 0; i < values.length; ++i) {
				FieldSnippet fieldSnippet = docSnippet.ensureSnippet(name, i);
				fieldSnippet.setFieldValue(name);
				fieldSnippet.setMandatory(true);
			}
		}
	}
}
