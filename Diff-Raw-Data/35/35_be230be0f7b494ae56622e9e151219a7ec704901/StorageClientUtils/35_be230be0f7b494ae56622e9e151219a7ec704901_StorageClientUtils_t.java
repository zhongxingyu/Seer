 /*
  * Licensed to the Sakai Foundation (SF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The SF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations under the License.
  */
 package org.sakaiproject.nakamura.api.lite;
 
 import com.google.common.collect.ImmutableMap;
 import com.google.common.collect.ImmutableMap.Builder;
 import com.google.common.collect.Maps;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.time.FastDateFormat;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.api.lite.content.ContentManager;
 import org.sakaiproject.nakamura.api.lite.util.Type1UUID;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Method;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.TimeZone;
 
 /**
  * Utilites for managing storage related to the Sparse Map Content Store.
  */
 public class StorageClientUtils {
 
     /**
      * UTF8 charset constant
      */
     public final static String UTF8 = "UTF-8";
     /** how are numbers encoded, base ? */
     public final static int ENCODING_BASE = 10;
     /**
      * Default hashing algorithm for passwords
      */
     public final static String SECURE_HASH_DIGEST = "SHA-512";
     /**
      * Charset for encoding byte data as char
      */
     public static final char[] URL_SAFE_ENCODING = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
             .toCharArray();
     /**
      * Based on JackRabbit: Jackrabbit uses a subset of 8601 (8601:2000) for
      * their date times.
      */
     public static String ISO8601_JCR_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZZ";
     @SuppressWarnings("unused")
     private final static FastDateFormat ISO8601_JCR_FORMAT = FastDateFormat.getInstance(
             ISO8601_JCR_PATTERN, TimeZone.getTimeZone("UTC"), Locale.ROOT);
 
     private static final Logger LOGGER = LoggerFactory.getLogger(StorageClientUtils.class);
 
     /**
      * Concert a storage object to string. In the sparse store everything is
      * stored in as an efficient way as possible. To avoid unnecessary
      * marshalling to and from the store we leave it to the application to
      * marshal properties in and out of the store on demand.
      * 
      * @param object
      *            the storage object
      * @return a string representation of the storage object.
      */
     @Deprecated
     public static String toString(Object object) {
         try {
             if (object instanceof String) {
                 return (String) object;
             } else if (object == null || object instanceof RemoveProperty) {
                 return null;
             } else if (object instanceof byte[]) {
                 return new String((byte[]) object, UTF8);
             } else {
                 LOGGER.warn("Converting " + object.getClass() + " to String via toString");
                 return String.valueOf(object);
             }
         } catch (UnsupportedEncodingException e) {
             return null; // no utf8.. get real!
         }
     }
 
     /**
      * Get the name of an alternative field for an alternative stream.
      * 
      * @param field
      *            the fieldname
      * @param streamId
      *            the alternative stream name
      * @return the alternative field name.
      */
     public static String getAltField(String field, String streamId) {
         if (streamId == null) {
             return field;
         }
         return field + "/" + streamId;
     }
 
     /**
      * Convert the object from application to store format. This should be used
      * whenever a value is being placed into the store.
      * 
      * @param object
      *            the object to place in store.
      * @return the Store representation of the object.
      */
     @Deprecated
     public static Object toStore(Object object) {
         return object;
     }
 
     /**
      * Convert a store object to a byte[]
      * 
      * @param value
      *            the store object
      * @return a byte[] of the store object.
      */
     @Deprecated
     public static byte[] toBytes(Object value) {
         if (value instanceof byte[]) {
             return (byte[]) value;
         } else {
             try {
                 return String.valueOf(value).getBytes("UTF-8");
             } catch (UnsupportedEncodingException e) {
                 return null; // no utf8.. get real!
             }
         }
     }
 
     /**
      * @param objectPath
      * @return true if the objectPath represents a root path.
      */
     public static boolean isRoot(String objectPath) {
         return (objectPath == null) || "/".equals(objectPath) || "".equals(objectPath)
                 || (objectPath.indexOf("/") < 0);
     }
 
     /**
      * @param objectPath
      * @return the parent of the supplied path or the path if already a root.
      */
     public static String getParentObjectPath(String objectPath) {
         if ("/".equals(objectPath)) {
             return "/";
         }
         int i = objectPath.lastIndexOf('/');
         if (i == objectPath.length() - 1) {
             i = objectPath.substring(0, i).lastIndexOf('/');
         }
         String res = objectPath;
         if (i > 0) {
             res = objectPath.substring(0, i);
         } else if (i == 0) {
             return "/";
         }
         return res;
     }
 
     /**
      * @param objectPath
      * @return the name of the supplied path, normally defiend as the last
      *         element in the path.
      */
     public static String getObjectName(String objectPath) {
         if ("/".equals(objectPath)) {
             return "/";
         }
         int i = objectPath.lastIndexOf('/');
         int j = objectPath.length();
         if (i == objectPath.length() - 1) {
             j--;
             i = objectPath.substring(0, i).lastIndexOf('/');
         }
         String res = objectPath;
         if (i >= 0) {
             res = objectPath.substring(i + 1, j);
         }
         return res;
 
     }
 
     /**
      * @param naked
      * @return a lower cost insecure hash of the naked value which can be used
      *         for keys as its not too long.
      */
     // TODO: Unit test
     public static String insecureHash(String naked) {
         try {
             MessageDigest md;
             try {
                 md = MessageDigest.getInstance("SHA-1");
             } catch (NoSuchAlgorithmException e1) {
                 try {
                     md = MessageDigest.getInstance("MD5");
                 } catch (NoSuchAlgorithmException e2) {
                     LOGGER.error("You have no Message Digest Algorightms intalled in this JVM, secure Hashes are not availalbe, encoding bytes :"
                             + e2.getMessage());
                     return encode(StringUtils.leftPad(naked, 10, '_').getBytes(UTF8),
                             URL_SAFE_ENCODING);
                 }
             }
             byte[] bytes = md.digest(naked.getBytes(UTF8));
             return encode(bytes, URL_SAFE_ENCODING);
         } catch (UnsupportedEncodingException e3) {
             LOGGER.error("no UTF-8 Envoding, get a real JVM, nothing will work here. NPE to come");
             return null;
         }
     }
 
     /**
      * @param password
      * @return as secure hash of the supplied password, unsuitable for keys as
      *         its too long.
      */
     public static String secureHash(String password) {
         try {
             MessageDigest md;
             try {
                 md = MessageDigest.getInstance(SECURE_HASH_DIGEST);
             } catch (NoSuchAlgorithmException e) {
                 try {
                     md = MessageDigest.getInstance("SHA-1");
                 } catch (NoSuchAlgorithmException e1) {
                     try {
                         md = MessageDigest.getInstance("MD5");
                     } catch (NoSuchAlgorithmException e2) {
                         LOGGER.error("You have no Message Digest Algorightms intalled in this JVM, secure Hashes are not availalbe, encoding bytes :"
                                 + e2.getMessage());
                         return encode(StringUtils.leftPad(password, 10, '_').getBytes(UTF8),
                                 URL_SAFE_ENCODING);
                     }
                 }
             }
             byte[] bytes = md.digest(password.getBytes(UTF8));
             return encode(bytes, URL_SAFE_ENCODING);
         } catch (UnsupportedEncodingException e3) {
             LOGGER.error("no UTF-8 Envoding, get a real JVM, nothing will work here. NPE to come");
             return null;
         }
     }
 
     /**
      * Generate an encoded array of chars using as few chars as possible
      * 
      * @param hash
      *            the hash to encode
      * @param encode
      *            a char array of encodings any length you lik but probably but
      *            the shorter it is the longer the result. Dont be dumb and use
      *            an encoding size of < 2.
      * @return
      */
     public static String encode(byte[] hash, char[] encode) {
         StringBuilder sb = new StringBuilder((hash.length * 15) / 10);
         int x = (int) (hash[0] + 128);
         int xt = 0;
         int i = 0;
         while (i < hash.length) {
             if (x < encode.length) {
                 i++;
                 if (i < hash.length) {
                     if (x == 0) {
                         x = (int) (hash[i] + 128);
                     } else {
                         x = (x + 1) * (int) (hash[i] + 128);
                     }
                 } else {
                     sb.append(encode[x]);
                     break;
                 }
             }
             xt = x % encode.length;
             x = x / encode.length;
             sb.append(encode[xt]);
         }
 
         return sb.toString();
     }
 
     /**
      * Converts to an Immutable map, with keys that are in the filter not
      * transdered. Nested maps are also transfered.
      * 
      * @param <K>
      * @param <V>
      * @param source
      * @param filter
      * @return
      */
     @SuppressWarnings("unchecked")
     public static <K, V> Map<K, V> getFilterMap(Map<K, V> source, Map<K, V> modified, Set<K> include, Set<K> exclude) {
        if ((modified == null || modified.size() == 0) && (include == null) && ( exclude == null || exclude.size() == 0)) {
            if ( source instanceof ImmutableMap ) {
                return source;
            } else {
                return ImmutableMap.copyOf(source);
            }
         }
         Builder<K, V> filteredMap = new ImmutableMap.Builder<K, V>();
         for (Entry<K, V> e : source.entrySet()) {
             K k = e.getKey();
             if (include == null || include.contains(k)) {
                 if (exclude == null || !exclude.contains(k)) {
                     if ( modified != null && modified.containsKey(k) ) {
                         V o = modified.get(k);
                         if (o instanceof Map) {
                             filteredMap.put(k,
                                     (V) getFilterMap((Map<K, V>) o, null, null, exclude));
                         } else if ( !(o instanceof RemoveProperty) ) {
                             filteredMap.put(k, o);
                         }
                     } else {
                         Object o = e.getValue();
                         if (o instanceof Map) {
                             filteredMap.put(k,
                                     (V) getFilterMap((Map<K, V>) e.getValue(), null, null, exclude));
                         } else {
                             filteredMap.put(k, e.getValue());
                         }
                     }
                 }
             }
         }
         if ( modified != null ) {
             // process additions
             for (Entry<K, V> e : modified.entrySet()) {
                 K k = e.getKey();
                 if ( !source.containsKey(k)) {
                     V v = e.getValue();
                     if ( !(v instanceof RemoveProperty) && v != null ) {
                         filteredMap.put(k,v);
                     }
                 }
             }
         }
         return filteredMap.build();
     }
 
     /**
      * Converts a map into Map or byte[] values with String keys. No control
      * over depth of nesting. Keys in the filter set are not transfered
      * Resulting map is mutable.
      * 
      * @param source
      * @param filter
      * @return
      */
     @SuppressWarnings("unchecked")
     public static Map<String, Object> getFilteredAndEcodedMap(Map<String, Object> source,
             Set<String> filter) {
         Map<String, Object> filteredMap = Maps.newHashMap();
         for (Entry<String, Object> e : source.entrySet()) {
             if (!filter.contains(e.getKey())) {
                 Object o = e.getValue();
                 if (o instanceof Map) {
                     filteredMap.put(e.getKey(),
                             getFilteredAndEcodedMap((Map<String, Object>) o, filter));
                 } else {
                     filteredMap.put(e.getKey(), o);
                 }
             }
         }
         return filteredMap;
     }
 
     /**
      * @return a UUID, compact encoded, suitable for use in URLs
      */
     public static String getUuid() {
         return StorageClientUtils.encode(Type1UUID.next(), StorageClientUtils.URL_SAFE_ENCODING);
     }
 
     /**
      * @param object
      * @return the store object as an int.
      */
     public static int toInt(Object object) {
         if (object instanceof Integer) {
             return ((Integer) object).intValue();
         } else if (object == null || object instanceof RemoveProperty) {
             return 0;
         }
         return Integer.parseInt(toString(object), ENCODING_BASE);
     }
 
     /**
      * @param object
      * @return the store object as a Long
      */
     public static long toLong(Object object) {
         if (object instanceof Long) {
             return ((Long) object).longValue();
         } else if (object == null || object instanceof RemoveProperty) {
             return 0;
         }
         return Long.parseLong(toString(object), ENCODING_BASE);
     }
 
     /**
      * @param object
      * @return the store object as a {@link Calendar}
      * @throws ParseException
      */
     @Deprecated
     public static Calendar toCalendar(Object object) throws ParseException {
         if (object instanceof Calendar) {
             return (Calendar) object;
         } else if (object == null || object instanceof RemoveProperty) {
             return null;
         }
         final SimpleDateFormat sdf = new SimpleDateFormat(ISO8601_JCR_PATTERN, Locale.ROOT);
         final Date date = sdf.parse(toString(object));
         final Calendar c = Calendar.getInstance();
         c.setTime(date);
         return c;
     }
 
     /**
      * @param path
      * @param child
      * @return create a new path by appending the child to the parent path.
      */
     public static String newPath(String path, String child) {
         if (!path.endsWith("/")) {
             if (!child.startsWith("/")) {
                 return path + "/" + child;
             } else {
                 return path + child;
 
             }
         } else {
             if (!child.startsWith("/")) {
                 return path + child;
             } else {
                 return path + child.substring(1);
             }
         }
     }
 
     /**
      * @param <T>
      * @param setting
      * @param defaultValue
      * @return gets a setting of type <T> usign the default value if null.
      */
     @SuppressWarnings("unchecked")
     public static <T> T getSetting(Object setting, T defaultValue) {
         if (setting != null) {
             return (T) setting;
         }
         return defaultValue;
     }
 
     /**
      * @param id
      * @return perform a 3 level shard of the path using base^^2 as the width of
      *         the shard where base is the cardinality of the encoding of the
      *         ID.
      */
     // TODO: There is no reason to use this method in sparse (or very little),
     // check usage.
     // For instance the SparsePrincipal uses it.
     public static String shardPath(String id) {
         String hash = insecureHash(id);
         return hash.substring(0, 2) + "/" + hash.substring(2, 4) + "/" + hash.substring(4, 6) + "/"
                 + id;
     }
 
     /**
      * @param string
      * @return an escaped version of the supplied string suitable for using in a
      *         stored array. The implemenation is not that efficient.
      */
     // TODO: Unit test
     public static String arrayEscape(String string) {
         string = string.replaceAll("%", "%1");
         string = string.replaceAll(",", "%2");
         return string;
     }
 
     /**
      * @param string
      * @return reverse the arrayEscape operation.
      */
     // TODO: Unit test
     public static String arrayUnEscape(String string) {
         string = string.replaceAll("%2", ",");
         string = string.replaceAll("%1", "%");
         return string;
     }
 
     /**
      * @param object
      * @return null or the store object converted to a string[]
      */
     // TODO: Unit test
     @Deprecated
     public static String[] toStringArray(Object object) {
         if ( object instanceof String[] ) {
             return (String[]) object;
         } else if (object == null) {
                 return null;
         } else {
             String[] v = StringUtils.split(StorageClientUtils.toString(object), ',');
             for (int i = 0; i < v.length; i++) {
                 v[i] = StorageClientUtils.arrayUnEscape(v[i]);
             }
             return v;
         }
     }
 
     /**
      * @param object
      * @return null or the store object converted to a string[]
      * @throws ParseException
      */
     // TODO: Unit test
     @Deprecated
     public static Calendar[] toCalendarArray(Object object) throws ParseException {
         if ( object instanceof Calendar[] ) {
             return (Calendar[]) object;
         } else if (object == null) {
             return null;
         } else {
             String[] v = StringUtils.split(StorageClientUtils.toString(object), ',');
             Calendar[] c = new Calendar[v.length];
             for (int i = 0; i < v.length; i++) {
                 c[i] = toCalendar(arrayUnEscape(v[i]));
             }
             return c;
         }
     }
 
     /**
      * @param parameterValues
      * @return ensure that a string[] is not null after conversion from store.
      *         Use this to make iterators easier on stored arrays.
      */
     // TODO: Unit test
     public static String[] nonNullStringArray(String[] parameterValues) {
         if (parameterValues == null) {
             return new String[0];
         }
         return parameterValues;
     }
 
 
 
     /**
      * Adapt an object to a session. I haven't used typing here becuase I don't
      * want to bind to the Jars in question and create dependencies.
      * 
      * @param source
      *            the input object that the method
      * @return
      */
     public static Session adaptToSession(Object source) {
         if (source instanceof SessionAdaptable) {
             return ((SessionAdaptable) source).getSession();
         } else {
             // assume this is a JCR session of someform, in which case there
             // should be a SparseUserManager
             Object userManager = safeMethod(source, "getUserManager", new Object[0], new Class[0]);
             if (userManager != null) {
                 return (Session) safeMethod(userManager, "getSession", new Object[0], new Class[0]);
             }
             return null;
         }
     }
 
     private static Object safeMethod(Object target, String methodName, Object[] args,
             @SuppressWarnings("rawtypes") Class[] argsTypes) {
         if (target != null) {
             try {
                 Method m = target.getClass().getMethod(methodName, argsTypes);
                 if (!m.isAccessible()) {
                     m.setAccessible(true);
                 }
                 return m.invoke(target, args);
             } catch (Throwable e) {
                 LOGGER.info("Failed to invoke method " + methodName + " " + target, e);
             }
         }
         return null;
     }
 
     @Deprecated
     public static boolean toBoolean(Object property) {
         return "true".equals(StorageClientUtils.toString(property));
     }
 
     /**
      * Delete an entire tree starting from the deepest part of the tree and
      * working back up. Will stop the moment a permission denied is encountered
      * either for read or for delete.
      *
      * @param contentManager
      * @param path
      * @throws AccessDeniedException
      * @throws StorageClientException
      */
     public static void deleteTree(ContentManager contentManager, String path)
             throws AccessDeniedException, StorageClientException {
         Content content = contentManager.get(path);
         if (content != null) {
             for (String childPath : content.listChildPaths()) {
                deleteTree(contentManager, childPath);
             }
         }
         contentManager.delete(path);
     }
 
 }
