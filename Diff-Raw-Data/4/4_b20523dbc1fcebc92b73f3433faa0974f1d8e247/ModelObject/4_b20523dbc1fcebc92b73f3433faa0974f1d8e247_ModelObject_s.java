 package de.freiburg.uni.iig.sisi.model;
 
 /**
  * 
  * Small "helper" to quickly have model objects with an id and name.
  * @author Sebastian
  *
  */
 public abstract class ModelObject {
 	
 	private String id;
 	private String name;
 	
 	public ModelObject() {
 		this.id = "";
 		this.name = "";
 	}
 	
 	public ModelObject(String id, String name) {
 		this.id = id;
 		this.name = name;
 	}
 	
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public String toString() {
		if (name != null) return name;
		if (id != null) return id;
 		return super.toString();
 	}
 
 	
 	
 }
