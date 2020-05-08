 package org.eweb4j.orm.sql;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.Method;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.persistence.JoinColumn;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToOne;
 
 import org.eweb4j.orm.config.ORMConfigBeanUtil;
 import org.eweb4j.util.ClassUtil;
 import org.eweb4j.util.ReflectUtil;
 
 /**
  * 生成更新语句
  * 
  * @author cfuture.aw
  * @since v1.a.0
  */
 @SuppressWarnings("all")
 public class UpdateSqlCreator<T> {
 	private T[] ts;
 
 	public UpdateSqlCreator(T[] ts) {
 		T[] tmp = null;
 		if (ts != null && ts.length > 0) {
 			tmp = ts;
 		}
 		this.ts = tmp;
 	}
 
 	public Sql[] update(String condition) {
 		Sql[] sqls = new Sql[ts.length];
 		if (this.ts != null && this.ts.length > 0) {
 			for (int i = 0; i < ts.length; ++i) {
 				sqls[i] = this.makeSQL(ts[i], condition);
 			}
 		}
 		return sqls;
 	}
 
 	private Sql makeSQL(T t, String condition) {
 		Sql sql = new Sql();
 		Class<?> clazz = t.getClass();
 		String table = ORMConfigBeanUtil.getTable(clazz, false);
 		sql.sql = String.format("UPDATE %s %s ;", table, condition);
 		
 		return sql;
 	}
 
 	public Sql[] update() throws SqlCreateException {
 		Sql[] sqls = new Sql[ts.length];
 		for (int i = 0; i < ts.length; ++i) {
 			sqls[i] = makeSQL(ts[i]);
 		}
 
 		return sqls;
 	}
 
 	public Sql[] update(String... fields) throws SqlCreateException {
 		Sql[] sqls = new Sql[ts.length];
 		for (int i = 0; i < ts.length; ++i) {
 			sqls[i] = makeSQL(ts[i], fields);
 		}
 
 		return sqls;
 	}
 
 	public Sql[] update(String[] fields, String[] values)
 			throws SqlCreateException {
 		Sql[] sqls = new Sql[ts.length];
 		for (int i = 0; i < ts.length; ++i) {
 			sqls[i] = makeSQL(ts[i], fields, values);
 		}
 
 		return sqls;
 	}
 
 	private Sql makeSQL(T t) throws SqlCreateException {
 		Sql sql = new Sql();
 		Class<?> clazz = t.getClass();
 		String table;
 		String[] columns;
 		String[] fields;
 		Object[] values = null;
 		String idColumn;
 		String idField;
 		Object idValue = null;
 		HashMap<String, Object> map = null;
 		if (Map.class.isAssignableFrom(clazz)) {
 			map = (HashMap) t;
 			table = (String) map.get("table");
 			idColumn = (String) map.get("idColumn");
 			idField = idColumn;
 			idValue = map.get("idValue");
 			columns = (String[]) map.get("columns");
 			fields = columns;
 			values = (Object[]) map.get("values");
 		} else {
 			table = ORMConfigBeanUtil.getTable(clazz, false);
 			columns = ORMConfigBeanUtil.getColumns(clazz);
 			fields = ORMConfigBeanUtil.getFields(clazz);
 			idColumn = ORMConfigBeanUtil.getIdColumn(clazz);
 			idField = ORMConfigBeanUtil.getIdField(clazz);
 		}
 
 		StringBuilder valuesSb = new StringBuilder();
 		ReflectUtil ru = new ReflectUtil(t);
 		try {
 			if (map == null) {
 				Method idGetter = ru.getGetter(idField);
 				if (idGetter == null)
 					throw new SqlCreateException("can not find id getter");
 				idValue = idGetter.invoke(t);
 			}
 
 			for (int i = 0; i < columns.length; i++) {
 				String column = columns[i];
 				String field = fields[i];
 				Object value = null;
 				// id 字段不允许
 				if (idColumn != null && idColumn.equalsIgnoreCase(column))
 					continue;
 
 				if (map != null && values != null) {
 					value = values[i];
 				} else {
 					Method getter = ru.getGetter(field);
 					if (getter == null)
 						continue;
 
 					Object _value = getter.invoke(t);
 					if (_value == null)
 						continue;
 
 					if (ClassUtil.isPojo(_value.getClass())) {
 						Field f = ru.getField(field);
 						OneToOne oneAnn = getter.getAnnotation(OneToOne.class);
 						if (oneAnn == null)
 							oneAnn = f.getAnnotation(OneToOne.class);
 						
 						ManyToOne manyToOneAnn = null;
 						if (oneAnn == null){
 							manyToOneAnn = getter.getAnnotation(ManyToOne.class);
 							if (manyToOneAnn == null)
 								manyToOneAnn = f.getAnnotation(ManyToOne.class);
 							
 						}
 						
 						if (oneAnn != null || manyToOneAnn != null){ 
 							JoinColumn joinColAnn = getter.getAnnotation(JoinColumn.class);
 							if (joinColAnn == null)
 								joinColAnn = f.getAnnotation(JoinColumn.class);
 							
 							if (joinColAnn != null && joinColAnn.referencedColumnName().trim().length() > 0){
 								String refCol = joinColAnn.referencedColumnName();
 								String refField = ORMConfigBeanUtil.getField(_value.getClass(), refCol);
 								ReflectUtil tarRu = new ReflectUtil(_value);
 								Method tarFKGetter = tarRu.getGetter(refField);
 								value = tarFKGetter.invoke(_value);
 							}else{
 								ReflectUtil tarRu = new ReflectUtil(_value);
 								String tarFKField = ORMConfigBeanUtil.getIdField(_value.getClass());
 								if (tarFKField != null){
 									Method tarFKGetter = tarRu.getGetter(tarFKField);
 									value = tarFKGetter.invoke(_value);
 								}
 							}
 						}
 						if (value == null)
 							continue;
 					}else
 						value = _value;
 				}
 
 				if (valuesSb.length() > 0)
 					valuesSb.append(",");
 
 //				valuesSb.append(column).append(" = '").append(value).append("'");
 				valuesSb.append(column).append(" = ? ");
 				sql.args.add(value);
 			}
 		} catch (Exception e) {
 			throw new SqlCreateException("" + e.toString(), e);
 		}
 
 //		String condition = new StringBuilder().append(idColumn).append(" = ").append("'").append(idValue).append("'").toString();
 		String condition = new StringBuilder().append(idColumn).append(" = ? ").toString();
 		sql.args.add(idValue);
 		sql.sql = String.format("UPDATE %s SET %s WHERE %s ;", table, valuesSb, condition);
 		return sql;
 	}
 
 	private Sql makeSQL(T t, String[] fields) throws SqlCreateException {
 		Sql sql = new Sql();
 		Class<?> clazz = t.getClass();
		//if fields is empty
		if (fields == null || fields.length == 0){
			fields = ORMConfigBeanUtil.getFields(clazz);
		}
 		String table = ORMConfigBeanUtil.getTable(clazz, false);
 		StringBuilder values = new StringBuilder();
 		ReflectUtil ru = new ReflectUtil(t);
 		String[] columns = ORMConfigBeanUtil.getColumns(clazz, fields);
 		String idColumn = ORMConfigBeanUtil.getIdColumn(clazz);
 		String idField = ORMConfigBeanUtil.getIdField(clazz);
 		Method idGetter = ru.getGetter(idField);
 		if (idGetter == null)
 			throw new SqlCreateException("can not find id getter.");
 		Object idValue = null;
 		try {
 			idValue = idGetter.invoke(t);
 		} catch (Exception e) {
 			throw new SqlCreateException(idGetter + " invoke exception " + e.toString(), e);
 		}
 
 		for (int i = 0; i < fields.length; i++) {
 			String field = fields[i];
 			String column = columns[i];
 			Method getter = ru.getGetter(field);
 			if (getter == null)
 				continue;
 
 			try {
 				Object _value = getter.invoke(t);
 				if (_value == null)
 					continue;
 
 				Object value = null;
 
 				if (ClassUtil.isPojo(_value.getClass())) {
 					Field f = ru.getField(field);
 					OneToOne oneAnn = getter.getAnnotation(OneToOne.class);
 					if (oneAnn == null)
 						oneAnn = f.getAnnotation(OneToOne.class);
 					
 					ManyToOne manyToOneAnn = null;
 					if (oneAnn == null){
 						manyToOneAnn = getter.getAnnotation(ManyToOne.class);
 						if (manyToOneAnn == null)
 							manyToOneAnn = f.getAnnotation(ManyToOne.class);
 						
 					}
 					
 					if (oneAnn != null || manyToOneAnn != null){ 
 						JoinColumn joinColAnn = getter.getAnnotation(JoinColumn.class);
 						if (joinColAnn == null)
 							joinColAnn = f.getAnnotation(JoinColumn.class);
 						
 						if (joinColAnn != null && joinColAnn.referencedColumnName().trim().length() > 0){
 							String refCol = joinColAnn.referencedColumnName();
 							String refField = ORMConfigBeanUtil.getField(_value.getClass(), refCol);
 							ReflectUtil tarRu = new ReflectUtil(_value);
 							Method tarFKGetter = tarRu.getGetter(refField);
 							value = tarFKGetter.invoke(_value);
 						}else{
 							ReflectUtil tarRu = new ReflectUtil(_value);
 							String tarFKField = ORMConfigBeanUtil.getIdField(_value.getClass());
 							if (tarFKField != null){
 								Method tarFKGetter = tarRu.getGetter(tarFKField);
 								value = tarFKGetter.invoke(_value);
 							}
 						}
 					}
 					
 					if (value == null)
 						continue;
 					
 				}else{
 					value = _value;
 				}
 
 				if (values.length() > 0)
 					values.append(", ");
 
 //				values.append(column).append(" = '").append(value).append("'");
 				values.append(column).append(" = ? ");
 				sql.args.add(value);
 			} catch (Exception e) {
 				throw new SqlCreateException(idGetter + " invoke exception " + e.toString(), e);
 			}
 		}
 
 //		String condition = new StringBuilder().append(idColumn).append(" = ").append("'").append(idValue).append("'").toString();
 		String condition = new StringBuilder().append(idColumn).append(" = ? ").toString();
 		sql.args.add(idValue);
 		sql.sql = String.format("UPDATE %s SET %s WHERE %s ;", table, values,condition);
 		return sql;
 	}
 
 	private Sql makeSQL(T t, String[] fields, String[] values) throws SqlCreateException {
 		Sql sql = new Sql();
 		Class<?> clazz = t.getClass();
 		String table = ORMConfigBeanUtil.getTable(clazz, false);
 		ReflectUtil ru = new ReflectUtil(t);
 		String[] columns = ORMConfigBeanUtil.getColumns(clazz, fields);
 		String idColumn = ORMConfigBeanUtil.getIdColumn(clazz);
 		String idField = ORMConfigBeanUtil.getIdField(clazz);
 		Method idGetter = ru.getGetter(idField);
 		if (idGetter == null)
 			throw new SqlCreateException("can not find id getter.");
 		Object idValue = null;
 		try {
 			idValue = idGetter.invoke(t);
 		} catch (Exception e) {
 			throw new SqlCreateException(idGetter + " invoke exception " + e.toString(), e);
 		}
 		StringBuilder sb = new StringBuilder();
 		for (int i = 0; i < columns.length; ++i) {
 			String column = columns[i];
 			if (sb.length() > 0)
 				sb.append(", ");
 
 //			sb.append(column).append(" = '").append(values[i]).append("'");
 			sb.append(column).append(" = ?");
 			sql.args.add(values[i]);
 		}
 //		String condition = new StringBuilder().append(idColumn).append(" = ").append("'").append(idValue).append("'").toString();
 		String condition = new StringBuilder().append(idColumn).append(" = ? ").toString();
 		sql.args.add(idValue);
 		sql.sql = String.format("UPDATE %s SET %s WHERE %s ;", table, sb.toString(), condition);
 		
 		return sql;
 	}
 
 	public T[] getTs() {
 		T[] tmp = null;
 		if (ts != null && ts.length > 0) {
 			tmp = ts.clone();
 		}
 		return tmp;
 	}
 
 	public void setTs(T[] ts) {
 		T[] tmp = null;
 		if (ts != null && ts.length > 0) {
 			tmp = ts.clone();
 		}
 		this.ts = tmp;
 	}
 }
