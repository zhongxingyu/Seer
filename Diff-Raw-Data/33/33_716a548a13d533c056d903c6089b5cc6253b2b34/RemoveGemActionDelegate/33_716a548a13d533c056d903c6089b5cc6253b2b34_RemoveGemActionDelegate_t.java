 /*******************************************************************************
  * Copyright (c) 2005 RadRails.org and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package com.aptana.rdt.internal.ui.actions;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IViewActionDelegate;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 
 import com.aptana.rdt.AptanaRDTPlugin;
 import com.aptana.rdt.core.gems.Gem;
 import com.aptana.rdt.ui.gems.GemsMessages;
 import com.aptana.rdt.ui.gems.GemsView;
 import com.aptana.rdt.ui.gems.RemoveGemDialog;
 
 /**
  * Install a gem
  * 
  * @author cwilliams
  */
 public class RemoveGemActionDelegate implements IObjectActionDelegate, IViewActionDelegate {
 	
 	private IViewPart view;
 	private Gem selectedGem;
 
 	/**
 	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction,
 	 *      org.eclipse.ui.IWorkbenchPart)
 	 */
 	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
 
 	}
 
 	/**
 	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
 	 */
 	public void run(IAction action) {
 		boolean okay = MessageDialog.openConfirm(view.getViewSite()
 				.getShell(), null, GemsMessages.bind(GemsMessages.RemoveGemDialog_msg, selectedGem.getName()));
 		if (!okay) return;
 		Display.getDefault().asyncExec(new Runnable() {
 		
 			public void run() {
 				if (selectedGem.hasMultipleVersions()) {
 					RemoveGemDialog dialog = new RemoveGemDialog(Display.getDefault().getActiveShell(), selectedGem.versions());
 					if (dialog.open() == RemoveGemDialog.OK) {
 						AptanaRDTPlugin.getDefault().getGemManager().removeGem(new Gem(selectedGem.getName(), dialog.getVersion(), selectedGem.getDescription()));
 					}
 				} else {
 					AptanaRDTPlugin.getDefault().getGemManager().removeGem(selectedGem);		
 				}
 			}
 		
 		});
 	}
 
 	/**
 	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
 	 *      org.eclipse.jface.viewers.ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (view instanceof GemsView) {
 			if (selection != null && !selection.isEmpty()) {
 				if (selection instanceof IStructuredSelection) {
 					IStructuredSelection sel = (IStructuredSelection) selection;
 					Object element = sel.getFirstElement();
 					if (element instanceof Gem) {
 						this.selectedGem = (Gem) element;
 					}
 				}
 			}
 		}
 	}
 
 	/**
 	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
 	 */
 	public void init(IViewPart view) {
 		this.view = view;
 	}
 
 }
