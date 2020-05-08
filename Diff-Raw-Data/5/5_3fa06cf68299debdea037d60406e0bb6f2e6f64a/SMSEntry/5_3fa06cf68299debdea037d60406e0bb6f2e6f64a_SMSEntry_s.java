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
 * $Id: SMSEntry.java,v 1.1 2005-11-01 00:31:27 arvindp Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.sm;
 
 import java.net.URL;
 import java.security.AccessController;
 import java.security.Principal;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 import javax.naming.directory.BasicAttribute;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.ModificationItem;
 
 import netscape.ldap.LDAPException;
 import netscape.ldap.util.DN;
 
 import com.iplanet.am.util.AMResourceBundleCache;
 import com.iplanet.am.util.Cache;
 import com.iplanet.am.util.Debug;
 import com.iplanet.am.util.OrderedSet;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.services.naming.WebtopNaming;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.sso.SSOTokenManager;
 import com.iplanet.ums.IUMSConstants;
 import com.sun.identity.common.CaseInsensitiveHashMap;
 import com.sun.identity.common.CaseInsensitiveHashSet;
 import com.sun.identity.common.DNUtils;
 import com.sun.identity.delegation.DelegationEvaluator;
 import com.sun.identity.delegation.DelegationException;
 import com.sun.identity.delegation.DelegationPermission;
 import com.sun.identity.jaxrpc.SOAPClient;
 
 /**
  * This object represents a SMS entry in datstore, similar to UMS's equivalent
  * class called PersistentObject.
  * <p>
  * This class is used both to read and write information into the datastore.
  */
 public class SMSEntry implements Cloneable {
 
     // Name of place holder nodes
     public static final String SERVICES_NODE = "services";
 
     public static final String PLACEHOLDER_RDN = "ou";
 
     public static final String SERVICES_RDN = PLACEHOLDER_RDN + "="
             + SERVICES_NODE;
 
     public static final String COMMA = ",";
 
     // Debug instance & SSOTokenManager
     public static Debug debug = Debug.getInstance("amSMS");
 
     public static Debug eventDebug = Debug.getInstance("amSMSEvent");
 
     public static SSOTokenManager tm;
 
     // Variable for caching parse organization names
     private static Cache cache = new Cache(500);
 
     // Variable to check if the SMS entries can be cached
     static String CACHE_PROPERTY = "com.iplanet.am.sdk.caching.enabled";
 
     static boolean cacheSMSEntries;
 
     public static ResourceBundle bundle;
 
     // BaseDN (Root DN)
     static String DEFAULT_ORG_PROPERTY = "com.iplanet.am.defaultOrg";
 
     static String baseDN;
 
     static int baseDNCount;
 
     static SMSException initializationException;
 
     // DataStore Implementation Object
     static final String SMS_OBJECT_PROPERTY = 
         "com.sun.identity.sm.sms_object_class_name";
 
     static final String DEFAULT_SMS_CLASS_NAME = 
         "com.sun.identity.sm.ldap.SMSLdapObject";
 
     static final String JAXRPC_SMS_CLASS_NAME = 
         "com.sun.identity.sm.jaxrpc.SMSJAXRPCObject";
 
     static final String SMS_ENABLE_DB_NOTIFICATION = 
         "com.sun.identity.sm.enableDataStoreNotification";
 
     // Flag to enable LDAP's proxy support
     public static final String DB_PROXY_ENABLE = 
         "com.sun.identity.sm.ldap.enableProxy";
 
     static SMSObject smsObject;
 
     // Variable for import/export
     static final String SLASH_STR = "/";
 
     static final String DOT_STR = ".";
 
     public static final String EXPORTEDARGS = "exportedTo";
 
     public static final String IMPORTEDARGS = "importedFrom";
 
     // Variables for checking delegation permission
     static final String AUTH_SUPER_USER = 
         "com.sun.identity.authentication.super.user";
 
     static final String READ = "READ";
 
     static final String MODIFY = "MODIFY";
 
     static Set specialUserSet = new HashSet(50);
 
     static Set readActionSet = new HashSet(2);
 
     static Set modifyActionSet = new HashSet(2);
 
     static DelegationEvaluator dlgEval;
 
     static boolean SMSJAXRPCObjectFlg;
 
     static boolean backendProxyEnabled;
 
     static SSOToken adminSSOToken;
 
     static CaseInsensitiveHashSet mCaseSensitiveAttributes;
 
     // Notification handlers
     static Set changeListeners = new HashSet();
 
     static List localChanges = Collections.synchronizedList(new LinkedList());
 
     static int LOCAL_CHANGES_MAX_SIZE = 25;
 
     static boolean enableDataStoreNotification;
 
     // Initalize the above varibales
     static {
         // Initialize for checking delegation permissions
         readActionSet.add(READ);
         modifyActionSet.add(MODIFY);
         // Cache internal users
         String adminUser = SystemProperties.get(AUTH_SUPER_USER, "");
         if (adminUser != null && adminUser.length() != 0) {
             specialUserSet.add(DNUtils.normalizeDN(adminUser));
         }
         // Add adminDN to the specialUserSet
         adminUser = com.iplanet.am.util.AdminUtils.getAdminDN();
         if (adminUser != null && adminUser.length() != 0) {
             specialUserSet.add(DNUtils.normalizeDN(adminUser));
         }
         if (debug.messageEnabled()) {
             debug.message("SMSEntry: Special User Set: " + specialUserSet);
         }
         // Check if backend has permission check enabled
         String proxy = SystemProperties.get(DB_PROXY_ENABLE);
         if (proxy != null && proxy.equalsIgnoreCase("true")) {
             backendProxyEnabled = true;
         }
 
         // initialize case sensitive attributes
         // (everything else is case Insensitive)
         mCaseSensitiveAttributes = new CaseInsensitiveHashSet(3);
         mCaseSensitiveAttributes.add(SMSEntry.ATTR_SCHEMA);
         mCaseSensitiveAttributes.add(SMSEntry.ATTR_PLUGIN_SCHEMA);
         mCaseSensitiveAttributes.add(SMSEntry.ATTR_KEYVAL);
 
         // Check if SMSEntries can be cached
         String cacheEnabled = System.getProperty(CACHE_PROPERTY,
                 SystemProperties.get(CACHE_PROPERTY, "true"));
         if (cacheEnabled.equalsIgnoreCase("true")) {
             cacheSMSEntries = true;
         }
         if (debug.messageEnabled()) {
             debug.message("SMSEntry: cache enabled: " + cacheSMSEntries);
         }
 
         // Check if backend datastore notification needs to be enabled
         String enable = SystemProperties.get(SMS_ENABLE_DB_NOTIFICATION);
         if (enable != null && enable.equalsIgnoreCase("true")) {
             enableDataStoreNotification = true;
         }
         if (debug.messageEnabled()) {
             debug.message("SMSEntry: DN notification enabled: "
                     + enableDataStoreNotification);
         }
 
         // Resource bundle
         AMResourceBundleCache amCache = AMResourceBundleCache.getInstance();
         bundle = amCache.getResBundle(IUMSConstants.UMS_BUNDLE_NAME,
                 java.util.Locale.ENGLISH);
 
         // Get an instance of SMSObject
         String smsClassName = SystemProperties.get(SMS_OBJECT_PROPERTY,
                 DEFAULT_SMS_CLASS_NAME);
         try {
             tm = SSOTokenManager.getInstance();
             Class smsEntryClass = Class.forName(smsClassName);
             smsObject = (SMSObject) smsEntryClass.newInstance();
             if (debug.messageEnabled()) {
                 debug.message("Using SMS object class " + smsClassName);
             }
         } catch (ClassNotFoundException cfe) {
             if (debug.warningEnabled()) {
                 debug.warning("SMSObject class not found: " + smsClassName);
             }
             initializationException = new SMSException(cfe,
                     "sms-init-no-class-found");
         } catch (Exception e) {
             if (debug.warningEnabled()) {
                 debug.warning("SMSEntry: error in instantiation of: "
                         + smsClassName + " Message: " + e.getMessage());
             }
             initializationException = new SMSException(e,
                     "sms-instantiation-failed");
         }
 
         // Check if smsObject is null
         if (smsObject == null) {
             try {
                 if (smsClassName.equals(DEFAULT_SMS_CLASS_NAME)) {
                     debug.message("SMSEntry: Using default JAXRPC " +
                             "implementation");
                     smsObject = (SMSObject) Class
                             .forName(JAXRPC_SMS_CLASS_NAME).newInstance();
                     SMSJAXRPCObjectFlg = true;
                 } else if (smsClassName.equals(JAXRPC_SMS_CLASS_NAME)) {
                     debug.message("SMSEntry: Using default JAXRPC " +
                             "implementation");
                     smsObject = (SMSObject) Class
                             .forName(JAXRPC_SMS_CLASS_NAME).newInstance();
                     SMSJAXRPCObjectFlg = true;
                 } else {
                     debug.message("SMSEntry: Using default LDAP " +
                             "implementation");
                     smsObject = (SMSObject) Class.forName(
                             DEFAULT_SMS_CLASS_NAME).newInstance();
                 }
                 initializationException = null;
             } catch (Exception fe) {
                 debug.error("SMSEntry: Error in getting configured/default "
                         + "SMSObject", initializationException);
                 debug.error("SMSEntry: Error in getting backupSMSObject", fe);
             }
         }
 
         // Get the baseDN
         String temp = smsObject.getRootSuffix();
         if (temp != null) {
             baseDN = DNUtils.normalizeDN(temp);
         } else {
             baseDN = "o=unknown-suffix";
         }
         if (baseDN == null) {
             // Problem in getting base DN
             initializationException = new SMSException(bundle
                     .getString("sms-invalid-dn"), "sms-invalid-dn");
         } else {
             baseDNCount = (new StringTokenizer(baseDN, ",")).countTokens();
         }
     }
 
     /**
      * Constructor for a persistent SMS object given an authenticated SSOToken
      * and DN. The entry is read from the directory.
      */
     SMSEntry(SSOToken token, String dn) throws SSOException, SMSException {
         if (initializationException != null)
             throw (initializationException);
         ssoToken = token;
         this.dn = dn;
         normalizedDN = DNUtils.normalizeDN(dn);
         read();
     }
 
     /**
      * Returns the read attributes
      */
     public Map getAttributes() {
         return (attrSet);
     }
 
     /**
      * Returns the attribute values for the given attribute name. The values are
      * returned from the cached attribute set. It is not read from the
      * directory.
      */
     String[] getAttributeValues(String attrName) {
         return getAttributeValues(attrName, false);
     }
 
     String[] getAttributeValues(String attrName, boolean ignoreCache) {
         if (ignoreCache || !cacheSMSEntries) {
             try {
                 read();
             } catch (SMSException e) {
                 // this should not happen
                 debug.error("SMSLdapEntry: Error in reading attrs: " + e);
             } catch (SSOException ssoe) {
                 // this should not happen
                 debug.error("SMSLdapEntry: SSOToken problem "
                         + "in reading attrs: " + ssoe);
             }
         }
         Set attr = (attrSet == null) ? null : (Set) attrSet.get(attrName);
         return ((attr == null) ? null : (String[]) attr.toArray(new String[attr
                 .size()]));
     }
 
     /**
      * Adds the attribute value to the given attribute name. It is stored
      * locally and is not written to the directory.
      */
     void addAttribute(String attrName, String value) throws SMSException {
         Set attrValues = null;
         if (attrSet == null) {
             attrSet = new CaseInsensitiveHashMap();
         } else if (attrSet.containsKey(attrName)) {
             attrValues = (Set) attrSet.get(attrName);
             if (attrValues.contains(value)) {
                 // Value is already present
                 if (debug.messageEnabled()) {
                     debug.message("SMSEntry: Duplicate value for addition");
                 }
                 throw (new SMSException(new LDAPException(bundle
                         .getString(IUMSConstants.SMS_ATTR_OR_VAL_EXISTS),
                         LDAPException.ATTRIBUTE_OR_VALUE_EXISTS),
                         "sms-ATTR_OR_VAL_EXISTS"));
             }
         }
 
         // Add the attribute to attrset
         if (attrValues == null) {
             attrValues = new HashSet();
         }
         attrValues.add(value);
         attrSet.put(attrName, attrValues);
 
         // Check if the modification set exists, and add the attribute
         if (modSet == null) {
             modSet = new HashSet();
         }
         modSet.add(new ModificationItem(DirContext.ADD_ATTRIBUTE,
                 new BasicAttribute(attrName, value)));
     }
 
     /**
      * Set the attribute values. <code>save()</code> must be called to make
      * the changes persistant
      */
     void setAttribute(String attrName, String[] attrValues) {
         // Attribute Values to be Set and BasicAttribute
         Set attrs = new HashSet();
         BasicAttribute ba = new BasicAttribute(attrName);
         for (int i = 0; attrValues != null && i < attrValues.length; i++) {
             attrs.add(attrValues[i]);
             ba.add(attrValues[i]);
         }
 
         // Check if attrSet, modSet is present, if not create
         attrSet = (attrSet == null) ? (new CaseInsensitiveHashMap()) : attrSet;
         modSet = (modSet == null) ? (new HashSet()) : modSet;
 
         // Check if the attribute exists, if not present add, else replace
         if (!attrSet.containsKey(attrName)) {
             // Not present: add it, update modset
             modSet.add(new ModificationItem(DirContext.ADD_ATTRIBUTE, ba));
         } else {
             // Remove old attrbute and add the new attribute, update modset
             modSet.add(new ModificationItem(DirContext.REPLACE_ATTRIBUTE, ba));
         }
         // Update attrset
         attrSet.put(attrName, attrs);
     }
 
     /**
      * Remove the attribute value from the attribute.
      */
     void removeAttribute(String attrName, String value) throws SMSException {
         Set attr = null;
         if ((attrSet == null) || ((attr = (Set) attrSet.get(attrName)) == null)
                 || (!attr.contains(value))) {
             throw (new SMSException(new LDAPException(bundle
                     .getString(IUMSConstants.SMS_ATTR_OR_VAL_EXISTS),
                     LDAPException.ATTRIBUTE_OR_VALUE_EXISTS),
                     "sms-ATTR_OR_VAL_EXISTS"));
         }
         // Update attr and attrSet --> will not be null
         attr.remove(value);
         attrSet.put(attrName, attr);
 
         // Update modification set
         if (modSet == null) {
             modSet = new HashSet();
         }
         modSet.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE,
                 new BasicAttribute(attrName, value)));
     }
 
     /**
      * Remove the attribute from the entry.
      */
     void removeAttribute(String attrName) throws SMSException {
         Set attribute = (Set) attrSet.get(attrName);
         if (attribute == null) {
             throw (new SMSException(new LDAPException(bundle
                     .getString(IUMSConstants.SMS_ATTR_OR_VAL_EXISTS),
                     LDAPException.ATTRIBUTE_OR_VALUE_EXISTS),
                     "sms-ATTR_OR_VAL_EXISTS"));
         }
         attrSet.remove(attrName);
         if (modSet == null) {
             modSet = new HashSet();
         }
         BasicAttribute ba = new BasicAttribute(attrName, attribute);
         for (Iterator items = attribute.iterator(); items.hasNext();)
             ba.add(items.next());
         modSet.add(new ModificationItem(DirContext.REMOVE_ATTRIBUTE, ba));
     }
 
     /**
      * Checks if the attribute value exists in the attribute
      */
     boolean containsAttrValue(String attrName, String attrValue) {
         if (attrSet != null) {
             Set attr = (Set) attrSet.get(attrName);
             if (attr != null) {
                 return (attr.contains(attrValue));
             }
         }
         return (false);
     }
 
     /**
      * Reads in the object from persistent store, assuming that the guid and the
      * principal are valid
      */
     void read() throws SSOException, SMSException {
         read(ssoToken);
     }
 
     /**
      * Reads in the object from persistent store using the given ssoToken
      */
     void read(SSOToken token) throws SSOException, SMSException {
         // If backend has proxy enabled, check for delegation
         // permissions and use admin token
         if (backendProxyEnabled) {
             if (isAllowed(token, normalizedDN, readActionSet)) {
                 if (adminSSOToken == null) {
                     adminSSOToken = 
                         (SSOToken) AccessController.doPrivileged(
                                 com.sun.identity.security.AdminTokenAction.
                                         getInstance());
                 }
                 token = adminSSOToken;
             }
         } else {
             // Check for delegation permission throws exception if
             // permission is denied
             getDelegationPermission(token, normalizedDN, readActionSet);
         }
         attrSet = smsObject.read(token, dn);
         if (attrSet == null) {
             newEntry = true;
         } else {
             newEntry = false;
         }
     }
 
     /**
      * Save the modification(s) to the object. Save the changes made so far to
      * the datastore.
      */
     void save() throws SSOException, SMSException {
         if (readOnly) {
             if (debug.warningEnabled()) {
                 debug.warning("SMSEntry: Attempted to save an entry that "
                         + "is marked as read-only: " + dn);
             }
             throw (new SMSException(SMSException.STATUS_NO_PERMISSION,
                     "sms-INSUFFICIENT_ACCESS_RIGHTS"));
         }
         save(ssoToken);
     }
 
     /**
      * Save the modification(s) to the object. Save the changes made so far to
      * the datastore using the given SSOToken
      */
     void save(SSOToken token) throws SSOException, SMSException {
         // If backend has proxy enabled, check for delegation
         // permissions and use admin token
         if (backendProxyEnabled) {
             if (isAllowed(token, normalizedDN, modifyActionSet)) {
                 if (adminSSOToken == null) {
                     adminSSOToken = (SSOToken) AccessController.doPrivileged(
                             com.sun.identity.security.AdminTokenAction
                                     .getInstance());
                 }
                 token = adminSSOToken;
             }
         } else {
             // Check for delegation permission throws exception if
             // permission is denied
             getDelegationPermission(token, normalizedDN, modifyActionSet);
         }
 
         if (newEntry && attrSet != null) {
             smsObject.create(token, dn, attrSet);
             // send object change notification
             notifyObjectChanged(dn, SMSObjectListener.ADD);
         } else if (modSet != null) {
             smsObject.modify(token, dn, (ModificationItem[]) (modSet
                     .toArray(new ModificationItem[modSet.size()])));
             // send object change notification
             notifyObjectChanged(dn, SMSObjectListener.MODIFY);
         } else {
             // %%% throw an exception, since nothing has changed
         }
         newEntry = false;
     }
 
     /**
      * Delete the entry in the datastore. This will delete sub-entries also!
      */
     void delete() throws SMSException, SSOException {
         if (readOnly) {
             if (debug.warningEnabled()) {
                 debug.warning("SMSEntry: Attempted to delete an entry that "
                         + "is marked as read-only: " + dn);
             }
             throw (new SMSException(SMSException.STATUS_NO_PERMISSION,
                     "sms-INSUFFICIENT_ACCESS_RIGHTS"));
         }
         delete(ssoToken);
     }
 
     /**
      * Delete the entry in the directory. This will delete sub-entries also!
      */
     void delete(SSOToken token) throws SMSException, SSOException {
         if (!newEntry) {
             // If backend has proxy enabled, check for delegation
             // permissions and use admin token
             if (backendProxyEnabled) {
                 if (isAllowed(token, normalizedDN, modifyActionSet)) {
                     if (adminSSOToken == null) {
                         adminSSOToken = (SSOToken) 
                                 AccessController.doPrivileged(
                                     com.sun.identity.security.AdminTokenAction
                                         .getInstance());
                     }
                     token = adminSSOToken;
                 }
             } else {
                 // Check for delegation permission throws exception if
                 // permission is denied
                 getDelegationPermission(token, normalizedDN, modifyActionSet);
             }
             smsObject.delete(token, dn);
             newEntry = true;
             attrSet = null; // new HashMap();
             modSet = null;
             // send object change notification
             notifyObjectChanged(dn, SMSObjectListener.DELETE);
         } else {
             if (debug.warningEnabled()) {
                 debug.warning("SMSEntry: Attempted to delete an entry that "
                         + "does not exist: " + dn);
             }
         }
     }
 
     /**
      * Returns the subOrgNames. Returns a set of suborganization names (rdns).
      * The paramter <code>numOfEntries</code> identifies the number of entries
      * to return, if <code>0</code> returns all the entries. The paramter
      * <code>recursive</code> determines if to return one level of entries
      * beneath the entryDN or all the entries till the leaf node.
      */
 
     Set searchSubOrgNames(SSOToken token, String filter, int numOfEntries,
             boolean sortResults, boolean ascendingOrder, boolean recursive)
             throws SMSException, SSOException {
 
         Set resultSet = smsObject.searchSubOrgNames(token, dn, filter,
                 numOfEntries, sortResults, ascendingOrder, recursive);
 
         // Check for read permissions
         Set allowedSet = new OrderedSet();
         for (Iterator items = resultSet.iterator(); items.hasNext();) {
             String item = (String) items.next();
             if (hasReadPermission(token, item)) {
                 allowedSet.add(item);
             }
         }
 
         Set answer = parseResult(allowedSet, normalizedDN);
         if (debug.messageEnabled()) {
             debug.message("SMSEntry: Successfully obtained "
                     + "suborganization names for : " + dn);
         }
         return (answer);
     }
 
     /**
      * Returns the sub-entries. Returns a set of sub-entry names (rdns). The
      * paramter <code>numOfEntries</code> identifies the number of entries to
      * return, if <code>0</code> returns all the entries.
      */
     Set subEntries(String filter, int numOfEntries, boolean sortResults,
             boolean ascendingOrder) throws SMSException, SSOException {
         return (subEntries(ssoToken, filter, numOfEntries, sortResults,
                 ascendingOrder));
     }
 
     Set subEntries(SSOToken token, String filter, int numOfEntries,
             boolean sortResults, boolean ascendingOrder) throws SMSException,
             SSOException {
         Set subEntries = smsObject.subEntries(token, dn, filter, numOfEntries,
                 sortResults, ascendingOrder);
         // Need to check if the user has permissions before returning
         Set answer = new OrderedSet();
         for (Iterator items = subEntries.iterator(); items.hasNext();) {
             String subEntry = (String) items.next();
             if (hasReadPermission(token, "ou=" + subEntry + "," + dn)) {
                 answer.add(subEntry);
             }
         }
         return (answer);
     }
 
     Set schemaSubEntries(SSOToken token, String filter, String sidFilter,
             int numOfEntries, boolean sortResults, boolean ascendingOrder)
             throws SMSException, SSOException {
         Set subEntries = smsObject.schemaSubEntries(token, dn, filter,
                 sidFilter, numOfEntries, sortResults, ascendingOrder);
         // Need to check if the user has permissions before returning
         Set answer = new OrderedSet();
         for (Iterator items = subEntries.iterator(); items.hasNext();) {
             String subEntry = (String) items.next();
             if (hasReadPermission(token, "ou=" + subEntry + "," + dn)) {
                 answer.add(subEntry);
             }
         }
         return (answer);
     }
 
     /**
      * Returns the Orgnization Names. Returns a set of organization names. The
      * paramter <code>numOfEntries</code> identifies the number of entries to
      * return, if <code>0</code> returns all the entries. The paramter
      * <code>recursive</code> determines if to return one level of entries
      * beneath the entryDN or all the entries till the leaf node.
      */
 
     Set searchOrganizationNames(SSOToken token, int numOfEntries,
             boolean sortResults, boolean ascendingOrder, String serviceName,
             String attrName, Set values) throws SMSException, SSOException {
 
         Set resultSet = smsObject.searchOrganizationNames(token, dn,
                 numOfEntries, sortResults, ascendingOrder, serviceName,
                 attrName, values);
 
         // Check for read permissions
         Set allowedSet = new OrderedSet();
         for (Iterator items = resultSet.iterator(); items.hasNext();) {
             String item = (String) items.next();
             if (hasReadPermission(token, item)) {
                 allowedSet.add(item);
             }
         }
 
         if (attrName.equalsIgnoreCase(EXPORTEDARGS))
             return allowedSet;
 
         Set answer = parseResult(allowedSet, normalizedDN, true);
         if (debug.messageEnabled()) {
             debug.message("SMSEntry: Successfully obtained "
                     + "organization names for : " + dn);
         }
         return answer;
     }
 
     /**
      * Returns the DNs that match the filter. The search is performed from the
      * root suffix ie., DN. It searchs for SMS objects only.
      */
     static Set search(String filter) throws SMSException {
         try {
             return (smsObject.search(null, baseDN, filter));
         } catch (SSOException ssoe) {
             debug.error("SMSEntry: Search ERROR: " + filter, ssoe);
             throw new SMSException(bundle.getString("sms-error-in-searching"),
                     ssoe, "sms-error-in-searching");
         }
     }
 
     /**
      * Updates the attribute set from the provided SMSEntry
      */
     void refresh(SMSEntry e) {
         if (e.attrSet != null) {
             attrSet = SMSUtils.copyAttributes(e.attrSet);
         } else {
             attrSet = null;
         }
         newEntry = e.newEntry;
         modSet = null;
     }
 
     /**
      * Checks if the provided DN exists. Used by PolicyManager.
      */
     public static boolean checkIfEntryExists(String dn, SSOToken token) {
         try {
             return (smsObject.entryExists(token, dn));
         } catch (Exception e) {
             debug
                     .error(
                             "SMSEntry: Error in checking if entry exists: "
                                     + dn, e);
         }
         return (false);
     }
 
     /**
      * Register the callback handler
      */
     public static void registerCallbackHandler(SSOToken token,
             SMSObjectListener ocl) throws SMSException, SSOException {
         // Manage callback objects
         changeListeners.add(ocl);
         // Check if native datastore notifications need to
         // be enabled
         if (!ServiceManager.isRealmEnabled() || enableDataStoreNotification) {
             // For backward compatibility
             smsObject.registerCallbackHandler(token, ocl);
             if (eventDebug.messageEnabled()) {
                 eventDebug.message("SMSEntry:registerCallbackHander calling "
                         + "SMSObject for co-existence mode");
             }
         }
         eventDebug.message("SMSEntry:registerCallbackHander called");
     }
 
     /**
      * Notifications received for objects changed
      */
     public static void objectChanged(String name, int type) {
         // Since this method will be called only by remote
         // notifications, set the flag to be false
         objectChanged(name, type, false);
     }
 
     public static void objectChanged(String name, int type, boolean isLocal) {
         if (eventDebug.messageEnabled()) {
             eventDebug
                     .message("SMSEntry:objectChanged: " + name + " type: "
                             + type + " IsLocal: " + isLocal
                             + "\nNumber of callback objects: "
                             + changeListeners.size());
         }
         if (!isLocal) {
             // Check if notification has been sent locally
             if (localChanges.contains(name + type)) {
                 localChanges.remove(name + type);
                 if (eventDebug.messageEnabled()) {
                     eventDebug.message("SMSEntry:objectChanged: " + name
                             + " type: " + type + " IsLocal: " + isLocal
                             + "\nHas been delivered locally");
                 }
                 return;
             } else if (eventDebug.messageEnabled()) {
                 eventDebug.message("SMSEntry:objectChanged: " + name
                         + " type: " + type + " IsLocal: " + isLocal
                         + "\nHas NOT been delivered locally, will be notified");
             }
         } else {
             // Since it is local notification, add it to localChanges list
             // Also check the size to remove old notification DNs
             if (localChanges.size() > LOCAL_CHANGES_MAX_SIZE) {
                 // Remove the first element
                 localChanges.remove(0);
             }
             localChanges.add(name + type);
         }
         for (Iterator items = changeListeners.iterator(); items.hasNext();) {
             SMSObjectListener listener = (SMSObjectListener) items.next();
             listener.objectChanged(name, type);
         }
     }
 
     /**
      * Sends notifications to all servers
      */
     public static void notifyObjectChanged(String name, int type) {
         if (eventDebug.messageEnabled()) {
             eventDebug.message("SMSEntry:notifyObjectChanged: " + name
                     + " type: " + type + "\nCalling NotificationThread");
         }
         NotificationThread nt = new NotificationThread(name, type);
         if (WebtopNaming.isServerMode()) {
             // Start the thread if in server mode
             nt.start();
         } else {
             // Send notifications
             nt.run();
         }
     }
 
     /**
      * Returns the DN of the entity
      */
     String getDN() {
         return (dn);
     }
 
     /**
      * Returns the principal used to access the entry
      */
     Principal getPrincipal() {
         try {
             return (ssoToken.getPrincipal());
         } catch (SSOException ssoe) {
             return (null);
         }
     }
 
     /**
      * Returns the ssoToken used to access the entry
      */
     SSOToken getSSOToken() {
         return (ssoToken);
     }
 
     /**
      * Sets the SMSEntry to be read only. Can be changed only by explicitly
      * providing the principal name.
      */
     void setReadOnly() {
         readOnly = true;
     }
 
     /**
      * Returns <code>true</code> if the entry does not exist in the data store
      */
     boolean isNewEntry() {
         return (newEntry);
     }
 
     /**
      * Returns the SMSObject
      */
     public static SMSObject getSMSObject() {
         return (smsObject);
     }
 
     public static void validateToken(SSOToken token) throws SMSException {
         try {
             tm.validateToken(token);
         } catch (SSOException ssoe) {
             throw (new SMSException(ssoe, "sms-INVALID_SSO_TOKEN"));
         }
     }
 
     public Object clone() throws CloneNotSupportedException {
         SMSEntry answer = (SMSEntry) super.clone();
         answer.ssoToken = ssoToken;
         answer.dn = dn;
         answer.newEntry = newEntry;
         answer.modSet = null;
         if (attrSet != null) {
             answer.attrSet = SMSUtils.copyAttributes(attrSet);
         } else {
             answer.attrSet = null;
         }
         if (debug.messageEnabled()) {
             debug.message("SMSEntry being cloned: " + dn);
         }
         return (answer);
     }
 
     public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append("DN\t\t: " + dn + "\n");
         if (newEntry) {
             sb.append("\t(NEW Entry)");
         }
         sb.append("Attribute Set\t: " + attrSet + "\n");
         sb.append("Modifcation Set\t: " + modSet + "\n");
         return (sb.toString());
     }
 
     public static String getRootSuffix() {
         return baseDN;
     }
 
     /**
      * @return true if the given attribute's value is case sensitive.
      */
     public static boolean isAttributeCaseSensitive(String attrName) {
         return (mCaseSensitiveAttributes.contains(attrName));
     }
 
     /**
      * @return the service filter pattern string
      */
     public static String getFilterPatternService() {
         return SMSEntry.FILTER_PATTERN_SERVICE;
     }
 
     // Parse the DNs and return as "/" seperated, but does not
     // include the current DN
     protected static Set parseResult(Set resultSet, String dn) {
         return (parseResult(resultSet, dn, false));
     }
 
     // Parse the DNs and return as "/" seperated
     protected static Set parseResult(Set resultSet, String dn,
             boolean includeThisDN) {
         /*
          * Search results based on scope 'SCOPE_SUB' returns all entries beneath
          * the entry DN, and adding to the set. This piece of code iterates
          * through the list, gets the suborganization names, replaces the
          * namingattribute 'o' with '/' and reverses the result string to be
          * displayed in the slash '/' seperated format. eg., Search for suborgs
          * from 'o=coke' in o=fanta,o=pepsi,o=coke,ou=services,dc=iplanet,dc=com
          * will return the following set: [pepsi, pepsi/fanta]
          */
 
         Set answer = new OrderedSet();
         if (resultSet != null) {
             Iterator Iter = resultSet.iterator();
             while (Iter.hasNext()) {
                 DN sdn = new DN((String) Iter.next());
                 String rfcDN = sdn.toRFCString();
                 String rfcDNlc = rfcDN.toLowerCase();
                 if (!rfcDNlc.equals(baseDN)
                         && !rfcDNlc.startsWith(SUN_INTERNAL_REALM_PREFIX)) {
 
                     /*
                      * Need to include current DN for search operations.
                      * Required by AuthN to login to root organization
                      */
                     if (rfcDNlc.equals(dn)) {
                         if (includeThisDN) {
                             answer.add(SLASH_STR);
                         }
                         continue;
                     }
 
                     /*
                      * To handle such a case. eg., ou=policy1,
                      * ou=Policies,ou=default,ou=OrganizationConfig,
                      * ou=1.0,ou=iPlanetAMPolicyService,ou=services,
                      * o=engg,o=coke,ou=services,dc=iplanet,dc=com in which case
                      * we return "/Coke/engg"
                      */
                     String orgAttr = ServiceManager.isRealmEnabled() ? 
                             ORG_PLACEHOLDER_RDN
                             : OrgConfigViaAMSDK.getNamingAttrForOrg() + EQUALS;
                     if (debug.messageEnabled()) {
                         debug
                                 .message("SMSEntry:parseResult:orgAttr "
                                         + orgAttr);
                     }
                     int i = rfcDNlc.indexOf(orgAttr.toLowerCase());
                     if (i > 0) {
                         rfcDN = rfcDN.substring(i);
                     }
                     if (debug.messageEnabled()) {
                         debug.message("SMSEntry:parseResult:DNName " + dn);
                         debug.message("SMSEntry:parseResult:RFCDN " + rfcDN);
                     }
 
                     int indx = rfcDNlc.indexOf(dn);
                     if (indx < 0) {
                        indx = rfcDNlc.indexOf(baseDN);
                     }
                     String origStr = rfcDN.substring(0, indx - 1);
                     // If orgAttr is not null,replace with the org naming
                     // attribute which is defined for legacy mode.
                     // Replace orgAttr= to '/' and ',' to "" (or)
                     // Replace 'o=' to '/' and ',' to ""
                     origStr = DNMapper.replaceString(origStr, orgAttr,
                             SLASH_STR);
                     if (debug.messageEnabled()) {
                         debug.message("SMSEntry:parseResult:origStr1 "
                                 + origStr);
                     }
                     origStr = DNMapper.replaceString(origStr, SMSEntry.COMMA,
                             "");
                     if (debug.messageEnabled()) {
                         debug.message("SMSEntry:parseResult:origStr2 "
                                 + origStr);
                     }
 
                     // Logic here is to reverse the string from dn format to
                     // slash format.
                     String tmpStr = "";
                     StringBuffer sb = new StringBuffer();
                     while (origStr.length() != 0) {
                         int id = origStr.lastIndexOf(SLASH_STR);
                         if (id >= 0) {
                             sb.append(origStr.substring(id + 1)).append(
                                     SLASH_STR);
                             origStr = origStr.substring(0, id);
                         }
                     }
                     tmpStr = sb.toString();
 
                     /*
                      * To remove the ending slash '/'. eg., pepsi/fanta/ to be
                      * added as pepsi/fanta
                      */
                     if (tmpStr != null && tmpStr.length() > 0) {
                         answer.add(tmpStr.substring(0, tmpStr.length() - 1));
                     }
                 }
             }
         }
         return answer;
     }
 
     static String[] parseOrgDN(String dnName) {
         // Check in cache first.
         String[] answer = (String[]) cache.get(dnName);
         if (answer != null) {
             return (answer);
         }
 
         // Not in cache, parse the DN
         if (debug.messageEnabled()) {
             debug.message("SMSEntry:parseOrgDN:DNName " + dnName);
         }
         answer = new String[5];
         if (dnName == null || dnName.length() == 0) {
             // This is an invalid DN, return "*"s
             answer[0] = baseDN;
             answer[1] = "*";
             answer[2] = "*";
             answer[3] = "*";
             answer[4] = "*";
             return (answer);
         }
 
         /*
          * We assume "ou=services" seperate the organization name from the rest
          * of the service components Hence if the full DN is: ou=subconfig1,
          * ou=default,ou=OrganizationConfig,
          * ou=1.0,ou=iPlanetAMPolicyService,ou=services,
          * o=engg,o=coke,ou=services,dc=iplanet,dc=com the orgDN would be:
          * o=engg,o=coke,ou=services,dc=iplanet,dc=com And if the full DN is:
          * ou=subconfig1, ou=default, ou=OrganizationConfig, ou=1.0,
          * ou=iPlanetAMPolicyService,ou=services, dc=iplanet, dc=com the orgDN
          * would be: dc=iplanet,dc=com
          */
         String rfcDN = DNUtils.normalizeDN(dnName);
         String restOfDN = null;
 
         // Get the index to the first occurance of ",ou=services,"
         int oldStrIndex = rfcDN.indexOf(DELEGATION_SERVICES_RDN_WITH_COMMA);
         if (oldStrIndex == -1 || rfcDN.equals(DNMapper.serviceDN)) {
             // The DN must be for root suffix for realm mode
             // and orgname in the case of legacy mode.
             answer[0] = rfcDN;
             restOfDN = "";
         } else if (ServiceManager.isRealmEnabled()) {
             // In realm mode there will be 2 ou=services, except
             // for the root org and in Legacy mode, there will be
             // only one "ou=services" for the root org.
             // Hence remove baseDN and check if rfcDN contains realm name
             int baseDNIndex = rfcDN.indexOf(DNMapper.serviceDN);
             if (baseDNIndex == -1 || baseDNIndex == 0) {
                 // Invalid DN or base DN, return base DN as org
                 answer[0] = baseDN;
                 restOfDN = "";
             } else {
                 String dn1 = rfcDN.substring(0, baseDNIndex - 1);
                 if (dn1.indexOf(DELEGATION_SERVICES_RDN) == -1) {
                     // Since services node is not present, it
                     // must be root realm.
                     answer[0] = baseDN;
                     restOfDN = dn1;
                 } else {
                     // In Legacy mode, there will be only one "ou=services"
                     answer[0] = rfcDN.substring(oldStrIndex
                             + DELEGATION_SERVICES_RDN_WITH_COMMA_LEN);
                     restOfDN = rfcDN.substring(0, oldStrIndex);
                 }
             }
         } else {
             // In Legacy mode, there will be only one "ou=services"
             answer[0] = rfcDN.substring(oldStrIndex
                     + DELEGATION_SERVICES_RDN_WITH_COMMA_LEN);
             restOfDN = rfcDN.substring(0, oldStrIndex);
         }
 
         if (debug.messageEnabled()) {
             debug.message("SMSEntry:parseOrgDN: orgDN: " + answer[0]
                     + " restOfDN: " + restOfDN);
         }
 
         // Parse restOfDN to get servicename, version, type and subconfig
         String[] rdns = null;
         if (restOfDN.length() > 0) {
             rdns = new DN(restOfDN).explodeDN(true);
         }
         int size = (rdns == null) ? 0 : rdns.length;
 
         // store serviceName,version,configType,subConfigNames
         // from restOfDN which will be of the format:
         // ou=default,ou=globalconfig,ou=1.0,ou=iPlanetAMAdminConsoleService
         answer[4] = (size < 1) ? REALM_SERVICE : rdns[size - 1];
         answer[3] = (size < 2) ? "*" : rdns[size - 2];
         answer[2] = (size < 3) ? "*" : rdns[size - 3];
 
         // The subconfig names should be "/" separated and left to right
         if (size >= 4) {
             StringBuffer sbr = new StringBuffer();
             for (int i = size - 4; i >= 0; i--) {
                 sbr.append('/').append(rdns[i]);
             }
             answer[1] = sbr.toString();
         } else {
             answer[1] = "*";
         }
 
         // Add to cache
         cache.put(dnName, answer);
         return answer;
     }
 
     static boolean hasReadPermission(SSOToken token, String dn) {
         try {
             getDelegationPermission(token, dn, readActionSet);
         } catch (SMSException smse) {
             if (debug.messageEnabled()) {
                 try {
                     debug
                             .message("SMSEntry::hasReadPermission Denied user: "
                                     + token.getPrincipal().getName()
                                     + " for dn: " + dn);
                 } catch (SSOException ssoe) {
                     debug.message("SMSEntry::hasReadPermission Denied access"
                             + " for dn: " + dn + " Got SSOException", ssoe);
                 }
             }
             return (false);
         }
         if (debug.messageEnabled()) {
             try {
                 debug.message("SMSEntry::hasReadPermission Allowed user: "
                         + token.getPrincipal().getName() + " for dn: " + dn);
             } catch (SSOException ssoe) {
                 // This should not happen
                 debug.message("SMSEntry::hasReadPermission Allowed access"
                         + " for dn: " + dn + " Got SSOException", ssoe);
             }
         }
         return (true);
     }
 
     static boolean getDelegationPermission(SSOToken token, String dnName,
             Set actions) throws SMSException {
         // call this API in SMSEntry::write,read,delete... methods.
         // If true proceed writing and reading.
         boolean delPermFlag = true;
 
         /*
          * No delegation checked for the following : Allow them for all
          * operations. 1) Since client SDK uses JAXRPC, bypass delegation
          * permission check in client SDK. Else it would done twice once on the
          * client and another at the server. if SMSJAXRPCObject is used then
          * bypass delegation check.
          * 
          * 2) Allow the users in the special users set.This is configurable
          * using the following 2 properties in AMConfig.properties.
          * 'com.sun.identity.authentication.super.user'
          * 'com.sun.identity.authentication.special.users'
          * 
          * 3) Since service config/data resides only under the group node ie.,
          * ou=default node, bypass the delegation check for the nodes above
          * that. This is to avoid unnecessary parsing and delegation checking.
          * bypass dnNames : ou=services,dc=iplanet,dc=com dc=iplanet,dc=com
          * ou=GlobalConfig ou=OrganizationConfig ou=PluginConfig
          */
         if (SMSJAXRPCObjectFlg || backendProxyEnabled || dnName.equals(baseDN)
                 || dnName.equals(DNMapper.serviceDN)) {
             if (debug.messageEnabled()) {
                 debug.message("SMSEntry:getDelegationPermission :"
                         + "No delegation check needed for client sdk, "
                         + "db proxy enabled and for baseDNs: " + baseDN);
             }
             return delPermFlag;
         }
 
         // Check for special and admin users
         try {
             // Normalize the user name from the token and compare it
             // against the special user set and if equal returns true
             // allowing the user to perform all operations.
             // eg., if root_suffix is dn=iplanet, dn=com then the normalized
             // dn is dc=iplanet,dc=com.
             String tokenName = token.getPrincipal().getName();
             String normTok = DNUtils.normalizeDN(tokenName);
             if (normTok != null) {
                 if (specialUserSet.contains(normTok)) {
                     if (debug.messageEnabled()) {
                         debug.message("SMSEntry.getDelegationPermission : No " +
                                 "delegation check needed for special users."
                                         + normTok);
                     }
                     return delPermFlag;
                 }
             }
         } catch (SSOException se) {
             debug.error("SMSEntry.isAllowed : " + "Invalid Token: ", se);
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     "sms-INVALID_SSO_TOKEN"));
         }
 
         // If running in co-existence mode, return true if backend
         // has proxy enabled
         if (!ServiceManager.isConfigMigratedTo70()) {
             // Make sure Backend Datastore Proxy is enabled
             if (!backendProxyEnabled) {
                 debug.error("SMSEntry::getDelegationPermission "
                         + "Must enable LDAP proxy support if configuration "
                         + "(DIT) is not migrated to AM 7.0");
                 // Throw permission denied exception
                 throw (new SMSException(SMSException.STATUS_NO_PERMISSION,
                         "sms-INSUFFICIENT_ACCESS_RIGHTS"));
             }
             // Since backend would check permissions, return true
             return (delPermFlag);
         }
 
         // Perform delgation check
         if (debug.messageEnabled()) {
             debug.message("SMSEntry:getDelegationPermission :"
                     + "Calling delegation service for dnName: " + dnName
                     + " for permissions: " + actions);
         }
 
         if (!isAllowedByDelegation(token, dnName, actions)) {
             throw (new SMSException(SMSException.STATUS_NO_PERMISSION,
                     "sms-INSUFFICIENT_ACCESS_RIGHTS"));
         }
         return (delPermFlag);
 
     }
 
     private static boolean isAllowed(SSOToken token, String dnName, Set actions)
             throws SMSException {
         // If JAXRPC return false
         if (SMSJAXRPCObjectFlg) {
             return (false);
         }
 
         // Return true for base DN and base services node
         if (dnName.equals(baseDN) || dnName.equals(DNMapper.serviceDN)) {
             return (true);
         }
 
         // Check for special and admin users
         try {
             // Normalize the user name from the token and compare it
             // against the special user set and if equal returns true
             // allowing the user to perform all operations.
             // eg., if root_suffix is dn=iplanet, dn=com then the normalized
             // dn is dc=iplanet,dc=com.
             String tokenName = token.getPrincipal().getName();
             if (DN.isDN(tokenName)) {
                 String normTok = (new DN(tokenName)).toRFCString()
                         .toLowerCase();
                 if (specialUserSet.contains(normTok)) {
                     if (debug.messageEnabled()) {
                         debug.message("SMSEntry.isAllowed :No delegation " +
                                 "check needed for special users."
                                         + normTok);
                     }
                     return (true);
                 }
             }
         } catch (SSOException se) {
             debug.error("SMSEntry.isAllowed : " + "Invalid Token: ", se);
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     "sms-INVALID_SSO_TOKEN"));
         }
 
         // If running in co-existence mode, return false since
         // delegation service would not have been initialized
         if (!ServiceManager.isConfigMigratedTo70()) {
             return (false);
         }
 
         return (isAllowedByDelegation(token, dnName, actions));
     }
 
     private static boolean isAllowedByDelegation(SSOToken token, String dnName,
             Set actions) throws SMSException {
         boolean delPermFlag = true;
 
         // Parse the DN
         String[] parseTokens = parseOrgDN(dnName);
         String orgName = parseTokens[0];
         String subConfigName = parseTokens[1];
         String configType = parseTokens[2];
         String version = parseTokens[3];
         String serviceName = parseTokens[4];
 
         // Ignore permission checks for DN that donot have config type
         // and subConfigName, except for sunAMRealmService
         if (!serviceName.equals(REALM_SERVICE)
                 && (configType.equalsIgnoreCase("*") || subConfigName
                         .equalsIgnoreCase("*"))) {
             return (delPermFlag);
         }
 
         try {
             // get orgName,serviceName,subConfigName from the parsed result.
             // Call DelegatedPermission's constructor
             DelegationPermission dlgPerm = new DelegationPermission(orgName,
                     serviceName, version, configType, subConfigName, actions,
                     Collections.EMPTY_MAP);
 
             // If DelegationEvaluator is null, initialize it
             if (dlgEval == null) {
                 dlgEval = new DelegationEvaluator();
             }
 
             // Perform delegation check
             delPermFlag = dlgEval.isAllowed(token, dlgPerm,
                     Collections.EMPTY_MAP);
             if (!delPermFlag) {
                 // Debug the message
                 if (debug.warningEnabled()) {
                     try {
                         debug.warning("SMSEntry: Attempt by:  "
                                 + token.getPrincipal().getName()
                                 + " to read/modify entry: " + dnName
                                 + " has no permissions");
                     } catch (SSOException ssoe) {
                         debug.warning("SMSEntry: Attempted to:  "
                                 + "read/modify an entry that has invalid "
                                 + "delegation privilege: " + dnName, ssoe);
                     }
 
                 }
             }
         } catch (SSOException se) {
             debug.error("SMSEntry.isAllowed : " + "Invalid Token: ", se);
             throw (new SMSException(bundle.getString("sms-INVALID_SSO_TOKEN"),
                     "sms-INVALID_SSO_TOKEN"));
         } catch (DelegationException de) {
             debug.error("SMSEntry.isAllowed : "
                     + "Invalid DelegationPermission: ", de);
             throw (new SMSException(bundle
                     .getString("sms-invalid_delegation_privilege"),
                     "sms-invalid_delegation_privilege"));
         }
         return delPermFlag;
     }
 
     // Instance variables
     private SSOToken ssoToken;
 
     protected String dn;
 
     protected String normalizedDN;
 
     private boolean newEntry;
 
     private boolean readOnly;
 
     private Map attrSet;
 
     private Set modSet;
 
     // Static global variables
 
     // Attributes with defined positions
     public static final String ORGANIZATION_RDN = "o";
 
     public static final String DC_RDN = "dc";
 
     // Constructs for placeholder nodes
     public static final String EQUALS = "=";
 
     public static final String DEFAULT_RDN = PLACEHOLDER_RDN + EQUALS
             + SMSUtils.DEFAULT;
 
     static final String ORG_PLACEHOLDER_RDN = ORGANIZATION_RDN + EQUALS;
 
     static final String DELEGATION_SERVICES_RDN = PLACEHOLDER_RDN + EQUALS
             + SERVICES_NODE + COMMA;
 
     static final String DELEGATION_SERVICES_RDN_WITH_COMMA = COMMA
             + PLACEHOLDER_RDN + EQUALS + SERVICES_NODE + COMMA;
 
     static final int DELEGATION_SERVICES_RDN_WITH_COMMA_LEN = 
         DELEGATION_SERVICES_RDN_WITH_COMMA.length();
 
     // Types of SMS objects
     static final int ORG_UNIT_OBJECT = 1;
 
     static final int SERVICE_OBJECT = 2;
 
     static final int SERVICE_COMP_OBJECT = 3;
 
     // Pre-defined SMS ATTRIBUTE names
     public static final String ATTR_SCHEMA = "sunServiceSchema";
 
     public static final String ATTR_PLUGIN_SCHEMA = "sunPluginSchema";
 
     public static final String ATTR_KEYVAL = "sunKeyValue";
 
     public static final String ATTR_XML_KEYVAL = "sunxmlKeyValue";
 
     public static final String ATTR_OBJECTCLASS = "objectclass";
 
     public static final String ATTR_PRIORITY = "sunsmspriority";
 
     public static final String ATTR_SERVICE_ID = "sunserviceID";
 
     public static final String ATTR_LABELED_URI = "labeleduri";
 
     public static final String ATTR_MODIFY_TIMESTAMP = "modifytimestamp";
 
     public static final String[] SMS_ATTRIBUTES = { PLACEHOLDER_RDN,
             ATTR_SCHEMA, ATTR_PLUGIN_SCHEMA, ATTR_KEYVAL, ATTR_XML_KEYVAL,
             ATTR_OBJECTCLASS, ATTR_PRIORITY, ATTR_SERVICE_ID, ATTR_LABELED_URI,
             ATTR_MODIFY_TIMESTAMP };
 
     // Object classes to identify the objects
     public static final String OC_TOP = "top";
 
     public static final String OC_ORG_UNIT = "organizationalunit";
 
     public static final String OC_SERVICE = "sunService";
 
     public static final String OC_REALM_SERVICE = "sunRealmService";
 
     public static final String OC_SERVICE_COMP = "sunServiceComponent";
 
     // Internal hidden realms used for storing delegation policies
     public static final String SUN_INTERNAL_REALM_NAME = "sunamhiddenrealm";
 
     public static final String SUN_INTERNAL_REALM_PREFIX = ORGANIZATION_RDN
             + EQUALS + SUN_INTERNAL_REALM_NAME;
 
     // Service name for Realm management used for delegation
     public static final String REALM_SERVICE = "sunAMRealmService";
 
     // Pre-defined filters
     protected static final String FILTER_PATTERN_ALL = "(&(&(objectclass="
             + OC_TOP + ")(" + PLACEHOLDER_RDN + "={0}))" + "(&(objectclass="
             + OC_TOP + ")(" + ATTR_SERVICE_ID + "={1})))";
 
     protected static final String FILTER_PATTERN = "(&(objectclass=" + OC_TOP
             + ")(" + PLACEHOLDER_RDN + "={0}))";
 
     protected static final String FILTER_PATTERN_SERVICE = "(&(objectclass="
             + OC_SERVICE + ")(" + PLACEHOLDER_RDN + "={0})(" + PLACEHOLDER_RDN
             + "={1}))";
 
     public static final String FILTER_SERVICE_COMPONENTS = "(|(objectclass="
             + OC_SERVICE + ")(objectclass=" + OC_SERVICE_COMP + "))";
 
     // Notification thread
     static class NotificationThread extends Thread {
         String name;
 
         int type;
 
         NotificationThread(String name, int type) {
             this.name = name;
             this.type = type;
             // Setting this as a daemon thread causes
             // amadmin CLI to not send notifications.
             // Would be true in case of all application using AMSDK.
             // setDaemon(true);
         }
 
         public void run() {
             // At install time and creation of placeholder nodes
             // should not send notifications
             // This would be determined if type == ADD and the DNs are
             // 1) ou=<serviceName>,<rootSuffix>
             // 2) ou=globalconfig
             // 3) ou=organizationconfig
             // 4) ou=instances
             // 5) ou=services
             // 6) ...
             String tmpName = name.toLowerCase();
             if (type == SMSObjectListener.ADD
                     && ((new StringTokenizer(name, ",")).countTokens() 
                             <= (baseDNCount + 1)
                             || tmpName.startsWith("ou=services,")
                             || tmpName.startsWith("ou=globalconfig,")
                             || tmpName.startsWith("ou=organizationconfig,")
                             || tmpName.startsWith("ou=instances,") || tmpName
                             .startsWith("ou=pluginconfig,"))) {
                 return;
             }
 
             // Send local notification only if datastore notification is
             // not enabled
             if (ServiceManager.isRealmEnabled() && !enableDataStoreNotification)
             {
                 if (eventDebug.messageEnabled()) {
                     eventDebug.message("SMSEntry::NotificationThread:run "
                             + "sending local notifications");
                 }
                 SMSEntry.objectChanged(name, type, true);
             }
 
             // If in server mode, send notifications to other servers
             if (!SMSJAXRPCObjectFlg && !enableDataStoreNotification
                     && ServiceManager.isRealmEnabled()) {
                 // Get servers from ServiceManager and send notifications
                 try {
                     Iterator sl = ServiceManager.getAMServerInstances()
                             .iterator();
                     while (sl != null && sl.hasNext()) {
                         URL url = new URL((String) sl.next());
                         URL weburl = WebtopNaming.getServiceURL("jaxrpc", url
                                 .getProtocol(), url.getHost(), Integer
                                 .toString(url.getPort()), false);
                         String surl = weburl.toString();
                         if (!surl.endsWith("/")) {
                             surl += "/SMSObjectIF";
                         } else {
                             surl += "SMSObjectIF";
                         }
                         try {
                             // Send notification
                             SOAPClient client = new SOAPClient();
                             client.setURL(surl);
                             Object[] params = new Object[2];
                             params[0] = name;
                             params[1] = new Integer(type);
                             if (eventDebug.messageEnabled()) {
                                 eventDebug.message(
                                         "SMSEntry:NotificationThread:run "
                                                 + "Sending to URL: " + surl);
                             }
                             client.send("notifyObjectChanged", params, null);
                         } catch (Throwable t) {
                             if (eventDebug.warningEnabled()) {
                                 eventDebug.warning(
                                         "SMSEntry:NotificationThread:: Unable "
                                                 + "to send notification to: "
                                                 + surl, t);
                             }
                         }
                     }
                 } catch (Throwable t) {
                     if (eventDebug.warningEnabled()) {
                         eventDebug.warning("SMSEntry:NotificationThread:: "
                                 + "Unable to send notifications", t);
                     }
                 }
             }
         }
     }
 }
