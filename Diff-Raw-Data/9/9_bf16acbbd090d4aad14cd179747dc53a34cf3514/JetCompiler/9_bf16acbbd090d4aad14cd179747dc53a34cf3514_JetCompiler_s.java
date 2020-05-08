 /**
  * 
  */
 package net.frontlinesms.build.jet.compile;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map;
 
 import net.frontlinesms.build.jet.FileUtils;
 import net.frontlinesms.build.jet.ProcessStreamPrinter;
 import net.frontlinesms.build.jet.PropertyUtils;
 
 /**
  * Compiles a jet package from java.
  * @author Alex Anderson <alex@frontlinesms.com>
  */
 public class JetCompiler {
 	
 //> CONFIGURATION PROPERTY KEYS
 	private static final String CONF_PROP_COMPILE_EXECUTABLE = "compiler.path";
 	/** File encoding used for .prj files. */
 	private static final String PRJ_FILE_ENCODING = "UTF-8";
 	
 //> INSTANCE VARIABLES
 	/** Indicates whether this instance has been configured yet. */
 	private boolean configured;
 	/** The working directory for the packager */
 	private final File workingDirectory;
 	/** The path to the package executable */
 	private String compileExecutable;
 	
 //> CONSTRUCTORS
 	public JetCompiler(File workingDirectory) {
 		this.workingDirectory = workingDirectory;
 	}
 	
 //> INSTANCE METHODS
 	public int doCompile(JetCompileProfile compileProfile) throws IOException {
 		assert(configured) : "This packager is not configured yet.";
 		
 		// Load the template.prj file from the classpath into memory
 		String[] dotPrj = FileUtils.readFileFromClasspath(this.getClass(), "template.prj", PRJ_FILE_ENCODING);
 		
 		// TODO append !module command for jars to .prj  N.B. Recently I am not sure this is necessary as we are
 		// using !batch *.jar etc. to define java classes
 		
 		// Filter the properties in template.prj
 		PropertyUtils.subProperties(dotPrj, compileProfile.getSubstitutionProperties());
 		
 		// write .prj file to the temp working directory
 		FileUtils.writeFile(getPrjFile(), PRJ_FILE_ENCODING, dotPrj);
 		
 		// TODO Copy code to the classpath directory.  Currently this is manually copied in
 		
 		// Execute the build
 		Process buildProcess = Runtime.getRuntime().exec(getCompileCommand(), null, this.workingDirectory);
 		ProcessStreamPrinter printer = ProcessStreamPrinter.createStandardPrinter("compile", buildProcess);
 		int buildStatus = printer.startBlocking();
 		
 		return buildStatus;
 	}
 	
 	private File getPrjFile() {
		return new File(this.workingDirectory, "output.prj");
 	}
 	
 	private String getCompileCommand() {
 		String executable;
 		if(this.compileExecutable != null && this.compileExecutable.length() > 0) {
 			executable = this.compileExecutable + "/jc";
 		} else {
 			executable = "jc";
 		}
 		return executable
 				+ " =p " + getPrjFile();
 	}
 
 	/** Calls {@link #configure(Map)} with the contents of the supplied config file. */
 	private void configure(String confPath) throws IOException {
 		Map<String, String> props = PropertyUtils.loadProperties(new File(confPath));
 		configure(props);
 	}
 	
 	/** Configures the {@link JetCompiler} itself.  This is basically environment
 	 * variables, working directory etc. */
 	private void configure(Map<String, String> props) {
 		assert(!this.configured) : "This should not be configured more than once.";
 		
 		this.compileExecutable = props.get(CONF_PROP_COMPILE_EXECUTABLE);
 		assert(this.compileExecutable!=null) : "No compile executable was specified.  Should be set with key: " + CONF_PROP_COMPILE_EXECUTABLE;
 		
 		this.configured = true;
 	}
 	
 	/** Call {@link #configure(Map)} with default settings. */
 	public void configureDefaults() {
 		Map<String, String> defaultConfiguration = new HashMap<String, String>();
 		defaultConfiguration.put(CONF_PROP_COMPILE_EXECUTABLE, "");
 		configure(defaultConfiguration);
 	}
 
 	public static void main(String[] args) throws IOException {
 		assert(args.length > 3) : "Not enough args.";
 		String workingDirectoryRoot = args[0];
 		// Get the working directory as an absolute location
 		File workingDirectory = new File(workingDirectoryRoot, "compile").getAbsoluteFile();
 		if(!workingDirectory.exists()) {
 			if(!workingDirectory.mkdirs()) throw new FileNotFoundException("Working directory could not be created at " + workingDirectory.getAbsolutePath()); 
 		}
 		
 		String packagerConfigFilePath = args[1];
 		File profileRootDirectory = new File(args[2]); 
 		String profileName = args[3];
 		
 		System.out.println("Starting...");
 		
 		// configure the JetPackage
 		JetCompiler compiler = new JetCompiler(workingDirectory);
 		compiler.configure(packagerConfigFilePath);
 		JetCompileProfile compileProfile = JetCompileProfile.loadFromDirectory(
 				new File(profileRootDirectory, profileName),
 				workingDirectory);
 		compiler.doCompile(compileProfile);
 		
 		System.out.println("...completed.");
 	}
 }
