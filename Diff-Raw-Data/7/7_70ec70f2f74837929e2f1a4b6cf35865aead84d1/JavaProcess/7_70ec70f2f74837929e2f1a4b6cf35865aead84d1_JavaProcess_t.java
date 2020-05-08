 package org.jegrid.util;
 
 import org.apache.log4j.Logger;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.*;
 
 /**
  * Launches a JVM in a separate process using the specified classpath and main class.  The standard
  * output and error streams are piped into the parent's streams.
  *
  * @author josh Jan 4, 2005 10:11:37 PM
  */
 public class JavaProcess
 {
     private static Logger log = Logger.getLogger(JavaProcess.class);
 
     private Process process;
     private String classpath;
     private Properties sysprops;
     private String mainClass;
     private String[] args;
     private OutputStream out;
     private OutputStream err;
     private Thread outThread;
     private Thread errThread;
 
     public JavaProcess(String mainClass)
     {
         this.mainClass = mainClass;
         out = System.out;
         err = System.err;
     }
 
 
     public void setOutStream(OutputStream out)
     {
         this.out = out;
     }
 
     public void setErrorStream(OutputStream err)
     {
         this.err = err;
     }
 
     public String getClasspath()
     {
         if (classpath == null || classpath.length() == 0)
             return System.getProperty("java.class.path");
         else
             return classpath;
     }
 
     public void setClasspath(String classpath)
     {
         this.classpath = classpath;
     }
 
     public void appendClasspath(String classpath)
     {
         String old = getClasspath();
         setClasspath(old + File.pathSeparator + classpath);
     }
 
     public void setArgs(String[] args)
     {
         this.args = args;
     }
 
     public void setSysprops(Properties props)
     {
         this.sysprops = props;
     }
 
     public void start() throws IOException
     {
         if (process != null)
             stopIt();
        log.info("start() : " + mainClass + " args: " + (args != null ? Arrays.asList(args) : "<none>"));
         Runtime rt = Runtime.getRuntime();
         List commandLine = new ArrayList();
         commandLine.add(getJavaCommand());
         commandLine.add("-classpath");
         String cp = getClasspath();
         // TODO: Chop the classpath up and remove duplicates.
         commandLine.add(cp);
         if (sysprops != null && sysprops.size() > 0)
         {
             Enumeration en = sysprops.propertyNames();
             while (en.hasMoreElements())
             {
                 String key = (String) en.nextElement();
                 String value = (String) sysprops.get(key);
                 commandLine.add("-D" + key + "=" + value);
             }
         }
         commandLine.add(mainClass);
 
         if (args != null && args.length > 0)
             commandLine.addAll(Arrays.asList(args));
 
         String[] cmdarray = (String[]) commandLine.toArray(new String[commandLine.size()]);
 
         process = rt.exec(cmdarray);
         StreamCopier stdout = new StreamCopier(process.getInputStream(), out);
         StreamCopier stderr = new StreamCopier(process.getErrorStream(), err);
         errThread = new Thread(stderr);
         errThread.setDaemon(true);
         errThread.setPriority(Thread.MIN_PRIORITY);
         errThread.start();
         outThread = new Thread(stdout);
         outThread.setDaemon(true);
         outThread.setPriority(Thread.MIN_PRIORITY);
         outThread.start();
         log.info("Process started.");
     }
 
     private String getJavaCommand()
     {
         return Util.getJavaCommand();
     }
 
     public void kill()
     {
         log.info("Kill...");
         stopIt();
         int rc = 0;
         try
         {
             rc = process.waitFor();
         }
         catch (InterruptedException ignore)
         {
             // ignore
         }
         log.info("Process killed.  return code = " + rc);
     }
 
     private void stopIt()
     {
         if (errThread != null)
         {
             stopThread(errThread);
         }
         if (outThread != null)
         {
             stopThread(outThread);
         }
         if (process != null)
         {
             process.destroy();
         }
     }
 
     private void stopThread(Thread t)
     {
         t.interrupt();
     }
 
     /**
      * Waits for the process to complete.
      *
      * @return the return code from the process.
      * @throws InterruptedException if the thread is interrupted.
      */
     public int waitFor() throws InterruptedException
     {
         if (process == null)
             throw new RuntimeException("No process!");
         log.info("Waiting ...");
         int rc = process.waitFor();
         stopIt();
         log.info("Process completed.  return code = " + rc);
         return rc;
     }
 }
