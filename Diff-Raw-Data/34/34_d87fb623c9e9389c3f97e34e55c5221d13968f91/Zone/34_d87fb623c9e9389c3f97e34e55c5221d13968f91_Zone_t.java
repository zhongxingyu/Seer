 package org.endeios.penguineye.domain;
 
 import java.io.Serializable;
 import java.lang.Long;
 import java.text.MessageFormat;
import java.util.Set;
 
 import javax.persistence.*;
 
import org.codehaus.jackson.annotate.JsonBackReference;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonManagedReference;
 import org.endeios.penguineye.domain.Penguin;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.junit.Ignore;
 
 import antlr.collections.List;
 
 /**
  * Entity implementation class for Entity: Zone
  * 
  */
 @Entity
 public class Zone implements Serializable {
 
 	@Id
	@GeneratedValue
 	private Long id;
	
	@JsonBackReference
 	@ManyToOne(optional = false, fetch = FetchType.LAZY)
 	private Penguin penguin;
 	
 	@Column
 	private String name;
 
	@OneToMany(mappedBy = "zone",fetch=FetchType.EAGER)
	private java.util.Set<Fish> fishes;
 
 	private static final long serialVersionUID = 1L;
 
 	public Zone() {
 		super();
 	}
 
 	public Long getId() {
 		return this.id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	public Penguin getPenguin() {
 		return this.penguin;
 	}
 
 	public void setPenguin(Penguin penguin) {
 		this.penguin = penguin;
 	}
 
	public Set<Fish> getFishes() {
 		return fishes;
 	}
 
	public void setFishes(Set<Fish> fishes) {
 		this.fishes = fishes;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	@Override
 	public String toString() {
 		return MessageFormat.format("[({0}) - {1}]", id,name); 
 	}
 
 }
