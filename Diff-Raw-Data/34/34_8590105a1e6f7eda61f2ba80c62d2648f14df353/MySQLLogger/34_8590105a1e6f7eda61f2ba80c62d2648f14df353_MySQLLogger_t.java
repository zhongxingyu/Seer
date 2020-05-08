 /**
  * Class for logging reasoning metrics to a MySQL database
  * 
  * @author Chris (c.baillie@abdn.ac.uk)
  */
 
 package uk.ac.dotrural.reasoning.logging;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 public class MySQLLogger {
 	
 	private String url;
 	private String user;
 	private String password;
 	
 	/**
 	 * Constructor
 	 * 
 	 * @param url The url of the MySQL database
 	 * @param user Username for the database
 	 * @param password Password for the database
 	 */
 	public MySQLLogger(String url, String user, String password)
 	{
 		this.url = url;
 		this.user = user;
 		this.password = password;
 	}
 	
 	private Connection getConnection()
 	{
 		Connection con = null;
 		try
 		{
 			Class.forName("com.mysql.jdbc.Driver");
 			con = DriverManager.getConnection(url, user, password);
 			System.out.println("[MySQLLogger] Connection created");
 		}
 		catch (Exception ex)
 		{
 			ex.printStackTrace();
 		}		
 		return con;
 	}
 	
 	public ResultSet doQuery(String query)
 	{
		System.out.println("MySQLLogger] Query: " + query);
 		Connection con = getConnection();
 		try
 		{
 			Statement stmt = con.createStatement();
 			con.close();
 			return stmt.executeQuery(query);
 		}
 		catch(Exception ex)
 		{
 			System.out.println("[MySQLLogger] doQuery : Query failed");
 			ex.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Perform an update query
 	 * 
 	 * @param updateQuery The query to be executed
 	 * @return boolean indicating success
 	 */
 	public boolean doMySQLInsert(String updateQuery)
 	{
 		Connection con = getConnection();
 		try
 		{
 			Statement stmt = con.createStatement();
			int result = stmt.executeUpdate(updateQuery);
 			con.close();
			if(result > 0)
				return true;
 		}
 		catch(Exception ex)
 		{
 			System.out.println("[MySQLLogger] doMySQLInsert : Update failed");
 			ex.printStackTrace();
 		}
 		return false;
 	}
 
 }
