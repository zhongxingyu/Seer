 /*
  * This file is part of SQLDatabaseAPI (2012).
  *
  * SQLDatabaseAPI is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * SQLDatabaseAPI is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with SQLDatabaseAPI.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Last modified: 29.12.12 16:50
  */
 
 package com.p000ison.dev.sqlapi.jbdc;
 
 import com.p000ison.dev.sqlapi.*;
 import com.p000ison.dev.sqlapi.exception.DatabaseConnectionException;
 import com.p000ison.dev.sqlapi.exception.QueryException;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Represents a JBDCDatabase
  */
 public abstract class JBDCDatabase extends Database {
     /**
      * The connection to the database
      */
     private Connection connection;
 
     public JBDCDatabase(DatabaseConfiguration configuration) throws DatabaseConnectionException
     {
         super(configuration);
 
         connection = connect(configuration);
     }
 
     protected abstract Connection connect(DatabaseConfiguration configuration) throws DatabaseConnectionException;
 
     @Override
     public void closeDatabaseConnection() throws QueryException
     {
         try {
             getConnection().close();
         } catch (SQLException e) {
             throw new QueryException(e);
         }
     }
 
     @Override
     public List<String> getDatabaseColumns(String table)
     {
         List<String> columns = new ArrayList<String>();
 
         try {
             ResultSet columnResult = getMetadata().getColumns(null, null, table, null);
 
 
             while (columnResult.next()) {
                 columns.add(columnResult.getString("COLUMN_NAME"));
             }
         } catch (SQLException e) {
             throw new QueryException(e);
         }
 
         return columns;
     }
 
     private DatabaseMetaData getMetadata()
     {
         try {
             return getConnection().getMetaData();
         } catch (SQLException e) {
             throw new QueryException(e);
         }
     }
 
     @Override
     public boolean existsDatabaseTable(String table)
     {
         ResultSet columnResult;
         try {
             columnResult = this.getMetadata().getTables(null, null, null, null);
 
             while (columnResult.next()) {
                 if (table.equals(columnResult.getString("TABLE_NAME"))) {
                     return true;
                 }
             }
 
         } catch (SQLException e) {
             throw new QueryException(e);
         }
 
         return false;
     }
 
     protected final Connection getConnection()
     {
         return connection;
     }
 
     @Override
     public boolean executeDirectUpdate(String query)
     {
         if (query == null) {
             return false;
         }
         Statement statement = null;
         try {
             statement = getConnection().createStatement();
             return statement.executeUpdate(query) != 0;
         } catch (SQLException e) {
             throw new QueryException(e);
         } finally {
             handleClose(statement, null);
         }
     }
 
     @Override
     public boolean isConnected()
     {
         try {
             return getConnection() != null && !getConnection().isClosed();
         } catch (SQLException e) {
             throw new QueryException(e);
         }
     }
 
     public PreparedStatement prepare(String query)
     {
         try {
             return getConnection().prepareStatement(query);
         } catch (SQLException e) {
             throw new QueryException(e);
         }
     }
 
     @Override
     public JBDCPreparedQuery createPreparedStatement(String query)
     {
         return new JBDCPreparedQuery(this, query);
     }
 
     @Override
    public boolean existsEntry(RegisteredTable table, TableObject object)
     {
         Column column = table.getIDColumn();
 
         PreparedStatement check = null;
         ResultSet result = null;
         try {
             check = getConnection().prepareStatement(String.format("SELECT %s FROM %s WHERE %s=%s;", column.getName(), table.getName(), column.getName(), column.getValue(object)));
 
             result = check.executeQuery();
             return result.next();
         } catch (SQLException e) {
             throw new QueryException(e);
         } finally {
             handleClose(check, result);
         }
     }
 
     @Override
    public boolean existsEntry(TableObject object)
     {
         return this.existsEntry(getRegisteredTable(object.getClass()), object);
     }
 
     @Override
     protected int getLastEntryId(RegisteredTable table)
     {
         Column idColumn = table.getIDColumn();
         PreparedStatement check = null;
         ResultSet result = null;
         try {
             check = getConnection().prepareStatement(String.format("SELECT %s FROM %s ORDER BY %s DESC LIMIT 1;", idColumn.getName(), table.getName(), idColumn.getName()));
             result = check.executeQuery();
             if (!result.next()) {
                 return 1;
             }
             int lastId = result.getInt(idColumn.getName());
             result.close();
             check.close();
             return lastId;
         } catch (SQLException e) {
             throw new QueryException(e);
         } finally {
             handleClose(check, result);
         }
     }
 
     public static void handleClose(Statement check, ResultSet result)
     {
         try {
             if (check != null) {
                 check.close();
             }
             if (result != null) {
                 result.close();
             }
         } catch (SQLException e) {
             throw new QueryException(e);
         }
     }
 
     @Override
     public boolean isSupported(Class<?> type)
     {
         return isSupportedByDatabase(type);
     }
 
     static boolean isSupportedByDatabase(Class<?> type)
     {
         return type.isPrimitive() || Number.class.isAssignableFrom(type)
                 || type == boolean.class || type == Boolean.class
                 || type == char.class || type == Character.class
                 || type == Date.class || type == Timestamp.class
                 || type == String.class;
     }
 
     static int getDatabaseDataType(Class<?> type)
     {
         if (type == boolean.class || type == Boolean.class) {
             return Types.TINYINT;
         } else if (type == byte.class || type == Byte.class) {
             return Types.TINYINT;
         } else if (type == short.class || type == Short.class) {
             return Types.SMALLINT;
         } else if (type == int.class || type == Integer.class) {
             return Types.INTEGER;
         } else if (type == float.class || type == Float.class) {
             return Types.FLOAT;
         } else if (type == double.class || type == Double.class) {
             return Types.DOUBLE;
         } else if (type == long.class || type == Long.class) {
             return Types.INTEGER;
         } else if (type == char.class || type == Character.class) {
             return Types.CHAR;
         } else if (type == String.class) {
             return Types.VARCHAR;
         } else if (type == java.util.Date.class || type == java.sql.Timestamp.class) {
             return Types.TIMESTAMP;
         } else if (RegisteredTable.isSerializable(type)) {
             return Types.BLOB;
         }
 
         return UNSUPPORTED_TYPE;
     }
 
     static Object getDatabaseFromResultSet(int index, ResultSet set, Class<?> type)
     {
         try {
             if (type == boolean.class || type == Boolean.class) {
                 return set.getBoolean(index);
             } else if (type == byte.class || type == Byte.class) {
                 return set.getByte(index);
             } else if (type == short.class || type == Short.class) {
                 return set.getShort(index);
             } else if (type == int.class || type == Integer.class) {
                 return set.getInt(index);
             } else if (type == float.class || type == Float.class) {
                 return set.getFloat(index);
             } else if (type == double.class || type == Double.class) {
                 return set.getDouble(index);
             } else if (type == long.class || type == Long.class) {
                 return set.getLong(index);
             } else if (type == char.class || type == Character.class) {
                 return (char) set.getInt(index);
             } else if (type == String.class) {
                 return set.getString(index);
             } else if (type == java.util.Date.class || type == java.sql.Timestamp.class) {
                 return set.getTimestamp(index);
             } else if (RegisteredTable.isSerializable(type)) {
                 return set.getBlob(index);
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
 
         return null;
     }
 
     @Override
     protected <T extends TableObject> JBDCPreparedSelectQuery<T> createPreparedSelectQuery(String query, RegisteredTable table)
     {
         return new JBDCPreparedSelectQuery<T>(this, query, table);
     }
 
     public ResultSet query(String query)
     {
         Statement statement;
         try {
             statement = getConnection().createStatement();
             return statement.executeQuery(query);
         } catch (SQLException e) {
             throw new QueryException(e);
         }
     }
 }
