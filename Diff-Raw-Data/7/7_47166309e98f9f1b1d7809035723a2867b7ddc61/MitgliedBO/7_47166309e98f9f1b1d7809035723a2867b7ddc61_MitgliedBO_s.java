 package de.oose.ep.shared;
 
 import java.io.Serializable;
 
 import javax.validation.constraints.Size;
 
 public class MitgliedBO implements Serializable {
 
 	@Size(min = 1, message = "Der Name darf nicht leer sein.")
 	private String name;
 	@Size(min = 1, message = "Der Vorname darf nicht leer sein.")
 	private String vorname;
 	private Long id;
 
 	public MitgliedBO() {
 
 	}
 
 	public MitgliedBO(String name, String vorname) {
 		this.name = name;
 		this.vorname = vorname;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name.trim();
 	}
 
 	public String getVorname() {
 		return vorname;
 	}
 
 	public void setVorname(String vorname) {
 		this.vorname = vorname.trim();
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((name == null) ? 0 : name.hashCode());
 		result = prime * result + ((vorname == null) ? 0 : vorname.hashCode());
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
 		MitgliedBO other = (MitgliedBO) obj;
 		if (name == null) {
 			if (other.name != null)
 				return false;
 		} else if (!name.equals(other.name))
 			return false;
 		if (vorname == null) {
 			if (other.vorname != null)
 				return false;
 		} else if (!vorname.equals(other.vorname))
 			return false;
 		return true;
 	}
 
 }
