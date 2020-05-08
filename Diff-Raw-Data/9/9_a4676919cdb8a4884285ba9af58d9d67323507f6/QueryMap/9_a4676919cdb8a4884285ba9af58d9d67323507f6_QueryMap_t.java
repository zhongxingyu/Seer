 package com.hxdcml.sql;
 
 import java.util.AbstractMap;
 import java.util.ArrayList;
 import java.util.Set;
 
 /**
  * User: Souleiman Ayoub
  * Date: 12/30/12
  * Time: 6:02 PM
  */
 public class QueryMap {
     private SQLMap map;
 
     public QueryMap() {
         map = new SQLMap();
     }
 
     public QueryMap(AbstractMap query) {
         map = new SQLMap();
         for (Object key : query.keySet()) {
             ArrayList values = (ArrayList) query.get(key);
             String value = (String) values.get(0);
            int var = Integer.parseInt(values.get(1).toString().replaceAll("\\.0", ""));
             put((String) key, new QueryNode(value, var));
         }
     }
 
     /**
      * The data inserted, Where Key will indicate what Search Parameter we are going to be
      * looking at, and the QueryNode will contain the information we are to compare it to by.
      *
      * @param key search type
      * @param value the comparison values which indicates exact or exhuastive search.
      */
     public void put(String key, QueryNode value) {
         map.put(key, value);
     }
 
     public Set<String> keySet() {
         return map.keySet();
     }
 
     public QueryNode get(String key) {
         return (QueryNode) map.get(key);
     }
 }
