 /**********************************************************************
  * Copyright (c) 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
 *
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.server.core.model;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IServer;
 import org.eclipse.wst.server.core.internal.Server;
import org.eclipse.wst.server.core.internal.ServerPlugin;
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
  * <p>
  * <it>Caveat: The server core API is still in an early form, and is
  * likely to change significantly before the initial release.</it>
  * </p>
  * 
  * @see IServer
  * @see IServerWorkingCopy
  * @since 1.0
  */
 public abstract class ServerBehaviourDelegate {
 	private Server server;
 
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
 	 * @param server the server instance
 	 */
 	public final void initialize(Server newServer) {
 		server = newServer;
 		initialize();
 	}
 
 	/**
 	 * Initializes this server delegate. This method gives delegates a chance
 	 * to do their own initialization.
 	 * <p>
 	 * This method is called by the server core framework.
 	 * Clients should never call this method.
 	 * </p>
 	 */
 	public void initialize() {
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
 	 * @param state one of the server state (<code>STATE_XXX</code>)
 	 * constants declared on {@link IServer}
 	 * @see IServer#getServerState()
 	 */
 	public final void setServerState(int state) {
 		server.setServerState(state);
 	}
 	
 	public final void setMode(String mode) {
 		server.setMode(mode);
 	}
 
 	/**
 	 * Sets the server restart state.
 	 *
 	 * @param state boolean
 	 */
 	public final void setServerRestartState(boolean state) {
 		server.setServerRestartState(state);
 	}
 
 	/**
 	 * Sets the server publish state.
 	 *
 	 * @param state int
 	 */
 	public final void setServerPublishState(int state) {
 		server.setServerPublishState(state);
 	}
 
 	/**
 	 * Hook to fire an event when a module state changes.
 	 * 
 	 * @param module
 	 * @param state
 	 */
 	public final void setModuleState(IModule module, int state) {
 		server.setModuleState(module, state);
 	}
 
 	/**
 	 * Sets the module publish state.
 	 *
 	 * @param state int
 	 */
 	public final void setModulePublishState(IModule module, int state) {
 		server.setModulePublishState(module, state);
 	}
 	
 	/**
 	 * Sets the module restart state.
 	 *
 	 * @param state int
 	 */
 	public final void setModuleRestartState(IModule module, boolean state) {
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
 	 * The server configuration has changed. This method should return
 	 * quickly. If any republishing must occur, the relevant in-sync
 	 * methods should return a new value. If the server must be restarted,
 	 * the isRestartNeeded() method should return true.
 	 * 
 	 * @see IServer#updateConfiguration()
 	 */
 	//public abstract void updateConfiguration();
 
 	/**
 	 * A module resource has changed. This method should return
 	 * quickly. If the server must be restarted to handle the
 	 * change of this file, the isRestartNeeded() method should
 	 * return true and the event should be fired.
 	 *
 	 * @param module org.eclipse.wst.server.core.IModule
 	 * @param delta org.eclipse.wst.server.core.IModuleResourceDelta
 	 */
 	//public abstract void updateModule(IModule module, IModuleResourceDelta delta);
 
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
 	 * @param monitor org.eclipse.core.runtime.IProgressMonitor
 	 * @throws CoreException
 	 */
 	public void publishStart(IProgressMonitor monitor) throws CoreException {
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
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	public abstract void publishServer(IProgressMonitor monitor) throws CoreException;
 
 	/**
 	 * Publish an individual module to the server.
 	 * <p>
 	 * This method is called by the server core framework,
 	 * in response to a call to <code>IServer.publish()</code>.
 	 * Clients should never call this method.
 	 * </p>
 	 * 
 	 * @param parents
 	 * @param module
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	public abstract void publishModule(IModule[] parents, IModule module, IProgressMonitor monitor) throws CoreException;
 
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
 	 * @param monitor org.eclipse.core.runtime.IProgressMonitor
 	 * @throws CoreException
 	 */
 	public void publishFinish(IProgressMonitor monitor) throws CoreException {
 		// do nothing
 	}
 
 	/**
 	 * 
 	 * @see IServer#setLaunchDefaults(ILaunchConfigurationWorkingCopy)
 	 */
 	public abstract void setLaunchDefaults(ILaunchConfigurationWorkingCopy workingCopy);
 
 	/**
 	 * Restart this server. The server should use the server
 	 * listener to notify progress. It must use the same debug
 	 * flags as was originally passed into the start() method.
 	 * 
 	 * This method is used if there is a quick/better way to restart
 	 * the server. If it throws a CoreException, the normal stop/start
 	 * actions will be used.
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
 	public boolean canRestartModule(IModule module) {
 		return false;
 	}
 
 	/**
 	 * Check if the given module is in sync on the server. It should
 	 * return true if the module should be restarted (is out of
 	 * sync) or false if the module does not need to be restarted.
 	 *
 	 * @param module org.eclipse.wst.server.core.model.IModule
 	 * @return boolean
 	 */
 	/*public boolean isModuleRestartNeeded(IModule module) {
 		return false;
 	}*/
 
 	/**
 	 * Asynchronously restarts the given module on the server.
 	 * See the specification of 
 	 * {@link IServer#synchronousRestartModule(IModule, IProgressMonitor)}
 	 * for further details. 
 	 * <p>
 	 * The implementation should update the module sync state and fire
 	 * an event for the module.
 	 * </p>
 	 * <p>
 	 * [issue: It should probably be spec'd to throw an exception error if the
 	 * given module is not associated with the server.]
 	 * </p>
 	 * <p>
 	 * [issue: Since this method is ascynchronous, is there
 	 * any need for the progress monitor?]
 	 * </p>
 	 * <p>
 	 * [issue: Since this method is ascynchronous, how can
 	 * it return a meaningful IStatus? 
 	 * And IServer.synchronousModuleRestart throws CoreException
 	 * if anything goes wrong.]
 	 * </p>
 	 * <p>
 	 * [issue: If the module was just published to the server
 	 * and had never been started, would is be ok to "start"
 	 * the module using this method?]
 	 * </p>
 	 * 
 	 * @param module the module to be started
 	 * @param monitor a progress monitor, or <code>null</code> if progress
 	 *    reporting and cancellation are not desired
 	 * @return status object
 	 * @exception CoreException if an error occurs while trying to restart the module
 	 */
 	public void restartModule(IModule module, IProgressMonitor monitor) throws CoreException {
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
 }
