 package polly.core.update;
 
 import java.io.Closeable;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.math.BigInteger;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 
 import de.skuzzle.polly.sdk.AbstractDisposable;
 import de.skuzzle.polly.sdk.exceptions.DisposingException;
 
 
 /**
  * 
  * @author Simon
  * @version 27.07.2011 ae73250
  */
 public class DownloadManager extends AbstractDisposable {
     
     public static void main(String[] args) throws MalformedURLException {
         DownloadManager dm = new DownloadManager();
         
         URL url = new URL("http://www.bullshit.skuzzle.de/KIZ.rar");
         File dest = new File("C:\\Users\\Simon\\Desktop\\kiz.rar");
         
         dm.downloadLater(url, dest, new DownloadCallback() {
             
             @Override
             public void downloadFinished(DownloadObject o) {
                 System.out.println("Fertig: " + o.toString());
             }
             
             
             
             @Override
             public void downloadFailed(DownloadObject o, Exception e) {
                 System.out.println("Failed: " + o);
                 System.out.println("Reson: " + e);
             }
         });
         
     }
     
     
     public final static int PAKET_SIZE = 1024 * 64; // 64kb    
     
     public class DownloadObject implements Runnable {
     
         private URL url;
         private File destination;
         private int totalBytes;
         private long start;
         private long end;
         private String md5Hash;
         private DownloadCallback callback;
         
         
         public DownloadObject(URL url, File destination, DownloadCallback callback) {
             this.url = url;
             this.destination = destination;
             this.callback = callback;
             this.md5Hash = "";
         }
         
         
         
         public URL getURL() {
             return this.url;
         }
         
         
         public File getDestination() {
             return this.destination;
         }
         
         
         
         public int getTotalBytes() {
             return this.totalBytes;
         }
         
         
         
         public int getAverageSpeed() {
             return this.getTotalBytes() / this.getTotalTime();
         }
         
         
         
         public int getTotalTime() {
             return (int) (this.end - this.start);
         }
         
         
         
         public String getMd5Hash() {
             return this.md5Hash;
         }
         
         
         
         private int tryGetFileSize() {
             HttpURLConnection conn = null;
             try {
                 conn = (HttpURLConnection) this.url.openConnection();
                 conn.setRequestMethod("HEAD");
                 conn.getInputStream();
                 return conn.getContentLength();
             } catch (IOException e) {
                 return -1;
             } finally {
                conn.disconnect();
             }
         }
         
         
                
         
         
         @Override
         public void run() {
             DigestInputStream in = null;
             OutputStream out = null;
             boolean success = false;
             Exception failReason = null;
             
             try {
                 int size = this.tryGetFileSize();
                 in = new DigestInputStream(this.url.openStream(), 
                         MessageDigest.getInstance("MD5"));
                 out = new FileOutputStream(this.destination);
 
                 this.start = System.currentTimeMillis();
                 byte[] buffer = new byte[PAKET_SIZE];
                 int bytes = in.read(buffer);
                 this.totalBytes += bytes;
                 
                 System.out.println("Progress");
                 
                 int progressOld = 0;
                 int progress = 0;
                 while (bytes != -1) {
                     out.write(buffer, 0, bytes);
                     out.flush();
                     bytes = in.read(buffer);
                     this.totalBytes += bytes;
                     if (size != -1) {
                         progress = (int)((float)this.totalBytes / (float)size * 100f);
                         if (progress != progressOld) {
                             progressOld = progress;
                             //logger.trace("Progress: " + formatter.format(progress) + "%");
                             System.out.print("=");
                             // ISSUE: 0000046
                             // HACK:  do not print 100% multiple times
                         }
                         
                     }
                 }
                 System.out.println(" 100%");
                 this.md5Hash = new BigInteger(
                             1, in.getMessageDigest().digest()).toString(16);
                 this.end = System.currentTimeMillis();
                 success = true;
             } catch (Exception e) {
                 if (this.destination.exists()) {
                     this.destination.delete();
                 }
                 failReason = e;
             } finally {
                 this.closeIgnore(in);
                 this.closeIgnore(out);
             }
             
             if (success) {
                 this.callback.downloadFinished(this);
             } else {
                 this.callback.downloadFailed(this, failReason);
             }
         }
         
         
         
         private void closeIgnore(Closeable s) {
             if (s != null) {
                 try {
                     s.close();
                 } catch (Exception ignore) {}
             }
         }
         
         
         @Override
         public String toString() {
             return "Source: " + this.url.toString() + ", Destination: " + this.destination
                 + ", total bytes: " + this.getTotalBytes() + ", total time: " +
                 (this.getTotalTime() / 1000) + "sec, MD5: " + this.md5Hash;
         }
     }
     
     
     
     public static interface DownloadCallback {
         public abstract void downloadFinished(DownloadObject o);
         public abstract void downloadFailed(DownloadObject o, Exception e);
     }
     
     
     private ExecutorService downloadPool;
     
     
     
     public DownloadManager() {
         this.downloadPool = Executors.newFixedThreadPool(3);
     }
     
     
     
     public void downloadLater(URL url, File destination, DownloadCallback callback) {
         this.downloadPool.execute(new DownloadObject(url, destination, callback));
     }
     
     
     
     public void downloadAndWait(URL url, File destination, DownloadCallback callback) {
         DownloadObject d = new DownloadObject(url, destination, callback);
         d.run();
     }
     
 
 
     @Override
     protected void actualDispose() throws DisposingException {
         this.downloadPool.shutdown();
     }
 }
