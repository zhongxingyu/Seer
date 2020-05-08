 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.ui.interpreters;
 
 import org.eclipse.dltk.launching.IInterpreterInstall;
 
 /**
  * This interface is implemented by clients of the
  * <code>AddInterpreterDialog</code>.
  */
 public interface IAddInterpreterDialogRequestor {
 
 	/**
 	 * Reply whether or not a new Interpreter of the specified name would
 	 * constitute a duplicate.
 	 * 
 	 * @param name
 	 *            the name of a potential new Interpreter
 	 * @return whether a new Interpreter with the specified name would be a
 	 *         duplicate Interpreter
 	 */
	public boolean isDuplicateName(String name,
			IInterpreterInstall editedInterpreter);
 
 	/**
 	 * Notification that a Interpreter has been added from the
 	 * <code>AddInterpreterDialog</code>.
 	 * 
 	 * @param Interpreter
 	 *            the added Interpreter
 	 */
 	public void interpreterAdded(IInterpreterInstall Interpreter);
 
 }
