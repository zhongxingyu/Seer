 /*
  * Copyright (C) 2003 - 2013 OpenSubsystems.com/net/org and its owners. All rights reserved.
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
 
 import java.io.File;
 import java.util.Comparator;
 
 /**
  * Comparator to compare length of files so that the files will be sorted from 
  * the smallest one to the largest one.
  * 
  * @author bastafidli
  */
 public class FileLengthComparator implements Comparator<File>
 {
    // Cached values ////////////////////////////////////////////////////////////
    
    /**
     * Shared comparator instance. Must be named this way to avoid Checkstyle 
     * warning.
     */
   private static Comparator s_instance = new FileLengthComparator();
 
    // Logic ////////////////////////////////////////////////////////////////////
    
    /**
     * Get shared comparator instance.
     *
     * @return FileLengthComparator  - shared comparator instance
     */
   public static Comparator getInstance(
    )
    {
       return s_instance;
    }
 
    /**
     * Compare lengths two files represented by File Objects.
     *
     * @param  o1 - File #1
     * @param  o2 - File #2
     * @return int - -1 if o1 < o2,
     *                0 if o1 == o2
     *                1 if o1 > o2
     */
    @Override
    public int compare(
       File o1,
       File o2
    )
    {
       long lLength1 = o1.length();
       long lLength2 = o2.length();
 
       if (lLength1 < lLength2)
       {
          return -1;
       }
       else
       {
          if (lLength1 == lLength2)
          {
             return 0;
          }
          else
          {
             return 1;
          }
       }
    }
 }
