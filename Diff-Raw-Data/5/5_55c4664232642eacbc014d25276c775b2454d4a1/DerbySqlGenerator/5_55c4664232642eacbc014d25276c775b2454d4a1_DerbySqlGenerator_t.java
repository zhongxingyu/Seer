 package com.gooddata.transformation.generator;
 
 import com.gooddata.exceptions.ModelException;
 import com.gooddata.integration.model.Column;
 import com.gooddata.integration.model.DLIPart;
 import com.gooddata.modeling.model.SourceColumn;
 import com.gooddata.modeling.model.SourceSchema;
 import com.gooddata.transformation.generator.model.PdmColumn;
 import com.gooddata.transformation.generator.model.PdmSchema;
 import com.gooddata.transformation.generator.model.PdmTable;
 import com.gooddata.util.StringUtil;
 
 import java.util.Date;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * GoodData Derby SQL generator. Generates the DDL (tables and indexes), DML (transformation SQL) and other
  * SQL statements necessary for the data normalization (lookup generation)
  * @author zd <zd@gooddata.com>
  * @version 1.0
  */
 public class DerbySqlGenerator {
 
     // separates the different LABELs when we concatenate them to create an unique identifier out of them
     protected static final String HASH_SEPARATOR = "%";
     // Derby SQL concat operator to merge LABEL content
     protected static final String CONCAT_OPERATOR = " || '" + HASH_SEPARATOR + "' || ";
 
 
     /**
      * Generates the DDL initialization
      * @param schema the PDM schema
      * @return the SQL script
      * @throws ModelException if there is a problem with the PDM schema (e.g. multiople source or fact tables)
      */
     public String generateDdlSql(PdmSchema schema) throws ModelException {
         String script = "CREATE FUNCTION ATOD(str VARCHAR(255)) RETURNS DOUBLE\n" +
                 " PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA" +
                 " EXTERNAL NAME 'com.gooddata.derby.extension.DerbyExtensions.atod';\n\n";
         script += "CREATE FUNCTION DTTOI(str VARCHAR(255), fmt VARCHAR(30)) RETURNS INT\n" +
                 " PARAMETER STYLE JAVA NO SQL LANGUAGE JAVA" +
                 " EXTERNAL NAME 'com.gooddata.derby.extension.DerbyExtensions.dttoi';\n\n";
         // indexes creation script
         for(PdmTable table : schema.getTables()) {
             String iscript = "";
             String pk = "";
             script += "CREATE TABLE " + table.getName() + " (\n";
             for( PdmColumn column : table.getColumns()) {
                 script += " "+ column.getName() + " " + column.getType();
                 if(column.isAutoIncrement())
                     script += " GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1)";
                 if(column.isUnique())
                     script += " UNIQUE";
                 if(column.isPrimaryKey())
                     if(pk != null && pk.length() > 0)
                         pk += "," + column.getName();
                     else
                         pk += column.getName();
                 script += ",\n";
                 if(PdmTable.PDM_TABLE_TYPE_SOURCE.equals(table.getType())) {
                     if(!"o_genid".equals(column.getName()))
                         iscript += "CREATE INDEX idx_" + table.getName() + "_" + column.getName() + " ON " +
                               table.getName() + "("+column.getName()+");\n\n";
                 }
                 /* There is an UNIQUE index on the hashid already
                 if(PdmTable.PDM_TABLE_TYPE_LOOKUP.equals(table.getType())) {
                     if("hashid".equals(column.getName()))
                         iscript += "CREATE INDEX idx_" + table.getName() + "_" + column.getName() + " ON " +
                               table.getName() + "("+column.getName()+");\n\n";
                 }
                 */
             }
             script += " PRIMARY KEY (" + pk + ")\n);\n\n" + iscript;
         }
         script += "INSERT INTO snapshots(name,firstid,lastid,tmstmp) VALUES ('" + schema.getFactTable().getName() +
                       "',0,0,0);\n\n";
         return script;
     }
 
     /**
      * Generates the data normalization script
      * @param schema the PDM schema
      * @return the SQL script
      * @throws ModelException if there is a problem with the PDM schema (e.g. multiople source or fact tables)
      */
     public String generateNormalizeSql(PdmSchema schema) throws ModelException {
         String script = "";
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
 
             script += "INSERT INTO " + lookupTable.getName() + "(" + insertColumns +
                       ") SELECT DISTINCT " + nestedSelectColumns + " FROM " + schema.getSourceTable().getName() +
                       " WHERE o_genid > (SELECT MAX(lastid) FROM snapshots WHERE name='" + factTable.getName() +
                        "') AND " + concatenatedRepresentingColumns + " NOT IN (SELECT hashid FROM " +
                        lookupTable.getName() + ");\n\n";
         }
 
         String insertColumns = "";
         String nestedSelectColumns = "";
         for(PdmColumn factTableColumn : factTable.getColumns()) {
             if(insertColumns.length() >0) {
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
         script += "INSERT INTO snapshots(name,tmstmp,firstid) SELECT '" + factTable.getName() + "'," + dt.getTime()
                    + ",MAX(id)+1 FROM " + factTable.getName() + ";\n\n";
         script += "UPDATE snapshots SET firstid = 0 WHERE name = '" + factTable.getName() + "' AND firstid IS NULL;\n\n";
         
         script += "INSERT INTO " + factTable.getName() + "(" + insertColumns + ") SELECT " + nestedSelectColumns +
                   " FROM " + factInsertFromClause + " WHERE " + factInsertWhereClause + ";\n\n";
 
 
         script += "UPDATE snapshots SET lastid = (SELECT MAX(id) FROM " + factTable.getName() + ") WHERE name = '" +
                   factTable.getName() + "' AND lastid IS NULL;\n\n";
         script += "UPDATE snapshots SET lastid = 0 WHERE name = '" + factTable.getName() + "' AND lastid IS NULL;\n\n";
 
         return script;
     }
 
     /**
      * Generates the Derby SQL that extracts the data from a CSV file to the normalization database
      * @param schema the PDM schema
      * @return the SQL script
      * @throws ModelException in case when there is a problem with the PDM model
      */
     public String generateExtractSql(PdmSchema schema, String file) throws ModelException {
         String cols = "";
         PdmTable sourceTable = schema.getSourceTable();
         for (PdmColumn c : sourceTable.getColumns()) {
             if(!c.isAutoIncrement())
                 if (cols != null && cols.length() > 0)
                     cols += "," + StringUtil.formatShortName( c.getName());
                 else
                     cols += StringUtil.formatShortName(c.getName());
         }
         return "CALL SYSCS_UTIL.SYSCS_IMPORT_DATA " +
                 "(NULL, '" + sourceTable.getName().toUpperCase() + "', '" + cols.toUpperCase() + 
                "', null, '" + file + "', null, null, 'utf-8',0);\n\n";
     }
 
     /**
      * Generates the Derby SQL that unloads the data from the normalization database to a CSV
      * @param part DLI part
      * @param dir target directory
      * @param snapshotIds specific snapshots IDs that will be integrated
      * @return the SQL script
      */
     public String generateLoadSql(PdmSchema schema, DLIPart part, String dir, int[] snapshotIds) throws ModelException {
         String file = dir + System.getProperty("file.separator") + part.getFileName();
         String dliTable = StringUtil.formatShortName(part.getFileName().split("\\.")[0]);
 
         PdmTable pdmTable = schema.getTableByName(dliTable);
 
         List<Column> columns = part.getColumns();
         String cols = "";
         for (Column c : columns) {
             PdmColumn col = pdmTable.getColumnByName(c.getName());
             // fact table fact columns
             if(PdmTable.PDM_TABLE_TYPE_FACT.equals(pdmTable.getType()) &&
                     SourceColumn.LDM_TYPE_FACT.equals(col.getLdmTypeReference())) {
                 if (cols != null && cols.length() > 0)
                     cols += ",ATOD(" + dliTable.toUpperCase() + "." +
                             StringUtil.formatShortName(c.getName())+")";
                 else
                     cols +=  "ATOD(" + dliTable.toUpperCase() + "." +
                             StringUtil.formatShortName(c.getName())+")";
             }
             // lookup table name column
             else if (PdmTable.PDM_TABLE_TYPE_LOOKUP.equals(pdmTable.getType()) && 
                     SourceColumn.LDM_TYPE_ATTRIBUTE.equals(col.getLdmTypeReference())) {
                 if (cols != null && cols.length() > 0)
                     cols += ",CAST(" + dliTable.toUpperCase() + "." + StringUtil.formatShortName(c.getName())+
                             " AS VARCHAR(128))";
                 else
                     cols +=  "CAST("+dliTable.toUpperCase() + "." + StringUtil.formatShortName(c.getName())+
                             " AS VARCHAR(128))";
             }
             else {
                 if (cols != null && cols.length() > 0)
                     cols += "," + dliTable.toUpperCase() + "." + StringUtil.formatShortName(c.getName());
                 else
                     cols +=  dliTable.toUpperCase() + "." + StringUtil.formatShortName(c.getName());
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
         return "CALL SYSCS_UTIL.SYSCS_EXPORT_QUERY " +
                 "('SELECT " + cols + " FROM " + dliTable.toUpperCase() + whereClause + "', '" + file
                + "', null, null, 'utf-8');\n\n";
     }
 
 }
