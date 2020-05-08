 package com.cit.eugene.model;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 import javax.persistence.UniqueConstraint;
 import javax.validation.constraints.NotNull;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 
 /**
  * User.
  * 
  * @author ebell
  */
 @Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = { "username" }))
 public class User {
 
 	@Id
 	@Column(name = "USERNAME")
 	@NotNull
 	private String username;
 	
 	@Column(name = "PASSWORD")
 	@NotNull
 	private String password;
 	
 	@Column(name = "ENABLED")
 	@NotNull
 	private boolean enabled;
 	
 	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "users", orphanRemoval = true)
 	private Set<Authorities> authoritieses = new HashSet<Authorities>();
 
 	public User() {
 	}
 
 	public String getUsername() {
 		return this.username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 
 	public String getPassword() {
 		return this.password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public boolean isEnabled() {
 		return this.enabled;
 	}
 
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 
 	// To stop JSON going nuts on recursion
 	@JsonIgnore
 	public Set<Authorities> getAuthoritieses() {
 		return this.authoritieses;
 	}
 
 	public void setAuthoritieses(Set<Authorities> authoritieses) {
 		this.authoritieses = authoritieses;
 	}
 }
