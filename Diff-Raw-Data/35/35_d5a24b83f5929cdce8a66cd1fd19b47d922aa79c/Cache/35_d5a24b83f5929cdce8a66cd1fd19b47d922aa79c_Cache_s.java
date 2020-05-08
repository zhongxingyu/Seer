 /**
  *  Copyright 2008 ThimbleWare Inc.
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  */
 package com.thimbleware.jmemcached;
 
 import org.apache.mina.common.ByteBuffer;
 
 import static java.lang.Integer.parseInt;
 import static java.lang.String.valueOf;
 import java.util.Set;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 /**
  */
 public class Cache {
     public int currentItems;
     public int totalItems;
     public int getCmds;
     public int setCmds;
     public int getHits;
     public int getMisses;
 
     public long casCounter;
 
     protected CacheStorage cacheStorage;
 
     public enum StoreResponse {
         STORED, NOT_STORED, EXISTS, NOT_FOUND
     }
 
     public enum DeleteResponse {
         DELETED, NOT_FOUND
     }
 
     /**
      * Read-write lock allows maximal concurrency, since readers can share access;
      * only writers need sole access.
      */
     private final ReadWriteLock cacheReadWriteLock;
 
     /**
      * Construct the server session handler
      *
      * @param cacheStorage the cache to use
      */
     public Cache(CacheStorage cacheStorage) {
         initStats();
         this.cacheStorage = cacheStorage;
 
         cacheReadWriteLock = new ReentrantReadWriteLock();
     }
 
     /**
      * Handle the deletion of an item from the cache.
      *
      * @param key the key for the item
      * @param time only delete the element if time (time in seconds)
      * @return the message response
      */
     public DeleteResponse delete(String key, int time) {
         try {
             startCacheWrite();
 
             if (is_there(key)) {
                 if (time != 0) {
                     MCElement el = this.cacheStorage.get(key);
                     if (el.expire == 0 || el.expire > (Now() + time)) {
                         el.expire = Now() + time; // update the expire time
                         this.cacheStorage.put(key, el);
                     }// else it expire before the time we were asked to expire it
                 } else {
                     this.cacheStorage.remove(key); // just remove it
                 }
                 return DeleteResponse.DELETED;
             } else {
                 return DeleteResponse.NOT_FOUND;
             }
         } finally {
             finishCacheWrite();
         }
     }
 
     /**
      * Add an element to the cache
      *
      * @param e the element to add
     * @return the message response string
      */
     protected StoreResponse add(MCElement e) {
         try {
             startCacheRead();
             if (is_there(e.keystring)) return set(e);
             else return StoreResponse.NOT_STORED;
         } finally {
             finishCacheRead();
         }
     }
 
     /**
      * Replace an element in the cache
      *
      * @param e the element to replace
     * @return the message response string
      */
     public StoreResponse replace(MCElement e) {
         try {
             startCacheRead();
             if (is_there(e.keystring)) return set(e);
             else return StoreResponse.NOT_STORED;
         } finally {
             finishCacheRead();
         }
     }
 
     /**
      * Append bytes to the end of an element in the cache
      *
      * @param element the element to append
     * @return message response
      */
     public StoreResponse append(MCElement element) {
        // TODO verify that the cache is performing entry expiries based on the new size of the item...
         try {
             startCacheWrite();
             MCElement ret = get(element.keystring);
             if (ret == null)
                 return StoreResponse.NOT_FOUND;
             else {
                 ret.data_length += element.data_length;
                 ByteBuffer b = ByteBuffer.allocate(ret.data_length);
                 b.put(ret.data);
                 b.put(element.data);
                 ret.data = new byte[ret.data_length];
                 b.flip();
                 b.get(ret.data);
                 ret.cas_unique++;
                 this.cacheStorage.put(ret.keystring, ret);
 
                 return StoreResponse.STORED;
             }
         } finally {
             finishCacheWrite();
         }
     }
 
     public StoreResponse prepend(MCElement element) {
         try {
             startCacheWrite();
             MCElement ret = get(element.keystring);
             if (ret == null)
                 return StoreResponse.NOT_FOUND;
             else {
                 ret.data_length += element.data_length;
                 ByteBuffer b = ByteBuffer.allocate(ret.data_length);
                 b.put(element.data);
                 b.put(ret.data);
                 ret.data = new byte[ret.data_length];
                 b.flip();
                 b.get(ret.data);
                 ret.cas_unique++;
                 this.cacheStorage.put(ret.keystring, ret);
 
                 return StoreResponse.STORED;
             }
         } finally {
             finishCacheWrite();
         }
     }
     /**
      * Set an element in the cache
      *
      * @param e the element to set
     * @return the message response string
      */
     protected StoreResponse set(MCElement e) {
         try {
             startCacheWrite();
             setCmds += 1;//update stats
 
             // increment the CAS counter; put in the new CAS
             e.cas_unique = casCounter++;
 
             this.cacheStorage.put(e.keystring, e);
             return StoreResponse.STORED;
         } finally {
             finishCacheWrite();
         }
     }
 
     public StoreResponse cas(Long cas_key, MCElement e) {
         try {
             startCacheWrite();
             // have to get the element
             MCElement element = get(e.keystring);
             if (element == null)
                 return StoreResponse.NOT_FOUND;
 
             if (element.cas_unique == cas_key) {
                 // cas_unique matches, now set the element
                 return set(e);
             } else {
                 // cas didn't match; someone else beat us to it
                 return StoreResponse.EXISTS;
             }
 
         } finally {
             finishCacheWrite();
         }
     }
 
     /**
      * Increment an (integer) element inthe cache
      * @param key the key to increment
      * @param mod the amount to add to the value
      * @return the message response
      */
     protected Integer get_add(String key, int mod) {
         try {
             startCacheWrite();
             MCElement e = this.cacheStorage.get(key);
             if (e == null) {
                 getMisses += 1;//update stats
                 return null;
             }
             if (e.expire != 0 && e.expire < Now()) {
                 //logger.info("FOUND BUT EXPIRED");
                 getMisses += 1;//update stats
                 return null;
             }
             // TODO handle parse failure!
             int old_val = parseInt(new String(e.data)) + mod; // change value
             if (old_val < 0) {
                 old_val = 0;
             } // check for underflow
             e.data = valueOf(old_val).getBytes(); // toString
             e.data_length = e.data.length;
 
             // assign new cas id
             e.cas_unique = casCounter++;
 
             this.cacheStorage.put(e.keystring, e); // save new value
             return old_val;
         } finally {
             finishCacheWrite();
         }
     }
 
 
     /**
      * Check whether an element is in the cache and non-expired
      * @param key the key for the element to lookup
      * @return whether the element is in the cache and is live
      */
     protected boolean is_there(String key) {
         try {
             startCacheRead();
             MCElement e = this.cacheStorage.get(key);
             return e != null && !(e.expire != 0 && e.expire < Now());
         } finally {
             finishCacheRead();
         }
     }
 
     /**
      * Get an element from the cache
      * @param key the key for the element to lookup
      * @return the element, or 'null' in case of cache miss.
      */
     protected MCElement get(String key) {
         getCmds += 1;//updates stats
 
         try {
             startCacheRead();
             MCElement e = this.cacheStorage.get(key);
 
 
             if (e == null) {
                 getMisses += 1;//update stats
                 return null;
             }
             if (e.expire != 0 && e.expire < Now()) {
                 getMisses += 1;//update stats
 
                 // TODO shouldn't this actually remove the item from cacheStorage since it's expired?
                 return null;
             }
             getHits += 1;//update stats
             return e;
         } finally {
             finishCacheRead();
         }
     }
 
     /**
      * Flush all cache entries
      * @return command response
      */
     protected boolean flush_all() {
         return flush_all(0);
     }
 
     /**
      * Flush all cache entries with a timestamp after a given expiration time
      * @param expire the flush time in seconds
      * @return command response
      */
     protected boolean flush_all(int expire) {
         // TODO implement this, it isn't right... but how to handle efficiently? (don't want to linear scan entire cacheStorage)
         try {
             startCacheWrite();
             this.cacheStorage.flushAll();
         } finally {
             finishCacheWrite();
         }
         return true;
     }
 
     /**
      * @return the current time in seconds (from epoch), used for expiries, etc.
      */
     protected final int Now() {
         return (int) (System.currentTimeMillis() / 1000);
     }
 
     /**
      * Initialize all statistic counters
      */
     protected void initStats() {
         currentItems = 0;
         totalItems = 0;
         getCmds = setCmds = getHits = getMisses = 0;
     }
 
 
     public Set<String> keys() {
         try { startCacheRead();
             return cacheStorage.keys();
         } finally {
             finishCacheRead();
         }
     }
 
     public long getCurrentItems() {
         try {
             startCacheRead();
             return  this.cacheStorage.count();
         } finally {
             finishCacheRead();
         }
     }
 
     public long getLimitMaxBytes() {
         try {
             startCacheRead();
             return this.cacheStorage.maxSize();
         } finally {
             finishCacheRead();
         }
     }
 
     public long getCurrentBytes() {
         try {
             startCacheRead();
             return this.cacheStorage.size();
         } finally {
             finishCacheRead();
         }
     }
 
     /**
      * Blocks of code in which the contents of the cache
      * are examined in any way must be surrounded by calls to <code>startRead</code>
      * and <code>finishRead</code>. See documentation for ReadWriteLock.
      */
     private void startCacheRead() {
         cacheReadWriteLock.readLock().lock();
 
     }
 
     /**
      * Blocks of code in which the contents of the cache
      * are examined in any way must be surrounded by calls to <code>startRead</code>
      * and <code>finishRead</code>. See documentation for ReadWriteLock.
      */
     private void finishCacheRead() {
         cacheReadWriteLock.readLock().unlock();
     }
 
 
     /**
      * Blocks of code in which the contents of the cache
      * are changed in any way must be surrounded by calls to <code>startWrite</code> and
      * <code>finishWrite</code>. See documentation for ReadWriteLock.
      * protect the higher layers from implementation details.
      */
     private void startCacheWrite() {
         cacheReadWriteLock.writeLock().lock();
 
     }
 
     /**
      * Blocks of code in which the contents of the cache
      * are changed in any way must be surrounded by calls to <code>startWrite</code> and
      * <code>finishWrite</code>. See documentation for ReadWriteLock.
      */
     private void finishCacheWrite() {
         cacheReadWriteLock.writeLock().unlock();
     }
 
     public int getTotalItems() {
         return totalItems;
     }
 
     public int getGetCmds() {
         return getCmds;
     }
 
     public int getSetCmds() {
         return setCmds;
     }
 
     public int getGetHits() {
         return getHits;
     }
 
     public int getGetMisses() {
         return getMisses;
     }
 
 }
