 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.userregion.internal;
 
 import static org.eclipse.virgo.kernel.osgi.framework.ServiceUtils.PROPERTY_KERNEL_STARTUP_WAIT_LIMIT;
 import static org.eclipse.virgo.kernel.osgi.framework.ServiceUtils.getPotentiallyDelayedService;
 import static org.eclipse.virgo.kernel.osgi.framework.ServiceUtils.getWaitLimitSeconds;
 
 import java.io.IOException;
 import java.util.Dictionary;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Set;
 import java.util.concurrent.TimeoutException;
 
 import org.eclipse.equinox.region.RegionDigraph;
 import org.eclipse.osgi.service.resolver.PlatformAdmin;
 import org.eclipse.virgo.kernel.core.ConfigurationExporter;
 import org.eclipse.virgo.kernel.core.Shutdown;
 import org.eclipse.virgo.kernel.deployer.config.ConfigurationDeployer;
 import org.eclipse.virgo.kernel.deployer.core.ApplicationDeployer;
 import org.eclipse.virgo.kernel.deployer.core.DeployUriNormaliser;
 import org.eclipse.virgo.kernel.install.artifact.ScopeServiceRepository;
 import org.eclipse.virgo.kernel.module.ModuleContextAccessor;
 import org.eclipse.virgo.kernel.osgi.framework.ImportExpander;
 import org.eclipse.virgo.kernel.osgi.framework.OsgiFramework;
 import org.eclipse.virgo.kernel.osgi.framework.OsgiFrameworkUtils;
 import org.eclipse.virgo.kernel.osgi.framework.PackageAdminUtil;
 import org.eclipse.virgo.kernel.osgi.quasi.QuasiFrameworkFactory;
 import org.eclipse.virgo.kernel.services.work.WorkArea;
 import org.eclipse.virgo.kernel.shim.scope.ScopeFactory;
 import org.eclipse.virgo.kernel.userregion.internal.dump.StandardDumpExtractor;
 import org.eclipse.virgo.kernel.userregion.internal.equinox.EquinoxHookRegistrar;
 import org.eclipse.virgo.kernel.userregion.internal.equinox.EquinoxOsgiFramework;
 import org.eclipse.virgo.kernel.userregion.internal.equinox.StandardPackageAdminUtil;
 import org.eclipse.virgo.kernel.userregion.internal.equinox.TransformedManifestProvidingBundleFileWrapper;
 import org.eclipse.virgo.kernel.userregion.internal.importexpansion.ImportExpansionHandler;
 import org.eclipse.virgo.kernel.userregion.internal.management.StateDumpMBeanExporter;
 import org.eclipse.virgo.kernel.userregion.internal.quasi.ResolutionFailureDetective;
 import org.eclipse.virgo.kernel.userregion.internal.quasi.StandardQuasiFrameworkFactory;
 import org.eclipse.virgo.kernel.userregion.internal.quasi.StandardResolutionFailureDetective;
 import org.eclipse.virgo.medic.eventlog.EventLogger;
 import org.eclipse.virgo.medic.eventlog.EventLoggerFactory;
 import org.eclipse.virgo.osgi.extensions.equinox.hooks.MetaInfResourceClassLoaderDelegateHook;
 import org.eclipse.virgo.repository.Repository;
 import org.eclipse.virgo.util.osgi.ServiceRegistrationTracker;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.Constants;
 import org.osgi.service.cm.Configuration;
 import org.osgi.service.cm.ConfigurationAdmin;
 import org.osgi.service.event.EventAdmin;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.osgi.service.packageadmin.ExportedPackage;
 import org.osgi.service.packageadmin.PackageAdmin;
 
 /**
  * {@link BundleActivator} for the Equinox-specific OSGi integration
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Thread-safe.
  * 
  */
 @SuppressWarnings("deprecation")
 public class Activator implements BundleActivator {
 
     private static final long SYSTEM_BUNDLE_ID = 0;
 
     private static final String PROPERTY_USER_REGION_ARTIFACTS = "initialArtifacts";
 
     private static final String PROPERTY_USER_REGION_COMMANDLINE_ARTIFACTS = "commandLineArtifacts";
 
     private static final String USER_REGION_CONFIGURATION_PID = "org.eclipse.virgo.kernel.userregion";
 
     private static final String KERNEL_REGION_CONFIGURATION_PID = "org.eclipse.virgo.kernel";
 
     private final ServiceRegistrationTracker registrationTracker = new ServiceRegistrationTracker();
 
     private volatile EquinoxHookRegistrar hookRegistrar;
 
     private StateDumpMBeanExporter stateDumpMBeanExorter;
     
     private ConsoleConfigurationConvertor consoleConfigurationConvertor = null;
 
     /**
      * {@inheritDoc}
      */
     public void start(BundleContext context) throws Exception {
         publishConfigurations(context);
         ResolutionFailureDetective rfd = createResolutionFailureDetective(context);
         Repository repository = getPotentiallyDelayedService(context, Repository.class);
         PackageAdmin packageAdmin = getPotentiallyDelayedService(context, PackageAdmin.class);
 
         EventLogger eventLogger = getPotentiallyDelayedService(context, EventLoggerFactory.class).createEventLogger(context.getBundle());
 
         RegionDigraph regionDigraph = getPotentiallyDelayedService(context, RegionDigraph.class);
 
         WorkArea workArea = getPotentiallyDelayedService(context, WorkArea.class);
 
         ImportExpansionHandler importExpansionHandler = createImportExpansionHandler(context, packageAdmin, repository, eventLogger);
         this.registrationTracker.track(context.registerService(ImportExpander.class.getName(), importExpansionHandler, null));
 
         TransformedManifestProvidingBundleFileWrapper bundleTransformerHandler = createBundleTransformationHandler(importExpansionHandler);
 
         OsgiFramework osgiFramework = createOsgiFramework(context, packageAdmin, bundleTransformerHandler);
         this.registrationTracker.track(context.registerService(OsgiFramework.class.getName(), osgiFramework, null));
 
         DumpExtractor dumpExtractor = new StandardDumpExtractor(workArea);
         QuasiFrameworkFactory quasiFrameworkFactory = createQuasiFrameworkFactory(context, rfd, repository, bundleTransformerHandler, regionDigraph,
             dumpExtractor);
         this.registrationTracker.track(context.registerService(QuasiFrameworkFactory.class.getName(), quasiFrameworkFactory, null));
 
         EquinoxHookRegistrar hookRegistrar = createHookRegistrar(context, packageAdmin, bundleTransformerHandler);
         hookRegistrar.init();
         this.hookRegistrar = hookRegistrar;
 
         PackageAdminUtil packageAdminUtil = createPackageAdminUtil(context);
         this.registrationTracker.track(context.registerService(PackageAdminUtil.class.getName(), packageAdminUtil, null));
 
         scheduleRegistrationOfServiceScopingRegistryHooks(context, eventLogger);
 
         Dictionary<String, Object> properties = new Hashtable<String, Object>();
         properties.put(Constants.SERVICE_RANKING, Integer.MIN_VALUE);
         this.registrationTracker.track(context.registerService(ModuleContextAccessor.class.getName(), new EmptyModuleContextAccessor(), properties));
 
         scheduleInitialArtifactDeployerCreation(context, eventLogger);
 
         context.registerService(ConfigurationDeployer.class, new UserRegionConfigurationDeployer(context), null);
         initializeConsoleConfigurationConvertor(context);
         this.stateDumpMBeanExorter = new StateDumpMBeanExporter(quasiFrameworkFactory);
     }
 
     /**
      * This method gets the kernel and user regions configurations from the kernel region and publishes them in the
      * configuration admin in the user region.
      * 
      * @throws Exception
      */
     private void publishConfigurations(BundleContext context) throws Exception {
         ConfigurationExporter configurationExporter = getPotentiallyDelayedService(context, ConfigurationExporter.class);
         ConfigurationAdmin admin = getPotentiallyDelayedService(context, ConfigurationAdmin.class);
 
         try {
             publishConfigurationFromKernelRegion(configurationExporter.getUserRegionConfigurationProperties(),
                 admin.getConfiguration(USER_REGION_CONFIGURATION_PID));
             publishConfigurationFromKernelRegion(configurationExporter.getKernelRegionConfigurationProperties(),
                 admin.getConfiguration(KERNEL_REGION_CONFIGURATION_PID));
         } catch (IOException e) {
             throw new RuntimeException("Failed to publish required configurations. Startup sequence can't continue", e);
         }
     }
 
     private void publishConfigurationFromKernelRegion(Dictionary<?, ?> configurationProperties, Configuration config) throws IOException {
         config.update(configurationProperties);
     }
 
     private ResolutionFailureDetective createResolutionFailureDetective(BundleContext context) {
         PlatformAdmin platformAdmin = OsgiFrameworkUtils.getService(context, PlatformAdmin.class).getService();
         return new StandardResolutionFailureDetective(platformAdmin);
     }
 
     private OsgiFramework createOsgiFramework(BundleContext context, PackageAdmin packageAdmin,
         TransformedManifestProvidingBundleFileWrapper bundleTransformerHandler) {
         return new EquinoxOsgiFramework(context, packageAdmin, bundleTransformerHandler);
     }
 
     private QuasiFrameworkFactory createQuasiFrameworkFactory(BundleContext bundleContext, ResolutionFailureDetective detective,
         Repository repository, TransformedManifestProvidingBundleFileWrapper bundleTransformerHandler, RegionDigraph regionDigraph,
         DumpExtractor dumpExtractor) {
         return new StandardQuasiFrameworkFactory(bundleContext, detective, repository, bundleTransformerHandler, regionDigraph, dumpExtractor);
     }
 
     private TransformedManifestProvidingBundleFileWrapper createBundleTransformationHandler(ImportExpansionHandler importExpander) {
         return new TransformedManifestProvidingBundleFileWrapper(importExpander);
     }
 
     private ImportExpansionHandler createImportExpansionHandler(BundleContext context, PackageAdmin packageAdmin, Repository repository,
         EventLogger eventLogger) {
 
         Set<String> packagesExportedBySystemBundle = new HashSet<String>(30);
         ExportedPackage[] exportedPackages = packageAdmin.getExportedPackages(context.getBundle(SYSTEM_BUNDLE_ID));
 
         for (ExportedPackage exportedPackage : exportedPackages) {
             packagesExportedBySystemBundle.add(exportedPackage.getName());
         }
 
         return new ImportExpansionHandler(repository, context, packagesExportedBySystemBundle, eventLogger);
     }
 
     private EquinoxHookRegistrar createHookRegistrar(BundleContext context, PackageAdmin packageAdmin,
         TransformedManifestProvidingBundleFileWrapper bundleFileWrapper) {
         MetaInfResourceClassLoaderDelegateHook hook = new MetaInfResourceClassLoaderDelegateHook(context, packageAdmin);
         return new EquinoxHookRegistrar(bundleFileWrapper, hook);
     }
 
     private PackageAdminUtil createPackageAdminUtil(BundleContext context) {
         return new StandardPackageAdminUtil(context);
     }
 
     private void scheduleRegistrationOfServiceScopingRegistryHooks(final BundleContext context, EventLogger eventLogger) {
         Runnable runnable = new ServiceScopingHookRegisteringRunnable(context, this.registrationTracker, eventLogger);
         Thread thread = new Thread(runnable);
         thread.setDaemon(true);
         thread.start();
     }
 
     private void scheduleInitialArtifactDeployerCreation(BundleContext context, EventLogger eventLogger) {
         KernelStartedAwaiter startedAwaiter = new KernelStartedAwaiter();
 
         Dictionary<String, String> properties = new Hashtable<String, String>();
         properties.put(EventConstants.EVENT_TOPIC, "org/eclipse/virgo/kernel/*");
         this.registrationTracker.track(context.registerService(EventHandler.class.getName(), startedAwaiter, properties));
 
         Runnable runnable = new InitialArtifactDeployerCreatingRunnable(context, eventLogger, this.registrationTracker, startedAwaiter);
         Thread thread = new Thread(runnable);
         thread.setDaemon(true);
         thread.start();
     }
     
     private void initializeConsoleConfigurationConvertor(BundleContext context) {
     	consoleConfigurationConvertor = new ConsoleConfigurationConvertor(context);
     	consoleConfigurationConvertor.start();
     }
 
     /**
      * {@inheritDoc}
      */
     public void stop(BundleContext context) throws Exception {
         this.registrationTracker.unregisterAll();
         
         if (this.consoleConfigurationConvertor != null) {
         	this.consoleConfigurationConvertor.stop();
         }
 
         StateDumpMBeanExporter localStateDumpMBeanExporter = this.stateDumpMBeanExorter;
         if (localStateDumpMBeanExporter != null) {
             localStateDumpMBeanExporter.close();
             this.stateDumpMBeanExorter = null;
         }
 
         EquinoxHookRegistrar hookRegistrar = this.hookRegistrar;
         if (hookRegistrar != null) {
             hookRegistrar.destroy();
             this.hookRegistrar = null;
         }
         
     }
 
     private static final class ServiceScopingHookRegisteringRunnable implements Runnable {
 
         private final EventLogger eventLogger;
 
         private final BundleContext context;
 
         private final ServiceRegistrationTracker registrationTracker;
 
         public ServiceScopingHookRegisteringRunnable(BundleContext context, ServiceRegistrationTracker registrationTracker, EventLogger eventLogger) {
             this.context = context;
             this.registrationTracker = registrationTracker;
             this.eventLogger = eventLogger;
         }
 
         public void run() {
             ScopeFactory scopeFactory = OsgiFrameworkUtils.getService(context, ScopeFactory.class).getService();
             Shutdown shutdown = OsgiFrameworkUtils.getService(context, Shutdown.class).getService();
 
             try {
                 ScopeServiceRepository scopeServiceRepository = getPotentiallyDelayedService(context, ScopeServiceRepository.class);
 
                 ServiceScopingStrategy serviceScopingStrategy = new ServiceScopingStrategy(scopeFactory, scopeServiceRepository);
 
                 ServiceScopingRegistryHook serviceScopingRegistryHook = new ServiceScopingRegistryHook(serviceScopingStrategy);
 
                 this.registrationTracker.track(context.registerService(new String[] { "org.osgi.framework.hooks.service.FindHook",
                     "org.osgi.framework.hooks.service.EventHook" }, serviceScopingRegistryHook, null));
             } catch (TimeoutException te) {
                 this.eventLogger.log(UserRegionLogEvents.KERNEL_SERVICE_NOT_AVAILABLE, te, getWaitLimitSeconds(), PROPERTY_KERNEL_STARTUP_WAIT_LIMIT);
                 shutdown.immediateShutdown();
             } catch (InterruptedException ie) {
                 this.eventLogger.log(UserRegionLogEvents.USERREGION_START_INTERRUPTED, ie);
                 shutdown.immediateShutdown();
             }
         }
     }
 
     private static final class InitialArtifactDeployerCreatingRunnable implements Runnable {
 
         private final BundleContext context;
 
         private final EventLogger eventLogger;
 
         private final KernelStartedAwaiter startAwaiter;
 
         private final ServiceRegistrationTracker registrationTracker;
 
         public InitialArtifactDeployerCreatingRunnable(BundleContext context, EventLogger eventLogger,
             ServiceRegistrationTracker registrationTracker, KernelStartedAwaiter startAwaiter) {
             this.context = context;
             this.eventLogger = eventLogger;
             this.startAwaiter = startAwaiter;
             this.registrationTracker = registrationTracker;
         }
 
         /**
          * {@inheritDoc}
          */
         public void run() {
             EventAdmin eventAdmin = OsgiFrameworkUtils.getService(context, EventAdmin.class).getService();
             Shutdown shutdown = OsgiFrameworkUtils.getService(context, Shutdown.class).getService();
 
             try {
                 DeployUriNormaliser uriNormaliser = getPotentiallyDelayedService(context, DeployUriNormaliser.class);
                 ApplicationDeployer deployer = getPotentiallyDelayedService(context, ApplicationDeployer.class);
 
                 Dictionary<String, String> artifactConfiguration = getRegionArtifactConfiguration();
 
                 InitialArtifactDeployer initialArtifactDeployer = new InitialArtifactDeployer(this.startAwaiter, deployer,
                     artifactConfiguration.get(PROPERTY_USER_REGION_ARTIFACTS), artifactConfiguration.get(PROPERTY_USER_REGION_COMMANDLINE_ARTIFACTS),
                     uriNormaliser, eventAdmin, eventLogger, shutdown);
                 Dictionary<String, String> properties = new Hashtable<String, String>();
                 properties.put(EventConstants.EVENT_TOPIC, "org/eclipse/virgo/kernel/*");
                 this.registrationTracker.track(context.registerService(EventHandler.class.getName(), initialArtifactDeployer, properties));
 
                 initialArtifactDeployer.deployArtifacts();
             } catch (TimeoutException te) {
                 this.eventLogger.log(UserRegionLogEvents.KERNEL_SERVICE_NOT_AVAILABLE, te, getWaitLimitSeconds(), PROPERTY_KERNEL_STARTUP_WAIT_LIMIT);
                 shutdown.immediateShutdown();
             } catch (InterruptedException ie) {
                 this.eventLogger.log(UserRegionLogEvents.USERREGION_START_INTERRUPTED, ie);
                 shutdown.immediateShutdown();
             }
         }
 
         @SuppressWarnings("unchecked")
         private Dictionary<String, String> getRegionArtifactConfiguration() {
             ConfigurationAdmin configAdmin = OsgiFrameworkUtils.getService(this.context, ConfigurationAdmin.class).getService();
             try {
                 Configuration config = configAdmin.getConfiguration(USER_REGION_CONFIGURATION_PID, null);
                 Dictionary<String, String> properties = (Dictionary<String, String>) config.getProperties();
                 return properties;
             } catch (IOException ioe) {
                 throw new RuntimeException("Failed to read region artifact configuration", ioe);
             }
         }
 
     }
 
 }
