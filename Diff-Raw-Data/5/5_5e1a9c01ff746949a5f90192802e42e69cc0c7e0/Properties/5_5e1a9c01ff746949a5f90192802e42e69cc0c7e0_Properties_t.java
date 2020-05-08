 package com.apidump;
 
import static org.eclipse.persistence.config.PersistenceUnitProperties.DDL_GENERATION;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_DRIVER;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_PASSWORD;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_URL;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.JDBC_USER;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.TARGET_SERVER;
 import static org.eclipse.persistence.config.PersistenceUnitProperties.TRANSACTION_TYPE;
 
 import java.net.URI;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.spi.PersistenceUnitTransactionType;
 
 import org.eclipse.persistence.config.TargetServer;
 
 /**
  * Defines properties for EntityManager.
  * Uses the DATABASE_URL env variable to define where the db is.
  */
 public class Properties {
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public static Map getProperties() {
 		Map properties = new HashMap();
 		URI dbUrl = URI.create(System.getenv("DATABASE_URL"));
 		properties.put(TRANSACTION_TYPE,
 		        PersistenceUnitTransactionType.RESOURCE_LOCAL.name());
 		properties.put(JDBC_DRIVER, "org.postgresql.Driver");
 	    properties.put(JDBC_URL, "jdbc:postgresql://" + dbUrl.getHost() + dbUrl.getPath());
 	    properties.put(JDBC_USER, dbUrl.getUserInfo().split(":")[0]);
 	    properties.put(JDBC_PASSWORD, dbUrl.getUserInfo().split(":")[1]);
 	    
	    properties.put(DDL_GENERATION, "drop-and-create-tables");
 	    properties.put(TARGET_SERVER, TargetServer.None);
 	    return properties;
 	}
 }
