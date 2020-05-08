 /*******************************************************************************
  * Copyright (c) 2007, 2009 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.tests;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Widget;
 
 /**
  * Utility class for UI tests.
  */
 public final class UITestHelper {
 
 	/** Keycode for 'arrow down' (16777218) */
 	public static final int KC_ARROW_DOWN = 16777218;
 	/** Keycode for 'arrow up' (16777217) */
 	public static final int KC_ARROW_UP = 16777217;
 	/** Keycode for 'home' (16777223) */
 	public static final int KC_HOME = 16777223;
 	/** Keycode for 'end' (16777224) */
 	public static final int KC_END = 16777224;
 	/** Keycode for 'del' (127) */
 	public static final int KC_DEL = 127;
 
 	private UITestHelper() {
 		// prevent instantiation
 	}
 
 	public static void readAndDispatch(Widget control) {
 		Display display = control.getDisplay();
 		while (display.readAndDispatch()) {
 			// keep going
 		}
 	}
 
 	/**
 	 * Send a key event with the given keycode.
 	 * 
 	 * @param display
 	 *            a non-null {@link Display} instance
 	 * @param keyCode
 	 *            a SWT key code
 	 */
 	public static void sendKeyAction(Display display, int keyCode) {
 		EventSender sender = new EventSender(display, keyCode);
 		send(display, sender);
 	}
 
 	/**
 	 * Send the give message as a series of key events
 	 * 
 	 * @param display
 	 *            a non-null {@link Display} instance
 	 * @param message
 	 *            a non-null String
 	 */
 	public static void sendString(Display display, String message) {
 		EventSender sender = new EventSender(display, message);
 		send(display, sender);
 	}
 
 	private static void send(Display display, Runnable runnable) {
 		Thread thread = new Thread(runnable);
		//		Shell activeShell = display.getActiveShell();
		//		if (activeShell != null) {
		//			activeShell.forceActive();
		//		}
 		thread.start();
 		waitAndDispatch(display, thread);
 	}
 
 	private static void waitAndDispatch(Display display, Thread thread) {
 		Shell shell = display.getActiveShell();
 		while (!shell.isDisposed() && thread.isAlive()) {
 			display.readAndDispatch();
 		}
 	}
 
 	private static class EventSender implements Runnable {
 
 		private static final int MS_SHORT_WAIT = 10;
 		private static final int MS_LONG_WAIT = 250;
 
 		private final Display display;
 		private final int keyCode;
 		private final String message;
 
 		EventSender(Display display, int keyCode) {
 			this.display = display;
 			this.keyCode = keyCode;
 			this.message = null;
 		}
 
 		EventSender(Display display, String message) {
 			this.display = display;
 			this.keyCode = 0;
 			this.message = message;
 		}
 
 		public void run() {
 			if (message != null) {
 				sendMessage();
 			} else {
 				sendKeyEvent();
 			}
 		}
 
 		private void sendMessage() {
 			for (int i = 0; i < message.length(); i++) {
 				char ch = message.charAt(i);
 				boolean isShift = Character.isUpperCase(ch);
 				if (isShift) {
 					postShift(true);
 				}
 				postCharacter(ch);
 				if (isShift) {
 					postShift(false);
 				}
 			}
 		}
 
 		private void sendKeyEvent() {
 			Event event = new Event();
 			event.type = SWT.KeyDown;
 			event.keyCode = keyCode;
 			doSleep(MS_LONG_WAIT);
 			post(event);
 			doSleep(MS_LONG_WAIT);
 			event.type = SWT.KeyUp;
 			post(event);
 			doSleep(MS_LONG_WAIT);
 		}
 
 		private void postCharacter(char ch) {
 			Event event = new Event();
 			event.type = SWT.KeyDown;
 			event.character = ch;
 			doSleep(MS_LONG_WAIT);
 			post(event);
 			doSleep(MS_LONG_WAIT);
 			event.type = SWT.KeyUp;
 			post(event);
 			doSleep(MS_LONG_WAIT);
 		}
 
 		private void postShift(final boolean keyDown) {
 			Event event = new Event();
 			event.keyCode = SWT.SHIFT;
 			event.type = keyDown ? SWT.KeyDown : SWT.KeyUp;
 			post(event);
 			doSleep(MS_SHORT_WAIT);
 		}
 
 		private boolean post(final Event event) {
 			return display.post(event);
 		}
 
 		private void doSleep(int millis) {
 			try {
 				Thread.sleep(millis);
 			} catch (InterruptedException iex) {
 				// ignore
 			}
 		}
 
 	}
 
 }
