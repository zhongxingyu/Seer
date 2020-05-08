 package de.enwida.web.model;
 
 import org.springframework.beans.factory.annotation.Autowired;
 
 import de.enwida.web.dao.implementation.UserDao;
 
 
 public class User {
  
 	private Long userID;
 	private String userName;
 	private String lastName;
 	private String firstName;
 	private String password;
 	private boolean enabled;
 	
 
 	private UserPermissionCollection userPermissionCollection=new UserPermissionCollection();
 
 	public User(long userID, String userName, String password,String firstName,String lastName, boolean enabled) {
 		// TODO Auto-generated constructor stub
 		this.setUserID(userID);
 		this.setUserName(userName);
 		this.setLastName(lastName);
 		this.setFirstName(firstName);
 		this.setPassword(password);
 		this.setEnabled(enabled);
 	}
 	
 	public User(long userID,String userName,String password, boolean enabled){
 		this(userID,userName,password,null,null,enabled);
 	}
 
 	public User() {
 		// TODO Auto-generated constructor stub
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 
 	public void setLastName(String lastName) {
 		this.lastName = lastName;
 	}
 
 	public String getFirstName() {
 		return firstName;
 	}
 
 	public void setFirstName(String firstName) {
 		this.firstName = firstName;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getUserName() {
 		return userName;
 	}
 
 	public void setUserName(String userName) {
 		this.userName = userName;
 	}
 
 	public Long getUserID() {
 		return userID;
 	}
 
 	public void setUserID(Long userID) {
 		this.userID = userID;
 	}
 	
 	@Override
 	public String toString() {
 		return this.getUserName();
 	}
 
 	public UserPermissionCollection getUserPermissionCollection() {
 		return userPermissionCollection;
 	}
 
 	public void setUserPermissionCollection(
 			UserPermissionCollection userPermissionCollection) {
 		this.userPermissionCollection = userPermissionCollection;
 	}
 	
 	public boolean hasPermission(String permission){
 		return getUserPermissionCollection().implies(new UserPermission(permission));
 	}
 
 	public boolean isEnabled() {
 		return enabled;
 	}
 
 	public void setEnabled(boolean enabled) {
 		this.enabled = enabled;
 	}
 }
