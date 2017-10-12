package fr.inra.mig_bibliome.alvisir.core;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import fr.inra.maiage.bibliome.util.Strings;
import fr.inra.maiage.bibliome.util.Timer;
import fr.inra.maiage.bibliome.util.clio.CLIOException;
import fr.inra.maiage.bibliome.util.clio.CLIOParser;
import fr.inra.maiage.bibliome.util.clio.CLIOption;
import fr.inra.maiage.bibliome.util.xml.XMLUtils;

/**
 * Command line search.
 * @author rbossy
 *
 */
public class AlvisIRCLI extends CLIOParser {
	private String searchConfigPath;
	private String xsltPath;
	private String xsltParamsPath;
	private Integer startSnippet;
	private List<String> queryStrings = new ArrayList<String>(1);
	private String queryString;
	private String queryPath;
	private Integer snippetCount;
	private Collection<String> mandatoryFields = new HashSet<String>();

	@CLIOption("-xslt")
	public void setXSLTPath(String xsltPath) {
		this.xsltPath = xsltPath;
	}
	
	@CLIOption("-params")
	public void setXSLTParamsPath(String xsltParamsPath) {
		this.xsltParamsPath = xsltParamsPath;
	}
	
	@CLIOption("-start")
	public void setStartSnippet(int startSnippet) {
		this.startSnippet = startSnippet;
	}
	
	@CLIOption("-config")
	public void setSearchConfigPath(String searchConfigPath) {
		this.searchConfigPath = searchConfigPath;
	}
	
	@CLIOption("-query")
	public void setQueryPath(String queryPath) {
		this.queryPath = queryPath;
	}
	
	@CLIOption("-snippets")
	public void setSnippetCount(int snippetCount) {
		this.snippetCount = snippetCount;
	}
	
	@CLIOption("-display")
	public void addMandatoryField(String fieldName) {
		mandatoryFields.add(fieldName);
	}

	@CLIOption(value="-help", stop=true)
	public void help() { 
		System.out.print(usage());
	}

	@Override
	protected boolean processArgument(String arg) throws CLIOException {
		if (queryPath != null) {
			throw new CLIOException("extra argument: '" + arg + "'");
		}
		queryStrings.add(arg);
		return false;
	}

	@Override
	public String getResourceBundleName() {
		return getClass().getCanonicalName() + "Help";
	}
	
	private void run() throws IOException, ParserConfigurationException, TransformerException {
		SearchResult searchResult = new SearchResult(searchConfigPath);
		SearchConfig searchConfig = searchResult.getSearchConfig();
		if (startSnippet != null) {
			searchConfig.setStartSnippet(startSnippet);
		}
		if (snippetCount != null) {
			searchConfig.setSnippetCount(snippetCount);
		}
		for (String fieldName : mandatoryFields) {
			searchConfig.addMandatoryField(fieldName);
		}
		Timer<AlvisIRTimerCategory> timer = searchResult.getTimer();
		if (queryPath != null) {
			queryString = readWholeFile(queryPath);
		}
		else {
			queryString = Strings.join(queryStrings, ' ');
		}
		searchResult.search(queryString);
		timer.stop();
		
		ResultXMLSerializer serializer = new ResultXMLSerializer();
		if (xsltPath != null) {
			serializer.setTransformer(xsltPath);
			serializer.setTarget(System.out);
			if (xsltParamsPath != null) {
				serializer.setParams(xsltParamsPath);
			}
			serializer.serialize(searchResult);
		}
		else {
			serializer.serialize(searchResult);
			XMLUtils.writeDOMToFile(serializer.getOriginalDoc(), null, new OutputStreamWriter(System.out));
		}
	}
	
	public static void main(String[] args) throws IOException, ParserConfigurationException, TransformerFactoryConfigurationError, TransformerException, CLIOException {
		AlvisIRCLI inst = new AlvisIRCLI();
		if (inst.parse(args)) {
			return;
		}
		if (inst.searchConfigPath == null) {
			throw new CLIOException("-config is mandatory");
		}
		inst.run();
	}
	
	private static String readWholeFile(String path) throws IOException {
		StringBuilder result = new StringBuilder();
		try (Reader r = new InputStreamReader(new FileInputStream(path), "UTF-8")) {
			char[] buffer = new char[1024];
			while (true) {
				int nread = r.read(buffer);
				if (nread == -1) {
					break;
				}
				result.append(buffer, 0, nread);
			}
		}
		return result.toString();
	}
}
