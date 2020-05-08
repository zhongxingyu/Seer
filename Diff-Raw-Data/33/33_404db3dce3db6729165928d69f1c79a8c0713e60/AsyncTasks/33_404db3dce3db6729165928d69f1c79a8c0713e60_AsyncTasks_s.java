 /*
  * Copyright 2012 The Clustermeister Team.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package com.github.nethad.clustermeister.example.async;
 
 import com.github.nethad.clustermeister.api.Clustermeister;
import com.github.nethad.clustermeister.api.Job;
 import com.github.nethad.clustermeister.api.impl.ClustermeisterFactory;
 import com.github.nethad.clustermeister.api.impl.JobFactory;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.Monitor;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicInteger;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author thomas
  */
 public class AsyncTasks {
     private static final int NUMBER_OF_TASKS = 100;
     
     private static final Logger logger = LoggerFactory.getLogger(AsyncTasks.class);
     
     public static void main(String... args) {
         new AsyncTasks().execute();
     }
     private Clustermeister clustermeister;
     private AtomicInteger callbackCounter = new AtomicInteger(0);
     private Monitor monitor = new Monitor();
     private Monitor.Guard futureCallbackGuard = new Monitor.Guard(monitor) {
         @Override
         public boolean isSatisfied() {
             return NUMBER_OF_TASKS == callbackCounter.get();
         }
     };
 
     private void execute() {
                 try {
             if (clustermeister == null) {
                 clustermeister = ClustermeisterFactory.create();
             }
             Job<String> job = JobFactory.create("CM AsyncTasks", null);
             for (int i=0; i<NUMBER_OF_TASKS; i++) {
                 job.addTask(new ReturnNumberTaskWithSleep(i, 2000));
             }
             List<ListenableFuture<String>> resultFutures = clustermeister.executeJobAsyncTasks(job);
             FutureCallback<String> callback = new FutureCallback<String>() {
                 public void onSuccess(String result) {
                     logger.info("Got result {}", result);
                     monitor.enter();
                     try {
                         callbackCounter.incrementAndGet();
                     } finally {
                         monitor.leave();
                     }
                 }
                 public void onFailure(Throwable t) {
                     logger.warn("Failure.", t);
                     monitor.enter();
                     try {
                         callbackCounter.incrementAndGet();
                     } finally {
                         monitor.leave();
                     }
                 }
             };
 
             for (ListenableFuture<String> listenableFuture : resultFutures) {
                 Futures.addCallback(listenableFuture, callback);
             }
 
             logger.info("Registered callback, waiting for it to be finished.");
             monitor.enter();
             try {
                 monitor.waitFor(futureCallbackGuard);
                 logger.info("Future callback done.");
             } finally {
                 monitor.leave();
             }
         } catch (Exception ex) {
             logger.warn("Exception while executing example.", ex);
         } finally {
             if (clustermeister != null) {
                 logger.info("Shutting down.");
                 clustermeister.shutdown();
             }
         }
     }
     
 }
