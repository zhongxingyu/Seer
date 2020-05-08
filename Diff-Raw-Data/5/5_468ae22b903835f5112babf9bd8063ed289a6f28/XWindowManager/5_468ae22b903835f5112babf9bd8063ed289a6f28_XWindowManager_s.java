 /**
  * Copyright (C) 2011 / cube-team <https://cube.forge.osor.eu>
  *
  * Licensed under the Apache License, Version 2.0 (the "License").
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *         http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package ch.admin.vbs.cube.client.wm.ui.x.imp;
 
 import java.awt.Color;
 import java.awt.Rectangle;
 import java.util.Collection;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.admin.vbs.cube.client.wm.ui.x.IWindowManagerCallback;
 import ch.admin.vbs.cube.client.wm.ui.x.IXWindowManager;
 import ch.admin.vbs.cube.client.wm.ui.x.imp.X11.Display;
 import ch.admin.vbs.cube.client.wm.ui.x.imp.X11.Window;
 import ch.admin.vbs.cube.client.wm.ui.x.imp.X11.WindowByReference;
 import ch.admin.vbs.cube.client.wm.ui.x.imp.X11.XConfigureEvent;
 import ch.admin.vbs.cube.client.wm.ui.x.imp.X11.XEvent;
 import ch.admin.vbs.cube.client.wm.ui.x.imp.X11.XTextProperty;
 import ch.admin.vbs.cube.client.wm.ui.x.imp.X11.XWindowAttributes;
 
 import com.sun.jna.NativeLong;
 import com.sun.jna.ptr.IntByReference;
 import com.sun.jna.ptr.PointerByReference;
 
 /**
  * The XWindowManager is a singleton implementation, that encapsulate the x11
  * calls from the xlib.
  */
 public final class XWindowManager implements IXWindowManager {
 	/** Logger */
 	private static final Logger LOG = LoggerFactory.getLogger(XWindowManager.class);
 	private static final int CHECKING_INTERVAL_FOR_NEW_EVENTS = 10;
 	private static final int OS_32_BIT = 32;
 	private static final int OS_64_BIT = 64;
 	private int osArchitectur = 0;
 	private String displayName = null;
 	private int screenIndex = 0;
 	private static XWindowManager instance;
 	private Thread eventThread = null;
 	private boolean destroyed = false;
 	private X11 x11;
 	/**
 	 * Display which must be held open during the whole life time, so that the
 	 * registered events can be caught. Otherwise all registered events will be
 	 * lost.
 	 */
 	private Display eventDisplay;
 	private IWindowManagerCallback cb;
 
 	/**
 	 * Creates the x window manager singleton instance.
 	 */
 	private XWindowManager() {
 	}
 
 	public void start() {
 		detectOperationSystemArchitecture();
 		detectDefaultDisplay();
 		// get Xlib reference
 		x11 = X11.INSTANCE;
 		// make it thread safe (..somewhat)
 		x11.XInitThreads();
 		eventDisplay = x11.XOpenDisplay(displayName);
 		// Start event processor thread
 		eventThread = new Thread(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					XEvent event = new XEvent();
 					while (!destroyed) {
 						try {
 							// check for new events
 							while (x11.XPending(eventDisplay) > 0) {
 								x11.XNextEvent(eventDisplay, event);
 								processEvent(event);
 							}
 							Thread.sleep(CHECKING_INTERVAL_FOR_NEW_EVENTS);
 						} catch (Exception e) {
 							LOG.error("Error during catching events", e);
 						}
 					}
 					x11.XCloseDisplay(eventDisplay);
 				} catch (Exception e) {
 					LOG.error("XEvent Thread Failed");
 				}
 			}
 		});
 		eventThread.start();
 		//
 		registerRootWindowEvents();
 		// check all existing window once at start
 		// for (Window w : listWindows()) {
 		// cb.windowUpdated(w);
 		// }
 	}
 
 	/**
 	 * Returns the singleton x window manager instance.
 	 * 
 	 * @return the x window manager instance
 	 */
 	public static synchronized IXWindowManager getInstance() {
 		if (instance == null) {
 			instance = new XWindowManager();
 		} else if (instance.destroyed) {
 			instance = new XWindowManager();
 		}
 		return instance;
 	}
 
 	@Override
 	public void reparentWindow(Window parentWindow, Window insideWindow) {
 		synchronized (XWindowManager.this) {
 			// register window for events
 			registerWindowForEvents(insideWindow);
 			// re-parent the x window and show this window alone
 			reparentWindow(parentWindow, insideWindow, 0, 0, true);
 		}
 	}
 
 	@Override
 	public synchronized Window findWindowByTitle(String name) {
 		/**
 		 * Look in the root window if there is a window with this name
 		 */
 		Window foundWindow = null;
 		Display display = x11.XOpenDisplay(displayName);
 		// get the root window
 		Window rootWindow = x11.XRootWindow(display, screenIndex);
 		long[] childrenWindowIdArray = getChildrenList(display, rootWindow);
 		for (long windowId : childrenWindowIdArray) {
 			Window window = new Window(windowId);
 			// get window attributes
 			XWindowAttributes attributes = new XWindowAttributes();
 			x11.XGetWindowAttributes(display, window, attributes);
 			// get window title
 			XTextProperty windowTitle = new XTextProperty();
 			x11.XFetchName(display, window, windowTitle);
 			// filter windows with attributes which our windows do not have
 			if (!attributes.override_redirect && windowTitle.value != null) {
 				// LOG.debug("Scan windows [{}] [{}]",windowTitle.value,name);
 				if (name.equals(windowTitle.value)) {
 					foundWindow = window;
 					break;
 				}
 			}
 		}
 		// close display
 		x11.XCloseDisplay(display);
 		if (foundWindow == null) {
 			LOG.error("No XWindow found that match name [{}]", name);
 		}
 		return foundWindow;
 	}
 
 	private synchronized Window findRootWindow() {
 		Display display = x11.XOpenDisplay(displayName);
 		// get the root window
 		Window rootWindow = x11.XRootWindow(display, screenIndex);
 		x11.XCloseDisplay(display);
 		return rootWindow;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * ch.admin.vbs.cube.client.wm.x.XXWindowManager#showOnlyTheseWindow(java
 	 * .util.Collection, java.util.Collection)
 	 */
 	public synchronized void showOnlyTheseWindow(Collection<Window> hideWindowList, Collection<Window> showWindowList) {
 		// connection to the x server)
 		Display display = x11.XOpenDisplay(displayName);
 		if (showWindowList != null) {
 			// maps and sets all show window
 			for (Window window : showWindowList) {
				LOG.error("Raise window {}", window);
 				x11.XMapWindow(display, window);
 				x11.XMapRaised(display, window);
 			}
 		}
 		if (hideWindowList != null) {
 			// set all visible window hidden
 			for (Window window : hideWindowList) {
 				// get window attributes
 				XWindowAttributes attributes = new XWindowAttributes();
 				x11.XGetWindowAttributes(display, window, attributes);
 				if (attributes.map_state != X11.IsUnmapped) {
					LOG.error("Unmap window {}", window);
 					x11.XUnmapWindow(display, window);
 				}
 			}
 		}
 		// commit changes and close display
 		x11.XFlush(display);
 		x11.XCloseDisplay(display);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * ch.admin.vbs.cube.client.wm.x.XXWindowManager#createBorderWindow(ch.admin
 	 * .vbs.cube.client.wm.x.X11.Window, int, java.awt.Color, java.awt.Color,
 	 * java.awt.Rectangle)
 	 */
 	public Window createBorderWindow(Window parentWindow, int borderSize, Color borderColor, Color backgroundColor, Rectangle bounds) {
 		/*
 		 * connection to the x server and set resources permanent, otherwise
 		 * window would be destroyed by calling XCloseDisplay
 		 */
 		Display display = x11.XOpenDisplay(displayName);
 		x11.XSetCloseDownMode(display, X11.RetainPermanent);
 		/*
 		 * Create window which will hold the virtual machine window and paints a
 		 * border. This border window is a child of the parent window.
 		 */
 		Window borderWindow = x11.XCreateSimpleWindow(//
 				display, //
 				parentWindow, //
 				bounds.x, //
 				bounds.y, //
 				bounds.width - 2 * borderSize, //
 				bounds.height - 2 * borderSize, //
 				borderSize, //
 				borderColor.getRGB(), //
 				backgroundColor.getRGB());
 		LOG.debug("Bordered window created [{}]", borderWindow);
 		// Register win
 		registerWindowForEvents(borderWindow);
 		x11.XMapWindow(display, borderWindow);
 		// commit changes and close display
 		x11.XFlush(display);
 		x11.XCloseDisplay(display);
 		return borderWindow;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * ch.admin.vbs.cube.client.wm.x.XXWindowManager#removeWindow(ch.admin.vbs
 	 * .cube.client.wm.x.X11.Window)
 	 */
 	public void removeWindow(Window window) {
 		LOG.debug("Remove Window [{}]", window);
 		Display display = x11.XOpenDisplay(displayName);
 		x11.XDestroyWindow(display, window);
 		// commit changes and close display
 		x11.XFlush(display);
 		x11.XCloseDisplay(display);
 	}
 
 	/**
 	 * Reparents the inside window to the new parent window.
 	 * 
 	 * @param parentWindow
 	 *            the new parent of the inside window
 	 * @param childWindow
 	 *            the window which will be child of the new parent window
 	 * @param x
 	 *            the x position of the child window
 	 * @param y
 	 *            the y positon of the child window
 	 */
 	private void reparentWindow(Window parentWindow, Window childWindow, int x, int y, boolean modeInsert) {
 		Display display = x11.XOpenDisplay(displayName);
 		// Map the virtual machine window as child to the border window
 		x11.XReparentWindow(display, childWindow, parentWindow, x, y);
 		if (modeInsert) {
 			x11.XChangeSaveSet(display, childWindow, X11.SetModeInsert);
 		}
 		// commit changes and close display
 		x11.XFlush(display);
 		x11.XCloseDisplay(display);
 		LOG.debug(String.format("reparentWindow() - child[%s / %s]  parent[%s / %s] [%d x %d]\n", getWindowName(childWindow), childWindow,
 				getWindowName(parentWindow), parentWindow, x, y));
 	}
 
 	@Override
 	public void hideAndReparentToRoot(Window window) {
 		Display display = x11.XOpenDisplay(displayName);
 		// Map the virtual machine window as child to the border window
 		x11.XUnmapWindow(display, window);
 		Window rootWindow = x11.XRootWindow(display, screenIndex);
 		x11.XReparentWindow(display, window, rootWindow, 0, 0);
 		// commit changes and close display
 		x11.XFlush(display);
 		x11.XCloseDisplay(display);
 		LOG.debug("unmap and reparentWindow() - child[{} / {}]  to root\n", getWindowName(window), window);
 	}
 
 	@Override
 	public void reparentWindowAndResize(Window parentWindow, Window childWindow, Rectangle bounds) {
 		LOG.debug("Reparent window [{}] to parent [{}]", childWindow, parentWindow);
 		Display display = x11.XOpenDisplay(displayName);
 		// move border window to new CubeFrame
 		x11.XReparentWindow(display, childWindow, parentWindow, bounds.x, bounds.y);
 		// resize border window to the new CubeFrame size
 		x11.XMoveResizeWindow(display, childWindow, bounds.x, bounds.y, bounds.width, bounds.height);
 		x11.XFlush(display);
 		// close display
 		sendResizeEvent(display, childWindow, bounds);
 		x11.XCloseDisplay(display);
 	}
 
 	@Override
 	public synchronized void destroy() {
 		if (!destroyed) {
 			destroyed = true;
 			if (eventThread != null) {
 				eventThread.interrupt();
 				eventThread = null;
 			}
 		}
 	}
 
 	/**
 	 * Registers the window for all interesting events.
 	 * 
 	 * @param window
 	 *            the window which events will be catched
 	 */
 	private void registerWindowForEvents(Window window) {
 		// select the events that this window will get
 		// x11.XSelectInput(eventDisplay, window, new
 		// NativeLong(X11.ResizeRedirectMask | X11.EnterWindowMask ));
 		x11.XSelectInput(eventDisplay, window, new NativeLong(X11.ResizeRedirectMask | X11.EnterWindowMask | X11.SubstructureNotifyMask
 				| X11.StructureNotifyMask));
 		x11.XFlush(eventDisplay);
 	}
 
 	private void registerWindowForExtraEvents(Window window) {
 		// register for events
 		x11.XSelectInput(eventDisplay, window, new NativeLong(X11.PropertyChangeMask));
 		x11.XFlush(eventDisplay);
 	}
 
 	private void registerRootWindowEvents() {
 		Window root = findRootWindow();
 		// register for events
 		x11.XSelectInput(eventDisplay, root, new NativeLong(X11.SubstructureNotifyMask | X11.PropertyChangeMask | X11.StructureNotifyMask));
 		// x11.XSelectInput(eventDisplay, root, new
 		// NativeLong(X11.SubstructureNotifyMask | X11.PropertyChangeMask));
 		x11.XFlush(eventDisplay);
 	}
 
 	/**
 	 * Processes events which has been registered.
 	 * 
 	 * @param event
 	 *            the thrown event
 	 * @see XWindowManager#registerWindowForEvents(Window)
 	 */
 	private void processEvent(XEvent event) {
 		if (!destroyed) {
 			switch (event.type) {
 			case X11.ResizeRequest:
 				// Loads the correct type for the event and fills the
 				// attributes, just event.xresizerequest does not
 				// work!
 				X11.XResizeRequestEvent resizeRequest = (X11.XResizeRequestEvent) event.getTypedValue(X11.XResizeRequestEvent.class);
 				if (LOG.isDebugEnabled()) {
 					LOG.debug("ResizeRequest for window " + resizeRequest.window.longValue());
 				}
 				reactOnResizeRequest(resizeRequest.window, resizeRequest.width, resizeRequest.height);
 				break;
 			case X11.EnterNotify:
 				X11.XCrossingEvent enterWindowEvent = (X11.XCrossingEvent) event.getTypedValue(X11.XCrossingEvent.class);
 				// set focus
 				x11.XSetInputFocus(eventDisplay, enterWindowEvent.window, X11.RevertToParent, new NativeLong(0));
 			case X11.PropertyNotify: {
 				X11.XPropertyEvent xe = (X11.XPropertyEvent) event.getTypedValue(X11.XPropertyEvent.class);
 				String atomName = x11.XGetAtomName(eventDisplay, xe.atom);
 				if ("WM_NAME".equals(atomName)) {
 					notifyWindowUpdated(xe.window);
 				}
 			}
 				break;
 			case X11.CreateNotify: {
 				X11.XCreateWindowEvent xe = (X11.XCreateWindowEvent) event.getTypedValue(X11.XCreateWindowEvent.class);
 				registerWindowForExtraEvents(xe.window);
 				notifyWindowCreated(xe.window);
 			}
 				break;
 			case X11.ClientMessage: {
 				// noisy !!
 				// X11.XClientMessageEvent xe = (X11.XClientMessageEvent)
 				// event.getTypedValue(X11.XClientMessageEvent.class);
 				// String atomName = x11.XGetAtomName(eventDisplay,
 				// xe.message_type);
 				// LOG.debug("X11.ClientMessage [" + atomName + "] changed");
 			}
 				break;
 			case X11.DestroyNotify: {
 				X11.XDestroyWindowEvent xe = (X11.XDestroyWindowEvent) event.getTypedValue(X11.XDestroyWindowEvent.class);
 				notifyWindowDestroyed(xe.window);
 			}
 				break;
 			case X11.MapNotify: {
 				X11.XMapEvent xe = (X11.XMapEvent) event.getTypedValue(X11.XMapEvent.class);
 				XTextProperty windowTitle = new XTextProperty();
 				x11.XFetchName(eventDisplay, xe.window, windowTitle);
 			}
 				break;
 			case X11.ConfigureNotify:
 			case X11.UnmapNotify:
 			default:
 				// noisy !!
 				// LOG.error("Ignore XEvent [{}]", event.type);
 				break;
 			}
 		}
 	}
 
 	private void notifyWindowUpdated(Window window) {
 		cb.windowUpdated(window);
 	}
 
 	private void notifyWindowCreated(Window window) {
 		cb.windowCreated(window);
 	}
 
 	private void notifyWindowDestroyed(Window window) {
 		cb.windowDestroyed(window);
 	}
 
 	@Override
 	public String getWindowName(Window w) {
 		XTextProperty windowTitle = new XTextProperty();
 		x11.XFetchName(eventDisplay, w, windowTitle);
 		return windowTitle.value;
 	}
 
 	/**
 	 * Checks if the resized event resizes the window to the propriety size,
 	 * otherwise it resize it new with the inside width of his parent window.
 	 * 
 	 * @param window
 	 *            the window which was requested to be resized
 	 * @param width
 	 *            the width of the window, to be requested to be resized
 	 * @param height
 	 *            the heihgt of the window, to be requested to be resized
 	 */
 	private synchronized void reactOnResizeRequest(Window window, int width, int height) {
 		// prepare reference values
 		WindowByReference rootWindowRef = new WindowByReference();
 		WindowByReference parentWindowRef = new WindowByReference();
 		PointerByReference childrenPtr = new PointerByReference();
 		IntByReference childrenCount = new IntByReference();
 		// find the parent to the window
 		if (x11.XQueryTree(eventDisplay, window, rootWindowRef, parentWindowRef, childrenPtr, childrenCount) == 0) {
 			if (LOG.isErrorEnabled()) {
 				LOG.error("BadWindow - A value for a Window argument does not name a defined Window!");
 			}
 			return;
 		}
 		// get parent attributes (width and height)
 		XWindowAttributes parentAttributes = new XWindowAttributes();
 		x11.XGetWindowAttributes(eventDisplay, parentWindowRef.getValue(), parentAttributes);
 		int insideWidth = parentAttributes.width;
 		int insideHeight = parentAttributes.height;
 		// check if resize needs to be changed
 		if (width != insideWidth || height != insideHeight) {
 			x11.XMoveResizeWindow(eventDisplay, window, 0, 0, insideWidth, insideHeight);
 			x11.XFlush(eventDisplay);
 		} else {
 			LOG.debug("Ignore resize request.");
 			x11.XMoveResizeWindow(eventDisplay, window, 0, 0, insideWidth, insideHeight);
 			x11.XFlush(eventDisplay);
 		}
 	}
 
 	@Override
 	public void setWindowManagerCallBack(IWindowManagerCallback cb) {
 		this.cb = cb;
 	}
 
 	// private ArrayList<Window> listWindows() {
 	// ArrayList<Window> wins = new ArrayList<X11.Window>();
 	// Display display = x11.XOpenDisplay(displayName);
 	// // get the root window
 	// Window rootWindow = x11.XRootWindow(display, screenIndex);
 	// LOG.debug("---------------------------");
 	// long[] childrenWindowIdArray = getChildrenList(display, rootWindow);
 	// for (long windowId : childrenWindowIdArray) {
 	// Window window = new Window(windowId);
 	// // get window attributes
 	// XWindowAttributes attributes = new XWindowAttributes();
 	// x11.XGetWindowAttributes(display, window, attributes);
 	// // get window title
 	// XTextProperty windowTitle = new XTextProperty();
 	// x11.XFetchName(display, window, windowTitle);
 	// // filter windows with attributes which our windows do not have
 	// if (!attributes.override_redirect && windowTitle.value != null) {
 	// wins.add(window);
 	// }
 	// }
 	// LOG.debug("---------------------------");
 	// // close display
 	// x11.XCloseDisplay(display);
 	// return wins;
 	// }
 	// private void dumpWindows() {
 	// Display display = x11.XOpenDisplay(displayName);
 	// // get the root window
 	// Window rootWindow = x11.XRootWindow(display, screenIndex);
 	// LOG.debug("---------------------------");
 	//
 	// dumpWindowsRec(display, rootWindow, 0);
 	//
 	// LOG.debug("---------------------------");
 	// // close display
 	// x11.XCloseDisplay(display);
 	// }
 	// private void dumpWindowsRec(Display display, Window parent, int stage) {
 	// // get the root window
 	// long[] childrenWindowIdArray = getChildrenList(display, parent);
 	// if (childrenWindowIdArray.length == 0) {
 	// String spaces = "";
 	// for (int i = 0; i < stage; i++) {
 	// spaces += '-';
 	// }
 	// XTextProperty windowTitle = new XTextProperty();
 	// x11.XFetchName(display, parent, windowTitle);
 	// LOG.debug("DUMP " + spaces + " => [{}] [{}]", parent, windowTitle.value);
 	// } else {
 	// for (long windowId : childrenWindowIdArray) {
 	// Window window = new Window(windowId);
 	// dumpWindowsRec(display, window, stage+1);
 	// }
 	// }
 	// }
 	/**
 	 * Detectes and sets the current operation system architecture. Default is
 	 * 32Bit.
 	 */
 	private void detectOperationSystemArchitecture() {
 		String osBits = System.getProperty("os.arch");
 		LOG.debug("Detected architecture [{}]", osBits);
 		if ("amd64".equals(osBits)) {
 			osArchitectur = OS_64_BIT;
 		} else {
 			osArchitectur = OS_32_BIT;
 		}
 	}
 
 	/**
 	 * Detect and set the display name and the screen index. Default is ":0.0"
 	 * for display name and 0 for the screen index.
 	 */
 	private void detectDefaultDisplay() {
 		// get display name from system environment
 		displayName = System.getenv("DISPLAY");
 		if (LOG.isInfoEnabled()) {
 			LOG.info("Display name is '" + displayName + "'.");
 		}
 		if (displayName != null) {
 			// displayName is something like this ":0.0"
 			int dotIndex = displayName.indexOf(".");
 			if (dotIndex < 0) {
 				screenIndex = 0;
 			} else {
 				screenIndex = Integer.parseInt(displayName.substring(dotIndex + 1));
 			}
 		} else {
 			displayName = ":0.0";
 			screenIndex = 0;
 		}
 		if (LOG.isInfoEnabled()) {
 			LOG.info("Display name is '" + displayName + "' and screen index is '" + screenIndex + "'.");
 		}
 	}
 
 	/**
 	 * Returns all window IDs to the given parent window.
 	 * 
 	 * @param display
 	 *            the display for better performance
 	 * @param parentWindow
 	 *            the parent window for all children
 	 * @return a list of long which are window IDs
 	 */
 	private long[] getChildrenList(Display display, Window parentWindow) {
 		long[] childrenWindowIdArray = new long[] {};
 		// prepare reference values
 		WindowByReference rootWindowRef = new WindowByReference();
 		WindowByReference parentWindowRef = new WindowByReference();
 		PointerByReference childrenPtr = new PointerByReference();
 		IntByReference childrenCount = new IntByReference();
 		// find all children to the rootWindow
 		if (x11.XQueryTree(display, parentWindow, rootWindowRef, parentWindowRef, childrenPtr, childrenCount) == 0) {
 			LOG.error("BadWindow - A value for a Window argument does not name a defined Window!");
 			return childrenWindowIdArray;
 		}
 		// get all window id's from the pointer and the count
 		if (childrenCount.getValue() > 0) {
 			if (osArchitectur == OS_32_BIT) {
 				int[] intChildrenWindowIdArray = childrenPtr.getValue().getIntArray(0, childrenCount.getValue());
 				childrenWindowIdArray = new long[intChildrenWindowIdArray.length];
 				int index = 0;
 				for (int windowId : intChildrenWindowIdArray) {
 					childrenWindowIdArray[index] = windowId;
 					++index;
 				}
 			} else if (osArchitectur == OS_64_BIT) {
 				childrenWindowIdArray = childrenPtr.getValue().getLongArray(0, childrenCount.getValue());
 			} else {
 				if (LOG.isInfoEnabled()) {
 					LOG.warn("OS architecture is not supported or could not be mapped! Trying 32 bit os architecture.");
 				}
 				int[] intChildrenWindowIdArray = childrenPtr.getValue().getIntArray(0, childrenCount.getValue());
 				childrenWindowIdArray = new long[intChildrenWindowIdArray.length];
 				int index = 0;
 				for (int windowId : intChildrenWindowIdArray) {
 					childrenWindowIdArray[index] = windowId;
 					++index;
 				}
 			}
 		}
 		return childrenWindowIdArray;
 	}
 
 	private void sendResizeEvent(Display display, Window childWindow, Rectangle bounds) {
 		long[] childrenWindowIdArray = getChildrenList(display, childWindow);
 		for (long windowId : childrenWindowIdArray) {
 			Window window = new Window(windowId);
 			LOG.debug("---> sendResizeEvent() to [{}] name[{}]", window, getWindowName(window));
 			// Send an event to force application (VirtualBox) to resize. It is
 			// needed if we move the VM on a 2nd screen with another resolution
 			// than the first one.
 			NativeLong event_mask = new NativeLong(X11.ConfigureNotify);
 			XConfigureEvent event = new XConfigureEvent();
 			event.type = X11.ConfigureNotify;
 			event.display = display;
 			event.height = bounds.height;
 			event.width = bounds.width;
 			event.border_width = 0;
 			event.above = null;
 			event.override_redirect = 0;
 			x11.XSendEvent(display, window, 1, event_mask, event);
 		}
 	}
 }
