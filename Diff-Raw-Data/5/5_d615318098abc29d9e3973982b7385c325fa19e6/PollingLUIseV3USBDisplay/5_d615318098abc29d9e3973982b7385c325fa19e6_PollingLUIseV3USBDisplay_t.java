 package swudi.LUIseV3;
 
 import com.ftdi.FTD2XXException;
 import com.ftdi.FTDevice;
 import swudi.device.USBDisplayExceptionHandler;
 
 import java.util.TimerTask;
 import java.util.regex.Pattern;
 
 /**
  * This is needed, because i cannot get the AutoTouch working on windows -> read did block write
  * Created: 26.01.12   by: Armin Haaf
  *
  * @author Armin Haaf
  */
 public class PollingLUIseV3USBDisplay extends AbstractLUIseV3USBDisplay {
     private static final byte[] TOUCH_COMMAND = "Touch?;".getBytes(US_ASCII);
     private static Pattern TOUCH_PATTERN = Pattern.compile("Touch: ([0-9]),([0-9]+),([0-9]+)");
 
 
     private int mouseRefreshRate = 50;
 
     // use a timer outside eventdispatching to get the touch info
     // -> howver mouse events must be send inside the eventdispatching thread
     private java.util.TimerTask mouseRefreshTimer;
 
     // TODO use executor
     static java.util.Timer TIMER = new java.util.Timer("SWUDI_MOUSE_REFRESH_TIMER");
 
     public PollingLUIseV3USBDisplay(final FTDevice pFTDevice) throws FTD2XXException {
         this(pFTDevice, null);
     }
 
     public PollingLUIseV3USBDisplay(final FTDevice pFTDevice, final USBDisplayExceptionHandler<FTD2XXException> pExceptionHandler) throws FTD2XXException {
         super(pFTDevice, pExceptionHandler);
         sendCommand("autosendtouch (0);");
 
         startMouseRefreshTimer();
     }
 
     public int getMouseRefreshRate() {
         return mouseRefreshRate;
     }
 
     public void setMouseRefreshRate(final int pMouseRefreshRate) {
         mouseRefreshRate = pMouseRefreshRate;
         if (mouseRefreshTimer != null) {
             startMouseRefreshTimer();
         }
     }
 
     private void startMouseRefreshTimer() {
         if (mouseRefreshTimer != null) {
             mouseRefreshTimer.cancel();
         }
         mouseRefreshTimer = new TimerTask() {
             @Override
             public void run() {
                 pollTouch();
             }
         };
         if (mouseRefreshRate < 1) {
             mouseRefreshRate = 50;
         }
         TIMER.schedule(mouseRefreshTimer, 1000 / mouseRefreshRate, 1000 / mouseRefreshRate);
     }
 
 
     protected synchronized String readReply() {
         try {
             StringBuilder tResult = new StringBuilder();
             int tReadByte;
             int tReceivedCount = 0;
             // todo read into a buffer would be good, however, we do not get a result, because ftdevice blocks (maybe it waits for the buffer to fill
 
             while ((tReadByte = ftDevice.read()) >= 0) {
                 tReceivedCount++;
                 if ((char) tReadByte == ';') {
                     break;
                 }
                 tResult.append((char) tReadByte);
             }
             transferStatistics.addBytesReceived(tReceivedCount);
             return tResult.length() > 0 ? tResult.toString() : null;
         } catch (FTD2XXException ex) {
             handleException(ex);
             return null;
         }
     }
 
     @Override
     protected Pattern getTouchReplyPattern() {
         return TOUCH_PATTERN;
     }
 
     @Override
     public void setState(final State pState) {
        super.setState(pState);

         if (getState() == State.OFF && mouseRefreshTimer != null) {
             mouseRefreshTimer.cancel();
             mouseRefreshTimer = null;
         }
 
         if (getState() != State.OFF && mouseRefreshTimer == null) {
             startMouseRefreshTimer();
         }
     }
 
     private void pollTouch() {
         try {
             if (getState() != State.OFF) {
                 String tReply;
                 synchronized (this) {
                     sendData(TOUCH_COMMAND);
                     tReply = readReply();
                 }
                 handleTouchEvent(tReply);
             }
         } catch (Exception ex) {
             ex.printStackTrace();
         }
     }
 }
