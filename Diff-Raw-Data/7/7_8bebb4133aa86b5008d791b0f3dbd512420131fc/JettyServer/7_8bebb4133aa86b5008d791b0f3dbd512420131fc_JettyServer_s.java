 package no.steria.swhrs;
 
 import net.sourceforge.jtds.jdbc.Driver;
 import net.sourceforge.jtds.jdbcx.JtdsDataSource;
 import org.eclipse.jetty.plus.jndi.EnvEntry;
 import org.eclipse.jetty.server.Server;
 import org.eclipse.jetty.webapp.WebAppContext;
 import org.hibernate.cfg.Environment;
 import org.hsqldb.jdbc.JDBCDataSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.naming.NamingException;
 import java.io.InputStream;
 import java.util.Properties;
 
 public class JettyServer {
 
     private static final Logger logger = LoggerFactory.getLogger(JettyServer.class);
     public static final String DB_JNDI = "jdbc/registerHoursDS";
     private Server server;
     private Integer port;
 
     public static void main(String[] args) throws Exception {
         JettyServer jettyServer = new JettyServer();
         jettyServer.startServer();
    }

    public void stopServer() throws Exception {
        server.destroy();
     }
 
     public void startServer() throws Exception {
         InputStream resourceAsStream = JettyServer.class.getResourceAsStream("/config.properties");
         Properties properties = System.getProperties();
         properties.load(resourceAsStream);
 
         if ("true".equals(System.getProperty("swhrs.useSqlServer"))) {
             registerSqlServerDatasource();
         } else {
             registerInmemoryDatasource();
         }
 
         if (port == null) {
             port = Integer.parseInt(System.getProperty("swhrs.serverPort"));
         }
 
         server = new Server(port);
         server.setHandler(new WebAppContext("src/main/webapp", "/"));
         server.start();
         logger.info("Server started! - port " + port);
        server.join();
     }
 
     private void registerInmemoryDatasource() throws NamingException {
         JDBCDataSource jdbcDataSource = new JDBCDataSource();
         jdbcDataSource.setDatabase("jdbc:hsqldb:mem:testDb");
         jdbcDataSource.setUser("sa");
         jdbcDataSource.setPassword("");
         System.setProperty(Environment.HBM2DDL_AUTO, "create");
         new EnvEntry(DB_JNDI, jdbcDataSource);
     }
 
     private void registerSqlServerDatasource() throws NamingException {
         String serverAddress = System.getProperty("swhrs.dbServer");
         String databaseName = System.getProperty("swhrs.dbName");
         String user = System.getProperty("swhrs.dbUsername");
         String password = System.getProperty("swhrs.dbPassword");
         int dbPort = Integer.parseInt(System.getProperty("swhrs.dbPort"));
 
         logger.info("Connecting to '" + serverAddress + "' db '" + databaseName + "' port '" + dbPort + "' user '" + user + "'");
 
         JtdsDataSource datasource = new JtdsDataSource();
         datasource.setServerType(Driver.SQLSERVER);
         datasource.setServerName(serverAddress);
         datasource.setPortNumber(dbPort);
         datasource.setDatabaseName(databaseName);
         datasource.setUser(user);
         datasource.setPassword(password);
 
         new EnvEntry(DB_JNDI, datasource);
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 }
