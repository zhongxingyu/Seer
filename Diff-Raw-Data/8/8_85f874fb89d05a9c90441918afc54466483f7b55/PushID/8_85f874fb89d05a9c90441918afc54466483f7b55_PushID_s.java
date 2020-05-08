 package org.icepush;
 
 import java.io.Serializable;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class PushID
 implements Serializable {
     private static final Logger LOGGER = Logger.getLogger(PushID.class.getName());
 
     // These counters are only used by LOGGER
     private static AtomicInteger globalConfirmationTimeoutCounter = new AtomicInteger(0);
     private static AtomicInteger globalExpiryTimeoutCounter = new AtomicInteger(0);
 
     private transient final long cloudPushIDTimeout;
     private transient final Set<String> groups = new HashSet<String>();
     private final String id;
     private transient final LocalPushGroupManager localPushGroupManager;
     private transient final long minCloudPushInterval;
     private transient final long pushIDTimeout;
     private transient final Timer timeoutTimer;
 
     private transient TimerTask confirmationTimeout;
     private transient TimerTask expiryTimeout;
     private NotifyBackURI notifyBackURI;
     private transient PushConfiguration pushConfiguration;
    private transient Status status = newStatus();
 
     // These counters are only used by LOGGER
     private AtomicInteger confirmationTimeoutCounter = new AtomicInteger(0);
     private AtomicInteger expiryTimeoutCounter = new AtomicInteger(0);
 
     protected PushID(
         final String id, final String group, final long pushIDTimeout, final long cloudPushIDTimeout,
         final Timer timeoutTimer, final long minCloudPushInterval, final LocalPushGroupManager localPushGroupManager) {
 
         this.id = id;
         this.pushIDTimeout = pushIDTimeout;
         this.cloudPushIDTimeout = cloudPushIDTimeout;
         this.timeoutTimer = timeoutTimer;
         this.minCloudPushInterval = minCloudPushInterval;
         this.localPushGroupManager = localPushGroupManager;
         addToGroup(group);
     }
 
     public void addToGroup(String group) {
         groups.add(group);
     }
 
     public boolean cancelConfirmationTimeout() {
         if (confirmationTimeout != null) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(
                     Level.FINE,
                     "Cancel confirmation timeout for PushID '" + id + "'.\r\n\r\n" +
                         "Confirmation Timeout Counter        : " +
                             confirmationTimeoutCounter.decrementAndGet() + "\r\n" +
                         "Global Confirmation Timeout Counter : " +
                             globalConfirmationTimeoutCounter.decrementAndGet() + "\r\n" +
                         "Expiry Timeout Counter              : " +
                             expiryTimeoutCounter.get() + "\r\n" +
                         "Global Expiry Timeout Counter       : " +
                             globalExpiryTimeoutCounter.get() + "\r\n");
             }
             confirmationTimeout.cancel();
             confirmationTimeout = null;
             pushConfiguration = null;
             return true;
         }
         return false;
     }
 
     public boolean cancelExpiryTimeout() {
         if (expiryTimeout != null) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(
                     Level.FINE,
                     "Cancel expiry timeout for PushID '" + id + "'.\r\n\r\n" +
                         "Confirmation Timeout Counter        : " +
                             confirmationTimeoutCounter.get() + "\r\n" +
                         "Global Confirmation Timeout Counter : " +
                             globalConfirmationTimeoutCounter.get() + "\r\n" +
                         "Expiry Timeout Counter              : " +
                             expiryTimeoutCounter.decrementAndGet() + "\r\n" +
                         "Global Expiry Timeout Counter       : " +
                             globalExpiryTimeoutCounter.decrementAndGet() + "\r\n");
             }
             expiryTimeout.cancel();
             expiryTimeout = null;
             return true;
         }
         return false;
     }
 
     public void discard() {
         if (!localPushGroupManager.isParked(id)) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(Level.FINE, "PushID '" + id + "' discarded.");
             }
             localPushGroupManager.removePushID(id);
             localPushGroupManager.removePendingNotification(id);
             for (String groupName : groups) {
                 Group group = localPushGroupManager.getGroup(groupName);
                 if (group != null) {
                     group.removePushID(id);
                 }
             }
         }
     }
 
     public String getID() {
         return id;
     }
 
     public NotifyBackURI getNotifyBackURI() {
         return notifyBackURI;
     }
 
     public PushConfiguration getPushConfiguration() {
         return pushConfiguration;
     }
 
     public long getSequenceNumber() {
         return status.getSequenceNumber();
     }
 
     public Status getStatus() {
         return status;
     }
 
     public void removeFromGroup(String group) {
         groups.remove(group);
         if (groups.isEmpty()) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(
                     Level.FINE, "Disposed PushID '" + id + "' since it no longer belongs to any Push Group.");
             }
             localPushGroupManager.removePushID(id);
         }
     }
 
     public boolean setNotifyBackURI(final NotifyBackURI notifyBackURI) {
         boolean _modified =
             this.notifyBackURI == null ||
             !this.notifyBackURI.getURI().equals(notifyBackURI.getURI());
         this.notifyBackURI = notifyBackURI;
         return _modified;
     }
 
     public void setPushConfiguration(final PushConfiguration pushConfiguration) {
         this.pushConfiguration = pushConfiguration;
     }
 
     public void setSequenceNumber(final long sequenceNumber) {
         status.setSequenceNumber(sequenceNumber);
     }
 
     public boolean startConfirmationTimeout(final long sequenceNumber) {
         if (notifyBackURI != null) {
             long now = System.currentTimeMillis();
             long timeout = status.getConnectionRecreationTimeout() * 2;
             LOGGER.log(Level.FINE, "Calculated confirmation timeout: '" + timeout + "'");
             if (notifyBackURI.getTimestamp() + minCloudPushInterval <= now + timeout) {
                 return startConfirmationTimeout(sequenceNumber, timeout);
             } else {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE,
                         "Timeout is within the minimum Cloud Push interval for URI '" + notifyBackURI + "'. (" +
                             "timestamp: '" + notifyBackURI.getTimestamp() + "', " +
                             "minCloudPushInterval: '" + minCloudPushInterval + "', " +
                             "now: '" + now + "', " +
                             "timeout: '" + timeout + "'" +
                         ")");
                 }
             }
         }
         return false;
     }
 
     public boolean startConfirmationTimeout(final long sequenceNumber, final long timeout) {
         if (notifyBackURI != null &&
             notifyBackURI.getTimestamp() + minCloudPushInterval <= System.currentTimeMillis() + timeout &&
             pushConfiguration != null) {
 
             if (confirmationTimeout == null) {
                 if (LOGGER.isLoggable(Level.FINE)) {
                     LOGGER.log(
                         Level.FINE,
                         "Start confirmation timeout for PushID '" + id + "' (" +
                                 "URI: '" + notifyBackURI + "', " +
                                 "timeout: '" + timeout + "', " +
                                 "sequence number: '" + sequenceNumber + "'" +
                         ").\r\n\r\n" +
                             "Confirmation Timeout Counter        : " +
                                 confirmationTimeoutCounter.incrementAndGet() + "\r\n" +
                             "Global Confirmation Timeout Counter : " +
                                 globalConfirmationTimeoutCounter.incrementAndGet() + "\r\n" +
                             "Expiry Timeout Counter              : " +
                                 expiryTimeoutCounter.get() + "\r\n" +
                             "Global Expiry Timeout Counter       : " +
                                 globalExpiryTimeoutCounter.get() + "\r\n");
                 }
                 try {
                     timeoutTimer.schedule(
                         confirmationTimeout = newConfirmationTimeout(notifyBackURI, timeout), timeout);
                     return true;
                 } catch (final IllegalStateException exception) {
                     // timeoutTimer was cancelled or its timer thread terminated.
                     return false;
                 }
             }
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(
                     Level.FINE,
                     "Confirmation timeout already scheduled for PushID '" + id + "' " +
                         "(URI: '" + notifyBackURI + "', timeout: '" + timeout + "').");
             }
         }
         return false;
     }
 
     public boolean startExpiryTimeout() {
         return startExpiryTimeout(null, status.getSequenceNumber());
     }
 
     public boolean startExpiryTimeout(final NotifyBackURI notifyBackURI, final long sequenceNumber) {
         if (expiryTimeout == null) {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(
                     Level.FINE,
                     "Start expiry timeout for PushID '" + id + "' (" +
                             "URI: '" + notifyBackURI + "', " +
                             "timeout: '" + (notifyBackURI == null ? pushIDTimeout : cloudPushIDTimeout) + "', " +
                             "sequence number: '" + sequenceNumber + "'" +
                     ").\r\n\r\n" +
                         "Confirmation Timeout Counter        : " +
                             confirmationTimeoutCounter.get() + "\r\n" +
                         "Global Confirmation Timeout Counter : " +
                             globalConfirmationTimeoutCounter.get() + "\r\n" +
                         "Expiry Timeout Counter              : " +
                             expiryTimeoutCounter.incrementAndGet() + "\r\n" +
                         "Global Expiry Timeout Counter       : " +
                             globalExpiryTimeoutCounter.incrementAndGet() + "\r\n");
             }
             try {
                 timeoutTimer.schedule(
                     expiryTimeout = newExpiryTimeout(notifyBackURI),
                     notifyBackURI == null ? pushIDTimeout : cloudPushIDTimeout);
                 return true;
             } catch (final IllegalStateException exception) {
                 // timeoutTimer was cancelled or its timer thread terminated.
                 return false;
             }
         }
         if (LOGGER.isLoggable(Level.FINE)) {
             LOGGER.log(
                 Level.FINE,
                 "Expiry timeout already scheduled for PushID '" + id + "' (" +
                     "URI: '" + notifyBackURI + "', " +
                     "timeout: '" + (notifyBackURI == null ? pushIDTimeout : cloudPushIDTimeout) + "'" +
                 ").");
         }
         return false;
     }
 
     protected TimerTask getConfirmationTimeout() {
         return confirmationTimeout;
     }
 
     protected TimerTask getExpiryTimeout() {
         return expiryTimeout;
     }
 
     protected ConfirmationTimeout newConfirmationTimeout(final NotifyBackURI notifyBackURI, final long timeout) {
         return new ConfirmationTimeout(this, notifyBackURI, timeout);
     }
 
     protected ExpiryTimeout newExpiryTimeout(final NotifyBackURI notifyBackURI) {
         return new ExpiryTimeout(this, notifyBackURI);
     }
 
     protected Status newStatus() {
         return new Status();
     }
 
     protected static class ConfirmationTimeout
     extends TimerTask {
         protected final PushID pushID;
         protected final NotifyBackURI notifyBackURI;
         protected final long timeout;
 
         protected ConfirmationTimeout(final PushID pushID, final NotifyBackURI notifyBackURI, final long timeout) {
             this.pushID = pushID;
             this.notifyBackURI = notifyBackURI;
             this.timeout = timeout;
         }
 
         @Override
         public void run() {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(
                     Level.FINE,
                     "Confirmation timeout occurred for PushID '" + pushID.getID() + "' " +
                         "(URI: '" + notifyBackURI + "', timeout: '" + timeout + "').");
             }
             try {
                 if (notifyBackURI != null) {
                     pushID.localPushGroupManager.park(pushID.getID(), notifyBackURI);
                 }
                 NotifyBackURI notifyBackURI = pushID.localPushGroupManager.getNotifyBackURI(pushID.getID());
                 if (notifyBackURI != null &&
                     notifyBackURI.getTimestamp() + pushID.minCloudPushInterval <=
                         System.currentTimeMillis()) {
 
                     if (LOGGER.isLoggable(Level.FINE)) {
                         LOGGER.log(Level.FINE, "Cloud Push dispatched for PushID '" + pushID.getID() + "'.");
                     }
                     notifyBackURI.touch();
                     pushID.localPushGroupManager.getOutOfBandNotifier().broadcast(
                         (PushNotification)pushID.getPushConfiguration(),
                         new String[] {
                             notifyBackURI.getURI()
                         });
                 }
                 pushID.cancelConfirmationTimeout();
             } catch (Exception exception) {
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.log(
                         Level.WARNING,
                         "Exception caught on confirmationTimeout TimerTask.",
                         exception);
                 }
             }
         }
     }
 
     protected static class ExpiryTimeout
     extends TimerTask {
         protected final PushID pushID;
         protected final NotifyBackURI notifyBackURI;
 
         protected ExpiryTimeout(final PushID pushID, final NotifyBackURI notifyBackURI) {
             this.pushID = pushID;
             this.notifyBackURI = notifyBackURI;
         }
 
         @Override
         public void run() {
             if (LOGGER.isLoggable(Level.FINE)) {
                 LOGGER.log(
                     Level.FINE,
                     "Expiry timeout occurred for PushID '" + pushID.getID() + "' (" +
                         "URI: '" + notifyBackURI + "', " +
                         "timeout: '" +
                             (notifyBackURI == null ? pushID.pushIDTimeout : pushID.cloudPushIDTimeout) + "'" +
                     ").");
             }
             try {
                 pushID.discard();
                 pushID.cancelExpiryTimeout();
             } catch (Exception exception) {
                 if (LOGGER.isLoggable(Level.WARNING)) {
                     LOGGER.log(
                         Level.WARNING,
                         "Exception caught on expiryTimeout TimerTask.",
                         exception);
                 }
             }
         }
     }
 
     public static class Status
     implements Serializable {
         private long backupConnectionRecreationTimeout;
         private long connectionRecreationTimeout = -1;
         private long sequenceNumber = -1;
 
         protected Status() {
             // Do nothing.
         }
 
         protected Status(final Status status) {
             setBackupConnectionRecreationTimeout(status.getBackupConnectionRecreationTimeout());
             setConnectionRecreationTimeout(status.getConnectionRecreationTimeout());
             setSequenceNumber(status.getSequenceNumber());
         }
 
         public void backUpConnectionRecreationTimeout() {
             backupConnectionRecreationTimeout = connectionRecreationTimeout;
         }
 
         public long getBackupConnectionRecreationTimeout() {
             return backupConnectionRecreationTimeout;
         }
 
         public long getConnectionRecreationTimeout() {
             return connectionRecreationTimeout;
         }
 
         public long getSequenceNumber() {
             return sequenceNumber;
         }
 
         public void revertConnectionRecreationTimeout() {
             connectionRecreationTimeout = backupConnectionRecreationTimeout;
         }
 
         public void setBackupConnectionRecreationTimeout(final long backupConnectionRecreationTimeout) {
             this.backupConnectionRecreationTimeout = backupConnectionRecreationTimeout;
         }
 
         public void setConnectionRecreationTimeout(final long connectionRecreationTimeout) {
             this.connectionRecreationTimeout = connectionRecreationTimeout;
         }
 
         public void setSequenceNumber(final long sequenceNumber) {
             this.sequenceNumber = sequenceNumber;
         }
 
         @Override
         public String toString() {
             return
                 new StringBuilder().
                     append("PushID.Status[").append(membersAsString()).append("]").
                         toString();
         }
 
         protected String membersAsString() {
             return
                 new StringBuilder().
                     append("backupConnectionRecreationTimeout: ").
                         append("'").append(getBackupConnectionRecreationTimeout()).append("', ").
                     append("connectionRecreationTimeout: ").
                         append("'").append(getConnectionRecreationTimeout()).append("', ").
                     append("sequenceNumber: ").
                         append("'").append(getSequenceNumber()).append("'").
                             toString();
         }
     }
 }
