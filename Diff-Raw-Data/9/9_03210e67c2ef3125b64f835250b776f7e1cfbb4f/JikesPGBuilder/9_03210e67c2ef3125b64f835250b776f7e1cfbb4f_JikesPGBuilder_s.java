 package org.jikespg.uide.builder;
 
 /*
  * Licensed Materials - Property of IBM,
  * (c) Copyright IBM Corp. 1998, 2004  All Rights Reserved
  */
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.URL;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.jikespg.uide.views.JikesPGView;
 import org.osgi.framework.Bundle;
 
 /**
  * @author CLaffra
  */
 public class JikesPGBuilder extends IncrementalProjectBuilder {
     private static final String PLUGIN_ID= "org.jikespg.uide";
 
     private static final String JIKESPG_PROBLEM_MARKER= "org.jikespg.uide.problem";
 
     String lpg;
 
     String outdir;
 
     String incdir;
 
     private final String MSG_REGEXP= "(.*):([0-9]+):([0-9]+):([0-9]+):([0-9]+):([0-9]+):([0-9]+): (.*)";
 
     private final Pattern MSG_PATTERN= Pattern.compile(MSG_REGEXP);
 
     protected IProject[] build(int kind, Map args, IProgressMonitor monitor) {
 	try {
 	    if (kind == IncrementalProjectBuilder.FULL_BUILD) {
 		fullBuild(monitor);
 	    } else {
 		IResourceDelta delta= getDelta(getProject());
 		if (delta == null) {
 		    fullBuild(monitor);
 		} else {
 		    incrementalBuild(delta, monitor);
 		}
 	    }
 	} catch (CoreException e) {
 	    e.printStackTrace();
 	}
 	return new IProject[0];
     }
 
     private boolean isGrammarFile(IResource resource) {
 	IPath path= resource.getRawLocation();
 	if (path == null) return false;
 
 	String fileName= path.toString();
 	return (fileName.indexOf("/bin/") == -1 && "g".equals(path.getFileExtension()));
     }
 
     private void fullBuild(IProgressMonitor monitor) throws CoreException {
 	getProject().accept(new IResourceVisitor() {
 	    public boolean visit(IResource resource) throws CoreException {
 		if (isGrammarFile(resource) && resource.exists()) {
 		    compile(resource);
 		    return false;
 		}
 		return true;
 	    }
 	});
     }
 
     private void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
 	delta.accept(new IResourceDeltaVisitor() {
 	    public boolean visit(IResourceDelta delta) {
 		IResource resource= delta.getResource();
 		if (isGrammarFile(resource) && resource.exists()) {
 		    try {
 			resource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
 		    } catch (CoreException e) {
 		    }
 		    compile(resource);
 		    return false;
 		}
 		return true; // visit children too
 	    }
 	});
     }
 
     protected void compile(final IResource resource) {
 	String fileName= resource.getLocation().toOSString();
 	// IPath projectRelativePath = currentResource.getProjectRelativePath();
 	// System.out.println("JikesPG builder runs on "+currentResource.getProject()+". Compiling "+fileName);
 	try {
 	    File includeDir= new File(fileName).getParentFile();
 	    //			String includeDirName = includeDir.getAbsolutePath();
 	    String cmd[]= new String[] {
 		    getLpgExecutable(),
 		    "-list",
 		    "-table=java",
 		    "-escape=$",
 		    //"-scopes",		// generates GPF
 		    "-em",
 		    "-parseTable=org.jikes.lpg.runtime.*",
 		    // TODO RMF 7/21/05 -- probably shouldn't use this - performance issue - ask Philippe for details
 		    "-dat-directory=" + getOutputDirectory(resource.getProject()),
 		    fileName.replace('/', '\\') };
 	    // String projectLocation = currentResource.getProject().getLocation().toOSString();
 	    Process process= Runtime.getRuntime().exec(cmd, new String[0], includeDir);
 	    JikesPGView consoleView= JikesPGView.getDefault();
 
 	    processJikesPGOutput(resource, process, consoleView);
 	    processJikesPGErrors(resource, process, consoleView);
 	    doRefresh(resource);
 	} catch (Exception e) {
 	    e.printStackTrace();
 	}
     }
 
     private void doRefresh(final IResource resource) {
 	new Thread() {
 	    public void run() {
 		try {
 		    resource.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
 		} catch (CoreException e) {
 		    e.printStackTrace();
 		}
 	    }
 	}.start();
     }
 
     private void processJikesPGErrors(IResource resource, Process process, JikesPGView view) throws IOException {
 	InputStream is= process.getErrorStream();
 	BufferedReader in2= new BufferedReader(new InputStreamReader(is));
 
 	String line;
 	while ((line= in2.readLine()) != null) {
 	    if (view != null)
 		JikesPGView.println(line);
 	    System.err.println(line);
 	}
 	is.close();
     }
 
     private void processJikesPGOutput(final IResource resource, Process process, JikesPGView view) throws IOException {
 	InputStream is= process.getInputStream();
 	BufferedReader in= new BufferedReader(new InputStreamReader(is));
 	String line= null;
 	while ((line= in.readLine()) != null) {
 	    if (view != null)
 		JikesPGView.println(line);
 	    else {
 		System.out.println(line);
 	    }
 	    final String msg= line;
 	    if (msg.indexOf(": Syntax error detected") >= 0) {
 		Matcher matcher= MSG_PATTERN.matcher(msg);
 
 		if (matcher.matches()) {
 		    String errorFile= matcher.group(1);
 		    int startLine= Integer.parseInt(matcher.group(2));
 		    int startCol= Integer.parseInt(matcher.group(3));
 		    int endLine= Integer.parseInt(matcher.group(4));
 		    int endCol= Integer.parseInt(matcher.group(5));
 		    String descrip= matcher.group(8);
 
 		    try {
 			IMarker m= resource.createMarker(JIKESPG_PROBLEM_MARKER);
 
 			m.setAttribute(IMarker.LINE_NUMBER, startLine);
 			m.setAttribute(IMarker.MESSAGE, descrip);
 			m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
 			m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
 		    } catch (CoreException e) {
 			System.err.println(e.getMessage());
 			e.printStackTrace();
 		    }
 		}
 //		Display.getDefault().asyncExec(new Runnable() {
 //		    public void run() {
 //			MessageDialog.openError(new Shell(), "JikesPG: Grammar Error", msg);
 //		    }
 //		});
 	    }
 	}
     }
 
     String getOutputDirectory(IProject project) throws IOException {
 	if (outdir == null) {
 	    outdir= new File(project.getLocation().toFile(), "/src").getAbsolutePath().replace('\\', '/');
 	}
 	return outdir;
 
     }
 
     String getInputDirectory(IProject project) throws IOException {
 	if (incdir == null) {
 	    File file= new File(project.getLocation().toFile(), "/src");
 	    file.mkdirs();
 	    file.mkdir();
 	    incdir= file.getAbsolutePath().replace('\\', '/');
 	}
 	return incdir;
 
     }
 
     String getLpgExecutable() throws IOException {
 	if (lpg == null) {
 	    Bundle bundle= Platform.getBundle(PLUGIN_ID);
 	    Path path= new Path("lpg.exe");
 	    URL url= Platform.resolve(Platform.find(bundle, path));
 	    lpg= url.getFile();
 	    if (lpg.startsWith("/"))
 		lpg= lpg.substring(1);
 	    lpg= lpg.replace('/', '\\');
 	}
 	return lpg;
     }
 }
