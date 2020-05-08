 package org.kevoree.watchdog;
 
 import org.kevoree.log.Log;
 import org.kevoree.watchdog.child.jvm.ChildJVM;
 import org.kevoree.watchdog.child.jvm.JVMStream;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.net.DatagramPacket;
 import java.net.DatagramSocket;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Properties;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * Created by duke on 17/05/13.
  */
 public class WatchDogCheck implements Runnable {
 
     public static Integer internalPort = 9999;
     public static Integer checkTime = 3000;
     private File runtimeFile = null;
     private File modelFile = null;
     private AtomicLong lastCheck = new AtomicLong();
     private static ScheduledExecutorService pool = Executors.newScheduledThreadPool(1);
     private Process currentProcess = null;
     public static JVMStream.LineHandler lineHandler = null;
 
     public void setModelFile(File modelFile) {
         this.modelFile = modelFile;
     }
 
     public void setRuntimeFile(File runtimeFile) {
         this.runtimeFile = runtimeFile;
     }
 
     private File sysoutFile = null;
     private File syserrFile = null;
     private BufferedWriter sysoutFileWriter = null;
     private BufferedWriter syserrFileWriter = null;
 
     public void setSysoutFile(File sysoutFile) throws IOException {
         this.sysoutFile = sysoutFile;
         this.sysoutFileWriter = new BufferedWriter(new FileWriter(sysoutFile));
         this.syserrFileWriter = this.sysoutFileWriter; //by default system.out will also be used for error
     }
 
     public void setSyserrFile(File syserrFile) throws IOException {
         this.syserrFile = syserrFile;
         this.syserrFileWriter = new BufferedWriter(new FileWriter(syserrFile));
     }
 
     @Override
     public void run() {
         Long dif = System.currentTimeMillis() - lastCheck.get();
         if (dif > checkTime) {
             System.err.println("Kevoree Runtime does not send news since " + checkTime + " force restart");
             destroyChild();
             startKevoreeProcess();
         }
     }
 
     public void destroyChild() {
        if(currentProcess!= null){
             currentProcess.destroy();
         }
         try {
             sysoutThread.stop();
             try {
                 sysoutFileWriter.flush();
             } catch (Exception e) {
             }
             try {
                 sysoutFileWriter.close();
             } catch (Exception e) {
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
         try {
             syserrThread.stop();
             try {
                 syserrFileWriter.flush();
             } catch (Exception e) {
             }
             try {
                 syserrFileWriter.close();
             } catch (Exception e) {
             }
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 
     private static Thread serverThread = null;
 
     public void startServer() {
         serverThread = new Thread(new WatchDogServer());
         serverThread.setDaemon(true);
         serverThread.start();
         lastCheck.set(System.currentTimeMillis());
         pool.scheduleAtFixedRate(this, WatchDogCheck.checkTime, WatchDogCheck.checkTime, TimeUnit.MILLISECONDS);
     }
 
     public void startKevoreeProcess() {
         ArrayList<String> childargs = new ArrayList<String>();
         ArrayList<String> childmainargs = new ArrayList<String>();
         childargs.add("-Dkevruntime=" + runtimeFile.getAbsolutePath());
         if (modelFile != null) {
             childargs.add("-Dnode.bootstrap=" + modelFile.getAbsolutePath());
         } else {
             Object nodeBoot = System.getProperty("node.bootstrap");
             if (nodeBoot != null) {
                 childargs.add("-Dnode.bootstrap=" + nodeBoot.toString());
             }
         }
         if (System.getProperty("node.name") != null && !System.getProperty("node.name").equals("")) {
             childargs.add("-Dnode.name=" + System.getProperty("node.name"));
         }
 
         if (System.getProperty("kevoree.jvmArgs") != null && !System.getProperty("kevoree.jvmArgs").equals("")) {
             String args[] = System.getProperty("kevoree.jvmArgs").substring(1,System.getProperty("kevoree.jvmArgs").length()-1).split(" ");
             Collections.addAll(childargs, args);
         }
 
 
         Properties props = System.getProperties();
         for (Object key : props.keySet()) {
 
 
             if (!key.equals("node.name") || !key.equals("node.bootstrap") || !key.toString().equals("kevoree.jvmArgs")) {
                 if (!key.toString().startsWith("os")
                         && !key.toString().startsWith("android")
                         && !key.toString().startsWith("java")
                         && !key.toString().startsWith("user")
                         && !key.toString().startsWith("line.separator")
                         && !key.toString().startsWith("sun")
                         && !key.toString().startsWith("idea")
                         && !key.toString().startsWith("path.separator")
                         && !key.toString().startsWith("file.encoding")
                         && !key.toString().startsWith("file.separator")
                         && !System.getProperty(key.toString()).equals("")) {
                     childargs.add("-D" + key + "=" + System.getProperty(key.toString()));
                 }
 
             }
         }
 
         Log.info("[Watchdog] Starting new JVM with args: " + Arrays.toString(childargs.toArray()));
 
         currentProcess = new ChildJVM.Builder()
                 .withMainClassName("org.kevoree.watchdog.child.watchdog.ChildRunner")
                 .withAdditionalCommandLineArguments(childargs)
                 .withMainClassArguments(childmainargs)
                 .withInheritClassPath(true).isolate();
 
         handleStdOutAndStdErrOf(currentProcess);
     }
 
     private Thread sysoutThread = null;
     private Thread syserrThread = null;
 
     private void handleStdOutAndStdErrOf(Process process) {
         sysoutThread = new JVMStream("stdout", process.getInputStream(), new JVMStream.LineHandler() {
 
             public void handle(String line) {
                 if (sysoutFileWriter != null) {
                     try {
                         sysoutFileWriter.append(line);
                         sysoutFileWriter.newLine();
                         sysoutFileWriter.flush();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 } else {
                     if (lineHandler != null) {
                         lineHandler.handle(line);
                     } else {
                         System.out.println(line);
                     }
                 }
             }
         });
         sysoutThread.start();
         syserrThread = new JVMStream("stderr", process.getErrorStream(), new JVMStream.LineHandler() {
 
             public void handle(String line) {
                 if (syserrFileWriter != null) {
                     try {
                         syserrFileWriter.append(line);
                         syserrFileWriter.newLine();
                         syserrFileWriter.flush();
                     } catch (IOException e) {
                         e.printStackTrace();
                     }
                 } else {
                     if (lineHandler != null) {
                         lineHandler.handle(line);
                     } else {
                         System.err.println(line);
                     }
                 }
             }
         });
         syserrThread.start();
     }
 
 
     private class WatchDogServer implements Runnable {
         @Override
         public void run() {
             DatagramSocket serverSocket = null;
             try {
                 serverSocket = new DatagramSocket(internalPort);
                 byte[] receiveData = new byte[1024];
                 while (true) {
                     DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                     serverSocket.receive(receivePacket);
                     lastCheck.set(System.currentTimeMillis());
                 }
             } catch (Exception e) {
                 e.printStackTrace();
             } finally {
                if(serverSocket != null){
                     try {
                         serverSocket.close();
                    } catch (Exception e){
                         e.printStackTrace();
                     }
                 }
             }
 
         }
     }
 
 
 }
