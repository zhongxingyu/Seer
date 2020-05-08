 package com.thimbleware.jmemcached.test;
 
 import com.thimbleware.jmemcached.*;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 
 import static com.thimbleware.jmemcached.LocalCacheElement.Now;
 import static junit.framework.Assert.*;
 
 /**
  */
 @RunWith(Parameterized.class)
 public class CacheFIFOTest extends AbstractCacheTest {
 
     public CacheFIFOTest(CacheType cacheType, int blockSize, ProtocolMode protocolMode) {
         super(cacheType, blockSize, protocolMode);
     }
 
     @Test
     public void testExpire() {
         // max MAX_SIZE items in cache, so create fillSize items and then verify that only a MAX_SIZE are ever in the cache
         int fillSize = MAX_SIZE * 2;
 
         for (int i = 0; i < fillSize; i++) {
             String testvalue = i + "x";
             LocalCacheElement el = createElement("" + i , testvalue);
 
             assertEquals(daemon.getCache().add(el), Cache.StoreResponse.STORED);
 
             // verify that the size of the cache is correct
             int maximum = i < MAX_SIZE ? i + 1 : MAX_SIZE;
 
             assertEquals("correct number of items stored", maximum, daemon.getCache().getCurrentItems());
         }
 
         // verify that the size of the cache is correct
         assertEquals("maximum items stored", MAX_SIZE, daemon.getCache().getCurrentItems());
 
         // verify that only the last MAX_SIZE items are actually physically in there
         for (int i = 0; i < fillSize; i++) {
             CacheElement result = daemon.getCache().get(new Key(("" + i).getBytes()))[0];
             if (i < MAX_SIZE) {
                 assertTrue(i + "th result absence", result == null);
             } else {
                 assertNotNull(i + "th result should be present", result);
                 assertNotNull(i + "th result's should be present", result.getKey());
                assertEquals("key of present item should match" , ("" + i).getBytes(), result.getKey());
                 assertEquals(new String(result.getData()), i + "x");
             }
         }
         assertEquals("correct number of cache misses", fillSize - MAX_SIZE, daemon.getCache().getGetMisses());
         assertEquals("correct number of cache hits", MAX_SIZE, daemon.getCache().getGetHits());
     }
 
     private LocalCacheElement createElement(String testKey, String testvalue) {
         LocalCacheElement element = new LocalCacheElement(new Key(testKey.getBytes()), 0, Now() + (1000*60*5), 0L);
         element.setData(testvalue.getBytes());
 
         return element;
     }
 }
