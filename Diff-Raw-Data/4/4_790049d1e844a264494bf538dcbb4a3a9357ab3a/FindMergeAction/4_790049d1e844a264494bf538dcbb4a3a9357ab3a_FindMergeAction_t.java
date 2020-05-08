 package net.sourceforge.eclipseccase.ui.actions;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import net.sourceforge.clearcase.commandline.CleartoolCommandLine;
 import net.sourceforge.clearcase.commandline.CommandLauncher;
 import net.sourceforge.eclipseccase.ClearcaseProvider;
 import net.sourceforge.eclipseccase.ui.console.ConsoleOperationListener;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IAction;
 
 public class FindMergeAction extends ClearcaseWorkspaceAction {
 	private IResource[] resources = null;
 
 	/**
 	 * {@inheritDoc
 	 */
 	@Override
 	public boolean isEnabled() {
 		boolean bRes = true;
 
 		IResource[] resources = getSelectedResources();
 		if (resources.length != 0) {
 			for (int i = 0; (i < resources.length) && (bRes); i++) {
 				IResource resource = resources[i];
 				ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
 				if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource) || !provider.isClearcaseElement(resource)) {
 					bRes = false;
 				}
 			}
 		} else {
 			bRes = false;
 		}
 
 		return bRes;
 
 	}
 
 	@Override
 	public void execute(IAction action) throws InvocationTargetException, InterruptedException {
 		resources = getSelectedResources();
 
 		IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
 			public void run(IProgressMonitor monitor) throws CoreException {
 
 				try {
 					if (resources != null && resources.length != 0) {
 						IResource resource = resources[0];
 						File workingDir = null;
 						if (resource.getType() == IResource.FOLDER) {
 							workingDir = new File(resource.getLocation().toOSString());
 						} else {
 							workingDir = new File(resource.getLocation().toOSString()).getParentFile();
 						}
 
						CommandLauncher launcher = new CommandLauncher();
						launcher.setAutoReturn(3); // FIXME: use CC Provider API
						launcher.execute(new CleartoolCommandLine("findmerge").addOption("-graphical").create(), workingDir, null, new ConsoleOperationListener(monitor));
 					}
 				} catch (Exception e) {
 				} finally {
 					monitor.done();
 				}
 			}
 		};
 
 		executeInBackground(runnable, "Make Branch Type");
 	}
 }
