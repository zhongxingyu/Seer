 package org.wyona.security.impl.util;
 
 import org.wyona.security.core.api.User;
 
 import org.apache.log4j.Logger;
 
 /**
  * Utility class for various user operations
  */
 public class UserUtil {
 
     private static Logger log = Logger.getLogger(UserUtil.class);
 
     /**
      * Check whether user is expired
      */
    public static boolean isExpired(User user){
         boolean expired = false;
         if(user.getExpirationDate() != null){
             expired = user.getExpirationDate().before(new java.util.Date()); // INFO: Compare with NOW/Today
         } else {
            log.debug("User '' has no expiration date and hence never expires.");
         }
         return expired;
     }
 }
