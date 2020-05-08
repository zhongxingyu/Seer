 /*-
  * Copyright (c) 2006, Derek Konigsberg
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
 
 package org.logicprobe.LogicMail.mail.imap;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import net.rim.device.api.system.EventLogger;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.conf.ConnectionConfig;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.conf.ImapConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.conf.MailSettingsEvent;
 import org.logicprobe.LogicMail.conf.MailSettingsListener;
 import org.logicprobe.LogicMail.mail.AbstractIncomingMailClient;
 import org.logicprobe.LogicMail.mail.FolderMessageCallback;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.MailException;
 import org.logicprobe.LogicMail.mail.MailProgressHandler;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.mail.imap.ImapProtocol.FetchEnvelopeResponse;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.MimeMessageContentFactory;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.message.MimeMessagePartFactory;
 import org.logicprobe.LogicMail.message.MultiPart;
 import org.logicprobe.LogicMail.message.UnsupportedContentException;
 import org.logicprobe.LogicMail.message.UnsupportedPart;
 import org.logicprobe.LogicMail.util.Connection;
 import org.logicprobe.LogicMail.util.NetworkConnector;
 import org.logicprobe.LogicMail.util.DataStore;
 import org.logicprobe.LogicMail.util.DataStoreFactory;
 
 /**
  * 
  * Implements the IMAP client
  * 
  */
 public class ImapClient extends AbstractIncomingMailClient {
     private final NetworkConnector networkConnector;
     private final MailSettings mailSettings;
     private final ImapConfig accountConfig;
     private final ImapProtocol imapProtocol;
     private Connection connection;
     private String username;
     private String password;
     private boolean openStarted;
 
     /**
      * Table of supported server capabilities
      */
     private Hashtable capabilities;
 
     /**
      * Delimiter between folder names in the hierarchy
      */
     private String folderDelim = "";
 
     /**
      * Personal namespace from the IMAP server
      */
     private ImapProtocol.Namespace nsPersonal;
 
     /**
      * Active mailbox path, so most commands do not need
      * to take a mailbox path parameter.  This makes it easier
      * to provide a common front-end that still works for
      * protocols that do not support a mailbox hierarchy.
      */
     private FolderTreeItem activeMailbox = null;
 
     /**
      * Special INBOX mailbox reference.
      */
     private FolderTreeItem inboxMailbox = null;
 
     /**
      * Seen mailboxes, used to track whether fetching new
      * messages should be based on a limited range, or
      * the UIDNEXT parameter.
      */
     private final Hashtable seenMailboxes = new Hashtable();
 
     /**
      * Known mailboxes, used to track protocol-specific
      * information on mailboxes that is not exposed
      * to other classes.
      */
     private final Hashtable knownMailboxes = new Hashtable();
 
     private static String INBOX = "INBOX";
     private static String CAPABILITY_CHILDREN = "CHILDREN";
     private static String CAPABILITY_NAMESPACE = "NAMESPACE";
     private static String CAPABILITY_STARTTLS = "STARTTLS";
     private static String CAPABILITY_IDLE = "IDLE";
     
     public ImapClient(NetworkConnector networkConnector, GlobalConfig globalConfig, ImapConfig accountConfig) {
         this.networkConnector = networkConnector;
         this.accountConfig = accountConfig;
         this.mailSettings = MailSettings.getInstance();
         imapProtocol = new ImapProtocol();
         username = accountConfig.getServerUser();
         password = accountConfig.getServerPass();
         openStarted = false;
         mailSettings.addMailSettingsListener(mailSettingsListener);
     }
 
     private MailSettingsListener mailSettingsListener = new MailSettingsListener() {
         public void mailSettingsSaved(MailSettingsEvent e) {
             mailSettings_MailSettingsSaved(e);
         }
     };
 
     private void mailSettings_MailSettingsSaved(MailSettingsEvent e) {
         // Check for a list change, where we no longer exist afterwards
         if((e.getListChange() & MailSettingsEvent.LIST_CHANGED_ACCOUNT) != 0
                 && !mailSettings.containsAccountConfig(accountConfig)) {
             // We have been deleted, so unregister to make sure we
             // no longer affect the system and can be garbage collected.
             mailSettings.removeMailSettingsListener(mailSettingsListener);
         }
 
         // Check for a change to the global or account network settings
         if((e.getGlobalChange() & GlobalConfig.CHANGE_TYPE_NETWORK) != 0
                 || (e.getConfigChange(accountConfig) & AccountConfig.CHANGE_TYPE_CONNECTION) != 0) {
             // Refresh authentication information from the configuration
             username = accountConfig.getServerUser();
             password = accountConfig.getServerPass();
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#open()
      */
     public boolean open() throws IOException, MailException {
         try {
             if(!openStarted) {
                 connection = networkConnector.open(accountConfig);
                 imapProtocol.setConnection(connection);
                 
                 activeMailbox = null;
 
                 // Swallow the initial "* OK" line from the server
                 connection.receive();
                 
                 // Find out server capabilities
                 capabilities = imapProtocol.executeCapability();
 
                 openStarted = true;
             }
 
             // TLS initialization
             int serverSecurity = accountConfig.getServerSecurity();
             if((serverSecurity == ConnectionConfig.SECURITY_TLS_IF_AVAILABLE && capabilities.containsKey(CAPABILITY_STARTTLS))
                     || (serverSecurity == ConnectionConfig.SECURITY_TLS)) {
                 imapProtocol.executeStartTLS();
                 connection = networkConnector.getConnectionAsTLS(connection);
                 imapProtocol.setConnection(connection);
             }
 
             // Authenticate with the server
             if(!imapProtocol.executeLogin(username, password)) {
                 return false;
             }
 
             // Get the namespaces, if supported
             if(capabilities.containsKey(CAPABILITY_NAMESPACE)) {
                 ImapProtocol.NamespaceResponse nsResponse = imapProtocol.executeNamespace();
 
                 if(nsResponse.personal != null &&
                         nsResponse.personal.length > 0 &&
                         nsResponse.personal[0] != null &&
                         nsResponse.personal[0].delimiter != null &&
                         nsResponse.personal[0].prefix != null) {
                     // We got a valid personal namespace, so proceed
                     nsPersonal = nsResponse.personal[0];
                     folderDelim = nsPersonal.delimiter;
                 }
             }
             // We could not get valid personal namespace information,
             // so the folder delimiter will be acquired differently.
             if(nsPersonal == null) {
                 // Discover folder delimiter
                 Vector resp = imapProtocol.executeList("", "", null);
                 if(resp.size() > 0) {
                     folderDelim = ((ImapProtocol.ListResponse)resp.elementAt(0)).delim;
                 }
             }
 
             openStarted = false;
         } catch (MailException exp) {
             close();
             String msg = exp.getMessage();
             if(msg.startsWith("NO")) {
                 msg = msg.substring(msg.indexOf(' ')+1);
             }
             throw new MailException(msg);
         }
         return true;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#close()
      */
     public void close() throws IOException, MailException {
         openStarted = false;
         if(connection != null) {
             if(connection.isConnected()) {
                 // Note: Active mailbox is not closed to avoid unintentionally
                 // expunging deleted messages.
                 try {
                     imapProtocol.executeLogout();
                 } catch (Exception exp) { }
             }
             activeMailbox = null;
             connection.close();
             connection = null;
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#isConnected()
      */
     public boolean isConnected() {
         return connection != null && connection.isConnected();
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getAcctConfig()
      */
     public AccountConfig getAcctConfig() {
         return accountConfig;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#getConnectionConfig()
      */
     public ConnectionConfig getConnectionConfig() {
         return getAcctConfig();
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#getUsername()
      */
     public String getUsername() {
         return username;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#setUsername(java.lang.String)
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#getPassword()
      */
     public String getPassword() {
         return password;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#setPassword(java.lang.String)
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasFolders()
      */
     public boolean hasFolders() {
         return true;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasMessageParts()
      */
     public boolean hasMessageParts() {
         return true;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasUndelete()
      */
     public boolean hasUndelete() {
         return true;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasExpunge()
      */
     public boolean hasExpunge() {
         return true;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasAppend()
      */
     public boolean hasAppend() {
         return true;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasCopy()
      */
     public boolean hasCopy() {
         return true;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasAnswered()
      */
     public boolean hasFlags() {
         return true;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasIdle()
      */
     public boolean hasIdle() {
         return capabilities.containsKey(CAPABILITY_IDLE);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasLockedFolders()
      */
     public boolean hasLockedFolders() {
         return false;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getFolderTree(org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public FolderTreeItem getFolderTree(MailProgressHandler progressHandler) throws IOException, MailException {
         // Get the folder subscription list, used to prune the results
         Hashtable subscriptions = getFolderSubscriptions(progressHandler);
         
         FolderTreeItem rootItem = new FolderTreeItem("", "", folderDelim);
 
         boolean childrenExtension = capabilities.containsKey(CAPABILITY_CHILDREN);
 
         // Special logic to handle a user-specified folder prefix
         String folderPrefix = accountConfig.getFolderPrefix();
         if(folderPrefix != null && folderPrefix.length() > 0) {
             FolderTreeItem fakeRootItem = new FolderTreeItem("", folderPrefix, folderDelim);
             getFolderTreeImpl(fakeRootItem, 0, childrenExtension, progressHandler);
 
             // Since we have no way to find the inbox with a hard-coded prefix,
             // assume it to be there and then do a STATUS to verify.
             // If the STATUS fails, a MailException will be thrown, which we can
             // safely ignore.  Otherwise, create a folder item and add it.
             try {
                 imapProtocol.executeStatus(new String[] { INBOX }, null);
                 FolderTreeItem inboxItem = new FolderTreeItem(rootItem, INBOX, INBOX, folderDelim, true, true);
                 rootItem.addChild(inboxItem);
             } catch (MailException exp) { }
 
             if(fakeRootItem.hasChildren()) {
                 FolderTreeItem[] children = fakeRootItem.children();
                 for(int i=0; i<children.length; i++) {
                     if(children[i].getName().trim().length() > 0) {
                         rootItem.addChild(children[i]);
                     }
                 }
             }
         }
         else {
             getFolderTreeImpl(rootItem, 0, childrenExtension, progressHandler);
         }
 
         // Find and save the INBOX folder
         FolderTreeItem inbox = findInboxFolder(rootItem);
         if(inbox != null) {
             setInboxFolder(inbox);
             // Force the INBOX to be selectable, since there are rare cases
             // where it is created as not being selectable.
             inbox.setSelectable(true);
         }
 
         if(subscriptions != null) {
             // If the subscriptions set does not contain the INBOX folder for
             // some reason, explicitly add it.  This prevents unusual behavior
             // where the user cannot find the INBOX in their folder tree.
             if(!subscriptions.containsKey(inbox.getPath())) {
                 subscriptions.put(inbox.getPath(), Boolean.TRUE);
             }
             FolderTreeItem prunedTree = pruneFolderTree(rootItem, subscriptions);
             if(prunedTree != null) {
                 rootItem = prunedTree;
             }
         }
         
         return rootItem;
     }
 
     private Hashtable getFolderSubscriptions(MailProgressHandler progressHandler) throws IOException, MailException {
         Hashtable result = null;
         if(accountConfig.getOnlySubscribedFolders()) {
             result = new Hashtable();
             Vector respList = imapProtocol.executeLsub("", "*", progressHandler);
             int size = respList.size();
             for(int i=0;i<size;++i) {
                 ImapProtocol.ListResponse resp = (ImapProtocol.ListResponse)respList.elementAt(i);
                 result.put(resp.name, Boolean.TRUE);
             }
         }
         else {
             result = null;
         }
         return result;
     }
     
     private void getFolderTreeImpl(
             FolderTreeItem baseFolder,
             int depth,
             boolean childrenExtension,
             MailProgressHandler progressHandler) throws IOException, MailException {
         
         Vector respList;
         if(depth == 0) {
             respList = imapProtocol.executeList(baseFolder.getPath(), "%", progressHandler);
         }
         else {
             respList = imapProtocol.executeList(baseFolder.getPath() + baseFolder.getDelim(), "%", progressHandler);
         }
 
         int size = respList.size();
         for(int i=0;i<size;++i) {
             ImapProtocol.ListResponse resp = (ImapProtocol.ListResponse)respList.elementAt(i);
             FolderTreeItem childItem = getFolderItem(baseFolder, resp.name, resp.canSelect);
             baseFolder.addChild(childItem);
             if(resp.hasChildren || (!resp.noInferiors && !childrenExtension)) {
                 // The folder has children, so lets go and list them
                 if(depth+1 < accountConfig.getMaxFolderDepth()) {
                     getFolderTreeImpl(childItem, depth+1, childrenExtension, progressHandler);
                 }
             }
             else if(depth == 0 &&
                     nsPersonal != null &&
                     (resp.name + nsPersonal.delimiter).equals(nsPersonal.prefix) &&
                     accountConfig.getMaxFolderDepth() > 1) {
                 // The folder claims to have no children, but it is a root
                 // folder that matches the personal namespace prefix, so
                 // look for children anyways.
                 getFolderTreeImpl(childItem, depth+1, childrenExtension, progressHandler);
             }
         }
     }
 
     private FolderTreeItem pruneFolderTree(FolderTreeItem currentItem, Hashtable subscriptions) {
         if(subscriptions.containsKey(currentItem.getPath()) || currentItem.hasChildren()) {
             FolderTreeItem duplicate = new FolderTreeItem(currentItem);
             if(currentItem.hasChildren()) {
                 FolderTreeItem[] children = currentItem.children();
                 for(int i=0; i<children.length; i++) {
                     FolderTreeItem duplicateChild = pruneFolderTree(children[i], subscriptions);
                     if(duplicateChild != null) {
                         duplicate.addChild(duplicateChild);
                         duplicateChild.setParent(duplicate);
                     }
                 }
             }
             return duplicate;
         }
         else {
             return null;
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#refreshFolderStatus(org.logicprobe.LogicMail.mail.FolderTreeItem[])
      */
     public void refreshFolderStatus(FolderTreeItem[] folders, MailProgressHandler progressHandler) throws IOException, MailException {
         int i;
 
         // Construct an array of mailbox paths to match the folder vector
         Vector mboxPaths = new Vector();
         Hashtable mboxMap = new Hashtable();
         for(i=0; i<folders.length; i++) {
             FolderTreeItem item = folders[i];
             if(item.isSelectable()) {
                 mboxPaths.addElement(item.getPath());
                 mboxMap.put(item.getPath(), item);
             }
         }
         String[] mboxPathsArray = new String[mboxPaths.size()];
         mboxPaths.copyInto(mboxPathsArray);
 
         // Execute the STATUS command on the folders
         ImapProtocol.StatusResponse[] response = imapProtocol.executeStatus(mboxPathsArray, progressHandler);
 
         // Iterate through the results and update the FolderTreeItem objects
         for(i=0; i<mboxPathsArray.length; i++) {
             FolderTreeItem item = (FolderTreeItem)mboxMap.get(mboxPathsArray[i]);
             item.setMsgCount(response[i].exists);
             item.setUnseenCount(response[i].unseen);
         }
     }
 
     /**
      * Recursively search a folder tree for the INBOX folder.
      * 
      * @param mailbox Starting folder
      * @return INBOX folder
      */
     private FolderTreeItem findInboxFolder(FolderTreeItem mailbox) {
         if(mailbox.getName().equals(INBOX)) {
             return mailbox;
         }
         else if(mailbox.hasChildren()) {
             FolderTreeItem[] children = mailbox.children();
             for(int i=0; i<children.length; i++) {
                 FolderTreeItem result = findInboxFolder(children[i]);
                 if(result != null) {
                     return result;
                 }
             }
         }
         return null;
     }
 
     /**
      * Gets the INBOX folder.
      * If no INBOX folder is available, this method will attempt
      * to load a persisted folder item that may have been associated
      * with the account configuration.
      * 
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getInboxFolder()
      */
     public FolderTreeItem getInboxFolder() {
         if(this.inboxMailbox == null) {
             DataStore dataStore = DataStoreFactory.getConnectionCacheStore();
             Object loadedObject = dataStore.getNamedObject(Long.toString(accountConfig.getUniqueId()) + "_INBOX");
             if(loadedObject instanceof FolderTreeItem) {
                 this.inboxMailbox = (FolderTreeItem)loadedObject;
             }
         }
         return this.inboxMailbox;
     }
 
     /**
      * Sets the INBOX folder.
      * This method also persists the INBOX folder in association
      * with the account configuration.
      * 
      * @param mailbox the new INBOX folder
      */
     private void setInboxFolder(FolderTreeItem mailbox) {
         if(this.inboxMailbox != mailbox) {
             mailbox = new FolderTreeItem(mailbox);
             DataStore dataStore = DataStoreFactory.getConnectionCacheStore();
             dataStore.putNamedObject(Long.toString(accountConfig.getUniqueId()) + "_INBOX", mailbox);
             dataStore.save();
             this.inboxMailbox = mailbox;
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getActiveFolder()
      */
     public FolderTreeItem getActiveFolder() {
         return activeMailbox;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#setActiveFolder(org.logicprobe.LogicMail.mail.FolderTreeItem)
      */
     public void setActiveFolder(FolderTreeItem mailbox) throws IOException, MailException {
         // Shortcut out if a folder change is not necessary
         if(activeMailbox != null && activeMailbox.getPath().equals(mailbox.getPath())) {
             return;
         }
 
         // Change active mailbox
         ImapProtocol.SelectResponse response = imapProtocol.executeSelect(mailbox.getPath());
 
         this.activeMailbox = mailbox;
         activeMailbox.setMsgCount(response.exists);
         
         // If the response lacked a valid UIDNEXT value, then copy it over from
         // the previous SELECT of this mailbox
         ImapProtocol.SelectResponse oldResponse = (ImapProtocol.SelectResponse)knownMailboxes.get(activeMailbox);
         if(oldResponse != null && response.uidNext == -1) {
             response.uidNext = oldResponse.uidNext;
         }
         knownMailboxes.put(activeMailbox, response);
 
         // Ideally, this should parse out the message counts
         // and populate the appropriate fields of the activeMailbox FolderItem
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#setActiveFolder(org.logicprobe.LogicMail.mail.MessageToken)
      */
     public void setActiveFolder(MessageToken messageToken) throws IOException, MailException {
         ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
         String folderPath = imapMessageToken.getFolderPath();
 
         // Shortcut out if a folder change is not necessary
         if(activeMailbox != null && activeMailbox.getPath().equals(imapMessageToken.getFolderPath())) {
             return;
         }
 
         // change active mailbox
         ImapProtocol.SelectResponse response = imapProtocol.executeSelect(folderPath);
 
         FolderTreeItem mailbox = null;
         Enumeration e = knownMailboxes.keys();
         while(e.hasMoreElements()) {
             FolderTreeItem currentMailbox = (FolderTreeItem)e.nextElement();
             if(currentMailbox.getPath().equals(folderPath)) {
                 mailbox = currentMailbox;
                 break;
             }
         }
         if(mailbox == null) {
             int p = folderPath.lastIndexOf(folderDelim.charAt(0));
             if(p != -1 && p < folderPath.length() - 1) {
                 mailbox = new FolderTreeItem(folderPath.substring(p + 1), folderPath, folderDelim);
             }
             else {
                 mailbox = new FolderTreeItem("", folderPath, folderDelim);
             }
         }
 
         this.activeMailbox = mailbox;
         activeMailbox.setMsgCount(response.exists);
         knownMailboxes.put(activeMailbox, response);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.AbstractIncomingMailClient#expungeActiveFolder()
      */
     public int[] expungeActiveFolder() throws IOException, MailException {
         int[] indices = imapProtocol.executeExpunge();
         return indices;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getFolderMessages(org.logicprobe.LogicMail.mail.MessageToken, int, org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public void getFolderMessages(MessageToken firstToken, int increment, FolderMessageCallback callback, MailProgressHandler progressHandler) throws IOException, MailException {
         ImapMessageToken imapToken = (ImapMessageToken)firstToken;
         int lastIndex = imapToken.getMessageIndex() - 1;
         if(lastIndex <= 0) { return; }
         int firstIndex = Math.max(1, lastIndex - (increment - 1));
         if(firstIndex > lastIndex) { return; }
         
         getFolderMessages(firstIndex, lastIndex, false, callback, progressHandler);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getFolderMessages(int, int, org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public void getFolderMessages(int firstIndex, int lastIndex, FolderMessageCallback callback, MailProgressHandler progressHandler) throws IOException, MailException {
         getFolderMessages(firstIndex, lastIndex, false, callback, progressHandler);
     }
 
     private class ImapFetchEnvelopeCallback implements ImapProtocol.FetchEnvelopeCallback {
         private FolderMessageCallback callback;
         public ImapFetchEnvelopeCallback(FolderMessageCallback callback) {
             this.callback = callback;
         }
         public void responseAvailable(FetchEnvelopeResponse response) {
             if(response != null) {
                 callback.folderMessageUpdate(ImapClient.this.prepareFolderMessagesEnvelope(response));
             }
             else {
                 callback.folderMessageUpdate(null);
             }
         }
     }
 
     private void getFolderMessages(int firstIndex, int lastIndex, boolean flagsOnly, FolderMessageCallback callback, MailProgressHandler progressHandler) throws IOException, MailException {
         // Sanity check
         if(activeMailbox == null) {
             throw new MailException("Mailbox not selected");
         }
 
         // Make sure we do not FETCH an empty folder
         if(firstIndex > lastIndex) {
             callback.folderMessageUpdate(null);
             return;
         }
 
         if(flagsOnly) {
             ImapProtocol.FetchFlagsResponse[] flagsResponse =
                 imapProtocol.executeFetchFlags(firstIndex, lastIndex, progressHandler);
             FolderMessage[] result = prepareFolderMessagesFlags(flagsResponse);
             for(int i=0; i<result.length; i++) {
                 callback.folderMessageUpdate(result[i]);
             }
             
             // If the folder's UIDNEXT parameter was not set when it was
             // selected, fake it based on the last item returned by the
             // first FETCH.
             ImapProtocol.SelectResponse selectResponse = (ImapProtocol.SelectResponse)knownMailboxes.get(activeMailbox);
             if(selectResponse.uidNext == -1 && result.length > 0) {
                 selectResponse.uidNext = result[result.length - 1].getUid() + 1;
             }
             callback.folderMessageUpdate(null);
         }
         else {
             imapProtocol.executeFetchEnvelope(firstIndex, lastIndex, new ImapFetchEnvelopeCallback(callback), progressHandler);
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getFolderMessages(org.logicprobe.LogicMail.mail.MessageToken[], boolean, org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public void getFolderMessages(MessageToken[] messageTokens, boolean flagsOnly, FolderMessageCallback callback, MailProgressHandler progressHandler)
     throws IOException, MailException {
         // Sanity check
         if(activeMailbox == null) {
             throw new MailException("Mailbox not selected");
         }
 
         int[] uids = new int[messageTokens.length];
         for(int i=0; i<messageTokens.length; i++) {
             uids[i] = ((ImapMessageToken)messageTokens[i]).getImapMessageUid();
         }
 
         if(flagsOnly) {
             ImapProtocol.FetchFlagsResponse[] flagsResponse =
                 imapProtocol.executeFetchFlagsUid(uids, progressHandler);
             FolderMessage[] result = prepareFolderMessagesFlags(flagsResponse);
             for(int i=0; i<result.length; i++) {
                 callback.folderMessageUpdate(result[i]);
             }
             callback.folderMessageUpdate(null);
         }
         else {
             imapProtocol.executeFetchEnvelopeUid(uids, new ImapFetchEnvelopeCallback(callback), progressHandler);
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getFolderMessages(org.logicprobe.LogicMail.mail.MessageToken[], org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public void getFolderMessages(int[] messageIndices, FolderMessageCallback callback, MailProgressHandler progressHandler)
     throws IOException, MailException {
         // Sanity check
         if(activeMailbox == null) {
             throw new MailException("Mailbox not selected");
         }
 
         imapProtocol.executeFetchEnvelope(messageIndices, new ImapFetchEnvelopeCallback(callback), progressHandler);
     }
     
     private class ImapNewFetchEnvelopeCallback implements ImapProtocol.FetchEnvelopeCallback {
         private FolderMessageCallback callback;
         private FolderMessage lastFolderMessage;
         public ImapNewFetchEnvelopeCallback(FolderMessageCallback callback) {
             this.callback = callback;
         }
         public void responseAvailable(FetchEnvelopeResponse response) {
             if(response != null) {
                 FolderMessage folderMessage = ImapClient.this.prepareFolderMessagesEnvelope(response);
                 callback.folderMessageUpdate(folderMessage);
                 lastFolderMessage = folderMessage;
             }
             else {
                 if(lastFolderMessage != null) {
                     int uidNext = lastFolderMessage.getUid() + 1;
                     ((ImapProtocol.SelectResponse)knownMailboxes.get(activeMailbox)).uidNext = uidNext;
                 }
                 callback.folderMessageUpdate(null);
             }
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getNewFolderMessages(boolean, org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public void getNewFolderMessages(boolean flagsOnly, FolderMessageCallback callback, MailProgressHandler progressHandler) throws IOException, MailException {
         // Sanity check
         if(activeMailbox == null) {
             throw new MailException("Mailbox not selected");
         }
 
         if(!seenMailboxes.containsKey(activeMailbox)) {
             int count = accountConfig.getInitialFolderMessages();
             int msgCount = activeMailbox.getMsgCount();
            int firstIndex = Math.max(1, msgCount - count + 1);
             getFolderMessages(firstIndex, activeMailbox.getMsgCount(), flagsOnly, callback, progressHandler);
             seenMailboxes.put(activeMailbox, Boolean.TRUE);
         }
         else {
             FolderMessage[] result;
             int uidNext = ((ImapProtocol.SelectResponse)knownMailboxes.get(activeMailbox)).uidNext;
             
             if(uidNext == -1) {
                 // This condition should never be possible, since something
                 // should set the UIDNEXT prior to the invocation of this
                 // method.  However, just to be absolutely sure, we will
                 // check anyways so a useful error can be reported.
                 throw new MailException("Next UID for folder is unknown");
             }
             
             if(flagsOnly) {
                 ImapProtocol.FetchFlagsResponse[] flagsResponse =
                     imapProtocol.executeFetchFlagsUid(uidNext, progressHandler);
                 result = prepareFolderMessagesFlags(flagsResponse);
                 if(result.length > 0) {
                     uidNext = result[result.length-1].getUid() + 1;
                     ((ImapProtocol.SelectResponse)knownMailboxes.get(activeMailbox)).uidNext = uidNext;
                 }
                 for(int i=0; i<result.length; i++) {
                     callback.folderMessageUpdate(result[i]);
                 }
                 callback.folderMessageUpdate(null);
             }
             else {
                 imapProtocol.executeFetchEnvelopeUid(uidNext, new ImapNewFetchEnvelopeCallback(callback), progressHandler);
             }
         }
     }
 
     private FolderMessage prepareFolderMessagesEnvelope(ImapProtocol.FetchEnvelopeResponse response) {
         ImapMessageToken token = new ImapMessageToken(activeMailbox.getPath(), response.uid);
         token.setMessageIndex(response.index);
         FolderMessage folderMessage = new FolderMessage(
                 token,
                 response.envelope,
                 response.index,
                 response.uid);
         folderMessage.setSeen(response.flags.seen);
         folderMessage.setAnswered(response.flags.answered);
         folderMessage.setDeleted(response.flags.deleted);
         folderMessage.setRecent(response.flags.recent);
         folderMessage.setFlagged(response.flags.flagged);
         folderMessage.setDraft(response.flags.draft);
         folderMessage.setForwarded(response.flags.forwarded);
         folderMessage.setJunk(response.flags.junk);
         folderMessage.setStructure(createMessagePartTree(response.structure));
         return folderMessage;
     }
 
     private FolderMessage[] prepareFolderMessagesFlags(ImapProtocol.FetchFlagsResponse[] response) {
         FolderMessage[] folderMessages = new FolderMessage[response.length];
         for(int i=0;i<response.length;i++) {
             ImapMessageToken token = new ImapMessageToken(activeMailbox.getPath(), response[i].uid);
             token.setMessageIndex(response[i].index);
             folderMessages[i] = new FolderMessage(
                     token,
                     null,
                     response[i].index,
                     response[i].uid);
             folderMessages[i].setSeen(response[i].flags.seen);
             folderMessages[i].setAnswered(response[i].flags.answered);
             folderMessages[i].setDeleted(response[i].flags.deleted);
             folderMessages[i].setRecent(response[i].flags.recent);
             folderMessages[i].setFlagged(response[i].flags.flagged);
             folderMessages[i].setDraft(response[i].flags.draft);
             folderMessages[i].setForwarded(response[i].flags.forwarded);
             folderMessages[i].setJunk(response[i].flags.junk);
         }
         return folderMessages;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getMessage(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public Message getMessage(MessageToken messageToken, MailProgressHandler progressHandler) throws IOException, MailException {
         ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
         if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
             throw new MailException("Invalid mailbox for message");
         }
 
         ImapParser.MessageSection structure = getMessageStructure(imapMessageToken.getImapMessageUid());
         Hashtable contentMap = new Hashtable();
         MimeMessagePart rootPart =
             getMessagePart(contentMap, imapMessageToken.getImapMessageUid(),
                     structure, accountConfig.getMaxMessageSize(),
                     progressHandler);
         Message msg = new Message(rootPart);
         Enumeration e = contentMap.keys();
         while(e.hasMoreElements()) {
             MimeMessagePart part = (MimeMessagePart)e.nextElement();
             msg.putContent(part, (MimeMessageContent)contentMap.get(part));
         }
         return msg;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getMessagePart(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.message.MimeMessagePart, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public MimeMessageContent getMessagePart(MessageToken messageToken, MimeMessagePart mimeMessagePart, MailProgressHandler progressHandler) throws IOException, MailException {
         ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
         if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
             throw new MailException("Invalid mailbox for message");
         }
 
         // Get the relevant data from the MessagePart object
         String partAddress = mimeMessagePart.getTag();
         String mimeType = mimeMessagePart.getMimeType();
         String mimeSubtype = mimeMessagePart.getMimeSubtype();
 
         // Make sure we can actually get this part
         if(partAddress == null || mimeType == null || mimeSubtype == null) { return null; }
         if(mimeType.equalsIgnoreCase("multipart")) { return null; }
         if(!(messageToken instanceof ImapMessageToken)) { return null; }
 
 
         byte[] data = getMessageBody(imapMessageToken.getImapMessageUid(), partAddress, progressHandler);
         MimeMessageContent content;
         try {
             content = MimeMessageContentFactory.createContentEncoded(mimeMessagePart, data);
         } catch (UnsupportedContentException e) {
             content = null;
         }
         return content;
     }
 
     private MimeMessagePart getMessagePart(
             Hashtable contentMap,
             int uid,
             ImapParser.MessageSection structure,
             int maxSize,
             MailProgressHandler progressHandler)
     throws IOException, MailException
     {
         MimeMessagePart part;
         if(MimeMessagePartFactory.isMimeMessagePartSupported(structure.type, structure.subtype)) {
             byte[] data;
             if(structure.type.equalsIgnoreCase("multipart"))
                 data = null;
             else {
                 if(structure.size < maxSize) {
                     data = getMessageBody(uid, structure.address, progressHandler);
                     maxSize -= structure.size;
                 }
                 else {
                     // We hit the size limit, so stop processing
                     return null;
                 }
             }
             part = MimeMessagePartFactory.createMimeMessagePart(
                     structure.type,
                     structure.subtype,
                     structure.name,
                     structure.encoding,
                     structure.charset,
                     structure.disposition,
                     structure.contentId,
                     structure.size,
                     structure.address);
             try {
                 contentMap.put(part, MimeMessageContentFactory.createContentEncoded(part, data));
             } catch (UnsupportedContentException e) {
                 EventLogger.logEvent(AppInfo.GUID,
                         ("UnsupportedContentException: " + e.getMessage()).getBytes(),
                         EventLogger.WARNING);
             }
         }
         else if(structure.address.equals("1")) {
             // If this was the root part, and still could not be loaded,
             // insert a placeholder to avoid having to return null.
             part = new UnsupportedPart(structure.type, structure.subtype);
         }
         else {	
             part = null;
         }
 
         if((part instanceof MultiPart)&&(structure.subsections != null)&&(structure.subsections.length > 0)) {
             for(int i=0;i<structure.subsections.length;i++) {
                 MimeMessagePart subPart = getMessagePart(contentMap, uid, structure.subsections[i], maxSize, progressHandler);
                 if(subPart != null) {
                     ((MultiPart)part).addPart(subPart);
                 }
             }
         }
         return part;
     }
 
     private MimeMessagePart createMessagePartTree(ImapParser.MessageSection structure) {
         if(structure == null) { return null; }
         MimeMessagePart part = MimeMessagePartFactory.createMimeMessagePart(
                 structure.type,
                 structure.subtype,
                 structure.name,
                 structure.encoding,
                 structure.charset,
                 structure.disposition,
                 structure.contentId,
                 structure.size,
                 structure.address);
 
         if((part instanceof MultiPart)&&(structure.subsections != null)&&(structure.subsections.length > 0)) {
             MultiPart multiPart = (MultiPart)part;
             for(int i=0; i<structure.subsections.length; i++) {
                 MimeMessagePart subPart = createMessagePartTree(structure.subsections[i]);
                 if(subPart != null) {
                     multiPart.addPart(subPart);
                 }
             }
         }
         return part;
     }
 
     /**
      * Returns the message structure tree independent of message content.
      * This tree is used to build the final message tree.
      */
     private ImapParser.MessageSection getMessageStructure(int uid) throws IOException, MailException {
         if(activeMailbox == null) {
             throw new MailException("Mailbox not selected");
         }
 
         return imapProtocol.executeFetchBodystructure(uid);
     }
 
     /**
      * Create a new folder item, doing the relevant parsing on the path.
      * @param parent Parent folder
      * @param folderPath Folder path string
      * @param canSelect Whether the folder is selectable
      * @return Folder item object
      */
     private FolderTreeItem getFolderItem(FolderTreeItem parent, String folderPath, boolean canSelect) throws IOException, MailException {
         int pos = 0;
         int i = 0;
         while((i = folderPath.indexOf(folderDelim, i)) != -1) {
             if(i != -1) { pos = i+1; i++; }
         }
         String decodedName = ImapParser.parseFolderName(folderPath.substring(pos));
         FolderTreeItem item = new FolderTreeItem(parent, decodedName, folderPath, folderDelim, canSelect, canSelect);
         item.setMsgCount(0);
         return item;
     }
 
     private byte[] getMessageBody(int uid, String address, MailProgressHandler progressHandler) throws IOException, MailException {
         if(activeMailbox == null) {
             throw new MailException("Mailbox not selected");
         }
         return imapProtocol.executeFetchBody(uid, address, progressHandler);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#deleteMessage(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.message.MessageFlags)
      */
     public void deleteMessage(MessageToken messageToken, MessageFlags messageFlags) throws IOException, MailException {
         ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
         if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
             throw new MailException("Invalid mailbox for message");
         }
 
         ImapProtocol.MessageFlags updatedFlags =
             imapProtocol.executeStore(imapMessageToken.getImapMessageUid(), true, new String[] { ImapParser.FLAG_DELETED });
         refreshMessageFlags(updatedFlags, messageFlags);
     }
 
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#undeleteMessage(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.message.MessageFlags)
      */
     public void undeleteMessage(MessageToken messageToken, MessageFlags messageFlags) throws IOException, MailException {
         ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
         if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
             throw new MailException("Invalid mailbox for message");
         }
 
         ImapProtocol.MessageFlags updatedFlags =
             imapProtocol.executeStore(imapMessageToken.getImapMessageUid(), false, new String[] { ImapParser.FLAG_DELETED });
         refreshMessageFlags(updatedFlags, messageFlags);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#messageAnswered(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.message.MessageFlags)
      */
     public void messageAnswered(MessageToken messageToken, MessageFlags messageFlags) throws IOException, MailException {
         ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
         if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
             throw new MailException("Invalid mailbox for message");
         }
 
         ImapProtocol.MessageFlags updatedFlags =
             imapProtocol.executeStore(imapMessageToken.getImapMessageUid(), true, new String[] { ImapParser.FLAG_ANSWERED });
         refreshMessageFlags(updatedFlags, messageFlags);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#messageForwarded(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.message.MessageFlags)
      */
     public void messageForwarded(MessageToken messageToken, MessageFlags messageFlags) throws IOException, MailException {
         ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
         if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
             throw new MailException("Invalid mailbox for message");
         }
 
         ImapProtocol.MessageFlags updatedFlags =
             imapProtocol.executeStore(imapMessageToken.getImapMessageUid(), true, new String[] { ImapParser.FLAG_FORWARDED });
         refreshMessageFlags(updatedFlags, messageFlags);
     }
     
     private static void refreshMessageFlags(ImapProtocol.MessageFlags updatedFlags, MessageFlags messageFlags) {
         if(updatedFlags != null) {
             messageFlags.setAnswered(updatedFlags.answered);
             messageFlags.setDeleted(updatedFlags.deleted);
             messageFlags.setDraft(updatedFlags.draft);
             messageFlags.setFlagged(updatedFlags.flagged);
             messageFlags.setRecent(updatedFlags.recent);
             messageFlags.setSeen(updatedFlags.seen);
             messageFlags.setForwarded(updatedFlags.forwarded);
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#appendMessage(org.logicprobe.LogicMail.mail.FolderTreeItem, java.lang.String, org.logicprobe.LogicMail.message.MessageFlags)
      */
     public void appendMessage(FolderTreeItem folder, String rawMessage, MessageFlags initialFlags) throws IOException, MailException {
         ImapProtocol.MessageFlags flags = new ImapProtocol.MessageFlags();
         flags.seen = initialFlags.isSeen();
         flags.answered = initialFlags.isAnswered();
         flags.flagged = initialFlags.isFlagged();
         flags.deleted = initialFlags.isDeleted();
         flags.draft = initialFlags.isDraft();
         flags.junk = initialFlags.isJunk();
         flags.forwarded = initialFlags.isForwarded();
         flags.recent = false; // invalid for appending
 
         imapProtocol.executeAppend(folder.getPath(), rawMessage, flags);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#copyMessage(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.mail.FolderTreeItem)
      */
     public void copyMessage(MessageToken messageToken, FolderTreeItem destinationFolder) throws IOException, MailException {
         ImapMessageToken imapMessageToken = (ImapMessageToken)messageToken;
         if(!imapMessageToken.getFolderPath().equalsIgnoreCase(activeMailbox.getPath())) {
             throw new MailException("Invalid mailbox for message");
         }
 
         imapProtocol.executeCopy(imapMessageToken.getImapMessageUid(), destinationFolder.getPath());
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#noop()
      */
     public boolean noop() throws IOException, MailException {
         boolean result = imapProtocol.executeNoop();
         return result;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#idleModeBegin()
      */
     public void idleModeBegin() throws IOException, MailException {
         imapProtocol.executeIdle();
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#idleModeEnd()
      */
     public void idleModeEnd() throws IOException, MailException {
         imapProtocol.executeIdleDone();
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#idleModePoll()
      */
     public boolean idleModePoll() throws IOException, MailException {
         return imapProtocol.executeIdlePoll();
     }
 }
