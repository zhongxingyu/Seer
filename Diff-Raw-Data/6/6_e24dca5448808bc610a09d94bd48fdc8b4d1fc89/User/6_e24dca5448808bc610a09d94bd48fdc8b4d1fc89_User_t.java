 package net.alpha01.jwtest.beans;
 
 import java.io.Serializable;
 
 import net.alpha01.jwtest.WebUser;
 
 public class User extends IdBean implements Serializable {
 
 	private static final long serialVersionUID = 1L;
 	private String username;
 	private String password;
 	private String email;
 	private String name;
 	private Boolean ldap;
 
 	public User(){
 		super();
 	}
 	
 	public User(String username, String password) {
 		setUsername(username);
 		setPassword(password);
 	}
 
 	public User(String username, String password, boolean ldapAutenticated) {
 		setUsername(username);
 		setPassword(password);
 		setLdap(ldapAutenticated);
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
 
 
 	@Override
 	public boolean equals(Object obj) {
 		if (obj instanceof WebUser){
 			return ((WebUser)obj).getId().equals(getId());
 		}
 		if (obj instanceof User){
 			return ((User)obj).getId().equals(getId());
 		}
 		return false;
 	}
 
 	public Boolean getLdap() {
		/**
		 * prevent exception if you use getLdap() in conditional statement 
		 */
		if (ldap==null) {
			return new Boolean(false);
		}
 		return ldap;
 	}
 
 	public void setLdap(Boolean ldap) {
 		this.ldap = ldap;
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	public void setName(String name) {
 		this.name = name;
 	}
 	
 }
