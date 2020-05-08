 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
 * Contributors: 
  * emueller
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.controller;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.client.ESServer;
 import org.eclipse.emf.emfstore.internal.client.model.ServerInfo;
 import org.eclipse.emf.emfstore.internal.client.ui.dialogs.login.LoginDialogController;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * UI controller for logging a session.
  * 
  * @author emueller
  * 
  */
 public class UILoginSessionController extends AbstractEMFStoreUIController<Void> {
 
 	private final ESServer serverInfo;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that should be used during loggin in the given session
 	 * @param serverInfo
 	 *            the {@link ServerInfo} that is used to determine the server that is used to log in the session against
 	 */
 	public UILoginSessionController(Shell shell, ESServer serverInfo) {
 		super(shell);
 		this.serverInfo = serverInfo;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.common.MonitoredEMFStoreAction#doRun(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public Void doRun(IProgressMonitor monitor) throws ESException {
 		final LoginDialogController loginDialogController = new LoginDialogController();
 
 		try {
 			loginDialogController.login(serverInfo);
 		} catch (ESException e) {
			// don't show user that login failed, duh
 		}
 
 		return null;
 	}
 
 }
