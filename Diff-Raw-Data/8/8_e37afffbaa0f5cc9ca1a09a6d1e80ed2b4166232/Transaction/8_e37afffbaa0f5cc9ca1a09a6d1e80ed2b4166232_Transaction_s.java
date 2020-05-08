 package com.mistphizzle.donationpoints.plugin.Objects;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import com.mistphizzle.donationpoints.plugin.DBConnection;
 
 public class Transaction {
 
 	final String player;
 	final Double price;
 	final String packageName;
 	final String date;
 	final String activated;
 	final String expires;
 	final String expiredate;
 	final String expired;
 	final String server;
 
 	public Transaction(String player, Double price, String packageName, String date, String activated, String expires, String expiredate, String expired, String server) {
 		this.player = player;
 		this.price = price;
 		this.packageName = packageName;
 		this.date = date;
 		this.activated = activated;
 		this.expires = expires;
 		this.expiredate = expiredate;
 		this.expired = expired;
 		this.server = server;
 	}
 
 	public static int getID() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable);
 		try {
 			if (DBConnection.engine.equalsIgnoreCase("mysql")) {
 				return rs2.getInt("id");
 			} else {
 				return rs2.getInt("rowid");
 			}
 		} catch (SQLException e) {
 			return 0;
 		}
 	}
 	
 	public static String getPlayer() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable);
 		try {
 			return rs2.getString("player");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static Double getPrice() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable);
 		try {
 			return rs2.getDouble("price");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return 0.0;
 	}
 	
 	public static String getPackageName() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable);
 		try {
 			return rs2.getString("package");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static String getDate() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable);
 		try {
 			return rs2.getString("date");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static String getActivated() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable);
 		try {
 			return rs2.getString("activated");
 		} catch (SQLException e ) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static String getExpires() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable);
 		try {
 			return rs2.getString("expires");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static String getExpireDate() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable);
 		try {
 			return rs2.getString("expiredate");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static String hasExpired() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable);
 		try {
 			return rs2.getString("expired");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
		return null;
 	}
 	
 	public static String getServer() {
 		ResultSet rs2 = DBConnection.sql.readQuery("SELECT * FROM " + DBConnection.transactionTable);
 		try {
 			return rs2.getString("server");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 }
