 /*
  * Copyright 2000,2005 wingS development team.
  *
  * This file is part of wingS (http://wingsframework.org).
  *
  * wingS is free software; you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License
  * as published by the Free Software Foundation; either version 2.1
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
 package org.wings;
 
 import org.wings.event.SInternalFrameEvent;
 import org.wings.event.SInternalFrameListener;
 import org.wings.plaf.InternalFrameCG;
 import org.wings.style.Selector;
 
 /**
  * A root container repesenting an iconifyable and minimizable internal window. 
  *
  * @author <a href="mailto:engels@mercatis.de">Holger Engels</a>
  */
 public class SInternalFrame
         extends SRootContainer
         implements LowLevelEventListener
 {
     /**
      * A Pseudo selector addressing the container area of this container.
      * Refer to {@link SComponent#setAttribute(org.wings.style.Selector, org.wings.style.CSSProperty, String)}
      */
     public static final Selector SELECTOR_CONTENT = new Selector("content area");
     /**
      * A Pseudo selector addressing the title bar
      * Refer to {@link SComponent#setAttribute(org.wings.style.Selector, org.wings.style.CSSProperty, String)}
      */
     public static final Selector SELECTOR_TITLE = new Selector("title bar");
 
     private boolean iconifyable = true;
     private boolean maximizable = true;
     private boolean closable = true;
 
     private boolean iconified = false;
     private boolean maximized = false;
     private boolean closed = false;
 
     protected SIcon icon = null;
 
     protected String title = null;
 
     public SInternalFrame() {
         super();
     }
 
     public void setIconifyable(boolean v) {
         iconifyable = v;
         reloadIfChange(iconifyable, v);
     }
 
     public boolean isIconifyable() { return iconifyable; }
 
     public void setMaximizable(boolean v) {
         maximizable = v;
         reloadIfChange(maximizable, v);
     }
 
     public boolean isMaximizable() { return maximizable; }
 
     public void setClosable(boolean v) {
         closable = v;
         reloadIfChange(closable, v);
     }
 
     public boolean isClosable() { return closable; }
 
     public void setIconified(boolean v) {
         v &= isIconifyable();
         boolean old = iconified;
         iconified = v;
         if (old != iconified) {
             reload();
             if (iconified)
                 setMaximized(false);
         }
     }
 
     public boolean isIconified() { return iconified; }
 
     public void setMaximized(boolean v) {
         v &= isMaximizable();
         boolean old = maximized;
         maximized = v;
         if (old != maximized) {
             reload();
             if (maximized)
                 setIconified(false);
         }
     }
 
     public boolean isMaximized() { return maximized; }
 
     public void setClosed(boolean v) {
         v &= isClosable();
         boolean old = closed;
         closed = v;
         if (old != closed)
             reload();
     }
 
     public boolean isClosed() { return closed; }
 
     public void setIcon(SIcon i) {
         if (i != icon || i != null && !i.equals(icon)) {
             icon = i;
             reload();
         }
     }
 
     public SIcon getIcon() {
         return icon;
     }
 
     public void setTitle(String t) {
         String oldTitle = title;
         title = t;
         if ((title == null && oldTitle != null) ||
                 (title != null && !title.equals(oldTitle)))
             reload();
     }
 
     public String getTitle() {
         return title;
     }
 
     public void dispose() {
         SDesktopPane desktop = (SDesktopPane) getParent();
         desktop.remove(this);
     }
 
     public void setVisible(boolean visible) {
         if (visible)
             show();
         else
             hide();
     }
 
     public void show() {
         super.setVisible(true);
         if (isIconified()) {
             setIconified(false);
         }
         if (isClosed()) {
             setClosed(false);
         }
     }
 
     public void hide() {
         super.setVisible(false);
     }
 
     public boolean isShowingChildren() {
         return !iconified;
     }
 
     public void addInternalFrameListener(SInternalFrameListener listener) {
         addEventListener(SInternalFrameListener.class, listener);
     }
 
     public void removeInternalFrameListener(SInternalFrameListener listener) {
         removeEventListener(SInternalFrameListener.class, listener);
     }
 
     private SInternalFrameEvent event;
 
     // LowLevelEventListener interface. Handle own events.
     public void processLowLevelEvent(String action, String[] values) {
         processKeyEvents(values);
         if (action.endsWith("_keystroke"))
             return;
 
         switch (new Integer(values[0]).intValue()) {
             case SInternalFrameEvent.INTERNAL_FRAME_CLOSED:
                 setClosed(true);
                 break;
 
             case SInternalFrameEvent.INTERNAL_FRAME_ICONIFIED:
                 setIconified(true);
                 break;
 
             case SInternalFrameEvent.INTERNAL_FRAME_DEICONIFIED:
                 setIconified(false);
                 break;
 
             case SInternalFrameEvent.INTERNAL_FRAME_MAXIMIZED:
                 setMaximized(true);
                 break;
 
             case SInternalFrameEvent.INTERNAL_FRAME_UNMAXIMIZED:
                 setMaximized(false);
                 break;
 
             default:
                 throw new RuntimeException("unknown id: " + values[0]);
         }
 
         event = new SInternalFrameEvent(this, new Integer(values[0]).intValue());
         SForm.addArmedComponent(this); // trigger later invocation of fire*()
     }
 
     public void fireIntermediateEvents() {}
 
     public void fireFinalEvents() {
         super.fireFinalEvents();
         // Guaranteed to return a non-null array
         Object[] listeners = getListenerList();
         // Process the listeners last to first, notifying
         // those that are interested in this event
         for (int i = listeners.length - 2; i >= 0; i -= 2) {
             if (listeners[i] == SInternalFrameListener.class) {
                 SInternalFrameListener listener;
                 listener = (SInternalFrameListener) listeners[i + 1];
                 switch (event.getID()) {
                     case SInternalFrameEvent.INTERNAL_FRAME_CLOSED:
                         listener.internalFrameClosed(event);
                         break;
 
                     case SInternalFrameEvent.INTERNAL_FRAME_ICONIFIED:
                         listener.internalFrameIconified(event);
                         break;
 
                     case SInternalFrameEvent.INTERNAL_FRAME_DEICONIFIED:
                         listener.internalFrameDeiconified(event);
                         break;
 
                     case SInternalFrameEvent.INTERNAL_FRAME_MAXIMIZED:
                         listener.internalFrameMaximized(event);
                         break;
 
                     case SInternalFrameEvent.INTERNAL_FRAME_UNMAXIMIZED:
                         listener.internalFrameUnmaximized(event);
                         break;
                 }
             }
         }
 
         event = null;
     }
 
     public boolean isEpochCheckEnabled() {
         return true;
     }
 
     public void setCG(InternalFrameCG cg) {
         super.setCG(cg);
     }
 }
