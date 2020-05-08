 /*
 *
 *  JMoney - A Personal Finance Manager
 *  Copyright (c) 2004 Nigel Westbury <westbury@users.sourceforge.net>
 *
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */
 
 package net.sf.jmoney;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.ui.application.IWorkbenchConfigurer;
 import org.eclipse.ui.application.IWorkbenchWindowConfigurer;
 import org.eclipse.ui.application.WorkbenchAdvisor;
 import org.eclipse.ui.application.WorkbenchWindowAdvisor;
 import org.eclipse.ui.internal.Workbench;
 
 /**
  * @author Nigel
  *
  * TODO To change the template for this generated type comment go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 public class JMoneyWorkbenchAdvisor extends WorkbenchAdvisor {
 
 	public String getInitialWindowPerspectiveId() {
 		// This method will not be called if
 		// org.eclipse.ui/defaultPerspectiveId is set in
 		// plugin_customization.ini in org.eclipse.platform
 		// plugin. (This seems to be a design flaw - if
 		// the application wants this perspective then it
 		// would stupid to use some other).
 		// TODO sort this out
 		return "net.sf.jmoney.JMoneyPerspective";
 	}
 
     /* (non-Javadoc)
      * @see org.eclipse.ui.application.WorkbenchAdvisor#createWorkbenchWindowAdvisor(org.eclipse.ui.application.IWorkbenchWindowConfigurer)
      */
     public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(IWorkbenchWindowConfigurer configurer) {
         return  new JMoneyWorkbenchWindowAdvisor(configurer);
     }
     
     public void initialize(IWorkbenchConfigurer configurer) {
         super.initialize(configurer);
 
         // Turn on support for the saving and restoring of
         // view states through IMemento interfaces.
         configurer.setSaveAndRestore(true);
     }
 
     public boolean preWindowShellClose(IWorkbenchWindowConfigurer configurer) {
         // If a session is open, ensure we have all the information we
         // need to close it. Some datastores need additional information
         // to save the session. For example the serialized XML datastore
         // requires a file name which will not have been requested from the
         // user if the datastore has not yet been saved.
 
         // This call must be done here for two reasons.
         // 1. It ensures that the session data can be saved.
         // 2. The navigation view saves, as part of its state,
         // the datastore which was open when the workbench was
         // last shut down, allowing it to open the same session
         // when the workbench is next opened. In order to do this,
         // the navigation view saves the session details.
 
         return JMoneyPlugin.getDefault().saveOldSession(configurer.getWindow());
     }
 	
 	/** 
 	 * Display a window to document the error if an Exception occurs
 	 */
 	public void eventLoopException (Throwable t) {
 		
 		
 		String text =  getText(t);
 		IStatus status = 
			new Status(Status.ERROR, "Unknown Plugin" /*TODO*/, Status.OK,  new String("Uncatched error"), t);
 
 		ErrorDialog dialog = new ErrorDialog(
 				Workbench.getInstance().getWorkbenchWindows()[0].getShell(),
 				"An error occured",
 				text, 
 				status, 
 				status.getSeverity());
 		// dialog.setBlockOnOpen(true);
		dialog.create();
		dialog.getShell().setSize(dialog.getShell().computeSize(800, SWT.DEFAULT));
 		dialog.open();
 	}
 	
 	String getText (Throwable t) {
 		String text = new String();
 		text += "Following error has occured:\n\n";
 		text += t.getLocalizedMessage() + "\n\n";
 		StackTraceElement st[] = t.getStackTrace();
 		for (int i = 0; i<st.length && i<6; i++) {
 			text += st[i].toString() + "\n";
 		}
 		return text;
 	}
 
 }
