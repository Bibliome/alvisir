package fr.inra.mig_bibliome.alvisir.core.index;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.TopDocs;

public class IndexGlobalAttributes {
	public static final class FieldName {
		public static final String TOKEN_POSITION_GAP = "__TOKEN_POSITION_GAP";
		public static final String FIELD_NAMES = "__FIELD_NAMES";
		public static final String ROLE_NAMES = "__ROLE_NAMES";
		public static final String RELATION_NAMES = "__RELATION_NAMES";
		public static final String CREATION_DATE = "__CREATION_DATE";
		public static final String PROPERTY_KEYS = "__PROPERTY_KEYS";
	}
	
	private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	private final int tokenPositionGap;
	private final Map<String,String[]> relationInfo = new TreeMap<String,String[]>();
	private final String[] propertyKeys;
	private final String creationDate;
	private Collection<String> fieldNames;

	public IndexGlobalAttributes(int tokenPositionGap, String[] propertyKeys) {
		super();
		this.tokenPositionGap = tokenPositionGap;
		this.creationDate = DATE_FORMAT.format(new Date());
		this.propertyKeys = propertyKeys;
	}
	
	public IndexGlobalAttributes(IndexReader indexReader) throws IOException {
		Document globalAttrDoc = getGlobalAttributesDocument(indexReader);
		
		String tpgStr = globalAttrDoc.get(FieldName.TOKEN_POSITION_GAP);
		tokenPositionGap = Integer.parseInt(tpgStr);
		
		fieldNames = new LinkedHashSet<String>(Arrays.asList(globalAttrDoc.getValues(FieldName.FIELD_NAMES)));

		String[] relationNames = globalAttrDoc.getValues(FieldName.RELATION_NAMES);
		String[] roleNames = globalAttrDoc.getValues(FieldName.ROLE_NAMES);
		for (int i = 0; i < relationNames.length; ++i) {
			String rel = relationNames[i];
			String role1 = roleNames[i*2];
			String role2 = roleNames[i*2+1];
			relationInfo.put(rel, new String[] { role1, role2 });
		}
		propertyKeys = globalAttrDoc.getValues(FieldName.PROPERTY_KEYS);
		creationDate = globalAttrDoc.get(FieldName.CREATION_DATE);
	}
	
	private static Document getGlobalAttributesDocument(IndexReader indexReader) throws IOException {
		try (IndexSearcher indexSearcher = new IndexSearcher(indexReader)) {
			Term tpgTerm = new Term(FieldName.TOKEN_POSITION_GAP, "");
			PrefixQuery tpgQuery = new PrefixQuery(tpgTerm);
			TopDocs tpgDocs = indexSearcher.search(tpgQuery, 1);
			if (tpgDocs.totalHits < 1) {
				throw new RuntimeException("could not retreive global index attributes");
			}
			return indexReader.document(tpgDocs.scoreDocs[0].doc);
		}
	}

	public int getTokenPositionGap() {
		return tokenPositionGap;
	}
	
	public String[] getAllRoleNames() {
		List<String> result = new ArrayList<String>();
		for (String[] roles : relationInfo.values()) {
			result.addAll(Arrays.asList(roles));
		}
		return result.toArray(new String[result.size()]);
	}

	public String[] getPropertyKeys() {
		return propertyKeys;
	}
	
	public String[] getAllRelationNames() {
		return relationInfo.keySet().toArray(new String[relationInfo.size()]);
	}

	public Map<String,String[]> getRelationInfo() {
		return Collections.unmodifiableMap(relationInfo);
	}

	public Collection<String> getFieldNames() {
		return Collections.unmodifiableCollection(fieldNames);
	}

	public void setFieldNames(Collection<String> fieldNames) {
		this.fieldNames = fieldNames;
	}
	
	public void addRelation(String name, String... roles) {
		relationInfo.put(name, roles);
	}
	
	public void addRelations(Map<String,String[]> relations) {
		relationInfo.putAll(relations);
	}

	public String getCreationDate() {
		return creationDate;
	}
}
