 // Copied from Maven, 2006-2007
 /*
  * Copyright 2001-2006 The Apache Software Foundation.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.freehep.maven.nar;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.StringTokenizer;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.surefire.SurefireBooter;
 import org.codehaus.plexus.util.FileUtils;
 
 /**
  * Run integration tests using Surefire.
  * 
  * This goal was copied from Maven's surefire plugin to accomodate a few
  * things for the NAR plugin: 1. To test a jar file with its native module we
  * can only run after the package phase, so we use the integration-test phase.
  * 2. We need to set java.library.path to an AOL (architecture-os-linker)
  * specific value, but AOL is only known in the NAR plugin and thus cannot be
  * set from the pom. 3. To have the java.library.path definition picked up by
  * java we need the "pertest" forkmode.
  * 
  * To use this goal you need to put the test sources in the regular test
  * directories but disable the running of the tests by the
  * maven-surefire-plugin.
  * 
  * @author Jason van Zyl (modified by Mark Donszelmann, noted by FREEHEP)
 * @version $Id: src/main/java/org/freehep/maven/nar/NarIntegrationTestMojo.java 97b7257b9e8d 2007/01/03 23:11:17 duns $, 2.1.x maven repository maven-surefire-plugin
  * @requiresDependencyResolution test
  * @goal nar-integration-test
  * @phase integration-test
  */
 // FREEHEP, changed class name, inheritence, goal and phase
 public class NarIntegrationTestMojo extends AbstractCompileMojo {
 
     // FREEHEP added test for JNI module
     private boolean testJNIModule() {
         for (Iterator i = getLibraries().iterator(); i.hasNext();) {
             Library lib = (Library) i.next();
             if (lib.getType().equals("jni"))
                 return true;
         }
         return false;
     }    
     
     // FREEHEP added to get names
     /**
      * @parameter expression="${project}"
      * @readonly
      * @required
      */
     private MavenProject project;    
 
     // FREEHEP added because of naming conflict
     /**
      * Skip running of NAR plugins (any) altogether.
      * 
      * @parameter expression="${nar.skip}" default-value="false"
      */
     private boolean skipNAR;
 
     /**
      * Set this to 'true' to bypass unit tests entirely. Its use is NOT
      * RECOMMENDED, but quite convenient on occasion.
      * 
      * @parameter expression="${maven.test.skip}"
      */
     private boolean skip;
 
     /**
      * Set this to true to ignore a failure during testing. Its use is NOT
      * RECOMMENDED, but quite convenient on occasion.
      * 
      * @parameter expression="${maven.test.failure.ignore}"
      */
     private boolean testFailureIgnore;
 
     /**
      * The base directory of the project being tested. This can be obtained in
      * your unit test by System.getProperty("basedir").
      * 
      * @parameter expression="${basedir}"
      * @required
      */
     private File basedir;
 
     /**
      * The directory containing generated classes of the project being tested.
      * 
      * @parameter expression="${project.build.outputDirectory}"
      * @required
      */
     private File classesDirectory;
 
     /**
      * The directory containing generated test classes of the project being
      * tested.
      * 
      * @parameter expression="${project.build.testOutputDirectory}"
      * @required
      */
     private File testClassesDirectory;
 
     /**
      * The classpath elements of the project being tested.
      * 
      * @parameter expression="${project.testClasspathElements}"
      * @required
      * @readonly
      */
     private List classpathElements;
 
     /**
      * Base directory where all reports are written to.
      * 
      * @parameter expression="${project.build.directory}/surefire-reports"
      */
     private String reportsDirectory;
 
     /**
      * Specify this parameter if you want to use the test pattern matching
      * notation, Ant pattern matching, to select tests to run. The Ant pattern
      * will be used to create an include pattern formatted like
      * <code>**&#47;${test}.java</code> When used, the <code>includes</code>
      * and <code>excludes</code> patterns parameters are ignored
      * 
      * @parameter expression="${test}"
      */
     private String test;
 
     /**
      * List of patterns (separated by commas) used to specify the tests that
      * should be included in testing. When not specified and whent the
      * <code>test</code> parameter is not specified, the default includes will
      * be
      * <code>**&#47;Test*.java   **&#47;*Test.java   **&#47;*TestCase.java</code>
      * 
      * @parameter
      */
     private List includes;
 
     /**
      * List of patterns (separated by commas) used to specify the tests that
      * should be excluded in testing. When not specified and whent the
      * <code>test</code> parameter is not specified, the default excludes will
      * be
      * <code>**&#47;Abstract*Test.java  **&#47;Abstract*TestCase.java **&#47;*$*</code>
      * 
      * @parameter
      */
     private List excludes;
 
     /**
      * ArtifactRepository of the localRepository. To obtain the directory of
      * localRepository in unit tests use System.setProperty( "localRepository").
      * 
      * @parameter expression="${localRepository}"
      * @required
      * @readonly
      */
     // FREEHEP removed, already in superclass
 //    private ArtifactRepository localRepository;
 
     /**
      * List of System properties to pass to the JUnit tests.
      * 
      * @parameter
      */
     private Properties systemProperties;
 
     /**
      * List of of Plugin Artifacts.
      * 
      * @parameter expression="${plugin.artifacts}"
      */
     private List pluginArtifacts;
 
     /**
      * Option to print summary of test suites or just print the test cases that
      * has errors.
      * 
      * @parameter expression="${surefire.printSummary}" default-value="true"
      */
     private boolean printSummary;
 
     /**
      * Selects the formatting for the test report to be generated. Can be set as
      * brief, plain, or xml.
      * 
      * @parameter expression="${surefire.reportFormat}" default-value="brief"
      */
     private String reportFormat;
 
     /**
      * Option to generate a file test report or just output the test report to
      * the console.
      * 
      * @parameter expression="${surefire.useFile}" default-value="true"
      */
     private boolean useFile;
 
     /**
      * Option to specify the forking mode. Can be "none", "once" or "pertest".
      * 
      * @parameter expression="${forkMode}" default-value="none"
      */
     private String forkMode;
 
     /**
      * Option to specify the jvm (or path to the java executable) to use with
      * the forking options. For the default we will assume that java is in the
      * path.
      * 
      * @parameter expression="${jvm}" default-value="java"
      */
     private String jvm;
 
     /**
      * Arbitrary options to set on the command line.
      * 
      * @parameter expression="${argLine}"
      */
     private String argLine;
 
     /**
      * Additional environments to set on the command line.
      * 
      * @parameter
      */
     private Map environmentVariables = new HashMap();
 
     /**
      * Command line working directory.
      * 
      * @parameter
      */
     private File workingDirectory;
 
     /**
      * When false it makes tests run using the standard classloader delegation
      * instead of the default Maven isolated classloader. Only used when forking
      * (forkMode is not "none").<br/> Setting it to false helps with some
      * problems caused by conflicts between xml parsers in the classpath and the
      * Java 5 provider parser.
      * 
      * @parameter expression="${childDelegation}" default-value="true"
      */
     private boolean childDelegation;
 
     public void execute() throws MojoExecutionException {
     	// FREEHEP, shouldSkip() does not work...
     	if (skipNAR) {
     		getLog().info("***********************************************************************");
     		getLog().info("NAR Integration Tests are SKIPPED since no NAR libraries were built.");
     		getLog().info("***********************************************************************");
     		return;
     	}
     	
         if (skip) {
             getLog().info("Tests are skipped.");
 
             return;
         }
 
         if (!testClassesDirectory.exists()) {
             getLog().info("No tests to run.");
 
             return;
         }
 
         // ----------------------------------------------------------------------
         // Setup the surefire booter
         // ----------------------------------------------------------------------
 
         SurefireBooter surefireBooter = new SurefireBooter();
 
         // ----------------------------------------------------------------------
         // Reporting
         // ----------------------------------------------------------------------
 
         getLog().info("Setting reports dir: " + reportsDirectory);
 
         surefireBooter.setReportsDirectory(reportsDirectory);
 
         // ----------------------------------------------------------------------
         // Check to see if we are running a single test. The raw parameter will
         // come through if it has not been set.
         // ----------------------------------------------------------------------
 
         if (test != null) {
             // FooTest -> **/FooTest.java
 
             List includes = new ArrayList();
 
             List excludes = new ArrayList();
 
             String[] testRegexes = split(test, ",", -1);
 
             for (int i = 0; i < testRegexes.length; i++) {
                 includes.add("**/" + testRegexes[i] + ".java");
             }
 
             surefireBooter.addBattery(
                     "org.apache.maven.surefire.battery.DirectoryBattery",
                     new Object[] { testClassesDirectory, includes, excludes });
         } else {
             // defaults here, qdox doesn't like the end javadoc value
             // Have to wrap in an ArrayList as surefire expects an ArrayList
             // instead of a List for some reason
             if (includes == null || includes.size() == 0) {
                 includes = new ArrayList(Arrays
                         .asList(new String[] { "**/Test*.java",
                                 "**/*Test.java", "**/*TestCase.java" }));
             }
             if (excludes == null || excludes.size() == 0) {
                 excludes = new ArrayList(Arrays.asList(new String[] {
                         "**/Abstract*Test.java", "**/Abstract*TestCase.java",
                         "**/*$*" }));
             }
 
             surefireBooter.addBattery(
                     "org.apache.maven.surefire.battery.DirectoryBattery",
                     new Object[] { testClassesDirectory, includes, excludes });
         }
 
         // ----------------------------------------------------------------------
         //
         // ----------------------------------------------------------------------
 
         getLog().debug("Test Classpath :");
 
         getLog().debug(testClassesDirectory.getPath());
 
         surefireBooter.addClassPathUrl(testClassesDirectory.getPath());
 
         getLog().debug(classesDirectory.getPath());
 
         surefireBooter.addClassPathUrl(classesDirectory.getPath());
 
         for (Iterator i = classpathElements.iterator(); i.hasNext();) {
             String classpathElement = (String) i.next();
 
             getLog().debug(classpathElement);
 
             surefireBooter.addClassPathUrl(classpathElement);
         }
 
         for (Iterator i = pluginArtifacts.iterator(); i.hasNext();) {
             Artifact artifact = (Artifact) i.next();
 
             // TODO: this is crude for now. We really want to get
             // "surefire-booter" and all its dependencies, but the
             // artifacts don't keep track of their children. We could just throw
             // all of them in, but that would add an
             // unnecessary maven-artifact dependency which is precisely the
             // reason we are isolating the classloader
             if ("junit".equals(artifact.getArtifactId())
                     || "surefire".equals(artifact.getArtifactId())
                     || "surefire-booter".equals(artifact.getArtifactId())
                     || "plexus-utils".equals(artifact.getArtifactId())) {
                 getLog().debug(
                         "Adding to surefire test classpath: "
                                 + artifact.getFile().getAbsolutePath());
 
                 surefireBooter.addClassPathUrl(artifact.getFile()
                         .getAbsolutePath());
             }
         }
 
         addReporters(surefireBooter);
 
         processSystemProperties();
 
         // ----------------------------------------------------------------------
         // Forking
         // ----------------------------------------------------------------------
 
         boolean success;
         try {
 // FREEHEP
             if (project.getPackaging().equals("nar") || (getNarManager().getNarDependencies("test").size() > 0)) forkMode="pertest";
             
             surefireBooter.setForkMode(forkMode);
 
             if (!forkMode.equals("none")) {
                 surefireBooter.setSystemProperties(System.getProperties());
 
                 surefireBooter.setJvm(jvm);
 
                 surefireBooter.setBasedir(basedir.getAbsolutePath());
 
 // FREEHEP      
                 if (argLine == null) argLine = "";
                 
                 StringBuffer javaLibraryPath = new StringBuffer();
                 if (testJNIModule()) {
                     // Add libraries to java.library.path for testing
                     String thisLib = "target/nar/lib/"+getAOL()+"/jni";
                     if (new File(thisLib).exists()) {
                         System.err.println("Adding to java.library.path: "+thisLib);
                         javaLibraryPath.append(thisLib);
                     }
                     
                     // add jar file to classpath, as one may want to read a properties file for artifactId and version
                     String jarFile = "target/"+project.getArtifactId()+"-"+project.getVersion()+".jar";
                     getLog().debug("Adding to surefire test classpath: "+jarFile);
                     surefireBooter.addClassPathUrl(jarFile);
                 }
                 
                 
                 List dependencies = getNarManager().getNarDependencies("test");
                 for (Iterator i=dependencies.iterator(); i.hasNext(); ) {
                     NarArtifact dependency = (NarArtifact)i.next();
                     NarInfo info = dependency.getNarInfo();
                     String binding = info.getBinding(getAOL(), "static");
                     if (!binding.equals("static")) {
                         File depLib = new File(getNarManager().getNarFile(dependency).getParent(), "nar/lib/"+getAOL()+"/"+binding);
                         String depLibPath = depLib.getPath();
                         System.err.println("Adding to java.library.path: "+depLibPath);
                        if (javaLibraryPath.length() > 0) javaLibraryPath.append(";");                        
                         javaLibraryPath.append(depLibPath);
                     }
                 }
                                 
                 // add final javalibrary path
                 if (javaLibraryPath.length() > 0) {
                     if ((javaLibraryPath.indexOf(" ") >= 0) || (javaLibraryPath.indexOf("\"") >= 0) || (javaLibraryPath.indexOf("'") >= 0)) {
                         String windowsPath = javaLibraryPath.toString();
                        String unixPath = windowsPath.replace(';', ':');
                         addPathToEnv("PATH", windowsPath, ";");
                         addPathToEnv("LD_LIBRARY_PATH", unixPath, ":");
                         addPathToEnv("DYLD_LIBRARY_PATH", unixPath, ":");
                     } else {
                         // NOTE: does not work with arguments with spaces as SureFireBooter splits the line in parts and then quotes it wrongly
                         argLine += " -Djava.library.path="+javaLibraryPath.toString();
                     }
                 }
 // ENDFREEHEP
 
                 surefireBooter.setArgLine(argLine);
 
                 surefireBooter.setEnvironmentVariables(environmentVariables);
 
                 surefireBooter.setWorkingDirectory(workingDirectory);
 
                 surefireBooter.setChildDelegation(childDelegation);
 
                 if (getLog().isDebugEnabled()) {
                     surefireBooter.setDebug(true);
                 }
             }
 
             success = surefireBooter.run();
         } catch (Exception e) {
             // TODO: better handling
             throw new MojoExecutionException("Error executing surefire", e);
         }
 
         if (!success) {
             String msg = "There are test failures.";
 
             if (testFailureIgnore) {
                 getLog().error(msg);
             } else {
                 throw new MojoExecutionException(msg);
             }
         }
     }
 
 // FREEHEP
     private void addPathToEnv(String key, String path, String separator) {
         String value = (String)environmentVariables.get(key);
         if (value == null) {
             value = getEnv(key, key, null);
         }
         
         if (value != null) {
             value += separator + path;
         } else {
             value = path;
         }
         environmentVariables.put(key, value);
     }
     
     private String getEnv( String envKey, String alternateSystemProperty, String defaultValue ) {
         String envValue = null;
         try {
             envValue = System.getenv( envKey );
             if ( envValue == null && alternateSystemProperty != null ) {
                 envValue = System.getProperty( alternateSystemProperty );
             }
         } catch ( Error e ) {
             //JDK 1.4?
             if ( alternateSystemProperty != null ) {
                 envValue = System.getProperty( alternateSystemProperty );
             }
         }
 
         if ( envValue == null ) {
             envValue = defaultValue;
         }
 
         return envValue;
     } 
 // ENDFREEHEP
     
     protected void processSystemProperties() {
         System.setProperty("basedir", basedir.getAbsolutePath());
 
         // FREEHEP, use access method
         System.setProperty("localRepository", getLocalRepository().getBasedir());
 
         // Add all system properties configured by the user
         if (systemProperties != null) {
             Enumeration propertyKeys = systemProperties.propertyNames();
 
             while (propertyKeys.hasMoreElements()) {
                 String key = (String) propertyKeys.nextElement();
 
                 System.setProperty(key, systemProperties.getProperty(key));
 
                 getLog().debug(
                         "Setting system property [" + key + "]=["
                                 + systemProperties.getProperty(key) + "]");
             }
         }
 
     }
 
     protected String[] split(String str, String separator, int max) {
         StringTokenizer tok;
 
         if (separator == null) {
             // Null separator means we're using StringTokenizer's default
             // delimiter, which comprises all whitespace characters.
             tok = new StringTokenizer(str);
         } else {
             tok = new StringTokenizer(str, separator);
         }
 
         int listSize = tok.countTokens();
 
         if (max > 0 && listSize > max) {
             listSize = max;
         }
 
         String[] list = new String[listSize];
 
         int i = 0;
 
         int lastTokenBegin;
 
         int lastTokenEnd = 0;
 
         while (tok.hasMoreTokens()) {
             if (max > 0 && i == listSize - 1) {
                 // In the situation where we hit the max yet have
                 // tokens left over in our input, the last list
                 // element gets all remaining text.
                 String endToken = tok.nextToken();
 
                 lastTokenBegin = str.indexOf(endToken, lastTokenEnd);
 
                 list[i] = str.substring(lastTokenBegin);
 
                 break;
             } else {
                 list[i] = tok.nextToken();
 
                 lastTokenBegin = str.indexOf(list[i], lastTokenEnd);
 
                 lastTokenEnd = lastTokenBegin + list[i].length();
             }
             i++;
         }
 
         return list;
     }
 
     /**
      * <p>
      * Adds Reporters that will generate reports with different formatting.
      * <p>
      * The Reporter that will be added will be based on the value of the
      * parameter useFile, reportFormat, and printSummary.
      * 
      * @param surefireBooter
      *            The surefire booter that will run tests.
      */
     private void addReporters(SurefireBooter surefireBooter) {
         if (useFile) {
             if (printSummary) {
                 if (forking()) {
                     surefireBooter
                             .addReport("org.apache.maven.surefire.report.ForkingConsoleReporter");
                 } else {
                     surefireBooter
                             .addReport("org.apache.maven.surefire.report.ConsoleReporter");
                 }
             } else {
                 if (forking()) {
                     surefireBooter
                             .addReport("org.apache.maven.surefire.report.ForkingSummaryConsoleReporter");
                 } else {
                     surefireBooter
                             .addReport("org.apache.maven.surefire.report.SummaryConsoleReporter");
                 }
             }
 
             if (reportFormat.equals("brief")) {
                 surefireBooter
                         .addReport("org.apache.maven.surefire.report.BriefFileReporter");
             } else if (reportFormat.equals("plain")) {
                 surefireBooter
                         .addReport("org.apache.maven.surefire.report.FileReporter");
             }
         } else {
             if (reportFormat.equals("brief")) {
                 surefireBooter
                         .addReport("org.apache.maven.surefire.report.BriefConsoleReporter");
             } else if (reportFormat.equals("plain")) {
                 surefireBooter
                         .addReport("org.apache.maven.surefire.report.DetailedConsoleReporter");
             }
         }
 
         surefireBooter
                 .addReport("org.apache.maven.surefire.report.XMLReporter");
     }
 
     private boolean forking() {
         return !forkMode.equals("none");
     }
 }
