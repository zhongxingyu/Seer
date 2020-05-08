 package pian.model.dao;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.sql.DataSource;
 
 public class ConnectionFactory {
 	
 	public static Connection getConnection(){
 		Connection connection = null;
 		InitialContext ctx;
 		try {
 			ctx = new InitialContext();
 			Context envContext  = (Context)ctx.lookup("java:/comp/env"); 
 			DataSource ds = (DataSource)envContext.lookup("jdbc/PianDB");
 			connection = ds.getConnection();
 		} catch (Exception e) {
 			//System.out.println("Can't connect as connection pool. System will create a normal connection.");
 			try {
 				Class.forName("org.sqlite.JDBC");
				connection = DriverManager.getConnection("jdbc:sqlite:pian.sqlite");
 			} catch (Exception e1) {
 				e1.printStackTrace();
 			}
 		} 
 		return connection;
 	}
 }
