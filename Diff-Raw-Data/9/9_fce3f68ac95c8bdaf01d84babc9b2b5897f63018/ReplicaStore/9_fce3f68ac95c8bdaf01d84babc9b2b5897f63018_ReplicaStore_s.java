 package org.daum.library.ormH.store;
 
 import org.daum.library.ormH.api.PersistenceSessionStore;
 import org.daum.library.ormH.persistence.Orhm;
 import org.daum.library.ormH.utils.PersistenceException;
 import org.daum.library.replica.cache.Cache;
 import org.daum.library.replica.cache.ReplicaService;
 
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: jed
  * Date: 22/06/12
  * Time: 10:35
  * To change this template use File | Settings | File Templates.
  */
 public class ReplicaStore  implements PersistenceSessionStore {
 
     private ReplicaService replicatingService=null;
 
    public ReplicaStore(ReplicaService cache)
    {
         this.replicatingService = cache;

     }
 
     @Override
     public void save(Orhm id, Object bean) throws PersistenceException {
         if(replicatingService == null){
             throw new PersistenceException("service is null");
         }
         Cache cache = replicatingService.getCache(id.getCacheName());
         cache.put(id.getId(),bean);
     }
 
     @Override
     public void update(Orhm orhm, Object bean) throws PersistenceException {
         if(replicatingService == null){
             throw new PersistenceException("service is null");
         }
         Cache cache = replicatingService.getCache(orhm.getCacheName());
         cache.put(orhm.getId(),bean);
     }
 
     @Override
     public void delete(Orhm id) throws PersistenceException {
         if(replicatingService == null){
             throw new PersistenceException("service is null");
         }
         Cache cache = replicatingService.getCache(id.getCacheName());
         cache.remove(id.getId());
     }
 
     @Override
     public Object get(Orhm id) throws PersistenceException {
         if(replicatingService == null){
             throw new PersistenceException("service is null");
         }
         Cache cache = replicatingService.getCache(id.getCacheName());
         return cache.get(id.getId()).getValue();
     }
 
     @Override
     public Map<Object, Object> getAll(Orhm id) throws PersistenceException
     {
         if(replicatingService == null)
         {
             throw new PersistenceException("service is null");
         }
         HashMap<Object,Object> result = new HashMap<Object, Object>();
         Cache cache = replicatingService.getCache(id.getCacheName());
         if(cache != null)
         {
             for( Object key : cache.keySet())
             {
                 result.put(key,cache.get(key).getValue());
             }       return result;
         }
         return  result;
     }
 
     @Override
     public Object lock(Orhm id) throws PersistenceException {
 
         return null;
     }
 
     @Override
     public void unlock(Orhm id) throws PersistenceException {
 
     }
 
 }
