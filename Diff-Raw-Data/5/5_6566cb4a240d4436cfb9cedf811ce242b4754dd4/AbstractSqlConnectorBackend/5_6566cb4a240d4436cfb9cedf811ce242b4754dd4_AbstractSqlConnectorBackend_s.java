 /*
  * Copyright (c) 2009, GoodData Corporation. All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided
  * that the following conditions are met:
  *
  *     * Redistributions of source code must retain the above copyright notice, this list of conditions and
  *        the following disclaimer.
  *     * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
  *        and the following disclaimer in the documentation and/or other materials provided with the distribution.
  *     * Neither the name of the GoodData Corporation nor the names of its contributors may be used to endorse
  *        or promote products derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
  * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
  * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
  * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.gooddata.connector.backend;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.Connection;
 import java.sql.DatabaseMetaData;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import au.com.bytecode.opencsv.CSVReader;
 import com.gooddata.util.CSVWriter;
 import org.apache.log4j.Logger;
 
 import com.gooddata.connector.model.PdmColumn;
 import com.gooddata.connector.model.PdmLookupReplication;
 import com.gooddata.connector.model.PdmSchema;
 import com.gooddata.connector.model.PdmTable;
 import com.gooddata.exception.ConnectorBackendException;
 import com.gooddata.exception.InternalErrorException;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.DLI;
 import com.gooddata.integration.model.DLIPart;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.naming.N;
 import com.gooddata.util.FileUtil;
 import com.gooddata.util.JdbcUtil;
 import com.gooddata.util.StringUtil;
 import com.gooddata.util.JdbcUtil.StatementHandler;
 
 /**
  * GoodData abstract connector backend. This connector backend provides the base implementation that the specific
  * connector backends reuse.
  * Connector backend handles communication with the specific SQL database. Specifically it handles the DB connection
  * and other communication specifics of the DBMS. It uses the SQL driver that generates appropriate SQL dialect.
  *
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */public abstract class AbstractSqlConnectorBackend extends AbstractConnectorBackend implements ConnectorBackend {
 
     private static final int BATCH_SIZE = 1000;
 
     private static Logger l = Logger.getLogger(AbstractSqlConnectorBackend.class);
 
     // Project id
     private String projectId;
 
     // database username
     private String username;
 
     // database password
     private String password;
     
     // database connection
     protected Connection connection = null;
     
     // autoincrement syntax
     protected String SYNTAX_AUTOINCREMENT = "";
 
     // SQL concat function prefix and suffix
     protected String SYNTAX_CONCAT_FUNCTION_PREFIX = "";
     protected String SYNTAX_CONCAT_FUNCTION_SUFFIX = "";
     protected String SYNTAX_CONCAT_OPERATOR = "";
 
     // separates the different LABELs when we concatenate them to create an unique identifier out of them
     protected String HASH_SEPARATOR = "%";
 
     /**
      * Constructor
      * @param username database backend username
      * @param password database backend password 
      * @throws IOException in case of an IO issue 
      */
     protected AbstractSqlConnectorBackend(String username, String password) throws IOException {
         setUsername(username);
         setPassword(password);
     }
 
     /**
      * {@inheritDoc}
      */
     public abstract void dropSnapshots(); 
 
     /**
      * {@inheritDoc}
      */
     public void deploy(DLI dli, List<DLIPart> parts, String dir, String archiveName)
             throws IOException {
         deploySnapshot(dli, parts, dir, archiveName, null);
     }
 
     /**
      * {@inheritDoc}
      */
     public void initialize() {
         Connection con;
         try {
             l.debug("Initializing schema.");
         	con = getConnection();
             if(!isInitialized()) {
                 l.debug("Initializing system schema.");
                 initializeLocalProject();
                 l.debug("System schema initialized.");
             }
             initializeLocalDataSet(getPdm());
             l.debug("Schema initialized.");
         }
         catch (SQLException e) {
             throw new ConnectorBackendException("Error initializing pdm schema '" + getPdm().getName() + "'", e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String listSnapshots() {
         String result = "ID        FROM ROWID        TO ROWID        TIME\n";
               result += "------------------------------------------------\n";
         Connection con = null;
         Statement s = null;
         ResultSet r = null;
         try {
             con = getConnection();
             s = con.createStatement();
             r = JdbcUtil.executeQuery(s, "SELECT id,firstid,lastid,tmstmp FROM snapshots");
             for(boolean rc = r.next(); rc; rc = r.next()) {
                 int id = r.getInt(1);
                 int firstid = r.getInt(2);
                 int lastid = r.getInt(3);
                 long tmstmp = r.getLong(4);
                 Date tm = new Date(tmstmp);
                 result += id + "        " + firstid + "        " + lastid + "        " + tm + "\n";
             }
         }
         catch (SQLException e) {
             throw new ConnectorBackendException(e);
         }
         finally {
             try {
                 if(r != null)
                     r.close();
                 if (s != null)
                     s.close();
             }
             catch (SQLException ee) {
                l.warn("Error closing stuff: " + ee.getMessage(), ee);
             }
         }
         l.debug("Current snapshots: \n"+result);
         return result;
     }
 
 
     /**
      * {@inheritDoc}
      */
     public int getLastSnapshotId() {
         Connection con = null;
         Statement s = null;
         ResultSet r = null;
         try {
             con = getConnection();
             s = con.createStatement();
             r = s.executeQuery("SELECT MAX(id) FROM snapshots");
             for(boolean rc = r.next(); rc; rc = r.next()) {
                 int id = r.getInt(1);
                 l.debug("Last snapshot is "+id);
                 return id;
             }
         }
         catch (SQLException e) {
             throw new InternalErrorException(e.getMessage());
         }
         finally {
             try {
                 if(r != null)
                     r.close();
                 if(s != null)
                     s.close();
             }
             catch (SQLException ee) {
                 ee.printStackTrace();
             }
         }
         throw new InternalErrorException("Can't retrieve the last snapshot number.");
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isInitialized() {
         return exists("snapshots");
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean exists(String tbl) {
         Connection con = null;
         try {
             con = getConnection();
             return exists(con, tbl);
         }
         catch (SQLException e) {
         	throw new InternalErrorException(e);
 		}
     }
     
     protected abstract Connection getConnection() throws SQLException;
 
 	/**
      * {@inheritDoc}
      */
     protected void initializeLocalProject() {
     	try {
 	        l.debug("Executing system DDL SQL.");
 	        Connection c = getConnection();
 	        createSnapshotTable(c);
 	        createFunctions(c);
 	        l.debug("System DDL SQL execution finished.");
     	} catch (SQLException e) {
     		throw new ConnectorBackendException(e);
     	}
     }
 
     /**
      * {@inheritDoc}
      */
     protected void initializeLocalDataSet(PdmSchema schema) {
         l.debug("Executing DDL SQL.");
         try {
 	        Connection c = getConnection();
 	        for(PdmTable table : schema.getTables()) {
 	        	if (!exists(c, table.getName())) {
 	        		createTable(c, table);
 	        		if (PdmTable.PDM_TABLE_TYPE_LOOKUP.equals(table.getType())) {
 	        			prepopulateLookupTable(c, table);
 	        		} else if (PdmTable.PDM_TABLE_TYPE_CONNECTION_POINT.equals(table.getType())) {
 	        			final List<Map<String,String>> rows = prepareInitialTableLoad(table);
 	        			if (!rows.isEmpty()) {
 	        				l.warn("Prepopulating of connection point tables is not suppported (table = " + table.getName() + ")");
 	        			}
 	        		}
 		            /*
                     if(PdmTable.PDM_TABLE_TYPE_SOURCE.equals(table.getType()))
 		                indexAllTableColumns(c, table);
                     */
 	        	} else {
 	        		for (PdmColumn column : table.getColumns()) {
 	        			if (!exists(c, table.getName(), column.getName())) {
 	        				addColumn(c, table, column);
                             /*
 	        				if (PdmTable.PDM_TABLE_TYPE_SOURCE.equals(table.getType()))
 	        					indexTableColumn(c, table, column);
 	        		        */
 	        			}
 	        		}
 	        	}
 	        }
 	        JdbcUtil.executeUpdate(c,
 	            "INSERT INTO snapshots(name,firstid,lastid,tmstmp) VALUES ('" + schema.getFactTable().getName() + "',0,0,0)"
 	        );
 	        l.debug("DDL SQL Execution finished.");
         } catch (SQLException e) {
         	throw new ConnectorBackendException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     protected void createSnowflake(PdmSchema schema) {
     	try {
 	    	Connection c = getConnection();
 	        l.debug("Executing data normalization SQL.");
 	        //populate REFERENCEs lookups from the referenced lookups
 
 	        l.debug("Executing referenced lookups replication.");	        
 	        executeLookupReplicationSql(c, schema);
 	        
 	        l.debug("Finished referenced lookups replication.");
 	        l.debug("Executing lookup tables population.");	        
 	        populateLookupTables(c, schema);
 	        
 	        l.debug("Finished lookup tables population.");
 	        l.debug("Executing connection point tables population.");
 	        populateConnectionPointTables(c, schema);
 	        
 	        l.debug("FInished connection point tables population.");
 	        // nothing for the reference columns
 	        l.debug("Inserting partial snapshot record.");
 	        insertSnapshotsRecord(c, schema);
 	        
 	        l.debug("Executing fact table population.");
 	        insertFactsToFactTable(c, schema);
 	        
 	        l.debug("FInished fact table population.");
 	        l.debug("Executing fact table FK generation.");
 	        updateFactTableFk(c, schema);	        
 	        
 	        l.debug("Finished fact table FK generation.");
 	        updateSnapshotsRecord(c, schema);
 	        
 	        l.debug("Snapshot record updated.");
 	        l.debug("Finished data normalization SQL.");
     	} catch (SQLException e) {
     		throw new ConnectorBackendException(e);
     	}
     }
 
 
 
     /**
      * {@inheritDoc}
      */
     protected void executeLookupReplicationSql(Connection c, PdmSchema schema) throws SQLException {
         for (PdmLookupReplication lr : schema.getLookupReplications()) {
             JdbcUtil.executeUpdate(c,
                 "DELETE FROM " + lr.getReferencingLookup()
             );
             JdbcUtil.executeUpdate(c,
                 "INSERT INTO " + lr.getReferencingLookup() + "("+N.ID+"," + lr.getReferencingColumn() +","+N.HSH+")" +
                 " SELECT "+ N.ID+"," + lr.getReferencedColumn() + "," + lr.getReferencedColumn() + " FROM " +
                 lr.getReferencedLookup()
             );
         }
     }
     
     /**
      * {@inheritDoc}
      * @throws SQLException 
      */
     protected boolean exists(Connection c, String tbl) throws SQLException {
     	DatabaseMetaData md = c.getMetaData();
     	ResultSet rs = md.getTables(null, null, tbl, null);
     	try {
 	    	return rs.next();
     	} finally {
     		if (rs != null)
     			rs.close();
     	}
     }
 
     /**
      * Returns true if the specified column of the specified table exists in the DB. Case sensitive!
      * @param tbl table name
      * @param col column name
      * @return true if the table exists, false otherwise
      * @throws IllegalArgumentException if the required table does not exist
      * @throws SQLException if other database related problem occures 
      */
     protected boolean exists(Connection c, String tbl, String col) throws SQLException {
     	if (!exists(c, tbl))
     		throw new IllegalArgumentException("Table '" + tbl + "' does not exist.");
     	String sql = "SELECT * FROM " + tbl + " WHERE 1=0";
 		Statement st = c.createStatement();
 		try {
             ResultSet rs = st.executeQuery(sql);
             try {
 	            ResultSetMetaData md = rs.getMetaData();
 	            int cols = md.getColumnCount();
 	            for (int i = 1; i <= cols; i++) {
 	            	if (col.equals(md.getColumnName(i)))
 	            		return true;
 	            }
 	            return false;
     		} finally {
     			if (rs != null)
     				rs.close();
     		}
 		} finally {
 			if (st != null)
 				st.close();
 		}
     }
 
 
     /**
      * Indexes all table columns
      * @param c JDBC connection
      * @param table target table
      * @throws SQLException in case of SQL issues
      */
     protected void indexAllTableColumns(Connection c, PdmTable table) throws SQLException {
         for( PdmColumn column : table.getColumns()) {
             indexTableColumn(c, table, column);
         }
     }
 
     /**
      * Indexes table's column
      * @param c JDBC connection
      * @param table target table
      * @param column target table's columns
      * @throws SQLException in case of SQL issues
      */
     private void indexTableColumn(Connection c, PdmTable table, PdmColumn column) throws SQLException {
     	if(!column.isPrimaryKey() && !column.isUnique()) {
             JdbcUtil.executeUpdate(c,"CREATE INDEX idx_" + table.getName()
             		+ "_" + column.getName()
             		+ " ON " + table.getName() + "("+column.getName()+")");
     	}
     }
 
     /**
      * Creates a new table
      * @param c JDBC connection
      * @param table target table
      * @throws SQLException in case of SQL issues
      */
     protected void createTable(Connection c, PdmTable table) throws SQLException {
         String pk = "";
         String sql = "CREATE TABLE " + table.getName() + " (\n";
         for( PdmColumn column : table.getColumns()) {
             sql += " "+ column.getName() + " " + column.getType();
             if(column.isUnique())
                 sql += " UNIQUE";
             if(column.isAutoIncrement())
                 sql += " " + SYNTAX_AUTOINCREMENT;
             if(column.isPrimaryKey())
                 if(pk != null && pk.length() > 0)
                     pk += "," + column.getName();
                 else
                     pk += column.getName();
             sql += ",";
         }
         sql += " PRIMARY KEY (" + pk + "))";
 
         JdbcUtil.executeUpdate(c, sql);
         for( PdmColumn column : table.getColumns()) {
             if(column.isNonUniqueIndexed()) {
                 indexTableColumn(c, table, column);   
             }
         }
 
     }
 
     /**
      * Fills the lookup table with the DISTINCT values from the source table
      * @param c JDBC connection
      * @param table target lookup table
      * @throws SQLException in case of SQL issues
      */
     private void prepopulateLookupTable(Connection c, PdmTable table) throws SQLException {
     	final List<Map<String,String>> rows = prepareInitialTableLoad(table);
     	if (rows.isEmpty())
     		return;
     	
     	// create the list to make sure consistent keys order in the following loop
 		final List<String> columns = new ArrayList<String>(rows.get(0).keySet());
     	final String placeholders = StringUtil.join(", ", columns, "?");
 
     	for (final Map<String,String> row : rows) {
     		
     		final String sql = "INSERT INTO " + table.getName() + " ("
     						 + N.HSH + ", " + StringUtil.join(", ", columns)
     						 + ") VALUES (?, " + placeholders + ")";
     		
     		JdbcUtil.executeUpdate(c, sql, new StatementHandler() {
 				public void prepare(PreparedStatement stmt) throws SQLException {
 					boolean first = true;
 					final StringBuffer hashbf = new StringBuffer();
 					int index = 2;
 					for (final String col : columns) {
 						if (first)
 							first = false;
 						else
 							hashbf.append(HASH_SEPARATOR);
 						hashbf.append(row.get(col));
 						stmt.setString(index++, row.get(col));
 					}
 					stmt.setString(1, hashbf.toString());
 				}
 			});
     	}
     }
 
     /**
      * Add column to the table (ALTER TABLE)
      * @param c JDBC connection
      * @param table target table
      * @param column target column
      * @throws SQLException in case of SQL issues
      */
     private void addColumn(Connection c, PdmTable table, PdmColumn column) throws SQLException {
     	String sql = "ALTER TABLE " + table.getName() + " ADD COLUMN "
     			   + column.getName() + " " + column.getType();
     	if (column.isUnique())
     		sql += " UNIQUE";
     	JdbcUtil.executeUpdate(c, sql);
     }
 
     /**
      * Creates the system snapshots table
      * @param c JDBC connection
      * @throws SQLException in case of a DB issue
      */
     protected void createSnapshotTable(Connection c) throws SQLException {
         JdbcUtil.executeUpdate(c,
             "CREATE TABLE snapshots (" +
                 " id INT " + SYNTAX_AUTOINCREMENT + "," +
                 " name VARCHAR(255)," +
                 " tmstmp BIGINT," +
                 " firstid INT," +
                 " lastid INT," +
                 " PRIMARY KEY (id)" +
                 ")"
         );
     }
 
     /**
      * Inserts new records to the snapshots table before the load
      * @param c JDBC connection
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected void insertSnapshotsRecord(Connection c, PdmSchema schema) throws SQLException {
         PdmTable factTable = schema.getFactTable();
         String fact = factTable.getName();
         Date dt = new Date();
         {
 	        final String sql1 = "INSERT INTO snapshots(name,tmstmp,firstid) SELECT '"+fact+"',"+dt.getTime()+",MAX("+N.ID+")+1 FROM " + fact;
 	        JdbcUtil.executeUpdate(c, sql1);
         }
         {
 	        // compensate for the fact that MAX returns NULL when there are no rows in the SELECT
 	        final String sql2 = "UPDATE snapshots SET firstid = 0 WHERE name = '"+fact+"' AND firstid IS NULL";
 	        JdbcUtil.executeUpdate(c, sql2);
         }
     }
 
     /**
      * Updates the snapshots table after load
      * @param c JDBC connection
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected void updateSnapshotsRecord(Connection c, PdmSchema schema) throws SQLException {
         PdmTable factTable = schema.getFactTable();
         String fact = factTable.getName();
         JdbcUtil.executeUpdate(c,
             "UPDATE snapshots SET lastid = (SELECT MAX("+N.ID+") FROM " + fact + ") WHERE name = '" +
             fact + "' AND lastid IS NULL"
         );
         // compensate for the fact that MAX returns NULL when there are no rows in the SELECT
         JdbcUtil.executeUpdate(c,
             "UPDATE snapshots SET lastid = 0 WHERE name = '" + fact + "' AND lastid IS NULL"
         );
     }
 
     /**
      * Inserts rows from the source table to the fact table
      * @param c JDBC connection
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected abstract void insertFactsToFactTable(Connection c, PdmSchema schema) throws SQLException;
 
     protected void populateLookupTables(Connection c, PdmSchema schema) throws SQLException {
         for(PdmTable lookupTable : schema.getLookupTables()) {
             populateLookupTable(c, lookupTable, schema);
         }
     }
 
     /**
      * Populates the connection point table
      * @param c JDBC connection
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected void populateConnectionPointTables(Connection c, PdmSchema schema) throws SQLException {
         for(PdmTable cpTable : schema.getConnectionPointTables())
             populateConnectionPointTable(c, cpTable, schema);
     }
 
     private void updateFactTableFk(Connection c, PdmSchema schema) throws SQLException {
         String fact = schema.getFactTable().getName();
         String updateStatement = "";
         List<PdmTable> tables = new ArrayList<PdmTable>();
         tables.addAll(schema.getLookupTables());
         tables.addAll(schema.getConnectionPointTables());
         tables.addAll(schema.getReferenceTables());
         for(PdmTable tbl : tables) {
             if(updateStatement.length() > 0)
                 updateStatement += " , " + generateFactUpdateSetStatement(tbl, schema);
             else
                 updateStatement += generateFactUpdateSetStatement(tbl, schema);
     	}
         if(updateStatement.length()>0) {
             updateStatement = "UPDATE " + fact + " SET " + updateStatement +
                 " WHERE "+N.ID+" > "+getLastId(c,fact);
             JdbcUtil.executeUpdate(c, updateStatement);
         }
     }
 
 
     /**
      * Generates the UPDATE SET statement for individual lookup FK
      * @param lookupTable lookup table
      * @param schema PDM schema
      * @return the column update clause
      */
     protected String generateFactUpdateSetStatement(PdmTable lookupTable, PdmSchema schema) {
         String lookup = lookupTable.getName();
         String fact = schema.getFactTable().getName();
         String source = schema.getSourceTable().getName();
         String associatedSourceColumns = concatAssociatedSourceColumns(lookupTable);
 
         return lookupTable.getAssociatedSourceColumn() + "_"+N.ID+" = (SELECT "+N.ID+" FROM " +
               lookup + " d," + source + " o WHERE " + associatedSourceColumns + " = d."+N.HSH+" AND o."+N.SRC_ID+"= " +
               fact + "."+N.ID+") ";
     }
 
     /**
      * Populates lookup table
      * @param c JDBC connection
      * @param lookupTable lookup table
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected void populateLookupTable(Connection c, PdmTable lookupTable, PdmSchema schema) throws SQLException {
         String lookup = lookupTable.getName();
         String fact = schema.getFactTable().getName();
         String source = schema.getSourceTable().getName();
         String insertColumns = N.HSH+"," + getInsertColumns(lookupTable);
         String associatedSourceColumns = getAssociatedSourceColumns(lookupTable);
         String concatAssociatedSourceColumns = concatAssociatedSourceColumns(lookupTable);
         String nestedSelectColumns = concatAssociatedSourceColumns+","+associatedSourceColumns;
         JdbcUtil.executeUpdate(c,
             "INSERT INTO " + lookup + "(" + insertColumns +
             ") SELECT DISTINCT " + nestedSelectColumns + " FROM " + source +
             " WHERE "+N.SRC_ID+" > "+getLastId(c,fact)
         );
         JdbcUtil.executeUpdate(c,
             "CREATE TABLE delete_ids("+N.HSH+" "+PdmColumn.PDM_COLUMN_TYPE_LONG_TEXT+", "+N.ID+" INT, PRIMARY KEY(id))"
         );
         JdbcUtil.executeUpdate(c,
             "INSERT INTO delete_ids SELECT "+N.HSH+",max("+N.ID+") FROM "+lookup+
                     " GROUP by "+N.HSH+" HAVING count("+N.ID+") > 1"
         );
         JdbcUtil.executeUpdate(c,
             "DELETE FROM "+lookup+" WHERE "+N.ID+" IN (SELECT "+N.ID+" FROM delete_ids)"
         );
         JdbcUtil.executeUpdate(c,
             "DROP TABLE delete_ids"
         );
     }
 
     /**
      * Gets the last id in the snapshot table
      * @param factTable the fact table
      * @return the last id
      */
     public int getLastId(Connection c, String factTable) {
         Statement s = null;
         ResultSet r = null;
         try {
             s = c.createStatement();
             r = s.executeQuery("SELECT MAX(lastid) FROM snapshots WHERE name='" + factTable +"'");
             for(boolean rc = r.next(); rc; rc = r.next()) {
                 int id = r.getInt(1);
                 l.debug("Last is is "+id);
                 return id;
             }
         }
         catch (SQLException e) {
             throw new InternalErrorException(e.getMessage());
         }
         finally {
             try {
                 if(r != null)
                     r.close();
                 if(s != null)
                     s.close();
             }
             catch (SQLException ee) {
                 ee.printStackTrace();
             }
         }
         throw new InternalErrorException("Can't retrieve the last id number.");
     }
 
 
     /**
      * Populates connection point table
      * @param c JDBC connection
      * @param lookupTable connection point table
      * @param schema PDM schema
      * @throws SQLException in case of a DB issue
      */
     protected void populateConnectionPointTable(Connection c, PdmTable lookupTable, PdmSchema schema) throws SQLException {
         String lookup = lookupTable.getName();
         String fact = schema.getFactTable().getName();
         String source = schema.getSourceTable().getName();
         String insertColumns = N.ID+","+N.HSH+"," + getInsertColumns(lookupTable);
         String associatedSourceColumns = getAssociatedSourceColumns(lookupTable);
         String concatAssociatedSourceColumns = concatAssociatedSourceColumns(lookupTable);
         String nestedSelectColumns = N.SRC_ID+","+concatAssociatedSourceColumns+","+associatedSourceColumns;
         /*
         JdbcUtil.executeUpdate(c,
             "INSERT INTO " + lookup + "(" + insertColumns + ") SELECT DISTINCT " + nestedSelectColumns +
             " FROM " + source + " WHERE "+N.SRC_ID+" > (SELECT MAX(lastid) FROM snapshots WHERE name='" + fact +
             "') AND " + associatedSourceColumns + " NOT IN (SELECT "+N.HSH+" FROM " + lookup + ")"
         );
         */
         // TODO: when snapshotting, there are duplicate CONNECTION POINT VALUES
         // we need to decide if we want to accumultae the connection point lookup or not
         /*
         JdbcUtil.executeUpdate(c,
             "INSERT INTO " + lookup + "(" + insertColumns + ") SELECT DISTINCT " + nestedSelectColumns +
             " FROM " + source + " WHERE "+N.SRC_ID+" > (SELECT MAX(lastid) FROM snapshots WHERE name='" + fact +"')"
         );
         */
         JdbcUtil.executeUpdate(c,
             "INSERT INTO " + lookup + "(" + insertColumns +
             ") SELECT DISTINCT " + nestedSelectColumns + " FROM " + source +
             " WHERE "+N.SRC_ID+" > "+getLastId(c,fact)
         );
         JdbcUtil.executeUpdate(c,
             "CREATE TABLE delete_ids("+N.HSH+" "+PdmColumn.PDM_COLUMN_TYPE_LONG_TEXT+", "+N.ID+" INT, PRIMARY KEY(id))"
         );
         JdbcUtil.executeUpdate(c,
            "INSERT INTO delete_ids SELECT "+N.HSH+",max("+N.ID+") FROM "+lookup+
                     " GROUP by "+N.HSH+" HAVING count("+N.ID+") > 1"
         );
         JdbcUtil.executeUpdate(c,
             "DELETE FROM "+lookup+" WHERE "+N.ID+" IN (SELECT "+N.ID+" FROM delete_ids)"
         );
         JdbcUtil.executeUpdate(c,
             "DROP TABLE delete_ids"
         );
      }
 
     /**
      * Concats associated source columns with a DB specific concat method
      * The concatenated columns are used as a unique ke (hash id) of each lookup row
      * @param lookupTable lookup table
      * @return the concatenated columns as String
      */
     protected String concatAssociatedSourceColumns(PdmTable lookupTable) {
         String associatedColumns = "";
         for(PdmColumn column : lookupTable.getAssociatedColumns()) {
             // if there are LABELS, the lookup can't be added twice to the FROM clause
             if(associatedColumns.length() > 0)
                 associatedColumns += SYNTAX_CONCAT_OPERATOR +  column.getSourceColumn();
             else
                 associatedColumns = column.getSourceColumn();
         }
         associatedColumns = SYNTAX_CONCAT_FUNCTION_PREFIX + associatedColumns + SYNTAX_CONCAT_FUNCTION_SUFFIX;
         return associatedColumns;
     }
 
     /**
      * Get all columns that will be inserted (exclude autoincrements)
      * @param lookupTable lookup table
      * @return all columns eglibile for insert
      */
     protected String getInsertColumns(PdmTable lookupTable) {
         String insertColumns = "";
         for(PdmColumn column : lookupTable.getAssociatedColumns()) {
             if(insertColumns.length() > 0)
                 insertColumns += "," + column.getName();
             else
                 insertColumns += column.getName();
         }
         return insertColumns;
     }
 
     /**
      * Returns associted columns in the source table
      * @param lookupTable lookup table
      * @return list of associated source columns
      */
     protected String getAssociatedSourceColumns(PdmTable lookupTable) {
         String sourceColumns = "";
         for(PdmColumn column : lookupTable.getAssociatedColumns()) {
             if(sourceColumns.length() > 0)
                 sourceColumns += "," + column.getSourceColumn();
             else
                 sourceColumns += column.getSourceColumn();
         }
         return sourceColumns;
     }
 
     /**
      * Returns non-autoincrement columns
      * @param tbl table
      * @return non-autoincrement columns
      */
     protected String getNonAutoincrementColumns(PdmTable tbl) {
         String cols = "";
         for (PdmColumn col : tbl.getColumns()) {
             String cn = col.getName();
             if(!col.isAutoIncrement())
                 if (cols != null && cols.length() > 0)
                     cols += "," + cn;
                 else
                     cols += cn;
         }
         return cols;
     }
 
     /**
      * Returns non-autoincrement columns count
      * @param tbl table
      * @return non-autoincrement columns count
      */
     protected int getNonAutoincrementColumnsCount(PdmTable tbl) {
         int cnt =0;
         for (PdmColumn col : tbl.getColumns())
             if(!col.isAutoIncrement())
                 cnt++;
         return cnt;
     }
 
     /**
      * Returns the prepared statement quetionmarks
      * @param tbl table
      * @return prepared statement question column
      */
     protected String getPreparedStatementQuestionMarks(PdmTable tbl) {
         String cols = "";
         for (PdmColumn col : tbl.getColumns()) {
             if(!col.isAutoIncrement())
                 if (cols != null && cols.length() > 0)
                     cols += ",?";
                 else
                     cols += "?";
         }
         return cols;
     }
 
 
 
     /**
      * Generates the where clause for unloading data to CSVs in the data loading package
      * @param part DLI part
      * @param schema PDM schema
      * @param snapshotIds ids of snapshots to unload
      * @return SQL where clause
      */
     protected String getLoadWhereClause(DLIPart part, PdmSchema schema, int[] snapshotIds) {
         String dliTable = getTableNameFromPart(part);
         PdmTable pdmTable = schema.getTableByName(dliTable);
         String whereClause = "";
         if(PdmTable.PDM_TABLE_TYPE_FACT.equals(pdmTable.getType()) && snapshotIds != null && snapshotIds.length > 0) {
             String inClause = "";
             for(int i : snapshotIds) {
                 if(inClause.length()>0)
                     inClause += ","+i;
                 else
                     inClause = "" + i;
             }
             whereClause = ",snapshots WHERE " + dliTable +
                     ".ID BETWEEN snapshots.firstid and snapshots.lastid AND snapshots.id IN (" + inClause + ")";
         }
         return whereClause;
     }
 
     /**
      * Generates the list of columns for unloading data to CSVs in the data loading package
      * @param part DLI part
      * @param schema PDM schema
      * @return list of columns
      */
     protected String getLoadColumns(DLIPart part, PdmSchema schema)  {
         String dliTable = getTableNameFromPart(part);
         PdmTable pdmTable = schema.getTableByName(dliTable);
         List<Column> columns = part.getColumns();
         String cols = "";
         for (Column cl : columns) {
         	PdmColumn col = null;
         	if (isPrimaryKey(cl)) {
         		col = pdmTable.getConnectionPointReferenceColumn();
         	}
         	if (col == null) {
         		col = pdmTable.getColumnByName(cl.getName());
         	}
             // fact table fact columns
             if(PdmTable.PDM_TABLE_TYPE_FACT.equals(pdmTable.getType()) &&
                     SourceColumn.LDM_TYPE_FACT.equals(col.getLdmTypeReference()))
                 cols = decorateFactColumnForLoad(cols, col, dliTable);
             // lookup table name column
             else if (PdmTable.PDM_TABLE_TYPE_LOOKUP.equals(pdmTable.getType()) &&
                     SourceColumn.LDM_TYPE_ATTRIBUTE.equals(col.getLdmTypeReference()))
                 cols = decorateLookupColumnForLoad(cols, col, dliTable);
             else
                 cols = decorateOtherColumnForLoad(cols, col, dliTable);
         }
         return cols;
     }
 
     /**
      * Returns the CSV header of the loaded data
      * @param part DLI part
      * @return list of columns
      */
     protected String[] getLoadHeader(DLIPart part)  {
         List<Column> cols = part.getColumns();
         String[] header = new String[cols.size()];
         for(int i=0; i<cols.size(); i++) {
             header[i] = cols.get(i).getName();
         }
         return header;
     }
 
     protected static boolean isPrimaryKey(Column cl) {
     	if (cl.getConstraints() == null)
     		return false;
 		return cl.getConstraints().matches("(?i).*PRIMARY  *KEY.*");
 	}
 
 	/**
      * Uses DBMS specific functions for decorating fact columns for unloading from DB to CSV
      * @param cols column list
      * @param cl column to add to cols
      * @param table table name
      * @return the amended list
      */
     protected String decorateFactColumnForLoad(String cols, PdmColumn cl, String table) {
         return decorateOtherColumnForLoad(cols, cl, table);
     }
 
     /**
      * Uses DBMS specific functions for decorating lookup columns for unloading from DB to CSV
      * @param cols column list
      * @param cl column to add to cols
      * @param table table name
      * @return the amended list
      */
     protected String decorateLookupColumnForLoad(String cols, PdmColumn cl, String table) {
         return decorateOtherColumnForLoad(cols, cl, table);
     }
 
     /**
      * Uses DBMS specific functions for decorating generic columns for unloading from DB to CSV
      * @param cols column list
      * @param cl column to add to cols
      * @param table table name
      * @return the amended list
      */
     protected String decorateOtherColumnForLoad(String cols, PdmColumn cl, String table) {
         if (cols != null && cols.length() > 0)
             cols += "," + table + "." + StringUtil.toIdentifier(cl.getName());
         else
             cols +=  table + "." + StringUtil.toIdentifier(cl.getName());
         return cols;
     }
     
     public void close() {
 		try {
 			if (connection != null && !connection.isClosed()) {
     			connection.close();
     		}
     	} catch (SQLException e) {
 			throw new InternalErrorException(e);
 		}
     }
 
     /**
      * Get tab,e name from DLI part
      * @param part DLI part
      * @return table name
      */
     protected String getTableNameFromPart(DLIPart part) {
         return StringUtil.toIdentifier(part.getFileName().split("\\.")[0]);
     }
 
     /**
      * Creates the DBMS specific system functions
      * @param c JDBC connection
      * @throws SQLException in case of DB issues
      */
     protected abstract void createFunctions(Connection c) throws SQLException;
 
 
     /**
      * {@inheritDoc}
      */
     public String getUsername() {
         return username;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setUsername(String username) {
         this.username = username;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * {@inheritDoc}
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     /**
      * {@inheritDoc}
      */
     public String getProjectId() {
         return projectId;
     }
     
     /**
      * {@inheritDoc}
      */
     public void setProjectId(String projectId) {
         this.projectId = projectId;
     }
 
     /**
      * {@inheritDoc}
      */
     public void executeExtract(PdmSchema schema, String file, boolean hasHeader) {
         Connection c = null;
         PreparedStatement s  = null;
         try {
 	    	c = getConnection();
 
 	        l.debug("Extracting data.");
 	        PdmTable sourceTable = schema.getSourceTable();
 	        String source = sourceTable.getName();
 	        String cols = getNonAutoincrementColumns(sourceTable);
             String qmrks = getPreparedStatementQuestionMarks(sourceTable);
             int cnt = getNonAutoincrementColumnsCount(sourceTable);
             CSVReader csvIn = new CSVReader(FileUtil.createBufferedUtf8Reader(file));
             String[] nextLine;
             int rowCnt = 0;
             while ((nextLine = csvIn.readNext()) != null) {
                 rowCnt++;
                 if(hasHeader)
                     hasHeader = false;
                 else {
                     if(s == null) {
                         s = c.prepareStatement("INSERT INTO "+source+"("+cols+") VALUES ("+qmrks+")");
                     }
                     if(nextLine.length == cnt) {
                         for(int i=1; i<=nextLine.length; i++)
                             if(nextLine[i-1]!=null)
                                 s.setString(i,nextLine[i-1].substring(0,Math.min(Constants.LABEL_MAX_LENGTH,nextLine[i-1].length())));
                             else
                                 s.setString(i,"");
                         s.addBatch();
                     }
                     else {
                         l.warn("Skipping row "+file+":"+rowCnt+" as it has "+nextLine.length+
                                 " columns. Expecting "+cnt+" columns.");
                     }
                     if(rowCnt % BATCH_SIZE == 0) {
                         s.executeBatch();
                         s.close();
                         s = null;
                     }
                 }
 	        }
             if(s != null) {
                 s.executeBatch();
             }
	        l.debug("Finished extracting data.");
     	} catch (SQLException e) {
     		throw new ConnectorBackendException(e);
         } catch (IOException e) {
     		throw new ConnectorBackendException(e);
     	}
         finally {
             try  {
                 if(s != null)
                     s.close();
             }
             catch (SQLException e) {
                 // nothing we can do
             }
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void executeLoad(PdmSchema schema, DLIPart part, String dir, int[] snapshotIds) {
     	Connection c = null;
         try {
 	    	c = getConnection();
 	        l.debug("Unloading data.");
 	        final String file = dir + System.getProperty("file.separator") + part.getFileName();
 	        String cols = getLoadColumns(part, schema);
 	        String whereClause = getLoadWhereClause(part, schema, snapshotIds);
 	        String dliTable = getTableNameFromPart(part);
 	        String sql = "SELECT " + cols + " FROM " + dliTable.toUpperCase() + whereClause;
             String[] header = getLoadHeader(part);
             CSVWriter cw = FileUtil.createUtf8CsvWriter(new File(file));
             cw.writeNext(header);
             JdbcUtil.executeQuery(c, sql, new ResultSetCsvWriter(cw));
             cw.close();
 	        l.debug("Finished unloading data.");
     	} catch (SQLException e) {
     		throw new ConnectorBackendException(e);
         } catch (IOException e) {
     		throw new ConnectorBackendException(e);
     	}
     }
 
     private static class ResultSetCsvWriter implements JdbcUtil.ResultSetHandler {
 
         private final CSVWriter cw;
         protected int rowCnt = 0;
 
         public ResultSetCsvWriter(CSVWriter cw) {
             this.cw = cw;
         }
 
         public void handle(ResultSet rs) throws SQLException {
             final int length = rs.getMetaData().getColumnCount();
             final String[] line = new String[length];
             for (int i = 1; i <= length; i++)
                 line[i - 1] = rs.getString(i);
             cw.writeNext(line, true);
         }
 
     }
 
 }
