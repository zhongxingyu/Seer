 package de.tiq.beeswax.jdbc;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.math.BigDecimal;
 import java.net.URL;
 import java.sql.Array;
 import java.sql.Blob;
 import java.sql.Clob;
 import java.sql.Date;
 import java.sql.NClob;
 import java.sql.Ref;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.RowId;
 import java.sql.SQLException;
 import java.sql.SQLWarning;
 import java.sql.SQLXML;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.hadoop.hive.metastore.api.Schema;
 import org.apache.thrift.TException;
 
 import com.cloudera.beeswax.api.Query;
 import com.cloudera.beeswax.api.QueryHandle;
 import com.cloudera.beeswax.api.QueryNotFoundException;
 import com.cloudera.beeswax.api.Results;
 import com.cloudera.beeswax.api.ResultsMetadata;
 
 import de.tiq.jdbc.QueryExecutor;
 import de.tiq.jdbc.annotations.JdbcDriver;
 
 @JdbcDriver(name = "BeeswaxDriver", packageDefinition = "de.tiq.beeswax.jdbc", scheme = "beeswax")
 public class Executor extends QueryExecutor {
 
 	private static class DefaultResultSetMetaData implements ResultSetMetaData {
 		@Override
 		public <T> T unwrap(Class<T> arg0) throws SQLException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public boolean isWrapperFor(Class<?> arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.isWrapperFor");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isWritable(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.isWritable");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isSigned(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.isSigned");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isSearchable(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.isSearchable");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isReadOnly(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.isReadOnly");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public int isNullable(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.isNullable");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public boolean isDefinitelyWritable(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.isDefinitelyWritable");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isCurrency(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.isCurrency");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isCaseSensitive(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.isCaseSensitive");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isAutoIncrement(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.isAutoIncrement");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public String getTableName(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getTableName");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String getSchemaName(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getSchemaName");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getScale(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getScale");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public int getPrecision(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getPrecision");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public String getColumnTypeName(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getColumnTypeName");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getColumnType(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getColumnType");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public String getColumnName(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getColumnName");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String getColumnLabel(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getColumnLabel");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getColumnDisplaySize(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getColumnDisplaySize");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public int getColumnCount() throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getColumnCount");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public String getColumnClassName(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getColumnClassName");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String getCatalogName(int arg0) throws SQLException {
 			System.out.println("TIQ ResultSetMetaData.getCatalogName");
 			// TODO Auto-generated method stub
 			return null;
 		}
 	}
 
 	private static class DefaultResultSet implements ResultSet {
 		@Override
 		public <T> T unwrap(Class<T> iface) throws SQLException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public boolean isWrapperFor(Class<?> iface) throws SQLException {
 			System.out.println("TIQ ResultSet.isWrapperFor");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean wasNull() throws SQLException {
 			System.out.println("TIQ ResultSet.wasNull");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public void updateTimestamp(String columnLabel, Timestamp x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateTimestamp");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateTimestamp(int columnIndex, Timestamp x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateTimestamp");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateTime(String columnLabel, Time x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateTime");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateTime(int columnIndex, Time x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateTime");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateString(String columnLabel, String x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateString");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateString(int columnIndex, String x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateString");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateShort(String columnLabel, short x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateShort");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateShort(int columnIndex, short x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateShort");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateSQLXML(String columnLabel, SQLXML xmlObject)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateSQLXML");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateSQLXML(int columnIndex, SQLXML xmlObject)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateSQLXML");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateRowId(String columnLabel, RowId x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateRowId");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateRowId(int columnIndex, RowId x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateRowId");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateRow() throws SQLException {
 			System.out.println("TIQ ResultSet.updateRow");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateRef(String columnLabel, Ref x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateRef");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateRef(int columnIndex, Ref x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateRef");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateObject(String columnLabel, Object x, int scaleOrLength)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateObject");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateObject(int columnIndex, Object x, int scaleOrLength)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateObject");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateObject(String columnLabel, Object x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateObject");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateObject(int columnIndex, Object x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateObject");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNull(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.updateNull");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNull(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.updateNull");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNString(String columnLabel, String nString)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateNString");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNString(int columnIndex, String nString)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateNString");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNClob(String columnLabel, Reader reader, long length)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateNClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNClob(int columnIndex, Reader reader, long length)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateNClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNClob(String columnLabel, Reader reader)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateNClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNClob(int columnIndex, Reader reader)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateNClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNClob(String columnLabel, NClob nClob)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateNClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNClob(int columnIndex, NClob nClob)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateNClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNCharacterStream(String columnLabel, Reader reader,
 				long length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateNCharacterStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNCharacterStream(int columnIndex, Reader x,
 				long length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateNCharacterStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNCharacterStream(String columnLabel, Reader reader)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateNCharacterStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateNCharacterStream(int columnIndex, Reader x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateNCharacterStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateLong(String columnLabel, long x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateLong");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateLong(int columnIndex, long x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateLong");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateInt(String columnLabel, int x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateInt");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateInt(int columnIndex, int x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateInt");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateFloat(String columnLabel, float x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateFloat");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateFloat(int columnIndex, float x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateFloat");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateDouble(String columnLabel, double x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateDouble");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateDouble(int columnIndex, double x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateDouble");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateDate(String columnLabel, Date x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateDate");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateDate(int columnIndex, Date x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateDate");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateClob(String columnLabel, Reader reader, long length)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateClob(int columnIndex, Reader reader, long length)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateClob(String columnLabel, Reader reader)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateClob(int columnIndex, Reader reader)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateClob(String columnLabel, Clob x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateClob(int columnIndex, Clob x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateClob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateCharacterStream(String columnLabel, Reader reader,
 				long length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateCharacterStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateCharacterStream(int columnIndex, Reader x, long length)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateCharacterStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateCharacterStream(String columnLabel, Reader reader,
 				int length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateCharacterStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateCharacterStream(int columnIndex, Reader x, int length)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateCharacterStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateCharacterStream(String columnLabel, Reader reader)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateCharacterStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateCharacterStream(int columnIndex, Reader x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateCharacterStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBytes(String columnLabel, byte[] x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateBytes");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBytes(int columnIndex, byte[] x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateBytes");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateByte(String columnLabel, byte x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateByte");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateByte(int columnIndex, byte x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateByte");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBoolean(String columnLabel, boolean x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateBoolean");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBoolean(int columnIndex, boolean x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateBoolean");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBlob(String columnLabel, InputStream inputStream,
 				long length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateBlob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBlob(int columnIndex, InputStream inputStream,
 				long length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateBlob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBlob(String columnLabel, InputStream inputStream)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateBlob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBlob(int columnIndex, InputStream inputStream)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateBlob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBlob(String columnLabel, Blob x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateBlob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBlob(int columnIndex, Blob x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateBlob");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBinaryStream(String columnLabel, InputStream x,
 				long length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateBinaryStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBinaryStream(int columnIndex, InputStream x,
 				long length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateBinaryStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBinaryStream(String columnLabel, InputStream x,
 				int length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateBinaryStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBinaryStream(int columnIndex, InputStream x,
 				int length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateBinaryStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBinaryStream(String columnLabel, InputStream x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateBinaryStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBinaryStream(int columnIndex, InputStream x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateBinaryStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBigDecimal(String columnLabel, BigDecimal x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateBigDecimal");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateBigDecimal(int columnIndex, BigDecimal x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateBigDecimal");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateAsciiStream(String columnLabel, InputStream x,
 				long length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateAsciiStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateAsciiStream(int columnIndex, InputStream x,
 				long length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateAsciiStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateAsciiStream(String columnLabel, InputStream x,
 				int length) throws SQLException {
 			System.out.println("TIQ ResultSet.updateAsciiStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateAsciiStream(int columnIndex, InputStream x, int length)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateAsciiStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateAsciiStream(String columnLabel, InputStream x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateAsciiStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateAsciiStream(int columnIndex, InputStream x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateAsciiStream");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateArray(String columnLabel, Array x)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.updateArray");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void updateArray(int columnIndex, Array x) throws SQLException {
 			System.out.println("TIQ ResultSet.updateArray");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void setFetchSize(int rows) throws SQLException {
 			System.out.println("TIQ ResultSet.setFetchSize");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void setFetchDirection(int direction) throws SQLException {
 			System.out.println("TIQ ResultSet.setFetchDirection");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public boolean rowUpdated() throws SQLException {
 			System.out.println("TIQ ResultSet.rowUpdated");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean rowInserted() throws SQLException {
 			System.out.println("TIQ ResultSet.rowInserted");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean rowDeleted() throws SQLException {
 			System.out.println("TIQ ResultSet.rowDeleted");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean relative(int rows) throws SQLException {
 			System.out.println("TIQ ResultSet.relative");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public void refreshRow() throws SQLException {
 			System.out.println("TIQ ResultSet.refreshRow");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public boolean previous() throws SQLException {
 			System.out.println("TIQ ResultSet.previous");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean next() throws SQLException {
 			System.out.println("TIQ ResultSet.next");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public void moveToInsertRow() throws SQLException {
 			System.out.println("TIQ ResultSet.moveToInsertRow");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void moveToCurrentRow() throws SQLException {
 			System.out.println("TIQ ResultSet.moveToCurrentRow");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public boolean last() throws SQLException {
 			System.out.println("TIQ ResultSet.last");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isLast() throws SQLException {
 			System.out.println("TIQ ResultSet.isLast");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isFirst() throws SQLException {
 			System.out.println("TIQ ResultSet.isFirst");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isClosed() throws SQLException {
 			System.out.println("TIQ ResultSet.isClosed");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isBeforeFirst() throws SQLException {
 			System.out.println("TIQ ResultSet.isBeforeFirst");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean isAfterLast() throws SQLException {
 			System.out.println("TIQ ResultSet.isAfterLast");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public void insertRow() throws SQLException {
 			System.out.println("TIQ ResultSet.insertRow");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public SQLWarning getWarnings() throws SQLException {
 			System.out.println("TIQ ResultSet.getWarnings");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public InputStream getUnicodeStream(String columnLabel)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getUnicodeStream");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public InputStream getUnicodeStream(int columnIndex)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getUnicodeStream");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public URL getURL(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getURL");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public URL getURL(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getURL");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getType() throws SQLException {
 			System.out.println("TIQ ResultSet.getType");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public Timestamp getTimestamp(String columnLabel, Calendar cal)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getTimestamp");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Timestamp getTimestamp(int columnIndex, Calendar cal)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getTimestamp");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Timestamp getTimestamp(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getTimestamp");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Timestamp getTimestamp(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getTimestamp");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Time getTime(String columnLabel, Calendar cal)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getTime");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Time getTime(int columnIndex, Calendar cal) throws SQLException {
 			System.out.println("TIQ ResultSet.getTime");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Time getTime(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getTime");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Time getTime(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getTime");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String getString(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getString");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String getString(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getString");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public java.sql.Statement getStatement() throws SQLException {
 			System.out.println("TIQ ResultSet.getStatement");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public short getShort(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getShort");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public short getShort(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getShort");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public SQLXML getSQLXML(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getSQLXML");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public SQLXML getSQLXML(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getSQLXML");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public RowId getRowId(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getRowId");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public RowId getRowId(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getRowId");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getRow() throws SQLException {
 			System.out.println("TIQ ResultSet.getRow");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public Ref getRef(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getRef");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Ref getRef(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getRef");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Object getObject(String columnLabel, Map<String, Class<?>> map)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getObject");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Object getObject(int columnIndex, Map<String, Class<?>> map)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getObject");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Object getObject(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getObject");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Object getObject(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getObject");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String getNString(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getNString");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String getNString(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getNString");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public NClob getNClob(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getNClob");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public NClob getNClob(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getNClob");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Reader getNCharacterStream(String columnLabel)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getNCharacterStream");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Reader getNCharacterStream(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getNCharacterStream");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public ResultSetMetaData getMetaData() throws SQLException {
 			System.out.println("TIQ ResultSet.getMetaData");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public long getLong(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getLong");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public long getLong(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getLong");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public int getInt(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getInt");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public int getInt(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getInt");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public int getHoldability() throws SQLException {
 			System.out.println("TIQ ResultSet.getHoldability");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public float getFloat(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getFloat");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public float getFloat(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getFloat");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public int getFetchSize() throws SQLException {
 			System.out.println("TIQ ResultSet.getFetchSize");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public int getFetchDirection() throws SQLException {
 			System.out.println("TIQ ResultSet.getFetchDirection");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public double getDouble(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getDouble");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public double getDouble(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getDouble");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public Date getDate(String columnLabel, Calendar cal)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getDate");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Date getDate(int columnIndex, Calendar cal) throws SQLException {
 			System.out.println("TIQ ResultSet.getDate");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Date getDate(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getDate");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Date getDate(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getDate");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public String getCursorName() throws SQLException {
 			System.out.println("TIQ ResultSet.getCursorName");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public int getConcurrency() throws SQLException {
 			System.out.println("TIQ ResultSet.getConcurrency");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public Clob getClob(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getClob");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Clob getClob(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getClob");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Reader getCharacterStream(String columnLabel)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getCharacterStream");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Reader getCharacterStream(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getCharacterStream");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public byte[] getBytes(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getBytes");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public byte[] getBytes(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getBytes");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public byte getByte(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getByte");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public byte getByte(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getByte");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public boolean getBoolean(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getBoolean");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public boolean getBoolean(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getBoolean");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public Blob getBlob(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getBlob");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Blob getBlob(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getBlob");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public InputStream getBinaryStream(String columnLabel)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getBinaryStream");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public InputStream getBinaryStream(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getBinaryStream");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public BigDecimal getBigDecimal(String columnLabel, int scale)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getBigDecimal");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public BigDecimal getBigDecimal(int columnIndex, int scale)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getBigDecimal");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getBigDecimal");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getBigDecimal");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public InputStream getAsciiStream(String columnLabel)
 				throws SQLException {
 			System.out.println("TIQ ResultSet.getAsciiStream");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public InputStream getAsciiStream(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getAsciiStream");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Array getArray(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.getArray");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public Array getArray(int columnIndex) throws SQLException {
 			System.out.println("TIQ ResultSet.getArray");
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		@Override
 		public boolean first() throws SQLException {
 			System.out.println("TIQ ResultSet.first");
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		@Override
 		public int findColumn(String columnLabel) throws SQLException {
 			System.out.println("TIQ ResultSet.findColumn");
 			// TODO Auto-generated method stub
 			return 0;
 		}
 
 		@Override
 		public void deleteRow() throws SQLException {
 			System.out.println("TIQ ResultSet.deleteRow");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void close() throws SQLException {
 			System.out.println("TIQ ResultSet.close");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void clearWarnings() throws SQLException {
 			System.out.println("TIQ ResultSet.clearWarnings");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void cancelRowUpdates() throws SQLException {
 			System.out.println("TIQ ResultSet.cancelRowUpdates");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void beforeFirst() throws SQLException {
 			System.out.println("TIQ ResultSet.beforeFirst");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public void afterLast() throws SQLException {
 			System.out.println("TIQ ResultSet.afterLast");
 			// TODO Auto-generated method stub
 
 		}
 
 		@Override
 		public boolean absolute(int row) throws SQLException {
 			System.out.println("TIQ ResultSet.absolute");
 			// TODO Auto-generated method stub
 			return false;
 		}

		@Override
		public <T> T getObject(int columnIndex, Class<T> type)
				throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <T> T getObject(String columnLabel, Class<T> type)
				throws SQLException {
			// TODO Auto-generated method stub
			return null;
		}
 	}
 
 	private BeeswaxConnectionHandler handle;
 	private ResultSet resultSet;
 
 	public Executor(BeeswaxConnectionHandler handle) {
 		super(handle);
 		this.handle = handle;
 	}
 
 	@Override
 	public ResultSet executeQuery(final String sql) throws SQLException {
 		execute(sql);
 		return getResultSet();
 	}
 	
 	@Override
 	public boolean execute(String sql) throws SQLException {
 		Query query = new Query().setQuery(sql);
 		try {
 			QueryHandle queryHandle = handle.getClient().query(query);
 			resultSet = getResultSet(queryHandle);
 			return resultSet != null;
 		} catch (Exception e) {
 			throw new SQLException(e);
 		}
 	}
 	
 	@Override
 	public ResultSet getResultSet() throws SQLException {
 		return resultSet;
 	}
 
 	@Override
 	public int executeUpdate(String sql) throws SQLException {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	
 	private ResultSet getResultSet(final QueryHandle queryHandle) {
 		return new DefaultResultSet() {
 			
 			private List<String> data = Collections.emptyList();
 			private String row;
 			private String field;
 			private boolean closed = false;
 			
 			@Override
 			public boolean next() throws SQLException {
 				if (closed)
 					return false;
 				
 				if (data.isEmpty()) {
 					try {
 						Results results = handle.getClient().fetch(
 								queryHandle, false, -1);
 						data = results.getData();
 						if (data.isEmpty()) {
 							handle.getClient().close(queryHandle);
 							closed = true;
 						}
 					} catch (Exception e) {
 						throw new SQLException(e);
 					}
 				}
 				
 				if (data.isEmpty()) {
 					row = null;
 				} else {
 					row = data.remove(0);
 				}
 				return row != null;
 			}
 			
 			private String getField(int columnIndex) throws SQLException {
 				String[] split = row.split("\t");
 				if (columnIndex > split.length) {
 					field = null;
 				} else {
 					field = split[columnIndex - 1];
 				}
 				return field;
 			}
 
 			@Override
 			public String getString(int columnIndex) throws SQLException {
 				return getField(columnIndex);
 			}
 			
 			@Override
 			public long getLong(int columnIndex) throws SQLException {
 				return Long.valueOf(getField(columnIndex));
 			}
 			
 			@Override
 			public double getDouble(int columnIndex) throws SQLException {
 				return Double.valueOf(getField(columnIndex));
 			}
 			
 			@Override
 			public Object getObject(int columnIndex) throws SQLException {
 				return getField(columnIndex);
 			}
 			
 			@Override
 			public boolean wasNull() throws SQLException {
 				return field == null || field.trim().length() == 0;
 			}
 			
 			@Override
 			public ResultSetMetaData getMetaData() throws SQLException {
 				return new DefaultResultSetMetaData() {
 					private Schema schema;
 					
 					@Override
 					public int getColumnCount() throws SQLException {
 						try {
 							fetchSchemaIfNeeded(queryHandle);
 							return schema.getFieldSchemasSize();
 						} catch (QueryNotFoundException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (TException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 						return 0;
 					}
 					
 					@Override
 					public String getColumnName(int arg0)
 							throws SQLException {
 						try {
 							fetchSchemaIfNeeded(queryHandle);
 							return schema.getFieldSchemas().get(arg0 - 1)
 									.getName();
 						} catch (QueryNotFoundException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (TException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						
 						return null;
 					}
 					
 					@Override
 					public int getColumnType(int arg0) throws SQLException {
 						String type = getColumnTypeName(arg0);
 						
 						// TODO
 						if ("string".equals(type)) {
 							return Types.VARCHAR;
 						}
 						if ("bigint".equals(type)) {
 							return Types.BIGINT;
 						}
 						System.out.println("TIQ ResultSetMetaData#getColumnType unknown column type: " + type);
 						return Types.JAVA_OBJECT;
 					}
 					
 					@Override
 					public String getColumnTypeName(int arg0)
 							throws SQLException {
 						try {
 							fetchSchemaIfNeeded(queryHandle);
 							return schema.getFieldSchemas()
 									.get(arg0 - 1).getType();
 						} catch (QueryNotFoundException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						} catch (TException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 						// TODO Auto-generated method stub
 						return null;
 					}
 					
 					private void fetchSchemaIfNeeded(QueryHandle queryHandle)
 							throws QueryNotFoundException, TException {
 						if (schema == null) {
 							ResultsMetadata metadata = handle.getClient()
 									.get_results_metadata(queryHandle);
 							schema = metadata.getSchema();
 						}
 					}
 				};
 			}
 		};
 	}
 }
