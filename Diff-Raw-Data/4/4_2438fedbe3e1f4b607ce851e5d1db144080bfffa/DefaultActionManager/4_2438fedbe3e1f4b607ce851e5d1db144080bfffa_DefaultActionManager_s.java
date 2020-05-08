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
 package org.eclipse.riena.internal.ui.ridgets.swt;
 
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Shell;
 
 import org.eclipse.riena.ui.ridgets.IActionRidget;
 import org.eclipse.riena.ui.ridgets.IDefaultActionManager;
 import org.eclipse.riena.ui.ridgets.IRidget;
 import org.eclipse.riena.ui.ridgets.IWindowRidget;
 import org.eclipse.riena.ui.swt.utils.SwtUtilities;
 
 /**
  * Manages the default button state for one or more ridgets. See
  * {@link IDefaultActionManager} for details.
  * 
  * @see IWindowRidget#addDefaultAction(IRidget, IActionRidget)
  * @see IDefaultActionManager
  * 
  * @since 2.0
  */
 public final class DefaultActionManager implements IDefaultActionManager, Listener {
 
 	private final IWindowRidget windowRidget;
 	private final Map<IRidget, IActionRidget> ridget2button;
 
 	private Map<Control, Button> control2button;
 	private Shell shell;
 	private Display display;
 
 	/**
 	 * Create a new {@link DefaultActionManager} instance
 	 * 
 	 * @param windowRidget
 	 *            a {@link IWindowRidget}; never null.
 	 */
 	public DefaultActionManager(final IWindowRidget windowRidget) {
 		Assert.isNotNull(windowRidget);
 		this.windowRidget = windowRidget;
 		ridget2button = new HashMap<IRidget, IActionRidget>(1);
 	}
 
 	/**
 	 * Callers can add one or more (focusRidget, actionRidget) pairs.
 	 * <p>
 	 * When the focusRidget's control, or one of the controls therein, obtains
 	 * the focus, the matching actionRidget will be set as the default action.
 	 * <p>
 	 * The matching algorithm works "inside-out", i.e. it will start with the
 	 * innermost widget and work upwards. It stops when the first match is
 	 * found.
 	 * 
 	 * @param focusRidget
 	 *            an {@link IRidget}; never null
 	 * @param actionRidget
 	 *            an {@link IActionRidget}; never null
 	 */
 	public void addAction(final IRidget focusRidget, final IActionRidget actionRidget) {
 		Assert.isNotNull(focusRidget);
 		Assert.isNotNull(actionRidget);
 		ridget2button.put(focusRidget, actionRidget);
 	}
 
 	public void activate() {
		if (control2button == null) {
 			shell = ((Control) windowRidget.getUIControl()).getShell();
 			display = shell.getDisplay();
 			control2button = new HashMap<Control, Button>();
 			for (final Entry<IRidget, IActionRidget> entry : ridget2button.entrySet()) {
 				final Control control = (Control) entry.getKey().getUIControl();
 				Assert.isNotNull(control);
 
 				final Button button = (Button) entry.getValue().getUIControl();
 				Assert.isNotNull(button);
 
 				control2button.put(control, button);
 			}
 			updateDefaultButton(display.getFocusControl());
 			display.removeFilter(SWT.FocusIn, this);
 			display.addFilter(SWT.FocusIn, this);
 		}
 	}
 
 	public void deactivate() {
 		if (display != null) {
 			display.removeFilter(SWT.FocusIn, this);
 			display = null;
 		}
 		if (shell != null) {
 			clearDefaultButton(shell);
 			shell = null;
 		}
 		control2button = null;
 	}
 
 	public void dispose() {
 		deactivate();
 		ridget2button.clear();
 	}
 
 	/**
 	 * @noreference This method is not intended to be referenced by clients.
 	 */
 	public void handleEvent(final Event event) {
 		if (SWT.FocusIn == event.type && event.widget instanceof Control) {
 			final Control control = (Control) event.widget;
 			updateDefaultButton(control);
 		}
 	}
 
 	// helping methods
 	//////////////////
 
 	private void clearDefaultButton(final Shell shell) {
 		if (!SwtUtilities.isDisposed(shell)) {
 			// the setDefaultButton(...) API is strange! The first call
 			// will just reset the saved button to null, the second call will 
 			// make null the default button
 			shell.setDefaultButton(null);
 			shell.setDefaultButton(null);
 		}
 	}
 
 	private Button findDefaultButton(final Control start) {
 		Button result = null;
 		Control control = start;
 		while (result == null && control != null) {
 			result = control2button.get(control);
 			control = control.getParent();
 		}
 		return result;
 	}
 
 	private void updateDefaultButton(final Control control) {
 		final Button button = findDefaultButton(control);
 		if (SwtUtilities.isDisposed(button)) {
 			clearDefaultButton(shell);
 			return;
 		}
 		if (button != shell.getDefaultButton()) {
 			// System.out.println("Focus on: " + event.widget + ", " + button);
 			shell.setDefaultButton(button);
 		}
 	}
 
 }
