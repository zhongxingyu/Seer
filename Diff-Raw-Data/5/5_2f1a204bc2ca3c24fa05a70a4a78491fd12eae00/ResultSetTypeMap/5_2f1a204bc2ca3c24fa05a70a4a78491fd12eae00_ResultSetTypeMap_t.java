 /**
  *
  *  Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package org.apache.tuscany.das.rdb.graphbuilder.schema;
 
 import java.sql.Types;
 
 import org.apache.tuscany.sdo.SDOPackage;
 
 import commonj.sdo.Type;
 import commonj.sdo.helper.TypeHelper;
 
 /**
  */
 public class ResultSetTypeMap {
 
 	public static ResultSetTypeMap instance = new ResultSetTypeMap();
 
 	/**
 	 * Constructor for ResultSetTypeMap.
 	 */
 	protected ResultSetTypeMap() {
 		// Empty Constructor
 	}
 
 	/**
 	 * These mappings taken primarily from "JDBC API and Tutorial and Reference" by
 	 * Fisher, Ellis and Bruce.
 	 * 
 	 * @param type
 	 * @param isNullable
 	 * @return
 	 */
 	public Type getEDataType(int type, boolean isNullable) {
      
 	    TypeHelper helper = TypeHelper.INSTANCE;
         SDOPackage.eINSTANCE.eClass();
 		switch (type) {
 
 		case Types.CHAR:
 		case Types.VARCHAR:
 		case Types.LONGVARCHAR:
 			return helper.getType("commonj.sdo", "String");
 
 		case Types.NUMERIC:
 		case Types.DECIMAL:
 			return helper.getType("commonj.sdo", "Decimal");
 
 		case Types.BIT:
 		case Types.BOOLEAN:
 			if (isNullable)
 				return helper.getType("commonj.sdo", "Boolean");
 			else
 				return helper.getType("commonj.sdo", "boolean");
 
 		case Types.TINYINT:
 		case Types.SMALLINT:
 		case Types.INTEGER:
 			if (isNullable) {               
 				return helper.getType("commonj.sdo", "IntObject");
             } else
 				return helper.getType("commonj.sdo", "Int");
 
 		case Types.BIGINT:
 			if (isNullable)
 				return helper.getType("commonj.sdo", "Long");
 			else
 				return helper.getType("commonj.sdo", "long");
 
 		case Types.REAL:
 			if (isNullable)
				return helper.getType("commonj.sdo", "Float");
 			else
				return helper.getType("commonj.sdo", "float");
 
 		case Types.FLOAT:
 		case Types.DOUBLE:
 			if (isNullable)
 				return helper.getType("commonj.sdo", "Double");
 			else
 				return helper.getType("commonj.sdo", "double");
 
 		case Types.BINARY:
 		case Types.VARBINARY:
 		case Types.LONGVARBINARY:
 			return helper.getType("commonj.sdo", "ByteArray");
 
 		case Types.DATE:
 		case Types.TIME:
 		case Types.TIMESTAMP:
 			return helper.getType("commonj.sdo", "Date");
 
 		case Types.CLOB:
 			return helper.getType("commonj.sdo", "Clob");
 
 		case Types.BLOB:
 			return helper.getType("commonj.sdo", "Blob");
 
 		case Types.ARRAY:
 			return helper.getType("commonj.sdo", "Array");
 
 		case Types.DISTINCT:
 		case Types.STRUCT:
 		case Types.REF:
 		case Types.DATALINK:
 		case Types.JAVA_OBJECT:
 			return helper.getType("commonj.sdo", "Object");
 
 		default:
             return helper.getType("commonj.sdo", "Object");
 		}
 
 	}
 
 
 	public Type getType(int columnType, boolean b) {
 		return getEDataType(columnType, b);
 	}
 
 }
