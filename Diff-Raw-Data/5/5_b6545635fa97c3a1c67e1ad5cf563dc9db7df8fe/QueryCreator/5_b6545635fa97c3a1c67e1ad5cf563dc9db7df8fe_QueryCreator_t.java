 package org.narwhal.query;
 
 
 public abstract class QueryCreator {
 
 
     /**
      * Makes prepared INSERT SQL statement by using the table name.
      *
      * @param tableName String representation of the table name that maps to the particular entity.
      * @return String representation of the INSERT SQL statement.
      * */
     public String makeInsertQuery(String tableName, String[] columns, String primaryColumnName) {
         StringBuilder builder = new StringBuilder("INSERT INTO ");
         builder.append(tableName);
         builder.append(" VALUES (");
 
         for (int i = 0; i < columns.length; ++i) {
             if (i > 0) {
                 builder.append(',');
             }
 
             builder.append('?');
         }
         builder.append(')');
 
         return builder.toString();
     }
 
     /**
      * Makes prepared SELECT SQL statement by using the table name.
      *
      * @param tableName String representation of the table name that maps to the particular entity.
      * @return String representation of the SELECT SQL statement.
      * */
     public String makeSelectQuery(String tableName, String[] columns, String primaryColumnName) {
         StringBuilder builder = new StringBuilder("SELECT ");
        
         for (int i = 0; i < columns.length; ++i) {
             if (i > 0) {
                 builder.append(',');
             }
 
             builder.append(columns[i]);
         }
 
         builder.append(" FROM ");
         builder.append(tableName);
         builder.append(" WHERE ");
         builder.append(primaryColumnName);
         builder.append(" = ?");
 
         return builder.toString();
     }
 
     /**
      * Makes prepared DELETE SQL statement by using the table name.
      *
      * @param tableName String representation of the table name that maps to the particular entity.
      * @return String representation of the DELETE SQL statement.
      * */
     public String makeDeleteQuery(String tableName, String primaryColumnName) {
         StringBuilder builder = new StringBuilder("DELETE FROM ");
         builder.append(tableName);
         builder.append(" WHERE ");
         builder.append(primaryColumnName);
         builder.append(" = ?");
 
         return builder.toString();
     }
 
     /**
      * Makes prepared UPDATE SQL statement by using the table name.
      *
      * @param tableName String representation of the table name that maps to the particular entity.
      * @return String representation of the UPDATE SQL statement.
      * */
     public String makeUpdateQuery(String tableName, String[] columns, String primaryColumnName) {
         StringBuilder builder = new StringBuilder("UPDATE ");
         builder.append(tableName);
         builder.append(" SET ");
 
         for (int i = 0; i < columns.length; ++i) {
             if (i > 0) {
                 builder.append(',');
             }
 
             builder.append(columns[i]);
             builder.append(" = ?");
         }
         builder.append(" WHERE ");
         builder.append(primaryColumnName);
         builder.append(" = ?");
 
         return builder.toString();
     }
 }
