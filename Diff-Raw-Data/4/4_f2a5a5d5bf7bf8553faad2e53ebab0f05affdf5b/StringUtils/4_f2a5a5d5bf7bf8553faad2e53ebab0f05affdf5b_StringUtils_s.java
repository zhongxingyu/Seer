 /*******************************************************************************
  * Copyright (c) 2010 Oobium, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Jeremy Dowdall <jeremy@oobium.com> - initial API and implementation
  ******************************************************************************/
 package org.oobium.utils;
 
 import static java.lang.Character.isLowerCase;
 import static java.lang.Character.isUpperCase;
 import static org.oobium.utils.Utils.isEqual;
 import static org.oobium.utils.json.JsonUtils.toObject;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Array;
 import java.math.BigDecimal;
 import java.net.URLDecoder;
 import java.net.URLEncoder;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 
 import org.oobium.utils.json.JsonModel;
 import org.oobium.utils.json.JsonUtils;
 
 public class StringUtils {
 
 	public static String asString(Collection<?> collection) {
 		return asString(collection.toArray());
 	}
 
 	public static String asString(Object object) {
 		if(object == null) {
 			return "null";
 		}
 		if(object instanceof Collection<?>) {
 			return asString(((Collection<?>) object).toArray());
 		}
 		if(object.getClass().isArray()) {
 			Class<?> ctype = object.getClass().getComponentType();
 			if(ctype.isPrimitive()) {
 				if(ctype == int.class) {
 					return Arrays.toString((int[]) object);
 				}
 				else if(ctype == boolean.class) {
 					return Arrays.toString((boolean[]) object);
 				}
 				else if(ctype == byte.class) {
 					return Arrays.toString((byte[]) object);
 				}
 				else if(ctype == char.class) {
 					return Arrays.toString((char[]) object);
 				}
 				else if(ctype == double.class) {
 					return Arrays.toString((double[]) object);
 				}
 				else if(ctype == float.class) {
 					return Arrays.toString((float[]) object);
 				}
 				else if(ctype == long.class) {
 					return Arrays.toString((long[]) object);
 				}
 			} else {
 				return asString((Object[]) object);
 			}
 		}
 		return object.toString();
 	}
 	
 	public static String asString(Object[] oa) {
 		if(oa == null) {
 			return "null";
 		}
 		
 		StringBuilder sb = new StringBuilder();
 		sb.append('[');
 		for(int i = 0; i < oa.length; i++) {
 			if(i != 0) sb.append(',').append(' ');
 			if(oa[i] != null && oa[i].getClass().isArray()) {
 				sb.append(asString((Object[]) oa[i]));
 			} else {
 				sb.append(oa[i]);
 			}
 		}
 		sb.append(']');
 		return sb.toString();
 	}
 	
 	public static String[] attrDecode(String attribute) {
 		String[] nvp = attribute.split("=");
 		nvp[0] = decode(nvp[0]);
 		nvp[1] = (nvp.length == 1) ? "" : decode(nvp[1]);
 		return nvp;
 	}
 	
 	public static String attrEncode(String name, String value) {
 		StringBuilder sb = new StringBuilder(name.length() + ((value == null) ? 1 : (value.length() + 1)));
 		sb.append(name).append('=');
 		if(value != null) {
 			sb.append(value);
 		}
 		return sb.toString();
 	}
 
 	public static Map<String, String> attrsDecode(String attributes) {
 		Map<String, String> map = new HashMap<String, String>();
 		String[] attrs = attributes.split("&");
 		for(String attr : attrs) {
 			String[] nvp = attr.split("=");
 			String key = decode(nvp[0]);
 			String val = (nvp.length == 1) ? "" : decode(nvp[1]);
 			map.put(key, val);
 		}
 		return map;
 	}
 	
 	public static String attrsEncode(Map<String, String> attributes) {
 		if(attributes == null || attributes.isEmpty()) {
 			return "";
 		}
 
 		StringBuilder sb = new StringBuilder();
 		for(Iterator<String> iter = attributes.keySet().iterator(); iter.hasNext();) {
 			String key = iter.next();
 			if(key != null && key.length() > 0) {
 				String val = attributes.get(key);
 				try {
 					key = URLEncoder.encode(key, "UTF-8");
 					if(val == null) {
 						val = "";
 					} else {
 						val = URLEncoder.encode(val, "UTF-8");
 					}
 					sb.append(key);
 					sb.append('=');
 					sb.append(val);
 					if(iter.hasNext()) {
 						sb.append('&');
 					}
 				} catch(UnsupportedEncodingException e) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return sb.toString();
 	}
 	
 	// TODO this is not a StringUtil function anymore... move to JsonUtils?
 	public static boolean blank(Object obj) {
 		if(obj == null) {
 			return true;
 		}
 		
 		if(obj instanceof CharSequence) {
 			CharSequence cs = (CharSequence) obj;
 			for(int i = 0; i < cs.length(); i++) {
 				if(!Character.isWhitespace(cs.charAt(i))) {
 					return false;
 				}
 			}
 			return true;
 		}
 		
 		if(obj instanceof BigDecimal) {
 			return ((BigDecimal) obj).signum() == 0;
 		}
 		if(obj instanceof Double) {
 			return ((Double) obj).doubleValue() == 0d;
 		}
 		if(obj instanceof Float) {
 			return ((Double) obj).floatValue() == 0f;
 		}
 		if(obj instanceof Number) {
 			return ((Number) obj).intValue() == 0;
 		}
 		
 		if(obj instanceof Collection<?>) {
 			Collection<?> c = (Collection<?>) obj;
 			if(!c.isEmpty()) {
 				for(Object o : c) {
 					if(!blank(o)) {
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		
 		if(obj instanceof Map<?, ?>) {
 			Map<?, ?> m = (Map<?, ?>) obj;
 			if(!m.isEmpty()) {
 				for(Object o : m.values()) {
 					if(!blank(o)) {
 						return false;
 					}
 				}
 			}
 			return true;
 		}
 		
 		if(obj.getClass().isArray()) {
 			return Array.getLength(obj) == 0;
 		}
 		
 		if(obj instanceof JsonModel) {
 			return ((JsonModel) obj).isBlank();
 		}
 		return false;
 	}
 
 	public static String build(String...values) {
 		int len = 0;
 		for(String value : values) {
 			len += value.length();
 		}
 		StringBuilder sb = new StringBuilder(len);
 		for(String value : values) {
 			sb.append(value);
 		}
 		return sb.toString();
 	}
 	
 	/**
 	 * converts string to Camelcase:
 	 * <p>
 	 * my_model -> MyModel, MY_MODEL -> MyModel,
 	 * MODEL -> Model, myModel -> MyModel
 	 * </p>
 	 * @param string
 	 * @return
 	 */
 	public static String camelCase(String string) {
 		if(string == null) {
 			return "Null";
 		}
 		String[] sa = string.split("[_\\s]+");
 		StringBuilder sb = new StringBuilder();
 		for(String s : sa) {
 			boolean wasUpper = false;
 			char[] ca = s.toCharArray();
 			for(int i = 0; i < ca.length; i++) {
 				if(i == 0) {
 					sb.append(Character.toUpperCase(ca[i]));
 				} else {
 					if(wasUpper && (i+1 == ca.length || !isLowerCase(ca[i+1]))) {
 						sb.append(Character.toLowerCase(ca[i]));
 					} else {
 						sb.append(ca[i]);
 					}
 				}
 				wasUpper = isUpperCase(ca[i]);
 			}
 		}
 		return sb.toString();
 	}
 	
 	public static String className(String tableName) {
 		return camelCase(singular(tableName));
 	}
 
 	public static String columnName(Class<?> model, String relation) {
 		return tableName(model) + "__" + columnName(relation);
 	}
 
 	public static String columnName(String variable) {
 		return underscored(variable);
 	}
 	
 	public static String columnName(String table, String column) {
 		return table + "__" + column;
 	}
 
 	public static String[] columnNames(String table1, String column1, String table2, String column2) {
 		String name1 = columnName(table1, column1);
 		String name2 = columnName(table2, column2);
 		if(name1.compareTo(name2) <= 0) {
 			return new String[] { name1, name2 };
 		} else {
 			return new String[] { name2, name1 };
 		}
 	}
 
 	/**
 	 * convert the given string to one that is appropriate for use as a Java
 	 * Constant: MyModel -> MY_MODEL myModel -> MY_MODEL
 	 * 
 	 * @param singular
 	 * @return
 	 */
 	public static String constant(String singular) {
 		String sep = "_";
 		StringBuilder sb = new StringBuilder();
 		for(char c : singular.toCharArray()) {
 			if(Character.isUpperCase(c)) {
 				sb.append(sep).append(c);
 			} else {
 				sb.append(Character.toUpperCase(c));
 			}
 		}
 		return sb.toString();
 	}
 
 	public static boolean contains(Iterable<String> iterable, String string) {
 		for(String s : iterable) {
 			if(s.equals(string)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean contains(String[] array, String string) {
 		for(String s : array) {
 			if(s.equals(string)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static String controllerCanonicalName(String canonicalModelName) {
 		return controllersPackageName(canonicalModelName) + "." + simpleName(canonicalModelName) + "Controller";
 	}
 	
 	public static String controllerSimpleName(String modelName) {
 		return simpleName(modelName) + "Controller";
 	}
 	
 	public static String controllersPackageName(String canonicalModelName) {
 		String packageName = packageName(canonicalModelName);
 		int ix = packageName.lastIndexOf('.');
 		if(ix == -1) {
 			return "controllers";
 		} else {
 			return packageName.substring(0, ix) + ".controllers";
 		}
 	}
 	
 	public static int count(String[] strings) {
 		int i = 0;
 		for(String string : strings) {
 			i += string.length();
 		}
 		return i;
 	}
 	
 	/**
 	 * @param strings
 	 * @param joinBuffer the amount to add for each String segment
 	 * @return
 	 */
 	public static int count(String[] strings, int joinBuffer) {
 		int i = 0;
 		for(String string : strings) {
 			i += string.length() + joinBuffer;
 		}
 		return i;
 	}
 	
 	public static String dashed(String string) {
 		return separated(string, '-');
 	}
 	
 	public static String dateTimeTags(String prefix, DateFormat df, Date selection) {
 		StringBuilder sb = new StringBuilder();
 		dateTimeTags(sb, prefix, df, selection);
 		return sb.toString();
 	}
 	
 	public static void dateTimeTags(StringBuilder sb, String prefix, DateFormat df, Date selection) {
 		for(String pattern : getPatternComponents(df)) {
 			char c = pattern.charAt(0);
 			if(c == '\'') {
 				sb.append("<span>").append(pattern.substring(1, pattern.length()-1)).append("</span>");
 			} else {
 				int field = getCalendarField(c);
 				if(field < 0) {
 					sb.append("<span>").append(pattern).append("</span>");
 				} else {
 					// TODO allow setting name and id separately
 					String name = new StringBuilder().append(prefix).append('[').append(c).append(']').toString();
 					sb.append("<select id=\"").append(name).append("\" name=\"").append(name).append("\" >");
 					dateTimeTags(sb, pattern, field, selection);
 					sb.append("</select>");
 				}
 			}
 		}
 	}
 	
 	public static String dateTimeTags(String prefix, String format) {
 		return dateTimeTags(prefix, format, new Date());
 	}
 	
 	public static String dateTimeTags(String prefix, String format, Date selection) {
 		return dateTimeTags(prefix, new SimpleDateFormat(format), selection);
 	}
 	
 	public static void dateTimeTags(StringBuilder sb, String prefix, String format, Date selection) {
 		dateTimeTags(sb, prefix, new SimpleDateFormat(format), selection);
 	}
 	
	public static void dateTimeTags(StringBuilder sb, String prefix, String format, Date selection) {
		dateTimeTags(sb, prefix, new SimpleDateFormat(format), selection);
	}
	
 	private static void dateTimeTags(StringBuilder sb, String pattern, int field, Date selection) {
 		Calendar cal = GregorianCalendar.getInstance();
 		int min = (field == Calendar.YEAR) ? (cal.get(Calendar.YEAR) - 100) : cal.getMinimum(field);
 		int max = (field == Calendar.YEAR) ? (cal.get(Calendar.YEAR) + 10) : cal.getMaximum(field);
 		if(selection == null) {
 			selection = new Date();
 		}
 		cal.setTime(selection);
 		Integer sel = cal.get(field);
 		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
 		for(int i = min; i <= max; i++) {
 			cal.set(field, i);
 			optionTag(sb, sdf.format(cal.getTime()), i, sel);
 		}
 	}
 	
 	public static String decode(String string) {
 		try {
 			return URLDecoder.decode(string, "UTF-8");
 		} catch(UnsupportedEncodingException e) {
 			return "";
 		}
 	}
 	
 	/**
 	 * Convenience method equivalent to URLEncoder.encode(value, "UTF-8")
 	 * @param value
 	 * @return
 	 */
 	public static String encode(String value) {
 		try {
 			return URLEncoder.encode(value, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		return "";
 	}
 	
 	/**
 	 * Short cut for {@link #formEscape(Object)}
 	 */
 	public static String f(Object object) {
 		return formEscape(object);
 	}
 	
 	public static String field(String method) {
 		if(method.equals("get") || method.equals("set") || method.equals("has")) {
 			return null;
 		}
 		if(method.startsWith("get") || method.startsWith("set") || method.startsWith("has")) {
 			return Character.toLowerCase(method.charAt(3)) + method.substring(4);
 		}
 		if(method.startsWith("is")) {
 			return Character.toLowerCase(method.charAt(2)) + method.substring(3);
 		}
 		return Character.toLowerCase(method.charAt(0)) + method.substring(1);
 	}
 	
 	public static String format(Date date, String pattern) {
 		if(date == null) {
 			return "";
 		}
 		return new SimpleDateFormat(pattern).format(date);
 	}
 
 	public static String format(long dateInMillis, String pattern) {
 		return new SimpleDateFormat(pattern).format(new Date(dateInMillis));
 	}
 
 	/**
 	 * If the given object is a JsonModel, then return its id.
 	 * If it is a Map, then return its JSON representation.
 	 * Otherwise HTML escape the object.
 	 * @param object
 	 * @return
 	 */
 	public static String formEscape(Object object) {
 		if(object == null) {
 			return "";
 		}
 		if(object instanceof JsonModel) {
 			return String.valueOf(((JsonModel) object).getId());
 		}
 		if(object instanceof Map) {
 			return htmlEscape(JsonUtils.toJson(object));
 		}
 		if(object instanceof Date) {
 			return String.valueOf(((Date) object).getTime());
 		}
 		return htmlEscape(object);
 	}
 
 	public static int getCalendarField(char c) {
 		switch(c) {
 		case 'y': return Calendar.YEAR;
 		case 'M': return Calendar.MONTH;
 		case 'D': return Calendar.DAY_OF_YEAR;
 		case 'd': return Calendar.DAY_OF_MONTH;
 		case 'H': return Calendar.HOUR_OF_DAY;
 		case 'k': return Calendar.HOUR_OF_DAY;
 		case 'h': return Calendar.HOUR;
 		case 'K': return Calendar.HOUR;
 		case 'm': return Calendar.MINUTE;
 		case 's': return Calendar.SECOND;
 		case 'S': return Calendar.MILLISECOND;
 		case 'a': return Calendar.AM_PM;
 		default:  return -1;
 		}
 	}
 	
 	public static String getPattern(DateFormat df) {
 		return ((SimpleDateFormat) df).toPattern();
 	}
 	
 	public static String[] getPatternComponents(DateFormat df) {
 		String pattern = getPattern(df);
 		List<String> components = new ArrayList<String>();
 		StringBuilder sb = new StringBuilder();
 		for(int i = 0; i <= pattern.length(); i++) {
 			if(i == pattern.length()) {
 				components.add(sb.toString());
 			} else {
 				char c = pattern.charAt(i);
 				if(c == '\'') {
 					sb.append(pattern.charAt(i++));
 					while(i < pattern.length()) {
 						sb.append(pattern.charAt(i));
 						if(pattern.charAt(i) == '\'') {
 							break;
 						}
 						i++;
 					}
 					components.add(sb.toString());
 					sb = new StringBuilder();
 				} else {
 					if(i > 0 && (c != pattern.charAt(i-1))) {
 						components.add(sb.toString());
 						sb = new StringBuilder();
 					}
 					sb.append(c);
 				}
 			}
 		}
 		return components.toArray(new String[components.size()]);
 	}
 
 	public static String getResourceAsString(Class<?> clazz, String name) {
 		return getString(clazz.getResourceAsStream(name));
 	}
 	
 	public static String getString(InputStream in) {
 		if(in != null) {
 			try {
 				StringBuilder sb = new StringBuilder();
 				int c;
 				while((c = in.read()) != -1) {
 					sb.append((char) c);
 				}
 				return sb.toString();
 			} catch(IOException e) {
 				// discard
 			}
 		}
 		return null;
 	}
 	
 	public static String getterName(String variable) {
 		return getterName(variable, false);
 	}
 
 	public static String getterName(String variable, boolean bool) {
 		if(bool) {
 			return "is" + camelCase(variable);
 		} else {
 			return "get" + camelCase(variable);
 		}
 	}
 	
 	public static String h(Object obj) {
 		return htmlEscape(obj);
 	}
 	
 	public static String hasserName(String variable) {
 		return "has" + camelCase(variable);
 	}
 	
 	public static String htmlEscape(Object obj) {
 		if(obj == null) {
 			return "";
 		} else {
 			return obj.toString().replaceAll("&", "&amp;").replaceAll(">", "&gt;").replaceAll("<", "&lt;").replaceAll("\"", "&quot;");
 		}
 	}
 
 	/**
 	 * <p>
 	 * convert the given type implication to the closest real
 	 * type available.  only works for basic java types, otherwise
 	 * the original string is returned. It does return primitives.
 	 * </p>
 	 * <p>
 	 * Example 1: "string" => "String"
 	 * </p>
 	 * <p>
 	 * Example 2: "int" => "int"
 	 * </p>
 	 * @param type
 	 * @return
 	 */
 	public static String impliedType(String type) {
 		if(type == null || type.length() == 0) {
 			return "String";
 		}
 		if(type.endsWith("[]")) {
 			String base = type.substring(0, type.length()-2);
 			return impliedType(base) + "[]";
 		}
 		if("int".equals(type)) {
 			return "int";
 		}
 		if("int".equalsIgnoreCase(type)) {
 			return "Integer";
 		}
 		if("integer".equalsIgnoreCase(type)) {
 			return "Integer";
 		}
 		if("bool".equalsIgnoreCase(type) || "boolean".equals(type)) {
 			return "boolean";
 		}
 		if("char".equals(type)) {
 			return "char";
 		}
 		if("char".equalsIgnoreCase(type)) {
 			return "Character";
 		}
 		if("byte".equals(type)) {
 			return "byte";
 		}
 		if("long".equals(type)) {
 			return "long";
 		}
 		if("short".equals(type)) {
 			return "short";
 		}
 		if("float".equals(type)) {
 			return "float";
 		}
 		if("double".equals(type)) {
 			return "double";
 		}
 		
 		if("one".equalsIgnoreCase(type)) {
 			return "one";
 		}
 		if("many".equalsIgnoreCase(type)) {
 			return "many";
 		}
 
 		return camelCase(type);
 	}
 	
 	public static String j(Object obj) {
 		return jsonEscape(obj);
 	}
 	
 	public static String join(Character separator, Object...segments) {
 		return ((segments.length > 0) ? join(segments, separator) : "");
 	}
 	
 	// TODO all join methods should calculate the length to avoid array resizing
 	public static String join(CharSequence sequence, char separator) {
 		StringBuilder sb = new StringBuilder();
 		for(int i = 0; i < sequence.length(); i++) {
 			sb.append(sequence.charAt(i)).append(separator);
 		}
 		if(sb.length() > 0) {
 			return sb.substring(0, sb.length()-1);
 		} else {
 			return "";
 		}
 	}
 
 	public static String join(int[] segments, char separator) {
 		return join(segments, String.valueOf(separator));
 	}
 	
 	public static String join(int[] segments, String separator) {
 		StringBuilder sb = new StringBuilder();
 		for(Object segment : segments) {
 			if(!blank(segment)) {
 				sb.append(segment).append(separator);
 			}
 		}
 		if(sb.length() > 0) {
 			return sb.substring(0, sb.length()-separator.length());
 		} else {
 			return "";
 		}
 	}
 	
 	public static String join(Iterable<? extends Object> segments, char separator) {
 		StringBuilder sb = new StringBuilder();
 		for(Object segment : segments) {
 			if(!blank(segment)) {
 				sb.append(segment).append(separator);
 			}
 		}
 		if(sb.length() > 0) {
 			return sb.substring(0, sb.length()-1);
 		} else {
 			return "";
 		}
 	}
 	
 	public static String join(Iterable<? extends Object> segments, String separator) {
 		StringBuilder sb = new StringBuilder();
 		for(Object segment : segments) {
 			if(!blank(segment)) {
 				sb.append(segment).append(separator);
 			}
 		}
 		if(sb.length() > 0) {
 			return sb.substring(0, sb.length()-separator.length());
 		} else {
 			return "";
 		}
 	}
 
 	public static String join(Object[] segments, char separator) {
 		String[] sa = new String[segments.length];
 		for(int i = 0; i < sa.length; i++) {
 			if(!blank(segments[i])) {
 				sa[i] = String.valueOf(segments[i]);
 			}
 		}
 		return join(sa, separator);
 	}
 	
 	public static String join(Object[] segments, String separator) {
 		StringBuilder sb = new StringBuilder();
 		for(Object segment : segments) {
 			if(!blank(segment)) {
 				sb.append(segment).append(separator);
 			}
 		}
 		if(sb.length() > 0) {
 			return sb.substring(0, sb.length()-separator.length());
 		} else {
 			return "";
 		}
 	}
 	
 	public static String join(String starter, int[] segments, String closer, String separator) {
 		return ((segments.length > 0) ? (starter + join(segments, separator) + closer) : "");
 	}
 	
 	public static String join(String starter, Iterable<? extends Object> segments, String closer, String separator) {
 		Iterator<? extends Object> iter = segments.iterator();
 		if(iter.hasNext()) {
 			StringBuilder sb = new StringBuilder();
 			for(Object segment : segments) {
 				if(!blank(segment)) {
 					sb.append(segment).append(separator);
 				}
 			}
 			if(sb.length() > 0) {
 				sb.insert(0, starter);
 				sb.replace(sb.length()-separator.length(), sb.length(), closer);
 				return sb.toString();
 			}
 		}
 		return "";
 	}
 	
 	public static String join(String separator, Object...segments) {
 		return ((segments.length > 0) ? join(segments, separator) : "");
 	}
 
 	public static String join(String starter, Object[] segments, String closer, String separator) {
 		return ((segments.length > 0) ? (starter + join(segments, ", ") + closer) : "");
 	}
 
 	public static String join(String[] segments, char separator) {
 		return join(segments, separator, false);
 	}
 	
 	public static String join(String[] segments, char separator, boolean includeBlank) {
 		int len = segments.length;
 		for(String segment : segments) {
 			if(includeBlank || !blank(segment)) {
 				len += segment.length();
 			}
 		}
 		StringBuilder sb = new StringBuilder(len);
 		for(String segment : segments) {
 			if(includeBlank || !blank(segment)) {
 				sb.append(segment).append(separator);
 			}
 		}
 		sb.deleteCharAt(sb.length()-1);
 		return sb.toString();
 	}
 
 	public static String joinColumn(String table1, String column1, String table2, String column2) {
 		String name1 = columnName(table1, column1);
 		String name2 = columnName(table2, column2);
 		if(name1.compareTo(name2) <= 0) {
 			return "b";
 		} else {
 			return "a";
 		}
 	}
 
 	public static String[] joinColumns(String table1, String column1, String table2, String column2) {
 		String name1 = columnName(table1, column1);
 		String name2 = columnName(table2, column2);
 		if(name1.compareTo(name2) <= 0) {
 			return new String[] { "a", "b" };
 		} else {
 			return new String[] { "b", "a" };
 		}
 	}
 
 	public static String joinTable(String table1, String column1, String table2, String column2) {
 		String name1 = columnName(table1, column1);
 		String name2 = columnName(table2, column2);
 		if(name1.compareTo(name2) <= 0) {
 			return tableName(name1, name2);
 		} else {
 			return tableName(name2, name1);
 		}
 	}
 	
 	public static String jsonEscape(Object obj) {
 		if(obj == null) {
 			return "";
 		} else {
 			return obj.toString().replaceAll("&", "\u0026").replaceAll(">", "\u003E").replaceAll("<", "\u003C");
 		}
 	}
 	
 	public static Map<String, Object> lowerKeys(Map<?, ?> map) {
 		Map<String, Object> lkmap;
 		if(map instanceof TreeMap<?, ?>) {
 			lkmap = new TreeMap<String, Object>();
 		} else if(map instanceof LinkedHashMap<?, ?>) {
 			lkmap = new LinkedHashMap<String, Object>();
 		} else {
 			lkmap = new HashMap<String, Object>();
 		}
 		for(Entry<?, ?> entry : map.entrySet()) {
 			String key = String.valueOf(entry.getKey()).toLowerCase();
 			Object value = entry.getValue();
 			if(value instanceof Map<?,?>) {
 				value = lowerKeys((Map<?,?>) value);
 			}
 			lkmap.put(key, value);
 		}
 		return lkmap;
 	}
 
 	public static String mask(Object value, String mask) {
 		return mask(value, mask, '0');
 	}
 	
 	public static String mask(Object value, String mask, char placeholder) {
 		if(value == null || mask == null) {
 			return "";
 		}
 		char[] ca = value.toString().toCharArray();
 		StringBuilder sb = new StringBuilder();
 		for(int i = mask.length()-1, j = ca.length-1; i >= 0; i--) {
 			char c = mask.charAt(i);
 			if(c == '#') {
 				if(j >= 0) {
 					sb.append(ca[j--]);
 				} else {
 					sb.append(placeholder);
 				}
 			} else {
 				sb.append(c);
 			}
 		}
 		return sb.reverse().toString();
 	}
 	
 	public static String methodName(String input) {
 		char[] ca = input.toCharArray();
 		StringBuilder sb = new StringBuilder(ca.length + 5);
 		for(int i = 0; i < ca.length; i++) {
 			if(sb.length() == 0) {
 				if(Character.isJavaIdentifierStart(ca[i])) sb.append(ca[i]);
 				else sb.append('_');
 			} else {
 				if(Character.isJavaIdentifierPart(ca[i])) sb.append(ca[i]);
 				else sb.append('_');
 			}
 		}
 		if(ca[0] == '.') sb.append("_class");
 		return varName(sb.toString());
 	}
 	
 	public static String modelName(String tableName) {
 		return camelCase(singular(tableName));
 	}
 	
 	public static String modelsName(Class<?> clazz) {
 		return plural(clazz.getSimpleName());
 	}
 
 	public static String n(Object obj) {
 		return nullEscape(obj);
 	}
 
 	public static String nullEscape(Object obj) {
 		return (obj == null) ? "" : obj.toString();
 	}
 	
 	public static String optionTag(String text, Object value, Object selection) {
 		StringBuilder sb = new StringBuilder();
 		optionTag(sb, text, value, selection);
 		return sb.toString();
 	}
 	
 	private static void optionTag(StringBuilder sb, Object text, Object value, Object selection) {
 		boolean selected = isEqual(value, selection);
 		sb.append("<option title=\"").append(h(text)).append("\" value=\"").append(f(value)).append('"');
 		if(selected) {
 			sb.append(" selected");
 		}
 		sb.append('>').append(h(text)).append("</option>");
 	}
 	
 	public static String optionTags(Object options) {
 		return optionTags(options, null, false);
 	}
 	
 	public static String optionTags(Object options, boolean required) {
 		return optionTags(options, null, required);
 	}
 	
 	public static String optionTags(Object options, Object selection) {
 		return optionTags(options, selection, false);
 	}
 	
 	public static String optionTags(Object options, Object selection, boolean required) {
 		if(options == null) {
 			return "";
 		}
 		if(options instanceof String) {
 			options = toObject((String) options);
 		}
 		if(options instanceof Iterable<?>) {
 			StringBuilder sb = new StringBuilder();
 			if(!required) {
 				optionTag(sb, "select", "", selection);
 			}
 			for(Object option : (Iterable<?>) options) {
 				if(option instanceof Iterable<?>) {
 					Iterator<?> iter = ((Iterable<?>) option).iterator();
 					optionTag(sb, iter.next(), iter.next(), selection);
 				} else if(option instanceof Map<?, ?>) {
 					Map<?,?> map = (Map<?,?>) option;
 					optionTag(sb, map.get("text"), map.get("value"), selection);
 				} else if(option != null && option.getClass().isArray()) {
 					Object o1 = Array.get(option, 0);
 					Object o2 = Array.get(option, 1);
 					optionTag(sb, o1, o2, selection);
 				} else {
 					optionTag(sb, option, option, selection);
 				}
 			}
 			return sb.toString();
 		}
 		if(options.getClass().isArray()) {
 			if(options.getClass().getComponentType().isArray()) {
 				StringBuilder sb = new StringBuilder();
 				int len = Array.getLength(options);
 				for(int i = 0; i < len; i++) {
 					Object option = Array.get(options, i);
 					Object o1 = Array.get(option, 0);
 					Object o2 = Array.get(option, 1);
 					optionTag(sb, o1, o2, selection);
 				}
 				return sb.toString();
 			} else {
 				StringBuilder sb = new StringBuilder();
 				int len = Array.getLength(options);
 				for(int i = 0; i < len; i++) {
 					Object option = Array.get(options, i);
 					optionTag(sb, option, option, selection);
 				}
 				return sb.toString();
 			}
 		}
 		return "";
 	}
 	
 	public static String optionTags(Object[] options, Object selection) {
 		StringBuilder sb = new StringBuilder();
 		optionTags(sb, options, selection);
 		return sb.toString();
 	}
 
 	private static void optionTags(StringBuilder sb, Object[] options, Object selection) {
 		for(Object option : options) {
 			String text = String.valueOf(option);
 			String value = underscored(text);
 			boolean selected = isEqual(value, selection);
 			sb.append("<option value=\"").append(h(value)).append('"').append(' ');
 			if(selected) {
 				sb.append("selected ");
 			}
 			sb.append('>').append(h(text)).append("</option>");
 		}
 	}
 	
 	public static String packageName(String type) {
 		int ix = type.lastIndexOf('.');
 		if(ix == -1) {
 			return null; // there is no package (default)
 		}
 		return type.substring(0, ix);
 	}
 	
 	/**
 	 * convert to the given string to one that is appropriate for use as a Java
 	 * package segment: MyModel -> my_model MY_MODEL -> my_model my_model ->
 	 * my_model
 	 * 
 	 * @param string
 	 * @return
 	 */
 	public static String packageSegment(String string) {
 		return underscored(string).toLowerCase();
 	}
 	
 	public static String pad(String string, int length) {
 		return pad(string, length, ' ');
 	}
 	
 	public static String pad(String string, int length, char placeholder) {
 		if(string == null || length <= 0) {
 			return "";
 		}
 		if(length < string.length()) {
 			return string.substring(0, length);
 		}
 		StringBuilder sb = new StringBuilder(length);
 		sb.append(string);
 		while(sb.length() < length) {
 			sb.append(placeholder);
 		}
 		return sb.toString();
 	}
 	
 	public static Map<String, Object> parseUrl(String url) {
 		Map<String, Object> properties = new HashMap<String, Object>();
 		int ix = url.indexOf('@');
 		if(ix == -1) {
 			properties.put("username", "root");
 			properties.put("password", "");
 		} else {
 			String credentials = url.substring(0, ix);
 			url = url.substring(ix+1);
 			ix = credentials.indexOf(':');
 			if(ix == -1) {
 				properties.put("username", credentials);
 				properties.put("password", "");
 			} else {
 				properties.put("username", credentials.substring(0, ix));
 				properties.put("password", credentials.substring(ix+1));
 			}
 		}
 
 		ix = url.indexOf('/');
 		if(ix == -1) {
 			properties.put("host", null); // use default
 			properties.put("port", null); // use default
 			properties.put("database", url);
 		} else if(ix == 0) {
 			properties.put("host", null); // use default
 			properties.put("port", null); // use default
 			properties.put("database", url.substring(1));
 		} else {
 			String s = url.substring(0, ix);
 			properties.put("database", url.substring(ix+1));
 			ix = s.indexOf(':');
 			if(ix == -1) {
 				properties.put("host", s);
 				properties.put("port", null); // use default
 			} else {
 				properties.put("host", s.substring(0, ix));
 				properties.put("port", Integer.parseInt(s.substring(ix+1)));
 			}
 		}
 		return properties;
 	}
 
 	public static String path(String...segments) {
 		return join(segments, '/');
 	}
 
 	/**
 	 * (###) ###-####
 	 * @param str
 	 * @return
 	 */
 	public static String phone(Object obj) {
 		if(blank(obj)) {
 			return "";
 		} else {
 			String str = obj.toString();
 			StringBuilder sb = new StringBuilder(str.replaceAll("\\D", ""));
 			if(sb.length() == 10) {
 				sb.insert(0, '(').insert(4, ')').insert(5, ' ').insert(9, '-');
 			}
 			return sb.toString();
 		}
 	}
 
 	public static String plural(String singular) {
 		if(singular == null || singular.length() == 0) {
 			return singular;
 		}
 		if(singular.equalsIgnoreCase("person")) {
 			return singular.charAt(0) + "eople";
 		}
 		else if(singular.equalsIgnoreCase("child")) {
 			return singular.charAt(0) + "hildren";
 		}
 		else if(singular.equalsIgnoreCase("alumnus")) {
 			return singular.charAt(0) + "lumni";
 		}
 		else if('y' == singular.charAt(singular.length()-1)) {
 			if(singular.length() > 1) {
 				switch(singular.charAt(singular.length()-2)) {
 				case 'a':
 				case 'e':
 				case 'i':
 				case 'o':
 				case 'u':
 					break;
 				default:
 					return singular.substring(0, singular.length()-1) + "ies";
 				}
 			}
 		}
 		else if('s' == singular.charAt(singular.length()-1)){
 			return singular + "es";
 		}
 		else if(singular.endsWith("ch")){
 			return singular + "es";
 		}
 		return singular + "s";
 	}
 	
 	/**
 	 * Pluralize the singular word, unless count == 1, and concatenate it to the count.
 	 * <dl>
 	 * <dt>For example:</dt>
 	 * <dd>pluralize(1, "person") -> "1 person"</dd>
 	 * <dd>pluralize(2, "person") -> "2 people"</dd>
 	 * </dl>
 	 * @return a String with the result of pluralization; never null.
 	 */
 	public static String pluralize(int count, String singular) {
 		return count + " " + pluralize(singular, count);
 	}
 	
 	/**
 	 * Pluralize the singular word, unless count == 1, and concatenate it to the count.
 	 * Uses the provided plural word rather than calling plural(singular).
 	 * <dl>
 	 * <dt>For example:</dt>
 	 * <dd>pluralize(1, "person", "users") -> "1 person"</dd>
 	 * <dd>pluralize(2, "person", "users") -> "2 users"</dd>
 	 * </dl>
 	 * @return a String with the result of pluralization; never null.
 	 */
 	public static String pluralize(int count, String singular, String plural) {
 		return count + " " + pluralize(singular, plural, count);
 	}
 	
 	/**
 	 * Pluralize the singular word, unless count == 1, and return it.
 	 * The result does not include the count in case you want to do something different.
 	 * <dl>
 	 * <dt>For example:</dt>
 	 * <dd>pluralize("person", 1) -> "person"</dd>
 	 * <dd>pluralize("person", 2) -> "people"</dd>
 	 * </dl>
 	 * @return a String with the result of pluralization; never null.
 	 */
 	public static String pluralize(String singular, int count) {
 		return (count == 1) ? singular : plural(singular);
 	}
 	
 	/**
 	 * Pluralize the singular word, unless count == 1, and return it.
 	 * Uses the provided plural word rather than calling plural(singular).
 	 * The result does not include the count in case you want to do something different.
 	 * <dl>
 	 * <dt>For example:</dt>
 	 * <dd>pluralize("person", "users", 1) -> "person"</dd>
 	 * <dd>pluralize("person", "users", 2) -> "users"</dd>
 	 * </dl>
 	 * @return a String with the result of pluralization; never null.
 	 */
 	public static String pluralize(String singular, String plural, int count) {
 		return (count == 1) ? singular : plural;
 	}
 	
 	public static int[] range(int start, int end) {
 		return range(start, end, false);
 	}
 	
 	public static int[] range(int start, int end, boolean exclusive) {
 		int[] range = exclusive ? new int[end-start] : new int[end-start+1];
 		for(int i = 0; i < range.length; i++) {
 			range[i] = i + start;
 		}
 		return range;
 	}
 	
 	public static String repeat(char c, int times) {
 		StringBuilder sb = new StringBuilder();
 		for(int i = 0; i < times; i++) {
 			sb.append(c);
 		}
 		return sb.toString();
 	}
 	
 	public static String replace(String string, Object[] values) {
 		if(values == null || values.length == 0) {
 			return string;
 		}
 		int len = string.length();
 		String[] sa = new String[values.length];
 		for(int i = 0; i < sa.length; i++) {
 			sa[i] = String.valueOf(values[i]);
 			len += sa[i].length();
 		}
 		StringBuilder sb = new StringBuilder(len);
 		int i = 0;
 		int pos = 0;
 		int ix = string.indexOf("{}");
 		while(ix != -1) {
 			if(ix > 0) {
 				if(string.charAt(ix-1) == '\\') {
 					if(ix > 1 && string.charAt(ix-2) == '\\') {
 						// escape is escaped :)
 						sb.append(string.substring(pos, ix-1)); // skip one of the escape characters
 						sb.append(sa[i++]); // print the anchor
 					} else {
 						// anchor is escaped
 						sb.append(string.substring(pos, ix-1)); // skip the escape character
 						sb.append("{}"); // print the escaped braces
 					}
 				} else {
 					sb.append(string.substring(pos, ix));
 					sb.append(sa[i++]);
 				}
 			} else {
 				sb.append(sa[i++]);
 			}
 			pos = ix + 2;
 			ix = string.indexOf("{}", pos);
 		}
 		if(pos < string.length()) {
 			sb.append(string.substring(pos));
 		}
 		return sb.toString();
 	}
 
 	public static String separated(String string, char separator) {
 		if(string == null || string.length() == 0) {
 			return "null";
 		}
 		
 		String s = string.trim().replaceAll("[\\s]+", Character.toString(separator));
 		StringBuilder sb = new StringBuilder();
 		char[] ca = s.toCharArray();
 		for(int i = 0; i < ca.length; i++) {
 			if(Character.isUpperCase(ca[i])) {
 				if(i != 0 && Character.isLetterOrDigit(ca[i-1])) {
 					if(Character.isUpperCase(ca[i-1])) {
 						if(i < ca.length-1 && Character.isLetter(ca[i+1]) && !Character.isUpperCase(ca[i+1])) {
 							sb.append(separator);
 						}
 					} else {
 						sb.append(separator);
 					}
 				}
 				sb.append(Character.toLowerCase(ca[i]));
 			} else {
 				sb.append(ca[i]);
 			}
 		}
 		return sb.toString();
 	}
 
 	public static String setterName(String variable) {
 		return "set" + camelCase(variable);
 	}
 
 	public static String simpleName(String type) {
 		int ix = type.lastIndexOf('.');
 		if(ix == -1) {
 			return type;
 		}
 		return type.substring(ix+1);
 	}
 	
 	public static String singular(String plural) {
 		if(plural.equalsIgnoreCase("people")) {
 			return plural.charAt(0) + "erson";
 		} else if(plural.equalsIgnoreCase("alumni")) {
 			return plural.charAt(0) + "lumnus";
 		} else if(plural.endsWith("ies")) {
 			return plural.substring(0, plural.length()-3) + 'y';
 		} else if('s' == plural.charAt(plural.length()-1)){
 			return plural.substring(0, plural.length() - 1);
 		}
 		return plural;
 	}
 	
 	public static String source(Collection<String> lines) {
 		if(lines == null || lines.isEmpty()) {
 			return "";
 		}
 		return source(lines.toArray(new String[lines.size()]));
 	}
 
 	public static String source(String indent, char indentChar, Collection<String> lines) {
 		if(lines == null || lines.isEmpty()) {
 			return "";
 		}
 		return source(indent, indentChar, lines.toArray(new String[lines.size()]));
 	}
 
 	public static String source(String...lines) {
 		return source("", '\t', lines);
 	}
 	
 	public static String source(char indentChar, String...lines) {
 		return source("", indentChar, lines);
 	}
 	
 	public static String source(char indent, char indentChar, String...lines) {
 		return source(String.valueOf(indent), indentChar, lines);
 	}
 	
 	public static String source(String indent, char indentChar, String...lines) {
 		String s = join(lines, '\n', true);
 		StringBuilder sb = new StringBuilder(s.length() + (indent.length() * lines.length));
 		sb.append(indent);
 		for(int i = 0; i < s.length(); i++) {
 			char c = s.charAt(i);
 			sb.append(c);
 			if(c == '\n' && i+1 < s.length() && s.charAt(i+1) != '\n') {
 				i++;
 				sb.append(indent);
 				while(i < s.length()) {
 					c = s.charAt(i);
 					if(Character.isWhitespace(c)) {
 						sb.append(indentChar);
 						i++;
 					} else {
 						sb.append(c);
 						break;
 					}
 				}
 			}
 		}
 		return sb.toString();
 	}
 	
 	public static String tableName(Class<?> clazz) {
 		return tableName(clazz.getSimpleName());
 	}
 	
 	public static String tableName(Class<?> model1, String relation1, Class<?> model2, String relation2) {
 		return tableName(tableName(model1), relation1, tableName(model2), relation2);
 	}
 	
 	public static String tableName(Object model) {
 		return tableName(model.getClass().getSimpleName());
 	}
 
 	public static String tableName(String singular) {
 		return underscored(plural(singular));
 	}
 	
 	public static String tableName(String column1, String column2) {
 		if(column1.compareTo(column2) <= 0) {
 			return column1 + "___" + column2;
 		} else {
 			return column2 + "___" + column1;
 		}
 	}
 
 	public static String tableName(String table1, String column1, String table2, String column2) {
 		String c1 = table1 + "__" + column1;
 		String c2 = table2 + "__" + column2;
 		return tableName(c1, c2);
 	}
 
 //	public static String[] tableNames(Object model) {
 //		return classTableNames(model.getClass());
 //	}
 
 	public static String[] tableNames(Class<?>[] classes) {
 		String[] tables = new String[classes.length];
 		for(int i = 0; i < classes.length; i++) {
 			tables[i] = tableName(classes[i]);
 		}
 		return tables;
 	}
 
 	public static String[] tableNames(String table1, String table2) {
 		if(table1.compareTo(table2) <= 0) {
 			return new String[] { table1, table2 };
 		} else {
 			return new String[] { table2, table1 };
 		}
 	}
 	
 	public static String titleize(String string) {
 		if(string == null) {
 			return "Null";
 		}
 		String[] sa = string.split("[_\\s]+");
 		StringBuilder sb = new StringBuilder();
 		for(int i = 0; i < sa.length; i++) {
 			if(i != 0) {
 				sb.append(' ');
 			}
 			boolean isUpper = false;
 			boolean wasUpper = false;
 			char[] ca = sa[i].toCharArray();
 			for(int j = 0; j < ca.length; j++) {
 				isUpper = Character.isUpperCase(ca[j]);
 				if(j == 0) {
 					sb.append(Character.toUpperCase(ca[j]));
 				} else {
 					if(isUpper && !wasUpper) {
 						sb.append(' ');
 					}
 					if(wasUpper) {
 						sb.append(Character.toLowerCase(ca[j]));
 					} else {
 						sb.append(ca[j]);
 					}
 				}
 				wasUpper = isUpper;
 			}
 		}
 		return sb.toString();
 	}
 	
 	/**
 	 * Convert from CamelCase to underscored: MyModel -> my_model.
 	 * null values are returned as a String -> "null".
 	 * All returned characters are lower case.
 	 * @param string
 	 * @return
 	 */
 	public static String underscored(String string) {
 		return separated(string, '_');
 	}
 	
 	public static String varName(Class<?> clazz) {
 		return varName(clazz.getSimpleName());
 	}
 
 	public static String varName(Class<?> clazz, boolean plural) {
 		return varName(clazz.getSimpleName(), plural);
 	}
 
 	/**
 	 * convert to the given string to one that is appropriate for use as a Java
 	 * variable: MyModel -> myModel MY_MODEL -> myModel my_model -> myModel
 	 * 
 	 * @param column
 	 * @return
 	 */
 	public static String varName(String column) {
 		if(column != null && column.length() > 0) {
 			if(column.length() == 1) {
 				return column.toUpperCase();
 			} else {
 				String var = camelCase(column);
 				return Character.toLowerCase(var.charAt(0)) + var.substring(1);
 			}
 		}
 		return column;
 	}
 
 	public static String varName(String string, boolean plural) {
 		String var = camelCase(string);
 		var = Character.toLowerCase(var.charAt(0)) + var.substring(1);
 		return plural ? plural(var) : var;
 	}
 
 	public static String viewName(Object view) {
 		String name = view.getClass().getSimpleName();
 		if(name.endsWith("View")) {
 			return name.substring(0, name.length()-4);
 		} else {
 			return name;
 		}
 	}
 	
 	public static String viewsPackageName(String model) {
 		String packageName = packageName(model);
 		int ix = packageName.lastIndexOf('.');
 		if(ix == -1) {
 			return "views." + tableName(simpleName(model));
 		} else {
 			return packageName = packageName.substring(0, ix) + ".views." + tableName(simpleName(model));
 		}
 	}
 
 	public static int when(String test, String...is) {
 		if(test == null) {
 			for(int i = 0; i < is.length; i++) {
 				if(is[i] == null) return i;
 			}
 		} else {
 			for(int i = 0; i < is.length; i++) {
 				if(test.equals(is[i])) return i;
 			}
 		}
 		return -1;
 	}
 	
 	public static void yearTags(StringBuilder sb, String pattern, int relmin, int relmax) {
 		Calendar cal = GregorianCalendar.getInstance();
 		int min = cal.get(Calendar.YEAR) - relmin;
 		int max = cal.get(Calendar.YEAR) + relmax;
 		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
 		for(int i = min; i <= max; i++) {
 			cal.set(Calendar.YEAR, i);
 			optionTag(sb, sdf.format(cal.getTime()), i, null);
 		}
 	}
 
 }
