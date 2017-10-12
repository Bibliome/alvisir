package fr.inra.maiage.bibliome.alvisir.core.snippet;

/**
 * Field reference.
 * @author rbossy
 *
 */
public class FieldReference implements Comparable<FieldReference> {
	private final String fieldName;
	private final int fieldInstance;
	
	/**
	 * Creates a field reference with the specified name and instance number.
	 * @param fieldName field name.
	 * @param fieldInstance field instance number.
	 */
	public FieldReference(String fieldName, int fieldInstance) {
		super();
		this.fieldName = fieldName;
		this.fieldInstance = fieldInstance;
	}

	/**
	 * Returns the field name.
	 * @return the field name.
	 */
	public String getFieldName() {
		return fieldName;
	}

	/**
	 * Returns the field instance number.
	 * @return the field instance number.
	 */
	public int getFieldInstance() {
		return fieldInstance;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + fieldInstance;
		result = prime * result + ((fieldName == null) ? 0 : fieldName.hashCode());
		return result;
	}

	@Override
	public int compareTo(FieldReference o) {
		if (fieldName.equals(o.fieldName)) {
			return Integer.compare(fieldInstance, o.fieldInstance);
		}
		return fieldName.compareTo(o.fieldName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FieldReference other = (FieldReference) obj;
		if (fieldInstance != other.fieldInstance)
			return false;
		if (fieldName == null) {
			if (other.fieldName != null)
				return false;
		}
		else if (!fieldName.equals(other.fieldName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return fieldName + "/" + fieldInstance;
	}
}
