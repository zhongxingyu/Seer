 package de.ptb.epics.eve.editor.actions;
 
 import java.io.File;
 
 import org.eclipse.jface.action.IAction;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.IWorkbenchWindowActionDelegate;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.WorkbenchException;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 
 import de.ptb.epics.eve.editor.graphical.GraphicalEditor;
 import de.ptb.epics.eve.viewer.Activator;
 
 /**
  * <code>HandOverAction</code>.
  * 
  * @author ?
  * @author Marcus Michalsky
  */
 public class HandOverAction implements IWorkbenchWindowActionDelegate {
 
 	private IWorkbenchWindow window;
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void run(final IAction action) {
 
 		// check whether the perspective should be switched 
 		// (hand over or hand over silent pressed)
 		boolean silent = (!action.getId().equals(
 				"de.ptb.epics.eve.editor.actions.HandOverAction"));
 
 		// check if the scan description which should be handed over has 
 		// unsaved changes
 		boolean unsaved = de.ptb.epics.eve.editor.Activator.getDefault().
 								getWorkbench().getActiveWorkbenchWindow().
 								getActivePage().getActiveEditor().isDirty();	
 				
 		// if there are unsaved changes, inform the user
 		if(unsaved)
 		{
 			String filename = de.ptb.epics.eve.editor.Activator.getDefault().
 								getWorkbench().getActiveWorkbenchWindow().
 								getActivePage().getActiveEditor().getTitle();
 			
 			boolean confirm = MessageDialog.openConfirm(null, "Unsaved Changes", 
 					filename + " has been modified. Save Changes ?");
 				
 			if(confirm)
 			{   // save changes 
 				window.getActivePage().getActiveEditor().doSave(null);					
 			}
 			else
 			{ // abort = do nothing
 				return;
 			}
 		}	
		
		// if it is still dirty (save unsuccessful) don't switch perspective
		if(de.ptb.epics.eve.editor.Activator.getDefault().
				getWorkbench().getActiveWorkbenchWindow().
				getActivePage().getActiveEditor().isDirty())
			return;
		
 		// in all cases -> switch perspective and load file in play list
 		switchPerspective(silent);
 
 		final GraphicalEditor graphicalEditor = 
 			(GraphicalEditor) window.getActivePage().getActiveEditor();
 		
 		final FileStoreEditorInput editorInput = 
 			(FileStoreEditorInput)graphicalEditor.getEditorInput();	
 		
 		final File file = new File(editorInput.getURI());
 		Activator.getDefault().addScanDescription(file);
 	}
 	
 	/*
 	 * called by the run method
 	 * does nothing if argument is true (silent handover)
 	 */
 	private void switchPerspective(boolean silent)
 	{
 		if(silent) return;
 		try {
 			PlatformUI.getWorkbench().showPerspective(
 					"EveEnginePerspective", window);
 		} catch (WorkbenchException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void init(final IWorkbenchWindow window) {
 		this.window = window;
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void selectionChanged(final IAction action, 
 								 final ISelection selection) {
 	}
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void dispose() {
 	}
 }
