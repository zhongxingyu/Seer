 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.eagerlogic.cubee.client;
 
 import com.google.gwt.user.client.Timer;
 import java.util.LinkedList;
 
 /**
  *
  * @author dipacs
  */
 public final class EventQueue {
 
     private static EventQueue instance;
 
     public static EventQueue getInstance() {
        if (instance == null) {
             instance = new EventQueue();
         }
 
         return instance;
     }
    
     private final LinkedList<Runnable> queue = new LinkedList<Runnable>();
     private final Timer timer;
 
     private EventQueue() {
         timer = new Timer() {
             @Override
             public void run() {
                 int size = queue.size();
                 for (int i = 0; i < size; i++) {
                     Runnable task = queue.pollFirst();
                     if (task != null) {
                         try {
                             task.run();
                         } catch (Throwable t) {
                             t.printStackTrace();
                         }
                     }
                 }
                timer.schedule(1);
             }
         };
         timer.schedule(10);
     }
 
     public void invokeLater(Runnable task) {
         if (task == null) {
             throw new NullPointerException("The task can not be null.");
         }
         queue.add(task);
     }
 
     public void invokePrior(Runnable task) {
         if (task == null) {
             throw new NullPointerException("The task can not be null.");
         }
         queue.addFirst(task);
     }
 }
