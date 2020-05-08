 /*******************************************************************************
  * Copyright (c) 2003, 2005 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  * IBM Corporation - initial API and implementation
  *******************************************************************************/
 /*
  * Created on Feb 9, 2004
  *
  * To change the template for this generated file go to
  * Window - Preferences - Java - Code Generation - Code and Comments
  */
 
 
 
 package org.eclipse.jst.j2ee.internal.webservice.helper;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceChangeEvent;
 import org.eclipse.core.resources.IResourceChangeListener;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.common.util.URI;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jem.util.emf.workbench.ProjectUtilities;
 import org.eclipse.jem.util.emf.workbench.WorkbenchResourceHelperBase;
 import org.eclipse.jem.util.logger.proxy.Logger;
 import org.eclipse.jst.j2ee.client.ApplicationClient;
 import org.eclipse.jst.j2ee.client.ApplicationClientResource;
 import org.eclipse.jst.j2ee.ejb.EJBJar;
 import org.eclipse.jst.j2ee.ejb.EJBResource;
 import org.eclipse.jst.j2ee.ejb.EnterpriseBean;
 import org.eclipse.jst.j2ee.internal.J2EEVersionConstants;
 import org.eclipse.jst.j2ee.internal.common.XMLResource;
 import org.eclipse.jst.j2ee.internal.project.J2EEProjectUtilities;
 import org.eclipse.jst.j2ee.internal.webservice.componentcore.util.WSCDDArtifactEdit;
 import org.eclipse.jst.j2ee.internal.webservice.componentcore.util.WSDDArtifactEdit;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceExtManager;
 import org.eclipse.jst.j2ee.internal.webservices.WSDLServiceHelper;
 import org.eclipse.jst.j2ee.webapplication.WebApp;
 import org.eclipse.jst.j2ee.webapplication.WebAppResource;
 import org.eclipse.jst.j2ee.webservice.wsclient.ComponentScopedRefs;
 import org.eclipse.jst.j2ee.webservice.wsclient.ServiceRef;
 import org.eclipse.jst.j2ee.webservice.wsclient.WebServicesClient;
 import org.eclipse.jst.j2ee.webservice.wsclient.WebServicesResource;
 import org.eclipse.jst.j2ee.webservice.wsdd.PortComponent;
 import org.eclipse.jst.j2ee.webservice.wsdd.ServiceImplBean;
 import org.eclipse.jst.j2ee.webservice.wsdd.WebServiceDescription;
 import org.eclipse.jst.j2ee.webservice.wsdd.WebServices;
 import org.eclipse.jst.j2ee.webservice.wsdd.WsddResource;
 import org.eclipse.wst.common.componentcore.ArtifactEdit;
 import org.eclipse.wst.common.componentcore.ComponentCore;
 import org.eclipse.wst.common.componentcore.ModuleCoreNature;
 import org.eclipse.wst.common.componentcore.internal.ArtifactEditModel;
 import org.eclipse.wst.common.componentcore.resources.IVirtualComponent;
 import org.eclipse.wst.common.componentcore.resources.IVirtualResource;
 import org.eclipse.wst.common.internal.emfworkbench.WorkbenchResourceHelper;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModelEvent;
 import org.eclipse.wst.common.internal.emfworkbench.integration.EditModelListener;
 
 /**
  * @author jlanuti
  * 
  * To change the template for this generated type comment go to Window - Preferences - Java - Code
  * Generation - Code and Comments
  */
 public class WebServicesManager implements EditModelListener, IResourceChangeListener, IResourceDeltaVisitor {
 
 	private HashMap wsArtifactEdits = new HashMap();
 	private HashMap wsClientArtifactEdits = new HashMap();
 	private List wsArtifactEditsList;
 	private List wsClientArtifactEditsList;
 	private static WebServicesManager INSTANCE = null;
 	private List listeners;
 	private List removedListeners = new ArrayList();
 	private boolean isNotifying = false;
 	private boolean wsClientElementsChanged = true;
 	private boolean wsElementsChanged = true;
 
 	public static final String WSDL_EXT = "wsdl"; //$NON-NLS-1$
 	public static final String WSIL_EXT = "wsil"; //$NON-NLS-1$
 
 	public synchronized static WebServicesManager getInstance() {
 		if (INSTANCE == null)
 			INSTANCE = new WebServicesManager();
 		return INSTANCE;
 	}
 
 	/**
 	 * Default Constructor
 	 */
 	public WebServicesManager() {
 		super();
 		init();
 	}
 
 	private void init() {
 		collectArtifactEdits();
 		ResourcesPlugin.getWorkspace().addResourceChangeListener(this, IResourceChangeEvent.POST_CHANGE);
 	}
 	
 	private void addArtifactEdit(IProject handle) {
 		synchronized(wsArtifactEdits) {
 			if (!wsArtifactEdits.containsKey(handle)) {
 				ArtifactEdit edit = WSDDArtifactEdit.getWSDDArtifactEditForRead(handle);
 				if (edit != null) {
 					edit.addListener(this);
 					wsArtifactEdits.put(handle, edit);
 					wsElementsChanged = true;
 				}
 			}
 		}
 		synchronized (wsClientArtifactEdits) {
 			if (!wsClientArtifactEdits.containsKey(handle)) {
 				ArtifactEdit edit = WSCDDArtifactEdit.getWSCDDArtifactEditForRead(handle);
 				if (edit != null) {
 					edit.addListener(this);
 					wsClientArtifactEdits.put(handle, edit);
 					wsClientElementsChanged = true;
 				}
 			}
 		}
 	}
 	
 	private void removeArtifactEdit(IProject handle) {
 		synchronized(wsArtifactEdits) {
 			if (wsArtifactEdits.containsKey(handle)) {
 				ArtifactEdit edit = (ArtifactEdit) wsArtifactEdits.get(handle);
 				if (edit != null) {
 					wsArtifactEdits.remove(handle);
 					edit.removeListener(this);
 					edit.dispose();
 					wsElementsChanged = true;
 				}
 			}
 		}
 		synchronized (wsClientArtifactEdits) {
 			if (wsClientArtifactEdits.containsKey(handle)) {
 				ArtifactEdit edit = (ArtifactEdit) wsClientArtifactEdits.get(handle);
 				if (edit != null) {
 					wsClientArtifactEdits.remove(handle);
 					edit.removeListener(this);
 					edit.dispose();
 					wsClientElementsChanged = true;
 				}
 			}
 		}
 	}
 
 	private void collectArtifactEdits() {
 		IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
 		for (int i = 0; i < projects.length; i++) {
 			IProject project = projects[i];
 			IVirtualComponent component = ComponentCore.createComponent(project);
 			try {
 				if (!ModuleCoreNature.isFlexibleProject(project) || J2EEProjectUtilities.isEARProject(project) || J2EEProjectUtilities.isStaticWebProject(project))
 					continue;
 			} catch (Exception e) {
 				continue;
 			}
 			addArtifactEdit(component.getProject());
 		}
 	}
 
 	/**
 	 * @return Returns the artifact edit iterator for web service artifact edits
 	 */
 	private List getWSArtifactEdits() {
 		synchronized (wsArtifactEdits) {
 			if (wsElementsChanged) {
 				wsArtifactEditsList = new ArrayList();
 				wsArtifactEditsList.addAll(wsArtifactEdits.values());
 				wsElementsChanged = false;
 			}
 		}
 		return wsArtifactEditsList;
 	}
 	
 	/**
 	 * @return Returns the editModels.
 	 */
 	private List getWSClientArtifactEdits() {
 		synchronized (wsClientArtifactEdits) {
 			if (wsClientElementsChanged) {
 				wsClientArtifactEditsList = new ArrayList();
 				wsClientArtifactEditsList.addAll(wsClientArtifactEdits.values());
 				wsClientElementsChanged = false;
 			}
 		}
 		return wsClientArtifactEditsList;
 	}
 
 	private List getListeners() {
 		if (listeners == null)
 			listeners = new ArrayList();
 		return listeners;
 	}
 
 	/**
 	 * Add aListener to the list of listeners.
 	 */
 	public void addListener(WebServiceManagerListener aListener) {
 		if (aListener != null && !getListeners().contains(aListener))
 			getListeners().add(aListener);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.wst.common.internal.emfworkbench.integration.EditModelListener#editModelChanged(org.eclipse.wst.common.internal.emfworkbench.integration.EditModelEvent)
 	 */
 	public void editModelChanged(EditModelEvent anEvent) {
 		if (anEvent == null)
 			return;
 		if (anEvent.getEventCode()==EditModelEvent.UNLOADED_RESOURCE) {
 			List resources = anEvent.getChangedResources();
 			for (int i=0; i<resources.size(); i++) {
 				Resource res = (Resource) resources.get(i);
 				if (isInterestedInResource(res))
 					notifyListeners(anEvent.getEventCode());
 			}
 		}
 		else if (anEvent.getEventCode() == EditModelEvent.PRE_DISPOSE) {
 			ArtifactEditModel editModel = (ArtifactEditModel) anEvent.getEditModel();
 			if (editModel == null || editModel.getProject() == null)
 				return;
 			removeArtifactEdit(editModel.getProject());
 			notifyListeners(anEvent.getEventCode());
 		}
 	}
 
 
 	/**
 	 * Notify listeners of
 	 * 
 	 * @anEvent.
 	 */
 	protected void notifyListeners(int anEventType) {
 		if (listeners == null)
 			return;
 		synchronized (this) {
 			isNotifying = true;
 		}
 		try {
 			WebServiceEvent webServiceEvent = null;
 			
 			switch (anEventType) {
 			case EditModelEvent.UNLOADED_RESOURCE:
 			case EditModelEvent.PRE_DISPOSE:
 			case EditModelEvent.REMOVED_RESOURCE:
 				webServiceEvent = new WebServiceEvent(WebServiceEvent.REMOVE);
 				break;
 			default:
 				if (!getAllWorkspaceServiceRefs().isEmpty() || !getAllWSDLServices().isEmpty())
 					webServiceEvent = new WebServiceEvent(WebServiceEvent.REFRESH);
 			}
 			List list = getListeners();
 			if (webServiceEvent!=null) {
 				for (int i = 0; i < list.size(); i++) {
 					((WebServiceManagerListener) list.get(i)).webServiceManagerChanged(webServiceEvent);
 				}
 			}
 		} finally {
 			synchronized (this) {
 				isNotifying = false;
 				if (removedListeners != null && !removedListeners.isEmpty()) {
 					for (int i = 0; i < removedListeners.size(); i++)
 						listeners.remove(removedListeners.get(i));
 					removedListeners.clear();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Remove aListener from the list of listeners.
 	 */
 	public synchronized boolean removeListener(WebServiceManagerListener aListener) {
 		if (aListener != null) {
 			if (isNotifying)
 				return removedListeners.add(aListener);
 			return getListeners().remove(aListener);
 		}
 		return false;
 	}
 
 	private void releaseArtifactEdits() {
 		synchronized (wsArtifactEdits) {
 			Iterator iter = wsArtifactEdits.values().iterator();
 			while (iter.hasNext()) {
 				WSDDArtifactEdit artifactEdit = (WSDDArtifactEdit) iter.next();
 				artifactEdit.removeListener(this);
 				artifactEdit.dispose();
 			}
 			wsArtifactEdits.clear();
 		}
 		
 		synchronized (wsClientArtifactEdits) {
 			Iterator iter = wsClientArtifactEdits.values().iterator();
 			while (iter.hasNext()) {
 				WSCDDArtifactEdit artifactEdit = (WSCDDArtifactEdit) iter.next();
 				artifactEdit.removeListener(this);
 				artifactEdit.dispose();
 			}
 			wsClientArtifactEdits.clear();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
 	 */
 	public void resourceChanged(IResourceChangeEvent event) {
 		acceptDelta(event);
 	}
 
 
 	protected void acceptDelta(IResourceChangeEvent event) {
 		IResourceDelta delta = event.getDelta();
 		// search for changes to any projects using a visitor
 		if (delta != null) {
 			try {
 				delta.accept(this);
 			} catch (Exception e) {
 				Logger.getLogger().logError(e);
 			}
 		}
 	}
 
 	/**
 	 * returns a list of internal web services descriptions in the workspace
 	 */
 	public List getInternalWebServicesDescriptions() {
 		List result = new ArrayList();
 		List webServices = getInternalWebServices();
 		for (int i = 0; i < webServices.size(); i++) {
 			WebServices webService = (WebServices) webServices.get(i);
 			if (webService != null)
 				result.addAll(webService.getWebServiceDescriptions());
 		}
 		return result;
 	}
 
 	/**
 	 * @return all internal web services instances in workspace
 	 */
 	public List getInternalWebServices() {
 		List result = new ArrayList();
 		Iterator iter = getWSArtifactEdits().iterator();
 		while (iter.hasNext()) {
 			WSDDArtifactEdit artifactEdit = (WSDDArtifactEdit) iter.next();
 			WebServices webServices = artifactEdit.getWebServices();
 			if (webServices != null)
 				result.add(webServices);
 		}
 		return result;
 	}
 
 	/**
 	 * returns a list of all the internal wsdl services in wsdl's pointed to by wsdd's
 	 */
 	public List getInternalWSDLServices() {
 		return getWSDLServicesFromWSDLResources(getInternalWSDLResources());
 	}
 
 	public List getInternalWSDLResources() {
 		List result = new ArrayList();
 		List wsddWebServices = getInternalWebServicesDescriptions();
 		for (int i = 0; i < wsddWebServices.size(); i++) {
 			WebServiceDescription webServices = (WebServiceDescription) wsddWebServices.get(i);
 			Resource wsdl = getWSDLResource(webServices);
 			if (wsdl != null && !result.contains(wsdl))
 				result.add(wsdl);
 		}
 		return result;
 	}
 
 		public List getExternalWSDLResources() {
 			//TODO fix up for basis off .wsil
 			List result = getWorkspaceWSDLResources();
 			result.removeAll(getInternalWSDLResources());
 			List serviceRefs = getAllWorkspaceServiceRefs();
 			for (int i=0; i<serviceRefs.size(); i++) {
 				ServiceRef ref = (ServiceRef) serviceRefs.get(i);
 				try {
 					Resource res = WorkbenchResourceHelperBase.getResource(URI.createURI(ref.getWsdlFile()), true);
 					if (res !=null && result.contains(res))
 						result.remove(res);
 				} catch (Exception e) {
 					//Ignore
 				}
 			}
 			return result;
 		}
 		
 	public boolean isServiceInternal(EObject service) {
 		return getInternalWSDLResources().contains(getWSDLResource(service));
 	}
 
 	private List getWSDLServicesFromWSDLResources(List wsdlResources) {
 		List result = new ArrayList();
 		for (int i = 0; i < wsdlResources.size(); i++) {
 			Resource wsdl = (Resource) wsdlResources.get(i);
 			List services = getWSDLServices(wsdl);
 			if (wsdl != null && services != null && !services.isEmpty())
 				result.addAll(services);
 		}
 		return result;
 	}
 
 	public List getExternalWSDLServices() {
 		List result = getWsdlServicesFromWorkspaceWSILs();
 		result.removeAll(getInternalWSDLServices());
 		return result;
 	}
 
 	public List getWsdlServicesFromWorkspaceWSILs() {
 		List result = new ArrayList();
 		List wsilFiles = getWorkspaceWSILFiles();
 		for (int i = 0; i < wsilFiles.size(); i++) {
 			IFile wsil = (IFile) wsilFiles.get(i);
 			List services = getWsdlServicesFromWsilFile(wsil);
 			if (!services.isEmpty())
 				result.addAll(services);
 		}
 	return result;
 	}
 
 	public List getWsdlServicesFromWsilFile(IFile wsil) {
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		return serviceHelper.getWsdlServicesFromWsilFile(wsil);
 	}
 
 	/**
 	 * Returns all WSDL Services, both internal and external
 	 */
 	public List getAllWSDLServices() {
 		List result = new ArrayList();
 		result.addAll(getInternalWSDLServices());
 		result.addAll(getExternalWSDLServices());
 		return result;
 	}
 
 	protected void dispose() {
 		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
 		releaseArtifactEdits();
 		INSTANCE = null;
 	}
 
 	public Resource getWSDLResource(WebServiceDescription webService) {
 		if (webService == null)
 			return null;
 		String wsdlFileName = webService.getWsdlFile();
 		Resource res = null;
 		IVirtualResource[] resources = ComponentCore.createResources(WorkbenchResourceHelper.getFile(webService));
		if (resources == null) return res;
 		WSDDArtifactEdit artifactEdit = getExistingWSDDArtifactEdit(resources[0].getComponent().getProject());
 		if (artifactEdit!=null) 
 			res = artifactEdit.getWsdlResource(wsdlFileName);
 		return res;
 	}
 	
 	private WSDDArtifactEdit getExistingWSDDArtifactEdit(IProject project) {
 		List wsEdits = getWSArtifactEdits();
 		for (int i=0; i<wsEdits.size(); i++) {
 			WSDDArtifactEdit edit = (WSDDArtifactEdit) wsEdits.get(i);
 			if (edit !=null && edit.getProject()!= null && edit.getProject().equals(project))
 				return edit;
 		}
 		return null;
 	}
 	
 	private WSCDDArtifactEdit getExistingWSCDDArtifactEdit(IProject project) {
 		List wsClientEdits = getWSClientArtifactEdits();
 		for (int i=0; i<wsClientEdits.size(); i++) {
 			WSCDDArtifactEdit edit = (WSCDDArtifactEdit) wsClientEdits.get(i);
 			if (edit !=null && edit.getProject()!= null && edit.getProject().equals(project))
 				return edit;
 		}
 		return null;
 	}
 
 	public List getWSDLServices(Resource wsdl) {
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		List result = new ArrayList();
 		Object def = serviceHelper.getWSDLDefinition(wsdl);
 		if (def == null)
 			return result;
 		result = new ArrayList(serviceHelper.getDefinitionServices(def).values());
 		return result;
 	}
 
 	public EObject getWSDLServiceForWebService(WebServiceDescription webService) {
 		EObject service = null;
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		Resource wsdl = getWSDLResource(webService);
 		if (wsdl == null) return service;
 		Object definition = serviceHelper.getWSDLDefinition(wsdl);
 		if (definition == null) return service;
 		Map services = serviceHelper.getDefinitionServices(definition);
 		if (services.isEmpty()) return service;
 		PortComponent portComp = null;
 		if (webService.getPortComponents()!=null && webService.getPortComponents().size()>0) {
 			portComp = (PortComponent) webService.getPortComponents().get(0);
 			return getService(portComp);
 		}
 		return service;
 	}
 
 	public Resource getWSDLResource(EObject wsdlService) {
 		return wsdlService.eResource();
 	}
 
 	public EObject getService(PortComponent port) {
 		List services = getInternalWSDLServices();
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		for (int i = 0; i < services.size(); i++) {
 			EObject service = (EObject)services.get(i);
 			if (serviceHelper.getServicePorts(service).size() == 1) {
 				Object wsdlPort = serviceHelper.getServicePorts(service).values().toArray()[0];
 				String qName = serviceHelper.getPortBindingNamespaceURI(wsdlPort);
 				if (port.getWsdlPort().getNamespaceURI().equals(qName))
 					return service;
 			}
 		}
 		return null;
 	}
 
 	public PortComponent getPortComponent(String qName) {
 		List wsDescs = getInternalWebServicesDescriptions();
 		for (int i = 0; i < wsDescs.size(); i++) {
 			WebServiceDescription wsDesc = (WebServiceDescription) wsDescs.get(i);
 			List ports = wsDesc.getPortComponents();
 			for (int j = 0; j < ports.size(); j++) {
 				PortComponent portComp = (PortComponent) ports.get(j);
 				if (portComp.getWsdlPort().getNamespaceURI().equals(qName))
 					return portComp;
 			}
 		}
 		return null;
 	}
 
 	public PortComponent getPortComponent(String qName, IProject project) {
 		List wsDescs = getInternalWebServicesDescriptions();
 		for (int i = 0; i < wsDescs.size(); i++) {
 			WebServiceDescription wsDesc = (WebServiceDescription) wsDescs.get(i);
 			List ports = wsDesc.getPortComponents();
 			for (int j = 0; j < ports.size(); j++) {
 				PortComponent portComp = (PortComponent) ports.get(j);
 				if (project == ProjectUtilities.getProject(portComp))
 					return portComp;
 			}
 		}
 		return null;
 	}
 
 	public PortComponent getPortComponent(EObject wsdlService) {
 		// If there is only one port in the wsdl service, find the matching port component
 		// otherwise if multiple ports return null because we need more information
 		WSDLServiceHelper serviceHelper = WSDLServiceExtManager.getServiceHelper();
 		if (wsdlService == null || serviceHelper.getServicePorts(wsdlService).isEmpty())
 			return null;
 		if (serviceHelper.getServicePorts(wsdlService).size() == 1) {
 			Object port = serviceHelper.getServicePorts(wsdlService).values().toArray()[0];
 			String qName = serviceHelper.getPortBindingNamespaceURI(port);
 			return getPortComponent(qName, ProjectUtilities.getProject(wsdlService));
 		}
 		return null;
 	}
 
 	public ServiceImplBean getServiceImplBean(EObject wsdlService) {
 		PortComponent port = getPortComponent(wsdlService);
 		if (port == null)
 			return null;
 		return port.getServiceImplBean();
 	}
 
 	public WsddResource getWsddResource(EObject wsdlService) {
 		PortComponent port = getPortComponent(wsdlService);
 		if (port == null)
 			return null;
 		return (WsddResource) port.eResource();
 	}
 
 	public String getServiceEndpointInterface(EObject wsdlService) {
 		PortComponent port = getPortComponent(wsdlService);
 		if (port == null)
 			return null;
 		return port.getServiceEndpointInterface();
 	}
 
 	public List getAllWorkspaceServiceRefs() {
 		List result = new ArrayList();
 		result.addAll(getWorkspace13ServiceRefs());
 		result.addAll(getWorkspace14ServiceRefs());
 		return result;
 	}
 
 	public List getWorkspace13ServiceRefs() {
 		List result = new ArrayList();
 		Iterator iter = getWSClientArtifactEdits().iterator();
 		while (iter.hasNext()) {
 			WSCDDArtifactEdit artifactEdit = (WSCDDArtifactEdit) iter.next();
 			WebServicesResource res = artifactEdit.getWscddXmiResource();
 			if (res != null && res.isLoaded() && res.getWebServicesClient() != null) {
 				if (J2EEProjectUtilities.isEJBProject(artifactEdit.getProject())) {
 					List scopedBeans = res.getWebServicesClient().getComponentScopedRefs();
 					for (Iterator iterator = scopedBeans.iterator(); iterator.hasNext();) {
 						ComponentScopedRefs refBean = (ComponentScopedRefs) iterator.next();
 						result.addAll(refBean.getServiceRefs());
 					}
 				} else	result.addAll(res.getWebServicesClient().getServiceRefs());
 			}
 		}
 		return result;
 	}
 
 	public List get13ServiceRefs(IProject handle) {
 		List result = new ArrayList();
 		WSCDDArtifactEdit wsClientArtifactEdit = getExistingWSCDDArtifactEdit(handle);
 		if (wsClientArtifactEdit !=null) {
 			WebServicesResource res = wsClientArtifactEdit.getWscddXmiResource();
 			if (res != null && res.isLoaded() && res.getWebServicesClient() != null)
 				result.addAll(res.getWebServicesClient().getServiceRefs());
 		}
 		return result;
 	}
 
 	public List getWorkspace14ServiceRefs() {
 		List result = new ArrayList();
 		Iterator iter = getWSClientArtifactEdits().iterator();
 		while (iter.hasNext()) {
 			WSCDDArtifactEdit wscArtifactEdit = (WSCDDArtifactEdit) iter.next();
 			ArtifactEdit artifactEdit = ArtifactEdit.getArtifactEditForRead(wscArtifactEdit.getProject());
 			try {
 				EObject rootObject = null;
 				if (artifactEdit!=null)
 					rootObject = artifactEdit.getContentModelRoot();
 				// handle EJB project case
 				if (rootObject instanceof EJBJar) {
 					List cmps = ((EJBJar) rootObject).getEnterpriseBeans();
 					for (int j = 0; j < cmps.size(); j++) {
 						EnterpriseBean bean = (EnterpriseBean) cmps.get(j);
 						if (bean.getServiceRefs() != null && !bean.getServiceRefs().isEmpty())
 							result.addAll(bean.getServiceRefs());
 					}
 				}
 				// handle Web Project
 				else if (rootObject instanceof WebApp) {
 					if (((WebApp) rootObject).getServiceRefs() != null && !((WebApp) rootObject).getServiceRefs().isEmpty())
 						result.addAll(((WebApp) rootObject).getServiceRefs());
 				}
 				// handle App clients
 				else if (rootObject instanceof ApplicationClient) {
 					if (((ApplicationClient) rootObject).getServiceRefs() != null && !((ApplicationClient) rootObject).getServiceRefs().isEmpty())
 						result.addAll(((ApplicationClient) rootObject).getServiceRefs());
 				}
 			} finally {
 				if (artifactEdit != null)
 					artifactEdit.dispose();
 			}
 		}
 		return result;
 	}
 
 	public boolean isJ2EE14(ServiceRef ref) {
 		return !(ref.eContainer() instanceof WebServicesClient);
 	}
 
 	public List getWorkspaceWSILFiles() {
 		List result = new ArrayList();
 		Iterator iter = getWSArtifactEdits().iterator();
 		while (iter.hasNext()) {
 			WSDDArtifactEdit artifactEdit = (WSDDArtifactEdit) iter.next();
 			List files = artifactEdit.getWSILResources();
 			for (int j = 0; j < files.size(); j++) {
 				IFile file = (IFile) files.get(j);
 				if (file != null && WSIL_EXT.equals(file.getFileExtension()))
 					result.add(file);
 			}
 		}
 		return result;
 	}
 
 	public List getWorkspaceWSDLResources() {
 		List result = new ArrayList();
 		Iterator iter = getWSArtifactEdits().iterator();
 		while (iter.hasNext()) {
 			WSDDArtifactEdit artifactEdit = (WSDDArtifactEdit) iter.next();
 			IProject project = artifactEdit.getProject();
 			if (project != null) {
 				List wsdlResources = artifactEdit.getWSDLResources();
 				if (wsdlResources != null && !wsdlResources.isEmpty()) {
 					for (int j = 0; j < wsdlResources.size(); j++) {
 						Resource wsdl = (Resource) wsdlResources.get(j);
 						if (!result.contains(wsdl))
 							result.add(wsdl);
 					}
 				}
 			}
 		}
 		return result;
 	}
 
 	public List getWSDLServices() {
 		List result = new ArrayList();
 		List internalWsdls = getInternalWSDLServices();
 		if (internalWsdls != null && !internalWsdls.isEmpty())
 			result.addAll(internalWsdls);
 		//TODO add externals
 		return result;
 	}
 
 	/**
 	 * @param bean
 	 * @return
 	 */
 	public List get13ServiceRefs(EnterpriseBean bean) {
 		WSCDDArtifactEdit artifactEdit = null;
 		IFile file = WorkbenchResourceHelper.getFile(bean);
 		if (file!=null)
 			artifactEdit = getExistingWSCDDArtifactEdit(file.getProject());
 		if (artifactEdit !=null) {
 			WebServicesResource res = artifactEdit.getWscddXmiResource();
 			if (res != null && res.getWebServicesClient() != null) {
 				String ejbName = bean.getName();
 				List scopes = res.getWebServicesClient().getComponentScopedRefs();
 				for (Iterator iter = scopes.iterator(); iter.hasNext();) {
 					ComponentScopedRefs scope = (ComponentScopedRefs) iter.next();
 					if (scope.getComponentName().equals(ejbName))
 						return scope.getServiceRefs();
 				}
 			}
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @param client
 	 * @return
 	 */
 	public List get13ServiceRefs(ApplicationClient client) {
 		IProject handle = getComponentProject(WorkbenchResourceHelper.getFile(client));
 		if (handle == null)
 			return Collections.EMPTY_LIST;
 		WSCDDArtifactEdit artifactEdit = getExistingWSCDDArtifactEdit(handle);
 		if (artifactEdit !=null) {
 			WebServicesResource res = artifactEdit.getWscddXmiResource();
 			if (res != null) {
 				WebServicesClient webClient = res.getWebServicesClient();
 				if (webClient != null)
 					return webClient.getServiceRefs();
 			}
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/**
 	 * @param webapp
 	 * @return
 	 */
 	public List get13ServiceRefs(WebApp webapp) {
 		IProject handle = getComponentProject(WorkbenchResourceHelper.getFile(webapp));
 		if (handle == null)
 			return Collections.EMPTY_LIST;
 		WSCDDArtifactEdit artifactEdit = getExistingWSCDDArtifactEdit(handle);
 		if (artifactEdit !=null) {
 			WebServicesResource res = artifactEdit.getWscddXmiResource();
 			if (res != null) {
 				WebServicesClient webClient = res.getWebServicesClient();
 				if (webClient != null)
 					return webClient.getServiceRefs();
 			}
 		}
 		return Collections.EMPTY_LIST;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse.core.resources.IResourceDelta)
 	 */
 	public boolean visit(IResourceDelta delta) throws CoreException {
 		IResource resource = delta.getResource();
 		if (resource.getType() == IResource.PROJECT) {
 			IProject p = (IProject) resource;
 			// Handle project adds and project opens
 			if ((delta.getKind()==IResourceDelta.ADDED || (((delta.getFlags() & IResourceDelta.OPEN) != 0) && p.isAccessible()))) {
 				IVirtualComponent component = ComponentCore.createComponent(p);
 				if (component!=null && !J2EEProjectUtilities.isEARProject(p) && !J2EEProjectUtilities.isStaticWebProject(p)) {
 					addArtifactEdit(p);
 					notifyListeners(EditModelEvent.ADDED_RESOURCE);
 					return false;
 				}
 			}
 			// Handle project close events and removals 
 			else if ((delta.getKind() == IResourceDelta.CHANGED  && ((delta.getFlags() & IResourceDelta.OPEN) != 0))
 					|| (delta.getKind() == IResourceDelta.REMOVED)){
 				removeArtifactEdit(p);
 				return false;
 			}
 		}
 		
 		else if (resource.getType() == IResource.FILE && isInterrestedInFile((IFile) resource)) {
 			// Handle WSIL and WSDL File additions
 			if ((delta.getKind() == IResourceDelta.ADDED) || ((delta.getFlags() & IResourceDelta.MOVED_TO) != 0)) {
 				if (resource.getFileExtension().equals(WSDL_EXT))
 				    addedWsdl((IFile) resource);
 				else if (resource.getFileExtension().equals(WSIL_EXT))
 				    addedWsil((IFile)resource);
 			}
 			// Handle WSIL or WSDL file removals
 			else if ((delta.getKind() == IResourceDelta.REMOVED) || ((delta.getFlags() & IResourceDelta.MOVED_FROM) != 0)) {
 				if (resource.getFileExtension().equals(WSDL_EXT) || resource.getFileExtension().equals(WSIL_EXT))
 				notifyListeners(EditModelEvent.UNLOADED_RESOURCE);
 			}
 			return false;
 		}
 		return true;
 	}
 
 	protected void addedWsdl(IFile wsdl) {
 		if (!wsdl.exists())
 			return;
 		IProject handle = getComponentProject(wsdl);
 		if (handle != null) {
 			addArtifactEdit(handle);
 			notifyListeners(EditModelEvent.LOADED_RESOURCE);
 		}
 	}
 
 	protected void addedWsil(IFile wsil) {
 		if (!wsil.exists())
 			return;
 		IProject handle = getComponentProject(wsil);
 		if (handle != null) {
 			addArtifactEdit(handle);
 			notifyListeners(EditModelEvent.LOADED_RESOURCE);
 		}
 	}
 	
 	private IProject getComponentProject(IFile res) {
 		return res.getProject();
 	}
 
 	protected boolean isInterrestedInFile(IFile aFile) {
 		if (aFile != null && aFile.getFileExtension() != null) {
 			String extension = aFile.getFileExtension();
 			return extension.equals(WSDL_EXT) || extension.equals(WSIL_EXT);
 		}
 		return false;
 	}
 	
 	/**
 	 * The only resources webservice manager is concerned about are:
 	 * webservice.xml, webserviceclient.xml, and J2EE 1.4 web.xml, ejb-jar-xml, and application-client.xml
 	 * @param res
 	 * @return boolean isInterested
 	 */
 	private boolean isInterestedInResource(Resource res) {
 		if (res instanceof WsddResource || res instanceof WebServicesResource) {
 			return true;
 		} else if (res instanceof EJBResource || res instanceof WebAppResource || res instanceof ApplicationClientResource) {
 			return ((XMLResource)res).getJ2EEVersionID()>J2EEVersionConstants.J2EE_1_3_ID;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * @param object
 	 * @return
 	 */
 	public Collection getServiceRefs(EJBJar jar) {
 
 		List list = new ArrayList();
 		List beans = jar.getEnterpriseBeans();
 		try {
 			for (int i = 0; i < beans.size(); i++) {
 				EnterpriseBean bean = (EnterpriseBean) beans.get(i);
 				list.addAll(getServiceRefs(bean));
 			}
 		} catch (Exception e) {
 			//Ignore
 		}
 		return list;
 	}
 
 	public Collection getServiceRefs(EnterpriseBean bean) {
 		List list = new ArrayList();
 		if (bean.getEjbJar().getJ2EEVersionID() >= J2EEVersionConstants.J2EE_1_4_ID)
 			list.addAll(bean.getServiceRefs());
 		else
 			list.addAll(get13ServiceRefs(bean));
 		return list;
 	}
 
 	public Collection getServiceRefs(WebApp webapp) {
 
 		List list = new ArrayList();
 		try {
 			if (webapp.getVersionID() >= J2EEVersionConstants.WEB_2_4_ID)
 				list.addAll(webapp.getServiceRefs());
 			else
 				list.addAll(get13ServiceRefs(webapp));
 		} catch (Exception e) {
 			//Ignore
 		}
 		return list;
 	}
 
 	public Collection getServiceRefs(ApplicationClient client) {
 
 		List list = new ArrayList();
 		try {
 			if (client.getJ2EEVersionID() >= J2EEVersionConstants.J2EE_1_4_ID)
 				list.addAll(client.getServiceRefs());
 			else
 				list.addAll(get13ServiceRefs(client));
 		} catch (Exception e) {
 			//Ignore
 		}
 		return list;
 	}
 }
