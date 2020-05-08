 /*
  * AccessProperties.java
  *
  * Created on March 29, 2001, 2:16 AM
 
  * Copyright © 2012 jbundle.org. All rights reserved.
  */
 package org.jbundle.base.db.jdbc.properties;
 
 import java.util.ListResourceBundle;
 
 import org.jbundle.base.db.SQLParams;
 import org.jbundle.base.model.DBConstants;
 import org.jbundle.base.model.DBSQLTypes;
 
 
 /**
  * AccessProperties - SQL specficics for the MS Access database.
  * @author  don
  * @version 
  */
 public class DBProperties_derby extends ListResourceBundle
 {
   public Object[][] getContents() {
       return contents;
   }
   static final Object[][] contents = {
   // LOCALIZE THIS
       {SQLParams.TABLE_NOT_FOUND_ERROR_TEXT, " does not exist"},    // Table not found 42X05"42Y07},
       {SQLParams.AUTO_SEQUENCE_ENABLED, DBConstants.FALSE},
       {SQLParams.NO_NULL_UNIQUE_KEYS, DBConstants.TRUE},	// A null is considered a indexable value and will produce a dup error
       {SQLParams.NO_PREPARED_STATEMENTS_ON_CREATE, DBConstants.TRUE},
       {SQLParams.NO_DUPLICATE_KEY_NAMES, DBConstants.TRUE},
       {SQLParams.MAX_KEY_NAME_LENGTH, "18"},
 //      {SQLParams.BIT_TYPE_SUPPORTED, DBConstants.TRUE},
       {DBSQLTypes.DATETIME, "TIMESTAMP"},
       {DBSQLTypes.BOOLEAN, "CHAR"},
       {DBSQLTypes.MEMO, "VARCHAR(32000)"},
       {DBSQLTypes.OBJECT, "BLOB(32M)"},
       {"INNER_JOIN", ","},
       {"INNER_JOIN_ON", "WHERE"},
 //      {SQLParams.SQL_DATE_FORMAT, "yyyy-MM-dd"},
 //      {SQLParams.SQL_TIME_FORMAT, "HH:mm:ss"},
 //      {SQLParams.SQL_DATETIME_FORMAT, "yyyy-MM-dd HH:mm:ss"},
 //      {SQLParams.SQL_DATE_QUOTE, "\'"},
 //      {SQLParams.CREATE_PRIMARY_INDEX, "alter table {table} add CONSTRAINT {table}_{keyname} PRIMARY KEY({fields})"},
 //      {SQLParams.CREATE_INDEX, ""}, // Blank = Not supported
 //      {SQLParams.ALT_SECONDARY_INDEX, "INDEX_BLIST"}, // Alt method supported (This will speed things up a little).
       {SQLParams.CREATE_DATABASE_SUPPORTED, DBConstants.TRUE},  // Can create databases.
       {SQLParams.RENAME_TABLE_SUPPORT, DBConstants.TRUE},  // Can rename tables.
       {SQLParams.AUTO_COMMIT_PARAM, DBConstants.TRUE},
       {DBConstants.LOAD_INITIAL_DATA, DBConstants.TRUE},  // Load the initial data
 
       {SQLParams.INTERNAL_DB_NAME, "derby"},
       {SQLParams.JDBC_DRIVER_PARAM, "org.apache.derby.jdbc.EmbeddedDriver"},
      {SQLParams.DEFAULT_JDBC_URL_PARAM, "jdbc:derby:${user.home}/.jbundle/derby/${dbname};create=true;collation=TERRITORY_BASED"}, //;collation=TERRITORY_BASED:SECONDARY"},
       {SQLParams.DEFAULT_USERNAME_PARAM, "tourapp"},
       {SQLParams.DEFAULT_PASSWORD_PARAM, "sa1sa"}
 //      {SQLParams.DEFAULT_DATASOURCE_PARAM, "jdbc/{dbname}"},
 //      {SQLParams.DATASOURCE_FACTORY, "DerbyDatasourceFactory -> org.apache.derby.jdbc.ClientDataSource"}
   };
       // END OF MATERIAL TO LOCALIZE
  }
