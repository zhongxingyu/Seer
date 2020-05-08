 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package EmailReader;
 
import com.sun.mail.imap.IMAPMessage;
 import java.util.Date;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.Authenticator;
 
 /**
  *
  * @author Roman Nort
  */
 public class MessagesProvider {
 
     private AccountData account;
 
     private Folder currentFolder;
     
     private Store currentStore;
     
     /**
      *
      */
     public MessagesProvider()
     {
         account = AccountData.ActiveAccount;
     }
     
     
     // protocol://username:password@host/foldername
     public Message[] GetMessages() {
 
         Authenticator auth = new Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication(account.Login, account.Password);
             }
         };
 
         Properties props = new Properties();
         String protocol = account.HostIn.Protocl;
         props.put("mail." + protocol + ".host", account.HostIn.Host);
         props.put("mail." + protocol + ".user", account.Login);
         props.put("mail.store.protocol", protocol);
         props.put("mail." + protocol + ".auth", "true");
         props.put("mail." + protocol + ".socketFactory", account.HostIn.Port);
         props.put("mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
         props.put("mail." + protocol + ".port", account.HostIn.Port);
         //props.put("mail.pop3s.connectiontimeout", "5000"); // ten seconds
 
         try {
 
             // Connect to the server
             Session session = Session.getDefaultInstance(props, auth);
             Store store = session.getStore(protocol);
             store.connect();
 
             // Open the folder
             Folder inbox = store.getFolder("INBOX");
             Folder[] namespaces = store.getPersonalNamespaces();
 
             if (inbox == null) {
                 //System.out.println("No INBOX");
                 System.exit(1);
             }
             inbox.open(Folder.READ_WRITE);
 
             // Get the messages from the server
             Message[] messages = inbox.getMessages();
             // Close the connection 
             // but don't remove the messages from the server
             
             //inbox.close(false);            
             //store.close();
             
             FetchProfile fp = new FetchProfile();
             fp.add(FetchProfile.Item.ENVELOPE);
             fp.add("X-mailer");
             inbox.fetch(messages, fp);
             
             currentFolder = inbox;
             currentStore = store;
             
             return messages;
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
         return null;
     }
 
     public void CloseConnection()
     {
         try {
             currentFolder.close(true);
             currentStore.close();
         } catch (MessagingException ex) {
             Logger.getLogger(MessagesProvider.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
     
     private static void WorkingWithImap() {
 
         final String username = "giantrog@gmail.com";
         final String password = "bear_103_19";
         String port = "993";
         String provider = "imaps";
 
         Properties props = new Properties();
         props.put("mail.transport.protocol", "smtp");
         props.put("mail.smtp.starttls.enable", "true");
         props.put("mail.smtp.host", "smtp.gmail.com");
         props.put("mail.store.protocol", provider);
         props.put("mail.smtp.auth", "true");
         props.put("mail.imaps.host", "imap.gmail.com");
         props.put("mail.imaps.port", port);
         props.put("mail.imaps.ssl.socketFactory", port);
         props.put("mail.imaps.ssl.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
 
         Authenticator auth = new Authenticator() {
             @Override
             protected PasswordAuthentication getPasswordAuthentication() {
                 return new PasswordAuthentication(username, password);
             }
         };
 
         Session session = Session.getDefaultInstance(props, auth);
         Store store;
         try {
             store = session.getStore(provider);
             store.connect();
 
             Folder[] folders = store.getPersonalNamespaces();
             Folder inbox = store.getFolder("INBOX");
             inbox.open(Folder.READ_WRITE);
             
             Message[] messages = inbox.getMessages();
             for (int i = 0; i < messages.length; i++) {
                 messages[i].setFlag(Flags.Flag.SEEN, true);
                 
                 System.out.println("------------ Message " + (i + 1)
                         + " ------------");
                 // Here's the big change...
                 String from = InternetAddress.toString(messages[i].getFrom());
                 if (from != null) {
                     System.out.println("From: " + from);
                 }
                 String replyTo = InternetAddress.toString(
                         messages[i].getReplyTo());
                 if (replyTo != null) {
                     System.out.println("Reply-to: "
                             + replyTo);
                 }
                 String to = InternetAddress.toString(
                         messages[i].getRecipients(Message.RecipientType.TO));
                 if (to != null) {
                     System.out.println("To: " + to);
                 }
                 String cc = InternetAddress.toString(
                         messages[i].getRecipients(Message.RecipientType.CC));
                 if (cc != null) {
                     System.out.println("Cc: " + cc);
                 }
                 String bcc = InternetAddress.toString(
                         messages[i].getRecipients(Message.RecipientType.BCC));
                 if (bcc != null) {
                     System.out.println("Bcc: " + to);
                 }
                 String subject = messages[i].getSubject();
                 if (subject != null) {
                     System.out.println("Subject: " + subject);
                 }
                 Date sent = messages[i].getSentDate();
                 if (sent != null) {
                     System.out.println("Sent: " + sent);
                 }
                 Date received = messages[i].getReceivedDate();
                 if (received != null) {
                     System.out.println("Received: " + received);
                 }
 
                 System.out.println();
             }
 
             // Close the connection 
             // but don't remove the messages from the server
             inbox.close(false);
 
 
         } catch (Exception ex) {
             ex.printStackTrace();
         }
 
     }
     
     
 }
