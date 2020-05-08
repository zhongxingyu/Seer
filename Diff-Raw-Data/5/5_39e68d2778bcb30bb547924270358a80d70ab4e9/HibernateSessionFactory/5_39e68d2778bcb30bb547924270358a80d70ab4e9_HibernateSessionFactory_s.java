 package org.pentaho.pac.server.common;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.hibernate.HibernateException;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.AnnotationConfiguration;
 import org.hibernate.cfg.Configuration;
 
 
 /**
  * Configures and provides access to Hibernate sessions, tied to the
  * current thread of execution.  Follows the Thread Local Session
  * pattern, see {@link http://hibernate.org/42.html }.
  */
 public class HibernateSessionFactory {
 
    private static final String HIBERNATE_DIR = "hibernate/";
 	public static final String DEFAULT_CONFIG_NAME = "$$DEFAULT_CONFIG";
     public static String DEFAULT_CONFIG_FILE_LOCATION = "hsql.hibernate.cfg.xml"; //$NON-NLS-1$
 	/** 
      * Location of hibernate.cfg.xml file.
      * Location should be on the classpath as Hibernate uses  
      * #resourceAsStream style lookup for its configuration file. 
      * The default classpath location of the hibernate config file is 
      * in the default package. Use #setConfigFile() to update 
      * the location of the configuration file for the current session.   
      */
    // private static final ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();
    // private  static org.hibernate.cfg.AnnotationConfiguration configuration = new AnnotationConfiguration();
     //private static org.hibernate.SessionFactory sessionFactory;
      
     private static Map<String,HibConfig> configs = new HashMap<String,HibConfig>();
     private static String defaultConfigFile = null;
 
     private HibernateSessionFactory() {
     }
     
     public static void addDefaultConfiguration() {
       defaultConfigFile = AppConfigProperties.getInstance().getHibernateConfigPath();
      if (!defaultConfigFile.startsWith(HIBERNATE_DIR))
    	  defaultConfigFile=HIBERNATE_DIR+defaultConfigFile;
      
       if(defaultConfigFile != null && defaultConfigFile.length() > 0) {
         addConfiguration(DEFAULT_CONFIG_NAME,defaultConfigFile);
       } else {
         defaultConfigFile = DEFAULT_CONFIG_FILE_LOCATION;
         addConfiguration(DEFAULT_CONFIG_NAME,defaultConfigFile);
       }
       
     }
     public static void addConfiguration(String name,String configFile)
     {
     	if (!configs.containsKey(name))
     	addOrUpdateConfiguration(name,configFile);
     }
     
     
     
     public static void addOrUpdateConfiguration(String name,String configFile)
     {
     	Configuration configuration = new AnnotationConfiguration();
     	
     	try {
 			configuration.configure(configFile);
 			SessionFactory sessionFactory = configuration.buildSessionFactory();
 			configs.put(name,new HibConfig(sessionFactory,configuration,configFile));
 		} catch (Exception e) {
 			System.err
 					.println("%%%% Error Creating SessionFactory %%%%");
 			e.printStackTrace();
 		}
     }
 	
 	/**
      * Returns the ThreadLocal Session instance.  Lazy initialize
      * the <code>SessionFactory</code> if needed.
      *
      *  @return Session
      *  @throws HibernateException
      */
     public static Session getSession() throws HibernateException {
        return getSession(DEFAULT_CONFIG_NAME);
     }
     
     /**
      * Returns the ThreadLocal Session instance.  Lazy initialize
      * the <code>SessionFactory</code> if needed.
      *
      *  @return Session
      *  @throws HibernateException
      */
     public static Session getSession(String configName) throws HibernateException {
     	HibConfig cfg = configs.get(configName);
     	if (cfg==null)
     		throw new HibernateException("Unknown configuration: " + configName);
     	
         Session session = cfg.threadLocal.get();
 
 		if (session == null || !session.isOpen()) {
 			if (cfg.sessionFactory == null) {
 				rebuildSessionFactory(cfg);
 			}
 			session = (cfg.sessionFactory != null) ? cfg.sessionFactory.openSession()
 					: null;
 			cfg.threadLocal.set(session);
 		}
 
         return session;
     }
     
     
 
 	/**
      *  Rebuild hibernate session factory
      *
      */
 	public static void rebuildSessionFactory(HibConfig cfg) {
 		try {
 			cfg.configuration.configure(cfg.configFile);
 			cfg.sessionFactory = cfg.configuration.buildSessionFactory();
 		} catch (Exception e) {
 			System.err
 					.println("%%%% Error Creating SessionFactory %%%%"); //$NON-NLS-1$
 			e.printStackTrace();
 		}
 	}
 
 	/**
      *  Close the single hibernate session instance.
      *
      *  @throws HibernateException
      */
     public static void closeSession() throws HibernateException {
        closeSession(DEFAULT_CONFIG_NAME);
     }
     
     public static void closeSession(String configName)
     {
     	HibConfig cfg = configs.get(configName);
     	if (cfg==null)
     		throw new HibernateException("Unknown configuration: " + configName);
     	
     	 Session session = (Session) cfg.threadLocal.get();
          cfg.threadLocal.set(null);
 
          if (session != null) {
              session.close();
          }
     }
 
 	/**
      *  return session factory
      *
      */
 	public static org.hibernate.SessionFactory getSessionFactory() {
 		return getSessionFactory(defaultConfigFile);
 	}
 	
 	public static org.hibernate.SessionFactory getSessionFactory(String configName) {
 		HibConfig cfg = configs.get(configName);
     	if (cfg==null)
     		throw new HibernateException("Unknown configuration: " + configName);
     	
     	return cfg.sessionFactory;
 	}
 
 	/**
      *  return hibernate configuration
      *
      */
 	public static Configuration getConfiguration() {
 		return getConfiguration(defaultConfigFile);
 	}
 
 
 	/**
      *  return hibernate configuration
      *
      */
 	public static Configuration getConfiguration(String configName) {
 		HibConfig cfg = configs.get(configName);
     	if (cfg==null)
     		throw new HibernateException("Unknown configuration: " + configName);
     	
     	return cfg.configuration;
 	}
 	
 	private static class HibConfig
 	{
 		HibConfig(SessionFactory sessionFactory,Configuration configuration,String configFile)
 		{
 			this.sessionFactory = sessionFactory;
 			this.configuration = configuration;
 			this.configFile = configFile;
 		}
 		SessionFactory sessionFactory;
 		Configuration configuration;
 		ThreadLocal<Session> threadLocal = new ThreadLocal<Session>();
 		String configFile;
 		
 	}
 
 }
