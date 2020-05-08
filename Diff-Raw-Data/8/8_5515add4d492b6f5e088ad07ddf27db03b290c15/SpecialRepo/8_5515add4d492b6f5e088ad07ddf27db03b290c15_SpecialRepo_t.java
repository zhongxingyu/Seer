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
 * $Id: SpecialRepo.java,v 1.3 2006-02-05 06:29:21 veiming Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.idm.plugins.internal;
 
 import java.security.AccessController;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.security.auth.callback.Callback;
 import javax.security.auth.callback.NameCallback;
 import javax.security.auth.callback.PasswordCallback;
 import javax.security.auth.login.LoginException;
 
 import com.iplanet.am.sdk.IdRepoListener;
 import com.iplanet.am.util.Debug;
 import com.iplanet.services.ldap.ServerConfigMgr;
 import com.iplanet.services.util.Hash;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.authentication.internal.AuthSubject;
 import com.sun.identity.authentication.internal.server.SMSAuthModule;
 import com.sun.identity.authentication.spi.AuthLoginException;
 import com.sun.identity.authentication.util.ISAuthConstants;
 import com.sun.identity.common.CaseInsensitiveHashSet;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.idm.IdOperation;
 import com.sun.identity.idm.IdRepo;
 import com.sun.identity.idm.IdRepoBundle;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdRepoUnsupportedOpException;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idm.RepoSearchResults;
 import com.sun.identity.security.AdminPasswordAction;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SchemaType;
 import com.sun.identity.sm.ServiceConfig;
 import com.sun.identity.sm.ServiceConfigManager;
 import com.sun.identity.sm.ServiceListener;
 import com.sun.identity.sm.ServiceSchemaManager;
 
 public class SpecialRepo extends IdRepo implements ServiceListener {
     public static final String NAME = 
         "com.sun.identity.idm.plugins.internal.SpecialRepo";
 
     IdRepoListener repoListener = null;
 
     Debug debug = null;
 
     private Map supportedOps = new HashMap();
 
     ServiceSchemaManager ssm = null;
 
     ServiceConfigManager scm = null;
 
     ServiceConfig globalConfig, userConfig, roleConfig;
 
     String ssmListenerId, scmListenerId;
 
     public SpecialRepo() {
         loadSupportedOps();
         debug = Debug.getInstance("amSpecialRepo");
         if (debug.messageEnabled()) {
             debug.message(": SpecialRepo invoked");
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#addListener(com.iplanet.sso.SSOToken,
      *      com.iplanet.am.sdk.IdRepoListener)
      */
     public int addListener(SSOToken token, IdRepoListener listener)
             throws IdRepoException, SSOException {
         if (debug.messageEnabled()) {
             debug.message(": SpecialRepo addListener");
         }
         repoListener = listener;
         try {
             if (ssm == null) {
                 ssm = new ServiceSchemaManager(token, IdConstants.REPO_SERVICE,
                         "1.0");
             }
             if (scm == null) {
                 scm = new ServiceConfigManager(token, IdConstants.REPO_SERVICE,
                         "1.0");
             }
             ssmListenerId = ssm.addListener(this);
             scmListenerId = scm.addListener(this);
         } catch (SMSException smse) {
             debug.error("SpecialRepo.addListener: Unable to add listener to SM"
                     + " Updates to special users will not reflect", smse);
         }
         return 0;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#assignService(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      com.sun.identity.sm.SchemaType, java.util.Map)
      */
     public void assignService(SSOToken token, IdType type, String name,
             String serviceName, SchemaType stype, Map attrMap)
             throws IdRepoException, SSOException {
         Object args[] = { NAME, IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#create(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map)
      */
     public String create(SSOToken token, IdType type, String name, Map attrMap)
             throws IdRepoException, SSOException {
 
         Object args[] = { NAME, IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#delete(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String)
      */
     public void delete(SSOToken token, IdType type, String name)
             throws IdRepoException, SSOException {
         Object args[] = { NAME, IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getAssignedServices(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map)
      */
     public Set getAssignedServices(SSOToken token, IdType type, String name,
             Map mapOfServicesAndOCs) throws IdRepoException, SSOException {
         Object args[] = { NAME, IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getAttributes(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set)
      */
     public Map getAttributes(SSOToken token, IdType type, String name,
             Set attrNames) throws IdRepoException, SSOException {
         return getAttributes(token, type, name);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getAttributes(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String)
      */
     public Map getAttributes(SSOToken token, IdType type, String name)
             throws IdRepoException, SSOException {
         if (type.equals(IdType.USER) || type.equals(IdType.ROLE)) {
             try {
                 if (globalConfig == null) {
                     if (scm == null) {
                         scm = new ServiceConfigManager(token,
                                 IdConstants.REPO_SERVICE, "1.0");
                     }
                     globalConfig = scm.getGlobalConfig(null);
                 }
                 if (type.equals(IdType.USER)) {
                     if (userConfig == null) {
                         userConfig = globalConfig.getSubConfig("users");
                     }
                     // Check if the user is present
                     for (Iterator items = userConfig.getSubConfigNames()
                             .iterator(); items.hasNext();) {
                         String n = (String) items.next();
                         if (n.equalsIgnoreCase(name)) {
                             ServiceConfig usc1 = userConfig.getSubConfig(name);
                             if (usc1 != null) {
                                 // Return without the userPassword attribute
                                 // BugID: 6309830
                                 Map answer = usc1.getAttributes();
                                 if (name.equalsIgnoreCase(
                                     IdConstants.AMADMIN_USER)
                                         || name.equalsIgnoreCase(
                                    IdConstants.ANONYMOUS_USER)) {
                                     // The passwords for these would
                                     // be returned from LDAP
                                     answer.remove("userPassword");
                                 }
                                 return (answer);
                             }
                         }
                     }
                     // User not found, thrown exception
                     Object args[] = { name };
                     throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "202",
                             args);
                 } else if (type.equals(IdType.ROLE)) {
                     if (roleConfig == null) {
                         roleConfig = globalConfig.getSubConfig("roles");
                     }
                     // Check if the role is present
                     for (Iterator items = roleConfig.getSubConfigNames()
                             .iterator(); items.hasNext();) {
                         String n = (String) items.next();
                         if (n.equalsIgnoreCase(name)) {
                             ServiceConfig usc1 = roleConfig.getSubConfig(name);
                             if (usc1 != null) {
                                 return usc1.getAttributes();
                             }
                         }
                     }
                     // Role not found, thrown exception
                     Object args[] = { name };
                     throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "202",
                             args);
                 }
 
             } catch (SMSException smse) {
                 debug.error("SpecialRepo: Unable to read user attributes ",
                         smse);
                 Object args[] = { NAME };
                 throw new IdRepoException(
                         IdRepoBundle.BUNDLE_NAME, "200", args);
             }
         }
         Object args[] = { NAME, IdOperation.READ.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getBinaryAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set)
      */
     public Map getBinaryAttributes(SSOToken token, IdType type, String name,
             Set attrNames) throws IdRepoException, SSOException {
         Object args[] = { NAME, IdOperation.READ.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#setBinaryAttributes(
      *      com.iplanet.sso.SSOToken, com.sun.identity.idm.IdType,
      *      java.lang.String, java.util.Map, boolean)
      */
     public void setBinaryAttributes(SSOToken token, IdType type, String name,
             Map attributes, boolean isAdd) throws IdRepoException, SSOException 
     {
         Object args[] = { NAME, IdOperation.EDIT.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getMembers(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String,
      *      com.sun.identity.idm.IdType)
      */
     public Set getMembers(SSOToken token, IdType type, String name,
             IdType membersType) throws IdRepoException, SSOException {
         if (type.equals(IdType.ROLE) && membersType.equals(IdType.USER)) {
             try {
                 Set members = Collections.EMPTY_SET;
                 if (roleConfig == null) {
                     if (globalConfig == null) {
                         if (scm == null) {
                             scm = new ServiceConfigManager(token,
                                     IdConstants.REPO_SERVICE, "1.0");
                         }
                         globalConfig = scm.getGlobalConfig(null);
                     }
                     roleConfig = globalConfig.getSubConfig("roles");
                 }
                 // For performance reasons we get the set of
                 // subConfigNames and check if "name" is present before
                 // getting the subconfig which makes a datastore call
                 Set roleNames = roleConfig.getSubConfigNames();
                 ServiceConfig rConfig = null;
                 if (roleNames != null && !roleNames.isEmpty()) {
                     CaseInsensitiveHashSet rns = new CaseInsensitiveHashSet();
                     rns.addAll(roleNames);
                     if (rns.contains(name)) {
                         rConfig = roleConfig.getSubConfig(name);
                     }
                 }
                 if (rConfig != null) {
                     Map attrs = rConfig.getAttributes();
                     members = (Set) attrs.get("members");
                 } else {
                     // Role does not exist in this Repo
                     if (debug.messageEnabled()) {
                         debug.message("SpecialRepo:getMembers failed for"
                                 + name);
                     }
                     Object args[] = { NAME, type.getName(), name };
                     throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "207",
                             args);
                 }
                 return members;
             } catch (SMSException smse) {
                 if (debug.warningEnabled()) {
                     debug.warning(
                             "SpecialRepo: Unable to read user attributes ",
                             smse);
                 }
                 Object args[] = { NAME };
                 throw new IdRepoException(
                         IdRepoBundle.BUNDLE_NAME, "200", args);
             }
         }
         Object args[] = { NAME, IdOperation.READ.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getMemberships(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String,
      *      com.sun.identity.idm.IdType)
      */
     public Set getMemberships(SSOToken token, IdType type, String name,
             IdType membershipType) throws IdRepoException, SSOException {
 
         if (type.equals(IdType.USER) && membershipType.equals(IdType.ROLE)) {
             try {
                 if (userConfig == null) {
                     if (globalConfig == null) {
                         if (scm == null) {
                             scm = new ServiceConfigManager(token,
                                     IdConstants.REPO_SERVICE, "1.0");
                         }
                         globalConfig = scm.getGlobalConfig(null);
                     }
                     userConfig = globalConfig.getSubConfig("users");
                 }
                 // For performance reasons we get the set of
                 // subConfigNames and check if "name" is present before
                 // getting the subconfig which makes a datastore call
                 Set userNames = userConfig.getSubConfigNames();
                 ServiceConfig uConfig = null;
                 if (userNames != null && !userNames.isEmpty()) {
                     CaseInsensitiveHashSet uns = new CaseInsensitiveHashSet();
                     uns.addAll(userNames);
                     if (uns.contains(name)) {
                         uConfig = userConfig.getSubConfig(name);
                     }
                 }
                 if (uConfig != null) {
                     Map attrs = uConfig.getAttributes();
                     return ((Set) attrs.get("roles"));
                 } else {
                     // User does not exist in this Repo
                     if (debug.messageEnabled()) {
                         debug.message("SpecialRepo:getMemberships failed for"
                                 + name);
                     }
                     Object args[] = { NAME, type.getName(), name };
                     throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "207",
                             args);
                 }
             } catch (SMSException smse) {
                 debug.error("SpecialRepo: Unable to read user attributes ",
                         smse);
                 Object args[] = { NAME };
                 throw new IdRepoException(
                         IdRepoBundle.BUNDLE_NAME, "200", args);
             }
         }
         Object args[] = { NAME, IdOperation.READ.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getServiceAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      java.util.Set)
      */
     public Map getServiceAttributes(SSOToken token, IdType type, String name,
             String serviceName, Set attrNames) throws IdRepoException,
             SSOException {
         // Check if the name is present
         if (type.equals(IdType.USER)) {
             try {
                 if (userConfig == null) {
                     if (globalConfig == null) {
                         if (scm == null) {
                             scm = new ServiceConfigManager(token,
                                     IdConstants.REPO_SERVICE, "1.0");
                         }
                         globalConfig = scm.getGlobalConfig(null);
                     }
                     userConfig = globalConfig.getSubConfig("users");
                 }
                 Set userSet = new CaseInsensitiveHashSet();
                 userSet.addAll(userConfig.getSubConfigNames());
                 if (userSet != null && userSet.contains(name)) {
                     return (Collections.EMPTY_MAP);
                 }
             } catch (SMSException smse) {
                 if (debug.warningEnabled()) {
                     debug.warning(
                             "SpecialRepo: Unable to get service attributes ",
                             smse);
                 }
                 Object args[] = { NAME, type.getName(), name };
                 throw new IdRepoException(
                         IdRepoBundle.BUNDLE_NAME, "212", args);
             }
         }
         // Throw exception otherwise
         Object args[] = { NAME, IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#isExists(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String)
      */
     public boolean isExists(SSOToken token, IdType type, String name)
             throws IdRepoException, SSOException {
         Object args[] = { NAME, IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#modifyMemberShip(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set,
      *      com.sun.identity.idm.IdType, int)
      */
     public void modifyMemberShip(SSOToken token, IdType type, String name,
             Set members, IdType membersType, int operation)
             throws IdRepoException, SSOException {
 
         Object args[] = { NAME, IdOperation.EDIT.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#modifyService(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      com.sun.identity.sm.SchemaType, java.util.Map)
      */
     public void modifyService(SSOToken token, IdType type, String name,
             String serviceName, SchemaType sType, Map attrMap)
             throws IdRepoException, SSOException {
         Object args[] = { NAME, IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#removeAttributes(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Set)
      */
     public void removeAttributes(SSOToken token, IdType type, String name,
             Set attrNames) throws IdRepoException, SSOException {
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#removeListener()
      */
     public void removeListener() {
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#search(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, int, int,
      *      java.util.Set, boolean, int, java.util.Map, boolean)
      */
     public RepoSearchResults search(SSOToken token, IdType type,
             String pattern, int maxTime, int maxResults, Set returnAttrs,
             boolean returnAllAttrs, int filterOp, Map avPairs,
             boolean recursive)
             throws IdRepoException, SSOException {
         Set userRes = new HashSet();
         Map userAttrs = new HashMap();
         int errorCode = RepoSearchResults.SUCCESS;
         try {
             // Get the global config
             if (globalConfig == null) {
                 // Check ServiceConfigManager
                 if (scm == null) {
                     scm = new ServiceConfigManager(token,
                             IdConstants.REPO_SERVICE, "1.0");
                 }
                 globalConfig = scm.getGlobalConfig(null);
             }
             if (type.equals(IdType.USER)) {
                 if (userConfig == null) {
                     userConfig = globalConfig.getSubConfig("users");
                 }
                 // Support aliasing for "uid" at least..
                 if (pattern.equals("*") && avPairs != null
                         && !avPairs.isEmpty()) {
                     Set uidVals = (Set) avPairs.get("uid");
                     if (uidVals != null && !uidVals.isEmpty()) {
                         pattern = (String) uidVals.iterator().next();
                     } else {
                         // pattern is "*" and avPairs is not empty, so return
                         // empty results
                         return new RepoSearchResults(Collections.EMPTY_SET,
                                 RepoSearchResults.SUCCESS,
                                 Collections.EMPTY_MAP, type);
                     }
                 }
 
                 // If wild card is used for pattern, do a search else a lookup
                 if (pattern.indexOf('*') != -1) {
                     userRes = userConfig.getSubConfigNames(pattern);
                 } else {
                     for (Iterator items = userConfig.getSubConfigNames()
                             .iterator(); items.hasNext();) {
                         String name = (String) items.next();
                         if (name.equalsIgnoreCase(pattern)) {
                             userRes.add(pattern);
                             break;
                         }
                     }
                 }
 
                 if (userRes != null) {
                     Iterator it = userRes.iterator();
                     while (it.hasNext()) {
                         String u = (String) it.next();
                         ServiceConfig thisUser = userConfig.getSubConfig(u);
                         Map attrs = thisUser.getAttributes();
                         // Return without the userPassword attribute
                         // BugID: 6309830
                         if (u.equalsIgnoreCase(
                                     IdConstants.AMADMIN_USER)
                                 || u.equalsIgnoreCase(
                                    IdConstants.ANONYMOUS_USER)) {
                             // The passwords for these would
                             // be returned from LDAP
                             attrs.remove("userPassword");
                         }
                         userAttrs.put(u, attrs);
                     }
                 }
                 return new RepoSearchResults(userRes, errorCode, userAttrs,
                         type);
 
             } else if (type.equals(IdType.ROLE)) {
                 if (roleConfig == null) {
                     roleConfig = globalConfig.getSubConfig("roles");
                 }
                 // If wild card is used for pattern, do a search else a lookup
                 if (pattern.indexOf('*') != -1) {
                     userRes = roleConfig.getSubConfigNames(pattern);
                 } else {
                     for (Iterator items = roleConfig.getSubConfigNames()
                             .iterator(); items.hasNext();) {
                         String name = (String) items.next();
                         if (name.equalsIgnoreCase(pattern)) {
                             userRes.add(pattern);
                             break;
                         }
                     }
                 }
 
                 if (userRes != null) {
                     Iterator it = userRes.iterator();
                     while (it.hasNext()) {
                         String u = (String) it.next();
                         ServiceConfig thisUser = roleConfig.getSubConfig(u);
                         Map attrs = thisUser.getAttributes();
                         userAttrs.put(u, attrs);
                     }
                 }
                 return new RepoSearchResults(userRes, errorCode, userAttrs,
                         type);
             } else {
                 return new RepoSearchResults(Collections.EMPTY_SET,
                         RepoSearchResults.SUCCESS, Collections.EMPTY_MAP, type);
             }
         } catch (SMSException smse) {
             debug.error("SpecialRepo.search: Unable to retrieve entries: ",
                     smse);
             Object args[] = { NAME };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "219", args);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#search(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map,
      *      boolean, int, int, java.util.Set)
      */
     public RepoSearchResults search(SSOToken token, IdType type,
             String pattern, Map avPairs, boolean recursive, int maxResults,
             int maxTime, Set returnAttrs) throws IdRepoException, SSOException {
         return (search(token, type, pattern, maxTime, maxResults, returnAttrs,
                 (returnAttrs == null), OR_MOD, avPairs, recursive));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#setAttributes(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.util.Map,
      *      boolean)
      */
     public void setAttributes(SSOToken token, IdType type, String name,
             Map attributes, boolean isAdd) throws IdRepoException, SSOException
     {
         if (type.equals(IdType.USER)) {
             try {
                 if (userConfig == null) {
                     if (globalConfig == null) {
                         if (scm == null) {
                             scm = new ServiceConfigManager(token,
                                     IdConstants.REPO_SERVICE, "1.0");
                         }
                         globalConfig = scm.getGlobalConfig(null);
                     }
                     userConfig = globalConfig.getSubConfig("users");
                 }
                 // For performance reason check if the user entry
                 // is present before getting the subConfig
                 CaseInsensitiveHashSet userSet = new CaseInsensitiveHashSet();
                 userSet.addAll(userConfig.getSubConfigNames());
                 if (userSet.contains(name)) {
                     ServiceConfig usc1 = userConfig.getSubConfig(name);
                     Map attrs = usc1.getAttributes();
                     // can only set "userpassword" and "inetUserStatus"
                     String newPassword = null;
                     Set vals = (Set) attributes.get("userPassword");
                     if (vals != null
                             || (vals = (Set) attributes.get("userpassword")) 
                             != null) 
                     {
                         Set hashedVals = new HashSet();
                         Iterator it = vals.iterator();
                         while (it.hasNext()) {
                             String val = (String) it.next();
                             hashedVals.add(Hash.hash(val));
                             newPassword = val;
                         }
                         attrs.put("userPassword", hashedVals);
                     }
                     if ((vals = (Set) attributes.get("inetUserStatus")) != null
                             || (vals = (Set) attributes.get("inetuserstatus")) 
                             != null) 
                     {
                         attrs.put("inetUserStatus", vals);
                     }
                     usc1.setAttributes(attrs);
                     // If password is changed for dsameuser, need to
                     // update serverconfig.xml and directory
                     if (name.equalsIgnoreCase("dsameuser")) {
                         String op = (String) AccessController
                                 .doPrivileged(new AdminPasswordAction());
                         try {
                             ServerConfigMgr sscm = new ServerConfigMgr();
                             sscm.setAdminUserPassword(op, newPassword);
                             sscm.save();
                         } catch (Exception e) {
                             debug.error("SpecialRepo: error in "
                                     + "changing password", e);
                         }
                     }
                 } else {
                     Object args[] = { name };
                     throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "202",
                             args);
                 }
             } catch (SMSException smse) {
                 debug
                         .error("SpecialRepo: Unable to set user attributes ",
                                 smse);
                 Object args[] = { NAME, type.getName(), name };
                 throw new IdRepoException(
                         IdRepoBundle.BUNDLE_NAME, "212", args);
             }
         } else {
             Object args[] = { NAME, IdOperation.READ.getName() };
             throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME,
                     "305", args);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#unassignService(
      *      com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String, java.lang.String,
      *      java.util.Map)
      */
     public void unassignService(SSOToken token, IdType type, String name,
             String serviceName, Map attrMap) throws IdRepoException,
             SSOException {
         Object args[] = {
                 "com.sun.identity.idm.plugins.specialusers.SpecialRepo",
                 IdOperation.SERVICE.getName() };
         throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME, "305",
                 args);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getSupportedOperations(
      *      com.sun.identity.idm.IdType)
      */
     public Set getSupportedOperations(IdType type) {
         if (debug.messageEnabled()) {
             debug
                     .message("SpecialRepo: getSupportedOps on " + type
                             + " called");
             debug.message("SpecialRepo: supportedOps Map = "
                     + supportedOps.toString());
         }
         return (Set) supportedOps.get(type);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#getSupportedTypes()
      */
     public Set getSupportedTypes() {
         return supportedOps.keySet();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#initialize(java.util.Map)
      */
     public void initialize(Map configParams) {
         super.initialize(configParams);
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#isActive(com.iplanet.sso.SSOToken,
      *      com.sun.identity.idm.IdType, java.lang.String)
      */
     public boolean isActive(SSOToken token, IdType type, String name)
             throws IdRepoException, SSOException {
         Map attributes = getAttributes(token, type, name);
         if (attributes == null) {
             Object[] args = { NAME, name };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "202", args);
         }
         Set activeVals = (Set) attributes.get("inetUserStatus");
         if (activeVals == null || activeVals.isEmpty()) {
             return true;
         } else {
             Iterator it = activeVals.iterator();
             String active = (String) it.next();
             return (active.equalsIgnoreCase("active") ? true : false);
         }
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.idm.IdRepo#shutdown()
      */
     public void shutdown() {
         scm.removeListener(scmListenerId);
         ssm.removeListener(ssmListenerId);
     }
 
     private void loadSupportedOps() {
         Set opSet = new HashSet();
         opSet.add(IdOperation.EDIT);
         opSet.add(IdOperation.READ);
         opSet.add(IdOperation.SERVICE);
 
         Set opSet2 = new HashSet(opSet);
         opSet2.remove(IdOperation.EDIT);
         opSet2.remove(IdOperation.SERVICE);
         supportedOps.put(IdType.USER, Collections.unmodifiableSet(opSet));
         supportedOps.put(IdType.ROLE, Collections.unmodifiableSet(opSet2));
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.sm.ServiceListener#globalConfigChanged(
      *      java.lang.String,
      *      java.lang.String, java.lang.String, java.lang.String, int)
      */
     public void globalConfigChanged(String serviceName, String version,
             String groupName, String serviceComponent, int type) {
         repoListener.allObjectsChanged();
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.sm.ServiceListener#organizationConfigChanged(
      *      java.lang.String,
      *      java.lang.String, java.lang.String, java.lang.String,
      *      java.lang.String, int)
      */
     public void organizationConfigChanged(String serviceName, String version,
             String orgName, String groupName, String serviceComponent, int type)
     {
         repoListener.allObjectsChanged();
 
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see com.sun.identity.sm.ServiceListener#schemaChanged(java.lang.String,
      *      java.lang.String)
      */
     public void schemaChanged(String serviceName, String version) {
         repoListener.allObjectsChanged();
     }
 
     public String getFullyQualifiedName(SSOToken token, IdType type, 
             String name) throws IdRepoException, SSOException {
         RepoSearchResults results = search(token, type, name, null, true, 0, 0,
                 null);
         Set dns = results.getSearchResults();
         if (dns.size() != 1) {
             String[] args = { name };
             throw (new IdRepoException(IdRepoBundle.BUNDLE_NAME, "220", args));
         }
         return ("sms://specialRepo/" + dns.iterator().next().toString());
     }
 
     public boolean supportsAuthentication() {
         return (true);
     }
 
     public boolean authenticate(Callback[] credentials) throws IdRepoException,
             AuthLoginException {
         debug.message("SpecialRepo:authenticate called");
 
         // Obtain user name and password from credentials and authenticate
         String username = null;
         String password = null;
         for (int i = 0; i < credentials.length; i++) {
             if (credentials[i] instanceof NameCallback) {
                 username = ((NameCallback) credentials[i]).getName();
                 if (debug.messageEnabled()) {
                     debug.message("SpecialRepo:authenticate username: "
                             + username);
                 }
             } else if (credentials[i] instanceof PasswordCallback) {
                 char[] passwd = ((PasswordCallback) credentials[i])
                         .getPassword();
                 if (passwd != null) {
                     password = new String(passwd);
                     debug.message("SpecialRepo:authN passwd present");
                 }
             }
         }
         if (username == null || password == null) {
             return (false);
         }
         Map sharedState = new HashMap();
         sharedState.put(ISAuthConstants.SHARED_STATE_USERNAME, username);
         sharedState.put(ISAuthConstants.SHARED_STATE_PASSWORD, password);
         debug.message("SpecialRepo:authenticate inst. SMSAuthModule");
 
         SMSAuthModule module = new SMSAuthModule();
         debug.message("SpecialRepo:authenticate SMSAuthModule:init");
         module.initialize(new AuthSubject(), null, sharedState,
                 Collections.EMPTY_MAP);
         boolean answer = false;
         try {
             answer = module.login();
             if (debug.messageEnabled()) {
                 debug.message("SpecialRepo:authenticate login: " + answer);
             }
         } catch (LoginException le) {
             if (debug.warningEnabled()) {
                 debug.warning("authentication: login exception", le);
             }
             if (le instanceof AuthLoginException) {
                 throw ((AuthLoginException) le);
             }
         }
         return (answer);
     }
 }
