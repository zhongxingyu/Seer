 /*
  * The MIT License (MIT)
  * 
  * Copyright (c) 2013 Michael Sena
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
  * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
  * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
  * Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
  * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
  * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
  * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
  */
 
 package com.github.mikesena.maven.plugins.environments;
 
 // CHECKSTYLE SUPPRESS AvoidStaticImport FOR 6 LINES
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.BuildPluginManager;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugins.annotations.Component;
 import org.apache.maven.plugins.annotations.Execute;
 import org.apache.maven.plugins.annotations.LifecyclePhase;
 import org.apache.maven.plugins.annotations.Mojo;
 import org.apache.maven.plugins.annotations.Parameter;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.FileUtils;
 import org.twdata.maven.mojoexecutor.MojoExecutor;
 
 /**
  * The create-environment-configurations goal takes a directory as a template and creates a version related to a
  * particular environment, based on the contents of a properties file.
  * 
  * @author Michael Sena
  * @threadSafe
  */
 @Execute(goal = "create-environment-configurations", phase = LifecyclePhase.GENERATE_RESOURCES)
 @Mojo(name = "create-environment-configurations", defaultPhase = LifecyclePhase.GENERATE_RESOURCES, threadSafe = true)
 public final class CreateEnvironmentConfigurationsMojo extends AbstractMojo {
 
     /** Suffix used for the properties files. */
     private static final String PROPERTIES_FILE_SUFFIX = ".properties";
 
     /** Directory, containing property files with common properties in them. */
     @Parameter
     private File commonPropertiesDirectory;
 
     /** Performs a check, so that all environment files can guaranteed to all have the same proprety keys. */
     @Parameter(required = false, defaultValue = "true")
     private boolean enforcePropertiesMustExist;
 
     /** List of environments that will be created. */
     @Parameter(required = true)
     private String[] environments;
 
     /** Whether directory & file names are filtered (allows files to be renamed based on a property). */
     @Parameter(required = false, defaultValue = "true")
     private boolean filterOnFilenames;
 
     /** Used when verifying consistency & completeness between environment files. */
     private Properties initialEnvironmentProperties;
 
     /** Directory to create the environments. */
     @Parameter(required = true, defaultValue = "${project.build.directory}/environments")
     private File outputDirectory;
 
     /** Whether to override files that already exist when creating an environment. */
     @Parameter(required = false, defaultValue = "true")
     private boolean overrideIfExists;
 
     /** Maven component that manages plugins for this session. */
     @Component(role = BuildPluginManager.class)
     private BuildPluginManager pluginManager;
 
     /** Maven component, representing the project being built. */
     @Component(role = MavenProject.class)
     private MavenProject project;
 
     /** Input directory, containing all the environment property files. */
     @Parameter(required = true, defaultValue = "${basedir}/src/main/properties")
     private File propertiesDirectory;
 
     /** Maven component, that controls the session. */
     @Component(role = MavenSession.class)
     private MavenSession session;
 
     /** Directory containing the environment template. */
     @Parameter(required = true, defaultValue = "${basedir}/src/main/environment-template")
     private File templateDirectory;
 
     @Override
     public void execute() throws MojoExecutionException, MojoFailureException {
         verifyDirectoryParameters(commonPropertiesDirectory, propertiesDirectory, templateDirectory);
         getLog().info("Creating configurations for " + environments.length + " environment(s).");
         loadCommonProperties();
         for (final String environment : environments) {
             createEnvironment(environment);
         }
     }
 
     private void checkProperties(final Properties newProps) throws MojoExecutionException {
         final Properties clonedProps = (Properties) newProps.clone();
         if (initialEnvironmentProperties == null) {
             initialEnvironmentProperties = clonedProps;
         } else {
             compareProperties(clonedProps);
         }
     }
 
     private void compareProperties(final Properties newProps) throws MojoExecutionException {
         final Set<Object> newKeys = newProps.keySet();
         final List<Object> missingKeys = new ArrayList<>();
         for (final Object key : initialEnvironmentProperties.keySet()) {
             if (newKeys.contains(key)) {
                 newKeys.remove(key);
             } else {
                 missingKeys.add(" > " + key);
             }
         }
         for (final Object newKey : newKeys) {
             missingKeys.add(" < " + newKey);
         }
 
         for (final Object key : missingKeys) {
             final String message = "Missing key: " + key;
             if (enforcePropertiesMustExist) {
                 getLog().error(message);
             } else {
                 getLog().warn(message);
             }
         }
 
         if ((missingKeys.size() > 0) && enforcePropertiesMustExist) {
             throw new MojoExecutionException("Environment files must be matching in which properties they include.");
         }
     }
 
     private void copyResources(final File environmentOutputDirectory) throws MojoExecutionException {
         getLog().info("Copying across environment files.");
         MojoExecutor.executeMojo(MojoExecutor.plugin("org.apache.maven.plugins", "maven-resources-plugin", "2.6"),
                         MojoExecutor.goal("copy-resources"), MojoExecutor.configuration(MojoExecutor.element(
                                         "outputDirectory", environmentOutputDirectory.getPath()), MojoExecutor.element(
                                         "resources", MojoExecutor.element("resource", MojoExecutor.element("directory",
                                                         templateDirectory.getPath()), MojoExecutor.element("filtering",
                                                         "true")))), MojoExecutor.executionEnvironment(project, session,
                                         pluginManager));
 
         // Rename files with filtering
        if (filterOnFilenames) {
            getLog().info("Filtering on filenames.");
            MavenFilteringUtil.doFilenameFiltering(environmentOutputDirectory, project.getProperties());
        }
     }
 
     private void createEnvironment(final String environment) throws MojoExecutionException {
         getLog().info("Creating environment: " + environment);
         final Properties environmentProperties = getEnvironmentProperties(environment);
         checkProperties(environmentProperties);
         final Properties originalProperties = (Properties) project.getProperties().clone();
         final File environmentOutputDirectory = new File(outputDirectory, environment);
         if (environmentOutputDirectory.exists()) {
             if (overrideIfExists) {
                 try {
                     FileUtils.deleteDirectory(environmentOutputDirectory);
                 } catch (final IOException e) {
                     throw new MojoExecutionException("Unable to delete existing environment output directory: "
                                     + environmentOutputDirectory.getPath(), e);
                 }
             } else {
                 getLog().warn("Environment Exists; skipping: " + environment);
                 return;
             }
         }
         project.getProperties().putAll(environmentProperties);
         copyResources(environmentOutputDirectory);
         resetProperties(originalProperties);
     }
 
     private Properties getEnvironmentProperties(final String environment) throws MojoExecutionException {
         getLog().debug("Reading properties file for environment: " + environment);
         final File propertiesFile = new File(propertiesDirectory, environment + PROPERTIES_FILE_SUFFIX);
         final Properties properties = new Properties();
         try {
             properties.load(new FileInputStream(propertiesFile));
         } catch (final FileNotFoundException e) {
             throw new MojoExecutionException("Missing required file: " + propertiesFile.getPath(), e);
         } catch (final IOException e) {
             throw new MojoExecutionException("Unable to read properties file: " + propertiesFile.getPath(), e);
         }
         return properties;
     }
 
     private void loadCommonProperties() throws MojoExecutionException {
         final Properties properties = new Properties();
         if ((commonPropertiesDirectory != null) && commonPropertiesDirectory.exists()) {
             try {
                 for (final File file : commonPropertiesDirectory.listFiles()) {
                     if (!file.isDirectory() && file.getName().endsWith(PROPERTIES_FILE_SUFFIX)) {
                         properties.load(new FileInputStream(file));
                     }
                 }
             } catch (final IOException e) {
                 throw new MojoExecutionException("Unable to load common properties files.", e);
             }
         }
         project.getProperties().putAll(properties);
     }
 
     private void resetProperties(final Properties originalProperties) {
         getLog().debug("Resetting properties back to their original.");
         project.getProperties().clear();
         project.getProperties().putAll(originalProperties);
     }
 
     /**
      * Utility function, to check provided File parameters exist and are directories, not files. Null values are passed,
      * as they should reflect nullable/optional parameters, but should be checked if provided.
      * 
      * @param directories
      *            List of {@link File}s to check.
      * @throws MojoExecutionException
      *             If a directory could not be found.
      */
     private void verifyDirectoryParameters(final File... directories) throws MojoExecutionException {
         for (final File directory : directories) {
             if ((directory != null) && (!directory.exists() || directory.isFile())) {
                 throw new MojoExecutionException("Unable to find directory: " + directory);
             }
         }
     }
 }
