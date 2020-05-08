 /*******************************************************************************
  * Copyright (c) 2010 BestSolution.at and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Tom Schindl <tom.schindl@bestsolution.at> - initial API and implementation
  ******************************************************************************/
 package org.eclipse.e4.tools.emf.liveeditor;
 
 import javax.inject.Named;
 
 import org.eclipse.e4.tools.emf.ui.internal.wbm.ApplicationModelEditor;
 import org.eclipse.e4.ui.model.application.MApplication;
 import org.eclipse.e4.ui.services.IServiceConstants;
 import org.eclipse.swt.layout.FillLayout;
 import org.eclipse.swt.widgets.Shell;
 
 public class OpenLiveDialogHandler {
 	private Shell shell;
 	
 	public OpenLiveDialogHandler() {
 		System.err.println("Created Live Dialog Handler");
 	}
 	
 	public void execute(@Named(IServiceConstants.ACTIVE_SHELL) Shell shell, MApplication application) {
		if( this.shell != null && ! this.shell.isDisposed() ) {
 			shell = new Shell(shell.getDisplay());
 			shell.setLayout(new FillLayout());
 			MemoryModelResource resource = new MemoryModelResource(application);
			new ApplicationModelEditor(shell, resource, null);
 			shell.open();			
 		}
 	}
 }
