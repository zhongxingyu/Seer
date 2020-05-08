 /*-
  * Copyright (c) 2008, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.logicprobe.LogicMail.mail;
 
 import java.io.IOException;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.util.Queue;
 
 public class IncomingMailConnectionHandler extends AbstractMailConnectionHandler {
     private final NetworkMailStore mailStore;
     private final IncomingMailClient incomingClient;
     private FolderTreeItem previousActiveFolder;
     private AccountConfig accountConfig;
     
     /**
      * Maximum amount of time to spend in the idle state.
      * Currently set to 5 minutes. (1000 ms/sec * 60 sec/min * 5 min)
      */
     private static final int IDLE_TIMEOUT = 300000;
 
     /**
      * Interval to do explicit NOOP-based polling when the idle state is
      * not available.  Currently set to 5 minutes.
      */
     private static final int NOOP_TIMEOUT = 300000;
     
     /**
      * Maximum amount of time to keep a connection open on a mail store that
      * uses locked folders.  Currently set to 4 minutes, so it is slightly
      * shorter than the smallest configurable polling frequency.
      */
     private static final int LOCKED_TIMEOUT = 240000;
     
     private final Timer idleTimer = new Timer();
     private TimerTask idleTimerTask;
     private boolean idleTimeout;
     private boolean idleRecentMessagesRequested;
     private long idleStartTime;
     
     private volatile long accumulatedIdleTime;
     private final Timer pollingTimer = new Timer();
     private TimerTask pollingTimerTask;
     
     private static final int MS_PER_MIN = 60000;
     private static final int REFRESH_TOLERANCE = 60000;
     
     /**
      * Listener to handle asynchronous notifications from the mail client.
      */
     private IncomingMailClientListener mailClientListener = new IncomingMailClientListener() {
         public void recentFolderMessagesAvailable() {
             handleRecentFolderMessagesAvailable();
         }
         public void folderMessageFlagsChanged(MessageToken token, MessageFlags messageFlags) {
             handleFolderMessageFlagsChanged(token, messageFlags);
         }
         public void folderMessageExpunged(MessageToken expungedToken, MessageToken[] updatedTokens) {
             handleFolderMessageExpunged(expungedToken, updatedTokens);
         }
         public void idleModeError() {
             handleIdleModeError();
         }
     };
     
     public IncomingMailConnectionHandler(NetworkMailStore mailStore, IncomingMailClient client) {
         super(client);
         this.accountConfig = client.getAcctConfig();
         this.mailStore = mailStore;
         this.incomingClient = client;
         this.incomingClient.setListener(mailClientListener);
     }
 
     public void start() {
         cleanupIdleState();
         super.start();
     }
     
     public void shutdown(boolean wait) {
         cleanupIdleState();
         super.shutdown(wait);
     }
     
     protected void handleRequest(ConnectionHandlerRequest request) throws IOException, MailException {
         cleanupIdleState();
         super.handleRequest(request);
     }
     
     private void handleRecentFolderMessagesAvailable() {
         if(getConnectionState() == STATE_IDLE) {
             if(idleRecentMessagesRequested) { return; }
             idleRecentMessagesRequested = true;
         }
         
         // Make sure we ignore this event if it occurs during the setup portion
         // of the command normally enqueued as a result of this notification.
         ConnectionHandlerRequest currentRequest = getRequestInProgress();
         if(currentRequest instanceof FolderMessagesRequest
                 && ((FolderMessagesRequest)currentRequest).getType() == FolderMessagesRequest.TYPE_RECENT) {
             return;
         }
 
         FolderMessagesRequest request = mailStore.createFolderMessagesRecentRequest(incomingClient.getActiveFolder(), false);
         ((ConnectionHandlerRequest)request).setDeliberate(false);
         mailStore.processRequest(request);
     }
 
     private void handleFolderMessageFlagsChanged(MessageToken token, MessageFlags messageFlags) {
         // This notification just updates local data, so it does not need to
         // break out of the idle state.
         
         mailStore.fireMessageFlagsChanged(token, messageFlags);
     }
 
     private void handleFolderMessageExpunged(MessageToken expungedToken, MessageToken[] updatedTokens) {
         // This notification just updates local data, so it does not need to
         // break out of the idle state.
         
         mailStore.fireFolderExpunged(incomingClient.getActiveFolder(), new MessageToken[] { expungedToken }, updatedTokens);
     }
 
     private void handleIdleModeError() {
         idleTimerTask.cancel();
         Queue requestQueue = getRequestQueue();
         synchronized(requestQueue) {
             requestQueue.notifyAll();
         }
     }
 
     /**
      * Handles the start of the IDLE state.
      */
     protected void handleBeginIdle() throws IOException, MailException {
         idleStartTime = System.currentTimeMillis();
         FolderTreeItem inboxFolder = incomingClient.getInboxFolder();
         FolderTreeItem activeFolder = incomingClient.getActiveFolder();
         
         // This case will happen if the connection died while idling, was
         // restored, and we need to re-select the correct active folder.
         if(activeFolder == null && previousActiveFolder != null) {
             try {
                 handleSetActiveFolder(previousActiveFolder);
             } catch (MailException e) {
                 handleSetActiveFolder(inboxFolder);
             }
         }
         this.previousActiveFolder = activeFolder;
         
        if(incomingClient.hasIdle() && incomingClient.isIdleEnabled()) {
             startIdleTimer(IDLE_TIMEOUT);
             incomingClient.idleModeBegin();
         }
         else if(!incomingClient.hasLockedFolders()) {
             // In this case, we do a NOOP-based polling
             startIdleTimer(NOOP_TIMEOUT);
         }
         else {
             // If we got here, that means we are on a mail store that locks
             // folders while the client is connected. In this case, the only
             // reason to keep the connection open is to improve the user
             // experience.  However, we should still eventually timeout and
             // disconnect.
             startIdleTimer(LOCKED_TIMEOUT);
         }
     }
 
     private void startIdleTimer(int timeout) {
         idleRecentMessagesRequested = false;
         idleTimeout = false;
         idleTimerTask = new TimerTask() {
             public void run() {
                 handleIdleModeTimeout();
             }
         };
         idleTimer.schedule(idleTimerTask, timeout);
     }
 
     protected void handleIdleModeTimeout() {
         idleTimeout = true;
         Queue requestQueue = getRequestQueue();
         synchronized(requestQueue) {
             requestQueue.notifyAll();
         }
     }
 
     protected void handleEndIdle() throws IOException, MailException {
         if(idleTimerTask != null) {
             idleTimerTask.cancel();
         }
         
        if(incomingClient.hasIdle() && incomingClient.isIdleEnabled()) {
             incomingClient.idleModeEnd();
         }
         
         if(idleTimeout) {
             if(incomingClient.hasLockedFolders()) {
                 // An idle timeout on a mail store with locked folders should
                 // be handled by promptly disconnecting.  That way, any further
                 // data requests will cause a reconnect and get fresh data.
                 NetworkDisconnectRequest disconnectRequest = new NetworkDisconnectRequest(mailStore, NetworkDisconnectRequest.REQUEST_DISCONNECT_TIMEOUT);
                 disconnectRequest.setDeliberate(false);
                 mailStore.processRequest(disconnectRequest);
             }
             else {
                 // If idle mode was disabled for long enough that a timeout
                 // occurred, then something forgot to re-enable it. Just to be
                 // safe, we will re-enable it behind the scenes.
                 incomingClient.setIdleEnabled(true);
                 
                 incomingClient.noop();
                 if(!idleRecentMessagesRequested) {
                     // If we had a non-INBOX folder selected, then an idle timeout
                     // should switch the active folder back to the INBOX.
                     FolderTreeItem inboxFolder = incomingClient.getInboxFolder();
                     FolderTreeItem activeFolder = incomingClient.getActiveFolder();
                     if(inboxFolder != null && activeFolder != null
                             && !inboxFolder.getPath().equalsIgnoreCase(activeFolder.getPath())) {
                         handleSetActiveFolder(inboxFolder);
                     }
                 }
                 handleConnectionIdleTimeout(System.currentTimeMillis() - idleStartTime);
             }
         }
     }
     
     void handleRequestDisconnect() throws IOException, MailException {
         this.previousActiveFolder = null;
         throw new MailException("", true, REQUEST_DISCONNECT);
     }
     
     void handleRequestDisconnectTimeout() throws IOException, MailException {
         this.previousActiveFolder = null;
         handleConnectionDisconnectTimeout(System.currentTimeMillis() - idleStartTime);
         throw new MailException("", true, REQUEST_DISCONNECT_TIMEOUT);
     }
 
     private void handleSetActiveFolder(FolderTreeItem folder) throws IOException, MailException {
         boolean isStateValid = incomingClient.setActiveFolder(folder, true);
         
         if(!isStateValid) {
             mailStore.fireFolderRefreshRequired(folder, false);
         }
     }
     
     private void handleConnectionIdleTimeout(long idleDuration) {
         accumulatedIdleTime += idleDuration;
         
         long refreshFrequency = accountConfig.getRefreshFrequency() * MS_PER_MIN;
 
         if(refreshFrequency == 0) {
             accumulatedIdleTime = 0;
             return;
         }
         
         // If we've been idle for a time period close to the refresh frequency,
         // then we should trigger a refresh.
         if(Math.abs(accumulatedIdleTime - refreshFrequency) < REFRESH_TOLERANCE) {
             accumulatedIdleTime = 0;
             mailStore.fireRefreshRequired(false);
         }
     }
     
     private void handleConnectionDisconnectTimeout(long idleDuration) {
         accumulatedIdleTime += idleDuration;
         
         long refreshFrequency = accountConfig.getRefreshFrequency() * MS_PER_MIN;
         
         if(refreshFrequency == 0) {
             accumulatedIdleTime = 0;
             return;
         }
         
         // If we've been idle for a time period close to the refresh frequency,
         // then we should trigger a refresh.
         if((accumulatedIdleTime + (REFRESH_TOLERANCE >>> 1)) > refreshFrequency) {
             accumulatedIdleTime = 0;
             mailStore.fireRefreshRequired(false);
         }
         else {
             // Otherwise, we should start the polling timer
             long nextRefresh = refreshFrequency - accumulatedIdleTime;
             
             schedulePollingRefresh(nextRefresh);
         }
     }
     
     protected void handleFailedConnection(boolean isSilent) {
         long refreshFrequency = accountConfig.getRefreshFrequency() * MS_PER_MIN;
         if(refreshFrequency == 0) { return; }
         
         schedulePollingRefresh(refreshFrequency);
     }
 
     private void schedulePollingRefresh(long nextRefresh) {
         synchronized(pollingTimer) {
             if(pollingTimerTask != null) {
                 pollingTimerTask.cancel();
                 pollingTimerTask = null;
             }
             pollingTimerTask = new TimerTask() {
                 public void run() {
                     accumulatedIdleTime = 0;
                     mailStore.fireRefreshRequired(false);
                 }
             };
             pollingTimer.schedule(pollingTimerTask, nextRefresh);
         }
     }
     
     private void cleanupIdleState() {
         synchronized(pollingTimer) {
             if(pollingTimerTask != null) {
                 pollingTimerTask.cancel();
                 pollingTimerTask = null;
             }
         }
         accumulatedIdleTime = 0;
     }
 }
