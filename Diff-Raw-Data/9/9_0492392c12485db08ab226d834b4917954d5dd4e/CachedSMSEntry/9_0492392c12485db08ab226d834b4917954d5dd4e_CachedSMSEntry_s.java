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
 * $Id: CachedSMSEntry.java,v 1.5 2008-04-15 21:07:29 goodearth Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.sm;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import netscape.ldap.util.DN;
 
 import com.iplanet.sso.SSOException;
 import com.iplanet.sso.SSOToken;
 import com.sun.identity.common.CaseInsensitiveHashMap;
 
 /**
  * The class <code>CachedSchemaManagerImpl</code> provides interfaces to
  * manage the SMSEntry. It caches SMSEntries which can used by ServiceSchema and
  * ServiceConfig classes.
  */
 public class CachedSMSEntry {
 
     // Mutex for shared table
     private static final String cachedSMSEntriesMutex = "CachedSMSEntriesMutex";
 
     // Notification method that will be called ...
     protected static final String UPDATE_NOTIFY_METHOD = 
         "updateAndNotifyListeners";
 
     protected static final String UPDATE_METHOD = "update";
 
     // Cache of SMSEntries (static)
     protected static Map smsEntries = new CaseInsensitiveHashMap(1000);
 
     // Pointer to Service objects and principals
    protected Set serviceObjects; // callback service objects
 
     protected Set principals; // Principals who have read access
 
     protected SSOToken token; // Valid SSOToken used for read
 
     protected DN dn;
 
     protected SMSEntry smsEntry;
 
     protected String notificationID = null;
 
     private boolean valid = false;
 
     // Private constructor, can be instantiated only via getInstance
     private CachedSMSEntry(SMSEntry e) {
         smsEntry = e;
         dn = new DN(e.getDN());
         serviceObjects = new HashSet();
         token = e.getSSOToken();
         principals = new HashSet();
         // principals.add(token.getTokenID().toString());
         this.addPrincipal(token);
         valid = true;
 
         // Set the SMSEntry as read only
         smsEntry.setReadOnly();
 
         // Add listener for this CachedSMSEntry object
         try {
             Class c = this.getClass();
             notificationID = SMSEventListenerManager.notifyChangesToNode(token,
                 smsEntry.getDN(), c.getDeclaredMethod("update", (Class[])null),
                     this, null);
         } catch (Exception ce) {
             SMSEntry.debug.error("CachedSMSEntry: unable to add listener for "
                     + e.getDN(), ce);
         }
         // Write debug messages
         if (SMSEntry.debug.messageEnabled()) {
             SMSEntry.debug.message("CachedSMSEntry: create " + "new instance: "
                     + dn);
         }
     }
 
     // ----------------------------------------------
     // Protected instance methods
     // ----------------------------------------------
 
     boolean isValid() {
         return valid;
     }
 
     void update() {
         if (SMSEntry.debug.messageEnabled()) {
             SMSEntry.debug.message("CachedSMSEntry: update "
                     + "method called: " + dn);
         }
         // Read the LDAP attributes and update listeners
         try {
             SSOToken t = getValidSSOToken();
             if (t != null) {
                 smsEntry.read(t);
                 updateServiceListeners(UPDATE_NOTIFY_METHOD);
             } else {
                 // this entry is no long valid, remove from cache
                 synchronized (cachedSMSEntriesMutex) {
                     smsEntries.remove(dn.toRFCString());
                 }
                 SMSEventListenerManager.removeNotification(notificationID);
                 notificationID = null;
                 valid = false;
             }
         } catch (SMSException e) {
             // Error in reading the attribtues, entry could be deleted
             // or does not have permissions to read the object
             SMSEntry.debug.error("Error in reading entry attributes: " + dn, e);
             // Remove this entry from the cache
             synchronized (cachedSMSEntriesMutex) {
                 smsEntries.remove(dn.toRFCString());
             }
             SMSEventListenerManager.removeNotification(notificationID);
             notificationID = null;
             valid = false;
         } catch (SSOException ssoe) {
             // Error in reading the attribtues, SSOToken problem
             // Might have timed-out
             SMSEntry.debug.error("SSOToken problem in reading entry "
                     + "attributes: " + dn, ssoe);
             // Remove this entry from the cache
             synchronized (cachedSMSEntriesMutex) {
                 smsEntries.remove(dn.toRFCString());
             }
             SMSEventListenerManager.removeNotification(notificationID);
             notificationID = null;
             valid = false;
         }
     }
 
     // Returns a valid SSOToken that can be used for reading
     SSOToken getValidSSOToken() {
         // Check if the cached SSOToken is valid
         if (!SMSEntry.tm.isValidToken(token)) {
             // Get a valid ssoToken from cached TokenIDs
             Set removeSSOTokens = new HashSet();
             for (Iterator items = principals.iterator(); items.hasNext();) {
                 String tokenID = (String) items.next();
                 try {
                     token = SMSEntry.tm.createSSOToken(tokenID);
                     if (SMSEntry.tm.isValidToken(token))
                         break;
                 } catch (SSOException ssoe) {
                     // SSOToken has expired, remove from list
                     removeSSOTokens.add(tokenID);
                 }
             }
             if (!removeSSOTokens.isEmpty()) {
                 Set sudoPrincipals = new HashSet(principals);
                 for (Iterator items = removeSSOTokens.iterator(); items
                         .hasNext();)
                     sudoPrincipals.remove(items.next());
                 principals = sudoPrincipals;
             }
         }
         // If there are no valid SSO Tokens return null
         if (principals.isEmpty()) {
             return (null);
         }
         return (token);
     }
 
     void updateServiceListeners(String method) {
         if (SMSEntry.debug.messageEnabled()) {
             SMSEntry.debug.message("CachedSMSEntry::updateServiceListeners "
                     + "method called: " + dn);
         }
         // Inform the ServiceSchemaManager's of changes to attributes
        Iterator objs = serviceObjects.iterator();
         while (objs.hasNext()) {
             synchronized (objs) {
                 Object obj = objs.next();
                 try {
                     Method m = obj.getClass().getDeclaredMethod(
                         method, (Class[])null);
                     m.invoke(obj, (Object[])null);
                 } catch (Exception e) {
                     SMSEntry.debug.error("CachedSMSEntry::unable to "
                         + "deliver notification(" + dn + ")", e);
                 }
             }
         }
     }
 
     void addServiceListener(Object o) {
         if (notificationID == null) {
             // Register for changes to SMSEntry attributes
             try {
                 Class c = Class.forName("com.sun.identity.sm.CachedSMSEntry");
                 SSOToken token = getValidSSOToken();
                 if (token == null) {
                     // Since there are no valid SSO Token
                     // do not add for event notification
                     return;
                 }
                 notificationID = SMSEventListenerManager.notifyChangesToNode(
                     token, smsEntry.getDN(), c.getDeclaredMethod("update",
                     (Class[])null), this, null);
             } catch (Exception ce) {
                 // this should not happen
                 SMSEntry.debug.error("CachedSMSEntry::unable to register "
                         + "service objects for notifications: ", ce);
             }
         }
         serviceObjects.add(o);
     }
 
     protected void removeServiceListener(Object o) {
         serviceObjects.remove(o);
         if (serviceObjects.isEmpty()) {
             SMSEventListenerManager.removeNotification(notificationID);
             notificationID = null;
         }
     }
 
     synchronized void addPrincipal(SSOToken t) {
         // Making a local copy to avoid synchronization problems
         Set sudoPrincipals = new HashSet(principals);
         sudoPrincipals.add(t.getTokenID().toString());
         principals = sudoPrincipals;
     }
 
     boolean checkPrincipal(SSOToken t) {
         return (principals.contains(t.getTokenID().toString()));
     }
 
     public SMSEntry getSMSEntry() {
         return (smsEntry);
     }
 
     public SMSEntry getClonedSMSEntry() {
         try {
             return ((SMSEntry) smsEntry.clone());
         } catch (CloneNotSupportedException c) {
             SMSEntry.debug.error("Unable to clone SMSEntry: " + smsEntry, c);
         }
         return (null);
     }
 
     boolean isNewEntry() {
         return (smsEntry.isNewEntry());
     }
 
     String getDN() {
         return (dn.toString());
     }
 
     void refresh(SMSEntry e) throws SMSException {
         smsEntry.refresh(e);
         updateServiceListeners(UPDATE_METHOD);
     }
 
     // ----------------------------------------------
     // protected static methods
     // ----------------------------------------------
     // Used by ServiceSchemaManager
     static CachedSMSEntry getInstance(SSOToken t, ServiceSchemaManagerImpl ssm,
             String serviceName, String version) throws SMSException {
         String dn = ServiceManager.getServiceNameDN(serviceName, version);
         try {
             return (getInstance(t, dn, ssm));
         } catch (SSOException ssoe) {
             SMSEntry.debug.error("SMS: Invalid SSOToken: ", ssoe);
             return (null);
         }
     }
 
     public static CachedSMSEntry getInstance(SSOToken t, String dn, Object obj)
             throws SMSException, SSOException {
         if (SMSEntry.debug.messageEnabled()) {
             SMSEntry.debug.message("CachedSMSEntry::getInstance: " + dn);
         }
         String cacheEntry = (new DN(dn)).toRFCString().toLowerCase();
         CachedSMSEntry answer = null;
         synchronized (cachedSMSEntriesMutex) {
             answer = (CachedSMSEntry) smsEntries.get(cacheEntry);
         }
         if (answer == null) {
             // Construct the SMS entry
             answer = new CachedSMSEntry(new SMSEntry(t, dn));
             // Check and add it to cache
             CachedSMSEntry tmp;
             synchronized (cachedSMSEntriesMutex) {
                 if ((tmp = (CachedSMSEntry) smsEntries.get(
                     cacheEntry)) == null) {
                     smsEntries.put(cacheEntry, answer);
                 } else {
                     answer = tmp;
                 }
             }
         }
         
         // Check if user has permissions
         if (!answer.checkPrincipal(t)) {
             // Read the SMS entry as that user, and ignore the results
             new SMSEntry(t, dn);
             answer.addPrincipal(t);
         }
         // Check for event notification object
         if (obj != null) {
             answer.addServiceListener(obj);
         }
         if (SMSEntry.debug.messageEnabled()) {
             SMSEntry.debug
                     .message("CachedSMSEntry::getInstance success: " + dn);
         }
         if (answer.isNewEntry()) {
             SMSEntry sEntry = answer.getSMSEntry();
             sEntry.dn = dn;
         }
         return (answer);
     }
 
     // Clears the cache
     static void clearCache() {
         Map sudoSMSEntries = smsEntries;
         synchronized (cachedSMSEntriesMutex) {
             // Reset smsEntries
             smsEntries = new CaseInsensitiveHashMap();
         }
         // Clear the cache
         Iterator it = sudoSMSEntries.keySet().iterator();
         while (it.hasNext()) {
             CachedSMSEntry curr = (CachedSMSEntry) sudoSMSEntries
                     .get(it.next());
             curr.valid = false;
         }
     }
 
     // ----------------------------------------------
     // protected instance methods for ServiceSchemaManager
     // ----------------------------------------------
     String getXMLSchema() {
         String[] schema = smsEntry.getAttributeValues(SMSEntry.ATTR_SCHEMA);
         if (schema == null) {
             // The entry could be deleted, hence return null
             return (null);
         }
         // Since schema is a single valued attribute
         return (schema[0]);
     }
 
     void writeXMLSchema(SSOToken token, InputStream xmlServiceSchema)
             throws SSOException, SMSException, IOException {
         int lengthOfStream = xmlServiceSchema.available();
         byte[] byteArray = new byte[lengthOfStream];
         xmlServiceSchema.read(byteArray, 0, lengthOfStream);
         writeXMLSchema(token, new String(byteArray));
     }
 
     void writeXMLSchema(SSOToken token, String xmlSchema) throws SSOException,
             SMSException {
         // Validate SSOtoken
         SMSEntry.validateToken(token);
         // Replace the attribute in the directory
         String[] attrValues = { xmlSchema };
         SMSEntry e = getClonedSMSEntry();
         e.setAttribute(SMSEntry.ATTR_SCHEMA, attrValues);
         e.save(token);
         refresh(e);
         if (SMSEntry.debug.messageEnabled()) {
             SMSEntry.debug.message("CachedSMSEntry::writeXMLSchema: "
                     + "successfully wrote the XML schema for dn: " + e.getDN());
         }
     }
 }
