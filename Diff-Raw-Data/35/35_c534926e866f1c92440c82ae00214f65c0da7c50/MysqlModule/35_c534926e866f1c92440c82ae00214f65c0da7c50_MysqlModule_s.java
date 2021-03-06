 /*
  * Copyright (c) 1998-2006 Caucho Technology -- all rights reserved
  *
  * This file is part of Resin(R) Open Source
  *
  * Each copy or derived work must preserve the copyright notice and this
  * notice unmodified.
  *
  * Resin Open Source is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2 of the License, or
  * (at your option) any later version.
  *
  * Resin Open Source is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, or any warranty
  * of NON-INFRINGEMENT.  See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Resin Open Source; if not, write to the
  *
  *   Free Software Foundation, Inc.
  *   59 Temple Place, Suite 330
  *   Boston, MA 02111-1307  USA
  *
  * @author Scott Ferguson
  */
 
 package com.caucho.quercus.lib.db;
 
 import java.sql.Statement;
 import java.sql.SQLException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.caucho.util.L10N;
 import com.caucho.util.Log;
 
 import com.caucho.quercus.env.*;
 import com.caucho.quercus.module.AbstractQuercusModule;
 import com.caucho.quercus.module.Optional;
 import com.caucho.quercus.module.NotNull;
 import com.caucho.quercus.module.ReturnNullAsFalse;
 
 
 /**
  * PHP mysql routines.
  */
 public class MysqlModule extends AbstractQuercusModule {
 
   private static final Logger log = Log.open(MysqlModule.class);
   private static final L10N L = new L10N(MysqlModule.class);
 
   public static final int MYSQL_ASSOC = 0x1;
   public static final int MYSQL_NUM = 0x2;
   public static final int MYSQL_BOTH = 0x3;
 
   public static final int MYSQL_USE_RESULT = 0x0;
   public static final int MYSQL_STORE_RESULT = 0x1;
 
   public MysqlModule()
   {
   }
 
   /**
    * Returns true for the mysql extension.
    */
   public String []getLoadedExtensions()
   {
     return new String[] { "mysql" };
   }
 
   /**
    * Returns the number of affected rows.
    */
   public int mysql_affected_rows(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.affected_rows();
   }
 
   /**
    * Returns the client encoding
    */
   public String mysql_client_encoding(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.client_encoding();
   }
 
   /**
    * Closes a mysql connection.
    */
   public boolean mysql_close(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     if (conn != null) {
       if (conn == getConnection(env))
         env.removeSpecialValue("caucho.mysql");
 
       conn.close(env);
 
       return true;
     }
     else
       return false;
   }
 
   /**
    * Creates a database.
    */
   public boolean mysql_create_db(Env env, @NotNull String name, @Optional Mysqli conn)
   {
     if (name == null)
       return false;
 
     if (conn == null)
       conn = getConnection(env);
 
     Statement stmt = null;
 
     // XXX: move implementation
     try {
       try {
         stmt = conn.validateConnection().getConnection().createStatement();
         stmt.setEscapeProcessing(false);
         stmt.executeUpdate("CREATE DATABASE " + name);
       } finally {
         if (stmt != null)
           stmt.close();
       }
     } catch (SQLException e) {
       log.log(Level.FINE, e.toString(), e);
       return false;
     }
 
     return true;
   }
 
   /**
    * Moves the intenal row pointer of the MySQL result to the
    * specified row number, 0 based.
    */
   public boolean mysql_data_seek(Env env,
                                  @NotNull MysqliResult result,
                                  int rowNumber)
   {
     if (result == null)
       return false;
 
     if (result.seek(env, rowNumber)) {
       return true;
     } else {
       env.warning(L.l("Offset {0} is invalid for MySQL (or the query data is unbuffered)",
                       rowNumber));
       return false;
     }
   }
 
   /**
    * Retrieves the database name after a call to mysql_list_dbs()
    */
   public Value mysql_db_name(Env env,
                              @NotNull MysqliResult result,
                              int row,
                              @Optional("0") Value field)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     return mysql_result(env, result, row, field);
   }
 
   /**
    * Returns the value of one field in the result set.
    */
   public Value mysql_result(Env env,
                             @NotNull MysqliResult result,
                             int row,
                             @Optional("0") Value field)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     return result.getResultField(env, row, field);
   }
 
   /**
    * Drops a database.
    */
   public boolean mysql_drop_db(Env env,
                                @NotNull String databaseName,
                                @Optional Mysqli conn)
   {
     if (databaseName == null)
       return false;
 
     Value value = mysql_query(env, "DROP DATABASE " + databaseName, conn);
 
     return (value != null && value.toBoolean());
   }
 
   /**
    * Returns the error number of the most recent error
    */
   public int mysql_errno(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.errno();
   }
 
   /**
    * Returns the most recent error.
    */
   public String mysql_error(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     String error = conn.error();
 
     if (error == null)
       return "";
 
     return error;
   }
 
   public Value mysql_escape_string(Env env, StringValue unescapedString)
   {
     return mysql_real_escape_string(env, unescapedString, null);
   }
 
   /**
    * Escapes special characters.
    *
    * @see String QuercusMysqliModule.mysqli_real_escape_string(JdbcConnectionResource, String)
    *
    * @return the escaped string
    */
 
   public Value mysql_real_escape_string(Env env,
                                         StringValue unescapedString,
                                         @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.real_escape_string(unescapedString);
   }
 
   /**
    * Returns a row from the connection
    */
   public Value mysql_fetch_array(Env env,
                                  @NotNull MysqliResult result,
                                  @Optional("MYSQL_BOTH") int type)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     Value value = result.fetch_array(env, type);
 
     if (value != null)
       return value;
     else
       return BooleanValue.FALSE;
   }
 
   /**
    * Returns a row from the connection
    */
   @ReturnNullAsFalse
   public ArrayValue mysql_fetch_assoc(Env env, @NotNull MysqliResult result)
   {
     if (result == null)
       return null;
 
     return result.fetch_array(env, MYSQL_ASSOC);
   }
 
   /**
    * Returns an object containing field information.
    * On success, this method increments the field offset
    * (see {@link #mysql_field_seek}).
    *
    * The properties are:
    * <ul>
    * <li> blob
    * <li> def
    * <li> max_length
    * <li> multiple_key
    * <li> name
    * <li> not_null
    * <li> numeric
    * <li> primary_key
    * <li> table
    * <li> type
    * <li> unique key.
    * <li> unsigned
    * <li> zerofill.
    * </ul>
    *
    */
   public Value mysql_fetch_field(Env env,
                                  @NotNull MysqliResult result,
                                  @Optional("-1") int fieldOffset)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     // XXX: move implementation
     try {
       if (fieldOffset == -1)
         fieldOffset = result.field_tell(env);
 
       Value fieldTable = result.getFieldTable(env, fieldOffset);
       Value fieldName = result.getFieldName(env, fieldOffset);
       Value fieldType = result.getFieldType(env, fieldOffset);
       Value fieldLength = result.getFieldLength(env, fieldOffset);
       Value fieldCatalog = result.getFieldCatalog(fieldOffset);
 
       if ((fieldTable == BooleanValue.FALSE)
           || (fieldName == BooleanValue.FALSE)
           || (fieldType == BooleanValue.FALSE)
           || (fieldLength == BooleanValue.FALSE)
           || (fieldCatalog == BooleanValue.FALSE)) {
         return BooleanValue.FALSE;
       }
 
       result.field_seek(env, fieldOffset + 1);
 
       JdbcConnectionResource conn = getConnection(env).validateConnection();
 
       JdbcTableMetaData tableMd = conn.getTableMetaData(fieldCatalog.toString(),
                                                         null,
                                                         fieldTable.toString());
 
       if (tableMd == null)
         return BooleanValue.FALSE;
 
       JdbcColumnMetaData columnMd = tableMd.getColumn(fieldName.toString());
 
       if (columnMd == null)
         return BooleanValue.FALSE;
 
       // XXX: remove cast when value cleaned up
       ObjectValue fieldResult = (ObjectValue) env.createObject();
 
       fieldResult.putField("name", columnMd.getName());
       fieldResult.putField("table", tableMd.getName());
       fieldResult.putField("def", "");
       fieldResult.putField("max_length", fieldLength.toInt());
 
       fieldResult.putField("not_null", columnMd.isNotNull() ? 1 : 0);
 
       fieldResult.putField("primary_key", columnMd.isPrimaryKey() ? 1 : 0);
       fieldResult.putField("multiple_key", columnMd.isIndex() && ! columnMd.isPrimaryKey() ? 1 : 0);
       fieldResult.putField("unique_key", columnMd.isUnique() ? 1 : 0);
 
       fieldResult.putField("numeric", columnMd.isNumeric() ? 1 : 0);
       fieldResult.putField("blob", columnMd.isBlob() ? 1 : 0);
 
       fieldResult.putField("type", fieldType.toString());
 
       fieldResult.putField("unsigned", columnMd.isUnsigned() ? 1 : 0);
       fieldResult.putField("zerofill", columnMd.isZeroFill() ? 1 : 0);
 
       return fieldResult;
     } catch (SQLException e) {
       log.log(Level.FINE, e.toString(), e);
       return BooleanValue.FALSE;
     }
   }
 
   /**
    * Executes a query and returns a result set.
    *
    * Returns true on update success, false on failure, and a result set
    * for a successful select
    */
   public Value mysql_query(Env env, String sql, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.query(env, sql, MYSQL_STORE_RESULT);
   }
 
   /**
    * Returns an array of lengths.
    */
   public Value mysql_fetch_lengths(Env env, @NotNull MysqliResult result)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     return result.fetch_lengths();
   }
 
   /**
    * Returns an object with properties that correspond to the fetched row
    * and moves the data pointer ahead.
    */
   public Value mysql_fetch_object(Env env, @NotNull MysqliResult result)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     return result.fetch_object(env);
   }
 
   /**
    * Returns a row from the connection
    */
   @ReturnNullAsFalse
   public ArrayValue mysql_fetch_row(Env env, @NotNull MysqliResult result)
   {
     if (result == null)
       return null;
 
     return result.fetch_row(env);
   }
 
   /**
    * Returns the field flags of the specified field.  The flags are reported as
    * a space separated list of words, the returned value can be split using explode().
    *
    * The following flages are reported, older version of MySQL may not report all flags:
    * <ul>
    * <li> not_null
    * <li> primary_key
    * <li> multiple_key
    * <li> blob
    * <li> unsigned
    * <li> zerofill
    * <li> binary
    * <li> enum
    * <li> auto_increment
    * <li> timestamp
    * </ul>
    */
   public Value mysql_field_flags(Env env,
                                  @NotNull MysqliResult result,
                                  int fieldOffset)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     Value fieldName = result.getFieldName(env, fieldOffset);
 
     if (fieldName == BooleanValue.FALSE)
       return BooleanValue.FALSE;
 
     Value fieldTable = result.getFieldTable(env, fieldOffset);
 
     if (fieldTable == BooleanValue.FALSE)
       return BooleanValue.FALSE;
 
     String sql = "SHOW FULL COLUMNS FROM " + fieldTable.toString() + " LIKE \'" + fieldName.toString() + "\'";
 
     Mysqli conn = getConnection(env);
 
     Object metaResult = conn.validateConnection().realQuery(sql);
 
     if (metaResult instanceof MysqliResult)
       return ((MysqliResult) metaResult).getFieldFlags();
 
     return BooleanValue.FALSE;
   }
 
   /**
    * Returns field name at given offset.
    */
   public Value mysql_field_name(Env env,
                                 @NotNull MysqliResult result,
                                 int fieldOffset)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     return result.getFieldName(env, fieldOffset);
   }
 
   /**
    * Seeks to the specified field offset, the field offset is
    * is used as the default for the next call to {@link #mysql_fetch_field}.
    */
   public boolean mysql_field_seek(Env env,
                                   @NotNull MysqliResult result,
                                   int fieldOffset)
   {
     if (result == null)
       return false;
 
     return result.field_seek(env, fieldOffset);
   }
 
   /**
    * Returns the table corresponding to the field.
    */
   public Value mysql_field_table(Env env,
                                  @NotNull MysqliResult result,
                                  int fieldOffset)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     return result.getFieldTable(env, fieldOffset);
   }
 
   /**
    * Returns the field type.
    */
   public static Value mysql_field_type(Env env,
                                        @NotNull MysqliResult result,
                                        Value fieldOffset)
   {
     if (result == null) {
       return NullValue.NULL;
     }
 
     if (! fieldOffset.isset())
       return NullValue.NULL;
 
     return result.getFieldType(env, fieldOffset.toInt());
   }
 
   /**
    * Deprecated alias for mysql_field_len.
    */
   public static Value mysql_fieldlen(Env env,
                                      @NotNull MysqliResult result,
                                      int fieldOffset)
   {
     return mysql_field_len(env, result, fieldOffset);
   }
 
   /**
    * Returns the length of the specified field
    */
   public static Value mysql_field_len(Env env,
                                       @NotNull MysqliResult result,
                                       int fieldOffset)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     // ERRATUM: Returns 10 for datatypes DEC and NUMERIC instead of 11
 
     return result.getFieldLength(env, fieldOffset);
   }
 
   /**
    * Frees a mysql result.
    */
   public boolean mysql_free_result(MysqliResult result)
   {
     if (result != null)
       result.close();
 
     return true;
   }
 
   /**
    * Returns the MySQL client version.
    */
   public String mysql_get_client_info(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.get_client_info();
   }
 
   /**
    * Returns a string describing the host.
    */
   public String mysql_get_host_info(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.get_host_info();
   }
 
   /**
    * Returns an integer respresenting the MySQL protocol
    * version.
    */
   public int mysql_get_proto_info(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.get_proto_info();
   }
 
   /**
    * Returns the MySQL server version.
    */
   public String mysql_get_server_info(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.get_server_info();
   }
 
   /**
    * returns ID generated for an AUTO_INCREMENT column by the previous
    * INSERT query on success, 0 if the previous query does not generate
    * an AUTO_INCREMENT value, or FALSE if no MySQL connection was established
    */
   public Value mysql_insert_id(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.insert_id();
   }
 
   /**
    * Returns a result pointer containing the databases available from the current mysql daemon.
    */
   public JdbcResultResource mysql_list_dbs(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.list_dbs();
   }
 
   /**
    * Retrieves information about the given table name
    */
   public Value mysql_list_fields(Env env,
                                  String databaseName,
                                  String tableName,
                                  @Optional Mysqli conn)
   {
     if (databaseName == null)
       return BooleanValue.FALSE;
 
     if (tableName == null)
       return BooleanValue.FALSE;
 
     return mysql_db_query(env,
                           databaseName,
                           "SHOW COLUMNS FROM " + tableName,
                           conn);
   }
 
   /**
    * Returns result set or false on error
    */
   public Value mysql_db_query(Env env,
                               String databaseName,
                               String query,
                               @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     if (! conn.select_db(databaseName))
       return BooleanValue.FALSE;
 
     return conn.query(env, query, 1);
   }
 
   /**
    * Selects the database
    */
   public boolean mysql_select_db(Env env,
                                  String name,
                                  @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.select_db(name);
   }
 
   /**
    * Retrieves a list of table names from a MySQL database.
    */
   public Object mysql_list_tables(Env env,
                                   String databaseName,
                                   @Optional Mysqli conn)
   {
     return mysql_query(env, "SHOW TABLES FROM " + databaseName, conn);
   }
 
   public int mysql_num_fields(Env env, @NotNull MysqliResult result)
   {
     if (result == null)
       return -1;
 
     return result.num_fields();
   }
 
   /**
    * Retrieves the number of rows in a result set.
    */
   public Value mysql_num_rows(Env env, @NotNull MysqliResult result)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     return result.num_rows();
   }
 
   /**
    * Returns a new mysql connection.
    */
   public Value mysql_pconnect(Env env,
                               @Optional String server,
                               @Optional String user,
                               @Optional String password,
                               @Optional Value newLinkV,
                               @Optional Value flagsV)
   {
     return mysql_connect(env, server, user, password, newLinkV, flagsV);
   }
 
   /**
    * Returns a new mysql connection.
    */
   public Value mysql_connect(Env env,
                              @Optional String host,
                              @Optional String userName,
                              @Optional String password,
                              @Optional Value newLinkV,
                              @Optional Value flagsV)
   {
     Mysqli mysqli = new Mysqli(env, host, userName, password, "", 3306, "", 0, null, null);
 
     Value value = env.wrapJava(mysqli);
 
     env.setSpecialValue("caucho.mysql", mysqli);
 
     return value;
   }
 
   /**
    * Checks if the connection is still valid.
    */
   public boolean mysql_ping(Env env, @Optional Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     return conn.ping();
   }
 
   /**
    * Returns a string with the status of the connection
    * or NULL if error.
    */
   public Value mysql_stat(Env env, Mysqli conn)
   {
     if (conn == null)
       conn = getConnection(env);
 
     Value result = conn.stat(env);
 
     return result == BooleanValue.FALSE ? NullValue.NULL : result;
   }
 
   /**
    * Retrieves the table name corresponding to a field, using
    * a result return by {@link #mysql_list_tables}.
    */
   public Value mysql_tablename(Env env,
                                @NotNull MysqliResult result,
                                int i)
   {
     if (result == null)
       return BooleanValue.FALSE;
 
     return result.getResultField(env, i, LongValue.ZERO);
   }
 
   /**
    * Queries the database.
    */
   public Object mysql_unbuffered_query(Env env,
                                        @NotNull String name,
                                        @Optional Mysqli conn)
   {
     return mysql_query(env, name, conn);
   }
 
   //@todo mysql_change_user()
   //@todo mysql_info()
   //@todo mysql_list_processes()
   //@todo mysql_thread_id()
 
   private Mysqli getConnection(Env env)
   {
     Mysqli conn = (Mysqli) env.getSpecialValue("caucho.mysql");
 
     if (conn != null)
       return conn;
 
     conn = new Mysqli(env, "localhost", "", "", "", 3306, "", 0, null, null);
 
     env.setSpecialValue("caucho.mysql", conn);
 
     return conn;
   }
 }
 
