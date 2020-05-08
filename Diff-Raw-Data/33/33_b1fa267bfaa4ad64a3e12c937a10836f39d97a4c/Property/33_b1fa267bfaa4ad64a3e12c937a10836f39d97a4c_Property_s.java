 package de.hackerspacebremen.data.entities;
 
 import com.googlecode.objectify.annotation.Id;
 import com.googlecode.objectify.annotation.Index;
 
 import de.hackerspacebremen.data.annotations.Entity;
 import de.hackerspacebremen.data.annotations.FormatPart;
 
 @Entity(name="Property")
 @com.googlecode.objectify.annotation.Entity
 public final class Property implements BasicEntity{
 
 	@Id
 	@FormatPart(key="P1")
 	private Long id;
 	
 	@Index
 	@FormatPart(key="P2")
 	private String key;
 	
 	@FormatPart(key="P3")
 	private String value;
 
 	@Override
 	public Long getId() {
 		return id;
 	}
 
 	@Override
 	public void setId(final Long id) {
 		this.id = id;
 	}
 	
 	public String getKey() {
 		return key;
 	}
 
 	public void setKey(String key) {
 		this.key = key;
 	}
 
 	public String getValue() {
 		return value;
 	}
 
 	public void setValue(String value) {
 		this.value = value;
 	}
 }
