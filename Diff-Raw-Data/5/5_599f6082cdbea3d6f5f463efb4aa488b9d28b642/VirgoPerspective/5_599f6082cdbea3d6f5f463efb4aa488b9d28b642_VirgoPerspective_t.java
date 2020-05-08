 /*******************************************************************************
 * Copyright (c) 2009, 2012 SpringSource, a divison of VMware, Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
 *     SpringSource, a division of VMware, Inc. - initial API and implementation
  *******************************************************************************/
 
 package org.eclipse.virgo.ide.internal.ui;
 
 import org.eclipse.ui.IEditorReference;
 import org.eclipse.ui.IPageLayout;
 import org.eclipse.ui.IPerspectiveFactory;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.PlatformUI;
 
 public class VirgoPerspective implements IPerspectiveFactory {
 
 	public void createInitialLayout(IPageLayout layout) {
 		// Initially hide editor unless a server editor is open..
 		boolean hideEditor = true;
 		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
 		if (activeWorkbenchWindow != null && activeWorkbenchWindow.getActivePage() != null) {
 			IEditorReference[] editorReferences = activeWorkbenchWindow.getActivePage().getEditorReferences();
 			for (IEditorReference reference : editorReferences) {
 				if (reference.getId().equals("org.eclipse.wst.server.ui.editor")) {
 					hideEditor = false;
 					break;
 				}
 			}
 		}
 		layout.setEditorAreaVisible(!hideEditor);
 	}
 }
