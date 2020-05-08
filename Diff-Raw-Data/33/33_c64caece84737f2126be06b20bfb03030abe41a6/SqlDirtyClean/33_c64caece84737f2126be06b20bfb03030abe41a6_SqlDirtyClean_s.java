 package com.digitolio.jdbi.codegen.codemodel;
 
 import com.digitolio.jdbi.auto.SqlSupport;
 import com.digitolio.jdbi.table.Table;
 import org.skife.jdbi.v2.Binding;
 
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
        return " WHERE USER_ID = :userId";
     }
 
     @Override
     public String generate(Binding params) {
         return clause;
     }
 }
