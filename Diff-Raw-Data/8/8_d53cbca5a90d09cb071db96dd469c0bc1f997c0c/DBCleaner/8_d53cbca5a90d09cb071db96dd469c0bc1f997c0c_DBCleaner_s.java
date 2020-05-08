 package com.nedap.dbcleaner;
 
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.DriverPropertyInfo;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeSet;
 
 /**
  * The DBCleaner JDBC driver
  *
  * @author Pieter Bos
  */
 public class DBCleaner implements Driver {
 
     private static final String JDBCPREFIX = "jdbc:dbcleaner";
     /**
      * The last actual, underlying driver that was requested via a URL.
      */
     private Driver recentRealDriver;
     /**
      * Instance cache of the underlying singleton connections
      */
     private final static Map<ConnectionKey, TransactionWrappedConnection> transactionWrappedCache = Collections.synchronizedMap(new HashMap<ConnectionKey, TransactionWrappedConnection>());
 
     static {
         Set<String> drivers = new TreeSet<String>();
 
         drivers.add("oracle.jdbc.driver.OracleDriver");
         drivers.add("oracle.jdbc.OracleDriver");
         drivers.add("com.sybase.jdbc2.jdbc.SybDriver");
         drivers.add("net.sourceforge.jtds.jdbc.Driver");
 
         // Sql Server 2000
         drivers.add("com.microsoft.jdbc.sqlserver.SQLServerDriver");
 
         // Sql Server 2005
         drivers.add("com.microsoft.sqlserver.jdbc.SQLServerDriver");
 
         drivers.add("weblogic.jdbc.sqlserver.SQLServerDriver");
         drivers.add("com.informix.jdbc.IfxDriver");
         drivers.add("org.apache.derby.jdbc.ClientDriver");
         drivers.add("org.apache.derby.jdbc.EmbeddedDriver");
         drivers.add("com.mysql.jdbc.Driver");
         drivers.add("org.postgresql.Driver");
         drivers.add("org.hsqldb.jdbcDriver");
         drivers.add("org.h2.Driver");
 
         try {
             DriverManager.registerDriver(new DBCleaner());
         } catch (SQLException s) {
             throw new RuntimeException("Registration of DBCleaner failed!", s);
         }
 
         //load drivers
         for (String driver : drivers) {
             try {
                 Class.forName(driver);
             } catch (Throwable c) {
                 //driver not available, fine!
             }
         }
     }
 
     /**
      * Default constructor.
      */
     public DBCleaner() {
     }
 
     @Override
     public int getMajorVersion() {
         if (recentRealDriver == null) {
             return 1;
         } else {
             return recentRealDriver.getMajorVersion();
         }
     }
 
     @Override
     public int getMinorVersion() {
         if (recentRealDriver == null) {
             return 0;
         } else {
             return recentRealDriver.getMinorVersion();
         }
     }
 
     @Override
     public boolean jdbcCompliant() {
         return recentRealDriver != null
                 && recentRealDriver.jdbcCompliant();
     }
 
     @Override
     public boolean acceptsURL(String url) throws SQLException {
         Driver d = getUnderlyingDriver(url);
         if (d != null) {
             recentRealDriver = d;
             return true;
         } else {
             return false;
         }
     }
 
     /**
      * Ensure that a wrapper is present for the given url, properties and driver
      *
      * @param url
      * @param info
      * @param d
      * @return
      * @throws SQLException
      */
     protected TransactionWrappedConnection ensureWrapperPresent(String url, Properties info, Driver d) throws SQLException {
         //create the wrapped transaction
         TransactionWrappedConnection transactionWrappedConnection;
         synchronized (transactionWrappedCache) {
             ConnectionKey key = new ConnectionKey(url, info);
             transactionWrappedConnection = DBCleaner.transactionWrappedCache.get(key);
             if (transactionWrappedConnection != null && transactionWrappedConnection.isClosed()) {
                 transactionWrappedConnection = null;
             }
             if (transactionWrappedConnection == null) {
                 Connection c = d.connect(url, info);
                 if (c == null) {
                     throw new SQLException("invalid or unknown driver url: " + url);
                 }
                 transactionWrappedConnection = new TransactionWrappedConnection(c);
                 transactionWrappedCache.put(key, transactionWrappedConnection);
             }
         }
         return transactionWrappedConnection;
     }
 
     /**
      * Accepts a URL that starts with jdbc:dbcleaner, and returns the underlying driver
      */
     private Driver getUnderlyingDriver(String url) throws SQLException {
 
         if (url.startsWith(JDBCPREFIX)) {
 
             url = getUnderlyingUrl(url);
             Enumeration e = DriverManager.getDrivers();
 
             while (e.hasMoreElements()) {
                 Driver d = (Driver) e.nextElement();
 
                 if (d.acceptsURL(url)) {
                     return d;
                 }
             }
         }
         return null;
     }
 
     /**
      * Strip jdbc:dbcleaner from the URL, then add jdbc: to it.
      *
      * @param url
      * @return
      */
     protected String getUnderlyingUrl(String url) {
         url = "jdbc:" + url.substring(JDBCPREFIX.length() + 1);
         return url;
     }
 
     @Override
     public Connection connect(String url, Properties info) throws SQLException {
         Driver d = getUnderlyingDriver(url);
         if (d == null) {
             return null;
         }
 
         // get actual URL that the real driver expects
         // (strip off "jdbc:dbcleaner" from url)
         url = getUnderlyingUrl(url);
         TransactionWrappedConnection transactionWrappedConnection = ensureWrapperPresent(url, info, d);
         recentRealDriver = d;
 
         //now create the actual connection
         Connection c = null;
         if (!TransactionUtil.isInForcedTransaction()) {
             c = d.connect(url, info);
         }
         SwitchingConnectionWrapper wrapper = new SwitchingConnectionWrapper(c, transactionWrappedConnection, url, info);
 
 
         return wrapper;
     }
 
     @Override
     public DriverPropertyInfo[] getPropertyInfo(String url, Properties info)
             throws SQLException {
         Driver d = getUnderlyingDriver(url);
         if (d == null) {
             return new DriverPropertyInfo[0];
         }
 
         recentRealDriver = d;
         return d.getPropertyInfo(url, info);
     }
 }
 
 /**
  * Properties + JDBC url of an underlying connection, for use as cache key
  *
  * @author pieter.bos
  */
 class ConnectionKey {
 
     Properties info;
     String url;
 
     public ConnectionKey(String url, Properties info) {
         this.url = url;
         this.info = info;
     }
 
     @Override
     public boolean equals(Object other) {
         if (other instanceof ConnectionKey) {
             ConnectionKey o = (ConnectionKey) other;
             return info.equals(o.info) && url.equals(o.url);
         }
         return false;
     }
 
     @Override
     public int hashCode() {
         return info.hashCode() + url.hashCode();
     }
 }
