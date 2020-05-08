 /*******************************************************************************
  * Copyright (c) 2011 Composent, Inc. and others. All rights reserved. This
  * program and the accompanying materials are made available under the terms of
  * the Eclipse Public License v1.0 which accompanies this distribution, and is
  * available at http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   Composent, Inc. - initial API and implementation
  ******************************************************************************/
 package org.eclipse.ecf.remoteservice.internal.tm.restlet.servlet;
 
 import java.io.IOException;
 import java.io.StringWriter;
 import java.lang.reflect.InvocationHandler;
 import java.lang.reflect.Method;
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Dictionary;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.ecf.core.ContainerCreateException;
 import org.eclipse.ecf.core.IContainer;
 import org.eclipse.ecf.core.IContainerManager;
 import org.eclipse.ecf.core.identity.ID;
 import org.eclipse.ecf.core.identity.IDFactory;
 import org.eclipse.ecf.core.identity.Namespace;
 import org.eclipse.ecf.osgi.services.remoteserviceadmin.AbstractTopologyManager;
 import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescription;
 import org.eclipse.ecf.osgi.services.remoteserviceadmin.EndpointDescriptionWriter;
 import org.eclipse.ecf.osgi.services.remoteserviceadmin.HostContainerSelector;
 import org.eclipse.ecf.osgi.services.remoteserviceadmin.IEndpointDescriptionAdvertiser;
 import org.eclipse.ecf.remoteservice.IRemoteServiceContainer;
 import org.eclipse.ecf.remoteservice.RemoteServiceContainer;
 import org.eclipse.ecf.remoteservice.internal.Activator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.remoteserviceadmin.ExportRegistration;
 import org.restlet.Application;
 import org.restlet.Restlet;
 import org.restlet.resource.Finder;
 import org.restlet.routing.Filter;
 import org.restlet.routing.Route;
 import org.restlet.routing.Router;
 import org.restlet.routing.TemplateRoute;
 import org.restlet.util.RouteList;
 
 public class RestletTopologyManager extends AbstractTopologyManager {
 
	private static final boolean ADVERTISE_TO_SYSTEM_OUT = false;
 	private static final String RESTLET_CONTAINER_HOST = "ecf.container.restlet.host";
 
 	private static Object createDummyProxy(Class<?>[] interfaces) {
 		return Proxy.newProxyInstance(interfaces[0].getClassLoader(),
 				interfaces, new InvocationHandler() {
 					public Object invoke(Object arg0, Method arg1, Object[] arg2)
 							throws Throwable {
 						return null;
 					}
 				});
 	}
 
 	private RestletComponent restletComponent;
 	private RestletHostContainerSelector hostContainerSelector;
 
 	public RestletTopologyManager(BundleContext context,
 			RestletComponent component) {
 		super(context);
 		this.hostContainerSelector = new RestletHostContainerSelector();
 		this.restletComponent = component;
 	}
 
 	@SuppressWarnings("rawtypes")
 	private Dictionary createLocalRemoteServiceProperties(
 			RestletApplicationServlet restletApplicationServlet, String path,
 			Class<?>[] exportedInterfaces, Map<?, ?> resourceProviderProperties) {
 		return null;
 	}
 
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private ServiceRegistration registerLocalRemoteService(
 			Class<?>[] exportedInterfaces, Dictionary properties) {
 		String[] eiNames = new String[exportedInterfaces.length];
 		for (int i = 0; i < eiNames.length; i++)
 			eiNames[i] = exportedInterfaces[i].getName();
 		return Activator.getContext().registerService(eiNames,
 				createDummyProxy(exportedInterfaces), properties);
 	}
 
 	public void exportRemoteApplicationServlet(
 			RestletApplicationServlet restletApplicationServlet) {
 		// Get restlet application
 		Application application = restletApplicationServlet.getApplication();
 		Restlet restlet = application.getInboundRoot();
 		while (restlet instanceof Filter)
 			restlet = ((Filter) restlet).getNext();
 		if (restlet instanceof Router) {
 			Router router = (Router) restlet;
 			RouteList routeList = router.getRoutes();
 			for (Route route : routeList) {
 				if (route instanceof TemplateRoute) {
 				String path = ((TemplateRoute) route).getTemplate().getPattern();
 				Restlet next = route.getNext();
 				if (next instanceof Finder) {
 					Finder finder = (Finder) next;
 					Class<?> targetClass = finder.getTargetClass();
 					if (targetClass != null) {
 						Class<?>[] interfaces = targetClass.getInterfaces();
 						Map<?, ?> resourceProviderProperties = restletComponent
 								.getResourceProviderProperties(path);
 						if (resourceProviderProperties == null) {
 							trace("exportRemoteApplicationServlet",
 									"resource path=" + path
 											+ " has no associated properties");
 							continue;
 						}
 						String[] exportedInterfaceNames = getExportedInterfaceNames(resourceProviderProperties);
 						if (exportedInterfaceNames == null
 								|| exportedInterfaceNames.length == 0) {
 							trace("exportRemoteApplicationServlet",
 									"resource path="
 											+ path
 											+ " has no exported interface names");
 							continue;
 						}
 						Class<?>[] exportedInterfaces = getExportedInterfaces(
 								interfaces, exportedInterfaceNames);
 						if (exportedInterfaces == null) {
 							trace("exportRemoteApplicationServlet",
 									"resource path=" + path
 											+ " has no exported interfaces");
 							continue;
 						}
 						@SuppressWarnings("rawtypes")
 						Dictionary localRemoteServiceProperties = createLocalRemoteServiceProperties(
 								restletApplicationServlet, path,
 								exportedInterfaces, resourceProviderProperties);
 						@SuppressWarnings("rawtypes")
 						ServiceRegistration localRemoteServiceReg = registerLocalRemoteService(
 								exportedInterfaces,
 								localRemoteServiceProperties);
 						exportRestletService(restletApplicationServlet,
 								localRemoteServiceReg,
 								restletApplicationServlet.getFullURI(path),
 								resourceProviderProperties);
 					}
 				}
 			}
 			}
 		}
 	}
 
 	private String[] getExportedInterfaceNames(
 			Map<?, ?> resourceProviderProperties) {
 		Object o = resourceProviderProperties
 				.get(RestletComponent.RESTLET_SERVICE_EXPORTED_INTERFACES);
 		if (o == null)
 			return null;
 		if (o instanceof String)
 			return new String[] { (String) o };
 		if (o instanceof String[])
 			return (String[]) o;
 		return null;
 	}
 
 	private Map<String, Object> createOverridingProperties(
 			@SuppressWarnings("rawtypes") ServiceReference localRSReference,
 			String uri, Map<?, ?> resourceProviderProperties) {
 		Map<String, Object> result = new TreeMap<String, Object>(
 				String.CASE_INSENSITIVE_ORDER);
 		for (Iterator<?> i = resourceProviderProperties.keySet().iterator(); i
 				.hasNext();) {
 			String key = (String) i.next();
 			if (key.equals(org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGS)
 					|| key.equals("paths")
 					|| key.equals(Constants.OBJECTCLASS)
 					|| key.equals(Constants.SERVICE_ID)
 					|| key.equals("component.id")
 					|| key.equals("component.name"))
 				continue;
 			else
 				result.put(key, resourceProviderProperties.get(key));
 		}
 		result.put(
 				org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_INTERFACES,
 				localRSReference
 						.getProperty(org.osgi.framework.Constants.OBJECTCLASS));
 		result.put(
 				org.osgi.service.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONFIGS,
 				RESTLET_CONTAINER_HOST);
 		result.put(
 				org.eclipse.ecf.osgi.services.remoteserviceadmin.RemoteConstants.SERVICE_EXPORTED_CONTAINER_FACTORY_ARGS,
 				uri);
 		return result;
 	}
 
 	@SuppressWarnings("unchecked")
 	private Collection<ExportRegistration> exportRestletService(
 			RestletApplicationServlet restletApplicationServlet,
 			@SuppressWarnings("rawtypes") ServiceRegistration localRSRegistration,
 			String uri, Map<?, ?> resourceProviderProperties) {
 
 		@SuppressWarnings("rawtypes")
 		ServiceReference localRSReference = localRSRegistration.getReference();
 		// Create overridingProperties for RSA, and add exported interfaces
 		Map<String, Object> overridingProperties = createOverridingProperties(
 				localRSReference, uri, resourceProviderProperties);
 		// Add the uri for this service reference to the hostContainerSelector's
 		// set of available URIs
 		this.hostContainerSelector.addRemoteServiceURI(localRSReference, uri);
 
 		// get RSA
 		org.osgi.service.remoteserviceadmin.RemoteServiceAdmin rsa = getRemoteServiceAdmin();
 		// Use RSA to export the service
 		Collection<org.osgi.service.remoteserviceadmin.ExportRegistration> registrations = rsa
 				.exportService(localRSReference, overridingProperties);
 
 		if (registrations == null || registrations.size() == 0) {
 			logError("exportRestletService", //$NON-NLS-1$
 					"No export registrations created by RemoteServiceAdmin=" //$NON-NLS-1$
 							+ rsa + ".  ServiceReference=" + localRSReference //$NON-NLS-1$
 							+ " NOT EXPORTED"); //$NON-NLS-1$
 			return Collections.EMPTY_LIST;
 		}
 
 		List<EndpointDescription> endpointDescriptions = new ArrayList<EndpointDescription>();
 
 		for (org.osgi.service.remoteserviceadmin.ExportRegistration exportRegistration : registrations) {
 			// If they are invalid report as such
 			Throwable t = exportRegistration.getException();
 			if (t != null)
 				handleInvalidExportRegistration(exportRegistration, t);
 			else {
 				endpointDescriptions
 						.add((EndpointDescription) exportRegistration
 								.getExportReference().getExportedEndpoint());
 				synchronized (exportedRegistrations) {
 					exportedRegistrations.add(exportRegistration);
 				}
 				// Also add registration to RestletApplicationServlet for
 				// unexport
 				restletApplicationServlet
 						.addServiceRegistration(localRSRegistration);
 			}
 		}
 		// advertise valid exported registrations
 		advertiseEndpointDescriptions(endpointDescriptions);
 
 		return registrations;
 	}
 
 	private Class<?>[] getExportedInterfaces(Class<?>[] classes,
 			String[] classNames) {
 		List<Class<?>> result = new ArrayList<Class<?>>();
 		for (int i = 0; i < classes.length; i++) {
 			String className = classes[i].getName();
 			if (classNames != null) {
 				for (int j = 0; j < classNames.length; j++)
 					if (classNames[i].equals("*") || classNames[0].equals("*")
 							|| className.equals(classNames[i]))
 						result.add(classes[i]);
 			} else
 				result.add(classes[i]);
 		}
 		return result.toArray(new Class<?>[result.size()]);
 	}
 
 	public void unexportRemoteApplicationServlet(
 			RestletApplicationServlet restletApplicationServlet) {
 		// First get service registrations from remoteApplicationServlet
 		ServiceRegistration<?>[] regs = restletApplicationServlet
 				.getServiceRegistrations();
 		for (int i = 0; i < regs.length; i++) {
 			// unexport first
 			Collection<EndpointDescription> unexportEndpoints = unexportService(regs[i]
 					.getReference());
 			// Then unadvertise
 			for (EndpointDescription endpointDescription : unexportEndpoints)
 				unadvertiseEndpointDescription(endpointDescription);
 			// finally unregister local service
 			regs[i].unregister();
 		}
 	}
 
 	protected IEndpointDescriptionAdvertiser getEndpointDescriptionAdvertiser(
 			EndpointDescription endpointDescription) {
 		if (ADVERTISE_TO_SYSTEM_OUT) {
 			final EndpointDescriptionWriter writer = new EndpointDescriptionWriter();
 			return new IEndpointDescriptionAdvertiser() {
 
 				private void publish(EndpointDescription endpointDescription,
 						boolean advertise) {
 					try {
 						StringWriter sr = new StringWriter();
 						sr.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
 								.append("\n");
 						writer.writeEndpointDescriptions(
 								sr,
 								new EndpointDescription[] { (EndpointDescription) endpointDescription });
 						System.out.println(advertise ? "ADVERTISE"
 								: "UNADVERTISE");
 						System.out.print(sr.toString());
 					} catch (IOException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 				public IStatus advertise(EndpointDescription endpointDescription) {
 					publish(endpointDescription, true);
 					return Status.OK_STATUS;
 				}
 
 				public IStatus unadvertise(
 						EndpointDescription endpointDescription) {
 					publish(endpointDescription, false);
 					return Status.OK_STATUS;
 				}
 			};
 		} else
 			return super.getEndpointDescriptionAdvertiser(endpointDescription);
 	}
 
 	class RestletHostContainerSelector extends HostContainerSelector {
 
 		private static final String RESTLET_NAMESPACE = "ecf.namespace.restlet";
 
 		@SuppressWarnings("rawtypes")
 		private Map<ServiceReference, List<String>> resourceProviderURIs = new HashMap<ServiceReference, List<String>>();
 
 		public RestletHostContainerSelector() {
 			super(new String[] {}, true);
 		}
 
 		public RestletHostContainerSelector(String[] defaultConfigTypes,
 				boolean autoCreateContainer) {
 			super(defaultConfigTypes, autoCreateContainer);
 		}
 
 		synchronized void addRemoteServiceURI(
 				@SuppressWarnings("rawtypes") ServiceReference resourceProviderSR,
 				String uri) {
 			List<String> uris = resourceProviderURIs.get(resourceProviderSR);
 			if (uris == null)
 				uris = new ArrayList<String>();
 			uris.add(uri);
 			resourceProviderURIs.put(resourceProviderSR, uris);
 		}
 
 		@SuppressWarnings("rawtypes")
 		public synchronized IRemoteServiceContainer[] selectHostContainers(
 				ServiceReference serviceReference,
 				String[] serviceExportedInterfaces,
 				String[] serviceExportedConfigs, String[] serviceIntents) {
 
 			List<String> uris = resourceProviderURIs.remove(serviceReference);
 
 			return (uris != null) ? getRestletHostContainers(uris) : null;
 		}
 
 		private IRemoteServiceContainer[] getRestletHostContainers(
 				List<String> uris) {
 			List<IRemoteServiceContainer> results = new ArrayList<IRemoteServiceContainer>();
 			for (String uri : uris) {
 				IRemoteServiceContainer rsContainer = getRestletHostContainer(uri);
 				if (rsContainer != null)
 					results.add(rsContainer);
 			}
 			return results.toArray(new IRemoteServiceContainer[results.size()]);
 		}
 
 		private IRemoteServiceContainer getRestletHostContainer(String uri) {
 			Namespace restletNamespace = IDFactory.getDefault()
 					.getNamespaceByName(RESTLET_NAMESPACE);
 			ID restletID = IDFactory.getDefault().createID(restletNamespace,
 					uri);
 			IContainerManager containerManager = getContainerManager();
 			IContainer container = containerManager.getContainer(restletID);
 			if (container == null) {
 				try {
 					container = containerManager.getContainerFactory()
 							.createContainer(RESTLET_CONTAINER_HOST,
 									new Object[] { restletID });
 				} catch (ContainerCreateException e) {
 					logError("getRestletHostContainer",
 							"Could not create restlet host container for uri="
 									+ uri);
 					return null;
 				}
 			}
 			return new RemoteServiceContainer(container);
 		}
 	}
 
 }
