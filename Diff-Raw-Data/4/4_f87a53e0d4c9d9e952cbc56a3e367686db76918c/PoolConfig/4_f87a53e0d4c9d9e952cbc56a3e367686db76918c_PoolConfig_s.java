 package org.lightmare.jpa.datasource;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import org.lightmare.utils.ObjectUtils;
 import org.lightmare.utils.reflect.MetaUtils;
 
 /**
  * Configuration with default parameters for c3p0 connection pooling
  * 
  * @author levan
  * 
  */
 public class PoolConfig {
 
     public static final String MAX_POOL_SIZE = "maxPoolSize";
     public static final String INITIAL_POOL_SIZE = "initialPoolSize";
     public static final String MIN_POOL_SIZE = "minPoolSize";
     public static final String MAX_IDLE_TIMEOUT = "maxIdleTime";
     public static final String MAX_STATEMENTS = "maxStatements";
     public static final String AQUIRE_INCREMENT = "acquireIncrement";
     public static final String MAX_IDLE_TIME_EXCESS_CONN = "maxIdleTimeExcessConnections";
     public static final String STAT_CACHE_NUM_DEFF_THREADS = "statementCacheNumDeferredCloseThreads";
 
     public static final String MAX_POOL_SIZE_DEF_VALUE = "15";
     public static final String INITIAL_POOL_SIZE_DEF_VALUE = "5";
     public static final String MIN_POOL_SIZE_DEF_VALUE = "5";
     public static final String MAX_IDLE_TIMEOUT_DEF_VALUE = "0";
     public static final String MAX_STATEMENTS_DEF_VALUE = "50";
     public static final String AQUIRE_INCREMENT_DEF_VALUE = "50";
     public static final String MAX_IDLE_TIME_EXCESS_CONN_DEF_VALUE = "0";
     public static final String STAT_CACHE_NUM_DEFF_THREADS_DEF_VALUE = "1";
 
     private static final String DEFAULT_POOL_PATH = "META-INF/pool.properties";
 
     public static String poolPath;
 
     public static Map<Object, Object> poolProperties;
 
     /**
      * Enumeration to choose which type connection pool should be in use
      * 
      * @author levan
      * 
      */
     public static enum PoolProviderType {
 
 	C3P0, TOMCAT;
     }
 
     public static PoolProviderType poolProviderType = PoolProviderType.C3P0;
 
     /**
      * Sets default connection pooling properties
      * 
      * @return
      */
     public static Map<Object, Object> getDefaultPooling() {
 	Map<Object, Object> c3p0Properties = new HashMap<Object, Object>();
 	c3p0Properties.put(PoolConfig.MAX_POOL_SIZE,
 		PoolConfig.MAX_POOL_SIZE_DEF_VALUE);
 	c3p0Properties.put(PoolConfig.INITIAL_POOL_SIZE,
 		PoolConfig.INITIAL_POOL_SIZE_DEF_VALUE);
 	c3p0Properties.put(PoolConfig.MIN_POOL_SIZE,
 		PoolConfig.MIN_POOL_SIZE_DEF_VALUE);
 	c3p0Properties.put(PoolConfig.MAX_IDLE_TIMEOUT,
 		PoolConfig.MAX_IDLE_TIMEOUT_DEF_VALUE);
 	c3p0Properties.put(PoolConfig.MAX_STATEMENTS,
 		PoolConfig.MAX_STATEMENTS_DEF_VALUE);
 	c3p0Properties.put(PoolConfig.AQUIRE_INCREMENT,
 		PoolConfig.AQUIRE_INCREMENT_DEF_VALUE);
 	c3p0Properties.put(STAT_CACHE_NUM_DEFF_THREADS,
 		STAT_CACHE_NUM_DEFF_THREADS_DEF_VALUE);
 
 	return c3p0Properties;
     }
 
     private static boolean checkModifiers(Field field) {
 
 	return Modifier.isStatic(field.getModifiers())
 		&& Modifier.isFinal(field.getModifiers())
 		&& field.getType().equals(String.class);
     }
 
     private static Set<Object> unsopportedKeys() throws IOException {
 
 	Set<Object> keys = new HashSet<Object>();
 	Field[] fields = DataSourceInitializer.class.getDeclaredFields();
 	Object key;
 	String apprEnd = "_PROPERTY";
 	String name;
 	for (Field field : fields) {
 	    name = field.getName();
 	    if (checkModifiers(field) && name.endsWith(apprEnd)) {
 		key = MetaUtils.getFieldValue(field);
 		keys.add(key);
 	    }
 	}
 
 	return keys;
     }
 
     /**
      * Add initialized properties to defaults
      * 
      * @param defaults
      * @param initial
      */
     private static void fillDefaults(Map<Object, Object> defaults,
 	    Map<Object, Object> initial) {
 
 	defaults.putAll(initial);
     }
 
     /**
      * Generates pooling configuration properties
      * 
      * @param initial
      * @return {@link Map}<Object, Object>
      * @throws IOException
      */
     private static Map<Object, Object> configProperties(
 	    Map<Object, Object> initial) throws IOException {
 
 	Map<Object, Object> propertiesMap = getDefaultPooling();
 	fillDefaults(propertiesMap, initial);
 	Set<Object> keys = unsopportedKeys();
 	for (Object key : keys) {
 	    propertiesMap.remove(key);
 	}
 	return propertiesMap;
     }
 
     public static int asInt(Map<Object, Object> properties, Object key) {
 
 	Object property = properties.get(key);
 	Integer propertyInt;
 	if (property == null) {
 	    propertyInt = null;
 	} else if (property instanceof Integer) {
 	    propertyInt = (Integer) property;
 	} else if (property instanceof String) {
 	    propertyInt = Integer.valueOf((String) property);
 	} else {
 	    propertyInt = null;
 	}
 
 	return propertyInt;
     }
 
     /**
      * Loads {@link Properties} from specific path
      * 
      * @param path
      * @return {@link Properties}
      * @throws IOException
      */
     public static Map<Object, Object> load() throws IOException {
 
 	InputStream stream;
 	if (ObjectUtils.notAvailable(poolPath)) {
	    poolPath = DEFAULT_POOL_PATH;
 	    ClassLoader loader = Thread.currentThread().getContextClassLoader();
	    stream = loader.getResourceAsStream(poolPath);
 	} else {
 	    File file = new File(poolPath);
 	    stream = new FileInputStream(file);
 	}
 	try {
 	    Map<Object, Object> properties;
 	    Properties propertiesToLoad;
 	    if (ObjectUtils.notNull(stream)) {
 		propertiesToLoad = new Properties();
 		propertiesToLoad.load(stream);
 		properties = new HashMap<Object, Object>();
 		properties.putAll(propertiesToLoad);
 	    } else {
 		properties = null;
 	    }
 
 	    return properties;
 	} finally {
 	    if (ObjectUtils.notNull(stream)) {
 		stream.close();
 	    }
 	}
     }
 
     /**
      * Merges passed properties, startup time passed properties and properties
      * loaded from file
      * 
      * @param properties
      * @return {@link Map}<Object, Object> merged properties map
      * @throws IOException
      */
     public static Map<Object, Object> merge(Map<Object, Object> properties)
 	    throws IOException {
 
 	Map<Object, Object> configMap = configProperties(properties);
 	Map<Object, Object> loaded = load();
 	if (ObjectUtils.notNull(loaded)) {
 	    fillDefaults(configMap, loaded);
 	}
 
 	if (ObjectUtils.notNull(poolProperties)) {
 	    fillDefaults(configMap, poolProperties);
 	}
 
 	return configMap;
     }
 }
