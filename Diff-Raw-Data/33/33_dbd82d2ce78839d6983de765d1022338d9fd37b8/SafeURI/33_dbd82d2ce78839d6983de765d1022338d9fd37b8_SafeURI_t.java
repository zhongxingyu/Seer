 package pl.psnc.dl.wf4ever.common.util;
 
 import java.net.URI;
 
 public class SafeURI {
 
     public static String URItoString(URI uri) {
        return URItoString(uri.toString());
     }
 
 
     public static String URItoString(String string) {
         if (string.substring(0, 6).equals("file:/") && !string.substring(6, 8).equals("//")) {
             String result = "file:///" + string.substring(6);
             return result;
         }
         return string;
     }
 }
