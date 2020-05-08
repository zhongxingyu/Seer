 package org.eclipse.dltk.internal.core.builder;
 
 import java.io.DataInputStream;
 import java.io.DataOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.IncrementalProjectBuilder;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IBuildpathEntry;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IDLTKProject;
 import org.eclipse.dltk.core.IModelElement;
 import org.eclipse.dltk.core.IModelElementVisitor;
 import org.eclipse.dltk.core.IModelMarker;
 import org.eclipse.dltk.core.IProjectFragment;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.builder.IScriptBuilder;
 import org.eclipse.dltk.core.search.IDLTKSearchScope;
 import org.eclipse.dltk.core.search.SearchEngine;
 import org.eclipse.dltk.internal.core.BuildpathEntry;
 import org.eclipse.dltk.internal.core.DLTKProject;
 import org.eclipse.dltk.internal.core.ExternalProjectFragment;
 import org.eclipse.dltk.internal.core.ExternalSourceModule;
 import org.eclipse.dltk.internal.core.ModelManager;
 import org.eclipse.dltk.internal.core.util.HandleFactory;
 
 public class ScriptBuilder extends IncrementalProjectBuilder {
 	public static final boolean DEBUG = DLTKCore.DEBUG_SCRIPT_BUILDER;
 
 	public IProject currentProject = null;
 	DLTKProject scriptProject = null;
 	State lastState;
 
 	class ResourceVisitor implements IResourceDeltaVisitor, IResourceVisitor {
 		private List resources;
 
 		public ResourceVisitor(List resources) {
 			this.resources = resources;
 		}
 
 		public boolean visit(IResourceDelta delta) throws CoreException {
 			IResource resource = delta.getResource();
 			switch (delta.getKind()) {
 			case IResourceDelta.ADDED:
 			case IResourceDelta.CHANGED:
 				if (!this.resources.contains(resource)
 						&& resource.getType() == IResource.FILE) {
 					resources.add(resource);
 				}
 				break;
 			}
 			return true;
 		}
 
 		public boolean visit(IResource resource) {
 			if (!this.resources.contains(resource)
 					&& resource.getType() == IResource.FILE) {
 				resources.add(resource);
 			}
 			return true;
 		}
 	}
 
 	class ExternalModuleVisitor implements IModelElementVisitor {
 		private List elements;
 
 		public ExternalModuleVisitor(List elements) {
 			this.elements = elements;
 		}
 
 		/**
 		 * Visit only external source modules, witch we aren't builded yet.
 		 */
 		public boolean visit(IModelElement element) {
 			if (element.getElementType() == IModelElement.PROJECT_FRAGMENT) {
 				if (!(element instanceof ExternalProjectFragment)) {
 					return false;
 				}
 				IProjectFragment fragment = (IProjectFragment) element;
 				
 				if (lastState.externalFolderLocations.contains(fragment.getPath())) {
 					return false;
 				} else {
 					lastState.externalFolderLocations.add(fragment.getPath());
 				}
 			}
 			if (element.getElementType() == IModelElement.SOURCE_MODULE
 					&& element instanceof ExternalSourceModule) {
 				if (!elements.contains(element)) {
 					elements.add(element);
 				}
 				return false; // do not enter into source module content.
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * Hook allowing to initialize some static state before a complete build
 	 * iteration. This hook is invoked during PRE_AUTO_BUILD notification
 	 */
 	public static void buildStarting() {
 		// build is about to start
 	}
 
 	/**
 	 * Hook allowing to reset some static state after a complete build
 	 * iteration. This hook is invoked during POST_AUTO_BUILD notification
 	 */
 	public static void buildFinished() {
 		if (DLTKCore.DEBUG)
 			System.out.println("build finished");
 	}
 
 	protected IProject[] build(int kind, Map args, IProgressMonitor monitor)
 			throws CoreException {
 		this.currentProject = getProject();
 		
 		if( !DLTKLanguageManager.hasScriptNature(this.currentProject) ) {
 			return null;
 		}
 		this.scriptProject = (DLTKProject) DLTKCore.create(currentProject);
 
 		if (currentProject == null || !currentProject.isAccessible())
 			return new IProject[0];
 
 		if (kind == FULL_BUILD) {
 			fullBuild(monitor);
 		} else {
 			if ((this.lastState = getLastState(currentProject, monitor )) == null) {
 				if (DEBUG)
 					System.out
 							.println("Performing full build since last saved state was not found"); //$NON-NLS-1$
 				fullBuild(monitor);
 			} else {
 				IResourceDelta delta = getDelta(getProject());
 				if (delta == null) {
 					fullBuild(monitor);
 				} else {
 					incrementalBuild(delta, monitor);
 				}
 			}
 		}
 		IProject[] requiredProjects = getRequiredProjects(true);
 		if (DEBUG)
 			System.out.println("Finished build of " + currentProject.getName() //$NON-NLS-1$
 				+ " @ " + new Date(System.currentTimeMillis())); //$NON-NLS-1$
 		return requiredProjects;
 	}
 	private IProject[] getRequiredProjects(boolean includeBinaryPrerequisites) {
 		if (scriptProject == null ) return new IProject[0];
 		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
 		ArrayList projects = new ArrayList();
 		try {
 			IBuildpathEntry[] entries = scriptProject.getExpandedBuildpath(true);
 			for (int i = 0, l = entries.length; i < l; i++) {
 				IBuildpathEntry entry = entries[i];
 				IPath path = entry.getPath();
 				IProject p = null;
 				switch (entry.getEntryKind()) {
 					case IBuildpathEntry.BPE_PROJECT :
 						p = workspaceRoot.getProject(path.lastSegment()); // missing projects are considered too
 						if (((BuildpathEntry) entry).isOptional() && !DLTKProject.hasScriptNature(p)) // except if entry is optional
 							p = null;
 						break;
 					case IBuildpathEntry.BPE_LIBRARY :
 						if (includeBinaryPrerequisites && path.segmentCount() > 1) {
 							// some binary resources on the class path can come from projects that are not included in the project references
 							IResource resource = workspaceRoot.findMember(path.segment(0));
 							if (resource instanceof IProject)
 								p = (IProject) resource;
 						}
 				}
 				if (p != null && !projects.contains(p))
 					projects.add(p);
 			}
 		} catch(ModelException e) {
 			return new IProject[0];
 		}
 		IProject[] result = new IProject[projects.size()];
 		projects.toArray(result);
 		return result;
 	}
 
 	public State getLastState(IProject project, IProgressMonitor monitor) {
 		return (State) ModelManager.getModelManager().getLastBuiltState(project, monitor);
 	}
 	private void clearLastState() {
 		ModelManager.getModelManager().setLastBuiltState(currentProject, null);
 	}
 	protected void fullBuild(final IProgressMonitor monitor)
 			throws CoreException {
 		
 		clearLastState();
 		State newState = new State(this);
		if( this.lastState != null ) {
			newState.copyFrom(this.lastState);
		}
 		lastState = newState;
 		try {
 			List resources = new ArrayList();
 
 			currentProject.accept(new ResourceVisitor(resources));
 
 			// Project external resources should also be added into list. Only
 			// on full build we need to manage this.
 			List elements = new ArrayList();
 			scriptProject.accept(new ExternalModuleVisitor(elements));
 
 			// Call builders for resources.
 			int count = resources.size() + elements.size();
 			monitor.beginTask("Building", count);
 			buildResources(resources, monitor);
 			buildElements(elements, monitor);
 			monitor.done();
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		finally {
 			ModelManager.getModelManager().setLastBuiltState(currentProject, this.lastState);
 		}
 	}
 
 	protected void incrementalBuild(IResourceDelta delta,
 			IProgressMonitor monitor) throws CoreException {
 		List resources = new ArrayList();
 		delta.accept(new ResourceVisitor(resources));
 		// Call builders for resources.
 		List actualResourcesToBuild = findDependencies(resources);
 
 		List elements = new ArrayList();
 		scriptProject.accept(new ExternalModuleVisitor(elements));
 
 		monitor.beginTask("Building", actualResourcesToBuild.size()
 				+ elements.size());
 		buildResources(actualResourcesToBuild, monitor);
 		buildElements(elements, monitor);
 		monitor.done();
 	}
 
 	private HandleFactory factory = new HandleFactory();
 
 	protected void buildResources(List resources, IProgressMonitor monitor) {
 		List status = new ArrayList();
 		IDLTKSearchScope scope = SearchEngine
 				.createSearchScope(new IModelElement[] { scriptProject });
 
 		List realResources = new ArrayList(); // real resources
 		List elements = new ArrayList(); // Model elements
 
 		for (int i = 0; i < resources.size(); ++i) {
 			monitor.worked(1);
 			if (monitor.isCanceled()) {
 				return;
 			}
 			IResource res = (IResource) resources.get(i);
 			IModelElement element = factory.createOpenable(res.getFullPath()
 					.toString(), scope);
 			if (element != null
 					&& element.getElementType() == IModelElement.SOURCE_MODULE
 					&& element.exists()) {
 				elements.add(element);
 			} else {
 				realResources.add(res);
 			}
 		}
 		buildElements(elements, monitor);
 
 		// Else build as resource.
 		String[] natureIds = null;
 		try {
 			natureIds = currentProject.getDescription().getNatureIds();
 		} catch (CoreException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 			return;
 		}
 		for (int j = 0; j < natureIds.length; j++) {
 			try {
 				IScriptBuilder[] builders = ScriptBuilderManager
 						.getScriptBuilders(natureIds[j]);
 				if (builders != null) {
 					for (int k = 0; k < builders.length; k++) {
 						IStatus s = builders[k].buildResources( this.scriptProject, realResources,
 								monitor);
 						if (s != null && s.getSeverity() != IStatus.OK) {
 							status.add(s);
 						}
 					}
 				}
 			} catch (CoreException e) {
 				if (DLTKCore.DEBUG) {
 					e.printStackTrace();
 				}
 			}
 		}
 		// TODO: Do something with status.
 	}
 
 	protected void buildElements(List elements, IProgressMonitor monitor) {
 		List status = new ArrayList();
 		IDLTKLanguageToolkit toolkit = null;
 		try {
 			toolkit = DLTKLanguageManager.getLanguageToolkit(scriptProject);
 			IScriptBuilder[] builders = ScriptBuilderManager
 					.getScriptBuilders(toolkit.getNatureID());
 
 			if (builders != null) {
 				for (int k = 0; k < builders.length; k++) {
 					IStatus s = builders[k].buildModelElements(scriptProject,
 							elements, monitor);
 					if (s != null && s.getSeverity() != IStatus.OK) {
 						status.add(s);
 					}
 				}
 
 			}
 		} catch (CoreException e) {
 			e.printStackTrace();
 			return;
 		}
 		// TODO: Do something with status.
 	}
 
 	private List findDependencies(List resources) {
 		try {
 			IScriptBuilder[] builders = ScriptBuilderManager
 					.getAllScriptBuilders();
 
 			List elementsToCheck = new ArrayList();
 			elementsToCheck.addAll(resources);
 			List result = new ArrayList();
 			result.addAll(resources);
 			while (elementsToCheck.size() > 0) {
 				List newElementsToCheck = new ArrayList();
 				for (int i = 0; i < builders.length; ++i) {
 					List newResources = builders[i]
 							.getDependencies(this.scriptProject, elementsToCheck);
 					if (newResources != null) {
 						newElementsToCheck.addAll(newResources);
 					}
 				}
 				for (int i = 0; i < newElementsToCheck.size(); ++i) {
 					Object o = newElementsToCheck.get(i);
 					if (!result.contains(o)) {
 						result.add(o);
 					}
 				}
 				elementsToCheck.clear();
 				elementsToCheck.addAll(newElementsToCheck);
 			}
 			return result;
 		} catch (CoreException e) {
 			e.printStackTrace();
 		}
 		return resources;
 	}
 
 	public static void removeProblemsAndTasksFor(IResource resource) {
 		try {
 			if (resource != null && resource.exists()) {
 				resource.deleteMarkers(
 						IModelMarker.SCRIPT_MODEL_PROBLEM_MARKER, false,
 						IResource.DEPTH_INFINITE);
 				resource.deleteMarkers(IModelMarker.TASK_MARKER, false,
 						IResource.DEPTH_INFINITE);
 
 				// delete managed markers
 			}
 		} catch (CoreException e) {
 			// assume there were no problems
 		}
 	}
 
 	public static void writeState(Object state, DataOutputStream out)
 			throws IOException {
 		((State) state).write(out);
 	}
 
 	public static State readState(IProject project, DataInputStream in)
 			throws IOException {
 		State state = State.read(project, in);
 		return state;
 	}
 }
