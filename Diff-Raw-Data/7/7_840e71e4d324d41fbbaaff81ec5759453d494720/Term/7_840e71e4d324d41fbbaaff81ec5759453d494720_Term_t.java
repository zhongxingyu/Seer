 package uk.ac.ox.oucs.oxam.model;
 
 import java.io.Serializable;
 
 public class Term  implements Serializable{
 
	private static final long serialVersionUID = 1L;
 	private final String code;
 	private final String name;
 	
 	public Term(String code, String name) {
 		this.code = code;
 		this.name = name;
 	}
 
 	public String getCode() {
 		return code;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public String toString() {
 		return "Term [code=" + code + ", name=" + name + "]";
 	}
 
 }
