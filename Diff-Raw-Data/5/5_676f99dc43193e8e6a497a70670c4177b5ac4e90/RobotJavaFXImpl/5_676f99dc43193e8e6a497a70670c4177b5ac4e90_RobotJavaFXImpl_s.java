 /*******************************************************************************
  * Copyright (c) 2013 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.javafx.driver;
 
 import java.awt.AWTEvent;
 import java.awt.AWTException;
 import java.awt.MouseInfo;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.awt.Toolkit;
 import java.awt.image.BufferedImage;
 import java.lang.reflect.InvocationTargetException;
 import java.util.concurrent.Callable;
 
 import javafx.event.Event;
 import javafx.event.EventTarget;
 import javafx.geometry.BoundingBox;
 import javafx.geometry.Bounds;
 import javafx.geometry.Point2D;
 import javafx.scene.Node;
 import javafx.scene.Parent;
 import javafx.scene.Scene;
 import javafx.scene.control.ListView;
 import javafx.scene.control.TableView;
 import javafx.scene.control.TreeCell;
 import javafx.scene.control.TreeView;
 import javafx.scene.input.KeyEvent;
 import javafx.scene.input.MouseEvent;
 import javafx.stage.Screen;
 import javafx.stage.Stage;
 import javafx.stage.Window;
 
 import javax.swing.UIManager;
 
 import org.apache.commons.beanutils.PropertyUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.Validate;
 import org.eclipse.jubula.rc.common.CompSystemConstants;
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.ClickOptions.ClickModifier;
 import org.eclipse.jubula.rc.common.driver.DragAndDropHelper;
 import org.eclipse.jubula.rc.common.driver.IEventThreadQueuer;
 import org.eclipse.jubula.rc.common.driver.IMouseMotionTracker;
 import org.eclipse.jubula.rc.common.driver.IRobot;
 import org.eclipse.jubula.rc.common.driver.IRobotEventConfirmer;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.driver.InterceptorOptions;
 import org.eclipse.jubula.rc.common.driver.KeyTyper;
 import org.eclipse.jubula.rc.common.driver.MouseMovementStrategy;
 import org.eclipse.jubula.rc.common.driver.RobotTiming;
 import org.eclipse.jubula.rc.common.exception.RobotException;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.common.util.LocalScreenshotUtil;
 import org.eclipse.jubula.rc.common.util.PointUtil;
 import org.eclipse.jubula.rc.javafx.components.CurrentStages;
 import org.eclipse.jubula.rc.javafx.util.NodeBounds;
 import org.eclipse.jubula.rc.javafx.util.Rounding;
 import org.eclipse.jubula.tools.constants.InputConstants;
 import org.eclipse.jubula.tools.constants.StringConstants;
 import org.eclipse.jubula.tools.i18n.I18n;
 import org.eclipse.jubula.tools.objects.event.EventFactory;
 import org.eclipse.jubula.tools.objects.event.TestErrorEvent;
 
 /**
  * <p>
  * JavaFX implementation but similar to the AWT/Swing implementation of the
  * <code>IRobot</code> interface. It uses the {@link java.awt.Robot}to move the
  * mouse and perform clicks. Any mouse move or click is intercepted and
  * confirmed using the appropriate AWT/Swing implementations of
  * {@link org.eclipse.jubula.rc.swing.driver.IRobotEventInterceptor}and
  * {@link org.eclipse.jubula.rc.swing.driver.IRobotEventConfirmer}.
  * </p>
  *
  * <p>
  * The <code>click()</code> and <code>move()</code> implementations expect that
  * the graphics component is of type {@link java.awt.Component}and the
  * constraints object is <code>null</code> or of type {@link java.awt.Rectangle}
  * .
  * </p>
  *
  * @author BREDEX GmbH
  * @created 31.10.2013
  */
 public class RobotJavaFXImpl implements IRobot {
     /** the logger */
     private static AutServerLogger log = new AutServerLogger(
             RobotJavaFXImpl.class);
     /** ID of Metal Look and Feel */
     private static final String METAL_LAF_ID = "Metal"; //$NON-NLS-1$
     /** The AWT Robot instance. */
     private Robot m_robot;
     /** The event interceptor. */
     private RobotEventInterceptorJavaFXImpl m_interceptor;
     /** The mouse motion tracker. */
     private IMouseMotionTracker m_mouseMotionTracker;
     /** The event thread queuer. */
     private IEventThreadQueuer m_queuer;
 
     /**
      * Scrolls to a component, to make it visible. Currently only ListView and
      * TableView are supported.
      */
     private class Scroller {
         /** The component to scroll to visible. */
         private Node m_component;
 
         /**
          * @param component
          *            The component to scroll to visible.
          */
         public Scroller(Node component) {
             m_component = component;
         }
 
         /**
          * Scrolls the component to visible.
          *
          * @param component
          *            The component.
          */
         private void scrollObjectToVisible(Node component) {
             Parent parent = component.getParent();
             Node scrollNode = component;
             for (; (parent != null) && !(parent instanceof ListView)
                     && !(parent instanceof TableView)
                     && !(parent instanceof TreeView); parent = parent
                     .getParent()) {
                 if (parent instanceof TreeCell) {
                     scrollNode = parent;
                 }
             }
             if ((parent != null) && (parent instanceof ListView)) {
 
                 ((ListView) parent).scrollTo(scrollNode);
 
             } else if (parent != null && (parent instanceof TableView)) {
                 ((TableView) parent).scrollTo(scrollNode);
             } else if (parent != null && (parent instanceof TreeView)) {
                 if (scrollNode instanceof TreeCell) {
                     ((TreeView) parent).scrollTo(((TreeView) parent)
                             .getRow(((TreeCell) scrollNode).getTreeItem()));
                 }
             }
         }
 
         /**
          * Scrolls the component passed to the constructor to visible.
          *
          */
         public void scrollToVisible() {
             scrollObjectToVisible(m_component);
         }
     }
 
     /**
      * Creates a new instance.
      *
      * @param factory
      *            The Robot factory instance.
      * @throws RobotException
      *             If the AWT-Robot cannot be created.
      */
     public RobotJavaFXImpl(RobotFactoryJavaFXImpl factory) 
         throws RobotException {
         try {
             m_robot = new Robot();
             m_robot.setAutoWaitForIdle(true);
             m_robot.setAutoDelay(0);
         } catch (AWTException awte) {
             log.error(awte);
             m_robot = null;
             throw new RobotException(awte);
         } catch (SecurityException se) {
             log.error(se);
             m_robot = null;
             throw new RobotException(se);
         }
         m_interceptor = factory.getRobotEventInterceptor();
         m_mouseMotionTracker = factory.getMouseMotionTracker();
         m_queuer = factory.getEventThreadQueuer();
     }
 
     /**
      * Gets a location inside the component. If <code>offset</code> is
      * <code>null</code>, it returns the middle of the component otherwise it
      * adds the offset to the upper left corner.
      *
      * @param comp
      *            the component to get the location for
      * @param offset
      *            the offset
      * @throws IllegalArgumentException
      *             if <code>component</code> is null
      * @return the <b>global </b> coordinates of <code>component</code>
      */
     private Point getLocation(Node comp, final Point offset)
         throws IllegalArgumentException {
 
         Validate.notNull(comp, "component must not be null"); //$NON-NLS-1$
 
         Scene s = comp.getScene();
         s.getRoot().layout();
 
         Point2D pos = comp.localToScreen(0, 0);
 
         double x = pos.getX();
         double y = pos.getY();
         if (offset == null) {
             x += comp.getBoundsInParent().getWidth() / 2;
             y += comp.getBoundsInParent().getHeight() / 2;
         } else {
             x += offset.x;
             y += offset.y;
         }
 
         return new Point(Rounding.round(x), Rounding.round(y));
     }
 
     /**
      * Implementation of the mouse click. The mouse is moved into the graphics
      * component by calling <code>moveImpl()</code> before performing the click.
      *
      * @param graphicsComponent
      *            The graphics component to click on
      * @param constraints
      *            The constraints, must be a <code>java.awt.Rectangle</code> or
      *            <code>null</code>. The constraints are <em>relative</em> to
      *            the location/origin of the <code>graphicsComponent</code>.
      * @param clickOptions
      *            The click options
      * @param xPos
      *            xPos in component
      * @param yPos
      *            yPos in component
      * @param yAbsolute
      *            true if y-position should be absolute
      * @param xAbsolute
      *            true if x-position should be absolute
      * @throws RobotException
      *             If the click delay is interrupted or the event confirmation
      *             receives a timeout.
      */
     private void clickImpl(Object graphicsComponent, Object constraints,
             ClickOptions clickOptions, int xPos, boolean xAbsolute, int yPos,
             boolean yAbsolute) throws RobotException {
         moveImpl(graphicsComponent, (Rectangle) constraints, xPos, xAbsolute,
                 yPos, yAbsolute, clickOptions);
         clickImpl(graphicsComponent, clickOptions);
     }
 
     /**
      * Clicks at the current mouse position.
      *
      * @param graphicsComp
      *            The component used for confirming the click.
      * @param clickOp
      *            Configuration for the click.
      */
     private void clickImpl(Object graphicsComp, ClickOptions clickOp) {
 
         int buttonMask = getButtonMask(clickOp.getMouseButton());
         int clickCount = clickOp.getClickCount();
         int[] modifierMask = getModifierMask(clickOp.getClickModifier());
         if (clickCount > 0) {
             IRobotEventConfirmer confirmer = null;
             if (clickOp.isConfirmClick()) {
                 InterceptorOptions options = new InterceptorOptions(
                         new long[] { AWTEvent.MOUSE_EVENT_MASK });
                 confirmer = m_interceptor.intercept(options);
             }
             try {
                 pressModifier(modifierMask);
                 RobotTiming.sleepPreClickDelay();
 
                 for (int i = 0; i < clickCount; i++) {
                     m_robot.mousePress(buttonMask);
                     RobotTiming.sleepPostMouseDownDelay();
 
                     m_robot.mouseRelease(buttonMask);
                     RobotTiming.sleepPostMouseUpDelay();
                 }
                 if (confirmer != null) {
                     confirmer.waitToConfirm(null,
                             new ClickJavaFXEventMatcher(clickOp));
                 }
             } finally {
                 releaseModifier(modifierMask);
             }
         }
     }
 
     /**
      * @param modifierMask
      *            array of modifiers to press before click
      */
     private void pressModifier(int[] modifierMask) {
         for (int i = 0; i < modifierMask.length; i++) {
             keyPress(null, modifierMask[i]);
         }
     }
 
     /**
      * @param modifierMask
      *            array of modifiers release after click
      */
     private void releaseModifier(int[] modifierMask) {
         for (int i = 0; i < modifierMask.length; i++) {
             keyRelease(null, modifierMask[i]);
         }
     }
 
     /**
      * @param clickModifier
      *            the click modifier to use for this click
      * @return an array of modifiers to press before click and release after
      *         click
      */
     private int[] getModifierMask(ClickModifier clickModifier) {
         int[] modifier = new int[0];
         if (clickModifier.hasModifiers(ClickModifier.M1)) {
             modifier = ArrayUtils.add(modifier, 1 << 7);
         }
         if (clickModifier.hasModifiers(ClickModifier.M2)) {
             modifier = ArrayUtils.add(modifier, 1 << 6);
         }
         if (clickModifier.hasModifiers(ClickModifier.M3)) {
             modifier = ArrayUtils.add(modifier, 1 << 9);
         }
         if (clickModifier.hasModifiers(ClickModifier.M4)) {
             modifier = ArrayUtils.add(modifier, 1 << 7);
         }
         return modifier;
     }
 
     /**
      * Checks if the mouse has to be moved on <code>p</code> or if the mouse
      * pointer already resides on this location.
      *
      * @param p
      *            The point to move to
      * @return <code>true</code> if the mouse pointer resides on a different
      *         point, otherwise <code>false</code>.
      */
     private boolean isMouseMoveRequired(Point p) {
         boolean result = true;
         Point point = getCurrentMousePosition();
         if (point != null) {
             result = !point.equals(p);
             if (log.isDebugEnabled()) {
                 log.debug("Last converted screen point  : " + point); //$NON-NLS-1$
                 log.debug("Required screen point        : " + p); //$NON-NLS-1$
                 log.debug("Mouse move required?         : " + result); //$NON-NLS-1$
             }
         }
         return result;
     }
 
     /**
      * Implementation of the mouse move. The mouse is moved into the graphics
      * component.
      *
      * @param graphicsComponent
      *            The component to move to
      * @param constraints
      *            The more specific constraints. Use this, for example when you
      *            want the click point to be relative to a part of the component
      *            (e.g. tree node, table cell, etc) rather than the overall
      *            component itself. May be <code>null</code>.
      * @param xPos
      *            xPos in component
      * @param yPos
      *            yPos in component
      * @param xAbsolute
      *            true if x-position should be absolute
      * @param yAbsolute
      *            true if y-position should be absolute
      * @param clickOptions
      *            The click options
      * @throws StepExecutionException
      *             If the click delay is interrupted or the event confirmation
      *             receives a timeout.
      */
     private void moveImpl(final Object graphicsComponent,
             final Rectangle constraints, final int xPos,
             final boolean xAbsolute, final int yPos, final boolean yAbsolute,
             final ClickOptions clickOptions) throws StepExecutionException {
         Rectangle bounds = getComponentBounds(graphicsComponent, clickOptions);
         if (constraints != null) {
             bounds.x += constraints.x;
             bounds.y += constraints.y;
             bounds.height = constraints.height;
             bounds.width = constraints.width;
         }
         Point p = PointUtil.calculateAwtPointToGo(xPos, xAbsolute, yPos,
                 yAbsolute, bounds);
         // Move if necessary
         if (isMouseMoveRequired(p)) {
             if (log.isDebugEnabled()) {
                 log.debug("Moving mouse to: " + p); //$NON-NLS-1$
             }
             Point startpoint = m_mouseMotionTracker.getLastMousePointOnScreen();
             if (startpoint == null) {
                 // If there is no starting point the center of the root
                 // component is used
                 if (graphicsComponent instanceof Stage) {
                     Stage s = (Stage) graphicsComponent;
                     Node root = s.getScene().getRoot();
                     startpoint = (root != null) ? getLocation(root, null)
                             : new Point(Rounding.round(s.getWidth() / 2),
                                     Rounding.round(s.getHeight() / 2));
                 } else {
                     Node node = (Node) graphicsComponent;
                     Node root = node.getScene().getRoot();
                     Node c = (root != null) ? root : node;
                     startpoint = getLocation(c, null);
                 }
             }
             IRobotEventConfirmer confirmer = null;
             InterceptorOptions options = new InterceptorOptions(
                     new long[] { AWTEvent.MOUSE_MOTION_EVENT_MASK });
             //For drag Events we have to register the confirmer earlier
             //because the drag event is thrown when the movement starts
             if (DragAndDropHelper.getInstance().isDragMode()) {
                 confirmer = m_interceptor.intercept(options); 
             }
             final Point[] mouseMove = MouseMovementStrategy.getMovementPath(
                     startpoint, p, clickOptions.getStepMovement(),
                     clickOptions.getFirstHorizontal());
             Point currP = new Point(0, 0);
             for (int i = 0; i < mouseMove.length - 1; i++) {
                 m_robot.mouseMove(mouseMove[i].x, mouseMove[i].y);
                 currP.x = mouseMove[i].x;
                 currP.y = mouseMove[i].y;
                 if (!currP.equals(MouseInfo.getPointerInfo().getLocation())) {
                     mouseMoveFallback(currP);
                 }
             }
             if (!DragAndDropHelper.getInstance().isDragMode()) {
                 confirmer = m_interceptor.intercept(options); 
             }
             m_robot.mouseMove(mouseMove[mouseMove.length - 1].x,
                     mouseMove[mouseMove.length - 1].y);
             currP.x = mouseMove[mouseMove.length - 1].x;
             currP.y = mouseMove[mouseMove.length - 1].y;
             if (!currP.equals(MouseInfo.getPointerInfo().getLocation())) {
                 mouseMoveFallback(currP);
             }
             if (confirmer != null) {
                 confirmMove(confirmer, graphicsComponent);
             }
         }
     }
 
     /**
      * Checks whether the move was really successful. If not, a few workarounds
      * are attempted in order to correct the situation. The workarounds are
      * logged at the warn-level as they are used. If, after all workarounds have
      * been applied, the mouse pointer is still not at the correct position,
      * then an error is logged.
      *
      * @param pointToGo
      *            The point where the mouse pointer should currently be.
      */
     private void mouseMoveFallback(Point pointToGo) {
         Point curPoint = MouseInfo.getPointerInfo().getLocation();
         while (!(curPoint.equals(pointToGo))) {
             m_robot.delay(1);
             curPoint = MouseInfo.getPointerInfo().getLocation();
         }
     }
 
     /**
      * Confirms a move, either a normal move or a drag move.
      *
      * @param confirmer
      *            the confirmer
      * @param comp
      *            the component to confirm for
      */
     private void confirmMove(IRobotEventConfirmer confirmer, Object comp) {
         if (DragAndDropHelper.getInstance().isDragMode()) {
             confirmer.waitToConfirm(null, new MouseMovedEventMatcher(
                     MouseEvent.MOUSE_DRAGGED));
         } else {
             confirmer.waitToConfirm(null, new MouseMovedEventMatcher(
                     MouseEvent.MOUSE_MOVED));
         }
     }
 
     /**
      * Refreshes the complete layout and returns the bounds of the given
      * Component.
      *
      * @param comp
      *            the Component
      * @param clickOp
      *            not used
      * @return Rectangle with the Bounds
      */
     private Rectangle getComponentBounds(final Object comp,
             ClickOptions clickOp) {
         Rectangle bounds = null;
         if (comp instanceof Stage) {
             Stage s = (Stage) comp;
             bounds = new Rectangle(new Point(Rounding.round(s.getX()),
                     Rounding.round(s.getY())));
 
             // This is not multi display compatible
             Screen screen = Screen.getPrimary();
             int displayWidth = Rounding.round(screen.getBounds().getWidth());
             int displayHeight = Rounding.round(screen.getBounds().getHeight());
             if (s.isFullScreen()) {
                 bounds.width = Rounding.round(displayWidth);
                 bounds.height = Rounding.round(displayHeight);
             } else if (s.isMaximized()) {
                 int x = Rounding.round(s.getX());
                 int y = Rounding.round(s.getY());
                 // trimming the bounds to the display if necessary
                 if (x < 0 || y < 0) {
                     bounds = new Rectangle(new Point(0, 0));
                    bounds.width = Rounding.round(s.getWidth());
                    bounds.height = Rounding.round(s.getHeight());
                     if (bounds.width > displayWidth) {
                         bounds.width = displayWidth;
                     }
                     if (bounds.height > displayHeight) {
                         bounds.height = displayHeight;
                     }
                 }
             } else {
                 bounds.width = Rounding.round(s.getWidth());
                 bounds.height = Rounding.round(s.getHeight());
             }
         } else {
             final Node node = (Node) comp;
             if (clickOp.isScrollToVisible()) {
                 ensureComponentVisible(node);
             }
             bounds = EventThreadQueuerJavaFXImpl.invokeAndWait(
                     "Robot get node bounds", new Callable<Rectangle>() { //$NON-NLS-1$
 
                         @Override
                         public Rectangle call() throws Exception {
                             Rectangle bs = new Rectangle(getLocation(node,
                                     new Point(0, 0)));
                             Bounds b = node.getBoundsInParent();
                             bs.width = Rounding.round(b.getWidth());
                             bs.height = Rounding.round(b.getHeight());
                             return bs;
                         }
                     });
 
         }
         return bounds;
     }
 
     /**
      * {@inheritDoc}
      */
     public void click(Object graphicsComponent, Object constraints)
         throws RobotException {
         click(graphicsComponent, constraints, ClickOptions.create());
     }
 
     /**
      * {@inheritDoc}
      */
     public void click(Object graphicsComponent, Object constraints,
             ClickOptions clickOptions) throws RobotException {
 
         clickImpl(graphicsComponent, constraints, clickOptions, 50, false, 50,
                 false);
     }
 
     /**
      * Gets the InputEvent-ButtonMask of the given mouse button number
      *
      * @param button
      *            the button number
      * @return the InputEvent button mask
      */
     private int getButtonMask(int button) {
         switch (button) {
             case InputConstants.MOUSE_BUTTON_LEFT:
                 return java.awt.event.InputEvent.BUTTON1_MASK;
             case InputConstants.MOUSE_BUTTON_MIDDLE:
                 return java.awt.event.InputEvent.BUTTON2_MASK;
             case InputConstants.MOUSE_BUTTON_RIGHT:
                 return java.awt.event.InputEvent.BUTTON3_MASK;
             default:
                 throw new RobotException("unsupported mouse button", null); //$NON-NLS-1$
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public void clickAtCurrentPosition(Object graphicsComponent,
             int clickCount, int button) {
         ClickOptions clickOptions = new ClickOptions();
         clickOptions.setClickCount(clickCount);
         clickOptions.setMouseButton(button);
         clickImpl(graphicsComponent, clickOptions);
     }
 
     /**
      * {@inheritDoc} java.lang.Object)
      */
     public void doubleClick(Object graphicsComponent, Object constraints)
         throws RobotException {
 
         click(graphicsComponent, constraints, ClickOptions.create()
                 .setClickCount(2));
     }
 
     /**
      * {@inheritDoc}
      *
      */
     public void move(Object graphicsComponent, Object constraints)
         throws RobotException {
 
         moveImpl(graphicsComponent, (Rectangle) constraints, 50, false, 50,
                 false, ClickOptions.create());
     }
 
     /**
      * {@inheritDoc} <br>
      * <b>* Currently delegates the key type to the Robot </b>
      */
     public void type(final Object graphicsComponent, char c) 
         throws RobotException {
         
         Validate.notNull(graphicsComponent,
                 "The graphic component must not be null"); //$NON-NLS-1$
 
         final KeyEvent event = new KeyEvent(
                 KeyEvent.KEY_TYPED, String.valueOf(c), 
                 StringUtils.EMPTY, null, false, false, false, false);
 
         InterceptorOptions options = new InterceptorOptions(
                 new long[] { AWTEvent.KEY_EVENT_MASK });
         IRobotEventConfirmer confirmer = m_interceptor.intercept(options);
 
         m_queuer.invokeLater("Type character", new Runnable() { //$NON-NLS-1$
             @Override
             public void run() {
                 final Scene scene;
                 if (graphicsComponent instanceof Stage) {
                     scene = ((Stage)graphicsComponent).getScene();
                 } else {
                     scene = ((Node)graphicsComponent).getScene();
                 }
                 
                 Node focusOwner = scene.getFocusOwner();
                 EventTarget eventTarget = 
                         focusOwner != null ? focusOwner : scene;
                 
                 Event.fireEvent(eventTarget, event);
             }
         });
         
         confirmer.waitToConfirm(graphicsComponent,
                 new KeyJavaFXEventMatcher(KeyEvent.KEY_TYPED));
 
     }
     
     /**
      * {@inheritDoc}
      */
     public void type(Object graphicsComponent, String text)
         throws RobotException {
         if (text != null) {
             for (int i = 0; i < text.length(); i++) {
                 char ch = text.charAt(i);
                 type(graphicsComponent, ch);
             }
         }
     }
     
     
     /**
      * {@inheritDoc}
      */
     public void keyType(Object graphicsComponent, int keycode) {
         keyType(graphicsComponent, keycode, false);
     }
 
     /**
      * @param graphicsComponent The graphics component the key code is typed in, may be null
      * @param keycode The key code.
      * @param isUpperCase Boolean whether character is upper case.
      */
     public void keyType(Object graphicsComponent, int keycode, 
             boolean isUpperCase)
         throws RobotException {
         try {
             InterceptorOptions options = new InterceptorOptions(
                     new long[] { AWTEvent.KEY_EVENT_MASK });
             IRobotEventConfirmer confirmer = m_interceptor.intercept(options);
             try {
                 if (isUpperCase) {
                     m_robot.keyPress(java.awt.event.KeyEvent.VK_SHIFT);
                 }
                 m_robot.keyPress(keycode);
             } finally {
                 m_robot.keyRelease(keycode);
                 if (isUpperCase) {
                     m_robot.keyRelease(java.awt.event.KeyEvent.VK_SHIFT);
                 }
             }
             confirmer.waitToConfirm(graphicsComponent,
                     new KeyJavaFXEventMatcher(KeyEvent.KEY_RELEASED));
         } catch (IllegalArgumentException e) {
             throw new RobotException(e);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     public String getSystemModifierSpec() {
         String keyStrokeSpec = CompSystemConstants.MODIFIER_CONTROL;
         if (!(UIManager.getLookAndFeel().getID().equals(METAL_LAF_ID))) {
             if (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                     == java.awt.Event.META_MASK) {
                 keyStrokeSpec = CompSystemConstants.MODIFIER_META;
             } else if (Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()
                     == java.awt.Event.ALT_MASK) {
                 keyStrokeSpec = CompSystemConstants.MODIFIER_ALT;
             }
         }
         return keyStrokeSpec;
     }
 
     /**
      * Implements the key press or release.
      *
      * @param graphicsComponent
      *            The component, may be <code>null</code>
      * @param keyCode
      *            The key code
      * @param press
      *            If <code>true</code>, the key is pressed, otherwise released
      */
     private void keyPressReleaseImpl(Object graphicsComponent, int keyCode,
             boolean press) {
 
         InterceptorOptions options = new InterceptorOptions(
                 new long[] { AWTEvent.KEY_EVENT_MASK });
         IRobotEventConfirmer confirmer = m_interceptor.intercept(options);
         if (press) {
             m_robot.keyPress(keyCode);
         } else {
             m_robot.keyRelease(keyCode);
         }
         confirmer.waitToConfirm(graphicsComponent, new KeyJavaFXEventMatcher(
                 press ? KeyEvent.KEY_PRESSED : KeyEvent.KEY_RELEASED));
     }
 
     /**
      * {@inheritDoc}
      */
     public void keyPress(Object graphicsComponent, int keycode)
         throws RobotException {
 
         keyPressReleaseImpl(graphicsComponent, keycode, true);
     }
 
     /**
      * {@inheritDoc}
      */
     public void keyRelease(Object graphicsComponent, int keycode)
         throws RobotException {
 
         keyPressReleaseImpl(graphicsComponent, keycode, false);
     }
 
     /**
      * a method to turn the toggle keys caps-lock, num-lock and scroll-lock on
      * and off. If the given key code is one of these buttons otherwise this is
      * a normal button press.
      *
      * @param obj
      *            Component
      * @param key
      *            to set key Event
      * @param activated
      *            boolean
      */
     public void keyToggle(Object obj, int key, boolean activated) {
         keyPressReleaseImpl(null, key, true);
         keyPressReleaseImpl(null, key, false);
     }
 
     /**
      * {@inheritDoc}
      */
     public void keyStroke(String keyStrokeSpec) throws RobotException {
         try {
             KeyTyper.getInstance().type(keyStrokeSpec, m_interceptor,
                     new KeyJavaFXEventMatcher(KeyEvent.KEY_PRESSED),
                     new KeyJavaFXEventMatcher(KeyEvent.KEY_RELEASED));
         } catch (AWTException e) {
             throw new RobotException(e);
         }
     }
 
     /**
      * Ensures that the passed component is visible.
      *
      * @param component
      *            The component.
      * @throws RobotException
      *             If the component's screen location cannot be calculated.
      */
     private void ensureComponentVisible(final Node component)
         throws RobotException {
         m_queuer.invokeAndWait("ensureVisible", new IRunnable() { //$NON-NLS-1$
             public Object run() {
                 Scroller scroller = new Scroller(component);
                 scroller.scrollToVisible();
                 return null;
             }
         });
     }
 
     /**
      * {@inheritDoc}
      */
     public void scrollToVisible(Object graphicsComponent, Object constraints)
         throws RobotException {
 
         ensureComponentVisible((Node) graphicsComponent);
     }
 
     /**
      * {@inheritDoc}
      */
     public void activateApplication(String method) throws RobotException {
         try {
             final Window window = getActiveWindow();
             if (window == null) {
                 return;
             }
             WindowActivationMethod wam = WindowActivationMethod
                     .createWindowActivationMethod(method, m_robot, m_queuer);
             wam.activate(window);
 
             // Verify that window was successfully activated
             Window activeWindow = (Window) m_queuer.invokeAndWait(
                     "getActiveWindow", //$NON-NLS-1$
                     new IRunnable() {
                         public Object run() throws StepExecutionException {
 
                             if (window.isFocused()) {
                                 return window;
                             }
                             return null;
                         }
                     });
             if (activeWindow != window) {
                 throw new StepExecutionException(
                         I18n.getString(TestErrorEvent.WINDOW_ACTIVATION_FAILED,
                                 true),
                         EventFactory
                                 .createActionError(
                                         TestErrorEvent.
                                         WINDOW_ACTIVATION_FAILED));
             }
 
         } catch (Exception exc) {
             throw new RobotException(exc);
         }
     }
 
     /**
      * @return The current mouse position as a Point {@inheritDoc}
      */
     public Point getCurrentMousePosition() {
         return MouseInfo.getPointerInfo().getLocation();
     }
 
     /**
      * Guesses the active window. Returns null if no active window is found.
      *
      * @return the active window
      */
     private Window getActiveWindow() {
         return (Window) m_queuer.invokeAndWait("getActiveWindow", //$NON-NLS-1$
                 new IRunnable() {
                     public Object run() throws StepExecutionException {
                         Window w = CurrentStages.getfocusStage();
                         if (w == null) {
                             w = CurrentStages.getfirstStage();
                             ((Stage) w).toFront();
                         }
                         return w;
                     }
                 });
     }
 
     /**
      *
      * {@inheritDoc}
      */
     public boolean isMouseInComponent(final Object graphicsComponent) {
         final Point currMousePos = getCurrentMousePosition();
         return EventThreadQueuerJavaFXImpl.invokeAndWait("isMouseInComponent", //$NON-NLS-1$
                 new Callable<Boolean>() {
 
                     @Override
                     public Boolean call() throws Exception {
                         if (graphicsComponent instanceof Node) {
                             Node comp = (Node) graphicsComponent;
                             comp.getScene().getRoot().layout();
 
                             if (currMousePos == null) {
                                 return false;
                             }
                             return NodeBounds.checkIfContains(new Point2D(
                                     currMousePos.x, currMousePos.y), comp);
                         }
                         Stage comp = (Stage) graphicsComponent;
                         comp.getScene().getRoot().layout();
                         Bounds stageBounds = new BoundingBox(comp.getX(),
                                 comp.getY(), comp.getWidth(), comp
                                         .getHeight());
                         return stageBounds.contains(new Point2D(
                                 currMousePos.x, currMousePos.y));
                     }
                 });
 
     }
 
     /**
      * Presses the given mouse button on the given component in the given
      * constraints. <br>
      * <b>Note:</b> Use only for Drag and Drop! To click with the mouse, use
      * click-methods!
      *
      * @param graphicsComponent
      *            the component where to press the mouse button. If null, the
      *            mouse is pressed at the current location.
      * @param constraints
      *            A constraints object used by the Robot implementation, may be
      *            <code>null</code>.
      * @param button
      *            the mouse button which is to be pressed.
      */
     public void mousePress(Object graphicsComponent, Object constraints,
             int button) {
         DragAndDropHelper.getInstance().setDragMode(true);
         if (graphicsComponent != null) {
             move(graphicsComponent, constraints);
         }
 
         RobotTiming.sleepPreClickDelay();
 
         m_robot.mousePress(getButtonMask(button));
     }
 
     /**
      * Releases the given mouse button on the given component in the given
      * constraints. <br>
      * <b>Note:</b> Use only for Drag and Drop! To click with the mouse, use
      * click-methods!
      *
      * @param graphicsComponent
      *            The graphics component. If null, the mouse button is released
      *            at the current location.
      * @param constraints
      *            A constraints object used by the Robot implementation, may be
      *            <code>null</code>.
      * @param button
      *            the mouse button.
      */
     public void mouseRelease(Object graphicsComponent, Object constraints,
             int button) throws RobotException {
         if (graphicsComponent != null) {
             move(graphicsComponent, constraints);
         }
         RobotTiming.sleepPreClickDelay();
         m_robot.mouseRelease(getButtonMask(button));
         DragAndDropHelper.getInstance().setDragMode(false);
     }
 
     /**
      * {@inheritDoc}
      */
     public void click(Object graphicsComponent, Object constraints,
             ClickOptions clickOptions, int xPos, boolean xAbsolute, int yPos,
             boolean yAbsolute) throws RobotException {
 
         clickImpl(graphicsComponent, constraints, clickOptions, xPos,
                 xAbsolute, yPos, yAbsolute);
     }
 
     /**
      * {@inheritDoc}
      */
     public String getPropertyValue(Object graphicsComp, String propertyName)
         throws RobotException {
         String propertyValue = StringConstants.EMPTY;
         Validate.notNull(graphicsComp, "Tested component must not be null"); //$NON-NLS-1$
         try {
             final Object prop = PropertyUtils.getProperty(graphicsComp,
                     propertyName);
             propertyValue = String.valueOf(prop);
         } catch (IllegalAccessException e) {
             throw new RobotException(e);
         } catch (InvocationTargetException e) {
             throw new RobotException(e);
         } catch (NoSuchMethodException e) {
             throw new RobotException(e);
         }
 
         return propertyValue;
     }
 
     /** {@inheritDoc} */
     public BufferedImage createFullScreenCapture() {
         return LocalScreenshotUtil.createFullScreenCapture();
     }
 
     /**
      * Return the currently used EventInterceptor
      * @return the Interceptor
      */
     public RobotEventInterceptorJavaFXImpl getInterceptor() {
         return m_interceptor;
     }
 }
