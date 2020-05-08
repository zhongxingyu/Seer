 /*******************************************************************************
  * Copyright (c) 2012 EclipseSource Muenchen GmbH.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Otto von Wesendonk
  * Edgar Mueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.dialogs.login;
 
 import java.util.List;
 import java.util.concurrent.Callable;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.emfstore.client.ESServer;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.client.util.RunESCommand;
 import org.eclipse.emf.emfstore.internal.client.model.ESWorkspaceProviderImpl;
 import org.eclipse.emf.emfstore.internal.client.model.Usersession;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESUsersessionImpl;
 import org.eclipse.emf.emfstore.internal.client.model.impl.api.ESWorkspaceImpl;
 import org.eclipse.emf.emfstore.internal.client.ui.common.RunInUI;
 import org.eclipse.emf.emfstore.internal.common.APIUtil;
 import org.eclipse.emf.emfstore.internal.server.exceptions.AccessControlException;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.jface.window.Window;
 import org.eclipse.swt.widgets.Display;
 
 /**
  * The login dialog controller manages a given {@link ESUsersession} and/or a {@link ESServer} to determine when it is
  * necessary to open a {@link LoginDialog} in order to authenticate the user. It does not, however,
  * open a dialog, if the usersession is already logged in.
  * 
  * @author ovonwesen
  * @author emueller
  */
 public class LoginDialogController implements ILoginDialogController {
 
 	private ESUsersession usersession;
 	private ESServer server;
 	private LoginDialog dialog;
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.dialogs.login.ILoginDialogController#getKnownUsersessions()
 	 */
 	public List<ESUsersession> getKnownUsersessions() {
 		return APIUtil.mapToAPI(
 			ESUsersession.class,
 			ESWorkspaceProviderImpl.getInstance().getWorkspace().toInternalAPI().getUsersessions());
 	}
 
 	private ESUsersession login(final boolean force) throws ESException {
 
 		if (server != null
 			&& server.getLastUsersession() != null
 			&& server.getLastUsersession().isLoggedIn()
 			&& !force) {
 			return server.getLastUsersession();
 		}
 
 		Integer userInput = RunInUI.runWithResult(new Callable<Integer>() {
 			public Integer call() throws Exception {
 				dialog = new LoginDialog(Display
 					.getCurrent().getActiveShell(),
 					LoginDialogController.this);
 				dialog.setBlockOnOpen(true);
 				return dialog.open();
 			}
 		});
 
		if (userInput != Window.OK) {
 			throw new AccessControlException("Couldn't login.");
 		}
 
 		final Usersession session = dialog.getSelectedUsersession();

		if (session == null) {
			throw new AccessControlException("Couldn't login.");
		}

 		final String password = dialog.getPassword();
 		final boolean savePassword = dialog.isSavePassword();
 		final boolean passwordModified = dialog.isPasswordModified();
 
 		RunESCommand.run(new Callable<Void>() {
 			public Void call() throws Exception {
 				session.setSavePassword(savePassword);
 				if (passwordModified) {
 					session.setPassword(password);
 				}
 				return null;
 			}
 		});
 
 		validate(session.toAPI());
 
 		// contract: #validate() sets the usersession;
 		return usersession;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.dialogs.login.ILoginDialogController#isUsersessionLocked()
 	 */
 	public boolean isUsersessionLocked() {
 		if (getUsersession() == null) {
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.dialogs.login.ILoginDialogController#getServerLabel()
 	 */
 	public String getServerLabel() {
 		return getServer().getName();
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.dialogs.login.ILoginDialogController#validate(org.eclipse.emf.emfstore.internal.client.model.Usersession)
 	 */
 	public void validate(final ESUsersession session) throws ESException {
 
 		final Usersession usersession = ((ESUsersessionImpl) session).toInternalAPI();
 		final ESWorkspaceImpl workspace = ESWorkspaceProviderImpl.getInstance().getWorkspace();
 		final EList<Usersession> usersessions = workspace.toInternalAPI().getUsersessions();
 
 		RunESCommand.WithException.run(ESException.class, new Callable<Void>() {
 			public Void call() throws Exception {
 				// TODO login code
 				usersession.logIn();
 				// if successful, else exception is thrown prior reaching this code
 				if (!usersessions.contains(usersession)) {
 					usersessions.add(usersession);
 				}
 				return null;
 			}
 		});
 
 		this.usersession = session;
 		// TODO OTS auto save
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.dialogs.login.ILoginDialogController#getUsersession()
 	 */
 	public ESUsersession getUsersession() {
 		return usersession;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.dialogs.login.ILoginDialogController#getServer()
 	 */
 	public ESServer getServer() {
 		if (server != null) {
 			return server;
 		}
 		return usersession.getServer();
 	}
 
 	/**
 	 * Perform a login using an {@link ESUsersession} that can be determined with
 	 * the given {@link ESServer}.
 	 * 
 	 * 
 	 * @param server
 	 *            the server info to be used in order to determine a valid
 	 *            usersession
 	 * @param force
 	 *            whether to force requesting the password
 	 * @return a logged-in usersession
 	 * @throws ESException
 	 *             in case the login fails
 	 */
 	public ESUsersession login(ESServer server, boolean force)
 		throws ESException {
 		this.server = server;
 		this.usersession = null;
 		return login(force);
 	}
 
 	/**
 	 * Perform a login using the given {@link ESUsersession}.
 	 * 
 	 * @param usersession
 	 *            the usersession to be used during login
 	 * @param force
 	 *            whether to force requesting the password
 	 * @throws ESException
 	 *             in case the login fails
 	 */
 	public void login(ESUsersession usersession, boolean force)
 		throws ESException {
 		this.server = null;
 		this.usersession = usersession;
 		login(force);
 	}
 
 	/**
 	 * Perform a login using an {@link ESUsersession} that can be determined with
 	 * the given {@link ESServer}.
 	 * 
 	 * 
 	 * @param server
 	 *            the server info to be used in order to determine a valid
 	 *            usersession
 	 * @return a logged-in usersession
 	 * @throws ESException
 	 *             in case the login fails
 	 */
 	public ESUsersession login(ESServer server) throws ESException {
 		this.server = server;
 		this.usersession = null;
 		return login(false);
 	}
 
 	/**
 	 * Perform a login using the given {@link ESUsersession}.
 	 * 
 	 * @param usersession
 	 *            the usersession to be used during login
 	 * @throws ESException
 	 *             in case the login fails
 	 */
 	public void login(ESUsersession usersession) throws ESException {
 		this.server = null;
 		this.usersession = usersession;
 		login(false);
 	}
 }
