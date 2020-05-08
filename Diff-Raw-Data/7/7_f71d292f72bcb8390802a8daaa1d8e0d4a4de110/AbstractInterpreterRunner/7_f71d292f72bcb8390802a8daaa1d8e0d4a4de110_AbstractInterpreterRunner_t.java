 /*******************************************************************************
  * Copyright (c) 2000, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.dltk.launching;
 
 import java.io.File;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.debug.core.DebugPlugin;
 import org.eclipse.debug.core.ILaunch;
 import org.eclipse.debug.core.model.IProcess;
 import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
 import org.eclipse.dltk.internal.launching.InterpreterMessages;
 
 import com.ibm.icu.text.DateFormat;
 import com.ibm.icu.text.MessageFormat;
 
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
 		return MessageFormat.format(format, new String[] { commandLine[0],
 				timestamp });
 	}
 
 	/**
 	 * String representation of the command line
 	 * 
 	 * @param commandLine
 	 * @return
 	 */
 	private static String renderCommandLineLabel(String[] commandLine) {
 		if (commandLine.length < 1)
 			return ""; //$NON-NLS-1$
 		StringBuffer buf = new StringBuffer();
 		for (int i = 0; i < commandLine.length; i++) {
 			buf.append(' ');
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
 	protected Process exec(String[] cmdLine, File workingDirectory)
 			throws CoreException {
 		return DebugPlugin.exec(cmdLine, workingDirectory);
 	}
 
 	// 
 	protected Map getDefaultProcessMap() {
 		Map map = new HashMap();
 		map.put(IProcess.ATTR_PROCESS_TYPE, getProcessType());
 		return map;
 	}
 
 	protected String getProcessType() {
 		return ScriptLaunchConfigurationConstants.ID_SCRIPT_PROCESS_TYPE;
 	}
 
 	protected AbstractInterpreterRunner(IInterpreterInstall install) {
 		this.interpreterInstall = install;
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
 	 * 
 	 */
 	private IProcess newProcess(ILaunch launch, Process p, String label,
 			Map attributes) throws CoreException {
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
 
 	protected IProcess rawRun(ILaunch launch, InterpreterConfig config)
 			throws CoreException {
 		checkConfig(config);
 		
 		String[] cmdLine = renderCommandLine(config);
 		File workingDirectory = config.getWorkingDirectoryPath().toFile();
 		String[] environment = config.getEnvironmentAsStringsIncluding(interpreterInstall.getEnvironmentVariables());
 
 		final String cmdLineLabel = renderCommandLineLabel(cmdLine);
 		final String processLabel = renderProcessLabel(cmdLine);
 
 		if (DLTKLaunchingPlugin.TRACE_EXECUTION) {
 			traceExecution(processLabel, cmdLineLabel, workingDirectory,
 					environment);
 		}
 		
 		Process p = DebugPlugin.exec(cmdLine, workingDirectory, environment);
 		if (p == null) {
 			abort("Execution was cancelled", null);
 		}
 
 		launch.setAttribute(DLTKLaunchingPlugin.LAUNCH_COMMAND_LINE,
 				cmdLineLabel);
 
 		IProcess process = newProcess(launch, p, processLabel,
 				getDefaultProcessMap());
 		process.setAttribute(IProcess.ATTR_CMDLINE, cmdLineLabel);
 		return process;
 	}
 
 	private void traceExecution(String processLabel,
 			String cmdLineLabel, File workingDirectory, String[] environment) {
 		StringBuffer sb = new StringBuffer();
 		sb.append("-----------------------------------------------\n");
 		sb.append("Running ").append(processLabel).append('\n');
 		sb.append("Command line: ").append(cmdLineLabel).append('\n');
 		sb.append("Working directory: ").append(workingDirectory).append('\n');
 		sb.append("Environment:\n");
 		for (int i=0; i<environment.length; i++) {
 			sb.append('\t').append(environment[i]).append('\n');
 		}
 		sb.append("-----------------------------------------------\n");
 		System.out.println(sb);
 	}
 
 	public void run(InterpreterConfig config, ILaunch launch,
 			IProgressMonitor monitor) throws CoreException {
 		if (monitor == null) {
 			monitor = new NullProgressMonitor();
 		}
 
 		try {
 			monitor.beginTask("Launching...", 5);
 			if (monitor.isCanceled()) {
 				return;
 			}
			alterConfig(launch, config);
 			monitor.worked(1);
 			monitor.subTask("Running");
 			rawRun(launch, config);
 			monitor.worked(4);
 
 		} finally {
 			monitor.done();
 		}
	}
 
	protected void alterConfig(ILaunch launch, InterpreterConfig config) {
 	}
 }
