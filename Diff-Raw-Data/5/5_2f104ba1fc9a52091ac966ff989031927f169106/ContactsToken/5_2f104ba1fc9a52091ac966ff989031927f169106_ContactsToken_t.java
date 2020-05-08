 package org.cvut.wa2.projectcontrol.entities;
 
 import java.io.Serializable;
 
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 @PersistenceCapable
 public class ContactsToken implements Serializable {
 	
 	@PrimaryKey
 	private String email;
 	
 	@Persistent
 	private String token;
 	
 	@Persistent
 	private String tokenSecret;
 	
 	public String getEmail() {
 		return email;
 	}
 	
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getToken() {
 		return token;
 	}
 
 	public void setToken(String token) {
 		this.token = token;
 	}
 
 	public String getTokenSecret() {
 		return tokenSecret;
 	}
 
 	public void setTokenSecret(String tokenSecret) {
 		this.tokenSecret = tokenSecret;
 	}
 }
