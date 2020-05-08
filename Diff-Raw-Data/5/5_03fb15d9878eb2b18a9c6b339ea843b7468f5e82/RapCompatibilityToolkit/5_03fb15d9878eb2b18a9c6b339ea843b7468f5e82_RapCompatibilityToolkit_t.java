 /*******************************************************************************
  * Copyright (c) 2013 Olivier Moises
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Olivier Moises- initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.wazaabi.engine.swt.viewers;
 
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.wazaabi.engine.swt.commons.viewers.AbstractCompatibilityToolkit;
 import org.eclipse.wazaabi.engine.swt.commons.views.AbstractControlDecoration;
 
 public class RapCompatibilityToolkit extends AbstractCompatibilityToolkit {
 
 	@Override
 	public AbstractControlDecoration createControlDecoration(Control control,
 			int position) {
 		return new AbstractControlDecoration(control, position) {
 
			private static final long serialVersionUID = 1L;

 			@Override
 			public void updateDecoration() {
 				// NOTHING TO DO, Rap manages that
 			}
 		};
 	}
 
 	@Override
 	public int getSWT_RIGHT_TO_LEFT_Value() {
		return org.eclipse.swt.SWT.NONE;
 	}
 
 }
