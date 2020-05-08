 package com.jentfoo;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Future;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import org.threadly.concurrent.SubmitterSchedulerInterface;
 import org.threadly.util.Clock;
 import org.threadly.util.ExceptionUtils;
 
 public class MencoderConverter implements ConverterInterface {
   private static final boolean VERBOSE = true;
   //private static final String FLAGS = "-oac mp3lame -lameopts vol=5.5 " +
   private static final String FLAGS = "-oac mp3lame -ovc xvid -xvidencopts fixed_quant=2 -sws 8";
   //private static final String FLAGS = "-oac mp3lame -ovc lavc -lavcopts vcodec=mpeg4:vhq:vbitrate=8000";
   private static final String DESIRED_EXTENSION = ".avi";
   
   @Override
   public String getProducedExtesion() {
     return DESIRED_EXTENSION;
   }
   
   @Override
   public Map<File, Future<?>> submitJobs(SubmitterSchedulerInterface scheduler, 
                                          List<File> sourceFileList, 
                                          File destFolder) {
     File[] origDestFileArray = destFolder.listFiles();
     Map<File, Future<?>> result = new HashMap<File, Future<?>>();
     AtomicInteger processedCount = new AtomicInteger();
     
     Iterator<File> it = sourceFileList.iterator();
     while (it.hasNext()) {
       File sourceFile = it.next();
 
       if (sourceFile.isDirectory()) {  // don't try and recursively copy/encode directories
         continue;
       }
       
       File newFile = FileUtils.makeNewFile(destFolder, sourceFile, DESIRED_EXTENSION);
       
       if (FileUtils.fileInArray(origDestFileArray, newFile)) {
         // skip file, already added to processedFiles
         processedCount.incrementAndGet();
         
         continue;
       }
       
       Future<?> future = scheduler.submit(new ConverterWorker(processedCount, 
                                                               sourceFileList.size(), 
                                                               sourceFile, newFile));
       result.put(sourceFile, future);
     }
     
     return result;
   }
   
   private static class ConverterWorker implements Runnable {
     private final long creationTime;
     private final long originalSize;
     private final AtomicInteger processedCount;
     private final int totalProcessCount;
     private final File sourceFile;
     private final File newFile;
     
     private ConverterWorker(AtomicInteger processedCount, 
                             int totalProcessCount, 
                             File sourceFile, File newFile) {
       originalSize = sourceFile.length();
       creationTime = Clock.lastKnownTimeMillis();
       
       this.processedCount = processedCount;
       this.totalProcessCount = totalProcessCount;
       this.sourceFile = sourceFile;
       this.newFile = newFile;
     }
     
     @Override
     public void run() {
       // verify file is not still growing before continuing
       if (! FileUtils.sizeStable(sourceFile, originalSize, creationTime)) {
         processedCount.incrementAndGet();
         
         return;
       }
       
       String extension = FileUtils.getExtension(sourceFile.getName());
       
       if (! extension.equalsIgnoreCase(DESIRED_EXTENSION)) {
         // convert
         if (VERBOSE) {
           /*System.out.println("Running: " + "mencoder " + sourceFile.getAbsolutePath() + 
                                " "  + FLAGS + " -o " + newFile.getAbsolutePath());*/
           System.out.println("Encoding " + extension + " file to: " + newFile.getAbsolutePath());
         }
         
         try {
           encodeFile(sourceFile, newFile);
         } catch (IOException e) {
           throw ExceptionUtils.makeRuntime(e);
         } catch (InterruptedException e) {
           ExceptionUtils.handleException(e);
           return;
         }
       } else {
         // copy the file
         if (VERBOSE) {
           System.out.println("Copying file to: " + newFile.getAbsolutePath());
         }
         
         try {
           FileUtils.copyFile(sourceFile, newFile);
         } catch (IOException e) {
           throw ExceptionUtils.makeRuntime(e);
         }
       }
 
       int count = processedCount.incrementAndGet();
          
       if (VERBOSE) {
         String percent = Double.toString(((count / (double)totalProcessCount)) * 100);
         System.out.println("Estimated % done: " + 
                              percent.substring(0, Math.min(percent.length() - 1, 5)) + "%" + 
                              " - ( " + count + " out of " + totalProcessCount + " )\n");
       }
     }
     
     private static void encodeFile(File sourceFile, File destFile) throws IOException, InterruptedException {
      String command[] = {"bash", 
                          "-c", 
                          "mencoder '" + sourceFile.getAbsolutePath() + '\'' + 
                            " "  + FLAGS + " -o '" + destFile.getAbsolutePath() + '\''
                         };
       Process p = Runtime.getRuntime().exec(command);
 
       byte[] buf = new byte[2048];
       InputStream stdOutIs = p.getInputStream();
       try {
         InputStream stdErrIs = p.getErrorStream();
         try {
           while (stdOutIs.read(buf) > -1 || 
                  stdErrIs.read(buf) > -1) {
             // consume
           }
         } finally {
           stdErrIs.close();
         }
       } finally {
         stdOutIs.close();
       }
       
       if (p.waitFor() != 0) {
        throw new IllegalStateException("non-zero exit code for command: " + command[2]);
       }
     }
   }
 }
