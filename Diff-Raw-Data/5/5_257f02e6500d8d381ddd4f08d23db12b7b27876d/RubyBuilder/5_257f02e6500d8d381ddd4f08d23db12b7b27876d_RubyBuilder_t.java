 /**
  * 
  */
 package org.rubypeople.rdt.internal.core.builder;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.rubypeople.rdt.core.IRubyModelMarker;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.internal.core.parser.MarkerUtility;
 import org.rubypeople.rdt.internal.core.parser.RdtWarnings;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 import org.rubypeople.rdt.internal.core.parser.TaskParser;
 
 /**
  * @author Chris
  * 
  */
 public class RubyBuilder extends IncrementalProjectBuilder {
 
 	private static final boolean DEBUG = false;
 	private static final int MAX_AT_ONCE = 1000;
 	private IProject currentProject;
 	private int totalWork = 10000;
 	private WorkQueue workQueue;
 	private boolean compiledAllAtOnce;
 	private int percentPerUnit = 0;
 	private int grandTotal = 0;
 
 	private RdtWarnings warnings;
 	private RubyParser parser;
 	private TaskParser taskParser;
 	
 	public RubyBuilder() {
 		this.workQueue = new WorkQueue();
 		warnings = new RdtWarnings();
 		parser = new RubyParser(warnings);
 		IEclipsePreferences preferences = RubyCore.getInstancePreferences();
 		taskParser = new TaskParser(preferences);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
 	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
 	 */
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
 		monitor.beginTask("build", totalWork);
 		IProject[] returnProjects = new IProject[0];
 		this.currentProject = getProject();
 		if (currentProject == null || !currentProject.isAccessible()) return returnProjects;
 
 		checkCancel(monitor);
 
 		if (kind == INCREMENTAL_BUILD || kind == AUTO_BUILD) {
 			if (DEBUG) System.out.println("INCREMENTAL build...");
 			workQueue.clear();
 			IResourceDelta delta = getDelta(currentProject);
 			List files = getAffectedFiles(delta.getAffectedChildren());
 			IFile[] fileArray = new IFile[files.size()];
 			System.arraycopy(files.toArray(), 0, fileArray, 0, fileArray.length);
 			compile(fileArray, monitor);
 			cleanUp();
 			return returnProjects;
 		}
 		build(monitor);
 		cleanUp();
 
 		// FIXME Get the required projects as in JavaBuilder
 		if (DEBUG) System.out.println("Finished build of " + currentProject.getName() //$NON-NLS-1$
 				+ " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
 		return returnProjects;
 	}
 
 	private void cleanUp() {
 		workQueue.clear();
 		percentPerUnit = 0;
 		warnings.clear();
 		taskParser.clear();
 	}
 
 	/**
 	 * Check whether the build has been canceled.
 	 * 
 	 * @param monitor
 	 */
 	public void checkCancel(IProgressMonitor monitor) {
 		if (monitor != null && monitor.isCanceled()) throw new OperationCanceledException();
 	}
 
 	private List getAffectedFiles(IResourceDelta[] deltas) {
 		List files = new ArrayList();
 		for (int i = 0; i < deltas.length; i++) {
 			IResourceDelta curDelta = deltas[i];
 			// Skip removals, we don't want to parse those
 			if (curDelta.getKind() == IResourceDelta.REMOVED) continue;
 			IResource resource = curDelta.getResource();
 			if (resource.getType() == IResource.FOLDER || resource.getType() == IResource.PROJECT) {
 				files.addAll(getAffectedFiles(curDelta.getAffectedChildren()));
 				continue;
 			}
 			if (resource.getType() != IResource.FILE) continue;
 			// FIXME This uses the Util class to check if teh filename looks like a ruby file, use behavior like RubyFileMatcher
 			if (!org.rubypeople.rdt.internal.core.util.Util.isRubyLikeFileName(resource.getName())) continue;
 			files.add(resource);
 		}
 		return files;
 	}
 
 	private void build(IProgressMonitor monitor) {
 		if (DEBUG) System.out.println("FULL build"); //$NON-NLS-1$
 
 		try {
 			RubyBuilder.removeProblemsAndTasksFor(currentProject);
 
 			ArrayList sourceFiles = new ArrayList(33);
 			addAllSourceFiles(sourceFiles);
 
 			if (sourceFiles.size() > 0) {
 				IFile[] allSourceFiles = new IFile[sourceFiles.size()];
 				sourceFiles.toArray(allSourceFiles);
 				workQueue.clear();
 				workQueue.addAll(allSourceFiles);
 				compile(allSourceFiles, monitor);
 			}
 
 		} catch (CoreException e) {
 			// throw internalException(e);
 			throw new RuntimeException(e);
 		}
 	}
 
 	public static void removeProblemsAndTasksFor(IResource resource) {
 		try {
 			if (resource != null && resource.exists()) {
 				resource.deleteMarkers(IRubyModelMarker.RUBY_MODEL_PROBLEM_MARKER, false, IResource.DEPTH_INFINITE);
 				resource.deleteMarkers(IRubyModelMarker.TASK_MARKER, false, IResource.DEPTH_INFINITE);
 			}
 		} catch (CoreException e) {
 			// assume there were no problems
 		}
 	}
 
 	protected void addAllSourceFiles(final ArrayList sourceFiles) throws CoreException {
 		currentProject.accept(new IResourceProxyVisitor() {
 
 			public boolean visit(IResourceProxy proxy) throws CoreException {
 				IResource resource = null;
 				switch (proxy.getType()) {
 				case IResource.FILE:
 					if (org.rubypeople.rdt.internal.core.util.Util.isRubyLikeFileName(proxy.getName())) {
 						if (resource == null) resource = proxy.requestResource();
 						sourceFiles.add(resource);
 					}
 					return false;
 				}
 				return true;
 			}
 		}, IResource.NONE);
 	}
 
 	/*
 	 * Compile the given elements, adding more elements to the work queue if
 	 * they are affected by the changes.
 	 */
 	protected void compile(IFile[] units, IProgressMonitor monitor) {
        if (units == null) return;
 		int unitsLength = units.length;
        grandTotal  = unitsLength;
        if (unitsLength == 0) return;
 		percentPerUnit  = totalWork / unitsLength;
 
 		this.compiledAllAtOnce = unitsLength <= MAX_AT_ONCE;
 		if (this.compiledAllAtOnce) {
 			// do them all now
 			doCompile(units, monitor);
 		} else {
 			int i = 0;
 			boolean compilingFirstGroup = true;
 			while (i < unitsLength) {
 				int doNow = unitsLength < MAX_AT_ONCE ? unitsLength : MAX_AT_ONCE;
 				int index = 0;
 				IFile[] toCompile = new IFile[doNow];
 				while (i < unitsLength && index < doNow) {
 					// Although it needed compiling when this method was called,
 					// it may have
 					// already been compiled when it was referenced by another
 					// unit.
 					IFile unit = units[i++];
 					if (compilingFirstGroup || workQueue.isWaiting(unit)) {
 						toCompile[index++] = unit;
 					}
 				}
 				if (index < doNow) System.arraycopy(toCompile, 0, toCompile = new IFile[index], 0, index);
 				IFile[] additionalUnits = new IFile[unitsLength - i];
 				System.arraycopy(units, i, additionalUnits, 0, additionalUnits.length);
 				compilingFirstGroup = false;
 				doCompile(toCompile, monitor);
 			}
 		}
 	}
 
 	/*
 	 * Compile the given elements, adding more elements to the work queue if
 	 * they are affected by the changes.
 	 */
 	protected void doCompile(IFile[] units, IProgressMonitor monitor) {
 		int unitsLength = units.length;
 		if (unitsLength == 0) {
 			monitor.worked(unitsLength * percentPerUnit);
 			return;
 		}
 
 		// do them all now
 		for (int i = 0; i < unitsLength; i++) {
 			checkCancel(monitor);
 			Reader reader = null;
 			try {
 				IFile file = units[i];
 				if (DEBUG) System.out.println("About to compile " + file); //$NON-NLS-1$
 				String name = file.getFullPath().makeRelative().toString();
 				monitor.subTask(name + ": (" + i + " of " + grandTotal + ")");				
 				
 				removeProblemsAndTasksFor(file);
 				reader = new InputStreamReader(file.getContents());
 				try {
 					parser.parse(units[i].getName(), reader);
 				} catch (SyntaxException e) {
 					MarkerUtility.createSyntaxError(file, e);
 				}
 				MarkerUtility.createProblemMarkers(file, warnings.getWarnings());
 				createTasks(file);
 				monitor.worked(percentPerUnit);
 			} catch (CoreException e) {
 				RubyCore.log(e);
 			} catch (IOException e) {
 				RubyCore.log(e);
 			} finally {
 				try {
 					reader.close();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			}
 			warnings.clear();
 		}
 		checkCancel(monitor);
 	}
 
 	private void createTasks(IFile file) throws CoreException, IOException {
 		InputStream contents = file.getContents();
 		try {
 			taskParser.clear();
 			taskParser.parse(new InputStreamReader(contents));
 			MarkerUtility.createTasks(file, taskParser.getTasks());
 			taskParser.clear();
 		} finally {
 			closeSilently(contents);
 		}
 	}
 
 	private void closeSilently(InputStream contents) {
 		try {
 			contents.close();
 		} catch (IOException e) {
 		}
 	}
 }
