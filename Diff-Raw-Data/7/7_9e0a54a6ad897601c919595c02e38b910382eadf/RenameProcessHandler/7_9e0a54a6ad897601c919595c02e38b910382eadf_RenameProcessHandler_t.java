 package org.jbpm.gd.jpdl.refactoring;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ltk.ui.refactoring.RefactoringWizardOpenOperation;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.handlers.HandlerUtil;
 
 public class RenameProcessHandler extends AbstractProcessHandler {
 
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		Shell activeShell= HandlerUtil.getActiveShell(event);
 		ISelection sel= HandlerUtil.getCurrentSelection(event);
 		if (sel instanceof IStructuredSelection) {
 			IResource resource= getCurrentResource((IStructuredSelection) sel);
 			if (resource != null) {
 				RenameProcessWizard refactoringWizard= new RenameProcessWizard(resource);
 				RefactoringWizardOpenOperation op= new RefactoringWizardOpenOperation(refactoringWizard);
 				try {
 					op.run(activeShell, "Rename Process");
 				} catch (InterruptedException e) {
 					// do nothing
 				}
 			}
 		}
 		return null;
 	}
 
 	private IResource getCurrentResource(IStructuredSelection sel) {
 		IResource[] resources= getSelectedResources(sel);
 		if (resources.length == 1) {
 			return resources[0];
 		}
 		return null;
 	}
 }
