 /*******************************************************************************
  * Copyright (c) 2005 Oracle Corporation.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    Ian Trimble - initial API and implementation
  *******************************************************************************/ 
 package org.eclipse.jst.jsf.core.jsfappconfig;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.jar.JarEntry;
 import java.util.jar.JarFile;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jst.j2ee.common.ParamValue;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.web.componentcore.util.WebArtifactEdit;
 import org.eclipse.jst.j2ee.webapplication.ContextParam;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.jsf.core.IJSFCoreConstants;
 import org.eclipse.jst.jsf.core.internal.JSFCorePlugin;
 import org.eclipse.jst.jsf.core.internal.Messages;
 import org.eclipse.osgi.util.NLS;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualFolder;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 /**
  * JSFAppConfigUtils provides utility methods useful in processing of a JSF
  * application configuration.
  * 
  * @author Ian Trimble - Oracle
  */
 public class JSFAppConfigUtils {
 
 	/**
 	 * Name of JSF CONFIG_FILES context parameter ("javax.faces.CONFIG_FILES").
 	 */
 	public static final String CONFIG_FILES_CONTEXT_PARAM_NAME = "javax.faces.CONFIG_FILES"; //$NON-NLS-1$
 
 	/**
 	 * Location in JAR file of application configuration resource file
 	 * ("META-INF/faces-config.xml"). 
 	 */
 	public static final String FACES_CONFIG_IN_JAR_PATH = "META-INF/faces-config.xml"; //$NON-NLS-1$
 
     
     /**
      * @param project
      * @param minVersion
      * @return true if project is a JSF facet project and the version of the project
      * is at least minVersion.
      */
     public static boolean isValidJSFProject(IProject project, String minVersion)
     {
         boolean isValid = false;
         
         final IProjectFacetVersion projectFacetVersion = getProjectFacet(project);
         
         if (projectFacetVersion != null)
         {
             try
             {
                 final String versionString = 
                     projectFacetVersion.getVersionString();
                 final Comparator comparator = 
                     projectFacetVersion.getProjectFacet().getVersionComparator();
                 final int compareToMin = 
                     comparator.compare(versionString, minVersion);
                 
                 if (compareToMin >=0)
                 {
                     return true;
                 }
             }
             catch (CoreException ce)
             {
                 JSFCorePlugin.log(ce, "Error checking facet version"); //$NON-NLS-1$
             }
         }
         return isValid;
     }
     
 	/**
 	 * Tests if the passed IProject instance is a valid JSF project in the
 	 * following ways:
 	 * <ul>
 	 * <li>project is not null and is accessible, </li>
 	 * <li>project has the JSF facet set on it.</li>
 	 * </ul>
 	 * 
 	 * @param project IProject instance to be tested.
 	 * @return true if the IProject instance is a valid JSF project, else
 	 * false.
 	 */
 	public static boolean isValidJSFProject(IProject project) {
 		boolean isValid = false;
         IProjectFacetVersion projectFacet = getProjectFacet(project);
         if (projectFacet != null)
         {
             isValid = true;
         }
 		return isValid;
 	}
     /**
      * Get the facet version for the project 
      * @param project
      * @return the project facet version or null if could not be found or if
      * project is not accessible
      */
     public static IProjectFacetVersion getProjectFacet(IProject project)
     {
         //check for null or inaccessible project
         if (project != null && project.isAccessible()) {
             //check for JSF facet on project
             try {
                 IFacetedProject facetedProject = ProjectFacetsManager.create(project);
                 if (facetedProject != null) {
                     Set projectFacets = facetedProject.getProjectFacets();
                     Iterator itProjectFacets = projectFacets.iterator();
                     while (itProjectFacets.hasNext()) {
                         IProjectFacetVersion projectFacetVersion = (IProjectFacetVersion)itProjectFacets.next();
                         if (IJSFCoreConstants.JSF_CORE_FACET_ID.equals(projectFacetVersion.getProjectFacet().getId()))
                         {
                             return projectFacetVersion;
                         }
                     }
                 }
             } catch(CoreException ce) {
                 //log error
                 JSFCorePlugin.log(IStatus.ERROR, ce.getLocalizedMessage(), ce);
             }
         }
         return null;
     }
     
 	/**
 	 * Gets an IVirtualFolder instance which represents the root context's
 	 * web content folder.
 	 * 
 	 * @param project IProject instance for which to get the folder.
 	 * @return IVirtualFolder instance which represents the root context's
 	 * web content folder.
 	 */
 	public static IVirtualFolder getWebContentFolder(IProject project) {
 		IVirtualFolder folder = null;
 		IVirtualComponent component = ComponentCore.createComponent(project);
 		if (component != null) {
 			folder = component.getRootFolder();
 		}
 		return folder;
 	}
 
 	/**
 	 * Gets an IPath instance representing the path of the passed IFile
 	 * instance relative to the web content folder.
 	 * 
 	 * @param file IFile instance for which a path is required.
 	 * @return IPath instance representing the path relative to the web content
 	 * folder.
 	 */
 	public static IPath getWebContentFolderRelativePath(IFile file) {
 		IPath path = null;
 		if (file != null) {
 			IVirtualFolder webContentFolder = getWebContentFolder(file.getProject());
 			if (webContentFolder != null) {
 				IPath webContentPath = webContentFolder.getProjectRelativePath();
 				IPath filePath = file.getProjectRelativePath();
 				int matchingFirstSegments = webContentPath.matchingFirstSegments(filePath);
 				path = filePath.removeFirstSegments(matchingFirstSegments);
 			}
 		}
 		return path;
 	}
 
 	/**
 	 * Gets list of application configuration file names as listed in the JSF
 	 * CONFIG_FILES context parameter ("javax.faces.CONFIG_FILES"). Will return
 	 * an empty list if WebArtifactEdit is null, if WebApp is null, if context
 	 * parameter does not exist, or if trimmed context parameter's value is
 	 * an empty String.
 	 * 
 	 * @param project IProject instance for which to get the context
 	 * parameter's value.
 	 * @return List of application configuration file names as listed in the
 	 * JSF CONFIG_FILES context parameter ("javax.faces.CONFIG_FILES"); list
 	 * may be empty.
 	 */
 	public static List getConfigFilesFromContextParam(IProject project) {
 		List filesList = Collections.EMPTY_LIST;
 		if (isValidJSFProject(project)) {
 			IModelProvider provider = ModelProviderManager.getModelProvider(project);
 			Object webAppObj = provider.getModelObject();
 			if (webAppObj != null){
 				if (webAppObj instanceof WebApp)
 					filesList = getConfigFilesForJ2EEApp(project);
 				else if (webAppObj instanceof org.eclipse.jst.javaee.web.WebApp)
 					filesList = getConfigFilesForJEEApp((org.eclipse.jst.javaee.web.WebApp)webAppObj);
 			}
 			
 		}
 		return filesList;
 	}
 
 	private static List getConfigFilesForJEEApp(org.eclipse.jst.javaee.web.WebApp webApp) {
 		String filesString = null;
 		List contextParams = webApp.getContextParams();
 		Iterator itContextParams = contextParams.iterator();
 		while (itContextParams.hasNext()) {
 			org.eclipse.jst.javaee.core.ParamValue paramValue = (org.eclipse.jst.javaee.core.ParamValue)itContextParams.next();
 			if (paramValue.getParamName().equals(CONFIG_FILES_CONTEXT_PARAM_NAME)) {
 				filesString = paramValue.getParamValue();
 				break;
 			}
 		}		
 		return parseFilesString(filesString);	
 	}
 
 	private static List getConfigFilesForJ2EEApp(IProject project){
 		List filesList = new ArrayList();
 		WebArtifactEdit webArtifactEdit = WebArtifactEdit.getWebArtifactEditForRead(project);
 		if (webArtifactEdit != null) {
 			try {
 				WebApp webApp = null;
 				try {
 					webApp = webArtifactEdit.getWebApp();
 				} catch(ClassCastException cce) {
 					//occasionally thrown from WTP code in RC3 and possibly later
 					JSFCorePlugin.log(IStatus.ERROR, cce.getLocalizedMessage(), cce);
 					return filesList;
 				}
 				if (webApp != null) {
 					String filesString = null;
 					//need to branch here due to model version differences (BugZilla #119442)
 					if (webApp.getVersionID() == J2EEVersionConstants.WEB_2_3_ID) {
 						EList contexts = webApp.getContexts();
 						Iterator itContexts = contexts.iterator();
 						while (itContexts.hasNext()) {
 							ContextParam contextParam = (ContextParam)itContexts.next();
 							if (contextParam.getParamName().equals(CONFIG_FILES_CONTEXT_PARAM_NAME)) {
 								filesString = contextParam.getParamValue();
 								break;
 							}
 						}
 					} else {
 						EList contextParams = webApp.getContextParams();
 						Iterator itContextParams = contextParams.iterator();
 						while (itContextParams.hasNext()) {
 							ParamValue paramValue = (ParamValue)itContextParams.next();
 							if (paramValue.getName().equals(CONFIG_FILES_CONTEXT_PARAM_NAME)) {
 								filesString = paramValue.getValue();
 								break;
 							}
 						}
 					}					
 					filesList = parseFilesString(filesString);				
 				}
 			} finally {
 				webArtifactEdit.dispose();
 			}
 		}
 
 		return filesList;
 	}
 	
 	private static List parseFilesString(String filesString) {
 		List filesList = new ArrayList();
 		if (filesString != null && filesString.trim().length() > 0) {			
 			StringTokenizer stFilesString = new StringTokenizer(filesString, ","); //$NON-NLS-1$
 			while (stFilesString.hasMoreTokens()) {
 				String configFile = stFilesString.nextToken().trim();
 				filesList.add(configFile);
 			}
 		}
 		return filesList;
 	}
 
 	/**
 	 * Gets list of JAR file names, where each file name represents a JAR on
 	 * the classpath that contains a /META-INF/faces-config.xml entry. Will
 	 * return an empty list if no such JAR files are located.
 	 * 
 	 * @param project IProject instance for which to scan the classpath.
 	 * @return List of JAR file names, where each file name represents a JAR
 	 * on the classpath that contains a ...META-INF/faces-config.xml entry;
 	 * list may be empty.
 	 * @throws CoreException Thrown when underlying calls into JavaCore fail.
 	 * @throws IOException Thrown when attempt to open JAR to determine if it
 	 * contains a /META-INF/faces-config.xml entry fails.
 	 */
 	public static List getConfigFileJARsFromClasspath(IProject project) throws CoreException, IOException {
 		ArrayList JARsList = new ArrayList();
 		if (project.isAccessible()
 		        && project.hasNature(JavaCore.NATURE_ID)) {
 			IJavaProject javaProject = JavaCore.create(project);
 			if (javaProject != null) {
 				IClasspathEntry[] classpathEntries = javaProject.getResolvedClasspath(true);
 				if (classpathEntries != null && classpathEntries.length > 0) {
 					for (int i = 0; i < classpathEntries.length; i++) {
 						IClasspathEntry classpathEntry = classpathEntries[i];
 						if (classpathEntry.getEntryKind() == IClasspathEntry.CPE_LIBRARY) {
 							IPath libraryPath = classpathEntry.getPath();
 							if (libraryPath.getFileExtension() != null && libraryPath.getFileExtension().length() > 0) {
								//TODO: find better way to determine if workspace must be prepended to path
								if (libraryPath.getDevice() == null) {
									IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
									IPath workspaceRootPath = workspaceRoot.getLocation();
									libraryPath = workspaceRootPath.append(libraryPath);
 								}
 								String libraryPathString = libraryPath.toString();
 								JarFile jarFile = null;
 								try {
 									jarFile = new JarFile(libraryPathString);
 									if (jarFile != null) {
 										JarEntry jarEntry = jarFile.getJarEntry(FACES_CONFIG_IN_JAR_PATH);
 										if (jarEntry != null) {
 											JARsList.add(libraryPathString);
 										}
 									}
 								} catch(IOException ioe) {
 									JSFCorePlugin.log(
 											IStatus.ERROR,
 											NLS.bind(Messages.JSFAppConfigUtils_ErrorOpeningJarFile, libraryPathString),
 											ioe);
 								} finally {
 									if (jarFile != null) {
 										jarFile.close();
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		return JARsList;
 	}
 
 }
