 //
 // Treasure Data Bulk-Import Tool in Java
 //
 // Copyright (C) 2012 - 2013 Muga Nishizawa
 //
 //    Licensed under the Apache License, Version 2.0 (the "License");
 //    you may not use this file except in compliance with the License.
 //    You may obtain a copy of the License at
 //
 //        http://www.apache.org/licenses/LICENSE-2.0
 //
 //    Unless required by applicable law or agreed to in writing, software
 //    distributed under the License is distributed on an "AS IS" BASIS,
 //    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //    See the License for the specific language governing permissions and
 //    limitations under the License.
 //
 package com.treasure_data.bulk_import.prepare_parts;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Logger;
 
 public class MultiThreadPrepareProcessor {
     static class Worker extends Thread {
         private MultiThreadPrepareProcessor parent;
         private PrepareProcessor proc;
         AtomicBoolean isFinished = new AtomicBoolean(false);
 
         public Worker(MultiThreadPrepareProcessor parent, PrepareProcessor proc) {
             this.parent = parent;
             this.proc = proc;
         }
 
         @Override
         public void run() {
             while (true) {
                 PrepareProcessor.Task t = parent.taskQueue.poll();
                 if (t == null) {
                     continue;
                 } else if (PrepareProcessor.Task.endTask(t)) {
                     break;
                 }
 
                 PrepareProcessor.ErrorInfo error = proc.execute(t);
                 if (error.error != null) {
                     parent.setErrors(error);
                 }
             }
             isFinished.set(true);
         }
     }
 
     private static final Logger LOG = Logger.getLogger(MultiThreadPrepareProcessor.class.getName());
     private static BlockingQueue<PrepareProcessor.Task> taskQueue;
 
     static {
         taskQueue = new LinkedBlockingQueue<PrepareProcessor.Task>();
     }
 
     public static synchronized void addTask(PrepareProcessor.Task task) {
         taskQueue.add(task);
     }
 
     public static synchronized void addFinishTask(PrepareConfig conf) {
         for (int i = 0; i < conf.getNumOfPrepareThreads(); i++) {
             taskQueue.add(PrepareProcessor.Task.FINISH_TASK);
         }
     }
 
     private PrepareConfig conf;
     private List<Worker> workers;
     private List<PrepareProcessor.ErrorInfo> errors;
 
     public MultiThreadPrepareProcessor(PrepareConfig conf) {
         this.conf = conf;
         workers = new ArrayList<Worker>();
         errors = new ArrayList<PrepareProcessor.ErrorInfo>();
     }
 
    protected void setErrors(PrepareProcessor.ErrorInfo error) {
         errors.add(error);
     }
 
     public List<PrepareProcessor.ErrorInfo> getErrors() {
         return errors;
     }
 
     public void registerWorkers() {
         for (int i = 0; i < conf.getNumOfPrepareThreads(); i++) {
             addWorker(createWorker(conf));
         }
     }
 
     protected Worker createWorker(PrepareConfig conf) {
         return new Worker(this, createPrepareProcessor(conf));
     }
 
     protected void addWorker(Worker w) {
         workers.add(w);
     }
 
     protected PrepareProcessor createPrepareProcessor(PrepareConfig conf) {
         return new PrepareProcessor(conf);
     }
 
     public void startWorkers() {
         for (int i = 0; i < workers.size(); i++) {
             workers.get(i).start();
         }
     }
 
     public void joinWorkers() {
         long waitSec = 1 * 1000;
         while (!workers.isEmpty()) {
             Worker last = workers.get(workers.size() - 1);
             if (last.isFinished.get()) {
                 workers.remove(workers.size() - 1);
             }
 
             try {
                 Thread.sleep(waitSec);
             } catch (InterruptedException e) {
                 // ignore
             }
         }
     }
 }
