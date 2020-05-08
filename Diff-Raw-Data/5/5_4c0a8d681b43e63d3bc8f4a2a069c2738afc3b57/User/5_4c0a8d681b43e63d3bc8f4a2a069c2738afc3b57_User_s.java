 package database;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import javax.persistence.CascadeType;
 import javax.persistence.Entity;
 import javax.persistence.Id;
 import javax.persistence.MapKey;
 import javax.persistence.OneToMany;
 
 /**
  * Contain a voter username and password
  * @author Varunyu 5410546326
  * @version Nov 9 2012
  */
 @Entity
 public class User {
 	@Id
 	private Integer id;
 	private String username;
 	private String password;
 	private String name;
 	private String surename;
 	
 	
 	@OneToMany(cascade=CascadeType.ALL)
 	@MapKey(name="question")
 	private Map<QuestionDescription, NBallot> nBallot = new LinkedHashMap<QuestionDescription, NBallot>();
 	
 	public User(String name, String password) {		
 		this.username = name;
 		this.password = password;
 	}
 	public User(){
 	
 	}
	public String getUserName() {
 		return username;
 	}
	public void setUserName(String name) {
 		this.username = name;
 	}
 	public String getPassword() {
 		return password;
 	}
 	public void setPassword(String password) {
 		this.password = password;
 	}
 	public Integer getId() {
 		return id;
 	}
 	public void setId(Integer id) {
 		this.id = id;
 	}
 	
 	public Map<QuestionDescription, NBallot> getNBallot() {
 		return nBallot;
 	}
 	public void setNBallot(Map<QuestionDescription, NBallot> nBallot) {
 		this.nBallot = nBallot;
 	}
 
 	public String getName() {
 		return name;
 	}
 	public void setName(String name) {
 		this.name = name;
 	}
 	public String getSurename() {
 		return surename;
 	}
 	public void setSurename(String surename) {
 		this.surename = surename;
 	}
 	
 }
