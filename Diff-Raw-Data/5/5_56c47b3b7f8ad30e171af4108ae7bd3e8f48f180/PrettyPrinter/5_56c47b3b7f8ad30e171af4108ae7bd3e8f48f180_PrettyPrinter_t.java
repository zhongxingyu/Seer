 package org.flowdev.base.data;
 
 import java.lang.reflect.Field;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Map;
 
 /**
  * Utility class for printing data and config objects. This is useful for
  * 'toString()' methods, debugging, ...
  */
 public final class PrettyPrinter {
     public final static String INDENT = "    ";
     public final static String NL = System.lineSeparator();
     public final static String NULL = "NULL";
 
     public static class Entry implements Comparable<Entry> {
 	public String name;
 	public Object value;
 
 	public Entry(String nam, Object val) {
 	    name = (nam == null) ? NULL : nam;
 	    value = val;
 	}
 
 	public int compareTo(Entry e) {
 	    return name.compareTo(e.name);
 	}
     }
 
     /** Prevent instantiation. */
     private PrettyPrinter() {
     }
 
     public static String prettyPrint(Object obj) {
 	return prettyPrintObject("", new StringBuilder(4096), obj).append(NL)
 		.toString();
     }
 
     public static StringBuilder prettyPrintObject(String indentation,
 	    StringBuilder buf, Object obj) {
 	buf.append(obj.getClass().getSimpleName()).append(" {").append(NL);
 
 	prettyPrintEntries(indentation + INDENT, buf,
 		fieldsToEntries(obj.getClass().getFields(), obj), " = ");
 
 	buf.append(indentation).append("}");
 	return buf;
     }
 
     private static Entry[] fieldsToEntries(Field[] fields, Object obj) {
 	Entry[] entries = new Entry[fields.length];
 
 	for (int i = 0; i < fields.length; i++) {
 	    Field field = fields[i];
 	    entries[i] = new Entry(field.getName(), getValueOfField(field, obj));
 	}
 
 	return entries;
     }
 
     private static Object getValueOfField(Field field, Object obj) {
 	try {
 	    return field.get(obj);
 	} catch (IllegalAccessException e) {
 	    throw new RuntimeException(e);
 	}
     }
 
     public static StringBuilder prettyPrintEntries(String indentation,
 	    StringBuilder buf, Entry[] entries, String relation) {
 
 	Arrays.sort(entries);
 
 	for (Entry entry : entries) {
 	    prettyPrintEntry(indentation, buf, entry.name, " : ", entry.value);
 	}
 
 	return buf;
     }
 
     public static StringBuilder prettyPrintEntry(String indentation,
 	    StringBuilder buf, String name, String relation, Object value) {
 
 	String type = (value == null) ? "" : value.getClass().getName();
 
 	buf.append(indentation).append(name).append(relation);
 
 	if (value == null) {
 	    buf.append(NULL);
 	} else if (value.getClass().isEnum() || value.getClass().isPrimitive()) {
 	    buf.append(value.toString());
 	} else if (value instanceof Map) {
 	    prettyPrintMap(indentation, buf, (Map<?, ?>) value);
 	} else if (value instanceof List) {
 	    prettyPrintList(indentation, buf, (List<?>) value);
 	} else if (value.getClass().isArray()) {
 	    buf.append("WARNING: Arrays are not supported: ").append(
 		    value.toString());
 	} else {
 	    switch (type) {
 	    case "java.lang.Boolean":
 	    case "java.lang.Byte":
 	    case "java.lang.Character":
 	    case "java.lang.Short":
 	    case "java.lang.Integer":
 	    case "java.lang.Long":
 	    case "java.lang.Float":
 	    case "java.lang.Double":
		buf.append(value.toString());
 		break;
 	    case "java.lang.String":
		buf.append('"').append(value.toString()).append('"');
 		break;
 	    default:
 		prettyPrintObject(indentation, buf, value);
 		break;
 	    }
 	}
 
 	buf.append(" ;").append(NL);
 	return buf;
     }
 
     public static StringBuilder prettyPrintMap(String indentation,
 	    StringBuilder buf, Map<?, ?> map) {
 	buf.append("Map {").append(NL);
 
 	prettyPrintEntries(indentation + INDENT, buf, mapToEntries(map), " : ");
 
 	buf.append(indentation).append("}");
 	return buf;
     }
 
     private static Entry[] mapToEntries(Map<?, ?> map) {
 	Entry[] entries = new Entry[map.size()];
 	int i = 0;
 
 	for (Object key : map.keySet()) {
 	    entries[i] = new Entry(escapeString(key.toString()), map.get(key));
 	    i++;
 	}
 
 	return entries;
     }
 
     public static String escapeString(String s) {
 	return "\""
 		+ s.replace("\\", "\\\\").replace("\t", "\\t")
 			.replace("\n", "\\n").replace("\r", "\\r") + "\"";
     }
 
     public static StringBuilder prettyPrintList(String indentation,
 	    StringBuilder buf, List<?> list) {
 	String innerIndentation = indentation + INDENT;
 	int N = list.size();
 
 	buf.append("List [").append(NL);
 
 	for (int i = 0; i < N; i++) {
 	    prettyPrintEntry(innerIndentation, buf, "" + i, " : ", list.get(i));
 	}
 
 	buf.append(indentation).append("]");
 	return buf;
     }
 }
