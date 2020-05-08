 /*******************************************************************************
  * Copyright (c) 2007, 2008 compeople AG and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    compeople AG - initial API and implementation
  *******************************************************************************/
 package org.eclipse.riena.communication.core.progressmonitor;
 
 /**
  * Abstract base class for implementing a progress monitor for remote services
  */
 public abstract class AbstractRemoteProgressMonitor implements IRemoteProgressMonitor {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.communication.core.IProgressMonitor#start()
 	 */
	public void start() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.communication.core.IProgressMonitor#end()
 	 */
	public void end() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.communication.core.IProgressMonitor#request(int,
 	 * int)
 	 */
 	public abstract void request(int bytes, int totalBytes);
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.riena.communication.core.IProgressMonitor#response(int,
 	 * int)
 	 */
 	public abstract void response(int bytes, int totalBytes);
 
 }
