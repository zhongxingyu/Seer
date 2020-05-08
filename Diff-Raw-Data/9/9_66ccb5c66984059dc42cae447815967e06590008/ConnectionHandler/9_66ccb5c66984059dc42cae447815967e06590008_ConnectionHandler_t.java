 package com.minecraftdimensions.bungeesuite.database;
 
 import java.sql.Connection;
import java.sql.SQLException;
 
 public class ConnectionHandler {
 	private Connection connection;
 	private boolean used;
 	public long lastUsed;
 	
 	public ConnectionHandler(Connection connection) {
 		this.connection=connection;
 	}
 
 	public Connection getConnection(){
 		this.lastUsed = System.currentTimeMillis();
 		this.used=true;
 		return connection;
 	}
 	
 	public boolean isOldConnection(){
 		if((System.currentTimeMillis()-lastUsed)>3600000){
 			return true;
 		}else{
 			return false;
 		}
 		
 	}
 	
 	public void release(){
 		this.used=false;
 	}
 	
 	public boolean isUsed(){
 		return used;
 	}
 	
 	public void closeConnection(){
		try {
			this.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
 }
