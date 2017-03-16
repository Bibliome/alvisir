package fr.inra.mig_bibliome.alvisir.core.expand;

import java.io.IOException;
import java.util.Properties;

/**
 * A text expander that never expands.
 * Using this object for query expansion emulates the classical SE behaviour.
 * @author rbossy
 *
 */
public enum NullTextExpander implements TextExpander, TextExpansionResult {
	INSTANCE;

	@Override
	public TextExpansionResult searchExpansion(String text) throws IOException, ExpanderException {
		return this;
	}

	@Override
	public TextExpansionResult searchCanonical(String text) throws IOException, ExpanderException {
		return this;
	}

	@Override
	public ExplanationFactory getExplanationFactory(String type) throws ExpanderException {
		return null;
	}

	@Override
	public void setExplanationFactory(String type, Properties properties) throws ExpanderException {
	}

	@Override
	public boolean any() {
		return false;
	}

	@Override
	public boolean next() throws IOException, ExpanderException {
		return false;
	}

	@Override
	public String getType() {
		return null;
	}

	@Override
	public String getCanonical() {
		return null;
	}

	@Override
	public String[] getSynonyms() {
		return null;
	}

	@Override
	public String getLabel() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getProperty(String key) {
		return null;
	}
}
