 package com.naildrivin5.fwf;
 
 /** Utility methods for dealing with strings.
  */
 public class StringUtils
 {
     private StringUtils() 
     {
     }
 
     /** Returns the camel-cased version of the string, using underscores as the word delimiter.
      * @param uncameled the string to camelize.
      * @return the string in camel-case, capitalized
      */
     public static String camelize(String uncameled)
     {
         if (uncameled == null)
             return null;
         String parts[] = uncameled.split("_");
         StringBuilder cameled = new StringBuilder("");
         for (String word: parts)
         {
             cameled.append(ucfirst(word));
         }
         return cameled.toString();
     }
 
     /** Returns a non-capitalized version of this string.
      * @param string the string to uncapitalize.
     * @return the string where the first character is in lower case (based on {@link java.lang.String#toLowerCase(java.lang.String)}).
      */
     public static String lcfirst(String string)
     {
         if ( (string == null) || (string.length() == 0) )
             return string;
 
         String first = string.substring(0,1);
         String rest = string.substring(1);
         return first.toLowerCase() + rest;
     }
 
     /** Returns a capitalized version of this string.
      * @param string the string to capitalize.
     * @return the string where the first character is in upper case (based on {@link java.lang.String#toUpperCase(java.lang.String)}).
      */
     public static String ucfirst(String string)
     {
         if ( (string == null) || (string.length() == 0) )
             return string;
 
         String first = string.substring(0,1);
         String rest = string.substring(1);
         return first.toUpperCase() + rest;
     }
 }
