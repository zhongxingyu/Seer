 /*
  * Copyright 2011 Piero Ottuzzi <piero.ottuzzi@brucalipto.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 package org.brucalipto.sqlutil;
 
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.sql.Types;
 
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
import org.apache.commons.beanutils.DynaProperty;
 import org.apache.commons.beanutils.RowSetDynaClass;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 /**
  * This class exposes methods useful to simplify SQL usage in Java
  * @author Piero Ottuzzi <piero.ottuzzi@brucalipto.org>
  */
 public abstract class SQLManager
 {
     private final static Log log = LogFactory.getLog(SQLManager.class);
     
 	private final static String REF_PREFIX = "java:comp/env/";
 
 	protected final DataSource dataSource;
 	protected final Connection connection;
     
     public final static int SUCCESS = 0;
     public final static int GENERIC_ERROR = -1;
     public static final int INSERT_ERROR = -2;
     public static final int UPDATE_ERROR = -3;
     public static final int DELETE_ERROR = -4;
     public static final int NOTHING_CREATED = -5;
     public static final int NOTHING_UPDATED = -6;
     public static final int NOTHING_DELETED = -7;
     
     /**
      *Protected constructor to be used by extending classes
      */
     protected SQLManager(String dsJNDIName)
     {
         this.dataSource = setupDataSource(dsJNDIName);
         this.connection = null;
     }
 
     /**
      *Protected constructor to be used by extending classes
      */
     protected SQLManager(DataSource dataSource)
     {
         this.dataSource = dataSource;
         this.connection = null;
     }
 
     /**
      *Protected constructor to be used by extending classes
      */
     protected SQLManager(Connection conn)
     {
     	this.dataSource = null;
         this.connection = conn;
     }
 
     /**
      * Method useful for SQL INSERT
      * @param preparedStatement The prepared statement to execute
      * @param parameters List of {@link SQLParameter} to use to complete the prepared statement
      * @return The number of rows inserted, -1 if an error occurs
      */
     public int insert(final String preparedStatement, final SQLParameter[] parameters)
     {
         return executeSimpleQuery(preparedStatement, parameters);
     }
 
     /**
      * Method useful for SQL INSERT
      * @param preparedStatement The prepared statement to execute
      * @param parameters The {@link PrepStmtInputBean} to use to complete the prepared statement
      * @return The number of rows inserted, -1 if an error occurs
      */
     public int insert(final String preparedStatement, final PrepStmtInputBean parameters)
     {
         if (parameters!=null)
         {
             return executeSimpleQuery(preparedStatement, parameters.getInputParams());
         }
         return executeSimpleQuery(preparedStatement, null);
     }
 
     /**
      * Method useful for SQL UPDATE
      * @param preparedStatement The prepared statement to execute
      * @param parameters List of {@link SQLParameter} to use to complete the prepared statement
      * @return The number of rows updated, -1 if an error occurs
      */
     public int update(final String preparedStatement, final SQLParameter[] parameters)
     {
         return executeSimpleQuery(preparedStatement, parameters);
     }
 
     /**
      * Method useful for SQL UPDATE
      * @param preparedStatement The prepared statement to execute
      * @param parameters The {@link PrepStmtInputBean} to use to complete the prepared statement
      * @return The number of rows updated, -1 if an error occurs
      */
     public int update(final String preparedStatement, final PrepStmtInputBean parameters)
     {
         if (parameters!=null)
         {
             return executeSimpleQuery(preparedStatement, parameters.getInputParams());
         }
         return executeSimpleQuery(preparedStatement, null);
     }
 
     /**
      * Method useful for SQL DELETE
      * @param preparedStatement The prepared statement to execute
      * @param parameters List of {@link SQLParameter} to use to complete the prepared statement
      * @return The number of rows deleted, -1 if an error occurs
      */
     public int delete(final String preparedStatement, final SQLParameter[] parameters)
     {
         return executeSimpleQuery(preparedStatement, parameters);
     }
 
     /**
      * Method useful for SQL DELETE
      * @param preparedStatement The prepared statement to execute
      * @param parameters The {@link PrepStmtInputBean} to use to complete the prepared statement
      * @return The number of rows deleted, -1 if an error occurs
      */
     public int delete(final String preparedStatement, final PrepStmtInputBean parameters)
     {
         if (parameters!=null)
         {
             return executeSimpleQuery(preparedStatement, parameters.getInputParams());
         }
         return executeSimpleQuery(preparedStatement, null);
     }
 
     /**
      * Method useful for SQL SELECT
      * @param preparedStatement The prepared statement to execute
      * @param parameters List of {@link SQLParameter} to use to complete the prepared statement
     * @return Returns a RowSetDynaClass containing returned rows
     * @throws SQLException 
      */
    public RowSetDynaClass dynaSelect(final String preparedStatement, final SQLParameter[] params) throws SQLException
     {
     	SQLParameter[] parameters;
         if (params==null)
         {
             parameters = new SQLParameter[0];
             log.debug("Going to execute a query without parameters.");
         }
         else
         {
         	parameters = (SQLParameter[])params.clone();
         }
         Connection dbConn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         
         try
         {
             if (this.dataSource!=null)
         	{
             	dbConn = this.dataSource.getConnection();
         	}
         	else
         	{
         		dbConn = this.connection;
         	}
             pstmt = dbConn.prepareStatement(preparedStatement);
             for (int i=0; i<parameters.length; i++)
             {
                 final SQLParameter param = parameters[i];
                 log.debug((i+1)+") Going to add parameter "+param);
                 final int sqlType = param.getSqlType();
                 final Object paramValue = param.getValue();
                 if (paramValue==null)
                 {
                     pstmt.setNull(i+1, sqlType);
                     continue;
                 }
                 switch(sqlType)
                 {
                     case Types.VARCHAR:
                         pstmt.setString(i+1, (String)paramValue);
                         break;
                     case Types.INTEGER:
                         if (paramValue instanceof Integer)
                         {
                             pstmt.setInt(i+1, ((Integer)paramValue).intValue());
                         }
                         else if (paramValue instanceof Long)
                         {
                             pstmt.setLong(i+1, ((Long)paramValue).longValue());
                         }
                         break;
                     case Types.DATE:
                         pstmt.setDate(i+1, (Date)paramValue);
                         break;
                     case Types.BOOLEAN:
                         pstmt.setBoolean(i+1, ((Boolean)paramValue).booleanValue());
                         break;
                     case Types.CHAR:
                         pstmt.setString(i+1, ((Character)paramValue).toString());
                         break;
                     case Types.DOUBLE:
                         pstmt.setDouble(i+1, ((Double)paramValue).doubleValue());
                         break;
                     case Types.FLOAT:
                         pstmt.setFloat(i+1, ((Float)paramValue).floatValue());
                         break;
                     case Types.TIMESTAMP:
                         pstmt.setTimestamp(i+1, (Timestamp)paramValue);
                         break;
                     default:
                         pstmt.setObject(i+1, paramValue);
                         break;
                 }
             }
 
             rs = pstmt.executeQuery();
            RowSetDynaClass rowSetDynaClass = new RowSetDynaClass(rs, false);
             if (log.isDebugEnabled())
             {
                log.debug("Prepared statement '"+preparedStatement+"' returned '"+rowSetDynaClass.getRows().size()+"' rows with following properties:");
        		DynaProperty[] properties = rowSetDynaClass.getDynaProperties();
        		for (int i=0; i<properties.length; i++)
        		{
        			log.debug("Name: '"+properties[i].getName()+"'; Type: '"+properties[i].getType().getName()+"'");
        		}
             }
            return rowSetDynaClass;
         }
         catch(SQLException e)
         {
             log.error("Error executing prepared statement '"+preparedStatement+"'", e);
            throw e;
         }
         finally
         {
             closeResources(rs, pstmt, dbConn);
         }
     }
 
     /**
      * Method useful for SQL SELECT
      * @param preparedStatement The prepared statement to execute
      * @param params List of {@link SQLParameter} to use to complete the prepared statement
      * @param outputSQLType A java.sql.Types type of return value
      * @return The {@link SPParameter} containing the returned value
      */
     public SQLParameter simpleSelect(final String preparedStatement, SQLParameter[] params, final int outputSQLType)
     {
         final SQLParameter[] parameters;
         if (params==null)
         {
             parameters = new SQLParameter[0];
             log.debug("Going to execute a query without parameters.");
         }
         else
         {
         	parameters = (SQLParameter[])params.clone();
         }
         Connection dbConn = null;
         PreparedStatement pstmt = null;
         ResultSet rs = null;
         try
         {
             if (this.dataSource!=null)
         	{
             	dbConn = this.dataSource.getConnection();
         	}
         	else
         	{
         		dbConn = this.connection;
         	}
             pstmt = dbConn.prepareStatement(preparedStatement);
             for (int i=0; i<parameters.length; i++)
             {
                 final SQLParameter param = parameters[i];
                 log.debug((i+1)+") Going to add parameter "+param);
                 final int sqlType = param.getSqlType();
                 final Object paramValue = param.getValue();
                 if (paramValue==null)
                 {
                     pstmt.setNull(i+1, sqlType);
                     continue;
                 }
                 switch(sqlType)
                 {
                     case Types.VARCHAR:
                         pstmt.setString(i+1, (String)paramValue);
                         break;
                     case Types.INTEGER:
                         if (paramValue instanceof Integer)
                         {
                             pstmt.setInt(i+1, ((Integer)paramValue).intValue());
                         }
                         else if (paramValue instanceof Long)
                         {
                             pstmt.setLong(i+1, ((Long)paramValue).longValue());
                         }
                         break;
                     case Types.DATE:
                         pstmt.setDate(i+1, (Date)paramValue);
                         break;
                     case Types.BOOLEAN:
                         pstmt.setBoolean(i+1, ((Boolean)paramValue).booleanValue());
                         break;
                     case Types.CHAR:
                         pstmt.setString(i+1, ((Character)paramValue).toString());
                         break;
                     case Types.DOUBLE:
                         pstmt.setDouble(i+1, ((Double)paramValue).doubleValue());
                         break;
                     case Types.FLOAT:
                         pstmt.setFloat(i+1, ((Float)paramValue).floatValue());
                         break;
                     case Types.TIMESTAMP:
                         pstmt.setTimestamp(i+1, (Timestamp)paramValue);
                         break;
                     default:
                         pstmt.setObject(i+1, paramValue);
                         break;
                 }
             }
 
             rs = pstmt.executeQuery();
             log.debug("Prepared statement '"+preparedStatement+"' succesfully executed!");
             while (rs.next())
             {
                 return new SQLParameter(outputSQLType, (Serializable)rs.getObject(1));
             }
             log.info("Prepared statement '"+preparedStatement+"' returned '0' rows");
         }
         catch(SQLException e)
         {
             log.error("Error executing prepared statement '"+preparedStatement+"'", e);
         }
         catch(Exception e)
         {
             log.error("Error executing prepared statement '"+preparedStatement+"'", e);
         }
         finally
         {
             closeResources(rs, pstmt, dbConn);
         }
 
         return new SQLParameter(outputSQLType, null);
     }
 
     protected int executeSimpleQuery(final String preparedStatement, final SQLParameter[] params)
     {
     	final SQLParameter[] parameters;
         if (params==null)
         {
             parameters = new SQLParameter[0];
             log.debug("Going to execute a query without parameters.");
         }
         else
         {
         	parameters = (SQLParameter[])params.clone();
         }
         Connection dbConn = null;
         PreparedStatement pstmt = null;
         try
         {
         	if (this.dataSource!=null)
         	{
             	dbConn = this.dataSource.getConnection();
         	}
         	else
         	{
         		dbConn = this.connection;
         	}
             pstmt = dbConn.prepareStatement(preparedStatement);
             for (int i=0; i<parameters.length; i++)
             {
                 final SQLParameter param = parameters[i];
                 log.debug((i+1)+") Going to add parameter "+param);
                 final int sqlType = param.getSqlType();
                 final Object paramValue = param.getValue();
                 if (paramValue==null)
                 {
                     pstmt.setNull(i+1, sqlType);
                     continue;
                 }
                 switch(sqlType)
                 {
                     case Types.VARCHAR:
                         pstmt.setString(i+1, (String)paramValue);
                         break;
                     case Types.INTEGER:
                         if (paramValue instanceof Integer)
                         {
                             pstmt.setInt(i+1, ((Integer)paramValue).intValue());
                         }
                         else if (paramValue instanceof Long)
                         {
                             pstmt.setLong(i+1, ((Long)paramValue).longValue());
                         }
                         break;
                     case Types.DATE:
                         pstmt.setDate(i+1, (Date)paramValue);
                         break;
                     case Types.BOOLEAN:
                         pstmt.setBoolean(i+1, ((Boolean)paramValue).booleanValue());
                         break;
                     case Types.CHAR:
                         pstmt.setString(i+1, ((Character)paramValue).toString());
                         break;
                     case Types.DOUBLE:
                         pstmt.setDouble(i+1, ((Double)paramValue).doubleValue());
                         break;
                     case Types.FLOAT:
                         pstmt.setFloat(i+1, ((Float)paramValue).floatValue());
                         break;
                     case Types.TIMESTAMP:
                         pstmt.setTimestamp(i+1, (Timestamp)paramValue);
                         break;
                     default:
                         pstmt.setObject(i+1, paramValue);
                         break;
                 }
             }
 
             int result = pstmt.executeUpdate();
             log.debug("Prepared statement '"+preparedStatement+"' correctly executed ("+result+")");
             return result;
         }
         catch(SQLException e)
         {
             log.error("Error executing prepared statement '"+preparedStatement+"'", e);
         }
         catch(Exception e)
         {
             log.error("Error executing prepared statement '"+preparedStatement+"'", e);
         }
         finally
         {
             closeResources(pstmt, dbConn);
         }
 
         return -1;
     }
     
     /**
      * Utility method to close DB access structures
      * @param rs The Resultset to be closed
      * @param stmt The Statement to be closed
      * @param dbConn The Connection to be closed
      */
     protected static void closeResources(final ResultSet rs, final Statement stmt, final Connection dbConn)
     {
         try{if (rs!=null)rs.close();}catch(Exception e){log.error("Error closing resultSet", e);}
         closeResources(stmt, dbConn);
     }
 
     /**
      * Utility method to close DB access structures
      * @param stmt The Statement to be closed
      * @param dbConn The Connection to be closed
      */
     protected static void closeResources(final Statement stmt, final Connection dbConn)
     {
         try{if (stmt!=null)stmt.close();}catch(Exception e){log.error("Error closing statement", e);}
         closeResources(dbConn);
     }
 
     /**
      * Utility method to close DB access structures
      * @param dbConn The Connection to be closed
      */
     protected static void closeResources(final Connection dbConn)
     {
         try{if (dbConn!=null)dbConn.close();}catch(Exception e){log.error("Error closing connection", e);}
     }
     
     protected static DataSource setupDataSource(final String dsJNDIName)
     {
         Context env = null;
 		final String contextURI = dsJNDIName.startsWith(REF_PREFIX)?dsJNDIName:REF_PREFIX+dsJNDIName;
 		log.debug("Looking for '"+contextURI+"' in Context");
         try
         {
             env = new InitialContext();
             return (DataSource) env.lookup(contextURI);
         }
         catch(NamingException e)
         {
         	log.error("Error getting datasource '' from Context", e);
         	return null;
         }
         finally
         {
             try{if (env!=null)env.close();}catch(NamingException e){log.error("Error closing context", e);}
         }
     }
     
     public abstract SPOutputBean executeSP(final SPInputBean spib) throws SQLException;
 }
