 package edu.northwestern.bioinformatics.bering.dialect;
 
 import static edu.northwestern.bioinformatics.bering.SqlUtils.sqlLiteral;
 import org.apache.ddlutils.model.Table;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * @author Rhett Sutphin
  */
 public class Oracle extends Generic {
     public List<String> createTable(Table table) {
         List<String> statments = new ArrayList<String>(2);
        if (hasPrimaryKey(table)) statments.add("CREATE SEQUENCE " + createIdSequenceName(table));
        statments.addAll(super.createTable(massageTableForOracle(table)));
         return statments;
     }
 
     public List<String> dropTable(Table table) {
         List<String> statments = new ArrayList<String>(2);
         statments.addAll(super.dropTable(massageTableForOracle(table)));
         if (hasPrimaryKey(table)) statments.add("DROP SEQUENCE " + createIdSequenceName(table));
         return statments;
     }
 
     public List<String> setDefaultValue(String table, String column, String newDefault) {
         return Arrays.asList(String.format(
             "ALTER TABLE %s MODIFY (%s DEFAULT %s)", table, column, sqlLiteral(newDefault)
         ));
     }
 
     public List<String> setNullable(String table, String column, boolean nullable) {
         return Arrays.asList(String.format(
             "ALTER TABLE %s MODIFY (%s %sNULL)", table, column, nullable ? "" : "NOT "
         ));
     }
 
     private String createIdSequenceName(Table table) {
         int maxlen = getPlatform().getPlatformInfo().getMaxIdentifierLength();
         return "seq_" + truncate(table.getName(), maxlen - 7) + "_id";
     }
 
     private String truncate(String str, int maxlen) {
         if (str.length() <= maxlen) return str;
         return str.substring(0, maxlen);
     }
 
     private Table massageTableForOracle(Table table) {
         if (hasPrimaryKey(table)) {
             table.getPrimaryKeyColumns()[0].setAutoIncrement(false);
         }
         return table;
     }
 
     private boolean hasPrimaryKey(Table table) {
         return table.getPrimaryKeyColumns().length > 0;
     }
 }
