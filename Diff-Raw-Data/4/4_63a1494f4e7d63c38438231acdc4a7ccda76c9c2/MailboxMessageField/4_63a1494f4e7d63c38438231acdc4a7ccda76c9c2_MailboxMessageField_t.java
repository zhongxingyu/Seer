 /*-
  * Copyright (c) 2009, Derek Konigsberg
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
 
 import org.logicprobe.LogicMail.conf.MailSettings;
 import org.logicprobe.LogicMail.message.MimeMessagePart;
 import org.logicprobe.LogicMail.model.Address;
 import org.logicprobe.LogicMail.model.MailboxNode;
 import org.logicprobe.LogicMail.model.MessageNode;
 import org.logicprobe.LogicMail.util.UnicodeNormalizer;
 
 import net.rim.device.api.i18n.DateFormat;
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Color;
 import net.rim.device.api.ui.DrawStyle;
 import net.rim.device.api.ui.Field;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 
 /**
  * Field to represent message items on the mailbox screen.
  */
 public class MailboxMessageField extends Field {
 	private MailboxNode mailboxNode;
 	private MessageNode messageNode;
 	private int lineHeight;
     private int maxWidth;
     private UnicodeNormalizer unicodeNormalizer;
 	
 	/**
 	 * Instantiates a new mailbox message field.
 	 * 
 	 * @param mailboxNode The mailbox node containing the message
 	 * @param messageNode The message node representing the message to display
 	 */
 	public MailboxMessageField(MailboxNode mailboxNode, MessageNode messageNode) {
 		this.mailboxNode = mailboxNode;
 		this.messageNode = messageNode;
         if(MailSettings.getInstance().getGlobalConfig().getUnicodeNormalization()) {
             unicodeNormalizer = UnicodeNormalizer.getInstance();
         }
 	}
 
 	/**
 	 * Instantiates a new mailbox message field.
 	 * 
 	 * @param mailboxNode The mailbox node containing the message
 	 * @param messageNode The message node representing the message to display
 	 * @param style Combination of field style bits to specify display attributes
 	 */
 	public MailboxMessageField(MailboxNode mailboxNode, MessageNode messageNode, long style) {
 		super(style);
 		this.mailboxNode = mailboxNode;
 		this.messageNode = messageNode;
         if(MailSettings.getInstance().getGlobalConfig().getUnicodeNormalization()) {
             unicodeNormalizer = UnicodeNormalizer.getInstance();
         }
 	}
 	
 	/**
 	 * Gets the displayed message node.
 	 * 
 	 * @return The message node
 	 */
 	public MessageNode getMessageNode() {
 		return this.messageNode;
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.ui.Field#layout(int, int)
 	 */
 	protected void layout(int width, int height) {
         maxWidth = width;
         lineHeight = getPreferredHeight() / 2;
 		setExtent(width, getPreferredHeight());
 	}
 
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.ui.Field#getPreferredHeight()
 	 */
 	public int getPreferredHeight() {
 		return (Font.getDefault().getHeight() * 2);
 	};
 	
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.ui.Field#invalidate()
 	 */
 	public void invalidate() {
 		super.invalidate();
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.ui.Field#onUnfocus()
 	 */
 	protected void onUnfocus() {
 		super.invalidate();
 		super.onUnfocus();
 	}
 	
 	/* (non-Javadoc)
 	 * @see net.rim.device.api.ui.Field#paint(net.rim.device.api.ui.Graphics)
 	 */
 	protected void paint(Graphics graphics) {
         String senderText = createSenderText();
         String dateString = createDisplayDate();
         Bitmap attachmentIcon = null;
         MimeMessagePart[] attachments = messageNode.getAttachmentParts();
         if(attachments != null && attachments.length > 0) {
         	attachmentIcon = Bitmap.getBitmapResource("mail_attachment.png");
         }
 
     	boolean isFocus = this.isFocus();
         int width = this.getWidth();
         int originalColor = graphics.getColor();
 
         int dateWidth = Font.getDefault().getAdvance(dateString);
         int senderWidth = maxWidth - dateWidth - 20;
         
         // Draw the separator line
         graphics.setColor(Color.DARKGRAY);
         graphics.drawLine(0, lineHeight * 2 - 1, width, lineHeight * 2 - 1);
         graphics.setColor(originalColor);
         
         // Draw the message icon
         Bitmap messageIcon = NodeIcons.getIcon(messageNode);
         int messageIconY = (lineHeight / 2) - (messageIcon.getHeight() / 2);
         int messageIconW = messageIcon.getWidth();
         int messageIconH = messageIcon.getHeight();
         graphics.drawBitmap(1, messageIconY, messageIconW, messageIconH, messageIcon, 0, 0);
         
         if(!messageNode.hasCachedContent() && !messageNode.hasMessageContent()) {
             graphics.setColor(graphics.getBackgroundColor());
             graphics.drawFilledPath(
                    new int[] { 1, 11, 1 },
                    new int[] { 1, 1, messageIconY + messageIconH },
                     null,
                     null);
             graphics.setColor(originalColor);
         }
         
         if(attachmentIcon != null) {
             graphics.drawBitmap(1, lineHeight, 20, lineHeight*2, attachmentIcon, 0, 0);
         }
 
         // Draw the sender text
         if(senderText != null) {
             graphics.drawText(
 	    		normalize(senderText), 20, 0,
 	    		DrawStyle.ELLIPSIS,
 	    		senderWidth);
         }
         
         // Draw the subject text
         String subject = messageNode.getSubject();
         if(subject != null) {
             if(!isFocus) { graphics.setColor(0x7B7B7B); }
             graphics.drawText(normalize(subject), 20, lineHeight,
                               DrawStyle.ELLIPSIS,
                               maxWidth-20);
             if(!isFocus) { graphics.setColor(originalColor); }
         }
         
         // Draw the message date text
         if(dateString != null) {
             graphics.drawText(
 	    		dateString, senderWidth+20, 0,
 	    		DrawStyle.ELLIPSIS,
 	    		dateWidth);
         }
 	}
 	
 	/**
 	 * Creates the sender text to display.
 	 * This is normally the sender of the message.  However, if this
 	 * message is contained within a Sent folder, it is the first
 	 * recipient of the message.
 	 * 
 	 * @return Sender display text
 	 */
 	private String createSenderText() {
         Address sender = null;
         if(mailboxNode.getType() == MailboxNode.TYPE_SENT) {
         	Address[] to = messageNode.getTo();
         	if(to != null && to.length > 0) {
             	sender = to[0];
             }
         }
         else {
         	Address[] from = messageNode.getFrom();
             if(from != null && from.length > 0) {
             	sender = from[0];
             }
         }
         
         if(sender != null) {
         	String senderText = sender.getName();
         	if(senderText == null || senderText.length() == 0) {
         		senderText = sender.getAddress();
         	}
         	return senderText;
         }
         else {
         	return null;
         }
 	}
 	
 	/**
 	 * Creates the date string to display.
 	 * Determines which version of the date to display based on the relationship
 	 * between the current date and the message date.
 	 * 
 	 * @return The date string to display
 	 */
 	private String createDisplayDate() {
         Date date = messageNode.getDate();
         if(date == null) {
         	return null;
         }
         
         Calendar nowCal = Calendar.getInstance();
         Calendar dispCal = Calendar.getInstance();
         DateFormat dateFormat;
 
         dispCal.setTime(date);
 
         // Determine the date format to display,
         // based on the distance from the current time
         if(nowCal.get(Calendar.YEAR) == dispCal.get(Calendar.YEAR))
             if((nowCal.get(Calendar.MONTH) == dispCal.get(Calendar.MONTH)) &&
             (nowCal.get(Calendar.DAY_OF_MONTH) == dispCal.get(Calendar.DAY_OF_MONTH))) {
             	// Show just the time
                 dateFormat = DateFormat.getInstance(DateFormat.TIME_MEDIUM);
             }
             else {
                 dateFormat = DateFormat.getInstance(DateFormat.DATE_SHORT);
             }
         else {
             dateFormat = DateFormat.getInstance(DateFormat.DATE_SHORT);
         }
     
         StringBuffer buffer = new StringBuffer();
         dateFormat.format(dispCal, buffer, null);
         return buffer.toString();
 	}
 
 	/**
      * Run the Unicode normalizer on the provide string,
      * only if normalization is enabled in the configuration.
      * If normalization is disabled, this method returns
      * the input unmodified.
      * 
      * @param input Input string
      * @return Normalized string
      */
     private String normalize(String input) {
         if(unicodeNormalizer == null) {
             return input;
         }
         else {
             return unicodeNormalizer.normalize(input);
         }
     }
 }
