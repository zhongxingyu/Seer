 package nl.astraeus.persistence;
 
 import nl.astraeus.persistence.reflect.ReflectHelper;
 
 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * User: rnentjes
  * Date: 3/27/12
  * Time: 10:09 PM
  */
 public class PersistentIndex<K, M extends Persistent<K>, T> implements Serializable {
     public final static long serialVersionUID = 1L;
 
     protected Map<T, Set<K>> index = new HashMap<T, Set<K>>();
     private Class<M> cls;
     private String propertyName;
     private boolean initialized = false;
 
     protected PersistentIndex(Class<M> cls, String propertyName) {
         this.cls = cls;
         this.propertyName = propertyName;
     }
 
     public T getIndexValue(M model) {
        Object value = ReflectHelper.get().getFieldValue(model, propertyName);

        if (value instanceof PersistentReference) {
            value = ((PersistentReference)value).get();
        }

        return (T) value;
     }
 
     public void update(M model) {
         Set<K> set = index.get(getIndexValue(model));
 
         if (set == null) {
             set = new HashSet<K>();
 
             index.put(getIndexValue(model), set);
         }
 
         set.add(model.getId());
     }
 
     public void remove(M model) {
         Set<K> set = index.get(getIndexValue(model));
 
         if (set != null) {
             set.remove(model.getId());
         }
     }
     
     public Set<K> find(T value) {
         Set<K> result = new HashSet<K>();
 
         if (index.get(value) != null) {
             result = index.get(value);
         }
 
         return result;
     }
 
 }
