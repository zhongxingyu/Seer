 /*
  * Copyright (C) 2010 Teleal GmbH, Switzerland
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as
  * published by the Free Software Foundation, either version 3 of the
  * License, or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU Affero General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.teleal.lemma.maven;
 
 import org.apache.maven.artifact.DependencyResolutionRequiredException;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.classworlds.ClassRealm;
 import org.codehaus.classworlds.ClassWorld;
 import org.codehaus.plexus.util.FileUtils;
 import org.teleal.common.io.IO;
 import org.teleal.common.logging.LoggingUtil;
 import org.teleal.common.xhtml.XHTML;
 import org.teleal.lemma.pipeline.javadoc.XHTMLTemplateJavadocPipeline;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.LogManager;
 import java.util.logging.LogRecord;
 
 /**
  * @author Christian Bauer
  * @goal manual
  * @requiresDependencyResolution test
  */
 public class LemmaMojo extends AbstractMojo {
 
     /**
      * @parameter expression="${manual.testSourceDirectory}"
      * default-value="${project.build.testSourceDirectory}" description="The base path of all test sources."
      */
     protected File testSourceDirectory;
 
     /**
      * @parameter expression="${manual.manualSourceDirectory}"
      * default-value="${basedir}/src/manual"
      * description="The directory containing manual template and resources."
      */
     protected File manualSourceDirectory;
 
     /**
      * @parameter description="Included package, repeat option for multiple packages."
      */
     protected List<String> packageNames = new ArrayList();
 
     /**
      * @parameter expression="${manual.templateFilename}"
      * default-value="${project.artifactId}-manual.xhtml"
      */
     protected String templateFilename;
 
     /**
      * @parameter expression="${manual.outputFilename}"
      * default-value="${project.artifactId}-manual"
      */
     protected String outputFilename;
 
     /**
      * @parameter expression="${manual.outputPath}"
      * default-value="manual"
      */
     protected String outputPath;
 
     /**
      * @parameter expression="${manual.renameXHTMLFiles}"
      * default-value="true"
      */
     protected boolean renameXHTMLFiles;
 
     /**
      * @parameter
      */
     protected List<String> deleteSiteFiles = new ArrayList();
 
     /**
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     protected MavenProject project;
 
     public MavenProject getProject() {
         return project;
     }
 
     // Maven's plugin classloader does not see the project's build/test output by default, see
     // http://maven.apache.org/guides/mini/guide-maven-classloading.html
     public void extendPluginClasspath(List<String> elements) throws MojoExecutionException {
         // I found most of this on pastebin
         ClassWorld world = new ClassWorld();
         ClassRealm realm;
         try {
             realm = world.newRealm(
                     "maven.plugin." + getClass().getSimpleName(),
                     Thread.currentThread().getContextClassLoader()
             );
 
             for (String element : elements) {
                 File elementFile = new File(element);
                 getLog().debug("Adding element to plugin classpath" + elementFile.getPath());
                 URL url = new URL("file:///" + elementFile.getPath() + (elementFile.isDirectory() ? "/" : ""));
                 realm.addConstituent(url);
             }
         } catch (Exception ex) {
             throw new MojoExecutionException(ex.toString(), ex);
         }
         Thread.currentThread().setContextClassLoader(realm.getClassLoader());
     }
 
     public void execute() throws MojoExecutionException, MojoFailureException {
 
         try {
             // We might want to load stuff from the test classpath
             extendPluginClasspath((List<String>)project.getTestClasspathElements());
 
             File templateFile = new File(manualSourceDirectory, templateFilename);
             if (!templateFile.exists()) {
                 throw new Exception("Configured template not found in manual directory: " + templateFile);
             }
 
             XHTMLTemplateJavadocPipeline pipeline = createPipeline(testSourceDirectory, packageNames, project);
             XHTML result = pipeline.execute(templateFile);
 
             String path = IO.makeRelativePath(outputPath, project.getBuild().getDirectory());
             File outputFile = new File(project.getBuild().getDirectory() + "/" + path, outputFilename + ".xhtml");
             pipeline.prepareOutputFile(outputFile, true);
             getLog().info("Writing output file: " + outputFile.getAbsolutePath());
 
             IO.writeUTF8(
                     outputFile,
                     pipeline.getParser().print(result, 4, true)
             );
 
             copyManualResources(new File(project.getBuild().getDirectory(), path));
             copyDocFiles(new File(project.getBuild().getDirectory(), path));
 
         } catch (Exception ex) {
             throw new MojoExecutionException("Error occured: " + ex.getMessage(), ex);
         }
 
     }
 
     public XHTMLTemplateJavadocPipeline createPipeline(File baseDirectory, List<String> packageNames, MavenProject project)
             throws Exception {
         getLog().info(">>> Generating documentation using source base directory: " + baseDirectory);
 
         // Yep, the Javadoc tool has its own classpath, we abuse the javac system property to get it into the Gaftercode
         String javadocClasspath = null;
         try {
 
             List<String> classpathElements = (List<String>) project.getTestClasspathElements();
             StringBuilder sb = new StringBuilder();
             for (String classpathElement : classpathElements) {
                 sb.append(classpathElement).append(File.pathSeparator);
             }
             if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
             javadocClasspath = sb.toString();
 
             if (javadocClasspath != null) {
                 getLog().debug("Setting Javadoc classpath: " + javadocClasspath);
                 System.setProperty("env.class.path", javadocClasspath); // The Javadoc code reads this env variable!
             }
 
         } catch (DependencyResolutionRequiredException ex) {
             throw new Exception("Can't get test classpath: " + ex.toString(), ex);
         }
 
         // Hurray for more logging abstractions!
         Handler loggingAdapter = new Handler() {
 
             Formatter formatter = new Formatter() {
                 @Override
                 public String format(LogRecord logRecord) {
                     return formatMessage(logRecord);
                 }
             };
 
             @Override
             public void publish(LogRecord logRecord) {
                 if (logRecord.getLevel().equals(Level.SEVERE) && getLog().isErrorEnabled()) {
                     getLog().error(formatter.format(logRecord));
                 } else if (logRecord.getLevel().equals(Level.WARNING) && getLog().isWarnEnabled()) {
                     getLog().warn(formatter.format(logRecord));
                 } else if (logRecord.getLevel().equals(Level.INFO) && getLog().isInfoEnabled()) {
                     getLog().info(formatter.format(logRecord));
                 } else if (getLog().isDebugEnabled()) {
                     getLog().debug(formatter.format(logRecord));
                 }
             }
 
             @Override
             public void flush() {
             }
 
             @Override
             public void close() throws SecurityException {
             }
         };
         loggingAdapter.setLevel(Level.ALL);
         LoggingUtil.resetRootHandler(loggingAdapter);
         LogManager.getLogManager().getLogger("").setLevel(Level.ALL);
 
         // Check the configuration
         if (!baseDirectory.canRead()) {
             throw new Exception("Configured 'testSourceDirectory' not found or not readable: " + baseDirectory);
         }
 
         if (packageNames.size() == 0) {
             // Default to all sub-directories in base directory
             File[] subdirs = baseDirectory.listFiles(new FileFilter() {
                 public boolean accept(File file) {
                     return file.isDirectory() && file.getName().matches("[a-zA-Z_]+");
                 }
             });
             for (File subdir : subdirs) {
                 getLog().info("Adding source package for Javadoc processing: " + subdir.getName() + ".*");
                 packageNames.add(subdir.getName());
             }
         }
 
         // Finally, do the work
         return new XHTMLTemplateJavadocPipeline(baseDirectory, packageNames);
     }
 
     public void copyManualResources(File destination) throws IOException {
 
         final List<File> manualResources = new ArrayList();
         getLog().info("Searching for manual resources to copy in: " + manualSourceDirectory);
         File[] files = manualSourceDirectory.listFiles(new FileFilter() {
             public boolean accept(File file) {
                 // Do not copy any .xhtml files, we assume that they all are "included" within the generated manual
                 return !file.getName().endsWith(".xhtml");
             }
         });
         if (files != null)
             manualResources.addAll(Arrays.asList(files));
 
         for (File manualResource : manualResources) {
 
             if (manualResource.getName().startsWith(".")) continue;
 
             if (manualResource.isDirectory()) {

                 // Copy the directory only if it contains any non-XHTML files
                 for (File file : manualResource.listFiles()) {
                     if (file.getName().startsWith(".")) continue;
 
                     if (!file.getName().endsWith(".xhtml")) {
                        getLog().info("Copying directory recursively: " + manualResource);
                         FileUtils.copyDirectoryStructure(manualResource, new File(destination, manualResource.getName()));
                         break;
                     }
                 }
             } else {
 
                 getLog().info("Copying file: " + manualResource);
 
                 FileUtils.copyFile(manualResource, new File(destination, manualResource.getName()));
             }
         }
     }
 
     public void copyDocFiles(File destination) throws IOException {
 
         final List<File> docFiles = new ArrayList();
         getLog().info("Searching for doc-files to copy in: " + testSourceDirectory);
         IO.findFiles(testSourceDirectory, new IO.FileFinder() {
             public void found(File file) {
                 if (file.isDirectory() && file.getName().equals("doc-files")) {
                     File[] children = file.listFiles();
                     if (children != null) docFiles.addAll(Arrays.asList(children));
                 }
             }
         });
 
         File destinationDir = new File(destination, "doc-files");
         if (docFiles.size() > 0 && !destinationDir.exists()) {
             destinationDir.mkdir();
         } else if (docFiles.size() > 0 && destinationDir.exists()) {
             getLog().info("Cleaning old doc-files target directory: " + destinationDir);
             FileUtils.deleteDirectory(destinationDir);
             destinationDir.mkdir();
         }
 
         for (File docFile : docFiles) {
 
             if (docFile.getName().startsWith(".")) continue;
 
             File targetDocFile = new File(destinationDir, docFile.getName());
             if (targetDocFile.exists()) {
                 throw new IOException("Duplicate doc-files detected, rename one: " + docFile.getName());
             }
 
             getLog().info("Copying doc-file to: " + targetDocFile);
             FileUtils.copyFile(docFile, targetDocFile);
         }
 
 
     }
 
 }
