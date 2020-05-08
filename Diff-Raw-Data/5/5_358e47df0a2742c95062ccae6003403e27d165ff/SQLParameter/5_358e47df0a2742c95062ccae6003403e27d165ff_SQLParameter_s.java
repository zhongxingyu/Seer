 /*
  * Copyright 2011 Piero Ottuzzi <piero.ottuzzi@brucalipto.org>
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 package org.brucalipto.sqlutil;
 
 import java.io.Serializable;
 import java.sql.Timestamp;
 import java.sql.Types;
 import java.util.Date;
 
 /**
  * Class rappresenting a parameter in a SQL statement
  * @author Piero Ottuzzi <piero.ottuzzi@brucalipto.org>
  */
 public class SQLParameter implements Serializable, Cloneable
 {
    /**
	 * 
	 */
	private static final long serialVersionUID = 2752389841215169269L;
 	public final int sqlType;
     public final Serializable value;
 
     /**
      * Constructor
      * @param sqlType An int rappresenting a java.sql.Types of this object
      * @param value An Object
      */
     public SQLParameter(int sqlType, Serializable value)
     {
         this.sqlType = sqlType;
         this.value = value;
     }
 
     /**
      * Method to get the java.sql.Types
      * @return An int rappresenting a java.sql.Types of this object
      */
     public int getSqlType()
     {
         return this.sqlType;
     }
 
     /**
      * Method to get the value associated to the SqlType
      * @return An Object
      */
     public Object getValue()
     {
         return this.value;
     }
 
     /**
      * Overrides Object's toString()
      * @return A String rappresenting a SQLParameter
      */
     public String toString()
     {
         return "'Types."+SQLUtilTypes.SQL_TYPES.get(Integer.valueOf(""+this.sqlType))+"'->'"+this.value+"'";
     }
 
     /**
      * An utility method to get an instance of SQLParameter
      * @param value The object you need to represent as SQLParameter
      * @return The SQLParameter representing the input object
      */
     public static SQLParameter getSQLParameter(final Serializable value)
     {
         if (value instanceof String)
         {
             return new SQLParameter(Types.VARCHAR, value);
         }
         else if (value instanceof Character)
         {
             return new SQLParameter(Types.CHAR, value);
         }
         else if (value instanceof Integer)
         {
             return new SQLParameter(Types.INTEGER, value);
         }
         else if (value instanceof Long)
         {
             return new SQLParameter(Types.INTEGER, value);
         }
         else if (value instanceof Double)
         {
             return new SQLParameter(Types.DOUBLE, value);
         }
         else if (value instanceof Float)
         {
             return new SQLParameter(Types.FLOAT, value);
         }
         else if (value instanceof Boolean)
         {
             return new SQLParameter(Types.BOOLEAN, value);
         }
         else if (value instanceof Timestamp)
         {
             return new SQLParameter(Types.TIMESTAMP, value);
         }
         else if (value instanceof Date)
         {
             return new SQLParameter(Types.DATE, value);
         }
         else
         {
             return new SQLParameter(Types.OTHER,  value);
         }
     }
     
     public Object clone()
     {
     	return new SQLParameter(this.sqlType, this.value);
     }
 }
