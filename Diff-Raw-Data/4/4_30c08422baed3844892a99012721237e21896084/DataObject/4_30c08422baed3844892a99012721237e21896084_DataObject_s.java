 package im.mctop.bot;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class DataObject {
     private Map<String, Object> m = new HashMap<>();
 
     public void set( String k, String v ) {
         m.put(k, v);
     }
 
     public void set( String k, Boolean v ) {
         m.put(k, v);
     }
 
     public void set( String k, int v ) {
         m.put(k, v);
     }
 
     public void set( String k, float v ) {
         m.put(k, v);
     }
 
     public String getString( String k ) {
         return m.get(k).toString();
     }
 
     public Boolean getBoolean( String k ) {
         return Boolean.valueOf(m.get(k).toString());
     }
 
     public int getInt( String k ) {
         return Integer.valueOf(m.get(k).toString());
     }
 
     public float getFloat( String k ) {
         return Float.valueOf(m.get(k).toString());
     }
 
     @Override
     public String toString() {
         StringBuilder data = new StringBuilder();
         data.append("DataObject [");
         for (Map.Entry<String, Object> entry : m.entrySet()) {
             data.append(entry.getKey()).append("=").append(entry.getValue().toString());
         }
         data.append("]");
         return data.toString();
     }
 }
