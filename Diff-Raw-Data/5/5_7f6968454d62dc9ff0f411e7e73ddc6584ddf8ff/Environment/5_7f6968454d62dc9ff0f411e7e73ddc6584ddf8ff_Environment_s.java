 package net.argius.stew;
 
 import static net.argius.stew.Bootstrap.getDirectory;
 
 import java.io.*;
 import java.sql.*;
 
 import net.argius.stew.ui.*;
 
 /**
  * Environment.
  */
 public final class Environment {
 
     static final String CONNECTOR_PROPERTIES_NAME = "connector.properties";
     static final String ALIAS_PROPERTIES_NAME = "alias.properties";
 
     private static final Logger log = Logger.getLogger(Environment.class);
     static ResourceManager res = ResourceManager.getInstance(Environment.class);
 
     private OutputProcessor outputProcessor;
     private ConnectorMap connectorMap;
     private Connector connector;
     private Connection conn;
 
     private int timeoutSeconds;
     private File systemDirectory;
     private File currentDirectory;
     private long connectorTimestamp;
     private AliasMap aliasMap;
 
     /**
      * A constructor.
      */
     public Environment() {
         initializeQueryTimeout();
         // init connections
         this.connectorMap = new ConnectorMap();
         loadConnectorMap();
         // init directories
         this.systemDirectory = getDirectory();
         this.currentDirectory = getInitialCurrentDirectory();
         // init alias
         final File aliasPropFile = new File(this.systemDirectory, ALIAS_PROPERTIES_NAME);
         this.aliasMap = new AliasMap(aliasPropFile);
         if (aliasPropFile.exists()) {
             try {
                 aliasMap.load();
             } catch (IOException ex) {
                 log.warn(ex);
             }
         }
     }
 
     /**
      * A constructor (for copy).
      * @param src
      */
     public Environment(Environment src) {
         // never copy coconnector,conn,op into this
         this.connectorMap = new ConnectorMap(src.connectorMap);
         this.timeoutSeconds = src.timeoutSeconds;
         this.systemDirectory = src.systemDirectory;
         this.currentDirectory = src.currentDirectory;
     }
 
     /**
      * Releases resouces it keeps.
      */
     public void release() {
         try {
             releaseConnection();
             log.debug("released connection");
         } catch (SQLException ex) {
             log.error(ex, "release error");
         } finally {
             outputProcessor = null;
             connectorMap = null;
             connector = null;
             conn = null;
             systemDirectory = null;
             currentDirectory = null;
         }
         log.debug("released internal state of Environment");
     }
 
     /**
      * Establishes a connection.
      * @param connector
      * @throws SQLException
      */
     void establishConnection(Connector connector) throws SQLException {
         Connection conn = connector.getConnection();
         try {
             if (connector.isReadOnly()) {
                 conn.setReadOnly(true);
             }
         } catch (RuntimeException ex) {
             log.warn(ex);
         }
         boolean isAutoCommitAvailable;
         try {
             conn.setAutoCommit(false);
             isAutoCommitAvailable = conn.getAutoCommit();
         } catch (RuntimeException ex) {
             log.warn(ex);
             isAutoCommitAvailable = false;
         }
         if (isAutoCommitAvailable) {
             outputMessage("w.auto-commit-not-available");
         }
         setCurrentConnection(conn);
         setCurrentConnector(connector);
         outputMessage("i.connected");
         log.debug("connected %s (conn=%08x, env=%08x)", connector.getId(), conn.hashCode(), hashCode());
         if (Bootstrap.getPropertyAsBoolean("net.argius.stew.print-connected-time")) {
             outputMessage("i.now", System.currentTimeMillis());
         }
     }
 
     /**
      * Releases the connection.
      * @throws SQLException
      */
     void releaseConnection() throws SQLException {
         if (conn == null) {
             log.debug("not connected");
             return;
         }
         try {
             if (connector != null && connector.usesAutoRollback()) {
                 try {
                     conn.rollback();
                     outputMessage("i.rollbacked");
                     log.debug("rollbacked %s (%s)", connector.getId(), conn);
                 } catch (SQLException ex) {
                     log.warn(ex);
                 }
             }
             try {
                 conn.close();
                 log.debug("disconnected %s (conn=%08x, env=%08x)", connector.getId(), conn.hashCode(), hashCode());
                 if (Bootstrap.getPropertyAsBoolean("net.argius.stew.print-disconnected-time")) {
                     outputMessage("i.now", System.currentTimeMillis());
                 }
             } catch (SQLException ex) {
                 log.warn(ex);
                 throw ex;
             }
         } finally {
             conn = null;
             connector = null;
         }
     }
 
     private void outputMessage(String id, Object... args) throws CommandException {
         if (outputProcessor != null) {
             outputProcessor.output(res.get(id, args));
         }
     }
 
     private static File getInitialCurrentDirectory() {
         final String propkey = "net.argius.stew.directory";
         if (Bootstrap.hasProperty(propkey)) {
             File directory = new File(Bootstrap.getProperty(propkey, ""));
             if (directory.isDirectory()) {
                 return directory;
             }
         }
         return new File(".");
     }
 
     private void initializeQueryTimeout() {
         this.timeoutSeconds = Bootstrap.getPropertyAsInt("net.argius.stew.query.timeout", -1);
         if (log.isDebugEnabled()) {
             log.debug("timeout: " + this.timeoutSeconds);
         }
     }
 
     /**
      * Loads and refreshes connector map.
      */
     public void loadConnectorMap() {
         File connectorFile = new File(getDirectory(), CONNECTOR_PROPERTIES_NAME);
         ConnectorMap m;
         try {
             InputStream is = new FileInputStream(connectorFile);
             try {
                 m = ConnectorConfiguration.load(is);
             } finally {
                 is.close();
             }
         } catch (IOException ex) {
             m = new ConnectorMap();
         }
         synchronized (connectorMap) {
             if (connectorMap.size() > 0) {
                 connectorMap.clear();
             }
             connectorMap.putAll(m);
             connectorTimestamp = connectorFile.lastModified();
         }
     }
 
     /**
      * Updates connector map.
      * When file was updated, it calls loadConnectorMap().
      * @return whether updated or not
      */
     public boolean updateConnectorMap() {
         File connectorFile = new File(getDirectory(), CONNECTOR_PROPERTIES_NAME);
         if (connectorFile.lastModified() > connectorTimestamp) {
             loadConnectorMap();
             return true;
         }
         return false;
     }
 
     public OutputProcessor getOutputProcessor() {
         return outputProcessor;
     }
 
     public void setOutputProcessor(OutputProcessor outputProcessor) {
         this.outputProcessor = outputProcessor;
     }
 
     public ConnectorMap getConnectorMap() {
         return connectorMap;
     }
 
     public Connector getCurrentConnector() {
         return connector;
     }
 
     void setCurrentConnector(Connector connector) {
         this.connector = connector;
     }
 
     public Connection getCurrentConnection() {
         return conn;
     }
 
     void setCurrentConnection(Connection conn) {
         this.conn = conn;
     }
 
     public int getTimeoutSeconds() {
         return timeoutSeconds;
     }
 
     public File getCurrentDirectory() {
         return currentDirectory;
     }
 
     public void setCurrentDirectory(File currentDirectory) {
         this.currentDirectory = currentDirectory;
     }
 
     public File getSystemDirectory() {
         return systemDirectory;
     }
 
     public AliasMap getAliasMap() {
         return aliasMap;
     }
 
 }
