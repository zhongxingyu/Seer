 package org.apache.cassandra.hadoop.trackers;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.cassandra.utils.FBUtilities;
 import org.apache.hadoop.mapred.JobTracker;
 import org.apache.hadoop.mapred.TaskTracker;
 import org.apache.hadoop.metrics2.util.MBeans;
 import org.apache.log4j.Logger;
 
 //Will start job and or task trackers
 //depending on the ring
 public class TrackerInitializer
 {
     private static Logger logger = Logger.getLogger(TrackerInitializer.class);
     private static final CountDownLatch jobTrackerStarted = new CountDownLatch(1);
     public static final String  trackersProperty = "hadoop-trackers";
     public static final boolean isTrackerNode = System.getProperty(trackersProperty, "false").equalsIgnoreCase("true");
     
     public static void init() 
     {
              
         //Wait for gossip                
         try
         {                    
             logger.info("Waiting for gossip to start");
            Thread.sleep(5000);
         }
         catch (InterruptedException e)
         {
            throw new RuntimeException(e);
         }
         
         //Are we a JobTracker?
         InetAddress jobTrackerAddr = CassandraJobConf.getJobTrackerNode();
         if(jobTrackerAddr.equals(FBUtilities.getLocalAddress()))
         {
             Thread jobTrackerThread = getJobTrackerThread();
             jobTrackerThread.start();
             
             try
             {
                 jobTrackerStarted.await(1, TimeUnit.MINUTES);
             }
             catch (InterruptedException e)
             {
                 throw new RuntimeException("JobTracker not started",e);
             }            
         }
         else
         {
             logger.info("We are not the job tracker: "+jobTrackerAddr+" vs "+FBUtilities.getLocalAddress());
         }
               
         
         getTaskTrackerThread().start();
     }
     
     
     private static Thread getJobTrackerThread()
     {
        Thread jobTrackerThread = new Thread(new Runnable() {
             
             public void run()
             {
                 JobTracker jobTracker = null; 
                                               
                 while(true)
                 {
                     try
                     {
                         jobTracker = JobTracker.startTracker(new CassandraJobConf());     
                         logger.info("Hadoop Job Tracker Started...");
                         jobTrackerStarted.countDown();
                         jobTracker.offerService();
                        
                     }
                     catch(Throwable t)
                     {
                         //on OOM shut down the tracker
                         if(t instanceof OutOfMemoryError || t.getCause() instanceof OutOfMemoryError)
                         {
                             try
                             {
                                 jobTracker.stopTracker();
                             }
                             catch (IOException e)
                             {
                                
                             }
                             break;
                         }
                         logger.warn("Error starting job tracker", t);
                         break;
                     }
                 }
             }
         }, "JOB-TRACKER-INIT");  
        
        return jobTrackerThread;
     }
     
     
     private static Thread getTaskTrackerThread()
     {
         Thread taskTrackerThread = new Thread(new Runnable() {
             
             public void run()
             {
                 TaskTracker taskTracker = null; 
                                
                 
                 while(true)
                 {
                     try
                     {                        
                         taskTracker = new TaskTracker(new CassandraJobConf());
                         MBeans.register("TaskTracker", "TaskTrackerInfo", taskTracker);
                         logger.info("Hadoop Task Tracker Started... ");
                         taskTracker.run();
                     }
                     catch(Throwable t)
                     {
                         //on OOM shut down the tracker
                         if(t instanceof OutOfMemoryError || t.getCause() instanceof OutOfMemoryError)
                         {                         
                             break;
                         }
                     }
                 }
             }
         }, "TASK-TRACKER-INIT");  
        
        return taskTrackerThread;
     }
     
 }
