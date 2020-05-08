 /*
  * Copyright 2004-2012 ICEsoft Technologies Canada Corp.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the
  * License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an "AS
  * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
  * express or implied. See the License for the specific language
  * governing permissions and limitations under the License.
  *
  */
 package org.icepush;
 
 import org.icepush.servlet.ServletContextConfiguration;
 
 import javax.servlet.ServletContext;
 import java.util.*;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class LocalPushGroupManager extends AbstractPushGroupManager implements PushGroupManager {
     private static final Logger LOGGER = Logger.getLogger(LocalPushGroupManager.class.getName());
     private static final String[] STRINGS = new String[0];
     private static final int GROUP_SCANNING_TIME_RESOLUTION = 3000; // ms
     private static final OutOfBandNotifier NOOPOutOfBandNotifier = new OutOfBandNotifier() {
         public void broadcast(PushNotification notification, String[] uris) {
             System.out.println("message send " + notification + " to " + Arrays.asList(uris));
         }
 
         public void registerProvider(String protocol, NotificationProvider provider) {
         }
         public void trace(String message)  {
             LOGGER.warning("NOOPOutOfBandNotifier discarding notification " + message);
         }
     };
     private static final Runnable NOOP = new Runnable() {
         public void run() {
         }
     };
 
     private final Map<String, PushID> pushIDMap = new HashMap<String, PushID>();
     private final Map<String, Group> groupMap = new HashMap<String, Group>();
     private final HashSet<String> pendingNotifications = new HashSet();
     private final HashMap<String, String> parkedPushIDs = new HashMap();
     private final NotificationBroadcaster outboundNotifier = new LocalNotificationBroadcaster();
     private final Timer timer = new Timer("Notification queue consumer.", true);
     private final TimerTask queueConsumer;
     private final BlockingQueue<Runnable> queue;
     private final long groupTimeout;
     private final long pushIdTimeout;
     private final ServletContext context;
 
     private long lastTouchScan = System.currentTimeMillis();
     private long lastExpiryScan = System.currentTimeMillis();
 
     public LocalPushGroupManager(final ServletContext servletContext) {
         this.context = servletContext;
         Configuration configuration = new ServletContextConfiguration("org.icepush", servletContext);
         this.groupTimeout = configuration.getAttributeAsLong("groupTimeout", 2 * 60 * 1000);
         this.pushIdTimeout = configuration.getAttributeAsLong("pushIdTimeout", 2 * 60 * 1000);
         int notificationQueueSize = configuration.getAttributeAsInteger("notificationQueueSize", 1000);
         this.queue = new LinkedBlockingQueue<Runnable>(notificationQueueSize);
         this.queueConsumer = new QueueConsumerTask();
         this.timer.schedule(queueConsumer, 0);
     }
 
     public void scan(String[] confirmedPushIDs) {
         Set<String> pushIDs = new HashSet<String>();
         long now = System.currentTimeMillis();
         //accumulate pushIDs
         pushIDs.addAll(Arrays.asList(confirmedPushIDs));
         //avoid to scan/touch the groups on each notification
         if (lastTouchScan + GROUP_SCANNING_TIME_RESOLUTION < now) {
             try {
                 for (Group group : new ArrayList<Group>(groupMap.values())) {
                     group.touchIfMatching(pushIDs);
                     group.discardIfExpired();
                 }
                 for (PushID pushID : new ArrayList<PushID>(pushIDMap.values())) {
                     pushID.touchIfMatching(pushIDs);
                     pushID.discardIfExpired();
                 }
             } finally {
                 lastTouchScan = now;
                 lastExpiryScan = now;
             }
         }
     }
 
     public void addMember(final String groupName, final String id) {
         PushID pushID = pushIDMap.get(id);
         if (pushID == null) {
             pushIDMap.put(id, new PushID(id, groupName));
         } else {
             pushID.addToGroup(groupName);
         }
         Group group = groupMap.get(groupName);
         if (group == null) {
             groupMap.put(groupName, new Group(groupName, id));
         } else {
             group.addPushID(id);
         }
         memberAdded(groupName, id);
         if (LOGGER.isLoggable(Level.FINEST)) {
             LOGGER.log(Level.FINEST, "Added pushId '" + id + "' to group '" + groupName + "'.");
         }
     }
 
     public void addNotificationReceiver(final NotificationBroadcaster.Receiver observer) {
         outboundNotifier.addReceiver(observer);
     }
 
     public void deleteNotificationReceiver(final NotificationBroadcaster.Receiver observer) {
         outboundNotifier.deleteReceiver(observer);
     }
 
     public void clearPendingNotifications(List pushIdList) {
         pendingNotifications.removeAll(pushIdList);
     }
 
     public String[] getPendingNotifications() {
         return pendingNotifications.toArray(STRINGS);
     }
 
     public Map<String, String[]> getGroupMap() {
         Map<String, String[]> groupMap = new HashMap<String, String[]>();
         for (Group group : new ArrayList<Group>(this.groupMap.values())) {
             groupMap.put(group.name, group.getPushIDs());
         }
         return groupMap;
     }
 
     public void push(final String groupName) {
         if (!queue.offer(new Notification(groupName))) {
             if (LOGGER.isLoggable(Level.INFO)) {
                 LOGGER.log(Level.INFO, "Notification for group '" + groupName + "' was dropped, queue maximum size reached.");
             }
         }
     }
 
     public void push(final String groupName, final PushConfiguration config) {
         Notification notification;
         if (config.getAttributes().get("subject") != null) {
             notification = new OutOfBandNotification(groupName, config);
         } else {
             notification = new Notification(groupName, config);
         }
         //add this notification to a blocking queue
         queue.add(notification);
     }
 
     public void removeMember(final String groupName, final String pushId) {
         Group group = groupMap.get(groupName);
         if (group != null) {
             group.removePushID(pushId);
             PushID id = pushIDMap.get(pushId);
             if (id != null) {
                 id.removeFromGroup(groupName);
             }
             memberRemoved(groupName, pushId);
             if (LOGGER.isLoggable(Level.FINEST)) {
                 LOGGER.log(Level.FINEST, "Removed pushId '" + pushId + "' from group '" + groupName + "'.");
             }
         }
     }
 
     public void park(String[] pushIds, String notifyBackURI) {
         for (int i = 0; i < pushIds.length; i++) {
             parkedPushIDs.put(pushIds[i], notifyBackURI);
         }
     }
 
     public void pruneParkedIDs(String notifyBackURI, List<String> listenedPushIds)  {
         for (String parkedID : parkedPushIDs.keySet())  {
             String thisNotifyBack = parkedPushIDs.get(parkedID);
             if (thisNotifyBack.equals(notifyBackURI) && 
                     !listenedPushIds.contains(parkedID))  {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.fine("Removed unlistened parked pushID " + 
                             parkedID + " for " + notifyBackURI);
                 }
                 parkedPushIDs.remove(parkedID);
             }
         }
     }
 
     public void shutdown() {
         queueConsumer.cancel();
         timer.cancel();
     }
 
     private void scanForExpiry() {
         long now = System.currentTimeMillis();
         //avoid to scan/touch the groups on each notification
         if (lastExpiryScan + GROUP_SCANNING_TIME_RESOLUTION < now) {
             try {
                 for (Group group : new ArrayList<Group>(groupMap.values())) {
                     group.discardIfExpired();
                 }
                 for (PushID pushID : new ArrayList<PushID>(pushIDMap.values())) {
                     pushID.discardIfExpired();
                 }
             } finally {
                 lastExpiryScan = now;
             }
         }
     }
 
     public void touchPushID(final String id, final Long timestamp) {
         PushID pushID = pushIDMap.get(id);
         if (pushID != null) {
             pushID.touch(timestamp);
         }
     }
 
     private OutOfBandNotifier getOutOfBandNotifier() {
         Object attribute = context.getAttribute(OutOfBandNotifier.class.getName());
         return attribute == null ? NOOPOutOfBandNotifier : (OutOfBandNotifier) attribute;
     }
 
     private class Group {
         private final Set<String> pushIdList = new HashSet<String>();
         private final String name;
         private long lastAccess = System.currentTimeMillis();
 
         private Group(final String name, final String firstPushId) {
             this.name = name;
             if (LOGGER.isLoggable(Level.FINEST)) {
                 LOGGER.log(Level.FINEST, "'" + this.name + "' push group created.");
             }
             addPushID(firstPushId);
         }
 
         private void addPushID(final String pushId) {
             pushIdList.add(pushId);
         }
 
         private void discardIfExpired() {
             //expire group
             if (lastAccess + groupTimeout < System.currentTimeMillis()) {
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.log(Level.FINEST, "'" + name + "' push group expired.");
                 }
                 groupMap.remove(name);
                 pendingNotifications.removeAll(pushIdList);
                 for (String id : pushIdList) {
                     PushID pushID = pushIDMap.get(id);
                     if (pushID != null) {
                         pushID.removeFromGroup(name);
                     }
                 }
             }
         }
 
         private String[] getPushIDs() {
             return pushIdList.toArray(new String[pushIdList.size()]);
         }
 
         private void removePushID(final String pushId) {
             pushIdList.remove(pushId);
             if (pushIdList.isEmpty()) {
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.log(
                             Level.FINEST, "Disposed '" + name + "' push group since it no longer contains any pushIds.");
                 }
                 groupMap.remove(name);
             }
         }
 
         private void touch() {
             touch(System.currentTimeMillis());
         }
 
         private void touch(final Long timestamp) {
             lastAccess = timestamp;
         }
 
         private void touchIfMatching(final Collection pushIds) {
             Iterator i = pushIds.iterator();
             while (i.hasNext()) {
                 String pushId = (String) i.next();
                 if (pushIdList.contains(pushId)) {
                     touch();
                     groupTouched(name, lastAccess);
                     //no need to touchIfMatching again
                     //return right away without checking the expiration
                     return;
                 }
             }
         }
     }
 
     private class PushID {
         private final String id;
         private final Set<String> groups = new HashSet<String>();
         private long lastAccess = System.currentTimeMillis();
 
         private PushID(String id, String group) {
             this.id = id;
             addToGroup(group);
         }
 
         private void addToGroup(String group) {
             groups.add(group);
         }
 
         private void removeFromGroup(String group) {
             groups.remove(group);
             if (groups.isEmpty()) {
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.log(Level.FINEST, "Disposed '" + id + "' pushId since it no longer belongs to any group.");
                 }
                 pushIDMap.remove(id);
             }
         }
 
         private void touch() {
             touch(System.currentTimeMillis());
         }
 
         private void touch(final Long timestamp) {
             lastAccess = timestamp;
         }
 
         private void touchIfMatching(Set pushIDs) {
             if (pushIDs.contains(id)) {
                 touch();
                 pushIDTouched(id, lastAccess);
             }
         }
 
         private void discardIfExpired() {
             //expire pushId
             if (!parkedPushIDs.containsKey(id) && lastAccess + pushIdTimeout < System.currentTimeMillis()) {
                 if (LOGGER.isLoggable(Level.FINEST)) {
                     LOGGER.log(Level.FINEST, "'" + id + "' pushId expired.");
                 }
                 pushIDMap.remove(id);
                 pendingNotifications.remove(id);
                 for (String groupName : groups) {
                     Group group = groupMap.get(groupName);
                     if (group != null) {
                         group.removePushID(id);
                     }
                 }
             }
         }
     }
 
     private class Notification implements Runnable {
         private final String groupName;
         private final Set<String> exemptPushIDSet = new HashSet<String>();
 
         public Notification(String groupName) {
             this.groupName = groupName;
         }
 
         public Notification(final String groupName, final PushConfiguration config) {
             this.groupName = groupName;
             Set pushIDSet = (Set)config.getAttributes().get("pushIDSet");
             if (pushIDSet != null) {
                 this.exemptPushIDSet.addAll(pushIDSet);
             }
         }
 
         public void run() {
             try {
                 Group group = groupMap.get(groupName);
                 if (group != null) {
                     if (LOGGER.isLoggable(Level.FINEST)) {
                         LOGGER.log(Level.FINEST, "Push notification triggered for '" + groupName + "' group.");
                     }
                    List<String> pushIDList = Arrays.asList(group.getPushIDs());
                     pushIDList.removeAll(exemptPushIDSet);
                     pendingNotifications.addAll(pushIDList);
                     outboundNotifier.broadcast(
                         pushIDList.toArray(new String[pushIDList.size()]),
                         new NotifiedPushIDsHandler() {
                             public void handle(final String[] notifiedPushIDs) {
                                 for (int i = 0; i < notifiedPushIDs.length; i++) {
                                     parkedPushIDs.remove(notifiedPushIDs[i]);
                                 }
                                 scan(notifiedPushIDs);
                             }
                         });
                     pushed(groupName);
                 }
             } finally {
                 scanForExpiry();
             }
         }
     }
 
     private class OutOfBandNotification extends Notification {
         private final String groupName;
         private final Set<String> exemptPushIDSet = new HashSet<String>();
         private final PushConfiguration config;
 
         public OutOfBandNotification(String groupName, PushConfiguration config) {
             super(groupName);
             this.groupName = groupName;
             Set pushIDSet = (Set)config.getAttributes().get("pushIDSet");
             if (pushIDSet != null) {
                 this.exemptPushIDSet.addAll(pushIDSet);
             }
             this.config = config;
         }
 
         public void run() {
             try {
                 try {
                     Group group = groupMap.get(groupName);
                     if (group != null) {
                         if (LOGGER.isLoggable(Level.FINEST)) {
                             LOGGER.log(Level.FINEST, "Push notification triggered for '" + groupName + "' group.");
                         }
                        List<String> pushIDList = Arrays.asList(group.getPushIDs());
                         pushIDList.removeAll(exemptPushIDSet);
                         pendingNotifications.addAll(pushIDList);
 
                         String[] notified = outboundNotifier.broadcast(pushIDList.toArray(new String[pushIDList.size()]));
                         for (int i = 0; i < notified.length; i++) {
                             parkedPushIDs.remove(notified[i]);
                         }
                         scan(notified);
                         pushed(groupName);
                     }
                 } finally {
                     scanForExpiry();
                 }
                 Group group = groupMap.get(groupName);
                 String[] pushIDs = new String[0];
                 if (null != group) {
                     pushIDs = group.getPushIDs();
                 }
                 HashSet uris = new HashSet();
                 for (int i = 0; i < pushIDs.length; i++) {
                     String pushID = pushIDs[i];
                     String uri = parkedPushIDs.get(pushID);
                     if (uri != null) {
                         uris.add(uri);
                     }
                 }
 
                 if (!uris.isEmpty()) {
                     if (LOGGER.isLoggable(Level.FINE)) {
                         LOGGER.log(Level.FINE, "Cloud Push dispatched for " + parkedPushIDs);
                     }
                     getOutOfBandNotifier().broadcast(
                             (PushNotification) config,
                             (String[]) uris.toArray(STRINGS));
                 }
             } finally {
                 scanForExpiry();
             }
         }
     }
 
     private class QueueConsumerTask extends TimerTask {
         private boolean running = true;
 
         public void run() {
             //take tasks from the queue and execute them serially
             while (running) {
                 try {
                     queue.take().run();
                 } catch (InterruptedException e) {
                     LOGGER.log(Level.FINEST, "Notification queue draining interrupted.");
                 } catch (Throwable t)  {
                     LOGGER.log(Level.WARNING, "Notification queue encountered ", t);
                 }
             }
         }
 
         public boolean cancel() {
             running = false;
             queue.offer(NOOP);//add noop to unblock the queue
             return super.cancel();
         }
     }
 }
