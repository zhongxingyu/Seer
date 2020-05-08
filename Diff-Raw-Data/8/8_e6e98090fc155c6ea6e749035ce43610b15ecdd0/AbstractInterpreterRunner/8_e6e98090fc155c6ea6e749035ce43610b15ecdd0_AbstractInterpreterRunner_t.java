 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.launching;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugEvent;
 import org.eclipse.debug.core.DebugException;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.IDebugEventSetListener;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.environment.IEnvironment;
 import org.eclipse.dltk.core.environment.IExecutionEnvironment;
 import org.eclipse.dltk.core.environment.IExecutionLogger;
 import org.eclipse.dltk.core.environment.IFileHandle;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.internal.launching.InterpreterMessages;
 import org.eclipse.osgi.util.NLS;
 
 import com.ibm.icu.text.DateFormat;
 
 /**
  * Abstract implementation of a interpreter runner.
  * <p>
  * Clients implementing interpreter runners should subclass this class.
  * </p>
  * 
  * @see IInterpreterRunner
  * 
  */
 public abstract class AbstractInterpreterRunner implements IInterpreterRunner {
 	private IInterpreterInstall interpreterInstall;
 
 	protected IInterpreterInstall getInstall() {
 		return interpreterInstall;
 	}
 
 	private static String renderProcessLabel(String[] commandLine) {
 		String format = LaunchingMessages.StandardInterpreterRunner;
 		String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM,
 				DateFormat.MEDIUM).format(new Date(System.currentTimeMillis()));
 		return NLS.bind(format, commandLine[0], timestamp);
 	}
 
 	/**
 	 * String representation of the command line
 	 * 
 	 * @param commandLine
 	 * @return
 	 */
 	private static String renderCommandLineLabel(String[] commandLine) {
 		if (commandLine.length == 0)
 			return Util.EMPTY_STRING;
 		StringBuffer buf = new StringBuffer();
 		for (int i = 0; i < commandLine.length; i++) {
 			if (i != 0) {
 				buf.append(' ');
 			}
 			char[] characters = commandLine[i].toCharArray();
 			StringBuffer command = new StringBuffer();
 			boolean containsSpace = false;
 			for (int j = 0; j < characters.length; j++) {
 				char character = characters[j];
 				if (character == '\"') {
 					command.append('\\');
 				} else if (character == ' ') {
 					containsSpace = true;
 				}
 				command.append(character);
 			}
 			if (containsSpace) {
 				buf.append('\"');
 				buf.append(command.toString());
 				buf.append('\"');
 			} else {
 				buf.append(command.toString());
 			}
 		}
 		return buf.toString();
 	}
 
 	protected String renderCommandLineLabel(InterpreterConfig config) {
 		String[] cmdLine = renderCommandLine(config);
 		return renderCommandLineLabel(cmdLine);
 	}
 
 	protected void abort(String message, Throwable exception)
 			throws CoreException {
 		throw new CoreException(new Status(IStatus.ERROR,
 				DLTKLaunchingPlugin.PLUGIN_ID,
 				ScriptLaunchConfigurationConstants.ERR_INTERNAL_ERROR, message,
 				exception));
 	}
 
 	protected void abort(String message, Throwable exception, int code)
 			throws CoreException {
 		throw new CoreException(new Status(IStatus.ERROR,
 				DLTKLaunchingPlugin.PLUGIN_ID, code, message, exception));
 	}
 
 	// Execution helpers
 	// protected Process exec(String[] cmdLine, File workingDirectory)
 	// throws CoreException {
 	// return DebugPlugin.exec(cmdLine, workingDirectory);
 	// }
 
 	//
 	protected Map<String, String> getDefaultProcessMap() {
 		Map<String, String> map = new HashMap<String, String>();
 		map.put(IProcess.ATTR_PROCESS_TYPE, getProcessType());
 		return map;
 	}
 
 	protected String getProcessType() {
 		return ScriptLaunchConfigurationConstants.ID_SCRIPT_PROCESS_TYPE;
 	}
 
 	protected AbstractInterpreterRunner(IInterpreterInstall install) {
 		this.interpreterInstall = install;
 	}
 
 	protected void checkConfig(InterpreterConfig config,
 			IEnvironment environment) throws CoreException {
 		IPath workingDirectoryPath = config.getWorkingDirectoryPath();
 		IFileHandle dir = environment.getFile(workingDirectoryPath);
 		if (!dir.exists()) {
 			abort(
 					NLS
 							.bind(
 									InterpreterMessages.errDebuggingEngineWorkingDirectoryDoesntExist,
 									dir.toString()), null);
 		}
 		if (config.getScriptFilePath() == null) {
 			return;
 		}
 		if (!config.isNoFile()) {
 			final IFileHandle script = environment.getFile(config
 					.getScriptFilePath());
 			if (!script.exists()) {
 				abort(
 						NLS
 								.bind(
 										InterpreterMessages.errDebuggingEngineScriptFileDoesntExist,
 										script.toString()), null);
 			}
 		}
 	}
 
 	/**
 	 * Returns a new process aborting if the process could not be created.
 	 * 
 	 * @param launch
 	 *            the launch the process is contained in
 	 * @param p
 	 *            the system process to wrap
 	 * @param label
 	 *            the label assigned to the process
 	 * @param attributes
 	 *            values for the attribute map
 	 * @return the new process
 	 * @throws CoreException
 	 *             problems occurred creating the process
 	 * @since 2.0
 	 * 
 	 */
 	protected IProcess newProcess(ILaunch launch, Process p, String label,
 			Map<String, String> attributes) throws CoreException {
 		IProcess process = DebugPlugin.newProcess(launch, p, label, attributes);
 		if (process == null) {
 			p.destroy();
 			abort(LaunchingMessages.AbstractInterpreterRunner_0, null);
 		}
 		return process;
 	}
 
 	protected String[] renderCommandLine(InterpreterConfig config) {
 		return config.renderCommandLine(interpreterInstall);
 	}
 
 	protected IProcess rawRun(final ILaunch launch, InterpreterConfig config)
 			throws CoreException {
 
 		checkConfig(config, getInstall().getEnvironment());
 
 		String[] cmdLine = renderCommandLine(config);
 		IPath workingDirectory = config.getWorkingDirectoryPath();
 		String[] environment = getEnvironmentVariablesAsStrings(config);
 
 		final String cmdLineLabel = renderCommandLineLabel(cmdLine);
 		final String processLabel = renderProcessLabel(cmdLine);
 
 		if (DLTKLaunchingPlugin.TRACE_EXECUTION) {
 			traceExecution(processLabel, cmdLineLabel, workingDirectory,
 					environment);
 		}
 
 		IExecutionEnvironment exeEnv = interpreterInstall.getExecEnvironment();
 		IExecutionLogger logger = DLTKLaunchingPlugin.LOGGING_CATCH_OUTPUT
 				.isEnabled() ? new LaunchLogger() : null;
 		Process p = exeEnv.exec(cmdLine, workingDirectory, environment, logger);
 		if (p == null) {
 			abort(
 					LaunchingMessages.AbstractInterpreterRunner_executionWasCancelled,
 					null);
 		}
 
 		launch.setAttribute(DLTKLaunchingPlugin.LAUNCH_COMMAND_LINE,
 				cmdLineLabel);
 		final IProcess process[] = new IProcess[] { null };
 		DebugPlugin.getDefault().addDebugEventListener(
 				new IDebugEventSetListener() {
 					public void handleDebugEvents(DebugEvent[] events) {
 						for (int i = 0; i < events.length; i++) {
 							DebugEvent event = events[i];
 							if (event.getSource().equals(process[0])) {
 								if (event.getKind() == DebugEvent.CHANGE
 										|| event.getKind() == DebugEvent.TERMINATE) {
 									updateProcessLabel(launch, cmdLineLabel,
 											process[0]);
 									if (event.getKind() == DebugEvent.TERMINATE) {
 										DebugPlugin.getDefault()
 												.removeDebugEventListener(this);
 									}
 								}
 							}
 						}
 					}
 				});
 		process[0] = newProcess(launch, p, processLabel, getDefaultProcessMap());
 		process[0].setAttribute(IProcess.ATTR_CMDLINE, cmdLineLabel);
 		updateProcessLabel(launch, cmdLineLabel, process[0]);
 		return process[0];
 	}
 
 	private void updateProcessLabel(final ILaunch launch,
 			final String cmdLineLabel, final IProcess process) {
 		StringBuffer buffer = new StringBuffer();
 		int exitValue = 0;
 		try {
 			exitValue = process.getExitValue();
 		} catch (DebugException e1) {
 			// DLTKCore.error(e1);
 			exitValue = 0;// Seems not available yet
 		}
 		if (exitValue != 0) {
 			buffer.append("<abnormal exit code:" + exitValue + "> ");
 		}
 		String type = null;
 		ILaunchConfiguration launchConfiguration = launch
 				.getLaunchConfiguration();
 		if (launchConfiguration != null) {
 			try {
				ILaunchConfigurationType launchConfigType = launchConfiguration
						.getType();
				if (launchConfigType != null) {
					type = launchConfigType.getName();
				}
 			} catch (CoreException e) {
 				DLTKCore.error(e);
 			}
 			buffer.append(launchConfiguration.getName());
 		}
 		if (type != null) {
 			buffer.append(" ["); //$NON-NLS-1$
 			buffer.append(type);
 			buffer.append("] "); //$NON-NLS-1$
 		}
 		buffer.append(process.getLabel());
 		process.setAttribute(IProcess.ATTR_PROCESS_LABEL, buffer.toString());
 	}
 
 	/**
 	 * @since 2.0
 	 */
 	protected String[] getEnvironmentVariablesAsStrings(InterpreterConfig config) {
 		return config.getEnvironmentAsStringsIncluding(getInstall()
 				.getEnvironmentVariables());
 	}
 
 	private void traceExecution(String processLabel, String cmdLineLabel,
 			IPath workingDirectory, String[] environment) {
 		StringBuffer sb = new StringBuffer();
 		sb.append("-----------------------------------------------\n"); //$NON-NLS-1$
 		sb.append("Running ").append(processLabel).append('\n'); //$NON-NLS-1$
 		sb.append("Command line: ").append(cmdLineLabel).append('\n'); //$NON-NLS-1$
 		sb.append("Working directory: ").append(workingDirectory).append('\n'); //$NON-NLS-1$
 		sb.append("Environment:\n"); //$NON-NLS-1$
 		for (int i = 0; i < environment.length; i++) {
 			sb.append('\t').append(environment[i]).append('\n');
 		}
 		sb.append("-----------------------------------------------\n"); //$NON-NLS-1$
 		System.out.println(sb);
 	}
 
 	public void run(InterpreterConfig config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 
 		try {
 			monitor.beginTask(
 					LaunchingMessages.AbstractInterpreterRunner_launching, 5);
 			if (monitor.isCanceled()) {
 				return;
 			}
 			alterConfig(launch, config);
 			monitor.worked(1);
 			monitor
 					.subTask(LaunchingMessages.AbstractInterpreterRunner_running);
 			rawRun(launch, config);
 			monitor.worked(4);
 
 		} finally {
 			monitor.done();
 		}
 	}
 
 	protected void alterConfig(ILaunch launch, InterpreterConfig config) {
 	}
 }
