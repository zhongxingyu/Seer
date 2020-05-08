 package org.freecode.irc.votebot;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.Map;
 import java.util.concurrent.*;
 
 /**
  * Created with IntelliJ IDEA.
  * User: shivam
  * Date: 8/29/13
  * Time: 7:10 PM
  * To change this template use File | Settings | File Templates.
  */
 public class ExpiryQueue<T> implements Runnable {
     private final long expiry;
     private final ConcurrentHashMap<T, Long> entryTimes;
     private final LinkedList<T> queue;
     private Future<?> future;
     private ScheduledExecutorService service;
 
     public ExpiryQueue(final long defaultExpiry) {
         this.expiry = defaultExpiry;
         entryTimes = new ConcurrentHashMap<>();
         queue = new LinkedList<>();
         service = Executors.newSingleThreadScheduledExecutor();
        future = service.scheduleAtFixedRate(this, expiry / 2, expiry / 2, TimeUnit.MILLISECONDS);
     }
 
     public boolean insert(T t) {
         if(queue.contains(t)) {
             throw new IllegalArgumentException("Queue already contains this element");
         }
         if (entryTimes.containsKey(t)) {
             entryTimes.replace(t, System.currentTimeMillis());
         } else
             entryTimes.put(t, System.currentTimeMillis());
         return queue.add(t);
     }
 
     public void run() {
         for (Map.Entry<T, Long> entry : entryTimes.entrySet()) {
             long start = entry.getValue();
             if (System.currentTimeMillis() - start >= expiry) {
                 entryTimes.remove(entry.getKey(), entry.getValue());
                queue.remove(entry.getValue());
             }
         }
     }
 
     public boolean remove(T t) {
         if(entryTimes.containsKey(t)) {
             entryTimes.remove(t);
         }
         return queue.remove(t);
     }
 
     public boolean contains(T t) {
         return queue.contains(t);
     }
 
 
 }
