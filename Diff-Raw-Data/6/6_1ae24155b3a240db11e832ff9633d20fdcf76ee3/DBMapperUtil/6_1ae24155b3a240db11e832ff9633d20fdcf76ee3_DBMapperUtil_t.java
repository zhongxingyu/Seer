 package de.consistec.syncframework.common.util;
 
 import static de.consistec.syncframework.common.MdTableDefaultValues.FLAG_COLUMN_NAME;
 import static de.consistec.syncframework.common.MdTableDefaultValues.MDV_COLUMN_NAME;
 import static de.consistec.syncframework.common.MdTableDefaultValues.METADATA_COLUMN_COUNT;
 import static de.consistec.syncframework.common.MdTableDefaultValues.PK_COLUMN_NAME;
 import static de.consistec.syncframework.common.MdTableDefaultValues.REV_COLUMN_NAME;
 import static de.consistec.syncframework.common.util.CollectionsUtil.newHashMap;
 
 import de.consistec.syncframework.common.data.MDEntry;
 
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.util.Map;
 
 /**
  * @author marcel
  * @company Consistec Engineering and Consulting GmbH
  * @date 09.01.13 11:05
  */
 public final class DBMapperUtil {
 
     private DBMapperUtil() {
     }
 
     /**
      * Maps the resultSet to an MDEntry object.
      *
      * @param resultSet the result set to map
      * @param tableName the name of the table which is represented through the MDEntry.
      * @return MDEntry object with the values of the result set.
      * @throws SQLException
      */
     public static MDEntry getMetadata(ResultSet resultSet, String tableName) throws SQLException {
         ResultSetMetaData meta = resultSet.getMetaData();
 
         MDEntry tmpEntry = new MDEntry();
         tmpEntry.setTableName(tableName);
 
         String columnName;
 
         for (int i = 1; i <= METADATA_COLUMN_COUNT; i++) {
             columnName = meta.getColumnName(i);
 
             if (REV_COLUMN_NAME.equalsIgnoreCase(columnName)) {
                 tmpEntry.setRevision(resultSet.getInt(i));
             } else if (PK_COLUMN_NAME.equalsIgnoreCase(columnName)) {
                 tmpEntry.setPrimaryKey(resultSet.getObject(i));
             } else if (MDV_COLUMN_NAME.equalsIgnoreCase(columnName)) {
                 tmpEntry.setMdv(resultSet.getString(i));
             } else if (FLAG_COLUMN_NAME.equalsIgnoreCase(columnName)) {
                 // do nothing, we don't want to sync the f flag
                 continue;
             }
         }
         return tmpEntry;
     }
 
     /**
      * Maps the result set to a map.
      *
      * @param resultSet the result set to map.
      * @return a map which key is the column name and value the value in the result set in the given column.
      * @throws SQLException
      */
     public static Map<String, Object> getRowData(ResultSet resultSet) throws SQLException {
         Map<String, Object> rowData = newHashMap();
         ResultSetMetaData meta = resultSet.getMetaData();
 
         int columnCount = meta.getColumnCount();
 
         for (int i = METADATA_COLUMN_COUNT + 1; i <= columnCount; i++) {
             rowData.put(meta.getColumnName(i), resultSet.getObject(i));
         }
         return rowData;
     }
 
     /**
      * Checks if at least one value in the data row is not null.
      *
      * @param dataRow the data row
      * @return true if it still exists
      */
     public static boolean dataRowExists(Map<String, Object> dataRow) {
         for (Object value : dataRow.values()) {
             if (value != null) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Checks if the current row is already marked as deleted, i.e the hash value in the metadata is null or empty.
      * <p/>
     *
      * @param deletedRows result set positioned on the current row
      * @return true if the row is already deleted
      * @throws SQLException
      */
     public static boolean rowIsAlreadyDeleted(ResultSet deletedRows) throws SQLException {
        return "".equals(deletedRows.getString(MDV_COLUMN_NAME)) || deletedRows.getString(MDV_COLUMN_NAME) == null;
     }
 
     /**
      * Compares the current hash value in the metadata with the given hash, to assert if a row data has changed or not.
      * <p/>
     *
      * @param rows the result set
      * @param hash the calculated hash
      * @return true in case of equality
      * @throws SQLException
      */
     public static boolean rowHasSameHash(ResultSet rows, String hash) throws SQLException {
         String mdTableHash = rows.getString(MDV_COLUMN_NAME);
         return mdTableHash != null && mdTableHash.equalsIgnoreCase(hash);
     }
 }
