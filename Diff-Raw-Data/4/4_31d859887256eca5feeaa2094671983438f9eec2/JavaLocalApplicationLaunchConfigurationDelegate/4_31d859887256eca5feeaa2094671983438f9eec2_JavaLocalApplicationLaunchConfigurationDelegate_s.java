 package org.eclipse.dltk.javascript.internal.launching;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
 import org.eclipse.debug.core.model.ISourceLocator;
 import org.eclipse.debug.core.model.IStackFrame;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.IDbgpService;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.ui.ConsoleScriptDebugTargetStreamManager;
 import org.eclipse.dltk.internal.debug.core.model.ScriptDebugTarget;
 import org.eclipse.dltk.internal.launching.JavaScriptSourceLookupDirector;
 import org.eclipse.jdt.launching.JavaLaunchDelegate;
 import org.eclipse.ui.console.IOConsole;
 
 public class JavaLocalApplicationLaunchConfigurationDelegate extends
 		JavaLaunchDelegate implements ILaunchConfigurationDelegate {
 
 	public String getVMArguments(ILaunchConfiguration configuration)
 			throws CoreException {
 		return super.getVMArguments(configuration)
 				+ "-javaagent:C:/rhino-agent.jar";
 	}
 
 	public void launch(ILaunchConfiguration configuration, String mode,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
 		System.out.println("launching");
 		IDbgpService dbgpService = null;
 		try {
			int port = 2300;
			dbgpService = DLTKDebugPlugin.getDefault().createDbgpService(port,
					port + 1);
 			
 			IScriptDebugTarget target = new ScriptDebugTarget("org.eclipse.dltk.debug.javascriptModel", dbgpService,
 					"hello", launch,null);
 			IOConsole cs=new IOConsole("aa",null);
 			ConsoleScriptDebugTargetStreamManager manager = new ConsoleScriptDebugTargetStreamManager(
 					cs);
 			target.setStreamManager(manager);
 			launch.addDebugTarget(target);			
 			final ISourceLocator sourceLocator = launch.getSourceLocator();
 			final JavaScriptSourceLookupDirector l=new JavaScriptSourceLookupDirector();
 			launch.setSourceLocator(new ISourceLocator(){
 
 				public Object getSourceElement(IStackFrame stackFrame) {
 					Object sourceElement = sourceLocator.getSourceElement(stackFrame);
 					if (sourceElement!=null) return sourceElement;
 					return l.getSourceElement(stackFrame);
 				}
 				
 			});
 		} catch (Exception e) {
 
 		}
 
 		super.launch(configuration, mode, launch, monitor);
 	}
 
 	public static final String LOCAL_APPLICATION = "debug.localJavaApplication";
 
 }
