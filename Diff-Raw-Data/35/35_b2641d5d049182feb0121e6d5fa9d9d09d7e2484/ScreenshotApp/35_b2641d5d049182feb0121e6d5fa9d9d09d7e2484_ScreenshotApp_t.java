 package com.screenshot.gui;
 
 import java.awt.Cursor;
 import java.awt.Rectangle;
 import java.awt.event.*;
 
 import javax.swing.*;
 
 import com.screenshot.ScreenshotListener;
 import com.screenshot.Settings;
 import com.screenshot.util.Messenger;
 import com.screenshot.util.ScreenUtils;
 
 public class ScreenshotApp {
 
     private final JFrame frame;
     ScreenshotPanel screenshotPanel;
     private final Messenger messenger;
     private boolean closed = false;
 
     public ScreenshotApp(ScreenshotListener listener, Messenger messenger) {
         this.messenger = messenger;
         frame = new JFrame("Screenshot");
         screenshotPanel = new ScreenshotPanel(listener, messenger);
         frame.getContentPane().add(screenshotPanel);
         frame.setUndecorated(true);
         frame.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
         frame.addKeyListener(new KeyAdapter() {
             @Override
             public void keyPressed(KeyEvent e) {
                 if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    close("ESC button");
                 }
             }
         });
         frame.addFocusListener(new FocusAdapter() {
             public void focusLost(FocusEvent e) {
                close("focus lost");
             }
         });
         frame.addWindowListener(new WindowAdapter() {
             public void windowIconified(WindowEvent e) {
                close("inconified");
             }
         });      
         open();
     }
 
     private void open() {
         Rectangle screen = ScreenUtils.getScreenBounds();
         frame.setLocation(screen.getLocation());
         frame.setSize(screen.width, screen.height);
         frame.setVisible(true);
         //If we don't dispose but simply hide window between snapshots 
         // then we need the code below to cope with iconified windows
         //frame.setState(Frame.NORMAL);
         //frame.setVisible(true);
         //frame.setVisible(true);
     }
 
    public void close(String reason){
        close(reason, false, true);
    }

    public void close(String reason, boolean synchGC, boolean exit) {
         if (closed){
             return;
         } else {
             closed = true;
         }
         messenger.debug("Window is closed: " + reason);
         screenshotPanel.clear();
         frame.dispose();
         if (Settings.getInstance().isSystemTrayMode()) {
             // need explicit GC for two reason
             // first keep heap small and don't waste PC memory
             // second avoid OOM in case of abnormal frequent user clicks
             if (synchGC) {
                 GC.run();
             } else {
                 SwingUtilities.invokeLater(GC);
             }
        } else if (exit) {
             System.exit(0);
         }
     }
 
     private final static Runnable GC = new Runnable() {
         public void run() {
             System.gc();
             try {
                 Thread.sleep(100L);
             } catch (InterruptedException e) {
                 //ignore
             }
         }
     };
 
 }
