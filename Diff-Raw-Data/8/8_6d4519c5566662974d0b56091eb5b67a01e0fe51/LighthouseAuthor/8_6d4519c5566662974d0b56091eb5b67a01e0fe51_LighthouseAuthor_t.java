 package edu.uci.lighthouse.model;
 
import java.io.Serializable;

 import javax.persistence.Entity;
 import javax.persistence.Id;
 
 /**
  * @author tproenca
  */
 @Entity
public class LighthouseAuthor implements Serializable{
 
	private static final long serialVersionUID = 4952522633654542472L;
	
 	@Id
 	private String name;
 	
 	/**
 	 * @param name
 	 */
 	public LighthouseAuthor(String name) {
 		this.name = name;
 	}
 
 	protected LighthouseAuthor() {
 		this("");
 	}
 
 	/**
 	 * @return the name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	@Override
 	public String toString() {
 		return name;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		return result;
 	}
 	
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		LighthouseAuthor other = (LighthouseAuthor) obj;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		return true;
 	}
 	
 }
