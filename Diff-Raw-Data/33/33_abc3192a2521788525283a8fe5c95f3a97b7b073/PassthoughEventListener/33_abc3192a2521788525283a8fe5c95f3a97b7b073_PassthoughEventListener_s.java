 /**
  *   Copyright 2011 Karl Martens
  *
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *       http://www.apache.org/licenses/LICENSE-2.0
  *       
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  *
  *   net.karlmartens.ui, is a library of UI widgets
  */
 package net.karlmartens.ui.widget;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 
 final class PassthoughEventListener {
 
   private final Control _target;
   private final Collection<Control> _sources = new ArrayList<Control>();
   private final ListenerImpl _listener = new ListenerImpl();
 
   PassthoughEventListener(Control target) {
     _target = target;
     _target.addDisposeListener(_listener);
   }
 
   void addSource(Control source) {
     _sources.add(source);
     hookControl(source);
   }
 
   void removeSource(Control source) {
     _sources.remove(source);
     releaseControl(source);
   }
 
   private void hookControl(Control control) {
     control.addDisposeListener(_listener);
 
     final int[] events = new int[] { SWT.KeyDown, SWT.KeyUp, SWT.Paint,
         SWT.MouseDoubleClick, SWT.MouseDown, SWT.MouseUp, SWT.MouseMove,
         SWT.MouseEnter, SWT.MouseExit, SWT.MouseHorizontalWheel,
        SWT.MouseWheel, SWT.MouseVerticalWheel, SWT.MouseHover, SWT.MouseWheel };
     for (int event : events)
       control.addListener(event, _listener);
   }
 
   private void releaseControl(Control control) {
     control.removeDisposeListener(_listener);
 
     final int[] events = new int[] { SWT.KeyDown, SWT.KeyUp, SWT.Paint,
         SWT.MouseDoubleClick, SWT.MouseDown, SWT.MouseUp, SWT.MouseMove,
         SWT.MouseEnter, SWT.MouseExit, SWT.MouseHorizontalWheel,
        SWT.MouseWheel, SWT.MouseVerticalWheel, SWT.MouseHover, SWT.MouseWheel };
     for (int event : events)
       control.removeListener(event, _listener);
   }
 
   private class ListenerImpl implements Listener, DisposeListener {
 
     @Override
     public void widgetDisposed(DisposeEvent e) {
       if (e.getSource() == _target) {
         for (Control source : _sources) {
           releaseControl(source);
         }
         _sources.clear();
       } else {
         removeSource((Control) e.getSource());
       }
     }
 
     @Override
     public void handleEvent(Event event) {
       final Event e = new Event();
       e.button = event.button;
       e.character = event.character;
       e.count = event.count;
       e.data = event.data;
       e.detail = event.detail;
       e.display = event.display;
       e.doit = event.doit;
       e.end = event.end;
       e.gc = event.gc;
       e.height = event.height;
       e.index = event.index;
       e.item = event.item;
       e.keyCode = event.keyCode;
       e.keyLocation = event.keyLocation;
       e.magnification = event.magnification;
       e.rotation = event.rotation;
       e.start = event.start;
       e.stateMask = event.stateMask;
       e.text = event.text;
       e.touches = event.touches;
       e.type = event.type;
       e.widget = _target;
       e.width = event.width;
       e.x = event.x;
       e.xDirection = event.xDirection;
       e.y = event.y;
       e.yDirection = event.yDirection;
 
       updateCords(e.widget, e);
 
       _target.notifyListeners(e.type, e);
     }
 
     private void updateCords(Object source, Event event) {
       if (source instanceof Control) {
         final Control c = (Control) source;
         final Point pt = _target.toControl(c.toDisplay(event.x, event.y));
         event.x = pt.x;
         event.y = pt.y;
       }
     }
   }
 }
