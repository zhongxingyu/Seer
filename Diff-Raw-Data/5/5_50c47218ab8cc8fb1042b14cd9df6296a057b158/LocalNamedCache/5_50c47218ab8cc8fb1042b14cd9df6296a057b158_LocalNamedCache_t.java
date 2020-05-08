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
 import java.util.Date;
 import java.util.List;
import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.log4j.Logger;
 import org.infinispan.Cache;
 import org.infinispan.container.entries.InternalCacheEntry;
 
 import com.paxxis.cornerstone.common.ScheduledExecutionPool;
 
 /**
  * Extends the capabilities of NamedCache for caches that are local only (not clustered).
  * 
  * The registered CacheExpirationListener's onExpiration() method is called asynchronously from a scheduled execution thread
  * so that the listener can't interfere with the expiration process.  However the listener's allowExpiration() method is
  * called inline with the eviction processor.  Listener implementations must respond very quickly so as not to interfere with
  * the process.
  * 
  * The eviction mechanism is altered somewhat from what the underlying Infinispan mechanism does.  Infinispan uses lazy expiration,
  * meaning that expired entries won't actually be removed until a get() is performed using the associated key.  In this implementation,
  * LocalNamedCache is proactive and will removed the entry if it has reached its expiration time.
  * 
  * @author Rob Englander
  */
 public class LocalNamedCache<K, V> extends NamedCache<K, V> {
     private static final Logger logger = Logger.getLogger(LocalNamedCache.class);
     private static final long DEFAULTEVICTIONFREQUENCY = 10000;
     private static final long MINEVICTIONFREQUENCY = 1000;
 
     private ScheduledExecutionPool scheduler = null;
     private CacheExpirationListener<V> listener = null;
     private long evictionFrequency = DEFAULTEVICTIONFREQUENCY;
     private Runnable evictionRunnable;
     
     public void setExpirationListener(CacheExpirationListener<V> listener) {
     	this.listener = listener;
     }
     
     public void setExpirationExecutor(ScheduledExecutionPool scheduler) {
     	this.scheduler = scheduler;
     }
     
     public void setEvictionFrequency(long frequency) {
     	if (frequency < MINEVICTIONFREQUENCY) {
 			throw new RuntimeException("evictionFrequency must be >= " + MINEVICTIONFREQUENCY);
     	}
     	
     	evictionFrequency = frequency;
     }
     
     public void initialize() {
 		super.initialize();
 
 		logger.debug("Initializing cache "  + getCacheName());
 		
 		if (scheduler == null) {
 			throw new RuntimeException("expirationExecutor can't be null.");
 		}
 		
 		// the additional features of LocalNamedCache are only available on non clustered caches.
 		Cache<K, ValueStorage<V>> cache = getCache();
 		if (cache.getConfiguration().getCacheMode().isClustered()) {
 			throw new RuntimeException("LocalNamedCache " + getCacheName() + " can't be clustered.");
 		}
 		
 		evictionRunnable = new Runnable() {
 			public void run() {
 				runExpiration();
 			}
 		};
 		
 		scheduler.schedule(evictionRunnable, evictionFrequency, TimeUnit.MILLISECONDS);
     }
 
     private void runExpiration() {
     	long start = System.currentTimeMillis();
     	final List<V> expired = expireEntries();
     	if (listener != null) {
     		Runnable r = new Runnable() {
 				@Override
 				public void run() {
 		    		listener.onExpiration(expired);
 				}
     		};
     		scheduler.schedule(r, 0, TimeUnit.MILLISECONDS);
     	}
     	long elapsed = System.currentTimeMillis() - start;
 		scheduler.schedule(evictionRunnable, evictionFrequency - elapsed, TimeUnit.MILLISECONDS);
     }
     
     @SuppressWarnings("unchecked")
 	private List<V> expireEntries() {
 		Cache<K, ValueStorage<V>> cache = getCache();
 		List<V> expiredValues = new ArrayList<V>();
		Set<Object> set = cache.getAdvancedCache().getDataContainer().keySet();
		for (K key : (Set<K>)set) {
 	        InternalCacheEntry entry = cache.getAdvancedCache().getDataContainer().peek(key);
 	        if (entry != null) {
 	        	if (entry.isExpired()) {
 	        		boolean expire = true;
 		        	ValueStorage<V> result = (ValueStorage<V>)entry.getValue();
 	        		if (listener != null) {
 	        			expire = listener.allowExpiration(result.getValue(), new Date(entry.getExpiryTime()));
 	        		}
 	        		if (expire) {
 			        	expiredValues.add(result.getValue());
 			        	cache.remove(key);
 	        		}
 	        	}
 	        }
 		}
 		
 		return expiredValues;
     }
 }
