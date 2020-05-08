 package org.javaz.cache;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 
 /**
  *
  */
 public class CacheImpl implements CacheI
 {
     /**
      * Simple const, to make code clear
      */
     private static final long NO_OBJECTS = -1;
     /**
      * TTL value for objects
      */
     private long timeToLive = 0;
     /**
      * LOCK object for make sure that we are running
      * clearExpired() in only one instance
      */
    private final Integer CLEAR_LOCK = 0;
     /**
      * Current state of LOCK regarding clearExpired()
      */
     private Integer LOCK_STATE = 0;
 
     /**
      * Constants for LOCK regarding clearExpired()
      */
     private static final Integer LOCK_STATE_UNLOCKED = 0;
     private static final Integer LOCK_STATE_LOCKED = 1;
 
     /**
      * This property tells us when next object will expire
      * and special value NO_OBJECTS tells us that cache is pretty empty.
      */
     private long nextTimeSomeExpired = NO_OBJECTS;
 
     /**
      * This is our cache
      */
     private final HashMap objects = new HashMap();
 
     /**
      * We store here object key and time when it was created;
      */
     private final HashMap<Object, Long> objectTimeStamps = new HashMap<Object, Long>();
 
     public long getTimeToLive()
     {
         return timeToLive;
     }
 
     /**
      * Store TTL as property. If it changed - we must found again nextTimeSomeExpired.
      */
     public void setTimeToLive(long timeToLive)
     {
         boolean needRecheckNextExpire = this.timeToLive != timeToLive;
         this.timeToLive = timeToLive;
         if (needRecheckNextExpire)
         {
             findNextExpireTime();
         }
     }
 
     /**
      * Method to store value in cache, if this value first in cache - also mark nextTimeSomeExpired
      * as its creating time;
      *
      * @param key,  may not be null
      * @param value - any value object
      * @return old value or null
      */
     public Object put(Object key, Object value)
     {
         long objectStamp = System.currentTimeMillis();
         synchronized (objects)
         {
             objectTimeStamps.put(key, objectStamp);
             if (nextTimeSomeExpired == NO_OBJECTS)
             {
                 nextTimeSomeExpired = objectStamp;
             }
             return objects.put(key, value);
         }
     }
 
     /**
      * Clear all expired values, only if needed (this provided by storing time of nearest object expiration)
      * After removal objects calls findNextExpireTime(), to set next time to clearExpired.
      * <p/>
      * This method called in almost all methods (containsKey, containsValue, get, isEmpty)
      */
     public void clearExpired()
     {
         synchronized (CLEAR_LOCK)
         {
             if (LOCK_STATE.equals(LOCK_STATE_LOCKED))
             {
                 return;
             }
             LOCK_STATE = LOCK_STATE_LOCKED;
         }
         long expireRunStamp = System.currentTimeMillis();
         if (nextTimeSomeExpired != NO_OBJECTS && expireRunStamp >= nextTimeSomeExpired)
         {
             synchronized (objects)
             {
                 try
                 {
                     //remove obj
                     ArrayList set = new ArrayList(objects.keySet());
                     for (Iterator iterator = set.iterator(); iterator.hasNext(); )
                     {
                         Object key = iterator.next();
                         Long timeToExpire = objectTimeStamps.get(key) + timeToLive;
                         if (timeToExpire < expireRunStamp)
                         {
                             objects.remove(key);
                             objectTimeStamps.remove(key);
                         }
                     }
                     findNextExpireTime();
                 }
                 catch (Exception e)
                 {
                     e.printStackTrace();
                 }
             }
         }
         LOCK_STATE = LOCK_STATE_UNLOCKED;
     }
 
     /**
      * Clears both HashMaps, but not re-initialize their sizes.
      */
     public void clear()
     {
         synchronized (objects)
         {
             objectTimeStamps.clear();
             objects.clear();
             nextTimeSomeExpired = NO_OBJECTS;
         }
     }
 
     public boolean containsKey(Object key)
     {
         clearExpired();
         synchronized (objects)
         {
             return objects.containsKey(key);
         }
     }
 
     public boolean containsValue(Object value)
     {
         clearExpired();
         synchronized (objects)
         {
             return objects.containsValue(value);
         }
     }
 
     public Object get(Object key)
     {
         clearExpired();
         synchronized (objects)
         {
             return objects.get(key);
         }
     }
 
     public boolean isEmpty()
     {
         clearExpired();
         synchronized (objects)
         {
             return objects.isEmpty();
         }
     }
 
     /**
      * When we remove any object, we must found again nextTimeSomeExpired.
      *
      * @param key - key to remove
      * @return - object, if any
      */
     public Object remove(Object key)
     {
         synchronized (objects)
         {
             Object remove = objects.remove(key);
             objectTimeStamps.remove(key);
             if (remove != null)
             {
                 findNextExpireTime();
             }
             return remove;
         }
     }
 
     public int size()
     {
         clearExpired();
         synchronized (objects)
         {
             return objects.size();
         }
     }
 
     /**
      * Internal-use method, finds out when next object will expire and store this as nextTimeSomeExpired.
      * If there's no items in cache - let's store NO_OBJECTS
      */
     private void findNextExpireTime()
     {
         if (objects.size() == 0)
         {
             nextTimeSomeExpired = NO_OBJECTS;
         }
         else
         {
             nextTimeSomeExpired = NO_OBJECTS;
             Collection<Long> longs = null;
             synchronized (objects)
             {
                 longs = new ArrayList(objectTimeStamps.values());
             }
             for (Iterator<Long> iterator = longs.iterator(); iterator.hasNext(); )
             {
                 Long next = iterator.next() + timeToLive;
                 if (nextTimeSomeExpired == NO_OBJECTS || next < nextTimeSomeExpired)
                 {
                     nextTimeSomeExpired = next;
                 }
             }
         }
     }
 }
