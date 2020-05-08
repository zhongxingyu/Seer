 package org.cp.monitor;
 
 import org.apache.log4j.Logger;
 import org.cp.monitor.resources.ResourceA;
 import org.cp.monitor.resources.ResourceB;
 import org.cp.monitor.resources.ResourcesPool;
 import org.cp.monitor.threads.MonitorA;
 import org.cp.monitor.threads.MonitorAB;
 import org.cp.monitor.threads.MonitorB;
 import org.cp.monitor.threads.MonitorRunnable;
 
 import java.io.FileReader;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created by: Nappa
  * Version: 0.01
  * Since: 0.01
  */
 public class CPMonitor {
 
     private static Logger LOGGER=Logger.getLogger(CPMonitor.class);
 
     private static List<MonitorRunnable> monitors = new ArrayList<>();
 
     public static void main(String[] args) throws Exception {
         init(Arrays.asList(args));
 
         run();
 
     }
 
     private static void run() throws InterruptedException {
         final ExecutorService executorService = Executors.newCachedThreadPool();
         for (final Runnable runnable : monitors) {
             LOGGER.info(String.format("Bootstrapped with thread=%s", executorService.submit(runnable)));
         }
         executorService.shutdown();
         executorService.awaitTermination(1, TimeUnit.MINUTES);
     }
 
     private static void init(List<String> args) throws Exception {
         if (args.contains("-p")) {
             int pIndex = args.indexOf("-p");
 
             if (pIndex + 1 < args.size()) {
                 final String pathToProperties = args.get(pIndex + 1);
                 final Properties properties = new Properties();
 
                 try {
                     properties.load(new FileReader(pathToProperties));
                     LOGGER.info("Reading configuration");
                     MonitorRunnable.RESOURCE_A_RESOURCES_POOL = new ResourcesPool<>(ResourceA.class,
                             Integer.parseInt(properties.getProperty("resourcesA.amount")));
                     MonitorRunnable.RESOURCE_B_RESOURCES_POOL=new ResourcesPool<>(ResourceB.class,
                             Integer.parseInt(properties.getProperty("resourcesB.amount")));
                     MonitorRunnable.roundsAmount= Integer.parseInt(properties.getProperty("rounds.amount"));
                     createThreads(MonitorA.class.getName(),Integer.parseInt(properties.getProperty("monitorA.amount")));
                     createThreads(MonitorB.class.getName(),Integer.parseInt(properties.getProperty("monitorB.amount")));
                     createThreads(MonitorAB.class.getName(),Integer.parseInt(properties.getProperty("monitorAB.amount")));
                     LOGGER.info("Program configured");
                 } catch (Exception e) {
                     throw new RuntimeException(e);
                 }
 
             } else {
                 throw new RuntimeException("Failed to read properties file, missing path to properties file");
             }
         } else {
             throw new RuntimeException("Failed to read properties file, missing cmd argument -p");
         }
 
     }
 
     private static void createThreads(String strClazz, int numberOfThreads) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException, ClassNotFoundException {
 
        Class clazz=Class.forName(strClazz);
 
        Constructor constructor=clazz.getConstructor(Integer.class);
 
         for(int i=0;i<numberOfThreads;i++){
            monitors.add((MonitorRunnable) constructor.newInstance(i));
         }
     }
 }
