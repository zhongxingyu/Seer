 package org.fiteagle.core.userdatabase;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Pattern;
 
 import net.iharder.Base64;
 
 import org.codehaus.jackson.annotate.JsonIgnore;
 import org.fiteagle.core.userdatabase.UserPersistable.DuplicatePublicKeyException;
 import org.fiteagle.core.userdatabase.UserPersistable.InValidAttributeException;
 import org.fiteagle.core.userdatabase.UserPersistable.NotEnoughAttributesException;
 import org.fiteagle.core.userdatabase.UserPersistable.PublicKeyNotFoundException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class User {
 	
 	private String username;
 	private String firstName;
 	private String lastName;
 	private String email;
 	private String affiliation;
 	@JsonIgnore
 	private Date created;
 	@JsonIgnore
 	private Date last_modified;
 	@JsonIgnore
 	private String passwordHash;
 	@JsonIgnore
 	private String passwordSalt;
 	private List<UserPublicKey> publicKeys;
 
 	private final static int MINIMUM_PASSWORD_LENGTH = 3;
	private final static Pattern USERNAME_PATTERN = Pattern.compile("[\\w|-|@.]{3,200}");
 	private final static Pattern EMAIL_PATTERN = Pattern.compile("[^@]+@{1}[^@]+\\.+[^@]+");
 	private final static int MINIMUM_FIRST_AND_LASTNAME_LENGTH = 2;
   private final static int MINIMUM_AFFILITAION_LENGTH = 2;
 
   static Logger log = LoggerFactory.getLogger(UserDBManager.class);
 	
 	public User(String username, String firstName, String lastName, String email, String affiliation, String passwordHash, String passwordSalt, Date created, Date lastModified, List<UserPublicKey> publicKeys){
 		this.username = username;
 		this.firstName = firstName;
 		this.lastName = lastName;
 		this.email = email;
 		this.affiliation = affiliation;
 		this.passwordHash = passwordHash;
 		this.passwordSalt =  passwordSalt;
 		this.created = created;
 		this.last_modified = lastModified;
 		this.publicKeys = publicKeys;
 		if(publicKeys == null){
 		  this.publicKeys = new ArrayList<>();
 		}
 	}
 	
 	public User(String username, String firstName, String lastName, String email, String affiliation, String password){ 
 	  this.username = username;
     this.firstName = firstName;
     this.lastName = lastName;
     this.email = email;
     this.affiliation = affiliation;
     this.created = Calendar.getInstance().getTime();
     this.last_modified = created;
     byte[] salt = generatePasswordSalt();
     this.passwordSalt = Base64.encodeBytes(salt);        
     this.passwordHash = generatePasswordHash(salt, password);    
     this.publicKeys = new ArrayList<UserPublicKey>();
 	}
 	
 	public User(String username, String firstName, String lastName, String email, String affiliation, String password, List<UserPublicKey> publicKeys){ 
 	  this.username = username;
     this.firstName = firstName;
     this.lastName = lastName;
     this.email = email;
     this.affiliation = affiliation;
     this.created = Calendar.getInstance().getTime();
     this.last_modified = created;
     byte[] salt = generatePasswordSalt();
     this.passwordSalt = Base64.encodeBytes(salt);        
     this.passwordHash = generatePasswordHash(salt, password);
     this.publicKeys = publicKeys;
     if(publicKeys == null){
       this.publicKeys = new ArrayList<>();
     }
 	}
 	
 
 	public static User createUser(String username, String firstName, String lastName, String email, String affiliation, String password, List<UserPublicKey> publicKeys) throws NotEnoughAttributesException, InValidAttributeException, DuplicatePublicKeyException{
 	  User u = new User(username, firstName, lastName, email, affiliation, password, publicKeys);
 	  u.checkAttributes();
 	  return u;
 	}
 	
 	public static User createUserWithExistingPassword(String username, String firstName, String lastName, String email, String affiliation, String passwordHash, String passwordSalt, Date created, Date lastModified, List<UserPublicKey> publicKeys){
 	  User u = new User(username, firstName, lastName, email, affiliation, passwordHash, passwordSalt, created, lastModified, publicKeys);
 	  u.checkAttributes();
 	  return u;
 	}
 	public static User createUser(String username){
 		User u = new User(username, "", "", "", "", "", new LinkedList<UserPublicKey>());
 		return u;
 	}
 	private void checkAttributes() throws NotEnoughAttributesException, InValidAttributeException, DuplicatePublicKeyException{  
 	
 	  if(username == null){
 	    throw new NotEnoughAttributesException("no username given");
 	  }
 	  if(firstName == null){
 	    this.firstName = "default";
 	  }
 	  if(lastName == null){
       this.lastName = "default";
     }
 	  if(email == null){
       this.email = "default";
     }
 	  if(affiliation == null){
       this.affiliation = "default";
     }	  
 	  if(passwordHash == null){
       throw new NotEnoughAttributesException("no password given or password too short");
     }   
 	  
 	  if(!USERNAME_PATTERN.matcher(username).matches()){
       throw new InValidAttributeException("invalid username, only letters, numbers, \"@\", and \"-\" is allowed and the username has to be from 3 to 200 characters long");
     }
 	  if(firstName.length() < MINIMUM_FIRST_AND_LASTNAME_LENGTH){
       throw new InValidAttributeException("firstName too short");
     }
 	  if(lastName.length() < MINIMUM_FIRST_AND_LASTNAME_LENGTH){
       throw new InValidAttributeException("lastName too short");
     }
 	  if(!EMAIL_PATTERN.matcher(email).matches() && !email.equals("default")){
       throw new InValidAttributeException("an email needs to contain \"@\" and \".\"");
     }
 	  if(affiliation.length() < MINIMUM_AFFILITAION_LENGTH){
       throw new InValidAttributeException("affiliation too short");
     }
 	  
 	  for(UserPublicKey userPublicKey : publicKeys){
 	    String description = userPublicKey.getDescription();
 	    String publicKeyString = userPublicKey.getPublicKeyString();
 	    
 	    for(UserPublicKey key : publicKeys){
 	      if(key != userPublicKey && (key.getDescription().equals(description) || key.getPublicKeyString().equals(publicKeyString))){
 	        throw new DuplicatePublicKeyException();
 	      }
 	    }
 	  }
   }
 	
 	private byte[] generatePasswordSalt(){
 	  SecureRandom random = new SecureRandom();
     return random.generateSeed(20);
 	}
 	
 	private String generatePasswordHash(byte[] salt, String password){
 	  if(password == null || password.length() < MINIMUM_PASSWORD_LENGTH){
 	    return null;
 	  }
     
 	  byte[] passwordBytes = null;
     try {
       passwordBytes = createHash(salt, password);
     } catch (NoSuchAlgorithmException e) {
       log.error(e.getMessage());
     }
     return Base64.encodeBytes(passwordBytes);
 	}
 	
   private byte[] createHash(byte[] salt, String password) throws NoSuchAlgorithmException {    
     MessageDigest digest = MessageDigest.getInstance("SHA-256");
     digest.reset();
     digest.update(salt);
     return digest.digest(password.getBytes());
   }
 	
   public void update(String firstName, String lastName, String email, String affiliation, String password, List<UserPublicKey> publicKeys) throws NotEnoughAttributesException, InValidAttributeException, DuplicatePublicKeyException{
     if(firstName != null){
      this.firstName = firstName;
     }
     if(lastName != null){
       this.lastName = lastName;
     }
     if(publicKeys != null && publicKeys.size() != 0){
       this.publicKeys = publicKeys;
     }
     if(email != null){
       this.email = email;
     }
     if(affiliation != null){
       this.affiliation = affiliation;
     }
     if(password != null){
       byte[] salt = generatePasswordSalt();
       this.passwordSalt = Base64.encodeBytes(salt);        
       this.passwordHash = generatePasswordHash(salt, password);
     }
     this.setLast_modified(Calendar.getInstance().getTime());
     this.checkAttributes();      
   }	
 	
 	public void addPublicKey(UserPublicKey userPublicKey){		
 		String description = userPublicKey.getDescription();
 		String publicKeyString = userPublicKey.getPublicKeyString();
 		for(UserPublicKey key : publicKeys){
 		  if(key.getDescription().equals(description) || key.getPublicKeyString().equals(publicKeyString)){
 		    throw new DuplicatePublicKeyException();
 		  }
 		}
 		this.publicKeys.add(userPublicKey);
 	}
 	
 	public void deletePublicKey(String description){ 
 	  UserPublicKey keyToRemove = null;
 	  for(UserPublicKey key : this.publicKeys){
 	    if(key.getDescription().equals(description)){
 	      keyToRemove = key;
 	    }
 	  }    
 	  if(keyToRemove != null){
 	    this.publicKeys.remove(keyToRemove);
 	  }
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
     if (affiliation == null) {
       if (other.affiliation != null)
         return false;
     } else if (!affiliation.equals(other.affiliation))
       return false;
     if (email == null) {
       if (other.email != null)
         return false;
     } else if (!email.equals(other.email))
       return false;
     if (firstName == null) {
       if (other.firstName != null)
         return false;
     } else if (!firstName.equals(other.firstName))
       return false;
     if (lastName == null) {
       if (other.lastName != null)
         return false;
     } else if (!lastName.equals(other.lastName))
       return false;
     if (passwordHash == null) {
       if (other.passwordHash != null)
         return false;
     } else if (!passwordHash.equals(other.passwordHash))
       return false;
     if (passwordSalt == null) {
       if (other.passwordSalt != null)
         return false;
     } else if (!passwordSalt.equals(other.passwordSalt))
       return false;
     if (publicKeys == null) {
       if (other.publicKeys != null)
         return false;
     } else if (!publicKeys.containsAll(other.publicKeys) || publicKeys.size() != other.publicKeys.size())
       return false;
     if (username == null) {
       if (other.username != null)
         return false;
     } else if (!username.equals(other.username))
       return false;
     return true;
   }
 	
 	@Override
   public String toString() {
     return "User [username=" + username + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
         + ", affiliation=" + affiliation + ", created=" + created + ", last_modified=" + last_modified
         + ", publicKeys=" + publicKeys + "]";
   }	
 	
 	public UserPublicKey getPublicKey(String description){
     for(UserPublicKey key : publicKeys){
       if(key.getDescription().equals(description)){
         return key;
       }
     }
     throw new PublicKeyNotFoundException();
   }
 	
 	public void renamePublicKey(String description, String newDescription){
 	  for(UserPublicKey key : publicKeys){
       if(key.getDescription().equals(newDescription)){
         throw new DuplicatePublicKeyException();
       }
     }
 	  for(UserPublicKey key : publicKeys){
       if(key.getDescription().equals(description)){
         key.setDescription(newDescription);
         return;
       }
     }	  
 	  throw new PublicKeyNotFoundException();
 	}
 	
 	public String getUsername() {
     return username;
   }
 
   public void setUsername(String username) {
     this.username = username;
   }
 
   public String getFirstName() {
     return firstName;
   }
 
   public void setFirstName(String firstName) {
     this.firstName = firstName;
   }
 
   public String getLastName() {
     return lastName;
   }
 
   public void setLastName(String lastName) {
     this.lastName = lastName;
   }
   
   public String getEmail() {
     return email;
   }
 
   public void setEmail(String email) {
     this.email = email;
   }
 
   public String getAffiliation() {
     return affiliation;
   }
 
   public void setAffiliation(String affiliation) {
     this.affiliation = affiliation;
   }  
 
   public String getPasswordHash() {
     return passwordHash;
   }
 
   public String getPasswordSalt() {
     return passwordSalt;
   }
     
   public void setPasswordHash(String passwordHash) {
     this.passwordHash = passwordHash;
   }
 
   public void setPasswordSalt(String passwordSalt) {
     this.passwordSalt = passwordSalt;
   }
 
   public Date getLast_modified() {
     return last_modified;
   }
 
   public void setLast_modified(Date last_modified) {
     this.last_modified = last_modified;
   }
 
   public Date getCreated() {
     return created;
   }
 
   public List<UserPublicKey> getPublicKeys() {
     return publicKeys;
   }
 
   public void setPublicKeys(List<UserPublicKey> publicKeys) {
     this.publicKeys = publicKeys;
   }  
 }
