 package edu.mit.cci.amtprojects.util;
 
 import org.apache.log4j.Logger;
 
 import javax.activation.DataHandler;
 import javax.activation.DataSource;
 import javax.activation.FileDataSource;
 import javax.mail.BodyPart;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.PasswordAuthentication;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class Mailer {
 
 
     public static Mailer instance;
 
     private final Properties props;
 
     private String username;
     private String password;
 
     private Session session;
 
     private static Logger logger = Logger.getLogger(Mailer.class);
 
     private Mailer(String username, String password) {
         props = new Properties();
         props.put("mail.debug", "true");
         props.put("mail.debug.auth", "true");
         props.put("mail.smtp.auth", "true");
         props.put("mail.smtp.starttls.enable", "true");
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.smtp.port", "587");
         this.username = username;
         this.password = password;
     }
 
     public static Mailer get() {
         if (instance == null) {
             String f = System.getProperty("turkmdr.mailer.config","mailer.properties");
 
             InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + f);
             Properties p = new Properties();
             try {
                 p.load(stream);
 
             } catch (IOException e) {
                 logger.warn("Error reading mailer configuration file");
                 return null;
             }
             instance = new Mailer(p.getProperty("username"), p.getProperty("password"));
 
         }
         return instance;
     }
 
 
     public void sendMail(String from, String to,
                          String subject, String messageBody) throws MessagingException {
         sendMail(from, to, subject, messageBody, null);
 
     }
 
     public void sendMail(String from, String to,
                          String subject, String messageBody,
                          String[] attachments) throws
             MessagingException, AddressException {
         // Setup mail server
 
 
         // Get a mail session
         if (to == null) {
             logger.warn("No email address specified; not sending mail");
             return;
         }
 
         if (session == null) {
             session = Session.getInstance(props, new javax.mail.Authenticator() {
                 protected PasswordAuthentication getPasswordAuthentication() {
                     return new PasswordAuthentication(username, password);
                 }
             });
 
            // session.getProperties().putAll(props);
 
         }
 
         // Define a new mail message
         Message message = new MimeMessage(session);
         message.setFrom(new InternetAddress(from));
         message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
         message.setSubject(subject);
 
         // Create a message part to represent the body text
         BodyPart messageBodyPart = new MimeBodyPart();
         messageBodyPart.setText(messageBody);
 
         //use a MimeMultipart as we need to handle the file attachments
         Multipart multipart = new MimeMultipart();
 
         //add the message body to the mime message
         multipart.addBodyPart(messageBodyPart);
 
         // add any file attachments to the message
         if (attachments != null) addAtachments(attachments, multipart);
 
         // Put all message parts in the message
         message.setContent(multipart);
 
         // Send the message
         session.getTransport("smtp").send(message);
 
        // Transport.send(message);
 
 
     }
 
     protected void addAtachments(String[] attachments, Multipart multipart)
             throws MessagingException, AddressException {
         for (int i = 0; i <= attachments.length - 1; i++) {
             String filename = attachments[i];
             MimeBodyPart attachmentBodyPart = new MimeBodyPart();
 
             //use a JAF FileDataSource as it does MIME type detection
             DataSource source = new FileDataSource(filename);
             attachmentBodyPart.setDataHandler(new DataHandler(source));
 
             //assume that the filename you want to send is the same as the
             //actual file name - could alter this to remove the file path
             attachmentBodyPart.setFileName(filename);
 
             //add the attachment
             multipart.addBodyPart(attachmentBodyPart);
         }
     }
 
     public static void main(String[] args) {
 
         //add your own parameters
         try {
             Mailer client = new Mailer("jintrone@gmail.com", "FcJi7701");
 
             String from = "jintrone@gmail.com";
             String to = "jintrone@mit.edu";
             String subject = "[TURKMDR NOTIFICATION]";
             String message = "Testing this thing";
 
 
             client.sendMail(from, to, subject, message, null);
         } catch (Exception e) {
             e.printStackTrace(System.out);
         }
 
     }
 
 }
