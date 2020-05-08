 /*
  * This is a common dao with basic CRUD operations and is not limited to any
  * persistent layer implementation
  *
  * Copyright (C) 2010  Imran M Yousuf (imyousuf@smartitengineering.com)
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package com.smartitengineering.dao.impl.hbase.spi.impl;
 
 import com.google.inject.Inject;
 import com.smartitengineering.dao.impl.hbase.spi.AsyncExecutorService;
 import com.smartitengineering.dao.impl.hbase.spi.Callback;
 import com.smartitengineering.dao.impl.hbase.spi.LockAttainer;
 import com.smartitengineering.dao.impl.hbase.spi.SchemaInfoProvider;
 import com.smartitengineering.domain.PersistentDTO;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 import org.apache.hadoop.hbase.client.HTableInterface;
 import org.apache.hadoop.hbase.client.RowLock;
 import org.apache.hadoop.hbase.util.Bytes;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author imyousuf
  */
 public class LockAttainerImpl<T extends PersistentDTO, IdType>
     implements LockAttainer<T, IdType> {
 
   private final ConcurrentHashMap<Key<T>, MonitorableThreadLocal<Map<String, RowLock>>> locksCache =
                                                                                         new ConcurrentHashMap<Key<T>, MonitorableThreadLocal<Map<String, RowLock>>>();
   protected final Logger logger = LoggerFactory.getLogger(getClass());
   @Inject
   private SchemaInfoProvider<T, IdType> infoProvider;
   @Inject
   private AsyncExecutorService executorService;
   private final ScheduledExecutorService cleanupExecutor;
 
   {
     cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
     cleanupExecutor.scheduleAtFixedRate(new Runnable() {
 
       @Override
       public void run() {
         Iterator<Entry<Key<T>, MonitorableThreadLocal<Map<String, RowLock>>>> entries = locksCache.entrySet().iterator();
         while (entries.hasNext()) {
           Entry<Key<T>, MonitorableThreadLocal<Map<String, RowLock>>> entry = entries.next();
           if (entry.getKey().getInstance() == null || entry.getValue().container.size() <= 0) {
             entries.remove();
           }
         }
       }
     }, 5, 10, TimeUnit.MINUTES);
   }
 
   @Override
   public Map<String, RowLock> getLock(final T instance,
                                                    String... tables) {
     final Key key = new Key(instance);
     if (locksCache.containsKey(key) && locksCache.get(key).get() != null) {
       return Collections.unmodifiableMap(locksCache.get(key).get());
     }
     logger.info("Not found in cache so trying to retrieve!");
     final Map<String, Future<RowLock>> map = new LinkedHashMap<String, Future<RowLock>>();
     if (tables == null) {
       tables = new String[]{infoProvider.getMainTableName()};
     }
     if (executorService != null) {
       for (String table : tables) {
         Future<RowLock> future = executorService.executeAsynchronously(table, new Callback<RowLock>() {
 
           @Override
           public RowLock call(HTableInterface tableInterface)
               throws Exception {
             final byte[] rowIdFromRow = infoProvider.getRowIdFromRow(instance);
             if (logger.isDebugEnabled()) {
               logger.debug("Attaining lock for " + Bytes.toString(rowIdFromRow));
             }
             try {
               return tableInterface.lockRow(rowIdFromRow);
             }
             catch (Exception ex) {
               logger.warn(ex.getMessage(), ex);
               throw ex;
             }
           }
         });
         logger.debug("RECEIVED FUTURE Lock");
         map.put(table, future);
       }
     }
     final Map<String, RowLock> lockMap = new HashMap<String, RowLock>(map.size());
     for (Entry<String, Future<RowLock>> lock : map.entrySet()) {
       try {
         lockMap.put(lock.getKey(), lock.getValue().get(infoProvider.getWaitTime(), infoProvider.getUnit()));
       }
       catch (Exception ex) {
         logger.error("Error trying to get lock!", ex);
         throw new RuntimeException(ex);
       }
     }
     MonitorableThreadLocal<Map<String, RowLock>> old =
                                                  locksCache.putIfAbsent(key,
                                                                         new MonitorableThreadLocal<Map<String, RowLock>>());
     if (logger.isDebugEnabled() && old == null) {
       logger.debug("OLD THREAD LOCAL is NULL for " + key);
     }
     MonitorableThreadLocal<Map<String, RowLock>> local = locksCache.get(key);
     if (local == null) {
       logger.error("THREAD LOCAL NULL for " + key + ", " + lockMap);
       if (logger.isDebugEnabled()) {
         logger.debug("Locks " + locksCache);
         for (Map.Entry<Key<T>, MonitorableThreadLocal<Map<String, RowLock>>> lockes : locksCache.entrySet()) {
           logger.debug("Key: " + lockes.getKey() + ", Value: " + lockes.getValue() + ", " + key.equals(lockes.getKey()));
         }
       }
     }
     local.set(lockMap);
     return lockMap;
   }
 
   @Override
   public boolean evictFromCache(T instance) {
     final Key key = new Key(instance);
     final MonitorableThreadLocal<Map<String, RowLock>> local = locksCache.get(key);
     local.remove();
     return local != null;
   }
 
   @Override
   public void putLock(T instance, Map<String, RowLock> locks) {
     Key key = new Key(instance);
     locksCache.putIfAbsent(key, new MonitorableThreadLocal<Map<String, RowLock>>());
     MonitorableThreadLocal<Map<String, RowLock>> local = locksCache.get(key);
     local.set(locks);
   }
 
   @Override
   public boolean unlockAndEvictFromCache(T instance) {
    if (logger.isInfoEnabled()) {
      logger.info("Instance to remove " + instance.getClass() + " " + instance);
      logger.info("Cache " + locksCache.getClass() + " " + locksCache);
     }
     final Key key = new Key(instance);
     MonitorableThreadLocal<Map<String, RowLock>> local = locksCache.get(key);
     if (local == null) {
       logger.info("No thread local container for key!");
       return false;
     }
     Map<String, RowLock> locks = local.get();
     if (locks == null) {
       logger.info("No locks in cache!");
       return false;
     }
     else {
       local.remove();
       for (final Entry<String, RowLock> lock : locks.entrySet()) {
         executorService.executeAsynchronously(lock.getKey(), new Callback<Void>() {
 
           @Override
           public Void call(HTableInterface tableInterface)
               throws Exception {
             final RowLock lockVal = lock.getValue();
             if (logger.isInfoEnabled()) {
               logger.info("Unlocking row: " + lockVal.getLockId());
             }
             tableInterface.unlockRow(lockVal);
             return null;
           }
         });
       }      
       return false;
     }
   }
 
   private static class MonitorableThreadLocal<T> {
 
     private final AtomicLong counter = new AtomicLong(0);
     private final ThreadLocal<Long> localId = new ThreadLocal<Long>();
     private final ConcurrentHashMap<Long, T> container =
                                              new ConcurrentHashMap<Long, T>();
 
     public T get() {
       Long currentId = getCurrentId();
       return container.get(currentId);
     }
 
     protected Long getCurrentId() {
       Long currentId = localId.get();
       if (currentId == null) {
         currentId = counter.incrementAndGet();
         localId.set(currentId);
       }
       return currentId;
     }
 
     public void set(T data) {
       Long currentId = getCurrentId();
       container.put(currentId, data);
     }
 
     public void remove() {
       Long currentId = getCurrentId();
       container.remove(currentId);
     }
 
     @Override
     public boolean equals(Object obj) {
       if (obj == null) {
         return false;
       }
       if (getClass() != obj.getClass()) {
         return false;
       }
       final MonitorableThreadLocal<T> other =
                                       (MonitorableThreadLocal<T>) obj;
       if (this.container != other.container && (this.container == null || !this.container.equals(other.container))) {
         return false;
       }
       return true;
     }
 
     @Override
     public int hashCode() {
       int hash = 7;
       hash = 47 * hash + (this.container != null ? this.container.hashCode() : 0);
       return hash;
     }
   }
 
   private static class Key<T extends PersistentDTO> {
 
     private final T instance;
     protected final Logger logger = LoggerFactory.getLogger(getClass());
 
     public Key(T instance) {
       this.instance = instance;
     }
 
     public T getInstance() {
       return this.instance;
     }
 
     @Override
     public boolean equals(Object obj) {
       if (obj == null) {
         return false;
       }
       if (getClass() != obj.getClass()) {
         return false;
       }
       final Key<T> other = (Key<T>) obj;
       T thisObj = this.instance;
       T thatObj = other.instance;
       if (thisObj == null && thatObj == null) {
         return true;
       }
       else if (thisObj != null && thatObj != null) {
         return thisObj.equals(thatObj);
       }
       else {
         return false;
       }
     }
 
     @Override
     public int hashCode() {
       int hash = 5;
       return hash;
     }
 
     @Override
     public String toString() {
       final T vInstance = instance;
       return "Key{" + "instance=" + (instance != null && vInstance != null && vInstance.getId() != null ? vInstance.
                                      getId().toString() : null) + '}';
     }
   }
 }
