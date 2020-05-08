 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.manuwebdev.mirageobjectlibrary.Authentication;
 
 /**
  * Defines the User object which represents the 
  * physical user that is logged in at an {@link Extender}
  * @author Manuel Gauto
  */
public class User {
    /**
      * Username used to log in
      */
     String user;
     
     /**
      * Real Firstname of user
      */
     String FirstName;
     
     /**
      * Real Lastname of user
      */
     String LastName;
     
     /**
      * Facebook Graph API access token
      */
     String FBToken;
     /**
      * 
      * @param user String containing username
      * @param first String containing real first name
      * @param last String containing real last name
      * @param FBToken String containing Facebook access token
      */
     public User(String user, String first, String last, String FBToken){
         this.user=user;
         this.FirstName=first;
         this.LastName=last;
         this.FBToken=FBToken;
     }
     
     /**
      * Sets Facebook Graph API access token
      * @param token Facebook Graph API access token
      */
     public void setFacebookToken(String token){
         this.FBToken=token;
     }
     
     /**
      * Returns username used to login
      * @return Username of user 
      */
     public String getUserName(){
         return user;
     }
     
     /**
      * Returns the full name of the user
      * @return Fullname of user
      */
     public String getFullName(){
         return FirstName+" "+LastName;
     }
     
     /**
      * Returns the real first name of the user
      * @return User's first name
      */
     public String getFirstName(){
         return FirstName;
     }
     
     /**
      * Returns the last name of the user
      * @return Last name of user
      */
     public String getLastName(){
         return LastName;
     }
     
     /**
      * Returns Facebook access token for user
      * @return Facebook access token
      */
     public String getFacebookToken(){
         return FBToken;
     }
 }
