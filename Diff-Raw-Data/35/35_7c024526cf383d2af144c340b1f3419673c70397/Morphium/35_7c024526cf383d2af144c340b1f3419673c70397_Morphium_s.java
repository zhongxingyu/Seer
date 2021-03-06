 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.caluga.morphium;
 
 import com.mongodb.*;
 import de.caluga.morphium.annotations.*;
 import de.caluga.morphium.annotations.caching.Cache;
 import de.caluga.morphium.annotations.caching.NoCache;
 import de.caluga.morphium.annotations.lifecycle.*;
 import de.caluga.morphium.annotations.security.NoProtection;
 import de.caluga.morphium.cache.CacheElement;
 import de.caluga.morphium.cache.CacheHousekeeper;
 import de.caluga.morphium.secure.MongoSecurityException;
 import de.caluga.morphium.secure.MongoSecurityManager;
 import de.caluga.morphium.secure.Permission;
 import net.sf.cglib.proxy.Enhancer;
 import net.sf.cglib.proxy.MethodInterceptor;
 import net.sf.cglib.proxy.MethodProxy;
 import org.apache.log4j.Logger;
 import org.bson.types.ObjectId;
 
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.*;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * This is the single access point for accessing MongoDB. This should
  *
  * @author stephan
  */
 public class Morphium {
 
     /**
      * singleton is usually not a good idea in j2ee-Context, but as we did it on
      * several places in the Application it's the easiest way Usage:
      * <code>
      * MorphiumConfig cfg=new MorphiumConfig("testdb",false,false,10,5000,2500);
      * cfg.addAddress("localhost",27017);
      * Morphium.config=cfg;
      * Morphium l=Morphium.get();
      * if (l==null) {
      * System.out.println("Error establishing connection!");
      * System.exit(1);
      * }
      * </code>
      *
      * @see MorphiumConfig
      */
     private final static Logger logger = Logger.getLogger(Morphium.class);
     private MorphiumConfig config;
     private Mongo mongo;
     private DB database;
     private ThreadPoolExecutor writers = new ThreadPoolExecutor(1, 1,
             1000L, TimeUnit.MILLISECONDS,
             new LinkedBlockingQueue<Runnable>());
     //Cache by Type, query String -> CacheElement (contains list etc)
     private Hashtable<Class<? extends Object>, Hashtable<String, CacheElement>> cache;
     private final Map<StatisticKeys, StatisticValue> stats;
     private Map<Class<?>, Map<Class<? extends Annotation>, Method>> lifeCycleMethods;
     /**
      * String Representing current user - needs to be set by Application
      */
     private String currentUser;
     private CacheHousekeeper cacheHousekeeper;
 
     private Vector<MorphiumStorageListener> listeners;
     private Vector<Thread> privileged;
     private Vector<ShutdownListener> shutDownListeners;
 
     public MorphiumConfig getConfig() {
         return config;
     }
 //    private boolean securityEnabled = false;
 
     /**
      * init the MongoDbLayer. Uses Morphium-Configuration Object for Configuration.
      * Needs to be set before use or RuntimeException is thrown!
      * all logging is done in INFO level
      *
      * @see MorphiumConfig
      */
     public Morphium(MorphiumConfig cfg) {
         if (cfg == null) {
             throw new RuntimeException("Please specify configuration!");
         }
         config = cfg;
         privileged = new Vector<Thread>();
         shutDownListeners = new Vector<ShutdownListener>();
         listeners = new Vector<MorphiumStorageListener>();
         cache = new Hashtable<Class<? extends Object>, Hashtable<String, CacheElement>>();
         stats = new Hashtable<StatisticKeys, StatisticValue>();
         lifeCycleMethods = new Hashtable<Class<?>, Map<Class<? extends Annotation>, Method>>();
         for (StatisticKeys k : StatisticKeys.values()) {
             stats.put(k, new StatisticValue());
         }
 
 
         //dummyUser.setGroupIds();
         MongoOptions o = new MongoOptions();
         o.autoConnectRetry = true;
         o.fsync = true;
         o.connectTimeout = 2500;
         o.connectionsPerHost = config.getMaxConnections();
         o.socketKeepAlive = true;
         o.threadsAllowedToBlockForConnectionMultiplier = 5;
         o.safe = false;
 
 
         if (config.getAdr().isEmpty()) {
             throw new RuntimeException("Error - no server address specified!");
         }
         switch (config.getMode()) {
             case REPLICASET:
                 if (config.getAdr().size() < 2) {
 
                     throw new RuntimeException("at least 2 Server Adresses needed for MongoDB in ReplicaSet Mode!");
                 }
                 mongo = new Mongo(config.getAdr(), o);
                 break;
             case PAIRED:
                 throw new RuntimeException("PAIRED Mode not available anymore!!!!");
 //                if (config.getAdr().size() != 2) {
 //                    morphia = null;
 //                    dataStore = null;
 //                    throw new RuntimeException("2 Server Adresses needed for MongoDB in Paired Mode!");
 //                }
 //
 //                morphium = new Mongo(config.getAdr().get(0), config.getAdr().get(1), o);
 //                break;
             case SINGLE:
             default:
                 if (config.getAdr().size() > 1) {
 //                    Logger.getLogger(Morphium.class.getName()).warning("WARNING: ignoring additional server Adresses only using 1st!");
                 }
                 mongo = new Mongo(config.getAdr().get(0), o);
                 break;
         }
 
         database = mongo.getDB(config.getDatabase());
         if (config.getMongoLogin() != null) {
             if (!database.authenticate(config.getMongoLogin(), config.getMongoPassword().toCharArray())) {
                 throw new RuntimeException("Authentication failed!");
             }
         }
         int cnt = database.getCollection("system.indexes").find().count(); //test connection
 
         if (config.getConfigManager() == null) {
             config.setConfigManager(new ConfigManager(this));
         }
         cacheHousekeeper = new CacheHousekeeper(this, 5000, config.getGlobalCacheValidTime());
         cacheHousekeeper.start();
 
 
         logger.info("Initialization successful...");
 
     }
 
     public void addListener(MorphiumStorageListener lst) {
         listeners.add(lst);
     }
 
     public void removeListener(MorphiumStorageListener lst) {
         listeners.remove(lst);
     }
 
 
     public Mongo getMongo() {
         return mongo;
     }
 
     public DB getDatabase() {
         return database;
     }
 
 
     public ConfigManager getConfigManager() {
         return config.getConfigManager();
     }
 
     /**
      * search for objects similar to template concerning all given fields.
      * If no fields are specified, all NON Null-Fields are taken into account
      * if specified, field might also be null
      *
      * @param template
      * @param fields
      * @param <T>
      * @return
      */
     public <T> List<T> findByTemplate(T template, String... fields) {
         Class cls = template.getClass();
         List<String> flds = new ArrayList<String>();
         if (fields.length > 0) {
             flds.addAll(Arrays.asList(fields));
         } else {
             flds = getFields(cls);
         }
         Query<T> q = createQueryFor(cls);
         for (String f : flds) {
             try {
                 q.f(f).eq(getValue(template, f));
             } catch (IllegalAccessException e) {
                 logger.error("Could not read field " + f + " of object " + cls.getName());
             }
         }
         return q.asList();
     }
 
     public void unset(Object toSet, Enum field) {
         unset(toSet, field.name());
     }
 
     /**
      * Un-setting a value in an existing mongo collection entry - no reading necessary. Object is altered in place
      * db.collection.update({"_id":toSet.id},{$unset:{field:1}}
      * <b>attention</b>: this alteres the given object toSet in a similar way
      *
      * @param toSet: object to set the value in (or better - the corresponding entry in mongo)
      * @param field: field to remove from document
      */
     public void unset(Object toSet, String field) {
         if (toSet == null) throw new RuntimeException("Cannot update null!");
         if (getId(toSet) == null) {
             logger.info("just storing object as it is new...");
             store(toSet);
         }
         Class cls = toSet.getClass();
         String coll = config.getMapper().getCollectionName(cls);
         BasicDBObject query = new BasicDBObject();
         query.put("_id", getId(toSet));
         Field f = getField(cls, field);
         if (f == null) {
             throw new RuntimeException("Unknown field: " + field);
         }
         String fieldName = getFieldName(cls, field);
 
         BasicDBObject update = new BasicDBObject("$unset", new BasicDBObject(fieldName, 1));
         WriteConcern wc = getWriteConcernForClass(toSet.getClass());
         if (wc == null) {
             database.getCollection(coll).update(query, update);
         } else {
             database.getCollection(coll).update(query, update, false, false, wc);
         }
 
         clearCacheIfNecessary(cls);
         try {
             f.set(toSet, null);
         } catch (IllegalAccessException e) {
             //May happen, if null is not allowed for example
         }
     }
 
 
     private void clearCacheIfNecessary(Class cls) {
         Cache c = getAnnotationFromHierarchy(cls, Cache.class); //cls.getAnnotation(Cache.class);
         if (c != null) {
             if (c.clearOnWrite()) {
                 clearCachefor(cls);
             }
         }
     }
 
     private DBObject simplifyQueryObject(DBObject q) {
         if (q.keySet().size() == 1 && q.get("$and") != null) {
             BasicDBObject ret = new BasicDBObject();
             BasicDBList lst = (BasicDBList) q.get("$and");
             for (Object o : lst) {
                 if (o instanceof DBObject) {
                     ret.putAll(((DBObject) o));
                 } else if (o instanceof Map) {
                     ret.putAll(((Map) o));
                 } else {
                     //something we cannot handle
                     return q;
                 }
             }
             return ret;
         }
         return q;
     }
 
     public void set(Query<?> query, Enum field, Object val) {
         set(query, field.name(), val);
     }
 
     public void set(Query<?> query, String field, Object val) {
         set(query, field, val, false, false);
     }
 
     public void setEnum(Query<?> query, Map<Enum, Object> values, boolean insertIfNotExist, boolean multiple) {
         HashMap<String, Object> toSet = new HashMap<String, Object>();
         for (Enum k : values.keySet()) {
             toSet.put(k.name(), values.get(k));
         }
         set(query, toSet, insertIfNotExist, multiple);
     }
 
     public void push(Query<?> query, Enum field, Object value) {
         pushPull(true, query, field.name(), value, false, true);
     }
 
     public void pull(Query<?> query, Enum field, Object value) {
         pushPull(false, query, field.name(), value, false, true);
     }
 
     public void push(Query<?> query, String field, Object value) {
         pushPull(true, query, field, value, false, true);
     }
 
     public void pull(Query<?> query, String field, Object value) {
         pushPull(false, query, field, value, false, true);
     }
 
 
     public void push(Query<?> query, Enum field, Object value, boolean insertIfNotExist, boolean multiple) {
         pushPull(true, query, field.name(), value, insertIfNotExist, multiple);
     }
 
     public void pull(Query<?> query, Enum field, Object value, boolean insertIfNotExist, boolean multiple) {
         pushPull(false, query, field.name(), value, insertIfNotExist, multiple);
     }
 
     public void pushAll(Query<?> query, Enum field, List<Object> value, boolean insertIfNotExist, boolean multiple) {
         pushPullAll(true, query, field.name(), value, insertIfNotExist, multiple);
     }
 
     public void pullAll(Query<?> query, Enum field, List<Object> value, boolean insertIfNotExist, boolean multiple) {
         pushPullAll(false, query, field.name(), value, insertIfNotExist, multiple);
     }
 
 
     public void push(Query<?> query, String field, Object value, boolean insertIfNotExist, boolean multiple) {
         pushPull(true, query, field, value, insertIfNotExist, multiple);
     }
 
     public void pull(Query<?> query, String field, Object value, boolean insertIfNotExist, boolean multiple) {
         pushPull(false, query, field, value, insertIfNotExist, multiple);
     }
 
     public void pushAll(Query<?> query, String field, List<Object> value, boolean insertIfNotExist, boolean multiple) {
         pushPullAll(true, query, field, value, insertIfNotExist, multiple);
     }
 
     public void pullAll(Query<?> query, String field, List<Object> value, boolean insertIfNotExist, boolean multiple) {
         pushPull(false, query, field, value, insertIfNotExist, multiple);
     }
 
 
     private void pushPull(boolean push, Query<?> query, String field, Object value, boolean insertIfNotExist, boolean multiple) {
         Class<?> cls = query.getType();
         String coll = config.getMapper().getCollectionName(cls);
 
         DBObject qobj = query.toQueryObject();
         if (insertIfNotExist) {
             qobj = simplifyQueryObject(qobj);
         }
         field = config.getMapper().getFieldName(cls, field);
         BasicDBObject set = new BasicDBObject(field, value);
         BasicDBObject update = new BasicDBObject(push ? "$push" : "$pull", set);
 
         WriteConcern wc = getWriteConcernForClass(cls);
         if (wc == null) {
             database.getCollection(coll).update(qobj, update, insertIfNotExist, multiple);
         } else {
             database.getCollection(coll).update(qobj, update, insertIfNotExist, multiple, wc);
         }
         clearCacheIfNecessary(cls);
     }
 
     private void pushPullAll(boolean push, Query<?> query, String field, List<Object> value, boolean insertIfNotExist, boolean multiple) {
         Class<?> cls = query.getType();
         String coll = config.getMapper().getCollectionName(cls);
 
         BasicDBList dbl = new BasicDBList();
         dbl.addAll(value);
 
         DBObject qobj = query.toQueryObject();
         if (insertIfNotExist) {
             qobj = simplifyQueryObject(qobj);
         }
         field = config.getMapper().getFieldName(cls, field);
         BasicDBObject set = new BasicDBObject(field, value);
         BasicDBObject update = new BasicDBObject(push ? "$pushAll" : "$pullAll", set);
         WriteConcern wc = getWriteConcernForClass(cls);
         if (wc == null) {
             database.getCollection(coll).update(qobj, update, insertIfNotExist, multiple);
         } else {
             database.getCollection(coll).update(qobj, update, insertIfNotExist, multiple, wc);
         }
         clearCacheIfNecessary(cls);
     }
 
     /**
      * will change an entry in mongodb-collection corresponding to given class object
      * if query is too complex, upsert might not work!
      * Upsert should consist of single and-queries, which will be used to generate the object to create, unless
      * it already exists. look at Mongodb-query documentation as well
      *
      * @param query            - query to specify which objects should be set
      * @param values           - map fieldName->Value, which values are to be set!
      * @param insertIfNotExist - insert, if it does not exist (query needs to be simple!)
      * @param multiple         - update several documents, if false, only first hit will be updated
      */
     public void set(Query<?> query, Map<String, Object> values, boolean insertIfNotExist, boolean multiple) {
         Class<?> cls = query.getType();
         String coll = config.getMapper().getCollectionName(cls);
         BasicDBObject toSet = new BasicDBObject();
         for (String f : values.keySet()) {
             String fieldName = getFieldName(cls, f);
             toSet.put(fieldName, values.get(f));
         }
         DBObject qobj = query.toQueryObject();
         if (insertIfNotExist) {
             qobj = simplifyQueryObject(qobj);
         }
         BasicDBObject update = new BasicDBObject("$set", toSet);
         WriteConcern wc = getWriteConcernForClass(cls);
         if (wc == null) {
             database.getCollection(coll).update(qobj, update, insertIfNotExist, multiple);
         } else {
             database.getCollection(coll).update(qobj, update, insertIfNotExist, multiple, wc);
         }
         clearCacheIfNecessary(cls);
     }
 
     /**
      * will change an entry in mongodb-collection corresponding to given class object
      * if query is too complex, upsert might not work!
      * Upsert should consist of single and-queries, which will be used to generate the object to create, unless
      * it already exists. look at Mongodb-query documentation as well
      *
      * @param query            - query to specify which objects should be set
      * @param field            - field to set
      * @param val              - value to set
      * @param insertIfNotExist - insert, if it does not exist (query needs to be simple!)
      * @param multiple         - update several documents, if false, only first hit will be updated
      */
     public void set(Query<?> query, String field, Object val, boolean insertIfNotExist, boolean multiple) {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put(field, val);
         set(query, map, insertIfNotExist, multiple);
     }
 
     public void dec(Query<?> query, Enum field, int amount, boolean insertIfNotExist, boolean multiple) {
         dec(query, field.name(), amount, insertIfNotExist, multiple);
     }
 
     public void dec(Query<?> query, String field, int amount, boolean insertIfNotExist, boolean multiple) {
         inc(query, field, -amount, insertIfNotExist, multiple);
     }
 
     public void dec(Query<?> query, String field, int amount) {
         inc(query, field, -amount, false, false);
     }
 
     public void dec(Query<?> query, Enum field, int amount) {
         inc(query, field, -amount, false, false);
     }
 
     public void inc(Query<?> query, String field, int amount) {
         inc(query, field, amount, false, false);
     }
 
     public void inc(Query<?> query, Enum field, int amount) {
         inc(query, field, amount, false, false);
     }
 
     public void inc(Query<?> query, Enum field, int amount, boolean insertIfNotExist, boolean multiple) {
         inc(query, field.name(), amount, insertIfNotExist, multiple);
     }
 
     public void inc(Query<?> query, String field, int amount, boolean insertIfNotExist, boolean multiple) {
         Class<?> cls = query.getType();
         String coll = config.getMapper().getCollectionName(cls);
         String fieldName = getFieldName(cls, field);
         BasicDBObject update = new BasicDBObject("$inc", new BasicDBObject(fieldName, amount));
         DBObject qobj = query.toQueryObject();
         if (insertIfNotExist) {
             qobj = simplifyQueryObject(qobj);
         }
         WriteConcern wc = getWriteConcernForClass(cls);
         if (wc == null) {
             database.getCollection(coll).update(qobj, update, insertIfNotExist, multiple);
         } else {
             database.getCollection(coll).update(qobj, update, insertIfNotExist, multiple, wc);
         }
         clearCacheIfNecessary(cls);
     }
 
 
     public void set(Object toSet, Enum field, Object value) {
         set(toSet, field.name(), value);
     }
 
     /**
      * setting a value in an existing mongo collection entry - no reading necessary. Object is altered in place
      * db.collection.update({"_id":toSet.id},{$set:{field:value}}
      * <b>attention</b>: this alteres the given object toSet in a similar way
      *
      * @param toSet: object to set the value in (or better - the corresponding entry in mongo)
      * @param field: the field to change
      * @param value: the value to set
      */
     public void set(Object toSet, String field, Object value) {
         if (toSet == null) throw new RuntimeException("Cannot update null!");
         if (getId(toSet) == null) {
             logger.info("just storing object as it is new...");
             storeNoCache(toSet);
         }
         Class cls = toSet.getClass();
         String coll = config.getMapper().getCollectionName(cls);
         BasicDBObject query = new BasicDBObject();
         query.put("_id", getId(toSet));
         Field f = getField(cls, field);
         if (f == null) {
             throw new RuntimeException("Unknown field: " + field);
         }
         String fieldName = getFieldName(cls, field);
 
         BasicDBObject update = new BasicDBObject("$set", new BasicDBObject(fieldName, value));
         WriteConcern wc = getWriteConcernForClass(toSet.getClass());
         if (wc == null) {
             database.getCollection(coll).update(query, update);
         } else {
             database.getCollection(coll).update(query, update, false, false, wc);
         }
 
         clearCacheIfNecessary(cls);
         try {
             f.set(toSet, value);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
 
     }
 
     /**
      * decreasing a value of a given object
      * calles <code>inc(toDec,field,-amount);</code>
      */
     public void dec(Object toDec, String field, int amount) {
         inc(toDec, field, -amount);
     }
 
     /**
      * Increases a value in an existing mongo collection entry - no reading necessary. Object is altered in place
      * db.collection.update({"_id":toInc.id},{$inc:{field:amount}}
      * <b>attention</b>: this alteres the given object toSet in a similar way
      *
      * @param toInc:  object to set the value in (or better - the corresponding entry in mongo)
      * @param field:  the field to change
      * @param amount: the value to set
      */
     public void inc(Object toInc, String field, int amount) {
         if (toInc == null) throw new RuntimeException("Cannot update null!");
         if (getId(toInc) == null) {
             logger.info("just storing object as it is new...");
             storeNoCache(toInc);
         }
         Class cls = toInc.getClass();
         String coll = config.getMapper().getCollectionName(cls);
         BasicDBObject query = new BasicDBObject();
         query.put("_id", getId(toInc));
         Field f = getField(cls, field);
         if (f == null) {
             throw new RuntimeException("Unknown field: " + field);
         }
         String fieldName = getFieldName(cls, field);
 
         BasicDBObject update = new BasicDBObject("$inc", new BasicDBObject(fieldName, amount));
         WriteConcern wc = getWriteConcernForClass(toInc.getClass());
         if (wc == null) {
             database.getCollection(coll).update(query, update);
         } else {
             database.getCollection(coll).update(query, update, false, false, wc);
         }
 
         clearCacheIfNecessary(cls);
 
         //TODO: check inf necessary
         if (f.getType().equals(Integer.class) || f.getType().equals(int.class)) {
             try {
                 f.set(toInc, ((Integer) f.get(toInc)) + (int) amount);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
         } else if (f.getType().equals(Double.class) || f.getType().equals(double.class)) {
             try {
                 f.set(toInc, ((Double) f.get(toInc)) + amount);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
         } else if (f.getType().equals(Float.class) | f.getType().equals(float.class)) {
             try {
                 f.set(toInc, ((Float) f.get(toInc)) + (float) amount);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
         } else if (f.getType().equals(Long.class) || f.getType().equals(long.class)) {
             try {
                 f.set(toInc, ((Long) f.get(toInc)) + (long) amount);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
         } else {
             logger.error("Could not set increased value - unsupported type " + cls.getName());
         }
 
 
     }
 
 
     /**
      * adds some list of objects to the cache manually...
      * is being used internally, and should be used with care
      *
      * @param k    - Key, usually the mongodb query string
      * @param type - class type
      * @param ret  - list of results
      * @param <T>  - Type of record
      */
     public <T extends Object> void addToCache(String k, Class<? extends Object> type, List<T> ret) {
         if (k == null) {
             return;
         }
 
 
         CacheElement e = new CacheElement(ret);
         e.setLru(System.currentTimeMillis());
         Hashtable<Class<? extends Object>, Hashtable<String, CacheElement>> cl = (Hashtable<Class<? extends Object>, Hashtable<String, CacheElement>>) cache.clone();
         if (cl.get(type) == null) {
             cl.put(type, new Hashtable<String, CacheElement>());
         }
         cl.get(type).put(k, e);
 
         //atomar execution of this operand - no synchronization needed
         cache = cl;
 
     }
 
     protected void setPrivilegedThread(Thread thr) {
 
     }
 
 
     protected void inc(StatisticKeys k) {
         stats.get(k).inc();
     }
 
 
     public String toJsonString(Object o) {
         return config.getMapper().marshall(o).toString();
     }
 
 
     public int writeBufferCount() {
         return writers.getQueue().size();
     }
 
 
     public String getCacheKey(DBObject qo, Map<String, Integer> sort, int skip, int limit) {
         StringBuffer b = new StringBuffer();
         b.append(qo.toString());
         b.append(" l:");
         b.append(limit);
         b.append(" s:");
         b.append(skip);
         if (sort != null) {
             b.append(" sort:");
             b.append(new BasicDBObject(sort).toString());
         }
         return b.toString();
     }
 
     /**
      * create unique cache key for queries, also honoring skip & limit and sorting
      *
      * @param q
      * @return
      */
     public String getCacheKey(Query q) {
         return getCacheKey(q.toQueryObject(), q.getOrder(), q.getSkip(), q.getLimit());
     }
 
 
     private void storeNoCacheUsingFields(Object ent, String... fields) {
         ObjectId id = getId(ent);
         if (ent == null) return;
         if (id == null) {
             //new object - update not working
             logger.warn("trying to partially update new object - storing it in full!");
             storeNoCache(ent);
             return;
         }
         Class<?> type = ent.getClass();
         firePreStoreEvent(ent);
         inc(StatisticKeys.WRITES);
         DBObject find = new BasicDBObject();
 
         find.put("_id", id);
         DBObject update = new BasicDBObject();
         for (String f : fields) {
             try {
                 Object value = getValue(ent, f);
                 if (isAnnotationPresentInHierarchy(value.getClass(), Entity.class)) {
                     value = config.getMapper().marshall(value);
                 }
                 update.put(f, value);
 
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
         }
 
         StoreLastChange t = getAnnotationFromHierarchy(type, StoreLastChange.class); //(StoreLastChange) type.getAnnotation(StoreLastChange.class);
         if (t != null) {
             List<String> lst = config.getMapper().getFields(ent.getClass(), LastChange.class);
 
             long now = System.currentTimeMillis();
             for (String ctf : lst) {
                 Field f = getField(type, ctf);
                 if (f != null) {
                     try {
                         f.set(ent, now);
                     } catch (IllegalAccessException e) {
                         logger.error("Could not set modification time", e);
 
                     }
                 }
                 update.put(ctf, now);
             }
             lst = config.getMapper().getFields(ent.getClass(), LastChangeBy.class);
             if (lst != null && lst.size() != 0) {
                 for (String ctf : lst) {
 
                     Field f = getField(type, ctf);
                     if (f != null) {
                         try {
                             f.set(ent, config.getSecurityMgr().getCurrentUserId());
                         } catch (IllegalAccessException e) {
                             logger.error("Could not set changed by", e);
                         }
                     }
                     update.put(ctf, config.getSecurityMgr().getCurrentUserId());
                 }
             }
         }
 
 
         update = new BasicDBObject("$set", update);
         //no Writeconcern possible... :-(
         database.getCollection(config.getMapper().getCollectionName(ent.getClass())).findAndModify(find, update);
 
         firePostStoreEvent(ent);
     }
 
     /**
      * updating an enty in DB without sending the whole entity
      * only transfers the fields to be changed / set
      *
      * @param ent
      * @param fields
      */
     public void updateUsingFields(final Object ent, final String... fields) {
         if (ent == null) return;
         if (fields.length == 0) return; //not doing an update - no change
         if (!isAnnotationPresentInHierarchy(ent.getClass(), NoProtection.class)) {
             if (getId(ent) == null) {
                 if (accessDenied(ent, Permission.INSERT)) {
                     throw new SecurityException("Insert of new Object denied!");
                 }
             } else {
                 if (accessDenied(ent, Permission.UPDATE)) {
                     throw new SecurityException("Update of Object denied!");
                 }
             }
         }
 
         if (isAnnotationPresentInHierarchy(ent.getClass(), NoCache.class)) {
             storeNoCacheUsingFields(ent, fields);
             return;
         }
 
         Cache cc = getAnnotationFromHierarchy(ent.getClass(), Cache.class); //ent.getClass().getAnnotation(Cache.class);
         if (cc != null) {
             if (cc.writeCache()) {
                 writers.execute(new Runnable() {
                     @Override
                     public void run() {
                         storeNoCacheUsingFields(ent, fields);
                     }
                 });
                 inc(StatisticKeys.WRITES_CACHED);
 
             } else {
                 storeNoCacheUsingFields(ent, fields);
             }
         } else {
             storeNoCacheUsingFields(ent, fields);
         }
 
 
     }
 
     /**
      * returns annotations, even if in class hierarchy or
      * lazyloading proxy
      *
      * @param cls
      * @return
      */
     public <T extends Annotation> T getAnnotationFromHierarchy(Class<?> cls, Class<T> anCls) {
         if (cls.getName().contains("$$EnhancerByCGLIB$$")) {
             try {
                 cls = Class.forName(cls.getName().substring(0, cls.getName().indexOf("$$")));
 //                return cls.getAnnotation(anCls);
             } catch (Exception e) {
                 //TODO: Implement Handling
                 throw new RuntimeException(e);
             }
         }
         if (cls.isAnnotationPresent(anCls)) {
             return cls.getAnnotation(anCls);
         }
         //class hierarchy?
         Class<?> z = cls;
         while (!z.equals(Object.class)) {
             if (z.isAnnotationPresent(anCls)) {
                 return z.getAnnotation(anCls);
             }
             z = z.getSuperclass();
             if (z == null) break;
         }
         return null;
     }
 
     public <T extends Annotation> boolean isAnnotationPresentInHierarchy(Class<?> cls, Class<T> anCls) {
         return getAnnotationFromHierarchy(cls, anCls) != null;
     }
 
 
     public void callLifecycleMethod(Class<? extends Annotation> type, Object on) {
         if (on == null) return;
         //No synchronized block - might cause the methods to be put twice into the
         //hashtabel - but for performance reasons, it's ok...
         Class<?> cls = on.getClass();
         //No Lifecycle annotation - no method calling
         if (!isAnnotationPresentInHierarchy(cls, Lifecycle.class)) {//cls.isAnnotationPresent(Lifecycle.class)) {
             return;
         }
         //Already stored - should not change during runtime
         if (lifeCycleMethods.get(cls) != null) {
             if (lifeCycleMethods.get(cls).get(type) != null) {
                 try {
                     lifeCycleMethods.get(cls).get(type).invoke(on);
                 } catch (IllegalAccessException e) {
                     throw new RuntimeException(e);
                 } catch (InvocationTargetException e) {
                     throw new RuntimeException(e);
                 }
             }
             return;
         }
 
         Map<Class<? extends Annotation>, Method> methods = new HashMap<Class<? extends Annotation>, Method>();
         //Methods must be public
         for (Method m : cls.getMethods()) {
             for (Annotation a : m.getAnnotations()) {
                 methods.put(a.annotationType(), m);
             }
         }
         lifeCycleMethods.put(cls, methods);
         if (methods.get(type) != null) {
             try {
                 methods.get(type).invoke(on);
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             } catch (InvocationTargetException e) {
                 throw new RuntimeException(e);
             }
         }
     }
 
     public <T> T reread(T o) {
         if (o == null) throw new RuntimeException("Cannot re read null!");
         ObjectId id = getId(o);
         List<String> flds = config.getMapper().getFields(o.getClass(), Id.class);
         if (flds == null || flds.size() > 1) {
             throw new RuntimeException("error finding ID ");
         }
 
         String fld = flds.get(0);
 
         List<T> ret = (List<T>) findByField(o.getClass(), fld, id);
         if (ret == null || ret.size() == 0) {
             return null;
         }
         return ret.get(0);
     }
 
     private void firePreStoreEvent(Object o) {
         if (o == null) return;
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.preStore(o);
         }
         callLifecycleMethod(PreStore.class, o);
 
     }
 
     private void firePostStoreEvent(Object o) {
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.postStore(o);
         }
         callLifecycleMethod(PostStore.class, o);
         //existing object  => store last Access, if needed
 
     }
 
     private void firePreDropEvent(Class cls) {
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.preDrop(cls);
         }
 
     }
 
     private void firePostDropEvent(Class cls) {
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.postDrop(cls);
         }
     }
 
     private void firePostRemoveEvent(Object o) {
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.postRemove(o);
         }
         callLifecycleMethod(PostRemove.class, o);
     }
 
     private void firePostRemoveEvent(Query q) {
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.postRemove(q);
         }
         //TODO: FIX - Cannot call lifecycle method here
 
     }
 
     private void firePreRemoveEvent(Object o) {
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.preDelete(o);
         }
         callLifecycleMethod(PreRemove.class, o);
     }
 
     private void firePreRemoveEvent(Query q) {
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.preRemove(q);
         }
         //TODO: Fix - cannot call lifecycle method
     }
 
     private void firePreListStoreEvent(List records) {
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.preListStore(records);
         }
         for (Object o : records) {
             callLifecycleMethod(PreStore.class, o);
         }
     }
 
     private void firePostListStoreEvent(List records) {
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.postListStore(records);
         }
         for (Object o : records) {
             callLifecycleMethod(PostStore.class, o);
         }
 
     }
 
     /**
      * will be called by query after unmarshalling
      *
      * @param o
      */
     protected void firePostLoadEvent(Object o) {
         //Avoid concurrent modification exception
         List<MorphiumStorageListener> lst = (List<MorphiumStorageListener>) listeners.clone();
         for (MorphiumStorageListener l : lst) {
             l.postLoad(o);
         }
         callLifecycleMethod(PostLoad.class, o);
 
     }
 
     public void storeNoCache(Object o) {
         Class type = o.getClass();
         if (!isAnnotationPresentInHierarchy(type, Entity.class)) {
             throw new RuntimeException("Not an entity! Storing not possible!");
         }
 
         ObjectId id = config.getMapper().getId(o);
         if (o instanceof PartiallyUpdateable && id != null) {
             updateUsingFields(o, ((PartiallyUpdateable) o).getAlteredFields().toArray(new String[((PartiallyUpdateable) o).getAlteredFields().size()]));
             return;
         }
         inc(StatisticKeys.WRITES);
         firePreStoreEvent(o);
 
         DBObject marshall = config.getMapper().marshall(o);
         boolean isNew = id == null;
 
         if (isNew) {
 
             //new object - need to store creation time
             if (isAnnotationPresentInHierarchy(type, StoreCreationTime.class)) {
                 List<String> lst = config.getMapper().getFields(type, CreationTime.class);
                 if (lst == null || lst.size() == 0) {
                     logger.error("Unable to store creation time as @CreationTime is missing");
                 } else {
                     long now = System.currentTimeMillis();
                     for (String ctf : lst) {
                         Field f = getField(type, ctf);
                         if (f != null) {
                             try {
                                 f.set(o, now);
                             } catch (IllegalAccessException e) {
                                 logger.error("Could not set creation time", e);
 
                             }
                         }
                         marshall.put(ctf, now);
                     }
 
                 }
                 lst = config.getMapper().getFields(type, CreatedBy.class);
                 if (lst != null && lst.size() > 0) {
                     for (String ctf : lst) {
 
                         Field f = getField(type, ctf);
                         if (f != null) {
                             try {
                                 f.set(o, config.getSecurityMgr().getCurrentUserId());
                             } catch (IllegalAccessException e) {
                                 logger.error("Could not set created by", e);
                             }
                         }
                         marshall.put(ctf, config.getSecurityMgr().getCurrentUserId());
                     }
                 }
             }
         }
         if (isAnnotationPresentInHierarchy(type, StoreLastChange.class)) {
             List<String> lst = config.getMapper().getFields(type, LastChange.class);
             if (lst != null && lst.size() > 0) {
                 for (String ctf : lst) {
                     long now = System.currentTimeMillis();
                     Field f = getField(type, ctf);
                     if (f != null) {
                         try {
                             f.set(o, now);
                         } catch (IllegalAccessException e) {
                             logger.error("Could not set modification time", e);
 
                         }
                     }
                     marshall.put(ctf, now);
                 }
             } else {
                 logger.warn("Could not store last change - @LastChange missing!");
             }
 
             lst = config.getMapper().getFields(type, LastChangeBy.class);
             if (lst != null && lst.size() > 0) {
                 for (String ctf : lst) {
 
                     Field f = getField(type, ctf);
                     if (f != null) {
                         try {
                             f.set(o, config.getSecurityMgr().getCurrentUserId());
                         } catch (IllegalAccessException e) {
                             logger.error("Could not set changed by", e);
                         }
                     }
                     marshall.put(ctf, config.getSecurityMgr().getCurrentUserId());
                 }
             }
         }
 
         WriteConcern wc = getWriteConcernForClass(o.getClass());
         if (wc != null) {
 
             database.getCollection(config.getMapper().getCollectionName(o.getClass())).save(marshall, wc);
         } else {
 
             database.getCollection(config.getMapper().getCollectionName(o.getClass())).save(marshall);
         }
         if (isNew) {
             List<String> flds = config.getMapper().getFields(o.getClass(), Id.class);
             if (flds == null) {
                 throw new RuntimeException("Object does not have an ID field!");
             }
             try {
                 //Setting new ID (if object was new created) to Entity
                 getField(o.getClass(), flds.get(0)).set(o, marshall.get("_id"));
             } catch (IllegalAccessException e) {
                 throw new RuntimeException(e);
             }
         }
 
         Cache ch = getAnnotationFromHierarchy(o.getClass(), Cache.class);
         if (ch != null) {
             if (ch.clearOnWrite()) {
                 clearCachefor(o.getClass());
             }
         }
 
         firePostStoreEvent(o);
     }
 
     private WriteConcern getWriteConcernForClass(Class<?> cls) {
         WriteSafety safety = getAnnotationFromHierarchy(cls, WriteSafety.class);  // cls.getAnnotation(WriteSafety.class);
         if (safety == null) return null;
         return new WriteConcern(safety.level().getValue(), safety.timeout(), safety.waitForSync(), safety.waitForJournalCommit());
     }
 
     public void storeNoCacheList(List o) {
 
         if (!o.isEmpty()) {
             List<Class> clearCaches = new ArrayList<Class>();
             firePreListStoreEvent(o);
             for (Object c : o) {
                 storeNoCache(c);
             }
             firePostListStoreEvent(o);
         }
     }
 
 
     protected boolean isCached(Class<? extends Object> type, String k) {
         Cache c = getAnnotationFromHierarchy(type, Cache.class); ///type.getAnnotation(Cache.class);
         if (c != null) {
             if (!c.readCache()) return false;
         } else {
             return false;
         }
         return cache.get(type) != null && cache.get(type).get(k) != null && cache.get(type).get(k).getFound() != null;
     }
 
     /**
      * return object by from cache. Cache key usually is the string-representation of the search
      * query.toQueryObject()
      *
      * @param type
      * @param k
      * @param <T>
      * @return
      */
     public <T> List<T> getFromCache(Class<T> type, String k) {
         if (cache.get(type) == null || cache.get(type).get(k) == null) return null;
         final CacheElement cacheElement = cache.get(type).get(k);
         cacheElement.setLru(System.currentTimeMillis());
         return cacheElement.getFound();
     }
 
     public Hashtable<Class<? extends Object>, Hashtable<String, CacheElement>> cloneCache() {
         return (Hashtable<Class<? extends Object>, Hashtable<String, CacheElement>>) cache.clone();
     }
 
     /**
      * issues a delete command - no lifecycle methods calles, no drop, keeps all indexec this way
      *
      * @param cls
      */
     public void clearCollection(Class<? extends Object> cls) {
         delete(createQueryFor(cls));
     }
 
     /**
      * clears every single object in collection - reads ALL objects to do so
      * this way Lifecycle methods can be called!
      *
      * @param cls
      */
 
     public void deleteCollectionItems(Class<? extends Object> cls) {
         if (!isAnnotationPresentInHierarchy(cls, NoProtection.class)) { //cls.isAnnotationPresent(NoProtection.class)) {
             try {
                 if (accessDenied(cls.newInstance(), Permission.DROP)) {
                     throw new SecurityException("Drop of Collection denied!");
                 }
             } catch (InstantiationException ex) {
                 Logger.getLogger(Morphium.class).error(ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(Morphium.class).error(ex);
             }
         }
 
 
         inc(StatisticKeys.WRITES);
         List<? extends Object> lst = readAll(cls);
         for (Object r : lst) {
             deleteObject(r);
         }
 
         clearCacheIfNecessary(cls);
 
 
     }
 
     /**
      * return a list of all elements stored in morphium for this type
      *
      * @param cls - type to search for, needs to be an Property
      * @param <T> - Type
      * @return - list of all elements stored
      */
     public <T> List<T> readAll(Class<T> cls) {
         inc(StatisticKeys.READS);
         Query<T> qu;
         qu = createQueryFor(cls);
         return qu.asList();
     }
 
     public <T> Query<T> createQueryFor(Class<T> type) {
         return new QueryImpl<T>(this, type, config.getMapper());
     }
 
     public <T> List<T> find(Query<T> q) {
         return q.asList();
     }
 
     public <T> T findById(Class<T> type, ObjectId id) {
         List<String> ls = config.getMapper().getFields(type, Id.class);
         if (ls.size() == 0) throw new RuntimeException("Cannot find by ID on non-Entity");
 
         return (T) createQueryFor(type).f(ls.get(0)).eq(id).get();
     }
 //    /**
 //     * returns a list of all elements for the given type, matching the given query
 //     * @param qu - the query to search
 //     * @param <T> - type of the elementyx
 //     * @return  - list of elements matching query
 //     */
 //    public <T> List<T> readAll(Query<T> qu) {
 //        inc(StatisticKeys.READS);
 //        if (qu.getEntityClass().isAnnotationPresent(Cache.class)) {
 //            if (isCached(qu.getEntityClass(), qu.toString())) {
 //                inc(StatisticKeys.CHITS);
 //                return getFromCache(qu.getEntityClass(), qu.toString());
 //            } else {
 //                inc(StatisticKeys.CMISS);
 //            }
 //        }
 //        List<T> lst = qu.asList();
 //        addToCache(qu.toString()+" / l:"+((QueryImpl)qu).getLimit()+" o:"+((QueryImpl)qu).getOffset(), qu.getEntityClass(), lst);
 //        return lst;
 //
 //    }
 
 
     /**
      * does not set values in DB only in the entity
      *
      * @param toSetValueIn
      */
     public void setValueIn(Object toSetValueIn, String fld, Object value) {
         config.getMapper().setValue(toSetValueIn, value, fld);
     }
 
     public void setValueIn(Object toSetValueIn, Enum fld, Object value) {
         config.getMapper().setValue(toSetValueIn, value, fld.name());
     }
 
     public Object getValueOf(Object toGetValueFrom, String fld) {
         return config.getMapper().getValue(toGetValueFrom, fld);
     }
 
     public Object getValueOf(Object toGetValueFrom, Enum fld) {
         return config.getMapper().getValue(toGetValueFrom, fld.name());
     }
 
 
     @SuppressWarnings("unchecked")
     public <T> List<T> findByField(Class<T> cls, String fld, Object val) {
         Query<T> q = createQueryFor(cls);
         q = q.f(fld).eq(val);
         return q.asList();
 //        return createQueryFor(cls).field(fld).equal(val).asList();
     }
 
     public <T> List<T> findByField(Class<T> cls, Enum fld, Object val) {
         Query<T> q = createQueryFor(cls);
         q = q.f(fld).eq(val);
         return q.asList();
 //        return createQueryFor(cls).field(fld).equal(val).asList();
     }
 
 
     /**
      * deletes all objects matching the given query
      *
      * @param q
      * @param <T>
      */
     public <T> void delete(Query<T> q) {
         firePreRemoveEvent(q);
         WriteConcern wc = getWriteConcernForClass(q.getType());
         if (wc == null) {
             database.getCollection(config.getMapper().getCollectionName(q.getType())).remove(q.toQueryObject());
         } else {
             database.getCollection(config.getMapper().getCollectionName(q.getType())).remove(q.toQueryObject(), wc);
         }
         clearCacheIfNecessary(q.getType());
         firePostRemoveEvent(q);
     }
 
 
     /**
      * get a list of valid fields of a given record as they are in the MongoDB
      * so, if you have a field Mapping, the mapped Property-name will be used
      *
      * @param cls
      * @return
      */
     public final List<String> getFields(Class cls) {
         return config.getMapper().getFields(cls);
     }
 
     public final Class getTypeOfField(Class cls, String fld) {
         Field f = getField(cls, fld);
         if (f == null) return null;
         return f.getType();
     }
 
     public boolean storesLastChange(Class<? extends Object> cls) {
         return isAnnotationPresentInHierarchy(cls, StoreLastChange.class);
     }
 
     public boolean storesLastAccess(Class<? extends Object> cls) {
         return isAnnotationPresentInHierarchy(cls, StoreLastAccess.class);
     }
 
     public boolean storesCreation(Class<? extends Object> cls) {
         return isAnnotationPresentInHierarchy(cls, StoreCreationTime.class);
     }
 
 
     private String getFieldName(Class cls, String fld) {
         return config.getMapper().getFieldName(cls, fld);
     }
 
     /**
      * extended logic: Fld may be, the java field name, the name of the specified value in Property-Annotation or
      * the translated underscored lowercase name (mongoId => mongo_id)
      *
      * @param cls - class to search
      * @param fld - field name
      * @return field, if found, null else
      */
     private Field getField(Class cls, String fld) {
         return config.getMapper().getField(cls, fld);
     }
 
     public void setValue(Object in, String fld, Object val) throws IllegalAccessException {
         Field f = getField(in.getClass(), fld);
         if (f == null) {
             throw new IllegalAccessException("Field " + fld + " not found");
         }
         f.set(in, val);
     }
 
     public Object getValue(Object o, String fld) throws IllegalAccessException {
         Field f = getField(o.getClass(), fld);
         if (f == null) {
             throw new IllegalAccessException("Field " + fld + " not found");
         }
         return f.get(o);
     }
 
     public Long getLongValue(Object o, String fld) throws IllegalAccessException {
         return (Long) getValue(o, fld);
     }
 
     public String getStringValue(Object o, String fld) throws IllegalAccessException {
         return (String) getValue(o, fld);
     }
 
     public Date getDateValue(Object o, String fld) throws IllegalAccessException {
         return (Date) getValue(o, fld);
     }
 
     public Double getDoubleValue(Object o, String fld) throws IllegalAccessException {
         return (Double) getValue(o, fld);
     }
 
 
     /**
      * Erase cache entries for the given type. is being called after every store
      * depending on cache settings!
      *
      * @param cls
      */
     public void clearCachefor(Class<? extends Object> cls) {
         if (cache.get(cls) != null) {
             cache.get(cls).clear();
         }
         //clearCacheFor(cls);
     }
 
     public void storeInBackground(final Object lst) {
         inc(StatisticKeys.WRITES_CACHED);
         writers.execute(new Runnable() {
             @Override
             public void run() {
                 storeNoCache(lst);
             }
         });
     }
 
     public void storeListInBackground(final List lst) {
         writers.execute(new Runnable() {
             @Override
             public void run() {
                 storeNoCacheList(lst);
             }
         });
     }
 
 
     public ObjectId getId(Object o) {
         return config.getMapper().getId(o);
     }
 
     public void dropCollection(Class<? extends Object> cls) {
         if (!isAnnotationPresentInHierarchy(cls, NoProtection.class)) {
             try {
                 if (accessDenied(cls.newInstance(), Permission.DROP)) {
                     throw new SecurityException("Drop of Collection denied!");
                 }
             } catch (InstantiationException ex) {
                 Logger.getLogger(Morphium.class.getName()).fatal(ex);
             } catch (IllegalAccessException ex) {
                 Logger.getLogger(Morphium.class.getName()).fatal(ex);
             }
         }
         if (isAnnotationPresentInHierarchy(cls, Entity.class)) {
             firePreDropEvent(cls);
             Entity entity = getAnnotationFromHierarchy(cls, Entity.class); //cls.getAnnotation(Entity.class);
             database.getCollection(config.getMapper().getCollectionName(cls)).drop();
             firePostDropEvent(cls);
         } else {
             throw new RuntimeException("No entity class: " + cls.getName());
         }
     }
 
     public void ensureIndex(Class<?> cls, Map<String, Integer> index) {
         List<String> fields = getFields(cls);
 
         Map<String, Integer> idx = new HashMap<String, Integer>();
         for (String k : index.keySet()) {
             if (!fields.contains(k) && !fields.contains(config.getMapper().convertCamelCase(k))) {
                 throw new IllegalArgumentException("Field unknown for type " + cls.getSimpleName() + ": " + k);
             }
             String fn = config.getMapper().getFieldName(cls, k);
             idx.put(fn, index.get(k));
         }
         database.getCollection(config.getMapper().getCollectionName(cls)).ensureIndex(new BasicDBObject(idx));
     }
 
     /**
      * ensureIndex(CachedObject.class,"counter","-value");
      * Similar to sorting
      *
      * @param cls
      * @param fldStr
      */
     public void ensureIndex(Class<?> cls, String... fldStr) {
         Map<String, Integer> m = new HashMap<String, Integer>();
         for (String f : fldStr) {
             int idx = 1;
             if (f.startsWith("-")) {
                 idx = -1;
                 f = f.substring(1);
             } else if (f.startsWith("+")) {
                 f = f.substring(1);
             }
             m.put(f, idx);
         }
         ensureIndex(cls, m);
     }
 
     public void ensureIndex(Class<?> cls, Enum... fldStr) {
         Map<String, Integer> m = new HashMap<String, Integer>();
         for (Enum e : fldStr) {
             String f = e.name();
             m.put(f, 1);
         }
         ensureIndex(cls, m);
     }
 
 
     /**
      * Stores a single Object. Clears the corresponding cache
      *
      * @param o - Object to store
      */
     public void store(final Object o) {
         if (o instanceof List) {
             throw new RuntimeException("Lists need to be stored with storeList");
         }
 
 
         if (!isAnnotationPresentInHierarchy(o.getClass(), NoProtection.class)) { //o.getClass().isAnnotationPresent(NoProtection.class)) {
             if (getId(o) == null) {
                 if (accessDenied(o, Permission.INSERT)) {
                     throw new SecurityException("Insert of new Object denied!");
                 }
             } else {
                 if (accessDenied(o, Permission.UPDATE)) {
                     throw new SecurityException("Update of Object denied!");
                 }
             }
         }
 
         Cache cc = getAnnotationFromHierarchy(o.getClass(), Cache.class);//o.getClass().getAnnotation(Cache.class);
         if (cc == null || isAnnotationPresentInHierarchy(o.getClass(), NoCache.class)) {
             storeNoCache(o);
             return;
         }
 
         if (cc.writeCache()) {
             writers.execute(new Runnable() {
                 @Override
                 public void run() {
                     storeNoCache(o);
                 }
             });
             inc(StatisticKeys.WRITES_CACHED);
 
         } else {
             storeNoCache(o);
         }
 
     }
 
     public <T> void storeList(List<T> lst) {
         //have to sort list - might have different objects 
         List<T> storeDirect = new ArrayList<T>();
         List<T> storeInBg = new ArrayList<T>();
 
         //checking permission - might take some time ;-(
         for (T o : lst) {
             if (!isAnnotationPresentInHierarchy(o.getClass(), NoProtection.class)) {
                 if (getId(o) == null) {
                     if (accessDenied(o, Permission.INSERT)) {
                         throw new SecurityException("Insert of new Object denied!");
                     }
                 } else {
                     if (accessDenied(o, Permission.UPDATE)) {
                         throw new SecurityException("Update of Object denied!");
                     }
                 }
             }
 
             Cache c = getAnnotationFromHierarchy(o.getClass(), Cache.class);//o.getClass().getAnnotation(Cache.class);
             if (c != null && !isAnnotationPresentInHierarchy(o.getClass(), NoCache.class)) {
                 if (c.writeCache()) {
                     storeInBg.add(o);
                 } else {
                     storeDirect.add(o);
                 }
             } else {
                 storeDirect.add(o);
 
             }
         }
 
         storeListInBackground(storeInBg);
         storeNoCacheList(storeDirect);
 
     }
 
 
     /**
      * deletes a single object from morphium backend. Clears cache
      *
      * @param o
      */
     public <T> void deleteObject(T o) {
         if (!isAnnotationPresentInHierarchy(o.getClass(), NoProtection.class)) {
             if (accessDenied(o, Permission.DELETE)) {
                 throw new SecurityException("Deletion of Object denied!");
             }
         }
         firePreRemoveEvent(o);
 
         ObjectId id = config.getMapper().getId(o);
         BasicDBObject db = new BasicDBObject();
         db.append("_id", id);
         WriteConcern wc = getWriteConcernForClass(o.getClass());
         if (wc == null) {
             database.getCollection(config.getMapper().getCollectionName(o.getClass())).remove(db);
         } else {
             database.getCollection(config.getMapper().getCollectionName(o.getClass())).remove(db, wc);
         }
 
         clearCachefor(o.getClass());
         inc(StatisticKeys.WRITES);
         firePostRemoveEvent(o);
     }
 
     public void resetCache() {
         setCache(new Hashtable<Class<? extends Object>, Hashtable<String, CacheElement>>());
     }
 
     public void setCache(Hashtable<Class<? extends Object>, Hashtable<String, CacheElement>> cache) {
         this.cache = cache;
     }
 
 
     //////////////////////////////////////////////////////////////////////
     //////////////////////////////////////////////////
     //////////////////////////////
     /////////////// Statistics
     /////////
     /////
     ///
     public Map<String, Double> getStatistics() {
         return new StatisticsMap();
     }
 
     public enum StatisticKeys {
 
         WRITES, WRITES_CACHED, READS, CHITS, CMISS, NO_CACHED_READS, CHITSPERC, CMISSPERC, CACHE_ENTRIES, WRITE_BUFFER_ENTRIES
     }
 
     public class StatisticValue {
 
         private long value = 0;
 
         public void inc() {
             value++;
         }
 
         public void dec() {
             value--;
         }
 
         public long get() {
             return value;
         }
     }
 
     private class StatisticsMap extends Hashtable<String, Double> {
 
         /**
          *
          */
         private static final long serialVersionUID = -2831335094438480701L;
 
         @SuppressWarnings("rawtypes")
         public StatisticsMap() {
             for (StatisticKeys k : stats.keySet()) {
                 super.put(k.name(), (double) stats.get(k).get());
             }
             double entries = 0;
             for (Class k : cache.keySet()) {
                 entries += cache.get(k).size();
                 super.put("X-Entries for: " + k.getName(), (double) cache.get(k).size());
             }
             super.put(StatisticKeys.CACHE_ENTRIES.name(), entries);
 
             entries = 0;
 
             super.put(StatisticKeys.WRITE_BUFFER_ENTRIES.name(), Double.valueOf((double) writeBufferCount()));
             super.put(StatisticKeys.CHITSPERC.name(), ((double) stats.get(StatisticKeys.CHITS).get()) / (stats.get(StatisticKeys.READS).get() - stats.get(StatisticKeys.NO_CACHED_READS).get()) * 100.0);
             super.put(StatisticKeys.CMISSPERC.name(), ((double) stats.get(StatisticKeys.CMISS).get()) / (stats.get(StatisticKeys.READS).get() - stats.get(StatisticKeys.NO_CACHED_READS).get()) * 100.0);
         }
 
         @Override
         public synchronized Double put(String arg0, Double arg1) {
             throw new RuntimeException("not allowed!");
         }
 
         @Override
         public synchronized void putAll(@SuppressWarnings("rawtypes") Map arg0) {
             throw new RuntimeException("not allowed");
         }
 
         @Override
         public synchronized Double remove(Object arg0) {
             throw new RuntimeException("not allowed");
         }
 
         @Override
         public String toString() {
             StringBuffer b = new StringBuffer();
             String[] lst = keySet().toArray(new String[keySet().size()]);
             Arrays.sort(lst);
             for (String k : lst) {
                 b.append("- ");
                 b.append(k);
                 b.append("\t");
                 b.append(get(k));
                 b.append("\n");
             }
             return b.toString();
         }
     }
 
     public void addShutdownListener(ShutdownListener l) {
         shutDownListeners.add(l);
     }
 
     public void removeShutdownListener(ShutdownListener l) {
         shutDownListeners.remove(l);
     }
 
     public void close() {
         cacheHousekeeper.end();
 
         for (ShutdownListener l : shutDownListeners) {
             l.onShutdown(this);
         }
         try {
             Thread.sleep(1000); //give it time to end ;-)
         } catch (Exception e) {
             logger.debug("Ignoring interrupted-exception");
         }
         if (cacheHousekeeper.isAlive()) {
             cacheHousekeeper.interrupt();
         }
         database = null;
         config = null;
 
         mongo.close();
 
     }
 
 
     public String createCamelCase(String n) {
         return config.getMapper().createCamelCase(n, false);
     }
 
     /**
      * create a proxy object, implementing the ParitallyUpdateable Interface
      * these objects will be updated in mongo by only changing altered fields
      * <b>Attention:</b> the field name if determined by the setter name for now. That means, it does not honor the @Property-Annotation!!!
      * To make sure, you take the correct field - use the UpdatingField-Annotation for the setters!
      *
      * @param o
      * @param <T>
      * @return
      */
     public <T> T createPartiallyUpdateableEntity(T o) {
         return (T) Enhancer.create(o.getClass(), new Class[]{PartiallyUpdateable.class}, new PartiallyUpdateableInvocationHandler(o));
     }
 
    public <T> T createLazyLoadedEntity(T o) {
        ObjectId id = config.getMapper().getId(o);
        return (T) Enhancer.create(o.getClass(), new Class[]{}, new LazyDeReferencingHandler(o.getClass(), id));
     }
 
     protected <T> MongoField<T> createMongoField() {
         try {
             return (MongoField<T>) Class.forName(config.getFieldImplClass()).newInstance();
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         } catch (ClassNotFoundException e) {
             throw new RuntimeException(e);
         }
     }
 
     /**
      * CGLib Interceptor to create a transparent Proxy for partially updateable Entities
      */
     private class PartiallyUpdateableInvocationHandler<T> implements MethodInterceptor, PartiallyUpdateable {
         private List<String> updateableFields;
         private T reference;
 
         public PartiallyUpdateableInvocationHandler(T o) {
             updateableFields = new Vector<String>();
             reference = o;
         }
 
         public T __getDeref() {
             //do nothing - will be intercepted
             return reference;
         }
 
         @Override
         public List<String> getAlteredFields() {
             return updateableFields;
         }
 
         @Override
         public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
             if (method.getName().startsWith("set")) {
                 if (method.isAnnotationPresent(PartialUpdate.class)) {
                     PartialUpdate up = method.getAnnotation(PartialUpdate.class);
                     if (!getFields(o.getClass()).contains(up.value())) {
                         throw new IllegalArgumentException("Field " + up.value() + " is not known to Type " + o.getClass().getName());
                     }
                     updateableFields.add(up.value());
                 } else {
                     String n = method.getName().substring(3);
                     n = n.substring(0, 1).toLowerCase() + n.substring(1);
                     updateableFields.add(n);
                 }
             }
             if (method.getName().equals("getAlteredFields")) {
                 return getAlteredFields();
             }
             if (method.getName().equals("__getType")) {
                 return o.getClass();
             }
             if (method.getName().equals("__getDeref")) {
                 return o;
             }
             return methodProxy.invokeSuper(o, objects);
         }
     }
 
     // Lazy loading / DeReferencing of References
     private class LazyDeReferencingHandler<T> implements MethodInterceptor {
         private T deReferenced;
         private Class<T> cls;
         private ObjectId id;
 
         public LazyDeReferencingHandler(Class type, ObjectId id) {
             cls = type;
             this.id = id;
         }
 
         public T __getDeref() {
             try {
                 dereference();
             } catch (Throwable throwable) {
             }
             return deReferenced;
         }
 
         @Override
         public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
 //            if (method.getName().equals("getClass")) {
 //                return cls;
 //            }
             if (method.getName().equals("__getType")) {
                 return cls;
             }
             if (method.getName().equals("finalize")) {
                 return methodProxy.invokeSuper(o, objects);
             }
 
             if (dereference()) return methodProxy.invokeSuper(o, objects);
             if (method.getName().equals("__getDeref")) {
                 return deReferenced;
             }
             if (deReferenced != null) {

                 return methodProxy.invoke(deReferenced, objects);
             }
             return methodProxy.invokeSuper(o, objects);
 
         }
 
         private boolean dereference() throws Throwable {
            MethodProxy methodProxy;
            Object o;
            Object[] objects;
             if (deReferenced == null) {
                 if (logger.isDebugEnabled())
                     logger.debug("DeReferencing due to first access");
 
                 if (id == null) {
                     return true;
                 }
 
                 deReferenced = (T) findById(cls, id);
 
             }
             return false;
         }
 
     }
 
     public String getLastChangeField(Class<?> cls) {
         if (!isAnnotationPresentInHierarchy(cls, StoreLastChange.class)) return null;
         List<String> lst = config.getMapper().getFields(cls, LastChange.class);
         if (lst == null || lst.isEmpty()) return null;
         return lst.get(0);
     }
 
     public String getLastChangeByField(Class<?> cls) {
         if (!isAnnotationPresentInHierarchy(cls, StoreLastChange.class)) return null;
         List<String> lst = config.getMapper().getFields(cls, LastChangeBy.class);
         if (lst == null || lst.isEmpty()) return null;
         return lst.get(0);
     }
 
     public String getLastAccessField(Class<?> cls) {
         if (!isAnnotationPresentInHierarchy(cls, StoreLastAccess.class)) return null;
         List<String> lst = config.getMapper().getFields(cls, LastAccess.class);
         if (lst == null || lst.isEmpty()) return null;
         return lst.get(0);
     }
 
     public String getLastAccessByField(Class<?> cls) {
         if (!isAnnotationPresentInHierarchy(cls, StoreLastAccess.class)) return null;
         List<String> lst = config.getMapper().getFields(cls, LastAccessBy.class);
         if (lst == null || lst.isEmpty()) return null;
         return lst.get(0);
     }
 
 
     public String getCreationTimeField(Class<?> cls) {
         if (!isAnnotationPresentInHierarchy(cls, StoreCreationTime.class)) return null;
         List<String> lst = config.getMapper().getFields(cls, CreationTime.class);
         if (lst == null || lst.isEmpty()) return null;
         return lst.get(0);
     }
 
     public String getCreatedByField(Class<?> cls) {
         if (!isAnnotationPresentInHierarchy(cls, StoreCreationTime.class)) return null;
         List<String> lst = config.getMapper().getFields(cls, CreatedBy.class);
         if (lst == null || lst.isEmpty()) return null;
         return lst.get(0);
     }
 
 
     //////////////////////////////////////////////////////
     ////////// SecuritySettings
     ///////
     /////
     ////
     ///
     //
     public MongoSecurityManager getSecurityManager() {
         return config.getSecurityMgr();
     }
 
     /**
      * temporarily switch off security settings - needed by SecurityManagers
      */
     public void setPrivileged() {
         privileged.add(Thread.currentThread());
     }
 
     public boolean checkAccess(String domain, Permission p) throws MongoSecurityException {
         if (privileged.contains(Thread.currentThread())) {
             privileged.remove(Thread.currentThread());
             return true;
         }
         return getSecurityManager().checkAccess(domain, p);
     }
 
     public boolean accessDenied(Class<?> cls, Permission p) throws MongoSecurityException {
         if (privileged.contains(Thread.currentThread())) {
             privileged.remove(Thread.currentThread());
             return false;
         }
         return !getSecurityManager().checkAccess(cls, p);
     }
 
     public boolean accessDenied(Object r, Permission p) throws MongoSecurityException {
         if (privileged.contains(Thread.currentThread())) {
             privileged.remove(Thread.currentThread());
             return false;
         }
         return !getSecurityManager().checkAccess(r, p);
     }
 
 
 }
