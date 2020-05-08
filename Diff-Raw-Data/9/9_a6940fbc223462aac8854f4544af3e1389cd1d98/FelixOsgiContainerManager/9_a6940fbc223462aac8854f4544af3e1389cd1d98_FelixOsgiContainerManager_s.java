 package com.atlassian.plugin.osgi.container.felix;
 
 import com.atlassian.plugin.osgi.container.OsgiContainerException;
 import com.atlassian.plugin.osgi.container.OsgiContainerManager;
 import com.atlassian.plugin.osgi.container.PackageScannerConfiguration;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentRegistration;
 import com.atlassian.plugin.osgi.hostcomponents.impl.DefaultComponentRegistrar;
 import com.atlassian.plugin.util.ClassLoaderUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.commons.io.FileUtils;
 import org.apache.felix.framework.Felix;
 import org.apache.felix.framework.cache.BundleCache;
 import org.apache.felix.framework.util.FelixConstants;
 import org.apache.felix.framework.util.StringMap;
 import org.osgi.framework.*;
 import org.twdata.pkgscanner.ExportPackage;
 import org.twdata.pkgscanner.PackageScanner;
 import static org.twdata.pkgscanner.PackageScanner.jars;
 import static org.twdata.pkgscanner.PackageScanner.include;
 import static org.twdata.pkgscanner.PackageScanner.exclude;
 import static org.twdata.pkgscanner.PackageScanner.packages;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.FilenameFilter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.*;
 
 /**
  * Felix implementation of the OSGi container manager
  */
 public class FelixOsgiContainerManager implements OsgiContainerManager
 {
     private static final Log log = LogFactory.getLog(FelixOsgiContainerManager.class);
     private BundleRegistration registration = null;
     private Felix felix = null;
     private boolean felixRunning = false;
     private File cacheDirectory;
 
     private final URL frameworkBundlesUrl;
     private PackageScannerConfiguration packageScannerConfig;
    public static final String OSGI_FRAMEWORK_BUNDLES_ZIP = "/osgi-framework-bundles.zip";
     private File frameworkBundlesDir;
 
 
     public FelixOsgiContainerManager(File frameworkBundlesDir, PackageScannerConfiguration packageScannerConfig)
     {
         this(ClassLoaderUtils.getResource(OSGI_FRAMEWORK_BUNDLES_ZIP, FelixOsgiContainerManager.class), frameworkBundlesDir, packageScannerConfig);
     }
     public FelixOsgiContainerManager(URL frameworkBundlesZip, File frameworkBundlesDir, PackageScannerConfiguration packageScannerConfig)
     {
         if (frameworkBundlesZip == null)
             throw new IllegalArgumentException("The framework bundles zip is required");
 
         this.frameworkBundlesUrl = frameworkBundlesZip;
         this.packageScannerConfig = packageScannerConfig;
         this.frameworkBundlesDir = frameworkBundlesDir;
     }
 
 
 
     public void start(HostComponentProvider provider) throws OsgiContainerException
     {
         Collection<ExportPackage> exports = generateExports();
         initialiseCacheDirectory();
 
         // Create a case-insensitive configuration property map.
         final Map configMap = new StringMap(false);
         // Configure the Felix instance to be embedded.
         configMap.put(FelixConstants.EMBEDDED_EXECUTION_PROP, "true");
         // Add the bundle provided service interface package and the core OSGi
         // packages to be exported from the class path via the system bundle.
         configMap.put(Constants.FRAMEWORK_SYSTEMPACKAGES,
             "org.osgi.framework; version=1.3.0," +
             "org.osgi.service.packageadmin; version=1.2.0," +
             "org.osgi.service.startlevel; version=1.0.0," +
             "org.osgi.service.url; version=1.0.0," +
             "org.osgi.util; version=1.3.0," +
             "org.osgi.util.tracker; version=1.3.0," +
             "host.service.command; version=1.0.0," +
 
             constructAutoExports(exports));
 
         configMap.put(FelixConstants.LOG_LEVEL_PROP,
                 (log.isDebugEnabled() ? "4" :
                  log.isInfoEnabled() ? "3" :
                  log.isWarnEnabled() ? "2" :
                  log.isErrorEnabled() ? "1" : "1"));
         // Explicitly specify the directory to use for caching bundles.
         configMap.put(BundleCache.CACHE_PROFILE_DIR_PROP, cacheDirectory.getAbsolutePath());
 
         try
         {
             // Create host activator;
             registration = new BundleRegistration(frameworkBundlesUrl, frameworkBundlesDir, provider);
             final List<BundleActivator> list = new ArrayList<BundleActivator>();
             list.add(registration);
 
             // Now create an instance of the framework with
             // our configuration properties and activator.
             felix = new Felix(configMap, list);
 
             // Now start Felix instance.  Starting in a different thread to explicity set daemon status
             Thread t = new Thread() {
                 @Override
                 public void run() {
                     try
                     {
 
                         felix.start();
                         felixRunning = true;
                     } catch (BundleException e)
                     {
                         throw new OsgiContainerException("Unable to start felix", e);
                     }
                 }
             };
             t.setDaemon(true);
             t.start();
 
             // Give it 10 seconds
             t.join(10 * 60 * 1000);
 
 
 
         }
         catch (Exception ex)
         {
             throw new OsgiContainerException("Unable to start OSGi container", ex);
         }
     }
 
     public void stop() throws OsgiContainerException
     {
         try
         {
             felix.stop();
             felixRunning = false;
             felix = null;
         } catch (BundleException e)
         {
             throw new OsgiContainerException("Unable to stop OSGi container", e);
         }
     }
 
     public Bundle[] getBundles()
     {
         return registration.getBundles();
     }
 
     public ServiceReference[] getRegisteredServices()
     {
         return felix.getRegisteredServices();
     }
 
     public Bundle installBundle(File file) throws OsgiContainerException
     {
         try
         {
             return registration.install(file);
         } catch (BundleException e)
         {
             throw new OsgiContainerException("Unable to install bundle", e);
         }
     }
 
     public void reloadHostComponents(HostComponentProvider provider)
     {
         registration.reloadHostComponents(provider);
     }
 
     String constructAutoExports(Collection<ExportPackage> packageExports) {
 
         StringBuilder sb = new StringBuilder();
         for (Iterator<ExportPackage> i = packageExports.iterator(); i.hasNext(); ) {
             ExportPackage pkg = i.next();
             sb.append(pkg.getPackageName());
             if (pkg.getVersion() != null) {
                 try {
                     Version.parseVersion(pkg.getVersion());
                     sb.append(";version=").append(pkg.getVersion());
                 } catch (IllegalArgumentException ex) {
                     log.info("Unable to parse version: "+pkg.getVersion());
                 }
             }
             if (i.hasNext()) {
                 sb.append(",");
             }
         }
         if (log.isDebugEnabled()) {
             log.debug("Exports:\n"+sb.toString().replaceAll(",", "\r\n"));
         }
 
         return sb.toString();
     }
 
     Collection<ExportPackage> generateExports()
     {
         String[] arrType = new String[0];
         Collection<ExportPackage> exports = new PackageScanner()
            .select(
                jars(
                        include(packageScannerConfig.getJarIncludes().toArray(arrType)),
                        exclude(packageScannerConfig.getJarExcludes().toArray(arrType))),
                packages(
                        include(packageScannerConfig.getPackageIncludes().toArray(arrType)),
                        exclude(packageScannerConfig.getPackageExcludes().toArray(arrType)))
            )
            .withMappings(packageScannerConfig.getPackageVersions())
            .scan();
         return exports;
     }
 
     void initialiseCacheDirectory() throws OsgiContainerException
     {
         try
         {
             cacheDirectory = new File(File.createTempFile("foo", "bar").getParentFile(), "felix");
             if (cacheDirectory.exists())
                 FileUtils.deleteDirectory(cacheDirectory);
 
             cacheDirectory.mkdir();
             cacheDirectory.deleteOnExit();
         } catch (IOException e)
         {
             throw new OsgiContainerException("Cannot create cache directory", e);
         }
     }
 
     public boolean isRunning()
     {
         return felixRunning;
     }
 
     public List<HostComponentRegistration> getHostComponentRegistrations()
     {
         return registration.hostComponentRegistrations;
     }
 
     /**
      * Manages framwork-level framework bundles and host components registration, and individual plugin bundle
      * installation and removal.
      */
     static class BundleRegistration implements BundleActivator, BundleListener
     {
         private BundleContext bundleContext;
         private HostComponentProvider hostProvider;
         private List<ServiceRegistration> hostServicesReferences;
         private List<HostComponentRegistration> hostComponentRegistrations;
         private URL frameworkBundlesUrl;
         private File frameworkBundlesDir;
 
         public BundleRegistration(URL frameworkBundlesUrl, File frameworkBundlesDir, HostComponentProvider provider)
         {
             this.hostProvider = provider;
             this.frameworkBundlesUrl = frameworkBundlesUrl;
             this.frameworkBundlesDir = frameworkBundlesDir;
         }
 
         public void start(BundleContext context) throws Exception {
             this.bundleContext = context;
             context.addBundleListener(this);
 
             reloadHostComponents(hostProvider);
             extractAndInstallFrameworkBundles();
         }
 
         public void reloadHostComponents(HostComponentProvider provider)
         {
             // Unregister any existing host components
             if (hostServicesReferences != null) {
                 for (ServiceRegistration reg : hostServicesReferences)
                     reg.unregister();
             }
             
             if (provider != null)
             {
                 // Retrieve and register host components as OSGi services
                 DefaultComponentRegistrar registrar = new DefaultComponentRegistrar();
                 provider.provide(registrar);
                 hostServicesReferences = registrar.writeRegistry(bundleContext);
                 hostComponentRegistrations = registrar.getRegistry();
             }
 
         }
 
         public void stop(BundleContext ctx) throws Exception {
         }
 
         public void bundleChanged(BundleEvent evt) {
             switch (evt.getType()) {
                 case BundleEvent.INSTALLED:
                     log.warn("Installed bundle " + evt.getBundle().getSymbolicName() + " ("+evt.getBundle().getBundleId()+")");
                     break;
                 case BundleEvent.STARTED:
                     log.warn("Started bundle " + evt.getBundle().getSymbolicName() + " ("+evt.getBundle().getBundleId()+")");
                     break;
                 case BundleEvent.STOPPED:
                     log.warn("Stopped bundle " + evt.getBundle().getSymbolicName() + " ("+evt.getBundle().getBundleId()+")");
                     break;
                 case BundleEvent.UNINSTALLED:
                     log.warn("Uninstalled bundle " + evt.getBundle().getSymbolicName() + " ("+evt.getBundle().getBundleId()+")");
                     break;
             }
         }
 
         public Bundle install(File path) throws BundleException
         {
             Bundle bundle;
             try
             {
 
                 bundle = bundleContext.installBundle(path.toURL().toString());
             } catch (MalformedURLException e)
             {
                 throw new BundleException("Invalid path: "+path);
             }
             bundle.start();
             return bundle;
         }
 
         public Bundle[] getBundles()
         {
             return bundleContext.getBundles();
         }
 
         public List<HostComponentRegistration> getHostComponentRegistrations()
         {
             return hostComponentRegistrations;
         }
 
         private void extractAndInstallFrameworkBundles() throws IOException, BundleException
         {
             com.atlassian.plugin.util.FileUtils.conditionallyExtractZipFile(frameworkBundlesUrl, frameworkBundlesDir);
             for (File bundleFile : frameworkBundlesDir.listFiles(new FilenameFilter() {
                     public boolean accept(File file, String s) { return file.getName().endsWith(".jar"); }
                 }))
             {
                 install(bundleFile);
             }
         }
     }
 
 }
