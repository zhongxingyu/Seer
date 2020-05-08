 package com.github.ddth.plommon.bo;
 
 import java.text.MessageFormat;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.StringUtils;
 
 /**
  * MySQL-specific DAO.
  * 
  * @author Thanh Nguyen <btnguyen2k@gmail.com>
  * @since 0.3.2
  */
 public class BaseMysqlDao extends BaseDao {
 
     /**
      * Executes a DELETE IGNORE statement (without WHERE clause).
      * 
      * @param tableName
      * @return number of deleted rows
      */
     protected static int deleteIgnore(String tableName) {
         return deleteEx("IGNORE", tableName, null, null);
     }
 
     /**
      * Executes a DELETE IGNORE statement.
      * 
      * @param tableName
      * @param whereColumns
      *            supply {@code null} to ignore WHERE clause
      * @param whereValues
      *            supply {@code null} to ignore WHERE clause
      * @return number deleted rows
      */
     protected static int deleteIgnore(String tableName, String[] whereColumns, Object[] whereValues) {
         return deleteEx("IGNORE", tableName, whereColumns, whereValues);
     }
 
     /**
      * Executes a DELETE LOW_PRIORITY statement (without WHERE clause).
      * 
      * @param tableName
      * @return number of deleted rows
      */
     protected static int deleteLowPriority(String tableName) {
         return deleteEx("LOW_PRIORITY", tableName, null, null);
     }
 
     /**
      * Executes a DELETE LOW_PRIORITY statement.
      * 
      * @param tableName
      * @param whereColumns
      *            supply {@code null} to ignore WHERE clause
      * @param whereValues
      *            supply {@code null} to ignore WHERE clause
      * @return number of deleted rows
      */
     protected static int deleteLowPriority(String tableName, String[] whereColumns,
             Object[] whereValues) {
         return deleteEx("LOW_PRIORITY", tableName, whereColumns, whereValues);
     }
 
     /**
      * Executes a DELETE QUICK statement (without WHERE clause).
      * 
      * @param tableName
      * @return number of deleted rows
      */
     protected static int deleteQuick(String tableName) {
         return deleteEx("QUICK", tableName, null, null);
     }
 
     /**
      * Executes a DELETE QUICK statement.
      * 
      * @param tableName
      * @param whereColumns
      *            supply {@code null} to ignore WHERE clause
      * @param whereValues
      *            supply {@code null} to ignore WHERE clause
      * @return number of deleted rows
      */
     protected static int deleteQuick(String tableName, String[] whereColumns, Object[] whereValues) {
         return deleteEx("QUICK", tableName, whereColumns, whereValues);
     }
 
    /*
     * @since 0.4.1 add support of {@link ParamExpression}
     */
     private static int deleteEx(String ex, String tableName, String[] whereColumns,
             Object[] whereValues) {
         final String SQL_TEMPLATE_FULL_IGNORE = "DELETE IGNORE FROM {0} WHERE {1}";
         final String SQL_TEMPLATE_IGNORE = "DELETE IGNORE FROM {0}";
         final String SQL_TEMPLATE_FULL_LOW_PRIORITY = "DELETE LOW_PRIORITY FROM {0} WHERE {1}";
         final String SQL_TEMPLATE_LOW_PRIORITY = "DELETE LOW_PRIORITY FROM {0}";
         final String SQL_TEMPLATE_FULL_QUICK = "DELETE QUICK FROM {0} WHERE {1}";
         final String SQL_TEMPLATE_QUICK = "DELETE QUICK FROM {0}";
 
         final String SQL_TEMPLATE_FULL = "IGNORE".equalsIgnoreCase(ex) ? SQL_TEMPLATE_FULL_IGNORE
                 : ("QUICK".equalsIgnoreCase(ex) ? SQL_TEMPLATE_FULL_QUICK
                         : SQL_TEMPLATE_FULL_LOW_PRIORITY);
         final String SQL_TEMPLATE = "IGNORE".equalsIgnoreCase(ex) ? SQL_TEMPLATE_IGNORE : ("QUICK"
                 .equalsIgnoreCase(ex) ? SQL_TEMPLATE_QUICK : SQL_TEMPLATE_LOW_PRIORITY);
 
         final List<String> WHERE_CLAUSE = new ArrayList<String>();
         if (whereColumns != null && whereColumns.length > 0 && whereValues != null
                 && whereValues.length > 0) {
            if (whereColumns.length != whereValues.length) {
                throw new IllegalArgumentException(
                        "Number of whereColumns must be equal to number of whereValues.");
            }
             for (int i = 0; i < whereColumns.length; i++) {
                if (whereValues[i] instanceof ParamExpression) {
                    WHERE_CLAUSE.add("(" + whereColumns[i] + "="
                            + ((ParamExpression) whereValues[i]).getExpression() + ")");
                } else {
                    WHERE_CLAUSE.add("(" + whereColumns[i] + "=?)");
                }
             }
         }
 
         String SQL;
         if (WHERE_CLAUSE.size() > 0) {
             SQL = MessageFormat.format(SQL_TEMPLATE_FULL, tableName,
                     StringUtils.join(WHERE_CLAUSE, " AND "));
         } else {
             SQL = MessageFormat.format(SQL_TEMPLATE, tableName);
         }
         return delete(SQL, whereValues);
     }
 
     /**
      * Executes an INSERT DELAYED statement.
      * 
      * @param tableName
      * @param columnNames
      * @param values
      * @return number of inserted rows
      */
     protected static int insertDelayed(String tableName, String[] columnNames, Object[] values) {
         return insertEx("DELAYED", tableName, columnNames, values);
     }
 
     /**
      * Executes an INSERT IGNORE statement.
      * 
      * @param tableName
      * @param columnNames
      * @param values
      * @return number of inserted rows
      */
     protected static int insertIgnore(String tableName, String[] columnNames, Object[] values) {
         return insertEx("IGNORE", tableName, columnNames, values);
     }
 
     /**
      * Executes an INSERT LOW_PRIORITY statement.
      * 
      * @param tableName
      * @param columnNames
      * @param values
      * @return number of inserted rows
      */
     protected static int insertLowPriority(String tableName, String[] columnNames, Object[] values) {
         return insertEx("LOW_PRIORITY", tableName, columnNames, values);
     }
 
     private static int insertEx(String ex, String tableName, String[] columnNames, Object[] values) {
        if (columnNames.length != values.length) {
            throw new IllegalArgumentException(
                    "Number of columns must be equal to number of values.");
        }
         final String SQL_TEMPLATE_DELAYED = "INSERT DELAYED INTO {0} ({1}) VALUES ({2})";
         final String SQL_TEMPLATE_IGNORE = "INSERT IGNORE INTO {0} ({1}) VALUES ({2})";
         final String SQL_TEMPLATE_LOW_PRIORITY = "INSERT LOW_PRIORITY INTO {0} ({1}) VALUES ({2})";
 
         final String SQL_TEMPLATE = "DELAYED".equalsIgnoreCase(ex) ? SQL_TEMPLATE_DELAYED
                 : ("IGNORE".equalsIgnoreCase(ex) ? SQL_TEMPLATE_IGNORE : SQL_TEMPLATE_LOW_PRIORITY);
         final String SQL_PART_COLUMNS = StringUtils.join(columnNames, ',');
        final StringBuilder SQL_PART_VALUES = new StringBuilder();
        for (int i = 0; i < values.length; i++) {
            if (values[i] instanceof ParamExpression) {
                SQL_PART_VALUES.append(((ParamExpression) values[i]).getExpression());
            } else {
                SQL_PART_VALUES.append('?');
            }
            if (i < values.length - 1) {
                SQL_PART_VALUES.append(',');
            }
        }
         final String SQL = MessageFormat.format(SQL_TEMPLATE, tableName, SQL_PART_COLUMNS,
                 SQL_PART_VALUES);
         return insert(SQL, values);
     }
 
     /**
      * Executes a UPDATE IGNORE statement (without WHERE clause).
      * 
      * @param tableName
      * @param columnNames
      * @param values
      * @return number affected rows
      */
     protected static int updateIgnore(String tableName, String[] columnNames, Object[] values) {
         return updateEx("IGNORE", tableName, columnNames, values, null, null);
     }
 
     /**
      * Executes a UPDATE IGNORE statement.
      * 
      * @param tableName
      * @param columnNames
      * @param values
      * @param whereColumns
      *            supply {@code null} to ignore WHERE clause
      * @param whereValues
      *            supply {@code null} to ignore WHERE clause
      * @return number affected rows
      */
     protected static int updateIgnore(String tableName, String[] columnNames, Object[] values,
             String[] whereColumns, Object[] whereValues) {
         return updateEx("IGNORE", tableName, columnNames, values, whereColumns, whereValues);
     }
 
     /**
      * Executes a UPDATE LOW_PRIORITY statement (without WHERE clause).
      * 
      * @param tableName
      * @param columnNames
      * @param values
      * @return number affected rows
      */
     protected static int updateLowPriority(String tableName, String[] columnNames, Object[] values) {
         return updateEx("LOW_PRIORITY", tableName, columnNames, values, null, null);
     }
 
     /**
      * Executes a UPDATE LOW_PRIORITY statement.
      * 
      * @param tableName
      * @param columnNames
      * @param values
      * @param whereColumns
      *            supply {@code null} to ignore WHERE clause
      * @param whereValues
      *            supply {@code null} to ignore WHERE clause
      * @return number affected rows
      */
     protected static int updateLowPriority(String tableName, String[] columnNames, Object[] values,
             String[] whereColumns, Object[] whereValues) {
         return updateEx("LOW_PRIORITY", tableName, columnNames, values, whereColumns, whereValues);
     }
 
     private static int updateEx(String ex, String tableName, String[] columnNames, Object[] values,
             String[] whereColumns, Object[] whereValues) {
         final String SQL_TEMPLATE_FULL_IGNORE = "UPDATE IGNORE {0} SET {1} WHERE {2}";
         final String SQL_TEMPLATE_IGNORE = "UPDATE IGNORE {0} SET {1}";
         final String SQL_TEMPLATE_FULL_LOW_PRIORITY = "UPDATE LOW_PRIORITY {0} SET {1} WHERE {2}";
         final String SQL_TEMPLATE_LOW_PRIORITY = "UPDATE LOW_PRIORITY {0} SET {1}";
 
         final String SQL_TEMPLATE_FULL = "IGNORE".equalsIgnoreCase(ex) ? SQL_TEMPLATE_FULL_IGNORE
                 : SQL_TEMPLATE_FULL_LOW_PRIORITY;
         final String SQL_TEMPLATE = "IGNORE".equalsIgnoreCase(ex) ? SQL_TEMPLATE_IGNORE
                 : SQL_TEMPLATE_LOW_PRIORITY;
 
         final List<String> UPDATE_CLAUSE = new ArrayList<String>();
         for (int i = 0; i < columnNames.length; i++) {
             UPDATE_CLAUSE.add(columnNames[i] + "=?");
         }
 
         final List<String> WHERE_CLAUSE = new ArrayList<String>();
         if (whereColumns != null && whereColumns.length > 0 && whereValues != null
                 && whereValues.length > 0) {
             for (int i = 0; i < whereColumns.length; i++) {
                 WHERE_CLAUSE.add("(" + whereColumns[i] + "=?)");
             }
         }
 
         String SQL;
         Object[] PARAM_VALUES;
         if (whereColumns != null && whereColumns.length > 0 && whereValues != null
                 && whereValues.length > 0) {
             SQL = MessageFormat.format(SQL_TEMPLATE_FULL, tableName,
                     StringUtils.join(UPDATE_CLAUSE, ','), StringUtils.join(WHERE_CLAUSE, " AND "));
             PARAM_VALUES = ArrayUtils.addAll(values, whereValues);
         } else {
             SQL = MessageFormat.format(SQL_TEMPLATE, tableName,
                     StringUtils.join(UPDATE_CLAUSE, ','));
             PARAM_VALUES = values;
         }
         return update(SQL, PARAM_VALUES);
     }
 
     public static void main(String... args) {
         insert("asm_platform", new String[] { "a", "b", "c" }, new Object[] { 1, "2", 3.0 });
     }
 }
