 /*
  * This file is part of CraftCommons.
  *
  * Copyright (c) 2011-2012, CraftFire <http://www.craftfire.com/>
  * CraftCommons is licensed under the GNU Lesser General Public License.
  *
  * CraftCommons is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * CraftCommons is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.craftfire.commons;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import com.craftfire.commons.util.Util;
 
 /**
  * PhpSerializer - util class to serialize java objects with php serialization
  *
  * @see SerializedPhpParser
  * @see Util
  */
 public final class PhpSerializer {
     private PhpSerializer() {
     }
     /**
      * Serialize integer.
      *
      * @param i     integer to serialize
      * @return      serialized i
      */
     public static String serialize(int i) {
        return "i:" + i + ";";
     }
 
     /**
      * Serialize double.
      *
      * @param d     double to serialize
      * @return      serialized d
      */
     public static String serialize(double d) {
        return "d:" + d + ";";
     }
 
     /**
      * Serialize boolean.
      *
      * @param b     boolean to serialize
      * @return      serialized b
      */
     public static String serialize(boolean b) {
         return "b:" + (b ? "1" : "0") + ";";
     }
 
     /**
      * Serialize string
      *
      * @param s     string to serialize
      * @return      s serialized to string
      */
     public static String serialize(String s) {
         if (s == null) {
             return "N;";
         }
         return "s:" + s.length() + ":\"" + s + "\";";
     }
 
     /**
      * Serialize list
      *
      * @param list  list to serialize
      * @return      list serialized to string
      */
     public static String serialize(List<?> list) {
         if (list == null) {
             return "N;";
         }
         String out = "a:" + list.size() + ":{";
         int index = 0;
         Iterator<?> i = list.iterator();
         while (i.hasNext()) {
             out += serialize(index++);
             out += serialize(i.next());
             if (!out.endsWith(";")) {
                 out += ";";
             }
         }
         out += "}";
         return out;
     }
 
     /**
      * Serialize map
      *
      * @param map   map to serialize
      * @return      map serialized to string
      */
     public static String serialize(Map<?, ?> map) {
         if (map == null) {
             return "N;";
         }
         String out = "a:" + map.size() + ":{";
         Iterator<?> i = map.keySet().iterator();
         while (i.hasNext()) {
             Object key = i.next();
             out += serialize(key);
             out += serialize(map.get(key));
         }
         out += "}";
         return out;
     }
 
     /**
      * Serialize {@link SerializedPhpParser.PhpObject}
      *
      * @param value     PhpObject to serialize
      * @return          serialized value
      */
     public static String serialize(SerializedPhpParser.PhpObject value) {
         if (value == null) {
             return "N;";
         }
         String out = "O:" + value.name.length() + ":\"" + value.name + "\":";
         out += value.attributes.size() + ":{";
         Iterator<Object> i = value.attributes.keySet().iterator();
         while (i.hasNext()) {
             Object key = i.next();
             out += serialize(key);
             out += serialize(value.attributes.get(key));
         }
         out += "};";
         return out;
     }
 
     /**
      * Detect object type and serialize it.
      *
      * @param value     object to serialize
      * @return          serialized value, or serialized value.toString
      *                  if unknown type.
      */
     public static String serialize(Object value) {
         if (value == null) {
             return "N;";
         } else if (value instanceof Integer) {
             return serialize(((Integer) value).intValue());
         } else if (value instanceof Number) {
             return serialize(((Number) value).doubleValue());
         } else if (value instanceof Boolean) {
             return serialize(((Boolean) value).booleanValue());
         } else if (value instanceof List<?>) {
             return serialize((List<?>) value);
         } else if (value instanceof Map<?,?>) {
             return serialize((Map<?, ?>) value);
         } else if (value instanceof SerializedPhpParser.PhpObject) {
             return serialize((SerializedPhpParser.PhpObject) value);
         }
         return serialize(value.toString());
     }
 }
