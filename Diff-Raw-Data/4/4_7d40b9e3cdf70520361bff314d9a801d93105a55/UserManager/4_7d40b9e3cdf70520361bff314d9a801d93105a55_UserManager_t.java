 package com.wit.base;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Random;
 
 import javax.mail.Address;
 import javax.mail.BodyPart;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 import com.google.appengine.api.datastore.Query.CompositeFilter;
 import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
 import com.google.appengine.api.datastore.Query.FilterOperator;
 import com.google.appengine.api.datastore.Query.FilterPredicate;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.appengine.api.datastore.TransactionOptions;
 import com.google.appengine.api.memcache.MemcacheService;
 import com.google.appengine.api.memcache.MemcacheServiceFactory;
 import com.wit.base.BaseConstants;
 import com.wit.base.Log;
 import com.wit.base.model.MailMessage;
 import com.wit.base.model.Session;
 import com.wit.base.model.User;
 import com.wit.base.model.UserEmail;
 import com.wit.base.model.UserEmailGrp;
 import com.wit.base.model.UserUname;
 import com.wit.base.model.UserUnameGrp;
 
 public class UserManager {
 
     public static User signUp(String username, String email,
             String password, String repeatPassword, Log log)
             throws UnsupportedEncodingException, MessagingException {
 
         username = username.trim();
         email = email.trim();
         password = password.trim();
         repeatPassword = repeatPassword.trim();
 
         // Email always be lowercase.
         email = email.toLowerCase();
 
         // Checking for duplicate will be done in the transaction
         // in signUpAttempt.
         UserVerifier.isUsernameValid(username, false, log);
         UserVerifier.isEmailValid(email, false, log);
         UserVerifier.isPasswordValid(password, log, BaseConstants.PASSWORD);
         UserVerifier.isRepeatPasswordValid(password, repeatPassword, log);
         if (!log.isValid()) {
             return null;
         }
 
         // In order to have unique username and email,
         // 1. All users need to be in the same entity group.
         // 2. Use transaction to check if exists and if not, insert.
         // 3. Check existing with get() or ancestor query to make sure of
         //    getting the most update.
         //
         // With entity group and transaction, only one transaction can touch
         // this entity group at a time from beginning to commiting and will
         // throw java.util.ConcurrentModificationException if entity group was
         // modified by other thread (that why need to have for loop to try several time).
         //
         // Because of ConcurrentModificationException, this table is really critical.
         //
         // http://blog.broc.seib.net/2011/06/unique-constraint-in-appengine.html
         //
         // Keep the transaction time minimum as possible.
         // Prepare everything before hand.
 
         String lowerCaseUsername = username.toLowerCase();
         String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
 
         // Create keys.
         Key userUnameGrpKey = getUserUnameGrpKey(lowerCaseUsername);
         Key userEmailGrpKey = getUserEmailGrpKey(email);
 
         Key userUnameKey = UserUname.createKey(userUnameGrpKey,
                 lowerCaseUsername);
         Key userEmailKey = UserEmail.createKey(userEmailGrpKey, email);
 
         // Create a new user object.
         User user = new User(userUnameGrpKey, userEmailGrpKey, hashPassword);
         UserUname userUname = new UserUname(userUnameKey, username);
         UserEmail userEmail = new UserEmail(userEmailKey);
 
         ArrayList<Entity> entityList = new ArrayList<Entity>();
         entityList.add(userUname.getEntity());
         entityList.add(userEmail.getEntity());
 
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         TransactionOptions options = TransactionOptions.Builder.withXG(true);
         for (int i = 0; i < 10; i++) {
             try {
                 Transaction txn = ds.beginTransaction(options);
                 try {
                     try {
                         // Check if username is duplicate. Compare it with lowercase.
                         ds.get(txn, userUnameKey);
                         txn.commit();
                         log.addLogInfo(BaseConstants.USERNAME, false, username,
                                 BaseConstants.ERR_NAME_TAKEN);
                         return null;
                     } catch (EntityNotFoundException e1) {
                         try {
                             // Check if email is duplicate. Always be lowercase.
                             ds.get(txn, userEmailKey);
                             txn.commit();
                             log.addLogInfo(BaseConstants.EMAIL, false, email,
                                     BaseConstants.ERR_EMAIL_TAKEN);
                             return null;
                         } catch (EntityNotFoundException e2) {
 
                             Key userKey = ds.put(txn, user.getEntity());
 
                             userUname.setUserKey(userKey);
                             userEmail.setUserKey(userKey);
                             ds.put(txn, entityList);
 
                             // Throws java.util.ConcurrentModificationException
                             // if entity group was modified by other thread
                             txn.commit();
 
                             // Put User to Memcacahe
                             MemcacheService syncCache = MemcacheServiceFactory
                                     .getMemcacheService();
                             syncCache.put(user.getKeyString(), user);
 
                             log.addLogInfo(BaseConstants.SIGN_UP, true, null, null);
                             return user;
                         }
                     }
                 } finally {
                     if (txn.isActive()) {
                         txn.rollback();
                     }
                 }
             } catch (ConcurrentModificationException e) {
                 // stay in the loop and try again.
             }
             // you could use another backoff algorithm here rather than 100ms each time.
             try { Thread.sleep(100); } catch (InterruptedException e) {}
         }
         log.addLogInfo(BaseConstants.SIGN_UP, false, null,
                 BaseConstants.CONCURRENT_MODIFICATION_EXCEPTION);
         return null;
     }
 
     public static void deleteAccount(User user, String password, Log log)
             throws EntityNotFoundException {
 
         password = password.trim();
 
         // validate password
         UserVerifier.isPasswordValid(password, log, BaseConstants.PASSWORD);
         if (!log.isValid()) {
             return ;
         }
 
         if (!BCrypt.checkpw(password, user.getHashPassword())) {
             log.addLogInfo(BaseConstants.PASSWORD, false, null,
                     BaseConstants.ERR_PASSWORD);
             return;
         }
 
         UserUname userUname = getUserUname(user.getUserUnameGrpKey(), user.getKey());
         UserEmail userEmail = getUserEmail(user.getUserEmailGrpKey(), user.getKey());
 
         // Delete User, UserUname and UserEmail from DB
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         ds.delete(user.getKey(), userUname.getKey(), userEmail.getKey());
 
         // Delete User from Memcache
         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
         syncCache.delete(user.getKeyString());
 
         // Delete all user's login sessions
         deleteLoginSessions(user.getKey(), null);
 
         log.addLogInfo(BaseConstants.DELETE_ACCOUNT, true, null, null);
     }
 
     public static User checkUsernameOrEmailAndPassword(String usernameOrEmail,
             String password, Log log) throws EntityNotFoundException {
 
         User user;
         usernameOrEmail = usernameOrEmail.trim();
 
         if (usernameOrEmail.contains("@")) {
 
             // Email always be lowercase.
             String email = usernameOrEmail.toLowerCase();
 
             UserVerifier.isEmailValid(email, false, log);
             UserVerifier.isPasswordValid(password, log, BaseConstants.PASSWORD);
             if (!log.isValid()) {
                 return null;
             }
             user = getUserFromEmail(email, log);
         } else {
 
             String username = usernameOrEmail;
 
             UserVerifier.isUsernameValid(username, false, log);
             UserVerifier.isPasswordValid(password, log, BaseConstants.PASSWORD);
             if (!log.isValid()) {
                 return null;
             }
             user = getUserFromUsername(username, log);
         }
 
         if (user != null && BCrypt.checkpw(password, user.getHashPassword())) {
             log.addLogInfo(BaseConstants.LOG_IN, true, null, null);
             return user;
         }
 
         log.addLogInfo(BaseConstants.LOG_IN, false, null, BaseConstants.ERR_LOG_IN);
         return null;
     }
 
     public static User checkLoggedInAndGetUser(String sessionKeyString,
             String sessionID) throws NotLoggedInException{
 
         Session session = getSession(sessionKeyString, Session.LOG_IN, sessionID);
         if (session == null) {
             throw new NotLoggedInException();
         }
 
         User user = getUserFromKeyString(session.getUserKeyString());
         if (user == null) {
             // 2 devices logged in, then delete account,
             // 1 device still have the session but the user is gone.
             throw new NotLoggedInException();
         }
 
         return user;
     }
 
     public static void changeUsername(User user, String newUsername,
             String password, Log log) throws EntityNotFoundException {
 
         //TODO: Warning - can't undo and the username might be taken by others.
         newUsername = newUsername.trim();
         password = password.trim();
 
         // Checking for duplicate will be done in the transaction.
         UserVerifier.isUsernameValid(newUsername, false, log);
         UserVerifier.isPasswordValid(password, log, BaseConstants.PASSWORD);
         if (!log.isValid()) {
             return;
         }
 
         if (!BCrypt.checkpw(password, user.getHashPassword())) {
             log.addLogInfo(BaseConstants.PASSWORD, false, null,
                     BaseConstants.ERR_PASSWORD);
             return;
         }
 
         // Check if username is duplicate. Compare it with lowercase.
         String lowerCaseNewUsername = newUsername.toLowerCase();
 
         Key userUnameKey = getUserUname(user.getUserUnameGrpKey(),
                 user.getKey()).getKey();
 
         Key newUserUnameGrpKey = getUserUnameGrpKey(lowerCaseNewUsername);
         Key newUserUnameKey = UserUname.createKey(newUserUnameGrpKey,
                 lowerCaseNewUsername);
 
         UserUname newUserUname = new UserUname(newUserUnameKey, newUsername);
         newUserUname.setUserKey(user.getKey());
 
         ArrayList<Entity> entityList = new ArrayList<Entity>();
         entityList.add(user.getEntity());
         entityList.add(newUserUname.getEntity());
 
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         TransactionOptions options = TransactionOptions.Builder.withXG(true);
         for (int i = 0; i < 10; i++) {
             try {
                 Transaction txn = ds.beginTransaction(options);
                 try {
                     try {
                         ds.get(txn, newUserUnameKey);
                         txn.commit();
                         log.addLogInfo(BaseConstants.USERNAME, false, newUsername,
                                 BaseConstants.ERR_NAME_TAKEN);
                         return;
                     } catch (EntityNotFoundException e) {
                         // Update to DB
                         user.setUserUnameGrpKey(newUserUnameGrpKey);
                         ds.put(txn, entityList);
                         ds.delete(txn, userUnameKey);
 
                         // Throws java.util.ConcurrentModificationException
                         // if entity group was modified by other thread
                         txn.commit();
 
                         // Update User to memcache
                         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
                         syncCache.put(user.getKeyString(), user);
 
                         log.addLogInfo(BaseConstants.CHANGE_USERNAME, true,
                                 newUsername, null);
                         return;
                     }
                 } finally {
                     if (txn.isActive()) {
                         txn.rollback();
                     }
                 }
             } catch (ConcurrentModificationException e) {
                 // stay in the loop and try again.
             }
             // you could use another backoff algorithm here rather than 100ms each time.
             try { Thread.sleep(100); } catch (InterruptedException e) {}
         }
         log.addLogInfo(BaseConstants.CHANGE_USERNAME, false, null,
                 BaseConstants.CONCURRENT_MODIFICATION_EXCEPTION);
     }
 
     public static void changeEmail(User user, String newEmail,
             String password, Log log) throws EntityNotFoundException,
             UnsupportedEncodingException, MessagingException {
 
         //TODO: Warning - can't undo.
         newEmail = newEmail.trim();
         password = password.trim();
 
         newEmail = newEmail.toLowerCase();
 
         // Checking for duplicate will be done in the transaction.
         UserVerifier.isEmailValid(newEmail, false, log);
         UserVerifier.isPasswordValid(password, log, BaseConstants.PASSWORD);
         if (!log.isValid()) {
             return;
         }
 
         if (!BCrypt.checkpw(password, user.getHashPassword())) {
             log.addLogInfo(BaseConstants.PASSWORD, false, null,
                     BaseConstants.ERR_PASSWORD);
             return;
         }
 
         Key userEmailKey = getUserEmail(user.getUserEmailGrpKey(),
                 user.getKey()).getKey();
 
         Key newUserEmailGrpKey = getUserEmailGrpKey(newEmail);
         Key newUserEmailKey = UserEmail.createKey(newUserEmailGrpKey, newEmail);
 
         UserEmail newUserEmail = new UserEmail(newUserEmailKey);
         newUserEmail.setUserKey(user.getKey());
 
         ArrayList<Entity> entityList = new ArrayList<Entity>();
         entityList.add(user.getEntity());
         entityList.add(newUserEmail.getEntity());
 
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         TransactionOptions options = TransactionOptions.Builder.withXG(true);
         for (int i = 0; i < 10; i++) {
             try {
                 Transaction txn = ds.beginTransaction(options);
                 try {
                     try {
                         ds.get(txn, newUserEmailKey);
                         txn.commit();
                         log.addLogInfo(BaseConstants.EMAIL, false, newEmail,
                                 BaseConstants.ERR_EMAIL_TAKEN);
                         return;
                     } catch (EntityNotFoundException e) {
                         // Update to DB.
                         user.setUserEmailGrpKey(newUserEmailGrpKey);
                         ds.put(txn, entityList);
                         ds.delete(txn, userEmailKey);
 
                         // Throws java.util.ConcurrentModificationException
                         // if entity group was modified by other thread
                         txn.commit();
 
                         log.addLogInfo(BaseConstants.CHANGE_EMAIL, true, newEmail, null);
                         return;
                     }
                 } finally {
                     if (txn.isActive()) {
                         txn.rollback();
                     }
                 }
             } catch (ConcurrentModificationException e) {
                 // stay in the loop and try again.
             }
             // you could use another backoff algorithm here rather than 100ms each time.
             try { Thread.sleep(100); } catch (InterruptedException e) {}
         }
         log.addLogInfo(BaseConstants.CHANGE_EMAIL, false, null,
                 BaseConstants.CONCURRENT_MODIFICATION_EXCEPTION);
     }
 
     public static void changePassword(User user, String currentSessionKeyString,
             String newPassword, String repeatPassword, String password, Log log) {
 
         newPassword = newPassword.trim();
         repeatPassword = repeatPassword.trim();
         password = password.trim();
 
         // validate password
         UserVerifier.isPasswordValid(newPassword, log, BaseConstants.NEW_PASSWORD);
         UserVerifier.isRepeatPasswordValid(newPassword, repeatPassword, log);
         UserVerifier.isPasswordValid(password, log, BaseConstants.PASSWORD);
         if (!log.isValid()) {
             return ;
         }
 
         if (!BCrypt.checkpw(password, user.getHashPassword())) {
             log.addLogInfo(BaseConstants.PASSWORD, false, null,
                     BaseConstants.ERR_PASSWORD);
             return;
         }
 
         setUserPassword(user, newPassword);
 
         //delete user's sessions except the current one!
         deleteLoginSessions(user.getKey(), currentSessionKeyString);
 
         log.addLogInfo(BaseConstants.CHANGE_PASSWORD, true, null, null);
     }
 
     public static void sendEmailResetPassword(String usernameOrEmail, String url, String fromEmail,
             String fromName, String subject, String msgBody, Log log)
             throws UnsupportedEncodingException, MessagingException, EntityNotFoundException {
 
         User user;
         usernameOrEmail = usernameOrEmail.trim();
 
         if (usernameOrEmail.contains("@")) {
 
             // Email always be lowercase.
             String email = usernameOrEmail.toLowerCase();
 
             UserVerifier.isEmailValid(email, false, log);
             if (!log.isValid()) {
                 return;
             }
             user = getUserFromEmail(email, log);
         } else {
 
             String username = usernameOrEmail;
 
             UserVerifier.isUsernameValid(username, false, log);
             if (!log.isValid()) {
                 return;
             }
             user = getUserFromUsername(username, log);
         }
 
         if (user == null) {
             log.addLogInfo(BaseConstants.SEND_EMAIL_RESET_PASSWORD, false,
                     usernameOrEmail, BaseConstants.ERR_USER_NOT_FOUND);
             return;
         }
 
         // Generate forgot password session
         Session session = addSession(Session.RESET_PASSWORD, user.getKey(),
                 genSessionID());
 
         //Send an email with a link to reset password
         String resetPasswordUrl = genResetPasswordUrl(url, session.getKeyString(),
                 session.getSessionID());
         msgBody = String.format(msgBody, resetPasswordUrl);
 
         UserEmail userEmail = getUserEmail(user.getUserEmailGrpKey(),
                 user.getKey());
         UserUname userUname = getUserUname(user.getUserUnameGrpKey(),
                 user.getKey());
 
         sendEmail(fromEmail, fromName, userEmail.getKeyName(), userUname.getUsername(),
                 subject, msgBody);
 
         log.addLogInfo(BaseConstants.SEND_EMAIL_RESET_PASSWORD, true,
                 usernameOrEmail, null);
     }
 
     private static String genResetPasswordUrl(String url, String sessionKeyString,
             String sessionID) {
         return url + "/resetpassword?" + BaseConstants.SSID + "=" + sessionKeyString
                 + "&" + BaseConstants.FID + "=" + sessionID;
     }
 
     public static void resetPassword(String sessionKeyString, String sessionID,
             String newPassword, String repeatPassword, Log log) {
 
         newPassword = newPassword.trim();
         repeatPassword = repeatPassword.trim();
 
         UserVerifier.isPasswordValid(newPassword, log, BaseConstants.NEW_PASSWORD);
         UserVerifier.isRepeatPasswordValid(newPassword, repeatPassword, log);
         if (!log.isValid()) {
             return;
         }
 
         Session session = getSession(sessionKeyString, Session.RESET_PASSWORD,
                 sessionID);
         if (session == null) {
             // Session not found!
             log.addLogInfo(BaseConstants.RESET_PASSWORD, false, null,
                     BaseConstants.RESET_PASSWORD_FAILURE);
             return;
         }
 
         // The session should not be too old - 24 hrs
         Date now = new Date();
         if (now.getTime() - session.getCreateDate().getTime() > 86400000l) {
             log.addLogInfo(BaseConstants.RESET_PASSWORD, false, null,
                     BaseConstants.RESET_PASSWORD_SESSION_TOO_OLD);
             return;
         }
 
         // Set user a new password
         User user = getUserFromKeyString(session.getUserKeyString());
         setUserPassword(user, newPassword);
 
         // Delete the session
         deleteSession(session.getKey());
 
         // Delete all user's login sessions
         deleteLoginSessions(session.getUserKey(), null);
 
         log.addLogInfo(BaseConstants.RESET_PASSWORD, true, null,
                 BaseConstants.RESET_PASSWORD_SUCCESS);
         return;
     }
 
     public static void sendEmailConfirmEmail(Key userKey, String url, String fromEmail,
             String fromName, String toEmail, String toName, String subject, String msgBody, Log log)
             throws UnsupportedEncodingException, MessagingException {
 
         // Generate forgot password session
         Session session = addSession(Session.EMAIL_CONFIRM, userKey, genSessionID());
 
         //Send an email with a link to confirm password
         String confirmEmailUrl = genConfirmEmailUrl(url, session.getKeyString(),
                 session.getSessionID());
         msgBody = String.format(msgBody, confirmEmailUrl);
 
         sendEmail(fromEmail, fromName, toEmail, toName, subject, msgBody);
 
         log.addLogInfo(BaseConstants.RESEND_EMAIL_CONFIRM, true, null, null);
     }
 
     private static String genConfirmEmailUrl(String url, String sessionKeyString,
             String sessionID) {
         return url + "/emailverification?" + BaseConstants.SSID + "="
                 + sessionKeyString + "&" + BaseConstants.VID + "=" + sessionID;
     }
 
     public static boolean confirmEmail(String sessionKeyString,
             String sessionID) throws EntityNotFoundException {
 
         Session session = getSession(sessionKeyString, Session.EMAIL_CONFIRM,
                 sessionID);
         if (session == null) {
             return false;
         }
 
         // Set didConfirmEmail to true
         User user = getUserFromKeyString(session.getUserKeyString());
         setUserDidConfirmEmail(user, true);
 
         // Delete the verified email session
         deleteSession(session.getKey());
 
         return true;
     }
 
     private static void sendEmail(String fromEmail, String fromName,
             String toEmail, String toName, String subject, String msgBody)
                     throws UnsupportedEncodingException, MessagingException {
 
         Properties props = new Properties();
         javax.mail.Session mailSession = javax.mail.Session.getDefaultInstance(
                 props, null);
 
         MimeMessage msg = new MimeMessage(mailSession);
         msg.setFrom(new InternetAddress(fromEmail, fromName));
         msg.addRecipient(Message.RecipientType.TO,
                 new InternetAddress(toEmail, toName));
         msg.setSubject(subject);
 
         Multipart mp = new MimeMultipart();
         MimeBodyPart htmlPart = new MimeBodyPart();
         htmlPart.setContent(msgBody, "text/html");
         mp.addBodyPart(htmlPart);
         msg.setContent(mp);
 
         Transport.send(msg);
     }
 
     public static Key receiveEmail(MimeMessage msg) throws MessagingException,
             IOException {
         Address[] a;
 
         // FROM -> Sender
         String sender = "";
         if ((a = msg.getFrom()) != null) {
             for (int i = 0; i < a.length; i++) {
                 sender += (i == 0 ? "" : ", ") + a[i].toString();
             }
         }
 
         // TO -> Recipients
         String recipients = "";
         if ((a = msg.getRecipients(Message.RecipientType.TO)) != null) {
             for (int i = 0; i < a.length; i++) {
                 recipients += (i == 0 ? "" : ", ") + a[i].toString();
             }
         }
 
         // SUBJECT
         String subject = msg.getSubject();
 
         // CONTENT
         ArrayList<String> contentList = new ArrayList<String>();
         Object o;
         o = msg.getContent();
         if (o instanceof String) {
             contentList.add((String)o);
         } else if (o instanceof Multipart) {
             Multipart mp = (Multipart)o;
             for (int i = 0; i < mp.getCount(); i++) {
                 BodyPart bp = mp.getBodyPart(i);
                 o = bp.getContent();
                 if (o instanceof String) {
                     contentList.add((String)o);
                 }
             }
         }
 
         // DATE
         Date sentDate = msg.getSentDate();
 
         MailMessage mm = new MailMessage(sender, recipients, subject,
                 contentList.toString(), sentDate);
 
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         return ds.put(mm.getEntity());
     }
 
     public static Session addLoginSession(Key userKey) {
         return addSession(Session.LOG_IN, userKey, genSessionID());
     }
 
     public static boolean deleteLoginSession(String sessionKeyString, String sessionID) {
         Session session = getSession(sessionKeyString, Session.LOG_IN, sessionID);
        if (session != null) {
            deleteSession(session.getKey());
        }
         return true;
     }
 
     private static void deleteLoginSessions(Key userKey,
             String currentSessionKeyString) {
 
         // Keep the current session in case of changing password.
         // Delete all sessions if deleting account - give current session null.
 
         List<Key> sessionKeyList = new ArrayList<Key>();
         List<String> sessionKeyStringList = new ArrayList<String>();
 
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         Query query = new Query(Session.class.getSimpleName());
         CompositeFilter compositeFilter =  CompositeFilterOperator.and(
                 FilterOperator.EQUAL.of(BaseConstants.TYPE, Session.LOG_IN),
                 FilterOperator.EQUAL.of(BaseConstants.USER_KEY, userKey));
         query.setFilter(compositeFilter);
         Iterator<Entity> iterator = ds.prepare(query).asIterator();
         while(iterator.hasNext()){
             Entity entity = iterator.next();
             Session session = new Session(entity);
             if (!session.getKeyString().equals(currentSessionKeyString)) {
                 sessionKeyList.add(session.getKey());
                 sessionKeyStringList.add(session.getKeyString());
             }
         }
 
         // 1. Delete from DB.
         if (!sessionKeyList.isEmpty()) {
             ds.delete(sessionKeyList);
         }
 
         // 2. Delete from memcache
         if (!sessionKeyStringList.isEmpty()) {
             MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
             syncCache.deleteAll(sessionKeyStringList);
         }
     }
 
     public static Session getSession(String sessionKeyString, int type,
             String sessionID) {
         Session session = getSession(sessionKeyString);
         if (session != null) {
             if (session.getType() == type
                     && session.getSessionID().equals(sessionID)) {
                 return session;
             }
         }
         return null;
     }
 
     private static Session getSession(String sessionKeyString){
 
         Session session = null;
 
         // Try to get from memcache first.
         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
         session = (Session)syncCache.get(sessionKeyString);
         if (session != null) {
             return session;
         }
 
         // Not found in memcache, try to get from DB.
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         try {
             Key sessionKey = KeyFactory.stringToKey(sessionKeyString);
             Entity entity = ds.get(sessionKey);
             session = new Session(entity);
 
             // Save the session in memcache.
             syncCache.put(sessionKeyString, session);
 
             return session;
         } catch (IllegalArgumentException e) {
             return null;
         } catch (EntityNotFoundException e) {
             return null;
         }
     }
 
     private static Session addSession(int type, Key userKey, String sessionID) {
         assert(userKey != null && sessionID != null);
 
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         Session session = new Session(type, userKey, sessionID);
         ds.put(session.getEntity());
 
         // Save the session in memcache.
         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
         syncCache.put(session.getKeyString(), session);
 
         return session;
     }
 
     private static void deleteSession(Key key){
         // Delete from DB
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         ds.delete(key);
 
         // Delete from memcache
         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
         syncCache.delete(KeyFactory.keyToString(key));
     }
 
     private static User getUserFromUsername(String username, Log log)
             throws EntityNotFoundException {
 
         assert(UserVerifier.isUsernameValid(username, false, log));
 
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         Query query = new Query(UserUname.class.getSimpleName());
         query.setAncestor(getUserUnameGrpKey(username.toLowerCase()));
         query.setFilter(new FilterPredicate(BaseConstants.USERNAME,
                 Query.FilterOperator.EQUAL, username));
         Entity entity = ds.prepare(query).asSingleEntity();
         if (entity != null) {
             UserUname userUname = new UserUname(entity);
 
             // If found userUname, gotta found user,
             // Otherwise, EntityNotFoundException will be thrown
             User user = getUserFromKeyString(userUname.getUserKeyString());
             if (user == null) {
                 throw new EntityNotFoundException(userUname.getKey());
             }
             return user;
         }
         return null;
     }
 
     private static User getUserFromEmail(String email, Log log)
             throws EntityNotFoundException {
 
         assert(UserVerifier.isEmailValid(email, false, log));
 
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         Key userEmailKey = UserEmail.createKey(getUserEmailGrpKey(email), email);
         UserEmail userEmail = null;
         try {
             Entity userEmailEntity = ds.get(userEmailKey);
             userEmail = new UserEmail(userEmailEntity);
         } catch (EntityNotFoundException e) {
             return null;
         }
 
         // If found userEmail, gotta found user,
         // Otherwise, EntityNotFoundException will be thrown
         User user = getUserFromKeyString(userEmail.getUserKeyString());
         if (user == null) {
             throw new EntityNotFoundException(userEmail.getUserKey());
         }
 
         return user;
     }
 
     private static User getUserFromKeyString(String userKeyString){
 
         User user = null;
 
         // Try to get from memcache first.
         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
         user = (User)syncCache.get(userKeyString);
         if (user != null) {
             return user;
         }
 
         // Not found in memcache, try to get from DB.
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         try {
             Key userKey = KeyFactory.stringToKey(userKeyString);
             Entity entity = ds.get(userKey);
             user = new User(entity);
 
             // Save the user in memcache.
             syncCache.put(userKeyString, user);
 
             return user;
         } catch (IllegalArgumentException e) {
             return null;
         } catch (EntityNotFoundException e) {
             return null;
         }
     }
 
     public static UserUname getUserUname(Key userUnameGrpKey, Key userKey)
             throws EntityNotFoundException {
 
         // No save to Memcache as rarely used by deleteAccount(),
         // changeUsername(), changeEmail(), and sendEmailResetPassword.
 
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         Query query = new Query(UserUname.class.getSimpleName());
         query.setAncestor(userUnameGrpKey);
         query.setFilter(new FilterPredicate(BaseConstants.USER_KEY,
                 Query.FilterOperator.EQUAL, userKey));
         Entity entity = ds.prepare(query).asSingleEntity();
         if (entity != null) {
             return new UserUname(entity);
         }
         throw new EntityNotFoundException(userKey);
     }
 
     public static UserEmail getUserEmail(Key userEmailGrpKey, Key userKey)
             throws EntityNotFoundException {
 
         // No save to Memcache as rarely used by deleteAccount(), changeEmail(),
         // and sendEmailResetPassword.
 
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         Query query = new Query(UserEmail.class.getSimpleName());
         query.setAncestor(userEmailGrpKey);
         query.setFilter(new FilterPredicate(BaseConstants.USER_KEY,
                 Query.FilterOperator.EQUAL, userKey));
         Entity entity = ds.prepare(query).asSingleEntity();
         if (entity != null) {
             return new UserEmail(entity);
         }
         throw new EntityNotFoundException(userKey);
     }
 
     private static void setUserPassword(User user, String password) {
 
         String hashPassword = BCrypt.hashpw(password, BCrypt.gensalt());
         user.setPassword(hashPassword);
 
         // Update to DB
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         ds.put(user.getEntity());
 
         // Update to memcache
         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
         syncCache.put(user.getKeyString(), user);
     }
 
     public static void setUserDidConfirmEmail(User user,
             boolean didEmailConfirm) {
 
         user.setConfirmEmail(didEmailConfirm);
 
         // Update to DB
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         ds.put(user.getEntity());
 
         // Update to memcache
         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
         syncCache.put(user.getKeyString(), user);
     }
 
     public static void setUserLang(User user, String lang, Log log) {
 
         user.setLang(lang);
 
         // Update to DB
         DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
         ds.put(user.getEntity());
 
         // Update to memcache
         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
         syncCache.put(user.getKeyString(), user);
 
         log.addLogInfo(BaseConstants.CHANGE_LANG, true, null, null);
     }
 
     private static String genSessionID() {
         // a-z, A-Z, 0-9, -, _
         String s = "";
         Random random = new Random();
         for (int i = 0; i < 32; i++) {
             s += BaseConstants.WEB_SAFE_CHARACTERS.charAt(random.nextInt(
                     BaseConstants.WEB_SAFE_CHARACTERS.length()));
         }
         return s;
     }
 
     protected static Key getUserUnameGrpKey(String username) {
         assert(UserVerifier.isUsernameValid(username, false, new Log()));
         assert(username.equals(username.toLowerCase()));
 
         String keyName = username.substring(0, 2);
         Key userUnameGrpKey = UserUnameGrp.createKey(keyName);
         String userUnameGrpKeyString = KeyFactory.keyToString(userUnameGrpKey);
 
         // Try to get from memcache first.
         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
         if (!syncCache.contains(userUnameGrpKeyString)) {
             // Not found in memcache, try to get from DB.
             DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
             try {
                 ds.get(userUnameGrpKey);
             } catch (EntityNotFoundException e) {
                 // Not found in DB, create a new one.
                 UserUnameGrp usernameGrp = new UserUnameGrp(userUnameGrpKey);
                 ds.put(usernameGrp.getEntity());
             }
             syncCache.put(userUnameGrpKeyString, null);
         }
         return userUnameGrpKey;
     }
 
     protected static Key getUserEmailGrpKey(String email) {
         assert(UserVerifier.isEmailValid(email, false, new Log()));
         assert(email.equals(email.toLowerCase()));
 
         String keyName = email.substring(0, 2);
         Key userEmailGrpKey = UserEmailGrp.createKey(keyName);
         String userEmailGrpKeyString = KeyFactory.keyToString(userEmailGrpKey);
 
         // Try to get from memcache first.
         MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
         if (!syncCache.contains(userEmailGrpKeyString)) {
             // Not found in memcache, try to get from DB.
             DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
             try {
                 ds.get(userEmailGrpKey);
             } catch (EntityNotFoundException e) {
                 // Not found in DB, create a new one.
                 UserEmailGrp userEmailGrp = new UserEmailGrp(userEmailGrpKey);
                 ds.put(userEmailGrp.getEntity());
             }
             syncCache.put(userEmailGrpKeyString, null);
         }
         return userEmailGrpKey;
     }
 }
