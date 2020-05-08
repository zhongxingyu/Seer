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
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.threadly.concurrent.SubmitterSchedulerInterface;
 import org.threadly.util.Clock;
 import org.threadly.util.ExceptionUtils;
 
 public class LibavConverter implements ConverterInterface {
   private static final File libavExecutable;
   
   static {
     File executable = new File("/usr/bin/avconv");
     if (executable.exists() && executable.canExecute()) {
       libavExecutable = executable;
     } else {
       executable = new File("/usr/local/bin/avconv");
       if (executable.exists() && executable.canExecute()) {
         libavExecutable = executable;
       } else {
         libavExecutable = null; // could not find it
       }
     }
   }
   
   private static final boolean VERBOSE = true;
   private static final String AVCONV_ENCODE_GLOBAL_FLAGS = "-threads 2";
   private static final String AVCONV_ENCODE_ALL_FLAGS = AVCONV_ENCODE_GLOBAL_FLAGS + " -vcodec libx264 -acodec ac3 -ab 160k";
   private static final String AVCONV_ENCODE_VIDEO_FLAGS = AVCONV_ENCODE_GLOBAL_FLAGS + " -vcodec libx264 -acodec copy";
   private static final String AVCONV_ENCODE_AUDIO_FLAGS = AVCONV_ENCODE_GLOBAL_FLAGS + " -vcodec copy -acodec ac3 -ab 160k";
   private static final String AVCONV_COPY_FLAGS = AVCONV_ENCODE_GLOBAL_FLAGS + " -vcodec copy -acodec copy";
   private static final String DESIRED_EXTENSION = ".mp4";
   
   public static File getAvconvExecutable() {
     return libavExecutable;
   }
   
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
       
       try {
         String fileInfo = getFileInfo(sourceFile);
         boolean desiredVideoCodec = isDesiredVideoCodec(fileInfo);
         boolean desiredAudioCodec = isDesiredAudioCodec(fileInfo);
         if (desiredVideoCodec && desiredAudioCodec) {
           String extension = FileUtils.getExtension(sourceFile.getName());
           if (extension.equalsIgnoreCase(DESIRED_EXTENSION)) {
             // copy the file
             if (VERBOSE) {
               System.out.println("Copying file to: " + newFile.getAbsolutePath());
             }
             
             FileUtils.copyFile(sourceFile, newFile);
           } else {
             if (VERBOSE) {
               System.out.println("Copying codec data for " + extension + " file to: " + newFile.getAbsolutePath());
             }
             
             encodeFile(sourceFile, newFile, 
                        AVCONV_COPY_FLAGS);
           }
         } else if (desiredVideoCodec) {
           if (VERBOSE) {
             System.out.println("Encoding audio from " + sourceFile + " to: " + newFile.getAbsolutePath());
           }
           
           encodeFile(sourceFile, newFile, 
                      AVCONV_ENCODE_AUDIO_FLAGS);
         } else if (desiredAudioCodec) {
           if (VERBOSE) {
             System.out.println("Encoding video from " + sourceFile + " to: " + newFile.getAbsolutePath());
           }
           
           encodeFile(sourceFile, newFile, 
                      AVCONV_ENCODE_VIDEO_FLAGS);
         } else {
           if (VERBOSE) {
             System.out.println("Encoding " + sourceFile + " to: " + newFile.getAbsolutePath());
           }
           
           encodeFile(sourceFile, newFile, 
                      AVCONV_ENCODE_ALL_FLAGS);
         }
       } catch (IOException e) {
         throw ExceptionUtils.makeRuntime(e);
       } catch (InterruptedException e) {
         ExceptionUtils.handleException(e);
         return;
       }
 
       int count = processedCount.incrementAndGet();
          
       if (VERBOSE) {
         String percent = Double.toString(((count / (double)totalProcessCount)) * 100);
         System.out.println("Estimated % done: " + 
                              percent.substring(0, Math.min(percent.length() - 1, 5)) + "%" + 
                              " - ( " + count + " out of " + totalProcessCount + " )\n");
       }
     }
     
     public static boolean isDesiredVideoCodec(String info) {
       Matcher m = Pattern.compile("Video: h264").matcher(info);
       
       return m.find();
     }
     
     public static boolean isDesiredAudioCodec(String info) {
       Matcher m = Pattern.compile("Audio: ac3").matcher(info);
       
       return m.find();
     }
     
     private static String getFileInfo(File sourceFile) throws IOException {
       String command[] = {"bash", 
                           "-c", 
                           libavExecutable.getAbsolutePath() + " -i '" + 
                               sourceFile.getAbsolutePath() + "\' 2>&1"
                          };
       
       Process p = Runtime.getRuntime().exec(command);
       StringBuilder sb = new StringBuilder();
 
       InputStream stdOutIs = p.getInputStream();
       try {
         byte[] buf = new byte[2048];
         int c;
         while ((c = stdOutIs.read(buf)) > -1) {
           sb.append(new String(buf, 0, c));
         }
       } finally {
         stdOutIs.close();
       }
       
       return sb.toString();
     }
     
     private static void encodeFile(File sourceFile, File destFile, 
                                    String flags) throws IOException, 
                                                         InterruptedException {
      String command[] = {"bash", 
                          "-c", 
                          libavExecutable.getAbsolutePath() + " -i '" + sourceFile.getAbsolutePath() + '\'' + 
                            " "  + flags + " '" + destFile.getAbsolutePath() + '\''
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
