 package testutils;
 
 import jargs.gnu.CmdLineParser;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.security.CodeSource;
 import java.util.jar.JarFile;
 import java.util.zip.ZipEntry;
 
 import org.apache.commons.io.IOUtils;
 
 public class PTToJavaPackage {
 	private static final String GENERIC_BUILD_XML_LOCATION = "txt/generic-build.xml";
 	private String sourceFolderName;
 	private String outputFolderName;
 	private boolean verbose;
 	private FileIO sourceFolder;
 	private String[] inputfileNames;
 	private FileIO outputSrcFolder;
 	private CompileToPackage compilerInterface;
 	private FileIO outputBaseFolder;
 	private String genericBuildFile;
 
 	public static void main(String[] args) {
 		try {
 			PTToJavaPackage controller = parseArgsAndInstantiate(args);
 			controller.checkArgs();
 			controller.buildAST();
 			if (controller.noErrors()) {
 				controller.writePackages();
 				controller.writeBuildXML();
				System.out.println("Compilation completed. Java package(s) written to " + controller.outputFolderName);
 			} else {
 				controller.printErrorReport();
 			}
 		} catch (FatalErrorException e) {
 			System.out.println("Fatal error:");
 			System.out.println(e.getMessage());
 		}
 	}
 
 	private boolean noErrors() {
 		return compilerInterface.getErrorMsgs().isEmpty();
 	}
 
 	private void printErrorReport() {
 		StringBuilder sb = new StringBuilder();
 		for (String errorSource : compilerInterface.getSourceWithErrors()) {
 			sb.append(errorSource + "\n");
 		}
 		sb.append("Error: " + compilerInterface.getErrorMsgs());
 		error(sb.toString());
 	}
 
 	private void writeBuildXML() {
 		File jarPath = getJarPath();
 		genericBuildFile = readGenericBuildFile(jarPath);
 		FileIO buildFile = outputBaseFolder.createExtendedPath("build.xml");
 		String buildFileText = genericBuildFile.replaceFirst("PROJECTNAME",
 				outputBaseFolder.getName());
 		buildFile.write(buildFileText);
 	}
 
 	private String readGenericBuildFile(File jarPath) {
 		StringWriter writer = new StringWriter();
 		try {
 			JarFile jar = new JarFile(jarPath);
 			ZipEntry entry = jar.getEntry(GENERIC_BUILD_XML_LOCATION);
 			InputStream stream = jar.getInputStream(entry);
 			IOUtils.copy(stream, writer);
 		} catch (IOException e) {
 			error(String.format(
 					"Couldn't read file %s from jarfile at path: %s",
 					GENERIC_BUILD_XML_LOCATION, jarPath));
 		}
 		return writer.toString();
 	}
 
 	private void error(String message) {
 		throw new FatalErrorException(message);
 
 	}
 
 	private File getJarPath() {
 		CodeSource codeSource = PTToJavaPackage.class.getProtectionDomain()
 				.getCodeSource();
 		String jarPath = codeSource.getLocation().getPath();
 		return new File(jarPath);
 	}
 
 	/** TODO cleanup */
 	private void checkArgs() {
 		verbose("SourceFolderName: " + sourceFolderName);
 		verbose("OutputFolderName: " + outputFolderName);
 		if (sourceFolderName == null || outputFolderName == null)
 			error("Missing sourceFolder or outputFolder");
 		sourceFolder = new FileIO(sourceFolderName);
 		if (!sourceFolder.isDirectory())
 			error(String.format("Source folder directory '%s' not found.",
 					sourceFolderName));
 		inputfileNames = sourceFolder.getFilePaths("java");
 		outputBaseFolder = new FileIO(outputFolderName);
 		outputSrcFolder = outputBaseFolder.createExtendedPath("src");
 		if (outputSrcFolder.exists()) {
 			if (!outputSrcFolder.isDirectory())
 				throw new IllegalArgumentException(String.format(
 						"Destination path '%s' exists and is not a directory.",
 						outputFolderName));
 		} else {
 			boolean outputFolderCreated = outputSrcFolder.mkdirs();
 			if (!outputFolderCreated)
 				error(String
 						.format(
 								"Destination path '%s' did not exist and couldn't be created.",
 								outputFolderName));
 		}
 		if (!outputSrcFolder.exists())
 			outputSrcFolder.mkdir();
 		verbose("Printing all [" + inputfileNames.length + "] inputfilenames.");
 		for (String filename : inputfileNames)
 			verbose("\tinputfilename: " + filename);
 	}
 
 	private void buildAST() {
 		compilerInterface = new CompileToPackage(inputfileNames);
 		boolean result = compilerInterface.process();
 		verbose("Compilation done " + (result ? "without" : "with")
 				+ " errors.");
 	}
 
 	private void writePackages() {
 		verbose("Writing packages to disk");
 		for (String packageName : compilerInterface.getPackageNames()) {
 			FileIO packageFolder = outputSrcFolder
 					.createExtendedPath(packageName);
 			packageFolder.mkdir();
 			verbose(String
 					.format("\tWriting package [%s] to disk", packageName));
 			for (String classname : compilerInterface
 					.getClassnames(packageName)) {
 				verbose(String
 						.format("\tWriting class [%s] to disk", classname));
 				FileIO classFile = packageFolder.createExtendedPath(classname
 						+ ".java");
 				String source = String.format("package %s;\n\n", packageName);
 				source += compilerInterface
 						.getClassData(packageName, classname);
 				source += "\n";
 				classFile.write(source);
 			}
 		}
 	}
 
 	private void verbose(String string) {
 		if (verbose)
 			System.out.println("Verbose: " + string);
 	}
 
 	private static PTToJavaPackage parseArgsAndInstantiate(String[] args) {
 		CmdLineParser parser = new CmdLineParser();
 		CmdLineParser.Option verboseOption = parser.addBooleanOption('v',
 				"verbose");
 		CmdLineParser.Option outputFolderOption = parser.addStringOption('o',
 				"outputFolder");
 		CmdLineParser.Option genericBuildXMLPathOpt = parser.addStringOption(
 				'p', "buildXMLPath");
 
 		try {
 			parser.parse(args);
 		} catch (Exception e) {
 			System.err.println("Unknown args: " + e.getMessage());
 			System.exit(1);
 		}
 		boolean verbose = (Boolean) parser.getOptionValue(verboseOption,
 				Boolean.FALSE);
 
 		String[] remainingArgs = parser.getRemainingArgs();
 		if (remainingArgs.length == 0)
 			throw new FatalErrorException("couldn't find an input file.");
		String sourceFolder = new File(remainingArgs[0]).getPath();
 		String outputFolder = (String) parser.getOptionValue(
 				outputFolderOption, sourceFolder + "_output");
 		PTToJavaPackage controller = new PTToJavaPackage(sourceFolder,
 				outputFolder, verbose);
 		controller.verbose("Verbose flag turned on.");
 		return controller;
 	}
 
 	public PTToJavaPackage(String sourceFolder, String outputFolder,
 			boolean verbose) {
 		super();
 		this.sourceFolderName = sourceFolder;
 		this.outputFolderName = outputFolder;
 		this.verbose = verbose;
 	}
 }
