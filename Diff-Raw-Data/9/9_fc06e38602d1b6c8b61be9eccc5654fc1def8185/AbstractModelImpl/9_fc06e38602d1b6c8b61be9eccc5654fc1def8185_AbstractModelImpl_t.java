 /*
  * Created On: Sep 16, 2006 1:54:18 PM
  */
 package com.thinkparity.codebase.model;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 
 import com.thinkparity.codebase.ErrorHelper;
 import com.thinkparity.codebase.log4j.Log4JWrapper;
 
 import com.thinkparity.codebase.model.stream.StreamMonitor;
 import com.thinkparity.codebase.model.stream.StreamReader;
 import com.thinkparity.codebase.model.stream.StreamSession;
 
 /**
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 public abstract class AbstractModelImpl {
 
     /** An apache logger wrapper. */
     protected final Log4JWrapper logger;
 
     private Context context;
 
         /**
          * Create AbstractModelImpl.
          * 
          * 
          */
         protected AbstractModelImpl() {
             super();
             this.logger = new Log4JWrapper(getClass());
         }
 
     /**
      * Set context.
      *
      * @param context The Context.
      */
     public final void setContext(final Context context) {
         this.context = context;
     }
 
     /**
      * Download a stream from the stream server. In order to complete the
      * download, the stream session must already be established; the stream id
      * must be known and the stream file must have been created.
      * 
      * @param downloadMonitor
      *            A <code>DownloadMonitor</code>. Used to notify a caller
      *            about specific download events.
      * @param monitor
      *            A <code>StreamMonitor</code>. Used to notify a caller about
      *            all stream events.
      * @param session
      *            A <code>StreamSession</code>.
      * @param streamId
      *            A stream id <code>String</code>.
      * @param streamFile
      *            A stream file <code>File</code>. The downloaded stream is
      *            written to this file.
      * @param streamOffset
      *            An offset within the remote stream at which to begin the
      *            download.
      * @throws IOException
      */
     protected final void downloadStream(final DownloadMonitor downloadMonitor,
             final StreamMonitor monitor, final StreamSession session,
             final String id, final File file, final Long offset)
             throws IOException {
         final Long actualOffset;
         if (file.length() < offset) {
             logger.logWarning("Cannot resume download for {0} at {1}.  Starting over.",
                     id, offset);
             actualOffset = 0L;
         } else {
             actualOffset = offset;
         }
         final FileOutputStream stream;
         if (0 == actualOffset) {
             stream = new FileOutputStream(file);
             logger.logInfo("Starting download for {0}.", id);
         } else {
             stream = new FileOutputStream(file, true);
             logger.logInfo("Resuming download for {0} at {1}.", id, actualOffset);
         }
         final StreamReader reader = new StreamReader(monitor, session);
         try {
             reader.open();
             reader.read(id, stream, actualOffset);
         } finally {
             try {
                 stream.close();
             } finally {
                 reader.close();
             }
         }
     }
 
     /**
      * Obtain the context
      *
      * @return The Context.
      */
     protected final Context getContext() {
         return context;
     }
 
     /**
      * Panic. Nothing can be done about the error that has been generated. An
      * appropriate error is constructed suitable for throwing beyond the model
      * interface.
      * 
      * @param t
      *            A <code>Throwable</code>.
      * @return A <code>RuntimeException</code>.
      */
     protected RuntimeException panic(final Throwable t) {
         if (ThinkParityException.class.isAssignableFrom(t.getClass())) {
             return (ThinkParityException) t;
        } else {
             final String errorId = new ErrorHelper().getErrorId(t);
             logger.logError(t, "{0}", errorId);
             return new ThinkParityException(errorId.toString(), t);
         }
     }
 
     /**
     * Upload a stream to the stream server. This invocation will take care of
     * common network errors.
     * 
     * @param uploadMonitor
     *            An <code>UploadMoniotor</code>.
     * @param streamId
     *            A stream id <code>String</code>.
     * @param session
     *            A <code>StreamSession</code>.
     * @param stream
     *            An <code>InputStream</code>.
     * @param streamSize
     *            The stream size <code>Long</code>.
     * @throws IOException
     */
    protected final void upload(final UploadMonitor uploadMonitor,
         final String streamId, final StreamSession session,
         final InputStream stream, final Long streamSize) throws IOException {
     new UploadHelper(this, logger).upload(uploadMonitor, streamId, session,
             stream, streamSize);
    }
 }
