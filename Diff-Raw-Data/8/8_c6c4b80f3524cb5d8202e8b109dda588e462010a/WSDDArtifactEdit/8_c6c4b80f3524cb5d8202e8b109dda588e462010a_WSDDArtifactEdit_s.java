 /*******************************************************************************
  * Copyright (c) 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.eclipse.jst.j2ee.internal.webservice.componentcore.util;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceProxy;
 import org.eclipse.core.resources.IResourceProxyVisitor;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.j2ee.componentcore.EnterpriseArtifactEdit;
 import org.eclipse.jst.j2ee.internal.J2EEConstants;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.internal.webservice.plugin.WebServicePlugin;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceExtManager;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper;
 import org.eclipse.jst.j2ee.model.IModelProvider;
 import org.eclipse.jst.j2ee.webservice.wsdd.WebServices;
 import org.eclipse.jst.j2ee.webservice.wsdd.WsddFactory;
 import org.eclipse.jst.j2ee.webservice.wsdd.WsddResource;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.UnresolveableURIException;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
import org.eclipse.wst.common.project.facet.core.IFacetedProject;
import org.eclipse.wst.common.project.facet.core.ProjectFacetsManager;
 
 /**
  * <p>
  * WSDDArtifactEdit obtains a WS Deployment Descriptor metamodel specifec data
  * from a {@see org.eclipse.jst.j2ee.ejb.EJBResource}&nbsp; which stores the
  * metamodel. The {@see org.eclipse.jst.j2ee.ejb.EJBResource}&nbsp;is retrieved
  * from the {@see org.eclipse.wst.common.modulecore.ArtifactEditModel}&nbsp;using
  * a constant {@see J2EEConstants#EJBJAR_DD_URI_OBJ}. The defined methods
  * extract data or manipulate the contents of the underlying resource.
  * </p>
  */
 public class WSDDArtifactEdit extends EnterpriseArtifactEdit {
 
 	/**
 	 * <p>
 	 * Identifier used to link WSDDArtifactEdit to a WsddAdapterFactory {@see
 	 * WsddAdapterFactory} stored in an AdapterManger (@see AdapterManager)
 	 * </p>
 	 */
 
 	public static final Class ADAPTER_TYPE = WSDDArtifactEdit.class;
 
 	public static final String WSIL_FILE_EXT = "wsil"; //$NON-NLS-1$
 
 	public static final String WSDL_FILE_EXT = "wsdl"; //$NON-NLS-1$
 
 	/**
 	 * @param aHandle
 	 * @param toAccessAsReadOnly
 	 * @throws IllegalArgumentException
 	 */
 	public WSDDArtifactEdit(IProject aProject, boolean toAccessAsReadOnly) throws IllegalArgumentException {
 		super(aProject, toAccessAsReadOnly);
 		// TODO Auto-generated constructor stub
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}.
 	 * </p>
 	 * 
 	 * @param anArtifactEditModel
 	 */
 	public WSDDArtifactEdit(ArtifactEditModel model) {
 		super(model);
 	}
 
 	/**
 	 * <p>
 	 * Creates an instance facade for the given {@see ArtifactEditModel}
 	 * </p>
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 * </p>
 	 * 
 	 * @param aNature
 	 *            A non-null {@see ModuleCoreNature}for an accessible project
 	 * @param aModule
 	 *            A non-null {@see WorkbenchComponent}pointing to a module from
 	 *            the given {@see ModuleCoreNature}
 	 */
 	public WSDDArtifactEdit(ModuleCoreNature aNature, IVirtualComponent aModule, boolean toAccessAsReadOnly) {
 		super(aNature, aModule, toAccessAsReadOnly);
 	}
 
 	/**
 	 * @return WsddResource from (@link getDeploymentDescriptorResource())
 	 */
 
 	public WsddResource getWsddXmiResource() {
 		return (WsddResource) getDeploymentDescriptorResource();
 	}
 
 	/**
 	 * <p>
 	 * Retrieves J2EE version information from EJBResource.
 	 * </p>
 	 * 
 	 * @return an integer representation of a J2EE Spec version
 	 */
 
 	public int getJ2EEVersion() {
 		return getWsddXmiResource().getJ2EEVersionID();
 	}
 
 	/**
 	 * <p>
 	 * Retrieves the underlying resource from the ArtifactEditModel using
 	 * defined URI.
 	 * </p>
 	 * 
 	 * @return Resource
 	 */
 
 	public Resource getDeploymentDescriptorResource() {
 		return getArtifactEditModel().getResource(getWebServicesXmlResourceURI());
 	}
 
 	public URI getWebServicesXmlResourceURI() {
 
 		URI resourceURI = J2EEConstants.WEB_SERVICES_WEB_INF_DD_URI_OBJ;
 		if (isValidAppClientModule(getComponent()))
 			resourceURI = J2EEConstants.WEB_SERVICES_META_INF_DD_URI_OBJ;
 		else if (isValidEJBModule(getComponent()))
 			resourceURI = J2EEConstants.WEB_SERVICES_META_INF_DD_URI_OBJ;
 		return resourceURI;
 	}
 
 	/**
 	 * @return WebServices from (@link getDeploymentDescriptorRoot())
 	 */
 	public WebServices getWebServices() {
 		if (!getProject().isAccessible())
 			return null;
 		if (getWsddXmiResource().getContents().isEmpty())
 			return null;
 		return (WebServices) getDeploymentDescriptorRoot();
 	}
 
 	/**
 	 * <p>
 	 * Obtains the WebServices (@see WebServices) root object from the
 	 * WsddResource. If the root object does not exist, then one is created
 	 * (@link addEJBJarIfNecessary(getEJBJarXmiResource())). The root object
 	 * contains all other resource defined objects.
 	 * </p>
 	 * 
 	 * @return EObject
 	 */
 	public EObject getDeploymentDescriptorRoot() {
 		List contents = getDeploymentDescriptorResource().getContents();
 		if (contents.size() > 0)
 			return (EObject) contents.get(0);
 		addWebServicesIfNecessary(getWsddXmiResource());
 		if (contents.isEmpty())
 			return null;
 		return (EObject) contents.get(0);
 	}
 
 	/**
 	 * <p>
 	 * Creates a deployment descriptor root object (WebServices) and populates
 	 * with data. Adds the root object to the deployment descriptor resource.
 	 * </p>
 	 * <p>
 	 * 
 	 * @param aModule
 	 *            A non-null pointing to a {@see XMLResource} Note: This method
 	 *            is typically used for JUNIT - move?
 	 *            </p>
 	 */
 	protected void addWebServicesIfNecessary(WsddResource aResource) {
 		if (aResource != null) {
 			if (aResource.getContents() == null || aResource.getContents().isEmpty()) {
 				WebServices ws = WsddFactory.eINSTANCE.createWebServices();
 				aResource.getContents().add(ws);
 			}
 			aResource.getContents().get(0);
 			getArtifactEditModel().getModuleURI();
 			try {
 				aResource.saveIfNecessary();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the
 	 * given {@see WorkbenchComponent}. Instances of ArtifactEdit that are
 	 * returned through this method must be {@see #dispose()}ed of when no
 	 * longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an ArtifactEdit facade for a specific
 	 * {@see WorkbenchComponent}&nbsp;that will not be used for editing.
 	 * Invocations of any save*() API on an instance returned from this method
 	 * will throw exceptions.
 	 * </p>
 	 * <p>
 	 * <b>The following method may return null. </b>
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that
 	 *            resolves to an accessible project in the workspace
 	 * @return An instance of ArtifactEdit that may only be used to read the
 	 *         underlying content model
 	 */
 	public static WSDDArtifactEdit getWSDDArtifactEditForRead(IProject aProject) {
 		WSDDArtifactEdit artifactEdit = null;
 		IVirtualComponent comp = ComponentCore.createComponent(aProject);
 		if (comp != null && isValidWSDDModule(comp)) {
 			try {
 				artifactEdit = new WSDDArtifactEdit(aProject, true);
 			} catch (IllegalArgumentException iae) {
 				artifactEdit = null;
 			}
 		}
 		return artifactEdit;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the
 	 * given {@see WorkbenchComponent}. Instances of ArtifactEdit that are
 	 * returned through this method must be {@see #dispose()}ed of when no
 	 * longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an ArtifactEdit facade for a specific
 	 * {@see WorkbenchComponent}&nbsp;that will be used for editing.
 	 * </p>
 	 * <p>
 	 * <b>The following method may return null. </b>
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that
 	 *            resolves to an accessible project in the workspace
 	 * @return An instance of ArtifactEdit that may be used to modify and
 	 *         persist changes to the underlying content model
 	 */
 	public static WSDDArtifactEdit getWSDDArtifactEditForWrite(IProject aProject) {
 		WSDDArtifactEdit artifactEdit = null;
 		IVirtualComponent comp = ComponentCore.createComponent(aProject);
 		if (comp != null && isValidWSDDModule(comp)) {
 			try {
 				artifactEdit = new WSDDArtifactEdit(aProject, false);
 			} catch (IllegalArgumentException iae) {
 				artifactEdit = null;
 			}
 		}
 		return artifactEdit;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the
 	 * given {@see WorkbenchComponent}. Instances of WSDDArtifactEdit that are
 	 * returned through this method must be {@see #dispose()}ed of when no
 	 * longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an WSDDArtifactEdit facade for a specific
 	 * {@see WorkbenchComponent}&nbsp;that will not be used for editing.
 	 * Invocations of any save*() API on an instance returned from this method
 	 * will throw exceptions.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that
 	 *            resolves to an accessible project in the workspace
 	 * @return An instance of WSDDArtifactEdit that may only be used to read the
 	 *         underlying content model
 	 * @throws UnresolveableURIException
 	 *             could not resolve uri.
 	 */
 	public static WSDDArtifactEdit getWSDDArtifactEditForRead(IVirtualComponent aModule) {
 		IProject project = aModule.getProject();
 		ModuleCoreNature nature = ModuleCoreNature.getModuleCoreNature(project);
 		if (aModule != null && isValidWSDDModule(aModule))
 			return new WSDDArtifactEdit(nature, aModule, true);
 		return null;
 	}
 
 	/**
 	 * <p>
 	 * Returns an instance facade to manage the underlying edit model for the
 	 * given {@see WorkbenchComponent}. Instances of EJBArtifactEdit that are
 	 * returned through this method must be {@see #dispose()}ed of when no
 	 * longer in use.
 	 * </p>
 	 * <p>
 	 * Use to acquire an WSDDArtifactEdit facade for a specific
 	 * {@see WorkbenchComponent}&nbsp;that will be used for editing.
 	 * </p>
 	 * <p>
 	 * <b>This method may return null. </b>
 	 * </p>
 	 * <p>
 	 * Note: This method is for internal use only. Clients should not call this
 	 * method.
 	 * </p>
 	 * 
 	 * @param aModule
 	 *            A valid {@see WorkbenchComponent}&nbsp;with a handle that
 	 *            resolves to an accessible project in the workspace
 	 * @return An instance of WSDDArtifactEdit that may be used to modify and
 	 *         persist changes to the underlying content model
 	 */
 	public static WSDDArtifactEdit getWSDDArtifactEditForWrite(IVirtualComponent aModule) {
 		IProject project = aModule.getProject();
 		ModuleCoreNature nature = ModuleCoreNature.getModuleCoreNature(project);
 		if (aModule != null && isValidWSDDModule(aModule))
 			return new WSDDArtifactEdit(nature, aModule, false);
 		return null;
 	}
 
 	/**
 	 * @param component
 	 *            A {@see IVirtualComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidEditableModule(IVirtualComponent)}and
 	 *         the moduleTypeId is a JST module
 	 */
 	public static boolean isValidEJBModule(IVirtualComponent aComponent) {
 		return J2EEProjectUtilities.isEJBProject(aComponent.getProject());
 	}
 
 	/**
 	 * @param component
 	 *            A {@see IVirtualComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidWSDDModule(IVirtualComponent)}and the
 	 *         moduleTypeId is a JST module
 	 */
 	protected static boolean isValidWSDDModule(IVirtualComponent aComponent) {
 		return (isValidAppClientModule(aComponent) || isValidWebModule(aComponent) || isValidEJBModule(aComponent));
 	}
 
 	/**
 	 * @param component
 	 *            A {@see IVirtualComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidEditableModule(IVirtualComponent)}and
 	 *         the moduleTypeId is a JST module
 	 */
 	public static boolean isValidWebModule(IVirtualComponent aComponent) {
 		return J2EEProjectUtilities.isDynamicWebProject(aComponent.getProject());
 	}
 
 	/**
 	 * @param component
 	 *            A {@see IVirtualComponent}
 	 * @return True if the supplied module
 	 *         {@see ArtifactEdit#isValidEditableModule(IVirtualComponent)}and
 	 *         the moduleTypeId is a JST module
 	 */
 	public static boolean isValidAppClientModule(IVirtualComponent aComponent) {
 		return J2EEProjectUtilities.isApplicationClientProject(aComponent.getProject());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.modulecore.util.EnterpriseArtifactEdit#createModelRoot()
 	 */
 	public EObject createModelRoot() {
 		return createModelRoot(getJ2EEVersion());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jst.j2ee.internal.modulecore.util.EnterpriseArtifactEdit#createModelRoot(int)
 	 */
 	public EObject createModelRoot(int version) {
 		WsddResource res = (WsddResource) getDeploymentDescriptorResource();
 		res.setModuleVersionID(version);
 		addWebServicesIfNecessary(res);
 		return getWebServices();
 	}
 
 	public EObject getContentModelRoot() {
 		return getWebServices();
 	}
 
 	public List getWSILResources() {
 		final List result = new ArrayList(); 
 		
 		try {
 			getProject().accept(new IResourceProxyVisitor() {
 				
 				public boolean visit(IResourceProxy proxy) throws CoreException {
 					if(proxy.getName().endsWith(WSIL_FILE_EXT)) {
 						IResource file = proxy.requestResource();
 						IVirtualResource[] vResources = ComponentCore.createResources(file);
 						if (vResources.length > 0 && !result.contains(file))
 							result.add(file);
 					}
 					return true;
 				}
 				
 			}, IResource.NONE);
 		} catch (CoreException e) {
 			WebServicePlugin.logError(0, e.getMessage(), e);
 		} 
 		return result;
 	}
 
 	public List getWSDLResources() {
 		return getResources(WSDL_FILE_EXT);
 	}
 
 	private List getResources(String ext) {
 		List resources = getArtifactEditModel().getResources();
 		List result = new ArrayList();
 		for (int i = 0; i < resources.size(); i++) {
 			Resource res = (Resource) resources.get(i);
 			if (res != null && res.getURI().fileExtension() != null && res.getURI().fileExtension().equals(ext))
 				result.add(res);
 		}
 		return result;
 	}
 
 	/**
 	 * return the WSDLResource if it exists, otherwise return null
 	 */
 	public Resource getWsdlResource(String path) {
 		if (path == null || path.equals(""))return null; //$NON-NLS-1$
 		Resource res = null;
 		try {
 			res = getArtifactEditModel().getResource(URI.createURI(path));
 		} catch (Exception e) {
 			// Ignore
 		}
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		if (res != null && res.isLoaded() && serviceHelper != null && serviceHelper.isWSDLResource(res))
 			return res;
 		return null;
 	}
 	public IModelProvider create(IProject project) {
 		return (IModelProvider)getWSDDArtifactEditForRead(project);
 	}
 
 	public IModelProvider create(IVirtualComponent component) {
 		return (IModelProvider)getWSDDArtifactEditForRead(component);
 	}
 
 	// [182417] This ArtifactEdit works for all project versions, so just return true.
 	protected boolean validProjectVersion(IProject project) {
 		return true;
 	}
 }
