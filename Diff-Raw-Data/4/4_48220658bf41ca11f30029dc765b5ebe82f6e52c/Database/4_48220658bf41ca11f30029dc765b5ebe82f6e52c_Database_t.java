 
 
 
 package overwatch.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 
 
 
 
 /**
  * Provides basic global database functions.
  * 
  * @author Lee Coakley
  * @version 3
  */
 
 
 
 
 
 public class Database
 {
 	private static final ConnectionPool connPool = new ConnectionPool( 6, true );
 	
 	
 	
 	
 	
 	/**
 	 * Start making connections to the database.
 	 * For the moment, this is automatic and doesn't need to be used.
 	 */
 	public static void connect() {
 		connPool.start();
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Disconnect from the database.
 	 * Closes all connections and cleans up resources.
 	 */
 	public static void disconnect() {
 		connPool.stop();
 	}
 	
 	
 		
 	
 	
 	/**
 	 * Check if the pool has an unused connection ready.
 	 * Informational - 
 	 * @return status
 	 * @see ConnectionPool
 	 */
 	public static boolean hasConnection() {
 		return connPool.getConnectionCount() > 0;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Get a database connection from the pool.
 	 * Return it when finished!
 	 * @return Connection
 	 * @see ConnectionPool
 	 */
 	public static Connection getConnection() {
 		return connPool.getConnection();
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Return a database connection to the pool so it can be reused.
 	 * @param conn
 	 * @see ConnectionPool
 	 */
 	public static void returnConnection( Connection conn ) {
 		connPool.returnConnection( conn );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Run an SQL query that yields a single set of integers.
 	 * @param sql
 	 * @return Integer[]
 	 */
 	public static Integer[] queryInts( String sql ) {
 		return query(sql).getColumnAs( 0, Integer[].class );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Run a query, get an EnhancedResultSet.
 	 * Handles cleanup and conversion automatically.
 	 * @param ps
 	 * @return EnhancedResultSet
 	 * @throws SQLException
 	 */
 	public static EnhancedResultSet query( PreparedStatement ps )
 	{
 		EnhancedResultSet ers = null;
 		
 		try {
 	    	ResultSet rs = ps.executeQuery();
 	    		ers = new EnhancedResultSet( rs );
 	    	rs.close();
 		}
 		catch( SQLException ex) {
 			throw new RuntimeException( ex );
 		}
     	
     	return ers;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Run a query, get an EnhancedResultSet.
 	 * Handles cleanup and conversion automatically.
 	 * @param sql
 	 * @return EnhancedResultSet
 	 */
 	public static EnhancedResultSet query( String sql )
 	{
 		Connection conn = getConnection();
 		
 			EnhancedResultSet ers = null;
 		
 			try {
 				Statement st = conn.createStatement();
					ResultSet rs = st.executeQuery( sql );
		    			ers = new EnhancedResultSet( rs );
		    		rs.close();
 				st.close();
 			}
 			catch (SQLException ex) {
 				throw new RuntimeException( ex );
 			}
 		
 		returnConnection( conn );
     	
     	return ers;
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Dump the contents of an entire table into an EnhancedResultSet.
 	 * @param tableName
 	 * @return EnhancedResultSet
 	 */
 	public EnhancedResultSet dumpTable( String tableName ) {
 	    return query( "SELECT * FROM " + tableName + ";" );
 	}
 	
 	
 	
 	
 	
 	/**
 	 * Run update/insert/delete SQL and get back the number of rows modified.
 	 * @param sql
 	 * @return number rows modified
 	 */
 	public static int update( String sql )
 	{
 		int rowsModified = -1;
 		
 		Connection conn = getConnection();
 	
 			try {
 				Statement st = conn.createStatement();
 				rowsModified = st.executeUpdate( sql );
 				st.close();
 			}
 			catch (SQLException ex) {
 				throw new RuntimeException( ex );
 			}
 		
 		returnConnection( conn );
 		
 		return rowsModified; 
 	}
 	
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
