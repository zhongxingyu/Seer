 package org.nodeclipse.debug.launch;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.chromium.debug.core.ChromiumDebugPlugin;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.model.ILaunchConfigurationDelegate;
 import org.eclipse.debug.core.model.RuntimeProcess;
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.nodeclipse.debug.util.Constants;
 import org.nodeclipse.debug.util.NodeDebugUtil;
 import org.nodeclipse.debug.util.VariablesUtil;
 import org.nodeclipse.ui.Activator;
 import org.nodeclipse.ui.preferences.Dialogs;
 import org.nodeclipse.ui.preferences.PreferenceConstants;
 import org.nodeclipse.ui.util.NodeclipseConsole;
 
 /**
  * launch() implements starting Node and passing all parameters.
  * Node is launched as node, coffee, coffee -c, tsc or node-dev(or other monitors)
  * 
  * @author Lamb, Tomoyuki, Pushkar, Paul Verest
  */
 public class LaunchConfigurationDelegate implements
 		ILaunchConfigurationDelegate {
 	private static RuntimeProcess nodeProcess = null; //since 0.7 it should be debuggable instance
 	//@since 0.7. contain all running Node thread, including under debug. Non Thread-safe, as it should be only in GUI thread
 	//private static List<RuntimeProcess> nodeRunningProcesses = new LinkedList<RuntimeProcess>(); 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.
 	 * eclipse.debug.core.ILaunchConfiguration, java.lang.String,
 	 * org.eclipse.debug.core.ILaunch,
 	 * org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	@Override
 	public void launch(ILaunchConfiguration configuration, String mode,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
 		
 		IPreferenceStore preferenceStore = Activator.getDefault().getPreferenceStore();
 		boolean allowedMany = preferenceStore.getBoolean(PreferenceConstants.NODE_ALLOW_MANY);//@since 0.7
 		boolean isDebugMode = mode.equals(ILaunchManager.DEBUG_MODE);
 
 		if (allowedMany){//@since 0.7
 			if ( isDebugMode  
 				&& (nodeProcess != null && !nodeProcess.isTerminated()) ) {
 				showErrorDialog("Only 1 node process can be debugged in 1 Eclipse instance!\n\n"+
 				"Open other Eclipse/Enide Studio with different node debug port configurred. ");
 				return;
 			}
 		
 		}else{	
 			if(nodeProcess != null && !nodeProcess.isTerminated()) {
 				//throw new CoreException(new Status(IStatus.OK, ChromiumDebugPlugin.PLUGIN_ID, null, null));
 				showErrorDialog("Other node process is running!");
 				return;
 				//TODO suggest to terminate and start new
 			}	
 		}
 
 		 
 		// Using configuration to build command line	
 		List<String> cmdLine = new ArrayList<String>();
 
 		if (preferenceStore.getBoolean(PreferenceConstants.NODE_JUST_NODE)){
 			cmdLine.add("node");
 		}else{
 			// old way: Application path should be stored in preference.
 			String nodePath= preferenceStore.getString(PreferenceConstants.NODE_PATH);
 			// Check if the node location is correctly configured
 			File nodeFile = new File(nodePath);
 			if(!nodeFile.exists()){
 				// If the location is not valid than show a dialog which prompts the user to goto the preferences page
 				Dialogs.showPreferencesDialog("Node.js runtime is not correctly configured.\n\n"
 						+ "Please goto Window -> Prefrences -> Nodeclipse and configure the correct location");
 				return;
 			}			
 			cmdLine.add(nodePath);
 		}
 		
 		if (isDebugMode) {
 			// -brk says to Node runtime wait until Chromium Debugger starts and connects
 			// that is causing "stop on first line" behavior,
 			// otherwise small apps or first line can be undebuggable.
 			String brk = "-brk" ; //default "-brk"
 			if (preferenceStore.getBoolean(PreferenceConstants.NODE_DEBUG_NO_BREAK)) //default false
 				brk = "";
 			// done: flexible debugging port, instead of hard-coded 5858
 			// #61 https://github.com/Nodeclipse/nodeclipse-1/issues/61
 			int nodeDebugPort = preferenceStore.getInt(PreferenceConstants.NODE_DEBUG_PORT);
 			if (nodeDebugPort==0) { nodeDebugPort=5858;};
 			cmdLine.add("--debug"+brk+"="+nodeDebugPort); //--debug-brk=5858
 		}
 		
 		//@since 0.9 from Preferences
 		String nodeOptions= preferenceStore.getString(PreferenceConstants.NODE_OPTIONS);
 		if(!nodeOptions.equals("")) {
 			String[] sa = nodeOptions.split(" ");
 			for(String s : sa) {
 				cmdLine.add(s);
 			}			
 		}
 		
 		String nodeArgs = configuration.getAttribute(Constants.ATTR_NODE_ARGUMENTS, "");
 		if(!nodeArgs.equals("")) {
 			String[] sa = nodeArgs.split(" ");
 			for(String s : sa) {
 				cmdLine.add(s);
 			}
 		}
 		
 		String file = configuration.getAttribute(Constants.KEY_FILE_PATH,	Constants.BLANK_STRING);
 		String extension = null;
 		int i = file.lastIndexOf('.');
 		if(i > 0) {
 			extension = file.substring(i+1);
 		} else {
 			throw new CoreException(new Status(IStatus.OK, ChromiumDebugPlugin.PLUGIN_ID,
 				"Target file does not have extension: " + file, null));
 		}
 		
 		// #57 running app.js with node-dev, forever, supervisor, nodemon etc
 		// https://github.com/Nodeclipse/nodeclipse-1/issues/57
 		String nodeMonitor = configuration.getAttribute(Constants.ATTR_NODE_MONITOR, "");
 		if(!nodeMonitor.equals("")) { // any value
 			//TODO support selection, now only one
 			
 			String nodeMonitorPath= preferenceStore.getString(PreferenceConstants.NODE_MONITOR_PATH);
 			
 			// Check if the node monitor location is correctly configured
 			File nodeMonitorFile = new File(nodeMonitorPath);
 			if(!nodeMonitorFile.exists()){
 				// If the location is not valid than show a dialog which prompts the user to goto the preferences page
 				Dialogs.showPreferencesDialog("Node.js monitor is not correctly configured.\n"
 						+ "Select path to installed util: forever, node-dev, nodemon or superviser.\n\n"
 						+ "Please goto Window -> Prefrences -> Nodeclipse and configure the correct location");
 				return;
 			}
 			cmdLine.add(nodeMonitorPath);
 		} else if ( ("coffee".equals(extension))||("litcoffee".equals(extension))||("md".equals(extension)) ) {
 			//if (preferenceStore.getBoolean(PreferenceConstants.COFFEE_JUST_COFFEE)){
 			//	cmdLine.add("coffee"); //TODO should be instead of node above
 			//}else{
 				cmdLine.add(preferenceStore.getString(PreferenceConstants.COFFEE_PATH));
 			//}
 			// coffee -c
 			String coffeeCompile = configuration.getAttribute(Constants.ATTR_COFFEE_COMPILE, "");
 			if(!coffeeCompile.equals("")) { // any value
 				cmdLine.add("-c");
 				String coffeeCompileOptions = preferenceStore.getString(PreferenceConstants.COFFEE_COMPILE_OPTIONS);
 				if(!coffeeCompileOptions.equals("")) {
 					cmdLine.add(coffeeCompileOptions);
 				}
 			}
 		} else if ("ts".equals(extension)) {
 			// the only thing we can do now with .ts is to compile, so no need to check if it was launched as tsc
 			//String typescriptCompiler = configuration.getAttribute(Constants.ATTR_TYPESCRIPT_COMPILER, "");
 			cmdLine.add(preferenceStore.getString(PreferenceConstants.TYPESCRIPT_COMPILER_PATH));
 		}
 		
 		String filePath = ResourcesPlugin.getWorkspace().getRoot().findMember(file).getLocation().toOSString();
 		// path is relative, so can not found it.
 		cmdLine.add(filePath);
 
 		//@since 0.9 from Preferences
 		String nodeApplicationArguments = preferenceStore.getString(PreferenceConstants.NODE_APPLICATION_ARGUMENTS);
 		if(!nodeApplicationArguments.equals("")) {
 			String[] sa = nodeApplicationArguments.split(" ");
 			for(String s : sa) {
 				cmdLine.add(s);
 			}
 		}
 		
 		String programArgs = configuration.getAttribute(Constants.ATTR_PROGRAM_ARGUMENTS, "");
 		if(!programArgs.equals("")) {
 			String[] sa = programArgs.split(" ");
 			for(String s : sa) {
 				cmdLine.add(s);
 			}
 		}
 
 		String workingDirectory = configuration.getAttribute(Constants.ATTR_WORKING_DIRECTORY, "");
 		File workingPath = null;
 		if(workingDirectory.length() == 0) {
 			workingPath = (new File(filePath)).getParentFile();
 		} else {
 			workingDirectory = VariablesUtil.resolveValue(workingDirectory);
 			if(workingDirectory == null) {
 				workingPath = (new File(filePath)).getParentFile();
 			} else {
 				workingPath = new File(workingDirectory);
 			}
 		}
 		
 		Map<String, String> envm = new HashMap<String, String>();
 		envm = configuration.getAttribute(Constants.ATTR_ENVIRONMENT_VARIABLES, envm);
		String[] envp = new String[envm.size()];
 		int idx = 0;
 		for(String key : envm.keySet()) {
 			String value = envm.get(key);
 			envp[idx++] = key + "=" + value;
 		}
 		
 		for(String s : cmdLine) NodeclipseConsole.write(s+" ");
 		NodeclipseConsole.write("\n");
 		
 		String[] cmds = {};
 		cmds = cmdLine.toArray(cmds);
 		// Launch a process to run/debug. See also #71 (output is less or no output)
 		Process p = DebugPlugin.exec(cmds, workingPath, envp);
 		// no way to get private p.handle from java.lang.ProcessImpl
 		RuntimeProcess process = (RuntimeProcess)DebugPlugin.newProcess(launch, p, Constants.PROCESS_MESSAGE); 
 		if (isDebugMode) {
 			if(!process.isTerminated()) { 
 				int nodeDebugPort = preferenceStore.getInt(PreferenceConstants.NODE_DEBUG_PORT);
 				NodeDebugUtil.launch(mode, launch, monitor, nodeDebugPort);
 			}
 		}
 		
 		if (allowedMany){ //@since 0.7
 			if (isDebugMode){
 				nodeProcess = process;	
 			}
 			//nodeRunningProcesses.add(process);
 		}else{
 			nodeProcess = process;	
 		}
 	}
 
 	private void showErrorDialog(final String message) {
 		Display.getDefault().syncExec(new Runnable() {
 			public void run() {
 				Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
 
 				MessageDialog dialog = new MessageDialog(shell, "Nodeclipse", null, message, 
 						MessageDialog.ERROR, new String[] { "OK" }, 0);
 				dialog.open();
 			}
 		});
 	}
 	
 	public static void terminateNodeProcess() {
 		if(nodeProcess != null) {
 			try {
 				nodeProcess.terminate();
 			} catch (DebugException e) {
 				//e.printStackTrace();
 				NodeclipseConsole.write(e.getLocalizedMessage()+"\n");
 			}
 			nodeProcess = null;
 		}
 	}
 }
