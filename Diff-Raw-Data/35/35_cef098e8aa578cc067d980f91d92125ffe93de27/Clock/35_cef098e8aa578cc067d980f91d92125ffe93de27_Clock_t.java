 package edu.uwm.cs552.util;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 public class Clock {
   private Clock() {
     new Thread(new Runnable() {
       public void run() {
         doCycles();
       }
     }).start();
   }
 
   public static Clock getInstance() {
     return clock;
   }
 
   public void addListener(Runnable r, long millis) {
     synchronized (this) {
       allListeners.add(r);
       lastRun.put(r, System.currentTimeMillis());
       request.put(r, millis);
 
       if (millis < waitTime)
         waitTime = millis;
     }

   }
 
   public void doCycles() {
     while (true) {
       try {
         Thread.sleep(waitTime);
       } catch (InterruptedException e) {
       }
 
       long currentTime = System.currentTimeMillis();
 
       synchronized (this) {
         for (Runnable r : allListeners) {
          if (lastRun.get(r) + request.get(r) <= currentTime) {
             new Thread(r).start();
            lastRun.put(r, lastRun.get(r) + request.get(r));
          }
         }
       }
     }
   }
 
  private long waitTime = 1000; // maximum wait
   private Set<Runnable> allListeners = new HashSet<Runnable>();
   private final Map<Runnable, Long> lastRun = new HashMap<Runnable, Long>();
   private final Map<Runnable, Long> request = new HashMap<Runnable, Long>();
   private static volatile Clock clock = new Clock();
 }
