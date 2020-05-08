 /*
 Copyright (c) 1998, 1999 Wabasoft  All rights reserved.
 
 This software is furnished under a license and may be used only in accordance
 with the terms of that license. This software and documentation, and its
 copyrights are owned by Wabasoft and are protected by copyright law.
 
 THIS SOFTWARE AND REFERENCE MATERIALS ARE PROVIDED "AS IS" WITHOUT WARRANTY
 AS TO THEIR PERFORMANCE, MERCHANTABILITY, FITNESS FOR ANY PARTICULAR PURPOSE,
 OR AGAINST INFRINGEMENT. WABASOFT ASSUMES NO RESPONSIBILITY FOR THE USE OR
 INABILITY TO USE THIS SOFTWARE. WABASOFT SHALL NOT BE LIABLE FOR INDIRECT,
 SPECIAL OR CONSEQUENTIAL DAMAGES RESULTING FROM THE USE OF THIS PRODUCT.
 
 WABASOFT SHALL HAVE NO LIABILITY OR RESPONSIBILITY FOR SOFTWARE ALTERED,
 MODIFIED, OR CONVERTED BY YOU OR A THIRD PARTY, DAMAGES RESULTING FROM
 ACCIDENT, ABUSE OR MISAPPLICATION, OR FOR PROBLEMS DUE TO THE MALFUNCTION OF
 YOUR EQUIPMENT OR SOFTWARE NOT SUPPLIED BY WABASOFT.
 */
 
 package waba.ui;
 
 import waba.fx.*;
 import waba.sys.Vm;
 
 
 /**
  * Window is a "floating" top-level window. This class IS functional like a modal window.
  * The following example creates an popup window class:
  <pre>
    class TestWindow extends Window
    {
       Button btnHi;
       public TestWindow()
       {
          super("Test",true); // with caption and borders
          setRect(CENTER,CENTER,100,50);
          add(btnHi=new Button("Hi!"));
          btnHi.setRect(CENTER,CENTER,PREFERRED,PREFERRED);
       }
       public void onEvent(Event event)
       {
          if (event.type == ControlEvent.PRESSED && event.target == btnHi)
             unpop(); // a WINDOW_CLOSED event will be posted to this PARENT window by the Window class.
       }
    }
  </pre>
  * To use it:
  <pre>
     class Main extends MainWindow
     {
        TestWindow tw;
        public void onStart()
        {
           popupModal(tw = new TestWindow());
        }
        public void onEvent(Event event)
        {
           if (event.target == tw && event.type == ControlEvent.WINDOW_CLOSED)
           {
              tw = null; // or any other stuff
           }
        }
     }
  </pre>
  */
 
 public class Window extends Container implements ISurface
 {
    public static final byte NO_BORDER       = 0;
    public static final byte RECT_BORDER     = 1;
    public static final byte ROUND_BORDER    = 2;
    public static final byte TAB_BORDER      = 3;
    public static final byte TAB_ONLY_BORDER = 4;
 /** used to hide the showing state */
 public static final int HIDE_STATE = 1000;
 protected static KeyEvent _keyEvent = new KeyEvent();
 protected static PenEvent _penEvent = new PenEvent();
 protected static ControlEvent _controlEvent = new ControlEvent();
 
 protected boolean needsPaint;
 private int paintX, paintY, paintWidth, paintHeight;
 protected Graphics _g;
 private Control _focus;
 private boolean _inPenDrag;
 
 protected Image imgBuf; // guich@101
 protected Graphics gbuf; // guich@101
 private boolean doubleBuf; // guich@101
 private Control focusOnPopup; // last control that had focus when popup was called.
 
 protected String title; // guich@102
 protected boolean beepIfOut=true; // guich@102: cancels the event if the user type outside the window area. to be used in a future window manager
 static waba.util.Vector zStack = new waba.util.Vector(3); // guich@102
 private boolean visible; // guich@102
 int stateX=HIDE_STATE, stateY=HIDE_STATE; // guich@102
 protected Font titleFont = MainWindow.defaultFont.asBold(); // guich@110
 public boolean flicker = true; // guich@110
 protected byte style = NO_BORDER;
 
 /** Constructs a window. */
 public Window()
    {
    name = "Window" + controlCount++;
    _nativeCreate();
    }
 /** @deprecated */
 public Window(String title, boolean border) // guich@112
 {
    this(title,border?RECT_BORDER:NO_BORDER);
 }
 public Window(String title, byte style) // guich@112
 {
    this();
    this.title = title;
    this.style = style;
 }
 /** sets the title font */
 public void setTitleFont(Font titleFont)
 {
    this.titleFont = titleFont;
 }
 /** sets the title and call repaint. if you want a imediate repaint, call paintTitle. */
 public void setTitle(String title) // guich@102
 {
    this.title = title;
    repaint();
 }
 /** sets the border style. use the constants XXXX_BORDER. sets the flag border accordingly to the style. */
 public void setBorderStyle(byte style)
 {
    this.style = style;
    repaint();
 }
 
 private native void _nativeCreate();
 
 /** Sets true if we have to use an double buffer to paint the screen. only works in palm os 3.0 or greater.
     it automatically sets to off if palm os 2.0 is running. */
 public void setDoubleBuffer(boolean doubleBuf)
 {
    this.doubleBuf = doubleBuf;
 }
 
 /**
  * Sets focus to the given control. When a user types a key, the control with
  * focus get the key event. At any given time, only one control in a window
  * can have focus. Calling this method will cause a FOCUS_OUT control event
  * to be posted to the window's current focus control (if one exists)
  * and will cause a FOCUS_IN control event to be posted to the new focus
  * control.
  */
 public void setFocus(Control c)
    {
    if (_focus != null)
       {
       _controlEvent.type = ControlEvent.FOCUS_OUT;
       _controlEvent.target = _focus;
       _focus.postEvent(_controlEvent);
       }
    _focus = c;
    if (c != null)
       {
       _controlEvent.type = ControlEvent.FOCUS_IN;
       _controlEvent.target = c;
       c.postEvent(_controlEvent);
       }
    }
 
 /**
  * Returns the focus control for this window.
  * @see waba.ui.Window#setFocus
  */
 public Control getFocus()
    {
    return _focus;
    }
 
 /**
  * Adds a damage rectangle to the current list of areas that need
  * repainting.
  */
 protected void damageRect(int x, int y, int width, int height)
 {
    if (needsPaint)
       {
       int ax = x + width;
       int ay = y + height;
       int bx = paintX + paintWidth;
       int by = paintY + paintHeight;
       if (paintX < x)
          x = paintX;
       if (paintY < y)
          y = paintY;
       if (ax > bx)
          width = ax - x;
       else
          width = bx - x;
       if (ay > by)
          height = ay - y;
       else
          height = by - y;
       }
    // guich@102 : makes sure that the paint is inside the area of the control
    paintX = x;//(x >= this.x)?x:this.x;
    paintY = y;//(y >= this.y)?y:this.y;
   paintWidth = (width <= this.width)?width:this.width;
   paintHeight = (height <= this.height)?height:this.height;
    needsPaint = true;
 }
 
 /** called when the user clicks outside the bounds of this window. must return true if the event was handled, false otherwise. guich@1.2 */
 protected boolean onClickedOutside(int x, int y)
 {
    return false;
 }
 
 /**
  * Called by the VM to post key and pen events.
  */
 public void _postEvent(int type, int key, int x, int y, int modifiers, int timeStamp)
 {
    if (!isTopMost()) // if this isnt the top-most window, pass control to it. i must do this because the vm calls MainWindow._postEvent
    {
       getTopMost()._postEvent(type,key,x,y,modifiers,timeStamp);
       return;
    }
    if (200 <= type && type <= 203 && !contains(x,y)) // guich@102: user clicked outside the window?
    {
       if (!onClickedOutside(x,y)) // if clicked outside was not handled by this method...
          if (type == PenEvent.PEN_DOWN && beepIfOut) // alert him! - ds: i changed this accordingly to your comments about win32 problems
             Sound.beep();
       return;
    }
 
    if (Edit.keyboard.isVisible() // guich: if popup keyboard is active, deviate events to it
       && (type == PenEvent.PEN_DOWN || type == PenEvent.PEN_DRAG || type == PenEvent.PEN_MOVE || type == PenEvent.PEN_UP)
       && Edit.keyboard.handlePenEvent(type,x,y))
          return;
    Event event;
    if (type == KeyEvent.KEY_PRESS)
    {
       _keyEvent.type = type;
       _keyEvent.key = key;
       _keyEvent.modifiers = modifiers;
       event = _keyEvent;
       if (_focus == null)
          _focus = this;
    }
    else
    {
       // set focus to new control
       if (type == PenEvent.PEN_DOWN)
       {
          Control c = findChild(x-this.x,y-this.y);
          if (c != _focus)
             setFocus(c);
          _inPenDrag = true;
       }
       else if (type == PenEvent.PEN_MOVE && _inPenDrag)
          type = PenEvent.PEN_DRAG;
       else if (type == PenEvent.PEN_UP)
          _inPenDrag = false;
 
       _penEvent.type = type;
       _penEvent.x = x;
       _penEvent.y = y;
 
       // translate x, y to coordinate system of target
       Control c = _focus;
       while (c != null)
       {
          _penEvent.x -= c.x;
          _penEvent.y -= c.y;
          c = c.parent;
       }
 
       _penEvent.modifiers = modifiers;
       event = _penEvent;
    }
    event.target = _focus;
    event.timeStamp = timeStamp;
    if (_focus != null)
       _focus.postEvent(event);
    if (needsPaint)
       _doPaint();
 }
 
 /** returns the client rect, ie, the rect minus the border and title area */
 public Rect getClientRect()
 {
    Rect r = getRect();
    if (title != null)
    {
       int m = style!=NO_BORDER?(style==ROUND_BORDER?3:2):0;
       r = r.modifiedBy(m,m,-m*2,-(m+fm.getHeight()));
    }
    return r;
 }
 /** paints the title immediatly. */
 public void paintTitle(String title, Graphics gg)
 {
    if (title != null || style != NO_BORDER)
    {
       if (title == null) title = "   ";
       gg.setColor(0,0,0);
       FontMetrics fm2 = getFontMetrics(titleFont);
       int ww = fm2.getTextWidth(title);
       int hh = fm2.getHeight();
       int xx = (this.width-ww)/2, yy = 1;
       if (style != NO_BORDER)
       {
          int y0 = style == RECT_BORDER?0:hh;
          if (style != TAB_ONLY_BORDER) gg.drawRect(0,y0,this.width,this.height-y0);
          switch (style)
          {
             case TAB_BORDER:
             case TAB_ONLY_BORDER:
                gg.setColor(0, 0, 0);
                /* Draws the tab */
                gg.drawLine(1, 0, ww+2, 0);
                gg.fillRect(0, 1, ww+4, hh);
                /* Draws the line */
                gg.fillRect(0, hh, width, 2);
                /* Draws text */
                xx = 3;
                break;
             case ROUND_BORDER:
                gg.fillRect(0,0,this.width,hh); // black border, white text
                gg.drawRect(1,y0+1,this.width-2,this.height-y0-2); // draws inside rect
                // erases bottom pixels
                gg.setColor(255,255,255);
                gg.drawRect(0,this.height-2,2,2); // erase bottom left
                gg.drawRect(this.width-2,this.height-2,2,2); // erase bottom right
                gg.drawRect(0,0,2,2); // erase top left
                gg.drawRect(this.width-2,0,2,2); // erase top right
                gg.setColor(0,0,0);
                gg.drawLine(1,this.height-2,2,this.height-3); // draw pixels bottom left /
                gg.drawLine(this.width-2,this.height-2,this.width-3,this.height-3); // draw pixels bottom right \
                gg.drawLine(1,1,1,1);
                gg.drawLine(this.width-2,1,this.width-2,1);
                break;
             case RECT_BORDER:
             default:
                gg.fillRect(0,0,this.width,hh+2); // black border, white text
                break;
          }
          gg.setColor(255,255,255);
       }
       gg.setFont(titleFont);
       gg.drawText(title,xx,yy);
       gg.setFont(font);
    }
    this.title = title;
 }
 /**
  * Called by the VM to repaint an area. If doubleBuf is true, paints the whole screen in a buffer and draws it on screen.
  */
 public void _doPaint(int x, int y, int width, int height) // x and y are coords relative to this window
 {
    if (imgBuf == null && doubleBuf) // guich
    {
       imgBuf = new Image(this.width,this.height);
       gbuf = new Graphics(imgBuf);
    }
    if (_g == null) _g = createGraphics();
 
    Graphics gg = doubleBuf?gbuf:_g;
    gg.setClip(x, y, width, height);
    // clear background
    if (Vm.isColor())
       gg.setColor(200, 200, 200);
    else
       gg.setColor(255, 255, 255);
    if (flicker) gg.fillRect(x, y, width, height); // guich@110
 
    // guich@102: if border or title, draw it
    if (style != NO_BORDER || title != null) gg.setColor(0,0,0);
    paintTitle(title,gg);
    onPaint(gg);
    gg.clearClip();
    paintChildren(gg, x, y, width, height);
    if (doubleBuf) _g.copyRect(imgBuf,x,y,width,height,x,y);
 
    if (needsPaint)
    {
       int ax = x + width;
       int ay = y + height;
       int bx = paintX + paintWidth;
       int by = paintY + paintHeight;
       if (x <= paintX && y <= paintY && ax >= bx && ay >= by)
       {
          needsPaint = false;
          onWindowPaintFinished();
          if (_focus != null) _focus.onWindowPaintFinished();
       }
    }
 }
 
 // here to below added by guich@102
 void resetPaintArea()
 {
    paintX = 0;
    paintY = 0;
    paintWidth = width;
    paintHeight = height;
 }
 /** used when a popup window is closed to repaint all windows behind it */
 void drawBack()
 {
    if (doubleBuf)
       _g.copyRect(imgBuf,x,y,width,height,x,y);
    else
       _doPaint();
 }
 /** popup a modal window, and make it child of this one. all events in the behind window are deactivated */
 final public void popupModal(Window another)
 {
    focusOnPopup = _focus;
    if (focusOnPopup == null) focusOnPopup = this;
    another.onPopup();
    _doPaint();
    setEnabled(false); // disables this window
    zStack.push(another);
    setFocus(another);
    another.setEnabled(true); // enable the new window
    another.resetPaintArea();
    another._doPaint();
    another.postPopup();
 }
 /** hides this window. if there are no more windows, exit is called. */
 final public void unpop()
 {
    onUnpop();
    setEnabled(false);
    zStack.pop();
    Window lastWin = getTopMost();
    if (lastWin != null)
    {
       for (int i =0; i < zStack.getCount()-1; i++) // redraws each window, from first to last parent
          ((Window)zStack.get(i)).drawBack();
       lastWin.setEnabled(true);
       lastWin.focusOnPopup.postEvent(new ControlEvent(ControlEvent.WINDOW_CLOSED,this)); // tell last control that we closed
       lastWin.resetPaintArea();
       lastWin._doPaint();
       lastWin.setStatePosition(lastWin.stateX,lastWin.stateY);
       lastWin.setFocus(lastWin.focusOnPopup);
       postUnpop();
    } else MainWindow.getMainWindow().exit(0); // no more windows: exit app
 }
 /** paints the top-level window. called by MainWindow */
 public void _doPaint()
 {
    if (!isTopMost())
       getTopMost()._doPaint();
    else
       _doPaint(paintX, paintY, paintWidth, paintHeight);
 }
 /** returns true if this window is the top-level window */
 protected boolean isTopMost()
 {
    return this == zStack.peek();
 }
 public String toString()
 {
    return name+" - "+title;
 }
 /** returns the topmost window */
 static public Window getTopMost()
 {
    return (Window)zStack.peek();
 }
 /** called imediatly before the popup began. the default implementation does nothing. */
 protected void onPopup()
 {
 }
 /** called after the popup is done and after the repaint of this window. the default implementation does nothing. */
 protected void postPopup()
 {
 }
 /** called imediatly before the unpop began. the default implementation does nothing. */
 protected void onUnpop()
 {
 }
 /** called after the unpop is done and after the repaint of the other window. the default implementation does nothing. */
 protected void postUnpop()
 {
 }
 /** this method does nothing in this class */
 public void setVisible(boolean visible)
 {
 }
 /** true if this is the topmost window */
 public boolean isVisible()
 {
    return this == getTopMost();
 }
 /** used to set the position where the status chars will be displayed.
     the chars are 8x11 pixels. this method must be called so the status can be displayed.
     these positions must be relative to this window, because they are converted to absolute coordinates.
     use setStatePosition(Window.HIDE_STATE,Window.HIDE_STATE) to remove the display of the state.
 */
 public void setStatePosition(int x, int y)
 {
    this.stateX = x;
    this.stateY = y;
    if (x < 0 || x > 150) x = HIDE_STATE;
    if (y < 0 || y > 150) y = HIDE_STATE;
    nativeSetStatePosition(this.x+x,this.y+y);
 }
 private native void nativeSetStatePosition(int x, int y);
 }
