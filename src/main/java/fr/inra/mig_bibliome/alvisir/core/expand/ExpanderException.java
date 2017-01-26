package fr.inra.mig_bibliome.alvisir.core.expand;

/**
 * Exception thrown by query and text expanders.
 * @author rbossy
 *
 */
@SuppressWarnings("serial")
public class ExpanderException extends Exception {
	public ExpanderException() {
		super();
	}

	public ExpanderException(String message, Throwable cause) {
		super(message, cause);
	}

	public ExpanderException(String message) {
		super(message);
	}

	public ExpanderException(Throwable cause) {
		super(cause);
	}
}
