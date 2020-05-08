 package com.github.mailerific.server;
 
 import java.util.List;
 import java.util.Properties;
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.Query;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import com.github.mailerific.client.Incoming;
 import com.github.mailerific.client.Mail;
 import com.github.mailerific.client.MailList;
 import com.github.mailerific.client.Outgoing;
 
 public class Mails {
 
     private static final Mails SINGLETON = new Mails();
     private static final int FETCH_MAX = MailList.REC_PER_PAGE + 1;
 
     private static final String ADMIN = "mailerific.appspot@gmail.com";
 
     private Mails() {
     }
 
     private Incoming saveIncoming(final Incoming incoming) {
         PersistenceManager pm = Persistence.manager();
         return pm.makePersistent(incoming);
     }
 
     private Outgoing saveOutgoing(final Outgoing outgoing) {
         PersistenceManager pm = Persistence.manager();
         return pm.makePersistent(outgoing);
     }
 
     @SuppressWarnings("unchecked")
     private List<Mail> findByOwner(final String email, final Long upperBound) {
         PersistenceManager pm = Persistence.manager();
         Query query = pm.newQuery(Incoming.class);
         query.setFilter("owner == email && id <  upperBound");
         query.setOrdering("id desc");
         query.declareParameters("String email, Long upperBound");
         query.setRange(0, FETCH_MAX);
         return (List<Mail>) pm.detachCopyAll((List<Mail>) query.execute(email,
                 upperBound));
     }
 
     @SuppressWarnings("unchecked")
     private List<Mail> findByOwner(final String email) {
         PersistenceManager pm = Persistence.manager();
         Query query = pm.newQuery(Incoming.class);
         query.setFilter("owner == email");
         query.setOrdering("id desc");
         query.declareParameters("String email");
         query.setRange(0, FETCH_MAX);
         return (List<Mail>) query.execute(email);
     }
 
     private List<Mail> findByOwnerDetached(final String email) {
         PersistenceManager pm = Persistence.manager();
         return (List<Mail>) pm.detachCopyAll(findByOwner(email));
     }
 
     @SuppressWarnings("unchecked")
     private List<Mail> findBySender(final String email) {
         PersistenceManager pm = Persistence.manager();
         Query query = pm.newQuery(Outgoing.class);
         query.setFilter("sender == email");
         query.setOrdering("id desc");
         query.declareParameters("String email");
         query.setRange(0, FETCH_MAX);
         return (List<Mail>) query.execute(email);
     }
 
     private List<Mail> findBySenderDetached(final String email) {
         PersistenceManager pm = Persistence.manager();
         return (List<Mail>) pm.detachCopyAll(findBySender(email));
     }
 
     @SuppressWarnings("unchecked")
     private List<Mail> findBySender(final String email, final Long upperBound) {
         PersistenceManager pm = Persistence.manager();
         Query query = pm.newQuery(Outgoing.class);
         query.setFilter("sender == email && id <  upperBound");
         query.setOrdering("id desc");
         query.declareParameters("String email, Long upperBound");
         query.setRange(0, FETCH_MAX);
         return (List<Mail>) pm.detachCopyAll((List<Mail>) query.execute(email,
                 upperBound));
     }
 
     public static Incoming save(final Incoming incoming) {
         return SINGLETON.saveIncoming(incoming);
     }
 
     public static Outgoing save(final Outgoing outgoing) {
         return SINGLETON.saveOutgoing(outgoing);
     }
 
     public static List<Mail> getByOwner(final String email,
             final Long upperBound) {
         return SINGLETON.findByOwner(email, upperBound);
     }
 
     public static List<Mail> getByOwner(final String email) {
         return SINGLETON.findByOwnerDetached(email);
     }
 
     public static List<Mail> getAttachedByOwner(final String email) {
         return SINGLETON.findByOwner(email);
     }
 
     public static List<Mail> getBySender(final String email) {
         return SINGLETON.findBySenderDetached(email);
     }
 
     public static List<Mail> getAttachedBySender(final String email) {
         return SINGLETON.findBySender(email);
     }
 
     public static List<Mail> getBySender(final String email,
             final Long upperBound) {
         return SINGLETON.findBySender(email, upperBound);
     }
 
     public static void send(final Incoming mail) throws AddressException,
             MessagingException {
         send(ADMIN, mail.getOwner(), mail.getSubject(), mail.getMessage());
     }
 
     public static void send(final Outgoing mail) throws AddressException,
             MessagingException {
        String message = mail.getMessage();
        String signature = mail.getSignature();
        if (signature != null && !signature.trim().isEmpty()) {
            message += ("\n\n\n" + signature);
        }
        send(mail.getSender(), mail.getRecipient(), mail.getSubject(), message);
     }
 
     private static void send(final String from, final String to,
             final String subject, final String message)
             throws AddressException, MessagingException {
         Properties props = new Properties();
         Session session = Session.getDefaultInstance(props, null);
         Message msg = new MimeMessage(session);
         msg.setFrom(new InternetAddress(from));
         msg.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
         msg.setSubject(subject);
         msg.setText(message);
         Transport.send(msg);
     }
 
 }
