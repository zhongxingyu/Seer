 /**
  * Copyright (C) 2012 - present by OpenGamma Inc. and the OpenGamma group of companies
  * 
  * Please see distribution for license.
  */
 package com.opengamma.maven;
 
 import static org.twdata.maven.mojoexecutor.MojoExecutor.artifactId;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.executeMojo;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.executionEnvironment;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.goal;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.groupId;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.plugin;
 import static org.twdata.maven.mojoexecutor.MojoExecutor.version;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 
 import org.apache.maven.execution.MavenSession;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.BuildPluginManager;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.util.xml.Xpp3Dom;
 import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
 import org.codehaus.plexus.util.xml.pull.XmlPullParserException;
 
 /**
  * Abstract class for running an OpenGamma component server.
  */
 public abstract class AbstractServerMojo extends AbstractMojo {
 
   /**
    * The configuration file to pass to the component server.
    * @parameter alias="config" property="configFile"
    * @required
    */
   private String configFile; // CSIGNORE
   /**
    * The log level for startup of component server logging.
    * Set to 'ERROR', 'WARN', 'INFO' or 'DEBUG'. Default 'WARN'.
    * @parameter alias="startupLogging" property="startupLogging" default-value="WARN"
    */
   private String startupLogging; // CSIGNORE
   /**
    * The log level for component server logging, or a logback config file.
    * Set to 'ERROR', 'WARN', 'INFO', 'DEBUG' or the path to a file. Default 'WARN'.
    * If used, the file path would typically be something like 'com/opengamma/util/warn-logback.xml'.
    * @parameter alias="serverLogging" property="serverLogging" default-value="WARN"
    */
   private String serverLogging; // CSIGNORE
   /**
    * Any VM memory/GC args to pass to the component server.
    * These are separated from vmArgs to allow default settings for Xms, Xmx and MaxPermSize.
    * Default value '-Xms1024m -Xmx4096m -XX:MaxPermSize=512M'.
    * @parameter alias="vmMemoryArgs" property="vmMemoryArgs" default-value="-Xms1024m -Xmx4096m -XX:MaxPermSize=512M"
    */
   private String vmMemoryArgs; // CSIGNORE
   /**
    * Any additional VM args to pass to the component server.
    * @parameter alias="vmArgs" property="vmArgs"
    */
   private String vmArgs; // CSIGNORE
 
   /**
    * @parameter default-value="${project}"
    * @required
    * @readonly
    */
   private MavenProject project;  // CSIGNORE
   /**
    * The current Maven session.
    *
    * @parameter default-value="${session}"
    * @required
    * @readonly
    */
   private MavenSession mavenSession;  // CSIGNORE
   /**
    * The Maven BuildPluginManager component.
    *
    * @component
    * @required
    */
   private BuildPluginManager mavenPluginManager;  // CSIGNORE
 
   //-------------------------------------------------------------------------
   @Override
   public void execute() throws MojoExecutionException, MojoFailureException {
     // smart interpretation of configFile
     if (configFile.startsWith("file:") == false && configFile.startsWith("classpath:") == false) {
       File file = new File(configFile);
       if (file.exists()) {
         configFile = "file:" + configFile;
       } else {
         configFile = "classpath:" + configFile;
       }
     }
     
     // build arguments
     String fullAppArgs = buildApplicationArguments();
     String fullVmArgs = buildVmArguments();
     String fullArgs = fullVmArgs + " com.opengamma.component.OpenGammaComponentServer " + fullAppArgs;
     getLog().info("Running component server: " + fullArgs);
     if (isSpawn()) {
       fullVmArgs += "-Dcommandmonitor.secret=OpenGammaMojo ";
     }
     
     // build the configuration for ant
     // parse XML manually, as per https://github.com/TimMoore/mojo-executor/issues/10
     String cp = MojoUtils.calculateRuntimeClasspath(project);
     String taskStr = 
         "<configuration>" +
           "<target>" +
             "<java classpath='" + cp + "' classname='com.opengamma.component.OpenGammaComponentServer' fork='true' spawn='" + isSpawn() + "'>" +
               "<jvmarg line='" + fullVmArgs + "' />" +
               "<arg line='" + fullAppArgs + "' />" +
             "</java>" +
             "<echo>Server starting...</echo>" +
           "</target>" +
         "</configuration>";
     Xpp3Dom config;
     try {
       config = Xpp3DomBuilder.build(new StringReader(taskStr));
     } catch (XmlPullParserException | IOException ex) {
       throw new MojoExecutionException("Unable to parse XML configuration: ", ex);
     }
     
     // run the java process using ant
     // uses antrun, as exec-maven-plugin lacks features, eg http://jira.codehaus.org/browse/MEXEC-113
     executeMojo(
       plugin(
         groupId("org.apache.maven.plugins"),
         artifactId("maven-antrun-plugin"),
         version("1.7")
       ),
       goal("run"),
       config,
       executionEnvironment(
         project,
         mavenSession,
         mavenPluginManager
       )
     );
   }
 
   protected abstract boolean isSpawn();
 
   //-------------------------------------------------------------------------
   private String buildApplicationArguments() throws MojoExecutionException {
     String simpleArgs = "";
     switch (startupLogging) {
       case "ERROR":
        simpleArgs += "-v ";
         break;
       case "WARN":
       case "INFO":
         break;
       case "DEBUG":
        simpleArgs += "-q ";
         break;
       default:
         throw new MojoExecutionException("Invalid value for startupLogging: " + startupLogging);
     }
     simpleArgs += configFile;
     return simpleArgs;
   }
 
   //-------------------------------------------------------------------------
   private String buildVmArguments() throws MojoExecutionException {
     String fullVmArgs = "";
     switch (serverLogging) {
       case "ERROR":
         fullVmArgs += "-Dlogback.configurationFile=com/opengamma/util/error-logback.xml ";
         break;
       case "WARN":
         fullVmArgs += "-Dlogback.configurationFile=com/opengamma/util/warn-logback.xml ";
         break;
       case "INFO":
         fullVmArgs += "-Dlogback.configurationFile=com/opengamma/util/info-logback.xml ";
         break;
       case "DEBUG":
         fullVmArgs += "-Dlogback.configurationFile=com/opengamma/util/debug-logback.xml ";
         break;
       default:
         fullVmArgs += "-Dlogback.configurationFile=" + serverLogging + " ";
         break;
     }
     if (vmMemoryArgs != null) {
       fullVmArgs += vmMemoryArgs + " ";
     }
     if (vmArgs != null) {
       fullVmArgs += vmArgs + " ";
     }
     return fullVmArgs;
   }
 
 }
