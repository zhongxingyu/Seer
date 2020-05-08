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
 * $Id: IdServicesImpl.java,v 1.17 2007-05-17 23:44:13 kenwho Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.idm.server;
 
 import java.security.AccessController;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import javax.security.auth.callback.Callback;
 
 import netscape.ldap.LDAPDN;
 import netscape.ldap.util.DN;
 
 import com.iplanet.am.sdk.AMHashMap;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.authentication.spi.AuthLoginException;
 import com.sun.identity.common.CaseInsensitiveHashMap;
 import com.sun.identity.common.CaseInsensitiveHashSet;
 import com.sun.identity.common.DNUtils;
 import com.sun.identity.delegation.DelegationEvaluator;
 import com.sun.identity.delegation.DelegationException;
 import com.sun.identity.delegation.DelegationPermission;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdConstants;
 import com.sun.identity.idm.IdOperation;
 import com.sun.identity.idm.IdRepo;
 import com.sun.identity.idm.IdRepoBundle;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdRepoFatalException;
 import com.sun.identity.idm.IdRepoListener;
 import com.sun.identity.idm.IdRepoServiceListener;
 import com.sun.identity.idm.IdRepoUnsupportedOpException;
 import com.sun.identity.idm.IdSearchControl;
 import com.sun.identity.idm.IdSearchOpModifier;
 import com.sun.identity.idm.IdSearchResults;
 import com.sun.identity.idm.IdServices;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idm.IdUtils;
 import com.sun.identity.idm.RepoSearchResults;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.shared.datastruct.OrderedSet;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.sm.DNMapper;
 import com.sun.identity.sm.OrganizationConfigManager;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SchemaType;
 import com.sun.identity.sm.ServiceConfig;
 import com.sun.identity.sm.ServiceConfigManager;
 import com.sun.identity.sm.ServiceManager;
 import com.sun.identity.sm.ServiceSchema;
 import com.sun.identity.sm.ServiceSchemaManager;
 
 public class IdServicesImpl implements IdServices {
 
     private final static String DELEGATION_ATTRS_NAME = "attributes";
 
     protected static Debug debug = Debug.getInstance("amIdm");
 
     protected Map idRepoMap = new HashMap();
 
     protected Set idRepoPlugins;
 
     protected String notificationID;
 
     protected ServiceSchemaManager idRepoServiceSchemaManager;
 
     protected ServiceSchema idRepoSubSchema;
 
     private static HashSet READ_ACTION = new HashSet(2);
 
     private static HashSet WRITE_ACTION = new HashSet(2);
 
     private static IdServices _instance;
 
     static {
         READ_ACTION.add("READ");
         WRITE_ACTION.add("MODIFY");
     }
 
     protected static synchronized IdServices getInstance() {
         if (_instance == null) {
             getDebug().message("IdServicesImpl.getInstance(): "
                     + "Creating new Instance of IdServicesImpl()");
             _instance = new IdServicesImpl();
         }
         return _instance;
     }
 
     protected IdServicesImpl() {
         initializeListeners();
     }
 
     protected static Debug getDebug() {
         return debug;
     }
 
     public void reinitialize() {
         initializeListeners();
     }
 
     private void initializeListeners() {
         if (notificationID == null) {
             // On the server side, configure and set up a service
             // listener for listening to changes made to plugin
             // configurations.
             if (getDebug().messageEnabled()) {
                 getDebug().message(
                     "IdServicesImpl.initializeListeners: "
                     + "setting up ServiceListener");
             }
             SSOToken stoken = (SSOToken) AccessController
                     .doPrivileged(AdminTokenAction.getInstance());
             try {
 
                 ServiceConfigManager ssm = new ServiceConfigManager(stoken,
                         IdConstants.REPO_SERVICE, "1.0");
                 ssm.addListener(new IdRepoServiceListener());
 
                 // Initialize schema objects
                 idRepoServiceSchemaManager = new ServiceSchemaManager(stoken,
                         IdConstants.REPO_SERVICE, "1.0");
                 idRepoSubSchema = idRepoServiceSchemaManager
                         .getOrganizationSchema();
                 idRepoPlugins = idRepoSubSchema.getSubSchemaNames();
                 IdRepoServiceListener sListener = new IdRepoServiceListener();
                 notificationID = idRepoServiceSchemaManager
                         .addListener(sListener);
 
                 // Should we just ignore these exceptions or throw them?
                 // Are'nt they Fatal that we should not start the system ??
             } catch (SMSException smse) {
                 if (getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.initializeListeners: "
                         + "Unable to set up a service listener for IdRepo",
                         smse);
                 }
             } catch (SSOException ssoe) {
                 getDebug().error(
                     "IdServicesImpl.initializeListeners: "
                     + "Unable to set up a service listener for IdRepo. ",
                     ssoe);
             }
         }
     }
     
     /**
      * Returns <code>true</code> if the data store has successfully
      * authenticated the identity with the provided credentials. In case the
      * data store requires additional credentials, the list would be returned
      * via the <code>IdRepoException</code> exception.
      * 
      * @param orgName
      *            realm name to which the identity would be authenticated
      * @param credentials
      *            Array of callback objects containing information such as
      *            username and password.
      * 
      * @return <code>true</code> if data store authenticates the identity;
      *         else <code>false</code>
      */
     public boolean authenticate(String orgName, Callback[] credentials)
             throws IdRepoException, AuthLoginException {
         if (getDebug().messageEnabled()) {
             getDebug().message(
                 "IdServicesImpl.authenticate: called for org: " + orgName);
         }
 
         IdRepoException firstException = null;
         // Get the list of plugins and check if they support authN
         SSOToken token = (SSOToken) AccessController
                 .doPrivileged(AdminTokenAction.getInstance());
         Set plugins = getIdRepoPlugins(token, orgName);
         Set cPlugins = null;
         try {        
             cPlugins = getAllConfiguredPlugins(token, orgName, plugins);
         } catch (SSOException ssoe) {
             // Here an admin token is being used and it should cause this 
             // exception. If caused it would be fatal one - report it.
             throw new IdRepoFatalException(ssoe.getL10NMessage());
         }
         for (Iterator items = cPlugins.iterator(); items.hasNext();) {
             IdRepo idRepo = (IdRepo) items.next();
             if (idRepo.supportsAuthentication()) {
                 if (getDebug().messageEnabled()) {
                     getDebug().message(
                         "IdServicesImpl.authenticate: "
                         + "AuthN to " + idRepo.getClass().getName()
                         + " in org: " + orgName);
                 }
                 try {
                     if (idRepo.authenticate(credentials)) {
                         // Successfully authenticated
                         if (getDebug().messageEnabled()) {
                             getDebug().message(
                                 "IdServicesImpl.authenticate: "
                                 + "AuthN success for "
                                 + idRepo.getClass().getName());
                         }
                         return (true);
                     }
                 } catch (IdRepoException ide) {
                     // Save the exception to be thrown later if
                     // all authentication calls fail
                     if (firstException == null) {
                         firstException = ide;
                     }
                 }
             } else if (getDebug().messageEnabled()) {
                 getDebug().message(
                     "IdServicesImpl.authenticate: "
                     + "AuthN not supported by " + idRepo.getClass().getName());
             }
         }
         if (firstException != null) {
             throw (firstException);
         }
         return (false);
     }
     
     
     private AMIdentity getRealmIdentity(SSOToken token, String orgDN)
             throws IdRepoException {
         String universalId = "id=ContainerDefaultTemplateRole,ou=realm,"
                 + orgDN;
         return IdUtils.getIdentity(token, universalId);
     }
 
     private AMIdentity getSubRealmIdentity(SSOToken token, String subRealmName,
             String parentRealmName) throws IdRepoException, SSOException {
         String realmName = parentRealmName;
         if (DN.isDN(parentRealmName)) {
             // Wouldn't be a DN if it starts with "/"
             realmName = DNMapper.orgNameToRealmName(parentRealmName);
         }
 
         String fullRealmName = realmName + IdConstants.SLASH_SEPARATOR
                 + subRealmName;
         String subOrganizationDN = DNMapper.orgNameToDN(fullRealmName);
 
         return getRealmIdentity(token, subOrganizationDN);
     }
 
     private AMIdentity createRealmIdentity(SSOToken token, IdType type,
             String name, Map attrMap, String orgName) throws IdRepoException,
             SSOException {
 
         try {
             OrganizationConfigManager orgMgr = new OrganizationConfigManager(
                     token, orgName);
 
             Map serviceAttrsMap = new HashMap();
             serviceAttrsMap.put(IdConstants.REPO_SERVICE, attrMap);
 
             orgMgr.createSubOrganization(name, serviceAttrsMap);
 
             return getSubRealmIdentity(token, name, orgName);
         } catch (SMSException sme) {
             debug.error("AMIdentityRepository.createIdentity() - "
                     + "Error occurred while creating " + type.getName() + ":"
                     + name, sme);
             throw new IdRepoException(sme.getMessage());
         }
     }
 
     public AMIdentity create(SSOToken token, IdType type, String name,
             Map attrMap, String amOrgName) throws IdRepoException, SSOException 
             {
         
         if (type.equals(IdType.REALM)) {                        
             return createRealmIdentity(token, type, name, attrMap, amOrgName);
         }
         
         IdRepoException origEx = null;
         // First get the list of plugins that support the create operation.
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, attrMap.keySet(),
                 IdOperation.CREATE, type);
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         String amsdkdn = null;
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.CREATE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
 
                 Map cMap = idRepo.getConfiguration(); // do stuff to map attr
                 // names.
                 attrMap = mapAttributeNames(attrMap, cMap);
                 String representation = idRepo.create(token, type, name,
                         attrMap);
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)) {
                     amsdkdn = representation;
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.create: "
                         + "Unable to create identity in the"
                         + " following repository "
                         + idRepo.getClass().getName() + ":: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.create: "
                     + "Create: Fatal Exception", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.create: "
                         + "Unable to create identity in the following "
                         + "repository "
                         + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         AMIdentity id = new AMIdentity(token, name, type, amOrgName, amsdkdn);
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.create: "
                     + "Unable to create identity " + type.getName() + " :: "
                     + name + " in any of the configured data stores", origEx);
             }
             throw origEx;
         } else {
             return id;
         }
 
     }
 
     private void deleteRealmIdentity(SSOToken token, String realmName,
             String orgDN) throws IdRepoException {
         
         try {
             // By default a Realm is not a leaf node, delete the
             // whole realm tree.
             String parentRealmName = DNMapper.orgNameToRealmName(orgDN);
             OrganizationConfigManager orgMgr =
                 new OrganizationConfigManager(token, parentRealmName);
             orgMgr.deleteSubOrganization(realmName, true);
         } catch (SMSException sme) {
             throw new IdRepoException(sme.getMessage());
         }
         
     }
         
     /*
      * (non-Javadoc)
      */
     public void delete(SSOToken token, IdType type, String name,
             String orgName, String amsdkDN) throws IdRepoException,
             SSOException {        
         
         if (type.equals(IdType.REALM)) {
             deleteRealmIdentity(token, name, orgName);
             return;
         }    
         
         IdRepoException origEx = null;
         // First get the list of plugins that support the create operation.
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, orgName, name, null, IdOperation.DELETE, type);
         Set plugIns = getIdRepoPlugins(token, orgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, orgName, plugIns,
                 IdOperation.DELETE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     idRepo.delete(token, type, amsdkDN);
                 } else {
                     idRepo.delete(token, type, name);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.delete: "
                         + "Unable to delete identity in the following "
                         + "repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.delete: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.delete: "
                         + "Unable to delete identity in the following "
                         + "repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 if (!ide.getErrorCode().equalsIgnoreCase("220")
                         || (origEx == null)) {
                     origEx = ide;
                 }
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.delete: "
                     + "Unable to delete identity " + type.getName() + " :: "
                     + name + " in any of the configured data stores", origEx);
             }
             throw origEx;
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public Map getAttributes(SSOToken token, IdType type, String name,
             Set attrNames, String amOrgName, String amsdkDN, boolean isString)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
 
         // First get the list of plugins that support the create operation.
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, attrNames, IdOperation.READ,
                 type);
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Set attrMapsSet = new HashSet();
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 // do stuff to map attr names.
                 attrNames = mapAttributeNames(attrNames, cMap);
                 Map aMap = null;
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     if (isString) {
                         aMap = idRepo.getAttributes(token, type, amsdkDN,
                                 attrNames);
                     } else {
                         aMap = idRepo.getBinaryAttributes(token, type, amsdkDN,
                                 attrNames);
                     }
                 } else {
                     if (isString) {
                         aMap = idRepo.getAttributes(token, type, name,
                                 attrNames);
                     } else {
                         aMap = idRepo.getBinaryAttributes(token, type, name,
                                 attrNames);
                     }
                 }
                 aMap = reverseMapAttributeNames(aMap, cMap);
                 attrMapsSet.add(aMap);
             } catch (IdRepoUnsupportedOpException ide) {
                 if (getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getAttributes: "
                         + "Unable to read identity in the following "
                         + "repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error("GetAttributes: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getAttributes: "
                         + "Unable to read identity in the following "
                         + "repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning("idServicesImpl.getAttributes: " +
                     "Unable to get attributes for identity " + type.getName() + 
                     ", " + name + " in any configured data store", origEx);
             }
             throw origEx;
         }
         
         return combineAttrMaps(attrMapsSet, isString);
     }
 
     /*
      * (non-Javadoc)
      */
     public Map getAttributes(SSOToken token, IdType type, String name,
             String amOrgName, String amsdkDN) throws IdRepoException,
             SSOException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.READ, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Set attrMapsSet = new HashSet();
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 Map aMap = null;
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     aMap = idRepo.getAttributes(token, type, amsdkDN);
                 } else {
                     aMap = idRepo.getAttributes(token, type, name);
                 }
                 aMap = reverseMapAttributeNames(aMap, cMap);
                 attrMapsSet.add(aMap);
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getAttributes: "
                         + "Unable to read identity in the following "
                         + "repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                         "IdServicesImpl.getAttributes: "
                         + "Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getAttributes: "
                         + "Unable to read identity in the following "
                         + "repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.getAttributes: "
                     + "Unable to get attributes for identity "
                     + type.getName() +
                     "::" + name + " in any configured data store", origEx);
             }
             throw origEx;
         } else {
             Map returnMap = combineAttrMaps(attrMapsSet, true);
             return returnMap;
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public Set getMembers(SSOToken token, IdType type, String name,
             String amOrgName, IdType membersType, String amsdkDN)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.READ, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Set membersSet = new HashSet();
         Set amsdkMembers = new HashSet();
         boolean amsdkIncluded = false;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             if (!idRepo.getSupportedTypes().contains(membersType)) {
                 // IdRepo plugin does not support the idType for
                 // memberships
                 continue;
             }
             try {
                 // Map cMap = idRepo.getConfiguration();
                 boolean isAMSDK = idRepo.getClass().getName().equals(
                         IdConstants.AMSDK_PLUGIN);
                 Set members = (isAMSDK && (amsdkDN != null)) ? idRepo
                         .getMembers(token, type, amsdkDN, membersType) : idRepo
                         .getMembers(token, type, name, membersType);
                 if (isAMSDK) {
                     amsdkMembers.addAll(members);
                     amsdkIncluded = true;
                 } else {
                     membersSet.add(members);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getMembers: "
                         + "Unable to read identity members in the following"
                         + " repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.getMembers: "
                     + "Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getMembers: "
                         + "Unable to read identity members in the following"
                         + " repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.getMembers: "
                     + "Unable to get members for identity " + type.getName()
                     + "::" + name + " in any configured data store", origEx);
             }
             throw origEx;
         } else {
             Set results = combineMembers(token, membersSet, membersType,
                     amOrgName, amsdkIncluded, amsdkMembers);
             return results;
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public Set getMemberships(SSOToken token, IdType type, String name,
             IdType membershipType, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.READ, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Set membershipsSet = new HashSet();
         Set amsdkMemberShips = new HashSet();
         boolean amsdkIncluded = false;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             // Skip SpecialRepo if the user does not belong to it.
             if (!isSpecialRepoUser(token, type, name, amOrgName, idRepo)) {
                 continue;
             }
 
             if (!idRepo.getSupportedTypes().contains(membershipType)) {
                 // IdRepo plugin does not support the idType for
                 // memberships
                 continue;
             }
             try {
                 // Map cMap = idRepo.getConfiguration();
                 boolean isAMSDK = idRepo.getClass().getName().equals(
                         IdConstants.AMSDK_PLUGIN);
                 Set members = (isAMSDK && (amsdkDN != null)) ? idRepo
                         .getMemberships(token, type, amsdkDN, membershipType)
                         : idRepo.getMemberships(token, type, name,
                                 membershipType);
                 if (isAMSDK) {
                     amsdkMemberShips.addAll(members);
                     amsdkIncluded = true;
                 } else {
                     membershipsSet.add(members);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getMemberships: "
                         + "Unable to get memberships in the following "
                         + "repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.getMemberships: "
                     + "Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getMemberships: "
                         + "Unable to read identity in the following "
                         + "repository " + idRepo.getClass().getName(), ide);
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.getMemberships: "
                     + "Unable to get members for identity " + type.getName()
                     + "::" + name + " in any configured data store", origEx);
             }
             throw origEx;
         } else {
             Set results = combineMembers(token, membershipsSet, membershipType,
                     amOrgName, amsdkIncluded, amsdkMemberShips);
             return results;
         }
     }
 
     private boolean isSpecialRepoUser(SSOToken token, IdType type, String name,
             String amOrgName, IdRepo idRepo) throws IdRepoException,
             SSOException {
         // This is to avoid checking the hidden Special Repo plugin
         // for non special users while getting memberships.
         boolean isSpecialUser = false;
         if (idRepo.getClass().getName().equals(IdConstants.SPECIAL_PLUGIN)) {
             if (type.equals(IdType.USER)) {
                 /*
                  * Iterating through to get out the special identities and
                  * compare with the name sent to get memberships. If name is not
                  * in the list of special identities go back. This is for the
                  * scenerio, when no datastore is configured, but need the
                  * special identity for authentication.
                  */
                 IdSearchResults results = getSpecialIdentities(token, type,
                         amOrgName);
 
                 Set identities = results.getSearchResults();
                 // iterating through to get the special identity names.
                 if ((identities != null) && (!identities.isEmpty())) {
                     for (Iterator i = identities.iterator(); !isSpecialUser
                             && i.hasNext();) {
                         String idName = ((AMIdentity) i.next()).getName();
                         if (name.equalsIgnoreCase(idName)) {
                             isSpecialUser = true;
                         }
                     }
                 }
             }
         } else {
             isSpecialUser = true;
         }
         return isSpecialUser;
     }
 
     /*
      * (non-Javadoc)
      */
     public boolean isExists(SSOToken token, IdType type, String name,
             String amOrgName) throws SSOException, IdRepoException {
 
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         IdRepo idRepo;
         boolean exists = false;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             if (!idRepo.getClass().getName().equals(IdConstants.SPECIAL_PLUGIN))
             {
                 exists = idRepo.isExists(token, type, name);
                 if (exists) {
                     break;
                 }
             }
         }
         return exists;
     }
 
     public boolean isActive(SSOToken token, IdType type, String name,
             String amOrgName, String amsdkDN) throws SSOException,
             IdRepoException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.READ, type);
         // First get the list of plugins that support the create operation.
 
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         boolean active = false;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     active = idRepo.isActive(token, type, amsdkDN);
                 } else {
                     active = idRepo.isActive(token, type, name);
                 }
                 if (active) {
                     break;
                 }
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.isActive: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.isActive: "
                         + "Unable to check isActive identity in the "
                         + "following repository "
                         + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
 
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.isActive: "
                     + "Unable to check if identity is active " + type.getName()
                     + "::" + name + " in any configured data store", origEx);
             }
             throw origEx;
         }
 
         return active;
     }
 
     /*
      * (non-Javadoc)
      */
     public void setActiveStatus(SSOToken token, IdType type, String name,
         String amOrgName, String amsdkDN, boolean active) throws SSOException,
         IdRepoException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.EDIT, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
             plugIns, IdOperation.EDIT, type);
         if (configuredPluginClasses == null
             || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 if (idRepo.getClass().getName()
                     .equals(IdConstants.AMSDK_PLUGIN)
                     && amsdkDN != null) {
                     idRepo.setActiveStatus(token, type, amsdkDN, active);
                 } else {
                     idRepo.setActiveStatus(token, type, name, active);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning("IdServicesImpl:setActiveStatus: "
                             + "Unable to set attributes in the following "
                             + "repository" + idRepo.getClass().getName()
                             + " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error("IsActive: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "Unable to setActiveStatus in the " +
                         "following repository" + idRepo.getClass().getName() +
                         " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
                 // 220 is entry not found. this error should have lower
                 // precedence than other error because we search thru all
                 // the ds and this entry might exist in one of the other ds.
                 if (!ide.getErrorCode().equalsIgnoreCase("220") ||
                     (origEx == null)) {
                     origEx = ide;
                 }
             }
         }
         if (noOfSuccess == 0) {
             getDebug().error("Unable to setActiveStatus for identity "
                     + type.getName() + "::" + name + " in any configured "
                     + "datastore", origEx);
             throw origEx;
         }
 
     }
 
     /*
      * (non-Javadoc)
      */
     public void modifyMemberShip(SSOToken token, IdType type, String name,
             Set members, IdType membersType, int operation, String amOrgName)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.EDIT, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.EDIT, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
 
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             if (!idRepo.getSupportedTypes().contains(membersType)) {
                 // IdRepo plugin does not support the idType for
                 // memberships
                 continue;
             }
             try {
                 idRepo.modifyMemberShip(token, type, name, members,
                         membersType, operation);
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.modifyMembership: "
                         + "Unable to modify memberships  in the following"
                         + " repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.modifyMembership: "
                     + "Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().error(
                         "IdServicesImpl.modifyMembership: "
                         + "Unable to modify memberships in the following"
                         + " repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.modifyMemberShip: "
                     + "Unable to modify members for identity " + type.getName()
                     + "::" + name + " in any configured data store", origEx);
             }
             throw origEx;
         }
     }
 
     /*
      * (non-Javadoc)
      */
     public void removeAttributes(SSOToken token, IdType type, String name,
             Set attrNames, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, attrNames, IdOperation.EDIT,
                 type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.EDIT, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 // do stuff to map attr names.
                 attrNames = mapAttributeNames(attrNames, cMap);
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     idRepo.removeAttributes(token, type, amsdkDN, attrNames);
                 } else {
                     idRepo.removeAttributes(token, type, name, attrNames);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.removeAttributes: "
                         + "Unable to modify identity in the following "
                         + "repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.removeAttributes: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.removeAttributes: "
                         + "Unable to remove attributes in the following "
                         + "repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 // 220 is entry not found. this error should have lower
                 // precedence than other errors because we search through
                 // all the ds and this entry might exist in one of the other ds.
                 if (!ide.getErrorCode().equalsIgnoreCase("220")
                         || (origEx == null)) {
                     origEx = ide;
                 }
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.removeAttributes: "
                     + "Unable to remove attributes  for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store", origEx);
             }
             throw origEx;
         }
     }
 
     public IdSearchResults search(SSOToken token, IdType type, String pattern,
             IdSearchControl ctrl, String amOrgName) throws IdRepoException,
             SSOException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, null, null, IdOperation.READ, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.READ, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         Object[][] amsdkResults = new Object[1][2];
         boolean amsdkIncluded = false;
         Object[][] arrayOfResult = new Object[noOfSuccess][2];
         // Set resultsSet = new HashSet();
         int iterNo = 0;
         int maxTime = ctrl.getTimeOut();
         int maxResults = ctrl.getMaxResults();
         Set returnAttrs = ctrl.getReturnAttributes();
         boolean returnAllAttrs = ctrl.isGetAllReturnAttributesEnabled();
         IdSearchOpModifier modifier = ctrl.getSearchModifier();
         int filterOp = IdRepo.NO_MOD;
         if (modifier.equals(IdSearchOpModifier.AND)) {
             filterOp = IdRepo.AND_MOD;
         } else if (modifier.equals(IdSearchOpModifier.OR)) {
             filterOp = IdRepo.OR_MOD;
         }
         Map avPairs = ctrl.getSearchModifierMap();
         boolean recursive = ctrl.isRecursive();
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 RepoSearchResults results = idRepo.search(token, type, pattern,
                         maxTime, maxResults, returnAttrs, returnAllAttrs,
                         filterOp, avPairs, recursive);
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)) {
                     amsdkResults[0][0] = results;
                     amsdkResults[0][1] = cMap;
                     amsdkIncluded = true;
                 } else {
                     arrayOfResult[iterNo][0] = results;
                     arrayOfResult[iterNo][1] = cMap;
                     iterNo++;
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.search: "
                         + "Unable to search in the following repository "
                         + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.search: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.search: "
                         + "Unable to search identity in the following"
                         + " repository " + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.search: "
                     + "Unable to search for identity " + type.getName()
                     + "::" + pattern
                     + " in any configured data store", origEx);
             }
             throw origEx;
         } else {
             IdSearchResults res = combineSearchResults(token, arrayOfResult,
                     iterNo, type, amOrgName, amsdkIncluded, amsdkResults);
             return res;
         }
     }
     
     protected IdRepo getSpecialRepoPlugin(SSOToken token, String orgName) 
         throws SSOException, IdRepoException 
     {
         
         IdRepo pluginClass = null;
         boolean pluginInitialized = false;
         synchronized (idRepoMap) {
             // Checking for the presence of the plugin & adding it to the
             // plugin cache should be in the critical section 
             // hence synchronized.
             Object o = idRepoMap.get(IdConstants.SPECIAL_PLUGIN);
             if (o instanceof IdRepo) {
                 pluginClass = (IdRepo) o;
             }
         
             if (pluginClass == null) {                    
                 try {                    
                     Class thisClass = Class.forName(IdConstants.SPECIAL_PLUGIN);
                     pluginClass = (IdRepo) thisClass.newInstance();
                     pluginClass.initialize(new HashMap());
                     idRepoMap.put(IdConstants.SPECIAL_PLUGIN, pluginClass);
                     pluginInitialized = true;
                 } catch (Exception e) {
                     getDebug().error("IdServicesImpl.getSpecialIdentities: "
                         + "Unable to instantiate plugin: " 
                         + IdConstants.SPECIAL_PLUGIN, e);
                 }
             }
         }
         
         if (pluginInitialized) {
             // Ideally, this add listener part should also be in the
             // synchronized block. Here it is not to avoid a deadlock that
             // can happen when the addListener is called on Special Repo
             // and it acquires a lock to ServiceSchemaManagerImpl. 
             Map listenerConfig = new HashMap();
             listenerConfig.put("realm", orgName);
             IdRepoListener lter = new IdRepoListener();
             lter.setConfigMap(listenerConfig);
             pluginClass.addListener(token, lter);                       
         }
         
         return pluginClass;
     }
     
     
     protected IdRepo getAMRepoPlugin(SSOToken token, String orgName) 
         throws SSOException, IdRepoException 
     {  
        
         String p = IdConstants.AMSDK_PLUGIN;
         String cacheKey = orgName + ":" + IdConstants.AMSDK_PLUGIN_NAME;
         IdRepo pluginClass = null;
         synchronized (idRepoMap) {
             Object o = idRepoMap.get(cacheKey);
             if (o instanceof IdRepo) {
                 pluginClass = (IdRepo) o;
             }
         
             if (pluginClass == null) {
                 Map amsdkConfig = new HashMap();
                 Set vals = new HashSet();
                 vals.add(DNMapper.realmNameToAMSDKName(orgName));
                 amsdkConfig.put("amSDKOrgName", vals);
                 try {
                     
                     Class thisClass = Class.forName(p);
                     pluginClass = (IdRepo) thisClass.newInstance();
                     pluginClass.initialize(amsdkConfig);
                     idRepoMap.put(cacheKey, pluginClass);                                    
 
                 } catch (Exception e) {
                     getDebug().error("IdServicesImpl.getConfiguredPlugins: "
                             + "Unable to instantiate plugin: " + p, e);
                 }
                 
                 if (pluginClass != null) { // Was initialized successfully                
                     // Add listener to this plugin class!
                     Map listenerConfig = new HashMap();
                     listenerConfig.put("realm", orgName);
                     listenerConfig.put("amsdk", "true");
                     IdRepoListener lter = new IdRepoListener();
                     lter.setConfigMap(listenerConfig);
                     pluginClass.addListener(token, lter);  
                 }
             }
         }
         
         return pluginClass;
     }
 
     public IdSearchResults getSpecialIdentities(SSOToken token, IdType type,
             String orgName) throws IdRepoException, SSOException {
         
         Set pluginClasses = new OrderedSet();
 
         if (ServiceManager.isConfigMigratedTo70()
                 && ServiceManager.getBaseDN().equalsIgnoreCase(orgName)) {
             // add the "SpecialUser plugin
             IdRepo specialRepo = getSpecialRepoPlugin(token, orgName);
             
             Set opSet = specialRepo.getSupportedOperations(type);
             if (opSet != null && opSet.contains(IdOperation.READ)) {
                 pluginClasses.add(specialRepo);
             }                
         }
         
         if (pluginClasses.isEmpty()) {
             return new IdSearchResults(type, orgName);
         } else {
             IdRepo specialRepo = (IdRepo) pluginClasses.iterator().next();
             RepoSearchResults res = specialRepo.search(token, type, "*", 0, 0,
                     Collections.EMPTY_SET, false, 0, Collections.EMPTY_MAP,
                     false);
             Object obj[][] = new Object[1][2];
             obj[0][0] = res;
             obj[0][1] = Collections.EMPTY_MAP;
             return combineSearchResults(token, obj, 1, type, orgName, false,
                     null);
 
         }
     }
 
     public void setAttributes(SSOToken token, IdType type, String name,
             Map attributes, boolean isAdd, String amOrgName, String amsdkDN,
             boolean isString) throws IdRepoException, SSOException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, attributes.keySet(),
                 IdOperation.EDIT, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         if (attributes.containsKey("objectclass")) {
             configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                     plugIns, IdOperation.SERVICE, type);
         } else {
             configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                     plugIns, IdOperation.EDIT, type);
         }
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo;
         while (it.hasNext()) {
             idRepo = (IdRepo) it.next();
             try {
                 Map cMap = idRepo.getConfiguration();
                 // do stuff to map attr names.
                 attributes = mapAttributeNames(attributes, cMap);
                 if (idRepo.getClass().getName()
                         .equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     if (isString) {
                         idRepo.setAttributes(token, type, amsdkDN, attributes,
                                 isAdd);
                     } else {
                         idRepo.setBinaryAttributes(token, type, amsdkDN,
                                 attributes, isAdd);
                     }
                 } else {
                     if (isString) {
                         idRepo.setAttributes(token, type, name, attributes,
                                 isAdd);
                     } else {
                         idRepo.setBinaryAttributes(token, type, name,
                                 attributes, isAdd);
                     }
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning("IdServicesImpl.setAttributes: "
                             + "Unable to set attributes in the following "
                             + "repository "
                             + idRepo.getClass().getName()
                             + " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.setAttributes: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.setAttributes: "
                         + "Unable to modify identity in the "
                         + "following repository "
                         + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 // 220 is entry not found. this error should have lower
                 // precedence than other error because we search thru
                 // all the ds and this entry might exist in one of the other ds.
                 if (!ide.getErrorCode().equalsIgnoreCase("220")
                         || (origEx == null)) {
                     origEx = ide;
                 }
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.setAttributes: "
                     + "Unable to set attributes  for identity "
                     + type.getName() + "::" + name + " in any configured data"
                     + " store", origEx);
             }
             throw origEx;
 
         }
 
     }
 
     public Set getAssignedServices(SSOToken token, IdType type, String name,
             Map mapOfServiceNamesAndOCs, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
 
         // First get the list of plugins that support the create operation.
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.READ, type);
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.SERVICE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             if (ServiceManager.getBaseDN().equalsIgnoreCase(amOrgName)
                     && (type.equals(IdType.REALM))) {
                 return (configuredPluginClasses);
             } else {
                 throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", 
                         null);
             }
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo = null;
         Set resultsSet = new HashSet();
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             try {
 
                 Set services = null;
                 if (repo.getClass().getName().equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     services = repo.getAssignedServices(token, type, amsdkDN,
                             mapOfServiceNamesAndOCs);
                 } else {
                     services = repo.getAssignedServices(token, type, name,
                             mapOfServiceNamesAndOCs);
                 }
                 if (services != null && !services.isEmpty()) {
                     resultsSet.addAll(services);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().error(
                         "IdServicesImpl.getAssignedServices: "
                         + "Services not supported for repository "
                         + repo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.getAssignedServices: Fatal Exception ",
                     idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getAssignedServices: "
                         + "Unable to get services for identity "
                         + "in the following repository "
                         + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.getAssignedServices: "
                     + "Unable to get assigned services for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store", origEx);
             }
             throw origEx;
         } else {
             return resultsSet;
         }
 
     }
 
     public void assignService(SSOToken token, IdType type, String name,
             String serviceName, SchemaType stype, Map attrMap,
             String amOrgName, String amsdkDN) throws IdRepoException,
             SSOException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null,
             IdOperation.SERVICE, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.SERVICE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo = null;
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Map cMap = repo.getConfiguration();
             try {
                 attrMap = mapAttributeNames(attrMap, cMap);
                 if (repo.getClass().getName().equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     repo.assignService(token, type, amsdkDN, serviceName,
                             stype, attrMap);
                 } else {
                     repo.assignService(token, type, name, serviceName, stype,
                             attrMap);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().error("IdServicesImpl.assignService: "
                             + "Assign Services not supported for repository "
                             + repo.getClass().getName()
                             + " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.assignService: FatalException ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().error(
                         "IdServicesImpl.assignService: "
                         + "Unable to assign Service identity in "
                         + "the following repository "
                         + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }   
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.assignService: "
                     + "Unable to assign service for identity " 
                     + type.getName()
                     + "::" + name + " in any configured data store ", origEx);
             }
             throw origEx;
         }
     }
 
     public void unassignService(SSOToken token, IdType type, String name,
             String serviceName, Map attrMap, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
         IdRepoException origEx = null;
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, null, IdOperation.SERVICE,
                 type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.SERVICE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo = null;
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Map cMap = repo.getConfiguration();
             try {
                 attrMap = mapAttributeNames(attrMap, cMap);
                 if (repo.getClass().getName().equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     repo.unassignService(token, type, amsdkDN, serviceName,
                             attrMap);
                 } else {
                     repo.unassignService(token, type, name, serviceName,
                             attrMap);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().error(
                         "IdServicesImpl.unassignService: "
                         + "Unassign Service not supported for repository "
                         + repo.getClass().getName()
                         + " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                    "IdServicesImpl.unassignService: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.unassignService: "
                         + "Unable to unassign service in the "
                         + "following repository "
                         + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.unassignService: "
                     + "Unable to unassign Service for identity "
                     + type.getName() + "::" + name + " in any configured "
                     + "data store ", origEx);
             }
             throw origEx;
         }
 
     }
 
     /**
      * Non-javadoc, non-public methods
      * Get the service attributes of the name identity. Traverse to the global
      * configuration if necessary until all attributes are found or reached
      * the global area whichever occurs first.
      *
      * @param token is the sso token of the person performing this operation.
      * @param type is the identity type of the name parameter.
      * @param name is the identity we are interested in.
      * @param serviceName is the service we are interested in
      * @param attrNames are the name of the attributes wer are interested in.
      * @param amOrgName is the orgname.
      * @param amsdkDN is the amsdkDN.
      * @throws IdRepoException if there are repository related error conditions.
      * @throws SSOException if user's single sign on token is invalid.
      */
     public Map getServiceAttributesAscending(SSOToken token, IdType type,
             String name, String serviceName, Set attrNames, String amOrgName,
             String amsdkDN) throws IdRepoException, SSOException {
 
         Map finalResult = new HashMap();
         Set finalAttrName = new HashSet();
         String nextName = name;
         String nextAmOrgName = amOrgName;
         String nextAmsdkDN = amsdkDN;
         IdType nextType = type;
         Set missingAttr = new HashSet(attrNames);
         do {
             // name is the name of AMIdentity object. will change as we move
             // up the tree.
             // attrNames is missingAttr and will change as we move up the tree.
             // amOrgname will change as we move up the tree.
             // amsdkDN will change as we move up the tree.
             try {
                 Map serviceResult = getServiceAttributes(token, nextType,
                     nextName, serviceName, missingAttr, nextAmOrgName,
                     nextAmsdkDN);
                 if (getDebug().messageEnabled()) {
                     getDebug().message("IdServicesImpl."
                         + "getServiceAttributesAscending:"
                         + " nextType=" + nextType + "; nextName=" + nextName
                         + "; serviceName=" + serviceName + "; missingAttr="
                         + missingAttr + "; nextAmOrgName=" + nextAmOrgName
                         + "; nextAmsdkDN=" + nextAmsdkDN);
                     getDebug().message("  getServiceAttributesAscending: "
                         + "serviceResult=" + serviceResult);
                     getDebug().message("  getServiceAttributesAscending: "
                         + " finalResult=" + finalResult);
                     getDebug().message("  getServiceAttributesAscending: "
                         + " finalAttrName=" + finalAttrName);
                 }
                 if (serviceResult != null) {
                     Set srvNameReturned = serviceResult.keySet();
                     // save the newly found attrs
                     // amsdk returns emptyset when attrname is not present.
                     Iterator nameIt = srvNameReturned.iterator();
                     while (nameIt.hasNext()) {
                         String attr = (String) nameIt.next();
                         Set attrValue = (Set) serviceResult.get(attr);
                         if (!attrValue.isEmpty()) {
                             finalResult.put(attr, attrValue);
                             finalAttrName.add(attr);
                         }
                     }
                     if (getDebug().messageEnabled()) {
                         getDebug().message("    getServiceAttributesAscending:"
                            + " serviceResult=" + serviceResult);
                         getDebug().message("    getServiceAttributesAscending:"
                           + " finalResult=" + finalResult);
                     }
                 }
                 if (finalAttrName.containsAll(attrNames)) {
                     if (getDebug().messageEnabled()) {
                         getDebug().message("exit getServiceAttributesAscending:"
                             + " finalResult=" + finalResult);
                     }
                     return(finalResult);
                 }
 
                 // find the missing attributes
                 missingAttr.clear();
                 Iterator it = attrNames.iterator();
                 while (it.hasNext()) {
                     String attrName = (String) it.next();
                     if (!finalAttrName.contains(attrName)) {
                         missingAttr.add(attrName);
                     }
                 }
             } catch (IdRepoException idrepo) {
                 if (getDebug().warningEnabled()) {
                     getDebug().warning("  getServiceAttributesAscending: "
                         + "idrepoerr", idrepo);
                 }
             } catch (SSOException ssoex) {
                 if (getDebug().warningEnabled()) {
                     getDebug().warning("  getServiceAttributesAscending: "
                         + "ssoex", ssoex);
                 }
             }
 
             //  go up to the parent org
             try {
 
                 if (nextType.equals(IdType.USER) ||
                     nextType.equals(IdType.AGENT)) {
                     // try the user or agent's currect realm.
                     nextAmsdkDN = nextAmOrgName;
                     nextType = IdType.REALM;
                 } else {
                     OrganizationConfigManager ocm =
                         new OrganizationConfigManager(token, nextAmOrgName);
                     OrganizationConfigManager parentOCM =
                         ocm.getParentOrgConfigManager();
                     String tmpParentName = parentOCM.getOrganizationName();
                     String parentName = DNMapper.realmNameToAMSDKName(
                         tmpParentName);
                     if (getDebug().messageEnabled()) {
                         getDebug().message("  getServiceAttributesAscending: "
                             + " tmpParentName=" + tmpParentName
                             + " parentName=" + parentName);
                     }
                     nextType = IdType.REALM;
                     if (nextAmOrgName.equalsIgnoreCase(parentName)) {
                         // at root.
                         nextName = null;
                     } else {
                         nextAmOrgName = parentName;
                     }
                     nextAmOrgName = parentName;
                     nextAmsdkDN = parentName;
                 }
             } catch (SMSException smse) {
                 if (getDebug().warningEnabled()) {
                     getDebug().warning("  getServiceAttributesAscending: "
                         + "smserror", smse);
                 }
                 nextName = null;
             }
         } while (nextName != null);
 
         // get the rest from global.
         if (!missingAttr.isEmpty()) {
             try {
                 ServiceSchemaManager ssm =
                     new ServiceSchemaManager(serviceName, token);
                 ServiceSchema schema = ssm.getDynamicSchema();
                 Map gAttrs = schema.getAttributeDefaults();
                 Iterator missingIt = missingAttr.iterator();
                 while (missingIt.hasNext()) {
                     String missingAttrName = (String) missingIt.next();
                     finalResult.put(missingAttrName,
                         gAttrs.get(missingAttrName));
                 }
             } catch (SMSException smse) {
                 if (getDebug().messageEnabled()) {
                     getDebug().message(
                         "IdServicesImpl(): getServiceAttributeAscending "
                         + " Failed to get global default.", smse);
                 }
             }
         }
 
         if (getDebug().messageEnabled()) {
             getDebug().message("exit end  getServiceAttributesAscending: "
                 + " finalResult=" + finalResult);
         }
         return finalResult;
     }
 
 
     public Map getServiceAttributes(SSOToken token, IdType type, String name,
             String serviceName, Set attrNames, String amOrgName, String amsdkDN)
             throws IdRepoException, SSOException {
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, attrNames, IdOperation.READ,
                 type);
 
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.SERVICE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo = null;
         Set resultsSet = new HashSet();
         IdRepoException origEx = null;
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Map cMap = repo.getConfiguration();
             try {
                 Map attrs = null;
                 if (repo.getClass().getName().equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     attrs = repo.getServiceAttributes(token, type, amsdkDN,
                             serviceName, attrNames);
                 } else {
                     attrs = repo.getServiceAttributes(token, type, name,
                             serviceName, attrNames);
                 }
                 attrs = reverseMapAttributeNames(attrs, cMap);
                 resultsSet.add(attrs);
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getServiceAttributes: "
                         + "Services not supported for repository "
                         + repo.getClass().getName()
                         + " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.getServiceAttributes: Fatal Exception ",
                     idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.getServiceAttributes: "
                         + "Unable to get service "
                         + "attributes for the repository "
                         + idRepo.getClass().getName()
                         + " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
                 origEx = ide;
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.getServiceAttributes: "
                     + "Unable to get service attributes for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store", origEx);
             }
             throw origEx;
         } else {
             Map resultsMap = combineAttrMaps(resultsSet, true);
             return resultsMap;
         }
 
     }
 
     public void modifyService(SSOToken token, IdType type, String name,
             String serviceName, SchemaType stype, Map attrMap,
             String amOrgName, String amsdkDN) throws IdRepoException,
             SSOException {
 
         // Check permission first. If allowed then proceed, else the
         // checkPermission method throws an "402" exception.
         checkPermission(token, amOrgName, name, attrMap.keySet(),
                 IdOperation.SERVICE, type);
         // First get the list of plugins that support the create operation.
         Set plugIns = getIdRepoPlugins(token, amOrgName);
         Set configuredPluginClasses = new HashSet();
         configuredPluginClasses = getConfiguredPlugins(token, amOrgName,
                 plugIns, IdOperation.SERVICE, type);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         int noOfSuccess = configuredPluginClasses.size();
         IdRepo idRepo = null;
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Map cMap = repo.getConfiguration();
             try {
                 attrMap = mapAttributeNames(attrMap, cMap);
                 if (repo.getClass().getName().equals(IdConstants.AMSDK_PLUGIN)
                         && amsdkDN != null) {
                     repo.modifyService(token, type, amsdkDN, serviceName,
                             stype, attrMap);
                 } else {
                     repo.modifyService(token, type, name, serviceName, stype,
                             attrMap);
                 }
             } catch (IdRepoUnsupportedOpException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning("IdServicesImpl.modifyService: "
                             + "Modify Services not supported for repository "
                             + repo.getClass().getName()
                             + " :: " + ide.getMessage());
                 }
                 noOfSuccess--;
             } catch (IdRepoFatalException idf) {
                 // fatal ..throw it all the way up
                 getDebug().error(
                     "IdServicesImpl.modifyService: Fatal Exception ", idf);
                 throw idf;
             } catch (IdRepoException ide) {
                 if (idRepo != null && getDebug().warningEnabled()) {
                     getDebug().warning(
                         "IdServicesImpl.modifyService: "
                         + "Unable to modify service in the "
                         + "following repository "
                         + idRepo.getClass().getName() + " :: "
                         + ide.getMessage());
                 }   
                 noOfSuccess--;
             }
         }
         if (noOfSuccess == 0) {
             if (getDebug().warningEnabled()) {
                 getDebug().warning(
                     "IdServicesImpl.modifyService: "
                     + "Unable to modify service attributes for identity "
                     + type.getName() + "::" + name
                     + " in any configured data store");
             }
             Object[] args = { IdOperation.SERVICE.toString() };
             throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME,
                     "302", args);
         }
     }
 
     public Set getSupportedTypes(SSOToken token, String amOrgName)
             throws IdRepoException, SSOException {
 
         // First get the list of plugins that support the create operation.
         Set unionSupportedTypes = new HashSet();
         if (!orgExist(token, amOrgName)) {
             debug.error("IdServicesImpl.getSupportedTypes: "
                 + "Realm " + amOrgName + " does not exist.");
             Object[] args = { amOrgName };
             throw new IdRepoUnsupportedOpException(IdRepoBundle.BUNDLE_NAME,
                     "312", args);
         }
         SSOToken stoken = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
         Set plugIns = getIdRepoPlugins(stoken, amOrgName);
         Set configuredPluginClasses = 
             getAllConfiguredPlugins(stoken, amOrgName, plugIns);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Set supportedTypes = repo.getSupportedTypes();
             if (supportedTypes != null && !supportedTypes.isEmpty()) {
                 unionSupportedTypes.addAll(supportedTypes);
             }
         }
         // Check if the supportedTypes is defined as supported in
         // the global schema.
         unionSupportedTypes.retainAll(IdUtils.supportedTypes);
         return unionSupportedTypes;
     }
 
     public Set getSupportedOperations(SSOToken token, IdType type,
             String amOrgName) throws IdRepoException, SSOException {
 
         // First get the list of plugins that support the create operation.
         Set unionSupportedOps = new HashSet();
         SSOToken stoken = (SSOToken) AccessController.doPrivileged(
             AdminTokenAction.getInstance());
         Set plugIns = getIdRepoPlugins(stoken, amOrgName);
         Set configuredPluginClasses = 
             getAllConfiguredPlugins(stoken, amOrgName, plugIns);
         if (configuredPluginClasses == null
                 || configuredPluginClasses.isEmpty()) {
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "301", null);
         }
 
         Iterator it = configuredPluginClasses.iterator();
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             Set supportedOps = repo.getSupportedOperations(type);
             if (supportedOps != null && !supportedOps.isEmpty()) {
                 unionSupportedOps.addAll(supportedOps);
             }
         }
         return unionSupportedOps;
     }
 
     public void clearIdRepoPlugins() {
         getDebug().message("IdServicesImpl.cleanupIdRepoPlugins(): "
                 + "Cleanup IdRepo Plugins is called...\n. Cleaning up the map.."
                 + idRepoMap);
         Set localSet = new HashSet();
         synchronized (idRepoMap) {
             Set keys = idRepoMap.keySet();
             Iterator it = keys.iterator();
             while (it.hasNext()) {
                 String cachekey = (String) it.next();
                 Object o = idRepoMap.get(cachekey);
                 if (o instanceof IdRepo) {
                     localSet.add(idRepoMap.get(cachekey));
                 } else {
                     Map rMap = (Map) o;
                     localSet.addAll(rMap.values());
                 }
             }
             idRepoMap.clear();
         }
 
         // FIXME: Shutdown should not happen here !!!
         // TODO:
         Iterator it = localSet.iterator();
         while (it.hasNext()) {
             IdRepo repo = (IdRepo) it.next();
             repo.removeListener();
             repo.shutdown();
         }
     }
 
     private Set getIdRepoPlugins(SSOToken token, String orgName) {
         if (idRepoPlugins == null) {
             return (Collections.EMPTY_SET);
         }
         if (ServiceManager.isCoexistenceMode()
                 && !idRepoPlugins.contains("amSDK")) {
             idRepoPlugins.add("amSDK");
         }
         return idRepoPlugins;
     }
 
     public void reloadIdRepoServiceSchema() {
         // Re-Load !
         idRepoPlugins = idRepoSubSchema.getSubSchemaNames();
     }
        
     
     private Set getInitializedPlugins(SSOToken token, String orgName, 
             Set pluginNames) {
         
         Set pluginClasses = new OrderedSet();
         // Check in cache if plugins are already instantiated.
         Iterator it = pluginNames.iterator();
         while (it.hasNext()) {
             String p = (String) it.next();
                         
             // Check if it is Legacy mode & if the plugin is AM SDK. If so skip
             // it because, AM SDK plugin is always added by default in Legacy
             // mode
             if (ServiceManager.isCoexistenceMode()
                     && p.equals(IdConstants.AMSDK_PLUGIN_NAME)) {
                 continue;
             }
                         
             String cacheKey = orgName + ":" + p;
             IdRepo pClass = null;
             Map classMap = null;
             synchronized (idRepoMap) {
                 Object obj = idRepoMap.get(cacheKey);
                 if (obj instanceof Map) {
                     // set it to class map only if this is an instance of
                     // Map. It could possibly be an IdRepo impl class in some
                     // cases.
                     classMap = (Map) obj;
                 }
 
                 if (classMap != null) {
                     // This plugin is in cache. Check for
                     // support of operation and type and add
                     // to list of plugins to be invoked.
                     Iterator it2 = classMap.keySet().iterator();
                     while (it2.hasNext()) {
                         String pName = (String) it2.next();
                         pClass = (IdRepo) classMap.get(pName);
                         pluginClasses.add(pClass);
                     }
                 } else {
                     // Not in cache. Invoke and initialize this class.
                     Set pNames = getConfiguredPluginNames(orgName, p);
                     if (pNames == null) {
                         // Update the cache with empty HashMap
                         idRepoMap.put(cacheKey, Collections.EMPTY_MAP);
                         continue; // go to start of while
                     }
                     Iterator it2 = pNames.iterator();
                     while (it2.hasNext()) {
                         String pn = (String) it2.next();
                         Map configMap = getConfigMap(orgName, p, pn);
                         if (configMap != null && !configMap.isEmpty()) {
                             Set vals = (Set) configMap.get(IdConstants.ID_REPO);
                             if (vals != null) {
                                 String className = (String) vals.iterator()
                                         .next();
                                 try {
 
                                     Class thisClass = Class.forName(className);
                                     IdRepo thisPlugin = (IdRepo) thisClass
                                             .newInstance();
                                     thisPlugin.initialize(configMap);
                                     // Add listener to this plugin class!
                                     Map listenerConfig = new HashMap();
                                     listenerConfig.put("realm", orgName);
                                     if (className.equals(
                                             IdConstants.AMSDK_PLUGIN)) {
                                         listenerConfig.put("amsdk", "true");
                                     }
                                     listenerConfig.put("plugin-name", pn);
                                     IdRepoListener lter = new IdRepoListener();
                                     lter.setConfigMap(listenerConfig);
                                     thisPlugin.addListener(token, lter);
 
                                     Map tmpMap = (Map) idRepoMap.get(cacheKey);
                                     if (tmpMap == null) {
                                         tmpMap = new HashMap();
                                     }
                                     tmpMap.put(pn, thisPlugin);
                                     idRepoMap.put(cacheKey, tmpMap);
 
                                     pluginClasses.add(thisPlugin);
 
                                 } catch (Exception e) {
                                     getDebug().error("IdServicesImpl."
                                             + "getConfiguredPlugins: Unable to" 
                                             + " instantiate plugin: " 
                                             + className, e);
                                 }
                             }
                         }
                     } // end inner while it.hasNext()
                 } // end else
             } // end while
         }
         
         return pluginClasses;
     }
 
     private Set getConfiguredPlugins(SSOToken token, String orgName,
             Set pluginNames, IdOperation op, IdType type) 
         throws SSOException, IdRepoException 
     {
         // Check and load what is in the cache.
         Set pluginClasses = new OrderedSet();
         if (ServiceManager.isConfigMigratedTo70()
                 && ServiceManager.getBaseDN().equalsIgnoreCase(orgName)) {
             // add the "SpecialUser plugin
             IdRepo specialRepo = getSpecialRepoPlugin(token, orgName);
             
             Set opSet = specialRepo.getSupportedOperations(type);
             if (opSet != null && opSet.contains(op)) {
                 pluginClasses.add(specialRepo);
             }
         }
 
         if (ServiceManager.isCoexistenceMode()) { 
             // Legacy Mode. Add AM Repo plugin by default.
 
             IdRepo amRepo = getAMRepoPlugin(token, orgName);
             
             Set opSet = amRepo.getSupportedOperations(type);
             if (opSet != null && opSet.contains(op)) {
                     pluginClasses.add(amRepo);
             }
             
         }
 
         // Get rest of the plugins configured at the realm
         Set otherPlugins = (OrderedSet) getInitializedPlugins(token, orgName, 
                 pluginNames);
         
         // Add only the plugins which support the given operation
         Iterator iter = otherPlugins.iterator();
         while (iter.hasNext()) {
             IdRepo idRepoPlugin = (IdRepo) iter.next();
             Set opSet = idRepoPlugin.getSupportedOperations(type);
             if (opSet != null && opSet.contains(op)) {
                 pluginClasses.add(idRepoPlugin);
             }
         }
         
         return pluginClasses;
     }// end getConfiguredPlugins
 
     private Set getAllConfiguredPlugins(SSOToken token, String orgName,
             Set pluginNames) throws SSOException, IdRepoException {
         
         // Check and load what is in the cache.
         Set pluginClasses = new OrderedSet();
         if (!orgExist(token, orgName)) {
             if (debug.messageEnabled()) {
                 debug.message("IdServicesImpl.getAllConfiguredPlugins: "
                         + " organization does not exist.");
             }
             return pluginClasses;
         }
         
         if (ServiceManager.isConfigMigratedTo70()
                 && ServiceManager.getBaseDN().equalsIgnoreCase(orgName)) {
             // add the "SpecialUser plugin            
             IdRepo specialRepo = getSpecialRepoPlugin(token, orgName);            
             pluginClasses.add(specialRepo);            
         }
 
         if (ServiceManager.isCoexistenceMode()) {
             
             IdRepo amRepo = getAMRepoPlugin(token, orgName);            
             pluginClasses.add(amRepo);                
         }
         
         // Get rest of the plugins configured at the realm
         pluginClasses.addAll(getInitializedPlugins(token, orgName, 
                 pluginNames));
 
         return pluginClasses;
     }// end getAllConfiguredPlugins
 
     private Set getConfiguredPluginNames(String orgName, String schemaName) {
         Set cPlugins = new HashSet();
         SSOToken token = (SSOToken) AccessController
                 .doPrivileged(AdminTokenAction.getInstance());
         try {
             ServiceConfigManager scm = new ServiceConfigManager(token,
                     IdConstants.REPO_SERVICE, "1.0");
             if (scm == null) {
                 // Plugin not configured for this organization
                 // return an empty map.
                 return cPlugins;
             }
             ServiceConfig sc = scm.getOrganizationConfig(orgName, null);
             if (sc == null) {
                 // plugin not configured. Return empty Map
                 return cPlugins;
             }
 
             ServiceConfig subConfig = null;
             Set plugins = sc.getSubConfigNames();
             if (plugins != null && !plugins.isEmpty()) {
                 Iterator items = plugins.iterator();
                 while (items.hasNext()) {
                     String pName = (String) items.next();
                     subConfig = sc.getSubConfig(pName);
                     if (subConfig != null
                             && subConfig.getSchemaID().equalsIgnoreCase(
                                     schemaName)) {
                         cPlugins.add(pName);
                     }
                 }
             }
             return cPlugins;
         } catch (SMSException smse) {
             // the if condition below is added to avoid writing this message
             // in debug logs at install time. Durng installation, due
             // a call from amAuth.xml, idrepo is invoked before idrepo service
             // is loaded. The condition below returns false and hence
             // we avoid writing to debug logs at install time
             if (ServiceManager.isConfigMigratedTo70()) {
                 getDebug().error(
                     "IdServicesImpl.getConfiguredPluginNames: "
                     + "SM Exception: unable to get plugin information", smse);
             }
             return cPlugins;
         } catch (SSOException ssoe) {
             getDebug().error(
                 "IdServicesImpl.getConfiguredPluginNames: "
                 + "SSO Exception: ", ssoe);
             return cPlugins;
         }
 
     }
 
     private Map getConfigMap(String orgName, String pluginName,
             String configName) {
         SSOToken token = (SSOToken) AccessController
                 .doPrivileged(AdminTokenAction.getInstance());
         if (ServiceManager.isCoexistenceMode() && pluginName.equals("amSDK")) {
             Map amsdkConfig = new HashMap();
             Set vals = new HashSet();
             vals.add(DNMapper.realmNameToAMSDKName(orgName));
             amsdkConfig.put("amSDKOrgName", vals);
             return amsdkConfig;
         }
         Map configMap = Collections.EMPTY_MAP;
         try {
             ServiceConfigManager scm = new ServiceConfigManager(token,
                     IdConstants.REPO_SERVICE, "1.0");
             if (scm == null) {
                 // Plugin not configured for this organization
                 // return an empty map.
                 return configMap;
             }
             ServiceConfig sc = scm.getOrganizationConfig(orgName, null);
             if (sc == null) {
                 // plugin not configured. Return empty Map
                 return configMap;
             }
 
             ServiceConfig subConfig = sc.getSubConfig(configName);
             if (subConfig != null
                     && subConfig.getSchemaID().equalsIgnoreCase(pluginName)) {
                 configMap = subConfig.getAttributes();
             }
             /*
              * Set plugins = sc.getSubConfigNames(); if (plugins != null &&
              * !plugins.isEmpty()) { Iterator items = plugins.iterator(); while
              * (items.hasNext()) { String pName = (String) items.next();
              * subConfig = sc.getSubConfig(pName); if
              * (subConfig.getSchemaID().equalsIgnoreCase(pluginName)) {
              * configMap = subConfig.getAttributes(); break; } } }
              */
 
             return configMap;
         } catch (SMSException smse) {
             getDebug().error(
                 "IdServicesImpl.getConfigMap: "
                 + "SM Exception: unable to get plugin information",
                 smse);
             return configMap;
         } catch (SSOException ssoe) {
             getDebug().error(
                 "IdServicesImpl.getConfigMap: "
                 + "SSO Exception: ", ssoe);
             return configMap;
         }
 
     }
 
     private Map combineAttrMaps(Set setOfMaps, boolean isString) {
         Map resultMap = new AMHashMap(!isString);
         Iterator it = setOfMaps.iterator();
         while (it.hasNext()) {
             Map currMap = (Map) it.next();
             if (currMap != null) {
                 Iterator keyset = currMap.keySet().iterator();
                 while (keyset.hasNext()) {
                     String thisAttr = (String) keyset.next();
                     if (isString) {
                         Set resultSet = (Set) resultMap.get(thisAttr);
                         Set thisSet = (Set) currMap.get(thisAttr);
                         if (resultSet != null) {
                             resultSet.addAll(thisSet);
                         } else {
                             /*
                              * create a new Set so that we do not alter the set
                              * that is referenced in setOfMaps
                              */
                             resultSet = new HashSet((Set) 
                                     currMap.get(thisAttr));
                             resultMap.put(thisAttr, resultSet);
                         }
                     } else { // binary attributes
 
                         byte[][] resultSet = (byte[][]) resultMap.get(thisAttr);
                         byte[][] thisSet = (byte[][]) currMap.get(thisAttr);
                         int combinedSize = thisSet.length;
                         if (resultSet != null) {
                             combinedSize = resultSet.length + thisSet.length;
                             byte[][] tmpSet = new byte[combinedSize][];
                             for (int i = 0; i < resultSet.length; i++) {
                                 tmpSet[i] = (byte[]) resultSet[i];
                             }
                             for (int i = 0; i < thisSet.length; i++) {
                                 tmpSet[i] = (byte[]) thisSet[i];
                             }
                             resultSet = tmpSet;
                         } else {
                             resultSet = (byte[][]) thisSet.clone();
                         }
                         resultMap.put(thisAttr, resultSet);
 
                     }
 
                 }
             }
         }
         return resultMap;
     }
 
     private Map mapAttributeNames(Map attrMap, Map configMap) {
         if (attrMap == null || attrMap.isEmpty()) {
             return attrMap;
         }
         Map resultMap;
         Map[] mapArray = getAttributeNameMap(configMap);
         if (mapArray == null) {
             resultMap = attrMap;
         } else {
             resultMap = new CaseInsensitiveHashMap();
             Map forwardMap = mapArray[0];
             Iterator it = attrMap.keySet().iterator();
             while (it.hasNext()) {
                 String curr = (String) it.next();
                 if (forwardMap.containsKey(curr)) {
                     resultMap.put((String) forwardMap.get(curr), (Set) attrMap
                             .get(curr));
                 } else {
                     resultMap.put(curr, (Set) attrMap.get(curr));
                 }
             }
         }
         return resultMap;
     }
 
     private Set mapAttributeNames(Set attrNames, Map configMap) {
         if (attrNames == null || attrNames.isEmpty()) {
             return attrNames;
         }
         Map[] mapArray = getAttributeNameMap(configMap);
         Set resultSet;
         if (mapArray == null) {
             resultSet = attrNames;
         } else {
             resultSet = new CaseInsensitiveHashSet();
             Map forwardMap = mapArray[0];
             Iterator it = attrNames.iterator();
             while (it.hasNext()) {
                 String curr = (String) it.next();
                 if (forwardMap.containsKey(curr)) {
                     resultSet.add((String) forwardMap.get(curr));
                 } else {
                     resultSet.add(curr);
                 }
             }
         }
         return resultSet;
     }
 
     private Map reverseMapAttributeNames(Map attrMap, Map configMap) {
         if (attrMap == null || attrMap.isEmpty()) {
             return attrMap;
         }
         Map resultMap;
         Map[] mapArray = getAttributeNameMap(configMap);
         if (mapArray == null) {
             resultMap = attrMap;
         } else {
             resultMap = new CaseInsensitiveHashMap();
             Map reverseMap = mapArray[1];
             Iterator it = attrMap.keySet().iterator();
             while (it.hasNext()) {
                 String curr = (String) it.next();
                 if (reverseMap.containsKey(curr)) {
                    resultMap.put((String) reverseMap.get(curr), attrMap
                             .get(curr));
                 } else {
                    resultMap.put(curr, attrMap.get(curr));
                 }
             }
         }
         return resultMap;
     }
 
     private Set combineMembers(SSOToken token, Set membersSet, IdType type,
             String orgName, boolean amsdkIncluded, Set amsdkMemberships) {
         Set results = new HashSet();
         Map resultsMap = new CaseInsensitiveHashMap();
         if (amsdkIncluded) {
             if (amsdkMemberships != null) {
                 Iterator it = amsdkMemberships.iterator();
                 while (it.hasNext()) {
                     String m = (String) it.next();
                     String mname = DNUtils.DNtoName(m);
                     AMIdentity id = new AMIdentity(token, mname, type, orgName,
                             m);
                     results.add(id);
                     resultsMap.put(mname, id);
                 }
             }
         }
         Iterator miter = membersSet.iterator();
         while (miter.hasNext()) {
             Set first = (Set) miter.next();
             if (first == null) {
                 continue;
             }
             Iterator it = first.iterator();
             while (it.hasNext()) {
                 String m = (String) it.next();
                 String mname = DNUtils.DNtoName(m);
                 // add to results, if not already there!
                 if (!resultsMap.containsKey(mname)) {
                     AMIdentity id = new AMIdentity(token, mname, type, orgName,
                             null);
                     results.add(id);
                     resultsMap.put(mname, id);
                 }
             }
         }
         return results;
     }
 
     private IdSearchResults combineSearchResults(SSOToken token,
             Object[][] arrayOfResult, int sizeOfArray, IdType type,
             String orgName, boolean amsdkIncluded, Object[][] amsdkResults) {
         Map amsdkDNs = new CaseInsensitiveHashMap();
         Map resultsMap = new CaseInsensitiveHashMap();
         int errorCode = IdSearchResults.SUCCESS;
         if (amsdkIncluded) {
             RepoSearchResults amsdkRepoRes = (RepoSearchResults) 
                 amsdkResults[0][0];
             Set results = amsdkRepoRes.getSearchResults();
             Map attrResults = amsdkRepoRes.getResultAttributes();
             Iterator it = results.iterator();
             while (it.hasNext()) {
                 String dn = (String) it.next();
                 String name =  LDAPDN.explodeDN(dn, true)[0];
                 amsdkDNs.put(name, dn);
                 Set attrMaps = new HashSet();
                 attrMaps.add((Map) attrResults.get(dn));
                 resultsMap.put(name, attrMaps);
             }
             errorCode = amsdkRepoRes.getErrorCode();
         }
         for (int i = 0; i < sizeOfArray; i++) {
             RepoSearchResults current = (RepoSearchResults) arrayOfResult[i][0];
             Map configMap = (Map) arrayOfResult[i][1];
             Iterator it = current.getSearchResults().iterator();
             Map allAttrMaps = current.getResultAttributes();
             while (it.hasNext()) {
                 String m = (String) it.next();
                 String mname = DNUtils.DNtoName(m, false);
                 Map attrMap = (Map) allAttrMaps.get(m);
                 attrMap = reverseMapAttributeNames(attrMap, configMap);
                 Set attrMaps = (Set) resultsMap.get(mname);
                 if (attrMaps == null) {
                     attrMaps = new HashSet();
                 }
                 attrMaps.add(attrMap);
                 resultsMap.put(mname, attrMaps);
             }
         }
         IdSearchResults results = new IdSearchResults(type, orgName);
         Iterator it = resultsMap.keySet().iterator();
         while (it.hasNext()) {
             String mname = (String) it.next();
             Map combinedMap = combineAttrMaps((Set) resultsMap.get(mname), 
                     true);
             AMIdentity id = new AMIdentity(token, mname, type, orgName,
                     (String) amsdkDNs.get(mname));
             results.addResult(id, combinedMap);
         }
         results.setErrorCode(errorCode);
         return results;
     }
 
     private Map[] getAttributeNameMap(Map configMap) {
         Set attributeMap = (Set) configMap.get(IdConstants.ATTR_MAP);
 
         if (attributeMap == null || attributeMap.isEmpty()) {
             return null;
         } else {
             Map returnArray[] = new Map[2];
             int size = attributeMap.size();
             returnArray[0] = new CaseInsensitiveHashMap(size);
             returnArray[1] = new CaseInsensitiveHashMap(size);
             Iterator it = attributeMap.iterator();
             while (it.hasNext()) {
                 String mapString = (String) it.next();
                 int eqIndex = mapString.indexOf('=');
                 if (eqIndex > -1) {
                     String first = mapString.substring(0, eqIndex);
                     String second = mapString.substring(eqIndex + 1);
                     returnArray[0].put(first, second);
                     returnArray[1].put(second, first);
                 } else {
                     returnArray[0].put(mapString, mapString);
                     returnArray[1].put(mapString, mapString);
                 }
             }
             return returnArray;
         }
     }
 
     private boolean checkPermission(SSOToken token, String realm, String name,
             Set attrs, IdOperation op, IdType type) throws IdRepoException,
             SSOException {
         if (!ServiceManager.isConfigMigratedTo70()) {
             // Config not migrated to 7.0 which means this is
             // in coexistence mode. Do not perform any delegation check
             return true;
         }
         Set thisAction = null;
         if (op.equals(IdOperation.READ)) {
             // thisAction = readAction;
             // TODO This is a temporary fix where-in all users are
             // being allowed read permisions, till delegation component
             // is fixed to support "user self read" operations
             thisAction = READ_ACTION;
         } else {
             thisAction = WRITE_ACTION;
         }
         try {
             DelegationEvaluator de = new DelegationEvaluator();
             String resource = type.getName();
             if (name != null) {
                 resource += "/" + name;
             }
             DelegationPermission dp = new DelegationPermission(realm,
                     IdConstants.REPO_SERVICE, "1.0", "application", resource,
                     thisAction, Collections.EMPTY_MAP);
             Map envMap = Collections.EMPTY_MAP;
             if (attrs != null) {
                 envMap = new HashMap();
                 envMap.put(DELEGATION_ATTRS_NAME, attrs);
             }
             if (!de.isAllowed(token, dp, envMap)) {
                 Object[] args = { op.getName(), token.getPrincipal().getName() 
                         };
                 throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "402", 
                         args);
             }
             return true;
 
         } catch (DelegationException dex) {
             getDebug().error(
                     "IdServicesImpl.checkPermission "
                     + "Got Delegation Exception: ", dex);
             Object[] args = { op.getName(), token.getPrincipal().getName() };
             throw new IdRepoException(IdRepoBundle.BUNDLE_NAME, "402", args);
         }
     }
 
     private boolean orgExist(SSOToken token, String orgName) {
         // see if the realm actually exists
         boolean isExist = false;
         try {
             ServiceConfigManager scm = new ServiceConfigManager(token,
                     IdConstants.REPO_SERVICE, "1.0");
             if (scm != null) {
                 if (scm.getOrganizationConfig(orgName, null) != null) {
                     isExist = true;
                 } else {
                     if (debug.messageEnabled()) {
                         debug.message("IdServicesImpl.orgExist organization"
                             + " does not exist. orgName=" + orgName);
                     }
                 }
             } else {
                 if (debug.messageEnabled()) {
                     debug.message("IdServicesImpl.orgExist service not"
                         + " configured for this org. orgName=" +orgName);
                 }
            }
         } catch (SMSException smse) {
             getDebug().error("IdServicesImpl.orgExist: "
                 + " sms Exception: realm not found", smse);
         } catch (SSOException ssoe) {
             getDebug().error("IdServicesImpl.orgExist: "
                 + " SSO  Exception: realm not found", ssoe);
         }
         return isExist;
     }
 
 }
