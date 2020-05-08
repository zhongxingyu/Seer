 package ai.ilikeplaces.util.cache;
 
 import com.hazelcast.core.MapStore;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.Persistence;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Created by IntelliJ IDEA.
  * User: ravindranathakila
  * Date: 7/22/12
  * Time: 2:53 PM
  */
 public abstract class AbstractHCMapStore<T> implements MapStore<Object, Object> {
 
     public AbstractHCMapStore() {
         System.out.println("Starting Hazelcast MapStore implementation:" + this.getClass().getSimpleName());
     }
 
 
     public void store(Object key, Object value, Class type) {
         try {
             System.out.println("Attempting to store data:" + value);
 
             EntityManagerFactory emf = Persistence.createEntityManagerFactory("adimpression_ilikeplaces_war_1.6-SNAPSHOTPU_DN");
             System.out.println("Obtained EntityManagerFactory.");
 
             EntityManager em = emf.createEntityManager();
             System.out.println("Obtained EntityManager.");
 
             em.getTransaction().begin();
             System.out.println("Began Transaction.");
 
            final Object existingValue = em.find(type, key);
 
            if (existingValue == null) {
                em.persist(value);
            } else {
                em.merge(value);
            }

            System.out.println("Persisted data.");
 
             em.getTransaction().commit();
             System.out.println("Committed Transaction");
 
         } catch (final Throwable t) {
             t.printStackTrace(System.err);
         }
     }
 
     @Override
     public void storeAll(Map<Object, Object> keyValueMap) {
         try {
             for (final Object key : keyValueMap.keySet()) {
                 store(key, keyValueMap.get(key));
             }
         } catch (final Throwable t) {
             t.printStackTrace(System.err);
         }
     }
 
     public void delete(Object key, Class type) {
         try {
             System.out.println("Attempting to delete data:" + key);
             EntityManagerFactory emf = Persistence.createEntityManagerFactory("adimpression_ilikeplaces_war_1.6-SNAPSHOTPU_DN");
             System.out.println("Obtained EntityManagerFactory.");
             EntityManager em = emf.createEntityManager();
             System.out.println("Obtained EntityManager.");
             em.getTransaction().begin();
             System.out.println("Began Transaction.");
 
 
             System.out.println("key.getClass()");
             System.out.println(type);
 
             em.remove(em.find(type, key));
 
 
             System.out.println("Removed data.");
             em.getTransaction().commit();
             System.out.println("Committed Transaction.");
         } catch (final Throwable t) {
             t.printStackTrace(System.err);
         }
     }
 
     public void deleteAll(Collection<Object> keys) {
         try {
             for (final Object string : keys) {
                 delete(string);
             }
         } catch (final Throwable t) {
             t.printStackTrace(System.err);
         }
 
     }
 
     public Object load(Object key, Class type) {
         try {
             System.out.println("Attempting to load data:" + key);
             EntityManagerFactory emf = Persistence.createEntityManagerFactory("adimpression_ilikeplaces_war_1.6-SNAPSHOTPU_DN");
             System.out.println("Obtained EntityManagerFactory.");
             EntityManager em = emf.createEntityManager();
             System.out.println("Obtained EntityManager.");
             em.getTransaction().begin();
             System.out.println("Began Transaction.");
 
             System.out.println("key.getClass()");
             System.out.println(key.getClass());
 
             final Object returnVal = em.find(type, key);
 
             System.out.println("Loaded data.");
             em.getTransaction().commit();
             System.out.println("Committed Transaction.");
             return returnVal;
         } catch (final Throwable t) {
             t.printStackTrace(System.err);
             throw new RuntimeException(t);
         }
     }
 
     @Override
     public Map<Object, Object> loadAll(Collection<Object> keys) {
         try {
             final Map<Object, Object> returnVal = new HashMap<Object, Object>(keys.size());
             for (final Object string : keys) {
                 returnVal.put(string, load(string));
             }
             return returnVal;
         } catch (final Throwable t) {
             t.printStackTrace(System.err);
             throw new RuntimeException(t);
         }
     }
 
     @Override
     public Set<Object> loadAllKeys() {
         return null;
     }
 }
