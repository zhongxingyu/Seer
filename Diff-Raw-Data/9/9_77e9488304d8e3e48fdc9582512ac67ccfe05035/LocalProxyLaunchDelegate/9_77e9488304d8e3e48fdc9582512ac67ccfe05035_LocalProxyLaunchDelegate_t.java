 /***************************************************************************************************
  * Copyright (c) 2001, 2003 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 /*
 * $RCSfile: LocalProxyLaunchDelegate.java,v $ $Revision: 1.13 $ $Date: 2004/08/23 14:03:15 $
  */
 package org.eclipse.jem.internal.proxy.remote;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.*;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.text.MessageFormat;
 import java.util.*;
 import java.util.Map;
 import java.util.Random;
 import java.util.logging.Level;
 
 import org.eclipse.core.runtime.*;
 import org.eclipse.debug.core.*;
 import org.eclipse.debug.core.model.*;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.launching.*;
 
 import org.eclipse.jem.internal.proxy.core.*;
 import org.eclipse.jem.internal.proxy.remote.awt.REMRegisterAWT;
 
 /**
  * Launch Delegate for launching Local (i.e. remote vm is on local system). Here "remote" means the
  * registry is not in the IDE but in a separate VM, and "local" means that is in on the local
  * physical machine and not on a separate machine.
  * 
  * @since 1.0.0
  */
 public class LocalProxyLaunchDelegate extends AbstractJavaLaunchConfigurationDelegate {
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration,
 	 *      java.lang.String, org.eclipse.debug.core.ILaunch,
 	 *      org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor pm) throws CoreException {
 
 		String launchKey = configuration.getAttribute(IProxyConstants.ATTRIBUTE_LAUNCH_KEY, (String) null);
 		if (launchKey == null)
 			abort(ProxyRemoteMessages.getString("ProxyRemoteNoLaunchKey"), null, 0); //$NON-NLS-1$
 
 		// In Eclipse, even if private, a launch will show up in the debug process tree and in the console viewer.
 		// To be absolutely private, we need to remove the launch which has already been added.
 		if (ProxyLaunchSupport.ATTR_PRIVATE != null && configuration.getAttribute(ProxyLaunchSupport.ATTR_PRIVATE, false)) 
 			DebugPlugin.getDefault().getLaunchManager().removeLaunch(launch);		
 		if (pm == null) {
 			pm = new NullProgressMonitor();
 		}
 
 		IJavaProject project = getJavaProject(configuration);
 		String name = configuration.getAttribute(IProxyConstants.ATTRIBUTE_VM_TITLE, (String) null);
 		if (name == null)
 			name = MessageFormat.format(ProxyRemoteMessages.getString("ProxyRemoteVMName"), new Object[] { project != null ? project.getProject().getName() : "" }); //$NON-NLS-1$
 		else
 			name = MessageFormat.format(ProxyRemoteMessages.getString("ProxyRemoteVMNameWithComment"), new Object[] { project != null ? project.getProject().getName() : "", name }); //$NON-NLS-1$
 
 		// Problem with launch, can't have double-quotes in vmName.
 		if (name.indexOf('"') != -1)
 			name = name.replace('"', '\'');
 
 		pm.beginTask("", 500); //$NON-NLS-1$
 		pm.subTask(MessageFormat.format(ProxyRemoteMessages.getString("ProxyRemoteLaunchVM"), new Object[] { name })); //$NON-NLS-1$
 		// check for cancellation
 		if (pm.isCanceled())
 			return;
 
 		IVMInstall vm = verifyVMInstall(configuration);
 
 		IVMRunner runner = vm.getVMRunner(mode);
 		if (runner == null) {
 			abort(MessageFormat.format(ProxyRemoteMessages.getString("Proxy_NoRunner_ERROR_"), new Object[] { name }), null, 0); //$NON-NLS-1$
 		}
 
 		File workingDir = verifyWorkingDirectory(configuration);
 		String workingDirName = null;
 		if (workingDir != null) {
 			workingDirName = workingDir.getAbsolutePath();
 		}
 
 		// Environment variables
 		String[] envp = DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
 
 		// Program & VM args
 		String pgmArgs = getProgramArguments(configuration);
 		String vmArgs = getVMArguments(configuration);
 		ExecutionArguments execArgs = new ExecutionArguments(vmArgs, pgmArgs);
 
 		// VM-specific attributes
 		Map vmAttributesMap = getVMSpecificAttributesMap(configuration);
 
 		pm.worked(100);
 		
 		// Now let's get the classpaths created through the contributors.
 		URL[] classpath = ProxyLaunchSupport.convertStringPathsToURL(getClasspath(configuration));
 		String[][] bootpathInfoStrings = getBootpathExt(vmAttributesMap);
 		URL[][] bootpathInfo = new URL[][]{
 				ProxyLaunchSupport.convertStringPathsToURL(bootpathInfoStrings[0]),
 				ProxyLaunchSupport.convertStringPathsToURL(bootpathInfoStrings[1]),
 				ProxyLaunchSupport.convertStringPathsToURL(bootpathInfoStrings[2]),
 		};
 		ProxyLaunchSupport.LaunchInfo launchInfo = ProxyLaunchSupport.getInfo(launchKey);
 		final IConfigurationContributor[] contributors = launchInfo.contributors;
 		final LocalFileConfigurationContributorController controller =
 			new LocalFileConfigurationContributorController(classpath, bootpathInfo, launchInfo);
 		if (contributors != null) {		
 			for (int i = 0; i < contributors.length; i++) {
 				// Run in safe mode so that anything happens we don't go away.
 				final int ii = i;
 				Platform.run(new ISafeRunnable() {
 					public void handleException(Throwable exception) {
 						// Don't need to do anything. Platform.run logs it for me.
 					}
 
 					public void run() throws Exception {
 						contributors[ii].contributeClasspaths(controller);
 					}
 				});
 			}
 		}
 
 		// Add in the required ones by the Proxy support. These are hard-coded since they are
 		// required.
 		ProxyRemoteUtil.updateClassPaths(controller);
 
 		classpath = controller.getFinalClasspath();
 		if (bootpathInfo[0] != controller.getFinalPrependBootpath()) {
 		    if (vmAttributesMap == null)
 		        vmAttributesMap = new HashMap(2);
 		    vmAttributesMap.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND, ProxyLaunchSupport.convertURLsToStrings(bootpathInfo[0]));
 		}
 		if (bootpathInfo[2] != controller.getFinalAppendBootpath()) {
 		    if (vmAttributesMap == null)
 		        vmAttributesMap = new HashMap(2);
 		    vmAttributesMap.put(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_APPEND, ProxyLaunchSupport.convertURLsToStrings(bootpathInfo[2]));
 		}
 
 		// check for cancellation
 		if (pm.isCanceled())
 			return;
 		pm.worked(100);
 
 		// Create VM config
 		VMRunnerConfiguration runConfig =
 			new VMRunnerConfiguration("org.eclipse.jem.internal.proxy.vm.remote.RemoteVMApplication", ProxyLaunchSupport.convertURLsToStrings(classpath)); //$NON-NLS-1$
 
 		REMProxyFactoryRegistry registry = new REMProxyFactoryRegistry(ProxyRemoteUtil.getRegistryController(), name);
 		Integer registryKey = registry.getRegistryKey();
 
 		Integer bufSize = Integer.getInteger("proxyvm.bufsize"); //$NON-NLS-1$
 		if (bufSize == null)
 			bufSize = new Integer(16000);
 
 		int masterServerPort = ProxyRemoteUtil.getRegistryController().getMasterSocketPort();
 
 		// See if debug mode is requested.
 		DebugModeHelper dh = new DebugModeHelper();
 		boolean debugMode = dh.debugMode(name);
 
 		String[] evmArgs = execArgs.getVMArgumentsArray();
 		int extraArgs = 4;	// Number of extra standard args added (if number changes below, this must change)
 		if (debugMode)
 			extraArgs+=4;	// Number of extra args added for debug mode (if number changes below, this must change).
 		List javaLibPaths = controller.getFinalJavaLibraryPath();
 		int existingLibpaths = -1;
 		if (!javaLibPaths.isEmpty()) {
 			// first need to see if java lib path also specified in standard args by someone configuring the configuration by hand.
 			for (int i = 0; i < evmArgs.length; i++) {
 				if (evmArgs[i].startsWith("-Djava.library.path")) { //$NON-NLS-1$
 					// We found one already here, save the spot so we update it later.
 					existingLibpaths = i;
 					break;
 				}
 			}
 			if (existingLibpaths == -1)
 				++extraArgs;	// Need to have room for one more.
 		}
 		
 		String[] cvmArgs = new String[evmArgs.length + extraArgs];
 		System.arraycopy(evmArgs, 0, cvmArgs, extraArgs, evmArgs.length);	// Put existing into new list at the end.		
 		
 		cvmArgs[0] = "-Dproxyvm.registryKey=" + registryKey; //$NON-NLS-1$
 		cvmArgs[1] = "-Dproxyvm.masterPort=" + String.valueOf(masterServerPort); //$NON-NLS-1$
 		cvmArgs[2] = "-Dproxyvm.bufsize=" + bufSize; //$NON-NLS-1$
 		cvmArgs[3] = "-Dproxyvm.servername=" + name; //$NON-NLS-1$
 
 		// If in debug mode, we need to find a port for it to use.
 		int dport = -1;
 		if (debugMode) {
 			dport = findUnusedLocalPort("localhost", 5000, 15000, new int[0]); //$NON-NLS-1$
 			cvmArgs[4] = "-Djava.compiler=NONE"; //$NON-NLS-1$
 			cvmArgs[5] = "-Xdebug"; //$NON-NLS-1$
 			cvmArgs[6] = "-Xnoagent"; //$NON-NLS-1$
 			cvmArgs[7] = "-Xrunjdwp:transport=dt_socket,server=y,address=" + dport; //$NON-NLS-1$
 		}
 		
 		if (!javaLibPaths.isEmpty()) {
 			StringBuffer appendTo = null;
 			if (existingLibpaths != -1) {
 				appendTo = new StringBuffer(evmArgs[existingLibpaths]); 
 				appendTo.append(File.pathSeparatorChar);	// Plus a separator so we can append
 			} else 
 				appendTo = new StringBuffer("-Djava.library.path="); //$NON-NLS-1$
			String [] libPaths = ProxyLaunchSupport.convertURLsToStrings((URL[]) javaLibPaths.toArray(new URL[javaLibPaths.size()]));
			for (int i = 0; i < libPaths.length; i++) {
 				if (i != 0)
 					appendTo.append(File.pathSeparator);
				appendTo.append(libPaths[i]);
 			}
 			if (existingLibpaths != -1)
 				cvmArgs[extraArgs+existingLibpaths] = appendTo.toString();
 			else
 				cvmArgs[extraArgs-1] = appendTo.toString();
 		}
 
 		runConfig.setProgramArguments(execArgs.getProgramArgumentsArray());
 		runConfig.setEnvironment(envp);
 		runConfig.setVMArguments(cvmArgs);
 		runConfig.setWorkingDirectory(workingDirName);
 		runConfig.setVMSpecificAttributesMap(vmAttributesMap);
 
 		// Bootpath
 		runConfig.setBootClassPath(getBootpath(configuration));
 
 		// check for cancellation
 		if (pm.isCanceled())
 			return;
 		pm.worked(100);
 
 		// set the default source locator if required
 		setDefaultSourceLocator(launch, configuration);
 
 		// Launch the configuration - 1 unit of work
 		runner.run(runConfig, launch, new SubProgressMonitor(pm, 100));
 
 		// check for cancellation
 		if (pm.isCanceled())
 			return;
 
 		IProcess[] processes = launch.getProcesses();
 		IProcess process = processes[0]; // There is only one.
 		// Check if it is already terminated. If it is, then there was a bad error, so just
 		// print out the results from it.
 		if (process.isTerminated()) {
 			IStreamsProxy stProxy = process.getStreamsProxy();
 			// Using a printWriter for println capability, but it needs to be on another
 			// writer, which will be string
 			java.io.StringWriter s = new java.io.StringWriter();
 			java.io.PrintWriter w = new java.io.PrintWriter(s);
 
 			w.println(ProxyRemoteMessages.getString(ProxyRemoteMessages.VM_TERMINATED));
 			w.println(ProxyRemoteMessages.getString(ProxyRemoteMessages.VM_TERMINATED_LINE1));
 			w.println(stProxy.getErrorStreamMonitor().getContents());
 			w.println(ProxyRemoteMessages.getString(ProxyRemoteMessages.VM_TERMINATED_LINE2));
 			w.println(stProxy.getOutputStreamMonitor().getContents());
 			w.println(ProxyRemoteMessages.getString(ProxyRemoteMessages.VM_TERMINATED_LINE3));
 			w.close();
 
 			String msg = MessageFormat.format(ProxyRemoteMessages.getString("Proxy_Terminated_too_soon_ERROR_"), new Object[] { name }); //$NON-NLS-1$
 			dh.displayErrorMessage(ProxyRemoteMessages.getString("Proxy_Error_Title"), msg); //$NON-NLS-1$
 			throw new CoreException(
 				new Status(IStatus.WARNING, ProxyPlugin.getPlugin().getBundle().getSymbolicName(), 0, s.toString(), null));
 		} else {
 			final String traceName = name;
 			IStreamsProxy fStreamsProxy = process.getStreamsProxy();
 
 			class StreamListener implements IStreamListener {
 				String tracePrefix;
 				Level level;
 
 				public StreamListener(String type, Level level) {
 					tracePrefix = traceName + ':' + type + '>';
 					this.level = level;
 				}
 
 				public void streamAppended(String newText, IStreamMonitor monitor) {
 					ProxyPlugin.getPlugin().getLogger().log(tracePrefix + newText, level);
 				}
 			};
 
 			// Always listen to System.err output.
 			IStreamMonitor monitor = fStreamsProxy.getErrorStreamMonitor();
 			if (monitor != null)
 				monitor.addListener(new StreamListener("err", Level.WARNING)); //$NON-NLS-1$
 
 			// If debug trace is requested, then attach trace listener for System.out
 			if ("true".equalsIgnoreCase(Platform.getDebugOption(ProxyPlugin.getPlugin().getBundle().getSymbolicName() + ProxyRemoteUtil.DEBUG_VM_TRACEOUT))) { //$NON-NLS-1$
 				// Want to trace the output of the remote vm's.
 
 				monitor = fStreamsProxy.getOutputStreamMonitor();
 				if (monitor != null)
 					monitor.addListener(new StreamListener("out", Level.INFO)); //$NON-NLS-1$							
 			}
 		}
 
 		// If in debug mode, tester must start debugger before going on.
 		if (debugMode) {
 			if (!dh.promptPort(dport)) {
 				process.terminate();
 				throw new CoreException(
 					new Status(
 						IStatus.WARNING,
 						ProxyPlugin.getPlugin().getBundle().getSymbolicName(),
 						0,
 						"Debugger attach canceled", //$NON-NLS-1$
 						null));
 			}
 		}
 
 		// Now set up the registry.
 		registry.initializeRegistry(process);
 		new REMStandardBeanTypeProxyFactory(registry);
 		new REMStandardBeanProxyFactory(registry);
 		new REMMethodProxyFactory(registry);
 
 		if (debugMode || REMProxyFactoryRegistry.fGlobalNoTimeouts)
 			registry.fNoTimeouts = true;
 		if (configuration.getAttribute(IProxyConstants.ATTRIBUTE_AWT_SWING, true))
 			REMRegisterAWT.registerAWT(registry);
 
 		launchInfo.resultRegistry = registry;
 
 		pm.done();
 	}
 
 	// Utilities to find the free port
 	private static final Random fgRandom = new Random(System.currentTimeMillis());
 
 	private static int findUnusedLocalPort(String host, int searchFrom, int searchTo, int[] exclude) {
 		for (int i = 0; i < 10; i++) {
 			int port = 0;
 			newport : while (true) {
 				port = getRandomPort(searchFrom, searchTo);
 				if (exclude != null)
 					for (int e = 0; e < exclude.length; e++)
 						if (port == exclude[e])
 							continue newport;
 				break;
 			}
 			try {
 				new Socket(host, port);
 			} catch (ConnectException e) {
 				return port;
 			} catch (IOException e) {
 			}
 		}
 		return -1;
 	}
 
 	private static int getRandomPort(int low, int high) {
 		return (int) (fgRandom.nextFloat() * (high - low)) + low;
 	}
 	
 	private String[][] getBootpathExt(Map vmMap) {
 	    String[][] ext = new String[3][];
 	    if (vmMap != null) {
 		    ext[0] = (String[]) vmMap.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_PREPEND);
 		    ext[1] = (String[]) vmMap.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH);
 		    ext[2] = (String[]) vmMap.get(IJavaLaunchConfigurationConstants.ATTR_BOOTPATH_APPEND);
 	    }
 	    return ext;
 	}
 
 }
