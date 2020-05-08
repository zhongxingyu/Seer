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
 import org.eclipse.dltk.launching.DebuggingEngineRunner;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.debug.DbgpInterpreterConfig;
 import org.eclipse.dltk.ruby.fastdebugger.preferences.FastDebuggerPreferenceConstants;
import org.eclipse.dltk.ruby.internal.launching.RubyGenericInstallType;
 
 public class FastDebuggerRunner extends DebuggingEngineRunner {
 	public static final String ENGINE_ID = "org.eclipse.dltk.ruby.fastdebugger";
 
 	private static final String RUBY_HOST_VAR = "DBGP_RUBY_HOST";
 	private static final String RUBY_PORT_VAR = "DBGP_RUBY_PORT";
 	private static final String RUBY_KEY_VAR = "DBGP_RUBY_KEY";
 	private static final String RUBY_SCRIPT_VAR = "DBGP_RUBY_SCRIPT";
 	private static final String RUBY_LOG_VAR = "DBGP_RUBY_LOG";
 
 	private static final String DEBUGGER_SCRIPT = "FastRunner.rb";
 
 	protected IPath getLogFilename(PreferencesLookupDelegate delegate,
 			String sessionId) {
 		String pluginId = FastDebuggerPlugin.PLUGIN_ID;
 
 		String logFilePath = delegate.getString(pluginId,
 				FastDebuggerPreferenceConstants.LOG_FILE_PATH);
 		String logFileName = delegate.getString(pluginId,
 				FastDebuggerPreferenceConstants.LOG_FILE_NAME);
 
 		String fileName = MessageFormat.format(logFileName,
 				new Object[] { sessionId });
 
 		return Path.fromOSString(logFilePath).append(fileName);
 	}
 
 	protected boolean isLoggingEnabled(PreferencesLookupDelegate delegate) {
 		return delegate.getBoolean(FastDebuggerPlugin.PLUGIN_ID,
 				FastDebuggerPreferenceConstants.ENABLE_LOGGING);
 	}
 
 	protected IPath deploy() throws CoreException {
 		try {
 			return FastDebuggerPlugin.getDefault().deployDebuggerSource();
 		} catch (IOException e) {
 			// TODO: add code for handler
 			throw new CoreException(new Status(IStatus.ERROR,
 					FastDebuggerPlugin.PLUGIN_ID,
 					"Can't deploy debugger source", e));
 		}
 	}
 
 	public FastDebuggerRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected InterpreterConfig addEngineConfig(InterpreterConfig config,
 			PreferencesLookupDelegate delegate) throws CoreException {
		if (!(getInstall().getInterpreterInstallType() instanceof RubyGenericInstallType)) {
			throw new DebugException(
					new Status(IStatus.ERROR, FastDebuggerPlugin.PLUGIN_ID,
							"Fast debugger can be runned only with Generic Ruby interpreter"));
		}
 		// Get debugger source location
 		final IPath sourceLocation = deploy();
 
 		final IPath scriptFile = sourceLocation.append(DEBUGGER_SCRIPT);
 
 		// Creating new config
 		InterpreterConfig newConfig = (InterpreterConfig) config.clone();
 		newConfig.addInterpreterArg("-r" + scriptFile.toPortableString());
 		newConfig.addInterpreterArg("-I" + sourceLocation.toPortableString());
 
 		// Environment
 		final DbgpInterpreterConfig dbgpConfig = new DbgpInterpreterConfig(
 				config);
 
 		newConfig.addEnvVar(RUBY_HOST_VAR, dbgpConfig.getHost());
 		newConfig.addEnvVar(RUBY_PORT_VAR, Integer.toString(dbgpConfig
 				.getPort()));
 
 		String sessionId = dbgpConfig.getSessionId();
 		newConfig.addEnvVar(RUBY_KEY_VAR, sessionId);
 		newConfig.addEnvVar(RUBY_SCRIPT_VAR, config.getScriptFilePath()
 				.toPortableString());
 
 		if (isLoggingEnabled(delegate)) {
 			newConfig.addEnvVar(RUBY_LOG_VAR, getLogFilename(delegate,
 					sessionId).toPortableString());
 		}
 
 		return newConfig;
 	}
 
 	protected String getDebuggingEngineId() {
 		return ENGINE_ID;
 	}
 }
