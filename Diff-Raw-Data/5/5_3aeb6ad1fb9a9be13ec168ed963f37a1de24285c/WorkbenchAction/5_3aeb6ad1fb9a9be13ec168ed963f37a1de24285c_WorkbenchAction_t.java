 /*******************************************************************************
  * Copyright (c) 2005 Ed Burnette, Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: Ed Burnette, Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.example.rcpchat.actions;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.eclipse.ecf.core.ContainerDescription;
 import org.eclipse.ecf.example.rcpchat.wizard.ConnectWizard;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.actions.ActionDelegate;
 
 public class WorkbenchAction extends ActionDelegate implements IWorkbenchWindowActionDelegate {
     
     public void run() {
     }
 
     protected IWorkbench getWorkbench() {
         return PlatformUI.getWorkbench();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
      */
     public void dispose() {
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
      */
     public void init(IWorkbenchWindow window) {
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
      */
     public void run(IAction action) {
     	String namespaceName = "ecf.xmpp.smack";
     	String namespaceDescription = "XMPP (Jabber)";
     	Map namespaceProps = new HashMap();
     	namespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage.usepassword","true");
     	namespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage.examplegroupid","<user>@<xmppserver>");
     	namespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage..defaultgroupid","");
     	namespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage.urlprefix","xmpp:");
     	namespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage.groupIDLabel","Account:");
     	namespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage.namespace",namespaceName);
     	ContainerDescription desc1 = new ContainerDescription(namespaceName,namespaceDescription,namespaceProps);
     	
    	String snamespaceName = "ecf.xmpps.smack";
     	String snamespaceDescription = "XMPP SSL (Secure Jabber)";
     	
     	Map snamespaceProps = new HashMap();
     	snamespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage.usepassword","true");
     	snamespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage.examplegroupid","<user>@<xmppserver>");
     	snamespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage..defaultgroupid","");
    	snamespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage.urlprefix","xmpps:");
     	snamespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage.groupIDLabel","Account:");
     	snamespaceProps.put("org.eclipse.ecf.ui.wizards.JoinGroupWizardPage.namespace",snamespaceName);
     	ContainerDescription desc2 = new ContainerDescription(snamespaceName,snamespaceDescription,snamespaceProps);
     	
         ConnectWizard wizard = new ConnectWizard(getWorkbench(), "Connect to Server",
 				new ContainerDescription[] { desc1, desc2 });
         // Create the wizard dialog
         WizardDialog dialog = new WizardDialog
          (getWorkbench().getActiveWorkbenchWindow().getShell(),wizard);
         // Open the wizard dialog
         dialog.open();
     }
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
      */
     public void selectionChanged(IAction action, ISelection selection) {
     }
 }
