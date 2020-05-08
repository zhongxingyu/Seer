 package org.codehaus.mojo.mail;
 
 /* 
  * Copyright 2011 Markus W Mahlberg 
  * 
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
 
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 
 import javax.mail.Authenticator;
 import javax.mail.BodyPart;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Part;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException; 
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 /**
  * Goal which sends a mail to recipients 
  *
  * @goal mail 
  * @phase deploy 
  */
 public class MailMojo 
 extends AbstractMojo
 {
 
     // Sadly, one can't extend enums, so we have to wrap it ourselves
     
     private enum RecipientType {
       TO("To",Message.RecipientType.TO),
       CC("CC",Message.RecipientType.CC),
       BCC("BCC",Message.RecipientType.BCC);
 
       private final String description_;
       private final Message.RecipientType rectype_;
 
       private RecipientType(String desc, Message.RecipientType rt) {
         this.description_ = desc;
         this.rectype_ = rt;
       }
 
       String getDescription() {
         return this.description_;
       }
       
       Message.RecipientType getType() {
         return this.rectype_;
       }
   }
 
   /**
    * Sender 
    * @parameter default-value="maven2@localhost"
    * @required
    */
   private String from;
 
   /**
    * To-Adresses
    * @parameter
    * @required
    */
   private List<String> recipients;
 
   /**
    * CC-Adresses
    * @parameter
    */
   private List<String> ccRecipients;
 
   /**
    * BCC-Adresses
    * @parameter
    */
   private List<String> bccRecipients;
 
   /**
    * Subject
    * @parameter expression="${mail.subject}" default-value="${project.name} deployed"
    */
   private String subject;
 
   /**
    * Body
    * @parameter expression="${mail.body}" default-value="${project.name} was successfully deployed"
    * @required
    */
   private String body;
 
   /**
   * Attachements 
   * @parameter 
   */
   private List<String> attachments;
 
   /**
    * SMTP Host
    * @parameter expression="${mail.smtp.host}" default-value="localhost"
    * @required
    */
   private String smtphost;
 
   /**
    * SMTP Port
    * The port which will be used to connect to the SMTP server
    * @parameter expression="${mail.smtp.port}" default-value="25"
    */
   private Integer smtpport;
 
   /**
    * SMTP User
    * @parameter expression="${mail.smtp.user}"
    */
   private String smtpuser; 
 
   /**
    * SMTP Password 
    * @parameter expression="${mail.smtp.user}"
    */
   private String smtppassword;
 
   private MimeMessage message;
 
   private static Map<String, String> propsToMap(Properties props) {
           HashMap<String, String> hmap = new HashMap<String,String>();
           Enumeration<Object> e = props.keys();
           while (e.hasMoreElements()) {
                   String s = (String)e.nextElement();
                   hmap.put(s, props.getProperty(s));
           }
           return hmap;
   }
 
     private void addRecipents(List<String> recipients, RecipientType rt)
             throws MojoExecutionException {
 
         if (recipients == null) {
             return;
         }
 
         getLog().info(rt.getDescription() + ": " + recipients);
         Iterator<String> iterator = recipients.iterator();
 
         while (iterator.hasNext()) {
             try {
                 message.addRecipient(rt.getType(),
                         new InternetAddress(iterator.next(), true));
                 getLog().debug("Added " + rt.getType());
             } catch (AddressException e) {
                 throw new MojoExecutionException(
                         "Reason was an AddressException", e);
             } catch (MessagingException e) {
                 throw new MojoExecutionException(
                         "Reason was a MessagingException", e);
             } catch (Exception e) {
                 throw new MojoExecutionException("Unknown Reason", e);
             }
         }
     }
 
     private void addAttachements(MimeMultipart parent)
             throws MojoExecutionException {
         Iterator<String> it = attachments.iterator();
 
         while (it.hasNext()) {
             File attachment = new File(it.next());
 
             getLog().info("\t...\"" + attachment.getPath() + "\"");
 
             /*
              * Traversing directories is not yet implemented I strongly doubt
              * that it should be, since this is most likely going to produce
              * quite some mailtraffic
              */
 
             if (attachment.isDirectory()) {
                 throw new MojoExecutionException(
                         "Attachement can not be a directory (yet)");
             }
 
             DataSource src = new FileDataSource(attachment);
             BodyPart msgPart = new MimeBodyPart();
             try {
                 msgPart.setDataHandler(new DataHandler(src));
                 msgPart.setFileName(attachment.getName());
 
                 // Don't know whether using FileDataSource.getContentType() is
                 // really a good idea,
                 // but so far it works.
                 msgPart.setHeader("Content-Type", src.getContentType());
                 msgPart.setHeader("Content-ID", attachment.getName());
                 msgPart.setDisposition(Part.ATTACHMENT);
             } catch (MessagingException e) {
                 getLog().error(
                         "Could not create attachment from file \""
                                 + attachment.getName() + "\"");
                 throw new MojoExecutionException("Cought MessagingException", e);
             }
 
             try {
                 parent.addBodyPart(msgPart);
             } catch (MessagingException e) {
                 getLog().error(
                         "Could not attach \"" + attachment.getName()
                                 + "\" to message");
                 throw new MojoExecutionException("Cought MessagingException", e);
             }
         }
     }
 
     public void execute() throws MojoExecutionException {
 
         Properties props = new Properties();
         getLog().debug("Sending Mail via: " + smtphost);
         props.put("mail.smtp.host", smtphost);
         props.put("mail.smtp.port", smtpport);
         props.put("mail.smtp.starttls.enable", "true");
 
         Session s = Session.getInstance(props, null);
         message = new MimeMessage(s);
 
         try {
             message.setFrom(new InternetAddress(from));
         } catch (AddressException e) {
             throw new MojoExecutionException("Cought AddressException!", e);
         } catch (MessagingException e) {
             throw new MojoExecutionException("Cought MessagingException!", e);
         } catch (Exception e) {
             getLog().error("Could not set " + from + " as FromAddress");
             throw new MojoExecutionException(
                     "Something is SERIOUSLY going wrong: ", e);
         }
 
         if (recipients == null) {
             throw new MojoExecutionException(
                     "There must be at last one recipient");
         }
 
         getLog().info("Preparing mail from " + from + " via " + smtphost);
 
        addRecipients(recipients, RecipientType.TO);
 
         if (ccRecipients != null) {
            this.addRecipients(ccRecipients, RecipientType.CC);
         }
 
         if (bccRecipients != null) {
            this.addRecipients(bccRecipients, RecipientType.BCC);
         }
 
         BodyPart msgBodyPart = new MimeBodyPart();
         try {
             msgBodyPart.setContent(body, "text/plain; charset=utf-8");
             msgBodyPart.setDisposition(Part.INLINE);
         } catch (MessagingException e) {
             getLog().error("Could not set body to \"" + body + "\"");
             throw new MojoExecutionException("Cought MessagingException: ", e);
         }
 
         MimeMultipart multipart = new MimeMultipart();
 
         try {
             multipart.addBodyPart(msgBodyPart);
         } catch (MessagingException e) {
             getLog().error("Could not attach body to MultipartMessage");
             throw new MojoExecutionException("Cought MessagingException: ", e);
         }
         if (attachments != null) {
             getLog().info("Attaching...");
             this.addAttachements(multipart);
         } else {
             getLog().info("No attachments");
         }
 
         try {
             message.setSubject(subject, "UTF-8");
             message.setHeader("Content-Transfer-Encoding", "8bit");
             message.setContent(multipart);
             getLog().info("Mail sucessfully sent");
             if (smtpuser != null && smtppassword != null) {
                 Transport transport = s.getTransport("smtp");
                 transport.connect(smtpuser, smtppassword);
             }
             Transport.send(message);
         } catch (MessagingException e) {
             getLog().info("Cought MessagingException: " + e.toString());
             throw new MojoExecutionException(e.toString());
         }
     }
 }
