 package org.liveSense.api.sql;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.Statement;
 import java.sql.Types;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import javax.persistence.Column;
 import javax.persistence.Id;
 import javax.sql.DataSource;
 
 import org.apache.commons.dbcp.BasicDataSource;
 import org.apache.commons.dbutils.BasicRowProcessor;
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.ResultSetHandler;
 import org.apache.commons.dbutils.handlers.BeanListHandler;
 import org.apache.commons.lang.StringUtils;
 import org.liveSense.api.beanprocessors.DbStandardBeanProcessor;
 import org.liveSense.api.sql.exceptions.SQLException;
 import org.liveSense.misc.queryBuilder.QueryBuilder;
 import org.liveSense.misc.queryBuilder.SimpleSQLQueryBuilder;
 import org.liveSense.misc.queryBuilder.criterias.EqualCriteria;
 import org.liveSense.misc.queryBuilder.exceptions.QueryBuilderException;
 import org.liveSense.misc.queryBuilder.jdbcDriver.JdbcDrivers;
 import org.liveSense.misc.queryBuilder.operands.OperandSource;
 import org.liveSense.misc.queryBuilder.operators.AndOperator;
 
 
 /**
  * This class provides basic functionalities of SQL for javax.persistence annotated beans.
  * 
  * @param <T> - The Bean class is used for
  */
 public abstract class SQLExecute<T> {
 
 	
 	
 	//SUBCLASSES
 	public enum StatementType {
 		SELECT, LOCK, INSERT, UPDATE, DELETE, INSERT_SELECT, PROCEDURE
 	}
 	
 	
 	
 	//CONSTS
 	private static final String[] BLOBNAMES = new String[] {"BLOB", "CLOB", "LONGTEXT", "LONGVARCHAR"};//array element must be sorted! (due to binarySearch)
 	private static final String THIS_TYPE_OF_JDBC_DIALECT_IS_NOT_IMPLEMENTED = "This type of JDBC dialect is not implemented";
 	private static final String CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION = "Class does not contain javax.persistence.Entity or javax.persistence.Entity.Table annotation";
 	private static final String CLASS_DOES_NOT_HAVE_ID_ANNOTATION = "Entity does not contain javax.persistence.Id annotation";
 	private static final String ENTITY_IS_NULL = "Entity is null";
 	private static final String BASIC_DATASOURCE_OBJECT_NEEDED = "No org.apache.commons.dbcp.BasicDataSource object is defined";
 	private static final String ENTITY_TYPE_MISMATCH = "Entity class type mismatch";
 	private static final String COLUMN_NAME_IS_UNDEFINED = "Column name is undefined";
 	private static final String COLUMN_DEFINITION_IS_UNDEFINED = "Column definition is undefined";
 	private static final String WRONG_PREPARED_STATEMENT = "Wrong prepared statement.";
 	private static final String THE_STATEMENT_IS_NOT_PREPARED = "The statement is not prepared";
 	
 	
 	
 	//FIELDS
 	//create parameters
 	private DataSource dataSource;
 	@SuppressWarnings("rawtypes")
 	private Class clazz;
 	//prepare
 	private StatementType preparedStatementType = null;
 	@SuppressWarnings("rawtypes")
 	private Class preparedStatementClass = null;
 	private Connection preparedConnection = null;
 	private NamedParameterProcessor preparedNPP = null;
 	private List<String> preparedNamedParameters = new ArrayList<String>();
 	private int preparedSQLParametersCount = 0;
 	private ArrayList<String> preparedFields = new ArrayList<String>();
 	private String preparedSQL = "";
 	private PreparedStatement preparedStatement = null;
 	private Map<String, Integer> preparedProcedureOutputParams = new HashMap<String, Integer>();
 	//log
 	private Map<String, Object> lastNamedParameters = new HashMap<String, Object>();
 	private ArrayList<Object> lastSQLParameters = new ArrayList<Object>();
 	//builder
 	protected QueryBuilder builder;
 
 	
 	
 	//GETTERS (NO SETTERS)
 	//create parameters
 	public DataSource getDataSource() {return dataSource;}
 	@SuppressWarnings("rawtypes")
 	public Class getClazz() {return clazz;}
 	//prepare
 	public StatementType getPreparedStatementType() {return preparedStatementType;}
 	@SuppressWarnings("rawtypes")
 	public Class getPreparedStatementClass() {return preparedStatementClass;}
 	public Connection getPreparedConnection() {return preparedConnection;}
 	public List<String> getPreparedNamedParameters() {return preparedNamedParameters;}
 	public int getPreparedSQLParametersCount() {return preparedSQLParametersCount;}
 	public ArrayList<String> getPreparedFields() {return preparedFields;}
 	public String getPreparedSQL() {return preparedSQL;}
 	public PreparedStatement getPreparedStatement() {return preparedStatement;}
 	public Map<String, Integer> getPreparedProcedureOutputParams() {return preparedProcedureOutputParams;}
 	//log
 	public Map<String, Object> getLastNamedParameters() {return lastNamedParameters;}
 	public ArrayList<Object> getLastSQLParameters() {return lastSQLParameters;}
 
 	
 
 	//CONSTRUCTOR (FACTORY)
 	/**
 	 * Returns RDMS dependent SQLExecute. (Apache DBCP required).
 	 * The currently supported engines are: MYSQL, HSQLDB, FIREBIRD, ORACLE
 	 * 
 	 * @param ds DataSource
 	 * @return SQL Execute Object (optimized for dialect)
 	 * @throws SQLException
 	 */
 	public static SQLExecute<?> getExecuterByDataSource(DataSource ds) throws SQLException {
 		return getExecuterByDataSource(ds, null);
 	}
 	
 	
 
 
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#getExecuterByDataSource(DataSource ds)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public static SQLExecute<?> getExecuterByDataSource(DataSource ds, Class clazz) throws SQLException {
 		String driverClass = getDriverClassByDataSource(ds);
 		
 		SQLExecute<?> executer = null;
 		if (driverClass.equals(JdbcDrivers.MYSQL.getDriverClass())) executer = new MySqlExecute(-1);
 		else if (driverClass.equals(JdbcDrivers.HSQLDB.getDriverClass())) executer = new HSqlDbExecute(-1);
 		else if (driverClass.equals(JdbcDrivers.FIREBIRD.getDriverClass())) executer = new FirebirdExecute(-1);
 		else if (driverClass.equals(JdbcDrivers.ORACLE.getDriverClass())) executer = new OracleExecute(-1);
 		else if (driverClass.equals(JdbcDrivers.ORACLE2.getDriverClass())) executer = new OracleExecute(-1);
 
 		else throw new SQLException(THIS_TYPE_OF_JDBC_DIALECT_IS_NOT_IMPLEMENTED+": "+driverClass);
 		
 		executer.dataSource = ds;
 		executer.clazz = clazz;
 		return executer;
 	}
 	
 	
 	
 	//METHODS - static
 	/**
 	 * Returns the DataSource driver class name
 	 * 
 	 * @param ds DataSource (have to be instance of org.apache.commons.dbcp.BasicDataSource)
 	 * @return The driver class name
 	 * @throws SQLException
 	 */
 	public static String getDriverClassByDataSource(DataSource ds) throws SQLException {
 		if (!(ds instanceof BasicDataSource)) throw new SQLException(BASIC_DATASOURCE_OBJECT_NEEDED);
 		return ((BasicDataSource)ds).getDriverClassName();
 	}
 
 	/**
 	 * Returns the DataSource connection URL String
 	 * 
 	 * @param ds DataSource (have to be instance of org.apache.commons.dbcp.BasicDataSource)
 	 * @return The connection URL
 	 * @throws SQLException
 	 */
 	public static String getDataSourceUrlByDataSource(DataSource ds) throws SQLException {
 		if (!(ds instanceof BasicDataSource)) throw new SQLException(BASIC_DATASOURCE_OBJECT_NEEDED);
 		return ((BasicDataSource)ds).getUrl();
 	}
 	
 	
 	
 	//METHODS - abstract
 	/**
 	 * Add where clause for select. The method depends on the type of SQL dialect
 	 * @param helper
 	 * @return
 	 * @throws SQLException
 	 * @throws QueryBuilderException
 	 */
 	public abstract ClauseHelper addWhereClause(ClauseHelper helper) throws SQLException, QueryBuilderException;
 
 	/**
 	 * Add limit clause for select. The method depends on the type of SQL dialect
 	 * @param helper
 	 * @return
 	 * @throws SQLException
 	 * @throws QueryBuilderException
 	 */
 	public abstract ClauseHelper addLimitClause(ClauseHelper helper) throws SQLException, QueryBuilderException;
 
 	/**
 	 * Add order by clause for select. The method depends on the type of SQL dialect
 	 * @param helper
 	 * @return
 	 * @throws SQLException
 	 * @throws QueryBuilderException
 	 */
 	public abstract ClauseHelper addOrderByClause(ClauseHelper helper) throws SQLException, QueryBuilderException;
 
 	/**
 	 * It builds the select statement from the base query.
 	 * @param clazz Class used by AnnotationHelper
 	 * @return The final SQL statement
 	 * @throws SQLException
 	 * @throws QueryBuilderException
 	 */
 	@SuppressWarnings("rawtypes")
 	public abstract String getSelectQuery(Class clazz) throws SQLException, QueryBuilderException;
 		
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL table alias name
 	 * @see {@link SQLExecute#getSelectQuery(Class clazz)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public abstract String getSelectQuery(Class clazz, String tableAlias) throws SQLException, QueryBuilderException;
 	
 	/**
 	 * It builds the lock (select) statement from the base query.
 	 * @param clazz Class used by AnnotationHelper Class of the bean
 	 * @return The final SQL statement
 	 * @throws SQLException
 	 * @throws QueryBuilderException
 	 */
 	@SuppressWarnings("rawtypes")
 	public abstract String getLockQuery(Class clazz) throws SQLException, QueryBuilderException;
 		
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL table alias name
 	 * @see {@link SQLExecute#getLockQuery(Class clazz)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public abstract String getLockQuery(Class clazz, String tableAlias) throws SQLException, QueryBuilderException;
 	
 	public abstract String getBlobName();
 
 
 
 	//METHODS - private
 	private void clearPrepare() {
 		
 		preparedStatementType = null;
 		preparedStatementClass = null;
 		preparedConnection = null;
 		preparedNPP = null;
 		preparedNamedParameters.clear();
 		preparedSQLParametersCount = 0;
 		preparedFields.clear();
 		preparedSQL = "";
 		preparedStatement = null;
 		preparedProcedureOutputParams.clear();
 	}
 	
 	private void clearLastStatement() {
 			
 		lastNamedParameters.clear();
 		lastSQLParameters.clear();
 	}
 	
 	@SuppressWarnings("rawtypes")
 	private void checkPrepare(
 		StatementType type,	Class clazz) throws SQLException {
 		
 		if (preparedStatementType == null ||
 			preparedConnection == null ||
 			preparedSQL == "" ||
 			preparedStatement == null)
 			throw new SQLException(THE_STATEMENT_IS_NOT_PREPARED);
 			
 		if (preparedStatementType != type)
 			throw new SQLException(WRONG_PREPARED_STATEMENT);
 		
 		if (clazz != preparedStatementClass)
 			throw new SQLException(ENTITY_TYPE_MISMATCH);
 	}
 		
 	@SuppressWarnings("rawtypes")
 	private void prepare(StatementType statementType, Connection connection, Class clazz, String sql, List<String> fields) throws SQLException, java.sql.SQLException {
 		
 		clearPrepare();
 		clearLastStatement();
 		
 		Class localClass = clazz;
 		if (localClass == null)	localClass = this.clazz;
 		
 		preparedStatementType = statementType;
 		preparedStatementClass = localClass;
 		preparedConnection = connection;
 		preparedNPP = new NamedParameterProcessor(sql);
 		preparedNamedParameters.addAll(preparedNPP.getParameters().keySet());
 		if (fields != null)
 			preparedFields.addAll(fields);
 		preparedSQL = preparedNPP.getSqlStatement();
 		if (preparedStatementType == StatementType.PROCEDURE) {
 			preparedStatement = connection.prepareCall(preparedSQL);
 			preparedSQLParametersCount = preparedNamedParameters.size();
 		}
 		else {
 			preparedStatement = connection.prepareStatement(preparedSQL);
 			preparedSQLParametersCount = preparedStatement.getParameterMetaData().getParameterCount();
 		}
 	}
 	
 	
 	
 	//METHODS - public
 	//prepare - select >>>
 	/**
 	 * Prepare a statement for query entities from database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @param builder Query builder
 	 * @throws Exception
 	 */
 	public void prepareQueryStatement(
 		Connection connection, QueryBuilder builder) throws Exception {
 		
 		prepareQueryStatement(connection, null, "", builder);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#prepareQueryStatement(Connection connection, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public void prepareQueryStatement(
 		Connection connection, Class clazz, QueryBuilder builder) throws Exception {
 		
 		prepareQueryStatement(connection, clazz, "", builder);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL table alias name
 	 * @see {@link SQLExecute#prepareQueryStatement(Connection connection, Class clazz, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public void prepareQueryStatement(
 		Connection connection, Class clazz, String tableAlias, QueryBuilder builder) throws Exception {
 		
 		Class localClass = clazz;
 		if (localClass == null)	localClass = this.clazz;
 		
 		this.builder = builder;
 		String sql = getSelectQuery(localClass, tableAlias);
 		
 		prepare(StatementType.SELECT, connection, localClass, sql, null);
 	}
 	
 	/**
 	 * Prepare a statement for query one entity from database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareQueryStatement(
 		Connection connection) throws Exception {
 		
 		prepareQueryStatement(connection, (Class)null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#prepareQueryStatement(Connection connection)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareQueryStatement(
 		Connection connection, Class clazz) throws Exception {
 		
 		Class localClass = clazz;
 		if (localClass == null)	localClass = this.clazz;
 			
 		String idColumn = AnnotationHelper.getIdColumnName(localClass);
 		if (idColumn == null || idColumn.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		
 		QueryBuilder builder = new SimpleBeanSQLQueryBuilder(localClass);
 		builder.setWhere(new AndOperator(new EqualCriteria<OperandSource>(idColumn, new OperandSource("", ":"+idColumn, false))));
 		
 		prepareQueryStatement(connection, localClass, "", builder);
 	}
 	
 	
 	
 	//prepare - lock >>>
 	/**
 	 * Prepare a statement for lock entities in database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param Connection SQL Connection
 	 * @param builder Query builder
 	 * @throws Exception
 	 */
 	public void prepareLockStatement(
 		Connection connection, QueryBuilder builder) throws Exception {
 		
 		prepareLockStatement(connection, null, "", builder);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#prepareLockStatement(Connection connection, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public void prepareLockStatement(
 		Connection connection, Class clazz, QueryBuilder builder) throws Exception {
 		
 		prepareLockStatement(connection, clazz, "", builder);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL table alias name
 	 * @see {@link SQLExecute#prepareLockStatement(Connection connection, Class clazz, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public void prepareLockStatement(
 		Connection connection, Class clazz, String tableAlias, QueryBuilder builder) throws Exception {
 
 		Class localClass = clazz;
 		if (localClass == null)	localClass = this.clazz;
 
 		this.builder = builder;
 		String sql = getLockQuery(localClass, tableAlias);
 		
 		prepare(StatementType.LOCK, connection, localClass, sql, null);
 	}
 	
 	/**
 	 * Prepare a statement for lock one entity in database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareLockStatement(
 		Connection connection) throws Exception {
 		
 		prepareLockStatement(connection, (Class)null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#prepareLockStatement(Connection connection)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareLockStatement(
 		Connection connection, Class clazz) throws Exception {
 		
 		Class localClass = clazz;
 		if (localClass == null)	localClass = this.clazz;
 			
 		String idColumn = AnnotationHelper.getIdColumnName(localClass);
 		if (idColumn == null || idColumn.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		
 		QueryBuilder builder = new SimpleBeanSQLQueryBuilder(localClass);
 		builder.setWhere(new AndOperator(new EqualCriteria<OperandSource>(idColumn, new OperandSource("", ":"+idColumn, false))));
 		
 		prepareLockStatement(connection, localClass, "", builder);
 	}
 	
 	
 	
     //prepare - insert >>>
 	/**
 	 * Prepare a statement for insert one entity into database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareInsertStatement(
 		Connection connection) throws Exception {
 		
 		prepareInsertStatement(connection, (Class)null, (List<String>)null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#prepareInsertStatement(Connection connection)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareInsertStatement(
 		Connection connection, Class clazz) throws Exception {
 		
 		prepareInsertStatement(connection, clazz, (List<String>)null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields used in SQL
 	 * @see {@link SQLExecute#prepareInsertStatement(Connection connection, Class clazz)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareInsertStatement(
 		Connection connection, Class clazz, String[] fields) throws Exception {
 		
 		List<String> list = new ArrayList<String>(Arrays.asList(fields));
 		prepareInsertStatement(connection,clazz,list);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields used in SQL
 	 * @see {@link SQLExecute#prepareInsertStatement(Connection connection, Class clazz)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public void prepareInsertStatement(
 		Connection connection, Class clazz, List<String> fields) throws Exception {
 		
 		Class localClass = clazz;
 		if (localClass == null)	localClass = this.clazz;
 		
 		this.builder = null;
 			
 		Map<String, String> annotationMap = AnnotationHelper.getAnnotationMap(localClass, fields, true);
 		String tableName = AnnotationHelper.getTableName(localClass);
 		if (tableName == null || tableName.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 				
 		StringBuffer sb = new StringBuffer();
 		StringBuffer sb2 = new StringBuffer();
 		sb.append("INSERT INTO "+tableName+" (");
 		sb2.append("(");
 		boolean first = true;
 		for (String columnName : annotationMap.values()) {
 			if (!first) {sb.append(","); sb2.append(",");} else first = false;
 			sb.append(columnName);
 			sb2.append("?");
 		}
 		
 		String sql = sb.toString()+") VALUES "+sb2.toString()+")";
 		
 		prepare(StatementType.INSERT, connection, localClass, sql, new ArrayList<String>(annotationMap.keySet()));
 	}
 	
 	
 	
 	//prepare - update >>>
 	/**
 	 * Prepare a statement for update entities in database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @param condition SQL condition
 	 * @throws Exception
 	 */
 	public void prepareUpdateStatement(
 		Connection connection, Object condition) throws Exception {
 		
 		prepareUpdateStatement(connection, null, condition);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#prepareUpdateStatement(Connection connection, Object condition)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(
 		Connection connection, Class clazz, Object condition) throws Exception {
 		
 		prepareUpdateStatement(connection, clazz, (List<String>)null, condition);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields used in SQL
 	 * @see {@link SQLExecute#prepareUpdateStatement(Connection connection, Class clazz, Object condition)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(
 		Connection connection, Class clazz, String[] fields, Object condition) throws Exception {
 		
 		List<String> list = new ArrayList<String>(Arrays.asList(fields));
 		prepareUpdateStatement(connection, clazz, list, condition);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields used in SQL
 	 * @see {@link SQLExecute#prepareUpdateStatement(Connection connection, Class clazz, Object condition)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(
 		Connection connection, Class clazz, List<String> fields, Object condition) throws Exception {
 		
 		prepareUpdateStatement(connection, clazz, "", fields, condition);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields used in SQL
 	 * @see {@link SQLExecute#prepareUpdateStatement(Connection connection, Class clazz, List<String> fields, Object condition)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(
 		Connection connection, Class clazz, String tableAlias, List<String> fields, Object condition) throws Exception {
 		
 		Class localClass = clazz;
 		if (localClass == null)	localClass = this.clazz;
 		
 		this.builder = null;
 		
 		Map<String, String> columns = AnnotationHelper.getAnnotationMap(localClass, fields, false);
 		String tableName = AnnotationHelper.getTableName(localClass);
 		if (tableName == null || tableName.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		String tableAlias2 = tableAlias;
 		if (tableAlias2.length() != 0)
 			tableAlias2 = tableAlias2 + ".";
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append("UPDATE "+tableName+" "+tableAlias+" SET ");
 		boolean first = true;
 		for (String columnName : columns.values()) {
 			if (!first) {sb.append(",");} else first = false;
 			sb.append(tableAlias2+columnName+" = ?");
 		}
 		SimpleBeanSQLQueryBuilder builder = new SimpleBeanSQLQueryBuilder(localClass);
 		if (condition != null)
 			sb.append(" WHERE " + builder.buildWhere(localClass, condition));
 				
 		String sql = sb.toString();
 		
 		prepare(StatementType.UPDATE, connection, localClass, sql, new ArrayList<String>(columns.keySet()));
 	}
 	
 	/**
 	 * Prepare a statement for update one entity in database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(
 		Connection connection) throws Exception {
 		
 		prepareUpdateStatement(connection, (Class)null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#prepareUpdateStatement(Connection connection, Class clazz)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(
 		Connection connection, Class clazz) throws Exception {
 		
 		prepareUpdateStatement(connection, clazz, (List<String>)null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields used in SQL
 	 * @see {@link SQLExecute#prepareUpdateStatement(Connection connection, Class clazz)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(
 		Connection connection, Class clazz, String[] fields) throws Exception {
 		
 		List<String> list = new ArrayList<String>(Arrays.asList(fields));
 		prepareUpdateStatement(connection, clazz, list);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields used in SQL
 	 * @see {@link SQLExecute#prepareUpdateStatement(Connection connection, Class clazz)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(
 		Connection connection, Class clazz, List<String> fields) throws Exception {
 		
 		Class localClass = clazz;
 		if (localClass == null)	localClass = this.clazz;
 			
 		String idColumn = AnnotationHelper.getIdColumnName(localClass);
 		if (idColumn == null || idColumn.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		
 		Object condition = new AndOperator(new EqualCriteria<OperandSource>(idColumn, new OperandSource("", ":"+idColumn, false)));
 		
 		prepareUpdateStatement(connection, localClass, fields, condition);
 	}
 
 	
 	//prepare - delete >>>
 	/**
 	 * Prepare a statement for delete entities from database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @param condition SQL condition
 	 * @throws Exception
 	 */
 	public void prepareDeleteStatement(
 		Connection connection, Object condition) throws Exception {
 		
 		prepareDeleteStatement(connection, null, condition);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#prepareDeleteStatement(Connection connection, Object condition)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareDeleteStatement(
 		Connection connection, Class clazz, Object condition) throws Exception {
 		
 		prepareDeleteStatement(connection, clazz, "", condition);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL table alias name
 	 * @see {@link SQLExecute#prepareDeleteStatement(Connection connection, Class clazz, Object condition)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareDeleteStatement(
 		Connection connection, Class clazz, String tableAlias, Object condition) throws Exception {
 						
 		Class localClass = clazz;
 		if (localClass == null)	localClass = this.clazz;
 		
 		this.builder = null;
 		
 		String tableName = AnnotationHelper.getTableName(localClass);
 		if (tableName == null || tableName.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append("DELETE FROM "+tableName+" "+tableAlias+" ");
 		SimpleBeanSQLQueryBuilder builder = new SimpleBeanSQLQueryBuilder(localClass);
 		if (condition != null)
 			sb.append(" WHERE " + builder.buildWhere(localClass, condition));
 				
 		String sql = sb.toString();
 		
 		prepare(StatementType.DELETE, connection, localClass, sql, null);
 	}
 	
 	/**
 	 * Prepare a statement for delete one entity in database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareDeleteStatement(
 		Connection connection) throws Exception {
 		
 		prepareDeleteStatement(connection, (Class)null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#prepareDeleteStatement(Connection connection)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareDeleteStatement(
 		Connection connection, Class clazz) throws Exception {
 				
 		Class localClass = clazz;
 		if (localClass == null)	localClass = this.clazz;
 			
 		String idColumn = AnnotationHelper.getIdColumnName(localClass);
 		if (idColumn == null || idColumn.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		
 		Object condition = new AndOperator(new EqualCriteria<OperandSource>(idColumn, new OperandSource("", ":"+idColumn, false)));
 		
 		prepareDeleteStatement(connection, localClass, condition);
 	}
 	
 	
 	
 	//prepare - insert-select >>>
 	/**
 	 * Prepare a statement for insert entities into database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @param insertClass Class used by AnnotationHelper
 	 * @param insertFields list of fields used in SQL
 	 * @param selectClass Class used by AnnotationHelper
 	 * @param tableAlias SQL table alias name
 	 * @param selectFields list of fields used in SQL
 	 * @param selectCondition SQL condition
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareInsertSelectStatement(
 		Connection connection,
 		Class insertClass, String[] insertFields,
 		Class selectClass, String tableAlias, String[] selectFields, Object selectCondition)
 		throws java.sql.SQLException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, QueryBuilderException{
 				
 		List<String> list1 = new ArrayList<String>(Arrays.asList(insertFields));
 		List<String> list2 = new ArrayList<String>(Arrays.asList(selectFields));
 		
 		prepareInsertSelectStatement(connection, insertClass, list1, selectClass, tableAlias, list2, selectCondition);
 	}
 		
 	/**
 	 * {@inheritDoc}
 	 * @see {@link SQLExecute#prepareInsertSelectStatement(Connection connection, Class insertClass, String[] insertFields,	Class selectClass, String tableAlias, String[] selectFields, Object selectCondition)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareInsertSelectStatement(
 		Connection connection,
 		Class insertClass, List<String> insertFields,
 		Class selectClass, String tableAlias, List<String> selectFields, Object selectCondition)
 		throws java.sql.SQLException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, QueryBuilderException{
 			
 		Class localClassInsert = insertClass;
 		if (localClassInsert == null) localClassInsert = this.clazz;
 		
 		Class localClassSelect = selectClass;
 		if (localClassSelect == null) localClassSelect = this.clazz;
 		
 		this.builder = null;
 		
 		String insertTableName = AnnotationHelper.getTableName(localClassInsert);
 		if (StringUtils.isEmpty(insertTableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		String selectTableName = AnnotationHelper.getTableName(localClassSelect);
 		if (StringUtils.isEmpty(selectTableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		//insert
 		ArrayList<String> insertColumns = AnnotationHelper.getClassColumnNames(localClassInsert, insertFields, true);
 		StringBuffer sb = new StringBuffer();
 		sb.append("INSERT INTO "+insertTableName+" (");
 		boolean first = true;
 		for (String columnName : insertColumns) {
 			if (!first) {
 				sb.append(",");
 			}
 			else first = false;
 			sb.append(columnName);
 		}
 		sb.append(")");
 		String insert = sb.toString();
 		
 		String tableAlias2 = tableAlias;
 		if (tableAlias2 != "")
 			tableAlias2 = tableAlias2+".";
 		
 		//select
 		ArrayList<String> selectColumns = AnnotationHelper.getClassColumnNames(localClassSelect, selectFields, true);
 		sb = new StringBuffer();
 		first = true;
 		for (String columnName : selectColumns) {
 			if (!first) {
 				sb.append(",");
 			}
 			else first = false;
 			sb.append(tableAlias2+columnName);
 		}
 		this.builder = new SimpleSQLQueryBuilder("SELECT " + sb.toString() +" FROM "+selectTableName);
 		if (selectCondition != null) {
 			this.builder.setWhere(selectCondition);
 		}
 		String select = getSelectQuery(localClassSelect, tableAlias).replace("*", sb.toString());
 		
 		String sql = insert +"\n"+ select;
 		
 		prepare(StatementType.INSERT_SELECT, connection, null, sql, null);
 	}
 	
 	
 	
 	//prepare - stored procedure >>>
 	public void prepareExecuteProcedure(
 		Connection connection, String procName) throws Exception {
 		
 		this.builder = null;
 		
 		Map<String, Integer> procedureOutputParams = new HashMap<String, Integer>();
 		
 		List<String> inParamNames = new ArrayList<String>();
 		List<Integer> outParamTypes = new ArrayList<Integer>();
 					
 		int out = 1;
 		int in = 1;
 		ResultSet rs = connection.getMetaData().getProcedureColumns(null, null, procName, null);
 		while (rs.next()) {
 			if ((rs.getInt(5) == java.sql.DatabaseMetaData.procedureColumnOut) ||
 				(rs.getInt(5) == java.sql.DatabaseMetaData.procedureColumnInOut)) {
 				procedureOutputParams.put(rs.getString(4), out);
 				outParamTypes.add(rs.getInt(6));
 				out++;
 			}
 			if ((rs.getInt(5) == java.sql.DatabaseMetaData.procedureColumnIn) ||
 				(rs.getInt(5) == java.sql.DatabaseMetaData.procedureColumnInOut)) {
 				inParamNames.add(rs.getString(4));
 				in++;
 			}
 		}
 	
 		StringBuffer sb = new StringBuffer("EXECUTE PROCEDURE " + procName);
 		if (inParamNames.size() != 0) {
 			sb.append("(");
 			for (String paramName : inParamNames) {
 				sb.append(":"+paramName+",");
 			}
 			sb.deleteCharAt(sb.length() - 1);
 			sb.append(")");
 		}
 		String sql = sb.toString();
 		
 		prepare(StatementType.PROCEDURE, connection, null, sql, null);
 		
 		preparedProcedureOutputParams.putAll(procedureOutputParams);
 				
 		out = 1;
 		for (Integer paramType : outParamTypes) {
 			((CallableStatement) preparedStatement).registerOutParameter(out, paramType);
 			out ++;
 		}
 	};
 	
 	
 	
 	//run prepared - select >>>
 	/**
 	 * Query entities with prepared statement.
 	 * 
 	 * @param params Parameters for SQL conditions
 	 * @return list of beans
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	public List<T> queryEntitiesWithPreparedStatement(Map<String, Object> params)
 		throws SQLException, java.sql.SQLException {
 		
 		clearLastStatement();
 		checkPrepare(StatementType.SELECT, preparedStatementClass);
 		
 		List<Object> paramValues = preparedNPP.getSQLParameters(params);
 		if (params != null)
 			lastNamedParameters.putAll(params);
 		lastSQLParameters.addAll(paramValues);
 		
 		QueryRunner run = new QueryRunner();
 		ResultSetHandler<List<T>> rh = new BeanListHandler<T>(preparedStatementClass, new BasicRowProcessor(new DbStandardBeanProcessor()));
 			
 		Object[] queryParams = null;
 		if (lastSQLParameters.size() != 0)
 			queryParams = lastSQLParameters.toArray();
 		
 		return run.query(preparedConnection, preparedSQL, rh, queryParams);
 	}
 	
 	/**
 	 * Query one entity with prepared statement.
 	 * 
 	 * @param entity The bean
 	 * @return bean
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	public T queryEntityWithPreparedStatement(T entity)
 		throws SQLException, java.sql.SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
 		
 		clearLastStatement();
 		checkPrepare(StatementType.SELECT, entity.getClass());
 		
 		Map<String, Object> params = new HashMap<String, Object>();
 		Map<String, Object> map = AnnotationHelper.getObjectAsMap(entity);
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		params.put(idColumn, map.get(idColumn));
 		
 		List<Object> paramValues = preparedNPP.getSQLParameters(params);
 		if (params != null)
 			lastNamedParameters.putAll(params);
 		lastSQLParameters.addAll(paramValues);
 			
 		QueryRunner run = new QueryRunner();
 		ResultSetHandler<List<T>> rh = new BeanListHandler<T>(preparedStatementClass, new BasicRowProcessor(new DbStandardBeanProcessor()));
 			
 		Object[] queryParams = null;
 		if (lastSQLParameters.size() != 0)
 			queryParams = lastSQLParameters.toArray();
 		
 		List<T> list = run.query(preparedConnection, preparedSQL, rh, queryParams);
 		if (list.size() == 0)
 			return null;
 		else
 			return list.get(0);
 	}
 	
 	
 	
 	//run prepared - lock >>>
 	/**
 	 * Lock entities with prepared statement.
 	 * 
 	 * @param params Parameters for SQL conditions
 	 * @return list of beans
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	public List<T> lockEntitiesWithPreparedStatement(Map<String, Object> params)
 		throws SQLException, java.sql.SQLException {
 		
 		clearLastStatement();
 		checkPrepare(StatementType.LOCK, preparedStatementClass);
 		
 		List<Object> paramValues = preparedNPP.getSQLParameters(params);
 		if (params != null)
 			lastNamedParameters.putAll(params);
 		lastSQLParameters.addAll(paramValues);
 		
 		QueryRunner run = new QueryRunner();
 		ResultSetHandler<List<T>> rh = new BeanListHandler<T>(preparedStatementClass, new BasicRowProcessor(new DbStandardBeanProcessor()));
 			
 		Object[] queryParams = null;
 		if (lastSQLParameters.size() != 0)
 			queryParams = lastSQLParameters.toArray();
 		
 		return run.query(preparedConnection, preparedSQL, rh, queryParams);
 	}
 		
 	/**
 	 * Lock one entity with prepared statement.
 	 * 
 	 * @param entity The bean
 	 * @return bean
 	 * @throws Exception
 	 */
 	@SuppressWarnings("unchecked")
 	public T lockEntityWithPreparedStatement(T entity)
 		throws SQLException, java.sql.SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
 		
 		clearLastStatement();
 		checkPrepare(StatementType.LOCK, entity.getClass());
 		
 		Map<String, Object> params = new HashMap<String, Object>();
 		Map<String, Object> map = AnnotationHelper.getObjectAsMap(entity);
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		params.put(idColumn, map.get(idColumn));
 		
 		List<Object> paramValues = preparedNPP.getSQLParameters(params);
 		if (params != null)
 			lastNamedParameters.putAll(params);
 		lastSQLParameters.addAll(paramValues);
 				
 		QueryRunner run = new QueryRunner();
 		ResultSetHandler<List<T>> rh = new BeanListHandler<T>(preparedStatementClass, new BasicRowProcessor(new DbStandardBeanProcessor()));
 			
 		Object[] queryParams = null;
 		if (lastSQLParameters.size() != 0)
 			queryParams = lastSQLParameters.toArray();
 		
 		List<T> list = run.query(preparedConnection, preparedSQL, rh, queryParams);
 		if (list.size() == 0)
 			return null;
 		else
 			return list.get(0);
 	}
 	
 	
 	
     //run prepared - insert >>>
 	/**
 	 * Insert one entity with prepared statement.
 	 * 
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void insertEntityWithPreparedStatement(T entity) throws Exception {
 		
 		clearLastStatement();
 		checkPrepare(StatementType.INSERT, entity.getClass());
 						
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap2(entity);
 					
 		int idx = 1;
 		for (String field : preparedFields) {
 			Object param = objs.get(field);
 			if (param instanceof java.util.Date) {
 				java.sql.Date paramD = new java.sql.Date(((java.util.Date)param).getTime());
 				param = paramD;
 			}
 			preparedStatement.setObject(idx, param);
 			lastSQLParameters.add(param);
 			idx++;
 		}
 		
 		preparedStatement.execute();
 	}
 	
 	
 	
 	//run prepared - update >>>
 	/**
 	 * Update entities with prepared statement.
 	 * 
 	 * @param entity The bean
 	 * @param params Parameters for SQL conditions
 	 * @throws Exception
 	 */
 	public void updateEntitiesWithPreparedStatement(T entity, Map<String, Object> params) throws Exception {
 		
 		clearLastStatement();
 		checkPrepare(StatementType.UPDATE, entity.getClass());
 		
 		List<Object> paramValues = preparedNPP.getSQLParameters(params);
 		if (params != null)
 			lastNamedParameters.putAll(params);
 				
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap2(entity);
 		
 		int idx = 1;
 		for (String key : preparedFields) {
 			Object param = objs.get(key);
 			if (param instanceof java.util.Date) {
 				java.sql.Date paramD = new java.sql.Date(((java.util.Date)param).getTime());
 				param = paramD;
 			}
 			preparedStatement.setObject(idx, param);
 			lastSQLParameters.add(param);
 			idx++;
 		}
 		for (Object object : paramValues) {
 			preparedStatement.setObject(idx, object);
 			idx++;
 		}
 		
 		lastSQLParameters.addAll(paramValues);
 		
 		preparedStatement.execute();
 	}
 	
 	/**
 	 * Update one entity with prepared statement.
 	 * 
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void updateEntityWithPreparedStatement(T entity) throws Exception {
 						
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		if (idColumn == null || idColumn.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		
 		Map<String, Object> params = new HashMap<String, Object>();
 		
 		Map<String, Object> map = AnnotationHelper.getObjectAsMap(entity);
 		params.put(idColumn, map.get(idColumn));
 		
 		updateEntitiesWithPreparedStatement(entity, params);
 	}
 	
 	
 	
 	//run prepared - delete >>>
 	/**
 	 * Delete entities with prepared statement.
 	 * 
 	 * @param params Parameters for SQL conditions
 	 * @throws Exception
 	 */
 	public void deleteEntitiesWithPreparedStatement(Map<String, Object> params) throws Exception {
 		
 		clearLastStatement();
 		checkPrepare(StatementType.DELETE, preparedStatementClass);
 		
 		List<Object> paramValues = preparedNPP.getSQLParameters(params);
 		if (params != null)
 			lastNamedParameters.putAll(params);
 		lastSQLParameters.addAll(paramValues);
 		
 		int idx = 1;
 		for (Object object : paramValues) {
 			preparedStatement.setObject(idx, object);
 			idx++;
 		}
 						
 		preparedStatement.execute();
 	}
 	
 	/**
 	 * Delete one entity with prepared statement.
 	 * 
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void deleteEntityWithPreparedStatement(T entity) throws Exception {
 						
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		if (idColumn == null || idColumn.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		
 		Map<String, Object> params = new HashMap<String, Object>();
 		
 		Map<String, Object> map = AnnotationHelper.getObjectAsMap(entity);
 		params.put(idColumn, map.get(idColumn));
 		
 		deleteEntitiesWithPreparedStatement(params);
 	}
 	
 	
 	
 	//run prepared - insert-select >>>
 	/**
 	 * Insert entities with prepared statement.
 	 * 
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void insertSelectWithPreparedStatement(Map<String, Object> params) throws Exception {
 		
 		clearLastStatement();
 		checkPrepare(StatementType.INSERT_SELECT, preparedStatementClass);
 		
 		List<Object> paramValues = preparedNPP.getSQLParameters(params);
 		if (params != null)
 			lastNamedParameters.putAll(params);
 		lastSQLParameters.addAll(paramValues);
 		
 		int idx = 1;
 		for (Object object : paramValues) {
 			preparedStatement.setObject(idx, object);
 			idx++;
 		}
 				
 		preparedStatement.execute();
 	}
 	
 	
 	
 	//run prepared - stored procedure >>>
 	public Map<String, Object> executeProcedureWithPreparedStatement(
 		Map<String, Object> params) throws Exception {
 		
 		clearLastStatement();
 		checkPrepare(StatementType.PROCEDURE, preparedStatementClass);
 		
 		List<Object> paramValues = preparedNPP.getSQLParameters(params);
 		if (params != null)
 			lastNamedParameters.putAll(params);
 		lastSQLParameters.addAll(paramValues);
 		
 		int idx = 1;
 		for (Object value : lastSQLParameters) {
 			preparedStatement.setObject(idx, value);
 			idx++;
 		}
 		
 		preparedStatement.execute();
 		
 		Map<String, Object> out = new HashMap<String, Object>();
 		for (Entry<String, Integer> entry : preparedProcedureOutputParams.entrySet()) {
 			out.put(entry.getKey(), ((CallableStatement)preparedStatement).getObject(entry.getValue()));
 		}
 
 		return out;
 	};
 	
 	
 	//select >>>
 	/**
 	 * Query entities from database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param Connection SQL Connection
 	 * @param builder Query builder
 	 * @return list of beans
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public List<T> queryEntities(
 		Connection connection, QueryBuilder builder) throws Exception {
 		
 		return queryEntities(connection, (Class)null, builder);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param params Parameters for SQL conditions
 	 * @see {@link SQLExecute#queryEntities(Connection connection, QueryBuilder builder)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public List<T> queryEntities(
 		Connection connection, QueryBuilder builder, Map<String, Object> params) throws Exception {
 		
 		return queryEntities(connection, (Class)null, builder, params);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#queryEntities(Connection connection, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> queryEntities(
 		Connection connection, Class clazz, QueryBuilder builder) throws Exception {
 		
 		return queryEntities(connection, clazz, "", builder, null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param params Parameters for SQL conditions
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class clazz, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> queryEntities(
 		Connection connection, Class clazz, QueryBuilder builder, Map<String, Object> params) throws Exception {
 		
 		return queryEntities(connection, clazz, "", builder, params);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL table alias name
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class clazz, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> queryEntities(
 		Connection connection, Class clazz, String tableAlias, QueryBuilder builder) throws Exception {
 		
 		return queryEntities(connection, clazz, tableAlias, builder, null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param params Parameters for SQL conditions
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class clazz, String tableAlias, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> queryEntities(
 		Connection connection, Class clazz, String tableAlias, QueryBuilder builder, Map<String, Object> params) throws Exception {
 		
 		clearLastStatement();
 		prepareQueryStatement(connection, clazz, tableAlias, builder);
 		List<T> list = queryEntitiesWithPreparedStatement(params);
 
 		return list;
 	}
 	
 	/**
 	 * Query one entity from database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param Connection SQL Connection
 	 * @param entity bean
 	 * @return bean
 	 * @throws Exception
 	 */
 	public T queryEntity(Connection connection, T entity) throws Exception {
 		
 		if (entity == null) throw new SQLException(ENTITY_IS_NULL);
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		if (idColumn == null || idColumn.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity);
 		
 		QueryBuilder builder = new SimpleBeanSQLQueryBuilder(entity.getClass());
 		builder.setWhere(new AndOperator(new EqualCriteria<Object>(idColumn, objs.get(idColumn))));
 		
 		List<T> beans = queryEntities(connection, entity.getClass(), builder);
 		
 		this.builder = null;
 		
 		if (beans.size() == 0)
 			return null;
 		else
 			return beans.get(0);
 	}
 	
 	
 	
 	//lock >>>
 	/**
 	 * Lock entities in database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param Connection SQL Connection
 	 * @param builder Query builder
 	 * @return list of beans
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public List<T> lockEntities(
 		Connection connection, QueryBuilder builder) throws Exception {
 		
 		return lockEntities(connection, (Class)null, builder);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param params Parameters for SQL conditions
 	 * @see {@link SQLExecute#lockEntities(Connection connection, QueryBuilder builder)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public List<T> lockEntities(
 		Connection connection, QueryBuilder builder, Map<String, Object> params) throws Exception {
 		
 		return lockEntities(connection, (Class)null, builder, params);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#lockEntities(Connection connection, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> lockEntities(
 		Connection connection, Class clazz, QueryBuilder builder) throws Exception {
 		
 		return lockEntities(connection, clazz, "", builder, null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param params Parameters for SQL conditions
 	 * @see {@link SQLExecute#lockEntities(Connection connection, Class clazz, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> lockEntities(
 		Connection connection, Class clazz, QueryBuilder builder, Map<String, Object> params) throws Exception {
 		
 		return lockEntities(connection, clazz, "", builder, params);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL table alias name
 	 * @see {@link SQLExecute#lockEntities(Connection connection, Class clazz, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> lockEntities(
 		Connection connection, Class clazz, String tableAlias, QueryBuilder builder) throws Exception {
 		
 		return lockEntities(connection, clazz, tableAlias, builder, null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param params Parameters for SQL conditions
 	 * @see {@link SQLExecute#lockEntities(Connection connection, Class clazz, String tableAlias, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> lockEntities(
 		Connection connection, Class clazz, String tableAlias, QueryBuilder builder, Map<String, Object> params) throws Exception {
 		
 		clearLastStatement();
 		prepareLockStatement(connection, clazz, tableAlias, builder);
 		List<T> list = lockEntitiesWithPreparedStatement(params);
 		
 		return list;
 	}
 	
 	/**
 	 * Lock one entity in database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param Connection SQL Connection
 	 * @param entity The bean
 	 * @return bean
 	 * @throws Exception
 	 */
 	public T lockEntity(
 		Connection connection, T entity) throws Exception {
 		
 		if (entity == null) throw new SQLException(ENTITY_IS_NULL);
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		if (idColumn == null || idColumn.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity);
 		
 		QueryBuilder builder = new SimpleBeanSQLQueryBuilder(entity.getClass());
 		builder.setWhere(new AndOperator(new EqualCriteria<Object>(idColumn, objs.get(idColumn))));
 		
 		List<T> beans = lockEntities(connection, entity.getClass(), builder);
 		
 		this.builder = null;
 		
 		if (beans.size() == 0)
 			return null;
 		else
 			return beans.get(0);
 	}
 	
 	
 	
 	//insert >>>
 	/**
 	 * Insert one entity into database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void insertEntity(Connection connection, T entity) throws Exception {
 		prepareInsertStatement(connection, entity.getClass());
 		insertEntityWithPreparedStatement(entity);
 	}
 	
 	
 	
 	//update >>>
 	/**
 	 * Update entities in database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @param entity The bean
 	 * @param fields list of fields used in SQL
 	 * @param condition SQL condition
 	 * @throws Exception
 	 */
 	public void updateEntities(
 		Connection connection, T entity, List<String> fields, Object condition) throws Exception {
 		
 		updateEntities(connection, entity, "", fields, condition);
 	}
 	 
 	/**
 	 * {@inheritDoc}
 	 * @see {@link SQLExecute#updateEntities(Connection connection, T entity, List<String> fields, Object condition)}
 	*/
 	public void updateEntities
 		(Connection connection, T entity, String[] fields, Object condition) throws Exception {
 		
 		List<String> list = new ArrayList<String>(Arrays.asList(fields));
 		updateEntities(connection, entity, "", list, condition);
 	}
 	 
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL table alias name
 	 * @see {@link SQLExecute#updateEntities(Connection connection, T entity, List<String> fields, Object condition)}
 	 */
 	public void updateEntities(
 		Connection connection,T entity, String tableAlias, String[] fields, Object condition) throws Exception {
 		
 		List<String> list = new ArrayList<String>(Arrays.asList(fields));
 		updateEntities(connection, entity, tableAlias, list, condition);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL table alias name
 	 * @see {@link SQLExecute#updateEntities(Connection connection, T entity, List<String> fields, Object condition)}
 	 */
 	public void updateEntities(
 		Connection connection,T entity, String tableAlias, List<String> fields, Object condition) throws Exception {
 		
 		updateEntities(connection, entity, tableAlias, fields, condition, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param params Parameters for SQL conditions
 	 * @see {@link updateEntities#updateEntity(Connection connection, T entity, List<String> fields, Object condition)}
 	 */
 	public void updateEntities(Connection connection,T entity, String tableAlias, List<String> fields, Object condition, Map<String, Object> params) throws Exception {
 		
 		clearLastStatement();
 		prepareUpdateStatement(connection, entity.getClass(), tableAlias, fields, condition);
 		updateEntitiesWithPreparedStatement(entity, params);
 	}
 	
 	/**
 	 * Update one entity in database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void updateEntity(
 		Connection connection, T entity) throws Exception {
 		
 		updateEntity(connection, entity, (List<String>)null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields used in SQL
 	 * @see {@link SQLExecute#updateEntity(Connection connection, T entity)}
 	 */
 	public void updateEntity(
 		Connection connection, T entity, String[] fields) throws Exception {
 		
 		List<String> list = new ArrayList<String>(Arrays.asList(fields));
 		updateEntity(connection, entity, list);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields used in SQL
 	 * @see {@link SQLExecute#updateEntity(Connection connection, T entity)}
 	 */
 	public void updateEntity(
 		Connection connection, T entity, List<String> fields) throws Exception {
 		
 		if (entity == null) throw new SQLException(ENTITY_IS_NULL);
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		if (idColumn == null || idColumn.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity);
 		
 		Object condition = new AndOperator(new EqualCriteria<Object>(idColumn, objs.get(idColumn)));
 		
 		updateEntities(connection, entity, "", fields, condition);
 	}
 	
 	
 	
 	//delete >>>
 	/**
 	 * Delete entities from database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @param entity The bean
 	 * @param condition SQL condition
 	 * @throws Exception
 	 */
 	public void deleteEntities(
 		Connection connection, Object condition) throws Exception {
 		
 		deleteEntities(connection, null, condition);
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public void deleteEntities(
 		Connection connection, Class clazz, Object condition) throws Exception {
 		
 		deleteEntities(connection, clazz, "",  condition);
 	}
 	 
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL table alias name
 	 * @see {@link SQLExecute#DeleteEntities(Connection connection, T entity, List<String> fields, Object condition)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void deleteEntities(
 		Connection connection,Class clazz, String tableAlias, Object condition) throws Exception {
 		
 		deleteEntities(connection, clazz, tableAlias, condition, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param params Parameters for SQL conditions
 	 * @see {@link DeleteEntities#DeleteEntity(Connection connection, T entity, List<String> fields, Object condition)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void deleteEntities(Connection connection,Class clazz, String tableAlias, Object condition, Map<String, Object> params) throws Exception {
 		
 		clearLastStatement();
 		prepareDeleteStatement(connection, clazz, tableAlias, condition);
 		deleteEntitiesWithPreparedStatement(params);
 	}
 	
 	/**
 	 * Delete one entity in database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void deleteEntity(
 		Connection connection, T entity) throws Exception {
 				
 		if (entity == null) throw new SQLException(ENTITY_IS_NULL);
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		if (idColumn == null || idColumn.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity);
 		
 		Object condition = new AndOperator(new EqualCriteria<Object>(idColumn, objs.get(idColumn)));
 		
 		deleteEntities(connection, entity.getClass(), condition);
 	}
 	
 	
 	
 	//insert-select >>>
 	/**
 	 * Insert entities into database. The result set mapped with bean's javax.persistence.Column annotations by default.
 	 * If an annotation is not found then the field name is the result set column name (The _ character are deleted).
 	 * 
 	 * @param connection SQL Connection
 	 * @param insertClass Class used by AnnotationHelper
 	 * @param insertFields list of fields used in SQL
 	 * @param selectClass Class used by AnnotationHelper
 	 * @param tableAlias SQL table alias name
 	 * @param selectFields list of fields used in SQL
 	 * @param selectCondition SQL condition
 	 * @param params Parameters for SQL conditions
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void insertSelect(
 		Connection connection,
 		Class insertClass, String[] insertFields,
 		Class selectClass, String tableAlias, String[] selectFields, Object selectCondition,
 		Map<String, Object> params)
 		throws Exception{
 				
 		List<String> list1 = new ArrayList<String>(Arrays.asList(insertFields));
 		List<String> list2 = new ArrayList<String>(Arrays.asList(selectFields));
 		
 		insertSelect(connection, insertClass, list1, selectClass, tableAlias, list2, selectCondition, params);
 	}
 		
 	/**
 	 * {@inheritDoc}
 	 * @throws Exception
 	 * @see {@link SQLExecute#insertSelect(Connection connection, Class insertClass, String[] insertFields, Class selectClass, String tableAlias, String[] selectFields, Object selectCondition)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void insertSelect(
 		Connection connection,
 		Class insertClass, List<String> insertFields,
 		Class selectClass, String tableAlias, List<String> selectFields, Object selectCondition,
 		Map<String, Object> params)
 		throws Exception{
 		
 		clearLastStatement();
 		prepareInsertSelectStatement(connection, insertClass, insertFields, selectClass, tableAlias, selectFields, selectCondition);
 		insertSelectWithPreparedStatement(params);
 	}
 	
 	
 	
 	//stored procedure >>>
 	public Map<String, Object> executeProcedure(Connection connection, String procName, Map<String, Object> params) throws Exception {
 		
 		prepareExecuteProcedure(connection, procName);
 		Map<String, Object> res = executeProcedureWithPreparedStatement(params);
 		
 		return res;
 	}
 
 
 
 	//script >>>
 	/**
 	 * Execute an SQL Script.
 	 * @param connection
 	 * @param sql
 	 * @throws SQLException
 	 */
 	public void executeScript(
 		Connection connection, File sql) throws SQLException {
 		
 		executeScript(connection, sql, null, null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param section
 	 * @see {@link SQLExecute#executeScript(Connection connection, File sql)}
 	 */
 	public void executeScript(
 		Connection connection, File sql, String section) throws SQLException {
 		
 		executeScript(connection, sql, section, ";");
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param separator
 	 * @see {@link SQLExecute#executeScript(Connection connection, File sql, String section)}
 	 */
 	public void executeScript(
 		Connection connection, File sql, String section, String separator) throws SQLException {
 		
 		String s = new String();
 		StringBuffer sb = new StringBuffer();
 		
 		String actSection = null;
 	
 	    try {
 			FileReader fr = new FileReader(sql);
 		    BufferedReader br = new BufferedReader(fr);
 
 	    	while((s = br.readLine()) != null)
 			{
 	    		if (s.trim().startsWith("@")) {
 	    			actSection = s.trim().substring(1);
 	    		} else {
 	    			boolean use = true;
 	    			if (section != null) use = false;
 	    			if (section != null && actSection != null && actSection.equalsIgnoreCase(section))
 	    				use = true;
 	    			if (use) sb.append(s+"\n");
 	    		}
 			}
 		    br.close();
 	    }
 		catch (IOException e) {
 			throw new SQLException(e);
 		}
 
 		
 		//begin the sql file parser to separate the sql commands into
         //separate array entries. This parser requires that your
         //sql statements be typed in uppercase because that is the
         //convention of the author.
 		
 		//Step 1: Split script to commands when needed
 		String[] stmts = null;
 		if (separator != null) {
 			stmts = sb.toString().split(separator);
 		} else {
 			stmts = new String[] { sb.toString() };
 		}
 		
 
         //Step 2: Put Transactions back into a single statement.
         for(int i=0;i<stmts.length;i++){
             //if the current statement starts a transaction
             if(stmts[i].contains("BEGIN TRANSACTION")){
                 int tInt = i;
                 //find the end of the transaction or the end of the file
                 //whichever comes first
                 while(tInt<stmts.length && !stmts[tInt].contains("END TRANSACTION")) {
                     tInt++;
                 } //end while
 
                 //add a semicolon to the first sql entry in the transaction
                 //which will be in the same array entry as the BEGIN
                 //statement
                 stmts[i] += separator;
 
                 //loop through the remaining transaction and place them
                 //into the transaction start entry appending semicolons
                 //at the end of each statement
                 for(int j = (i+1); j< tInt; j++) {
                     stmts[i] += "\n" + stmts[j] + separator;
                     //blank out the current transaction entry so that the
                     //executer skips it
                     stmts[j] = " ";
                 } //end for
 
                 //and the end statement to the end of the transaction
                 stmts[i] += "\nEND TRANSACTION";
 
                 //remove the END transaction from the statement it is
                 //currently embedded in
                 String tStr[] = stmts[tInt].split("END TRANSACTION");
                 if (tStr.length>0)
                 	stmts[tInt] = tStr[1];
                 else
                 	stmts[tInt] = "";
                 //skip the statements blanked out earlier, actually pointing
                 //to the last transaction entry so that the for statement
                 //points to the first statement after the transaction
                 i = tInt - 1;
             } //end if
         } //end for
 
         // Removes BEGIN and END with
         for (int i=0; i<stmts.length; i++)
         	stmts[i] = stmts[i].replaceAll("BEGIN TRANSACTION", "").replaceAll("END TRANSACTION", "COMMIT");
 
 
         //end sql file parsers
 
         // Executing commands
         for (int si = 0; si<stmts.length; si++) {
         	String[] inst = null;
         	if (separator != null) {
         		inst = stmts[si].split(separator);
         	} else {
         		inst = new String[] { stmts[si] };
         	}
 		    
 		    Statement st;
 			try {
 				st = connection.createStatement();
 		        for(int i = 0; i<inst.length; i++)
 		        {
 		            // we ensure that there is no spaces before or after the request string
 		            // in order to not execute empty statements
 		            if(!inst[i].trim().equals(""))
 		            {
 		                if (inst[i].trim().toUpperCase().startsWith("COMMIT")) {
 		                	connection.commit();
 		                } else {
 		                	st.executeUpdate(inst[i]);
 		                }
 		            }
 		        }
 			}
 			catch (java.sql.SQLException e) {
 				throw new SQLException(e);
 			}
         }
 	}
 		
 	//other - select >>>
 	//
 	private ResultSetHandler<List<Map<String, ?>>> mapResultSetHandler = new ResultSetHandler<List<Map<String, ?>>>() {
 		//implementation of ResultSetHandler interface
 	    public List<Map<String, ?>> handle(ResultSet rs) throws java.sql.SQLException {
 	    	
 	        ResultSetMetaData meta = rs.getMetaData();
 
 	        List<Map<String, ?>> result = new ArrayList<Map<String,?>>();
 	        while (rs.next()) {
 	        	HashMap<String, Object> record = new HashMap<String, Object>();
 	        	for (int i = 0; i < meta.getColumnCount(); i++) {
 	        		String columnName = meta.getColumnLabel(i+1);
 	        		if (columnName == null || "".equals(columnName)) columnName = meta.getColumnName(i+1);
 	        		int columnType = meta.getColumnType(i+1);
 	        		
 	        		if (Types.DATE == columnType || Types.TIME == columnType || Types.TIMESTAMP == columnType) {
 	        			record.put(columnName, rs.getDate(i+1));
 	        		} else if (Types.BOOLEAN == columnType) {
 	        			record.put(columnName, rs.getBoolean(i+1));
 	        		} else if (Types.FLOAT == columnType || Types.DECIMAL == columnType || Types.DOUBLE == columnType || Types.NUMERIC == columnType) {
 	        			record.put(columnName, rs.getDouble(i+1));
 	        		} else if (Types.INTEGER == columnType || Types.BIGINT == columnType) {
 	        			record.put(columnName, rs.getInt(i+1));
 	        		} else if (Types.BLOB == columnType) {
 	        			try {
 							record.put(columnName, BlobClobConverter.convertStreamToString(rs.getBlob(i+1).getBinaryStream()));
 						} catch (IOException e) {
 						}
 	        		} else if (Types.CLOB == columnType) {
 	        			try {
 							record.put(columnName, BlobClobConverter.convertReaderToString(rs.getCharacterStream(i+1)));
 						} catch (IOException e) {
 						}
 	        		} else {
 	        			try {
 	        				record.put(columnName, rs.getString(i+1));
 	        			} catch (Exception e) {
 						}
 	        		}
 		        }
 	        	result.add(record);
 	        }
 	        return result;
 	    }
 	};
 
 	public List<Map<String, ?>> queryEntitiesAsMap(Connection connection, QueryBuilder builder) throws Exception {
		return queryEntitiesAsMap(connection, null, builder, null);
 	}
 
 	public List<Map<String, ?>> queryEntitiesAsMap(Connection connection,  String tableAlias, QueryBuilder builder) throws Exception {
 		return queryEntitiesAsMap(connection, tableAlias, builder, null);
 	}
 
 	public List<Map<String, ?>> queryEntitiesAsMap(Connection connection, QueryBuilder builder, Map<String, Object> params) throws Exception {
		return queryEntitiesAsMap(connection, null, builder, params);
 	}
 
 	public List<Map<String, ?>> queryEntitiesAsMap(Connection connection,
 		  String tableAlias, QueryBuilder builder, Map<String, Object> params) throws Exception {
 		
     	clearLastStatement();
     	clearPrepare();
     	this.builder = builder;
 		
 		// Create a QueryRunner that will use connections from
 		// the given DataSource
 		QueryRunner run = new QueryRunner();
 
 		String sql = getSelectQuery(HashMap.class, tableAlias);
 
 		preparedNPP =  new NamedParameterProcessor(sql);
 		preparedSQL = preparedNPP.getSqlStatement();
 		lastSQLParameters.addAll(preparedNPP.getSQLParameters(params));
 		
 		Object[] sqlParams = null;
 		if (lastSQLParameters.size() !=0)
 			sqlParams = lastSQLParameters.toArray();
 		
 		return run.query(connection, preparedNPP.getSqlStatement(), mapResultSetHandler, sqlParams);
 	}
 	
 	
 	
 	//other - table >>>
 	/**
 	 * Create a table in database. The given bean have to be annotated with javax.persistence.Entity, javax.presistence.Column and javax.persistence.Id
 	 * 
 	 * @param connection SQL Connection
 	 * @throws Exception
 	 */
 
 	public void createTable(Connection connection) throws Exception {
 		
 		createTable(connection, null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#createTable(Connection connection)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void createTable(Connection connection, Class clazz) throws Exception {
 		
     	clearLastStatement();
     	clearPrepare();
     	this.builder = null;
     	
 		Class localClass = clazz;
 		if (localClass == null) localClass = this.clazz;
     	
 		String tableName = AnnotationHelper.getTableName(localClass);
 		if (tableName == null || tableName.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		List<Field> fields = AnnotationHelper.getAllFields(localClass);
 		StringBuffer sb = new StringBuffer();
 		sb.append("CREATE TABLE "+tableName+" (");
 
 		boolean firstField = true;
     	for (Field fld : fields) {
     		Annotation[] annotations = fld.getAnnotations();
 			Id id = null;
 			Column col = null;
 
     		for (int i=0; i<annotations.length; i++) {
     			if (annotations[i] instanceof Column) {
     				col = (Column)annotations[i];
     			} else if (annotations[i] instanceof Id) {
     				id = (Id)annotations[i];
     			}
     		}
 			if (col != null) {
 				if (firstField) firstField = false; else sb.append(",");
     			if (col.name() == null || col.name().length() == 0) throw new SQLException(COLUMN_NAME_IS_UNDEFINED);
     			if (col.columnDefinition() == null || col.columnDefinition().equals("")) throw new SQLException(COLUMN_DEFINITION_IS_UNDEFINED);
     			sb.append(col.name());
     			    		
     			//I know, I know... It's "dirty", but what can we do, when RDMS not follow the SQL naming standard...
     			String colDef = col.columnDefinition();
     			if (Arrays.binarySearch(BLOBNAMES, colDef.toUpperCase()) >= 0 ) {
     				colDef = getBlobName();
     			}
     			    				
     			sb.append(" "+colDef);
     			if (!col.nullable()) {
     				sb.append(" NOT NULL");
     			}
     			if (col.unique()) {
     				sb.append(" UNIQUE");
     			}
     			if (id != null) {
     				sb.append(" PRIMARY KEY");
     			}
 			}
     	}
     	sb.append(")");
 
     	String sql = sb.toString();
     	preparedNPP = new NamedParameterProcessor(sql);
     	preparedSQL = preparedNPP.getSqlStatement();
 		PreparedStatement stm = connection.prepareStatement(preparedSQL);
 				
 		stm.execute();
 		connection.commit();
 	}
 	
 	/**
 	 * Drop a table from database. The given bean have to be annotated with javax.persistence.Entity or javax.persistence.Table
 	 * 
 	 * @param connection SQL Connection
 	 * @throws Exception
 	 */
 	public void dropTable(Connection connection) throws Exception {
 		
 		dropTable(connection, null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#dropTable(Connection connection)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public void dropTable(Connection connection, Class clazz) throws Exception {
 			
     	clearLastStatement();
     	clearPrepare();
     	this.builder = null;
     	
 		Class localClass = clazz;
 		if (localClass == null) localClass = this.clazz;
     	
 		String tableName = AnnotationHelper.getTableName(localClass);
 		if (tableName == null || tableName.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append("DROP TABLE "+tableName);
 
 		String sql = sb.toString();
     	preparedNPP = new NamedParameterProcessor(sql);
     	preparedSQL = preparedNPP.getSqlStatement();
 		PreparedStatement stm = connection.prepareStatement(preparedSQL);
 		
 		stm.execute();
 		connection.commit();
 	}
 	
 	/**
 	 * Check table existence. The given bean have to be annotated with javax.persistence.Entity
 	 * 
 	 * @param connection SQL Connection
 	 * @throws Exception
 	 */
 	public boolean existsTable(Connection connection) throws Exception {
 		
 		return existsTable(connection, null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param clazz Class used by AnnotationHelper
 	 * @see {@link SQLExecute#existsTable(Connection connection)}
 	 */
 	@SuppressWarnings("rawtypes")
 	public boolean existsTable(Connection connection, Class clazz) throws Exception {
 		
     	clearLastStatement();
     	clearPrepare();
     	this.builder = null;
     	
 		Class localClass = clazz;
 		if (localClass == null) localClass = this.clazz;
 						
 		String tableName = AnnotationHelper.getTableName(localClass);
 		if (tableName == null || tableName.length() == 0) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		DatabaseMetaData dbm = connection.getMetaData();
 		ResultSet tables = dbm.getTables((String)null, (String)null, tableName.toUpperCase(), (String[])null);
 		if (tables == null)
 			return false;
 		
 		return tables.next();
 	}
 
 	
 }
