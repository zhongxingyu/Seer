 package controllers;
 
 import play.*;
 import play.mvc.*;
 
 import java.util.*;
 
 import models.*;
 
 public class Security extends Secure.Security {
     
     /**
      * Authenticate User
      */
     static boolean authenticate(String email, String password) {
         User user = User.find("byEmail", email).first();
         Boolean authed = user != null && user.password.equals(password);
         return authed;
     }
 
     /**
     * Is admin?
      */
    static boolean isAdmin() {
        User user = connected();
         return user.admin;
     }

 }
