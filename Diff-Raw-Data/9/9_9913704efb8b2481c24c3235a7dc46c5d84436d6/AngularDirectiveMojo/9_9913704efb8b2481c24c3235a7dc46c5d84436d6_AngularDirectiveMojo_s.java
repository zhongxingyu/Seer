 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements. See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership. The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License. You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied. See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 package be.rubus.angularprime.build.maven;
 
 import com.google.javascript.jscomp.CompilationLevel;
 import com.google.javascript.jscomp.WarningLevel;
 import org.apache.maven.model.Resource;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.shared.filtering.MavenResourcesFiltering;
 import org.codehaus.plexus.util.FileUtils;
 import org.primefaces.extensions.optimizerplugin.*;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.Serializable;
 import java.util.*;
 
 /**
  * Generates (combines) and optimize the JavaScript and CSS files for AngularPrime.
  *
  * @goal generate-optimize-resources
  * @phase process-resources
  */
 public class AngularDirectiveMojo extends AbstractMojo {
 
     private static final String[] DEFAULT_INCLUDES = {"**/*.css", "**/*.js"};
 
     private static final String[] DEFAULT_EXCLUDES = {};
 
     // Location of the output
     private static final String PRODUCTION_LOCATION = "/../build/production";
 
     private static final String DEVELOPMENT_LOCATION = "/../build/development";
 
 
     /**
      * The maven project.
      *
      * @parameter expression="${project}"
      * @readonly
      */
     private MavenProject project;
 
     /**
      * @component role="org.apache.maven.shared.filtering.MavenResourcesFiltering" role-hint="default"
      * @required
      */
     protected MavenResourcesFiltering mavenResourcesFiltering;
 
     /**
      * Encoding to read files.
      *
      * @parameter expression="${encoding}" default-value="UTF-8"
      * @required
      */
     private String encoding;
 
     /**
      * Compilation level for Google Closure Compiler.
      *
      * @parameter expression="${compilationLevel}" default-value="SIMPLE_OPTIMIZATIONS"
      */
     private String compilationLevel;
 
     /**
      * Warning level for Google Closure Compiler.
      *
      * @parameter expression="${warningLevel}" default-value="QUIET"
      */
     private String warningLevel;
 
     /**
      * The library name is the filename for the aggregated resources..
      *
      * @parameter expression="${library}"
      * @required
      */
     private String library;
 
     /**
      * when release is set to 'true', production build is performed.
      *
      * @parameter expression="${release}" default-value="false"
      * @required
      */
     private String release;
 
     private File sourceDirectory;
 
     private List<String> directoriesToProcess;
 
 
     private DataUriTokenResolver dataUriTokenResolver;
 
     private String imagesDir;
 
     /**
      * Main entry point of the plugin.
      *
      * @throws MojoExecutionException
      * @throws MojoFailureException
      */
     public void execute() throws MojoExecutionException, MojoFailureException {
         getLog().info("Creation of " + library + " resource artifacts started ...");
 
         imagesDir = project.getBasedir().getAbsolutePath() + "/src/main/resources/META-INF/resources";
         sourceDirectory = new File(((Resource) project.getResources().get(0)).getDirectory());
 
         buildForDevelopment();
         if (Boolean.valueOf(release)) {
             buildForProduction();
         }
     }
 
     private void buildForDevelopment() throws MojoExecutionException {
         File buildDirectory = new File(project.getBuild().getOutputDirectory() + DEVELOPMENT_LOCATION);
 
         // Determine what directories needs to be processed.
         determineDirectories(buildDirectory);
 
         // Process the widgets that are updated
         processResources(sourceDirectory, buildDirectory, true);
 
         useDevelopmentResources(buildDirectory);
     }
 
     private void buildForProduction() throws MojoExecutionException {
         File buildDirectory = new File(project.getBuild().getOutputDirectory() + PRODUCTION_LOCATION);
 
         // Determine what directories needs to be processed.
         determineDirectories(buildDirectory);
 
         // Process the widgets
         processResources(sourceDirectory, buildDirectory, false);
     }
 
     private void processResources(File inputDirectory, File buildDirectory, boolean withoutCompress) throws
             MojoExecutionException {
 
         ResourcesScanner scanner = new ResourcesScanner();
         scanner.scan(inputDirectory, DEFAULT_INCLUDES, DEFAULT_EXCLUDES);
 
         Aggregation aggr = new Aggregation();
         aggr.setWithoutCompress(withoutCompress);
 
         for (String directory : directoriesToProcess) {
             getLog().info("Processing " + directory);
 
             ResourcesScanner subDirScanner = new ResourcesScanner();
             subDirScanner.scan(new File(sourceDirectory, directory), DEFAULT_INCLUDES, DEFAULT_EXCLUDES);
 
             File targetDir = new File(buildDirectory, directory);
             File inputDir = new File(inputDirectory, directory);
 
             Set<File> subDirCssFiles = filterSubDirFiles(scanner.getCssFiles(), subDirScanner.getCssFiles());
             if (!subDirCssFiles.isEmpty()) {
 
                 // handle CSS files
                 processCssFiles(inputDir, subDirCssFiles, getDataUriTokenResolver(), getSubDirAggregation(targetDir,
                         aggr, ResourcesScanner.CSS_FILE_EXTENSION), null);
             }
 
             Set<File> subDirJsFiles = filterSubDirFiles(scanner.getJsFiles(), subDirScanner.getJsFiles());
 
             if (!subDirJsFiles.isEmpty()) {
                 // handle JavaScript files
 
                 processJsFiles(inputDir, subDirJsFiles, getCompilationLevel(compilationLevel),
                         getWarningLevel(warningLevel), getSubDirAggregation(targetDir, aggr,
                         ResourcesScanner.JS_FILE_EXTENSION), null);
 
             }
         }
 
     }
 
     /**
      * Determines those directories Copies the JavaScript and CSS files from src\main\resources directory to the
      * build directory.
      *
      * @param buildDirectory Build directory location
      * @return
      * @throws MojoExecutionException
      */
     private void determineDirectories(File buildDirectory) throws MojoExecutionException {
 
         directoriesToProcess = UpdatedResources.determineDirectories(buildDirectory, sourceDirectory);
         if (!directoriesToProcess.isEmpty()) {
             addGlobalAggregationDirectories();
         }
     }
 
     private void addGlobalAggregationDirectories() {
         String[] dirParts;
         Set<String> uniquePartNames = new HashSet<String>();
 
         for (String dirName : directoriesToProcess) {
            dirParts = dirName.split("\\\\");
             uniquePartNames.add(dirParts[1]);
         }
 
         for (String globalDirame : uniquePartNames) {
 
            directoriesToProcess.add(globalDirame + '\\');
         }
     }
 
 
     /**
      * Copy the files from the build directory to the web application location.
      *
      * @param buildDirectory The build location
      */
     private void useDevelopmentResources(File buildDirectory) {
         File webAppDirectory = new File(project.getBasedir(), "/src/main/webapp");
         File targetDirectory;
         try {
             List<String> names = FileUtils.getFileNames(buildDirectory, "**/*.css,**/*.js", null, true);
             for (String fileName : names) {
                 String normalized = FileUtils.normalize(fileName);
                 List<String> parts = extractImportantParts(normalized);
 
                 //parts.add(1, "angularPrime");
                 String targetDir = determineTargetLocation(parts, false);
 
                 targetDirectory = new File(webAppDirectory, targetDir);
                 FileUtils.copyFileToDirectory(fileName, targetDirectory.getAbsolutePath());
             }
 
         } catch (IOException e) {
             throw new IllegalArgumentException(e);
         }
 
     }
 
     private String determineTargetLocation(List<String> parts, boolean singleResource) {
 
         if (!singleResource) {
             parts.remove(0);
             parts.add(parts.size() - 1, "libs");
         }
         StringBuilder result = new StringBuilder();
         for (int idx = parts.size(); idx > 0; idx--) {
             result.append('/').append(parts.get(idx - 1));
         }
         return result.toString();
     }
 
     private List<String> extractImportantParts(String normalized) {
         List<String> result = new ArrayList<String>();
         File f = new File(normalized);
 
         while (!("development".equals(f.getName()) || "production".equals(f.getName()))) {
             result.add(f.getName());
             f = f.getParentFile();
         }
         return result;
     }
 
     private Aggregation getSubDirAggregation(final File dir, final Aggregation aggr, final String fileExtension) {
         Aggregation subDirAggr = new Aggregation();
         subDirAggr.setPrependedFile(aggr.getPrependedFile());
         subDirAggr.setWithoutCompress(aggr.isWithoutCompress());
         subDirAggr.setSubDirMode(true);
         subDirAggr.setRemoveIncluded(false);
 
         File outputFile;
         if (dir.getName().equals("js") || dir.getName().equals("css")) {
             String libraryWithVersion = library + "-" + project.getModel().getVersion();
             outputFile = new File(dir, libraryWithVersion + "/" + libraryWithVersion + "." + fileExtension);
         } else {
             outputFile = new File(dir, dir.getName() + "." + fileExtension);
         }
 
         subDirAggr.setOutputFile(outputFile);
 
         return subDirAggr;
     }
 
     private CompilationLevel getCompilationLevel(final String compilationLevel) throws MojoExecutionException {
         try {
             return CompilationLevel.valueOf(compilationLevel);
         } catch (Exception e) {
             final String errMsg = "Compilation level '" + compilationLevel + "' is wrong. Valid constants are: " +
                     "'WHITESPACE_ONLY', 'SIMPLE_OPTIMIZATIONS', 'ADVANCED_OPTIMIZATIONS'";
             throw new MojoExecutionException(errMsg);
 
         }
     }
 
     private WarningLevel getWarningLevel(final String warningLevel) throws MojoExecutionException {
         try {
             return WarningLevel.valueOf(warningLevel);
         } catch (Exception e) {
             final String errMsg = "Warning level '" + warningLevel + "' is wrong. Valid constants are: 'QUIET', " +
                     "'DEFAULT', 'VERBOSE'";
             throw new MojoExecutionException(errMsg);
         }
     }
 
     private void processCssFiles(final File inputDir, final Set<File> cssFiles,
                                  final DataUriTokenResolver dataUriTokenResolver, final Aggregation aggr,
                                  final String suffix) throws MojoExecutionException {
         ResourcesSetAdapter rsa = new ResourcesSetCssAdapter(inputDir, cssFiles, dataUriTokenResolver, aggr,
                 encoding, true, suffix);
 
         YuiCompressorOptimizer yuiOptimizer = new YuiCompressorOptimizer();
         yuiOptimizer.optimize(rsa, getLog());
 
     }
 
     private void processJsFiles(final File inputDir, final Set<File> jsFiles,
                                 final CompilationLevel compilationLevel, final WarningLevel warningLevel,
                                 final Aggregation aggr, final String suffix) throws MojoExecutionException {
 
         ResourcesSetAdapter rsa = new ResourcesSetJsAdapter(inputDir, jsFiles, compilationLevel, warningLevel, aggr,
                 encoding, true, suffix);
 
         ClosureCompilerOptimizer closureOptimizer = new ClosureCompilerOptimizer();
         closureOptimizer.optimize(rsa, getLog());
 
     }
 
     private static Set<File> filterSubDirFiles(final Set<File> resSetFiles, final Set<File> subDirFiles) {
         Set<File> filteredFiles = new TreeSet<File>(new DependencyFileOrdering());
 
         if (subDirFiles == null || subDirFiles.isEmpty() || resSetFiles == null || resSetFiles.isEmpty()) {
             return filteredFiles;
         }
 
         for (File subDirFile : subDirFiles) {
             if (resSetFiles.contains(subDirFile)) {
                 filteredFiles.add(subDirFile);
             }
         }
 
         return filteredFiles;
     }
 
     private DataUriTokenResolver getDataUriTokenResolver() {
         if (dataUriTokenResolver != null) {
             return dataUriTokenResolver;
         }
 
         String[] arrImagesDir = imagesDir.split(",");
         File[] fileImagesDir = new File[arrImagesDir.length];
         for (int i = 0; i < arrImagesDir.length; i++) {
             fileImagesDir[i] = new File(arrImagesDir[i]);
         }
 
         dataUriTokenResolver = new DataUriTokenResolver(fileImagesDir);
 
         return dataUriTokenResolver;
     }
 
 
     private static class DependencyFileOrdering implements Comparator<File>, Serializable {
         public int compare(File f1, File f2) {
             int result = f1.getParentFile().getName().compareTo(f2.getParentFile().getName());
             if (result == 0) {
                 result = f1.getName().compareTo(f2.getName());
             }
             return result;
         }
 
     }
 
 }
