 package com.mucommander.ui.viewer.text;
 
 import com.google.common.io.Closeables;
 import com.google.common.util.concurrent.Futures;
 import com.mucommander.commons.file.AbstractFile;
 import com.mucommander.commons.io.bom.BOMInputStream;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.io.*;
 import java.util.List;
 
 /**
  * @author Eugene Morozov
  */
 public class TextViewerImpl extends TextProcessor {
 
     public static final String LINE_SEPARATOR = System.getProperty("line.separator");
     private BufferedReader reader;
 
     private boolean endOfStreamIsReached = false;
 
     /**
      * Buffer to download file into textArea.
      * It is equal to one line width to append additional
      * lines of text during scrolling.
      * <p/>
      * Assumption of initial textArea's line width
      * (should be enough for most of the cases)
      */
     private char[] buffer = new char[200];
 
     @Override
     protected void initTextArea() {
         super.initTextArea();
         textArea.setEditable(false);
 
         textArea.addMouseWheelListener(new MouseWheelListener() {
             @Override
             public void mouseWheelMoved(MouseWheelEvent e) {
                 switch (e.getScrollType()) {
                     case MouseWheelEvent.WHEEL_UNIT_SCROLL:
                         int unitsToScroll = e.getUnitsToScroll();
                         if (unitsToScroll > 0) {
                             readRows(unitsToScroll);
                         }
                 }
             }
         });
 
         textArea.addKeyListener(new KeyAdapter() {
             @Override
             public void keyReleased(KeyEvent e) {
                 switch (e.getKeyCode()) {
                     case KeyEvent.VK_DOWN:
                         readRows(1);
                         break;
                     case KeyEvent.VK_PAGE_DOWN:
                         readRows(calcHeightInRows());
                         break;
                 }
             }
         });
 
         //Reinitialize buffer size in case of textArea's size is changed
         textArea.addComponentListener(new ComponentAdapter() {
             @Override
             public void componentResized(ComponentEvent e) {
                 //Each time text is appended into textArea it's resizing itself.
                 //Thus rely on size of its parent
                 initializeBuffer(calcWidthInSymbols());
             }
         });
     }
 
     /**
      * Calculates height in rows for visible part of {@link #textArea}
      *
      * @return number of rows
      */
     private int calcHeightInRows() {
         FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
         return textArea.getParent().getHeight() / metrics.getHeight();
     }
 
     /**
      * Calculates width in symbols for visible part of {@link #textArea}
      *
      * @return width in symbols
      */
     private int calcWidthInSymbols() {
         FontMetrics metrics = textArea.getFontMetrics(textArea.getFont());
 
         //standard procedure to get symbol width
         return textArea.getParent().getWidth() / metrics.charWidth('m');
     }
 
     /**
      * Initializes internal buffer according to width of view size.
      * Calculates number of columns according to specified font and
      * creates buffer of such size.
      * The buffer is equal to one line of textArea.
      *
      * @param width current view width
      */
     private void initializeBuffer(int width) {
         if (width != buffer.length) {
             buffer = new char[width];
         }
     }
 
     @Override
     void read(AbstractFile file, String encoding) throws IOException {
         InputStream input = file.getInputStream();
 
         // If the encoding is UTF-something, wrap the stream in a BOMInputStream to filter out the byte-order mark
         // (see ticket #245)
         if (encoding.toLowerCase().startsWith("utf")) {
             input = new BOMInputStream(input);
         }
 
         reader = new BufferedReader(new InputStreamReader(input, encoding), 8192);
         final int rowsToRead = calcHeightInRows();
         readRows(rowsToRead == 0 ? 50 : rowsToRead); //read first 50 lines at the beginning
 
         textArea.setCaretPosition(0); // Move cursor to the top
     }
 
     /**
      * Reads next portion of data from {@link #reader}
      * <p/>
      * Doesn't require to have additional buffer for smooth scrolling.
      * Smooth will be supplied by BufferedReader buffer size.
      * <p/>
      * It's in times more than one general line width.
      * <p/>
      * In case there is line wrapping turned off it requires to read whole line
      * in original file. That could be comparatively long (in case of really long
      * line it could even kill the app), but in other case it gives much
      * inconvenience for user experience.
      */
     private void readRows(final int rowsToRead) {
         if (!endOfStreamIsReached) {
             SwingWorker<Boolean, String> worker = new SwingWorker<Boolean, String>() {
 
                 @Override
                 protected Boolean doInBackground() throws Exception {
                     int rows = rowsToRead;
                     int count = 0;
                     while (rows-- > 0 && count != -1) {
                         if (textArea.getLineWrap()) {
                             count = reader.read(buffer);
                             if (count != -1) {
                                 publish(new String(buffer, 0, count));
                             }
                         } else {
                             String line = reader.readLine();
                             if (line == null) {
                                 return true;
                             }
                             publish(line + LINE_SEPARATOR);
                         }
                     }
                     return count == -1;
                 }
 
                 @Override
                 protected void process(List<String> chunks) {
                     for (String chunk : chunks) {
                         textArea.append(chunk);
                     }
                 }
 
                 @Override
                 protected void done() {
                     endOfStreamIsReached = Futures.getUnchecked(this);
                 }
             };
            worker.run();
         }
     }
 
     @Override
     void write(Writer writer) throws IOException {
         //Do nothing - it's file viewer
     }
 
     @Override
     public void beforeCloseHook() {
         new Thread() {
             @Override
             public void run() {
                 Closeables.closeQuietly(reader);
             }
         }.start();
     }
 }
