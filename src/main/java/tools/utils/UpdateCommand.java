package tools.utils;

/**
 * This class is used by {@link TestBuilder} and {@link TestWritter} to make and write update statements of the form
 * {@code updateValue(objectName, fieldName, value)}
 * 
 * @author Simon Emmanuel Gutierrez Brida
 * @version 0.2u
 */
public class UpdateCommand implements Comparable<UpdateCommand>{
	
	private String objectName;
	private String fieldName;
	private String value;
	
	public UpdateCommand(String objectName, String fieldName, String value) {
		this.objectName = objectName;
		this.fieldName = fieldName;
		this.value = value;
	}
	
	public String getObjectName() {
		return this.objectName;
	}
	
	public String getFieldName() {
		return this.fieldName;
	}
	
	public String getValue() {
		return this.value;
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof UpdateCommand)) return false;
		if (this == other) return true;
		UpdateCommand otherUpdateCommand = (UpdateCommand) other;
		boolean sameObjectName = (this.objectName == otherUpdateCommand.objectName) || ((this.objectName != null && otherUpdateCommand.objectName != null) && (this.objectName.compareTo(otherUpdateCommand.objectName)==0));
		boolean sameFieldName = (this.fieldName == otherUpdateCommand.fieldName) || ((this.fieldName != null && otherUpdateCommand.fieldName != null) && (this.fieldName.compareTo(otherUpdateCommand.fieldName)==0));
		boolean sameValue = (this.value == otherUpdateCommand.value) || ((this.value != null && otherUpdateCommand.value != null) && (this.value.compareTo(otherUpdateCommand.value)==0));
		return sameObjectName && sameFieldName && sameValue;
	}

	@Override
	public int compareTo(UpdateCommand other) {
		if (other == null) throw new NullPointerException("object to compare is null");
		if (this.objectName.compareTo(other.objectName) == 0) {
			if (this.fieldName.compareTo(other.fieldName) == 0) {
				if (this.value.compareTo(other.value) == 0) {
					return 0;
				} else {
					return this.value.compareTo(other.value);
				}
			} else {
				return this.fieldName.compareTo(other.fieldName) * 10;
			}
		} else {
			return this.objectName.compareTo(other.objectName) * 100;
		}
	}
}
