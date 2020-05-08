 package com.cit.eugene.model;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 import javax.validation.constraints.NotNull;
 
 /**
  * Authorities.
  * 
  * @author ebell
  */
 @Entity
@Table(name = "AUTHORITIES")
 public class Authorities {
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "AUTHORITIESID")
 	private Integer authoritiesId;
 	
 	@ManyToOne(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
 	@JoinColumn(name = "USERNAME")
 	@NotNull
 	private User users;
 	
 	@Column(name = "AUTHORITY")
 	@NotNull
 	private String authority;
 
 	/** default constructor */
 	public Authorities() {
 	}
 
 	public User getUsers() {
 		return this.users;
 	}
 
 	public void setUsers(User users) {
 		this.users = users;
 	}
 
 	public String getAuthority() {
 		return this.authority;
 	}
 
 	public void setAuthority(String authority) {
 		this.authority = authority;
 	}
 
 	public Integer getAuthoritiesId() {
 		return authoritiesId;
 	}
 
 	public void setAuthoritiesId(Integer authoritiesId) {
 		this.authoritiesId = authoritiesId;
 	}
 }
