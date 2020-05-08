 package xtremweb.core.db;
 
 /**
  * DBInterfaceFactory.java
  *
  *
  * Created: Fri Mar  3 13:50:48 2006
  *
  * @author <a href="mailto:">Gilles Fedak</a>
  * @version 1.0
  */
 
 import javax.jdo.PersistenceManager;
 import javax.jdo.PersistenceManagerFactory;
 import java.util.Properties;
 import javax.jdo.JDOHelper;
 
 import xtremweb.core.conf.*;
 import xtremweb.core.log.Logger;
 import xtremweb.core.log.LoggerFactory;
 
 public class DBInterfaceFactory {
 
     private static DBInterface dbi;
     private static DBInterfaceFactory dbif;
     private static PersistenceManagerFactory pmf;
 
     protected static Logger log = LoggerFactory.getLogger(DBInterfaceFactory.class);
 
     static {
 	dbif = new DBInterfaceFactory();
     }
 
     public DBInterfaceFactory( ) {
 
 	Properties mainprop;
 	try {
 	    mainprop = ConfigurationProperties.getProperties();
 	} catch (ConfigurationException ce) {
 	    log.debug("No Database configuratioin found for DBInterfaceFactory : " + ce);
 	    mainprop = new Properties();
 	}
 
        	Properties properties = new Properties();
 	properties.setProperty("javax.jdo.PersistenceManagerFactoryClass",
 			       "org.jpox.PersistenceManagerFactoryImpl");
 	properties.setProperty("javax.jdo.option.ConnectionDriverName", mainprop.getProperty("xtremweb.core.db.driver","org.hsqldb.jdbcDriver"));
 	properties.setProperty("javax.jdo.option.ConnectionURL", mainprop.getProperty("xtremweb.core.db.url","jdbc:hsqldb:mem:test"));
 	properties.setProperty("javax.jdo.option.ConnectionUserName",mainprop.getProperty("xtremweb.core.db.user","sa"));
 	properties.setProperty("javax.jdo.option.ConnectionPassword",mainprop.getProperty("xtremweb.core.db.password",""));
 
 	properties.setProperty("org.jpox.autoCreateSchema","true");
 	properties.setProperty("org.jpox.validateTables","false");
 	properties.setProperty("org.jpox.validateConstraints","false");
 	properties.setProperty("javax.jdo.option.DetachAllOnCommit", "true");
 	if (mainprop.getProperty("xtremweb.core.db.connectionPooling")!=null) {
 	    properties.setProperty("org.jpox.connectionPoolingType",mainprop.getProperty("xtremweb.core.db.connectionPooling"));
	    properties.setProperty("org.jpox.connectionPoolingConfigurationFile","dbcp.properties");
 	}
 
 	pmf = JDOHelper.getPersistenceManagerFactory(properties);
 	dbi = new DBInterface();
 	//	pmf.setMultithreaded("true");
     } // DBInterfaceFactory constructor
     
     public static PersistenceManagerFactory  getPersistenceManagerFactory() {
 	return pmf;
     }
     
     public static DBInterfaceFactory getDBInterfaceFactory() {
 	return dbif;
     }
     
     public static DBInterface getDBInterface() {
 	return dbi;
     }
     
 
 
 } // DBInterfaceFactory
