 /*******************************************************************************
  * Copyright (c) 2005 RadRails.org and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 
 package com.aptana.rdt.internal.ui.actions;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IObjectActionDelegate;
 import org.eclipse.ui.IViewActionDelegate;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchPart;
 
 import com.aptana.rdt.AptanaRDTPlugin;
import com.aptana.rdt.internal.core.gems.GemManager;
 
 /**
  * Install a gem
  * 
  * @author cwilliams
  */
 public class UpdateAllActionDelegate implements IObjectActionDelegate, IViewActionDelegate {
 	
 
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
 		Display.getDefault().asyncExec(new Runnable() {
 		
 			public void run() {
 				AptanaRDTPlugin.getDefault().getGemManager().updateAll();		
 			}
 		
 		});		
 	}
 
 	/**
 	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
 	 *      org.eclipse.jface.viewers.ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 		
 	}
 
 	/**
 	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
 	 */
 	public void init(IViewPart view) {
 	}
 
 }
