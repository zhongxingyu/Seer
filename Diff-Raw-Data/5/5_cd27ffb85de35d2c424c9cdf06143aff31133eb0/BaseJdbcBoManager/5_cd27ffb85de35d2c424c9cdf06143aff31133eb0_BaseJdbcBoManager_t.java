 package ddth.dasp.framework.bo.jdbc;
 
 import java.io.InputStream;
 import java.lang.reflect.Array;
 import java.sql.CallableStatement;
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.concurrent.ConcurrentMap;
 
 import org.apache.commons.lang3.StringUtils;
 import org.osgi.framework.BundleContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.MapMaker;
 
 import ddth.dasp.common.logging.JdbcLogEntry;
 import ddth.dasp.common.logging.JdbcLogger;
 import ddth.dasp.common.utils.JsonUtils;
 import ddth.dasp.common.utils.OsgiUtils;
 import ddth.dasp.common.utils.PropsUtils;
 import ddth.dasp.framework.bo.CacheBoManager;
 import ddth.dasp.framework.cache.NullValue;
 import ddth.dasp.framework.dbc.DbcpInfo;
 import ddth.dasp.framework.dbc.IJdbcFactory;
 import ddth.dasp.framework.dbc.JdbcUtils;
 import ddth.dasp.framework.utils.EhProperties;
 
 /**
  * Use this class as starting point for JDBC-based Business Object manager.
  * 
  * @author NBThanh <btnguyen2k@gmail.com>
  * @version 0.1.0
  */
 public abstract class BaseJdbcBoManager extends CacheBoManager implements IJdbcBoManager {
 
     private final static int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();
     private ThreadLocal<Connection> localConnection = new ThreadLocal<Connection>();
 
     private Logger LOGGER = LoggerFactory.getLogger(BaseJdbcBoManager.class);
     private IJdbcFactory jdbcFactory;
     private String dbDriver, dbConnUrl, dbUsername, dbPassword;
     private DbcpInfo dbcpInfo;
     private long maxConnectionLifetime = DbcpInfo.DEFAULT_MAX_CONNECTION_LIFETIME;
     private List<String> setupSqls;
     private Properties sqlProps = new EhProperties();
     private ConcurrentMap<String, SqlProps> cacheSqlProps = new MapMaker().concurrencyLevel(
             NUM_PROCESSORS).makeMap();
     private Object sqlPropsLocation;
     private int transactionIsolationLevel = Connection.TRANSACTION_READ_COMMITTED;
 
     protected int getTransactionIsolationLevel() {
         return transactionIsolationLevel;
     }
 
     public void setTransactionIsolationLevel(int transactionIsolationLevel) {
         this.transactionIsolationLevel = transactionIsolationLevel;
     }
 
     protected IJdbcFactory getJdbcFactory() {
         if (jdbcFactory != null) {
             return jdbcFactory;
         }
         /*
          * If the JDBC factory has not been set, try to get it from OSGi
          * container.
          */
         BundleContext bundleContext = getBundleContext();
         if (bundleContext != null) {
             return OsgiUtils.getService(bundleContext, IJdbcFactory.class);
         }
         return null;
     }
 
     public void setJdbcFactory(IJdbcFactory jdbcFactory) {
         this.jdbcFactory = jdbcFactory;
     }
 
     public void setDbDriver(String dbDriver) {
         this.dbDriver = dbDriver;
     }
 
     public void setDbConnUrl(String dbConnUrl) {
         this.dbConnUrl = dbConnUrl;
     }
 
     public void setDbUsername(String dbUsername) {
         this.dbUsername = dbUsername;
     }
 
     public void setDbPassword(String dbPassword) {
         this.dbPassword = dbPassword;
     }
 
     public List<String> getSetupSqls() {
         return setupSqls;
     }
 
     public void setSetupSqls(List<String> setupSqls) {
         this.setupSqls = setupSqls;
     }
 
     public DbcpInfo getDbcpInfo() {
         return dbcpInfo;
     }
 
     public void setDbcpInfo(DbcpInfo dbcpInfo) {
         this.dbcpInfo = dbcpInfo;
     }
 
     public long getMaxConnectionLifetime() {
         return maxConnectionLifetime;
     }
 
     public void setMaxConnectionLifetime(long maxConnectionLifetime) {
         this.maxConnectionLifetime = maxConnectionLifetime;
     }
 
     /**
      * Initializing method.
      */
     public void init() {
         loadSqlProps();
     }
 
     /**
      * Destruction method.
      */
     public void destroy() {
         // EMPTY
     }
 
     /**
      * Loads SQL properties. It's in {@link Properties} format.
      */
     protected void loadSqlProps() {
         Object sqlProps = getSqlPropsLocation();
         if (sqlProps instanceof Properties) {
             this.sqlProps.putAll((Properties) sqlProps);
         } else if (sqlProps instanceof InputStream) {
             Properties props = PropsUtils.loadProperties((InputStream) sqlProps);
             if (props != null) {
                 this.sqlProps.putAll(props);
             }
         } else if (sqlProps != null) {
             String location = sqlProps.toString();
             InputStream is = getClass().getResourceAsStream(location);
             Properties props = PropsUtils.loadProperties(is, location.endsWith(".xml"));
             if (props != null) {
                 this.sqlProps.putAll(props);
             }
         } else {
             String msg = "Can not load SQL properties from [" + sqlProps + "]!";
             LOGGER.warn(msg);
         }
     }
 
     /**
      * Gets the SQL properties location. The location can be either of:
      * 
      * <ul>
      * <li>{@link InputStream}: properties are loaded from the input stream.</li>
      * <li>{@link Properties}: properties are copied from this one.</li>
      * <li>{@link String}: properties are loaded from file (located within the
      * classpath) specified by this string.</li>
      * </ul>
      * 
      * @return location of the SQL properties
      */
     protected Object getSqlPropsLocation() {
         return sqlPropsLocation;
     }
 
     /**
      * Sets the SQL properties location. The location can be either of:
      * 
      * <ul>
      * <li>{@link InputStream}: properties are loaded from the input stream.</li>
      * <li>{@link Properties}: properties are copied from this one.</li>
      * <li>{@link String}: properties are loaded from file (located within the
      * classpath) specified by this string.</li>
      * </ul>
      * 
      * @param sqlPropsLocation
      */
     public void setSqlPropsLocation(Object sqlPropsLocation) {
         this.sqlPropsLocation = sqlPropsLocation;
     }
 
     /**
      * Gets a SQL property by name.
      * 
      * @param name
      * @return
      */
     @SuppressWarnings("unchecked")
     protected SqlProps getSqlProps(String name) {
         SqlProps result = cacheSqlProps.get(name);
         if (result == null) {
             String rawProps = sqlProps.getProperty(name);
             if (!StringUtils.isBlank(rawProps)) {
                 try {
                     Map<String, Object> props = JsonUtils.fromJson(rawProps, Map.class);
                     result = new SqlProps();
                     result.populate(props);
                     cacheSqlProps.put(name, result);
                 } catch (Exception e) {
                     LOGGER.warn(e.getMessage(), e);
                     result = null;
                 }
             }
         }
         return result;
     }
 
     /**
      * Runs setup SQLs for newly obtained {@link Connection}.
      * 
      * @param conn
      * @throws SQLException
      */
     protected void runSetupSqls(Connection conn) throws SQLException {
         if (setupSqls != null && setupSqls.size() > 0) {
             Statement stm = conn.createStatement();
             try {
                 for (String sql : setupSqls) {
                     stm.execute(sql);
                 }
             } finally {
                 stm.close();
             }
         }
     }
 
     /**
      * Real method to obtain a database connection from the JDBC factory.
      * 
      * @return
      * @throws SQLException
      */
     protected Connection _getConnection() throws SQLException {
         IJdbcFactory jdbcFactory = getJdbcFactory();
         Connection conn = jdbcFactory.getConnection(dbDriver, dbConnUrl, dbUsername, dbPassword,
                 maxConnectionLifetime, dbcpInfo);
         if (LOGGER.isDebugEnabled()) {
             String msg = "Opened JDBC connection [" + conn + "].";
             LOGGER.debug(msg);
         }
         if (conn != null) {
             conn.setAutoCommit(true);
             runSetupSqls(conn);
         }
         return conn;
     }
 
     /**
      * Real method to release an open database connection.
      * 
      * @param conn
      * @throws SQLException
      */
     protected void _releaseConnection(Connection conn) throws SQLException {
         IJdbcFactory jdbcFactory = getJdbcFactory();
         jdbcFactory.releaseConnection(conn);
         if (LOGGER.isDebugEnabled()) {
             String msg = "Closed JDBC connection [" + conn + "].";
             LOGGER.debug(msg);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean inTransaction() {
         return localConnection.get() != null;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Connection startTransaction() throws SQLException {
         return startTransaction(transactionIsolationLevel);
     }
 
     /**
      * {@inheritDoc}
      */
     public Connection startTransaction(int transactionIsolationLevel) throws SQLException {
         if (inTransaction()) {
             throw new SQLException("Transaction already started!");
         }
         Connection conn = _getConnection();
         conn.setAutoCommit(false);
         conn.setTransactionIsolation(transactionIsolationLevel);
         localConnection.set(conn);
         return conn;
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void cancelTransaction() throws SQLException {
         Connection conn = localConnection.get();
         if (conn == null) {
             throw new SQLException("Transaction has not started!");
         }
         try {
             try {
                 conn.rollback();
                 conn.setAutoCommit(true);
             } finally {
                 _releaseConnection(conn);
             }
         } finally {
             localConnection.remove();
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void finishTransaction() throws SQLException {
         Connection conn = localConnection.get();
         if (conn == null) {
             throw new SQLException("Transaction has not started!");
         }
         try {
             try {
                 conn.commit();
                 conn.setAutoCommit(true);
             } finally {
                 _releaseConnection(conn);
             }
         } finally {
             localConnection.remove();
         }
     }
 
     private void throwDbConnException(Connection conn, SQLException e) {
         if (conn == null) {
             String msg = "Can not create db connection [" + dbDriver + "/" + dbConnUrl + "]!";
             throw new RuntimeException(msg);
         }
         throw new RuntimeException(e);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public Connection getConnection() throws SQLException {
         Connection conn = localConnection.get();
         return conn != null ? conn : _getConnection();
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void releaseConnection(Connection conn) throws SQLException {
        Connection localConn = localConnection.get();
        if (conn != localConn) {
            _releaseConnection(conn);
        }
     }
 
     /**
      * Obtains and builds the {@link SqlProps}.
      * 
      * @param sqkKey
      * @return
      */
     protected SqlProps buildSqlProps(final Object sqlKey) {
         final String finalKey = (sqlKey instanceof Object[]) ? ((Object[]) sqlKey)[0].toString()
                 : sqlKey.toString();
         SqlProps sqlProps = null;
         SqlProps tempSqlProps = getSqlProps(finalKey);
         if (tempSqlProps != null) {
             sqlProps = tempSqlProps.clone();
         }
         if (sqlProps != null && sqlKey instanceof Object[]) {
             String sql = sqlProps.getSql();
             Object[] temp = (Object[]) sqlKey;
             for (int i = 1; i < temp.length; i++) {
                 sql = sql.replaceAll("\\{" + i + "\\}", temp[i] != null ? temp[i].toString() : "");
             }
             sqlProps.setSql(sql);
         }
         return sqlProps;
     }
 
     /**
      * Executes a COUNT query and returns the result.
      * 
      * @param sqlKey
      * @param params
      * @return
      * @throws SQLException
      */
     protected Long executeCount(final Object sqlKey, Map<String, Object> params)
             throws SQLException {
         return executeCount(sqlKey, params, null);
     }
 
     /**
      * Executes a COUNT query and returns the result.
      * 
      * @param sqlKey
      * @param params
      * @return
      * @throws SQLException
      */
     protected Long executeCount(final Object sqlKey, Map<String, Object> params,
             final String cacheKey) throws SQLException {
         Long result = null;
         if (!StringUtils.isBlank(cacheKey) && cacheEnabled()) {
             // get from cache
             Object temp = getFromCache(cacheKey);
             if (temp instanceof Long) {
                 result = (Long) temp;
             } else if (temp instanceof Number) {
                 result = ((Number) temp).longValue();
             }
             result = null;
         }
         if (result == null) {
             // cache missed
             SqlProps sqlProps = buildSqlProps(sqlKey);
             if (sqlProps == null) {
                 throw new SQLException("Can not retrieve SQL [" + sqlKey + "]!");
             }
             Connection conn = getConnection();
             PreparedStatement stm = null;
             ResultSet rs = null;
             if (conn == null) {
                 throwDbConnException(conn, null);
             }
             try {
                 long startTimestamp = System.currentTimeMillis();
                 String sql = sqlProps.getSql();
                 stm = JdbcUtils.prepareStatement(conn, sql, params);
                 rs = stm.executeQuery();
                 if (rs.next()) {
                     result = rs.getLong(1);
                 }
                 long endTimestamp = System.currentTimeMillis();
                 JdbcLogEntry jdbcLogEntry = new JdbcLogEntry(startTimestamp, endTimestamp, sql,
                         params);
                 JdbcLogger.log(jdbcLogEntry);
             } finally {
                 JdbcUtils.closeResources(null, stm, rs);
                 releaseConnection(conn);
             }
         }
         if (!StringUtils.isBlank(cacheKey) && cacheEnabled()) {
             // put to cache
             putToCache(cacheKey, result);
         }
         return result;
     }
 
     /**
      * Executes a non-SELECT query and returns the number of affected rows.
      * 
      * @param sqlKey
      * @param params
      * @return
      * @throws SQLException
      */
     protected long executeNonSelect(final Object sqlKey, Map<String, Object> params)
             throws SQLException {
         SqlProps sqlProps = buildSqlProps(sqlKey);
         if (sqlProps == null) {
             throw new SQLException("Can not retrieve SQL [" + sqlKey + "]!");
         }
         Connection conn = getConnection();
         PreparedStatement stm = null;
         if (conn == null) {
             throwDbConnException(conn, null);
         }
         try {
             long startTimestamp = System.currentTimeMillis();
             String sql = sqlProps.getSql();
             stm = JdbcUtils.prepareStatement(conn, sql, params);
             long result = stm.executeUpdate();
             long endTimestamp = System.currentTimeMillis();
             JdbcLogEntry jdbcLogEntry = new JdbcLogEntry(startTimestamp, endTimestamp, sql, params);
             JdbcLogger.log(jdbcLogEntry);
             return result;
         } finally {
             JdbcUtils.closeResources(null, stm, null);
             releaseConnection(conn);
         }
     }
 
     /**
      * Executes a SELECT query and returns the result as an array of records,
      * each record is a Map<String, Object>.
      * 
      * @param sqlKey
      * @param params
      * @return
      * @throws SQLException
      */
     protected Map<String, Object>[] executeSelect(final Object sqlKey, Map<String, Object> params)
             throws SQLException {
         return executeSelect(sqlKey, params, (String) null);
     }
 
     /**
      * Executes a SELECT query and returns the result as an array of records,
      * each record is a Map<String, Object>.
      * 
      * @param sqlKey
      * @param params
      * @param cacheKey
      * @return
      * @throws SQLException
      */
     @SuppressWarnings("unchecked")
     protected Map<String, Object>[] executeSelect(final Object sqlKey, Map<String, Object> params,
             final String cacheKey) throws SQLException {
         List<Map<String, Object>> result = null;
         if (!StringUtils.isBlank(cacheKey) && cacheEnabled()) {
             // get from cache
             Object temp = getFromCache(cacheKey);
             if (temp instanceof NullValue) {
                 result = new ArrayList<Map<String, Object>>();
             } else {
                 result = (List<Map<String, Object>>) temp;
             }
         }
         if (result == null) {
             // cache missed
             SqlProps sqlProps = buildSqlProps(sqlKey);
             if (sqlProps == null) {
                 throw new SQLException("Can not retrieve SQL [" + sqlKey + "]!");
             }
             Connection conn = getConnection();
             PreparedStatement stm = null;
             ResultSet rs = null;
             if (conn == null) {
                 throwDbConnException(conn, null);
             }
             try {
                 long startTimestamp = System.currentTimeMillis();
                 String sql = sqlProps.getSql();
                 result = new ArrayList<Map<String, Object>>();
                 stm = JdbcUtils.prepareStatement(conn, sql, params);
                 rs = stm.executeQuery();
                 ResultSetMetaData rsMetaData = rs != null ? rs.getMetaData() : null;
                 while (rs.next()) {
                     Map<String, Object> obj = new HashMap<String, Object>();
                     for (int i = 1, n = rsMetaData.getColumnCount(); i <= n; i++) {
                         String colLabel = rsMetaData.getColumnLabel(i);
                         if (StringUtils.isEmpty(colLabel)) {
                             colLabel = rsMetaData.getColumnName(i);
                         }
                         Object value = rs.getObject(colLabel);
                         obj.put(colLabel, value);
                     }
                     result.add(obj);
                 }
                 long endTimestamp = System.currentTimeMillis();
                 JdbcLogEntry jdbcLogEntry = new JdbcLogEntry(startTimestamp, endTimestamp, sql,
                         params);
                 JdbcLogger.log(jdbcLogEntry);
             } finally {
                 JdbcUtils.closeResources(null, stm, rs);
                 releaseConnection(conn);
             }
         }
         if (!StringUtils.isBlank(cacheKey) && cacheEnabled()) {
             // put to cache
             putToCache(cacheKey, result);
         }
         return result.toArray((Map<String, Object>[]) Array.newInstance(Map.class, 0));
     }
 
     /**
      * Executes a SELECT query and returns the result as an array of result,
      * each result is an instance of type {@link BaseJdbcBo}.
      * 
      * @param <T>
      * @param sqlKey
      * @param params
      * @param clazz
      * @return
      * @throws SQLException
      */
     protected <T extends BaseJdbcBo> T[] executeSelect(final Object sqlKey,
             Map<String, Object> params, Class<T> clazz) throws SQLException {
         return executeSelect(sqlKey, params, clazz, (String) null);
     }
 
     /**
      * Executes a SELECT query and returns the result as an array of result,
      * each result is an instance of type {@link BaseJdbcBo}.
      * 
      * @param <T>
      * @param sqlKey
      * @param params
      * @param clazz
      * @param cacheKey
      * @return
      * @throws SQLException
      */
     @SuppressWarnings("unchecked")
     protected <T extends BaseJdbcBo> T[] executeSelect(final Object sqlKey,
             Map<String, Object> params, Class<T> clazz, final String cacheKey) throws SQLException {
         Map<String, Object>[] dbResult = executeSelect(sqlKey, params, cacheKey);
         List<T> result = new ArrayList<T>();
         for (Map<String, Object> data : dbResult) {
             try {
                 T obj = createBusinessObject(clazz);
                 obj.populate(data);
                 result.add(obj);
             } catch (Exception e) {
                 throw new RuntimeException(e);
             }
         }
         return result.toArray((T[]) Array.newInstance(clazz, 0));
     }
 
     /**
      * Executes a stored procedure call.
      * 
      * @param sqlKey
      * @param params
      * @throws SQLException
      */
     protected void executeStoredProcedure(final Object sqlKey, Map<String, Object> params)
             throws SQLException {
         SqlProps sqlProps = buildSqlProps(sqlKey);
         if (sqlProps == null) {
             throw new SQLException("Can not retrieve SQL [" + sqlKey + "]!");
         }
         Connection conn = getConnection();
         CallableStatement stm = null;
         if (conn == null) {
             throwDbConnException(conn, null);
         }
         try {
             long startTimestamp = System.currentTimeMillis();
             String sql = sqlProps.getSql();
             stm = (CallableStatement) JdbcUtils.prepareStatement(conn, sql, params, true);
             stm.execute();
             long endTimestamp = System.currentTimeMillis();
             JdbcLogEntry jdbcLogEntry = new JdbcLogEntry(startTimestamp, endTimestamp, sql, params);
             JdbcLogger.log(jdbcLogEntry);
         } finally {
             JdbcUtils.closeResources(null, stm, null);
             releaseConnection(conn);
         }
     }
 
     /**
      * Utility method to build parameter map from a list of objects.
      * 
      * @param params
      * @return
      */
     protected static Map<String, Object> buildParams(Object... params) {
         Map<String, Object> result = new HashMap<String, Object>();
         for (int i = 0, n = params.length / 2; i < n; i++) {
             String key = params[i * 2].toString();
             result.put(key, params[i * 2 + 1]);
         }
         return result;
     }
 }
