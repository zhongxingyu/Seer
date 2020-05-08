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
 import java.util.Hashtable;
 import java.util.Vector;
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.IncomingMailClient;
 import org.logicprobe.LogicMail.mail.MailException;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MessagePart;
 import org.logicprobe.LogicMail.message.MessagePartFactory;
 import org.logicprobe.LogicMail.message.MultiPart;
 import org.logicprobe.LogicMail.util.Connection;
 
 /**
  * 
  * Implements the IMAP client
  * 
  */
 public class ImapClient implements IncomingMailClient {
     private GlobalConfig globalConfig;
     private AccountConfig acctCfg;
     private Connection connection;
     private ImapProtocol imapProtocol;
     private String username;
     private String password;
     private boolean openStarted;
 
     /**
      * Table of supported server capabilities
      */
     private Hashtable capabilities;
     
     /**
      * Delmiter between folder names in the hierarchy
      */
     private String folderDelim = "";
 
     /**
      * Active mailbox path, so most commands do not need
      * to take a mailbox path parameter.  This makes it easier
      * to provide a common front-end that still works for
      * protocols that do not support a mailbox hierarchy.
      */
     private FolderTreeItem activeMailbox = null;
 
     public ImapClient(GlobalConfig globalConfig, AccountConfig acctCfg) {
         this.acctCfg = acctCfg;
         this.globalConfig = globalConfig;
         connection = new Connection(
                 acctCfg.getServerName(),
                 acctCfg.getServerPort(),
                 acctCfg.getServerSSL(),
                 acctCfg.getDeviceSide());
         imapProtocol = new ImapProtocol(connection);
         username = acctCfg.getServerUser();
         password = acctCfg.getServerPass();
         openStarted = false;
     }
 
     public boolean open() throws IOException, MailException {
         try {
             if(!openStarted) {
                 connection.open();
                 activeMailbox = null;
                 // Find out server capabilities
                 capabilities = imapProtocol.executeCapability();
                 openStarted = true;
             }
             // Authenticate with the server
             if(!imapProtocol.executeLogin(username, password)) {
                 return false;
             }
 
             // Discover folder delim
             Vector resp = imapProtocol.executeList("", "");
             if(resp.size() > 0) {
                 folderDelim = ((ImapProtocol.ListResponse)resp.elementAt(0)).delim;
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
     
     public void close() throws IOException, MailException {
         if(connection.isConnected()) {
             if(activeMailbox != null && !activeMailbox.equals("")) {
                 imapProtocol.executeClose();
             }
             imapProtocol.executeLogout();
         }
         activeMailbox = null;
         connection.close();
     }
 
     public boolean isConnected() {
         return connection.isConnected();
     }
 
     public AccountConfig getAcctConfig() {
         return acctCfg;
     }
 
     public String getUsername() {
         return username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public boolean hasFolders() {
         return true;
     }
 
     public FolderTreeItem getFolderTree() throws IOException, MailException {
         FolderTreeItem rootItem = new FolderTreeItem("", "", folderDelim);
         getFolderTreeImpl(rootItem, 0);
         return rootItem;
     }
 
     private void getFolderTreeImpl(FolderTreeItem baseFolder, int depth) throws IOException, MailException {
         Vector respList;
         if(depth == 0)
             respList = imapProtocol.executeList(baseFolder.getPath(), "%");
         else
             respList = imapProtocol.executeList(baseFolder.getPath() + baseFolder.getDelim(), "%");
 
         int size = respList.size();
         for(int i=0;i<size;++i) {
             ImapProtocol.ListResponse resp = (ImapProtocol.ListResponse)respList.elementAt(i);
             if(resp.canSelect) {
                 FolderTreeItem childItem = getFolderItem(baseFolder, resp.name);
                 baseFolder.addChild(childItem);
                 if(resp.hasChildren) {
                    if(depth+1 >= globalConfig.getImapMaxFolderDepth()) {
                        return;
                     }
                    getFolderTreeImpl(childItem, depth+1);
                 }
             }
         }
     }
 
     public FolderTreeItem getActiveFolder() {
         return activeMailbox;
     }
 
     public void setActiveFolder(FolderTreeItem mailbox) throws IOException, MailException {
         this.activeMailbox = mailbox;
         // change active mailbox
         ImapProtocol.SelectResponse response = imapProtocol.executeSelect(activeMailbox.getPath());
         
         activeMailbox.setMsgCount(response.exists);
         
         // ideally, this should parse out the message counts
         // and populate the appropriate fields of the activeMailbox FolderItem
     }
 
     public FolderMessage[] getFolderMessages(int firstIndex, int lastIndex) throws IOException, MailException {
         ImapProtocol.FetchEnvelopeResponse[] response =
                 imapProtocol.executeFetchEnvelope(firstIndex, lastIndex);
         
         FolderMessage[] folderMessages = new FolderMessage[response.length];
         for(int i=0;i<response.length;i++) {
             folderMessages[i] = new FolderMessage(response[i].envelope, response[i].index);
             folderMessages[i].setSeen(response[i].flags.seen);
             folderMessages[i].setAnswered(response[i].flags.answered);
             folderMessages[i].setDeleted(response[i].flags.deleted);
             folderMessages[i].setRecent(response[i].flags.recent);
             folderMessages[i].setFlagged(response[i].flags.flagged);
             folderMessages[i].setDraft(response[i].flags.draft);
         }
         
         return folderMessages;
     }
 
     public Message getMessage(FolderMessage folderMessage) throws IOException, MailException {
         ImapParser.MessageSection structure = getMessageStructure(folderMessage.getIndex());
         MessagePart rootPart =
             getMessagePart(folderMessage.getIndex(),
                            structure, globalConfig.getImapMaxMsgSize());
         Message msg = new Message(folderMessage.getEnvelope(), rootPart);
         return msg;
     }
 
     private MessagePart getMessagePart(int index,
                                        ImapParser.MessageSection structure,
                                        int maxSize)
         throws IOException, MailException
     {
         MessagePart part;
         if(MessagePartFactory.isMessagePartSupported(structure.type, structure.subtype)) {
             String data;
             if(structure.type.equalsIgnoreCase("multipart"))
                 data = null;
             else {
                 if(structure.size < maxSize) {
                     data = getMessageBody(index, structure.address);
                     maxSize -= structure.size;
                 }
                 else {
                     // We hit the size limit, so stop processing
                     return null;
                 }
             }
             part = MessagePartFactory.createMessagePart(structure.type, structure.subtype, structure.encoding, structure.charset, data);
         }
         else
             part = null;
 
         if((part instanceof MultiPart)&&(structure.subsections != null)&&(structure.subsections.length > 0)) {
             for(int i=0;i<structure.subsections.length;i++) {
                 MessagePart subPart = getMessagePart(index, structure.subsections[i], maxSize);
                 if(subPart != null) {
                     ((MultiPart)part).addPart(subPart);
                 }
             }
         }
         return part;
     }
     
     /**
      * Returns the message structure tree independent of message content.
      * This tree is used to build the final message tree.
      */
     private ImapParser.MessageSection getMessageStructure(int msgIndex) throws IOException, MailException {
         if(activeMailbox.equals("")) {
             throw new MailException("Mailbox not selected");
         }
 
         return imapProtocol.executeFetchBodystructure(msgIndex);
     }
 
     /**
      * Create a new folder item, doing the relevant parsing on the path.
      * @param parent Parent folder
      * @param folderPath Folder path string
      * @return Folder item object
      */
     private FolderTreeItem getFolderItem(FolderTreeItem parent, String folderPath) throws IOException, MailException {
         int pos = 0;
         int i = 0;
         while((i = folderPath.indexOf(folderDelim, i)) != -1) {
             if(i != -1) { pos = i+1; i++; }
         }
         FolderTreeItem item = new FolderTreeItem(parent, folderPath.substring(pos), folderPath, folderDelim);
         item.setMsgCount(0);
         return item;
     }
 
     private String getMessageBody(int index, String address) throws IOException, MailException {
         if(activeMailbox.equals("")) {
             throw new MailException("Mailbox not selected");
         }
         
         return imapProtocol.executeFetchBody(index, address);
     }
 }
