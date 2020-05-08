 package org.hackystat.utilities.uricache;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCache;
 import org.apache.jcs.auxiliary.disk.indexed.IndexedDiskCacheAttributes;
 import org.apache.jcs.engine.CacheElement;
 import org.apache.jcs.engine.ElementAttributes;
 import org.apache.jcs.engine.behavior.ICacheElement;
 import org.apache.jcs.engine.behavior.IElementAttributes;
 
 /**
  * Provides an easy caching mechanism which is backed by Apache JCS (Java Caching System). Once
  * cache configured and initialized, it caches pairs &lt;K, V&gt;. Note that both K and V types must
  * be Serializable.
  * 
  * <br/><br/> Using an UriCache has several advantages: it increases performance as it reduces URL
  * lookups.
  * 
  * @author <a href="mailto:seninp@gmail.com">Pavel Senin<a>
  * 
  * @param <K> type of the cache keys.
  * @param <V> type of the cache items.
  */
 public class UriCache<K, V> {
 
   /** JCS cache handler. */
   private IndexedDiskCache uriCache;
 
   /** JCS cache name. */
   private String CacheName;
 
   /** JCS cache properties set. */
   private Properties cacheProperties;
 
   /** JCS element "life time". */
   private Long maxIdleTime;
 
   /** JCS instances name holder */
   private static ArrayList<String> cacheNames = new ArrayList<String>();
 
   /**
    * Constructor, an instance of UriCache configured according to the Properties provided.
    * 
    * @param cacheName the name used for this cache identification, the cache filename will bear this
    *        name as well.
    * @param cacheProperties the cache configuration properties.
    * @throws UriCacheException when unable instantiate a cache.
    */
   public UriCache(String cacheName, UriCacheProperties cacheProperties) throws UriCacheException {
 
     // getting rid of DEBUG level log messages
     Level loggerLevel = cacheProperties.getLoggerLevel();
     Logger logger = Logger.getLogger("org.apache.jcs");
     logger.setLevel(loggerLevel);
 
     this.CacheName = cacheName;
 
     // check for the name duplication in here
     if (UriCache.cacheNames.contains(this.CacheName)) {
       throw new UriCacheException(
           "Error while attemting get a cache instance: the cache region name " + cacheName
               + " is in use.");
     }
     else {
       UriCache.cacheNames.add(this.CacheName);
     }
 
     // try {
     // CacheAccess instance = JCS.getAccess(this.CacheName);
     // }
     // catch (CacheException e) {
     // // TODO Auto-generated catch block
     // e.printStackTrace();
     // }
     // CompositeCacheManager mgr = CompositeCacheManager.getUnconfiguredInstance();
     // String[] currentCaches = mgr.getCacheNames();
 
     this.cacheProperties = cacheProperties.getProperties();
     this.maxIdleTime = Long.valueOf(this.cacheProperties
         .getProperty("jcs.region.UriCache.elementattributes.MaxLifeSeconds"));
 
     IndexedDiskCacheAttributes cattr = new IndexedDiskCacheAttributes();
     cattr.setCacheName(this.CacheName);
     cattr.setMaxKeySize(Integer.valueOf(this.cacheProperties
         .getProperty("jcs.region.UriCache.cacheattributes.MaxObjects")));
     cattr.setDiskPath(cacheProperties.getCacheStoragePath());
     this.uriCache = new IndexedDiskCache(cattr);
 
     // this.uriCache = JCS.getInstance(cacheProperties.getCacheRegionName());
     // @SuppressWarnings("unused")
     // CompositeCache cache = mgr.getCache(cacheProperties.getCacheRegionName());
     // }
     // catch (CacheException e) {
     // throw new UriCacheException(e.getMessage());
     // }
   }
 
   /**
    * Place a new item in the cache, associated with uriString. If there is currently an item
    * associated with the same uriString it is replaced.
    * 
    * @param uriString identity (URI) of the object to cache.
    * @param obj The object to cache.
    * @throws UriCacheException in case of error.
    */
   public void cache(Serializable uriString, Serializable obj) throws UriCacheException {
     //
     // I'm hardcoding parameters here, probably good move is to get these from preperties when
     // everything will be working.
     //
     IElementAttributes eAttr = new ElementAttributes();
     eAttr.setIdleTime(this.maxIdleTime);
     eAttr.setIsSpool(true);
     eAttr.setIsEternal(false);
     eAttr.setIsLateral(false);
     eAttr.setIsRemote(false);
     eAttr.setIsSpool(true);
     ICacheElement element = new CacheElement(this.CacheName, uriString, obj);
     element.setElementAttributes(eAttr);
     this.uriCache.doUpdate(element);
   }
 
   /**
    * Puts object into cache and specifies object lifetime within the cache.
    * 
    * @param uriString Identity of the object to cache.
    * @param obj Object to cache.
    * @param expirationTimeStamp Object expiration timestamp, object will be removed from the cache
    *        once System time is greater than the timestamp value.
    * @throws UriCacheException in case of error.
    */
   public void cache(Serializable uriString, Serializable obj,
       XMLGregorianCalendar expirationTimeStamp) throws UriCacheException {
 
     // first of all calculating the life time of this object from now
     Long currTime = System.currentTimeMillis();
     Long lifeTime = ((Integer) expirationTimeStamp.getMillisecond()).longValue() - currTime;
 
     IElementAttributes eAttr = new ElementAttributes();
     eAttr.setIdleTime(lifeTime);
     eAttr.setIsSpool(true);
     eAttr.setIsEternal(false);
     eAttr.setIsLateral(false);
     eAttr.setIsRemote(false);
     eAttr.setIsSpool(true);
     ICacheElement element = new CacheElement(this.CacheName, uriString, obj);
     element.setElementAttributes(eAttr);
     this.uriCache.doUpdate(element);
   }
 
   /**
    * Removes all elements from the cache.
    * 
    * @throws UriCacheException in case of error.
    */
   public void clear() throws UriCacheException {
     this.uriCache.doRemoveAll();
   }
 
   /**
    * ShutDowns the cache, flushes all items from the memory to cache and clears memory region.
    * 
    */
   public void shutdown() {
     this.uriCache.dispose();
     UriCache.cacheNames.remove(this.CacheName);
   }
 
   /**
    * Retrieves object from the cache.
    * 
    * @param uriString URI of the object to search for.
    * @return The cached object or <em>null</em> if not found.
    */
   public Serializable lookup(Serializable uriString) {
     ICacheElement ce = this.uriCache.get(uriString);
     if (null == ce) {
       return null;
     }
     else {
       return ce.getVal();
     }
   }
 
   /**
    * Removes an Object from the cache.
    * 
    * @param uriString Identity of the object to be removed.
    * @throws UriCacheException in case of error.
    */
   public void remove(Serializable uriString) throws UriCacheException {
     // try {
     this.uriCache.remove(uriString);
     // }
     // catch (CacheException e) {
     // throw new UriCacheException(e.getMessage());
     // }
   }
 
   /**
    * Sets the cache auto-expire time. Cache will auto expire elements after specified seconds to
    * reclaim space.
    * 
    * @param seconds The new ShrinkerIntervalSeconds value.
    */
   public void setMaxMemoryIdleTimeSeconds(Long seconds) {
     this.maxIdleTime = seconds;
   }
 
   /**
    * Reports the cache memory region name.
    * 
    * @return cache name.
    */
  public String getRegionName() {
     return this.CacheName;
   }
 
 }
