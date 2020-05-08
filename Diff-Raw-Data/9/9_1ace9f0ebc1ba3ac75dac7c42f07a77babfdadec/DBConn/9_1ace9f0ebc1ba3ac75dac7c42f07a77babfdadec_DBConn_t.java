 package db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 
 import dao.User;
 
 public class DBConn {
 
 	private final static String _mysql_Driver = "com.mysql.jdbc.Driver";
 	private final static String hostname = "localhost";
 	private final static String port = "3306";
 	private final static String dbname = "hack";
 	private final static String user = "root";
	private final static String password= "jesus";
 	
 	// singleton conn
 	private static Connection conn = null;
 	
 	public static Connection getDbConnection(){
 		if(conn == null){
 			initConnection();
 		}
 		
 		return conn;
 	}
 	
 	public static void closeDbConnection(){
 		try{
 			if( conn != null){
 				conn.close();
 				System.out.println("connectin closed");
 			}
 		}catch(Exception e){
 			System.err.println("Unable to close the DatabaseConnection");
 			e.printStackTrace();
 		}
 	}
 	
 	private static void initConnection(){
 		String database = "jdbc:mysql://" + hostname + ":" + port + "/" + dbname;
 		
 		try {
 			Class.forName(_mysql_Driver).newInstance();
 		} catch (Exception e) {
 			System.err.println("Unable to load this driver: " + _mysql_Driver);
 			e.printStackTrace();
 		}
 		
 		try{
 			conn = DriverManager.getConnection(database, user, password);
 		} catch(SQLException s){
 			System.err.println(s.getMessage() 
 					+ "\n\n SQLState: " 
 					+ s.getSQLState()
 					+ "\n\n ErrorCode: "
 					+ s.getErrorCode());
 		}
 	}
 	
 	public static void main(String[] args){
 		
 		TableQuery t = new GiftTable();
 		t.printTable(t.getAll());
 	}
 	
 }
