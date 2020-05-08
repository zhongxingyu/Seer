 /***************************************************************************
  * Copyright (c) 2013 Codestorming.org.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Codestorming - initial API and implementation
  ****************************************************************************/
 package org.codestorming.eclipse.util.swt;
 
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
 
 /**
  * Utility class for Standard Widget Toolkit (SWT).
  * 
  * @author Thaedrik <thaedrik@gmail.com>
  */
 public class SWTUtil {
 
 	public static final int BUTTON_STD_WIDTH = 100;
 
 	// Suppressing default constructor, ensuring non-instantiability.
	private SWTUtil() {}
 
 	/**
 	 * Sets the minimum width of the given {@link Button button} to
 	 * {@link #BUTTON_STD_WIDTH}.
 	 * 
 	 * @param button
 	 * @param data
 	 */
 	public static void computeButton(Button button, GridData data) {
 		int width = button.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
 		if (width < BUTTON_STD_WIDTH) {
 			width = BUTTON_STD_WIDTH;
 		}
 		data.minimumWidth = width;
 		data.widthHint = width;
 		button.setLayoutData(data);
 	}

	/**
	 * Create and open an error {@link MessageBox} with the given parent {@code shell},
	 * {@code title} and {@code message}.
	 * 
	 * @param shell The parent shell of the message box.
	 * @param title The title of the message box or {@code null}.
	 * @param message The message of the message box.
	 */
	public static void errorMessageBox(Shell shell, String title, String message) {
		final MessageBox msgBox = new MessageBox(shell, SWT.OK | SWT.ICON_ERROR);
		if (title != null) {
			msgBox.setText(title);
		}
		msgBox.setMessage(message);
		msgBox.open();
	}

	/**
	 * Create and open an error {@link MessageBox} with the given parent {@code shell} and
	 * {@code message}.
	 * 
	 * @param shell The parent shell of the message box.
	 * @param message The message of the message box.
	 */
	public static void errorMessageBox(Shell shell, String message) {
		errorMessageBox(shell, null, message);
	}
 }
