 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package zkndb.benchmark;
 
 import java.util.ArrayList;
 import zkndb.metrics.Metric;
 import zkndb.metrics.MetricsEngine;
 import zkndb.storage.Storage;
 
 /**
  *
  * @author 4knahs
  */
 public abstract class BenchmarkUtils {
 
     public static int nStorageThreads = 1;
     public static long metricPeriod = 1000;
     public static long executionTime = 10000;
     public static int requestRate = 100;
     public static int nWrites = 1;
     public static int nReads = 1;
     public static ArrayList<Metric> sharedData = new ArrayList<Metric>();
     protected static ArrayList<Storage> storages = new ArrayList<Storage>();
     protected static ArrayList<Thread> storageThreads = new ArrayList<Thread>();
     protected static Class<?> storageClass = null;
     protected static Class<?> metricClass = null;
     protected static Class<?> engineClass = null;
     protected static Thread metricsThread;
 
     public static void setStorage(String storage) {
         storageClass = null;
         try {
             storageClass = Class.forName("zkndb.storage." + storage);
         } catch (ClassNotFoundException e) {
             System.err.println("Storage : Class not found!");
             System.exit(1);
         }
     }
 
     public static void setMetric(String metric) {
         metricClass = null;
         try {
             metricClass = Class.forName("zkndb.metrics." + metric);
         } catch (ClassNotFoundException e) {
             System.err.println("Metric : Class not found!");
             System.exit(1);
         }
     }
 
     public static void setEngine(String engine) {
         engineClass = null;
         try {
             engineClass = Class.forName("zkndb.metrics." + engine);
         } catch (ClassNotFoundException e) {
             System.err.println("Engine : Class not found!");
             System.exit(1);
         }
     }
 
     public static void run() {
         try {
             MetricsEngine engine = (MetricsEngine) engineClass.newInstance();
             engine.init();
 
             //Allocate shared data
             for (int i = 0; i < nStorageThreads; i++) {
                 Metric metric = (Metric) metricClass.newInstance();
                 metric.init();
                 sharedData.add(metric);
             }
 
             //Create storages
             for (int i = 0; i < nStorageThreads; i++) {
                 Storage stor = (Storage) storageClass.newInstance();
                 stor.init();
                stor.setId(i);
                 storages.add(stor);
             }
 
             //Run storages
             for (Storage storage : storages) {
                 Thread storeThread = new Thread(storage);
                 storageThreads.add(storeThread);
                 storeThread.start();
             }
 
             //Run metrics
             metricsThread = new Thread(engine);
             metricsThread.start();
 
             try {
                 //Calculate execution time
                 Thread.sleep(executionTime);
             } catch (InterruptedException ex) {
                 System.err.println("Could not sleep!");
             }
 
             //Stop threads
             engine.stop();
             for (Storage storage : storages) {
                 storage.stop();
             }
             
         } catch (IllegalAccessException e) {
             System.err.println("Class not accessible!");
             System.exit(1);
         } catch (InstantiationException e) {
             System.err.println("Class not instantiable!");
             System.exit(1);
         }
     }
 
     public static void readInput(String[] args) {
         if (args.length == 4) {
             nStorageThreads = new Integer(args[0]);
             metricPeriod = new Long(args[1]);
             executionTime = new Long(args[2]);
             requestRate = new Integer(args[3]);
         } else {
             if (args.length == 6) {
                 nStorageThreads = new Integer(args[0]);
                 metricPeriod = new Long(args[1]);
                 executionTime = new Long(args[2]);
                 requestRate = new Integer(args[3]);
                 nWrites = new Integer(args[4]);
                 nReads = new Integer(args[5]);
             } else {
                 System.err.println("Expected arguments: nThreads metricPeriod executionTime requestRate nWrites nReads");
                 System.exit(1);
             }
         }
 
         System.out.println("Running:\n Number of storage threads = " + nStorageThreads
                 + "\n Logging period = " + metricPeriod
                 + "\n Total execution time (ms) = " + executionTime
                 + "\n Cycle time (ms) = " + requestRate
                 + "\n Sequential writes per cycle = " + nWrites
                 + "\n Sequential reads per cycle = " + nReads
                 + "\n\n Output:");
     }
 }
