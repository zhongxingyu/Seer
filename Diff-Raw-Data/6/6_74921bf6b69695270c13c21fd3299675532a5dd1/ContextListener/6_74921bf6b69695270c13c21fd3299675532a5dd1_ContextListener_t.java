 package de.consistec.syncframework.server;
 
 import de.consistec.syncframework.common.Config;
 import de.consistec.syncframework.common.exception.ConfigException;
 import de.consistec.syncframework.common.util.PropertiesUtil;
 import de.consistec.syncframework.impl.proxy.http_servlet.HttpServletProcessor;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 import javax.naming.InitialContext;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletContextEvent;
 import javax.servlet.ServletContextListener;
 import javax.servlet.ServletException;
 import javax.sql.DataSource;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 //import javax.servlet.annotation.WebListener;
 
 /**
  * Web application lifecycle listener.
  * <ul>
  * <li><b>Company:</b> consistec Engineering and Consulting GmbH</li>
  * </ul>
  * <p/>
  *
  * @author Piotr Wieczorek
  * @since 0.0.1-SNAPSHOT
  */
 //@WebListener("Listener initializes syncframework")
 public class ContextListener implements ServletContextListener {
 
     public static final String SERVER_OPTION_POOLING = "pooling";
     public static final String SERVER_OPTION_DEBUG = "debug";
     public static final String HTTP_PROCESSOR_CTX_ATTR = "HTTP_PROCESSOR";
     private static final Logger LOGGER = LoggerFactory.getLogger(ContextListener.class.getCanonicalName());
     private static final String PARAMA_SYNC_CONFIG_FILE_NAME = "sync_config_file_name";
     private static final String PARAMA_SERVER_CONFIG_FILE_NAME = "server_config_file_name";
     private static final String DEFAULT_FRAMEWORK_CONFIG_FILE = "syncframework.properties";
     private static final String DEFAULT_SERVER_CONFIG_FILE = "server.properties";
     private ServletContext ctx;
     private boolean isPooling;
     private boolean isDebugEnabled;
     private final String configFileName;
 
     public ContextListener(String configFileName) {
         this.configFileName = configFileName;
     }
 
     public ContextListener() {
         this.configFileName = "/WEB-INF/" + DEFAULT_FRAMEWORK_CONFIG_FILE;
     }
 
     @Override
     public void contextInitialized(ServletContextEvent sce) {
 
         LOGGER.info("ContextListener startet ...");
 
         ctx = sce.getServletContext();
 
 
         try {
             loadSyncFrameworkConfig();
             LOGGER.info("Sync framework configuration loaded");
         } catch (IOException ex) {
             throw new ConfigException("Error while loading configuration for synchronization framework", ex);
         }
 
         try {
             loadServerConfig();
         } catch (IOException ex) {
             throw new ConfigException("Error while loading server configuration", ex);
         }
 
         try {
             prepareHttpProcessor();
         } catch (Exception ex) {
             throw new RuntimeException("Couldn't construct Http processor", ex);
         }
 
     }
 
     private void prepareHttpProcessor() throws Exception {
 
         HttpServletProcessor processor;
 
         if (isPooling) {
 
             InitialContext initCxt = new InitialContext();
             if (initCxt == null) {
                 throw new Exception("Uh oh -- no context!");
             }
             DataSource ds = (DataSource) initCxt.lookup("java:/comp/env/jdbc/sync");
             if (ds == null) {
                 throw new ServletException("Data source not found!");
             } else {
                 LOGGER.debug("Datasource found");
             }
 
             processor = new HttpServletProcessor(ds, isDebugEnabled);
         } else {
             processor = new HttpServletProcessor(isDebugEnabled);
         }
 
         ctx.setAttribute(HTTP_PROCESSOR_CTX_ATTR, processor);
     }
 
     private String readParamFromContext(String defValue, String paramName) {
         return PropertiesUtil.defaultIfNull(defValue, ctx.getInitParameter(paramName));
     }
 
     private File readFile(String fileName) {
        if (fileName.contains("WEB-INF")) {
            return new File(ctx.getRealPath(fileName));
        }

        return new File(ctx.getRealPath("/WEB-INF/" + fileName));
     }
 
     private void loadSyncFrameworkConfig() throws IOException {
 
         FileInputStream fis = null;
 
         try {
             fis = new FileInputStream(readFile(readParamFromContext(configFileName,
                 PARAMA_SYNC_CONFIG_FILE_NAME)));
             Config config = Config.getInstance();
             config.init(fis);
         } finally {
             if (fis != null) {
                 try {
                     fis.close();
                 } catch (IOException ex) {
                     LOGGER.warn("Can't close sync framework config file");
                 }
             }
         }
     }
 
     private void loadServerConfig() throws IOException {
 
         FileInputStream fis = null;
         Properties props = new Properties();
 
         try {
             fis = new FileInputStream(readFile(readParamFromContext(DEFAULT_SERVER_CONFIG_FILE,
                 PARAMA_SERVER_CONFIG_FILE_NAME)));
             props.load(fis);
 
         } finally {
             if (fis != null) {
                 try {
                     fis.close();
                 } catch (IOException ex) {
                     LOGGER.warn("Can't close server config file");
                 }
             }
         }
 
         isPooling = PropertiesUtil.defaultIfNull(Boolean.FALSE, PropertiesUtil.readBoolean(props, SERVER_OPTION_POOLING,
             false)).booleanValue();
         LOGGER.debug("db pooling is {}", isPooling);
         ctx.setAttribute(SERVER_OPTION_POOLING, isPooling);
 
         isDebugEnabled = PropertiesUtil.defaultIfNull(Boolean.FALSE,
             PropertiesUtil.readBoolean(props, SERVER_OPTION_DEBUG,
                 false)).booleanValue();
         LOGGER.debug("debug mode {}", isDebugEnabled);
         ctx.setAttribute(SERVER_OPTION_DEBUG, isDebugEnabled);
     }
 
     @Override
     public void contextDestroyed(final ServletContextEvent servletContextEvent) {
         LOGGER.info("ContextListener finished ...");
     }
 }
