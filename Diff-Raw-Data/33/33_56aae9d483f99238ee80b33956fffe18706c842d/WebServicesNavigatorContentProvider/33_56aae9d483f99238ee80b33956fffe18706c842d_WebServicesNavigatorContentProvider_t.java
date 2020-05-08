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
 /*
  * Created on Jan 19, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 package org.eclipse.jst.j2ee.internal.webservice;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 
import org.eclipse.core.resources.IWorkspaceRoot;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.emf.common.notify.AdapterFactory;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jface.viewers.TreeViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.jst.common.navigator.internal.providers.CommonAdapterFactoryContentProvider;
 import org.eclipse.jst.j2ee.internal.webservice.helper.WebServiceEvent;
 import org.eclipse.jst.j2ee.internal.webservice.helper.WebServiceManagerListener;
 import org.eclipse.jst.j2ee.internal.webservice.helper.WebServicesManager;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceExtManager;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper;
 import org.eclipse.jst.j2ee.webservice.wsclient.ServiceRef;
 import org.eclipse.jst.j2ee.webservice.wsdd.Handler;
 import org.eclipse.jst.j2ee.webservice.wsdd.PortComponent;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.wst.common.internal.emfworkbench.integration.DynamicAdapterFactory;
 import org.eclipse.wst.common.navigator.internal.provisional.views.INavigatorContentProvider;
 
 /**
  * @author jlanuti
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class WebServicesNavigatorContentProvider extends CommonAdapterFactoryContentProvider implements INavigatorContentProvider, WebServiceManagerListener {
 
 	private WebServicesManager webServicesManager = null;
 	private boolean activityEnabled = false;
 	private WebServiceNavigatorGroup webServiceNavigatorGroup;
 	private WebServiceNavigatorGroupType SERVICES = null;
 	private WebServiceNavigatorGroupType CLIENTS = null;
 	private HashMap HANDLERS = new HashMap();
 	private final static String VIEWER_ID = "org.eclipse.wst.navigator.ui.WTPCommonNavigator";//$NON-NLS-1$
 	
 	private TreeViewer viewer = null;
 	
 	public WebServicesNavigatorContentProvider() {
 		super(createAdapterFactory());
 		WebServicesManager.getInstance().addListener(this);
 		// create the default synchronizer for any web service editor to use with view due
 		// to the usage of seperate edit models.
 		WebServicesNavigatorSynchronizer.createInstance(createAdapterFactory(), this);
 		
 	}
 	
 	/**
 	 * Configure and return a composite adapter factory for our contents
 	 */
 	public static AdapterFactory createAdapterFactory() {
 		return new DynamicAdapterFactory(VIEWER_ID);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentProvider#getChildren(java.lang.Object)
 	 */
 	public Object[] getChildren(Object parentElement) {
 //		if (!NavigatorActivityHelper.isActivityEnabled(getContainingExtension())) {
 //			activityEnabled = false;
 //			return super.getChildren(parentElement);
 //		}
 //		activityEnabled = true;
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
		if (parentElement instanceof IWorkspaceRoot) {
 			return new Object[]{getWebServicesNavigatorGroup(parentElement)};
 		} else if (parentElement instanceof WebServiceNavigatorGroup) {
 			return new Object[]{getServicesGroup(), getClientsGroup()};
 		} else if (parentElement instanceof WebServiceNavigatorGroupType && ((WebServiceNavigatorGroupType) parentElement).isServices()) {
 			List result = new ArrayList();
 			result.addAll(getWebServicesManager().getInternalWSDLServices());
 			result.addAll(getWebServicesManager().getExternalWSDLServices());
 			return result.toArray();
 		} else if (parentElement instanceof WebServiceNavigatorGroupType && ((WebServiceNavigatorGroupType) parentElement).isClients()) {
 			return getWebServicesManager().getAllWorkspaceServiceRefs().toArray();
 		} else if (serviceHelper.isService(parentElement)) {
 			return getServiceLevelNodes(parentElement).toArray();
 		} else if (parentElement instanceof WebServiceNavigatorGroupType && ((WebServiceNavigatorGroupType) parentElement).isHandlers()) {
 			return getHandlerChildren(parentElement).toArray();
 		} else if (parentElement instanceof ServiceRef) {
 			Collection result = new ArrayList();
 			result.add(getHandlersGroup(parentElement));
 			return result.toArray();
 		} else if (parentElement instanceof Handler) {
 			return new ArrayList().toArray();
 		} else if (serviceHelper.isWSDLResource(parentElement))
 			return new ArrayList().toArray();
 		else
 			return super.getChildren(parentElement);
 	}
 
 	private List getServiceLevelNodes(Object parentElement) {
 		List result = new ArrayList();
 		// add service classes
 		if (getWebServicesManager().isServiceInternal((EObject) parentElement) && getWebServicesManager().getServiceImplBean((EObject) parentElement) != null)
 			result.add(getWebServicesManager().getServiceImplBean((EObject) parentElement));
 		// Add handlers
 		if (getWebServicesManager().isServiceInternal((EObject) parentElement))
 			result.add(getHandlersGroup(parentElement));
 		//add wsdl file
 		Resource wsdl = getWebServicesManager().getWSDLResource((EObject) parentElement);
 		if (wsdl != null)
 			result.add(wsdl);
 		return result;
 	}
 
 	private List getHandlerChildren(Object parentElement) {
 		List result = new ArrayList();
 		WebServiceNavigatorGroupType handlersGroup = (WebServiceNavigatorGroupType) parentElement;
 		// handle web service handlers case
 		if (handlersGroup.getWsdlService() != null) {
 			PortComponent port = WebServicesManager.getInstance().getPortComponent(handlersGroup.getWsdlService());
 			if (port != null && port.getHandlers() != null && !port.getHandlers().isEmpty())
 				result.addAll(port.getHandlers());
 		}
 		// handle service ref case
 		else if (handlersGroup.getServiceRef() != null) {
 			result.addAll(handlersGroup.getServiceRef().getHandlers());
 		}
 		return result;
 	}
 
 	/**
 	 * @param parentElement
 	 * @return
 	 */
 	protected WebServiceNavigatorGroup getWebServicesNavigatorGroup(Object parentElement) {
 		if (webServiceNavigatorGroup == null)
			webServiceNavigatorGroup = new WebServiceNavigatorGroup((IWorkspaceRoot) parentElement);
 		return webServiceNavigatorGroup;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentProvider#getParent(java.lang.Object)
 	 */
 	public Object getParent(Object element) {
		if (element instanceof IWorkspaceRoot)
 			return null;
 		else if (element instanceof WebServiceNavigatorGroup)
 			return ((WebServiceNavigatorGroup) element).getRoot();
 		else
 			return super.getParent(element);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentProvider#hasChildren(java.lang.Object)
 	 */
 	public boolean hasChildren(Object element) {
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.navigator.internal.views.navigator.INavigatorContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
 	 *      java.lang.Object, java.lang.Object)
 	 */
 	public void inputChanged(Viewer aViewer, Object oldInput, Object newInput) {
 		// TODO handle change events
 		if (aViewer !=null && aViewer instanceof TreeViewer)
 			viewer = (TreeViewer)aViewer;
 		super.inputChanged(aViewer,oldInput,newInput);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	public void dispose() {
 		super.dispose();
 		WebServicesManager.getInstance().removeListener(this);
 		// dispose current instance of web service editor/explorer synchronizer
 		WebServicesNavigatorSynchronizer.disposeInstance();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.internal.emfworkbench.integration.EditModelListener#editModelChanged(org.eclipse.wst.common.internal.emfworkbench.integration.EditModelEvent)
 	 */
 	public void webServiceManagerChanged(WebServiceEvent anEvent) {
 		if (getViewer()==null) return;
 		Display d = null;
 		try {
 			d = getViewer().getControl().getDisplay();
 		} catch (Exception e) {
 			//Ignore
 		}
 		if (d != Display.getCurrent() & d != null) {
 			d.syncExec(new Runnable() {
 				public void run() {
 					getViewer().refresh(getNavigatorGroup());
 				}
 			});
 		} else
 			getViewer().refresh(getNavigatorGroup()); 
 	}
 	
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
 	 */
 	public Object[] getElements(Object inputElement) {
 		return getChildren(inputElement);
 	}
 
 	/**
 	 * @return Returns the navigatorGroup.
 	 */
 	public WebServiceNavigatorGroup getNavigatorGroup() {
 		if (null == webServiceNavigatorGroup) {
			webServiceNavigatorGroup = new WebServiceNavigatorGroup(ResourcesPlugin.getWorkspace().getRoot());
 		}
 		return webServiceNavigatorGroup;
 	}
 
 	protected WebServicesManager getWebServicesManager() {
 		if (webServicesManager == null)
 			webServicesManager = WebServicesManager.getInstance();
 		return webServicesManager;
 	}
 
 	/**
 	 * @return Returns the activityEnabled.
 	 */
 	public boolean isActivityEnabled() {
 		return activityEnabled;
 	}
 
 	private WebServiceNavigatorGroupType getServicesGroup() {
 		if (SERVICES == null)
 			SERVICES = new WebServiceNavigatorGroupType(WebServiceNavigatorGroupType.SERVICES);
 		return SERVICES;
 	}
 
 	private WebServiceNavigatorGroupType getClientsGroup() {
 		if (CLIENTS == null)
 			CLIENTS = new WebServiceNavigatorGroupType(WebServiceNavigatorGroupType.CLIENTS);
 		return CLIENTS;
 	}
 
 	private WebServiceNavigatorGroupType getHandlersGroup(Object key) {
 		if (key == null)
 			return null;
 		WebServiceNavigatorGroupType handler = null;
 		handler = (WebServiceNavigatorGroupType) HANDLERS.get(key);
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		if (handler == null) {
 			if (serviceHelper.isService(key))
 				handler = new WebServiceNavigatorGroupType(WebServiceNavigatorGroupType.HANDLERS, (EObject) key);
 			else if (key instanceof ServiceRef)
 				handler = new WebServiceNavigatorGroupType(WebServiceNavigatorGroupType.HANDLERS, (ServiceRef) key);
 			if (handler != null)
 				HANDLERS.put(key, handler);
 		}
 		return handler;
 	}
 	/**
 	 * @return Returns the viewer.
 	 */
 	public TreeViewer getViewer() {
 		return viewer;
 	}
 }
