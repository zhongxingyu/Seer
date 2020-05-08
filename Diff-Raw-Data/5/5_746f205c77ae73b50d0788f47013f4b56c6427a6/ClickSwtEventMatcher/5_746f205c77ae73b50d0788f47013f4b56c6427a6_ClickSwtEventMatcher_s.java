 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.rc.swt.driver;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.jubula.rc.common.driver.ClickOptions;
 import org.eclipse.jubula.rc.common.driver.IRunnable;
 import org.eclipse.jubula.rc.common.exception.StepExecutionException;
 import org.eclipse.jubula.rc.common.logger.AutServerLogger;
 import org.eclipse.jubula.rc.swt.listener.ComponentHandler;
 import org.eclipse.jubula.rc.swt.utils.SwtUtils;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.MenuItem;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Tree;
 import org.eclipse.swt.widgets.Widget;
 
 
 /**
  * This event matcher checks wether a mouse click event matches the
  * requested properties. The properties are defined by a
  * <code>ClickOptions</code> instance.
  *
  * @author BREDEX GmbH
  * @created 26.07.2006
  */
 public class ClickSwtEventMatcher extends DefaultSwtEventMatcher {
 
     /** the log */
     private static final AutServerLogger LOG = 
         new AutServerLogger(ClickSwtEventMatcher.class);
     
     /**
      * The click options.
      */
     private ClickOptions m_clickOptions;
        
     /**
      * Creates a new matcher which checks SWT events against a mouse event type
      * that is determined from the given ClickOptions.
      * @param clickOptions the ClickOptions
      */
     public ClickSwtEventMatcher(ClickOptions clickOptions) {
         super(getMouseEventId(clickOptions));
         m_clickOptions = clickOptions;
     }
     
     /**
      * Converts the click type to the corresponding AWT event ID. 
      * @param clickOptions The click options.
      * @return The event ID.
      */
     private static int getMouseEventId(ClickOptions clickOptions) {
         return (clickOptions.getClickType()
                 == ClickOptions.ClickType.CLICKED) ? SWT.MouseDown
                     : SWT.MouseUp;
     }
 
     /**
      * {@inheritDoc}
      */
     public boolean isFallBackEventMatching(List eventObjects, 
         final Object graphicsComponent) {
             
         // Checks whether we've received a mouse up event. This really only
         // makes a difference in the case that we're listening for a mouse down
         // event that never comes rather than the mouse up event that does.
         // This can happen, for example, when selecting from a TabFolder under 
         // Linux. It may also happen in other situations.
         Iterator mouseUpIt = eventObjects.iterator();
         while (mouseUpIt.hasNext()) {
             Event event = (Event)mouseUpIt.next();
             if (event.widget == graphicsComponent
                 && event.type == SWT.MouseUp) {
                 
                 return true;
             }
         }
 
         if (isEventlessWidget(graphicsComponent)) {
             return true;
         }
         
         try {
             // checks if the component received a mouse-down event from the 
             // right-hand mouse button (open context menu)
             Iterator eventIt = eventObjects.iterator();
             while (eventIt.hasNext()) {
                 Event event = (Event)eventIt.next();
                 if (event.widget == graphicsComponent
                     && event.button == 3 // right-click
                     && event.type == SWT.MouseDown) {
                     
                     return true;
                 }
             }
             // checks if the component is visible (= in hierarchy conatiner)
             // and if the key-released event occured
             if ((ComponentHandler.getAutHierarchy().getHierarchyContainer(
                     (Widget)graphicsComponent) == null)
                 && (m_clickOptions.getClickType() == ClickOptions.ClickType
                         .RELEASED || m_clickOptions.getClickCount() == 0)) {
                     
                 return true;
             
             }
         } catch (IllegalArgumentException e) {
             if (m_clickOptions.getClickType() == ClickOptions.ClickType.RELEASED
                     || m_clickOptions.getClickCount() == 0) {
                     
                 return true;
             }
         }
         
         // Check whether the mouse pointer is currently on the very edge of the component
         // Some components (such as TabFolder) do not send mouse events when a click occurs
         // on its "border".
         if (graphicsComponent instanceof Widget
                 && !((Widget)graphicsComponent).isDisposed()
                 && isOnBorder((Widget)graphicsComponent)) {
             return true;
         }
         
         return false;
     }
 
     /**
      * 
      * @param graphicsComponent The component to check.
      * @return <code>true</code> if we cannot expect to receive mouse events 
      *         for given component but can reasonably assume that a mouse 
      *         click did indeed occur. Otherwise, <code>false</code>.
      */
     private boolean isEventlessWidget(Object graphicsComponent) {
         // unfortunately we get no events from the MenuBar, 
         // so we have nothing to check!
         if (graphicsComponent instanceof MenuItem)  {
             
             return true;
         // Similar situation with ToolItem. We receive no MouseUp event from
         // a mouse click on the ToolItem's chevron.
         } else if (graphicsComponent instanceof ToolItem) {
             return true;
         } else if (graphicsComponent instanceof Combo) {
             // FIXME zeb Must be some way to check if a menu open/close event occurred
             return true;
         // We receive no Mouse events on Table and Tree(Table) headers.
         // This will supposedly be fixed for SWT 3.4. 
         // See https://bugs.eclipse.org/bugs/show_bug.cgi?id=17871
        } else if ((graphicsComponent instanceof Table
            || graphicsComponent instanceof Tree)
             && SwtUtils.isMouseCursorInWidget((Widget)graphicsComponent)) {
             // Assuming that if a Table or Tree was the target component and 
             // the mouse pointer is currently within the bounds for that Table, 
             // then that is enough confirmation.
             return true;
         }
         
         return false;
     }
     
     /**
      * @param graphicsComponent The component to check.
      * @return <code>true</code> if the mouse cursor is currently directly on
      *         the very edge of the given component. 
      *         Otherwise, <code>false</code>.
      */
     private boolean isOnBorder(final Widget graphicsComponent) {
         try {
             Boolean isOnBorder = (Boolean)new EventThreadQueuerSwtImpl().invokeAndWait("CheckBorderFallbackMatching", new IRunnable() { //$NON-NLS-1$
                 public Object run() throws StepExecutionException {
                     int fuzz = 3;
                     Display d = graphicsComponent.getDisplay();
                     Rectangle widgetBounds = 
                         SwtUtils.getWidgetBounds(graphicsComponent);
                     Point cursorLocation = d.getCursorLocation();
                     if (widgetBounds.contains(cursorLocation)) {
                         widgetBounds.x += fuzz;
                         widgetBounds.y += fuzz;
                         widgetBounds.width -= (fuzz * 2);
                         widgetBounds.height -= (fuzz * 2);
     
                         if (!widgetBounds.contains(cursorLocation)) {
                             return Boolean.TRUE;
                         }
                     }
                     
                     return Boolean.FALSE;
                 }
             });
             return isOnBorder.booleanValue();
         } catch (Throwable t) {
             // Since this method is a workaround, it would be unacceptable to
             // propogate any errors here. Instead, we'll log the problem and
             // assume that the workaround conditions were not met.
             LOG.warn("An error occurred during an event-confirmation workaround. The results of the workaround will be ignored.", t); //$NON-NLS-1$
             return false;
         }
     }
 }
