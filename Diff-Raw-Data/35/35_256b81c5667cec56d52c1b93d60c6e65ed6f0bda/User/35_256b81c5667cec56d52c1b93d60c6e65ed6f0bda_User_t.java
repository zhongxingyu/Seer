 package com.seventysevenagency.chat.domain;
 
 import java.util.Set;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.OneToMany;
 import javax.persistence.Table;
 
 @Entity
 @Table(name = "users")
 public class User {
 	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
 	@Column(name = "id", unique = true, nullable = false)
 	private int id;
 	@Column(name = "name",  nullable = false)
 	private String name;
 	@Column(name = "surname", nullable = false)
 	private String surname;
 	@Column(name = "username", nullable = false)
 	private String username;
 	@Column(name = "password", nullable = false)
 	private String password;
 	@Column(name = "email", nullable = false)
 	private String email;
 	@OneToMany(mappedBy="user")
 	private Set<Message> messages;	
 	
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 	public String getSurname() {
 		return surname;
 	}
 
 	public void setSurname(String surname) {
 		this.surname = surname;
 	}
 	
 	public String getUsername() {
 		return username;
 	}
 
 	public void setUsername(String username) {
 		this.username = username;
 	}
 	
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public Set<Message> getMessages() {
 		return messages;
 	}
 
 	public void setMessages(Set<Message> messages) {
 		this.messages = messages;
 	}
 }
