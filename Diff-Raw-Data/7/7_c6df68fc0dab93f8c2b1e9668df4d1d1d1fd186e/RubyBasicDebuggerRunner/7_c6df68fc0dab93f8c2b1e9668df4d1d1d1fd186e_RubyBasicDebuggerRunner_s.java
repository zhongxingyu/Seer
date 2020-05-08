 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 
 package org.eclipse.dltk.ruby.basicdebugger;
 
 import java.io.IOException;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.PreferencesLookupDelegate;
 import org.eclipse.dltk.core.environment.IDeployment;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IExecutionEnvironment;
 import org.eclipse.dltk.launching.DebuggingEngineRunner;
 import org.eclipse.dltk.launching.IInterpreterInstall;
 import org.eclipse.dltk.launching.InterpreterConfig;
 import org.eclipse.dltk.launching.debug.DbgpInterpreterConfig;
 import org.eclipse.dltk.ruby.debug.RubyDebugPlugin;
 import org.eclipse.dltk.ruby.internal.launching.JRubyInstallType;
 
 public class RubyBasicDebuggerRunner extends DebuggingEngineRunner {
 	public static final String ENGINE_ID = "org.eclipse.dltk.ruby.basicdebugger"; //$NON-NLS-1$
 
 	private static final String RUBY_HOST_VAR = "DBGP_RUBY_HOST"; //$NON-NLS-1$
 	private static final String RUBY_PORT_VAR = "DBGP_RUBY_PORT"; //$NON-NLS-1$
 	private static final String RUBY_KEY_VAR = "DBGP_RUBY_KEY"; //$NON-NLS-1$
 	private static final String RUBY_LOG_VAR = "DBGP_RUBY_LOG"; //$NON-NLS-1$
 
 	private static final String DEBUGGER_SCRIPT = "BasicRunner.rb"; //$NON-NLS-1$
 
 	protected IPath deploy(IDeployment deployment) throws CoreException {
 		try {
 			IPath deploymentPath = RubyBasicDebuggerPlugin.getDefault()
 					.deployDebuggerSource(deployment);
 			return deployment.getFile(deploymentPath).getPath();
 		} catch (IOException e) {
 			abort(
 					Messages.RubyBasicDebuggerRunner_unableToDeployDebuggerSource,
 					e);
 		}
 
 		return null;
 	}
 
 	public RubyBasicDebuggerRunner(IInterpreterInstall install) {
 		super(install);
 	}
 
 	protected InterpreterConfig addEngineConfig(InterpreterConfig config,
 			PreferencesLookupDelegate delegate) throws CoreException {
 		IEnvironment env = getInstall().getEnvironment();
 		IExecutionEnvironment exeEnv = (IExecutionEnvironment) env
 				.getAdapter(IExecutionEnvironment.class);
 		IDeployment deployment = exeEnv.createDeployment();
 
 		// Get debugger source location
 		final IPath sourceLocation = deploy(deployment);
 
 		final IPath scriptFile = sourceLocation.append(DEBUGGER_SCRIPT);
 
 		// Creating new config
 		InterpreterConfig newConfig = (InterpreterConfig) config.clone();
 
 		if (getInstall().getInterpreterInstallType() instanceof JRubyInstallType) {
 			newConfig.addEnvVar("JAVA_OPTS", "-Djruby.jit.enabled=false"); //$NON-NLS-1$ //$NON-NLS-2$
 			newConfig.addInterpreterArg("-X-C"); //$NON-NLS-1$
 		}
 
 		newConfig.addInterpreterArg("-r"); //$NON-NLS-1$
 		newConfig.addInterpreterArg(env.convertPathToString(scriptFile)); //$NON-NLS-1$
 		newConfig.addInterpreterArg("-I"); //$NON-NLS-1$
 		newConfig.addInterpreterArg(env.convertPathToString(sourceLocation)); //$NON-NLS-1$
 
 		// Environment
 		final DbgpInterpreterConfig dbgpConfig = new DbgpInterpreterConfig(
 				config);
 
 		String sessionId = dbgpConfig.getSessionId();
 
 		newConfig.addEnvVar(RUBY_HOST_VAR, dbgpConfig.getHost());
 		newConfig.addEnvVar(RUBY_PORT_VAR, Integer.toString(dbgpConfig
 				.getPort()));
 		newConfig.addEnvVar(RUBY_KEY_VAR, sessionId);
 
		if (isLoggingEnabled(delegate)) {
			newConfig.addEnvVar(RUBY_LOG_VAR, getLogFileName(delegate,
					sessionId));
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
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getDebuggingEnginePreferenceQualifier()
 	 */
 	protected String getDebuggingEnginePreferenceQualifier() {
 		return RubyBasicDebuggerPlugin.PLUGIN_ID;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLoggingEnabledPreferenceKey()
 	 */
 	protected String getLoggingEnabledPreferenceKey() {
 		return RubyBasicDebuggerConstants.ENABLE_LOGGING;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLogFileNamePreferenceKey()
 	 */
 	protected String getLogFileNamePreferenceKey() {
 		return RubyBasicDebuggerConstants.LOG_FILE_NAME;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.launching.DebuggingEngineRunner#getLogFilePathPreferenceKey()
 	 */
 	protected String getLogFilePathPreferenceKey() {
 		return RubyBasicDebuggerConstants.LOG_FILE_PATH;
 	}
 }
