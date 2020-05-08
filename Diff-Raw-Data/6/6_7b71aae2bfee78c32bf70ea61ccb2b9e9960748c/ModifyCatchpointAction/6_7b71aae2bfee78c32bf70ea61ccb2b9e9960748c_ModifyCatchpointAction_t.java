 /*
  * Author: Markus Barchfeld
  * 
  * Copyright (c) 2005 RubyPeople.
  * 
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. RDT is
  * subject to the "Common Public License (CPL) v 1.0". You may not use RDT except in 
  * compliance with the License. For further information see org.rubypeople.rdt/rdt.license.
  */
 
 package org.rubypeople.rdt.internal.debug.ui.actions ;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.window.Window;
 import org.eclipse.ui.IViewActionDelegate;
 import org.eclipse.ui.IViewPart;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.rubypeople.rdt.internal.debug.core.RdtDebugCorePlugin;
 import org.rubypeople.rdt.internal.debug.core.RubyExceptionBreakpoint;
 import org.rubypeople.rdt.internal.debug.ui.ModifyCatchpointDialog;
 import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiMessages;
 import org.rubypeople.rdt.internal.debug.ui.RdtDebugUiPlugin;
 
 public class ModifyCatchpointAction implements IViewActionDelegate,
 		IWorkbenchWindowActionDelegate {
 
 	private RubyExceptionBreakpoint findRubyExceptionBreakpoint(IBreakpoint[] breakpoints) {
 		for (int i = 0; i < breakpoints.length; i++) {
 			if (breakpoints[i] instanceof RubyExceptionBreakpoint) {
 				return (RubyExceptionBreakpoint) breakpoints[i] ;
 			}			
 		}
 		return null ;
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
 	 */
 	public void run(IAction action) {
 		final ModifyCatchpointDialog dialog = new ModifyCatchpointDialog(RdtDebugUiPlugin.getActiveWorkbenchWindow().getShell()) ;
 		dialog.setTitle(RdtDebugUiMessages.ModifyCatchpointDialog_title); 
 		dialog.setMessage(RdtDebugUiMessages.ModifyCatchpointDialog_message);
 		int result = dialog.open();
 		
 		if (result == Window.CANCEL) {
 			return ;
 		}
         IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints("org.rubypeople.rdt.debug");
         RubyExceptionBreakpoint existingBreakpoint = this.findRubyExceptionBreakpoint(breakpoints) ;
         try {
 			if (existingBreakpoint == null) {
 				IBreakpoint breakpoint = new RubyExceptionBreakpoint(dialog.getException()); 
 			}
 			else {
				// This will modify the Breakpoint marker as well which then triggers a
				// ResourceChange event which will be delivered to the RubyDebugTarget
 				existingBreakpoint.setException(dialog.getException()) ;
 			}
 		} catch (CoreException e) {
 			RdtDebugCorePlugin.log(e) ;
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
 	 */
 	public void init(IViewPart view) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction,
 	 *      org.eclipse.jface.viewers.ISelection)
 	 */
 	public void selectionChanged(IAction action, ISelection selection) {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
 	 */
 	public void dispose() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
 	 */
 	public void init(IWorkbenchWindow window) {
 	}
 }
