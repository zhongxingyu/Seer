 package vsp;
 
 import java.util.Date;
 import java.sql.*;
 import javax.sql.*;
 import javax.jws.*;
 
 public class VspWebServiceImpl
 {
 	public AccountData getAccountInfo(String userName)
 	{
 		AccountData data = new AccountData("[unavailable]", new Date());
 		
		String dbUrl = "jdbc:mysql://your.database.domain/yourDBname";
 		String dbClass = "com.mysql.jdbc.Driver";
 		String query = "SELECT email, signup FROM users WHERE user_name='" + userName + "'";
 
 		try
 		{
 			Class.forName(dbClass);
			Connection con = DriverManager.getConnection (dbUrl);
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(query);
 			
 			String email = rs.getString("email");
 			Date signup = rs.getDate("signup");
 			
 			data = new AccountData(email, signup);
 
 			con.close();
 		} //end try
 		catch(ClassNotFoundException e)
 		{
 			e.printStackTrace();
 		}
 		catch(SQLException e)
 		{
 			e.printStackTrace();
 		}
 		
 		return data;
 	}
 }
