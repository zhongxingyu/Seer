 package liquibase.ext.modifycolumn;
 
 import liquibase.change.ColumnConfig;
import liquibase.statement.SqlStatement;
 
public class ModifyColumnStatement implements SqlStatement {
     private String schemaName;
     private String tableName;
     private ColumnConfig[] columns;
 
     public ModifyColumnStatement(String schemaName, String tableName, ColumnConfig... columns) {
         this.schemaName = schemaName;
         this.tableName = tableName;
         this.columns = columns;
     }
 
     public String getSchemaName() {
         return schemaName;
     }
 
     public String getTableName() {
         return tableName;
     }
 
     public ColumnConfig[] getColumns() {
         return columns;
     }
 }
