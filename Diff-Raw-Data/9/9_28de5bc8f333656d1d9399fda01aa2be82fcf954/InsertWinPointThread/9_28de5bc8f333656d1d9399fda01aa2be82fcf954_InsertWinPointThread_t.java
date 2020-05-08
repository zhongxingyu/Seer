 /**
  *  Name: InsertWinPointThread.java
  *  Date: 16:25:27 - 12 sep 2012
  * 
  *  Author: LucasEmanuel @ bukkit forums
  *  
  *  
  *  Description:
  *  
  *  
  *  
  * 
  * 
  */
 
 package me.lucasemanuel.survivalgamesmultiverse.threads;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class InsertWinPointThread extends Thread {
 	
 	private final String username;
 	private final String password;
 	private final String host;
 	private final int    port;
 	private final String database;
 	private final String tablename;
 	
 	private final String playername;
 	private final int points;
 	
 	public InsertWinPointThread(String username, String password, String host, int port, String database, String tablename, String playername, int points) {
 		
 		this.username   = username;
 		this.password   = password;
 		this.host       = host;
 		this.port       = port;
 		this.database   = database;
 		this.tablename  = tablename;
 		
 		this.playername = playername;
 		this.points     = points;
 		
 		start();
 	}
 
 	public void run() {
 		try {
 			
 			Class.forName("com.mysql.jdbc.Driver");
 			
 			String url = "jdbc:mysql://" + host + ":" + port + "/" + database;
 			
 			Connection con = DriverManager.getConnection(url, username, password);
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM " + tablename + " WHERE playernames='" + playername + "'");
 			
 			int wins = points;
 			
 			if(rs.next() && rs.getString("playernames") != null) {
 				wins = wins + rs.getInt("wins");
 				
 				stmt.executeUpdate("UPDATE " + tablename + " SET wins=" + wins + " WHERE playernames='" + playername + "'");
 			}
 			else {
 				stmt.executeUpdate("INSERT INTO " + tablename + " VALUES('" + playername + "', " + wins + ", 0, 0)");
 			}
 			
 			rs.close();
 			stmt.close();
 			con.close();
 		}
 		catch(SQLException | ClassNotFoundException e) {
			System.out.println("Error while inserting winpoint! Message: " + e.getMessage());
 		}
 	}
 }
