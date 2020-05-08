 /**
  * This file is part of Twonky String Utils.
  *
  * Copyright (C) 2012 Søren Lund <soren@lund.org>
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; version 2 dated June, 1991 or at your option
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * A copy of the GNU General Public License is available in the source tree;
  * if not, write to the Free Software Foundation, Inc.,
  * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  */
 package net.twonky;
 
 import java.util.regex.Pattern;
 
 /**
  * Contains various static methods to check the contents of a string.
  */
 public class StringCheck {
 
     static final Pattern emptyPattern = Pattern.compile("^[\\s]*$");
     static final Pattern intPattern = Pattern.compile("^-?\\d+$");
     static final Pattern pintPattern = Pattern.compile("^\\d+$");
 
     /**
      * Checks if a string is empty.
      * 
      * The string is considered empty if
      *
      * <ol>
      * <li> it is null
      * <li> its length is 0
      * <li> it contains only whitespace
      * </ol>
      *
      * @param string the string to check.
      *
      * @return true if string is empty else false.
      */
     static public Boolean isEmpty(String string) {
         return string == null || emptyPattern.matcher(string).matches();
     }
 
     /**
      * Checks if a string consists of only upper case letters.
      *
      * @param string the string to check
      *
      * @return true if string contains only upper case letters else
      * false.
      */
     static public Boolean isUpper(String string) {
         return string == null || string.toUpperCase().equals(string);
     }
 
     /**
      * Checks if a string consists of only lower case letters.
      *
      * @param string the string to check
      *
      * @return true if string contains only lower case letters else
      * false.
      */
     static public Boolean isLower(String string) {
         return string == null || string.toLowerCase().equals(string);
     }
 
     /**
      * Checks if a string contains an integer.
      *
      * @param string the string to check
      *
      * @return true if string contains an integer else false.
      */
     static public Boolean isInt(String string) {
         return string == null || intPattern.matcher(string).matches();
     }
 
     /**
      * Checks if a string contains a positive integer.
      *
      * @param string the string to check
      *
      * @return true if string contains a positive integer else false.
      */
     static public Boolean isPosInt(String string) {
        return new Boolean(string == null || pintPattern.matcher(string).matches());
    }
 }
