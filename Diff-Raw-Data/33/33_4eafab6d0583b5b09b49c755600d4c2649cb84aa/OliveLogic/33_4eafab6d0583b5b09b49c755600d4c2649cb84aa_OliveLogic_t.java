 package com.readytalk.olive.logic;
 
 import java.io.UnsupportedEncodingException;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 import com.readytalk.olive.model.Project;
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
 				
 				return true;
 			}
 			else{
 				closeConnection(conn);
 				return false;
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
 		return false;	// Error!
 	}
 	public static String populateProjects(User user) {
 
 		String projects = "";
 		try {
 			Connection conn = getDBConnection();
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
			if(user==null){
				return projects;
			}
 			if(user.getAccountID()==-1){
 				s = "SELECT AccountID FROM Accounts WHERE username ='"+user.getUsername()+"';";
 				ResultSet r = st.executeQuery(s);
 				int accountID = -1;
 				if(r.first()){
 					accountID = r.getInt("AccountID");
 				}
 				user.setAccountID(accountID);
 			}
 			s  = "SELECT * FROM Projects WHERE AccountID = "+user.getAccountID()+";";
 			ResultSet r = st.executeQuery(s);
 			if(r.first()){
 				int projectNum = 0;
 				do{
 					projectNum +=1;
 					String projectTitle = r.getString("Name");
 					String projectIcon = "/olive/images/SPANISH OLIVES.jpg";
 					projects += "<div id=\"project-"+projectNum+"\" class=\"project-icon-container\">" + "\n"+
 							"<img src=\""+projectIcon+"\" class=\"project-icon\" alt=\""+projectTitle+"\" />" + "\n"+
 							"<p><a href=\"OliveServlet?page=editor&projectTitle="+projectTitle+"\">"+projectTitle+"</a></p>" + "\n"+
 							"<p><small><a href=\"\" class=\"warning\">Delete</a></small></p>" + "\n"+
 							"</div>" +
 							"\n";
 				}while(r.next());
 			}
 			return projects;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return projects;
 		//TODO change db to have unique usernames for accounts and names for both projects and videos in one project 
 		
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
 	public static boolean AddAccount(User user){
 		try{
 			Connection conn = getDBConnection();
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
 			s = "INSERT INTO Accounts (Username, Password, Name, Email)" +
 					"VALUES ('"+user.getUsername()+"', '"+user.getPassword()+"' " +
 							", '"+user.getName()+"', '"+user.getEmail()+"');";
 			st.executeUpdate(s);
			s = "SELECT AccountID FROM Accounts WHERE username ='"+user.getUsername()+"';";
 			ResultSet r = st.executeQuery(s);
 			if(r.first()){
 				int id = r.getInt("AccountID");
 				user.setAccountID(id);
 			}
 			else{
 				//TODO Add error for rare case that it can't find the data
 			}	
 			closeConnection(conn);
 			return true;
 		}catch (Exception e) { e.printStackTrace(); }	
 		return false;
 	}
 	public static void AddProject(Project p, User u){
 		try{
 			Connection conn = getDBConnection();
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
 			s = "SELECT AccountID FROM Accounts WHERE username ='"+u.getUsername()+"';";
 			ResultSet r = st.executeQuery(s);
 			int accountID = -1;
 			if(r.first()){
 				accountID = r.getInt("AccountID");
 			}
 			if(accountID == -1){
 				throw new SQLException("There is no account for the username: "+u.getUsername());
 			}
 			s = "INSERT INTO Projects (Name, AccountID, Icon)" +
 					"VALUES ('"+p.getTitle()+"', '"+accountID+"' , '');";
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
 	public static Boolean editAccount(User user){
 		try{
 			Connection conn = getDBConnection();
 			Statement st = conn.createStatement();
 			String s = "USE OliveData;";
 			st.executeUpdate(s);
 			s = "UPDATE Accounts SET Name = '"+user.getName()+"' WHERE Username " +
 					"= '"+user.getUsername()+"'";
 			st.executeUpdate(s);
 
 			s = "UPDATE Accounts SET Password = '"+user.getPassword()+"' WHERE Username " +
 					"= '"+user.getUsername()+"'";
 			st.executeUpdate(s);
 
 			s = "UPDATE Accounts SET Email = '"+user.getEmail()+"' WHERE Username " +
 					"= '"+user.getUsername()+"'";
 			st.executeUpdate(s);
 			closeConnection(conn);
 			return true;
 		}catch (Exception e) { e.printStackTrace(); }
 		return false;
 	}
 	
 	public static String sanitize(String input) throws UnsupportedEncodingException {
 		String output = input;
 		
 		// Remove the *really* bad stuff (which cause XSS attacks and SQL
 		// injections).
 		String[] illegalStrings = {"!", "#", "$", "%", "^", "&", "*", "(", ")", "-", "=", "{", "}", "[", "]", "\\", "|", ";", "'", "\"", ":", ",", "<", ">", "/", "?", "`", "~"};
 		for (int i = 0; i < illegalStrings.length; ++i) {
 			output = output.replace(illegalStrings[i], "");
 		}
 		
 		// Let encoding convert the rest to safe strings (but not remove).
 		String encoding = "UTF-8";	// This is recommended by the The World Wide Web Consortium Recommendation. See : http://download.oracle.com/javase/1.4.2/docs/api/java/net/URLEncoder.html#encode(java.lang.String, java.lang.String)
 									// Other encodings: http://download.oracle.com/javase/1.4.2/docs/api/java/nio/charset/Charset.html
 		try {
 			output = java.net.URLEncoder.encode(output, encoding);
 		} catch (UnsupportedEncodingException e) {
 			throw new UnsupportedEncodingException("Choose a different encoding than \"" + encoding + "\". " + e.getMessage());
 		}
 		
 		return output;
 	}
 }
