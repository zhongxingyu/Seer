 package org.phicluster.core;
 
 import org.apache.zookeeper.WatchedEvent;
 import org.apache.zookeeper.Watcher;
 import org.phicluster.core.task.PhiTask;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 

 public abstract class DistTaskExecutor implements Runnable, Watcher {
     protected static final Logger logger = LoggerFactory.getLogger(DistTaskExecutor.class);
 
     protected final DistTaskPool distTaskPool;
 
     private boolean executeTasks;
     private volatile boolean suspended;
     
     public enum State { INSTANTIATED, RUNNING, SUSPENDED, STOPPED };
    protected State state;
     
     protected DistTaskExecutor(DistTaskPool distTaskPool) {
         this.distTaskPool = distTaskPool;
         
         this.executeTasks = true;
         this.suspended = false;
         
         this.state = State.INSTANTIATED; 
     }
     
     @Override
     public void run() {
         while (executeTasks) {
             state = State.RUNNING;
             try {
                 if (suspended) {
                     synchronized (this) {
                         while (suspended) {
                             state = State.SUSPENDED;
                             this.wait();
                         }
                     }
                     continue;
                 }
                 
                 PhiTask phiTask = distTaskPool.take(this);
                 if (phiTask == null) {
                     synchronized (this) {
                         this.wait(1000); // time out in 1 second in case watch event is missed
                         continue;
                     }
                 }
                 executeTask(phiTask);
             } catch (Exception e) {
                 logger.error("exception while executing task: {}", e);
                 e.printStackTrace();
             }
         }
         
         state = State.STOPPED;
     }
     
     public State state() {
         return state;
     }
     
     public void stopExecutor() {
         executeTasks = false;
     }
     
     public synchronized void suspend() {
         suspended = true;
     }
     
     public synchronized void resume() {
         suspended = false;
         notifyAll();
     }
 
     
     protected abstract void executeTask(PhiTask task);
     
     @Override
     public void process(WatchedEvent event) {
         // notify itself
         synchronized (this) {
             notifyAll();
         }        
     }
 
 
 }
