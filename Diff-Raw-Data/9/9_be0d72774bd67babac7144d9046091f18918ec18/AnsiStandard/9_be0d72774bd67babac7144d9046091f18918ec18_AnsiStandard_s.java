 /*
  * $Source$
  * $Revision$
  *
  * Copyright (C) 2000 David Warnock
  * 
  * Part of Melati (http://melati.org), a framework for the rapid
  * development of clean, maintainable web applications.
  *
  * Melati is free software; Permission is granted to copy, distribute
  * and/or modify this software under the terms either:
  *
  * a) the GNU General Public License as published by the Free Software
  *    Foundation; either version 2 of the License, or (at your option)
  *    any later version,
  *
  *    or
  *
  * b) any version of the Melati Software License, as published
  *    at http://melati.org
  *
  * You should have received a copy of the GNU General Public License and
  * the Melati Software License along with this program;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA to obtain the
  * GNU General Public License and visit http://melati.org to obtain the
  * Melati Software License.
  *
  * Feel free to contact the Developers of Melati (http://melati.org),
  * if you would like to work out a different arrangement than the options
  * outlined here.  It is our intention to allow Melati to be used by as
  * wide an audience as possible.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * Contact details for copyright holder:
  *
  *     David Warnock (david At sundayta.co.uk)
  *     Sundayta Ltd
  *     International House, 
  *     174 Three Bridges Road, 
  *     Crawley, 
  *     West Sussex RH10 1LE, UK
  *
  */
 
 package org.melati.poem.dbms;
 
 import java.math.BigDecimal;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.Date;
 import java.sql.Driver;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Properties;
 
 import org.melati.poem.BigDecimalPoemType;
 import org.melati.poem.BinaryPoemType;
 import org.melati.poem.BooleanPoemType;
 import org.melati.poem.Column;
 import org.melati.poem.DatePoemType;
 import org.melati.poem.DoublePoemType;
 import org.melati.poem.ExecutingSQLPoemException;
 import org.melati.poem.IntegerPoemType;
 import org.melati.poem.IntegrityFixPoemType;
 import org.melati.poem.LongPoemType;
 import org.melati.poem.PasswordPoemType;
 import org.melati.poem.PoemBugPoemException;
 import org.melati.poem.PoemType;
 import org.melati.poem.SQLPoemException;
 import org.melati.poem.SQLPoemType;
 import org.melati.poem.SQLType;
 import org.melati.poem.StandardIntegrityFix;
 import org.melati.poem.StringPoemType;
 import org.melati.poem.Table;
 import org.melati.poem.TimestampPoemType;
 import org.melati.poem.UnexpectedExceptionPoemException;
 import org.melati.poem.util.StringUtils;
 
 /**
  * An SQL 92 compliant Database Management System. 
  * <p>
  * Should there ever be such a
  * thing then you wouldn't need to extend this, but all DBs used with Melati so
  * far have needed to extend the standard with their own variations.
  */
 public class AnsiStandard implements Dbms {
   
   private boolean driverLoaded = false;
   private String driverClassName = null;
   private Driver driver = null;
   protected String schema;
 
   protected synchronized void setDriverClassName(String name) {
     driverClassName = name;
   }
   protected synchronized String getDriverClassName() {
     return driverClassName;
   }
 
   protected synchronized void setDriverLoaded(boolean loaded) {
     driverLoaded = loaded;
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#unloadDriver()
    */
   public void unloadDriver() {
     driver = null;
     setDriverLoaded(false);
   }
   
   protected synchronized boolean getDriverLoaded() {
     return driverLoaded;
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getSchema()
    */
   public String getSchema() {
     return null;
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#shutdown(java.sql.Connection)
    */
   public void shutdown(Connection connection)  
     throws SQLException{    
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#canDropColumns()
    */
   public boolean canDropColumns(){
     return false;
   }
 
   protected synchronized void loadDriver() {
     Class driverClass;
     try {
       driverClass = Class.forName(getDriverClassName());
       setDriverLoaded(true);
     } catch (java.lang.ClassNotFoundException e) {
       // A call to Class.forName() forces us to consider this exception :-)...
       setDriverLoaded(false);
       return;
     }
 
     try {
       driver = (Driver) driverClass.newInstance();
     } catch (java.lang.Exception e) {
       // ... otherwise, "something went wrong" and I don't here care what
       // or have the wherewithal to do anything about it :)
       throw new UnexpectedExceptionPoemException(e);
     }
   }
 
   /**
    * The default windows installation of MySQL has autocommit set true, 
    * which throws an SQLException when one issues a commit.
    * 
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getConnection(java.lang.String, java.lang.String, java.lang.String)
    */
   public Connection getConnection(String url, String user, String password)
       throws ConnectionFailurePoemException {
     schema = user;
     try { 
       synchronized (driverClassName) {
         if (!getDriverLoaded()) {
           if (getDriverClassName() == null)
             throw new SQLException(
                 "No Driver Classname set in dbms specific class");
           loadDriver();
         }
       }
       if (!getDriverLoaded()) {
         throw new SQLException(
             "The Driver class " + getDriverClassName() + " failed to load");
       }
 
       Connection c = null;
       if (driver != null) {
         Properties info = new Properties();
         if (user != null)
           info.put("user", user);
         if (password != null)
           info.put("password", password);
 
         c = driver.connect(url, info);
         if (c == null) 
           throw new SQLException(
                     "Null connection from driver using url: " + 
                       url + 
                       " user: " + 
                       user + 
                       " password: " + password);
       } else { 
         c = DriverManager.getConnection(url, user, password);
         if (c == null) 
           throw new SQLException(
                     "Null connection from DriverManager using url: " + 
                     url + 
                     " user: " + 
                     user + 
                     " password: " + password);
       }
       if (c.getAutoCommit())
         c.setAutoCommit(false);
         //c.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED); 
       return c;
     } catch (Exception e ) { 
       throw new ConnectionFailurePoemException(e);
     }
   }
   
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#preparedStatementPlaceholder(org.melati.poem.PoemType)
    */
   public String preparedStatementPlaceholder(PoemType type) {
     return "?";
   }
   
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#createTableSql()
    */
   public String createTableSql() {
     return "CREATE TABLE ";
   }
   
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#createTableOptionsSql()
    */
   public String createTableOptionsSql() {
     return "";
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getSqlDefinition(java.lang.String)
    */
   public String getSqlDefinition(String sqlTypeName) {
     return sqlTypeName;
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getStringSqlDefinition(int)
    */
   public String getStringSqlDefinition(int size) throws SQLException {
     if (size < 0)
       throw new SQLException(
           "unlimited length not supported in AnsiStandard STRINGs");
 
     return "VARCHAR(" + size + ")";
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getLongSqlDefinition()
    */
   public String getLongSqlDefinition() {
     return "INT8";
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getBinarySqlDefinition(int)
    */
   public String getBinarySqlDefinition(int size) throws SQLException {
     if (size < 0)
       throw new SQLException(
           "unlimited length not supported in AnsiStandard BINARYs");
 
     return "LONGVARBINARY(" + size + ")";
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getFixedPtSqlDefinition(int, int)
    */
   public String getFixedPtSqlDefinition(int scale, int precision)
       throws SQLException {
    if (scale < 0 || precision <= 0)
       throw new SQLException(
          "negative scale or nonpositive precision not supported " + 
           "in AnsiStandard DECIMALs");
 
     return "DECIMAL(" + precision + "," + scale + ")";
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#sqlBooleanValueOfRaw(java.lang.Object)
    */
   public String sqlBooleanValueOfRaw(Object raw) {
     return raw.toString();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#canRepresent(org.melati.poem.PoemType, org.melati.poem.PoemType)
    */
   public PoemType canRepresent(PoemType storage, PoemType type) {
     return storage.canRepresent(type);
   }
 
   private SQLPoemType unsupported(String sqlTypeName, ResultSet md)
       throws UnsupportedTypePoemException {
     UnsupportedTypePoemException e;
     try {
       e = new UnsupportedTypePoemException(md.getString("TABLE_NAME"), md
           .getString("COLUMN_NAME"), md.getShort("DATA_TYPE"), sqlTypeName, md
           .getString("TYPE_NAME"));
     } catch (SQLException ee) {
       throw new UnsupportedTypePoemException(sqlTypeName);
     }
 
     throw e;
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#defaultPoemTypeOfColumnMetaData(java.sql.ResultSet)
    */
   public SQLPoemType defaultPoemTypeOfColumnMetaData(ResultSet md)
       throws SQLException {
     int typeCode = md.getShort("DATA_TYPE");
     boolean nullable = md.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
     int width = md.getInt("COLUMN_SIZE");
     int scale = md.getInt("DECIMAL_DIGITS");
 
     switch (typeCode) {
       case Types.BIT :
         return new BooleanPoemType(nullable);
       case Types.TINYINT :
         return unsupported("TINYINT", md);
       case Types.SMALLINT :
         return unsupported("SMALLINT", md);
       case Types.INTEGER :
         return new IntegerPoemType(nullable);
       case Types.BIGINT :
         return new LongPoemType(nullable);
 
       case Types.FLOAT :
         return unsupported("FLOAT", md);
       case Types.REAL :
         return new DoublePoemType(nullable);
       case Types.DOUBLE :
         return new DoublePoemType(nullable);
 
       case Types.NUMERIC :
         return new BigDecimalPoemType(nullable, width, scale);
       case Types.DECIMAL :
         return new BigDecimalPoemType(nullable, width, scale);
 
       case Types.CHAR :
         return unsupported("CHAR", md);
       case Types.VARCHAR :
         return new StringPoemType(nullable, width);
       case Types.LONGVARCHAR :
         return new StringPoemType(nullable, width);
 
       case Types.DATE :
         return new DatePoemType(nullable);
       case Types.TIME :
         return unsupported("TIME", md);
       case Types.TIMESTAMP :
         return new TimestampPoemType(nullable);
 
       case Types.BINARY :
         return unsupported("BINARY", md);
       case Types.VARBINARY :
         return new BinaryPoemType(nullable, width);
       case Types.LONGVARBINARY :
         return new BinaryPoemType(nullable, width);
 
       case Types.NULL :
         return unsupported("NULL", md);
 
       case Types.OTHER :
         return unsupported("OTHER", md);
 
 
         // Following introduced since 1.1
       case Types.JAVA_OBJECT : 
         return unsupported("JAVA_OBJECT", md);
       case Types.DISTINCT : 
         return unsupported("DISTINCT", md);
       case Types.STRUCT : 
         return unsupported("STRUCT", md);
       case Types.ARRAY : 
         return unsupported("ARRAY", md);
       case Types.BLOB : 
         return unsupported("BLOB", md);
       case Types.CLOB : 
         return unsupported("CLOB", md);
       case Types.REF : 
         return unsupported("REF", md);
       case Types.DATALINK : 
         return unsupported("DATLINK", md);
 
       case Types.BOOLEAN : 
         return new BooleanPoemType(nullable);
       default :
         return unsupported("<code not in Types.java!>", md);
     }
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#exceptionForUpdate
    */
   public SQLPoemException exceptionForUpdate(Table table, String sql,
       boolean insert, SQLException e) {
     return new ExecutingSQLPoemException(sql, e);
   }
 
   /**
    * {@inheritDoc}
    * @see Dbms#exceptionForUpdate(org.melati.poem.Table, 
    *                              java.sql.PreparedStatement, 
    *                              boolean, java.sql.SQLException)
    */
   public SQLPoemException exceptionForUpdate(Table table, PreparedStatement ps,
       boolean insert, SQLException e) {
     return exceptionForUpdate(table, ps == null ? null : ps.toString(), insert,
         e);
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getQuotedName(java.lang.String)
    */
   public String getQuotedName(String name) {
     StringBuffer b = new StringBuffer();
     StringUtils.appendQuoted(b, unreservedName(name), '"');
     return b.toString();
   }
   
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getQuotedValue(org.melati.poem.SQLType, java.lang.Object)
    */
   public String getQuotedValue(SQLType sqlType, String value) {
     if (sqlType instanceof BooleanPoemType) {
       return value;
     }
     if (sqlType instanceof DoublePoemType) {
       return value;
     }
     if (sqlType instanceof LongPoemType) {
       return value;
     }
     if (sqlType instanceof BinaryPoemType) {
       return StringUtils.quoted(value,'\'');
     }
     if (sqlType instanceof BigDecimalPoemType) {
       return value;
          }
     if (sqlType instanceof DatePoemType) {
       return StringUtils.quoted(value,'\'');
     }
     if (sqlType instanceof TimestampPoemType) {
       return StringUtils.quoted(value,'\'');
     }
     if (sqlType instanceof PasswordPoemType) {
       return StringUtils.quoted(value,'\'');
     }
     if (sqlType instanceof StringPoemType) {
       return StringUtils.quoted(value,'\'');
     }
     if (sqlType instanceof IntegrityFixPoemType) {
       return value;
     }
     if (sqlType instanceof IntegerPoemType) {
       return value;
     }
     throw new PoemBugPoemException("Unrecognised sqlType: " + sqlType);
     
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getJdbcMetadataName(java.lang.String)
    */
   public String getJdbcMetadataName(String name) {
     return name;
   }
 
   /**
    * A pair of functions for getting around keywords which make your 
    * JDBC driver barf, as 'group' does for MySQL.
    * 
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#unreservedName(java.lang.String)
    * @see org.melati.poem.dbms.MySQL#unreservedName
    * @see org.melati.poem.dbms.MySQL#melatiName
    */
   public String unreservedName(String name) {
     return name;
   }
   
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#melatiName(java.lang.String)
    */
   public String melatiName(String name) {
     return name;
   }
 
   /**
    * MySQL requires a length argument when creating an index on a BLOB or TEXT
    * column.
    * 
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getIndexLength(org.melati.poem.Column)
    * @see org.melati.poem.dbms.MySQL#getIndexLength
    */
   public String getIndexLength(Column column) {
     return "";
   }
 
   /**
    * MSSQL cannot index a TEXT column. But neither can it compare them so we
    * don't use it, we use VARCHAR(255).
    *  
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#canBeIndexed(org.melati.poem.Column)
    */
   public boolean canBeIndexed(Column column) {
     return true;
   }
 
   /**
    * MySQL had no EXISTS keyword, from 4.1 onwards it does.
    * NOTE There is a bootstrap problem here, we need to use the 
    * unchecked troid, otherwise we get a stack overflow.
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#givesCapabilitySQL
    * @see org.melati.poem.dbms.MySQL#givesCapabilitySQL
    */
   public String givesCapabilitySQL(Integer userTroid, String capabilityExpr) {
     return "SELECT * FROM " + getQuotedName("groupmembership") + " WHERE "
         + getQuotedName("user") + " = " + userTroid + " AND "
         + "EXISTS ( " + "SELECT " + getQuotedName("groupcapability") + "."
         + getQuotedName("group") + " FROM "
         + getQuotedName("groupcapability") + " WHERE "
         + getQuotedName("groupcapability") + "." + getQuotedName("group")
         + " = " + getQuotedName("groupmembership") + "."
         + getQuotedName("group") + " AND " + getQuotedName("capability")
         + " = " + capabilityExpr + ")";
   }
 
   /**
    * This is the Postgresql syntax.
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#caseInsensitiveRegExpSQL(String, String)
    */
   public String caseInsensitiveRegExpSQL(String term1, String term2) {
     if (StringUtils.isQuoted(term2)) {
       term2 = term2.substring(1, term2.length() - 1);
     } 
     term2 = StringUtils.quoted(StringUtils.quoted(term2, '%'), '\'');
     
     return term1 + " ILIKE " + term2;
   }
 
   /**
    * {@inheritDoc}
    * @see java.lang.Object#toString()
    */
   public String toString() {
     return this.getClass().getName();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getForeignKeyDefinition
    */
   public String getForeignKeyDefinition(String tableName, String fieldName, 
       String targetTableName, String targetTableFieldName, String fixName) {
     StringBuffer sb = new StringBuffer();
     sb.append(" ADD FOREIGN KEY (" + getQuotedName(fieldName) + ") REFERENCES " + 
               getQuotedName(targetTableName) + 
               "(" + getQuotedName(targetTableFieldName) + ")");
     if (fixName.equals("prevent"))
       sb.append(" ON DELETE RESTRICT");
     if (fixName.equals("delete"))
       sb.append(" ON DELETE CASCADE");      
     if (fixName.equals("clear"))
       sb.append(" ON DELETE SET NULL");      
     return sb.toString();
   }
 
   /**
    * Return the PRIMARY KEY definition string for this dbms. 
    * 
    * @param fieldName the table Troid column, often id, unquoted
    * @return The definition string
    * @see org.melati.poem.dbms.AnsiStandard#getPrimaryKeyDefinition(java.lang.String)
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getPrimaryKeyDefinition(java.lang.String)
    */
   public String getPrimaryKeyDefinition(String fieldName) {
     return " ADD PRIMARY KEY (" + getQuotedName(fieldName) + ")";
   }
   
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#
    *      alterColumnNotNullableSQL(java.lang.String, java.lang.String)
    */
   public String alterColumnNotNullableSQL(String tableName, Column column) {
     return "ALTER TABLE " + getQuotedName(tableName) +
     " ALTER COLUMN " + getQuotedName(column.getName()) +
     " SET NOT NULL";
   }
   
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#selectLimit(java.lang.String, int)
    */
   public String selectLimit(String querySelection, int limit) {
     return "SELECT " + querySelection + " LIMIT " + limit;
   }
   
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#booleanTrueExpression(org.melati.poem.Column)
    */
   public String booleanTrueExpression(Column booleanColumn) {
     return booleanColumn.fullQuotedName();
   }
 
   /**
    * {@inheritDoc}
    * @see org.melati.poem.dbms.Dbms#getSqlDefaultValue(java.lang.String)
    */
   public String getSqlDefaultValue(SQLType sqlType) {
     if (sqlType instanceof BooleanPoemType) {
       return ("false");
     }
     if (sqlType instanceof DoublePoemType) {
       return ("0.0");
     }
     if (sqlType instanceof LongPoemType) {
       return ("0");
     }
     if (sqlType instanceof BinaryPoemType) {
       return "";
     }
     if (sqlType instanceof BigDecimalPoemType) {
       return new BigDecimal(0.0).toString();
     }
     if (sqlType instanceof DatePoemType) {
       return new Date(new java.util.Date().getTime()).toString();
     }
     if (sqlType instanceof TimestampPoemType) {
       return new Timestamp(System.currentTimeMillis()).toString();
     }
     if (sqlType instanceof PasswordPoemType) {
       return "FIXME";
     }
     if (sqlType instanceof StringPoemType) {
       return "default";
     }
     //Set prevent as default fix
     if (sqlType instanceof IntegrityFixPoemType) {
       return StandardIntegrityFix.prevent.getIndex().toString();
     }
 
     // Defaults to User for ColumnPoemType
     // Primary for SearchabilityPoemType
     // This needs to be last, as types above extend IntegerPoemType
     if (sqlType instanceof IntegerPoemType) {
       return ("0");
     }
     throw new PoemBugPoemException("Unrecognised sqlType: " + sqlType);
 
   }
 
 }
 
