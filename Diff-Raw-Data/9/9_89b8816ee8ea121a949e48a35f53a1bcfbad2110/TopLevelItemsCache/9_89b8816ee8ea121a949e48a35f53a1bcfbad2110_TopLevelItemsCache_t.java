 /*
  * Copyright (c) 2013 Hudson.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Hudson - initial API and implementation and/or initial documentation
  */
 package hudson.model;
 
 import com.google.common.cache.CacheBuilder;
 import com.google.common.cache.CacheLoader;
 import com.google.common.cache.LoadingCache;
 import com.google.common.cache.RemovalListener;
 import com.google.common.cache.RemovalNotification;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * A cache for {@link TopLevelItems} object that are directly held by
  * the {@link Hudson} instance.
  * 
  * This class is package private.
  * 
  * @author Roy Varghese
  */
 class TopLevelItemsCache {
     
     // Cache parameters
     private final static int EVICT_IN_SECONDS = 60;
     private final static int INITIAL_CAPACITY = 1024;
     private final static int MAX_ENTRIES = 1000;
     
     final LoadingCache<LazyTopLevelItem.Key, TopLevelItem> cache;
     
     TopLevelItemsCache() { 
         
        cache = CacheBuilder.newBuilder()
                 .initialCapacity(INITIAL_CAPACITY)
                 .expireAfterAccess(EVICT_IN_SECONDS, TimeUnit.SECONDS)
                 .maximumSize(MAX_ENTRIES)
                 .softValues()
                 .removalListener(new RemovalListener<LazyTopLevelItem.Key, TopLevelItem>() {
 
                     @Override
                     public void onRemoval(RemovalNotification<LazyTopLevelItem.Key, TopLevelItem> notification) {
                         // System.out.println("*** Removed from cache " + notification.getKey().name );
                     }
                     
                 })
                 .build(new CacheLoader<LazyTopLevelItem.Key, TopLevelItem>() {
                     
                     Map<String, Integer> map = new HashMap<String, Integer>();
 
                     @Override
                     public TopLevelItem load(LazyTopLevelItem.Key key) throws Exception {
                         TopLevelItem item = (TopLevelItem) key.configFile.read();
                         item.onLoad(key.parent, key.name);
                         return item;
                     }
                     
                 });
         
         
 
     }
     
     
     
     TopLevelItem get(LazyTopLevelItem.Key key) {
         try {
             return cache.get(key);
         } catch (ExecutionException ex) {
             Logger.getLogger(TopLevelItemsCache.class.getName()).log(Level.SEVERE, null, ex);
             return null;
         }
 
     }
     
     void put(LazyTopLevelItem.Key key, TopLevelItem item) {
         cache.put(key, item);
     }
     
 }
