 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011-2012, CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons.database;
 
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Vector;
 
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 import com.craftfire.commons.TimeUtil;
 import com.craftfire.commons.util.LoggingManager;
 
 public class DataManager {
     private boolean keepAlive, reconnect;
     private String host, username, password, database, prefix = "", lastQuery,
             directory;
     private String url = null;
     private Map<Long, String> queries = new HashMap<Long, String>();
     private long startup;
     private int timeout = 0, port = 3306, queriesCount = 0;
     private Connection con = null;
     private final DataType datatype;
     private PreparedStatement pStmt = null;
     private Statement stmt = null;
     private ResultSet rs = null;
     private LoggingManager loggingManager = new LoggingManager("CraftFire.DataManager", "[DataManager]");
 
     public DataManager(String username, String password) {
         this(DataType.MYSQL, username, password);
     }
 
     public DataManager(DataType type, String username, String password) {
         this.datatype = type;
         this.username = username;
         this.password = password;
         if (!getLogging().isLogging()) {
             getLogging().setDirectory(this.directory);
             getLogging().setLogging(true);
         }
     }
 
     public String getURL() {
         return this.url;
     }
 
     public LoggingManager getLogging() {
         return this.loggingManager;
     }
 
     public void setLoggingManager(LoggingManager loggingManager) {
         this.loggingManager = loggingManager;
     }
 
     public boolean isKeepAlive() {
         return this.keepAlive;
     }
 
     public void setKeepAlive(boolean keepAlive) {
         this.keepAlive = keepAlive;
         if (keepAlive) {
             connect();
         }
     }
 
     public int getTimeout() {
         return this.timeout;
     }
 
     public void setTimeout(int timeout) {
         this.timeout = timeout;
     }
 
     public Long getStartup() {
         return this.startup;
     }
 
     public Long getUptime() {
         return (System.currentTimeMillis() / 1000) - getStartup();
     }
 
     public String getHost() {
         return this.host;
     }
 
     public void setHost(String host) {
         this.host = host;
     }
 
     public int getPort() {
         return this.port;
     }
 
     public void setPort(int port) {
         this.port = port;
     }
 
     public String getDatabase() {
         return this.database;
     }
 
     public void setDatabase(String database) {
         this.database = database;
     }
 
     public String getUsername() {
         return this.username;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public String getPassword() {
         return this.password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getDirectory() {
         return this.directory;
     }
 
     public void setDirectory(String directory) {
         File file = new File(directory);
         if (!file.exists()) {
             getLogging().debug(directory + " does not exist, attempting to create it.");
             if (file.mkdirs()) {
                 getLogging().debug("Successfully created directory: " + directory);
             } else {
                 getLogging().error("Could not create directory: " + directory);
             }
         }
         this.directory = directory;
     }
 
     public String getPrefix() {
         return this.prefix;
     }
 
     public void setPrefix(String prefix) {
         this.prefix = prefix;
     }
 
     public int getQueriesCount() {
         return this.queriesCount;
     }
 
     public Map<Long, String> getQueries() {
         return this.queries;
     }
 
     public String getLastQuery() {
         return this.lastQuery;
     }
 
     public DataType getDataType() {
         return this.datatype;
     }
 
     public Connection getConnection() {
         return this.con;
     }
 
     protected boolean setURL() {
         switch (this.datatype) {
             case MYSQL:
                 if (this.host == null || this.database == null) {
                     getLogging().debug("Could not set mySQL URL. Host: " + this.host + ", Database: " + this.database);
                     return false;
                 }
                 this.url = "jdbc:mysql://" + this.host + "/" + this.database
                          + "?zeroDateTimeBehavior=convertToNull"
                          + "&jdbcCompliantTruncation=false"
                          + "&autoReconnect=true"
                          + "&characterEncoding=UTF-8"
                          + "&characterSetResults=UTF-8";
                 outputDrivers();
                 return true;
             case H2:
                 if (this.directory == null || this.database == null) {
                     getLogging().debug("Could not set H2 URL. Host: " + this.directory + ", Database: " + this.database);
                     return false;
                 }
                 this.url = "jdbc:h2:" + this.directory + this.database + ";AUTO_RECONNECT=TRUE";
                 outputDrivers();
                 return true;
         }
         return false;
     }
 
     public boolean exist(String table, String field, Object value) {
         try {
             return getField(FieldType.STRING, "SELECT `" + field + "` " +
                             "FROM `" + getPrefix() + table + "` " +
                             "WHERE `" + field + "` = '" + value + "' " + "LIMIT 1") != null;
         } catch (SQLException e) {
             return false;
         }
     }
 
     public boolean tableExist(String table) {
         try {
             return getField(FieldType.INTEGER, "SELECT COUNT(*) FROM `" + getPrefix() + table + "` LIMIT 1") != null;
         } catch (SQLException e) {
             return false;
         }
     }
 
     public int getLastID(String field, String table) {
         DataField f;
         try {
             f = getField(FieldType.INTEGER, "SELECT `" + field +
                          "` FROM `" + getPrefix() + table +
                          "` ORDER BY `" + field + "` DESC LIMIT 1");
             if (f != null) {
                 return f.getInt();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return 0;
     }
 
     public int getLastID(String field, String table, String where) {
         DataField f;
         try {
             f = getField(FieldType.INTEGER, "SELECT `" + field + "` "
                     + "FROM `" + getPrefix() + table + "` " + "WHERE "
                     + where + " " + "ORDER BY `" + field + "` DESC LIMIT 1");
             if (f != null) {
                 return f.getInt();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return 0;
     }
 
     public int getCount(String table, String where) {
         DataField f;
         try {
             f = getField(FieldType.INTEGER, "SELECT COUNT(*) FROM `"
                     + getPrefix() + table + "` WHERE " + where
                     + " LIMIT 1");
             if (f != null) {
                 return f.getInt();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return 0;
     }
 
     public int getCount(String table) {
         DataField f;
         try {
             f = getField(FieldType.INTEGER, "SELECT COUNT(*) FROM `"
                     + getPrefix() + table + "` LIMIT 1");
             if (f != null) {
                 return f.getInt();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return 0;
     }
 
     public void increaseField(String table, String field, String where) throws SQLException {
         executeQuery("UPDATE `" + getPrefix() + table + "` SET `" + field
                           + "` = " + field + " + 1 WHERE " + where);
     }
 
     public String getStringField(String query) {
         DataField f;
         try {
             f = getField(FieldType.STRING, query);
             if (f != null) {
                 return f.getString();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return null;
     }
 
     public String getStringField(String table, String field, String where) {
         DataField f;
         try {
             f = getField(FieldType.STRING, table, field, where);
             if (f != null) {
                 return f.getString();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return null;
     }
 
     public int getIntegerField(String query) {
         DataField f;
         try {
             f = getField(FieldType.INTEGER, query);
             if (f != null) {
                 return f.getInt();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return 0;
     }
 
     public int getIntegerField(String table, String field, String where) {
         DataField f;
         try {
             f = getField(FieldType.INTEGER, table, field, where);
             if (f != null) {
                 return f.getInt();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return 0;
     }
 
     public Date getDateField(String query) {
         DataField f;
         try {
             f = getField(FieldType.DATE, query);
             if (f != null) {
                 return f.getDate();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return null;
     }
 
     public Date getDateField(String table, String field, String where) {
         DataField f;
         try {
             f = getField(FieldType.DATE, table, field, where);
             if (f != null) {
                 return f.getDate();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return null;
     }
 
     public Blob getBlobField(String query) {
         DataField f;
         try {
             f = getField(FieldType.BLOB, query);
             if (f != null) {
                 return f.getBlob();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return null;
     }
 
     public Blob getBlobField(String table, String field, String where) {
         DataField f;
         try {
             f = getField(FieldType.BLOB, table, field, where);
             if (f != null) {
                 return f.getBlob();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return null;
     }
 
     public boolean getBooleanField(String query) {
         DataField f;
         try {
             f = getField(FieldType.BOOLEAN, query);
             if (f != null) {
                 return f.getBool();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return false;
     }
 
     public boolean getBooleanField(String table, String field, String where) {
         DataField f;
         try {
             f = getField(FieldType.BOOLEAN, table, field, where);
             if (f != null) {
                 return f.getBool();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return false;
     }
 
     public double getDoubleField(String query) {
         DataField f;
         try {
             f = getField(FieldType.REAL, query);
             if (f != null) {
                 return f.getDouble();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return 0;
     }
 
     public double getDoubleField(String table, String field, String where) {
         DataField f;
         try {
             f = getField(FieldType.REAL, table, field, where);
             if (f != null) {
                 return f.getDouble();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return 0;
     }
 
     public String getBinaryField(String query) {
         DataField f;
         try {
             f = getField(FieldType.BINARY, query);
             if (f != null) {
                 return f.getString();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return null;
     }
 
     public String getBinaryField(String table, String field, String where) {
         DataField f;
         try {
             f = getField(FieldType.BINARY, table, field, where);
             if (f != null) {
                 return f.getString();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return null;
     }
 
     public DataField getField(FieldType type, String table, String field, String where) throws SQLException {
         return getField(type, "SELECT `" + field + "` FROM `" + getPrefix() + table + "` WHERE " + where + " LIMIT 1");
     }
 
     public DataField getField(FieldType type, String query) throws SQLException {
         try {
             this.rs = getResultSet(query);
             if (this.rs.next()) {
                 Object value;
                 if (type.equals(FieldType.STRING)) {
                     value = this.rs.getString(1);
                 } else if (type.equals(FieldType.INTEGER)) {
                     value = this.rs.getInt(1);
                 } else if (type.equals(FieldType.DATE)) {
                     value = this.rs.getTimestamp(1);
                 } else if (type.equals(FieldType.BLOB)) {
                     value = this.rs.getBlob(1);
                 } else if (type.equals(FieldType.BINARY)) {
                     value = this.rs.getBytes(1);
                 } else if (type.equals(FieldType.BOOLEAN)) {
                     value = this.rs.getBoolean(1);
                 } else if (type.equals(FieldType.REAL)) {
                     value = this.rs.getDouble(1);
                 } else if (type.equals(FieldType.UNKNOWN)) {
                     DataField dataField = new DataField(1, this.rs);
                     close();
                     return dataField;
                 } else {
                 	close();
                 	return null;
                 }
                 DataField dataField = new DataField(type, this.rs.getMetaData().getColumnDisplaySize(1), value);
                 close();
                 return dataField;
             }
         } finally {
             close();
         }
         return null;
     }
 
     public void executeQuery(String query) throws SQLException {
         connect();
         this.pStmt = this.con.prepareStatement(query);
         this.pStmt.executeUpdate();
         log(query);
         close();
     }
 
     public void executeQueryVoid(String query) {
         try {
             executeQuery(query);
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
     }
 
     public void updateBlob(String table, String field, String where, String data) {
         try {
             String query = "UPDATE `" + getPrefix() + table + "` " + "SET `" + field + "` = ? " + "WHERE " + where;
             byte[] array = data.getBytes();
             ByteArrayInputStream inputStream = new ByteArrayInputStream(array);
             connect();
             log(query);
             this.stmt = this.con.createStatement();
             this.pStmt = this.con.prepareStatement(query);
             this.pStmt.setBlob(1, inputStream, array.length);
             this.pStmt.executeUpdate();
             close();
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
     }
 
     public void updateField(String table, String field, Object value, String where) throws SQLException {
         executeQuery("UPDATE `" + getPrefix() + table + "` SET `" + field + "` = '" + value + "' WHERE " + where);
     }
 
     public void updateFields(Map<String, Object> data, String table, String where) throws SQLException {
         String update = updateFieldsString(data);
         executeQuery("UPDATE `" + getPrefix() + table + "`" + update + " WHERE " + where);
     }
 
     public void insertField(String table, String field, Object value) throws SQLException {
         executeQuery("INSERT INTO `" + getPrefix() + table + "` (`" + field + "`) VALUES ('" + value + "')");
     }
 
     public void insertFields(Map<String, Object> data, String table) throws SQLException {
         String insert = insertFieldString(data);
         executeQuery("INSERT INTO `" + getPrefix() + table + "` " + insert);
     }
 
     @Deprecated
     public TableModel resultSetToTableModel(String query) {
         try {
             this.rs = getResultSet(query);
             ResultSetMetaData metaData = this.rs.getMetaData();
             int numberOfColumns = metaData.getColumnCount();
             Vector<String> columnNames = new Vector<String>();
             for (int column = 0; column < numberOfColumns; column++) {
                 columnNames.addElement(metaData.getColumnLabel(column + 1));
             }
             Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
             while (this.rs.next()) {
                 Vector<Object> newRow = new Vector<Object>();
                 for (int i = 1; i <= numberOfColumns; i++) {
                     newRow.addElement(this.rs.getObject(i));
                 }
                 rows.addElement(newRow);
             }
             close();
             return new DefaultTableModel(rows, columnNames);
         } catch (Exception e) {
             getLogging().stackTrace(e);
             close();
             return null;
         }
     }
 
     public Results getResults(String query) throws SQLException {
         try {
             this.rs = getResultSet(query);
             Results results = new Results(query, this.rs);
             close();
             return results;
         } finally {
             close();
         }
     }
 
     @Deprecated
     public Map<String, Object> getArray(String query) {
         try {
             this.rs = getResultSet(query);
             ResultSetMetaData metaData = this.rs.getMetaData();
             int numberOfColumns = metaData.getColumnCount();
             Map<String, Object> data = new HashMap<String, Object>();
             while (this.rs.next()) {
                 for (int i = 1; i <= numberOfColumns; i++) {
                     data.put(metaData.getColumnLabel(i), this.rs.getObject(i));
                 }
             }
             close();
             return data;
         } catch (SQLException e) {
             close();
             getLogging().stackTrace(e);
         }
         return null;
     }
 
     @Deprecated
     public List<HashMap<String, Object>> getArrayList(String query) {
         try {
             List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
             this.rs = getResultSet(query);
             ResultSetMetaData metaData = this.rs.getMetaData();
             int numberOfColumns = metaData.getColumnCount();
             while (this.rs.next()) {
                 HashMap<String, Object> data = new HashMap<String, Object>();
                 for (int i = 1; i <= numberOfColumns; i++) {
                     data.put(metaData.getColumnLabel(i), this.rs.getString(i));
                 }
                 list.add(data);
             }
             close();
             return list;
         } catch (SQLException e) {
             close();
             getLogging().stackTrace(e);
         }
         return null;
     }
 
     public ResultSet getResultSet(String query) throws SQLException {
         connect();
         this.stmt = this.con.createStatement();
         log(query);
         this.rs = this.stmt.executeQuery(query);
         return this.rs;
     }
 
     private void log(String query) {
        getLogging().debug("Executing " + this.datatype + " query: '" + query + "'");
         this.lastQuery = query;
         this.queries.put(System.currentTimeMillis(), query);
         this.queriesCount++;
     }
 
     private void outputDrivers() {
         if (getLogging().isDebug()) {
             getLogging().debug("Checking DriverManager drivers.");
             Enumeration<Driver> driverList = DriverManager.getDrivers();
             int count = 0;
             while (driverList.hasMoreElements()) {
                 Driver driverClass = driverList.nextElement();
                 getLogging().debug("Found driver #" + (count + 1) + ": " + driverClass.getClass().getName());
                 count++;
             }
             getLogging().debug("Found " + count + " drivers in DriverManager.");
         }
     }
 
     public boolean isConnected() {
         try {
             if (this.con != null) {
                 return !this.con.isClosed();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return false;
     }
 
     public boolean hasConnection() {
         try {
             boolean result = false;
             connect();
             if (this.con != null) {
                 result = !this.con.isClosed();
             }
             close();
             return result;
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
         return false;
     }
 
     public void connect() {
         if (this.url == null && !setURL()) {
             return;
         }
         if (this.con != null && isConnected()) {
             return;
         }
         try {
             switch (this.datatype) {
                 case MYSQL:
                     getLogging().debug("Connecting to MYSQL with URL '" + this.url + "'.");
                     Class.forName("com.mysql.jdbc.Driver");
                     this.con = DriverManager.getConnection(this.url, this.username, this.password);
                     break;
                 case H2:
                     getLogging().debug("Connecting to H2 with URL '" + this.url + "'.");
                     Class.forName("org.h2.Driver");
                     this.con = DriverManager.getConnection(this.url, this.username, this.password);
                     break;
             }
             this.startup = System.currentTimeMillis() / 1000;
         } catch (ClassNotFoundException e) {
             getLogging().stackTrace(e);
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
     }
 
     public void close() {
         close(false);
     }
 
     public void close(boolean force) {
         if (this.keepAlive && !this.reconnect && !force) {
             if (this.timeout == 0) {
                 return;
             } else if ((System.currentTimeMillis() / 1000) < (this.startup + this.timeout)) {
                 return;
             }
         }
         try {
             getLogging().debug("Closing connection for '" + this.datatype + "'. Uptime: " + new TimeUtil(getUptime()).toString() + ". Queries: " + getQueriesCount());
             this.con.close();
             if (this.rs != null) {
                 this.rs.close();
                 this.rs = null;
             }
             if (this.pStmt != null) {
                 this.pStmt.close();
                 this.pStmt = null;
             }
             if (this.stmt != null) {
                 this.stmt.close();
                 this.stmt = null;
             }
            if (this.keepAlive && !force) {
                 connect();
             }
         } catch (SQLException e) {
             getLogging().stackTrace(e);
         }
     }
 
     public void reconnect() {
         this.reconnect = true;
         close();
         connect();
         this.reconnect = false;
     }
 
     private String updateFieldsString(Map<String, Object> data) {
         String query = " SET", suffix = ",";
         int i = 1;
         Iterator<Entry<String, Object>> it = data.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry<String, Object> pairs = it.next();
             if (i == data.size()) {
                 suffix = "";
             }
             Object val = pairs.getValue();
             String valstr = null;
             if (val instanceof Date) {
                 val = new Timestamp(((Date) val).getTime());
             }
             if (val == null) {
                 valstr = "NULL";
             } else {
                 valstr = "'" + val.toString().replaceAll("'", "''") + "'";
             }
             query += " `" + pairs.getKey() + "` =  " + valstr + suffix;
             i++;
         }
         return query;
     }
 
     private String insertFieldString(Map<String, Object> data) {
         String fields = "", values = "", query = "", suffix = ",";
         int i = 1;
         Iterator<Entry<String, Object>> it = data.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry<String, Object> pairs = it.next();
             if (i == data.size()) {
                 suffix = "";
             }
             Object val = pairs.getValue();
             String valstr = null;
             if (val instanceof Date) {
                 val = new Timestamp(((Date) val).getTime());
             }
             if (val == null) {
                 valstr = "NULL";
             } else {
                 valstr = "'" + val.toString().replaceAll("'", "''") + "'";
             }
             fields += " `" + pairs.getKey() + "`" + suffix;
             values += valstr + suffix;
             i++;
         }
         query = "(" + fields + ") VALUES (" + values + ")";
         return query;
     }
 }
