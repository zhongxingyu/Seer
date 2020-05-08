 /*
 * $Id: StringUtils.java,v 1.3 2002-11-03 10:49:17 o_rossmueller Exp $
  */
 package org.junitee.util;
 
 import java.io.*;
 
 
 /**
  * @author <a href="mailto:oliver@oross.net">Oliver Rossmueller</a>
 * @version $Revision: 1.3 $
  * @since 1.5
  */
 public class StringUtils {
 
   private static final String[] DEFAULT_TRACE_FILTERS = new String[]{
     "junit.framework.TestCase",
     "junit.framework.TestResult",
     "junit.framework.TestSuite",
     "junit.framework.Assert.", // don't filter AssertionFailure
     "junit.swingui.TestRunner",
     "junit.awtui.TestRunner",
     "junit.textui.TestRunner",
     "java.lang.reflect.Method.invoke(",
     "org.apache.tools.ant."
   };
 
   private static final String[] DEFAULT_STOP_FILTERS = new String[]{
    "junit.framework.TestCase.runTest"
   };
 
   /**
    * This method converts texts to be displayed on
    * html-page. Following conversion are done
    * "<" => "&lt;" , ">" => "&gt;" and "&" => "&amp;", "\n" => "<br>"
    */
   public static String htmlText(String text) {
     StringBuffer sb = new StringBuffer();
     char c;
     if (text == null) return "";
     for (int i = 0; i < text.length(); i++) {
       c = text.charAt(i);
       switch (c) {
         case '<':
           sb.append("&lt;");
           break;
         case '>':
           sb.append("&gt;");
           break;
         case '&':
           sb.append("&amp;");
           break;
         case '\"':
           sb.append("&quot;");
           break;
         case '\n':
           sb.append("<br>");
           break;
         default:
           sb.append(c);
       }
     }
     return sb.toString();
   }
 
 
   /**
    * This method converts texts to be used in an
    * xml document. Following conversion are done
    * "<" => "&lt;" , ">" => "&gt;" and "&" => "&amp;"
    */
   public static String xmlText(String text) {
     StringBuffer sb = new StringBuffer();
     char c;
     if (text == null) return "";
     for (int i = 0; i < text.length(); i++) {
       c = text.charAt(i);
       switch (c) {
         case '<':
           sb.append("&lt;");
           break;
         case '>':
           sb.append("&gt;");
           break;
         case '&':
           sb.append("&amp;");
           break;
         case '\"':
           sb.append("&quot;");
           break;
         default:
           sb.append(c);
       }
     }
     return sb.toString();
   }
 
 
   /**
    * Filter the given stack trace.
    * 
    * @param stack
    * @return
    */
   public static String filterStack(String stack) {
     StringWriter sw = new StringWriter();
     PrintWriter pw = new PrintWriter(sw);
     StringReader sr = new StringReader(stack);
     BufferedReader br = new BufferedReader(sr);
 
     String line;
     try {
       while ((line = br.readLine()) != null) {
         if (stopLine(line)) {
           pw.println(line);
           break;
         }
         if (! filterLine(line)) {
           pw.println(line);
         }
       }
     } catch (Exception IOException) {
       return stack; // return the stack unfiltered
     }
     return sw.toString();
   }
 
 
   private static boolean filterLine(String line) {
     for (int i = 0; i < DEFAULT_TRACE_FILTERS.length; i++) {
       if (line.indexOf(DEFAULT_TRACE_FILTERS[i]) > 0) {
         return true;
       }
     }
     return false;
   }
 
   private static boolean stopLine(String line) {
     for (int i = 0; i < DEFAULT_STOP_FILTERS.length; i++) {
       if (line.indexOf(DEFAULT_STOP_FILTERS[i]) > 0) {
         return true;
       }
     }
     return false;
   }
 
 }
