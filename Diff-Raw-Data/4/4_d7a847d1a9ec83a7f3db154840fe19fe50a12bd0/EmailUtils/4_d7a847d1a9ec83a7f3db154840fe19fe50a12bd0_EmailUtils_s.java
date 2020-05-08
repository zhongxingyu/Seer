 package com.cffreedom.utils.net;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import javax.mail.Message;
 import javax.mail.Multipart;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.*;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.cffreedom.beans.EmailMessage;
 import com.cffreedom.utils.Convert;
 import com.cffreedom.utils.Utils;
 
 /**
  * @author markjacobsen.net (http://mjg2.net/code)
  * Copyright: Communication Freedom, LLC - http://www.communicationfreedom.com
  * 
  * Free to use, modify, redistribute.  Must keep full class header including 
  * copyright and note your modifications.
  * 
  * If this helped you out or saved you time, please consider...
  * 1) Donating: http://www.communicationfreedom.com/go/donate/
  * 2) Shoutout on twitter: @MarkJacobsen or @cffreedom
  * 3) Linking to: http://visit.markjacobsen.net
  * 
  * Changes:
  * 2013-05-15 	markjacobsen.net	Added unauthenticated sendEmail() option
  * 2013-05-17 	markjacobsen.net 	Added sendGmail() option and added protocol to sendEmail()
  * 2013-05-18 	markjacobsen.net 	Added htmlBody options
  * 2013-10-05 	markjacobsen.net 	Additional sendEmail()
  * 2013-11-05 	MarkJacobsen.net 	Fix in sendEmail() for CC and BCC
  * 2014-09-13 	MarkJacobsen.net 	Added support for attachments
  */
 public class EmailUtils
 {
 	private static final Logger logger = LoggerFactory.getLogger("com.cffreedom.utils.net.EmailUtils");
 	
 	public static final String SMTP_SERVER = "127.0.0.1";
 	public static final String SMTP_PORT = "25";
 	public static final String SMTP_SERVER_GMAIL = "smtp.gmail.com";
 	public static final String SMTP_PORT_GMAIL = "465";
 	public static final String PROTOCOL_SMTP = "smtp";
 	public static final String PROTOCOL_SMTPS = "smtps";
     
 	public static void sendGmail(String to, String from, String subject, String body, boolean htmlBody, String user, String pass) throws Exception
 	{
 		sendEmail(to, from, subject, body, htmlBody, user, pass, SMTP_SERVER_GMAIL, PROTOCOL_SMTPS, SMTP_PORT_GMAIL);
 	}
 	
 	public static void sendEmail(String to, String from, String subject, String body, String smtpServer, String port) throws Exception
 	{
 		sendEmail(to, from, subject, body, false, null, null, smtpServer, null, port);
 	}
 	
 	public static void sendHtmlEmail(String to, String from, String subject, String body, String smtpServer, String port) throws Exception
 	{
 		sendEmail(to, from, subject, body, true, null, null, smtpServer, null, port);
 	}
 	
     public static void sendEmail(String to, String from, String subject, String body, boolean htmlBody, String user, String pass, String smtpServer, String protocol, String port) throws Exception
 	{
     	EmailMessage msg = new EmailMessage(to, from, subject, body);
     	if (htmlBody == true) { msg.setBodyHtml(body); }
     	sendEmail(msg, user, pass, smtpServer, protocol, port);		
 	}
     
     /**
      * Send an email message
      * @param msg The EmailMessage object containing details about the message to send
      * @param user SMTP username
      * @param pass SMTP password
      * @param smtpServer SMTP server
      * @param protocol SMTP protocol
      * @param port SMTP port
      * @throws Exception
      */
     public static void sendEmail(EmailMessage msg, String user, String pass, String smtpServer, String protocol, String port) throws Exception
     {	
     	boolean authenticatedSession = true;
     	if ((user == null) || (user.length() == 0)) { authenticatedSession = false; }
 		Properties sysProps = System.getProperties();
 		sysProps.put("mail.smtp.host", smtpServer);
 		if (protocol != null)
 		{
 			sysProps.put("mail.transport.protocol", protocol);
 		}
 		if (authenticatedSession == true){
 			sysProps.put("mail.smtps.auth", "true");
 		}
 		
 		String[] toArray = getRecipientArray(msg.getTo());
 		String[] ccArray = getRecipientArray(msg.getCc());
 		String[] bccArray = getRecipientArray(msg.getBcc());
 		
 		Session session = Session.getDefaultInstance(sysProps, null);
         
 		MimeMessage message = new MimeMessage(session);
 		message.setFrom(new InternetAddress(msg.getFrom()));
 		
 		for (int y = 0; y < toArray.length; y++) {
 			message.addRecipient(Message.RecipientType.TO, new InternetAddress(toArray[y]));
 		}
 		
 		if (ccArray != null) {
 			for (int y = 0; y < ccArray.length; y++) {
 				message.addRecipient(Message.RecipientType.CC, new InternetAddress(ccArray[y]));
 			}
 		}
 		
 		if (bccArray != null) {
 			for (int y = 0; y < bccArray.length; y++) {
 				message.addRecipient(Message.RecipientType.BCC, new InternetAddress(bccArray[y]));
 			}
 		}
 		
 		message.setSubject(msg.getSubject());
 		
 		try
 		{
 			if ((msg.getAttachments() != null) && (msg.getAttachments().length > 0))
 			{
 				// Adapeted From: http://www.codejava.net/java-ee/javamail/send-e-mail-with-attachment-in-java
 				
 				// creates message part
 		        MimeBodyPart messageBodyPart = new MimeBodyPart();
 		        messageBodyPart.setContent(message, "text/html");
 		 
 		        // creates multi-part
 		        Multipart multipart = new MimeMultipart();
 		        multipart.addBodyPart(messageBodyPart);
 		 
 		        // adds attachments
 	            for (String[] attachment : msg.getAttachments()) 
 	            {
 	            	MimeBodyPart attachPart = new MimeBodyPart();
 	 
 	                try {
 	                	String file = attachment[0];
 	                    attachPart.attachFile(file);
 	                    
 	                    if (attachment.length > 1)
 	                    {
 		                    String name = attachment[1];
		                    attachPart.setFileName(name);
 	                    }
 	                } catch (IOException ex) {
 	                    ex.printStackTrace();
 	                }
 	 
 	                multipart.addBodyPart(attachPart);
 	            }
 		 
 		        // sets the multi-part as e-mail's content
 		        message.setContent(multipart);
 			}
 			else
 			{
 				if ((msg.getBodyHtml() != null) && (msg.getBodyHtml().trim().length() > 0)){
 					message.setText(msg.getBodyHtml(), "utf-8", "html");
 				}else{
 					message.setText(msg.getBody());
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			// this is what was working so use it as a fallback
 			if ((msg.getBodyHtml() != null) && (msg.getBodyHtml().trim().length() > 0)){
 				message.setText(msg.getBodyHtml(), "utf-8", "html");
 			}else{
 				message.setText(msg.getBody());
 			}
 		}
         
 		logger.trace("Sending message to {}, cc {}, bcc {}, from {} w/ subject: {}", msg.getTo(), msg.getCc(), msg.getBcc(), msg.getFrom(), msg.getSubject());
 		
 		if (authenticatedSession == true){
 			Transport transport = session.getTransport();
 			transport.connect(smtpServer, Convert.toInt(port), user, pass);
 			transport.sendMessage(message, message.getAllRecipients());
 			transport.close();
 		}else{
 			Transport.send(message);
 		}
     }
     
     private static String[] getRecipientArray(String recipients)
     {
     	String[] returnArray = null;
 		if (Utils.hasLength(recipients) == true) {
 			recipients = recipients.replace(',', ';');
 			recipients = recipients.replace(' ', ';');
 			returnArray = recipients.split(";");
 		}
 		return returnArray;
     }
 }
 
