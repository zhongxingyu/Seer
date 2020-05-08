 package controllers;
 
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.List;
 
 import models.account.User;
 import models.tm.Project;
 import models.tm.TMUser;
 import play.mvc.Util;
 import util.Logger;
 
 /**
  * Custom implementation of the {@link Security} class of Play's security module.
  * This is where the authentication code sits.
  */
 public class Security extends Secure.Security {
 
     static boolean authenticate(String username, String password) {
         User a = User.find("byEmailAndActive", username, true).first();
         boolean success = a != null && a.connect(password);
         if (success) {
             Logger.info(Logger.LogType.AUTHENTICATION, false, "Authenticated user '%s'", username);
         } else {
             Logger.warn(Logger.LogType.AUTHENTICATION, false, "Unsuccessful authentication attempt by user '%s'", username);
         }
         return success;
     }
 
     static void onAuthenticated() {
         TMUser u = TMUser.find("from TMUser u where u.user.email = ?", Security.connected()).first();
 
         // set the session expiration timestamp and compute the active project
         u.sessionExpirationTime = getSessionExpirationTimestamp();
 
         if (u.activeProject == null) {
             // try fetching the first project where this user has any role
             // TODO make the "default project" for a new user configurable
            List<Project> projects = Project.listByUser(u);
             if (!projects.isEmpty()) {
                 u.activeProject = projects.get(0);
             }
         }
 
         u.save();
         // bind the basic session variables
         // we really want to keep the user session as thin as possible as it is sent at each request
         session.put("account", u.user.account.getId());
         if (u.activeProject != null) {
             session.put("activeProject", u.activeProject.getId());
         }
     }
 
     @Util
     public static Timestamp getSessionExpirationTimestamp() {
         // TODO this should be configurable in the Account / Project settings
         Calendar c = Calendar.getInstance();
         c.add(Calendar.HOUR, 1);
         return new Timestamp(c.getTime().getTime());
     }
 
 }
