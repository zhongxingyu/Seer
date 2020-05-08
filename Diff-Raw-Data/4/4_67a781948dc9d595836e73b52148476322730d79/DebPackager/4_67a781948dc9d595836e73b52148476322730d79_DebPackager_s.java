 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb, ipk, izpack)
  * Copyright (C) 2000-2008 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License,version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'Maven Packaging Plugin'
  * Signature of Elmar Geese, 11 March 2008
  * Elmar Geese, CEO tarent GmbH.
  */
 
 /*
  * Maven Packaging Plugin,
  * Maven plugin to package a Project (deb and izpack)
  * Copyright (C) 2000-2007 tarent GmbH
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License,version 2
  * as published by the Free Software Foundation.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
  * 02110-1301, USA.
  *
  * tarent GmbH., hereby disclaims all copyright
  * interest in the program 'Maven Packaging Plugin'
  * Signature of Elmar Geese, 14 June 2007
  * Elmar Geese, CEO tarent GmbH.
  */
 
 package de.tarent.maven.plugins.pkg.packager;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 
 import de.tarent.maven.plugins.pkg.AotCompileUtils;
 import de.tarent.maven.plugins.pkg.Path;
 import de.tarent.maven.plugins.pkg.SysconfFile;
 import de.tarent.maven.plugins.pkg.TargetConfiguration;
 import de.tarent.maven.plugins.pkg.Utils;
 import de.tarent.maven.plugins.pkg.generator.ControlFileGenerator;
 import de.tarent.maven.plugins.pkg.helper.Helper;
 import de.tarent.maven.plugins.pkg.map.PackageMap;
 
 /**
  * Creates a Debian package file (.deb)
  * 
  * TODO: Description needs to formatted in a Debian-specific way 
  */
 public class DebPackager extends Packager
 {
 
   public void execute(Log l,
                       Helper helper,
                       PackageMap packageMap) throws MojoExecutionException
   {
 	
 	TargetConfiguration distroConfig = helper.getTargetConfiguration();
	if(!(helper instanceof Helper)){
 		throw new IllegalArgumentException("Debian helper needed");
 	}
 	Helper ph = (Helper) helper;
 	
     String packageName = ph.getPackageName();
     String packageVersion = ph.getPackageVersion();
 
     File basePkgDir = ph.getBasePkgDir();
     
     // Provide a proper default value to make script file copying work.
     ph.setDstScriptDir(new File(basePkgDir, "DEBIAN"));
     
     // The Debian control file (package name, dependencies etc).
     File controlFile = new File(basePkgDir, "DEBIAN/control");
 
     // The file listing the config files.
     File conffilesFile = new File(basePkgDir, "DEBIAN/conffiles");
     
     File srcArtifactFile = ph.getSrcArtifactFile();
 
     String gcjPackageName = ph.getAotPackageName();
     
     File aotPkgDir = ph.getAotPkgDir();
     
     // The file extension for aot-compiled binaries.
     String aotExtension = ".jar.so";
     
     // The destination directory for all aot-compiled binaries.
     File aotDstDir = new File(aotPkgDir, packageMap.getDefaultJarPath());
     
     // The file name of the aot-compiled binary of the project's own artifact.
     File aotCompiledBinaryFile = new File(aotDstDir, ph.getArtifactId() + aotExtension);
     
     // The destination directory for all classmap files. 
     File aotDstClassmapDir = new File(aotPkgDir, "usr/share/gcj-4.1/classmap.d");
     
     // The file name of the classmap of the project's own artifact. 
     File aotClassmapFile = new File(aotDstClassmapDir, ph.getArtifactId() + ".db");
     
     // The destination file for the 'postinst' script.
     File aotPostinstFile = new File(aotPkgDir, "DEBIAN/postinst");
 
     // The destination file for the 'control' file.
     File aotControlFile = new File(aotPkgDir, "DEBIAN/control");
     
     // A set which will be filled with the artifacts which need to be bundled with the
     // application.
     Set<Artifact> bundledArtifacts = null;
     Path bcp = new Path();
     Path cp = new Path();
     
     long byteAmount = 0;
     
     try
       {
     	// The following section does the coarse-grained steps
     	// to build the package(s). It is meant to be kept clean
     	// from simple Java statements. Put additional functionality
     	// into the existing methods or create a new one and add it
     	// here.
     	
         ph.prepareInitialDirectories();
 
         byteAmount += ph.copyProjectArtifact();
         
         byteAmount += ph.copyFiles();
         
         generateConffilesFile(l, conffilesFile, ph);
         
         byteAmount += ph.copyScripts();
         
 		byteAmount += ph.createCopyrightFile();
 
         // Create classpath line, copy bundled jars and generate wrapper
         // start script only if the project is an application.
         if (distroConfig.getMainClass() != null)
           {
             // TODO: Handle native library artifacts properly.
             bundledArtifacts = ph.createClasspathLine(bcp, cp);
 
             ph.generateWrapperScript(bundledArtifacts, bcp, cp, false);
 
             byteAmount += ph.copyArtifacts(bundledArtifacts);
           }
         
         generateControlFile(l,
                             ph,
         		            controlFile,
         		            packageName,
         		            packageVersion,
         		            ph.createDependencyLine(),
         		            ph.createRecommendsLine(),
         		            ph.createSuggestsLine(),
         		            ph.createProvidesLine(),
         		            ph.createConflictsLine(),
         		            ph.createReplacesLine(),
         		            byteAmount);
         
         createPackage(l, ph, basePkgDir);
         
         if (distroConfig.isAotCompile())
           {
             ph.prepareAotDirectories();
             // At this point anything created in the basePkgDir cannot be used
             // any more as it was removed by the above method.
             
             byteAmount = aotCompiledBinaryFile.length() + aotClassmapFile.length();
             
             AotCompileUtils.compile(l, srcArtifactFile, aotCompiledBinaryFile);
             
             AotCompileUtils.generateClassmap(l,
                                              aotClassmapFile,
                                              srcArtifactFile,
                                              aotCompiledBinaryFile,
                                              packageMap.getDefaultJarPath());
 
             // AOT-compile and classmap generation for bundled Jar libraries
             // are only needed for applications.
             if (distroConfig.getMainClass() != null)
               byteAmount += AotCompileUtils.compileAndMap(l,
             		                bundledArtifacts,
             		                aotDstDir,
             		                aotExtension,
             		                aotDstClassmapDir,
                                     packageMap.getDefaultJarPath());
             
 /*            gen.setShortDescription(gen.getShortDescription() + " (GCJ version)");
             gen.setDescription("This is the ahead-of-time compiled version of "
                                + "the package for use with GIJ.\n"
                                + gen.getDescription());
  */          
             // The dependencies of a "-gcj" package are always java-gcj-compat
             // and the corresponding 'bytecode' version of the package.
             // GCJ can only compile for one architecture.
             distroConfig.setArchitecture(System.getProperty("os.arch"));
             generateControlFile(l,
                                 ph,
                                 aotControlFile,
             		            gcjPackageName,
             		            packageVersion,
             		            "java-gcj-compat",
             		            null,
             		            null,
             		            null,
             		            null,
             		            null,
             		            byteAmount);
             
             AotCompileUtils.depositPostinstFile(l, aotPostinstFile);
             
             createPackage(l, ph, aotPkgDir);
           }
         
       }
     catch (MojoExecutionException badMojo)
       {
         throw badMojo;
       }
     
     /* When the Mojo fails to complete its task the work directory will be left
      * in an unclean state to make it easier to debug problems.
      * 
      * However the work dir will be cleaned up when the task is run next time.
      */
   }
 
   /** Validates arguments and test tools.
    * 
    * @throws MojoExecutionException
    * @override
    */
   public void checkEnvironment(Log l,
                                Helper ph) throws MojoExecutionException
   {
     // No specifics to show or test.
   }
 
   /**
    * Creates a control file whose dependency line can be provided as an argument.
    * 
    * @param l
    * @param controlFile
    * @param packageName
    * @param packageVersion
    * @param installedSize
    * @throws MojoExecutionException
    */
   private void generateControlFile(Log l,
                                    Helper ph,
                                    File controlFile,
                                    String packageName,
                                    String packageVersion,
                                    String dependencyLine,
                                    String recommendsLine,
                                    String suggestsLine,
                                    String providesLine,
                                    String conflictsLine,
                                    String replacesLine,
                                    long byteAmount)
       throws MojoExecutionException
   {
 	ControlFileGenerator cgen = new ControlFileGenerator();
 	cgen.setPackageName(packageName);
 	cgen.setVersion(packageVersion);
 	cgen.setSection(ph.getTargetConfiguration().getSection());
 	cgen.setDependencies(dependencyLine);
 	cgen.setRecommends(recommendsLine);
 	cgen.setSuggests(suggestsLine);
 	cgen.setProvides(providesLine);
 	cgen.setConflicts(conflictsLine);
 	cgen.setReplaces(replacesLine);
 	cgen.setMaintainer(ph.getTargetConfiguration().getMaintainer());
 	cgen.setShortDescription(ph.getProjectDescription());
 	cgen.setDescription(ph.getProjectDescription());
 	cgen.setArchitecture(ph.getTargetConfiguration().getArchitecture());
 	cgen.setInstalledSize(Utils.getInstalledSize(byteAmount));
 	    
     l.info("creating control file: " + controlFile.getAbsolutePath());
     Utils.createFile(controlFile, "control");
     
     try
       {
         cgen.generate(controlFile);
       }
     catch (IOException ioe)
       {
         throw new MojoExecutionException("IOException while creating control file.",
                                          ioe);
       }
 
   }
   
   /**
    * Iterates over the sysconf files and creates the Debian 'conffiles' file for them.
    * 
    * <p>If no sysconf files exists nothing is done however.</p>
    * 
    * @param l
    * @param conffilesFile
    * @param ph
    * @param tc
    * @throws MojoExecutionException
    */
   private void generateConffilesFile(Log l, File conffilesFile, Helper ph)
   	throws MojoExecutionException
   	{
 	  List<SysconfFile> sysconffiles = (List<SysconfFile>) ph.getTargetConfiguration().getSysconfFiles();
 	  if (sysconffiles.isEmpty())
 	  {
 		  l.info("No sysconf files defined - not creating file.");
 		  return;
 	  }
 	  
 	  StringBuilder sb = new StringBuilder(sysconffiles.size() * 10);
 	  for (SysconfFile scf : sysconffiles)
 	  {
 		  File targetFile; 
 		  if (scf.isRename())
 		  {
 			  targetFile = new File(ph.getTargetSysconfDir(), scf.getTo());
 		  }
 		  else
 		  {
 			  File srcFile = new File(ph.getSrcSysconfFilesDir(), scf.getFrom());  
 			  File targetPath = new File(ph.getTargetSysconfDir(), scf.getTo());
 			  targetFile = new File(targetPath, srcFile.getName());
 		  }
 		  sb.append(targetFile.getAbsolutePath());
 		  sb.append("\n");
 	  }
 
 	  	if (!conffilesFile.getParentFile().mkdirs())
 			throw new MojoExecutionException(
 					"Could not create directory for conffiles file.");
 	  
 		try {
 			conffilesFile.createNewFile();
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(
 					"IOException while creating conffiles file.", ioe);
 		}
 
 		try {
 			FileUtils.writeStringToFile(conffilesFile, sb.toString());
 		} catch (IOException ioe) {
 			throw new MojoExecutionException(
 					"IOException while writing to conffiles file.", ioe);
 		}
   }
 
   private void createPackage(Log l, Helper ph, File base) throws MojoExecutionException
   {
     l.info("calling dpkg-deb to create binary package");
     
     
     
     Utils.exec(new String[] {"fakeroot",
                              "dpkg-deb",
                              "--build",
                              base.getName(),
                              ph.getOutputDirectory().getAbsolutePath() },
                 ph.getTempRoot(),
                 "'fakeroot dpkg --build' failed.",
                 "Error creating the .deb file.");
   }
 
 
 
 }
