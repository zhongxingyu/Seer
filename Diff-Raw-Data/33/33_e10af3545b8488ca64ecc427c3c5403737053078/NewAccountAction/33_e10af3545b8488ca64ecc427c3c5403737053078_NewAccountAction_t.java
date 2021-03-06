 /*
  * DPP - Serious Distributed Pair Programming
  * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
  * (c) Riad Djemili - 2006
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 1, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 package de.fu_berlin.inf.dpp.ui.actions;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 
 import de.fu_berlin.inf.dpp.Saros;
 import de.fu_berlin.inf.dpp.ui.wizards.CreateAccountWizard;
 
 public class NewAccountAction implements IWorkbenchWindowActionDelegate {
     private IWorkbenchWindow window;
 
     public void run(IAction action) {
         try {
             Shell shell = this.window.getShell();
            WizardDialog wd = new WizardDialog(shell, new CreateAccountWizard(
                true, true, true));
            wd.setHelpAvailable(false);
            wd.open();
         } catch (Exception e) {
             // TODO Use consistent way of dealing with Exceptions
             Saros.log("Could not create new Account.", e);
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see org.eclipse.ui.IWorkbenchWindowActionDelegate
      */
     public void init(IWorkbenchWindow window) {
         this.window = window;
     }
 
     public void selectionChanged(IAction action, ISelection selection) {
         // We don't need to update on a selectionChanged
     }
 
     public void dispose() {
         // Nothing to dispose
     }
 }
