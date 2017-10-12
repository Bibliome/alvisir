package fr.inra.maiage.bibliome.alvisir.core.expand;

import java.io.IOException;
import java.util.Properties;


public interface TextExpander {
	TextExpansionResult searchExpansion(String text) throws IOException, ExpanderException;
	TextExpansionResult searchCanonical(String text) throws IOException, ExpanderException;
	ExplanationFactory getExplanationFactory(String type) throws ExpanderException;
	void setExplanationFactory(String type, Properties properties) throws ExpanderException;
	String getProperty(String key) throws IOException;
}
