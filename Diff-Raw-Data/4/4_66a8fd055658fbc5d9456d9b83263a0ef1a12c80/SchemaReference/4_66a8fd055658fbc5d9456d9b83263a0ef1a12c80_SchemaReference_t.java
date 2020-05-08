 /*
  * Copyright (c) 2009 Hidenori Sugiyama
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
 
 /**
  * 
  */
 package org.madogiwa.plaintable.schema;
 
import org.madogiwa.plaintable.schema.annot.Table;
 import org.madogiwa.plaintable.util.ReflectionUtils;
 
 import java.io.Serializable;
 
 /**
  * @author Hidenori Sugiyama
  * 
  */
 public class SchemaReference implements Serializable {
 
 	private Schema schema;
 
 	private String schemaName;
 
 	private String className;
 
 	/**
 	 * @param clazz
 	 */
 	public SchemaReference(Class<?> clazz) {
 		schemaName = ReflectionUtils.findSchema(clazz).getName();
 		className = clazz.getCanonicalName();
 	}
 
 	/**
 	 * @param className
 	 */
 	public SchemaReference(String className) {
 		this.className = className;
 	}
 
 	/**
 	 * @param schema
 	 */
 	public SchemaReference(Schema schema) {
 		this.schema = schema;
 	}
 
 	/**
 	 * @return
 	 */
 	public String getSchemaName() {
 		if (schemaName != null) {
 			return schemaName;
 		}
 
 		return getSchema().getName();
 	}
 
 	/**
 	 * @return
 	 */
 	public Schema getSchema() {
 		if (schema == null) {
 			Class<?> clazz = ReflectionUtils.findClass(className);
 			schema = getSchemaFromClass(clazz);
 		}
 
 		return schema;
 	}
 
 	private Schema getSchemaFromClass(Class<?> clazz) {
 		Schema foundSchema = ReflectionUtils.findSchema(clazz);
 		if (foundSchema != null) {
 			return foundSchema;
 		}
 
 		Class<?> implClazz = ReflectionUtils.findClass(clazz.getCanonicalName() + "$");
		if (implClazz != null && implClazz.getAnnotation(Table.class) != null) {
 			return getSchemaFromClass(implClazz);
 		}
 
 		return null;
 	}
 
 }
