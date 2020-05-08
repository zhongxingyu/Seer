 package com.github.cwilper.fcrepo.cloudsync.service;
 
 import org.apache.commons.io.IOUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.SQLException;
 import java.util.Properties;
 
 public class CloudSyncContextListener implements ServletContextListener {
 
     private Logger logger = null;
 
     @Override
     public void contextInitialized(ServletContextEvent sce) {
         try {
             ServletContext context = sce.getServletContext();
 
             // Read cloudsync.properties, adding all to System properties
             Properties props = new Properties();
             props.load(context.getResourceAsStream("WEB-INF/cloudsync.properties"));
             for (String name: props.stringPropertyNames()) {
                 System.setProperty(name, props.getProperty(name));
             }
 
             // Determine home directory while ensuring the normalized form
             // (without a trailing slash) is used from now on as the value
             // of the cloudsync.home property.
             String home = System.getProperty("cloudsync.home");
             if (home == null || home.trim().length() == 0) {
                 home = System.getenv("CLOUDSYNC_HOME");
                 if (home == null || home.trim().length() == 0) {
                     File userHome = new File(System.getProperty("user.home"));
                     home = new File(userHome, ".cloudsync").getPath();
                 }
             }
             File homeDir = new File(home);
             System.setProperty("cloudsync.home", homeDir.getPath());
 
             // Ensure home directory exists
             if (!homeDir.exists() && !homeDir.mkdirs()) {
                 throw new IOException("Unable to create CloudSync home "
                         + "directory: " + homeDir.getPath());
             }
 
             // Ensure logback.xml exists in home directory
             File logbackFile = new File(homeDir, "logback.xml");
             if (!logbackFile.exists()) {
                 InputStream in = context.getResourceAsStream("WEB-INF/default-logback.xml");
                 OutputStream out = new FileOutputStream(logbackFile);
                 try {
                     IOUtils.copy(in, out);
                 } finally {
                     IOUtils.closeQuietly(in);
                     IOUtils.closeQuietly(out);
                 }
             }
 
             // Signal to Logback that it should initialize from that file
             System.setProperty("logback.configurationFile", logbackFile.getPath());
 
             // Replace java.util.logging's default handlers with one that
             // redirects everything to SLF4J
             java.util.logging.Logger rootLogger =
                     java.util.logging.LogManager.getLogManager().getLogger("");
             java.util.logging.Handler[] handlers = rootLogger.getHandlers();
            for (int i = 0; i < handlers.length; i++) {
                rootLogger.removeHandler(handlers[i]);
             }
             SLF4JBridgeHandler.install();
 
             // Make sure the systemlogs directory exists
             File systemLogDir = new File(homeDir, "systemlogs");
             if (!systemLogDir.isDirectory() && !systemLogDir.mkdir()) {
                 throw new IOException("Unable to create CloudSync system log "
                         + "directory: " + systemLogDir.getPath());
             }
 
             // Log a startup message
             logger = LoggerFactory.getLogger(this.getClass());
             logger.info("Starting CloudSync. Home directory is " + homeDir.getPath());
 
             // Test a connection to the database, creating it if necessary
             String jdbcDriver = "org.apache.derby.jdbc.EmbeddedDriver";
             File dbDir = new File(homeDir, "database");
             String jdbcURL = "jdbc:derby:" + dbDir.getPath() + ";create=true";
             Class.forName(jdbcDriver).newInstance();
             Connection conn = DriverManager.getConnection(jdbcURL);
             try {
                 conn.close();
             } catch (SQLException e) {
                 logger.error("Error closing database connection", e);
             }
 
             // Set jdbc.drive and jdbc.url System Properties for Spring use
             System.setProperty("jdbc.driver", jdbcDriver);
             System.setProperty("jdbc.url", jdbcURL);
 
         } catch (Exception e) {
             if (logger == null) {
                 e.printStackTrace();
                 System.out.println("Failed to initialize CloudSync; see above.");
             } else {
                 logger.error("Failed to initialize CloudSync", e);
             }
             throw new RuntimeException("Failed to initialize CloudSync", e);
         }
     }
 
     @Override
     public void contextDestroyed(ServletContextEvent sce) {
         try {
             DriverManager.getConnection("jdbc:derby:;shutdown=true");
         } catch (SQLException e) {
             // SQL exception XJ015 is expected
         }
         logger.info("Stopped CloudSync.");
     }
 }
