 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 package de.cismet.tools.gui.downloadmanager;
 
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 /**
 * A ByteArrayDownload writes a given byte array to the file system. Using the DownloadManager this class can be used
 * to create the impression of a real download.
  *
  * @author   jweintraut
  * @version  $Revision$, $Date$
  */
 public class ByteArrayDownload extends AbstractDownload {
 
     //~ Instance fields --------------------------------------------------------
 
     private final transient byte[] content;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new ByteArrayDownload object.
      *
      * @param  content    The byte[] to save.
      * @param  title      The title of the download.
      * @param  directory  The directory of the download.
      * @param  filename   The name of the file to be created.
      * @param  extension  The extension of the file to be created.
      */
     public ByteArrayDownload(final byte[] content,
             final String title,
             final String directory,
             final String filename,
             final String extension) {
         this.content = content;
         this.title = title;
         this.directory = directory;
 
         if (DownloadManager.instance().isEnabled()) {
             determineDestinationFile(filename, extension);
             status = State.WAITING;
         } else {
             status = State.COMPLETED_WITH_ERROR;
             caughtException = new Exception("DownloadManager is disabled. Cancelling download.");
         }
     }
 
     //~ Methods ----------------------------------------------------------------
 
     @Override
     public void run() {
         if (status != State.WAITING) {
             return;
         }
 
         status = State.RUNNING;
         stateChanged();
 
         if ((content == null) || (content.length <= 0)) {
             log.info("Downloaded content seems to be empty..");
 
             if (status == State.RUNNING) {
                 status = State.COMPLETED;
                 stateChanged();
             }
 
             return;
         }
 
         FileOutputStream out = null;
         try {
             out = new FileOutputStream(fileToSaveTo);
             out.write(content);
         } catch (final IOException ex) {
             log.warn("Couldn't write downloaded content to file '" + fileToSaveTo + "'.", ex);
             error(ex);
             return;
         } finally {
             if (out != null) {
                 try {
                     out.close();
                 } catch (Exception e) {
                 }
             }
         }
 
         if (status == State.RUNNING) {
             status = State.COMPLETED;
             stateChanged();
         }
     }
 }
