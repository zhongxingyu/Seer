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
 
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.util.Enumeration;
 import java.util.Hashtable;
 
 import javax.microedition.io.Connector;
 import javax.microedition.io.file.FileConnection;
 
 import net.rim.device.api.system.EventLogger;
 import net.rim.device.api.system.UnsupportedOperationException;
 
 import org.logicprobe.LogicMail.AppInfo;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.message.Message;
 import org.logicprobe.LogicMail.message.MimeMessageContent;
 import org.logicprobe.LogicMail.message.MessageFlags;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.util.MailMessageParser;
 import org.logicprobe.LogicMail.util.ThreadQueue;
 
 /**
  * This class manages local mail folders on the device.
  * It should only have a single instance, and does not
  * currently have any user configuration.
  */
 public class LocalMailStore extends AbstractMailStore {
     private GlobalConfig globalConfig;
     private FolderTreeItem rootFolder;
     private ThreadQueue threadQueue;
     private Hashtable folderMaildirMap;
     
     public LocalMailStore() {
         super();
         globalConfig = MailSettings.getInstance().getGlobalConfig();
         threadQueue = new ThreadQueue();
         folderMaildirMap = new Hashtable();
         
         // Build the local folder tree, which matches a fixed layout for now.
         // Eventually it should be partially editable by the user.
         // This is also the only folder tree that should lack an INBOX.
         // The "Outbox" folder is marked to prevent the user from being able
         // to append messages to it.  However, it is a special mailbox, and the
         // object model can and does append messages to it as a spool.
         rootFolder = new FolderTreeItem("", "", "");
         rootFolder.addChild(new FolderTreeItem(rootFolder, "Outbox", "Outbox", "/", true, false));
         rootFolder.addChild(new FolderTreeItem(rootFolder, "Drafts", "Drafts", "/", true, true));
         rootFolder.addChild(new FolderTreeItem(rootFolder, "Sent", "Sent", "/", true, true));
         rootFolder.addChild(new FolderTreeItem(rootFolder, "Trash", "Trash", "/", true, true));
     }
 
     public void shutdown(boolean wait) {
     	threadQueue.shutdown(wait);
     }
 
     public boolean isLocal() {
         return true;
     }
 
     public boolean hasFolders() {
         return true;
     }
 
     public boolean hasMessageParts() {
     	return false;
     }
     
     public boolean hasFlags() {
         return true;
     }
 
     public boolean hasAppend() {
         return true;
     }
 
     public boolean hasCopy() {
     	return true;
     }
     
     public boolean hasUndelete() {
         return true;
     }
 
     public boolean hasExpunge() {
         return true;
     }
     
     public void requestFolderTree(MailStoreRequestCallback callback) {
         if(callback != null) { callback.mailStoreRequestComplete(); }
         fireFolderTreeUpdated(rootFolder);
     }
 
     public void requestFolderExpunge(FolderTreeItem folder, MailStoreRequestCallback callback) {
         FolderTreeItem requestFolder = getMatchingFolderTreeItem(folder.getPath());
         
         if(requestFolder != null) {
             threadQueue.invokeLater(new ExpungeFolderRunnable(requestFolder, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
     
     private class ExpungeFolderRunnable extends MaildirRunnable {
         public ExpungeFolderRunnable(FolderTreeItem requestFolder, MailStoreRequestCallback callback) {
             super(requestFolder, callback);
         }
         
         public void run() {
             Throwable throwable = null;
             boolean expunged = false;
             try {
                 maildirFolder.open();
                 maildirFolder.expunge();
                 maildirFolder.close();
                 expunged = true;
             } catch (IOException e) {
                 EventLogger.logEvent(AppInfo.GUID, ("Unable to expunge folder: " + e.toString()).getBytes(), EventLogger.ERROR);
                 throwable = e;
             }
             
             if(expunged) {
                 if(callback != null) { callback.mailStoreRequestComplete(); }
                 fireFolderExpunged(requestFolder, null);
             }
             else {
                 if(callback != null) { callback.mailStoreRequestFailed(throwable); }
             }
         }
     }
     
     public void requestFolderStatus(FolderTreeItem[] folders, MailStoreRequestCallback callback) {
         // Make every entry in the provided array match the local folder
         // objects just in case they do not.  Then, fire change events
         // for all those folders.  The actual data to answer this
         // request should already be available.
         FolderTreeItem[] localFolders = rootFolder.children();
 
         for (int i = 0; i < folders.length; i++) {
             for (int j = 0; j < localFolders.length; j++) {
                 if (folders[i].getPath().equals(localFolders[j].getPath())) {
                     folders[i] = localFolders[j];
                     break;
                 }
             }
         }
         
         if(callback != null) { callback.mailStoreRequestComplete(); }
         for (int i = 0; i < folders.length; i++) {
             fireFolderStatusChanged(folders[i]);
         }
     }
 
     public void requestFolderMessagesRange(FolderTreeItem folder, MessageToken firstToken, int increment, MailStoreRequestCallback callback) {
     	throw new UnsupportedOperationException("Not yet implemented");
 	}
 
 	public void requestFolderMessagesSet(FolderTreeItem folder, MessageToken[] messageTokens, boolean flagsOnly, MailStoreRequestCallback callback) {
 		throw new UnsupportedOperationException("Not yet implemented");
 	}
 
 	public void requestFolderMessagesSet(FolderTreeItem folder, int[] messageIndices, MailStoreRequestCallback callback) {
         throw new UnsupportedOperationException("Not yet implemented");
 	}
 	
     public void requestFolderMessagesRecent(FolderTreeItem folder, boolean flagsOnly, MailStoreRequestCallback callback) {
     	// The flagsOnly parameter has no effect on local mail stores,
     	// and it is not likely to ever be called on them anyways.
         FolderTreeItem requestFolder = getMatchingFolderTreeItem(folder.getPath());
         
         if(requestFolder != null) {
         	threadQueue.invokeLater(new RequestFolderMessagesRecentRunnable(requestFolder, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
 
     private class RequestFolderMessagesRecentRunnable extends MaildirRunnable {
     	public RequestFolderMessagesRecentRunnable(FolderTreeItem requestFolder, MailStoreRequestCallback callback) {
     		super(requestFolder, callback);
     	}
     	
 		public void run() {
 		    Throwable throwable = null;
         	FolderMessage[] folderMessages = null;
         	try {
         		maildirFolder.open();
         		folderMessages = maildirFolder.getFolderMessages();
         		maildirFolder.close();
         	} catch (IOException e) {
                 EventLogger.logEvent(AppInfo.GUID, ("Unable to read folder: " + e.toString()).getBytes(), EventLogger.ERROR);
         		throwable = e;
         	}
         	
         	if(folderMessages != null) {
                 if(callback != null) { callback.mailStoreRequestComplete(); }
         		fireFolderMessagesAvailable(requestFolder, folderMessages, false);
         	}
         	else {
                 if(callback != null) { callback.mailStoreRequestFailed(throwable); }
         	}
 		}
     }
     
     public void requestMessage(MessageToken messageToken, boolean useLimits, MailStoreRequestCallback callback) {
     	LocalMessageToken localMessageToken = (LocalMessageToken)messageToken;
         FolderTreeItem requestFolder = getMatchingFolderTreeItem(localMessageToken.getFolderPath());
         
         if(requestFolder != null) {
         	threadQueue.invokeLater(new RequestMessageRunnable(requestFolder, localMessageToken, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
 
     private class RequestMessageRunnable extends MaildirRunnable {
     	private LocalMessageToken localMessageToken;
     	
     	public RequestMessageRunnable(FolderTreeItem requestFolder, LocalMessageToken localMessageToken, MailStoreRequestCallback callback) {
     		super(requestFolder, callback);
     		this.localMessageToken = localMessageToken;
     	}
     	
 		public void run() {
 		    Throwable throwable = null;
 			String messageSource = null;
 			Message message = null;
         	try {
         		maildirFolder.open();
         		messageSource = maildirFolder.getMessageSource(localMessageToken);
         		maildirFolder.close();
         		
         		// Parse the message source
         		Hashtable contentMap = new Hashtable();
                 MimeMessagePart rootPart = MailMessageParser.parseRawMessage(contentMap, new ByteArrayInputStream(messageSource.getBytes()));
                 message = new Message(rootPart);
                 Enumeration e = contentMap.keys();
                 while(e.hasMoreElements()) {
                     Object element = e.nextElement();
                     if(!(element instanceof MimeMessagePart)) { continue; }
                 	MimeMessagePart part = (MimeMessagePart)element;
                	MimeMessageContent content = (MimeMessageContent)contentMap.get(part);
                	// Local parts are always complete, regardless of what the parser thinks
                	content.setPartComplete(MimeMessageContent.PART_COMPLETE);
                	message.putContent(part, content);
                 }
         	} catch (IOException e) {
                 EventLogger.logEvent(AppInfo.GUID, ("Unable to read message: " + e.toString()).getBytes(), EventLogger.ERROR);
         		message = null;
         		messageSource = null;
         		throwable = e;
         	}
         	
         	if(message != null && messageSource != null) {
                 if(callback != null) { callback.mailStoreRequestComplete(); }
         		fireMessageAvailable(localMessageToken, true, message.getStructure(), message.getAllContent(), messageSource);
         	}
         	else {
                 if(callback != null) { callback.mailStoreRequestFailed(throwable); }
         	}
 		}
     }
 
     public void requestMessageParts(MessageToken messageToken, MimeMessagePart[] messageParts, MailStoreRequestCallback callback) {
     	throw new UnsupportedOperationException();
     }
     
     public void requestMessageDelete(MessageToken messageToken, MessageFlags messageFlags, MailStoreRequestCallback callback) {
         LocalMessageToken localMessageToken = (LocalMessageToken)messageToken;
         FolderTreeItem requestFolder = getMatchingFolderTreeItem(localMessageToken.getFolderPath());
         
         if(requestFolder != null && !messageFlags.isDeleted()) {
             MessageFlags newFlags = copyMessageFlags(messageFlags);
             newFlags.setDeleted(true);
             threadQueue.invokeLater(new UpdateMessageFlagsRunnable(requestFolder, localMessageToken, newFlags, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
 
     public void requestMessageUndelete(MessageToken messageToken, MessageFlags messageFlags, MailStoreRequestCallback callback) {
         LocalMessageToken localMessageToken = (LocalMessageToken)messageToken;
         FolderTreeItem requestFolder = getMatchingFolderTreeItem(localMessageToken.getFolderPath());
         
         if(requestFolder != null && messageFlags.isDeleted()) {
             MessageFlags newFlags = copyMessageFlags(messageFlags);
             newFlags.setDeleted(false);
             threadQueue.invokeLater(new UpdateMessageFlagsRunnable(requestFolder, localMessageToken, newFlags, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
 
     public void requestMessageAnswered(MessageToken messageToken, MessageFlags messageFlags, MailStoreRequestCallback callback) {
         LocalMessageToken localMessageToken = (LocalMessageToken)messageToken;
         FolderTreeItem requestFolder = getMatchingFolderTreeItem(localMessageToken.getFolderPath());
         
         if(requestFolder != null && !messageFlags.isAnswered()) {
             MessageFlags newFlags = copyMessageFlags(messageFlags);
             newFlags.setAnswered(true);
             threadQueue.invokeLater(new UpdateMessageFlagsRunnable(requestFolder, localMessageToken, newFlags, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
     
     public void requestMessageForwarded(MessageToken messageToken, MessageFlags messageFlags, MailStoreRequestCallback callback) {
         LocalMessageToken localMessageToken = (LocalMessageToken)messageToken;
         FolderTreeItem requestFolder = getMatchingFolderTreeItem(localMessageToken.getFolderPath());
         
         if(requestFolder != null && !messageFlags.isForwarded()) {
             MessageFlags newFlags = copyMessageFlags(messageFlags);
             newFlags.setForwarded(true);
             threadQueue.invokeLater(new UpdateMessageFlagsRunnable(requestFolder, localMessageToken, newFlags, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
     
     public void requestMessageSeen(MessageToken messageToken, MessageFlags messageFlags, MailStoreRequestCallback callback) {
         LocalMessageToken localMessageToken = (LocalMessageToken)messageToken;
         FolderTreeItem requestFolder = getMatchingFolderTreeItem(localMessageToken.getFolderPath());
         
         if(requestFolder != null && !messageFlags.isSeen()) {
             MessageFlags newFlags = copyMessageFlags(messageFlags);
             newFlags.setSeen(true);
             threadQueue.invokeLater(new UpdateMessageFlagsRunnable(requestFolder, localMessageToken, newFlags, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
     
     public void requestMessageUnseen(MessageToken messageToken, MessageFlags messageFlags, MailStoreRequestCallback callback) {
         LocalMessageToken localMessageToken = (LocalMessageToken)messageToken;
         FolderTreeItem requestFolder = getMatchingFolderTreeItem(localMessageToken.getFolderPath());
         
         if(requestFolder != null && messageFlags.isSeen()) {
             MessageFlags newFlags = copyMessageFlags(messageFlags);
             newFlags.setSeen(false);
             threadQueue.invokeLater(new UpdateMessageFlagsRunnable(requestFolder, localMessageToken, newFlags, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
 
     private class UpdateMessageFlagsRunnable extends MaildirRunnable {
         private LocalMessageToken localMessageToken;
         private MessageFlags messageFlags;
         
         public UpdateMessageFlagsRunnable(
                 FolderTreeItem requestFolder,
                 LocalMessageToken localMessageToken,
                 MessageFlags messageFlags,
                 MailStoreRequestCallback callback) {
             super(requestFolder, callback);
             this.localMessageToken = localMessageToken;
             this.messageFlags = messageFlags;
         }
 
         public void run() {
             Throwable throwable = null;
             boolean flagsUpdated = false;
             boolean success;
             try {
                 maildirFolder.open();
                 flagsUpdated = maildirFolder.setMessageFlags(localMessageToken, messageFlags);
                 maildirFolder.close();
                 success = true;
             } catch (IOException e) {
                 EventLogger.logEvent(AppInfo.GUID, ("Unable to read folder: " + e.toString()).getBytes(), EventLogger.ERROR);
                 success = false;
                 throwable = e;
             }
             
             if(success) {
                 if(callback != null) { callback.mailStoreRequestComplete(); }
             }
             else {
                 if(callback != null) { callback.mailStoreRequestFailed(throwable); }
             }
 
             if(flagsUpdated) {
                 fireMessageFlagsChanged(localMessageToken, messageFlags);
             }
         }
     }
     
     public void requestMessageAppend(FolderTreeItem folder, String rawMessage, MessageFlags initialFlags, MailStoreRequestCallback callback) {
         FolderTreeItem requestFolder = getMatchingFolderTreeItem(folder.getPath());
         
         if(requestFolder != null && rawMessage != null && rawMessage.length() > 0 && initialFlags != null) {
         	threadQueue.invokeLater(new RequestMessageAppendRunnable(requestFolder, rawMessage, initialFlags, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
 
     private class RequestMessageAppendRunnable extends MaildirRunnable {
     	private String rawMessage;
     	private MessageFlags initialFlags;
     	
     	public RequestMessageAppendRunnable(FolderTreeItem requestFolder, String rawMessage, MessageFlags initialFlags, MailStoreRequestCallback callback) {
     		super(requestFolder, callback);
     		this.rawMessage = rawMessage;
     		this.initialFlags = initialFlags;
     	}
     	
 		public void run() {
 		    Throwable throwable = null;
         	FolderMessage folderMessage = null;
         	try {
         		maildirFolder.open();
         		folderMessage = maildirFolder.appendMessage(rawMessage, initialFlags);
         		maildirFolder.close();
         	} catch (IOException e) {
                 EventLogger.logEvent(AppInfo.GUID, ("Unable to read folder: " + e.toString()).getBytes(), EventLogger.ERROR);
         		throwable = e;
         	}
         	
         	if(folderMessage != null) {
                 if(callback != null) { callback.mailStoreRequestComplete(); }
         		fireFolderMessagesAvailable(requestFolder, new FolderMessage[] { folderMessage }, false);
         	}
         	else {
                 if(callback != null) { callback.mailStoreRequestFailed(throwable); }
         	}
 		}
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.mail.AbstractMailStore#requestMessageCopy(org.logicprobe.LogicMail.mail.MessageToken, org.logicprobe.LogicMail.mail.FolderTreeItem)
      */
     public void requestMessageCopy(MessageToken messageToken, FolderTreeItem destinationFolder, MailStoreRequestCallback callback) {
         LocalMessageToken localMessageToken = (LocalMessageToken)messageToken;
         FolderTreeItem fromFolder = getMatchingFolderTreeItem(localMessageToken.getFolderPath());
         FolderTreeItem toFolder = getMatchingFolderTreeItem(destinationFolder.getPath());
         if(fromFolder != null && toFolder != null) {
             threadQueue.invokeLater(new MessageCopyRunnable(localMessageToken, fromFolder, toFolder, callback));
         }
         else {
             if(callback != null) { callback.mailStoreRequestFailed(null); }
         }
     }
     
     private class MessageCopyRunnable implements Runnable {
         private LocalMessageToken localMessageToken;
         private FolderTreeItem toFolder;
         private MaildirFolder fromMaildirFolder;
         private MaildirFolder toMaildirFolder;
         private MailStoreRequestCallback callback;
 
         public MessageCopyRunnable(LocalMessageToken localMessageToken, FolderTreeItem fromFolder, FolderTreeItem toFolder, MailStoreRequestCallback callback) {
             this.localMessageToken = localMessageToken;
             this.toFolder = toFolder;
             this.fromMaildirFolder = getMaildirFolder(fromFolder);
             this.toMaildirFolder = getMaildirFolder(toFolder);
             this.callback = callback;
         }
 
         public void run() {
             Throwable throwable = null;
             FolderMessage copiedMessage = null;
             try {
                 fromMaildirFolder.open();
                 toMaildirFolder.open();
 
                 String messageSource = fromMaildirFolder.getMessageSource(localMessageToken);
                 copiedMessage = toMaildirFolder.appendMessage(
                         messageSource,
                         new MessageFlags(false, false, false, false, false, true, false, false));
 
                 toMaildirFolder.close();
                 fromMaildirFolder.close();
             } catch (IOException e) {
                 EventLogger.logEvent(AppInfo.GUID, ("Unable to read folder: " + e.toString()).getBytes(), EventLogger.ERROR);
                 throwable = e;
             }
             
             if(copiedMessage != null) {
                 if(callback != null) { callback.mailStoreRequestComplete(); }
                 fireFolderMessagesAvailable(toFolder, new FolderMessage[] { copiedMessage }, false);
             }
             else {
                 if(callback != null) { callback.mailStoreRequestFailed(throwable); }
             }
         }
     }
     
     /**
      * Gets the matching folder tree item for the parameter.
      * This method ensures that we are working with a FolderTreeItem object
      * owned by this mail store, even if the provided parameter is a
      * separately created object with similar properties.
      * 
      * @param folderPath The folder path.
      * @return The matching folder tree item.
      */
     private FolderTreeItem getMatchingFolderTreeItem(String folderPath) {
         FolderTreeItem[] localFolders = rootFolder.children();
         FolderTreeItem requestFolder = null;
         for (int i = 0; i < localFolders.length; i++) {
         	if(localFolders[i].getPath().equals(folderPath)) {
         		requestFolder = localFolders[i];
         		break;
         	}
         }
     	return requestFolder;
     }
     
     private abstract class MaildirRunnable implements Runnable {
     	protected FolderTreeItem requestFolder;
     	protected MaildirFolder maildirFolder;
     	protected MailStoreRequestCallback callback;
     	
     	public MaildirRunnable(FolderTreeItem requestFolder, MailStoreRequestCallback callback) {
     		this.requestFolder = requestFolder;
     		this.maildirFolder = getMaildirFolder(requestFolder);
     		this.callback = callback;
     	}
     }
     
     private MaildirFolder getMaildirFolder(FolderTreeItem requestFolder) {
         MaildirFolder maildirFolder;
         if(folderMaildirMap.containsKey(requestFolder)) {
             maildirFolder = (MaildirFolder)folderMaildirMap.get(requestFolder);
         }
         else {
             String folderUrl = globalConfig.getLocalDataLocation() + "local/";
             try {
                 FileConnection fileConnection = (FileConnection)Connector.open(folderUrl);
                 if(!fileConnection.exists()) {
                     fileConnection.mkdir();
                 }
                 fileConnection.close();
             } catch (IOException e) {
                 EventLogger.logEvent(AppInfo.GUID, ("Error preparing root path: " + e.toString()).getBytes(), EventLogger.ERROR);
             }
             
             StringBuffer buf = new StringBuffer();
             buf.append(folderUrl);
             buf.append(requestFolder.getPath());
             maildirFolder = new MaildirFolder(requestFolder.getPath(), buf.toString());
             folderMaildirMap.put(requestFolder, maildirFolder);
         }
         return maildirFolder;
     }
     
     private static MessageFlags copyMessageFlags(MessageFlags sourceFlags) {
         return new MessageFlags(
                 sourceFlags.isSeen(),
                 sourceFlags.isAnswered(),
                 sourceFlags.isFlagged(),
                 sourceFlags.isDeleted(),
                 sourceFlags.isDraft(),
                 sourceFlags.isRecent(),
                 sourceFlags.isForwarded(),
                 sourceFlags.isJunk());
     }
 }
