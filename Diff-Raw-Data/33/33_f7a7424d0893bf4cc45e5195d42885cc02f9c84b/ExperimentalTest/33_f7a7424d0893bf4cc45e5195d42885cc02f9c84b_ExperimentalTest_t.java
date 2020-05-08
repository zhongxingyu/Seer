 package domain;
 
 import org.junit.Test;
 
 import javax.cache.Cache;
 import javax.cache.CacheLoader;
 import javax.cache.CacheManager;
import javax.cache.Caching;
 import javax.cache.event.CacheEntryListener;
 import javax.cache.event.NotificationScope;
 
import java.sql.Connection;
 import java.util.Date;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNull;
 
 public class ExperimentalTest {
     private static final String CACHE_NAME = "myCache";
 
    public enum TxIsolationLevels {
        TX_NONE(java.sql.Connection.TRANSACTION_NONE),
        TX_READ_UNCOMMITTED(java.sql.Connection.TRANSACTION_READ_UNCOMMITTED),
        TX_READ_COMMITTED(java.sql.Connection.TRANSACTION_READ_COMMITTED),
        TX_READ_REPEATABLE(java.sql.Connection.TRANSACTION_REPEATABLE_READ),
        TX_READ_SERIALIZABLE(java.sql.Connection.TRANSACTION_SERIALIZABLE);

        private final int value;
        private TxIsolationLevels(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }

    }


     private void init(String cacheName, int[] keys) {
        CacheManager cacheManager = Caching.getCacheManager();
         Cache<Integer, String> myCache = cacheManager.<Integer, String>createCacheBuilder(cacheName).build();
         for (int key : keys) {
             myCache.put(key, "value" + key);
         }
     }
 
     public void doIt() {
         int[] keys = {1, 2, 3, 4, 5};
         init(CACHE_NAME, keys);
        Cache<Integer, String> myCache = Caching.getCacheManager().getCache(CACHE_NAME);
         for (int key : keys) {
             assert myCache.containsKey(key);
         }
     }
 
     enum Suits { SPADES, HEARTS, DIAMONDS, CLUBS }
 
     public void foo() {
         String cacheName = "sampleCache";
        CacheManager cacheManager = Caching.getCacheManager();
         Cache<Integer, Date> cache = cacheManager.getCache(cacheName);
         CacheLoader cl = null;
         CacheEntryListener listener1 = null;
         CacheEntryListener listener2 = null;
         if (cache == null) {
           cache = cacheManager.<Integer, Date>createCacheBuilder(cacheName).
                   setCacheLoader(cl).
                   setStoreByValue(true).
                   setReadThrough(true).
                   setWriteThrough(true).
                   setStatisticsEnabled(true).
                   setTransactionEnabled(true).
                   registerCacheEntryListener(listener1, NotificationScope.LOCAL, false).
                   registerCacheEntryListener(listener1, NotificationScope.LOCAL, false).
                   build();
         }
         Date value1 = new Date();
         Integer key = 1;
         cache.put(key, value1);
         Date value2 = cache.get(key);
     }
 
 }
