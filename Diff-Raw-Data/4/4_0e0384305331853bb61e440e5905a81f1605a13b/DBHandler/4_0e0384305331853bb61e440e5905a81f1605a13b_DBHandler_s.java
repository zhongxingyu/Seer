 package edu.berkeley.cs.cs162;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Time;
 import java.util.ArrayList;
 import java.util.List;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.Properties;
 
 
 public class DBHandler {
     private static Connection conn;
     static {
         conn = null;
         Properties connectionProps = new Properties();
         connectionProps.put("user", "group24");
         connectionProps.put("password", "dkhjjjprtd");
         try {
             conn = DriverManager.
                 getConnection("jdbc:" + "mysql" + "://" + "ec2-50-17-180-71.compute-1.amazonaws.com" +
                               ":" + 3306 + "/" + "group24", connectionProps);
         } catch (SQLException e) {
             // TODO Auto-generated catch block
             e.printStackTrace();
         }
     }
     
     public static void addToGroup(String uname, String gname) throws SQLException{
     	PreparedStatement pstmt = null;
     	try {
     		pstmt = conn.prepareStatement("INSERT INTO memberships (gname,username)" +
     		" VALUES (?,?)");
     		pstmt.setString(1, gname);
     		pstmt.setString(2, uname);
     		pstmt.executeUpdate();
     	} 
     	finally {
     		if(pstmt!=null) pstmt.close();
     	}
     }
     
     public static void writeLog(Message msg, String recipient) throws SQLException{
     	PreparedStatement pstmt = null;
     	pstmt = conn.prepareStatement("INSERT INTO messages (sender, sqn, timestamp, destination, message, recipient) " + 
     			"VALUES (?,?,?,?,?,?)");
     	pstmt.setString(1, msg.getSource());
     	pstmt.setInt(2, msg.getSQN());
     	long time = Long.parseLong(msg.getTimestamp());
     	pstmt.setTime(3,new Time(time));
     	pstmt.setString(4, msg.getDest());
     	pstmt.setString(5, msg.getContent());
     	pstmt.setString(6, recipient);
     	pstmt.executeUpdate();
     }
     
     public static List<Message> readLog(String uname) throws SQLException{
     	List<Message> messages = new ArrayList<Message>();
    	PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM messages WHERE recipient = " + uname);
     	if(pstmt==null) return null;
     	ResultSet rs = pstmt.executeQuery();
     	while(rs.next()){
     		String sender = rs.getString("sender");
     		int sqn = rs.getInt("sqn");
     		Long timestamp = rs.getTime("timestamp").getTime();
     		String destination = rs.getString("destination");
     		String content = rs.getString("message");
     		Message msg = new Message(timestamp.toString(),sender,destination,content);
     		msg.setSQN(sqn);
     		messages.add(msg);
     	}
     	
     	return messages;
     }
     
     public static void addUser(String username, String salt, String hashedPassword) throws SQLException {
     	Statement stmt = conn.createStatement();
     	stmt.executeQuery("INSERT INTO users (username, salt, encrypted_password) VALUES + (" + username + "," + salt +"," + hashedPassword + ")");
     	
     }
     
     public static String getSalt(String username) throws SQLException {
     	Statement stmt = conn.createStatement();
     	ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username = " + username);
     	rs.next();
     	String salt = rs.getString("salt");
     	return salt;
     	
     }
     
     public static void removeFromGroup(String uname, String gname) throws SQLException{
         	PreparedStatement pstmt = null;
         	try {
         		pstmt = conn.prepareStatement("DELETE FROM memberships WHERE gname = ? AND username = ?");
         		pstmt.setString(1, gname);
         		pstmt.setString(2, uname);
         		pstmt.executeUpdate();
         	} 
         	finally {
         		if(pstmt!=null) pstmt.close();
         	}
     }
     
     public static String getHashedPassword(String uname) throws SQLException {
     	Statement stmt = conn.createStatement();
     	ResultSet rs = stmt.executeQuery("SELECT * FROM users WHERE username = " + uname);
     	rs.next();
     	String hashedPassword = rs.getString("encrypted_password");
     	return hashedPassword;
     }
     
     public static ResultSet getUsers() throws SQLException {
     	Statement stmt = conn.createStatement();
     	return stmt.executeQuery("SELECT username FROM users");
     }
     
     public static ResultSet getGroups() throws SQLException {
     	Statement stmt = conn.createStatement();
     	return stmt.executeQuery("SELECT gname FROM groups");
     }
     
     public static ResultSet getMemberships() throws SQLException {
     	Statement stmt = conn.createStatement();
     	return stmt.executeQuery("SELECT * FROM memberships");
     }
 }
