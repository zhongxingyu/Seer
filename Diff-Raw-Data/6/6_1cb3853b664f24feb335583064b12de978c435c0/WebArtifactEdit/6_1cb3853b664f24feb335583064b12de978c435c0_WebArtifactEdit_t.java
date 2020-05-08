 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.web.componentcore.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.Archive;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.CommonarchiveFactory;
 import org.eclipse.jst.j2ee.commonarchivecore.internal.exception.OpenFailureException;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.componentcore.util.EARArtifactEdit;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.componentcore.EnterpriseBinaryComponentHelper;
 import org.eclipse.jst.j2ee.internal.componentcore.WebBinaryComponentHelper;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.internal.web.archive.operations.WebComponentLoadStrategyImpl;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webapplication.WebAppResource;
 import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
 import org.eclipse.jst.j2ee.webapplication.WelcomeFile;
 import org.eclipse.jst.j2ee.webapplication.WelcomeFileList;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.internal.BinaryComponentHelper;
 import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
 import org.eclipse.wst.common.componentcore.internal.StructureEdit;
 import org.eclipse.wst.common.componentcore.internal.impl.ModuleURIUtil;
 import org.eclipse.wst.common.componentcore.internal.util.IArtifactEditFactory;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualReference;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 
 
 
 /**
  * <p>
  * WebArtifactEdit obtains a Web Deployment Descriptor metamodel specifec data from a
  * {@see org.eclipse.jst.j2ee.webapplication.WebAppResource}&nbsp; which stores the metamodel. The
  * {@see org.eclipse.jst.j2ee.webapplication.WebAppResource}&nbsp;is retrieved from the
  * {@see org.eclipse.wst.common.modulecore.ArtifactEditModel}&nbsp;using a constant {@see
  * J2EEConstants#WEBAPP_DD_URI_OBJ}. The defined methods extract data or manipulate the contents of
  * the underlying resource.
  * </p>
  * 
  */
 public class WebArtifactEdit extends EnterpriseArtifactEdit implements IArtifactEditFactory {
 
 	/**
 	 * <p>
 	 * Identifier used to link WebArtifactEdit to a WebEditAdapterFactory {@see
 	 * WebEditAdapterFactory} stored in an AdapterManger (@see AdapterManager)
 	 * </p>
 	 */
 	public static final Class ADAPTER_TYPE = WebArtifactEdit.class;
 	public static final String WEB_CONTENT = "WebContent"; //$NON-NLS-1$
 	public static final String WEB_INF = "WEB-INF"; //$NON-NLS-1$
 	public static final String META_INF = "META-INF"; //$NON-NLS-1$
 
 	public static IPath WEBLIB = new Path("/WEB-INF/lib"); //$NON-NLS-1$
 
 	/**
 	 * 
 	 */
 	public WebArtifactEdit() {
 		super();
 	}
 
 	public WebArtifactEdit(IVirtualComponent aModule) {
 		super(aModule);
 	}
 
 	protected BinaryComponentHelper initBinaryComponentHelper(IVirtualComponent binaryModule) {
 		return new WebBinaryComponentHelper(binaryModule);
 	}
 
 	/**
 	 * @param aHandle
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	public WebArtifactEdit(IProject aProject, boolean toAccessAsReadOnly) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly);
 	}
 
 	/**
 	 * @param aHandle
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	public WebArtifactEdit(IProject aProject, boolean toAccessAsReadOnly, boolean forCreate) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly, forCreate, J2EEProjectUtilities.DYNAMIC_WEB);
 	}
 
 	/**
 	 * @param aHandle
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	protected WebArtifactEdit(IProject aProject, boolean toAccessAsReadOnly, boolean forCreate, String editModelID) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly, forCreate, editModelID);
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the given
 	 * {@see WorkbenchComponent}. Instances of ArtifactEdit that are returned through this method
 	 * must be {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an ArtifactEdit facade for a specific {@see WorkbenchComponent}&nbsp;that
 	 * will not be used for editing. Invocations of any save*() API on an instance returned from
 	 * this method will throw exceptions.
 	 * </p>
 	 * <p>
 	 * <b>The following method may return null. </b>
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that resolves to an
 	 *            accessible project in the workspace
 	 * @return An instance of ArtifactEdit that may only be used to read the underlying content
 	 *         model
 	 */
 	public static WebArtifactEdit getWebArtifactEditForRead(IProject aProject) {
 		WebArtifactEdit artifactEdit = null;
 		try {
 			if (isValidWebModule(ComponentCore.createComponent(aProject)))
 				artifactEdit = new WebArtifactEdit(aProject, true, false);
 		} catch (Exception e) {
 			artifactEdit = null;
 		}
 		return artifactEdit;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the given
 	 * {@see WorkbenchComponent}. Instances of ArtifactEdit that are returned through this method
 	 * must be {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an ArtifactEdit facade for a specific {@see WorkbenchComponent}&nbsp;that
 	 * will be used for editing.
 	 * </p>
 	 * <p>
 	 * <b>The following method may return null. </b>
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that resolves to an
 	 *            accessible project in the workspace
 	 * @return An instance of ArtifactEdit that may be used to modify and persist changes to the
 	 *         underlying content model
 	 */
 	public static WebArtifactEdit getWebArtifactEditForWrite(IProject aProject) {
 		WebArtifactEdit artifactEdit = null;
 		try {
 			if (isValidWebModule(ComponentCore.createComponent(aProject)))
 				artifactEdit = new WebArtifactEdit(aProject, false, false);
 		} catch (Exception e) {
 			artifactEdit = null;
 		}
 		return artifactEdit;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the given
 	 * {@see WorkbenchComponent}. Instances of WebArtifactEdit that are returned through this
 	 * method must be {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an WebArtifactEdit facade for a specific {@see WorkbenchComponent}&nbsp;that
 	 * will not be used for editing. Invocations of any save*() API on an instance returned from
 	 * this method will throw exceptions.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this method.
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that resolves to an
 	 *            accessible project in the workspace
 	 * @return An instance of WebArtifactEdit that may only be used to read the underlying content
 	 *         model
 	 * @throws UnresolveableURIException
 	 *             could not resolve uri.
 	 */
 	public static WebArtifactEdit getWebArtifactEditForRead(IVirtualComponent aModule) {
 		if (aModule == null)
 			return null;
 		if (aModule.isBinary()) {
 			return new WebArtifactEdit(aModule);
 		}
 
 		return getWebArtifactEditForRead(aModule.getProject());
 	}
 
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the given
 	 * {@see WorkbenchComponent}. Instances of WebArtifactEdit that are returned through this
 	 * method must be {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an WebArtifactEdit facade for a specific {@see WorkbenchComponent}&nbsp;that
 	 * will be used for editing.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this method.
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that resolves to an
 	 *            accessible project in the workspace
 	 * @return An instance of WebArtifactEdit that may be used to modify and persist changes to the
 	 *         underlying content model
 	 */
 	public static WebArtifactEdit getWebArtifactEditForWrite(IVirtualComponent aModule) {
 		if (aModule == null || aModule.isBinary())
 			return null;
 		return getWebArtifactEditForWrite(aModule.getProject());
 	}
 
 	/**
 	 * @param module
 	 *            A {@see WorkbenchComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidEditableModule(WorkbenchComponent)}and the moduleTypeId is
 	 *         a JST module
 	 */
 	public static boolean isValidWebModule(IVirtualComponent aModule) throws UnresolveableURIException {
 		if (!isValidEditableModule(aModule))
 			return false;
 		return J2EEProjectUtilities.isDynamicWebProject(aModule.getProject());
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}.
 	 * </p>
 	 * 
 	 * @param anArtifactEditModel
 	 */
 	public WebArtifactEdit(ArtifactEditModel model) {
 		super(model);
 
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}
 	 * </p>
 	 * 
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this method.
 	 * </p>
 	 * 
 	 * @param aNature
 	 *            A non-null {@see ModuleCoreNature}for an accessible project
 	 * @param aModule
 	 *            A non-null {@see WorkbenchComponent}pointing to a module from the given
 	 *            {@see ModuleCoreNature}
 	 */
 	protected WebArtifactEdit(ModuleCoreNature aNature, IVirtualComponent aModule, boolean toAccessAsReadOnly) {
 		super(aNature, aModule, toAccessAsReadOnly);
 	}
 
 
 
 	/**
 	 * <p>
 	 * Retrieves J2EE version information from WebAppResource.
 	 * </p>
 	 * 
 	 * @return an integer representation of a J2EE Spec version
 	 * 
 	 */
 	public int getJ2EEVersion() {
 		return ((WebAppResource) getDeploymentDescriptorResource()).getJ2EEVersionID();
 	}
 
 	/**
 	 * <p>
 	 * Obtains the WebApp (@see WebApp) root object from the WebAppResource. If the root object does
 	 * not exist, then one is created (@link addWebAppIfNecessary(getWebApplicationXmiResource())).
 	 * The root object contains all other resource defined objects.
 	 * </p>
 	 * 
 	 * @return EObject
 	 * 
 	 */
 	public EObject getDeploymentDescriptorRoot() {
 		List contents = getDeploymentDescriptorResource().getContents();
 		if (contents.size() > 0)
 			return (EObject) contents.get(0);
 		if (isBinary()) {
 			return null;
 		}
 		addWebAppIfNecessary((WebAppResource) getDeploymentDescriptorResource());
 		return (EObject) contents.get(0);
 	}
 
 	/**
 	 * <p>
 	 * Retrieves the underlying resource from the ArtifactEditModel using defined URI.
 	 * </p>
 	 * 
 	 * @return Resource
 	 * 
 	 */
 	public Resource getDeploymentDescriptorResource() {
 		if (isBinary()) {
 			return getBinaryComponentHelper().getResource(J2EEConstants.WEBAPP_DD_URI_OBJ);
 		}
 		return getArtifactEditModel().getResource(J2EEConstants.WEBAPP_DD_URI_OBJ);
 	}
 
 	/**
 	 * <p>
 	 * Retrieves Servlet version information derived from the {@see WebAppResource}.
 	 * </p>
 	 * 
 	 * @return an integer representation of a module version
 	 * 
 	 */
 	public int getServletVersion() {
 		return ((WebAppResource) getDeploymentDescriptorResource()).getModuleVersionID();
 	}
 
 	/**
 	 * This method returns the integer representation for the JSP specification level associated
 	 * with the J2EE version for this workbench module. This method will not return null and returns
 	 * 20 as default.
 	 * 
 	 * @see WebArtifactEdit#getServletVersion()
 	 * 
 	 * @return an integer representation of the JSP level
 	 */
 	public int getJSPVersion() {
 		int servletVersion = getServletVersion();
 		if (servletVersion == J2EEVersionConstants.WEB_2_2_ID)
 			return J2EEVersionConstants.JSP_1_1_ID;
 		else if (servletVersion == J2EEVersionConstants.WEB_2_3_ID)
 			return J2EEVersionConstants.JSP_1_2_ID;
 		else
 			return J2EEVersionConstants.JSP_2_0_ID;
 	}
 
 	/**
 	 * <p>
 	 * Creates a deployment descriptor root object (WebApp) and populates with data. Adds the root
 	 * object to the deployment descriptor resource.
 	 * </p>
 	 * 
 	 * <p>
 	 * 
 	 * @param aModule
 	 *            A non-null pointing to a {@see XMLResource}
 	 * @param version
 	 *            Version to be set on resource....if null default is taken
 	 * 
 	 * Note: This method is typically used for JUNIT - move?
 	 * </p>
 	 */
 	protected void addWebAppIfNecessary(XMLResource aResource) {
 		if (isBinary()) {
 			throwAttemptedBinaryEditModelAccess();
 		}
 		if (aResource != null) {
 			if (aResource.getContents() == null || aResource.getContents().isEmpty()) {
 				WebApp webAppNew = WebapplicationFactory.eINSTANCE.createWebApp();
 				aResource.getContents().add(webAppNew);
 				aResource.setModified(true);
 			}
 			WebApp webApp = (WebApp) aResource.getContents().get(0);
 			URI moduleURI = getArtifactEditModel().getModuleURI();
 			try {
 				webApp.setDisplayName(StructureEdit.getDeployedName(moduleURI));
 			} catch (UnresolveableURIException e) {
 				// Ignore
 			}
 			aResource.setID(webApp, J2EEConstants.WEBAPP_ID);
 
 			WelcomeFileList wList = WebapplicationFactory.eINSTANCE.createWelcomeFileList();
 			List files = wList.getFile();
 			WelcomeFile file = WebapplicationFactory.eINSTANCE.createWelcomeFile();
 			file.setWelcomeFile("index.html"); //$NON-NLS-1$
 			files.add(file);
 			file = WebapplicationFactory.eINSTANCE.createWelcomeFile();
 			file.setWelcomeFile("index.htm"); //$NON-NLS-1$
 			files.add(file);
 			file = WebapplicationFactory.eINSTANCE.createWelcomeFile();
 			file.setWelcomeFile("index.jsp"); //$NON-NLS-1$
 			files.add(file);
 			file = WebapplicationFactory.eINSTANCE.createWelcomeFile();
 			file.setWelcomeFile("default.html"); //$NON-NLS-1$
 			files.add(file);
 			file = WebapplicationFactory.eINSTANCE.createWelcomeFile();
 			file.setWelcomeFile("default.htm"); //$NON-NLS-1$
 			files.add(file);
 			file = WebapplicationFactory.eINSTANCE.createWelcomeFile();
 			file.setWelcomeFile("default.jsp"); //$NON-NLS-1$
 			files.add(file);
			
			webApp.setFileList(wList);
			
 			try {
 				aResource.saveIfNecessary();
 			} catch (java.net.ConnectException ex) {
 
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 
 	/**
 	 * This method returns the full path to the deployment descriptor resource for the associated
 	 * workbench module. This method may return null.
 	 * 
 	 * @see WebArtifactEdit#getDeploymentDescriptorResource()
 	 * 
 	 * @return the full IPath for the deployment descriptor resource
 	 */
 	public IPath getDeploymentDescriptorPath() {
 		IFile file = WorkbenchResourceHelper.getFile(getDeploymentDescriptorResource());
 		if (file != null)
 			return file.getFullPath();
 		return null;
 	}
 
 	/**
 	 * This method will retrieve the web app resource, create it if necessary, add get the root
 	 * object, the web app out of that web app resource. It will create the web app instance if need
 	 * be, and add it to the web resource. Then, it returns the web app object as the model root.
 	 * This method will not return null.
 	 * 
 	 * @see EnterpriseArtifactEdit#createModelRoot()
 	 * 
 	 * @return the eObject instance of the model root
 	 */
 	public EObject createModelRoot() {
 		if (isBinary()) {
 			throwAttemptedBinaryEditModelAccess();
 		}
 		return createModelRoot(getJ2EEVersion());
 	}
 
 	/**
 	 * This method will retrieve the web app resource, create it if necessary, add get the root
 	 * object, set version the web app out of that web app resource. It will create the web app
 	 * instance if need be, and add it to the web resource. Then, it returns the web app object as
 	 * the model root. This method will not return null.
 	 * 
 	 * @see EnterpriseArtifactEdit#createModelRoot()
 	 * 
 	 * @return the eObject instance of the model root
 	 */
 	public EObject createModelRoot(int version) {
 		if (isBinary()) {
 			throwAttemptedBinaryEditModelAccess();
 		}
 		WebAppResource res = (WebAppResource) getDeploymentDescriptorResource();
 		res.setModuleVersionID(version);
 		addWebAppIfNecessary(res);
 		return res.getRootObject();
 	}
 
 	/**
 	 * This method will return the list of dependent modules which are utility jars in the web lib
 	 * folder of the deployed path of the module. It will not return null.
 	 * 
 	 * @return array of the web library dependent modules
 	 */
 	public IVirtualReference[] getLibModules() {
 		List result = new ArrayList();
 		IVirtualComponent comp = ComponentCore.createComponent(getProject());
 		IVirtualReference[] refComponents = comp.getReferences();
 		// Check the deployed path to make sure it has a lib parent folder and matchs the web.xml
 		// base path
 		for (int i = 0; i < refComponents.length; i++) {
 			if (refComponents[i].getRuntimePath().equals(WEBLIB))
 				result.add(refComponents[i]);
 		}
 
 		return (IVirtualReference[]) result.toArray(new IVirtualReference[result.size()]);
 	}
 
 	/**
 	 * This method will add the dependent modules from the passed in array to the dependentmodules
 	 * list of the associated workbench module. It will ensure a null is not passed and it will
 	 * ensure the dependent modules are not already in the list.
 	 * 
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this method.
 	 * </p>
 	 * 
 	 * @param libModules
 	 *            array of dependent modules to add as web libraries
 	 */
 	public void addLibModules(ReferencedComponent[] libModules) {
 		// TODO - Need to implement
 		// if (libModules==null)
 		// return;
 		// for (int i=0; i<libModules.length; i++) {
 		// if (!module.getReferencedComponents().contains(libModules[i]))
 		// module.getReferencedComponents().add(libModules[i]);
 		// }
 	}
 
 	/**
 	 * This method will retrieve the context root for this web project's .component file. It is
 	 * meant to handle a standalone web case.
 	 * 
 	 * @return contextRoot String
 	 */
 	public String getServerContextRoot() {
 		return J2EEProjectUtilities.getServerContextRoot(getProject());
 	}
 
 	/**
 	 * This method will retrieve the context root for this web project in the associated parameter's
 	 * application.xml. If the earProject is null, then the contextRoot from the .component of the
 	 * web project is returned.
 	 * 
 	 * @param earProject
 	 * @return contextRoot String
 	 */
 	public String getServerContextRoot(IProject earProject) {
 		if (earProject == null || !J2EEProjectUtilities.isEARProject(earProject))
 			return getServerContextRoot();
 		EARArtifactEdit earEdit = null;
 		String contextRoot = null;
 		try {
 			earEdit = EARArtifactEdit.getEARArtifactEditForRead(earProject);
 			if (earEdit != null)
 				contextRoot = earEdit.getWebContextRoot(getProject());
 		} finally {
 			if (earEdit != null)
 				earEdit.dispose();
 		}
 		return contextRoot;
 	}
 
 	/**
 	 * This method will update the context root for this web project on the EAR which is passed in.
 	 * If no EAR is passed the .component file for the web project will be updated.
 	 * 
 	 * @param earProject
 	 * @param aContextRoot
 	 */
 	public void setServerContextRoot(IProject earProject, String aContextRoot) {
 		if (earProject == null || !J2EEProjectUtilities.isEARProject(earProject))
 			setServerContextRoot(aContextRoot);
 		EARArtifactEdit earEdit = null;
 		try {
 			earEdit = EARArtifactEdit.getEARArtifactEditForWrite(earProject);
 			if (earEdit != null)
 				earEdit.setWebContextRoot(getProject(), aContextRoot);
 		} finally {
 			if (earEdit != null) {
 				earEdit.saveIfNecessary(new NullProgressMonitor());
 				earEdit.dispose();
 			}
 		}
 	}
 
 	/**
 	 * This method sets the context root property on the web project's .component file for the
 	 * standalone case.
 	 * 
 	 * @param contextRoot
 	 *            string
 	 */
 	public void setServerContextRoot(String contextRoot) {
 		J2EEProjectUtilities.setServerContextRoot(getProject(), contextRoot);
 	}
 
 
 	/**
 	 * @return WebApp
 	 */
 	public WebApp getWebApp() {
 
 		return (WebApp) getDeploymentDescriptorRoot();
 	}
 
 
 	public ArtifactEdit createArtifactEditForRead(IVirtualComponent aComponent) {
 		return getWebArtifactEditForRead(aComponent);
 	}
 
 
 	public ArtifactEdit createArtifactEditForWrite(IVirtualComponent aComponent) {
 		return getWebArtifactEditForWrite(aComponent);
 	}
 
 	public Archive asArchive(boolean includeSource) throws OpenFailureException {
 		if (isBinary()) {
 			return ((EnterpriseBinaryComponentHelper) getBinaryComponentHelper()).getArchive();
 		} else {
 			WebComponentLoadStrategyImpl loader = new WebComponentLoadStrategyImpl(getComponent());
 			loader.setExportSource(includeSource);
 			String uri = ModuleURIUtil.getHandleString(getComponent());
 			return CommonarchiveFactory.eINSTANCE.openWARFile(loader, uri);
 		}
 	}
 
 	public static void createDeploymentDescriptor(IProject project, int version) {
 		WebArtifactEdit webEdit = new WebArtifactEdit(project, false, true);
 		try {
 			webEdit.createModelRoot(version);
 			webEdit.save(null);
 		} finally {
 			webEdit.dispose();
 		}
 	}
 }
