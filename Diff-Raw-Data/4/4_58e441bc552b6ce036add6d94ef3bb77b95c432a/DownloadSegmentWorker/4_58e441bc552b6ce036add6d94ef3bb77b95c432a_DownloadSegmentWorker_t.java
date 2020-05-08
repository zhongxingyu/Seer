 package net.madz.download.engine;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.util.concurrent.locks.Condition;
 import java.util.concurrent.locks.Lock;
 
 import net.madz.download.LogUtils;
 import net.madz.download.service.metadata.DownloadTask;
 import net.madz.download.service.metadata.MetaManager;
 import net.madz.download.service.metadata.Segment;
 
 public final class DownloadSegmentWorker implements Runnable {
 
     private final DownloadTask task;
     private final Segment segment;
     private volatile boolean pauseFlag;
     private IDownloadProcess process;
     private File dataFile;
     private File metadataFile;
 
     public DownloadSegmentWorker(IDownloadProcess process, DownloadTask task, Segment segment, File dataFile, File metadataFile) {
         this.process = process;
         this.task = task;
         this.segment = segment;
         this.dataFile = dataFile;
         this.metadataFile = metadataFile;
     }
 
     @Override
     public void run() {
         URL url = task.getUrl();
         HttpURLConnection openConnection = null;
         InputStream inputStream = null;
         RandomAccessFile randomAccessDataFile = null;
         byte[] buf = new byte[8096];
         int size = 0;
         try {
             openConnection = (HttpURLConnection) url.openConnection();
             openConnection.setRequestProperty("RANGE", "bytes=" + segment.getStartBytes() + "-" + segment.getEndBytes());
             openConnection.connect();
             inputStream = openConnection.getInputStream();
             randomAccessDataFile = new RandomAccessFile(dataFile, "rw");
             long off = segment.getStartBytes();
             while ( ( !isPauseFlag() ) && ( size = inputStream.read(buf) ) != -1 ) {
                 randomAccessDataFile.seek(off);
                 randomAccessDataFile.write(buf, 0, size);
                 synchronized (process) {
                     if ( !isPauseFlag() ) {
                         process.receive(size);
                     }
                 }
                 off += size;
                 MetaManager.updateSegmentDownloadProgress(metadataFile, segment.getId(), off);
             }
         } catch (IOException ignored) {
             LogUtils.error(DownloadSegmentWorker.class, ignored);
         } finally {
             openConnection.disconnect();
             try {
                 if ( null != inputStream) {
                     inputStream.close();
                 }
                if ( null != randomAccessDataFile ) {
                    randomAccessDataFile.close();
                }
             } catch (IOException ignored) {
                 LogUtils.error(DownloadSegmentWorker.class, ignored);
             }
         }
     }
 
     public synchronized boolean isPauseFlag() {
         return pauseFlag;
     }
 
     public synchronized void setPauseFlag(boolean pauseFlag) {
         this.pauseFlag = pauseFlag;
     }
 }
