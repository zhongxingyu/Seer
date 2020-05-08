 //  Copyright 2009 Wayne Keenan
 //  Copyright ##copyright## ##author##
 //
 //  Originally Licensed under the Apache License, Version 2.0 (the "License");
 //  you may not use this file except in compliance with the License.
 //  You may obtain a copy of the License at
 //
 //  http://www.apache.org/licenses/LICENSE-2.0
 //
 //  Unless required by applicable law or agreed to in writing, software
 //  distributed under the License is distributed on an "AS IS" BASIS,
 //  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 //  See the License for the specific language governing permissions and
 //  limitations under the License.
 //
 //  OSXmt.java
 //
 //  Created by Konstantin Fick on 22/08/2012.
 //
 //  mail@kofi.de.com
 //
 //
 //	This is a MODIFIED version of TouchpadObservable.java
 //  Created by Wayne Keenan on 27/05/2009.
 //  wayne.keenan@gmail.com
 //
 //  Added mouse cursor handling and Processing specific invoke methods
 
 
 package com.de.kofi.osxmt;
 
 
 import java.awt.AWTException;
 import java.awt.Rectangle;
 import java.awt.Robot;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import processing.core.*;
 
 /**
  * This library binds to OS X multitouch events and sends them to Processing
  *
  */
 
 public class OSXmt {
 	private static volatile Object initGuard;
 	private static volatile boolean loaded = false;
 
 	static {
 		initGuard = new Object();
 		System.loadLibrary("OSXmt");
 	}
 
 	public static int TAP_MAX_LIFETIME = 200;
 	public static double TAP_MAX_MOVEMENT = 0.03;
 	public static int LONG_TAP_MIN_LIFETIME = 600;
 	public static double LONG_PRESS_MAX_MOVEMENT = 0.03;
 	public static double SCROLL_MIN_MOVEMENT = 0.06;
 
 	private static PApplet p;
 	private static ArrayList<TouchListener> generalListeners = new ArrayList<TouchListener>();
 	private static Method mtEvent, mtPress, mtRelease, mtDrag, mtScroll, mtTap, mtLongPress;
 	private static Robot robot;
 
 	private static HashMap<Integer, TouchPoint> touches = new HashMap<Integer, TouchPoint>();
 	private static int frame;
 	private static volatile Object syncObj = new Object();
 
 	private static boolean catchCursor, needsFocus;
 
 	private static final OSXmt INSTANCE = new OSXmt();
 	private static volatile boolean initialized;
 	
 	private static String[] deviceList = null;
 
 	public final static String VERSION = "##library.prettyVersion##";
 
 	private OSXmt() { }
 
 	private static void startupNative(int deviceId) {		
 		synchronized (initGuard) {
 			if (!loaded) {
 				loaded = true;
 				registerListener(deviceId);
 				ShutdownHook shutdownHook = new ShutdownHook();
 				Runtime.getRuntime().addShutdownHook(shutdownHook);
 			}
 		}
 	}
 
 	private static void shutdownNative() {
 		deregisterListener();
 	}
 
 	@SuppressWarnings("unchecked")
 	public static void mtcallback(int frame, double timestamp, int id, int state, float size, float x, float y, float vx, float vy, float angle, float majorAxis, float minorAxis, int lastOfFrame) {
 		if(p == null || (needsFocus && !p.frame.isFocused()))
 			return;
 					
 		if((!needsFocus || (needsFocus && p.frame.isFocused())) && catchCursor) {
 			Rectangle r = p.frame.getBounds();
 			robot.mouseMove(r.x+p.width/2+p.getX(), r.y+p.height/2+p.getY());
 		}
 
 		TouchPoint t = new TouchPoint(frame, timestamp, id, state, size,  x,  y,  vx, vy, angle, majorAxis, minorAxis);
 		if(touches.containsKey(id)) {
 			touches.get(id).update(t);
 			t = touches.get(id);
 		}
 		else
 			touches.put(id, t);
 		
 		try {
 			if(t.getState() == PointState.TAP) {
 				if(mtPress != null)
 					mtPress.invoke(p, new Object[] { t.getId(), new PVector(t.getX(), t.getY()), touches.clone() });
 				
 				for(TouchListener listener:generalListeners)
 					listener.mtPress(t.getId(), new PVector(t.getX(), t.getY()), (HashMap<Integer, TouchPoint>) touches.clone());
 			}
 
 			else if(t.getState() == PointState.RELEASED) {
 				if(t.getLifetime() < TAP_MAX_LIFETIME && t.getAbsMovement() < TAP_MAX_MOVEMENT) {
 					if(mtTap != null)
 						mtTap.invoke(p, new Object[] { t.getId(), new PVector(t.getX(), t.getY()), touches.clone() });
 					
 					for(TouchListener listener:generalListeners)
 						listener.mtTap(t.getId(), new PVector(t.getX(), t.getY()), (HashMap<Integer, TouchPoint>) touches.clone());
 				}
 				
 				if(mtRelease != null)
 					mtRelease.invoke(p, new Object[] { t.getId(), new PVector(t.getX(), t.getY()), touches.clone() });
 				
 				for(TouchListener listener:generalListeners)
 					listener.mtRelease(t.getId(), new PVector(t.getX(), t.getY()), (HashMap<Integer, TouchPoint>) touches.clone());
 				
 				touches.remove(t.getId());
 			}
 
 			else if(t.getState() == PointState.PRESSED) {
 				if(mtDrag != null)
 					mtDrag.invoke(p, new Object[] { t.getId(), new PVector(t.getVelocityX(), t.getVelocityY()), touches.clone() });
 				
 				for(TouchListener listener:generalListeners)
 					listener.mtDrag(t.getId(), new PVector(t.getVelocityX(), t.getVelocityY()), (HashMap<Integer, TouchPoint>) touches.clone());
 				
 				if(t.getScrollState() == ScrollDirection.UNDEFINED && t.getAbsMovement() > SCROLL_MIN_MOVEMENT) {
 					if(Math.abs(t.getLerpDir().x) < Math.abs(t.getLerpDir().y))
 						t.setScrollState(ScrollDirection.VERTICAL);
 					else
 						t.setScrollState(ScrollDirection.HORIZONTAL);											
 				}
 				ScrollDirection dir = t.getScrollState();
 				if(dir != ScrollDirection.UNDEFINED) {
 					if(mtScroll != null)
 						mtScroll.invoke(p, new Object[] { t.getId(), dir, dir == ScrollDirection.VERTICAL ? t.getVelocityY() : t.getVelocityX(), touches.clone() });
 					
 					for(TouchListener listener:generalListeners)
 						listener.mtScroll(t.getId(), dir, dir == ScrollDirection.VERTICAL ? t.getVelocityY() : t.getVelocityX(), (HashMap<Integer, TouchPoint>) touches.clone());
 				}
 
 				if(!t.isLongPress() && t.getLifetime() > LONG_TAP_MIN_LIFETIME  && t.getLifetime() < LONG_TAP_MIN_LIFETIME+50 && t.getAbsMovement() < LONG_PRESS_MAX_MOVEMENT) {
 					t.setLongPress();
 					if(mtLongPress != null)
 						mtLongPress.invoke(p, new Object[] { t.getId(), new PVector(t.getX(), t.getY()), touches.clone() });
 					
 					for(TouchListener listener:generalListeners)
 						listener.mtLongPress(t.getId(), new PVector(t.getX(), t.getY()), (HashMap<Integer, TouchPoint>) touches.clone());
 				}
 			}
 			
 			if(lastOfFrame == 1/*&& t.getState() != PointState.HOVER &&
 					t.getState() != PointState.RELEASING &&
 					t.getState() != PointState.PRESSING &&
 					t.getState() != PointState.UNKNOWN &&
 					t.getState() != PointState.UNKNOWN_1*/) { // call general touch event
 				if(mtEvent != null)
 					mtEvent.invoke(p, touches.clone());
 				for(TouchListener listener:generalListeners)
 					listener.mtEvent((HashMap<Integer, TouchPoint>) touches.clone());			
 			}
 
 		} catch (IllegalArgumentException e) {
 			e.printStackTrace();
 		} catch (IllegalAccessException e) {
 			e.printStackTrace();
 		} catch (InvocationTargetException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// native methods	
 	native static int registerListener(int deviceId);
 	native static int deregisterListener();
 	native static String[] deviceList();
 
 	// shutdown hook
 	static class ShutdownHook extends Thread {
 		public void run() {
 			shutdownNative();
 		}
 	}
 	
	public static String[] getDeviceList() { //TODO
 		if(deviceList == null)
 			deviceList = deviceList();
 
 		return deviceList;
 	}
 
 	public static void init(PApplet _p, String deviceName, boolean _catchCursor, boolean _needsFocus) {
 		int deviceID = 0;
 		
 		for(int i = 0; i < deviceList.length; i++) {
 			if(deviceList[i].equalsIgnoreCase(deviceName)) {
 				deviceID = i;
 				break;
 			}	
 		}
 		init(_p, deviceID, _catchCursor, _needsFocus);
 	}
 	
 	public static void init(PApplet _p, int deviceId, boolean _catchCursor, boolean _needsFocus) {
 		if(initialized)
 			return;
 		initialized = true;
		
		startupNative(deviceId);
 		System.out.println("##library.name## ##library.prettyVersion## (##library.url##)");
 
 		p = _p;
 		catchCursor = _catchCursor;
 		needsFocus = _needsFocus;
 
 		try {
 			mtEvent = p.getClass().getMethod("mtEvent", touches.getClass());
 		} catch (SecurityException e) {} catch (NoSuchMethodException e) {}
 
 		try { // optional handler functions
 			mtPress = p.getClass().getMethod("mtPress", new Class[] { int.class, PVector.class, touches.getClass()});
 		} catch (SecurityException e) {} catch (NoSuchMethodException e) {}
 		try {
 			mtRelease = p.getClass().getMethod("mtRelease", new Class[] { int.class, PVector.class, touches.getClass()});
 		} catch (SecurityException e) {} catch (NoSuchMethodException e) {}
 		try {
 			mtDrag = p.getClass().getMethod("mtDrag", new Class[] { int.class, PVector.class, touches.getClass()});
 		} catch (SecurityException e) {} catch (NoSuchMethodException e) {}
 		try {
 			mtScroll = p.getClass().getMethod("mtScroll", new Class[] { int.class, ScrollDirection.class, float.class, touches.getClass()});
 		} catch (SecurityException e) {} catch (NoSuchMethodException e) {}
 		try {
 			mtTap = p.getClass().getMethod("mtTap", new Class[] { int.class, PVector.class, touches.getClass()});
 		} catch (SecurityException e) {} catch (NoSuchMethodException e) {}
 		try {
 			mtLongPress = p.getClass().getMethod("mtLongPress", new Class[] { int.class, PVector.class, touches.getClass()});
 		} catch (SecurityException e) {} catch (NoSuchMethodException e) {}
 
 		try {
 			robot = new Robot();
 		} catch (AWTException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void addGeneralListener(TouchListener listener) {
 		generalListeners.add(listener);
 	}
 	
 	public static void removeGeneralListener(TouchListener listener) {
 		generalListeners.remove(listener);
 	}
 
 	public static void setCatchCursor(boolean state) {
 		catchCursor = state;
 	}
 
 	public static boolean getCatchCursor() {
 		return catchCursor;
 	}
 
 	public static void setNeedsFocus(boolean state) {
 		needsFocus = state;
 	}
 
 	public static boolean getNeedsFocus() {
 		return needsFocus;
 	}
 
 	/**
 	 * return the version of the library.
 	 * 
 	 * @return String
 	 */
 	public static String version() {
 		return VERSION;
 	}
 }
 
