 /*******************************************************************************
  * Copyright (c) 2011-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Edgar Mueller - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.server.accesscontrol.authentication.verifiers;
 
import org.apache.commons.lang.StringUtils;
 import org.eclipse.emf.emfstore.internal.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.internal.server.model.AuthenticationInformation;
 import org.eclipse.emf.emfstore.internal.server.model.ClientVersionInfo;
 import org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser;
 
 /**
  * @author emueller
  * 
  */
 public class EMFModelAuthenticationVerifier extends AbstractAuthenticationControl {
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.server.accesscontrol.authentication.verifiers.AbstractAuthenticationControl#verifyPassword(org.eclipse.emf.emfstore.internal.server.model.accesscontrol.ACUser,
 	 *      java.lang.String, java.lang.String)
 	 */
 	@Override
 	protected boolean verifyPassword(ACUser resolvedUser, String username, String password)
 		throws AccessControlException {
 
 		if (resolvedUser == null) {
 			// TODO: throw UserNotFoundException? -> Signature
 			return false;
 		}
 
		final String userPassword = resolvedUser.getPassword();

		if (userPassword == null && (password == null || password.equals(StringUtils.EMPTY))) {
			// no password set
			return true;
		}

 		return resolvedUser.getPassword().equals(password);
 	}
 
 	/**
 	 * Tries to login the given user.
 	 * 
 	 * @param resolvedUser
 	 *            the user instance as resolved by the user
 	 * @param username
 	 *            the username as determined by the client
 	 * @param password
 	 *            the password as entered by the client
 	 * @param clientVersionInfo
 	 *            the version of the client
 	 * @return an {@link AuthenticationInformation} instance holding information about the
 	 *         logged-in session
 	 * 
 	 * @throws AccessControlException in case the login fails
 	 */
 	@Override
 	public AuthenticationInformation logIn(ACUser resolvedUser, String username, String password,
 		ClientVersionInfo clientVersionInfo)
 		throws AccessControlException {
 
 		super.checkClientVersion(clientVersionInfo);
 		password = preparePassword(password);
 
 		if (verifySuperUser(username, password) || verifyPassword(resolvedUser, username, password)) {
 			return createAuthenticationInfo();
 		}
 
 		throw new AccessControlException();
 	}
 }
