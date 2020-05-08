 package com.mariastore.core.domain;
 
 import javax.persistence.Entity;
 import javax.xml.bind.annotation.XmlRootElement;
 
 @Entity
 @XmlRootElement(name = "user")
 public class User {
 
 	private long id;
 
 	private String firstName;
 
 	private String lastName;
 
 	private String middleName;
 	
 	private String email;
 
 	public long getId() {
 		return id;
 	}
 
 	public void setId(long id) {
 		this.id = id;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public String getMiddleName() {
 		return middleName;
 	}
 
 	public void setMiddleName(String middleName) {
 		this.middleName = middleName;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 }
