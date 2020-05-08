 package es.udc.cartolab.com.hardcode.gdbms.driver.sqlite;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Date;
 
 import org.apache.log4j.Logger;
 
 import com.hardcode.gdbms.driver.exceptions.BadFieldDriverException;
 import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
 import com.hardcode.gdbms.engine.data.db.JDBCSupport;
 import com.hardcode.gdbms.engine.data.driver.ReadAccess;
 import com.hardcode.gdbms.engine.values.Value;
 import com.hardcode.gdbms.engine.values.ValueFactory;
 
 public class SQLiteJDBCSupport implements ReadAccess {
 
 	private static Logger logger = Logger
 			.getLogger(JDBCSupport.class.getName());
 
 	private ResultSet resultSet;
 	private int rowCount = -1;
 	private Connection conn = null;
 	private String sql = null;
	private int curIndex = -1;
 
 	/**
 	 * Creates a new JDBCSupport object.
 	 * 
 	 * @param r
 	 *            ResultSet that will be used to return the methods values
 	 */
 	protected SQLiteJDBCSupport(ResultSet r, Connection con, String sql) {
 		this.conn = con;
 		this.sql = sql;
 		resultSet = r;
 	}
 
 	/**
 	 * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldValue(long,
 	 *      int)
 	 */
 	public Value getFieldValue(long rowIndex, int fieldId)
 			throws ReadDriverException {
 		Value value = null;
 
 		try {
 			fieldId += 1;
			if (rowIndex < curIndex) {
 				resultSet = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
 						ResultSet.CONCUR_READ_ONLY).executeQuery(sql);
				curIndex = -1;
 			}
 			while ((curIndex < rowIndex) && resultSet.next()) {
 				curIndex++;
 			}
 
 			int type = resultSet.getMetaData().getColumnType(fieldId);
 
 			switch (type) {
 			case Types.BIGINT:
 				value = ValueFactory.createValue(resultSet.getLong(fieldId));
 
 				break;
 
 			case Types.BIT:
 			case Types.BOOLEAN:
 				value = ValueFactory.createValue(resultSet.getBoolean(fieldId));
 
 				break;
 
 			case Types.CHAR:
 			case Types.VARCHAR:
 			case Types.LONGVARCHAR:
 				String auxString = resultSet.getString(fieldId);
 				if (auxString != null) {
 					value = ValueFactory.createValue(auxString);
 				}
 
 				break;
 
 			case Types.DATE:
 				Date auxDate = resultSet.getDate(fieldId);
 				if (auxDate != null) {
 					value = ValueFactory.createValue(auxDate);
 				}
 
 				break;
 
 			case Types.DECIMAL:
 			case Types.NUMERIC:
 			case Types.FLOAT:
 			case Types.DOUBLE:
 				value = ValueFactory.createValue(resultSet.getDouble(fieldId));
 
 				break;
 
 			case Types.INTEGER:
 				value = ValueFactory.createValue(resultSet.getInt(fieldId));
 
 				break;
 
 			case Types.REAL:
 				value = ValueFactory.createValue(resultSet.getFloat(fieldId));
 
 				break;
 
 			case Types.SMALLINT:
 				value = ValueFactory.createValue(resultSet.getShort(fieldId));
 
 				break;
 
 			case Types.TINYINT:
 				value = ValueFactory.createValue(resultSet.getByte(fieldId));
 
 				break;
 
 			case Types.BINARY:
 			case Types.VARBINARY:
 			case Types.LONGVARBINARY:
 				byte[] auxByteArray = resultSet.getBytes(fieldId);
 				if (auxByteArray != null) {
 					value = ValueFactory.createValue(auxByteArray);
 				}
 
 				break;
 
 			case Types.TIMESTAMP:
 				try {
 					Timestamp auxTimeStamp = resultSet.getTimestamp(fieldId);
 					value = ValueFactory.createValue(auxTimeStamp);
 				} catch (SQLException e) {
 					value = ValueFactory.createValue(new Timestamp(0));
 				}
 
 				break;
 
 			case Types.TIME:
 				try {
 					Time auxTime = resultSet.getTime(fieldId);
 					value = ValueFactory.createValue(auxTime);
 				} catch (SQLException e) {
 					value = ValueFactory.createValue(new Time(0));
 				}
 
 				break;
 
 			default:
 				Object _obj = null;
 				try {
 					_obj = resultSet.getObject(fieldId);
 				} catch (Exception ex) {
 					logger.error("Error getting object: " + ex.getMessage());
 				}
 				if (_obj == null) {
 					value = ValueFactory.createValue("");
 				} else {
 					value = ValueFactory.createValue(_obj.toString());
 				}
 			}
 
 			if (resultSet.wasNull()) {
 				return ValueFactory.createNullValue();
 			} else {
 				return value;
 			}
 		} catch (SQLException e) {
 			throw new BadFieldDriverException("JDBC", e);
 		}
 	}
 
 	/**
 	 * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldCount()
 	 */
 	public int getFieldCount() throws ReadDriverException {
 		try {
 			return resultSet.getMetaData().getColumnCount();
 		} catch (SQLException e) {
 			try {
 				newJDBCSupport(conn, sql);
 				return resultSet.getMetaData().getColumnCount();
 			} catch (SQLException e1) {
 				throw new ReadDriverException("JDBC", e);
 			}
 		}
 	}
 
 	/**
 	 * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldName(int)
 	 */
 	public String getFieldName(int fieldId) throws ReadDriverException {
 		try {
 			return resultSet.getMetaData().getColumnName(fieldId + 1);
 		} catch (SQLException e) {
 			try {
 				newJDBCSupport(conn, sql);
 				return resultSet.getMetaData().getColumnName(fieldId + 1);
 			} catch (SQLException e1) {
 				throw new ReadDriverException("JDBC", e);
 			}
 		}
 	}
 
 	/**
 	 * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getRowCount()
 	 */
 	public long getRowCount() throws ReadDriverException {
 		try {
 			resultSet = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY,
 					ResultSet.CONCUR_READ_ONLY).executeQuery(sql);
 			if (rowCount == -1) {
 				int n = 0;
 				while (resultSet.next()) {
 					n++;
 				}
 				rowCount = n;
 				curIndex = rowCount - 1;
 			}
 			return rowCount;
 		} catch (SQLException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/**
 	 * @see com.hardcode.gdbms.engine.data.driver.ReadAccess#getFieldType(int)
 	 */
 	public int getFieldType(int i) throws ReadDriverException {
 		try {
 			return resultSet.getMetaData().getColumnType(i + 1);
 		} catch (SQLException e) {
 			try {
 				newJDBCSupport(conn, sql);
 				return resultSet.getMetaData().getColumnType(i + 1);
 			} catch (SQLException e1) {
 				throw new ReadDriverException("JDBC", e);
 			}
 		}
 	}
 
 	/**
 	 * Closes the internal data source
 	 * 
 	 * @throws SQLException
 	 *             if the operation fails
 	 */
 	public void close() throws SQLException {
 		resultSet.close();
 	}
 
 	/**
 	 * Creates a new JDBCSuuport object with the data retrieved from the
 	 * connection with the given sql
 	 * 
 	 * @param con
 	 *            Connection to the database
 	 * @param sql
 	 *            SQL defining the data to use
 	 * 
 	 * @return JDBCSupport
 	 * 
 	 * @throws SQLException
 	 *             If the data cannot be retrieved
 	 */
 	public static SQLiteJDBCSupport newJDBCSupport(Connection con, String sql)
 			throws SQLException {
 		Statement st = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
 				ResultSet.CONCUR_READ_ONLY);
 		ResultSet res = st.executeQuery(sql);
 
 		return new SQLiteJDBCSupport(res, con, sql);
 	}
 
 	/**
 	 * Executes a query with the 'con' connection
 	 * 
 	 * @param con
 	 *            connection
 	 * @param sql
 	 *            instruction to execute
 	 * 
 	 * @throws SQLException
 	 *             if execution fails
 	 */
 	public static void execute(Connection con, String sql) throws SQLException {
 		Statement st = con.createStatement(ResultSet.TYPE_FORWARD_ONLY,
 				ResultSet.CONCUR_READ_ONLY);
 		st.execute(sql);
 	}
 
 	/**
 	 * @return
 	 */
 	public ResultSet getResultSet() {
 		return resultSet;
 	}
 
 	public int getFieldWidth(int i) throws ReadDriverException {
 		int width;
 		try {
 			width = resultSet.getMetaData().getColumnDisplaySize(i + 1);
 		} catch (SQLException e) {
 			try {
 				newJDBCSupport(conn, sql);
 				width = resultSet.getMetaData().getColumnDisplaySize(i + 1);
 			} catch (SQLException e1) {
 				throw new ReadDriverException("JDBC", e);
 			}
 		}
 		if (width < 0)
 			return 255;
 		return width;
 
 	}
 
 }
