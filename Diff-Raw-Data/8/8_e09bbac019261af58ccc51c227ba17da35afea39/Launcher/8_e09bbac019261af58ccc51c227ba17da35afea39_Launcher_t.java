 /**
  * Copyright (C) 2013 Schneider-Electric
  *
  * This file is part of "Mind Compiler" is free software: you can redistribute 
  * it and/or modify it under the terms of the GNU Lesser General Public License 
  * as published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful, but WITHOUT 
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more
  * details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  *
  * Contact: mind@ow2.org
  *
  * Authors: Stephane Seyvoz
  * Contributors: 
  */
 
 package org.ow2.mind.unit;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.objectweb.fractal.adl.ADLException;
 import org.objectweb.fractal.adl.CompilerError;
 import org.objectweb.fractal.adl.Definition;
 import org.objectweb.fractal.adl.Loader;
 import org.objectweb.fractal.adl.NodeFactory;
 import org.objectweb.fractal.adl.error.Error;
 import org.objectweb.fractal.adl.error.GenericErrors;
 import org.objectweb.fractal.adl.interfaces.Interface;
 import org.objectweb.fractal.adl.interfaces.InterfaceContainer;
 import org.objectweb.fractal.adl.types.TypeInterface;
 import org.objectweb.fractal.adl.util.FractalADLLogManager;
 import org.ow2.mind.ADLCompiler;
 import org.ow2.mind.ADLCompiler.CompilationStage;
 import org.ow2.mind.adl.ast.ASTHelper;
 import org.ow2.mind.adl.ast.Component;
 import org.ow2.mind.adl.ast.ComponentContainer;
 import org.ow2.mind.adl.ast.DefinitionReference;
 import org.ow2.mind.adl.ast.ImplementationContainer;
 import org.ow2.mind.adl.ast.Source;
 import org.ow2.mind.annotation.AnnotationHelper;
 import org.ow2.mind.cli.CmdFlag;
 import org.ow2.mind.cli.CmdOption;
 import org.ow2.mind.cli.CmdOptionBooleanEvaluator;
 import org.ow2.mind.cli.CommandLine;
 import org.ow2.mind.cli.CommandLineOptionExtensionHelper;
 import org.ow2.mind.cli.CommandOptionHandler;
 import org.ow2.mind.cli.InvalidCommandLineException;
 import org.ow2.mind.cli.Options;
 import org.ow2.mind.cli.OutPathOptionHandler;
 import org.ow2.mind.cli.PrintStackTraceOptionHandler;
 import org.ow2.mind.cli.SrcPathOptionHandler;
 import org.ow2.mind.cli.StageOptionHandler;
 import org.ow2.mind.error.ErrorManager;
 import org.ow2.mind.idl.IDLLoader;
 import org.ow2.mind.idl.ast.IDL;
 import org.ow2.mind.idl.ast.InterfaceDefinition;
 import org.ow2.mind.idl.ast.Method;
 import org.ow2.mind.idl.ast.Parameter;
 import org.ow2.mind.idl.ast.PrimitiveType;
 import org.ow2.mind.idl.ast.Type;
 import org.ow2.mind.inject.GuiceModuleExtensionHelper;
 import org.ow2.mind.io.OutputFileLocator;
 import org.ow2.mind.plugin.PluginLoaderModule;
 import org.ow2.mind.plugin.PluginManager;
 import org.ow2.mind.unit.annotations.Cleanup;
 import org.ow2.mind.unit.annotations.Init;
 import org.ow2.mind.unit.annotations.Test;
 import org.ow2.mind.unit.annotations.TestSuite;
 import org.ow2.mind.unit.cli.CUnitModeOptionHandler;
 import org.ow2.mind.unit.model.Suite;
 import org.ow2.mind.unit.model.TestCase;
 import org.ow2.mind.unit.model.TestInfo;
 import org.ow2.mind.unit.st.BasicSuiteSourceGenerator;
 import org.ow2.mind.unit.st.SuiteSourceGenerator;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 
 public class Launcher {
 
 	protected static final String PROGRAM_NAME_PROPERTY_NAME = "mindunit.launcher.name";
 	protected static final String ID_PREFIX                  = "org.ow2.mind.unit.test.";
 
 	protected final CmdFlag 		helpOpt					= new CmdFlag(
 			ID_PREFIX
 			+ "Help",
 			"h", "help",
 			"Print this help and exit");
 
 	protected final CmdFlag 		versionOpt				= new CmdFlag(
 			ID_PREFIX
 			+ "Version",
 			"v", "version",
 			"Print version number and exit");
 
 	protected final CmdFlag 		extensionPointsListOpt	= new CmdFlag(
 			ID_PREFIX
 			+ "PrintExtensionPoints",
 			null,
 			"extension-points",
 			"Print the list of available extension points and exit.");
 
 	protected final Options 		options					= new Options();
 
 	protected static Logger			logger					= FractalADLLogManager.getLogger("mindunit");
 
 	protected List<File>			validTestFoldersList 	= new ArrayList<File>();
 	protected List<URL>				urlList 				= new ArrayList<URL>();
 
 	protected List<String>			testFolderADLList 		= new ArrayList<String>();
 
 	protected List<Definition>		validTestsList 			= new ArrayList<Definition>();
 	protected List<Suite>			testSuites 				= new ArrayList<Suite>();
 
 	protected Map<Object, Object> 	compilerContext			= new HashMap<Object, Object>();
 
 	protected final String 			adlName 				= "org.ow2.mind.unit.MindUnitApplication";
 
 	// compiler components :
 	protected Injector 				injector;
 
 	protected ErrorManager 			errorManager;
 	protected ADLCompiler 			adlCompiler;
 
 	protected NodeFactory			nodeFactory;
 	protected SuiteSourceGenerator 	suiteCSrcGenerator;
 	protected Loader 				loaderItf;
 	protected IDLLoader 			idlLoaderItf;
 	protected OutputFileLocator 	outputFileLocatorItf;
 
 	protected void init(final String... args) throws InvalidCommandLineException {
 
 		List<String> 	testFoldersList;
 
 		if (logger.isLoggable(Level.CONFIG)) {
 			for (final String arg : args) {
 				logger.config("[arg] " + arg);
 			}
 		}
 
 		/****** Initialization of the PluginManager Component *******/
 
 		final Injector bootStrapPluginManagerInjector = getBootstrapInjector();
 		final PluginManager pluginManager = bootStrapPluginManagerInjector
 				.getInstance(PluginManager.class);
 
 		addOptions(pluginManager);
 
 		// parse arguments to a CommandLine.
 		final CommandLine cmdLine = CommandLine.parseArgs(options, false, args);
 		checkExclusiveGroups(pluginManager, cmdLine);
 		compilerContext
 		.put(CmdOptionBooleanEvaluator.CMD_LINE_CONTEXT_KEY, cmdLine);
 		invokeOptionHandlers(pluginManager, cmdLine, compilerContext);
 
 		// If help is asked, print it and exit.
 		if (helpOpt.isPresent(cmdLine)) {
 			printHelp(System.out);
 			System.exit(0);
 		}
 
 		// If version is asked, print it and exit.
 		if (versionOpt.isPresent(cmdLine)) {
 			printVersion(System.out);
 			System.exit(0);
 		}
 
 		// If the extension points list is asked, print it and exit.
 		if (extensionPointsListOpt.isPresent(cmdLine)) {
 			printExtensionPoints(pluginManager, System.out);
 			System.exit(0);
 		}
 
 		// get the test folders list
 		testFoldersList = cmdLine.getArguments();
 
 		if (!testFoldersList.isEmpty()) {
 			for (String testFolder : testFoldersList) {
 				final File testDirectory = new File(testFolder);
 				if (!testDirectory.isDirectory() || !testDirectory.canRead()) {
 					logger.severe(String.format("Cannot read source path '%s' - skipped.",
 							testDirectory.getPath()));
 				} else {
 					validTestFoldersList.add(testDirectory);
 					try {
 						URL testDirURL = testDirectory.toURI().toURL();
 						urlList.add(testDirURL);
 					} catch (final MalformedURLException e) {
 						// will never happen since we already checked File
 					}
 				}
 			}
 		} else {
 			logger.severe("You must specify a source path.");
 			printHelp(System.out);
 			System.exit(1);
 		}
 
 		// also add the URL to the generated files folder
 		File outDir = OutPathOptionHandler.getOutPath(compilerContext);
 		File genFilesDir = new File(outDir.getAbsolutePath(), "unit-gen");
 		while (!genFilesDir.exists())
 			genFilesDir.mkdirs();
 		try {
 			urlList.add(genFilesDir.toURI().toURL());
 		} catch (MalformedURLException e2) {
 			logger.severe("Could not access to " + outDir.getPath() + "/" + "unit-gen" + " file generation path !");
 		}
 
 		// add the computed test folders to the src-path
 		addTestFoldersToPath();
 
 		// initialize compiler
 		initInjector(pluginManager, compilerContext);
 
 		initCompiler();
 
 		// Build list of ADL files
 		listTestFolderADLs();
 
 		/*
 		 * Then parse/load them (but stay at CHECK_ADL stage).
 		 * We obtain a list of Definition-s
 		 */
 		for (String currentADL : testFolderADLList) {
 			List<Object> l;
 			try {
 				l = adlCompiler.compile(currentADL, "", CompilationStage.CHECK_ADL, compilerContext);
 				if (l != null && !l.isEmpty()) {
 					for (Object currObj : l) {
 						if (!(currObj instanceof Definition))
 							// error case that should never happen
 							logger.warning("Encountered object \"" + currObj.toString() + "\" while handling " + currentADL + " isn't a definition !");
 						else {
 							// we've got a definition
 							Definition currDef = (Definition) currObj;
							// Then keep only if annotated with @TestSuite
 							if (AnnotationHelper.getAnnotation(currDef, TestSuite.class) != null) {
 								validTestsList.add(currDef);
								logger.info("@TestSuite found: " + currDef.getName());
 							}
 						}
 					}
 				} else logger.info(currentADL + " definition load failed, invalid ADL");
 			} catch (ADLException e) {
 				logger.info(currentADL + " definition load failed, invalid ADL");
 			} catch (InterruptedException e) {
 				logger.info(currentADL + " definition load failed, thread was interrupted ! detailed error below: ");
 				e.printStackTrace();
 			}
 		}
 
 		if (validTestsList.isEmpty()) {
			logger.info("No @TestSuite found: exit");
 			System.exit(0);
 		}
 
 		/*
 		 * TODO: For all test cases get their "TestEntry" interface instance name 
 		 */
 
 		/*
 		 * Get the test container 
 		 */
 		Definition containerDef = null;
 		String testContainerName = "org.ow2.mind.unit.MindUnitContainer";
 
 		try {
 			List<Object> containerDefs = adlCompiler.compile(testContainerName, "", CompilationStage.CHECK_ADL, compilerContext);
 			if (containerDefs == null) {
 				logger.severe("Test container could not be loaded - Check availability of the MindUnit components in your runtime folder/source-path !");
 				System.exit(1);
 			}
 
 			// should be size 1 and of course a definition... why would it be different ?
 			if (containerDefs.size() > 1) 
 				logger.warning("Container loading returned multiple definitions - Using only the first one");
 
 			Object containerObj = containerDefs.get(0);
 			if (!(containerObj instanceof Definition)) {
 				logger.severe("Container type wasn't a definition or could not be loaded");
 				System.exit(1);
 			}
 
 			containerDef = (Definition) containerDefs.get(0);
 
 			if (!ASTHelper.isComposite(containerDef)) {
 				logger.severe("Container wasn't a composite ! Please be serious.");
 				System.exit(1);
 			}
 
 		} catch (ADLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		/*
 		 * Add all test cases to the test container as sub-component instances
 		 * TODO: FIXME: if the application was already compiled and the container re-loaded pre-filled,
 		 * we obtain duplicates of sub-components, and that leads to compilation errors !
 		 * Idea: force regen of container ? check its sub-components ?
 		 */
 		logger.info("Adding TestCases to the MindUnit container");
 		for (Definition currTestDef : validTestsList) {
 			DefinitionReference currTestDefRef = ASTHelper.newDefinitionReference(nodeFactory, currTestDef.getName());
 			Component currComp = ASTHelper.newComponent(nodeFactory, currTestDef.getName().replace(".", "_") + "Instance", currTestDefRef);
 			ASTHelper.setResolvedComponentDefinition(currComp, currTestDef);
 			((ComponentContainer) containerDef).addComponent(currComp);
 		}
 
 		/*
 		 * Load the application
 		 */
 		logger.info("Preparing the host application");
 
 		List<Object> loadedDefs = null;
 		String rootAdlName = adlName + "<";
 
 		if (((String) compilerContext.get(CUnitModeOptionHandler.CUNITMODE_CONTEXT_KEY)).equals("console")) {
 			logger.info("Loading container in Console mode");
 			rootAdlName += "org.ow2.mind.unit.MindUnitConsole";
 		} else {
 			logger.info("Loading container in Automated mode");
 			rootAdlName += "org.ow2.mind.unit.MindUnitAutomated";
 		}
 
 		rootAdlName += ">";
 
 		try {
 			loadedDefs = adlCompiler.compile(rootAdlName, "", CompilationStage.CHECK_ADL, compilerContext);
 		} catch (ADLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		/*
 		 * Write the C implementation of the "Suite" component with the good
 		 * struct referencing all the TestEntries ("run" method) previously found.
 		 * Needs to write an according StringTemplate. If we made all TestCases as
 		 * @Singleton, we could reference CALLs (no "this") and create according bindings ?
 		 * A bit heavy but more architecture-friendly. And may simplify function names calculations.
 		 */
 		logger.info("Generating Suite source implementation");
 
 		// prepare test cases list
 		List<TestCase> currItfValidTestCases = null;
 		TestInfo currTestInfo = null;
 
 
 		// build the Suite list
 		for (Definition currDef : validTestsList) {
 			String description = (AnnotationHelper.getAnnotation(currDef, TestSuite.class)).value;
 			if (description == null)
 				description = currDef.getName();
 
 			if (currDef instanceof ComponentContainer) {
 				// handle all sub-components (no recursion)
 				ComponentContainer currCompContainer = (ComponentContainer) currDef;
 				for (Component currComp : currCompContainer.getComponents()) {
 					try {
 						Definition currCompDef = ASTHelper.getResolvedComponentDefinition(currComp, loaderItf, compilerContext);
 						if (currCompDef instanceof InterfaceContainer) {
 							// handle sub-component server interfaces, find the list of @Test-annotated methods
 							InterfaceContainer currItfContainer = (InterfaceContainer) currCompDef;
 							
 							for (Interface currItf : currItfContainer.getInterfaces()) { // start for all interfaces
 								
 								// re-init at every loop
 								String currItfInitFuncName = null;
 								String currItfCleanupFuncName = null;
 								currItfValidTestCases = new ArrayList<TestCase>();
 								
 								// should be everywhere isn't it ?
 								assert currItf instanceof TypeInterface;
 								TypeInterface currTypeItf = (TypeInterface) currItf;
 								
 								// we only are concerned by server interfaces
 								if (currTypeItf.getRole().equals(TypeInterface.SERVER_ROLE)) {
 									
 									String itfSignature = currTypeItf.getSignature();
 									// now get the itf methods
 									IDL currIDL = idlLoaderItf.load(itfSignature, compilerContext);
 									assert currIDL instanceof InterfaceDefinition;
 									InterfaceDefinition currItfDef = (InterfaceDefinition) currIDL;
 									for (Method currMethod : currItfDef.getMethods()) {
 										boolean isTest 		= AnnotationHelper.getAnnotation(currMethod, Test.class) != null;
 										boolean isInit 		= AnnotationHelper.getAnnotation(currMethod, Init.class) != null;
 										boolean isCleanup 	= AnnotationHelper.getAnnotation(currMethod, Cleanup.class) != null;
 
 										// Maybe replace the algorithm for a switch-case ?
 										
 										// ^ = XOR
 										if (isTest ^ isInit ^ isCleanup) {
 
 											if (isTest) {
 												Test testCase = AnnotationHelper.getAnnotation(currMethod, Test.class);
 
 												String testDescription = null;
 												if (testCase.value == null)
 													testDescription = currMethod.getName();
 												else
 													testDescription = testCase.value;
 
 												// return type should be void
 												Type methodType = currMethod.getType();
 												if (!(methodType instanceof PrimitiveType
 														&& ((PrimitiveType) methodType).getName().equals("void"))) {
 													logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Test method return type should be \"void\" - Adding to test list anyway");
 												}
 
 												// argument must be void ( = no argument)
 												Parameter[] methodParams = currMethod.getParameters();
 												if (methodParams.length > 0) {
 													logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Test method arguments must be \"(void)\" - Skipping method");
 													continue;
 												}
 
 												// FIXME: maybe calculate the method name in a more elegant way...
 												String cMethodName = "__component_" + currCompDef.getName().replace(".", "_")
 														+ "_" + currTypeItf.getName() + "_" + currMethod.getName();
 
 												currItfValidTestCases.add(new TestCase(testDescription, cMethodName));
 											} else if (isInit) {
 												if (currItfInitFuncName != null) {
 													logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": An @Init method was already defined - Skipping");
 													continue;
 												}
 												// FIXME: maybe calculate the method name in a more elegant way...
 												currItfInitFuncName = "__component_" + currCompDef.getName().replace(".", "_")
 														+ "_" + currTypeItf.getName() + "_" + currMethod.getName();
 												
 												// return type should be void
 												Type methodType = currMethod.getType();
 												if (!(methodType instanceof PrimitiveType
 														&& ((PrimitiveType) methodType).getName().equals("int"))) {
 													logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Test method return type should be \"int\" - Adding to test list anyway");
 												}
 
 												// argument must be void ( = no argument)
 												Parameter[] methodParams = currMethod.getParameters();
 												if (methodParams.length > 0) {
 													logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Test method arguments must be \"(void)\" - Skipping method");
 													continue;
 												}
 												
 											} else if (isCleanup) {
 												if (currItfCleanupFuncName != null) {
 													logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": An @Init method was already defined - Skipping");
 													continue;
 												}
 												// FIXME: maybe calculate the method name in a more elegant way...
 												currItfCleanupFuncName = "__component_" + currCompDef.getName().replace(".", "_")
 														+ "_" + currTypeItf.getName() + "_" + currMethod.getName();
 												
 												// return type should be void
 												Type methodType = currMethod.getType();
 												if (!(methodType instanceof PrimitiveType
 														&& ((PrimitiveType) methodType).getName().equals("int"))) {
 													logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Test method return type should be \"int\" - Adding to test list anyway");
 												}
 
 												// argument must be void ( = no argument)
 												Parameter[] methodParams = currMethod.getParameters();
 												if (methodParams.length > 0) {
 													logger.warning("While handling " + currItfDef.getName() + "#" + currMethod.getName() + ": @Test method arguments must be \"(void)\" - Skipping method");
 													continue;
 												}
 											}
 										} else if (isTest || isInit || isCleanup) {
 											// if the XOR failed and there was at least one annotation it means we had 2 or more... 
 											// and we didn't want to raise an error when there was no annotation at all
 											logger.warning("@Init, @Test and @Cleanup are mutually exclusive - Please clarify " + currItfDef.getName() + "#" + currMethod.getName() + " role - Skipping method");
 											continue;
 										}
 									}
 								} // end if itf is server
 								
 								// build the test suite
 								if (!currItfValidTestCases.isEmpty()) {
 									if (!ASTHelper.isSingleton(currCompDef)) {
 										logger.warning("Component " + currCompDef.getName() + " must be @Singleton to host @Test-s - skip !");
 										break;
 									}
 									
 									currTestInfo = new TestInfo(currTypeItf.getName(), currItfValidTestCases);
 									testSuites.add(new Suite(description + " - " + currTypeItf.getName(), currItfInitFuncName, currItfCleanupFuncName, currTestInfo));
 								}
 								
 							} // end for all interfaces
 						}
 					} catch (ADLException e) {
 						logger.severe("Could not resolve definition of " + currDef.getName() + "." + currComp.getName() + " ! - skip");
 						continue;
 					}
 				}
 			}
 		}
 		try {
 			suiteCSrcGenerator.visit(testSuites, compilerContext);
 		} catch (ADLException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 
 		logger.info("Adding the Suite to the test container");
 
 		/*
 		 * Create a primitive with the source file as implementation
 		 */
 		Definition mindUnitSuiteDefinition = ASTHelper.newPrimitiveDefinitionNode(nodeFactory, "MindUnitSuiteDefinition", (DefinitionReference[]) null);
 		// the newPrimitiveDefinitionNode enforces ImplementationContainer.class compatibility
 		ImplementationContainer mindUnitSuiteDefAsImplCtr = (ImplementationContainer) mindUnitSuiteDefinition;
 		Source mindUnitSuiteSource = ASTHelper.newSource(nodeFactory);
 		mindUnitSuiteSource.setPath(BasicSuiteSourceGenerator.getSuiteFileName());
 		mindUnitSuiteDefAsImplCtr.addSource(mindUnitSuiteSource);
 
 		/*
 		 *Then add the "Suite" component to the test container
 		 */
 		DefinitionReference mindUnitSuiteDefRef = ASTHelper.newDefinitionReference(nodeFactory, mindUnitSuiteDefinition.getName());
 		Component mindUnitSuiteComp = ASTHelper.newComponent(nodeFactory, mindUnitSuiteDefRef.getName().replace(".", "_") + "Instance", mindUnitSuiteDefRef);
 		ASTHelper.setResolvedComponentDefinition(mindUnitSuiteComp, mindUnitSuiteDefinition);
 		((ComponentContainer) containerDef).addComponent(mindUnitSuiteComp);
 
 		/*
 		 * Then compile
 		 */
 		logger.info("Launching executable compilation");
 		try {
 			loadedDefs = adlCompiler.compile(rootAdlName, "mindUnitOutput", CompilationStage.COMPILE_EXE, compilerContext);
 		} catch (ADLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		/*
 		 * TODO: Then run the built executable
 		 */
 
 	}
 
 	/**
 	 * Inspired from org.ow2.mind.cli.SrcPathOptionHandler.
 	 * We extend the original ClassLoader by using it as a parent to a new ClassLoader.
 	 * Valid test folders are taken from this class urlList attribute.
 	 * We also extend the ClassLoader to the current jar in order to use local (hidden)
 	 * resources.
 	 */
 	protected void addTestFoldersToPath() {
 		// get the --src-path elements
 		URLClassLoader srcPathClassLoader = (URLClassLoader) SrcPathOptionHandler.getSourceClassLoader(compilerContext);
 
 		// URL array of test path, replace the original source class-loader with our enriched one
 		// and use the original source class-loader as parent so as to keep everything intact
 		ClassLoader srcAndTestPathClassLoader = new URLClassLoader(urlList.toArray(new URL[0]), srcPathClassLoader);
 
 		// replace the original source classloader with the new one in the context
 		compilerContext.remove("classloader");
 		compilerContext.put("classloader", srcAndTestPathClassLoader);
 	}
 
 	/**
 	 * Inspired by Mindoc's DocumentationIndexGenerator.
 	 * @throws IOException
 	 */
 	protected void listTestFolderADLs() {
 		for (final File directory : validTestFoldersList) {
 			try {
 				exploreDirectory(directory.getCanonicalFile(), null);
 			} catch (IOException e) {
 				logger.severe(String.format("Cannot find directory '%s' - skipped.",
 						directory.getPath()));
 			}
 		}
 	}
 
 	/**
 	 * Inspired by Mindoc's DocumentationIndexGenerator.
 	 * @throws IOException
 	 */
 	private void exploreDirectory(final File directory, String currentPackage) {
 		if(directory.isHidden()) return;
 
 		String subPackage = "";
 
 		for (final File file : directory.listFiles((FileFilter) FileFilterUtils.suffixFileFilter(".adl"))) {
 			String compName = file.getName();
 			// remove ".adl" extension
 			compName = compName.substring(0, compName.length() - 4);
 			// add package
 			compName = currentPackage + "." + compName;
 
 			// save component info
 			testFolderADLList.add(compName);
 		}
 
 		for (final File subDirectory: directory.listFiles(
 				new FileFilter() {
 					public boolean accept(final File pathname) {
 						return pathname.isDirectory();
 					}
 				})) {
 
 			// base folder
 			if (currentPackage == null)
 				subPackage = subDirectory.getName();
 			else // already in sub-folder
 				subPackage = currentPackage + "." + subDirectory.getName();
 
 			// recursion
 			exploreDirectory(subDirectory, subPackage);
 		}
 	}
 
 	protected Injector getBootstrapInjector() {
 		return Guice.createInjector(new PluginLoaderModule());
 	}
 
 	protected void initCompiler() {
 		errorManager = injector.getInstance(ErrorManager.class);
 		adlCompiler = injector.getInstance(ADLCompiler.class);
 
 		// TODO: check if cleanup/moving to another class is needed ?
 		nodeFactory = injector.getInstance(NodeFactory.class);
 		suiteCSrcGenerator = injector.getInstance(SuiteSourceGenerator.class);
 		loaderItf = injector.getInstance(Loader.class);
 		idlLoaderItf = injector.getInstance(IDLLoader.class);
 		outputFileLocatorItf = injector.getInstance(OutputFileLocator.class);
 	}
 
 	protected void initInjector(final PluginManager pluginManager,
 			final Map<Object, Object> compilerContext) {
 		injector = Guice.createInjector(GuiceModuleExtensionHelper.getModules(
 				pluginManager, compilerContext));
 	}
 
 	public List<Object> compile(final List<Error> errors,
 			final List<Error> warnings) throws InvalidCommandLineException {
 
 		final List<Object> result = new ArrayList<Object>();
 		try {
 			final HashMap<Object, Object> contextMap = new HashMap<Object, Object>(
 					compilerContext);
 			final String adlName = ""; // TODO
 			final String execName = "generated_mindunit_app"; // TODO
 
 			final List<Object> l = adlCompiler.compile(adlName, execName,
 					StageOptionHandler.getCompilationStage(contextMap), contextMap);
 
 			if (l != null) result.addAll(l);
 
 		} catch (final InterruptedException e1) {
 			throw new CompilerError(GenericErrors.INTERNAL_ERROR, "Interrupted while executing compilation tasks");
 		} catch (final ADLException e1) {
 			if (!errorManager.getErrors().contains(e1.getError())) {
 				// the error has not been logged in the error manager, print it.
 				try {
 					errorManager.logError(e1.getError());
 				} catch (final ADLException e2) {
 					// ignore
 				}
 			}
 		}
 		if (errors != null) errors.addAll(errorManager.getErrors());
 		if (warnings != null) warnings.addAll(errorManager.getWarnings());
 		return result;
 	}
 
 	protected void addOptions(final PluginManager pluginManagerItf) {
 		options.addOptions(helpOpt, versionOpt, extensionPointsListOpt);
 
 		options.addOptions(CommandLineOptionExtensionHelper
 				.getCommandOptions(pluginManagerItf));
 	}
 
 	protected void checkExclusiveGroups(final PluginManager pluginManagerItf,
 			final CommandLine cmdLine) throws InvalidCommandLineException {
 		final Collection<Set<String>> exclusiveGroups = CommandLineOptionExtensionHelper
 				.getExclusiveGroups(pluginManagerItf);
 		for (final Set<String> exclusiveGroup : exclusiveGroups) {
 			CmdOption opt = null;
 			for (final String id : exclusiveGroup) {
 				final CmdOption opt1 = cmdLine.getOptions().getById(id);
 				if (opt1.isPresent(cmdLine)) {
 					if (opt != null) {
 						throw new InvalidCommandLineException("Options '"
 								+ opt.getPrototype() + "' and '" + opt1.getPrototype()
 								+ "' cannot be specified simultaneously on the command line.",
 								1);
 					}
 					opt = opt1;
 				}
 			}
 		}
 	}
 
 	protected void invokeOptionHandlers(final PluginManager pluginManagerItf,
 			final CommandLine cmdLine, final Map<Object, Object> context)
 					throws InvalidCommandLineException {
 		final List<CmdOption> toBeExecuted = new LinkedList<CmdOption>(cmdLine
 				.getOptions().getOptions());
 		final Set<String> executedId = new HashSet<String>(toBeExecuted.size());
 		while (!toBeExecuted.isEmpty()) {
 			final int toBeExecutedSize = toBeExecuted.size();
 			final Iterator<CmdOption> iter = toBeExecuted.iterator();
 			while (iter.hasNext()) {
 				final CmdOption option = iter.next();
 				final List<String> precedenceIds = CommandLineOptionExtensionHelper
 						.getPrecedenceIds(option, pluginManagerItf);
 				if (executedId.containsAll(precedenceIds)) {
 					// task ready to be executed
 					for (final CommandOptionHandler handler : CommandLineOptionExtensionHelper
 							.getHandler(option, pluginManagerItf)) {
 						handler.processCommandOption(option, cmdLine, context);
 					}
 					executedId.add(option.getId());
 					iter.remove();
 				}
 			}
 			if (toBeExecutedSize == toBeExecuted.size()) {
 				// nothing has been executed. there is a circular dependency
 				throw new CompilerError(GenericErrors.GENERIC_ERROR,
 						"Circular dependency in command line option handlers: "
 								+ toBeExecuted);
 			}
 		}
 	}
 
 	// ---------------------------------------------------------------------------
 	// Utility methods
 	// ---------------------------------------------------------------------------
 
 	private void printExtensionPoints(final PluginManager pluginManager,
 			final PrintStream out) {
 		final Iterable<String> extensionPoints = pluginManager
 				.getExtensionPointNames();
 		System.out.println("Supported extension points are : ");
 		for (final String extensionPoint : extensionPoints) {
 			System.out.println("\t'" + extensionPoint + "'");
 		}
 	}
 
 	protected static void checkDir(final File d)
 			throws InvalidCommandLineException {
 		if (d.exists() && !d.isDirectory())
 			throw new InvalidCommandLineException("Invalid build directory '"
 					+ d.getAbsolutePath() + "' not a directory", 6);
 	}
 
 	protected String getVersion() {
 		final String pkgVersion = this.getClass().getPackage()
 				.getImplementationVersion();
 		return (pkgVersion == null) ? "unknown" : pkgVersion;
 	}
 
 	protected String getProgramName() {
 		return System.getProperty(PROGRAM_NAME_PROPERTY_NAME, getClass().getName());
 	}
 
 	protected void printVersion(final PrintStream ps) {
 		ps.println(getProgramName() + " version " + getVersion());
 	}
 
 	protected void printHelp(final PrintStream ps) {
 		printUsage(ps);
 		ps.println();
 		ps.println("Available options are :");
 		int maxCol = 0;
 
 		for (final CmdOption opt : options.getOptions()) {
 			final int col = 2 + opt.getPrototype().length();
 			if (col > maxCol) maxCol = col;
 		}
 		for (final CmdOption opt : options.getOptions()) {
 			final StringBuffer sb = new StringBuffer("  ");
 			sb.append(opt.getPrototype());
 			while (sb.length() < maxCol)
 				sb.append(' ');
 			sb.append("  ").append(opt.getDescription());
 			ps.println(sb);
 		}
 	}
 
 	protected void printUsage(final PrintStream ps) {
 		ps.println("Usage: " + getProgramName()
 				+ " [OPTIONS] (<test_path>)+");
 		ps.println("  where <test_path> is a path where to find test cases to be ran.");
 	}
 
 	protected void handleException(final InvalidCommandLineException e) {
 		logger.log(Level.FINER, "Caught an InvalidCommandLineException", e);
 		if (PrintStackTraceOptionHandler.getPrintStackTrace(compilerContext)) {
 			e.printStackTrace();
 		} else {
 			System.err.println(e.getMessage());
 			printHelp(System.err);
 			System.exit(e.getExitValue());
 		}
 	}
 
 	/**
 	 * Entry point.
 	 * 
 	 * @param args
 	 */
 	public static void main(final String... args) {
 		final Launcher l = new Launcher();
 		try {
 			l.init(args);
 			// TODO: not yet
 			//l.compile(null, null);
 		} catch (final InvalidCommandLineException e) {
 			l.handleException(e);
 		}
 		if (!l.errorManager.getErrors().isEmpty()) System.exit(1);
 	}
 
 	public static void nonExitMain(final String... args)
 			throws InvalidCommandLineException, ADLException {
 		nonExitMain(null, null, args);
 	}
 
 	public static void nonExitMain(final List<Error> errors,
 			final List<Error> warnings, final String... args)
 					throws InvalidCommandLineException, ADLException {
 		final Launcher l = new Launcher();
 		l.init(args);
 		// TODO: not yet
 		//l.compile(errors, warnings);
 	}
 
 }
