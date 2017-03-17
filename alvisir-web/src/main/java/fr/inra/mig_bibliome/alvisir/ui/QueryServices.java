/*
 *
 *      AlvisIR2 UI
 *
 *      Copyright Institut National de la Recherche Agronomique, 2013.
 *
 */
package fr.inra.mig_bibliome.alvisir.ui;

import com.sun.jersey.api.NotFoundException;
import static fr.inra.mig_bibliome.alvisir.ui.XMLUtils.getDocumentBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Date;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import org.apache.commons.io.IOUtils;
import fr.inra.mig_bibliome.alvisir.core.ResultXMLSerializer;
import fr.inra.mig_bibliome.alvisir.core.SearchConfig;
import fr.inra.mig_bibliome.alvisir.core.SearchConfigException;
import fr.inra.mig_bibliome.alvisir.core.SearchConfigXMLSerializer;
import fr.inra.mig_bibliome.alvisir.core.SearchResult;
import fr.inra.mig_bibliome.alvisir.core.expand.ExpanderException;
import fr.inra.mig_bibliome.alvisir.core.expand.TextExpander;
import org.bibliome.util.xml.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

@Path("")
public class QueryServices {

    //Name of the web context param used referencing the UI configuration file
    private static final String SearchConfigPath_ParamName = "configPath";
    private static final String ManifestResourcePath = "/META-INF/MANIFEST.MF";

    private Configuration getConfig() {
        //Get instance specific config file (configured with a web context parameter)
        String uiConfigPath = context.getInitParameter(SearchConfigPath_ParamName);
        if (uiConfigPath == null) {
            throw new IllegalArgumentException("The configuration of this instance is incomplete : Missing " + SearchConfigPath_ParamName + " parameter");
        }
        try {
            return new Configuration(context, uiConfigPath);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid configuration in " + uiConfigPath + " :\n " + e.getMessage());
        }
    }

    private String getError(String query, String primaryMessage, String reasonMessage) {
        try {
            Document errorDoc = getDocumentBuilder().newDocument();
            Element root = errorDoc.createElement("search-result");

            Element a = errorDoc.createElement("messages");
            root.appendChild(a);

            Element b = errorDoc.createElement("message");
            Element t = errorDoc.createElement("text");
            b.appendChild(t);
            t.appendChild(errorDoc.createTextNode(primaryMessage));
            a.appendChild(b);
            b = errorDoc.createElement("exception");
            b.appendChild(errorDoc.createTextNode(reasonMessage));
            a.appendChild(b);

            a = errorDoc.createElement("query");
            root.appendChild(a);
            b = errorDoc.createElement("query-string");
            b.appendChild(errorDoc.createTextNode(query));
            a.appendChild(b);

            errorDoc.appendChild(root);
            return serializedErroDocument(errorDoc);

        } catch (Exception ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
            return "<error>Search ended in irrecoverable error : consult server logs</error>";
        }

    }

    private String serializedDocument(Document originalDoc, String processingInstruction) throws TransformerFactoryConfigurationError, IOException {
        try (StringWriter writer = new StringWriter()) {
            Node commentNode = originalDoc.insertBefore(originalDoc.createComment(" AlvisIR-build-date : " + getBuildDateFromManifest() + " "), originalDoc.getDocumentElement());
            originalDoc.insertBefore(originalDoc.createProcessingInstruction("xml-stylesheet", processingInstruction), commentNode);
            XMLUtils.writeDOMToFile(originalDoc, null, writer);
            writer.flush();
            return writer.toString();
        }
    }

    private String serializedDocument(Document originalDoc, SearchConfig searchConfig, String transformSheetSuffix) throws TransformerFactoryConfigurationError, IOException {
        int startSnippet = 0;
        int snippetCount = 10;
        if (searchConfig != null) {
            startSnippet = searchConfig.getStartSnippet();
            snippetCount = searchConfig.getSnippetCount();
        }
        //add processing instruction to trigger xslt transformation on the client
        String xslt_procinst = "type=\"text/xsl\" "
                //Note : current UserAgents do not retrieve xslt with query params, hence the use of path params!!
                + "href=\"" + snippetCount
                + "/" + startSnippet
                + "/" + transformSheetSuffix + "\" ";
        return serializedDocument(originalDoc, xslt_procinst);
    }

