 package connections;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
import java.util.logging.Level;

import logger.GLogger;
 
 public class TargetDatabaseJDBC_Oracle implements TargetDAO
 {
 	 String userid="THO7_2011_2B_TEAM3B", password = "THO7_2011_2B_TEAM3B";
 	 String url = "jdbc:oracle:thin:@ondora01.hu.nl:8521:cursus01";
 	 Statement stmt;
 	 Connection con;
 	
 	@Override
 	public  String testConnection(String _username, String _password, String _URL)
 	{
 		 String s = "";
 		 try 
 			{
 				Class.forName("oracle.jdbc.OracleDriver");
 			} 
 			catch(java.lang.ClassNotFoundException e) 
 			{
 				s += e.getMessage();
 			}
 
 			try 
 			{
 				con = DriverManager.getConnection(_URL, _username, _password);
 			} 
 			catch(SQLException ex)
 			{
 				s += "\nConnection to database failed";
 			}
 			
 			if(s.equals(""))
 			{
 				return s = "Connection to database established";
 			}
 			else
 			{
 				return s;
 			}
 	}
 
 	public Connection getConnection(String _username, String _password, String _URL)
 	{		
 		try 
 		{
 			Class.forName("oracle.jdbc.OracleDriver");
 		} 
 		catch(java.lang.ClassNotFoundException e) 
 		{
 			System.err.print("ClassNotFoundException: ");
 			System.err.println(e.getMessage());
 		}
 
 		try 
 		{
 			con = DriverManager.getConnection(url, userid, password);
 		} 
 		catch(SQLException ex)
 		{
 			System.err.println("SQLException:  " + ex.getMessage());
 		}
 		return con;
 	}
 	
 	@Override
 	public boolean applyBusinessRule(String _code)
 	{
 		Connection con = getConnection(userid,password,url);
 		try 
 		{
			String [] code_parts = _code.split("/");
 	       	stmt = con.createStatement();
	       	
	       	for(String s : code_parts) {
		        stmt.executeQuery(s);
	       	}
 	        stmt.close();
 		    con.close();
 		    return true;
 		} 
 		catch(SQLException ex)
 		{
			GLogger.log(Level.SEVERE, "SQLException: " + ex.getMessage());
 			return false;
 		}
 	}
 	
 }
