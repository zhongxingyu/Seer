 /*******************************************************************************
  * Copyright (c) 2007 Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Red Hat, Inc. - initial API and implementation
  ******************************************************************************/
 package org.jboss.tools.gwt.core;
 
 import java.io.BufferedInputStream;
 import java.io.IOException;
 import java.text.MessageFormat;
 import java.util.List;
 import java.util.zip.ZipInputStream;
 
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspaceRunnable;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.preferences.IEclipsePreferences;
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.model.ModelProviderManager;
 import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
 import org.eclipse.jst.j2ee.webapplication.WelcomeFile;
 import org.eclipse.jst.javaee.core.JavaeeFactory;
 import org.eclipse.jst.javaee.core.UrlPatternType;
 import org.eclipse.jst.javaee.web.Servlet;
 import org.eclipse.jst.javaee.web.ServletMapping;
 import org.eclipse.jst.javaee.web.WebApp;
 import org.eclipse.jst.javaee.web.WebFactory;
 import org.eclipse.jst.javaee.web.WelcomeFileList;
 import org.eclipse.wst.common.project.facet.core.IDelegate;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.jboss.tools.common.EclipseUtil;
 import org.jboss.tools.common.log.LogHelper;
import org.jboss.tools.common.model.project.ProjectHome;
 import org.jboss.tools.gwt.core.internal.GWTCoreActivator;
 import org.jboss.tools.gwt.core.util.ProjectUtils;
 import org.jboss.tools.usage.util.StatusUtils;
 import org.osgi.service.prefs.BackingStoreException;
 
 /**
  * @author adietish
  */
 public class GWTInstallFacetDelegate implements IDelegate {
 
 	public void execute(IProject project, IProjectFacetVersion projectFacetVersion, Object config,
 			IProgressMonitor monitor) throws CoreException {
 		try {
 			IJavaProject javaProject = JavaCore.create(project);
 
 			addNature(javaProject, monitor);
 			addClasspathContainer(javaProject, monitor);
 
//			IPath webContentPath = ProjectHome.getFirstWebContentPath(project);
 			IPath webContentPath = ProjectUtils.getWebContentRootPath(javaProject.getProject());
 			Assert.isTrue(webContentPath != null && !webContentPath.isEmpty(),
 					MessageFormat
 							.format("no web content folder was found in project {0}", javaProject.getElementName()));
 
 			createWebApplicationPreferences(project, webContentPath, javaProject, monitor);
 			configureOutputFolder(webContentPath, javaProject, monitor);
 
 			List<IPath> srcFolderPaths = ProjectUtils.getSourceFolders(javaProject);
 			GWTInstallDataModelProvider dataModel = (GWTInstallDataModelProvider) config;
 			createSample(srcFolderPaths, webContentPath, dataModel, javaProject, monitor);
 
 			configureWebXml(project, monitor);
 		} catch (Exception e) {
 			throw new CoreException(StatusUtils.getErrorStatus(GWTCoreActivator.PLUGIN_ID, "Could not create gwt facet.", e));
 		}
 	}
 
 	private void addNature(IJavaProject javaProject, IProgressMonitor monitor) throws CoreException {
 		monitor.subTask("adding gwt nature");
 
 		EclipseUtil.addNatureToProject(javaProject.getProject(), IGoogleEclipsePluginConstants.GWT_NATURE);
 	}
 
 	private void addClasspathContainer(IJavaProject javaProject, IProgressMonitor monitor) throws CoreException {
 		monitor.subTask("adding gwt container to classpath");
 
 		IClasspathEntry entry = JavaCore.newContainerEntry(new Path(IGoogleEclipsePluginConstants.GWT_CONTAINER_ID),
 				false);
 		ProjectUtils.addClasspathEntry(javaProject, entry, monitor);
 	}
 
 	private void createWebApplicationPreferences(IProject project, IPath webContentPath,
 			IJavaProject javaProject, IProgressMonitor monitor) throws BackingStoreException, CoreException {
 		monitor.subTask("creating web application preferences");
 
 		IScopeContext projectScope = new ProjectScope(project);
 		IEclipsePreferences preferences = projectScope.getNode(IGoogleEclipsePluginConstants.GDT_PLUGIN_ID);
 
 		preferences.put(IGoogleEclipsePluginConstants.WAR_SRCDIR_KEY, 
 				webContentPath.makeRelativeTo(javaProject.getPath()).toString());
 		preferences.put(IGoogleEclipsePluginConstants.WAR_SRCDIR_ISOUTPUT_KEY,
 				IGoogleEclipsePluginConstants.WAR_SRCDIR_ISOUTPUT_DEFAULTVALUE);
 		preferences.flush();
 	}
 
 	private void configureOutputFolder(IPath webContentProjectPath, IJavaProject javaProject, IProgressMonitor monitor)
 			throws CoreException, JavaModelException {
 		monitor.subTask("configuring output folder");
 		IPath outputFolderProjectPath = webContentProjectPath.append(new Path(
 				IGoogleEclipsePluginConstants.OUTPUT_FOLDER_DEFAULTVALUE));
 		IFolder outputWorkspaceFolder = javaProject.getProject().getWorkspace().getRoot().getFolder(
 				outputFolderProjectPath);
 		if (!outputWorkspaceFolder.exists()) {
			outputWorkspaceFolder.create(false, true, monitor);
 		}
 		javaProject.setOutputLocation(outputWorkspaceFolder.getFullPath(), new NullProgressMonitor());
 	}
 
 	private void createSample(final List<IPath> srcPaths, final IPath webContentPath,
 			GWTInstallDataModelProvider dataModel, final IJavaProject javaProject, IProgressMonitor monitor)
 			throws IOException, CoreException {
 
 		if (srcPaths.size() <= 0) {
 			LogHelper.logWarning(GWTCoreActivator.PLUGIN_ID, MessageFormat.format("no source folders were found in project {0}", javaProject.getElementName()));
 			return;
 		}
 
 		if (dataModel.isGenerateSampleCode()) {
 			monitor.subTask("creating sample code");
 
 			javaProject.getProject().getWorkspace().run(new IWorkspaceRunnable() {
 				public void run(IProgressMonitor monitor) throws CoreException {
 					try {
 						/**
 						 * TODO: it is not secure to take the first
 						 * source-folder that was found (there might be several
 						 * of them).
 						 */
 						unzipSrc(srcPaths.get(0), javaProject);
 						unzipWebContent(webContentPath, javaProject);
 
 					} catch (IOException e) {
 						LogHelper.logError(GWTCoreActivator.PLUGIN_ID, "Could not create gwt facet", e);
 					}
 				}
 
 			}, monitor);
 
 			javaProject.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
 		}
 	}
 
 	private void unzipSrc(final IPath srcFolderPath, final IJavaProject javaProject) throws IOException {
 		ZipInputStream inputStream = new ZipInputStream(new BufferedInputStream(
 				ProjectUtils.checkedGetResourceStream(IGoogleEclipsePluginConstants.SAMPLE_HELLO_SRC_ZIP_FILENAME,
 						getClass())));
 		ProjectUtils.unzipToFolder(inputStream, ProjectUtils.getFile(srcFolderPath, javaProject.getProject()));
 	}
 
 	private void unzipWebContent(IPath webContentPath, IJavaProject javaProject) throws IOException {
 		ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(
 				ProjectUtils.checkedGetResourceStream(
 						IGoogleEclipsePluginConstants.SAMPLE_HELLO_WEBCONTENT_ZIP_FILENAME, getClass())));
 		ProjectUtils.unzipToFolder(zipInputStream, ProjectUtils.getFile(webContentPath, javaProject.getProject()));
 	}
 
 	/**
 	 * Configures the web xml. Adds the gwt servlet, servlet mapping and welcome
 	 * page.
 	 * 
 	 * @param project
 	 *            the project to configure the web xml of.
 	 * @param monitor
 	 *            the monitor to inform on progress
 	 */
 	protected void configureWebXml(final IProject project, IProgressMonitor monitor) {
 		IModelProvider modelProvider = ModelProviderManager.getModelProvider(project);
 		Object modelObject = modelProvider.getModelObject();
 		if (!(modelObject instanceof WebApp)) {
 			// TODO log
 			return;
 		}
 
 		monitor.subTask("configuring web.xml");
 
 		IPath webXmlPath = ProjectUtils.getWebXmlPath();
 		boolean exists = project.getProjectRelativePath().append(webXmlPath).toFile().exists();
 		if (!exists) {
 			webXmlPath = IModelProvider.FORCESAVE;
 		}
 		modelProvider.modify(new Runnable() {
 
 			public void run() {
 				IModelProvider modelProvider = ModelProviderManager.getModelProvider(project);
 				Object modelObject = modelProvider.getModelObject();
 				if (!(modelObject instanceof WebApp)) {
 					// TODO log
 					return;
 				}
 				WebApp webApp = (WebApp) modelObject;
 
 				Servlet servlet = createServlet(
 						IGoogleEclipsePluginConstants.SERVLET_NAME,
 						IGoogleEclipsePluginConstants.SERVLET_CLASS,
 						webApp);
 				createServletMapping(
 						IGoogleEclipsePluginConstants.SERVLET_MAPPING,
 						servlet,
 						webApp);
 				addWelcomePage(
 						IGoogleEclipsePluginConstants.WELCOME_FILE,
 						webApp);
 			}
 
 		}, webXmlPath);
 	}
 
 	private void createServletMapping(String urlPattern, Servlet servlet, WebApp webApp) {
 		ServletMapping mapping = WebFactory.eINSTANCE.createServletMapping();
 		mapping.setServletName(servlet.getServletName());
 		UrlPatternType urlPatternType = JavaeeFactory.eINSTANCE.createUrlPatternType();
 		urlPatternType.setValue(urlPattern);
 		mapping.getUrlPatterns().add(urlPatternType);
 		webApp.getServletMappings().add(mapping);
 	}
 
 	private Servlet createServlet(String servletName, String servletClass, WebApp webApp) {
 		Servlet servlet = WebFactory.eINSTANCE.createServlet();
 		servlet.setServletName(servletName);
 		servlet.setServletClass(servletClass);
 		webApp.getServlets().add(servlet);
 		return servlet;
 	}
 
 	private void addWelcomePage(String welcomeFileUrl, WebApp webApp) {
 		List<WelcomeFileList> welcomeList = webApp.getWelcomeFileLists();
 		WelcomeFileList welcomeFileList = null;
 		if (welcomeList.isEmpty()) {
 			welcomeFileList = WebFactory.eINSTANCE.createWelcomeFileList();
 			webApp.getWelcomeFileLists().add(welcomeFileList);
 		} else {
 			welcomeFileList = (WelcomeFileList) welcomeList.get(0);
 		}
 
 		WelcomeFile welcomeFile = WebapplicationFactory.eINSTANCE.createWelcomeFile();
 		welcomeFile.setWelcomeFile(welcomeFileUrl);
 		welcomeFileList.getWelcomeFiles().add(welcomeFileUrl);
 
 	}
 }
