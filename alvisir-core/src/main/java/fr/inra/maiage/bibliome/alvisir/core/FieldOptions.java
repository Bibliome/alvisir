package fr.inra.maiage.bibliome.alvisir.core;

public interface FieldOptions {
	/**
	 * Returns the indexed fields to which the specified alias refers.
	 * Throws RuntimeException if the specified field name is neither an alias, nor an indexed field.
	 * @param fieldName
	 * @return
	 */
	public String[] getFields(String fieldName);
}
