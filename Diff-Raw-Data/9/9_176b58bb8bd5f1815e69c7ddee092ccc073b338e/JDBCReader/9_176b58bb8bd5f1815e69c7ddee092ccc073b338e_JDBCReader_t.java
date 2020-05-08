 /*
     jBilling - The Enterprise Open Source Billing System
     Copyright (C) 2003-2009 Enterprise jBilling Software Ltd. and Emiliano Conde
 
     This file is part of jbilling.
 
     jbilling is free software: you can redistribute it and/or modify
     it under the terms of the GNU Affero General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     jbilling is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU Affero General Public License for more details.
 
     You should have received a copy of the GNU Affero General Public License
     along with jbilling.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.sapienter.jbilling.server.mediation.task;
 
 import com.sapienter.jbilling.common.Constants;
 import com.sapienter.jbilling.common.SessionInternalError;
 import com.sapienter.jbilling.common.Util;
 import com.sapienter.jbilling.server.item.PricingField;
 import com.sapienter.jbilling.server.mediation.Record;
 import com.sapienter.jbilling.server.util.PreferenceBL;
 import org.apache.log4j.Logger;
 import org.springframework.dao.EmptyResultDataAccessException;
 import org.springframework.jdbc.core.JdbcTemplate;
 import org.springframework.jdbc.datasource.DriverManagerDataSource;
 import org.springframework.jdbc.support.rowset.SqlRowSet;
 import org.springframework.jdbc.support.rowset.SqlRowSetMetaData;
 
 import java.sql.*;
 import java.util.*;
 import java.util.Date;
 
 /**
  * JDBCReader allows reading event records from a database for
  * mediation. By default it reads all field from a table 'cdr'. There
  * are two methods for marking which tables have been read: 'Last id'
  * and 'timestamp'. 'Last id' reads all records greater than the last
  * id that was read during a previous run (stored in preference
  * MEDIATION_JDBC_READER_LAST_ID). 'Timestamp' is used if a column
  * 'jbilling_timestamp' exists (or the 'timestamp_column_name'
  * parameter is set). In this method, the records with 'timestamp ==
  * null' are read and then timestamped. The meothd  also allows
  * composite  primary keys.
  */
 public class JDBCReader extends AbstractReader implements IMediationReader {
 
     private static final Logger LOG = Logger.getLogger(JDBCReader.class);
 
     // plug-in parameters
     protected static final String PARAM_DATABASE_NAME = "database_name";
     protected static final String PARAM_TABLE_NAME = "table_name";
     protected static final String PARAM_KEY_COLUMN_NAME = "key_column_name";
     protected static final String PARAM_WHERE_APPEND = "where_append";
     protected static final String PARAM_ORDER_BY = "order_by";
     protected static final String PARAM_DRIVER = "driver";
     protected static final String PARAM_URL = "url";
     protected static final String PARAM_USERNAME = "username";
     protected static final String PARAM_PASSWORD = "password";
     protected static final String PARAM_TIMESTAMP_COLUMN_NAME = "timestamp_column_name";
     protected static final String PARAM_LOWERCASE_COLUMN_NAME = "lc_column_names";
 
     // defaults
     protected static final String DATABASE_NAME_DEFAULT = "jbilling_cdr";
     protected static final String TABLE_NAME_DEFAULT = "cdr";
     protected static final String KEY_COLUMN_NAME_DEFAULT = "id";
     protected static final String DRIVER_DEFAULT = "org.hsqldb.jdbcDriver";
     protected static final String USERNAME_DEFAULT = "SA";
     protected static final String PASSWORD_DEFAULT = "";
     protected static final String TIMESTAMP_COLUMN_DEFAULT = "jbilling_timestamp";
     protected static final Boolean LOWERCASE_COLUMN_NAME_DEFAULT = true;
 
     protected enum MarkMethod { LAST_ID, TIMESTAMP }
 
     protected Connection connection = null;
     protected JdbcTemplate jdbcTemplate = null;
     protected String[] keyColumnNames = null;
     protected String timestampColName = null;
     protected MarkMethod markMethod = null;
 
     private int maxId = 0; // last record that was processed
     private String primaryKeySQL = null; // cache of record key SQL
 
     public JDBCReader() {
 
     }
 
     public boolean validate(List<String> messages) {
         super.validate(messages);
         try {
             DriverManagerDataSource dataSource = new DriverManagerDataSource();
             dataSource.setDriverClassName(getDriver());
             dataSource.setUrl(getUrl());
             dataSource.setUsername(getParameter(PARAM_USERNAME, USERNAME_DEFAULT));
             dataSource.setPassword(getParameter(PARAM_PASSWORD, PASSWORD_DEFAULT));
 
             jdbcTemplate = new JdbcTemplate(dataSource);
             jdbcTemplate.setMaxRows(getBatchSize());
             connection = dataSource.getConnection();
 
             keyColumnNames = getKeyColumnNames();
             timestampColName = getTimestampColumnName();
             if (timestampColName != null) {
                 LOG.debug("Using timestamp record marking method.");
                 markMethod = MarkMethod.TIMESTAMP;
             } else {
                 LOG.debug("Using last id record marking method.");
                 markMethod = MarkMethod.LAST_ID;
             }
         } catch (SQLException sqle) {
             throw new SessionInternalError(sqle);
         }
 
         // get the maximum id last processed
         PreferenceBL preferenceBL = new PreferenceBL();
         try {
             preferenceBL.set(getEntityId(),
                              Constants.PREFERENCE_MEDIATION_JDBC_READER_LAST_ID);
         } catch (EmptyResultDataAccessException fe) {
             // use default
         }
         maxId = preferenceBL.getInt();
 
         return true;
     }
 
     public Iterator<List<Record>> iterator() {
         try {
             return new Reader(jdbcTemplate);
         } catch (Exception e) {
             throw new SessionInternalError(e);
         }
     }
 
     /**
      * Returns the database name, either from a plug-in parameter or
      * the default.
      */
     protected String getDatabaseName() {
         return getParameter(PARAM_DATABASE_NAME, DATABASE_NAME_DEFAULT);
     }
 
     /**
      * Returns the table name, either from a plug-in parameter
      * or the default.
      */
     protected String getTableName() {
         return getParameter(PARAM_TABLE_NAME, TABLE_NAME_DEFAULT);
     }
 
     /**
      * Returns the driver string, either from a plug-in parameter or
      * the default.
      */
     protected String getDriver() {
         return getParameter(PARAM_DRIVER, DRIVER_DEFAULT);
     }
 
     /**
      * Returns the url string, either from a plug-in parameter or
      * a default.
      */
     protected String getUrl() {
         String url = (String) parameters.get(PARAM_URL);
         if (url != null) {
             return url;
         }
 
         // construct a default hsqldb url for a database in the 
         // 'jbilling/resources/mediation' directory.
         return "jdbc:hsqldb:" + Util.getSysProp("base_dir") + "mediation/" + getDatabaseName() + ";shutdown=true";
     }
 
     /**
      * Returns the default query string that selects all fields from
      * the table. For the 'last id' method, it selects all records
      * with the primary key greater than a value stored in the
      * MEDIATION_JDBC_READER_LAST_ID preference. For the 'timestamp'
      * method, all records are read where the timestamp column is
      * null. Also allows the WHERE clause to be appended with a value
      * from a plug-in parameter. By default, the records are ordered
      * by the primary key/s, but can be overrided by a plug-in
      * parameter. This method can be overrided to provide a specific
      * query to be executed that returns records to be processed.
      */
     protected String getQueryString() throws SQLException {
         String query = "SELECT * FROM " + getTableName() + " WHERE ";
 
         if (markMethod == MarkMethod.LAST_ID) {
             if (keyColumnNames.length > 1) {
                 throw new SessionInternalError("Only one key column is " +
                         "allowed when using the last id record marking method");
             }
 
             query += keyColumnNames[0] + " > " + maxId;
         } else {
             query += timestampColName + " IS NULL ";
         }
 
         String whereAppend = (String) parameters.get(PARAM_WHERE_APPEND);
         if (whereAppend != null) {
             query += " " + whereAppend + " ";
         }
 
         query += " ORDER BY ";
         String orderBy = (String) parameters.get(PARAM_ORDER_BY);
         if (orderBy != null) {
             query += orderBy;
         } else {
             query += keyColumnNames[0];
             for (int i = 1; i < keyColumnNames.length; i++) {
                 query += ", " + keyColumnNames[1];
             }
         }
 
         LOG.debug("Query string: " + query);
 
         return query;
     }
 
     /**
      * Returns the key columns names. This is either a single column
      * from a plug-in parameter, otherwise taken from the database
      * metadata. Failing that, it defaults to 'id'.
      */
     protected String[] getKeyColumnNames() throws SQLException {
         // try getting key column name from plug-in parameter
         String keyColumnName = (String) parameters.get(PARAM_KEY_COLUMN_NAME);
         if (keyColumnName != null) {
             return new String[]{keyColumnName};
         }
 
         // none, so try from metadata
 
         // first, try to get the table name in the correct case (needed for
         // hsqldb driver, which is case-sensitive for metadata info)
         String tableName = getTableNameCorrectCase();
 
         // now get the primary key from metadata
         ResultSet keys = connection.getMetaData().getPrimaryKeys(null, null, tableName);
         List<String> names = new LinkedList<String>();
         while (keys.next()) {
             names.add(keys.getString("COLUMN_NAME"));
         }
 
         if (!names.isEmpty()) {
             return names.toArray(new String[names.size()]);
         } else {
             // could not determine from metadata, default to 'id'
             LOG.warn("No primary key found, default to '" + KEY_COLUMN_NAME_DEFAULT + "'");
             return new String[]{KEY_COLUMN_NAME_DEFAULT};
         }
     }
 
     /**
      * Returns the timestamp column name, either from a plug-in
      * parameter or the default (if it exists). If none are found,
      * returns null (or throws an exception if parameter was used).
      */
     protected String getTimestampColumnName() throws SQLException {
         // try to get the table name in the correct case (needed for
         // hsqldb driver, which is case-sensitive for metadata info)
         String tableName = getTableNameCorrectCase();
 
         // try getting timestamp column name from plug-in parameter
         String timestampColumnName = (String) parameters.get(PARAM_TIMESTAMP_COLUMN_NAME);
         if (timestampColumnName != null) {
             // get the columns from metadata
             ResultSet columns = connection.getMetaData().getColumns(null, null, tableName, null);
             while (columns.next()) {
                 String colName = columns.getString("COLUMN_NAME");
                 if (colName.equalsIgnoreCase(timestampColumnName)) {
                     return colName;
                 }
             }
             throw new SessionInternalError("Couldn't find jbilling timestamp " + "column: " + timestampColumnName);
         }
 
         // see if default column name is there
         // get the columns from metadata
         ResultSet columns = connection.getMetaData().getColumns(null, null,
                 tableName, null);
         while (columns.next()) {
             String colName = columns.getString("COLUMN_NAME");
             if (colName.equalsIgnoreCase(TIMESTAMP_COLUMN_DEFAULT)) {
                 return colName;
             }
         }
 
         return null;
     }
 
     /**
      * Gets called with a record just read (but before it is
      * processed), as well as an array of the key fields. Can be
      * overrided to provide a means of marking records that have been
      * read. By default, when using the 'timestamp' mark method, sets
      * a timestamp for the record.
      */
     protected void recordRead(Record record, int[] keyColumnIndices) throws SQLException {
         if (markMethod == MarkMethod.TIMESTAMP) {
             List<PricingField> fields = record.getFields();
 
             // contruct primary key SQL if not yet done
             if (primaryKeySQL == null) {
                 primaryKeySQL = fields.get(keyColumnIndices[0]).getName() + " = ? ";
                 for (int i = 1; i < keyColumnIndices.length; i++) {
                     int index = keyColumnIndices[i];
                     primaryKeySQL += " AND " + fields.get(keyColumnIndices[i]).getName() + " = ? ";
                 }
             }
 
             String sql = "UPDATE " + getTableName() + " SET " + timestampColName + " = ? WHERE " + primaryKeySQL;
             PreparedStatement ps = connection.prepareStatement(sql);
 
             ps.setTimestamp(1, new Timestamp(new Date().getTime()));
 
             for (int i = 0; i < keyColumnIndices.length; i++) {
                 PricingField field = fields.get(keyColumnIndices[i]);
                 switch (field.getType()) {
                     case STRING:
                         ps.setString(i + 2, field.getStrValue());
                         break;
                     case INTEGER:
                         ps.setInt(i + 2, field.getIntValue());
                         break;
                     case DECIMAL:
                         ps.setDouble(i + 2, field.getDoubleValue());
                         break;
                     case DATE:
                         ps.setTimestamp(i + 2, new Timestamp(field.getDateValue().getTime()));
                         break;
                     case BOOLEAN:
                         ps.setBoolean(i + 2, field.getBooleanValue());
                         break;
                 }
             }
 
             ps.executeUpdate();
         }
 
         if (markMethod == MarkMethod.LAST_ID) {
             maxId = record.getFields().get(keyColumnIndices[0]).getIntValue();
         }
     }
 
     /**
      * Gets called after the last record is processed. Can be
      * overrided to be used for clean-up. By default, just saves the
      * last record id processed in the MEDIATION_JDBC_READER_LAST_ID
      * preference when the 'last id' mark method is used..
      */
     protected void batchProcessingComplete() {
         if (markMethod == MarkMethod.LAST_ID) {
 
             // no records processed
             if (maxId == 0) {
                 return;
             }
 
             // set the maximum id last processed
             LOG.debug("Updating last id preference: " + maxId);
             PreferenceBL preferenceBL = new PreferenceBL();
             preferenceBL.createUpdateForEntity(getEntityId(),
                                                Constants.PREFERENCE_MEDIATION_JDBC_READER_LAST_ID,
                                                maxId,
                                                null,
                                                null);
         }
     }
 
     /**
      * The Record iterator class.
      */
     public class Reader implements Iterator<List<Record>> {
         private JdbcTemplate jdbcTemplate;
         private boolean isResultSetClosed = false;
         private PricingField.Type[] columnTypes;
         private String[] columnNames;
         private int[] keyColumnIndices;
         private List<Record> recordList;
 
         protected Reader(JdbcTemplate jdbcTemplate) {
             this.jdbcTemplate = jdbcTemplate;
             recordList = new ArrayList<Record>(getBatchSize());
         }
 
         public boolean hasNext() {
             try {
                 recordList.clear();
                 recordList = getNextBatch();
                 return recordList.size() > 0;
             } catch (SQLException sqle) {
                 throw new SessionInternalError(sqle);
             }
         }
 
         public List<Record> next() {
             if (recordList.size() == 0) {
                 throw new NoSuchElementException("No more records.");
             } else {
                 return recordList;
             }
         }
 
         private List<Record> getNextBatch() throws SQLException {
             if (isResultSetClosed) return new ArrayList<Record>();
 
             // fetch records
             SqlRowSet dbRecords = jdbcTemplate.queryForRowSet(getQueryString());
 
             // fill metadata info in first time
             if (columnNames == null) {
                 setColumnInfo(dbRecords);
             }
 
             List<Record> resultList = new ArrayList<Record>();
             while (dbRecords.next()) {
                 Record currentRecord = new Record();
                 for (int i = 0; i < columnTypes.length; i++) {
                     switch (columnTypes[i]) {
                         case STRING:
                             currentRecord.addField(new PricingField(columnNames[i],
                                                                     dbRecords.getString(i + 1)), isKeyIndex(i));
                             break;
 
                         case INTEGER:
                             currentRecord.addField(new PricingField(columnNames[i],
                                                                     dbRecords.getInt(i + 1)), isKeyIndex(i));
                             break;
 
                         case DECIMAL:
                             currentRecord.addField(new PricingField(columnNames[i],
                                                                     dbRecords.getBigDecimal(i + 1)), isKeyIndex(i));
                             break;
 
                         case DATE:
                             currentRecord.addField(new PricingField(columnNames[i],
                                                                     dbRecords.getTimestamp(i + 1)), isKeyIndex(i));
                             break;
                         case BOOLEAN:
                             currentRecord.addField(new PricingField(columnNames[i],
                                                                     dbRecords.getBoolean(i + 1)), isKeyIndex(i));
                             break;
                     }
                 }
 
                 // primary key can't be grouped?
                 currentRecord.setPosition(1);
 
                 // current record read
                 recordRead(currentRecord, keyColumnIndices);
 
                 // add created record to results
                 resultList.add(currentRecord);
             }
 
             // mark batch as read
             batchProcessingComplete();
 
             // no more records for read
             if (resultList.isEmpty()) {
                 // close db connection
                 connection.close();
                 isResultSetClosed = true;
             }
             return resultList;
         }
 
         /**
          * Sets the column info (names, types, key) from the database
          * metadata.
          */
         private void setColumnInfo(SqlRowSet records) throws SQLException {
            boolean lowercase = getParameter(PARAM_LOWERCASE_COLUMN_NAME, LOWERCASE_COLUMN_NAME_DEFAULT);
 
             SqlRowSetMetaData metaData = records.getMetaData();
             columnTypes = new PricingField.Type[metaData.getColumnCount()];
             columnNames = new String[metaData.getColumnCount()];
             List<Integer> keyColumns = new LinkedList<Integer>();
 
             for (int i = 0; i < columnTypes.length; i++) {
                 // set column types of the result set
                 switch (metaData.getColumnType(i + 1)) {
                     case Types.CHAR:
                     case Types.LONGNVARCHAR:
                     case Types.LONGVARCHAR:
                     case Types.NCHAR:
                     case Types.NVARCHAR:
                     case Types.VARCHAR:
                         columnTypes[i] = PricingField.Type.STRING;
                         break;
 
                     case Types.BIGINT:
                     case Types.INTEGER:
                     case Types.SMALLINT:
                     case Types.TINYINT:
                         columnTypes[i] = PricingField.Type.INTEGER;
                         break;
 
                     case Types.DECIMAL:
                     case Types.DOUBLE:
                     case Types.FLOAT:
                     case Types.NUMERIC:
                     case Types.REAL:
                         columnTypes[i] = PricingField.Type.DECIMAL;
                         break;
 
                     case Types.DATE:
                     case Types.TIME:
                     case Types.TIMESTAMP:
                         columnTypes[i] = PricingField.Type.DATE;
                         break;
 
                     case Types.BIT:
                     case Types.BOOLEAN:
                         columnTypes[i] = PricingField.Type.BOOLEAN;
                         break;                                                   
 
                     default:
                         throw new SessionInternalError("In column '" +
                                 metaData.getColumnName(i + 1) +
                                 "', unsupported java.sql.Type: " +
                                 metaData.getColumnTypeName(i + 1));
                 }
 
                 // set column names
                 if (lowercase) {
                     columnNames[i] = metaData.getColumnName(i + 1).toLowerCase();
                 } else {
                     columnNames[i] = metaData.getColumnName(i + 1);
                 }
 
                 // check if primary key
                 for (String name : keyColumnNames) {
                     if (columnNames[i].equalsIgnoreCase(name)) {
                         keyColumns.add(i);
                     }
                 }
             }
 
             if (keyColumns.isEmpty()) {
                 throw new SessionInternalError("No primary key column/s found " + "in result set");
             } else {
                 keyColumnIndices = new int[keyColumns.size()];
                 int i = 0;
                 for (Integer index : keyColumns) {
                     keyColumnIndices[i] = index;
                     i++;
                 }
             }
         }
 
         /**
          * Returns if the column at this index is a key column.
          */
         private boolean isKeyIndex(int index) {
             for (int i : keyColumnIndices) {
                 if (i == index) {
                     return true;
                 }
             }
             return false;
         }
 
         public void remove() {
             // needed to comply with Iterator only
             throw new UnsupportedOperationException("remove not supported");
         }
     }
 
     /**
      * Returns table name in correct case, determined from database
      * metadata. (Needed for hsqldb driver,which is case-sensitive
      * for metadata info).
      */
     private String getTableNameCorrectCase() throws SQLException {
         String tableName = getTableName();
         ResultSet tables = connection.getMetaData().getTables(null, null, null, null);
         while (tables.next()) {
             String table = tables.getString(3);
             if (table.equalsIgnoreCase(tableName)) {
                 return table;
             }
         }
         throw new SessionInternalError("Couldn't find table named: " + tableName);
     }
 }
