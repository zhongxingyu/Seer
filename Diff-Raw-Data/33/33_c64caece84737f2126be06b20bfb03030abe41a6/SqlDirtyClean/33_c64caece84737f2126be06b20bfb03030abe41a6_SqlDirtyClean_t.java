 package com.digitolio.jdbi.codegen.codemodel;
 
 import com.digitolio.jdbi.auto.SqlSupport;
import com.digitolio.jdbi.table.Column;
 import com.digitolio.jdbi.table.Table;
 import org.skife.jdbi.v2.Binding;
 
import java.util.List;

 /**
  * UPDATE BASIC_PROFILE SET DIRTY = 0 WHERE USER_ID = :userId
  *
  * @author Cemo
  */
 public class SqlDirtyClean extends SqlSupport {
 
     private String clause;
 
     public SqlDirtyClean(Table table) {
         super(table);
         initConstantSqls();
     }
 
     private void initConstantSqls() {
         clause = initAllClause();
     }
 
     private String initAllClause() {
         return initUpdatePart() + " SET DIRTY = 0 " + initWherePart();
     }
 
     private String initUpdatePart() {
         return "UPDATE ".concat(table.getTableName());
     }
 
     private String initWherePart() {
        List<Column> primaryKeys = table.getPrimaryKeyColumns();
        StringBuilder builder = new StringBuilder(" WHERE ");
        for (Column entry : primaryKeys) {
            builder.append(entry.getDatabaseName()).append(" = :").append(entry.getFieldName()).append(" AND ");
        }
        return builder.substring(0,builder.length() - 5);
     }
 
     @Override
     public String generate(Binding params) {
         return clause;
     }
 }
