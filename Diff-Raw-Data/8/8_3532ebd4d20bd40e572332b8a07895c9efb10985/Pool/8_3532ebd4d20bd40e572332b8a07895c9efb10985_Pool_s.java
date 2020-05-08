 package com.arc90.xmlsanity.util;
 
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 /***
  * Based on http://sourcemaking.com/design_patterns/object_pool/java
  ***/
 public abstract class Pool<T>
 {
     public final static int    DEFAULT_EXPIRATION_SECONDS = 300;
     public final static int    DEFAULT_MAX_TO_RETAIN      = 255;
 
     protected final int        maxToRetain;
     private final long         expirationMs;
 
     private final Map<T, Long> in                         = new ConcurrentHashMap<T, Long>();
     private final Map<T, Long> out                        = new ConcurrentHashMap<T, Long>();
 
     /**
      * Constructor which uses the default expiration time of 5 minutes.
      */
     public Pool()
     {
         this(DEFAULT_EXPIRATION_SECONDS, DEFAULT_MAX_TO_RETAIN);
     }
 
     public Pool(int expirationSeconds)
     {
         this(expirationSeconds, DEFAULT_MAX_TO_RETAIN);
     }
 
     public Pool(int expirationSeconds, int maxToRetain)
     {
         this.expirationMs = expirationSeconds * 1000;
         this.maxToRetain = maxToRetain;
     }
 
     public synchronized void checkIn(T t)
     {
         out.remove(t);
 
         if (in.size() < maxToRetain)
         {
             in.put(t, System.currentTimeMillis());
         }
     }
 
     public synchronized T checkOut() throws PoolException
     {
         long now = System.currentTimeMillis();
 
         T t;
 
         if (in.size() > 0)
         {
             for (T tt : in.keySet())
             {
                 t = tt;
 
                 if ((now - in.get(t)) > expirationMs)
                 {
                     // object has expired
                     in.remove(t);
                     destroy(t);
                     t = null;
                 }
                 else
                 {
                     if (validate(t))
                     {
                         in.remove(t);
                         out.put(t, now);
                         return (t);
                     }
                     else
                     {
                         // object failed validation
                         in.remove(t);
                         destroy(t);
                         t = null;
                     }
                 }
             }
         }
 
         // no objects available, create a new one
         t = create();
         out.put(t, now);
         return (t);
     }
 
     /**
      * Default implementation does nothing.
      * 
      * @param o
      */
     public void destroy(T o)
     {
         // default does nothing
     }
 
     /**
      * Default implementation always returns true.
      * 
      * @param o
      * @return
      */
     public boolean validate(T o)
     {
         return true;
     }
 
     protected abstract T create() throws PoolException;
 
     /**
      * 
      * @param o an object which was checked out from this pool
      * @return the time this object was created, number of milliseconds between the time this object was created and midnight, January 1, 1970 UTC. If the object was not checked out from this pool, or was checked in before this method was called, will return null.
      */
     public long getTimeCreated(T o)
     {
        return out.get(o);
     }
 }
