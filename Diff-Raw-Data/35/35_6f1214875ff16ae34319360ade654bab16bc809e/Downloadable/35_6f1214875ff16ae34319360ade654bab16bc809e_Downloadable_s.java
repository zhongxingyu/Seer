 package ecologylab.io;
 
 import java.io.IOException;
 
 /**
  * Objects that implement this interface can be passed to a {@link DownloadProcessor DownloadProcessor}.
  *
  * @author andruid
  */
 public interface Downloadable
 {
 /**
  * Called to start download.
  */
    public void performDownload()
       throws IOException;
 /**
  * Called to inquire on the status of a download.<br>
  * @return	true if the download is complete.
  */
    public boolean isDownloadDone();
 /**
  * Called in case a timeout happens.
  * 
  * @return	true, if the object is able to abort the download and
  * release resources.
  * @return	false if its stuck.
  */
    public boolean handleTimeout();
 /**
  * Called in case an IO error happens.
  */
    public void handleIoError();
    
    /**
     * Call to notify the object that its download is completed;
     *
     */
    public void downloadAndParseDone();
    
    /**
     * True if the Downloadable has been recycled, and thus should not be downloaded.
     * 
     * @return
     */
    public boolean isRecycled();
    
    /**
     * Check to find out if this should not be downloaded, even though it was queued, because conditions have changed
     * since then.
     * 
     * @return	true if this should no longer be downloaded, because conditions have changed since it was queued.
     */
    public boolean cancel();
    
    public BasicSite getSite();
    
    /**
     * 
     * @return	What to tell the user about what is being downloaded.
     */
    public String message();
 }
