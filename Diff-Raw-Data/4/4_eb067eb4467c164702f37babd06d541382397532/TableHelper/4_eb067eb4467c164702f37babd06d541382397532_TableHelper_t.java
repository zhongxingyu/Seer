 /**
  * Bristlecone Test Tools for Databases
  * Copyright (C) 2006-2007 Continuent Inc.
  * Contact: bristlecone@lists.forge.continuent.org
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of version 2 of the GNU General Public License as
  * published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA
  *
  * Initial developer(s): Robert Hodges and Ralph Hannus.
  * Contributor(s): Linas Virbalas
  */
 
 package com.continuent.bristlecone.benchmark.db;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.sql.Types;
 
 import org.apache.log4j.Logger;
 
 import com.continuent.bristlecone.benchmark.BenchmarkException;
 
 /**
  * Implements methods to create, drop, and populate individual tables.
  * 
  * @author rhodges
  */
 public class TableHelper
 {
     private static Logger      logger           = Logger.getLogger(TableHelper.class);
 
     protected final String     connectionUrl;
     protected final String     login;
     protected final String     password;
     protected final String     defaultSchema;
     protected final SqlDialect sqlDialect;
 
     // Used with BatchLoader and staged load method.
     private String             stageTablePrefix = "stage_xxx";
     private String             stageRowIdColumn = "row_id";
 
     /**
      * Creates a new instance.
      * 
      * @param url JDBC URL of database where tables live
      * @param login
      * @param password
      * @throws BenchmarkException If JDBC driver cannot be loaded or we can't
      *             find the SqlDialect.
      */
     public TableHelper(String url, String login, String password)
     {
         this(url, login, password, null);
     }
 
     /**
      * Creates a new instance.
      * 
      * @param url JDBC URL of database where tables live
      * @param login
      * @param password
      * @param defaultSchema
      * @throws BenchmarkException If JDBC driver cannot be loaded or we can't
      *             find the SqlDialect.
      */
     public TableHelper(String url, String login, String password,
             String defaultSchema)
     {
         this.connectionUrl = url;
         this.login = login;
         this.password = password;
         this.defaultSchema = defaultSchema;
         this.sqlDialect = SqlDialectFactory.getInstance().getDialect(url);
         loadDriver(sqlDialect.getDriver());
     }
 
     /**
      * Loads a JDBC driver.
      */
     public void loadDriver(String name) throws BenchmarkException
     {
         try
         {
             Class.forName(name);
         }
         catch (Exception e)
         {
             throw new BenchmarkException("Unable to load JDBC driver: " + name,
                     e);
         }
     }
 
     /**
      * Returns the SQLDialect used by this helper.
      */
     public SqlDialect getSqlDialect()
     {
         return sqlDialect;
     }
 
     /**
      * Runs an arbitrary SQL command with proper clean-up of resources.
      */
     public void execute(String sql) throws SQLException
     {
         Connection conn = getConnection();
         Statement stmt = conn.createStatement();
         try
         {
             stmt.execute(sql);
         }
         finally
         {
             releaseStatement(stmt);
             releaseConnection(conn);
         }
     }
 
     /**
      * Creates staging tables for the base table. Staging tables are used by
      * Replicator's BatchLoader if using staged load method.
      * 
      * @param baseTable Base table for which to create the corresponding staging
      *            tables.
      */
     private void createStageTables(Table baseTable, boolean dropExisting)
             throws SQLException
     {
         String stageInsertName = stageTablePrefix + "_insert_"
                 + baseTable.getName();
         String stageDeleteName = stageTablePrefix + "_delete_"
                 + baseTable.getName();
 
         // Create stage table definition for insert by prefixing the base
         // name, removing PK constraint and adding a stageRowIdColumn column.
         Table stageInsertTable = baseTable.clone();
         stageInsertTable.setName(stageInsertName);
         Column primaryKey = stageInsertTable.getPrimaryKey();
         if (primaryKey != null)
             primaryKey.setPrimaryKey(false);
         Column rowIdColI = new Column(stageRowIdColumn, Types.INTEGER);
         stageInsertTable.addColumn(rowIdColI);
 
         if (logger.isDebugEnabled())
         {
             logger.debug("Creating test staging table: " + stageInsertName);
             logger.debug("Table details: " + stageInsertTable);
         }
 
         create(stageInsertTable, dropExisting);
 
         // There's a bug in Table.clone() - it actually did not clone the column
         // objects, so we must set PK back on.
         if (primaryKey != null)
             primaryKey.setPrimaryKey(true);
 
         // Create stage table definition for delete by prefixing the base
         // name and adding the primary key and row_id as columns.
         Table stageDeleteTable = new Table(stageDeleteName);
        Column pkeyCol = baseTable.getPrimaryKey();
         stageDeleteTable.addColumn(pkeyCol);
         Column rowIdColD = new Column(stageRowIdColumn, Types.INTEGER);
         stageDeleteTable.addColumn(rowIdColD);
 
         if (logger.isDebugEnabled())
         {
             logger.debug("Creating test staging table: " + stageDeleteTable);
             logger.debug("Table details: " + stageDeleteTable);
         }
 
         create(stageDeleteTable, dropExisting);
     }
 
     /**
      * Creates a table from a definition and corresponding staging tables if
      * needed. Staging tables are used with Replicator's BatchApplier with
      * staged load method.
      * 
      * @param stageTables If true, create the staging tables.
      * @see {@link #create(Table, boolean)}
      */
     public void create(Table baseTable, boolean dropExisting,
             boolean stageTables) throws SQLException
     {
         create(baseTable, dropExisting);
         if (stageTables)
             createStageTables(baseTable, dropExisting);
     }
 
     /**
      * Creates a table from a definition.
      * 
      * @param table Definition of table to be dropped
      * @param dropExisting If true, try to drop an existing table first
      */
     public void create(Table table, boolean dropExisting) throws SQLException
     {
         // Drop existing table.
         if (dropExisting)
             drop(table, true);
 
         Connection conn = getConnection();
         String createSql = null;
         Statement stmt = conn.createStatement();
         try
         {
             // Create the table.
             createSql = sqlDialect.getCreateTable(table);
             stmt.execute(createSql);
 
             // Add extra index for any indexed columns.
             if (sqlDialect.implementationSupportsIndexes())
             {
                 for (int c = 0; c < table.getColumns().length; c++)
                 {
                     Column col = table.getColumns()[c];
                     if (col.isIndexed())
                     {
                         createSql = sqlDialect.getCreateIndex(table, col);
                         stmt.execute(createSql);
                     }
                 }
             }
 
             // If additional command to create table is necessary, execute it
             // now.
             if (sqlDialect.implementationSupportsSupplementaryTableDdl())
             {
                 String supplementaryDdl = sqlDialect
                         .getSupplementaryTableDdl(table);
                 stmt.execute(supplementaryDdl);
             }
         }
         catch (SQLException e)
         {
             logger.debug("Table creation failed: " + createSql, e);
             throw e;
         }
         finally
         {
             releaseStatement(stmt);
             releaseConnection(conn);
         }
     }
 
     /**
      * Drop all tables in the table set, optionally ignoring errors due to
      * non-existing tables.
      */
     public void drop(Table table, boolean ignore) throws SQLException
     {
         Connection conn = getConnection();
 
         String dropSql = sqlDialect.getDropTable(table);
         Statement stmt = conn.createStatement();
         try
         {
             stmt.execute(dropSql);
         }
         catch (SQLException e)
         {
             if (ignore)
                 logger.debug("Table deletion failure ignored: " + dropSql);
             else
             {
                 logger.debug("Table drop failed: " + dropSql, e);
                 throw e;
             }
         }
         finally
         {
             releaseStatement(stmt);
             releaseConnection(conn);
         }
     }
 
     /**
      * Inserts a row using values from an array of objects. The input array
      * should not include values for auto-increment fields.
      * 
      * @param table Table
      * @param values Array of values to insert.
      */
     public void insert(Table table, Object[] values) throws SQLException
     {
         Connection conn = getConnection();
         String insert = sqlDialect.getInsert(table);
         PreparedStatement ps = conn.prepareStatement(insert);
         try
         {
             for (int i = 0; i < values.length; i++)
             {
                 ps.setObject(i + 1, values[i]);
             }
             ps.execute();
         }
         catch (SQLException e)
         {
             logger.debug("Table insert failed: " + insert, e);
             throw e;
         }
         finally
         {
             releaseStatement(ps);
             releaseConnection(conn);
         }
     }
 
     /**
      * Deletes a row using key values from an array of objects.
      * 
      * @param table Table
      * @param keys Array containing keys
      */
     public void delete(Table table, Object[] keys) throws SQLException
     {
         Connection conn = getConnection();
         String delete = sqlDialect.getDeleteByKey(table);
         PreparedStatement ps = conn.prepareStatement(delete);
         try
         {
             for (int i = 0; i < keys.length; i++)
             {
                 ps.setObject(i + 1, keys[i]);
             }
             ps.execute();
         }
         catch (SQLException e)
         {
             logger.debug("Table delete failed: " + delete, e);
             throw e;
         }
         finally
         {
             releaseStatement(ps);
             releaseConnection(conn);
         }
     }
 
     /** Gets a database connection. */
     public Connection getConnection() throws SQLException
     {
         // Connect to database.
         logger.debug("Connecting to database: url=" + connectionUrl + " user="
                 + login + " schema=" + defaultSchema);
         Connection conn = DriverManager.getConnection(connectionUrl, login,
                 password);
 
         // Set default schema if there is one.
         if (defaultSchema != null)
         {
             // Execute a statement to get the schema.
             String setDefaultSchema = sqlDialect
                     .getSetDefaultSchema(defaultSchema);
             Statement stmt = conn.createStatement();
             try
             {
                 if (logger.isDebugEnabled())
                 {
                     logger.debug("Setting default schema: " + setDefaultSchema);
                 }
                 stmt.execute(setDefaultSchema);
             }
             finally
             {
                 releaseStatement(stmt);
             }
         }
         return conn;
     }
 
     /** Releases a database connection. */
     public void releaseConnection(Connection conn)
     {
         // Connect to database.
         logger.debug("Releasing database connection: " + conn);
         try
         {
             conn.close();
         }
         catch (SQLException e)
         {
             logger.debug("Connection release failed", e);
         }
     }
 
     /** Releases a statement. */
     public void releaseStatement(Statement stmt)
     {
         // Connect to database.
         logger.debug("Releasing database statement: " + stmt);
         try
         {
             stmt.close();
         }
         catch (SQLException e)
         {
             logger.debug("Statement release failed", e);
         }
     }
 
     /**
      * Confirm [non-]existence of a particular row indexed by a key.
      * 
      * @param key Key value
      * @param exists If true, expect the key to exist. Otherwise we expect not
      *            to find it.
      * @param limit Number of milliseconds to wait before giving up.
      * @param logInterval Interval in milliseconds between writing messages.
      * @return true if the test succeeded, false if we exceeded the limit
      * @throws BenchmarkException If the test criteria appear to be bad
      * @throws Execption If there is any other exception
      */
     public boolean testRowExistence(PreparedStatement keyQuery, String key,
             boolean exists, long limit, long logInterval)
             throws BenchmarkException, Exception
     {
         long limitTimer = System.currentTimeMillis();
         long logIntervalTimer = limitTimer;
         keyQuery.setString(1, key);
 
         // Repeat the search until we exceed the time limit.
         do
         {
             // Look for matching tables.
             ResultSet rs = null;
             int matches = 0;
             try
             {
                 rs = keyQuery.executeQuery();
                 while (rs.next())
                 {
                     matches++;
                 }
             }
             finally
             {
                 if (rs != null)
                     rs.close();
             }
 
             // If there are multiple matches the test selection criteria are
             // buggy.
             if (matches > 1)
                 throw new BenchmarkException(
                         "Found multiple matches when searching for record: key="
                                 + key);
 
             // Apply result matching to ensure we get what we are looking for...
             if (exists && matches == 1)
             {
                 // Key expected to exist and we found it.
                 return true;
             }
             else if (!exists && matches == 0)
             {
                 // Key not expected to exist and we did not find it.
                 return true;
             }
 
             // See if we need to log a message because we have been waiting over
             // the
             // log limit.
             if ((System.currentTimeMillis() - logIntervalTimer) > logInterval)
             {
                 logIntervalTimer = System.currentTimeMillis();
                 logger.info("Waited " + (logInterval / 1000)
                         + " to test row existence: key=" + key + " existence="
                         + exists);
             }
         }
         while ((System.currentTimeMillis() - limitTimer) < limit);
 
         // If we got this far the test exceeded the timeout limit and is a
         // failure.
         return false;
     }
 }
