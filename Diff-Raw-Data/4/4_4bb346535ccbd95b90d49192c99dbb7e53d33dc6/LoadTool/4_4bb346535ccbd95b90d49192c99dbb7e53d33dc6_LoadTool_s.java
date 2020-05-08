 package org.amplafi.flow.utils;
 import java.io.*;
 import java.net.*;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import org.amplafi.dsl.ScriptRunner;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import static org.amplafi.flow.utils.LoadToolCommandLineOptions.*;
 import java.util.EnumSet;
 
 /**
  * Tool for load testing the wire server.
  * @author paul
  */
 public class LoadTool extends UtilParent{
     /**
      * Main method for proxy server.
      * See TestGenerationProxyCommandLineOptions for usage.
      */
     public static void main(String[] args) throws IOException {
         LoadTool proxy = new LoadTool();
 
         try {
             proxy.processCommandLine(args);
 
         } catch (Exception e) {
             proxy.getLog().error(e);
         }
     }
 
     private Log log;
 
     private static final String THICK_DIVIDER =
     "*********************************************************************************";
 
     public LoadTool(){
     }
 
     /** File to write the test report to. If null write to screen. */
     private String reportFile;
 
     private boolean running = true;
 
 
     /**
      * Process command line and run the server.
      * @param args
      */
     public void processCommandLine(String[] args) {
         // Process command line options.
         LoadToolCommandLineOptions cmdOptions = null;
         try {
                 cmdOptions = new LoadToolCommandLineOptions(args);
         } catch (ParseException e) {
                 getLog().error("Could not parse passed arguments, message:", e);
                 return;
         }
         // Print help if there has no args.
         if (args.length == 0) {
                 cmdOptions.printHelp();
                 return;
         }
 
         if (!cmdOptions.hasOption(HOST)){
             getLog().error("You must specify the host.");
         }
 
         if (!cmdOptions.hasOption(HOST_PORT) ){
             getLog().error("You must specify the host post.");
         }
 
         if (!cmdOptions.hasOption(SCRIPT)  ){
             getLog().error("You must specify the script to run.");
         }
 
 
         if (cmdOptions.hasOption(HOST) && cmdOptions.hasOption(HOST_PORT) && cmdOptions.hasOption(SCRIPT)  ) {
 
             int remotePort = -1;
             int numThreads = 1;
             int frequency = -1;
 
             try {
                 remotePort = Integer.parseInt(cmdOptions.getOptionValue(HOST_PORT));
             } catch (NumberFormatException nfe) {
                 getLog().error("Remote port should be in numeric form e.g. 80");
                 return;
             }
 
             String host = cmdOptions.getOptionValue(HOST);
             String reportFile = cmdOptions.getOptionValue(REPORT);
             String scriptName = cmdOptions.getOptionValue(SCRIPT);
 
 
             try {
                 if (cmdOptions.hasOption(NUM_THREADS)){
                     numThreads = Integer.parseInt(cmdOptions.getOptionValue(NUM_THREADS));
                 }
             } catch (NumberFormatException nfe) {
                 getLog().error("numThreads should be in numeric form e.g. 10 , defaulting to 1.");
 
             }
 
             try {
                 if (cmdOptions.hasOption(FREQUENCY)){
                     frequency = Integer.parseInt(cmdOptions.getOptionValue(FREQUENCY));
                 }
             } catch (NumberFormatException nfe) {
                 getLog().error("frequency should be in numeric form e.g. 10 , defaulting to -1 = max possible.");
 
             }
 
             // Register shutdown handler.
             Runtime.getRuntime().addShutdownHook(new Thread(){
                 public void run() {
                     getLog().info("Generating Report. Please Wait...");
 
                     // signal threads to stop
                     running = false;
 
                     // Wait for all threads to stop
                     for (Thread t : threads){
                         try {
                             t.join();
                         } catch (InterruptedException ie){
                             getLog().error("Error",ie);
                         }
                     }
 
                     // Loop over the ThreadReports
                     int threadNum = 1;
                    long totalTime = 0;
                    long totalCalls = 0;
                     getLog().info(THICK_DIVIDER);
                     for (Thread t : threads){
                        ThreadReport rep = threadReports.get(t);
                        totalTime = rep.endTime - rep.startTime;
                        getLog().info("threadNum" + threadNum + ": calls " + rep.callCount + " in " + (totalTime/1000) + "s. average = " + (rep.callCount*1000/totalTime) + " calls per second. Error count " +  rep.errorCount);
                        totalCalls += rep.callCount;
                        threadNum++;
                     }
                     getLog().info(THICK_DIVIDER);
                     getLog().info("Total calls in all threads=" + totalCalls + "  " + (totalCalls*1000/totalTime) +  " calls per second" );
                     getLog().info(THICK_DIVIDER);
 
                     // TODO output above to file or speadsheet
                     // TODO output time variant call data to speadsheet
                 }
             });
 
             String key =  "";
             try {
                 key = getOption(cmdOptions, API_KEY, "");
             } catch (IOException ioe) {
                 getLog().error("Reading API Key", ioe);
                 return;
             }
 
 
             try {
                 runLoadTest(host, key, remotePort, scriptName,  numThreads, frequency ); // never returns
             } catch (IOException ioe) {
                 getLog().error("Error running proxy", ioe);
                 return;
             }
         }
     }
 
     // Never accessed by multiple threads
     private List<Thread> threads = new ArrayList<Thread>();
     // Never accessed by multiple threads
     private Map<Thread,ThreadReport> threadReports = new LinkedHashMap();
 
     /**
      * runs a single-threaded proxy server on
      * the specified local port. It never returns.
      */
     public void runLoadTest(final String host,final String key,final int port,final String scriptName,final int numThreads,final int frequency )
             throws IOException {
         getLog().info("Running LoadTest with host=" + host + " host port=" + port + " script=" + scriptName + " numThreads=" + numThreads + " frequency=" + frequency);
         getLog().info("Press Ctrl+C to stop");
 
         for (int i=0; i<numThreads ; i++ ){
             final ThreadReport report = new ThreadReport();
             Thread thread = new Thread(new Runnable(){
                 public void run() {
                     ScriptRunner scriptRunner = new ScriptRunner(host, ""+port, "apiv1", key);
 
                     try {
                         // don't include the first run because this includes
                         // constructing gropvy runtime.
                         scriptRunner.loadAndRunOneScript(scriptName);
 
                         report.startTime = System.currentTimeMillis();
                         while (running){
                             try {
 
                                 report.callCount++;
                                 long startTime = System.currentTimeMillis();
                                 scriptRunner.reRunLastScript();
                                 long endTime = System.currentTimeMillis();
                                 long duration = (endTime - startTime);
 
                                 if (frequency != -1 ){
                                     int requiredDurationMS = 1000/frequency;
                                     if (duration < requiredDurationMS){
                                         long pause = requiredDurationMS - duration;
                                         Thread.currentThread().sleep(pause);
                                     }
 
                                 }
 
                                 report.callTimes.add(duration);
                             } catch (Throwable t){
                                 report.errors.add(t);
                                 report.errorCount++;
                             }
                         }
                         report.endTime = System.currentTimeMillis();
 
                     } catch (Throwable t){
 
                         getLog().error("Error on first run",t);
                     }
 
                 }// End run
 
             });
             threads.add(thread);
             threadReports.put(thread, report);
         }
 
         for (Thread t : threads){
             t.start();
         }
     }
 
     /**
      * Get the logger for this class.
      */
     public Log getLog() {
         if ( this.log == null ) {
             this.log = LogFactory.getLog(this.getClass());
         }
         return this.log;
     }
 
     /**
      *  This class holds the load test for a single thread.
      */
     private class ThreadReport {
         int callCount = 0;
         int errorCount = 0;
         List<Throwable> errors = new ArrayList<Throwable>();
         long startTime = 0;
         long endTime = 0;
         List<Long> callTimes = new ArrayList<Long>();
     }
 
 
 }
