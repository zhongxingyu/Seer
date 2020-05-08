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
 
 package com.gooddata.connector.driver;
 
 import com.gooddata.connector.model.PdmColumn;
 import com.gooddata.connector.model.PdmSchema;
 import com.gooddata.connector.model.PdmTable;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.DLIPart;
 import com.gooddata.naming.N;
 import com.gooddata.util.JdbcUtil;
 import com.gooddata.util.StringUtil;
 import org.apache.log4j.Logger;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 
 /**
  * GoodData Derby SQL driver. Generates the DDL (tables and indexes), DML (transformation SQL) and other
  * SQL statements necessary for the data normalization (lookup generation)
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class MySqlDriver extends AbstractSqlDriver implements SqlDriver {
 
     private static Logger l = Logger.getLogger(MySqlDriver.class);
 
  /**
      * Default constructor
      */
     public MySqlDriver() {
         // autoincrement syntax
         SYNTAX_AUTOINCREMENT = "AUTO_INCREMENT";
         SYNTAX_CONCAT_FUNCTION_PREFIX = "CONCAT(";
         SYNTAX_CONCAT_FUNCTION_SUFFIX = ")";
         SYNTAX_CONCAT_OPERATOR = ",'" + HASH_SEPARATOR + "',";
     }
     
     
     /**
      * {@inheritDoc}
      */
     public void executeExtractSql(Connection c, PdmSchema schema, String file) throws SQLException {
         l.debug("Extracting data.");
         PdmTable sourceTable = schema.getSourceTable();
         String source = sourceTable.getName();
         String cols = getNonAutoincrementColumns(sourceTable);
         JdbcUtil.executeUpdate(c,"ALTER TABLE "+source+" DISABLE KEYS");
 
         JdbcUtil.executeUpdate(c,
             "LOAD DATA INFILE '" + file + "' INTO TABLE " + source + " CHARACTER SET UTF8 "+
             "COLUMNS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\\n' (" + cols + ")"
         );
         JdbcUtil.executeUpdate(c,"ALTER TABLE "+source+" ENABLE KEYS");
         l.debug("Finished extracting data.");
     }
 
     /**
      * {@inheritDoc}
      */
     public void executeLoadSql(Connection c, PdmSchema schema, DLIPart part, String dir, int[] snapshotIds)
             throws SQLException {
         l.debug("Unloading data.");
         String file = dir + System.getProperty("file.separator") + part.getFileName();
         String cols = getLoadColumns(part, schema);
         String whereClause = getLoadWhereClause(part, schema, snapshotIds);
         String dliTable = getTableNameFromPart(part);
         Statement s = null;
         ResultSet rs = null;
         try {
             s = c.createStatement();
             String sql = "SELECT " + cols + " INTO OUTFILE '" + file +
 	            "' FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '\"' LINES TERMINATED BY '\\n' FROM " +
 	            dliTable + whereClause;
             rs = JdbcUtil.executeQuery(s, sql);
         }
         finally {
             if (rs != null)
                 rs.close();
             if (s != null)
                 s.close();
         }
         l.debug("Data unloading finished.");
     }
 
     /**
      * {@inheritDoc}
      */
     protected String decorateFactColumnForLoad(String cols, Column cl, String table) {
         if (cols.length() > 0)
            cols += ",ATOD(" + table.toUpperCase() + "." +
                     StringUtil.formatShortName(cl.getName())+")";
         else
            cols +=  "ATOD(" + table.toUpperCase() + "." +
                     StringUtil.formatShortName(cl.getName())+")";
         return cols;
     }
 
     /**
      * {@inheritDoc}
      */
     protected void insertFactsToFactTable(Connection c, PdmSchema schema) throws SQLException {
         PdmTable factTable = schema.getFactTable();
         PdmTable sourceTable = schema.getSourceTable();
         String fact = factTable.getName();
         String source = sourceTable.getName();
         String factColumns = "";
         String sourceColumns = "";
         for(PdmColumn column : factTable.getFactColumns()) {
             factColumns += "," + column.getName();
             sourceColumns += "," + column.getSourceColumn();
         }
 
         for(PdmColumn column : factTable.getDateColumns()) {
             factColumns += "," + column.getName();
             sourceColumns += ",IFNULL(DATEDIFF(STR_TO_DATE(" + column.getSourceColumn() + ",'" +
                     StringUtil.convertJavaDateFormatToMySql(column.getFormat())+"'),'1900-01-01'),2147483646)+1";
         }
         JdbcUtil.executeUpdate(c,
             "INSERT INTO "+fact+"("+N.ID+factColumns+") SELECT "+ N.SRC_ID + sourceColumns +
             " FROM " + source + " WHERE "+N.SRC_ID+" > (SELECT MAX(lastid) FROM snapshots WHERE name='"+fact+"')"
         );
     }
 
     /**
      * {@inheritDoc}
      */
     protected void createFunctions(Connection c) throws SQLException {
         l.debug("Creating system functions.");
     	String sql = "CREATE FUNCTION ATOD(str varchar(255)) RETURNS DECIMAL(15,4) "
 			    + "RETURN CASE "
 			    + "      WHEN str = '' THEN NULL "
 			    + "      ELSE CAST( ";
     	for (final String s : Constants.DISCARD_CHARS) {
     		sql += "REPLACE(";
     	}
     	sql += "str";
     	for (final String s : Constants.DISCARD_CHARS) {
     		sql += ", '" + s + "', '')";
     	}
 		sql +=  "           AS DECIMAL(15,4)) "
 			  + "   END";
 
         JdbcUtil.executeUpdate(c, sql);
         l.debug("System functions creation finished.");
     }
 }
