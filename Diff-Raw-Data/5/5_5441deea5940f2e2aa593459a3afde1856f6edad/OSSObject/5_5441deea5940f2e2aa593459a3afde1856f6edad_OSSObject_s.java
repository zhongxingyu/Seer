 /*
 * Copyright (C) 2012 OpenSubsystems.com/net/org and its owners. All rights reserved.
  * 
  * This file is part of OpenSubsystems.
  *
  * OpenSubsystems is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>. 
  */
  
 package org.opensubsystems.core.util;
 
 /**
  * Base class for all classes in this project. It is a place to define 
  * alternative or enhanced behavior of standard Java methods 
  * 
  * @author bastafidli
  */
 public class OSSObject 
 {
    // Constants ////////////////////////////////////////////////////////////////
    
    /**
    * Limit how many indendation are supported.
     */
    public static final int INDENTATION_LIMIT = 100;
    
    /**
     * Pregenerated indentation strings used to indent output of toString. Each
     * one starts with new line.
     */
    public static final String[] INDENTATION = new String[INDENTATION_LIMIT];
 
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Class constructor
     */
    static 
    {
       StringBuilder sb = new StringBuilder("\n");
       
       for (int iIndex = 0; iIndex < INDENTATION_LIMIT; iIndex++)
       {
          INDENTATION[iIndex] = sb.toString();
          sb.append("   ");
       }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public final String toString()
    {
       StringBuilder sb = new StringBuilder();
       
       toString(sb, 0);
       return sb.toString();
    }
 
    /**
     * Produce string representation of the object properly indented to display
     * the object hierarchy
     * 
     * @param sb - buffer used to create the string representation of object
     * @param iIndentLevel - indentation level with 0 being just new line
     */
    public void toString(
       StringBuilder sb,
       int           iIndentLevel
    )
    {
       // Default implementation does nothing
    }
    
    // Helper methods //////////////////////////////////////////////////////////
    
    /**
     * Indent the future content of the buffer by a specified level.
     *  
     * @param sb - buffer to which the indentation will be appended
     * @param iIndentLevel - level to which to indent future content
     */
    protected final void indent(
       StringBuilder sb,
       int           iIndentLevel
    )
    {
       append(sb, INDENTATION[iIndentLevel]);
    }
    
    /**
     * Safely append value to the buffer even if it is null.
     * 
     * @param sb - buffer to use to append values
     * @param value - value to append, can be null
     */
    protected final void append(
       StringBuilder sb,
       Object        value
    )
    {
       if (value == null)
       {
          sb.append(StringUtils.NULL_STRING);
       }
       else
       {
          sb.append(value);
       }
    }
 
    /**
     * Safely append value to the buffer even if it is null.
     * 
     * @param sb - buffer to use to append values
     * @param iIndentLevel - level at which to append the value
     * @param value - value to append, can be null
     */
    protected final void append(
       StringBuilder sb,
       int           iIndentLevel,
       Object        value
    )
    {
       sb.append(INDENTATION[iIndentLevel]);
       if (value == null)
       {
          sb.append(StringUtils.NULL_STRING);
       }
       else
       {
          sb.append(value);
       }
    }
 
    /**
     * Safely append value to the buffer even if it is null.
     * 
     * @param sb - buffer to use to append values
     * @param iIndentLevel - level at which to append the value
     * @param label - label to use for the value
     * @param value - value to append, can be null
     */
    protected final void append(
       StringBuilder sb,
       int           iIndentLevel,
       String        label,
       Object        value
    )
    {
       sb.append(INDENTATION[iIndentLevel]);
       sb.append(value);
       if (value == null)
       {
          sb.append(StringUtils.NULL_STRING);
       }
       else
       {
          sb.append(value);
       }
    }
 }
