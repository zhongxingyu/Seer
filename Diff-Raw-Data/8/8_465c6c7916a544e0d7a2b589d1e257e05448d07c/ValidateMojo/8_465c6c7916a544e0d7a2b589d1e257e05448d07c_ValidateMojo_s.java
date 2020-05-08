 /**
  * This file is part of the XP-Framework
  *
  * Maven plugin for XP-Framework
  * Copyright (c) 2011, XP-Framework Team
  */
 package net.xp_forge.maven.plugins.xp;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.FileNotFoundException;
 import java.util.List;
 import java.util.Calendar;
 import java.util.ArrayList;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.project.MavenProject;
 import org.apache.maven.plugin.MojoExecutionException;
 
 import org.codehaus.plexus.util.StringUtils;
 import org.codehaus.plexus.archiver.UnArchiver;
 
 import net.xp_forge.maven.plugins.xp.util.FileUtils;
 import net.xp_forge.maven.plugins.xp.util.ExecuteUtils;
 import net.xp_forge.maven.plugins.xp.util.ArchiveUtils;
 import net.xp_forge.maven.plugins.xp.ini.IniProperties;
 import static net.xp_forge.maven.plugins.xp.AbstractXpMojo.*;
 
 /**
  * Check for the presence of XP-Framework runners
  *
  * @goal validate
  * @requiresDependencyResolution compile
  */
 public class ValidateMojo extends AbstractXpMojo {
 
   /**
    * {@inheritDoc}
    *
    */
   public void execute() throws MojoExecutionException {
 
     // User clearly specified to use already installed XP-Runtime via ${xp.runtime.local}
     if (this.local) {
       this.setupRuntimeFromLocalInstall();
 
     // Special case: self-bootstrap for compiling XP-Framework itself
     // Setup XP-Runtime using project resources
     } else if (
         this.project.getGroupId().equals(XP_FRAMEWORK_GROUP_ID) &&
         (
           this.project.getArtifactId().equals(CORE_ARTIFACT_ID) ||
          this.project.getArtifactId().equals(TOOLS_ARTIFACT_ID) ||
          this.project.getArtifactId().equals(LANGUAGE_ARTIFACT_ID)
         )
       ) {
       this.setupRuntimeFromResources(new File(this.outputDirectory, ".runtime"));
 
     // Setup XP-Runtime using project dependencies
     } else {
       this.setupRuntimeFromDependencies(new File(this.outputDirectory, ".runtime"));
     }
 
     getLog().info("Using runners from [" + this.runnersDirectory + "]");
     this.project.getProperties().setProperty("xp.runtime.runners.directory", this.runnersDirectory.getAbsolutePath());
 
     getLog().info("USE_XP [" + (null == this.use_xp ? "N/A" : this.use_xp) + "]");
    this.project.getProperties().setProperty("xp.runtime.use_xp", this.use_xp);
 
     // Alter default Maven settings
     this.alterSourceDirectories();
   }
 
   /**
    * Use XP-Runtime already installed on local machine
    * by searching for XP-Runners in PATH
    *
    * @return void
    * @throws import org.apache.maven.plugin.MojoExecutionException
    */
   private void setupRuntimeFromLocalInstall() throws MojoExecutionException {
     getLog().debug("Preparing XP-Runtime from local install");
     try {
       this.runnersDirectory= ExecuteUtils.getExecutable("xp").getParentFile();
     } catch (FileNotFoundException ex) {
       throw new MojoExecutionException("Cannot find XP-Framework. Please install it from http://xp-framework.net/", ex);
     }
   }
 
   /**
    * Prepare our own XP-Runtime into specified directory using project dependencies
    *
    * @param  java.io.File targetDirectory
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupRuntimeFromDependencies(File targetDirectory) throws MojoExecutionException {
     getLog().debug("Preparing XP-Runtime from project dependencies into [" + targetDirectory + "]");
 
     // Configure directories
     File bootstrapDirectory = new File(targetDirectory, "bootstrap");
     this.runnersDirectory   = new File(targetDirectory, "runners");
 
     // Setup bootstrap from dependencies
     this.setupBootstrapFromDependencies(bootstrapDirectory);
 
     // Setup XP-Runners
     this.use_xp= bootstrapDirectory.getAbsolutePath();
     this.setupRunners(this.runnersDirectory, this.use_xp);
   }
 
   /**
    * Prepare our own XP-Runtime into specified directory using project resources
    *
    * @param  java.io.File targetDirectory
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupRuntimeFromResources(File targetDirectory) throws MojoExecutionException {
     getLog().debug("Preparing XP-Runtime from project resources into [" + targetDirectory + "]");
 
     // Configure directories
     this.runnersDirectory= new File(targetDirectory, "runners");
 
     // Configure USE_XP from reactor projects
     List<String> reactorRoots= new ArrayList<String>();
     for (MavenProject reactorProject : this.reactorProjects) {
       if (reactorProject.getArtifactId().equals(CORE_ARTIFACT_ID) || reactorProject.getArtifactId().equals(TOOLS_ARTIFACT_ID)) {
         reactorRoots.add(reactorProject.getBasedir().getAbsolutePath());
       }
     }
 
     // Setup XP-Runners
     this.use_xp= StringUtils.join(reactorRoots.iterator(), File.pathSeparator);
     this.setupRunners(this.runnersDirectory, this.use_xp);
   }
 
   /**
    * Prepare XP-Bootstrap using project dependencies into specified directory
    *
    * @param  java.io.File targetDirectory
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupBootstrapFromDependencies(File targetDirectory) throws MojoExecutionException {
     getLog().debug("- Preparing XP-Bootstrap from project dependencies into [" + targetDirectory + "]");
 
     // Init [boot.pth] entries
     List<String> pthEntries= new ArrayList<String>();
     pthEntries.add(targetDirectory.getAbsolutePath());
 
     // Locate required XP-artifacts: core & tools
     Artifact coreArtifact= this.findArtifact(XP_FRAMEWORK_GROUP_ID, CORE_ARTIFACT_ID);
     if (null == coreArtifact) {
       throw new MojoExecutionException("Missing dependency for [net.xp-framework:core]");
     }
 
     Artifact toolsArtifact= this.findArtifact(XP_FRAMEWORK_GROUP_ID, TOOLS_ARTIFACT_ID);
     if (null == toolsArtifact) {
       throw new MojoExecutionException("Missing dependency for [net.xp-framework:tools]");
     }
 
     pthEntries.add(coreArtifact.getFile().getAbsolutePath());
     pthEntries.add(toolsArtifact.getFile().getAbsolutePath());
 
     // Locate optional XP-artifacts: language
     Artifact languageArtifact= this.findArtifact(XP_FRAMEWORK_GROUP_ID, LANGUAGE_ARTIFACT_ID);
     if (null != languageArtifact) {
       pthEntries.add(languageArtifact.getFile().getAbsolutePath());
     }
 
     // Unpack bootstrap
     UnArchiver unArchiver= ArchiveUtils.getUnArchiver(coreArtifact);
     unArchiver.extract("lang.base.php", targetDirectory);
 
     File toolsDirectory= new File(targetDirectory, TOOLS_ARTIFACT_ID);
     unArchiver= ArchiveUtils.getUnArchiver(toolsArtifact);
     unArchiver.extract("tools/class.php", toolsDirectory);
     unArchiver.extract("tools/web.php", toolsDirectory);
     unArchiver.extract("tools/xar.php", toolsDirectory);
 
     // Create [target/bootstrap/boot.pth]
     File pthFile= new File(targetDirectory, "boot.pth");
     try {
       FileUtils.setFileContents(pthFile, pthEntries, "#" + CREATED_BY_NOTICE);
     } catch (IOException ex) {
       throw new MojoExecutionException("Cannot write [" + pthFile + "]", ex);
     }
   }
 
   /**
    * Setup XP-Runners into specified directory
    *
    * @param  java.io.File targetDirectory
    * @param  java.lang.String useXp value of USE_XP
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupRunners(File targetDirectory, String useXp) throws MojoExecutionException {
     getLog().debug("- Preparing XP-Runners into [" + targetDirectory + "]");
 
     // Extract XP-runners
     try {
       getLog().debug(" - Extracting runners from resources");
 
       ExecuteUtils.saveRunner("xp", targetDirectory);
       ExecuteUtils.saveRunner("xcc", targetDirectory);
       ExecuteUtils.saveRunner("xar", targetDirectory);
       ExecuteUtils.saveRunner("xpcli", targetDirectory);
       ExecuteUtils.saveRunner("doclet", targetDirectory);
       ExecuteUtils.saveRunner("unittest", targetDirectory);
 
     } catch (IOException ex) {
       throw new MojoExecutionException("Cannot extract XP-runners to [" + targetDirectory + "]", ex);
     }
 
     // Set USE_XP
     IniProperties ini= new IniProperties();
     ini.setProperty("use", useXp);
 
     // Set PHP executable and timezone
     this.setupPhp();
     ini.setProperty("runtime", "default", this.php.getAbsolutePath());
 
     this.setupTimezone();
     ini.setProperty("runtime", "date.timezone", this.timezone);
 
     // Dump ini file
     File iniFile= new File(targetDirectory, "xp.ini");
     try {
       ini.setComment(CREATED_BY_NOTICE);
       ini.dump(iniFile);
     } catch (IOException ex) {
       throw new MojoExecutionException("Cannot write [" + iniFile + "]", ex);
     }
   }
 
   /**
    * Locate PHP executable. If ${xp.runtime.php} is not set, look for 'php' executable in PATH
    *
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupPhp() throws MojoExecutionException {
     getLog().debug("Identifying PHP executable");
 
     // ${xp.runtime.php.executable} is already set
     if (null != this.php) {
       getLog().debug(" - Provided by caller");
 
     // Check for PHP executable in PATH
     } else {
       try {
         getLog().debug(" - Inspecting PATH");
         this.php= ExecuteUtils.getExecutable("php");
 
       // Extract runners from resources
       } catch (FileNotFoundException ex) {
         throw new MojoExecutionException("Cannot find PHP executable. You can use -Dxp.runtime.php=...", ex);
       }
     }
 
     // Update ${xp.runtime.php} property
     getLog().debug(" - Using PHP from [" + this.php.getAbsolutePath() + "]");
     this.project.getProperties().setProperty("xp.runtime.php", this.php.getAbsolutePath());
   }
 
   /**
    * Determine timezone. If ${xp.runtime.timezone} is not set, use system timezone
    *
    * @return void
    * @throws org.apache.maven.plugin.MojoExecutionException
    */
   private void setupTimezone() throws MojoExecutionException {
     getLog().debug("Identifying timezone");
 
     // ${xp.runtime.timezone} is already set
     if (null != this.timezone) {
       getLog().debug(" - Provided by caller");
 
     // Use system default
     } else {
       getLog().debug(" - Using system timezone");
       this.timezone= Calendar.getInstance().getTimeZone().getID();
     }
 
     // Update ${xp.runtime.timezone} property
     getLog().debug(" - Using timezone [" + this.timezone + "]");
     this.project.getProperties().setProperty("xp.runtime.timezone", this.timezone);
   }
 
   /**
    * This will alter ${project.sourceDirectory} and ${project.testSourceDirectory} only if they have
    * the default values set up in the Maven Super POM:
    * - src/main/java
    * - src/test/java
    *
    * to the following values:
    * - src/main/xp
    * - src/test/xp
    *
    * @return void
    */
   private void alterSourceDirectories() {
 
     // Check ${project.sourceDirectory} ends with "src/main/java"
     String oldDirectory = this.project.getBuild().getSourceDirectory();
     String xpDirectory  = this.basedir.getAbsolutePath() + File.separator + "src" + File.separator + "main" + File.separator + "xp";
     if (oldDirectory.endsWith("src" + File.separator + "main" + File.separator + "java")) {
 
       // Alter ${project.sourceDirectory}
       this.project.getBuild().setSourceDirectory(xpDirectory);
       getLog().debug("Set ${project.sourceDirectory} to [" + xpDirectory + "]");
 
       // Maven2 limitation: changing ${project.sourceDirectory} doesn't change ${project.compileSourceRoots}
       List<String> newRoots= new ArrayList<String>();
       for (String oldRoot : (List<String>)this.project.getCompileSourceRoots()) {
         if (oldRoot.equals(oldDirectory)) {
           newRoots.add(xpDirectory);
         } else {
           newRoots.add(oldRoot);
         }
       }
 
       // Replace ${project.compileSourceRoots} with new list
       this.project.getCompileSourceRoots().clear();
       for (String newRoot : newRoots) {
         this.project.addCompileSourceRoot(newRoot);
       }
     }
 
     // Check ${project.testSourceDirectory} ends with "src/test/java"
     oldDirectory= this.project.getBuild().getTestSourceDirectory();
     xpDirectory= this.basedir.getAbsolutePath() + File.separator + "src" + File.separator + "test" + File.separator + "xp";
     if (oldDirectory.endsWith("src" + File.separator + "test" + File.separator + "java")) {
 
       // Alter ${project.testSourceDirectory}
       this.project.getBuild().setTestSourceDirectory(xpDirectory);
       getLog().debug("Set ${project.testSourceDirectory} to [" + xpDirectory + "]");
 
       // Maven2 limitation: changing ${project.testSourceDirectory} doesn't change ${project.testCompileSourceRoots}
       List<String> newRoots= new ArrayList<String>();
       for (String oldRoot : (List<String>)this.project.getTestCompileSourceRoots()) {
         if (oldRoot.equals(oldDirectory)) {
           newRoots.add(xpDirectory);
         } else {
           newRoots.add(oldRoot);
         }
       }
 
       // Replace ${project.testCompileSourceRoots} with new list
       this.project.getTestCompileSourceRoots().clear();
       for (String newRoot : newRoots) {
         this.project.addTestCompileSourceRoot(newRoot);
       }
     }
   }
 }
