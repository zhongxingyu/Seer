 package org.synyx.minos.core.authentication;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Required;
 import org.springframework.security.access.AccessDecisionManager;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.access.ConfigAttribute;
 import org.springframework.security.access.SecurityConfig;
 import org.springframework.security.authentication.InsufficientAuthenticationException;
 import org.springframework.security.authentication.dao.SaltSource;
 import org.springframework.security.authentication.encoding.PasswordEncoder;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.synyx.hades.domain.auditing.AuditorAware;
 import org.synyx.minos.core.domain.User;
 import org.synyx.minos.security.MinosUserDetails;
 import org.synyx.minos.umt.dao.UserDao;
 
 
 /**
  * Implementation of the {@code AuthenticationService} to use Spring Security.
  * 
  * @author Oliver Gierke - gierke@synyx.de
  */
 public class SpringSecurityAuthenticationService extends
         AbstractAuthenticationService implements AuditorAware<User> {
 
     private static final Log LOG =
             LogFactory.getLog(SpringSecurityAuthenticationService.class);
 
     private UserDao userDao;
     private AccessDecisionManager accessDecisionManager;
     private SaltSource saltSource;
     private PasswordEncoder passwordEncoder;
 
 
     /**
      * @param userDao the userDao to set
      */
     @Required
     public void setUserDao(UserDao userDao) {
 
         this.userDao = userDao;
     }
 
 
     /**
      * @param accessDecisionManager
      */
     @Required
     public void setAccessDecisionManager(
             AccessDecisionManager accessDecisionManager) {
 
         this.accessDecisionManager = accessDecisionManager;
     }
 
 
     /**
      * @param saltSource the saltSource to set
      */
     @Required
     public void setSaltSource(SaltSource saltSource) {
 
         this.saltSource = saltSource;
     }
 
 
     /**
      * @param passwordEncoder the passwordEncoder to set
      */
     @Required
     public void setPasswordEncoder(PasswordEncoder passwordEncoder) {
 
         this.passwordEncoder = passwordEncoder;
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.synyx.hades.domain.support.AuditorAware#getCurrentAuditor()
      */
     @Override
     public User getCurrentAuditor() {
 
         return getCurrentUser();
 
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.synyx.minos.core.authentication.AuthenticationService#getCurrentUser
      * ()
      */
     @Override
     public User getCurrentUser() {
 
         UserDetails userDetails = getAuthenticatedUser();
 
         return null == userDetails ? null : userDao.findByUsername(userDetails
                 .getUsername());
     }
 
 
     /*
      * (non-Javadoc)
      * 
      * @seecom.synyx.minos.core.authentication.AuthenticationService#
      * getEncryptedPasswordFor(com.synyx.minos.core.domain.User)
      */
     @Override
     public String getEncryptedPasswordFor(User user) {
 
         if (passwordEncoder == null) {
             return user.getPassword();
         }
 
         Object salt =
                 saltSource == null ? null : saltSource
                         .getSalt(new MinosUserDetails(user));
 
         return passwordEncoder.encodePassword(user.getPassword(), salt);
     }
 
 
     /**
      * Checks the current authentication for the given permissions.
      * 
      * @param permissions
      * @return whether the currently authenticated {@link User} has the given
      *         permissions. Will return {@literal false} if {@literal null} or
      *         an empty collection is given.
      */
     @Override
     protected boolean hasPermissions(Collection<String> permissions) {
 
         if (null == permissions || permissions.isEmpty()) {
             return false;
         }
 
         Authentication authentication =
                 SecurityContextHolder.getContext().getAuthentication();
 
         if (authentication == null) {
             return false;
         }
 
         try {
             accessDecisionManager.decide(authentication, null,
                     toAttributes(permissions));
             return true;
         } catch (AccessDeniedException e) {
             LOG.debug("Access denied!", e);
             return false;
         } catch (InsufficientAuthenticationException e) {
             LOG.debug("Access denied!", e);
             return false;
         }
     }
 
 
     private List<ConfigAttribute> toAttributes(Collection<String> permissions) {
 
         List<ConfigAttribute> attributes = new ArrayList<ConfigAttribute>();
 
         for (String permission : permissions) {
             attributes.add(new SecurityConfig(permission.toString()));
         }
 
         return attributes;
     }
 
 
     private UserDetails getAuthenticatedUser() {
 
         Authentication authentication =
                 SecurityContextHolder.getContext().getAuthentication();
 
         if (null == authentication) {
             return null;
         }
 
	// Principal may be "anonymous", which is a string
	if (! (authentication.getPrincipal() instanceof UserDetails)) {
	    return null;
	}

         return (UserDetails) authentication.getPrincipal();
     }
 }
