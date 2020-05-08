 package com.sfdc.http.queue;
 
 import com.sfdc.http.loadgen.RequestGenerator;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import poc.SessionIdReader;
 
 import java.io.IOException;
 import java.util.concurrent.BlockingQueue;
 
 /**
  * @author psrinivasan
  *         Date: 9/4/12
  *         Time: 11:01 AM
  *         Pushes requests into the work queue for consumption by
  *         consumers.
  */
 public class Producer implements Runnable {
 
     private final BlockingQueue<WorkItem> queue;
     private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
     private int numHandshakes;
     private SessionIdReader sessionIdReader;
     // TODO:  temporary declaration - it's not clear that we want request generator to be a instance var.
     private RequestGenerator requestGenerator;
    private String instance;
 
 
     public Producer(BlockingQueue<WorkItem> queue, int numHandshakes, SessionIdReader sessionIdReader, String instance) throws Exception {
         this.queue = queue;
         requestGenerator = new RequestGenerator();
         this.numHandshakes = numHandshakes;
         this.sessionIdReader = sessionIdReader;
        this.instance = instance;
     }
 
     /**
      * When an object implementing interface <code>Runnable</code> is used
      * to create a thread, starting the thread causes the object's
      * <code>run</code> method to be called in that separately executing
      * thread.
      * <p/>
      * The general contract of the method <code>run</code> is that it may
      * take any action whatsoever.
      *
      * @see Thread#run()
      */
     @Override
     public void run() {
         //publish(numHandshakes);
         try {
             publishFromSessionIdFile();
         } catch (IOException e) {
             e.printStackTrace();
         }
         LOGGER.info("Producer is done.  exiting");
     }
 
     public void publish(int num_requests) {
         for (int i = 0; i < num_requests; i++) {
             boolean result = queue.add(requestGenerator.generateHandshakeWorkItem());
             if (!result) {
                 LOGGER.warn("Failed to publish request to queue");
             }
         }
     }
 
     public void publishFromSessionIdFile() throws IOException {
         String sessionId;
         while ((sessionId = sessionIdReader.getOneSessionId()) != null) {
            boolean result = queue.add(requestGenerator.generateHandshakeWorkItem(sessionId, instance));
             if (!result) {
                 LOGGER.warn("Failed to publish request to queue");
             }
         }
     }
 }
