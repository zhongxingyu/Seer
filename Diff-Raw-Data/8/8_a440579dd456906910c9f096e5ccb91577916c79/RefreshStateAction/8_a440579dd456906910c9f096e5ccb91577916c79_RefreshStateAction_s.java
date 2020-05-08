 package net.sourceforge.eclipseccase.ui.actions;
 
import net.sourceforge.clearcase.simple.ClearcaseJNI;
import net.sourceforge.eclipseccase.ClearcasePlugin;
 import net.sourceforge.eclipseccase.ClearcaseProvider;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.team.core.Team;
 
 public class RefreshStateAction extends ClearcaseWorkspaceAction
 {
 	/*
 	 * Method declared on IActionDelegate.
 	 */
 	public void run(IAction action)
 	{
 		IWorkspaceRunnable runnable = new IWorkspaceRunnable()
         {
             public void run(IProgressMonitor monitor) throws CoreException
             {
 				try
 				{
				    //only using 'quick and dirty' caching during "Refresh State" actions
			    	ClearcaseJNI.setLifeTime(ClearcasePlugin.getCacheTimeOut());
 					IResource[] resources = getSelectedResources();
                     beginTask(monitor, "Refreshing state...", resources.length);
 					for (int i = 0; i < resources.length; i++)
 					{
 						IResource resource = resources[i];
                         checkCanceled(monitor);
 						ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
 						provider.refreshRecursive(resource, subMonitor(monitor));
 					}
 				}
 				finally
 				{
 					monitor.done();
				    //always DISABLE caching			    	
					ClearcaseJNI.setLifeTime(0);
 				}
 			}
         };
         
         executeInBackground(runnable, "Refreshing state");
 	}
 
 	protected boolean isEnabled()
 	{
 		IResource[] resources = getSelectedResources();
 		if (resources.length == 0)
 			return false;
         if (resources.length == 1) {
             
             // always ignore derived resources
             if(Team.isIgnoredHint(resources[0]))
                 return false;
             
             ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resources[0]);
             if (provider == null)
                 return false;
         }
 		for (int i = 0; i < resources.length; i++)
 		{
 			IResource resource = resources[i];
 			ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
 			if (provider == null)
 				return false;
 		}
 		return true;
 	}
 
 }
