 /*******************************************************************************
  * Copyright (c) 2007, 2011 compeople AG and others.
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
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.layout.RowLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.swt.widgets.Text;
 
 import org.eclipse.riena.internal.core.test.collect.UITestCase;
 import org.eclipse.riena.ui.swt.CompletionCombo.AutoCompletionMode;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 import org.eclipse.riena.ui.swt.utils.UIControlsFactory;
 
 /**
  * Tests of the class {@link CompletionCombo}.
  */
 @UITestCase
 public class CompletionComboTest extends TestCase {
 
 	private Shell shell;
 	private CompletionCombo combo;
 	private Text text;
 
 	@Override
 	protected void setUp() throws Exception {
 		super.setUp();
 		shell = new Shell();
 		shell.setLayout(new RowLayout());
 		shell.setBounds(0, 0, 200, 200);
 		combo = UIControlsFactory.createCompletionCombo(shell, SWT.NONE);
 		text = UIControlsFactory.createText(shell, SWT.BORDER);
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		SwtUtilities.dispose(shell);
 	}
 
 	// testing methods
 	//////////////////
 
 	public void testSetBackground() {
 		final Color red = combo.getDisplay().getSystemColor(SWT.COLOR_RED);
 		combo.setBackground(red);
 
 		assertEquals(red, combo.getBackground());
 		assertEquals(red, combo.getTextBackground());
 		assertEquals(red, combo.getListBackground());
 	}
 
 	public void testSetTextBackground() {
 		final Color red = combo.getDisplay().getSystemColor(SWT.COLOR_RED);
 		final Color green = combo.getDisplay().getSystemColor(SWT.COLOR_GREEN);
 		combo.setBackground(red);
 		combo.setTextBackground(green);
 
 		assertEquals(red, combo.getBackground());
 		assertEquals(green, combo.getTextBackground());
 		assertEquals(red, combo.getListBackground());
 	}
 
 	public void testSetListBackground() {
 		final Color red = combo.getDisplay().getSystemColor(SWT.COLOR_RED);
 		final Color green = combo.getDisplay().getSystemColor(SWT.COLOR_GREEN);
 		final Color blue = combo.getDisplay().getSystemColor(SWT.COLOR_GREEN);
 		combo.setBackground(red);
 		combo.setTextBackground(green);
 		combo.setListBackground(blue);
 
 		assertEquals(red, combo.getBackground());
 		assertEquals(green, combo.getTextBackground());
 		assertEquals(blue, combo.getListBackground());
 	}
 
 	/**
 	 * As per bug 318301
 	 */
 	public void testGetChildren() {
 		final Control[] children = combo.getChildren();
 
 		assertEquals(2, children.length);
 		assertTrue(children[0] instanceof Text);
 		assertTrue(children[1] instanceof Button);
 	}
 
 	public void testArrowButtonEnabled() {
 		shell.setEnabled(false);
 		combo.setEnabled(false);
 		combo.setEnabled(true);
 		combo.setEditable(true);
 		shell.setEnabled(true);
 
 		assertTrue(combo.isEnabled());
		final Text textControl = combo.getTextControl();
 		assertTrue(textControl.isEnabled());
		final Button buttonControl = combo.getButtonControl();
 		assertTrue(buttonControl.isEnabled());
 	}
 
 	/**
 	 * Tests the method {@code setFocus()}.
 	 */
 	public void testSetFocus() {
 
 		shell.open();
 
 		text.setFocus();
 		assertTrue(text.isFocusControl());
 
 		text.setFocus();
 		combo.setAutoCompletionMode(AutoCompletionMode.ALLOW_MISSMATCH);
 		combo.setFocus();
 		assertTrue(combo.getTextControl().isFocusControl());
 
 		text.setFocus();
 		combo.setAutoCompletionMode(AutoCompletionMode.FIRST_LETTER_MATCH);
 		combo.setFocus();
 		assertTrue(combo.getTextControl().isFocusControl());
 
 		text.setFocus();
 		combo.setAutoCompletionMode(AutoCompletionMode.NO_MISSMATCH);
 		combo.setFocus();
 		assertTrue(combo.getTextControl().isFocusControl());
 
 		text.setFocus();
 		combo.setEnabled(false);
 		combo.setFocus();
 		assertFalse(combo.getTextControl().isFocusControl());
 
 		text.setFocus();
 		combo.setEnabled(true);
 		combo.setVisible(false);
 		combo.setFocus();
 		assertFalse(combo.getTextControl().isFocusControl());
 
 	}
 
 }
