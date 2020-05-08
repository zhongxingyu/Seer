 package gx.browserchannel;
 
 import gx.browserchannel.message.SaveMessage;
 import gx.realtime.custom.SaveRevisionResponse;
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import java.util.concurrent.LinkedBlockingQueue;
 
 /**
  * Thread that handles the process of sending messages through the forward channel
  */
 public class ForwardChannelThread extends Thread
 {
     private static Logger logger = LogManager.getLogger(ForwardChannelThread.class);
     private final LinkedBlockingQueue<SaveMessage> messageQueue;
     private final BrowserChannel parent;
 
     public ForwardChannelThread(BrowserChannel parent, LinkedBlockingQueue<SaveMessage> messageQueue)
     {
         this.parent = parent;
         this.messageQueue = messageQueue;
     }
 
     public void run()
     {
         try {
             while (!isInterrupted()) {
                 consume(messageQueue.take());
             }
         } catch (InterruptedException e) {
             logger.info("ForwardChannelThread interrupted");
         }
     }
 
    /**
     * Consumes the given message and hands the response off to the parent.
     * @param msg
     */
     private void consume(SaveMessage msg)
     {
         SaveRevisionResponse response = parent.send(msg);
         parent.processResponse(response);
     }
 }
