 package org.eclipse.dltk.launching;
 
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.dltk.core.IScriptProject;
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
 import org.eclipse.dltk.core.environment.EnvironmentPathUtils;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.dbgp.DbgpSessionIdGenerator;
 import org.eclipse.dltk.debug.core.DLTKDebugPlugin;
 import org.eclipse.dltk.debug.core.DLTKDebugPreferenceConstants;
 import org.eclipse.dltk.debug.core.ExtendedDebugEventDetails;
 import org.eclipse.dltk.debug.core.IDbgpService;
 import org.eclipse.dltk.debug.core.ScriptDebugManager;
 import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
 import org.eclipse.dltk.internal.debug.core.model.DebugEventHelper;
 import org.eclipse.dltk.internal.debug.core.model.ScriptDebugTarget;
 import org.eclipse.dltk.internal.launching.InterpreterMessages;
 import org.eclipse.dltk.launching.debug.DbgpInterpreterConfig;
 import org.eclipse.dltk.launching.debug.DebuggingEngineManager;
 import org.eclipse.dltk.launching.debug.IDebuggingEngine;
 
 public abstract class DebuggingEngineRunner extends AbstractInterpreterRunner {
 	// Launch attributes
 	public static final String LAUNCH_ATTR_DEBUGGING_ENGINE_ID = "debugging_engine_id"; //$NON-NLS-1$
 
 	public static final String OVERRIDE_EXE = "OVERRIDE_EXE"; //$NON-NLS-1$
 
 	private static final String LOCALHOST = "127.0.0.1"; //$NON-NLS-1$
 
 	protected String getSessionId(ILaunchConfiguration configuration)
 			throws CoreException {
 		return DbgpSessionIdGenerator.generate();
 	}
 
 	protected IScriptDebugTarget addDebugTarget(ILaunch launch,
 			IDbgpService dbgpService) throws CoreException {
 
 		final IScriptDebugTarget target = new ScriptDebugTarget(
 				getDebugModelId(), dbgpService, getSessionId(launch
 						.getLaunchConfiguration()), launch, null);
 
 		launch.addDebugTarget(target);
 		return target;
 	}
 
 	public DebuggingEngineRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected void initializeLaunch(ILaunch launch, InterpreterConfig config,
 			PreferencesLookupDelegate delegate) throws CoreException {
 		final IDbgpService service = DLTKDebugPlugin.getDefault()
 				.getDbgpService();
 
 		if (!service.available()) {
 			abort(InterpreterMessages.errDbgpServiceNotAvailable, null);
 		}
 
 		final IScriptDebugTarget target = addDebugTarget(launch, service);
 
 		String qualifier = getDebugPreferenceQualifier();
 
 		target.toggleGlobalVariables(delegate.getBoolean(qualifier,
 				showGlobalVarsPreferenceKey()));
 		target.toggleClassVariables(delegate.getBoolean(qualifier,
 				showClassVarsPreferenceKey()));
 		target.toggleLocalVariables(delegate.getBoolean(qualifier,
 				showLocalVarsPreferenceKey()));
 
 		// Disable the output of the debugging engine process
 		launch.setAttribute(DebugPlugin.ATTR_CAPTURE_OUTPUT, Boolean.FALSE
 				.toString());
 
 		// Debugging engine id
 		launch.setAttribute(LAUNCH_ATTR_DEBUGGING_ENGINE_ID,
 				getDebuggingEngineId());
 
 		// Configuration
 		final DbgpInterpreterConfig dbgpConfig = new DbgpInterpreterConfig(
 				config);
 
 		dbgpConfig.setSessionId(target.getSessionId());
 		dbgpConfig.setPort(service.getPort());
 		dbgpConfig.setHost(getBindAddress());
 	}
 
 	private String getBindAddress() {
		String address = DLTKDebugPlugin.getDefault().getBindAddress();
 		if (DLTKDebugPreferenceConstants.DBGP_AUTODETECT_BIND_ADDRESS
 				.equals(address)) {
			String[] ipAddresses = DLTKDebugPlugin.getLocalAddresses();
 			if (ipAddresses.length > 0) {
 				address = ipAddresses[0];
 			} else {
 				address = LOCALHOST;
 			}
 		}
 		return address;
 	}
 
 	/**
 	 * Add the debugging engine configuration.
 	 */
 	protected abstract InterpreterConfig addEngineConfig(
 			InterpreterConfig config, PreferencesLookupDelegate delegate)
 			throws CoreException;
 
 	public void run(InterpreterConfig config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 
 		monitor.beginTask(InterpreterMessages.DebuggingEngineRunner_launching,
 				5);
 		if (monitor.isCanceled()) {
 			return;
 		}
 		try {
 			PreferencesLookupDelegate prefDelegate = createPreferencesLookupDelegate(launch);
 
 			initializeLaunch(launch, config, prefDelegate);
 			InterpreterConfig newConfig = addEngineConfig(config, prefDelegate);
 
 			// Starting debugging engine
 			IProcess process = null;
 			try {
 				DebugEventHelper.fireExtendedEvent(newConfig,
 						ExtendedDebugEventDetails.BEFORE_VM_STARTED);
 
 				// Running
 				monitor
 						.subTask(InterpreterMessages.DebuggingEngineRunner_running);
 				process = rawRun(launch, newConfig);
 			} catch (CoreException e) {
 				abort(InterpreterMessages.errDebuggingEngineNotStarted, e);
 			}
 			monitor.worked(4);
 
 			// Waiting for debugging engine connect
 			waitDebuggerConnected(process, launch, monitor);
 		} catch (CoreException e) {
 			launch.terminate();
 			throw e;
 		} finally {
 			monitor.done();
 		}
 		// Happy debugging :)
 	}
 
 	protected String[] renderCommandLine(InterpreterConfig config) {
 		String exe = (String) config.getProperty(OVERRIDE_EXE);
 		if (exe != null) {
 			return config.renderCommandLine(getInstall().getEnvironment(), exe);
 		}
 
 		return config.renderCommandLine(getInstall());
 	}
 
 	/**
 	 * Waiting debugging process to connect to current launch
 	 * 
 	 * @param debuggingProcess
 	 *            process that will connect to current launch or null if handle
 	 *            to process is not available (remote debugging)
 	 * @param launch
 	 *            launch to connect to
 	 * @param monitor
 	 *            progress monitor
 	 * @throws CoreException
 	 *             if debuggingProcess terminated, monitor is canceled or // *
 	 *             timeout
 	 */
 	protected void waitDebuggerConnected(IProcess debuggingProcess,
 			ILaunch launch, IProgressMonitor monitor) throws CoreException {
 		final int WAIT_CHUNK = 100;
 
 		ILaunchConfiguration configuration = launch.getLaunchConfiguration();
 		int timeout = configuration
 				.getAttribute(
 						ScriptLaunchConfigurationConstants.ATTR_DLTK_DBGP_WAITING_TIMEOUT,
 						0);
 
 		ScriptDebugTarget target = (ScriptDebugTarget) launch.getDebugTarget();
 		target.setProcess(debuggingProcess);
 
 		try {
 			int all = 0;
 			while (timeout == 0 || all < timeout) {
 				if (target.isInitialized()
 						|| target.isTerminated()
 						|| monitor.isCanceled()
 						|| (debuggingProcess != null && debuggingProcess
 								.isTerminated()))
 					break;
 
 				Thread.sleep(WAIT_CHUNK);
 				all += WAIT_CHUNK;
 			}
 		} catch (InterruptedException e) {
 			Thread.interrupted();
 		}
 
 		if (!target.isInitialized()) {
 			if (debuggingProcess != null && debuggingProcess.canTerminate()) {
 				debuggingProcess.terminate();
 			}
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
 
 	protected String showGlobalVarsPreferenceKey() {
 		return DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_GLOBAL;
 	}
 
 	protected String showClassVarsPreferenceKey() {
 		return DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_CLASS;
 	}
 
 	protected String showLocalVarsPreferenceKey() {
 		return DLTKDebugPreferenceConstants.PREF_DBGP_SHOW_SCOPE_LOCAL;
 	}
 
 	protected abstract String getDebuggingEngineId();
 
 	protected PreferencesLookupDelegate createPreferencesLookupDelegate(
 			ILaunch launch) throws CoreException {
 		IScriptProject sProject = ScriptRuntime.getScriptProject(launch
 				.getLaunchConfiguration());
 		return new PreferencesLookupDelegate(sProject.getProject());
 	}
 
 	/**
 	 * Returns the id of the plugin whose preference store contains general
 	 * debugging preference settings.
 	 */
 	protected abstract String getDebugPreferenceQualifier();
 
 	/**
 	 * Returns the id of the plugin whose preference store contains debugging
 	 * engine preferences.
 	 */
 	protected abstract String getDebuggingEnginePreferenceQualifier();
 
 	/**
 	 * Returns the preference key used to store the enable logging setting.
 	 * 
 	 * <p>
 	 * Note: this preference controls logging for the actual debugging engine,
 	 * and not the DBGP protocol output.
 	 * </p>
 	 */
 	// protected abstract String getLoggingEnabledPreferenceKey();
 	/**
 	 * Returns the preference key used to store the log file path
 	 */
 	// protected abstract String getLogFilePathPreferenceKey();
 	/**
 	 * Returns the preference key usd to store the log file name
 	 */
 	protected abstract String getLogFileNamePreferenceKey();
 
 	/**
 	 * Returns true if debugging engine logging is enabled.
 	 * 
 	 * <p>
 	 * Subclasses should use this method to determine of logging is enabled for
 	 * the given debugging engine.
 	 * </p>
 	 */
 	// protected boolean isLoggingEnabled(PreferencesLookupDelegate delegate) {
 	// String key = getLoggingEnabledPreferenceKey();
 	// String qualifier = getDebuggingEnginePreferenceQualifier();
 	//
 	// return delegate.getBoolean(qualifier, key);
 	// }
 	/**
 	 * Returns a fully qualifed path to a log file name.
 	 * 
 	 * <p>
 	 * If the user chose to use '{0}' in their file name, it will be replaced
 	 * with the debugging session id.
 	 * </p>
 	 */
 	protected String getLogFileName(PreferencesLookupDelegate delegate,
 			String sessionId) {
 		String qualifier = getDebuggingEnginePreferenceQualifier();
 		String keyValue = delegate.getString(qualifier,
 				getLogFileNamePreferenceKey());
 
 		Map logFileNames = EnvironmentPathUtils.decodePaths(keyValue);
 		IEnvironment env = getInstall().getEnvironment();
 		String pathString = (String) logFileNames.get(env);
 		if (pathString != null && pathString.length() > 0) {
 			return pathString;
 			// IPath path = new Path(pathString);
 			// return PlatformFileUtils.findAbsoluteOrEclipseRelativeFile(env,
 			// path).toString();
 		} else {
 			return null;
 		}
 	}
 }
