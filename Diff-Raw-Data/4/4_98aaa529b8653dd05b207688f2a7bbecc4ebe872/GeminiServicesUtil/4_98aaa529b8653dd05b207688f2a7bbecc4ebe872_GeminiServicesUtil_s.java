 /*******************************************************************************
  * Copyright (c) 2010 Oracle.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * and Apache License v2.0 which accompanies this distribution. 
  * The Eclipse Public License is available at
  *     http://www.eclipse.org/legal/epl-v10.html
  * and the Apache License v2.0 is available at 
  *     http://www.opensource.org/licenses/apache2.0.php.
  * You may elect to redistribute this code under either of these licenses.
  *
  * Contributors:
  *     mkeith - Gemini JPA work 
  ******************************************************************************/
 package org.eclipse.gemini.jpa;
 
 import static org.eclipse.gemini.jpa.GeminiUtil.bundleVersion;
 import static org.eclipse.gemini.jpa.GeminiUtil.debug;
 import static org.eclipse.gemini.jpa.GeminiUtil.debugClassLoader;
 import static org.eclipse.gemini.jpa.GeminiUtil.fatalError;
 import static org.eclipse.gemini.jpa.GeminiUtil.warning;
 
 import java.lang.reflect.Proxy;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Dictionary;
 import java.util.Hashtable;
 import java.util.Map;
 
 import javax.persistence.EntityManagerFactory;
 
 import org.eclipse.gemini.jpa.classloader.BundleProxyClassLoader;
 import org.eclipse.gemini.jpa.classloader.CompositeClassLoader;
 import org.eclipse.gemini.jpa.provider.OSGiJpaProvider;
 import org.eclipse.gemini.jpa.proxy.EMFBuilderServiceProxyHandler;
 import org.eclipse.gemini.jpa.proxy.EMFServiceProxyHandler;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.InvalidSyntaxException;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 import org.osgi.service.jdbc.DataSourceFactory;
 import org.osgi.util.tracker.ServiceTracker;
 
 /**
  * This class provides functionality to handle service registration of 
  * persistence units and providers, etc. One instance per provider.
  */
 @SuppressWarnings({"rawtypes","unchecked"})
 public class GeminiServicesUtil {
 
     // The provider using this instance
     OSGiJpaProvider osgiJpaProvider;
     
     // Keep this for logging convenience
     String providerClassName;
    
     // The anchor util to use to get anchor class info from
     AnchorClassUtil anchorUtil;
     
     // PersistenceProvider service
     ServiceRegistration providerService;
     
     
     public GeminiServicesUtil(OSGiJpaProvider provider, AnchorClassUtil anchorUtil) {
         this.osgiJpaProvider = provider;
         this.providerClassName = provider.getProviderClassName();
         this.anchorUtil= anchorUtil;
     }
     
     /*==================*/
     /* Services methods */
     /*==================*/
     
     /**
      * Register the provider as a persistence provider service.
      * The service registration will be stored locally.
      */
     public void registerProviderService() {
         
         debug("GeminiServicesUtil registering provider service for ", providerClassName);
 
         // Service strings
         String[] serviceNames = { javax.persistence.spi.PersistenceProvider.class.getName() };
         // Get a provider JPA SPI instance 
         javax.persistence.spi.PersistenceProvider persistenceProvider = osgiJpaProvider.getProviderInstance();
 
         // Store the version of the provider as a service property
         String version = bundleVersion(osgiJpaProvider.getBundle());
         Dictionary<String,String> props = new Hashtable<String,String>();
         props.put("osgi.jpa.provider.version", version);
         props.put("javax.persistence.provider", providerClassName);
         
         // Register the provider service
         providerService = osgiJpaProvider.getBundleContext().registerService(
                 serviceNames, persistenceProvider, props);
         debug("GeminiServicesUtil successfully registered provider service for ", providerClassName);
     }    
 
     /**
      * Unregister the provider service. 
      */
     public void unregisterProviderService() {
 
         debug("GeminiServicesUtil un-registering provider service for ", providerClassName);
         providerService.unregister();
         providerService = null;
         debug("GeminiServicesUtil successfully un-registered provider service for ", providerClassName);
     }
 
     /**
      * Register the EMF and EMFBuilder services.
      */
     public void registerEMFServices(PUnitInfo pUnitInfo) {
 
         debug("GeminiServicesUtil registerEMFServices for ", pUnitInfo.getUnitName());
 
         // Map of generated anchor classes keyed by class name
         Map<String, Class<?>> anchorClasses; 
         
         // Will be empty if anchor classes not generated
         anchorClasses = anchorUtil.loadAnchorClasses(pUnitInfo);
 
         // Create the properties used for both services
         Dictionary<String,String> props = buildServiceProperties(pUnitInfo);
         
         // Try to register the EMF service (it will only occur if data source is available)
         tryToRegisterEMFService(pUnitInfo, anchorClasses, props);                
 
         // Create a builder service in any case
         registerEMFBuilderService(pUnitInfo, anchorClasses, props);
     }
 
     /**
      * Unregister whatever EMF and EMFBuilder services are registered.
      */
     public void unregisterEMFServices(PUnitInfo pUnitInfo) {
 
         unregisterEMFService(pUnitInfo);
         unregisterEMFBuilderService(pUnitInfo);
     }
 
     /**
      * Unregister the EMF service if there was an EMF service registered.
      * Clean up any resources the service may have allocated.
      * 
      * @param pUnitInfo
      */
     public void unregisterEMFService(PUnitInfo pUnitInfo) {
 
         debug("GeminiServicesUtil un-registerEMFService for ", pUnitInfo.getUnitName());
 
         // If the tracking service is going, stop it
         // Note: By stopping the tracker now we will not be able to handle a 
         //       DSF that comes and goes; only one that comes for the first time
         stopTrackingDataSourceFactory(pUnitInfo);
 
         // If an EMF service is registered then unregister it
         ServiceRegistration emfService = pUnitInfo.getEmfService();
         if (emfService != null) {
             debug("GeminiServicesUtil un-registering EMF service for ", pUnitInfo.getUnitName());
             try { 
                 emfService.unregister(); 
             } catch (Exception e) {
                 warning("Error unregistering EMF service: ", e);
             }
             debug("GeminiServicesUtil un-registered EMF service for ", pUnitInfo.getUnitName());
             pUnitInfo.setEmfService(null);
         }
 
         // If an EMF exists because we created one then we close and remove it
         EntityManagerFactory emf = pUnitInfo.getEmf();
         if ((emf != null) && (!pUnitInfo.isEmfSetByBuilderService())) {
             if (emf.isOpen()) emf.close();
             pUnitInfo.getEmfHandler().syncUnsetEMF();
             debug("GeminiServicesUtil EMF service removed EMF: ", emf);
         }
         pUnitInfo.setEmfHandler(null);
     }
 
     /**
      * Unregister the EMFBuilder service.
      * Clean up any resources the service may have allocated.
      * 
      * @param pUnitInfo
      */
     public void unregisterEMFBuilderService(PUnitInfo pUnitInfo) {
 
         debug("GeminiServicesUtil un-registerEMFBuilderService for ", pUnitInfo.getUnitName());
 
         // Unregister the service
         ServiceRegistration emfBuilderService = pUnitInfo.getEmfBuilderService();
         if (emfBuilderService != null) {
             debug("GeminiServicesUtil un-registering EMFBuilder service for ", pUnitInfo.getUnitName());
             try {
                 emfBuilderService.unregister();
             } catch (Exception e) {
                 warning("Error un-registering EMFBuilder service: ", e);
             }
             debug("GeminiServicesUtil un-registered EMFBuilder service for ", pUnitInfo.getUnitName());
             pUnitInfo.setEmfBuilderService(null);
         }
 
         // Close the EMF if one still exists and clear out the handler
         EntityManagerFactory emf = pUnitInfo.getEmf();
         if (emf != null) {
             if (emf.isOpen()) emf.close();
             pUnitInfo.getEmfBuilderHandler().syncUnsetEMF();
             debug("GeminiServicesUtil EMFBuilder service removed emf: ", emf);
         }
         pUnitInfo.setEmfBuilderHandler(null);
 
     }    
     
     /*================*/
     /* Helper methods */
     /*================*/
         
     /**
      * Get or create a loader to load classes from the punit.
      * A sequence of conditions provides a pattern for obtaining it.
      */
     ClassLoader extractPUnitLoader(PUnitInfo pUnitInfo, 
                                    Map<String, Class<?>> anchorClasses) {
     
         ClassLoader pUnitLoader = null;
         
         // 1. If there are any anchor classes then load one and get its loader
         if (!anchorClasses.isEmpty()) {
             pUnitLoader = anchorClasses.values().iterator().next().getClassLoader();
 
         // 2. Otherwise, if there are managed JPA classes listed, use one to get the loader
         } else if (!pUnitInfo.getClasses().isEmpty()) {
             try { 
                 pUnitLoader = pUnitInfo.getBundle().loadClass((String)(pUnitInfo.getClasses().toArray()[0])).getClassLoader();
             } catch (ClassNotFoundException cnfEx) {
                 fatalError("Could not load domain class in p-unit", cnfEx);
             }
             
         // 3. If all else fails just use a proxy loader
         } else {
             pUnitLoader = new BundleProxyClassLoader(pUnitInfo.getBundle());
         }
         debug("GeminiServicesUtil pUnit loader ", pUnitLoader);
         return pUnitLoader;
     }
 
     /**
      * Get or create a loader to use to create a proxy class.
      */
     ClassLoader proxyLoader(PUnitInfo pUnitInfo,
                             Map<String, Class<?>> anchorClasses, 
                             Class<?> jpaClass) {
         
         ClassLoader cl = null;
 
         // If there are no managed JPA classes listed, return loader used to load the class passed in
         if (pUnitInfo.getClasses().isEmpty()) {
             cl = jpaClass.getClassLoader();
         } else if (!anchorClasses.isEmpty()) {
             // If anchor classes exist then get a loader from one of them
             cl = anchorClasses.values().iterator().next().getClassLoader();
         } else {
             try {
                 // We have domain classes, but no anchor classes were generated.
                 // Load a domain class and get a loader from it. Combine it with the provider loader.
                 ClassLoader pUnitLoader = 
                     pUnitInfo.getBundle().loadClass((String)(pUnitInfo.getClasses().toArray()[0])).getClassLoader();
                 ClassLoader jpaClassLoader = jpaClass.getClassLoader();
                 cl = (pUnitLoader == jpaClassLoader) 
                     ? jpaClassLoader 
                     : new CompositeClassLoader(pUnitLoader, jpaClassLoader);
             } catch (ClassNotFoundException cnfEx) {
                 fatalError("Could not load domain class in p-unit", cnfEx);
             }
         }
         debugClassLoader("GeminiServicesUtil proxy loader ", cl);
         return cl;
     }
 
     /** 
      * Create and return a proxy for the EMF (and specified list of classes
      * which must include the EMF class).
      */
     Object createEMFProxy(PUnitInfo pUnitInfo, ClassLoader loader, Class<?>[] clsArray) {
 
         EMFServiceProxyHandler emfProxyHandler = new EMFServiceProxyHandler(pUnitInfo);
         Object result = null;
         try {
             result = Proxy.newProxyInstance(loader, clsArray, emfProxyHandler);
             debug("GeminiServicesUtil created EMF proxy ");
         } catch (Exception e) { 
             fatalError("GeminiServicesUtil - Failed to create proxy for EMF service: ", e); 
         }
         pUnitInfo.setEmfHandler(emfProxyHandler);
         return result;
     }
     
     /** 
      * Create and return a proxy for the EMFBuilder (and specified list of classes
      * which must include the EMFBuilder class).
      */
     Object createEMFBuilderProxy(PUnitInfo pUnitInfo, 
                                         ClassLoader loader, 
                                         Class<?>[] clsArray) {
         
         // Assume that EMF proxy handler has been created and is stored in pUnitInfo
         EMFBuilderServiceProxyHandler emfBuilderProxyHandler = 
             new EMFBuilderServiceProxyHandler(pUnitInfo, pUnitInfo.getEmfHandler());
         Object result = null;
         try {
             result = Proxy.newProxyInstance(loader, clsArray, emfBuilderProxyHandler);
             debug("GeminiServicesUtil created EMFBuilder proxy ");
         } catch (Exception e) { 
             fatalError("GeminiServicesUtil - Failed to create proxy for EMFBuilder service: ", e); 
         }
         pUnitInfo.setEmfBuilderHandler(emfBuilderProxyHandler);
         return result;
     }
     
     /** 
      * build the list of service properties for the service.
      */
     Dictionary<String,String> buildServiceProperties(PUnitInfo pUnitInfo) {
 
         Bundle pUnitBundle = pUnitInfo.getBundle();
         // Assemble the properties
         Dictionary<String,String> props = new Hashtable<String,String>();
         props.put("osgi.unit.name", pUnitInfo.getUnitName());
         props.put("osgi.unit.version", bundleVersion(pUnitInfo.getBundle()));
         props.put("osgi.unit.provider", providerClassName);
         // For now, only support punits composed of one bundle
         String bundleId = pUnitBundle.getSymbolicName() + "_" + bundleVersion(pUnitBundle);
         props.put("osgi.managed.bundles", bundleId);
         debug("GeminiServicesUtil JPA services props: ", props);
         return props;
     }
 
     /** 
      * Register the EMF service.
      */
     void tryToRegisterEMFService(PUnitInfo pUnitInfo,
                                         Map<String,Class<?>> anchorClasses,
                                         Dictionary<String,String> props) {
 
         debug("GeminiServicesUtil tryToregister EMF service for ", pUnitInfo.getUnitName());
         // Array of classes being proxied by EMF proxy
         Collection<Class<?>> proxiedClasses = new ArrayList<Class<?>>();
 
         // Load the EMF class. TODO Make this the pUnit loader?
         Class<?> emfClass = GeminiUtil.loadClassFromBundle("javax.persistence.EntityManagerFactory",
                                                            osgiJpaProvider.getBundle());
         
         // Add EMF class and anchor classes to the proxied class collection for EMF proxy
         proxiedClasses.addAll(anchorClasses.values());
         proxiedClasses.add(emfClass);
         Class<?>[] classArray = proxiedClasses.toArray(new Class[0]);
         debug("GeminiServicesUtil EMF proxy class array: ", classArray);
         
         // Get a loader to load the proxy classes
         ClassLoader loader = proxyLoader(pUnitInfo, anchorClasses, emfClass);
 
         // Create proxy impl object for EMF service
         Object emfServiceProxy = createEMFProxy(pUnitInfo, loader, classArray);
 
         // Do we create an EMF service?
         String driverClassName = pUnitInfo.getDriverClassName();
         if (driverClassName == null) {
             debug("GeminiServicesUtil No driver class specified so no factory service created");            
         } else {
             if (!trackDataSourceFactory(pUnitInfo)) {
                 // DSF service was not found.
                 debug("DataSourceFactory service for " + driverClassName + " not found.");
                 // Driver may be packaged in with the p-unit -- try loading it from there
                 try {
                    Class<?> driverCls = pUnitInfo.getBundle().loadClass(driverClassName);
                     debug("JDBC driver " + driverClassName + " found locally.");
                     // We found the driver in the punit. Stop tracking DBAccess service and revert to direct access
                     stopTrackingDataSourceFactory(pUnitInfo);
                 } catch (ClassNotFoundException cnfEx) {
                     // Driver not local, bail and wait for the tracker to detect DBAccess service
                     debug("JDBC driver " + driverClassName + " was not found locally.");
                     warning("DataSourceFactory service for " + driverClassName + " was not found. EMF service not registered.");
                     return;
                 }
             }
             // Either a DBAccess service exists for the driver or the driver is local
 
             // Convert array of classes to class name strings
             String[] classNameArray = new String[classArray.length];
             for (int i=0; i<classArray.length; i++)
                 classNameArray[i] = classArray[i].getName();
 
             // Register the EMF service (using p-unit context) and set registration in PUnitInfo
             ServiceRegistration emfService = null;
             try {
                 emfService = pUnitInfo.getBundle().getBundleContext()
                                .registerService(classNameArray, emfServiceProxy, props);
                 debug("GeminiServicesUtil EMF service: ", emfService);
             } catch (Exception e) {
                 fatalError("GeminiServicesUtil could not register EMF service for " + pUnitInfo.getUnitName(), e);
             }
             pUnitInfo.setEmfService(emfService);
         }
     }
     
     
     /** 
      * Register the EMFBuilder service.
      */
     void registerEMFBuilderService(PUnitInfo pUnitInfo,
                                           Map<String,Class<?>> anchorClasses,
                                           Dictionary<String,String> props) {
     
         debug("GeminiServicesUtil register EMFBuilder service for ", pUnitInfo.getUnitName());
         // Array of classes being proxied by EMFBuilder proxy
         Collection<Class<?>> proxiedClasses = new ArrayList<Class<?>>();
 
         // Load the EMFB class. TODO Make this the pUnit loader?
         Class<?> emfBuilderClass = GeminiUtil.loadClassFromBundle("org.osgi.service.jpa.EntityManagerFactoryBuilder",
                                                                   osgiJpaProvider.getBundle());
 
         // Add EMF class and anchor classes to the proxied class collection for EMF proxy
         proxiedClasses.addAll(anchorClasses.values());
         proxiedClasses.add(emfBuilderClass);
         debug("GeminiServicesUtil EMFBuilder proxied classes: ", proxiedClasses);
         Class<?>[] classArray = proxiedClasses.toArray(new Class[0]);
         
         // Get a loader to load the proxy classes
         ClassLoader loader = proxyLoader(pUnitInfo, anchorClasses, emfBuilderClass);
 
         // Create proxy impl object for EMF service
         Object emfBuilderServiceProxy = createEMFBuilderProxy(pUnitInfo, loader, classArray);
 
         // Convert array of classes to class name strings
         String[] classNameArray = new String[classArray.length];
         for (int i=0; i<classArray.length; i++)
             classNameArray[i] = classArray[i].getName();
     
         //Register the EMFBuilder service and set it in the PUnitInfo
         ServiceRegistration emfBuilderService = null;
         try {
             // TODO Should be registered by p-unit context, not provider context
             // emfBuilderService = pUnitInfo.getBundle().getBundleContext()
             emfBuilderService = osgiJpaProvider.getBundleContext()
                     .registerService(classNameArray, emfBuilderServiceProxy, props);
             debug("GeminiServicesUtil EMFBuilder service: ", emfBuilderService);
         } catch (Exception e) {
             fatalError("GeminiServicesUtil could not register EMFBuilder service for " + pUnitInfo.getUnitName(), e);
         }
         pUnitInfo.setEmfBuilderService(emfBuilderService);
     }    
 
     /*==============================================*/
     /* Data source factory service tracking methods */
     /*==============================================*/
     
     /** 
      * Look up the data source factory service for the specified
      * persistence unit and start a data source factory tracker. 
      * One of two trackers will be created:
      * 
      * a) If the DSF was registered then start a tracker to track when it goes away
      * so that we can remove the dependent EMF service
      * 
      * b) If the DSF was not registered then start a tracker to detect when it comes online
      * 
      * @param pUnitInfo The metadata for this p-unit
      * @return true if the data source factory service was registered, false if it wasn't
      */
     public boolean trackDataSourceFactory(PUnitInfo pUnitInfo) {
         
         debug("GeminiServicesUtil trackDataSourceFactory for p-unit ", pUnitInfo.getUnitName());
         ServiceReference[] dsfRefs = null;
         ServiceTracker tracker = null;
 
         // See if the data source factory service for the driver is registered
         String filter = "(" + DataSourceFactory.OSGI_JDBC_DRIVER_CLASS + "=" + pUnitInfo.getDriverClassName() + ")";
        ServiceReference<DataSourceFactory>[] result = null;
         try {
             dsfRefs = pUnitInfo.getBundle().getBundleContext()
                             .getServiceReferences(DataSourceFactory.class.getName(), filter);
             if (dsfRefs != null) {
                 // We found at least one -- track the first one
                 // *** Note: Race condition still exists where service could disappear before being tracked
                 debug("GeminiServicesUtil starting tracker on existing DSF for ", pUnitInfo.getUnitName());
                 tracker = new ServiceTracker(osgiJpaProvider.getBundleContext(), 
                                              dsfRefs[0],
                                              new DSFOfflineTracker(pUnitInfo, this));
                 pUnitInfo.setDsfService(dsfRefs[0]);
             } else {
                 // No service was found, track for a service that may come in the future 
                 debug("GeminiServicesUtil starting tracker to wait for DSF for ", pUnitInfo.getUnitName());
                 tracker = new ServiceTracker(osgiJpaProvider.getBundleContext(), 
                                              osgiJpaProvider.getBundleContext().createFilter(filter),
                                              new DSFOnlineTracker(pUnitInfo, this));
             }
         } catch (InvalidSyntaxException isEx) {
             fatalError("Bad filter syntax (likely because of missing driver class name)", isEx);
         } catch (Exception ex) {
             fatalError("Unexpected failure to creating DSF service tracker", ex);
         }
         pUnitInfo.setTracker(tracker);
         tracker.open();
         return dsfRefs != null;
     }
 
     /** 
      * Stop tracking the data source factory for the given p-unit
      */
     public void stopTrackingDataSourceFactory(PUnitInfo pUnitInfo) {
         // Clean up the tracker
         debug("GeminiServicesUtil stopTrackingDataSourceFactory", 
               " for p-unit ", pUnitInfo.getUnitName());
         if (pUnitInfo.getTracker() != null) {
             debug("GeminiServicesUtil stopping tracker for p-unit ", 
                     pUnitInfo.getUnitName());
             pUnitInfo.getTracker().close();
             pUnitInfo.setTracker(null);
         }
     }
 
     /** 
      * This method will be invoked by the OnlineTracker when a data source factory 
      * service comes online. This occurs when the p-unit has been processed before the
      * JDBC service has had a chance to be activated or register its DSF services.
      */
     public void dataSourceFactoryOnline(PUnitInfo pUnitInfo, ServiceReference ref) {
         // TODO async handling of data source adding
         debug("dataSourceFactoryOnline, ref=", ref, " for p-unit ", pUnitInfo.getUnitName());
         if (pUnitInfo.getEmf() != null) {
             // EMF has already been created by the user (using EMFBuilder svc). Too late for a DSF service
             warning("DSF " + ref + " came online when EMF for p-unit " + pUnitInfo.getUnitName() +
                     " already existed - ignoring DSF");
         } else {
             // If we already have a DSF service, for some reason, then ignore this one
             if (pUnitInfo.getDsfService() != null) { 
                 warning("DSF service already exists for p-unit " + pUnitInfo.getUnitName() + " - ignoring new DSF service");
             } else {
                 // We registered a tracker and don't have a DSF service so this one must be of interest to us.
                 // Unregister and go through the entire registration process again, assuming we will find this new DSF
                 debug("dataSourceFactoryOnline, unregistering and reregistering EMF services for p-unit ", pUnitInfo.getUnitName());
                 unregisterEMFServices(pUnitInfo);
                 registerEMFServices(pUnitInfo);
             }
         }
     }
 
     /** 
      * This method will be invoked by the OfflineTracker when the data source factory 
      * that we are relying on goes offline. 
      */
     public void dataSourceFactoryOffline(PUnitInfo pUnitInfo, ServiceReference removedRef) {
         // TODO async handling of data source removal
         ServiceReference dsServiceRef = pUnitInfo.getDsfService();
         debug("dataSourceFactoryOffline, p-unit=", pUnitInfo.getUnitName(), "removedRef=", removedRef,
               "storedRef=", dsServiceRef);
         // Verify that this is the dsf service that we care about
         if (dsServiceRef == null) {
             warning("DataSourceFactory " + removedRef + " went offline but no record of it was stored in p-unit " + pUnitInfo.getUnitName());
         } else {
             if (dsServiceRef.compareTo(removedRef) != 0) { 
                 warning("DataSourceFactory " + removedRef + " went offline but a different DSF was stored in p-unit " + pUnitInfo.getUnitName());
             }
         }
         // Unregister the EMF service but leave the Builder
         debug("dataSourceFactoryOffline - unregistering EMF service ", "for p-unit ", pUnitInfo.getUnitName());
         pUnitInfo.setDsfService(null);
         unregisterEMFService(pUnitInfo);
     }
 }
