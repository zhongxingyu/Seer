 /*
  * Copyright 2011 mercatis technologies AG
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package swudi.LUIseV3;
 
 import com.ftdi.FTD2XXException;
 import com.ftdi.FTDevice;
 import swudi.device.TransferStatistics;
 import swudi.device.USBDisplay;
 import swudi.device.USBDisplayExceptionHandler;
 
 import java.awt.Point;
 import java.awt.image.BufferedImage;
 import java.awt.image.DataBufferByte;
 import java.nio.charset.Charset;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * Created: 02.12.11   by: Armin Haaf
  * <p/>
  * <p/>
  * <p/>
  * a implementation of usb display of the <A href="http://www.wallbraun-electronics.de/produkte/lcdusbinterfacev301/index.html">LIUseV3</A>
  *
  * @author Armin Haaf
  */
 public class LUIseV3USBDisplay implements USBDisplay {
 
     // some often used commands
     private static final Charset US_ASCII = Charset.forName("US-ASCII");
     private static final byte[] TOUCH_COMMAND = "Touch?;".getBytes(US_ASCII);
     private static final byte[] BITMAP_COMMAND_PREFIX = "Bitmap (0,".getBytes(US_ASCII);
     private static final byte[] BITMAP_COMMAND_POSTFIX = ");".getBytes(US_ASCII);
     private static Pattern TOUCH_PATTERN = Pattern.compile("Touch: ([0-9]),([0-9]+),([0-9]+)");
 
     private FTDevice ftDevice;
 
     private final TransferStatistics transferStatistics = new TransferStatistics();
 
     /**
      * connection started
      */
     private long connectStartedMillis;
 
     private USBDisplayExceptionHandler<FTD2XXException> exceptionHandler;
 
     private boolean inverted = false;
 
     private boolean paused = false;
 
     private final byte[] bitmapData = new byte[11000];
 
     private int restoreBacklightValue = 100;
 
     @Override
     public BufferedImage createOffScreenBuffer() {
         return new BufferedImage(320, 240, BufferedImage.TYPE_BYTE_BINARY);
     }
 
     public LUIseV3USBDisplay(final FTDevice pFTDevice) throws FTD2XXException {
         this(pFTDevice, null);
     }
 
     public LUIseV3USBDisplay(final FTDevice pFTDevice, final USBDisplayExceptionHandler<FTD2XXException> pExceptionHandler) throws FTD2XXException {
         ftDevice = pFTDevice;
         init();
 
         exceptionHandler = pExceptionHandler;
     }
 
     public USBDisplayExceptionHandler getExceptionHandler() {
         return exceptionHandler;
     }
 
     public void setExceptionHandler(final USBDisplayExceptionHandler pExceptionHandler) {
         exceptionHandler = pExceptionHandler;
     }
 
     private byte[] getBytes(String pString) {
         return pString.getBytes(US_ASCII);
     }
 
     private void init() throws FTD2XXException {
         connectStartedMillis = System.currentTimeMillis();
 
         ftDevice.open();
         ftDevice.purgeBuffer(true, true);
 
         // typically we transfer one frame in one transaction and receive one touch in a transaction
         ftDevice.setUSBParameters(256, 10240);
 
         setScreenConfig(0, 2, 0);
     }
 
     public void clearScreen() {
         sendCommand("BitmapClear (0);BitmapClear (1);TextClear;");
     }
 
     public void setDisplayOn(final boolean pDisplayOn) {
         sendCommand("DisplayOn (" + (pDisplayOn ? "1" : "0") + ");");
     }
 
     public void setContrast(int pContrast) {
         sendCommand("Contrast (" + pContrast + ");");
         sendCommand("StoreContrast;");
     }
 
     public int getContrast() {
         sendCommand("Contrast?;");
         return Integer.parseInt(readReply().substring("Contrast ".length() + 1));
     }
 
     public void setInverted(final boolean pInverted) {
         inverted = pInverted;
         setScreenConfig(0, 2, inverted ? 1 : 0);
     }
 
     @Override
     public boolean isInverted() {
         return inverted;
     }
 
     public void setScreenConfig(int GfxMode, int TextMode, int Invert) {
         sendCommand(String.format("ScreenConfig (%d,%d,%d);", GfxMode, TextMode, Invert));
     }
 
     public void sendCommand(String pCommand) {
         sendData(getBytes(pCommand));
     }
 
     public void sendData(final byte pData[], final int pOffset, final int pLength) {
         try {
             transferStatistics.addBytesSent(pLength);
             ftDevice.write(pData, pOffset, pLength);
         } catch (FTD2XXException ex) {
             handleException(ex);
 
         }
     }
 
     private void handleException(final FTD2XXException pEx) {
         try {
             if (exceptionHandler != null) {
                 exceptionHandler.handleException(pEx);
             } else {
                 pEx.printStackTrace();
             }
         } catch (Exception ex) {
             // we ignore exceptions from the exception handler...
             ex.printStackTrace();
         }
     }
 
     public void sendData(byte[] pData) {
         sendData(pData, 0, pData.length);
     }
 
     public void calibrateTouch() {
         sendCommand("TouchCalib;");
     }
 
     public Point getTouch() {
         if (isPaused()) {
             return null;
         }
 
         sendData(TOUCH_COMMAND);
 
         String tResult = readReply();
         // fast check if we need to parse
         if (tResult.startsWith("Touch: 1")) {
             Matcher tMatcher = TOUCH_PATTERN.matcher(tResult);
             tMatcher.find();
             return new Point(Integer.parseInt(tMatcher.group(2)),
                     Integer.parseInt(tMatcher.group(3)));
         } else {
             return null;
         }
     }
 
     @Override
     public boolean isPaused() {
         return paused;
     }
 
     @Override
     public void setPaused(final boolean pPaused) {
         paused = pPaused;
 
         if (paused) {
             restoreBacklightValue = getBacklight();
             setBacklightWithoutStore(0);
            sendCommand("DisplayOn (0);");
         } else {
             setBacklightWithoutStore(restoreBacklightValue);
            sendCommand("DisplayOn (1);");
         }
     }
 
     private String readReply() {
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
             return tResult.toString();
         } catch (FTD2XXException ex) {
             handleException(ex);
             return null;
         }
     }
 
     /**
      * this is a optimization. I'm not sure if needed
      *
      * @param pMinByteCount the minimum bytes to be read -> ftdevice blocks, until buffer is fully read
      *
      * @return the reply read
      */
     private String readReply(int pMinByteCount) {
         try {
             // todo read into a buffer would be good, however, we do not get a result, because ftdevice blocks (maybe it waits for the buffer to fill)
             // so we read a minimum into a byte array and after that one byte after another until we get the termination character ";"
             StringBuilder tResult = new StringBuilder();
 
             int tReceivedCount = 0;
             if (pMinByteCount > 1) {
                 byte[] tMinBytesToRead = new byte[pMinByteCount];
                 ftDevice.read(tMinBytesToRead);
                 tResult.append(new String(tMinBytesToRead, US_ASCII));
                 tReceivedCount += tMinBytesToRead.length;
             }
 
             int tReadByte;
 
             while ((tReadByte = ftDevice.read()) >= 0) {
                 tReceivedCount++;
                 if ((char) tReadByte == ';') {
                     break;
                 }
                 tResult.append((char) tReadByte);
             }
             transferStatistics.addBytesReceived(tReceivedCount);
             return tResult.toString();
         } catch (FTD2XXException ex) {
             handleException(ex);
             return null;
         }
     }
 
     @Override
     public void paint(final BufferedImage pBufferedImage, final int pX, final int pY, final int pWidth, final int pHeight) {
         paintClip(pBufferedImage, pX, pY, pWidth, pHeight);
     }
 
     public void paintFullScreen(final BufferedImage pBufferedImage) {
         // try to reduce garbage creation -> it would be good, if we can store the data of a bufferedimage in a byte[], instead of copy one...
 
         // currently we only support to transfer the whole screen
         int x = 0;
         int y = 0;
         int width = pBufferedImage.getWidth();
         int height = pBufferedImage.getHeight();
 
 
         final byte[] tCommandPrefixData = getBytes("Bitmap (0," + x + "," + y + "," + width + "," + height + ",#");
         final byte[] tCommandPostfixData = getBytes(");");
 
         final DataBufferByte tImageBuffer = (DataBufferByte) pBufferedImage.getRaster().getDataBuffer();
         final byte[] tBuffer = tImageBuffer.getData();
         final int BMPDataLength = tBuffer.length;
 
         final int tSize = tCommandPrefixData.length + 4 + tBuffer.length + tCommandPostfixData.length;
         final byte[] tData = new byte[tSize];
 
         int tDestPos = 0;
 
         System.arraycopy(tCommandPrefixData, 0, tData, tDestPos, tCommandPrefixData.length);
         tDestPos += tCommandPrefixData.length;
 
         tData[tDestPos++] = (byte) (BMPDataLength >> 24);
         tData[tDestPos++] = (byte) (BMPDataLength >> 16);
         tData[tDestPos++] = (byte) (BMPDataLength >> 8);
         tData[tDestPos++] = (byte) BMPDataLength;
 
         System.arraycopy(tBuffer, 0, tData, tDestPos, tBuffer.length);
         tDestPos += tBuffer.length;
 
         System.arraycopy(tCommandPostfixData, 0, tData, tDestPos, tCommandPostfixData.length);
         tDestPos += tCommandPostfixData.length;
 
         sendData(tData);
     }
 
     public void paintClip(final BufferedImage pBufferedImage, final int pX, final int pY, final int pWidth, final int pHeight) {
         if (isPaused()) {
             return;
         }
         // we support only byte aligned clips
         final int x = pX & ~7;
         final int y = pY & ~7;
         final int width = ((pX + pWidth + 7) & ~7) - x;
         final int height = ((pY + pHeight + 7) & ~7) - y;
 
         final byte[] tCommandInfoData = getBytes(x + "," + y + "," + width + "," + height + ",#");
 
         final DataBufferByte tImageBuffer = (DataBufferByte) pBufferedImage.getRaster().getDataBuffer();
         final byte[] tBuffer = tImageBuffer.getData();
 
         final int tBmpDataLength = (width * height) / 8;
 
         int tDestPos = 0;
 
         System.arraycopy(BITMAP_COMMAND_PREFIX, 0, bitmapData, tDestPos, BITMAP_COMMAND_PREFIX.length);
         tDestPos += BITMAP_COMMAND_PREFIX.length;
 
         System.arraycopy(tCommandInfoData, 0, bitmapData, tDestPos, tCommandInfoData.length);
         tDestPos += tCommandInfoData.length;
 
         bitmapData[tDestPos++] = (byte) (tBmpDataLength >> 24);
         bitmapData[tDestPos++] = (byte) (tBmpDataLength >> 16);
         bitmapData[tDestPos++] = (byte) (tBmpDataLength >> 8);
         bitmapData[tDestPos++] = (byte) tBmpDataLength;
 
         // now copy row by row
         for (int tRow = y; tRow < y + height; tRow++) {
             System.arraycopy(tBuffer, (x / 8) + tRow * (pBufferedImage.getWidth() / 8), bitmapData, tDestPos, width / 8);
             tDestPos += width / 8;
         }
 
         System.arraycopy(BITMAP_COMMAND_POSTFIX, 0, bitmapData, tDestPos, BITMAP_COMMAND_POSTFIX.length);
         tDestPos += BITMAP_COMMAND_POSTFIX.length;
 
         sendData(bitmapData, 0, tDestPos);
     }
 
     public void close() {
         if (ftDevice != null) {
             try {
                 ftDevice.close();
             } catch (Exception ex) {
                 // ignore
             }
 
         }
     }
 
     @Override
     protected void finalize() throws Throwable {
         close();
         super.finalize();
     }
 
     @Override
     public void setBacklight(final int pBrightness) {
         setBacklightWithoutStore(pBrightness);
         sendCommand("StoreBackLight;");
        restoreBacklightValue = pBrightness;
     }
 
     private void setBacklightWithoutStore(final int pBrightness) {
         sendCommand("BackLight (" + pBrightness + ",0,0);");
     }
 
     @Override
     public int getBacklight() {
         // TODO -> this is currently unsupported by the device
 //        sendCommand("BackLight?;");
 //        return Integer.parseInt(readReply().substring("BackLight ".length() + 1));
         return restoreBacklightValue;
     }
 
     public void reset() {
         sendCommand("*RST;");
     }
 
     public TransferStatistics getTransferStatistics() {
         return transferStatistics;
     }
 
     public long getConnectStartedMillis() {
         return connectStartedMillis;
     }
 }
