 /*
  * Copyright (C) 2012 eXo Platform SAS.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.exoplatform.services.database.utils;
 
 /**
  * @author <a href="abazko@exoplatform.com">Anatoliy Bazko</a>
  * @version $Id: DialectConstants.java 34360 2009-07-22 23:58:59Z tolusha $
  */
 public class DialectConstants
 {
    /**
     * DB_DIALECT_AUTO.
     */
    public final static String DB_DIALECT_AUTO = "AUTO";
 
    /**
     * DB_DIALECT_GENERIC.
     */
    public final static String DB_DIALECT_GENERIC = "GENERIC";
 
    /**
     * DB_DIALECT_ORACLE.
     */
    public final static String DB_DIALECT_ORACLE = "ORACLE";
 
    /**
     * DB_DIALECT_ORACLEOCI.
     */
    public final static String DB_DIALECT_ORACLEOCI = "ORACLE-OCI";
 
    /**
     * DB_DIALECT_PGSQL.
     */
   public final static String DB_DIALECT_PGSQL = "PGSLQ";
 
    /**
     * DB_DIALECT_PGSQL_SCS.
     */
   public final static String DB_DIALECT_PGSQL_SCS = "PGSLQ-SCS";
 
    /**
     * DB_DIALECT_MYSQL.
     */
    public final static String DB_DIALECT_MYSQL = "MYSQL";
 
    /**
     * DB_DIALECT_MYSQL_UTF8.
     */
    public final static String DB_DIALECT_MYSQL_UTF8 = "MYSQL-UTF8";
 
    /**
     * DB_DIALECT_MYSQL_NDB.
     */
    public final static String DB_DIALECT_MYSQL_NDB = "MYSQL-NDB";
 
    /**
     * DB_DIALECT_MYSQL_NDB_UTF8.
     */
    public final static String DB_DIALECT_MYSQL_NDB_UTF8 = "MYSQL-NDB-UTF8";
 
    /**
     * DB_DIALECT_MYSQL_MYISAM.
     */
    public final static String DB_DIALECT_MYSQL_MYISAM = "MYSQL-MyISAM";
 
    /**
     * DB_DIALECT_MYSQL_MYISAM_UTF8.
     */
    public final static String DB_DIALECT_MYSQL_MYISAM_UTF8 = "MYSQL-MyISAM-UTF8";
 
    /**
     * DB_DIALECT_HSQLDB.
     */
    public final static String DB_DIALECT_HSQLDB = "HSQLDB";
 
    /**
     * DB_DIALECT_DB2.
     */
    public final static String DB_DIALECT_DB2 = "DB2";
 
    /**
     * DB_DIALECT_DB2-MYS.
     */
    public final static String DB_DIALECT_DB2_MYS = "DB2-MYS";
 
    /**
     * DB_DIALECT_DB2V8.
     */
    public final static String DB_DIALECT_DB2V8 = "DB2V8";
 
    /**
     * DB_DIALECT_MSSQL.
     */
    public final static String DB_DIALECT_MSSQL = "MSSQL";
 
    /**
     * DB_DIALECT_SYBASE.
     */
    public final static String DB_DIALECT_SYBASE = "SYBASE";
 
    /**
     * DB_DIALECT_DERBY.
     */
    public final static String DB_DIALECT_DERBY = "DERBY";
 
    /**
     * DB_DIALECT_INGRES.
     */
    public final static String DB_DIALECT_INGRES = "INGRES";
 
    /**
     * DB_DIALECT_H2.
     */
    public final static String DB_DIALECT_H2 = "H2";
 
    /**
     * DB_DIALECTS.
     */
    public final static String[] DB_DIALECTS = {DB_DIALECT_GENERIC, DB_DIALECT_ORACLE, DB_DIALECT_ORACLEOCI,
       DB_DIALECT_PGSQL, DB_DIALECT_PGSQL_SCS, DB_DIALECT_MYSQL, DB_DIALECT_MYSQL_NDB, DB_DIALECT_MYSQL_NDB_UTF8,
       DB_DIALECT_HSQLDB, DB_DIALECT_DB2, DB_DIALECT_DB2_MYS, DB_DIALECT_DB2V8, DB_DIALECT_MSSQL, DB_DIALECT_SYBASE,
       DB_DIALECT_DERBY, DB_DIALECT_MYSQL_UTF8, DB_DIALECT_INGRES, DB_DIALECT_H2, DB_DIALECT_MYSQL_MYISAM,
       DB_DIALECT_MYSQL_MYISAM_UTF8};
 
 }
