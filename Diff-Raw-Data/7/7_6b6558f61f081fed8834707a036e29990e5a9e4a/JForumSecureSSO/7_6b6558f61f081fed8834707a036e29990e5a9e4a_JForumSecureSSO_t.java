 package uk.co.smartkey.jforumsecuresso;
 
 import net.jforum.entities.UserSession;
 import net.jforum.context.RequestContext;
 import net.jforum.sso.SSO;
 import net.jforum.ControllerUtils;
 
 import javax.servlet.http.Cookie;
 
 /**
  * JForumSecureSSO
  */
 public class JForumSecureSSO implements SSO {
 
     /**
      * Authenticates an user.
      * This method should check if the incoming user is authorized
      * to access the forum.
      *
      * @param request The request object
      * @return The username, if authentication succeded, or <code>null</code> otherwise.
      */
     public String authenticateUser(RequestContext request) {
         Cookie ssoCookie = ControllerUtils.getCookie(SecurityTools.FORUM_COOKIE_NAME);
 
         if (ssoCookie == null) {
             //not logged in
             return null;
         }
 
         //get the email address and the screen name for this user from the cookie
         String[] emailAndScreenName = SecurityTools.getInstance().decryptCookieValues(ssoCookie.getValue());
 
         if (emailAndScreenName == null) {
             //not logged in            
             return null;
         }
 
         String email = emailAndScreenName[0];
         String screenName = emailAndScreenName[1];
 
         //the password is irrelevant as the user has already logged on (set it anyway in case JForum uses it)
         request.getSessionContext().setAttribute("password", "");        
         request.getSessionContext().setAttribute("email", email);
 
         return screenName;
     }
 
     /**
      * Check to see if the user for the current {@link UserSession} is the same user by
      * single sign on mechanisim.
      *
      * @param userSession the current user session
      * @param request     the current request
      * @return if the UserSession is valid
      */
     public boolean isSessionValid(UserSession userSession, RequestContext request) {
         Cookie ssoCookie = ControllerUtils.getCookie(SecurityTools.FORUM_COOKIE_NAME);

	if (ssoCookie == null) {
		return false;
	}

         String[] emailAndScreenName = SecurityTools.getInstance().decryptCookieValues(ssoCookie.getValue());
 
         if (emailAndScreenName == null) {
             return false;
         }
 
         String screenName = emailAndScreenName[1];
 
         //check whether user has changed
         if (screenName.equals(userSession.getUsername())) {
             return true;
         } else {
             return false;
         }
     }
 }

