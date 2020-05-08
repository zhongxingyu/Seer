 package beans;
 

 import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
 
 @ManagedBean
 @RequestScoped
 public class UserBean {
 	private String username;
 	private String password;
 	private String hash;
 	
 	public String getHash() {
 		return hash;
 	}
 
 	public void setHash(String hash) {
 		this.hash = hash;
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
 }
