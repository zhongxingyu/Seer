 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *******************************************************************************/
 package org.eclipse.dltk.internal.debug.core.model;
 
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.WeakHashMap;
 
 import org.eclipse.core.resources.IMarkerDelta;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.IBreakpoint;
 import org.eclipse.debug.core.model.IDebugTarget;
 import org.eclipse.debug.core.model.IMemoryBlock;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.debug.core.model.ITerminate;
 import org.eclipse.debug.core.model.IThread;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.ExtendedDebugEventDetails;
 import org.eclipse.dltk.debug.core.IDbgpService;
 import org.eclipse.dltk.debug.core.IDebugOptions;
 import org.eclipse.dltk.debug.core.model.DefaultDebugOptions;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTargetListener;
 import org.eclipse.dltk.debug.core.model.IScriptDebugThreadConfigurator;
 import org.eclipse.dltk.debug.core.model.IScriptThread;
 import org.eclipse.dltk.debug.core.model.IScriptVariable;
 
 public class ScriptDebugTarget extends ScriptDebugElement implements
 		IScriptDebugTarget, IScriptThreadManagerListener {
 
 	private static final String LAUNCH_CONFIGURATION_ATTR_PROJECT = "project"; //$NON-NLS-1$
 	private static final String LAUNCH_CONFIGURATION_ATTR_BREAK_ON_FIRST_LINE = "enableBreakOnFirstLine"; //$NON-NLS-1$
 
 	private static final int THREAD_TERMINATION_TIMEOUT = 5000; // 5 seconds
 
 	private final ListenerList listeners;
 
 	private IScriptStreamProxy streamProxy;
 
 	private IProcess process;
 
 	private final ILaunch launch;
 
 	private String name;
 
 	private boolean disconnected;
 
 	private final IScriptThreadManager threadManager;
 
 	private final ScriptBreakpointManager breakpointManager;
 
 	private final IDbgpService dbgpService;
 	private final String sessionId;
 
	private final String modelId;
 
 	private static final WeakHashMap targets = new WeakHashMap();
 	private String[] stepFilters;
 
 	private boolean useStepFilters;
 
 	private final Object processLock = new Object();
 
 	private boolean initialized = false;
 	private boolean retrieveGlobalVariables;
 	private boolean retrieveClassVariables;
 	private boolean retrieveLocalVariables;
 
 	private final IDebugOptions options;
 
 	public static List getAllTargets() {
 		synchronized (targets) {
 			return new ArrayList(targets.keySet());
 		}
 	}
 
 	public ScriptDebugTarget(String modelId, IDbgpService dbgpService,
 			String sessionId, ILaunch launch, IProcess process) {
 		this(modelId, dbgpService, sessionId, launch, process,
 				DefaultDebugOptions.getDefaultInstance());
 	}
 
 	public ScriptDebugTarget(String modelId, IDbgpService dbgpService,
 			String sessionId, ILaunch launch, IProcess process,
 			IDebugOptions options) {
 		Assert.isNotNull(options);
 
		this.modelId = modelId;
 
 		this.listeners = new ListenerList();
 
 		this.process = process;
 		this.launch = launch;
 		this.options = options;
 
 		this.threadManager = new /* New */ScriptThreadManager(this);
 		this.sessionId = sessionId;
 		this.dbgpService = dbgpService;
 		this.dbgpService.registerAcceptor(this.sessionId, this.threadManager);
 
 		this.disconnected = false;
 
 		this.breakpointManager = new ScriptBreakpointManager(this,
 				createPathMapper());
 
 		this.threadManager.addListener(this);
 
 		DebugEventHelper.fireCreateEvent(this);
 		synchronized (targets) {
 			targets.put(this, ""); //$NON-NLS-1$
 		}
 	}
 
 	public void shutdown() {
 		try {
 			terminate(true);
 		} catch (DebugException e) {
 			DLTKDebugPlugin.log(e);
 		}
 	}
 
 	public String getSessionId() {
 		return sessionId;
 	}
 
 	public IDebugTarget getDebugTarget() {
 		return this;
 	}
 
 	public String getModelIdentifier() {
		return modelId;
 	}
 
 	public ILaunch getLaunch() {
 		return launch;
 	}
 
 	// IDebugTarget
 	public IProcess getProcess() {
 		synchronized (processLock) {
 			return process;
 		}
 	}
 
 	public void setProcess(IProcess process) {
 		synchronized (processLock) {
 			this.process = process;
 		}
 	}
 
 	public boolean hasThreads() {
 		return threadManager.hasThreads();
 	}
 
 	public IThread[] getThreads() {
 		return threadManager.getThreads();
 	}
 
 	public String getName() {
 		return name;
 	}
 
 	// ITerminate
 	public boolean canTerminate() {
 		synchronized (processLock) {
 			return threadManager.canTerminate() || process != null
 					&& process.canTerminate();
 		}
 	}
 
 	public boolean isTerminated() {
 		synchronized (processLock) {
 			return threadManager.isTerminated()
 					&& (process == null || process.isTerminated());
 		}
 	}
 
 	protected static boolean waitTermianted(ITerminate terminate, int chunk,
 			long timeout) {
 		final long start = System.currentTimeMillis();
 		while (!terminate.isTerminated()) {
 			if (System.currentTimeMillis() - start > timeout) {
 				return false;
 			}
 			try {
 				Thread.sleep(chunk);
 			} catch (InterruptedException e) {
 				// interrupted
 			}
 		}
 		return true;
 	}
 
 	public void terminate() throws DebugException {
 		terminate(true);
 	}
 
 	protected void terminate(boolean waitTermination) throws DebugException {
 		dbgpService.unregisterAcceptor(sessionId);
 
 		threadManager.sendTerminationRequest();
 		if (waitTermination) {
 			final IProcess p = getProcess();
 			final int CHUNK = 500;
 			if (!(waitTermianted(threadManager, CHUNK,
 					THREAD_TERMINATION_TIMEOUT) && (p == null || waitTermianted(
 					p, CHUNK, THREAD_TERMINATION_TIMEOUT)))) {
 				// Debugging process is not answering, so terminating it
 				if (p != null && p.canTerminate()) {
 					p.terminate();
 				}
 			}
 		}
 
 		threadManager.removeListener(this);
 		breakpointManager.threadTerminated();
 
 		DebugEventHelper.fireTerminateEvent(this);
 	}
 
 	// ISuspendResume
 	public boolean canSuspend() {
 		return threadManager.canSuspend();
 	}
 
 	public boolean isSuspended() {
 		return threadManager.isSuspended();
 	}
 
 	public void suspend() throws DebugException {
 		threadManager.suspend();
 	}
 
 	public boolean canResume() {
 		return threadManager.canResume();
 	}
 
 	public void resume() throws DebugException {
 		threadManager.resume();
 	}
 
 	// IDisconnect
 	public boolean canDisconnect() {
 		// Detach feature support!!!
 		return false;
 	}
 
 	public void disconnect() {
 		disconnected = true;
 	}
 
 	public boolean isDisconnected() {
 		return disconnected;
 	}
 
 	// IMemoryBlockRetrieval
 	public boolean supportsStorageRetrieval() {
 		return false;
 	}
 
 	public IMemoryBlock getMemoryBlock(long startAddress, long length) {
 		return null;
 	}
 
 	public IScriptVariable findVariable(String variableName) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	// Request timeout
 	public int getRequestTimeout() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	public void setRequestTimeout(int timeout) {
 		// TODO Auto-generated method stub
 
 	}
 
 	// IBreakpointListener
 	public void breakpointAdded(IBreakpoint breakpoint) {
 		breakpointManager.breakpointAdded(breakpoint);
 	}
 
 	public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
 		breakpointManager.breakpointChanged(breakpoint, delta);
 	}
 
 	public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
 		breakpointManager.breakpointRemoved(breakpoint, delta);
 	}
 
 	// Streams
 	public IScriptStreamProxy getStreamProxy() {
 		return streamProxy;
 	}
 
 	public void setStreamProxy(IScriptStreamProxy proxy) {
 		this.streamProxy = proxy;
 	}
 
 	// IDbgpThreadManagerListener
 	public void threadAccepted(IScriptThread thread, boolean first) {
 		if (first) {
 			DebugEventHelper.fireExtendedEvent(this,
 					ExtendedDebugEventDetails.BEFORE_CODE_LOADED);
 
 			breakpointManager.setupDeferredBreakpoints();
 
 			/*
 			 * tell the manager the thread was accepted after creating the path
 			 * mapper and setting the deferred breakpoints
 			 */
 			breakpointManager.threadAccepted();
 
 			// DebugEventHelper.fireCreateEvent(this);
 			initialized = true;
 			fireTargetInitialized();
 		}
 	}
 
 	protected IScriptBreakpointPathMapper createPathMapper() {
 		return new NopScriptbreakpointPathMapper();
 	}
 
 	public void allThreadsTerminated() {
 		try {
 			if (streamProxy != null) {
 				streamProxy.close();
 			}
 			terminate(false);
 		} catch (DebugException e) {
 			DLTKDebugPlugin.log(e);
 		}
 	}
 
 	public String toString() {
 		return "Debugging engine (id = " + this.sessionId + ")"; //$NON-NLS-1$ //$NON-NLS-2$
 	}
 
 	// IScriptDebugTarget
 	public void runToLine(URI uri, int lineNumber) throws DebugException {
 		breakpointManager.setBreakpointUntilFirstSuspend(uri, lineNumber);
 		resume();
 	}
 
 	public boolean isInitialized() {
 		return initialized;
 	}
 
 	protected void fireTargetInitialized() {
 		Object[] list = listeners.getListeners();
 		for (int i = 0; i < list.length; ++i) {
 			((IScriptDebugTargetListener) list[i]).targetInitialized();
 		}
 	}
 
 	public void addListener(IScriptDebugTargetListener listener) {
 		listeners.add(listener);
 	}
 
 	public void removeListener(IScriptDebugTargetListener listener) {
 		listeners.remove(listener);
 	}
 
 	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
 		return breakpointManager.supportsBreakpoint(breakpoint);
 	}
 
 	public void setFilters(String[] activeFilters) {
 		this.stepFilters = activeFilters;
 	}
 
 	public String[] getFilters() {
 		if (this.stepFilters != null) {
 			return this.stepFilters;
 		}
 		return new String[0];
 	}
 
 	public boolean isUseStepFilters() {
 		return useStepFilters;
 	}
 
 	public void setUseStepFilters(boolean useStepFilters) {
 		this.useStepFilters = useStepFilters;
 	}
 
 	public IDLTKLanguageToolkit getLanguageToolkit() {
 		IScriptProject scriptProject = getScriptProject();
 		if (scriptProject != null) {
 			IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 					.getLanguageToolkit(scriptProject);
 			return toolkit;
 		}
 
 		return null;
 	}
 
 	protected IScriptProject getScriptProject() {
 		final ILaunchConfiguration configuration = launch
 				.getLaunchConfiguration();
 		if (configuration != null) {
 			try {
 				String projectName = configuration.getAttribute(
 						LAUNCH_CONFIGURATION_ATTR_PROJECT, (String) null);
 				if (projectName != null) {
 					projectName = projectName.trim();
 					if (projectName.length() > 0) {
 						IProject project = ResourcesPlugin.getWorkspace()
 								.getRoot().getProject(projectName);
 						return DLTKCore.create(project);
 					}
 				}
 			} catch (CoreException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 
 	public boolean breakOnFirstLineEnabled() {
 		try {
 			return launch.getLaunchConfiguration().getAttribute(
 					LAUNCH_CONFIGURATION_ATTR_BREAK_ON_FIRST_LINE, false);
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 			return false;
 		}
 	}
 
 	public void toggleGlobalVariables(boolean enabled) {
 		retrieveGlobalVariables = enabled;
 		threadManager.refreshThreads();
 	}
 
 	public void toggleClassVariables(boolean enabled) {
 		retrieveClassVariables = enabled;
 		threadManager.refreshThreads();
 	}
 
 	public void toggleLocalVariables(boolean enabled) {
 		retrieveLocalVariables = enabled;
 		threadManager.refreshThreads();
 	}
 
 	public boolean retrieveClassVariables() {
 		return retrieveClassVariables;
 	}
 
 	public boolean retrieveGlobalVariables() {
 		return retrieveGlobalVariables;
 	}
 
 	public boolean retrieveLocalVariables() {
 		return retrieveLocalVariables;
 	}
 
 	public String getConsoleEncoding() {
 		String encoding = "UTF-8"; //$NON-NLS-1$
 		try {
 			encoding = getLaunch().getLaunchConfiguration().getAttribute(
 					DebugPlugin.ATTR_CONSOLE_ENCODING, encoding);
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		return encoding;
 	}
 
 	public void setScriptDebugThreadConfigurator(
 			IScriptDebugThreadConfigurator configurator) {
 		this.threadManager.setScriptThreadConfigurator(configurator);
 	}
 
 	public IDebugOptions getOptions() {
 		return options;
 	}
 }
