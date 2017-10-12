package fr.inra.maiage.bibliome.alvisir.core.expand.index;

import fr.inra.maiage.bibliome.alvisir.core.expand.ExpanderException;

@SuppressWarnings("serial")
public class ExpanderIndexerException extends ExpanderException {
	public ExpanderIndexerException() {
		super();
	}

	public ExpanderIndexerException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExpanderIndexerException(String message) {
		super(message);
	}

	public ExpanderIndexerException(Throwable cause) {
		super(cause);
	}
}
