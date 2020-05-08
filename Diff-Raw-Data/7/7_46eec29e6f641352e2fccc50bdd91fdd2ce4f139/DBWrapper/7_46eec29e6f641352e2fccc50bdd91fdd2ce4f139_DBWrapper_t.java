 package simple.db.wrapper;
 
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.google.common.base.Preconditions;
 
 public class DBWrapper {
 
 	private static final Log LOG = LogFactory.getLog(DBWrapper.class);
 
 	private final DataSource dataSource;
 
 	public static final void registerDriver(String driverClassName) {
 		try {
 			Class.forName(driverClassName);
 		} catch (Exception e) {
 			throw new IllegalStateException("failed to init db drive class: "
 					+ driverClassName, e);
 		}
 	}
 
 	public static final int DEFAULT_QUERY_LIMIT = 1000;
 
 	private static final String MYSQL_DB_DRIVER = "com.mysql.jdbc.Driver";
 
 	private static final String PHOENIX_DB_DRIVER = "com.salesforce.phoenix.jdbc.PhoenixDriver";
 
 	/**
 	 * max time used to get a connection from data source
 	 * <p>
 	 * used to warn when connection pool is too busy
 	 * <p>
 	 * unit: millisecond
 	 */
 	private static final long MAX_TIME_GET_CONNECTION_ALLOWED = 100;
 
 	/**
 	 * register for mysql db driver class
 	 */
 	public static final void registerMySQLDriver() {
 		registerDriver(MYSQL_DB_DRIVER);
 	}
 
 	/**
 	 * register for phoenix db driver class
 	 */
 	public static final void registerPhoenixDriver() {
 		registerDriver(PHOENIX_DB_DRIVER);
 	}
 
 	private final String name;
 
 	private final String url;
 
 	private final String info;
 
 	public DBWrapper(String name, DataSource dataSource) {
 		this.name = Preconditions.checkNotNull(name);
 		this.dataSource = Preconditions.checkNotNull(dataSource);
 		this.url = this.getDatabaseUrl(dataSource);
 		this.info = String.format("name: %s, url: %s", this.name,
 		// if url is null, use DataSource.toString().
 		// hope the data source provider implement a descriptive toString method
 				(url == null ? this.dataSource : this.url));
 	}
 
 	public String getName() {
 		return this.name;
 	}
 
 	public String getUrl() {
 		return this.url;
 	}
 
 	private String getDatabaseUrl(DataSource ds) {
 		String url = null;
		Connection conn = null;
 		try {
			conn = ds.getConnection();
 			DatabaseMetaData metaData = conn.getMetaData();
 			url = metaData.getURL();
 		} catch (SQLException e) {
 			LOG.error("failed to get info from data source: " + ds, e);
		} finally {
			this.closeQuietly(conn, null, null);
 		}
 		return url;
 	}
 
 	public <T> Collection<T> queryList(DBQueryOperation<T> op)
 			throws SQLException {
 		return this.queryList(op, DEFAULT_QUERY_LIMIT);
 	}
 
 	/**
 	 * query db for result
 	 * 
 	 * @param op
 	 *            the query command
 	 * @param limit
 	 *            max record allowed, if exceed, SQLException will be throwed
 	 * @return
 	 * @throws SQLException
 	 */
 	public <T> Collection<T> queryList(DBQueryOperation<T> op, final int limit)
 			throws SQLException {
 		Connection conn = null;
 		PreparedStatement ps = null;
 		ResultSet rs = null;
 		Collection<T> result = null;
 		try {
 			conn = getDBConnection();
 			ps = conn.prepareStatement(op.getSql());
 			op.setParameter(ps);
 			rs = ps.executeQuery();
 			result = new LinkedList<T>();
 			int index = 0;
 			while (rs.next() && index++ < limit) {
 				result.add(op.parseResultSet(rs));
 			}
 			if (rs.next()) {
 				throw new QueryExceedLimitSQLException(
 						"result set too big, sql: " + op.getSql() + ", limit: "
 								+ limit);
 			}
 		} finally {
 			closeQuietly(conn, ps, rs);
 		}
 		return result;
 	}
 
 	/**
 	 * query single result
 	 * 
 	 * @param op
 	 * @return
 	 * @throws SQLException
 	 */
 	public <T> T queryUniq(DBQueryOperation<T> op) throws SQLException {
 		Collection<T> list = this.queryList(op, 1);
 		return list.isEmpty() ? null : list.iterator().next();
 	}
 
 	/**
 	 * update the db
 	 * 
 	 * @param op
 	 * @return
 	 * @throws SQLException
 	 */
 	public int update(DBUpdateOperation op) throws SQLException {
 		Connection conn = null;
 		PreparedStatement ps = null;
 		int result = 0;
 		try {
 			conn = getDBConnection();
 			ps = conn.prepareStatement(op.getSql());
 			op.setParameter(ps);
 			result = ps.executeUpdate();
 		} finally {
 			closeQuietly(conn, ps, null);
 		}
 		return result;
 	}
 
 	private void closeQuietly(Connection conn, PreparedStatement ps,
 			ResultSet rs) {
 		if (rs != null) {
 			try {
 				rs.close();
 			} catch (SQLException e) {
 			}
 		}
 		if (ps != null) {
 			try {
 				ps.close();
 			} catch (SQLException e) {
 			}
 		}
 		if (conn != null) {
 			try {
 				conn.close();
 			} catch (SQLException e) {
 			}
 		}
 	}
 
 	private Connection getDBConnection() throws SQLException {
 		long time = System.currentTimeMillis();
 		Connection conn = this.dataSource.getConnection();
 		time = System.currentTimeMillis() - time;
 		if (time > MAX_TIME_GET_CONNECTION_ALLOWED) {
 			LOG.warn("too slow to get connection, info : " + this.info
 					+ "time elapsed(ms): " + time);
 		}
 		return conn;
 	}
 }
