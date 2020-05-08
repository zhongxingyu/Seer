 package org.hackystat.utilities.uricache;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertTrue;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * Tests the UriCache class.
  * 
  * @author Philip Johnson
  * 
  */
 public class TestNewUriCache {
   
   private static final String testSubDir = "TestUriCache";
 
   /**
    * Test simple cache put and get.
    */
   @Test
   public void testSimpleCache() {
     // Create a cache
     String key = "key";
     String value = "value";
     NewUriCache cache = new NewUriCache("TestSimpleCache", testSubDir);
     cache.put(key, value);
     assertEquals("Checking simple get", value, cache.get(key));
     cache.remove(key);
     assertNull("Checking non-existant get", cache.get(key));
   }
   
   /**
    * Tests addition and deletion of hierarchical cache entries. 
    * The idea is that if you use keys with the ":" to separate parts, you can delete
    * collections of cache elements with one call by specifying the colon delimited hierarchy.
    * See http://jakarta.apache.org/jcs/faq.html#hierarchical-removal for details. 
    * Unfortunately this does not work.
    */
   @Ignore
   @Test
   public void testHierarchicalKeyCacheRemoval() {
     // Create a cache. 
     NewUriCache cache = new NewUriCache("HierarchicalKeyCache", testSubDir, 1D, 1L);
     cache.clear();
     Logger.getLogger("org.apache.jcs").setLevel(Level.ALL);
     cache.setLoggingLevel("ALL");
     // Add three elements.
     cache.put("foo:bar:baz", "one");
     cache.put("foo:bar:qux", "two");
     cache.put("bar:quxx", "three");
     cache.remove("foo:bar:");
     System.out.println(cache.get("foo:bar:baz"));
     System.out.println(cache.get("foo:bar:qux"));
     assertNull("Checking foo:bar:baz is gone", cache.get("foo:bar:baz"));
     assertNull("Checking foo:bar:qux is gone", cache.get("foo:bar:qux"));
     assertEquals("Checking foo:qux is still there", "three", cache.get("bar:quxx"));
   }
   
   /**
    * Tests addition and deletion of grouped cache entries. 
    */
   @Test
   public void testGroupedElementsCache() {
     // Create a cache. 
    NewUriCache cache = new NewUriCache("GroupedKeyCache", testSubDir, 1D, 1000L);
     cache.clear();
     String group1 = "group1";
     cache.putInGroup("one", group1, "1");
     cache.putInGroup("two", group1, "2");
     cache.putInGroup("three", "group2", "3");
     assertEquals("Test simple group retrieval1", "1", cache.getFromGroup("one", group1));
     assertEquals("Test simple group retrieval2", "2", cache.getFromGroup("two", group1));
     assertEquals("Test simple group retrieval3", "3", cache.getFromGroup("three", "group2"));
     assertNull("Test non-group retrieval won't get the element", cache.get("one"));
     assertEquals("Test group1 keyset", 2, cache.getGroupKeys(group1).size());
     assertEquals("Test group2 keyset", 1, cache.getGroupKeys("group2").size());
     cache.removeFromGroup("one", group1);
     assertEquals("Test new group1 keyset", 1, cache.getGroupKeys(group1).size());
     assertTrue("Test group1 keyset element", cache.getGroupKeys(group1).contains("two"));
   }
   
  
   /**
    * Test simple cache put and get.
    */
   @Test
   public void testDisposeCache() {
     // Create a cache
     String key = "key";
     String value = "value";
     String cacheName = "TestDisposeCache";
     NewUriCache cache = new NewUriCache(cacheName, testSubDir);
     cache.put(key, value);
     assertEquals("Checking simple get", value, cache.get(key));
     // now dispose and try again.
     NewUriCache.dispose(cacheName);
     cache = new NewUriCache(cacheName, testSubDir);
     cache.put(key, value);
     assertEquals("Checking simple get 2", value, cache.get(key));
   }
 
   /**
    * Test use of disk cache.
    */
   @Test
   public void testDiskCache() {
     // Create a cache
     Double maxLifeDays = 1D;
     Long capacity = 100L;
     NewUriCache cache = new NewUriCache("TestDiskCache", testSubDir, maxLifeDays, capacity);
     // Now do a loop and put more than 100 items in it.
     for (int i = 1; i < 200; i++) {
       cache.put(i, i);
     }
     // Now check to see that we can retrieve all 200 items.
     for (int i = 1; i < 200; i++) {
       assertEquals("Checking retrieval", i, cache.get(i));
     }
   }
   
   /**
    * Test that we can expire elements from the cache. 
    * This test no longer works because we've changed the 
    * @throws Exception If problems occur.
    */
   @Test
   public void testElementExpiration() throws Exception {
     // Create a cache with maxLife of 1 second and a maximum of 100 in-memory elements.
     Double maxLifeDays = 1.157e-5D;
     Long capacity = 100L;
     NewUriCache cache = new NewUriCache("TestExpiration", testSubDir, maxLifeDays, capacity);
     // Now do a loop and put 200 items in it.
     for (int i = 1; i < 200; i++) {
       cache.put(i, i);
     }
     // Add an element that expires in 3 seconds. 
     cache.put(300, 300, 8.4e-4D);
     // Now wait for two seconds.
     Thread.sleep(2000);
     // Now check to see that all of the items with the default maxLife are gone.
     for (int i = 1; i < 200; i++) {
       assertNull("Checking retrieval", cache.get(i));
     }
     // And check that our item with the custom maxLife time is still there.
     assertEquals("Check non-expired element", 300, cache.get(300));
     // Now wait one more second, enough time for our custom maxLife time to be exceeded.
     Thread.sleep(1001);
     // Now see that our element with the custom maxLife time is now gone.
     assertNull("Check expired element", cache.get(300));
   }
 }
