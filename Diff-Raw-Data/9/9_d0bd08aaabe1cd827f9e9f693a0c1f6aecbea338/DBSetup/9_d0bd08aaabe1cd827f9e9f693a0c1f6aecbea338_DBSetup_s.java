 /***************************************************************
  *  This file is part of the [fleXive](R) framework.
  *
  *  Copyright (c) 1999-2009
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) project is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU Lesser General Public
  *  License version 2.1 or higher as published by the Free Software Foundation.
  *
  *  The GNU Lesser General Public License can be found at
  *  http://www.gnu.org/licenses/lgpl.html.
  *  A copy is found in the textfile LGPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.tools.db;
 
 import com.flexive.core.storage.DBStorage;
 import com.flexive.core.storage.StorageManager;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 
 /**
  * Database setup tool
  * <p/>
  * Required libraries in classpath:
  * <needed jdbc drivers>, commons-lang-2.4.jar, commons-logging.jar, flexive-storage-*.jar, flexive-shared.jar, flexive-ejb.jar
  * <p/>
  * Example commandline for MySQL (execute in build/framework/jar):
  * java -classpath ../../../lib/mysql-connector-java-5.0.8-bin.jar:../../lib/commons-lang-2.4.jar:../../lib/commons-logging.jar:flexive-dbsetup.jar com.flexive.tools.db.DBSetup MySQL fxConf fxConf fxDiv true true root a jdbc:mysql://127.0.0.1:3306/ ?useUnicode=true\&characterEncoding=UTF-8
  *
  * @author Markus Plesser (markus.plesser@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  */
 public class DBSetup {
 
     public static void main(String[] args) {
         if (!(args.length == 9 || args.length == 10)) {
             System.err.println("Usage: " + DBSetup.class.getCanonicalName() + " vendor database schemaConfig schemaDivision createConfig createDivision user password URL [URLParameter]");
             return;
         }
         final String vendor = args[0];
         final String db = args[1];
         final String schemaConfig = args[2];
         final String schemaDivision = args[3];
         final boolean createConfig = Boolean.valueOf(args[4]);
         final boolean createDivision = Boolean.valueOf(args[5]);
         final String user = args[6];
         String pwd = args[7];
         if ("()".equals(pwd)) //marker for empty password
             pwd = "";
         final String jdbcURL = args[8];
         final String jdbcParams = (args.length == 9 ? null : args[9]);
         System.out.println("Setting up database for vendor: " + vendor + " (config:" + schemaConfig + ",division:" + schemaDivision + ")");
         DBStorage storage = StorageManager.getStorageImpl(args[0]);
         if (storage == null) {
             System.err.println("No matching storage implementation found!");
             return;
         }
         Connection con = null;
         try {
             if (createConfig) {
                 try {
                     con = storage.getConnection(db, schemaConfig, jdbcURL, jdbcParams, user, pwd, createConfig, createConfig, true);
                     storage.initConfiguration(con, schemaConfig, true);
                 } catch (Exception e) {
                     System.err.println("Error setting up configuration: " + e.getMessage());
                 }
             }
             if (createDivision) {
                 try {
                     if (con == null)
                         con = storage.getConnection(db, schemaDivision, jdbcURL, jdbcParams, user, pwd, createDivision, createDivision, true);
                     storage.initDivision(con, schemaDivision, true);
                 } catch (Exception e) {
                     System.err.println("Error setting up division: " + e.getMessage());
                 } finally {
                     try {
                         if (con != null)
                             con.close();
                     } catch (SQLException e) {
                         //ignore
                     }
                 }
             }
         } finally {
             try {
                 if (con != null)
                     con.close();
             } catch (SQLException e) {
                 //ignore
             }
        } 
     }
 }
