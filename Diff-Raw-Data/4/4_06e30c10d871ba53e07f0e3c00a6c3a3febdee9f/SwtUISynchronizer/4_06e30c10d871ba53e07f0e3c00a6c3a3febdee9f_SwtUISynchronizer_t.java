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
 package org.eclipse.riena.ui.swt.uiprocess;
 
 import org.eclipse.riena.ui.core.uiprocess.IUISynchronizer;
 
 /**
  * serializes a runnable to the SWT-Thread
  */
 public class SwtUISynchronizer implements IUISynchronizer {
 
 	public void synchronize(Runnable r) {
		// Display.getDefault().syncExec(r); TODO [ev] temporary hack to 'fix' race condition 
 	}
 
 }
