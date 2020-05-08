package org.fabric3.tests.eventing.performance;
 
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.atomic.AtomicBoolean;
 
import org.oasisopen.sca.annotation.Reference;
 import org.oasisopen.sca.annotation.Scope;
 
 import org.fabric3.api.annotation.Producer;
 import org.fabric3.api.annotation.management.Management;
 import org.fabric3.api.annotation.management.ManagementOperation;
 
 /**
  *
  * To start: http://localhost:8181/management/TestProducer/start
  * @version $Rev$ $Date$
  */
 @Management
 @Scope("COMPOSITE")
 public class TestProducerImpl implements TestProducer {
 
     @Producer
     protected TestChannel channel;
 
     private Runner runner = new Runner();
     private AtomicBoolean stop = new AtomicBoolean(true);
 
     @ManagementOperation
     public void start() {
         if (!stop.getAndSet(false)){
            return;
         }
         new Thread(runner).start();
     }
 
     @ManagementOperation
     public void stop() {
         stop.set(true);
     }
 
     private class Runner implements Runnable {
 
         public void run() {
             while (!stop.get()) {
                 try {
                     channel.send("test");
                 } catch (RejectedExecutionException e) {
                     e.printStackTrace();
                 }
             }
         }
     }
 
 }
