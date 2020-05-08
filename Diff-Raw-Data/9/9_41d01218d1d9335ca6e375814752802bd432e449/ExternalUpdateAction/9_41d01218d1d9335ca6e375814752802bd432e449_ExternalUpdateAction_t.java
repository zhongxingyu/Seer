 package net.sourceforge.eclipseccase.ui.actions;
 
 import net.sourceforge.eclipseccase.ClearCasePreferences;
 
 import org.eclipse.ui.PlatformUI;
 
 import net.sourceforge.eclipseccase.views.ConfigSpecView;
 
 import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
 
 import net.sourceforge.clearcase.ClearCaseInterface;
 
 import net.sourceforge.eclipseccase.ClearCasePlugin;
 
 import org.eclipse.core.resources.IProject;
 
 import net.sourceforge.clearcase.ClearCase;
 
 import java.io.File;
 
 import net.sourceforge.eclipseccase.ClearCaseProvider;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.ui.IActionDelegate;
 
 /**
  * Updates the resources in a clearcase snapshot view.
  */
 public class ExternalUpdateAction extends ClearCaseWorkspaceAction {
 	private IResource[] resources = null;
 
 	/**
 	 * {@inheritDoc}
 	 */
 	public boolean isEnabled() {
 		IResource[] resources = getSelectedResources();
 		if (resources.length == 0)
 			return false;
 		for (int i = 0; i < resources.length; i++) {
 			IResource resource = resources[i];
 			ClearCaseProvider provider = ClearCaseProvider.getClearCaseProvider(resource);
 			if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource))
 				return false;
 			if (!provider.isSnapShot(resource))
 				return false;
 		}
 		return true;
 	}
 
 	/**
 	 * @see IActionDelegate#run(IAction)
 	 */
 	public void execute(IAction action) {
 		resources = getSelectedResources();
 
 		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 
 			public void run(IProgressMonitor monitor) throws CoreException {
 
 				try {
 					if (resources != null && resources.length != 0) {
 						for (int i = 0; i < resources.length; i++) {
 
 							IResource resource = resources[i];
 							String element = null;
 
 							if (resource.getType() == IResource.FOLDER) {
 								element = resource.getLocation().toOSString();
 							} else if (resource.getType() == IResource.PROJECT) {
 								// Project folder.
 								element = resource.getLocation().toOSString();
 
 							} else {
 								// Folder to file.
 								element = resource.getParent().getLocation().toOSString();
 							}
 							ClearCaseProvider p = ClearCaseProvider.getClearCaseProvider(resource);
 							if (p != null) {
								if (ClearCasePreferences.useGraphicalExternalUpdateView()) {
 									p.update(element, ClearCase.GRAPHICAL, true);
 								} else {
 									ClearCaseInterface cci = ClearCase.createInterface(ClearCase.INTERFACE_CLI);
 									String viewName = cci.getViewName(resources[0].getLocation().toOSString());
 									String workingDir = resources[0].getProject().getLocation().toOSString();
 									cci.setViewConfigSpec(viewName, "-current", workingDir, new ConsoleOperationListener(monitor));
 								}
 							}
 
 						}
 					}
 				} finally {
 					monitor.done();
 				}
 
 			}
 		};
 		executeInBackground(runnable, "Updating resources in Snapshot View from ClearCase");
 	}
 
 }
