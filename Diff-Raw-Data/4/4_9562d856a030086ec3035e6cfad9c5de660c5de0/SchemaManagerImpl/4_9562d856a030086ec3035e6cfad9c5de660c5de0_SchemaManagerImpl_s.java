 /*
  * Copyright (c) 2008 Hidenori Sugiyama
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
 package org.madogiwa.plaintable.impl;
 
 import org.madogiwa.plaintable.DatabaseSchema;
 import org.madogiwa.plaintable.PlainTableException;
 import org.madogiwa.plaintable.SchemaManager;
 import org.madogiwa.plaintable.schema.AttributeColumn;
 import org.madogiwa.plaintable.schema.Index;
 import org.madogiwa.plaintable.schema.ReferenceKey;
 import org.madogiwa.plaintable.schema.Schema;
 import org.madogiwa.plaintable.schema.SchemaDefinition;
 import org.madogiwa.plaintable.schema.SchemaReference;
 import org.madogiwa.plaintable.schema.SyntheticKey;
 import org.madogiwa.plaintable.schema.annot.Attribute;
 import org.madogiwa.plaintable.schema.annot.Reference;
 import org.madogiwa.plaintable.schema.annot.Table;
 import org.madogiwa.plaintable.schema.attr.StringAttribute;
 import org.madogiwa.plaintable.util.ReflectionUtils;
 
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Logger;
 
 /**
  * @author Hidenori Sugiyama
  * 
  */
 public class SchemaManagerImpl implements SchemaManager {
 
 	private static Logger logger = Logger.getLogger(SchemaManagerImpl.class
 			.getName());
 
 	private Map<String, Schema> schemaMap = new HashMap<String, Schema>();
 
 	private DatabaseSchema databaseSchema;
 
 	private String prefix;
 
 	/**
 	 * @param databaseSchema
 	 */
 	public SchemaManagerImpl(DatabaseSchema databaseSchema, String prefix) {
 		this.databaseSchema = databaseSchema;
 		this.prefix = prefix;
 	}
 
 	public Schema getSchema(String schemaName) {
 		return schemaMap.get(schemaName);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.TableManager#manage(java.lang.Class)
 	 */
 	public void manage(Class<? extends SchemaDefinition> clazz) {
 		Class<? extends SchemaDefinition> implClazz = findImplClass(clazz);
 		Schema schema = extractSchemaFromClass(implClazz);
 		updateClassFields(implClazz, schema);
 		manage(schema);
 	}
 
 	private Class<? extends SchemaDefinition> findImplClass(Class<? extends SchemaDefinition> clazz) {
 		Table annot = (Table) clazz.getAnnotation(Table.class);
 		if (annot != null) {
 			return clazz;
 		}
 
 		// find Scala object
 		Class<?> implClass = ReflectionUtils.findClass(clazz.getCanonicalName() + "$");
		if (implClass != null && SchemaDefinition.class.isAssignableFrom(implClass)) {
			return findImplClass((Class<SchemaDefinition>)implClass);
 		}
 
 		throw new RuntimeException(String.format(
 				"%s is not a table schema", clazz));
 	}
 
 	/**
 	 * @param clazz
 	 * @return
 	 */
 	private Schema extractSchemaFromClass(Class<? extends SchemaDefinition> clazz) {
 		String tableName = clazz.getCanonicalName();
 
 		Table annot = (Table) clazz.getAnnotation(Table.class);
 		if (annot != null) {
 			tableName = annot.name();
 		}
 
 		if (tableName == null || tableName.equals("")) {
 			throw new RuntimeException(String.format("can't detect schema name from ", clazz));
 		}
 
 		Schema schema = extractSchemaFromClass(clazz, tableName);
 		verifySchema(schema);
 		return schema;
 	}
 
 	/**
 	 * @param clazz
 	 * @param name
 	 */
 	private Schema extractSchemaFromClass(Class<? extends SchemaDefinition> clazz,
 			String name) {
 
 		Schema schema = new Schema(prefix, name);
 
 		Field[] fields = clazz.getDeclaredFields();
 		for (Field field : fields) {
 			field.setAccessible(true);
 			if (field.getType().equals(SyntheticKey.class)) {
 				if (schema.getPrimaryKey() != null) {
 					throw new RuntimeException(String.format(
 							"%s has duplicate SyntheticKey", clazz));
 				}
 
 				SyntheticKey key = new SyntheticKey(schema, field.getName());
 				schema.setPrimaryKey(key);
 			} else if (field.getType().equals(ReferenceKey.class)) {
 				Reference reference = field.getAnnotation(Reference.class);
 
 				ReferenceKey ref = new ReferenceKey(schema,
 						field.getName(), new SchemaReference(reference
 						.target().getCanonicalName()));
 				ref.setCascade(reference.cascade());
 				schema.addReferenceKey(ref);
 
 				Index index = new Index(schema, ref);
 				index.setUnique(reference.unique());
 				schema.addIndex(index);
 
 			} else if (AttributeColumn.class.isAssignableFrom(field
 					.getType())) {
 				Attribute column = field.getAnnotation(Attribute.class);
 
 				try {
 					Constructor<?> constructor = field.getType()
 							.getConstructor(
 									new Class[]{Schema.class,
 											String.class});
 					AttributeColumn attr = (AttributeColumn) constructor
 							.newInstance(new Object[] { schema,
 									field.getName() });
 					if (column != null) {
 						attr.setNullable(column.nullable());
 						attr.setLength(column.length());
 					} else {
 						attr.setNullable(false);
 						attr.setLength(-1);
 					}
 
 					if (attr instanceof StringAttribute && attr.getLength() != -1) {
 						logger.warning(String.format("%s: length is ignored", attr.getName()));
 						attr.setLength(-1);
 					}
 
 					schema.addAttribute(attr);
 
 					if (column != null && (column.indexed() || column.unique())) {
 						Index index = new Index(schema, attr);
 						index.setUnique(column.unique());
 						schema.addIndex(index);
 					}
 				} catch (SecurityException e) {
 					throw new RuntimeException(e);
 				} catch (NoSuchMethodException e) {
 					throw new RuntimeException(e);
 				} catch (IllegalArgumentException e) {
 					throw new RuntimeException(e);
 				} catch (InstantiationException e) {
 					throw new RuntimeException(e);
 				} catch (IllegalAccessException e) {
 					throw new RuntimeException(e);
 				} catch (InvocationTargetException e) {
 					throw new RuntimeException(e);
 				}
 			}
 		}
 
 		return schema;
 	}
 
 	private void verifySchema(Schema schema) {
 		if (schema.getPrimaryKey() == null) {
 			throw new RuntimeException(String.format("schema %s has not a primary key.", schema.getName()));
 		}
 	}
 
 	/**
 	 * @param clazz
 	 * @param schema
 	 */
 	private void updateClassFields(Class<? extends SchemaDefinition> clazz,
 			Schema schema) {
 		try {
 			Object instance = ReflectionUtils.findInstance(clazz);
 
 			Field[] fields = clazz.getDeclaredFields();
 			for (Field field : fields) {
 				field.setAccessible(true);
 				if (field.getType().equals(Schema.class)) {
 					field.set(instance, schema);
 				} else if (field.getType().equals(SyntheticKey.class)) {
 					field.set(instance, schema.getPrimaryKey());
 				} else if (field.getType().equals(ReferenceKey.class)) {
 					Set<ReferenceKey> keySet = schema.getReferenceKeys();
 					for (ReferenceKey key : keySet) {
 						if (field.getName().equals(key.getName())) {
 							field.set(instance, key);
 						}
 					}
 				} else if (AttributeColumn.class.isAssignableFrom(field
 						.getType())) {
 					Set<AttributeColumn> attrSet = schema.getAttributes();
 					for (AttributeColumn attr : attrSet) {
 						if (field.getName().equals(attr.getName())) {
 							field.set(instance, attr);
 						}
 					}
 				}
 			}
 		} catch (IllegalArgumentException e) {
 			throw new RuntimeException(e);
 		} catch (IllegalAccessException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.madogiwa.plaintable.TableManager#addTable(org.madogiwa.plaintable
 	 * .schema.TableSchema)
 	 */
 	public void manage(Schema schema) {
 		if (schemaMap.containsKey(schema.getFullName())) {
 			return;
 		}
 
 		schemaMap.put(schema.getFullName(), schema);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.TableManager#sync(org.madogiwa.plaintable.
 	 * TableManager.SynchronizeMode)
 	 */
 	public boolean sync(SynchronizeMode mode) throws PlainTableException {
 		return sync(mode, false);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.madogiwa.plaintable.SchemaManager#sync(org.madogiwa.plaintable.
 	 * SchemaManager.SynchronizeMode, boolean)
 	 */
 	public boolean sync(SynchronizeMode mode, boolean forced)
 			throws PlainTableException {
 		try {
 			databaseSchema.open();
 
 			Map<String, Schema> currentMap = databaseSchema.retrieveSchemaMap();
 			Set<String> dirtyNames = diffSchema(currentMap, schemaMap);
 
 			if (dirtyNames.size() == 0 && !forced) {
 				return false;
 			}
 
 			switch(mode) {
 				case NONE:
 					return true;
 				case CHECK_COMPATIBILITY:
 					throw new RuntimeException(
 							"CHECK_COMPATIBILITY not implemented currently");
 				case UPDATE_ONLY:
 					throw new RuntimeException(
 							"UPDATE_ONLY not implemented currently");
 				case DROP_AND_CREATE:
 					dropAll(buildSchemaMap(currentMap, dirtyNames));
 					createAll(buildSchemaMap(schemaMap, dirtyNames));
 					break;
 				case ALL_DROP_AND_CREATE:
 					dropAll(currentMap);
 					createAll(schemaMap);
 					break;
 				default:
 					throw new RuntimeException(
 							String.format("mode %s is not supported", mode));
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				databaseSchema.close();
 			} catch (SQLException e) {
 				throw new PlainTableException(e);
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * @param current
 	 * @param target
 	 * @return
 	 */
 	private Set<String> diffSchema(Map<String, Schema> current,
 			Map<String, Schema> target) {
 		Set<String> diffNames = new HashSet<String>();
 
 		for (String name : target.keySet()) {
 			if (!current.containsKey(name)) {
 				diffNames.add(name);
 			} else {
 				if (diffSchema(current.get(name), target.get(name))) {
 					diffNames.add(name);
 				}
 			}
 		}
 
 		return diffNames;
 	}
 
 	private boolean diffSchema(Schema s1, Schema s2) {
 		return !s1.equals(s2);
 	}
 
 	/**
 	 * @param source
 	 * @param names
 	 * @return
 	 */
 	private Map<String, Schema> buildSchemaMap(Map<String, Schema> source,
 			Set<String> names) {
 		Map<String, Schema> map = new HashMap<String, Schema>();
 		for (String name : names) {
 			map.put(name, source.get(name));
 		}
 		return map;
 	}
 
 	/**
 	 * @param schemaMap
 	 * @throws SQLException
 	 */
 	private void dropAll(Map<String, Schema> schemaMap) throws SQLException {
 		Map<String, Schema> remainMap = new HashMap<String, Schema>(schemaMap);
 		for (Schema schema : schemaMap.values()) {
 			drop(remainMap, schema.getFullName());
 		}
 	}
 
 	/**
 	 * @param map
 	 * @param name
 	 * @throws SQLException
 	 */
 	private void drop(Map<String, Schema> map, String name) throws SQLException {
 		Schema schema = map.get(name);
 		map.remove(name);
 
 		if (schema == null) {
 			return;
 		}
 
 		logger.info("drop table " + schema.getFullName());
 
 		for(String schemaName : map.keySet()) {
 			Schema s = map.get(schemaName);
 			for (ReferenceKey key : s.getReferenceKeys()) {
 				if (key.getTarget().getSchemaName().equals(schema.getName())) {
 					drop(map, key.getSchema().getName());
 				}
 			}
 		}
 		databaseSchema.dropTable(schema);
 	}
 
 	/**
 	 * @param schemaMap
 	 * @throws SQLException
 	 */
 	private void createAll(Map<String, Schema> schemaMap) throws SQLException {
 		Map<String, Schema> remainMap = new HashMap<String, Schema>(schemaMap);
 		for (Schema schema : schemaMap.values()) {
 			create(remainMap, schema.getFullName());
 		}
 	}
 
 	/**
 	 * @param map
 	 * @param name
 	 * @throws SQLException
 	 */
 	private void create(Map<String, Schema> map, String name)
 			throws SQLException {
 		Schema schema = map.get(name);
 		map.remove(name);
 
 		if (schema == null) {
 			return;
 		}
 
 		logger.info("create table " + schema.getFullName());
 
 		for (ReferenceKey referenceKey : schema.getReferenceKeys()) {
 			create(map, referenceKey.getTarget().getSchemaName());
 		}
 		databaseSchema.createTable(schema);
 	}
 
 }
