 package com.mymed.utils.mail;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A mail message object with all its necessary information.
  * <p>
  * The values stored in the message are:
  * <ul>
  * <li><tt>recipients</tt></li>
  * <li><tt>subject</tt></li>
  * <li><tt>text</tt></li>
  * <li><tt>attachments</tt></li>
  * <li><tt>mime-type</tt></li>
  * </ul>
 * The <tt>mime-type</tt> field by default is set to "text/html". The <tt>attachments</tt> have to be file names that
 * are accessible via the Java classloader, it is not necessary to specify a path.
  * 
  * @author Milo Casagrande
  */
 public final class MailMessage {
     /**
      * Who receives the email.
      */
     private final List<String> recipients = new ArrayList<String>();
 
     /**
      * The subject of the email.
      */
     private String subject;
 
     /**
      * The text of email body.
      */
     private String text;
 
     /**
      * The attachments associated with this email.
      * <p>
      * Attachments here should be files names accessible via the default Java classloader.
      */
     private final List<String> attachments = new ArrayList<String>();
 
     /**
      * The mime type of the email body.
      * <p>
      * By default it is "text/html".
      */
     private String mimeType = "text/html";
 
     /**
      * Creates a new empty mail message.
      */
     public MailMessage() {
         // Empty constructor, just create the instance.
     }
 
     /**
      * Creates a new mail message.
      * 
      * @param recipient
      *            Who receives the email
      * @param subject
      *            The subject of the email
      * @param text
      *            The body of the email
      */
     public MailMessage(final String recipient, final String subject, final String text) {
         recipients.add(recipient);
         this.subject = subject;
         this.text = text;
     }
 
     /**
      * Creates a new mail message.
      * 
      * @param recipients
      *            Who receives the email
      * @param subject
      *            The subject of the email
      * @param text
      *            The body of the email
      */
     public MailMessage(final List<String> recipients, final String subject, final String text) {
         this.subject = subject;
         this.text = text;
         this.recipients.addAll(recipients);
     }
 
     /**
      * @return Who receives the email
      */
     public List<String> getRecipients() {
         return recipients;
     }
 
     /**
      * Adds a list of recipients.
      * 
      * @param recipients
      *            Who receives the email
      */
     public void setRecipients(final List<String> recipients) {
         this.recipients.addAll(recipients);
     }
 
     /**
      * Adds a single email address to the recipients list.
      * 
      * @param recipient
      *            The recipient to add
      */
     public void setRecipient(final String recipient) {
         recipients.add(recipient);
     }
 
     /**
      * @return The subject of the email
      */
     public String getSubject() {
         return subject;
     }
 
     /**
      * @param subject
      *            The subject of the email
      */
     public void setSubject(final String subject) {
         this.subject = subject;
     }
 
     /**
      * @return The body of the email
      */
     public String getText() {
         return text;
     }
 
     /**
      * Sets the body of the email.
      * 
      * @param text
      *            The body of the email
      */
     public void setText(final String text) {
         this.text = text;
     }
 
     /**
      * Adds one attachment to this email message.
      * 
      * @param attachment
      *            The attachment to add
      */
     public void setAttachment(final String attachment) {
         attachments.add(attachment);
     }
 
     /**
      * Adds the attachments to this email message.
      * 
      * @param attachments
      *            The attachments to add
      */
     public void setAttachments(final List<String> attachments) {
         this.attachments.addAll(attachments);
     }
 
     /**
      * Gets the attachments of this email message.
      * 
      * @return The list of attachments
      */
     public List<String> getAttachments() {
         return attachments;
     }
 
     /**
      * The mime-type of this email message.
      * 
      * @param mimeType
      *            the myme-type to set
      */
     public void setMymeType(final String mimeType) {
         this.mimeType = mimeType;
     }
 
     /**
      * Retrieves the mime-type of this email message.
      * 
      * @return the mime-type
      */
     public String getMymeType() {
         return mimeType;
     }
 }
