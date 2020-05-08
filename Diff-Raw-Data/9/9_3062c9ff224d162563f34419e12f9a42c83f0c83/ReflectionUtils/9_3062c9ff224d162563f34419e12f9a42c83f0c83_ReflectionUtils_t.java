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
 package org.madogiwa.plaintable.util;
 
 import org.madogiwa.plaintable.schema.Schema;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Modifier;
 
 /**
  * @author Hidenori Sugiyama
  * 
  */
 public class ReflectionUtils {
 
 	public static Schema findSchema(Class<?> clazz) {
 		try {
 			Schema schema = findSchemaFromStaticField(clazz);
 			if (schema != null) {
 				return schema;
 			}
 
 			Object instance = findInstance(clazz);
 			if (instance == null) {
				Class scalaObject = findScalaObject(clazz);
				if (scalaObject == null) {
					return null;
				}
				instance = findInstance(scalaObject);
				if (instance == null) {
					return null;
				}
 			}
 
 			return findSchemaFromInstance(clazz, instance);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	private static Schema findSchemaFromStaticField(Class<?> clazz) throws IllegalAccessException {
 		for (Field field : clazz.getDeclaredFields()) {
 			field.setAccessible(true);
 			if (Modifier.isStatic(field.getModifiers())
 					&& field.getType().equals(Schema.class)) {
 				return (Schema) field.get(null);
 			}
 		}
 
 		return null;
 	}
 
 	private static Schema findSchemaFromInstance(Class<?> clazz, Object instance) throws IllegalAccessException {
 		for (Field field : clazz.getDeclaredFields()) {
 			field.setAccessible(true);
 			if (!Modifier.isStatic(field.getModifiers())
 					&& field.getType().equals(Schema.class)) {
 				return (Schema) field.get(instance);
 			}
 		}
 
 		return null;
 	}
 
 	public static Object findInstance(Class<?> clazz) throws IllegalAccessException {
 		Field[] fields = clazz.getFields();
 		for (Field field : fields) {
 			if (Modifier.isStatic(field.getModifiers()) &&
 					field.getType().equals(clazz)) {
 
 				return field.get(null);
 			}
 		}
 
 		return null;
 	}
 
 	public static Class<?> findClassByName(String className) {
 		try {
 			return Thread.currentThread().getContextClassLoader().loadClass(className);
 		} catch (ClassNotFoundException e) {
 			return null;
 		}
 	}
 
 	public static Class<?> findScalaObject(Class<?> clazz) {
 		try {
 			return clazz.getClassLoader().loadClass(clazz.getCanonicalName() + "$");
 		} catch (ClassNotFoundException e) {
 			return null;
 		}
 	}
 
 }
