 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    IBM Corporation - Initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.server.tomcat.core.internal;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.*;
 import org.eclipse.debug.core.*;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jdt.launching.IRuntimeClasspathEntry;
 import org.eclipse.jdt.launching.IVMInstall;
 import org.eclipse.jdt.launching.JavaRuntime;
 import org.eclipse.osgi.util.NLS;
 
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.internal.IModulePublishHelper;
 import org.eclipse.wst.server.core.internal.Server;
 import org.eclipse.wst.server.core.model.*;
 import org.eclipse.wst.server.core.util.PublishHelper;
 import org.eclipse.wst.server.core.util.SocketUtil;
 /**
  * Generic Tomcat server.
  */
 public class TomcatServerBehaviour extends ServerBehaviourDelegate implements ITomcatServerBehaviour, IModulePublishHelper {
 	private static final String ATTR_STOP = "stop-server";
 	
 	private static final String[] JMX_EXCLUDE_ARGS = new String [] {
 		"-Dcom.sun.management.jmxremote",
 		"-Dcom.sun.management.jmxremote.port=",
 		"-Dcom.sun.management.jmxremote.ssl=",
 		"-Dcom.sun.management.jmxremote.authenticate="
 	};
 
 	// the thread used to ping the server to check for startup
 	protected transient PingThread ping = null;
 	protected transient IDebugEventSetListener processListener;
 
 	/**
 	 * TomcatServerBehaviour.
 	 */
 	public TomcatServerBehaviour() {
 		super();
 	}
 
 	public void initialize(IProgressMonitor monitor) {
 		// do nothing
 	}
 
 	public TomcatRuntime getTomcatRuntime() {
 		if (getServer().getRuntime() == null)
 			return null;
 		
 		return (TomcatRuntime) getServer().getRuntime().loadAdapter(TomcatRuntime.class, null);
 	}
 
 	public ITomcatVersionHandler getTomcatVersionHandler() {
 		return getTomcatServer().getTomcatVersionHandler();
 	}
 
 	public TomcatConfiguration getTomcatConfiguration() throws CoreException {
 		return getTomcatServer().getTomcatConfiguration();
 	}
 
 	public TomcatServer getTomcatServer() {
 		return (TomcatServer) getServer().loadAdapter(TomcatServer.class, null);
 	}
 
 	/**
 	 * Return the runtime class name.
 	 *
 	 * @return the class name
 	 */
 	public String getRuntimeClass() {
 		return getTomcatVersionHandler().getRuntimeClass();
 	}
 	
 	/**
 	 * Returns the runtime base path for relative paths in the server
 	 * configuration.
 	 * 
 	 * @return the base path
 	 */
 	public IPath getRuntimeBaseDirectory() {
 		return getTomcatServer().getRuntimeBaseDirectory();
 	}
 
 	/**
 	 * Return the program's runtime arguments to start or stop.
 	 *
 	 * @param starting true if starting
 	 * @return an array of runtime program arguments
 	 */
 	protected String[] getRuntimeProgramArguments(boolean starting) {
 		IPath configPath = null;
 		if (getTomcatServer().isTestEnvironment())
 			configPath = getRuntimeBaseDirectory();
 		return getTomcatVersionHandler().getRuntimeProgramArguments(configPath, getTomcatServer().isDebug(), starting);
 	}
 
 	protected String[] getExcludedRuntimeProgramArguments(boolean starting) {
 		return getTomcatVersionHandler().getExcludedRuntimeProgramArguments(getTomcatServer().isDebug(), starting);
 	}
 
 	/**
 	 * Return the runtime (VM) arguments.
 	 *
 	 * @return an array of runtime arguments
 	 */
 	protected String[] getRuntimeVMArguments() {
 		IPath installPath = getServer().getRuntime().getLocation();
 		IPath configPath = getRuntimeBaseDirectory();
 		IPath deployPath;
 		// If serving modules without publishing, use workspace path as the deploy path
 		if (getTomcatServer().isServeModulesWithoutPublish()) {
 			deployPath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
 		}
 		// Else normal publishing for modules
 		else {
 			deployPath = getServerDeployDirectory();
 		}
 		return getTomcatVersionHandler().getRuntimeVMArguments(installPath, configPath,
 				deployPath, getTomcatServer().isTestEnvironment());
 	}
 	
 	protected String getRuntimePolicyFile() {
 		IPath configPath = getRuntimeBaseDirectory();
 		return getTomcatVersionHandler().getRuntimePolicyFile(configPath);
 	}
 	
 	protected static String renderCommandLine(String[] commandLine, String separator) {
 		if (commandLine == null || commandLine.length < 1)
 			return "";
 		StringBuffer buf= new StringBuffer(commandLine[0]);
 		for (int i = 1; i < commandLine.length; i++) {
 			buf.append(separator);
 			buf.append(commandLine[i]);
 		}	
 		return buf.toString();
 	}
 
 	protected void addProcessListener(final IProcess newProcess) {
 		if (processListener != null || newProcess == null)
 			return;
 		
 		processListener = new IDebugEventSetListener() {
 			public void handleDebugEvents(DebugEvent[] events) {
 				if (events != null) {
 					int size = events.length;
 					for (int i = 0; i < size; i++) {
 						if (newProcess != null && newProcess.equals(events[i].getSource()) && events[i].getKind() == DebugEvent.TERMINATE) {
 							stopImpl();
 						}
 					}
 				}
 			}
 		};
 		DebugPlugin.getDefault().addDebugEventListener(processListener);
 	}
 
 	protected void setServerStarted() {
 		setServerState(IServer.STATE_STARTED);
 	}
 
 	protected void stopImpl() {
 		if (ping != null) {
 			ping.stop();
 			ping = null;
 		}
 		if (processListener != null) {
 			DebugPlugin.getDefault().removeDebugEventListener(processListener);
 			processListener = null;
 		}
 		setServerState(IServer.STATE_STOPPED);
 	}
 
 	protected void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
 		if (getServer().getRuntime() == null)
 			return;
 
 		IPath installDir = getServer().getRuntime().getLocation();
 		IPath confDir = null;
 		if (getTomcatServer().isTestEnvironment()) {
 			confDir = getRuntimeBaseDirectory();
 			IStatus status = getTomcatVersionHandler().prepareRuntimeDirectory(confDir);
 			if (status != null && !status.isOK())
 				throw new CoreException(status);
 		} else
 			confDir = installDir;
 		IStatus status = getTomcatVersionHandler().prepareDeployDirectory(getServerDeployDirectory());
 		if (status != null && !status.isOK())
 			throw new CoreException(status);
 
 		monitor = ProgressUtil.getMonitorFor(monitor);
 		monitor.beginTask(Messages.publishServerTask, 600);
 		
 		status = getTomcatConfiguration().cleanupServer(confDir, installDir,
 				!getTomcatServer().isSaveSeparateContextFiles(), ProgressUtil.getSubMonitorFor(monitor, 100));
 		if (status != null && !status.isOK())
 			throw new CoreException(status);
 		
 		status = getTomcatConfiguration().backupAndPublish(confDir, !getTomcatServer().isTestEnvironment(), ProgressUtil.getSubMonitorFor(monitor, 400));
 		if (status != null && !status.isOK())
 			throw new CoreException(status);
 		
 		status = getTomcatConfiguration().localizeConfiguration(confDir, getServerDeployDirectory(),
 				getTomcatServer(), ProgressUtil.getSubMonitorFor(monitor, 100));
 		if (status != null && !status.isOK())
 			throw new CoreException(status);
 		
 		monitor.done();
 		
 		setServerPublishState(IServer.PUBLISH_STATE_NONE);
 	}
 
 	/*
 	 * Publishes the given module to the server.
 	 */
 	protected void publishModule(int kind, int deltaKind, IModule[] moduleTree, IProgressMonitor monitor) throws CoreException {
 		if (getServer().getServerState() != IServer.STATE_STOPPED) {
 			if (deltaKind == ServerBehaviourDelegate.ADDED || deltaKind == ServerBehaviourDelegate.REMOVED)
 				setServerRestartState(true);
 		}
 		if (getTomcatServer().isTestEnvironment())
 			return;
 
 		IPath path = getTempDirectory().append("publish.txt");
 		Properties p = new Properties();
 		FileInputStream fin = null;
 		try {
 			fin = new FileInputStream(path.toFile());
 			p.load(fin);
 		} catch (Exception e) {
 			// ignore
 		} finally {
 			try {
 				fin.close();
 			} catch (Exception ex) {
 				// ignore
 			}
 		}
 		
 		PublishHelper helper = new PublishHelper(getRuntimeBaseDirectory().append("temp").toFile());
 		if (moduleTree.length == 1) // web module
 			publishDir(deltaKind, p, moduleTree, helper, monitor);
 		else // utility jar
 			publishJar(kind, deltaKind, p, moduleTree, helper, monitor);
 		
 		setModulePublishState(moduleTree, IServer.PUBLISH_STATE_NONE);
 		
 		try {
 			p.store(new FileOutputStream(path.toFile()), "Tomcat publish data");
 		} catch (Exception e) {
 			// ignore
 		}
 	}
 
 	/**
 	 * Publish a web module.
 	 * 
 	 * @param deltaKind
 	 * @param p
 	 * @param module
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	private void publishDir(int deltaKind, Properties p, IModule module[], PublishHelper helper, IProgressMonitor monitor) throws CoreException {
 		List status = new ArrayList();
 		// Remove if requested or if previously published and are now serving without publishing
 		if (deltaKind == REMOVED || getTomcatServer().isServeModulesWithoutPublish()) {
 			String publishPath = (String) p.get(module[0].getId());
 			if (publishPath != null) {
 				try {
 					File f = new File(publishPath);
 					if (f.exists()) {
 						IStatus[] stat = PublishHelper.deleteDirectory(f, monitor);
 						PublishOperation2.addArrayToList(status, stat);
 					}
 				} catch (Exception e) {
 					throw new CoreException(new Status(IStatus.WARNING, TomcatPlugin.PLUGIN_ID, 0,
 							NLS.bind(Messages.errorPublishCouldNotRemoveModule,module[0].getName()), e));
 				}
				p.remove(module[0].getId());
 			}
 		} else {
 			IPath path = getModuleDeployDirectory(module[0]);
 			IModuleResource[] mr = getResources(module);
 			IPath [] jarPaths = null;
 			IModule [] childModules = getServer().getChildModules(module, monitor);
 			if (childModules != null && childModules.length > 0) {
 				jarPaths = new IPath[childModules.length];
 				for (int i = 0; i < childModules.length; i++) {
 					jarPaths[i] = new Path("WEB-INF/lib").append(childModules[i].getName() + ".jar");
 				}
 			}
 			IStatus[] stat = helper.publishSmart(mr, path, jarPaths, monitor);
 			PublishOperation2.addArrayToList(status, stat);
 			p.put(module[0].getId(), path.toOSString());
 		}
 		PublishOperation2.throwException(status);
 	}
 
 	/**
 	 * Publish a jar file.
 	 * 
 	 * @param deltaKind
 	 * @param p
 	 * @param module
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	private void publishJar(int kind, int deltaKind, Properties p, IModule[] module, PublishHelper helper, IProgressMonitor monitor) throws CoreException {
 		// Remove if requested or if previously published and are now serving without publishing
 		if (deltaKind == REMOVED || getTomcatServer().isServeModulesWithoutPublish()) {
 			try {
 				String publishPath = (String) p.get(module[1].getId());
				if (publishPath != null) {
					new File(publishPath).delete();
					p.remove(module[1].getId());
				}
 			} catch (Exception e) {
 				throw new CoreException(new Status(IStatus.WARNING, TomcatPlugin.PLUGIN_ID, 0, "Could not remove module", e));
 			}
 		} else {
 			IPath path = getModuleDeployDirectory(module[0]);
 			path = path.append("WEB-INF").append("lib");
 			IPath jarPath = path.append(module[1].getName() + ".jar");
 			if (!path.toFile().exists()) {
 				path.toFile().mkdirs();
 			} else {
 				// If file still exists and we are not forcing a new one to be built
 				if (jarPath.toFile().exists() && kind != IServer.PUBLISH_CLEAN && kind != IServer.PUBLISH_FULL) {
 					// avoid changes if no changes to module since last publish
 					IModuleResourceDelta[] delta = getPublishedResourceDelta(module);
 					if (delta == null || delta.length == 0)
 						return;
 				}
 			}
 			
 			IModuleResource[] mr = getResources(module);
 			IStatus[] stat = helper.publishZip(mr, jarPath, monitor);
 			List status = new ArrayList();
 			PublishOperation2.addArrayToList(status, stat);
 			PublishOperation2.throwException(status);
 			p.put(module[1].getId(), jarPath.toOSString());
 		}
 	}
 
 	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
 		IStatus status;
 		IPath baseDir = getRuntimeBaseDirectory();
 		TomcatServer ts = getTomcatServer();
 		ITomcatVersionHandler tvh = getTomcatVersionHandler();
 		// Include or remove loader jar depending on state of serving directly 
 		status = tvh.prepareForServingDirectly(baseDir, getTomcatServer());
 		if (status.isOK()) {
 			// If serving modules directly, update server.xml accordingly (includes project context.xmls)
 			if (ts.isServeModulesWithoutPublish()) {
 				status = getTomcatConfiguration().updateContextsToServeDirectly(
 						baseDir, tvh.getSharedLoader(baseDir), monitor);
 			}
 			// Else serving normally. Add project context.xmls to server.xml
 			else {
 				// Publish context configuration for servers that support META-INF/context.xml
 				status = getTomcatConfiguration().publishContextConfig(
 						baseDir, getServerDeployDirectory(), monitor);
 			}
 			if (status.isOK() && ts.isSaveSeparateContextFiles()) {
 				// Determine if context's path attribute should be removed
 				String id = getServer().getServerType().getId();
 				boolean noPath = id.indexOf("55") > 0 || id.indexOf("60") > 0;
 				boolean serverStopped = getServer().getServerState() == IServer.STATE_STOPPED;
 				// TODO Add a monitor
 				TomcatVersionHelper.moveContextsToSeparateFiles(baseDir, noPath, serverStopped, null);
 			}
 		}
 		if (!status.isOK())
 			throw new CoreException(status);
 	}
 
 	/**
 	 * Setup for starting the server.
 	 * 
 	 * @param launch ILaunch
 	 * @param launchMode String
 	 * @param monitor IProgressMonitor
 	 * @throws CoreException if anything goes wrong
 	 */
 	public void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor) throws CoreException {
 		if ("true".equals(launch.getLaunchConfiguration().getAttribute(ATTR_STOP, "false")))
 			return;
 		//if (getTomcatRuntime() == null)
 		//	throw new CoreException();
 		
 		IStatus status = getTomcatRuntime().validate();
 		if (status != null && status.getSeverity() == IStatus.ERROR)
 			throw new CoreException(status);
 		
 		//setRestartNeeded(false);
 		TomcatConfiguration configuration = getTomcatConfiguration();
 		
 		// check that ports are free
 		Iterator iterator = configuration.getServerPorts().iterator();
 		List usedPorts = new ArrayList();
 		while (iterator.hasNext()) {
 			ServerPort sp = (ServerPort) iterator.next();
 			if (sp.getPort() < 0)
 				throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, Messages.errorPortInvalid, null));
 			if (SocketUtil.isPortInUse(sp.getPort(), 5)) {
 				usedPorts.add(sp);
 			}
 		}
 		if (usedPorts.size() == 1) {
 			ServerPort port = (ServerPort) usedPorts.get(0);
 			throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorPortInUse, new String[] {port.getPort() + "", getServer().getName()}), null));
 		} else if (usedPorts.size() > 1) {
 			String portStr = "";
 			iterator = usedPorts.iterator();
 			boolean first = true;
 			while (iterator.hasNext()) {
 				if (!first)
 					portStr += ", ";
 				first = false;
 				ServerPort sp = (ServerPort) iterator.next();
 				portStr += "" + sp.getPort();
 			}
 			throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorPortsInUse, new String[] {portStr, getServer().getName()}), null));
 		}
 		
 		// check that there is only one app for each context root
 		iterator = configuration.getWebModules().iterator();
 		List contextRoots = new ArrayList();
 		while (iterator.hasNext()) {
 			WebModule module = (WebModule) iterator.next();
 			String contextRoot = module.getPath();
 			if (contextRoots.contains(contextRoot))
 				throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(Messages.errorDuplicateContextRoot, new String[] { contextRoot }), null));
 			
 			contextRoots.add(contextRoot);
 		}
 		
 		setServerRestartState(false);
 		setServerState(IServer.STATE_STARTING);
 		setMode(launchMode);
 		
 		// ping server to check for startup
 		try {
 			String url = "http://localhost";
 			int port = configuration.getMainPort().getPort();
 			if (port != 80)
 				url += ":" + port;
 			ping = new PingThread(getServer(), url, -1, this);
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Can't ping for Tomcat startup.");
 		}
 	}
 
 	/**
 	 * Cleanly shuts down and terminates the server.
 	 * 
 	 * @param force <code>true</code> to kill the server
 	 */
 	public void stop(boolean force) {
 		if (force) {
 			terminate();
 			return;
 		}
 		int state = getServer().getServerState();
 		if (state == IServer.STATE_STOPPED)
 			return;
 		else if (state == IServer.STATE_STARTING || state == IServer.STATE_STOPPING) {
 			terminate();
 			return;
 		}
 		
 		try {
 			if (Trace.isTraceEnabled())
 				Trace.trace(Trace.FINER, "Stopping Tomcat");
 			if (state != IServer.STATE_STOPPED)
 				setServerState(IServer.STATE_STOPPING);
 	
 			ILaunchConfiguration launchConfig = ((Server)getServer()).getLaunchConfiguration(true, null);
 			ILaunchConfigurationWorkingCopy wc = launchConfig.getWorkingCopy();
 			
 			String args = renderCommandLine(getRuntimeProgramArguments(false), " ");
 			// Remove JMX arguments if present
 			String existingVMArgs = wc.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
 			if (existingVMArgs.indexOf(JMX_EXCLUDE_ARGS[0]) >= 0) {
 				wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, 
 						mergeArguments(existingVMArgs, new String [] {}, JMX_EXCLUDE_ARGS, false));
 			}
 			wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, args);
 			wc.setAttribute("org.eclipse.debug.ui.private", true);
 			wc.setAttribute(ATTR_STOP, "true");
 			wc.launch(ILaunchManager.RUN_MODE, new NullProgressMonitor());
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error stopping Tomcat", e);
 		}
 	}
 
 	/**
 	 * Terminates the server.
 	 */
 	protected void terminate() {
 		if (getServer().getServerState() == IServer.STATE_STOPPED)
 			return;
 		
 		try {
 			setServerState(IServer.STATE_STOPPING);
 			if (Trace.isTraceEnabled())
 				Trace.trace(Trace.FINER, "Killing the Tomcat process");
 			ILaunch launch = getServer().getLaunch();
 			if (launch != null) {
 				launch.terminate();
 				stopImpl();
 			}
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error killing the process", e);
 		}
 	}
 
 	public IPath getTempDirectory() {
 		return super.getTempDirectory();
 	}
 
 	/**
 	 * Return a string representation of this object.
 	 * @return java.lang.String
 	 */
 	public String toString() {
 		return "TomcatServer";
 	}
 	
 	protected static int getNextToken(String s, int start) {
 		int i = start;
 		int length = s.length();
 		char lookFor = ' ';
 		
 		while (i < length) {
 			char c = s.charAt(i);
 			if (lookFor == c) {
 				if (lookFor == '"')
 					return i+1;
 				return i;
 			}
 			if (c == '"')
 				lookFor = '"';
 			i++;
 		}
 		return -1;
 	}
 	
 	/**
 	 * Merge the given arguments into the original argument string, replacing
 	 * invalid values if they have been changed.  Special handling is provided
 	 * if the keepActionLast argument is true and the last vmArg is a simple
 	 * string.  The vmArgs will be merged such that the last vmArg is guaranteed
 	 * to be the last argument in the merged string.
 	 * 
 	 * @param originalArg String of original arguments.
 	 * @param vmArgs Arguments to merge into the original arguments string
 	 * @param excludeArgs Arguments to exclude from the original arguments string
 	 * @param keepActionLast If <b>true</b> the vmArguments are assumed to be Tomcat
 	 * program arguments, the last of which is the action to perform which must
 	 * remain the last argument.  This only has an impact if the last vmArg is
 	 * a simple string argument, like &quot;start&quot;.
 	 * @return merged argument string
 	 */
 	public static String mergeArguments(String originalArg, String[] vmArgs, String[] excludeArgs, boolean keepActionLast) {
 		if (vmArgs == null)
 			return originalArg;
 		
 		if (originalArg == null)
 			originalArg = "";
 		
 		// replace and null out all vmargs that already exist
 		int size = vmArgs.length;
 		for (int i = 0; i < size; i++) {
 			int ind = vmArgs[i].indexOf(" ");
 			int ind2 = vmArgs[i].indexOf("=");
 			if (ind >= 0 && (ind2 == -1 || ind < ind2)) { // -a bc style
 				int index = originalArg.indexOf(vmArgs[i].substring(0, ind + 1));
 				if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
 					// replace
 					String s = originalArg.substring(0, index);
 					int index2 = getNextToken(originalArg, index + ind + 1);
 					if (index2 >= 0)
 						originalArg = s + vmArgs[i] + originalArg.substring(index2);
 					else
 						originalArg = s + vmArgs[i];
 					vmArgs[i] = null;
 				}
 			} else if (ind2 >= 0) { // a=b style
 				int index = originalArg.indexOf(vmArgs[i].substring(0, ind2 + 1));
 				if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
 					// replace
 					String s = originalArg.substring(0, index);
 					int index2 = getNextToken(originalArg, index);
 					if (index2 >= 0)
 						originalArg = s + vmArgs[i] + originalArg.substring(index2);
 					else
 						originalArg = s + vmArgs[i];
 					vmArgs[i] = null;
 				}
 			} else { // abc style
 				int index = originalArg.indexOf(vmArgs[i]);
 				if (index == 0 || (index > 0 && originalArg.charAt(index-1) == ' ')) {
 					// replace
 					String s = originalArg.substring(0, index);
 					int index2 = getNextToken(originalArg, index);
 					if (!keepActionLast || i < (size - 1)) {
 						if (index2 >= 0)
 							originalArg = s + vmArgs[i] + originalArg.substring(index2);
 						else
 							originalArg = s + vmArgs[i];
 						vmArgs[i] = null;
 					}
 					else {
 						// The last VM argument needs to remain last,
 						// remove original arg and append the vmArg later
 						if (index2 >= 0)
 							originalArg = s + originalArg.substring(index2);
 						else
 							originalArg = s;
 					}
 				}
 			}
 		}
 
 		// remove excluded arguments
 		if (excludeArgs != null && excludeArgs.length > 0) {
 			for (int i = 0; i < excludeArgs.length; i++) {
 				int ind = excludeArgs[i].indexOf(" ");
 				int ind2 = excludeArgs[i].indexOf("=");
 				if (ind >= 0 && (ind2 == -1 || ind < ind2)) { // -a bc style
 					int index = originalArg.indexOf(excludeArgs[i].substring(0, ind + 1));
 					if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
 						// remove
 						String s = originalArg.substring(0, index);
 						int index2 = getNextToken(originalArg, index + ind + 1);
 						if (index2 >= 0) {
 							// If remainder will become the first argument, remove leading blanks
 							while (index2 < originalArg.length() && originalArg.charAt(index2) == ' ')
 								index2 += 1;
 							originalArg = s + originalArg.substring(index2);
 						}
 						else
 							originalArg = s;
 					}
 				} else if (ind2 >= 0) { // a=b style
 					int index = originalArg.indexOf(excludeArgs[i].substring(0, ind2 + 1));
 					if (index == 0 || (index > 0 && originalArg.charAt(index - 1) == ' ')) {
 						// remove
 						String s = originalArg.substring(0, index);
 						int index2 = getNextToken(originalArg, index);
 						if (index2 >= 0) {
 							// If remainder will become the first argument, remove leading blanks
 							while (index2 < originalArg.length() && originalArg.charAt(index2) == ' ')
 								index2 += 1;
 							originalArg = s + originalArg.substring(index2);
 						}
 						else
 							originalArg = s;
 					}
 				} else { // abc style
 					int index = originalArg.indexOf(excludeArgs[i]);
 					if (index == 0 || (index > 0 && originalArg.charAt(index-1) == ' ')) {
 						// remove
 						String s = originalArg.substring(0, index);
 						int index2 = getNextToken(originalArg, index);
 						if (index2 >= 0) {
 							// Remove leading blanks
 							while (index2 < originalArg.length() && originalArg.charAt(index2) == ' ')
 								index2 += 1;
 							originalArg = s + originalArg.substring(index2);
 						}
 						else
 							originalArg = s;
 					}
 				}
 			}
 		}
 
 		// add remaining vmargs to the end
 		for (int i = 0; i < size; i++) {
 			if (vmArgs[i] != null) {
 				if (originalArg.length() > 0 && !originalArg.endsWith(" "))
 					originalArg += " ";
 				originalArg += vmArgs[i];
 			}
 		}
 		
 		return originalArg;
 	}
 
 	/**
 	 * Replace the current JRE container classpath with the given entry.
 	 * 
 	 * @param cp
 	 * @param entry
 	 */
 	public static void replaceJREContainer(List cp, IRuntimeClasspathEntry entry) {
 		int size = cp.size();
 		for (int i = 0; i < size; i++) {
 			IRuntimeClasspathEntry entry2 = (IRuntimeClasspathEntry) cp.get(i);
 			if (entry2.getPath().uptoSegment(2).isPrefixOf(entry.getPath())) {
 				cp.set(i, entry);
 				return;
 			}
 		}
 		
 		cp.add(0, entry);
 	}
 
 	/**
 	 * Merge a single classpath entry into the classpath list.
 	 * 
 	 * @param cp
 	 * @param entry
 	 */
 	public static void mergeClasspath(List cp, IRuntimeClasspathEntry entry) {
 		Iterator iterator = cp.iterator();
 		while (iterator.hasNext()) {
 			IRuntimeClasspathEntry entry2 = (IRuntimeClasspathEntry) iterator.next();
 			
 			if (entry2.getPath().equals(entry.getPath()))
 				return;
 		}
 		
 		cp.add(entry);
 	}
 
 	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
 		String existingProgArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS, (String)null);
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_PROGRAM_ARGUMENTS,
 				mergeArguments(existingProgArgs, getRuntimeProgramArguments(true), getExcludedRuntimeProgramArguments(true), true));
 		
 		String existingVMArgs = workingCopy.getAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS, (String)null);
 		String[] parsedVMArgs = null;
 		if (null != existingVMArgs) {
 			parsedVMArgs = DebugPlugin.parseArguments(existingVMArgs);
 		}
 		String [] configVMArgs = getRuntimeVMArguments();
 		if (getTomcatServer().isSecure()) {
 			boolean addSecurityArgs = true;
 			if (null != parsedVMArgs) {
 				for (int i = 0; i < parsedVMArgs.length; i++) {
 					if (parsedVMArgs[i].startsWith("wtp.configured.security")) {
 						addSecurityArgs = false;
 						break;
 					}
 				}
 			}
 			if (addSecurityArgs) {
 				String [] newVMArgs = new String [configVMArgs.length + 3];
 				System.arraycopy(configVMArgs, 0, newVMArgs, 0, configVMArgs.length);
 				newVMArgs[configVMArgs.length] = "-Djava.security.manager";
 				newVMArgs[configVMArgs.length + 1] = "-Djava.security.policy=\""
 						+ getRuntimePolicyFile() +"\"";
 				newVMArgs[configVMArgs.length + 2] = "-Dwtp.configured.security=true";
 				configVMArgs = newVMArgs;
 			}
 		}
 		else if (null != parsedVMArgs){
 			boolean removeSecurityArgs = false;
 			for (int i = 0; i < parsedVMArgs.length; i++) {
 				if (parsedVMArgs[i].startsWith("-Dwtp.configured.security")) {
 					removeSecurityArgs = true;
 					break;
 				}
 			}
 			if (removeSecurityArgs) {
 				StringBuffer filteredVMArgs = new StringBuffer();
 				for (int i = 0; i < parsedVMArgs.length; i++) {
 					String arg = parsedVMArgs[i];
 					if (!arg.startsWith("-Djava.security.manager")
 							&& !arg.startsWith("-Djava.security.policy=")
 							&& !arg.startsWith("-Dwtp.configured.security=")) {
 						if (filteredVMArgs.length() > 0) {
 							filteredVMArgs.append(' ');
 						}
 						filteredVMArgs.append(arg);
 					}
 				}
 				existingVMArgs = filteredVMArgs.toString();
 			}
 		}
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_VM_ARGUMENTS,
 				mergeArguments(existingVMArgs, configVMArgs, null, false));
 		
 		ITomcatRuntime runtime = getTomcatRuntime();
 		IVMInstall vmInstall = runtime.getVMInstall();
 		if (vmInstall != null)
 			workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_JRE_CONTAINER_PATH, JavaRuntime.newJREContainerPath(vmInstall).toPortableString());
 		
 		// update classpath
 		IRuntimeClasspathEntry[] originalClasspath = JavaRuntime.computeUnresolvedRuntimeClasspath(workingCopy);
 		int size = originalClasspath.length;
 		List oldCp = new ArrayList(originalClasspath.length + 2);
 		for (int i = 0; i < size; i++)
 			oldCp.add(originalClasspath[i]);
 		
 		List cp2 = runtime.getRuntimeClasspath();
 		Iterator iterator = cp2.iterator();
 		while (iterator.hasNext()) {
 			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) iterator.next();
 			mergeClasspath(oldCp, entry);
 		}
 		
 		if (vmInstall != null) {
 			try {
 				String typeId = vmInstall.getVMInstallType().getId();
 				replaceJREContainer(oldCp, JavaRuntime.newRuntimeContainerClasspathEntry(new Path(JavaRuntime.JRE_CONTAINER).append(typeId).append(vmInstall.getName()), IRuntimeClasspathEntry.BOOTSTRAP_CLASSES));
 			} catch (Exception e) {
 				// ignore
 			}
 			
 			IPath jrePath = new Path(vmInstall.getInstallLocation().getAbsolutePath());
 			if (jrePath != null) {
 				IPath toolsPath = jrePath.append("lib").append("tools.jar");
 				if (toolsPath.toFile().exists()) {
 					IRuntimeClasspathEntry toolsJar = JavaRuntime.newArchiveRuntimeClasspathEntry(toolsPath);
 					// Search for index to any existing tools.jar entry
 					int toolsIndex;
 					for (toolsIndex = 0; toolsIndex < oldCp.size(); toolsIndex++ ) {
 						IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) oldCp.get(toolsIndex);
 						if (entry.getType() == IRuntimeClasspathEntry.ARCHIVE
 								&& entry.getPath().lastSegment().equals("tools.jar")) {
 							break;
 						}
 					}
 					// If existing tools.jar found, replace in case it's different.  Otherwise add.
 					if (toolsIndex < oldCp.size())
 						oldCp.set(toolsIndex, toolsJar); 
 					else
 						mergeClasspath(oldCp, toolsJar);
 				}
 			}
 		}
 		
 		iterator = oldCp.iterator();
 		List list = new ArrayList();
 		while (iterator.hasNext()) {
 			IRuntimeClasspathEntry entry = (IRuntimeClasspathEntry) iterator.next();
 			try {
 				list.add(entry.getMemento());
 			} catch (Exception e) {
 				Trace.trace(Trace.SEVERE, "Could not resolve classpath entry: " + entry, e);
 			}
 		}
 		
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_CLASSPATH, list);
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_DEFAULT_CLASSPATH, false);
 	}
 
 	protected IModuleResource[] getResources(IModule[] module) {
 		return super.getResources(module);
 	}
 
 	protected IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
 		return super.getPublishedResourceDelta(module);
 	}
 
 	/**
 	 * @see ServerBehaviourDelegate#handleResourceChange()
 	 */
 	public void handleResourceChange() {
 		if (getServer().getServerRestartState())
 			return;
 		
 		Iterator iterator = getAllModules().iterator();
 		while (iterator.hasNext()) {
 			IModule[] module = (IModule[]) iterator.next();
 			IModuleResourceDelta[] delta = getPublishedResourceDelta(module);
 			if (delta == null || delta.length == 0)
 				continue;
 			
 			if (containsNonResourceChange(delta)) {
 				setServerRestartState(true);
 				return;
 			}
 		}
 	}
 
 	protected boolean containsNonResourceChange(IModuleResourceDelta[] delta) {
 		int size = delta.length;
 		for (int i = 0; i < size; i++) {
 			IModuleResourceDelta d = delta[i];
 			if (d.getModuleRelativePath().segmentCount() == 0) {
 				if ("WEB-INF".equals(d.getModuleResource().getName())) {
 					return containsNonResourceChange(d.getAffectedChildren());
 				}
 				continue;
 			}
 			if (d.getModuleResource() instanceof IModuleFile)
 				return true;
 			
 			boolean b = containsNonAddChange(d.getAffectedChildren());
 			if (b)
 				return true;
 		}
 		return false;
 	}
 
 	protected boolean containsNonAddChange(IModuleResourceDelta[] delta) {
 		if (delta == null)
 			return false;
 		int size = delta.length;
 		for (int i = 0; i < size; i++) {
 			IModuleResourceDelta d = delta[i];
 			if (d.getModuleResource() instanceof IModuleFile) {
 				if (d.getKind() != IModuleResourceDelta.ADDED)
 					return true;
 			}
 			
 			boolean b = containsNonAddChange(d.getAffectedChildren());
 			if (b)
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Cleans the entire work directory for this server.  This involves
 	 * deleting all subdirectories of the server's work directory.
 	 * 
 	 * @param monitor a progress monitor
 	 * @return results of the clean operation
 	 * @throws CoreException
 	 */
 	public IStatus cleanServerWorkDir(IProgressMonitor monitor) throws CoreException {
 		IStatus result;
 		IPath basePath = getRuntimeBaseDirectory();
 		IPath workPath = getTomcatConfiguration().getServerWorkDirectory(basePath);
 		if (workPath != null) {
 			File workDir = workPath.toFile();
 			result = Status.OK_STATUS;
 			if (workDir.exists() && workDir.isDirectory()) {
 				// Delete subdirectories of the server's work dir
 				File[] files = workDir.listFiles();
 				if (files != null && files.length > 0) {
 					MultiStatus ms = new MultiStatus(TomcatPlugin.PLUGIN_ID, 0, "Problem occurred deleting work directory for module.", null);
 					int size = files.length;
 					monitor = ProgressUtil.getMonitorFor(monitor);
 					monitor.beginTask(NLS.bind("Cleaning Server Work Directory", new String[] { workDir.getAbsolutePath() }), size * 10);
 
 					for (int i = 0; i < size; i++) {
 						File current = files[i];
 						if (current.isDirectory()) {
 							IStatus [] results = PublishHelper.deleteDirectory(current, ProgressUtil.getSubMonitorFor(monitor, 10));
 							if (results != null && results.length > 0) {
 								for (int j = 0; j < results.length; j++) {
 									ms.add(results[j]);
 								}
 							}
 						}
 					}
 					monitor.done();
 					result = ms;
 				}
 			}
 		}
 		else {
 			result = new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, "Could not determine work directory for module", null);
 		}
 		return result;
 	}
 	
 	/**
 	 * Cleans the work directory associated with the specified module on this
 	 * server.
 	 * 
 	 * @param module module whose work directory should be cleaned
 	 * @param monitor a progress monitor
 	 * @return result of the clean operation
 	 * @throws CoreException
 	 */
 	public IStatus cleanContextWorkDir(ITomcatWebModule module, IProgressMonitor monitor) throws CoreException {
 		IStatus result;
 		IPath basePath = getRuntimeBaseDirectory();
 		IPath workPath = getTomcatConfiguration().getContextWorkDirectory(basePath, module);
 		if (workPath != null) {
 			IStatus [] results = PublishHelper.deleteDirectory(workPath.toFile(), monitor);
 			MultiStatus ms = new MultiStatus(TomcatPlugin.PLUGIN_ID, 0, "Problem occurred deleting work directory for module.", null);
 			if (results != null && results.length > 0) {
 				for (int i = 0; i < results.length; i++) {
 					ms.add(results[i]);
 				}
 			}
 			result = ms;
 		}
 		else {
 			result = new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, "Could not determine work directory for module", null);
 		}
 		return result;
 	}
 	
 	/**
 	 * Gets the directory to which modules should be deployed for
 	 * this server.
 	 * 
 	 * @return full path to deployment directory for the server
 	 */
 	public IPath getServerDeployDirectory() {
 		return getTomcatServer().getServerDeployDirectory();
 	}
 	
 	/**
 	 * Gets the directory to which to deploy a module's web application.
 	 * 
 	 * @param module a module
 	 * @return full path to deployment directory for the module
 	 */
 	public IPath getModuleDeployDirectory(IModule module) {
 		return getServerDeployDirectory().append(module.getName());
 	}
 	
 	/**
 	 * Temporary method to help web services team. Returns the path that the module is
 	 * published to.
 	 * 
 	 * @param module a module on the server 
 	 * @return the path that the module is published to when in test environment mode,
 	 *    or null if the module is not a web module
 	 */
 	public IPath getPublishDirectory(IModule[] module) {
 		if (module == null || module.length != 1)
 			return null;
 		
 		return getModuleDeployDirectory(module[0]);
 	}
 
 	public void setModulePublishState2(IModule[] module, int state) {
 		setModulePublishState(module, state);
 	}
 }
