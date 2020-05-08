 package no.ebakke.studycaster.screencasting;
 
 import java.awt.AWTException;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import no.ebakke.studycaster.backend.TimeSource;
 import no.ebakke.studycaster.screencasting.CaptureScheduler.CaptureTask;
 import no.ebakke.studycaster.util.stream.NonBlockingOutputStream;
 
 /** Thread-safe. */
 public class ScreenRecorder {
   private static final Logger LOG = Logger.getLogger("no.ebakke.studycaster");
   private final NonBlockingOutputStream nbos;
   private final CaptureEncoder enc;
   private final AtomicBoolean stopped = new AtomicBoolean(true);
   private final ScreenRecorderConfiguration config;
   private volatile CaptureScheduler pointerRecorder, frameRecorder;
 
   private final CaptureTask pointerRecorderTask = new CaptureTask() {
     public void capture() {
       enc.capturePointer();
     }
 
     public double getMaxFrequency() {
       return config.getMaxPointerSamplingFrequency();
     }
 
     public double getMaxDutyCycle() {
       return config.getMaxPointerDutyCycle();
     }
 
     @Override
     public String toString() {
       return "pointerRecorderTask";
     }
   };
 
   private final CaptureTask frameRecorderTask = new CaptureTask() {
     public void capture() throws IOException {
       enc.captureFrame();
     }
 
     public double getMaxFrequency() {
       double ret = config.getMaxFrameSamplingFrequency();
       if (nbos == null) {
         return ret;
       } else {
         double fillLevel = ((double) (nbos.getBytesPosted() - nbos.getBytesWritten())) /
                            ((double) nbos.getBufferLimitBytes());
         // TODO: Find a less ad-hoc way of doing this.
         if (fillLevel > 0.9) {
           return 0.0;
         } else if (fillLevel > 0.75) {
           return ret * 0.1;
         } else if (fillLevel > 0.50) {
           return ret * 0.2;
         } else {
           return ret;
         }
       }
     }
 
     public double getMaxDutyCycle() {
       return config.getMaxFrameDutyCycle();
     }
 
     @Override
     public String toString() {
       return "frameRecorderTask";
     }
   };
 
   public ScreenRecorder(OutputStream out, TimeSource timeSource,
       ScreenRecorderConfiguration config) throws IOException, AWTException
   {
     this.config = config;
     // TODO: Get rid of this abstraction violation.
     nbos = (out instanceof NonBlockingOutputStream) ? ((NonBlockingOutputStream) out) : null;
     Rectangle screenRect = new Rectangle(Toolkit.getDefaultToolkit().getScreenSize());
     enc = new CaptureEncoder(out, screenRect);
     enc.setTimeSource(timeSource);
   }
 
  public synchronized void setCensor(ScreenCensor censor) {
     enc.setCensor(censor);
   }
 
   public synchronized void start() {
     if (!stopped.getAndSet(false))
       return;
     LOG.info("Starting screen recorder");
     pointerRecorder = new CaptureScheduler(pointerRecorderTask);
     pointerRecorder.start();
     frameRecorder   = new CaptureScheduler(frameRecorderTask);
     frameRecorder.start();
   }
 
  public void stop() throws IOException {
     if (stopped.getAndSet(true))
       return;
     LOG.info("Stopping screen recorder");
     try {
       pointerRecorder.close();
     } catch (IOException e) {
       LOG.log(Level.WARNING, "Unexpected error while closing pointerRecorder", e);
     } finally {
       pointerRecorder = null;
     }
     try {
       frameRecorder.close();
     } finally {
       frameRecorder = null;
     }
   }
 
   /** Closes the underlying OutputStream as well. */
  public void close() throws IOException {
     LOG.info("Closing screen recorder");
     stop();
     enc.close();
   }
 }
