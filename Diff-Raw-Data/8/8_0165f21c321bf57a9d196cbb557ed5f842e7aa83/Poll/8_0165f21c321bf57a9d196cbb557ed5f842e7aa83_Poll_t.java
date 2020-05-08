 package org.freecode.irc.votebot.entity;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Deprecated
  * Date: 11/21/13
  * Time: 7:29 PM
  */
 public class Poll {
     private int id;
     private long expiry;
     private String question, options, creator;
     private boolean closed;
 
     public Poll(ResultSet rs) throws SQLException {
         id = rs.getInt(1);
         question = rs.getString(2);
         options = rs.getString(3);
         closed = rs.getBoolean(4);
        expiry = rs.getLong(5);
         creator = rs.getString(6);
     }
 
     public int getId() {
         return id;
     }
 
     public long getExpiry() {
         return expiry;
     }
 
     public String getQuestion() {
         return question;
     }
 
     public String getOptions() {
         return options;
     }
 
     public String getCreator() {
         return creator;
     }
 
     public boolean isClosed() {
         return closed;
     }
 }
