 package de.zib.gndms.stuff.threading;
 
import com.sun.istack.internal.NotNull;
 import de.zib.gndms.stuff.threading.impl.DefaultDV;
 
 import java.util.concurrent.*;
 
 /**
  * Forketeer
  *
  * @author try ste fan pla nti kow zib
  *         <p/>
  *         User stepn Date: 01.04.11 TIME: 16:39
  */
 public class Forketeer {
     private long delay;
 
     private final Thread thread;
     private final BlockingQueue<ForkRequest> q = new LinkedBlockingQueue<ForkRequest>();
 
 
     public Forketeer(long delay) {
         this.delay = delay;
 
         thread = new Thread(new Runnable() {
             public void run() {
                 while(true) {
                     try {
                         final ForkRequest request = q.take();
                         final Thread forkOff = new Thread(request);
                     } catch (InterruptedException e) {
                         Thread.interrupted();
                     }
                     waitUntil(System.currentTimeMillis() + getDelay());
                 }
             }
         });
 
         thread.start();
     }
 
     public void enqueue(final @NotNull ForkRequest req) throws InterruptedException {
         q.put(req);
     }
 
     private void waitUntil(long t) {
         for(long gap = t-System.currentTimeMillis(); gap > 0L; gap = t-System.currentTimeMillis())
             try {
                 Thread.sleep(gap);
             }
             catch (InterruptedException ie) {
                 Thread.interrupted();
             }
     }
 
     public synchronized long getDelay() {
         return delay;
     }
 
     public synchronized void setDelay(long delay) {
         this.delay = delay;
     }
 
     public class ForkRequest<T> implements Runnable {
         private final Callable<T> callable;
         private final DV<T, Exception> result = DefaultDV.Factory.getInstance().newEmpty();
 
         public ForkRequest(Callable<T> callable) {
             this.callable = callable;
         }
 
         public void run() {
             try {
                 result.setValue(callable.call());
             } catch (Exception e) {
                 result.setCause(e);
             }
         }
 
         public DV<T, Exception> getResult() {
             return result;
         }
     }
 }
