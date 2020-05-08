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
 
 package org.logicprobe.LogicMail.ui;
 
 import java.io.IOException;
 import java.util.Calendar;
 import net.rim.device.api.system.Application;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.i18n.SimpleDateFormat;
 import net.rim.device.api.ui.DrawStyle;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.Keypad;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.ListField;
 import net.rim.device.api.ui.component.ListFieldCallback;
 import net.rim.device.api.ui.component.Menu;
 import org.logicprobe.LogicMail.conf.ImapConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.mail.FolderTreeItem;
 import org.logicprobe.LogicMail.mail.IncomingMailClient;
 import org.logicprobe.LogicMail.message.FolderMessage;
 import org.logicprobe.LogicMail.mail.MailException;
 import org.logicprobe.LogicMail.message.MessageEnvelope;
 
 /**
  * Display the active mailbox listing.
  * If the supplied folder matches the configured sent folder
  * for the provided account, then the display fields will be
  * adjusted accordingly.
  */
 public class MailboxScreen extends BaseScreen {
     private FolderMessage[] messages;
     private ListField msgList;
     private boolean isSentFolder;
     
     // Message icons
     private Bitmap bmapOpened;
     private Bitmap bmapUnopened;
     private Bitmap bmapReplied;
     private Bitmap bmapFlagged;
     private Bitmap bmapDraft;
     private Bitmap bmapDeleted;
     private Bitmap bmapUnknown;
     private Bitmap bmapJunk;
 
     private MailSettings mailSettings;
     private FolderTreeItem folderItem;
     private IncomingMailClient client;
     private RefreshMessageListHandler refreshMessageListHandler;
     
     // Things to calculate in advance
     private static int lineHeight;
     private static int dateWidth;
     private static int senderWidth;
     private static int maxWidth;
     
     public MailboxScreen(IncomingMailClient client, FolderTreeItem folderItem) {
         super(folderItem.getName());
         mailSettings = MailSettings.getInstance();
         this.folderItem = folderItem;
         this.client = client;
 
         // Load message icons
         bmapOpened = Bitmap.getBitmapResource("mail_opened.png");
         bmapUnopened = Bitmap.getBitmapResource("mail_unopened.png");
         bmapReplied = Bitmap.getBitmapResource("mail_replied.png");
         bmapFlagged = Bitmap.getBitmapResource("mail_flagged.png");
         bmapDraft = Bitmap.getBitmapResource("mail_draft.png");
         bmapDeleted = Bitmap.getBitmapResource("mail_deleted.png");
         bmapUnknown = Bitmap.getBitmapResource("mail_unknown.png");
         bmapJunk = Bitmap.getBitmapResource("mail_junk.png");
 
         messages = new FolderMessage[0];
         
         // add field elements
         msgList = new ListField();
         lineHeight = msgList.getRowHeight();
         msgList.setRowHeight(lineHeight * 2);
         
         msgList.setCallback(new ListFieldCallback() {
             public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
                 msgList_drawListRow(listField, graphics, index, y, width);
             }
             public int getPreferredWidth(ListField listField) {
                 return msgList_getPreferredWidth(listField);
             }
             public Object get(ListField listField, int index) {
                 return msgList_get(listField, index);
             }
             public int indexOfList(ListField listField, String prefix, int start) {
                 return msgList_indexOfList(listField, prefix, start);
             }
         });
         
         add(msgList);
         
         // Determine field sizes
         maxWidth = Graphics.getScreenWidth();
         dateWidth = Font.getDefault().getAdvance("00/0000");
         senderWidth = maxWidth - dateWidth - 20;
 
         // Determine if this screen is viewing the sent folder
         if(client.getAcctConfig() instanceof ImapConfig) {
             String sentFolderPath = ((ImapConfig)client.getAcctConfig()).getSentFolder();
             if(sentFolderPath != null) {
                 this.isSentFolder = folderItem.getPath().equals(sentFolderPath);
             }
         }
         else {
             this.isSentFolder = false;
         }
         
         if(client != null) {
             refreshMessageList();
         }
     }
 
     private void refreshMessageList() {
         // Initialize the handler on demand
         if(refreshMessageListHandler == null) {
             refreshMessageListHandler = new RefreshMessageListHandler(folderItem);
             refreshMessageListHandler.setListener(new MailClientHandlerListener() {
                 public void mailActionComplete(MailClientHandler source, boolean result) {
                     refreshMessageListHandler_mailActionComplete(source, result);
                 }
             });
         }
 
         // Start the background process
         refreshMessageListHandler.start();
     }
 
     protected boolean onSavePrompt() {
         return true;
     }
 
     public boolean onClose() {
         if(checkClose()) {
             close();
             return true;
         }
         else
             return false;
     }
 
     private boolean checkClose() {
         // Immediately close without prompting if we are
         // using a protocol that supports folders.
         if(client.hasFolders()) {
             return true;
         }
         
         // Otherwise we are on the main screen for the account, so prompt
         // before closing the connection
         if(client.isConnected()) {
             if(Dialog.ask(Dialog.D_YES_NO, "Disconnect from server?") == Dialog.YES) {
                 try { client.close(); } catch (Exception exp) { }
                 return true;
             }
             else
                 return false;
         }
         else {
             return true;
         }
     }
 
     private MenuItem selectItem = new MenuItem("Select", 100, 10) {
         public void run() {
             openSelectedMessage();
         }
     };
     private MenuItem compositionItem = new MenuItem("Compose E-Mail", 120, 10) {
         public void run() {
             UiApplication.getUiApplication().pushScreen(new CompositionScreen(client.getAcctConfig()));
         }
     };
     private MenuItem deleteItem = new MenuItem("Delete", 130, 10) {
         public void run() {
             if(Dialog.ask(Dialog.D_YES_NO, "Are you sure you want to delete this message?") == Dialog.YES) {
                 int index = msgList.getSelectedIndex();
                 if(index >= 0 && index < msgList.getSize() && messages[index] != null) {
                     DeleteMessageHandler deleteMessageHandler = new DeleteMessageHandler(messages[index], true);
                     deleteMessageHandler.setListener(new MailClientHandlerListener() {
                         public void mailActionComplete(MailClientHandler source, boolean result) {
                             source.setListener(null);
                             msgList.setDirty(true);
                             msgList.invalidate();
                         }
                     });
                     deleteMessageHandler.start();
                 }
             }
         }
     };
     private MenuItem undeleteItem = new MenuItem("Undelete", 135, 10) {
         public void run() {
             int index = msgList.getSelectedIndex();
             if(index >= 0 && index < msgList.getSize() && messages[index] != null) {
                 DeleteMessageHandler deleteMessageHandler = new DeleteMessageHandler(messages[index], false);
                 deleteMessageHandler.setListener(new MailClientHandlerListener() {
                     public void mailActionComplete(MailClientHandler source, boolean result) {
                         source.setListener(null);
                         msgList.setDirty(true);
                         msgList.invalidate();
                     }
                 });
                 deleteMessageHandler.start();
             }
         }
     };
 
     protected void makeMenu(Menu menu, int instance) {
         menu.add(selectItem);
         if(this.client.getAcctConfig().getOutgoingConfig() != null) {
             menu.add(compositionItem);
         }
         int index = msgList.getSelectedIndex();
         if(index >= 0 && index < msgList.getSize() && messages[index] != null) {
             if(messages[index].isDeleted()) {
                 if(client.hasUndelete()) {
                     menu.add(undeleteItem);
                 }
             }
             else {
                 menu.add(deleteItem);
             }
         }
         super.makeMenu(menu, instance);
     }
     
     /**
      * Draw a row of the message list.
      * Currently using a double row so that more meaningful
      * information can be displayed for each message entry.
      * Actual rendering is crude at the moment, and needs to
      * be reworked to use relative positioning for everything.
      */
     public void msgList_drawListRow(ListField listField,
                                     Graphics graphics,
                                     int index,
                                     int y,
                                     int width)
     {
         // sanity check
         if(index >= messages.length) {
             return;
         }
         FolderMessage entry = (FolderMessage)messages[index];
         MessageEnvelope env = entry.getEnvelope();
         graphics.drawBitmap(1, y, 20, lineHeight*2, getIconForMessage(entry), 0, 0);
             
         Font origFont = graphics.getFont();
         graphics.setFont(origFont.derive(Font.BOLD));
 
         if(isSentFolder) {
             if(env.to != null && env.to.length > 0) {
                 graphics.drawText((String)env.to[0], 20, y,
                                   (int)(getStyle() | DrawStyle.ELLIPSIS),
                                    senderWidth);
             }
         }
         else {
             if(env.from != null && env.from.length > 0) {
                 graphics.drawText((String)env.from[0], 20, y,
                                   (int)(getStyle() | DrawStyle.ELLIPSIS),
                                    senderWidth);
             }
         }
         graphics.setFont(origFont.derive(Font.PLAIN));
         if(env.subject != null) {
             graphics.drawText((String)env.subject, 20, y+lineHeight,
                               (int)(getStyle() | DrawStyle.ELLIPSIS),
                                maxWidth-20);
         }
         graphics.setFont(origFont);
         
         // Current time
         // Perhaps this should only be set on initialization
         // of this screen, and/or new message downloads
         if(env.date != null) {
             Calendar nowCal = Calendar.getInstance();
     
             Calendar dispCal = Calendar.getInstance();
             dispCal.setTime(env.date);
 
             SimpleDateFormat dateFormat;
 
             // Determine the date format to display,
             // based on the distance from the current time
             if(nowCal.get(Calendar.YEAR) == dispCal.get(Calendar.YEAR))
                 if((nowCal.get(Calendar.MONTH) == dispCal.get(Calendar.MONTH)) &&
                 (nowCal.get(Calendar.DAY_OF_MONTH) == dispCal.get(Calendar.DAY_OF_MONTH)))
                     dateFormat = new SimpleDateFormat("h:mma");
                 else
                     dateFormat = new SimpleDateFormat("MM/dd");
             else
                 dateFormat = new SimpleDateFormat("MM/yyyy");
         
             StringBuffer buffer = new StringBuffer();
             dateFormat.format(dispCal, buffer, null);
             graphics.setFont(origFont.derive(Font.BOLD));
             graphics.drawText(buffer.toString(), senderWidth+20, y,
                               (int)(getStyle() | DrawStyle.ELLIPSIS),
                               dateWidth);
             graphics.setFont(origFont);
         }
     }
     
     private Bitmap getIconForMessage(FolderMessage message) {
         if(message.isDeleted())
             return bmapDeleted;
         else if(message.isJunk())
             return bmapJunk;
         else if(message.isAnswered())
             return bmapReplied;
         else if(message.isFlagged())
             return bmapFlagged;
         else if(message.isDraft())
             return bmapDraft;
         else if(message.isRecent())
             return bmapUnopened;
         else if(message.isSeen())
             return bmapOpened;
         else
             return bmapUnknown;
     }
     
     public int msgList_getPreferredWidth(ListField listField) {
         return Graphics.getScreenWidth();
     }
     
     public Object msgList_get(ListField listField, int index) {
         return (Object)messages[index];
     }
     
     public int msgList_indexOfList(ListField listField,
                                    String prefix,
                                    int start)
     {
         return 0;
     }
 
     private void openSelectedMessage()
     {
         int index = msgList.getSelectedIndex();
         if(index < 0 || index > messages.length) {
             return;
         }
         
         UiApplication.getUiApplication().pushScreen(new MessageScreen(client, folderItem, messages[index]));
     }
 
     public boolean keyChar(char key,
                            int status,
                            int time)
     {
         boolean retval = false;
         switch(key) {
             case Keypad.KEY_ENTER:
                 openSelectedMessage();
                 retval = true;
                 break;
         }
         return retval;
     }
 
     public void refreshMessageListHandler_mailActionComplete(MailClientHandler source, boolean result) {
         if(source.equals(refreshMessageListHandler)) {
             if(refreshMessageListHandler.getFolderMessages() != null) {
                 FolderMessage[] folderMessages = refreshMessageListHandler.getFolderMessages();
                 boolean hideDeleted = mailSettings.getGlobalConfig().getHideDeletedMsg();
                 
                 // Count the number of deleted messages
                 int numDeleted = 0;
                 if(hideDeleted) {
                     for(int i=0; i<folderMessages.length; i++) {
                         if(folderMessages[i].isDeleted()) {
                             numDeleted++;
                         }
                     }
                 }
                 
                 synchronized(Application.getEventLock()) {
                     int junks = 0;
                     if(mailSettings.getGlobalConfig().getDispOrder()) {
                         messages = new FolderMessage[folderMessages.length - numDeleted];
                         int j = 0;
                         for(int i=0; i<folderMessages.length; i++) {
                             if(!hideDeleted || !folderMessages[i].isDeleted()) {
                                 messages[j++] = folderMessages[i];
                             }
                         }
                     }
                     else {
                         messages = new FolderMessage[folderMessages.length - numDeleted];
                         int j = 0;
                         for(int i=folderMessages.length-1;i>=0;i--) {
                             if(!hideDeleted || !folderMessages[i].isDeleted()) {
                                 messages[j++] = folderMessages[i];
                             }
                         }
                     }
                     int size = msgList.getSize();
                     for(int i=0;i<size;i++)
                         msgList.delete(0);
                     for(int i=0;i<messages.length;i++)
                         msgList.insert(i);
 
                     msgList.setDirty(true);
                 }
             }
         }
     }
 
     /**
      * Implements the message list refresh action
      */
     private class RefreshMessageListHandler extends MailClientHandler {
         private FolderTreeItem folderItem;
         private FolderMessage[] folderMessages;
         
         public RefreshMessageListHandler(FolderTreeItem folderItem) {
             super(MailboxScreen.this.client, "Retrieving message list");
             this.folderItem = folderItem;
         }
 
         public void runSession() throws IOException, MailException {
             FolderMessage[] folderMessages;
             try {
                 ((IncomingMailClient)client).setActiveFolder(folderItem);
                 int firstIndex = folderItem.getMsgCount() - mailSettings.getGlobalConfig().getRetMsgCount();
                firstIndex = Math.max(1, firstIndex);
                
                 folderMessages = ((IncomingMailClient)client).getFolderMessages(firstIndex, folderItem.getMsgCount());
             } catch (MailException exp) {
                 folderMessages = null;
                 throw exp;
             }
             this.folderMessages = folderMessages;
         }
         
         public FolderMessage[] getFolderMessages() {
             return folderMessages;
         }
     }
     
     /**
      * Implements message flag changes
      */
     private class DeleteMessageHandler extends MailClientHandler {
         private FolderMessage folderMessage;
         private boolean delete;
         
         public DeleteMessageHandler(FolderMessage folderMessage, boolean delete) {
             super(MailboxScreen.this.client, "");
             if(delete) {
                 this.changeStatusMessage("Deleting message");
             }
             else {
                 this.changeStatusMessage("Undeleting message");
             }
             this.folderMessage = folderMessage;
             this.delete = delete;
         }
 
         public void runSession() throws IOException, MailException {
             if(delete) {
                 ((IncomingMailClient)client).deleteMessage(folderMessage);
             }
             else {
                 ((IncomingMailClient)client).undeleteMessage(folderMessage);
             }
         }
         
         public FolderMessage getFolderMessage() {
             return folderMessage;
         }
     }
 }
