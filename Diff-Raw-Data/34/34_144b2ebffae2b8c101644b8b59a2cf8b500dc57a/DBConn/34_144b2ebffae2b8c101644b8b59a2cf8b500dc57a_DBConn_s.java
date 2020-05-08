 import java.sql.*;
 
 public class DBConn {
 	
<<<<<<< HEAD
	public static Connection getConnection(String args[]) throws Exception
 	{
 		Class.forName("com.mysql.jdbc.Driver"); 
 		String url = "jdbc:mysql://localhost/test"; 
 		String username = "root"; 
 		String pwd = ""; 
 		
 		Connection conn = DriverManager.getConnection(url, username, pwd);
 		return conn; 
=======
	public void getConnection() {
		System.out.println("Connecting to JDBC...");
		try {
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Successfully connected.");
		} catch (ClassNotFoundException e) {
			System.out.println("Error: No Driver!");
			e.printStackTrace();
			return;
		}
	 
		Connection connection = null;
		try {
			connection = DriverManager
			.getConnection("jdbc:mysql://localhost:3306/", "root", "mysql");
		} catch (SQLException e) {
			System.out.println("Error: Cannot connect!");
			e.printStackTrace();
			return;
		}
	 
		if (connection != null) {
			System.out.println("You made it, take control your database now!");
		} else {
			System.out.println("Cannot find database.");
		}
>>>>>>> d61e7c805e973d23aa25f9a3fc12e92f84428693
 		
 	}
 }
