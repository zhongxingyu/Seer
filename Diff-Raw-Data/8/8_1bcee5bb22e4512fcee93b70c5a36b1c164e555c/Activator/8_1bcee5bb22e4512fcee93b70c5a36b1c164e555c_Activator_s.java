 /*******************************************************************************
  * Copyright (c) 2012 Sebastian Schmidt and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Sebastian Schmidt - initial API and implementation
  *******************************************************************************/
 package org.eclipse.mylyn.internal.context.breakpoints;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.List;
 
 import org.eclipse.core.resources.ISavedState;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.mylyn.context.core.ContextCore;
 import org.eclipse.ui.plugin.AbstractUIPlugin;
 import org.osgi.framework.BundleContext;
 
 /**
  * The activator class controls the plug-in life cycle
  */
 public class Activator extends AbstractUIPlugin {
 
 	public static final String WORKSPACE_STATE_FILE = "activeContextBreakpoints.xml"; //$NON-NLS-1$
 
 	// The plug-in ID
 	public static final String PLUGIN_ID = "org.eclipse.mylyn.context.breakpoints"; //$NON-NLS-1$
 
 	// The shared instance
 	private static Activator plugin;
 
 	private static BreakpointsContextManager breakpointContextManager = null;
 
 	private WorkspaceSaveParticipant workspaceSaveParticipant;
 
 	/**
 	 * The constructor
 	 */
 	public Activator() {
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	@Override
 	public void start(BundleContext context) throws Exception {
 		super.start(context);
 		plugin = this;
 
 		breakpointContextManager = new BreakpointsContextManager();
 		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(breakpointContextManager);
 		ContextCore.getContextManager().addListener(breakpointContextManager);
 		workspaceSaveParticipant = new WorkspaceSaveParticipant(breakpointContextManager);
 
 		ISavedState previousPluginState = ResourcesPlugin.getWorkspace().addSaveParticipant(PLUGIN_ID,
 				workspaceSaveParticipant);
 		if (previousPluginState != null) {
			restorePreviousPluginState(previousPluginState);
 		}
 	}
 
 	private void restorePreviousPluginState(ISavedState previousPluginState) throws IOException {
 		IPath path = previousPluginState.lookup(new Path(WORKSPACE_STATE_FILE));
 		File file = path.append(WORKSPACE_STATE_FILE).toFile();
 		FileInputStream inputStream = new FileInputStream(file);
 		List<IBreakpoint> importBreakpoints = BreakpointsContextUtil.importBreakpoints(inputStream);
 		breakpointContextManager.setContextBreakpoints(importBreakpoints);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
 	 * )
 	 */
 	@Override
 	public void stop(BundleContext context) throws Exception {
 		plugin = null;
 		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(breakpointContextManager);
 		ContextCore.getContextManager().removeListener(breakpointContextManager);
 		breakpointContextManager = null;
 		ResourcesPlugin.getWorkspace().removeSaveParticipant(PLUGIN_ID);
 		workspaceSaveParticipant = null;
 		super.stop(context);
 	}
 
 	/**
 	 * Returns the shared instance
 	 * 
 	 * @return the shared instance
 	 */
 	public static Activator getDefault() {
 		return plugin;
 	}
 
 	public static BreakpointsContextManager getBreakpointContextManager() {
 		return breakpointContextManager;
 	}
 
 }
