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
 package com.craftfire.commons.managers;
 
 import java.io.ByteArrayInputStream;
 import java.sql.Blob;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Vector;
 
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 
 import com.craftfire.commons.database.DataField;
 import com.craftfire.commons.database.Results;
 import com.craftfire.commons.enums.DataType;
 import com.craftfire.commons.enums.FieldType;
 
 public class DataManager {
     private boolean keepAlive, reconnect;
     private String host, username, password, database, prefix, query,
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
     private static LoggingManager logMgr = new LoggingManager("CraftFire.DataManager", "[DataManager]");
 
     public DataManager(DataType type, String username, String password) {
         this.datatype = type;
         this.username = username;
         this.password = password;
         this.startup = System.currentTimeMillis() / 1000;
         if (!logMgr.isLogging()) {
             logMgr.setDirectory(this.directory);
             logMgr.setLogging(true);
         }
     }
 
     public String getURL() {
         return this.url;
     }
 
     public static LoggingManager getLogManager() {
         return DataManager.logMgr;
     }
 
     public boolean isKeepAlive() {
         return this.keepAlive;
     }
 
     public void setKeepAlive(boolean keepAlive) {
         this.keepAlive = keepAlive;
         if (keepAlive) {
             this.connect();
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
         this.directory = directory;
     }
 
     public String getPrefix() {
         return this.prefix;
     }
 
     public void setPrefix(String prefix) {
         this.prefix = prefix;
     }
 
     public ResultSet getCurrentResultSet() {
         return this.rs;
     }
 
     public PreparedStatement getCurrentPreparedStatement() {
         return this.pStmt;
     }
 
     public Statement getCurrentStatement() {
         return this.stmt;
     }
 
     public int getQueriesCount() {
         return this.queriesCount;
     }
 
     public Map<Long, String> getQueries() {
         return this.queries;
     }
 
     public String getLastQuery() {
         return this.query;
     }
 
     public DataType getDataType() {
         return this.datatype;
     }
 
     public Connection getConnection() {
         return this.con;
     }
 
     protected void setURL() {
         switch (this.datatype) {
         case MYSQL:
             this.url = "jdbc:mysql://" + this.host + "/" + this.database
                     + "?zeroDateTimeBehavior=convertToNull"
                     + "&jdbcCompliantTruncation=false";
             break;
         case H2:
             this.url = "jdbc:h2:" + this.directory + this.database;
             break;
         }
     }
 
     public boolean exist(String table, String field, Object value) {
         return this.getField(FieldType.STRING, "SELECT `" + field + "` " + "FROM `"
                 + this.getPrefix() + table + "` " + "WHERE `" + field + "` = '"
                 + value + "' " + "LIMIT 1") != null;
     }
 
     public int getLastID(String field, String table) {
         DataField f = this.getField(FieldType.INTEGER, "SELECT `" + field
                 + "` FROM `" + this.getPrefix() + table + "` ORDER BY `"
                 + field + "` DESC LIMIT 1");
         if (f != null) {
             return f.getInt();
         }
         return 0;
     }
 
     public int getLastID(String field, String table, String where) {
         DataField f = this.getField(FieldType.INTEGER, "SELECT `" + field
                 + "` " + "FROM `" + this.getPrefix() + table + "` " + "WHERE "
                 + where + " " + "ORDER BY `" + field + "` DESC LIMIT 1");
         if (f != null) {
             return f.getInt();
         }
         return 0;
     }
 
     public int getCount(String table, String where) {
         DataField f = this.getField(FieldType.INTEGER, "SELECT COUNT(*) FROM `"
                 + this.getPrefix() + table + "` WHERE " + where + " LIMIT 1");
         if (f != null) {
             return f.getInt();
         }
         return 0;
     }
 
     public int getCount(String table) {
         DataField f = this.getField(FieldType.INTEGER, "SELECT COUNT(*) FROM `"
                 + this.getPrefix() + table + "` LIMIT 1");
         if (f != null) {
             return f.getInt();
         }
         return 0;
     }
 
     public void increaseField(String table, String field, String where) {
         this.executeQueryVoid("UPDATE `" + this.getPrefix() + table + "` SET `" + field
                 + "` =" + " " + field + " + 1 WHERE " + where);
     }
 
     public String getStringField(String query) {
         DataField f = this.getField(FieldType.STRING, query);
         if (f != null) {
             return f.getString();
         }
         return null;
     }
 
     public String getStringField(String table, String field, String where) {
         DataField f = this.getField(FieldType.STRING, table, field, where);
         if (f != null) {
             return f.getString();
         }
         return null;
     }
 
     public int getIntegerField(String query) {
         DataField f = this.getField(FieldType.INTEGER, query);
         if (f != null) {
             return f.getInt();
         }
         return 0;
     }
 
     public int getIntegerField(String table, String field, String where) {
         DataField f = this.getField(FieldType.INTEGER, table, field, where);
         if (f != null) {
             return f.getInt();
         }
         return 0;
     }
 
     public Date getDateField(String query) {
         DataField f = this.getField(FieldType.DATE, query);
         if (f != null) {
             return f.getDate();
         }
         return null;
     }
 
     public Date getDateField(String table, String field, String where) {
         DataField f = this.getField(FieldType.DATE, table, field, where);
         if (f != null) {
             return f.getDate();
         }
         return null;
     }
 
     public Blob getBlobField(String query) {
         DataField f = this.getField(FieldType.BLOB, query);
         if (f != null) {
             return f.getBlob();
         }
         return null;
     }
 
     public Blob getBlobField(String table, String field, String where) {
         DataField f = this.getField(FieldType.BLOB, table, field, where);
         if (f != null) {
             return f.getBlob();
         }
         return null;
     }
 
     public boolean getBooleanField(String query) {
         DataField f = this.getField(FieldType.BOOLEAN, query);
         if (f != null) {
             return f.getBool();
         }
         return false;
     }
 
     public boolean getBooleanField(String table, String field, String where) {
         DataField f = this.getField(FieldType.BOOLEAN, table, field, where);
         if (f != null) {
             return f.getBool();
         }
         return false;
     }
 
     public double getDoubleField(String query) {
         DataField f = this.getField(FieldType.REAL, query);
         if (f != null) {
             return f.getDouble();
         }
         return 0;
     }
 
     public double getDoubleField(String table, String field, String where) {
         DataField f = this.getField(FieldType.REAL, table, field, where);
         if (f != null) {
             return f.getDouble();
         }
         return 0;
     }
 
     public String String(String query) {
         DataField f = this.getField(FieldType.BINARY, query);
         if (f != null) {
             return f.getString();
         }
         return null;
     }
 
     public String getBinaryField(String table, String field, String where) {
         DataField f = this.getField(FieldType.BINARY, table, field, where);
         if (f != null) {
             return f.getString();
         }
         return null;
     }
 
     public DataField getField(FieldType fieldType, String table, String field,
             String where) {
         return this.getField(fieldType, "SELECT `" + field + "` FROM `"
                 + this.getPrefix() + table + "` WHERE " + where + " LIMIT 1");
     }
 
     public DataField getField(FieldType field, String query) {
         try {
             this.connect();
             this.stmt = this.con.createStatement();
             this.rs = this.stmt.executeQuery(query);
             this.log(query);
             if (this.rs.next()) {
                 Object value = null;
                 if (field.equals(FieldType.STRING)) {
                     value = this.rs.getString(1);
                 } else if (field.equals(FieldType.INTEGER)) {
                     value = this.rs.getInt(1);
                 } else if (field.equals(FieldType.DATE)) {
                     value = this.rs.getDate(1);
                 } else if (field.equals(FieldType.BLOB)) {
                     value = this.rs.getBlob(1);
                 } else if (field.equals(FieldType.BINARY)) {
                     value = this.rs.getBytes(1);
                 } else if (field.equals(FieldType.BOOLEAN)) {
                     value = this.rs.getBoolean(1);
                 } else if (field.equals(FieldType.REAL)) {
                     value = this.rs.getDouble(1);
                 } else if (field.equals(FieldType.UNKNOWN)) {
                     return new DataField(1, this.rs);
                 }
                 this.close();
                 if (value == null) {
                     return null;
                 }
                 return new DataField(field, this.rs.getMetaData()
                         .getColumnDisplaySize(1), value);
             }
         } catch (SQLException e) {
             e.printStackTrace();
             this.close();
         }
         return null;
     }
 
     public void executeQuery(String query) throws SQLException {
         this.connect();
         this.pStmt = this.con.prepareStatement(query);
         this.pStmt.executeUpdate();
         this.log(query);
         this.close();
     }
 
     public void executeQueryVoid(String query) {
         try {
             this.connect();
             this.pStmt = this.con.prepareStatement(query);
             this.pStmt.executeUpdate();
             this.log(query);
             this.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public void updateBlob(String table, String field, String where, String data) {
         try {
             String query = "UPDATE `" + this.getPrefix() + table + "` " + "SET `"
                     + field + "` = ? " + "WHERE " + where;
             byte[] array = data.getBytes();
             ByteArrayInputStream inputStream = new ByteArrayInputStream(array);
             this.connect();
             this.log(query);
             this.stmt = this.con.createStatement();
             this.pStmt = this.con.prepareStatement(query);
             this.pStmt.setBlob(1, inputStream, array.length);
             this.pStmt.executeUpdate();
             this.close();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public void updateFields(HashMap<String, Object> data, String table,
             String where) throws SQLException {
         String update = this.updateFieldsString(data);
         String query = "UPDATE `" + this.getPrefix() + table + "`" + update
                 + " WHERE " + where;
         this.connect();
         this.pStmt = this.con.prepareStatement(query);
         this.log(query);
         this.pStmt.executeUpdate();
         this.close();
     }
 
     public void insertFields(HashMap<String, Object> data, String table)
             throws SQLException {
         String insert = this.insertFieldString(data);
         String query = "INSERT INTO `" + this.getPrefix() + table + "` " + insert;
         this.connect();
         this.pStmt = this.con.prepareStatement(query);
         this.log(query);
         this.pStmt.executeUpdate();
         this.close();
     }
 
     public TableModel resultSetToTableModel(String query) {
         try {
             this.connect();
             Statement stmt = this.con.createStatement();
             this.rs = stmt.executeQuery(query);
             this.log(query);
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
             this.close();
             return new DefaultTableModel(rows, columnNames);
         } catch (Exception e) {
             e.printStackTrace();
             this.close();
             return null;
         }
     }
 
     public Results getResults(String query) {
         try {
             this.connect();
             this.stmt = this.con.createStatement();
             this.rs = this.stmt.executeQuery(query);
             this.log(query);
             Results results = new Results(query, this.rs);
             this.close();
             return results;
         } catch (SQLException e) {
             this.close();
             e.printStackTrace();
         }
         return null;
     }
 
     public HashMap<String, Object> getArray(String query) {
         try {
             this.connect();
             this.stmt = this.con.createStatement();
             this.rs = this.stmt.executeQuery(query);
             this.log(query);
             ResultSetMetaData metaData = this.rs.getMetaData();
             int numberOfColumns = metaData.getColumnCount();
             HashMap<String, Object> data = new HashMap<String, Object>();
             while (this.rs.next()) {
                 for (int i = 1; i <= numberOfColumns; i++) {
                     data.put(metaData.getColumnLabel(i), this.rs.getObject(i));
                 }
             }
             this.close();
             return data;
         } catch (SQLException e) {
             this.close();
             e.printStackTrace();
         }
         return null;
     }
 
     public List<HashMap<String, Object>> getArrayList(String query) {
         try {
             this.connect();
             List<HashMap<String, Object>> list = new ArrayList<HashMap<String, Object>>();
             this.stmt = this.con.createStatement();
             this.rs = this.stmt.executeQuery(query);
             this.log(query);
             ResultSetMetaData metaData = this.rs.getMetaData();
             int numberOfColumns = metaData.getColumnCount();
             while (this.rs.next()) {
                 HashMap<String, Object> data = new HashMap<String, Object>();
                 for (int i = 1; i <= numberOfColumns; i++) {
                     data.put(metaData.getColumnLabel(i), this.rs.getString(i));
                 }
                 list.add(data);
             }
             this.close();
             return list;
         } catch (SQLException e) {
             this.close();
             e.printStackTrace();
         }
         return null;
     }
 
     public ResultSet getResultSet(String query) throws SQLException {
         this.connect();
         this.stmt = this.con.createStatement();
         this.log(query);
         this.rs = this.stmt.executeQuery(query);
         return this.rs;
     }
 
     private void log(String query) {
         this.query = query;
         this.queries.put(System.currentTimeMillis(), query);
         this.queriesCount++;
     }
 
     public boolean isConnected() {
         try {
             if (this.con != null) {
                 return !this.con.isClosed();
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return false;
     }
 
     public boolean hasConnection() {
         try {
             boolean result = false;
             this.connect();
             if (this.con != null) {
                 result = !this.con.isClosed();
             }
             this.close();
             return result;
         } catch (SQLException e) {
             e.printStackTrace();
         }
         return false;
     }
 
     public void connect() {
         if (this.url == null) {
             this.setURL();
         }
         if (this.con != null && this.isConnected()) {
             return;
         }
         try {
             switch (this.datatype) {
             case MYSQL:
                 Class.forName("com.mysql.jdbc.Driver");
                 this.con = DriverManager.getConnection(this.url, this.username,
                         this.password);
                 break;
             case H2:
                 Class.forName("org.h2.Driver");
                 this.con = DriverManager.getConnection(this.url, this.username,
                         this.password);
                 break;
             }
         } catch (ClassNotFoundException e) {
             e.printStackTrace();
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public void close() {
         if (this.keepAlive && !this.reconnect) {
             if (this.timeout == 0) {
                 return;
             } else if ((System.currentTimeMillis() / 1000) < (this.startup + this.timeout)) {
                 return;
             }
         }
         try {
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
             if (this.keepAlive) {
                 this.connect();
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
     }
 
     public void reconnect() {
         this.reconnect = true;
         this.close();
         this.connect();
     }
 
     private String updateFieldsString(HashMap<String, Object> data) {
         String query = " SET", suffix = ",";
         int i = 1;
         Iterator<Entry<String, Object>> it = data.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry<String, Object> pairs = it.next();
             if (i == data.size()) {
                 suffix = "";
             }
            query += " `" + pairs.getKey() + "` =  '" + pairs.getValue() + "'"
                    + suffix;
             i++;
         }
         return query;
     }
 
     private String insertFieldString(HashMap<String, Object> data) {
         String fields = "", values = "", query = "", suffix = ",";
         int i = 1;
         Iterator<Entry<String, Object>> it = data.entrySet().iterator();
         while (it.hasNext()) {
             Map.Entry<String, Object> pairs = it.next();
             if (i == data.size()) {
                 suffix = "";
             }
             fields += " `" + pairs.getKey() + "`" + suffix;
             values += " '" + pairs.getValue() + "'" + suffix;
             i++;
         }
         query = "(" + fields + ") VALUES (" + values + ")";
         return query;
     }
 }
