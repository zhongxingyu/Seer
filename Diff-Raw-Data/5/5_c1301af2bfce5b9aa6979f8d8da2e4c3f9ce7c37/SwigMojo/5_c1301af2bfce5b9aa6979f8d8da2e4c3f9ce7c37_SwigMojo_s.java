 // Copyright FreeHEP, 2006.
 package org.freehep.maven.swig;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.DefaultArtifact;
 import org.apache.maven.artifact.handler.ArtifactHandler;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.artifact.versioning.VersionRange;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.archiver.manager.ArchiverManager;
 import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
 import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
 import org.codehaus.plexus.compiler.util.scan.StaleSourceScanner;
 import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
 import org.codehaus.plexus.util.FileUtils;
 import org.freehep.maven.nar.AbstractNarMojo;
 import org.freehep.maven.nar.Linker;
 import org.freehep.maven.nar.NarArtifact;
 import org.freehep.maven.nar.NarManager;
 import org.freehep.maven.nar.NarUtil;
 
 /**
  * Compiles swg files using the swig compiler.
  * 
  * @goal generate
  * @description Compiles swg files using the swig compiler.
  * @phase generate-sources
  * @requiresDependencyResolution compile
  * @author <a href="Mark.Donszelmann@slac.stanford.edu">Mark Donszelmann</a>
 * @version $Id: src/main/java/org/freehep/maven/swig/SwigMojo.java 800ec2a9a665 2006/11/07 18:26:36 duns $
  */
 public class SwigMojo extends AbstractMojo {
 
     /**
      * Skip the running of SWIG
      * 
      * @parameter expression="${swig.skip}" default-value="false"
      */
     private boolean skip;
 
     /**
      * Force the running of SWIG
      * 
      * @parameter expression="${swig.force}" default-value="false"
      */
     private boolean force;
 
     /**
      * Level of logging messages, 0 is minimum.
      * 
      * @parameter expression="${swig.logLevel}" default-value="0"
      */
     private int logLevel;
 
     /**
      * Define symbol for conditional compilation, same as -D option for swig.
      * 
      * @parameter
      */
     private List defines;
 
     /**
      * Sets a fake version number, same as -fakeversion for swig.
      * 
      * @parameter
      */
     private String fakeVersion;
     
     /**
      * Enable C++ processing, same as -c++ option for swig.
      * 
      * @parameter expression="${swig.cpp}" default-value="false"
      */
     private boolean cpp;
 
     /**
      * Add include paths. By default the current directory is scanned.
      * 
      * @parameter
      */
     private List includePaths;
 
     /**
      * List of warning numbers to be suppressed, same as -w option for swig.
      * 
      * @parameter expression="${swig.noWarn}"
      */
     private String noWarn;
 
     /**
      * Enable all warnings, same as -Wall
      * 
      * @parameter expression="${swig.warnAll}" default-value="false"
      */
     private boolean warnAll;
 
     /**
      * Treat warnings as errors, same as -Werror
      * 
      * @parameter expression="${swig.warnError}" default-value="false"
      */
     private boolean warnError;
 
     /**
      * The target directory into which to generate the output.
      * 
      * @parameter expression="${project.build.directory}/swig"
      */
     private File targetDirectory;
 
     /**
      * The package name for the generated java files (fully qualified ex:
      * org.freehep.jni).
      * 
      * @parameter expression="${swig.packageName}"
      */
     private String packageName;
 
     /**
      * The target directory into which to generate the java output, becomes
      * -outdir option for swig.
      * 
      * @parameter expression="${project.build.directory}/swig/java"
      */
     private String javaTargetDirectory;
 
     /**
      * The directory to look for swig files and swig include files. Also added
      * to -I flag when swig is run.
      * 
      * @parameter expression="${basedir}/src/main/swig"
      * @required
      */
     private String sourceDirectory;
 
     /**
      * The swig file to process, normally in source directory set by
      * swigDirectory.
      * 
      * @parameter
      * @required
      */
     private String source;
 
     /**
      * The Architecture for picking up swig, Some choices are: "x86", "i386",
      * "amd64", "ppc", "sparc", ... Defaults to a derived value from ${os.arch}
      * 
      * @parameter expression="${os.arch}"
      * @required
      */
     private String architecture;
 
     /**
      * The Operating System for picking up swig. Some choices are: "Windows",
      * "Linux", "MacOSX", "SunOS", ... Defaults to a derived value from
      * ${os.name}
      * 
      * @parameter expression=""
      */
     private String os;
 
     /**
      * The granularity in milliseconds of the last modification date for testing
      * whether a source needs recompilation
      * 
      * @parameter expression="${idlj.staleMillis}" default-value="0"
      * @required
      */
     private int staleMillis;
 
     /**
      * Swig Executable (overrides built-in or user configured reference to NAR)
      * 
      * @parameter expression="${swig.exec}"
      */
     private String exec;
 
     /**
      * GroupId for the swig NAR
      * 
      * @parameter expression="${swig.groupId}" default-value="org.swig"
      */
     private String groupId;
 
     /**
      * ArtifactId for the swig NAR
      * 
      * @parameter expression="${swig.artifactId}" default-value="swig"
      */
     private String artifactId;
 
     /**
      * Version for the swig NAR
      * 
     * @parameter expression="${swig.version}" default-value="1.3.29-1"
      */
     private String version;
 
     /**
      * @parameter expression="${project}"
      * @required
      * @readonly
      */
     private MavenProject project;
 
     /**
      * @parameter expression="${localRepository}"
      * @required
      * @readonly
      */
     private ArtifactRepository localRepository;
 
     /**
      * Artifact handler
      * 
      * @component role="org.apache.maven.artifact.handler.ArtifactHandler"
      * @required
      * @readonly
      */
     private ArtifactHandler artifactHandler;
 
     /**
      * Artifact resolver, needed to download source jars for inclusion in
      * classpath.
      * 
      * @component role="org.apache.maven.artifact.resolver.ArtifactResolver"
      * @required
      * @readonly
      */
     private ArtifactResolver artifactResolver;
 
     /**
      * Remote repositories which will be searched for source attachments.
      * 
      * @parameter expression="${project.remoteArtifactRepositories}"
      * @required
      * @readonly
      */
     private List remoteArtifactRepositories;
 
     /**
      * To look up Archiver/UnArchiver implementations
      * 
      * @parameter expression="${component.org.codehaus.plexus.archiver.manager.ArchiverManager}"
      * @required
      */
     private ArchiverManager archiverManager;
 
     private NarManager narManager;
 
     public void execute() throws MojoExecutionException, MojoFailureException {
         if (skip) {
             getLog()
                     .warn("SKIPPED Running SWIG compiler on " + source + " ...");
             return;
         }
 
         os = NarUtil.getOS(os);
         // FIXME, should have some function in NarUtil
         Linker linker = new Linker("g++");
         narManager = new NarManager(getLog(), logLevel, localRepository,
                 project, architecture, os, linker);
 
         targetDirectory = new File(targetDirectory, cpp ? "c++" : "c");
         if (!targetDirectory.exists()) {
             targetDirectory.mkdirs();
         }
 
         if (project != null) {
             project.addCompileSourceRoot(javaTargetDirectory);
             project.addCompileSourceRoot(targetDirectory.getPath());
         }
 
         if (packageName != null) {
             if (!javaTargetDirectory.endsWith("/")) {
                 javaTargetDirectory = javaTargetDirectory + "/";
             }
             javaTargetDirectory += packageName.replace('.', File.separatorChar);
         }
 
         if (!FileUtils.fileExists(javaTargetDirectory)) {
             FileUtils.mkdir(javaTargetDirectory);
         }
 
         if (!sourceDirectory.endsWith("/")) {
             sourceDirectory = sourceDirectory + "/";
         }
 
         // make sure all NAR dependencies are downloaded and unpacked
         // even if packaging is NOT nar
         // in nar packaging, downloading happens in generate-sources phase and
         // thus may be too late
         // unpacking happens in process-sources which is definitely too late
         // so we need to handle this here ourselves.
         List narArtifacts = narManager.getNarDependencies("compile");
         narManager.downloadAttachedNars(narArtifacts,
                 remoteArtifactRepositories, artifactResolver, null);
         narManager.unpackAttachedNars(narArtifacts, archiverManager, null, os);
 
         File swig, swigInclude, swigJavaInclude;
         if (exec == null) {
             // NOTE, since a project will just load this as a plugin, there is
             // no way to look up
             // the org.swig:swig dependency, so we hardcode that in here, but it
             // is configurable
             // in the configuration part of this plugin.
             Artifact swigJar = new DefaultArtifact(groupId, artifactId,
                     VersionRange.createFromVersion(version), "compile", "jar",
                     "", artifactHandler);
 
             // download jar file
             try {
                 // System.err.println("Resolving " + swigJar);
                 artifactResolver.resolve(swigJar, remoteArtifactRepositories,
                         localRepository);
             } catch (ArtifactNotFoundException e) {
                 String message = "Jar not found " + swigJar;
                 throw new MojoExecutionException(message, e);
             } catch (ArtifactResolutionException e) {
                 String message = "Jar cannot be resolved " + swigJar;
                 throw new MojoExecutionException(message, e);
             }
 
             NarArtifact swigNar = new NarArtifact(swigJar, narManager
                     .getNarInfo(swigJar));
 
             // download attached nars
             List swigNarArtifacts = new ArrayList();
             swigNarArtifacts.add(swigNar);
             narManager.downloadAttachedNars(swigNarArtifacts,
                     remoteArtifactRepositories, artifactResolver, null);
             narManager.unpackAttachedNars(swigNarArtifacts, archiverManager,
                     null, os);
 
             swig = new File(narManager.getNarFile(swigNar).getParentFile(),
                     "nar");
             swigInclude = new File(swig, "include");
             swigJavaInclude = new File(swigInclude, "java");
             swig = new File(swig, "bin");
             swig = new File(swig, NarUtil
                     .getAOL(architecture, os, linker, null));
             swig = new File(swig, "swig");
         } else {
             swig = new File(exec);
             swigInclude = null;
             swigJavaInclude = null;
         }
 
         File sourceFile = new File(sourceDirectory);
         File targetFile = targetDirectory;
         SourceInclusionScanner scanner = new StaleSourceScanner(staleMillis,
                 Collections.singleton(source), Collections.EMPTY_SET);
         SuffixMapping mapping = new SuffixMapping(".swg", ".flag");
         scanner.addSourceMapping(mapping);
         try {
             Set files = scanner.getIncludedSources(sourceFile, targetFile);
 
             if (!files.isEmpty() || force) {
                 getLog().info(
                         (force ? "FORCE " : "") + "Running SWIG compiler on "
                                 + source + " ...");
                 int error = runCommand(generateCommandLine(swig, swigInclude,
                         swigJavaInclude));
                 if (error != 0) {
                     throw new MojoFailureException("SWIG returned error code "
                             + error);
                 }
                 File flagFile = new File(targetDirectory, FileUtils.basename(
                         source, ".swg")
                         + ".flag");
                 FileUtils.fileDelete(flagFile.getPath());
                 FileUtils.fileWrite(flagFile.getPath(), "");
             } else {
                 getLog().info("Nothing to swig - all classes are up to date");
             }
         } catch (InclusionScanException e) {
             throw new MojoExecutionException("IDLJ: Source scanning failed", e);
         } catch (IOException e) {
             throw new MojoExecutionException(
                     "SWIG: Creation of timestamp flag file failed", e);
         }
     }
 
     private String[] generateCommandLine(File swig, File swigInclude,
             File swigJavaInclude) throws MojoExecutionException,
             MojoFailureException {
 
         List cmdLine = new ArrayList();
 
         cmdLine.add(swig.toString());
 
         if (fakeVersion != null) {
             cmdLine.add("-fakeversion");
             cmdLine.add(fakeVersion);
         }
         
         if (getLog().isDebugEnabled()) {
             cmdLine.add("-v");
         }
 
         // FIXME hardcoded
         cmdLine.add("-java");
 
         if (cpp) {
             cmdLine.add("-c++");
         }
 
         // defines
         if (defines != null) {
             for (Iterator i = defines.iterator(); i.hasNext();) {
                 cmdLine.add("-D");
                 cmdLine.add((String) i.next());
             }
         }
 
         // warnings
         if (noWarn != null) {
             String noWarns[] = noWarn.split(",| ");
             for (int i = 0; i < noWarns.length; i++) {
                 cmdLine.add("-w" + noWarns[i]);
             }
         }
 
         if (warnAll) {
             cmdLine.add("-Wall");
         }
 
         if (warnError) {
             cmdLine.add("-Werror");
         }
 
         // output file
         String baseName = FileUtils.basename(source);
         cmdLine.add("-o");
         cmdLine.add((new File(targetDirectory, baseName + (cpp ? "cxx" : "c")))
                 .toString());
 
         // package for java code
         if (packageName != null) {
             cmdLine.add("-package");
             cmdLine.add(packageName);
         }
 
         // output dir for java code
         cmdLine.add("-outdir");
         cmdLine.add(javaTargetDirectory);
 
         // user added include dirs
         if (includePaths != null) {
             for (Iterator i = includePaths.iterator(); i.hasNext();) {
                 cmdLine.add("-I" + i.next());
             }
         }
         // default include dirs
         cmdLine.add("-I" + "src/main/include");
         cmdLine.add("-I" + sourceDirectory);
 
         // NAR dependency include dirs
         List narIncludes = narManager.getNarDependencies("compile");
         for (Iterator i = narIncludes.iterator(); i.hasNext();) {
             Artifact narInclude = (Artifact) i.next();
             File narIncludeDir = new File(narManager.getNarFile(narInclude)
                     .getParentFile(), "nar");
             narIncludeDir = new File(narIncludeDir, "include");
             if (narIncludeDir.isDirectory()) {
                 cmdLine.add("-I" + narIncludeDir);
             }
         }
 
         // system swig include dirs
         if (swigJavaInclude != null)
             cmdLine.add("-I" + swigJavaInclude.toString());
         if (swigInclude != null)
             cmdLine.add("-I" + swigInclude.toString());
 
         // swig file
         cmdLine.add(sourceDirectory + source);
 
         StringBuffer b = new StringBuffer();
         for (Iterator i=cmdLine.iterator(); i.hasNext(); ) {
             b.append((String)i.next());
             if (i.hasNext()) b.append(' ');
         }
         getLog().info(b.toString());
                 
         return (String[]) cmdLine.toArray(new String[cmdLine.size()]);
     }
 
     private int runCommand(String[] cmdLine) throws MojoExecutionException {
         try {
             Runtime runtime = Runtime.getRuntime();
             Process process = runtime.exec(cmdLine);
             StreamGobbler errorGobbler = new StreamGobbler(process
                     .getErrorStream(), true);
             StreamGobbler outputGobbler = new StreamGobbler(process
                     .getInputStream(), false);
 
             errorGobbler.start();
             outputGobbler.start();
             return process.waitFor();
         } catch (Throwable e) {
             throw new MojoExecutionException("Could not launch " + cmdLine[0],
                     e);
         }
     }
 
     class StreamGobbler extends Thread {
         InputStream is;
 
         boolean error;
 
         StreamGobbler(InputStream is, boolean error) {
             this.is = is;
             this.error = error;
         }
 
         public void run() {
             try {
                 BufferedReader reader = new BufferedReader(
                         new InputStreamReader(is));
                 String line = null;
                 while ((line = reader.readLine()) != null) {
                     if (error) {
                         getLog().error(line);
                     } else {
                         getLog().debug(line);
                     }
                 }
                 reader.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
     }
 }
