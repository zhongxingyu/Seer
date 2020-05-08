 package nl.astraeus.http;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 /**
  * User: rnentjes
  * Date: 5/7/12
  * Time: 10:16 PM
  */
 public class ConnectionHandlerThread extends Thread {
 
     private BlockingQueue<ConnectionHandler> jobs;
     private volatile boolean running = false;
     private volatile boolean handling = false;
     private SimpleWebServer server;
 
     public ConnectionHandlerThread(SimpleWebServer server, String name, BlockingQueue<ConnectionHandler> jobs) {
         super(name);
 
         this.server = server;
         this.jobs = jobs;
     }
 
     public boolean isRunning() {
         return running;
     }
 
     public void setRunning(boolean running) {
         this.running = running;
     }
 
     @Override
     public void run() {
         running = true;
 
         try {
             while (running) {
                 try {
                     ConnectionHandler currentJob = jobs.poll(1000, TimeUnit.MILLISECONDS);
 
                     if (currentJob != null) {
                         try {
                             handling = true;
 
                            System.out.println("Handling: ("+Thread.currentThread().getId()+")"+currentJob);

                             currentJob.handle();
                         } finally {
                             handling = false;
                         }
                     }
                 } catch (InterruptedException e) {
                     // Expected
                 }
             }
 
             server.removeThread(this);
         } catch (Throwable e) {
             e.printStackTrace();
             server.removeThread(this, e);
         } finally {
             running = false;
         }
     }
 }
