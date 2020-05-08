 package net.sourceforge.eclipseccase.ui.actions;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 
 import net.sourceforge.clearcase.simple.ClearcaseUtil;
 import net.sourceforge.eclipseccase.ClearcasePlugin;
 import net.sourceforge.eclipseccase.ClearcaseProvider;
 
import org.apache.tools.ant.taskdefs.condition.Os;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.jface.action.IAction;
 import org.eclipse.team.core.TeamException;
 import org.eclipse.team.internal.ui.actions.TeamAction;
 import org.eclipse.ui.IActionDelegate;
 import org.eclipse.ui.actions.WorkspaceModifyOperation;
 
 /**
  *  Pulls up the clearcase version tree for the element
  */
 public class FindCheckOutsAction extends ClearcaseAction
 {
 
     /**
      * @see TeamAction#isEnabled()
      */
     protected boolean isEnabled() throws TeamException
     {
        // workaround for bug 960292
        if(!Os.isFamily(Os.WINDOWS))
            return false;
        
         IResource[] resources = getSelectedResources();
         if (resources.length == 0)
             return false;
         for (int i = 0; i < resources.length; i++)
         {
             IResource resource = resources[i];
             if (resource.getType() == IResource.FILE)
                 return false;
 
             ClearcaseProvider provider = ClearcaseProvider.getClearcaseProvider(resource);
             if (provider == null || provider.isUnknownState(resource) || provider.isIgnored(resource))
                 return false;
         }
         return true;
     }
 
     /**
      * @see IActionDelegate#run(IAction)
      */
     public void run(IAction action)
     {
         run(new WorkspaceModifyOperation()
         {
             public void execute(IProgressMonitor monitor)
                 throws InterruptedException, InvocationTargetException
             {
                 try
                 {
                     IResource[] resources = getSelectedResources();
                     for (int i = 0; i < resources.length; i++)
                     {
                         IResource resource = resources[i];
                         String path = resource.getLocation().toOSString();
                         if (ClearcasePlugin.isUseCleartool())
                         {
                             ClearcasePlugin.getEngine().cleartool("lscheckout -graphical " + ClearcaseUtil.quote(path));
                         }
                         else
                         {
                             Runtime.getRuntime().exec(new String[] {"clearfindco", resource.getLocation().toOSString()});
                         }
                     }
                 }
                 catch (IOException ex)
                 {
                     throw new InvocationTargetException(ex);
                 }
             }
         }, "Find checkouts", TeamAction.PROGRESS_BUSYCURSOR);
     }
 
 }
