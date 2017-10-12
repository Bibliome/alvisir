package fr.inra.maiage.bibliome.alvisir.core.expand;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

public class CompoundTextExpansionResult implements TextExpansionResult {
	private final Iterator<TextExpansionResult> iterator;
	private TextExpansionResult current;

	public CompoundTextExpansionResult(Iterator<TextExpansionResult> iterator) {
		super();
		this.iterator = iterator;
		if (iterator.hasNext()) {
			current = iterator.next();
		}
		else {
			current = null;
		}
	}
	
	public CompoundTextExpansionResult(Collection<TextExpansionResult> collection) {
		this(collection.iterator());
	}
	
	public CompoundTextExpansionResult(TextExpansionResult... array) {
		this(Arrays.asList(array));
	}

	@Override
	public boolean any() {
		if (current == null) {
			return false;
		}
		return current.any();
	}

	@Override
	public boolean next() throws IOException, ExpanderException {
		if (current == null) {
			return false;
		}
		while (!current.next()) {
			if (!iterator.hasNext()) {
				return false;
			}
			current = iterator.next();
		}
		return true;
	}
	
	@Override
	public String getType() {
		return current.getType();
	}
	
	@Override
	public String getCanonical() {
		return current.getCanonical();
	}
	
	@Override
	public String[] getSynonyms() {
		return current.getSynonyms();
	}
	
	@Override
	public String getLabel() {
		return current.getLabel();
	}
}
