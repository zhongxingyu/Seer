 /*******************************************************************************
  * Copyright (c) 2005 BEA Systems, Inc. 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    rfrost@bea.com - initial API and implementation
  *    
  *    Based on GenericServerBehavior by Gorkem Ercan
  *******************************************************************************/
 package org.eclipse.jst.server.generic.core.internal;
 
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.debug.core.ILaunchManager;
 import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
 import org.eclipse.jface.dialogs.ErrorDialog;
 import org.eclipse.jst.server.generic.internal.xml.Resolver;
 import org.eclipse.jst.server.generic.servertype.definition.External;
 import org.eclipse.jst.server.generic.servertype.definition.ServerRuntime;
import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.ServerPort;
 import org.eclipse.wst.server.core.internal.Server;
 import org.eclipse.wst.server.core.util.SocketUtil;
 
 /**
  * Subclass of <code>GenericServerBehavior</code> that supports 
  * servers which are started/stopped via external executables (e.g. scripts).
  */
 public class ExternalServerBehaviour extends GenericServerBehaviour {
 	
 	// config for debugging session
 	private ILaunchConfigurationWorkingCopy wc;
     private String mode;
     private ILaunch launch; 
     private IProgressMonitor monitor;
 	
     /**
 	 * Since the server may already be running, need to check if any of the server
 	 * ports are in use; if they are, need to set the server state to "unknown" so 
 	 * that the user can stop the server from the UI.
 	 */
     protected void initialize(IProgressMonitor monitor) {
 		ServerPort[] ports = getServer().getServerPorts(null);
 		ServerPort sp;
     	for(int i=0;i<ports.length;i++){
     		sp = ports[i];
     		if (SocketUtil.isPortInUse(sp.getPort(), 5)) {
     			Trace.trace(Trace.WARNING, "Port " + sp.getPort() + " is currently in use");
     			Status status = new Status(Status.WARNING, CorePlugin.PLUGIN_ID, Status.OK, 
     						GenericServerCoreMessages.bind(GenericServerCoreMessages.errorPortInUse,Integer.toString(sp.getPort()),sp.getName()), null);
     			setServerStatus(status);
     			setServerState(IServer.STATE_UNKNOWN);
     			return;
     		}
     	}
 	}
     
     /**
      * Override to reset the status if the state was unknown
      */
     public void stop(boolean force) {
     	resetStatus(getServer().getServerState());
     	super.stop(force);
     }
 
     /**
      * Override to reset the status if the state was unknown and an exception was not thrown
      */
     protected void setupLaunch(ILaunch launch, String launchMode, IProgressMonitor monitor) throws CoreException {
     	int state = getServer().getServerState();
     	super.setupLaunch(launch, launchMode, monitor);
     	resetStatus(state);
     }
     
 	/**
 	 * Override to trigger the launch of the debugging session (if appropriate).
 	 */
 	protected synchronized void setServerStarted() {
 		if (wc != null) {
 			try {
 				ExternalLaunchConfigurationDelegate.startDebugging(wc, mode, launch, monitor);
 			} catch (CoreException ce) {
 				// failed to start debugging, so set mode to run
 				setMode(ILaunchManager.RUN_MODE);
 				final Status status = new Status(IStatus.ERROR, CorePlugin.PLUGIN_ID, 1,
 							GenericServerCoreMessages.errorStartingExternalDebugging, ce); 
 				CorePlugin.getDefault().getLog().log(status);
 				Trace.trace(Trace.SEVERE, GenericServerCoreMessages.errorStartingExternalDebugging, ce);
 				// inform user via an error dialog
 				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
 					public void run() {
						ErrorDialog.openError(new Shell(PlatformUI.getWorkbench().getDisplay()), null, null, status);						
 					}
 				});
 			} finally {
 				clearDebuggingConfig();
 			}
 		}
 		setServerState(IServer.STATE_STARTED);
  	}
 	
 	/*
 	 * If the server state is unknown, reset the status to OK
 	 */
 	private void resetStatus(int state) {
 		if (state == IServer.STATE_UNKNOWN) {
 			setServerStatus(null);
 		}
 	}
 	
 	/**
 	 * Since terminate() is called during restart, need to override to
 	 * call shutdown instead of just killing the original process.
 	 */
 	protected void terminate() {
 		int state = getServer().getServerState();
 		if (state == IServer.STATE_STOPPED) 
     		return;
     
 		// need to execute a standard shutdown rather than
 		// just killing the original process
 		// the originally launched process may have only been
 		// a proxy and thus no longer executing
 		shutdown(state);
 	}
 	
 	/**
 	 * Override superclass method to correctly setup the launch configuration for starting an external
 	 * server.
 	 */
 	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy,
 										 IProgressMonitor monitor) throws CoreException {
 		clearDebuggingConfig();
 		ServerRuntime serverDef = getServerDefinition();
 		Resolver resolver = serverDef.getResolver();
 		workingCopy.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
 					resolver.resolveProperties(serverDef.getStart().getWorkingDirectory()));
 		String external = resolver.resolveProperties(getExternalForOS(serverDef.getStart().getExternal()));
 		workingCopy.setAttribute(ExternalLaunchConfigurationDelegate.COMMANDLINE, external);
 		workingCopy.setAttribute(ExternalLaunchConfigurationDelegate.DEBUG_PORT, 
 					resolver.resolveProperties(serverDef.getStart().getDebugPort()));
 		// just use the commandline for now
 		workingCopy.setAttribute(ExternalLaunchConfigurationDelegate.EXECUTABLE_NAME, external); 
 	}
 
 	/*
 	 * Returns the first external whose "os" attribute matches (case insensitive) the beginning 
 	 * of the name of the current OS (as determined by the System "os.name" property). If
 	 * no such match is found, returns the first external that does not have an OS attribute.
 	 */
 	private String getExternalForOS(List externals) {
 		String currentOS = System.getProperty("os.name").toLowerCase();
 		External external;
 		String matchingExternal = null;
 		String externalOS;
 		Iterator i = externals.iterator();
 		while (i.hasNext()) {
 			external= (External) i.next();
 			externalOS = external.getOs();
 			if (externalOS == null) {
 				if (matchingExternal == null) {
 					matchingExternal = external.getValue();
 				}
 			} else if (currentOS.startsWith(externalOS.toLowerCase())) {
 				matchingExternal = external.getValue();
 				break;
 			}
 		}
 		return matchingExternal;
 	}
 
 	/**
      * Returns the String ID of the launch configuration type.
      * @return
      */
 	protected String getConfigTypeID() {
 		return ExternalLaunchConfigurationDelegate.ID_EXTERNAL_LAUNCH_TYPE;
 	}
 
 	/**
 	 * Returns the String name of the stop launch configuration.
 	 * @return
 	 */
 	protected String getStopLaunchName() {
 		return GenericServerCoreMessages.externalStopLauncher;
 	}
 	
 	/**
 	 * Sets up the launch configuration for stopping the server.
 	 * @param workingCopy
 	 */
 	protected void setupStopLaunchConfiguration(GenericServerRuntime runtime, ILaunchConfigurationWorkingCopy wc) {
 		clearDebuggingConfig();
 		ServerRuntime serverDef = getServerDefinition();
 		Resolver resolver = serverDef.getResolver(); 
 		wc.setAttribute(IJavaLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY,
 					resolver.resolveProperties(serverDef.getStop().getWorkingDirectory()));
 		String external = resolver.resolveProperties(getExternalForOS(serverDef.getStop().getExternal()));
 		wc.setAttribute(ExternalLaunchConfigurationDelegate.COMMANDLINE, external);
 		// just use commandline for now
 		wc.setAttribute(ExternalLaunchConfigurationDelegate.EXECUTABLE_NAME, external); 	
 		wc.setAttribute(Server.ATTR_SERVER_ID, getServer().getId());
 	}
 	
 	/**
 	 * Sets the configuration to use for launching a debugging session
 	 */
 	protected synchronized void setDebuggingConfig(ILaunchConfigurationWorkingCopy wc,
 					 			      String mode,
 					 			      ILaunch launch, 
 					 			      IProgressMonitor monitor) {
 		this.wc = wc;
 		this.mode = mode;
 		this.launch = launch;
 		this.monitor = monitor;
 	}
 	
 	private synchronized void clearDebuggingConfig() {
 		this.wc = null;
 		this.mode = null;
 		this.launch = null;
 		this.monitor = null;
 	}
 	
 	
 }
