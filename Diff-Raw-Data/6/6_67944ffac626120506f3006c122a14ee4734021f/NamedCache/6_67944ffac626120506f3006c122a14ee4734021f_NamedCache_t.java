 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.paxxis.cornerstone.cache;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 import org.infinispan.Cache;
 import org.infinispan.container.entries.InternalCacheEntry;
 
 /**
  * @author Rob Englander
  */
 public class NamedCache<K, V> implements com.paxxis.cornerstone.cache.Cache<K, V> {
     private static final Logger logger = Logger.getLogger(NamedCache.class);
         
     /** the name of the cache */
     private String cacheName = null;
 
     /** the associated cache manager */
     private CacheManager cacheManager = null;
 
     /** the cache itself */
     private Cache<K, ValueStorage<V>> cache = null;
 
 	private List<CacheListener> listeners = new ArrayList<CacheListener>();
 
     public void setCacheName(String name) {
         cacheName = name;
     }
     
     public String getCacheName() {
     	return cacheName;
     }
 
     protected Cache<K, ValueStorage<V>> getCache() {
     	return cache;
     }
     
     public void setCacheManager(CacheManager cacheManager) {
         this.cacheManager = cacheManager;
     }
     
     @Override
     public void addListener(CacheListener listener) {
         this.listeners.add(listener);
     }
     
 	public void setListeners(List<CacheListener> listeners) {
 		this.listeners.clear();
 		this.listeners.addAll(listeners);
 	}
 
 	@SuppressWarnings("unchecked")
     public void initialize() {
         try {
             cache = cacheManager.getCache(cacheName);
 			for (CacheListener listener : listeners) {
 				cache.addListener(listener);
 			}
         } catch (Exception e) {
             String msg = e.getLocalizedMessage();
             logger.error(msg);
             throw new RuntimeException(msg);
         }
     }
 
 	public void clear() {
 		cache.clear();
 	}
 	
 	public boolean isExpired(K key) {
         boolean expired = false;
         InternalCacheEntry entry = cache.getAdvancedCache().getDataContainer().peek(key);
         if (null != entry) {
         	expired = entry.isExpired();
         }
         
         return expired;
 	}
 	
 	public boolean isNearExpiration(K key, long time) {
 		boolean near = false;
         InternalCacheEntry entry = cache.getAdvancedCache().getDataContainer().peek(key);
         if (null != entry && entry.canExpire()) {
         	if (!entry.isExpired()) {
 	        	long expiry = entry.getExpiryTime();
 	        	long now = System.currentTimeMillis();
 	        	if ((expiry - now) < time) {
 	        		near = true;
 	        	}
         	}
         }
         
         return near;
 	}
 	
     /**
      * Retrieves a cache entry in the same way as get() except that it 
      * does not update or reorder any of the internal constructs.
      */
 	@Override
     @SuppressWarnings("unchecked")
 	public V peek(K key) {
         ValueStorage<V> result = null;
         InternalCacheEntry entry = cache.getAdvancedCache().getDataContainer().peek(key);
         if (entry != null) {
         	result = (ValueStorage<V>)entry.getValue();
         }
         
        return result == null ? null : result.getValue();
     }
     
     protected ValueStorage<V> put(K key, ValueStorage<V> content) {
     	return cache.put(key, content);
     }
 
     protected ValueStorage<V> put(K key, ValueStorage<V> content, long lifespan, TimeUnit lifespanUnits) {
     	return cache.put(key, content, lifespan, lifespanUnits);
     }
 
     protected ValueStorage<V> put(K key, ValueStorage<V> content, long lifespan, 
             TimeUnit lifespanUnits, long maxIdle, TimeUnit maxIdleUnits) {
     	return cache.put(key, content, lifespan, lifespanUnits, maxIdle, maxIdleUnits);
     }
     
     protected ValueStorage<V> putIfAbsent(K key, ValueStorage<V> content) {
     	return cache.putIfAbsent(key, content);
     }
 
     protected ValueStorage<V> putIfAbsent(K key, ValueStorage<V> content, long lifespan, TimeUnit lifespanUnits) {
     	return cache.putIfAbsent(key, content, lifespan, lifespanUnits);
     }
 
     protected ValueStorage<V> putIfAbsent(K key, ValueStorage<V> content, long lifespan, 
             TimeUnit lifespanUnits, long maxIdle, TimeUnit maxIdleUnits) {
     	return cache.putIfAbsent(key, content, lifespan, lifespanUnits, maxIdle, maxIdleUnits);
     }
     
     @Override
     public V put(K key, V content) {
         ValueStorage<V> value = put(key, createValueStorage(content));
         return value == null ? null : value.getValue();
     }
 
     @Override
     public V put(K key, V content, long lifespan, TimeUnit lifespanUnits) {
     	ValueStorage<V> value = put(key, createValueStorage(content), lifespan, lifespanUnits);
     	return value == null ? null : value.getValue();
     }
 
     @Override
     public V put(K key, V content, long lifespan, TimeUnit lifespanUnits, long maxIdle, TimeUnit maxIdleUnits) {
     	ValueStorage<V> value = put(key, createValueStorage(content), lifespan, lifespanUnits, maxIdle, maxIdleUnits);
     	return value == null ? null : value.getValue();
     }
     
     @Override
     public V putIfAbsent(K key, V content) {
     	ValueStorage<V> value = putIfAbsent(key, createValueStorage(content));
     	return value == null ? null : value.getValue();
     }
 
     @Override
     public V putIfAbsent(K key, V content, long lifespan, TimeUnit lifespanUnits) {
     	ValueStorage<V> value = putIfAbsent(key, createValueStorage(content), lifespan, lifespanUnits);
     	return value == null ? null : value.getValue();
     }
 
     @Override
     public V putIfAbsent(K key, V content, long lifespan, TimeUnit lifespanUnits, long maxIdle, TimeUnit maxIdleUnits) {
     	ValueStorage<V> value = putIfAbsent(key, createValueStorage(content), lifespan, lifespanUnits, maxIdle, maxIdleUnits);
     	return value == null ? null : value.getValue();
     }
 
     @Override
     public V get(K key) throws CacheException {
         ValueStorage<V> value = getValue(key);
         return value == null ? null : value.getValue();
     }
     
     protected ValueStorage<V> getValue(K key) throws CacheException {
         if (key == null) {
             return null;
         }
         
         return cache.get(key);
     }
     
     public V remove(K key) {
        ValueStorage<V> value = cache.remove(key);
    	return value == null ? null : value.getValue();
     }
     
     public Set<K> getKeys() {
     	return cache.keySet();
     }
     
     public Collection<V> getEntries() {
     	List<V> list = new ArrayList<V>();
     	Set<K> keys = cache.keySet();
     	for (K key : keys) {
     		V value = this.peek(key);
     		list.add(value);
     	}
 
     	return list;
     }
 
     protected ValueStorage<V> createValueStorage(final V value) {
         return new ValueStorage<V>() {
             private static final long serialVersionUID = 1L;
 
             @Override
             public V getValue() {
                 return value;
             }
         };
     }
 }
