 /*******************************************************************************
  * Copyright (c) 2001, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jem.workbench.utility;
 
 /*
 * $RCSfile: JavaModelListener.java,v $ $Revision: 1.4 $ $Date: 2006/09/11 23:42:31 $
  */
 
 import java.util.*;
 
 import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.*;
 
 import org.eclipse.jem.internal.core.JEMPlugin;
 
 /**
  * An element change listener to listen for Java Model changes. It breaks the notification up into individual method calls to make it easier to walk
  * the notification tree.
  * <p>
  * When finished with this listener, the new approved API <b>requires</b> calling {@link #releaseListener()}. The old way of directly calling
  * JavaCore to do the release will cause resource leaks if {@link #initializeClasspaths()} was called.
  * <p>
  * <b>Note:</b> If the subclass implementation will be calling {@link #isInClasspath(IJavaProject)} then a call to {@link #initializeClasspaths()}
  * will be required at the appropriate time. See the comments for the initialize method for info. If the initialize is not called, then
  * when/if isInClasspath is called then the older way of determining if it is in the classpath will be called. This may result in a deadlock
  * depending on the situation, so it is better to use initializeClasspaths and releaseListener instead of the old way.
  * <p>
  * <b>Subclass implementation details:</b> Subclass implementations should not override {@link #elementChanged(ElementChangedEvent)}. 
  * The elementChanged method should be made final but it cannot because the API would be broken for older subclassers. If they wish to
  * override what elementChanged does, the API way is to override {@link #processElementChanged(ElementChangedEvent)} instead.  There the
  * subclass can do additional processing (by still calling super.processElementChanged) or replace it entirely. That
  * way the processing of the classpath projects list will still be done.  
  * Older subclass implementations must call {@link #elementChanged(ElementChangedEvent)} if they
  * have overridden it. This is necessary for {@link #isInClasspath(IJavaProject)} to be processed correctly. 
  * @since 1.2.0
  */
 public abstract class JavaModelListener implements IElementChangedListener {
 
 	/**
 	 * Construct with listening only for {@link ElementChangedEvent#POST_CHANGE} events.
 	 * 
 	 * 
 	 * @since 1.2.0
 	 */
 	public JavaModelListener() {
 		this(ElementChangedEvent.POST_CHANGE);
 	}
 
 	/**
 	 * Construct with supplying the type of events to listen for.
 	 * 
 	 * @param eventsToListen
 	 *            or'd together event listening types. See {@link ElementChangedEvent} for the types.
 	 * 
 	 * @since 1.2.0
 	 */
 	public JavaModelListener(int eventsToListen) {
 		JavaCore.addElementChangedListener(this, eventsToListen);
 	}
 	
 	
 	/**
 	 * Used to initialize the classpath projects list. This should be
 	 * called if the subclass will need to use the {@link #isInClasspath(IJavaProject)} method.
 	 * If it doesn't need to do this, then this call is not needed. Even if it is not called,
	 * but the subclass does call isInClasspath then it will be initialized lazily, but it
	 * may not be acurate the first time because it may not be able to get the build rule.
	 * It is better to call this explicitly.
 	 * <p>
 	 * <b>Note:</b>
 	 * When finished with the model listener, <b>must</b> call {@link #releaseListener()} instead of the old
 	 * deprecated way of just calling to remove this listener from JavaCore. If the old way
 	 * is done, then it will not release all of the listeners because another internal listener
 	 * is added somewhere else.
 	 * <p> 
 	 * <b>Note:</b> This must be called after it is ok to call {@link #getJavaProject()} to return the project
 	 * that is of interest.
	 * 
	 * 
 	 * @since 1.2.1
 	 */
 	public void initializeClasspaths() {
 		buildProjectsInClasspath(false);
 		projectChangeListener = new IResourceChangeListener() {
 		
 			public void resourceChanged(IResourceChangeEvent event) {
 				try {
 					final boolean[] rebuild = new boolean[1]; 
 					event.getDelta().accept(new IResourceDeltaVisitor() {
 					
 						public boolean visit(IResourceDelta delta) throws CoreException {
 							IResource res = delta.getResource();
 							boolean isProject = res.getType() == IResource.PROJECT;
 							if (isProject && projectsInClasspath.containsKey(res.getName())) {
 								if ((delta.getFlags() & IResourceDelta.DESCRIPTION) != 0) {
 									rebuild[0] = true;
 								}
 							}
 							return res.getType() != IResource.PROJECT;	// As soon as we reach project, we don't want to go to the children.
 						}
 					
 					});
 					if (rebuild[0])
 						buildProjectsInClasspath(true);
 				} catch (CoreException e) {
 				}
 			}
 		
 		};
 		
 		// We will be listening for changes to the IProjectDescription (the dynamic references will be the projects that are added/removed,
 		// that is the only way we know that a project has been added/removed). Need to listen to POST_BUILD because that is the only
 		// time notification is sent due to a Container changing and having a project added/removed. However, also
		// listening for POST_CHANGE because there could be some direct changes.
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(projectChangeListener, IResourceChangeEvent.POST_BUILD | IResourceChangeEvent.POST_CHANGE);
 	}
 	
 	private static final IJavaProject[] EMPTY_PROJECTS = new IJavaProject[0];
 	
 	private void buildProjectsInClasspath(boolean notify) {
 		IProject project = getJavaProject().getProject();
 		String topProjectName = project.getName();
 		IWorkspaceRoot wsroot = ResourcesPlugin.getWorkspace().getRoot();
 		Map newMap = new HashMap();
		traverseProjects(project, newMap);
 		
 		if (notify) {
 			// Now notify of the added/removed projects, if any.
 			if (projectsInClasspath.size() > 1) {
 				// There are some old ones other than the top project.
 				String[] oldProjectNames = (String[]) projectsInClasspath.keySet().toArray(new String[projectsInClasspath.size()]);
 				int oldProjectsLeft = oldProjectNames.length;
 				String[] newProjectNames = (String[]) newMap.keySet().toArray(new String[newMap.size()]);
 				int newProjectsAdded = 0;
 				nextDyn: for (int nndx = 0; nndx < newProjectNames.length; nndx++) {
 					String newProjectName = newProjectNames[nndx];
 					for (int ondx = 0; ondx < oldProjectNames.length; ondx++) {
 						if (newProjectName.equals(oldProjectNames[ondx])) {
 							oldProjectNames[ondx] = null;
 							newProjectNames[nndx] = null;
 							oldProjectsLeft--;
 							continue nextDyn;
 						}
 					}
 					newProjectsAdded++;
 				}
 				IJavaProject[] added = EMPTY_PROJECTS;
 				if (newProjectsAdded > 0) {
 					added = new IJavaProject[newProjectsAdded];
 					int addndx = 0;
 					for (int i = 0; i < newProjectNames.length; i++) {
 						if (newProjectNames[i] != null)
 							added[addndx++] = JavaCore.create(wsroot.getProject(newProjectNames[i]));
 					}
 				}
 				IJavaProject[] removed = EMPTY_PROJECTS;
 				if (oldProjectsLeft > 0) {
 					removed = new IJavaProject[oldProjectsLeft];
 					int removedndx = 0;
 					for (int i = 0; i < newProjectNames.length; i++) {
 						String oldProjectName = oldProjectNames[i];
 						if (oldProjectName != null)
 							removed[removedndx++] = JavaCore.create(wsroot.getProject(oldProjectName));
 					}
 				}
 				notifyProjectAddedRemovedFromClasspath(added, removed);
 			} else if (newMap.size() > 1) {
 				// There are no old, other than the top project. So we will notify all as added.
 				IJavaProject[] added = new IJavaProject[newMap.size() - 1]; // Won't include top project. 
 				int addedndx = 0;
 				for (Iterator itr = newMap.keySet().iterator(); itr.hasNext();) {
 					String newProjectName = (String) itr.next();
 					if (!topProjectName.equals(newProjectName))
 						added[addedndx++] = JavaCore.create(wsroot.getProject(newProjectName));
 				}
 				notifyProjectAddedRemovedFromClasspath(added, EMPTY_PROJECTS);
 			}
 		}		
 		synchronized (projectsInClasspath) {
 			projectsInClasspath.clear();
 			projectsInClasspath.putAll(newMap);
 		}
 	}
 	
	private void traverseProjects(IProject project, Map projectsProcessed) {
 		String projectName = project.getName();
 		if (projectsProcessed.containsKey(projectName))
 			return;
 		// Now gather the referenced projects.
 		// Note: we can no longer determine if visible or not since we can't traverse the classpath for these.
 		// Compare to old, if not changed then don't go any further. Assume order change is change. Easiest for now.
 		try {
 			IProject[] dynProjects = project.getDescription().getDynamicReferences();
 			projectsProcessed.put(projectName, dynProjects);
 			
 			// Now walk all of the projects to get their references.
 			for (int i = 0; i < dynProjects.length; i++) {
				traverseProjects(dynProjects[i], projectsProcessed);
 			}
 		} catch (CoreException e) {
 		}
 	}
 	
 	/**
 	 * Called when project(s) were added/removed from the classpath (somewhere, it may be nested down
 	 * in another project from the top project. It doesn't say which project it was removed/added to).
 	 * This is used because currently JDT does not notify
 	 * that a project was added/removed from a classpath. When/if it ever does that, then the normal processJavaElement can be
 	 * used instead. Since this is API, it will continue, but it will be deprecated.
 	 * <p>
 	 * This will be called after all have been gathered for any one notification.
 	 * <p>
 	 * The default implementation does nothing. Subclasses may override to do what they need.
 	 * @param added array of projects added
 	 * @param removed array of projects removed
 	 * 
 	 * @since 1.2.1
 	 */
 	protected void notifyProjectAddedRemovedFromClasspath(IJavaProject[] added, IJavaProject[] removed) {
 		
 	}
 	
 	private IResourceChangeListener projectChangeListener;
 	
 	/**
 	 * Call to release this listener. This is the API way of doing this. The old way of calling JavaCore directly
 	 * to release the listener will no longer work if {@link #initializeClasspaths()} had been called. Even if
 	 * initializeClasspath has not been called, this is still the new approved way of doing this. It should never
 	 * of been the API that users would do the remove listener directly against JavaCore. That exposed too much
 	 * of the internals.
 	 * 
 	 * @since 1.2.1
 	 */
 	public void releaseListener() {
 		JavaCore.removeElementChangedListener(this);
 		if (projectChangeListener != null) {
 			ResourcesPlugin.getWorkspace().removeResourceChangeListener(projectChangeListener);
 			projectChangeListener = null;
 		}
 	}
 	
 	/*
 	 * The projects in classpath (the key will be the project name as a string, and the value will be IProject[] of projects that
 	 * are referenced by this project).
 	 * This must be a synchronzied set so that accessing and updating are synced.
 	 */
 	private Map projectsInClasspath = Collections.synchronizedMap(new HashMap(5));
 				
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jdt.core.IElementChangedListener#elementChanged(org.eclipse.jdt.core.ElementChangedEvent)
 	 */
 	public void elementChanged(ElementChangedEvent event) {
 		processElementChanged(event);
 	}
 	
 	/**
 	 * Process the element changed event.
 	 * <p>
 	 * Subclasses should override this method instead of {@link #elementChanged(ElementChangedEvent)}.
 	 * @param event
 	 * 
 	 * @since 1.2.1
 	 */
 	protected void processElementChanged(ElementChangedEvent event) {
 		processDelta((IJavaElementDelta) event.getSource());
 	}
 	
 	/**
 	 * Generally dispatch the children of the delta. Normally this method should not be overridden.
 	 * 
 	 * @param element
 	 * @param delta
 	 * 
 	 * @since 1.2.0
 	 */
 	protected void processChildren(IJavaElement element, IJavaElementDelta delta) {
 		IJavaElementDelta[] children = delta.getAffectedChildren();
 		for (int i = 0; i < children.length; i++) {
 			processDelta(children[i]);
 		}
 	}
 
 	/**
 	 * Source content has changed. The default is to do nothing. Subclasses may override to perform own functions.
 	 * 
 	 * @param delta
 	 * 
 	 * @since 1.2.0
 	 */
 	protected void processContentChanged(IJavaElementDelta delta) {
 	}
 
 	/**
 	 * Dispatch the java element delta. This method should normally not be overridden. One
 	 * usage would be to add delta types that are to be processed and dispatched by the
 	 * subclasses. For example also dispatch on {@link IJavaElement#IMPORT_CONTAINER}. Subclasses
 	 * should call <code>super.processDelta(IJavaElementDelta)</code> if it is not one they
 	 * are interested in.
 	 * 
 	 * @param delta
 	 * 
 	 * @since 1.2.0
 	 */
 	public void processDelta(IJavaElementDelta delta) {
 		IJavaElement element = delta.getElement();
 
 		switch (element.getElementType()) {
 			case IJavaElement.JAVA_MODEL:
 				processJavaElementChanged((IJavaModel) element, delta);
 				break;
 			case IJavaElement.JAVA_PROJECT:
 				processJavaElementChanged((IJavaProject) element, delta);
 				break;
 			case IJavaElement.PACKAGE_FRAGMENT_ROOT:
 				processJavaElementChanged((IPackageFragmentRoot) element, delta);
 				break;
 			case IJavaElement.PACKAGE_FRAGMENT:
 				processJavaElementChanged((IPackageFragment) element, delta);
 				break;
 			case IJavaElement.COMPILATION_UNIT:
 				processJavaElementChanged((ICompilationUnit) element, delta);
 				processContentChanged(delta);
 				break;
 			case IJavaElement.CLASS_FILE:
 				processJavaElementChanged((IClassFile) element, delta);
 				break;
 			case IJavaElement.TYPE:
 				processJavaElementChanged((IType) element, delta);
 				break;
 			// Note: if we are to update the Method/Field adapters, we should process the
 			// IJavaElement.METHOD and IJavaElement.FIELD
 		}
 	}
 
 	/**
 	 * Process the classfile changed event. The default is to do nothing. It will not walk any children of the delta either. Subclasses may override
 	 * to perform their own functions.
 	 * 
 	 * @param element
 	 * @param delta
 	 * 
 	 * @since 1.2.0
 	 */
 	protected void processJavaElementChanged(IClassFile element, IJavaElementDelta delta) {
 	}
 
 	/**
 	 * Process the compilation unit changed event. The default is to process the children of the delta. Subclasses may override to perform their own
 	 * functions. If they wish to walk the children they should then also call
 	 * <code>super.processJavaElementChanged(ICompilationUnit, IJavaElementDelta)</code>.
 	 * 
 	 * @param element
 	 * @param delta
 	 * 
 	 * @since 1.2.0
 	 */
 	protected void processJavaElementChanged(ICompilationUnit element, IJavaElementDelta delta) {
 		processChildren(element, delta);
 	}
 
 	/**
 	 * Process the java model changed event. The default is to process the children of the delta. Subclasses may override to perform their own
 	 * functions. If they wish to walk the children they should then also call
 	 * <code>super.processJavaElementChanged(IJavaModel, IJavaElementDelta)</code>.
 	 * 
 	 * @param element
 	 * @param delta
 	 * 
 	 * @since 1.2.0
 	 */
 	protected void processJavaElementChanged(IJavaModel element, IJavaElementDelta delta) {
 		processChildren(element, delta);
 	}
 
 	/**
 	 * Process the java project changed event. The default is to process the children of the delta. Subclasses may override to perform their own
 	 * functions. If they wish to walk the children they should then also call
 	 * <code>super.processJavaElementChanged(IJavaProject, IJavaElementDelta)</code>.
 	 * 
 	 * @param element
 	 * @param delta
 	 * 
 	 * @since 1.2.0
 	 */
 	protected void processJavaElementChanged(IJavaProject element, IJavaElementDelta delta) {
 		processChildren(element, delta);
 	}
 
 	/**
 	 * Process the package fragment changed event. The default is to process the children of the delta. Subclasses may override to perform their own
 	 * functions. If they wish to walk the children they should then also call
 	 * <code>super.processJavaElementChanged(IPackageFragment, IJavaElementDelta)</code>.
 	 * 
 	 * @param element
 	 * @param delta
 	 * 
 	 * @since 1.2.0
 	 */
 	protected void processJavaElementChanged(IPackageFragment element, IJavaElementDelta delta) {
 		processChildren(element, delta);
 	}
 
 	/**
 	 * Process the package fragment root changed event. The default is to process the children of the delta. Subclasses may override to perform their
 	 * own functions. If they wish to walk the children they should then also call
 	 * <code>super.processJavaElementChanged(IPackageFragmentRoot, IJavaElementDelta)</code>.
 	 * 
 	 * @param element
 	 * @param delta
 	 * 
 	 * @since 1.2.0
 	 */
 	protected void processJavaElementChanged(IPackageFragmentRoot element, IJavaElementDelta delta) {
 		processChildren(element, delta);
 	}
 
 	/**
 	 * Process the IType changed event. The default is to do nothing. It will not walk any children of the delta either. Subclasses may override to
 	 * perform their own functions.
 	 * 
 	 * @param element
 	 * @param delta
 	 * 
 	 * @since 1.2.0
 	 */
 	protected void processJavaElementChanged(IType element, IJavaElementDelta delta) {
 	}
 
 	/**
 	 * Answers whether this element delta is a classpath change. I.e. something added/removed/moved around for the classpath. This should only be
 	 * called for a delta that is for an {@link IPackageFragmentRoot}. Should be called from an override of
 	 * {@link #processJavaElementChanged(IPackageFragmentRoot, IJavaElementDelta)}
 	 * 
 	 * @param delta
 	 * @return <code>true</code> if it is classpath change or <code>false</code> if not.
 	 * 
 	 * @since 1.2.0
 	 */
 	protected boolean isClassPathChange(IJavaElementDelta delta) {
 		int flags = delta.getFlags();
 		return (delta.getKind() == IJavaElementDelta.CHANGED && ((flags & IJavaElementDelta.F_ADDED_TO_CLASSPATH) != 0)
 				|| ((flags & IJavaElementDelta.F_REMOVED_FROM_CLASSPATH) != 0) || ((flags & IJavaElementDelta.F_REORDER) != 0));
 	}
 
 	/**
 	 * Answer whether the change is this is a raw classpath change, or if a file that is in the root of the project was changed that also can mean a
 	 * raw classpath change. This must only be called for a delta that is for an {@link IJavaProject}.
 	 * <p>
 	 * See {@link IJavaElementDelta.#F_CLASSPATH_CHANGED} and {@link #isAlsoClasspathChange(IPath)} for details.
 	 * 
 	 * @param delta
 	 * @return
 	 * 
 	 * @since 1.2.0
 	 */
 	protected boolean isClasspathResourceChange(IJavaElementDelta delta) {
 		if ((delta.getFlags() & IJavaElementDelta.F_CLASSPATH_CHANGED) != 0)
 			return true;
 		IResourceDelta[] resources = delta.getResourceDeltas();
 		if (resources == null)
 			return false;
 		IPath path = null;
 		for (int i = 0; i < resources.length; i++) {
 			if (resources[i].getKind() == IResourceDelta.CHANGED) {
 				path = resources[i].getProjectRelativePath();
 				if (isAlsoClasspathChange(path))
 					return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Answers whether the file specified by the path is one that when changed can result in a classpath change. This is called from
 	 * {@link #isClasspathResourceChange(IJavaElementDelta)}. The file must be in the root of the project. The default is <code>false</code>.
 	 * Subclasses may override.
 	 * 
 	 * @param path
 	 * @return <code>true</code> if this file being changed would result in a classpath change, <code>false</code> if not.
 	 * 
 	 * @since 1.2.0
 	 */
 	protected boolean isAlsoClasspathChange(IPath path) {
 		return false;
 	}
 
 	/**
 	 * Answers whether the given java project is in the classpath (including recursive). The java project is determined by subclasses, see
 	 * {@link #getJavaProject()}. The list is cached and rebuild whenever the classpath has changed.
 	 * 
 	 * @param javaProject
 	 * @return <code>true</code> if project is in classpath or <code>false</code> if not.
 	 * 
 	 * @since 1.2.0
 	 */
 	protected boolean isInClasspath(IJavaProject javaProject) {
 		// If there is no listener, then we need to do it the old way, ricking deadlock.
 		if (projectChangeListener == null) {
 			IJavaProject listenerJavaProject = getJavaProject();
 			if (javaProject.equals(listenerJavaProject))
 				return true;
 			return isInClasspath(javaProject, listenerJavaProject, true, new HashSet());
 		} else
 			return projectsInClasspath.containsKey(javaProject.getElementName());
 	}
 
 	/**
 	 * The java project for this listener. Subclasses must provide a java project.
 	 * 
 	 * @return the java project. <code>null</code> is not valid.
 	 * 
 	 * @since 1.2.0
 	 */
 	protected abstract IJavaProject getJavaProject();
 
 	/*
 	 * test to see if the testProject is in the classpath (including from any referenced projects) of the target project. Keep track of those already
 	 * visited so as not to visit again. Too late to make private. But it should not be overridden.
 	 */
 	/**
 	 * @deprecated There is no replacement. Should not be used in listener because it can cause deadlocks. Use {@link #isInClasspath(IJavaProject)} only.
 	 */
 	protected boolean isInClasspath(IJavaProject testProject, IJavaProject targetProject, boolean isFirstLevel, Set visited) {
 		if (visited.contains(targetProject))
 			return false;
 		visited.add(targetProject);
 		IClasspathEntry[] entries = null;
 		try {
 			entries = targetProject.getRawClasspath();
 		} catch (JavaModelException e) {
 			return false;
 		}
 		List projects = null;
 		for (int i = 0; i < entries.length; i++) {
 			IClasspathEntry entry;
 			entry = entries[i];
 			switch (entry.getEntryKind()) {
 				case IClasspathEntry.CPE_PROJECT:
 					IJavaProject entryProject = getVisibleJavaProject(entry, isFirstLevel);
 					if (entryProject != null) {
 						if (entryProject.equals(testProject)) {
 							return true;
 						} else {
 							if (projects == null) {
 								projects = new ArrayList();
 							}
 							projects.add(entryProject);
 						}
 					}
 					break;
 				//A container may contain references to projects.
 				case IClasspathEntry.CPE_CONTAINER :
 					IClasspathContainer container = null;
 					try {
 						container = JavaCore.getClasspathContainer(entry.getPath(), targetProject);
 					} catch (JavaModelException e) {
 						JEMPlugin.getPlugin().getLogger().logError(e);
 					}
 					if (container == null || container.getKind() != IClasspathContainer.K_APPLICATION)
 						break;
 					IClasspathEntry[] containerEntries = container.getClasspathEntries();
 					for (int j = 0; j < containerEntries.length; j++) {
 						if (containerEntries[j].getEntryKind() == IClasspathEntry.CPE_PROJECT) {
 							IJavaProject conEntryProject = getVisibleJavaProject(containerEntries[j], isFirstLevel);
 							if (conEntryProject != null) {
 								if (conEntryProject.equals(testProject)) {
 									return true;
 								} else {
 									if (projects == null) {
 										projects = new ArrayList();
 									}
 									projects.add(conEntryProject);
 								}
 							}
 						}
 					}
 					break;
 			}
 		}
 		return isInClasspath(testProject, projects, false, visited);
 	}
 	
 	/*
 	 * This method is used to return an IJavaProject that is resolved from the entry
 	 * if it is currently visible to downstream projects.
 	 */
 	private IJavaProject getVisibleJavaProject(IClasspathEntry entry, boolean isFirstLevel) {
 		if (isFirstLevel || entry.isExported()) {
 			IClasspathEntry resEntry = JavaCore.getResolvedClasspathEntry(entry);
 			return getJavaProject(resEntry);
 		}
 		return null;
 	}
 
 	/*
 	 * See if the testProject is in the classpath of any of the list of projects or in any project that an entry in the list may of visited. Too late
 	 * to make private. But it should not be overridden.
 	 */
 	/**
 	 * @deprecated There is no replacement. Should not be used in listener because it can cause deadlocks. Use {@link #isInClasspath(IJavaProject)} only.
 	 */
 	protected boolean isInClasspath(IJavaProject testProject, List someJavaProjects, boolean isFirstLevel, Set visited) {
 		if (someJavaProjects == null)
 			return false;
 		int size = someJavaProjects.size();
 		IJavaProject javaProj = null;
 		for (int i = 0; i < size; i++) {
 			javaProj = (IJavaProject) someJavaProjects.get(i);
 			return isInClasspath(testProject, javaProj, isFirstLevel, visited);
 		}
 		return false;
 	}
 
 	/**
 	 * Get the java project that classpath entry is for. This should only be called on classpath entries of type {@link IClasspathEntry#CPE_PROJECT}
 	 * 
 	 * @param entry
 	 *            classpath entry of type {@link IClasspathEntry#CPE_PROJECT}.
 	 * @return the java project for this entry. The project might not actually exist. That is not verified.
 	 * 
 	 * @since 1.2.0
 	 */
 	protected IJavaProject getJavaProject(IClasspathEntry entry) {
 		IProject proj = getWorkspaceRoot().getProject(entry.getPath().segment(0));
 		if (proj != null)
 			return JavaCore.create(proj);
 		return null;
 	}
 
 	/**
 	 * Get the workspace root. A utility method.
 	 * @return the workspace root.
 	 * 
 	 * @since 1.2.0
 	 */
 	protected IWorkspaceRoot getWorkspaceRoot() {
 		return ResourcesPlugin.getWorkspace().getRoot();
 	}
 }
