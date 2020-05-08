 /*******************************************************************************
  * Copyright (c) 2000, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.dltk.javascript.internal.launching;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.net.URL;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.debug.core.Launch;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.dltk.console.ScriptConsoleServer;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.javascript.launching.IJavaScriptLaunchConfigurationConstants;
 import org.eclipse.dltk.javascript.launching.JavaScriptLaunchingPlugin;
 import org.eclipse.dltk.launching.AbstractInterpreterRunner;
 import org.eclipse.dltk.launching.AbstractScriptLaunchConfigurationDelegate;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterRunnerConfiguration;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.IVMRunner;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.jdt.launching.VMRunnerConfiguration;
 import org.osgi.framework.Bundle;
 
 public class JavaScriptInterpreterRunner extends AbstractInterpreterRunner {
 
 	public void run(InterpreterRunnerConfiguration config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 		IDLTKProject proj = AbstractScriptLaunchConfigurationDelegate
 				.getScriptProject(launch.getLaunchConfiguration());
 		IJavaProject myJavaProject = JavaCore.create(proj.getProject());
 		IVMInstall vmInstall = JavaRuntime.getVMInstall(myJavaProject);
 		if (vmInstall == null)
 			vmInstall = JavaRuntime.getDefaultVMInstall();
 		if (vmInstall != null) {
 			IVMRunner vmRunner = vmInstall.getVMRunner(ILaunchManager.RUN_MODE);
 			if (vmRunner != null) {
 				String[] classPath = null;
 				try {
 					classPath = JavaRuntime
 							.computeDefaultRuntimeClassPath(myJavaProject);
 				} catch (CoreException e) {
 				}
 				if (classPath != null) {
 
 					Bundle bundle = Platform
 							.getBundle("org.eclipse.dltk.javascript.rhino");
 					try {
 						URL resolve = FileLocator.toFileURL(bundle
 								.getResource("RhinoRunner.class"));
 						try {
 							File fl = new File(resolve.toURI()).getParentFile();
 							String[] newClassPath = new String[classPath.length + 1];
 							System.arraycopy(classPath, 0, newClassPath, 0,
 									classPath.length);
 							newClassPath[classPath.length] = fl
 									.getAbsolutePath();
 							VMRunnerConfiguration vmConfig = new VMRunnerConfiguration(
 									"RhinoRunner", newClassPath);
 							vmConfig.setProgramArguments(new String[] { config
 									.getScriptToLaunch() });
 							ILaunch launchr = new Launch(launch.getLaunchConfiguration(),
 									ILaunchManager.RUN_MODE, null);
 							vmRunner.run(vmConfig, launchr, null);
 							IProcess[] processes = launchr.getProcesses();
 							for (int a = 0; a < processes.length; a++)
 								launch.addProcess(processes[a]);
 							return;
 						} catch (URISyntaxException e) {
 							// TODO Auto-generated catch block
 							e.printStackTrace();
 						}
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 		throw new CoreException(new Status(IStatus.ERROR, "", ""));
 	}
 
 	protected String constructProgramString(
 			InterpreterRunnerConfiguration config) throws CoreException {
 
 		return "";
 	}
 
 	public JavaScriptInterpreterRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected String[] alterCommandLine(String[] cmdLine, String id) {
 		ScriptConsoleServer server = ScriptConsoleServer.getInstance();
 		String port = Integer.toString(server.getPort());
 		String[] newCmdLine = new String[cmdLine.length + 4];
 
 		newCmdLine[0] = cmdLine[0];
 		newCmdLine[1] = DLTKCore.getDefault().getStateLocation().append(
 				"tcl_proxy").toOSString();
 
 		newCmdLine[2] = "localhost";
 		newCmdLine[3] = port;
 		newCmdLine[4] = id;
 
 		for (int i = 1; i < cmdLine.length; ++i) {
 			newCmdLine[i + 4] = cmdLine[i];
 		}
 
 		return newCmdLine;
 	}
 
 	protected String getPluginIdentifier() {
 		return JavaScriptLaunchingPlugin.getUniqueIdentifier();
 	}
 
 	protected String getProcessType() {
 		return IJavaScriptLaunchConfigurationConstants.ID_JAVASCRIPT_PROCESS_TYPE;
 	}
 }
