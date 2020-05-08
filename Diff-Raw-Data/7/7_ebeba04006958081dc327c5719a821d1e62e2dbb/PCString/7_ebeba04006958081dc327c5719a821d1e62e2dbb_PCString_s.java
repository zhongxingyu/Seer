 /**
  * 
  */
 package de.unisiegen.informatik.bs.alvis.primitive.datatypes;
 
 import java.util.Arrays;
 import java.util.List;
 
 
 /**
  * @author Dominik Dingel
  * 
  */
 
 public class PCString extends PCObject {
 	protected static final String TYPENAME = "String";
 
 	private String value;
 
 	/**
 	 * 
 	 * @param literal to create String from
 	 */
 	public PCString(String literal) {
 		value = literal;
 	}
 
 	/**
 	 * 
 	 * @return literal String
 	 */
 	public String getLiteralValue() {
 		return value;
 	}
 
 	/**
 	 * 
 	 * @param value set literal from String
 	 */
 	public void setLiteralValue(String value) {
 		this.value = value;
 	}
 
 	/**
 	 * 
 	 * @param value set from another PC String
 	 */
 	public void setValue(PCString value) {
 		this.value = value.getLiteralValue();
 	}
 
 	@Override
 	public String toString() {
 		return value;
 	}
 
 	@Override
 	public PCObject set(String memberName, PCObject value) {
 		if (memberName.isEmpty()) {
 			this.setValue((PCString) value);
 		}
 		return null;
 	}
 
 	@Override
 	public boolean equals(PCObject toCheckAgainst) {
 		try {
			return ((PCString) toCheckAgainst).value.equals(value);
 		} catch (ClassCastException e) {
 			return false;
 		}
 	}
 
 	public static String getTypeName() {
 		return PCString.TYPENAME;
 	}
 
 	public PCString add(PCString other) {
 		return new PCString(this.getLiteralValue() + other.getLiteralValue());
 	}
 
 	public PCBoolean equal(PCString other) {
 		return new PCBoolean(this.equals(other));
 	}
 
 	public PCBoolean notEqual(PCString other) {
 		return this.equal(other).not();
 	}
 	
 	public List<String> getMethods() {
 		String[] methods = { "add", "equal", "notEqual" };
 		return Arrays.asList(methods);
 	}
 }
