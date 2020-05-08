 /**
  * rarity – An extensible tiling window manager
  * 
  * Copyright © 2013  Mattias Andrée (maandree@member.fsf.org)
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package rarity;
 
 
 /**
  * Mane class
  * 
  * @author  Mattias Andrée, <a href="mailto:maandree@member.fsf.org">maandree@member.fsf.org</a>
  */
 public class Rarity
 {
     /**
      * Whether Xinerama is used
      */
     public static boolean usingXinerama = false;
     
     
     
     /**
      * Constructor hiding
      */
     private Rarity()
     {
 	/* do nothing */
     }
     
     
     
     /**
      * Mane method
      * 
      * @param  args  Command line arguments excluding program name, the first is the library to load
      */
     public static void main(final String... args)
     {
 	try
 	{   System.load(args[0]);
 	}
 	catch (final Throwable err)
 	{   System.err.println("Rarity: Unable to load library: rarity");
 	    abort();
 	}
 	
 	final int SCREEN_INPUT = X11.EventMask.PROPERTY_CHANGE
 	                       | X11.EventMask.COLORMAP_CHANGE
	                       | X11.EventMask.SUBSTRUCTURE_REDIRECT
 	                       | X11.EventMask.SUBSTRUCTURE_NOTIFY
 	                       | X11.EventMask.STRUCTURE_NOTIFY;
 	
 	boolean abort = false;
 	try
 	{
 	    staticInit();
 	    setLocale();
 	    X11.openDisplay();
	    /* FIXME
 	    setXAtoms();
 	    usingXinerama = Xinerama.initialise();
 	    
 	    int screenCount;
 	    synchronized (Screen.screens)
 	    {   screenCount = X11.screenCount();
 		for (int i = 0; i < screenCount; i++)
 		{   X11.activateScreen(i);
 		    X11.selectRootInput(i, SCREEN_INPUT);
 		    Screen.screens.add(new Screen(X11.screenWidth(i), X11.screenHeight(i), 0, 0));
 		    Screen.ExistanceMessage e = new Screen.ExistanceMessage(Screen.ExistanceMessage.ADDED, i);
 		    Blackboard.getInstance(Screen.class).broadcastMessage(e);
 	    }   }
 	    
 	    for (int i = 0, n = usingXinerama ? Xinerama.screenCount() : X11.screenCount(); i < n; i++)
 		scanForWindows(i);
 	    
 	    X11.sync();
	    */
 	    eventLoop();
 	}
 	catch (final Throwable err)
 	{
 	    err.printStackTrace(System.err);
 	    abort = true;
 	}
 	finally
 	{
 	    synchronized (Screen.screens)
 	    {   Screen.ExistanceMessage e;
 		int screenCount = X11.screenCount();
 		for (int i = 0; i < screenCount; i++)
 		{   e = new Screen.ExistanceMessage(Screen.ExistanceMessage.REMOVING, i);
 		    Blackboard.getInstance(Screen.class).broadcastMessage(e);
 		    X11.selectRootInput(i, 0);
 		    X11.deactivateScreen(i);
 		    Screen.screens.remove(0);
 		    e = new Screen.ExistanceMessage(Screen.ExistanceMessage.REMOVED, i);
 		    Blackboard.getInstance(Screen.class).broadcastMessage(e);
 	    }   }
 	    Xinerama.terminate();
 	    X11.closeDisplay();
 	    if (abort)
 	    	abort();
 	}
     }
     
     
     /**
      * Intialise static variables
      */
     private static native void staticInit();
     
     
     /**
      * Set locale stuff
      */
     private static native void setLocale();
     // setlocale(LC_CTYPE, "");
     // if (XSupportsLocale())
     //   XSetLocaleModifiers("");
     
     
     /**
      * Sets X atoms
      */
     private static native void setXAtoms();
     // xa_string                  = XA_STRING;
     // xa_compound_text           = XInternAtom(dpy, "COMPOUND_TEXT",              False);
     // xa_utf8_string             = XInternAtom(dpy, "UTF8_STRING",                False);
     // 
     // wm_name                    = XInternAtom(dpy, "WM_NAME",                    False);
     // wm_state                   = XInternAtom(dpy, "WM_STATE",                   False);
     // wm_change_state            = XInternAtom(dpy, "WM_CHANGE_STATE",            False);
     // wm_protocols               = XInternAtom(dpy, "WM_PROTOCOLS",               False);
     // wm_delete                  = XInternAtom(dpy, "WM_DELETE_WINDOW",           False);
     // wm_take_focus              = XInternAtom(dpy, "WM_TAKE_FOCUS",              False);
     // wm_colormaps               = XInternAtom(dpy, "WM_COLORMAP_WINDOWS",        False);
     // 
     // _net_wm_pid                = XInternAtom(dpy, "_NET_WM_PID",                False);
     // _net_supported             = XInternAtom(dpy, "_NET_SUPPORTED",             False);
     // _net_wm_window_type        = XInternAtom(dpy, "_NET_WM_WINDOW_TYPE",        False);
     // _net_wm_window_type_dialog = XInternAtom(dpy, "_NET_WM_WINDOW_TYPE_DIALOG", False);
     // _net_wm_name               = XInternAtom(dpy, "_NET_WM_NAME",               False);
     
     
     /**
      * Exit with SIGABRT
      */
     public static native void abort();
     // abort();
     
     
     /**
      * Start event loop
      */
     public static native void eventLoop();
     
     
     
     /**
      * Gets the pointer to the atom for XA_STRING
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointerXA_STRING();
     // return (jlong)(void*)&xa_string;
     
     /**
      * Gets the pointer to the atom for XA_COMPOUND_TEXT
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointerXA_COMPOUND_TEXT();
     // return (jlong)(void*)&xa_compound_text;
     
     /**
      * Gets the pointer to the atom for XA_UTF8_STRING
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointerXA_UTF8_STRING();
     // return (jlong)(void*)&xa_utf8_string;
     
     
     /**
      * Gets the pointer to the atom for WM_NAME
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointerWM_NAME();
     // return (jlong)(void*)&wm_name;
     
     /**
      * Gets the pointer to the atom for WM_STATE
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointerWM_STATE();
     // return (jlong)(void*)&wm_state;
     
     /**
      * Gets the pointer to the atom for WM_CHANGE_STATE
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointerWM_CHANGE_STATE();
     // return (jlong)(void*)&wm_change_state;
     
     /**
      * Gets the pointer to the atom for WM_PROTOCOLS
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointerWM_PROTOCOLS();
     // return (jlong)(void*)&wm_protocols;
     
     /**
      * Gets the pointer to the atom for WM_DELETE
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointerWM_DELETE();
     // return (jlong)(void*)&wm_delete;
     
     /**
      * Gets the pointer to the atom for WM_TAKE_FOCUS
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointerWM_TAKE_FOCUS();
     // return (jlong)(void*)&wm_take_focus;
     
     /**
      * Gets the pointer to the atom for WM_COLORMAPS
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointerWM_COLORMAPS();
     // return (jlong)(void*)&wm_colormaps;
     
     
     /**
      * Gets the pointer to the atom for _NET_WM_PID
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointer_NET_WM_PID();
     // return (jlong)(void*)&_net_wm_pid;
     
     /**
      * Gets the pointer to the atom for _NET_SUPPORTED
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointer_NET_SUPPORTED();
     // return (jlong)(void*)&_net_supported;
     
     /**
      * Gets the pointer to the atom for _NET_WM_WINDOW_TYPE
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointer_NET_WM_WINDOW_TYPE();
     // return (jlong)(void*)&_net_wm_window_type;
     
     /**
      * Gets the pointer to the atom for _NET_WM_WINDOW_TYPE_DIALOG
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointer_NET_WM_WINDOW_TYPE_DIALOG();
     // return (jlong)(void*)&_net_wm_window_type_dialog;
     
     /**
      * Gets the pointer to the atom for _NET_WM_NAME
      * 
      * @param  The pointer to the atom
      */
     public static native long getAtomPointer_NET_WM_NAME();
     // return (jlong)(void*)&_net_wm_name;
     
     
     /**
      * Scan for existing windows
      * 
      * @param  screen  The index of the screen to scan
      */
     private static native void scanForWindows(int screen);
     // unsigned int i, n;
     // Window _root, _parent, *windows;
     // XQueryTree(display, RootWindow(display, screen), &_root, &_parent, &windows, &n);
     // for (i = 0; i < n; i++)
     //   {
     //     unsigned int width, height, _border, _depth;
     //     int _x, _y;
     //     XGetGeometry(display, *(windows + i), _root, &_x, &_y, &width, &height, _border, _depth);
     //     $invoke$ rarity.Rarity.newWindow((jint)*(windows + i), (jint)width, (jint)height);
     //   }
     // XFree(windows);
     
     
     /**
      * Invoked by native code when a new window has been found
      * 
      * @param  pointer  The pointer to the window
      * @param  width    The width of the window
      * @param  height   The height of the window
      */
     public static void newWindow(final int pointer, final int width, final int height)
     {
 	final Window window = new Window(width, height, pointer);
 	synchronized (Window.windows)
 	{
 	    final int index = Window.windows.size();
 	    Window.windows.add(window);
 	    Window.ExistanceMessage e = new Window.ExistanceMessage(Window.ExistanceMessage.ADDED, index);
 	    Blackboard.getInstance(Window.class).broadcastMessage(e);
 	}
     }
     
     
     public static void eventButtonPress(final long serial, final boolean sendEvent, final int time, final int window, final int root, final int subwindow, final int x, final int y, final int xRoot, final int yRoot, final int state, final int button, final boolean sameScreen)
     {
 	(new XEvent.RatButton(serial, sendEvent, time, window, root, subwindow, x, y, xRoot, yRoot, state, button, sameScreen, XEvent.RatButton.PRESSED)).broadcast();
     }
     
     
     public static void eventButtonRelease(final long serial, final boolean sendEvent, final int time, final int window, final int root, final int subwindow, final int x, final int y, final int xRoot, final int yRoot, final int state, final int button, final boolean sameScreen)
     {
 	(new XEvent.RatButton(serial, sendEvent, time, window, root, subwindow, x, y, xRoot, yRoot, state, button, sameScreen, XEvent.RatButton.RELEASED)).broadcast();
     }
     
     
     public static void eventClientMessage(final long serial, final boolean sendEvent, final int window, final long messageType, final int format, final byte[] data)
     {
 	(new XEvent.ClientMessage(serial, sendEvent, window, messageType, format, data)).broadcast();
     }
     
     
     public static void eventCirculateNotify(final long serial, final boolean sendEvent, final int event, final int window, final int place)
     {
 	(new XEvent.Circulate(serial, sendEvent, event, window, place, XEvent.Circulate.NOTIFY)).broadcast();
     }
     
     
     public static void eventCirculateRequest(final long serial, final boolean sendEvent, final int parent, final int window, final int place)
     {
 	(new XEvent.Circulate(serial, sendEvent, parent, window, place, XEvent.Circulate.REQUEST)).broadcast();
     }
     
     
     public static void eventColormapNotify(final long serial, final boolean sendEvent, final int window, final int colormap, final boolean isNew, final int state)
     {
 	(new XEvent.ColormapNotify(serial, sendEvent, window, colormap, isNew, state)).broadcast();
     }
     
     
     public static void eventConfigureNotify(final long serial, final boolean sendEvent, final int event, final int window, final int above, final int x, final int y, final int width, final int height, final int borderWidth, final boolean overrideRedirect)
     {
 	(new XEvent.ConfigureNotify(serial, sendEvent, event, window, above, x, y, width, height, borderWidth, overrideRedirect)).broadcast();
     }
     
     
     public static void eventConfigureRequest(final long serial, final boolean sendEvent, final long valueMask, final int parent, final int window, final int above, final int x, final int y, final int width, final int height, final int borderWidth, final int detail)
     {
 	(new XEvent.ConfigureRequest(serial, sendEvent, valueMask, parent, window, above, x, y, width, height, borderWidth, detail)).broadcast();
     }
     
     
     public static void eventCreateNotify(final long serial, final boolean sendEvent, final int parent, final int window, final int x, final int y, final int width, final int height, final int borderWidth, final boolean overrideRedirect)
     {
 	(new XEvent.CreateWindow(serial, sendEvent, parent, window, x, y, width, height, borderWidth, overrideRedirect)).broadcast();
     }
     
     
     public static void eventDestroyNotify(final long serial, final boolean sendEvent, final int event, final int window)
     {
 	(new XEvent.DestroyWindow(serial, sendEvent, event, window)).broadcast();
     }
     
     
     public static void eventEnterNotify(final long serial, final boolean sendEvent, final int window, final int root, final int subwindow, final int time, final int x, final int y, final int xRoot, final int yRoot, final int mode, final int detail, final boolean sameScreen, final boolean focus, final int state)
     {
 	(new XEvent.RatWindow(serial, sendEvent, window, root, subwindow, time, x, y, xRoot, yRoot, mode, detail, sameScreen, focus, state, XEvent.RatWindow.ENTER)).broadcast();
     }
     
     
     public static void eventExpose(final long serial, final boolean sendEvent, final int window, final int x, final int y, final int width, final int height, final int count)
     {
 	(new XEvent.Expose(serial, sendEvent, window, x, y, width, height, count)).broadcast();
     }
     
     
     public static void eventFocusIn(final long serial, final boolean sendEvent, final int window, final int mode, final int detail)
     {
 	(new XEvent.Focus(serial, sendEvent, window, mode, detail, XEvent.Focus.FOCUS_IN)).broadcast();
     }
     
     
     public static void eventFocusOut(final long serial, final boolean sendEvent, final int window, final int mode, final int detail)
     {
 	(new XEvent.Focus(serial, sendEvent, window, mode, detail, XEvent.Focus.FOCUS_OUT)).broadcast();
     }
     
     
     public static void eventGraphicsExpose(final long serial, final boolean sendEvent, final int drawable, final int x, final int y, final int width, final int height, final int count, final int majorCode, final int minorCode)
     {
 	(new XEvent.GraphicsExpose(serial, sendEvent, drawable, x, y, width, height, count, majorCode, minorCode)).broadcast();
     }
     
     
     public static void eventGravityNotify(final long serial, final boolean sendEvent, final int event, final int window, final int x, final int y)
     {
 	(new XEvent.Gravity(serial, sendEvent, event, window, x, y)).broadcast();
     }
     
     
     public static void eventKeymapNotify(final long serial, final boolean sendEvent, final int window, final byte[] keyVector)
     {
 	(new XEvent.Keymap(serial, sendEvent, window, keyVector)).broadcast();
     }
     
     
     public static void eventKeyPress(final long serial, final boolean sendEvent, final int time, final int window, final int root, final int subwindow, final int x, final int y, final int xRoot, final int yRoot, final int state, final int keycode, final boolean sameScreen)
     {
 	(new XEvent.Key(serial, sendEvent, time, window, root, subwindow, x, y, xRoot, yRoot, state, keycode, sameScreen, XEvent.Key.PRESSED)).broadcast();
     }
     
     
     public static void eventKeyRelease(final long serial, final boolean sendEvent, final int time, final int window, final int root, final int subwindow, final int x, final int y, final int xRoot, final int yRoot, final int state, final int keycode, final boolean sameScreen)
     {
 	(new XEvent.Key(serial, sendEvent, time, window, root, subwindow, x, y, xRoot, yRoot, state, keycode, sameScreen, XEvent.Key.RELEASED)).broadcast();
     }
     
     
     public static void eventLeaveNotify(final long serial, final boolean sendEvent, final int window, final int root, final int subwindow, final int time, final int x, final int y, final int xRoot, final int yRoot, final int mode, final int detail, final boolean sameScreen, final boolean focus, final int state)
     {
 	(new XEvent.RatWindow(serial, sendEvent, window, root, subwindow, time, x, y, xRoot, yRoot, mode, detail, sameScreen, focus, state, XEvent.RatWindow.LEAVE)).broadcast();
     }
     
     
     public static void eventMapNotify(final long serial, final boolean sendEvent, final int window, final boolean overrideRedirect)
     {
 	(new XEvent.MapNotify(serial, sendEvent, window, overrideRedirect)).broadcast();
     }
     
     
     public static void eventMappingNotify(final long serial, final boolean sendEvent, final int window, final int request, final int firstKeycode, final int count)
     {
 	(new XEvent.MappingNotify(serial, sendEvent, window, request, firstKeycode, count)).broadcast();
     }
     
     
     public static void eventMapRequest(final long serial, final boolean sendEvent, final int parent, final int window)
     {
 	(new XEvent.MapRequest(serial, sendEvent, parent, window)).broadcast();
     }
     
     
     public static void eventMotionNotify(final long serial, final boolean sendEvent, final int time, final int window, final int root, final int subwindow, final int x, final int y, final int xRoot, final int yRoot, final int state, final boolean sameScreen, final boolean isHint)
     {
 	(new XEvent.RatMove(serial, sendEvent, time, window, root, subwindow, x, y, xRoot, yRoot, state, sameScreen, isHint)).broadcast();
     }
     
     
     public static void eventNoExpose(final long serial, final boolean sendEvent, final int drawable, final int majorCode, final int minorCode)
     {
 	(new XEvent.NoExpose(serial, sendEvent, drawable, majorCode, minorCode)).broadcast();
     }
     
     
     public static void eventPropertyNotify(final long serial, final boolean sendEvent, final int window, final int time, final int state, final int atom)
     {
 	(new XEvent.PropertyNotify(serial, sendEvent, window, time, state, atom)).broadcast();
     }
     
     
     public static void eventReparentNotify(final long serial, final boolean sendEvent, final int event, final int window, final int parent, final int x, final int y, final boolean overrideRedirect)
     {
 	(new XEvent.ReparentNotify(serial, sendEvent, event, window, parent, x, y, overrideRedirect)).broadcast();
     }
     
     
     public static void eventResizeRequest(final long serial, final boolean sendEvent, final int event, final int window, final int width, final int height)
     {
 	(new XEvent.ResizeRequest(serial, sendEvent, event, window, width, height)).broadcast();
     }
     
     
     public static void eventSelectionClear(final long serial, final boolean sendEvent, final int window, final int time, final int selection)
     {
 	(new XEvent.SelectionClear(serial, sendEvent, window, time, selection)).broadcast();
     }
     
     
     public static void eventSelectionNotify(final long serial, final boolean sendEvent, final int requestor, final int time, final int selection, final int target, final int property)
     {
 	(new XEvent.SelectionNotify(serial, sendEvent, requestor, time, selection, target, property)).broadcast();
     }
     
     
     public static void eventSelectionRequest(final long serial, final boolean sendEvent, final int owner, final int requestor, final int time, final int selection, final int target, final int property)
     {
 	(new XEvent.SelectionRequest(serial, sendEvent, owner, requestor, time, selection, target, property)).broadcast();
     }
     
     
     public static void eventUnmapNotify(final long serial, final boolean sendEvent, final int event, final int window, final boolean fromConfigure)
     {
 	(new XEvent.UnmapNotify(serial, sendEvent, event, window, fromConfigure)).broadcast();
     }
     
     
     public static void eventVisibilityNotify(final long serial, final boolean sendEvent, final int window, final boolean state)
     {
 	(new XEvent.VisibilityNotify(serial, sendEvent, window, state)).broadcast();
     }
     
 }
 
