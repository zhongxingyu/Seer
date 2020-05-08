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
     * Returns true, if the specified user has the provided role.
     * @param user the user
     * @param role the role as string
     * @return true, if user has role
      */
     public static boolean hasRole(UserBase user, String role) {
         return user != null && user.hasRole(role);
     }
 }
