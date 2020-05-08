 package org.eclipse.dltk.ruby.fastdebugger;
 
 import java.io.IOException;
 import java.text.MessageFormat;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
 import org.eclipse.dltk.core.environment.IDeployment;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IExecutionEnvironment;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.launching.DebuggingEngineRunner;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.debug.DbgpInterpreterConfig;
 import org.eclipse.dltk.ruby.debug.RubyDebugPlugin;
 import org.eclipse.dltk.ruby.internal.launching.RubyGenericInstallType;
 
 public class FastDebuggerRunner extends DebuggingEngineRunner {
 	public static final String ENGINE_ID = "org.eclipse.dltk.ruby.fastdebugger"; //$NON-NLS-1$
 
 	private static final String RUBY_HOST_VAR = "DBGP_RUBY_HOST"; //$NON-NLS-1$
 	private static final String RUBY_PORT_VAR = "DBGP_RUBY_PORT"; //$NON-NLS-1$
 	private static final String RUBY_KEY_VAR = "DBGP_RUBY_KEY"; //$NON-NLS-1$
 	private static final String RUBY_LOG_VAR = "DBGP_RUBY_LOG"; //$NON-NLS-1$
 
 	private static final String DEBUGGER_SCRIPT = "FastRunner.rb"; //$NON-NLS-1$
 
 	protected IPath deploy(IDeployment deployment) throws CoreException {
 		try {
 			IPath deploymentPath = FastDebuggerPlugin.getDefault()
 					.deployDebuggerSource(deployment);
 			return deployment.getFile(deploymentPath).getPath();
 		} catch (IOException e) {
 			// TODO: add code for handler
 			throw new CoreException(
 					new Status(
 							IStatus.ERROR,
 							FastDebuggerPlugin.PLUGIN_ID,
 							Messages.FastDebuggerRunner_unableToDeployDebuggerSource,
 							e));
 		}
 	}
 
 	public FastDebuggerRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected InterpreterConfig addEngineConfig(InterpreterConfig config,
 			PreferencesLookupDelegate delegate) throws CoreException {
 		if (!(getInstall().getInterpreterInstallType() instanceof RubyGenericInstallType)) {
 			throw new DebugException(
 					new Status(
 							IStatus.ERROR,
 							FastDebuggerPlugin.PLUGIN_ID,
 							Messages.FastDebuggerRunner_fastDebuggerCanOnlyBeRunWithGenericRubyInterpreter));
 		}
 
 		IEnvironment env = getInstall().getEnvironment();
 		IExecutionEnvironment exeEnv = (IExecutionEnvironment) env
 				.getAdapter(IExecutionEnvironment.class);
 		IDeployment deployment = exeEnv.createDeployment();
 
 		// Get debugger source location
 		final IPath sourceLocation = deploy(deployment);
 
 		final IPath scriptFile = sourceLocation.append(DEBUGGER_SCRIPT);
 
 		// Creating new config
 		InterpreterConfig newConfig = (InterpreterConfig) config.clone();
 		newConfig.addInterpreterArg("-r"); //$NON-NLS-1$
 		newConfig.addInterpreterArg(env.convertPathToString(scriptFile)); //$NON-NLS-1$
 		newConfig.addInterpreterArg("-I"); //$NON-NLS-1$
 		newConfig.addInterpreterArg(env.convertPathToString(sourceLocation)); //$NON-NLS-1$
 
 		// Environment
 		final DbgpInterpreterConfig dbgpConfig = new DbgpInterpreterConfig(
 				config);
 
 		newConfig.addEnvVar(RUBY_HOST_VAR, dbgpConfig.getHost());
 		newConfig.addEnvVar(RUBY_PORT_VAR, Integer.toString(dbgpConfig
 				.getPort()));
 
 		String sessionId = dbgpConfig.getSessionId();
 		newConfig.addEnvVar(RUBY_KEY_VAR, sessionId);
 
 		String logFileName = getLogFileName(delegate, sessionId);
 		if (logFileName != null) {
 			newConfig.addEnvVar(RUBY_LOG_VAR, logFileName);
 		}
 
 		return newConfig;
 	}
 
 	protected String getDebuggingEngineId() {
 		return ENGINE_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getDebugPreferenceQualifier()
 	 */
 	protected String getDebugPreferenceQualifier() {
 		return RubyDebugPlugin.PLUGIN_ID;
 	}
 
 	public IPath resolveGemsPath(boolean user) {
 		IEnvironment env = getInstall().getEnvironment();
 		IPath gemsPath = new Path(getInstall().getInstallLocation()
				.toOSString());
 		if (gemsPath.segmentCount() < 2)
 			return null;
 
 		gemsPath = gemsPath.removeLastSegments(2);
 
 		if (user == true) {
 			gemsPath = gemsPath.append("lib/ruby/user-gems/1.8/gems"); //$NON-NLS-1$
 
 			IPath userGemsPathUbuntu = new Path("/var/lib/user-gems/1.8/gems"); //$NON-NLS-1$
 			if ((env.getFile(gemsPath).exists() != true)
 					&& (env.getFile(userGemsPathUbuntu).exists() == true)) {
 				gemsPath = userGemsPathUbuntu;
 			}
 		} else {
 			gemsPath = gemsPath.append("lib/ruby/gems/1.8/gems"); //$NON-NLS-1$
 
 			IPath gemsPathUbuntu = new Path("/var/lib/gems/1.8/gems"); //$NON-NLS-1$
 			if ((env.getFile(gemsPath).exists() != true)
 					&& (env.getFile(gemsPathUbuntu).exists() == true)) {
 				gemsPath = gemsPathUbuntu;
 			}
 		}
 
 		return gemsPath;
 	}
 
 	private boolean resolveRubyDebugGemExists(boolean userGems) {
 		IEnvironment env = getInstall().getEnvironment();
 		IPath gemsPath = resolveGemsPath(userGems);
 
 		IFileHandle gemDir = env.getFile(gemsPath);
 		if ((gemsPath != null) && (gemDir.exists() == true)) {
 			IFileHandle[] children = gemDir.getChildren();
 			for (int i = 0; i < children.length; i++) {
 				String name = children[i].getName();
 				if (name.indexOf('-') != -1
 						&& "ruby-debug".equals(name.substring(0, //$NON-NLS-1$
 								name.lastIndexOf('-')))) {
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public boolean resolveRubyDebugGemExists() {
 		return (resolveRubyDebugGemExists(true) || resolveRubyDebugGemExists(false));
 	}
 
 	protected void checkConfig(InterpreterConfig config,
 			IEnvironment environment) throws CoreException {
 		super.checkConfig(config, environment);
 
 		if (resolveRubyDebugGemExists() != true) {
 			abort(
 					MessageFormat
 							.format(
 									Messages.FastDebuggerRunner_rubyDebugGemDoesntSeemToBeInstalled,
 									new Object[] {
 											getDebuggingEngine().getName(),
 											getInstall().getInstallLocation()
													.toOSString() }), null);
 		}
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getDebuggingEnginePreferenceQualifier()
 	 */
 	protected String getDebuggingEnginePreferenceQualifier() {
 		return FastDebuggerPlugin.PLUGIN_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLoggingEnabledPreferenceKey()
 	 */
 	protected String getLoggingEnabledPreferenceKey() {
 		return FastDebuggerConstants.ENABLE_LOGGING;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLogFileNamePreferenceKey()
 	 */
 	protected String getLogFileNamePreferenceKey() {
 		return FastDebuggerConstants.LOG_FILE_NAME;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLogFilePathPreferenceKey()
 	 */
 	protected String getLogFilePathPreferenceKey() {
 		return FastDebuggerConstants.LOG_FILE_PATH;
 	}
 }
