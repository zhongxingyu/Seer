 /*
  * Copyright 2011 SUSE Linux Products GmbH
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.suse.logkeeper.plugins;
 
 import de.suse.logkeeper.plugins.LogKeeperBackend;
 import de.suse.logkeeper.plugins.LogKeeperBackendException;
 import de.suse.logkeeper.service.entities.LogEntry;
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.MalformedURLException;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 
 /**
  *
  * @author bo
  */
 public class RDBMSLog implements LogKeeperBackend {
     public static final String DB_POSTGRES = "postgresql";
     public static final String DB_MYSQL = "mysql";
     public static final String DB_ORACLE_THIN = "oracle-thin";
 
     private static final String SCHEMA_LOG_TABLE = "AUDIT_LOG";
     
     private static final String FLAG_DEBUG = "";
  
     private String user;
     private String password;
     private String host;
     private String port;
     private String database;
     private String vendor;
     private String ssl;
 
     private Connection connection;
     private boolean debug;
 
 
     public void setup(String definition, Properties setup) throws LogKeeperBackendException {
         this.user = setup.getProperty("plugin." + definition + ".user");
         this.password = setup.getProperty("plugin." + definition + ".password");
         this.host = setup.getProperty("plugin." + definition + ".host");
         this.port = setup.getProperty("plugin." + definition + ".port");
         this.database = setup.getProperty("plugin." + definition + ".database", "");
         this.vendor = setup.getProperty("plugin." + definition + ".vendor", "");
         this.ssl = setup.getProperty("plugin." + definition + ".ssl");
 
         try {
             this.connect();
         } catch (SQLException ex) {
             if (this.isDebug()) {
                 Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
             }
             System.out.println("SQL Error: " + ex.getLocalizedMessage());
         } catch (InstantiationException ex) {
             if (this.isDebug()) {
                 Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
             }
             System.out.println("RDBMS driver error: " + ex.getLocalizedMessage());
         } catch (IllegalAccessException ex) {
             if (this.isDebug()) {
                 Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
             }
             System.out.println("Access error: " + ex.getLocalizedMessage());
         } catch (ClassNotFoundException ex) {
             if (this.isDebug()) {
                 Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
             }
             System.out.println(" Error: " + ex.getLocalizedMessage());
         } catch (Exception ex) {
             if (this.isDebug()) {
                 Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
             }
             System.out.println("Connect error: " + ex.getLocalizedMessage());
         }
     }
 
 
     /**
      * Add log entry to the database.
      *
      * @param entry
      * @throws LogKeeperBackendException
      */
     public void log(LogEntry entry) throws LogKeeperBackendException {
         synchronized (this) {
             Long id = this.allocateId();
             if (id != null) {
                 try {
                     this.insertEntry(id, entry);
                     this.insertExtmap(id, entry);
                 } catch (SQLException ex) {
                     Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
     }
 
 
     /**
      * Insert a log entry to the database.
      * @param id
      * @param entry
      */
     private void insertEntry(Long id, LogEntry entry) throws SQLException {
         PreparedStatement statement = this.connection.prepareStatement("INSERT INTO AUDIT_LOG_ENTRIES "
                 + "(ENTRY_ID, LOG_LEVEL, MESSAGE, USERID, IPADDR, HAPPENED) VALUES (?, ?, ?, ?, ?, ?)");
         statement.setLong(1, id);
         statement.setLong(2, entry.getLevel());
         statement.setString(3, entry.getMessage());
         statement.setString(4, entry.getUserName());
         statement.setString(5, entry.getNode().getHostAddress());
         statement.setTimestamp(6, (Timestamp) entry.getTimestamp());
         statement.execute();
     }
 
 
     /**
      * Insert all ext-map to the database.
      * @param id
      * @param entry
      */
     private void insertExtmap(Long id, LogEntry entry) throws SQLException {
         for (String key : entry.getExtmap().keySet()) {
             PreparedStatement statement = this.connection.prepareStatement("INSERT INTO AUDIT_LOG_EXTMAP "
                     + "(E_ID, E_KEY, E_VALUE) VALUES (?, ?, ?)");
             statement.setLong(1, id);
             statement.setString(2, key);
             statement.setString(3, entry.getExtmap().get(key));
             statement.execute();
         }
     }
 
 
     /**
      * Allocate a log entry ID.
      * 
      * @return
      */
     private Long allocateId() {
         Long id = null;
         try {
             PreparedStatement allocId = null;
             if (this.vendor.equals(RDBMSLog.DB_ORACLE_THIN)) {
                 allocId = this.connection.prepareStatement("SELECT AUDIT_LOG_SEQ.NEXTVAL AS ENTRY_ID FROM DUAL");
             } else if (this.vendor.equals(RDBMSLog.DB_POSTGRES)) {
                 allocId = this.connection.prepareStatement("SELECT NEXTVAL('AUDIT_LOG_SEQ') AS ENTRY_ID");
             }
 
             ResultSet result = allocId.executeQuery();
             if (result.next()) {
                 id = result.getLong("ENTRY_ID");
             }
         } catch (SQLException ex) {
             Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         return id;
     }
 
 
     /**
      * Method to initialize log data.
      */
     protected void initDatabase() throws SQLException {
         List<String> statements = new ArrayList<String>();
         if (this.vendor.equals(RDBMSLog.DB_POSTGRES)) {
             statements.add(this.getTextFromResource(this.getClass().getResourceAsStream("/com/suse/logkeeper/plugins/postgres-init.sql")));
         } else if (this.vendor.equals(RDBMSLog.DB_ORACLE_THIN)) {
             statements.add(this.getTextFromResource(this.getClass().getResourceAsStream("/com/suse/logkeeper/plugins/oracle-init-drop.sql")));
             statements.add(this.getTextFromResource(this.getClass().getResourceAsStream("/com/suse/logkeeper/plugins/oracle-init-create-seq.sql")));
             statements.add(this.getTextFromResource(this.getClass().getResourceAsStream("/com/suse/logkeeper/plugins/oracle-init-create-entries.sql")));
             statements.add(this.getTextFromResource(this.getClass().getResourceAsStream("/com/suse/logkeeper/plugins/oracle-init-create-extmap.sql")));
         } else {
             System.err.println(String.format("Database vendor %s is not supported.", this.vendor));
             System.exit(1);
         }
 
         try {
             for (int i = 0; i < statements.size(); i++) {
                 try{
                     this.connection.prepareStatement(statements.get(i)).execute();
                 } catch (SQLException ex) {
                     System.err.println(ex.getLocalizedMessage());
                     System.err.println("----");
                     System.err.println(statements.get(i));
                     System.err.println("----");
                 }
             }
         } finally {
             this.connection.clearWarnings();
             this.connection.close();
         }
     }
 
 
     /**
      * Get test from the resource.
      * 
      * @param inputStream
      * @return
      */
     private String getTextFromResource(InputStream inputStream) {
         BufferedReader bufferedReader = null;
         String line;
         StringBuilder buff = new StringBuilder();
 
         try {
             bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
             while (null != (line = bufferedReader.readLine())) {
                 buff.append(line).append("\n");
             }
         } catch (Exception ex) {
             Logger.getLogger(RDBMSLog.class.getName()).log(Level.WARNING, null, ex);
         } finally {
             try {
                 if (bufferedReader != null) {
                     bufferedReader.close();
                 }
 
                 if (inputStream != null) {
                     inputStream.close();
                 }
             } catch (IOException ex) {
                 Logger.getLogger(RDBMSLog.class.getName()).log(Level.WARNING, null, ex);
             }
         }
 
         return buff.toString();
     }
 
 
     private void connect() throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
         if (this.vendor.equals(RDBMSLog.DB_POSTGRES)) {
             Class.forName("org.postgresql.Driver");
             Properties props = new Properties();
             props.setProperty("user", this.user);
             props.setProperty("password", this.password);
             if (this.ssl != null) {
                 props.setProperty("ssl", "true");
             }
 
             if (this.port == null) {
                 this.port = "5432";
             }
             this.connection = DriverManager.getConnection(String.format("jdbc:postgresql://%s:%s/%s",
                     this.host, this.port, this.database), props);
         } else if (this.vendor.equals(RDBMSLog.DB_MYSQL)) {
             Class.forName("com.mysql.jdbc.Driver").newInstance();
             if (this.port == null) {
                 this.port = "3306";
             }
             this.connection = DriverManager.getConnection(String.format("jdbc:mysql://%s:%s/%s",
                     this.host, this.port, this.database), this.user, this.password);
         } else if (this.vendor.equals(RDBMSLog.DB_ORACLE_THIN)) {
             Class.forName ("oracle.jdbc.OracleDriver");
             if (this.port == null) {
                 this.port = "1521";
             }
             this.connection = DriverManager.getConnection(String.format("jdbc:oracle:thin:%s/%s@//%s:%s/%s",
                     this.user, this.password, this.host, this.port, this.database));
         }
     }
 
     /**
      * @param args the command line arguments
      * The argument is not meant to be used by humans.
      * There are three argumens can be passed:
      * 1. Path to the auditlog configuration file.
      * 2. Tag of the database in the configuration file.
      * 3. Comma-separated flags.
      */
     public static void main(String[] args){
         if (args.length > 1) {
             boolean debug = args.length > 2 ? args[2].contains(RDBMSLog.FLAG_DEBUG) : false;
             try {
                 RDBMSLog.localInit(new URL(args[0]), args[1], debug);
                 System.out.println("Done.");
             } catch (MalformedURLException ex) {
                 if (debug) {
                     Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 System.out.println("URL is malformed: " + ex.getLocalizedMessage());
             } catch (URISyntaxException ex) {
                 if (debug) {
                     Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 System.out.println("URL syntax error: " + ex.getLocalizedMessage());
             } catch (LogKeeperBackendException ex) {
                 if (debug) {
                     Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 System.out.println("Log keeper exception: " + ex.getLocalizedMessage());
             } catch (SQLException ex) {
                 if (debug) {
                     Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);
                 }
                 System.out.println("SQL Error: " + ex.getLocalizedMessage());
             }
         } else {
             System.out.println("Usage: " + RDBMSLog.class.getCanonicalName() + " <config url> <tag> [flag,flag1,...]");
         }
     }
 
     public static void localInit(URL configPath, String tag, boolean debug)
             throws URISyntaxException,
                    LogKeeperBackendException,
                    SQLException {
         File conf = new File(configPath.toURI());
         if (!conf.canRead()) {
             System.out.println(String.format("Error: %s is missing.", configPath));
         }
 
         RDBMSLog log = new RDBMSLog().setDebug(debug);
         Properties setup = new Properties();
         try {
             setup.load(new FileInputStream(conf));
         } catch (IOException ex) {
             if (log.isDebug()) {
                 Logger.getLogger(RDBMSLog.class.getName()).log(Level.SEVERE, null, ex);                
             }
             System.out.println("Error: " + ex.getLocalizedMessage());
             System.exit(0);
         }
 
         String database = setup.getProperty("plugin." + tag + ".database");
         if (database == null) {
             System.out.println("Database was not found...");
             System.exit(0);
         }
         
         log.setup(tag, setup);
         log.initDatabase();
     }
 
     /**
      * Set debugging on or off.
      *
      * @param debug
      * @return 
      */
     private RDBMSLog setDebug(boolean debug) {
         this.debug = debug;
         return this;
     }
 
     /**
      * Debug mode status.
      * @return 
      */
     protected boolean isDebug() {
         return debug;
     }
 }
