 package org.aimas.craftingquest.state;
 
 import java.io.Serializable;
 
 /**
  * Shows how to construct an artifact.
  *
  */
 @SuppressWarnings("serial")
 public class Blueprint implements Serializable {
 	/**
 	 * the value of this blueprint
 	 */
 	int value;
 	
 	/**
 	 * the artifact it describes 
 	 */
 	CraftedObject describedObject;
 	
 	public Blueprint () {
 	}
 	
 	public Blueprint (int value, CraftedObject describedObject) {
 		this.value = value;
 		this.describedObject = describedObject;
 	}
 
 	public CraftedObject getDescribedObject() {
 		return describedObject;
 	}
 
 	public int getValue() {
 		return value;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (!(obj instanceof Blueprint)) {
 			return false;
 		}
 		
 		final Blueprint other = (Blueprint)obj;
 		if (describedObject.getType() != other.describedObject.getType()) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	@Override
	public int hashCode() {
		return describedObject.hashCode();
	}
	
	@Override
 	public String toString() {
 		String info = "";
 		
 		info += "DescribedObj: " + describedObject.getType().name() + "\n";
 		info += "    value: " + value + "\n";
 		
 		return info;
 	}
 }
