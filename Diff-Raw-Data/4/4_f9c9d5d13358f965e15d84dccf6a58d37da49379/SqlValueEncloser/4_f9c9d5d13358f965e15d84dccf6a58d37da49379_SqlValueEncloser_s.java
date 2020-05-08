 package edu.uw.dbdiff;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Provides the String.format() format for a given SQL type.
  * The ( character is changed to _ when matching strings to Enums.
  */
 public enum SqlValueEncloser {
     SINGLE_QUOTE("'%s'", Arrays.asList(new String[]{"VARCHAR2", "VARCHAR", "CHAR"})),
    TIMESTAMP("ts{%s}", Arrays.asList(new String[]{"TIMESTAMP", "TIMESTAMP(6)"})),
    DATE("%s", Arrays.asList(new String[]{"DATE"})),
     NO_QUOTES("%s", Arrays.asList(new String[]{"NUMBER"}));
 
     private List<String> types;
     private String format;
 
     private SqlValueEncloser(String format, List<String> types) {
         this.format = format;
         this.types = types;
     }
 
     public static String findFormat(String sqlType) {
         for (SqlValueEncloser encloser: SqlValueEncloser.values()) {
             if (encloser.types.contains(sqlType)) {
                 return encloser.format;
             }
         }
         return null;
     }
 }
