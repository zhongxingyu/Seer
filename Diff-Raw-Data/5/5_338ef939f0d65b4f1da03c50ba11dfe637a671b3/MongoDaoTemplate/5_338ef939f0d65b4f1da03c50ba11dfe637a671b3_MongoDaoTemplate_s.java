 package org.inigma.shared.mongo;
 
 import java.io.Serializable;
 import java.lang.annotation.Annotation;
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.UUID;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
 
 import com.mongodb.BasicDBObject;
import com.mongodb.CommandResult;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
import com.mongodb.MongoException;
 import com.mongodb.WriteResult;
 
 /**
  * Mongo DAO Templating framework. Similar in intent to the spring jdbc template.
  * 
  * @author <a href="mailto:sejal@inigma.org">Sejal Patel</a>
  */
 public abstract class MongoDaoTemplate<T> {
     protected Log logger = LogFactory.getLog(getClass());
 
     protected final MongoDataStore pool;
     protected final String collection;
 
     public MongoDaoTemplate(MongoDataStore pool, String collection) {
         this.pool = pool;
         this.collection = collection;
     }
 
     protected MongoDaoTemplate() {
         // for @Cacheable annotation support. Should never be used actually.
         this(null, null);
     }
 
     /**
      * A query retrieving all documents in the collection.
      */
     public Collection<T> find() {
         return convert(getCollection(true).find());
     }
 
     /**
      * A query retrieving all documents in the collection up to the specified limit.
      */
     public Collection<T> find(int limit) {
         return convert(getCollection(true).find().limit(limit));
     }
 
     public Collection<T> find(Map<String, Object> params) {
         BasicDBObject query = new BasicDBObject(params);
         return convert(getCollection(true).find(query));
     }
 
     /**
      * A very simplistic query with only a single key/value pairing.
      */
     public Collection<T> find(String key, Object value) {
         BasicDBObject query = new BasicDBObject(key, value);
         return convert(getCollection(true).find(query));
     }
 
     /**
      * A very simple retrieval of data given the internal object id.
      */
     public T findById(Serializable id) {
         BasicDBObject query = new BasicDBObject("_id", id);
         return convert(getCollection(true).findOne(query));
     }
 
     public Collection<T> findByIds(Collection<?> ids) {
         BasicDBObject inClause = new BasicDBObject("$in", ids);
         BasicDBObject query = new BasicDBObject("_id", inClause);
         return convert(getCollection(true).find(query));
     }
 
     public DBCollection getCollection() {
         return pool.getCollection(collection);
     }
 
     public DBCollection getCollection(boolean slave) {
         return pool.getCollection(collection, slave);
     }
     protected WriteResult upsert(DBObject object) {
         BasicDBObject query = new BasicDBObject("_id", object.removeField("_id"));
         BasicDBObject update = new BasicDBObject(object.toMap());
         return getCollection(false).update(query, new BasicDBObject("$set", update), true, false);
     }
 
     protected Collection<T> convert(final DBCursor cursor) {
         return new ArrayList<T>() {
             @Override
             public boolean contains(Object o) {
                 throw new UnsupportedOperationException("This collection is a cursor");
             }
 
             @Override
             public boolean isEmpty() {
                 return cursor.size() == 0;
             }
 
             @Override
             public Iterator<T> iterator() {
                 final Iterator<DBObject> iterator = cursor.iterator();
                 return new Iterator<T>() {
                     @Override
                     public boolean hasNext() {
                         return iterator.hasNext();
                     }
 
                     @Override
                     public T next() {
                         return convert(iterator.next());
                     }
 
                     @Override
                     public void remove() {
                         iterator.remove();
                     }
                 };
             }
 
             @Override
             public int size() {
                 return cursor.size();
             }
 
             @Override
             protected void finalize() throws Throwable {
                 cursor.close();
                 super.finalize();
             }
         };
     }
 
     protected final T convert(DBObject data) {
         if (data == null) {
             return null;
         }
         return convert(new DBObjectWrapper(data));
     }
 
     protected abstract T convert(DBObjectWrapper data);
 
     protected final <B> B convert(DBObjectWrapper data, Class<B> clazz) {
         try {
             B bean = clazz.newInstance();
             for (Field field : clazz.getDeclaredFields()) {
                 field.setAccessible(true);
                 for (Annotation annotation : field.getDeclaredAnnotations()) {
                     if (MongoMapping.class.isInstance(annotation)) {
                         MongoMapping mf = (MongoMapping) annotation;
                         String key = mf.value();
                         if ("".equals(key)) {
                             key = field.getName();
                         }
                         if (data.containsField(key)) {
                             Class<?> type = field.getType();
                             if (Byte.class.isAssignableFrom(type)) {
                                 field.setByte(bean, data.getByte(key));
                             } else if (Boolean.class.isAssignableFrom(type)) {
                                 field.setBoolean(bean, data.getBoolean(key));
                             } else if (Calendar.class.isAssignableFrom(type)) {
                                 Date date = data.getDate(key);
                                 Calendar c = null;
                                 if (date != null) {
                                     c = Calendar.getInstance();
                                     c.setTimeInMillis(date.getTime());
                                 }
                                 field.set(bean, c);
                             } else if (Character.class.isAssignableFrom(type) || char.class.isAssignableFrom(type)) {
                                 String value = data.getString(key);
                                 if (value == null || value.length() == 0) {
                                     field.set(bean, null);
                                 } else {
                                     field.set(bean, value.charAt(0));
                                 }
                             } else if (Date.class.isAssignableFrom(type)) {
                                 field.set(bean, data.getDate(key));
                             } else if (Double.class.isAssignableFrom(type) || double.class.isAssignableFrom(type)) {
                                 field.setDouble(bean, data.getDouble(key));
                             } else if (Float.class.isAssignableFrom(type) || float.class.isAssignableFrom(type)) {
                                 field.setFloat(bean, data.getFloat(key));
                             } else if (Integer.class.isAssignableFrom(type) || int.class.isAssignableFrom(type)) {
                                 field.setInt(bean, data.getInteger(key));
                             } else if (Long.class.isAssignableFrom(type) || long.class.isAssignableFrom(type)) {
                                 field.setLong(bean, data.getLong(key));
                             } else if (Short.class.isAssignableFrom(type) || short.class.isAssignableFrom(type)) {
                                 field.setShort(bean, data.getShort(key));
                             } else if (String.class.isAssignableFrom(type)) {
                                 field.set(bean, data.getString(key));
                             } else { // Document Type
                                 field.set(bean, convert(data.getDocument(key), type));
                             }
                         }
                     }
                 }
             }
             return bean;
         } catch (InstantiationException e) {
             throw new RuntimeException(e);
         } catch (IllegalAccessException e) {
             throw new RuntimeException(e);
         }
     }
 
     protected String generateId() {
         return UUID.randomUUID().toString().replaceAll("\\-", "");
     }
 }
