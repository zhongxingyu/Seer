 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.DatabaseMetaData;
 import java.util.ArrayList;
 import java.util.Map;
 
 import org.jfree.data.Range;
 import org.jfree.data.xy.YIntervalSeries;
 
 public class JdbcYIntervalSeries extends YIntervalSeries {
 
 	private static final long serialVersionUID = 7076927642023338578L;
 	private Connection con;
 	private String url;
 	private String driverName;
 	private String user;
 	private String password;
 	private String xAttribute;
 	private String yAttribute;
 	private String tableName;
 	private String constraint;
 	private Map<Integer,Integer> aggregationLevels;
 	ArrayList<Integer> odd = new ArrayList<Integer>();
 
 	protected int MAX_RESOLUTION = 100;
 
 	/**
 	 * Creates a new dataset (initially empty) and establishes a new database
 	 * connection.
 	 * 
 	 * @param key
 	 * @param url
 	 * @param driverName
 	 * @param user
 	 * @param password
 	 * @param xAttribute
 	 * @param yAttribute
 	 * @param tableName
 	 * @param constraint
 	 */
 	public JdbcYIntervalSeries(String key, String url, String driverName,
 			String user, String password, String xAttribute, String yAttribute,
 			String tableName, String constraint) {
 		super(key);
 		this.url = url;
 		this.driverName = driverName;
 		this.user = user;
 		this.password = password;
 		getConnection();
 		this.xAttribute = xAttribute;
 		this.yAttribute = yAttribute;
 		this.tableName = tableName;
 		this.constraint = constraint;
 		for (int i = 1; i <= 1000; i++) {
 			if (i % 2 == 0)
 				odd.add(i);
 		}
 		this.getConnection();
 	}
 
 	/**
 	 * return an existing connection. If the connection does not exists a new
 	 * connection is established.
 	 * 
 	 * @return connection object
 	 */
 	protected Connection getConnection() {
 		if (con == null)
 			try {
 				// Register the JDBC driver for MySQL.
 				Class.forName(driverName);
 				con = DriverManager.getConnection(url, user, password);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		return con;
 	}
 
 	/**
 	 * the range of the domain i.e. the x axis; this is the overall range and
 	 * not the range of the displayed data
 	 * 
 	 * @return range of the x axis
 	 */
 	public Range getDomainRange() {
 		long maximumItemCount = 0;
 		long minimumItemCount = 0;
 		Statement st;
 		try {
 			st = con.createStatement();
 			String query = "select min(`" + xAttribute + "`) as MIN ,max(`"
 					+ xAttribute + "`) as MAX from " + tableName;
 			if (constraint != null && !constraint.isEmpty())
 				query += " where " + constraint;
 			ResultSet rs = st.executeQuery(query);
 			rs.next();
 			minimumItemCount = rs.getLong("MIN");
 			maximumItemCount = rs.getLong("MAX");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return new Range(minimumItemCount, maximumItemCount);
 	}
 
 	/**
 	 * the range of the y axis; this is the overall range and not the range of
 	 * the displayed data
 	 * 
 	 * @return range of the y axis
 	 */
 	public Range getYRange() {
 		double minimumItemCount = 0;
 		double maximumItemCount = 0;
 		Statement st;
 		try {
 			st = con.createStatement();
 			String query = "select min(`" + yAttribute + "`) as MIN ,max(`"
 					+ yAttribute + "`) from " + tableName;
 			if (constraint != null && !constraint.isEmpty())
 				query += " where " + constraint;
 			ResultSet rs = st.executeQuery(query);
 			rs.next();
 			minimumItemCount = rs.getDouble("MIN");
 			maximumItemCount = rs.getDouble("MAX");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return new Range(minimumItemCount, maximumItemCount);
 	}
 	
 	/**
 	 * Setup the aggregation levels supplied by the array
 	 * @param levels
 	 */
 	public void setUpAggregation(Map<Integer,Integer> levels) {
 		aggregationLevels = levels;
 		for(int i : aggregationLevels.keySet()) {
 			setUpAggregationTable(i, false);
 		}
 	}
 	
 	/**
 	 * Setup a new aggregation level table
 	 * @param level the aggregation level
 	 */
 	public void setUpAggregationTable(int level, boolean refresh) {
 		if(level < 2) return; // Don't aggregate on levels < 2
 		try {
 			System.out.println("AGGREGATION: creation of level " + level + " started");
 			Statement st;
 			st = con.createStatement();
 			
 			// Drop the existing table
 			if(!aggregationTableExists(level) || refresh) {
 				String query = "DROP TABLE IF EXISTS dataset_ag_" + level;
 				st.execute(query);
 			
 				// Create a new table
 				query = "CREATE TABLE dataset_ag_" + level + " (" +
 						"id INT NOT NULL AUTO_INCREMENT PRIMARY KEY," +
 						"" + xAttribute + " BIGINT," +
 						"" + yAttribute + "_AVG DOUBLE," +
 						"" + yAttribute + "_MIN DOUBLE," +
 						"" + yAttribute + "_MAX DOUBLE" +
 						")";
 				st.execute(query);
 				
 				// Fill the new table
 				query = "INSERT INTO dataset_ag_" + level + " (" +
 						 	xAttribute + "," + 
 						 	yAttribute + "_AVG," + 
 						 	yAttribute + "_MIN," + 
 						 	yAttribute + "_MAX) " +
 						"SELECT AVG(" + xAttribute + "), " +
 							   "AVG(" + yAttribute + "), " +
 							   "MIN(" + yAttribute + "), " +
 							   "MAX(" + yAttribute + ") " +
 						"FROM " + tableName + " " +
 						"GROUP BY " + xAttribute + " div " + level;
 				st.execute(query);
 				System.out.println("AGGREGATION: creation of level " + level + " finished");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	
 	/**
 	 * Checks if a table for the specified aggregation level exists
 	 * @param level
 	 * @return
 	 */
 	public boolean aggregationTableExists(int level) {
 		boolean result = false;
 		try {
 			DatabaseMetaData dbm = con.getMetaData();
 			ResultSet tables;
 			tables = dbm.getTables(null, null, "dataset_ag_" + level, null);
 			if(tables.next()) result = true;
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		return result;
 	}
 	
 	/**
 	 * Return the best aggregation level
 	 * @param factor
 	 * @return
 	 */
	public String getAggregationTableName(long extend) {
 		int highestLevel = 1;
 		for(Map.Entry<Integer, Integer> entry : aggregationLevels.entrySet()) {
			if(highestLevel < entry.getKey() && extend > entry.getValue()) highestLevel = entry.getKey();
 		}
 		if(highestLevel > 1) return "dataset_ag_" + highestLevel;
 		return null;
 	}
 	
 	/**
 	 * Update the graph
 	 * @param start
 	 * @param extent
 	 */
 	public void update(long start, long extent) {
 		// Decide which table to use
		long factor = (long) Math.ceil(extent / MAX_RESOLUTION);
		String aggregationTable = this.getAggregationTableName(factor);
 		System.out.println("Using table: " + aggregationTable);
 
 		// Query the table
 		String query = "";
 		if(aggregationTable != null) {
 			// Aggregation is available, use that table
 			query = "SELECT " + xAttribute + " as timed," +
 						"AVG(" + yAttribute + "_AVG) as average," +
 						"MIN(" + yAttribute + "_MIN) as minimum," + 
 						"MAX(" + yAttribute + "_MAX) as maximum " +
 					"FROM " + aggregationTable + " " + 
 					"WHERE " + 
 						xAttribute + " >= " + (start - extent) + " AND " + 
 						xAttribute + " <= " + (start + 2 * extent) + " " +
 					"GROUP BY " +
 						xAttribute + " div " + factor;
 		} else {
 			query = "SELECT " + xAttribute + " as timed,  " +
 						"AVG(" + yAttribute + ") as average," +
 						"MIN(" + yAttribute + ") as minimum," + 
 						"MAX(" + yAttribute + ") as maximum " +
 					"FROM " + tableName + " " + 
 					"WHERE " + 
 						xAttribute + " >= " + (start - extent) + " AND " + 
 						xAttribute + " <= " + (start + 2 * extent) + " " +
 					"GROUP BY " +
 						xAttribute + " div " + factor;
 		}
 		
 		// Add the results to the graph
 		try {
 			Statement st = con.createStatement();
 			long starttime = System.currentTimeMillis();
 			ResultSet rs = st.executeQuery(query);
 			System.out.println("UPDATE (using " + (aggregationTable == null ? tableName : aggregationTable) + ")start, extent, factor, querytime: " + start + "," + extent + "," + factor + "," + (System.currentTimeMillis() - starttime));
 			while(rs.next()) {
 				Long timed = rs.getLong("timed");
 				Double average = rs.getDouble("average");
 				Double minimum = rs.getDouble("minimum");
 				Double maximum = rs.getDouble("maximum");
 				add(timed, average, minimum, maximum);
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 	}
 }
