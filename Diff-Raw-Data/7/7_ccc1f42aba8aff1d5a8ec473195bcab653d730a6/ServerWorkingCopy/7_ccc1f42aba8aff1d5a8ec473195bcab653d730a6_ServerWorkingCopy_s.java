 /**********************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
 *
  * Contributors:
  *     IBM Corporation - Initial API and implementation
  **********************************************************************/
 package org.eclipse.wst.server.core.internal;
 
 import java.beans.PropertyChangeListener;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.*;
 import org.eclipse.wst.server.core.*;
 import org.eclipse.wst.server.core.model.ServerDelegate;
 /**
  * 
  */
 public class ServerWorkingCopy extends Server implements IServerWorkingCopy {
 	protected Server server;
 	protected WorkingCopyHelper wch;
 	
 	protected ServerDelegate workingCopyDelegate;
 	
 	// working copy
 	public ServerWorkingCopy(Server server) {
 		super(server.getFile());
 		this.server = server;
 		
 		map = new HashMap(server.map);
 		wch = new WorkingCopyHelper(this);
 		resolve();
 	}
 	
 	// creation
 	public ServerWorkingCopy(String id, IFile file, IRuntime runtime, IServerType serverType) {
 		super(id, file, runtime, serverType);
 		//server = this;
 		wch = new WorkingCopyHelper(this);
 		wch.setDirty(true);
 		serverState = ((ServerType)serverType).getInitialState();
 	}
 
 	public boolean isWorkingCopy() {
 		return true;
 	}
 	
 	public IServer getOriginal() {
 		return server;
 	}
 	
 	public IServerWorkingCopy createWorkingCopy() {
 		return this;
 	}
 
 	public int getServerState() {
 		if (server != null)
 			return server.getServerState();
 		return serverState;
 	}
 
 	public void setServerState(byte state) {
 		if (server != null)
 			server.setServerState(state);
 		else
 			super.setServerState(state);
 	}
 
 	public void setAttribute(String attributeName, int value) {
 		wch.setAttribute(attributeName, value);
 	}
 
 	public void setAttribute(String attributeName, boolean value) {
 		wch.setAttribute(attributeName, value);
 	}
 
 	public void setAttribute(String attributeName, String value) {
 		wch.setAttribute(attributeName, value);
 	}
 
 	public void setAttribute(String attributeName, List value) {
 		wch.setAttribute(attributeName, value);
 	}
 
 	public void setAttribute(String attributeName, Map value) {
 		wch.setAttribute(attributeName, value);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.IServerWorkingCopy#setName(java.lang.String)
 	 */
 	public void setName(String name) {
 		setAttribute(PROP_NAME, name);
 	}
 	
 	public void setLocked(boolean b) {
 		setAttribute(PROP_LOCKED, b);
 	}
 
 	public void setPrivate(boolean b) {
 		setAttribute(PROP_PRIVATE, b);
 	}
 
 	public void setHost(String host) {
 		setAttribute(PROP_HOSTNAME, host);
 	}
 	
 	public void setServerConfiguration(IFolder config) {
 		this.configuration = config;
 		if (configuration == null)
 			setAttribute(CONFIGURATION_ID, (String)null);
 		else
 			setAttribute(CONFIGURATION_ID, configuration.getFullPath().toString());
 	}
 
 	public void setFile(IFile file) {
 		this.file = file;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.IServerWorkingCopy#isDirty()
 	 */
 	public boolean isDirty() {
 		return wch.isDirty();
 	}
 	
 	public ServerDelegate getWorkingCopyDelegate(IProgressMonitor monitor) {
 		// make sure that the regular delegate is loaded 
 		//getDelegate();
 		
 		if (workingCopyDelegate != null)
 			return workingCopyDelegate;
 		
 		if (serverType != null) {
 			synchronized (this) {
 				if (workingCopyDelegate == null) {
 					try {
 						long time = System.currentTimeMillis();
 						IConfigurationElement element = ((ServerType) serverType).getElement();
 						workingCopyDelegate = (ServerDelegate) element.createExecutableExtension("class");
 						workingCopyDelegate.initialize(this);
 						Trace.trace(Trace.PERFORMANCE, "ServerWorkingCopy.getWorkingCopyDelegate(): <" + (System.currentTimeMillis() - time) + "> " + getServerType().getId());
 					} catch (Exception e) {
 						Trace.trace(Trace.SEVERE, "Could not create delegate " + toString(), e);
 					}
 				}
 			}
 		}
 		return workingCopyDelegate;
 	}
 	
 	public void dispose() {
 		super.dispose();
 		if (workingCopyDelegate != null)
 			workingCopyDelegate.dispose();
 	}
 	
 	public IServer save(boolean force, IProgressMonitor monitor) throws CoreException {
 		monitor = ProgressUtil.getMonitorFor(monitor);
 		monitor.subTask(ServerPlugin.getResource("%savingTask", getName()));
 
 		if (!force)
 			wch.validateTimestamp(getOriginal());
 
 		if (server == null) {
 			server = new Server(file);
 			server.setServerState(serverState);
 			server.publishListeners = publishListeners;
 			server.serverListeners = serverListeners;
 		}
 		
 		server.setInternal(this);
 		server.doSave(monitor);
 		IFolder folder = getServerConfiguration();
 		if (folder != null && !folder.exists()) {
 			folder.create(IResource.FORCE, true, null);
 		}
 		//ResourcesPlugin.getWorkspace().getRoot().g
 		getDelegate().saveConfiguration(monitor);
 		wch.setDirty(false);
 		
 		return server;
 	}
 
 	public IServer saveAll(boolean force, IProgressMonitor monitor) throws CoreException {
 		if (runtime != null && runtime.isWorkingCopy()) {
 			IRuntimeWorkingCopy wc = (IRuntimeWorkingCopy) runtime;
 			wc.save(force, monitor);
 		}
 		
 		return save(force, monitor);
 	}
 
 	/**
 	 * Add a property change listener to this server.
 	 *
 	 * @param listener java.beans.PropertyChangeListener
 	 */
 	public void addPropertyChangeListener(PropertyChangeListener listener) {
 		wch.addPropertyChangeListener(listener);
 	}
 	
 	/**
 	 * Remove a property change listener from this server.
 	 *
 	 * @param listener java.beans.PropertyChangeListener
 	 */
 	public void removePropertyChangeListener(PropertyChangeListener listener) {
 		wch.removePropertyChangeListener(listener);
 	}
 	
 	/**
 	 * Fire a property change event.
 	 */
 	public void firePropertyChangeEvent(String propertyName, Object oldValue, Object newValue) {
 		wch.firePropertyChangeEvent(propertyName, oldValue, newValue);
 	}
 	
 	public void addServerListener(IServerListener listener) {
 		if (server != null)
 			server.addServerListener(listener);
 		else
 			super.addServerListener(listener);
 	}
 	
 	public void removeServerListener(IServerListener listener) {
 		if (server != null)
 			server.removeServerListener(listener);
 		else
 			super.removeServerListener(listener);
 	}
 	
 	public void addPublishListener(IPublishListener listener) {
 		if (server != null)
 			server.addPublishListener(listener);
 		else
 			super.addPublishListener(listener);
 	}
 	
 	public void removePublishListener(IPublishListener listener) {
 		if (server != null)
 			server.removePublishListener(listener);
 		else
 			super.removePublishListener(listener);
 	}
 
 	public void setRuntime(IRuntime runtime) {
 		this.runtime = runtime;
 		if (runtime != null)
 			setAttribute(RUNTIME_ID, runtime.getId());
 		else
 			setAttribute(RUNTIME_ID, (String)null);
 	}
 	
 	public void setRuntimeId(String runtimeId) {
 		setAttribute(RUNTIME_ID, runtimeId);
 		resolve();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.wst.server.core.IServer#modifyModule(org.eclipse.wst.server.core.model.IModule)
 	 */
 	public void modifyModules(IModule[] add, IModule[] remove, IProgressMonitor monitor) throws CoreException {
 		int i = 0;
 		while (getServerState() == IServer.STATE_UNKNOWN && i < 10) {
 			try {
 				Thread.sleep(1000);
 			} catch (Exception e) {
 				// ignore
 			}
 			i++;
 		}
 		
 		try {
 			monitor = ProgressUtil.getMonitorFor(monitor);
 			monitor.subTask(ServerPlugin.getResource("%taskModifyModules"));
 			getWorkingCopyDelegate(monitor).modifyModules(add, remove, monitor);
 			wch.setDirty(true);
 		} catch (CoreException ce) {
 			throw ce;
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate modifyModule() " + toString(), e);
 			throw new CoreException(new Status(IStatus.ERROR, ServerPlugin.PLUGIN_ID, 0, e.getLocalizedMessage(), e));
 		}
 	}
 
 	public void setDefaults(IProgressMonitor monitor) {
 		try {
 			getWorkingCopyDelegate(monitor).setDefaults();
 		} catch (Exception e) {
 			Trace.trace(Trace.SEVERE, "Error calling delegate setDefaults() " + toString(), e);
 		}
 	}
 	
 	public String toString() {
 		return "ServerWorkingCopy " + getId();
 	}
 }
