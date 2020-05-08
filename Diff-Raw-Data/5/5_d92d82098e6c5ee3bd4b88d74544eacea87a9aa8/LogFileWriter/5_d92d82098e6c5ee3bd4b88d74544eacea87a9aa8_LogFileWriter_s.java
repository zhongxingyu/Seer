 
 package org.logtools.core.writer.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.concurrent.BrokenBarrierException;
 import java.util.concurrent.CyclicBarrier;
 import java.util.concurrent.LinkedBlockingDeque;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.logtools.Const;
 import org.logtools.Exception.OutputResultException;
 import org.logtools.core.domain.LogEntry;
 import org.logtools.core.writer.LogWriter;
 
 public class LogFileWriter implements LogWriter {
 
     private static Logger logger = Logger.getLogger(LogFileWriter.class);
 
     private File outputFile;
 
     private Thread ioThread;
 
     private int MaxTryStopTimes = Integer.MAX_VALUE;
 
     private CyclicBarrier cyclicbarrier;
 
     private Object alarm;
 
     LinkedBlockingDeque<String> outputQueue;
 
     /*
      * try to use the property to stop thread
      */
     private boolean running = false;
 
     public LogFileWriter() {
 
     }
 
     public LogFileWriter(File outputFile) {
         super();
         this.outputFile = outputFile;
     }
 
     public void writeOneLine(String line) {
         outputQueue.offer(line);
     }
 
     public void writeOneLogEntry(LogEntry logEntry) {
         this.writeOneLine(logEntry.getContent());
     }
 
     public void start() {
 
         alarm = new Object();
 
         running = true;
         outputQueue = new LinkedBlockingDeque<String>();
 
         cyclicbarrier = new CyclicBarrier(1, new Runnable() {
             public void run() {
                 synchronized (alarm) {
                     alarm.notifyAll();
                 }
             }
         });
         FileWriterThread fwt = new FileWriterThread();
         ioThread = new Thread(fwt);
         ioThread.setDaemon(true);
         ioThread.start();
     }
 
     public void close() {
         running = false;
         synchronized (alarm) {
             try {
                 alarm.wait();
             } catch (InterruptedException e) {
                 OutputResultException ex = new OutputResultException();
                 ex.initCause(e);
                 throw ex;
             }
         }
 
         if (outputQueue.size() > 0) {
             logger.info(outputQueue.size() + " records not be output");
         }
     }
 
     public File getOutputFile() {
         return outputFile;
     }
 
     public void setOutputFile(File outputFile) {
         this.outputFile = outputFile;
     }
 
     /**
      * this flush has a bug <br>
      * if there are 3 records in outputQueue when ready to flush <br>
      * 1<br>
      * 2<br>
      * 2<br>
      * the third record won't be output to file when the fush finish. Becuase it's not very important her, so I ignore
      */
     public void flush() {
         String lastRecord = outputQueue.getLast();
 
         // nothing need to flush
         if (lastRecord == null) {
             return;
         }
 
         while (outputQueue.contains(lastRecord)) {
             try {
                 Thread.sleep(100);
             } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
             }
         }
 
     }
 
     public int getMaxTryStopTimes() {
         return MaxTryStopTimes;
     }
 
     public void setMaxTryStopTimes(int maxTryStopTimes) {
         MaxTryStopTimes = maxTryStopTimes;
     }
 
     private class FileWriterThread implements Runnable {
 
         private int tryStopTimes = 0;
 
         public void run() {
 
             while (this.keepRunning()) {
                 this.writeLine();
             }
 
             try {
                 cyclicbarrier.await();
             } catch (InterruptedException e) {
                 OutputResultException ex = new OutputResultException();
                 ex.initCause(e);
                 throw ex;
             } catch (BrokenBarrierException e) {
                 OutputResultException ex = new OutputResultException();
                 ex.initCause(e);
                 throw ex;
             }
         }
 
         public void writeLine() {
             try {
                 StringBuilder stringbuilder = new StringBuilder();
                 String line = outputQueue.poll(20, TimeUnit.MILLISECONDS);
 
                 if (StringUtils.isBlank(line)) {
                     return;
                 }
                 stringbuilder.append(line);
                 stringbuilder.append(Const.NEW_LINE);
                 FileUtils.writeStringToFile(outputFile, stringbuilder.toString(), true);
             } catch (InterruptedException e) {
                 OutputResultException ex = new OutputResultException();
                 ex.initCause(e);
                 throw ex;
             } catch (IOException e) {
                 OutputResultException ex = new OutputResultException();
                 ex.initCause(e);
                 throw ex;
             }
         }
 
         private boolean keepRunning() {
 
             if (running) {
                 return true;
             }
 
             if (outputQueue.isEmpty() || (tryStopTimes > MaxTryStopTimes)) {
                 return false;
             }
             tryStopTimes++;
             return true;
         }
     }
 }
