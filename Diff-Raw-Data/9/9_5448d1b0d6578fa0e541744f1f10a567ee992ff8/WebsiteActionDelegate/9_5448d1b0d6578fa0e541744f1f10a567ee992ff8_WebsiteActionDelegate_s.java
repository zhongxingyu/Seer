 /*******************************************************************************
  * Copyright (c) 2011, 2012 Formal Mind GmbH and University of Dusseldorf.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Michael Jastram - initial API and implementation
  ******************************************************************************/
 package org.eclipse.rmf.reqif10.pror.editor.actions;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.rmf.reqif10.ReqIFToolExtension;
 import org.eclipse.ui.IEditorActionDelegate;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.eclipse.ui.PartInitException;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.browser.IWebBrowser;
 import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
 
 public class WebsiteActionDelegate implements IEditorActionDelegate,
 		IWorkbenchWindowActionDelegate {
 
 	public void setActiveEditor(IAction action, IEditorPart editor) {
 		// No action required.
 	}
 
 	/**
 	 * Opens the {@link ReqIFToolExtension} for the current
 	 * {@link EditingDomain}.
 	 */
 	public void run(IAction action) {
 		try {
 			IWebBrowser browser = PlatformUI.getWorkbench().getBrowserSupport()
 					.createBrowser(
 							IWorkbenchBrowserSupport.AS_EDITOR
 									| IWorkbenchBrowserSupport.STATUS,
 							"pror-user-manual",
 							"ProR at eclipse.org",
							"ProR at eclipse.org/rmf/pror");
 
			URL url = new URL("http://eclipse.org/rmf/pror");
 			browser.openURL(url);
 		} catch (PartInitException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void selectionChanged(IAction action, ISelection selection) {
 		// No action required.
 	}
 
 	public void dispose() {
 	}
 
 	public void init(IWorkbenchWindow window) {
 	}
 
 }
