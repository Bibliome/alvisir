package fr.inra.mig_bibliome.alvisir.core.expand.index;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.shared.PrefixMapping;
import org.apache.lucene.index.IndexWriter;
import org.bibliome.util.Iterators;
import org.bibliome.util.LoggingUtils;
import org.bibliome.util.streams.SourceStream;

public class RDFExpanderIndexer extends ExpanderIndexer {
	private SourceStream source;
	private Map<String,String> prefixes;

	@Override
	public void indexExpansions(Logger logger, IndexWriter indexWriter) throws IOException, ExpanderIndexerException {
		Model model = createModel(logger);
		
	}

	private Model createModel(Logger logger) throws IOException {
		LoggingUtils.configureSilentLog4J();
		Model model = ModelFactory.createDefaultModel();
		model.setNsPrefixes(PrefixMapping.Standard);
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		model.setNsPrefix("skos", "http://www.w3.org/2004/02/skos/core#");
		model.setNsPrefix("oboInOwl", "http://www.geneontology.org/formats/oboInOwl#");
		model.setNsPrefixes(prefixes);
		for (InputStream is : Iterators.loop(source.getInputStreams())) {
			logger.info("loading model from: " + source.getStreamName(is));
//			System.err.println("is = " + is);
//			model.read(is, null, Lang.RDFXML.toString());
			RDFDataMgr.read(model, is, Lang.RDFXML);
		}
		return model;
	}

}
