 package user;
 
 //Required class imports.
import encrypt.BCrypt; //for hashing password.
 import regex.Regex; //for Regex testing.
 
 
 /**
  * A class definition for a User Object. This User Class is in the user
  * package. It contains reference variables for USER_CODE(3 for regular WebUsers),
  * firstName, username, password (hashed/salted), and emailAddress.
  * The class includes a default no-arg constructor and an overloaded constructor.
  *
  * @author Paul Trott (ptrott) Date: February 3, 2013
  */
 public class User {
     //Final variable to hold user code.
 
    private final String USER_CODE = "WebUser";
     //Instance variables
     private String firstName;
     private String username;
     private String password;
     private String emailAddress;
 
     /**
      * no-arg Default Constructor
      */
     public User() {
         this.firstName = "no name";
         this.username = "no name";
         this.password = "password";
         this.emailAddress = "no email";
     }
 
     /**
      * Overloaded Constructor
      *
      * @param firstName
      * @param username
      * @param password
      * @param emailAddress
      */
     public User(String firstName, String username, String password, String emailAddress) {
         this.setFirstName(firstName);
         this.setUsername(username);
         this.setPassword(password);
         this.setEmailAddress(emailAddress);
     }
 
     /**
      * Getter for firstName
      *
      * @return
      */
     public String getFirstName() {
         return firstName;
     }
 
     /**
      * Setter for firstName
      *
      * @param firstName
      */
     public final void setFirstName(String firstName) {
         //removes numbers in a name, as a user should not have numbers in a first name.
         String newName = Regex.replaceNumbersInFirstName(firstName);
         //removes spaces in name, as user should have only entered first name.
        newName = Regex.replaceSpacesWithNoSpaces(newName);
        //firstName has to be 2 or more characters long.
        if (newName.length() < 2) {
             this.firstName = "no name";
         } else {
             //test that first name has no numbers
            if(Regex.isFirstNameRegexMatch(newName)) {
                this.firstName = newName;
             } else {
                 this.firstName = "no name";
             }
         }
     }
 
     /**
      * Getter for username
      *
      * @return
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * Setter for username
      *
      * @param username
      */
     public final void setUsername(String username) {
         //removes spaces in name, as user should have only entered first name.
             String nospaceUserName = Regex.replaceSpacesWithNoSpaces(username);
        //username has to be 6 or more characters long
        if (nospaceUserName.length() < 6 || nospaceUserName.length() > 20) {
             this.username = "no name";
         } else {
             this.username = nospaceUserName;
         }
 
     }
 
     /**
      * Setter for password
      *
      * @param password
      */
     public final void setPassword(String password) {
        //Convert parameter to a hashed password.
        String pw_hash = BCrypt.hashpw(password, BCrypt.gensalt(11));
        //if parameter equals pw_hash then put pw_hash as password stored in DB.
        if (BCrypt.checkpw(password, pw_hash)) {
            this.password = pw_hash;
        } else {
            this.password = "password";
        }
     }
 
     /**
      * Getter for password
      *
      * @return password
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * Getter for emailAddress
      *
      * @return
      */
     public String getEmailAddress() {
         return emailAddress;
     }
 
     /**
      * Setter for emailAddress
      *
      * @param emailAddress
      */
     public final void setEmailAddress(String emailAddress) {
         //Only store email in object if email matches Regex expression.
         if (Regex.isEmailRegexMatch(emailAddress)) {
             this.emailAddress = emailAddress;
         } else {
             this.emailAddress = "no email";
         }
     }
 
     /**
      * Getter for USER_CODE
      *
      * @return
      */
    public String getUSER_CODE() {
         return USER_CODE;
     }
     
     
 }
