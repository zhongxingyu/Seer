 /*
  * Created On: Nov 23, 2006 8:13:10 AM
  */
 package com.thinkparity.codebase.swing;
 
 import java.awt.Dimension;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Window;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JComponent;
 import javax.swing.SwingUtilities;
 import javax.swing.event.MouseInputAdapter;
 import javax.swing.event.MouseInputListener;
 
 import com.thinkparity.codebase.log4j.Log4JWrapper;
 
 /**
  * <b>Title:</b>thinkParity Component Move Helper<br>
  * <b>Description:</b>A helper class designed to move the underlying window of
  * components. Simply call add listener on the component you wish to drag a
  * window around by.<br>
  * 
  * @author raymond@thinkparity.com
  * @version 1.1.2.1
  */
 final class JComponentMoveHelper {
 
     /** The original component position when start a move. */
     private static Point jComponentOrigin;
 
     /**
      * A client property key <code>String</code> used to store the mouse input
      * adapter within the component.
      */
     private static final String LISTENER_CLIENT_PROPERTY_KEY;
 
     /** The original mouse position when start a move. */
     private static Point mouseOrigin;
 
     static {
         LISTENER_CLIENT_PROPERTY_KEY = new StringBuffer(JComponentMoveHelper.class.getName())
             .append("#mouseInputListener").toString();
     }
 
     /** An apache logger wrapper. */
     private final Log4JWrapper logger;
     
     /** The offset x and y coordinates the mouse was dragged. */
     private int offsetX, offsetY;
     
     /** The number of pixels from the edge of a screen to interpret as sticky. */
     private final int stickyPixels;
 
     /**
      * Create JComponentMoveHelper.
      * 
      * @param jFrame
      *            An <code>AbstractJFrame</code>.
      */
     JComponentMoveHelper(final AbstractJFrame jFrame) {
         this();
     }
 
     /**
      * Create JComponentMoveHelper.
      * 
      * @param jPanel
      *            An <code>AbstractJPanel</code>.
      */
     JComponentMoveHelper(final AbstractJPanel jPanel) {
         this();
     }
 
     /**
      * Create JComponentMoveHelper.
      *
      */
     private JComponentMoveHelper() {
         super();
         this.logger = new Log4JWrapper();
         this.stickyPixels = 25;
     }
 
     /**
      * Add a move listener to the component.
      * 
      * @param jComponent
      *            A <code>JComponent</code>.
      */
     void addListener(final JComponent jComponent) {
         // property to protect against adding it twice
         MouseInputListener mouseInputListener =
             (MouseInputListener) jComponent.getClientProperty(LISTENER_CLIENT_PROPERTY_KEY);
         if (null == mouseInputListener) {
             mouseInputListener = new MouseInputAdapter() {
                 @Override
                 public void mouseDragged(final MouseEvent e) {
                     jComponentMouseDragged(e);
                 }
                 @Override
                 public void mousePressed(final MouseEvent e) {
                     jComponentMousePressed(e);
                 }
             };
             jComponent.addMouseMotionListener(mouseInputListener);
             jComponent.addMouseListener(mouseInputListener);
             jComponent.putClientProperty(LISTENER_CLIENT_PROPERTY_KEY, mouseInputListener);
         }
     }
 
     /**
      * Debug the move helper's state.
      *
      */
     void debug() {
         logger.logVariable("mouseOrigin", mouseOrigin);
         logger.logVariable("jComponentOrigin", jComponentOrigin); 
         logger.logVariable("offsetX", offsetX);
         logger.logVariable("offsetY", offsetY);
     }
 
     /**
      * Remove a move listener from the component.
      * 
      * @param jComponent
      *            A <code>JComponent</code>.
      */
     void removeListener(final JComponent jComponent) {
         MouseInputListener mouseInputListener =
             (MouseInputListener) jComponent.getClientProperty(LISTENER_CLIENT_PROPERTY_KEY);
         if (null != mouseInputListener) {
             jComponent.removeMouseMotionListener(mouseInputListener);
             jComponent.removeMouseListener(mouseInputListener);
             jComponent.putClientProperty(LISTENER_CLIENT_PROPERTY_KEY, null);
             mouseInputListener = null;
         }        
     }
 
     /**
      * The component's mouse dragged event was fired.
      * 
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void jComponentMouseDragged(final MouseEvent e) {        
         final Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
         final Point jComponentLocation = SwingUtilities.getWindowAncestor(((JComponent) e.getSource())).getLocation();
        offsetX = mouseLocation.x - mouseOrigin.x - jComponentLocation.x - jComponentOrigin.x;
        offsetY = mouseLocation.y - mouseOrigin.y - jComponentLocation.y - jComponentOrigin.y;
         moveWindow(e);
     }
 
     /**
      * The component's mouse pressed event was fired.
      * 
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void jComponentMousePressed(final MouseEvent e) {
         mouseOrigin = MouseInfo.getPointerInfo().getLocation();
         jComponentOrigin = SwingUtilities.getWindowAncestor(((JComponent) e.getSource())).getLocation();
     }
 
     /**
      * Move the window.
      * 
      * @param e
      *            A <code>MouseEvent</code>.
      */
     private void moveWindow(final MouseEvent e) {
         debug();
         if (offsetX != 0 || offsetY != 0) {
             final Window window =
                 SwingUtilities.getWindowAncestor(((JComponent) e.getSource()));
             final Point location = window.getLocation();
             final Point newLocation = (Point) location.clone();
             final Dimension size = window.getSize();
             final Dimension primaryScreenSize = SwingUtil.getPrimaryScreenSize();
             newLocation.x += offsetX;
             newLocation.y += offsetY;
             // if the location on the screen is within a given number of pixels;
             // stick to the size of the side of the screen
             if (stickyPixels > newLocation.x) {
                 newLocation.x = 0;
             } else if (newLocation.x + size.width > primaryScreenSize.width - stickyPixels) {
                 newLocation.x = primaryScreenSize.width - size.width;
             }
             if (stickyPixels > newLocation.y) {
                 newLocation.y = 0;
             } else if (newLocation.y + size.height > primaryScreenSize.height - stickyPixels) {
                 newLocation.y = primaryScreenSize.height - size.height;
             }
             window.setLocation(newLocation);
             window.firePropertyChange("location.x", Long.valueOf(location.x), Long.valueOf(newLocation.x));
             window.firePropertyChange("location.y", Long.valueOf(location.y), Long.valueOf(newLocation.y));
         }
     }
 }
