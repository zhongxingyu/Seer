 /**********************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
 * 
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.server.core.model;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import org.eclipse.core.runtime.*;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.internal.Messages;
 import org.eclipse.wst.server.core.internal.ProgressUtil;
 import org.eclipse.wst.server.core.internal.Server;
 import org.eclipse.wst.server.core.internal.ServerPlugin;
 import org.eclipse.wst.server.core.internal.Trace;
 /**
  * A server delegate provides the implementation for various 
  * generic and server-type-specific operations for a specific type of server.
  * A server delegate is specified by the
  * <code>class</code> attribute of a <code>serverTypes</code> extension.
  * <p>
  * When the server instance needs to be given a delegate, the delegate class
  * specified for the server type is instantiated with a 0-argument constructor
  * and primed with <code>delegate.initialize(((IServerState)server)</code>, 
  * which it is expected to hang on to. Later, when
  * <code>delegate.dispose()</code> is called as the server instance is
  * being discarded, the delegate is expected to let go of the server instance.
  * </p>
  * <p>
  * Server delegates may keep state in instance fields, but that state is
  * transient and will not be persisted across workbench sessions.
  * </p>
  * <p>
  * This abstract class is intended to be extended only by clients
  * to extend the <code>serverTypes</code> extension point.
  * </p>
  * 
  * @see org.eclipse.wst.server.core.IServer
  * @see org.eclipse.wst.server.core.IServerWorkingCopy
  * @plannedfor 1.0
  */
 public abstract class ServerBehaviourDelegate {
 	private Server server;
 
 	/**
 	 * Publish kind constant (value 0) for no change.
 	 * 
 	 * @see #publishModule(int, int, IModule[], IProgressMonitor)
 	 */
 	public static final int NO_CHANGE = 0;
 
 	/**
 	 * Publish kind constant (value 1) for added resources.
 	 * 
 	 * @see #publishModule(int, int, IModule[], IProgressMonitor)
 	 */
 	public static final int ADDED = 1;
 
 	/**
 	 * Publish kind constant (value 2) for changed resources.
 	 * 
 	 * @see #publishModule(int, int, IModule[], IProgressMonitor)
 	 */
 	public static final int CHANGED = 2;
 
 	/**
 	 * Publish kind constant (value 3) for removed resources.
 	 * 
 	 * @see #publishModule(int, int, IModule[], IProgressMonitor)
 	 */
 	public static final int REMOVED = 3;
 
 	/**
 	 * Delegates must have a public 0-arg constructor.
 	 */
 	public ServerBehaviourDelegate() {
 		// do nothing
 	}
 
 	/**
 	 * Initializes this server delegate with its life-long server instance.
 	 * <p>
 	 * This method is called by the server core framework.
 	 * Clients should never call this method.
 	 * </p>
 	 * 
 	 * @param newServer the server instance
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 */
 	final void initialize(Server newServer, IProgressMonitor monitor) {
 		server = newServer;
 		initialize(monitor);
 	}
 
 	/**
 	 * Initializes this server delegate. This method gives delegates a chance
 	 * to do their own initialization.
 	 * <p>
 	 * If the server state is initially unknown, this method should attempt
 	 * to connect to the server and update the state. On servers where the
 	 * state may change, this is also an excellent place to create a background
 	 * thread that will constantly ping the server (or have a listener) to
 	 * update the server state as changes occur.
 	 * </p>
 	 * <p>
 	 * This method is called by the server core framework.
 	 * Clients should never call this method.
 	 * </p>
 	 */
 	protected void initialize(IProgressMonitor monitor) {
 		// do nothing
 	}
 
 	/**
 	 * Returns the server that this server delegate corresponds to.
 	 * 
 	 * @return the server
 	 */
 	public final IServer getServer() {
 		return server;
 	}
 
 	/**
 	 * Sets the current state of this server.
 	 *
 	 * @param state the current state of the server, one of the state
 	 *    constants defined by {@link IServer}
 	 * @see IServer#getServerState()
 	 */
 	protected final void setServerState(int state) {
 		server.setServerState(state);
 	}
 
 	/**
 	 * Sets the ILaunchManager mode that the server is running in. The server
 	 * implementation will automatically return <code>null</code> to clients
 	 * when the server is stopped, so you only need to update the mode when
 	 * it changes.
 	 * 
 	 * @param mode the mode in which a server is running, one of the mode constants
 	 *    defined by {@link org.eclipse.debug.core.ILaunchManager}
 	 */
 	protected final void setMode(String mode) {
 		server.setMode(mode);
 	}
 
 	/**
 	 * Sets the server restart state.
 	 *
 	 * @param state <code>true</code> if the server needs to be restarted,
 	 *    and <code>false</code> otherwise
 	 */
 	protected final void setServerRestartState(boolean state) {
 		server.setServerRestartState(state);
 	}
 
 	/**
 	 * Sets the server publish state.
 	 *
 	 * @param state the current publish state of the server, one of the
 	 *    publish constants defined by {@link IServer}
 	 */
 	protected final void setServerPublishState(int state) {
 		server.setServerPublishState(state);
 	}
 
 	/**
 	 * Hook to fire an event when a module state changes.
 	 * 
 	 * @param module the module
 	 * @param state the current state of the module, one of the state
 	 *    constants defined by {@link IServer}
 	 */
 	protected final void setModuleState(IModule[] module, int state) {
 		server.setModuleState(module, state);
 	}
 
 	/**
 	 * Sets the module publish state.
 	 *
 	 * @param module the module
 	 * @param state the current publish state of the module, one of the
 	 *    publish constants defined by {@link IServer}
 	 */
 	protected final void setModulePublishState(IModule[] module, int state) {
 		server.setModulePublishState(module, state);
 	}
 
 	/**
 	 * Sets the module restart state.
 	 *
 	 * @param module the module
 	 * @param state <code>true</code> if the module needs to be restarted,
 	 *    and <code>false</code> otherwise
 	 */
 	protected final void setModuleRestartState(IModule[] module, boolean state) {
 		server.setModuleRestartState(module, state);
 	}
 
 	/**
 	 * Disposes of this server delegate.
 	 * <p>
 	 * This method is called by the web server core framework.
 	 * Clients should never call this method.
 	 * </p>
 	 * <p>
 	 * Implementations are expected to let go of the delegate's reference
 	 * to the server, deregister listeners, etc.
 	 * </p>
 	 */
 	public void dispose() {
 		// do nothing
 	}
 
 	/**
 	 * Methods called to notify that publishing is about to begin.
 	 * This allows the server to open a connection to the server
 	 * or get any global information ready.
 	 * <p>
 	 * This method is called by the server core framework,
 	 * in response to a call to <code>IServer.publish()</code>.
 	 * Clients should never call this method.
 	 * </p>
 	 *
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @throws CoreException if there is a problem starting the publish
 	 */
 	protected void publishStart(IProgressMonitor monitor) throws CoreException {
 		// do nothing
 	}
 
 	/**
 	 * Publish the server.
 	 * <p>
 	 * This method is called by the server core framework,
 	 * in response to a call to <code>IServer.publish()</code>.
 	 * Clients should never call this method.
 	 * </p>
 	 * 
 	 * @param kind one of the IServer.PUBLISH_XX constants. Valid values are
 	 *    <ul>
 	 *    <li><code>PUBLSIH_FULL</code>- indicates a full publish.</li>
 	 *    <li><code>PUBLISH_INCREMENTAL</code>- indicates a incremental publish.
 	 *    <li><code>PUBLSIH_AUTO</code>- indicates an automatic incremental publish.</li>
 	 *    <li><code>PUBLISH_CLEAN</code>- indicates a clean request. Clean throws
 	 *      out all state and cleans up the module on the server before doing a
 	 *      full publish.
 	 *    </ul>
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @throws CoreException if there is a problem publishing the server
 	 */
 	protected void publishServer(int kind, IProgressMonitor monitor) throws CoreException {
 		// do nothing
 	}
 
 	/**
 	 * Publish a single module to the server.
 	 * <p>
 	 * This method is called by the server core framework,
 	 * in response to a call to <code>IServer.publish()</code>.
 	 * Clients should never call this method directly.
 	 * </p>
 	 * <p>
 	 * If the deltaKind is IServer.REMOVED, the module may have been completely
 	 * deleted and does not exist anymore. In this case, a dummy module (with the
 	 * correct id) will be passed to this method.
 	 * </p>
 	 * 
 	 * @param kind one of the IServer.PUBLISH_XX constants. Valid values are:
 	 *    <ul>
 	 *    <li><code>PUBLSIH_FULL</code>- indicates a full publish.</li>
 	 *    <li><code>PUBLISH_INCREMENTAL</code>- indicates a incremental publish.
 	 *    <li><code>PUBLSIH_AUTO</code>- indicates an automatic incremental publish.</li>
 	 *    <li><code>PUBLISH_CLEAN</code>- indicates a clean request. Clean throws
 	 *      out all state and cleans up the module on the server before doing a
 	 *      full publish.
 	 *    </ul>
 	 * @param module the module to publish
 	 * @param deltaKind one of the IServer publish change constants. Valid values are:
 	 *    <ul>
 	 *    <li><code>ADDED</code>- indicates the module has just been added to the server
 	 *      and this is the first publish.
 	 *    <li><code>NO_CHANGE</code>- indicates that nothing has changed in the module
 	 *      since the last publish.</li>
 	 *    <li><code>CHANGED</code>- indicates that the module has been changed since
 	 *      the last publish. Call <code>getPublishedResourceDelta()</code> for
 	 *      details of the change.
 	 *    <li><code>REMOVED</code>- indicates the module has been removed and should be
 	 *      removed/cleaned up from the server.
 	 *    </ul>
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @throws CoreException if there is a problem publishing the module
 	 */
 	protected void publishModule(int kind, int deltaKind, IModule[] module, IProgressMonitor monitor) throws CoreException {
 		// do nothing
 	}
 
 	/**
 	 * Methods called to notify that publishing has finished.
 	 * The server can close any open connections to the server
 	 * and do any cleanup operations.
 	 * <p>
 	 * This method is called by the server core framework,
 	 * in response to a call to <code>IServer.publish()</code>.
 	 * Clients should never call this method.
 	 * </p>
 	 *
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @throws CoreException if there is a problem stopping the publish
 	 */
 	protected void publishFinish(IProgressMonitor monitor) throws CoreException {
 		// do nothing
 	}
 
 	/**
 	 * Configure the given launch configuration to start this server. This method is called whenever
 	 * the server is started to ensure that the launch configuration is accurate and up to date.
 	 * This method should not blindly update the launch configuration in cases where the user has
 	 * access to change the launch configuration by hand.
 	 * 
 	 * @param workingCopy a launch configuration working copy
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @throws CoreException if there is an error setting up the configuration
 	 */
 	public void setupLaunchConfiguration(ILaunchConfigurationWorkingCopy workingCopy, IProgressMonitor monitor) throws CoreException {
 		// do nothing
 	}
 
 	/**
 	 * Restart this server. The server should use the server
 	 * listener to notify progress. It must use the same debug
 	 * flags as was originally passed into the start() method.
 	 * 
 	 * This method is used if there is a quick/better way to restart
 	 * the server. If it throws a CoreException, the normal stop/start
 	 * actions will be used.
 	 * 
 	 * @param launchMode the mode to restart in, one of the mode constants
 	 *    defined by {@link org.eclipse.debug.core.ILaunchManager}
 	 * @throws CoreException if there was a problem restarting
 	 */
 	public void restart(String launchMode) throws CoreException {
 		 throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, "Could not restart", null));
 	}
 
 	/**
 	 * Returns whether the given module can be restarted.
 	 * <p>
 	 * [issue: It's unclear whether this operations is guaranteed to be fast
 	 * or whether it could involve communication with any actual
 	 * server. If it is not fast, the method should take a progress
 	 * monitor.]
 	 * </p>
 	 * 
 	 * @param module the module
 	 * @return <code>true</code> if the given module can be
 	 * restarted, and <code>false</code> otherwise 
 	 */
 	public boolean canControlModule(IModule[] module) {
 		return false;
 	}
 	
 	/**
 	 * Starts the given module on the server. See the specification of 
 	 * {@link IServer#startModule(IModule[], IServer.IOperationListener)}
 	 * for further details. 
 	 * <p>
 	 * The implementation should update the module sync state and fire
 	 * an event for the module.
 	 * </p>
 	 * <p>
 	 * This method will throw an exception if the module does not exist on
 	 * the server.
 	 * </p>
 	 * <p>
 	 * [issue: Since this method is ascynchronous, is there
 	 * any need for the progress monitor?]
 	 * </p>
 	 * 
 	 * @param module the module to be started
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @exception CoreException if an error occurs while trying to restart the module
 	 */
 	public void startModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
 		// do nothing
 	}
 
 	/**
 	 * Stops the given module on the server. See the specification of 
 	 * {@link IServer#stopModule(IModule[], IServer.IOperationListener)}
 	 * for further details. 
 	 * <p>
 	 * The implementation should update the module sync state and fire
 	 * an event for the module.
 	 * </p>
 	 * <p>
 	 * This method will throw an exception if the module does not exist on
 	 * the server.
 	 * </p>
 	 * <p>
 	 * [issue: Since this method is ascynchronous, is there
 	 * any need for the progress monitor?]
 	 * </p>
 	 * 
 	 * @param module the module to be stopped
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @exception CoreException if an error occurs while trying to restart the module
 	 */
 	public void stopModule(IModule[] module, IProgressMonitor monitor) throws CoreException {
 		// do nothing
 	}
 
 	/**
 	 * Shuts down and stops this server. The server should return from this method
 	 * quickly and use the server listener to notify shutdown progress.
 	 * <p> 
 	 * If force is <code>false</code>, it will attempt to stop the server
 	 * normally/gracefully. If force is <code>true</code>, then the server
 	 * process will be terminated any way that it can.
 	 * </p>
 	 * <p>
 	 * [issue: There is no way to communicate failure to the
 	 * client. Given that this operation can go awry, there probably
 	 * should be a mechanism that allows failing asynch operations
 	 * to be diagnosed.]
 	 * </p>
 	 * @param force <code>true</code> to kill the server, or <code>false</code>
 	 *    to stop normally
 	 */
 	public abstract void stop(boolean force);
 
 	/**
 	 * Returns the module resources that have been published to the server.
 	 * 
 	 * <p>
 	 * If the module has just been added to the server, an empty list will
 	 * be returned. If the module has never existed on the server, a CoreException
 	 * will be thrown.
 	 * </p>
 	 * 
 	 * @param module the module
 	 * @return an array containing the published module resource
 	 */
 	protected IModuleResource[] getPublishedResources(IModule[] module) {
 		return server.getPublishedResources(module);
 	}
 
 	/**
 	 * Returns the delta of the current module resources that have been
 	 * published compared to the current state of the module.
 	 *
 	 * @param module the module
 	 * @return an array containing the publish resource delta
 	 */
 	protected IModuleResourceDelta[] getPublishedResourceDelta(IModule[] module) {
 		return server.getPublishedResourceDelta(module);
 	}
 
 	/**
 	 * Returns a temporary directory that the requestor can use
 	 * throughout it's lifecycle. This is primary to be used by
 	 * servers for working directories, server specific
 	 * files, etc.
 	 * <p>
 	 * This method directory will return the same directory on
 	 * each use of the workbench. If the directory is not requested
 	 * over a period of time, the directory may be deleted and a
 	 * new one will be assigned on the next request. For this
 	 * reason, a server may want to request the temp directory on
 	 * startup if it wants to store files there. In any case, the
 	 * server should have a backup plan to refill the directory
 	 * in case it has been deleted since last use.</p>
 	 *
 	 * @return a temporary directory
 	 */
 	protected IPath getTempDirectory() {
 		return server.getTempDirectory();
 	}
 
 	/**
 	 * Set a global status on the server.
 	 *  
 	 * @param status the status
 	 */
 	protected final void setServerStatus(IStatus status) {
 		server.setServerStatus(status);
 	}
 
 	/**
 	 * Set a status on a specific module.
 	 * 
 	 * @param module the module
 	 * @param status the status
 	 */
 	protected final void setModuleStatus(IModule[] module, IStatus status) {
 		server.setModuleStatus(module, status);
 	}
 	
 	public IStatus publish(int kind, IProgressMonitor monitor) {
 		Trace.trace(Trace.FINEST, "-->-- Publishing to server: " + toString() + " -->--");
 		
		if (getServer().getRuntime() == null)
 			return new Status(IStatus.INFO, ServerPlugin.PLUGIN_ID, 0, Messages.errorPublishNoRuntime, null);

 		final List moduleList = getAllModules();
 		final List kindList = new ArrayList();
 		
 		Iterator iterator = moduleList.iterator();
 		while (iterator.hasNext()) {
 			IModule[] module = (IModule[]) iterator.next();
 			if (hasBeenPublished(module)) {
 				if (getPublishedResourceDelta(module).length == 0)
 					kindList.add(new Integer(ServerBehaviourDelegate.NO_CHANGE));
 				else
 					kindList.add(new Integer(ServerBehaviourDelegate.CHANGED));
 			} else
 				kindList.add(new Integer(ServerBehaviourDelegate.ADDED));
 		}
 		
 		addRemovedModules(moduleList, kindList);
 		
 		while (moduleList.size() > kindList.size()) {
 			kindList.add(new Integer(ServerBehaviourDelegate.REMOVED));
 		}
 		
 		PublishOperation[] tasks = getTasks(kind, moduleList, kindList);
 		int size = 2000 + 3500 * moduleList.size() + 500 * tasks.length;
 		
 		monitor = ProgressUtil.getMonitorFor(monitor);
 		monitor.beginTask(NLS.bind(Messages.publishing, getServer().getName()), size);
 
 		// TODO - group up status until the end and use better message based on success or failure
 		MultiStatus multi = new MultiStatus(ServerPlugin.PLUGIN_ID, 0, Messages.publishingStatus, null);
 
 		if (monitor.isCanceled())
 			return new Status(IStatus.INFO, ServerPlugin.PLUGIN_ID, 0, Messages.publishingCancelled, null);
 		
 		// start publishing
 		Trace.trace(Trace.FINEST, "Calling publishStart()");
 		try {
 			publishStart(ProgressUtil.getSubMonitorFor(monitor, 1000));
 		} catch (CoreException ce) {
 			Trace.trace(Trace.INFO, "CoreException publishing to " + toString(), ce);
 			return ce.getStatus();
 		}
 		
 		// perform tasks
 		MultiStatus taskStatus = performTasks(tasks, monitor);
 		if (taskStatus != null)
 			multi.addAll(taskStatus);
 		
 		// publish the server
 		try {
 			if (!monitor.isCanceled()) {
 				publishServer(kind, ProgressUtil.getSubMonitorFor(monitor, 1000));
 			}
 		} catch (CoreException ce) {
 			Trace.trace(Trace.INFO, "CoreException publishing to " + toString(), ce);
 			multi.add(ce.getStatus());
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error publishing configuration to " + toString(), e);
 			multi.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorPublishing, e));
 		}
 		
 		// publish modules
 		if (!monitor.isCanceled()) {
 			try {
 				publishModules(kind, moduleList, kindList, multi, monitor);
 			} catch (Exception e) {
 				Trace.trace(Trace.WARNING, "Error while publishing modules", e);
 				multi.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorPublishing, e));
 			}
 		}
 		
 		// end the publishing
 		Trace.trace(Trace.FINEST, "Calling publishFinish()");
 		try {
 			publishFinish(ProgressUtil.getSubMonitorFor(monitor, 500));
 		} catch (CoreException ce) {
 			Trace.trace(Trace.INFO, "CoreException publishing to " + toString(), ce);
 			multi.add(ce.getStatus());
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error stopping publish to " + toString(), e);
 			multi.add(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, Messages.errorPublishing, e));
 		}
 		
 		if (monitor.isCanceled()) {
 			IStatus status = new Status(IStatus.INFO, ServerPlugin.PLUGIN_ID, 0, Messages.publishingCancelled, null);
 			multi.add(status);
 		}
 		
 		monitor.done();
 		
 		Trace.trace(Trace.FINEST, "--<-- Done publishing --<--");
 		
 		if (multi.getChildren().length == 1)
 			return multi.getChildren()[0];
 		
 		return multi;
 	}
 
 	/**
 	 * Publish a single module.
 	 */
 	protected IStatus publishModule(int kind, IModule[] module, int deltaKind, IProgressMonitor monitor) {
 		Trace.trace(Trace.FINEST, "Publishing module: " + module);
 		
 		int size = module.length;
 		IModule m = module[size - 1];
 		monitor.beginTask(NLS.bind(Messages.publishingModule, m.getName()), 1000);
 		
 		IStatus status = new Status(IStatus.OK, ServerPlugin.PLUGIN_ID, 0, NLS.bind(Messages.publishedModule, m.getName()), null);
 		try {
 			publishModule(kind, deltaKind, module, monitor);
 		} catch (CoreException ce) {
 			status = ce.getStatus();
 		}
 		
 		/*Trace.trace(Trace.FINEST, "Delta:");
 		IModuleResourceDelta[] delta = getServerPublishInfo().getDelta(parents, module);
 		int size = delta.length;
 		for (int i = 0; i < size; i++) {
 			((ModuleResourceDelta)delta[i]).trace(">  ");
 		}*/
 		updatePublishInfo(deltaKind, module);
 		
 		monitor.done();
 		
 		Trace.trace(Trace.FINEST, "Done publishing: " + module);
 		return status;
 	}
 	
 	protected boolean hasBeenPublished(IModule[] module) {
 		return server.getServerPublishInfo().hasModulePublishInfo(module);
 	}
 	
 	protected void addRemovedModules(List moduleList, List kindList) {
 		server.getServerPublishInfo().addRemovedModules(moduleList, kindList);
 	}
 	
 	protected void updatePublishInfo(int deltaKind, IModule[] module) {
 		if (deltaKind == ServerBehaviourDelegate.REMOVED)
 			server.getServerPublishInfo().removeModulePublishInfo(module);
 		else
 			server.getServerPublishInfo().fill(module);
 	}
 
 	/**
 	 * Publishes the given modules. Returns true if the publishing
 	 * should continue, or false if publishing has failed or is cancelled.
 	 * 
 	 * Uses 500 ticks plus 3500 ticks per module
 	 */
 	protected void publishModules(int kind, List modules, List deltaKind, MultiStatus multi, IProgressMonitor monitor) {
 		if (modules == null)
 			return;
 		
 		int size = modules.size();
 		if (size == 0)
 			return;
 		
 		if (monitor.isCanceled())
 			return;
 		
 		// publish modules
 		for (int i = 0; i < size; i++) {
 			IStatus status = publishModule(kind, (IModule[]) modules.get(i), ((Integer)deltaKind.get(i)).intValue(), ProgressUtil.getSubMonitorFor(monitor, 3000));
 			multi.add(status);
 		}
 	}
 
 	/**
 	 * Returns the publish tasks that have been targetted to this server.
 	 * These tasks should be run during publishing.
 	 * 
 	 * @param kind one of the IServer.PUBLISH_XX constants
 	 * @param moduleList a list of modules
 	 * @param kindList list of one of the IServer publish change constants
 	 * @return a possibly empty array of IOptionalTasks
 	 */
 	protected final PublishOperation[] getTasks(int kind, List moduleList, List kindList) {
 		return server.getTasks(kind, moduleList, kindList);
 	}
 
 	/**
 	 * Returns all the modules that are on the server, including root
 	 * modules and all their children.
 	 * 
 	 * @return a list of IModule[]s
 	 */
 	protected final List getAllModules() {
 		return server.getAllModules();
 	}
 
 	/**
 	 * Perform (execute) all the given tasks.
 	 * 
 	 * @param tasks an array of tasks
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @return the status
 	 */
 	protected MultiStatus performTasks(PublishOperation[] tasks, IProgressMonitor monitor) {
 		int size = tasks.length;
 		Trace.trace(Trace.FINEST, "Performing tasks: " + size);
 		
 		if (size == 0)
 			return null;
 		
 		MultiStatus multi = new MultiStatus(ServerPlugin.PLUGIN_ID, 0, Messages.taskPerforming, null);
 		
 		for (int i = 0; i < size; i++) {
 			PublishOperation task = tasks[i];
 			monitor.subTask(NLS.bind(Messages.taskPerforming, task.toString()));
 			try {
 				task.execute(ProgressUtil.getSubMonitorFor(monitor, 500), null);
 			} catch (CoreException ce) {
 				Trace.trace(Trace.SEVERE, "Task failed", ce);
 				multi.add(ce.getStatus());
 			}
 			
 			// return early if the monitor has been cancelled
 			if (monitor.isCanceled())
 				return multi;
 		}
 		
 		return multi;
 	}
 
 	/**
 	 * Called when resources change within the workspace.
 	 * This gives the server an opportunity to update the server or module
 	 * restart state.
 	 */
 	public void handleResourceChange() {
 		// do nothing
 	}
 }
