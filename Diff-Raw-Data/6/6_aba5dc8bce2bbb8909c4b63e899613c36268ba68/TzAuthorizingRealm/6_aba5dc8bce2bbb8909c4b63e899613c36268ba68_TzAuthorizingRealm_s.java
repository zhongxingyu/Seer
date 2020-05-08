 /*
  *   Copyright 2012 George Norman
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package com.thruzero.auth.realm;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.apache.shiro.authc.AuthenticationException;
 import org.apache.shiro.authc.AuthenticationInfo;
 import org.apache.shiro.authc.AuthenticationToken;
 import org.apache.shiro.authc.SimpleAccount;
 import org.apache.shiro.authc.UsernamePasswordToken;
 import org.apache.shiro.authz.AuthorizationInfo;
 import org.apache.shiro.authz.Permission;
 import org.apache.shiro.authz.SimpleAuthorizationInfo;
 import org.apache.shiro.authz.permission.WildcardPermission;
 import org.apache.shiro.realm.AuthorizingRealm;
 import org.apache.shiro.subject.PrincipalCollection;
 
 import com.thruzero.auth.exception.MessageIdAuthenticationException;
 import com.thruzero.auth.model.User;
 import com.thruzero.auth.model.UserPermission;
 import com.thruzero.auth.service.UserService;
 import com.thruzero.auth.utils.AuthenticationUtils;
 import com.thruzero.common.core.locator.ServiceLocator;
 
 /**
  * Specialization of the Shiro AuthorizingRealm, that uses the tz-commons UserService to
  * load User and UserPermission objects for authentication and authorization.
  *
  * @author George Norman
  */
 public class TzAuthorizingRealm extends AuthorizingRealm {
   private static final Logger logger = Logger.getLogger(TzAuthorizingRealm.class);
 
   public static final String LOGIN_TOO_MANY_BAD_LOGIN_ATTEMPTS_ERROR = "login.tooManyBadLoginAttempts.error";
   public static final String LOGIN_INVALID_LOGIN_ERROR = "login.invalidLogin.error";
 
   @Override
   protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
     SimpleAuthorizationInfo result = new SimpleAuthorizationInfo();
 
     User user = (User)principals.getPrimaryPrincipal();
 
     for (UserPermission permission : user.getPermissions()) {
       result.addObjectPermission(new WildcardPermission(permission.getDomain() + ":" + permission.getActions()));
     }
 
     return result;
   }
 
   @Override
   protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
     try {
       AuthenticationInfo result;
       String submittedLoginId = token.getPrincipal().toString();
       char[] submittedOneTimePw = (char[])token.getCredentials();
       String nonce = null;
 
       if (token instanceof UsernamePasswordToken) { // TODO-p1(george) ugh - requires typecast to retrieve nonce
         nonce = ((UsernamePasswordToken)token).getHost(); // TODO-p1(george) ugh - using host as nonce; maybe should create a new subclass (e.g., TzUsernamePasswordToken)
       }
 
       UserService userService = ServiceLocator.locate(UserService.class);
       User user = userService.getUserByLoginId(submittedLoginId);
 
       if (AuthenticationUtils.isTooManyBadLoginAttempts(user)) {
         throw new MessageIdAuthenticationException(LOGIN_TOO_MANY_BAD_LOGIN_ATTEMPTS_ERROR);
       }
 
       if (AuthenticationUtils.isValidLogin(user, submittedLoginId, submittedOneTimePw, nonce)) {
         Set<String> roleNames = new HashSet<String>();
         Set<Permission> permissions = new HashSet<Permission>();
 
         for (UserPermission permission : user.getPermissions()) {
           permissions.add(new WildcardPermission(permission.getDomain() + ":" + permission.getActions()));
         }
 
         result = new SimpleAccount(user, token.getCredentials(), TzAuthorizingRealm.class.getSimpleName(), roleNames, permissions);
         AuthenticationUtils.resetBadLoginAttemptTracker(user);
         userService.saveUser(user);
       } else {
        AuthenticationUtils.handleBadLoginAttempt(user); // update bad login attempts or reset if time threshold has been exceeded
        userService.saveUser(user);
         throw new MessageIdAuthenticationException(LOGIN_INVALID_LOGIN_ERROR);
       }
 
       return result;
     } catch (MessageIdAuthenticationException e) {
       String err = "ERROR: failed to get authentication info.";
       logger.error(err, e);
       throw e; // rethrow, so it'll get caught and handled upstream
     } catch (Exception e) {
       String err = "ERROR: failed to get authentication info.";
       logger.error(err, e);
       throw new RuntimeException(err, e); // Something is seriously wrong; bail
     }
   }
 
 }
