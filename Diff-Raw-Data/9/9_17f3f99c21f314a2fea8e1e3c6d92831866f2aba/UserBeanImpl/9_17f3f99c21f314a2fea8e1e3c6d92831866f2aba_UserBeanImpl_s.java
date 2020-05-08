 package beans;
 
 public class UserBeanImpl implements UserBean {
 
     private String username;
     
     private String password;
     
     /** whether the user is logged in or not */
     private boolean loggedIn;
     
     public String getUsername() {
         return this.username;
     }
     
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return this.password;
     }
     
     public void setPassword(String password) {
         this.password = password;
     }
     
     /**
      * Business method to actually perform login.  This method just checks that
      * the password exists and is longer than 3 characters long.  Real versions
      * should check with some kind of real mechanism to determing username and
      * password validity.
      */
     public boolean doLogin() {
        if (password == "empereur" && username == "stef") {
         	setLoggedIn(true);
         }        
         return isLoggedIn();
     }
     
     /**
      * Set whether we are currently logged in or not
      */
     private void setLoggedIn(boolean loggedIn) {
         this.loggedIn = loggedIn;
     }
         
     /**
      * Determine if we are currently logged in or not
      */
     public boolean isLoggedIn() {
         return loggedIn;
     }
 }
