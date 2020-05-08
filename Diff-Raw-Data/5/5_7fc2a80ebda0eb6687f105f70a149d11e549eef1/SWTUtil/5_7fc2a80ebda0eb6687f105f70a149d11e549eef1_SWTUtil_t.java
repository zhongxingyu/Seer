 /*******************************************************************************
 * Copyright (c) 2012, 2013 Obeo.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Obeo - initial API and implementation
  *******************************************************************************/
 package org.eclipse.emf.compare.ide.ui.internal.util;
 
 import org.eclipse.swt.widgets.Display;
 
 /**
  * @author <a href="mailto:mikael.barbero@obeo.fr">Mikael Barbero</a>
  */
 public class SWTUtil {
 
 	public static void safeAsyncExec(final Runnable runnable) {
 		if (Display.getCurrent() != null) {
 			runnable.run();
 		} else {
 			Display.getDefault().asyncExec(runnable);
 		}
 	}
 
 	public static void safeSyncExec(final Runnable runnable) {
 		if (Display.getCurrent() != null) {
 			runnable.run();
 		} else {
			Display.getDefault().syncExec(runnable);
 		}
 	}
 }
