 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.hd.cl.haas.distributedcrawl.Indexer;
 
 /**
  * Holds utility methods.
  *
  * @author Michael Haas <haas@cl.uni-heidelberg.de>
  */
 public class FetchUtil {
 
     /**
      * Decides whether we can index a document based on mime type.
      *
      * @param contentType mime type
      * @return true or false
      */
     public static boolean isIndexable(String contentType) {

         System.err.println("isIndexable: contentType is " + contentType);
        if (contentType == null) {
            return false;
        }
         String c = contentType.toLowerCase();
         // http://www.w3.org/TR/xhtml-media-types/#media-types
         if (c.startsWith("text/html")) {
             return true;
         }
         // http://www.w3.org/TR/xhtml-media-types/#media-types
         if (c.startsWith("application/xhtml+xml")) {
             return true;
         }
         // http://de.selfhtml.org/diverses/mimetypen.htm
         if (c.startsWith("text/plain")) {
             return true;
         }
         if (c.startsWith("text/xml")) {
             return true;
         }
         if (c.startsWith("application/xml")) {
             return true;
         }
         return false;
 
     }
 }
