 /*******************************************************************************
  * Copyright (c) 2007, 2014 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.ui.swt.utils;
 
 import junit.framework.TestCase;
 
 import org.easymock.EasyMock;
 import org.junit.Test;
 
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.core.test.collect.UITestCase;
 import org.eclipse.riena.ui.swt.utils.MouseWheelAdapter.Scroller;
 
 /**
  * Tests for the class {@link MouseWheelAdapter}
  */
 @UITestCase
 public class MouseWheelAdapterTest extends TestCase {
 	private static final int OS_SETTING_MOUSE_WHEEL = 3;
 
 	private Display display;
 	private Shell shell;
 	private Scroller scroller;
 	private MouseWheelAdapter mouseWheelAdapter;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		display = Display.getDefault();
 		shell = new Shell(display);
		shell.open();
 
 		scroller = EasyMock.createMock(Scroller.class);
 		mouseWheelAdapter = new MouseWheelAdapter(shell, scroller);
 	}
 
 	@Override
 	protected void tearDown() {
 		SwtUtilities.dispose(shell);
 	}
 
 	public void testSetNegativeScrollingSpeed() throws Exception {
 		try {
 			mouseWheelAdapter.setScrollingSpeed(-1);
 			fail("Exception expected - setting a negative scrolling speed is not allowed!"); //$NON-NLS-1$
 		} catch (final IllegalArgumentException e) {
 			// everything is ok
 		}
 	}
 
 	@Test
 	public void testSetScrollingSpeed() throws Exception {
 		EasyMock.expect(scroller.mayScroll()).andReturn(true);
 		final int speed = 15;
 		mouseWheelAdapter.setScrollingSpeed(speed);
 		scroller.scrollUp(OS_SETTING_MOUSE_WHEEL * speed);
 		EasyMock.replay(scroller);
 
 		final Event event = new Event();
 		event.time = 1;
 		event.widget = shell;
 		event.count = OS_SETTING_MOUSE_WHEEL;
		event.x = shell.getLocation().x + 1;
		event.y = shell.getLocation().y + 1;
 		mouseWheelAdapter.handleEvent(event);
 
 		EasyMock.verify(scroller);
 	}
 }
