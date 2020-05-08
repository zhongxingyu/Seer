 /*******************************************************************************
  * Copyright (c) 2003, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.common.jdt.internal.javalite.JavaLiteUtilities;
 import org.eclipse.jst.j2ee.application.Application;
 import org.eclipse.jst.j2ee.classpathdep.ClasspathDependencyUtil;
 import org.eclipse.jst.j2ee.classpathdep.IClasspathDependencyConstants;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualArchiveComponent;
 import org.eclipse.jst.j2ee.componentcore.J2EEModuleVirtualComponent;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.internal.EjbModuleExtensionHelper;
 import org.eclipse.jst.j2ee.internal.IEJBModelExtenderManager;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyManifestUtil;
 import org.eclipse.jst.j2ee.internal.classpathdep.ClasspathDependencyVirtualComponent;
 import org.eclipse.jst.j2ee.internal.componentcore.JavaEEBinaryComponentHelper;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.j2ee.internal.plugin.J2EEPlugin;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.project.JavaEEProjectUtilities;
 import org.eclipse.jst.jee.util.internal.JavaEEQuickPeek;
 import org.eclipse.jst.server.core.IApplicationClientModule;
 import org.eclipse.jst.server.core.IConnectorModule;
 import org.eclipse.jst.server.core.IEJBModule;
 import org.eclipse.jst.server.core.IEnterpriseApplication;
 import org.eclipse.jst.server.core.IJ2EEModule;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.ComponentResource;
 import org.eclipse.wst.common.componentcore.internal.Property;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.resources.VirtualArchiveComponent;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.model.IModuleFile;
 import org.eclipse.wst.server.core.model.IModuleFolder;
 import org.eclipse.wst.server.core.model.IModuleResource;
 import org.eclipse.wst.server.core.util.ModuleFile;
 import org.eclipse.wst.server.core.util.ModuleFolder;
 import org.eclipse.wst.web.internal.deployables.ComponentDeployable;
 /**
  * J2EE module superclass.
  */
 public class J2EEFlexProjDeployable extends ComponentDeployable implements 
 				IJ2EEModule, IEnterpriseApplication, IApplicationClientModule, 
 				IConnectorModule, IEJBModule, IWebModule {
 	protected static final IPath WEB_CLASSES_PATH = new Path(J2EEConstants.WEB_INF_CLASSES);
 	protected static final IPath MANIFEST_PATH = new Path(J2EEConstants.MANIFEST_URI);
 	protected static IPath WEBLIB = new Path(J2EEConstants.WEB_INF_LIB).makeAbsolute();
 	private static String USE_SINGLE_ROOT_PROPERTY = "useSingleRoot"; //$NON-NLS-1$
 	protected IPackageFragmentRoot[] cachedSourceContainers;
 	protected IContainer[] cachedOutputContainers;
 	protected HashMap cachedOutputMappings;
 	protected HashMap cachedSourceOutputPairs;
 	protected List classpathComponentDependencyURIs = new ArrayList();
 
 	/**
 	 * Constructor for J2EEFlexProjDeployable.
 	 * 
 	 * @param project
 	 * @param aComponent
 	 */
 	public J2EEFlexProjDeployable(IProject project, IVirtualComponent aComponent) {
 		super(project, aComponent);
 	}
 	
 
 	/**
 	 * Constructor for J2EEFlexProjDeployable.
 	 * 
 	 * @param project
 	 */
 	public J2EEFlexProjDeployable(IProject project) {
 		super(project);
 	}
 
 	/**
 	 * Returns the root folders for the resources in this module.
 	 * The implementation was no different than the superclass implementation;
 	 * 
 	 * @return a possibly-empty array of resource folders
 	 */
 	@Override
 	public IContainer[] getResourceFolders() {
 		return super.getResourceFolders();
 	}
 
 	/**
 	 * Returns the root folders containing Java output in this module.
 	 * 
 	 * @return a possibly-empty array of Java output folders
 	 */
 	public IContainer[] getJavaOutputFolders() {
 		if (cachedOutputContainers == null)
 			cachedOutputContainers = getJavaOutputFolders(component);
 		return cachedOutputContainers;
 	}
 	
 	public IContainer[] getJavaOutputFolders(IVirtualComponent component) {
 		if (component == null)
 			return new IContainer[0];
 		List<IContainer> l = JavaLiteUtilities.getJavaOutputContainers(component);
 		return l.toArray(new IContainer[l.size()]);
 	}	
 
 	@Override
 	protected boolean shouldIncludeUtilityComponent(IVirtualComponent virtualComp,IVirtualReference[] references, ArtifactEdit edit) {
 		// If the component module is an EAR we know all archives are filtered out of virtual component members
 		// and we will return only those archives which are not binary J2EE modules in the EAR DD.  These J2EE modules will
 		// be returned by getChildModules()
 		if (J2EEProjectUtilities.isEARProject(component.getProject())) {
 			return virtualComp != null && virtualComp.isBinary() && !isNestedJ2EEModule(virtualComp, references, (EARArtifactEdit)edit);
 		} 
 		return super.shouldIncludeUtilityComponent(virtualComp, references, edit);
 	}
 	
 	@Override
 	protected void addUtilMember(IVirtualComponent parent, IVirtualReference reference, IPath runtimePath) {
 		// do not add classpath dependencies whose runtime path (../) maps to the parent component or that represent
 		// class folders
 		if (!runtimePath.equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH) 
 				&& !ClasspathDependencyUtil.isClassFolderReference(reference)) {
 			super.addUtilMember(parent, reference, runtimePath);
 		}
 	}
 	
 	protected IModuleResource[] getBinaryModuleMembers() {
 		IPath archivePath = ((J2EEModuleVirtualArchiveComponent)component).getWorkspaceRelativePath();
 		ModuleFile mf = null;
 		if (archivePath != null) { //In Workspace
 			IFile utilFile = ResourcesPlugin.getWorkspace().getRoot().getFile(archivePath);
 			mf = new ModuleFile(utilFile, utilFile.getName(), ((J2EEModuleVirtualArchiveComponent)component).getRuntimePath().makeRelative());
 		} else {
 			File extFile = ((J2EEModuleVirtualArchiveComponent)component).getUnderlyingDiskFile();
 			if( extFile != null )
 				mf = new ModuleFile(extFile, extFile.getName(), ((J2EEModuleVirtualArchiveComponent)component).getRuntimePath().makeRelative());
 		}
 		return mf == null ? new IModuleResource[]{} : new IModuleResource[] {mf};
 	}
 	
 	@Override
 	public IModuleResource[] members() throws CoreException {
 		members.clear();
 		classpathComponentDependencyURIs.clear();
 		cachedSourceContainers = null;
 		getSourceContainers(); // set the cache
 
 		// Handle binary components
 		if (component instanceof J2EEModuleVirtualArchiveComponent)
 			return getBinaryModuleMembers();
 
 		if (JavaEEProjectUtilities.isEARProject(component.getProject())) {
 			// If an EAR, add classpath contributions for all referenced modules
 			addReferencedComponentClasspathDependencies(component, false);
 		} else {
 			if (JavaEEProjectUtilities.isDynamicWebProject(component.getProject())) {
 				// If a web, add classpath contributions for all WEB-INF/lib modules
 				addReferencedComponentClasspathDependencies(component, true);
 			}
 			if (canExportClasspathComponentDependencies(component)){
 				saveClasspathDependencyURIs(component);
 			}
 			// Add all Java output folders that have publish/export attributes
 			addClassFolderDependencies(component);
 		}
 
 		if( isForceSingleRoot())
 			return getOptimizedMembers();
 
 		try {
 
 			/*
 			 *  This section will traverse the VCF and create all the 
 			 *  ModuleFile and ModuleFolder objects needed.
 			 *  The impl. is a bit odd. Unseen-before folders
 			 *  are automatically added to "members", but unseen files
 			 *  are instead returned via the return value.
 			 *  The upstream impl could benefit from cleanup
 			 */
 			
 			if (component != null) {
 				IVirtualFolder vFolder = component.getRootFolder();
 				IModuleResource[] mr = getMembers(vFolder, Path.EMPTY);
 				int size = mr.length;
 				for (int j = 0; j < size; j++) {
 					members.add(mr[j]);
 				}
 
 				addRelevantOutputFolders();
 
 				if (component != null) {
 					addUtilMembers(component);
 					List consumableMembers = getConsumableReferencedMembers(component);
 					if (!consumableMembers.isEmpty())
 						members.addAll(consumableMembers);
 				}
 			}
 			
 			IModuleResource[] mr = new IModuleResource[members.size()];
 			members.toArray(mr);
 			return mr;
 		} finally {
 			cachedSourceContainers = null;
 			cachedOutputContainers = null;
 			cachedOutputMappings = null;
 			cachedSourceOutputPairs = null;
 		}
 	}
 
 
     /*
 	 * This method was separated out because previously the paths that designated
 	 * where to put these files was completely ignoring the component.xml
 	 * and was instead making assumptions based on project type. 
 	 */
 	protected void addRelevantOutputFolders() throws CoreException {
 		StructureEdit edit = null;
 		try {
 			edit = StructureEdit.getStructureEditForRead(getProject());
 			if (edit == null || edit.getComponent() == null)
 				return;
 			List resourceMaps = edit.getComponent().getResources();
 			ComponentResource source;
 			for( int i = 0; i < resourceMaps.size(); i++ ) {
 				source = ((ComponentResource)resourceMaps.get(i));
 				if( isSourceContainer(source)) {
 					IContainer out = getOutputContainerIfExists(source.getSourcePath());
 					if (ComponentCore.createResources(out).length > 0) 
 						continue;
					IModuleResource[] mr = getMembers(out, 
							source.getRuntimePath().makeRelative(), 
							source.getRuntimePath().makeRelative(), 
							getJavaOutputFolders());
 					int size2 = mr.length;
 					for (int j = 0; j < size2; j++) {
 						members.add(mr[j]);
 					}
 				}
 			}
 		} finally {
 			if( edit != null ) 
 				edit.dispose();
 		}
 	}
 
 	protected boolean hasConsumableReferences(IVirtualComponent vc) {
 		IVirtualReference[] refComponents = vc.getReferences();
     	for (int i = 0; i < refComponents.length; i++) {
     		IVirtualReference reference = refComponents[i];
     		if (reference != null && reference.getDependencyType()==IVirtualReference.DEPENDENCY_TYPE_CONSUMES) 
     			return true;
     	}
     	return false;
     }
 	
 	@Override
 	protected IModuleFile createModuleFile(IFile file, IPath path) {
 		// if this is the MANIFEST.MF file and we have classpath component dependencies, 
 		// update it
 		return replaceManifestFile(super.createModuleFile(file, path));
 	}
 	
 	protected IModuleFile replaceManifestFile(IModuleFile moduleFile) {
 		final IFile file = (IFile) moduleFile.getAdapter(IFile.class);
 		final IPath path = moduleFile.getModuleRelativePath();
 		// if the MANIFEST.MF is being requested and we have classpath component dependencies, 
 		// dynamically generate a customized MANIFEST.MF and return that 
 		if (path.append(file.getName()).equals(MANIFEST_PATH) && !classpathComponentDependencyURIs.isEmpty()) {
 			final IProject project = file.getProject();
 			final IPath workingLocation = project.getWorkingLocation(J2EEPlugin.PLUGIN_ID);
 			// create path to temp MANIFEST.MF
 			final IPath tempManifestPath = workingLocation.append(MANIFEST_PATH);
 			final File fsFile = tempManifestPath.toFile();
 			if (!fsFile.exists()) {
 				// create parent dirs for temp MANIFEST.MF
 				final File parent = fsFile.getParentFile();
 				if (!parent.exists()) {
 					if (!parent.mkdirs()) {
 						return moduleFile;
 					}
 				}
 			}
 			// create temp MANIFEST.MF using util method
 			try {
 				ClasspathDependencyManifestUtil.updateManifestClasspath(file, classpathComponentDependencyURIs, fsFile);
 				// create new ModuleFile that points to temp MANIFEST.MF
 				return new ModuleFile(fsFile, file.getName(), path);
 			} catch (IOException ioe) {
 				return moduleFile;
 			}
 		}
 		return moduleFile;
 	}
 	
 	@Override
 	protected IModuleResource[] handleJavaPath(IPath path, IPath javaPath, IPath curPath, IContainer[] javaCont, IModuleResource[] moduleResource, IContainer cc) throws CoreException {
 		IModuleResource [] mr = moduleResource;
 		if (curPath.equals(javaPath)) {
 			int size = javaCont.length;
 			for (int i = 0; i < size; i++) {
 				IModuleResource[] mr2 = getMembers(javaCont[i], path.append(cc.getName()), null, null);
 				IModuleResource[] mr3 = new IModuleResource[mr.length + mr2.length];
 				System.arraycopy(mr, 0, mr3, 0, mr.length);
 				System.arraycopy(mr2, 0, mr3, mr.length, mr2.length);
 				mr = mr3;
 			}
 		} else {
 			boolean containsFolder = false;
 			String name = javaPath.segment(curPath.segmentCount());
 			int size = mr.length;
 			for (int i = 0; i < size && !containsFolder; i++) {
 				if (mr[i] instanceof IModuleFolder) {
 					IModuleFolder mf2 = (IModuleFolder) mr[i];
 					if (name.equals(mf2.getName())) {
 						containsFolder = true;
 					}
 				}
 			}
 			
 			if (!containsFolder && javaCont.length > 0) {
 				ModuleFolder mf2 = new ModuleFolder(javaCont[0], name, curPath);
 				IModuleResource[] mrf = new IModuleResource[0];
 				size = javaCont.length;
 				for (int i = 0; i < size; i++) {
 					IModuleResource[] mrf2 = getMembers(javaCont[i], javaPath, null, null);
 					IModuleResource[] mrf3 = new IModuleResource[mrf.length + mrf2.length];
 					System.arraycopy(mrf, 0, mrf3, 0, mrf.length);
 					System.arraycopy(mrf2, 0, mrf3, mrf.length, mrf2.length);
 					mrf = mrf3;
 				}
 				
 				mf2.setMembers(mrf);
 				
 				IModuleResource[] mr3 = new IModuleResource[mr.length + 1];
 				System.arraycopy(mr, 0, mr3, 0, mr.length);
 				mr3[mr.length] = mf2;
 				mr = mr3;
 			}
 		}
 		return mr;
 	}
 
     /**
      * Returns the classpath as a list of absolute IPaths.
      * 
      * @return an array of paths
      */
     public IPath[] getClasspath() {
 		List<IPath> paths = new ArrayList<IPath>();
         IJavaProject proj = JemProjectUtilities.getJavaProject(getProject());
         URL[] urls = JemProjectUtilities.getClasspathAsURLArray(proj);
 		for (int i = 0; i < urls.length; i++) {
 			URL url = urls[i];
 			paths.add(Path.fromOSString(url.getPath()));
 		}
         return paths.toArray(new IPath[paths.size()]);
     }
     
     public String getJNDIName(String ejbName) {
     	if (!JavaEEProjectUtilities.isEJBProject(component.getProject()))
     		return null;
 		EjbModuleExtensionHelper modHelper = null;
 		EJBJar jar = null;
 		ArtifactEdit ejbEdit = null;
 		try {
 			ejbEdit = ComponentUtilities.getArtifactEditForRead(component);
 			if (ejbEdit != null) {
 				jar = (EJBJar) ejbEdit.getContentModelRoot();
 				modHelper = IEJBModelExtenderManager.INSTANCE.getEJBModuleExtension(null);
 				return modHelper == null ? null : modHelper.getJNDIName(jar, jar.getEnterpriseBeanNamed(ejbName));
 			}
 		} catch (Exception e) {
 			J2EEPlugin.logError(e);
 		} finally {
 			if (ejbEdit != null)
 				ejbEdit.dispose();
 		}
 		return null;
 	}
 
     /**
      * This method will handle a number of J2EE related scenarios.  If this is an ear and a child module is passed in,
      * the URI for that child module will be returned.  If no child module was passed, the URI of the EAR is returned.
      * If this is a child component and the module passed in is the EAR, we grab the module uri for this compared to that
      * EAR.  If no ear module is passed in we look for one and use it and return URI relative to found EAR.  If no EAR's 
      * are found the URI is returned in a default manner.
      * 
      * @return URI string
      */
     public String getURI(IModule module) {
     	// If the component is an ear and the module passed in is a child module
     	if (component!=null && module!=null && JavaEEProjectUtilities.isEARProject(component.getProject()))
  			return getContainedURI(module);
 
     	IVirtualComponent ear = null;
     	String aURI = null;
     	// If the component is a child module and the module passed in is the ear
     	if (module != null && JavaEEProjectUtilities.isEARProject(module.getProject()))
     		ear = ComponentCore.createComponent(module.getProject());
     	// else if the component is a child module and the module passed in is null, search for first ear
     	else if (module==null && component != null) {
     		IProject[] earProjects = J2EEProjectUtilities.getReferencingEARProjects(component.getProject());
 	        if (earProjects.length>0)
 	        	ear = ComponentCore.createComponent(earProjects[0]);
     	}
     	// We have a valid ear and the component is a valid child
     	if (ear != null && component != null) {
     		EARArtifactEdit earEdit = null;
 			try {
 				earEdit = EARArtifactEdit.getEARArtifactEditForRead(ear);
 				if (earEdit != null)
 					aURI = earEdit.getModuleURI(component);
 			} catch (Exception e) {
 				J2EEPlugin.logError(e);
 			} finally {
 				if (earEdit != null)
 					earEdit.dispose();
 			}
     	} 
     	// We have an ear component and no child module
     	else if (component!=null && JavaEEProjectUtilities.isEARProject(component.getProject())) {
 			aURI = component.getDeployedName()+IJ2EEModuleConstants.EAR_EXT;
     	} 
     	// We have child components but could not find valid ears
     	else if (component != null && JavaEEProjectUtilities.isDynamicWebProject(component.getProject())) {
 			if (module != null) {
 				IVirtualComponent webComp = ComponentCore.createComponent(component.getProject());
 				String extension = IJ2EEModuleConstants.JAR_EXT;
 				if (JavaEEProjectUtilities.isDynamicWebProject(module.getProject())) {
 					extension = IJ2EEModuleConstants.WAR_EXT;
 				} else if (JavaEEProjectUtilities.isJCAProject(module.getProject())) {
 					extension = IJ2EEModuleConstants.RAR_EXT;
 				}
 				IVirtualReference reference = webComp.getReference(module.getProject().getName());
 				if(reference != null){
 					aURI = ComponentUtilities.getDeployUriOfComponent(reference, extension);
 				} else {
 					aURI = webComp.getDeployedName() + extension;
 				}
 			}
 			else {
 				aURI = component.getDeployedName() + IJ2EEModuleConstants.WAR_EXT;
 			}
 		} 
     	else if (component!=null && (J2EEProjectUtilities.isEJBProject(component.getProject()) || J2EEProjectUtilities.isApplicationClientProject(component.getProject()))) {
     		aURI = component.getDeployedName()+IJ2EEModuleConstants.JAR_EXT;
     	} 
     	else if (component!=null && J2EEProjectUtilities.isJCAProject(component.getProject())) {
     		aURI = component.getDeployedName()+IJ2EEModuleConstants.RAR_EXT;
     	}
     	
     	if (aURI !=null && aURI.length()>1 && aURI.startsWith("/")) //$NON-NLS-1$
     		aURI = aURI.substring(1);
     	return aURI;
 	}
     
     protected boolean isBinaryModuleArchive(IModule module) {
     	if (module!=null && (module.getName().endsWith(IJ2EEModuleConstants.JAR_EXT) || module.getName().endsWith(IJ2EEModuleConstants.WAR_EXT) ||
     			module.getName().endsWith(IJ2EEModuleConstants.RAR_EXT))) {
     		if (component!=null && JavaEEProjectUtilities.isEARProject(component.getProject()))
     			return true;
     	}
     	return false;
     }
     
     private String getContainedURI(IModule module) {
     	if( isBinaryModuleArchive(module)) {
     		J2EEFlexProjDeployable moduleDelegate = (J2EEFlexProjDeployable)module.loadAdapter(J2EEFlexProjDeployable.class, null);
     		if( moduleDelegate != null && moduleDelegate.component != null
     				&& moduleDelegate.component instanceof J2EEModuleVirtualArchiveComponent) {
     			J2EEModuleVirtualArchiveComponent moduleVirtualArchiveComponent = (J2EEModuleVirtualArchiveComponent)moduleDelegate.component;
     			IPath moduleDeployPath = moduleVirtualArchiveComponent.getDeploymentPath();
     			String moduleName = new Path(moduleVirtualArchiveComponent.getName()).lastSegment();
     			if (moduleName.equals(moduleDeployPath.lastSegment())){
     				return moduleDeployPath.toString();
     			}
     			return moduleDeployPath.append(moduleName).toString();
     		}
     	}
 
     	if (component instanceof J2EEModuleVirtualArchiveComponent || isBinaryModuleArchive(module)) {
     		return new Path(module.getName()).lastSegment();
     	}
 
     	IVirtualComponent comp = ComponentCore.createComponent(module.getProject());
     	String aURI = null;
     	if (comp!=null && component!=null && J2EEProjectUtilities.isEARProject(component.getProject())) {
 			EARArtifactEdit earEdit = null;
 			try {
 				earEdit = EARArtifactEdit.getEARArtifactEditForRead(component);
 				if (earEdit != null)
 					aURI = earEdit.getModuleURI(comp);
 			} catch (Exception e) {
 				J2EEPlugin.logError(e);
 			} finally {
 				if (earEdit != null)
 					earEdit.dispose();
 			}
     	}
     	if (aURI !=null && aURI.length()>1 && aURI.startsWith("/")) //$NON-NLS-1$
     		aURI = aURI.substring(1);
     	return aURI;
 	}
     
     /**
      * This method returns the context root property from the deployable project's .component file
      */
     public String getContextRoot() {
 		Properties props = component.getMetaProperties();
 		if(props.containsKey(J2EEConstants.CONTEXTROOT))
 			return props.getProperty(J2EEConstants.CONTEXTROOT);
 	    return component.getName();
     }
     
     /**
      * This method is applicable for a web deployable.  The module passed in should either be null or
      * the EAR module the web deployable is contained in.  It will return the context root from the EAR
      * if it has one or return the .component value in the web project if it is standalone.
      *  
      * @param module
      * @return contextRoot String
      */
     public String getContextRoot(IModule earModule) {
     	IProject deployProject = component.getProject();
     	String contextRoot = null;
     	if (earModule == null)
     		return getContextRoot();
     	else if (JavaEEProjectUtilities.isEARProject(earModule.getProject()) && J2EEProjectUtilities.isDynamicWebProject(deployProject)) {
     		EARArtifactEdit edit = null;
     		try {
     			edit = EARArtifactEdit.getEARArtifactEditForRead(earModule.getProject());
     			contextRoot = edit.getWebContextRoot(deployProject);
     		} finally {
     			if (edit!=null)
     				edit.dispose();
     		}
     	}
     	return contextRoot;
     }
     
     /**
      * Find the source container, if any, for the given file.
      * 
      * @param file
      * @return IPackageFragmentRoot sourceContainer for IFile
      */
     protected IPackageFragmentRoot getSourceContainer(IResource file) {
     	if (file == null)
     		return null;
     	IPackageFragmentRoot[] srcContainers = getSourceContainers();
     	for (int i=0; i<srcContainers.length; i++) {
     		IPath srcPath = srcContainers[i].getPath();
     		if (srcPath.isPrefixOf(file.getFullPath()))
     			return srcContainers[i];
     	}
     	return null;
     }
     
     /**
      * Either returns value from cache or stores result as value in cache for the corresponding
      * output container for the given source container.
      * 
      * @param sourceContainer
      * @return IContainer output container for given source container
      */
     protected IContainer getOutputContainer(IPackageFragmentRoot sourceContainer) {
     	if (sourceContainer == null)
     		return null;
     	
     	HashMap pairs = getCachedSourceOutputPairs();
     	IContainer output = (IContainer) pairs.get(sourceContainer);
     	if (output == null) {
     		output = J2EEProjectUtilities.getOutputContainer(getProject(), sourceContainer);
     		pairs.put(sourceContainer,output);
     	}
     	return output;
     }
     
 	private IPackageFragmentRoot[] getSourceContainers() {
 		if (cachedSourceContainers != null)
 			return cachedSourceContainers;
 		return J2EEProjectUtilities.getSourceContainers(getProject());
 	}
     
     protected List getConsumableReferencedMembers(IVirtualComponent vc) throws CoreException {
 		List consumableMembers = new ArrayList();
 		IVirtualReference[] refComponents = vc.getReferences();
     	for (int i = 0; i < refComponents.length; i++) {
     		IVirtualReference reference = refComponents[i];
     		if (reference != null && reference.getDependencyType()==IVirtualReference.DEPENDENCY_TYPE_CONSUMES) {
     			IVirtualComponent consumedComponent = reference.getReferencedComponent();
     			if (consumedComponent!=null && isProjectOfType(consumedComponent.getProject(),IModuleConstants.JST_UTILITY_MODULE)) {
     				if (consumedComponent.getRootFolder()!=null) {
     					IVirtualFolder vFolder = consumedComponent.getRootFolder();
     					IModuleResource[] mr = getMembers(vFolder, reference.getRuntimePath().makeRelative());
     					int size = mr.length;
     					for (int j = 0; j < size; j++) {
     						if (!members.contains(mr[j]))
     							members.add(mr[j]);
     					}
     					addUtilMembers(consumedComponent);
     					List childConsumableMembers = getConsumableReferencedMembers(consumedComponent);
     					if (!childConsumableMembers.isEmpty())
     						members.addAll(childConsumableMembers);
     				}
     				
     				IContainer[] javaCont = getJavaOutputFolders(consumedComponent);		
     				int size = javaCont.length;
     				for (int j = 0; j < size; j++) {
     					IModuleResource[] mr = getMembers(javaCont[j], reference.getRuntimePath(), reference.getRuntimePath(), javaCont);
     					int size2 = mr.length;
     					for (int k = 0; k < size2; k++) {
     						if (!members.contains(mr[k]))
     							members.add(mr[k]);
     					}
     				}
     			}
     		}
     	}
 		return consumableMembers;
 	}
     
     @Override
 	protected IModule gatherModuleReference(IVirtualComponent component, IVirtualComponent targetComponent ) {
     	IModule module = super.gatherModuleReference(component, targetComponent);
     	// Handle binary module components
     	if (targetComponent instanceof J2EEModuleVirtualArchiveComponent) {
     		if (JavaEEProjectUtilities.isEARProject(component.getProject()) || targetComponent.getProject()!=component.getProject())
     			module = ServerUtil.getModule(J2EEDeployableFactory.ID+":"+targetComponent.getName()); //$NON-NLS-1$
     	}
 		return module;
     }
     
     /**
      * Determine if the component is nested J2EE module on the application.xml of this EAR
      * @param aComponent
      * @return boolean is passed in component a nested J2EE module on this EAR
      */
     private boolean isNestedJ2EEModule(IVirtualComponent aComponent, IVirtualReference[] references, EARArtifactEdit edit) {
     	if (edit==null) 
 			return false;
 		Application app = edit.getApplication();
 		if (app == null) { // if no application.xml, return false
 			return false;
 		}
 		IVirtualReference reference = getReferenceNamed(references,aComponent.getName());
 		// Ensure module URI exists on EAR DD for binary archive
 		boolean inDD = app.getFirstModule(reference.getArchiveName()) != null;
 		return inDD && JavaEEBinaryComponentHelper.getJavaEEQuickPeek(aComponent).getType() != JavaEEQuickPeek.UNKNOWN;
     }
     
     protected IVirtualReference getReferenceNamed(IVirtualReference[] references, String name) {
     	for (int i=0; i<references.length; i++) {
     		if (references[i].getReferencedComponent().getName().equals(name))
     			return references[i];
     	}
     	return null;
     }
     
     @Override
 	protected ArtifactEdit getComponentArtifactEditForRead() {
 		return EARArtifactEdit.getEARArtifactEditForRead(component.getProject());
 	}
     
     /**
      * The references for J2EE module deployment are only those child modules of EARs or web modules
      */
     @Override
 	protected IVirtualReference[] getReferences(IVirtualComponent aComponent) {
     	if (aComponent == null || aComponent.isBinary()) {
     		return new IVirtualReference[] {};
     	} else if (JavaEEProjectUtilities.isDynamicWebProject(aComponent.getProject())) {
     		return getWebLibModules((J2EEModuleVirtualComponent)aComponent);
     	} else if (JavaEEProjectUtilities.isEARProject(aComponent.getProject())) {
     		return super.getReferences(aComponent);
     	} else {
     		return new IVirtualReference[] {};
     	}
     }
     
     /**
 	 * This method will return the list of dependent modules which are utility jars in the web lib
 	 * folder of the deployed path of the module. It will not return null.
 	 * 
 	 * @return array of the web library dependent modules
 	 */
 	private IVirtualReference[] getWebLibModules(J2EEModuleVirtualComponent comp) {
 		List result = new ArrayList();
 		IVirtualReference[] refComponents = comp.getNonManifestReferences();
 		// Check the deployed path to make sure it has a lib parent folder and matchs the web.xml
 		// base path
 		for (int i = 0; i < refComponents.length; i++) {
 			if (refComponents[i].getRuntimePath().equals(WEBLIB))
 				result.add(refComponents[i]);
 		}
 		return (IVirtualReference[]) result.toArray(new IVirtualReference[result.size()]);
 	}
 	
 	/*
 	 * Add any classpath component dependencies from this component
 	 */
 	private void addReferencedComponentClasspathDependencies(final IVirtualComponent component, final boolean web) {
 		final IVirtualReference[] refs = component.getReferences();
 		final Set absolutePaths = new HashSet();
 		for (int i = 0; i < refs.length; i++) {
 			final IVirtualReference reference = refs[i];
 			final IPath runtimePath = reference.getRuntimePath();
 			final IVirtualComponent referencedComponent = reference.getReferencedComponent();
 			
 			// if we are adding to a web project, only process references with the /WEB-INF/lib runtime path
 			if (web && !runtimePath.equals(WEBLIB)) {
 				continue;
 			}
 
 			// if the reference cannot export dependencies, skip
 			if (!canExportClasspathComponentDependencies(referencedComponent)) {
 				continue;
 			}
 			
 			if (!referencedComponent.isBinary() && referencedComponent instanceof J2EEModuleVirtualComponent) {
 				final IVirtualReference[] cpRefs = ((J2EEModuleVirtualComponent) referencedComponent).getJavaClasspathReferences();
 				for (int j = 0; j < cpRefs.length; j++) {
 					final IVirtualReference cpRef = cpRefs[j];
 					IPath cpRefRuntimePath = cpRef.getRuntimePath();
 
 					if (cpRef.getReferencedComponent() instanceof ClasspathDependencyVirtualComponent) {
 						// want to avoid adding dups
 						ClasspathDependencyVirtualComponent cpComp = (ClasspathDependencyVirtualComponent) cpRef.getReferencedComponent();
 						// don't want to process class folder refs here
 						if (cpComp.isClassFolder()) {
 							continue;
 						}
 						if (cpRefRuntimePath.equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH)) {
 							// runtime path within deployed app will be runtime path of parent component
 							cpRefRuntimePath = runtimePath;
 						} else {
 							//https://bugs.eclipse.org/bugs/show_bug.cgi?id=247090
 							//if path isn't ../, it shouldn't be added here
 							continue;
 						}
 						final IPath absolutePath = ClasspathDependencyUtil.getClasspathVirtualReferenceLocation(cpRef);
 						if (absolutePaths.contains(absolutePath)) {
 							// have already added a member for this archive
 							continue;
 						}
 						addUtilMember(component, cpRef, cpRefRuntimePath);
 						absolutePaths.add(absolutePath);
 					}
 				}
 			}
 		}
 	}
 	
 	private boolean canExportClasspathComponentDependencies(IVirtualComponent component) {
 		final IProject project = component.getProject();
 		// check for valid project type
 		if (JavaEEProjectUtilities.isEJBProject(project) 
 				|| JavaEEProjectUtilities.isDynamicWebProject(project)
 				|| JavaEEProjectUtilities.isJCAProject(project)
     			|| JavaEEProjectUtilities.isUtilityProject(project)) {
 			return true;
 		}
 		return false;
 	}
 	
 	private void saveClasspathDependencyURIs(IVirtualComponent component) {
 		if (!component.isBinary() && component instanceof J2EEModuleVirtualComponent) {
 			final IVirtualReference[] cpRefs = ((J2EEModuleVirtualComponent) component).getJavaClasspathReferences();
 			for (int j = 0; j < cpRefs.length; j++) {
 				final IVirtualReference cpRef = cpRefs[j];
 				// if we are adding to an EAR project, only process references with the root mapping
 				if (!cpRef.getRuntimePath().equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_CONTAINER_PATH)) {
 					// fails the runtime path test
 					continue;
 				}
 				if (cpRef.getReferencedComponent() instanceof VirtualArchiveComponent) {
 					classpathComponentDependencyURIs.add(cpRef.getArchiveName());
 				}
 			}
 		}
 	}
 	
 	private void addClassFolderDependencies(final IVirtualComponent component) throws CoreException {
 		if (!component.isBinary() && component instanceof J2EEModuleVirtualComponent) {
 			final IVirtualReference[] cpRefs = ((J2EEModuleVirtualComponent) component).getJavaClasspathReferences();
 			for (int i = 0; i < cpRefs.length; i++) {
 				final IVirtualReference cpRef = cpRefs[i];
 				final IPath runtimePath = cpRef.getRuntimePath();
 				final IVirtualComponent comp = cpRef.getReferencedComponent();
 				if (comp instanceof ClasspathDependencyVirtualComponent) {
 					final ClasspathDependencyVirtualComponent cpComp = (ClasspathDependencyVirtualComponent) comp;
 					if (cpComp.isClassFolder()) {
 						IPath targetPath = null;
 						if (runtimePath.equals(IClasspathDependencyConstants.RUNTIME_MAPPING_INTO_COMPONENT_PATH)) {
 							targetPath = Path.EMPTY;
 						} else if (runtimePath.equals(IClasspathDependencyConstants.WEB_INF_CLASSES_PATH)) {
 							targetPath =  WEB_CLASSES_PATH;
 						} else {
 							continue;
 						}
 						final IContainer container = cpComp.getClassFolder();
 						final IModuleResource[] mr = getMembers(container, targetPath, targetPath, new IContainer[]{container});
 						for (int j = 0; j < mr.length; j++) {
 							members.add(mr[j]);
 						}						
 					}
 				}
 			}
 		}
 	}
 	
     /*
      * If the resource to check is a file then this method will return true if the file is linked. If the resource to
      * check is a folder then this method will return true if it, any of its sub directories, or any file contained
      * with-in this directory of any of it's sub directories are linked. Otherwise false is returned.
      */
     private boolean hasLinkedContent(final IResource resourceToCheck) throws CoreException {
 
     	if ((resourceToCheck != null) && resourceToCheck.isAccessible()) {
     		// skip non-accessible files
     		if (resourceToCheck.isLinked()) {
     			return true;
     		}
 			switch (resourceToCheck.getType()) {
 				case IResource.FOLDER:
 					// recursively check sub directory contents
 					final IResource[] subDirContents = ((IFolder) resourceToCheck).members();
 					for (int i = 0; i < subDirContents.length; i++) {
 						if (hasLinkedContent(subDirContents[i])) {
 							return true;
 						}
 					}
 					break;
 				case IResource.FILE:
 					return resourceToCheck.isLinked();
 				default:
 					// skip as we only care about files and folders
 					break;
 			}
     	}
     	return false;
     }
 
     /*
      * This method returns true if the root folders of this component have any linked resources (folder or file);
      * Otherwise false is returned.
      */
     private boolean rootFoldersHaveLinkedContent() {
 
     	if (this.component != null) {
     		final IContainer[] rootFolders = this.component.getRootFolder().getUnderlyingFolders();
     		for (int i = 0; i < rootFolders.length; i++) {
     			try {
     				boolean hasLinkedContent = this.hasLinkedContent(rootFolders[i]);
     				if (hasLinkedContent) {
     					return true;
     				}
     			}
     			catch (CoreException coreEx) {
     				J2EEPlugin.logError(coreEx);
     			}
     		}
     	}
     	return false;
     }
 	
 	/**
 	 * Returns <code>true</code> if this module has a simple structure based on a
 	 * single root folder, and <code>false</code> otherwise.
 	 * <p>
 	 * In a single root structure, all files that are contained within the root folder
 	 * are part of the module, and are already in the correct module structure. No
 	 * module resources exist outside of this single folder.
 	 * 
 	 * For J2EE, this method will check if the project is already in J2EE spec standard output form.  
 	 * The project must follow certain rules, but in general, the project's content roots must be source folders
 	 * and the output folder must also be the the content root folder.
 	 * </p>
 	 * 
 	 * @return <code>true</code> if this module has a single root structure, and
 	 *    <code>false</code> otherwise
 	 */
 	@Override
 	public boolean isSingleRootStructure() {
 		return isForceSingleRoot() || getSingleRoot() != null;
 	}
 	
 	/**
 	 * Will attempt to return the IContainer that counts as the "single-root".
 	 * If this module does not qualify as a "single-root" module, this
 	 * method will return null. Otherwise it will return an IContainer
 	 * that may be used as the single root container. 
 	 * 
 	 * @return
 	 */
 	
 	public IContainer getSingleRoot() {
 		StructureEdit edit = null;
 		try {
 			edit = StructureEdit.getStructureEditForRead(getProject());
 			if (edit == null || edit.getComponent() == null)
 				return null;
 			
 			// 229650 - check to see if the property 'useSingleRoot' is defined. 
 			// The force status will be checked until a suitable root folder is found
 			boolean forceSingleRoot = isForceSingleRoot(edit); 
 			
 			// if there are any consumed references, this is not single root
 			if( hasConsumableReferences(component) && !forceSingleRoot)
 				return null;
 			
 			// if there are any linked resources in the workspace under this folder,
 			// then this is not a single root module
 			if (rootFoldersHaveLinkedContent() && !forceSingleRoot)
 				return null;
 
 			// Always return false for EARs so that members for EAR 
 			// are always calculated and j2ee modules are filtered out
 			if (JavaEEProjectUtilities.isEARProject(getProject()) && !forceSingleRoot)
 				return null;
 			
 			List resourceMaps = edit.getComponent().getResources();
 
 			// No mappings at all... undefined?  false for now
 			if (resourceMaps.size()<1)
 				return null;
 			
 			// One mapping, and something maps to root. That's a good sign
 			if( resourceMaps.size() == 1 ) {
 				ComponentResource mapping = (ComponentResource)resourceMaps.get(0); 
 				if( isRootMapping(mapping))
 					return getOutputContainerIfExists(mapping.getSourcePath());
 				// One mapping, but doesn't map to root... so there's no root to point to
 				if( !forceSingleRoot)
 					return null;
 			}
 			
 			// There are several mappings to root. 
 			// If more than one map to root, not single root
 			if( countExistingRootMappings(resourceMaps) > 1 )
 				return !forceSingleRoot ? null :  
 					getOutputContainerIfExists(getFirstExistingRootPath(resourceMaps));
 			
 			// if ALL are source folders, 
 			if( countSourceMappings(resourceMaps) == resourceMaps.size()) {
 				// then we check that they all output to the same place
 				if (getJavaOutputFolders().length==1) {
 					// Currently, we claim single-rooted when all source folders 
 					// have the same output folder.  However, we know this is not 
 					// true because it cannot contain non Java files unless it is 
 					// the only source folder. But, fixing at this time would break 
 					// all current users.
 					return getJavaOutputFolders()[0];
 				}
 				if( !forceSingleRoot)
 					return null;
 			}
 			
 			// Not all of them are source mappings, but there's only one root mapping
 			// Lets find our root mapping container
 			IContainer rootContainer = component.getRootFolder().getUnderlyingFolder();
 			IPath projectRelative = component.getRootFolder().getProjectRelativePath();
 			if( forceSingleRoot)
 				return getOutputContainerIfExists(projectRelative);
 			
 			// Lets check the underlying folders of all OTHER mappings, which are all source folders,
 			// and check its output folder against this root mapping. If the output is to  
 			// somewhere under the root mapping, then it's single root
 			IContainer[] outputFolders = getJavaOutputFolders();
 			IPath rootContainerProjectRelative = rootContainer.getProjectRelativePath(); 
 			for( int i = 0; i < outputFolders.length; i++ ) {
 				if( !rootContainerProjectRelative.isPrefixOf(outputFolders[i].getProjectRelativePath())) {
 					return null;
 				}
 			}
 
 			// All outputs go somewhere under the root folder, or the root folder doesn't exist (EH?)
 			return getOutputContainerIfExists(projectRelative);
 		} finally {
 			if (edit !=null)
 				edit.dispose();
 		}
 	}
 	
 	protected boolean isForceSingleRoot() {
 		StructureEdit edit =  StructureEdit.getStructureEditForRead(getProject());
 		return isForceSingleRoot(edit);
 	}
 	
 	protected boolean isForceSingleRoot(StructureEdit edit) {
 		WorkbenchComponent wbComp = edit.getComponent();
 		final List componentProperties = wbComp.getProperties();
 		if (componentProperties != null) {
 			final Iterator componentPropertiesIterator = componentProperties.iterator();
 			while (componentPropertiesIterator.hasNext()) {
 				Property wbProperty = (Property) componentPropertiesIterator.next();
 				if (J2EEFlexProjDeployable.USE_SINGLE_ROOT_PROPERTY.equals(wbProperty.getName())) {
 					boolean useSingleRoot = Boolean.valueOf(wbProperty.getValue()).booleanValue();
 					if (useSingleRoot) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * This method will return the container related to the given path.
 	 * If the container is a source path, this method will return its output
 	 * folder.
 	 * If the container is not a source path, the container itself will be returned
 	 * @param projectRelative
 	 * @return
 	 */
 	protected IContainer getOutputContainerIfExists(IPath projectRelative) {
 		IContainer container = (IContainer)getProject().findMember(projectRelative); 
 		if( !isSourceContainer(projectRelative))
 			return container;
 		return getOutputContainer(getSourceContainer(container));
 	}
 	
 	private int countExistingRootMappings(List resourceMaps) {
 		int count = 0;
 		ComponentResource cr = null;
 		for (int i=0; i<resourceMaps.size(); i++) {
 			cr = (ComponentResource)resourceMaps.get(i);
 			if(isRootMapping(cr) && exists(cr))
 				count++;
 		}
 		return count;
 	}
 	
 	private IPath getFirstExistingRootPath(List resourceMaps) {
 		ComponentResource cr = null;
 		for (int i=0; i<resourceMaps.size(); i++) {
 			cr = (ComponentResource)resourceMaps.get(i);
 			if(isRootMapping(cr) && exists(cr))
 				return cr.getSourcePath();
 		}
 		return null;
 	}
 	
 	private boolean exists(ComponentResource cr) {
 		IContainer container = (IContainer)getProject().findMember(cr.getSourcePath());
 		return container != null && container.exists();
 	}
 	
 	private boolean isRootMapping(ComponentResource mapping) {
 		// Verify it maps to "/" for the content root
 		if (mapping != null && !mapping.getRuntimePath().equals(Path.ROOT))
 			return false;
 		return true;
 	}
 	
 	private int countSourceMappings(List resourceMaps) {
 		int count = 0;
 		for (int i=0; i<resourceMaps.size(); i++) {
 			if(isSourceContainer((ComponentResource)resourceMaps.get(i)))
 				count++;
 		}
 		return count;
 	}
 
 	private boolean isSourceContainer(ComponentResource mapping) {
 		return isSourceContainer(mapping.getSourcePath());
 	}
 	
 	private boolean isSourceContainer(IPath projectRelative) {
 		IResource sourceResource = getProject().findMember(projectRelative);
 		if(sourceResource != null && sourceResource.exists()){
 			IPath sourcePath = getProject().getFullPath().append(projectRelative);
 			if (!isSourceContainer2(sourcePath))
 				return false;
 		}
 		return true;
 	}
 
 	private boolean isSourceContainer2(IPath fullPath) {
 		IPackageFragmentRoot[] srcContainers = getSourceContainers();
 		for (int i=0; i<srcContainers.length; i++) {
 			if (srcContainers[i].getPath().equals(fullPath))
 				return true;
 		}
 		return false;
 	}
 
 
 	/**
 	 * This method is added so that force-singleroot use case can behave properly. 
 	 * 
 	 * @return array of ModuleResources
 	 * @throws CoreException
 	 */
 	private IModuleResource[] getOptimizedMembers() throws CoreException {
 		IContainer root = getSingleRoot();
 		IModuleResource[] resources = getModuleResources(Path.EMPTY, root);
 		for( int i = 0; i < resources.length; i++ )
 			members.add(resources[i]);
 		return (IModuleResource[]) members.toArray(new IModuleResource[members.size()]);
 	}
 	
 	/**
 	 * This method will return from cache or add to cache whether or not an output container
 	 * is mapped in the virtual component.
 	 * 
 	 * @param outputContainer
 	 * @return if output container is mapped
 	 */
 	private boolean isOutputContainerMapped(IContainer outputContainer) {
 		if (outputContainer == null)
 			return false;
 		
 		HashMap outputMaps = getCachedOutputMappings();
 		Boolean result = (Boolean) outputMaps.get(outputContainer);
 		if (result == null) {
 			// If there are any component resources for the container, we know it is mapped
 			if (ComponentCore.createResources(outputContainer).length > 0)
 				result = Boolean.TRUE;	
 			// Otherwise it is not mapped
 			else
 				result = Boolean.FALSE;
 			// Cache the result in the map for this output container
 			outputMaps.put(outputContainer, result);
 		}
 		return result.booleanValue();
 	}
 	
 	/**
 	 * Lazy initialize the cached output mappings
 	 * @return HashMap
 	 */
 	private HashMap getCachedOutputMappings() {
 		if (cachedOutputMappings==null)
 			cachedOutputMappings = new HashMap();
 		return cachedOutputMappings;
 	}
 	
 	/**
 	 * Lazy initialize the cached source - output pairings
 	 * @return HashMap
 	 */
 	private HashMap getCachedSourceOutputPairs() {
 		if (cachedSourceOutputPairs==null)
 			cachedSourceOutputPairs = new HashMap();
 		return cachedSourceOutputPairs;
 	}
 	
 	/**
 	 * This file should be added to the members list from the virtual component maps only if:
 	 * a) it is not in a source folder
 	 * b) it is in a source folder, and the corresponding output folder is a mapped component resource
 	 * 
 	 * @return boolean should file be added to members
 	 */
 	@Override
 	protected boolean shouldAddComponentFile(IFile file) {
 		IPackageFragmentRoot sourceContainer = getSourceContainer(file);
 		// If the file is not in a source container, return true
 		if (sourceContainer==null) {
 			return true;
 		// Else if it is a source container and the output container is mapped in the component, return true
 		// Otherwise, return false.
 		}
 		IContainer outputContainer = getOutputContainer(sourceContainer);
 		return outputContainer!=null && isOutputContainerMapped(outputContainer);		
 	}
 }
