 /*
  * AugeasErrorCode.java
  *
  * Copyright (C) 2009 Red Hat Inc.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA
  *
  * Author: Bryan Kearney <bkearney@redhat.com>
  */
 package net.augeas;
 
 public enum AugeasErrorCode {
     NO_ERROR(0), /* No error */
     OUT_OF_MEMORY(1), /* Out of memory */
     INTERNAL_ERROR(2), /* Internal error (bug) */
     PATH_ERROR(3), /* Invalid path expression */
     NO_MATCH(4), /* No match for path expression */
     MANY_MATCHES(5), /* Too many matches for path expression */
    LENS_SYNTAX_ERROR(6), /* Syntax error in lens file */
    LENS_LOOKUP_ERROR(7), /* Lens lookup failed */
    MULTIPLE_TRANSFORMS_ERROR(8); /* Multiple Transforms */
 
     private int intValue;
     private static java.util.HashMap<Integer, AugeasErrorCode> mappings;
 
     public static AugeasErrorCode forValue(int value) {
         return getMappings().get(value);
     }
 
     private synchronized static java.util.HashMap<Integer, AugeasErrorCode> getMappings() {
         if (mappings == null) {
             mappings = new java.util.HashMap<Integer, AugeasErrorCode>();
         }
         return mappings;
     }
 
     private AugeasErrorCode(int value) {
         intValue = value;
         AugeasErrorCode.getMappings().put(value, this);
     }
 
     public int getValue() {
         return intValue;
     }
 }
