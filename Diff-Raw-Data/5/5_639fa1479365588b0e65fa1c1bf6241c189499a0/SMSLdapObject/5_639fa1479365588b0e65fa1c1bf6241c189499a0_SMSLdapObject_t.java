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
 * $Id: SMSLdapObject.java,v 1.12 2007-11-08 06:17:21 goodearth Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.sm.ldap;
 
 import java.security.Principal;
 import java.text.MessageFormat;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.Set;
 
 import javax.naming.NamingEnumeration;
 import javax.naming.NamingException;
 import javax.naming.directory.Attribute;
 import javax.naming.directory.DirContext;
 import javax.naming.directory.ModificationItem;
 
 import netscape.ldap.LDAPAttribute;
 import netscape.ldap.LDAPAttributeSet;
 import netscape.ldap.LDAPCompareAttrNames;
 import netscape.ldap.LDAPConnection;
 import netscape.ldap.LDAPEntry;
 import netscape.ldap.LDAPException;
 import netscape.ldap.LDAPModification;
 import netscape.ldap.LDAPModificationSet;
 import netscape.ldap.LDAPSearchConstraints;
 import netscape.ldap.LDAPSearchResults;
 import netscape.ldap.LDAPDN;
 import netscape.ldap.util.DN;
 
 import com.sun.identity.shared.locale.AMResourceBundleCache;
 import com.sun.identity.shared.debug.Debug;
 import com.sun.identity.shared.datastruct.OrderedSet;
 import com.iplanet.am.util.SystemProperties;
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.iplanet.ums.DataLayer;
 import com.iplanet.ums.IUMSConstants;
 import com.sun.identity.common.CaseInsensitiveHashMap;
 import com.sun.identity.sm.SMSEntry;
 import com.sun.identity.sm.SMSException;
 import com.sun.identity.sm.SMSObjectDB;
 import com.sun.identity.sm.SMSObjectListener;
 
 /**
  * This object represents an LDAP entry in the directory server. The UMS have an
  * equivalent class called PersistentObject. The SMS could not integrate with
  * PersistentObject, because of the its dependecy on the Session object. This
  * would mean that, to instantiate an PersistentObject inside SMS, we need to
  * create an UMS instance, which would be having directory parameters of SMS.
  * <p>
  * This class is used both to read and write information into the directory
  * server. The appropriate constructors discusses it is done.
  * <p>
  * There can be only three types of SMS entries in the directory (i) entry with
  * organizationUnit object class (attribute: ou) (ii) entry with sunService
 * object class (attributes: ou, labeledURI, sunServiceSchema, sunPluginSchema,
  * and sunKeyValue (sunXMLKeyValue, in the future) (iii) entry with
  * sunServiceComponent object class (attributes: ou, sunServiceID,
  * sunSMSPriority, sunKeyValue. All the schema, configuration and plugin entries
  * will be stored using the above entries.
  */
 public class SMSLdapObject extends SMSObjectDB implements SMSObjectListener {
 
     // LDAP specific & retry paramters
     static DataLayer dlayer;
 
     static SMDataLayer smdlayer;
 
     static int connNumRetry = 3;
 
     static int connRetryInterval = 1000;
 
     static HashSet retryErrorCodes = new HashSet();
 
     static Set entriesPresent = new HashSet();
 
     static Set entriesNotPresent = new HashSet();
 
     // Other parameters
     static ResourceBundle bundle;
 
     static boolean initialized;
 
     static boolean initializedForNotification;
 
     static Debug debug;
 
     static String[] OU_ATTR = new String[1];
 
     static String[] O_ATTR = new String[1];
 
     static boolean enableProxy;
 
     /**
      * Public constructor for SMSLdapObject
      */
     public SMSLdapObject() throws SMSException {
         // Check if initialized (should be called only once by SMSEntry)
         if (!initialized)
             initialize();
     }
 
     /**
      * Synchronized initialized method
      */
     private synchronized void initialize() throws SMSException {
         if (initialized)
             return;
 
         // Obtain the I18N resource bundle & Debug
         debug = Debug.getInstance("amSMSLdap");
         AMResourceBundleCache amCache = AMResourceBundleCache.getInstance();
         bundle = amCache.getResBundle(IUMSConstants.UMS_BUNDLE_NAME,
                 java.util.Locale.ENGLISH);
         OU_ATTR[0] = getNamingAttribute();
         O_ATTR[0] = getOrgNamingAttribute();
 
         String enableP = SystemProperties.get(SMSEntry.DB_PROXY_ENABLE);
         enableProxy = (enableP != null) && enableP.equalsIgnoreCase("true");
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject: proxy enable value: " + enableProxy);
         }
 
         try {
             if (enableProxy) {
                 // Get UMS datalayer
                 dlayer = DataLayer.getInstance();
                 
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject: DataLayer instance " 
                             + "obtained.");
                 }
             } else {
                 // Get SM datalayer
                 smdlayer = SMDataLayer.getInstance();
                 
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject: SMDataLayer instance " 
                             + "obtained.");
                 }               
             }
              if ((dlayer == null) && (smdlayer == null)) {
                 debug.error("SMSLdapObject: Unable to initialize LDAP");
                 throw (new SMSException(IUMSConstants.UMS_BUNDLE_NAME,
                         IUMSConstants.CONFIG_MGR_ERROR, null));
             }
             debug.message("SMSLdapObject: LDAP Initialized successfully");
 
             // Get connection retry parameters
             connNumRetry = DataLayer.getConnNumRetry();
             connRetryInterval = DataLayer.getConnRetryInterval();
             retryErrorCodes = DataLayer.getRetryErrorCodes();
 
             // Need to check if the root nodes exists. If not, create them
             String serviceDN = 
                 SMSEntry.SERVICES_RDN + SMSEntry.COMMA + getRootSuffix();
             if (!entryExists(serviceDN)) {
                 Map attrs = new HashMap();
                 Set attrValues = new HashSet();
                 attrValues.add(SMSEntry.OC_TOP);
                 attrValues.add(SMSEntry.OC_ORG_UNIT);
                 attrs.put(SMSEntry.ATTR_OBJECTCLASS, attrValues);
                 create(LDAPEventManager.adminPrincipal, serviceDN, attrs);
             }
         } catch (Exception e) {
             // Unable to initialize (trouble!!)
             debug.error("SMSEntry: Unable to initalize(exception):", e);
             throw (new SMSException(IUMSConstants.UMS_BUNDLE_NAME,
                     IUMSConstants.CONFIG_MGR_ERROR, null));
         }
         initialized = true;
     }
 
     /**
      * Reads in the object from persistent store, assuming that the guid and the
      * SSOToken are valid
      */
     public Map read(SSOToken token, String dn) throws SMSException,
             SSOException {
         if (dn == null || dn.length() == 0 ) {
             // This must not be possible return an exception.
             debug.error("SMSLdapObject: read():Null or Empty DN=" + dn);
             throw (new SMSException(new LDAPException(bundle
                 .getString(IUMSConstants.SMS_INVALID_DN)
                     + dn, LDAPException.NO_SUCH_OBJECT), "sms-NO_SUCH_OBJECT"));
         }
         
        
         if (!DN.isDN(dn)) {
             debug.warning("SMSLdapObject: Invalid DN=" + dn);
             String[] args = {dn};
             throw new SMSException(IUMSConstants.UMS_BUNDLE_NAME,
                 "sms-INVALID_DN", args);
         }
 
         // Check if entry does not exist
         if (entriesNotPresent.contains(dn)) {
             // Check if registered for changes to objects
             if (!initializedForNotification) {
                 SMSEntry.registerCallbackHandler(null, this);
                 initializedForNotification = true;
             }
             if (debug.messageEnabled()) {
                 debug.message("SMSLdapObject:read Entry not present: " + dn
                         + " (checked in cached)");
             }
             return (null);
         }
 
         LDAPAttributeSet attrSet = null;
         LDAPConnection conn = getConnection(token.getPrincipal());
         int errorCode = 0;
         try {
             LDAPEntry ldapEntry = null;
             int retry = 0;
             while (retry <= connNumRetry) {
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject.read() retry: " + retry);
                 }
                 try {
                     ldapEntry = conn.read(getNormalizedName(token, dn),
                         getAttributeNames());
                     break;
                 } catch (LDAPException e) {
                     errorCode = e.getLDAPResultCode();
                     if (!retryErrorCodes.contains("" + e.getLDAPResultCode())
                             || retry == connNumRetry) {
                         throw e;
                     }
                     retry++;
                     try {
                         Thread.sleep(connRetryInterval);
                     } catch (InterruptedException ex) {
                     }
                 }
             }
 
             if (ldapEntry == null) {
                 if (debug.warningEnabled()) {
                     debug.warning("SMSLdapObject: insufficient access rights "
                             + "to access DN=" + dn);
                 }
                 throw (new SMSException(IUMSConstants.UMS_BUNDLE_NAME,
                         IUMSConstants.SMS_INSUFFICIENT_ACCESS_RIGHTS, null));
             }
             attrSet = ldapEntry.getAttributeSet();
             if (debug.messageEnabled()) {
                 debug.message("SMSLdapObject: reading entry: " + dn);
             }
         } catch (LDAPException ex) {
             errorCode = ex.getLDAPResultCode();
             // Check if the entry is not present
             if (ex.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT) {
                 // Add to not present Set
                 objectChanged(dn, DELETE);
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject: entry not present:" + dn);
                 }
             } else {
                 if (debug.warningEnabled()) {
                     debug.warning(
                         "SMSLdapObject.read: Error in accessing entry DN: " +
                             dn, ex);
                 }
                 throw (new SMSException(ex, "sms-entry-cannot-access"));
             }
         } finally {
             releaseConnection(conn, errorCode);
         }
 
         // Convert LDAPAttributeSet to Map
         Map answer = null;
         if (attrSet != null) {
             for (Enumeration enums = attrSet.getAttributes(); enums
                     .hasMoreElements();) {
                 LDAPAttribute attr = (LDAPAttribute) enums.nextElement();
                 String attrName = attr.getName();
                 if (attr != null) {
                     Set values = new HashSet();
                     String[] value = attr.getStringValueArray();
                     for (int i = 0; i < value.length; i++)
                         values.add(value[i]);
                     if (answer == null)
                         answer = new CaseInsensitiveHashMap(10);
                     answer.put(attrName, values);
                 }
             }
         }
         return (answer);
     }
 
     /**
      * Create an entry in the directory
      */
     public void create(SSOToken token, String dn, Map attrs)
             throws SMSException, SSOException {
         // Call the private method that takes the principal name
         create(token.getPrincipal(), getNormalizedName(token, dn), 
             attrs);
         // Update entryPresent cache
         objectChanged(dn, ADD);
     }
 
     /**
      * Create an entry in the directory using the principal name
      */
     private static void create(Principal p, String dn, Map attrs)
             throws SMSException, SSOException {
         LDAPConnection conn = getConnection(p);
         try {
             int retry = 0;
             while (retry <= connNumRetry) {
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject.create() retry: " + retry);
                 }
                 try {
                     LDAPAttributeSet attrSet = copyMapToAttrSet(attrs);
                     conn.add(new LDAPEntry(dn, attrSet));
                     break;
                 } catch (LDAPException e) {
                     if (!retryErrorCodes.contains("" + e.getLDAPResultCode())
                             || retry == connNumRetry) {
                         throw e;
                     }
                     retry++;
                     try {
                         Thread.sleep(connRetryInterval);
                     } catch (InterruptedException ex) {
                     }
                 }
             }
         } catch (LDAPException le) {
             debug.error("SMSLdapObject::create() Error in creating entry: "
                     + dn + "\nBy Principal: " + p.getName(), le);
             throw (new SMSException(le, "sms-entry-cannot-create"));
         } finally {
             releaseConnection(conn);
         }
 
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject: Successfully created entry: " + dn);
         }
     }
 
     /**
      * Save the entry using the token provided. The principal provided will be
      * used to get the proxy connection.
      */
     public void modify(SSOToken token, String dn, ModificationItem mods[])
             throws SMSException, SSOException {
 
         LDAPConnection conn = getConnection(token.getPrincipal());
         try {
             int retry = 0;
             while (retry <= connNumRetry) {
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject.modify() retry: " + retry);
                 }
                 try {
                     LDAPModificationSet modSet = copyModItemsToLDAPModSet(mods);
                     conn.modify(getNormalizedName(token, dn), modSet);
                     break;
                 } catch (LDAPException e) {
                     if (!retryErrorCodes.contains("" + e.getLDAPResultCode())
                             || retry == connNumRetry) {
                         throw e;
                     }
                     retry++;
                     try {
                         Thread.sleep(connRetryInterval);
                     } catch (InterruptedException ex) {
                     }
                 }
             }
         } catch (LDAPException le) {
             debug.error("SMSLdapObject::modify() Error in modifying entry: "
                     + dn + "\nBy Principal: " + token.getPrincipal().getName(),
                     le);
             throw (new SMSException(le, "sms-entry-cannot-modify"));
         } finally {
             releaseConnection(conn);
         }
 
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject: Successfully modified entry: " + dn);
         }
     }
 
     /**
      * Delete the entry in the directory. This will delete sub-entries also!
      */
     public void delete(SSOToken token, String dn) throws SMSException,
             SSOException {
         // Check if there are sub-entries, delete if present
         Iterator se = subEntries(token, dn, "*", 0, false, false).iterator();
         while (se.hasNext()) {
             String entry = (String) se.next();
             if (debug.messageEnabled()) {
                 debug.message("SMSLdapObject: deleting sub-entry: " + entry);
             }
             delete(token, getNamingAttribute() + "=" + entry + "," + dn);
         }
         // Check if there are suborganizations, delete if present
         // The recursive 'false' here has the scope SCOPE_ONE
         // while searching for the suborgs.
         // Loop through the suborg at the first level and if there
         // is no next suborg, delete that.
         Set subOrgNames = searchSubOrgNames(
             token, dn, "*", 0, false, false, false);
         
         for (Iterator so = subOrgNames.iterator(); so.hasNext(); ) {
             String subOrg = (String) so.next();
             if (debug.messageEnabled()) {
                 debug.message("SMSLdapObject: deleting suborganization: "
                         + subOrg);
             }
             delete(token, getNormalizedName(token, subOrg));
         }
 
         // Get LDAP connection
         LDAPConnection conn = getConnection(token.getPrincipal());
         try {
             delete(conn, getNormalizedName(token, dn));
         } finally {
             releaseConnection(conn);
         }
         // Update entriesPresent cache
         objectChanged(dn, DELETE);
     }
 
     private static void delete(LDAPConnection conn, String dn)
             throws SMSException {
         // Delete the entry
         try {
             int retry = 0;
             while (retry <= connNumRetry) {
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject.delete() retry: " + retry);
                 }
                 try {
                     conn.delete(dn);
                     break;
                 } catch (LDAPException e) {
                     if (!retryErrorCodes.contains("" + e.getLDAPResultCode())
                             || retry == connNumRetry) {
                         throw e;
                     }
                     retry++;
                     try {
                         Thread.sleep(connRetryInterval);
                     } catch (InterruptedException ex) {
                     }
                 }
             }
         } catch (LDAPException le) {
             if (debug.warningEnabled()) {
                 debug.warning("SMSLdapObject:delete() Unable to delete entry:"
                         + dn, le);
             }
             throw (new SMSException(le, "sms-entry-cannot-delete"));
         }
     }
 
     /**
      * Returns the sub-entry names. Returns a set of RDNs that are sub-entries.
      * The paramter <code>numOfEntries</code> identifies the number of entries
      * to return, if <code>0</code> returns all the entries.
      */
     public Set subEntries(SSOToken token, String dn, String filter,
             int numOfEntries, boolean sortResults, boolean ascendingOrder)
             throws SMSException, SSOException {
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject: SubEntries search: " + dn);
         }
 
         // Construct the filter
         String[] objs = { filter };
         String sfilter = MessageFormat.format(getSearchFilter(),(Object[])objs);
         Set answer = getSubEntries(token, dn, sfilter, numOfEntries,
                 sortResults, ascendingOrder);
         return (answer);
     }
 
     private Set getSubEntries(SSOToken token, String dn, String filter,
             int numOfEntries, boolean sortResults, boolean ascendingOrder)
             throws SMSException, SSOException {
 
         LDAPConnection conn = getConnection(token.getPrincipal());
         // Setup the search constraints
         LDAPSearchConstraints constraints = conn.getSearchConstraints();
         constraints.setMaxResults(numOfEntries);
         constraints.setServerTimeLimit(0);
 
         LDAPSearchResults results = null;
         try {
             int retry = 0;
             while (retry <= connNumRetry) {
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject.subEntries() retry: " + retry);
                 }
                 try {
                     // Get the sub entries
                     results = conn.search(getNormalizedName(token, dn),
                         LDAPConnection.SCOPE_ONE, filter, OU_ATTR, false, 
                         constraints);
                     // Check if the results have to sorted
                     if (sortResults) {
                         LDAPCompareAttrNames comparator = 
                             new LDAPCompareAttrNames(
                                 getNamingAttribute(), ascendingOrder);
                         results.sort(comparator);
                     }
                     break;
                 } catch (LDAPException e) {
                     if (!retryErrorCodes.contains("" + e.getLDAPResultCode())
                             || retry == connNumRetry) {
                         throw e;
                     }
                     retry++;
                     try {
                         Thread.sleep(connRetryInterval);
                     } catch (InterruptedException ex) {
                     }
                 }
             }
         } catch (LDAPException le) {
             if (le.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT) {
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject: entry not present:" + dn);
                 }
             } else {
                 if (debug.warningEnabled()) {
                     debug.warning("SMSLdapObject: Unable to search for "
                             + "sub-entries: " + dn, le);
                 }
                 throw (new SMSException(le, "sms-entry-cannot-search"));
             }
         } finally {
             releaseConnection(conn);
         }
 
         // Construct the results and return
         LDAPEntry entry;
         Set answer = new OrderedSet();
         while (results != null && results.hasMoreElements()) {
             try {
                 entry = results.next();
             } catch (LDAPException ldape) {
                 if (debug.warningEnabled()) {
                     debug.warning("SMSLdapObject: Error in obtaining "
                             + "sub-entries: " + dn, ldape);
                 }
                 throw (new SMSException(ldape, "sms-entry-cannot-obtain"));
             }
             
             String temp = LDAPDN.explodeDN(entry.getDN(), true)[0]; 
             answer.add(getDenormalizedName(token, temp));
         }
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject: Successfully obtained "
                     + "sub-entries for : " + dn);
         }
         return (answer);
     }
 
     /**
      * Returns the sub-entry names. Returns a set of RDNs that are sub-entries.
      * The paramter <code>numOfEntries</code> identifies the number of entries
      * to return, if <code>0</code> returns all the entries.
      */
     public Set schemaSubEntries(SSOToken token, String dn, String filter,
             String sidFilter, int numOfEntries, boolean sortResults,
             boolean ascendingOrder) throws SMSException, SSOException {
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject: schemaSubEntries search: " + dn);
         }
         
         // Construct the filter
         String[] objs = { filter, sidFilter };
         String sfilter = MessageFormat.format(
             getServiceIdSearchFilter(), (Object[])objs);
         Set answer = getSubEntries(token, dn, sfilter, numOfEntries,
                 sortResults, ascendingOrder);
         return (answer);
     }
 
     public String toString() {
         return ("SMSLdapObject");
     }
 
     /**
      * Releases a LDAPConnection.
      */
     private static void releaseConnection(LDAPConnection conn) {
         if (conn != null) {
             if (enableProxy) {
                 dlayer.releaseConnection(conn);
             } else {
                 smdlayer.releaseConnection(conn);
             }
         }
     }
 
     /**
      * Releases a LDAPConnection.
      */
     private static void releaseConnection(LDAPConnection conn, int errorCode){
 
         if (conn != null) {
             if (enableProxy) {
               dlayer.releaseConnection(conn, errorCode);
             } else {
               smdlayer.releaseConnection(conn);
             }
         }
     }
 
     /**
      * Returns a LDAPConnection for the given principal
      */
     private static LDAPConnection getConnection(Principal p)
             throws SMSException {
         LDAPConnection conn = null;
         if (enableProxy) {
             conn = dlayer.getConnection(p);
         } else {
             conn = smdlayer.getConnection();
         }
         if (conn == null) {
             debug.error("SMSLdapObject: Unable to get connection to LDAP "
                     + "server for the principal: " + p);
             throw (new SMSException(new LDAPException(bundle
                     .getString(IUMSConstants.SMS_SERVER_DOWN)),
                     "sms-SERVER_DOWN"));
         }
         return (conn);
     }
 
     /**
      * Returns LDAP entries that match the filter, using the start DN provided
      * in method
      */
     public Set search(SSOToken token, String startDN, String filter)
             throws SSOException, SMSException {
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject: search filter: " + filter);
         }
 
         LDAPConnection conn = getConnection(LDAPEventManager.adminPrincipal);
         LDAPSearchResults results = null;
         Set answer = new OrderedSet();
         try {
             // Setup the search constraints
             LDAPSearchConstraints constraints = conn.getSearchConstraints();
             constraints.setMaxResults(0);
             constraints.setServerTimeLimit(0);
 
             int retry = 0;
             while (retry <= connNumRetry) {
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject.search() retry: " + retry);
                 }
                 
                 try {
                     results = conn.search(getNormalizedName(token, startDN),
                         LDAPConnection.SCOPE_SUB,filter, null, false, 
                         constraints);
                     break;
                 } catch (LDAPException e) {
                     if (!retryErrorCodes.contains("" + e.getLDAPResultCode())
                             || retry == connNumRetry) {
                         throw e;
                     }
                     retry++;
                     try {
                         Thread.sleep(connRetryInterval);
                     } catch (InterruptedException ex) {
                     }
                 }
             }
 
         } catch (LDAPException le) {
             if (debug.warningEnabled()) {
                 debug.warning("SMSLdapObject: LDAP exception in search "
                     + "for filter match: " + filter, le);
             }
             throw (new SMSException(le, "sms-error-in-searching"));
         } finally {
             releaseConnection(conn);
         }
 
         // Convert LDAP results to DNs
         LDAPEntry entry;
         while (results.hasMoreElements()) {
             try {
                 entry = results.next();
             } catch (LDAPException ldape) {
                 if (debug.warningEnabled()) {
                     debug.warning("SMSLdapObject: Error in searching for "
                             + "filter match: " + filter, ldape);
                 }
                 throw (new SMSException(ldape, "sms-error-in-searching"));
             }
             answer.add(entry.getDN());
         }
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject::search returned successfully: "
                     + filter + "\n\tObjects: " + answer);
         }
         return (answer);
     }
 
     /**
      * Checks if the provided DN exists. Used by PolicyManager.
      */
 
     /**
      * Checks if the provided DN exists. Used by PolicyManager.
      */
     public boolean entryExists(SSOToken token, String dn) {
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject: checking if entry exists: " + dn);
         }
         
         // Check the caches
         if (entriesPresent.contains(dn)) {
             if (debug.messageEnabled()) {
                 debug.message("SMSLdapObject: entry present in cache: " + dn);
             }
             return (true);
         } else if (entriesNotPresent.contains(dn)) {
             if (debug.messageEnabled()) {
                 debug.message("SMSLdapObject: entry present in "
                         + "not-present-cache: " + dn);
             }
             return (false);
         }
 
         // Check if entry exisits
         boolean entryExists = entryExists(getNormalizedName(token, dn));
 
         // Update the cache
         if (entryExists) {
             Set ee = new HashSet(entriesPresent);
             ee.add(dn);
             entriesPresent = ee;
         } else {
             Set enp = new HashSet(entriesNotPresent);
             enp.add(dn);
             entriesNotPresent = enp;
         }
         return (entryExists);
     }
 
     /**
      * Checks if the provided DN exists.
      */
     private static boolean entryExists(String dn) {
         boolean entryExists = false;
         LDAPConnection conn = null;
         try {
             // Use the Admin Principal to check if entry exists
             conn = getConnection(LDAPEventManager.adminPrincipal);
             conn.read(dn, OU_ATTR);
             entryExists = true;
         } catch (LDAPException e) {
             if (debug.warningEnabled()) {
                 debug.warning("SMSLdapObject:entryExists: " + dn
                         + "does not exist");
             }
         } catch (SMSException ssoe) {
             if (debug.warningEnabled()) {
                 debug.warning("SMSLdapObject: SMSException while "
                         + " checking for entry: " + dn, ssoe);
             }
         } finally {
             releaseConnection(conn);
         }
         return (entryExists);
     }
 
     /**
      * Registration of Notification Callbacks
      */
     public String registerCallbackHandler(SSOToken token,
             SMSObjectListener changeListener) throws SMSException, SSOException 
     {
         return (LDAPEventManager.addObjectChangeListener(changeListener));
     }
 
     /**
      * De-Registration of Notification Callbacks
      */
     public void deregisterCallbackHandler(String id) {
         LDAPEventManager.removeObjectChangeListener(id);
     }
 
     // Method to convert Map to LDAPAttributeSet
     private static LDAPAttributeSet copyMapToAttrSet(Map attrs) {
         LDAPAttribute[] ldapAttrs = new LDAPAttribute[attrs.size()];
         Iterator items = attrs.keySet().iterator();
         for (int i = 0; items.hasNext(); i++) {
             String attrName = (String) items.next();
             Set attrValues = (Set) attrs.get(attrName);
             ldapAttrs[i] = new LDAPAttribute(attrName, (String[]) attrValues
                     .toArray(new String[attrValues.size()]));
         }
         return (new LDAPAttributeSet(ldapAttrs));
     }
 
     // Method to covert JNDI ModificationItems to LDAPModificationSet
     private static LDAPModificationSet copyModItemsToLDAPModSet(
             ModificationItem mods[]) throws SMSException {
         LDAPModificationSet modSet = new LDAPModificationSet();
         try {
             for (int i = 0; i < mods.length; i++) {
                 Attribute attribute = mods[i].getAttribute();
                 LDAPAttribute attr = new LDAPAttribute(attribute.getID());
                 for (NamingEnumeration ne = attribute.getAll(); ne.hasMore();) {
                     attr.addValue((String) ne.next());
                 }
                 switch (mods[i].getModificationOp()) {
                 case DirContext.ADD_ATTRIBUTE:
                     modSet.add(LDAPModification.ADD, attr);
                     break;
                 case DirContext.REPLACE_ATTRIBUTE:
                     modSet.add(LDAPModification.REPLACE, attr);
                     break;
                 case DirContext.REMOVE_ATTRIBUTE:
                     modSet.add(LDAPModification.DELETE, attr);
                     break;
                 }
             }
         } catch (NamingException nne) {
             throw (new SMSException(nne, 
                     "sms-cannot-copy-fromModItemToModSet"));
         }
         return (modSet);
     }
 
     public void objectChanged(String dn, int type) {
         dn = (new DN(dn)).toRFCString().toLowerCase();
         synchronized (entriesPresent) {
             if (type == DELETE) {
                 // Remove from entriesPresent Set
                 Set enp = new HashSet();
                 for (Iterator items = entriesPresent.iterator(); items
                         .hasNext();) {
                     String odn = (String) items.next();
                     if (!dn.equals((new DN(odn)).toRFCString().toLowerCase())) {
                         enp.add(odn);
                     }
                 }
                 entriesPresent = enp;
             } else if (type == ADD) {
                 // Remove from entriesNotPresent set
                 Set enp = new HashSet();
                 for (Iterator items = entriesNotPresent.iterator(); items
                         .hasNext();) {
                     String odn = (String) items.next();
                     if (!dn.equals((new DN(odn)).toRFCString().toLowerCase())) {
                         enp.add(odn);
                     }
                 }
                 entriesNotPresent = enp;
             }
         }
     }
 
     public void allObjectsChanged() {
         // Not clear why this class is implemeting the SMSObjectListener
         // interface.
         SMSEntry.debug.error(
             "SMSLDAPObject: got notifications, all objects changed");
         synchronized (entriesPresent) {
             entriesPresent.clear();
             entriesNotPresent.clear();
         }
     }
 
     /**
      * Returns the suborganization names. Returns a set of RDNs that are
      * suborganization name. The paramter <code>numOfEntries</code> identifies
      * the number of entries to return, if <code>0</code> returns all the
      * entries.
      */
     public Set searchSubOrgNames(SSOToken token, String dn, String filter,
             int numOfEntries, boolean sortResults, boolean ascendingOrder,
             boolean recursive) throws SMSException, SSOException {
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject.searchSubOrgNames search: " + dn);
         }
 
         /*
          * Instead of constructing the filter in the framework(SMSEntry.java),
          * Construct the filter here in SMSLdapObject or the plugin
          * implementation to support JDBC or other data store.
          */
         String[] objs = { filter };
 
         String FILTER_PATTERN_ORG = "(&(objectclass="
                 + SMSEntry.OC_REALM_SERVICE + ")(" + SMSEntry.ORGANIZATION_RDN
                 + "={0}))";
 
         String sfilter = MessageFormat.format(
             FILTER_PATTERN_ORG, (Object[])objs);
         Set answer = searchSubOrganizationNames(token, dn, sfilter,
                 numOfEntries, sortResults, ascendingOrder, recursive);
         return (answer);
     }
 
     private Set searchSubOrganizationNames(
         SSOToken token, 
         String dn,
         String filter,
         int numOfEntries,
         boolean sortResults,
         boolean ascendingOrder, 
         boolean recursive
     ) throws SMSException, SSOException {
         LDAPConnection conn = getConnection(token.getPrincipal());
         // Setup the search constraints
         LDAPSearchConstraints constraints = conn.getSearchConstraints();
         constraints.setMaxResults(numOfEntries);
         constraints.setServerTimeLimit(0);
 
         LDAPSearchResults results = null;
         int scope = LDAPConnection.SCOPE_ONE;
         try {
             int retry = 0;
             while (retry <= connNumRetry) {
                 if (debug.messageEnabled()) {
                     debug.message(
                         "SMSLdapObject.searchSubOrganizationNames() retry: " +
                         retry);
                 }
                 try {
                     // Get the suborganization names
                     if (recursive) {
                         scope = LDAPConnection.SCOPE_SUB;
                     }
                     results = conn.search(getNormalizedName(token, dn), 
                         scope, filter, O_ATTR, false, constraints);
 
                     // Check if the results have to be sorted
                     if (sortResults) {
                         LDAPCompareAttrNames comparator =
                             new LDAPCompareAttrNames(
                                 getOrgNamingAttribute(), ascendingOrder);
                         results.sort(comparator);
                     }
                     break;
                 } catch (LDAPException e) {
                     if (!retryErrorCodes.contains("" + e.getLDAPResultCode())
                             || retry == connNumRetry) {
                         throw e;
                     }
                     retry++;
                     try {
                         Thread.sleep(connRetryInterval);
                     } catch (InterruptedException ex) {
                     }
                 }
             }
         } catch (LDAPException le) {
             if (le.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT) {
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject: suborg not present:" + dn);
                 }
             } else {
                 if (debug.warningEnabled()) {
                     debug.warning("SMSLdapObject: Unable to search for "
                             + "suborganization names: " + dn, le);
                 }
                 throw (new SMSException(le, "sms-suborg-cannot-search"));
             }
         } finally {
             releaseConnection(conn);
         }
 
         // Construct the results and return
         LDAPEntry entry;
         Set answer = new OrderedSet();
 
         while (results != null && results.hasMoreElements()) {
             try {
                 entry = results.next();
             } catch (LDAPException ldape) {
                 if (debug.warningEnabled()) {
                     debug.warning("SMSLdapObject: Error in obtaining "
                             + "suborganization names: " + dn, ldape);
                 }
                 throw (new SMSException(ldape, "sms-suborg-cannot-obtain"));
             }
 
             String rdn = (entry.getDN()).toString();
             answer.add(rdn);
         }
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject: Successfully obtained "
                     + "suborganization names for : " + dn);
             debug.message("SMSLdapObject: Successfully obtained "
                     + "suborganization names  : " + answer.toString());
         }
         return (answer);
     }
 
     /**
      * Returns the organization names. Returns a set of RDNs that are
      * organization name. The paramter <code>numOfEntries</code> identifies
      * the number of entries to return, if <code>0</code> returns all the
      * entries.
      */
     public Set searchOrganizationNames(SSOToken token, String dn,
             int numOfEntries, boolean sortResults, boolean ascendingOrder,
             String serviceName, String attrName, Set values)
             throws SMSException, SSOException {
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject:searchOrganizationNames search dn: "
                     + dn);
         }
 
         /*
          * Instead of constructing the filter in the framework(SMSEntry.java),
          * Construct the filter here in SMSLdapObject or the plugin
          * implementation to support JDBC or other data store. To return
          * organization names that match the given attribute name and values,
          * only exact matching is supported, and if more than one value is
          * provided the organization must have all these values for the
          * attribute. Basically an AND is performed for attribute values for
          * searching. The attributes can be under the service config as well
          * under the Realm/Organization directly. For eg.,
          * (|(&(objectclass=sunRealmService)(&
          * (|(sunxmlkeyvalue=SERVICE_NAME-ATTR_NAME=VALUE1)
          * (sunxmlkeyvalue=ATTR_NAME=VALUE1))
          * (|(sunxmlkeyvalue=SERVICE_NAME-ATTR_NAME=VALUE2)
          * (sunxmlkeyvalue=ATTR_NAME=VALUE2))(...))
          * (&(objectclass=sunServiceComponent)(&
          * (|(sunxmlkeyvalue=SERVICE_NAME-ATTR_NAME=VALUE1)
          * (sunxmlkeyvalue=ATTR_NAME=VALUE1))
          * (|(sunxmlkeyvalue=SERVICE_NAME-ATTR_NAME=VALUE2)
          * (sunxmlkeyvalue=ATTR_NAME=VALUE2))(...))
          * 
          */
 
         StringBuffer sb = new StringBuffer();
         sb.append("(&");
         for (Iterator itr = values.iterator(); itr.hasNext();) {
             String val = (String) itr.next();
             sb.append("(|(").append(SMSEntry.ATTR_XML_KEYVAL).append("=")
                     .append(serviceName).append("-").append(attrName).append(
                             "=").append(val).append(")");
             sb.append("(").append(SMSEntry.ATTR_XML_KEYVAL).append("=").append(
                     attrName).append("=").append(val).append("))");
         }
         sb.append(")");
         String filter = sb.toString();
 
         String FILTER_PATTERN_SEARCH_ORG = "{0}";
         String dataStore = SMSEntry.getDataStore(token);
         if ((dataStore != null) && !dataStore.equals(
             SMSEntry.DATASTORE_ACTIVE_DIR)
         ) {
            // Include the OCs only for sunDS, not Active Directory.
            //String FILTER_PATTERN_SEARCH_ORG = "(|(&(objectclass="
            FILTER_PATTERN_SEARCH_ORG = "(|(&(objectclass="
                 + SMSEntry.OC_REALM_SERVICE + "){0})" + "(&(objectclass="
                 + SMSEntry.OC_SERVICE_COMP + "){0}))";
         }
 
         String[] objs = { filter };
         String sfilter = MessageFormat.format(
             FILTER_PATTERN_SEARCH_ORG, (Object[])objs);
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject:orgNames search filter: " + sfilter);
         }
         Set answer = getOrgNames(token, dn, sfilter, numOfEntries, sortResults,
                 ascendingOrder);
         return (answer);
     }
 
     private Set getOrgNames(SSOToken token, String dn, String filter,
             int numOfEntries, boolean sortResults, boolean ascendingOrder)
             throws SMSException, SSOException {
 
         LDAPConnection conn = getConnection(token.getPrincipal());
         // Setup the search constraints
         LDAPSearchConstraints constraints = conn.getSearchConstraints();
         constraints.setMaxResults(numOfEntries);
         constraints.setServerTimeLimit(0);
 
         LDAPSearchResults results = null;
         int scope = LDAPConnection.SCOPE_SUB;
         try {
             int retry = 0;
             while (retry <= connNumRetry) {
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject.getOrgNames() retry: "+ retry);
                 }
                 try {
                     // Get the organization names
                     results = conn.search(getNormalizedName(token, dn),
                         scope, filter, O_ATTR, false, constraints);
 
                     // Check if the results have to be sorted
                     if (sortResults) {
                         LDAPCompareAttrNames comparator = 
                             new LDAPCompareAttrNames(
                                 getOrgNamingAttribute(), ascendingOrder);
                         results.sort(comparator);
                     }
                     break;
                 } catch (LDAPException e) {
                     if (!retryErrorCodes.contains("" + e.getLDAPResultCode())
                             || retry == connNumRetry) {
                         throw e;
                     }
                     retry++;
                     try {
                         Thread.sleep(connRetryInterval);
                     } catch (InterruptedException ex) {
                     }
                 }
             }
         } catch (LDAPException le) {
             if (le.getLDAPResultCode() == LDAPException.NO_SUCH_OBJECT) {
                 if (debug.messageEnabled()) {
                     debug.message("SMSLdapObject: org not present:" + dn);
                 }
             } else {
                 if (debug.warningEnabled()) {
                     debug.warning("SMSLdapObject: Unable to search for "
                             + "organization names: " + dn, le);
                 }
                 throw (new SMSException(le, "sms-org-cannot-search"));
             }
         } finally {
             releaseConnection(conn);
         }
 
         // Construct the results and return
         LDAPEntry entry;
         Set answer = new OrderedSet();
 
         while (results != null && results.hasMoreElements()) {
             try {
                 entry = results.next();
             } catch (LDAPException ldape) {
                 if (debug.warningEnabled()) {
                     debug.warning("SMSLdapObject: Error in obtaining "
                             + "organization names: " + dn, ldape);
                 }
                 throw (new SMSException(ldape, "sms-org-cannot-obtain"));
             }
 
             String rdn = (entry.getDN()).toString();
             answer.add(rdn);
         }
         if (debug.messageEnabled()) {
             debug.message("SMSLdapObject: Successfully obtained "
                     + "organization names for : " + dn);
             debug.message("SMSLdapObject: Successfully obtained "
                     + "organization names  : " + answer.toString());
         }
         return (answer);
     }
     
     private String getDenormalizedName(SSOToken token, String name) {
         if (name.indexOf("^") >= 0) {
             String dataStore = SMSEntry.getDataStore(token);
             if ((dataStore != null) && 
                 dataStore.equals(SMSEntry.DATASTORE_ACTIVE_DIR)
             ) {
                 name = name.replaceAll("_", "=");
             }
         }
         return name;
     }
     
     private String getNormalizedName(SSOToken token, String dn) {
         if (dn.indexOf("^") >= 0) {
             String dataStore = SMSEntry.getDataStore(token);
             /*
              * If the datastore is Active Directory, convert
              * ou=dc=samples^dc=com^^AgentLogging to
              * ou=dc_samples^dc_com^^AgentLogging.
              * Otherwise BAD_NAME error LDAPException code 34 will occur.
              **/
             if ((dataStore != null) && 
                 dataStore.equals(SMSEntry.DATASTORE_ACTIVE_DIR)
             ) {
                 String[] dns = LDAPDN.explodeDN(dn, false);
                 StringBuffer buff = new StringBuffer();
 
                 String s = dns[0];
                 int idx = s.indexOf('=');
                 String naming = s.substring(0, idx+1);
                 String value = s.substring(idx+1).replaceAll("=", "_");
                 buff.append(naming).append(value);
 
                 for (int i = 1; i < dns.length; i++) {
                     s = dns[i];
                     idx = s.indexOf('=');
                     naming = s.substring(0, idx+1);
                     value = s.substring(idx+1).replaceAll("=", "_");
                     buff.append(",").append(naming).append(value);
                 }
                 dn = buff.toString();
             }
         }
         return dn;
     }
 }
