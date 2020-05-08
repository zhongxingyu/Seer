 package nl.astraeus.persistence;
 
 import nl.astraeus.persistence.reflect.ReflectHelper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.CheckForNull;
 import javax.annotation.Nonnull;
 import java.io.Serializable;
 import java.lang.reflect.Field;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * User: rnentjes
  * Date: 8/19/11
  * Time: 8:54 PM
  */
 public class PersistentObjectStore implements Serializable {
     private final static Logger logger = LoggerFactory.getLogger(PersistentObjectStore.class);
 
     public static final long serialVersionUID = 1468478256843309473L;
 
     private Map<Class<? extends Persistent>, Map<Object, Persistent>> persistentStore = new HashMap<Class<? extends Persistent>, Map<Object, Persistent>>(64);
 
     // Optimistic locking control
     private Map<Class<? extends Persistent>, Map<Object, Long>> updateTimestamps = new HashMap<Class<? extends Persistent>, Map<Object, Long>>(64);
 
     // index definitions
     // index class + property
     // index type - tree, hash ???
     private Map<Class<? extends Persistent>, Map<String, PersistentIndex>> indexes =
             new HashMap<Class<? extends Persistent>, Map<String, PersistentIndex>>();
 
     /*
      * Class.getName()+id, class, list<id>
      */
     //private Map<String, Map<Class<? extends SimpleModel>, Set<Long>>> references = new HashMap<String, Map<Class<? extends SimpleModel>, Set<Long>>>();
     // of:
     private Map<Class<? extends Persistent>, Map<Object, Map<Class<? extends Persistent>, Set<Long>>>> references;
 
 
     protected Map<Class<? extends Persistent>, Map<Object, Persistent>> getPersistentStore() {
         return persistentStore;
     }
 
     @Nonnull
     public <K, M extends Persistent<K>> Map getModelMap(Class<M> cls) {
         Map result = persistentStore.get(cls);
 
         if (result == null) {
             result = new HashMap();
 
             persistentStore.put(cls, (Map<Object, Persistent>) result);
         }
 
         return result;
     }
 
     public <K, M extends Persistent<K>> M find(Class<M> cls, K id) {
         M result = (M)getModelMap(cls).get(id);
 
         return result;
     }
 
     /** Functions called from within ExecuteTransaction to update data model */
     protected <K, M extends Persistent<K>> void store(M objectToStore) {
         logger.debug("Storing: " + objectToStore.getClass().getName()+"-"+String.valueOf(objectToStore.getId()));
 
         // check for references, work with proxies?
 
         saveConversation(objectToStore);
 
         Map<K, M> modelMap = (Map<K, M>) getModelMap(objectToStore.getClass());
 
         modelMap.put(objectToStore.getId(), objectToStore);
     }
 
     /** Functions called from within ExecuteTransaction to update data model */
     protected <K, M extends Persistent<K>> void remove(M objectToRemove) {
         logger.debug("Removing: " + objectToRemove.getClass().getName()+"-"+String.valueOf(objectToRemove.getId()));
 
         getModelMap(objectToRemove.getClass()).remove(objectToRemove.getId());
     }
 
     // check any SimpleList and SimpleReferences and if they have values to save
     protected <K, M extends Persistent<K>> void saveConversation(M model) {
         try {
             for (Field field : ReflectHelper.get().getFieldsFromClass(model.getClass())) {
                 if (field.getType().equals(PersistentReference.class)) {
                     PersistentReference<K, M> ref = (PersistentReference<K, M>) field.get(model);
 
                     if (ref != null) {
                         M m = ref.getIncoming();
                        ref.clearIncoming();
 
                         if (m != null) {
                             store(m);
                         }
                     }
                 } else if (field.getType().equals(PersistentList.class)) {
                     PersistentList<K, M> list = (PersistentList<K, M>) field.get(model);
 
                     if (list != null) {
                         Map incoming = list.getIncoming();
                        list.clearIncoming();
 
                         if (incoming != null) {
                             for (M m : (Collection<M>)incoming.values()) {
                                 store(m);
                             }
                         }
 
                     }
                 }
             }
         } catch (IllegalAccessException e) {
             throw new IllegalStateException(e);
         }
 
     }
 
     /*
     protected <K, M extends Persistent<K>> void addReferences(M model) {
         // go through the model, add a reference for every object referenced from this one
         try {
             for (Field field : ReflectHelper.get().getFieldsFromClass(model.getClass())) {
                 if (field.getType().equals(PersistentReference.class)) {
                     PersistentReference ref = (PersistentReference) field.get(model);
 
                     if (!ref.isNull()) {
                         setReference(ref.get(), model.getClass(), model.getId());
                     }
                 } else if (field.getType().equals(SimpleList.class)) {
 
                 }
             }
         } catch (IllegalAccessException e) {
             throw new IllegalStateException(e);
         }
 
     }
 
     protected void setReference(SimpleModel model, Class<? extends SimpleModel> cls, Long id) {
         Map<Class<? extends SimpleModel>, Set<Long>> map = getReferenceMap(model);
 
         Set<Long> set = map.get(cls);
 
         if (set ==null) {
             set = new HashSet<Long>();
 
             map.put(cls, set);
         }
 
         set.add(id);
     }
 
     protected void setReference(SimpleModel model, SimpleModel other) {
         setReference(model, other.getClass(), other.getId());
    }
 
     protected void removeReference(SimpleModel model) {
         references.remove(model.getGUID());
     }
 
     protected String getReference(SimpleModel model) {
         return ReflectHelper.get().getClassName(model.getClass()) + ":" + model.getIdAsString();
     }
 
     protected Map<Class<? extends SimpleModel>, Set<Long>> getReferenceMap(SimpleModel model) {
         String ref = getReference(model);
         Map<Class<? extends SimpleModel>, Set<Long>> result = references.get(ref);
 
         if (result == null) {
             result = new HashMap<Class<? extends SimpleModel>, Set<Long>>();
 
             references.put(ref, result);
         }
 
         return result;
     }*/
 
     @CheckForNull
     protected <K, M extends Persistent<K>> PersistentIndex getIndex(Class<M> cls, String property) {
         PersistentIndex result;
 
         Map<String, ? extends PersistentIndex> indexMap = getIndexMap(cls);
 
         result = indexMap.get(property);
 
         return result;
     }
 
     protected <P extends PersistentIndex> void setIndex(Class cls, String property, @Nonnull P index) {
         Map<String, P> indexMap = (Map<String, P>) getIndexMap(cls);
 
         indexMap.put(property, index);
     }
 
     public <K, M extends Persistent<K>> void updateIndex(M model) {
         Map<String, ? extends PersistentIndex> indexMap = getIndexMap(model.getClass());
 
         for (PersistentIndex si : indexMap.values()) {
             if (si == null) {
                 logger.warn("null found in indexMap: {}", indexMap);
             } else {
                 si.update(model);
             }
         }
     }
 
     public <K, M extends Persistent<K>> void removeIndex(M model) {
         Map<String, ? extends PersistentIndex> indexMap = getIndexMap(model.getClass());
 
         for (PersistentIndex si : indexMap.values()) {
             if (si == null) {
                 logger.warn("null found in indexMap: {}", indexMap);
             } else {
                 si.remove(model);
             }
         }
     }
 
     public <K, M extends Persistent<K>> PersistentIndex createIndex(Class<M> cls, String propertyName) {
         PersistentIndex<K,M,Object> index;
 
         Map<String, PersistentIndex> indexMap = getIndexMap(cls);
 
         index = indexMap.get(propertyName);
 
         if (index == null) {
             index = new PersistentIndex(cls, propertyName);
             indexMap.put(propertyName, index);
 
             if (persistentStore.get(cls) != null) {
                 for (Persistent model : persistentStore.get(cls).values()) {
                     updateIndex(model);
                 }
             }
         }
 
         return index;
     }
 
     public <K, M extends Persistent<K>> void removeIndex(Class<M> cls, String propertyName) {
         Map<String, PersistentIndex> indexMap = getIndexMap(cls);
 
         indexMap.remove(propertyName);
     }
 
     @Nonnull
     protected <K, M extends Persistent<K>> Map<String, PersistentIndex> getIndexMap(Class<M> cls) {
         Map<String, PersistentIndex> result = indexes.get(cls);
 
         if (result == null) {
             result = new HashMap<String, PersistentIndex>();
 
             indexes.put(cls, result);
         }
 
         return result;
     }
 
 }
