 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk, Maximilian Koegel - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.server.connection;
 
 import org.eclipse.emf.emfstore.internal.server.EMFStoreInterface;
 import org.eclipse.emf.emfstore.internal.server.accesscontrol.AccessControl;
 import org.eclipse.emf.emfstore.internal.server.exceptions.FatalESException;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 
 /**
  * The ConnectionHandler makes the network transport layer transparent for the server.
  * It requires {@link org.eclipse.emf.emfstore.internal.server.EMFStore} and
  * {@link org.eclipse.emf.emfstore.internal.server.accesscontrol.AuthenticationControl} to delegate the messages.
  * 
  * @param <T> server interface type E.g {@link org.eclipse.emf.emfstore.internal.server.EMFStore} or
  *            {@link org.eclipse.emf.emfstore.internal.server.AdminEmfStore}
  * @author Wesendonk
  * @author koegel
  */
 public interface ConnectionHandler<T extends EMFStoreInterface> {
 
 	/**
 	 * This method initializes the ConnectionHandler.
 	 * 
 	 * @param emfStore
 	 *            an implementation of a server interface
 	 * @param accessControl
 	 *            an implementation of the
 	 *            {@link org.eclipse.emf.emfstore.internal.server.accesscontrol.AuthenticationControl}
 	 * @throws FatalESException
 	 *             in case the server can't initialize
 	 * @throws ESException
 	 *             in case an exception occurred within the server
 	 */
 	void init(T emfStore, AccessControl accessControl) throws FatalESException, ESException;
 
 	/**
 	 * Stop the handler.
 	 */
	void stop();
 
 	/**
 	 * Return the handler name.
 	 * 
 	 * @return the name of the handler
 	 */
 	String getName();
 }
