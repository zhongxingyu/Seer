 package db;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.math.BigDecimal;
 import java.net.URL;
 import java.sql.Array;
 import java.sql.Blob;
 import java.sql.Clob;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.ParameterMetaData;
 import java.sql.PreparedStatement;
 import java.sql.Ref;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.SQLWarning;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.util.Calendar;
 import java.util.TreeMap;
 import java.util.Map;
 
 
 /**
 * A wrapper around <code>java.sql.PreparedStatement<code> class that can store the bind variables and can regenerate the SQL query.
 * @author sekhri
 */
 public class PreparedStatementWrapper implements PreparedStatement {
 
 	private PreparedStatement embedded;
 	private Connection conn;
 	private String sql;
 	private Map bindParams;
  
 	/**
 	* Constructs a PreparedStatementWrapper object that inherits from <code>java.sql.PreparedStatement</code>. This constructor initializes a private <code>java.sql.PreparedStatement, java.sql.Connection, java.util.TreeMap</code> that stores bind variables and <code>java.lang.String</code> that store the sql query.
 	*/
 	PreparedStatementWrapper(PreparedStatement ps, Connection c, String s) {
 		embedded = ps;
 		conn = c;
 		sql = s;
 		bindParams = new TreeMap();
 	}
 	
 	/**
 	 * A method that simply calls private method toString(String sql) with stored sql query in private variable sql.
 	 * @return a <code>java.lang.String</code>  that conatins the SQL query that is executed by the driver
 	 */
 	public String toString() {
 		return toString(sql);
 	}
 
 	/**
 	 * A method that convert the bind variable into a well formed printable query. The format of the printable query can be user defined and be changed.
 	 * @return a <code>java.lang.String</code>  that conatins the SQL query that is executed by the driver
 	 */
 	private String toString(String sql) {
 		String logStr = sql;
 		int i = 1;
 		while (logStr.indexOf('?') >= 0) {
                        Object obj = bindParams.get(new Integer(i++));
                        String value="";
                        if ( obj != null ) value = obj.toString();
			logStr = logStr.replaceFirst("\\?", "'" + value  + "'");
         	}
 		return logStr;
 		//System.out.println("QUERY is "+ logStr);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int executeUpdate() throws SQLException {
 		return embedded.executeUpdate();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void addBatch() throws SQLException {
 		embedded.addBatch();
 	}
 	
 	/**
 	* {@inheritDoc}
 	*/
 	public void clearParameters() throws SQLException {
 		embedded.clearParameters();
 		bindParams.clear();
 	}
 	
 	/**
 	* {@inheritDoc}
 	*/
 	public boolean execute() throws SQLException {
 		return embedded.execute();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setByte(int parameterIndex, byte x) throws SQLException {
 		embedded.setByte(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), new Byte(x));
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setDouble(int parameterIndex, double x) throws SQLException {
 		embedded.setDouble(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), new Double(x));
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setFloat(int parameterIndex, float x) throws SQLException {
 		embedded.setFloat(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), new Float(x));
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setInt(int parameterIndex, int x) throws SQLException {
 		embedded.setInt(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), new Integer(x));
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setNull(int parameterIndex, int sqlType) throws SQLException {
 		embedded.setNull(parameterIndex, sqlType);
 		bindParams.put(new Integer(parameterIndex), null);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setLong(int parameterIndex, long x) throws SQLException {
 		embedded.setLong(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), new Long(x));
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setShort(int parameterIndex, short x) throws SQLException {
 		embedded.setShort(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), new Short(x));
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setBoolean(int parameterIndex, boolean x) throws SQLException {
 		embedded.setBoolean(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), new Boolean(x));
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setBytes(int parameterIndex, byte[] x) throws SQLException {
 		embedded.setBytes(parameterIndex, x);
 		// Should this be:
 		// bindParams.put(new Integer(parameterIndex), Arrays.asList(x));
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
 		embedded.setAsciiStream(parameterIndex, x, length);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
 		embedded.setBinaryStream(parameterIndex, x, length);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
 		embedded.setUnicodeStream(parameterIndex, x, length);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
 		embedded.setCharacterStream(parameterIndex, reader, length);
 		bindParams.put(new Integer(parameterIndex), reader);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setObject(int parameterIndex, Object x) throws SQLException {
 		embedded.setObject(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
 		embedded.setObject(parameterIndex, x, targetSqlType);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setObject(int parameterIndex, Object x, int targetSqlType, int scale) throws SQLException {
 		embedded.setObject(parameterIndex, x, targetSqlType, scale);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setNull(int paramIndex, int sqlType, String typeName)
         throws SQLException {
 		embedded.setNull(paramIndex, sqlType, typeName);
 		bindParams.put(new Integer(paramIndex), null);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setString(int parameterIndex, String x) throws SQLException {
 		embedded.setString(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setBigDecimal(int parameterIndex, BigDecimal x)
         throws SQLException {
 		embedded.setBigDecimal(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setURL(int parameterIndex, URL x) throws SQLException {
 		embedded.setURL(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setArray(int i, Array x) throws SQLException {
 		embedded.setArray(i, x);
 		bindParams.put(new Integer(i), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setBlob(int i, Blob x) throws SQLException {
 		embedded.setBlob(i, x);
 		bindParams.put(new Integer(i), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setClob(int i, Clob x) throws SQLException {
 		embedded.setClob(i, x);
 		bindParams.put(new Integer(i), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setDate(int parameterIndex, Date x) throws SQLException {
 		embedded.setDate(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public ParameterMetaData getParameterMetaData() throws SQLException {
 		return embedded.getParameterMetaData();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setRef(int i, Ref x) throws SQLException {
 		embedded.setRef(i, x);
 		bindParams.put(new Integer(i), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public ResultSet executeQuery() throws SQLException {
 		return embedded.executeQuery();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public ResultSetMetaData getMetaData() throws SQLException {
 		return embedded.getMetaData();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setTime(int parameterIndex, Time x) throws SQLException {
 		embedded.setTime(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
 		embedded.setTimestamp(parameterIndex, x);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
 		embedded.setDate(parameterIndex, x, cal);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
 		embedded.setTime(parameterIndex, x, cal);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
 		embedded.setTimestamp(parameterIndex, x, cal);
 		bindParams.put(new Integer(parameterIndex), x);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int getFetchDirection() throws SQLException {
 		return embedded.getFetchDirection();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int getFetchSize() throws SQLException {
 		return embedded.getFetchSize();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int getMaxFieldSize() throws SQLException {
 		return embedded.getMaxFieldSize();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int getMaxRows() throws SQLException {
 		return embedded.getMaxRows();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int getQueryTimeout() throws SQLException {
 		return embedded.getQueryTimeout();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int getResultSetConcurrency() throws SQLException {
 		return embedded.getResultSetConcurrency();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int getResultSetHoldability() throws SQLException {
 		return embedded.getResultSetHoldability();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int getResultSetType() throws SQLException {
 		return embedded.getResultSetType();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int getUpdateCount() throws SQLException {
 		return embedded.getUpdateCount();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void cancel() throws SQLException {
 		embedded.cancel();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void clearBatch() throws SQLException {
 		embedded.clearBatch();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void clearWarnings() throws SQLException {
 		embedded.clearWarnings();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void close() throws SQLException {
 		embedded.close();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public boolean getMoreResults() throws SQLException {
 		return embedded.getMoreResults();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int[] executeBatch() throws SQLException {
 		return embedded.executeBatch();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setFetchDirection(int direction) throws SQLException {
 		embedded.setFetchDirection(direction);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setFetchSize(int rows) throws SQLException {
 		embedded.setFetchSize(rows);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setMaxFieldSize(int max) throws SQLException {
 		embedded.setMaxFieldSize(max);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setMaxRows(int max) throws SQLException {
 		embedded.setMaxRows(max);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setQueryTimeout(int seconds) throws SQLException {
 		embedded.setQueryTimeout(seconds);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public boolean getMoreResults(int current) throws SQLException {
 		return embedded.getMoreResults(current);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setEscapeProcessing(boolean enable) throws SQLException {
 		embedded.setEscapeProcessing(enable);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int executeUpdate(String sql) throws SQLException {
 		return embedded.executeUpdate(sql);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void addBatch(String sql) throws SQLException {
 		embedded.addBatch(sql);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public void setCursorName(String name) throws SQLException {
 		embedded.setCursorName(name);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public boolean execute(String sql) throws SQLException {
 		return embedded.execute(sql);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
 		return embedded.executeUpdate(sql, autoGeneratedKeys);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
 		return embedded.execute(sql, autoGeneratedKeys);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
 		return embedded.executeUpdate(sql, columnIndexes);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public boolean execute(String sql, int[] columnIndexes) throws SQLException {
 		return embedded.execute(sql, columnIndexes);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public Connection getConnection() throws SQLException {
 		return conn;
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public ResultSet getGeneratedKeys() throws SQLException {
 		return embedded.getGeneratedKeys();
 	}
 
 
 	/**
 	* {@inheritDoc}
 	*/
 	public ResultSet getResultSet() throws SQLException {
 		return embedded.getResultSet();
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public SQLWarning getWarnings() throws SQLException {
 		return embedded.getWarnings();
 	}
 
 
 	/**
 	* {@inheritDoc}
 	*/
 	public int executeUpdate(String sql, String[] columnNames) throws SQLException {
 		return embedded.executeUpdate(sql, columnNames);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public boolean execute(String sql, String[] columnNames)  throws SQLException {
 		return embedded.execute(sql, columnNames);
 	}
 
 	/**
 	* {@inheritDoc}
 	*/
 	public ResultSet executeQuery(String sql) throws SQLException {
 		return embedded.executeQuery(sql);
 	}
 	
 	/*protected void finalize() throws Exception {
 		ResultSet rs = this.getResultSet();
 		if (rs != null) {
 			rs.close();
 		}
 		this.close();
 		try {
 			super.finalize();
 		} catch (Exception e) {
 		}
 	}*/
 }
