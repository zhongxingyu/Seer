 package com.quuzz.doors.server;
 
 import java.io.Serializable;
 import java.util.Collection;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.FetchType;
 import javax.persistence.GeneratedValue;
 import javax.persistence.GenerationType;
 import javax.persistence.Id;
 import javax.persistence.JoinColumn;
 import javax.persistence.JoinTable;
 import javax.persistence.ManyToMany;
 import javax.persistence.OneToMany;
 
 @Entity(name="user_t")
 public class User implements Serializable {
 	private static final long serialVersionUID = 1374170403269277391L;
 	@Id
 	@Column(nullable = false)
 	@GeneratedValue(strategy = GenerationType.IDENTITY)	
 	private Long id;
 	@OneToMany(mappedBy="user")
 	private Collection<DoorGroup> doorGroups;
 	private String contactLine;
 	private String login;
 	private String password;
	@ManyToMany
 	@JoinTable(name="user_doors", joinColumns={@JoinColumn(name="user_id")}, 
 		inverseJoinColumns = {@JoinColumn(name="door_id")})
 	private Collection<Door> doors;
 	@ManyToMany
     @JoinTable(
             name="contacts",
             joinColumns = @JoinColumn( name="user_id"),
             inverseJoinColumns = @JoinColumn( name="contact_id"))
 	private Collection<User> contacts;
 	
 	public Long getId() {
 		return id;
 	}
 	public void setId(Long id) {
 		this.id = id;
 	}
 	public String getContactLine() {
 		return contactLine;
 	}
 	public Collection<Door> getDoors() {
 		return doors;
 	}
 	public void setDoors(Collection<Door> doors) {
 		this.doors = doors;
 	}
 	public void setContactLine(String contactLine) {
 		this.contactLine = contactLine;
 	}
 	public String getLogin() {
 		return login;
 	}
 	public void setLogin(String login) {
 		this.login = login;
 	}
 	public String getPassword() {
 		return password;
 	}
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	public Collection<DoorGroup> getDoorGroups() {
 		return doorGroups;
 	}
 	public void setDoorGroups(Collection<DoorGroup> doorGroups) {
 		this.doorGroups = doorGroups;
 	}
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 	public Collection<User> getContacts() {
 		return contacts;
 	}
 	public void setContacts(Collection<User> contacts) {
 		this.contacts = contacts;
 	}
 }
