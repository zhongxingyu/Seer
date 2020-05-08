 /*
  * Copyright 2012 Amadeus s.a.s.
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.ariatemplates.atjstestrunner.maven;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.maven.model.Dependency;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 
 public abstract class RunATJSTestRunner extends RunNode {
 
     /**
      * @parameter
      */
     public File configFile;
 
     /**
      * @parameter
      *            expression="${project.build.directory}/${project.build.finalName}"
      */
     public File webappDirectory;
 
     /**
      * @parameter expression="aria/aria-templates-${at.version}.js"
      */
     public String ariaTemplatesBootstrap;
 
     /**
      * @parameter
      */
     public String[] ariaTemplatesClasspathsIncludes = new String[] { "MainTestSuite" };
 
     /**
      * @parameter
      */
     public String[] ariaTemplatesClasspathsExcludes;
 
     /**
      * @parameter expression="${project.build.directory}/jstestdriver"
      */
     public File xmlReportsDirectory;
 
     /**
      * @parameter expression="${project.build.directory}/atjstestsReport.xml"
      */
     public File xmlReportFile;
 
     /**
      * @parameter expression="${project.build.directory}/atjstestsReport.json"
      */
     public File jsonReportFile;
 
     /**
      * @parameter
      *            expression="${project.build.directory}/atjstestsCoverageReport.json"
      */
     public File jsonCoverageReportFile;
 
     /**
      * @parameter expression=
      *            "${project.build.directory}/jstestdriver/jsTestDriver.conf-coverage.dat"
      */
     public File lcovCoverageReportFile;
 
     /**
      * @parameter expression="${com.ariatemplates.atjstestrunner.path}"
      */
     public File atjstestrunnerPath;
 
     /**
      * @parameter
      */
     public Integer port;
 
     /**
      * @parameter
      */
     public boolean colors = false;
 
     private static final String PATH_IN_ATJSTESTRUNNER_DIRECTORY = "bin" + File.separator + "atjstestrunner.js";
 
     protected File atjstestrunnerJsMainFile;
     protected File phantomjsExecutable;
 
     public static Dependency getATJSTestRunnerDependency() {
         Dependency dependency = new Dependency();
         dependency.setGroupId("com.ariatemplates.atjstestrunner");
         dependency.setArtifactId("atjstestrunner-nodejs");
         dependency.setVersion(RunATJSTestRunner.class.getPackage().getImplementationVersion());
         dependency.setClassifier("project");
         dependency.setType("zip");
         return dependency;
     }
 
     protected File extractDependency(File property, Dependency dependency, String pathAfterProperty, String pathAfterDependency) {
         File res;
         try {
             if (property != null) {
                 res = new File(property, pathAfterProperty);
             } else {
                 ArtifactExtractor extractor = new ArtifactExtractor();
                 extractor.setLog(this.getLog());
                 String outputDirectory = extractor.inplaceExtractDependency(session.getLocalRepository(), dependency);
                 res = new File(outputDirectory, pathAfterDependency);
             }
             if (!res.exists()) {
                 throw new FileNotFoundException("Could not find file: " + res.getAbsolutePath());
             }
         } catch (Exception e) {
             throw new RuntimeException("Failed to find or extract " + dependency.getArtifactId(), e);
         }
         return res;
     }
 
     protected void extractATJSTestRunner() {
         atjstestrunnerJsMainFile = extractDependency(atjstestrunnerPath, getATJSTestRunnerDependency(), PATH_IN_ATJSTESTRUNNER_DIRECTORY, "atjstestrunner"
             + File.separator + PATH_IN_ATJSTESTRUNNER_DIRECTORY);
     }
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         extractATJSTestRunner();
         super.execute();
     }
 
     protected void addMultipleOptions(List<String> optionsArray, String optionName, String[] array) {
         if (array != null) {
             for (String item : array) {
                 optionsArray.add(optionName);
                 optionsArray.add(item);
             }
         }
     }
 
     @Override
     protected List<String> getNodeArguments() {
         List<String> res = new LinkedList<String>();
         res.add(atjstestrunnerJsMainFile.getAbsolutePath());
 
         if (configFile != null) {
             res.add(configFile.getAbsolutePath());
         }
 
         if (port != null) {
             res.add("--port");
             res.add(port.toString());
         }
 
         if (!colors) {
             res.add("--no-colors");
         }
 
         res.add("--config.resources./");
         res.add(webappDirectory.getAbsolutePath());
 
         res.add("--config.test-reports.xml-directory");
         res.add(xmlReportsDirectory.getAbsolutePath());
 
         res.add("--config.test-reports.xml-file");
         res.add(xmlReportFile.getAbsolutePath());
 
         res.add("--config.test-reports.json-file");
         res.add(jsonReportFile.getAbsolutePath());
 
         res.add("--config.coverage-reports.json-file");
         res.add(jsonCoverageReportFile.getAbsolutePath());
 
         res.add("--config.coverage-reports.lcov-file");
         res.add(lcovCoverageReportFile.getAbsolutePath());
 
        res.add("--config.test.aria-templates.bootstrap");
         res.add(ariaTemplatesBootstrap);
 
        addMultipleOptions(res, "--config.test.aria-templates.files.includes", ariaTemplatesClasspathsIncludes);
        addMultipleOptions(res, "--config.test.aria-templates.files.excludes", ariaTemplatesClasspathsExcludes);
 
         addExtraAtjstestrunnerOptions(res);
 
         res.addAll(arguments);
 
         return res;
     }
 
     protected void addExtraAtjstestrunnerOptions(List<String> list) {
     }
 
 }
