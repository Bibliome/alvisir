/*
 *
 *      AlvisIR2 UI
 *
 *      Copyright Institut National de la Recherche Agronomique, 2013.
 *
 */
package fr.inra.mig_bibliome.alvisir.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author fpapazian
 */
public class XSLTStyleSheetProducer {

    //path of the stylesheet resource templates used to transform produced XML in HTML on the client
    private static final String Xml2StandardUI_stylesheet = "/xslt/alvisir.xslt";
    private static final String Xml2TabularOutput_stylesheet = "/xslt/tabular.xslt";
    //styleSheet templates loaded in memory
    private static Document standardUITransformSheetTemplate;
    private static Document tabularOuputTransformSheetTemplate;
    //Name of dynamic parameters of the stylesheet templates
    private static final String PageSize_XslParam = "pageSize";
    private static final String PageNumber_XslParam = "pageNumber";
    private static final String StartSnippet_XslParam = "startSnippet";
    private static final String SearchUrl_XslParam = "search-url";
    private static final String TabularUrl_XslParam = "tabular-url";
    private static final String QueryParam_XslParam = "query-param";
    private static final String StartSnippetParam_XslParam = "start-snippet-param";
    private static final String SnippetCountParam_XslParam = "snippet-count-param";
    private static final String OntologyNamesParam_XslParam = "onto-names";
    private static final String UseExtraCss_XslParam = "extrastyle-url";

    private static Document getXmlEmbeddedResource(ServletContext context, String xmlResourcePath) {

        try {
            try (InputStream xmlResourceIS = context.getResourceAsStream(xmlResourcePath)) {
                return XMLUtils.getDocumentBuilder().parse(xmlResourceIS);
            }
        } catch (SAXException | IOException ex) {
            Logger.getLogger(XSLTStyleSheetProducer.class.getName()).log(Level.SEVERE, "Can not read embedded xml (stylesheet template) in {0}", xmlResourcePath);
            throw new RuntimeException(ex);
        }
    }

    private static Document getRawStandardUITransformSheet(ServletContext context) {
        if (standardUITransformSheetTemplate == null) {
            standardUITransformSheetTemplate = getXmlEmbeddedResource(context, Xml2StandardUI_stylesheet);
        }
        return XMLUtils.cloneDocument(standardUITransformSheetTemplate);
    }

    private static Document getRawTabularOutputTransformSheet(ServletContext context) {
        if (tabularOuputTransformSheetTemplate == null) {
            tabularOuputTransformSheetTemplate = getXmlEmbeddedResource(context, Xml2TabularOutput_stylesheet);
        }
        return XMLUtils.cloneDocument(tabularOuputTransformSheetTemplate);
    }

    private static Document getUIAlternateTransformSheet(Configuration uiConfig) throws SAXException, IOException {
        String path = uiConfig.getAlternateStyleSheetPath();
        if (path == null) {
            return null;
        } else {
            return XMLUtils.getDocumentBuilder().parse(new File(path));
        }

    }

    public static String getMinimalStandardUITransformSheet(ServletContext context) {
        return XMLUtils.documentToString(getRawStandardUITransformSheet(context));
    }

    public static String getStandardUITransformSheet(ServletContext context, Configuration uiConfig, int startSnippet, int snippetCount) {
        Document transformSheet = null;
        try {
            transformSheet = getUIAlternateTransformSheet(uiConfig);

        } catch (IOException | SAXException ex) {
            Logger.getLogger(XSLTStyleSheetProducer.class.getName()).log(Level.SEVERE, "Could not process alternate transform stylesheet", ex);
            throw new RuntimeException(ex);
        }
        if (transformSheet == null) {
            transformSheet = getRawStandardUITransformSheet(context);
        }

        parametrizeTransformSheet(transformSheet, uiConfig, startSnippet, snippetCount);
        return XMLUtils.documentToString(transformSheet);
    }

    public static String getTabularOutputransformSheet(ServletContext context, Configuration uiConfig, int startSnippet, int snippetCount) {
        Document transformSheet = getRawTabularOutputTransformSheet(context);
        parametrizeTransformSheet(transformSheet, uiConfig, startSnippet, snippetCount);
        return XMLUtils.documentToString(transformSheet);
    }

    private static void parametrizeTransformSheet(Document transformSheet, Configuration uiConfig, int startSnippet, int snippetCount) {
        Map<String, String> params = uiConfig.getDisplayParams();

        //retrieve xslt param and set the dynamic value
        XPath xPath = XPathFactory.newInstance().newXPath();
        try {
            NodeList paramNodes = (NodeList) xPath.evaluate("/stylesheet/param", transformSheet.getDocumentElement(), XPathConstants.NODESET);
            for (int i = 0; i < paramNodes.getLength(); ++i) {
                Element e = (Element) paramNodes.item(i);

                String paramName = e.getAttribute("name");
                if (PageSize_XslParam.equals(paramName)) {
                    e.setTextContent(String.valueOf(snippetCount));
                } else if (PageNumber_XslParam.equals(paramName)) {
                    int pageNumber = 1 + (1 + startSnippet) / snippetCount;
                    e.setTextContent(String.valueOf(pageNumber));
                } else if (StartSnippet_XslParam.equals(paramName)) {
                    e.setTextContent(String.valueOf(startSnippet));
                } else if (SearchUrl_XslParam.equals(paramName)) {
                    e.setTextContent(Configuration.QueryService_Path);    
                } else if (TabularUrl_XslParam.equals(paramName)) {
                    e.setTextContent(Configuration.TabularOuputQueryService_Path);
                } else if (QueryParam_XslParam.equals(paramName)) {
                    e.setTextContent(Configuration.Query_QParamName);
                } else if (StartSnippetParam_XslParam.equals(paramName)) {
                    e.setTextContent(Configuration.StartSnippet_QParamName);
                } else if (SnippetCountParam_XslParam.equals(paramName)) {
                    e.setTextContent(Configuration.SnippetCount_QParamName);
                } else if (params.containsKey(paramName)) {
                    e.setTextContent(params.get(paramName));
                } else if (OntologyNamesParam_XslParam.equals(paramName)) {
                    StringBuilder joined = new StringBuilder();
                    for (String name : uiConfig.getOntologyNames()) {
                        joined.append(",").append(name);
                    }
                    if (joined.length() > 0) {
                        e.setTextContent(joined.substring(1));
                    }
                } else if (UseExtraCss_XslParam.equals(paramName)) {
                    e.setTextContent(Configuration.ExtraCSS_Path);
                }


            }
        } catch (XPathExpressionException ex) {
            Logger.getLogger(XSLTStyleSheetProducer.class
                    .getName()).log(Level.SEVERE, "Can not create dynamic stylesheet");
            throw new RuntimeException(ex);
        }
    }
}
