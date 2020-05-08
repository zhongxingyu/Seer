 /*******************************************************************************
  * Copyright (c) 2012 Bruno Ranschaert
  * Released under the MIT License: http://opensource.org/licenses/MIT
  * Library "jsonutil"
  ******************************************************************************/
 package com.sdicons.json;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.StreamTokenizer;
 import java.io.StringReader;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.TreeMap;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Utility functions to parse/render JSON text to/from nested maps and lists.
  * There are also a number of methods to access the nested maps with path
  * expressions of the form "mymap.element.arr[5][3].nested" of arbitrary length
  * and complexity. These paths can be used to construct your map before
  * rendering it to JSON or to parse JSON text and fetch the values using the
  * path expressions. The JSON input can contain single quoted or double quoted
  * strings, single quoted strings can be useful when embedding JSON strings
  * inside Java applications to prevent backslash garbage.
  */
 public final class JsonUtil {
 
     // Error messages.
     //
     private static final String JSON001 = "JSON001: Unexpected content encountered.\nContext: %s X <--ERROR";
     private static final String JSON002 = "JSON002: Input error during parsing.\nContext: %s X <--ERROR";
     private static final String JSON003 = "JSON003: Expected symbol '%s' but received token/symbol '%s'.\nContext: %s X <--ERROR";
    private static final String JSON004 = "JSON004: The key of a JSON object should be a String.\nContext: %s X <--ERROR";
 
     // Prevent the utility class from being instantiated.
     //
     private JsonUtil() {
     }
 
     /**
      * Convert a nested Map data structure to a JSON String. Nested maps, lists
      * and key/values are supported. We will not try to convert
      * beans/properties, it will not work on ordinary beans. General instances
      * will be converted to quoted String values.
      * 
      * @param obj
      *            The nested structure of maps.
      * 
      * @return The JSON representation.
      */
     public static String convertToJson(Object obj) {
         return convertToJson(obj, false);
     }
 
     /**
      * Convert a nested Map data structure to a JSON String. Nested maps, lists
      * and key/values are supported. We will not try to convert
      * beans/properties, it will not work on ordinary beans. General instances
      * will be converted to quoted String values.
      * 
      * @param obj
      *            The nested structure of maps.
      * @param pretty
      *            Pretty print flag.
      * 
      * @return The JSON representation.
      */
     public static String convertToJson(Object obj, boolean pretty) {
         return convertToJson(obj, pretty, "");
     }
 
     private static final String EOF = "EOF";
     private static final String INDENT = "  ";
     private static final String NULL_LITERAL = "null";
 
     // Render a map to a JSON string.
     //
     private static String mapToJson(Map<String, Object> map, boolean pretty, String indent) {
         StringBuilder builder = new StringBuilder(1024);
         builder.append(pretty ? indent + "{\n" : "{");
         int count = 0;
         for (Entry<String, Object> entry : map.entrySet()) {
             //
             final String key = entry.getKey();
             final Object val = entry.getValue();
             //
             if (count > 0) {
                 builder.append(pretty ? ",\n" : ",");
             }
             count++;
             builder.append(pretty ? indent + INDENT : "");
             builder.append("\"");
             builder.append(key);
             builder.append("\"");
             builder.append(":");
 
             String value = convertToJson(val, pretty, indent + INDENT + INDENT);
             if (pretty) {
                 // Remove indents for simple values ...
                 String trimmedValue = value.trim();
                 if (!trimmedValue.startsWith("{") && !trimmedValue.startsWith("[")) {
                     value = trimmedValue;
                 } else {
                     builder.append("\n");
                 }
             }
             builder.append(value);
         }
         builder.append(pretty ? "\n" + indent + "}" : "}");
         return builder.toString();
     }
 
     // Render a collection to a JSON string.
     //
     private static String collToJson(Collection<?> coll, boolean pretty, String indent) {
         StringBuilder builder = new StringBuilder(1024);
         builder.append(pretty ? indent + "[\n" : "[");
         int count = 0;
         for (Object el : coll) {
             if (count > 0) {
                 builder.append(pretty ? ",\n" : ",");
             }
             count++;
             builder.append(convertToJson(el, pretty, indent + INDENT));
         }
         builder.append(pretty ? "\n" + indent + "]" : "]");
         return builder.toString();
     }
 
     @SuppressWarnings({ "rawtypes", "unchecked" })
     private static String convertToJson(Object obj, boolean pretty, String indent) {
         if (obj == null) {
             // 'null' is a valid JSON literal.
             //
             return NULL_LITERAL;
         } else if (obj instanceof Collection) {
             //
             return collToJson((Collection) obj, pretty, indent);
             //
         } else if (obj instanceof Map) {
             //
             return mapToJson((Map<String, Object>) obj, pretty, indent);
         } else if (obj instanceof Number) {
             // No quotes for numbers.
             //
             return (pretty ? indent : "") + obj.toString();
         } else if (obj instanceof Boolean) {
             // No quotes for booleans.
             return (pretty ? indent : "") + obj.toString();
         } else {
             // Strings should be quoted.
             return (pretty ? indent : "") + "\"" + obj.toString().replace("\"", "'") + "\"";
         }
     }
 
     /**
      * Convert a JSON string into a nested structure of Map instances.
      * 
      * @param data
      *            The JSON string.
      * @return A nested data structure of Map/List instances.
      * 
      */
     public static Object parseJson(String data) {
         StreamTokenizer st = new StreamTokenizer(new StringReader(data));
         StringBuilder parsed = new StringBuilder(data.length());
         return parseJson(st, parsed);
     }
      
     /**
      * Convert a JSON string into a nested structure of Map instances.
      * 
      * @param data
      *            The JSON text stream.
      * @return A nested data structure of Map/List instances.
      * 
      */
     public static Object parseJson(BufferedReader reader) {
         StreamTokenizer st = new StreamTokenizer(reader);
         StringBuilder parsed = new StringBuilder();
         return parseJson(st, parsed);
     }
 
     // The parsing workhorse.
     //
     protected static Object parseJson(StreamTokenizer st, StringBuilder parsed) {
         // This is the top-level of the JSON parser, it decides which kind of
         // JSON expression is next in the input stream. The general strategy
         // is to look at the first characters, make a decision about which
         // expression
         // we expect and call the appropriate expression parser. In order to
         // make sure
         // the individual expression parsers see the whole expression we push
         // back the tokens we used to make the decision.
         // Each JSON expression type should have an entry here.
         try {
             st.nextToken();
             switch (st.ttype) {
             case '{':
                 // The start of a JSON object.
                 //
                 parsed.append("{");
                 return parseJsonObject(st, parsed);
             case '[':
                 // The start of a JSON list.
                 //
                 parsed.append("[");
                 return parseJsonList(st, parsed);
             case StreamTokenizer.TT_NUMBER:
                 // Plain JSON Number.
                 // We must take care of exponential notation as well
                 //
                 double num = st.nval;
                 parsed.append(num);
                 int exp = 0;
                 st.ordinaryChars('\0', ' ');
                 st.wordChars('+', '+');
                 st.nextToken();
                 st.whitespaceChars('\0', ' ');
                 st.ordinaryChars('+', '+');
                 if (st.ttype == StreamTokenizer.TT_WORD && Character.toUpperCase(st.sval.charAt(0)) == 'E') {
                     String sss = st.sval;
                     try {
                         if (sss.charAt(1) == '+')
                             exp = Integer.parseInt(sss.substring(2));
                         else
                             exp = Integer.parseInt(sss.substring(1));
                     }
                     catch (NumberFormatException e) {
                         st.pushBack();
                     }
                 }
                 else if (st.ttype < 0 || st.ttype > ' ') st.pushBack();
                 num =  num*Math.pow(10,exp);
                 // Plain JSON Number.
                 //
                 BigDecimal number = new BigDecimal(num);
                 try {
                     return number.toBigIntegerExact();
                 }
                 catch (ArithmeticException e) {
                     return number;
                 }
             case '"':
                 // JSON String expression.
                 //
                 st.quoteChar('"');
                 parsed.append('"').append(st.sval).append('"');
                 return st.sval;
             case '\'':
                 // JSON String expression.
                 //
                 st.quoteChar('\'');
                 parsed.append('\'').append(st.sval).append('\'');
                 return st.sval;
             default:
                 if ("false".equalsIgnoreCase(st.sval)) {
                     // JSON boolean "false" constant.
                     //
                     parsed.append("false");
                     return Boolean.FALSE;
                 } else if ("true".equalsIgnoreCase(st.sval)) {
                     // JSON boolean "true" constant.
                     //
                     parsed.append("true");
                     return Boolean.TRUE;
                 } else if (NULL_LITERAL.equalsIgnoreCase(st.sval)) {
                     // JSON null.
                     //
                     parsed.append("null");
                     return null;
                 } else {
                     throw new IllegalArgumentException(String.format(JSON001, parsed.toString()));
                 }
             }
         } catch (IOException e) {
             throw new IllegalArgumentException(String.format(JSON002, parsed.toString()), e);
         }
     }
 
     // Parse an object.
     // The first '{' is not in the stream anymore, the top-level parsing routine
     // already read it.
     //
     private static Map<String, Object> parseJsonObject(StreamTokenizer st, StringBuilder parsed) {
         // This is the JSON object parser, it parses expressions of the form: {
         // key : value , ... }.
         //
         try {
             final Map<String, Object> map = new LinkedHashMap<String, Object>();
             st.nextToken();
             while (st.ttype != '}') {
                 // Key.
                 st.pushBack();
                 final Object key = parseJson(st, parsed);
                if(!(key instanceof String)) {
                    throw new IllegalArgumentException(String.format(JSON004, parsed.toString()));
                }
 
                 // Colon.
                 st.nextToken();
                 if ((char) st.ttype != ':') {
                     expectationError(":", st, parsed);
                 }
                 parsed.append(':');
 
                 // Value.
                 final Object value = parseJson(st, parsed);
                 map.put(key.toString(), value);
 
                 // Comma.
                 st.nextToken();
                 if ((char) st.ttype != ',') {
                     if ((char) st.ttype != '}') {
                         expectationError("}", st, parsed);
                     } else {
                         parsed.append("}");
                         break;
                     }
                 } else {
                     parsed.append(",");
                     st.nextToken();
                 }
             }
             return map;
         } catch (IOException e) {
             throw new IllegalArgumentException(String.format(JSON002, parsed.toString()), e);
         }
     }
 
     // Convert the current tokenizer token into a readable string so that
     // we can create readable error messages with it.
     //
     private static String errToken(StreamTokenizer st) {
         switch (st.ttype) {
         case StreamTokenizer.TT_EOF:
             return EOF;
         case StreamTokenizer.TT_WORD:
             return st.sval;
         case StreamTokenizer.TT_NUMBER:
             return "" + st.nval;
         default:
             return Character.toString((char) st.ttype);
         }
     }
 
     // Create an error message, the tokenizer did not contain an expected
     // character.
     //
     private static String expectationError(String expected, StreamTokenizer st, StringBuilder parsed) {
         throw new IllegalArgumentException(String.format(JSON003, expected, errToken(st), parsed.toString()));
     }
 
     // Parse an object.
     // The first '[' is not in the stream anymore, the top-level parsing routine
     // already read it.
     //
     private static List<?> parseJsonList(StreamTokenizer st, StringBuilder parsed) {
         // This is the JSON list parser, it parses expressions of the form: [
         // val-1, val-2, ... val-n ].
         //
         try {
             final List<? super Object> list = new ArrayList<Object>();
             st.nextToken();
             while (st.ttype != ']') {
                 // Element
                 st.pushBack();
                 Object element = parseJson(st, parsed);
 
                 list.add(element);
 
                 // Comma.
                 st.nextToken();
                 if ((char) st.ttype != ',') {
                     if ((char) st.ttype != ']') {
                         expectationError("]", st, parsed);
                     } else {
                         parsed.append("]");
                         break;
                     }
                 } else {
                     parsed.append(",");
                     st.nextToken();
                 }
             }
             return list;
         } catch (IOException e) {
             throw new IllegalArgumentException(String.format(JSON002, parsed.toString()), e);
         }
     }
 
     /**
      * Utility method to fetch a value from a nested Map/List structure. The
      * path should have the form "part1.part2[5].part3". Multidimensional array
      * indices ("part[2][8][13]") are allowed as well.
      * 
      * @param path
      *            The path of the value.
      * @param map
      *            The map to get the value from.
      * @return null or the value.
      */
     public static Object getObjectFromMap(String path, Map<?, ?> map) {
         final PathResolver resolver = compilePath(path);
         return getObjectFromMap(resolver, map);
     }
 
     /**
      * Utility method to fetch a value from a nested Map/List structure. The
      * path should have the form "part1.part2[5].part3". Multidimensional array
      * indices ("part[2][8][13]") are allowed as well.
      * 
      * @param resolver
      *            The compiled path.
      * @param map
      *            The map to get the value from.
      * @return null or the value.
      */
     public static Object getObjectFromMap(PathResolver resolver, Map<?, ?> map) {
         return resolver.get(map);
     }
 
     /**
      * Utility method to fetch a String value from a nested Map structure.
      * 
      * @param path
      *            The path of the value.
      * @param map
      *            The map to get the value from.
      * @return null or the String value.
      */
     public static String getStringFromMap(String path, Map<?, ?> map) {
         final PathResolver resolver = compilePath(path);
         return getStringFromMap(resolver, map);
     }
 
     /**
      * Utility method to fetch a String value from a nested Map structure.
      * 
      * @param path
      *            The compiled path of the value.
      * @param map
      *            The map to get the value from.
      * @return null or the String value.
      */
     public static String getStringFromMap(PathResolver path, Map<?, ?> map) {
         final Object result = getObjectFromMap(path, map);
         if (result == null) {
             return null;
         } else {
             return result.toString();
         }
     }
 
     /**
      * Utility method to fetch an Integer value from a nested Map structure. The
      * path should have the form "part1.part2.part3".
      * 
      * @param path
      *            The path of the value.
      * @param map
      *            The map to get the value from.
      * @return null or the Integer value.
      */
     public static Integer getIntFromMap(String path, Map<?, ?> map) {
         final PathResolver resolver = compilePath(path);
         return getIntFromMap(resolver, map);
     }
 
     /**
      * Utility method to fetch an Integer value from a nested Map structure. The
      * path should have the form "part1.part2.part3".
      * 
      * @param path
      *            The compiled path of the value.
      * @param map
      *            The map to get the value from.
      * @return null or the Integer value.
      */
     public static Integer getIntFromMap(PathResolver path, Map<?, ?> map) {
         // First we attempt to lookup the value, whatever the type.
         //
         final Object result = getObjectFromMap(path, map);
 
         // We will try to convert the value into an Integer.
         //
         if (result == null) {
             // The value was null, we can quickly return.
             return null;
         } 
         else if (result instanceof BigInteger) {
             return ((BigInteger) result).intValue();
             
         } 
         else if (result instanceof Integer) {
             // The value is actually an integer, no conversions needed.
             return (Integer) result;
         } 
         else if (result instanceof String) {
             // We try to parse string values into boolean values.
             // String -> Integer coercion.
             try {
                 return Integer.parseInt((String) result);
             } 
             catch (NumberFormatException e) {
                 return null;
             }
         } 
         else if (result instanceof Double) {
             // We try to interpret Doubles as Integers.
             // Double -> Integer coercion.
             return (int) Math.round((Double) result);
         }
         return null;
     }
 
     /**
      * Utility method to fetch a Boolean value from a nested Map structure. The
      * path should have the form "part1.part2.part3".
      * 
      * @param path
      *            The path of the value.
      * @param map
      *            The map to get the value from.
      * @return null or the Boolean value.
      */
     public static Boolean getBoolFromMap(String path, Map<?, ?> map) {
         final PathResolver resolver = compilePath(path);
         return getBoolFromMap(resolver, map);
     }
 
     /**
      * Utility method to fetch a Boolean value from a nested Map structure. The
      * path should have the form "part1.part2.part3".
      * 
      * @param path
      *            The compiled path of the value.
      * @param map
      *            The map to get the value from.
      * @return null or the Boolean value.
      */
     public static Boolean getBoolFromMap(PathResolver path, Map<?, ?> map) {
         // First we attempt to lookup the value, whatever the type.
         //
         final Object result = getObjectFromMap(path, map);
 
         // We will try to convert the value into a Boolean.
         //
         if (result == null) {
             // The value was null, we can quickly return.
             return null;
         } else if (result instanceof Boolean) {
             // The value is actually a boolean, no conversions needed.
             return (Boolean) result;
         } else if (result instanceof String) {
             // We try to parse string values into boolean values.
             // String -> Boolean coercion.
             // We are very strict about the format of boolean values.
             //
             final String lowerRes = ((String) result).toLowerCase();
             if ("true".equals(lowerRes)) {
                 return true;
             } else if ("false".equals(lowerRes)) {
                 return false;
             } else {
                 return null;
             }
         }
         return null;
     }
 
     /**
      * Utility function to put a value in a nested map structure using a path.
      * All intermediary maps/arrays will be created on the fly if they don't yet
      * exist. The path should have the form "part1.part2[123].part3". Existing
      * values can be overwritten in this way.
      * 
      * @param path
      *            The path of the value.
      * @param map
      *            The map that should be filled in.
      * @param obj
      *            The value that will be put in the nested map structure.
      * @throws IllegalArgumentException
      *             When trying to access a map as a list or vice versa, while
      *             trying to do something against the data structure.
      * 
      */
     public static void putObjectInMap(String path, Map<String, Object> map, Object obj) {
         final PathResolver resolver = compilePath(path);
         putObjectInMap(resolver, map, obj);
     }
 
     /**
      * Utility function to put a value in a nested map structure using a path.
      * All intermediary maps/arrays will be created on the fly if they don't yet
      * exist. The path should have the form "part1.part2[123].part3". Existing
      * values can be overwritten in this way.
      * 
      * @param path
      *            The compiled path of the value.
      * @param map
      *            The map that should be filled in.
      * @param obj
      *            The value that will be put in the nested map structure.
      * @throws IllegalArgumentException
      *             When trying to access a map as a list or vice versa, while
      *             trying to do something against the data structure.
      * 
      */
     public static void putObjectInMap(PathResolver resolver, Map<String, Object> map, Object obj) {
         resolver.put(map, obj);
     }
 
     // Make the list larger so we can add values in there.
     //
     private static void expandIfNecessary(List<Object> list, int capacity) {
         int additional = capacity - list.size();
         for (int i = 0; i < additional; i++) {
             list.add(null);
         }
     }
 
     @SuppressWarnings("unchecked")
     private static Map<String, Object> flattenMapHelper(String prefix, Map<String, Object> flattened, Map<String, Object> nested) {
         // Walk trough all the keys in the map.
         //
         for (Entry<String, Object> entry : nested.entrySet()) {
             // Get the value of the current key.
             //
             final String key = entry.getKey();
             final Object value = entry.getValue();
             // Construct the key the current element would get in the flattened
             // map.
             //
             String newPrefix = (prefix.length() == 0) ? key : prefix + "." + key;
 
             // Q: Is the current value a map?
             // (If it is a nested map, we have to flatten it as well)
             //
             if (value instanceof Map) {
                 // A: Yes, the current value is a nested map.
                 // So we have to flatten this element.
                 flattenMapHelper(newPrefix, flattened, (Map<String, Object>) value);
             } else {
                 // A: No, the current element is not a nested map.
                 // We can copy it to the flattened list, we do not have to
                 // flatten this element.
                 flattened.put(newPrefix, value);
             }
         }
         return flattened;
     }
 
     private static Map<String, Object> flattenMap(Map<String, Object> nested) {
         return flattenMapHelper("", new TreeMap<String, Object>(), nested);
     }
 
     /**
      * Compare two nested maps, the result is a list of differences between the
      * maps.
      * <ul>
      * <li>"+key: [new value]" means that the right map contains a value that
      * was not present on the left hand.
      * <li>"-key: [deleted value]" means that the left map contains a value that
      * disappeared on the right hand.
      * <li>"!key: [old value] -> [new value]" means that the key is present on
      * the left and right sides, but the value is different.
      * </ul>
      * 
      * @param left
      * @param right
      * @return A list of differences.
      */
     public static List<String> compareMaps(Map<String, Object> left, Map<String, Object> right) {
         // Flatten both sides before comparing them.
         final Map<String, Object> flatLeft = flattenMap(left);
         final Map<String, Object> flatRight = flattenMap(right);
         final List<String> result = new ArrayList<String>();
 
         // Walk trough the left flattened map and compare the keys with the
         // right map.
         //
         for (String key : flatLeft.keySet()) {
             // Q: Does the right hand side contain the key as well?
             // Note that the keys can exist but the values can be null.
             // There is a distinction between a null value and the absence of a
             // key.
             //
             if (flatRight.containsKey(key)) {
                 // A: Yes, both sides contain the key.
                 // In this case we have to compare the values.
 
                 // Get left and right values, we must be careful since these
                 // values can be null.
                 final Object leftVal = flatLeft.get(key);
                 final Object rightVal = flatRight.get(key);
 
                 // Try to compare if one of the sides is not null.
                 // If both sides are null, the values are equal anyway.
                 //
                 if ((leftVal != null && !leftVal.equals(rightVal)) || (rightVal != null && !rightVal.equals(leftVal))) {
                     String marker = String.format("!%s: %s -> %s", key, leftVal == null ? NULL_LITERAL : leftVal.toString(), rightVal == null ? NULL_LITERAL : rightVal.toString());
                     result.add(marker);
                 }
 
                 // Delete the key from the right hand flattened map.
                 // All keys which are in the left hand side will be deleted from
                 // the right hand side.
                 // The keys that are remaining in the right hand map did not
                 // occur in the left hand map.
                 flatRight.remove(key);
             } else {
                 // A: Nope, only the left side contains the key.
                 // In this case we can immediately write the conclusion.
 
                 final Object leftVal = flatLeft.get(key);
                 String marker = String.format("-%s: %s", key, leftVal == null ? NULL_LITERAL : leftVal.toString());
                 result.add(marker);
             }
         }
 
         // Walk trough the remaining keys in the right map (we deleted the keys
         // present in the other map).
         // All these keys which are still in the right hand side are keys that
         // were not in the left hand map, otherwise
         // we would have seen them in the first loop and we would have deleted
         // these.
         for (String key : flatRight.keySet()) {
             Object val = flatRight.get(key);
             String marker = String.format("+%s: %s", key, val == null ? NULL_LITERAL : val.toString());
             result.add(marker);
         }
         return result;
     }
 
     /**
      * Check if the list of differences contains the addition of a key, the key
      * is a path part1.part2.part3
      * 
      * @param differences
      *            The list of differences as generated by the compareMaps()
      *            function.
      * @param path
      *            The path of the value in the map.
      * @return Can the new entry be found in the differences.
      */
     public static boolean hasAddition(List<String> differences, String path) {
         final String pattern = String.format("+%s:", path);
         return lookForPattern(differences, pattern);
     }
 
     /**
      * Check if the list of differences contains the removal of a key, the key
      * is a path part1.part2.part3
      * 
      * @param differences
      *            The list of differences as generated by the compareMaps()
      *            function.
      * @param path
      *            The path of the value in the map.
      * @return Was the key removed from the map?
      */
     public static boolean hasRemoval(List<String> differences, String path) {
         final String pattern = String.format("-%s:", path);
         return lookForPattern(differences, pattern);
     }
 
     /**
      * Check if the list of differences contains the value change for that key,
      * the key is a path part1.part2.part3
      * 
      * @param differences
      *            The list of differences as generated by the compareMaps()
      *            function.
      * @param path
      *            The path of the value in the map.
      * @return Was the value changed for that key?
      */
     public static boolean hasChange(List<String> differences, String path) {
         final String pattern = String.format("!%s:", path);
         return lookForPattern(differences, pattern);
     }
 
     private static boolean lookForPattern(List<String> differences, String pattern) {
         for (String diff : differences) {
             if (diff.startsWith(pattern)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * It represents a compiled path of the form "a.b.c[12][12].d". It is a good
      * idea to compile the path expressions to access the nested map structures
      * if the paths are used multiple times.
      */
     public static interface PathResolver {
         /**
          * Do a lookup of the path in the container and return the result.
          * 
          * @param container The container in which to resolve the path.
          * @return The resulting object.
          */
         Object get(Object container);
         
         /**
          * Put a value in the container as specified in the path reference.
          * If intermediate containers can be created, they will be created as well.
          * 
          * @param container The container in which we want to store a value.
          * @param value The value we want to put in the container.
          */
         void put(Object container, Object value);
         
         /**
          * Create a container corresponding to the current path. The type of 
          * the container depends on the type of the resolver. Indexed resolvers will create array
          * like structures, property resolvers will create map structures.
          * @return
          */
         Object createContainer();
     }
 
     /*
      * Resolver for a single property name in a map.
      * @see JsonUtil.PathResolver
      */
     private static class PropResolver implements PathResolver {
         private String property = null;
 
         public PropResolver(String property) {
             this.property = property;
         }
 
         private void check(Object container) {
             if (container == null || !(container instanceof Map)) {
                 throw new IllegalArgumentException("Container should be a Map instance.");
             }
         }
         
         /*
          * @see JsonUtil.PathResolver#get(Object)
          */
         public Object get(Object container) {
             check(container);
             Map<?, ?> map = (Map<?, ?>) container;
             return map.get(property);
         }
 
         /*
          * @see JsonUtil.PathResolver#put(Object, Object)
          */
         public void put(Object container, Object value) {
             check(container);
             @SuppressWarnings("unchecked")
             Map<Object, Object> map = (Map<Object, Object>) container;
             map.put(property, value);
         }
 
         /** 
          * @see JsonUtil.PathResolver#createContainer()
          */
         public Object createContainer() {
             return new LinkedHashMap<Object, Object>();
         }
     }
 
     /*
      * Resolver for a single index in an array. 
      * @see JsonUtil.PathResolver
      */
     private static class IndexResolver implements PathResolver {
         private int index;
 
         public IndexResolver(int index) {
             this.index = index;
         }
 
         private void check(Object container) {
             if (container == null || !(container instanceof List)) {
                 throw new IllegalArgumentException("Container should be a List instance.");
             }
         }
 
         /*
          * @see JsonUtil.PathResolver#get(java.lang.Object)
          */
         public Object get(Object container) {
             check(container);
             @SuppressWarnings("unchecked")
             List<Object> list = (List<Object>) container;
             if (index < list.size()) {
                 return list.get(index);
             } else {
                 return null;
             }
         }
 
         /*
          * @see JsonUtil.PathResolver#put(Object, Object)
          */
         public void put(Object container, Object value) {
             check(container);
             @SuppressWarnings("unchecked")
             List<Object> list = (List<Object>) container;
             expandIfNecessary(list, index);
             list.add(index, value);
         }
 
         /*
          * @see JsonUtil.PathResolver#createContainer()
          */
         public Object createContainer() {
             return new ArrayList<Object>();
         }
     }
 
     /*
      * Composite resolver, a sequence of resolvers. Complex paths are formed
      * by making a composition of the simple resolvers.
      * @see JsonUtil.PathResolver
      */
     private static class CompositeResolver implements PathResolver {
         private PathResolver[] resolvers;
 
         public CompositeResolver(PathResolver[] resolvers) {
             this.resolvers = resolvers;
         }
 
         /*
          * @see JsonUtil.PathResolver#get(Object)
          */
         public Object get(Object container) {
             Object intermediate = container;
             for (int i = 0; i < resolvers.length; i++) {
                 if (intermediate == null) {
                     return null;
                 } else {
                     intermediate = resolvers[i].get(intermediate);
                 }
             }
             return intermediate;
         }
 
         /* 
          * @see JsonUtil.PathResolver#put(Object, Object)
          */
         public void put(Object container, Object value) {
             Object intermediate = container;
             for (int i = 0; i < (resolvers.length - 1); i++) {
                 if (intermediate == null) {
                     // Can not proceed ...
                     throw new IllegalArgumentException("The value cannot be inserted in this container, there is no container to attach new nodes.");
                 } 
                 else {
                     Object candidate = resolvers[i].get(intermediate);
                     if (candidate != null) {
                         // We are fine.
                         intermediate = candidate;
                     } 
                     else if (candidate == null && (i < resolvers.length - 1)) {
                         // We will create a new node.
                         candidate = resolvers[i + 1].createContainer();
                         if (candidate == null) {
                             // Resolver type cannot produce a container ...
                             throw new IllegalArgumentException("It is not possible to create a new intermediary container.");
                         }
                         resolvers[i].put(intermediate, candidate);
                         intermediate = candidate;
                     } 
                     else {
                         // Cannot proceed, we don't know the type of the new
                         // node.
                         throw new IllegalArgumentException("It is not possible to create a new intermediary container.");
                     }
                 }
             }
             // finally
             resolvers[resolvers.length - 1].put(intermediate, value);
         }
 
         /*
          * @see JsonUtil.PathResolver#createContainer()
          */
         public Object createContainer() {
             return null;
         }
     }
 
     /*
      * A resolver that resolves to itself.
      * @see JsonUtil.PathResolver
      */
     private static class IdentityResolver implements PathResolver {
         
         /*
          * @see JsonUtil.PathResolver#get(Object)
          */
         public Object get(Object container) {
             return container;
         }
 
         /*
          * @see com.sdicons.json.JsonUtil.PathResolver#put(java.lang.Object, java.lang.Object)
          */
         public void put(Object container, Object value) {
             // Cannot put something in here.
             throw new IllegalArgumentException("You cannot put something in this resolver.");
         }
 
         /*
          * @see com.sdicons.json.JsonUtil.PathResolver#createContainer()
          */
         public Object createContainer() {
             return null;
         }
     }
 
     // Pattern of the form "part[123][456]"
     // Name of a field with optional indices.
     //
     private static final Pattern partFormat = Pattern.compile("([\\w\\-]+)((\\[\\s*\\d+\\s*\\])*)");
     // Pattern of an individual index "[123]"
     //
     private static final Pattern indexFormat = Pattern.compile("\\[\\s*(\\d+)\\s*\\]");
     //
     private static final PathResolver[] EMPTY_RESOLVER_ARR = new PathResolver[0];
 
     /**
      * Compile a path of the form "a.b[1].c[1][2]" into a resolver that can be
      * used to get/put values into a nested map structure. If a path is used
      * multiple times, compiling the path and using the resolver is much faster.
      * The resolvers are thread safe.
      * 
      * @param path
      *            A string representing a path into a data structure of nested
      *            maps and arrays. The path can have the form "a.b.c" and each
      *            path segment can contain zero or more indices "a[1][2]"
      * @return
      */
     public static PathResolver compilePath(String path) {
         // Root case.
         if ((null == path) || (".".equals(path) || "".equals(path))) {
             return new IdentityResolver();
         }
 
         // We collect the resolvers in this list.
         final List<PathResolver> resolvers = new ArrayList<JsonUtil.PathResolver>(16);
 
         // We will follow the path, part by part.
         //
         final String[] parts = path.split("\\.");
         for (int i = 0; i < parts.length; i++) {
             // Isolate the part we are going to examine.
             String part = parts[i].trim();
             //
             Matcher partMatcher = partFormat.matcher(part);
             if (partMatcher.matches()) {
                 // Identifier part.
                 //
                 String propName = partMatcher.group(1);
                 resolvers.add(new PropResolver(propName));
                 // Indices part.
                 //
                 String indices = partMatcher.group(2);
                 //
                 Matcher indexMatcher = indexFormat.matcher(indices);
                 while (indexMatcher.find()) {
                     String indexRepr = indexMatcher.group(1);
                     resolvers.add(new IndexResolver(Integer.parseInt(indexRepr)));
                 }
             }
         }
         return new CompositeResolver(resolvers.toArray(EMPTY_RESOLVER_ARR));
     }
 }
