 /*
  *
  */
 
 package simpleWebServer;
  
 import java.io.*;
 import java.net.*;
 import java.util.*;
 
 
 abstract class WorkerPool {
     Config settings = null;
     Vector<Worker> workerPool = new Vector<Worker>();
 
     public WorkerPool(Config config) {
         this.settings = config;
     }
     
     public void init() {
         for (int i = 0; i < settings.maxWorkersInPool; ++i) {
             Worker w = createWorker(this, settings);
             (new Thread(w, "worker #"+i)).start();
             workerPool.addElement(w);
         }
     }
 
     public Worker hireWorker() {
         Worker w = null;
         synchronized (workerPool) {
             if (workerPool.isEmpty()) {
                 w = createWorker(this, settings);
                 (new Thread(w, "additional worker")).start();
             } else {
                 w = workerPool.elementAt(0);
                 workerPool.removeElementAt(0);
             }
         }
         return w;
     }
 
     public void giveBack(Worker worker) {
         synchronized (workerPool) {
             if (workerPool.size() < settings.maxWorkersInPool) {
                 workerPool.addElement(worker);
             }
         }
     }
 
    abstract protected Worker createWorker(WorkerPool, Config);
 }
 
 
 abstract class Worker implements Runnable {
 
     Config settings = null;
     WorkerPool workerPool = null;
     protected Socket currentClient = null;
 
 
     public Worker(WorkerPool coworkers, Config config) {
         this.workerPool = coworkers;
         this.settings = config;
     }
 
 
     public synchronized void youGotWorkWith(Socket newClient) {
         this.currentClient = newClient;
         notify();
     }
 
     protected boolean hasClient() {
         if (currentClient == null) {
             return false;
         } else {
             return true;
         }
     }
 
     protected void waitForNextClient() {
         if (!hasClient()) {
             try {
                 wait();
             } catch (InterruptedException e) {
                 /* should not happen */
             }
         }
     }
 
     protected void doneWithClient() {
         currentClient = null;
         workerPool.giveBack(this);
     }
 
 
     public synchronized void run() {
         while(true) {
             waitForNextClient();
 
             try {
                 handleClient();
             } catch (Exception e) {
                 e.printStackTrace();
             }
 
             doneWithClient();
         }
     }
 
     abstract protected void handleClient() throws IOException;
 }
 
 
