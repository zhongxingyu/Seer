 package de.objectcode.time4u.client.ui.controls;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.accessibility.ACC;
 import org.eclipse.swt.accessibility.AccessibleAdapter;
 import org.eclipse.swt.accessibility.AccessibleControlAdapter;
 import org.eclipse.swt.accessibility.AccessibleControlEvent;
 import org.eclipse.swt.accessibility.AccessibleEvent;
 import org.eclipse.swt.accessibility.AccessibleTextAdapter;
 import org.eclipse.swt.accessibility.AccessibleTextEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.GC;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.TypedListener;
 
 import de.objectcode.time4u.client.ui.util.TimeFormat;
 
 public class TimeCombo extends Composite
 {
   Text text;
   Button arrow;
   Shell popup;
   TimeChooser m_timeChooser;
   boolean hasFocus;
   Listener listener, filter;
   Color foreground, background;
   Font font;
 
   public TimeCombo(final Composite parent, int style)
   {
     super(parent, style = checkStyle(style));
 
     int textStyle = SWT.SINGLE;
     if ((style & SWT.READ_ONLY) != 0) {
       textStyle |= SWT.READ_ONLY;
     }
     if ((style & SWT.FLAT) != 0) {
       textStyle |= SWT.FLAT;
     }
     text = new Text(this, textStyle);
     int arrowStyle = SWT.ARROW | SWT.DOWN;
     if ((style & SWT.FLAT) != 0) {
       arrowStyle |= SWT.FLAT;
     }
     arrow = new Button(this, arrowStyle);
 
     listener = new Listener() {
       public void handleEvent(final Event event)
       {
         if (popup == event.widget) {
           popupEvent(event);
           return;
         }
         if (text == event.widget) {
           textEvent(event);
           return;
         }
         if (m_timeChooser == event.widget) {
           timeChooserEvent(event);
           return;
         }
         if (arrow == event.widget) {
           arrowEvent(event);
           return;
         }
         if (TimeCombo.this == event.widget) {
           comboEvent(event);
           return;
         }
         if (getShell() == event.widget) {
           handleFocus(SWT.FocusOut);
         }
       }
     };
     filter = new Listener() {
       public void handleEvent(final Event event)
       {
         final Shell shell = ((Control) event.widget).getShell();
         if (shell == TimeCombo.this.getShell()) {
           handleFocus(SWT.FocusOut);
         }
       }
     };
 
     final int[] comboEvents = { SWT.Dispose, SWT.Move, SWT.Resize };
     for (int i = 0; i < comboEvents.length; i++) {
       this.addListener(comboEvents[i], listener);
     }
 
     final int[] textEvents = { SWT.KeyDown, SWT.KeyUp, SWT.MenuDetect, SWT.Modify, SWT.MouseDown, SWT.MouseUp,
         SWT.Traverse, SWT.FocusIn, SWT.FocusOut };
     for (int i = 0; i < textEvents.length; i++) {
       text.addListener(textEvents[i], listener);
     }
 
     final int[] arrowEvents = { SWT.Selection, SWT.FocusIn };
     for (int i = 0; i < arrowEvents.length; i++) {
       arrow.addListener(arrowEvents[i], listener);
     }
 
     createPopup(-1);
     initAccessible();
   }
 
   public void addSelectionListener(final SelectionListener listener)
   {
     checkWidget();
     if (listener == null) {
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
     }
     final TypedListener typedListener = new TypedListener(listener);
     addListener(SWT.Selection, typedListener);
     addListener(SWT.DefaultSelection, typedListener);
   }
 
   public void removeSelectionListener(final SelectionListener listener)
   {
     checkWidget();
     if (listener == null) {
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
     }
     removeListener(SWT.Selection, listener);
     removeListener(SWT.DefaultSelection, listener);
   }
 
   /*
    * Return the lowercase of the first non-'&' character following an '&' character in the given string. If there are no
    * '&' characters in the given string, return '\0'.
    */
   char _findMnemonic(final String string)
   {
     if (string == null) {
       return '\0';
     }
     int index = 0;
     final int length = string.length();
     do {
       while (index < length && string.charAt(index) != '&') {
         index++;
       }
       if (++index >= length) {
         return '\0';
       }
       if (string.charAt(index) != '&') {
         return Character.toLowerCase(string.charAt(index));
       }
       index++;
     } while (index < length);
     return '\0';
   }
 
   void arrowEvent(final Event event)
   {
     switch (event.type) {
       case SWT.FocusIn: {
         handleFocus(SWT.FocusIn);
         break;
       }
       case SWT.Selection: {
         dropDown(!isDropped());
         break;
       }
     }
   }
 
   void comboEvent(final Event event)
   {
     switch (event.type) {
       case SWT.Dispose:
         if (popup != null && !popup.isDisposed()) {
           m_timeChooser.removeListener(SWT.Dispose, listener);
           popup.dispose();
         }
         final Shell shell = getShell();
         shell.removeListener(SWT.Deactivate, listener);
         final Display display = getDisplay();
         display.removeFilter(SWT.FocusIn, filter);
         popup = null;
         text = null;
         m_timeChooser = null;
         arrow = null;
         break;
       case SWT.Move:
         dropDown(false);
         break;
       case SWT.Resize:
         internalLayout(false);
         break;
     }
   }
 
   @Override
   public Point computeSize(final int wHint, final int hHint, final boolean changed)
   {
     checkWidget();
     int width = 0, height = 0;
     int textWidth = 0;
     final GC gc = new GC(text);
     final int spacer = gc.stringExtent(" ").x; //$NON-NLS-1$
     textWidth = gc.stringExtent("22:22").x;
 
     gc.dispose();
     final Point textSize = text.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
     final Point arrowSize = arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
     final int borderWidth = getBorderWidth();
 
     height = Math.max(hHint, Math.max(textSize.y, arrowSize.y) + 2 * borderWidth);
     width = Math.max(wHint, textWidth + 2 * spacer + arrowSize.x + 2 * borderWidth);
     return new Point(width, height);
   }
 
   void createPopup(final int selection)
   {
     // create shell and list
     popup = new Shell(getShell(), SWT.NO_TRIM | SWT.ON_TOP);
     final int style = getStyle();
     int listStyle = SWT.SINGLE;
     if ((style & SWT.FLAT) != 0) {
       listStyle |= SWT.FLAT;
     }
     if ((style & SWT.RIGHT_TO_LEFT) != 0) {
       listStyle |= SWT.RIGHT_TO_LEFT;
     }
     if ((style & SWT.LEFT_TO_RIGHT) != 0) {
       listStyle |= SWT.LEFT_TO_RIGHT;
     }
     m_timeChooser = new TimeChooser(popup, listStyle);
     if (font != null) {
       m_timeChooser.setFont(font);
     }
     if (foreground != null) {
       m_timeChooser.setForeground(foreground);
     }
     if (background != null) {
       m_timeChooser.setBackground(background);
     }
 
     final int[] popupEvents = { SWT.Close, SWT.Paint, SWT.Deactivate };
     for (int i = 0; i < popupEvents.length; i++) {
       popup.addListener(popupEvents[i], listener);
     }
     final int[] listEvents = { SWT.MouseUp, SWT.Selection, SWT.Traverse, SWT.KeyDown, SWT.KeyUp, SWT.FocusIn,
         SWT.Dispose };
     for (int i = 0; i < listEvents.length; i++) {
       m_timeChooser.addListener(listEvents[i], listener);
     }
 
     if (selection != -1) {
       m_timeChooser.select(selection);
     }
   }
 
   void dropDown(final boolean drop)
   {
     if (drop == isDropped()) {
       return;
     }
     if (!drop) {
       popup.setVisible(false);
       if (!isDisposed() && arrow.isFocusControl()) {
         text.setFocus();
       }
       return;
     }
 
     if (getShell() != popup.getParent()) {
       final int selection = m_timeChooser.getSelection();
       m_timeChooser.removeListener(SWT.Dispose, listener);
       popup.dispose();
       popup = null;
       m_timeChooser = null;
       createPopup(selection);
     }
 
     final Point size = getSize();
     final Point listSize = m_timeChooser.computeSize(SWT.DEFAULT, SWT.DEFAULT, false);
     m_timeChooser.setBounds(1, 1, Math.max(size.x - 2, listSize.x), listSize.y);
 
     final Display display = getDisplay();
     final Rectangle listRect = m_timeChooser.getBounds();
     final Rectangle parentRect = display.map(getParent(), null, getBounds());
     final Point comboSize = getSize();
     final Rectangle displayRect = getMonitor().getClientArea();
     final int width = Math.max(comboSize.x, listRect.width + 2);
     final int height = listRect.height + 2;
     final int x = parentRect.x;
     int y = parentRect.y + comboSize.y;
     if (y + height > displayRect.y + displayRect.height) {
       y = parentRect.y - height;
     }
     popup.setBounds(x, y, width, height);
     popup.setVisible(true);
     m_timeChooser.setFocus();
   }
 
   /*
    * Return the Label immediately preceding the receiver in the z-order, or null if none.
    */
   Label getAssociatedLabel()
   {
     final Control[] siblings = getParent().getChildren();
     for (int i = 0; i < siblings.length; i++) {
       if (siblings[i] == this) {
         if (i > 0 && siblings[i - 1] instanceof Label) {
           return (Label) siblings[i - 1];
         }
       }
     }
     return null;
   }
 
   public boolean getEditable()
   {
     checkWidget();
     return text.getEditable();
   }
 
   public int getSelection()
   {
     checkWidget();
     return m_timeChooser.getSelection();
   }
 
   /**
    * Returns a string containing a copy of the contents of the receiver's text field.
    * 
    * @return the receiver's text
    * 
    * @exception SWTException
    *              <ul>
    *              <li>ERROR_WIDGET_DISPOSED - if the receiver has been disposed</li>
    *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the thread that created the receiver</li>
    *              </ul>
    */
   public String getText()
   {
     checkWidget();
     return text.getText();
   }
 
   void handleFocus(final int type)
   {
     if (isDisposed()) {
       return;
     }
     switch (type) {
       case SWT.FocusIn: {
         if (hasFocus) {
           return;
         }
         if (getEditable()) {
           text.selectAll();
         }
         hasFocus = true;
         final Shell shell = getShell();
         shell.removeListener(SWT.Deactivate, listener);
         shell.addListener(SWT.Deactivate, listener);
         final Display display = getDisplay();
         display.removeFilter(SWT.FocusIn, filter);
         display.addFilter(SWT.FocusIn, filter);
         final Event e = new Event();
         notifyListeners(SWT.FocusIn, e);
         break;
       }
       case SWT.FocusOut: {
         if (!hasFocus) {
           return;
         }
         final Control focusControl = getDisplay().getFocusControl();
         if (focusControl == arrow || focusControl == m_timeChooser || focusControl == text) {
           return;
         }
         hasFocus = false;
         final Shell shell = getShell();
         shell.removeListener(SWT.Deactivate, listener);
         final Display display = getDisplay();
         display.removeFilter(SWT.FocusIn, filter);
         final Event e = new Event();
         notifyListeners(SWT.FocusOut, e);
         break;
       }
     }
   }
 
   void initAccessible()
   {
     final AccessibleAdapter accessibleAdapter = new AccessibleAdapter() {
       @Override
       public void getHelp(final AccessibleEvent e)
       {
         e.result = getToolTipText();
       }
 
       @Override
       public void getKeyboardShortcut(final AccessibleEvent e)
       {
         String shortcut = null;
         final Label label = getAssociatedLabel();
         if (label != null) {
           final String text = label.getText();
           if (text != null) {
             final char mnemonic = _findMnemonic(text);
             if (mnemonic != '\0') {
               shortcut = "Alt+" + mnemonic; //$NON-NLS-1$
             }
           }
         }
         e.result = shortcut;
       }
 
       @Override
       public void getName(final AccessibleEvent e)
       {
         String name = null;
         final Label label = getAssociatedLabel();
         if (label != null) {
           name = stripMnemonic(label.getText());
         }
         e.result = name;
       }
     };
     getAccessible().addAccessibleListener(accessibleAdapter);
     text.getAccessible().addAccessibleListener(accessibleAdapter);
     m_timeChooser.getAccessible().addAccessibleListener(accessibleAdapter);
 
     arrow.getAccessible().addAccessibleListener(new AccessibleAdapter() {
       @Override
       public void getHelp(final AccessibleEvent e)
       {
         e.result = getToolTipText();
       }
 
       @Override
       public void getKeyboardShortcut(final AccessibleEvent e)
       {
         e.result = "Alt+Down Arrow"; //$NON-NLS-1$
       }
 
       @Override
       public void getName(final AccessibleEvent e)
       {
         e.result = isDropped() ? SWT.getMessage("SWT_Close") : SWT.getMessage("SWT_Open"); //$NON-NLS-1$ //$NON-NLS-2$
       }
     });
 
     getAccessible().addAccessibleTextListener(new AccessibleTextAdapter() {
       @Override
       public void getCaretOffset(final AccessibleTextEvent e)
       {
         e.offset = text.getCaretPosition();
       }
     });
 
     getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
       @Override
       public void getChildAtPoint(final AccessibleControlEvent e)
       {
         final Point testPoint = toControl(e.x, e.y);
         if (getBounds().contains(testPoint)) {
           e.childID = ACC.CHILDID_SELF;
         }
       }
 
       @Override
       public void getChildCount(final AccessibleControlEvent e)
       {
         e.detail = 0;
       }
 
       @Override
       public void getLocation(final AccessibleControlEvent e)
       {
         final Rectangle location = getBounds();
         final Point pt = toDisplay(location.x, location.y);
         e.x = pt.x;
         e.y = pt.y;
         e.width = location.width;
         e.height = location.height;
       }
 
       @Override
       public void getRole(final AccessibleControlEvent e)
       {
         e.detail = ACC.ROLE_COMBOBOX;
       }
 
       @Override
       public void getState(final AccessibleControlEvent e)
       {
         e.detail = ACC.STATE_NORMAL;
       }
 
       @Override
       public void getValue(final AccessibleControlEvent e)
       {
         e.result = getText();
       }
     });
 
     text.getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
       @Override
       public void getRole(final AccessibleControlEvent e)
       {
         e.detail = text.getEditable() ? ACC.ROLE_TEXT : ACC.ROLE_LABEL;
       }
     });
 
     arrow.getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {
       @Override
       public void getDefaultAction(final AccessibleControlEvent e)
       {
         e.result = isDropped() ? SWT.getMessage("SWT_Close") : SWT.getMessage("SWT_Open"); //$NON-NLS-1$ //$NON-NLS-2$
       }
     });
   }
 
   void internalLayout(final boolean changed)
   {
     if (isDropped()) {
       dropDown(false);
     }
     final Rectangle rect = getClientArea();
     final int width = rect.width;
     final int height = rect.height;
     final Point arrowSize = arrow.computeSize(SWT.DEFAULT, height, changed);
     text.setBounds(0, 0, width - arrowSize.x, height);
     arrow.setBounds(width - arrowSize.x, 0, arrowSize.x, arrowSize.y);
   }
 
   boolean isDropped()
   {
     return popup.getVisible();
   }
 
   void popupEvent(final Event event)
   {
     switch (event.type) {
       case SWT.Paint:
         // draw black rectangle around list
         final Rectangle listRect = m_timeChooser.getBounds();
         final Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
         event.gc.setForeground(black);
         event.gc.drawRectangle(0, 0, listRect.width + 1, listRect.height + 1);
         break;
       case SWT.Close:
         event.doit = false;
         dropDown(false);
         break;
       case SWT.Deactivate:
         dropDown(false);
         break;
     }
   }
 
   @Override
   public void redraw()
   {
     super.redraw();
     text.redraw();
     arrow.redraw();
     if (popup.isVisible()) {
       m_timeChooser.redraw();
     }
   }
 
   @Override
   public void redraw(final int x, final int y, final int width, final int height, final boolean all)
   {
     super.redraw(x, y, width, height, true);
   }
 
   public void select(final int selection)
   {
     checkWidget();
     if (selection == -1) {
       m_timeChooser.select(-1);
       text.setText(""); //$NON-NLS-1$
       return;
     }
    if (0 <= selection && selection <= 24 * 3600) {
       if (selection != getSelection()) {
         text.setText(TimeFormat.format(selection));
         m_timeChooser.select(selection);
       }
     }
   }
 
   @Override
   public boolean setFocus()
   {
     return text.setFocus();
   }
 
   String stripMnemonic(final String string)
   {
     int index = 0;
     final int length = string.length();
     do {
       while (index < length && string.charAt(index) != '&') {
         index++;
       }
       if (++index >= length) {
         return string;
       }
       if (string.charAt(index) != '&') {
         return string.substring(0, index - 1) + string.substring(index, length);
       }
       index++;
     } while (index < length);
     return string;
   }
 
   void textEvent(final Event event)
   {
     switch (event.type) {
       case SWT.FocusIn: {
         handleFocus(SWT.FocusIn);
         break;
       }
       case SWT.KeyDown: {
         if (event.character == SWT.CR) {
           dropDown(false);
           final int minutes = TimeFormat.parse(text.getText());
           if (minutes >= 0) {
             select(minutes);
           }
           final Event e = new Event();
           e.time = event.time;
           e.stateMask = event.stateMask;
           notifyListeners(SWT.DefaultSelection, e);
         }
         // At this point the widget may have been disposed.
         // If so, do not continue.
         if (isDisposed()) {
           break;
         }
 
         if (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN) {
           event.doit = false;
           if ((event.stateMask & SWT.ALT) != 0) {
             final boolean dropped = isDropped();
             text.selectAll();
             if (!dropped) {
               setFocus();
             }
             dropDown(!dropped);
             break;
           }
 
           final int oldSelection = m_timeChooser.getSelection();
           if (event.keyCode == SWT.ARROW_UP) {
             select(Math.max(oldSelection - 5, 0));
           } else {
             select(Math.min(oldSelection + 5, 24 * 60));
           }
           if (oldSelection != getSelection()) {
             final Event e = new Event();
             e.time = event.time;
             e.stateMask = event.stateMask;
             notifyListeners(SWT.Selection, e);
           }
           // At this point the widget may have been disposed.
           // If so, do not continue.
           if (isDisposed()) {
             break;
           }
         }
 
         // Further work : Need to add support for incremental search in
         // pop up list as characters typed in text widget
 
         final Event e = new Event();
         e.time = event.time;
         e.character = event.character;
         e.keyCode = event.keyCode;
         e.stateMask = event.stateMask;
         notifyListeners(SWT.KeyDown, e);
         break;
       }
       case SWT.KeyUp: {
         final Event e = new Event();
         e.time = event.time;
         e.character = event.character;
         e.keyCode = event.keyCode;
         e.stateMask = event.stateMask;
         notifyListeners(SWT.KeyUp, e);
         break;
       }
       case SWT.MenuDetect: {
         final Event e = new Event();
         e.time = event.time;
         notifyListeners(SWT.MenuDetect, e);
         break;
       }
       case SWT.FocusOut: {
         final int minutes = TimeFormat.parse(text.getText());
         if (minutes >= 0) {
           select(minutes);
         } else {
           select(-1);
         }
         final Event e = new Event();
         e.time = event.time;
         e.stateMask = event.stateMask;
         notifyListeners(SWT.Selection, e);
         break;
       }
       case SWT.MouseDown: {
         if (event.button != 1) {
           return;
         }
         if (text.getEditable()) {
           return;
         }
         final boolean dropped = isDropped();
         text.selectAll();
         if (!dropped) {
           setFocus();
         }
         dropDown(!dropped);
         break;
       }
       case SWT.MouseUp: {
         if (event.button != 1) {
           return;
         }
         if (text.getEditable()) {
           return;
         }
         text.selectAll();
         break;
       }
       case SWT.Traverse: {
         switch (event.detail) {
           case SWT.TRAVERSE_RETURN:
           case SWT.TRAVERSE_ARROW_PREVIOUS:
           case SWT.TRAVERSE_ARROW_NEXT:
             // The enter causes default selection and
             // the arrow keys are used to manipulate the list contents so
             // do not use them for traversal.
             event.doit = false;
             break;
         }
 
         final Event e = new Event();
         e.time = event.time;
         e.detail = event.detail;
         e.doit = event.doit;
         e.character = event.character;
         e.keyCode = event.keyCode;
         notifyListeners(SWT.Traverse, e);
         event.doit = e.doit;
         event.detail = e.detail;
         break;
       }
     }
   }
 
   void timeChooserEvent(final Event event)
   {
     switch (event.type) {
       case SWT.Dispose:
         if (getShell() != popup.getParent()) {
           final int selection = m_timeChooser.getSelection();
           popup = null;
           m_timeChooser = null;
           createPopup(selection);
         }
         break;
       case SWT.FocusIn: {
         handleFocus(SWT.FocusIn);
         break;
       }
       case SWT.MouseUp: {
         if (event.button != 1) {
           return;
         }
         dropDown(false);
         break;
       }
       case SWT.Selection: {
         final int selection = m_timeChooser.getSelection();
         if (selection == -1) {
           return;
         }
         text.setText(TimeFormat.format(selection));
         text.selectAll();
         final Event e = new Event();
         e.time = event.time;
         e.stateMask = event.stateMask;
         e.doit = event.doit;
         if ((event.stateMask & SWT.CTRL) != 0) {
           notifyListeners(SWT.DefaultSelection, e);
           dropDown(false);
         } else {
           notifyListeners(SWT.Selection, e);
         }
         event.doit = e.doit;
         break;
       }
       case SWT.Traverse: {
         switch (event.detail) {
           case SWT.TRAVERSE_RETURN:
           case SWT.TRAVERSE_ESCAPE:
           case SWT.TRAVERSE_ARROW_PREVIOUS:
           case SWT.TRAVERSE_ARROW_NEXT:
             event.doit = false;
             break;
         }
         final Event e = new Event();
         e.time = event.time;
         e.detail = event.detail;
         e.doit = event.doit;
         e.character = event.character;
         e.keyCode = event.keyCode;
         notifyListeners(SWT.Traverse, e);
         event.doit = e.doit;
         event.detail = e.detail;
         break;
       }
       case SWT.KeyUp: {
         final Event e = new Event();
         e.time = event.time;
         e.character = event.character;
         e.keyCode = event.keyCode;
         e.stateMask = event.stateMask;
         notifyListeners(SWT.KeyUp, e);
         break;
       }
       case SWT.KeyDown: {
         if (event.character == SWT.ESC) {
           // Escape key cancels popup list
           dropDown(false);
         }
         if ((event.stateMask & SWT.ALT) != 0 && (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN)) {
           dropDown(false);
         }
         if (event.character == SWT.CR) {
           // Enter causes default selection
           dropDown(false);
           final Event e = new Event();
           e.time = event.time;
           e.stateMask = event.stateMask;
           notifyListeners(SWT.DefaultSelection, e);
         }
         // At this point the widget may have been disposed.
         // If so, do not continue.
         if (isDisposed()) {
           break;
         }
         final Event e = new Event();
         e.time = event.time;
         e.character = event.character;
         e.keyCode = event.keyCode;
         e.stateMask = event.stateMask;
         notifyListeners(SWT.KeyDown, e);
         break;
 
       }
     }
   }
 
   static int checkStyle(final int style)
   {
     final int mask = SWT.BORDER | SWT.READ_ONLY | SWT.FLAT | SWT.LEFT_TO_RIGHT | SWT.RIGHT_TO_LEFT;
     return style & mask;
   }
 }
