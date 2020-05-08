 package org.aimas.craftingquest.state;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Set;
 
 /**
  * Description of a crafted object - value and means of construction. 
  * There are two types of crafted objects:
  * <ul>
  * <li>simple objects - are constructed only from basic resources</li>
  * <li>complex objects - are constructed only from other objects </li>
  * </ul>
  *
  */
 @SuppressWarnings("serial")
 public class CraftedObject implements Serializable {
 	public enum BasicResourceType {
 		R1, R2, R3, R4, R5, R6, R7, R8, R9, R10, R11, R12
 	}
 	
 	public enum ObjectType {
 		O1, O2, O3, O4, O5, O6, O7, O8, O9, O10, O11, O12
 	}
 	
 	/**
 	 * the object type
 	 */
 	private ObjectType type;
 	
 	/**
 	 * the object value
 	 */
 	private int value;
 	
 	/**
 	 * If not null, this field gives a list of alternative ways of constructing a complex object.<br/>
 	 * Each alternative is a mapping between <code>CraftedObject</code>s and their required quantities. 
 	 */
 	private List<HashMap<CraftedObject, Integer>> requiredObjects;
 	
 	/**
 	 * If not null, this field gives a list of alternative ways of constructing a simple object. <br/>
 	 * Each alternative is a mapping between <code>BasicResourceType</code>s and their required quantities.
 	 */
 	private List<HashMap<BasicResourceType, Integer>> requiredResources;
 	
 	public CraftedObject() {
 	}
 	
 	public CraftedObject(ObjectType type, int value, List<HashMap<CraftedObject, Integer>> requiredObjects, List<HashMap<BasicResourceType, Integer>> requiredResources) {
 		this.type = type;
 		this.value = value;
 		this.requiredObjects = requiredObjects;
 		this.requiredResources = requiredResources;
 	}
 
 	public List<HashMap<CraftedObject, Integer>> getRequiredObjects() {
 		return requiredObjects;
 	}
 
 	public List<HashMap<BasicResourceType, Integer>> getRequiredResources() {
 		return requiredResources;
 	}
 		
 	
 	public List<List<CraftedObject>> getRequiredObjectsList() {
 		List<List<CraftedObject>> list = new ArrayList<List<CraftedObject>>();
 		
 		for (HashMap<CraftedObject, Integer> alternative : requiredObjects) {
 			Set<CraftedObject> keys = alternative.keySet();
 			ArrayList<CraftedObject> keysArray = new ArrayList<CraftedObject>(keys);
 			
 			list.add(keysArray);
 		}
 		
 		return list;
 	}
 	
 	public ObjectType getType() {
 		return type;
 	}
 
 	public int getValue() {
 		return value;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if(!(obj instanceof CraftedObject)) {
 			return false;
 		}
 		
 		final CraftedObject other = (CraftedObject)obj;
 		
 		if (this.type != other.type) {
 			return false;
 		}
 		
 		if (this.value != other.value) {
 			return false;
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public String toString() {
 		String info = "";
 		
 		info += "Obj: " + type.name() + "\n";
 		info += "    value: " + value + "\n";
 		
 		if (requiredResources != null && !requiredResources.isEmpty()) {
 			info += "    construction alternatives \n"; 
 			for (HashMap<BasicResourceType, Integer> alternative : requiredResources) {
 				info += "        ";
 				for (BasicResourceType br : alternative.keySet()) {
 					info += br.name() + ":" + alternative.get(br) + " "; 
 				}
 				info += "\n";
 			}
 		}
 		
 		if (requiredObjects != null && !requiredObjects.isEmpty()) {
 			info += "    construction alternatives \n"; 
 			for (HashMap<CraftedObject, Integer> alternative : requiredObjects) {
 				info += "        ";
 				for (CraftedObject o : alternative.keySet()) {
 					info += o.type.name() + ":" + alternative.get(o) + " "; 
 				}
 				info += "\n";
 			}
 		}
 		
 		return info;
 	}
 }
