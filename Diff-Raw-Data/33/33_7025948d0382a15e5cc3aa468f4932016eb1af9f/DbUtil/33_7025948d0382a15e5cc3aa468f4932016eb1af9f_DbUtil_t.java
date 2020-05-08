 /*
  * $Id: DbUtil.java 1566 2009-11-01 16:11:16Z amandel $
  *
  * Copyright 2006, The jCoderZ.org Project. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are
  * met:
  *
  *    * Redistributions of source code must retain the above copyright
  *      notice, this list of conditions and the following disclaimer.
  *    * Redistributions in binary form must reproduce the above
  *      copyright notice, this list of conditions and the following
  *      disclaimer in the documentation and/or other materials
  *      provided with the distribution.
  *    * Neither the name of the jCoderZ.org Project nor the names of
  *      its contributors may be used to endorse or promote products
  *      derived from this software without specific prior written
  *      permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE REGENTS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
  * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS AND CONTRIBUTORS
  * BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
  * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
  * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.jcoderz.commons.util;
 
 import java.io.InputStream;
 import java.io.Reader;
 import java.io.StringReader;
 import java.math.BigDecimal;
 import java.net.URL;
 import java.sql.Array;
 import java.sql.Blob;
 import java.sql.Clob;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.ParameterMetaData;
 import java.sql.PreparedStatement;
 import java.sql.Ref;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.SQLWarning;
 import java.sql.Statement;
 import java.sql.Time;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Utility class for some DB routines.
  *
  * @author Albrecht Messner
  */
 public final class DbUtil
 {
    /** class name for use in logger */
    private static final String CLASSNAME = DbUtil.class.getName();
 
    /** logging facility */
    private static final Logger logger = Logger.getLogger(CLASSNAME);
 
    /** The oracle driver class for Oracle 8.x and 9.x */
    private static final String ORACLE_DRIVER_CLASSNAME
          = "oracle.jdbc.OracleDriver";
    /**
     * private constructor to avoid instantiation.
     */
    private DbUtil ()
    {
       // utility class -- no instances are allowed.
    }
 
    /**
     * Close a statement according to resource handling idiom.
     * @param stmt the statement to be closed
     */
    public static void close (Statement stmt)
    {
       if (stmt != null)
       {
          try
          {
             stmt.close();
          }
          catch (SQLException x)
          {
             logger.log(Level.FINE, "Error during resource cleanup: "
                   + "java.sql.Statement.close()", x);
          }
       }
    }
 
    /**
     * Close a DB connection according to resource handling idiom.
     * @param con the connection to be closed
     */
    public static void close (Connection con)
    {
       if (con != null)
       {
          try
          {
             con.close();
          }
          catch (SQLException x)
          {
             logger.log(Level.FINE, "Error during resource cleanup: "
                   + "java.sql.Connection.close()", x);
          }
       }
    }
 
    /**
     * Close a result set according to resource handling idiom.
     * @param resultSet the result set to close
     */
    public static void close (ResultSet resultSet)
    {
       if (resultSet != null)
       {
          try
          {
             resultSet.close();
          }
          catch (SQLException x)
          {
             logger.log(Level.FINE, "Error during resource cleanup: "
                   + "java.sql.ResultSet.close()", x);
          }
       }
    }
 
    /**
     * Sets a parameter in a PreparedStatement to either a value or null.
     *
     * @param pstmt the prepared statement
     * @param paramIndex the parameter index of the parameter to be set
     * @param value the value to which the parameter must be set
     * @throws SQLException if a database access error occurs.
     */
    public static void setVarcharOrNull (
          PreparedStatement pstmt, int paramIndex, String value)
          throws SQLException
    {
       setParameterOrNull(pstmt, paramIndex, value, Types.VARCHAR);
    }
 
    /**
     * Sets a parameter in a PreparedStatement to either a value or null.
     *
     * @param pstmt the prepared statement
     * @param paramIndex the parameter index of the parameter to be set
     * @param value the value to which the parameter must be set
     * @throws SQLException if a database access error occurs.
     */
    public static void setCharOrNull (
          PreparedStatement pstmt, int paramIndex, String value)
          throws SQLException
    {
       setParameterOrNull(pstmt, paramIndex, value, Types.CHAR);
    }
 
    /**
     * Sets a parameter in a PreparedStatement to either a value or null.
     *
     * @param pstmt the prepared statement
     * @param paramIndex the parameter index of the parameter to be set
     * @param value the value to which the parameter must be set
     * @throws SQLException if a database access error occurs.
     */
    public static void setClobOrNull (
          PreparedStatement pstmt, int paramIndex, String value)
          throws SQLException
    {
       setParameterOrNull(pstmt, paramIndex, value, Types.CLOB);
    }
 
    /**
     * Sets a parameter in a PreparedStatement to either a value or null.
     *
     * @param pstmt the prepared statement
     * @param paramIndex the parameter index of the parameter to be set
     * @param value the value to which the parameter must be set
     * @throws SQLException if a database access error occurs.
     */
    public static void setTimestampOrNull (
          PreparedStatement pstmt, int paramIndex, Timestamp value)
          throws SQLException
    {
       setParameterOrNull(pstmt, paramIndex, value, Types.TIMESTAMP);
    }
 
    /**
     * Sets a parameter in a PreparedStatement to either a value or null.
     *
     * @param pstmt the prepared statement
     * @param paramIndex the parameter index of the parameter to be set
     * @param value the value to which the parameter must be set
     * @throws SQLException if a database access error occurs.
     */
    public static void setIntegerOrNull (
          PreparedStatement pstmt, int paramIndex, Integer value)
          throws SQLException
    {
       setParameterOrNull(pstmt, paramIndex, value, Types.INTEGER);
    }
 
    /**
     * Sets a parameter in a PreparedStatement to either a value or null.
     *
     * @param pstmt the prepared statement
     * @param paramIndex the parameter index of the parameter to be set
     * @param value the value to which the parameter must be set
     * @throws SQLException if a database access error occurs.
     */
    public static void setLongOrNull (
          PreparedStatement pstmt, int paramIndex, Long value)
          throws SQLException
    {
       // Sorry, can't delegate this one to setParameterOrNull because
       // it's got a different type than setIntegerOrNull, but the same
       // SQL type code.
       if (value != null)
       {
          pstmt.setLong(paramIndex, value.longValue());
       }
       else
       {
          pstmt.setNull(paramIndex, Types.INTEGER);
       }
    }
 
    /**
     * Registers the oracle JDBC driver.
     *
     * NOTE: only use either in testcases that need a direct JDBC connection
     * or in stand-alone applications, never in the Application Server.
     */
    public static void registerOracleDriver ()
    {
       try
       {
          final Class<?> driverClass
                = Class.forName(ORACLE_DRIVER_CLASSNAME);
          final Driver driverObject = (Driver) driverClass.newInstance();
          DriverManager.registerDriver(driverObject);
       }
       catch (Exception e)
       {
          final RuntimeException rte = new RuntimeException(
                "Failed to register oracle driver "
                + ORACLE_DRIVER_CLASSNAME, e);
          throw rte;
       }
    }
 
    /**
     * This method wraps the {@link PreparedStatement} given as argument
     * into a delegate object that calls the <code>executeBatch</code>
     * method every <code>maxBatchSize</code>th time a batch is added to
     * the statement with the <code>addBatch</code> method.
     *
     * @param pstmt the prepared statement to wrap
     * @param maxBatchSize the maximum batch size at which an executeBatch
     *       should be called
     * @return a PreparedStatement implementation that does automatic
     *       batch updates
     */
    public static PreparedStatement getLimitedBatchSizePreparedStatement (
          PreparedStatement pstmt, int maxBatchSize)
    {
       return null; // new LimitedBatchSizePreparedStatement(pstmt, maxBatchSize);
    }
 
    private static void setParameterOrNull (
          PreparedStatement pstmt, int paramIndex, Object value, int type)
          throws SQLException
    {
       if (value != null)
       {
          switch (type)
          {
             case Types.VARCHAR:
             case Types.CHAR:
                pstmt.setString(paramIndex, (String) value);
                break;
             case Types.CLOB:
                final String strVal = (String) value;
                pstmt.setCharacterStream(
                      paramIndex, new StringReader(strVal), strVal.length());
                break;
             case Types.TIMESTAMP:
                final Timestamp tstampVal = (Timestamp) value;
                pstmt.setTimestamp(paramIndex, tstampVal);
                break;
             case Types.INTEGER:
                final Integer intVal = (Integer) value;
                pstmt.setInt(paramIndex, intVal.intValue());
                break;
             default:
                throw new RuntimeException("Unexpected SQL type "
                      + type + " encountered");
          }
       }
       else
       {
          pstmt.setNull(paramIndex, type);
       }
    }
 
   @SuppressWarnings("unused")
    private static final class LimitedBatchSizePreparedStatement
 // FIXME: does not work with JDK1.6.0     implements PreparedStatement
    {
       private final PreparedStatement mPreparedStatement;
       private final int mMaxBatchSize;
       private final List mResultList = new ArrayList();
 
       private int mBatchCount = 0;
 
       private LimitedBatchSizePreparedStatement
             (PreparedStatement pstmt, int maxBatchSize)
       {
          mPreparedStatement = pstmt;
          mMaxBatchSize = maxBatchSize;
       }
 
       /** {@inheritDoc} */
       public void addBatch ()
             throws SQLException
       {
          if (mBatchCount >= mMaxBatchSize)
          {
             internalExecuteBatch();
          }
          mPreparedStatement.addBatch();
          mBatchCount++;
       }
 
       /** {@inheritDoc} */
       public void addBatch (String sql)
             throws SQLException
       {
          if (mBatchCount >= mMaxBatchSize)
          {
             internalExecuteBatch();
          }
          mPreparedStatement.addBatch(sql);
          mBatchCount++;
       }
 
       /** {@inheritDoc} */
       public int[] executeBatch ()
             throws SQLException
       {
          internalExecuteBatch();
 
          int totalBatchSize = 0;
         for (final Iterator<int[]> it = mResultList.iterator(); it.hasNext(); )
          {
             final int[] updateResult = (int[]) it.next();
             totalBatchSize += updateResult.length;
          }
 
          final int[] result = new int[totalBatchSize];
          int offset = 0;
         for (final Iterator<int[]> it = mResultList.iterator(); it.hasNext(); )
          {
             final int[] updateResult = (int[]) it.next();
             System.arraycopy(
                   updateResult, 0, result, offset, updateResult.length);
             offset += updateResult.length;
          }
          mResultList.clear();
          return result;
       }
 
       /** {@inheritDoc} */
       public void clearBatch ()
             throws SQLException
       {
          mPreparedStatement.clearBatch();
          mBatchCount = 0;
          mResultList.clear();
       }
 
       /** {@inheritDoc} */
       public String toString ()
       {
          return "[LimitedBatchSizePreparedStatement: mBatchSize=" 
                + mMaxBatchSize + ", mPreparedStatement=" 
                + mPreparedStatement + "]";
       }
 
       private void internalExecuteBatch ()
             throws SQLException
       {
          mBatchCount = 0;
          final int[] result = mPreparedStatement.executeBatch();
          mResultList.add(result.clone());
       }
 
       // ==============================================================
       // only delegates below this point
       // ==============================================================
 
       /** {@inheritDoc} */
       public void cancel ()
             throws SQLException
       {
          mPreparedStatement.cancel();
       }
 
       /** {@inheritDoc} */
       public void clearParameters ()
             throws SQLException
       {
          mPreparedStatement.clearParameters();
       }
 
       /** {@inheritDoc} */
       public void clearWarnings ()
             throws SQLException
       {
          mPreparedStatement.clearWarnings();
       }
 
       /** {@inheritDoc} */
       public void close ()
             throws SQLException
       {
          mPreparedStatement.close();
       }
 
       /** {@inheritDoc} */
       public boolean execute ()
             throws SQLException
       {
          return mPreparedStatement.execute();
       }
 
       /** {@inheritDoc} */
       public boolean execute (String sql)
             throws SQLException
       {
          return mPreparedStatement.execute(sql);
       }
 
       /** {@inheritDoc} */
       public boolean execute (String sql, int autoGeneratedKeys)
             throws SQLException
       {
          return mPreparedStatement.execute(sql, autoGeneratedKeys);
       }
 
       /** {@inheritDoc} */
       public boolean execute (String sql, int[] columnIndexes)
             throws SQLException
       {
          return mPreparedStatement.execute(sql, columnIndexes);
       }
 
       /** {@inheritDoc} */
       public boolean execute (String sql, String[] columnNames)
             throws SQLException
       {
          return mPreparedStatement.execute(sql, columnNames);
       }
 
       /** {@inheritDoc} */
       public ResultSet executeQuery ()
             throws SQLException
       {
          return mPreparedStatement.executeQuery();
       }
 
       /** {@inheritDoc} */
       public ResultSet executeQuery (String sql)
             throws SQLException
       {
          return mPreparedStatement.executeQuery(sql);
       }
 
       /** {@inheritDoc} */
       public int executeUpdate ()
             throws SQLException
       {
          return mPreparedStatement.executeUpdate();
       }
 
       /** {@inheritDoc} */
       public int executeUpdate (String sql)
             throws SQLException
       {
          return mPreparedStatement.executeUpdate(sql);
       }
 
       /** {@inheritDoc} */
       public int executeUpdate (String sql, int autoGeneratedKeys)
             throws SQLException
       {
          return mPreparedStatement.executeUpdate(sql, autoGeneratedKeys);
       }
 
       /** {@inheritDoc} */
       public int executeUpdate (String sql, int[] columnIndexes)
             throws SQLException
       {
          return mPreparedStatement.executeUpdate(sql, columnIndexes);
       }
 
       /** {@inheritDoc} */
       public int executeUpdate (String sql, String[] columnNames)
             throws SQLException
       {
          return mPreparedStatement.executeUpdate(sql, columnNames);
       }
 
       /** {@inheritDoc} */
       public Connection getConnection ()
             throws SQLException
       {
          return mPreparedStatement.getConnection();
       }
 
       /** {@inheritDoc} */
       public int getFetchDirection ()
             throws SQLException
       {
          return mPreparedStatement.getFetchDirection();
       }
 
       /** {@inheritDoc} */
       public int getFetchSize ()
             throws SQLException
       {
          return mPreparedStatement.getFetchSize();
       }
 
       /** {@inheritDoc} */
       public ResultSet getGeneratedKeys ()
             throws SQLException
       {
          return mPreparedStatement.getGeneratedKeys();
       }
 
       /** {@inheritDoc} */
       public int getMaxFieldSize ()
             throws SQLException
       {
          return mPreparedStatement.getMaxFieldSize();
       }
 
       /** {@inheritDoc} */
       public int getMaxRows ()
             throws SQLException
       {
          return mPreparedStatement.getMaxRows();
       }
 
       /** {@inheritDoc} */
       public ResultSetMetaData getMetaData ()
             throws SQLException
       {
          return mPreparedStatement.getMetaData();
       }
 
       /** {@inheritDoc} */
       public boolean getMoreResults ()
             throws SQLException
       {
          return mPreparedStatement.getMoreResults();
       }
 
       /** {@inheritDoc} */
       public boolean getMoreResults (int current)
             throws SQLException
       {
          return mPreparedStatement.getMoreResults(current);
       }
 
       /** {@inheritDoc} */
       public ParameterMetaData getParameterMetaData ()
             throws SQLException
       {
          return mPreparedStatement.getParameterMetaData();
       }
 
       /** {@inheritDoc} */
       public int getQueryTimeout ()
             throws SQLException
       {
          return mPreparedStatement.getQueryTimeout();
       }
 
       /** {@inheritDoc} */
       public ResultSet getResultSet ()
             throws SQLException
       {
          return mPreparedStatement.getResultSet();
       }
 
       /** {@inheritDoc} */
       public int getResultSetConcurrency ()
             throws SQLException
       {
          return mPreparedStatement.getResultSetConcurrency();
       }
 
       /** {@inheritDoc} */
       public int getResultSetHoldability ()
             throws SQLException
       {
          return mPreparedStatement.getResultSetHoldability();
       }
 
       /** {@inheritDoc} */
       public int getResultSetType ()
             throws SQLException
       {
          return mPreparedStatement.getResultSetType();
       }
 
       /** {@inheritDoc} */
       public int getUpdateCount ()
             throws SQLException
       {
          return mPreparedStatement.getUpdateCount();
       }
 
       /** {@inheritDoc} */
       public SQLWarning getWarnings ()
             throws SQLException
       {
          return mPreparedStatement.getWarnings();
       }
 
       /** {@inheritDoc} */
       public void setArray (int i, Array x)
             throws SQLException
       {
          mPreparedStatement.setArray(i, x);
       }
 
       /** {@inheritDoc} */
       public void setAsciiStream (int parameterIndex, InputStream x, int length)
             throws SQLException
       {
          mPreparedStatement.setAsciiStream(parameterIndex, x, length);
       }
 
       /** {@inheritDoc} */
       public void setBigDecimal (int parameterIndex, BigDecimal x)
             throws SQLException
       {
          mPreparedStatement.setBigDecimal(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setBinaryStream (
             int parameterIndex, InputStream x, int length)
             throws SQLException
       {
          mPreparedStatement.setBinaryStream(parameterIndex, x, length);
       }
 
       /** {@inheritDoc} */
       public void setBlob (int i, Blob x)
             throws SQLException
       {
          mPreparedStatement.setBlob(i, x);
       }
 
       /** {@inheritDoc} */
       public void setBoolean (int parameterIndex, boolean x)
             throws SQLException
       {
          mPreparedStatement.setBoolean(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setByte (int parameterIndex, byte x)
             throws SQLException
       {
          mPreparedStatement.setByte(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setBytes (int parameterIndex, byte[] x)
             throws SQLException
       {
          mPreparedStatement.setBytes(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setCharacterStream (int parameterIndex, Reader reader,
             int length)
             throws SQLException
       {
          mPreparedStatement.setCharacterStream(parameterIndex, reader, length);
       }
 
       /** {@inheritDoc} */
       public void setClob (int i, Clob x)
             throws SQLException
       {
          mPreparedStatement.setClob(i, x);
       }
 
       /** {@inheritDoc} */
       public void setCursorName (String name)
             throws SQLException
       {
          mPreparedStatement.setCursorName(name);
       }
 
       /** {@inheritDoc} */
       public void setDate (int parameterIndex, Date x)
             throws SQLException
       {
          mPreparedStatement.setDate(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setDate (int parameterIndex, Date x, Calendar cal)
             throws SQLException
       {
          mPreparedStatement.setDate(parameterIndex, x, cal);
       }
 
       /** {@inheritDoc} */
       public void setDouble (int parameterIndex, double x)
             throws SQLException
       {
          mPreparedStatement.setDouble(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setEscapeProcessing (boolean enable)
             throws SQLException
       {
          mPreparedStatement.setEscapeProcessing(enable);
       }
 
       /** {@inheritDoc} */
       public void setFetchDirection (int direction)
             throws SQLException
       {
          mPreparedStatement.setFetchDirection(direction);
       }
 
       /** {@inheritDoc} */
       public void setFetchSize (int rows)
             throws SQLException
       {
          mPreparedStatement.setFetchSize(rows);
       }
 
       /** {@inheritDoc} */
       public void setFloat (int parameterIndex, float x)
             throws SQLException
       {
          mPreparedStatement.setFloat(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setInt (int parameterIndex, int x)
             throws SQLException
       {
          mPreparedStatement.setInt(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setLong (int parameterIndex, long x)
             throws SQLException
       {
          mPreparedStatement.setLong(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setMaxFieldSize (int max)
             throws SQLException
       {
          mPreparedStatement.setMaxFieldSize(max);
       }
 
       /** {@inheritDoc} */
       public void setMaxRows (int max)
             throws SQLException
       {
          mPreparedStatement.setMaxRows(max);
       }
 
       /** {@inheritDoc} */
       public void setNull (int parameterIndex, int sqlType)
             throws SQLException
       {
          mPreparedStatement.setNull(parameterIndex, sqlType);
       }
 
       /** {@inheritDoc} */
       public void setNull (int paramIndex, int sqlType, String typeName)
             throws SQLException
       {
          mPreparedStatement.setNull(paramIndex, sqlType, typeName);
       }
 
       /** {@inheritDoc} */
       public void setObject (int parameterIndex, Object x)
             throws SQLException
       {
          mPreparedStatement.setObject(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setObject (int parameterIndex, Object x, int targetSqlType)
             throws SQLException
       {
          mPreparedStatement.setObject(parameterIndex, x, targetSqlType);
       }
 
       /** {@inheritDoc} */
       public void setObject (int parameterIndex, Object x, int targetSqlType,
             int scale)
             throws SQLException
       {
          mPreparedStatement.setObject(parameterIndex, x, targetSqlType, scale);
       }
 
       /** {@inheritDoc} */
       public void setQueryTimeout (int seconds)
             throws SQLException
       {
          mPreparedStatement.setQueryTimeout(seconds);
       }
 
       /** {@inheritDoc} */
       public void setRef (int i, Ref x)
             throws SQLException
       {
          mPreparedStatement.setRef(i, x);
       }
 
       /** {@inheritDoc} */
       public void setShort (int parameterIndex, short x)
             throws SQLException
       {
          mPreparedStatement.setShort(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setString (int parameterIndex, String x)
             throws SQLException
       {
          mPreparedStatement.setString(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setTime (int parameterIndex, Time x)
             throws SQLException
       {
          mPreparedStatement.setTime(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setTime (int parameterIndex, Time x, Calendar cal)
             throws SQLException
       {
          mPreparedStatement.setTime(parameterIndex, x, cal);
       }
 
       /** {@inheritDoc} */
       public void setTimestamp (int parameterIndex, Timestamp x)
             throws SQLException
       {
          mPreparedStatement.setTimestamp(parameterIndex, x);
       }
 
       /** {@inheritDoc} */
       public void setTimestamp (int parameterIndex, Timestamp x, Calendar cal)
             throws SQLException
       {
          mPreparedStatement.setTimestamp(parameterIndex, x, cal);
       }
 
       /** {@inheritDoc} */
       public void setUnicodeStream (int parameterIndex, InputStream x,
             int length)
             throws SQLException
       {
          mPreparedStatement.setUnicodeStream(parameterIndex, x, length);
       }
 
       /** {@inheritDoc} */
       public void setURL (int parameterIndex, URL x)
             throws SQLException
       {
          mPreparedStatement.setURL(parameterIndex, x);
       }
    }
 }
