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
 package org.eclipse.riena.ui.swt;
 
 import junit.framework.TestCase;
 
 import org.eclipse.riena.core.util.ReflectionUtils;
 import org.eclipse.riena.internal.core.test.collect.UITestCase;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
 
 /**
  * Tests of the class {@link RienaDialogDelegate}.
  */
 @UITestCase
 public class RienaDialogDelegateTest extends TestCase {
 
 	private Shell shell;
 	private RienaDialog dlg;
 	private RienaDialogDelegate delegate;
 
 	@Override
 	protected void setUp() throws Exception {
 		shell = new Shell();
 		dlg = new RienaDialog(shell);
 		delegate = new RienaDialogDelegate(dlg);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		delegate = null;
 		dlg = null;
 		SwtUtilities.disposeWidget(shell);
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code evaluateStyle()}.
 	 */
 	public void testEvaluateStyle() {
 
 		dlg.setShellStyle(SWT.MAX | SWT.MIN | SWT.RESIZE | SWT.CLOSE);
 		ReflectionUtils.invokeHidden(delegate, "evaluateStyle");
 		assertTrue(delegate.isCloseable());
 		assertTrue(delegate.isMaximizeable());
 		assertTrue(delegate.isMinimizeable());
 		assertTrue(delegate.isResizeable());
 
 		dlg.setShellStyle(SWT.CLOSE);
 		ReflectionUtils.invokeHidden(delegate, "evaluateStyle");
 		assertTrue(delegate.isCloseable());
 		assertFalse(delegate.isMaximizeable());
 		assertFalse(delegate.isMinimizeable());
 		assertFalse(delegate.isResizeable());
 
 		dlg.setShellStyle(SWT.MAX);
 		ReflectionUtils.invokeHidden(delegate, "evaluateStyle");
 		assertFalse(delegate.isCloseable());
 		assertTrue(delegate.isMaximizeable());
 		assertFalse(delegate.isMinimizeable());
 		assertFalse(delegate.isResizeable());
 
 		dlg.setShellStyle(SWT.MIN);
 		ReflectionUtils.invokeHidden(delegate, "evaluateStyle");
 		assertFalse(delegate.isCloseable());
 		assertFalse(delegate.isMaximizeable());
 		assertTrue(delegate.isMinimizeable());
 		assertFalse(delegate.isResizeable());
 
 		dlg.setShellStyle(SWT.RESIZE);
 		ReflectionUtils.invokeHidden(delegate, "evaluateStyle");
 		assertFalse(delegate.isCloseable());
 		assertFalse(delegate.isMaximizeable());
 		assertFalse(delegate.isMinimizeable());
 		assertTrue(delegate.isResizeable());
 
 	}
 
 	/**
 	 * Tests the <i>private</i> method {@code updateDialogStyle()}.
 	 */
 	public void testUpdateDialogStyle() {
 
 		delegate.setHideOsBorder(true);
 		dlg.setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE | SWT.APPLICATION_MODAL);
 		ReflectionUtils.invokeHidden(delegate, "updateDialogStyle");
 		int style = dlg.getShellStyle();
 		assertFalse((style & SWT.DIALOG_TRIM) == SWT.DIALOG_TRIM);
 		assertTrue((style & SWT.NO_TRIM) == SWT.NO_TRIM);
 		assertTrue((style & SWT.RESIZE) == SWT.RESIZE);
 		assertFalse((style & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL);
 
 		delegate.setHideOsBorder(false);
 		ReflectionUtils.invokeHidden(delegate, "setApplicationModal", true);
 		dlg.setShellStyle(SWT.DIALOG_TRIM | SWT.RESIZE);
 		ReflectionUtils.invokeHidden(delegate, "updateDialogStyle");
 		style = dlg.getShellStyle();
 		assertFalse((style & SWT.DIALOG_TRIM) == SWT.DIALOG_TRIM);
 		assertTrue((style & SWT.RESIZE) == SWT.RESIZE);
 		assertTrue((style & SWT.APPLICATION_MODAL) == SWT.APPLICATION_MODAL);
 
 	}
 
 }
