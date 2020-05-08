 package translator.com.domain;
 
 import java.util.Set;
 
 import javax.jdo.annotations.IdentityType;
 import javax.jdo.annotations.PersistenceCapable;
 import javax.jdo.annotations.Persistent;
 import javax.jdo.annotations.PrimaryKey;
 
 @PersistenceCapable(identityType = IdentityType.APPLICATION)
 public class UserData {
 	@PrimaryKey
 	private String userSecret;
 	
 	@Persistent
 	private String userId;
 
 	@Persistent
 	private String userName;
 	
 	@Persistent
 	private Set<String> words;
 
 	public UserData(String userSecret, String userId, String userName,
 			Set<String> words) {
 		super();
 		this.userSecret = userSecret;
 		this.userId = userId;
 		this.userName = userName;
 		this.words = words;
 	}
 
 	public String getUserSecret() {
 		return userSecret;
 	}
 
 	public String getUserId() {
 		return userId;
 	}
 	
 	public String getUserName() {
 		return userName;
 	}
 	
 	public Set<String> getWords() {
 		return words;
 	}
 
 	public void setWords(Set<String> words) {
 		this.words = words;
 	}
 }
