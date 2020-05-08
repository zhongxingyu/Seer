 package org.eclipse.emf.refactor.smells.runtime.handler;
 
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.commands.IHandler;
 import org.eclipse.core.commands.IHandlerListener;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.refactor.smells.managers.SelectionManager;
 import org.eclipse.emf.refactor.smells.runtime.managers.RuntimeManager;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Cursor;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 
 public class FindModelSmellHandler implements IHandler {
 
 	private IFile selectedFile;
 	private EObject selectedEObject;
 	private IProject selectedProject;
 	
 	private Shell shell;
 
 	@Override
 	public void addHandlerListener(IHandlerListener handlerListener) { }
 
 	@Override
 	public void dispose() { }
 
 	@SuppressWarnings("finally")
 	@Override
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		Cursor oldCursor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getCursor();
 		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setCursor(new Cursor(null,SWT.CURSOR_WAIT));
 		ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
 		selectedEObject = SelectionManager.getEObject(selection);
 		if (selectedEObject == null) {	
 			MessageDialog.openError(
 					shell,
 					"EMF Quality Assurance: Error when trying to execute smell search",
 					"No selected EMF model element!");
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setCursor(oldCursor);
 			return null;
 		}
 		try {
 			if (selectedEObject != null) {
 				String path = selectedEObject.eResource().getURI().toPlatformString(true);
 				selectedFile = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(path);
 			} 
 			selectedProject = selectedFile.getProject();
 			RuntimeManager.getInstance();
 			System.out.println("Root: " + selectedEObject);
 			System.out.println("Project: " + selectedProject);
 			RuntimeManager.findConfiguredModelSmells(selectedProject, selectedEObject, selectedFile);
 		} catch (Exception ex) {
 //			ex.printStackTrace();
 			Throwable cause = ex.getCause();
 			if(!(cause == null) && cause.getClass().getName().equals("org.eclipse.emf.ecore.xmi.PackageNotFoundException")){
 				MessageDialog.openError(
 						shell,
 						"EMF Quality Assurance: Error when trying to open File",
 						"The file you selected is not a (valid) EMF model.");
 			} else {
 			MessageDialog.openError(
 					shell,
					"EMF Quality Assurance: Error when trying to execute smell search",
 					ex.toString());
 			ex.printStackTrace();
 			}
 		} finally {
 			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().setCursor(oldCursor);
 			return true;
 		}	
 	}
 
 	@Override
 	public boolean isEnabled() {
 		return true;
 	}
 
 	@Override
 	public boolean isHandled() {
 		return true;
 	}
 
 	@Override
 	public void removeHandlerListener(IHandlerListener handlerListener) { }
 	
 }
