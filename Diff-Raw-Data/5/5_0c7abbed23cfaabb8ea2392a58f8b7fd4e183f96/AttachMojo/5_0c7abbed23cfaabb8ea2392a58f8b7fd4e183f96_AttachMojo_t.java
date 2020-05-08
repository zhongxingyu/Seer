 /**
  * Copyright 1&1 Internet AG, https://github.com/1and1/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package net.oneandone.maven.plugins.attachqars;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Locale;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.project.MavenProjectHelper;
 
 /**
  * Attaches a number of hardcoded reports for deployment during install.
  *
  * @author Mirko Friedenhagen <mirko.friedenhagen@1und1.de>
  */
 @Mojo(name = "attach", aggregator = false, defaultPhase = LifecyclePhase.INSTALL)
 public class AttachMojo extends AbstractMojo {
 
     private static class FileToClassifier {
 
         final String fileName;
         final String classifier;
 
         public FileToClassifier(String fileName, String classifier) {
             this.fileName = fileName;
             this.classifier = classifier;
         }
 
         public static FileToClassifier create(String fileName, String classifier) {
             return new FileToClassifier(fileName, classifier);
         }
     }
     private final static List<FileToClassifier> fileToClassifiers = Arrays.asList(
             FileToClassifier.create("checkstyle-result.xml", "checkstyle-report"),
             FileToClassifier.create("pmd.xml", "pmd-report"),
             FileToClassifier.create("cpd.xml", "cpd-report"),
             FileToClassifier.create("findbugsXml.xml", "findbugs-report"),
             FileToClassifier.create("site/jacoco/jacoco.xml", "jacoco-report"));
     /**
      * The Maven project.
      */
     @Parameter(defaultValue = "${project}", readonly = true)
     private MavenProject project;
     /**
      * Project Helper for attaching files.
      */
     @Component
     private MavenProjectHelper projectHelper;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         final String projectForLog = String.format(
                 Locale.UK, "%s:%s:%s", project.getGroupId(), project.getArtifactId(), project.getVersion());
         if (project.getPackaging().equals("pom")) {
             getLog().info("Skipping execution for pom project " + projectForLog);
             return;
         }
         final File targetDirectory = new File(project.getBuild().getDirectory());
         for (final FileToClassifier fileToClassifier : fileToClassifiers) {
             final File result = new File(targetDirectory, fileToClassifier.fileName);
             if (result.exists()) {
                 projectHelper.attachArtifact(project, "xml", fileToClassifier.classifier, result);
             } else {
                 getLog().info(result + " not found in " + projectForLog);
             }
         }
     }
 }
