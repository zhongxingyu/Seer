 package com.pvminecraft.FlatDB;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 /**
  *
  * @author s0lder
  */
 public class Row {
     private String index;
     private HashMap<String,String> values;
     
     public Row(String id) {
         index = id;
         values = new HashMap<String,String>();
     }
     
     public Row(String key, List<String> row) {
         index = key;
         values = Row.extractHashMap(row);
     }
     
     public static HashMap<String,String> extractHashMap(List<String> row) {
         HashMap<String,String> map = new HashMap<String, String>();
         String in, val;
         String[] data;
         for(String element : row) {
             data = element.split(FlatDB.vSep);
             in = data[0];
             val = data[1];
             map.put(in, val);
         }
         return map;   
     }
     
     
     public String getIndex() {
         return index;
     }
     
     public String getElement(String in) {
        return values.get(in);
     }
     
     public List<String> getList(String in) {
         String val = values.get(in);
         List<String> ret;
         String[] vals = val.split(",");
         ret = Arrays.asList(vals);
         return ret;
     }
     
     public void removeElement(String key) {
         if(values.containsKey(key))
             values.remove(key);
     }
     
     public void addElement(String key, String val) {
         values.put(key, val);
     }
     
     public void addList(String key, List<String> val) {
         String ret;
         String[] tmp = new String[val.size()];
         ret = Arrays.toString(val.toArray(tmp));
         ret = ret.replaceAll("[\\[\\]]", "");
         ret = ret.replaceAll("[ ]", "");
         values.put(key, ret);
     }
     
     @Override
     public String toString() {
         String ret = "";
         Iterator it = values.entrySet().iterator();
         while(it.hasNext()) {
             Map.Entry pairs = (Map.Entry)it.next();
             String val = (String) pairs.getValue();
             String in = (String) pairs.getKey();
             ret += in + FlatDB.vSep + val + FlatDB.sep;
         }
         return ret;
     }
     
     public static Row fromString(String str) {
         String[] parts;
         String key;
         ArrayList<String> arr = new ArrayList<String>();
         parts = str.split(FlatDB.sep);
         key = parts[0];
         for(int i = 1; i < parts.length; i++) {
             arr.add(parts[i]);
         }
         return new Row(key, arr);
     }
 }
