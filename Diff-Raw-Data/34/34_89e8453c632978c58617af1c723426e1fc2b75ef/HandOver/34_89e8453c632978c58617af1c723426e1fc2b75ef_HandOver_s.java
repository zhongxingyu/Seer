 package de.ptb.epics.eve.editor.handler;
 
 import java.io.File;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.core.runtime.jobs.IJobManager;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.ui.IWorkbenchWindow;
 import org.eclipse.ui.handlers.HandlerUtil;
 import org.eclipse.ui.ide.FileStoreEditorInput;
 
 import de.ptb.epics.eve.editor.IScanDescriptionReceiver;
 import de.ptb.epics.eve.editor.gef.ScanDescriptionEditor;
 
 /**
  * <code>HandOver</code>.
  * 
  * @author Marcus Michalsky
  * @since 1.1
  */
 public class HandOver extends AbstractHandler {
 
 	private static Logger logger = Logger.getLogger(HandOver.class.getName());
 	
 	private static final String IDISPATCHER_ID = "de.ptb.epics.eve.editor.dispatcher";
 	
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Object execute(final ExecutionEvent event) throws ExecutionException {
 		
 		final IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
 		
		if(window.getActivePage().getActiveEditor() == null) {
 			logger.warn("No Editor found. Dispatch of Scan Description canceled.");
 			return null;
 		}
 		
 		// determine whether the perspective should be switched
 		// (depends on which of the two buttons was pressed)
 		final boolean switchPerspective = Boolean.parseBoolean(event.getParameter(
 				"de.ptb.epics.eve.editor.command.handover.switchPerspective"));
 		
		// check if the scan description which should be handed over has 
		// unsaved changes
		final boolean isDirty = window.getActivePage().getActiveEditor().isDirty();
		
 		// if there are unsaved changes, inform the user
		if(isDirty) {
			String filename = window.getActivePage().getActiveEditor().getTitle();
 			
 			boolean confirm = MessageDialog.openConfirm(null, "Unsaved Changes",
 					filename + " has been modified. Save Changes ?");
 				
 			if(!confirm) {
 				// abort = do nothing
 				return null;
 			}
 		}
 		
 		// save changes
 		NullProgressMonitor monitor = new NullProgressMonitor();
		window.getActivePage().getActiveEditor().
				doSave(monitor);
 		
 		// Obtain the Platform job manager to sync with save job
 		IJobManager manager = Job.getJobManager();
 		try {
 			manager.join("file", new NullProgressMonitor());
 		} catch (OperationCanceledException e1) {
 			logger.warn(e1.getMessage(), e1);
 			throw new ExecutionException(e1.getMessage());
 		} catch (InterruptedException e1) {
 			logger.warn(e1.getMessage(), e1);
 			throw new ExecutionException(e1.getMessage());
 		}
 		
		if (monitor.isCanceled()) {
			throw new ExecutionException("");
		}
		
 		// if it is still dirty (save unsuccessful) don't switch perspective
		if(window.getActivePage().getActiveEditor().isDirty()) {
 			throw new ExecutionException("");
 		}
 		
 		// determine filename
 		final ScanDescriptionEditor graphicalEditor = 
				(ScanDescriptionEditor) window.getActivePage().getActiveEditor();
 			
 		final FileStoreEditorInput editorInput = 
 				(FileStoreEditorInput)graphicalEditor.getEditorInput();	
 			
 		final File file = new File(editorInput.getURI());
 		
 		// get all plugins that connected to the extension point
 		// (should be only the viewer so far)
 		IConfigurationElement[] config = Platform.getExtensionRegistry()
 				.getConfigurationElementsFor(IDISPATCHER_ID);
 		try {
 			for (IConfigurationElement e : config) {
 				final Object o = e.createExecutableExtension("class");
 				if (o instanceof IScanDescriptionReceiver) {
 					ISafeRunnable runnable = new ISafeRunnable() {
 						@Override
 						public void handleException(Throwable exception) {
 							logger.error(exception.getMessage(), exception);
 						}
 						@Override
 						public void run() throws Exception {
 							((IScanDescriptionReceiver) o).scanDescriptionReceived(
 									file, switchPerspective);
 						}
 					};
 					SafeRunner.run(runnable);
 				}
 			}
 		} catch (CoreException ex) {
 			logger.error(ex.getMessage(), ex);
 		}
 		return null;
 	}
 }
