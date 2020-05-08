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
 
 package org.logicprobe.LogicMail.mail.pop;
 
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import net.rim.device.api.util.Arrays;
 import net.rim.device.api.util.ToIntHashtable;
 
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.conf.ConnectionConfig;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.conf.MailSettingsEvent;
 import org.logicprobe.LogicMail.conf.MailSettingsListener;
 import org.logicprobe.LogicMail.conf.PopConfig;
 import org.logicprobe.LogicMail.mail.AbstractIncomingMailClient;
 import org.logicprobe.LogicMail.mail.FolderMessageCallback;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.MailException;
 import org.logicprobe.LogicMail.mail.MailProgressHandler;
 import org.logicprobe.LogicMail.mail.MessageToken;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.util.Connection;
 import org.logicprobe.LogicMail.util.NetworkConnector;
 import org.logicprobe.LogicMail.util.MailMessageParser;
 import org.logicprobe.LogicMail.util.StringParser;
 
 /**
  * 
  * Implements the POP3 client
  * 
  */
 public class PopClient extends AbstractIncomingMailClient {
     private final NetworkConnector networkConnector;
     private final MailSettings mailSettings;
     private final GlobalConfig globalConfig;
     private final PopConfig accountConfig;
     private final PopProtocol popProtocol;
     private Connection connection;
     private String username;
     private String password;
     private boolean openStarted;
     
     /**
      * Table of supported server capabilities
      */
     private Hashtable capabilities;
     
     /**
      * Active mailbox.  Since POP3 does not support multiple
      * mailboxes for a user, it is used to contain some
      * relevant information for the user's single mailbox.
      */
     private FolderTreeItem activeMailbox = null;
     
     /** Creates a new instance of PopClient */
     public PopClient(NetworkConnector networkConnector, GlobalConfig globalConfig, PopConfig accountConfig) {
         this.networkConnector = networkConnector;
         this.globalConfig = globalConfig;
         this.accountConfig = accountConfig;
         this.mailSettings = MailSettings.getInstance();
         popProtocol = new PopProtocol();
         username = accountConfig.getServerUser();
         password = accountConfig.getServerPass();
         
         // Create our dummy folder item for the inbox
         activeMailbox = new FolderTreeItem("INBOX", "INBOX", "");
         activeMailbox.setMsgCount(0);
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
      * @see org.logicprobe.LogicMail.mail.MailClient#open()
      */
     public boolean open() throws IOException, MailException {
         if(!openStarted) {
             connection = networkConnector.open(accountConfig);
             popProtocol.setConnection(connection);
             
             // Eat the initial server response
             connection.receive();
             openStarted = true;
         }
         
         // Find out server capabilities
         capabilities = popProtocol.executeCapa();
         
         // TLS initialization
         int serverSecurity = accountConfig.getServerSecurity();
         if((serverSecurity == ConnectionConfig.SECURITY_TLS_IF_AVAILABLE
                 && capabilities != null && capabilities.containsKey("STARTTLS"))
         		|| (serverSecurity == ConnectionConfig.SECURITY_TLS)) {
         	if(popProtocol.executeStartTLS()) {
         	    connection = networkConnector.getConnectionAsTLS(connection);
         	    popProtocol.setConnection(connection);
         	}
         	else {
         	    return false;
         	}
         }
         else if(capabilities == null && serverSecurity == ConnectionConfig.SECURITY_TLS_IF_AVAILABLE) {
             if(popProtocol.executeStartTLS()) {
                 connection = networkConnector.getConnectionAsTLS(connection);
                 popProtocol.setConnection(connection);
             }
         }
         
         try {
             // Login to the server
             popProtocol.executeUser(username);
             popProtocol.executePass(password);
         } catch (MailException exp) {
             // We assume that fatal exceptions are due to protocol errors or
             // locked mailbox issues, while non-fatal exceptions are due to
             // authentication problems.
             if(exp.isFatal()) {
                 throw exp;
             }
             else {
                 return false;
             }
         }
         // Update message counts
         activeMailbox.setMsgCount(popProtocol.executeStat());
         
         openStarted = false;
         return true;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.MailClient#close()
      */
     public void close() throws IOException, MailException {
         openStarted = false;
         if(connection != null) {
             if(connection.isConnected()) {
                 try {
                     popProtocol.executeQuit();
                 } catch (Exception exp) { }
             }
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
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasMessageIndexMap()
      */
     public boolean hasFolderMessageIndexMap() {
         return true;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#hasLockedFolders()
      */
     public boolean hasLockedFolders() {
         return true;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#refreshFolderStatus(org.logicprobe.LogicMail.mail.FolderTreeItem[], org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public void refreshFolderStatus(FolderTreeItem[] folders, MailProgressHandler progressHandler) throws IOException, MailException {
         // Only one mailbox can exist, so we just pull the message counts.
     	// Since this is a single operation with POP, there is no reason to
     	// provide more detailed status through the progress handler.
         activeMailbox.setMsgCount(popProtocol.executeStat());
         if(folders.length == 1 && folders[0] != activeMailbox) {
         	folders[0].setMsgCount(activeMailbox.getMsgCount());
         }
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getInboxFolder()
      */
     public FolderTreeItem getInboxFolder() {
     	return activeMailbox;
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
         // Mailbox cannot be changed, so we just pull the message counts
         activeMailbox.setMsgCount(popProtocol.executeStat());
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#setActiveFolder(org.logicprobe.LogicMail.mail.MessageToken)
      */
     public void setActiveFolder(MessageToken messageToken) throws IOException, MailException {
         // Mailbox cannot be changed, so we just pull the message counts
         activeMailbox.setMsgCount(popProtocol.executeStat());
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getFolderMessages(org.logicprobe.LogicMail.mail.MessageToken, int, org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public void getFolderMessages(MessageToken firstToken, int increment, FolderMessageCallback callback, MailProgressHandler progressHandler) throws IOException, MailException {
         PopMessageToken popToken = (PopMessageToken)firstToken;
         int lastIndex = popToken.getMessageIndex() - 1;
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
     
     /**
      * Gets the folder messages from the mail server for a range of indices.
      * In order to provide a more natural ordering of the results, messages
      * are requested in an ordering based on the {@link GlobalConfig#getDispOrder()}
      * value.
      * 
      * @param firstIndex the first index of the range
      * @param lastIndex the last index of the range
      * @param flagsOnly whether to request only flags
      * @param callback the callback for result notification
      * @param progressHandler the progress handler
      * 
      * @throws IOException Signals that an I/O exception has occurred.
      * @throws MailException the mail exception
      */
     private void getFolderMessages(int firstIndex, int lastIndex, boolean flagsOnly, FolderMessageCallback callback, MailProgressHandler progressHandler) throws IOException, MailException {
         int[] indices = new int[(lastIndex - firstIndex)+1];
         
         if(globalConfig.getDispOrder()) {
         	int currentIndex = firstIndex;
         	for(int i=0; i<indices.length; i++) {
         		indices[i] = currentIndex++;
         	}
         }
         else {
             int currentIndex = lastIndex;
             for(int i=0; i<indices.length; i++) {
                 indices[i] = currentIndex--;
             }
         }
     
     	getFolderMessagesImpl(indices, flagsOnly, callback, progressHandler);
     }
 
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getFolderMessages(org.logicprobe.LogicMail.mail.MessageToken[], boolean, org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
 	 */
 	public void getFolderMessages(MessageToken[] messageTokens, boolean flagsOnly, FolderMessageCallback callback, MailProgressHandler progressHandler)
 			throws IOException, MailException {
 		// Since POP servers typically lock the mailbox while a client is connected,
 		// and given the typical use case of this method, we will make the assumption
 		// that the message indices in the provided tokens exactly match the messages
 		// we want to retrieve headers for.
 		
 		int[] indices = new int[messageTokens.length];
 		for(int i=0; i<messageTokens.length; i++) {
 			indices[i] = ((PopMessageToken)messageTokens[i]).getMessageIndex();
 		}
 		Arrays.sort(indices, 0, indices.length);
 		
 		if(!globalConfig.getDispOrder()) {
 		    int[] reverseIndices = new int[indices.length];
 		    for(int i=0; i<indices.length; i++) {
 		        reverseIndices[indices.length - i - 1] = indices[i];
 		    }
 		    indices = reverseIndices;
 		}
 		
 		getFolderMessagesImpl(indices, flagsOnly, callback, progressHandler);
 	}
 
 	public void getFolderMessages(int[] messageIndices, FolderMessageCallback callback, MailProgressHandler progressHandler)
 	        throws IOException, MailException {
 	    
 	    int[] indices = messageIndices;
         if(!globalConfig.getDispOrder()) {
             int[] reverseIndices = new int[indices.length];
             for(int i=0; i<indices.length; i++) {
                 reverseIndices[indices.length - i - 1] = indices[i];
             }
             indices = reverseIndices;
         }
         
         getFolderMessagesImpl(indices, false, callback, progressHandler);
 	}
 	
     private void getFolderMessagesImpl(int[] indices, boolean flagsOnly, FolderMessageCallback callback, MailProgressHandler progressHandler)
     		throws IOException, MailException {
         String[] headerText;
         String uid;
         MessageEnvelope env;
         for(int i=0; i<indices.length; i++) {
             if(!flagsOnly) {
                 headerText = popProtocol.executeTop(indices[i], 0);
                 env = MailMessageParser.parseMessageEnvelope(headerText);
             }
             else {
                 env = null;
             }
             uid = popProtocol.executeUidl(indices[i]);
             FolderMessage folderMessage = new FolderMessage(
                     new PopMessageToken(indices[i], uid),
                     env, indices[i], uid.hashCode());
             if(progressHandler != null) { progressHandler.mailProgress(MailProgressHandler.TYPE_PROCESSING, i + 1, indices.length); }
 
             callback.folderMessageUpdate(folderMessage);
         }
         callback.folderMessageUpdate(null);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.AbstractIncomingMailClient#getFolderMessageIndexMap(org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public ToIntHashtable getFolderMessageIndexMap(MailProgressHandler progressHandler) throws IOException, MailException {
         ToIntHashtable indexUidMap = popProtocol.executeUidl(progressHandler);
         return indexUidMap;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getNewFolderMessages(boolean, org.logicprobe.LogicMail.mail.FolderMessageCallback, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public void getNewFolderMessages(boolean flagsOnly, FolderMessageCallback callback, MailProgressHandler progressHandler) throws IOException, MailException {
     	int count = accountConfig.getInitialFolderMessages();
 		int msgCount = activeMailbox.getMsgCount();
        int firstIndex = Math.max(1, msgCount - count + 1);
     	getFolderMessages(firstIndex, activeMailbox.getMsgCount(), flagsOnly, callback, progressHandler);
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#getMessage(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.mail.MailProgressHandler)
      */
     public Message getMessage(MessageToken messageToken, MailProgressHandler progressHandler) throws IOException, MailException {
     	PopMessageToken popMessageToken = (PopMessageToken)messageToken;
     	
         // Figure out the max number of lines
         int maxLines = accountConfig.getMaxMessageLines();
 
         // Download the message text
         String[] message = popProtocol.executeTop((popMessageToken.getMessageIndex()), maxLines, progressHandler);
         
         Hashtable contentMap = new Hashtable();
         MimeMessagePart rootPart = MailMessageParser.parseRawMessage(contentMap, StringParser.createInputStream(message));
         Message msg = new Message(rootPart);
         Enumeration e = contentMap.keys();
         while(e.hasMoreElements()) {
         	MimeMessagePart part = (MimeMessagePart)e.nextElement();
         	msg.putContent(part, (MimeMessageContent)contentMap.get(part));
         }
         return msg;
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#deleteMessage(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.message.MessageFlags)
      */
     public void deleteMessage(MessageToken messageToken, MessageFlags messageFlags) throws IOException, MailException {
     	PopMessageToken popMessageToken = (PopMessageToken)messageToken;
     	
         popProtocol.executeDele(popMessageToken.getMessageIndex());
         messageFlags.setDeleted(true);
     }
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.IncomingMailClient#noop()
      */
     public boolean noop() throws IOException, MailException {
     	popProtocol.executeNoop();
 		return false;
 	}
 }
