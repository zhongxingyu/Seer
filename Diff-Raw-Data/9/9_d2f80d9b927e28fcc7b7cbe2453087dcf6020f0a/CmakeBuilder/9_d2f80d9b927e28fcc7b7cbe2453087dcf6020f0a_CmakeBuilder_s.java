 package org.iainhull.ant;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.Project;
 import org.apache.tools.ant.Task;
 import org.apache.tools.ant.taskdefs.Execute;
 import org.apache.tools.ant.types.LogLevel;
 
 /**
  * Ant tasks to control CMake described builds.  It executes cmake and
  * then builds resulting makefiles/projects.
  * 
  * @author iain.hull
  */
 public class CmakeBuilder extends Task implements Params {
 	private static String CMAKE_COMMAND = "cmake";
 	private static String CMAKE_CACHE = "CMakeCache.txt";
 	private static File CURRENT_DIR = new File(".");
 
 	private Params params = new SimpleParams();
 	private File sourceDir = CURRENT_DIR;
 	private List<GeneratorRule> genRules = new ArrayList<GeneratorRule>();
 	private List<ReadVariable> readVars = new ArrayList<ReadVariable>();
 	private boolean cmakeonly = false;
 
 	/**
 	 * Create a new CmakeBuilder
 	 */
 	public CmakeBuilder() {
 		params.setBindir(CURRENT_DIR);
 	}
 	
 	
 	/**
 	 * Call cmake as instructed, then build the makefiles/projects.
 	 */
 	public void execute() {
 		String osName = getOperatingSystem();
 		String osArch = System.getProperty("os.arch");
 		String osVer = System.getProperty("os.version");
 	
 		log("OS name: " + osName, Project.MSG_VERBOSE);
 		log("OS arch: " + osArch, Project.MSG_VERBOSE);
 		log("OS version: " + osVer, Project.MSG_VERBOSE);
 	
 		for (GeneratorRule g : genRules) {
 			log("Generator: " + g + (g.matches(osName) ? " (match)" : ""),
 					Project.MSG_VERBOSE);
 		}
 		
 		GeneratorRule rule = getBestGenerator();
 		testPaths(rule);
 		
 		if(cmakeonly || !testBuildAlreadyExists(rule.getBindir())) {
 			executeCmake(rule);			
 		}
 
 		CacheVariables vars = readCacheVariables(rule.getBindir());
 
 		if (!cmakeonly) {
 			executeBuild(rule, vars);
 			// Incase variables change if cmake is run a second time
 			vars = readCacheVariables(rule.getBindir());
 		}
 		
 		processReadVars(vars);
 	}
 
 
 	/**
 	 * Returns true if cmake has already created the build files.
 	 * 
 	 * @param binaryDir the binary directory
 	 * @return true if cmake has already created the build files.
 	 */
 	private boolean testBuildAlreadyExists(File binaryDir) {
 		File cache = new File(binaryDir, CMAKE_CACHE);
 		return cache.exists() && cache.canRead();
 	}
 
 
 	/**
 	 * Get the cmake source directory, where CMakeLists.txt lives.
 	 *  
 	 * @return the source directory
 	 */
 	public File getSrcdir() {
 		return this.sourceDir;
 	}
 
 	/**
 	 * Set the cmake source directory, where CMakeLists.txt lives.
 	 *  
 	 * @param sourceDir the source directory
 	 */
 	public void setSrcdir(File sourceDir) {
 		this.sourceDir = sourceDir;
 	}
 
 	/**
 	 * Create and add a new GeneratorRule, these are used to decide which
 	 * generator cmake uses based on the host operating system.
 	 * 
 	 * @return the new GeneratorRule
 	 */
 	public GeneratorRule createGenerator() {
 		GeneratorRule g = new GeneratorRule(this);
 		genRules.add(g);
 		return g;
 	}
 
 
 	/** 
 	 * Create and add a new ReadVariable, these enable CmakeBuilder to
 	 * set the ant variables based on CMakeCache.txt entries.
 	 */
 	public ReadVariable createReadvar() {
 		ReadVariable v = new ReadVariable();
 		readVars.add(v);
 		return v;
 	}
 
 
 	public File getBindir() {
 		return this.params.getBindir();
 	}
 
 	public void setBindir(File binaryDir) {
 		this.params.setBindir(binaryDir);
 	}
 
 	public BuildType getBuildtype() {
 		return this.params.getBuildtype();
 	}
 
 	public void setBuildtype(BuildType buildType) {
 		this.params.setBuildtype(buildType);
 	}	
 	
 	public String getTarget() {
 		return params.getTarget();
 	}
 
 	public void setTarget(String target) {
 		params.setTarget(target);
 	}
 	
 	public boolean getCmakeonly() {
 		return this.cmakeonly;
 	}
 	
 	public void setCmakeonly(boolean cmakeonly) {
 		this.cmakeonly = cmakeonly;
 	}
 	
 	/** 
 	 * Create and add a new Variable, these enable CmakeBuilder to
 	 * set the ant variables based on CMakeCache.txt entries.
 	 */
 	public Variable createVariable() {
 		return params.createVariable();
 	}	
 	
 	public Map<String, Variable> getVariables() {
 		return params.getVariables();
 	}
 
 	private void executeCmake(GeneratorRule rule) {
 		List<String> commandLine = new ArrayList<String>();
 		commandLine.add(CMAKE_COMMAND);
 		if (rule.getName() != null) {
 			commandLine.add("-G");
 			commandLine.add(rule.getName());
 		}
 		
 		for(Variable v : rule.getVariables().values()) {
 			commandLine.add("-D");
 			commandLine.add(v.toString());
 		}
 
 		commandLine.add(sourceDir.toString());
 		
 		try {
 			log("Calling CMake");
 			log("Source Directory: " + sourceDir);
 			log("Binary Directory: " + rule.getBindir());
 			if (rule != null) {
 				log("Generator: " + rule);
 			}
 	
 			int ret = doExecute(commandLine.toArray(new String[0]), rule.getBindir());
 			if (ret != 0) {
 				throw new BuildException(CMAKE_COMMAND
 						+ " returned error code " + ret);
 			}
 		} catch (IOException e) {
 			log(e, LogLevel.ERR.getLevel());
 			throw new BuildException(e);
 		}
 	}
 
 	private void executeBuild(GeneratorRule rule, CacheVariables vars) {
 		try {
 			String makeCommand = vars.getVariable("CMAKE_BUILD_TOOL").getValue();
 			String cmakeGenerator = vars.getVariable("CMAKE_GENERATOR").getValue();
 	
 			log("Building cmake output");
 			int ret = doExecute(
 					BuildCommand.inferCommand(rule, makeCommand, cmakeGenerator), 
 					rule.getBindir());
 			
 			if (ret != 0) {
 				throw new BuildException(makeCommand + " returned error code "
 						+ ret);
 			}
 		} catch (IOException e) {
 			log(e, LogLevel.ERR.getLevel());
 			throw new BuildException(e);
 		}
 	
 	}
 
 	private void processReadVars(CacheVariables vars) {
 		for (ReadVariable prop : readVars) {
 			Variable v = vars.getVariable(prop.getName());
 			if (v != null) {
 				log("Setting property: " + prop.getProperty() + " to " + v.getValue(), Project.MSG_VERBOSE);
 				getProject().setNewProperty(prop.getProperty(), v.getValue());
 			}
 			else {
 				log("Cannot set property: " + prop.getProperty() + ", cmake variable not defined" + prop.getName(), Project.MSG_WARN);
 			}
 		}
 	}
 
 	private void testPaths(GeneratorRule rule) {
 		testSourceDir(sourceDir);
 		testBinaryDir(rule.getBindir());
 	}
 
 
 	private GeneratorRule getBestGenerator() {
 		for (GeneratorRule g : genRules) {
 			if (g.matches(getOperatingSystem())) {
 				return g;
 			}
 		}
 		return createGenerator();
 	}
 
 
 	/**
 	 * Read the CacheVariables generated by executing CMake
 	 * This is package private to override during testing.
 	 * 
 	 * @param binaryDir The directory to find the CMakeCache.txt
 	 * @return the CacheVariables generated by executing CMake.
 	 */
 	CacheVariables readCacheVariables(File binaryDir) {
 		File cache = new File(binaryDir, CMAKE_CACHE);
 		CacheVariables vars;
 		try {
 			vars = new CacheVariables(cache);
 		}
 		catch (IOException e) {
 			throw new BuildException("Cannot read cmake cache: " + cache);
 		}
 		return vars;
 	}
 
 
 	/**
 	 * Utility to execute an external command.
 	 * This is package private to override during testing.
 	 * 
 	 * @param commandLine 
 	 * 		array of command line arguments
 	 * @param workingDirectory 
 	 * 		the working directory the command is run from 
 	 * @return
 	 * 		the exit code of the command
 	 * @throws IOException
 	 * 		if the command results in an IOException
 	 */
 	int doExecute(String [] commandLine, File workingDirectory) throws IOException {
 		Execute exec = new Execute();
 		
 		exec.setWorkingDirectory(workingDirectory);
 		exec.setCommandline(commandLine);
 		exec.setVMLauncher(true);
 
 		StringBuilder commandText = new StringBuilder();
 		commandText.append(commandLine[0]);
 		for (int i = 1; i < commandLine.length; ++i) {
 			commandText.append(" ").append(commandLine[i]);
 		}
 		log("Executing " + commandText, Project.MSG_VERBOSE);
 		return exec.execute();
 	}
 	
 	/**
 	 * Test that the source directory is valid.
 	 * This is package private to override during testing.
 	 * 
 	 * @param sourceDir the directory to test
 	 */
 	void testSourceDir(File sourceDir) {
 		if (sourceDir == null) {
 			throw new BuildException("Source directory not set");
 		}
 
 		if (!sourceDir.exists()) {
 			throw new BuildException("Source directory does not exist: "
 					+ sourceDir);
 		}
 
 		if (!sourceDir.isDirectory()) {
 			throw new BuildException("Source directory is not a directory: "
 					+ sourceDir);
 		}		
 	}
 	
 	/**
 	 * Test that the binary directory is valid.
 	 * This is package private to override during testing.
 	 * 
 	 * @param binaryDir the directory to test
 	 */
 	void testBinaryDir(File binaryDir) {
 		if (binaryDir == null) {
 			throw new BuildException("Binary directory not set");
 		}
 
 		if (!binaryDir.exists()) {
 			log("Creating binary directory: " + binaryDir, Project.MSG_VERBOSE);			
 			binaryDir.mkdirs();
 			if (!binaryDir.exists()) {
 				throw new BuildException("Cannot create binary directory: "
 						+ binaryDir);
 			}
 		}
 		
 		if (!binaryDir.isDirectory()) {
 			throw new BuildException("Binary directory is not a directory: "
 					+ binaryDir);
 		}
 	}
 	
 	/**
 	 * Return the host operating system name.
 	 * This is package private to override during testing.
 	 * 
 	 * @return the host operating system name.
 	 */
 	String getOperatingSystem() {
 		return System.getProperty("os.name");
 	}
 }
