 package nl.sense_os.commonsense.client.utility;
 
 public class Log {
 
     private static final String LABEL_DEBUG = " [D] ";
     private static final String LABEL_ERROR = " [E] ";
     private static final String LABEL_INFO = " [I] ";
     private static final String LABEL_VERBOSE = " [V] ";
     private static final String LABEL_WARN = " [W] ";
     private static final int MAX_TAG_LENGTH = 20;
     private static final String TAG_PADDING = "                ";
     
     public static void d(String tag, String message) {        
         tag = TAG_PADDING.concat(tag);
         tag = tag.substring(tag.length() - MAX_TAG_LENGTH, tag.length());
         System.out.println(" " + tag + LABEL_DEBUG + message);
     }
     
     public static void e(String tag, String message) {        
         tag = TAG_PADDING.concat(tag);
         tag = tag.substring(tag.length() - MAX_TAG_LENGTH, tag.length());
        System.err.println(" " + tag + LABEL_ERROR + message);
     }
     
     public static void i(String tag, String message) {        
         tag = TAG_PADDING.concat(tag);
         tag = tag.substring(tag.length() - MAX_TAG_LENGTH, tag.length());
         System.out.println(" " + tag + LABEL_INFO + message);
     }
     
     public static void v(String tag, String message) {        
         tag = TAG_PADDING.concat(tag);
         tag = tag.substring(tag.length() - MAX_TAG_LENGTH, tag.length());
         System.out.println(" " + tag + LABEL_VERBOSE + message);
     }
     
     public static void w(String tag, String message) {        
         tag = TAG_PADDING.concat(tag);
         tag = tag.substring(tag.length() - MAX_TAG_LENGTH, tag.length());
        System.err.println(" " + tag + LABEL_WARN + message);
     }
 }
