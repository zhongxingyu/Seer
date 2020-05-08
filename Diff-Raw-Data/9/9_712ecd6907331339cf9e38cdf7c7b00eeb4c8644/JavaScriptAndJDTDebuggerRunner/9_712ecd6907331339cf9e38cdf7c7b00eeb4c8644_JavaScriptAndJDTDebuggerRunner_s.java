 package org.eclipse.dltk.javascript.jsjdtdebugger;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
 import org.eclipse.dltk.javascript.internal.debug.JavaScriptDebugConstants;
 import org.eclipse.dltk.javascript.internal.debug.JavaScriptDebugPlugin;
 import org.eclipse.dltk.javascript.internal.launching.JavaScriptInterpreterRunner;
 import org.eclipse.dltk.javascript.launching.IConfigurableRunner;
 import org.eclipse.dltk.javascript.launching.IJavaScriptInterpreterRunnerConfig;
 import org.eclipse.dltk.launching.DebuggingEngineRunner;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterConfig;
 
 public class JavaScriptAndJDTDebuggerRunner extends DebuggingEngineRunner
 		implements IConfigurableRunner {
 
 	public static String ENGINE_ID = "org.eclipse.dltk.javascript.jsjdtdebugger";
 
 	IJavaScriptInterpreterRunnerConfig runnerconfig = JavaScriptInterpreterRunner.DEFAULT_CONFIG;
 
 	public JavaScriptAndJDTDebuggerRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	public String getDebugModelId() {
 		return JavaScriptDebugConstants.DEBUG_MODEL_ID;
 	}
 
 	public void run(InterpreterConfig config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 		initializeLaunch(launch, config,
 				createPreferencesLookupDelegate(launch));
 		JavaScriptInterpreterRunner
 				.doRunImpl(config, launch, this.runnerconfig);
 	}
 
 	public void setRunnerConfig(IJavaScriptInterpreterRunnerConfig config) {
 		this.runnerconfig = config;
 	}
 
 	protected String getDebuggingEngineId() {
 		return ENGINE_ID;
 	}
 
 	protected InterpreterConfig addEngineConfig(InterpreterConfig config,
 			PreferencesLookupDelegate delegate) throws CoreException {
 		return config;
 	}
 
 	protected String getDebugPreferenceQualifier() {
 		return JavaScriptDebugPlugin.PLUGIN_ID;
 	}
 
 	protected String getDebuggingEnginePreferenceQualifier() {
 		return JavaScriptAndJdtDebuggerPlugin.PLUGIN_ID;
 	}
 
 	protected String getLoggingEnabledPreferenceKey() {
 		// not yet supported...
 		return null;
 	}
 
 	protected String getLogFileNamePreferenceKey() {
 		// not yet supported...
 		return null;
 	}
 
 	protected String getLogFilePathPreferenceKey() {
 		// not yet supported...
 		return null;
 	}
 }
