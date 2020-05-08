 /*******************************************************************************
  * Copyright (c) 2008-2011 Chair for Applied Software Engineering,
  * Technische Universitaet Muenchen.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * Zardosht Hodaie - initial API and implementation
  ******************************************************************************/
 package org.eclipse.emf.emfstore.internal.client.ui.dialogs.admin;
 
 import org.eclipse.emf.emfstore.internal.client.model.AdminBroker;
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * This is a dialog to manage users and groups (OrgUnits) and their access
  * rights for different projects.
  * 
  * @author Hodaie
  */
 public class ManageOrgUnitsDialog extends Dialog {
 
 	private final AdminBroker adminBroker;
 
 	/**
 	 * Constructor.
 	 * 
 	 * @param parentShell
 	 *            Shell
 	 * @param adminBroker
 	 *            AdminBroker class responsible for interaction with server
 	 *            side.
 	 */
 	public ManageOrgUnitsDialog(Shell parentShell, AdminBroker adminBroker) {
 
 		super(parentShell);
 
 		setShellStyle(getShellStyle() | SWT.RESIZE);
 		this.adminBroker = adminBroker;
 	}
 
 	/**
 	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages.ManageOrgUnitsDialog_Title);

	}

	/**
	 * {@inheritDoc}
 	 */
 	@Override
 	public int open() {
 		getButton(OK).setVisible(false);
 		getButton(CANCEL).setText(Messages.ManageOrgUnitsDialog_Close);
 		return super.open();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		final Composite composite = (Composite) super.createDialogArea(parent);
 		new OrgUnitManagementGUI(composite, adminBroker);
 		return composite;
 	}
 }
