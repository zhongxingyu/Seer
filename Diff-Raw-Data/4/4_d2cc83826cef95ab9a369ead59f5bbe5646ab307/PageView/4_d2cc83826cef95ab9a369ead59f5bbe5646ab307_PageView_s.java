 package com.whysearchtwice.container;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.codehaus.jettison.json.JSONException;
 import org.codehaus.jettison.json.JSONObject;
 
 import com.tinkerpop.blueprints.Vertex;
 
 public class PageView {
     private Map<String, String> stringProperties;
     private Map<String, Long> longProperties;
     private Map<String, Integer> intProperties;
 
     private static final List<String> STRING_KEYS = Arrays.asList("id", "type", "pageUrl", "userId", "deviceId", "predecessorId", "parentId");
     private static final List<String> LONG_KEYS = Arrays.asList("pageOpenTime", "pageCloseTime");
     private static final List<String> INT_KEYS = Arrays.asList("tabId", "windowId");
 
     public PageView() {
         stringProperties = new HashMap<String, String>();
         longProperties = new HashMap<String, Long>();
         intProperties = new HashMap<String, Integer>();
     }
 
     public PageView(JSONObject attributes) throws JSONException {
         this();
         jsonToPageView(attributes);
     }
 
     public PageView(Vertex v) {
         this();
         vertexToPageView(v);
     }
 
     /**
      * Export the contents of the PageView into a JSON Object for sending back
      * to the client.
      * 
      * @return JSONObject
      * @throws JSONException
      */
     public JSONObject exportJson() throws JSONException {
         JSONObject json = new JSONObject();
 
         for (Entry<String, String> e : stringProperties.entrySet()) {
             json.append(e.getKey(), e.getValue());
         }
 
         for (Entry<String, Long> e : longProperties.entrySet()) {
             json.append(e.getKey(), e.getValue());
         }
 
         for (Entry<String, Integer> e : intProperties.entrySet()) {
             json.append(e.getKey(), e.getValue());
         }
 
         return json;
     }
 
     public void mergeIntoVertex(Vertex v) {
         for (Entry<String, String> e : stringProperties.entrySet()) {
            v.setProperty(e.getKey(), e.getValue());
         }
 
         for (Entry<String, Long> e : longProperties.entrySet()) {
             v.setProperty(e.getKey(), e.getValue());
         }
 
         for (Entry<String, Integer> e : intProperties.entrySet()) {
             v.setProperty(e.getKey(), e.getValue());
         }
     }
 
     /**
      * Store all contents of Vertex to PageView
      * 
      * @param v
      */
     public void vertexToPageView(Vertex v) {
         storeProperty("id", v.getId().toString());
         
         for (String key : v.getPropertyKeys()) {
             storeProperty(key, v.getProperty(key));
         }
     }
 
     /**
      * For each property in the JSON object, store it in our custom maps.
      * 
      * @param attributes
      */
     public void jsonToPageView(JSONObject attributes) throws JSONException {
         Iterator keysIter = attributes.keys();
         while (keysIter.hasNext()) {
             String key = (String) keysIter.next();
             storeProperty(key, attributes.get(key));
         }
     }
 
     /**
      * Determine which map to store the property in and store
      * 
      * @param key
      * @param value
      */
     private void storeProperty(String key, Object value) {
         if (STRING_KEYS.contains(key)) {
             stringProperties.put(key, (String) value);
         } else if (LONG_KEYS.contains(key)) {
             longProperties.put(key, (Long) value);
         } else if (INT_KEYS.contains(key)) {
             intProperties.put(key, (Integer) value);
         } else {
             // Ignore the property for now
         }
     }
 
     /**
      * This really isn't necessary. Public interface to a private method so in
      * the future we can place restrictions on it if necessary.
      * 
      * @param key
      * @param value
      */
     public void setProperty(String key, Object value) {
         storeProperty(key, value);
     }
 
     public String toString() {
         String toReturn = "{";
 
         for (Entry<String, String> e : stringProperties.entrySet()) {
             toReturn += "\"" + e.getKey() + "\": \"" + e.getValue() + "\", ";
         }
 
         for (Entry<String, Long> e : longProperties.entrySet()) {
             toReturn += "\"" + e.getKey() + "\": \"" + e.getValue() + "\", ";
         }
 
         for (Entry<String, Integer> e : intProperties.entrySet()) {
             toReturn += "\"" + e.getKey() + "\": \"" + e.getValue() + "\", ";
         }
 
         toReturn = toReturn.substring(0, toReturn.length() - 2);
 
         toReturn += "}";
         return toReturn;
     }
 }
