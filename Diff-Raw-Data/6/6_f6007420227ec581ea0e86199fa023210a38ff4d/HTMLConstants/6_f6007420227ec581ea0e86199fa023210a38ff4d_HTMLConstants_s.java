 /*
  * Created on 28-Jan-2006
  */
 package uk.org.ponder.htmlutil;
 
 public class HTMLConstants {
   
   public static final String JS_BLOCK_START = "\n//<![CDATA[\n";
   public static final String JS_BLOCK_END = "\n//]]>\n";
 
   public static String[][] tagtoURL = {
     {"href", "<a ", "<link "},
     {"src", "<img ", "<frame ", "<script ", "<iframe ", "<style ", "<input ", "<embed ", },
     {"action", "<form "},
     {"codebase", "<applet ", "<object "}
   };
 // Every tag may have a "background" attribute holding a URL
   public static String[] ubiquitousURL = {"background"};
 }
