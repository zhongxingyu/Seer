 package com.atlassian.plugin.main;
 
 import com.atlassian.plugin.osgi.container.PackageScannerConfiguration;
 import com.atlassian.plugin.osgi.container.impl.DefaultPackageScannerConfiguration;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
 import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
 import com.atlassian.plugin.*;
 import com.atlassian.plugin.store.MemoryPluginStateStore;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 /**
  * The builder for {@link PluginsConfiguration} instances that additionally performs validation and default creation.
  * For a usage example, see the package javadocs.
  */
 public class PluginsConfigurationBuilder
 {
     private PackageScannerConfiguration packageScannerConfiguration;
     private HostComponentProvider hostComponentProvider;
     private File frameworkBundlesDirectory;
     private File bundleCacheDirectory;
     private File pluginDirectory;
     private URL bundledPluginUrl;
     private File bundledPluginCacheDirectory;
     private String pluginDescriptorFilename;
     private ModuleDescriptorFactory moduleDescriptorFactory;
     private PluginStateStore pluginStateStore;
     private long hotDeployPollingPeriod;
 
     /**
      * Sets the package scanner configuration instance that contains information about what packages to expose to plugins
      * @param packageScannerConfiguration The configuration instance
      * @return this
      */
     public PluginsConfigurationBuilder setPackageScannerConfiguration(PackageScannerConfiguration packageScannerConfiguration)
     {
         this.packageScannerConfiguration = packageScannerConfiguration;
         return this;
     }
 
     /**
      * Sets a list of package expressions to expose to plugins.  Used as a shortcut for
      * {@link #setPackageScannerConfiguration(PackageScannerConfiguration)}
      *
      * @param pkgs A list of package expressions, where the '*' character matches any character including subpackages
      * @return this
      */
     public PluginsConfigurationBuilder setPackagesToInclude(String... pkgs)
     {
         if (packageScannerConfiguration == null)
             this.packageScannerConfiguration = new DefaultPackageScannerConfiguration();
 
         packageScannerConfiguration.getPackageIncludes().addAll(Arrays.asList(pkgs));
         return this;
     }
 
     /**
      * Sets which packages should be exposed as which versions.  Used as a shortcut for
      * {@link #setPackageScannerConfiguration(PackageScannerConfiguration)}
      *
      * @param packageToVersion A map of package names to version names.  No wildcards allowed, and the version names
      * must match the expected OSGi versioning scheme.
      * @return this
      */
     public PluginsConfigurationBuilder setPackagesVersions(Map<String,String> packageToVersion)
     {
         if (packageScannerConfiguration == null)
             this.packageScannerConfiguration = new DefaultPackageScannerConfiguration();
 
         packageScannerConfiguration.getPackageVersions().putAll(packageToVersion);
         return this;
     }
 
     /**
      * Sets the host component provider instance, used for registering application services as OSGi services so that
      * they can be automatically available to plugins
      *
      * @param hostComponentProvider The host component provider implementation
      * @return this
      */
     public PluginsConfigurationBuilder setHostComponentProvider(HostComponentProvider hostComponentProvider)
     {
         this.hostComponentProvider = hostComponentProvider;
         return this;
     }
 
     /**
      * Sets caching directory to extract framework bundles into.  Doesn't have to be preserved and will be automatically
      * cleaned out if it detects any modification.
      *
      * @param frameworkBundlesDirectory A directory that exists
      * @return this
      */
     public PluginsConfigurationBuilder setFrameworkBundlesDirectory(File frameworkBundlesDirectory)
     {
         this.frameworkBundlesDirectory = frameworkBundlesDirectory;
         return this;
     }
 
     /**
      * Sets the directory to use for the OSGi framework's bundle cache.  Doesn't have to be preserved across restarts
      * but shouldn't be externally modified at runtime.
      *
      * @param bundleCacheDirectory A directory that exists and is empty
      * @return this
      */
     public PluginsConfigurationBuilder setBundleCacheDirectory(File bundleCacheDirectory)
     {
         this.bundleCacheDirectory = bundleCacheDirectory;
         return this;
     }
 
     /**
      * Sets the directory that contains the plugins and will be used to store installed plugins.
      *
      * @param pluginDirectory A directory that exists
      * @return this
      */
     public PluginsConfigurationBuilder setPluginDirectory(File pluginDirectory)
     {
         this.pluginDirectory = pluginDirectory;
         return this;
     }
 
     /**
      * Sets the URL to a ZIP file containing plugins that are to be started before any user plugins but after
      * framework bundles.  Must be set if {@link #setBundledPluginCacheDirectory(java.io.File)} is set.
      *
      * @param bundledPluginUrl A URL to a ZIP of plugin JAR files
      * @return this
      */
     public PluginsConfigurationBuilder setBundledPluginUrl(URL bundledPluginUrl)
     {
         this.bundledPluginUrl = bundledPluginUrl;
         return this;
     }
 
     /**
      * Sets the directory to unzip bundled plugins into.  The directory will automatically be cleaned out if the
      * framework detects any modification.  Must be set if {@link #setBundledPluginUrl(java.net.URL)} is set.
      *
      * @param bundledPluginCacheDirectory A directory that exists
      * @return this
      */
     public PluginsConfigurationBuilder setBundledPluginCacheDirectory(File bundledPluginCacheDirectory)
     {
         this.bundledPluginCacheDirectory = bundledPluginCacheDirectory;
         return this;
     }
 
     /**
      * Sets the plugin descriptor file name to expect in a plugin JAR artifact
      *
      * @param pluginDescriptorFilename A valid file name
      * @return this
      */
     public PluginsConfigurationBuilder setPluginDescriptorFilename(String pluginDescriptorFilename)
     {
         this.pluginDescriptorFilename = pluginDescriptorFilename;
         return this;
     }
 
     /**
      * Sets the module descriptor factory that will be used to create instances of discovered module descriptors.
      * Usually, the {@link DefaultModuleDescriptorFactory} is what is used, which takes class instances of module
      * descriptors to instantiate.
      *
      * @param moduleDescriptorFactory A module descriptor factory instance
      * @return this
      */
     public PluginsConfigurationBuilder setModuleDescriptorFactory(ModuleDescriptorFactory moduleDescriptorFactory)
     {
         this.moduleDescriptorFactory = moduleDescriptorFactory;
         return this;
     }
 
     /**
      * Sets the plugin state store implementation used for persisting which plugins and modules are enabled or disabled
      * across restarts.
      *
      * @param pluginStateStore The plugin state store implementation
      * @return this
      */
     public PluginsConfigurationBuilder setPluginStateStore(PluginStateStore pluginStateStore)
     {
         this.pluginStateStore = pluginStateStore;
         return this;
     }
 
     /**
      * Sets the polling frequency for scanning for new plugins
      *
      * @param hotDeployPollingFrequency The quantity of time periods
      * @param timeUnit The units for the frequency
      */
    public void setHotDeployPollingFrequency(long hotDeployPollingFrequency, TimeUnit timeUnit)
     {
         this.hotDeployPollingPeriod = hotDeployPollingFrequency * timeUnit.toMillis(hotDeployPollingFrequency);
     }
 
     /**
      * Builds a {@link com.atlassian.plugin.main.PluginsConfiguration} instance by processing the configuration that
      * was previously set, validating the input, and setting any defaults where not explicitly specified.
      *
      * @return A valid {@link PluginsConfiguration} instance to pass to {@link AtlassianPlugins}
      */
     public PluginsConfiguration build()
     {
         if (this.pluginDirectory == null) throw new IllegalArgumentException("Plugin directory must be defined");
         if (!this.pluginDirectory.exists()) throw new IllegalArgumentException("Plugin directory must exist");
 
         if (packageScannerConfiguration == null)
             packageScannerConfiguration = new DefaultPackageScannerConfiguration();
 
         if (pluginDescriptorFilename == null)
             pluginDescriptorFilename = PluginManager.PLUGIN_DESCRIPTOR_FILENAME;
 
         if (hostComponentProvider == null)
         {
             hostComponentProvider = new HostComponentProvider()
             {
                 public void provide(ComponentRegistrar registrar) {}
             };
         }
 
         if (pluginStateStore == null)
             pluginStateStore = new MemoryPluginStateStore();
 
         if (moduleDescriptorFactory == null)
             moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
 
         if (bundleCacheDirectory == null)
         {
             try
             {
                 bundleCacheDirectory = File.createTempFile("atlassian-plugins-bundle-cache", ".tmp");
                 bundleCacheDirectory.delete();
             }
             catch (IOException e)
             {
                 throw new IllegalStateException("Should be able to create tmp files", e);
             }
             bundleCacheDirectory.mkdir();
         }
         if (!bundleCacheDirectory.exists())
             throw new IllegalArgumentException("Bundle cache directory should exist");
 
         if (frameworkBundlesDirectory == null)
         {
             try
             {
                 frameworkBundlesDirectory = File.createTempFile("atlassian-plugins-framework-bundles", ".tmp");
                 frameworkBundlesDirectory.delete();
             }
             catch (IOException e)
             {
                 throw new IllegalStateException("Should be able to create tmp files", e);
             }
             frameworkBundlesDirectory.mkdir();
         }
         if (!frameworkBundlesDirectory.exists())
             throw new IllegalArgumentException("Framework bundles directory should exist");
 
         if (bundledPluginCacheDirectory == null ^ bundledPluginUrl == null)
             throw new IllegalArgumentException("Both bundled plugin cache directory and bundle plugin URL must be defined or not at all.");
 
         return new InternalPluginsConfiguration();
     }
 
     private class InternalPluginsConfiguration implements PluginsConfiguration
     {
 
         public PackageScannerConfiguration getPackageScannerConfiguration()
         {
             return packageScannerConfiguration;
         }
 
         public HostComponentProvider getHostComponentProvider()
         {
             return hostComponentProvider;
         }
 
         public File getFrameworkBundlesDirectory()
         {
             return frameworkBundlesDirectory;
         }
 
         public File getBundleCacheDirectory()
         {
             return bundleCacheDirectory;
         }
 
         public String getPluginDescriptorFilename()
         {
             return pluginDescriptorFilename;
         }
 
         public File getPluginDirectory()
         {
             return pluginDirectory;
         }
 
         public URL getBundledPluginUrl()
         {
             return bundledPluginUrl;
         }
 
         public File getBundledPluginCacheDirectory()
         {
             return bundledPluginCacheDirectory;
         }
 
         public ModuleDescriptorFactory getModuleDescriptorFactory()
         {
             return moduleDescriptorFactory;
         }
 
         public PluginStateStore getPluginStateStore()
         {
             return pluginStateStore;
         }
 
         public long getHotDeployPollingPeriod()
         {
             return hotDeployPollingPeriod;
         }
     }
 
 }
