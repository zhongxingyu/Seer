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
 import com.github.nethad.clustermeister.api.impl.ClustermeisterFactory;
import com.github.nethad.clustermeister.api.impl.Job;
 import com.github.nethad.clustermeister.api.impl.JobFactory;
 import com.google.common.base.Joiner;
 import com.google.common.util.concurrent.FutureCallback;
 import com.google.common.util.concurrent.Futures;
 import com.google.common.util.concurrent.ListenableFuture;
 import com.google.common.util.concurrent.Monitor;
 import java.util.List;
 import java.util.concurrent.atomic.AtomicBoolean;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  *
  * @author thomas
  */
 public class Async {
     private static final int NUMBER_OF_TASKS = 1000;
     private static final Logger logger = LoggerFactory.getLogger(Async.class);
     
     private Clustermeister clustermeister;
     private AtomicBoolean futureCallbackDone = new AtomicBoolean(false);
     private Monitor monitor = new Monitor();
     private Monitor.Guard futureCallbackGuard = new Monitor.Guard(monitor) {
         @Override
         public boolean isSatisfied() {
             return futureCallbackDone.get();
         }
     };
     
     public static void main(String... args) {
         new Async().execute();
     }
 
     private void execute() {
         try {
             if (clustermeister == null) {
                 clustermeister = ClustermeisterFactory.create();
             }
             Job<Integer> job = JobFactory.create("CM Async", null);
             for (int i=0; i<NUMBER_OF_TASKS; i++) {
                 job.addTask(new ReturnNumberTask(i));
             }
             ListenableFuture<List<Integer>> resultFuture = clustermeister.executeJobAsync(job);
            
             logger.info("Returns immediately");
             Futures.addCallback(resultFuture, new FutureCallback<List<Integer>>() {
                 public void onSuccess(List<Integer> result) {
                     logger.info("Got list successfully, it contains {} items.", result.size());
                     String resultAsString = Joiner.on(", ").useForNull("null").join(result);
                     logger.info("Result: {}", resultAsString);
                     monitor.enter();
                     try {
                         futureCallbackDone.set(true);
                     } finally {
                         monitor.leave();
                     }
                 }
                 public void onFailure(Throwable t) {
                     logger.info("Could not execute job.", t);
                     try {
                         futureCallbackDone.set(true);
                     } finally {
                         monitor.leave();
                     }
                 }
             });
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
