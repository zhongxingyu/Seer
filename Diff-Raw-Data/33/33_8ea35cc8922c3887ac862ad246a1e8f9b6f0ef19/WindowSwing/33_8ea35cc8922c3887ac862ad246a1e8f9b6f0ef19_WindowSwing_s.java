 /**
  * Project Wonderland
  *
  * Copyright (c) 2004-2009, Sun Microsystems, Inc., All Rights Reserved
  *
  * Redistributions in source code form must reproduce the above
  * copyright and this condition.
  *
  * The contents of this file are subject to the GNU General Public
  * License, Version 2 (the "License"); you may not use this file
  * except in compliance with the License. A copy of the License is
  * available at http://www.opensource.org/licenses/gpl-license.php.
  *
  * Sun designates this particular file as subject to the "Classpath" 
  * exception as provided by Sun in the License file that accompanied 
  * this code.
  */
 package org.jdesktop.wonderland.modules.appbase.client.swing;
 
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import org.jdesktop.wonderland.client.jme.MainFrame;
 import org.jdesktop.wonderland.modules.appbase.client.WindowGraphics2D;
 import org.jdesktop.wonderland.modules.appbase.client.App2D;
 import org.jdesktop.wonderland.modules.appbase.client.swing.WindowSwingEmbeddedToolkit.WindowSwingEmbeddedPeer;
 import com.sun.embeddedswing.EmbeddedPeer;
 import com.jme.math.Vector2f;
 import java.awt.Canvas;
 import java.util.Iterator;
 import java.util.logging.Logger;
 import javax.swing.JPanel;
 import org.jdesktop.mtgame.Entity;
 import org.jdesktop.mtgame.EntityComponent;
 import org.jdesktop.wonderland.client.input.Event;
 import org.jdesktop.wonderland.client.input.EventClassListener;
 import org.jdesktop.wonderland.client.input.EventListenerBaseImpl;
 import org.jdesktop.wonderland.client.input.InputManager;
 import org.jdesktop.wonderland.client.input.InputManager.WindowSwingViewMarker;
 import org.jdesktop.wonderland.client.jme.JmeClientMain;
 import org.jdesktop.wonderland.common.ExperimentalAPI;
 import org.jdesktop.wonderland.client.jme.input.SwingEnterExitEvent3D;
 import org.jdesktop.wonderland.modules.appbase.client.DrawingSurfaceBufferedImage;
 import org.jdesktop.wonderland.modules.appbase.client.Window2D;
 import org.jdesktop.wonderland.modules.appbase.client.view.View2D;
 
 /**
  * A 2D window in which a Swing panel can be displayed. Use <code>setComponent</code> to specify the Swing panel.
  * Here is an example of how to use <code>WindowSwing</code>. (Note that it is extremely important that the 
  * Swing panel be parented as an descendant of the root frame).
  * <br><br>
  * <code>
  * JPanel testPanel = new TestPanel();
  * <br>
  * JmeClientMain.getFrame().getCanvas3DPanel().add(testPanel);
  * <br>
  * setComponent(testPanel);
  * </code>
  */
 
 // TODO: currently this has JME dependencies. It would be nice to do this in a graphics library independent fashion.
 @ExperimentalAPI
 public class WindowSwing extends WindowGraphics2D {
 
     private static final Logger logger = Logger.getLogger(WindowSwing.class.getName());
     /** The Swing component which is displayed in this window */
     private Component component;
     /** The Swing Embedder object */
     private WindowSwingEmbeddedPeer embeddedPeer;
     /** The size of the window */
     private Dimension size;
     /** Is input enabled on this window? */
     private boolean inputEnabled = true;
     /** A listener which allows the inputEnabled attribute to control whether this window receives events. */
     private MyEventListener eventListener;
 
     /** The event listener for this window. */
     private class MyEventListener extends EventListenerBaseImpl {
 
         @Override
         public boolean consumesEvent(Event event) {
             return inputEnabled;
         }
 
         @Override
         public boolean propagatesToParent(Event event) {
             return false;
         }
     }
 
     /** An entity component which provides a back pointer from the entity of a WindowSwing to the WindowSwing. */
     static class WindowSwingViewReference extends EntityComponent {
         private View2D view;
         WindowSwingViewReference (View2D view) {
             this.view = view;
         }
         View2D getView() {
             return view;
         }
     }
 
     /**
      * Create an instance of WindowSwing with a default name. The first such window created for an app 
      * becomes the primary window. Subsequent windows are secondary windows.
      * @param app The application to which this window belongs.
      * @param width The window width (in pixels).
      * @param height The window height (in pixels).
      * @param decorated Whether the window is decorated with a frame.
      * @param pixelScale The size of the window pixels.
      */
     public WindowSwing(App2D app, int width, int height, boolean decorated, Vector2f pixelScale) {
         this(app, width, height, decorated, pixelScale, null);
     }
 
     /**
      * Create an instance of WindowSwing with the given name. The first such window created for an app 
      * becomes the primary window. Subsequent windows are secondary windows.
      * @param app The application to which this window belongs.
      * @param width The window width (in pixels).
      * @param height The window height (in pixels).
      * @param decorated Whether the window is top-level (e.g. is decorated) with a frame.
      * @param pixelScale The size of the window pixels.
      * @param name The name of the window.
      */
     public WindowSwing(App2D app, int width, int height, boolean decorated, Vector2f pixelScale, 
                        String name) {
         super(app, width, height, decorated, pixelScale, name, 
               new DrawingSurfaceBufferedImage(width, height)); 
         initializeViews();
     }
 
     /**
      * Create an instance of WindowSwing of the given type with a default name. 
      * @param app The application to which this window belongs.
      * @param type The type of the window. If this is non-primary, the parent is set to the primary
      * window of the app (if there is one).
      * @param width The window width (in pixels).
      * @param height The window height (in pixels).
      * @param decorated Whether the window is decorated with a frame.
      * @param pixelScale The size of the window pixels.
      */
     public WindowSwing(App2D app, Type type, int width, int height, boolean decorated, 
                           Vector2f pixelScale) {
         this(app, type, width, height, decorated, pixelScale, null);
     }
 
     /**
      * Create an instance of WindowSwing of the given type with the given name. 
      * @param app The application to which this window belongs.
      * @param type The type of the window. If this is non-primary, the parent is set to the primary
      * window of the app (if there is one).
      * @param width The window width (in pixels).
      * @param height The window height (in pixels).
      * @param decorated Whether the window is top-level (e.g. is decorated) with a frame.
      * @param pixelScale The size of the window pixels.
      * @param name The name of the window.
      */
     public WindowSwing(App2D app, Type type, int width, int height, boolean decorated, Vector2f pixelScale, 
                        String name) {
         this(app, type, app.getPrimaryWindow(), width, height, decorated, pixelScale, name);
     }
 
     /**
      * Create an instance of WindowSwing of the given type with the given parent with a default name. 
      * @param app The application to which this window belongs.
      * @param type The type of the window. 
      * @param parent The parent of the window. (Ignored for primary windows).
      * @param width The window width (in pixels).
      * @param height The window height (in pixels).
      * @param decorated Whether the window is decorated with a frame.
      * @param pixelScale The size of the window pixels.
      */
     public WindowSwing(App2D app, Type type, Window2D parent, int width, int height, boolean decorated, 
                        Vector2f pixelScale) {
         this(app, type, parent, width, height, decorated, pixelScale, null);
     }
 
     /**
      * Create an instance of WindowSwing of the given type with the given parent with the given name. 
      * @param app The application to which this window belongs.
      * @param type The type of the window. If this is non-primary, the parent is set to the primary
      * window of the app (if there is one).
      * @param parent The parent of the window. (Ignored for primary windows).
      * @param width The window width (in pixels).
      * @param height The window height (in pixels).
      * @param decorated Whether the window is top-level (e.g. is decorated) with a frame.
      * @param pixelScale The size of the window pixels.
      * @param name The name of the window.
      */
     public WindowSwing(App2D app, Type type, Window2D parent, int width, int height, boolean decorated, 
                     Vector2f pixelScale, String name) {
         super(app, type, parent, width, height, decorated, pixelScale, name,
               new DrawingSurfaceBufferedImage(width, height)); 
         initializeViews();
     }
 
     public void cleanup () {
         cleanupViews();
         super.cleanup();
     }
 
     /** Initialize all existing views. */
     private void initializeViews () {
         Iterator<View2D> it = getViews();
         while (it.hasNext()) {
             View2D view = it.next();
             viewInit(view);
         }
     }
 
     /** Clean up all views. */
     private void cleanupViews () {
         Iterator<View2D> it = getViews();
         while (it.hasNext()) {
             View2D view = it.next();
             viewCleanup(view);
         }
     }
 
     /** 
      * Specify the Swing component displayed in this window. The component is validated (that is it
      * is layed out).
      *
      * Note: After you call <code>setComponent</code> the window will be in "preferred size" mode, 
      * that is, it the window will be sized according to the Swing component's preferred sizes and 
      * the component's layout manager. If you call <code>WindowSwing.setSize(int width, int height)</code> or 
      * <code>WindowSwing.setSize(Dimension dims)</code> with a non-null <code>dims</code> the window will be 
      * in "forced size" mode. This means that the window will always be the size you specify and this
      * will constrain the sizes of the contained component. To switch back into preferred size mode
      * call <code>WindowSwing.setSize(null)</code>.
      *
      * @param component The component to be displayed.
      */
     public void setComponent(Component component) {
         if (this.component == component) {
             return;
         }
         this.component = component;
         if (embeddedPeer != null) {
             embeddedPeer.dispose();
             embeddedPeer = null;
         }
         if (component != null) {
             checkContainer();
 
         // TODO: may eventually need this
         //FocusHandler.addNotify(this);
         }
 
 	// TODO: Uncomment this to demonstrate the embedded component enter/exit bug
 	//component.addMouseListener(new MyAwtEnterListener());
 
         addEventListener(new MySwingEnterExitListener());
 
         embeddedPeer.validate();
         embeddedPeer.repaint();
     }
 
     /* TODO: I'm leaving this hear to illustrate a bug
     private class MyAwtEnterListener extends MouseAdapter {
 
         public void mouseEntered(MouseEvent e) {
             if (e.getID() == MouseEvent.MOUSE_ENTERED) {
                 System.err.println("********* MOUSE Entered Window Swing embedded component");
             } else {
                 System.err.println("********* MOUSE Exited Window Swing embedded component");
             }
         }
     }
     */
 
     private static class MySwingEnterExitListener extends EventClassListener {
 
         @Override
         public Class[] eventClassesToConsume() {
             return new Class[]{SwingEnterExitEvent3D.class};
         }
 
         @Override
         public void commitEvent(Event event) {
             SwingEnterExitEvent3D seeEvent = (SwingEnterExitEvent3D) event;
 
             /* For debug
             StringBuffer sb = new StringBuffer();
             String typeStr = "SWING " + (seeEvent.isEntered() ? "ENTER" : "EXIT");
             sb.append(typeStr + ", entity = " + seeEvent.getEntity());
             System.err.println(sb.toString());
              */
 
             if (seeEvent.isEntered()) {
                 Entity entity = seeEvent.getEntity();
                 EntityComponent comp = entity.getComponent(WindowSwing.WindowSwingViewReference.class);
                 assert comp != null;
                 View2D view = ((WindowSwing.WindowSwingViewReference) comp).getView();
                 WindowSwing windowSwing = (WindowSwing) view.getWindow();
                 assert windowSwing != null;
                 windowSwing.requestFocusInWindow();
             } else {
                 requestFocusInWindowForCanvas();
             }
         }
     }
 
     /** 
      * Request focus in the Wonderland client's canvas, provided that the top-level frame has focus.
      */
     private static void requestFocusInWindowForCanvas() {
         Canvas canvas = JmeClientMain.getFrame().getCanvas();
         if (!canvas.requestFocusInWindow()) {
             logger.warning("Focus request for main canvas rejected.");
         }
     }
 
     /** 
      * Request focus in this window's embedded component, provided that the top-level frame has focus.
      */
     private void requestFocusInWindow() {
         if (!component.requestFocusInWindow()) {
             logger.warning("Focus request for embedded component rejected.");
         }
     }
 
     /** Returned the Swing component displayed in this window */
     public final Component getComponent() {
         return component;
     }
 
     /** First time initialization */
     private void checkContainer() {
         if (component == null) {
             if (embeddedPeer != null) {
                 embeddedPeer.dispose();
                 embeddedPeer = null;
             }
             return;
         }
 
         MainFrame frame = JmeClientMain.getFrame();
         JPanel embeddedParent = frame.getCanvas3DPanel();
         if (embeddedParent == null) {
             logger.warning("Embedded parent is null");
             return;
         }
 
         if (embeddedParent != null) {
             if (embeddedPeer != null && embeddedPeer.getParentComponent() != embeddedParent) {
                 embeddedPeer.dispose();
                 embeddedPeer = null;
             }
             if (embeddedPeer == null) {
                 WindowSwingEmbeddedToolkit embeddedToolkit =
                         WindowSwingEmbeddedToolkit.getWindowSwingEmbeddedToolkit();
                 embeddedPeer = embeddedToolkit.embed(embeddedParent, component);
                 embeddedPeer.setWindowSwing(this);
 
             // TODO: may eventually need this
             //embeddedPeer.setFocusTraversalPolicy(FocusHandler.getFocusTraversalPolicy());
             }
         }
     }
 
     /**
      * Specify the size of the window only (not the embedded peer).
      */
     void setWindowSize (int width, int height) {
         super.setSize(width, height);
     }
 
     /**
      * Specify the size of this WindowSwing. This switches the window from "preferred size" mode
      * to "forced size" mode?
      */
     @Override
     public void setSize (int width, int height) {
         setSize(new Dimension(width, height));
     }
 
     /**
      * Specify the size of this WindowSwing. If dims is non-null, the window is switched
      * into "forced size" mode--the window will be always be the size you specify. If dims is null,
      * the window is switched into "preferred size" mode--the window will size will be determined
      * by the size and layout of the embedded Swing component.
      */
     public void setSize (Dimension dims) {
         if (embeddedPeer == null) {
             throw new RuntimeException("You must first set a component for this WindowSwing.");
         }
         embeddedPeer.setSize(dims);
         embeddedPeer.validate();
         embeddedPeer.repaint();
     }
 
     /**
      * Re-lay out the contents of this window. This should be called whenever you make changes which
      * affect the layout of the contained component.
      */
     public void validate () {
        if (embeddedPeer == null) {
            throw new RuntimeException("You must first set a component for this WindowSwing.");
         }
        embeddedPeer.validate();
        embeddedPeer.repaint();
     }
 
     /**
      * Repaint out the contents of this window.
      */
     public void repaint () {
         if (embeddedPeer == null) {
             throw new RuntimeException("You must first set a component for this WindowSwing.");
         }
         embeddedPeer.repaint();
     }
     public final EmbeddedPeer getEmbeddedPeer () {
 	return embeddedPeer;
     }
 
     protected void paint(Graphics2D g) {}
 
     /** 
      * Set the input enable for this window. By default, input for a WindowSwing is enabled.
      */
     public void setInputEnabled(boolean enabled) {
         inputEnabled = enabled;
     }
 
     /** 
      * Return the input enabled for this window.
      */
     public boolean getInputEnabled() {
         return inputEnabled;
     }
 
     /** {@inheritDoc} */
     @Override
     public void addView(View2D view) {
         super.addView(view);
         viewInit(view);
     }
 
     /** {@inheritDoc} */
     @Override
     public void removeView(View2D view) {
         viewCleanup(view);
         super.removeView(view);
     }
 
     /** Attach the things we need to the given given view. */
     private void viewInit (View2D view) {
         view.addEntityComponent(InputManager.WindowSwingViewMarker.class, new WindowSwingViewMarker());
         view.addEntityComponent(WindowSwingViewReference.class, new WindowSwingViewReference(view));
         eventListener = new MyEventListener();
         view.addEventListener(eventListener);
     }
 
     /** Attach the things we use from the given given view. */
     private void viewCleanup (View2D view) {
         view.removeEntityComponent(InputManager.WindowSwingViewMarker.class);
         view.removeEntityComponent(WindowSwingViewReference.class);
         view.removeEventListener(eventListener);
         eventListener = null;
     }
 }
