 package org.kevoree.boot.child.watchdog;
 
 import org.kevoree.boot.WatchDogCheck;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /**
  * Created by duke on 17/05/13.
  */
 public class ChildRunner {
 
     private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
     private static WatchdogClient client = new WatchdogClient();
 
     public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {
         System.out.println("Kevoree Daemon");
 
         pool.scheduleAtFixedRate(client, 0, WatchDogCheck.checkTime / 2, TimeUnit.MILLISECONDS);
 
 
         URL[] kevURLS = new URL[1];
         Object kevRuntime = System.getProperty("kevruntime");
         kevURLS[0] = new File(kevRuntime.toString()).toURI().toURL();
         URLClassLoader cl = new URLClassLoader(kevURLS);
        Class miniMainClass = cl.loadClass("org.kevoree.platform.standalone.App");
         Method mainM = miniMainClass.getMethod("main", String[].class);
         String[] argsFork = new String[0];
         mainM.invoke(null, (Object) argsFork);
     }
 
 }
