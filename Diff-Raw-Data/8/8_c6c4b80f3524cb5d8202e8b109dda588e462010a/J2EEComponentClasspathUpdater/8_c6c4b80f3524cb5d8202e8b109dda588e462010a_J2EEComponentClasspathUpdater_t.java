 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.common.classpath;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.ISafeRunnable;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.ListenerList;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SafeRunner;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.common.jdt.internal.classpath.FlexibleProjectContainer;
 import org.eclipse.jst.j2ee.application.internal.operations.IModuleExtensions;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.componentcore.util.EARVirtualComponent;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.componentcore.EnterpriseBinaryComponentHelper;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.impl.ResourceTreeRootAdapter;
 import org.eclipse.wst.common.componentcore.internal.impl.WTPModulesResourceFactory;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.internal.emf.utilities.ExtendedEcoreUtil;
 import org.eclipse.wst.common.project.facet.core.FacetedProjectFramework;
 
 public class J2EEComponentClasspathUpdater implements IResourceChangeListener, IResourceDeltaVisitor {
 
 	private static J2EEComponentClasspathUpdater instance = null;
 
 	private int pauseCount = 0;
 	
 	private IPath WEB_APP_LIBS_PATH = new Path("org.eclipse.jst.j2ee.internal.web.container");
 
 	public static J2EEComponentClasspathUpdater getInstance() {
 		if (instance == null) {
 			init();
 		}
 		return instance;
 	}
 
 	private static void init() {
 		if (instance == null) {
 			instance = new J2EEComponentClasspathUpdater();
 		}
 	}
 
 	/**
 	 * Pauses updates; any caller of this method must ensure through a
 	 * try/finally block that resumeUpdates is subsequently called.
 	 */
 	public void pauseUpdates() {
 		synchronized (this) {
 			pauseCount++;
 		}
 	}
 
 	public void resumeUpdates() {
 		resumeUpdates(true);
 	}
 	
 	private void resumeUpdates(boolean scheduleJob){
 		synchronized (this) {
 			if (pauseCount > 0) {
 				pauseCount--;
 			}
 			if (pauseCount > 0) {
 				return;
 			}
 		}
 		if(scheduleJob){
 			moduleUpdateJob.schedule(MODULE_UPDATE_DELAY);
 		}
 	}
 	
 	public void forceUpdate(Collection projects){
 		forceUpdate(projects, true);
 	}
 	
 	/**
 	 * Collection of type IProject
 	 * @param projects
 	 */
 	public void forceUpdate(Collection projects, boolean runAsJob) {
 		try {
 			pauseUpdates();
 			Iterator iterator = projects.iterator();
 			while(iterator.hasNext()){
 				queueUpdate((IProject)iterator.next());
 			}
 		} finally {
 			forceUpdateOnNextRun = true;
 			// the following code is in place of the normal call to
 			// resume updates.  This restores the pauseCount and forces 
 			// the job to be scheduled immediately 
 			synchronized (this) {
 				if (pauseCount > 0) {
 					pauseCount--;
 				}
 			}
 			if(runAsJob){
 				moduleUpdateJob.schedule(0);
 			} else {
 				moduleUpdateJob.run(new NullProgressMonitor());
 			}
 		}
 	}
 	
 	private boolean forceUpdateOnNextRun = false;
 	
 	public void queueUpdate(IProject project) {
 		if (J2EEProjectUtilities.isEARProject(project)) {
 			queueUpdateEAR(project);
 		} else if (J2EEProjectUtilities.isApplicationClientProject(project) || J2EEProjectUtilities.isEJBProject(project) || J2EEProjectUtilities.isDynamicWebProject(project)
 				|| J2EEProjectUtilities.isJCAProject(project) || J2EEProjectUtilities.isUtilityProject(project)) {
 			queueUpdateModule(project);
 		}
 	}
 
 	public void queueUpdateModule(IProject project) {
 		moduleUpdateJob.queueModule(project);
 		if(!isKnown(project)){
 			IProject[] earProjects = J2EEProjectUtilities.getReferencingEARProjects(project);
 			for (int i = 0; i < earProjects.length; i++) {
 				moduleUpdateJob.queueEAR(earProjects[i]);
 			}
 		} 
 		synchronized (this) {
 			if (pauseCount > 0) {
 				return;
 			}
 		}
 		moduleUpdateJob.schedule(MODULE_UPDATE_DELAY);
 	}
 
 	public void queueUpdateEAR(IProject earProject) {
 		moduleUpdateJob.queueEAR(earProject);
 		synchronized (this) {
 			if (pauseCount > 0) {
 				return;
 			}
 		}
 		moduleUpdateJob.schedule(MODULE_UPDATE_DELAY);
 	}
 	
 	public boolean projectsQueued() {
 		return moduleUpdateJob.projectsQueued() || moduleUpdateJob.getState() != Job.NONE;
 	}
 	
 	private static final int MODULE_UPDATE_DELAY = 30;
 	public static final String MODULE_UPDATE_JOB_NAME = "EAR Libraries Update Job"; 
 
 	private final ModuleUpdateJob moduleUpdateJob = new ModuleUpdateJob();
 
 	public class ModuleUpdateJob extends Job {
 
 		public boolean belongsTo(Object family) {
 			if(family == MODULE_UPDATE_JOB_NAME){
 				return true;
 			}
 			return super.belongsTo(family);
 		}
 		
 		// We use the listener list as a thread safe queue.
 		private class Queue extends ListenerList  {
 			public synchronized Object[] getListeners() {
 				Object[] data = super.getListeners();
 				clear();
 				return data;
 			}
 		};
 
 		private Queue moduleQueue = new Queue();
 
 		private Queue earQueue = new Queue();
 
 		public ModuleUpdateJob() {
 			super(MODULE_UPDATE_JOB_NAME); 
 			setRule(ResourcesPlugin.getWorkspace().getRoot());
 			setSystem(true);
 		}
 
 		public void queueEAR(IProject ear) {
 			earQueue.add(ear);
 		}
 
 		public void queueModule(IProject project) {
 			moduleQueue.add(project);
 		}
 		
 		public boolean projectsQueued() {
 			return !earQueue.isEmpty() || !moduleQueue.isEmpty();
 		}
 
 		private void processEars() {
 			Object[] earProjects = earQueue.getListeners();
 			for (int i = 0; i < earProjects.length; i++) {
 				IProject earProject = (IProject) earProjects[i];
 				IVirtualComponent earComponent = ComponentCore.createComponent(earProject); 
 				IVirtualReference[] refs = J2EEProjectUtilities.getComponentReferences(earComponent);
 				IVirtualComponent comp = null;
 				for (int j = 0; j < refs.length; j++) {
 					comp = refs[j].getReferencedComponent();
 					if (!comp.isBinary()) {
 						queueModule(comp.getProject());
 					}
 				}
 				if(null != earComponent){
 					EnterpriseBinaryComponentHelper.ArchiveCache.getInstance().clearDisconnectedArchivesInEAR(earComponent);	
 				}
 			}
 		}
 		
 		private void processModules(){
 			Object[] projects = moduleQueue.getListeners();
 			for (int i = 0; i < projects.length; i++) {
 				IProject project = (IProject) projects[i];
 				IClasspathContainer container = getWebAppLibrariesContainer(project, false);
 				if (container != null && container instanceof FlexibleProjectContainer) {
 					((FlexibleProjectContainer) container).refresh();
 				}
 				IProject[] earProjects = J2EEProjectUtilities.getReferencingEARProjects(project);
 				if (earProjects.length == 0) {
 					removeContainerFromModuleIfNecessary(project);
 					return;
 				} 
 				
 				container = addContainerToModuleIfNecessary(project);
 				if (container != null && container instanceof J2EEComponentClasspathContainer) {
 					((J2EEComponentClasspathContainer) container).refresh(forceUpdateOnNextRun);
 				}
 			}
 		}
 		
 
 		protected IStatus run(IProgressMonitor monitor) {
 
 			SafeRunner.run(new ISafeRunnable() {
 				public void handleException(Throwable e) {
 					J2EEPlugin.getDefault().getLogger().logError(e);
 				}
 
 				public void run() throws Exception {
 					try {
 						processEars();
 						processModules();
 					} finally {
 						forceUpdateOnNextRun = false;
 					}
 					
 				}
 			});
 
 			return Status.OK_STATUS;
 		}
 	};
 
 	public IClasspathContainer getWebAppLibrariesContainer(IProject webProject, boolean create) {
 		IJavaProject jproj = JavaCore.create(webProject);
 		IClasspathContainer container = null;
 		IClasspathEntry entry = create ? null : getExistingContainer(jproj, WEB_APP_LIBS_PATH);
 		if (entry != null || create) {
 		try {
 				container = JavaCore.getClasspathContainer(WEB_APP_LIBS_PATH, jproj);
 		} catch (JavaModelException e) {
 				J2EEPlugin.getDefault().getLogger().logError(e);
 		}
 	}
 		return container;
 	}
 	
 	private IClasspathContainer addContainerToModuleIfNecessary(IProject moduleProject) {
 		IJavaProject jproj = JavaCore.create(moduleProject);
 		IClasspathEntry entry = getExistingContainer(jproj, J2EEComponentClasspathContainer.CONTAINER_PATH);
 		if (entry == null) {
 			try {
 				entry = JavaCore.newContainerEntry(J2EEComponentClasspathContainer.CONTAINER_PATH, true);
 				addToClasspath(jproj, entry);
 			} catch (CoreException e) {
 				J2EEPlugin.getDefault().getLogger().logError(e);
 			}
 		}
 		IClasspathContainer container = null;
 		try {
 			container = JavaCore.getClasspathContainer(J2EEComponentClasspathContainer.CONTAINER_PATH, jproj);
 		} catch (JavaModelException e) {
 			J2EEPlugin.getDefault().getLogger().logError(e);
 		}
 		return container;
 	}
 
 	private void removeContainerFromModuleIfNecessary(IProject moduleProject) {
 		IJavaProject jproj = JavaCore.create(moduleProject);
 		IClasspathEntry entry = getExistingContainer(jproj, J2EEComponentClasspathContainer.CONTAINER_PATH);
 		if (entry != null) {
 			try {
 				removeFromClasspath(jproj, entry);
 			} catch (CoreException e) {
 				J2EEPlugin.getDefault().getLogger().logError(e);
 			}
 		}
 	}
 
 	private void addToClasspath(final IJavaProject jproj, final IClasspathEntry entry) throws CoreException {
 		final IClasspathEntry[] current = jproj.getRawClasspath();
 		final IClasspathEntry[] updated = new IClasspathEntry[current.length + 1];
 		System.arraycopy(current, 0, updated, 0, current.length);
 		updated[current.length] = entry;
 		jproj.setRawClasspath(updated, null);
 	}
 
 	private void removeFromClasspath(final IJavaProject jproj, final IClasspathEntry entry) throws CoreException {
 		final IClasspathEntry[] current = jproj.getRawClasspath();
 		final IClasspathEntry[] updated = new IClasspathEntry[current.length - 1];
 		boolean removed = false;
 		for (int i = 0; i < current.length; i++) {
 			if (!removed) {
 				if (current[i] == entry) {
 					removed = true;
 				} else {
 					updated[i] = current[i];
 				}
 			} else {
 				updated[i - 1] = current[i];
 			}
 		}
 		jproj.setRawClasspath(updated, null);
 	}
 
 	/**
 	 * Returns the existing classpath container if it is already on the
 	 * classpath. This will not create a new container.
 	 * 
 	 * @param jproj
 	 * @param classpathContainerID
 	 * @return
 	 */
 	public IClasspathEntry getExistingContainer(IJavaProject jproj, IPath classpathContainerPath) {
 		try {
 			IClasspathEntry[] cpes;
 			cpes = jproj.getRawClasspath();
 			for (int j = 0; j < cpes.length; j++) {
 				final IClasspathEntry cpe = cpes[j];
 				if (cpe.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
 					if (cpe.getPath().equals(classpathContainerPath)) {
 						return cpe; // entry found
 					}
 				}
 			}
 		} catch (JavaModelException e) {
 			J2EEPlugin.getDefault().getLogger().logError(e);
 		}
 		// entry not found
 		return null;
 	}
 
 	private Set knownProjects = new HashSet();
 	
 	private boolean isKnown(IProject project){
 		return !knownProjects.add(project.getName());
 	}
 	
 	private void forgetProject(IProject project){
 		knownProjects.remove(project.getName());
 	}
 	
 	public void resourceChanged(IResourceChangeEvent event) {
 		boolean scheduleJob = false;
 		try {
 			pauseUpdates();
 			switch (event.getType()){
 				case IResourceChangeEvent.PRE_CLOSE:
 				case IResourceChangeEvent.PRE_DELETE:
 					IResource resource = event.getResource();
 					if(resource.getType() == IResource.PROJECT){
 						if(ModuleCoreNature.isFlexibleProject((IProject) resource)){
 							if(J2EEProjectUtilities.isEARProject((IProject)resource)){
 								IProject earProject = (IProject) resource;
 								EARArtifactEdit edit = null;
 								try {
 									edit = EARArtifactEdit.getEARArtifactEditForRead(earProject);
 									if(edit != null){
 										IVirtualReference[] refs = edit.getComponentReferences();
 										IVirtualComponent comp = null;
 										for (int j = 0; j < refs.length; j++) {
 											comp = refs[j].getReferencedComponent();
 											if (!comp.isBinary()) {
 												queueUpdateModule(comp.getProject());
 											}
 										}
 									}
 								} finally {
 									if (edit != null) {
 										edit.dispose();
 									}
 								}
 							} else {
 								IProject[] earProjects = J2EEProjectUtilities.getReferencingEARProjects((IProject)resource);
 								for(int i=0; i<earProjects.length; i++){
 									queueUpdateEAR(earProjects[i]);
 								}
 							}
 							forgetProject((IProject)resource);
 						}
 						EnterpriseBinaryComponentHelper.ArchiveCache.getInstance().clearAllArchivesInProject((IProject)resource);
 					}
 					break;
 				case IResourceChangeEvent.POST_CHANGE:
 					scheduleJob = true;
 					event.getDelta().accept(this);
 					IResourceDelta[] d = event.getDelta().getAffectedChildren();
 					findNode(d);
 			
 					break;
 			}
 		} catch (CoreException e) {
 			J2EEPlugin.getDefault().getLogger().logError(e);
 		} 
 		finally {
 			resumeUpdates(scheduleJob);
 		}
 	}
 	
 
 	public static void clearResourceTreeRootCache(WorkbenchComponent aModule) {
 
 		ResourceTreeRootAdapter resourceTreeAdapter = (ResourceTreeRootAdapter) ExtendedEcoreUtil
 				.getAdapter(aModule, aModule.eAdapters(),
 						ResourceTreeRootAdapter.DEPLOY_ADAPTER_TYPE);
 		if(null != resourceTreeAdapter) {
 			resourceTreeAdapter.setResourceTreeRoot(null);
 		}
 		resourceTreeAdapter = (ResourceTreeRootAdapter) ExtendedEcoreUtil
 				.getAdapter(aModule, aModule.eAdapters(),
 						ResourceTreeRootAdapter.SOURCE_ADAPTER_TYPE);
 		if(null != resourceTreeAdapter){
 			resourceTreeAdapter.setResourceTreeRoot(null);
 		}
 	}
 
 
 	/*
 	 * Needs to notice changes to MANIFEST.MF in any J2EE projects, changes to
 	 * .component in any J2EE Projects, and any archive changes in EAR projects
 	 */
 	
 	public boolean findNode(IResourceDelta[] delta) {
 
 		for (int i = 0; i < delta.length; i++) {
 			if (delta[i].toString().indexOf(IJ2EEModuleConstants.COMPONENT_FILE_NAME) != -1) {
 				StructureEdit core = StructureEdit
 						.getStructureEditForRead(delta[i].getResource()
 								.getProject());
 				if(null != core){
 					WorkbenchComponent component = core.getComponent();
 					if(component != null){
 						clearResourceTreeRootCache(component);
 					}
 				}
 			} else {
 				findNode(delta[i].getAffectedChildren());
 			}
 		}
 
 		return true;
 	}
 	
 	public boolean visit(IResourceDelta delta) {
 		IResource resource = delta.getResource();
 		switch (resource.getType()) {
 		case IResource.ROOT:
 			return true;
 		case IResource.PROJECT:
 			return ModuleCoreNature.isFlexibleProject((IProject) resource);
 		case IResource.FOLDER: {
 			if (resource.getName().equals(IJ2EEModuleConstants.DOT_SETTINGS)) {
 				return true;
 			}
 			IVirtualComponent comp = ComponentCore.createComponent(resource.getProject());
 
 			if (comp instanceof J2EEModuleVirtualComponent || comp instanceof EARVirtualComponent) {
 				IVirtualFolder rootFolder = comp.getRootFolder();
 				if (comp instanceof EARVirtualComponent) {
 					return isRootAncester(resource, rootFolder);
 				} else { // J2EEModuleVirtualComponent
 					return isRootAncester(resource, rootFolder) || isFolder(resource, rootFolder.getFolder(J2EEConstants.META_INF));
 				}
 			}
 			return false;
 		}
 		case IResource.FILE: {
 			String name = resource.getName();
 			if (name.equals(WTPModulesResourceFactory.WTP_MODULES_SHORT_NAME) || name.equals(ProjectUtilities.DOT_CLASSPATH)) {
 				queueUpdate(resource.getProject());
 			} else if (name.equals(J2EEConstants.MANIFEST_SHORT_NAME)) { // MANIFEST.MF must be all caps per spec
 				IFile manifestFile = J2EEProjectUtilities.getManifestFile(resource.getProject(), false);
 				if (null == manifestFile || resource.equals(manifestFile)) {
 					queueUpdateModule(resource.getProject());
 				}
 			} else if (endsWithIgnoreCase(name, IModuleExtensions.DOT_JAR)) {
 				try {
 					if (FacetedProjectFramework.hasProjectFacet(resource.getProject(), J2EEProjectUtilities.ENTERPRISE_APPLICATION)) {
 						IVirtualComponent comp = ComponentCore.createComponent(resource.getProject());
 						if(isFolder(resource.getParent(), comp.getRootFolder())){
 							queueUpdateEAR(resource.getProject());
 						}
 					}
 				} catch (CoreException e) {
 					J2EEPlugin.getDefault().getLogger().logError(e);
 				}
 			}
 		}
 		default:
 			return false;
 		}
 	}
 	
 	public static boolean endsWithIgnoreCase(String str, String sfx) {
 		return str.regionMatches(true, str.length() - sfx.length(), sfx, 0, sfx.length());
 	}
 
 	public static boolean isFolder(IResource resource, IVirtualFolder folder) {
 		IContainer[] realFolders = folder.getUnderlyingFolders();
 		for (int i = 0; i < realFolders.length; i++) {
 			if (realFolders[i].equals(resource)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static boolean isRootAncester(IResource resource, IVirtualFolder rootFolder) {
 		IContainer[] realRoots = rootFolder.getUnderlyingFolders();
 		IPath currentResourcePath = resource.getFullPath();
 		for (int i = 0; i < realRoots.length; i++) {
 			if (currentResourcePath.isPrefixOf(realRoots[i].getFullPath()))
 				return true;
 		}
 		return false;
 	}
 
 }
