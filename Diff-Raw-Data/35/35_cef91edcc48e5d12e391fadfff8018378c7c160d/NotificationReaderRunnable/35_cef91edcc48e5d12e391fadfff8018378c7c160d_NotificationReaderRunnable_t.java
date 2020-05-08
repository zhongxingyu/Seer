 /*
  * Created On:  4-Jun-07 11:38:49 AM
  */
 package com.thinkparity.ophelia.model.queue.notification;
 
 import java.io.IOException;
 import java.util.Observable;
 
import com.thinkparity.codebase.log4j.Log4JWrapper;

 import com.thinkparity.codebase.model.queue.notification.NotificationReader;
 import com.thinkparity.codebase.model.queue.notification.NotificationSession;
 
 /**
  * <b>Title:</b><br>
  * <b>Description:</b><br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public final class NotificationReaderRunnable extends Observable implements
         Runnable {
 
     /** A notification reader. */
     private final NotificationReader reader;
 
    /** A log4j wrapper. */
    private final Log4JWrapper logger;

     /**
      * Create NotificationReaderRunnable.
      * 
      * @param session
      *            A <code>NotificationSession</code>.
      * @throws IOException
      */
     public NotificationReaderRunnable(final NotificationSession session)
             throws IOException {
         super();
        this.logger = new Log4JWrapper(getClass());
         this.reader = new NotificationReader(session);
         this.reader.open();
     }
 
     /**
      * @see java.lang.Runnable#run()
      *
      */
     public void run() {
         while (true) {
             reader.waitForNotification();
             if (reader.didNotify()) {
                 setChanged();
                try {
                    notifyObservers(Boolean.TRUE);
                } catch (final Throwable t) {
                    logger.logError(t, "An error occured firing notification.");
                }
             }
         }
     }
 
     /**
      * Close the reader.
      * 
      * @throws IOException
      */
     public void closeReader() throws IOException {
         reader.close();
     }
 }
