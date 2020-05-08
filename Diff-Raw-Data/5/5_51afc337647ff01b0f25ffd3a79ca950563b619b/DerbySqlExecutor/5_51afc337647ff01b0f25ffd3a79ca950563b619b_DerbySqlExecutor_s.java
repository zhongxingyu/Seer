 package com.gooddata.transformation.executor;
 
 import com.gooddata.exceptions.ModelException;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.DLIPart;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.transformation.executor.model.PdmColumn;
 import com.gooddata.transformation.executor.model.PdmSchema;
 import com.gooddata.transformation.executor.model.PdmTable;
 import com.gooddata.util.JdbcUtil;
 import com.gooddata.util.StringUtil;
 import org.apache.log4j.Logger;
 import org.gooddata.transformation.executor.AbstractSqlExecutor;
 import org.gooddata.transformation.executor.SqlExecutor;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import java.util.*;
 
 /**
  * GoodData Derby SQL executor. Generates the DDL (tables and indexes), DML (transformation SQL) and other
  * SQL statements necessary for the data normalization (lookup generation)
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class DerbySqlExecutor extends AbstractSqlExecutor implements SqlExecutor {
 
     private static Logger l = Logger.getLogger(DerbySqlExecutor.class);
 
     // separates the different LABELs when we concatenate them to create an unique identifier out of them
     protected static final String HASH_SEPARATOR = "%";
     // Derby SQL concat operator to merge LABEL content
     protected static final String CONCAT_OPERATOR = " || '" + HASH_SEPARATOR + "' || ";
 
 
     /**
      * Executes the system DDL initialization
      * @param c JDBC connection
      * @param schema the PDM schema
      * @throws ModelException if there is a problem with the PDM schema (e.g. multiple source or fact tables)
      * @throws SQLException in case of db problems
      */
     public void executeSystemDdlSql(Connection c, PdmSchema schema) throws ModelException, SQLException {
         JdbcUtil.executeUpdate(c,
             "CREATE FUNCTION ATOD(str VARCHAR(255)) RETURNS DOUBLE\n" +
             " PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA" +
             " EXTERNAL NAME 'com.gooddata.derby.extension.DerbyExtensions.atod'"
         );
 
         JdbcUtil.executeUpdate(c,
             "CREATE FUNCTION DTTOI(str VARCHAR(255), fmt VARCHAR(30)) RETURNS INT\n" +
             " PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA" +
             " EXTERNAL NAME 'com.gooddata.derby.extension.DerbyExtensions.dttoi'"
         );
 
         JdbcUtil.executeUpdate(c,
             "CREATE TABLE snapshots (" +
                " id INT GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)," +
                 " name VARCHAR(255)," +
                 " tmstmp BIGINT," +
                 " firstid INT," +
                 " lastid INT," +
                 " PRIMARY KEY (id)" +
                 ")"
         );
     }
 
     /**
      * Executes the DDL initialization
      * @param c JDBC connection
      * @param schema the PDM schema
      * @throws ModelException if there is a problem with the PDM schema (e.g. multiple source or fact tables)
      * @throws SQLException in case of db problems 
      */
     public void executeDdlSql(Connection c, PdmSchema schema) throws ModelException, SQLException {
 
         String sql = "";
         // indexes creation script
         for(PdmTable table : schema.getTables()) {
             List<String> isql = new ArrayList<String>();
             String pk = "";
             sql += "CREATE TABLE " + table.getName() + " (\n";
             for( PdmColumn column : table.getColumns()) {
                 sql += " "+ column.getName() + " " + column.getType();
                 if(column.isUnique())
                     sql += " UNIQUE";
                 if(column.isAutoIncrement())
                    sql += " GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)";
                 if(column.isPrimaryKey())
                     if(pk != null && pk.length() > 0)
                         pk += "," + column.getName();
                     else
                         pk += column.getName();
                 sql += ",";
                 if(PdmTable.PDM_TABLE_TYPE_SOURCE.equals(table.getType())) {
                     if(!"o_genid".equals(column.getName()))
                         isql.add("CREATE INDEX idx_" + table.getName() + "_" + column.getName() + " ON " +
                               table.getName() + "("+column.getName()+")");
                 }
                 /* There is an UNIQUE index on the hashid already
                 if(PdmTable.PDM_TABLE_TYPE_LOOKUP.equals(table.getType())) {
                     if("hashid".equals(column.getName()))
                         isql += "CREATE INDEX idx_" + table.getName() + "_" + column.getName() + " ON " +
                               table.getName() + "("+column.getName()+")";
                 }
                 */
             }
             sql += " PRIMARY KEY (" + pk + "))";
 
             JdbcUtil.executeUpdate(c, sql);
 
             for(String s : isql) {
                 JdbcUtil.executeUpdate(c, s);
             }
             sql = "";
         }
 
         JdbcUtil.executeUpdate(c,
             "INSERT INTO snapshots(name,firstid,lastid,tmstmp) VALUES ('" + schema.getFactTable().getName() + "',0,0,0)"
         );
 
     }
 
 
     /**
      * Executes the data normalization script
      * @param c JDBC connection
      * @param schema the PDM schema
      * @throws ModelException if there is a problem with the PDM schema (e.g. multiple source or fact tables)
      * @throws SQLException in case of db problems
      */
     public void executeNormalizeSql(Connection c, PdmSchema schema) throws ModelException, SQLException {
         // fact table INSERT statement components
         String factInsertFromClause = schema.getSourceTable().getName();
         String factInsertWhereClause = "";
         PdmTable factTable = schema.getFactTable();
         for(PdmTable lookupTable : schema.getLookupTables()) {
             // INSERT tbl(insertColumns) SELECT nestedSelectColumns FROM nestedSelectFromClause
             // WHERE nestedSelectWhereClause  
             String insertColumns = "hashid";
             // fact table cols
             String nestedSelectColumns = "";
             // new fact table insert's nested select from
             String nestedSelectFromClause = "";
             // concatenate all representing columns to create a unique hashid
             String concatenatedRepresentingColumns = "";
             // new fact table insert's nested select where
             String nestedSelectWhereClause = "";
             Set<PdmTable> factInsertFromClauseTables = new HashSet<PdmTable>(); 
             for(PdmColumn column : lookupTable.getRepresentingColumns()) {
                 insertColumns += "," + column.getName();
                 nestedSelectColumns += "," + column.getSourceColumn();
                 // if there are LABELS, the lookup can't be added twice to the FROM clause
                 if(!factInsertFromClauseTables.contains(lookupTable)) {
                     factInsertFromClause += "," + lookupTable.getName();
                     factInsertFromClauseTables.add(lookupTable);                    
                 }
                 if(concatenatedRepresentingColumns.length() > 0)
                     concatenatedRepresentingColumns += CONCAT_OPERATOR +  column.getSourceColumn();
                 else
                     concatenatedRepresentingColumns = column.getSourceColumn();
             }
 
             if(factInsertWhereClause.length() > 0)
                 factInsertWhereClause += " AND " + concatenatedRepresentingColumns + "=" + lookupTable.getName() + ".hashid";
             else
                 factInsertWhereClause += concatenatedRepresentingColumns + "=" + lookupTable.getName() + ".hashid";
 
             // add the concatenated columns that fills the hashid to the beginning
             nestedSelectColumns = concatenatedRepresentingColumns + nestedSelectColumns;
 
             JdbcUtil.executeUpdate(c,
                     "INSERT INTO " + lookupTable.getName() + "(" + insertColumns +
                       ") SELECT DISTINCT " + nestedSelectColumns + " FROM " + schema.getSourceTable().getName() +
                       " WHERE o_genid > (SELECT MAX(lastid) FROM snapshots WHERE name='" + factTable.getName() +
                        "') AND " + concatenatedRepresentingColumns + " NOT IN (SELECT hashid FROM " +
                        lookupTable.getName() + ")"
             );
             
         }
 
         String insertColumns = "";
         String nestedSelectColumns = "";
         for(PdmColumn factTableColumn : factTable.getColumns()) {
             if(insertColumns.length() > 0) {
                 insertColumns += "," + factTableColumn.getName();
                 if(SourceColumn.LDM_TYPE_DATE.equals(factTableColumn.getLdmTypeReference()))
                     nestedSelectColumns += ",DTTOI(" + factTableColumn.getSourceColumn() + ",'" +
                                            factTableColumn.getFormat()+"')";
                 else
                     nestedSelectColumns += "," + factTableColumn.getSourceColumn();
             }
             else {
                 insertColumns += factTableColumn.getName();
                 if(SourceColumn.LDM_TYPE_DATE.equals(factTableColumn.getLdmTypeReference()))
                     nestedSelectColumns += "DTTOI(" + factTableColumn.getSourceColumn() + ",'" + 
                                            factTableColumn.getFormat()+"')";
                 else
                     nestedSelectColumns += factTableColumn.getSourceColumn();
             }
         }
 
         if (factInsertWhereClause.length() > 0)
             factInsertWhereClause += " AND " + schema.getSourceTable().getName() +
                                      ".o_genid > (SELECT MAX(lastid) FROM snapshots WHERE name='" +
                                      factTable.getName() + "')";
         else
             factInsertWhereClause += schema.getSourceTable().getName() +
                                      ".o_genid > (SELECT MAX(lastid) FROM snapshots WHERE name='" + 
                                      factTable.getName() + "')";
 
         //script += "DELETE FROM snapshots WHERE name = '" + factTable.getName() + "' AND lastid = 0;\n\n";
         Date dt = new Date();
 
         JdbcUtil.executeUpdate(c,
             "INSERT INTO snapshots(name,tmstmp,firstid) SELECT '" + factTable.getName() + "'," + dt.getTime() +
             ",MAX(id)+1 FROM " + factTable.getName()
         );
         JdbcUtil.executeUpdate(c,
             "UPDATE snapshots SET firstid = 0 WHERE name = '" + factTable.getName() +
             "' AND firstid IS NULL"
         );
 
         JdbcUtil.executeUpdate(c,
             "INSERT INTO " + factTable.getName() + "(" + insertColumns + ") SELECT " + nestedSelectColumns +
             " FROM " + factInsertFromClause + " WHERE " + factInsertWhereClause
         );
 
         JdbcUtil.executeUpdate(c,
             "UPDATE snapshots SET lastid = (SELECT MAX(id) FROM " + factTable.getName() + ") WHERE name = '" +
             factTable.getName() + "' AND lastid IS NULL"
         );
         JdbcUtil.executeUpdate(c,
             "UPDATE snapshots SET lastid = 0 WHERE name = '" + factTable.getName() +
             "' AND lastid IS NULL"
         );
 
     }
 
     /**
      * Executes the Derby SQL that extracts the data from a CSV file to the normalization database
      * @param c JDBC connection
      * @param schema the PDM schema
      * @throws ModelException in case when there is a problem with the PDM model
      * @throws SQLException in case of db problems
      */
     public void executeExtractSql(Connection c, PdmSchema schema, String file) throws ModelException, SQLException {
         String cols = "";
         PdmTable sourceTable = schema.getSourceTable();
         for (PdmColumn col : sourceTable.getColumns()) {
             if(!col.isAutoIncrement())
                 if (cols != null && cols.length() > 0)
                     cols += "," + StringUtil.formatShortName( col.getName());
                 else
                     cols += StringUtil.formatShortName(col.getName());
         }
 
         JdbcUtil.executeUpdate(c,
             "CALL SYSCS_UTIL.SYSCS_IMPORT_DATA " +
             "(NULL, '" + sourceTable.getName().toUpperCase() + "', '" + cols.toUpperCase() +
             "', null, '" + file + "', null, null, 'utf-8',0)"
         );
         
     }
 
     /**
      * Executes the Derby SQL that unloads the data from the normalization database to a CSV
      * @param c JDBC connection
      * @param part DLI part
      * @param dir target directory
      * @param snapshotIds specific snapshots IDs that will be integrated
      * @throws SQLException in case of db problems 
      */
     public void executeLoadSql(Connection c, PdmSchema schema, DLIPart part, String dir, int[] snapshotIds)
             throws ModelException, SQLException {
         String sql = "";
         int rc = 0;
         
         String file = dir + System.getProperty("file.separator") + part.getFileName();
         String dliTable = StringUtil.formatShortName(part.getFileName().split("\\.")[0]);
 
         PdmTable pdmTable = schema.getTableByName(dliTable);
 
         List<Column> columns = part.getColumns();
         String cols = "";
         for (Column cl : columns) {
             PdmColumn col = pdmTable.getColumnByName(cl.getName());
             // fact table fact columns
             if(PdmTable.PDM_TABLE_TYPE_FACT.equals(pdmTable.getType()) &&
                     SourceColumn.LDM_TYPE_FACT.equals(col.getLdmTypeReference())) {
                 if (cols != null && cols.length() > 0)
                     cols += ",ATOD(" + dliTable.toUpperCase() + "." +
                             StringUtil.formatShortName(cl.getName())+")";
                 else
                     cols +=  "ATOD(" + dliTable.toUpperCase() + "." +
                             StringUtil.formatShortName(cl.getName())+")";
             }
             // lookup table name column
             else if (PdmTable.PDM_TABLE_TYPE_LOOKUP.equals(pdmTable.getType()) && 
                     SourceColumn.LDM_TYPE_ATTRIBUTE.equals(col.getLdmTypeReference())) {
                 if (cols != null && cols.length() > 0)
                     cols += ",CAST(" + dliTable.toUpperCase() + "." + StringUtil.formatShortName(cl.getName())+
                             " AS VARCHAR(128))";
                 else
                     cols +=  "CAST("+dliTable.toUpperCase() + "." + StringUtil.formatShortName(cl.getName())+
                             " AS VARCHAR(128))";
             }
             else {
                 if (cols != null && cols.length() > 0)
                     cols += "," + dliTable.toUpperCase() + "." + StringUtil.formatShortName(cl.getName());
                 else
                     cols +=  dliTable.toUpperCase() + "." + StringUtil.formatShortName(cl.getName());
             }
         }
         String whereClause = "";
         if(PdmTable.PDM_TABLE_TYPE_FACT.equals(pdmTable.getType()) && snapshotIds != null && snapshotIds.length > 0) {
             String inClause = "";
             for(int i : snapshotIds) {
                 if(inClause.length()>0)
                     inClause += ","+i;
                 else
                     inClause = "" + i;
             }
             whereClause = ",SNAPSHOTS WHERE " + dliTable.toUpperCase() +
                     ".ID BETWEEN SNAPSHOTS.FIRSTID and SNAPSHOTS.LASTID AND SNAPSHOTS.ID IN (" + inClause + ")";
         }
 
         JdbcUtil.executeUpdate(c,
             "CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY " +
             "('SELECT " + cols + " FROM " + dliTable.toUpperCase() + whereClause + "', '" + file +
             "', null, null, 'utf-8')"
         );
 
     }
 
 }
