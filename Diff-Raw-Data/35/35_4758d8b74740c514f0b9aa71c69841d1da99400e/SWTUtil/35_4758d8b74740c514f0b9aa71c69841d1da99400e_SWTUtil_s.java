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
 
 /**
  * Utility class for Standard Widget Toolkit (SWT).
  * 
  * @author Thaedrik <thaedrik@gmail.com>
  */
 public class SWTUtil {
 
 	public static final int BUTTON_STD_WIDTH = 100;
 
 	// Suppressing default constructor, ensuring non-instantiability.
	private SWTUtil() {
	}
 
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
 }
