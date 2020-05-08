 package com.atlassian.refapp.sal.user;
 
 import java.security.Principal;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 
 import com.atlassian.sal.api.user.UserProfile;
 import com.atlassian.sal.api.user.UserResolutionException;
 import com.atlassian.seraph.auth.AuthenticationContext;
 import com.atlassian.user.EntityException;
 import com.atlassian.user.Group;
 import com.atlassian.user.GroupManager;
 import com.atlassian.user.User;
 import com.atlassian.user.UserManager;
 import com.atlassian.user.security.authentication.Authenticator;
 
 /**
  * Pretends the 'someUser' is logged in and is an admin
  */
 public class RefImplUserManager implements com.atlassian.sal.api.user.UserManager
 {
     private final Logger log = Logger.getLogger(getClass());
 
     private final AuthenticationContext authenticationContext;
     private final GroupManager groupManager;
     private final UserManager userManager;
     private final Authenticator authenticator;
 
     public RefImplUserManager(final AuthenticationContext authenticationContext, final UserManager userManager,
         final GroupManager groupManager, final Authenticator authenticator)
     {
         this.authenticationContext = assertNotNull(authenticationContext, "authenticationContext");
         this.userManager = assertNotNull(userManager, "userManager");
         this.groupManager = assertNotNull(groupManager, "groupManager");
         this.authenticator = assertNotNull(authenticator, "authenticator");
     }
 
     public String getRemoteUsername()
     {
         final Principal user = authenticationContext.getUser();
         if (user == null)
             return null;
         return user.getName();
     }
 
     public String getRemoteUsername(final HttpServletRequest request)
     {
         return request.getRemoteUser();
     }
 
     public UserProfile getUserProfile(String username)
     {
         final User user;
         try
         {
            user = userManager.getUser(username);
            return new RefimplUserProfile(user);
         }
         catch (EntityException e)
         {
             return null;
         }
    }
 
 
     public boolean isSystemAdmin(final String username)
     {
         return isUserInGroup(username, "system_administrators");
     }
 
     public boolean isAdmin(final String username)
     {
         return isSystemAdmin(username) || isUserInGroup(username, "administrators");
     }
 
     public boolean authenticate(final String username, final String password)
     {
         try
         {
             final boolean authenticated = authenticator.authenticate(username, password);
             if (!authenticated)
             {
                 log.info("Cannot authenticate user '" + username + "' as they used an incorrect password");
             }
             return authenticated;
         }
         catch (final EntityException e)
         {
             log.info("Cannot authenticate user '" + username + "' as they do not exist.");
             return false;
         }
     }
 
     public boolean isUserInGroup(final String username, final String group)
     {
         try
         {
             final User user = userManager.getUser(username);
             final Group adminGroup = groupManager.getGroup(group);
             return groupManager.hasMembership(adminGroup, user);
         }
         catch (final EntityException e)
         {
             return false;
         }
     }
 
     public Principal resolve(final String username) throws UserResolutionException
     {
         try
         {
             if (userManager.getUser(username)==null)
             {
                 return null;
             }
         } catch (final EntityException e)
         {
             throw new UserResolutionException("Exception resolving user  '" + username + "'.", e);
         }
         return new Principal()
         {
             public String getName()
             {
                 return username;
             }
         };
     }
 
     /**
      * Check that {@code reference} is not {@code null}. If it is, throw a
      * {@code IllegalArgumentException}.
      *
      * @param reference
      *            reference to check is {@code null} or not
      * @param errorMessage
      *            com.atlassian.refapp.sal.message passed to the {@code IllegalArgumentException} constructor
      *            to give more context when debugging
      * @return {@code reference} so it may be used
      * @throws IllegalArgumentException
      *             if {@code reference} is {@code null}
      */
     private static <T> T assertNotNull(final T reference, final Object errorMessage)
     {
         if (reference == null)
         {
             throw new IllegalArgumentException(String.valueOf(errorMessage));
         }
         return reference;
     }
 }
