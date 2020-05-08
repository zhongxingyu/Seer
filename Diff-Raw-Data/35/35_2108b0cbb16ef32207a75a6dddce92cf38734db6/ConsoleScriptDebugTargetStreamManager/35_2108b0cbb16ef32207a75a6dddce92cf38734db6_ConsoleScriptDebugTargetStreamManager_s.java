 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.debug.ui;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import org.eclipse.dltk.internal.debug.core.model.IScriptDebugTargetStreamManager;
 import org.eclipse.dltk.internal.debug.core.model.IScriptThreadStreamProxy;
 import org.eclipse.ui.console.IOConsole;
 
 public class ConsoleScriptDebugTargetStreamManager implements
 		IScriptDebugTargetStreamManager {
 	private IOConsole console;
 
 	public ConsoleScriptDebugTargetStreamManager(IOConsole console) {
 		this.console = console;
 	}
 
 	public IScriptThreadStreamProxy makeThreadStreamProxy() {
 		final InputStream input = console.getInputStream();
 		final OutputStream output = console.newOutputStream();
 
 		return new IScriptThreadStreamProxy() {
 
 			public OutputStream getStderr() {
 				return output;
 			}
 
 			public OutputStream getStdout() {
 				return output;
 			}
 
 			public InputStream getStdin() {
 				return input;
 			}
 		};
 	}
 }
