 /*
  * Created on Aug 4, 2005
  */
 package uk.org.ponder.beanutil;
 
 import uk.org.ponder.stringutil.CharWrap;
 
 /**
  * A set of utility methods to operate on dot-separated bean paths.
  * 
  * @author Antranig Basman (antranig@caret.cam.ac.uk)
  * 
  */
 public class PathUtil {
 
   public static String getHeadPath(String path) {
     return getPathSegment(path, 0);
   }
 
   public static String getFromHeadPath(String path) {
     int firstdot = getPathSegment(null, path, 0);
     return firstdot == path.length() ? null
         : path.substring(firstdot + 1);
   }
 
   public static String getToTailPath(String path) {
     int lastdot = lastDotIndex(path);
     return lastdot == -1 ? null
         : path.substring(0, lastdot);
   }
 
   /** Returns the very last path component of a bean path */
   public static String getTailPath(String path) {
     int lastdot = lastDotIndex(path);
     return getPathSegment(path, lastdot + 1);
   }
 
   /**
    * Compose a prefix and suffix EL path, where the prefix is already escaped.
    * Prefix may be empty, but not null.
    */
   public static String composePath(String prefix, String suffix) {
     CharWrap toappend = new CharWrap(prefix);
     if (toappend.size != 0) {
       toappend.append('.');
     }
     composeSegment(toappend, suffix);
     return toappend.toString();
   }
 
   /**
    * Compose a prefix and suffix EL path, where the prefix is not escaped, and
    * is not null.
    */
   public static String buildPath(String prefix, String suffix) {
     CharWrap toappend = new CharWrap();
     composeSegment(toappend, prefix);
     toappend.append('.');
     composeSegment(toappend, suffix);
     return toappend.toString();
   }
 
   static int lastDotIndex(String path) {
     for (int i = path.length() - 1; i >= 0; --i) {
       if (path.charAt(i) == '.' && (i == 0 || path.charAt(i) != '\\'))
         return i;
     }
     return -1;
   }
 
   static void composeSegment(CharWrap toaccept, String toappend) {
     for (int i = 0; i < toappend.length(); ++i) {
       char c = toappend.charAt(i);
      if (c == '.' || c == '\\') {
        toaccept.append('\\').append(c);
       }
       else
         toaccept.append(c);
     }
   }
 
   static String getPathSegment(String path, int i) {
     CharWrap accept = new CharWrap();
     getPathSegment(accept, path, i);
     return accept.toString();
   }
 
   static int getPathSegment(CharWrap accept, String path, int i) {
     boolean escaped = false;
     for (; i < path.length(); ++i) {
       char c = path.charAt(i);
       if (!escaped) {
         if (c == '.') {
           return i;
         }
         else if (c == '\\') {
           escaped = true;
         }
         else if (accept != null)
           accept.append(c);
       }
       else {
         escaped = false;
         if (accept != null)
           accept.append(c);
       }
     }
     return i;
   }
 
 }
