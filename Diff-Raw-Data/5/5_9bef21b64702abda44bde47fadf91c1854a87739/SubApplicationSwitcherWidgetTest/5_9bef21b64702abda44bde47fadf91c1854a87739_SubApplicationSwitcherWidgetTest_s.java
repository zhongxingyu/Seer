 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.navigation.ui.swt.component;
 
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.navigation.IApplicationNode;
 import org.eclipse.riena.navigation.listener.SubApplicationNodeListener;
 import org.eclipse.riena.navigation.model.ApplicationNode;
 import org.eclipse.riena.navigation.model.NavigationProcessor;
 import org.eclipse.riena.navigation.model.SubApplicationNode;
 import org.eclipse.riena.tests.collect.UITestCase;
 import org.eclipse.riena.ui.core.marker.DisabledMarker;
 import org.eclipse.riena.ui.core.marker.HiddenMarker;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Tests of the class {@link SubApplicationSwitcherWidget}.
  */
 @UITestCase
 public class SubApplicationSwitcherWidgetTest extends TestCase {
 
 	private SubApplicationSwitcherWidget switcher;
 	private Shell shell;
 
 	@Override
 	protected void setUp() throws Exception {
 		shell = new Shell();
 		IApplicationNode node = new ApplicationNode();
 		switcher = new SubApplicationSwitcherWidget(shell, SWT.NONE, node);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		SwtUtilities.disposeWidget(switcher);
 		SwtUtilities.disposeWidget(shell);
 	}
 
 	/**
 	 * Tests the constructor of {@link SubApplicationSwitcherWidget}.
 	 */
 	public void testCreate() {
 
 		IApplicationNode node = new ApplicationNode();
 		SubApplicationNode subNode1 = new SubApplicationNode("sub1");
 		subNode1.setIcon("icon1");
 		List<SubApplicationNodeListener> listeners = ReflectionUtils.invokeHidden(subNode1, "getListeners",
 				(Object[]) null);
 		assertNotNull(listeners);
 		assertTrue(listeners.isEmpty());
 		node.addChild(subNode1);
 		SubApplicationNode subNode2 = new SubApplicationNode("sub2");
 		subNode2.setIcon("icon2");
 		node.addChild(subNode2);
 		switcher = new SubApplicationSwitcherWidget(shell, SWT.NONE, node);
 
 		List<SubApplicationItem> items = ReflectionUtils.invokeHidden(switcher, "getItems", (Object[]) null);
 		assertNotNull(items);
 		assertEquals(2, items.size());
 		assertEquals("sub1", items.get(0).getLabel());
 		assertEquals("sub2", items.get(1).getLabel());
 		assertEquals("icon1", items.get(0).getIcon());
 		assertEquals("icon2", items.get(1).getIcon());
 
 		listeners = ReflectionUtils.invokeHidden(subNode1, "getListeners", (Object[]) null);
 		assertNotNull(listeners);
 		assertFalse(listeners.isEmpty());
 		assertEquals(1, listeners.size());
 
 		Listener[] swtListeners = switcher.getListeners(SWT.Paint);
 		assertEquals(1, swtListeners.length);
 
 		swtListeners = switcher.getListeners(SWT.MouseDown);
 		assertEquals(1, swtListeners.length);
 
 	}
 
 	/**
 	 * Tests the method {@code isTabEnabled}.
 	 */
 	public void testIsTabEnabled() {
 
 		SubApplicationNode node = new SubApplicationNode();
 		node.setNavigationProcessor(new NavigationProcessor());
 		SubApplicationItem item = new SubApplicationItem(switcher, node);
 		boolean ret = ReflectionUtils.invokeHidden(switcher, "isTabEnabled", new Object[] { item });
 		assertTrue(ret);
 
 		DisabledMarker disabledMarker = new DisabledMarker();
 		node.addMarker(disabledMarker);
 		ret = ReflectionUtils.invokeHidden(switcher, "isTabEnabled", new Object[] { item });
 		assertFalse(ret);
 
 		HiddenMarker hiddenMarker = new HiddenMarker();
 		node.removeMarker(disabledMarker);
 		node.addMarker(hiddenMarker);
 		ret = ReflectionUtils.invokeHidden(switcher, "isTabEnabled", new Object[] { item });
 		assertFalse(ret);
 
 		node.removeMarker(hiddenMarker);
 		ret = ReflectionUtils.invokeHidden(switcher, "isTabEnabled", new Object[] { item });
 		assertTrue(ret);
 
 	}
 
 	/**
 	 * Tests the method {@code getItem}.
 	 */
 	public void testGetItem() {
 
 		SubApplicationNode node = new SubApplicationNode();
 		SubApplicationItem item = new SubApplicationItem(switcher, node);
 		List<SubApplicationItem> items = ReflectionUtils.invokeHidden(switcher, "getItems", (Object[]) null);
 
 		Point point = new Point(0, 0);
 		SubApplicationItem retItem = ReflectionUtils.invokeHidden(switcher, "getItem", new Object[] { point });
 		assertNull(retItem);
 
 		items.add(item);
 		retItem = ReflectionUtils.invokeHidden(switcher, "getItem", new Object[] { point });
 		assertNull(retItem);
 
 		point = new Point(5, 5);
 		item.setBounds(new Rectangle(0, 0, 10, 10));
 		retItem = ReflectionUtils.invokeHidden(switcher, "getItem", new Object[] { point });
 		assertNotNull(retItem);
 		assertSame(item, retItem);
 
 		SubApplicationNode node2 = new SubApplicationNode();
 		SubApplicationItem item2 = new SubApplicationItem(switcher, node2);
 		items.add(item2);
 		item2.setBounds(new Rectangle(20, 20, 10, 10));
 		point = new Point(20, 20);
 		retItem = ReflectionUtils.invokeHidden(switcher, "getItem", new Object[] { point });
 		assertNotNull(retItem);
 		assertSame(item2, retItem);
 
 		point = new Point(31, 31);
 		retItem = ReflectionUtils.invokeHidden(switcher, "getItem", new Object[] { point });
 		assertNull(retItem);
 
 	}
 
	@SuppressWarnings("restriction")
 	public void testDisposeSubApplication() throws Exception {
 
 		IApplicationNode node = new ApplicationNode();
 		SubApplicationNode subNode1 = new SubApplicationNode("sub1");
 		subNode1.setIcon("icon1");
 		node.addChild(subNode1);
 		SubApplicationNode subNode2 = new SubApplicationNode("sub2");
 		subNode2.setIcon("icon2");
 		node.addChild(subNode2);
 		switcher = new SubApplicationSwitcherWidget(shell, SWT.NONE, node);
 		List<SubApplicationItem> items = ReflectionUtils.invokeHidden(switcher, "getItems", (Object[]) null);
 
 		subNode1.dispose();
 
 		assertEquals(1, items.size());
 
 		subNode2.dispose();
 
 		assertTrue(items.isEmpty());
 	}
 
	@SuppressWarnings("restriction")
 	public void testRemoveSubApplication() throws Exception {
 
 		IApplicationNode node = new ApplicationNode();
 		SubApplicationNode subNode1 = new SubApplicationNode("sub1");
 		subNode1.setIcon("icon1");
 		node.addChild(subNode1);
 		SubApplicationNode subNode2 = new SubApplicationNode("sub2");
 		subNode2.setIcon("icon2");
 		node.addChild(subNode2);
 		switcher = new SubApplicationSwitcherWidget(shell, SWT.NONE, node);
 		List<SubApplicationItem> items = ReflectionUtils.invokeHidden(switcher, "getItems", (Object[]) null);
 
 		node.removeChild(subNode1);
 
 		assertEquals(1, items.size());
 
 		node.removeChild(subNode2);
 
 		assertTrue(items.isEmpty());
 	}
 
	@SuppressWarnings("restriction")
 	public void testAddSubApplication() throws Exception {
 
 		IApplicationNode node = new ApplicationNode();
 		SubApplicationNode subNode1 = new SubApplicationNode("sub1");
 		subNode1.setIcon("icon1");
 		node.addChild(subNode1);
 		SubApplicationNode subNode2 = new SubApplicationNode("sub2");
 		subNode2.setIcon("icon2");
 		node.addChild(subNode2);
 		switcher = new SubApplicationSwitcherWidget(shell, SWT.NONE, node);
 		List<SubApplicationItem> items = ReflectionUtils.invokeHidden(switcher, "getItems", (Object[]) null);
 
 		SubApplicationNode subNode3 = new SubApplicationNode("sub3");
 		subNode2.setIcon("icon3");
 		node.addChild(subNode3);
 
 		assertEquals(3, items.size());
 	}
 
 }
