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
 package org.eclipse.jst.jee.ui.internal.deployables;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.ICompilationUnit;
 import org.eclipse.jdt.core.IJavaElement;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.IType;
 import org.eclipse.jdt.core.ITypeHierarchy;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.internal.web.jfaces.extension.FileURL;
 import org.eclipse.jst.j2ee.internal.web.jfaces.extension.FileURLExtensionReader;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.javaee.core.UrlPatternType;
 import org.eclipse.jst.javaee.web.Servlet;
 import org.eclipse.jst.javaee.web.ServletMapping;
 import org.eclipse.jst.javaee.web.WebApp;
 import org.eclipse.jst.javaee.web.internal.impl.ServletImpl;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 import org.eclipse.wst.server.core.IModule;
 import org.eclipse.wst.server.core.IModuleArtifact;
 import org.eclipse.wst.server.core.ServerUtil;
 import org.eclipse.wst.server.core.util.WebResource;
 
 /**
  * @version 1.0
  * @author
  */
 public class WebDeployableArtifactUtil {
 	
 	private final static String GENERIC_SERVLET_CLASS_TYPE = "javax.servlet.GenericServlet"; //$NON-NLS-1$
 	private final static String CACTUS_SERVLET_CLASS_TYPE = "org.apache.cactus.server.ServletTestRedirector"; //$NON-NLS-1$
 
 	public WebDeployableArtifactUtil() {
 		super();
 	}
 
 	public static IModuleArtifact getModuleObject(Object obj) {
 		IResource resource = null;
 		if (obj instanceof IResource)
 			resource = (IResource) obj;
 		if (obj instanceof IModuleArtifact)
 			resource = ((IModuleArtifact) obj).getModule().getProject();
 		else if (obj instanceof IAdaptable)
 			resource = (IResource) ((IAdaptable) obj).getAdapter(IResource.class);
 		else if (obj instanceof EObject) {
 			resource = ProjectUtilities.getProject((EObject) obj);
 			if (obj instanceof Servlet) {
 				ServletImpl servlet = ((ServletImpl) obj);
 				Resource servResource = servlet.eResource();
 				IVirtualResource[] resources = null;
 				try {
 					IResource eclipeServResoruce = WorkbenchResourceHelper.getFile(servResource);
 					resources = ComponentCore.createResources(eclipeServResoruce);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				IVirtualComponent component = null;
 				if (resources[0] != null)
 					component = resources[0].getComponent();
 				String mapping = null;
				java.util.List mappings = getServletMappings(resource, servlet.getServletName());
 
 				if (mappings != null && !mappings.isEmpty()) {
 					ServletMapping map = (ServletMapping) mappings.get(0);
 					UrlPatternType urlPattern = (UrlPatternType)map.getUrlPatterns().get(0);
 					mapping = urlPattern.getValue();
 				}
 				if (mapping != null) {
 					return new WebResource(getModule(resource.getProject(), component), new Path(mapping));
 				}
 				
 //				WebType webType = ((Servlet) obj).getWebType();
 //				if (webType.isJspType()) {
 //					resource = ((IProject) resource).getFile(((JSPType) webType).getJspFile()); //$NON-NLS-1$
 //				} else if (webType.isServletType()) {
 //					return new WebResource(getModule(resource.getProject(), component), new Path("servlet/" + ((ServletType) webType).getClassName())); //$NON-NLS-1$
 //				}
 			}
 		}
 		if (resource == null)
 			return null;
 
 		if (resource instanceof IProject) {
 			IProject project = (IProject) resource;
 			if (hasInterestedComponents(project))
 				return new WebResource(getModule(project, null), project.getProjectRelativePath());
 		}
 		
 		if (!hasInterestedComponents(resource.getProject()))
             return null;
 		if (isCactusJunitTest(resource))
 			return null;
 
 		IPath resourcePath = resource.getFullPath();
 		IVirtualResource[] resources = ComponentCore.createResources(resource);
 		IVirtualComponent component = null;
 		if (resources.length <= 0 || resources[0] == null )
 			return null;
 		component = resources[0].getComponent();
 		String className = getServletClassName(resource);
 		if (className != null && component != null) {
 			String mapping = getServletMapping(resource, true, className, component.getName());
 			if (mapping != null) {
 				return new WebResource(getModule(resource.getProject(), component), new Path(mapping));
 			}
 			// if there is no servlet mapping, provide direct access to the servlet
 			// through the fully qualified class name
 			return new WebResource(getModule(resource.getProject(), component), new Path("servlet/" + className)); //$NON-NLS-1$
 
 		}
 		if (className == null) {
 //			WebArtifactEdit webEdit = null;
 //			try {
 //				webEdit = WebArtifactEdit.getWebArtifactEditForRead(component);
 //				List servlets = webEdit.getWebApp().getServlets();
 //				for (int i=0; i<servlets.size(); i++) {
 //					Servlet servlet = (Servlet) servlets.get(i);
 //					WebType type = servlet.getWebType();
 //					if (type.isJspType()) {
 //						JSPType jsp = (JSPType)type;
 //						String jspPath = resource.getProjectRelativePath().removeFirstSegments(1).toString();
 //						if (jsp.getJspFile().equals(jspPath)) {
 //							List mappings = servlet.getMappings();
 //							String mapping = null;
 //							if (mappings != null && !mappings.isEmpty()) {
 //								ServletMapping map = (ServletMapping) mappings.get(0);
 //								mapping = map.getUrlPattern();
 //								if (mapping != null) 
 //									return new WebResource(getModule(resource.getProject(), component), new Path(mapping));
 //							}
 //						}
 //					}
 //				}
 //			} finally {
 //				if (webEdit != null)
 //					webEdit.dispose();
 //			}
 		}
         resourcePath = resources[0].getRuntimePath();
         
 		try {//adding try/catch to avoid future issues that would require commenting this out.
 			// Extension read to get the correct URL for Java Server Faces file if
 			// the jsp is of type jsfaces.
 			FileURL jspURL = FileURLExtensionReader.getInstance().getFilesURL();
 			if (jspURL != null) {
 				IPath correctJSPPath = jspURL.getFileURL(resource, resourcePath);
 				if (correctJSPPath != null && correctJSPPath.toString().length() > 0)
 					return new WebResource(getModule(resource.getProject(), component), correctJSPPath);
 			}
 		}catch (Exception e) {}
 		
 		return new WebResource(getModule(resource.getProject(), component), resourcePath);
 	}
 
 	protected static IModule getModule(IProject project, IVirtualComponent component) {
 		IModule deployable = null;
 		Iterator iterator = Arrays.asList(ServerUtil.getModules(J2EEProjectUtilities.DYNAMIC_WEB)).iterator(); 
 		String componentName = null;
 		if (component != null)
 			componentName = component.getName();
 		else
 			return getModuleProject(project, iterator);
 		while (iterator.hasNext()) {
 			Object next = iterator.next();
 			if (next instanceof IModule) {
 				deployable = (IModule) next;
 				if (deployable.getName().equals(componentName)) {
 					return deployable;
 				}
 			}
 		}
 		return null;
 	}
 
 	protected static IModule getModuleProject(IProject project, Iterator iterator) {
 		IModule deployable = null;
 		while (iterator.hasNext()) {
 			Object next = iterator.next();
 			if (next instanceof IModule) {
 				deployable = (IModule) next;
 				if (deployable.getProject().equals(project))
 					return deployable;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * Very temporary api - TODO - rip this out by 1.0
 	 */
 	private static boolean isCactusJunitTest(IResource resource) {
 		return getClassNameForType(resource, CACTUS_SERVLET_CLASS_TYPE) != null;
 	}
 
 
 
 	private static IType[] getTypes(IJavaElement element) {
 		try {
 			if (element.getElementType() != IJavaElement.COMPILATION_UNIT)
 				return null;
 
 			return ((ICompilationUnit) element).getAllTypes();
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static String getServletClassName(IResource resource) {
 		return getClassNameForType(resource, GENERIC_SERVLET_CLASS_TYPE);
 	}
 
 	public static String getClassNameForType(IResource resource, String superType) {
 		if (resource == null)
 			return null;
 
 		try {
 			IProject project = resource.getProject();
 			IPath path = resource.getFullPath();
 			if (!project.hasNature(JavaCore.NATURE_ID) || path == null)
 				return null;
 
 			IJavaProject javaProject = (IJavaProject) project.getNature(JavaCore.NATURE_ID);
 			if (!javaProject.isOpen())
 				javaProject.open(new NullProgressMonitor());
 
 			// output location may not be on classpath
 			IPath outputPath = javaProject.getOutputLocation();
 			if (outputPath != null && "class".equals(path.getFileExtension()) && outputPath.isPrefixOf(path)) { //$NON-NLS-1$
 				int count = outputPath.segmentCount();
 				path = path.removeFirstSegments(count);
 			}
 
 			// remove initial part of classpath
 			IClasspathEntry[] classPathEntry = javaProject.getResolvedClasspath(true);
 			if (classPathEntry != null) {
 				int size = classPathEntry.length;
 				for (int i = 0; i < size; i++) {
 					IPath classPath = classPathEntry[i].getPath();
 					if (classPath.isPrefixOf(path)) {
 						int count = classPath.segmentCount();
 						path = path.removeFirstSegments(count);
 						i += size;
 					}
 				}
 			}
 
 			// get java element
 			IJavaElement javaElement = javaProject.findElement(path);
 
 			IType[] types = getTypes(javaElement);
 			if (types != null) {
 				int size2 = types.length;
 				for (int i = 0; i < size2; i++) {
 					if (hasSuperclass(types[i], superType))
 						return types[i].getFullyQualifiedName();
 				}
 			}
 			return null;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static boolean hasSuperclass(IType type, String superClassName) {
 		try {
 			ITypeHierarchy hierarchy = type.newSupertypeHierarchy(null);
 			IType[] superClasses = hierarchy.getAllSuperclasses(type);
 
 			int size = superClasses.length;
 			for (int i = 0; i < size; i++) {
 				if (superClassName.equals(superClasses[i].getFullyQualifiedName()))
 					return true;
 			}
 			return false;
 		} catch (Exception e) {
 			return false;
 		}
 	}
 
 	public static String getServletMapping(IResource resource, boolean isServlet, String typeName, String componentName) {
 		if (typeName == null || typeName.equals("")) //$NON-NLS-1$
 			return null;
 		
 		IModelProvider provider = ModelProviderManager.getModelProvider( resource.getProject() );
 		Object mObj = provider.getModelObject();
 		
 		if ( mObj instanceof org.eclipse.jst.javaee.web.WebApp){
 			org.eclipse.jst.javaee.web.WebApp webApp= (org.eclipse.jst.javaee.web.WebApp) mObj;
 			List servlets = webApp.getServlets();
 			boolean exists = false;
 			// Ensure the display does not already exist in the web application
 			if (servlets != null && !servlets.isEmpty()) {
 				for (int i = 0; i < servlets.size(); i++) {
 					org.eclipse.jst.javaee.web.Servlet servlet = (org.eclipse.jst.javaee.web.Servlet)servlets.get(i);
 					if( servlet.getServletClass().equals(typeName)){
 				
 						java.util.List mappings = webApp.getServletMappings();
 						if (mappings != null && !mappings.isEmpty()) {
 							Iterator it = mappings.iterator();
 							while( it.hasNext() ){
 								org.eclipse.jst.javaee.web.ServletMapping map = (org.eclipse.jst.javaee.web.ServletMapping) it.next();
 								if( map.getServletName().equals(servlet.getServletName())){
 									org.eclipse.jst.javaee.core.UrlPatternType urlPattern = (org.eclipse.jst.javaee.core.UrlPatternType)map.getUrlPatterns().get(0);
 									return  urlPattern.getValue();
 								}
 							}
 
 						}
 					}
 				
 				}
 			}
 
 			
 		}
 		return null;
 
 	}
 
 	protected static boolean hasInterestedComponents(IProject project) {
 		return J2EEProjectUtilities.isDynamicWebProject(project);
 	}
 
 	private static List getServletMappings(IResource resource, String typeName){ 
 		IModelProvider provider = ModelProviderManager.getModelProvider( resource.getProject() );
 		WebApp webApp = (WebApp)provider.getModelObject();
 		
 		List servlets = webApp.getServlets();
 		boolean exists = false;
 		List list = new ArrayList();
 
 		if (servlets != null && !servlets.isEmpty()) {
 			for (int i = 0; i < servlets.size(); i++) {
 				Servlet servlet = (Servlet)servlets.get(i);
 				if( servlet.getServletClass().equals(typeName)){
 			
 					java.util.List mappings = webApp.getServletMappings();
 					if (mappings != null && !mappings.isEmpty()) {
 						Iterator it = mappings.iterator();
 						while( it.hasNext() ){
 							org.eclipse.jst.javaee.web.ServletMapping map = (org.eclipse.jst.javaee.web.ServletMapping) it.next();
							if( map.getServletName().equals(servlet.getServletClass())){
 								list.add(map);
 							}
 						}
 					}
 				}
 			
 			}
 		}
 		return list;
 	}
 }
