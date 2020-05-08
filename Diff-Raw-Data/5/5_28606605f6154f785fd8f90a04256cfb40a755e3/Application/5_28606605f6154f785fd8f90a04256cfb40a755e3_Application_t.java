 package controllers;
 
 import models.NdgUser;
 import play.i18n.Lang;
 import play.mvc.Before;
 import play.mvc.Controller;
 
 public class Application extends Controller {
 
      @Before(unless={"login", "authorize", "logout", "SurveyManager.upload"})
      public static void checkAccess() throws Throwable {
         if(!session.contains("ndgUser")) {
             login( null );
         }
     }
 
     public static void index() {
         render("Application/index.html");
     }
 
     public static void login( String lang ) {
         if( lang != null && !lang.isEmpty() ) {
             Lang.change( lang );
         }
         flash.keep("url");
         render("Application/login.html");
     }
 
     public static void authorize( String username, String password, String lang ) {
         if( lang != null && !lang.isEmpty() ) {
             Lang.change( lang );
         }
 
         NdgUser currentUser = NdgUser.find("byUsernameAndPassword", username, password ).first();
 
         if(currentUser != null && checkPermission(currentUser)) {
             session.put("ndgUser", username);
            index();
         } else {
             flash.put("error", "wrong username / password");
             render("Application/login.html");
         }
     }
 
     public static void logout() {
         session.remove("ndgUser");
         session.clear();
         flash.put("url", "/");
        login(null);
     }
 
     private static boolean checkPermission(NdgUser user) {
         boolean retval = false;
         if(user.hasRole("Operator")) {
             session.put("operator", true);
             retval = true;
         }
         if(user.hasRole("Admin")) {
             session.put("admin", true);
             retval = true;
         }
         return retval;
     }
 }
