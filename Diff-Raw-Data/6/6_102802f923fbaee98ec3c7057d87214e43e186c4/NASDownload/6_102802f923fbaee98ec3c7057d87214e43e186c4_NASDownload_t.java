 /***************************************************
 *
 * cismet GmbH, Saarbruecken, Germany
 *
 *              ... and it just works.
 *
 ****************************************************/
 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.cismet.cids.custom.nas;
 
 import Sirius.navigator.connection.SessionManager;
 import Sirius.navigator.exception.ConnectionException;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import org.openide.util.Cancellable;
 import org.openide.util.Exceptions;
 import org.openide.util.NbBundle;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.TimeoutException;
 
 import de.cismet.cids.custom.utils.nas.NasProductTemplate;
 import de.cismet.cids.custom.wunda_blau.search.actions.NasDataQueryAction;
 
 import de.cismet.cids.server.actions.ServerActionParameter;
 
 import de.cismet.cismap.commons.features.Feature;
 
 import de.cismet.tools.gui.downloadmanager.AbstractDownload;
 
 /**
  * DOCUMENT ME!
  *
  * @author   daniel
  * @version  $Revision$, $Date$
  */
 public class NASDownload extends AbstractDownload implements Cancellable {
 
     //~ Static fields/initializers ---------------------------------------------
 
     private static String SEVER_ACTION = "nasDataQuery";
     private static String EXTENSION = ".xml";
     private static final String BASE_TITLE;
 
     static {
         BASE_TITLE = NbBundle.getMessage(
                 NASDownload.class,
                 "NASDownload.basetitle.text");
     }
 
     //~ Enums ------------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private enum Phase {
 
         //~ Enum constants -----------------------------------------------------
 
         REQEUST_GEN, RETRIEVAL, DOWNLOAD, DONE
     }
 
     //~ Instance fields --------------------------------------------------------
 
     protected String filename = null;
     private Future<ByteArrayWrapper> pollingFuture;
     private NasProductTemplate template;
     private Geometry geometry;
     private String orderId;
     private transient byte[] content;
     private boolean omitSendingRequest = false;
     private String requestId;
 
     //~ Constructors -----------------------------------------------------------
 
     /**
      * Creates a new NASDownload object.
      *
      * @param  orderId  DOCUMENT ME!
      */
     public NASDownload(final String orderId) {
         omitSendingRequest = true;
         this.orderId = orderId;
         template = null;
         geometry = null;
         this.title = BASE_TITLE;
         status = State.WAITING;
         this.directory = "";
         filename = orderId;
         fileToSaveTo = new File("" + System.currentTimeMillis());
         if ((filename != null) && !filename.equals("")) {
             determineDestinationFile(filename, EXTENSION);
         } else {
             determineDestinationFile(requestId, EXTENSION);
         }
     }
 
     /**
      * Creates a new NASDownload object.
      *
      * @param  title      DOCUMENT ME!
      * @param  filename   DOCUMENT ME!
      * @param  directory  DOCUMENT ME!
      * @param  requestId  DOCUMENT ME!
      * @param  template   DOCUMENT ME!
      * @param  g          DOCUMENT ME!
      */
 // public NASDownload(final String title,
 // final String directory,
 // final NasProductTemplate template,
 // final Geometry g) {
 // this.template = template;
 // geometry = g;
 // this.title = title;
 // this.directory = directory;
 // status = State.WAITING;
 // fileToSaveTo = new File("" + System.currentTimeMillis());
 ////        determineDestinationFile(STANDARD_FILE_NAME, EXTENSION);
 //    }
     /**
      * Creates a new NASDownload object.
      *
      * @param  title      DOCUMENT ME!
      * @param  filename   DOCUMENT ME!
      * @param  directory  DOCUMENT ME!
      * @param  requestId  DOCUMENT ME!
      * @param  template   DOCUMENT ME!
      * @param  g          DOCUMENT ME!
      */
     public NASDownload(final String title,
             final String filename,
             final String directory,
             final String requestId,
             final NasProductTemplate template,
             final Geometry g) {
         this.template = template;
         geometry = g;
         this.title = title;
         this.directory = directory;
         this.requestId = requestId;
         status = State.WAITING;
         if ((requestId != null) && !requestId.equals("")) {
             fileToSaveTo = new File("" + requestId);
         } else {
             fileToSaveTo = new File("" + System.currentTimeMillis());
         }
         this.filename = filename;
         if ((filename != null) && !filename.equals("")) {
             determineDestinationFile(filename, EXTENSION);
         } else {
             determineDestinationFile(requestId, EXTENSION);
         }
     }
 
     /**
      * Creates a new NASDownload object.
      */
     private NASDownload() {
         fileToSaveTo = new File("" + System.currentTimeMillis());
     }
 
     //~ Methods ----------------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      */
     private void setAbortedStatus() {
         status = State.ABORTED;
         stateChanged();
     }
 
     @Override
     public boolean cancel() {
         final boolean cancelled = downloadFuture.cancel(true);
         if (pollingFuture != null) {
             pollingFuture.cancel(true);
         }
         if (cancelled) {
             status = State.ABORTED;
             stateChanged();
         }
         return downloadFuture.isCancelled();
     }
 
     @Override
     public void run() {
         if (status != State.WAITING) {
             return;
         }
         /*
          * Phase 1: sending the reqeust to the server
          */
         setTitleForPhase(Phase.REQEUST_GEN);
         status = State.RUNNING;
         stateChanged();
         if (!omitSendingRequest) {
             if (!Thread.interrupted()) {
                 orderId = sendNasRequest();
             } else {
                 doCancellationHandling(false, false);
                 return;
             }
             if (orderId == null) {
                 log.error("nas server request returned no orderId, cannot continue with NAS download");
                 this.status = State.COMPLETED_WITH_ERROR;
                 stateChanged();
                 return;
             }
             if ((filename == null) && (requestId != null)) {
 //                filename = orderId;
                 filename = requestId;
             }
         }
 
         if (!Thread.interrupted()) {
             setTitleForPhase(Phase.RETRIEVAL);
             stateChanged();
         }
 
         /*
          * Phase 2: retrive the result from the cids server
          */
         if (Thread.interrupted()) {
             doCancellationHandling(true, false);
             return;
         }
         final ExecutorService executor = Executors.newSingleThreadExecutor();
         if (!Thread.interrupted()) {
             pollingFuture = executor.submit(new ServerPollingRunnable());
         } else {
             doCancellationHandling(true, false);
             return;
         }
         try {
             if (!Thread.interrupted() && (pollingFuture != null)) {
                 content = pollingFuture.get(1, TimeUnit.HOURS).getByteArray();
             } else {
                 doCancellationHandling(true, true);
                 return;
             }
         } catch (InterruptedException ex) {
             doCancellationHandling(true, true);
             Thread.currentThread().interrupt();
             return;
         } catch (ExecutionException ex) {
             log.warn("could not execute nas download", ex);
             Exceptions.printStackTrace(ex);
         } catch (TimeoutException ex) {
             log.warn("the maximum timeout for nas download is exceeded", ex);
         }
 
         if (!Thread.interrupted()) {
             setTitleForPhase(Phase.DOWNLOAD);
             stateChanged();
         }
 
         if ((content == null) || (content.length <= 0)) {
             log.info("Downloaded content seems to be empty..");
 
             if ((status == State.RUNNING) && !Thread.interrupted()) {
                 status = State.COMPLETED_WITH_ERROR;
                 stateChanged();
             }
             return;
         }
         /*
          * Phase 3: save the result file
          */
         if (Thread.interrupted()) {
             doCancellationHandling(false, false);
             return;
         }
 
         FileOutputStream out = null;
         try {
             if (!Thread.interrupted()) {
                 out = new FileOutputStream(fileToSaveTo);
                 out.write(content);
             } else {
                 doCancellationHandling(false, false);
                 return;
             }
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
 
         if (!Thread.interrupted()) {
             setTitleForPhase(Phase.DONE);
             status = State.COMPLETED;
             stateChanged();
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  cancelServerRequest  DOCUMENT ME!
      * @param  cancelPollingThread  DOCUMENT ME!
      */
     private void doCancellationHandling(final boolean cancelServerRequest, final boolean cancelPollingThread) {
         log.warn("NAS Download was interuppted");
         if (cancelServerRequest) {
             cancelNasRequest();
         }
         if (cancelPollingThread && (pollingFuture != null)) {
             pollingFuture.cancel(true);
         }
 //        setAbortedStatus();
         deleteFile();
     }
 
     /**
      * DOCUMENT ME!
      *
      * @param  p  DOCUMENT ME!
      */
     private void setTitleForPhase(final Phase p) {
         String appendix = "";
         if (p == Phase.REQEUST_GEN) {
             appendix = NbBundle.getMessage(NASDownload.class, "NASDownload.requestGenTitle.text");
         } else if (p == Phase.RETRIEVAL) {
             appendix = NbBundle.getMessage(NASDownload.class, "NASDownload.resultRetrievalTitle.text");
         } else if (p == Phase.DOWNLOAD) {
             appendix = NbBundle.getMessage(NASDownload.class, "NASDownload.downloadTitle.text");
         }
         title = BASE_TITLE;
         if ((appendix != null) && !appendix.equals("")) {
             title += " - " + appendix;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @return  DOCUMENT ME!
      */
     private String sendNasRequest() {
         final ServerActionParameter paramTemplate = new ServerActionParameter(NasDataQueryAction.PARAMETER_TYPE.TEMPLATE
                         .toString(),
                 template);
         final ServerActionParameter paramGeom = new ServerActionParameter(NasDataQueryAction.PARAMETER_TYPE.GEOMETRY
                         .toString(),
                 geometry);
         final ServerActionParameter paramMethod = new ServerActionParameter(NasDataQueryAction.PARAMETER_TYPE.METHOD
                         .toString(),
                 NasDataQueryAction.METHOD_TYPE.ADD);
         final ServerActionParameter paramRequest = new ServerActionParameter(
                 NasDataQueryAction.PARAMETER_TYPE.REQUEST_ID.toString(),
                 requestId);
         try {
             return (String)SessionManager.getProxy()
                         .executeTask(
                                 SEVER_ACTION,
                                 "WUNDA_BLAU",
                                 null,
                                 paramTemplate,
                                 paramGeom,
                                 paramRequest,
                                 paramMethod);
         } catch (Exception ex) {
             log.error("error during enqueuing nas server request", ex);
         }
         return null;
     }
 
     /**
      * DOCUMENT ME!
      */
     private void cancelNasRequest() {
         final ServerActionParameter paramOrderId = new ServerActionParameter(NasDataQueryAction.PARAMETER_TYPE.ORDER_ID
                         .toString(),
                 orderId);
         final ServerActionParameter paramMethod = new ServerActionParameter(NasDataQueryAction.PARAMETER_TYPE.METHOD
                         .toString(),
                 NasDataQueryAction.METHOD_TYPE.CANCEL);
         try {
             SessionManager.getProxy().executeTask(
                 SEVER_ACTION,
                 "WUNDA_BLAU",
                 null,
                 paramOrderId,
                 paramMethod);
         } catch (Exception ex) {
             log.error("error during enqueuing nas server request", ex);
         }
     }
 
     /**
      * DOCUMENT ME!
      */
     private void deleteFile() {
         if (fileToSaveTo.exists() && fileToSaveTo.isFile()) {
             fileToSaveTo.delete();
         }
     }
 
     //~ Inner Classes ----------------------------------------------------------
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class ServerPollingRunnable implements Callable<ByteArrayWrapper> {
 
         //~ Methods ------------------------------------------------------------
 
         @Override
         public ByteArrayWrapper call() {
             final ServerActionParameter paramMethod = new ServerActionParameter(NasDataQueryAction.PARAMETER_TYPE.METHOD
                             .toString(),
                     NasDataQueryAction.METHOD_TYPE.GET);
 
             final ServerActionParameter paramOrderId = new ServerActionParameter(
                     NasDataQueryAction.PARAMETER_TYPE.ORDER_ID.toString(),
                     orderId);
             byte[] result = null;
            while ((result == null) || (result.length == 0)) {
                 if (Thread.interrupted()) {
                     log.info("result fetching thread was interrupted");
                     return null;
                 }
                 try {
                     result = (byte[])SessionManager.getProxy()
                                 .executeTask(
                                         SEVER_ACTION,
                                         "WUNDA_BLAU",
                                         null,
                                         paramOrderId,
                                         paramMethod);
                 } catch (ConnectionException ex) {
                     log.error("error during pulling nas result from server", ex);
                 }
                 if (result == null) {
                    // there went something wrong on server side so abort the download
                    return null;
                } else if (result.length == 0) {
                     try {
                         Thread.sleep(5000);
                     } catch (InterruptedException ex) {
                         log.info("result fetching thread was interrupted", ex);
                         Thread.currentThread().interrupt();
                         return null;
                     }
                 }
             }
             final ByteArrayWrapper baw = new ByteArrayWrapper(result);
             return baw;
         }
     }
 
     /**
      * DOCUMENT ME!
      *
      * @version  $Revision$, $Date$
      */
     private class ByteArrayWrapper {
 
         //~ Instance fields ----------------------------------------------------
 
         private byte[] byteArray;
 
         //~ Constructors -------------------------------------------------------
 
         /**
          * Creates a new ByteArrayWrapper object.
          *
          * @param  ba  DOCUMENT ME!
          */
         private ByteArrayWrapper(final byte[] ba) {
             byteArray = ba;
         }
 
         //~ Methods ------------------------------------------------------------
 
         /**
          * DOCUMENT ME!
          *
          * @return  DOCUMENT ME!
          */
         public byte[] getByteArray() {
             return byteArray;
         }
 
         /**
          * DOCUMENT ME!
          *
          * @param  b  DOCUMENT ME!
          */
         public void setByteArray(final byte[] b) {
             this.byteArray = b;
         }
     }
 }
