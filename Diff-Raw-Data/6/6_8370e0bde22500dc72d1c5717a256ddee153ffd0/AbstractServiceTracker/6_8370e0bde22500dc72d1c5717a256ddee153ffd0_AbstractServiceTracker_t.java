 /*******************************************************************************
  * Copyright (c) 2008, 2009 Bug Labs, Inc.
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *    - Redistributions of source code must retain the above copyright notice,
  *      this list of conditions and the following disclaimer.
  *    - Redistributions in binary form must reproduce the above copyright
  *      notice, this list of conditions and the following disclaimer in the
  *      documentation and/or other materials provided with the distribution.
  *    - Neither the name of Bug Labs, Inc. nor the names of its contributors may be
  *      used to endorse or promote products derived from this software without
  *      specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  *******************************************************************************/
 package com.buglabs.application;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.SortedMap;
 import java.util.TreeMap;
 
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.log.LogService;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
import com.buglabs.util.LogServiceUtil;

 /**
  * Service tracker for the BugApplication Bundle;
  * 
  */
 public abstract class AbstractServiceTracker implements ServiceTrackerCustomizer, IServiceProvider {
 
 	protected BundleContext context;
 
 	private Map servicesMap;
 
 	protected List services;
 
 	boolean started = false;
 
 	private LogService logService;
 
 	private String trackerName;
 
 	private SortedMap servicePropertiesMap = null;
 
 	public AbstractServiceTracker(BundleContext context) {
 		this.context = context;
 		servicesMap = new HashMap();
 		logService = getLogService(context);
 		trackerName = getTrackerName(context);
 		initServices();
 	}
 
 	private String getTrackerName(BundleContext context2) {
 		String name = (String) context2.getBundle().getHeaders().get("Bundle-SymbolicName");
 
 		if (name == null) {
 			name = context.getBundle().getLocation();
 		}
 
 		name = name + " [" + this.getClass().toString() + "]";
 
 		return name;
 	}
 
 	/**
 	 * Assume that a log service is always available.
 	 * 
 	 * @param context
 	 * @return
 	 */
 	private LogService getLogService(BundleContext context) {
		return LogServiceUtil.getLogService(context);
 	}
 
 	/**
 	 * Invoked when all services have been registered.
 	 * 
 	 */
 	public abstract void doStart();
 
 	/**
 	 * Invoked when a service is unregistered.
 	 * 
 	 */
 	public abstract void doStop();
 
 	/**
 	 * Implementations should add services of interest
 	 */
 	public void initServices() {
 		logService.log(LogService.LOG_DEBUG, trackerName + " A tracker in this bundle is tracking no services.");
 	}
 
 	/**
 	 * Used by OSGi to signal that a service was added.
 	 * 
 	 */
 	public Object addingService(ServiceReference reference) {
 		Object obj = context.getService(reference);
 		String[] objClassName = (String[]) reference.getProperty(Constants.OBJECTCLASS);
 		Object retval = null;
 		logService.log(LogService.LOG_DEBUG, trackerName + " Tracker found service: " + objClassName[0]);
 
 		if (!servicesMap.containsKey(objClassName[0])) {
 			servicesMap.put(objClassName[0], obj);
 			logService.log(LogService.LOG_DEBUG, trackerName + " Service added to service map: " + objClassName[0]);
 			retval = obj;
 
 			if (canStart()) {
 				logService.log(LogService.LOG_DEBUG, trackerName + " Starting tracker, all services are available.");
 				doStart();
 				started = true;
 			}
 		}
 		return retval;
 	}
 
 	public void modifiedService(ServiceReference reference, Object service) {
 
 	}
 
 	/**
 	 * Used by OSGi to signal that a service was removed.
 	 * 
 	 */
 	public void removedService(ServiceReference reference, Object service) {
 		Object obj = context.getService(reference);
 		boolean serviceRemoved = false;
 
 		String[] objClassName = (String[]) reference.getProperty(Constants.OBJECTCLASS);
 
 		if (servicesMap.get(objClassName[0]).equals(obj)) {
 			logService.log(LogService.LOG_DEBUG, trackerName + " removing service: " + objClassName[0]);
 			servicesMap.remove(objClassName[0]);
 			serviceRemoved = true;
 		}
 
 		if (hasStarted() && serviceRemoved) {
 			try {
 				logService.log(LogService.LOG_DEBUG, trackerName + " stopping tracker.");
 				doStop();
 			} catch (Exception e) {
 				logService.log(LogService.LOG_ERROR, e.getMessage());
 			}
 
 			started = false;
 		}
 	}
 
 	/**
 	 * Determines if the application can be started.
 	 * 
 	 * @returns true when the application is not running and there's a handle
 	 *          for each service.
 	 */
 	protected boolean canStart() {
 		if (hasStarted()) {
 			return false;
 		}
 
 		Iterator servicesIter = services.iterator();
 
 		while (servicesIter.hasNext()) {
 			if (servicesMap.get((String) servicesIter.next()) == null) {
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	/**
 	 * Helper method to retrieve a service of type class.
 	 * 
 	 */
 	public Object getService(Class clazz) {
 		return servicesMap.get(clazz.getName());
 	}
 
 	/**
 	 * Helps set up the serviceProperties map created to make generated code
 	 * simpler
 	 * 
 	 * @param serviceName
 	 * @param properties
 	 */
 	protected final void addServiceFilters(String serviceName, String[][] properties) {
 		Map propMap = new HashMap();
 		for (int i = 0; i < properties.length; i++) {
 			propMap.put(properties[i][0], properties[i][1]);
 		}
 		getServicePropertiesMap().put(serviceName, propMap);
 
 		// also add it to the services list
 		getServices().add(serviceName);
 	}
 
 	/**
 	 * returns the map of services and properties that represent the service
 	 * dependencies for this tracker the map, in pseudo-generic code, is:
 	 * Map<String serviceName, Map<String propertyKey, String propertyVal>>
 	 * 
 	 * @return
 	 */
 	public final SortedMap getServicePropertiesMap() {
 		if (servicePropertiesMap == null) {
 			servicePropertiesMap = new TreeMap();
 		}
 		return servicePropertiesMap;
 	}
 
 	/**
 	 * Used to retrieve a list of qualified service names. If you want your
 	 * application to depend on another service, simply add the fully qualified
 	 * name of the service to this list.
 	 * 
 	 * @return a list of Strings containing the fully qualified name of each
 	 *         service.
 	 * 
 	 */
 	public List getServices() {
 		if (services == null) {
 			services = new ArrayList();
 		}
 
 		return services;
 	}
 
 	public BundleContext getBundleContext() {
 		return context;
 	}
 
 	public boolean hasStarted() {
 		return started;
 	}
 
 	public void stop() {
 		if (hasStarted()) {
 			doStop();
 			started = false;
 		}
 	}
 }
