/*
 *
 *      AlvisIR2 UI
 *
 *      Copyright Institut National de la Recherche Agronomique, 2013.
 *
 */
package fr.inra.mig_bibliome.alvisir.ui;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author fpapazian
 */
public class Configuration {

    //Parameters used in query uri
    public static final String QueryService_Path = "search";
    public static final String TabularOuputQueryService_Path = "tab";
    public static final String Query_QParamName = "q";
    public static final String StartSnippet_QParamName = "s";
    public static final String SnippetCount_QParamName = "n";
    public static final String StandardUITransformSheet_PathSuffix = "alvisir.xslt";
    public static final String TabularTransformSheet_PathSuffix = "tabular.xslt";
    public static final String MinimalTransformSheet_Path = "minimal/" + StandardUITransformSheet_PathSuffix;
    public static final String ExtraCSS_Path = "resources/css/extracss";

    //
    private static Document readConfig(ServletContext context, String uiConfigPath) {

        File configFile = new File(uiConfigPath);

        InputStream configIS = null;
        if (uiConfigPath == null) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "No configuration file specified!");
            return null;
        } else if (!configFile.isAbsolute()) {
            //non-absolute path refer to package resource
            Logger.getLogger(Configuration.class.getName()).log(Level.INFO, "retreiving internal config : {0}", uiConfigPath);
            configIS = context.getResourceAsStream("/" + uiConfigPath);

        } else if (configFile.exists()) {
            //absolute path refer to external file
            try {
                Logger.getLogger(Configuration.class.getName()).log(Level.INFO, "retreiving external config : {0}", configFile.getAbsolutePath());
                configIS = new FileInputStream(configFile);
            } catch (FileNotFoundException ex) {
            }
        }
        if (configIS == null) {
            //absolute path refer to external file
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Configuration file not found : {0}", configFile.getAbsolutePath());
            return null;
        }
        try {
            Document configDocument = XMLUtils.getDocumentBuilder().parse(configIS);
            configIS.close();
            return configDocument;
        } catch (SAXException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Can not read configuration in {0}", configFile.getAbsoluteFile());
            throw new RuntimeException(ex);
        } catch (IOException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Can not read configuration in {0}", configFile.getAbsoluteFile());
            throw new RuntimeException(ex);
        }
    }
    //
    private final Document configDocument;

    public Configuration(ServletContext context, String uiConfigPath) {
        configDocument = readConfig(context, uiConfigPath);
    }

    public String getSearchConfigPath() {
        return getNodeAttributeValue("/alvisir-ui-config/alvisir-search", "filepath", true);
    }

    public String getAlternateStyleSheetPath() {
        return getNodeAttributeValue("/alvisir-ui-config/alternate-stylesheet", "filepath", false);
    }

    public String getExtraCSSPath() {
        return getNodeAttributeValue("/alvisir-ui-config/extra-css", "filepath", false);
    }

    public Map<String, String> getDisplayParams() {
        return getNodesAttributeValue("/alvisir-ui-config/display-params/param", "name", "value");
    }

    public String getOntologyPath(String ontoName) {
        return getNodeAttributeValue("/alvisir-ui-config/resources/ontology[@name='" + ontoName + "']", "filepath", false);
    }

    public String getOntologyExpanderKey(String ontoName) {
        return getNodeAttributeValue("/alvisir-ui-config/resources/ontology[@name='" + ontoName + "']", "expander-key", false);
    }

    public List<String> getOntologyNames() {
        return getNodesAttributeValue("/alvisir-ui-config/resources/ontology", "name");
    }

    // -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- -- 
    private String getNodeAttributeValue(String path, String attributeName, boolean failIfNotFound) {
        String attributeValue = null;

        XPath xPath = XPathFactory.newInstance().newXPath();

        try {
            Node node = (Node) xPath.evaluate(path, configDocument.getDocumentElement(), XPathConstants.NODE);

            if (node == null) {
                if (failIfNotFound) {
                    Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Invalid configuration : could not find " + path);
                    throw new IllegalArgumentException("Invalid configuration : could not find " + path);
                } else {
                    return null;
                }
            }
            Element e = (Element) node;
            if (e.hasAttribute(attributeName)) {
                attributeValue = e.getAttribute(attributeName);
                if (failIfNotFound && attributeValue == null) {
                    Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "could not find " + path + "/@" + attributeName);
                    throw new IllegalArgumentException("could not find " + path + "/@" + attributeName);
                }
            }
            return attributeValue;

        } catch (XPathExpressionException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Error while reading configuration: " + path + "/@" + attributeName);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private List<String> getNodesAttributeValue(String path, String valueAttributeId) {
        List<String> attributeValueList = new ArrayList<>();
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            NodeList nodes = (NodeList) xPath.evaluate(path, configDocument.getDocumentElement(), XPathConstants.NODESET);
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element e = (Element) nodes.item(i);
                    if (e.hasAttribute(valueAttributeId)) {
                        String attributeValue = e.getAttribute(valueAttributeId);
                        if (attributeValue != null) {

                            attributeValueList.add(attributeValue);
                        }
                    }
                }
            }
            return attributeValueList;
        } catch (XPathExpressionException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Error while reading configuration: " + path);
            throw new RuntimeException(ex.getMessage());
        }
    }

    private Map<String, String> getNodesAttributeValue(String path, String nameAttributeId, String valueAttributeId) {
        Map<String, String> attributeValueMap = new HashMap<>();

        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            NodeList nodes = (NodeList) xPath.evaluate(path, configDocument.getDocumentElement(), XPathConstants.NODESET);
            if (nodes != null) {
                for (int i = 0; i < nodes.getLength(); i++) {
                    Element e = (Element) nodes.item(i);
                    if (e.hasAttribute(valueAttributeId)) {
                        String attributeValue = e.getAttribute(valueAttributeId);
                        if (attributeValue != null) {
                            String nodeName = e.getAttribute(nameAttributeId);
                            if (nodeName != null) {
                                attributeValueMap.put(nodeName, attributeValue);
                            }
                        }
                    }
                }
            }
            return attributeValueMap;

        } catch (XPathExpressionException ex) {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "Error while reading configuration: " + path);
            throw new RuntimeException(ex.getMessage());
        }
    }
}
