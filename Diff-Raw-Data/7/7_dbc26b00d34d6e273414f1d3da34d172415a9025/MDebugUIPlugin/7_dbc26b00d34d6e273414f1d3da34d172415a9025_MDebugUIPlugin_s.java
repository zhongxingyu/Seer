 package gov.va.mumps.debug.ui;
 
 import gov.va.mumps.debug.core.model.MDebugTarget;
 import gov.va.mumps.debug.ui.console.MDevConsole;
 
import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchListener;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 public class MDebugUIPlugin extends AbstractUIPlugin implements
 		ILaunchListener {
 
 	@Override
 	public void start(BundleContext context) throws Exception { //TODO: what if this is started after launches were already added?
 		super.start(context);	
 		DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
 	}
 	
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		try {
 			DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
 		} finally {
 			super.stop(context);
 		}
 	}
 
 	@Override
 	public void launchAdded(final ILaunch launch) {
 	}
 
 	@Override
 	public void launchChanged(ILaunch launch) {
 				
 		if (launch.getDebugTarget() == null)
 			return;
 		
 		MDebugTarget mDebugTarget = (MDebugTarget) launch.getDebugTarget();
 		
 		synchronized (mDebugTarget) {
 			if (mDebugTarget.isLinkedToConsole())
 				return;
 			
 //			String debugTargetName = launch.getLaunchConfiguration().getName();
 //			try {
 //				String debugTargetName = mDebugTarget.getName();
 //			} catch (DebugException e) {
 //			}
 //			
 			MDevConsole mDevConsole = new MDevConsole("MUMPS Console: " +launch.getLaunchConfiguration().getName(), null, null, true);
 			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { mDevConsole });
 			
 			mDebugTarget.addReadCommandListener(mDevConsole);
 			mDebugTarget.addWriteCommandListener(mDevConsole);
 			mDevConsole.addInputReadyListener(mDebugTarget);
 			
 			mDebugTarget.setLinkedToConsole(true);
 		}
 	}
 
 	@Override
 	public void launchRemoved(ILaunch launch) {
 		//TODO: remove the listeners that were added to the custom console
 	}
 }
