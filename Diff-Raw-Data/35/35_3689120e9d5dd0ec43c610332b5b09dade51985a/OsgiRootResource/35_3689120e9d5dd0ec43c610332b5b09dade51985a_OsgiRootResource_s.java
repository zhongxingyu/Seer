 package org.apache.felix.ipojo.everest.osgi;
 
 import org.apache.felix.ipojo.annotations.*;
 import org.apache.felix.ipojo.everest.core.Everest;
 import org.apache.felix.ipojo.everest.impl.AbstractResourceManager;
 import org.apache.felix.ipojo.everest.impl.DefaultParameter;
 import org.apache.felix.ipojo.everest.impl.DefaultRelation;
 import org.apache.felix.ipojo.everest.impl.ImmutableResourceMetadata;
 import org.apache.felix.ipojo.everest.osgi.bundle.BundleResourceManager;
 import org.apache.felix.ipojo.everest.osgi.config.ConfigAdminResourceManager;
 import org.apache.felix.ipojo.everest.osgi.deploy.DeploymentAdminResourceManager;
 import org.apache.felix.ipojo.everest.osgi.log.LogServiceResourceManager;
 import org.apache.felix.ipojo.everest.osgi.packages.PackageResourceManager;
 import org.apache.felix.ipojo.everest.osgi.service.ServiceResourceManager;
 import org.apache.felix.ipojo.everest.services.*;
 import org.osgi.framework.*;
 import org.osgi.framework.startlevel.FrameworkStartLevel;
 import org.osgi.framework.wiring.FrameworkWiring;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.cm.ConfigurationListener;
 import org.osgi.service.deploymentadmin.DeploymentAdmin;
 import org.osgi.service.log.LogReaderService;
 import org.osgi.util.tracker.BundleTracker;
 import org.osgi.util.tracker.BundleTrackerCustomizer;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Osgi domain root resource.
  * Represents the osgi framework
  */
 @Component
 @Provides(
         specifications = Resource.class,
         properties = {@StaticServiceProperty(type = "String", name = "type", value = "osgi")})
 @Instantiate
 public class OsgiRootResource extends AbstractResourceManager implements BundleTrackerCustomizer, ServiceTrackerCustomizer, FrameworkListener {
 
     /**
      * Name of the osgi root resource
      */
     public static final String OSGI_ROOT = "osgi";
 
     /**
      * Description of the osgi root resource
      */
     public static final String OSGI_DESCRIPTION = "This root represents osgi framework and its subresources";
 
     /**
      * Path to Osgi root : "/osgi"
      */
     public static final Path OSGI_ROOT_PATH = Path.from(Path.SEPARATOR + OSGI_ROOT);
 
     /**
      * Relation name for stopping the framework
      */
     public static final String FRAMEWORK_STOP_RELATION = "stop";
 
     /**
      * Relation name for updating the framework
      */
     public static final String FRAMEWORK_UPDATE_RELATION = "update";
 
     /**
      * Relation name for restarting the framework
      */
     private static final String FRAMEWORK_RESTART_RELATION = "restart";
 
     /**
      * Parameter name for setting start level on update relation
      */
     public static final String STARTLEVEL_PARAMETER = "startlevel";
 
     /**
      * Parameter name for setting initial bundle start level on update relation
      */
     public static final String STARTLEVEL_BUNDLE_PARAMETER = "startlevel.bundle";
 
     /**
      * Parameter name for restarting the framework on update relation
      */
     public static final String FRAMEWORK_RESTART_PARAMETER = "restart";
 
     /**
      * Lock object for adding and removing late appearing resources
      */
     private final Object resourceLock = new Object();
 
     /**
      * Bundle context of this everest domain
      */
     private final BundleContext m_context;
 
     /**
      * Tracker for bundles
      */
     private final BundleTracker m_bundleTracker;
 
     /**
      * Tracker for services
      */
     private final ServiceTracker m_serviceTracker;
 
     /**
      * Static metadata for framework
      */
     private final ResourceMetadata m_metadata;
 
     /**
      * Relations list
      */
     private final ArrayList<Relation> m_relations;
 
     /**
      * Framework bundle
      */
     private final Bundle m_frameworkBundle;
 
     /**
      * First level resource manager for bundles.
      * Always present
      */
     private final BundleResourceManager m_bundleResourceManager;
 
     /**
      * First level resource manager for packages.
      * Always present
      */
     private final PackageResourceManager m_packageResourceManager;
 
     /**
      * First level resource manager for services.
      * Always present
      */
     private final ServiceResourceManager m_serviceResourceManager;
 
     /**
      * First level resource manager for config admin
      * Present only if configuration admin service is found
      */
     private ConfigAdminResourceManager m_configResourceManager;
 
     /**
      * Service registration for configuration listener
      */
     private ServiceRegistration<ConfigurationListener> m_configurationListenerServiceRegistration;
 
     /**
      * First level resource manager for deployment package admin
      * Present only if deployment package admin service is found
      */
     private DeploymentAdminResourceManager m_deploymentResourceManager;
 
     /**
      * First level resource manager for logs
      * Present only if log reader service is found
      */
     private LogServiceResourceManager m_logResourceManager;
 
     /**
      * Constructor for Osgi root resource
      *
      * @param context bundle context of the everest-osgi bundle
      */
     public OsgiRootResource(BundleContext context) {
         super(OSGI_ROOT, OSGI_DESCRIPTION);
         m_context = context;
         // Initialize subresource managers
         m_bundleResourceManager = BundleResourceManager.getInstance();
         m_packageResourceManager = PackageResourceManager.getInstance();
         m_serviceResourceManager = ServiceResourceManager.getInstance();
         m_frameworkBundle = m_context.getBundle(0);
         FrameworkWiring fwiring = m_frameworkBundle.adapt(FrameworkWiring.class);
         BundleContext fwContext = m_frameworkBundle.getBundleContext();
         // Construct static framework metadata
         ImmutableResourceMetadata.Builder metadataBuilder = new ImmutableResourceMetadata.Builder(super.getMetadata());
         //TODO take some metadata from the framework
         metadataBuilder.set(Constants.FRAMEWORK_VERSION, fwContext.getProperty(Constants.FRAMEWORK_VERSION));
         metadataBuilder.set(Constants.FRAMEWORK_VENDOR, fwContext.getProperty(Constants.FRAMEWORK_VENDOR));
         metadataBuilder.set(Constants.FRAMEWORK_LANGUAGE, fwContext.getProperty(Constants.FRAMEWORK_LANGUAGE));
         metadataBuilder.set(Constants.FRAMEWORK_PROCESSOR, fwContext.getProperty(Constants.FRAMEWORK_PROCESSOR));
         metadataBuilder.set(Constants.FRAMEWORK_OS_NAME, fwContext.getProperty(Constants.FRAMEWORK_OS_NAME));
         metadataBuilder.set(Constants.FRAMEWORK_OS_VERSION, fwContext.getProperty(Constants.FRAMEWORK_OS_VERSION));
         metadataBuilder.set(Constants.FRAMEWORK_UUID, fwContext.getProperty(Constants.FRAMEWORK_UUID));
         metadataBuilder.set(Constants.SUPPORTS_FRAMEWORK_EXTENSION, fwContext.getProperty(Constants.SUPPORTS_FRAMEWORK_EXTENSION));
         metadataBuilder.set(Constants.SUPPORTS_FRAMEWORK_FRAGMENT, fwContext.getProperty(Constants.SUPPORTS_FRAMEWORK_FRAGMENT));
         metadataBuilder.set(Constants.SUPPORTS_FRAMEWORK_REQUIREBUNDLE, fwContext.getProperty(Constants.SUPPORTS_FRAMEWORK_REQUIREBUNDLE));
         metadataBuilder.set(Constants.SUPPORTS_BOOTCLASSPATH_EXTENSION, fwContext.getProperty(Constants.SUPPORTS_BOOTCLASSPATH_EXTENSION));
         metadataBuilder.set(Constants.FRAMEWORK_BOOTDELEGATION, fwContext.getProperty(Constants.FRAMEWORK_BOOTDELEGATION));
         metadataBuilder.set(Constants.FRAMEWORK_SYSTEMPACKAGES, fwContext.getProperty(Constants.FRAMEWORK_SYSTEMPACKAGES));
         m_metadata = metadataBuilder.build();
 
         // Initialize bundle & service trackers
         int stateMask = Bundle.ACTIVE | Bundle.INSTALLED | Bundle.RESOLVED | Bundle.STARTING | Bundle.STOPPING;
         Filter allServicesFilter = null;
         try {
             StringBuilder sb = new StringBuilder();
             sb.append("(");
             sb.append(Constants.OBJECTCLASS);
             sb.append("=*)");
             allServicesFilter = FrameworkUtil.createFilter(sb.toString());
         } catch (InvalidSyntaxException e) {
             // Should never happen
             throw new RuntimeException(e.getMessage());
         }
 
         m_bundleTracker = new BundleTracker(m_context, stateMask, this);
         m_serviceTracker = new ServiceTracker(m_context, allServicesFilter, this);
 
         m_relations = new ArrayList<Relation>();
         m_relations.add(new DefaultRelation(getPath(), Action.DELETE, FRAMEWORK_STOP_RELATION, "Stops the osgi framework"));
         m_relations.add(new DefaultRelation(getPath(), Action.UPDATE, FRAMEWORK_UPDATE_RELATION, "updates start level",
                 new DefaultParameter()
                         .name(STARTLEVEL_BUNDLE_PARAMETER)
                         .description(STARTLEVEL_BUNDLE_PARAMETER)
                         .type(Integer.class)
                         .optional(true),
                 new DefaultParameter()
                         .name(STARTLEVEL_PARAMETER)
                         .description(STARTLEVEL_PARAMETER)
                         .type(Integer.class)
                         .optional(true)));
         m_relations.add(new DefaultRelation(getPath(), Action.UPDATE, FRAMEWORK_RESTART_RELATION, "Restarts the osgi framework",
                 new DefaultParameter()
                         .name(FRAMEWORK_RESTART_PARAMETER)
                         .description(FRAMEWORK_RESTART_PARAMETER)
                         .type(Boolean.class)
                         .optional(true)));
     }
 
     @Validate
     public void started() {
         // start trackers
         m_context.addFrameworkListener(this);
         m_bundleTracker.open();
         m_serviceTracker.open(true);
     }
 
     @Invalidate
     public void stopped() {
         // stop trackers
         m_context.removeFrameworkListener(this);
         m_bundleTracker.close();
         m_serviceTracker.close();
     }
 
     @Override
     public ResourceMetadata getMetadata() {
         ImmutableResourceMetadata.Builder metadataBuilder = new ImmutableResourceMetadata.Builder(m_metadata);
         // add Start Level metadata
         FrameworkStartLevel frameworkStartLevel = m_frameworkBundle.adapt(FrameworkStartLevel.class);
         metadataBuilder.set(STARTLEVEL_BUNDLE_PARAMETER, frameworkStartLevel.getInitialBundleStartLevel());
         metadataBuilder.set(STARTLEVEL_PARAMETER, frameworkStartLevel.getStartLevel());
         return metadataBuilder.build();
     }
 
     @Override
     public List<Resource> getResources() {
         ArrayList<Resource> resources = new ArrayList<Resource>();
         synchronized (resourceLock) {
             resources.add(m_bundleResourceManager);
             resources.add(m_packageResourceManager);
             resources.add(m_serviceResourceManager);
             if (m_configResourceManager != null) {
                 resources.add(m_configResourceManager);
             }
             if (m_deploymentResourceManager != null) {
                 resources.add(m_deploymentResourceManager);
             }
             if (m_logResourceManager != null) {
                 resources.add(m_logResourceManager);
             }
         }
         return resources;
     }
 
     @Override
     public List<Relation> getRelations() {
         ArrayList<Relation> relations = new ArrayList<Relation>();
         relations.addAll(super.getRelations());
         relations.addAll(m_relations);
         return relations;
     }
 
     @Override
     public Resource update(Request request) throws IllegalActionOnResourceException {
         FrameworkStartLevel frameworkStartLevel = m_frameworkBundle.adapt(FrameworkStartLevel.class);
         Integer bundleStartLevel = request.get(STARTLEVEL_BUNDLE_PARAMETER, Integer.class);
         if (bundleStartLevel != null) {
             frameworkStartLevel.setInitialBundleStartLevel(bundleStartLevel);
         }
         Integer startLevel = request.get(STARTLEVEL_PARAMETER, Integer.class);
         if (startLevel != null) {
             frameworkStartLevel.setStartLevel(startLevel);
         }
        Boolean restart = request.get("restart", Boolean.class);
         if (restart != null && restart) {
             try {
                 //restarting framework
                 m_frameworkBundle.update();
             } catch (BundleException e) {
                 throw new IllegalActionOnResourceException(request, e.getMessage());
             }
         }
         return this;
     }
 
     @Override
     public Resource delete(Request request) throws IllegalActionOnResourceException {
         // R.I.P :REST in PEACE
         try {
             m_frameworkBundle.stop();
         } catch (BundleException e) {
             throw new IllegalActionOnResourceException(request, e.getMessage());
         }
         return null;
     }
 
     @Override
     public boolean isObservable() {
         return true;
     }
 
     // Config Admin Bind / Unbind
     // =================================================================================================================
 
     @Bind(id = "configadmin", optional = true, aggregate = false)
     public void bindConfigAdmin(ConfigurationAdmin configAdmin) {
         synchronized (resourceLock) {
             m_configResourceManager = new ConfigAdminResourceManager(configAdmin);
             m_configurationListenerServiceRegistration = m_context.registerService(ConfigurationListener.class, m_configResourceManager, null);
         }
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     @Unbind(id = "configadmin")
     public void unbindConfigAdmin(ConfigurationAdmin configAdmin) {
         synchronized (resourceLock) {
             m_configurationListenerServiceRegistration.unregister();
             m_configResourceManager = null;
         }
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     // Deploy Admin Bind / Unbind
     // =================================================================================================================
 
     @Bind(id = "deploymentadmin", optional = true, aggregate = false)
     public void bindDeploymentAdmin(DeploymentAdmin deploymentAdmin) {
         synchronized (resourceLock) {
             m_deploymentResourceManager = new DeploymentAdminResourceManager(deploymentAdmin);
         }
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     @Unbind(id = "deploymentadmin")
     public void unbindDeploymentAdmin(DeploymentAdmin deploymentAdmin) {
         synchronized (resourceLock) {
             m_deploymentResourceManager = null;
         }
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     // Log Service Bind / Unbind
     // =================================================================================================================
 
     @Bind(id = "logservice", optional = true, aggregate = false)
     public void bindLogService(LogReaderService logService) {
         synchronized (resourceLock) {
             m_logResourceManager = new LogServiceResourceManager(logService);
         }
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     @Unbind(id = "logservice")
     public void unbindLogService(LogReaderService logService) {
         synchronized (resourceLock) {
             m_logResourceManager = null;
         }
         Everest.postResource(ResourceEvent.UPDATED, this);
     }
 
     // Bundle Tracker Methods
     // =================================================================================================================
 
     public Object addingBundle(Bundle bundle, BundleEvent bundleEvent) {
         m_bundleResourceManager.addBundle(bundle);
         if (bundle.getState() == Bundle.ACTIVE || bundle.getState() == Bundle.RESOLVED) {
             m_packageResourceManager.addPackagesFrom(bundle);
         }
         return bundle.getBundleId();
     }
 
     public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
         m_bundleResourceManager.modifyBundle(bundle);
         if (bundleEvent.getType() == BundleEvent.RESOLVED) {
             m_packageResourceManager.addPackagesFrom(bundle);
         }
         if (bundleEvent.getType() == BundleEvent.UNRESOLVED) {
             m_packageResourceManager.removePackagesFrom(bundle);
         }
     }
 
     public void removedBundle(Bundle bundle, BundleEvent bundleEvent, Object o) {
         m_bundleResourceManager.removeBundle(bundle);
     }
 
     // Service Tracker Methods
     // =================================================================================================================
 
     public Object addingService(ServiceReference serviceReference) {
         Object serviceId = serviceReference.getProperty(Constants.SERVICE_ID);
         m_serviceResourceManager.addService(serviceReference);
         return serviceId;
     }
 
     public void modifiedService(ServiceReference serviceReference, Object o) {
         m_serviceResourceManager.modifyService(serviceReference);
     }
 
     public void removedService(ServiceReference serviceReference, Object o) {
         m_serviceResourceManager.removeService(serviceReference);
     }
 
     // Framework Listener Method
     // =================================================================================================================
 
     public void frameworkEvent(FrameworkEvent event) {
         switch (event.getType()) {
             case FrameworkEvent.STARTED:
                 Everest.postResource(ResourceEvent.CREATED, this);
                 break;
             case FrameworkEvent.STOPPED:
                 Everest.postResource(ResourceEvent.DELETED, this);
                 break;
             case FrameworkEvent.STARTLEVEL_CHANGED:
                 Everest.postResource(ResourceEvent.UPDATED, this);
                 break;
             case FrameworkEvent.STOPPED_UPDATE:
                 Everest.postResource(ResourceEvent.UPDATED, this);
                 break;
             case FrameworkEvent.PACKAGES_REFRESHED:
                 Everest.postResource(ResourceEvent.UPDATED, this);
                 break;
         }
     }
 }
