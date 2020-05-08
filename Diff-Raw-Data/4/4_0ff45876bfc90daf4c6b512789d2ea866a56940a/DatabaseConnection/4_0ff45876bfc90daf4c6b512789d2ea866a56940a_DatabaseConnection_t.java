 package local.acme.library;
 
 import java.sql.*;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class DatabaseConnection
 {
 	private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
 
	private String url = "jdbc:mysql://localhost:3306/";
	private String dbName = "acme-library";
 	private String driver = "com.mysql.jdbc.Driver";
 	private String userName = "acme";
 	private String password = "acme";
 
 	private Connection connection = null;
 
 	public DatabaseConnection()
 	{
 		try
 		{
 			Class.forName(driver).newInstance();
 			connection = DriverManager.getConnection(url + dbName, userName, password);
 			logger.info("Connected to the database");
 		}
 
 		catch (Exception e)
 		{
 			logger.error(e.getMessage());
 		}
 	}
 
 	public ResultSet executeQuery(String sql)
 	{
 		try
 		{
 			Statement stmt = connection.createStatement();
 			return stmt.executeQuery(sql);
 		}
 
 		catch (Exception e)
 		{
 			logger.error(e.getMessage());
 			return null;
 		}
 	}
 
 	public int executeUpdate(String sql)
 	{
 		try
 		{
 			Statement stmt = connection.createStatement();
 			return stmt.executeUpdate(sql);
 		}
 
 		catch (Exception e)
 		{
 			logger.error(e.getMessage());
 			return -1;
 		}
 	}
 }
