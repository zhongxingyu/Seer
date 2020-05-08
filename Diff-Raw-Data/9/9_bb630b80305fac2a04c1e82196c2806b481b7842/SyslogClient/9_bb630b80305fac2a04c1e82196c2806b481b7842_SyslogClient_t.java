 package org.openstack.burrow.example.syslog;
 
 import org.openstack.burrow.backend.*;
 import org.openstack.burrow.client.Message;
 import org.openstack.burrow.client.Queue;
 import org.openstack.burrow.client.methods.DeleteMessages;
 
 import java.util.List;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 public class SyslogClient implements Runnable {
     public static final int DEF_WAITTIME = 10;
 
     private Queue syslog;
     private Backend back;
     private ConcurrentLinkedQueue<LogEntry> channel;
     private Logger log;
     private int consecutiveErrors;
     private int waitTime;
     private boolean running;
     private boolean keepGoing;
 
    public SyslogClient(Queue syslog, Backend back, ConcurrentLinkedQueue<LogEntry> channel, int waitTime) {
         this.syslog = syslog;
         this.back = back;
         this.channel = channel;
         //this.log = log
         this.waitTime = waitTime;
         this.keepGoing = true;
         this.running = false;
     }
 
     public void run() {
         running = true;
 
         while (keepGoing) {
             List<Message> grabbed = null;
             try {
                 grabbed = back.execute(new DeleteMessages(syslog).withWait(waitTime).withDetail("all"));
             } catch (ProtocolException pe) {
                 consecutiveErrors++;
                 //log.log(Level.WARNING, "Attempt to retrieve log entries failed", pe);
                 continue;
 
             } catch (ConnectionException ce) {
                 consecutiveErrors++;
                 //log.log(Level.WARNING, "Connection attempt failed", ce);
                 continue;
 
             } catch (QueueNotFoundException qe) {
                 //To be expected, will happen any time we drain the queue
                 //And nothing new arrives before our wait expires.
                 continue;
 
             } catch (CommandException ce) {
                 consecutiveErrors++;
                 //log.log(Level.WARNING, "Connection attempt failed", ce);
                 continue;
 
             } catch (Exception e) {
                 //Not reachable with the current BurrowException hierarchy,
                 //barring something going seriously wrong.
                 //log.log(Level.SEVERE, "An unexpected error has occurred", e);
                 throw new Error(e); //Per Bart: Crash early, crash often.
             }
 
             consecutiveErrors = 0;
 
             for (Message message : grabbed) {
                 try {
                     channel.add(LogEntry.fromRawEntry(message.getBody()));
                 } catch (MalformedEntryException mfe) {
                     //log.log(Level.WARNING, "Malformed Entry: " + message.getBody());
                 }
             }
 
             if (!grabbed.isEmpty()) channel.notifyAll();
         }
         running = false;
         //cleanup?
     }
 
     public void halt() {
         if (running == false) throw new IllegalStateException("Attempt to halt an non-running thread");
 
         keepGoing = false;
     }
 
     public boolean isRunning() {
         return running;
     }
 
     public int getConsecutiveErrors() {
         return consecutiveErrors;
     }
 }
