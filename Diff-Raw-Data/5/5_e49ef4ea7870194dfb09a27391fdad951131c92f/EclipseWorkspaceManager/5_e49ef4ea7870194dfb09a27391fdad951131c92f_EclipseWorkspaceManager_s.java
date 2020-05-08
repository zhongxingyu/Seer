 /**************************************************************************
  * Copyright (c) 2012 Anya Helene Bagge
  * Copyright (c) 2012 University of Bergen
  * 
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version. See http://www.gnu.org/licenses/
  * 
  * 
  * See the file COPYRIGHT for more information.
  * 
  * Contributors:
  * * Anya Helene Bagge
  * 
  *************************************************************************/
 package org.nuthatchery.pica.resources.eclipse;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.IdentityHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.IJobManager;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.annotation.Nullable;
 import org.nuthatchery.pica.Pica;
 import org.nuthatchery.pica.eclipse.EclipsePicaInfra;
 import org.nuthatchery.pica.eclipse.PicaActivator;
 import org.nuthatchery.pica.errors.ProjectNotFoundError;
 import org.nuthatchery.pica.resources.IManagedResource;
 import org.nuthatchery.pica.resources.IManagedResourceListener;
 import org.nuthatchery.pica.resources.IProjectManager;
 import org.nuthatchery.pica.resources.IWorkspaceConfig;
 import org.nuthatchery.pica.resources.IWorkspaceManager;
 import org.nuthatchery.pica.util.Pair;
 
 public final class EclipseWorkspaceManager implements IResourceChangeListener, IWorkspaceManager {
 	private static IdentityHashMap<IWorkspaceConfig, EclipseWorkspaceManager> instances = new IdentityHashMap<>();
 
 	private final Map<String, EclipseProjectManager> projects = new HashMap<String, EclipseProjectManager>();
 
 	private final List<IProject> closingProjects = new ArrayList<IProject>();
 
 	private final static boolean debug = false;
 
 	private static final Object JOB_FAMILY_WORKSPACE_MANAGER = new Object();
 
 	private final Map<String, List<Change>> changeQueue = new HashMap<String, List<Change>>();
 
 	private final IWorkspaceConfig config;
 
 	private final ThreadPoolExecutor execService;
 
 	private ThreadingPreference threadingPref = ThreadingPreference.NORMAL;
 
 
 	private EclipseWorkspaceManager(IWorkspaceConfig config) {
 		this.config = config;
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
 
 		execService = new ThreadPoolExecutor(0, 1, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
 		setThreadingPreference(threadingPref);
 		initialize();
 	}
 
 
 	public void closeProject(IProject project) {
 		// TODO: check nature
 		IProjectManager manager = projects.remove(project.getName());
 		if(manager != null) {
 			manager.dispose();
 		}
 	}
 
 
 	public void dataInvariant() {
 	}
 
 
 	@Override
 	public void dispose() {
 		// do nothing
 	}
 
 
 	public void dumpThreadingStats() {
 		System.err.printf("Threading: core pool size: %d, max pool size: %d, largest ever: %d%n", execService.getCorePoolSize(), execService.getMaximumPoolSize(), execService.getLargestPoolSize());
 		System.err.printf("           %d/%d active threads, %d/%d tasks completed, %d in queue%n", execService.getActiveCount(), execService.getPoolSize(), execService.getCompletedTaskCount(), execService.getTaskCount(), execService.getQueue().size());
 	}
 
 
 	@Nullable
 	public synchronized IManagedResource findResource(IResource resource) {
 		EclipseProjectManager projectManager = projects.get(resource.getProject().getName());
 		IManagedResource res = null;
 		if(projectManager != null) {
 			res = projectManager.findResource(resource);
 			if(res != null) {
 				return res;
 			}
 			if(resource.exists()) {
 				try {
 					System.err.println("EclipseWorkspaceManager: adding missing resource " + resource.getFullPath());
 					resourceAdded(resource);
 					return projectManager.findResource(resource);
 				}
 				catch(CoreException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 		return null;
 	}
 
 
 	@Nullable
 	public synchronized IProjectManager getManager(IProject project) {
 		return projects.get(project.getName());
 	}
 
 
 	@Override
 	public synchronized IProjectManager getManager(String project) {
 		EclipseProjectManager manager = projects.get(project);
 		if(manager == null)
 			throw new ProjectNotFoundError(project);
 		else
 			return manager;
 	}
 
 
 	public Pair<Set<IManagedResource>, Set<IPath>> getResourcesForDelta(IResourceDelta delta) {
 		final Set<IManagedResource> changed = new HashSet<IManagedResource>();
 		final Set<IPath> removed = new HashSet<IPath>();
 
 		try {
 			delta.accept(new IResourceDeltaVisitor() {
 				@Override
 				public boolean visit(@Nullable IResourceDelta delta) throws CoreException {
 					if(delta != null && delta.getResource() instanceof IFile) {
 						switch(delta.getKind()) {
 						case IResourceDelta.ADDED:
 						case IResourceDelta.CHANGED: {
 							IManagedResource resource = findResource(delta.getResource());
 							if(resource != null) {
 								changed.add(resource);
 							}
 							break;
 						}
 						case IResourceDelta.REMOVED:
 							removed.add(delta.getResource().getFullPath());
 							break;
 						default:
 							throw new UnsupportedOperationException("Resource change on " + delta.getFullPath() + ": " + delta.getKind());
 						}
 					}
 					return true;
 				}
 			});
 		}
 		catch(CoreException e) {
 			e.printStackTrace();
 		}
 
 		return new Pair<Set<IManagedResource>, Set<IPath>>(changed, removed);
 	}
 
 
 	@Override
 	public ThreadingPreference getThreadingPreference() {
 		return threadingPref;
 	}
 
 
 	@Override
 	public ExecutorService getThreadPool() {
 		return execService;
 	}
 
 
 	@Override
 	public URI getURI(String path) throws URISyntaxException {
 		IPath p = new Path(path);
 		String project = p.segment(0);
 		p = p.removeFirstSegments(1);
 		assert p != null;
 		return Pica.get().constructProjectURI(project, p);
 	}
 
 
 	@Override
 	public boolean hasURI(URI uri) {
 		if(uri.getScheme().equals("project")) {
 			return true;
 		}
 
 		IFile file = Pica.get().getFileHandle(uri);
 		return file != null;
 	}
 
 
 	public void openProject(IProject project) throws CoreException {
 		for(String nature : config.getActiveNatures()) {
 			if(project.hasNature(nature) && !projects.containsKey(project.getName())) {
 				projects.put(project.getName(), new EclipseProjectManager(this, config, project));
 				break;
 			}
 		}
 	}
 
 
 	@Override
 	public synchronized void resourceChanged(@Nullable IResourceChangeEvent event) {
 		if(event == null)
 			throw new IllegalArgumentException();
 		if(event.getType() == IResourceChangeEvent.POST_CHANGE) {
 			IResourceDelta delta = event.getDelta();
 			// System.err.println("PROCESSING RESOURCE CHANGE EVENT");
 			try {
 				delta.accept(new IResourceDeltaVisitor() {
 					@Override
 					public boolean visit(@Nullable IResourceDelta delta) throws CoreException {
 						if(delta != null) {
 							try {
 								switch(delta.getKind()) {
 								case IResourceDelta.ADDED:
 									if(debug) {
 										System.out.println(delta.getFullPath().toString() + " ADDED");
 									}
 									resourceAdded(delta.getResource());
 									break;
 								case IResourceDelta.CHANGED:
 									if(delta.getFlags() == IResourceDelta.MARKERS || delta.getFlags() == IResourceDelta.NO_CHANGE) {
 										if(debug) {
 											System.out.println(delta.getFullPath().toString() + " NO CHANGE");
 										}
 									}
 									else {
 										if(debug) {
 											System.out.println(delta.getFullPath().toString() + " CHANGED");
 										}
 										// only if its not just the markers
 										resourceChanged(delta);
 									}
 									break;
 								case IResourceDelta.REMOVED:
 									if(debug) {
 										System.out.println(delta.getFullPath().toString() + " REMOVED");
 									}
 									resourceRemoved(delta.getResource());
 									break;
 								default:
 									throw new UnsupportedOperationException("Resource change on " + delta.getFullPath() + ": " + delta.getKind());
 								}
 							}
 							catch(CoreException e) {
 								Pica.get().logException("CoreException while processing " + delta.getFullPath(), e);
 								e.printStackTrace();
 							}
 							catch(Throwable t) {
 								Pica.get().logException("INTERNAL ERROR IN RESOURCE MANAGER (for " + delta.getFullPath() + ")", t);
 								t.printStackTrace();
 							}
 						}
 
 						return true;
 					}
 				});
 			}
 			catch(CoreException e) {
 				e.printStackTrace();
 			}
 
 			for(IProject p : closingProjects) {
 				try {
 					if(p != null) {
 						closeProject(p);
 					}
 				}
 				catch(Throwable t) {
 					Pica.get().logException("INTERNAL ERROR IN RESOURCE MANAGER (for " + p + ")", t);
 					t.printStackTrace();
 				}
 			}
 			closingProjects.clear();
 			processChanges();
 			// System.err.println("FINISHED PROCESSING RESOURCE CHANGE EVENT");
 		}
 		dataInvariant();
 	}
 
 
 	@Override
 	public void setThreadingPreference(ThreadingPreference pref) {
 		threadingPref = pref;
 		int numThreads = pref.getNumThreads();
 		execService.setCorePoolSize(Math.max(0, numThreads / 2));
 		execService.setMaximumPoolSize(Math.max(1, numThreads));
 	}
 
 
 	@Override
 	public synchronized void stop() {
 		IJobManager jobManager = Job.getJobManager();
 		jobManager.cancel(JOB_FAMILY_WORKSPACE_MANAGER);
 		for(EclipseProjectManager mgr : projects.values()) {
 			mgr.stop();
 		}
 	}
 
 
 	private void addChange(String proj, URI uri, IResource resource, Change.Kind kind) {
 		List<Change> list = changeQueue.get(proj);
 		if(list == null) {
 			list = new ArrayList<Change>();
 			changeQueue.put(proj, list);
 		}
 		list.add(new Change(uri, resource, kind));
 	}
 
 
 	private void initialize() {
 		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
 		IProject[] projects = root.getProjects(0);
 		for(IProject proj : projects) {
 			if(proj.isOpen()) {
 				try {
 					openProject(proj);
 				}
 				catch(CoreException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 
 	}
 
 
 	private void processChanges() {
 		for(String proj : changeQueue.keySet()) {
 			List<Change> list = changeQueue.get(proj);
 			EclipseProjectManager manager = projects.get(proj);
 			if(manager != null) {
 				manager.queueChanges(list);
 			}
 			list.clear();
 		}
 	}
 
 
 	private void resourceAdded(IResource resource) throws CoreException {
 		if(resource.getType() == IResource.PROJECT) {
 			if(((IProject) resource).isOpen()) {
 				openProject((IProject) resource);
 			}
 		}
 		else {
 			IProject project = resource.getProject();
 			URI uri = EclipsePicaInfra.constructProjectURI(project, resource.getProjectRelativePath());
 			addChange(project.getName(), uri, resource, Change.Kind.ADDED);
 		}
 	}
 
 
 	private void resourceChanged(IResourceDelta delta) throws CoreException {
 		IPath path = delta.getFullPath();
 		int flags = delta.getFlags();
 
 		if((flags & (IResourceDelta.TYPE | IResourceDelta.REPLACED)) != 0) {
 			resourceRemoved(delta.getResource());
 			resourceAdded(delta.getResource());
 		}
 
 		if((flags & IResourceDelta.OPEN) != 0) {
 			IProject proj = (IProject) delta.getResource();
 			if(proj.isOpen()) {
 				openProject(proj);
 			}
 			else {
 				closingProjects.add(proj);
 			}
 
 		}
 
 		if((flags & IResourceDelta.LOCAL_CHANGED) != 0) {
 			System.err.println("LOCAL_CHANGED: " + path);
 		}
 		if((flags & IResourceDelta.CONTENT) != 0 || (flags & IResourceDelta.ENCODING) != 0) {
 			if(debug) {
 				System.err.println("RESOURCE CHANGED: " + path);
 			}
 			IResource resource = delta.getResource();
 			IProject project = resource.getProject();
 			URI uri = EclipsePicaInfra.constructProjectURI(project, resource.getProjectRelativePath());
 			addChange(project.getName(), uri, resource, Change.Kind.CHANGED);
 
 		}
 
 	}
 
 
 	private void resourceRemoved(IResource resource) {
 		if(resource.getType() == IResource.PROJECT) {
 			closeProject((IProject) resource);
 		}
 		else {
 			IProject project = resource.getProject();
 			URI uri = EclipsePicaInfra.constructProjectURI(project, resource.getProjectRelativePath());
 			addChange(project.getName(), uri, resource, Change.Kind.REMOVED);
 		}
 	}
 
 
 	@Nullable
 	public static IFile getEclipseFile(IManagedResource res) {
 		if(res instanceof ManagedEclipseResource) {
 			ManagedEclipseResource r = (ManagedEclipseResource) res;
 			if(r.getEclipseResource() instanceof IFile)
 				return (IFile) r.getEclipseResource();
 			else
 				return null;
 		}
 		else {
 			return null;
 		}
 	}
 
 
 	@Nullable
 	public static IResource getEclipseResource(IManagedResource res) {
 		if(res instanceof ManagedEclipseResource) {
 			return ((ManagedEclipseResource) res).getEclipseResource();
 		}
 		else {
 			return null;
 		}
 	}
 
 
 	public static synchronized EclipseWorkspaceManager getInstance(IWorkspaceConfig config) {
 		EclipseWorkspaceManager instance = instances.get(config);
 		if(instance == null) {
 			instance = new EclipseWorkspaceManager(config);
 			instances.put(config, instance);
 		}
 		return instance;
 	}
 
 
 	@Nullable
 	public static IPath getPath(URI uri) {
 		if(uri.getScheme().equals("project")) {
 			return new Path("/" + uri.getAuthority() + "/" + uri.getPath());
 		}
 		else {
 			return null;
 		}
 	}
 
 
 	/**
 	 * Ensure that a directory and all its ancestors exist.
 	 * 
 	 * @param path
 	 *            A workspace-relative directory path
 	 * @param updateFlags
 	 *            Flags, e.g., IResource.DERIVED and/or IResource.HIDDEN
 	 * @return The container/folder identified by path
 	 * @throws CoreException
 	 */
 	public static IContainer mkdir(IPath path, int updateFlags) throws CoreException {
 		IContainer parent = ResourcesPlugin.getWorkspace().getRoot();
 		for(String s : path.segments()) {
 			IResource member = parent.findMember(s, true);
 			if(member == null) {
 				parent = parent.getFolder(new Path(s));
 				try {
 					((IFolder) parent).create(updateFlags, true, null);
 				}
 				catch(CoreException e) {
 					member = parent.findMember(s, true);
					if(member.exists() && member instanceof IFolder)
 						continue;
					e.printStackTrace();
 				}
 			}
 			else if(member instanceof IContainer) {
 				parent = (IContainer) member;
 				if(!parent.exists()) {
 					((IFolder) parent).create(updateFlags, true, null);
 				}
 			}
 			else {
 				throw new CoreException(new Status(IStatus.ERROR, PicaActivator.PLUGIN_ID, "Path already exists, and is not a folder: " + member.getFullPath()));
 			}
 		}
 		return parent;
 
 	}
 
 
 	static class ChangeQueueRunner extends Job {
 		private final List<Change> changeQueue;
 		private final List<IManagedResourceListener> listeners;
 
 
 		public ChangeQueueRunner(List<Change> changeQueue, List<IManagedResourceListener> listeners) {
 			super("Do change notifications");
 			this.changeQueue = changeQueue;
 			this.listeners = listeners;
 			setSystem(true);
 		}
 
 
 		@Override
 		public boolean belongsTo(@Nullable Object obj) {
 			return obj == JOB_FAMILY_WORKSPACE_MANAGER;
 		}
 
 
 		@Override
 		protected IStatus run(@Nullable IProgressMonitor monitor) {
 			assert monitor != null;
 			for(Change c : changeQueue) {
 				if(monitor.isCanceled()) {
 					return Status.CANCEL_STATUS;
 				}
 				switch(c.kind) {
 				case ADDED:
 					for(IManagedResourceListener l : listeners) {
 						l.resourceAdded(c.getURI());
 					}
 					break;
 				case CHANGED:
 					for(IManagedResourceListener l : listeners) {
 						l.resourceChanged(c.getURI());
 					}
 					break;
 				case REMOVED:
 					for(IManagedResourceListener l : listeners) {
 						l.resourceRemoved(c.getURI());
 					}
 					break;
 				}
 			}
 			return Status.OK_STATUS;
 		}
 	}
 }
