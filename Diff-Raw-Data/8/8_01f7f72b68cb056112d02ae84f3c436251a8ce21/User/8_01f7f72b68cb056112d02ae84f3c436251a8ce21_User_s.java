 package net.thesocialos.shared.model;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.persistence.Id;
 
 import com.googlecode.objectify.Key;
 import com.googlecode.objectify.annotation.Cached;
 import com.googlecode.objectify.annotation.Unindexed;
 
 @SuppressWarnings("serial")
 @Cached
 public class User implements Serializable {
 
 
 	@Id private String email; //Email of the user
 	
 	@Unindexed private  String password; //Password of the user
 	
 	private String avatar; //Avatar of the user
 	
 	private String background; //Background of the user
 	
 	private String firstName; //firstname
 	
 	private String role; //The role of the user
 	
 	private String address; // address of user
 
 	String lastName; //lastnameString address;
 	
 	String mobilePhone;
 	
 	String job;
 	
 	private Date lastTimeActive;
 	
 	Key<Group> groups[];
 	
 	Key<Conversation> conversations[];
 	
 	Key<OutConversation> offlineConversations[];
 	
 	List<Key<Session>> sessions = new ArrayList<Key<Session>>();
 	
 	List<Key<? extends Account>> accounts = new ArrayList<Key<? extends Account>>();
 
 	public User(String email, String password, String picture, String background,String firstName,String lastName,String role){
 		this.email = email;
 		this.password = password;
 		this.avatar = picture;
 		this.background = background;
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.role = role;
 		
 	}
 	
 	public User(String email,String picture,String background,String firstName,String lastName,String role){
 		this.email = email;
 		this.avatar = picture;
 		this.background = background;
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.role = role;
 		
 	}
 	
 	public User(){
 		
 	}
 	
 	public Key<Group>[] getGroups() {
 		return groups;
 	}
 
 	public void setGroups(Key<Group>[] groups) {
 		this.groups = groups;
 	}
 
 	public Key<Conversation>[] getConversations() {
 		return conversations;
 	}
 
 	public void setConversations(Key<Conversation>[] conversations) {
 		this.conversations = conversations;
 	}
 
 	public Key<OutConversation>[] getOfflineConversations() {
 		return offlineConversations;
 	}
 
 	public void setOfflineConversations(Key<OutConversation>[] offlineConversations) {
 		this.offlineConversations = offlineConversations;
 	}
 
 	public List<Key<Session>> getSessions() {
 		return sessions;
 	}
 
 	public void addSessions(Key<Session> session) {
 		this.sessions.add(session);
 	}
 
 	public List<Key<? extends Account>> getAccounts() {
 		return accounts;
 	}
 
 	public void setAccounts(ArrayList<Key<? extends Account>> accounts) {
 		this.accounts = accounts;
 	}
 
 	public void addAccount(Key<? extends Account> account) {
 		accounts.add(account);
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 	public String getPassword() {
 		return password;
 	}
 	public void setPassword(String password) {
 		this.password = password;
 	}
 
 	public String getAvatar() {
 		return avatar;
 	}
 
 	public String getBackground() {
 		return background;
 	}
 	public String getName() {
 		return firstName;
 	}
 
 	public String getLastName() {
 		return lastName;
 	}
 	
 	public String getMobilePhone() {
 		return mobilePhone;
 	}
 
 	public String getRole() {
 		return role;
 	}
 	
 	public static User toDTO(User user) {
 		return new User(user.email, user.avatar, user.background, user.firstName, user.lastName, user.role);
 	}
 
 	public String getAddress() {
 		return address;
 	}
 
 	public Date getLastTimeActive() {
 		return lastTimeActive;
 	}
 
 	public void setLastTimeActive(Date lastTimeActive) {
 		this.lastTimeActive = lastTimeActive;
 	}
 }
