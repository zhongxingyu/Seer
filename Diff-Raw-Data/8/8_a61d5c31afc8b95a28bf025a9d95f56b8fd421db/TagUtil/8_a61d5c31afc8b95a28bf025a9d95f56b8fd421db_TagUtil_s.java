 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.nixis.commons.web.base.security.taglib;
 
 import de.nixis.commons.web.base.model.UserBase;
 
 /**
  * Implementing security related tag functions
  * @author nico.rehwaldt
  */
 public class TagUtil {
     
     /**
     * Returns true, if the specified argument is a 
     * @param o
     * @param cls
     * @return
      */
     public static boolean hasRole(UserBase user, String role) {
         return user != null && user.hasRole(role);
     }
 }
