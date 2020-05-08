 package br.com.fa7.firststepinagile.entities;
 
 import java.io.Serializable;
 
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
 
 @Entity
@Table(name="users")
 public class User implements Serializable{
 	
 	private static final long serialVersionUID = 1L;
 	
 	@Id
 	@GeneratedValue
 	private Long id;
 
 	private String name;
 	
 	private String email;
 	
 	private String password;
 	
 	private String login;
 	
 	private boolean admin;
 	
	@OneToOne
 	private Sprint sprint;
 	
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public void setEmail(String email) {
 		this.email = email;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public void setLogin(String login) {
 		this.login = login;
 	}
 
 	public Long getId() {
 		return id;
 	}
 
 	public void setId(Long id) {
 		this.id = id;
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((id == null) ? 0 : id.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		User other = (User) obj;
 		if (id == null) {
 			if (other.id != null)
 				return false;
 		} else if (!id.equals(other.id))
 			return false;
 		return true;
 	}
 
 	public boolean isAdmin() {
 		return admin;
 	}
 
 	public void setAdmin(boolean admin) {
 		this.admin = admin;
 	}
 
 	public Sprint getSprint() {
 		return sprint;
 	}
 
 	public void setSprint(Sprint sprint) {
 		this.sprint = sprint;
 	}
 	
 	
 	
 }
