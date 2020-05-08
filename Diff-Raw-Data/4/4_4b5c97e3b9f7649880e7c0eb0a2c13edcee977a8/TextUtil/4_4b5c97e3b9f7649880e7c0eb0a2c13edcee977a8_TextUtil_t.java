 /**
  * jQL - Calculation of Chemical Speciation in Aqueous Solution
  *
  * Copyright (C) 2009 Michael Gfeller, <mgfeller@mgfeller.net> - <http://www.mgfeller.net>
  *
  * This file is part of jQL.
  *
  * jQL is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * jQL is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with jQL.  If not, see <http://www.gnu.org/licenses/>.
  */
 package ch.ethz.polyql.jql.domain.shared;
 
 import org.apache.commons.lang.StringUtils;
 
import java.util.ArrayList;
 import java.util.List;
 
 // TODO: Auto-generated Javadoc
 /**
  * Author: Michael Gfeller.
  */
 public final class TextUtil {
   
   /** The Constant NEW_LINE. */
   private static final String NEW_LINE = System.getProperty( "line.separator" );
 
   /**
    * Instantiates a new text util.
    */
   private TextUtil() { }
 
   /**
    * Checks if is empty.
    *
    * @param text the text
    * @return true, if is empty
    */
   public static boolean isEmpty( final String text ) {
     return (text == null) || ("".equals( text.trim() ));
   }
 
   /**
    * Empty if null.
    *
    * @param text the text
    * @return the string
    */
   public static String emptyIfNull( final String text ) {
     return (text == null) ? "" : text;
   }
 
   /**
    * Compare ignore case.
    *
    * @param text1 the text1
    * @param text2 the text2
    * @return the int
    */
   public static int compareIgnoreCase( final String text1, final String text2 ) {
     return emptyIfNull( text1 ).compareToIgnoreCase( emptyIfNull( text2 ) );
   }
 
   /**
    * System dependent newline.
    *
    * @param text the text
    * @return the string
    */
   public static String systemDependentNewline( final String text ) {
     if (text == null) {
       return text;
     } else {
       return text.replaceAll( "\n", NEW_LINE );
     }
   }
 
   /**
    * Join.
    *
    * @param text the text
    * @return the string
    */
  public static String join(  final String[] text ) {
 	  
     return StringUtils.join( text, "\n" );
     
   }
 
 }
