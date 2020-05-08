 package com.janrain.backplane2.server.dao;
 
 import com.janrain.commons.supersimpledb.SimpleDBException;
 import com.janrain.commons.supersimpledb.message.Message;
 import org.apache.log4j.Logger;
 import org.jetbrains.annotations.NotNull;
 
 import java.util.*;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * Cache for (Backplane) Messages
  *
  * @author Johnny Bufu
  */
 public class MessageCache<T extends Message> {
 
     // - PUBLIC
 
     /**
      * @param maxCacheSizeBytes max cache size in bytes; 0 or negative values effectively disable the cache
      */
     public MessageCache(long maxCacheSizeBytes) {
         this.maxCacheSizeBytes = maxCacheSizeBytes;
     }
 
     public synchronized long getMaxCacheSizeBytes() {
         return maxCacheSizeBytes;
     }
 
     public synchronized void setMaxCacheSizeBytes(long maxCacheSizeBytes) {
         this.maxCacheSizeBytes = maxCacheSizeBytes;
     }
 
     public synchronized T get(String messageId) {
         return cache.get(messageId);
     }
 
     public synchronized T getFirstMessage() {
         //noinspection LoopStatementThatDoesntLoop
         for (T t : cache.values()) {
             return t;
         }
         return null;
     }
 
     public synchronized T getLastMessage() {
         T last = null;
         for (T t : cache.values()) {
             last = t;
         }
         return last;
     }
 
     /**
      * Adds new Messages to the Cache.
      *
      * All new Messages MUST compare greater than any existing message in the cache, otherwise the operation will fail.
      *
      * @param messages
      * @throws SimpleDBException if any of the provided messages compares smaller than any existing message in the cache.
      */
     public synchronized void add(List<T> messages) throws SimpleDBException {
 
         lastUpdated.set(System.currentTimeMillis());
 
         if (messages == null || messages.isEmpty()) return;
 
         Collections.sort(messages);
 
         T first = messages.get(0);
         T lastCached = getLastMessage();
         if (lastCached != null && first.compareTo(lastCached) < 0) {
             throw new SimpleDBException("Cache update rejected, newer messages exists: " + lastCached.getIdValue());
         }
 
         for(T message : messages) {
             cache.put(message.getIdValue(), message);
             size.addAndGet(message.sizeBytes());
         }
         logger.info("Added " + messages.size() + " " + first.getClass().getSimpleName() + " items to cache");
     }
 
     public synchronized @NotNull List<T> getMessagesSince(String sinceIso8601timestamp, long acceptableStaleMillis) {
         return System.currentTimeMillis() - lastUpdated.get() < acceptableStaleMillis ? getMessagesSince(sinceIso8601timestamp) :
                new ArrayList<T>();
     }
 
     /**
      * If the provided sinceIso8601timestamp is within the timestamps/IDs of the currently cached Messages,
      * all messages on or after the provided sinceIso8601timestamp are returned.
      * Otherwise an empty list is returned.
      */
     public synchronized @NotNull List<T> getMessagesSince(String sinceIso8601timestamp) {
         List<T> result = new ArrayList<T>();
         T first = getFirstMessage();
         if (first != null && first.getIdValue().compareTo(sinceIso8601timestamp) <= 0) {
             for(Map.Entry<String,T> entry : cache.entrySet()) {
                result.add(entry.getValue());
             }
         }
         return result;
     }
 
     public long getLastUpdated() {
         return lastUpdated.get();
     }
 
     // - PRIVATE
 
     private static final Logger logger = Logger.getLogger(MessageCache.class);
 
     private final LinkedHashMap<String,T> cache = new LinkedHashMap<String, T>() {
         @Override
         protected boolean removeEldestEntry(Map.Entry<String, T> eldest) {
             int removed = 0;
             Iterator<Map.Entry<String, T>> entries = entrySet().iterator();
             while ( size.get() > maxCacheSizeBytes && entries.hasNext()) {
                 Map.Entry<String, T> next = entries.next();
                 entries.remove();
                 size.addAndGet( -1 * next.getValue().sizeBytes());
                 removed++;
             }
             if (removed > 0) {
                 logger.info("Removed " + removed + " " + eldest.getClass().getSimpleName() + " items from cache, new size is: " + size() + " items / " + size.get() + " bytes");
             }
             return false;
         }
     };
 
     private final AtomicLong size = new AtomicLong(0);
     private final AtomicLong lastUpdated = new AtomicLong(0);
     private long maxCacheSizeBytes;
 }
