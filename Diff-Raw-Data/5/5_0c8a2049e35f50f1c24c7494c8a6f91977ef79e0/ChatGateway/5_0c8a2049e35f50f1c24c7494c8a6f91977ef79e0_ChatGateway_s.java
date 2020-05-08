 package com.java.server.gateways;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import com.java.server.util.logging.Log;
 
 public class ChatGateway extends TableGateway{
 
 	String tableName = "chat_tbl";
 	public ChatGateway(Connection conn) {
 		super(conn, "chat_tbl");
 		// TODO Auto-generated constructor stub
 	}
 	
 	public synchronized ResultSet findAllRooms(){
 		try {
 			Statement stmt = con.createStatement();
 			String sql = "SELECT * FROM "+tableName+"";
 			ResultSet rs = stmt.executeQuery(sql);
 		}
 		catch(SQLException ex){
 			Log.writeToFile("Exception occured " + ex.toString());
 		}
 		return null;
 	}
 	
 	public synchronized ResultSet findRoomCreator(String roomname, String creatorName){
 		try{
 			Statement stmt = con.createStatement();
 			String sql = "SELECT '"+creatorName+"' FROM "+tableName+" WHERE `roomname` ='"+roomname+"'";
 			ResultSet rs = stmt.executeQuery(sql);
 		}
 		catch(SQLException ex){
 			Log.writeToFile("Exception occured " + ex.toString());
 		}
 		return null;
 	}
 	
 	public synchronized ResultSet findConcreteRoom(String roomname){
 		try {
 			Statement stmt = con.createStatement();
 			String sql = "SELECT * FROM "+tableName+"WHERE `roomname`='"+roomname+"'";
 			ResultSet rs = stmt.executeQuery(sql);
 		}
 		catch(SQLException ex){
 			Log.writeToFile("Exception occured " + ex.toString());
 		}
 		return null;
 	}
 	
 	public synchronized ResultSet findRoomById(int roomId){
 		try {
 			Statement stmt = con.createStatement();
 			String sql = "SELECT * FROM "+tableName+"WHERE `roomId`="+roomId+"";
 			ResultSet rs = stmt.executeQuery(sql);
 		}
 		catch(SQLException ex){
 			Log.writeToFile("Exception occured " + ex.toString());
 		}
 		return null;
 	}
 	
 	public synchronized ResultSet getAllChatUsers(int id){
 		try {
 			Statement stmt = con.createStatement();
			String sql = "SELECT user_tbl.login, user_tbl.id FROM chatroom_tbl,chatuser_tbl,user_tbl " +
 			             "WHERE chatroom_tbl.id = chatuser_tbl.chatRoomId " +
			             "AND chatuser_tbl.user_id = user_tbl.id AND chatroom_tbl.chatRoomId = "+id+"";
 			ResultSet rs = stmt.executeQuery(sql);
 		}
 		catch(Exception ex){
 			Log.writeToFile("Exception occured " + ex.toString());
 		}
 		return null;
 	}
 	
 	public synchronized void deleteAllRooms(){
 		try {
 			Statement stmt = con.createStatement();
 			String sql = "DELETE FROM "+tableName+"";
 			stmt.executeUpdate(sql);
 		}
 		catch(SQLException ex){
 			Log.writeToFile("Exception occured " + ex.toString());
 		}
 	}
 	
 	public synchronized void deleteConcreteRoom(int roomId){
 		try {
 			Statement stmt = con.createStatement();
 			stmt.executeUpdate("DELETE FROM "+tableName+"WHERE `roomId`="+roomId+"");
 		}
 		catch(SQLException ex){
 			Log.writeToFile("Exception occured " + ex.toString());
 		}
 	}
 }
