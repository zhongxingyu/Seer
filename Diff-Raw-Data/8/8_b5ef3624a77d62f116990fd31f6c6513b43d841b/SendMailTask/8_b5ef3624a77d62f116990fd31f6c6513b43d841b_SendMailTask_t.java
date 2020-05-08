 package net.codjo.test.release.task.mail;
 import com.sun.mail.smtp.SMTPTransport;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import javax.activation.DataHandler;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import javax.mail.util.ByteArrayDataSource;
 import net.codjo.test.release.task.AgfTask;
 import org.apache.tools.ant.BuildException;

import static net.codjo.util.file.PathUtil.normalize;
 
 /**
  *
  */
 public class SendMailTask extends AgfTask {
 
     private static final String SMTP_HOST = "localhost";
 
     private int port = -1;
     private String subject = "";
     private String from;
     private String to;
     private String body = "";
     private List<Attachment> attachments = new ArrayList<Attachment>();
 
 
     @Override
     public void execute() {
         if (to == null) {
             throw new BuildException("Champ 'to' obligatoire");
         }
         if (from == null) {
             throw new BuildException("Champ 'from' obligatoire");
         }
         if (port < 0) {
             throw new BuildException("Champ 'port' obligatoire");
         }
         try {
             sendMail();
         }
         catch (Exception e) {
             throw new BuildException("Impossible d'envoyer le mail", e);
         }
     }
 
 
     private void sendMail() throws MessagingException, IOException {
         Properties props = System.getProperties();
         props.put("mail.smtp.host", SMTP_HOST);
         props.put("mail.smtp.port", Integer.toString(port));
 
         Session session = Session.getInstance(props);
         session.setDebug(true);
 
         Message msg = new MimeMessage(session);
         msg.setFrom(new InternetAddress(from));
         msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to, false));
 
         msg.setSubject(subject);
 
 //        msg.setDataHandler(new DataHandler(new ByteArrayDataSource(body, "text/html")));
         MimeMultipart mimeMultipart = new MimeMultipart();
         
         MimeBodyPart contentsPart = new MimeBodyPart();        
         contentsPart.setContent(body, "text/html; charset=UTF-8");
         mimeMultipart.addBodyPart(contentsPart);
         
         msg.setContent(mimeMultipart);
 
         addAttachmentsToMultipart(mimeMultipart);
 
         msg.setHeader("X-Mailer", "test");
         msg.setSentDate(new Date());
 
         Transport transport = new SMTPTransport(session, null);
         transport.connect(SMTP_HOST, port, "test", "test");
         transport.sendMessage(msg, msg.getAllRecipients());
         transport.close();
     }
 
 
     private void addAttachmentsToMultipart(MimeMultipart mimeMultipart) throws IOException, MessagingException {
         for (Attachment attachment : attachments) {
             MimeBodyPart attachmentPart = new MimeBodyPart();
            
            File attachedFile = new File(normalize(attachment.getFile()));
             attachmentPart.setDataHandler(new DataHandler(new ByteArrayDataSource(new FileInputStream(attachedFile),
                                                                                   attachment.getType())));
             attachmentPart.setFileName(attachedFile.getName());
             mimeMultipart.addBodyPart(attachmentPart);
         }
     }
 
     public void setSubject(String subject) {
         this.subject = subject;
     }
 
 
     public void setFrom(String from) {
         this.from = from;
     }
 
 
     public void setTo(String to) {
         this.to = to;
     }
 
 
     public void setPort(int port) {
         this.port = port;
     }
 
 
     public void addText(String msg) {
         this.body += msg;
     }
 
 
     public void addAttachment(Attachment attachment) {
         this.attachments.add(attachment);
     }
 }
