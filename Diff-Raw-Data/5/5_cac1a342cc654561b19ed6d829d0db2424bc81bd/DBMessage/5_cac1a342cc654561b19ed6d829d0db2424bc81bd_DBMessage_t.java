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
 
 package de.anycook.db.mysql;
 
 import de.anycook.messages.Message;
 import de.anycook.user.User;
 import de.anycook.utils.DateParser;
 import org.apache.commons.lang3.StringUtils;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.*;
 
 
 public class DBMessage extends DBHandler {
 
     public DBMessage() throws SQLException {
         super();
     }
 
     public boolean  checkSession(int sessionId, int userId) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT id from message_sessions " +
                         "INNER JOIN message_sessions_has_users ON message_sessions_id = id " +
                         "WHERE id = ? AND users_id = ?");
         pStatement.setInt(1, sessionId);
         pStatement.setInt(2, userId);
 
         ResultSet data = pStatement.executeQuery();
         return data.next();
     }
 
     public Set<User> getRecipients(int sessionId) throws SQLException {
         Set<User> recipients = new HashSet<>();
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT users_id, nickname, users.image from message_sessions_has_users " +
                         "INNER JOIN users ON users_id = id " +
                         "WHERE message_sessions_id = ?");
         pStatement.setInt(1, sessionId);
 
         ResultSet data = pStatement.executeQuery();
         while (data.next()) {
 
             int id = data.getInt("users_id");
             String name = data.getString("nickname");
             String image = data.getString("users.image");
             recipients.add(new User(id, name, image));
         }
         return recipients;
     }
 
     public List<Message> getMessages(int sessionId, int userId) throws SQLException {
         List<Message> messages = new LinkedList<>();
 
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT messages.id, sender, messages.text, datetime, nickname, users.image from messages " +
                         "LEFT JOIN users ON sender = users.id " +
                         "WHERE message_sessions_id = ? ORDER BY id");
         pStatement.setInt(1, sessionId);
 
         ResultSet data = pStatement.executeQuery();
         while (data.next()) {
             int id = data.getInt("id");
 
             String text = data.getString("messages.text");
            Date datetime = DateParser.parseDateTime(data.getString("datetime"));
             boolean unread = isUnread(userId, sessionId, id);
 
             int senderId = data.getInt("sender");
             String senderName = data.getString("nickname");
             String senderImage = data.getString("users.image");
             User sender = new User(senderId, senderName, senderImage);
 
             Message message = new Message(id, sender, text, datetime, unread);
             messages.add(message);
         }
         return messages;
     }
 
     public List<Message> getMessages(int sessionId, int lastId, int userId, int limit) throws SQLException {
         LinkedList<Message> messages = new LinkedList<>();
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT messages.id, sender, messages.text, datetime, nickname, users.image from messages " +
                         "LEFT JOIN users ON sender = users.id " +
                         "WHERE message_sessions_id = ? " +
                         "AND messages.id > ? ORDER BY id DESC LIMIT ?");
         pStatement.setInt(1, sessionId);
         pStatement.setInt(2, lastId);
         pStatement.setInt(3, limit);
 
         ResultSet data = pStatement.executeQuery();
         while (data.next()) {
             int id = data.getInt("messages.id");
             String text = data.getString("messages.text");
            Date datetime = DateParser.parseDateTime(data.getString("datetime"));
             boolean unread = isUnread(userId, sessionId, id);
 
             int senderId = data.getInt("sender");
             String senderName = data.getString("nickname");
             String senderImage = data.getString("users.image");
             User sender = new User(senderId, senderName, senderImage);
 
             Message message = new Message(id, sender, text, datetime, unread);
             messages.addFirst(message);
         }
         return messages;
     }
 
     public Set<Integer> getSessionIDsFromUser(int userId) throws SQLException {
         Set<Integer> sessionIds = new HashSet<>();
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT message_sessions_id from message_sessions_has_users " +
                         "WHERE users_id = ?");
         pStatement.setInt(1, userId);
 
         ResultSet data = pStatement.executeQuery();
         while (data.next()) {
             sessionIds.add(data.getInt("message_sessions_id"));
         }
         return sessionIds;
     }
 
     public Set<Integer> getSessionIDsFromUser(int userId, Date lastChange) throws SQLException {
         Set<Integer> sessionIds = new HashSet<>();
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT message_sessions_has_users.message_sessions_id from message_sessions_has_users " +
                         "LEFT JOIN messages ON message_sessions_has_users.message_sessions_id = messages.message_sessions_id  " +
                         "WHERE users_id = ? AND datetime > ?");
         pStatement.setInt(1, userId);
         pStatement.setString(2, DateParser.datetimeToString(lastChange));
 
         ResultSet data = pStatement.executeQuery();
         while (data.next()) {
             sessionIds.add(data.getInt("message_sessions_id"));
         }
         return sessionIds;
     }
 
     public Integer getSessionIDFromUsers(Collection<Integer> userIds) throws SessionNotFoundException, SQLException {
 
         Set<Integer> merge = null;
         for (int userId : userIds) {
             Set<Integer> userSessions = getSessionIDsFromUser(userId);
             if (merge == null)
                 merge = userSessions;
             else
                 merge.retainAll(userSessions);
         }
 
         for (Integer sessionId : merge) {
             if (getNumSessionUsers(sessionId) == userIds.size())
                 return sessionId;
         }
 
         throw new SessionNotFoundException(userIds);
     }
 
     public int getNumSessionUsers(int sessionId) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT COUNT(users_id) FROM message_sessions_has_users " +
                         "WHERE message_sessions_id = ? " +
                         "GROUP BY message_sessions_id");
         pStatement.setInt(1, sessionId);
         ResultSet data = pStatement.executeQuery();
         if (data.next())
             return data.getInt(1);
         return 0;
     }
 
     public int newMessage(int sender, int sessionId, String message) throws SQLException {
         int newMessageId = getNewMessageId(sessionId);
         PreparedStatement pStatement = connection.prepareStatement(
                 "INSERT INTO messages (id, text, datetime, sender, message_sessions_id) VALUES (" +
                         "?, ?, NOW(), ?, ?)");
         pStatement.setInt(1, newMessageId);
         pStatement.setString(2, message);
         pStatement.setInt(3, sender);
         pStatement.setInt(4, sessionId);
 
 
         pStatement.executeUpdate();
         return newMessageId;
     }
 
     public int getNewMessageId(int sessionId) throws SQLException {
         int newId = -1;
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT id FROM messages WHERE message_sessions_id = ? ORDER BY id DESC LIMIT 1");
         pStatement.setInt(1, sessionId);
         ResultSet data = pStatement.executeQuery();
         if (data.next())
             newId = data.getInt(1);
         return newId + 1;
     }
 
     public int newSession() throws SQLException {
         int id = getNewSessionId();
 
         PreparedStatement pStatement = connection.prepareStatement(
                 "INSERT INTO message_sessions (id) VALUES (?)");
         pStatement.setInt(1, id);
         pStatement.executeUpdate();
 
         logger.debug("created new session with id "+id);
 
         return id;
     }
 
     public int getNewSessionId() throws SQLException {
         int id = 0;
         String sql = "SELECT id+1 FROM message_sessions ORDER BY id DESC LIMIT 1";
         Statement st = connection.createStatement();
         ResultSet data = st.executeQuery(sql);
         if (data.next())
             id = data.getInt(1);
 
         return id;
     }
 
     public void addRecipientsToSession(int sessionId, Collection<Integer> userIds) throws SQLException {
         for (int userId : userIds)
             addRecipientToSession(sessionId, userId);
 
     }
 
     public void addRecipientToSession(Integer sessionId, int userId) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement(
                 "INSERT INTO message_sessions_has_users (message_sessions_id, users_id) VALUES (?,?)");
         pStatement.setInt(1, sessionId);
         pStatement.setInt(2, userId);
         pStatement.executeUpdate();
     }
 
     public void unreadMessage(int sender, int sessionId, int messageId) {
         try {
             PreparedStatement pStatement = connection.prepareStatement(
                     "INSERT INTO messages_unread (messages_message_sessions_id,messages_id, users_id) VALUES (?,?,?)");
             pStatement.setInt(1, sessionId);
             pStatement.setInt(2, messageId);
             pStatement.setInt(3, sender);
             pStatement.executeUpdate();
         } catch (SQLException e) {
             logger.error("execute MySQL-query failed at unreadMessage.", e);
         }
     }
 
     public boolean hasNewMessages(int userId) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT * FROM messages_unread WHERE users_id = ? LIMIT 1");
         pStatement.setInt(1, userId);
         ResultSet data = pStatement.executeQuery();
         return data.next();
     }
 
     public boolean isUnread(int userId, int sessionId, int messageId) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT * FROM messages_unread WHERE messages_message_sessions_id = ? AND " +
                         "messages_id = ? AND users_id = ?");
         pStatement.setInt(1, sessionId);
         pStatement.setInt(2, messageId);
         pStatement.setInt(3, userId);
         ResultSet data = pStatement.executeQuery();
         return data.next();
     }
 
     public Date lastChange(int sessionId) throws SQLException, SessionNotFoundException {
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT datetime FROM messages WHERE message_sessions_id = ? ORDER BY datetime DESC LIMIT 1");
         pStatement.setInt(1, sessionId);
         ResultSet data = pStatement.executeQuery();
         if (data.next())
             return DateParser.parseDateTime(data.getString("datetime"));
 
         throw new SessionNotFoundException(sessionId);
     }
 
     public void readMessage(int sessionId, int messageId, int userId) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement(
                 "DELETE FROM messages_unread WHERE messages_message_sessions_id = ? AND " +
                         "messages_id = ? AND users_id = ?");
         pStatement.setInt(1, sessionId);
         pStatement.setInt(2, messageId);
         pStatement.setInt(3, userId);
         pStatement.executeUpdate();
         logger.info(userId + "reading message: " + messageId + " session:" + sessionId);
     }
 
     public int getNewMessageNum(int userId) throws SQLException {
         PreparedStatement pStatement = connection.prepareStatement(
                 "SELECT COUNT(*) AS counter FROM messages_unread WHERE users_id = ?");
         pStatement.setInt(1, userId);
         ResultSet data = pStatement.executeQuery();
         if (data.next())
             return data.getInt("counter");
         return -1;
     }
 
     public static class SessionNotFoundException extends Exception {
         public SessionNotFoundException(Collection<Integer> usersIds) {
             super(String.format("Session for users %s does not exist", StringUtils.join(usersIds, ",")));
         }
 
         public SessionNotFoundException(int sessionId) {
             super(String.format("Session with id %d does not exist", sessionId));
         }
 
         public SessionNotFoundException(int sessionId, int userId) {
             super(String.format("Session with id %d does not include userid %d", sessionId, userId));
         }
     }
 }
