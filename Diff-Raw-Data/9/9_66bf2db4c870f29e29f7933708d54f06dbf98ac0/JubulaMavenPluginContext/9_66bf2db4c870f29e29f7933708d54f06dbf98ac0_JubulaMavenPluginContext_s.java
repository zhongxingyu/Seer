 package org.mule.tooling.jubula;
 
 import java.io.File;
 import java.text.MessageFormat;
 
 public abstract class JubulaMavenPluginContext {
 
 	public static final String RCPWORKSPACE_DIRECTORY_NAME = "rcpworkspace";
 	public static final String RESULTS_DIRECTORY_NAME = "results";
 	public static final String JUBULA_BOOTSTRAP_VERSION = "1.3";
 	public static final String SUREFIRE_RESULTS_DIRECTORY_NAME = "surefire-reports";
 	
 	public static File buildDirectory;
 
 	public static String pathToServerPluginsDirectory() {
 		return MessageFormat.format( //
 				"{2}{1}jubula-bootstrap-{0}{1}server{1}plugins", //
 				JUBULA_BOOTSTRAP_VERSION, //
 				File.separator, //
 				buildDirectory.getAbsolutePath());
 	}
 
 	public static String pathToJubulaPluginsDirectory() {
 		return MessageFormat.format( //
 				"{2}{1}jubula-bootstrap-{0}{1}jubula{1}plugins", //
 				JUBULA_BOOTSTRAP_VERSION, //
 				File.separator, //
 				buildDirectory.getAbsolutePath());
 	}
 
 	public static void initializeContext(final File buildDirectory) {
 		JubulaMavenPluginContext.buildDirectory = buildDirectory;
 	}
 
 	public static String pathToJubulaInstallationDirectory() {
 		return MessageFormat.format( //
				"{2}{1}jubula-bootstrap-{0}{1}jubula", //
 				JUBULA_BOOTSTRAP_VERSION, //
 				File.separator, //
 				buildDirectory.getAbsolutePath());
 	}
 
 }
