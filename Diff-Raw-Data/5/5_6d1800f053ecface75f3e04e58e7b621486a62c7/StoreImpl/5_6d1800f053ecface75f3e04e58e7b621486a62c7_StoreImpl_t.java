 package org.daum.library.fakeDemo;
 
 import org.daum.library.ormH.api.PersistenceSessionStore;
 import org.daum.library.ormH.persistence.Orhm;
 import org.daum.library.ormH.utils.PersistenceException;
 import org.daum.library.replica.cache.Cache;
 import org.daum.library.replica.cache.ReplicaService;
 import org.daum.library.replica.cache.VersionedValue;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created by jed
  * User: jedartois@gmail.com
  * Date: 29/05/12
  * Time: 18:57
  */
 public class StoreImpl implements PersistenceSessionStore {
 
     private ReplicaService replicatingService=null;
 
     public StoreImpl(ReplicaService cache){
         this.replicatingService = cache;
     }
 
     @Override
     public void save(Orhm id, Object bean) throws PersistenceException {
         Cache cache = replicatingService.getCache(id.getCacheName());
         cache.put(id.getId(),bean);
     }
 
     @Override
     public void update(Orhm orhm, Object bean) throws PersistenceException {
         Cache cache = replicatingService.getCache(orhm.getCacheName());
         cache.put(orhm.getId(),bean);
     }
 
     @Override
     public void delete(Orhm id) throws PersistenceException {
         Cache cache = replicatingService.getCache(id.getCacheName());
         cache.remove(id.getId());
     }
 
     @Override
     public Object get(Orhm id) throws PersistenceException {
         Cache cache = replicatingService.getCache(id.getCacheName());
         VersionedValue data = cache.get(id.getId());
        return data.getValue();
     }
 
     @Override
     public Map<Object, Object> getAll(Orhm id) throws PersistenceException
     {
         Cache cache = replicatingService.getCache(id.getCacheName());
         if(cache != null){
             HashMap<Object,Object> result = new HashMap<Object, Object>();
             for( Object key : cache.keySet())
             {
                result.put(key,((VersionedValue)cache.get(key)).getValue());
             }       return result;
         }
           return  null;
     }
 
     @Override
     public Object lock(Orhm id) throws PersistenceException {
 
         return null;
     }
 
     @Override
     public void unlock(Orhm id) throws PersistenceException {
 
     }
 
 }
