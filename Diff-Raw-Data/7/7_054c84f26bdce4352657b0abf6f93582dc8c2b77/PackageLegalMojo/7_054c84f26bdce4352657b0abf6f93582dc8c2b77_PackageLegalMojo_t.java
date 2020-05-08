 /**
  * Licensed to Jasig under one or more contributor license
  * agreements. See the NOTICE file distributed with this work
  * for additional information regarding copyright ownership.
  * Jasig licenses this file to you under the Apache License,
  * Version 2.0 (the "License"); you may not use this file
  * except in compliance with the License. You may obtain a
  * copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on
  * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.jasig.maven.legal;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.List;
 
 import org.apache.maven.artifact.DependencyResolutionRequiredException;
 import org.apache.maven.model.Build;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.FileUtils;
 import org.codehaus.plexus.util.io.URLInputStreamFacade;
 import org.jasig.maven.legal.util.ResourceFinder;
 
 /**
  * Copies the LICENSE and NOTICE files from the project's basedir or a parent project's basedir into META-INF
  * of the generated artifact.  
  * 
  * @author Eric Dalquist
  * @version $Revision$
  * @goal copy-files
  * @threadSafe true
  * @phase prepare-package
  * @inheritByDefault true
  */
 public class PackageLegalMojo extends AbstractMojo {
     
     /* DI configuration of Maven components needed for the plugin */
 
     /**
      * The Maven Project.
      *
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     protected MavenProject project;
     
     /**
      * Name of NOTICE file to copy. Will search ${basedir} and parent project's ${basedir}.
      * Absolute and URL paths can be specified as well. 
      *
      * @parameter default-value="NOTICE"
      */
     protected String noticeFile = "NOTICE";
     
     /**
      * Name of LICENSE file to copy. Will search ${basedir} and parent project's ${basedir}.
      * Absolute and URL paths can be specified as well.
      *
      * @parameter default-value="LICENSE"
      */
     protected String licenseFile = "LICENSE";
     
     /**
      * Location under ${project.build.outputDirectory} that the NOTICE and LICENSE files
      * should be placed.
      *
      * @parameter default-value="META-INF"
      */
     protected String targetDirectory = "META-INF";
     
     
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         final Log logger = this.getLog();
         
         if ("pom".equals(project.getPackaging())) {
             logger.info("Skipping inclusion of legal files for pom project: " + project.getName());
             return;
         }
         
         final File targetOutputDir = this.getTargetDirectory();
         
         final ResourceFinder resourceFinder = this.getResourceFinder();
         this.copyLegalFile(resourceFinder, this.noticeFile, targetOutputDir, "NOTICE");
         this.copyLegalFile(resourceFinder, this.licenseFile, targetOutputDir, "LICENSE");
        logger.info("Copied NOTICE and LICENSE to: " + targetOutputDir);
     }
 
     /**
     * Copy a file to the specified directory
      */
     protected void copyLegalFile(ResourceFinder resourceFinder, String legalFile, File targetOutputDir, String outputFileName) throws MojoFailureException {
         final Log logger = this.getLog();
         
         final URL resourceUrl = resourceFinder.findResource(legalFile);
         final File destFile = new File(targetOutputDir, outputFileName);
         try {
             FileUtils.copyStreamToFile(new URLInputStreamFacade(resourceUrl), destFile);
         }
         catch (IOException e) {
             throw new MojoFailureException("Failed to copy '" + resourceUrl + "' to '" + destFile + "'", e);
         }
         
        logger.debug("Copied '" + resourceUrl + "' to '" + destFile + "'");
     }
 
     /**
      * @return The target directory under ${project.build.outputDirectory}
      */
     protected File getTargetDirectory() {
         final Build build = project.getBuild();
         final String outputDirectory = build.getOutputDirectory();
         final File outputDirectoryFile = new File(outputDirectory);
         return new File(outputDirectoryFile, this.targetDirectory);
     }
 
     /**
      * Create the {@link ResourceFinder} for the project
      */
     @SuppressWarnings("unchecked")
     protected ResourceFinder getResourceFinder() throws MojoExecutionException {
         final ResourceFinder finder = new ResourceFinder(this.project);
         try {
             final List<String> classpathElements = this.project.getCompileClasspathElements();
             finder.setCompileClassPath(classpathElements);
         }
         catch (DependencyResolutionRequiredException e) {
             throw new MojoExecutionException(e.getMessage(), e);
         }
         finder.setPluginClassPath(getClass().getClassLoader());
         return finder;
     }
 }
