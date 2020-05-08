 package models;
 
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import javax.persistence.Entity;
 import javax.persistence.ManyToOne;
 import javax.persistence.Table;
 
 import play.db.jpa.Model;
 import play.Logger;
 
 // TODO: Auto-generated Javadoc
 /**
  * The Class User.
  */
 @Entity
 @Table(name="Users")
 public class User extends Model{
 	
     /** The user group. */
     @ManyToOne
     public UserGroup userGroup;
 
     /** The user status. */
     @ManyToOne
     public UserStatus userStatus;
 
     /** The email. */
     public String email;
 
     /** The login. */
     public String login;
 
     /** The password. */
     public String password;
 
     /**
         * Instantiates a new user.
         *
         * @param userGroup the user group
         * @param userStatus the user status
         * @param email the email
         * @param login the login
         * @param password the password
         */
     public User(UserGroup userGroup, UserStatus userStatus, 
                     String email, String login, String password){
 
         this.userGroup = userGroup;
         this.userStatus = userStatus;
         this.email = email;
         this.login = login;
         this.password = password;
     }
 
     public String urlParser(String url) {
         if (url.contains("@")) {
             url = "/" + url.substring(2, url.length() - 1);
             url = url.replace(".", "/");
             url = url.toLowerCase();
         }
         return url;
     }
     
     public boolean checkAccess(String url) {
         url = urlParser(url);
         List<UserGroupAccess> access = UserGroupAccess.find("userGroup", this.userGroup).fetch();
         for (UserGroupAccess userGroupAccess : access) {
             try {
                 Pattern p = Pattern.compile(userGroupAccess.pattern);
                 Matcher m = p.matcher(url);
                 if (m.matches()) {
                     return true;
                 }
             } catch (Exception e) {
                 Logger.error(e, "Incorrect pattern: " + userGroupAccess.pattern);
             }
         }        
 
         return false;
     }
    
    public String toString(){
        return login;
    }
 }
