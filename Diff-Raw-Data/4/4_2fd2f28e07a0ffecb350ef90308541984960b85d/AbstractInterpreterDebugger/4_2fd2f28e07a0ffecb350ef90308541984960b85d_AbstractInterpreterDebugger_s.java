 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.launching;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.IDbgpService;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTargetListener;
 import org.eclipse.dltk.debug.internal.core.model.ScriptDebugTarget;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 
 public abstract class AbstractInterpreterDebugger extends AbstractInterpreterRunner {
	private static final boolean DEBUG = DLTKCore.DEBUG;
 
 	protected static final int DEFAULT_WAITING_TIMEOUT = 1000 * 1000;
 
 	protected static final int DEFAULT_PAUSE = 500;
 
 	public static class ScriptDebugTargetWaiter implements IScriptDebugTargetListener {
 		private IScriptDebugTarget target;
 
 		public ScriptDebugTargetWaiter(IScriptDebugTarget target) {
 			if (target == null) {
 				throw new IllegalArgumentException();
 			}
 
 			this.target = target;
 		}
 
 		public synchronized void targetInitialized() {
 			notifyAll();
 		}
 
 		public synchronized boolean waitThread(int timeout) {
 			target.addListener(this);
 
 			try {
 				wait(timeout);
 			} catch (InterruptedException e) {
 				Thread.interrupted();
 			}
 
 			target.removeListener(this);
 			return target.isInitialized();
 		}
 	}
 
 	private String generateSessionId() {
 		return "dbgp" + System.currentTimeMillis();
 	}
 
 	protected static void sleep(long millis) {
 		try {
 			Thread.sleep(millis);
 		} catch (InterruptedException e) {
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	public AbstractInterpreterDebugger(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected String addDebugTarget(ILaunch launch, ILaunchConfiguration configuration, IDbgpService dbgpService) throws CoreException {
 
 		// Session id
 		String sessionId = configuration.getAttribute(IDLTKLaunchConfigurationConstants.ATTR_DLTK_DBGP_SESSION_ID, (String) null);
 
 		if (sessionId == null) {
 			sessionId = generateSessionId();
 		}
 
 		// RubyDebugTarget target = new RubyDebugTarget(launch, null, sessionId,
 		// dbgpService);
 		// launch.addDebugTarget(target);
 
 		IScriptDebugTarget target = new ScriptDebugTarget(getDebugModelIdentidier(), dbgpService, sessionId, launch, null);
 		launch.addDebugTarget(target);
 
 		return sessionId;
 	}
 
 	public void run(InterpreterRunnerConfiguration configuration, ILaunch launch, IProgressMonitor monitor) throws CoreException {
 
 		final ILaunchConfiguration config = launch.getLaunchConfiguration();
 
 		IDbgpService dbgpService = null;
 
 		try {
 			int port = config.getAttribute(IDLTKLaunchConfigurationConstants.ATTR_DLTK_DBGP_PORT, -1);
 
 			if (port == -1) {
 				dbgpService = DLTKDebugPlugin.getDefault().createDbgpService();
 			} else {
 				dbgpService = DLTKDebugPlugin.getDefault().creaeDbgpService(port);
 			}
 		} catch (Exception e) {
 			abort(DLTKLaunchingPlugin.ID_PLUGIN, "Dbgp service not available", null, DLTKLaunchingPlugin.DBGP_SERVICE_NOT_AVAILABLE);
 		}
 
 		final String sessionId = addDebugTarget(launch, config, dbgpService);
 		final int port = dbgpService.getPort();
 
 		//System.out.println("Session id: " + sessionId);
 		//System.out.println("Port: " + port);
 
 		try {
 			boolean remoteDebugging = config.getAttribute(IDLTKLaunchConfigurationConstants.ATTR_DLTK_DBGP_REMOTE, false);
 
 			// Starting debugging
 			final String host = "127.0.0.1";
 
 			final String[] commandLine = getCommandLine(sessionId, host, port, configuration);
 
 			// if (DEBUG) {
 			System.out.println(renderCommandLine(commandLine));
 			// }
 
 			if (!remoteDebugging) {
 				// Start local debugging engine
 				sleep(DEFAULT_PAUSE);
 
 				try {
 					exec(commandLine, getWorkingDir(configuration), configuration.getEnvironment());
 				} catch (CoreException e) {
 					abort(DLTKLaunchingPlugin.ID_PLUGIN, "Debugging engine not started", null, DLTKLaunchingPlugin.DEBUGGING_ENGINE_NOT_STARTED);
 				}
 			}
 
 			int waitingTimeout = config.getAttribute(IDLTKLaunchConfigurationConstants.ATTR_DLTK_DBGP_WAITING_TIMEOUT, DEFAULT_WAITING_TIMEOUT);
 
 			ScriptDebugTargetWaiter waiter = new ScriptDebugTargetWaiter((IScriptDebugTarget) launch.getDebugTarget());
 
 			if (!waiter.waitThread(waitingTimeout)) {
 				abort(DLTKLaunchingPlugin.ID_PLUGIN, "Debugging engine not connected", null, DLTKLaunchingPlugin.DEBUGGING_ENGINE_NOT_CONNECTED);
 			}
 
 			// try {
 			// Thread.sleep(2000);
 			// } catch (InterruptedException e) {
 			// // TODO Auto-generated catch block
 			// e.printStackTrace();
 			// }
 		} catch (CoreException e) {
 			launch.terminate();
 			throw e;
 		}
 
 		// Happy debugging :)
 	}
 
 	protected abstract String[] getCommandLine(String sessionId, String host, int port, InterpreterRunnerConfiguration configuration) throws CoreException;
 
 	protected abstract String getDebugModelIdentidier();			
 }
