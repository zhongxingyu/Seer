 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.internal.project;
 
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IPackageFragmentRoot;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jem.java.JavaClass;
 import org.eclipse.jem.java.JavaRefFactory;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.emf.workbench.WorkbenchByteArrayOutputStream;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jem.util.plugin.JEMUtilPlugin;
 import org.eclipse.jem.workbench.utility.JemProjectUtilities;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EARFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.EJBJarFile;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.File;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifest;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.helpers.ArchiveManifestImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.impl.CommonarchiveFactoryImpl;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.util.ArchiveUtil;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.archive.operations.JavaComponentLoadStrategyImpl;
 import org.eclipse.jst.j2ee.internal.moduleextension.EarModuleManager;
 import org.eclipse.jst.j2ee.project.facet.IJavaProjectMigrationDataModelProperties;
 import org.eclipse.jst.j2ee.project.facet.JavaProjectMigrationDataModelProvider;
 import org.eclipse.jst.j2ee.project.facet.JavaProjectMigrationOperation;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.util.ComponentUtilities;
 import org.eclipse.wst.common.componentcore.internal.util.IModuleConstants;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacet;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 
 
 public class J2EEProjectUtilities extends ProjectUtilities {
 	
 	public static final String ENTERPRISE_APPLICATION = IModuleConstants.JST_EAR_MODULE;
 	public static final String APPLICATION_CLIENT = IModuleConstants.JST_APPCLIENT_MODULE;
 	public static final String EJB = IModuleConstants.JST_EJB_MODULE;
 	public static final String DYNAMIC_WEB = IModuleConstants.JST_WEB_MODULE;
 	public static final String UTILITY = IModuleConstants.JST_UTILITY_MODULE;
 	public static final String JCA = IModuleConstants.JST_CONNECTOR_MODULE;
 	public static final String STATIC_WEB = IModuleConstants.WST_WEB_MODULE;
 
 	/**
 	 * Return the absolute path of a loose archive in a J2EE application or WAR file
 	 */
 	public static IPath getRuntimeLocation(IProject aProject) {
 		if (JemProjectUtilities.isBinaryProject(aProject))
 			return getBinaryProjectJARLocation(aProject);
 		return JemProjectUtilities.getJavaProjectOutputAbsoluteLocation(aProject);
 	}
 
 	public static IPath getBinaryProjectJARLocation(IProject aProject) {
 		List sources = JemProjectUtilities.getLocalJARPathsFromClasspath(aProject);
 		if (!sources.isEmpty()) {
 			IPath path = (IPath) sources.get(0);
 			return aProject.getFile(path).getLocation();
 		}
 		return null;
 	}
 
 	public static Archive getClientJAR(EJBJarFile file, EARFile earFile) {
 		EJBJar jar = file.getDeploymentDescriptor();
 		if (jar == null)
 			return null;
 		String clientJAR = jar.getEjbClientJar();
 		if (clientJAR == null || clientJAR.length() == 0)
 			return null;
 		String normalized = ArchiveUtil.deriveEARRelativeURI(clientJAR, file.getURI());
 		if (normalized != null) {
 			try {
 				File aFile = earFile.getFile(normalized);
 				if (aFile.isArchive() && !aFile.isModuleFile())
 					return (Archive) aFile;
 			} catch (FileNotFoundException nothingThere) {
 			}
 		}
 		return null;
 		// TODO - release the DD here to free up space
 	}
 
 	/**
 	 * Append one IClasspathEntry to the build path of the passed project. If a classpath entry
 	 * having the same path as the parameter already exists, then does nothing.
 	 */
 	public static void appendJavaClassPath(IProject p, IClasspathEntry newEntry) throws JavaModelException {
 		IJavaProject javaProject = JemProjectUtilities.getJavaProject(p);
 		if (javaProject == null)
 			return;
 		IClasspathEntry[] classpath = javaProject.getRawClasspath();
 		List newPathList = new ArrayList(classpath.length);
 		for (int i = 0; i < classpath.length; i++) {
 			IClasspathEntry entry = classpath[i];
 			// fix dup class path entry for .JETEmitter project
 			// Skip the entry to be added if it already exists
 			if (Platform.getOS().equals(Platform.OS_WIN32)) {
 				if (!entry.getPath().toString().equalsIgnoreCase(newEntry.getPath().toString()))
 					newPathList.add(entry);
 				else
 					return;
 			} else {
 				if (!entry.getPath().equals(newEntry.getPath()))
 					newPathList.add(entry);
 				else
 					return;
 			}
 		}
 		newPathList.add(newEntry);
 		IClasspathEntry[] newClasspath = (IClasspathEntry[]) newPathList.toArray(new IClasspathEntry[newPathList.size()]);
 		javaProject.setRawClasspath(newClasspath, new NullProgressMonitor());
 	}
 
 
 	public static Archive asArchiveFromBinary(String jarUri, IProject aProject) throws OpenFailureException {
 		IPath path = getBinaryProjectJARLocation(aProject);
 		if (path != null) {
 			String location = path.toOSString();
 			Archive anArchive = CommonarchiveFactoryImpl.getActiveFactory().primOpenArchive(location);
 			anArchive.setURI(jarUri);
 			return anArchive;
 		}
 		return null;
 	}
 
 	public static ArchiveManifest readManifest(IFile aFile) {
 		InputStream in = null;
 		try {
 			if (aFile == null || !aFile.exists())
 				return null;
 			in = aFile.getContents();
 			return new ArchiveManifestImpl(in);
 		} catch (Exception ex) {
 			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 			return null;
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException weTried) {
 				}
 			}
 		}
 	}
 
 	public static ArchiveManifest readManifest(IProject p) {
 		InputStream in = null;
 		try {
 			IFile aFile = getManifestFile(p);
 			if (aFile == null || !aFile.exists())
 				return null;
 			in = aFile.getContents();
 			return new ArchiveManifestImpl(in);
 		} catch (Exception ex) {
 			org.eclipse.jem.util.logger.proxy.Logger.getLogger().logError(ex);
 			return null;
 		} finally {
 			if (in != null) {
 				try {
 					in.close();
 				} catch (IOException weTried) {
 				}
 			}
 		}
 	}
 
 	public static IFile getManifestFile(IProject p) {
 		IVirtualComponent component = ComponentCore.createComponent(p);
 		try {
 			return ComponentUtilities.findFile(component, new Path(J2EEConstants.MANIFEST_URI));
 		} catch (CoreException ce) {
 			Logger.getLogger().log(ce);
 		}
 		return null;
 	}
 
 	public static void writeManifest(IProject p, ArchiveManifest manifest) throws java.io.IOException {
 
 		IFile aFile = getManifestFile(p);
 		if (aFile != null) {
 			OutputStream out = new WorkbenchByteArrayOutputStream(aFile);
 			manifest.writeSplittingClasspath(out);
 			out.close();
 		}
 	}
 
 	public static void writeManifest(IFile aFile, ArchiveManifest manifest) throws java.io.IOException {
 		if (aFile != null) {
 			OutputStream out = new WorkbenchByteArrayOutputStream(aFile);
 			manifest.writeSplittingClasspath(out);
 			out.close();
 		}
 	}
 
 	/**
 	 * Keys are the EJB JAR files and the values are the respective client JARs; includes only key
 	 * value pairs for which EJB Client JARs are defined and exist.
 	 * 
 	 * @author schacher
 	 */
 	public static Map collectEJBClientJARs(EARFile earFile) {
 		if (earFile == null)
 			return Collections.EMPTY_MAP;
 		Map ejbClientJARs = null;
 		List ejbJARFiles = earFile.getEJBJarFiles();
 		Archive clientJAR = null;
 		for (int i = 0; i < ejbJARFiles.size(); i++) {
 			EJBJarFile ejbJarFile = (EJBJarFile) ejbJARFiles.get(i);
 			clientJAR = getClientJAR(ejbJarFile, earFile);
 			if (clientJAR != null) {
 				if (ejbClientJARs == null)
 					ejbClientJARs = new HashMap();
 				ejbClientJARs.put(ejbJarFile, clientJAR);
 			}
 		}
 		return ejbClientJARs == null ? Collections.EMPTY_MAP : ejbClientJARs;
 	}
 
 	public static String computeRelativeText(String referencingURI, String referencedURI, EnterpriseBean bean) {
 		if (bean == null)
 			return null;
 
 		String beanName = bean.getName();
 		if (beanName == null)
 			return null;
 
 		String relativeUri = computeRelativeText(referencingURI, referencedURI);
 		if (relativeUri == null)
 			return beanName;
 		return relativeUri + "#" + beanName; //$NON-NLS-1$
 	}
 
 	public static String computeRelativeText(String referencingURI, String referencedURI) {
 		if (referencingURI == null || referencedURI == null)
 			return null;
 		IPath pPre = new Path(referencingURI);
 		IPath pDep = new Path(referencedURI);
 		if (pPre.getDevice() != null || pDep.getDevice() != null)
 			return null;
 		pPre = pPre.makeRelative();
 		pDep = pDep.makeRelative(); // referenced Archive path URI
 
 		while (pPre.segmentCount() > 1 && pDep.segmentCount() > 1 && pPre.segment(0).equals(pDep.segment(0))) {
 			pPre = pPre.removeFirstSegments(1);
 			pDep = pDep.removeFirstSegments(1);
 		}
 
 		IPath result = null; //$NON-NLS-1$
 		StringBuffer buf = new StringBuffer();
 		String segment = null;
 		do {
 			segment = pDep.lastSegment();
 			pPre = pPre.removeLastSegments(1);
 			pDep = pDep.removeLastSegments(1);
 			if (segment != null) {
 				if (result == null)
 					result = new Path(segment);
 				else
 					result = new Path(segment).append(result);
 			}
 			if (!pPre.equals(pDep) && !pPre.isEmpty())
 				buf.append("../"); //$NON-NLS-1$
 		} while (!pPre.equals(pDep));
 
 		if (result != null)
 			buf.append(result.makeRelative().toString());
 
 		return buf.toString();
 	}
 
 	public static IProject getEJBProjectFromEJBClientProject(IProject ejbClientProject) {
 		try {
 			if (null != ejbClientProject && ejbClientProject.hasNature(JavaCore.NATURE_ID)) {
 				IProject[] allProjects = getAllProjects();
 				for (int i = 0; i < allProjects.length; i++) {
 					if (null != EarModuleManager.getEJBModuleExtension().getEJBJar(allProjects[i])) {
 						if (ejbClientProject == EarModuleManager.getEJBModuleExtension().getDefinedEJBClientJARProject(allProjects[i])) {
 							return allProjects[i];
 						}
 					}
 				}
 			}
 		} catch (CoreException e) {
 		}
 		return null;
 	}
 
 	public static EnterpriseBean getEnterpriseBean(ICompilationUnit cu) {
 		IProject proj = cu.getJavaProject().getProject();
 		EJBJar jar = EarModuleManager.getEJBModuleExtension().getEJBJar(proj);
 		if (null == jar) {
 			jar = EarModuleManager.getEJBModuleExtension().getEJBJar(getEJBProjectFromEJBClientProject(proj));
 		}
 		if (jar != null) {
 			int index = cu.getElementName().indexOf('.');
 			String className = cu.getElementName();
 			if (index > 0)
 				className = className.substring(0, index);
 			JavaClass javaClass = (JavaClass) JavaRefFactory.eINSTANCE.reflectType(cu.getParent().getElementName(), className, jar.eResource().getResourceSet());
 			return jar.getEnterpriseBeanWithReference(javaClass);
 		}
 		return null;
 	}
 
 	public static IContainer getSourceFolderOrFirst(IProject p, String defaultSourceName) {
 		try {
 			IPath sourcePath = getSourcePathOrFirst(p, defaultSourceName);
 			if (sourcePath == null)
 				return null;
 			else if (sourcePath.isEmpty())
 				return p;
 			else
 				return p.getFolder(sourcePath);
 		} catch (IllegalArgumentException ex) {
 			return null;
 		}
 	}
 
 	public static IPath getSourcePathOrFirst(IProject p, String defaultSourceName) {
 		IJavaProject javaProj = JemProjectUtilities.getJavaProject(p);
 		if (javaProj == null)
 			return null;
 		IClasspathEntry[] cp = null;
 		try {
 			cp = javaProj.getRawClasspath();
 		} catch (JavaModelException ex) {
 			JEMUtilPlugin.getLogger().logError(ex);
 			return null;
 		}
 		IClasspathEntry firstSource = null;
 		IPath defaultSourcePath = null;
 		if (defaultSourceName != null)
 			defaultSourcePath = createPath(p, defaultSourceName);
 		boolean found = false;
 		for (int i = 0; i < cp.length; i++) {
 			if (cp[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 				// check if it contains /META-INF/MANIFEST.MF
 				IPath sourceFolderPath = cp[i].getPath().removeFirstSegments(1);
 				IFolder sourceFolder = p.getFolder(sourceFolderPath);
 				if (isSourceFolderAnInputContainer(sourceFolder)) {
 					found = true;
 					if (firstSource == null) {
 						firstSource = cp[i];
 						if (defaultSourcePath == null)
 							break;
 					}
 					if (cp[i].getPath().equals(defaultSourcePath))
 						return defaultSourcePath.removeFirstSegments(1);
 				}
 			}
 		}
 		if (!found) {
 			for (int i = 0; i < cp.length; i++) {
 				if (cp[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 					if (firstSource == null) {
 						firstSource = cp[i];
 						if (defaultSourcePath == null)
 							break;
 					}
 					if (cp[i].getPath().equals(defaultSourcePath))
 						return defaultSourcePath.removeFirstSegments(1);
 				}
 			}
 		}
 		if (firstSource == null)
 			return null;
 		if (firstSource.getPath().segment(0).equals(p.getName()))
 			return firstSource.getPath().removeFirstSegments(1);
 		return null;
 	}
 
 	public static boolean isSourceFolderAnInputContainer(IFolder sourceFolder) {
 		IContainer parent = sourceFolder;
 		while (true) {
 			parent = parent.getParent();
 			if (parent == null)
 				return false;
 			if (parent instanceof IProject)
 				break;
 		}
 		IProject project = (IProject) parent;
 		try {
 			if (!project.isAccessible())
 				return false;
 			if (isEJBProject(project)) {
 				return sourceFolder.findMember(J2EEConstants.EJBJAR_DD_URI) != null;
 			} else if (isApplicationClientProject(project)) {
 				return sourceFolder.findMember(J2EEConstants.APP_CLIENT_DD_URI) != null;
 			} else if (isDynamicWebProject(project)) {
 				return sourceFolder.findMember(J2EEConstants.WEBAPP_DD_URI) != null;
 			} else if (isJCAProject(project)) {
 				return sourceFolder.findMember(J2EEConstants.RAR_DD_URI) != null;
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		return false;
 	}
 	
 	public static Archive asArchive(String jarUri, IProject project, boolean exportSource) throws OpenFailureException {
 		JavaComponentLoadStrategyImpl strat = new JavaComponentLoadStrategyImpl(ComponentCore.createComponent(project));
 		strat.setExportSource(exportSource);
 		return CommonarchiveFactoryImpl.getActiveFactory().primOpenArchive(strat, jarUri);
 	}
 	
 	public static boolean isProjectOfType(IProject project, String typeID) {
 		IFacetedProject facetedProject = null;
 		try {
 			facetedProject = ProjectFacetsManager.create(project);
 		} catch (CoreException e) {
 			return false;
 		}
 		
 		if (facetedProject !=null && ProjectFacetsManager.isProjectFacetDefined(typeID)) {
 			IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet(typeID);
 			return projectFacet!=null && facetedProject.hasProjectFacet(projectFacet);
 		}
 		return false;
 	}
 	private static boolean isProjectOfType(IFacetedProject facetedProject, String typeID) {
 		
 		if (facetedProject !=null && ProjectFacetsManager.isProjectFacetDefined(typeID)) {
 			IProjectFacet projectFacet = ProjectFacetsManager.getProjectFacet(typeID);
 			return projectFacet!=null && facetedProject.hasProjectFacet(projectFacet);
 		}
 		return false;
 	}
 	
 	private static boolean isEARProject(IFacetedProject project) {
 		return isProjectOfType(project, ENTERPRISE_APPLICATION);
 	}
 	
 	private static boolean isDynamicWebProject(IFacetedProject project) {
 		return isProjectOfType(project, DYNAMIC_WEB);
 	}
 	
 	private static boolean isStaticWebProject(IFacetedProject project) {
 		return isProjectOfType(project, STATIC_WEB);
 	}
 	
 	private static boolean isEJBProject(IFacetedProject project) {
 		return isProjectOfType(project, EJB);
 	}
 	
 	private static boolean isJCAProject(IFacetedProject project) {
 		return isProjectOfType(project, JCA);
 	}
 	
 	private static boolean isApplicationClientProject(IFacetedProject project) {
 		return isProjectOfType(project, APPLICATION_CLIENT);
 	}
 	
 	private static boolean isUtilityProject(IFacetedProject project) {
 		return isProjectOfType(project, UTILITY);
 	}
 	public static boolean isEARProject(IProject project) {
 		return isProjectOfType(project, ENTERPRISE_APPLICATION);
 	}
 	
 	public static boolean isDynamicWebProject(IProject project) {
 		return isProjectOfType(project, DYNAMIC_WEB);
 	}
 	
 	public static boolean isStaticWebProject(IProject project) {
 		return isProjectOfType(project, STATIC_WEB);
 	}
 	
 	public static boolean isEJBProject(IProject project) {
 		return isProjectOfType(project, EJB);
 	}
 	
 	public static boolean isJCAProject(IProject project) {
 		return isProjectOfType(project, JCA);
 	}
 	
 	public static boolean isApplicationClientProject(IProject project) {
 		return isProjectOfType(project, APPLICATION_CLIENT);
 	}
 	
 	public static boolean isUtilityProject(IProject project) {
 		return isProjectOfType(project, UTILITY);
 	}
 	
 	public static boolean isStandaloneProject(IProject project) {
 		return getReferencingEARProjects(project).length==0;
 	}
 	
 	public static IProject[] getReferencingEARProjects(IProject project) {
 		List result = new ArrayList();
 		IVirtualComponent component = ComponentCore.createComponent(project);
 		IVirtualComponent[] refComponents = component.getReferencingComponents();
 		for (int i=0; i<refComponents.length; i++) {
 			if (isEARProject(refComponents[i].getProject()))
 				result.add(refComponents[i].getProject());
 		}
 		return (IProject[]) result.toArray(new IProject[result.size()]);
 	}
 	
 	/**
 	 * Return all projects in workspace of the specified type
 	 * @param type - use one of the static strings on this class as a type
 	 * @return IProject[]
 	 */
 	public static IProject[] getAllProjectsInWorkspaceOfType(String type) {
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		List result = new ArrayList();
 		for (int i = 0; i < projects.length; i++) {
 			if (isProjectOfType(projects[i],type))
 				result.add(projects[i]);
 		}
 		return (IProject[]) result.toArray(new IProject[result.size()]);
 	}
 	
 	public static String getJ2EEProjectType(IProject project) {
 		IFacetedProject facetedProject = null;
 		try {
 			facetedProject = ProjectFacetsManager.create(project);
 		} catch (CoreException e) {
 			return ""; //$NON-NLS-1$
 		}
 		if (isApplicationClientProject(facetedProject))
 			return APPLICATION_CLIENT;
 		else if (isDynamicWebProject(facetedProject))
 			return DYNAMIC_WEB;
 		else if (isEJBProject(facetedProject))
 			return EJB;
 		else if (isEARProject(facetedProject))
 			return ENTERPRISE_APPLICATION;
 		else if (isJCAProject(facetedProject))
 			return JCA;
 		else if (isStaticWebProject(facetedProject))
 			return STATIC_WEB;
 		else if (isUtilityProject(facetedProject))
 			return UTILITY;
 		else
 			return ""; //$NON-NLS-1$
 	}
 	public static String getJ2EEProjectVersion(IProject project) {
 		String type = getJ2EEProjectType(project);
 		IFacetedProject facetedProject = null;
 		IProjectFacet facet = null;
 		try {
 		facetedProject = ProjectFacetsManager.create(project);
 		facet = ProjectFacetsManager.getProjectFacet(type);
 		} catch (Exception e) {
 			// Not Faceted project or not J2EE Project
 		}
 		if (facet != null && facetedProject.hasProjectFacet(facet))
 			return facetedProject.getInstalledVersion(facet).getVersionString();
 		return null;
 	}
 	public static JavaProjectMigrationOperation createFlexJavaProjectForProjectOperation(IProject project) {
 		IDataModel model = DataModelFactory.createDataModel(new JavaProjectMigrationDataModelProvider());
 		model.setProperty(IJavaProjectMigrationDataModelProperties.PROJECT_NAME, project.getName());
 		return new JavaProjectMigrationOperation(model);
 	}
 	
 	/**
 	 * Retrieve all the source containers for a given virtual workbench component
 	 * 
 	 * @param vc
 	 * @return the array of IPackageFragmentRoots
 	 */
 	public static IPackageFragmentRoot[] getSourceContainers(IProject project) {
 		List list = new ArrayList();
 		IJavaProject jProject = JavaCore.create(project);
 		IVirtualComponent vc = ComponentCore.createComponent(project);
 		IPackageFragmentRoot[] roots;
 		try {
 			roots = jProject.getPackageFragmentRoots();
 			for (int i = 0; i < roots.length; i++) {
 				IResource resource = roots[i].getResource();
 				if (null != resource) {
 					IVirtualResource[] vResources = ComponentCore.createResources(resource);
 					boolean found = false;
 					for (int j = 0; !found && j < vResources.length; j++) {
 						if (vResources[j].getComponent().equals(vc)) {
							if (!list.contains(roots[i]))
								list.add(roots[i]);
 							found = true;
 						}
 					}
 				}
 			}
 		} catch (JavaModelException e) {
 			Logger.getLogger().logError(e);
 		}
 		return (IPackageFragmentRoot[]) list.toArray(new IPackageFragmentRoot[list.size()]);
 	}
 	
 	/**
 	 * Retrieve all the output containers for a given virtual component.
 	 * 
 	 * @param vc
 	 * @return array of IContainers for the output folders
 	 */
 	public static IContainer[] getOutputContainers(IProject project) {
 		List result = new ArrayList();
 		try {
 			if (!project.hasNature(JavaCore.NATURE_ID))
 				return new IContainer[]{};
 		}
 		catch (Exception e) {}
 		IPackageFragmentRoot[] sourceContainers = getSourceContainers(project);
 		IJavaProject jProject = JavaCore.create(project);
 		
 		for (int i=0; i<sourceContainers.length; i++) {
 			try {
 				IFolder outputFolder;
 				IPath outputPath = sourceContainers[i].getRawClasspathEntry().getOutputLocation();
 				if (outputPath == null)
 					outputFolder = project.getFolder(jProject.getOutputLocation().removeFirstSegments(1));
 				else
 					outputFolder = project.getFolder(outputPath.removeFirstSegments(1));
				if (outputFolder != null && !result.contains(outputFolder))
 					result.add(outputFolder);
 			} catch (Exception e) {
 				continue;
 			}
 		}
 		return (IContainer[]) result.toArray(new IContainer[result.size()]);
 	}
 	
 	/**
 	 * 
 	 * @param name
 	 * @return
 	 * @description the passed name should have either lib or var as its first segment e.g.
 	 *              lib/D:/foo/foo.jar or var/<CLASSPATHVAR>/foo.jar
 	 */
 	public static IPath getResolvedPathForArchiveComponent(String name) {
 
 		URI uri = URI.createURI(name);
 
 		String resourceType = uri.segment(0);
 		URI contenturi = ModuleURIUtil.trimToRelativePath(uri, 1);
 		String contentName = contenturi.toString();
 
 		if (resourceType.equals("lib")) { //$NON-NLS-1$
 			// module:/classpath/lib/D:/foo/foo.jar
 			return Path.fromOSString(contentName);
 
 		} else if (resourceType.equals("var")) { //$NON-NLS-1$
 
 			// module:/classpath/var/<CLASSPATHVAR>/foo.jar
 			String classpathVar = contenturi.segment(0);
 			URI remainingPathuri = ModuleURIUtil.trimToRelativePath(contenturi, 1);
 			String remainingPath = remainingPathuri.toString();
 
 			String[] classpathvars = JavaCore.getClasspathVariableNames();
 			boolean found = false;
 			for (int i = 0; i < classpathvars.length; i++) {
 				if (classpathVar.equals(classpathvars[i])) {
 					found = true;
 					break;
 				}
 			}
 			if (found) {
 				IPath path = JavaCore.getClasspathVariable(classpathVar);
 				URI finaluri = URI.createURI(path.toOSString() + IPath.SEPARATOR + remainingPath);
 				return Path.fromOSString(finaluri.toString());
 			}
 		}
 		return null;
 	}
 	
 	public static List getAllJavaNonFlexProjects() throws CoreException {
 		List nonFlexJavaProjects = new ArrayList();
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		for (int i = 0; i < projects.length; i++) {
 			if (projects[i].isAccessible() && projects[i].hasNature(JavaCore.NATURE_ID) && !projects[i].hasNature(IModuleConstants.MODULE_NATURE_ID)) {
 				nonFlexJavaProjects.add(projects[i]);
 			}
 		}
 		return nonFlexJavaProjects;
 	}
 }
