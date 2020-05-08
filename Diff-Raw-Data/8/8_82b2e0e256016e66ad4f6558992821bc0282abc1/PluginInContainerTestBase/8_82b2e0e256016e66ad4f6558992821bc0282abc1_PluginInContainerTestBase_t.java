 package com.atlassian.plugin.osgi;
 
 import com.atlassian.plugin.DefaultModuleDescriptorFactory;
 import com.atlassian.plugin.ModuleDescriptorFactory;
 import com.atlassian.plugin.PluginAccessor;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
 import com.atlassian.plugin.factories.LegacyDynamicPluginFactory;
 import com.atlassian.plugin.factories.PluginFactory;
 import com.atlassian.plugin.hostcontainer.SimpleConstructorHostContainer;
 import com.atlassian.plugin.loaders.BundledPluginLoader;
 import com.atlassian.plugin.loaders.DirectoryPluginLoader;
 import com.atlassian.plugin.loaders.PluginLoader;
 import com.atlassian.plugin.manager.DefaultPluginManager;
 import com.atlassian.plugin.manager.store.MemoryPluginPersistentStateStore;
 import com.atlassian.plugin.module.ClassPrefixModuleFactory;
 import com.atlassian.plugin.module.ModuleFactory;
 import com.atlassian.plugin.module.PrefixDelegatingModuleFactory;
 import com.atlassian.plugin.module.PrefixModuleFactory;
 import com.atlassian.plugin.osgi.container.OsgiContainerManager;
 import com.atlassian.plugin.osgi.container.OsgiPersistentCache;
 import com.atlassian.plugin.osgi.container.PackageScannerConfiguration;
 import com.atlassian.plugin.osgi.container.felix.FelixOsgiContainerManager;
 import com.atlassian.plugin.osgi.container.impl.DefaultOsgiPersistentCache;
 import com.atlassian.plugin.osgi.container.impl.DefaultPackageScannerConfiguration;
 import com.atlassian.plugin.osgi.factory.OsgiBundleFactory;
 import com.atlassian.plugin.osgi.factory.OsgiPluginFactory;
 import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
 import com.atlassian.plugin.osgi.hostcomponents.InstanceBuilder;
 import com.atlassian.plugin.osgi.module.BeanPrefixModuleFactory;
 import com.atlassian.plugin.repositories.FilePluginInstaller;
 import com.google.common.collect.ImmutableSet;
 import junit.framework.TestCase;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 /**
  * Base for in-container unit tests
  */
 public abstract class PluginInContainerTestBase extends TestCase
 {
     protected OsgiContainerManager osgiContainerManager;
     protected File tmpDir;
     protected File cacheDir;
     protected File pluginsDir;
     protected ModuleDescriptorFactory moduleDescriptorFactory;
     protected DefaultPluginManager pluginManager;
     protected PluginEventManager pluginEventManager;
     protected ModuleFactory moduleFactory;
     protected SimpleConstructorHostContainer hostContainer;
 
     @Override
     public void setUp() throws Exception
     {
         tmpDir = new File("target/plugin-temp").getAbsoluteFile();
         if (tmpDir.exists())
         {
             FileUtils.cleanDirectory(tmpDir);
         }
         tmpDir.mkdirs();
         cacheDir = new File(tmpDir, "cache");
         cacheDir.mkdir();
         pluginsDir = new File(tmpDir, "plugins");
         pluginsDir.mkdir();
         this.pluginEventManager = new DefaultPluginEventManager();
         moduleFactory = new PrefixDelegatingModuleFactory(ImmutableSet.<PrefixModuleFactory>of(
             new ClassPrefixModuleFactory(hostContainer),
             new BeanPrefixModuleFactory()));
         hostContainer = createHostContainer(new HashMap<Class<?>, Object>());
     }
 
     protected SimpleConstructorHostContainer createHostContainer(Map<Class<?>, Object> originalContext)
     {
         Map<Class<?>, Object> context = new HashMap<Class<?>, Object>(originalContext);
         context.put(ModuleFactory.class, moduleFactory);
         return new SimpleConstructorHostContainer(context);
     }
 
     @Override
     public void tearDown() throws Exception
     {
         if (osgiContainerManager != null)
         {
             osgiContainerManager.stop();
         }
         FileUtils.deleteDirectory(tmpDir);
         osgiContainerManager = null;
         tmpDir = null;
         pluginsDir = null;
         moduleDescriptorFactory = null;
         pluginManager = null;
         pluginEventManager = null;
         moduleFactory = null;
         hostContainer = null;
     }
 
     protected void initPluginManager() throws Exception
     {
         initPluginManager(null, new DefaultModuleDescriptorFactory(hostContainer));
     }
 
     protected void initPluginManager(final HostComponentProvider hostComponentProvider) throws Exception
     {
         initPluginManager(hostComponentProvider, new DefaultModuleDescriptorFactory(hostContainer));
     }
 
     protected void initPluginManager(final HostComponentProvider hostComponentProvider, final ModuleDescriptorFactory moduleDescriptorFactory, final String version)
             throws Exception
     {
         final PackageScannerConfiguration scannerConfig = buildScannerConfiguration(version);
         HostComponentProvider requiredWrappingProvider = getWrappingHostComponentProvider(hostComponentProvider);
         OsgiPersistentCache cache = new DefaultOsgiPersistentCache(cacheDir);
         osgiContainerManager = new FelixOsgiContainerManager(cache, scannerConfig, requiredWrappingProvider, pluginEventManager);
 
         final LegacyDynamicPluginFactory legacyFactory = new LegacyDynamicPluginFactory(PluginAccessor.Descriptor.FILENAME, tmpDir);
         final OsgiPluginFactory osgiPluginDeployer = new OsgiPluginFactory(PluginAccessor.Descriptor.FILENAME, (String) null, cache, osgiContainerManager, pluginEventManager);
         final OsgiBundleFactory osgiBundleFactory = new OsgiBundleFactory(osgiContainerManager, pluginEventManager);
 
         final DirectoryPluginLoader loader = new DirectoryPluginLoader(pluginsDir, Arrays.asList(legacyFactory, osgiPluginDeployer, osgiBundleFactory),
                 new DefaultPluginEventManager());
         initPluginManager(moduleDescriptorFactory, loader);
     }
 
     protected void initPluginManager(final HostComponentProvider hostComponentProvider, final ModuleDescriptorFactory moduleDescriptorFactory)
             throws Exception
     {
         initPluginManager(hostComponentProvider, moduleDescriptorFactory, (String) null);
     }
 
     protected void initPluginManager(final ModuleDescriptorFactory moduleDescriptorFactory, PluginLoader loader)
             throws Exception
     {
         this.moduleDescriptorFactory = moduleDescriptorFactory;
         pluginManager = new DefaultPluginManager(new MemoryPluginPersistentStateStore(), Arrays.<PluginLoader>asList(loader), moduleDescriptorFactory,
                 pluginEventManager);
         pluginManager.setPluginInstaller(new FilePluginInstaller(pluginsDir));
         pluginManager.init();
     }
 
     protected void initBundlingPluginManager(final ModuleDescriptorFactory moduleDescriptorFactory, File... bundledPluginJars) throws Exception
     {
         this.moduleDescriptorFactory = moduleDescriptorFactory;
         final PackageScannerConfiguration scannerConfig = buildScannerConfiguration("1.0");
         HostComponentProvider requiredWrappingProvider = getWrappingHostComponentProvider(null);
         OsgiPersistentCache cache = new DefaultOsgiPersistentCache(cacheDir);
         osgiContainerManager = new FelixOsgiContainerManager(cache, scannerConfig, requiredWrappingProvider, pluginEventManager);
 
         final OsgiPluginFactory osgiPluginDeployer = new OsgiPluginFactory(PluginAccessor.Descriptor.FILENAME, (String) null, cache, osgiContainerManager, pluginEventManager);
 
         final DirectoryPluginLoader loader = new DirectoryPluginLoader(pluginsDir, Arrays.<PluginFactory>asList(osgiPluginDeployer),
             new DefaultPluginEventManager());
 
         File zip = new File(bundledPluginJars[0].getParentFile(), "bundled-plugins.zip");
         for (File bundledPluginJar : bundledPluginJars)
         {
             ZipOutputStream stream = null;
             InputStream in = null;
             try
             {
                 stream = new ZipOutputStream(new FileOutputStream(zip));
                 in = new FileInputStream(bundledPluginJar);
                 stream.putNextEntry(new ZipEntry(bundledPluginJar.getName()));
                 IOUtils.copy(in, stream);
                 stream.closeEntry();
             }
             catch (IOException ex)
             {
                 IOUtils.closeQuietly(in);
                 IOUtils.closeQuietly(stream);
             }
         }
         File bundledDir = new File(bundledPluginJars[0].getParentFile(), "bundled-plugins");
         final BundledPluginLoader bundledLoader = new BundledPluginLoader(zip.toURL(), bundledDir, Arrays.<PluginFactory>asList(osgiPluginDeployer),
             new DefaultPluginEventManager());
 
         pluginManager = new DefaultPluginManager(new MemoryPluginPersistentStateStore(), Arrays.<PluginLoader> asList(bundledLoader, loader), moduleDescriptorFactory,
             pluginEventManager);
         pluginManager.setPluginInstaller(new FilePluginInstaller(pluginsDir));
         pluginManager.init();
     }
 
     private HostComponentProvider getWrappingHostComponentProvider(final HostComponentProvider hostComponentProvider)
     {
         HostComponentProvider requiredWrappingProvider = new HostComponentProvider()
         {
             public void provide(final ComponentRegistrar registrar)
             {
 
                 if (hostComponentProvider != null)
                 {
                     hostComponentProvider.provide(new ComponentRegistrar()
                     {
                         public InstanceBuilder register(Class<?>... mainInterfaces)
                         {
                             if (!Arrays.asList(mainInterfaces).contains(PluginEventManager.class))
                             {
                                 return registrar.register(mainInterfaces);
                             }
                             return null;
                         }
                     });
                 }
                 registrar.register(PluginEventManager.class).forInstance(pluginEventManager);
                 registrar.register(PluginAccessor.class).forInstance(pluginManager);
             }
         };
         return requiredWrappingProvider;
     }
 
     private PackageScannerConfiguration buildScannerConfiguration(String version)
     {
         final PackageScannerConfiguration scannerConfig = new DefaultPackageScannerConfiguration(version);
         scannerConfig.getPackageIncludes().add("com.atlassian.plugin*");
         scannerConfig.getPackageIncludes().add("javax.servlet*");
         scannerConfig.getPackageIncludes().add("com_cenqua_clover");
         scannerConfig.getPackageExcludes().add("com.atlassian.plugin.osgi.bridge*");
        scannerConfig.getPackageExcludes().add("com.atlassian.plugin.osgi.bridge.external");
         scannerConfig.getPackageExcludes().add("com.atlassian.plugin.osgi.spring.external");
         scannerConfig.getPackageVersions().put("org.apache.commons.logging", "1.1.1");
         return scannerConfig;
     }
 }
