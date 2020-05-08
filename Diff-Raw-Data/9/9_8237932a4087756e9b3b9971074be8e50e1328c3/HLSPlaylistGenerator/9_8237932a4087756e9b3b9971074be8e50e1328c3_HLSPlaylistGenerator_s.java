 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package repsaj.airstreamer.server.streaming;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import org.apache.commons.io.FileUtils;
 import org.apache.log4j.Logger;
 
 /**
  *
  * @author jasper
  */
 public class HLSPlaylistGenerator implements Runnable, JobAttachment {
 
     private static final Logger LOGGER = Logger.getLogger(HLSPlaylistGenerator.class);
    private static final String NEW_LINE = "\n";
     private final Thread directoryWatcher = new Thread(this);
     private boolean keepRunning = false;
     private final List<String> files = new ArrayList<String>();
     private File directory;
     private File playlistFile;
     private int segmentTime;
     private boolean doMonitor = true;
     private boolean firstTime = true;
     private int monitorInterval = 1000;
 
     @Override
     public void start(String path, int segmentTime) {
         this.segmentTime = segmentTime;
         this.directory = new File(path);
         if (!directory.isDirectory()) {
             throw new IllegalArgumentException("path must be a directory");
         }
         playlistFile = new File(path + "/index.m3u8");
 
         LOGGER.info("started PlayListGenerator in directory " + directory.getPath());
         generatePlayList(false);
 
         if (doMonitor) {
             keepRunning = true;
             directoryWatcher.setName(playlistFile.getName());
             directoryWatcher.start();
         }
     }
 
     @Override
     public void finish() {
         if (doMonitor) {
             keepRunning = false;
             directoryWatcher.interrupt();
         }
 
         checkFiles();
         //generate final playlistfile
         generatePlayList(true);
 
         LOGGER.info("finished PlayListGenerator in directory " + directory.getPath());
     }
 
     @Override
     public void update() {
         if (checkFiles()) {
             LOGGER.info("file list changed, generating new playlist file");
             generatePlayList(false);
         }
     }
 
     @Override
     public void setMonitorPath(boolean doMonitor) {
         this.doMonitor = doMonitor;
     }
 
     @Override
     public void addAttachment(JobAttachment attachment) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     @Override
     public void run() {
 
         while (keepRunning) {
 
             try {
                 Thread.sleep(monitorInterval);
 
                 if (checkFiles()) {
                     LOGGER.info("file list changed, generating new playlist file");
                     generatePlayList(false);
                 }
 
                 monitorInterval = 2000;
 
             } catch (InterruptedException iex) {
                 //ignore
             }
         }
     }
 
     private boolean checkFiles() {
         //Get all the files from the directory
         boolean fileListChanged = false;
         Collection<File> filesInDir = FileUtils.listFiles(directory, new String[]{"ts", "vvt"}, false);
         synchronized (files) {
             for (File file : filesInDir) {
                 if (!files.contains(file.getName())) {
                     files.add(file.getName());
                     fileListChanged = true;
                 }
             }
         }
         //Sort the files
         synchronized (files) {
             Collections.sort(files);
         }
         return fileListChanged;
 
 
     }
 
     private void generatePlayList(boolean isFinal) {
         StringBuilder builder = new StringBuilder();
         builder.append("#EXTM3U").append(NEW_LINE);
         builder.append("#EXT-X-PLAYLIST-TYPE:EVENT").append(NEW_LINE);
         builder.append("#EXT-X-TARGETDURATION:").append(segmentTime).append(NEW_LINE);
         builder.append("#EXT-X-MEDIA-SEQUENCE:0").append(NEW_LINE);
 
         synchronized (files) {
 
             for (String file : files) {
                 builder.append("#EXTINF:").append(segmentTime).append(",").append(NEW_LINE);
                 builder.append(file).append(NEW_LINE);
 
                 //Only generate 1 item
                 if (firstTime) {
                     firstTime = false;
                     break;
                 }
             }
         }
 
         if (isFinal) {
             builder.append("#EXT-X-ENDLIST").append(NEW_LINE);
         }
 
         try {
             FileUtils.writeStringToFile(playlistFile, builder.toString());
         } catch (IOException ex) {
             LOGGER.error("error writing playlistfile", ex);
         }
 
     }
 }
