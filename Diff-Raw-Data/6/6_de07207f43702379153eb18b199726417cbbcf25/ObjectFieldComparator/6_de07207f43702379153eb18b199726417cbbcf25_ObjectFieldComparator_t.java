 package com.psddev.dari.db;
 
import java.io.Serializable;
 import java.util.Comparator;
 
 import com.psddev.dari.util.ObjectUtils;
 
public class ObjectFieldComparator implements Comparator<Object>, Serializable {

    private static final long serialVersionUID = 1L;
 
     private final String field;
     private final boolean isNullGreatest;
 
     /**
      * @throws IllegalArgumentException If the given {@code field}
      *         is {@code null}.
      */
     public ObjectFieldComparator(String field, boolean isNullGreatest) {
         if (field == null) {
             throw new IllegalArgumentException("Field is required");
         }
 
         this.field = field;
         this.isNullGreatest = isNullGreatest;
     }
 
     // --- Comparator support ---
 
     @Override
     public int compare(Object x, Object y) {
         State xState = State.getInstance(x);
         State yState = State.getInstance(y);
 
         Object xValue = xState != null ? filter(xState.getValue(field)) : null;
         Object yValue = yState != null ? filter(yState.getValue(field)) : null;
 
         return ObjectUtils.compare(xValue, yValue, isNullGreatest);
     }
 
     protected Object filter(Object value) {
         return value;
     }
 }
