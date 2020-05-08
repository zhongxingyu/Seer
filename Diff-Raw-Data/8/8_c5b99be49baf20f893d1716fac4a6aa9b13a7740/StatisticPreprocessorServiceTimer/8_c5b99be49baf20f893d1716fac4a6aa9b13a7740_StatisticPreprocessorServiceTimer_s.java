 package de.escidoc.core.sm.mbean;
 
 
 /**
  * @author mih
  * 
  *         Singleton for timing Scheduled Job.
  *         We need this because Spring Scheduler has Bug and 
  *         always executes scheduled job twice.
  * 
  */
 public final class StatisticPreprocessorServiceTimer {
 
    private static StatisticPreprocessorServiceTimer instance = null;
     
     private long lastExecutionTime = 0;
 
     /**
      * private Constructor for Singleton.
      * 
      */
     private StatisticPreprocessorServiceTimer() {
     }
 
     /**
      * Only initialize Object once. Check for old objects in cache.
      * 
      * @return StatisticPreprocessorServiceTimer StatisticPreprocessorServiceTimer
      * 
      */
    public static synchronized StatisticPreprocessorServiceTimer getInstance() {
        if (instance == null) {
            instance = new StatisticPreprocessorServiceTimer();
        }
         return instance;
     }
     
     /**
      * Get lastExecutionTime.
      * 
      */
     public synchronized long getLastExecutionTime() {
         long savedLastExecutionTime = lastExecutionTime;
         lastExecutionTime = System.currentTimeMillis();
         return savedLastExecutionTime;
     }
 
 }
 
