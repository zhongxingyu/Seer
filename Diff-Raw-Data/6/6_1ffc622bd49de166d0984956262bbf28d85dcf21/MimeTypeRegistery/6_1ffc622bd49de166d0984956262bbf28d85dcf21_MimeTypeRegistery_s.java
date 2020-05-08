 package it.webalice.alexcoppo.lrxl.util;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  *
  */
 public class MimeTypeRegistery {
     private static Map<String, String> m2e = new HashMap<String, String>();
     private static Map<String, String> e2m = new HashMap<String, String>();
     
     static {
         add("jpg", "image/jpeg");
         add("jpeg", "image/jpeg");
         add("png", "image/png");
     }
 
     private static void add(String extension, String mime) {
         m2e.put(mime, extension);
         e2m.put(extension, mime);
     }
     
     public static String mimeToExtension(String mime) {
         String result = m2e.get(mime);
         return result;
     }
 
    public static String extentionToMime(String extension) {
        String result = e2m.get(extension);
        return result;
     }
 }
