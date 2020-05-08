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
  * Contains various static methods to convert strings.
  */
 public class StringConvert {
     /**
      * Converts the given string to an byte.
      *
      * @param string the sting to convert.
      * @param defaultValue a default value to use if string does not
      * represent a valid byte.
      *
      * @return byte value of string or defaultValue if string
      * does not represent a valid byte.
      *
      */
     static public Byte toByte(String string, Byte defaultValue) {
         try {
             if (string == null) throw new NumberFormatException();
            return new Byte(Byte.parseByte(string.trim()));
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Converts the given string to an byte.
      *
      * @param string the string to convert.
      *
      * @return byte value of string or null if string does not
      * represent a valid byte.
      *
      */
     static public Byte toByte(String string) {
         return toByte(string, null);
     }
 
     /**
      * Converts the given string to an short.
      *
      * @param string the sting to convert.
      * @param defaultValue a default value to use if string does not
      * represent a valid short.
      *
      * @return short value of string or defaultValue if string
      * does not represent a valid short.
      *
      */
     static public Short toShort(String string, Short defaultValue) {
         try {
             if (string == null) throw new NumberFormatException();
            return new Short(Short.parseShort(string.trim()));
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Converts the given string to an short.
      *
      * @param string the string to convert.
      *
      * @return short value of string or null if string does not
      * represent a valid short.
      *
      */
     static public Short toShort(String string) {
         return toShort(string, null);
     }
 
     /**
      * Converts the given string to an integer.
      *
      * @param string the sting to convert.
      * @param defaultValue a default value to use if string does not
      * represent a valid integer.
      *
      * @return integer value of string or defaultValue if string
      * does not represent a valid integer.
      *
      */
     static public Integer toInteger(String string, Integer defaultValue) {
         try {
             if (string == null) throw new NumberFormatException();
            return new Integer(Integer.parseInt(string.trim()));
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Converts the given string to an integer.
      *
      * @param string the string to convert.
      *
      * @return integer value of string or null if string does not
      * represent a valid integer.
      *
      */
     static public Integer toInteger(String string) {
         return toInteger(string, null);
     }
 
     /**
      * Converts the given string to an long.
      *
      * @param string the sting to convert.
      * @param defaultValue a default value to use if string does not
      * represent a valid long.
      *
      * @return long value of string or defaultValue if string
      * does not represent a valid long.
      *
      */
     static public Long toLong(String string, Long defaultValue) {
         try {
             if (string == null) throw new NumberFormatException();
            return new Long(Long.parseLong(string.trim()));
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Converts the given string to an long.
      *
      * @param string the string to convert.
      *
      * @return long value of string or null if string does not
      * represent a valid long.
      *
      */
     static public Long toLong(String string) {
         return toLong(string, null);
     }
 
     /**
      * Converts the given string to a float.
      *
      * @param string the string to convert.
      * @param defaultValue a default value to use if string does not
      * represent a valid float.
      *
      * @return float value of string or defaultValue if string
      * does not represent a valid float.
      */
     static public Float toFloat(String string, Float defaultValue) {
         try {
             if (string == null) throw new NumberFormatException();
            return new Float(Float.parseFloat(string.trim()));
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Converts the given string to a float.
      *
      * @param string the string to convert.
      *
      * @return float value of string or null if string does not
      * represent a valid float.
      */
     static public Float toFloat(String string) {
         return toFloat(string, null);
 
     }
 
     /**
      * Converts the given string to a double.
      *
      * @param string the string to convert.
      * @param defaultValue a default value to use if string does not
      * represent a valid double.
      *
      * @return double value of string or defaultValue if string
      * does not represent a valid double.
      */
     static public Double toDouble(String string, Double defaultValue) {
         try {
             if (string == null) throw new NumberFormatException();
            return new Double(Double.parseDouble(string.trim()));
         } catch (NumberFormatException e) {
             return defaultValue;
         }
     }
 
     /**
      * Converts the given string to a double.
      *
      * @param string the string to convert.
      *
      * @return double value of string or null if string does not
      * represent a valid double.
      */
     static public Double toDouble(String string) {
         return toDouble(string, null);
 
     }
 }
