 package uk.ac.ox.oucs.oxam.model;
 
 import java.io.Serializable;
 
 /**
  * These remain reasonably static but need to be editable.
  * @author buckett
  *
  */
 public class Category implements Serializable {
 
 	private final String name;
 	private final String code;
 	
 	public Category(String code, String name) {
 		this.name = name;
 		this.code = code;
 	}
 	
 	public String getName() {
 		return name;
 	}
 	
 	public String getCode() {
 		return code;
 	}	
 }
