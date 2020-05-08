 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package uk.ac.dundee.computing.fordyce.nwj.mrblabby.dataservice;
 
 import java.sql.CallableStatement;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import uk.ac.dundee.computing.fordyce.nwj.mrblabby.bean.Message;
 import uk.ac.dundee.computing.fordyce.nwj.mrblabby.bean.MessageList;
 import uk.ac.dundee.computing.fordyce.nwj.mrblabby.bean.User;
 
 /**
  *
  * @author Neil
  */
 public class MessageService extends DatabaseConnector {
 
     /**
      * Adds message to the database for a given user and message
      * 
      * @param user bean
      * @param message 
      */
     public void createMessage(User user, String message) {
         try {
             connect = getConnection();
             CallableStatement proc = connect.prepareCall("{ ? = call create_message(?, ?)}");
             proc.registerOutParameter(1, java.sql.Types.BOOLEAN);
             proc.setString(2, user.getEmail());
             proc.setString(3, message);
             proc.execute();
             connect.close();
         } catch (SQLException e) {
             System.err.println("Database connection unavailable to create message: " + e.toString());
         }
     }
     
     /**
      * Gets a message list of the most recent messages from a user.
      * 
      * @param user account bean
      * @param maxMessages max number of messages to return
      * @return MessageList bean containing most recent messages
      */
     public MessageList getMessageList(User user, int startMessage, int maxMessages){
         return getMessageList(user.getEmail(), startMessage, maxMessages);
     }
     
     /**
      * Gets a message list of the most recent messages from a particular user.
      * 
      * @param email of user account
      * @param startMessage omit most recent message up until this point
      * @param maxMessages max number of messages to return
      * @return MessageList bean containing most recent messages first
      */
     public MessageList getMessageList(String email, int startMessage, int maxMessages){
         try {
             connect = getConnection();
             
             PreparedStatement ps = connect.prepareStatement("SELECT message, email, created_timestamp "
                                                         + "FROM message WHERE email = ?"
                                                         + "ORDER BY created_timestamp DESC;");
             ps.setString(1, email);
             MessageList messageList = execute(ps, startMessage, maxMessages);
 
             return messageList;
         } catch (SQLException e) {
             System.err.println("Database connection unavailable to create message: " + e.toString());
         }
         
         return null;
     }
     
     /**
      * Gets a message list for the message with a particular id
      * 
      * @param startMessage
      * @param maxMessages
      * @return 
      */
     public MessageList getMessageList(int startMessage, int maxMessages, int id){
         try {
             connect = getConnection();
             
             PreparedStatement ps = connect.prepareStatement("SELECT message, email, created_timestamp "
                                                         + "FROM message WHERE id = ?"
                                                         + "ORDER BY created_timestamp DESC;");
             
             ps.setInt(1, id);
             
             MessageList messageList = execute(ps, startMessage, maxMessages);
  
             return messageList;
         } catch (SQLException e) {
             System.err.println("Database connection unavailable to create message: " + e.toString());
         }
         
         return null;
     }
     
     
     /**
      * Gets a message list for all the messages
      * 
      * @param startMessage
      * @param maxMessages
      * @return 
      */
     public MessageList getMessageList(int startMessage, int maxMessages){
         try {
             connect = getConnection();
             
             PreparedStatement ps = connect.prepareStatement("SELECT message, email, created_timestamp "
                                                         + "FROM message "
                                                         + "ORDER BY created_timestamp DESC;");
             
             MessageList messageList = execute(ps, startMessage, maxMessages);
             
   
             return messageList;
         } catch (SQLException e) {
             System.err.println("Database connection unavailable to create message: " + e.toString());
         }
         
         return null;
     }
     
     private MessageList execute(PreparedStatement ps, int startMessage, int maxMessages) throws SQLException {
         ResultSet rs = ps.executeQuery();
         int querySize = 0;
         
         //Read off records until you get to the start message
         while(rs.next() && startMessage > 0) {
             querySize++;
             startMessage--;
         }
         //Create message beans and add them to a message list bean
         MessageList messageList = new MessageList();
        while(rs.next() && maxMessages > 0){
             querySize++;
             Message message = new Message(rs.getString("message"),
                                             rs.getString("email"),
                                             rs.getTimestamp("created_timestamp").getTime());
             messageList.addMessage(message);
             maxMessages--;
         }
         
         while(rs.next()){querySize++;}
         messageList.setQuerySize(querySize);
         
         connect.close();
         
         return messageList;
     }
 }
