 package org.huamuzhen.oa.domain.entity;
 
 import java.io.Serializable;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "Key_Value_Pair")
 public class KeyValuePair implements Serializable{
 	
 	private static final long serialVersionUID = 1L;
 
 	@Id
	@Column(name="key")
 	private String key;
 	
	@Column(name="value")
 	private String value;
 
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
