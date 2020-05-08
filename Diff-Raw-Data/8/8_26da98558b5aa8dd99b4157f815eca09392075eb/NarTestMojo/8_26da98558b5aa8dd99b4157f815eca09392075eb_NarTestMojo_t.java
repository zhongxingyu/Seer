 // Copyright FreeHEP, 2005-2007.
 package org.freehep.maven.nar;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.maven.artifact.Artifact;
 import org.apache.maven.plugin.MojoExecutionException;
 import org.apache.maven.plugin.MojoFailureException;
 import org.apache.maven.plugin.logging.Log;
 import org.apache.maven.project.MavenProject;
 
 /**
  * Tests NAR files. Runs Native Tests and executables if produced.
  * 
  * @goal nar-test
  * @phase test
  * @requiresProject
  * @author <a href="Mark.Donszelmann@slac.stanford.edu">Mark Donszelmann</a>
 * @version $Id: plugin/src/main/java/org/freehep/maven/nar/NarTestMojo.java fd78a3770f65 2007/07/20 16:19:50 duns $
  */
 public class NarTestMojo extends AbstractCompileMojo {
 
 	public void execute() throws MojoExecutionException, MojoFailureException {
 		if (shouldSkip())
 			return;
 
 		// run all tests
 		for (Iterator i = getTests().iterator(); i.hasNext();) {
 			runTest((Test) i.next());
 		}
 
 		for (Iterator i = getLibraries().iterator(); i.hasNext();) {
 			runExecutable((Library) i.next());
 		}
 	}
 
 	private void runTest(Test test) throws MojoExecutionException,
 			MojoFailureException {
 		// run if requested
 		if (test.shouldRun()) {
 			String name = "target/test-nar/bin/" + getAOL() + "/" + test.getName();
 			getLog().info("Running " + name);
 			int result = NarUtil.runCommand(generateCommandLine(getMavenProject()
 					.getBasedir()
 					+ "/" + name, test), generateEnvironment(test,
 					getLog()), getLog());
 			if (result != 0)
 				throw new MojoFailureException("Test " + name
 						+ " failed with exit code: " + result);
 		}
 	}
 
 	private void runExecutable(Library library) throws MojoExecutionException,
 			MojoFailureException {
 		if (library.getType().equals(Library.EXECUTABLE) && library.shouldRun()) {
 			MavenProject project = getMavenProject();
 			String name = "target/nar/bin/" + getAOL() + "/"
 					+ project.getArtifactId();
 			getLog().info("Running " + name);
 			int result = NarUtil.runCommand(generateCommandLine(project.getBasedir()
 					+ "/" + name, library), generateEnvironment(
 					library, getLog()), getLog());
 			if (result != 0)
 				throw new MojoFailureException("Test " + name
 						+ " failed with exit code: " + result);
 		}
 	}
 
 	protected File getTargetDirectory() {
 		return new File(getMavenProject().getBuild().getDirectory(), "test-nar");
 	}
 
 	private String[] generateCommandLine(String name, Executable exec)
 			throws MojoExecutionException {
 
 		List cmdLine = new ArrayList();
 
 		cmdLine.add(name);
 
 		cmdLine.addAll(exec.getArgs());
 
 		return (String[]) cmdLine.toArray(new String[cmdLine.size()]);
 	}
 
 	private String[] generateEnvironment(Executable exec, Log log)
 			throws MojoExecutionException, MojoFailureException {
 		List env = new ArrayList();
 
 		Set/*<File>*/ sharedPaths = new HashSet();
 		
 		// add all shared libraries of this package
 		for (Iterator i=getLibraries().iterator(); i.hasNext(); ) {
 			Library lib = (Library)i.next();
 			if (lib.getType().equals(Library.SHARED)) {
 				sharedPaths.add(new File(getMavenProject().getBasedir(), "target/nar/lib/"+getAOL()+"/"+lib.getType()));
 			}
 		}
 
 		// add dependent shared libraries
 		String classifier = getAOL()+"-shared";
 		List narArtifacts = getNarManager().getNarDependencies("compile");
 		List dependencies = getNarManager().getAttachedNarDependencies(
 				narArtifacts, classifier);
 		for (Iterator d = dependencies.iterator(); d.hasNext();) {
 			Artifact dependency = (Artifact) d.next();
 			getLog().debug("Looking for dependency " + dependency);
 
 			// FIXME reported to maven developer list, isSnapshot
 			// changes behaviour
 			// of getBaseVersion, called in pathOf.
 			if (dependency.isSnapshot())
 				;
 
 			File libDir = new File(getLocalRepository().pathOf(dependency));
 			libDir = new File(getLocalRepository().getBasedir(), libDir
 					.getParent());
 			libDir = new File(libDir, "nar/lib/"+getAOL()+"/shared");
 			sharedPaths.add(libDir);
 		}
 		
 		// set environment
 		if (sharedPaths.size() > 0) {
 			String sharedPath = "";
 			for (Iterator i=sharedPaths.iterator(); i.hasNext(); ) {
 				sharedPath += ((File)i.next()).getPath();
 				if (i.hasNext()) sharedPath += File.pathSeparator;
 			}
 			
 			String sharedPathName = getOS().equals(OS.MACOSX) ? "DYLD_LIBRARY_PATH" : getOS().startsWith("Windows") ? "PATH" : "LD_LIBRARY_PATH";
 			env.add(sharedPathName+"="+sharedPath);
 		}
 		
 		return (String[]) env.toArray(new String[env.size()]);
 	}
 }
