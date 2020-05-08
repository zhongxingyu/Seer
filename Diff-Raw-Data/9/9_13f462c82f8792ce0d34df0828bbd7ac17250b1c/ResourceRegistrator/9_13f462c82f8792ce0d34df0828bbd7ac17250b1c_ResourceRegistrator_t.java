 /*******************************************************************************
  * Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Peter Lachenmaier - Design and initial implementation
  *     Jan Philipp Fiedler
  *     Christopher Rohde
  ******************************************************************************/
 package org.sociotech.communitymashup.interfaceprovider.restinterface.servicetracker;
 
 import javax.servlet.ServletException;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpService;
 import org.osgi.service.http.NamespaceException;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTracker;
 import org.sociotech.communitymashup.application.RESTInterface;
 import org.sociotech.communitymashup.application.Security;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.interfaceprovider.restinterface.RESTInterfaceService;
 import org.sociotech.communitymashup.interfaceprovider.restinterface.RESTServlet;
 import org.sociotech.communitymashup.interfaceprovider.restinterface.html.RESTFileServlet;
 import org.sociotech.communitymashup.security.facade.SecurityServiceFacade;
 import org.sociotech.communitymashup.security.factory.facade.SecurityFactoryServiceFacade;
 import org.sociotech.communitymashup.security.factory.facade.callback.AsynchronousSecurityInstantiationCallback;
 import org.sociotech.communitymashup.security.factorytracker.NewSecurityFactoryCallback;
 import org.sociotech.communitymashup.security.factorytracker.SecurityFactoryServiceTracker;
 
 /**
  * 
  * This class tracks http services. registers and deregisters
  * the servlets used for the rest interface. 
  * 
  * @author Peter Lachenmaier
  */
 public class ResourceRegistrator extends ServiceTracker<HttpService, HttpService> implements NewSecurityFactoryCallback, AsynchronousSecurityInstantiationCallback {
 
 	/**
 	 * Reference to the service used for registering servlets
 	 */
 	private HttpService usedHttpService = null;
 
 	/**
 	 * Data set that should be provided by the rest interface
 	 */
 	private DataSet dataSet = null;
 
 	/**
 	 * Reference to the configuration of the rest interface 
 	 */
 	private RESTInterface configuration = null;
 
 	/**
 	 * Reference to the rest interface service, used for logging
 	 */
 	private RESTInterfaceService restInterfaceService = null;
 
 	/**
 	 * Reference to the security factory tracker
 	 */
 	private SecurityFactoryServiceTracker securityFactoryTracker;
 	
 	/**
 	 * Reference to the rest servlet
 	 */
 	private RESTServlet restServlet;
 
 	/**
 	 * Reference to the file servlet
 	 */
 	private RESTFileServlet fileServlet;
 
 	/**
 	 * Path where the servlets are registered.
 	 */
 	private String path;
 
 	/**
 	 * Creates a new resource registrator that creates and registers the needed servlets and resources.
 	 * 
 	 * @param context The bundle context
 	 * @param dataSet Data set containing the data that should be served by the rest interface
 	 * @param configuration Configuration of the rest interface
 	 * @param restInterfaceService The rest interface service that will be used for logging
 	 */
 	public ResourceRegistrator(BundleContext context, DataSet dataSet, RESTInterface configuration, RESTInterfaceService restInterfaceService) {
 		super(context, HttpService.class.getName(), null);
 
 		this.dataSet = dataSet;
 		this.configuration = configuration;
 		this.restInterfaceService =  restInterfaceService;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.util.tracker.ServiceTracker#addingService(org.osgi.framework.ServiceReference)
 	 */
 	@Override
 	public HttpService addingService(ServiceReference<HttpService> reference) {
 
 		HttpService httpService = context.getService(reference);
 
 		// first unregister everything
 		unregisterAll();
 
 		path = configuration.getUrlSuffix();
 
 		// ensure to create correct urls
 		if(!path.startsWith("/"))
 		{
 			path = "/" + path;
 		}
 
 		if(!path.endsWith("/"))
 		{
 			path = path + "/";
 		}
 
 		boolean securityNeeded = false;
 
 		Security securityConfiguration = configuration.getSecurity();
 
 		if(securityConfiguration != null)
 		{
 			// security defined in interface configuration
 			securityNeeded = true;
 		}
 
 
 		String typeString = configuration.getType();
 		
 		int type =  parseRestType(typeString);
 			
 		// create rest and file servlet
 		restServlet = new RESTServlet(restInterfaceService, dataSet, path, securityNeeded, type, configuration);
 		fileServlet = new RESTFileServlet(dataSet, securityNeeded);
 
 		// if security is needed open up a tracker and security services will be added to the servlets
 		if(securityNeeded)
 		{
 			// open up security factory tracker
 			securityFactoryTracker = new SecurityFactoryServiceTracker(context, this);
 			securityFactoryTracker.open();
 		}
 		
		// register servlets 
 		try {
 
 			httpService.registerServlet(path + "mashup",restServlet, null, null);
 			log("Registered RESTinterface at " + path + "mashup", LogService.LOG_DEBUG);
 
 			httpService.registerServlet(path + "files", fileServlet, null, null);
 			log("Registered REST file servlet at " + path + "files", LogService.LOG_DEBUG);
 
 		} catch (ServletException e) {
 			log("Servlet-Exception at registering REST servlets. (" + e.getMessage() +")", LogService.LOG_ERROR);
 		} catch (NamespaceException e) {
 			log("Namespace-Exception at registering REST servlets. (" + e.getMessage() +")", LogService.LOG_ERROR);
 		}
 
 		// keep reference
 		usedHttpService = httpService;
 
 		return httpService;
 	}
 
 	/**
 	 * Parses the type out of the given string. Returns xml if misconfigured 
 	 * 
 	 * @param typeString String containing the type
 	 * @return Type value needed for {@link RESTServlet}
 	 */
 	private int parseRestType(String typeString) {
 		
 		// default is xml
 		int type = RESTServlet.TYPE_XML;
 		
 		if(typeString == null)
 		{
 			return type;
 		}
 		
 		// parse type
 		if(typeString.equalsIgnoreCase("json"))
 		{
 			type = RESTServlet.TYPE_JSON;
 		}
 		else if(typeString.equalsIgnoreCase("json-p") || typeString.equalsIgnoreCase("jsonp"))
 		{
 			type = RESTServlet.TYPE_JSON_P;
 		}
 		else if(typeString.equalsIgnoreCase("html"))
 		{
 			type = RESTServlet.TYPE_HTML;
 		}
 		
 		return type;
 	}		
 
 	private void log(String message, int logLevel) {
 		
 		// let rest interface service log the message
 		restInterfaceService.log(message, logLevel);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.util.tracker.ServiceTracker#removedService(org.osgi.framework.ServiceReference, java.lang.Object)
 	 */
 	@Override
 	public void removedService(ServiceReference<HttpService> reference, HttpService httpService) {
 
 
 		// unregister everything if the removed service was used
 		if(httpService == usedHttpService)
 		{
 			unregisterAll();
 		}
 		
 		// close security factory tracker
 		if(securityFactoryTracker != null)
 		{
 			securityFactoryTracker.close();
 			securityFactoryTracker = null;
 		}
 
 		super.removedService(reference, httpService);
 	}
 
 	/**
 	 * Unregisters all registered resources and servlets
 	 */
 	private void unregisterAll() {
 
 		// close security factory tracker
 		if(securityFactoryTracker != null)
 		{
 			securityFactoryTracker.close();
 			securityFactoryTracker = null;
 		}
 		
 		if(usedHttpService == null)
 		{
 			return;
 		}
 
 		// unregister all servlets
 		try {
 			usedHttpService.unregister(path + "mashup");
 			usedHttpService.unregister(path + "files");
 		
 			usedHttpService.unregister(path + "inc");
 		} catch(Exception e) {
 			log("Exception at unregistering REST servlets. (" + e.getMessage() +")", LogService.LOG_ERROR);
 		}
 
 		// set it to null when everything is unregistered
 		usedHttpService = null;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.osgi.util.tracker.ServiceTracker#close()
 	 */
 	@Override
 	public void close() {
 
 		// unregister all before closing
 		unregisterAll();
 
 		super.close();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.security.factorytracker.NewSecurityFactoryCallback#newSecurityFactory(org.sociotech.communitymashup.security.factory.facade.SecurityFactoryServiceFacade)
 	 */
 	@Override
 	public void newSecurityFactory(SecurityFactoryServiceFacade factory) {
 		if(factory == null)
 		{	
 			// noting can be done (should never happen)
 			return;
 		}
 		
 		restInterfaceService.log("Got security factory. Producing security service for rest interface", LogService.LOG_INFO);
 		
 		Security securityConfiguration = configuration.getSecurity();
 		
 		// start asynchronous production
 		factory.produceAsynchronous(securityConfiguration, this, null);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.security.factorytracker.NewSecurityFactoryCallback#removedSecurityFactory(org.sociotech.communitymashup.security.factory.facade.SecurityFactoryServiceFacade)
 	 */
 	@Override
 	public void removedSecurityFactory(SecurityFactoryServiceFacade factory) {
 		// nothing to do
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.security.factory.facade.callback.AsynchronousSecurityInstantiationCallback#securityServiceInstantiated(org.sociotech.communitymashup.security.facade.SecurityServiceFacade, java.lang.Object)
 	 */
 	@Override
 	public void securityServiceInstantiated(SecurityServiceFacade securityService, Object key) {
 		
 		// provide security service to servlets
 		restServlet.setSecurityService(securityService);
 		fileServlet.setSecurityService(securityService);
 	}
 }
