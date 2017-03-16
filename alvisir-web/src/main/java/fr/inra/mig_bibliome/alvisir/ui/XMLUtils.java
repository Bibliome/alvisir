/*
 *
 *      AlvisIR2 UI
 *
 *      Copyright Institut National de la Recherche Agronomique, 2013.
 *
 */
package fr.inra.mig_bibliome.alvisir.ui;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 *
 * @author fpapazian
 */
public class XMLUtils {

    private static final DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    private static final DocumentBuilder documentBuilder;

    static {
        DocumentBuilder docBuilder = null;
        try {
            docBuilder = builderFactory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        documentBuilder = docBuilder;
    }

    public static DocumentBuilder getDocumentBuilder() {
        return documentBuilder;
    }

    public static Document cloneDocument(Document originalDocument) {
        Document clonedDocument = getDocumentBuilder().newDocument();
        Node originalRoot = originalDocument.getDocumentElement();
        Node copiedRoot = clonedDocument.importNode(originalRoot, true);
        clonedDocument.appendChild(copiedRoot);
        return clonedDocument;
    }

    public static String documentToString(Document document) {
        try (StringWriter writer = new StringWriter()) {
            org.bibliome.util.xml.XMLUtils.writeDOMToFile(document, null, writer);
            writer.flush();
            return writer.toString();
        } catch (TransformerFactoryConfigurationError | IOException ex) {
            Logger.getLogger(XMLUtils.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
