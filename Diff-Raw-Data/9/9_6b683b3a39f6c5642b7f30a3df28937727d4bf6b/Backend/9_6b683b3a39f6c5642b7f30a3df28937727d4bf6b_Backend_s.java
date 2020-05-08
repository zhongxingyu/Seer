 package no.ebakke.studycaster.backend;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Properties;
 import javax.servlet.ServletException;
 import org.hibernate.HibernateException;
 import org.hibernate.SessionFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.engine.SessionFactoryImplementor;
 
 public final class Backend {
   public  static final Backend INSTANCE = new Backend();
   private static final String HCU_KEY = "hibernate.connection.url";
   private static final String HDA_KEY = "hibernate.hbm2ddl.auto";
   private SessionFactory   sessionFactory;
   private BackendException sessionFactoryError;
   private File             storageDir;
   private BackendException storageDirError;
 
   public Backend() {
     this(new BackendConfiguration(), false);
   }
 
   public Backend(BackendConfiguration config, boolean createDatabase) {
     try {
       sessionFactory = buildSessionFactory(config.getDatabaseURL(),
           createDatabase);
     } catch (BackendException e) {
       sessionFactoryError = e;
     }
     try {
       storageDir = openStorageDir(config.getStorageDirPath());
     } catch (BackendException e) {
       storageDirError = e;
     }
   }
 
   private static File openStorageDir(String path) throws BackendException {
     File ret = new File(path);
     if (!ret.isDirectory())
       throw new BackendException("Not a directory");
     try {
       File.createTempFile("~permtest", "tmp", ret).delete();
     } catch (IOException e) {
       throw new BackendException("Access denied (" + e.getMessage() + ")");
     }
     return ret;
   }
 
   public String getStatusMessage() {
     String ret;
     if (sessionFactoryError != null) {
       Throwable cause = sessionFactoryError;
       do {
         ret = cause.getMessage();
         if (!ret.contains("see the next exception for details"))
           break;
         cause = cause.getCause();
       } while (cause != null);
     } else {
       // See http://stackoverflow.com/questions/1571928/retrieve-auto-detected-hibernate-dialect .
       if (sessionFactory instanceof SessionFactoryImplementor) {
         ret = "Successful database connection, dialect=" +
           ((SessionFactoryImplementor) sessionFactory).getDialect().toString();
       } else {
         ret = "Successful database connection, " + sessionFactory.toString();
       }
     }
     ret += " / ";
     if (storageDirError != null) {
       ret += storageDirError.getMessage();
     } else {
      ret += "Successfully opened storage directory.";
     }
     return ret;
   }
 
   public SessionFactory getSessionFactory() throws HibernateException {
     if (sessionFactoryError != null) {
       throw new HibernateException("Backend was not properly initialized",
           sessionFactoryError);
     }
     return sessionFactory;
   }
 
   public File getStorageDirectory() throws ServletException {
     if (storageDirError != null) {
       throw new ServletException("Backend was not properly initialized",
           storageDirError);
     }
     return storageDir;
   }
 
   /** If connectionURL is null, take from environment. */
   private static SessionFactory buildSessionFactory(
       String connectionURL, boolean create) throws BackendException
   {
     try {
       Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
       Class.forName("org.apache.derby.jdbc.ClientDriver");
       Class.forName("com.mysql.jdbc.Driver");
 
       Configuration conf = createHibernateConfiguration(connectionURL, create);
       /* Try with regular JDBC first, as it gives better error messages for
       invalid URL parameters. */
       DriverManager.getConnection(conf.getProperty(HCU_KEY)).close();
       SessionFactory ret = conf.buildSessionFactory();
       if (create) {
         /* Hibernate may let schema creation fail silently, so reconnect in
         validate mode to be sure the operation succeeded. Aforementioned
         behavior observed when attempting to create a schema with which the
         specified user has read-only access. */
         ret.close();
         try {
           return buildSessionFactory(connectionURL, false);
         } catch (BackendException e) {
           throw new BackendException("Validation failed at reconnect", e);
         }
       } else {
         return ret;
       }
     } catch (ClassNotFoundException e) {
       throw new BackendException(e);
     } catch (SQLException e) {
       throw new BackendException(e);
     } catch (HibernateException e) {
       throw new BackendException(e);
     }
   }
 
   /** If connectionURL is null, take from environment. */
   private static Configuration createHibernateConfiguration(
     String connectionURL, boolean create)
     throws BackendException, HibernateException
   {
     Configuration ret = new Configuration();
     ret.configure("hibernate.cfg.xml");
     Properties p = new Properties();
     p.setProperty(HCU_KEY, connectionURL);
     if (ret.getProperty(HDA_KEY) != null) {
       throw new BackendException(
           "Hibernate configuration may not set " + HDA_KEY); 
     }
     p.setProperty(HDA_KEY, create ? "create" : "validate");
     ret.addProperties(p);
     return ret;
   }
 }
