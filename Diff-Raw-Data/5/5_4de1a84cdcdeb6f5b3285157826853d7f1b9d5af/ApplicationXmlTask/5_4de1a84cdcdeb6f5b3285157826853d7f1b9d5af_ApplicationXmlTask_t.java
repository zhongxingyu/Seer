 /* **********************************************************************
     Copyright 2010 Rensselaer Polytechnic Institute. All worldwide rights reserved.
 
     Redistribution and use of this distribution in source and binary forms,
     with or without modification, are permitted provided that:
        The above copyright notice and this permission notice appear in all
         copies and supporting documentation;
 
         The name, identifiers, and trademarks of Rensselaer Polytechnic
         Institute are not used in advertising or publicity without the
         express prior written permission of Rensselaer Polytechnic Institute;
 
     DISCLAIMER: The software is distributed" AS IS" without any express or
     implied warranty, including but not limited to, any implied warranties
     of merchantability or fitness for a particular purpose or any warrant)'
     of non-infringement of any current or pending patent rights. The authors
     of the software make no representations about the suitability of this
     software for any particular purpose. The entire risk as to the quality
     and performance of the software is with the user. Should the software
     prove defective, the user assumes the cost of all necessary servicing,
     repair or correction. In particular, neither Rensselaer Polytechnic
     Institute, nor the authors of the software are liable for any indirect,
     special, consequential, or incidental damages related to the software,
     to the maximum extent the law permits.
  */
 package org.bedework.deployment;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.DirectoryScanner;
 import org.apache.tools.ant.taskdefs.MatchingTask;
 import org.apache.tools.ant.types.FileSet;
 import org.apache.tools.ant.util.FileUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.Writer;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Properties;
 
 /** Ant task to build the application.xml file for a ear.
  *
  * Task attributes are <ul>
  * <li>outFile  The application.xml file we are creating</li>
  * <li>displayName  Optional display name</li>
  * <li>warDir    Directory containing war files or expanded wars with names
  *               ending in ".war"</li>
  * <li>contexts  property file defining context roots</li>
  * </ul>
  *
  * <p>Body is a fileset giving jar files to add.
  *
  * @author douglm @ rpi.edu
  */
 public class ApplicationXmlTask extends MatchingTask {
   private List<FileSet> filesets = new LinkedList<FileSet>();
 
   private List<String> wars = new LinkedList<String>();
 
   private List<String> jars = new LinkedList<String>();
 
   private File warDir;
 
   private String displayName;
 
   private String contexts;
 
   private File outFile;
   private Writer wtr;
   private Properties contextProps;
 
   /** Add a fileset
    *
    * @param set
    */
   public void addFileset(final FileSet set) {
     filesets.add(set);
   }
 
   /** Set the display name
    *
    * @param val   String
    */
   public void setDisplayName(final String val) {
     displayName = val;
   }
 
   /** Set the contexts file name
    *
    * @param val   String
    */
   public void setContexts(final String val) {
     contexts = val;
   }
 
   /** Set the application.xml output file
    *
    * @param val   File
    */
   public void setOutFile(final File val) {
     outFile = val;
   }
 
   /** Set the directory containing wars
    *
    * @param val   File
    */
   public void setWarDir(final File val) {
     warDir = val;
   }
 
   /** Executes the task
    */
   @Override
   public void execute() throws BuildException {
     try {
       getModules();
 
       FileUtils.getFileUtils().createNewFile(outFile, true);
 
       wtr = new FileWriter(outFile);
 
       if (contexts != null) {
         FileInputStream propFile = new FileInputStream(contexts);
         contextProps = new Properties();
         contextProps.load(propFile);
       }
 
       writeLine("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
       writeLine("");
       writeLine("<application>");
       if (displayName != null) {
         writeLine("  <display-name>" + displayName + "</display-name>");
       }
 
       for (String nm: wars) {
         writeLine("");
         writeLine("  <module>");
         writeLine("    <web>");
         writeLine("      <web-uri>" + nm + "</web-uri>");
 
         String warName = nm.substring(0, nm.lastIndexOf(".war"));
         String contextRoot = null;
 
         if (contextProps != null) {
           contextRoot = contextProps.getProperty(warName + ".context");
 
           if (contextRoot == null) {
             throw new BuildException("No context root defined for " + warName);
           }
 
           if (contextRoot.length() == 0) {
             contextRoot = "/";
           }
         }
 
         if (contextRoot == null) {
           contextRoot = "/" + warName;
         }
 
         writeLine("      <context-root>" + contextRoot + "</context-root>");
         writeLine("    </web>");
         writeLine("  </module>");
       }
 
       for (String nm: jars) {
         writeLine("");
         writeLine("  <module>");
         writeLine("    <java>" + nm + "</java>");
         writeLine("  </module>");
       }
 
       writeLine("</application>");
 
       wtr.close();
     } catch (BuildException be) {
       be.printStackTrace();
       throw be;
     } catch (Throwable t) {
       t.printStackTrace();
       throw new BuildException(t);
     }
   }
 
   /* Scan the filesets and extract files that end with ".jar" and directories
    * or files that end with ".war"
    *
    */
   private void getModules() throws BuildException {
     FilenameFilter fltr = new FilenameFilter() {
       public boolean accept(final File dir, final String name) {
         return name.endsWith(".war");
       }
     };
 
     if (warDir == null) {
       throw new BuildException("No wardir supplied");
     }
 
     String[] warnames = warDir.list(fltr);

    if (warnames == null) {
      throw new BuildException("No wars found at " + warDir);
    }

     for (int wi = 0; wi < warnames.length; wi++) {
       wars.add(warnames[wi]);
     }
 
     for (FileSet fs: filesets) {
       DirectoryScanner ds = fs.getDirectoryScanner(getProject());
 
       String[] dsFiles = ds.getIncludedFiles();
 
       for (int dsi = 0; dsi < dsFiles.length; dsi++) {
         String fname = dsFiles[dsi];
 
         if (fname.endsWith(".jar")) {
           jars.add(fname);
         } else if (fname.endsWith(".war")) {
           wars.add(fname);
         }
       }
     }
   }
 
   private void writeLine(final String ln) throws Throwable {
     wtr.write(ln);
     wtr.write("\n");
   }
 
 }
