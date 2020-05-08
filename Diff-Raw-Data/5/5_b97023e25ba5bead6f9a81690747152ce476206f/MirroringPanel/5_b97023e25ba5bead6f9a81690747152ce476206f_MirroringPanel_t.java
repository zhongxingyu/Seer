 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package org.netbeans.modules.javafx.preview;
 
 import java.awt.AWTEvent;
 import java.awt.AWTEvent.*;
 import java.awt.BorderLayout;
 import java.awt.Component;
 import java.awt.DefaultKeyboardFocusManager;
 import java.awt.EventQueue;
 import java.awt.Graphics;
 import java.awt.KeyboardFocusManager;
 import java.awt.Point;
 import java.awt.Window;
 import java.awt.event.InvocationEvent;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import javax.swing.*;
 
 import javax.swing.JPanel;
 import org.openide.util.Exceptions;
 import sun.awt.AppContext;
 import sun.awt.SunToolkit;
 
 public class MirroringPanel extends JPanel {
     JDialog         mirroredFrame = null;
     JPanel          mirroredPanel = null;
     MirroringThread mirroringTread = null;
     EventQueue      mirroredEventQueue = null;
     EventQueue      mirroringEventQueue = null;
     AppContext      ac = null;
     LookAndFeel     lf = null;
     BufferedImage   offscreenBuffer = null;
     ThreadGroup     threadGroup = null;
     
     public MirroringPanel(LookAndFeel lf) {
         super();
         mirroringEventQueue = SunToolkit.getDefaultToolkit().getSystemEventQueue();
         this.lf = lf;
         offscreenBuffer = (BufferedImage) createImage(getWidth(), getHeight());
         startMirroring();
     }
     
     protected JPanel createMirroredPanel() {
         return null;
     }
     
     class MirroringThread extends Thread {
         public MirroringThread(ThreadGroup tg) {
             super(tg, "SACT");
         }
         
         @Override
         public void run() {
             ac = SunToolkit.createNewAppContext();
 
             try {
                 UIManager.setLookAndFeel(lf);
             } catch (UnsupportedLookAndFeelException ex) {
                 Exceptions.printStackTrace(ex);
             }
             
             mirroredEventQueue = SunToolkit.getDefaultToolkit().getSystemEventQueue();
             
             KeyboardFocusManager.setCurrentKeyboardFocusManager(new DefaultKeyboardFocusManager(){
 
               @Override
                 public Window getFocusedWindow() {
                     synchronized (KeyboardFocusManager.class) {
                         return (mirroredFrame);
                     }
                 }
             });
 
             mirroredFrame = new JDialog() {
                 //@Override
                 /*public boolean getFocusableWindowState() {
                     return false;
                 }*/
             };
             mirroredFrame.setLayout(new BorderLayout());
             JScrollPane jsp = new JScrollPane();
             mirroredPanel = createMirroredPanel();
             jsp.setViewportView(mirroredPanel);
             mirroredFrame.add(jsp);
             mirroredFrame.setLocation(-2000, -2000);
             mirroredFrame.setVisible(true);
             mirroredFrame.setSize(getSize().width + mirroredFrame.getInsets().left + mirroredFrame.getInsets().right, getSize().height + mirroredFrame.getInsets().top + mirroredFrame.getInsets().bottom);
             
             RepaintManager.setCurrentManager(new RepaintManager() {
                 @Override
                 public void paintDirtyRegions() {
                     super.paintDirtyRegions();
                     if (offscreenBuffer != null) {
                         mirroredFrame.getLayeredPane().paintAll(offscreenBuffer.getGraphics());
                        mirroringEventQueue.postEvent(new InvocationEvent(SunToolkit.getDefaultToolkit(), new Runnable() {
                             public void run() {
                                 if (offscreenBuffer != null) {
                                     repaint();
                                 }
                             }
                        }));
                     }
                 }
             });
         }
     }
 
     @Override
     protected void processMouseEvent(MouseEvent e) {
         super.processMouseEvent(e);
         onMouseEvent(e);
     }
 
     @Override
     protected void processMouseMotionEvent(MouseEvent e) {
         super.processMouseMotionEvent(e);
         onMouseEvent(e);
     }
 
     @Override
     public void reshape(int x, int y, int w, int h) {
         super.reshape(x, y, w, h);
         if (offscreenBuffer == null)
             offscreenBuffer = (BufferedImage) createImage(getWidth(), getHeight());
         BufferedImage newOffscreenBuffer = (BufferedImage) createImage(getWidth(), getHeight());
         newOffscreenBuffer.setData(offscreenBuffer.getRaster());
         offscreenBuffer = newOffscreenBuffer;
         if (mirroredFrame != null)
             mirroredFrame.setSize(getSize().width + mirroredFrame.getInsets().left + mirroredFrame.getInsets().right, getSize().height + mirroredFrame.getInsets().top + mirroredFrame.getInsets().bottom);
     }
 
     @Override
     public void resize(int width, int height) {
         super.resize(width, height);
         BufferedImage newOffscreenBuffer = (BufferedImage) createImage(getWidth(), getHeight());
         newOffscreenBuffer.setData(offscreenBuffer.getRaster());
         offscreenBuffer = newOffscreenBuffer;
     }
 
     @Override
     public void paint(Graphics g) {
         g.drawImage(offscreenBuffer, 0, 0, null);
     }
         
     void onMouseEvent(MouseEvent ev) {
         switch (ev.getID()) {
             case MouseEvent.MOUSE_ENTERED:
                 SwingUtilities.getWindowAncestor(this).setFocusableWindowState(false);
                 break;
             case MouseEvent.MOUSE_EXITED:
                 SwingUtilities.getWindowAncestor(this).setFocusableWindowState(true);
                 break;
         }
         if (mirroredEventQueue != null && mirroredFrame != null) {
             Point point =  SwingUtilities.convertPoint(ev.getComponent(), ev.getPoint(), this);
             point =  SwingUtilities.convertPoint(mirroredFrame.getLayeredPane(), point, mirroredFrame);
             mirroredEventQueue.postEvent(new MouseEvent(mirroredFrame, ev.getID(), ev.getWhen(), ev.getModifiers(), point.x, point.y, ev.getClickCount(), ev.isPopupTrigger()));
 
             Component mirroredComponent = SwingUtilities.getDeepestComponentAt(mirroredFrame, point.x, point.y);
             if (mirroredComponent != null)
                 setCursor(mirroredComponent.getCursor());
         }
     }
 
     public void cleanup() {
         disableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
         if (mirroredEventQueue != null)
             mirroredEventQueue.postEvent(new InvocationEvent(SunToolkit.getDefaultToolkit(), new Runnable() {
                 public void run() {
                     mirroredFrame.dispose();
                     threadGroup.stop();
                     while (threadGroup.activeCount() > 0) {
                         try {
                             Thread.sleep(100);
                         } catch (InterruptedException ex) {
                             Exceptions.printStackTrace(ex);
                         }
                     }
                     threadGroup.destroy();
                 }
             }));
         mirroredEventQueue = null;
     }
     
     @Override
     protected void finalize() throws Throwable {
         super.finalize();
         cleanup();
     }
     
     private static int instanceCounter = 0;
     
     void startMirroring() {
         threadGroup = new ThreadGroup("SACG" + instanceCounter++);
         mirroringTread = new MirroringThread(threadGroup);
         mirroringTread.start();
         try {
             mirroringTread.join();
         } catch (InterruptedException ex) {
             Exceptions.printStackTrace(ex);
         }
         enableEvents(AWTEvent.MOUSE_EVENT_MASK | AWTEvent.MOUSE_MOTION_EVENT_MASK);
     }
 }
