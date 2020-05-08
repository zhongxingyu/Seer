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
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.ArrayList;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.codehaus.plexus.archiver.Archiver;
 import org.codehaus.plexus.archiver.zip.ZipArchiver;
 
 import net.xp_forge.maven.plugins.xp.util.FileUtils;
 import net.xp_forge.maven.plugins.xp.util.XarArchiver;
 
 /**
  * Build project package artifact
  *
  */
 public abstract class AbstractPackageMojo extends net.xp_forge.maven.plugins.xp.AbstractMojo {
   protected Archiver archiver;
 
   /**
    * Name of the generated XAR
    *
    * @parameter default-value="${project.build.finalName}"
    * @required
    */
   protected String finalName;
 
   /**
    * Classifier to add to the generated artifact
    *
    * @parameter
    */
   protected String classifier;
 
   /**
    * Specify what to do with dependencies. There are 3 options:
    * - ignore  - dependencies are ignored when building the package artifact
    * - include - dependencies are included in the package artifact in "lib" directory
    * - merge   - dependencies are merged in the package artifact
    *
    * @parameter expression="${xp.package.dependencies}" default-value="ignore"
    * @required
    */
   protected String dependencies;
 
   /**
    * Specify what archiver to use. There are 2 options:
    * - zip
    * - xar
    *
    * @parameter expression="${xp.package.format}" default-value="xar"
    * @required
    */
   protected String format;
 
   /**
    * Specify what type of artifact to build. There are 2 options:
    * - library
    * - application
    *
    * @parameter expression="${xp.package.type}" default-value="library"
    * @required
    */
   protected String type;
 
   /**
    * Get location of classes files to include in the package
    *
    */
   protected abstract File getSrcDirectory();
 
   /**
    * {@inheritDoc}
    *
    */
   public void execute() throws MojoExecutionException {
 
     // Init project.pth entries
     List<String> pthEntries= new ArrayList<String>();
 
     // Assemble XAR archive
     File srcDirectory = this.getSrcDirectory();
     File outputFile   = this.getOutputFile();
     getLog().info("Classes directory [" + srcDirectory + "]");
     getLog().info("Output file       [" + outputFile + "]");
     getLog().info("Pack dependencies [" + this.dependencies + "]");
     getLog().info("Package type      [" + this.type + "]");
     getLog().info("Artifact format   [" + this.format + "]");
 
     // Init archiver
     if (this.format.equals("xar")) {
       this.archiver= new XarArchiver();
 
     } else if (this.format.equals("zip")) {
       this.archiver= new ZipArchiver();
 
     } else{
       throw new MojoExecutionException("${xp.package.format} has an invalid value [" + this.format + "]");
     }
 
     // Add srcDirectory
     if (this.type.equals("library")) {
       getLog().debug(" - Add directory [" + srcDirectory + "] to [/]");
       this.archiver.addDirectory(srcDirectory);
       pthEntries.add(".");
 
     } else if (this.type.equals("aplication")) {
       getLog().debug(" - Add directory [" + srcDirectory + "] to [/classes/]");
       this.archiver.addDirectory(srcDirectory, "classes/");
       pthEntries.add("classes");
 
     } else{
       throw new MojoExecutionException("${xp.package.type} has an invalid value [" + this.type + "]");
     }
 
     // Process dependencies
     if (this.dependencies.equals("ignore")) {
 
     // Include dependencies into the "lib" folder
     } else if (this.dependencies.equals("include")) {
       getLog().info("Including dependencies");
       for (Artifact artifact : (Iterable<Artifact>)this.project.getArtifacts()) {
         getLog().info(" - " + artifact.getType() + " [" + artifact.getFile().getAbsolutePath() + "]");
 
         getLog().debug(" - Add file [" + artifact.getFile() + "] to [/libs/]");
         this.archiver.addFile(artifact.getFile(), "libs/" + artifact.getFile().getName());
         pthEntries.add("libs/" + artifact.getFile().getName());
       }
 
       // Also generate a "project.pth"
       getLog().info("Adding on-the-fly created [project.pth] to archive");
       File pthFile= new File(this.outputDirectory, "project.pth-package");
       try {
         FileUtils.setFileContents(pthFile, StringUtils.join(pthEntries, "\n"));
       } catch (IOException ex) {
         throw new MojoExecutionException("Cannot create temp file [" + pthFile + "]");
       }
 
       // Add it to archive
       getLog().debug(" - Add file [" + pthFile + "] to [/]");
       this.archiver.addFile(pthFile, "project.pth");
 
     // Merge dependencies into the "lib" folder
     } else if (this.dependencies.equals("merge")) {
       getLog().info("Merging dependencies");
       for (Artifact artifact : (Iterable<Artifact>)this.project.getArtifacts()) {
         getLog().info(" - " + artifact.getType() + " [" + artifact.getFile().getAbsolutePath() + "]");
         //**************archiver.addFile(artifact.getFile(), "libs/");
       }
     }
 
     // For applications also include "doc_root", "etc" and "xsl" directories
     if (this.type.equals("aplication")) {
       File mainDir= new File(this.project.getBuild().getSourceDirectory()).getParentFile();
       for (String appDirName : Arrays.asList("doc_root", "etc", "xsl")) {
 
         // If app dir does not exist; skip it
         File appDir= new File(mainDir, appDirName);
         if (!appDir.exists()) continue;
 
         // Add app dir contents to archive
         getLog().debug(" - Add directory [" + appDir + "] to [/" + appDirName + "/]");
         this.archiver.addDirectory(appDir, appDirName + "/");
       }
     }
 
     // Save archive to output file
     try {
       getLog().debug(" - Creating archive to [" + outputFile + "]");
       this.archiver.setDestFile(outputFile);
       this.archiver.createArchive();
     } catch (Exception ex) {
       throw new MojoExecutionException("Cannot create [" + this.format + "] archive to [" + outputFile + "]", ex);
     }
   }
 
   /**
   * Returns the output file, based on finalName, classifier and format
    *
    * @return java.io.File Location where to generate the output XAR file
    */
   protected File getOutputFile() {
     if (null == this.classifier || this.classifier.length() <= 0) {
       return new File(this.outputDirectory, this.finalName + "." + this.format);
     }
     return new File(
       this.outputDirectory,
       this.finalName +
       (null != this.classifier && this.classifier.startsWith("-") ? "" : "-") + this.classifier +
       "." + this.format
     );
   }
 }
