 package uk.ac.ebi.fgpt.conan.ae.lsf;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 /**
  * An adapter over an LSF process being executed as a native system process.  This adapter works by wrapping the {@link
  * File} object that the LSF process is writing it's output to and polling this file for updates.  Whenever output lines
  * are written that are recognised as LSF key phrases, events are triggered on any registered listeners.
  * <p/>
  * You can set the monitoring period to adjust the frequency of file polling operations as required.
  *
  * @author Tony Burdett
  * @date 20-Jun-2008
  */
 public class LSFProcessAdapter extends File implements LSFProcess {
     private final LSFOutputFileMonitor fileMonitor;
     private final Set<LSFProcessListener> listeners;
 
     private Thread fileMonitorThread;
 
     private boolean complete = false;
     private int exitValue = -1;
     private String processExecutionHost = "unknown";
     private List<String> processOutput = new ArrayList<String>();
 
     private int lastLineReadIndex = -1;
     private boolean commencedStdout = false;
 
     private Logger log = LoggerFactory.getLogger(getClass());
 
     public LSFProcessAdapter(String pathname, int monitoringPeriod) {
         super(pathname);
         this.fileMonitor = new LSFOutputFileMonitor(this, monitoringPeriod);
         this.listeners = new HashSet<LSFProcessListener>();
     }
 
     public LSFProcessAdapter(String parent, String child, int monitoringPeriod) {
         super(parent, child);
         this.fileMonitor = new LSFOutputFileMonitor(this, monitoringPeriod);
         this.listeners = new HashSet<LSFProcessListener>();
     }
 
     public LSFProcessAdapter(File parent, String child, int monitoringPeriod) {
         super(parent, child);
         this.fileMonitor = new LSFOutputFileMonitor(this, monitoringPeriod);
         this.listeners = new HashSet<LSFProcessListener>();
     }
 
     public LSFProcessAdapter(URI uri, int monitoringPeriod) {
         super(uri);
         this.fileMonitor = new LSFOutputFileMonitor(this, monitoringPeriod);
         this.listeners = new HashSet<LSFProcessListener>();
     }
 
     protected Logger getLog() {
         return log;
     }
 
     public void addLSFProcessListener(LSFProcessListener listener) {
         // do we have existing listeners?
         boolean startMonitor = listeners.isEmpty();
         listeners.add(listener);
         getLog().debug("Added process listener " + listener);
         // if we had no listeners, we might need to start monitoring
         if (startMonitor) {
             // clear any existing state
             complete = false;
             exitValue = -1;
             lastLineReadIndex = -1;
 
             // create new thread if required
             if (fileMonitorThread == null || !fileMonitorThread.isAlive()) {
                 getLog().debug("Creating new file monitor thread");
                 fileMonitorThread = new Thread(fileMonitor);
             }
             fileMonitorThread.start();
             getLog().debug("Started file monitor thread");
         }
     }
 
     public void removeLSFProcessListener(LSFProcessListener listener) {
         listeners.remove(listener);
         // if we have removed the last listener, stop the monitor
         fileMonitor.stop();
         getLog().debug("Removed process listener " + listener);
     }
 
     public boolean isComplete() {
         return complete;
     }
 
     public int waitForExitCode() throws InterruptedException {
         while (!isComplete()) {
             wait();
         }
         return exitValue;
     }
 
     public String getProcessExecutionHost() {
         return processExecutionHost;
     }
 
     public String[] getProcessOutput() {
         return processOutput.toArray(new String[processOutput.size()]);
     }
 
     protected void fireOutputFileDetectedEvent(final long lastModified) {
         try {
             // the list of new lines written to the file
             List<String> lines = new ArrayList<String>();
 
             // create reader
             LineNumberReader reader = new LineNumberReader(new FileReader(this));
             // read new lines, picking up where we left off
             String line;
             while ((line = reader.readLine()) != null) {
                 parseLine(line, lines);
             }
 
             // update to the last line read
             lastLineReadIndex = reader.getLineNumber();
             reader.close();
 
             // only fire completion events if already completed
             LSFProcessEvent evt = new LSFProcessEvent(lines.toArray(new String[lines.size()]), lastModified, exitValue);
             for (LSFProcessListener listener : listeners) {
                 if (complete) {
                     listener.processComplete(evt);
                     fileMonitor.stop();
                 }
             }
         }
         catch (IOException e) {
             e.printStackTrace();
             fileMonitor.stop();
         }
     }
 
     protected void fireOutputFileUpdateEvent(final long lastModified) {
         try {
             // the list of new lines written to the file
             List<String> lines = new ArrayList<String>();
 
             // create reader
             LineNumberReader reader = new LineNumberReader(new FileReader(this));
             // read new lines, picking up where we left off
             String line;
             while ((line = reader.readLine()) != null) {
                 if (reader.getLineNumber() <= lastLineReadIndex) {
                     // already read this line, ignore
                     getLog().debug("Skipping previously read content: " + line);
                 }
                 else {
                     parseLine(line, lines);
                 }
             }
 
             // update to the last line read
             lastLineReadIndex = reader.getLineNumber();
             reader.close();
 
             // now create our event and fire listeners
             LSFProcessEvent evt = new LSFProcessEvent(lines.toArray(new String[lines.size()]), lastModified, exitValue);
             for (LSFProcessListener listener : listeners) {
                 if (complete) {
                     listener.processComplete(evt);
                     fileMonitor.stop();
                 }
                 else {
                     listener.processUpdate(evt);
                 }
             }
         }
         catch (IOException e) {
             e.printStackTrace();
             fileMonitor.stop();
         }
     }
 
     protected void fireOutputFileDeleteEvent(final long lastModified) {
         // if the file is deleted, terminate our process
         fileMonitor.stop();
 
         // now create our event and fire listeners
         LSFProcessEvent evt = new LSFProcessEvent(new String[0], lastModified, -1);
         for (LSFProcessListener listener : listeners) {
             listener.processError(evt);
         }
     }
 
     private void parseLine(String line, List<String> lines) {
         // if the line indicates the LSF process is complete, set flag
         if (line.startsWith("Successfully completed")) {
             getLog().debug("Read completion content: " + line);
             exitValue = 0;
             complete = true;
         }
         else if (line.startsWith("Exited")) {
             getLog().debug("Read completion content: " + line);
             if (line.split(" ").length > 4) {
                 String exitValStr = line.split(" ")[4].trim();
                 getLog().debug("Read exit value from LSF output file, value was " + exitValStr);
                 if (exitValStr.endsWith(".")) {
                     exitValStr = exitValStr.replace(".", "");
                     getLog().debug("Munged string to remove full stop is now " + exitValStr);
                 }
                 exitValue = Integer.valueOf(exitValStr);
                 getLog().debug("Exit value: " + exitValue);
             }
             else {
                 exitValue = 1;
             }
             complete = true;
         }
         else if (line.startsWith("Job was executed on host")) {
             processExecutionHost = line.substring(line.indexOf("<") + 1, line.indexOf(">"));
         }
         else if (line.startsWith("The output (if any) follows:")) {
             commencedStdout = true;
         }
         else {
            if (commencedStdout) {
                 processOutput.add(line);
             }
             else {
                 getLog().debug("Read non-complete content: " + line);
             }
         }
 
         lines.add(line);
     }
 
 
     private class LSFOutputFileMonitor implements Runnable {
         private final File lsfOutputFile;
         private final int interval;
 
         private boolean running;
 
         private boolean fileExisted;
         private long lastModified;
         private long lastLength;
 
         private LSFOutputFileMonitor(File lsfOutputFile, int interval) {
             this.lsfOutputFile = lsfOutputFile;
             this.interval = interval;
 
             this.running = true;
             this.fileExisted = false;
             this.lastModified = -1;
             this.lastLength = -1;
         }
 
         public void run() {
             getLog().debug("Starting file monitor for " + lsfOutputFile.getAbsolutePath());
             while (running) {
                 synchronized (lsfOutputFile) {
                     // check the lsfOutputFile exists
                     if (lsfOutputFile.exists()) {
                         // we have found our file
                         if (!fileExisted) {
                             // the lsfOutputFile has been detected for the first time
                             fileExisted = true;
                             getLog().debug("File detected at " + lsfOutputFile.lastModified() + " " +
                                     "(size " + lsfOutputFile.length() + ")");
                             fireOutputFileDetectedEvent(lsfOutputFile.lastModified());
                         }
                         else {
                             // check for modifications
                             if (lsfOutputFile.lastModified() > lastModified ||
                                     lsfOutputFile.length() != lastLength) {
                                 // the lsfOutputFile has been updated since we last checked
                                 getLog().debug("File updated: " +
                                         "modified -  " + lsfOutputFile.lastModified() + " " +
                                         "(previously " + lastModified + "); " +
                                         "size - " + lsfOutputFile.length() + " " +
                                         "(previously " + lastLength + ")");
                                 fireOutputFileUpdateEvent(lsfOutputFile.lastModified());
                             }
                         }
                     }
                     else {
                         if (fileExisted) {
                             // the lsfOutputFile was found before, so it definitely existed at some point...
                             // therefore it has been deleted by an external process
                             getLog().debug("File previously existed but has been deleted");
                             fireOutputFileDeleteEvent(lsfOutputFile.lastModified());
                         }
                     }
 
                     // updated the lastModified time
                     lastModified = lsfOutputFile.lastModified();
                     lastLength = lsfOutputFile.length();
                 }
 
                 // sleep for interval seconds
                 synchronized (this) {
                     try {
                         wait(interval * 1000);
                     }
                     catch (InterruptedException e) {
                         // if interrupted, die
                         getLog().debug("Interrupted exception causing thread to die");
                         stop();
                     }
                 }
             }
             getLog().debug("Stopping file monitor for " + lsfOutputFile.getAbsolutePath());
         }
 
         public void stop() {
             running = false;
         }
     }
 }
