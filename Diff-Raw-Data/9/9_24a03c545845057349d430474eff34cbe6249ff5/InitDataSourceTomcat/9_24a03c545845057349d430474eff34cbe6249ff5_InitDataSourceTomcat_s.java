 package org.lightmare.jpa.datasource.tomcat;
 
 import java.io.IOException;
 import java.util.Map;
 import java.util.Properties;
 
 import javax.naming.Context;
 
 import org.apache.log4j.Logger;
 import org.apache.tomcat.jdbc.pool.DataSource;
 import org.apache.tomcat.jdbc.pool.PoolProperties;
 import org.lightmare.jndi.JndiManager;
 import org.lightmare.jpa.datasource.DataSourceInitializer;
 import org.lightmare.jpa.datasource.InitMessages;
 import org.lightmare.jpa.datasource.PoolConfig;
 
 /**
  * Initializes and bind to {@link Context} tomcat pooled {@link DataSource}
  * object
  * 
  * @author levan
  * 
  */
 public class InitDataSourceTomcat {
 
     public static final Logger LOG = Logger
 	    .getLogger(DataSourceInitializer.class);
 
     private static final String JDBC_INTERCEPTOR_KEY = "org.apache.tomcat.jdbc.pool.interceptor.ConnectionState;";
 
     private static final String JDBC_INTERCEPTOR_VALUE = "org.apache.tomcat.jdbc.pool.interceptor.StatementFinalizer";
 
     /**
      * Initializes appropriated driver and {@link DataSource} objects
      * 
      * @param properties
      * @return {@link DataSource}
      * @throws IOException
      */
     public static DataSource initilizeDataSource(Properties properties,
 	    PoolConfig poolConfig) throws IOException {
 
 	String driver = properties.getProperty("driver").trim();
 	String url = properties.getProperty("url").trim();
 	String user = properties.getProperty("user").trim();
 	String password = properties.getProperty("password").trim();
 
 	Map<Object, Object> configMap = poolConfig.merge(properties);
 
 	DataSource dataSource;
 	PoolProperties poolProperties = new PoolProperties();
 	poolProperties.setUrl(url);
 	poolProperties.setDriverClassName(driver);
 	poolProperties.setUsername(user);
 	poolProperties.setPassword(password);
 	poolProperties.setJmxEnabled(Boolean.TRUE);
 	poolProperties.setTestWhileIdle(Boolean.FALSE);
 	poolProperties.setTestOnBorrow(Boolean.TRUE);
 	poolProperties.setValidationQuery("SELECT 1");
 	poolProperties.setTestOnReturn(Boolean.FALSE);
 	poolProperties.setValidationInterval(30000);
 	poolProperties.setTimeBetweenEvictionRunsMillis(30000);
 	poolProperties.setMaxActive(PoolConfig.asInt(configMap,
 		PoolConfig.MAX_POOL_SIZE));
 	poolProperties.setInitialSize(PoolConfig.asInt(configMap,
 		PoolConfig.INITIAL_POOL_SIZE));
 	poolProperties.setMaxWait(10000);
 	poolProperties.setRemoveAbandonedTimeout(60);
 	poolProperties.setMinEvictableIdleTimeMillis(30000);
 	poolProperties.setMinIdle(10);
 	poolProperties.setLogAbandoned(Boolean.TRUE);
 	poolProperties.setRemoveAbandoned(Boolean.TRUE);
 	poolProperties.setJdbcInterceptors(String.format("%s%s",
 		JDBC_INTERCEPTOR_KEY, JDBC_INTERCEPTOR_VALUE));
 	dataSource = new DataSource();
 	dataSource.setPoolProperties(poolProperties);
 
 	return dataSource;
     }
 
     /**
      * Initializes and registers {@link DataSource} object in jndi by
      * {@link Properties} {@link Context}
      * 
      * @param poolingProperties
      * @param dataSource
      * @param jndiName
      * @throws IOException
      */
     public static void registerDataSource(Properties properties,
 	    PoolConfig poolConfig) throws IOException {
 	String jndiName = DataSourceInitializer.getJndiName(properties);
 	LOG.info(String.format(InitMessages.INITIALIZING_MESSAGE, jndiName));
 	try {
 	    DataSource dataSource = initilizeDataSource(properties, poolConfig);
 	    if (dataSource instanceof DataSource) {
 		JndiManager namingUtils = new JndiManager();
 		namingUtils.rebind(jndiName, dataSource);
 	    } else {
 		throw new IOException(String.format(
 			InitMessages.NOT_APPR_INSTANCE_ERROR, jndiName));
 	    }
	    LOG.info(String.format("Data source %s initialized", jndiName));
 	} catch (IOException ex) {
 	    LOG.error(
 		    String.format(InitMessages.COULD_NOT_INIT_ERROR, jndiName),
 		    ex);
 	} catch (Exception ex) {
 	    LOG.error(
 		    String.format(InitMessages.COULD_NOT_INIT_ERROR, jndiName),
 		    ex);
 	}
     }
 
     /**
      * Closes passed {@link javax.sql.DataSource} for shut down
      * 
      * @param dataSource
      */
     public static void cleanUp(javax.sql.DataSource dataSource) {
 
 	if (dataSource instanceof DataSource) {
 	    ((DataSource) dataSource).close();
 	}
     }
 }
