 package jobs;
 
 
 import loadbalance.Adaptor;
 import org.apache.log4j.Logger;
 import util.Util;
 
 import java.util.Date;
 import java.util.concurrent.atomic.AtomicBoolean;
 
 public class WorkerThread {
 
     private Thread mainThread;
     private Thread monitorThread;
     private JobQueue jobQueue;
     private Adaptor adaptor;
 
     private boolean stopWork;
     private AtomicBoolean isSuspended;
 
     private static Long NO_JOB_SLEEP_INTERVAL = 2000L;
     private static Long FINISH_SLEEP_INTERVAL = 1000L;
     private static Long RUNNING_TIME = 700L;
     private static Long SLEEPING_TIME = 1000L - RUNNING_TIME;
 
     private static Logger logger = Logger.getLogger(WorkerThread.class);
 
     private Job curRunJob;
 
     public WorkerThread(Adaptor adaptor) {
         jobQueue = new JobQueue();
         stopWork = false;
         isSuspended = new AtomicBoolean(false);
         curRunJob = null;
         this.adaptor = adaptor;
     }
 
     public void start() {
         mainThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 while(!stopWork) {
                     if(jobQueue.isEmpty() && getCurRunJob() == null) {
                         Util.sleep(NO_JOB_SLEEP_INTERVAL);
                         continue;
                     }
 
                     if(!isSuspended.get()) {
                         if(getCurRunJob() == null)
                             setCurRubJob(jobQueue.pop());
                         if(!getCurRunJob().isFinished())
                             getCurRunJob().run();
 
                         if(getCurRunJob().isFinished()) {
                             System.out.println(getCurRunJob().getResult());
                             if(adaptor != null)
                                 adaptor.jobFinished(getCurRunJob());
                             setCurRubJob(null);
                         }
                     }
                 }
             }
         });
         mainThread.start();
 
         monitorThread = new Thread(new Runnable() {
             @Override
             public void run() {
                 while(!stopWork || getCurRunJob()!= null) {
                     if(getCurRunJob() == null) continue;
                     try {
                         getCurRunJob().resume();
                         Util.sleep(RUNNING_TIME);
                         getCurRunJob().stop();
                         Util.sleep(SLEEPING_TIME);
                     } catch (NullPointerException ex) {
                         //do nothing, it's okay when there is a NPE, since the job can be finished during waiting
                     }
                 }
             }
         });
         monitorThread.start();
     }
 
     public void stop() {
         stopWork = true;
     }
 
     /**
      * This method will suspend the worker mainThread. However, it wont' stop running
      * the current job.
      */
     public void suspend() {
         isSuspended.set(true);
     }
 
     public void resume() {
         isSuspended.set(false);
     }
 
     public void addJob(Job job) {
         synchronized (this) {
             jobQueue.append(job);
         }
     }
 
     public boolean isEmpty() {
         synchronized (this) {
             return jobQueue.isEmpty();
         }
     }
 
     public JobQueue getJobQueue() {
         return jobQueue;
     }
 
     public Integer getJobQueueSize() {
         return jobQueue.size();
     }
 
     public boolean setThrottling(Integer percentage) {
         if(percentage <=0 || percentage >= 100) {
             logger.error("Wrong Throttling Parameter");
             return false;
         }
 
         RUNNING_TIME = (long) percentage * 10;
         SLEEPING_TIME = 1000L - RUNNING_TIME;
         return true;
     }
 
     private void setCurRubJob(Job job) {
         synchronized (this) {
             curRunJob = job;
         }
     }
 
     private Job getCurRunJob() {
         synchronized (this) {
             return curRunJob;
         }
     }
 
 
 }
