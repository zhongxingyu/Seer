 /*
  * #%L
  * it-xcode-maven-plugin
  * %%
  * Copyright (C) 2012 SAP AG
  * %%
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
  * #L%
  */
 package com.sap.prd.mobile.ios.mios;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.io.Reader;
 import java.io.Writer;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.maven.it.Verifier;
 import org.apache.maven.settings.Mirror;
 import org.apache.maven.settings.Settings;
 import org.apache.maven.settings.io.xpp3.SettingsXpp3Reader;
 import org.apache.maven.settings.io.xpp3.SettingsXpp3Writer;
import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 import org.junit.BeforeClass;
 import org.junit.Rule;
 import org.junit.rules.TemporaryFolder;
 
 public abstract class XCodeTest
 {
 
   private static File localRepo = null;
   private static String activeProfiles = null;
   
   @Rule
   public TemporaryFolder tmpFolder = new TemporaryFolder();
 
   @BeforeClass
   public static void setup() throws IOException, XmlPullParserException {
     prepareTestExecutionSettingsFile();
     prepareTestExecutionActiveProfiles();
     setupLocalRepo();
   }
   
   private static void prepareTestExecutionActiveProfiles()
   {
     String _activeProfiles = System.getProperty("com.sap.maven.integration-tests.active-profiles");
     
     if(_activeProfiles != null && !_activeProfiles.trim().isEmpty())
     {
       activeProfiles = _activeProfiles.trim();
       System.out.println("[INFO] Using active profiles: " + activeProfiles);
     }
   }
 
   private static void setupLocalRepo()
   {
     //
     // The local repo used during the integration tests must be the same than the local repo used for
     // building the xcode-maven-plugin previously in the build. Otherwise the binary version of the plugin
     // cannot be found or a wrong version will be found.
     // Since the base directory for building the xcode-maven-plugin and the base directory used for the
     // integration tests differs the corresponding path must be absolute.
     //
     
     String localRepoFilePath = System.getProperty("com.sap.maven.integration-tests.local-repo");
     
     if(localRepoFilePath == null || localRepoFilePath.isEmpty()) {
       
       localRepoFilePath = System.getProperty("user.home") + "/.m2/repository";
       
       System.out.println("[WARNING] Local Repository has not been provided. Please provide the local repo with \"-Dmaven.repo.local=\". " +
       		"Defaulting to '" + localRepoFilePath + "'.");
     }
     
     localRepo = new File(localRepoFilePath);
     
     if(!localRepo.isAbsolute())
       throw new RuntimeException("The path to the local repository '" + localRepoFilePath +"' is not absolute. " +
       		"Integration tests will only work reliably if that path is absolute.");
     
     System.out.println("[INFO] Using local repository '" + localRepo + "'.");
   }
 
   private static void prepareTestExecutionSettingsFile() throws IOException, XmlPullParserException {
 
     //
     // The settings file used for integration tests must be the same than the settings file used for the previously performed
     // build of the xcode-maven-plugin. Since the base directory for building the xcode-maven-plugin and the base directory for the
     // integration tests differs the corresponding path must be absolute.
     // For more details see the comment inside the configuration of the surefire plugin in the pom file of the integration tests.
     //
     
     String userSettingsFilePath = System.getProperty("com.sap.maven.integration-tests.user-settings");
     
     if(userSettingsFilePath == null || userSettingsFilePath.isEmpty())
     {
       userSettingsFilePath = System.getProperty("user.home") + "/.m2/settings.xml";
       
       System.out.println("[WARNING] No settings file has been provided. Please provide the user settings file used " +
       		"for integration tests with \"-Dcom.sap.maven.integration-tests.user-settings=\"." +
       		"Defaulting to user settings file located at '" + userSettingsFilePath + "'");
     }
     final File userSettingsFile = new File(userSettingsFilePath);
     
     if(!userSettingsFile.isAbsolute())
       throw new RuntimeException("The path to the user settings file '" + userSettingsFilePath + "' is not absolute. " +
       		"Integration tests will only work reliably if that path is absolute.");
     
     System.out.println("[INFO] Using settings file '" + userSettingsFile + "' for integration tests");
     
     final File testExecutionSettingsFile = getTestExectutionSettingsFile();
         
     if(testExecutionSettingsFile.exists())
       return;
     
     if(!testExecutionSettingsFile.getParentFile().exists() && !testExecutionSettingsFile.getParentFile().mkdirs())
         throw new IOException("Cannot create " + testExecutionSettingsFile.getParentFile());
       
     final Reader r = new InputStreamReader(new FileInputStream(userSettingsFile), "UTF-8");
     final Writer w = new OutputStreamWriter(new FileOutputStream(testExecutionSettingsFile), "UTF-8");
     
     try {
       
         Settings settings = new SettingsXpp3Reader().read(r);
         List<Mirror> mirrors = settings.getMirrors();  
         
         for(Mirror m : mirrors) {
 
           StringBuilder newMirrorOf = new StringBuilder(256);
 
           for(String mirror : m.getMirrorOf().split(",")) {
             
             if(newMirrorOf.length() > 0)
               newMirrorOf.append(",");
             
             if("*".equals(mirror))
               newMirrorOf.append("external:").append(mirror);
             else
               newMirrorOf.append(mirror);
           }
           
           m.setMirrorOf(newMirrorOf.toString());
         }
         
         new SettingsXpp3Writer().write(w, settings);
         System.out.println("[INFO] User settings file written to '" + testExecutionSettingsFile + "'.");
         
         
     } finally {
       IOUtils.closeQuietly(r);
       IOUtils.closeQuietly(w);
     }
   }
  
   private static File getTestExectutionSettingsFile() throws IOException {
     return new File(getTestsExecutionDirectory(), "settings.xml").getCanonicalFile();
   }
   
   protected final static Map<String, String> THE_EMPTY_MAP = new HashMap<String, String>();
   protected final static List<String> THE_EMPTY_LIST = new ArrayList<String>();
 
   protected Verifier test(final String testName, final File projectDirectory, final String pomFileName,
         final String target, List<String> additionalCommandLineOptions,
         Map<String, String> additionalSystemProperties, final File remoteRepositoryDirectory, final File frameworkRepositoryDirectory) throws Exception
   {
     return test(null, testName, projectDirectory, pomFileName, target, additionalCommandLineOptions,
           additionalSystemProperties, remoteRepositoryDirectory, frameworkRepositoryDirectory);
   }
   
   protected Verifier test(final String testName, final File projectDirectory, final String pomFileName,
         final String target, List<String> additionalCommandLineOptions,
         Map<String, String> additionalSystemProperties, final File remoteRepositoryDirectory) throws Exception
   {
 
     return test(null, testName, projectDirectory, pomFileName, target, additionalCommandLineOptions,
           additionalSystemProperties, remoteRepositoryDirectory, null);
   }
 
   protected Verifier test(final Verifier _verifier, final String testName, final File projectDirectory,
         final String pomFileName, final String target, List<String> additionalCommandLineOptions,
         Map<String, String> additionalSystemProperties, final File remoteRepositoryDirectory, final File frameworkRepository) throws Exception
   {
 
     if (additionalSystemProperties == null) {
       additionalSystemProperties = new HashMap<String, String>();
     }
 
     final String projectName = projectDirectory.getName();
 
     final Verifier verifier;
     final File testExecutionFolder;
 
     if (_verifier != null) {
       testExecutionFolder = new File(_verifier.getBasedir()).getCanonicalFile();
       verifier = _verifier;
     }
     else {
       testExecutionFolder = getTestExecutionDirectory(testName, projectName);
       verifier = new Verifier(testExecutionFolder.getAbsolutePath());
     }
 
     prepareTestExectutionFolder(projectDirectory, testExecutionFolder);
 
     rewritePom(
           new File(testExecutionFolder, pomFileName),
           remoteRepositoryDirectory, frameworkRepository);
 
     try {
 
       final Properties testSystemProperties = filterProperties(System
         .getProperties());
 
       testSystemProperties.putAll(additionalSystemProperties);
 
       System.out
         .println("SystemProperties used during integration test for '" + testName + "/" + projectName + "': \n"
               + testSystemProperties);
       
       final List<String> commandLineOptions = new ArrayList<String>();
       
       if(pomFileName != null) 
       {
         commandLineOptions.add("-f");
         commandLineOptions.add(pomFileName);
       }
 
       if(getTestExectutionSettingsFile() != null)
       {
         commandLineOptions.add("-s");
         commandLineOptions.add(getTestExectutionSettingsFile().getAbsolutePath());
       }
       
       if(localRepo != null)
       {
         commandLineOptions.add("-Dmaven.repo.local=" + localRepo.getAbsolutePath());
       }
       
       if(activeProfiles != null && !activeProfiles.trim().isEmpty())
       {
         commandLineOptions.add("-P");
         commandLineOptions.add(activeProfiles);
         
       }
         
       if (additionalCommandLineOptions != null)
         commandLineOptions.addAll(additionalCommandLineOptions);
 
       verifier.setCliOptions(commandLineOptions);
 
       verifier.setSystemProperties(testSystemProperties);
 
       verifier.deleteArtifacts("com.sap.production.ios.tests");
       
       verifier.executeGoal(target);
       verifier.verifyErrorFreeLog();
       verifier.resetStreams();
 
     }
     finally {
       verifier.resetStreams();
       final File logFile = new File(testExecutionFolder,
             verifier.getLogFileName());
 
       if (!logFile.exists())
         System.out.println("Log file '" + logFile
               + "' does not exist.");
       else
         showLog(projectName, logFile);
     }
     return verifier;
   }
 
   protected static File getTestsExecutionDirectory() {
     return new File(new File(".").getAbsoluteFile(), "target/tests/");
   }
   
   protected File getTestExecutionDirectory(final String testName, final String projectName)
   {
     return new File(
           getTestsExecutionDirectory(),
                 getClass().getName() + "/" + testName + "/" + projectName);
   }
 
   private void showLog(final String projectName, final File logFile)
         throws FileNotFoundException, IOException
   {
 
     System.out.println();
     System.out.println();
     System.out.println();
     System.out.println("Log output for project \"" + projectName + "\".");
     System.out.println();
     System.out.println();
     System.out.println();
 
     InputStream log = null;
 
     try {
 
       log = new BufferedInputStream(new FileInputStream(logFile));
 
       byte[] buff = new byte[1024];
 
       for (int i; (i = log.read(buff)) != -1;)
         System.out.write(buff, 0, i);
 
     }
     finally {
       if (log != null)
         IOUtils.closeQuietly(log);
     }
   }
 
   private void rewritePom(File pomFile, final File remoteRepository, final File frameworkRepository)
         throws IOException
   {
 
     String pom = IOUtils.toString(new FileInputStream(pomFile));
 
     pom = pom.replaceAll("\\$\\{deployrepo.directory\\}",
           remoteRepository.getAbsolutePath());
 
     if (frameworkRepository != null)
       pom = pom.replaceAll("\\$\\{frwkrepo.directory\\}",
             frameworkRepository.getAbsolutePath());
     
     pom = pom.replaceAll("\\$\\{xcode.maven.plugin.version\\}", getMavenXcodePluginVersion());
 
     final Writer w = new FileWriter(pomFile);
     try {
       w.write(pom);
     }
     finally {
       IOUtils.closeQuietly(w);
     }
   }
 
   private static Properties filterProperties(final Properties props)
   {
 
     final Properties filteredProperties = new Properties();
 
     //
     // If we do not filter the altDeploymentRepository this repository is
     // used
     // in the test cases. This is not the intended behavior. For the
     // test cases we would like to use the repository defined in the pom.
     // On the other hand we need altDeploymentRepository in order to be able
     // to deploy to SAP nexus from the team hudson.
     //
 
     for (final Entry<Object, Object> entry : props.entrySet()) {
       if (entry.getKey().equals("altDeploymentRepository"))
         continue;
       filteredProperties.put(entry.getKey(), entry.getValue());
     }
     return filteredProperties;
   }
 
   private static void prepareTestExectutionFolder(final File source,
         final File testExecutionFolder) throws IOException
   {
     FileUtils.deleteDirectory(testExecutionFolder);
    org.apache.commons.io.FileUtils.copyDirectory(source, testExecutionFolder);
   }
 
   protected static File getTestRootDirectory() throws IOException
   {
     return new File(new File(".").getCanonicalFile(), "src/test/projects");
   }
 
   protected static File getRemoteRepositoryDirectory(final String testName) throws IOException
   {
     return new File(new File(getTargetDirectory(), "remoteRepo"), testName);
   }
 
   protected static void prepareRemoteRepository(final File remoteRepository)
         throws IOException
   {
     FileUtils.deleteDirectory(remoteRepository);
     if (!remoteRepository.mkdirs())
       throw new IOException("Could not create directory "
             + remoteRepository);
   }
 
   protected String getMavenXcodePluginVersion() throws IOException
   {
     Properties properties = new Properties();
     properties.load(XCodeTest.class.getResourceAsStream("/project.properties"));
     final String xcodePluginVersion = properties.getProperty("xcode-plugin-version");
 
     if (xcodePluginVersion.equals("${project.version}"))
       throw new IllegalStateException(
             "Variable ${project.version} was not replaced. May be running \"mvn clean install\" beforehand might solve this issue.");
     return xcodePluginVersion;
   }
 
   private static File getTargetDirectory() throws IOException
   {
     return new File(new File(".").getCanonicalFile(), "target");
   }
 
   /**
    * Unpacks the zipped xcodeproj and checks if it can be compiled with a direct xcodebuild command
    * in order to verify if all dependencies are present. Code sogning is disabled for these test
    * builds
    * 
    * @return the directory where the zip file has bee extracted to
    */
   protected File assertUnpackAndCompile(File xcodeprojZip) throws IOException
   {
     File tmpXcodeProjDir = tmpFolder.newFolder(xcodeprojZip.getName());
     extractFileWithShellScript(xcodeprojZip, tmpXcodeProjDir);
     assertFalse("checkout dir should not be packaged", new File(tmpXcodeProjDir, "target/checkout").isDirectory());
     int exitcode = Forker.forkProcess(System.out, new File(tmpXcodeProjDir, "src/xcode/"), "xcodebuild", "clean",
           "build", "CODE_SIGN_IDENTITY=", "CODE_SIGNING_REQUIRED=NO");
 
     assertEquals("Building the unpacked project failed", 0, exitcode);
     return tmpXcodeProjDir;
   }
 
   protected void extractFileWithShellScript(File sourceFile, File destinationFolder)
         throws IOException
   {
     File workingDirectory = tmpFolder.newFolder("scriptWorkingDir");
     workingDirectory.deleteOnExit();
     ScriptRunner.copyAndExecuteScript(System.out, "/com/sap/prd/mobile/ios/mios/unzip.sh", workingDirectory,
           sourceFile.getCanonicalPath(), destinationFolder.getCanonicalPath());
   }
 }
