 package amit.sqlite;
 
 import java.sql.*;
 import java.util.ArrayList;
 
 
 public class DbConnector {
 	Connection connection = null;
 	Statement statement = null;
 	boolean connectionStatus = false;
 	static DbConnector dbConnector = null;
 	ArrayList<DbObject> dbObjects = null;
 	
	public static DbConnector returnDbConnetor() {
 		if (dbConnector == null)
 			dbConnector = new DbConnector();
 		return dbConnector;
 	}
 	public ArrayList<DbObject> getResults(String classname){
 		try {
 			statement = connection.createStatement();
 			String sqlString = 
 					"select	"
 					+ "methodchange.commitid,	"
 					+ "oneline,	classcommit.classname,	"
 					+ "previousmethod,	"
 					+ "nextmethod "
 					+ "from	"
 					+ "classcommit,"
 					+ "commitdescription,"
 					+ "methodchange "
 					+ "where "
 					+ "classcommit.classname = methodchange.classname	"
 					+ "AND	"
 					+ "classcommit.commitid = methodchange.commitid	"
 					+ "AND	classcommit.commitid = commitdescription.commitid	"
 					+ "AND	classcommit.classname = ";
 			sqlString+="\""+classname+"\"";
 			System.out.println(sqlString);
 			ResultSet resultSet = statement.executeQuery(sqlString );
 			dbObjects = new ArrayList<DbObject>();
 			while (resultSet.next()) {
 				String oneline = resultSet.getString("oneline");
 				String commitid = resultSet.getString("commitid");
 				String previousMethod= resultSet.getString("previousmethod");
 				String nextmethod = resultSet.getString("nextmethod");
 				DbObject dbObject =  new DbObject(commitid, oneline, previousMethod, nextmethod);
 				dbObjects.add(dbObject);
 			}
 			resultSet.close();
 			statement.close();
 			return dbObjects;
 		} catch (SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		
 		return null;
 	}
 
 	private DbConnector() {
 		try {
 			Class.forName("org.sqlite.JDBC");
 			connection = DriverManager.getConnection("jdbc:sqlite:test.sqlite");
 			connection.setAutoCommit(false);
 			System.out.println("Connection Established");
 			connectionStatus = true;
 //			statement = connection.createStatement();
 //			ResultSet resultSet = statement.executeQuery("Select * from  files");
 //			while (resultSet.next()) {
 //				System.out.println(resultSet.getString("classname")); 
 //			}
 		
 		}catch(Exception e){
 			connectionStatus = false;
 		}
 	}
 }
