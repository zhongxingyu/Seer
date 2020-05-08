 /**
  *  Name: InsertDeathPointThread.java
  *  Date: 16:27:53 - 12 sep 2012
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
 
 public class InsertDeathPointThread extends Thread {
 	
 	private final String username;
 	private final String password;
 	private final String host;
 	private final int    port;
 	private final String database;
 	private final String tablename;
 	
 	private final String playername;
 	private final int points;
 	
 	public InsertDeathPointThread(String username, String password, String host, int port, String database, String tablename, String playername, int points) {
 		
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
 			
 			int deaths = points;
 			
 			if(rs.next() && rs.getString("playernames") != null) {
 				deaths = deaths + rs.getInt("deaths");
 				
 				stmt.executeUpdate("UPDATE " + tablename + " SET deaths=" + deaths + " WHERE playernames='" + playername + "'");
 			}
 			else {
 				stmt.executeUpdate("INSERT INTO " + tablename + " VALUES('" + playername + "', 0, 0, " + deaths + ")");
 			}
 			
 			rs.close();
 			stmt.close();
 			con.close();
 		}
 		catch(SQLException | ClassNotFoundException e) {
			System.out.println("Error while inserting deathpoint! Message: " + e.getMessage());
 		}
 	}
 }
