 /*******************************************************************************
  * Copyright (c) 2003, 2006 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.common.classpath;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IAccessRule;
 import org.eclipse.jdt.core.IClasspathAttribute;
 import org.eclipse.jdt.core.IClasspathContainer;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.internal.core.ClasspathEntry;
 import org.eclipse.jdt.internal.core.JavaProject;
 import org.eclipse.jdt.internal.core.util.Util;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.common.jdt.internal.classpath.ClasspathDecorations;
 import org.eclipse.jst.common.jdt.internal.classpath.ClasspathDecorationsManager;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
 import org.eclipse.jst.j2ee.componentcore.util.EARVirtualComponent;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EECommonMessages;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.project.EarUtilities;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.javaee.application.Application;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.builder.DependencyGraphManager;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 
 /**
  * This classpath container is based on the Component references; not the manifest entries. Other
  * mechanisms are in place to ensure that the component references are updated when the manifest is
  * updated, and also to make sure the manifest is updated when the component references are updated.
  * 
  */
 public class J2EEComponentClasspathContainer implements IClasspathContainer {
 
 	public static final String CONTAINER_ID = "org.eclipse.jst.j2ee.internal.module.container"; //$NON-NLS-1$
 	public static final IPath CONTAINER_PATH = new Path(CONTAINER_ID);
 
 	private static IPath WEBLIB = new Path("/WEB-INF/lib"); //$NON-NLS-1$
 	
 	private static ClasspathDecorationsManager decorationsManager = new ClasspathDecorationsManager(J2EEPlugin.PLUGIN_ID);
 
 	public static ClasspathDecorationsManager getDecorationsManager() {
         return decorationsManager;
     }
 	
 	private IPath containerPath;
 	private IJavaProject javaProject;
 	private IClasspathEntry[] entries = new IClasspathEntry[0];
 	private boolean exportEntries = true; //the default behavior is to always export these dependencies
 	private static Map keys = new Hashtable();
 	private static Map previousSelves = new Hashtable();
 	private static int MAX_RETRIES = 10;
 	private static Map retries = new Hashtable();
 	
 	private class LastUpdate {
 		private long dotClasspathModificationStamp = -1;
 		private int refCount = 0;
 		private boolean[] isBinary = new boolean[refCount];
 		private IPath[] paths = new IPath[refCount];
 	}
 
 	private LastUpdate lastUpdate = new LastUpdate();
 
 	public J2EEComponentClasspathContainer(IPath path, IJavaProject javaProject) {
 		this.containerPath = path;
 		this.javaProject = javaProject;
 	}
 
 	private boolean requiresUpdate() {
 		IVirtualComponent component = ComponentCore.createComponent(javaProject.getProject());
 		if (component == null) {
 			return false;
 		}
 		
 		IFile dotClasspath = javaProject.getProject().getFile(ProjectUtilities.DOT_CLASSPATH);
 		long dotClasspathModificationStamp = dotClasspath.exists() ? dotClasspath.getModificationStamp() : 0;
 		if(dotClasspathModificationStamp != lastUpdate.dotClasspathModificationStamp){
 			return true;
 		}
 		
		IVirtualReference[] refs = component instanceof J2EEModuleVirtualComponent ? ((J2EEModuleVirtualComponent)component).getReferences(false, true): component.getReferences();
 		IVirtualComponent comp = null;
 
 		// avoid updating the container if references haven't changed
 		if (refs.length == lastUpdate.refCount) {
 			for (int i = 0; i < lastUpdate.refCount; i++) {
 				comp = refs[i].getReferencedComponent();
 				if (comp.isBinary() != lastUpdate.isBinary[i]) {
 					return true;
 				} else {
 					IPath path = null;
 					if (comp.isBinary()) {
 						VirtualArchiveComponent archiveComp = (VirtualArchiveComponent) comp;
 						java.io.File diskFile = archiveComp.getUnderlyingDiskFile();
 						if (diskFile.exists())
 							path = new Path(diskFile.getAbsolutePath());
 						else {
 							IFile iFile = archiveComp.getUnderlyingWorkbenchFile();
 							path = iFile.getFullPath();
 						}
 					} else {
 						path = comp.getProject().getFullPath();
 					}
 					if (!path.equals(lastUpdate.paths[i])) {
 						return true;
 					}
 				}
 			}
 			return false;
 		}
 		return true;
 	}
 	
 	private void update() {
 		if(!javaProject.isOpen()){
 			try {
 				if(javaProject.getProject().exists() && javaProject.getProject().hasNature(JavaCore.NATURE_ID)){
 					javaProject.open(null);
 				} else {
 					return;
 				}
 			} catch (JavaModelException e) {
 				Logger.getLogger().logError(e);
 			} catch (CoreException e) {
 				//ignore 
 				return;
 			}
 		}
 		
 		IVirtualComponent component = ComponentCore.createComponent(javaProject.getProject());
 		Object key = keys.get(new Integer(javaProject.getProject().hashCode()));
 		J2EEComponentClasspathContainer firstPreviousSelf = (J2EEComponentClasspathContainer)previousSelves.get(key);
 		if (component == null) {
 			return;
 		} 
 		
 		if(!javaProject.getProject().getFile(StructureEdit.MODULE_META_FILE_NAME).exists()){
 			Integer retryCount = (Integer)retries.get(key);
 			if(retryCount == null){
 				retryCount = new Integer(1);
 			} else if(retryCount.intValue() > MAX_RETRIES){
 				return;
 			} else {
 				retryCount = new Integer(retryCount.intValue() + 1);
 			}
 			retries.put(key, retryCount);
 			J2EEComponentClasspathUpdater.getInstance().queueUpdate(javaProject.getProject());
 			return;
 		} else {
 			retries.remove(key);
 		}
 		
 		IFile dotClasspath = javaProject.getProject().getFile(ProjectUtilities.DOT_CLASSPATH);
 		lastUpdate.dotClasspathModificationStamp = dotClasspath.exists() ? dotClasspath.getModificationStamp() : 0;
 		
 		IVirtualComponent comp = null;
 		IVirtualReference ref = null;
 		
		IVirtualReference[] refs = component instanceof J2EEModuleVirtualComponent ? ((J2EEModuleVirtualComponent)component).getReferences(false, true): component.getReferences();
 
 		List refsList = new ArrayList();
 		Set refedComps = new HashSet();
 		refedComps.add(component);
 		for(int i = 0; i<refs.length;i++){
 			refsList.add(refs[i]);
 			refedComps.add(refs[i].getReferencedComponent());
 		}
 		
 		// check for the references in the lib dirs of the referencing EARs
 		IVirtualComponent[] referencingList = component.getReferencingComponents();
 		for (IVirtualComponent referencingComp : referencingList) {
 			// check if the referencing component is an EAR
 			if (EarUtilities.isEARProject(referencingComp.getProject())) {
 				EARVirtualComponent earComp = (EARVirtualComponent) referencingComp;
 				// retrieve the EAR's library directory 
 				String libDir = getEARLibDir(earComp);
 				// if the EAR version is lower than 5, then the library directory will be null
 				if (libDir != null) {
 					// check if the component itself is not in the library directory of this EAR - avoid cycles in the build patch
 					if (!libDir.equals(earComp.getReference(component.getName()).getRuntimePath().toString())) {
 						// retrieve the referenced components from the EAR
 						IVirtualReference[] earRefs = earComp.getReferences();
 						for (IVirtualReference earRef : earRefs) {
 							// check if the referenced component is in the library directory
 							if (libDir.equals(earRef.getRuntimePath().toString())) {
 								IVirtualComponent earRefComp = earRef.getReferencedComponent();
 								// check if the referenced component is already visited - avoid cycles in the build path
 								if (!refedComps.contains(earRefComp)) {
 									// visit the referenced component
 									refsList.add(earRef);
 									refedComps.add(earRefComp);
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		
 		for(int i=0; i< refsList.size(); i++){
 			comp = ((IVirtualReference)refsList.get(i)).getReferencedComponent();
 			if(comp.isBinary()){
 				IVirtualReference [] binaryRefs = comp.getReferences();
 				for(int j = 0; j<binaryRefs.length; j++){
 					if(!refedComps.contains(binaryRefs[j].getReferencedComponent())){
 						refsList.add(binaryRefs[j]);
 						refedComps.add(binaryRefs[j].getReferencedComponent());
 					}
 				}
 			}
 		}
 		
 		lastUpdate.refCount = refsList.size();
 		lastUpdate.isBinary = new boolean[lastUpdate.refCount];
 		lastUpdate.paths = new IPath[lastUpdate.refCount];
 
 		boolean isWeb = JavaEEProjectUtilities.isDynamicWebProject(component.getProject());
 		boolean shouldAdd = true;
 
 		List entriesList = new ArrayList();
 
 		try {
 			IJavaProject javaProject = JavaCore.create(component.getProject());
 			
 			boolean useJDTToControlExport = J2EEComponentClasspathContainerUtils.getDefaultUseEARLibrariesJDTExport();
 			if(useJDTToControlExport){
 				//if the default is not enabled, then check whether the container is being exported
 				try{
 					IClasspathEntry [] rawEntries = javaProject.getRawClasspath();
 					for(int i=0;i<rawEntries.length; i++){
 						IClasspathEntry entry = rawEntries[i];
 						if(entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER){
 							if(entry.getPath().equals(CONTAINER_PATH)){
 								exportEntries = entry.isExported();
 								break;
 							}
 						}
 					}
 				}  catch (JavaModelException e) {
 					Logger.getLogger().logError(e);
 				}
 			}
 			
 			Collection <IClasspathEntry> existingEntries = new ArrayList<IClasspathEntry>();
 			try {
 				IClasspathContainer container = JavaCore.getClasspathContainer(CONTAINER_PATH, javaProject);
 				IClasspathEntry[] previousEntries  = null;
 				if(null != container){
 					previousEntries = container.getClasspathEntries();
 				}
 				existingEntries.addAll(Arrays.asList(javaProject.getResolvedClasspath(true)));
 				removeMatchingEntries(previousEntries, existingEntries);
 				
 				if(firstPreviousSelf != null){
 					removeMatchingEntries(firstPreviousSelf.entries, existingEntries);
 				}
 				J2EEComponentClasspathContainer secondPreviousSelf = (J2EEComponentClasspathContainer)previousSelves.get(key);
 				if(firstPreviousSelf != secondPreviousSelf && secondPreviousSelf != null){
 					removeMatchingEntries(secondPreviousSelf.entries, existingEntries);
 				}
 				removeMatchingEntries(entries, existingEntries);
 			} catch (JavaModelException e) {
 				Logger.getLogger().logError(e);
 			}
 			
 			for (int i = 0; i < refsList.size(); i++) {
 				ref = (IVirtualReference)refsList.get(i);
 				comp = ref.getReferencedComponent();
 				lastUpdate.isBinary[i] = comp.isBinary();
 				shouldAdd = !(isWeb && ref.getRuntimePath().equals(WEBLIB)); 
 				if (!shouldAdd) {
 					continue;
 				}
 				if (comp.isBinary()) {
 					VirtualArchiveComponent archiveComp = (VirtualArchiveComponent) comp;
 					if (archiveComp.getArchiveType().equals(VirtualArchiveComponent.CLASSPATHARCHIVETYPE)) {
 						// do not process components dynamically computed from the Java classpath
 						continue;
 					}
 					java.io.File diskFile = archiveComp.getUnderlyingDiskFile();
 					if (diskFile.exists()) {
 						lastUpdate.paths[i] = new Path(diskFile.getAbsolutePath());
 					} else {
 						IFile iFile = archiveComp.getUnderlyingWorkbenchFile();
 						lastUpdate.paths[i] = iFile.getFullPath();
 					}
 					if (!isAlreadyOnClasspath(existingEntries, lastUpdate.paths[i])) {
 						ClasspathDecorations dec = decorationsManager.getDecorations( getPath().toString(), lastUpdate.paths[i].toString() );
 						
 						IPath srcpath = null;
 				        IPath srcrootpath = null;
 				        IClasspathAttribute[] attrs = {};
 				        IAccessRule[] access = {};
 						
 				        if( dec != null ) {
 				            srcpath = dec.getSourceAttachmentPath();
 				            srcrootpath = dec.getSourceAttachmentRootPath();
 				            attrs = dec.getExtraAttributes();
 				        }
 			        
 //				        entriesList.add(JavaCore.newLibraryEntry( lastUpdate.paths[i], srcpath, srcrootpath, access, attrs, exportEntries ));
 				        IClasspathEntry newEntry = JavaCore.newLibraryEntry( lastUpdate.paths[i], srcpath, srcrootpath, access, attrs, exportEntries ); 
 				        entriesList.add(newEntry);
 				        existingEntries.add(newEntry);
 				        
 					}
 				} else {
 					IProject project = comp.getProject();
 					lastUpdate.paths[i] = project.getFullPath();
 					if (!isAlreadyOnClasspath(existingEntries, lastUpdate.paths[i])) {
 						entriesList.add(JavaCore.newProjectEntry(lastUpdate.paths[i], exportEntries));
 					}
 				}
 			}
 		} finally {
 			entries = new IClasspathEntry[entriesList.size()];
 			for (int i = 0; i < entries.length; i++) {
 				entries[i] = (IClasspathEntry) entriesList.get(i);
 			}
 		}
 		previousSelves.put(key, this);
 	}
 
 	private void removeMatchingEntries(IClasspathEntry [] oldEntries, Collection<IClasspathEntry> existingEntries) {
 		if(oldEntries != null){
 			for(IClasspathEntry oldEntry : oldEntries){
 				Iterator <IClasspathEntry> iterator = existingEntries.iterator();
 				while(iterator.hasNext()){
 					IClasspathEntry existingEntry = iterator.next(); 
 					if(isAlreadyOnClasspath(existingEntry, oldEntry.getPath())){
 						iterator.remove();
 						break;
 					}
 				}
 			}
 		}
 	}
 
 	public static void install(IPath containerPath, IJavaProject javaProject) {
 		try{
 			J2EEComponentClasspathUpdater.getInstance().pauseUpdates();
 			Integer hashCode = new Integer(javaProject.getProject().hashCode());
 			Object key = keys.get(hashCode);
 			if(key == null){
 				keys.put(hashCode, hashCode);
 				key = hashCode;
 			}
 			final IJavaProject[] projects = new IJavaProject[]{javaProject};
 			final J2EEComponentClasspathContainer container = new J2EEComponentClasspathContainer(containerPath, javaProject);
 			container.update();
 			final IClasspathContainer[] conts = new IClasspathContainer[]{container};
 			try {
 				JavaCore.setClasspathContainer(containerPath, projects, conts, null);
 				previousSelves.put(key, container);
 			} catch (JavaModelException e) {
 				Logger.getLogger().log(e);
 			}
 		} finally {
 			J2EEComponentClasspathUpdater.getInstance().resumeUpdates();
 		}
 	}
 
 	public void refresh(boolean force){
 		if(force || requiresUpdate()){
 			install(containerPath, javaProject);
 			if (J2EEComponentClasspathUpdater.shouldUpdateDependencyGraph())
 			{
 				// Update dependency graph
 				DependencyGraphManager.getInstance().forceRefresh();
 				// [202820]
 				J2EEComponentClasspathUpdater.setUpdateDependencyGraph(false);
 			}
 		}
 	}
 	
 	public void refresh() {
 		refresh(false);
 	}
 
 	private boolean isUpdating = false;
 	
 	public IClasspathEntry[] getClasspathEntries() {
 		if(!isUpdating){
 			if(this != J2EEComponentClasspathContainerUtils.getInstalledEARLibrariesContainer(javaProject.getProject())){
 				try {
 					isUpdating = true;
 					update();
 				} finally{
 					isUpdating = false;
 				}
 			}
 		}
 		return entries;
 	}
 
 	public String getDescription() {
 		return J2EECommonMessages.J2EE_MODULE_CLASSPATH_CONTAINER_NAME;
 	}
 
 	public int getKind() {
 		return K_APPLICATION;
 	}
 
 	public IPath getPath() {
 		return containerPath;
 	}
 
 	/**
 	 * Taken from {@link JavaProject#isOnClasspath(org.eclipse.core.resources.IResource)}
 	 * 
 	 * @param classpath
 	 * @param newPath
 	 * @return
 	 */
 	private static boolean isAlreadyOnClasspath(Collection<IClasspathEntry> classpath, IPath newPath) {
 		for (Iterator itr = classpath.iterator(); itr.hasNext();) {
 			IClasspathEntry entry = (IClasspathEntry) itr.next();
 			if(isAlreadyOnClasspath(entry, newPath)){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Taken from {@link JavaProject#isOnClasspath(org.eclipse.core.resources.IResource)}
 	 * 
 	 * @param classpath
 	 * @param newPath
 	 * @return
 	 */
 	private static boolean isAlreadyOnClasspath(IClasspathEntry classpathEntry, IPath newPath){
 		IPath entryPath = classpathEntry.getPath();
 		if (entryPath.equals(newPath)) { // package fragment roots must match exactly entry
 			// paths (no exclusion there)
 			return true;
 		}
 		if (entryPath.isPrefixOf(newPath) && !Util.isExcluded(newPath, ((ClasspathEntry) classpathEntry).fullInclusionPatternChars(), ((ClasspathEntry) classpathEntry).fullExclusionPatternChars(), false)) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * Get the library directory from an EAR virtual component
 	 * 
 	 * @param earComponent
 	 *            the EAR virtual component
 	 * 
 	 * @return a runtime representation of the library directory path or null if
 	 *         the EAR's version is lower than 5
 	 */
 	private String getEARLibDir(EARVirtualComponent earComponent) {
 		// check if the EAR component's version is 5 or greater
 		IProject project = earComponent.getProject();
 		if (!JavaEEProjectUtilities.isJEEComponent(earComponent, JavaEEProjectUtilities.DD_VERSION)) return null;
 		
 		// retrieve the model provider
 		IModelProvider modelProvider = ModelProviderManager.getModelProvider(project);
 		if (modelProvider == null) return null;
 		
 		// retrieve the EAR's model object
 		Application app = (Application) modelProvider.getModelObject();
 		if (app == null) return null;
 		
 		// retrieve the library directory from the model
 		String libDir = app.getLibraryDirectory();
 		if (libDir == null) {
 			// the library directory is not set - use the default one
 			libDir = J2EEConstants.EAR_DEFAULT_LIB_DIR;
 		}
 		
 		return libDir;
 	}
 	
 }
