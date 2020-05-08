 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package uk.ac.diamond.scisoft;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.IOUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.FileLocator;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.ui.IStartup;
 import org.osgi.framework.Bundle;
 import org.python.copiedfromeclipsesrc.JavaVmLocationFinder;
 import org.python.pydev.core.IInterpreterInfo;
 import org.python.pydev.core.IPythonNature;
 import org.python.pydev.core.MisconfigurationException;
 import org.python.pydev.core.REF;
 import org.python.pydev.core.Tuple;
 import org.python.pydev.editor.codecompletion.revisited.ModulesManagerWithBuild;
 import org.python.pydev.plugin.PydevPlugin;
 import org.python.pydev.runners.SimpleJythonRunner;
 import org.python.pydev.ui.interpreters.JythonInterpreterManager;
 import org.python.pydev.ui.pythonpathconf.InterpreterInfo;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class JythonCreator implements IStartup {
 
 	private static Logger logger = LoggerFactory.getLogger(JythonCreator.class);
 	private Boolean isRunningInEclipse;
 	
 	@Override
 	public void earlyStartup() {
 		try {
 			initialiseInterpreter(new NullProgressMonitor());
 		} catch (CoreException e) {
 			logger.error("Cannot create interpreter!", e);
 		}
 	}
 	
 	
 	private static final String JYTHON_VERSION = "2.5.1";
 	public static final String  INTERPRETER_NAME = "Jython" + JYTHON_VERSION;
 	public static final String  GIT_SUFFIX = "_git";
 	private static final String RUN_IN_ECLIPSE = "run.in.eclipse";
 	private static final String[] requiredKeys = {"org.python.pydev",
 		"uk.ac.gda.libs",
 		"cbflib",
 		"org.apache.commons.codec",
 		"org.apache.commons.math",
 		"uk.ac.diamond.CBFlib",
 		"uk.ac.diamond.jama",
 		"uk.ac.diamond.scisoft",
 		"uk.ac.diamond.scisoft.ncd",
 		"uk.ac.diamond.scisoft.ncd.rcp",
 		"uk.ac.gda.common",
 		"jhdf",
 		"com.springsource.slf4j",
 		"com.springsource.ch.qos.logback",
 		"com.springsource.org.castor",
 		"com.springsource.org.exolab.castor",
 		"com.springsource.org.apache.commons",
 		"com.springsource.javax.media",
 		"jtransforms",
 		"jai_imageio",
 		"it.tidalwave.imageio.raw",
 		"vecmath",
 		"jython",
 		"commons-math",
 		"uk.ac.diamond.org.apache.ws.commons.util",
 		"uk.ac.diamond.org.apache.xmlrpc.client",
 		"uk.ac.diamond.org.apache.xmlrpc.common",
 		"uk.ac.diamond.org.apache.xmlrpc.server",
 		"org.dawb.hdf5"
 	};
 
 	private final static String[] pluginKeys = { "uk.ac.diamond", "uk.ac.gda", "org.dawb.hdf5", "ncsa.hdf"};
 	
 	private void initialiseInterpreter(IProgressMonitor monitor) throws CoreException {
 
 		logger.debug("Initialising the Jython interpreter setup");
 
 		if (isRunningInEclipse == null) {
 			String runProp = System.getProperty(RUN_IN_ECLIPSE);
 			 isRunningInEclipse = runProp != null && runProp.equalsIgnoreCase("true");
 		}
 
 		// Horrible Hack warning: This code is copied from parts of Pydev to set up the interpreter and save it.
 		{
 
 			String defaultPluginsDir = System.getenv("SDA_HOME");
 			File pluginsDir = null;
 			if (defaultPluginsDir != null) {
 				pluginsDir = new File(defaultPluginsDir);
 				logger.debug("SDA_HOME defined as " + defaultPluginsDir);
 				if (!pluginsDir.isDirectory()) {
 					logger.debug("But SDA_HOME does not exist or is not a directory");
 					pluginsDir = null;
 				}
 			}
 			if (pluginsDir == null)
 				pluginsDir = getPluginsDirectory();
 			if (pluginsDir == null) {
 				logger.error("Failed to find plugins directory!");
 				return;
 			}
 
 			
 			// Code copies from Pydev when the user chooses a Jython interpreter - these are the defaults		
 			String executable = new File(getInterpreterDirectory(pluginsDir), "jython.jar").getAbsolutePath();
 			
 			final Bundle libBundle = Platform.getBundle("uk.ac.gda.libs");
 			
 			if(!(new File(executable)).exists()) { 
 			
 				logger.warn("Could not find jython jar, looking again");
 				// try to find the jar another way
 				String bundleLoc = libBundle.getLocation().replace("reference:file:", "");
 				File jarPath = new File(bundleLoc,"jython2.5.1/jython.jar");
 				
 				executable = jarPath.getAbsolutePath();
 
 			}
 			
 			
 			logger.debug("executable path = {}", executable);
 
 			// check for the existence of this standard pydev script
 			final File script = PydevPlugin.getScriptWithinPySrc("interpreterInfo.py");
 			if (!script.exists()) {
 				logger.error("The file specified does not exist: {} ", script);
 				throw new RuntimeException("The file specified does not exist: " + script);
 			}
 
 			logger.debug("Script path = {}", script.getAbsolutePath());
 			
 			
			String[] cmdarray = {"java", "-Xmx64m", "-jar",executable, REF.getFileAbsolutePath(script)};
 			File workingDir = new File(System.getProperty("java.io.tmpdir"));
 			IPythonNature nature = null;//new PythonNature();
 			Tuple<Process, String> outTup2 = new SimpleJythonRunner().run(cmdarray, workingDir, nature, monitor);
 		
 			String outputString = "";
 			try {
 				outputString = IOUtils.toString(outTup2.o1.getInputStream());
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				logger.error("TODO put description of error here", e1);
 			}
 			  
 			logger.debug("Output String is {}", outputString);
 
 			// this is the main info object which contains the environment data
 			InterpreterInfo info = null;
 
 			try {
 				// HACK Otherwise Pydev shows a dialog to the user.
 				ModulesManagerWithBuild.IN_TESTS = true;
 				//info = InterpreterInfo.fromString(outTup.o1, false);
 				info = InterpreterInfo.fromString(outputString, false);
 			} catch (Exception e) {
 				logger.error("InterpreterInfo.fromString(outTup.o1) has failed in pydev setup with exception");
 				logger.error("{}",e);
 
 			} finally {
 				ModulesManagerWithBuild.IN_TESTS = false;
 			}		
 
 			if (info == null) {
 				logger.error("pydev info is set to null");
 				return;
 			}
 
 			// set of python paths
 			Set<String> pyPaths = new HashSet<String>();
 
 			// the executable is the jar itself
 			info.executableOrJar = executable;
 
 			if (System.getProperty("os.name").contains("Windows"))
 				logPaths("Library paths:", System.getenv("PATH"));
 			else
 				logPaths("Library paths:", System.getenv("LD_LIBRARY_PATH"));
 
 			logPaths("Class paths:", System.getProperty("java.library.path"));
 
 			// we have to find the jars before we restore the compiled libs
 			List<File> jars = JavaVmLocationFinder.findDefaultJavaJars();
 			for (File jar : jars) {
 				if (pyPaths.contains(jar.getAbsolutePath())) {
 					logger.warn("File already there!");
 				}
 				pyPaths.add(jar.getAbsolutePath());
 			}
 
 			// Defines all third party libs that can be used in scripts.
 			logger.debug("Adding files to python path");
 			final List<File> gdaJars = findJars(pluginsDir);
 			for (File file : gdaJars) {
 				if (pyPaths.contains(file.getAbsolutePath())) {
 					logger.warn("File already there!");
 				}
 				pyPaths.add(file.getAbsolutePath());
 				logger.debug("Adding jar file to python path : {} ", file.getAbsolutePath());
 			}
 
 			final List<File> gdaDirs = findDirs(pluginsDir);
 
 			logger.debug("All Jars prepared");
 
 			if (isRunningInEclipse) {
 				File bundles = pluginsDir.getParentFile();
 				if (bundles.isDirectory()) {
 					// ok checking for items inside the tp directory
 					bundles = new File(bundles, "tp");
 					if (bundles.isDirectory()) {
 						bundles = new File(bundles, "plugins");
 						final List<File> tJars = findJars(bundles);
 						for (File file : tJars) {
 							if (pyPaths.contains(file.getAbsolutePath())) {
 								logger.warn("File already there!");
 							}
 							pyPaths.add(file.getAbsolutePath());
 							logger.debug("Adding jar file to python path : {} ", file.getAbsolutePath());
 						}						
 					}
 				}
 
 				// add plugins and ScisoftPy package
 				for (File file: gdaDirs) {
 					File b = new File(file, "bin");
 					if (b.isDirectory()) {
 						if (pyPaths.contains(b.getAbsolutePath())) {
 							logger.warn("File already there!");
 						}
 						pyPaths.add(b.getAbsolutePath());
 						logger.debug("Adding dir to python path : {} ", b.getAbsolutePath());
 					} 
 					// also check for internal jars
 					File j = new File(file, "jars");
 					if (j.isDirectory()) {
 						for(File jar : j.listFiles()) {
 							if (pyPaths.contains(jar.getAbsolutePath())) {
 								logger.warn("File already there!");
 							}
 							pyPaths.add(jar.getAbsolutePath());
 							logger.debug("Adding jar to python path : {} ", jar.getAbsolutePath());
 						}
 					} 
 					// and 1 further possible place
 					// also check for internal jars
 					File jj = new File(j, "ext");
 					if (jj.isDirectory()) {
 						for(File jar : jj.listFiles()) {
 							if (pyPaths.contains(jar.getAbsolutePath())) {
 								logger.warn("File already there!");
 							}
 							pyPaths.add(jar.getAbsolutePath());
 							logger.debug("Adding jar to python path : {} ", jar.getAbsolutePath());
 						}
 					}
 
 				}
 			} else {
 				// and add all unjarred folders
 				for (File file: gdaDirs) {
 					if (pyPaths.contains(file.getAbsolutePath())) {
 						logger.warn("File already there!");
 					}
 					pyPaths.add(file.getAbsolutePath());
 					logger.debug("Adding dir to python path : {} ", file.getAbsolutePath());
 				}
 			}
 
 			info.libs.addAll(pyPaths);
 			Collections.sort(info.libs);
 
 			// now set up the LD_LIBRARY_PATH, or PATH for windows
 			File libraryDir = new File(pluginsDir.getParentFile(), "lib");
 			String libraryPath;
 			if (libraryDir.exists()) {
 				libraryPath = libraryDir.getAbsolutePath();
 			} else {
 				StringBuilder allPaths = new StringBuilder();
 				String osarch = Platform.getOS() + "-" + Platform.getOSArch();
 				logger.debug("Using OS and ARCH: {}", osarch);
 				for (File dir : gdaDirs) {
 					File d = new File(dir, "lib");
 					if (d.isDirectory()) {
 						d = new File(d, osarch);
 						if (d.isDirectory()) {
 							if (allPaths.length() != 0) {
 								allPaths.append(File.pathSeparatorChar);
 							}
 							logger.debug("Adding library path : {}", d);
 							allPaths.append(d.getAbsolutePath());
 						}
 					}
 				}
 
 				libraryPath = allPaths.toString();
 			}
 
 			// TODO include 'Mac OS X' if needed "DYLD_LIBRARY_PATH" + others ...
 			String pathEnv = System.getProperty("os.name").contains("Windows") ? "PATH" : "LD_LIBRARY_PATH";
 			String env = pathEnv + "=" + libraryPath + File.pathSeparator + System.getenv(pathEnv);
 
 			logPaths("Setting " + pathEnv + " for dynamic libraries", env);
 
 			PyDevAdditionalInterpreterSettings settings = new PyDevAdditionalInterpreterSettings();
 			Collection<String> envVariables = settings.getAdditionalEnvVariables();
 			envVariables.add(env);
 			
 			String[] envVarsAlreadyIn = info.getEnvVariables();
 			if (envVarsAlreadyIn != null) {
 				envVariables.addAll(Arrays.asList(envVarsAlreadyIn));
 			}
 			
 			info.setEnvVariables(envVariables.toArray(new String[envVariables.size()]));
 
 			// java, java.lang, etc should be found now
 			info.restoreCompiledLibs(monitor);
 			info.setName(INTERPRETER_NAME);
 
 			logger.debug("Finalising the Jython interpreter manager");
 
 			final JythonInterpreterManager man = (JythonInterpreterManager) PydevPlugin.getJythonInterpreterManager();
 			HashSet<String> set = new HashSet<String>();
 			// Note, despite argument in PyDev being called interpreterNamesToRestore
 			// in this context that name is the exe. 
 			// Pydev doesn't allow two different interpreters to be configured for the same
 			// executable path so in some contexts the executable is the unique identifier (as it is here)
 			set.add(executable);
 			
 			// Attempt to update existing Jython configuration
 			IInterpreterInfo[] interpreterInfos = man.getInterpreterInfos();
 			InterpreterInfo existingInfo = null;
 			try {
 				existingInfo = man.getInterpreterInfo(executable, monitor);
 			} catch (MisconfigurationException e) {
 				// MisconfigurationException thrown if executable not found
 			}
 			
 			if (existingInfo != null && existingInfo.toString().equals(info.toString())) {
 				logger.debug("Jython interpreter already exists with exact settings");
 			} else {
 				List<IInterpreterInfo> infos = new ArrayList<IInterpreterInfo>(Arrays.asList(interpreterInfos));
 				if (existingInfo == null) {
 					logger.debug("Adding interpreter as an additional interpreter");
 				} else {
 					logger.debug("Updating interpreter which was previously created");
 					for (int i = 0; i < interpreterInfos.length; i++) {
 						if (infos.get(i) == existingInfo) {
 							infos.remove(i);
 							break;
 						}
 					}
 				}
 				infos.add(info);
 				logger.debug("Removing existing interpreter with the same name");
 				man.setInfos(infos.toArray(new IInterpreterInfo[infos.size()]), set, monitor);
 			}
 
 			logger.debug("Finished the Jython interpreter setup");
 		}
 	}
 
 	private static String JYTHON_DIR = "jython" + JYTHON_VERSION;
 	private File getInterpreterDirectory(File pluginsDir) {
 
 		for (File file : pluginsDir.listFiles()) {
 			if(file.getName().startsWith("uk.ac.gda.libs_")) {
 				File d = new File(file, JYTHON_DIR);
 				return d;
 			}
 
 		}
 		logger.error("Could not find a folder for 'uk.ac.gda.libs' defaulting to standard");
 		return new File(pluginsDir, "uk.ac.gda.libs/" + JYTHON_DIR);
 	}
 
 	
 	/**
 	 * Method returns recursively all the jars found in a directory
 	 * 
 	 * @return list of jar Files
 	 */
 	public static final List<File> findJars(File directoryName) {
 
 		final List<File> libs = new ArrayList<File>();
 
 		if (directoryName.exists() && directoryName.isDirectory()) {
 			for (File f : directoryName.listFiles()) {
 
 				// if the file is a jar, then add it
 				if (f.getName().endsWith(".jar")) {
 					if (isRequired(f, requiredKeys)) {
 						libs.add(f);
 					}
 				} else if (f.isDirectory()) {
 					for (File file : findJars(f)) {
 						libs.add(file);
 					}
 				}
 			}
 		}
 
 		return libs;
 	}
 
 	private File getPluginsDirectory() {
 		Bundle b = Platform.getBundle(Activator.PLUGIN_ID);
 		logger.debug("Bundle: {}", b);
 		try {
 			File f = FileLocator.getBundleFile(b);
 			logger.debug("Bundle location: {}", f.getParent());
 
 			if (isRunningInEclipse) {
 				File git = f.getParentFile().getParentFile().getParentFile();
 				File parent = git.getParentFile();
 				String projectName = git.getName().replace(GIT_SUFFIX, "");
 				File plugins = new File(parent, projectName + "/plugins");
 
 				if (plugins.isDirectory()) {
 					logger.debug("Plugins location: {}", plugins);
 					return plugins;
 				}
 			}
 
 			return f.getParentFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private void logPaths(String pathname, String paths) {
 		if (paths == null)
 			return;
 		logger.debug(pathname);
 		for (String p : paths.split(File.pathSeparator))
 			logger.debug("\t{}", p);
 	}
 
 
 	/**
 	 * Method returns path to directories
 	 * 
 	 * @return list of directories
 	 */
 	private List<File> findDirs(File directoryName) {
 
 		// ok we get the plugins directory here, so we need to explore a bit further for git
 		final List<File> libs = new ArrayList<File>();
 
 		// get the basic plugins directory
 		if (directoryName.exists() && directoryName.isDirectory()) {
 			for (File f : directoryName.listFiles()) {
 				if (f.isDirectory()) {
 					if (isRequired(f, pluginKeys)) {
 						logger.debug("Adding plugin directory {}", f);
 						libs.add(f);
 					}
 				}
 			}
 		}
 
 		// get down to the git checkouts
 		// only do this if we are running inside Eclipse
 		if (isRunningInEclipse) {
 			String gitpathname = directoryName.getParentFile().getAbsolutePath() + JythonCreator.GIT_SUFFIX;
 
 			List<File> dirs = new ArrayList<File>();
 
 			for (File d : new File(gitpathname).listFiles()) {
 				if (d.isDirectory()) {
 					String n = d.getName();
 					if (n.endsWith(".git")) {
 						dirs.add(d);
 					} else if (n.equals("scisoft")) {
 						for (File f : d.listFiles()) {
 							if (f.isDirectory()) {
 								logger.debug("Adding scisoft directory {}", f);
 								dirs.add(f);
 							}
 						}
 					}
 				}
 			}
 
 			for (File f : dirs) {
 				for (File plugin : f.listFiles()) {
 					if (plugin.isDirectory()) {
 						if (isRequired(plugin, pluginKeys)) {
 							logger.debug("Adding plugin directory {}", f);
 							libs.add(plugin);
 						}
 					}
 				}
 			}
 		}
 
 		return libs;
 	}
 
 	private static boolean isRequired(File file, String[] keys) {
 		String filename = file.getName();
 		logger.debug("Jar/dir found: {}", filename);
 		for (String key : keys) {
 			if (filename.startsWith(key)) return true;
 		}
 		return false;
 	}
 }
