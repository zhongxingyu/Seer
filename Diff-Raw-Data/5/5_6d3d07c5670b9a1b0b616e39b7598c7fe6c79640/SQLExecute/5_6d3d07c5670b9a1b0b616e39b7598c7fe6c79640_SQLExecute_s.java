 package org.liveSense.api.sql;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.io.StringWriter;
 import java.io.Writer;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
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
 import java.util.Set;
 
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
 import org.liveSense.misc.queryBuilder.operators.AndOperator;
 
 
 /**
  * This class provides basic functionalities of SQL for javax.persistence annotated beans.
  * 
  * @param <T> - The Bean class is used for
  */
 public abstract class SQLExecute<T> {
 
 	
 	private static final String THIS_TYPE_OF_JDBC_DIALECT_IS_NOT_IMPLEMENTED = "This type of JDBC dialect is not implemented";
 	private static final String CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION = "Class does not contain javax.persistence.Entity or javax.persistence.Entity.Table annotation";
 	private static final String CLASS_DOES_NOT_HAVE_ID_ANNOTATION = "Entity does not contain javax.persistence.Id annotation";
 	private static final String CONNECTION_IS_NULL = "Connection is null";
 	private static final String ENTITY_IS_NULL = "Entity is null";
 	private static final String NO_DATASOURCE = "No datasource is defined";
 	private static final String BASIC_DATASOURCE_OBJECT_NEEDED = "No org.apache.commons.dbcp.BasicDataSource object is defined";
 	private static final String INSERT_UNSUCCESSFULL = "INSERT was unsuccessfull";	
 	private static final String UPDATE_UNSUCCESSFULL = "UPDATE was unsuccessfull";
 	private static final String STATEMENT_TYPE_IS_NOT_INSERT = "The statement type does not match with INSERT";
 	private static final String STATEMENT_TYPE_IS_NOT_UPDATE = "The statement type does not match with UPDATE";
 	private static final String ENTITY_TYPE_MISMATCH = "Entity class type mismatch";
 	private static final String COLUMN_NAME_IS_UNDEFINED = "Column name is undefined";
 	private static final String COLUMN_DEFINITION_IS_UNDEFINED = "Column definition is undefined";
 	private static final String UNKNOWN_PARAMETER = "Unknown parameter";
 	
 
 	public enum StatementType {
 		INSERT, UPDATE, DELETE, SELECT
 	}
 		
 	protected String jdbcDriverClass;
 	
 	private PreparedStatement preparedStatement = null;	
 	private StatementType preparedType = null;
 	@SuppressWarnings("rawtypes")
 	private Class preparedStatementClass = null;
 	private Connection preparedConnection = null;
 	private ArrayList<String> prepareStatementElements = null;
 	
 	private String lastSQLStatement;
 	private ArrayList<Object> lastSQLStatementParameters = new ArrayList<Object>();
 	
 	protected QueryBuilder builder;
 	
 	private ArrayList<String> lastStatementFields = new ArrayList<String>();
 	
 	
 	
 	public StatementType getPreparedType() {	
 		return preparedType;
 	}
 
 	public String getLastSQLStatement() {	
 		return lastSQLStatement;
 	}
 	
 	public ArrayList<Object> getLastSQLStatementParameters() {
 		return lastSQLStatementParameters;
 	}
 
 	/**
 	 * This class is a transport class which contains and builds the query. 
 	 *
 	 */
 	public class ClauseHelper {
 		@SuppressWarnings("rawtypes")
 		private Class clazz;
 		private String query;
 		private Boolean subSelect;
 		
 		@SuppressWarnings("rawtypes")
 		public ClauseHelper(Class clazz, String query, Boolean subSelect) {
 			this.clazz = clazz;
 			this.query = query;
 			this.subSelect = subSelect;
 		}
 				
 		@SuppressWarnings("rawtypes")
 		public Class getClazz() {
 			return clazz;
 		}		
 		@SuppressWarnings("rawtypes")
 		public void setClazz(
 			Class clazz) {
 			this.clazz = clazz;
 		}
 		public String getQuery() {
 			return query;
 		}
 		public void setQuery(String query) {
 			this.query = query;
 		}
 		public Boolean getSubSelect() {
 			return subSelect;
 		}
 		public void setSubSelect(Boolean subSelect) {
 			this.subSelect = subSelect;
 		}
 	}
 	
 	
 	class StatementWithParameter {
 		private Map<String, List<Integer>> parameters;
 		private String sqlStatement;
 		
 		public StatementWithParameter(Map<String, List<Integer>> parameters, String statement) {
 			this.parameters = parameters;
 			this.sqlStatement = statement;
 		}
 
 		public Map<String, List<Integer>> getParameters() {
 			return parameters;
 		}
 
 		public void setParameters(Map<String, List<Integer>> parameters) {
 			this.parameters = parameters;
 		}
 
 		public String getSqlStatement() {
 			return sqlStatement;
 		}
 
 		public void setSqlStatement(String sqlStatement) {
 			this.sqlStatement = sqlStatement;
 		}
 	}
 	
 	public StatementWithParameter processSQLParameters(String sql) throws SQLException {
 		Map<String, List<Integer>> paramNames = new HashMap<String, List<Integer>>();
 		
 		int paramCount = 0;
 		int ps = sql.indexOf(':');
 		while (ps != -1) {
 			//check whether we found it in a string literal
 			if (StringUtils.countMatches(sql.substring(1, ps), "'") % 2 == 0) {
 				paramCount++;
 				
 				//get parameter name
 				StringBuffer sb = new StringBuffer();
 				ps++;
 				while (sql.substring(ps, ps + 1).matches("[a-zA-Z0-9_]+"))  {
 					sb.append(sql.substring(ps, ps + 1));
 					ps++;
 				}
 				//store position
 				String paramName = sb.toString();
 				List<Integer> list = paramNames.get(paramName);
 				if (list == null) {
 					list = new ArrayList<Integer>();
 					paramNames.put(paramName, list);					
 				}				
 				list.add(paramCount);
 				StringBuffer sb2 = new StringBuffer(sql);
 				sb2.delete(ps - paramName.length() - 1, ps);
 				sb2.insert(ps - paramName.length() - 1, "?");
 				sql = sb2.toString();
 			}
 			ps = sql.indexOf(':', ps + 1);
 		}
 		//lastSQLStatement = sql;
 		return new StatementWithParameter(paramNames, sql);
 		
 		/*
 				
 		String[] keys = new String[params.size()];
 		
 		int idx = -1;
 		for (String key : params.keySet()) {
 			idx++;
 			keys[idx] = key;			
 		}
 		
 		Arrays.sort(keys);
 		
 		List<Integer> paramPositions = new ArrayList<Integer>();
 		Map<Integer, Object> paramValue = new HashMap<Integer, Object>();
 		StringBuffer sb = new StringBuffer(sql);
 		
 		//it's important to go backward (descending order). This way we can avoid to replace wrong (e.g.: :param1, :param11)
 		while (idx != -1) {
 			String key = keys[idx];
 			if (key.contains(":")) throw new SQLException(PARAMERER_NAME_ERROR); 
 			
 			//WARNING : this will search in String literals too, choose your parameter names carefully
 			int ps = sb.toString().indexOf(":" + key);
 			while (ps != -1) {
 				paramPositions.add(ps);
 				paramValue.put(ps, params.get(key));
 				
 				sb.delete(ps, ps + key.length() + 1);				
 				sb.insert(ps, StringUtils.leftPad("?", key.length() + 1));//padding is nedded for to avoid position changes
 				
 				ps = sb.toString().indexOf(":" + key);
 			}
 			idx--;
 		}
 		Collections.sort(paramPositions);
 		
 		Object[] sqlParams = new Object[paramPositions.size()];
 		
 		Integer i = 0;
 		for (Integer ps : paramPositions) {
 			sqlParams[i] = paramValue.get(ps);
 			i++;
 		}
 
 		lastSQLStatement = sb.toString();
 		
 		return sqlParams;
 		*/
 	}
 	
 	private Object[] getSQLParameters(Map<String, List<Integer>> paramNames, Map<String, Object> params) throws SQLException {
 		ArrayList<Object> list = new ArrayList<Object>();
 		
 		if (params == null) 
 			return list.toArray();
 		
 		for (Entry<String, Object> entry : params.entrySet()) {
 			List<Integer> positions = paramNames.get(entry.getKey());
 			
 			if (positions == null)
 				throw new SQLException(UNKNOWN_PARAMETER + " : "+entry.getKey());
 			
 			for (Integer pos : positions) {
 				while (list.size() < pos) list.add(null);
 				list.set(pos - 1, entry.getValue());
 			}			
 		}
 		return list.toArray();
 	}
 	
 
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
 	 * @param clazz Class of the bean 
 	 * @return The final SQL statement
 	 * @throws SQLException
 	 * @throws QueryBuilderException
 	 */
 	@SuppressWarnings("rawtypes")
 	public abstract String getSelectQuery(Class clazz) throws SQLException, QueryBuilderException;
 		
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL alias of the table 
 	 * @see {@link SQLExecute#getSelectQuery()}
 	 */
 	@SuppressWarnings("rawtypes")
 	public abstract String getSelectQuery(Class clazz, String tableAlias) throws SQLException, QueryBuilderException;
 	
 	/**
 	 * It builds the lock (select) statement from the base query.
 	 * @param clazz Class of the bean 
 	 * @return The final SQL statement
 	 * @throws SQLException
 	 * @throws QueryBuilderException
 	 */	
 	@SuppressWarnings("rawtypes")
 	public abstract String getLockQuery(Class clazz) throws SQLException, QueryBuilderException;
 		
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL alias of the table 
 	 * @see {@link SQLExecute#getLockQuery()}
 	 */	
 	@SuppressWarnings("rawtypes")
 	public abstract String getLockQuery(Class clazz, String tableAlias) throws SQLException, QueryBuilderException;	
 
 	/**
 	 * Returns the DataSource connection URL String
 	 * 
 	 * @param ds The DataSource object (have to be instance of org.apache.commons.dbcp.BasicDataSource)
 	 * @return The connection URL
 	 * @throws SQLException
 	 */
 	public static String getDriverClassByDataSource(DataSource ds) throws SQLException {
 		if (ds == null) throw new SQLException(NO_DATASOURCE);
 		if (!(ds instanceof BasicDataSource)) throw new SQLException(BASIC_DATASOURCE_OBJECT_NEEDED);
 		return ((BasicDataSource)ds).getDriverClassName();
 	}
 
 	/**
 	 * Returns the DataSource driver class name
 	 * 
 	 * @param ds The DataSource object (have to be instance of org.apache.commons.dbcp.BasicDataSource)
 	 * @return The driver class name
 	 * @throws SQLException
 	 */
 	public static String getDataSourceUrlByDataSource(DataSource ds) throws SQLException {
 		if (ds == null) throw new SQLException(NO_DATASOURCE);
 		if (!(ds instanceof BasicDataSource)) throw new SQLException(BASIC_DATASOURCE_OBJECT_NEEDED);
 		return ((BasicDataSource)ds).getUrl();
 	}
 
 	/**
 	 * Returns RDMS dependent SQLExecuter. (Apache DBCP required).
 	 * The currently supported engines are: MYSQL, HSQLDB, FIREBIRD, ORACLE
 	 * 
 	 * @param ds The dataSource object
 	 * @return SQL Execute Object (optimized for dialect)
 	 * @throws SQLException
 	 */
 	@SuppressWarnings("rawtypes")
 	public static SQLExecute<?> getExecuterByDataSource(DataSource ds) throws SQLException {
 		String driverClass = getDriverClassByDataSource(ds);
 		
 		SQLExecute<?> executer;
 		if (driverClass.equals(JdbcDrivers.MYSQL.getDriverClass())) executer = new MySqlExecute(-1);
 		else if (driverClass.equals(JdbcDrivers.HSQLDB.getDriverClass())) executer = new HSqlDbExecute(-1);
 		else if (driverClass.equals(JdbcDrivers.FIREBIRD.getDriverClass())) executer = new FirebirdExecute(-1);
 		else if (driverClass.equals(JdbcDrivers.ORACLE.getDriverClass())) executer = new OracleExecute(-1);
 		else if (driverClass.equals(JdbcDrivers.ORACLE2.getDriverClass())) executer = new OracleExecute(-1);
 
 		else throw new SQLException(THIS_TYPE_OF_JDBC_DIALECT_IS_NOT_IMPLEMENTED+": "+driverClass);
 		executer.jdbcDriverClass = driverClass;
 		return executer;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param params parameter-value pair map 
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class<T> targetClass, String tableAlias, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public void prepareQueryStatement(Connection connection, 
 			Class targetClass, String tableAlias, QueryBuilder builder, Map<String, Object> params, boolean prepare) throws Exception {
 		
 		if (connection == null) throw new SQLException(CONNECTION_IS_NULL);		
 		
 		this.builder = builder;
 				
 		lastSQLStatement = getSelectQuery(targetClass, tableAlias);
 		lastSQLStatementParameters.clear();
 		
 		//lastSQLStatement = sb.toString();
 		//Object[] sqlParams = getSQLParameters(processSQLParameters(lastSQLStatement), params);
 
 		preparedStatement = connection.prepareStatement(processSQLParameters(lastSQLStatement).getSqlStatement());
 		
 		lastSQLStatementParameters.clear();
 		
 		preparedConnection = connection;
 		//return queryEntities(connection, targetClass, tableAlias, builder, null, true);
 	}	
 	
 	/**
 	 * Query entites from database. The resulset mapped by defult with bean's javax.persistence.Column annotations. 
 	 * If an annotation is not found then the field name is the resultset column name (The _ character are deleted).
 	 * 
 	 * @param Connection SQL Connection
 	 * @param targetClass
 	 * @param builder Query builder
 	 * @return List of bean objects
 	 * @throws Exception
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public void prepareQueryStatement(Connection connection, 
 			Class targetClass, QueryBuilder builder) throws Exception {
 		prepareQueryStatement(connection, targetClass, "", builder);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL alias of the table 
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class<T> targetClass, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public void prepareQueryStatement(Connection connection, 
 			Class targetClass, String tableAlias, QueryBuilder builder) throws Exception {
 		prepareQueryStatement(connection, targetClass, tableAlias, builder, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param params parameter-value pair map 
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class<T> targetClass, String tableAlias, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public void prepareQueryStatement(Connection connection, 
 			Class targetClass, String tableAlias, QueryBuilder builder, Map<String, Object> params) throws Exception {
 		prepareQueryStatement(connection, targetClass, tableAlias, builder, params, false);
 	}
 	
 	    
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL alias of the table 
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class<T> targetClass, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public void prepareQueryStatement(Connection connection, QueryBuilder builder) throws Exception {
 		prepareQueryStatement(connection, HashMap.class,  "", builder, null);
 	}
     
 	///////////////
 	
 	private String convertStreamToString(InputStream is) throws IOException {
 		/*
 		 * To convert the InputStream to String we use the
 		 * Reader.read(char[] buffer) method. We iterate until the
 		 * Reader return -1 which means there's no more data to
 		 * read. We use the StringWriter class to produce the string.
 		 */
 		if (is != null) {
 			Writer writer = new StringWriter();
 
 			char[] buffer = new char[1024];
 			try {
 				Reader reader = new BufferedReader(new InputStreamReader(is,
 						"UTF-8"));
 				int n;
 				while ((n = reader.read(buffer)) != -1) {
 					writer.write(buffer, 0, n);
 				}
 			} finally {
 				is.close();
 			}
 			return writer.toString();
 		} else {
 			return "";
 		}
 	}
 	
 	private String convertReaderToString(Reader reader) throws IOException {
 		StringBuffer data = new StringBuffer(1000);
 		char[] buf = new char[1024];
         int numRead=0;
         while((numRead=reader.read(buf)) != -1){
             String readData = String.valueOf(buf, 0, numRead);
             data.append(readData);
             buf = new char[1024];
         }
         reader.close();
         return data.toString();	 
     }
 
 	/**
 	 * Query entites from database. The resulset mapped by defult with bean's javax.persistence.Column annotations. 
 	 * If an annotation is not found then the field name is the resultset column name (The _ character are deleted).
 	 * 
 	 * @param Connection SQL Connection
 	 * @param targetClass
 	 * @param builder Query builder
 	 * @return List of bean objects
 	 * @throws Exception
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> queryEntities(Connection connection, 
 			Class targetClass, QueryBuilder builder) throws Exception {
 		return queryEntities(connection, targetClass, "", builder);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL alias of the table 
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class<T> targetClass, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> queryEntities(Connection connection, 
 			Class targetClass, String tableAlias, QueryBuilder builder) throws Exception {
 		return queryEntities(connection, targetClass, tableAlias, builder, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param params parameter-value pair map 
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class<T> targetClass, String tableAlias, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> queryEntities(Connection connection, 
 			Class targetClass, String tableAlias, QueryBuilder builder, Map<String, Object> params) throws Exception {
 		return queryEntities(connection, targetClass, tableAlias, builder, params, false);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param params parameter-value pair map 
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class<T> targetClass, String tableAlias, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public List<T> queryEntities(Connection connection, 
 			Class targetClass, String tableAlias, QueryBuilder builder, Map<String, Object> params, boolean prepare) throws Exception {
 		
 		prepareQueryStatement(connection, targetClass, tableAlias, builder, params, prepare);
 
 		// TODO Templateket kezelni
 		QueryRunner run = new QueryRunner();
 		ResultSetHandler<List<T>> rh = new BeanListHandler<T>(targetClass, new BasicRowProcessor(new DbStandardBeanProcessor()));
 
 		StatementWithParameter statement =  processSQLParameters(lastSQLStatement);
 		
 		Object[] sqlParams = getSQLParameters(statement.getParameters(), params);
 		
 		return run.query(connection, statement.getSqlStatement(), rh, sqlParams);
 	}	
 	
     
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL alias of the table 
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class<T> targetClass, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<Map<String, ?>> queryEntities(Connection connection, QueryBuilder builder) throws Exception {
 		return queryEntities(connection, "", builder, null);
 	}
 
 	
 
 	// 
 	private ResultSetHandler<List<Map<String, ?>>> mapResultSetHandler = new ResultSetHandler<List<Map<String, ?>>>() {
 	    public List<Map<String, ?>> handle(ResultSet rs) throws java.sql.SQLException {
 	    	
 	        ResultSetMetaData meta = rs.getMetaData();
 	        lastStatementFields.clear();
 	        for (int i = 0; i < meta.getColumnCount(); i++) {
 	        	String columnName = meta.getColumnName(i+1);
 	        	lastStatementFields.add(columnName);
 	        }
 
 	        List<Map<String, ?>> result = new ArrayList<Map<String,?>>();
 	        while (rs.next()) {
 	        	HashMap<String, Object> record = new HashMap<String, Object>();
 	        	for (int i = 0; i < meta.getColumnCount(); i++) {
 	        		String columnName = meta.getColumnName(i+1);
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
 							record.put(columnName, convertStreamToString(rs.getBlob(i+1).getBinaryStream()));
 						} catch (IOException e) {
 						}
 	        		} else if (Types.CLOB == columnType) {
 	        			try {
 							record.put(columnName, convertReaderToString(rs.getCharacterStream(i+1)));
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
 
 	
 	/**
 	 * {@inheritDoc}
 	 * @param params parameter-value pair map 
 	 * @see {@link SQLExecute#queryEntities(Connection connection, Class<T> targetClass, String tableAlias, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public List<Map<String, ?>> queryEntities(Connection connection, 
 		  String tableAlias, QueryBuilder builder, Map<String, Object> params) throws Exception {
 		
 		if (connection == null) throw new SQLException(CONNECTION_IS_NULL);
 		
 		this.builder = builder;
 		
 		// Create a ResultSetHandler implementation to convert the
 		// first row into an Object[].
 
 		// Create a QueryRunner that will use connections from
 		// the given DataSource
 		QueryRunner run = new QueryRunner();
 
 		lastSQLStatement = getSelectQuery(HashMap.class, tableAlias);
 		lastSQLStatementParameters.clear();
 
 		StatementWithParameter statement =  processSQLParameters(lastSQLStatement);
 		
 		Object[] sqlParams = getSQLParameters(statement.getParameters(), params);
 		
 		return run.query(connection, statement.getSqlStatement(), mapResultSetHandler, sqlParams);
 	}
 	
 	/**
 	 * Lock entities in database. The resulset mapped by defult with bean's javax.persistence.Column annotations. 
 	 * If an annotation is not found then the field name is the resultset column name (The _ character are deleted).
 	 * 
 	 * @param Connection SQL Connection
 	 * @param targetClass
 	 * @param builder Query builder
 	 * @return List of bean objects
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public List<T> lockEntities(Connection connection, 
 			Class targetClass, QueryBuilder builder) throws Exception {
 		return lockEntities(connection, targetClass, "", builder);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL alias of the table 
 	 * @see {@link SQLExecute#lockEntities(Connection connection, Class<T> targetClass, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public List<T> lockEntities(Connection connection, 
 			Class targetClass, String tableAlias, QueryBuilder builder) throws Exception {		
 		return lockEntities(connection, targetClass, tableAlias, builder, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param params parameter-value pair map 
 	 * @see {@link SQLExecute#lockEntities(Connection connection, Class<T> targetClass, String tableAlias, QueryBuilder builder)}
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	public List<T> lockEntities(Connection connection, 
 			Class targetClass, String tableAlias, QueryBuilder builder, Map<String, Object> params) throws Exception {		
 		if (connection == null) throw new SQLException(CONNECTION_IS_NULL);		
 		
 		this.builder = builder;
 		
 		// TODO Templateket kezelni
 		QueryRunner run = new QueryRunner();
 		ResultSetHandler<List<T>> rh = new BeanListHandler<T>(targetClass, new BasicRowProcessor(new DbStandardBeanProcessor()));
 		
 		lastSQLStatement = getLockQuery(targetClass, tableAlias);
 		lastSQLStatementParameters.clear();
 			
 		StatementWithParameter statement =  processSQLParameters(lastSQLStatement);
 		
 		Object[] sqlParams = getSQLParameters(statement.getParameters(), params);
 
 		return run.query(connection, statement.getSqlStatement(), rh, sqlParams);
 	}
 	
 	/**
 	 * Locks one entity in database. The given bean have to be annotated with javax.persistence.Entity and javax.persistence.Id
 	 * 
 	 * @param Connection SQL Connection
 	 * @param entity
 	 * @throws Exception
 	 */	
 	public void lockEntity(Connection connection, T entity) throws Exception {		
 		if (entity == null) throw new SQLException(ENTITY_IS_NULL);		
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		if (idColumn == null || "".equalsIgnoreCase(idColumn)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity);
 		
 		QueryBuilder builder = new SimpleBeanSQLQueryBuilder(entity.getClass()); 
 		builder.setWhere(new AndOperator(new EqualCriteria<Object>(idColumn, objs.get(idColumn))));
 		
 		lockEntities(connection, entity.getClass(), builder); 
 	}	
 	
 	/**
 	 * Delete entities from database.
 	 *  
 	 * @param connection SQL Connection
 	 * @param clazz
 	 * @param Object condition SQL condition for QueryBuilder.setParams  
 	 * @throws Exception
 	 */	
 	@SuppressWarnings("rawtypes")
 	public void deleteEntities(Connection connection, Class clazz, Object condition) throws Exception {
 		deleteEntities(connection, clazz, "", condition);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL alias of the table 
 	 * @see {@link SQLExecute#deleteEntities(Connection connection, Class<T> clazz, Object condition)}
 	 */	
 	@SuppressWarnings("rawtypes")
 	public void deleteEntities(Connection connection, Class clazz, String tableAlias, Object condition) throws Exception {
 		deleteEntities(connection, clazz, tableAlias, condition, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param params parameter-value pair map 
 	 * @see {@link SQLExecute#deleteEntities(Connection connection, String tableAlias, Class<T> clazz, Object condition)}
 	 */	
 	@SuppressWarnings("rawtypes")
 	public void deleteEntities(Connection connection, Class clazz, String tableAlias, Object condition, Map<String, Object> params) throws Exception {
 		if (connection == null) throw new SQLException(CONNECTION_IS_NULL);
 		String tableName = AnnotationHelper.getTableName(clazz);
 		if (tableName == null || "".equalsIgnoreCase(tableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append("DELETE FROM "+tableName);
 		if (tableAlias != null && !"".equals(tableAlias)) sb.append(" AS "+tableAlias);
 		if (condition != null) {
 			QueryBuilder builder = new SimpleSQLQueryBuilder("");
 			builder.setWhere(condition);
 			sb.append(" WHERE "+builder.buildWhere());
 		}
 		
 		lastSQLStatement = sb.toString();
 		StatementWithParameter statement =  processSQLParameters(lastSQLStatement);
 		
 		Object[] sqlParams = getSQLParameters(statement.getParameters(), params);
 
 		PreparedStatement stm = connection.prepareStatement(statement.getSqlStatement());
 				
 		lastSQLStatementParameters.clear();
 		int idx = 0;
 		while (idx < sqlParams.length) {
 			stm.setObject(idx + 1, sqlParams[idx]);
 			lastSQLStatementParameters.add(sqlParams[idx]);
 			idx++;
 		}		
 		
 		stm.execute();
 	}
 	
 	/**
 	 * Delete one entity from database. The given bean have to be annotated with javax.persistence.Entity and javax.persistence.Id
 	 * 
 	 * @param connection SQL Connection
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void deleteEntity(Connection connection, T entity) throws Exception {
 		if (entity == null) throw new SQLException(ENTITY_IS_NULL);		
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		if (idColumn == null || "".equalsIgnoreCase(idColumn)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity);
 				
 		deleteEntities(connection, entity.getClass(), new AndOperator(new EqualCriteria<Object>(idColumn, objs.get(idColumn))));		
 	}	
 
 	/**
 	 * Insert one entity into database. The given bean have to be annotated with javax.persistence.Entity and javax.persistence.Id
 	 * 
 	 * @param connection SQL Connection
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void insertEntity(Connection connection, T entity) throws Exception {
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity);
 		String tableName = AnnotationHelper.getTableName(entity);
 		if (tableName == null || "".equalsIgnoreCase(tableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		StringBuffer sb2 = new StringBuffer();
 		sb.append("INSERT INTO "+AnnotationHelper.getTableName(entity)+" (");
 		sb2.append("(");
 		boolean first = true;
 		for (String key : objs.keySet()) {
 			if (objs.get(key) != null) {
 				if (!first) {sb.append(","); sb2.append(",");} else first = false;
 				sb.append(key);
 				sb2.append("?");
 			}
 		}
 		
 		lastSQLStatement = sb.toString()+") VALUES "+sb2.toString()+")";
 		PreparedStatement stm = connection.prepareStatement(lastSQLStatement);
 		
 		lastSQLStatementParameters.clear();
 		int idx = 1;
 		for (Entry<String, Object> item : objs.entrySet()) {
 			Object param = item.getValue();
 			if (param != null) {
 				if (param instanceof java.util.Date) {
 					java.sql.Date paramD = new java.sql.Date(((java.util.Date)param).getTime());
 					param = paramD;
 				}
 				stm.setObject(idx, param);
 				lastSQLStatementParameters.add(param);
 				idx++;
 			}
 		}
 	
 		stm.execute();
 	}
 		
 	/**
 	 * Update entities in database. The given bean have to be annotated with javax.persistence.Entity.
 	 * 
 	 * @param connection SQL Connection
 	 * @param entity The bean
 	 * @param fields list of the updateabe fields
 	 * @param Object condition SQL condition for QueryBuilder.setParams
 	 * @throws Exception
 	 */
 	public void updateEntities(Connection connection, T entity, List<String> fields, Object condition) throws Exception {
 		updateEntities(connection, entity, "", fields, condition);
 	}
 	 
 	/**
 	 * {@inheritDoc}
 	 * @see {@link updateEntities#updateEntity(Connection connection, T entity, List<String> fields, Object condition)}
 	*/		 
 	public void updateEntities(Connection connection, T entity, String[] fields, Object condition) throws Exception {
 		List<String> list = new ArrayList<String>(Arrays.asList(fields));
 		updateEntities(connection, entity, "", list, condition);
 	}
 	 
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL alias of the table 
 	 * @see {@link updateEntities#updateEntity(Connection connection, T entity, List<String> fields, Object condition)}
 	 */	 	 
 	public void updateEntities(Connection connection,T entity, String tableAlias, String[] fields, Object condition) throws Exception {
 		List<String> list = new ArrayList<String>(Arrays.asList(fields));
 		updateEntities(connection, entity, tableAlias, list, condition);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param tableAlias SQL alias of the table 
 	 * @see {@link updateEntities#updateEntity(Connection connection, T entity, List<String> fields, Object condition)}
 	 */	 
 	public void updateEntities(Connection connection,T entity, String tableAlias, List<String> fields, Object condition) throws Exception {
 		updateEntities(connection, entity, tableAlias, fields, condition, null);
 	}
 
 	/**
 	 * {@inheritDoc}
 	 * @param params parameter-value pair map 
 	 * @see {@link updateEntities#updateEntity(Connection connection, T entity, List<String> fields, Object condition)}
 	 */	 
 	public void updateEntities(Connection connection,T entity, String tableAlias, List<String> fields, Object condition, Map<String, Object> params) throws Exception {
 		if (entity == null) throw new SQLException(ENTITY_IS_NULL);		
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		String tableName = AnnotationHelper.getTableName(entity);		
 		if (tableName == null || "".equalsIgnoreCase(tableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		if (idColumn == null || "".equalsIgnoreCase(idColumn)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		if ((fields != null) && (fields.size() > 0)) {
 			String idFieldName = AnnotationHelper.findFieldByAnnotationClass(entity.getClass(),Id.class).getName();
 			fields.add(idFieldName);	
 		}
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity, fields);
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append("UPDATE "+tableName+" "+tableAlias+" SET ");
 		boolean first = true;
 		for (String key : objs.keySet()) {
 			if (!key.equals(idColumn)) {
 				if (!first) {sb.append(",");} else first = false;
 				if (tableAlias.equals("")) {
 					sb.append(key+" = ?");
 				}
 				else {
 					sb.append(tableAlias+"."+key+" = ?");
 				}
 			}			
 		}	
 		if (condition != null) {
 			QueryBuilder builder = new SimpleSQLQueryBuilder("");
 			builder.setWhere(condition);
 			sb.append(" WHERE "+builder.buildWhere());
 		}
 		
 		lastSQLStatement = sb.toString();
 		
 		StatementWithParameter statement =  processSQLParameters(lastSQLStatement);
 		
 		Object[] sqlParams = getSQLParameters(statement.getParameters(), params);
 
 		PreparedStatement stm = connection.prepareStatement(statement.getSqlStatement());
 		
 		lastSQLStatementParameters.clear();
 		int idx = 1;
 		for (Entry<String, Object> item : objs.entrySet()) {
 			String key = item.getKey();
 			if (!key.equals(idColumn)) {
 				Object param = objs.get(key);
 				if (param instanceof java.util.Date) {
 					java.sql.Date paramD = new java.sql.Date(((java.util.Date)param).getTime());
 					param = paramD;					
 				} 
 				stm.setObject(idx, param);
 				lastSQLStatementParameters.add(param);
 				idx++;
 			}
 		}
 		
 		int idx2 = 0;
 		while (idx2 < sqlParams.length) {
 			stm.setObject(idx + idx2, sqlParams[idx2]);
 			lastSQLStatementParameters.add(sqlParams[idx2]);
 			idx2++;
 		}		
 		
 		stm.execute();		
 	}
 	
 	/**
 	 * Update one entity in database. The given bean have to be annotated with javax.persistence.Entity and javax.persistence.Id
 	 * 
 	 * @param connection SQL Connection
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void updateEntity(Connection connection, T entity) throws Exception {
 		updateEntity(connection, entity, (List<String>)null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of the updateabe fields
 	 * @see {@link SQLExecute#updateEntity(Connection connection, T entity)}
 	 */	
 	public void updateEntity(Connection connection, T entity, String[] fields) throws Exception {
 		List<String> list = new ArrayList<String>(Arrays.asList(fields));		
 		updateEntity(connection, entity, list);
 	}	
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of the updateabe fields
 	 * @see {@link SQLExecute#updateEntity(Connection connection, T entity)}
 	 */	
 	public void updateEntity(Connection connection, T entity, List<String> fields) throws Exception {
 		if (entity == null) throw new SQLException(ENTITY_IS_NULL);
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		if (idColumn == null || "".equalsIgnoreCase(idColumn)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		if ((fields != null) && (fields.size() > 0)) {
 			String idFieldName = AnnotationHelper.findFieldByAnnotationClass(entity.getClass(),Id.class).getName();
 			fields.add(idFieldName);	
 		}		
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity, fields);
 		
 		updateEntities(connection, entity, "", fields, new AndOperator(new EqualCriteria<Object>(idColumn, objs.get(idColumn))));	
 	}	
 	
 	/**
 	 * Prepare an insert statement.
 	 * 
 	 * @param connection
 	 * @param targetClass 
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareInsertStatement(Connection connection, Class targetClass) throws Exception {
 		prepareInsertStatement(connection,targetClass,(List<String>)null);
 	}	
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields  
 	 * @see {@link updateEntities#prepareInsertStatement(Connection connection, Class<T> targetClass)}
 	 */		
 	@SuppressWarnings("rawtypes")
 	public void prepareInsertStatement(Connection connection, Class targetClass, String[] fields) throws Exception {
 		List<String> list = new ArrayList<String>(Arrays.asList(fields));
 		prepareInsertStatement(connection,targetClass,list);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields  
 	 * @see {@link updateEntities#prepareInsertStatement(Connection connection, Class<T> targetClass)}
 	 */	
 	@SuppressWarnings("rawtypes")
 	public void prepareInsertStatement(Connection connection, Class targetClass, List<String> fields) throws Exception {
 		preparedType = StatementType.INSERT;
 		preparedStatementClass = targetClass;
 		Set<String> columns = AnnotationHelper.getClassColumnNames(targetClass, fields);
 		String tableName = AnnotationHelper.getTableName(targetClass);
 		if (tableName == null || "".equalsIgnoreCase(tableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		StringBuffer sb2 = new StringBuffer();
 		sb.append("INSERT INTO "+AnnotationHelper.getTableName(targetClass)+" (");
 		sb2.append("(");
 		boolean first = true;
 		prepareStatementElements = new ArrayList<String>();
 		for (String columnName : columns) {
 			prepareStatementElements.add(columnName);
 			if (!first) {sb.append(","); sb2.append(",");} else first = false;
 			sb.append(columnName);
 			sb2.append("?");
 		}
 		
 		lastSQLStatement = sb.toString()+") VALUES "+sb2.toString()+")";
 		preparedStatement = connection.prepareStatement(lastSQLStatement);
 		
 		lastSQLStatementParameters.clear();
 		
 		preparedConnection = connection;
 	}
 
 	/**
 	 * Prepare an update statement.
 	 * 
 	 * @param connection
 	 * @param targetClass
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(Connection connection, Class targetClass) throws Exception {
 		prepareUpdateStatement(connection, targetClass, (List<String>)null);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields  
 	 * @see {@link updateEntities#prepareUpdateStatement(Connection connection, Class<T> targetClass)}
 	 */	
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(Connection connection, Class targetClass, String[] fields) throws Exception {
 		List<String> list =  new ArrayList<String>(Arrays.asList(fields));
 		prepareUpdateStatement(connection, targetClass, list);
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 * @param fields list of fields  
 	 * @see {@link updateEntities#prepareUpdateStatement(Connection connection, Class<T> targetClass)}
 	 */	
 	@SuppressWarnings("rawtypes")
 	public void prepareUpdateStatement(Connection connection, Class targetClass, List<String> fields) throws Exception {
 		preparedType = StatementType.UPDATE;
 		preparedStatementClass = targetClass;
 		Set<String> columns = AnnotationHelper.getClassColumnNames(targetClass, fields);
 		String idColumn = AnnotationHelper.getIdColumnName(targetClass);
 		String tableName = AnnotationHelper.getTableName(targetClass);
 		if (tableName == null || "".equalsIgnoreCase(tableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		if (idColumn == null || "".equalsIgnoreCase(idColumn)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ID_ANNOTATION);
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append("UPDATE "+tableName+" SET ");
 		boolean first = true;
 		prepareStatementElements = new ArrayList<String>();
 		for (String columnName : columns) {
 			if (!columnName.equals(idColumn)) {
 				if (!first) {sb.append(",");} else first = false;
 				prepareStatementElements.add(columnName);
 				sb.append(columnName+" = ?");
 			}
 		}	
 		sb.append(" WHERE "+idColumn+" = ?");
 		
 		lastSQLStatement = sb.toString();
 		preparedStatement = connection.prepareStatement(lastSQLStatement);
 		
 		lastSQLStatementParameters.clear();
 		
 		preparedConnection = connection;
 	}
 	
 	/**
 	 * Prepare a delete statement.
 	 * 
 	 * @param connection
 	 * @param targetClass 
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void prepareDeleteStatement(Connection connection, Class targetClass) throws Exception {
 
 		//TODO : AMOLNAR
 	}
 	
 	
 	/**
 	 * Insert one entity with prepared statement.
 	 * 
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void insertEntityWithPreparedStatement(T entity) throws Exception {
 		if (preparedType == null || preparedStatementClass == null || preparedStatement == null || preparedConnection == null) throw new SQLException("The statement is not prepared");
 		if (preparedType != StatementType.INSERT) throw new SQLException(STATEMENT_TYPE_IS_NOT_INSERT);
 		if (entity.getClass() != preparedStatementClass) throw new SQLException(ENTITY_TYPE_MISMATCH);
 		
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity);
 		
 		lastSQLStatementParameters.clear();
 		int idx = 1;
 		for (String field : prepareStatementElements) {
 			Object param = objs.get(field);
 			if (param instanceof java.util.Date) {
 				java.sql.Date paramD = new java.sql.Date(((java.util.Date)param).getTime());
 				param = paramD;
 			}
 			preparedStatement.setObject(idx, param);
 			lastSQLStatementParameters.add(param);
 			idx++;
 		}
 		
 		preparedStatement.execute();
 		
 		if (preparedStatement.getUpdateCount() != 1) {
 			throw new java.sql.SQLException(INSERT_UNSUCCESSFULL);
 		}
 	}
 	
 	/**
 	 * Update one entity with prepared statement.
 	 * 
 	 * @param entity The bean
 	 * @throws Exception
 	 */
 	public void updateEntityWithPreparedStatement(T entity) throws Exception {
 		if (preparedType == null || preparedStatementClass == null || preparedStatement == null || preparedConnection == null) throw new SQLException("The statement is not prepared");
 		if (preparedType != StatementType.UPDATE) throw new SQLException(STATEMENT_TYPE_IS_NOT_UPDATE);
 		if (entity.getClass() != preparedStatementClass) throw new SQLException(ENTITY_TYPE_MISMATCH);
 
 		Map<String, Object> objs = AnnotationHelper.getObjectAsMap(entity);
 		
 		lastSQLStatementParameters.clear();
 		String idColumn = AnnotationHelper.getIdColumnName(entity);
 		int idx = 1;
 		for (String key : prepareStatementElements) {
 			if (!key.equals(idColumn)) {
 				Object param = objs.get(key);
 				if (param instanceof java.util.Date) {
 					java.sql.Date paramD = new java.sql.Date(((java.util.Date)param).getTime());
 					param = paramD;
 				} else {
 					
 				}
 				preparedStatement.setObject(idx, param);
 				lastSQLStatementParameters.add(param);
 				idx++;
 			}
 		}
 		preparedStatement.setObject(idx, objs.get(idColumn));
 		lastSQLStatementParameters.add(objs.get(idColumn));
 		
 		preparedStatement.execute();
 		
 		if (preparedStatement.getUpdateCount() != 1) {
 			throw new java.sql.SQLException(UPDATE_UNSUCCESSFULL);
 		}		
 	}
 	
 	@SuppressWarnings("rawtypes")
 	public void insertSelect(
 		Connection connection, 
 		Class<T> insertClass, String[] insertFields, 
 		Class selectClass, String tableAlias, String[] selectFields, Object selectCondition, Map<String, Object> params) 
 		throws java.sql.SQLException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, QueryBuilderException{
 				
 		List<String> list1 = new ArrayList<String>(Arrays.asList(insertFields));
 		List<String> list2 = new ArrayList<String>(Arrays.asList(selectFields));
 		
 		insertSelect(connection, insertClass, list1, selectClass, tableAlias, list2, selectCondition, params);
 	}
 		
 	@SuppressWarnings("rawtypes")
 	public void insertSelect(
 		Connection connection, 
 		Class insertClass, List<String> insertFields, 
 		Class selectClass, String tableAlias, List<String> selectFields, Object selectCondition, Map<String, Object> params) 
 		throws java.sql.SQLException, SQLException, IllegalAccessException, InvocationTargetException, NoSuchMethodException, QueryBuilderException{
 		
 		String insertTableName = AnnotationHelper.getTableName(insertClass);		
 		if (insertTableName == null || "".equalsIgnoreCase(insertTableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}		
 		String selectTableName = AnnotationHelper.getTableName(selectClass);		
 		if (selectTableName == null || "".equalsIgnoreCase(selectTableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}		
 		
 		//insert
 		ArrayList<String> insertColumns = AnnotationHelper.getClassColumnNames(insertClass, insertFields, true);
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
 		
 		//select
 		ArrayList<String> selectColumns = AnnotationHelper.getClassColumnNames(selectClass, insertFields, true);
 		sb = new StringBuffer();
 		first = true;
 		for (String columnName : selectColumns) {
 			if (!first) {
 				sb.append(","); 
 			} 
 			else first = false;
 			if (tableAlias.equals("")) {
 				sb.append(columnName);
 			} else {
 				sb.append(tableAlias+"."+columnName);
 			}
 		}	
 		this.builder = new SimpleSQLQueryBuilder("SELECT " + sb.toString() +" FROM "+selectTableName);
 		if (selectCondition != null) {
 			this.builder.setWhere(selectCondition);
 		}
 		String select = getSelectQuery(selectClass, tableAlias).replace("*", sb.toString());
 		
 		lastSQLStatement = insert +"\n"+ select;
 		
 		StatementWithParameter statement =  processSQLParameters(lastSQLStatement);
 		
 		Object[] sqlParams = getSQLParameters(statement.getParameters(), params);
 
 		PreparedStatement stm = connection.prepareStatement(statement.getSqlStatement());
 		
 		lastSQLStatementParameters.clear();
 		
 		int idx = 0;
 		while (idx < sqlParams.length) {
 			stm.setObject(idx + 1, sqlParams[idx]);
 			lastSQLStatementParameters.add(sqlParams[idx]);
 			idx++;
 		}
 		
 		stm.execute();		
 	}
 			
 	/**
 	 * Create a table in database. The given bean have to be annotated with javax.persistence.Entity, javax.presistence.Column and javax.persistence.Id
 	 * 
 	 * @param connection SQL Connection
 	 * @param class The entity bean class
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void createTable(Connection connection, Class clazz) throws Exception {
 		
 		
 		String tableName = AnnotationHelper.getTableName(clazz);
 		if (tableName == null || "".equalsIgnoreCase(tableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		List<Field> fields = AnnotationHelper.getAllFields(clazz);
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
     			if (col.name() == null || col.name().equals("")) throw new SQLException(COLUMN_NAME_IS_UNDEFINED);
     			if (col.columnDefinition() == null || col.columnDefinition().equals("")) throw new SQLException(COLUMN_DEFINITION_IS_UNDEFINED);
     			sb.append(col.name());
     			sb.append(" "+col.columnDefinition());
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
     	
     	lastSQLStatement = sb.toString();
 		PreparedStatement stm = connection.prepareStatement(lastSQLStatement);
 		
 		lastSQLStatementParameters.clear();
 		
 		stm.execute();
 		connection.commit();
 		
 	}
 	
 	/**
 	 * Drop a table from database. The given bean have to be annotated with javax.persistence.Entity or javax.persistence.Table
 	 * 
 	 * @param connection SQL Connection
 	 * @param class The entity bean class
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public void dropTable(Connection connection, Class clazz) throws Exception {
 		
 		
 		String tableName = AnnotationHelper.getTableName(clazz);
 		if (tableName == null || "".equalsIgnoreCase(tableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		StringBuffer sb = new StringBuffer();
 		sb.append("DROP TABLE "+tableName);		
 
     	lastSQLStatement = sb.toString();
 		PreparedStatement stm = connection.prepareStatement(lastSQLStatement);
 		
 		lastSQLStatementParameters.clear();
 		
 		stm.execute();
 	}
 
 	
 
 
 	/**
 	 * Check tabe existence. The given bean have to be annotated with javax.persistence.Entity
 	 * 
 	 * @param connection SQL Connection
 	 * @param class The entity bean class
 	 * @throws Exception
 	 */
 	@SuppressWarnings("rawtypes")
 	public boolean existsTable(Connection connection, Class clazz) throws Exception {
 		
 		
 		String tableName = AnnotationHelper.getTableName(clazz);
 		if (tableName == null || "".equalsIgnoreCase(tableName)) {
 			throw new SQLException(CLASS_DOES_NOT_HAVE_ENTITY_ANNOTATION);
 		}
 		
 		DatabaseMetaData dbm = connection.getMetaData();
 		ResultSet tables = dbm.getTables((String)null, (String)null, tableName.toUpperCase(), (String[])null);
 		if (tables == null) 
 			return false;
 		if (tables.next()) {
 			// Table exists
 			return true;
 		}
 		else {
 			// Table does not exist
 			return false;
 		}
 	}
 
 	/**
 	 * Execute an SQL Script. The script contains only a single statement. 
 	 * @param connection
 	 * @param sql
 	 * @throws SQLException
 	 */
 	public void executeScript(Connection connection, File sql) throws SQLException {
 		executeScript(connection, sql, null, null);
 	}
 	
 	/**
 	 * Execute an SQL Script. The statements are separated with ;
 	 * 
 	 * @param connection
 	 * @param sql
 	 * @param section
 	 * @throws SQLException
 	 */
 	public void executeScript(Connection connection, File sql, String section) throws SQLException {
 		executeScript(connection, sql, section, ";");
 	}
 	
 	/**
 	 * Execute an SQL Script. The statements are separated with the given string.
 	 * 
 	 * @param connection
 	 * @param sql
 	 * @param section
 	 * @param separator
 	 * @throws SQLException
 	 */
 	public void executeScript(Connection connection, File sql, String section, String separator) throws SQLException {
 		String s            = new String();  
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
 
 	public ArrayList<String> getLastStatementFields() {
 		return lastStatementFields;
 	}
 
 	public void setLastStatementFields(ArrayList<String> lastStatementFields) {
 		this.lastStatementFields = lastStatementFields;
 	}
 	
 	
 }
