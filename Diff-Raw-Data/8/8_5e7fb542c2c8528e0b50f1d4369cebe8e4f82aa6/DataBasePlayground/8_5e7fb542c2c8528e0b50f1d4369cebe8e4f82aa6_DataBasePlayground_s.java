 package database;
 import java.sql.*;
 import java.util.Calendar;
 import java.util.Properties;
 
 public class DataBasePlayground {
 	// Suppress default constructor for noninstantiability
 	private DataBasePlayground() {
 		throw new AssertionError();
 	}
 
 	static final String URL = "jdbc:postgresql://localhost:5433/chpv1";
 	static final String USER = "postgres";
 	static final String PASSWORD = "postgres";
 
	static PreparedStatement statementGetOrders = null;
 
 	static PreparedStatement statementPutOrder = null;
 
 	public static Connection getConnection() {
 		try {
 			Class.forName("org.postgresql.Driver");
 			Properties props = new Properties();
 			props.setProperty("user", USER);
 			props.setProperty("password", PASSWORD);
 			Connection conn = DriverManager.getConnection(URL, props);
 			return conn;
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/*###########################################
 	 *       ORDER MANAGEMENT SECTION
 	 ############################################*/
 	
 	/**
 	 * This function initializes the prepared Statements.
 	 * @param con The database connection
 	 */
 	public static void init(Connection con) {
 		try {
 			statementPutOrder = con.prepareStatement("INSERT INTO orders "
 					+ "(facility_id,drug_id,unit_number,date)"
 					+ " VALUES (?,?,?,?)");
			statementGetOrders = con.prepareStatement("SELECT o.id, d.med_name,"
					+ " o.unit_number, d.unit, d.id, o.date"
					+ " FROM orders o, drugs d"
					+ " WHERE o.drug_id = d.id"
					+ " AND o.facility_id = ?"
					+ " ORDER BY o.id ASC");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * 
 	 * @param orders
 	 *            Orders saved as arrays like [facility_id,drug_id,unit_number]
 	 */
 	public static void putOrders(int[][] orders) {
 		if (statementPutOrder == null) {
 			System.err.println("Init the statements, dude.. (init())");
 			return;
 		}
 		try {
 			for (int[] order : orders) {
 				statementPutOrder.setInt(1, order[0]);
 				statementPutOrder.setInt(2, order[1]);
 				statementPutOrder.setInt(3, order[2]);
 				statementPutOrder.setDate(4, new java.sql.Date(Calendar.getInstance().getTime().getTime()));
 				statementPutOrder.addBatch();
 			}
 			statementPutOrder.executeBatch();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void deleteOrder(int order_id) {
 		
 	}
 	
 	public static void main(String[] args) {
 		Connection con = DataBasePlayground.getConnection();
 		DataBasePlayground.init(con);
 		int[][] orders = {{1,2,10},{1,3,10},{1,1,10}};
 		DataBasePlayground.putOrders(orders); //should fail
 //		DataBasePlayground.initPutOrders(con);
 //		DataBasePlayground.putOrders(orders);
 		try {
 			con.close();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 
 }
