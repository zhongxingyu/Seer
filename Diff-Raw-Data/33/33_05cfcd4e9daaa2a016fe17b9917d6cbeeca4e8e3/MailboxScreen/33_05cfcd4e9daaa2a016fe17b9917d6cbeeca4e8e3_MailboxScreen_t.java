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
 
 package org.logicprobe.LogicMail.ui;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import net.rim.device.api.i18n.DateFormat;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Keypad;
 import net.rim.device.api.ui.Manager;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.Screen;
 import net.rim.device.api.ui.component.Dialog;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.container.VerticalFieldManager;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.util.Comparator;
 import net.rim.device.api.util.DateTimeUtilities;
 
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.conf.GlobalConfig;
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.conf.MailSettingsEvent;
 import org.logicprobe.LogicMail.conf.MailSettingsListener;
 import org.logicprobe.LogicMail.model.MailboxNode;
 import org.logicprobe.LogicMail.model.MailboxNodeEvent;
 import org.logicprobe.LogicMail.model.MailboxNodeListener;
 import org.logicprobe.LogicMail.model.MessageNode;
 import org.logicprobe.LogicMail.model.MessageNodeEvent;
 import org.logicprobe.LogicMail.model.MessageNodeListener;
 import org.logicprobe.LogicMail.util.EventObjectRunnable;
 
 /**
  * Display the active mailbox listing.
  * If the supplied folder matches the configured sent folder
  * for the provided account, then the display fields will be
  * adjusted accordingly.
  */
 public class MailboxScreen extends AbstractScreenProvider {
 	private static final int SHORTCUT_COMPOSE = 0;
 	private static final int SHORTCUT_UP = 3;
 	private static final int SHORTCUT_DOWN = 4;
 	
 	private MailboxNode mailboxNode;
     private Vector knownMessages;
     private Hashtable messageFieldMap;
     private boolean firstDisplay = true;
     private MailSettings mailSettings;
     private GlobalConfig globalConfig;
     private VerticalFieldManager messageFieldManager;
     private boolean navigationMoved;
     private boolean displayOrder;
     private boolean hideDeleted;
     private Screen screen;
     private MessageActions messageActions;
     private boolean composeEnabled;
 
 	private MenuItem compositionItem;
     
     /**
      * Initializes a new MailboxScreen to view the provided mailbox.
      * 
      * @param mailboxNode Mailbox node to view.
      */
     public MailboxScreen(MailboxNode mailboxNode) {
     	this.mailboxNode = mailboxNode;
     	this.knownMessages = new Vector();
     	this.messageFieldMap = new Hashtable();
     	this.mailSettings = MailSettings.getInstance();
     	this.globalConfig = this.mailSettings.getGlobalConfig();
     }
 
     public String getTitle() {
     	return mailboxNode.toString();
     }
     
     /**
      * Gets the mailbox node being displayed by this screen.
      * 
      * @return The mailbox node
      */
     public MailboxNode getMailboxNode() {
     	return mailboxNode;
     }
     
     private MailSettingsListener mailSettingsListener = new MailSettingsListener() {
         public void mailSettingsSaved(MailSettingsEvent e) {
             if((e.getGlobalChange() & GlobalConfig.CHANGE_TYPE_OTHER) != 0) {
                 if(globalConfig.getDispOrder() != displayOrder) {
                     UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
                         public void run() {
                             displayOrder = !displayOrder;
                             displayableChanged();
                         }
                     });
                 }
                 if(globalConfig.getHideDeletedMsg() != hideDeleted) {
                     UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
                         public void run() {
                             hideDeleted = !hideDeleted;
                             displayableChanged();
                         }
                     });
                 }
             }
         }
     };
     
     /** The mailbox node listener. */
     private MailboxNodeListener mailboxNodeListener = new MailboxNodeListener() {
 		public void mailboxStatusChanged(MailboxNodeEvent e) {
 			UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
 				public void run() {
 					mailboxNode_MailboxStatusChanged((MailboxNodeEvent)getEvent());
 				}
 			});
 		}
     };
     
     /** The message node listener. */
     private MessageNodeListener messageNodeListener = new MessageNodeListener() {
 		public void messageStatusChanged(MessageNodeEvent e) {
 			UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
 				public void run() {
 					messageNode_MessageStatusChanged((MessageNodeEvent)getEvent());
 				}
 			});
 		}
     };
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.BaseScreen#onDisplay()
      */
     public void onDisplay() {
         if(this.hideDeleted != globalConfig.getHideDeletedMsg()) {
             this.hideDeleted = !this.hideDeleted;
             displayableChanged();
         }
         
         if(this.displayOrder != globalConfig.getDispOrder()) {
             this.displayOrder = !this.displayOrder;
             displayableChanged();
         }
         this.mailSettings.addMailSettingsListener(mailSettingsListener);
         this.mailboxNode.addMailboxNodeListener(mailboxNodeListener);
         
         if(firstDisplay) {
             MessageNode[] initialMessages = this.mailboxNode.getMessages();
             for(int i=0; i<initialMessages.length; i++) {
             	knownMessages.addElement(initialMessages[i]);
             	if(isMessageDisplayable(initialMessages[i])) {
             		insertDisplayableMessage(initialMessages[i]);
             	}
             }
             
         	this.mailboxNode.refreshMessages();
         	firstDisplay = false;
         }
         int size = knownMessages.size();
         for(int i=0; i<size; i++) {
         	((MessageNode)knownMessages.elementAt(i)).addMessageNodeListener(messageNodeListener);
         }
         
         composeEnabled = mailboxNode.getParentAccount().hasMailSender();
         ((StandardScreen)screen).setShortcutEnabled(SHORTCUT_COMPOSE, composeEnabled);
         
         // TODO: Support message list changes between display pushing
         // TODO: Support updating when messages are deleted
     }
     
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.ui.BaseScreen#onUndisplay()
 	 */
 	public void onUndisplay() {
         this.mailSettings.removeMailSettingsListener(mailSettingsListener);
         this.mailboxNode.removeMailboxNodeListener(mailboxNodeListener);
         int size = knownMessages.size();
         for(int i=0; i<size; i++) {
         	((MessageNode)knownMessages.elementAt(i)).removeMessageNodeListener(messageNodeListener);
         }
     }
     
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.ui.AbstractScreenProvider#hasShortcuts()
 	 */
 	public boolean hasShortcuts() {
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.logicprobe.LogicMail.ui.AbstractScreenProvider#getShortcuts()
 	 */
 	public ShortcutItem[] getShortcuts() {
 		// Note: This method is only called once, during initialization of the screen,
 		// and only on devices that have touchscreen support.  The strings for the
 		// shortcuts are contained within the main application library's resources.
 		// However, the icons are contained within the platform support library
 		// containing actual touchscreen API support.
 		return new ShortcutItem[] {
 			new ShortcutItem(
 					SHORTCUT_COMPOSE,
 					resources.getString(LogicMailResource.MENUITEM_COMPOSE_EMAIL),
 					"shortcut-compose.png", "shortcut-compose-d.png"),
 			null,
 			null,
 			new ShortcutItem(
 					SHORTCUT_UP,
 					resources.getString(LogicMailResource.MENUITEM_SCROLL_UP),
 					"shortcut-up.png", "shortcut-up-d.png"),
 			new ShortcutItem(
 					SHORTCUT_DOWN,
 					resources.getString(LogicMailResource.MENUITEM_SCROLL_DOWN),
 					"shortcut-down.png", "shortcut-down-d.png")
 		};
 	}
 	
 	/**
 	 * Initializes the fields.
 	 */
 	public void initFields(Screen screen) {
 		messageFieldManager = new VerticalFieldManager(Manager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR) {
 		    protected boolean navigationMovement(int dx, int dy, int status, int time) {
 		        navigationMoved = true;
 		        return super.navigationMovement(dx, dy, status, time);
 		    }
 		};
         screen.add(messageFieldManager);
         this.screen = screen;
     	this.messageActions = navigationController.getMessageActions();
         initMenuItems();
 	}    
 	
 	private void initMenuItems() {
 	    compositionItem = new MenuItem(resources, LogicMailResource.MENUITEM_COMPOSE_EMAIL, 400100, 2000) {
 	        public void run() {
 	        	navigationController.displayComposition(mailboxNode.getParentAccount());
 	        }
 	    };
 	}
 
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.BaseScreen#makeMenu(net.rim.device.api.ui.component.Menu, int)
      */
     public void makeMenu(Menu menu, int instance) {
     	Field fieldWithFocus = messageFieldManager.getFieldWithFocus();
     	if(fieldWithFocus instanceof MailboxMessageField) {
     	    MessageNode messageNode = ((MailboxMessageField)fieldWithFocus).getMessageNode();
     	    messageActions.makeMenu(menu, instance, messageNode, false);
     	}
         if(composeEnabled) {
             menu.add(compositionItem);
         }
     }
     
     public boolean onClose() {
         // Check for deleted messages in the mailbox
         if(mailboxNode.getParentAccount().hasExpunge()
                 && mailboxNode.hasDeletedMessages()) {
             MailSettings mailSettings = MailSettings.getInstance();
             int expungeMode = globalConfig.getExpungeMode();
             if(expungeMode == GlobalConfig.EXPUNGE_PROMPT) {
                 Dialog dialog = new Dialog(
                         Dialog.D_YES_NO,
                         resources.getString(LogicMailResource.MAILBOX_EXPUNGE_PROMPT),
                         Dialog.NO,
                         Bitmap.getPredefinedBitmap(Bitmap.QUESTION), 0, true);
                 int choice = dialog.doModal();
 
                 // Request expunge if desired
                 if(choice == Dialog.YES) {
                     mailboxNode.expungeDeletedMessages();
                 }
 
                 if(dialog.getDontAskAgainValue()) {
                     if(choice == Dialog.YES) {
                         globalConfig.setExpungeMode(GlobalConfig.EXPUNGE_ALWAYS);
                         mailSettings.saveSettings();
                     }
                     else if(choice == Dialog.NO) {
                         globalConfig.setExpungeMode(GlobalConfig.EXPUNGE_NEVER);
                         mailSettings.saveSettings();
                     }
                 }
             }
             else if(expungeMode == GlobalConfig.EXPUNGE_ALWAYS) {
                 mailboxNode.expungeDeletedMessages();
             }
         }
 
         // Close the screen
         screen.close();
         return true;
     }
     
     /**
      * Handles mailbox status change events.
      * 
      * @param e Event data.
      */
     private void mailboxNode_MailboxStatusChanged(MailboxNodeEvent e) {
         int type = e.getType();
     	if(type == MailboxNodeEvent.TYPE_NEW_MESSAGES) {
     		MessageNode[] messageNodes = e.getAffectedMessages();
     		for(int i=0; i<messageNodes.length; i++) {
     			knownMessages.addElement(messageNodes[i]);
     			
     			if(isMessageDisplayable(messageNodes[i])) {
     				// Insert the message
 	    			insertDisplayableMessage(messageNodes[i]);
     			}
     			
     			if(screen != null && screen.isDisplayed()) {
     				messageNodes[i].addMessageNodeListener(messageNodeListener);
     			}
     		}
     	}
     	else if(type == MailboxNodeEvent.TYPE_DELETED_MESSAGES) {
             MessageNode[] messageNodes = e.getAffectedMessages();
             for(int i=0; i<messageNodes.length; i++) {
                 if(screen != null && screen.isDisplayed()) {
                     messageNodes[i].removeMessageNodeListener(messageNodeListener);
                 }
                 removeDisplayableMessage(messageNodes[i]);
                 knownMessages.removeElement(messageNodes[i]);
             }
     	}
 	}
     
     /**
      * Determines whether a message should be displayed,
      * per the configuration.
      * 
      * @param messageNode Message to check.
      * 
      * @return True if it should be displayed, false otherwise.
      */
     private boolean isMessageDisplayable(MessageNode messageNode) {
         if((messageNode.getFlags() & MessageNode.Flag.DELETED) != 0
                 && hideDeleted) {
             return false;
         }
         else {
             return true;
         }
     }
     
     /**
      * A configuration item affecting the displayable state of messages has
      * changed.  Since this is infrequent, this operation will take the simple
      * approach of emptying and repopulating the screen.
      */
     private void displayableChanged() {
         int size = knownMessages.size();
         if(size == 0) { return; }
         
         // Clear out all the existing content from the field manager and map
         messageFieldMap.clear();
         messageFieldManager.deleteAll();
         
         // Reset the flag that controls field focus behavior
         navigationMoved = false;
         
         // Get the known messages display them if appropriate
         for(int i=0; i<size; i++) {
             MessageNode messageNode = (MessageNode)knownMessages.elementAt(i);
             if(isMessageDisplayable(messageNode)) {
                 insertDisplayableMessage(messageNode);
             }
         }
     }
     
     /**
      * Insert a message into the list and associated data structures.
      * This will insert into the correct order, per the configuration.
      * 
      * @param messageNode Message to insert.
      */
     private void insertDisplayableMessage(MessageNode messageNode) {
     	Field selectedField = messageFieldManager.getFieldWithFocus();
     	
 		if(messageFieldManager.getFieldCount() > 0) {
 			Comparator comparator = MessageNode.getComparator();
 			int index = messageFieldManager.getFieldCount();
 			
 			if(displayOrder) {
 				// Ascending order
 				MessageNode lastMessage = getLastDisplayedMessage(index - 1);
 				while(lastMessage != null && index > 0 && comparator.compare(lastMessage, messageNode) >= 0) {
 					index--;
 					if(index > 0) { lastMessage = getLastDisplayedMessage(index - 1); }
 				}
 			}
 			else {
 				// Descending order
 				MessageNode lastMessage = getLastDisplayedMessage(index - 1);
 				while(lastMessage != null && index > 0 && comparator.compare(lastMessage, messageNode) <= 0) {
 					index--;
 					if(index > 0) { lastMessage = getLastDisplayedMessage(index - 1); }
 				}
 			}
 			MailboxMessageField mailboxMessageField =
 				new MailboxMessageField(mailboxNode, messageNode, Field.USE_ALL_WIDTH | Field.FOCUSABLE);
 			messageFieldMap.put(messageNode, mailboxMessageField);
             insertMessageField(mailboxMessageField, index);
 		}
 		else {
 			MailboxMessageField mailboxMessageField =
 				new MailboxMessageField(mailboxNode, messageNode, Field.USE_ALL_WIDTH | Field.FOCUSABLE);
 			messageFieldMap.put(messageNode, mailboxMessageField);
 			insertMessageField(mailboxMessageField, 0);
 		}
 		
 		if(!navigationMoved) {
 		    // Select newest message
             int count = messageFieldManager.getFieldCount();
             if(count > 0) {
                 if(displayOrder) {
                     messageFieldManager.getField(count - 1).setFocus();
                 }
                 else {
                     messageFieldManager.getField(0).setFocus();
                 }
             }
 		}
 		else {
 		    // Keep previous selection
             if(selectedField != null) { selectedField.setFocus(); }
 		}
     }
 
     /**
      * Remove a message from the list and associated data structures,
      * if that message exists in the list.
      * 
      * @param messageNode Message to insert.
      */
     private void removeDisplayableMessage(MessageNode messageNode) {
         MailboxMessageField mailboxMessageField = (MailboxMessageField)messageFieldMap.remove(messageNode);
         if(mailboxMessageField == null) { return; }
 
         deleteMessageField(mailboxMessageField);
     }
     
     private void insertMessageField(MailboxMessageField messageField, int index) {
         // It is assumed that the index will only be 0 for the first message.
         
         Date messageDate = messageField.getMessageNode().getDate();
         
         if(displayOrder) {
             // Ascending order
             if(messageFieldManager.getFieldCount() == 0) {
                 messageFieldManager.add(messageField);
                 messageFieldManager.add(new MessageSeparatorField(messageDate));
             }
             else {
                 if(index > 0 && messageFieldManager.getField(index - 1) instanceof MessageSeparatorField) {
                    Date sepDate = ((MessageSeparatorField)messageFieldManager.getField(index - 1)).getDate();
                    if(DateTimeUtilities.isSameDate(messageDate.getTime(), sepDate.getTime())) {
                        index--;
                    }
                 }
                 
                Field nextField;
                if(index < messageFieldManager.getFieldCount()) {
                    nextField = messageFieldManager.getField(index);
                }
                else {
                    nextField = null;
                }
                 
                 if(nextField instanceof MessageSeparatorField) {
                     Date sepDate = ((MessageSeparatorField)nextField).getDate();
                     if(DateTimeUtilities.isSameDate(messageDate.getTime(), sepDate.getTime())) {
                         messageFieldManager.insert(messageField, index);
                     }
                     else {
                         messageFieldManager.insert(new MessageSeparatorField(messageDate), index + 1);
                         messageFieldManager.insert(messageField, index + 1);
                     }
                 }
                else if(nextField instanceof MailboxMessageField) {
                     Date nextDate = ((MailboxMessageField)nextField).getMessageNode().getDate();
                     if(DateTimeUtilities.isSameDate(messageDate.getTime(), nextDate.getTime())) {
                         messageFieldManager.insert(messageField, index);
                     }
                     else {
                         messageFieldManager.insert(new MessageSeparatorField(messageDate), index);
                         messageFieldManager.insert(messageField, index);
                     }
                 }
                else {
                    messageFieldManager.insert(new MessageSeparatorField(messageDate), index);
                    messageFieldManager.insert(messageField, index);
                }
             }
         }
         else {
             // Descending order
             if(messageFieldManager.getFieldCount() == 0) {
                 messageFieldManager.add(new MessageSeparatorField(messageDate));
                 messageFieldManager.add(messageField);
             }
             else  {
                 Field prevField = messageFieldManager.getField(index - 1);

                if(prevField instanceof MessageSeparatorField) {
                    Date sepDate = ((MessageSeparatorField)prevField).getDate();
                    if(!DateTimeUtilities.isSameDate(messageDate.getTime(), sepDate.getTime())
                            && index > 1
                            && messageFieldManager.getField(index - 2) instanceof MailboxMessageField) {
                        index--;
                        prevField = messageFieldManager.getField(index - 1);
                    }
                }
                 
                 if(prevField instanceof MessageSeparatorField) {
                     Date sepDate = ((MessageSeparatorField)prevField).getDate();
                     if(DateTimeUtilities.isSameDate(messageDate.getTime(), sepDate.getTime())) {
                         messageFieldManager.insert(messageField, index);
                     }
                     else {
                         messageFieldManager.insert(new MessageSeparatorField(messageDate), index - 1);
                         messageFieldManager.insert(messageField, index);
                     }
                 }
                 else {
                     Date prevDate = ((MailboxMessageField)prevField).getMessageNode().getDate();
                     if(DateTimeUtilities.isSameDate(messageDate.getTime(), prevDate.getTime())) {
                         messageFieldManager.insert(messageField, index);
                     }
                     else {
                         messageFieldManager.insert(new MessageSeparatorField(messageDate), index);
                         messageFieldManager.insert(messageField, index + 1);
                     }
                 }
             }
         }
     }
     
     private void deleteMessageField(MailboxMessageField messageField) {
         int fieldCount = messageFieldManager.getFieldCount();
         int index = messageField.getIndex();
         
         if(displayOrder) {
             // Ascending order
             if(messageFieldManager.getField(index + 1) instanceof MessageSeparatorField
                     && (index == 0 || messageFieldManager.getField(index - 1) instanceof MessageSeparatorField)) {
                 messageFieldManager.deleteRange(index, 2);
             }
             else {
                 messageFieldManager.delete(messageField);
             }
         }
         else {
             // Descending order
             if(messageFieldManager.getField(index - 1) instanceof MessageSeparatorField
                     && (index == fieldCount - 1 || messageFieldManager.getField(index + 1) instanceof MessageSeparatorField)) {
                 messageFieldManager.deleteRange(index - 1, 2);
             }
             else {
                 messageFieldManager.delete(messageField);
             }
         }
     }
     
     /**
      * Gets the last displayed message.
      * 
      * @param index the index
      * 
      * @return the last displayed message
      */
     private MessageNode getLastDisplayedMessage(int index) {
     	while(index >= 0) {
     		if(messageFieldManager.getField(index) instanceof MailboxMessageField) {
     			return ((MailboxMessageField)messageFieldManager.getField(index)).getMessageNode();
     		}
     		index--;
     	}
     	return null;
     }
     
     /**
      * Handles message status change events.
      * 
      * @param e Event data.
      */
 	private void messageNode_MessageStatusChanged(MessageNodeEvent e) {
 		if(e.getType() == MessageNodeEvent.TYPE_FLAGS) {
 			MessageNode messageNode = (MessageNode)e.getSource();
 			boolean currentlyDisplayed = messageFieldMap.containsKey(messageNode);
 			boolean displayable = isMessageDisplayable(messageNode);
 			
 			if(currentlyDisplayed && !displayable) {
 				// Remove from display
 			    removeDisplayableMessage(messageNode);
 			}
 			else if(!currentlyDisplayed && displayable) {
 				// Add to display
 				insertDisplayableMessage(messageNode);
 			}
 			else if(currentlyDisplayed) {
 				// Just a visual flag update, so find and invalidate the item
 				MailboxMessageField mailboxMessageField = (MailboxMessageField)messageFieldMap.get(messageNode);
 				mailboxMessageField.invalidate();
 			}
 		}
 	}
 
     /**
      * Gets the selected message.
      * 
      * @return the selected message
      */
     private MessageNode getSelectedMessage() {
     	Field fieldWithFocus = messageFieldManager.getFieldWithFocus();
     	if(fieldWithFocus instanceof MailboxMessageField) {
     		return ((MailboxMessageField)fieldWithFocus).getMessageNode();
     	}
     	else {
     		return null;
     	}
     }
     
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#navigationClick(int, int)
      */
     public boolean navigationClick(int status, int time) {
     	MessageNode messageNode = getSelectedMessage();
     	if(messageNode != null) {
     		messageActions.openMessage(messageNode);
     		return true;
     	}
     	else {
     		return false;
     	}
     }
     
     /* (non-Javadoc)
      * @see net.rim.device.api.ui.Screen#keyChar(char, int, int)
      */
     public boolean keyChar(char key, int status, int time) {
         boolean retval = false;
         MessageNode messageNode;
         switch(key) {
             case Keypad.KEY_ENTER:
             	messageNode = getSelectedMessage();
             	if(messageNode != null) {
             		messageActions.openMessage(messageNode);
             		retval = true;
             	}
                 break;
             case Keypad.KEY_BACKSPACE:
             	messageNode = getSelectedMessage();
             	if(messageNode != null) {
             		messageActions.deleteMessage(messageNode);
             		retval = true;
             	}
             	break;
         }
         return retval;
     }
     
     /* (non-Javadoc)
      * @see org.logicprobe.LogicMail.ui.AbstractScreenProvider#shortcutAction(org.logicprobe.LogicMail.ui.ScreenProvider.ShortcutItem)
      */
     public void shortcutAction(ShortcutItem item) {
     	switch(item.getId()) {
     	case SHORTCUT_COMPOSE:
     		compositionItem.run();
     		break;
     	case SHORTCUT_UP:
     		screen.scroll(Manager.UPWARD);
     		break;
     	case SHORTCUT_DOWN:
     		screen.scroll(Manager.DOWNWARD);
     		break;
     	}
     }
     
     private static class MessageSeparatorField extends LabeledSeparatorField {
         private final Date date;
         
         public MessageSeparatorField(Date date) {
             super(Field.FOCUSABLE | LabeledSeparatorField.BOTTOM_BORDER);
    
             Calendar cal = Calendar.getInstance();
             cal.setTime(date);
             cal.set(Calendar.HOUR_OF_DAY, 0);
             cal.set(Calendar.MINUTE, 0);
             cal.set(Calendar.SECOND, 0);
             cal.set(Calendar.MILLISECOND, 0);
             
             StringBuffer buffer = new StringBuffer();
             DateFormat.getInstance(DateFormat.DATE_LONG).format(cal, buffer, null);
             setText(buffer.toString());
             
             this.date = cal.getTime();
         }
         
         public Date getDate() {
             return date;
         }
     }
 }
