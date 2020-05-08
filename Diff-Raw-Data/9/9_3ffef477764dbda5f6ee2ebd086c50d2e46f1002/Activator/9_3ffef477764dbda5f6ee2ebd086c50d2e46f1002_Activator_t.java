 /*******************************************************************************
  * Mission Control Technologies, Copyright (c) 2009-2012, United States Government
  * as represented by the Administrator of the National Aeronautics and Space 
  * Administration. All rights reserved.
  *
  * The MCT platform is licensed under the Apache License, Version 2.0 (the 
  * "License"); you may not use this file except in compliance with the License. 
  * You may obtain a copy of the License at 
  * http://www.apache.org/licenses/LICENSE-2.0.
  *
  * Unless required by applicable law or agreed to in writing, software 
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the 
  * License for the specific language governing permissions and limitations under 
  * the License.
  *
  * MCT includes source code licensed under additional open source licenses. See 
  * the MCT Open Source Licenses file included with this distribution or the About 
  * MCT Licenses dialog available at runtime from the MCT Help menu for additional 
  * information. 
  *******************************************************************************/
 package gov.nasa.arc.mct.services.config.impl;
 
 import java.io.IOException;
 import java.util.Dictionary;
 
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.util.tracker.ServiceTracker;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Initializes the service configurator service. Loads the service
  * configuration from the services.xml file, and adds all service
  * configurations, for either singletons or factory instances, to
  * the {@link ConfigurationAdmin} service.
  */
 public class Activator implements BundleActivator {
 	
 	// Set to default access so can be mocked by unit tests.
 	static Logger log = LoggerFactory.getLogger(Activator.class);
 	
 	private static final String CONFIGURATION_RESOURCE = "properties/services.xml";
 	
 	// This needs to be package protected instead of private, so that unit tests
 	// can see it.
 	private MyTracker cmTracker;
 	
 	static class MyTracker extends ServiceTracker {
 		MyTracker(BundleContext context, Class<? extends Object> clazz) {
 			super(context, clazz.getName(), null);
 		}
 	}
 
 	/**
 	 * Starts the service configurator service in the given bundle
 	 * context.
 	 * 
 	 * @param context the OSGi bundle context for this service
 	 */
 	public void start(BundleContext context) {
 		log.info("Starting MCT service configurator.");
 		
 		ServiceConfiguration config = null;
 		try {
 			config = ServiceHelper.getInstance().loadServiceConfiguration(ClassLoader.getSystemResourceAsStream(CONFIGURATION_RESOURCE));
 		} catch (Exception e) {
 			log.error("Error while parsing services configuration file {}", new Object[]{CONFIGURATION_RESOURCE}, e);
 			throw new RuntimeException(e);
 		}
 		log.debug("MCT service configurator found {} factories", config.getFactories().size());
 		
 		final ServiceConfiguration serviceConfig = config;
 		cmTracker = new MyTracker(context, ConfigurationAdmin.class) {
 			@Override
 			public Object addingService(ServiceReference reference) {
 				ConfigurationAdmin cm = (ConfigurationAdmin) context.getService(reference);
 				if (cm != null) {
 					configureServices(cm, serviceConfig);
 					configureFactories(cm, serviceConfig);
 				}
 				return super.addingService(reference);
 			}
 		};
 		
 		cmTracker.open();
 	}
 
 	/**
 	 * Stops the service configurator service.
 	 * 
 	 * @param context the OSGi bundle context for this service
 	 */
 	public void stop(BundleContext context) {
 		cmTracker.close();
 	}
 	
 	static void configureServices(ConfigurationAdmin cm, ServiceConfiguration serviceConfig) {
 		for (Service service : serviceConfig.getServices()) {
			Dictionary<String, ?> props = service.getProperties();
 			String pid = service.getServiceID();
 			try {
 				Configuration config = cm.getConfiguration(pid, null);
 				config.update(props);
 				log.debug("Created configuration for pid {}", pid);
 			} catch (IOException e) {
 				log.error("Could not create configuration for pid {}", pid, e);
 			}
 		}
 	}
 
 	static void configureFactories(ConfigurationAdmin cm, ServiceConfiguration serviceConfig) {
 		for (ServiceFactory factory : serviceConfig.getFactories()) {
 			String factoryPID = factory.getFactoryID();
 			for (Service service : factory.getServices()) {
 				try {
 					Configuration config = cm.createFactoryConfiguration(factoryPID, null);
 					config.update(service.getProperties());
 					log.debug("Added configuration for factory pid {}", factoryPID);
 				} catch (IOException e) {
 					log.error("Could not create factory configuration for factory pid {}", factoryPID, e);
 				}
 			}
 		}
 	}
 
 	MyTracker getCmTracker() {
 		return cmTracker;
 	}
 	
 }
