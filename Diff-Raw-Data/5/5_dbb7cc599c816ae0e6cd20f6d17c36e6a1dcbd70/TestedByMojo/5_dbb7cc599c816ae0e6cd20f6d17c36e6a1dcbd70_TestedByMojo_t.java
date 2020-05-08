 /*
  * Stefano Maestri, javalinuxlabs.org Copyright 2008, and individual contributors
  * as indicated by the @author tags. See the copyright.txt file in the
  * distribution for a full listing of individual contributors. 
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 
 package it.javalinux.testedby.plugins;
 
 import it.javalinux.testedby.metadata.impl.Helper;
 import it.javalinux.testedby.plugins.scanner.ChangedOrNewSourceScanner;
 import it.javalinux.testedby.plugins.scanner.RunsRepository;
 import it.javalinux.testedby.runner.impl.JunitTestRunner;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.artifact.factory.ArtifactFactory;
 import org.apache.maven.artifact.repository.ArtifactRepository;
 import org.apache.maven.artifact.resolver.ArtifactNotFoundException;
 import org.apache.maven.artifact.resolver.ArtifactResolutionException;
 import org.apache.maven.artifact.resolver.ArtifactResolver;
 import org.apache.maven.model.Dependency;
 import org.apache.maven.model.Plugin;
 import org.apache.maven.plugin.AbstractMojo;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 import org.codehaus.plexus.compiler.util.scan.InclusionScanException;
 import org.codehaus.plexus.compiler.util.scan.SourceInclusionScanner;
 import org.codehaus.plexus.compiler.util.scan.mapping.SuffixMapping;
 import org.codehaus.plexus.util.StringUtils;
 import org.codehaus.plexus.util.cli.CommandLineUtils;
 import org.codehaus.plexus.util.cli.Commandline;
 import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
 
 /**
  * The TestedBy Maven plugin Mojo
  * 
  * @author alessio.soldano@javalinux.it
  * 
  * @goal testedby
  * 
  * @phase test
  * @requiresDependencyResolution compile
  */
 public class TestedByMojo extends AbstractMojo {    
 
     // ------------ Maven parameters and components for accessing dependencies and repositories ------------
     
     /**
      * @parameter default-value="${project}"
      * @required
      * @readonly
      */
     private MavenProject mavenProject;
     
     /**
      * @parameter default-value="${project.dependencies}
      * @required
      * @readonly
      */
     private List<Dependency> dependencies;
     
     /**
      * @component
      */
     private ArtifactResolver resolver;
     
     /**
      * @component
      */
     private ArtifactFactory artifactFactory;
     
     /**
      * @parameter default-value="${localRepository}"
      * @required
      * @readonly
      */
     private ArtifactRepository localRepository;
 
     /**
      * @parameter default-value="${project.remoteArtifactRepositories}"
      * @required
      * @readonly
      */
     private List<ArtifactRepository> remoteRepositories;
     
     /**
      * @parameter default-value="${plugin.artifacts}"
      * @required
      * @readonly
      */
     private List<Artifact> pluginArtifacts;
 
     
 
     // ------------ Parameters leveraging special maven properties for injecting execution status ------------
     
     /**
      * The source directories containing the sources
      *
      * @parameter expression="${project.compileSourceRoots}"
      * @required
      * @readonly
      */
     private List<String> sourceRoots;
 
     /**
      * Project classpath.
      *
      * @parameter expression="${project.compileClasspathElements}"
      * @required
      * @readonly
      */
     private List<String> classpathElements;
     
     /**
     * The compiled classes directory
      * 
     * @parameter expression="${project.build.outputDirectory}"
      * @required
      */
     private File outputDirectory;
     
     /**
      * The source directories containing the test-source
      *
      * @parameter expression="${project.testCompileSourceRoots}"
      * @required
      * @readonly
      */
     private List<String> testSourceRoots;
 
     /**
      * Project test classpath.
      *
      * @parameter expression="${project.testClasspathElements}"
      * @required
      * @readonly
      */
     private List<String> testClasspathElements;
 
     /**
      * The compiled test classes directory
      *
      * @parameter expression="${project.build.testOutputDirectory}"
      * @required
      * @readonly
      */
     private File testOutputDirectory;
 
     
     // ------------ Inclusion / exclusion filters ------------
     
     /**
      * A list of inclusion filters for classesUnderTest.
      *
      * @parameter
      */
     private Set<String> includes = new HashSet<String>();
 
     /**
      * A list of exclusion filters for classesUnderTest.
      *
      * @parameter
      */
     private Set<String> excludes = new HashSet<String>();
 
 
     /**
      * A list of inclusion filters for test classes.
      *
      * @parameter
      */
     private Set<String> testIncludes = new HashSet<String>();
 
     /**
      * A list of exclusion filters for test classes.
      *
      * @parameter
      */
     private Set<String> testExcludes = new HashSet<String>();
 
     
     // ------------ TestedBy additional parameters ------------
     
     /**
      * Set to true to for verbose logging
      *
      * @parameter expression="${maven.compiler.verbose}" default-value="false"
      */
     private boolean verbose;
 
     /**
      * Sets the granularity in milliseconds of the last modification
      * date for testing whether a source needs has changed since last run.
      *
      * @parameter expression="${testedby.lastModGranularityMs}" default-value="0"
      */
     private int staleMillis;
     
     /**
      * Sets the granularity in milliseconds of the last modification
      * date for testing whether a source needs has changed since last run.
      *
      * @parameter expression="${testedby.runnerClass}" default-value="it.javalinux.testedby.runner.impl.JunitTestRunner"
      */
     private String runnerClass = JunitTestRunner.class.getName();
     
     
     // -------------------------------------------------------------------------------------------
     
     /**
      * 
      * {@inheritDoc}
      *
      * @see org.apache.maven.plugin.AbstractMojo#execute()
      */
     public void execute() throws MojoExecutionException {
 	// logs
 	Log log = getLog();
 	if (log.isDebugEnabled()) {
 	    log.debug("Classpath:");
 	    for (String s : getClasspathElements()) {
 		log.debug(" " + s);
 	    }
 	    log.debug("Source roots:");
 	    for (String root : getSourceRoots()) {
 		log.debug(" " + root);
 	    }
 	    log.debug("Output directory:");
 	    log.debug(" " + getOutputDirectory());
 	    log.debug("Test classpath:");
 	    for (String s : getTestClasspathElements()) {
 		log.debug(" " + s);
 	    }
 	    log.debug("Test source roots:");
 	    for (String root : getTestSourceRoots()) {
 		log.debug(" " + root);
 	    }
 	    log.debug("Test output directory:");
 	    log.debug(" " + getTestOutputDirectory());
 	}
 
 	Long time = System.currentTimeMillis();
 	RunsRepository repository = new RunsRepository(getOutputDirectory());
 	repository.load();
 	RunsRepository testRepository = new RunsRepository(getTestOutputDirectory());
 	testRepository.load();
 
 	try {
 	    // creating configuration...
 	    Configuration config = new Configuration();
 	    config.setRunner(getRunnerClass());
 	    
 	    // scanners
 	    SourceInclusionScanner classesUnderTestScanner = getSourceInclusionScanner(repository, getStaleMillis());
 	    Set<File> filesClassesUnderTest = computeChangedSources(getSourceRoots(), classesUnderTestScanner, getOutputDirectory());
 	    List<String> classesUnderTest = config.getChangedClassesUnderTest();
 	    int pos = getOutputDirectory().getCanonicalPath().length();
 	    for (File file : filesClassesUnderTest) {
 		repository.setLastRunTimeMillis(file.getCanonicalPath(), time);
 		String filename = file.getCanonicalPath();
 		classesUnderTest.add(Helper.getCanonicalNameFromJavaAssistName(filename.substring(pos, filename.length() - 5)));
 	    }
 	    SourceInclusionScanner testClassesScanner = getTestSourceInclusionScanner(testRepository, staleMillis);
 	    Set<File> filesTestClasses = computeChangedSources(getTestSourceRoots(), testClassesScanner, getTestOutputDirectory());
 	    List<String> testClasses = config.getChangedTestClasses();
 	    pos = getTestOutputDirectory().getCanonicalPath().length();
 	    for (File file : filesTestClasses) {
 		testRepository.setLastRunTimeMillis(file.getCanonicalPath(), time);
 		String filename = file.getCanonicalPath();
 		testClasses.add(Helper.getCanonicalNameFromJavaAssistName(filename.substring(pos, filename.length() - 5)));
 	    }
 	    
 	    // serializing configuration to temp file
 	    File confFile = File.createTempFile("TestedyBy-maven-plugin-", ".config");
 	    config.save(confFile);
 	    
 	    // preparing arguments and invoking runner in new process
 	    String testedByPluginJar = getBuildPluginArtifactPath("it.javalinux.testedby.plugins", "maven-testedby-plugin");
 	    String testedByJar = getArtifactPath("it.javalinux.testedby", "TestedBy", null, "jar");
 	    String junitJar = getArtifactPath("junit", "junit", null, "jar");
 	    String javassistJar = getArtifactPath("javassist", "javassist", null, "jar");
 
 	    int res = invokeExecutor("java", getExecutorArguments(testedByPluginJar, testedByJar, junitJar, javassistJar, confFile.getCanonicalPath()));
 	    log.info("res="+res);
 	    if (res < 0) {
 		
 	    }
 	    
 	} catch (Exception e) {
 	    throw new MojoExecutionException("Error while running TestedByMojo: ", e);
 	}
 	
 	//saving run
 	try {
 	    repository.save();
 	} catch (IOException e) {
 	    getLog().warn("Unable to write run's data to disk: ", e);
 	}
 	try {
 	    testRepository.save();
 	} catch (IOException e) {
 	    getLog().warn("Unable to write run's data to disk: ", e);
 	}
     }
     
     private String[] getExecutorArguments(String testedByPluginJar, String testedByJar, String junitJar, String javassistJar, String configPath) {
 	List<String> args = new LinkedList<String>();
 	StringBuilder bootCpArg = new StringBuilder("-Xbootclasspath/a:");
 	bootCpArg.append(testedByJar);
 	bootCpArg.append(File.pathSeparator);
 	bootCpArg.append(testedByPluginJar);
 	bootCpArg.append(File.pathSeparator);
 	bootCpArg.append(junitJar);
 	bootCpArg.append(File.pathSeparator);
 	bootCpArg.append(javassistJar);
 	args.add(bootCpArg.toString());
 	args.add("-javaagent:" + testedByJar);
 	List<String> cpElements = getTestClasspathElements();
 	if (cpElements != null && !cpElements.isEmpty()) {
 	    args.add("-cp");
 	    StringBuilder cpArg = new StringBuilder();
 	    for (Iterator<String> it = cpElements.iterator(); it.hasNext();) {
 		cpArg.append(it.next());
 		if (it.hasNext()) {
 		    cpArg.append(File.pathSeparator);
 		}
 	    }
 	    args.add(cpArg.toString());
 	}
 	args.add(Executor.class.getCanonicalName());
 	args.add(configPath);
 	return args.toArray(new String[args.size()]);
     }
     
     private int invokeExecutor(String command, String[] arguments) throws Exception {
 	Commandline cl = new Commandline(command);
 	cl.addArguments(arguments);
 	int returnValue;
 	StringStreamConsumer output = new StringStreamConsumer();
 	StringStreamConsumer error = new StringStreamConsumer();
 	try {
 	    if (getLog().isDebugEnabled()) {
 		getLog().debug("Command line: " + cl);
 	    }
 	    returnValue = CommandLineUtils.executeCommandLine(cl, output, error);
 	} finally {
 	    getLog().info(output.getOutput());
 	    String errorMessage = error.getOutput();
 	    if (!StringUtils.isEmpty(errorMessage)) {
 		returnValue = -1;
 	    }
 	    getLog().error(errorMessage);
 	}
 	return returnValue;
     }
     
     private String getArtifactPath(String groupId, String artifactId, String classifier, String type) throws ArtifactResolutionException, ArtifactNotFoundException, IOException {
 	if (pluginArtifacts != null) {
 	    for (Artifact artifact : pluginArtifacts) {
 		if (StringUtils.equals(groupId, artifact.getGroupId()) && StringUtils.equals(artifactId, artifact.getArtifactId()) &&
 			StringUtils.equals(classifier, artifact.getClassifier()) && StringUtils.equals(type, artifact.getType())) {
 		    resolver.resolve(artifact, remoteRepositories, localRepository);
 		    return artifact.getFile().getCanonicalPath();
 		}
 	    }
 	}
 	if (dependencies != null) {
 	    for (Dependency dep : dependencies) {
 		if (StringUtils.equals(groupId, dep.getGroupId()) && StringUtils.equals(artifactId, dep.getArtifactId()) &&
 			StringUtils.equals(classifier, dep.getClassifier()) && StringUtils.equals(type, dep.getType())) {
 		    Artifact artifact = artifactFactory.createArtifactWithClassifier(groupId, artifactId, dep.getVersion(), type, classifier);
 		    resolver.resolve(artifact, remoteRepositories, localRepository);
 		    return artifact.getFile().getCanonicalPath();
 		}
 	    }
 	}
 	return null;
     }
     
     @SuppressWarnings("unchecked")
     private String getBuildPluginArtifactPath(String groupId, String artifactId) throws ArtifactResolutionException, ArtifactNotFoundException, IOException {
 	List<Plugin> plugins = mavenProject.getBuildPlugins();
 	for (Plugin plugin : plugins) {
 	    if (StringUtils.equals(groupId, plugin.getGroupId()) && StringUtils.equals(artifactId, plugin.getArtifactId())) {
 		Artifact artifact = artifactFactory.createArtifactWithClassifier(plugin.getGroupId(), plugin.getArtifactId(), plugin.getVersion(), "jar", null);
 		resolver.resolve(artifact, remoteRepositories, localRepository);
 		return artifact.getFile().getCanonicalPath();
 	    }
 	}
 	throw new RuntimeException("it.javalinux.testedby.plugins:maven-testedby-plugin not found");
     }
     
     @SuppressWarnings("unchecked")
     private static Set<File> computeChangedSources(List<String> sourceRoots, SourceInclusionScanner scanner, File outputDirectory) throws MojoExecutionException {
 	scanner.addSourceMapping(new SuffixMapping(".java", ".class"));
 	Set<File> changedFiles = new HashSet<File>();
 	for (String sourceRoot : sourceRoots) {
 	    File rootFile = new File(sourceRoot);
 	    if (!rootFile.isDirectory()) {
 		continue;
 	    }
 	    try {
 		changedFiles.addAll(scanner.getIncludedSources(rootFile, outputDirectory));
 	    } catch (InclusionScanException e) {
 		throw new MojoExecutionException("Error scanning source root: \'" + sourceRoot + "\' " + "for changed files since last run.", e);
 	    }
 	}
 	return changedFiles;
     }
     
     protected SourceInclusionScanner getSourceInclusionScanner(RunsRepository repository, int staleMillis) {
 	if (includes.isEmpty()) {
 	    includes.add("**/*.java");
 	}
 	return new ChangedOrNewSourceScanner(staleMillis, includes, excludes, repository);
     }
 
     protected SourceInclusionScanner getTestSourceInclusionScanner(RunsRepository repository, int staleMillis) {
 	if (testIncludes.isEmpty()) {
 	    testIncludes.add("**/*.java");
 	}
 	return new ChangedOrNewSourceScanner(staleMillis, testIncludes, testExcludes, repository);
     }
     
     
     // ------------ package visible getters (for testability) ------------
     
     /**
      * @return sourceRoots
      */
     List<String> getSourceRoots() {
         return sourceRoots;
     }
 
     /**
      * @return classpathElements
      */
     List<String> getClasspathElements() {
         return classpathElements;
     }
 
     /**
      * @return outputDirectory
      */
     File getOutputDirectory() {
         return outputDirectory;
     }
 
     /**
      * @return testSourceRoots
      */
     List<String> getTestSourceRoots() {
         return testSourceRoots;
     }
 
     /**
      * @return testClasspathElements
      */
     List<String> getTestClasspathElements() {
         return testClasspathElements;
     }
 
     /**
      * @return testOutputDirectory
      */
     File getTestOutputDirectory() {
         return testOutputDirectory;
     }
     
     /**
      * @return includes
      */
     Set<String> getIncludes() {
         return includes;
     }
 
     /**
      * @return excludes
      */
     Set<String> getExcludes() {
         return excludes;
     }
 
     /**
      * @return includes
      */
     Set<String> getTestIncludes() {
         return testIncludes;
     }
 
     /**
      * @return excludes
      */
     Set<String> getTestExcludes() {
         return testExcludes;
     }
 
     /**
      * @return verbose
      */
     boolean isVerbose() {
         return verbose;
     }
 
     /**
      * @return staleMillis
      */
     int getStaleMillis() {
         return staleMillis;
     }
     
     /**
      * @return runnerClass
      */
     String getRunnerClass() {
 	return runnerClass;
     }
     
     MavenProject getMavenProject() {
 	return mavenProject;
     }
 
 }
