 package org.muralx.concurrent.executor;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.AbstractExecutorService;
 import java.util.concurrent.Executor;
 import java.util.concurrent.Executors;
 import java.util.concurrent.RejectedExecutionException;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 import java.util.concurrent.locks.ReentrantLock;
 
 /**
  * A {@link SerialExecutor} implementation which executes one task at a time
  * using an underlying executor as the actual processor of the task.
  * <p>
  * 
  * This implementation differs from an {@link Executor} created by
  * {@link Executors#newSingleThreadExecutor()} in that there is not actual
  * thread handling done by {@link SerialExecutor} but instead the processing of
  * the tasks is delegated to an underlying executor that can be shared among
  * many {@link Executor}. <p>
  * 
  * Tasks submitted on a {@link SerialExecutor} will be processed sequentially
  * and in the exact same order in which they were submitted, regardless of the
  * number of threads available in the underlying executor.
  * 
  * 
  * @author muralx (Diego Belfer)
  * 
  */
 public class SerialExecutor extends AbstractExecutorService {
     private static final int RUNNING = 0;
     private static final int SHUTDOWN = 1;
     private static final int TERMINATED = 2;
 
     final Lock lock = new ReentrantLock();
     final Condition termination = lock.newCondition();
 
     final Executor underlyingExecutor;
     final int maxQueueSize;
     final ArrayDeque<Runnable> commands;
 
     volatile int state = RUNNING;
     Runnable currentCommand;
 
     /*
      * The runnable we submit into the underlyingExecutor, we avoid creating
      * unnecessary runnables since only one will be submitted at a time
      */
     private final Runnable innerRunnable = new Runnable() {
 
         public void run() {
             /*
              * If state is TERMINATED, skip execution
              */
             if (state == TERMINATED) {
                 return;
             }
 
             try {
                 currentCommand.run();
             } finally {
                 lock.lock();
                 try {
                     currentCommand = commands.pollFirst();
                     if (currentCommand != null && state < TERMINATED) {
                        underlyingExecutor.execute(this);
                     } else {
                         if (state == SHUTDOWN) {
                             state = TERMINATED;
                         }
                     }
                 } finally {
                     lock.unlock();
                 }
             }
         }
     };
 
     /**
      * Creates a new {@link SerialExecutor}. <p>
      * 
      * @param underlyingExecutor
      *            The underlying executor to use for executing the tasks
      *            submitted into this executor.
      * @param expectedQueueSize
      *            The expected size of the queue for this executor.
      * @param maxQueueSize
      *            The max size of the queue for this executor. Any attempt to
      *            submit a tasks when the executor's queue is full will throw an
      *            {@link RejectedExecutionException}
      */
     public SerialExecutor(Executor underlyingExecutor, int expectedQueueSize, int maxQueueSize) {
         this.maxQueueSize = maxQueueSize;
         this.underlyingExecutor = underlyingExecutor;
         this.commands = new ArrayDeque<Runnable>(expectedQueueSize);
     }
 
     public void execute(Runnable command) {
         lock.lock();
         try {
             if (state != RUNNING) {
                 throw new IllegalStateException("Executor has been shutdown");
             }
             if (currentCommand == null && commands.isEmpty()) {
                 currentCommand = command;
                 underlyingExecutor.execute(innerRunnable);
             } else {
                 if (commands.size() >= maxQueueSize) {
                     throw new RejectedExecutionException("Max queue size exceeded");
                 }
                 commands.add(command);
             }
         } finally {
             lock.unlock();
         }
     }
 
     public void shutdown() {
        state = SHUTDOWN;
     }
 
     public List<Runnable> shutdownNow() {
        state = SHUTDOWN;
         lock.lock();
         try {
            state = TERMINATED;
            ArrayList<Runnable> result = new ArrayList<Runnable>(commands);
            commands.clear();
            return result;
         } finally {
             lock.unlock();
         }
     }
 
     public boolean isShutdown() {
         return state > RUNNING;
     }
 
     public boolean isTerminated() {
         return state == TERMINATED;
     }
 
     public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
         long nanos = unit.toNanos(timeout);
         lock.lock();
         try {
             while (!isTerminated() && nanos > 0) {
                 nanos = termination.awaitNanos(nanos);
             }
         } finally {
             lock.unlock();
         }
         return isTerminated();
     }
 }
