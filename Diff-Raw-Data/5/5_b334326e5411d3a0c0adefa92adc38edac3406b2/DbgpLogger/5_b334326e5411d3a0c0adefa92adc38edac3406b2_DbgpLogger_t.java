 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.dbgp.internal.packets;
 
 public class DbgpLogger implements IDbgpLogger {
 
 	public void logInput(String input) {
		System.out.println("<<< " + input);
 	}
 
 	public void logOutput(String output) {
		System.out.println(">>> " + output);
 	}
 }
