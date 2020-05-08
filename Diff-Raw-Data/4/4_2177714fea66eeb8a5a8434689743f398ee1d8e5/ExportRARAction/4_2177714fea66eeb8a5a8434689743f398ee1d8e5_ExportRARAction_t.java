 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Mar 27, 2003
  *
  * To change this generated comment go to 
  * Window>Preferences>Java>Code Generation>Code and Comments
  */
 package org.eclipse.jst.j2ee.jca.ui.internal.actions;
 
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jst.j2ee.internal.actions.BaseAction;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEUIPlugin;
import org.eclipse.jst.j2ee.jca.ui.internal.util.JCAUIMessages;
 import org.eclipse.jst.j2ee.jca.ui.internal.wizard.ConnectorComponentExportWizard;
 import org.eclipse.swt.widgets.Shell;
 
 
 /**
  * @author jsholl
  * 
  * To change this generated comment go to Window>Preferences>Java>Code Generation>Code and Comments
  */
 public class ExportRARAction extends BaseAction {
 
	private String label = JCAUIMessages.getResourceString(IConnectorArchiveConstants.CONNECTOR_EXPORT_ACTION_LABEL);
 	private static final String ICON = "export_rar_wiz"; //$NON-NLS-1$
 
 	public ExportRARAction() {
 		super();
 		setText(label);
 		setImageDescriptor(J2EEUIPlugin.getDefault().getImageDescriptor(ICON));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.internal.common.actions.BaseAction#primRun(org.eclipse.swt.widgets.Shell)
 	 */
 	protected void primRun(Shell shell) {
 		ConnectorComponentExportWizard wizard = new ConnectorComponentExportWizard();
 		J2EEUIPlugin plugin = J2EEUIPlugin.getDefault();
 		wizard.init(plugin.getWorkbench(), selection);
 		wizard.setDialogSettings(plugin.getDialogSettings());
 
 		WizardDialog dialog = new WizardDialog(shell, wizard);
 		dialog.create();
 		dialog.open();
 	}
 
 }