    private String serializedErroDocument(Document originalDoc) throws TransformerFactoryConfigurationError, IOException {
        String xslt_procinst = "type=\"text/xsl\" href=\"" + Configuration.MinimalTransformSheet_Path + "\" ";
        return serializedDocument(originalDoc, xslt_procinst);
    }

    private String getOntologyResource(String ontoName) {
        Configuration uiConfig = getConfig();
        //check 1rst if the ontology is stored in the expander
        String ontoKey = uiConfig.getOntologyExpanderKey(ontoName);
        
        if (ontoKey == null) {
            //if not in the expander, the ontology may be stored in a regular file
            String ontoPath = uiConfig.getOntologyPath(ontoName);
                    
            if (ontoPath == null) {
                Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, "Unknow ontology named: \"" + ontoName + "\"");
                throw new NotFoundException("Unknow ontology named: \"" + ontoName + "\"");
            } else {
                Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, "Path = " + ontoPath);
                try (FileInputStream is = new FileInputStream(ontoPath)) {
                    String onto = IOUtils.toString(is, "UTF-8");
                    Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, "onto = \n" + onto.length());
                    return onto;
                } catch (IOException ex) {
                    Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
                    throw new NotFoundException("!could not fetch ontology - File: \"" + ontoPath + "\"");
                }
            }
        } else {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, "Key = " + ontoKey);
            SearchConfig searchConfig = null;
            try {
                searchConfig = SearchConfigXMLSerializer.getSearch(uiConfig.getSearchConfigPath());
            } catch (IOException | SAXException | ParserConfigurationException | ExpanderException | SearchConfigException ex) {
                Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
                throw new NotFoundException("!could not fetch ontology - Prop: \"" + ontoKey + "\"");
            }

            TextExpander expander = searchConfig.getTextExpander();
            try {
                return expander.getProperty(ontoKey);
            } catch (IOException ex) {
                Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
                throw new NotFoundException("!could not fetch ontology - Prop: \"" + ontoKey + "\"");
            }
        }
    }
    /*
     * 
     */
    @javax.ws.rs.core.Context
    private ServletContext context;
    private String buildDate = null;

    /**
     *
     * @return the build date as set in the war manifest
     */
    public String getBuildDateFromManifest() {
        if (buildDate == null) {
            try {
                InputStream is = getClass().getResourceAsStream(ManifestResourcePath);
                Properties props = new Properties();
                props.load(is);
                for (Map.Entry<Object, Object> entry : props.entrySet()) {
                    if ("build-date".equals(entry.getKey().toString())) {
                        buildDate = entry.getValue().toString();
                        break;
                    }
                }
            } catch (Exception ex) {
                Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return buildDate;
    }

    @GET
    @Path(Configuration.QueryService_Path)
    @Produces(MediaType.APPLICATION_XML)
    /**
     * WebService used to process an actual AlvisIR query and return the XML
     * response document that will be transformed in the standard AlvisIR UI
     */
    public String getStandardUIQueryResult(
            @DefaultValue("") @QueryParam(value = Configuration.Query_QParamName) final String query,
            @DefaultValue("-1") @QueryParam(value = Configuration.StartSnippet_QParamName) final int startSnippet,
            @DefaultValue("-1") @QueryParam(value = Configuration.SnippetCount_QParamName) final int snippetCount) {
        return getQueryResult(query, startSnippet, snippetCount, Configuration.StandardUITransformSheet_PathSuffix);
    }

    @GET
    @Path(Configuration.TabularOuputQueryService_Path)
    @Produces(MediaType.APPLICATION_XML)
    /**
     * WebService used to process an actual AlvisIR query and return the XML
     * response document that will be transformed in a tabular output
     */
    public String getTabularOutputQueryResult(
            @DefaultValue("") @QueryParam(value = Configuration.Query_QParamName) final String query,
            @DefaultValue("-1") @QueryParam(value = Configuration.StartSnippet_QParamName) final int startSnippet,
            @DefaultValue("-1") @QueryParam(value = Configuration.SnippetCount_QParamName) final int snippetCount) {
        return getQueryResult(query, startSnippet, snippetCount, Configuration.TabularTransformSheet_PathSuffix);
    }

    public String getQueryResult(final String query, final int startSnippet, final int snippetCount, final String transformSheetSuffix) {
        SearchConfig searchConfig = null;
        try {
            Configuration uiConfig = getConfig();

            //Init AlvisIR search component
            SearchResult searchResult = new SearchResult(uiConfig.getSearchConfigPath());

            searchConfig = searchResult.getSearchConfig();
            if (searchConfig != null) {
                if (startSnippet != -1) {
                    searchConfig.setStartSnippet(startSnippet);
                }
                if (snippetCount != -1) {
                    searchConfig.setSnippetCount(snippetCount);
                }
            }
            //Perform actual search
            searchResult.search(query);

            ResultXMLSerializer serializer = new ResultXMLSerializer();
            serializer.serialize(searchResult);

            //
            return serializedDocument(serializer.getOriginalDoc(), searchConfig, transformSheetSuffix);

        } catch (IOException | ParserConfigurationException | TransformerException ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
            return getError(query, "Search ended in error : ", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
            return getError(query, "Search ended in error : ", ex.getMessage());
        }

    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{snippetCount}/{startSnippet}/" + Configuration.StandardUITransformSheet_PathSuffix)
    /**
     * WebService used to produce the dynamic XSLT style-sheet that will allow
     * to transform the XML search response in proper HTML document
     */
    public String getParametrizedStandardUITransformStyleSheet(
            @PathParam("startSnippet") final int startSnippet,
            @PathParam("snippetCount") final int snippetCount) {

        try {
            Configuration uiConfig = getConfig();
            return XSLTStyleSheetProducer.getStandardUITransformSheet(context, uiConfig, startSnippet, snippetCount);
        } catch (Exception ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
            throw new WebApplicationException(ex);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path(Configuration.MinimalTransformSheet_Path)
    /**
     * WebService used to produce the minimal XSLT style-sheet used in case of
     * an error to produce HTML document
     */
    public String getMinimalTransformStyleSheet() {
        try {
            return XSLTStyleSheetProducer.getMinimalStandardUITransformSheet(context);
        } catch (Exception ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
            throw new WebApplicationException(ex);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_XML)
    @Path("{snippetCount}/{startSnippet}/" + Configuration.TabularTransformSheet_PathSuffix)
    /**
     * WebService used to produce the dynamic XSLT style-sheet that will allow
     * to transform the XML search response in tabular output
     */
    public String getParametrizedTabularOutputTransformStyleSheet(
            @PathParam("startSnippet") final int startSnippet,
            @PathParam("snippetCount") final int snippetCount) {

        try {
            Configuration uiConfig = getConfig();
            return XSLTStyleSheetProducer.getTabularOutputransformSheet(context, uiConfig, startSnippet, snippetCount);
        } catch (Exception ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
            throw new WebApplicationException(ex);
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("resources/ontologies/{ontoName}")
    /**
     * WebService used to retrieve the ontology resources displayed in the UI
     */
    public Response getOntology(@PathParam("ontoName") final String ontoName) {
        try {
            return Response
                    .ok(getOntologyResource(ontoName), MediaType.APPLICATION_JSON)
                    //ontology is not supposed to change : 1 hour life time
                    .expires(new Date(System.currentTimeMillis() + 1000 * 60 * 60))
                    .build();

        } catch (IllegalArgumentException ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
            throw new WebApplicationException(ex);
        }
    }

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    @Path(Configuration.ExtraCSS_Path)
    /**
     * WebService used to retrieve the external extra CSS
     */
    public Response getExtraCss() {
        try {
            Configuration uiConfig = getConfig();
            String cssPath = uiConfig.getExtraCSSPath();
            if (cssPath == null || !new File(cssPath).canRead()) {
                throw new NotFoundException("Unknow extra CSS: \"" + cssPath + "\"");
            }
            try {
                return Response
                        .ok(new FileInputStream(cssPath), MediaType.TEXT_PLAIN)
                        //prevent caching of the extra CSS on the client
                        .expires(new Date())
                        .build();

            } catch (FileNotFoundException | SecurityException ex) {
                Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
                throw new NotFoundException("!could not fetch extra CSS : \"" + cssPath + "\"");
            }

        } catch (IllegalArgumentException ex) {
            Logger.getLogger(QueryServices.class.getName()).log(Level.SEVERE, null, ex);
            throw new WebApplicationException(ex);

        }
    }
}
