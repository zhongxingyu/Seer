 package org.smartly.packages.mongo.impl.i18n;
 
 import com.mongodb.DB;
 import com.mongodb.DBObject;
 import org.smartly.Smartly;
 import org.smartly.commons.logging.util.LoggingUtils;
 import org.smartly.commons.util.ExceptionUtils;
 import org.smartly.commons.util.FormatUtils;
 import org.smartly.commons.util.StringUtils;
 import org.smartly.packages.mongo.impl.IMongoConstants;
 import org.smartly.packages.mongo.impl.db.GenericMongoService;
 import org.smartly.packages.mongo.impl.util.MongoUtils;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Quick access to locale dictionaries.
  * <p/>
  * Works with internal cache for values.
  */
 public final class DictionaryManager {
 
     private DictionaryManager() {
 
     }
 
     private static final Map<String, String> _cache = Collections.synchronizedMap(new HashMap<String, String>());
 
     private static final String[] LOCALFIELDS = new String[]{
             IMongoConstants.VALUE
     };
 
    private static String getKey(final String lang, final String collection, final String id) {
        return StringUtils.concatDot(lang, collection, id);
     }
 
     private static void add(final String key, final String value) {
         synchronized (_cache) {
             _cache.put(key, value);
         }
     }
 
     private static String getFromDB(final String lang, final String collection, final String id) throws Exception {
         try {
             if (null != _DATABASE) {
                 final GenericMongoService srvc = new GenericMongoService(
                         _DATABASE,
                         collection,
                         Smartly.getLanguages());
                 final DBObject item = srvc.findById(id);
                 if (null != item) {
                     srvc.localize(item, lang, LOCALFIELDS);
                     return MongoUtils.getString(item, IMongoConstants.VALUE);
                 }
             } else {
                 LoggingUtils.getLogger(DictionaryManager.class).warning("DictionaryManager not initialized!");
             }
             return null;
         } catch (Throwable t) {
             throw new Exception(FormatUtils.format("Error: '{0}'", ExceptionUtils.getRealMessage(t)));
         }
     }
 
     private static String getValue(final String lang, final String collection, final String id) {
        final String key = getKey(lang, collection, id);
         try {
             if (!_cache.containsKey(key)) {
                 add(key, getFromDB(lang, collection, id));
             }
             return _cache.get(key);
         } catch (Throwable t) {
             return ExceptionUtils.getRealMessage(t);
         }
     }
 
     // --------------------------------------------------------------------
     //               S T A T I C
     // --------------------------------------------------------------------
 
     private static DB _DATABASE = null;
 
     public static void init(DB database){
         _DATABASE = database;
     }
 
     public static String get(final String lang, final String collection, final String id) {
         return get(lang, collection, id, "");
     }
 
     public static String get(final String lang, final String collection, final String id, final String defaultValue) {
         final String result = getValue(lang, collection, id);
         return null != result ? result : defaultValue;
     }
 
 }
