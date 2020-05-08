 
 package com.github.shimal.query_utils.sql;
 
 import com.github.shimal.query_utils.AliasAlreadyUsedException;
 import com.github.shimal.query_utils.Constrainable;
 import com.github.shimal.query_utils.Orderable;
 import com.github.shimal.query_utils.Querable;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 
 
 public class SqlQuery implements Querable {
 
 
 
     //~ --- [INSTANCE FIELDS] ------------------------------------------------------------------------------------------
 
     private String                  countParam;
     private List<Orderable>         orders;
     private String                  queryTableAlias;
     private String                  selectParam;
     private HashMap<String, String> tables;
     private Constrainable           topConstrainable;
 
 
 
     //~ --- [CONSTRUCTORS] ---------------------------------------------------------------------------------------------
 
     public SqlQuery(String table) {
 
         this(table, "S");
     }
 
 
 
     public SqlQuery(String table, String alias) {
 
         tables           = new HashMap<String, String>();
         orders           = new ArrayList<Orderable>();
         topConstrainable = null;
         countParam       = null;
         selectParam      = null;
         queryTableAlias  = alias;
 
         tables.put(alias, table);
     }
 
 
 
     //~ --- [METHODS] --------------------------------------------------------------------------------------------------
 
     @Override
     public Querable asc(String column) {
 
         this.orders.add(new SqlOrder(column, ORDER_ASCENDING));
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public String count() {
 
        String countPart = countParam == null ? "*" : countParam;
 
         return "SELECT COUNT(" + countPart + ") FROM " + generateSelectWoTable();
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public Querable desc(String column) {
 
         this.orders.add(new SqlOrder(column, ORDER_DESCENDING));
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public String getCountParam() {
 
         return countParam;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public String getSelectParam() {
 
         return selectParam;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     public SqlQuery join(String table, String alias) throws AliasAlreadyUsedException {
 
         if (tables.containsKey(alias)) {
             throw new AliasAlreadyUsedException();
         }
 
         tables.put(alias, table);
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     public SqlQuery join(String table, String alias, Constrainable constraint) throws AliasAlreadyUsedException {
 
         if (tables.containsKey(alias)) {
             throw new AliasAlreadyUsedException();
         }
 
         tables.put(alias, table);
         where(constraint);
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     public SqlQuery join(String table, String alias, Object leftSide, Object rightSide)
         throws AliasAlreadyUsedException {
 
         if (tables.containsKey(alias)) {
             throw new AliasAlreadyUsedException();
         }
 
         tables.put(alias, table);
         where(leftSide, rightSide);
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public Querable order(Orderable... orders) {
 
         this.orders.addAll(Arrays.asList(orders));
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public String select() {
 
         String selectPart = selectParam == null ? queryTableAlias + ".*" : selectParam;
 
         return "SELECT " + selectPart + " FROM " + generateSelectWoTable() + generateOrders();
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public void setCountParam(String countParam) {
 
         this.countParam = countParam;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public void setSelectParam(String selectParam) {
 
         this.selectParam = selectParam;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public Querable where(Constrainable constrainable) {
 
         if (topConstrainable == null) {
 
             if (constrainable instanceof SqlConstraint) {
                 topConstrainable = new SqlAnd(constrainable);
             } else {
                 topConstrainable = constrainable;
             }
         } else {
             topConstrainable.add(constrainable);
         }
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     @Override
     public Querable where(Object leftSide, Object rightSide) {
 
         SqlConstraint constraint = new SqlConstraint(leftSide, rightSide);
 
         if (topConstrainable == null) {
             topConstrainable = new SqlAnd(constraint);
         } else {
             topConstrainable.add(constraint);
         }
 
         return this;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     private String generateOrders() {
 
         String query = "";
 
         for (Orderable order : orders) {
             query += ", " + order.getColumn() + (order.getMethod() == ORDER_ASCENDING ? " ASC" : " DESC");
         }
 
         if (!query.isEmpty()) {
             query = " ORDER BY " + query.substring(2);
         }
 
         return query;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     private String generateSelectWoTable() {
 
         String query = "";
 
         for (String alias : tables.keySet()) {
             query += ", " + tables.get(alias) + " " + alias;
         }
 
         query = query.substring(2);
 
         if (topConstrainable != null && topConstrainable.size() > 0) {
             query += " WHERE " + wh(topConstrainable);
         }
 
         return query;
     }
 
 
 
     //~ ----------------------------------------------------------------------------------------------------------------
 
     private String wh(Constrainable constrainable) {
 
         String                  query      = "";
         Iterator<Constrainable> iterator   = constrainable.getIterator();
         String                  whereQuery = "";
 
         if (constrainable instanceof SqlConstraint) {
             SqlConstraint constraint = (SqlConstraint) constrainable;
 
             if (constraint.getOperator() == SqlConstraint.EQUAL) {
                 query += constraint.getLeftSide() + " = " + constraint.getRightSide();
             } else if (constraint.getOperator() == SqlConstraint.NOT_EQUAL) {
                 query += constraint.getLeftSide() + " != " + constraint.getRightSide();
             } else if (constraint.getOperator() == SqlConstraint.LIKE) {
                 query += constraint.getLeftSide() + " LIKE " + constraint.getRightSide();
             } else if (constraint.getOperator() == SqlConstraint.LIKE_LOWER) {
                 query += "LOWER(" + constraint.getLeftSide() + ") LIKE LOWER(" + constraint.getRightSide() + ")";
             } else if (constraint.getOperator() == SqlConstraint.LESS_THAN) {
                 query += constraint.getLeftSide() + " < " + constraint.getRightSide();
             } else if (constraint.getOperator() == SqlConstraint.LESS_THAN_OR_EQUAL) {
                 query += constraint.getLeftSide() + " <= " + constraint.getRightSide();
             } else if (constraint.getOperator() == SqlConstraint.GREATER_THAN) {
                 query += constraint.getLeftSide() + " > " + constraint.getRightSide();
             } else if (constraint.getOperator() == SqlConstraint.GREATER_THAN_OR_EQUAL) {
                 query += constraint.getLeftSide() + " >= " + constraint.getRightSide();
             }
         } else if (constrainable instanceof SqlAnd) {
 
             while (iterator.hasNext()) {
                 whereQuery += " AND " + wh(iterator.next());
             }
 
             query += "(" + whereQuery.substring(5) + ")";
         } else if (constrainable instanceof SqlOr) {
 
             while (iterator.hasNext()) {
                 whereQuery += " OR " + wh(iterator.next());
             }
 
             query += "(" + whereQuery.substring(4) + ")";
         }
 
         return query;
     }
 }
