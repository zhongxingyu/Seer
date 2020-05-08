 
 package com.zanoccio.axirassa.domain;
 
 import java.io.Serializable;
 import java.util.Set;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "Accounts")
 public class AccountModel implements Serializable {
 	private static final long serialVersionUID = -6937561064726878987L;
 
 	// ID
 	private Long id;
 
 
 	@Id
 	@GeneratedValue(strategy = GenerationType.AUTO)
 	public Long getId() {
 		return id;
 	}
 
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 
 	// USERS
 	private Set<UserModel> users;
 
 
 	@ManyToMany(targetEntity = UserModel.class, cascade = { CascadeType.PERSIST, CascadeType.MERGE })
 	@JoinTable(
 	        name = "Accounts_Users",
	        joinColumns = @JoinColumn(referencedColumnName = "EmployeeID"),
	        inverseJoinColumns = @JoinColumn(referencedColumnName = "AccountID"))
 	public Set<UserModel> getUsers() {
 		return users;
 	}
 
 
 	public void setUsers(Set<UserModel> users) {
 		this.users = users;
 	}
 }
