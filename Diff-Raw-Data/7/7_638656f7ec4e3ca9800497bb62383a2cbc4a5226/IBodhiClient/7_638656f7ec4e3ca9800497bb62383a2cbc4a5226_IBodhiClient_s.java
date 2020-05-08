 /*******************************************************************************
  * Copyright (c) 2010-2011 Red Hat Inc. and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat Inc. - initial API and implementation
  *******************************************************************************/
 package org.fedoraproject.eclipse.packager.bodhi.api;
 
 import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientException;
 import org.fedoraproject.eclipse.packager.bodhi.api.errors.BodhiClientLoginException;
 
 /**
  * Interface for Bodhi clients.
  */
 public interface IBodhiClient {
 
 	/**
 	 * Bodhi login with username and password.
 	 * 
 	 * @param username
 	 * @param password
 	 * @return The parsed response from the server or {@code null}.
 	 * @throws BodhiClientLoginException If some error occurred.
 	 */
 	public BodhiLoginResponse login(String username, String password)
 			throws BodhiClientLoginException;
 
 	/**
 	 * Push a new Bodhi update for one or more builds (i.e. N-V-Rs).
 	 * 
 	 * @param builds N-V-R's for which to push an update for
	 * @param release
	 * @param type
 	 * @param request {@code testing} or {@code stable}.
	 * @param bugs Numbers of bugs to close automatically.
 	 * @param notes The comment for this update.
 	 * @param csrf_token
 	 * @return The update response.
 	 * @throws BodhiClientException
 	 *             If some error occurred.
 	 */
 	public BodhiUpdateResponse createNewUpdate(String[] builds, String release,
 			String type, String request, String bugs, String notes, String csrf_token)
 			throws BodhiClientException;
 
 	/**
 	 * Log out from Bodhi server.
 	 * 
 	 * @throws BodhiClientException
 	 *             If an error occurred.
 	 * 
 	 */
 	public void logout() throws BodhiClientException;
 
 }
