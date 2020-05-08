 package fnug.resource;
 
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import fnug.config.BundleConfig;
 import fnug.config.Config;
 import fnug.config.ConfigParser;
 import fnug.config.JsonConfigParser;
 
 /*
  Copyright 2010 Martin Algesten
 
  Licensed under the Apache License, Version 2.0 (the "License");
  you may not use this file except in compliance with the License.
  You may obtain a copy of the License at
 
  http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
  */
 
 /**
  * Entry point for resource resolving. Comprised of a number of {@link Bundle}
  * that are created using {@link Config}.
  * 
  * @author Martin Algesten
  * 
  */
 public class ResourceResolver {
 
     private static final Logger LOG = LoggerFactory.getLogger(ResourceResolver.class);
 
     private static final String SEPARATOR = "/";
     private final static HashSet<String> BUNDLE_RESERVED_WORDS = new HashSet<String>(Arrays.asList(new String[] {
             "all",
             "1",
             "true"
     }));
 
     private List<Resource> configResources;
     private volatile List<Config> configs = new LinkedList<Config>();
     private ConfigParser configParser = new JsonConfigParser();
     private LinkedHashMap<String, Bundle> bundles = new LinkedHashMap<String, Bundle>();
 
     private static ThreadLocal<ResourceResolver> instance = new ThreadLocal<ResourceResolver>();
 
     /**
      * Must be called before using the {@link ResourceResolver} to bind this
      * instance of the resolver to the currently executing thread.
      */
     public void setThreadLocal() {
         setThreadLocal(this);
     }
 
     /**
      * Sets the thread local instance to use. Used for test cases.
      * 
      * @param resolver
      *            Resolver to use.
      */
     protected static void setThreadLocal(ResourceResolver resolver) {
         instance.set(resolver);
     }
 
     /**
      * Returns the thread associated instance.
      * 
      * @return the thread associated instance.
      */
     public static ResourceResolver getInstance() {
         ResourceResolver r = instance.get();
         if (r == null) {
             throw new IllegalStateException("No ResourceResolve set in thread locale. Someone forgot to set it.");
         }
         return r;
     }
 
     /**
      * Constructs a resolver from the given list of resources pointing out
      * config files. These files will be parsed into {@link Config} by a
      * {@link ConfigParser}.
      * 
      * @param configResources
      *            resources to configure from.
      */
     public ResourceResolver(List<Resource> configResources) {
         if (configResources == null || configResources.isEmpty()) {
             throw new IllegalArgumentException("Need at least one config resource");
         }
         this.configResources = configResources;
         initConfigs();
     }
 
     /**
      * Empty constructor for test cases.
      */
     protected ResourceResolver() {
     }
 
     /**
      * Sets the configs to use. For test cases.
      * 
      * @param configs
      *            configs to use.
      */
     protected void setConfigs(Config... configs) {
         this.configs = Arrays.asList(configs);
         initBundles();
     }
 
     /**
      * Resolves the give path. This first attempts to resolve using just the
      * bundle names themselves. I.e. a path such as
      * <code>/mybundle/myresource.js</code> would be matched against a bundle
      * named <code>mybundle</code>.
      * 
      * @param path
      *            Path to match.
      * @return The resolved resource, or null if no bundle will resolve.
      */
     public Resource resolve(String path) {
         if (configs.isEmpty()) {
             initConfigs();
         }
         if (path == null || path.isEmpty()) {
             throw new IllegalArgumentException("Can't resolve empty path");
         }
         if (path.startsWith(SEPARATOR)) {
             throw new IllegalArgumentException("Path must not start with '" + SEPARATOR + "'");
         }
         if (path.endsWith(SEPARATOR)) {
             throw new IllegalArgumentException("Path must not end with '" + SEPARATOR + "'");
         }
         for (Bundle bundle : bundles.values()) {
             if (path.startsWith(bundle.getName() + "/")) {
                 return bundle.resolve(path);
             }
         }
         return null;
     }
 
     /**
      * Returns the bundle for the given bundle name.
      * 
      * @param name
      *            name of the bundle
      * @return the bundle or null if not found.
      */
     public Bundle getBundle(String name) {
         if (configs.isEmpty()) {
             initConfigs();
         }
         return bundles.get(name);
     }
 
     private void initConfigs() {
         synchronized (this) {
             if (configs.isEmpty()) {
                
                 bundles.clear();

                 for (Resource configResource : configResources) {
 
                     if (configResource.getLastModified() == -1) {
                         LOG.warn("Config file missing: " + configResource.getFullPath());
                         continue;
                     }
 
                     LOG.info("Reading config: " + configResource.getFullPath());
 
                     Config parsedConfig = configParser.parse(configResource);
                     configs.add(parsedConfig);
 
                 }

                initBundles();

             }
         }
     }
 
     private void initBundles() {
         for (Config cfg : configs) {
             for (BundleConfig bcfg : cfg.getBundleConfigs()) {
                 if (BUNDLE_RESERVED_WORDS.contains(bcfg.name().toLowerCase())) {
                     throw new IllegalStateException("Bundle name '" + bcfg.name() + "' is a reserved word.");
                 }
                 if (bundles.containsKey(bcfg.name())) {
                     throw new IllegalStateException("Duplicate definitions of bundle name '" + bcfg.name() + "' " +
                             "in '" + bundles.get(bcfg.name()).getConfig().configResource().getFullPath() + "' and '" +
                             bcfg.configResource().getFullPath() + "'");
                 }
                 Bundle bundle = new DefaultBundle(bcfg);
                 bundles.put(bundle.getName(), bundle);
             }
         }
     }
 
     /**
      * Returns a list of all configured bundles.
      * 
      * @return the bundle that are configured.
      */
     public List<Bundle> getBundles() {
         return new LinkedList<Bundle>(bundles.values());
     }
 
     /**
      * Returns the last modified date of all the configured bundles, see
      * {@link Bundle#getLastModified()}.
      * 
      * @return the last modified date.
      */
     public long getLastModified() {
         long mostRecent = -1;
         for (Bundle b : getBundles()) {
             mostRecent = Math.max(mostRecent, b.getLastModified());
         }
         return mostRecent;
     }
 
     /**
      * Checks if any of the config resources passed in the constructor is
      * changed, in which case the resolver reinitialises all bundles.
      * 
      * @return true if any config was changed
      */
     public boolean checkModified() {
         boolean changed = false;
         for (Resource r : configResources) {
             changed = r.checkModified() || changed;
         }
         if (changed) {
             synchronized (this) {
                 // this also triggers a drop of all bundles.
                 configs.clear();
             }
         }
         return changed;
     }
 
 }
