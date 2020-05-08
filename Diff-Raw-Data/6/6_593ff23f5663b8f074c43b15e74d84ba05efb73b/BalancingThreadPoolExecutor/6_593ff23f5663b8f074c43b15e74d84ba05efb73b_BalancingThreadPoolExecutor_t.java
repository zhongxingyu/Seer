 /*
  * Copyright 2012-2013 Ray Holder
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.github.rholder.moar.concurrent.thread;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.AbstractExecutorService;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import static java.lang.Math.ceil;
 
 /**
  * This is a rough implementation of an auto-balancing thread pool to optimize
  * for the following condition: optimal pool size = N * U * (1 + (W/C)) where N
  * is the number of CPU's, U is the desired utilization, W is the time each
  * thread spends waiting, and C is the time each thread spends using the CPU.
  */
 public class BalancingThreadPoolExecutor extends AbstractExecutorService {
 
     private static final int CPUS = Runtime.getRuntime().availableProcessors();
 
     private final float targetUtilization;
 
     private final ConcurrentHashMap<Thread, Tracking> liveThreads;
     private final ThreadPoolExecutor threadPoolExecutor;
     private final ThreadProfiler threadProfiler;
     private final AtomicInteger tasksRun;
     private final float smoothingWeight;
     private final int balanceAfter;
 
     public BalancingThreadPoolExecutor(ThreadPoolExecutor threadPoolExecutor,
                                        ThreadProfiler threadProfiler,
                                        float targetUtilization,
                                        float smoothingWeight,
                                        int balanceAfter) {
 
         if (targetUtilization <= 0.0 || targetUtilization > 1.0) {
             throw new IllegalArgumentException();
         }
 
         if (threadPoolExecutor == null) {
             throw new NullPointerException();
         }
 
         this.threadPoolExecutor = threadPoolExecutor;
         this.threadProfiler = threadProfiler;
         this.targetUtilization = targetUtilization;
         this.liveThreads = new ConcurrentHashMap<Thread, Tracking>();
         this.tasksRun = new AtomicInteger(0);
         this.smoothingWeight = smoothingWeight;
         this.balanceAfter = balanceAfter;
     }
 
     @Override
     public void shutdown() {
         threadPoolExecutor.shutdown();
     }
 
     @Override
     public List<Runnable> shutdownNow() {
         return threadPoolExecutor.shutdownNow();
     }
 
     @Override
     public boolean isShutdown() {
         return threadPoolExecutor.isShutdown();
     }
 
     @Override
     public boolean isTerminated() {
         return threadPoolExecutor.isTerminated();
     }
 
     @Override
     public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
         return threadPoolExecutor.awaitTermination(timeout, unit);
     }
 
     @Override
     public void execute(final Runnable command) {
         // wrap the Runnable such that we can collect profiling on the given tasks
         threadPoolExecutor.execute(new Runnable() {
             @Override
             public void run() {
                 Thread thisThread = Thread.currentThread();
                 long threadId = thisThread.getId();
                 long startTime = threadProfiler.getThreadWaitTime(threadId);
                 long startCpu = threadProfiler.getThreadCpuTime(threadId);
                 try {
                     command.run();
                 } finally {
                     Tracking tracking = liveThreads.get(thisThread);
                     long totalCpuTime = threadProfiler.getThreadCpuTime(threadId) - startCpu;
                     long totalTime = threadProfiler.getThreadWaitTime(threadId) - startTime;
                     if(tracking == null) {
                         // this is an untracked thread, add tracking
                         tracking = new Tracking();
                         tracking.avgTotalTime = totalTime;
                         tracking.avgCpuTime = totalCpuTime;
                         liveThreads.put(thisThread, tracking);
                     } else {
                         // TODO determine exponential smoothing coefficient to specify weight of each task over time
                         // compute exponential moving averages, see http://en.wikipedia.org/wiki/Exponential_smoothing
                         tracking.avgTotalTime += smoothingWeight * (totalTime - tracking.avgTotalTime);
                         tracking.avgCpuTime += smoothingWeight * (totalCpuTime - tracking.avgCpuTime);
                     }
 
                     int count = tasksRun.getAndIncrement();
                     if(count % balanceAfter == 0) {
                         balance();
                     }
                 }
             }
         });
     }
 
     /**
      * Compute and set the optimal number of threads to use in this pool.
      */
     private void balance() {
         // only try to balance when we're not terminating
         if(!isTerminated()) {
             Set<Map.Entry<Thread, Tracking>> threads = liveThreads.entrySet();
             long liveAvgTimeTotal = 0;
             long liveAvgCpuTotal = 0;
             long liveCount = 0;
             for (Map.Entry<Thread, Tracking> e : threads) {
                 if (!e.getKey().isAlive()) {
                     // thread is dead or otherwise hosed
                     threads.remove(e);
                 } else {
                     liveAvgTimeTotal += e.getValue().avgTotalTime;
                     liveAvgCpuTotal += e.getValue().avgCpuTime;
                     liveCount++;
                 }
             }
             long waitTime = 1;
             long cpuTime = 1;
             if(liveCount > 0) {
                 waitTime = liveAvgTimeTotal / liveCount;
                 cpuTime = liveAvgCpuTotal / liveCount;
             }
 
            int size = 1;
            if(cpuTime > 0) {
                size = (int) ceil((CPUS * targetUtilization * (1 + (waitTime / cpuTime))));
            }
             size = Math.min(size, threadPoolExecutor.getMaximumPoolSize());
 
             // TODO remove debugging
             //System.out.println(waitTime / 1000000 + " ms");
             //System.out.println(cpuTime / 1000000 + " ms");
             //System.out.println(size);
 
             threadPoolExecutor.setCorePoolSize(size);
         }
     }
 }
