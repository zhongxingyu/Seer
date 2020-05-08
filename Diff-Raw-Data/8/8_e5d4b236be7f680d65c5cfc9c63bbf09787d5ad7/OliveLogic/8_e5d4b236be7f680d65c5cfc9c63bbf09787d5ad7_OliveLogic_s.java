 package com.readytalk.olive.logic;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.Statement;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 import com.readytalk.olive.model.User;
 
 /**
  * This is a nice, normal type of class. I call it logic because it is the type
  * of place where you would want to write some "business logic". The sorts of
  * things that a servlet would kick off. In this case, it's a ridiculous example
  * that mocks me (and other older folks). Not that old, of course...
  * 
  * All that said, this is a good sort of class to make to do things. I made the
  * methods static because this will only be used in the HttpRequest/HttpResponse
  * cycle that a user will initiate.
  * 
  * I hope that makes sense, read on!
  * 
  * @author mweaver
  */
 public class OliveLogic {
 
 	/**
 	 * I don't use this method in my example, but this would be an excellent
 	 * place to put some logic that:
 	 * 
 	 * 1) Connects to a database. 2) Validates a username and password. 3)
 	 * returns some value.
 	 * 
 	 * I use a boolean here, but an enum that you write would be heaps better!
 	 * 
 	 * @param username
 	 * @param password
 	 * @return
 	 */
 	public static Boolean isAuthorized(User user) {
 		try{ 
 			Connection conn = getDBConnection();	
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
 			s = "SELECT Username FROM Accounts WHERE Username = '"
 				+user.getUsername()+"' AND Password = '"+user.getPassword()+"';";
 			ResultSet r = st.executeQuery(s);
 			if(r.first()){
 				closeConnection(conn);
				return "Welcome!";
 			}
 			else{
 				closeConnection(conn);
				return "Incorrect username and/or password.";
 			}
 			
 		}catch (Exception e) { e.printStackTrace(); }
 		/*if (user.getUsername().equals("olive")
 				&& user.getPassword().equals("evilo")) {
 			return true;
 		} else {
 			return false;
 		}*/
 
 		/*
 		 * try { Statement stmt = null;
 		 * 
 		 * // Register the JDBC driver for MySQL.
 		 * Class.forName("com.mysql.jdbc.Driver");
 		 * 
 		 * // Define URL of database server for // database named mysql on the
 		 * localhost // with the default port number 3306. String url =
 		 * "jdbc:mysql://localhost:3306/testDB";
 		 * 
 		 * // Get a connection to the database for a // user named root with a
 		 * blank password. // This user is the default administrator // having
 		 * full privileges to do anything. Connection con =
 		 * DriverManager.getConnection(url, "root", "olive");
 		 * 
 		 * // Display URL and connection information System.out.println("URL: "
 		 * + url); System.out.println("Connection: " + con); stmt =
 		 * con.createStatement();
 		 * 
 		 * stmt.executeUpdate("CREATE TABLE myTable(test_id int," +
 		 * "test_val char(15) not null)");
 		 * stmt.executeUpdate("INSERT INTO myTable(test_id, " +
 		 * "test_val) VALUES(2,'Two')");
 		 * stmt.executeUpdate("INSERT INTO myTable(test_id, " +
 		 * "test_val) VALUES(3,'Three')");
 		 * stmt.executeUpdate("INSERT INTO myTable(test_id, " +
 		 * "test_val) VALUES(4,'Four')");
 		 * stmt.executeUpdate("INSERT INTO myTable(test_id, " +
 		 * "test_val) VALUES(5,'Five')");
 		 * 
 		 * try { stmt.executeUpdate("DROP TABLE myTable"); } catch (Exception e)
 		 * { System.out.print(e);
 		 * System.out.println("No existing table to delete"); }
 		 * 
 		 * } catch (Exception e) { e.printStackTrace(); }
 		 */
		return "error!";
 	}
 	public static Connection getDBConnection(){
 		try{
 			Context initCtx = new InitialContext();
 			DataSource ds = (DataSource)initCtx.lookup("java:comp/env/jdbc/OliveData");
 			return ds.getConnection();
 		}catch (Exception e) { e.printStackTrace(); }
 		return null;
 	}
 	public static void closeConnection(Connection c){
 		try{
 			c.close();
 		}catch (Exception e) { e.printStackTrace(); }
 	}
 	public static void AddAccount(String username, String password, String name, String email){
 		try{
 			Connection conn = getDBConnection();
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
 			s = "INSERT INTO Accounts (Username, Password, Name, Email)" +
 					"VALUES ('"+username+"', '"+password+"' , '"+name+"', '"+email+"');";
 			st.executeUpdate(s);
 			closeConnection(conn);
 		}catch (Exception e) { e.printStackTrace(); }	
 	}
 	public static void AddProject(String name, int AccountID, String icon){
 		try{
 			Connection conn = getDBConnection();
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
 			s = "INSERT INTO Projects (Name, AccountID, Icon)" +
 					"VALUES ('"+name+"', '"+AccountID+"' , '"+icon+"');";
 			st.executeUpdate(s);
 			closeConnection(conn);
 		}catch (Exception e) { e.printStackTrace(); }	
 	}
 	public static void AddVideo(String name,String URL,int ProjectID,String icon){
 		try{
 			Connection conn = getDBConnection();
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
 			s = "INSERT INTO Videos (Name, URL, ProjectID, Icon)" +
 					"VALUES ('"+name+"', '"+URL+"', '"+ProjectID+"' , '"+icon+"');";
 			st.executeUpdate(s);
 			closeConnection(conn);
 		}catch (Exception e) { e.printStackTrace(); }	
 	}
 	public static void deleteAccount(User user){
 		try{
 			Connection conn = getDBConnection();
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
 			s = "DELETE FROM Accounts WHERE" +
 					"username = '"+user.getUsername()+"';"; //Need to add error checking
 			st.executeUpdate(s);
 			closeConnection(conn);
 		}catch (Exception e) { e.printStackTrace(); }
 	}
 	public static void deleteProject(String name, int AccountID){
 		try{
 			Connection conn = getDBConnection();
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
 			s = "DELETE FROM Projects WHERE" +
 					"Name = '"+name+"' AND AccountID = '"+AccountID+"';"; //Need to add error checking
 			st.executeUpdate(s);
 			closeConnection(conn);
 		}catch (Exception e) { e.printStackTrace(); }
 	}
 	public static void deleteVideo(String URL){
 		try{
 			Connection conn = getDBConnection();
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
 			s = "DELETE FROM Videos WHERE" +
 					"URL = '"+URL+"';"; //Need to add error checking
 			st.executeUpdate(s);
 			closeConnection(conn);
 		}catch (Exception e) { e.printStackTrace(); }
 	}
 	
 }
