package fr.inra.mig_bibliome.alvisir.core.expand.index;

import java.io.IOException;
import java.util.logging.Logger;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.bibliome.util.obo.OBOUtils;
import org.json.simple.JSONObject;
import org.obo.dataadapter.OBOParseException;
import org.obo.datamodel.OBOClass;
import org.obo.datamodel.OBOSession;
import org.obo.datamodel.Synonym;
import org.obo.util.TermUtil;

/**
 * Insert expansions from an OBO file into an expander index.
 * Before calling indexExpansions(), the OBO file must have been set with setOBOSession() or parse().
 * Expansions: each OBO term.
 * Annotation type: specified with setType().
 * Canonical: path of ids of the OBO term (separated by '/'), with the prefix specified by setPrefix().
 * Label: OBO term name.
 * Synonyms: OBO term name and all synonyms.
 * @author rbossy
 *
 */
public class OBOExpanderIndexer extends ExpanderIndexer {
	private String source;
	private OBOSession oboSession;
	private String prefix;
	private String type;
	private String jsonPropertyName;
	private String jsonRoot;
	
	public OBOExpanderIndexer() {
	}
	
	@Override
	public void indexExpansions(Logger logger, IndexWriter indexWriter) throws IOException, ExpanderIndexerException {
		logger.info("reading '" + source + "'");
		for (OBOClass oboTerm : TermUtil.getTerms(oboSession)) {
			for (StringBuilder canonical : OBOUtils.getPaths(prefix, oboTerm)) {
				canonical.append('/');
				String label = oboTerm.getName();
				Document doc = createExpansionDocument(canonical.toString(), label, type);
				addSynonym(doc, label);
				for (Synonym synonym : oboTerm.getSynonyms()) {
					addSynonym(doc, synonym.getText());
				}
				indexWriter.addDocument(doc);
			}
		}
		if (jsonPropertyName != null) {
			JSONObject json = OBOUtils.toJSON(oboSession, jsonRoot);
			String value = json.toString();
			addProperty(jsonPropertyName, value);
		}
	}

	public OBOSession getOBOSession() {
		return oboSession;
	}

	public String getPrefix() {
		return prefix;
	}

	public String getType() {
		return type;
	}

	public void setOBOSession(OBOSession oboSession) {
		this.oboSession = oboSession;
	}
	
	public void parse(String source) throws IOException, ExpanderIndexerException {
		setSource(source);
		try {
			setOBOSession(OBOUtils.parseOBO(source));
		}
		catch (OBOParseException e) {
			throw new ExpanderIndexerException(e);
		}
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getJsonPropertyName() {
		return jsonPropertyName;
	}

	public String getJsonRoot() {
		return jsonRoot;
	}

	public void setJSONPropertyName(String jsonPropertyName, String jsonRoot) {
		this.jsonPropertyName = jsonPropertyName;
		this.jsonRoot = jsonRoot;
	}
}
