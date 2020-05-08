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
 package org.logicprobe.LogicMail.model;
 
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.mail.AbstractMailSender;
 import org.logicprobe.LogicMail.mail.AbstractMailStore;
 import org.logicprobe.LogicMail.mail.FolderEvent;
 import org.logicprobe.LogicMail.mail.FolderListener;
 import org.logicprobe.LogicMail.mail.FolderMessagesEvent;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.MailSenderListener;
 import org.logicprobe.LogicMail.mail.MailStoreListener;
 import org.logicprobe.LogicMail.mail.MessageEvent;
 import org.logicprobe.LogicMail.mail.MessageListener;
 import org.logicprobe.LogicMail.mail.MessageSentEvent;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.mail.NetworkMailStore;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.util.DataStore;
 import org.logicprobe.LogicMail.util.DataStoreFactory;
 import org.logicprobe.LogicMail.util.EventListenerList;
 
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 
 /**
  * Account node for the mail data model.
  * This node contains only the root <tt>MailboxNode</tt> instance.
  * Currently the type of mail store backing this node is
  * determined by the constructor that is called.
  * Eventually a more elegant approach will need to
  * be implemented.
  */
 public class AccountNode implements Node {
     public final static int STATUS_LOCAL = 0;
     public final static int STATUS_OFFLINE = 1;
     public final static int STATUS_ONLINE = 2;
     private AbstractMailStore mailStore;
     private boolean usePersistedState;
     private AbstractMailSender mailSender;
     private MailRootNode parent;
     private MailboxNode rootMailbox;
     private Hashtable pathMailboxMap;
     private Object rootMailboxLock = new Object();
     private EventListenerList listenerList = new EventListenerList();
     private AccountConfig accountConfig;
     private int status;
     private boolean shutdown = false;
     private DataStore accountDataStore;
     private MailSenderListener mailSenderListener = new MailSenderListener() {
             public void messageSent(MessageSentEvent e) {
                 mailSender_MessageSent(e);
             }
         };
 
     /**
      * Construct a new node for a network account.
      *
      * @param accountConfig Account configuration.
      * @param loadState True to load the stored account state, false to construct fresh
      */
     AccountNode(AbstractMailStore mailStore) {
     	this(mailStore, true);
     }
     
     /**
      * Construct a new node for a network account.
      *
      * @param accountConfig Account configuration.
      * @param usePersistedState True to use persisted account state, false otherwise
      */
     AccountNode(AbstractMailStore mailStore, boolean usePersistedState) {
         this.rootMailbox = null;
         this.pathMailboxMap = new Hashtable();
 
         this.mailStore = mailStore;
         this.usePersistedState = usePersistedState;
 
         if (!mailStore.isLocal() && mailStore instanceof NetworkMailStore) {
             this.accountConfig = ((NetworkMailStore) mailStore).getAccountConfig();
             this.status = STATUS_OFFLINE;
         } else {
             this.status = STATUS_LOCAL;
         }
 
         this.mailStore.addMailStoreListener(new MailStoreListener() {
                 public void folderTreeUpdated(FolderEvent e) {
                     mailStore_FolderTreeUpdated(e);
                 }
             });
 
         this.mailStore.addFolderListener(new FolderListener() {
                 public void folderStatusChanged(FolderEvent e) {
                     mailStore_FolderStatusChanged(e);
                 }
 
                 public void folderMessagesAvailable(FolderMessagesEvent e) {
                     mailStore_FolderMessagesAvailable(e);
                 }
             });
 
         this.mailStore.addMessageListener(new MessageListener() {
                 public void messageAvailable(MessageEvent e) {
                     mailStore_messageAvailable(e);
                 }
 
                 public void messageFlagsChanged(MessageEvent e) {
                     mailStore_messageFlagsChanged(e);
                 }
 
                 public void messageDeleted(MessageEvent e) {
                     mailStore_messageDeleted(e);
                 }
 
                 public void messageUndeleted(MessageEvent e) {
                     mailStore_messageUndeleted(e);
                 }
             });
 
         if (!mailStore.hasFolders()) {
             // Create the fake INBOX node for non-folder-capable mail stores
             this.rootMailbox = new MailboxNode(new FolderTreeItem("", "", ""),
             		false,
                     -1);
             this.rootMailbox.setParentAccount(this);
 
             MailboxNode inboxNode = new MailboxNode(new FolderTreeItem(
                         "INBOX", "INBOX", ""), false, MailboxNode.TYPE_INBOX);
             inboxNode.setParentAccount(this);
             this.rootMailbox.addMailbox(inboxNode);
             pathMailboxMap.put("INBOX", inboxNode);
         }
 
         // Load any saved tree data
         if(usePersistedState) { load(); }
     }
 
     public void accept(NodeVisitor visitor) {
         visitor.visit(this);
     }
 
     /**
      * Gets the mail sender associated with this account.
      *
      * @return The mail sender.
      */
     AbstractMailSender getMailSender() {
         return this.mailSender;
     }
 
     /**
      * Sets the mail sender associated with this account.
      * This is not set in the constructor since it can change
      * whenever account configuration changes.
      *
      * @param mailSender The mail sender.
      */
     void setMailSender(AbstractMailSender mailSender) {
         if ((this.mailSender != null) && (mailSender == null)) {
             this.mailSender.removeMailSenderListener(mailSenderListener);
             this.mailSender = null;
         } else if ((this.mailSender != null) &&
                 (this.mailSender != mailSender)) {
             this.mailSender.removeMailSenderListener(mailSenderListener);
             this.mailSender = mailSender;
             this.mailSender.addMailSenderListener(mailSenderListener);
         } else if ((this.mailSender == null) && (mailSender != null)) {
             this.mailSender = mailSender;
             this.mailSender.addMailSenderListener(mailSenderListener);
         }
     }
 
     /**
      * Returns whether this account has a mail sender associated with it.
      *
      * @return True if mail can be sent, false otherwise.
      */
     public boolean hasMailSender() {
         return (this.mailSender != null);
     }
 
     /**
      * Sets the root node which is the parent of this account.
      *
      * @param parent The root node.
      */
     void setParent(MailRootNode parent) {
         this.parent = parent;
     }
 
     /**
      * Gets the root node which is the parent of this account.
      *
      * @return The root node.
      */
     public MailRootNode getParent() {
         return this.parent;
     }
 
     /**
      * Get the top-level mailbox contained within this account.
      * This mailbox typically exists only for the purpose of
      * containing other mailboxes, and is not normally shown
      * to the user.
      *
      * @return Root mailbox node.
      */
     public MailboxNode getRootMailbox() {
         synchronized (rootMailboxLock) {
             return this.rootMailbox;
         }
     }
 
     /**
      * Gets the name of this account.
      *
      * @return The name.
      */
     public String toString() {
         if (accountConfig != null) {
             return this.accountConfig.toString();
         } else {
             return "Local Folders";
         }
     }
 
     /**
      * Gets the account configuration.
      *
      * @return The account configuration, or null for local accounts.
      */
     public AccountConfig getAccountConfig() {
         return this.accountConfig;
     }
 
     /**
      * Gets the mail store associated with this account.
      *
      * @return The mail store.
      */
     AbstractMailStore getMailStore() {
         return this.mailStore;
     }
 
     /**
      * Sets the status of this account.
      *
      * @param status The status.
      */
     void setStatus(int status) {
         if (this.status != status) {
             this.status = status;
 
             if ((this.status == STATUS_OFFLINE) && !shutdown &&
                     mailStore instanceof NetworkMailStore) {
                 ((NetworkMailStore) mailStore).restart();
             }
 
             fireAccountStatusChanged(AccountNodeEvent.TYPE_CONNECTION);
         }
     }
 
     /**
      * Gets the status of this account.
      *
      * @return The status.
      */
     public int getStatus() {
         return this.status;
     }
 
     /**
      * Requests that this account be disconnected.
      * <p>
      * This method is only valid for the <tt>STATUS_ONLINE<tt>
      * status, and will do nothing in any other state.
      * </p>
      *
      * @param shutdown True if the application is closing, false if the mail store should be usable again.
      */
     public void requestDisconnect(boolean shutdown) {
         if ((status == STATUS_ONLINE) && mailStore instanceof NetworkMailStore) {
             ((NetworkMailStore) mailStore).shutdown(false);
             this.shutdown = shutdown;
         }
     }
 
     /**
      * Gets whether this account supports folders.
      * If folders are not supported, then this account will automatically
      * present a single "INBOX" folder.  However, no other folder-related
      * operations will have any relevance.
      *
      * @return True if supported, false otherwise.
      */
     public boolean hasFolders() {
         return this.mailStore.hasFolders();
     }
 
     /**
      * Gets whether this account supports undelete.
      *
      * @return True if supported, false otherwise.
      */
     public boolean hasUndelete() {
         return this.mailStore.hasUndelete();
     }
 
     /**
      * Called to trigger a refresh of the mailboxes under
      * this account.  Completion is signaled by an
      * AccountStatusChanged event.
      */
     public void refreshMailboxes() {
         if (mailStore.hasFolders()) {
             mailStore.requestFolderTree();
         }
     }
 
     /**
      * Called to trigger a refresh of message count status
      * for mailboxes under this account.  Completion is
      * signaled by MailboxStatusChanged events on the
      * updated mailboxes.
      */
     public void refreshMailboxStatus() {
         int size = pathMailboxMap.size();
         FolderTreeItem[] folders = new FolderTreeItem[size];
         Enumeration e = pathMailboxMap.keys();
 
         for (int i = 0; i < size; i++) {
             folders[i] = ((MailboxNode) pathMailboxMap.get(e.nextElement())).getFolderTreeItem();
         }
 
         mailStore.requestFolderStatus(folders);
     }
 
     /**
      * Sends a message from this account.
      *
      * @param envelope Envelope of the message to send
      * @param message Message to send.
      */
     public void sendMessage(MessageEnvelope envelope, Message message) {
         if (mailSender != null) {
         	// Construct an outgoing message node
         	FolderMessage outgoingFolderMessage = new FolderMessage(null, envelope, -1, -1);
         	outgoingFolderMessage.setSeen(false);
         	outgoingFolderMessage.setRecent(true);
         	OutgoingMessageNode outgoingMessage =
         		new OutgoingMessageNode(
         				outgoingFolderMessage,
         				this, mailSender);
         	
         	outgoingMessage.setMessageStructure(message.getStructure());
         	outgoingMessage.putMessageContent(message.getAllContent());
         	MailManager.getInstance().getOutboxMailboxNode().addMessage(outgoingMessage);
         }
     }
 
     /**
      * Sends a reply message from this account.
      *
      * @param envelope Envelope of the message to send
      * @param message Message to send.
      * @param originalMessageNode Message node this was in reply to.
      */
     public void sendMessageReply(MessageEnvelope envelope, Message message,
         MessageNode originalMessageNode) {
         if (mailSender != null) {
         	// Construct an outgoing message node
         	FolderMessage outgoingFolderMessage = new FolderMessage(null, envelope, -1, -1);
         	outgoingFolderMessage.setSeen(false);
         	outgoingFolderMessage.setRecent(true);
         	OutgoingMessageNode outgoingMessage =
         		new OutgoingMessageNode(
         				outgoingFolderMessage,
         				this, mailSender, originalMessageNode);
         	outgoingMessage.setMessageStructure(message.getStructure());
         	outgoingMessage.putMessageContent(message.getAllContent());
         	MailManager.getInstance().getOutboxMailboxNode().addMessage(outgoingMessage);
         }
     }
 
     /**
      * Handles folder tree updates.
      *
      * @param e Event data.
      */
     private void mailStore_FolderTreeUpdated(FolderEvent e) {
         FolderTreeItem rootFolder = e.getFolder();
 
         synchronized (rootMailboxLock) {
             Hashtable remainingMailboxMap = new Hashtable();
 
             if (rootMailbox != null) {
                 // Disassemble the model tree into a flat collection of nodes
                 Vector flatMailboxes = new Vector();
                 populateFlatMailboxes(flatMailboxes, rootMailbox);
                 rootMailbox = null;
 
                 // Prune the collection to only include nodes that are still valid,
                 // and make them reference the new FolderTreeItem objects.
                 Hashtable folderPathMap = new Hashtable();
                 populateFolderPathMap(folderPathMap, rootFolder);
 
                 int size = flatMailboxes.size();
 
                 for (int i = 0; i < size; i++) {
                     MailboxNode mailboxNode = (MailboxNode) flatMailboxes.elementAt(i);
                     String path = mailboxNode.getFolderTreeItem().getPath();
 
                     if (folderPathMap.containsKey(path)) {
                         mailboxNode.setFolderTreeItem((FolderTreeItem) folderPathMap.get(
                                 path));
                         remainingMailboxMap.put(path, mailboxNode);
                     }
                 }
             }
 
             // Build a new tree from the FolderTreeItem, using the collected
             // nodes where possible, and new nodes when necessary.
             this.pathMailboxMap.clear();
             this.rootMailbox = new MailboxNode(rootFolder, false, -1);
             populateMailboxNodes(rootFolder, rootMailbox, remainingMailboxMap);
         }
 
         if(usePersistedState) { save(); }
         fireAccountStatusChanged(AccountNodeEvent.TYPE_MAILBOX_TREE);
     }
 
     private void populateFlatMailboxes(Vector flatMailboxes,
         MailboxNode currentMailbox) {
         flatMailboxes.addElement(currentMailbox);
 
         MailboxNode[] childNodes = currentMailbox.getMailboxes();
 
         for (int i = 0; i < childNodes.length; i++) {
             populateFlatMailboxes(flatMailboxes, childNodes[i]);
         }
 
         currentMailbox.clearMailboxes();
     }
 
     private void populateFolderPathMap(Hashtable folderPathMap,
         FolderTreeItem folderTreeItem) {
         if (folderTreeItem != null) {
             folderPathMap.put(folderTreeItem.getPath(), folderTreeItem);
 
             if (folderTreeItem.hasChildren()) {
                 FolderTreeItem[] children = folderTreeItem.children();
 
                 for (int i = 0; i < children.length; i++) {
                     populateFolderPathMap(folderPathMap, children[i]);
                 }
             }
         }
     }
 
     private void populateMailboxNodes(FolderTreeItem folderTreeItem,
         MailboxNode currentMailbox, Hashtable remainingMailboxMap) {
         pathMailboxMap.put(folderTreeItem.getPath(), currentMailbox);
 
         if (folderTreeItem.hasChildren()) {
             FolderTreeItem[] folderTreeItemChildren = folderTreeItem.children();
 
             for (int i = 0; i < folderTreeItemChildren.length; i++) {
                 MailboxNode childMailbox;
 
                 if (remainingMailboxMap.containsKey(
                             folderTreeItemChildren[i].getPath())) {
                     childMailbox = (MailboxNode) remainingMailboxMap.get(folderTreeItemChildren[i].getPath());
                 } else {
                 	int mailboxType = getMailboxType(folderTreeItemChildren[i]);
                 	if(mailboxType == MailboxNode.TYPE_OUTBOX) {
 	                    childMailbox = new OutboxMailboxNode(folderTreeItemChildren[i]);
                 	}
                 	else {
 	                    childMailbox = new MailboxNode(folderTreeItemChildren[i],
 	                    		folderTreeItemChildren[i].isAppendable(),
 	                    		mailboxType);
                 	}
                     childMailbox.setParentAccount(this);
                 }
 
                 populateMailboxNodes(folderTreeItemChildren[i], childMailbox,
                     remainingMailboxMap);
                 currentMailbox.addMailbox(childMailbox);
             }
         }
     }
 
     /**
      * Handles folder status changes.
      *
      * @param e Event data.
      */
     private void mailStore_FolderStatusChanged(FolderEvent e) {
         updateMailboxStatus(e.getFolder());
     }
 
     /**
      * Recursively update mailbox status.
      *
      * @param currentFolder Folder item to start from.
      */
     private void updateMailboxStatus(FolderTreeItem currentFolder) {
         MailboxNode mailboxNode = (MailboxNode) pathMailboxMap.get(currentFolder.getPath());
 
         if (mailboxNode != null) {
             FolderTreeItem mailboxFolder = mailboxNode.getFolderTreeItem();
             mailboxFolder.setMsgCount(currentFolder.getMsgCount());
             mailboxFolder.setUnseenCount(currentFolder.getUnseenCount());
             mailboxNode.fireMailboxStatusChanged(MailboxNodeEvent.TYPE_STATUS,
                 null);
         }
 
         if (currentFolder.hasChildren()) {
             FolderTreeItem[] children = currentFolder.children();
 
             for (int i = 0; i < children.length; i++) {
                 updateMailboxStatus(children[i]);
             }
         }
     }
 
     /**
      * Handles folder messages becoming available.
      *
      * @param e Event data.
      */
     private void mailStore_FolderMessagesAvailable(FolderMessagesEvent e) {
         if (e.getMessages() != null) {
             // Find the MailboxNode that this event applies to.
             // If none apply, then shortcut out of here.
             if (!pathMailboxMap.containsKey(e.getFolder().getPath())) {
                 return;
             }
 
             MailboxNode mailboxNode = (MailboxNode) pathMailboxMap.get(e.getFolder().getPath());
 
             // Determine what MessageNodes need to be created, and add them.
             FolderMessage[] folderMessages = e.getMessages();
             Vector addedMessages = new Vector();
 
             for (int i = 0; i < folderMessages.length; i++) {
                 addedMessages.addElement(new MessageNode(folderMessages[i]));
             }
 
             MessageNode[] addedMessagesArray = new MessageNode[addedMessages.size()];
             addedMessages.copyInto(addedMessagesArray);
             mailboxNode.addMessages(addedMessagesArray);
         }
     }
 
     /**
      * Handles a message being loaded.
      *
      * @param e Event data.
      */
     private void mailStore_messageAvailable(MessageEvent e) {
         MessageNode messageNode = findMessageForToken(e.getMessageToken());
 
         if (messageNode != null) {
         	switch(e.getType()) {
         	case MessageEvent.TYPE_FULLY_LOADED:
                 messageNode.setMessageStructure(e.getMessageStructure());
                 messageNode.setMessageSource(e.getMessageSource());
                 messageNode.putMessageContent(e.getMessageContent());
                 break;
         	case MessageEvent.TYPE_CONTENT_LOADED:
                 messageNode.putMessageContent(e.getMessageContent());
                 break;
         	}
         }
     }
 
     /**
      * Handles a message flags changing.
      *
      * @param e Event data.
      */
     private void mailStore_messageFlagsChanged(MessageEvent e) {
         MessageNode messageNode = findMessageForToken(e.getMessageToken());
 
         if (messageNode != null) {
        	messageNode.setFlags(MessageNode.convertMessageFlags(e.getMessageFlags()));
             messageNode.fireMessageStatusChanged(MessageNodeEvent.TYPE_FLAGS);
         }
     }
 
     /**
      * Handles a message being deleted.
      *
      * @param e Event data.
      */
     private void mailStore_messageDeleted(MessageEvent e) {
         MessageNode messageNode = findMessageForToken(e.getMessageToken());
 
         if (messageNode != null) {
        	messageNode.setFlags(MessageNode.convertMessageFlags(e.getMessageFlags()));
             messageNode.fireMessageStatusChanged(MessageNodeEvent.TYPE_FLAGS);
         }
     }
 
     /**
      * Handles a message being undeleted.
      *
      * @param e Event data.
      */
     private void mailStore_messageUndeleted(MessageEvent e) {
         MessageNode messageNode = findMessageForToken(e.getMessageToken());
 
         if (messageNode != null) {
        	messageNode.setFlags(MessageNode.convertMessageFlags(e.getMessageFlags()));
             messageNode.fireMessageStatusChanged(MessageNodeEvent.TYPE_FLAGS);
         }
     }
 
     /**
      * Finds the message node matching a particular token.
      *
      * @param messageToken
      * @return Message node, or null if none was found.
      */
     private MessageNode findMessageForToken(MessageToken messageToken) {
     	MailboxNode mailboxNode = null;
     	Enumeration e = pathMailboxMap.elements();
     	while(e.hasMoreElements()) {
     		MailboxNode currentMailbox = (MailboxNode)e.nextElement();
     		if(messageToken.containedWithin(currentMailbox.getFolderTreeItem())) {
     			mailboxNode = currentMailbox;
     			break;
     		}
     	}
     	
         if (mailboxNode != null) {
             // Change this to use the a real tag object once implemented
             return mailboxNode.getMessageByToken(messageToken);
         } else {
             return null;
         }
     }
 
     
     /**
      * Handles a message being sent.
      *
      * @param e Event data.
      */
     private void mailSender_MessageSent(MessageSentEvent e) {
 //            if ((sentMailbox != null) && mailStore.hasAppend() && sentMailbox.hasAppend()) {
 //                MessageFlags initialFlags = new MessageFlags();
 //                initialFlags.setSeen(true);
 //                sentMailbox.appendRawMessage(e.getMessageSource(), initialFlags);
 //            }
 //
 //            // Update flags if necessary
 //            if ((repliedMessageNode != null) && mailStore.hasFlags()) {
 //                mailStore.requestMessageAnswered(repliedMessageNode.getParent().getFolderTreeItem(),
 //                    repliedMessageNode.getFolderMessage());
 //            }
 //        }
     }
 
     /**
      * Attempts to determine the folder type based on its name,
      * and any configuration options.
      * <p>
      * This approach is only necessary to for local folders and general
      * defaults.  Explicitly configured special folders have their type set
      * later in the account loading process.
      * </p>
      * @param folderTreeItem Source folder tree item.
      * @return Mailbox type
      */
     private int getMailboxType(FolderTreeItem folderTreeItem) {
     	int mailboxType = MailboxNode.TYPE_NORMAL;
         if (folderTreeItem.getPath().equalsIgnoreCase("INBOX")) {
         	mailboxType = MailboxNode.TYPE_INBOX;
         }
         else if(status == STATUS_LOCAL) {
         	String path = folderTreeItem.getPath();
             if (path.equalsIgnoreCase("Outbox")) {
             	mailboxType = MailboxNode.TYPE_OUTBOX;
             }
             else if (path.equalsIgnoreCase("Drafts")) {
             	mailboxType = MailboxNode.TYPE_DRAFTS;
             }
             else if (path.equalsIgnoreCase("Sent")) {
             	mailboxType = MailboxNode.TYPE_SENT;
             }
             else if (path.equalsIgnoreCase("Trash")) {
             	mailboxType = MailboxNode.TYPE_TRASH;
             }
         }
         return mailboxType;
     }
 
     /**
     * Adds a <tt>AccountNodeListener</tt> to the account node.
     *
     * @param l The <tt>AccountNodeListener</tt> to be added.
     */
     public void addAccountNodeListener(AccountNodeListener l) {
         listenerList.add(AccountNodeListener.class, l);
     }
 
     /**
      * Removes a <tt>AccountNodeListener</tt> from the account node.
      *
      * @param l The <tt>AccountNodeListener</tt> to be removed.
      */
     public void removeAccountNodeListener(AccountNodeListener l) {
         listenerList.remove(AccountNodeListener.class, l);
     }
 
     /**
      * Returns an array of all <tt>AccountNodeListener</tt>s
      * that have been added to this account node.
      *
      * @return All the <tt>AccountNodeListener</tt>s that have been added,
      * or an empty array if no listeners have been added.
      */
     public AccountNodeListener[] getAccountNodeListeners() {
         return (AccountNodeListener[]) listenerList.getListeners(AccountNodeListener.class);
     }
 
     /**
      * Notifies all registered <tt>AccountNodeListener</tt>s that
      * the account status has changed.
      *
      * @param type Event type.
      */
     protected void fireAccountStatusChanged(int type) {
         Object[] listeners = listenerList.getListeners(AccountNodeListener.class);
         AccountNodeEvent e = null;
 
         for (int i = 0; i < listeners.length; i++) {
             if (e == null) {
                 e = new AccountNodeEvent(this, type);
             }
 
             ((AccountNodeListener) listeners[i]).accountStatusChanged(e);
         }
     }
 
     /**
      * Saves the mailbox tree to persistent storage.
      */
     private void save() {
         if (accountDataStore == null) {
             accountDataStore = DataStoreFactory.getConnectionCacheStore();
         }
 
         if(mailStore.isLocal()) {
         	accountDataStore.putNamedObject("LocalMailStore", rootMailbox);
         }
         else {
         	accountDataStore.putNamedObject(Long.toString(accountConfig.getUniqueId()), rootMailbox);
         }
         accountDataStore.save();
     }
 
     /**
      * Loads the mailbox tree from persistent storage.
      */
     private void load() {
         if (accountDataStore == null) {
             accountDataStore = DataStoreFactory.getConnectionCacheStore();
         }
         
         Object loadedObject;
         if(mailStore.isLocal()) {
 	        loadedObject = accountDataStore.getNamedObject("LocalMailStore");
         	
         }
         else {
 	        loadedObject = accountDataStore.getNamedObject(Long.toString(accountConfig.getUniqueId()));
         }
         
         if (loadedObject instanceof MailboxNode) {
             synchronized (rootMailboxLock) {
                 this.rootMailbox = (MailboxNode) loadedObject;
                 this.rootMailbox.setParentAccount(this);
                 prepareDeserializedMailboxNode(rootMailbox);
             }
         }
     }
 
     /**
      * Clear any persistent data associated with this account node.
      *
      * <p>When this account node removed from the model tree
      * because the underlying account has been deleted,
      * this method needs to be called to ensure that
      * persistent data does not linger on the device.
      */
     public void removeSavedData() {
         if (accountDataStore != null) {
             accountDataStore.delete();
             accountDataStore = null;
         }
     }
 
     /**
      * Traverses the deserialized mailbox nodes, populates any necessary
      * data structures in the account node, and sets the mailbox parent
      * account references.
      *
      * @param mailboxNode The mailbox node.
      */
     private void prepareDeserializedMailboxNode(MailboxNode mailboxNode) {
         mailboxNode.setParentAccount(this);
 
         FolderTreeItem item = mailboxNode.getFolderTreeItem();
 
         if ((item != null) && (item.getPath().length() > 0)) {
             this.pathMailboxMap.put(item.getPath(), mailboxNode);
         }
 
         MailboxNode[] children = mailboxNode.getMailboxes();
 
         for (int i = 0; i < children.length; i++) {
             prepareDeserializedMailboxNode(children[i]);
         }
     }
 }
