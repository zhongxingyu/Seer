 package uk.co.brotherlogic.mdb;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 /**
  * Class to deal with database connection
  * 
  * @author Simon Tucker
  */
 public final class Connect {
 	/** enum of modes */
 	private enum mode {
 		DEVELOPMENT, PRODUCTION
 	}
 
 	/** Current mode of operation */
 	private static mode operationMode = mode.DEVELOPMENT;
 
 	private static Connect singleton;
 
 	/**
 	 * Static constructor
 	 * 
 	 * @return A suitable db connection
 	 * @throws SQLException
 	 *             if a db connection cannot be established
 	 */
 	public static Connect getConnection() throws SQLException {
 		if (singleton == null)
 			singleton = new Connect(operationMode);
 		return singleton;
 	}
 
 	public static void setForProduction() {
 		operationMode = mode.PRODUCTION;
 	}
 
 	/** The connection to the local DB */
 	private Connection locDB;
 
 	int sCount = 0;
 
 	long longestQueryTime = 0;
 
 	String longestQuery = "";
 
 	private Connect(mode operationMode) throws SQLException {
 		makeConnection(operationMode);
 	}
 
 	/**
 	 * Cancels all impending transactions
 	 * 
 	 * @throws SQLException
 	 *             if the cancel fails
 	 */
 	public void cancelTrans() throws SQLException {
 		locDB.rollback();
 	}
 
 	/**
 	 * Commits the impending transactions
 	 * 
 	 * @throws SQLException
 	 *             If the commit fails
 	 */
 	public void commitTrans() throws SQLException {
 		locDB.commit();
 	}
 
 	public ResultSet executeQuery(PreparedStatement ps) throws SQLException {
 
 		System.err.println("RUN: " + ps);
 
 		long sTime = System.currentTimeMillis();
 		ResultSet rs = ps.executeQuery();
 		long eTime = System.currentTimeMillis() - sTime;
 		if (eTime > longestQueryTime) {
 			longestQueryTime = eTime;
 			longestQuery = ps.toString();
 		}
 		return rs;
 	}
 
 	/**
 	 * Builds a prepared statements from the data store
 	 * 
 	 * @param sql
 	 *            The statement to build
 	 * @return a {@link PreparedStatement}
 	 * @throws SQLException
 	 *             If the construction fails
 	 */
 	public PreparedStatement getPreparedStatement(final String sql)
 			throws SQLException {
 		// Create the statement
 		PreparedStatement ps = locDB.prepareStatement(sql);
 		sCount++;
		System.err.println(sql);
 		return ps;
 	}
 
 	public int getSCount() {
 		return sCount;
 	}
 
 	/**
 	 * Makes the connection to the DB
 	 * 
 	 * @throws SQLException
 	 *             if something fails
 	 */
 	private void makeConnection(mode operationMode) throws SQLException {
 		try {
 			// Load all the drivers and initialise the database connection
 			Class.forName("org.postgresql.Driver");
 
 			if (operationMode == mode.PRODUCTION) {
 				System.err.println("Connecting to production database");
 				locDB = DriverManager
 						.getConnection("jdbc:postgresql://192.168.1.100/music?user=music");
 			} else {
 				System.err.println("Connection to development database");
 				locDB = DriverManager
 						.getConnection("jdbc:postgresql://localhost/music?user=music");
 			}
 
 			// Switch off auto commit
 			locDB.setAutoCommit(false);
 		} catch (ClassNotFoundException e) {
 			throw new SQLException(e);
 		}
 	}
 
 	public void printStats() {
 		System.out.println(longestQueryTime + " => " + longestQuery);
 	}
 }
