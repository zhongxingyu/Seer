 /**
  * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE, version 2.1, dated February 1999.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the latest version of the GNU Lesser General
  * Public License as published by the Free Software Foundation;
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program (LICENSE.txt); if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  */
 package org.jamwiki.utils;
 
 import java.io.File;
 import net.sf.ehcache.Cache;
 import net.sf.ehcache.CacheManager;
 import net.sf.ehcache.Element;
 import net.sf.ehcache.config.CacheConfiguration;
 import net.sf.ehcache.config.Configuration;
 import net.sf.ehcache.config.DiskStoreConfiguration;
 import org.jamwiki.Environment;
 
 /**
  * Implement utility functions that interact with the cache and provide the
  * infrastructure for storing and retrieving items from the cache.
  */
 public class WikiCache {
 
 	private static final WikiLogger logger = WikiLogger.getLogger(WikiCache.class.getName());
 	private static CacheManager cacheManager = null;
 
 	/** Directory for cache files. */
 	private static final String CACHE_DIR = "cache";
 
 	static {
 		WikiCache.initialize();
 	}
 
 	/**
 	 *
 	 */
 	private WikiCache() {
 	}
 
 	/**
 	 * Add an object to the cache.
 	 *
 	 * @param cacheName The name of the cache that the object is being added
 	 *  to.
 	 * @param key A String, Integer, or other object to use as the key for
 	 *  storing and retrieving this object from the cache.
 	 * @param value The object that is being stored in the cache.
 	 */
 	public static void addToCache(String cacheName, Object key, Object value) {
 		Cache cache = WikiCache.getCache(cacheName);
 		cache.put(new Element(key, value));
 	}
 
 	/**
 	 * Add an object to the cache.
 	 *
 	 * @param cacheName The name of the cache that the object is being added
 	 *  to.
 	 * @param key An int value to use as the key for storing and retrieving
 	 *  this object from the cache.
 	 * @param value The object that is being stored in the cache.
 	 */
 	public static void addToCache(String cacheName, int key, Object value) {
 		WikiCache.addToCache(cacheName, new Integer(key), value);
 	}
 
 	/**
 	 * Internal method used to retrieve a cache given the cache name.  If no
 	 * cache exists with the given name then a new cache will be created.
 	 *
 	 * @param cacheName The name of the cache to retrieve.
 	 * @return The existing cache with the given name, or a new cache if no
 	 *  existing cache exists.
 	 */
 	private static Cache getCache(String cacheName) {
 		if (!WikiCache.cacheManager.cacheExists(cacheName)) {
 			int maxSize = Environment.getIntValue(Environment.PROP_CACHE_INDIVIDUAL_SIZE);
 			int maxAge = Environment.getIntValue(Environment.PROP_CACHE_MAX_AGE);
 			int maxIdleAge = Environment.getIntValue(Environment.PROP_CACHE_MAX_IDLE_AGE);
 			Cache cache = new Cache(cacheName, maxSize, true, false, maxAge, maxIdleAge);
 			WikiCache.cacheManager.addCache(cache);
 		}
 		return WikiCache.cacheManager.getCache(cacheName);
 	}
 
 	/**
 	 * Initialize the cache, clearing any existing cache instances and loading
 	 * a new cache instance.
 	 */
 	public static void initialize() {
 		try {
 			if (WikiCache.cacheManager != null) {
 				WikiCache.cacheManager.removalAll();
 				WikiCache.cacheManager.shutdown();
 			}
 			File directory = new File(Environment.getValue(Environment.PROP_BASE_FILE_DIR), CACHE_DIR);
 			if (!directory.exists()) {
 				directory.mkdir();
 			}
 			Configuration configuration = new Configuration();
 			CacheConfiguration defaultCacheConfiguration = new CacheConfiguration();
 			defaultCacheConfiguration.setDiskPersistent(false);
 			defaultCacheConfiguration.setEternal(false);
 			defaultCacheConfiguration.setOverflowToDisk(true);
 			defaultCacheConfiguration.setMaxElementsInMemory(Environment.getIntValue(Environment.PROP_CACHE_TOTAL_SIZE));
 			defaultCacheConfiguration.setName("defaultCache");
 			configuration.addDefaultCache(defaultCacheConfiguration);
 			DiskStoreConfiguration diskStoreConfiguration = new DiskStoreConfiguration();
 //			diskStoreConfiguration.addExpiryThreadPool(new ThreadPoolConfiguration("", new Integer(5), new Integer(5)));
 //			diskStoreConfiguration.addSpoolThreadPool(new ThreadPoolConfiguration("", new Integer(5), new Integer(5)));
 			diskStoreConfiguration.setPath(directory.getPath());
 			configuration.addDiskStore(diskStoreConfiguration);
 			WikiCache.cacheManager = new CacheManager(configuration);
 		} catch (Exception e) {
 			logger.severe("Initialization error in WikiCache", e);
 		}
 	}
 
 	/**
 	 * Given a virtual wiki name and a topic name, generate a unique key value
 	 * that can be used to store and retrieve cache objects.
 	 *
 	 * @param virtualWiki The virtual wiki name for the key value being
 	 *  created.
 	 * @param topicName The name of the topic for the key value being created.
 	 * @return The generated key value.
 	 */
 	public static String key(String virtualWiki, String topicName) {
 		return virtualWiki + "/" + topicName;
 	}
 
 	/**
 	 * Remove a cache with the given name from the system, freeing any
 	 * resources used by that cache.
 	 *
 	 * @param cacheName The name of the cache being removed.
 	 */
 	public static void removeCache(String cacheName) {
 		WikiCache.cacheManager.removeCache(cacheName);
 	}
 
 	/**
 	 * Remove a value from the cache with the given key and name.
 	 *
 	 * @param cacheName The name of the cache from which the object is being
 	 *  removed.
 	 * @param key The key for the record that is being removed from the cache.
 	 */
 	public static void removeFromCache(String cacheName, Object key) {
 		Cache cache = WikiCache.getCache(cacheName);
 		cache.remove(key);
 	}
 
 	/**
 	 * Remove a value from the cache with the given key and name.
 	 *
 	 * @param cacheName The name of the cache from which the object is being
 	 *  removed.
 	 * @param key The key for the record that is being removed from the cache.
 	 */
 	public static void removeFromCache(String cacheName, int key) {
 		WikiCache.removeFromCache(cacheName, new Integer(key));
 	}
 
 	/**
 	 * Retrieve a cached element from the cache.  This method will return
 	 * <code>null</code> if no matching element is cached, an element with
 	 * no value if a <code>null</code> value is cached, or an element with a
 	 * valid object value if such an element is cached.
 	 *
 	 * @param cacheName The name of the cache from which the object is being
 	 *  retrieved.
 	 * @param key The key for the record that is being retrieved from the
 	 *  cache.
 	 * @return A new <code>Element</code> object containing the key and cached
 	 *  object value.
 	 */
 	public static Element retrieveFromCache(String cacheName, Object key) {
 		Cache cache = WikiCache.getCache(cacheName);
 		return cache.get(key);
 	}
 
 	/**
 	 * Retrieve a cached element from the cache.  This method will return
 	 * <code>null</code> if no matching element is cached, an element with
 	 * no value if a <code>null</code> value is cached, or an element with a
 	 * valid object value if such an element is cached.
 	 *
 	 * @param cacheName The name of the cache from which the object is being
 	 *  retrieved.
 	 * @param key The key for the record that is being retrieved from the
 	 *  cache.
 	 * @return A new <code>Element</code> object containing the key and cached
 	 *  object value.
 	 */
 	public static Element retrieveFromCache(String cacheName, int key) {
 		return WikiCache.retrieveFromCache(cacheName, new Integer(key));
 	}
 }
