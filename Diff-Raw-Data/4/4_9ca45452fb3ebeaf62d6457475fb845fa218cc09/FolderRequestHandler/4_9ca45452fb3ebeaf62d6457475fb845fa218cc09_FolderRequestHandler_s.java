 /*-
  * Copyright (c) 2010, Derek Konigsberg
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
 package org.logicprobe.LogicMail.model;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import net.rim.device.api.collection.util.BigVector;
 import net.rim.device.api.util.Comparator;
 import net.rim.device.api.util.IntHashtable;
 import net.rim.device.api.util.IntVector;
 import net.rim.device.api.util.SimpleSortingIntVector;
 import net.rim.device.api.util.ToIntHashtable;
 
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.MailStoreRequestCallback;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.mail.NetworkMailStore;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.util.AtomicBoolean;
 
 /**
  * Handles folder-oriented requests for the mail store services layer.
  * <p>
  * While it is written to appear as generic as possible, it actually has two
  * somewhat protocol-specific logic flows.  If the protocol advertises support
  * for a mailbox-wide index map fetch (POP), then this is a two-part operation
  * consisting of an index map fetch and a message header fetch.  Otherwise
  * (IMAP) this is a three-part operation consisting of two flags fetches and
  * a message header fetch.
  * </p>
  */
 class FolderRequestHandler {
     private final NetworkMailStoreServices mailStoreServices;
     private final NetworkMailStore mailStore;
     private final FolderMessageCache folderMessageCache;
     private final FolderTreeItem folderTreeItem;
     
     /**
      * Flag to track whether a folder refresh is currently in progress.
      */
     private final AtomicBoolean refreshInProgress = new AtomicBoolean();
     
     /**
      * Indicates that the initial refresh has completed.
      */
     private boolean initialRefreshComplete;
     
     /** Indicates that cached messages have been loaded. */
     private boolean cacheLoaded;
     
     // For the two part flags refresh (IMAP) use case
     private Vector pendingFlagUpdates;
     private boolean secondaryFlagsRefresh;
     private Vector secondaryMessageTokensToFetch;
     
     /**
      * Set of messages that have been loaded from the cache, but no longer
      * exist on the server.
      */
     private final Hashtable orphanedMessageSet = new Hashtable();
     
     private Thread refreshThread;
     
     /**
      * Set if the mail store is disconnected, to indicate that local state
      * should be cleared prior to the next refresh request.
      */
     private volatile boolean cleanPriorToUse;
     
     public FolderRequestHandler(
             NetworkMailStoreServices mailStoreServices,
             NetworkMailStore mailStore,
             FolderMessageCache folderMessageCache,
             FolderTreeItem folderTreeItem) {
         
         this.mailStoreServices = mailStoreServices;
         this.mailStore = mailStore;
         this.folderMessageCache = folderMessageCache;
         this.folderTreeItem = folderTreeItem;
     }
 
     public void handleDisconnect() {
         cleanPriorToUse = true;
     }
     
     private void prepareForUse() {
         if(cleanPriorToUse) {
             initialRefreshComplete = false;
             secondaryFlagsRefresh = false;
             secondaryMessageTokensToFetch = null;
             pendingFlagUpdates = null;
             orphanedMessageSet.clear();
             refreshThread = null;
             cleanPriorToUse = false;
         }
     }
     
     public void requestFolderRefresh() {
         if(refreshInProgress.compareAndSet(false, true)) {
             prepareForUse();
             if(mailStore.hasLockedFolders() && initialRefreshComplete) {
                 // Subsequent refresh is pointless on locked-folder mail stores
                 refreshInProgress.set(false);
                 return;
             }
             
             refreshThread = new Thread() { public void run() {
                 if(mailStore.hasFolderMessageIndexMap()) {
                     mailStore.requestFolderMessageIndexMap(folderTreeItem, new MailStoreRequestCallback() {
                         public void mailStoreRequestComplete() { }
                         public void mailStoreRequestFailed(Throwable exception) {
                             indexMapFetchFailed();
                         }});
                 }
                 else {
                     // Queue a request for new folder messages from the mail store
                     pendingFlagUpdates = new Vector();
                     mailStore.requestFolderMessagesRecent(folderTreeItem, true, new MailStoreRequestCallback() {
                         public void mailStoreRequestComplete() { }
                         public void mailStoreRequestFailed(Throwable exception) {
                             initialFlagsRefreshFailed();
                         }});
                 }
     
                 if(!initialRefreshComplete) {
                     // Fetch messages stored in cache
                     loadCachedFolderMessages();
                 }
     
                 // If a message index map was requested (POP), execution will
                 // resume in indexMapFetchXXXX().
                 // Otherwise (IMAP), execution will resume in
                 // initialFlagsRefreshXXXX().
             }};
             refreshThread.start();
         }
     }
 
     private void loadCachedFolderMessages() {
         boolean dispOrder = MailSettings.getInstance().getGlobalConfig().getDispOrder();
         FolderMessage[] messages = folderMessageCache.getFolderMessages(folderTreeItem);
         if(messages.length > 0) {
             // Add all the messages that have been loaded from the
             // cache.  Server-side messages will be removed from the
             // set later on.
             for(int i=0; i<messages.length; i++) {
                 orphanedMessageSet.put(messages[i].getMessageToken().getMessageUid(), messages[i]);
             }
             
             // If the cached messages have already been loaded, then we can
             // skip notifying mail store listeners.  However, we still have to
             // add them to the orphan set, as seen above.
             if(!cacheLoaded) {
                 if(dispOrder) {
                     for(int i=0; i<messages.length; i+=5) {
                         int endIndex = Math.min(i + 5, messages.length);
                         FolderMessage[] subset = new FolderMessage[endIndex - i];
                         for(int j=0; j<subset.length; j++) {
                             subset[j] = messages[i + j];
                         }
                         mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, subset, false, false);
                     }
                 }
                 else {
                     for(int i=messages.length-1; i >= 0; i-=5) {
                        int startIndex = Math.max(i - 5, 0);
                        FolderMessage[] subset = new FolderMessage[i - startIndex];
                         for(int j=0; j<subset.length; j++) {
                             subset[j] = messages[i - j];
                         }
                         mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, subset, false, false);
                     }
                 }
                 cacheLoaded = true;
             }
         }
     }    
     
     private void indexMapFetchFailed() {
         // Initial fetch failed.  Since the index map retrieval is implemented
         // as a single operation, we cannot do anything more beyond cleanup.
         
         (new Thread() { public void run() {
             // Make sure the cache loading thread finishes
             joinRefreshThread();
             
             // Clear the set of loaded messages, since we cannot process them further
             orphanedMessageSet.clear();
             
             refreshInProgress.set(false);
         }}).start();
     }
     
     private void indexMapFetchComplete(final ToIntHashtable uidIndexMap) {
         final int initialMessageLimit = mailStore.getAccountConfig().getInitialFolderMessages();
         final int messageRetentionLimit = mailStore.getAccountConfig().getMaximumFolderMessages();
         
         (new Thread() { public void run() {
             joinRefreshThread();
             
             Vector messagesUpdated = new Vector();
             SimpleSortingIntVector indexVector = new SimpleSortingIntVector();
             IntHashtable cachedIndexToMessageMap = new IntHashtable();
             
             // Iterate through the UID-to-index map, and do the following:
             // - Remove cache-loaded messages from the orphan set if they exist on the server.
             // - Update index information for those messages that do still exist server-side.
             // - Build a sortable vector of index values
             Enumeration e = uidIndexMap.keys();
             while(e.hasMoreElements()) {
                 String uid = (String)e.nextElement();
                 int index = uidIndexMap.get(uid);
                 indexVector.addElement(index);
                 
                 FolderMessage message = (FolderMessage)orphanedMessageSet.remove(uid);
                 if(message != null) {
                     message.setIndex(index);
                     message.getMessageToken().updateMessageIndex(index);
                     
                     if(folderMessageCache.updateFolderMessage(folderTreeItem, message)) {
                         messagesUpdated.addElement(message);
                         cachedIndexToMessageMap.put(index, message);
                     }
                 }
             }
             indexVector.reSort(SimpleSortingIntVector.SORT_TYPE_NUMERIC);
             
             notifyMessageFlagUpdates(messagesUpdated);
             removeOrphanedMessages();
 
             // Determine the fetch range
             int size = indexVector.size();
             if(size == 0) { return; }
             int fetchRangeStart = Math.max(0, size - initialMessageLimit);
             
             // Build a list of indices to fetch
             IntVector messagesToFetch = new IntVector();
             for(int i=indexVector.size() - 1; i >= fetchRangeStart; --i) {
                 int index = indexVector.elementAt(i);
                 if(!cachedIndexToMessageMap.containsKey(index)) {
                     messagesToFetch.addElement(index);
                 }
             }
 
             int additionalMessageLimit = messageRetentionLimit - initialMessageLimit;
             for(int i=fetchRangeStart - 1; i >= 0; --i) {
                 if(additionalMessageLimit > 0) {
                     additionalMessageLimit--;
                 }
                 else {
                     // Beyond the limit, add these back to the orphan set
                     FolderMessage message = (FolderMessage)cachedIndexToMessageMap.get(indexVector.elementAt(i));
                     if(message != null) {
                         orphanedMessageSet.put(message.getMessageToken().getMessageUid(), message);
                     }
                 }
             }
             removeOrphanedMessages();
             
             // Do the final request for missing messages
             mailStore.requestFolderMessagesSet(folderTreeItem, messagesToFetch.toArray(), finalFetchCallback);
         }}).start();
     }
     
     private void initialFlagsRefreshFailed() {
         // Initial fetch failed.  Since flags retrieval is implemented as an
         // incremental operation, we can still apply any received flag updates
         // prior to cleaning up.  We cannot remove orphaned messages, however.
         
         (new Thread() { public void run() {
             // Make sure the cache loading thread finishes
             joinRefreshThread();
             
             Vector messagesUpdated = new Vector();
             int size = pendingFlagUpdates.size();
             for(int i=0; i<size; i++) {
                 FolderMessage message = (FolderMessage)pendingFlagUpdates.elementAt(i);
                 if(folderMessageCache.updateFolderMessage(folderTreeItem, message)) {
                     messagesUpdated.addElement(message);
                 }
             }
             pendingFlagUpdates.removeAllElements();
             notifyMessageFlagUpdates(messagesUpdated);
             
             // Clear the set of loaded messages, since we cannot process them further
             orphanedMessageSet.clear();
             
             refreshInProgress.set(false);
         }}).start();
     }
     
     private void initialFlagsRefreshComplete() {
         (new Thread() { public void run() {
             joinRefreshThread();
             
             Vector messagesUpdated = new Vector();
             secondaryMessageTokensToFetch = new Vector();
             MessageToken oldestFetchedToken = null;
             Comparator tokenComparator = null;
             
             // Iterate through the pending flag updates, doing the following:
             // - Remove messages from the orphan set that exist on the server
             // - Update the cache for fetched messages
             // - Build a collection of messages to provide update notifications for
             // - Build a collection of messages that need to be fetched
             // - Keep track of the oldest message in the update set
             int size = pendingFlagUpdates.size();
             for(int i=0; i<size; i++) {
                 FolderMessage message = (FolderMessage)pendingFlagUpdates.elementAt(i);
                 MessageToken token = message.getMessageToken();
                 
                 // Remove messages with received flag updates from the orphan set
                 orphanedMessageSet.remove(token.getMessageUid());
                 
                 if(folderMessageCache.updateFolderMessage(folderTreeItem, message)) {
                     messagesUpdated.addElement(message);
                 }
                 else {
                     secondaryMessageTokensToFetch.addElement(token);
                 }
                 
                 if(oldestFetchedToken == null) {
                     oldestFetchedToken = token;
                     tokenComparator = token.getComparator();
                 }
                 else {
                     if(tokenComparator.compare(token, oldestFetchedToken) < 0) {
                         oldestFetchedToken = token;
                     }
                 }
             }
             pendingFlagUpdates.removeAllElements();
             
             notifyMessageFlagUpdates(messagesUpdated);
             
             // Build a collection of messages in the cache that still need to be verified
             Vector cachedTokensToCheck = new Vector();
             if(oldestFetchedToken != null) {
                 Enumeration e = orphanedMessageSet.elements();
                 while(e.hasMoreElements()) {
                     MessageToken token = ((FolderMessage)e.nextElement()).getMessageToken();
                     if(tokenComparator.compare(token, oldestFetchedToken) < 0) {
                         cachedTokensToCheck.addElement(token);
                     }
                 }
             }
             
             if(cachedTokensToCheck.size() > 0) {
                 // Perform a second flags fetch
                 MessageToken[] tokens = new MessageToken[cachedTokensToCheck.size()];
                 cachedTokensToCheck.copyInto(tokens);
                 secondaryFlagsRefresh = true;
                 mailStore.requestFolderMessagesSet(folderTreeItem, tokens, true, new MailStoreRequestCallback() {
                     public void mailStoreRequestComplete() { }
                     public void mailStoreRequestFailed(Throwable exception) {
                         secondaryFlagsRefreshFailed();
                     }});
             }
             else {
                 removeOrphanedMessages();
                 finalFolderMessageFetch();
             }
         }}).start();
     }
     
     private void secondaryFlagsRefreshFailed() {
         // Secondary flags fetch failed.  We can still apply any received flag
         // updates prior to cleaning up, but we cannot remove orphaned messages.
 
         Vector messagesUpdated = new Vector();
         int size = pendingFlagUpdates.size();
         for(int i=0; i<size; i++) {
             FolderMessage message = (FolderMessage)pendingFlagUpdates.elementAt(i);
             if(folderMessageCache.updateFolderMessage(folderTreeItem, message)) {
                 messagesUpdated.addElement(message);
             }
         }
         pendingFlagUpdates.removeAllElements();
         notifyMessageFlagUpdates(messagesUpdated);
 
         // Clear the set of loaded messages, since we cannot process them further
         orphanedMessageSet.clear();
 
         refreshInProgress.set(false);
     }
     
     private void secondaryFlagsRefreshComplete() {
         int size = pendingFlagUpdates.size();
         BigVector messagesUpdated = new BigVector(size);
         Comparator folderMessageComparator = FolderMessage.getComparator();
         for(int i=0; i<size; i++) {
             FolderMessage message = (FolderMessage)pendingFlagUpdates.elementAt(i);
             MessageToken token = message.getMessageToken();
             
             // Remove messages with received flag updates from the orphan set
             orphanedMessageSet.remove(token.getMessageUid());
             
             if(folderMessageCache.updateFolderMessage(folderTreeItem, message)) {
                 messagesUpdated.insertElement(folderMessageComparator, message);
             }
         }
         pendingFlagUpdates.removeAllElements();
         
         // Determine the how many messages from this secondary set we can keep
         int initialMessageLimit = mailStore.getAccountConfig().getInitialFolderMessages();
         int messageRetentionLimit = mailStore.getAccountConfig().getMaximumFolderMessages();
         int additionalMessageLimit = messageRetentionLimit - initialMessageLimit;
         
         size = messagesUpdated.size();
         if(size > additionalMessageLimit) {
             // We have too many additional messages, so we need to prune the set
             messagesUpdated.optimize();
             Vector messagesToNotify = new Vector();
 
             int splitIndex = messagesUpdated.size() - additionalMessageLimit;
             for(int i=0; i<splitIndex; i++) {
                 FolderMessage message = (FolderMessage)messagesUpdated.elementAt(i);
                 orphanedMessageSet.put(message.getMessageToken().getMessageUid(), message);
             }
             for(int i=splitIndex; i<size; i++) {
                 messagesToNotify.addElement(messagesUpdated.elementAt(i));
             }
             
             notifyMessageFlagUpdates(messagesToNotify);
         }
         else {
             if(!messagesUpdated.isEmpty()) {
                 FolderMessage[] messages = new FolderMessage[messagesUpdated.size()];
                 messagesUpdated.copyInto(0, size, messages, 0);
                 mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, messages, true, true);
             }
         }
         
         removeOrphanedMessages();
         finalFolderMessageFetch();
     }
 
     private void finalFolderMessageFetch() {
         // Queue a fetch for messages missing from the cache
         if(!secondaryMessageTokensToFetch.isEmpty()) {
             MessageToken[] fetchArray = new MessageToken[secondaryMessageTokensToFetch.size()];
             secondaryMessageTokensToFetch.copyInto(fetchArray);
             secondaryMessageTokensToFetch.removeAllElements();
             mailStore.requestFolderMessagesSet(folderTreeItem, fetchArray, finalFetchCallback);
         }
         else {
             initialRefreshComplete = true;
             finalFetchComplete();
         }
     }
     
     private MailStoreRequestCallback finalFetchCallback = new MailStoreRequestCallback() {
         public void mailStoreRequestComplete() {
             initialRefreshComplete = true;
             finalFetchComplete();
         }
         public void mailStoreRequestFailed(Throwable exception) {
             finalFetchComplete();
         }
     };
 
     /**
      * The final fetch has no difference in handling depending on success or
      * failure.  This is due to the lack of any follow-up requests.  In both
      * cases we commit the cache, mark the refresh as complete, and notify
      * the listeners.
      */
     private void finalFetchComplete() {
         folderMessageCache.commit();
         refreshInProgress.set(false);
         
         // Notify the end of the operation
         mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, null, false, false);
     }
     
     /**
      * Joins refresh thread, after which the cached messages have been loaded.
      */
     private void joinRefreshThread() {
         // Make sure the cache refresh is completed
         try {
             refreshThread.join();
         } catch (InterruptedException e) { }
         refreshThread = null;
     }    
     
     private void notifyMessageFlagUpdates(final Vector messagesUpdated) {
         if(!messagesUpdated.isEmpty()) {
             FolderMessage[] messages = new FolderMessage[messagesUpdated.size()];
             messagesUpdated.copyInto(messages);
             mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, messages, true, true);
         }
     }
 
     private void removeOrphanedMessages() {
         Enumeration e = orphanedMessageSet.elements();
         MessageToken[] orphanedTokens = new MessageToken[orphanedMessageSet.size()];
         int index = 0;
         while(e.hasMoreElements()) {
             FolderMessage message = (FolderMessage)e.nextElement();
             folderMessageCache.removeFolderMessage(folderTreeItem, message);
             orphanedTokens[index++] = message.getMessageToken();
         }
         orphanedMessageSet.clear();
         mailStoreServices.fireFolderExpunged(folderTreeItem, orphanedTokens);
     }
     
     public void requestMoreFolderMessages(MessageToken firstToken, int increment) {
         mailStore.requestFolderMessagesRange(folderTreeItem, firstToken, increment, new MailStoreRequestCallback() {
             public void mailStoreRequestComplete() { }
             public void mailStoreRequestFailed(Throwable exception) {
                 // Commit and notify even in cases of failure, to ensure that
                 // post-request operations can occur.
                 folderMessageCache.commit();
                 mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, null, false);
             }
         });
     }
     
     void handleFolderMessageFlagsAvailable(FolderMessage[] messages) {
         if(refreshInProgress.get()) {
             handleFolderMessageFlagsAvailableDuringRefresh(messages);
         }
         else {
             if(messages != null) {
                 for(int i=0; i<messages.length; i++) {
                     folderMessageCache.updateFolderMessage(folderTreeItem, messages[i]);
                 }
                 mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, messages, true);
             }
             else {
                 folderMessageCache.commit();
                 mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, messages, true);
             }
         }
     }
 
     private void handleFolderMessageFlagsAvailableDuringRefresh(FolderMessage[] messages) {
         if(messages != null && pendingFlagUpdates != null) {
             for(int i=0; i<messages.length; i++) {
                 pendingFlagUpdates.addElement(messages[i]);
             }
         }
         else {
             if(secondaryFlagsRefresh) {
                 secondaryFlagsRefresh = false;
                 secondaryFlagsRefreshComplete();
             }
             else {
                 initialFlagsRefreshComplete();
             }
         }
     }
     
     void handleFolderMessagesAvailable(FolderMessage[] messages) {
         if(messages != null) {
             for(int i=0; i<messages.length; i++) {
                 folderMessageCache.addFolderMessage(folderTreeItem, messages[i]);
             }
             mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, messages, false);
         }
         else if(!refreshInProgress.get()) {
             // If a refresh is in progress, the request callback will handle
             // the end of the operation.
             folderMessageCache.commit();
             mailStoreServices.fireFolderMessagesAvailable(folderTreeItem, messages, false);
         }
     }
     
     void handleFolderMessageIndexMapAvailable(ToIntHashtable uidIndexMap) {
         if(!refreshInProgress.get()) { return; }
         
         indexMapFetchComplete(uidIndexMap);
     }
     
     void handleFolderExpunged(int[] indices) {
         FolderMessage[] messages = folderMessageCache.getFolderMessages(folderTreeItem);
         for(int i=0; i<messages.length; i++) {
             if(messages[i].isDeleted()) {
                 folderMessageCache.removeFolderMessage(folderTreeItem, messages[i]);
             }
         }
         folderMessageCache.commit();
         mailStoreServices.fireFolderExpunged(folderTreeItem, indices);
     }
 
     void setFolderMessageSeen(MessageToken messageToken) {
         FolderMessage message = folderMessageCache.getFolderMessage(folderTreeItem, messageToken);
         MessageFlags messageFlags = message.getFlags();
         if(!messageFlags.isSeen()) {
             messageFlags.setSeen(true);
             messageFlags.setRecent(false);
             folderMessageCache.updateFolderMessage(folderTreeItem, message);
             folderMessageCache.commit();
             //TODO: Inform the server that the message is seen
         }
     }
 }
