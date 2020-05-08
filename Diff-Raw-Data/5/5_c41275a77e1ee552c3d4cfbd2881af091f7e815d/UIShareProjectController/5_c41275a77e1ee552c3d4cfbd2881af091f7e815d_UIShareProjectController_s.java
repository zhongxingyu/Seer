 /*******************************************************************************
  * Copyright (c) 2012-2013 EclipseSource Muenchen GmbH and others.
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Max Hohenegger (bug 371196)
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.controller;
 
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.emf.emfstore.client.ESLocalProject;
 import org.eclipse.emf.emfstore.client.ESUsersession;
 import org.eclipse.emf.emfstore.client.util.ESVoidCallable;
 import org.eclipse.emf.emfstore.internal.client.model.exceptions.LoginCanceledException;
 import org.eclipse.emf.emfstore.internal.client.ui.common.RunInUI;
 import org.eclipse.emf.emfstore.server.exceptions.ESException;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * UI-related controller class that shares a project and displays a progress bar during the share.
  * When finished a confirmation dialog is shown.
  * 
  * @author emueller
  * 
  */
 public class UIShareProjectController extends AbstractEMFStoreUIController<Void> {
 
 	private final ESLocalProject localProject;
 	private ESUsersession usersession;
 	private boolean loginHasBeenCancelled;
 	private boolean shareWasSuccessful;
 	private String shareErrorMessage;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that is used during the share
 	 * @param localProject
 	 *            the {@link ESLocalProject} that should be shared
 	 */
 	public UIShareProjectController(Shell shell, ESLocalProject localProject) {
 		super(shell, true, false);
 		this.localProject = localProject;
 	}
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param shell
 	 *            the parent {@link Shell} that is used during the share
 	 * @param usersession
 	 *            the {@link ESUsersession} that is used to share the project
 	 * @param localProject
 	 *            the {@link ESLocalProject} that should be shared
 	 */
 	public UIShareProjectController(Shell shell, ESUsersession usersession, ESLocalProject localProject) {
 		super(shell, true, true);
 		this.usersession = usersession;
 		this.localProject = localProject;
 	}
 
 	@Override
 	public void afterRun() {
 		if (loginHasBeenCancelled) {
 			// do nothing
 			return;
 		}
 
 		RunInUI.run(new ESVoidCallable() {
 			@Override
 			public void run() {
 				if (shareWasSuccessful) {
 					MessageDialog.openInformation(
 						getShell(),
 						Messages.UIShareProjectController_ShareSucceeded,
 						Messages.UIShareProjectController_SharedSucceeded_Message);
 				} else {
 					MessageDialog.openError(
 						getShell(),
 						Messages.UIShareProjectController_ShareFailed,
 						shareErrorMessage);
 				}
 			}
 		});
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 * 
 	 * @see org.eclipse.emf.emfstore.internal.client.ui.common.MonitoredEMFStoreAction#doRun(org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public Void doRun(final IProgressMonitor progressMonitor) throws ESException {
 		try {
 			localProject.shareProject(usersession != null ? usersession : null, progressMonitor);
 			shareWasSuccessful = true;
 		} catch (final LoginCanceledException e) {
 			// fail silently
 			loginHasBeenCancelled = true;
 		} catch (final ESException e) {
 			shareWasSuccessful = false;
 			shareErrorMessage = e.getMessage();
 		}
 
 		return null;
 	}
 }
