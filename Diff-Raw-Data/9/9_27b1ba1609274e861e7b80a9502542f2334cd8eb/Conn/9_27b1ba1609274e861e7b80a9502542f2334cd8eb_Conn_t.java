 package Beans;
 
 import java.sql.Statement;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class Conn {
 	private static Conn instance;
 	private Connection con;
 	private ResultSet rs;
 	private Statement st;
 
 	private final String CONNECTURL = "jdbc:mysql://localhost:3306/mappedrace";
	private final String USER = "test";//TODO make TEST USER!!!
	
 	private final String PASS = "";
 
 	/**
 	 * Accessor for the Conn
 	 * 
 	 * @return
 	 */
 	public static Conn getInstance() {
 		return instance;
 	}
 
 	/**
 	 * Accessor for the jdbc connection
 	 * 
 	 * @return
 	 */
 	public Connection getConnection() {
 		return con;
 	}
 
 	static {
 		instance = new Conn();
 	}
 
 	/**
 	 * Constructor
 	 */
 	private Conn() {
 		try {
 			DriverManager.registerDriver(new com.mysql.jdbc.Driver());
 			con = DriverManager.getConnection(CONNECTURL, USER, PASS);
 			st = con.createStatement();
 			rs = st.executeQuery("SELECT VERSION()");
 
 			if (rs.next()) {
 				System.out.println(rs.getString(1));
 			}
 			
 			System.out.println ("Database connection established");
 			con.setAutoCommit(false);
 
 		} catch (SQLException ex) {
 			Logger lgr = Logger.getLogger(Conn.class.getName());
 			lgr.log(Level.SEVERE, ex.getMessage(), ex);
 		}
 	}
 }
