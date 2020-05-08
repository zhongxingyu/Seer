package org.fabric3.tests.eventing.performance;
 
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.oasisopen.sca.annotation.Scope;
 
 import org.fabric3.api.annotation.Consumer;
 
 /**
  * @version $Rev$ $Date$
  */
 @Scope("COMPOSITE")
public class TestConsumer implements Statistics{
     private AtomicInteger received = new AtomicInteger();
 
     public int getReceived() {
         return received.get();
     }
 
     public TestConsumer() {
         new Thread(new Gatherer()).start();
     }
 
     @Consumer
     public void onReceive(String msg) {
         received.incrementAndGet();
     }
 
 
     private class Gatherer implements Runnable {
 
         public void run() {
             long started = System.currentTimeMillis();
             while (true) {
                 long elapsed = System.currentTimeMillis() - started;
                 System.out.println(elapsed + " : " + received);
                 try {
                     Thread.sleep(5000);
                 } catch (InterruptedException e) {
                     Thread.currentThread().interrupt();
                 }
             }
         }
     }
 
 
 }
