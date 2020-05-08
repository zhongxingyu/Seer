 /**
  * <p>Title: AbstractJDBCDAOImpl Class>
  * <p>Description:	JDBCDAO is default implementation of DAO and JDBCDAO through JDBC.
  * Copyright:    Copyright (c) year
  * Company: Washington University, School of Medicine, St. Louis.
  * @author Gautam Shetty
  * @version 1.00
  */
 
 package edu.wustl.dao;
 
 import java.sql.Connection;
 import java.sql.Date;
 import java.sql.PreparedStatement;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.text.DateFormat;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import edu.wustl.common.audit.AuditManager;
 import edu.wustl.common.beans.SessionDataBean;
 import edu.wustl.common.exception.ErrorKey;
 import edu.wustl.common.querydatabean.QueryDataBean;
import edu.wustl.common.security.exceptions.SMException;
import edu.wustl.common.util.PagenatedResultData;
 import edu.wustl.common.util.QueryParams;
 import edu.wustl.common.util.Utility;
 import edu.wustl.common.util.global.Constants;
 import edu.wustl.common.util.logger.Logger;
 import edu.wustl.dao.condition.EqualClause;
 import edu.wustl.dao.connectionmanager.IConnectionManager;
 import edu.wustl.dao.exception.DAOException;
 import edu.wustl.dao.util.DAOConstants;
 import edu.wustl.dao.util.DatabaseConnectionParams;
 
 /**
  * @author kalpana_thakur
  *
  */
 public abstract class AbstractJDBCDAOImpl implements JDBCDAO
 {
 
 	/**
 	 * Connection object.
 	 */
 	private Connection connection = null;
 	/**
 	 * Audit Manager.
 	 */
 	private AuditManager auditManager;
 	/**
 	 * Class Logger.
 	 */
 	private static org.apache.log4j.Logger logger = Logger.getLogger(AbstractJDBCDAOImpl.class);
 	/**
 	 * Connection Manager.
 	 */
 	private IConnectionManager connectionManager = null ;
 
 	/**
 	 * This method will be used to establish the session with the database.
 	 * Declared in DAO class.
 	 * @param sessionDataBean : holds the data associated to the session.
 	 * @throws DAOException :It will throw DAOException
 	 */
 	public void openSession(SessionDataBean sessionDataBean)
 	throws DAOException
 	{
 		try
 		{
 			initializeAuditManager(sessionDataBean);
 			connection = connectionManager.getConnection();
 			connection.setAutoCommit(false);
 		}
 		catch (SQLException sqlExp)
 		{
 			logger.error(sqlExp.getMessage(), sqlExp);
 			ErrorKey errorKey = ErrorKey.getErrorKey("db.operation.error");
 			throw new DAOException(errorKey,sqlExp,"AbstractJDBCDAOImpl.java :"+
 					DAOConstants.OPEN_SESSION_ERROR);
 		}
 	}
 
 	/**
 	 * This method will be used to close the session with the database.
 	 * Declared in DAO class.
 	 * @throws DAOException : It will throw DAOException.
 	 */
 	public void closeSession() throws DAOException
 	{
 		try
 		{
 			auditManager = null;
 			getConnectionManager().closeConnection();
 		}
 		catch(Exception dbex)
 		{
 			ErrorKey errorKey = ErrorKey.getErrorKey("db.operation.error");
 			throw new DAOException(errorKey,dbex,"AbstractJDBCDAOImpl.java :"
 					+DAOConstants.CLOSE_SESSION_ERROR);
 		}
 	}
 
 	/**
 	 * Commit the database level changes.
 	 * Declared in DAO class.
 	 * @throws DAOException : It will throw DAOException
 	 * @throws SMException
 	 */
 	public void commit() throws DAOException
 	{
 		try
 		{
 			auditManager.insert(this);
 			if(connection == null)
 			{
 				logger.fatal(DAOConstants.NO_CONNECTION_TO_DB);
 			}
 
 			connection.commit();
 		}
 		catch (SQLException dbex)
 		{
 			logger.error(dbex.getMessage(), dbex);
 			ErrorKey errorKey = ErrorKey.getErrorKey("db.operation.error");
 			throw new DAOException(errorKey,dbex,"AbstractJDBCDAOImpl.java :"
 					+DAOConstants.COMMIT_DATA_ERROR);
 		}
 		catch (Exception exp)
 		{
 			ErrorKey errorKey = ErrorKey.getErrorKey("db.audit.error");
 			throw new DAOException(errorKey,exp,"AbstractJDBCDAOImpl.java :"+
 					DAOConstants.COMMIT_DATA_ERROR);
 		}
 	}
 
 	/**
 	 * RollBack all the changes after last commit.
 	 * Declared in DAO class.
 	 * @throws DAOException : It will throw DAOException.
 	 */
 	public void rollback() throws DAOException
 	{
 		try
 		{
 			if(connection == null)
 			{
 				logger.fatal(DAOConstants.NO_CONNECTION_TO_DB);
 			}
 
 			connection.rollback();
 		}
 		catch (SQLException dbex)
 		{
 			logger.error(dbex.getMessage(), dbex);
 			ErrorKey errorKey = ErrorKey.getErrorKey("db.operation.error");
 			throw new DAOException(errorKey, dbex,"AbstractJDBCDAOImpl.java :"+
 					DAOConstants.ROLLBACK_ERROR);
 
 		}
 	}
 
 
 	/**
 	 * This will be called to initialized the Audit Manager.
 	 * @param sessionDataBean : This will holds the session data.
 	 */
 	private void initializeAuditManager(SessionDataBean sessionDataBean)
 	{
 		auditManager = new AuditManager();
 		if (sessionDataBean == null)
 		{
 			auditManager.setUserId(null);
 		}
 		else
 		{
 			auditManager.setUserId(sessionDataBean.getUserId());
 			auditManager.setIpAddress(sessionDataBean.getIpAddress());
 		}
 	}
 
 	/**
 	 * This method will be called for executing a static SQL statement.
 	 * @see edu.wustl.dao.JDBCDAO#executeUpdate(java.lang.String)
 	 * @param query :Holds the query string.
 	 * @throws DAOException : DAOException.
 	 */
 	public void executeUpdate(String query) throws DAOException
 	{
 		DatabaseConnectionParams databaseConnectionParams = new DatabaseConnectionParams();
 		databaseConnectionParams.setConnection(connection);
 		databaseConnectionParams.executeUpdate(query);
 	}
 
 	/**
 	 * @see edu.wustl.common.dao.JDBCDAO#createTable(java.lang.String, java.lang.String[])
 	 * This method will Create and execute a table with the name and columns specified
 	 * @param tableName : Table Name
 	 * @param columnNames : Columns of the table
 	 * @throws DAOException DAOException
 	 * */
 
 	public void createTable(String tableName, String[] columnNames) throws DAOException
 	{
 		String query = createTableQuery(tableName,columnNames);
 		executeUpdate(query);
 	}
 
 	/**
 	 * Creates a table with the query specified.
 	 * @param query Query create table.
 	 * @throws DAOException DAOException
 	 */
 	public void createTable(String query) throws DAOException
 	{
 		executeUpdate(query);
 	}
 
 	/**
 	 * Generates the Create Table Query.
 	 * @param tableName Name of the table to create.
 	 * @param columnNames Columns in the table.
 	 * @return Create Table Query
 	 * @throws DAOException : It will throw DAOException
 	 */
 	private String createTableQuery(String tableName, String[] columnNames) throws DAOException
 	{
 		StringBuffer query = new StringBuffer("CREATE TABLE").append(DAOConstants.TAILING_SPACES).
 		append(tableName).append(" (");
 		int index;
 
 		for ( index=0; index < (columnNames.length - 1); index++)
 		{
 
 			query = query.append(columnNames[index]).append(" VARCHAR(50),");
 		}
 		query.append(columnNames[index]).append(" VARCHAR(50))");
 
 		return  query.toString();
 	}
 	/**
 	 * Returns the ResultSet containing all the rows in the table represented in sourceObjectName.
 	 * @param sourceObjectName The table name.
 	 * @return The ResultSet containing all the rows in the table represented in sourceObjectName.
 	 * @throws ClassNotFoundException
 	 * @throws DAOException generic DAOException
 	 */
 	public List<Object> retrieve(String sourceObjectName) throws DAOException
 	{
 		logger.debug("Inside retrieve method");
 		return retrieve(sourceObjectName, null, null,false);
 	}
 
 	/**
 	 * Returns the ResultSet containing all the rows according to the columns specified
 	 * from the table represented in sourceObjectName.
 	 * @param sourceObjectName The table name.
 	 * @param selectColumnName The column names in select clause.
 	 * @return The ResultSet containing all the rows according to the columns specified
 	 * from the table represented in sourceObjectName.
 	 * @throws DAOException : DAOException
 	*/
 	public List<Object> retrieve(String sourceObjectName, String[] selectColumnName) throws DAOException
 	{
 		return retrieve(sourceObjectName, selectColumnName,null,false);
 	}
 
 	/**
 	 * Returns the ResultSet containing all the rows according to the columns specified
 	 * from the table represented in sourceObjectName.
 	 * @param sourceObjectName The table name.
 	 * @param selectColumnName The column names in select clause.
 	 * @param onlyDistinctRows true if only distinct rows should be selected.
 	 * @return The ResultSet containing all the rows according to the columns specified
 	 * from the table represented in sourceObjectName.
 	 * @throws DAOException DAOException.
 	 */
 	public List<Object> retrieve(String sourceObjectName, String[] selectColumnName,
 			boolean onlyDistinctRows) throws DAOException
 	{
 		return retrieve(sourceObjectName, selectColumnName,null,
 				onlyDistinctRows);
 	}
 
 	/**
 	 * Returns the ResultSet containing all the rows according to the columns specified
 	 * from the table represented in sourceObjectName as per the where clause.
 	 * @param sourceObjectName The table name.
 	 * @param selectColumnName The column names in select clause.
 	 * @param queryWhereClause The where condition clause which holds the where column name,
 	 * value and conditions applied
 	 * @return The ResultSet containing all the rows according to the columns specified
 	 * from the table represented in sourceObjectName which satisfies the where condition
 	 * @throws DAOException : DAOException
 	 */
 	public List<Object> retrieve(String sourceObjectName,
 			String[] selectColumnName, QueryWhereClause queryWhereClause)
 			throws DAOException
 	{
 		return retrieve(sourceObjectName, selectColumnName,queryWhereClause,false);
 	}
 
 	/**
 	 * Returns the ResultSet containing all the rows from the table represented in sourceObjectName
 	 * according to the where clause.It will create the where condition clause which holds where column name,
 	 * value and conditions applied.
 	 * @param sourceObjectName The table name.
 	 * @param whereColumnName The column names in where clause.
 	 * @param whereColumnValue The column values in where clause.
 	 * @return The ResultSet containing all the rows from the table represented
 	 * in sourceObjectName which satisfies the where condition
 	 * @throws DAOException : DAOException
 	 */
 	public List<Object> retrieve(String sourceObjectName, String whereColumnName, Object whereColumnValue)
 			throws DAOException
 	{
 		String[] selectColumnName = null;
 
 		QueryWhereClause queryWhereClause = new QueryWhereClause(sourceObjectName);
 		queryWhereClause.addCondition(new EqualClause(whereColumnName,whereColumnValue,sourceObjectName));
 
 		return retrieve(sourceObjectName, selectColumnName,queryWhereClause,false);
 	}
 
 
 	/**
 	 * Retrieves the records for class name in sourceObjectName according to
 	 * field values passed in the passed session.
 	 * @param sourceObjectName This will holds the object name.
 	 * @param selectColumnName An array of field names in select clause.
 	 * @param queryWhereClause This will hold the where clause.It holds following:
 	 * 1.whereColumnName : An array of field names in where clause.
 	 * 2.whereColumnCondition : The comparison condition for the field values.
 	 * 3.whereColumnValue : An array of field values.
 	 * 4.joinCondition : The join condition.
 	 * @param onlyDistinctRows True if only distinct rows should be selected
 	 * @return The ResultSet containing all the rows from the table represented
 	 * in sourceObjectName which satisfies the where condition
 	 * @throws DAOException : DAOException
 	 */
 	public List<Object> retrieve(String sourceObjectName, String[] selectColumnName,
 			QueryWhereClause queryWhereClause,
 			 boolean onlyDistinctRows) throws DAOException
 	{
 
 		List<Object> list = null;
 		try
 		{
 			StringBuffer queryStrBuff = getSelectPartOfQuery(selectColumnName, onlyDistinctRows);
 			getFromPartOfQuery(sourceObjectName, queryStrBuff);
 
 			if(queryWhereClause != null)
 			{
 				queryStrBuff.append(queryWhereClause.toWhereClause());
 			}
 
 			logger.debug("JDBC Query " + queryStrBuff);
 			list = executeQuery(queryStrBuff.toString(), null, false, null);
 		}
 		catch (Exception exp)
 		{
 			ErrorKey errorKey = ErrorKey.getErrorKey("db.operation.error");
 			throw new DAOException(errorKey, exp,"AbstractJDBCDAOImpl.java :"+
 					DAOConstants.RETRIEVE_ERROR);
 
 		}
 
 		return list;
 	}
 
 	/**
 	 * This method will return the select clause of Query.
 	 * @param selectColumnName An array of field names in select clause.
 	 * @param onlyDistinctRows true if only distinct rows should be selected
 	 * @return It will return the select clause of Query.
 	 */
 	private StringBuffer getSelectPartOfQuery(String[] selectColumnName,
 			boolean onlyDistinctRows)
 	{
 		StringBuffer query = new StringBuffer("SELECT ");
 		if ((selectColumnName != null) && (selectColumnName.length > 0))
 		{
 			if (onlyDistinctRows)
 			{
 				query.append(" DISTINCT ");
 			}
 			int index;
 			for (index = 0; index < (selectColumnName.length - 1); index++)
 			{
 				query.append(selectColumnName[index]).append("  ,");
 			}
 			query.append(selectColumnName[index]).append("  ");
 		}
 		else
 		{
 			query.append("* ");
 		}
 		return query;
 	}
 
 	/**
 	 * This will generate the from clause of Query.
 	 * @param sourceObjectName The table name.
 	 * @param queryStrBuff Query buffer
 	 */
 	private void getFromPartOfQuery(String sourceObjectName, StringBuffer queryStrBuff)
 	{
 		queryStrBuff.append("FROM ").append(sourceObjectName);
 	}
 
 	/**
 	 * Executes the query.
 	 * @param query :Query to be executed.
 	 * @param sessionDataBean : Holds the data associated to the session.
 	 * @param isSecureExecute Query will be executed only if isSecureExecute is true.
 	 * @param queryResultObjectDataMap : queryResultObjectDataMap
 	 * @return This method executed query, parses the result and returns List of rows.
 	 * @throws DAOException : DAOException
 	 * @throws ClassNotFoundException : ClassNotFoundException
 	 */
 	public List<Object> executeQuery(String query, SessionDataBean sessionDataBean,
 			boolean isSecureExecute, Map<Object,QueryDataBean>
 			queryResultObjectDataMap) throws ClassNotFoundException,
 			DAOException
 	{
 
 		logger.debug("Inside executeQuery method");
 		QueryParams queryParams = new QueryParams();
 		queryParams.setQuery(query);
 		queryParams.setSessionDataBean(sessionDataBean);
 		queryParams.setSecureToExecute(isSecureExecute);
 		queryParams.setHasConditionOnIdentifiedField(false);
 		queryParams.setQueryResultObjectDataMap(queryResultObjectDataMap);
 		queryParams.setStartIndex(-1);
 		queryParams.setNoOfRecords(-1);
 
 		return getQueryResultList(queryParams).getResult();
 	}
 
 
 	/**
 	 *Description: Query performance issue. Instead of saving complete query results in session,
 	 *results will be fetched for each page navigation.
 	 *@param queryParams : This object will hold all the Query related details.
 	 *@throws DAOException : DAOException
 	 *@return : It will return the pagenatedResultData.
 	 * */
 	public PagenatedResultData executeQuery(QueryParams  queryParams) throws DAOException
 	{
 		PagenatedResultData pagenatedResultData = null;
 		if (!(Constants.SWITCH_SECURITY && queryParams.isSecureToExecute() &&
 				queryParams.getSessionDataBean() == null))
 		{
 		  pagenatedResultData = (PagenatedResultData)getQueryResultList(queryParams);
 		}
 		return pagenatedResultData;
 	}
 
 	/**
 	 * This method executed query, parses the result and returns List of rows after doing security checks.
 	 * for user's right to view a record/field
 	 * @param queryParams : It will hold all information related to query.
 	 * @return It will return the pagenatedResultData.
 	 * @throws DAOException : DAOException
 	 */
 	public abstract PagenatedResultData getQueryResultList(QueryParams queryParams) throws DAOException;
 
 	/**
 	 * @param tableName TODO
 	 * @param columnValues TODO
 	 * @param columnNames TODO
 	 * @throws DAOException  :DAOException
 	 * @throws SQLException : SQLException
 	 */
 	public void insertHashedValues(String tableName, List<Object> columnValues, List<String> columnNames)
 	throws DAOException, SQLException
 	{
 
 		List<String>columnNamesList = new ArrayList<String>();
 		ResultSetMetaData metaData;
 
 		DatabaseConnectionParams dbConnParamForMetadata = new DatabaseConnectionParams();
 		dbConnParamForMetadata.setConnection(getConnection());
 
 		DatabaseConnectionParams dbConnParamForInsertQuery = new DatabaseConnectionParams();
 		dbConnParamForInsertQuery.setConnection(getConnection());
 
 		PreparedStatement stmt = null;
 		try
 		{
 			if(columnNames != null && !columnNames.isEmpty())
 			{
 				metaData = getMetaData(tableName, columnNames,dbConnParamForMetadata);
 				columnNamesList = columnNames;
 			}
 			else
 			{
 				metaData = getMetaDataAndUpdateColumns(tableName,columnNamesList,
 						dbConnParamForMetadata);
 			}
 
 			String insertQuery = createInsertQuery(tableName,columnNamesList);
 			stmt = dbConnParamForInsertQuery.getPreparedStatement(insertQuery);
 			setStmtIndexValue(columnValues, metaData, stmt);
 			stmt.executeUpdate();
 		}
 		catch (SQLException sqlExp)
 		{
 			logger.error(sqlExp.getMessage(),sqlExp);
 			ErrorKey errorKey = ErrorKey.getErrorKey("db.operation.error");
 			throw new DAOException(errorKey, sqlExp,"AbstractJDBCDAOImpl.java :"+
 					DAOConstants.INSERT_OBJ_ERROR);
 		}
 		finally
 		{
 			dbConnParamForMetadata.closeConnectionParams();
 			dbConnParamForInsertQuery.closeConnectionParams();
 		}
 	}
 
 	/**
 	 * @param columnValues :
 	 * @param metaData :
 	 * @param stmt :
 	 * @throws SQLException :
 	 * @throws DAOException :
 	 */
 	private void setStmtIndexValue(List<Object> columnValues,
 			ResultSetMetaData metaData, PreparedStatement stmt)
 			throws SQLException, DAOException
 	{
 		for (int i = 0; i < columnValues.size(); i++)
 		{
 			Object obj = columnValues.get(i);
 			int index = i;index++;
 			if(isDateColumn(metaData,index))
 			{
 				setDateColumns(stmt, index,obj);
 				continue;
 			}
 			if(isTinyIntColumn(metaData,index))
 			{
 				setTinyIntColumns(stmt, index, obj);
 				continue;
 			}
 			/*if(isTimeStampColumn(stmt,i,obj))
 			{
 				continue;
 			}*/
 			if(isNumberColumn(metaData,index))
 			{
 				setNumberColumns(stmt, index, obj);
 				continue;
 			}
 			stmt.setObject(index, obj);
 		}
 	}
 
 	/**
 	 * @param metaData :
 	 * @param index :
 	 * @return true if column type date.
 	 * @throws SQLException :Exception
 	 */
 	private boolean isDateColumn(ResultSetMetaData metaData,int index) throws SQLException
 	{
 		boolean isDateType = false;
 		String type = metaData.getColumnTypeName(index);
 		if (("DATE").equals(type))
 		{
 			isDateType = true;
 		}
 		return isDateType;
 	}
 
 	/**
 	 * @param metaData :
 	 * @param index :
 	 * @return true if column type TinyInt.
 	 * @throws SQLException :Exception
 	 */
 	private boolean isTinyIntColumn(ResultSetMetaData metaData,int index) throws SQLException
 	{
 		boolean isTinyIntType = false;
 		String type = metaData.getColumnTypeName(index);
 		if (("TINYINT").equals(type))
 		{
 			isTinyIntType = true;
 		}
 		return isTinyIntType;
 	}
 
 	/**
 	 * @param metaData :
 	 * @param index :
 	 * @return true if column type is Number.
 	 * @throws SQLException :Exception
 	 */
 	private boolean isNumberColumn(ResultSetMetaData metaData,int index) throws SQLException
 	{
 		boolean isNumberType = false;
 		String type = metaData.getColumnTypeName(index);
 		if (("NUMBER").equals(type))
 		{
 			isNumberType = true;
 		}
 		return isNumberType;
 	}
 
 
 	/**
 	 * This method returns the metaData associated to the table specified in tableName.
 	 * @param tableName Name of the table whose metaData is requested
 	 * @param columnNames Table columns
 	 * @param dbConnParamForMetadata : Database connections to retrieve meta data.
 	 * @return It will return the metaData associated to the table.
 	 * @throws DAOException : DAOException
 	 */
 	protected final ResultSetMetaData getMetaData(String tableName,List<String> columnNames,
 			DatabaseConnectionParams dbConnParamForMetadata)throws DAOException
 	{
 
 		ResultSetMetaData metaData;
 		StringBuffer sqlBuff = new StringBuffer(DAOConstants.TAILING_SPACES);
 		sqlBuff.append("Select").append(DAOConstants.TAILING_SPACES);
 
 		dbConnParamForMetadata.setConnection(connection);
 		for (int i = 0; i < columnNames.size(); i++)
 		{
 			sqlBuff.append(columnNames.get(i));
 			if (i != columnNames.size() - 1)
 			{
 				sqlBuff.append("  ,");
 			}
 		}
 		sqlBuff.append(" from " + tableName + " where 1!=1");
 		metaData = dbConnParamForMetadata.getMetaData(sqlBuff.toString());
 
 		return metaData;
 
 	}
 
 	/**
 	 * This method will returns the metaData associated to the table specified in tableName
 	 * and update the list columnNames.
 	 * @param tableName Name of the table whose metaData is requested
 	 * @param columnNames Table columns
 	 * @param dbConnParamForMetadata : Database connections to retrieve meta data.
 	 * @return It will return the metaData associated to the table.
 	 * @throws DAOException : DAOException
 	 */
 	protected final ResultSetMetaData getMetaDataAndUpdateColumns(String tableName,
 			List<String> columnNames,DatabaseConnectionParams dbConnParamForMetadata)
 	throws DAOException
 	{
 		ResultSetMetaData metaData;
 		try
 		{
 
 			dbConnParamForMetadata.setConnection(connection);
 			StringBuffer sqlBuff = new StringBuffer(DAOConstants.TAILING_SPACES);
 			sqlBuff.append("Select * from " ).append(tableName).append(" where 1!=1");
 			metaData = dbConnParamForMetadata.getMetaData(sqlBuff.toString());
 
 			for (int i = 1; i <= metaData.getColumnCount(); i++)
 			{
 				columnNames.add(metaData.getColumnName(i));
 			}
 		}
 		catch (SQLException sqlExp)
 		{
 			logger.fatal(sqlExp.getMessage(), sqlExp);
 			ErrorKey errorKey = ErrorKey.getErrorKey("db.operation.error");
 			throw new DAOException(errorKey,sqlExp,"AbstractJDBCDAOImpl.java :"+
 					DAOConstants.RS_METADATA_ERROR);
 		}
 
 		return metaData;
 	}
 
 	/**
 	 * This method generates the Insert query.
 	 * @param tableName : Name of the table given to insert query
 	 * @param columnNamesList : List of columns of the table.
 	 * @return query String.
 	 */
 	protected String createInsertQuery(String tableName,List<String> columnNamesList)
 	{
 		StringBuffer query = new StringBuffer("INSERT INTO " + tableName + "(");
 		StringBuffer colValues = new StringBuffer();
 		Iterator<String> columnIterator = columnNamesList.iterator();
 		while (columnIterator.hasNext())
 		{
 			query.append(columnIterator.next());
 			colValues.append(DAOConstants.INDEX_VALUE_OPERATOR).append(DAOConstants.TAILING_SPACES);
 			if (columnIterator.hasNext())
 			{
 				query.append(DAOConstants.SPLIT_OPERATOR).append(DAOConstants.TAILING_SPACES);
 				colValues.append(DAOConstants.SPLIT_OPERATOR);
 			}
 			else
 			{
 				query.append(") values(");
 				colValues.append(") ");
 				query.append(colValues.toString());
 			}
 		}
 
 		return query.toString();
 	}
 
 
 	/**
 	 * This method called to set Number value to PreparedStatement.
 	 * @param stmt : TODO
 	 * @param index : TODO
 	 * @param obj : Object
 	 * @throws SQLException : SQLException
 	 */
 	protected void setNumberColumns(PreparedStatement stmt,
 			int index, Object obj) throws SQLException
 	{
 			if (obj != null	&& obj.toString().equals("##"))
 			{
 				stmt.setObject(index , Integer.valueOf(-1));
 			}
 			else
 			{
 				stmt.setObject(index , obj);
 			}
 	}
 
 	/**
 	 * This method called to set TimeStamp value to PreparedStatement.
 	 * @param stmt :PreparedStatement
 	 * @param index :
 	 * @param obj :
 	 * @return return true if column type is timeStamp.
 	 * @throws SQLException SQLException
 	 */
 	protected boolean isTimeStampColumn(PreparedStatement stmt, int index,Object obj) throws SQLException
 	{
 		boolean isTimeStampColumn = false;
 		Timestamp date = isColumnValueDate(obj);
 		if (date != null)
 		{
 			stmt.setObject(index , date);
 			isTimeStampColumn = true;
 		}
 		return isTimeStampColumn;
 	}
 
 
 	/**
 	 * This method is called to set TinyInt value
 	 * to prepared statement.
 	 * @param stmt : TODO
 	 * @param index :
 	 * @param obj :
 	 * @throws SQLException : SQLException
 	 */
 	private void setTinyIntColumns(PreparedStatement stmt, int index, Object obj)
 			throws SQLException
 	{
 		if (obj != null && (Boolean.parseBoolean(obj.toString())|| obj.equals("1")))
 		{
 			stmt.setObject(index , 1);
 		}
 		else
 		{
 			stmt.setObject(index, 0);
 		}
 	}
 
 	/**
 	 * This method used to set Date values.
 	 * to prepared statement
 	 * @param stmt :TODO
 	 * @param index :
 	 * @param obj :
 	 * @throws SQLException : SQLException
 	 * @throws DAOException : DAOException
 	 */
 	protected void setDateColumns(PreparedStatement stmt,
 			int index,Object obj)
 			throws SQLException, DAOException
 	{
 		if (obj != null && obj.toString().equals("##"))
 		{
 			java.util.Date date = null;
 			try
 			{
 				date = Utility.parseDate("1-1-9999", "mm-dd-yyyy");
 			}
 			catch (ParseException exp)
 			{
 				//TODO have to replace this by parse key
 				ErrorKey errorKey = ErrorKey.getErrorKey("db.operation.error");
 				throw new DAOException(errorKey,exp,"AbstractJDBCDAOImpl.java :");
 			}
 			Date sqlDate = new Date(date.getTime());
 			stmt.setDate(index, sqlDate);
 		}
 	}
 
 
 	/**
 	 * This method checks the TimeStamp value.
 	 * @param obj :
 	 * @return It returns the TimeStamp value
 	 * */
 	private Timestamp isColumnValueDate(Object obj)
 	{
 		Timestamp timestamp = null;
 		try
 		{
 			DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy",Locale.getDefault());
 			formatter.setLenient(false);
 			java.util.Date date;
 			date = formatter.parse(obj.toString());
 			/*
 			 * Recheck if some issues occurs.
 			 */
 			Timestamp timestampInner = new Timestamp(date.getTime());
 			if (obj != null && !obj.toString().equals(""))
 			{
 				timestamp = timestampInner;
 			}
 		}
 		catch (ParseException parseExp)
 		{
 			logger.error(parseExp.getMessage(),parseExp);
 		}
 
 		return timestamp;
 	}
 
 	/* @see edu.wustl.dao.DAO#setConnectionManager(edu.wustl.dao.connectionmanager.IConnectionManager)
 	 */
 
 	/**
 	 * This method will be called to set connection Manager object.
 	 * @param connectionManager : Connection Manager.
 	 */
 	public void setConnectionManager(IConnectionManager connectionManager)
 	{
 		this.connectionManager = connectionManager;
 	}
 
 	/**
 	 * This method will be called to get connection Manager object.
 	 * @return IConnectionManager: Connection Manager.
 	 */
 	public IConnectionManager getConnectionManager()
 	{
 		return connectionManager;
 	}
 
 	/**
 	 * This method will be called to get connection object.
 	 * @return Connection: Connection object.
 	 */
 	protected Connection getConnection()
 	{
 		return connection;
 	}
 
 
 	/**@see edu.wustl.dao.JDBCDAO#getActivityStatus(java.lang.String, java.lang.Long)
 	 * @param sourceObjectName :
 	 * @param indetifier :
 	 * @throws DAOException :
 	 * @return Activity status :
 	 *//*
 
 	public String getActivityStatus(String sourceObjectName, Long indetifier) throws DAOException
 	{
 		throw new DAOException(DAOConstants.METHOD_WITHOUT_IMPLEMENTATION);
 	}*/
 
 	/**
 	 * @param obj :
 	 * @param oldObj :
 	 * @param sessionDataBean :
 	 * @param isAuditable :
 	 * @throws DAOException :
 	 */
 	public void audit(Object obj, Object oldObj, SessionDataBean sessionDataBean,
 			boolean isAuditable) throws DAOException
 	{
 		ErrorKey errorKey = ErrorKey.getErrorKey("dao.method.without.implementation");
 		throw new DAOException(errorKey,new Exception(),"AbstractJDBCDAOImpl.java :");
 	}
 
 	/**
 	 * @param obj :
 	 * @throws DAOException :
 	 */
 	public void delete(Object obj) throws DAOException
 	{
 		ErrorKey errorKey = ErrorKey.getErrorKey("dao.method.without.implementation");
 		throw new DAOException(errorKey,new Exception(),"AbstractJDBCDAOImpl.java :");
 	}
 
 	/**
 	 * @param tableName :
 	 * @param whereColumnName :
 	 * @param whereColumnValues :
 	 * @throws DAOException :
 	 */
 	public void disableRelatedObjects(String tableName, String whereColumnName,
 			Long[] whereColumnValues) throws DAOException
 	{
 		ErrorKey errorKey = ErrorKey.getErrorKey("dao.method.without.implementation");
 		throw new DAOException(errorKey,new Exception(),"AbstractJDBCDAOImpl.java :");
 	}
 
 	/**
 	 * @param obj :
 	 * @param sessionDataBean :
 	 * @param isAuditable :
 	 * @param isSecureInsert :
 	 * @throws DAOException :
 	 */
 	public void insert(Object obj, SessionDataBean sessionDataBean,
 			boolean isAuditable, boolean isSecureInsert)
 			throws DAOException
 	{
 		ErrorKey errorKey = ErrorKey.getErrorKey("dao.method.without.implementation");
 		throw new DAOException(errorKey,new Exception(),"AbstractJDBCDAOImpl.java :");
 	}
 
 	/**
 	 * @see edu.wustl.common.dao.DAO#retrieveAttribute(java.lang.Class, java.lang.Long, java.lang.String)
 	 * @param objClass : Class name
 	 * @param identifier : Identifier of object
 	 * @param attributeName : Attribute Name to be fetched
 	 * @param columnName : where clause column field.
 	 * @return It will return the Attribute of the object having given identifier
 	 * @throws DAOException : DAOException
 	 */
 	public Object retrieveAttribute(Class objClass, Long identifier,
 			String attributeName,String columnName) throws DAOException
 	{
 		ErrorKey errorKey = ErrorKey.getErrorKey("dao.method.without.implementation");
 		throw new DAOException(errorKey,new Exception(),"AbstractJDBCDAOImpl.java :");
 	}
 
 
 	/**
 	 * @param obj :
 	 * @throws DAOException :
 	 */
 	public void update(Object obj) throws DAOException
 	{
 		ErrorKey errorKey = ErrorKey.getErrorKey("dao.method.without.implementation");
 		throw new DAOException(errorKey,new Exception(),"AbstractJDBCDAOImpl.java :");
 	}
 
 	/**
 	 * @param sourceObjectName :
 	 * @param identifier :
 	 * @return Object :
 	 * @throws DAOException :
 	 */
 	public Object retrieve(String sourceObjectName, Long identifier)
 			throws DAOException
 	{
 		ErrorKey errorKey = ErrorKey.getErrorKey("dao.method.without.implementation");
 		throw new DAOException(errorKey,new Exception(),"AbstractJDBCDAOImpl.java :");
 	}
 
 	/**
 	 * @param excp : Exception Object.
 	 * @param applicationName : Name of the application.
 	 * @return : It will return the formated messages.
 	 * @throws DAOException : DAO exception.
 	 */
 	public String formatMessage(Exception excp, String applicationName)
 	throws DAOException
 	{
 		ErrorKey errorKey = ErrorKey.getErrorKey("dao.method.without.implementation");
 		throw new DAOException(errorKey,new Exception(),"AbstractJDBCDAOImpl.java :");
 	}
 
 }
