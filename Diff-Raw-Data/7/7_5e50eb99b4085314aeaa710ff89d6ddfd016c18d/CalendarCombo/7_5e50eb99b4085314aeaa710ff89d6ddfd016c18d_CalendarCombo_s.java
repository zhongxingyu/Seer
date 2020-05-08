 /**
  *   Copyright 2012 Karl Martens
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
 
 import static net.karlmartens.ui.widget.Calendar.NO_SELECTION;
 import static org.eclipse.swt.SWT.ERROR_NULL_ARGUMENT;
 import static org.eclipse.swt.SWT.error;
 
 import java.util.Arrays;
 
 import net.karlmartens.platform.util.DateSupport;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.SWTException;
 import org.eclipse.swt.accessibility.ACC;
 import org.eclipse.swt.accessibility.AccessibleAdapter;
 import org.eclipse.swt.accessibility.AccessibleControlAdapter;
 import org.eclipse.swt.accessibility.AccessibleControlEvent;
 import org.eclipse.swt.accessibility.AccessibleEvent;
 import org.eclipse.swt.accessibility.AccessibleTextAdapter;
 import org.eclipse.swt.accessibility.AccessibleTextEvent;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.events.VerifyListener;
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
 import org.eclipse.swt.widgets.Layout;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.swt.widgets.ScrollBar;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.swt.widgets.TypedListener;
 import org.eclipse.swt.widgets.Widget;
 import org.joda.time.LocalDate;
 import org.joda.time.format.DateTimeFormatter;
 import org.joda.time.format.DateTimeFormatterBuilder;
 import org.joda.time.format.DateTimeParser;
 import org.joda.time.format.DateTimePrinter;
 
 /**
  * @author karl
  * 
  */
 public final class CalendarCombo extends Composite {
 
   private final DateTimeFormatter _dateFormat;
 
   private Shell _shell;
 
   private Text _text;
   private Button _arrow;
   private Shell _popup;
   private Calendar _calendar;
 
   private Color foreground, background;
   private Font font;
 
   private boolean hasFocus;
 
   /**
    * Constructs a new instance of this class given its parent and a style value
    * describing its behavior and appearance.
    * <p>
    * The style value is either one of the style constants defined in class
    * <code>SWT</code> which is applicable to instances of this class, or must be
    * built by <em>bitwise OR</em>'ing together (that is, using the
    * <code>int</code> "|" operator) two or more of those <code>SWT</code> style
    * constants. The class description lists the style constants that are
    * applicable to the class. Style bits are also inherited from superclasses.
    * </p>
    * 
    * @param parent
    *          a widget which will be the parent of the new instance (cannot be
    *          null)
    * @param style
    *          the style of widget to construct
    * 
    * @param dateFormart
    *          the format used for printing and parsing dates.
    * 
    * @exception IllegalArgumentException
    *              <ul>
    *              <li>ERROR_NULL_ARGUMENT - if the parent is null</li>
    *              </ul>
    * @exception SWTException
    *              <ul>
    *              <li>ERROR_THREAD_INVALID_ACCESS - if not called from the
    *              thread that created the parent</li>
    *              </ul>
    * 
    * @see SWT#BORDER
    * @see SWT#READ_ONLY
    * @see SWT#FLAT
    * @see Widget#getStyle()
    */
   public CalendarCombo(Composite parent, int style, DateTimeFormatter dateFormat) {
     super(parent, style = checkStyle(style));
     final int textStyle = checkTextStyle(style);
     final int arrowStyle = checkArrowStyle(style);
 
     _dateFormat = dateFormat;
 
     _shell = super.getShell();
     _text = new Text(this, textStyle);
 
     _arrow = new Button(this, arrowStyle);
 
     final int[] comboEvents = { SWT.Dispose, SWT.FocusIn, SWT.Move, SWT.Resize };
     for (int i = 0; i < comboEvents.length; i++)
       this.addListener(comboEvents[i], _listener);
 
     final int[] textEvents = { SWT.DefaultSelection, SWT.DragDetect,
         SWT.KeyDown, SWT.KeyUp, SWT.MenuDetect, SWT.Modify, SWT.MouseDown,
         SWT.MouseUp, SWT.MouseDoubleClick, SWT.MouseEnter, SWT.MouseExit,
         SWT.MouseHover, SWT.MouseMove, SWT.MouseWheel, SWT.Traverse,
         SWT.FocusIn, SWT.Verify };
     for (int i = 0; i < textEvents.length; i++)
       _text.addListener(textEvents[i], _listener);
 
     final int[] arrowEvents = { SWT.DragDetect, SWT.MouseDown, SWT.MouseEnter,
         SWT.MouseExit, SWT.MouseHover, SWT.MouseMove, SWT.MouseUp,
         SWT.MouseWheel, SWT.Selection, SWT.FocusIn };
     for (int i = 0; i < arrowEvents.length; i++)
       _arrow.addListener(arrowEvents[i], _listener);
 
     createPopup(null);
 
     final int[] date = _calendar.getSelection();
     setSelection(date[0], date[1], date[2]);
 
     initAccessible();
   }
   
   public CalendarCombo(Composite parent, int style) {
     this(parent, style, createDateFormat());
   }
 
   public Control[] getChildren() {
     checkWidget();
     return new Control[0];
   }
 
   public Shell getShell() {
     checkWidget();
     final Shell shell = super.getShell();
     if (shell != _shell) {
       if (_shell != null && !_shell.isDisposed()) {
         _shell.removeListener(SWT.Deactivate, _listener);
       }
       _shell = shell;
     }
     return _shell;
   }
 
   public int getStyle() {
     int style = super.getStyle();
     style &= ~SWT.READ_ONLY;
     if (!_text.getEditable())
       style |= SWT.READ_ONLY;
     return style;
   }
 
   public void setLayout(Layout layout) {
     checkWidget();
     return;
   }
 
   public void setBackground(Color color) {
     super.setBackground(color);
     background = color;
     if (_text != null)
       _text.setBackground(color);
     if (_calendar != null)
       _calendar.setBackground(color);
     if (_arrow != null)
       _arrow.setBackground(color);
   }
 
   public void setForeground(Color color) {
     super.setForeground(color);
     foreground = color;
     if (_text != null)
       _text.setForeground(color);
     if (_calendar != null)
       _calendar.setForeground(color);
     if (_arrow != null)
       _arrow.setForeground(color);
   }
 
   public void setFont(Font font) {
     super.setFont(font);
     this.font = font;
     _text.setFont(font);
     _calendar.setFont(font);
     internalLayout(true);
   }
 
   public boolean getEditable() {
     checkWidget();
     return _text.getEditable();
   }
 
   public void setEditable(boolean editable) {
     checkWidget();
     _text.setEditable(editable);
   }
 
   public void setEnabled(boolean enabled) {
     super.setEnabled(enabled);
     if (_popup != null)
       _popup.setVisible(false);
     if (_text != null)
       _text.setEnabled(enabled);
     if (_arrow != null)
       _arrow.setEnabled(enabled);
   }
 
   public void setVisible(boolean visible) {
     super.setVisible(visible);
     /*
      * At this point the widget may have been disposed in a FocusOut event. If
      * so then do not continue.
      */
     if (isDisposed())
       return;
     // TEMPORARY CODE
     if (_popup == null || _popup.isDisposed())
       return;
     if (!visible)
       _popup.setVisible(false);
   }
 
   public boolean isFocusControl() {
     checkWidget();
     if (_text.isFocusControl() || _arrow.isFocusControl()
         || _calendar.isFocusControl() || _popup.isFocusControl()) {
       return true;
     }
     return super.isFocusControl();
   }
 
   public boolean setFocus() {
     checkWidget();
     if (!isEnabled() || !getVisible())
       return false;
     if (isFocusControl())
       return true;
     return _text.setFocus();
   }
 
   public Point computeSize(int wHint, int hHint, boolean changed) {
     checkWidget();
 
     final GC gc = new GC(_text);
     final int spacer = gc.getFontMetrics().getAverageCharWidth();
     final int textWidth = spacer * 10;
     gc.dispose();
 
     final Point textSize = _text.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
     final Point arrowSize = _arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT,
         changed);
     final int borderWidth = getBorderWidth();
 
     int width = textWidth + 2 * spacer + arrowSize.x;
     if (wHint != SWT.DEFAULT)
       width = wHint;
 
     int height = Math.max(textSize.y, arrowSize.y);
     if (hHint != SWT.DEFAULT)
       height = hHint;
 
     return new Point(width + 2 * borderWidth, height + 2 * borderWidth);
   }
 
   public void redraw() {
     super.redraw();
     _text.redraw();
     _arrow.redraw();
     if (_popup.isVisible())
       _calendar.redraw();
   }
 
   public void redraw(int x, int y, int width, int height, boolean all) {
     super.redraw(x, y, width, height, true);
   }
 
   public Menu getMenu() {
     return _text.getMenu();
   }
 
   public void setMenu(Menu menu) {
     _text.setMenu(menu);
   }
 
   public int getTextHeight() {
     checkWidget();
     return _text.getLineHeight();
   }
 
   public void setToolTipText(String string) {
     checkWidget();
     super.setToolTipText(string);
     _arrow.setToolTipText(string);
     _text.setToolTipText(string);
   }
 
   public void copy() {
     checkWidget();
     _text.copy();
   }
 
   public void cut() {
     checkWidget();
     _text.cut();
   }
 
   public void paste() {
     checkWidget();
     _text.paste();
   }
 
   public boolean isCalendarVisible() {
     checkWidget();
     return isDropped();
   }
 
   public void setCalendarVisible(boolean visible) {
     checkWidget();
     dropDown(visible);
   }
 
   public int[] getSelection() {
     checkWidget();
     return _calendar.getSelection();
   }
 
   public void setSelection(int year, int month, int day) {
     checkWidget();
     _calendar.setSelection(year, month, day);
     _calendar.scrollTo(year, month);
 
     if (Arrays.equals(Calendar.NO_SELECTION, new int[] { year, month, day }))
       return;
 
     final LocalDate ld = new LocalDate(year, month, day);
     setText(_dateFormat.print(ld));
   }
 
   public String getText() {
     checkWidget();
     return _text.getText();
   }
 
   public void setText(String text) {
     checkWidget();
     if (text == null)
       error(ERROR_NULL_ARGUMENT);
 
     if (text.equals(_text.getText()))
       return;
 
     _text.setText(text);
 
     int[] selection;
     try {
       final LocalDate ld = _dateFormat.parseLocalDate(text);
       selection = new int[] { ld.getYear(), ld.getMonthOfYear(),
           ld.getDayOfMonth() };
     } catch (IllegalArgumentException e) {
       selection = NO_SELECTION;
     }
     setSelection(selection[0], selection[1], selection[2]);
   }
 
   public void setTextSelection(int start) {
     checkWidget();
     _text.setSelection(start);
   }
 
   public void addDays(int days) {
     _calendar.addDays(days);
   }
 
   public boolean traverse(int event) {
     /*
      * When the traverse event is sent to the CCombo, it will create a list of
      * controls to tab to next. Since the CCombo is a composite, the next
      * control is the Text field which is a child of the CCombo. It will set
      * focus to the text field which really is itself. So, call the traverse
      * next events directly on the text.
      */
     if (event == SWT.TRAVERSE_ARROW_NEXT || event == SWT.TRAVERSE_TAB_NEXT) {
       return _text.traverse(event);
     }
     return super.traverse(event);
   }
 
   public void addModifyListener(ModifyListener listener) {
     checkWidget();
     if (listener == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
     final TypedListener typedListener = new TypedListener(listener);
     addListener(SWT.Modify, typedListener);
   }
 
   public void removeModifyListener(ModifyListener listener) {
     checkWidget();
     if (listener == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
     removeListener(SWT.Modify, listener);
   }
 
   public void addSelectionListener(SelectionListener listener) {
     checkWidget();
     if (listener == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
     final TypedListener typedListener = new TypedListener(listener);
     addListener(SWT.Selection, typedListener);
     addListener(SWT.DefaultSelection, typedListener);
   }
 
   public void removeSelectionListener(SelectionListener listener) {
     checkWidget();
     if (listener == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
     removeListener(SWT.Selection, listener);
     removeListener(SWT.DefaultSelection, listener);
   }
 
   public void addVerifyListener(VerifyListener listener) {
     checkWidget();
     if (listener == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
 
     final TypedListener typedListener = new TypedListener(listener);
     addListener(SWT.Verify, typedListener);
   }
 
   public void removeVerifyListener(VerifyListener listener) {
     checkWidget();
     if (listener == null)
       SWT.error(SWT.ERROR_NULL_ARGUMENT);
     removeListener(SWT.Verify, listener);
   }
 
   private boolean isParentScrolling(Control scrollableParent) {
     Control parent = this.getParent();
     while (parent != null) {
       if (parent.equals(scrollableParent))
         return true;
       parent = parent.getParent();
     }
     return false;
   }
 
   private void internalLayout(boolean changed) {
     if (isDropped())
       dropDown(false);
     Rectangle rect = getClientArea();
     int width = rect.width;
     int height = rect.height;
     Point arrowSize = _arrow.computeSize(SWT.DEFAULT, height, changed);
     _text.setBounds(0, 0, width - arrowSize.x, height);
     _arrow.setBounds(width - arrowSize.x, 0, arrowSize.x, arrowSize.y);
   }
 
   private void createPopup(int[] date) {
     _popup = new Shell(getShell(), SWT.NO_TRIM | SWT.ON_TOP);
     _calendar = new Calendar(_popup, getStyle());
 
     if (font != null)
       _calendar.setFont(font);
 
     if (foreground != null)
       _calendar.setForeground(foreground);
 
     if (background != null)
       _calendar.setBackground(background);
 
     final int[] popupEvents = { SWT.Close, SWT.Paint };
     for (int i = 0; i < popupEvents.length; i++)
       _popup.addListener(popupEvents[i], _listener);
 
     final int[] calendarEvents = { SWT.MouseUp, SWT.Selection, SWT.Traverse,
         SWT.KeyDown, SWT.KeyUp, SWT.FocusIn, SWT.FocusOut, SWT.Dispose };
     for (int i = 0; i < calendarEvents.length; i++)
       _calendar.addListener(calendarEvents[i], _listener);
 
     if (date != null)
       _calendar.setSelection(date[0], date[1], date[2]);
   }
 
   public static DateTimeFormatter createDateFormat() {
     final int pivotYear = DateSupport.currentYear() + 40;
     final DateTimeParser[] parsers = new DateTimeParser[] { //
     //
         new DateTimeFormatterBuilder()//
             .appendTwoDigitYear(pivotYear, false)//
             .appendLiteral('-')//
             .appendMonthOfYear(1)//
             .appendLiteral('-')//
             .appendDayOfMonth(1)//
             .toParser(),
 
         new DateTimeFormatterBuilder()//
             .appendYear(4, 4)//
             .appendLiteral('-')//
             .appendMonthOfYear(1)//
             .appendLiteral('-')//
             .appendDayOfMonth(1)//
             .toParser(), //
 
         new DateTimeFormatterBuilder()//
             .appendMonthOfYearShortText()//
             .appendLiteral(' ')//
             .appendDayOfMonth(1)//
             .appendLiteral(", ")//
             .appendTwoDigitYear(pivotYear, false)//
             .toParser(), //
 
         new DateTimeFormatterBuilder()//
             .appendMonthOfYearShortText()//
             .appendLiteral(' ')//
             .appendDayOfMonth(1)//
             .appendLiteral(", ")//
             .appendYear(4, 4)//
             .toParser(), //
     };
 
     final DateTimePrinter printer = new DateTimeFormatterBuilder()//
         .appendYear(4, 4)//
        .appendLiteral('/')//
         .appendMonthOfYear(2)//
        .appendLiteral('/')//
         .appendDayOfMonth(2)//
         .toPrinter();
 
     return new DateTimeFormatterBuilder()//
         .append(printer, parsers)//
         .toFormatter()//
         .withPivotYear(pivotYear);
   }
 
   private String getAssociatedLabel() {
     final Control[] siblings = getParent().getChildren();
     for (int i = 0; i < siblings.length; i++) {
       if (siblings[i] == this) {
         if (i > 0) {
           final Control sibling = siblings[i - 1];
           if (sibling instanceof Label)
             return ((Label) sibling).getText();
           if (sibling instanceof CLabel)
             return ((CLabel) sibling).getText();
         }
         break;
       }
     }
     return null;
   }
 
   private void initAccessible() {
     final AccessibleAdapter accessibleAdapter = new AccessibleAdapter() {
       public void getName(AccessibleEvent e) {
         String name = null;
         String text = getAssociatedLabel();
         if (text != null) {
           name = stripMnemonic(text);
         }
         e.result = name;
       }
 
       public void getKeyboardShortcut(AccessibleEvent e) {
         String shortcut = null;
         String text = getAssociatedLabel();
         if (text != null) {
           char mnemonic = findMnemonic(text);
           if (mnemonic != '\0') {
             shortcut = "Alt+" + mnemonic; //$NON-NLS-1$
           }
         }
         e.result = shortcut;
       }
 
       public void getHelp(AccessibleEvent e) {
         e.result = getToolTipText();
       }
     };
 
     getAccessible().addAccessibleListener(accessibleAdapter);
     _text.getAccessible().addAccessibleListener(accessibleAdapter);
     _calendar.getAccessible().addAccessibleListener(accessibleAdapter);
 
     _arrow.getAccessible().addAccessibleListener(new AccessibleAdapter() {
       public void getName(AccessibleEvent e) {
         e.result = isDropped() ? SWT.getMessage("SWT_Close") : SWT.getMessage("SWT_Open"); //$NON-NLS-1$ //$NON-NLS-2$
       }
 
       public void getKeyboardShortcut(AccessibleEvent e) {
         e.result = "Alt+Down Arrow"; //$NON-NLS-1$
       }
 
       public void getHelp(AccessibleEvent e) {
         e.result = getToolTipText();
       }
     });
 
     getAccessible().addAccessibleTextListener(new AccessibleTextAdapter() {
       public void getCaretOffset(AccessibleTextEvent e) {
         e.offset = _text.getCaretPosition();
       }
 
       public void getSelectionRange(AccessibleTextEvent e) {
         Point sel = _text.getSelection();
         e.offset = sel.x;
         e.length = sel.y - sel.x;
       }
     });
 
     getAccessible().addAccessibleControlListener(
         new AccessibleControlAdapter() {
           public void getChildAtPoint(AccessibleControlEvent e) {
             Point testPoint = toControl(e.x, e.y);
             if (getBounds().contains(testPoint)) {
               e.childID = ACC.CHILDID_SELF;
             }
           }
 
           public void getLocation(AccessibleControlEvent e) {
             Rectangle location = getBounds();
             Point pt = getParent().toDisplay(location.x, location.y);
             e.x = pt.x;
             e.y = pt.y;
             e.width = location.width;
             e.height = location.height;
           }
 
           public void getChildCount(AccessibleControlEvent e) {
             e.detail = 0;
           }
 
           public void getRole(AccessibleControlEvent e) {
             e.detail = ACC.ROLE_COMBOBOX;
           }
 
           public void getState(AccessibleControlEvent e) {
             e.detail = ACC.STATE_NORMAL;
           }
 
           public void getValue(AccessibleControlEvent e) {
             e.result = _text.getText();
           }
         });
 
     _text.getAccessible().addAccessibleControlListener(
         new AccessibleControlAdapter() {
           public void getRole(AccessibleControlEvent e) {
             e.detail = _text.getEditable() ? ACC.ROLE_TEXT : ACC.ROLE_LABEL;
           }
         });
 
     _arrow.getAccessible().addAccessibleControlListener(
         new AccessibleControlAdapter() {
           public void getDefaultAction(AccessibleControlEvent e) {
             e.result = isDropped() ? SWT.getMessage("SWT_Close") : SWT.getMessage("SWT_Open"); //$NON-NLS-1$ //$NON-NLS-2$
           }
         });
   }
 
   private boolean isDropped() {
     if (_popup == null || _popup.isDisposed())
       return false;
 
     return _popup.getVisible();
   }
 
   private void dropDown(boolean drop) {
     if (drop == isDropped())
       return;
 
     final Display display = getDisplay();
     if (!drop) {
       display.removeFilter(SWT.Selection, _filter);
       _popup.setVisible(false);
       if (!isDisposed() && isFocusControl()) {
         _text.setFocus();
       }
       return;
     }
 
     if (!isVisible())
       return;
 
     if (getShell() != _popup.getParent()) {
       final int[] date = _calendar.getSelection();
       _calendar.removeListener(SWT.Dispose, _listener);
       _popup.dispose();
       _popup = null;
       _calendar = null;
       createPopup(date);
     }
 
     final Point comboSize = getSize();
     final Point listSize = _calendar.computeSize(SWT.DEFAULT, SWT.DEFAULT,
         false);
     final Rectangle displayRect = getMonitor().getClientArea();
     _calendar.setBounds(1, 1,
         Math.max(comboSize.x - 2, Math.min(listSize.x, displayRect.width - 2)),
         listSize.y);
 
     final Rectangle listRect = _calendar.getBounds();
     final Rectangle parentRect = display.map(getParent(), null, getBounds());
     final int width = listRect.width + 2;
     int height = listRect.height + 2;
     int x = parentRect.x;
     if (x + width > displayRect.x + displayRect.width) {
       x = displayRect.x + displayRect.width - width;
     }
 
     int y = parentRect.y + comboSize.y;
     if (y + height > displayRect.y + displayRect.height) {
       final int popUpwardsHeight = (parentRect.y - height < displayRect.y) ? parentRect.y
           - displayRect.y
           : height;
       final int popDownwardsHeight = displayRect.y + displayRect.height - y;
       if (popUpwardsHeight > popDownwardsHeight) {
         height = popUpwardsHeight;
         y = parentRect.y - popUpwardsHeight;
       } else {
         height = popDownwardsHeight;
       }
       _calendar.setSize(listRect.width, height - 2);
     }
     _popup.setBounds(x, y, width, height);
     _popup.setVisible(true);
     if (isFocusControl())
       _calendar.setFocus();
 
     /*
      * Add a filter to listen to scrolling of the parent composite, when the
      * drop-down is visible. Remove the filter when drop-down is not visible.
      */
     display.removeFilter(SWT.Selection, _filter);
     display.addFilter(SWT.Selection, _filter);
   }
 
   private void handlePopupEvent(Event event) {
     switch (event.type) {
       case SWT.Paint:
         // draw black rectangle around list
         final Rectangle listRect = _calendar.getBounds();
         final Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
         event.gc.setForeground(black);
         event.gc.drawRectangle(0, 0, listRect.width + 1, listRect.height + 1);
         break;
 
       case SWT.Close:
         event.doit = false;
         dropDown(false);
         break;
     }
   }
 
   private final int[] _modifyId = new int[1];
 
   private void handleTextEvent(Event event) {
     switch (event.type) {
       case SWT.FocusIn: {
         handleFocus(SWT.FocusIn);
         break;
       }
 
       case SWT.DefaultSelection: {
         dropDown(false);
         final Event e = new Event();
         e.time = event.time;
         e.stateMask = event.stateMask;
         notifyListeners(SWT.DefaultSelection, e);
         break;
       }
 
       case SWT.DragDetect:
       case SWT.MouseDoubleClick:
       case SWT.MouseMove:
       case SWT.MouseEnter:
       case SWT.MouseExit:
       case SWT.MouseHover: {
         final Point pt = getDisplay().map(_text, this, event.x, event.y);
         event.x = pt.x;
         event.y = pt.y;
         notifyListeners(event.type, event);
         event.type = SWT.None;
         break;
       }
 
       case SWT.KeyDown: {
         final Event keyEvent = new Event();
         keyEvent.time = event.time;
         keyEvent.character = event.character;
         keyEvent.keyCode = event.keyCode;
         keyEvent.keyLocation = event.keyLocation;
         keyEvent.stateMask = event.stateMask;
         notifyListeners(SWT.KeyDown, keyEvent);
         if (isDisposed())
           break;
         event.doit = keyEvent.doit;
         if (!event.doit)
           break;
         if (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN) {
           event.doit = false;
           if ((event.stateMask & SWT.ALT) != 0) {
             final boolean dropped = isDropped();
             _text.selectAll();
             if (!dropped)
               setFocus();
             dropDown(!dropped);
             break;
           }
 
           if (event.keyCode == SWT.ARROW_UP) {
             addDays(-1);
           } else {
             addDays(1);
           }
 
           if (isDisposed())
             break;
         }
 
         break;
       }
 
       case SWT.KeyUp: {
         final Event e = new Event();
         e.time = event.time;
         e.character = event.character;
         e.keyCode = event.keyCode;
         e.keyLocation = event.keyLocation;
         e.stateMask = event.stateMask;
         notifyListeners(SWT.KeyUp, e);
         event.doit = e.doit;
         break;
       }
 
       case SWT.MenuDetect: {
         final Event e = new Event();
         e.time = event.time;
         e.detail = event.detail;
         e.x = event.x;
         e.y = event.y;
         if (event.detail == SWT.MENU_KEYBOARD) {
           Point pt = getDisplay().map(_text, null, _text.getCaretLocation());
           e.x = pt.x;
           e.y = pt.y;
         }
         notifyListeners(SWT.MenuDetect, e);
         event.doit = e.doit;
         event.x = e.x;
         event.y = e.y;
         break;
       }
 
       case SWT.Modify: {
         final int id = ++_modifyId[0];
         getDisplay().timerExec(1000, new Runnable() {
           @Override
           public void run() {
             if (isDisposed() || id != _modifyId[0])
               return;
 
             try {
               final LocalDate ld = _dateFormat.parseLocalDate(_text.getText());
               setSelection(ld.getYear(), ld.getMonthOfYear(),
                   ld.getDayOfMonth());
             } catch (IllegalArgumentException e) {
               setSelection(Calendar.NO_SELECTION[0], Calendar.NO_SELECTION[1],
                   Calendar.NO_SELECTION[2]);
             }
           }
         });
 
         final Event e = new Event();
         e.time = event.time;
         notifyListeners(SWT.Modify, e);
         break;
       }
 
       case SWT.MouseDown: {
         final Point pt = getDisplay().map(_text, this, event.x, event.y);
         final Event mouseEvent = new Event();
         mouseEvent.button = event.button;
         mouseEvent.count = event.count;
         mouseEvent.stateMask = event.stateMask;
         mouseEvent.time = event.time;
         mouseEvent.x = pt.x;
         mouseEvent.y = pt.y;
         notifyListeners(SWT.MouseDown, mouseEvent);
         if (isDisposed())
           break;
         event.doit = mouseEvent.doit;
         if (!event.doit)
           break;
         if (event.button != 1)
           return;
         if (_text.getEditable())
           return;
         boolean dropped = isDropped();
         _text.selectAll();
         if (!dropped)
           setFocus();
         dropDown(!dropped);
         break;
       }
 
       case SWT.MouseUp: {
         final Point pt = getDisplay().map(_text, this, event.x, event.y);
         final Event mouseEvent = new Event();
         mouseEvent.button = event.button;
         mouseEvent.count = event.count;
         mouseEvent.stateMask = event.stateMask;
         mouseEvent.time = event.time;
         mouseEvent.x = pt.x;
         mouseEvent.y = pt.y;
         notifyListeners(SWT.MouseUp, mouseEvent);
         if (isDisposed())
           break;
         event.doit = mouseEvent.doit;
         if (!event.doit)
           break;
         if (event.button != 1)
           return;
         if (_text.getEditable())
           return;
         _text.selectAll();
         break;
       }
 
       case SWT.MouseWheel: {
         notifyListeners(SWT.MouseWheel, event);
         event.type = SWT.None;
         if (isDisposed())
           break;
         if (!event.doit)
           break;
         if (event.count != 0) {
           event.doit = false;
           if (event.count > 0) {
             addDays(-1);
           } else {
             addDays(1);
           }
 
           if (isDisposed())
             break;
         }
         break;
       }
 
       case SWT.Traverse: {
         switch (event.detail) {
           case SWT.TRAVERSE_ARROW_PREVIOUS:
           case SWT.TRAVERSE_ARROW_NEXT:
             // The enter causes default selection and
             // the arrow keys are used to manipulate the list contents so
             // do not use them for traversal.
             event.doit = false;
             break;
           case SWT.TRAVERSE_TAB_PREVIOUS:
             event.doit = traverse(SWT.TRAVERSE_TAB_PREVIOUS);
             event.detail = SWT.TRAVERSE_NONE;
             return;
         }
         final Event e = new Event();
         e.time = event.time;
         e.detail = event.detail;
         e.doit = event.doit;
         e.character = event.character;
         e.keyCode = event.keyCode;
         e.keyLocation = event.keyLocation;
         notifyListeners(SWT.Traverse, e);
         event.doit = e.doit;
         event.detail = e.detail;
         break;
       }
 
       case SWT.Verify: {
         final Event e = new Event();
         e.text = event.text;
         e.start = event.start;
         e.end = event.end;
         e.character = event.character;
         e.keyCode = event.keyCode;
         e.keyLocation = event.keyLocation;
         e.stateMask = event.stateMask;
         notifyListeners(SWT.Verify, e);
         event.text = e.text;
         event.doit = e.doit;
         break;
       }
     }
   }
 
   private void handleCalendarEvent(Event event) {
     switch (event.type) {
       case SWT.Dispose:
         if (getShell() != _popup.getParent()) {
           final int[] date = _calendar.getSelection();
           _popup = null;
           _calendar = null;
           createPopup(date);
         }
         break;
 
       case SWT.FocusIn: {
         handleFocus(SWT.FocusIn);
         break;
       }
 
       case SWT.FocusOut: {
         /*
          * Behavior in Windows, GTK & Cocoa: When the arrow button is pressed
          * with the popup list visible, the following events are received- popup
          * control receives a deactivate event, list receives focus lost event,
          * and then arrow button receives a selection event. If we hide the
          * popup in the focus out event, the selection event will show it again.
          * To prevent the popup from showing again, we will detect this case and
          * let the selection event of the arrow button hide the popup.
          */
         if (!"carbon".equals(SWT.getPlatform())) {
           final Point point = _arrow
               .toControl(getDisplay().getCursorLocation());
           final Point size = _arrow.getSize();
           final Rectangle rect = new Rectangle(0, 0, size.x, size.y);
           if (rect.contains(point)) {
             final boolean comboShellActivated = getDisplay().getActiveShell() == getShell();
             if (!comboShellActivated)
               dropDown(false);
             break;
           }
         }
         dropDown(false);
         break;
       }
 
       case SWT.MouseUp: {
         if (event.button != 1)
           return;
         dropDown(false);
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
           case SWT.TRAVERSE_TAB_NEXT:
           case SWT.TRAVERSE_TAB_PREVIOUS:
             event.doit = _text.traverse(event.detail);
             event.detail = SWT.TRAVERSE_NONE;
             if (event.doit)
               dropDown(false);
             return;
         }
         final Event e = new Event();
         e.time = event.time;
         e.detail = event.detail;
         e.doit = event.doit;
         e.character = event.character;
         e.keyCode = event.keyCode;
         e.keyLocation = event.keyLocation;
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
         e.keyLocation = event.keyLocation;
         e.stateMask = event.stateMask;
         notifyListeners(SWT.KeyUp, e);
         event.doit = e.doit;
         break;
       }
 
       case SWT.KeyDown: {
         if (event.character == SWT.ESC) {
           // Escape key cancels popup list
           dropDown(false);
         }
         if ((event.stateMask & SWT.ALT) != 0
             && (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN)) {
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
         if (isDisposed())
           break;
         final Event e = new Event();
         e.time = event.time;
         e.character = event.character;
         e.keyCode = event.keyCode;
         e.keyLocation = event.keyLocation;
         e.stateMask = event.stateMask;
         notifyListeners(SWT.KeyDown, e);
         event.doit = e.doit;
         break;
       }
 
       case SWT.Selection: {
         final int[] date = _calendar.getSelection();
         if (Arrays.equals(date, Calendar.NO_SELECTION))
           break;
 
         _text.setText(_dateFormat
             .print(new LocalDate(date[0], date[1], date[2])));
         _text.selectAll();
         _calendar.setSelection(date[0], date[1], date[2]);
 
         final Event e = new Event();
         notifyListeners(SWT.Selection, e);
       }
     }
   }
 
   private void handleArrowEvent(Event event) {
     switch (event.type) {
       case SWT.FocusIn: {
         handleFocus(SWT.FocusIn);
         break;
       }
 
       case SWT.DragDetect:
       case SWT.MouseDown:
       case SWT.MouseUp:
       case SWT.MouseMove:
       case SWT.MouseEnter:
       case SWT.MouseExit:
       case SWT.MouseHover: {
         final Point pt = getDisplay().map(_arrow, this, event.x, event.y);
         event.x = pt.x;
         event.y = pt.y;
         notifyListeners(event.type, event);
         event.type = SWT.None;
         break;
       }
 
       case SWT.MouseWheel: {
         final Point pt = getDisplay().map(_arrow, this, event.x, event.y);
         event.x = pt.x;
         event.y = pt.y;
         notifyListeners(SWT.MouseWheel, event);
         event.type = SWT.None;
         if (isDisposed())
           break;
         if (!event.doit)
           break;
         if (event.count != 0) {
           event.doit = false;
           if (event.count > 0) {
             addDays(-1);
           } else {
             addDays(1);
           }
 
           if (isDisposed())
             break;
         }
         break;
       }
 
       case SWT.Selection: {
         _text.setFocus();
         dropDown(!isDropped());
         break;
       }
     }
   }
 
   private void handleComboEvent(Event event) {
     switch (event.type) {
       case SWT.Dispose:
         removeListener(SWT.Dispose, _listener);
         notifyListeners(SWT.Dispose, event);
         event.type = SWT.None;
 
         if (_popup != null && !_popup.isDisposed()) {
           _calendar.removeListener(SWT.Dispose, _listener);
           _popup.dispose();
         }
         final Shell shell = getShell();
         shell.removeListener(SWT.Deactivate, _listener);
         final Display display = getDisplay();
         display.removeFilter(SWT.FocusIn, _filter);
         _popup = null;
         _text = null;
         _calendar = null;
         _arrow = null;
         _shell = null;
         break;
 
       case SWT.FocusIn:
         final Control focusControl = getDisplay().getFocusControl();
         if (focusControl == _arrow || focusControl == _calendar)
           return;
         if (isDropped()) {
           _calendar.setFocus();
         } else {
           _text.setFocus();
         }
         break;
 
       case SWT.Move:
         dropDown(false);
         break;
 
       case SWT.Resize:
         internalLayout(false);
         break;
     }
   }
 
   private void handleFocus(int type) {
     switch (type) {
       case SWT.FocusIn: {
         if (hasFocus)
           return;
         if (getEditable())
           _text.selectAll();
         hasFocus = true;
 
         final Shell shell = getShell();
         shell.removeListener(SWT.Deactivate, _listener);
         shell.addListener(SWT.Deactivate, _listener);
 
         final Display display = getDisplay();
         display.removeFilter(SWT.FocusIn, _filter);
         display.addFilter(SWT.FocusIn, _filter);
 
         final Event e = new Event();
         notifyListeners(SWT.FocusIn, e);
         break;
       }
 
       case SWT.FocusOut: {
         if (!hasFocus)
           return;
         Control focusControl = getDisplay().getFocusControl();
         while (focusControl != null) {
           if (focusControl == this || focusControl == _text
               || focusControl == _arrow || focusControl == _calendar)
             return;
 
           focusControl = focusControl.getParent();
         }
 
         hasFocus = false;
 
         final Shell shell = getShell();
         shell.removeListener(SWT.Deactivate, _listener);
 
         final Display display = getDisplay();
         display.removeFilter(SWT.FocusIn, _filter);
 
         final Event e = new Event();
         notifyListeners(SWT.FocusOut, e);
         break;
       }
     }
   }
 
   private void handleScroll(Event event) {
     final ScrollBar scrollBar = (ScrollBar) event.widget;
     final Control scrollableParent = scrollBar.getParent();
     if (scrollableParent.equals(_calendar))
       return;
 
     if (isParentScrolling(scrollableParent))
       dropDown(false);
   }
 
   private static int checkStyle(int style) {
     final int mask = SWT.BORDER | SWT.READ_ONLY | SWT.FLAT | SWT.LEFT_TO_RIGHT
         | SWT.RIGHT_TO_LEFT;
     return SWT.NO_FOCUS | (style & mask);
   }
 
   private static int checkTextStyle(int style) {
     final int mask = SWT.READ_ONLY | SWT.FLAT;
     return SWT.SINGLE | (style & mask);
   }
 
   private static int checkArrowStyle(int style) {
     final int mask = SWT.FLAT;
     return SWT.ARROW | SWT.DOWN | (style & mask);
   }
 
   private static char findMnemonic(String string) {
     if (string == null)
       return '\0';
 
     final int length = string.length();
     int index = 0;
     do {
       while (index < length && string.charAt(index) != '&')
         index++;
       if (++index >= length)
         return '\0';
       if (string.charAt(index) != '&')
         return Character.toLowerCase(string.charAt(index));
       index++;
     } while (index < length);
     return '\0';
   }
 
   private static String stripMnemonic(String string) {
     final int length = string.length();
     int index = 0;
     do {
       while ((index < length) && (string.charAt(index) != '&'))
         index++;
       if (++index >= length)
         return string;
       if (string.charAt(index) != '&') {
         return string.substring(0, index - 1) + string.substring(index, length);
       }
       index++;
     } while (index < length);
     return string;
   }
 
   private final Listener _listener = new Listener() {
     public void handleEvent(Event event) {
       if (isDisposed())
         return;
 
       if (_popup == event.widget) {
         handlePopupEvent(event);
         return;
       }
 
       if (_text == event.widget) {
         handleTextEvent(event);
         return;
       }
 
       if (_calendar == event.widget) {
         handleCalendarEvent(event);
         return;
       }
 
       if (_arrow == event.widget) {
         handleArrowEvent(event);
         return;
       }
 
       if (CalendarCombo.this == event.widget) {
         handleComboEvent(event);
         return;
       }
 
       if (getShell() == event.widget) {
         getDisplay().asyncExec(new Runnable() {
           public void run() {
             if (isDisposed())
               return;
             handleFocus(SWT.FocusOut);
           }
         });
       }
     }
   };
 
   private final Listener _filter = new Listener() {
     public void handleEvent(Event event) {
       if (isDisposed())
         return;
       if (event.type == SWT.Selection) {
         if (event.widget instanceof ScrollBar) {
           handleScroll(event);
         }
         return;
       }
       Shell shell = ((Control) event.widget).getShell();
       if (shell == CalendarCombo.this.getShell()) {
         handleFocus(SWT.FocusOut);
       }
     }
   };
 }
