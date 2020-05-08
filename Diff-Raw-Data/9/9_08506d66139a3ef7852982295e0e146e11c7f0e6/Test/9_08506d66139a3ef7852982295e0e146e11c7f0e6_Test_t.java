 package amit.sqlite;
 import java.sql.*;
 import java.util.ArrayList;
 
 public class Test {
 
 	public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		System.out.println("Hello from sqlite");
 //		Connection connection = null;
 //		Statement statement = null;
 //		try {
 //			Class.forName("org.sqlite.JDBC");
 //			connection = DriverManager.getConnection("jdbc:sqlite:test.sqlite");
 //			connection.setAutoCommit(false);
 //			System.out.println("Connection Established");
 //			statement = connection.createStatement();
 //			ResultSet resultSet = statement.executeQuery("Select * from  files");
 //			while (resultSet.next()) {
 //				System.out.println(resultSet.getString("classname")); 
 //			}
 //			resultSet.close();
 //			statement.close();
 //			connection.close();
 //			
 //		} catch (Exception e) {
 //			// TODO: handle exception
 //			System.err.println(e.getClass().getName()+":"+e.getMessage());
 //			System.exit(0);
 //		}
 		
		DbConnector dbConnector = DbConnector.returnDbConnetor();
 		ArrayList<DbObject> dbObjects = dbConnector.getResults("StormClientHandler");
 		for (DbObject dbObject : dbObjects) {
 			System.out.println("CommitID: "+dbObject.commitid);
 		}
 	}
 
 }
