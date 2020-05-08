 package org.eclipse.dltk.launching;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 
 public class InterpreterConfig implements Cloneable {
 	/**
 	 * Script file to launch
 	 */
 	private IPath scriptFile;
 
 	/**
 	 * Working directory
 	 */
 	private IPath workingDirectory;
 
 	/**
 	 * Arguments for interpreter (Strings)
 	 */
 	private ArrayList interpreterArgs;
 
 	/**
 	 * Arguments for script (Strings)
 	 */
 	private ArrayList scriptArgs;
 
 	/**
 	 * Environment variables (String => String)
 	 */
 	private HashMap environment;
 
 	/**
 	 * Additional properties (String => Object)
 	 */
 	private HashMap properties;
 
 	protected void checkScriptFile(File file) {
 		if (file == null) {
 			throw new IllegalArgumentException("Script file cannot be null");
 		}
 	}
 
 	protected void checkScriptFile(IPath file) {
 		if (file == null) {
 			throw new IllegalArgumentException("Script file cannot be null");
 		}
 	}
 
 	protected void checkWorkingDirectory(File directory) {
 		if (directory == null) {
 			throw new IllegalArgumentException(
 					"Working directory cannot be null");
 		}
 	}
 
 	protected void checkWorkingDirectory(IPath directory) {
 		if (directory == null) {
 			throw new IllegalArgumentException(
 					"Working directory cannot be null");
 		}
 	}
 
 	protected void init(IPath scriptFile, IPath workingDirectory) {
 		init(scriptFile, workingDirectory, true);
 	}
 
 	protected void init(IPath scriptFile, IPath workingDirectory,
 			boolean isLocal) {
 		// local debugger run
 		if (isLocal) {
 			// Script file
 			this.scriptFile = scriptFile;
 
 			// Working directory
 			this.workingDirectory = workingDirectory != null ? workingDirectory
 					: scriptFile.removeLastSegments(1);
 		}
 
 		this.interpreterArgs = new ArrayList();
 		this.scriptArgs = new ArrayList();
 		this.environment = new HashMap();
 		this.properties = new HashMap();
 	}
 
 	public InterpreterConfig() {
 		init(null, null, false);
 	}
 
 	public InterpreterConfig(File scriptFile) {
 		this(scriptFile, (File) null);
 	}
 
 	public InterpreterConfig(File scriptFile, File workingDirectory) {
 		if (scriptFile == null) {
 			throw new IllegalArgumentException();
 		}
 
 		init(new Path(scriptFile.getAbsolutePath()),
 				workingDirectory == null ? null : new Path(workingDirectory
 						.getAbsolutePath()));
 	}
 
 	public InterpreterConfig(IPath scriptFile) {
 		this(scriptFile, (IPath) null);
 	}
 
 	public InterpreterConfig(IPath scriptFile, IPath workingDirectory) {
 		checkScriptFile(scriptFile);
 		init(scriptFile, workingDirectory);
 	}
 
 	// Script file
 	/**
 	 * @deprecated Use getScriptFilePath instead
 	 */
 	public File getScriptFile() {
 		return scriptFile.toFile();
 	}
 
 	public IPath getScriptFilePath() {
 		return scriptFile;
 	}
 
 	public void setScriptFile(File file) {
 		checkScriptFile(file);
 		setScriptFile(new Path(file.toString()));
 	}
 
 	public void setScriptFile(IPath file) {
 		checkScriptFile(file);
 		this.scriptFile = file;
 	}
 
 	// Working directory
 	/**
 	 * @deprecated Use getWorkingDirectoryPath instead
 	 */
 	public File getWorkingDirectory() {
 		return workingDirectory.toFile();
 	}
 
 	public IPath getWorkingDirectoryPath() {
 		return workingDirectory;
 	}
 
 	public void setWorkingDirectory(File directory) {
 		checkWorkingDirectory(directory);
 		setWorkingDirectory(new Path(directory.toString()));
 	}
 
 	public void setWorkingDirectory(IPath directory) {
 		checkWorkingDirectory(directory);
 		this.workingDirectory = directory;
 	}
 
 	// Interpreter section
 	public boolean addInterpreterArg(String arg) {
 		if (arg == null) {
 			throw new IllegalArgumentException(
 					"Interpreter argument cannot be null");
 		}
 
 		return interpreterArgs.add(arg);
 	}
 
 	public void addInterpreterArgs(String[] args) {
 		for (int i = 0; i < args.length; ++i) {
 			addInterpreterArg(args[i]);
 		}
 	}
 
 	public void addInterpreterArgs(List args) {
 		interpreterArgs.addAll(args);
 	}
 
 	public boolean hasInterpreterArg(String arg) {
 		return interpreterArgs.contains(arg);
 	}
 
 	public boolean hasMatchedInterpreterArg(String regex) {
 		Iterator it = interpreterArgs.iterator();
 		while (it.hasNext()) {
 			if (((String) it.next()).matches(regex)) {
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	public boolean removeInterpreterArg(String arg) {
 		return interpreterArgs.remove(arg);
 	}
 
 	public List getInterpreterArgs() {
 		return (List) interpreterArgs.clone();
 	}
 
 	// Script section
 	public boolean addScriptArg(String arg) {
 		if (arg == null) {
 			throw new IllegalArgumentException("Script argument cannot be null");
 		}
 
 		return scriptArgs.add(arg);
 	}
 
 	// Script section
 	public void addScriptArg(String arg, int pos) {
 		if (arg == null) {
 			throw new IllegalArgumentException("Script argument cannot be null");
 		}
 
 		scriptArgs.add(pos, arg);
 	}
 
 	public void addScriptArgs(String[] args) {
 		for (int i = 0; i < args.length; ++i) {
 			addScriptArg(args[i]);
 		}
 	}
 
 	public void addScriptArgs(List args) {
 		scriptArgs.addAll(args);
 	}
 
 	public boolean hasScriptArg(String arg) {
 		return scriptArgs.contains(arg);
 	}
 
 	public boolean removeScriptArg(String arg) {
 		return scriptArgs.remove(arg);
 	}
 
 	public List getScriptArgs() {
 		return (List) scriptArgs.clone();
 	}
 
 	// Environment
 	public String addEnvVar(String name, String value) {
 		if (name == null || value == null) {
 			throw new IllegalArgumentException();
 		}
 
 		return (String) environment.put(name, value);
 	}
 
 	public void addEnvVars(Map vars) {
 		environment.putAll(vars);
 	}
 
 	public String removeEnvVar(String name) {
 		if (name == null) {
 			throw new IllegalArgumentException();
 		}
 
 		return (String) environment.remove(name);
 	}
 
 	public String getEnvVar(String name) {
 		if (name == null) {
 			throw new IllegalArgumentException();
 		}
 
 		return (String) environment.get(name);
 	}
 
 	public boolean hasEnvVar(String name) {
 		if (name == null) {
 			throw new IllegalArgumentException();
 		}
 
 		return environment.containsKey(name);
 	}
 
 	public Map getEnvVars() {
 		return (Map) environment.clone();
 	}
 
 	public String[] getEnvironmentAsStrings() {
 		ArrayList list = new ArrayList();
 		Iterator it = environment.keySet().iterator();
 		while (it.hasNext()) {
 			String key = (String) it.next();
 			String value = (String) environment.get(key);
 			list.add(key + "=" + value);
 		}
 
 		return (String[]) list.toArray(new String[list.size()]);
 	}
 
 	public String[] getEnvironmentAsStringsIncluding(
 			EnvironmentVariable[] variables) {
 		ArrayList list = new ArrayList();
 		if (variables != null) {
 			for (int i = 0; i < variables.length; i++) {
 				list
 						.add(variables[i].getName() + "="
 								+ variables[i].getValue());
 			}
 		}
 
 		Iterator it = environment.keySet().iterator();
 		while (it.hasNext()) {
 			String key = (String) it.next();
 			String value = (String) environment.get(key);
 			list.add(key + "=" + value);
 		}
 
 		return (String[]) list.toArray(new String[list.size()]);
 	}
 
 	// Properties
 	public Object setProperty(String name, Object value) {
 		return properties.put(name, value);
 	}
 
 	public void unsetProperty(String name) {
 		properties.remove(name);
 	}
 
 	public Object getProperty(String name) {
 		return properties.get(name);
 	}
 
 	public void addProperties(Map map) {
 		properties.putAll(map);
 	}
 
 	public Map getPropeties() {
 		return (Map) properties.clone();
 	}
 
 	public Object clone() {
 		final InterpreterConfig config = new InterpreterConfig(scriptFile,
 				workingDirectory);
 		config.addProperties(getPropeties());
 		config.addEnvVars(getEnvVars());
 		config.addInterpreterArgs(getInterpreterArgs());
 		config.addScriptArgs(getScriptArgs());
 		return config;
 	}
 
 	public String[] renderCommandLine(IInterpreterInstall interpreter) {
 		final List items = new ArrayList();
 
 		items.add(interpreter.getInstallLocation().getAbsolutePath());
 		items.addAll(interpreterArgs);
 
 		String[] interpreterOwnArgs = interpreter.getInterpreterArguments();
 		if (interpreterOwnArgs != null) {
 			items.addAll(Arrays.asList(interpreterOwnArgs));
 		}
 
 		items.add(scriptFile.toPortableString());
 		items.addAll(scriptArgs);
 
 		return (String[]) items.toArray(new String[items.size()]);
 	}
 
 	protected String[] renderCommandLine(IPath interpreter) {
 		final List items = new ArrayList();
 
 		items.add(interpreter.toPortableString());
 		items.addAll(interpreterArgs);
 		items.add(scriptFile.toPortableString());
 		items.addAll(scriptArgs);
 
 		return (String[]) items.toArray(new String[items.size()]);
 	}
 
 	public String[] renderCommandLine(String interpreter) {
 		return renderCommandLine(new Path(interpreter));
 	}
 
 	// TODO: make more real implementation
 	public String toString() {
 		final List items = new ArrayList();
 		items.add("<interpreter>");
 		items.addAll(interpreterArgs);
 		items.add(scriptFile.toPortableString());
 		items.addAll(scriptArgs);
 
 		Iterator it = items.iterator();
 		StringBuffer sb = new StringBuffer();
 		while (it.hasNext()) {
 			sb.append(it.next());
 			sb.append(' ');
 		}
 
 		return sb.toString();
 	}

	public void clearScriptArgs() {
		this.scriptArgs.clear();
	}
 }
