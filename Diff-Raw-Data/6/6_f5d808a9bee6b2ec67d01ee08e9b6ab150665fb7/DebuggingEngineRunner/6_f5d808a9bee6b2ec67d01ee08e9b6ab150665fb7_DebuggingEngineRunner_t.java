 package org.eclipse.dltk.launching;
 
 import java.io.File;
 import java.text.MessageFormat;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.dltk.dbgp.DbgpSessionIdGenerator;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.IDbgpService;
 import org.eclipse.dltk.debug.core.ScriptDebugManager;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTargetListener;
 import org.eclipse.dltk.internal.debug.core.model.ScriptDebugTarget;
 import org.eclipse.dltk.internal.launching.InterpreterMessages;
 import org.eclipse.dltk.launching.debug.DbgpInterpreterConfig;
 import org.eclipse.dltk.launching.debug.DebuggingEngineManager;
 import org.eclipse.dltk.launching.debug.IDebuggingEngine;
 
 public abstract class DebuggingEngineRunner extends AbstractInterpreterRunner {
 	public static class ScriptDebugTargetWaiter implements
 			IScriptDebugTargetListener {
 
 		private IScriptDebugTarget target;
 
 		public ScriptDebugTargetWaiter(IScriptDebugTarget target) {
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
 
 	// Launch attributes
 	public static final String LAUNCH_ATTR_DEBUGGING_ENGINE_ID = "debugging_engine_id";
 
 	private static final String LOCALHOST = "127.0.0.1";
 
 	protected String getSessionId(ILaunchConfiguration configuration)
 			throws CoreException {
 		return DbgpSessionIdGenerator.generate();
 	}
 
 	protected IScriptDebugTarget addDebugTarget(ILaunch launch,
 			ILaunchConfiguration configuration, IDbgpService dbgpService)
 			throws CoreException {
 
 		final IScriptDebugTarget target = new ScriptDebugTarget(
 				getDebugModelId(), dbgpService, getSessionId(configuration),
 				launch, null);
 		
 		IDebuggingEngine engine = getDebuggingEngine();
		if (engine != null) {
			target.setSupportsSuspendOnEntry(engine.supportsSuspendOnEntry());
			target.setSupportsSuspendOnExit(engine.supportsSuspendOnExit());
		}
 		
 		launch.addDebugTarget(target);
 		return target;
 	}
 
 	public DebuggingEngineRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected InterpreterConfig alterConfig(String exe, InterpreterConfig config)
 			throws CoreException {
 		return (InterpreterConfig) config.clone();
 	}
 
 	protected void initialize(InterpreterConfig config, ILaunch launch,
 			ILaunchConfiguration configuration) throws CoreException {
 		final IDbgpService service = DLTKDebugPlugin.getDefault()
 				.getDbgpService();
 
 		if (!service.available()) {
 			abort(InterpreterMessages.errDbgpServiceNotAvailable, null);
 		}
 
 		final IScriptDebugTarget target = addDebugTarget(launch, configuration,
 				service);
 
 		// Disable the output of the debugging engine process
 		launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, Boolean.FALSE
 				.toString());
 
 		// Debugging engine id
 		launch.setAttribute(LAUNCH_ATTR_DEBUGGING_ENGINE_ID,
 				getDebuggingEngineId());
 
 		// Configuration
 		final DbgpInterpreterConfig dbgpConfig = new DbgpInterpreterConfig(config);
 
 		dbgpConfig.setSessionId(target.getSessionId());
 		dbgpConfig.setPort(service.getPort());
 		dbgpConfig.setHost(LOCALHOST);
 	}
 
 	protected void checkConfig(InterpreterConfig config) throws CoreException {
 		File dir = new File(config.getWorkingDirectoryPath().toOSString());
 		if (!dir.exists()) {
 			abort(
 					MessageFormat
 							.format(
 									InterpreterMessages.errDebuggingEngineWorkingDirectoryDoesntExist,
 									new Object[] { dir.toString() }), null);
 		}
 
 		File script = new File(config.getScriptFilePath().toOSString());
 		if (!script.exists()) {
 			abort(
 					MessageFormat
 							.format(
 									InterpreterMessages.errDebuggingEngineScriptFileDoesntExist,
 									new Object[] { script.toString() }), null);
 		}
 	}
 
 	public void run(InterpreterConfig config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 
 		try {
 			// Configuration
 			final ILaunchConfiguration configuration = launch
 					.getLaunchConfiguration();
 
 			initialize(config, launch, configuration);
 
 			final InterpreterConfig newConfig = alterConfig(
 					constructProgramString(), config);
 
 			checkConfig(newConfig);
 
 			// Starting debugging engine
 			try {
 				String exe = (String) newConfig.getProperty("OVERRIDE_EXE");
 
 				if (exe != null) {
 					String[] cmdLine = newConfig.renderCommandLine(exe);
 					rawRun(launch, cmdLine, newConfig.getWorkingDirectoryPath()
 							.toFile(), newConfig.getEnvironmentAsStrings());
 				} else {
 					super.run(newConfig, launch, monitor);
 				}
 			} catch (CoreException e) {
 				abort(InterpreterMessages.errDebuggingEngineNotStarted, e);
 			}
 
 			// Waiting for debugging engine connect
 			startDebuggingEngine(launch, configuration);
 		} catch (CoreException e) {
 			launch.terminate();
 			throw e;
 		}
 
 		// Happy debugging :)
 	}
 
 	protected void startDebuggingEngine(ILaunch launch,
 			final ILaunchConfiguration configuration) throws CoreException {
 		int waitingTimeout = configuration
 				.getAttribute(
 						ScriptLaunchConfigurationConstants.ATTR_DLTK_DBGP_WAITING_TIMEOUT,
 						0);
 
 		ScriptDebugTargetWaiter waiter = new ScriptDebugTargetWaiter(
 				(IScriptDebugTarget) launch.getDebugTarget());
 
 		if (!waiter.waitThread(waitingTimeout)) {
 			abort(InterpreterMessages.errDebuggingEngineNotConnected, null);
 		}
 	}
 
 	public String getDebugModelId() {
 		return ScriptDebugManager.getInstance().getDebugModelByNature(
 				getInstall().getNatureId());
 	}
 
 	public IDebuggingEngine getDebuggingEngine() {
 		return DebuggingEngineManager.getInstance().getDebuggingEngine(
 				getDebuggingEngineId());
 	}
 	
 	protected abstract String getDebuggingEngineId();
 }
