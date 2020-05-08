 /***************************************************************************************************
  * Copyright (c) 2003, 2004 IBM Corporation and others. All rights reserved. This program and the
  * accompanying materials are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors: IBM Corporation - initial API and implementation
  **************************************************************************************************/
 package org.eclipse.jst.j2ee.web.componentcore.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.modulecore.util.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webapplication.WebAppResource;
 import org.eclipse.jst.j2ee.webapplication.WebapplicationFactory;
 import org.eclipse.jst.j2ee.webapplication.WelcomeFile;
 import org.eclipse.jst.j2ee.webapplication.WelcomeFileList;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.StructureEdit;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ReferencedComponent;
 import org.eclipse.wst.common.componentcore.internal.WorkbenchComponent;
 import org.eclipse.wst.common.componentcore.internal.resources.ComponentHandle;
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
  */
 public class WebArtifactEdit extends EnterpriseArtifactEdit {
 
 	/**
 	 * <p>
 	 * Identifier used to link WebArtifactEdit to a WebEditAdapterFactory {@see
 	 * WebEditAdapterFactory} stored in an AdapterManger (@see AdapterManager)
 	 * </p>
 	 */
 	public static final Class ADAPTER_TYPE = WebArtifactEdit.class;
 
 	/**
 	 * <p>
 	 * Identifier used to group and query common artifact edits.
 	 * </p>
 	 */
 	public static String TYPE_ID = "jst.web"; //$NON-NLS-1$
 	
 	private static String LIB = "lib"; //$NON-NLS-1$
 
 	/**
 	 * @param aHandle
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	public WebArtifactEdit(ComponentHandle aHandle, boolean toAccessAsReadOnly) throws IllegalArgumentException {
 		super(aHandle, toAccessAsReadOnly);
 		// TODO Auto-generated constructor stub
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
 	public static WebArtifactEdit getWebArtifactEditForRead(ComponentHandle aHandle) {
 		WebArtifactEdit artifactEdit = null;
 		try {
 			artifactEdit = new WebArtifactEdit(aHandle, true);
 		} catch (IllegalArgumentException iae) {
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
 	public static WebArtifactEdit getWebArtifactEditForWrite(ComponentHandle aHandle) {
 		WebArtifactEdit artifactEdit = null;
 		try {
 			artifactEdit = new WebArtifactEdit(aHandle, false);
 		} catch (IllegalArgumentException iae) {
 			artifactEdit = null;
 		}
 		return artifactEdit;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the given
 	 * {@see WorkbenchComponent}. Instances of WebArtifactEdit that are returned through this method
 	 * must be {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an WebArtifactEdit facade for a specific {@see WorkbenchComponent}&nbsp;that will not
 	 * be used for editing. Invocations of any save*() API on an instance returned from this method
 	 * will throw exceptions.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that resolves to an accessible
 	 *            project in the workspace
 	 * @return An instance of WebArtifactEdit that may only be used to read the underlying content
 	 *         model
 	 * @throws UnresolveableURIException
 	 *             could not resolve uri.
 	 */
 	public static WebArtifactEdit getWebArtifactEditForRead(WorkbenchComponent aModule) {
 		try {
 			if (isValidWebModule(aModule)) {
 				IProject project = StructureEdit.getContainingProject(aModule);
 				ModuleCoreNature nature = ModuleCoreNature.getModuleCoreNature(project);
 				return new WebArtifactEdit(nature, aModule, true);
 			}
 		} catch (UnresolveableURIException uue) {
 			//Ignore
 		}
 		return null;
 	}
 
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the given
 	 * {@see WorkbenchComponent}. Instances of WebArtifactEdit that are returned through this method
 	 * must be {@see #dispose()}ed of when no longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an WebArtifactEdit facade for a specific {@see WorkbenchComponent}&nbsp;that
 	 * will be used for editing.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that resolves to an accessible
 	 *            project in the workspace
 	 * @return An instance of WebArtifactEdit that may be used to modify and persist changes to the
 	 *         underlying content model
 	 */
 	public static WebArtifactEdit getWebArtifactEditForWrite(WorkbenchComponent aModule) {
 		try {
 			if (isValidWebModule(aModule)) {
 				IProject project = StructureEdit.getContainingProject(aModule);
 				ModuleCoreNature nature = ModuleCoreNature.getModuleCoreNature(project);
 				return new WebArtifactEdit(nature, aModule, false);
 			}
 		} catch (UnresolveableURIException uue) {
 			//Ignore
 		}
 		return null;
 	}
 
 	/**
 	 * @param module
 	 *            A {@see WorkbenchComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidEditableModule(WorkbenchComponent)}and the moduleTypeId is a
 	 *         JST module
 	 */
 	public static boolean isValidWebModule(WorkbenchComponent aModule) throws UnresolveableURIException {
 		if (!isValidEditableModule(aModule))
 			return false;
 		/* and match the JST_WEB_MODULE type */
 		if (!TYPE_ID.equals(aModule.getComponentType().getComponentTypeId()))
 			return false;
 		return true;
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
 	 * @param aNature
 	 *            A non-null {@see ModuleCoreNature}for an accessible project
 	 * @param aModule
 	 *            A non-null {@see WorkbenchComponent}pointing to a module from the given
 	 *            {@see ModuleCoreNature}
 	 */
 	public WebArtifactEdit(ModuleCoreNature aNature, WorkbenchComponent aModule, boolean toAccessAsReadOnly) {
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
 		return ((WebAppResource)getDeploymentDescriptorResource()).getJ2EEVersionID();
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
 		addWebAppIfNecessary((WebAppResource)getDeploymentDescriptorResource());
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
 		return ((WebAppResource)getDeploymentDescriptorResource()).getModuleVersionID();
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
 	 * 			Version to be set on resource....if null default is taken
 	 * 
 	 * Note: This method is typically used for JUNIT - move?
 	 * </p>
 	 */
 	protected void addWebAppIfNecessary(XMLResource aResource) {
 		if (aResource != null) {
 		    if(aResource.getContents() == null || aResource.getContents().isEmpty()) {
 		        WebApp webAppNew = WebapplicationFactory.eINSTANCE.createWebApp();
 				aResource.getContents().add(webAppNew);
 				aResource.setModified(true);
 		    } 
 		    WebApp webApp = (WebApp)aResource.getContents().get(0);
 			URI moduleURI = getArtifactEditModel().getModuleURI();
 			try {
 				webApp.setDisplayName(StructureEdit.getDeployedName(moduleURI));
 			} catch (UnresolveableURIException e) {
 				//Ignore
 			}
 			aResource.setID(webApp, J2EEConstants.WEBAPP_ID);
 
 			WelcomeFileList wList = WebapplicationFactory.eINSTANCE.createWelcomeFileList();
 			webApp.setFileList(wList);
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
 			
 			try{
 				aResource.saveIfNecessary();
 			}catch(Exception e){
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	/**
 	 * This method returns the integer representation for the JSP specification level associated with
 	 * the J2EE version for this workbench module.  This method will not return null and returns 20
 	 * as default.
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
 	 * This method returns the full path to the deployment descriptor resource for the associated
 	 * workbench module.  This method may return null.
 	 * 
 	 * @see WebArtifactEdit#getDeploymentDescriptorResource()
 	 * 
 	 * @return the full IPath for the deployment descriptor resource
 	 */
 	public IPath getDeploymentDescriptorPath() {
 		IFile file = WorkbenchResourceHelper.getFile(getDeploymentDescriptorResource());
 		if (file!=null)
 			return file.getFullPath();
 		return null;
 	}
 	
 	/**
 	 * This method will retrieve the web app resource, create it if necessary, add get the root object,
 	 * the web app out of that web app resource.  It will create the web app instance if need be, and add
 	 * it to the web resource.  Then, it returns the web app object as the model root.  This method will
 	 * not return null.
 	 * 
 	 * @see EnterpriseArtifactEdit#createModelRoot()
 	 * 
 	 * @return the eObject instance of the model root
 	 */
 	public EObject createModelRoot() {
 	    return createModelRoot(getJ2EEVersion());
 	}
 	/**
 	 * This method will retrieve the web app resource, create it if necessary, add get the root object, set version
 	 * the web app out of that web app resource.  It will create the web app instance if need be, and add
 	 * it to the web resource.  Then, it returns the web app object as the model root.  This method will
 	 * not return null.
 	 * 
 	 * @see EnterpriseArtifactEdit#createModelRoot()
 	 * 
 	 * @return the eObject instance of the model root
 	 */
 	public EObject createModelRoot(int version) {
 		WebAppResource res = (WebAppResource)getDeploymentDescriptorResource();
 		res.setModuleVersionID(version);
 	    addWebAppIfNecessary(res);
 		return res.getRootObject();
 	}
 	/**
 	 * This method will return the list of dependent modules which are utility jars in the web lib
 	 * folder of the deployed path of the module.  It will not return null.
 	 * 
 	 * @return array of the web library dependent modules
 	 */
 	public ReferencedComponent[] getLibModules() {
 		List result = new ArrayList();
 		List dependentModules = module.getReferencedComponents();
 		// Check the deployed path to make sure it has a lib parent folder and matchs the web.xml base path
 		for (int i=0; i<dependentModules.size(); i++) {
 			ReferencedComponent child = (ReferencedComponent) dependentModules.get(i);
 			URI parentFolderURI = URI.createURI(child.getRuntimePath().removeLastSegments(1).toString());
 			URI webLib = getDeploymentDescriptorResource().getURI().trimSegments(1).appendSegment(LIB);
 			if (parentFolderURI.equals(webLib))
 				result.add(child);
 		}
 		// add results to an array for return
 		ReferencedComponent[] libModules = new ReferencedComponent[result.size()];
 		for (int i=0; i<result.size(); i++) {
 			ReferencedComponent child = (ReferencedComponent) result.get(i);
 			libModules[i] = child;
 		}
 		return libModules;
 	}
 	
 	/**
 	 * This method will add the dependent modules from the passed in array to the dependentmodules list
 	 * of the associated workbench module. It will ensure a null is not passed and it will ensure the
 	 * dependent modules are not already in the list.
 	 * 
 	 * @param libModules array of dependent modules to add as web libraries 
 	 */
 	public void addLibModules(ReferencedComponent[] libModules) {
 		if (libModules==null)
 			return;
 		for (int i=0; i<libModules.length; i++) {
 			if (!module.getReferencedComponents().contains(libModules[i]))
 				module.getReferencedComponents().add(libModules[i]);
 		}
 	}
 	
 	/**
 	 * This method will retrieve the context root for the associated workbench module which is used
 	 * by the server at runtime.  This method is not yet completed as the context root has to be
 	 * abstracted and added to the workbenchModule model.  This API will not change though.
 	 * Returns null for now.
 	 * 
 	 * @return String value of the context root for runtime of the associated module
 	 */
 	public String getServerContextRoot() {
 		//TODO return the valid context root for the module, needs to be be added to the model
 		return null;
 	}
 	
 	/**
 	 * This method will set the context root on the associated workbench module with the given string
 	 * value passed in.  This context root is used by the server at runtime.  This method is not yet
 	 * completed as the context root still needs to be abstracted and added to the workbench module
 	 * model.  This API will not change though.
 	 * Does nothing as of now.
 	 * 
 	 * @param contextRoot string
 	 */
 	public void setServerContextRoot(String contextRoot) {
 		//TODO set the new context root on the module, needs to be added to the model
 	}


	/**
	 * @return WebApp
	 */
	public WebApp getWebApp() {
		
		return (WebApp)getDeploymentDescriptorRoot();
	}
 }
