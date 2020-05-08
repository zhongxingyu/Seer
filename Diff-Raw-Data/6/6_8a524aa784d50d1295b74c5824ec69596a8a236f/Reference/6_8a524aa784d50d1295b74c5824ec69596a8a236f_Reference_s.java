 package com.psddev.dari.db;
 
 import java.util.HashMap;
 
 @SuppressWarnings("serial")
 public class Reference extends HashMap<String, Object> {
 
     public static final String OBJECT_KEY = "_object";
 
     public Object getObject() {
         Object value = get(OBJECT_KEY);
         if (value == null) {
             value = get("record");
         }
        return value instanceof Record
                ? value
                : Query.findById(Object.class, StateValueUtils.toIdIfReference(value));
     }
 
     public void setObject(Object object) {
         put(OBJECT_KEY, object);
     }
 }
