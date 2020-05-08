 package com.dianping.wizard.widget.concurrent;
 
 import com.dianping.wizard.config.Configuration;
 import org.apache.log4j.Logger;
 
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * @author ltebean
  */
 public class Executor {
 
     private ThreadPoolExecutor executorService;
 
     private Status status;
 
     private Logger logger = Logger.getLogger(Executor.class);
 
     enum Status {
         ALIVE, SHUTDOWN
     };
 
     private Executor() {
         init();
     }
 
     private static final Executor instance= new Executor();
 
     public static ExecutorService getInstance(){
         return instance.executorService;
     }
 
     public void init() {
         int corePoolSize= Configuration.get("concurrent.threadPool.corePoolSize",50,Integer.class);
         int maximumPoolSize= Configuration.get("concurrent.threadPool.maximumPoolSize",50,Integer.class);
        int keepAliveTime= Configuration.get("concurrent.threadPool.keepAliveTime",00,Integer.class);
        int blockingQueueCapacity= Configuration.get("concurrent.threadPool.blockingQueueCapacity",500,Integer.class);
 
         if (executorService == null) {
             executorService = new ThreadPoolExecutor(corePoolSize,
                     maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
                     new LinkedBlockingQueue<Runnable>(blockingQueueCapacity),
                     new ThreadPoolExecutor.CallerRunsPolicy());
 
             logger.info("executorService has been initialized");
 
             // adds a shutdown hook to shut down the executorService
             Thread shutdownHook = new Thread() {
                 @Override
                 public void run() {
                     synchronized (this) {
                         if (status == Status.ALIVE) {
                             executorService.shutdown();
                             status = Status.SHUTDOWN;
                             logger.info("excecutorService has been shut down");
                         }
                     }
                 }
             };
             Runtime.getRuntime().addShutdownHook(shutdownHook);
             logger.info("successfully add shutdown hook");
         }
     }
 
 }
