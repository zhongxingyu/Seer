 package net.croxis.plugins.civilmineation.components;
 
import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.MappedSuperclass;
import javax.persistence.OneToOne;
 
 @MappedSuperclass
 public abstract class Component {
 	@Id
 	private int id;
	@OneToOne(fetch = FetchType.LAZY)
 	private Ent entityID;
 	private String name;
 	public int getId() {
 		return id;
 	}
 	public void setId(int id) {
 		this.id = id;
 	}
 	public Ent getEntityID() {
 		return entityID;
 	}
 	public void setEntityID(Ent entityID) {
 		this.entityID = entityID;
 	}
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 }
