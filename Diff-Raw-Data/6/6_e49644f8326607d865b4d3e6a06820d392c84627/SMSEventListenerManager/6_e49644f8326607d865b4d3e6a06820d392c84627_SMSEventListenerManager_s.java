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
 * $Id: SMSEventListenerManager.java,v 1.6 2006-09-13 22:37:47 goodearth Exp $
  *
  * Copyright 2005 Sun Microsystems Inc. All Rights Reserved
  */
 
 package com.sun.identity.sm;
 
 import java.lang.reflect.Method;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Set;
 
 import netscape.ldap.LDAPDN;
 import netscape.ldap.util.DN;
 
 import com.sun.identity.shared.debug.Debug;
 import com.iplanet.sso.SSOToken;
 
 /**
  * This class registers a persistant search with the event service for any
  * changes to SMS object classes
  */
 class SMSEventListenerManager implements SMSObjectListener {
 
     // Notification list
     protected static Map notificationObjects = Collections
             .synchronizedMap(new HashMap());
 
     protected static HashMap allChanges = new HashMap();
 
    protected static Map nodeChanges = new HashMap();
 
     protected static Map subNodeChanges = Collections
             .synchronizedMap(new HashMap());
 
     private static Debug debug = SMSEntry.eventDebug;
 
     // Static Initialization variables
     protected static boolean initialized;
 
     static void initialize(SSOToken token) {
         if (!initialized) {
             try {
                 initialized = true;
                 SMSEntry.registerCallbackHandler(token,
                         new SMSEventListenerManager());
                 debug.message("Initialized SMS Event listner");
             } catch (Exception e) {
                 debug.error("SMSEventListenerManager::initialize "
                         + "Unable to intialize SMS listener: " + e);
                 initialized = false;
             }
         }
     }
 
     SMSEventListenerManager() {
         // do nothing
     }
 
     // Processes object changed notifications
     public void objectChanged(String odn, int event) {
         objectChanged(odn, event, false);
     }
 
     // Processes object changed notifications, including locally
     // generated ones.
     private void objectChanged(String odn, int event, boolean isLocal) {
         if (debug.messageEnabled()) {
             debug.message("SMSEventListener::entry changed for: " + odn
                     + " type: " + event);
         }
 
         // Normalize the DN
         DN sdn = new DN(odn);
         String dn = sdn.toRFCString().toLowerCase();
 
         // If event is delete, need to send notifications for sub-entries
         // if backend datastore notification is not enabled
         if (!isLocal && !SMSEntry.enableDataStoreNotification
                 && event == SMSObjectListener.DELETE) {
             // Collect the immediate children of the current sdn
             // from nodeChanges. All "subNodeChanges" entries would
             // have an entry in "nodeChanges", hence donot have to
             // iterate throught it
             Set childDNs = new HashSet();
             HashSet itemSet = new HashSet(2);
             synchronized (nodeChanges) {
                 itemSet.addAll(nodeChanges.keySet());
             }
             Iterator keyitems = itemSet.iterator();
             while (keyitems.hasNext()) {
                 String cdn = (String) keyitems.next();
                 if ((new DN(cdn)).isDescendantOf(sdn)) {
                     childDNs.add(cdn);
                 }
             }
             // Send the notifications
             if (debug.messageEnabled()) {
                 debug.message("SMSEventListener::objectChanged: Sending "
                         + "delete event of: " + dn + " to child nodes: "
                         + childDNs);
             }
             for (Iterator items = childDNs.iterator(); items.hasNext();) {
                 objectChanged((String) items.next(), event, true);
             }
         }
 
         // Send notifications to CachedSMSEntries
         sendNotifications((Set) nodeChanges.get(dn), null, event);
 
         // Process sub-entry changed events, not interested in attribute mods
         if (event == SMSObjectListener.ADD || event == SMSObjectListener.DELETE)
         {
             // Send notifications to CachedSubEntries
             if (debug.messageEnabled()) {
                 debug.message("SMSEventListener::entry changed for: " + dn
                         + " sending notifications to its parents");
             }
             sendNotifications((Set) subNodeChanges.get((new DN(dn)).getParent()
                     .toRFCString().toLowerCase()), odn, event);
         }
 
         // Send notification to objects that are interested all changes
         sendAllChangesNotification(dn, event);
     }
 
     public void allObjectsChanged() {
         if (debug.messageEnabled()) {
             debug.message("SMSEventListenerManager::allObjectsChanged called");
         }
         // Collect all the DNs from "nodeChanges" and send notifications
         Set dns = new HashSet();
         synchronized (nodeChanges) {
             for (Iterator items = nodeChanges.keySet().iterator(); items
                     .hasNext();) {
                 dns.add(items.next());
             }
         }
         // Send MODIFY notifications
         for (Iterator items = dns.iterator(); items.hasNext();) {
             objectChanged((String) items.next(), SMSObjectListener.MODIFY);
         }
     }
 
     /**
      * Registers notification for all changes to service entries
      */
     protected static String notifyAllNodeChanges(SSOToken token, Object object)
     {
         initialize(token);
         String id = SMSUtils.getUniqueID();
         synchronized (allChanges) {
             allChanges.put(id, object);
         }
         return (id);
     }
 
     /**
      * Registers notification for changes to nodes
      */
     protected static String notifyChangesToNode(SSOToken token, String dn,
             Method method, Object object, Object[] args) {
         initialize(token);
         String ndn = (new DN(dn)).toRFCString().toLowerCase();
         return (addNotificationObject(nodeChanges, ndn, method, object, args));
     }
 
     /**
      * Registers notification for changes to its sub-nodes
      */
     protected static String notifyChangesToSubNodes(SSOToken token, String dn,
             Method method, Object object, Object[] args) {
         initialize(token);
         String ndn = (new DN(dn)).toRFCString().toLowerCase();
         return (addNotificationObject(
                 subNodeChanges, ndn, method, object, args));
     }
 
     /**
      * Removes notification objects
      */
     protected static void removeNotification(String notificationID) {
         NotificationObject no = (NotificationObject) notificationObjects
                 .get(notificationID);
         if (no != null) {
             no.set.remove(no);
         }
         // Also remove from allChanges
         synchronized (allChanges) {
             allChanges.remove(notificationID);
         }
     }
 
     /**
      * Adds notification method to the map
      */
     private static String addNotificationObject(Map nChangesMap, String dn,
             Method method, Object object, Object[] args) {
         Set nObjects = (Set) nChangesMap.get(dn);
         if (nObjects == null) {
             nObjects = new HashSet();
             nChangesMap.put(dn, nObjects);
         }
         NotificationObject no = new NotificationObject(method, object, args,
                 nObjects);
         nObjects.add(no);
         notificationObjects.put(no.getID(), no);
         return (no.getID());
     }
 
     /**
      * Send notification for all changes to ServiceConfigManagerImpls
      */
     private static void sendAllChangesNotification(String dn, int type) {
         HashMap ac;
         synchronized (allChanges) {
             ac = (HashMap) allChanges.clone();
         }
         Iterator items = ac.values().iterator();
         while (items.hasNext()) {
             Object obj = items.next();
             if (obj instanceof ServiceConfigManagerImpl) {
                 ServiceConfigManagerImpl scimpl = (
                         ServiceConfigManagerImpl) obj;
                 scimpl.entryChanged(dn, type);
             }
             if (obj instanceof OrganizationConfigManagerImpl) {
                 OrganizationConfigManagerImpl ocimpl = 
                     (OrganizationConfigManagerImpl) obj;
                 ocimpl.entryChanged(dn, type);
             }
         }
     }
 
     /**
      * Sends notification to methods and objects within the set
      */
     private static void sendNotifications(Set nObjects, String dn, int event) {
         if ((nObjects == null) || (nObjects.isEmpty())) {
             return;
         }
         HashSet nobjs = new HashSet(2);
         synchronized (nObjects) {
             nobjs.addAll(nObjects);
         }
         Iterator items = nobjs.iterator();
 
         while (items.hasNext()) {
             try {
                 NotificationObject no = (NotificationObject) items.next();
                 if ((dn != null) && (no.object instanceof CachedSubEntries))
                 {
                     CachedSubEntries cse = (CachedSubEntries) no.object;
                     // We do not cache Realm names.
                     // We cache only service names and policy names.
                     if (!dn.startsWith(SMSEntry.ORG_PLACEHOLDER_RDN)) {
                         if (event == SMSObjectListener.ADD) {
                             cse.add(LDAPDN.explodeDN(dn, true)[0]);
                         } else {
                             cse.remove(LDAPDN.explodeDN(dn, true)[0]);
                         }
                     }
                 } else {
                     no.method.invoke(no.object, no.args);
                 }
             } catch (Exception e) {
                 debug.error("SMSEvent notification: "
                         + "Unable to send notification: ", e);
             }
         }
     }
 
     private static class NotificationObject {
         String id;
 
         Method method;
 
         Object object;
 
         Object[] args;
 
         Set set;
 
         NotificationObject(Method m, Object o, Object[] a, Set s) {
             method = m;
             object = o;
             args = a;
             set = s;
         }
 
         String getID() {
             if (id == null) {
                 id = SMSUtils.getUniqueID();
             }
             return (id);
         }
 
         public boolean equals(Object o) {
             if (o instanceof NotificationObject) {
                 NotificationObject no = (NotificationObject) o;
                 if (id == no.id) {
                     return (true);
                 }
             }
             return (false);
         }
     }
 }
