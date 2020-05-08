 /* The contents of this file are subject to the terms
  * of the Common Development and Distribution License
  * (the License). You may not use this file except in
  * compliance with the License.
  *
  * You can obtain a copy of the License at
  * https://opensso.dev.java.net/public/CDDLv1.0.html or
  * opensso/legal/CDDLv1.0.txt
  * See the License for the specific language governing
  * permission and limitations under the License.
  *
  * When distributing Covered Code, include this CDDL
  * Header Notice in each file and include the License file
  * at opensso/legal/CDDLv1.0.txt.
  * If applicable, add the following below the CDDL Header,
  * with the fields enclosed by brackets [] replaced by
  * your own identifying information:
  * "Portions Copyrighted [year] [name of copyright owner]"
  *
 * $Id: IdentityServicesImpl.java,v 1.2 2007-09-10 19:38:00 arviranga Exp $
  *
  * Copyright 2007 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.idsvcs.opensso;
 
 import com.sun.identity.authentication.spi.AuthLoginException;
 import com.sun.identity.policy.PolicyException;
 
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 
 import com.sun.identity.authentication.AuthContext;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdUtils;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idsvcs.AccessDenied;
 import com.sun.identity.idsvcs.GeneralFailure;
 import com.sun.identity.idsvcs.InvalidCredentials;
 import com.sun.identity.idsvcs.InvalidPassword;
 import com.sun.identity.idsvcs.NeedMoreCredentials;
 import com.sun.identity.idsvcs.Token;
 import com.sun.identity.idsvcs.UserDetails;
 import com.sun.identity.idsvcs.UserNotFound;
 import com.sun.identity.idsvcs.TokenExpired;
 import com.sun.identity.idsvcs.Attribute;
 import com.sun.identity.log.LogRecord;
 import com.sun.identity.log.Logger;
 import com.sun.identity.log.AMLogException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOTokenManager;
 import com.sun.identity.idm.AMIdentityRepository;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idsvcs.LogResponse;
import com.sun.identity.policy.client.PolicyEvaluator;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.debug.Debug;
 
 
 import java.net.URI;
 import java.rmi.RemoteException;
 import java.security.AccessController;
 import java.util.HashSet;
 import java.util.Iterator;
 
 /**
  * Web Service to provide security based on authentication and authorization
  * support.
  */
 public class IdentityServicesImpl implements
     com.sun.identity.idsvcs.IdentityServicesImpl {
     
     // Debug
     private static Debug debug = Debug.getInstance("amIdentityServices");
     
     /**
      * Attempt to authenticate using simple user/password credentials.
      * @param username Subject's user name.
      * @param password Subject's password
      * @param uri Subject's context such as module, organization, etc.
      * @return Subject's token if authenticated.
      * @throws UserNotFound if user not found.
      * @throws InvalidPassword if password is invalid.
      * @throws NeedMoreCredentials if additional credentials are needed for
      * authentication.
      * @throws InvalidCredentials if credentials are invalid.
      * @throws GeneralFailure on other errors.
      */
     public Token authenticate(String username, String password, String uri)
         throws UserNotFound, InvalidPassword, NeedMoreCredentials,
         InvalidCredentials, GeneralFailure, RemoteException {
         
         assert username != null && password != null;
         Token ret = null;
         try {
             // Parse the URL to get realm, module, level, etc
             String realm = "/";
             String module = null;
             String level = null;
             if (uri != null) {
                 // Parse the uri parameters for realm, module, etc
                 // TODO
             }
             AuthContext lc = new AuthContext(realm);
             lc.login();
             while (lc.hasMoreRequirements()) {
                 Callback[] callbacks = lc.getRequirements();
                 ArrayList missing = new ArrayList();
                 // loop through the requires setting the needs..
                 for (int i = 0; i < callbacks.length; i++) {
                     if (callbacks[i] instanceof NameCallback) {
                         NameCallback nc = (NameCallback) callbacks[i];
                         nc.setName(username);
                     } else if (callbacks[i] instanceof PasswordCallback) {
                         PasswordCallback pc = (PasswordCallback) callbacks[i];
                         pc.setPassword(password.toCharArray());
                     } else {
                         missing.add(callbacks[i]);
                     }
                 }
                 // there's missing requirements not filled by this
                 if (missing.size() > 0) {
                     // need add the missing later..
                     throw new InvalidCredentials("");
                 }
                 lc.submitRequirements(callbacks);
             }
             // validate the password..
             if (lc.getStatus() != AuthContext.Status.SUCCESS) {
                 throw new InvalidPassword("");
             } else {
                 try {
                     // package up the token for transport..
                     ret = new Token();
                     String id = lc.getSSOToken().getTokenID().toString();
                     ret.setId(id);
                 } catch (Exception e) {
                     debug.error("IdentityServicesImpl:authContext " +
                         "AuthException", e);
                     // we're going to throw a generic error
                     // because the system is likely down..
                     throw new GeneralFailure(e.getMessage());
                 }
             }
         } catch (AuthLoginException le) {
             debug.error("IdentityServicesImpl:authContext AuthException", le);
             // we're going to throw a generic error
             // because the system is likely down..
             throw (new GeneralFailure(le.getMessage()));
         }
         return ret;
     }
 
     /**
      * Attempt to authorize the subject for the optional action on the
      * requested URI.
      * @param uri URI for which authorization is required
      * @param action Optional action for which subject is being authorized
      * @param subject Token identifying subject to be authorized
      * @return boolean <code>true</code> if allowed; <code>false</code>
      * otherwise
      * @throws NeedMoreCredentials when more credentials are required for
      * authorization.
      * @throws TokenExpired when subject's token has expired.
      * @throws GeneralFailure on other errors.
      */
     public boolean authorize(String uri, String action, Token subject)
         throws NeedMoreCredentials, TokenExpired, GeneralFailure,
         RemoteException {
         
         boolean isAllowed = false;
         // Check policy
         try {
             // create the SSOToken
             SSOToken ssoToken = SSOTokenManager.getInstance()
                 .createSSOToken(subject.getId());
 
             // Evaluate policy
             String serviceName = "iPlanetAMWebAgentService";
             String resource = uri;
             // Check if service name is encoded in uri
             // Format of uri with service name:
             //   service://<sevicename>/?resource=<resourcename>
             if (uri.toLowerCase().startsWith("service://")) {
                 URI iuri = new URI(uri);
                 serviceName = iuri.getHost();
                 resource = iuri.getQuery();
                 int index = resource.indexOf('=');
                 if (index > 0) {
                     resource = resource.substring(index);
                 }
             }
             PolicyEvaluator pe = new PolicyEvaluator(serviceName);
             if ((action == null) || (action.length() == 0)) {
                 action = "GET";
             }
             // Evaluate policy decisions
             if (pe.isAllowed(ssoToken, resource, action)) {
                 isAllowed = true;
             }
         } catch (SSOException e) {
             debug.error("IdentityServicesImpl:authorize", e);
             throw new GeneralFailure(e.getMessage());
         } catch (PolicyException ex) {
             debug.error("IdentityServicesImpl:authorize", ex);
             throw new GeneralFailure(ex.getMessage());
         } catch (URISyntaxException ex) {
             debug.error("IdentityServicesImpl:authorize", ex);
             throw new GeneralFailure(ex.getMessage());
         }
 
         return isAllowed;
     }
 
     /**
      * Logs a message on behalf of the authenticated app.
      *
      * @param app         Token corresponding to the authenticated application.
      * @param subject     Optional token identifying the subject for which the
      * log record pertains.
      * @param logName     Identifier for the log file, e.g. "MyApp.access"
      * @param message     String containing the message to be logged
      * @throws AccessDenied   if app token is not specified
      * @throws GeneralFailure on error
      */
     public LogResponse log(Token app, Token subject, String logName,
         String message) throws AccessDenied, TokenExpired, GeneralFailure,
         RemoteException {
         if (app == null) {
             throw new AccessDenied("No logging application token specified");
         }
 
         SSOToken appToken = null;
         SSOToken subjectToken = null;
         try {
             SSOTokenManager ssoTokenManager = SSOTokenManager.getInstance();
             appToken = ssoTokenManager.createSSOToken(app.getId());
             subjectToken = (subject == null) ? null :
                 ssoTokenManager.createSSOToken(subject.getId());
         } catch (SSOException e) {
             System.err.println("SSOException%n");
             throw new GeneralFailure(e.getMessage());
         }
 
         try {
             LogRecord logRecord = new LogRecord(
                 java.util.logging.Level.INFO, message, subjectToken);
             // todo Support internationalization via a resource bundle
             // specification
             Logger logger = (Logger) Logger.getLogger(logName);
             logger.log(logRecord, appToken);
             logger.flush();
         } catch (AMLogException e) {
             debug.error("IdentityServicesImpl:authorize", e);
             throw new GeneralFailure(e.getMessage());
         }
         return new LogResponse();
     }
 
 
     /**
      * Retrieve user details (roles, attributes) for the subject.
      * @param attributeNames Optional array of attributes to be returned
      * @param subject Token for subject.
      * @return User details for the subject.
      * @throws TokenExpired when Token has expired.
      * @throws GeneralFailure on other errors.
      */
     public UserDetails attributes(String[] attributeNames, Token subject)
         throws TokenExpired, GeneralFailure, RemoteException {
         List attrNames = null;
         if ((attributeNames != null) && (attributeNames.length > 0)) {
             attrNames = new ArrayList();
             for (int i = 0; i < attributeNames.length; i++) {
                 attrNames.add(attributeNames[i]);
             }
         }
         return attributes(attrNames, subject);
     }
 
     /**
      * Retrieve user details (roles, attributes) for the subject.
      * @param attributeNames Optional list of attributes to be returned
      * @param subject Token for subject.
      * @return User details for the subject.
      * @throws TokenExpired when Token has expired.
      * @throws GeneralFailure on other errors.
      */
     public UserDetails attributes(List attributeNames, Token subject)
         throws TokenExpired, GeneralFailure, RemoteException {
         UserDetails details = new UserDetails();
         try {
             SSOToken ssoToken = SSOTokenManager.getInstance()
                 .createSSOToken(subject.getId());
             
             // Obtain user memberships (roles and groups)
             AMIdentity userIdentity = IdUtils.getIdentity(ssoToken);
 
             // Determine the types that can have members
             SSOToken adminToken = (SSOToken) AccessController
                     .doPrivileged(AdminTokenAction.getInstance());
             AMIdentityRepository idrepo = new AMIdentityRepository(
                 adminToken, userIdentity.getRealm());
             Set supportedTypes = idrepo.getSupportedIdTypes();
             Set membersTypes = new HashSet();
             for (Iterator its = supportedTypes.iterator(); its.hasNext();) {
                 IdType type = (IdType) its.next();
                 if (type.canHaveMembers().contains(userIdentity.getType())) {
                     membersTypes.add(type);
                 }
             }
 
             // Determine the roles and groups
             List roles = new ArrayList();
             for (Iterator items = membersTypes.iterator(); items.hasNext();) {
                 IdType type = (IdType) items.next();
                 Set mems = userIdentity.getMemberships(type);
                 for (Iterator rs = mems.iterator(); rs.hasNext();) {
                     AMIdentity mem = (AMIdentity) rs.next();
                     roles.add(mem.getUniversalId());
                 }
             }
             String[] r = new String[roles.size()];
             details.setRoles((String[]) roles.toArray(r));
 
             Map userAttributes = null;
             if (attributeNames != null) {
                 Set attrNames = new HashSet(attributeNames);
                 userAttributes = userIdentity.getAttributes(attrNames);
             } else {
                 userAttributes = userIdentity.getAttributes();
             }
             if (userAttributes != null) {
                 List attributes = new ArrayList(userAttributes.size());
                 for (Iterator it = userAttributes.keySet().iterator();
                     it.hasNext();) {
                     Attribute attribute = new Attribute();
                     String name = it.next().toString();
                     attribute.setName(name);
                     Set value = (Set) userAttributes.get(name);
                     List valueList = new ArrayList(value.size());
                     // Convert the set to a List of String
                     if (value != null) {
                         for (Iterator valueIt = value.iterator();
                             valueIt.hasNext();) {
                             Object next = valueIt.next();
                             if (next != null) {
                                 valueList.add(next.toString());
                             }
                         }
                     }
                     String[] v = new String[valueList.size()];
                     attribute.setValues((String[]) valueList.toArray(v));
                     attributes.add(attribute);
                 }
                 Attribute[] a = new Attribute[attributes.size()];
                 details.setAttributes((Attribute[]) attributes.toArray(a));
             }
         } catch (IdRepoException e) {
             debug.error("IdentityServicesImpl:authorize", e);
             throw new GeneralFailure(e.getMessage());
         } catch (SSOException e) {
             debug.error("IdentityServicesImpl:authorize", e);
             throw new GeneralFailure(e.getMessage());
         }
 
         // todo handle token translation
         details.setToken(subject);
         return details;
     }
 }
