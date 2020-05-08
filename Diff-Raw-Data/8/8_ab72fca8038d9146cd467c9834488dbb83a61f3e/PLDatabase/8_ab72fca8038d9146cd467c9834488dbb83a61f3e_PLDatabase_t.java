 package me.sd5.pvplogger;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 public class PLDatabase {
 	
 	private Connection connection;
 	private Statement statement;
 	private ResultSet resultSet;
 	
 	public PLDatabase() {
		System.out.println("Connecting to database...");
 		try {
 			connection = DriverManager.getConnection(PLConfig.dbUrl, PLConfig.dbUser, PLConfig.dbPassword);
 			statement = connection.createStatement();
			System.out.println("Successfully connected to database!");
 		} catch (SQLException e) {
 			System.out.println("Could not connect to database!");
 			return;
 		}
 		
 		System.out.println("Creating MySQL table...");
 		try {
 			statement.execute("CREATE TABLE " + PLConfig.dbTable);
			System.out.println("Successfully created MySQL table!");
 		} catch(SQLException e) {
 			System.out.println("MySQL table already exists!");
 			return;
 		}
 	}
 	
 }
