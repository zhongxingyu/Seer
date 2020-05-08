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
 * $Id: DirectoryManagerImpl.java,v 1.13 2008-03-06 17:25:24 goodearth Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.iplanet.am.sdk.remote;
 
 import java.net.InetAddress;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.rmi.RemoteException;
 import java.security.AccessController;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.iplanet.am.sdk.AMDirectoryAccessFactory;
 import com.iplanet.am.sdk.AMException;
 import com.iplanet.am.sdk.AMObjectListener;
 import com.iplanet.am.sdk.AMSearchResults;
 import com.iplanet.am.sdk.common.IComplianceServices;
 import com.iplanet.am.sdk.common.IDCTreeServices;
 import com.iplanet.am.sdk.common.IDirectoryServices;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.comm.server.PLLServer;
 import com.iplanet.services.comm.server.SendNotificationException;
 import com.iplanet.services.comm.share.Notification;
 import com.iplanet.services.comm.share.NotificationSet;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.iplanet.ums.SearchControl;
 import com.iplanet.ums.SortKey;
 import com.sun.identity.common.CaseInsensitiveHashMap;
 import com.sun.identity.idm.AMIdentity;
 import com.sun.identity.idm.IdOperation;
 import com.sun.identity.idm.IdRepo;
 import com.sun.identity.idm.IdRepoException;
 import com.sun.identity.idm.IdRepoListener;
 import com.sun.identity.idm.IdSearchControl;
 import com.sun.identity.idm.IdSearchOpModifier;
 import com.sun.identity.idm.IdSearchResults;
 import com.sun.identity.idm.IdServices;
 import com.sun.identity.idm.IdServicesFactory;
 import com.sun.identity.idm.IdType;
 import com.sun.identity.idm.IdUtils;
 import com.sun.identity.security.AdminTokenAction;
 import com.sun.identity.session.util.RestrictedTokenAction;
 import com.sun.identity.session.util.RestrictedTokenContext;
 import com.sun.identity.shared.Constants;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.sm.SMSUtils;
 import com.sun.identity.sm.SchemaType;
 
 
 public class DirectoryManagerImpl implements DirectoryManagerIF,
     AMObjectListener {
     
     protected static Debug debug = Debug.getInstance("amProfile_Server");
     
     protected static SSOTokenManager tm;
     
     protected static boolean initialized;
     
     // Handle to all the new DirectoryServices implementations.
     protected static IDirectoryServices dsServices;
     
     protected static IDCTreeServices dcTreeServices;
     
     protected static IComplianceServices complianceServices;
     
     // IdRepo Services Handlers
     protected static IdServices idServices;
     
     // Cache of modifications for last 30 minutes & notification URLs
     static int cacheSize = 30;
     
     static LinkedList cacheIndices = new LinkedList();
     
     static LinkedList idrepoCacheIndices = new LinkedList();
     
     static HashMap cache = new HashMap(cacheSize);
     
     static HashMap idrepoCache = new HashMap(cacheSize);
     
     static HashMap notificationURLs = new HashMap();
     
     static String serverURL;
     
     static String serverPort;
     
     static {
         
         dsServices = AMDirectoryAccessFactory.getDirectoryServices();
         dcTreeServices = AMDirectoryAccessFactory.getDCTreeServices();
         complianceServices = AMDirectoryAccessFactory.getComplianceServices();
         
         // Get the IdRepo providers
         idServices = IdServicesFactory.getDataStoreServices();
     }
     
     public DirectoryManagerImpl() {
         if (initialized) {
             return;
         }
         
         // Construct serverURL
 
         serverPort = SystemProperties.get(Constants.AM_SERVER_PORT);
         serverURL = SystemProperties.get(Constants.AM_SERVER_PROTOCOL) +
             "://" + SystemProperties.get(Constants.AM_SERVER_HOST) +
              ":" + serverPort;
                        
         // Get TokenManager and register this class for events
         try {
             tm = SSOTokenManager.getInstance();
             dsServices.addListener((SSOToken) AccessController
                 .doPrivileged(AdminTokenAction.getInstance()), this, null);
             IdRepoListener.addRemoteListener(new IdRepoEventListener());
             
             initialized = true;
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl::init success: "
                     + serverURL);
             }
         } catch (Exception e) {
             debug.error("DirectoryManagerImpl::init ERROR", e);
         }
     }
     
     public String createAMTemplate(String token, String entryDN,
         int objectType, String serviceName, Map attributes, int priority)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.createAMTemplate(ssoToken, entryDN, objectType,
                 serviceName, attributes, priority);
         } catch (AMException e) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.createAMTemplate."
                     + " Caught Exception: " + e);
             }
             throw convertException(e);
         }
         
     }
     
     public void createEntry(String token, String entryName, int objectType,
         String parentDN, Map attributes) throws AMRemoteException,
         SSOException, RemoteException {
         
         try {
             SSOToken ssoToken = getSSOToken(token);
             dsServices.createEntry(ssoToken, entryName, objectType, parentDN,
                 attributes);
         } catch (AMException e) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.createEntry."
                     + " Caught Exception: " + e);
             }
             throw convertException(e);
         }
         
     }
     
     public boolean doesEntryExists(String token, String entryDN)
         throws AMRemoteException, SSOException, RemoteException {
         SSOToken ssoToken = getSSOToken(token);
         return dsServices.doesEntryExists(ssoToken, entryDN);
     }
     
     public String getAMTemplateDN(String token, String entryDN, int objectType,
         String serviceName, int type) throws AMRemoteException,
         SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getAMTemplateDN(ssoToken, entryDN, objectType,
                 serviceName, type);
         } catch (AMException e) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getAMTemplateDN."
                     + " Caught Exception: " + e);
             }
             throw convertException(e);
         }
         
     }
     
     public Map getAttributes3(String token, String entryDN,
         boolean ignoreCompliance, boolean byteValues, int profileType)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getAttributes(ssoToken, entryDN,
                 ignoreCompliance, byteValues, profileType);
         } catch (AMException e) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getAttributes3."
                     + " Caught Exception: " + e);
             }
             throw convertException(e);
         }
     }
     
     public Map getAttributes1(String token, String entryDN, int profileType)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getAttributes(ssoToken, entryDN, profileType);
         } catch (AMException e) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getAttributes1."
                     + " Caught Exception: " + e);
             }
             throw convertException(e);
         }
         
     }
     
     public Map getAttributes2(
         String token,
         String entryDN,
         Set attrNames,
         int profileType
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getAttributes(ssoToken, entryDN, attrNames,
                 profileType);
         } catch (AMException e) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getAttributes2."
                     + " Caught Exception: " + e);
             }
             throw convertException(e);
         }
         
     }
     
     public Map getAttributesByteValues1(
         String token,
         String entryDN,
         int profileType
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getAttributesByteValues(ssoToken, entryDN,
                 profileType);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getAttributesByteValues1."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public Map getAttributesByteValues2(
         String token,
         String entryDN,
         Set attrNames,
         int profileType
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getAttributesByteValues(ssoToken, entryDN,
                 attrNames, profileType);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getAttributesByteValues2."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     
     public Set getAttributesForSchema(String objectclass)
         throws RemoteException {
         return dsServices.getAttributesForSchema(objectclass);
     }
     
     
     public String getCreationTemplateName(int objectType)
         throws RemoteException {
         return dsServices.getCreationTemplateName(objectType);
     }
     
     public Map getDCTreeAttributes(
         String token,
         String entryDN,
         Set attrNames,
         boolean byteValues,
         int objectType
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getDCTreeAttributes(ssoToken, entryDN, attrNames,
                 byteValues, objectType);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getDCTreeAttributes."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public String getDeletedObjectFilter(int objecttype)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             return complianceServices.getDeletedObjectFilter(objecttype);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getDeletedObjectFilter."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public Map getExternalAttributes(
         String token,
         String entryDN,
         Set attrNames,
         int profileType
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getExternalAttributes(ssoToken, entryDN,
                 attrNames, profileType);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getExternalAttributes."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public LinkedList getGroupFilterAndScope(
         String token,
         String entryDN,
         int profileType
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             String[] array = dsServices.getGroupFilterAndScope(ssoToken,
                 entryDN, profileType);
             LinkedList list = new LinkedList();
             for (int i = 0; i < array.length; i++) {
                 list.add(array[i]);
             }
             return list;
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getGroupFilterAndScope."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
     }
     
     public Set getMembers(String token, String entryDN, int objectType)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getMembers(ssoToken, entryDN, objectType);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getMembers."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
     }
     
     public String getNamingAttr(int objectType, String orgDN)
         throws RemoteException {
         return dsServices.getNamingAttribute(objectType, orgDN);
     }
     
     public String getObjectClassFromDS(int objectType) throws RemoteException {
         return dsServices.getObjectClass(objectType);
     }
     
     public int getObjectType(String token, String dn)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getObjectType(ssoToken, dn);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getObjectType."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public String getOrganizationDN(String token, String entryDN)
         throws AMRemoteException, RemoteException, SSOException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getOrganizationDN(ssoToken, entryDN);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getOrganizationDN."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public String verifyAndGetOrgDN(
         String token,
         String entryDN,
         String childDN
     ) throws AMRemoteException, RemoteException, SSOException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.verifyAndGetOrgDN(ssoToken, entryDN, childDN);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.verifyAndGetOrgDN."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public String getOrgDNFromDomain(String token, String domain)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dcTreeServices.getOrganizationDN(ssoToken, domain);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getOrgDNFromDomain."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public String getOrgSearchFilter(String entryDN)
         throws RemoteException {
         return dsServices.getOrgSearchFilter(entryDN);
     }
     
     public Set getRegisteredServiceNames(String token, String entryDN)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             return dsServices.getRegisteredServiceNames(null, entryDN);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getRegisteredServiceNames."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public String getSearchFilterFromTemplate(
         int objectType,
         String orgDN,
         String searchTemplateName
     ) throws RemoteException {
         return dsServices.getSearchFilterFromTemplate(objectType, orgDN,
             searchTemplateName);
     }
     
     public Set getTopLevelContainers(String token)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getTopLevelContainers(ssoToken);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getTopLevelContainers."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public boolean isAncestorOrgDeleted(
         String token,
         String dn,
         int profileType
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return complianceServices.isAncestorOrgDeleted(ssoToken, dn,
                 profileType);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.isAncestorOrgDeleted."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public void modifyMemberShip(
         String token,
         Set members,
         String target,
         int type,
         int operation
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             dsServices.modifyMemberShip(ssoToken, members, target, type,
                 operation);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.modifyMemberShip."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public void registerService(
         String token,
         String orgDN,
         String serviceName
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             dsServices.registerService(ssoToken, orgDN, serviceName);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.registerService."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public void removeAdminRole(String token, String dn, boolean recursive)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             dsServices.removeAdminRole(ssoToken, dn, recursive);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.removeAdminRole."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public void removeEntry(
         String token,
         String entryDN,
         int objectType,
         boolean recursive,
         boolean softDelete
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             dsServices.removeEntry(ssoToken, entryDN, objectType, recursive,
                 softDelete);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.removeEntry."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public String renameEntry(
         String token,
         int objectType,
         String entryDN,
         String newName,
         boolean deleteOldName
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.renameEntry(ssoToken, objectType, entryDN,
                 newName, deleteOldName);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.renameEntry."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public Set search1(
         String token,
         String entryDN,
         String searchFilter,
         int searchScope
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.search(ssoToken, entryDN, searchFilter,
                 searchScope);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.search1."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public Map search2(String token, String entryDN, String searchFilter,
         List sortKeys, int startIndex, int beforeCount, int afterCount,
         String jumpTo, int timeOut, int maxResults, int scope,
         boolean allAttributes, String[] attrNames)
         throws AMRemoteException, SSOException, RemoteException {
         // Construct the SortKeys
         SortKey[] keys = null;
         int keysLength = 0;
         if (sortKeys != null && (keysLength = sortKeys.size()) != 0) {
             keys = new SortKey[keysLength];
             for (int i = 0; i < keysLength; i++) {
                 String data = (String) sortKeys.get(i);
                 keys[i] = new SortKey();
                 keys[i].reverse = data.startsWith("true:");
                 keys[i].attributeName = data.substring(5);
             }
         }
         // Construct SearchControl
         SearchControl sc = new SearchControl();
         if (keys != null) {
             sc.setSortKeys(keys);
         }
         if (jumpTo == null) {
             sc.setVLVRange(startIndex, beforeCount, afterCount);
         } else {
             sc.setVLVRange(jumpTo, beforeCount, afterCount);
         }
         sc.setTimeOut(timeOut);
         sc.setMaxResults(maxResults);
         sc.setSearchScope(scope);
         sc.setAllReturnAttributes(allAttributes);
         
         // Perform the search
         try {
             AMSearchResults results = dsServices.search(tm
                 .createSSOToken(token), entryDN, searchFilter, sc,
                 attrNames);
             // Convert results to Map
             Map answer = results.getResultAttributes();
             if (answer == null) {
                 answer = new HashMap();
             }
             answer.put(com.iplanet.am.sdk.remote.RemoteServicesImpl.AMSR_COUNT,
                 Integer.toString(results.getTotalResultCount()));
             answer.put(
                 com.iplanet.am.sdk.remote.RemoteServicesImpl.AMSR_RESULTS,
                 results.getSearchResults());
             answer.put(com.iplanet.am.sdk.remote.RemoteServicesImpl.AMSR_CODE,
                 Integer.toString(results.getErrorCode()));
             return (answer);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DMI::search(with SearchControl):  entryDN="
                     + entryDN + "the exception is: " +  amex);
             }
             throw convertException(amex);
         }
     }
     
     public Map search3(String token, String entryDN, String searchFilter,
         List sortKeys, int startIndex, int beforeCount, int afterCount,
         String jumpTo, int timeOut, int maxResults, int scope,
         boolean allAttributes, Set attrNamesSet) throws AMRemoteException,
         SSOException, RemoteException {
         // Construct the SortKeys
         SortKey[] keys = null;
         int keysLength = 0;
         if (sortKeys != null && (keysLength = sortKeys.size()) != 0) {
             keys = new SortKey[keysLength];
             for (int i = 0; i < keysLength; i++) {
                 String data = (String) sortKeys.get(i);
                 keys[i] = new SortKey();
                 keys[i].reverse = data.startsWith("true:");
                 keys[i].attributeName = data.substring(5);
             }
         }
         // Construct SearchControl
         SearchControl sc = new SearchControl();
         if (keys != null) {
             sc.setSortKeys(keys);
         }
         if (jumpTo == null) {
             sc.setVLVRange(startIndex, beforeCount, afterCount);
         } else {
             sc.setVLVRange(jumpTo, beforeCount, afterCount);
         }
         sc.setTimeOut(timeOut);
         sc.setMaxResults(maxResults);
         sc.setSearchScope(scope);
         sc.setAllReturnAttributes(allAttributes);
         
         String[] attrNames = new String[attrNamesSet.size()];
         attrNames = (String[]) attrNamesSet.toArray(attrNames);
         
         // Perform the search
         try {
             AMSearchResults results = dsServices.search(tm
                 .createSSOToken(token), entryDN, searchFilter, sc,
                 attrNames);
             // Convert results to Map
             Map answer = results.getResultAttributes();
             if (answer == null) {
                 answer = new HashMap();
             }
             answer.put(com.iplanet.am.sdk.remote.RemoteServicesImpl.AMSR_COUNT,
                 Integer.toString(results.getTotalResultCount()));
             answer.put(
                 com.iplanet.am.sdk.remote.RemoteServicesImpl.AMSR_RESULTS,
                 results.getSearchResults());
             answer.put(com.iplanet.am.sdk.remote.RemoteServicesImpl.AMSR_CODE,
                 Integer.toString(results.getErrorCode()));
             return (answer);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DMI::search(with SearchControl3): entryDN="
                     + entryDN + "the exception is: " +  amex);
             }
             throw convertException(amex);
         }
     }
     
     public void setAttributes(
         String token,
         String entryDN,
         int objectType,
         Map stringAttributes,
         Map byteAttributes,
         boolean isAdd
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             dsServices.setAttributes(ssoToken, entryDN, objectType,
                 stringAttributes, byteAttributes, isAdd);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.setAttributes."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
     }
     
     public void setGroupFilter(String token, String entryDN, String filter)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             dsServices.setGroupFilter(ssoToken, entryDN, filter);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.setGroupFilter."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public void unRegisterService(
         String token,
         String entryDN,
         int objectType,
         String serviceName,
         int type
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             // TODO FIX LATER
             dsServices.unRegisterService(ssoToken, entryDN, objectType,
                 serviceName, type);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.unRegisterService."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public void updateUserAttribute(
         String token,
         Set members,
         String staticGroupDN,
         boolean toAdd
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             dsServices.updateUserAttribute(ssoToken, members, staticGroupDN,
                 toAdd);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.updateUserAttribute."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     public void verifyAndDeleteObject(String token, String dn)
         throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             complianceServices.verifyAndDeleteObject(ssoToken, dn);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.verifyAndDeleteObject."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
         
     }
     
     private AMRemoteException convertException(AMException amex) {
         String ldapErrCodeString = null;
         if ((ldapErrCodeString = amex.getLDAPErrorCode()) == null) {
             
             return new AMRemoteException(amex.getMessage(),
                 amex.getErrorCode(), 0, copyObjectArrayToStringArray(amex
                 .getMessageArgs()));
         } else {
             return new AMRemoteException(amex.getMessage(),
                 amex.getErrorCode(), Integer.parseInt(ldapErrCodeString),
                 copyObjectArrayToStringArray(amex.getMessageArgs()));
         }
     }
     
     private String[] copyObjectArrayToStringArray(Object[] objArray) {
         if ((objArray != null) && (objArray.length != 0)) {
             int count = objArray.length;
             String[] strArray = new String[count];
             for (int i = 0; i < count; i++) {
                 strArray[i] = (String) objArray[i];
             }
             return strArray;
         }
         return null;
     }
     
     public Map getAttributes4(
         String token,
         String entryDN,
         Set attrNames,
         boolean ignoreCompliance,
         boolean byteValues,
         int profileType
     ) throws AMRemoteException, SSOException, RemoteException {
         try {
             SSOToken ssoToken = getSSOToken(token);
             return dsServices.getAttributes(ssoToken, entryDN, attrNames,
                 ignoreCompliance, byteValues, profileType);
         } catch (AMException amex) {
             if (debug.messageEnabled()) {
                 debug.message("DirectoryManagerImpl.getAttributes4."
                     + " Caught Exception: " + amex);
             }
             throw convertException(amex);
         }
     }
     
     // Notification methods
     public Set objectsChanged(int time) throws RemoteException {
         Set answer = new HashSet();
         // Get the cache index for times upto time+2
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(new Date());
         // Add 1 minute to offset, the initial lookup
         calendar.add(Calendar.MINUTE, 1);
         for (int i = 0; i < time + 3; i++) {
             calendar.add(Calendar.MINUTE, -1);
             String cacheIndex = calendarToString(calendar);
             Set modDNs = (Set) cache.get(cacheIndex);
             if (modDNs != null)
                 answer.addAll(modDNs);
         }
         if (debug.messageEnabled()) {
             debug.message("DirectoryManagerImpl:objectsChanged in time: "
                 + time + " minutes:\n" + answer);
         }
         return (answer);
     }
     
     
     private boolean isClientOnSameServer(String clientURL) {
         // Check URL is not the local server
 
         boolean success = true;
         
         URL urlClient = null;
         URL urlServer = null;
         try {
             urlClient = new URL(clientURL);
             urlServer = new URL(serverURL);
         } catch (MalformedURLException e) {
             // TODO Auto-generated catch block
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManagerImpl.checkIfCientOnSameServer()" 
                         + " - clientURL is malformed." + clientURL);
             }
             success = false;
         }
         
         if (success) { // check if it is the same server
             int port = urlClient.getPort();
             if (port == -1) { 
                 // If it is Port 80, and is not explicilty in the URL
                 port = urlClient.getDefaultPort();              
             }
             String clientPort = Integer.toString(port);
 
             // Protocol is same - http, so no need to check that
             boolean sameServer = ((urlServer.getHost().equalsIgnoreCase(
                     urlClient.getHost())) && serverPort.equals(clientPort));
 
             debug.message("DirectoryManagerImpl.checkIfClientOnSameServer() "                     
                     + "Received registerNotification request from client: " 
                     + clientURL + " Server URL " + serverURL 
                     + " Port determined as: " + clientPort + " Check is: " 
                     + sameServer);
             
             return sameServer;
         } else { 
             return false;
         }
     }
     
     public String registerNotificationURL(String url) 
         throws RemoteException {
         String id = SMSUtils.getUniqueID();
         try {
             // Check URL is not the local server
             if (!isClientOnSameServer(url)) {
                 synchronized (notificationURLs) {
                     notificationURLs.put(id, new URL(url));
                 }
                 if (debug.messageEnabled()) {
                     debug.message("DirectoryManagerImpl: " 
                             + "registerNotificationURL register for " 
                             + "notification URL: " + url);
 
                 }
             } else {
                 // Cannot add this server for notifications
                 if (debug.warningEnabled()) {
                     debug.warning("DirectoryManagerImpl:registerURL "
                         + "cannot add local server: " + url);
                 }
                 throw (new RemoteException("invalid-notification-URL"));
             }
         } catch (MalformedURLException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManagerImpl:registerNotificationURL "
                     + " invalid URL: " + url, e);
             }
         }
         return (id);
     }
     
     public void deRegisterNotificationURL(String notificationID)
     throws RemoteException {
         synchronized (notificationURLs) {
             notificationURLs.remove(notificationID);
         }
     }
     
     public void assignService_idrepo(
         String token,
         String type,
         String name,
         String serviceName,
         String stype,
         Map attrMap,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         SchemaType schemaType = new SchemaType(stype);
         idServices.assignService(ssoToken, idtype, name, serviceName,
             schemaType, attrMap, amOrgName, amsdkDN);
         
     }
     
     public String create_idrepo(
         String token,
         String type,
         String name,
         Map attrMap,
         String amOrgName
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         return IdUtils.getUniversalId(idServices.create(ssoToken, idtype, name,
             attrMap, amOrgName));
     }
     
     public void delete_idrepo(
         String token,
         String type,
         String name,
         String orgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         idServices.delete(ssoToken, idtype, name, orgName, amsdkDN);
         
     }
     
     public Set getAssignedServices_idrepo(
         String token,
         String type,
         String name,
         Map mapOfServiceNamesAndOCs,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException,
         SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         return idServices.getAssignedServices(ssoToken, idtype, name,
             mapOfServiceNamesAndOCs, amOrgName, amsdkDN);
     }
     
     public Map getAttributes1_idrepo(
         String token,
         String type,
         String name,
         Set attrNames,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         Map res = idServices.getAttributes(ssoToken, idtype, name, attrNames,
             amOrgName, amsdkDN, true);
         if (res != null && res instanceof CaseInsensitiveHashMap) {
             Map res2 = new HashMap();
             Iterator it = res.keySet().iterator();
             while (it.hasNext()) {
                 Object attr = it.next();
                 res2.put(attr, res.get(attr));
             }
             res = res2;
         }
         return res;
     }
     
     public Map getAttributes2_idrepo(
         String token,
         String type,
         String name,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         Map res = idServices.getAttributes(ssoToken, idtype, name, amOrgName,
             amsdkDN);
         
         if (res != null && res instanceof CaseInsensitiveHashMap) {
             Map res2 = new HashMap();
             Iterator it = res.keySet().iterator();
             while (it.hasNext()) {
                 Object attr = it.next();
                 res2.put(attr, res.get(attr));
             }
             res = res2;
         }
         return res;
     }
     
     public Set getMembers_idrepo(
         String token,
         String type,
         String name,
         String amOrgName,
         String membersType,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         Set results = new HashSet();
         IdType idtype = IdUtils.getType(type);
         IdType mtype = IdUtils.getType(membersType);
         Set idSet = idServices.getMembers(ssoToken, idtype, name, amOrgName,
             mtype, amsdkDN);
         if (idSet != null) {
             Iterator it = idSet.iterator();
             while (it.hasNext()) {
                 AMIdentity id = (AMIdentity) it.next();
                 results.add(IdUtils.getUniversalId(id));
             }
         }
         return results;
     }
     
     public Set getMemberships_idrepo(
         String token,
         String type,
         String name,
         String membershipType,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         Set results = new HashSet();
         IdType idtype = IdUtils.getType(type);
         IdType mtype = IdUtils.getType(membershipType);
         Set idSet = idServices.getMemberships(ssoToken, idtype, name, mtype,
             amOrgName, amsdkDN);
         if (idSet != null) {
             Iterator it = idSet.iterator();
             while (it.hasNext()) {
                 AMIdentity id = (AMIdentity) it.next();
                 results.add(IdUtils.getUniversalId(id));
             }
         }
         return results;
     }
     
     public Map getServiceAttributes_idrepo(
         String token,
         String type,
         String name,
         String serviceName,
         Set attrNames,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         return idServices.getServiceAttributes(ssoToken, idtype, name,
             serviceName, attrNames, amOrgName, amsdkDN);
     }
 
     public Map getBinaryServiceAttributes_idrepo(
         String token, String type,
         String name, 
         String serviceName, 
         Set attrNames, 
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken stoken = tm.createSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         return idServices.getBinaryServiceAttributes(stoken, idtype, name,
                 serviceName, attrNames, amOrgName, amsdkDN);
     }
 
     public Map getServiceAttributesAscending_idrepo(
         String token,
         String type,
         String name,
         String serviceName,
         Set attrNames,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         return idServices.getServiceAttributesAscending(ssoToken, idtype,
             name, serviceName, attrNames, amOrgName, amsdkDN);
     }
     
     public Set getSupportedOperations_idrepo(
         String token,
         String type,
         String amOrgName
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         Set opSet = idServices
             .getSupportedOperations(ssoToken, idtype, amOrgName);
         Set resSet = new HashSet();
         if (opSet != null) {
             Iterator it = opSet.iterator();
             while (it.hasNext()) {
                 IdOperation thisop = (IdOperation) it.next();
                 String opStr = thisop.getName();
                 resSet.add(opStr);
             }
         }
         return resSet;
     }
     
     public Set getSupportedTypes_idrepo(String token, String amOrgName)
         throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         Set typeSet = idServices.getSupportedTypes(ssoToken, amOrgName);
         Set resTypes = new HashSet();
         if (typeSet != null) {
             Iterator it = typeSet.iterator();
             while (it.hasNext()) {
                 IdType thistype = (IdType) it.next();
                 String typeStr = thistype.getName();
                 resTypes.add(typeStr);
             }
         }
         return resTypes;
     }
 
     public Set getFullyQualifiedNames_idrepo(String token, String type,
         String name, String amOrgName)
         throws RemoteException, IdRepoException, SSOException {
         SSOToken stoken = tm.createSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         Set opSet = idServices.getFullyQualifiedNames(stoken, idtype,
                 name, amOrgName);
         Set resSet = null;
         if (opSet != null) {
             // Convert CaseInsensitiveHashSet to HashSet
             resSet = new HashSet();
             Iterator it = opSet.iterator();
             while (it.hasNext()) {
                 IdOperation thisop = (IdOperation) it.next();
                 String opStr = thisop.getName();
                 resSet.add(opStr);
             }
         }
         return resSet;
     }
 
     public boolean isExists_idrepo(
         String token,
         String type,
         String name,
         String amOrgName
     ) throws RemoteException, SSOException, IdRepoException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         return idServices.isExists(ssoToken, idtype, name, amOrgName);
         
     }
     
     public boolean isActive_idrepo(
         String token,
         String type,
         String name,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         return idServices.isActive(ssoToken, idtype, name, amOrgName, amsdkDN);
     }
 
     public void setActiveStatus_idrepo(
         String token,
         String type,
         String name,
         String amOrgName,
         String amsdkDN,
         boolean active
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         idServices.setActiveStatus(
             ssoToken, idtype, name, amOrgName, amsdkDN, active);
 
     }
 
     public void modifyMemberShip_idrepo(
         String token,
         String type,
         String name,
         Set members,
         String membersType,
         int operation,
         String amOrgName
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         IdType mtype = IdUtils.getType(membersType);
         idServices.modifyMemberShip(ssoToken, idtype, name, members, mtype,
             operation, amOrgName);
     }
     
     public void modifyService_idrepo(
         String token,
         String type,
         String name,
         String serviceName,
         String stype,
         Map attrMap,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         SchemaType schematype = new SchemaType(stype);
         idServices.modifyService(ssoToken, idtype, name, serviceName,
             schematype, attrMap, amOrgName, amsdkDN);
     }
     
     public void removeAttributes_idrepo(
         String token,
         String type,
         String name,
         Set attrNames,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         idServices.removeAttributes(ssoToken, idtype, name, attrNames,
             amOrgName, amsdkDN);
     }
     
     public Map search1_idrepo(
         String token,
         String type,
         String pattern,
         Map avPairs,
         boolean recursive,
         int maxResults,
         int maxTime,
         Set returnAttrs,
         String amOrgName
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         return search2_idrepo(token, type, pattern, maxTime, maxResults,
             returnAttrs, (returnAttrs == null), 0, avPairs, recursive,
             amOrgName);
     }
     
     public Map search2_idrepo(
         String token,
         String type,
         String pattern,
         int maxTime,
         int maxResults,
         Set returnAttrs,
         boolean returnAllAttrs,
         int filterOp,
         Map avPairs,
         boolean recursive,
         String amOrgName
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         IdSearchControl ctrl = new IdSearchControl();
         ctrl.setAllReturnAttributes(returnAllAttrs);
         ctrl.setMaxResults(maxResults);
         ctrl.setReturnAttributes(returnAttrs);
         ctrl.setTimeOut(maxTime);
         IdSearchOpModifier modifier = (filterOp == IdRepo.OR_MOD) ?
             IdSearchOpModifier.OR : IdSearchOpModifier.AND;
         ctrl.setSearchModifiers(modifier, avPairs);
         IdSearchResults idres = idServices.search(ssoToken, idtype, pattern,
             ctrl, amOrgName);
         return IdSearchResultsToMap(idres);
     }
     
     public void setAttributes_idrepo(
         String token,
         String type,
         String name,
         Map attributes,
         boolean isAdd,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         idServices.setAttributes(ssoToken, idtype, name, attributes, isAdd,
             amOrgName, amsdkDN, true);
     }
     
     public void setAttributes2_idrepo(
         String token,
         String type,
         String name,
         Map attributes,
         boolean isAdd,
         String amOrgName,
         String amsdkDN,
         boolean isString
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         idServices.setAttributes(ssoToken, idtype, name, attributes, isAdd,
             amOrgName, amsdkDN, isString);
     }
     
     public void unassignService_idrepo(
         String token,
         String type,
         String name,
         String serviceName,
         Map attrMap,
         String amOrgName,
         String amsdkDN
     ) throws RemoteException, IdRepoException, SSOException {
         SSOToken ssoToken = getSSOToken(token);
         IdType idtype = IdUtils.getType(type);
         idServices.unassignService(ssoToken, idtype, name, serviceName,
             attrMap, amOrgName, amsdkDN);
         
     }
     
     // Implementation for AMObjectListener
     public void objectChanged(String name, int type, Map configMap) {
         processEntryChanged(EventListener.OBJECT_CHANGED, name, type, null,
             true);
     }
     
     public void objectsChanged(String name, int type, Set attrNames,
         Map configMap) {
         processEntryChanged(EventListener.OBJECTS_CHANGED, name, type,
             attrNames, true);
     }
     
     public void permissionsChanged(String name, Map configMap) {
         processEntryChanged(EventListener.PERMISSIONS_CHANGED, name, 0, null,
             true);
     }
     
     public void allObjectsChanged() {
         processEntryChanged(EventListener.ALL_OBJECTS_CHANGED, "", 0, null,
             true);
     }
     
     public void deRegisterNotificationURL_idrepo(
         String notificationID)
     throws RemoteException {
         synchronized (notificationURLs) {
             notificationURLs.remove(notificationID);
         }
     }
     
     public Set objectsChanged_idrepo(int time) throws RemoteException {
         Set answer = new HashSet();
         // Get the cache index for times upto time+2
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(new Date());
         // Add 1 minute to offset, the initial lookup
         calendar.add(Calendar.MINUTE, 1);
         for (int i = 0; i < time + 3; i++) {
             calendar.add(Calendar.MINUTE, -1);
             String cacheIndex = calendarToString(calendar);
             Set modDNs = (Set) idrepoCache.get(cacheIndex);
             if (modDNs != null)
                 answer.addAll(modDNs);
         }
         if (debug.messageEnabled()) {
             debug.message("DirectoryManagerImpl:objectsChanged in time: "
                 + time + " minutes:\n" + answer);
         }
         return (answer);
     }
     
     public String registerNotificationURL_idrepo(String url)
     throws RemoteException {
         // TODO Auto-generated method stub
         String id = SMSUtils.getUniqueID();
         try {
             // Check URL is not the local server
             if (!isClientOnSameServer(url)) {
                 synchronized (notificationURLs) {
                     notificationURLs.put(id, new URL(url));
                 }
                 if (debug.messageEnabled()) {
                     debug.message("DirectoryManagerImpl:" 
                             + "registerNotificationURL_idrepo() - register " 
                             + "for notification URL: " + url);
                 }
             } else {
                 // Cannot add this server for notifications
                 if (debug.warningEnabled()) {
                     debug.warning("DirectoryManagerImpl:registerURL "
                         + "cannot add local server: " + url);
                 }
                 throw (new RemoteException("invalid-notification-URL"));
             }
         } catch (MalformedURLException e) {
             if (debug.warningEnabled()) {
                 debug.warning("DirectoryManagerImpl:registerNotificationURL "
                     + " invalid URL: " + url, e);
             }
         }
         return (id);
     }
     
     // Implementation to process entry changed events
     protected static synchronized void processEntryChanged(String method,
         String name, int type, Set attrNames, boolean amsdk) {
         
         debug.message("DirectoryManagerImpl.processEntryChaged method "
                 + "processing");
         
         HashMap thisCache = amsdk ? cache : idrepoCache;
         LinkedList cIndices = amsdk ? cacheIndices : idrepoCacheIndices;
         // Obtain the cache index
         Calendar calendar = Calendar.getInstance();
         calendar.setTime(new Date());
         String cacheIndex = calendarToString(calendar);
         Set modDNs = (Set) thisCache.get(cacheIndex);
         if (modDNs == null) {
             modDNs = new HashSet();
             thisCache.put(cacheIndex, modDNs);
             // Maintain cacheIndex
             cIndices.addFirst(cacheIndex);
             if (cIndices.size() > cacheSize) {
                 String removedIndex = (String) cIndices.removeLast();
                 thisCache.remove(removedIndex);
             }
         }
         
         // Construct the XML document for the event change
         StringBuffer sb = new StringBuffer(100);
         sb.append("<EventNotification><AttributeValuePair>").append(
             "<Attribute name=\"method\" /><Value>").append(method).append(
             "</Value></AttributeValuePair>").append(
             "<AttributeValuePair><Attribute name=\"entityName\" />")
             .append("<Value>").append(name).append(
             "</Value></AttributeValuePair>");
         if (method.equalsIgnoreCase("objectChanged")
         || method.equalsIgnoreCase("objectsChanged")) {
             sb.append("<AttributeValuePair><Attribute name=\"eventType\" />")
             .append("<Value>").append(type).append(
                 "</Value></AttributeValuePair>");
             if (method.equalsIgnoreCase("objectsChanged")) {
                 sb.append("<AttributeValuePair><Attribute ").append(
                     "name=\"attrNames\"/>");
                 for (Iterator items = attrNames.iterator(); items.hasNext();) {
                     String attr = (String) items.next();
                     sb.append("<Value>").append(attr).append("</Value>");
                 }
                 sb.append("</AttributeValuePair>");
             }
         }
         sb.append("</EventNotification>");
         // Add to cache
         modDNs.add(sb.toString());
         if (debug.messageEnabled()) {
             debug.message("DirectoryManagerImpl::processing entry change: "
                 + sb.toString());
             debug.message("DirectoryManagerImpl = notificationURLS" 
                     + notificationURLs.values());
 
         }
         
         // If notification URLs are present, send notifications
         Map notifications = new HashMap(notificationURLs); // Make a copy
         NotificationSet ns = null;
         synchronized (notificationURLs) {
            for (Iterator entries = notificationURLs.entrySet().iterator(); 
                 entries.hasNext();) {
                 Map.Entry entry = (Map.Entry) entries.next();
                 String id = (String) entry.getKey();
                 URL url = (URL) entry.getValue();
             
                 // Construct NotificationSet
                 if (ns == null) {
                     Notification notification = 
                         new Notification(sb.toString());
                     ns = amsdk ? new NotificationSet(
                         com.iplanet.am.sdk.remote.RemoteServicesImpl
                         .SDK_SERVICE)
                         : new NotificationSet(
                         com.iplanet.am.sdk.remote.RemoteServicesImpl
                         .IDREPO_SERVICE);
                     ns.addNotification(notification);
                 }
                 try {
                     PLLServer.send(url, ns);
                     if (debug.messageEnabled()) {
                         debug.message("DirectorManagerImpl:sentNotification "
                             + "URL: " + url + " Data: " + ns);
                     }
                 } catch (SendNotificationException ne) {
                     if (debug.warningEnabled()) {
                         debug.warning("DirectoryManagerImpl: failed sending "
                             + "notification to: " + url + "\nRemoving "
                             + "URL from notification list.", ne);
                     }
                     // Remove the URL from Notification List
                     notificationURLs.remove(id);
                 }
             }
         }
     }
     
     private static String calendarToString(Calendar calendar) {
         // Get year, month, date, hour and minute
         int year = calendar.get(Calendar.YEAR);
         int month = calendar.get(Calendar.MONTH);
         int date = calendar.get(Calendar.DATE);
         int hour = calendar.get(Calendar.HOUR);
         int minute = calendar.get(Calendar.MINUTE);
         // Clear the calendar, set the params and get the string
         calendar.clear();
         calendar.set(year, month, date, hour, minute);
         return (serverURL + calendar.toString());
     }
     
     private Map IdSearchResultsToMap(IdSearchResults res) {
         // TODO ..check if the Map gets properly populated and sent.
         Map answer = new HashMap();
         Map attrMaps = new HashMap();
         Set idStrings = new HashSet();
 
         Map answer1 = res.getResultAttributes();
         Set ids = res.getSearchResults();
         if (ids != null) {
             Iterator it = ids.iterator();
             while (it.hasNext()) {
                 AMIdentity id = (AMIdentity) it.next();
                 String idStr = IdUtils.getUniversalId(id);
                 idStrings.add(idStr);
                 Map attrMap = (Map) answer1.get(id);
                 if (attrMap != null) {
                     Map cattrMap = new HashMap();
                     for (Iterator items = attrMap.keySet().iterator();
                         items.hasNext();) {
                         Object item = items.next();
                         cattrMap.put(item.toString(), attrMap.get(item));
                     }
                     attrMaps.put(idStr, attrMap);
                 }
             }
         }
         answer.put(com.iplanet.am.sdk.remote.RemoteServicesImpl.AMSR_RESULTS,
             idStrings);
         answer.put(com.iplanet.am.sdk.remote.RemoteServicesImpl.AMSR_CODE,
             new Integer(res.getErrorCode()));
         answer.put(com.iplanet.am.sdk.remote.RemoteServicesImpl.AMSR_ATTRS,
             attrMaps);
         return (answer);
     }
     
     public Map getConfigMap() {
         
         return null;
     }
     
     public void setConfigMap(Map cmap) {
         
     }
 
 
     /*
      * Check if agent token ID is appended to the token string.
      * if yes, we use it as a restriction context. This is meant
      * for cookie hijacking feature where agent appends the agent token ID
      * to the user sso token before sending it over to the server for
      * validation.
      */
     private SSOToken getSSOToken(String token) throws SSOException {
         int index = token.indexOf(" ");
 
         if (index == -1) {
             return tm.createSSOToken(token);
         }
 
         SSOToken stoken = null;
         String agentTokenStr = token.substring(index +1);
         String tokenStr = token.substring(0,index);
         final String ftoken = tokenStr;
         
         try {
             /*
              * for 7.0 patch-4 agent, IP address maybe send back to server.
              * this is a very simple check for IP Address
              */
             Object context = null;
             if (agentTokenStr.indexOf('.') != -1) {
                 context = InetAddress.getByName(agentTokenStr);
             } else {
                 context = tm.createSSOToken(agentTokenStr);
             } 
             stoken = (SSOToken)RestrictedTokenContext.doUsing(context,
                 new RestrictedTokenAction() {
                     public Object run() throws Exception {
                         return tm.createSSOToken(ftoken);
                     }
             });
        } catch (SSOException e) {
            debug.error("DirectoryManagerImpl.getSSOToken", e);
            return tm.createSSOToken(tokenStr);
        } catch (Exception e) {
            debug.error("DirectoryManagerImpl.getSSOToken", e);
        }
        return stoken;
    }
 
 }
