 package org.endeios.penguineye.domain;
 
 import java.io.Serializable;
 import java.lang.Long;
 import java.text.MessageFormat;
 
 import javax.persistence.*;
 
 import org.endeios.penguineye.domain.Penguin;
 
 import antlr.collections.List;
 
 /**
  * Entity implementation class for Entity: Zone
  * 
  */
 @Entity
 public class Zone implements Serializable {
 
 	@Id
 	private Long id;
 	@ManyToOne(optional = false, fetch = FetchType.LAZY)
 	private Penguin penguin;
 	
 	@Column
 	private String name;
 
	@OneToMany(mappedBy = "zone")
	private java.util.List<Fish> fishes;
 
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
 
	public java.util.List<Fish> getFishes() {
 		return fishes;
 	}
 
	public void setFishes(java.util.List<Fish> fishes) {
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
