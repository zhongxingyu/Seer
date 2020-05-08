 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: ValueSourceSpecification.java,v 1.2 2003-03-21 13:56:20 shahbaz.javeed Exp $
  */
 
 package com.netspective.commons.value;
 
 import com.netspective.commons.value.source.StaticValueSource;
 
 /**
  * Given a string of the format abc:def, parse the string into a value source Id ('abc') and value source
  * parameters ('def'). If the string contains a '\' (backslash) immediately prior to the ':' then the string is
  * not considered a value source string. Once parsed, check to see if 'abc' is an ID found in the srcClasses
  * private Map that contains value source Id keys that are mapped to ValueSource classes. If it's found, create
  * a new instance, pass along the parameters, and cache it. If the ID is not found in the map, check to if it is
  * a valid Java class that is found in the classpath -- if abc is a valid class, instantiate it and pass in the
  * value source parameters.
  */
 public class ValueSourceSpecification
 {
     public static final char VALUE_SOURCE_ID_DELIM = ':';
     public static final char VALUE_SOURCE_ID_DELIM_ESCAPE = '\\';
     public static final char VALUE_SOURCE_PI_START = '[';
     public static final char VALUE_SOURCE_PI_END = ']';
     public static final char VALUE_SOURCE_PI_ESCAPE = '\\';
 
     /**
      * The original string that was parsed.
      */
     private String specificationText;
 
     /**
      * The delimiter that separates the id or class name from the parameters.
      */
     private int idDelimPos;
 
     /**
      * Either the id or the class name (basically everything before the VALUE_SOURCE_ID_DELIM)
      */
     private String idOrClassName;
 
     /**
      * The value source parameters (basically everything after the VALUE_SOURCE_ID_DELIM not including the processing instructions)
      */
     private String params;
 
     /**
      * The value source processing instructions -- like abc:[xyz]other stuff
      */
     private String processingInstructions;
 
     /**
      * Whether or not the value source specification string is valid.
      */
     private boolean valid;
 
     /**
      * Whether or not the value source specification is simply an escaped string (not a real value source)
      */
     private boolean escaped;
 
     /**
      * Whether or not the ID actually points to a value source identifier or to a custom class name.
      */
     private boolean customClass;
 
     public ValueSourceSpecification(String text)
     {
         this.specificationText = text;
         idDelimPos = text.indexOf(VALUE_SOURCE_ID_DELIM);
         valid = idDelimPos >= 0;
 
         if(valid)
         {
             if(idDelimPos > 0 && text.charAt(idDelimPos-1) == VALUE_SOURCE_ID_DELIM_ESCAPE)
             {
                 escaped = true;
                 valid = false;
             }
             else
             {
                 idOrClassName = text.substring(0, idDelimPos);
                 params = text.substring(idDelimPos + 1);
 
                 if(params.length() > 0)
                 {
                     switch(params.charAt(0))
                     {
                         case VALUE_SOURCE_PI_START:
                             int endPos = params.indexOf(VALUE_SOURCE_PI_END);
                             if(endPos == -1)
                                 valid = false;
                             else
                             {
                                 processingInstructions = params.substring(1, endPos);
                                params = params.substring(endPos + 1);
                             }
                             break;
 
                         case VALUE_SOURCE_PI_ESCAPE:
                             params = params.substring(1);
                             break;
                     }
                 }
             }
         }
     }
 
     public StaticValueSource getStaticValueSource()
     {
         StaticValueSource result = null;
         if(escaped)
         {
             StringBuffer sb = new StringBuffer(specificationText);
             sb.deleteCharAt(idDelimPos - 1);
             result = new StaticValueSource(sb.toString());
         }
         else
             result = new StaticValueSource(specificationText);
         return result;
     }
 
     public String getSpecificationText()
     {
         return specificationText;
     }
 
     public String toString()
     {
         return getSpecificationText();
     }
 
     public int getIdDelimPos()
     {
         return idDelimPos;
     }
 
     public String getIdOrClassName()
     {
         return idOrClassName;
     }
 
     public String getParams()
     {
         return params;
     }
 
     public String getProcessingInstructions()
     {
         return processingInstructions;
     }
 
     public boolean isValid()
     {
         return valid;
     }
 
     public boolean isEscaped()
     {
         return escaped;
     }
 
     public boolean isCustomClass()
     {
         return customClass;
     }
 
     public void setCustomClass(boolean customClass)
     {
         this.customClass = customClass;
     }
 }
