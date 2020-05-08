 /*
  * This file is part of anycook. The new internet cookbook
  * Copyright (C) 2014 Jan Graßegger
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see [http://www.gnu.org/licenses/].
  */
 
 package de.anycook.messages;
 
 import de.anycook.api.providers.MessageNumberProvider;
 import de.anycook.api.providers.MessageProvider;
 import de.anycook.api.providers.MessageSessionProvider;
 import de.anycook.db.mysql.DBMessage;
 import de.anycook.db.mysql.DBUser;
 import de.anycook.news.News;
 import de.anycook.notifications.Notification;
 import de.anycook.user.User;
 import de.anycook.utils.enumerations.NotificationType;
 import org.apache.log4j.Logger;
 
 import javax.xml.bind.annotation.XmlElement;
 import java.sql.SQLException;
 import java.util.*;
 
 
 public class MessageSession extends News {
 
     private final static Logger sLogger;
 
     static {
         sLogger = Logger.getLogger(MessageSession.class);
     }
 
     public static MessageSession getSession(int sessionId, int userId) throws SQLException, DBMessage.SessionNotFoundException {
         return getSession(sessionId, userId, -1);
     }
 
     public static MessageSession getSession(int sessionId, int userId, int lastId) throws SQLException,
             DBMessage.SessionNotFoundException {
         try (DBMessage db = new DBMessage()) {
             if (!db.checkSession(sessionId, userId))
                 throw new DBMessage.SessionNotFoundException(sessionId, userId);
 
             Set<User> recipients = db.getRecipients(sessionId);
             List<Message> messages = db.getMessages(sessionId, lastId, userId, 100);
 
             Date lastChange = null;
             if(messages.size() > 0)
                 lastChange = db.lastChange(sessionId);
 
             return new MessageSession(sessionId, recipients, messages, lastChange);
         }
     }
 
     public static int getNewMessageNum(int userId) throws SQLException {
         try(DBMessage dbmessage = new DBMessage()){
             return dbmessage.getNewMessageNum(userId);
         }
     }
 
     public static MessageSession getSession(List<Integer> userIds) throws SQLException, DBMessage.SessionNotFoundException {
         try (DBMessage db = new DBMessage()) {
             int sessionId;
             try {
                 sessionId = db.getSessionIDFromUsers(userIds);
             } catch (DBMessage.SessionNotFoundException e) {
                 sLogger.debug(e);
                 sessionId = db.newSession();
                 db.addRecipientsToSession(sessionId, userIds);
             }
             return getSession(sessionId, userIds.get(0));
         }
 
     }
 
     public static MessageSession getAnycookSession(int userId) throws SQLException, DBMessage.SessionNotFoundException {
         List<Integer> userIds = new LinkedList<>();
         userIds.add(1);
         userIds.add(userId);
         return getSession(userIds);
     }
 
     public static List<MessageSession> getSessionsFromUser(int userId, Date lastChange) throws SQLException, DBMessage.SessionNotFoundException {
         DBMessage db = new DBMessage();
         Set<Integer> sessionIds = db.getSessionIDsFromUser(userId, lastChange);
         db.close();
         List<MessageSession> sessions = new LinkedList<>();
         for (Integer sessionId : sessionIds) {
             sessions.add(getSession(sessionId, userId));
         }
         return sessions;
     }
 
     public static List<MessageSession> getSessionsFromUser(int userId) throws SQLException {
         DBMessage db = new DBMessage();
         Set<Integer> sessionIds = db.getSessionIDsFromUser(userId);
         db.close();
         List<MessageSession> sessions = new LinkedList<>();
         for (Integer sessionId : sessionIds) {
             try {
                 sessions.add(getSession(sessionId, userId));
             } catch (DBMessage.SessionNotFoundException e) {
                 //nope
             }
         }
         return sessions;
     }
 
     private final Logger logger;
     private List<Message> messages;
     private Set<User> recipients;
 
 
     public MessageSession() {
         logger = Logger.getLogger(getClass());
     }
 
     public MessageSession(int id, Set<User> recipients, List<Message> messages, Date lastChange) {
         super(id, lastChange);
         this.messages = messages;
         this.recipients = recipients;
         logger = Logger.getLogger(getClass());
 
     }
 
     public static Logger getsLogger() {
         return sLogger;
     }
 
     public Logger getLogger() {
         return logger;
     }
 
     @Override
     @XmlElement
     public int getId() {
         return super.getId();
     }
 
     @Override
     @XmlElement
     public long getDatetime(){
         return super.getDatetime();
     }
 
     public List<Message> getMessages() {
         return messages;
     }
 
     public void setMessages(List<Message> messages) {
         this.messages = messages;
     }
 
     public Set<User> getRecipients() {
         return recipients;
     }
 
     public void setRecipients(Set<User> recipients) {
         this.recipients = recipients;
     }
 
     public void newMessage(int sender, String text) throws SQLException {
         newMessage(sender, text, true);
     }
 
     public void newMessage(int sender, String text, boolean sendNotification) throws SQLException {
         if (text == null) return;
 
         try(DBMessage db = new DBMessage()){
             int messageId = db.newMessage(sender, id, text);
 
             for (User recipient : recipients) {
                 if (recipient.getId() != sender) {
                     sendNotification = sendNotification && !db.hasNewMessages(recipient.getId());
 
                     db.unreadMessage(recipient.getId(), id, messageId);
 
 
                     if(sendNotification){
                         try {
                             Map<String, String> data = new HashMap<>();
                             data.put("sender", User.getUsername(sender));
                             data.put("content", text);
                             Notification.sendNotification(recipient.getId(), NotificationType.NEW_MESSAGE, data);
                         } catch (DBUser.UserNotFoundException e) {
                             logger.error(e, e);
                         }
                     }
 
                    MessageNumberProvider.INSTANCE.wakeUpSuspended(recipient.getId());
                 }
 
                 MessageSessionProvider.INSTANCE.wakeUpSuspended(recipient.getId());
                 MessageProvider.INSTANCE.wakeUpSuspended(id);
             }
             logger.info(sender + " sent a new message");
         }
     }
 
     public boolean isEmpty() {
         return messages.isEmpty();
     }
 }
