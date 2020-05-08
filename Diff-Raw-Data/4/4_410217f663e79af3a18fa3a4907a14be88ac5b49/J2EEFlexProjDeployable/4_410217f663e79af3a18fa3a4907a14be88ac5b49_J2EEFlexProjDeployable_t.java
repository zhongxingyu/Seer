 /*******************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others.
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
 import org.eclipse.jst.common.componentcore.util.ComponentUtilities;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.internal.EjbModuleExtensionHelper;
 import org.eclipse.jst.j2ee.internal.IEJBModelExtenderManager;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.J2EEVersionUtil;
 import org.eclipse.jst.j2ee.internal.plugin.IJ2EEModuleConstants;
 import org.eclipse.jst.server.core.IApplicationClientModule;
 import org.eclipse.jst.server.core.IConnectorModule;
 import org.eclipse.jst.server.core.IEJBModule;
 import org.eclipse.jst.server.core.IJ2EEModule;
 import org.eclipse.jst.server.core.IWebModule;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualContainer;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleType;
 import org.eclipse.wst.server.core.internal.ModuleFile;
 import org.eclipse.wst.server.core.internal.ModuleFolder;
 import org.eclipse.wst.server.core.model.IModuleResource;
 import org.eclipse.wst.server.core.util.ProjectModule;
 
 /**
  * J2EE deployable superclass.
  */
 public class J2EEFlexProjDeployable extends ProjectModule implements IJ2EEModule, IApplicationClientModule, IConnectorModule, IEJBModule, IWebModule {
 	private String factoryId;
     protected IVirtualComponent component = null;
     private boolean outputMembersAdded = false;
     private List members = new ArrayList();
 
 	/**
 	 * Constructor for J2EEFlexProjDeployable.
 	 * 
 	 * @param project
 	 */
 	public J2EEFlexProjDeployable(IProject project, String aFactoryId, IVirtualComponent aComponent) {
 		super(project);
 		factoryId = aFactoryId;
 		component = aComponent;
 	}
 
 	public String getJ2EESpecificationVersion() {
 		String type = component.getComponentTypeId();
 		if (IModuleConstants.JST_EAR_MODULE.equals(type))
 			return component.getVersion();
 		else if (IModuleConstants.JST_APPCLIENT_MODULE.equals(type))
 			return J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertAppClientVersionStringToJ2EEVersionID(component.getVersion()));
 		else if (IModuleConstants.JST_CONNECTOR_MODULE.equals(type))
 			return J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertConnectorVersionStringToJ2EEVersionID(component.getVersion()));
 		else if (IModuleConstants.JST_EJB_MODULE.equals(type))
 			return J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertEJBVersionStringToJ2EEVersionID(component.getVersion()));
 		else if (IModuleConstants.JST_WEB_MODULE.equals(type))
 			return J2EEVersionUtil.convertVersionIntToString(J2EEVersionUtil.convertWebVersionStringToJ2EEVersionID(component.getVersion()));
 		else
 			return component.getVersion();
 	}
 	
 	/*
 	 * @see IModule#getFactoryId()
 	 */
 	public String getFactoryId() {
 		return factoryId;
 	}
 
 	/**
 	 * @see com.ibm.etools.server.j2ee.IJ2EEModule#isBinary()
 	 */
 	public boolean isBinary() {
 		return false;
 	}
 
 	public String getModuleTypeName() {
 		return getName();
 	}
 
 	public String getModuleTypeVersion() {
 		return getVersion();
 	}
 
 	public String getVersion() {
		return getJ2EESpecificationVersion();
 	}
 
 	public String getType() {
 		String type = component.getComponentTypeId();
 		if (IModuleConstants.JST_EAR_MODULE.equals(type))
 			return "j2ee.ear"; //$NON-NLS-1$
 		else if (IModuleConstants.JST_APPCLIENT_MODULE.equals(type))
 			return "j2ee.appclient"; //$NON-NLS-1$
 		else if (IModuleConstants.JST_CONNECTOR_MODULE.equals(type))
 			return "j2ee.connector"; //$NON-NLS-1$
 		else if (IModuleConstants.JST_EJB_MODULE.equals(type))
 			return "j2ee.ejb"; //$NON-NLS-1$
 		else if (IModuleConstants.JST_WEB_MODULE.equals(type))
 			return "j2ee.web"; //$NON-NLS-1$
 		else
 			return null;
 	}
 
 	public IModuleType getModuleType() {
 		return new IModuleType() {
 
 			public String getId() {
 				return getType();
 			}
 
 			public String getName() {
 				return getModuleTypeName();
 			}
 
 			public String getVersion() {
 				return getModuleTypeVersion();
 			}
 		};
 
 	}
 	/**
 	 * Return the module resources for both the component meta folder and the java output folder
 	 * 
 	 * @return IModuleResource[]
 	 */
     public IModuleResource[] members() throws CoreException {
         outputMembersAdded = false;
         members.clear();
         if (component == null)
         	return new IModuleResource[] {};
         try {
         	// Retrieve the java output folder files
 //	    	IContainer[] outputFolders = ComponentUtilities.getOutputContainers(component);
 //	    	for (int i=0; i<outputFolders.length; i++) {
 //	    		if (outputFolders[i]!=null && outputFolders[i].exists()) {
 //	    			IModuleResource[] javaResources = getModuleResources(Path.EMPTY,outputFolders[i]);
 //	    			members.addAll(Arrays.asList(javaResources));
 //	    		}
 //	    	}
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
     
     /**
      * Helper method to check a given list of resource trees for the given resource.  It will return the
      * already existing instance of the module Resource if found, or it will return null.
      * 
      * @param List moduleResources
      * @param IModuleResource aModuleResource
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
 	    		else if (moduleResource instanceof ModuleFolder) {
 	    			result = getExistingModuleResource(Arrays.asList(((ModuleFolder)moduleResource).members()),aModuleResource);
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
     	if (runtimePath.endsWith(".java")) { //$NON-NLS-1$
     		IContainer output = ComponentUtilities.getOutputContainers(container.getComponent())[0];
     		runtimePath = resource.getRuntimePath().lastSegment().toString();
     		String className = runtimePath.substring(0,runtimePath.length()-4)+"class"; //$NON-NLS-1$
     		file = findClassFileInOutput(output,className);
     	} else
     		file = (IFile) resource.getUnderlyingResource();
     	if (file == null)
     		return;
         ModuleFile moduleFile = new ModuleFile(file, file.getName(), path, file.getModificationStamp());
         // we have to be sure its not in cache and current result group waiting to be added to cache
         if (file != null && file.exists() && getExistingModuleResource(result,moduleFile)==null && getExistingModuleResource(members,moduleFile)==null)
             result.add(moduleFile);
     }
     
     private IFile findClassFileInOutput(IContainer container, String className) {
     	IFile result = null;
     	result = (IFile) container.findMember(className);
     	if (result == null) {
 	    	try {
 	    		IResource[] currentMembers = container.members();
 	    		for (int i=0; i<currentMembers.length; i++) {
 	    			if (currentMembers[i].getType()==IResource.FOLDER) {
 	    				result = findClassFileInOutput((IContainer)currentMembers[i],className);
 	    				if (result != null)
 	    					break;
 	    			}
 	    		}
 	    	} catch (Exception e) {}
     	}
     	return result;
     }
     
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
     
     public String getJ2CSpecificationVersion() {
 		return getVersion();
 	}
     
     /**
      * Returns the classpath as a list of absolute IPaths.
      * 
      * @param java.util.List
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
     
     public String getEJBSpecificationVersion() {
 		return getVersion();
 	}
     
     public String getJNDIName(String ejbName) {
     	if (!IModuleConstants.JST_EJB_MODULE.equals(component.getComponentTypeId()))
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
     	for (int i=0; i<components.length; i++) {
 			IVirtualReference reference = components[i];
 			IVirtualComponent virtualComp = reference.getReferencedComponent();
 			Object module = FlexibleProjectServerUtil.getModule(virtualComp);
 			if (module!=null)
 				modules.add(module);
 		}
         return (IModule[]) modules.toArray(new IModule[modules.size()]);
 	}
     
     public String getURI(IModule module) {
     	IVirtualComponent comp = ComponentCore.createComponent(module.getProject());
     	if (IModuleConstants.JST_EAR_MODULE.equals(comp.getComponentTypeId())) {
 			EARArtifactEdit earEdit = null;
 			String aURI = null;
 			try {
 				earEdit = EARArtifactEdit.getEARArtifactEditForRead(component);
 				if (earEdit != null) {
 					aURI = earEdit.getModuleURI(comp);
 				}
 			} catch (Exception e) {
 				e.printStackTrace();
 			} finally {
 				if (earEdit != null)
 					earEdit.dispose();
 			}
 			return aURI;
     	}
     	else if (IModuleConstants.JST_WEB_MODULE.equals(comp.getComponentTypeId())) {
     		String result = null;
     		if (!comp.isBinary()) {
         		IVirtualReference ref = component.getReference(comp.getName());
         		result = ref.getRuntimePath().append(comp.getName()+IJ2EEModuleConstants.JAR_EXT).toString();
         	}
         	return result;
     	}
     	return null;
 	}
     
     public String getJSPSpecificationVersion() {
     	String ret = "1.2"; //$NON-NLS-1$
     	String stringVersion = getServletSpecificationVersion();
 		int nVersion = J2EEVersionUtil.convertVersionStringToInt(stringVersion);
        	switch( nVersion ){
     		case 22:
     			ret = J2EEVersionConstants.VERSION_1_1_TEXT;
     			break;
     		case 23:
     			ret = J2EEVersionConstants.VERSION_1_2_TEXT;
     			break;
     		case 24:	
     			ret = J2EEVersionConstants.VERSION_2_0_TEXT;
     			break;
       		default:
     			ret = J2EEVersionConstants.VERSION_1_1_TEXT;
     			break;    			
     	}
     	return ret; 
     }
 
     public String getServletSpecificationVersion() {
 		return getVersion();
 	}
     
     public String getContextRoot() {
 		Properties props = component.getMetaProperties();
 		if(props.containsKey(J2EEConstants.CONTEXTROOT))
 			return props.getProperty(J2EEConstants.CONTEXTROOT);
 	    return component.getName();
     }
 }
