 /*******************************************************************************
  * Copyright (c) 2007 Exadel, Inc. and Red Hat, Inc.
  * Distributed under license by Red Hat, Inc. All rights reserved.
  * This program is made available under the terms of the
  * Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Exadel, Inc. and Red Hat, Inc. - initial API and implementation
  ******************************************************************************/ 
 package org.jboss.tools.jst.web.ui.operation;
 
 import java.io.File;
 import java.lang.reflect.InvocationTargetException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Properties;
 import java.util.Set;
 
 import org.eclipse.core.commands.operations.AbstractOperation;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IProjectDescription;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.eclipse.jdt.core.JavaCore;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jface.operation.IRunnableWithProgress;
 import org.eclipse.jst.j2ee.internal.web.archive.operations.WebFacetProjectCreationDataModelProvider;
 import org.eclipse.jst.j2ee.project.facet.IJ2EEModuleFacetInstallDataModelProperties;
 import org.eclipse.jst.j2ee.web.project.facet.IWebFacetInstallDataModelProperties;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties;
 import org.eclipse.wst.common.componentcore.datamodel.properties.IFacetProjectCreationDataModelProperties.FacetDataModelMap;
 import org.eclipse.wst.common.componentcore.internal.operation.FacetProjectCreationOperation;
 import org.eclipse.wst.common.frameworks.datamodel.DataModelFactory;
 import org.eclipse.wst.common.frameworks.datamodel.IDataModel;
 import org.eclipse.wst.common.frameworks.internal.operations.IProjectCreationPropertiesNew;
 import org.eclipse.wst.common.project.facet.core.IFacetedProject;
 import org.eclipse.wst.common.project.facet.core.IProjectFacetVersion;
 import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 import org.eclipse.wst.common.project.facet.core.runtime.RuntimeManager;
 import org.eclipse.wst.server.core.IRuntime;
 import org.jboss.tools.common.meta.action.SpecialWizard;
 import org.jboss.tools.common.meta.action.SpecialWizardFactory;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelConstants;
 import org.jboss.tools.common.model.XModelException;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.filesystems.FileSystemsHelper;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.project.ClassPathUpdate;
 import org.jboss.tools.common.model.project.IModelNature;
 import org.jboss.tools.common.model.project.Watcher;
 import org.jboss.tools.common.model.project.WatcherLoader;
 import org.jboss.tools.common.model.ui.ModelUIPlugin;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.common.util.FileUtil;
 import org.jboss.tools.jst.web.WebModelPlugin;
 import org.jboss.tools.jst.web.context.RegisterServerContext;
 import org.jboss.tools.jst.web.project.helpers.NewWebProjectContext;
 import org.jboss.tools.jst.web.server.RegistrationHelper;
 import org.jboss.tools.jst.web.ui.WebUiPlugin;
 import org.osgi.framework.Bundle;
 
 /**
  * 
  * @author glory
  * 
  */
 public abstract class WebNatureOperation implements IRunnableWithProgress {
 	
 	public static final String DEFAULT_WEB_CONTENT = "WebContent"; //$NON-NLS-1$
 	public static final String DEFAULT_WEB_INF = "WEB-INF"; //$NON-NLS-1$
 	public static final String DEFAULT_JAVA_SOURCES = "JavaSource"; //$NON-NLS-1$
 	public static final String DEFAULT_JAVA_CLASSES = "classes"; //$NON-NLS-1$
 	public static final String DEFAULT_WEB_INF_LIBRARY = "lib"; //$NON-NLS-1$
 	
 	public static final String PROJECT_NAME_ID = "WebNatureOperation.PROJECT_NAME_ID"; //$NON-NLS-1$
 	public static final String PROJECT_LOCATION_ID = "WebNatureOperation.PROJECT_LOCATION_ID"; //$NON-NLS-1$
 	public static final String WEB_CONTENT_ID = "WebNatureOperation.WEB_CONTENT_ID"; //$NON-NLS-1$
 	public static final String WEB_CONTENT_LOCATION_ID = "WebNatureOperation.WEB_CONTENT_LOCATION_ID"; //$NON-NLS-1$
 	public static final String WEB_INF_ID = "WebNatureOperation.WEB_INF_ID"; //$NON-NLS-1$
 	public static final String WEB_INF_LOCATION_ID = "WebNatureOperation.WEB_INF_LOCATION_ID"; //$NON-NLS-1$
 	public static final String WEB_XML_ID = "WebNatureOperation.WEB_XML_ID"; //$NON-NLS-1$
 	public static final String WEB_XML_LOCATION_ID = "WebNatureOperation.WEB_XML_ID"; //$NON-NLS-1$
 	public static final String WEB_INF_LIBRARY_ID = "WebNatureOperation.WEB_INF_LIBRARY_ID"; //$NON-NLS-1$
 	public static final String WEB_INF_LIBRARY_LOCATION_ID = "WebNatureOperation.WEB_INF_LIBRARY_LOCATION_ID"; //$NON-NLS-1$
 	public static final String JAVA_SOURCES_ID = "WebNatureOperation.JAVA_SOURCES_ID"; //$NON-NLS-1$
 	public static final String JAVA_SOURCES_LOCATION_ID = "WebNatureOperation.JAVA_SOURCES_LOCATION_ID"; //$NON-NLS-1$
 	public static final String JAVA_CREATE_SOURCE_FOLDERS_ID = "WebNatureOperation.JAVA_CREATE_SOURCE_FOLDERS_ID"; //$NON-NLS-1$
 	public static final String JAVA_CLASSES_ID = "WebNatureOperation.JAVA_CLASSES_ID"; //$NON-NLS-1$
 	public static final String JAVA_CLASSES_LOCATION_ID = "WebNatureOperation.JAVA_CLASSES_LOCATION_ID"; //$NON-NLS-1$
 	public static final String JAVA_CLASSPATH_ID = "WebNatureOperation.JAVA_CLASSPATH_ID"; //$NON-NLS-1$
 	public static final String SERVLET_VERSION_ID = "WebNatureOperation.SERVLET_VERSION_ID"; //$NON-NLS-1$
 	public static final String JSP_VERSION_ID = "WebNatureOperation.JSP_VERSION_ID"; //$NON-NLS-1$
 	public static final String WEB_VERSION_ID = "WebNatureOperation.WEB_VERSION_ID"; //$NON-NLS-1$
 	public static final String ANT_BUILD_XML_ID = "WebNatureOperation.ANT_BUILD_XML_ID"; //$NON-NLS-1$
 	public static final String USE_DEFAULT_LOCATION_ID = "WebNatureOperation.USE_DEFAULT_LOCATION_ID"; //$NON-NLS-1$
 	public static final String REGISTER_WEB_CONTEXT_ID = "WebNatureOperation.REGISTER_WEB_CONTEXT_ID"; //$NON-NLS-1$
 	public static final String TEMPLATE_ID = "WebNatureOperation.TEMPLATE_ID"; //$NON-NLS-1$
 	public static final String TEMPLATE_VERSION_ID = "WebNatureOperation.TEMPLATE_VERSION_ID"; //$NON-NLS-1$
 	public static final String RUNTIME_NAME = "WebNatureOperation.RUNTIME_NAME"; //$NON-NLS-1$
 	
 	// for STRUTS
 	public static final String TLDS_ID = "WebNatureOperation.TLDS_ID"; //$NON-NLS-1$
 	
 	private static final String PROJECT_ID = "WebNatureOperation.PROJECT_ID"; //$NON-NLS-1$
 
 	protected XModel model;
 	private HashMap propertyValue = new HashMap();
 	private HashMap wizardPropertiesForVelocity = new HashMap();
 	private Properties wizardPropertiesAsIs;
 	RegisterServerContext registry;
 	
 	boolean isCancelled = false;
 	
 	/**
 	 * 
 	 * @param project
 	 * @param projectLocation
 	 * @param registry
 	 * @param properties
 	 */
 	public WebNatureOperation(IProject project, IPath projectLocation, RegisterServerContext registry, Properties properties)	{
 		initDefaults();
 		setProject(project);
 		setProperty(PROJECT_NAME_ID, project.getName());
 		setProperty(PROJECT_LOCATION_ID, projectLocation.toString());
 		setProperty(WEB_CONTENT_LOCATION_ID, projectLocation/*.append(project.getName())*/.append(DEFAULT_WEB_CONTENT).toString());
 		setProperty(WEB_INF_LOCATION_ID, projectLocation.append(project.getName()).append(DEFAULT_WEB_CONTENT).append(DEFAULT_WEB_INF).toString());
 		setProperty(JAVA_CREATE_SOURCE_FOLDERS_ID, Boolean.TRUE);
 		setProperty(JAVA_CLASSES_LOCATION_ID, projectLocation.append(project.getName()).append(DEFAULT_WEB_CONTENT).append(DEFAULT_WEB_INF).append(DEFAULT_JAVA_CLASSES).toString());
 		setProperty(WEB_INF_LIBRARY_LOCATION_ID, projectLocation.append(project.getName()).append(DEFAULT_WEB_CONTENT).append(DEFAULT_WEB_INF).append(DEFAULT_WEB_INF_LIBRARY).toString());
 						
 		setProperty(SERVLET_VERSION_ID, properties.getProperty(NewWebProjectContext.ATTR_SERVLET_VERSION));
 		this.registry = registry;
 		// init wizard properties as is for template processor
 		initWizardPropertiesForVelocity(properties);
 		this.wizardPropertiesAsIs = properties;
 		
 	}
 	
 	public boolean isCancelled() {
 		return isCancelled;
 	}
 	
 	/**
 	 * 
 	 * @param properties
 	 */
 	protected void initWizardPropertiesForVelocity(Properties properties){
 		wizardPropertiesForVelocity.clear();
 		Iterator it = properties.keySet().iterator();
 		while(it.hasNext()) {
 			String key = it.next().toString();
 			Object o = properties.get(key);
 			if(o instanceof XModelObject) {
 				XModelObject object = (XModelObject)o;
 				XModelObject[] cs = object.getChildren();
 				for (int i = 0; i < cs.length; i++) {
 					String n = cs[i].getAttributeValue("name"); //$NON-NLS-1$
 					String v = cs[i].getAttributeValue("value"); //$NON-NLS-1$
 					if(n != null && v != null) {
 						wizardPropertiesForVelocity.put(n.replace(' ', '_'), v);
 					}
 				}
 			} else {
 				wizardPropertiesForVelocity.put(key.replace(' ', '_'), properties.getProperty(key));
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 *
 	 */
 	protected void initDefaults() {
 		setProperty(WEB_CONTENT_ID, DEFAULT_WEB_CONTENT);
 		setProperty(WEB_INF_ID, DEFAULT_WEB_INF);
 		setProperty(WEB_INF_LIBRARY_ID, DEFAULT_WEB_INF_LIBRARY);
 		setProperty(JAVA_SOURCES_ID, DEFAULT_JAVA_SOURCES);
 		setProperty(JAVA_CLASSES_ID, DEFAULT_JAVA_CLASSES);
 	}
 	
 	/**
 	 * 
 	 * @param context
 	 */
 	public WebNatureOperation(NewWebProjectContext context) {
 		this(context.getProject(), context.getLocationPath(), context.getRegisterServerContext(), context.getActionProperties());
 	}
 
 	/**
 	 * 
 	 */
 	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 		if (monitor == null) monitor = new NullProgressMonitor();
 		monitor.beginTask("", 25); //$NON-NLS-1$
 		try {
 			// clear all .-files
 			clearProjectRoot(monitor);
 			monitor.worked(1);
 			if(monitor.isCanceled()) return;
 			// create Dynamic Web Project from WTP: create Java Nature, create WTP Nature
 			AbstractOperation wcco = createWTPNature(monitor);
 			monitor.worked(5);
 			// create lock
 			createLockFile();
 			monitor.worked(1);
 			// create Red Hat Web Nature
 			preCreateWebNature();
 //			createWebNature();
 //			monitor.worked(4);
 			// create Java Nature
 			JavaCore.create(getProject());
 //			createJavaNature(); // create java nature now migrate into create WTP nature @see createWTPNature()
 			monitor.worked(3);
 			// update version of FileSystem object
 //			updateVersion();
 			monitor.worked(1);
 			// refresh project resource
 //			getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 //			monitor.worked(3);
 			// update model
 				createWebNature();
 				monitor.worked(4);
 
 				getProject().refreshLocal(IResource.DEPTH_INFINITE, new NullProgressMonitor());
 				monitor.worked(3);
 
 				updateJavaNature(); // create java nature now migrate into create WTP nature @see createWTPNature()
 				updateVersion();
 
 			model.update();
 			monitor.worked(2);
 			model.save();
 			monitor.worked(1);
 			createWTPNature2(wcco, monitor);
 			monitor.worked(1);
 			// register application
 			registerServer2(monitor);
 			///registerServer(monitor);
 			
 			postCreateWebNature();
 		} catch (CoreException e) {
 			WebModelPlugin.getPluginLog().logError(e);
 		} finally {
 			if(!monitor.isCanceled()) deleteLockFile();
 			monitor.done();
 		}
 
 	}
 	
 	protected String[] dotFilesList = {
 		".classpath", ".project", IModelNature.PROJECT_FILE, ".runtime", ".wtpmodules", ".settings" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
 	};
 	
 	/*
 	 * 
 	 */
 	private void clearProjectRoot(IProgressMonitor monitor) throws CoreException {
 		IProject project = getProject();
 		IPath projectLocation = new Path(getProperty(PROJECT_LOCATION_ID)); // project.getLocation();
 		isCancelled = false;
 		if(!checkOverwrite()) {
 			isCancelled = true;
 			monitor.setCanceled(true);
 			return;
 		}
 		if (!project.exists()) {
 			removeDotFiles(projectLocation);
 		} 
 	}
 	
 	protected void removeDotFiles(IPath projectLocation) {
 		for (int i = 0; i < dotFilesList.length; i++) {
 			File file = projectLocation.append(dotFilesList[i]).toFile();
 			if (file.exists()) FileUtil.remove(file);
 		}
 	}
 
 	/*
 	 * 
 	 */
 	private void updateVersion() throws XModelException {
 		Bundle bundle = ModelUIPlugin.getDefault().getBundle();
 		String version = (String) bundle.getHeaders().get(org.osgi.framework.Constants.BUNDLE_VERSION);
 		model.changeObjectAttribute(
 			FileSystemsHelper.getFileSystems(model),
 			XModelConstants.MODEL_VERSION,
 			version
 		);
 	}
 
 	/*
 	 * 
 	 */
 	private void registerServer2(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
 		XModelObject fs = FileSystemsHelper.getFileSystems(model);
 		try {
 			model.changeObjectAttribute(fs, "application name", registry.getApplicationName()); //$NON-NLS-1$
 		} catch (XModelException e) {
 			throw new InvocationTargetException(e);
 		}
 		fs.setModified(true);
 		model.save();
 		if(registry.isEnabled()) ModelPlugin.getDefault().getWorkbench().getDisplay().asyncExec(new Runnable() {
 			public void run() {
 				RegistrationHelper.runRegisterInServerJob(getProject(), registry.getTargetServers(), null);
 			}
 		});
 	}
 	
 	protected boolean checkOverwrite() {
 		return true;
 	}
 	
 	protected abstract String getNatureID();
 	protected abstract void preCreateWebNature() throws CoreException;
 	protected abstract void createWebNature() throws CoreException;
 	protected abstract void postCreateWebNature();
 	
 	/*
 	 * 
 	 */
 	private void createLockFile() {
 		try {
 			getProject().setSessionProperty(WatcherLoader.LOCK, "true"); //$NON-NLS-1$
 		} catch (CoreException e) {
 			WebUiPlugin.getPluginLog().logError(e);
 		}
 	}
 	
 	/*
 	 * 
 	 */
 	private void deleteLockFile() {
 		try {
 			boolean lock = "true".equals(getProject().getSessionProperty(WatcherLoader.LOCK)); //$NON-NLS-1$
 			if(lock) {
 				getProject().setSessionProperty(WatcherLoader.LOCK, null);
 				if(model != null) {	
 					Watcher.getInstance(model).forceUpdate();
 				}	
 			}
 		} catch (CoreException e) {
 			WebUiPlugin.getPluginLog().logError(e);
 		}
 	}
 
 	/*
 	 * 
 	 */
 	private void updateJavaNature() throws CoreException {
 //		JavaCore.create(getProject());
 		EclipseResourceUtil.addNatureToProject(getProject(), JavaCore.NATURE_ID);
 		SpecialWizard w = SpecialWizardFactory.createSpecialWizard("org.jboss.tools.common.model.project.ClassPathUpdateWizard"); //$NON-NLS-1$
 		Properties p = new Properties();
 		p.put("model", model); //$NON-NLS-1$
 		p.put("classes", new Path(getProperty(JAVA_CLASSES_LOCATION_ID))); //$NON-NLS-1$
 		//webInfLocation.append("classes"));
 		w.setObject(p);
 		w.execute();
 	}
 	
 	/**
 	 * 
 	 * @param monitor
 	 * @throws CoreException
 	 */
 	protected AbstractOperation createWTPNature(IProgressMonitor monitor) throws CoreException {
 
 			boolean exists = getProject().exists();
 			String projectName = getProperty(PROJECT_NAME_ID);
 			String projectLocation = getProperty(PROJECT_LOCATION_ID);
 			if(isLinkingToProjectOutsideWorkspace()) {
 				projectLocation = createLinks(projectLocation);
 			}
 			
 			WebFacetProjectCreationDataModelProvider modelProvider = new WebFacetProjectCreationDataModelProvider();
 			IDataModel dataModel = DataModelFactory.createDataModel(modelProvider);
 
 			boolean hasJSTWebFacet = false;
 			if(exists) {
 				IFacetedProject fp0 = ProjectFacetsManager.create(getProject());
 				exists = fp0 != null;
 				if(exists) {
 					Set<IProjectFacetVersion> vs = fp0.getProjectFacets();
 					if(vs != null) for (IProjectFacetVersion v: vs) {
 						String id = v.getProjectFacet().getId();
 						if("jst.web".equals(id)) { //$NON-NLS-1$
 							hasJSTWebFacet = true;
 						}
 					}
 				}
 			}
 
 			configFacets(dataModel, projectLocation);
 
 			modelProvider.setDataModel(dataModel);
 			FacetProjectCreationOperation wcco = (FacetProjectCreationOperation)modelProvider.getDefaultOperation();
 	
 			wcco.setDataModel(dataModel);
 			dataModel.setProperty(IProjectCreationPropertiesNew.PROJECT_NAME, projectName);
 			dataModel.setProperty(IFacetProjectCreationDataModelProperties.FACET_PROJECT_NAME, projectName);
 			if(!isDefaultLocation(projectLocation)) {
 				dataModel.setProperty(IProjectCreationPropertiesNew.USE_DEFAULT_LOCATION, Boolean.FALSE);
 				dataModel.setProperty(IProjectCreationPropertiesNew.USER_DEFINED_LOCATION, projectLocation);
 			}
 
 			if(!getProject().exists()) {
 				IProjectDescription pd = ModelPlugin.getWorkspace().newProjectDescription(getProject().getName());
 				if(!isDefaultLocation(projectLocation)) {
 					pd.setLocation(new Path(projectLocation));
 				}
 				getProject().create(pd, null);
 				getProject().open(null);
 				JavaCore.create(getProject());
 				EclipseResourceUtil.addNatureToProject(getProject(), JavaCore.NATURE_ID);
 			}
 			if(getProject().exists()) {
 				ModuleCoreNature.addModuleCoreNatureIfNecessary(getProject(), monitor);
 				String emfNature = "org.eclipse.jem.workbench.JavaEMFNature"; //$NON-NLS-1$
 				if(!getProject().hasNature(emfNature)) {
 					ProjectUtilities.addNatureToProject(getProject(), emfNature);
 				}
 			}
 			if(!exists || !hasJSTWebFacet) {
 				return wcco;
 			} else {
 				return null;
 			}
 	}
 
 	protected void configFacets(IDataModel dataModel, String projectLocation) {
 		String webroot = getProperty(WEB_CONTENT_LOCATION_ID);
 		String[] javaRoot = (String[])getPropertyObject(JAVA_SOURCES_LOCATION_ID);
 		String sv = getProperty(SERVLET_VERSION_ID);
 
 		FacetDataModelMap map = (FacetDataModelMap) dataModel.getProperty(IFacetProjectCreationDataModelProperties.FACET_DM_MAP);
 		IDataModel configDM = (IDataModel) map.get("jst.web"); //$NON-NLS-1$
 		IDataModel configJavaDM = (IDataModel) map.get("java"); //$NON-NLS-1$
 		
 		if(sv != null && (sv.indexOf("2.3") >= 0 || sv.indexOf("2.4") >= 0 || sv.indexOf("2.5") >= 0 || sv.indexOf("3.0") >= 0)) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 			configDM.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, sv);
 		}
 		if(configJavaDM != null && sv != null && sv.indexOf("3.0") >= 0) { //$NON-NLS-1$
 			configJavaDM.setProperty(IFacetDataModelProperties.FACET_VERSION_STR, "1.6"); //$NON-NLS-1$
 		}
		String contextRoot = getProperty("WebNatureOperation.CONTEXT_ROOT");
		if(contextRoot != null && contextRoot.length() > 0 && !contextRoot.equals(getProject().getName())) {
			configDM.setProperty(IWebFacetInstallDataModelProperties.CONTEXT_ROOT, contextRoot);
		}
 		
 		if(webroot != null) {
 			int i = webroot.lastIndexOf("/"); //$NON-NLS-1$
 			String webRootName = webroot.substring(i + 1);
 			if(webroot.startsWith(projectLocation.replace('\\', '/') + "/")) { //$NON-NLS-1$
 				webRootName = webroot.substring(projectLocation.length() + 1);
 			}
 			configDM.setProperty(IJ2EEModuleFacetInstallDataModelProperties.CONFIG_FOLDER, webRootName);
 		}
 		if(javaRoot != null && javaRoot.length > 0 && javaRoot[0].length() > 0) {
 			String jr = javaRoot[0].replace('\\', '/');
 			int i = jr.lastIndexOf("/"); //$NON-NLS-1$
 			String javaRootName = javaRoot[0].substring(i + 1);
 			if(jr.startsWith(projectLocation.replace('\\', '/') + "/")) { //$NON-NLS-1$
 				javaRootName = jr.substring(projectLocation.length() + 1);
 			}
 			configDM.setProperty(IWebFacetInstallDataModelProperties.SOURCE_FOLDER, javaRootName);
 		}
 		
 	}
 	
 	//https://bugs.eclipse.org/bugs/show_bug.cgi?id=119066
 	private void createWTPNature2(AbstractOperation wcco, IProgressMonitor monitor) {
 		try {
 			if(wcco != null) wcco.execute(monitor, null);
 			IFacetedProject fp = ProjectFacetsManager.create(getProject());
 			fp.setRuntime(findFacetRuntime(null/*runtime*/), monitor);
 		} catch (Exception e) {
 			WebUiPlugin.getPluginLog().logError(e);
 		}
 		
 	}
 	
 	protected boolean isLinkingToProjectOutsideWorkspace() {
 		return false;
 	}
 		
 	protected boolean hasJavaSource() {
 		return true;
 	}
 	
 	private boolean isDefaultLocation(String projectLocation) {
 		String root = ModelPlugin.getWorkspace().getRoot().getLocation().toString().replace('\\', '/');
 		return (projectLocation.replace('\\','/') + "/").equals(root + "/" + getProject().getName() + "/"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 //		return (projectLocation.replace('\\','/') + "/").startsWith(root + "/" + getProject().getName() + "/");
 	}
 	
 	private String createLinks(String projectLocation) throws CoreException {
 		IProject project = getProject();
 		String root = ModelPlugin.getWorkspace().getRoot().getLocation().toString().replace('\\', '/');
 		if((projectLocation.replace('\\','/') + "/").startsWith(root + "/" + project.getName() + "/")) return projectLocation; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
 
 		String webroot = getProperty(WEB_CONTENT_LOCATION_ID);
 		String[] javaRoot = (String[])getPropertyObject(JAVA_SOURCES_LOCATION_ID);
 
 		String wsProjectLocation = root + "/" + project.getName(); //$NON-NLS-1$
 		if(!project.exists()) {
 			IProjectDescription pd = ModelPlugin.getWorkspace().newProjectDescription(project.getName());
 			project.create(pd, null);
 			project.open(null);
 			JavaCore.create(getProject());
 			EclipseResourceUtil.addNatureToProject(getProject(), JavaCore.NATURE_ID);
 			IJavaProject jp = EclipseResourceUtil.getJavaProject(project);
 			IClasspathEntry entry = new ClassPathUpdate().createNewClasspathEntry(project.getFullPath().append("src"), IClasspathEntry.CPE_SOURCE); //$NON-NLS-1$
 			try {
 				jp.setRawClasspath(new IClasspathEntry[]{entry}, project.getFullPath().append("classes"), null); //$NON-NLS-1$
 			} catch (CoreException e) {
 				WebUiPlugin.getPluginLog().logError(e);
 			}
 		}
 		if(webroot != null) {
 			IFolder f = project.getFolder("WEB-ROOT"); //$NON-NLS-1$
 			if(f.exists()) {
 				setProperty(WEB_CONTENT_LOCATION_ID, "/WEB-ROOT"); //$NON-NLS-1$
 			} else if(project.exists() && webroot.startsWith(project.getLocation().toString().replace('\\', '/') + "/")) {
 				f = project.getFolder(webroot.substring(project.getLocation().toString().replace('\\', '/').length() + 1));
 			}
 			if(!f.exists()) {
 				f = project.getFolder("WEB-ROOT"); //$NON-NLS-1$
 				f.createLink(new Path(webroot), IFolder.FORCE, null);
 				setProperty(WEB_CONTENT_LOCATION_ID, "/WEB-ROOT"); //$NON-NLS-1$
 			}
 		}
 		if(javaRoot != null && javaRoot.length > 0 && javaRoot[0].length() > 0) {
 			IFolder f = project.getFolder("src"); //$NON-NLS-1$
 			if(!f.exists()) f.createLink(new Path(javaRoot[0]), IFolder.FORCE, null);
 			javaRoot[0] = "/src"; //$NON-NLS-1$
 		}
 		
 		return wsProjectLocation;
 	}
 	
 	private org.eclipse.wst.common.project.facet.core.runtime.IRuntime findFacetRuntime(IRuntime runtime) {
 		String runtimeName = getProperty(WebNatureOperation.RUNTIME_NAME);
 		if(runtimeName == null) return null;
 		if(runtime != null) runtimeName = runtime.getName();
 		Set set = RuntimeManager.getRuntimes();
 		Iterator it = set.iterator();
 		while(it.hasNext()) {
 			org.eclipse.wst.common.project.facet.core.runtime.IRuntime r = (org.eclipse.wst.common.project.facet.core.runtime.IRuntime)it.next();
 			if(runtimeName.equals(r.getName())) return r;
 		}
 		return null;
 	}
 
 	/**
 	 * 
 	 * @param sourceDir
 	 * @param targetDir
 	 * @throws Exception
 	 */
 	protected void preprocessTemplate(File sourceDir, File targetDir) throws Exception {
 		TemplatePreprocessor preprocessor = new TemplatePreprocessor();
 		preprocessor.setSourceDir(sourceDir);
 		preprocessor.setTargetDir(targetDir);
 		preprocessor.setParameters(wizardPropertiesForVelocity);
 		preprocessor.execute();
 	}
 
 	/**
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public String getProperty(String key) {
 		return (String)propertyValue.get(key);
 	}
 	
 	/**
 	 * 
 	 * @param key
 	 * @param value
 	 */
 	public void setProperty(String key, Object value) {
 		propertyValue.put(key, value);
 	}
 	
 	/**
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public Object getPropertyObject(String key) {
 		return (Object)propertyValue.get(key);
 	}
 	
 	/**
 	 * 
 	 * @param key
 	 * @param value
 	 */
 	public void setPropertyObject(String key, Object value) {
 		propertyValue.put(key, value);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public IProject getProject() {
 		return (IProject)getPropertyObject(PROJECT_ID);
 	}
 	
 	/**
 	 * 
 	 * @param project
 	 */
 	public void setProject(IProject project) {
 		setPropertyObject(PROJECT_ID, project);
 	}
 	
 	/**
 	 * 
 	 * @return
 	 */
 	public Properties getWizardPropertiesAsIs() {
 		return wizardPropertiesAsIs;
 	}
 
 }
