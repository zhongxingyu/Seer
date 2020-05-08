 package com.ecolemo.jangorm;
 
 import static com.ecolemo.jangorm.util.KeyValueEntry.kv;
 
 import java.lang.reflect.Field;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.NoSuchElementException;
 
 import com.ecolemo.jangorm.aggregate.Aggregate;
 import com.ecolemo.jangorm.manager.ModelManager;
 import com.ecolemo.jangorm.util.DataMap;
 import com.j256.ormlite.field.FieldType;
 import com.j256.ormlite.table.DatabaseTableConfig;
 
 public class QuerySet<T extends Model> implements Iterable<T> {
 
 	protected ModelManager manager = ModelManager.getDefaultModelManager();
 
 	protected Class modelClass;
 	protected List<From> fromClause = new ArrayList<From>();
 	protected List<From> outerJoinClause = new ArrayList<From>();
 	protected List<From> selectRelatedClause = new ArrayList<From>();
 	protected List<Object> joinParameters = new ArrayList<Object>();
 	protected List<String> whereClause = new ArrayList<String>();
 	protected List<Object> whereParameters = new ArrayList<Object>();
 	protected List<String> orders = new ArrayList<String>();
 	protected List<Aggregate> aggregates = new ArrayList<Aggregate>();
 	protected List<String> selects = new ArrayList<String>();
 
 	protected int pageBegin = 0;
 	protected int pageSize = 0;
 
 	public QuerySet(Class<T> modelClass) {
 		this.modelClass = modelClass;
 
 		fromClause.add(new From(modelClass, null));
 	}
 
 	public QuerySet<T> filter(String where, Object... parameters) {
 		whereClause.add(where);
 		whereParameters.addAll(Arrays.asList(parameters));
 		return this;
 	}
 
 	public QuerySet<T> selectRelated(Class<? extends Model> foreignClass, String fieldName) {
 		From foreign = new From(foreignClass, fieldName);
 		selectRelatedClause.add(foreign);
 		return this;
 	}
 
 	public QuerySet<T> join(Class<? extends Model> joinClass, String fieldName) {
 		From foreign = new From(joinClass, fieldName);
 		fromClause.add(foreign);
 		String join = String.format("`%s`.id=`%s`.%s_id", fromClause.get(0).getTableName(), fieldName, fromClause.get(0).getTableName());
 		whereClause.add(join);
 		return this;
 	}
 	
 	public QuerySet<T> outerJoin(Class<? extends Model> joinClass, String fieldName, String where, Object... parameters) {
 		From foreign = new From(joinClass, fieldName, where);
 		outerJoinClause.add(foreign);
 		for (Object param : parameters) {
 			joinParameters.add(param);
 		}
 		return this;
 	}
 	
 	public QuerySet<T> orderBy(String order) {
 		orders.add(order);
 		return this;
 	}
 	
 	protected String getTableName(Class<? extends Model> class1) {
 		return class1.getSimpleName().toLowerCase();
 	}
 
 	@Override
 	public Iterator<T> iterator() {
 		return manager.queryForList(this).iterator();
 	}
 
 	public String buildSQL() {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("SELECT ");
 		if (aggregates.size() > 0) {
 			for (Aggregate aggregate : aggregates) {
 				buffer.append(aggregate + ",");
 			}
 		} else if (selects.size() > 0) {
 				for (String select: selects) {
 					buffer.append(select + ",");
 				}
 		} else {
 			for (From from : fromClause) {
 				for (String columns : from.getSelectColumns()) {
 					buffer.append(columns + ",");
 				}
 			}
 			for (From from : selectRelatedClause) {
 				for (String columns : from.getSelectColumns()) {
 					buffer.append(columns + ",");
 				}
 			}
 			for (From from : outerJoinClause) {
 				for (String columns : from.getSelectColumns()) {
 					buffer.append(columns + ",");
 				}
 			}
 		}
 		buffer.deleteCharAt(buffer.length() - 1);
 
 		buffer.append("\nFROM ");
 		for (From from : fromClause) {
 			buffer.append(String.format(" `%s` `%s`,", from.getTableName(), from.alias));
 		}
 		buffer.deleteCharAt(buffer.length() - 1);
 		
 		if (selectRelatedClause.size() > 0) {
 			for (From from : selectRelatedClause) {
 				buffer.append(" LEFT JOIN ");
 				buffer.append(String.format(" `%s` `%s`", from.getTableName(), from.alias));
 				buffer.append(" ON ");
 				buffer.append(String.format("`%s`.%s_id=`%s`.id", fromClause.get(0).getTableName(), from.alias, from.getTableName()));
 			}
 		}
 		if (outerJoinClause.size() > 0) {
 			buffer.append(" LEFT OUTER JOIN ");
 			for (From from : outerJoinClause) {
 				buffer.append(String.format(" `%s` `%s`", from.getTableName(), from.alias));
 				buffer.append(" ON ");
 				buffer.append(String.format("`%s`.id=`%s`.%s_id", fromClause.get(0).getTableName(), from.alias, fromClause.get(0).getTableName()));
 				buffer.append(" AND ");
 				buffer.append(from.getWhere());
 			}
 		}
 
 		if (whereClause.size() > 0) {
 			buffer.append("\nWHERE ");
 			for (String where : whereClause) {
 				buffer.append(where + "\n  AND ");
 			}
 			buffer.delete(buffer.length() - 4, buffer.length() - 1);
 		}
 		
 		if (orders.size() > 0) {
 			buffer.append("\nORDER BY ");
 			for (String order: orders) {
 				buffer.append(order+ ", ");
 			}
 			buffer.delete(buffer.length() - 2, buffer.length());
 		}
 		
 		if (pageSize > 0) {
 			buffer.append(String.format(" LIMIT %d, %d", pageBegin, pageSize));
 		}
 		return buffer.toString();
 	}
 
 	public T get(Entry<String, Object>... entries) {
 		From from = fromClause.get(0);
 		for (Entry<String, Object> entry : entries) {
 			whereClause.add(String.format("`%s`.`%s`=?", from.getTableName(), entry.getKey()));
 			whereParameters.add(entry.getValue());
 		}
 		
 		return manager.queryForList(this).iterator().next();
 	}
 
 	public T create(DataMap data) {
 		try {
			T object = (T) modelClass.newInstance();
			for (Entry<String, Object> entry : data.entrySet()) {
 				if (entry.getKey().endsWith("_id")) {
 					String key = entry.getKey().split("_")[0];
 					Field field = modelClass.getDeclaredField(key);
 					Model foreign = (Model) field.getType().newInstance();
 					foreign.set("id", entry.getValue());
 					object.set(key, foreign);
 					object.set(entry.getKey(), entry.getValue());
 				} else if (entry.getValue() instanceof Map) {
 					Field field = modelClass.getDeclaredField(entry.getKey());
 					Model foreign = (Model) field.getType().newInstance();
 					foreign.setAll((Map<String, Object>) entry.getValue());
 					object.set(entry.getKey(), foreign);
 				} else {
 					Field field = modelClass.getDeclaredField(entry.getKey());
 					if (field.getType().equals(Date.class) && entry.getValue() instanceof Long) {
 						object.set(entry.getKey(), new Date((Long) entry.getValue()));
 					} else {
 						object.set(entry.getKey(), entry.getValue());
 					}
 				}

 			}
			manager.insertModel(object);
			return object;
		} catch (InstantiationException e) {
			throw new ModelException(e);
		} catch (IllegalAccessException e) {
			throw new ModelException(e);
		} catch (SecurityException e) {
			throw new ModelException(e);
		} catch (NoSuchFieldException e) {
			throw new ModelException(e);
 		}
 	}
 		
 	
 	public T create(Entry<String, Object>... entries) {
 		try {
 			T object = (T) modelClass.newInstance();
 			for (Entry<String, Object> entry : entries) {
 				object.set(entry.getKey(), entry.getValue());
 			}
 			manager.insertModel(object);
 			return object;
 		} catch (InstantiationException e) {
 			throw new ModelException(e);
 		} catch (IllegalAccessException e) {
 			throw new ModelException(e);
 		}
 	}
 
 	public class From {
 		private Class<? extends Model> modelClass;
 		private String alias;
 		private DatabaseTableConfig tableConfig;
 		private String where;
 
 		public From(Class<? extends Model> modelClass, String alias) {
 			this(modelClass, alias, null);
 		}
 		
 		public From(Class<? extends Model> modelClass, String alias, String where) {
 			this.modelClass = modelClass;
 			this.alias = alias;
 			this.where = where;
 			try {
 				tableConfig = TableConfigCache._cache.get(manager.getConnectionSource(), modelClass);
 				if (this.alias == null)
 					this.alias = tableConfig.getTableName();
 			} catch (SQLException e) {
 				throw new ModelException(e);
 			}
 		}
 
 		public String getTableName() {
 			return tableConfig.getTableName();
 		}
 
 		public List<String> getSelectColumns() {
 			List<String> columns = new ArrayList<String>();
 
 			try {
 				for (FieldType type : tableConfig.getFieldTypes(manager.getDatabaseType())) {
 					String fieldName = type.getFieldName().toLowerCase();
 					if (type.isForeignCollection()) continue;
 					if (type.isForeign()) {
 						columns.add(String.format("`%s`.%s_id %s__%s_id", alias, fieldName, alias, fieldName));
 					} else {
 						columns.add(String.format("`%s`.`%s` %s__%s", alias, fieldName, alias,
 								fieldName));
 					}
 				}
 			} catch (SQLException e) {
 				throw new RuntimeException(e);
 			}
 
 			return columns;
 		}
 
 		public String getWhere() {
 			return where;
 		}
 	}
 
 	public ModelManager getManager() {
 		return manager;
 	}
 
 	public Class<T> getModelClass() {
 		return modelClass;
 	}
 
 	public List<From> getFromClause() {
 		return fromClause;
 	}
 
 	public List<String> getWhereClause() {
 		return whereClause;
 	}
 
 	public List<Object> getParameters() {
 		List<Object> params = new ArrayList<Object>();
 		params.addAll(joinParameters);
 		params.addAll(whereParameters);
 		return params;
 	}
 
 	public Class<? extends Model> findModelClass(String alias) {
 		for (From from : fromClause) {
 			if (alias.equals(from.alias)) return from.modelClass;
 		}
 		for (From from : outerJoinClause) {
 			if (alias.equals(from.alias)) return from.modelClass;
 		}
 		throw new ModelException("No model class found named " + alias);
 	}
 
 	public List<T> asList() {
 		List<T> list = new ArrayList<T>();
 		for (T object : this) {
 			list.add(object);
 		}
 		return list;
 	}
 	
 	public void delete() {
 		StringBuffer buffer = new StringBuffer();
 		buffer.append("DELETE FROM ");
 		for (From from : fromClause) {
 			buffer.append(String.format(" `%s`,", from.getTableName()));
 		}
 		buffer.deleteCharAt(buffer.length() - 1);
 		
 		if (whereClause.size() > 0) {
 			buffer.append("\nWHERE ");
 			for (String where : whereClause) {
 				buffer.append(where + "\n  AND ");
 			}
 			buffer.delete(buffer.length() - 4, buffer.length() - 1);
 		}
 		System.out.println(buffer.toString());
 		System.out.println("params:" + whereParameters);
 		
 		int rows = manager.executeUpdate(buffer.toString(), whereParameters.toArray());
 		System.out.println("rows:" + rows);
 		
 	}
 
 	public QuerySet<T> aggregate(Aggregate... aggregates) {
 		this.aggregates.addAll(Arrays.asList(aggregates));
 		return this;
 	}
 	public QuerySet<T> select(String... selects) {
 		this.selects.addAll(Arrays.asList(selects));
 		return this;
 	}
 
 	public T getByID(String resourceID) {
 		try {
 			return get(kv("id", resourceID));
 		} catch (NoSuchElementException e) {
 			throw new ModelException("Could not found " + modelClass + " for ID [" + resourceID + "].");
 		}
 	}
 
 	public long count() {
 		return this.select("count(id) " + modelClass.getSimpleName().toLowerCase() + "__count").get().getLong("count");
 	}
 
 	public QuerySet<T> limit(int pageBegin, int pageSize) {
 		
 		this.pageBegin = pageBegin;
 		this.pageSize = pageSize;
 		return this;
 	}
 
 }
