 package org.meta_environment.eclipse.builder;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 
 public class Enabler implements IWorkbenchWindowActionDelegate {
 	private IProject fProject;
 
 	public Enabler(){
 		super();
 	}
 
 	public void dispose(){
 	}
 
 	public void init(IWorkbenchWindow window) {
 		System.err.println("Enabler init");
 	}
 
 	public void run(IAction action) {
 		new Nature().addToProject(fProject);
 	}
 
 	public void selectionChanged(IAction action, ISelection selection) {
 		if (selection instanceof IStructuredSelection) {
 			IStructuredSelection ss = (IStructuredSelection) selection;
 			Object first = ss.getFirstElement();
 
 			if (first instanceof IProject) {
 				fProject = (IProject) first;
 			}
 		}
 	}
 }
