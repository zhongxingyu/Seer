 package org.eweb4j.orm.config;
 
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.persistence.Column;
 import javax.persistence.JoinColumn;
 
 import org.eweb4j.cache.ORMConfigBeanCache;
 import org.eweb4j.orm.PropType;
 import org.eweb4j.orm.config.bean.ORMConfigBean;
 import org.eweb4j.orm.config.bean.Property;
 import org.eweb4j.util.ClassUtil;
 import org.eweb4j.util.CommonUtil;
 import org.eweb4j.util.ReflectUtil;
 
import test.po.Pet;

 @SuppressWarnings("all")
 public class ORMConfigBeanUtil {
 
 	public static String[] getToOneField(Class<?> clazz){
 		ORMConfigBean ormBean = ORMConfigBeanCache.get(clazz.getName());
 		if (ormBean == null)
 			return null;
 		
 		List<String> list = new ArrayList<String>();
 		
 		for (Property p : ormBean.getProperty()) {
 			if (PropType.ONE_ONE.equals(p.getType()) || PropType.MANY_ONE.equals(p.getType()))
 				list.add(p.getName());
 		}
 		
 		return list.toArray(new String[]{});
 	}
 	
 	public static String parseQuery(String query, Class<?> clazz) {
 		if (query == null || query.trim().length() == 0)
 			return query;
 		
 		if (query.startsWith("by")) {
 			query = query.replace("by", "");
 			String _query = query.replace("And", "#").replace("Or", "#");
 			String[] fields = _query.split("#");
 			for (String field : fields) {
 				
 				if (field.contains("Is")){
 					String[] ff = field.split("Is");
 					if (ff[1].equalsIgnoreCase("NotNull"))
 						ff[1] = "NOT NULL";
 					else if (ff[1].equalsIgnoreCase("Null"))
 						ff[1] = "NULL";
 					else{
 						query = query.replace(field, " "+getColumn(clazz, CommonUtil.toLowCaseFirst(field))+" = ? ");
 						continue;
 					}
 					query = query.replace(field.trim(), " "+getColumn(clazz, CommonUtil.toLowCaseFirst(ff[0]))+" IS " + ff[1] + " ");
 				}else if (field.contains("NotLike")){
 					query = query.replace(field, " "+getColumn(clazz, CommonUtil.toLowCaseFirst(field.replace("NotLike", "")))+" NOT LIKE ? ");
 				}else if (field.contains("Like")){
 					query = query.replace(field, " "+getColumn(clazz, CommonUtil.toLowCaseFirst(field.replace("Like", "")))+" LIKE ? ");
 				}else
 					query = query.replace(field, " "+getColumn(clazz, CommonUtil.toLowCaseFirst(field))+" = ? ");
 			}
 			
 			return query;
 		}else{
 			if (query.contains("="))
 				query = parse(query, clazz);
 			if (query.contains("<>"))
 				query = parse("<>",query, clazz);
 			if (query.contains("<="))
 				query = parse("<=",query, clazz);
 			if (query.contains("<"))
 				query = parse("<",query, clazz);
 			if (query.contains(">="))
 				query = parse(">=",query, clazz);
 			if (query.contains(">"))
 				query = parse(">",query, clazz);
 			if (query.contains("is"))
 				query = parse("is", query, clazz);
 			if (query.contains("not like"))
 				query = parse("not like", query, clazz);
 			if (query.contains("like"))
 				query = parse("like", query, clazz);
 			
 			return query;
 		}
 	}
 	private static String parse(String query, Class<?> clazz){
 		return parse(null, query, clazz);
 	}
 	private static String parse(String con, String query, Class<?> clazz){
 		if (con == null)
 			con = "=";
 		
 		String regex = "[a-zA-Z]+ "+con;
 		if ("=".equals(con))
 			regex = "[a-zA-Z]+[ ]?"+con;
 		Pattern pattern = Pattern.compile(regex);
 		Matcher matcher = pattern.matcher(new String(query));
 		while (matcher.find()) {
 			String g = matcher.group();
 			String field = g.replace(con, "");
 			String col = getColumn(clazz, field.trim());
 			query = query.replace(field.trim(), col);
 		}
 		
 		return query;
 	}
 	
 	public static <T> Object getIdVal(T _t) throws IllegalArgumentException,
 			IllegalAccessException, InvocationTargetException {
 		Class<?> clazz = _t.getClass();
 		if (Map.class.isAssignableFrom(clazz)) {
 			HashMap<String, Object> map = (HashMap<String, Object>) _t;
 			return map.get("idValue");
 		}
 		String _idField = ORMConfigBeanUtil.getIdField(_t.getClass());
 		if (_idField == null)
 			return null;
 		
 		ReflectUtil _ru = new ReflectUtil(_t);
 		Method _idGetter = _ru.getGetter(_idField);
 		return _idGetter.invoke(_t);
 	}
 
 	public static <T> String getIdColumn(T t) {
 		return getId(t, 1);
 	}
 	
 	public static <T> String getIdColumn(List<Property> props) {
 		for (Property p : props){
 			if (("true".equals(p.getPk()) || "1".equals(p.getPk()))
 					&& ("true".equals(p.getAutoIncrement()) || "1".equals(p.getAutoIncrement()))) {
 				return p.getColumn();
 			}
 		}
 		
 		return null;
 	}
 
 	public static <T> String getIdField(T t) {
 		return getId(t, 2);
 	}
 
 	/**
 	 * 获取自增长的主键名字
 	 * 
 	 * @param clazz
 	 * @param type
 	 *            1的时候获取的是数据库字段名，2的时候获取的是java类的属性名
 	 * @return
 	 */
 	public static <T> String getId(T t, int type) {
 		Class<?> clazz;
 		if (t instanceof Class) {
 			clazz = (Class<?>) t;
 		} else {
 			clazz = t.getClass();
 		}
 		if (!(t instanceof Class) && Map.class.isAssignableFrom(clazz)) {
 			HashMap<String, Object> map = (HashMap<String, Object>) t;
 			return (String) map.get("idColumn");
 		}
 
 		ORMConfigBean ormBean = ORMConfigBeanCache.get(clazz.getName());
 		if (ormBean == null)
 			return null;
 
 		for (Property property : ormBean.getProperty()) {
 			if (("true".equals(property.getPk()) || "1"
 					.equals(property.getPk()))
 					&& ("true".equals(property.getAutoIncrement()) || "1"
 							.equals(property.getAutoIncrement()))) {
 				if (1 == type)
 					return property.getColumn();
 				else if (2 == type)
 					return property.getName();
 
 				break;
 			}
 		}
 
 		return null;
 	}
 
 	public static <T> String getTable(T t) {
 		return getTable(t, false);
 	}
 	/**
 	 * get table name
 	 * 
 	 * @param clazz
 	 * @return
 	 */
 	public static <T> String getTable(T t, boolean isAlias) {
 		String table = null;
 		Class<?> clazz;
 		if (t instanceof Class) {
 			clazz = (Class<?>) t;
 		} else {
 			clazz = t.getClass();
 		}
 
 		if (!(t instanceof Class) && Map.class.isAssignableFrom(clazz)) {
 			HashMap<String, Object> map = (HashMap<String, Object>) t;
 			table = (String) map.get("table");
 			if (isAlias)
 				return table + " map";
 			return table;
 		}
 
 		ORMConfigBean ormBean = ORMConfigBeanCache.get(clazz.getName());
 		table = ormBean == null ? clazz.getSimpleName() : ormBean.getTable();
 		if (isAlias)
 			return table + " "+clazz.getSimpleName().toLowerCase();
 		
 		return table;
 	}
 
 	public static <T> String getSelectAllColumn(T t) {
 
 		Class<?> clazz;
 		if (t instanceof Class) {
 			clazz = (Class<?>) t;
 		} else {
 			clazz = t.getClass();
 		}
 		String[] columns;
 		if (!(t instanceof Class) && Map.class.isAssignableFrom(clazz)) {
 			HashMap<String, Object> map = (HashMap<String, Object>) t;
 			columns = (String[]) map.get("columns");
 		} else {
 			columns = ORMConfigBeanUtil.getColumns(clazz);
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for (String column : columns) {
 			if (sb.length() > 0)
 				sb.append(", ");
 
 			sb.append(clazz.getSimpleName().toLowerCase()+"."+column);
 		}
 
 		String allColumn = sb.toString();
 		if (allColumn.trim().length() == 0)
 			allColumn = "*";
 
 		return allColumn;
 	}
 
 	public static String[] getColumns(Class<?> clazz) {
 		return getColumnsOrFields(clazz, null, 3);
 	}
 
 	public static String[] getFields(Class<?> clazz) {
 		return getColumnsOrFields(clazz, null, 4);
 	}
 
 	public static String[] getColumns(Class<?> clazz, String[] fields) {
 		return getColumnsOrFields(clazz, fields, 1);
 	}
 
 	public static String getColumn(Class<?> clazz, String field) {
 		return getColumns(clazz, new String[] { field })[0];
 	}
 
 	public static String[] getFields(Class<?> clazz, String[] columns) {
 		return getColumnsOrFields(clazz, columns, 2);
 	}
 
 	public static String getField(Class<?> clazz, String column) {
 		return getColumnsOrFields(clazz, new String[] { column }, 2)[0];
 	}
 
 	public static <T> String[] getValues(T t) throws Exception {
 
 		if (!(t instanceof Class) && Map.class.isAssignableFrom(t.getClass())) {
 			HashMap<String, Object> map = (HashMap<String, Object>) t;
 			return (String[]) map.get("values");
 		}
 
 		String[] fields = ORMConfigBeanUtil.getFields(t.getClass());
 		String[] values = new String[fields.length];
 		ReflectUtil _ru = new ReflectUtil(t);
 		for (int i = 0; i < fields.length; i++) {
 			Method getter = _ru.getGetter(fields[i]);
 			Object val = getter.invoke(t);
 			if (val == null)
 				continue;
 			values[i] = String.valueOf(val);
 		}
 
 		return values;
 	}
 
 	public static <T> Object getValue(T _t, String field) throws Exception {
 
 		ReflectUtil _ru = new ReflectUtil(_t);
 		Method getter = _ru.getGetter(field);
 		Object val = getter.invoke(_t);
 		if (val == null)
 			return null;
 
 		Object value = String.valueOf(val);
 
 		return value;
 	}
 
 	/**
 	 * get columns through fields
 	 * 
 	 * @param clazz
 	 * @param fields
 	 * @param type
 	 *            1.getColumns 2.getFields 3.allColumns 4.allFields
 	 * 
 	 * @return
 	 */
 	private static <T> String[] getColumnsOrFields(T t, String[] strs, int type) {
 
 		Class<?> clazz;
 		if (t instanceof Class) {
 			clazz = (Class<?>) t;
 		} else {
 			clazz = t.getClass();
 		}
 
 		if (!(t instanceof Class) && Map.class.isAssignableFrom(clazz)) {
 			HashMap<String, Object> map = (HashMap<String, Object>) t;
 			return (String[]) map.get("columns");
 		}
 
 		if (strs == null)
 			strs = new String[] { "" };
 		
 		String[] result = strs.clone();
 		List<String> list = new ArrayList<String>();
 
 		ORMConfigBean ormBean = ORMConfigBeanCache.get(clazz.getName());
 		if (ormBean == null) {
 			try {
 				ReflectUtil ru = new ReflectUtil(clazz);
 				// String idColumn = getIdColumn(clazz);
 				for (int i = 0; i < strs.length; i++) {
 					boolean finished = false;
 					Field[] _fields = ru.getFields();
 					for (Field _f : _fields) {
 						String pName = _f.getName();
 						JoinColumn jc = _f.getAnnotation(JoinColumn.class);
 						Column c = _f.getAnnotation(Column.class);
 						String col = pName;
 						if (c != null && c.name().trim().length() > 0)
 							col = c.name();
 						
 						if (jc != null && jc.name().trim().length() > 0)
 							col = jc.name();
 						
 						if (finished)
 							break;
 						switch (type) {
 						case 1:
 							int dotIndex = strs[i].indexOf(".");
 							if (dotIndex > 0 && dotIndex < strs[i].length() -1){
 								String[] dots = strs[i].split("\\.");
 								StringBuilder builder = new StringBuilder();
 								Class<?> prevCls = null;
 								for (int j = 0; j < dots.length; j++){
 									String dot = dots[j];
 									Field field = ru.getField(dot);
 									if (field == null && dot.equals(clazz.getSimpleName().toLowerCase())){
 										if (builder.length() > 0)
 											builder.append(".");
 										builder.append(dot);
 										prevCls = clazz;
 									}else if(field != null){
 										if (j == dots.length-1){
 											if (builder.length() > 0)
 												builder.append(".");
 											builder.append(ORMConfigBeanUtil.getColumn(prevCls, dot));
 										}else{
 											Class<?> cls = ClassUtil.getGenericType(field);
 											if (cls != null){
 												if (ORMConfigBeanCache.get(cls.getName()) != null){
 													if (builder.length() > 0)
 														builder.append(".");
 													
 													builder.append(cls.getSimpleName().toLowerCase());
 													prevCls = cls;
 													ru = new ReflectUtil(cls);
 												}
 											}
 										}
 									}
 								}
 								
 								if (builder.length() > 0){
 									result[i] = builder.toString();
 									finished = true;
 									break;
 								}
 							}
 							if (pName.equals(strs[i])) {
 								result[i] = col;
 								finished = true;
 							}
 							break;
 						case 2:
 							if (col.equals(strs[i])) {
 								result[i] = pName;
 								finished = true;
 							}
 							break;
 						case 3:
 							list.add(col);
 							break;
 						case 4:
 							list.add(pName);
 						}
 					}
 				}
 			} catch (Throwable e) {
 				
 			}
 		}else {
 			try{
 				ReflectUtil ru = new ReflectUtil(clazz);
 				// String idColumn = getIdColumn(clazz);
 				for (int i = 0; i < strs.length; i++) {
 					boolean finished = false;
 					List<Property> properties = ormBean.getProperty();
 					for (Property p : properties) {
 						if (finished)
 							break;
 						switch (type) {
 						case 1:
 							int dotIndex = strs[i].indexOf(".");
 							if (dotIndex > 0 && dotIndex < strs[i].length() -1){
 								String[] dots = strs[i].split("\\.");
 								StringBuilder builder = new StringBuilder();
 								Class<?> prevCls = null;
 								for (int j = 0; j < dots.length; j++){
 									String dot = dots[j];
 									Field field = ru.getField(dot);
 									if (field == null && dot.equals(clazz.getSimpleName().toLowerCase())){
 										if (builder.length() > 0)
 											builder.append(".");
 										builder.append(dot);
 										prevCls = clazz;
 									}else if(field != null){
 										if (j == dots.length-1){
 											if (builder.length() > 0)
 												builder.append(".");
 											builder.append(ORMConfigBeanUtil.getColumn(prevCls, dot));
 										}else{
 											Class<?> cls = ClassUtil.getGenericType(field);
 											if (cls != null){
 												if (ORMConfigBeanCache.get(cls.getName()) != null){
 													if (builder.length() > 0)
 														builder.append(".");
 													
 													builder.append(cls.getSimpleName().toLowerCase());
 													prevCls = cls;
 													ru = new ReflectUtil(cls);
 												}
 											}
 										}
 									}
 								}
 								
 								if (builder.length() > 0){
 									result[i] = builder.toString();
 									finished = true;
 									break;
 								}
 							}
 							if (p.getName().equals(strs[i])) {
 								result[i] = p.getColumn();
 								finished = true;
 							}
 							break;
 						case 2:
 							if (p.getColumn().equals(strs[i])) {
 								result[i] = p.getName();
 								finished = true;
 							}
 							break;
 						case 3:
 							list.add(p.getColumn());
 							break;
 						case 4:
 							list.add(p.getName());
 						}
 					}
 				}
 			}catch(Exception e){
 				
 			}
 		}
 
 		return list.isEmpty() ? result : list.toArray(new String[] {});
 	}
	public static void main(String[] args) {
		String q = ORMConfigBeanUtil.parseQuery("byNameAndTypeOrNumberIsNull", Map.class);
		System.out.println(q);
	}
 }
