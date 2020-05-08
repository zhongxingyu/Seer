 /*
  * 
  */
 package org.smartly.packages.mongo.impl.util;
 
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.smartly.commons.cryptograph.SecurityMessageDigester;
 import org.smartly.commons.logging.util.LoggingUtils;
 import org.smartly.commons.util.*;
 import org.smartly.packages.mongo.impl.IMongoConstants;
 
 import java.util.*;
 import java.util.regex.Pattern;
 
 /**
  * @author angelo.geminiani
  */
 public class MongoUtils implements IMongoConstants {
 
     public static final int CASE_INSENSITIVE = Pattern.CASE_INSENSITIVE;
 
     private static String SEP = "-";
     private static String LIST = "list"; // actionscript list tag
 
     private MongoUtils() {
     }
 
     public static String createUUID() {
         return UUID.randomUUID().toString().replace("-", "");
     }
 
     public static String createUUID(final int len) {
         return RandomUtils.random(len, RandomUtils.CHARS_LOW_NUMBERS);
     }
 
     /**
      * Return an ID based on date and time. i.e. "20010101_12:45:22".
      *
      * @return
      */
     public static String createDateTimeId() {
         final String datetime = FormatUtils.formatDate(DateUtils.now(),
                 FormatUtils.DEFAULT_DATEFORMAT.concat("_").
                         concat(FormatUtils.DEFAULT_TIMEFORMAT));
         return datetime;
     }
 
     public static String createMD5Id(final Object id) {
         try {
             return SecurityMessageDigester.encodeMD5(null != id ? id.toString() : createUUID());
         } catch (Exception ex) {
         }
         return createUUID();
     }
 
     public static Object hasId(final DBObject object) {
         return null != getId(object);
     }
 
     public static Object getId(final DBObject object) {
         if (null != object) {
             if (object.containsField(ID)) {
                 return object.get(ID);
             }
         }
         return null;
     }
 
     public static String getIdAsString(final DBObject object) {
         final Object id = getId(object);
         return null != id ? id.toString() : "";
     }
 
     public static String concatId(final Object... items) {
         final StringBuilder result = new StringBuilder();
         for (final Object item : items) {
             StringUtils.append(item, result, SEP);
         }
         return result.length() > 0
                 ? result.toString().trim()
                 : createUUID();
     }
 
     public static String[] splitId(final String id) {
         return StringUtils.split(id, SEP);
     }
 
     public static DBObject parseObject(final String jsontext) {
         if (StringUtils.hasText(jsontext) && StringUtils.isJSON(jsontext)) {
             final JsonWrapper wrapper = new JsonWrapper(jsontext);
             return wrapper.isJSONArray()
                     ? parseObject(wrapper.getJSONArray())
                     : parseObject(wrapper.getJSONObject());
         }
         return null;
     }
 
     public static DBObject parseObject(final JSONObject jsonObject) {
         if (null == jsonObject) {
             return null;
         }
         final BasicDBObject result = new BasicDBObject();
         final Iterator<String> keys = jsonObject.keys();
         while (keys.hasNext()) {
             final String key = keys.next();
             final Object value = jsonObject.opt(key);
             if(null!=value){
                 if(value instanceof JSONObject){
                    final DBObject item = parseObject((JSONArray) value);
                     if (null != item) {
                         result.put(key, item);
                     }
                 } else if (value instanceof JSONArray){
                    final DBObject item = parseObject((JSONObject) value);
                     if (null != item) {
                         result.put(key, item);
                     }
                 } else {
                     result.put(key, value);
                 }
             }
         }
         return result;
     }
 
     public static DBObject parseObject(final JSONArray jsonArray) {
         if (null == jsonArray) {
             return null;
         }
         final BasicDBList result = new BasicDBList();
         if (null != jsonArray && jsonArray.length() > 0) {
             for (int i = 0; i < jsonArray.length(); i++) {
                 final Object value = jsonArray.opt(i);
                 if (null != value) {
                     if (value instanceof JSONArray) {
                         final DBObject item = parseObject((JSONArray) value);
                         if (null != item) {
                             result.add(item);
                         }
                     } else if (value instanceof JSONObject) {
                         final DBObject item = parseObject((JSONObject) value);
                         if (null != item) {
                             result.add(item);
                         }
                     } else {
                         result.add(value);
                     }
                 }
             }
         }
         return result;
     }
 
     /**
      * Parse JSON object for a List.<br/>
      * Actionscript may wrap lists in Objects with a 'list' field.
      * i.e. {list:['hello','list']}
      *
      * @param jsontext
      * @return
      */
     public static List parseList(final String jsontext) {
         final DBObject item = parseObject(jsontext);
         if (item instanceof List) {
             return (List) item;
         } else {
             final Object list = item.get(LIST);
             if (list instanceof List) {
                 return (List) list;
             }
         }
         return null;
     }
 
     /**
      * Validate an object for JSON standard.
      * Null or empty collections, empty array or empty maps are not allowed.
      *
      * @param value Value to validate
      * @return
      */
     public static boolean isValidJSONValue(final Object value) {
         return !CollectionUtils.isEmpty(value);
     }
 
     // <editor-fold defaultstate="collapsed" desc=" get with convertions (getBoolean, getString,..)">
     public static Object get(final DBObject object,
                              final String fieldName) {
         return get(object, fieldName, null);
     }
 
     public static Object get(final DBObject object,
                              final String fieldName, final Object defaultValue) {
         Object result = null;
         if (null != object && object.containsField(fieldName)) {
             result = object.get(fieldName);
         }
         return null != result ? result : defaultValue;
     }
 
     public static String getString(final DBObject object,
                                    final String fieldName) {
         return getString(object, fieldName, "");
     }
 
     public static String getString(final DBObject object,
                                    final String fieldName, final String defaultValue) {
         if (null != object && object.containsField(fieldName)) {
             return StringUtils.toString(object.get(fieldName), defaultValue);
         }
         return defaultValue;
     }
 
     public static boolean getBoolean(final DBObject object,
                                      final String fieldName) {
         return getBoolean(object, fieldName, false);
     }
 
     public static boolean getBoolean(final DBObject object,
                                      final String fieldName, final boolean defaultValue) {
         if (null != object && object.containsField(fieldName)) {
             return ConversionUtils.toBoolean(object.get(fieldName), defaultValue);
         }
         return defaultValue;
     }
 
     public static long getLong(final DBObject object,
                                final String fieldName) {
         return getLong(object, fieldName, 0L);
     }
 
     public static long getLong(final DBObject object,
                                final String fieldName, final long defaultValue) {
         if (null != object && object.containsField(fieldName)) {
             return ConversionUtils.toLong(object.get(fieldName), defaultValue);
         }
         return defaultValue;
     }
 
     public static int getInt(final DBObject object,
                              final String fieldName) {
         return getInt(object, fieldName, 0);
     }
 
     public static int getInt(final DBObject object,
                              final String fieldName, final int defaultValue) {
         if (null != object && object.containsField(fieldName)) {
             return ConversionUtils.toInteger(object.get(fieldName), defaultValue);
         }
         return defaultValue;
     }
 
     public static double getDouble(final DBObject object,
                                    final String fieldName) {
         return getDouble(object, fieldName, -1, 0.0);
     }
 
     public static double getDouble(final DBObject object,
                                    final String fieldName, final double defaultValue) {
         return getDouble(object, fieldName, -1, defaultValue);
     }
 
     public static double getDouble(final DBObject object,
                                    final String fieldName,
                                    final int decimals,
                                    final double defaultValue) {
         if (null != object && object.containsField(fieldName)) {
             return ConversionUtils.toDouble(object.get(fieldName), decimals,
                     defaultValue);
         }
         return defaultValue;
     }
 
     public static DBObject getDBObject(final DBObject object,
                                        final String fieldName) {
         return getDBObject(object, fieldName, new BasicDBObject());
     }
 
     public static DBObject getDBObject(final DBObject object,
                                        final String fieldName, final DBObject defaultValue) {
         if (null != object && object.containsField(fieldName)) {
             final Object result = object.get(fieldName);
             if (result instanceof DBObject) {
                 return (DBObject) result;
             }
         }
         return defaultValue;
     }
 
     //-- LIST --//
     public static List getList(final DBObject object,
                                final String fieldName) {
         return getList(object, fieldName, new ArrayList());
     }
 
     public static List getList(final DBObject object,
                                final String fieldName,
                                final List defaultValue) {
         if (null != object) {
             if (object.containsField(fieldName)) {
                 final Object result = object.get(fieldName);
                 if (result instanceof List) {
                     return (List) result;
                 }
             } else {
                 // add missing field
                 object.put(fieldName, defaultValue);
             }
         }
         return defaultValue;
     }
     // </editor-fold>
 
     public static int inc(final DBObject object,
                           final String fieldName, final int value) {
         if (null != object) {
             final int data = getInt(object, fieldName) + value;
             object.put(fieldName, data);
             return data;
         }
         return 0;
     }
 
     public static void put(final DBObject object,
                            final String key, final Object value) {
         if (isValidJSONValue(value)) {
             // valus is not null or empty collection
             object.put(key, value);
         } else {
             // value is null or empty. REMOVED!
             object.removeField(key);
         }
     }
 
     // <editor-fold defaultstate="collapsed" desc=" merge, clone ">
     public static void merge(final Object source, final Object target,
                              final String... excludeProperties) throws Exception {
         if (null != source && null != target) {
             if (source instanceof DBObject && target instanceof DBObject) {
                 merge((DBObject) source, (DBObject) target);
             } else if (target instanceof DBObject) {
                 merge(source, (DBObject) target, excludeProperties);
             }
         }
     }
 
     public static void merge(final Object source, final DBObject target,
                              final String[] excludeProperties) throws Exception {
         if (null != source && null != target) {
             final String[] keys = BeanUtils.getPropertyNames(source.getClass());
             for (final String key : keys) {
                 if (!CollectionUtils.contains(excludeProperties, key)) {
                     mergeKey(key, source, target);
                 }
             }
         }
     }
 
     /**
      * Merge properties of target with source.
      *
      * @param source
      * @param target
      */
     public static void merge(final DBObject source, final DBObject target,
                              final String[] excludeProperties) {
         if (null != source && null != target) {
             final Set<String> keys = source.keySet();
             for (final String key : keys) {
                 if (!CollectionUtils.contains(excludeProperties, key)) {
                     mergeKey(key, source, target);
                 }
             }
         }
     }
 
     public static void mergeKey(final String key,
                                 final Object source, final DBObject target) throws Exception {
         if (source instanceof DBObject) {
             mergeKey(key, (DBObject) source, target);
         } else {
             final Object svalue = BeanUtils.getValue(source, key);
             final Object tvalue = target.get(key);
             if (null == tvalue || null == svalue) {
                 // add new value to target
                 target.put(key, svalue);
             } else {
                 if (svalue instanceof DBObject) {
                     putValue(target, key, (DBObject) svalue);
                 } else {
                     // SOURCE is NULL or other Type (String, int, Object...)
                     // only primitives are allowed
                     if (BeanUtils.isPrimitiveClass(svalue)) {
                         if (tvalue instanceof Collection) {
                             // TARGET is a Collection
                             ((Collection) tvalue).add(svalue);
                         } else {
                             // add new value to target
                             target.put(key, svalue);
                         }
                     }
                 }
             }
         }
     }
 
     /**
      * Merge single property from source to target.
      *
      * @param key
      * @param source
      * @param target
      */
     public static void mergeKey(final String key,
                                 final DBObject source, final DBObject target) {
         final Object svalue = source.get(key);
         final Object tvalue = target.get(key);
         if (null == tvalue || null == svalue) {
             // add new value to target
             target.put(key, svalue);
         } else {
             if (svalue instanceof Collection) {
                 // SOURCE is Collection
                 putValue(target, key, (Collection) svalue);
             } else if (svalue instanceof DBObject) {
                 putValue(target, key, (DBObject) svalue);
             } else {
                 // SOURCE is NULL or other Type (String, int, Object...)
                 if (tvalue instanceof Collection) {
                     // TARGET is a Collection
                     ((Collection) tvalue).add(svalue);
                 } else {
                     // add new value to target
                     target.put(key, svalue);
                 }
             }
         }
     }
 
     public static DBObject clone(final DBObject source,
                                  final String[] excludeProperties) {
         final DBObject target = new BasicDBObject();
         merge(source, target, excludeProperties);
         return target;
     }
 
     // </editor-fold>
 
     public static Pattern patternStartWith(final String value) {
         return patternStartWith(value, CASE_INSENSITIVE);
     }
 
     public static Pattern patternStartWith(final String value, final Integer flags) {
         return null != flags
                 ? Pattern.compile("^".concat(value).concat(".*$"), flags)
                 : Pattern.compile("^".concat(value).concat(".*$"));
     }
 
     public static Pattern patternEndWith(final String value) {
         return patternEndWith(value, CASE_INSENSITIVE);
     }
 
     public static Pattern patternEndWith(final String value, final Integer flags) {
         return null != flags
                 ? Pattern.compile(value.concat("$"), flags)
                 : Pattern.compile(value.concat("$"));
     }
 
     public static Pattern patternStartWithEndWith(final String startValue,
                                                   final String endValue) {
         return patternStartWithEndWith(startValue, endValue, CASE_INSENSITIVE);
     }
 
     public static Pattern patternStartWithEndWith(final String startValue,
                                                   final String endValue, final Integer flags) {
         return null != flags
                 ? Pattern.compile("^".concat(startValue).concat("(.*)".concat(endValue).concat("$")), flags)
                 : Pattern.compile("^".concat(startValue).concat("(.*)".concat(endValue).concat("$")));
     }
 
     public static Pattern patternContains(final String value) {
         return patternContains(value, CASE_INSENSITIVE);
     }
 
     public static Pattern patternContains(final String value,
                                           final Integer flags) {
         return null != flags
                 ? Pattern.compile("^.*".concat(value).concat(".*$"), flags)
                 : Pattern.compile("^.*".concat(value).concat(".*$"));
     }
 
     public static Pattern patternEquals(final String value) {
         return patternEquals(value, CASE_INSENSITIVE);
     }
 
     public static Pattern patternEquals(final String value,
                                         final Integer flags) {
         return null != flags
                 ? Pattern.compile("\\A".concat(value).concat("\\z"), flags)
                 : Pattern.compile("\\A".concat(value).concat("\\z"));
     }
 
     public static boolean queryIsOR(final DBObject query) {
         if (null != query) {
             return null != query.get(OP_OR);
         }
         return false;
     }
 
     /**
      * { x : "a" }<br/>
      * { x : { $in : [ null ] } }<br/>
      * { x : { $in : [ a, b ] } }<br/>
      *
      * @param field
      * @param value
      * @return
      */
     public static DBObject queryEquals(final String field,
                                        final Object value) {
         final DBObject query;
         if (null == value) {
             // {"z" : {"$in" : [null], "$exists" : true}}
             query = new BasicDBObject();
             final DBObject condition = new BasicDBObject();
             condition.put(OP_IN, new Object[]{null});
             condition.put(OP_EXISTS, true);
             query.put(field, condition);
         } else if (value instanceof List) {
             // { x : { $in : [ a, b ] } }
             final List lvalue = (List) value;
             query = new BasicDBObject();
             final DBObject condition = new BasicDBObject();
             condition.put(OP_IN, lvalue.toArray(new Object[lvalue.size()]));
             //condition.put(OP_EXISTS, true);
             query.put(field, condition);
         } else {
             query = new BasicDBObject(field, value);
         }
         return query;
     }
 
     public static DBObject queryStartWith(final String field,
                                           final String value) {
         return queryStartWith(field, value, CASE_INSENSITIVE);
     }
 
     public static DBObject queryStartWith(final String field,
                                           final String value, final Integer flags) {
         final Pattern pattern = patternStartWith(value, flags);
         final DBObject query = new BasicDBObject(field, pattern);
         return query;
     }
 
     /**
      * Return a query object to search a value "like"
      *
      * @param field Search field
      * @param value Search value
      * @return
      */
     public static DBObject queryContains(final String field,
                                          final String value) {
         // ^.*John.*$
         return queryContains(field, value, CASE_INSENSITIVE);
     }
 
     /**
      * @param field
      * @param value
      * @param flags Match flags, a bit mask.
      *              (i.e. Pattern.CASE_INSENSITIVE | Pattern.MULTILINE)
      * @return
      */
     public static DBObject queryContains(final String field,
                                          final String value,
                                          final Integer flags) {
         // ^.*John.*$
         final Pattern pattern = patternContains(value, flags);
         final DBObject query = new BasicDBObject(field, pattern);
         return query;
     }
 
     public static DBObject queryEndWith(final String field,
                                         final String value) {
         return queryEndWith(field, value, CASE_INSENSITIVE);
     }
 
     public static DBObject queryEndWith(final String field,
                                         final String value, final Integer flags) {
         final Pattern pattern = patternEndWith(value, flags);
         final DBObject query = new BasicDBObject(field, pattern);
         return query;
     }
 
     public static DBObject queryStartWithEndWith(final String field,
                                                  final String startValue, final String endValue) {
         return queryStartWithEndWith(field, startValue, endValue, CASE_INSENSITIVE);
     }
 
     public static DBObject queryStartWithEndWith(final String field,
                                                  final String startValue, final String endValue, final Integer flags) {
         final Pattern pattern = patternStartWithEndWith(startValue, endValue, flags);
         final DBObject query = new BasicDBObject(field, pattern);
         return query;
     }
 
     public static DBObject queryGreaterThan(final String field,
                                             final Object value, final boolean equals) {
         final DBObject condition = conditionGreaterThan(value, equals);
         final DBObject query = new BasicDBObject(field, condition);
         return query;
     }
 
     public static DBObject conditionGreaterThan(
             final Object value, final boolean equals) {
         final String operator = equals ? OP_GTE : OP_GT;
         final DBObject condition = new BasicDBObject();
         condition.put(operator, value);
         return condition;
     }
 
     public static DBObject queryLowerThan(final String field,
                                           final Object value, final boolean equals) {
         final DBObject condition = conditionLowerThan(value, equals);
         final DBObject query = new BasicDBObject(field, condition);
         return query;
     }
 
     public static DBObject conditionLowerThan(
             final Object value, final boolean equals) {
         final String operator = equals ? OP_LTE : OP_LT;
         final DBObject condition = new BasicDBObject();
         condition.put(operator, value);
         return condition;
     }
 
     public static DBObject queryBetween(final String field,
                                         final Object value1, final Object value2, final boolean equals) {
         final String op_lower = equals ? OP_LTE : OP_LT;
         final String op_greater = equals ? OP_GTE : OP_GT;
         final DBObject condition = new BasicDBObject();
         condition.put(op_greater, value1);
         condition.put(op_lower, value2);
         final DBObject query = new BasicDBObject(field, condition);
         return query;
     }
 
     public static DBObject queryFromTo(final String fieldFROM,
                                        final String fieldTO, final Date date) {
         final String today = FormatUtils.formatDate(date);
         final DBObject from = new BasicDBObject();
         from.put(IMongoConstants.OP_LTE, today);
         final DBObject to = new BasicDBObject();
         to.put(IMongoConstants.OP_GTE, today);
         final DBObject query = new BasicDBObject();
         query.put(fieldFROM, from);
         query.put(fieldTO, to);
 
         return query;
     }
 
     public static DBObject modifierSet(final String[] names,
                                        final Object[] values) {
         final DBObject modifier = new BasicDBObject();
         if (!CollectionUtils.isEmpty(names) && !CollectionUtils.isEmpty(values)) {
             final DBObject condition = new BasicDBObject();
             for (int i = 0; i < names.length; i++) {
                 condition.put(names[i], values[i]);
             }
             modifier.put(MO_SET, condition);
         }
         return modifier;
     }
 
     /**
      * { $addToSet : { field : value } }
      *
      * @param field
      * @param value
      * @return
      */
     public static DBObject modifierAddToSet(final String field,
                                             final Object value) {
         final DBObject modifier = new BasicDBObject();
         final DBObject condition = new BasicDBObject();
         condition.put(field, value);
         modifier.put(MO_ADDTOSET, condition);
 
         return modifier;
     }
 
     /**
      * { $pull : { field : {fieldName: value} } }
      * Returns a pull modifier to
      * remove all occurrences of value from field, if field is an array.
      * If field is present but is not an array, an error condition is raised.
      *
      * @param fieldName Array field name
      * @param names     Condition field names
      * @param values    Condition field values
      * @return { $pull : { field : {fieldName: value} } }
      *         i.e. removes array elements with fieldName matching value
      */
     public static DBObject modifierPull(final String fieldName,
                                         final String[] names, final Object[] values) {
         final DBObject modifier = new BasicDBObject();
         if (!CollectionUtils.isEmpty(names) && !CollectionUtils.isEmpty(values)) {
             final DBObject condition = new BasicDBObject();
             for (int i = 0; i < names.length; i++) {
                 condition.put(names[i], values[i]);
             }
             final DBObject field = new BasicDBObject(fieldName, condition);
             modifier.put(MO_PULL, field);
         }
         return modifier;
     }
 
     /**
      * { $pull : { field : _value } }
      *
      * @param fieldName
      * @param value
      * @return { $pull : { field : _value } }
      */
     public static DBObject modifierPull(final String fieldName,
                                         final Object value) {
         final DBObject modifier = new BasicDBObject();
         final DBObject condition = new BasicDBObject();
         condition.put(fieldName, value);
         modifier.put(MO_PULL, condition);
 
         return modifier;
     }
 
     /**
      * Remove empty array from List or simple DBObject.
      * Empty Array are not valid in json and may cause parsing exceptions.
      *
      * @param object DBObject
      * @return new instance of DBObject.
      */
     public static DBObject removeEmptyArrays(final DBObject object) {
         try {
             if (object instanceof List) {
                 // BasicDBList
                 final List list = (List) object;
                 final BasicDBList result = new BasicDBList();
                 for (final Object obj : list) {
                     final Object cleaned;
                     if (obj instanceof DBObject) {
                         final DBObject item = (DBObject) obj;
                         cleaned = removeEmptyArrays(item);
                     } else {
                         cleaned = obj;
                     }
                     result.add(cleaned);
                 }
                 return result;
             } else {
                 // BasicDBObject
                 final DBObject result = new BasicDBObject();
                 final Set<String> keys = object.keySet();
                 for (final String key : keys) {
                     final Object value = object.get(key.toString());
                     if (value instanceof List) {
                         final List list = (List) value;
                         if (!list.isEmpty()) {
                             if (list instanceof DBObject) {
                                 result.put(key, removeEmptyArrays((DBObject) list));
                             } else {
                                 result.put(key, list);
                             }
                         }
                     } else {
                         result.put(key, value);
                     }
                 }
                 return result;
             }
         } catch (Throwable t) {
             LoggingUtils.getLogger().fine(t.toString());
         }
 
         return object;
     }
 
     /**
      * Return an array of field names. i.e. {"_id", "name", ...} excluding from
      * result all names in 'excludeFieldNames' parameter
      *
      * @param item              The item to analyze for field names
      * @param excludeFieldNames Names to exclude from list
      * @return
      */
     public static String[] getFieldNames(final DBObject item,
                                          final String[] excludeFieldNames) {
         final Set<String> keys = item.keySet();
         if (CollectionUtils.isEmpty(excludeFieldNames)) {
             return keys.toArray(new String[keys.size()]);
         } else {
             final List<String> result = new LinkedList<String>();
             for (final String key : keys) {
                 if (!CollectionUtils.contains(excludeFieldNames, key)) {
                     result.add(key);
                 }
             }
             return result.toArray(new String[result.size()]);
         }
     }
 
     /**
      * Return an object containing fields to include or fields to exclude.
      * i.e. "{thumbnail:0}" exclude 'thumbnail' field.
      *
      * @param fieldNames
      * @param include
      * @return
      */
     public static DBObject getFields(final String[] fieldNames,
                                      final boolean include) {
         if (!CollectionUtils.isEmpty(fieldNames)) {
             final DBObject result = new BasicDBObject();
             for (final String field : fieldNames) {
                 result.put(field, include ? 1 : 0);
             }
             return result;
         }
         return null;
     }
 
     /**
      * @param fieldNames
      * @param ascending
      * @return i.e. "{name : 1, age : 1}" 'name' and 'age'ascending<br/>
      *         i.e. "{name : -1, age : -1}" 'name' and 'age'descending
      */
     public static DBObject getSortFields(final String[] fieldNames,
                                          final boolean ascending) {
         if (!CollectionUtils.isEmpty(fieldNames)) {
             final DBObject result = new BasicDBObject();
             for (final String field : fieldNames) {
                 result.put(field, ascending ? 1 : -1);
             }
             return result;
         }
         return new BasicDBObject(ID, 1);
     }
 
     /**
      * @param asc
      * @param desc
      * @return i.e. "{name : 1, age : 1}" 'name' and 'age'ascending<br/>
      *         i.e. "{name : -1, age : -1}" 'name' and 'age'descending
      */
     public static DBObject getSortFields(final String[] asc,
                                          final String[] desc) {
         if (!CollectionUtils.isEmpty(asc) || !CollectionUtils.isEmpty(desc)) {
             final DBObject result = new BasicDBObject();
             if (null != asc) {
                 for (final String field : asc) {
                     result.put(field, 1);
                 }
             }
             if (null != desc) {
                 for (final String field : desc) {
                     result.put(field, -1);
                 }
             }
             return result;
         }
         return new BasicDBObject(ID, 1);
     }
 
     /**
      * Check two DBObject's ID. If item1._id==item2._id return true.
      *
      * @param item1
      * @param item2
      * @return
      */
     public static boolean equals(final DBObject item1, final DBObject item2) {
         final String id1 = MongoUtils.getIdAsString(item1);
         final String id2 = MongoUtils.getIdAsString(item2);
         return id1.equalsIgnoreCase(id2);
     }
 
     // ------------------------------------------------------------------------
     //                      p r i v a t e
     // ------------------------------------------------------------------------
     private static void putValue(final DBObject target, final String key,
                                  final Collection sourcevalues) {
         final Object tvalue = target.get(key);
         if (tvalue instanceof Collection) {
             final Collection tvaluelist = (Collection) tvalue;
             // add items to target collection
             CollectionUtils.addAllNoDuplicates(tvaluelist, sourcevalues);
         } else {
             // Source is Collection but target not.
             sourcevalues.add(tvalue);
             target.put(key, sourcevalues);
         }
     }
 
     private static void putValue(final DBObject target, final String key,
                                  final DBObject sourcevalue) {
         final Object tvalue = target.get(key);
         if (tvalue instanceof Collection) {
             final Collection tvaluelist = (Collection) tvalue;
             // TARGET is a Collection
             CollectionUtils.addNoDuplicates(tvaluelist, sourcevalue);
         } else if (tvalue instanceof DBObject) {
             // TARGET is DBObject
             // merge values
             merge(sourcevalue, (DBObject) tvalue, null);
         } else {
             // TARGET is NULL or other Type (String, int, Object...)
             target.put(key, sourcevalue);
         }
     }
 }
