 package jobs;
 
 
 import util.Util;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.List;
 
 public class WorkerThread {
 
     private Thread thread;
     private JobQueue jobQueue;
 
     private boolean stopWork;
     private boolean suspend;
 
     private static Long NO_JOB_SLEEP_INTERVAL = 2000L;
     private static Long FINISH_SLEEP_INTERVAL = 1000L;
 
     public WorkerThread() {
         jobQueue = new JobQueue();
         stopWork = false;
         suspend = false;
     }
 
    public void start() {
         thread = new Thread(new Runnable() {
             @Override
             public void run() {
                 while(!stopWork) {
                     if(jobQueue.isEmpty()) {
                         Util.sleep(NO_JOB_SLEEP_INTERVAL);
                         continue;
                     }
 
                     if(!suspend) {
                         Job job = jobQueue.pop();
                         executeJob(job);
                     }
                     Util.sleep(FINISH_SLEEP_INTERVAL);
                 }
             }
         });
        thread.start();
     }
 
     public void stop() {
         stopWork = true;
     }
 
     /**
      * This method will suspend the worker thread. However, it wont' stop running
      * the current job.
      */
     public void suspend() {
         synchronized (this) {
             suspend = true;
         }
     }
 
     public void resume() {
         synchronized (this) {
             suspend = false;
         }
     }
 
     public void executeJob(Job job) {
         //TODO: fake code
         String result = execute(job.getExecuteName());
         if(result != null) {
             job.setJobResult(result);
         } else {
             System.out.println("There is no result for job");
         }
         Util.sleep(2000);
     }
 
     private static String execute(String command) {
         try {
             List<String> cmds = new LinkedList<String>();
 //            cmds.add("sh");
 //            cmds.add("-c");
             cmds.add("./" + command);
 
             System.out.println(cmds.toString());
 
             ProcessBuilder pb = new ProcessBuilder(cmds);
             Process p = pb.start();
             BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(p.getInputStream())));
 
             String lineStr;
             while((lineStr = br.readLine()) != null) {
                 System.out.println(lineStr);
             }
             if(p.waitFor() != 0) {
                 if(p.exitValue() == 1) {
                     System.err.println("Job Execute error");
                 }
             }
 
             return lineStr;
 
         } catch (IOException ex) {
             System.err.println(ex);
         } catch (InterruptedException ex) {
             System.err.println(ex);
         }
         return null;
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
 
     public static void main(String[] args) {
         Job job = new Job("test");
         job.loadJobFromFile();
         job.saveJobToFile();
 
 
         execute(job.getExecuteName());
     }
 }
