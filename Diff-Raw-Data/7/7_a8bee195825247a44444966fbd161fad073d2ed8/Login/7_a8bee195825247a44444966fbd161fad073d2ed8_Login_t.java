 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package monsterMashGroupProject;
 
 import Scheduling.LogginChecker;
 import Scheduling.MonsterLifeCycle;
 import databaseManagement.*;
 import java.util.*;
 import java.util.regex.Pattern;
 import java.util.regex.PatternSyntaxException;
 
 /**
  * Class containing methods related to logging a user
  * into the system.
  * @author dam44
  */
 public class Login {
 
     private PersistManager persistIt = new PersistManager();
     private static final String REGEX = "([0-9a-zA-Z_-!?@]+)";
     private String username;
     private String password;
     public MyUser user;
     /**
      * Returns the user this class relates to.
      * @return MyUser user.
      */
     public MyUser getUser() {
         return user;
     }
     /**
      * Sets the current user the class relates to.
      * @param user 
      */
     public void setUser(MyUser user) {
         this.user = user;
     }
     /**
      * Gets the username the class relates to.
      * @return String username.
      */
     public String getUsername() {
         return username;
     }
     /**
      * Sets the username currently related to.
      * @param username 
      */
     public void setUsername(String username) {
         this.username = username;
     }
     /**
      * gets the password currently related to.
      * @return String password.
      */
     public String getPassword() {
         return password;
     }
     /**
      * Sets a new password currently related to.
      * @param password 
      */
     public void setPassword(String password) {
         this.password = password;
     }
     /**
      * Gets current user and logs them in.
      * @return String file to be directed to.
      */
     public String LoginUser() {
         persistIt.init();
         List<MyUser> users = persistIt.searchUsers();
         for (int i = 0; i < users.size(); i++) {
             if ((users.get(i).getUsername().equals(username)) && (users.get(i).getUserPassword().equals(password))) {
                 user = users.get(i);
                 user.setIsLoggedIn(true);
                 user.setIsActive(true);
                 persistIt.update(user);
                 new LogginChecker(600, user);
                 new MonsterLifeCycle(60, user);
                
                RequestFactory rf = new RequestFactory();
                
                Requests req = rf.makeIt("Breeding", "jas38", "ben", "http://localhost:8080/");
                
                persistIt.create(req);
                
                 return "welcome.jsp";
             }
         }
         persistIt.shutdown();
         return "index.jsp";
     }
 
     /**
      * Gets current user and logs them off.
      */
     public String LogOff() {
         persistIt.init();
         List<MyUser> users = persistIt.searchUsers();
         for (int i = 0; i < users.size(); i++) {
             if ((users.get(i).getUsername().equals(username)) && (users.get(i).getUserPassword().equals(password))) {
                 user = users.get(i);
                 user.setIsLoggedIn(false);
                 user.setIsActive(false);
                 persistIt.update(user);
                 return "welcome.jsp";
             }
         }
         persistIt.shutdown();
         return "index.jsp";
     }
     /**
      * Registers a new user.
      * @param usrname
      * @param usrpassword
      * @param email
      * @return String page to be redirected to.
      */
     public String Register(String usrname, String usrpassword, String email) {
         persistIt.init();
         
         MyUser myUser = UserFactory.makeIt(usrname, usrpassword, 100);
         persistIt.create(myUser);
         
         Monster gary = MonsterFactory.makeIt("Gary", 10, 10, 10, 10, usrname, true);
         
         persistIt.create(gary);
         
         persistIt.update(user);
         //}
         persistIt.shutdown();
         return "index.jsp";
     }
     /**
      * Applies a regex to an input.
      * @param regex
      * @param input
      * @return bool matches.
      */
     public boolean matches(String regex, String input) {
         return Pattern.matches(regex, input);
     }
 }
