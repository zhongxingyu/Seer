 package net.sparkmuse.data.util;
 
 import com.google.code.twig.ObjectDatastore;
 import com.google.appengine.api.datastore.Transaction;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Iterables;
 import com.google.common.collect.Maps;
 import com.google.common.base.Function;
 import com.google.common.base.Predicates;
 
 import javax.inject.Inject;
 
 import net.sparkmuse.common.Cache;
 import net.sparkmuse.data.entity.Entity;
 import net.sparkmuse.data.entity.CounterModel;
 import net.sparkmuse.data.entity.Wish;
 import net.sparkmuse.data.twig.DatastoreUtils;
 
 import java.util.ConcurrentModificationException;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.collections.CollectionUtils;
 
 /**
  * @author neteller
  * @created: Mar 22, 2011
  */
 public class Counters {
 
   //injected by guice, yea i know...
   public static ObjectDatastore datastore;
   public static Cache cache;
 
   public static final EntityCounter<Wish> WISH_TRYCOMMIT_COUNTER = new EntityCounter("WISH_TRYCOMMIT_COUNTER", 1);
   public static final EntityCounter<Wish> WISH_SEECOMMIT_COUNTER = new EntityCounter("WISH_SEECOMMIT_COUNTER", 1);
   public static final EntityCounter<Wish> WISH_SURVEYCOMMIT_COUNTER = new EntityCounter("WISH_SURVEYCOMMIT_COUNTER", 1);
   public static final EntityCounter<Wish> WISH_BUYCOMMIT_COUNTER = new EntityCounter("WISH_BUYCOMMIT_COUNTER", 1);
 
   private static int count(Collection<CounterModel> newCounters) {
     if (CollectionUtils.size(newCounters) == 0) return 0;
 
     //look for count in cache
     Integer count = cache.get(cacheKeyFor(Iterables.get(newCounters, 0).getQueryKey()), Integer.class);
     if (null != count) return count;
 
     //recalculate count
     Collection<String> keys = Lists.newArrayList(Iterables.transform(newCounters, new Function<CounterModel, String>(){
       public String apply(CounterModel counterModel) {
         return counterModel.getKey();
       }
     }));
     Collection<CounterModel> models = Maps.filterValues(datastore.loadAll(CounterModel.class, keys), Predicates.<CounterModel>notNull()).values();
 
     int toReturn = 0;
     for (CounterModel model: models) {
       toReturn += model.getCount();
     }
 
     //restore cache
     cache.set(cacheKeyFor(Iterables.get(newCounters, 0).getQueryKey()), toReturn);
 
     return toReturn;
   }
 
   private static void increment(CounterModel newCounter) {
     increment(newCounter, 2);
   }
 
   private static void increment(CounterModel newCounter, int retries) {
     Transaction tx = datastore.beginTransaction();
 
     CounterModel counter = datastore.load(CounterModel.class, newCounter.getKey());
     CounterModel updateCounter = null == counter? newCounter : counter;
 
     try {
       updateCounter.setCount(updateCounter.getCount() + 1);
      if (null != counter) DatastoreUtils.associate(updateCounter, datastore);
      datastore.store(updateCounter); // store must have same ancestor
       tx.commit();
     }
     catch(ConcurrentModificationException e) { //data has been written since last read
       if (tx.isActive()) tx.rollback();
       if (retries > 0) increment(newCounter, --retries);
     }
     finally {
       if (tx.isActive()) tx.rollback();
       //remove from cache
       cache.delete(cacheKeyFor(updateCounter.getQueryKey()));
     }
   }
 
   private static String cacheKeyFor(String queryKey) {
     return "CACHE:" + queryKey;
   }
 
   public static class EntityCounter<T extends Entity> {
 
     private String name;
     private int shards; //decreasing this number can cause lost data
 
     public EntityCounter(String name, int shards) {
       this.shards = shards;
       this.name = name;
     }
 
     public int count(T type) {
       return Counters.count(CounterModel.allInstances(name, shards, type.getId()));
     }
 
     public void increment(T type) {
       Counters.increment(CounterModel.singleInstance(name, shards, type.getId()));
     }
 
   }
 
 }
