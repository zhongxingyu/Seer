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
 package org.eclipse.jst.j2ee.internal.deployables;
 
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Properties;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.internal.EjbModuleExtensionHelper;
 import org.eclipse.jst.j2ee.internal.IEJBModelExtenderManager;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.server.core.IApplicationClientModule;
 import org.eclipse.jst.server.core.IConnectorModule;
 import org.eclipse.jst.server.core.IEJBModule;
 import org.eclipse.jst.server.core.IEnterpriseApplication;
 import org.eclipse.jst.server.core.IJ2EEModule;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualContainer;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.internal.ModuleFile;
 import org.eclipse.wst.server.core.internal.ModuleFolder;
 import org.eclipse.wst.server.core.model.IModuleFolder;
 import org.eclipse.wst.server.core.model.IModuleResource;
 import org.eclipse.wst.server.core.util.ProjectModule;
 /**
  * J2EE module superclass.
  */
 public class J2EEFlexProjDeployable extends ProjectModule implements IJ2EEModule, IEnterpriseApplication, IApplicationClientModule, IConnectorModule, IEJBModule, IWebModule {
     protected IVirtualComponent component = null;
     private boolean outputMembersAdded = false;
     private List members = new ArrayList();
     private static IPath WEB_INF_PATH = new Path(J2EEConstants.WEB_INF);
     private static IPath WEB_INF_CLASSES_PATH = new Path(J2EEConstants.WEB_INF_CLASSES);
 
 	/**
 	 * Constructor for J2EEFlexProjDeployable.
 	 * 
 	 * @param project
 	 * @param aComponent
 	 */
 	public J2EEFlexProjDeployable(IProject project, IVirtualComponent aComponent) {
 		super(project);
 		component = aComponent;
 	}
 
 	/**
 	 * @see org.eclipse.jst.server.core.IJ2EEModule#isBinary()
 	 */
 	public boolean isBinary() {
 		return false;
 	}
 
 	/**
 	 * Returns the root folders for the resources in this module.
 	 * 
 	 * @return a possibly-empty array of resource folders
 	 */
 	public IContainer[] getResourceFolders() {
 		IVirtualComponent vc = ComponentCore.createComponent(getProject());
 		if (vc != null) {
 			IVirtualFolder vFolder = vc.getRootFolder();
 			if (vFolder != null)
 				return vFolder.getUnderlyingFolders();
 		}
 		
 		return null;
 	}
 
 	/**
 	 * Returns the root folders containing Java output in this module.
 	 * 
 	 * @return a possibly-empty array of Java output folders
 	 */
 	public IContainer[] getJavaOutputFolders() {
 		IVirtualComponent vc = ComponentCore.createComponent(getProject());
 		if (vc == null)
 			return new IContainer[0];
 		
 		return J2EEProjectUtilities.getOutputContainers(getProject());
 	}
 
 	private void getJavaResources() throws CoreException {
 		IContainer[] outputFolders = J2EEProjectUtilities.getOutputContainers(component.getProject());
     	for (int i=0; i<outputFolders.length; i++) {
     		if (outputFolders[i]==null || !outputFolders[i].exists())
     			continue;
     		if (J2EEProjectUtilities.isDynamicWebProject(component.getProject())) {
     			ModuleFolder webInf = (ModuleFolder) getExistingModuleResource(members, WEB_INF_PATH);
     			if (webInf == null) {
     				webInf = new ModuleFolder(outputFolders[i],J2EEConstants.WEB_INF,Path.EMPTY);
     				webInf.setMembers(new IModuleResource[]{});
     				members.add(webInf);
     			}
     			ModuleFolder classes = (ModuleFolder) getExistingModuleResource(members, WEB_INF_CLASSES_PATH);
     			if (classes == null) {
     				classes = new ModuleFolder(outputFolders[i],"classes",new Path(J2EEConstants.WEB_INF)); //$NON-NLS-1$
     				classes.setMembers(new IModuleResource[]{});
     			}
     			List currentMembers = new ArrayList();
     			List membersList = Arrays.asList(webInf.members());
     			currentMembers.addAll(membersList);
     			currentMembers.add(classes);
     			webInf.setMembers((IModuleResource[])currentMembers.toArray(new IModuleResource[currentMembers.size()]));
     			IModuleResource[] javaResources = getModuleResources(WEB_INF_CLASSES_PATH,outputFolders[i]);
     			currentMembers.clear();
     			membersList = Arrays.asList(classes.members());
     			currentMembers.addAll(membersList);
     			currentMembers.addAll(Arrays.asList(javaResources));
     			classes.setMembers((IModuleResource[])currentMembers.toArray(new IModuleResource[currentMembers.size()]));
     		}
     		else {
     			IModuleResource[] javaResources = getModuleResources(Path.EMPTY,outputFolders[i]);
     			members.addAll(Arrays.asList(javaResources));
     		}
     	}
 	}
 
 	/**
 	 * Return the module resources for both the component meta folder and the java output folder
 	 * 
 	 * @return IModuleResource[]
 	 * @throws CoreException
 	 */
     public IModuleResource[] members() throws CoreException {
         outputMembersAdded = false;
         members.clear();
         if (component == null)
         	return new IModuleResource[] {};
         try {
         	// Retrieve the java output folder files
 	    	getJavaResources();
 	    	
         	// Retrieve the module resources from the virtual component's root folder
 	    	IVirtualFolder componentRoot = component.getRootFolder();
 	    	if (componentRoot!=null && componentRoot.exists()) {
 	    		IModuleResource[] rootResources = getModuleResources(Path.EMPTY, componentRoot);
 	    		members.addAll(Arrays.asList(rootResources));
 	    	}	
         } catch (CoreException ce) {
         	throw ce;
         }
         //Return the combined array of the meta resources and java class files
         return (IModuleResource[]) members.toArray(new IModuleResource[members.size()]);
     }
     
     private IModuleResource getExistingModuleResource(List moduleResources, IPath path) {
     	IModuleResource result = null;
     	// If the list is empty, return null
     	if (moduleResources==null || moduleResources.isEmpty())
     		return null;
     	// Otherwise recursively check to see if given resource matches current resource or if it is a child
     	int i=0;
     	do {
 	    	IModuleResource moduleResource = (IModuleResource) moduleResources.get(i);
 	    		if (moduleResource.getModuleRelativePath().append(moduleResource.getName()).equals(path))
 	    			result = moduleResource;
 	    		// if it is a folder, check its children for the resource path
 	    		else if (moduleResource instanceof IModuleFolder) {
 	    			result = getExistingModuleResource(Arrays.asList(((IModuleFolder)moduleResource).members()),path);
 	    		}
 	    		i++;
     	} while (result == null && i<moduleResources.size() );
     	return result;
     }
     
     /**
      * Helper method to check a given list of resource trees for the given resource.  It will return the
      * already existing instance of the module Resource if found, or it will return null.
      * 
      * @param moduleResources
      * @param aModuleResource
      * @return existing module resource or null
      */
     private IModuleResource getExistingModuleResource(List moduleResources, IModuleResource aModuleResource) {
     	IModuleResource result = null;
     	// If the list is empty, return null
     	if (moduleResources==null || moduleResources.isEmpty())
     		return null;
     	// Otherwise recursively check to see if given resource matches current resource or if it is a child
     	int i=0;
     	do {
 	    	IModuleResource moduleResource = (IModuleResource) moduleResources.get(i);
 	    		if (moduleResource.equals(aModuleResource))
 	    			result = moduleResource;
 	    		// if it is a folder, check its children for the resource path
 	    		else if (moduleResource instanceof IModuleFolder) {
 	    			result = getExistingModuleResource(Arrays.asList(((IModuleFolder)moduleResource).members()),aModuleResource);
 	    		}
 	    		i++;
     	} while (result == null && i<moduleResources.size() );
     	return result;
     }
     
     /**
      * This is a helper method for J2EE deployables to gather the module resources for a J2EE module
      * using the virtual component API.  it ensures the non java meta files are represented by
      * appropriate module resource files.
      * 
      * @param path
      * @param container
      * @return array of IModuleResources
      * @throws CoreException
      */
     protected IModuleResource[] getModuleResources(IPath path, IVirtualContainer container) throws CoreException {
     	List result = new ArrayList();
         IVirtualResource[] resources = container.members();
         if (resources != null) {
             int size = resources.length;
             for (int i = 0; i < size; i++) {
                 IVirtualResource resource = resources[i];
                 // If the resource is a folder, we need to add it and recursively add its members
                 if (resource!=null && resource.getType()==IVirtualResource.FOLDER) 
                 	addFolderModuleResources(path,container,resource,result);
                 // If the resource is a file, then make sure it does not exist in module resources, then add
                 else if (resource!=null && resource.getType()==IVirtualResource.FILE)
                 	addFileModuleResource(path,container,resource,result);
             }
         }
         // return the result list as a ModuleResource array
         return (IModuleResource[]) result.toArray(new IModuleResource[result.size()]);
     }
     
     /**
      * Helper method to add module resources for the given folder and its children
      * 
      * @param path
      * @param container
      * @param resource
      * @param result
      * @throws CoreException
      */
     private void addFolderModuleResources(IPath path, IVirtualContainer container, IVirtualResource resource, List result) throws CoreException {
         IVirtualContainer container2 = (IVirtualContainer) resource;
         if (container2 != null && container2.exists()) {
         	ModuleFolder mf = new ModuleFolder((IContainer)container.getUnderlyingResource(), container2.getName(), path);
         	//If the folder does not exist yet in module resources, let's create it
         	if (getExistingModuleResource(members,mf)==null && getExistingModuleResource(result,mf)==null) {
         		mf.setMembers(getModuleResources(path.append(container2.getName()), container2));
                 result.add(mf);
         	}
         	// if folder exists, we need to retreive the existing module folder and update
         	else {
         		// first try to retrieve from cached list
         		ModuleFolder matchingMf = (ModuleFolder) getExistingModuleResource(members,mf);
         		// if still null, retrieve from current result set which has not been added to cache
         		if (matchingMf==null)
         			matchingMf = (ModuleFolder) getExistingModuleResource(result,mf);
         		// update the members on the existing module resource to have the new children 
         		List currentMembers = new ArrayList();
         		List membersList = Arrays.asList(matchingMf.members());
             	List newMembers = Arrays.asList(getModuleResources(path.append(container2.getName()), container2));
             	currentMembers.addAll(membersList);
             	currentMembers.addAll(newMembers);
             	matchingMf.setMembers((IModuleResource[])currentMembers.toArray(new IModuleResource[currentMembers.size()]));
         	}
         }
     }
     
     /**
      * Helper method to add a module resource for the given file
      * 
      * @param path
      * @param container
      * @param resource
      * @param result
      */
     private void addFileModuleResource(IPath path, IVirtualContainer container, IVirtualResource resource, List result) {
     	IFile file = null;
     	String runtimePath = resource.getRuntimePath().toString();
     	// temporary hack because virtual component API returns .java rather than .class files
     	if (runtimePath.endsWith(".java")) //$NON-NLS-1$
     		return;
     	file = (IFile) resource.getUnderlyingResource();
     	if (file == null)
     		return;
         ModuleFile moduleFile = new ModuleFile(file, file.getName(), path, file.getModificationStamp());
         // we have to be sure its not in cache and current result group waiting to be added to cache
         if (file != null && file.exists() && getExistingModuleResource(result,moduleFile)==null && getExistingModuleResource(members,moduleFile)==null)
             result.add(moduleFile);
     }
     
 //    private IFile findClassFileInOutput(IContainer container, String className) {
 //    	IFile result = null;
 //    	result = (IFile) container.findMember(className);
 //    	if (result == null) {
 //	    	try {
 //	    		IResource[] currentMembers = container.members();
 //	    		for (int i=0; i<currentMembers.length; i++) {
 //	    			if (currentMembers[i].getType()==IResource.FOLDER) {
 //	    				result = findClassFileInOutput((IContainer)currentMembers[i],className);
 //	    				if (result != null)
 //	    					break;
 //	    			}
 //	    		}
 //	    	} catch (Exception e) {}
 //    	}
 //    	return result;
 //    }
     
     protected IContainer getContainerResource(IResource resource){
         if(resource instanceof IFolder) {
             IJavaElement element =JavaCore.create((IFolder)resource);
             if (element != null && element.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT)
                 //means this is a source folder add output once and only once
                 return addOutputFolderIfNecessary(element);
         }
         return (IContainer)resource;
     }
     
     private IContainer addOutputFolderIfNecessary(IJavaElement je) {
         if(!outputMembersAdded){
             outputMembersAdded = true;
             IPath javaOutputPath = null;
             try {
                 if (je.getElementType() == IJavaElement.PACKAGE_FRAGMENT_ROOT) {
                     IPackageFragmentRoot packRoot = (IPackageFragmentRoot) je;
                     if (packRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
                         IClasspathEntry ce = packRoot.getRawClasspathEntry();
                         javaOutputPath = ce.getOutputLocation();
                         javaOutputPath = javaOutputPath.removeFirstSegments(1);
                         return getProject().getFolder(javaOutputPath);
                     }
                 }
             } catch (JavaModelException e) {
                 Logger.getLogger().log(e.getMessage());
             }
         }
         return null;
     }
 
     /**
      * Returns the classpath as a list of absolute IPaths.
      * 
      * @return an array of paths
      */
     public IPath[] getClasspath() {
 		List paths = new ArrayList();
         IJavaProject proj = JemProjectUtilities.getJavaProject(getProject());
         URL[] urls = JemProjectUtilities.getClasspathAsURLArray(proj);
 		for (int i = 0; i < urls.length; i++) {
 			URL url = urls[i];
 			paths.add(Path.fromOSString(url.getPath()));
 		}
         return  (IPath[]) paths.toArray(new IPath[paths.size()]);
     }
     
     public String getJNDIName(String ejbName) {
     	if (!J2EEProjectUtilities.isEJBProject(component.getProject()))
     		return null;
 		EjbModuleExtensionHelper modHelper = null;
 		EJBJar jar = null;
 		ArtifactEdit ejbEdit = null;
 		try {
 			ejbEdit = ComponentUtilities.getArtifactEditForRead(component);
 			if (ejbEdit != null) {
 				jar = (EJBJar) ejbEdit.getContentModelRoot();
 				modHelper = IEJBModelExtenderManager.INSTANCE.getEJBModuleExtension(null);
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			if (ejbEdit != null)
 				ejbEdit.dispose();
 		}
 		return modHelper == null ? null : modHelper.getJNDIName(jar, jar.getEnterpriseBeanNamed(ejbName));
 	}
 
     /**
      * Returns the child modules of this module.
      * 
      * @return org.eclipse.wst.server.core.model.IModule[]
      */
     public IModule[] getChildModules() {
         return getModules();
     }
 
     public IModule[] getModules() {
 		List modules = new ArrayList();
     	IVirtualReference[] components = component.getReferences();
     	for (int i = 0; i < components.length; i++) {
 			IVirtualReference reference = components[i];
 			IVirtualComponent virtualComp = reference.getReferencedComponent();
			Object module = FlexibleProjectServerUtil.getModule(virtualComp.getProject());
			if (module != null)
				modules.add(module);
 		}
       return (IModule[]) modules.toArray(new IModule[modules.size()]);
 	}
 
     public String getURI(IModule module) {
     	IVirtualComponent comp = ComponentCore.createComponent(module.getProject());
     	String aURI = null;
     	if (J2EEProjectUtilities.isEARProject(comp.getProject())) {
 			EARArtifactEdit earEdit = null;
 			try {
 				earEdit = EARArtifactEdit.getEARArtifactEditForRead(component);
 				if (earEdit != null)
 					aURI = earEdit.getModuleURI(comp);
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				if (earEdit != null)
 					earEdit.dispose();
 			}
     	}
     	else if (J2EEProjectUtilities.isDynamicWebProject(comp.getProject())) {
     		if (!comp.isBinary()) {
         		IVirtualReference ref = component.getReference(comp.getName());
         		aURI = ref.getRuntimePath().append(comp.getName()+IJ2EEModuleConstants.WAR_EXT).toString();
         	}
     	} else if (J2EEProjectUtilities.isEJBProject(comp.getProject()) || J2EEProjectUtilities.isApplicationClientProject(comp.getProject())) {
     		if (!comp.isBinary()) {
         		IVirtualReference ref = component.getReference(comp.getName());
         		aURI = ref.getRuntimePath().append(comp.getName()+IJ2EEModuleConstants.JAR_EXT).toString();
         	}
     	} else if (J2EEProjectUtilities.isJCAProject(comp.getProject())) {
     		if (!comp.isBinary()) {
         		IVirtualReference ref = component.getReference(comp.getName());
         		aURI = ref.getRuntimePath().append(comp.getName()+IJ2EEModuleConstants.RAR_EXT).toString();
         	}
     	}
     	
     	if (aURI !=null && aURI.length()>1 && aURI.startsWith("/")) //$NON-NLS-1$
     		aURI = aURI.substring(1);
     	return aURI;
 	}
     
     public String getContextRoot() {
 		Properties props = component.getMetaProperties();
 		if(props.containsKey(J2EEConstants.CONTEXTROOT))
 			return props.getProperty(J2EEConstants.CONTEXTROOT);
 	    return component.getName();
     }
 }
