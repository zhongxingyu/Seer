 package ch.rotscher.mavenext;
 
 /**
  * Copyright 2012 Roger Brechbühl
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 import org.apache.maven.MavenExecutionException;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Model;
 import org.apache.maven.model.Parent;
 import org.apache.maven.model.io.DefaultModelReader;
 import org.apache.maven.model.io.ModelReader;
 import org.codehaus.plexus.component.annotations.Component;
 import org.codehaus.plexus.component.annotations.Requirement;
 import org.codehaus.plexus.logging.Logger;
 import org.codehaus.plexus.logging.console.ConsoleLogger;
 
 import java.io.*;
 import java.util.Map;
 
 /**
  * <p>
  * extension of {@link org.apache.maven.model.io.ModelReader} for overriding ${project.version} with the value given as a Java system property:
  * </p>
  * <p>
  * <code>-Dversion.override=A-VERSION</code><br />
  * <br />
  * other options are:<br />
  * <code>-Dversion.override.fail-on-error=[true | false]</code><br />
  * <code>-Dversion.override.strict=[true | false]</code><br />
  * <code>-Dversion.override.check-snapshot-dependency=[true | false]</code><br />
  * </p>
  * <p>
  * This class helps releasing a project (from a maven point of view, that means not having "SNAPSHOT" in the version) without actually changing (and committing)
  * the pom.xml
  * </p>
  * <p>
  * Copyright by Roger Brechbühl, 2012
  * </p>
  *
  * @author Roger Brechbühl
  */
 @Component(role = ModelReader.class, hint = "custom-version-model-reader")
 public class VersionOverrideModelReader extends DefaultModelReader implements ModelReader {
 
     static final String MAVENEXT_RELEASE_VERSION = "version.override";
     static final String MAVENEXT_BE_STRICT = "version.override.strict";
     static final String MAVENEXT_CHECK_SNAPSHOT_DEP_FAIL_ON_ERROR = "version.override.fail-on-error";
     static final String MAVENEXT_CHECK_SNAPSHOT_DEP = "version.override.check-snapshot-dependency";
     static final String MAVENEXT_BUILDNUMBER_FILE = ".buildnumber";
 
     private static RootPomData rootPomData = null;
 
     @Requirement
     private Logger logger = new ConsoleLogger();
 
     @Override
     public Model read(InputStream input, Map<String, ?> options) throws IOException {
         Model model = super.read(input, options);
         // this property is set from the command line with -Dversion.override
         String version = System.getProperty(MAVENEXT_RELEASE_VERSION);
         if (version == null) {
             return model;
         }
 
         String currentGroupId = getGroupId(model);
         if (rootPomData == null) {
             rootPomData = new RootPomData(model, version, logger);
         }
 
         version = rootPomData.version;
 
         Boolean beStrict = Boolean.parseBoolean(System.getProperty(MAVENEXT_BE_STRICT));
 
         //either the groupId is equals or 
         //e.g. the version is overridden here: ch.rotscher.app and ch.rotscher.app.domain 
         // vs. not overridden in that case:    ch.rotscher.app and ch.rotscher.application
         if (currentGroupId.equals(rootPomData.groupId) || currentGroupId.startsWith(rootPomData.groupId + ".")) {
        	String modelVersion = getVersion(model);
            if (beStrict && !modelVersion.equals(rootPomData.originalVersion)) {
                 if (logger.isDebugEnabled()) {
                     logger.debug(String.format("version of %s (%s) not changed as it differ: %s", model, model.getVersion(), rootPomData.version));
                 }
             } else {
                 model.setVersion(version);
                 if (logger.isDebugEnabled()) {
                     logger.debug(String.format("changing version of %s to %s", model, version));
                 }
                 Parent parent = model.getParent();
                 if (parent != null && (parent.getGroupId().equals(rootPomData.groupId) || parent.getGroupId().startsWith(rootPomData.groupId + "."))) {
                     logger.debug(String.format("changing version of parent  %s  to %s", parent, version));
                     parent.setVersion(version);
                 }
             }
         }
 
         Boolean checkSnapshotDependencies = Boolean.parseBoolean(System.getProperty(MAVENEXT_CHECK_SNAPSHOT_DEP));
         Boolean failOnError = Boolean.parseBoolean(System.getProperty(MAVENEXT_CHECK_SNAPSHOT_DEP_FAIL_ON_ERROR));
 
         if (checkSnapshotDependencies == Boolean.TRUE) {
             for (Dependency dep : model.getDependencies()) {
                 String depVersion = dep.getVersion();
                 if (depVersion != null && depVersion.contains("SNAPSHOT")) {
                     File pomFile = model.getPomFile();
                     String pomFilePath = "[path not available]";
                     if (pomFile != null) {
                         pomFilePath = pomFile.getPath();
                     }
                     String errorMsg = String.format("there is a snapshot dependency (%s) in %s (%s)", dep, model, pomFilePath);
                     if (failOnError == Boolean.TRUE) {
                         logger.warn(errorMsg);
                         // wrap in a IOException as this one is thrown in the signature
                         throw new IOException(new MavenExecutionException(errorMsg, model.getPomFile()));
                     } else {
                         logger.warn(errorMsg);
                     }
                 }
             }
         }
 
         return model;
     }
    
    private String getVersion(Model model) {
    	return model.getVersion() == null ? model.getParent().getVersion() : model.getVersion();
    }
 
     /**
      * get the groupId of the given model. if not set then ask the parent
      *
      * @param model the maven model
      * @return the groupId of the given module
      */
     static String getGroupId(Model model) {
         if (model.getGroupId() != null) {
             return model.getGroupId();
         }
 
         if (model.getParent() != null) {
             return model.getParent().getGroupId();
         }
 
         return "n/A";
     }
 
     /**
      * set by the unit test.
      *
      * @param logger the logger
      */
     void setLogger(Logger logger) {
         this.logger = logger;
     }
 
     static void reset() {
         rootPomData = null;
     }
 
     public static boolean isVersionOverridden() {
         String version = System.getProperty(VersionOverrideModelReader.MAVENEXT_RELEASE_VERSION);
         return version != null;
     }
 
     /**
      * helper class to store the very first groupId which is the base groupId for all following modules:
      * <p/>
      * the modules version is changed if the test <code>if module.groupId starts with GroupIdOfRootPom.groupId</code> is true
      */
     static class RootPomData {
 
         private final String originalVersion;
         private final String groupId;
         private final String version;
         private final Logger logger;
 
         public RootPomData(Model model, String version, Logger logger) throws IOException {
             this.logger = logger;
             this.originalVersion = model.getVersion();
             this.groupId = VersionOverrideModelReader.getGroupId(model);
             if (version.trim().length() < 1 || Boolean.valueOf(version)) {
                 this.version = generateVersion(model);
                 System.setProperty(MAVENEXT_RELEASE_VERSION, this.version);
             } else {
                 this.version = version;
             }
 
             logger.info(String
                     .format("initialize groupId to '%s', the version of all modules and dependencies starting with this groupdId will be changed!",
                             groupId));
         }
 
         private String generateVersion(Model model) throws IOException {
 
             //generate a version based on the {project.version}
             String[] pomVersionTokens = model.getVersion().split("-");
             if (pomVersionTokens.length == 1) {
                 return model.getVersion();
             }
 
             String buildNumber = "0";
 
             String jenkinsBuildNumber = System.getenv("BUILD_NUMBER");
             if (jenkinsBuildNumber != null) {
                 buildNumber = jenkinsBuildNumber;
                 logger.info(String
                         .format("using buildnumber from jenkins: %s",
                                 buildNumber));
             } else {
                 //read from buildnumber file
                 File buildNumberFile = new File(MAVENEXT_BUILDNUMBER_FILE);
                 if (buildNumberFile.exists()) {
                     buildNumber = readFirstLine(buildNumberFile);
                 }
                 int incrementedBuildNumber = Integer.parseInt(buildNumber) + 1;
                 buildNumber = incrementedBuildNumber + "";
                 writeLine(incrementedBuildNumber + "", buildNumberFile);
                 logger.info(String
                         .format("using buildnumber from file .buildnumber: %s",
                                 buildNumber));
             }
 
             String versionNumber = pomVersionTokens[0];
             String classifier = pomVersionTokens[1];
             if (classifier.equals("SNAPSHOT")) {
                 //we shorten a little and avoid the literal 'SNAPSHOT'
                 classifier = "S";
             }
 
             return String.format("%s-%s-%s", versionNumber, classifier, buildNumber);
         }
 
         private String readFirstLine(File file) throws IOException {
 
             BufferedReader br = null;
 
             try {
                 br = new BufferedReader(new FileReader(file));
                 return br.readLine();
             } finally {
                 if (br != null)
                     br.close();
             }
         }
 
         private void writeLine(String line, File file) throws IOException {
             if (!file.exists()) {
                 file.createNewFile();
             }
 
 
             FileOutputStream fos = new FileOutputStream(file);
 
             // get the content in bytes
             byte[] contentInBytes = line.getBytes();
 
             fos.write(contentInBytes);
             fos.flush();
             fos.close();
         }
 
     }
 
 
 
 }
