 /**
  *
  */
 package com.github.jtse.puzzle.util;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang.StringUtils;
 
 import com.google.common.annotations.VisibleForTesting;
 
 
 /**
  * Utilities for parsing script file
  *
  * @author jtse
  */
 public class ScriptUtils {
   public static class ScriptException extends RuntimeException {
     private static final long serialVersionUID = 1L;
 
     public ScriptException(String message) {
       super(message);
     }
   }
 
   /**
    * Reads a script file and creates an array of key-value map
    *
   * @param file
   *          the file to load
   * @param requiredKeys
   *          the required keys for each map
    * @return list of key-value map
    */
   public static final Map<String, String>[] read(File file, String... requiredKeys) {
     FileInputStream in = null;
     try {
       in = new FileInputStream(file);
       return read(in, requiredKeys);
     } catch (FileNotFoundException e) {
       throw new RuntimeException(e);
     } finally {
       try {
         if (in != null) {
           in.close();
         }
       } catch (IOException e) {
       }
     }
   }
 
   @VisibleForTesting
   static final Map<String, String>[] read(InputStream in, String... requiredKeys) {
     int n = 1;
 
     List<Map<String, String>> maps = new ArrayList<Map<String, String>>();
 
     BufferedReader reader = new BufferedReader(new InputStreamReader(in));
     String line = "";
 
     Map<String, String> map = new HashMap<String, String>();
 
     try {
       while (line != null) {
         line = reader.readLine();
         if (!StringUtils.isEmpty(line) && !line.startsWith("#")) {
           String[] pair = line.split("=");
 
           if (pair.length != 2) {
             if (n > 1) {
               throw new ScriptException("Missing '=' on line #" + n);
             } else {
               throw new ScriptException(
                   "Missing '=' on line the first line. Are you sure this is a plain-text file?");
             }
           }
 
           String key = StringUtils.trim(pair[0]);
           String value = StringUtils.trim(pair[1]);
 
           if (map.containsKey(key)) {
             if (mapHasKeys(map, requiredKeys)) {
               maps.add(map);
               map = new HashMap<String, String>();
               map.put(key, value);
             } else {
               throw new ScriptException("The trial before line #" + n
                   + " is incomplete.\nTrial must contain: " + StringUtils.join(requiredKeys, ", "));
             }
           } else {
             map.put(key, value);
           }
         }
         n++;
       }
 
       if (mapHasKeys(map, requiredKeys)) {
         maps.add(map);
       } else {
         throw new ScriptException("The trial before line #" + n
             + " is incomplete.\nTrial must contain: " + StringUtils.join(requiredKeys, ", "));
       }
     } catch (IOException e) {
       throw new RuntimeException(e);
     }
 
     @SuppressWarnings("unchecked")
     Map<String, String>[] a = new Map[0];
     return maps.toArray(a);
   }
 
   @VisibleForTesting
   static final boolean mapHasKeys(Map<String, String> map, String... requiredKeys) {
     for (String key : requiredKeys) {
       if (!map.containsKey(key)) {
         return false;
       }
     }
     return true;
   }
 
   @VisibleForTesting
   static final Map<String, String> toMap(String... keyValues) {
     Map<String, String> map = new HashMap<String, String>();
 
     String key = null;
     for (int i = 0; i < keyValues.length; i++) {
       if (i % 2 == 0) {
         key = keyValues[i];
         map.put(key, null);
       } else {
         map.put(key, keyValues[i]);
       }
     }
     return map;
   }
 
   /**
    * Parses color string into RGBA floating point values
    *
    * @param color
    *          string
    * @return array of 4 floats representing RGBA
    */
   public static final float[] parseColor(String color) {
     String[] values = color.split("[^0-9\\.]+");
 
     float[] f = new float[4];
 
     for (int i = 0; i < values.length; i++) {
       f[i] = Float.parseFloat(values[i]);
     }
 
     if (values.length < 4) {
       f[3] = 1.0f;
     }
 
     return f;
   }
 }
