 package controllers;
 
 import models.Member;
 
 /**
  * User: soyoung
  * Date: Dec 22, 2010
  */
 public class Security extends Secure.Security {
     static boolean authenticate(String username, String password) {
         return Member.connect(username, password) != null;
     }

    static String connected() {
        return session == null ? null : session.get("username");
    }
 }
