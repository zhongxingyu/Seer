 /*
  * Copyright (C) 2010-2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.zep.impl;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.BeanFactoryUtils;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.ApplicationEventPublisher;
 import org.springframework.context.ApplicationEventPublisherAware;
 import org.springframework.context.ApplicationListener;
 import org.springframework.context.annotation.AnnotationConfigApplicationContext;
 import org.springframework.context.event.ContextRefreshedEvent;
 import org.zenoss.utils.ZenPack;
 import org.zenoss.utils.ZenPacks;
 import org.zenoss.utils.ZenossException;
 import org.zenoss.zep.PluginService;
 import org.zenoss.zep.events.PluginServiceStartedEvent;
 import org.zenoss.zep.plugins.EventPlugin;
 import org.zenoss.zep.plugins.EventPostCreatePlugin;
 import org.zenoss.zep.plugins.EventPostIndexPlugin;
 import org.zenoss.zep.plugins.EventPreCreatePlugin;
 import org.zenoss.zep.plugins.exceptions.DependencyCycleException;
 import org.zenoss.zep.plugins.exceptions.MissingDependencyException;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 /**
  * {@link PluginService} implementation which supports loading plug-ins from a
  * Spring {@link ApplicationContext}.
  */
 public class PluginServiceImpl implements PluginService, ApplicationListener<ContextRefreshedEvent>,
         ApplicationEventPublisherAware {
 
     private static final Logger logger = LoggerFactory.getLogger(PluginServiceImpl.class);
 
     private final PluginRepository pluginRepository;
 
     private URLClassLoader pluginClassLoader = null;
 
     private ApplicationEventPublisher applicationEventPublisher;
 
     public PluginServiceImpl(Properties pluginProperties) throws ZenossException {
         this(pluginProperties, true);
     }
 
     public PluginServiceImpl(Properties pluginProperties, boolean loadExternalPlugins) throws ZenossException {
         this.pluginRepository = new PluginRepository(pluginProperties);
         if (loadExternalPlugins) {
             this.pluginClassLoader = createPluginClassLoader();
         }
     }
 
     @Override
     public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
         this.applicationEventPublisher = applicationEventPublisher;
     }
 
     private URLClassLoader createPluginClassLoader() throws ZenossException {
         final List<URL> urls = new ArrayList<URL>();
        List<ZenPack> zenPacks;
        try {
            zenPacks = ZenPacks.getAllZenPacks();
        } catch (ZenossException e) {
            logger.warn("Unable to find ZenPacks", e);
            return null;
        }

         for (ZenPack zenPack : zenPacks) {
             final File pluginDir = new File(zenPack.packPath("zep", "plugins"));
             if (!pluginDir.isDirectory()) {
                 continue;
             }
             final File[] pluginJars = pluginDir.listFiles(new FileFilter() {
                 @Override
                 public boolean accept(File pathname) {
                     return pathname.isFile() && pathname.getName().endsWith(".jar");
                 }
             });
             if (pluginJars != null) {
                 for (File pluginJar : pluginJars) {
                     try {
                         urls.add(pluginJar.toURI().toURL());
                         logger.info("Loading plugin: {}", pluginJar.getAbsolutePath());
                     } catch (MalformedURLException e) {
                         logger.warn("Failed to get URL from file: {}", pluginJar.getAbsolutePath());
                     }
                 }
             }
         }
 
         URLClassLoader classLoader = null;
         if (!urls.isEmpty()) {
             logger.info("Discovered plug-ins: {}", urls);
             classLoader = new URLClassLoader(urls.toArray(new URL[urls.size()]), getClass().getClassLoader());
         }
         else {
             logger.info("No external plug-ins found.");
         }
         return classLoader;
     }
 
 
     /**
      * Recursive method used to find plug-in cycles.
      *
      * @param plugins
      *            The plug-ins to analyze.
      * @param existingDeps
      *            Any existing dependencies which will be scanned to avoid
      *            {@link MissingDependencyException} from being thrown.
      * @param plugin
      *            The plug-in which is currently being analyzed.
      * @param analyzed
      *            The previously analyzed plug-ins which are not analyzed again.
      * @param dependencies
      *            The current dependency chain which is being analyzed.
      * @throws MissingDependencyException
      *             If a missing dependency is discovered.
      * @throws DependencyCycleException
      *             If a cycle in dependencies is detected.
      */
     private static void detectPluginCycles(
             Map<String, ? extends EventPlugin> plugins,
             Set<String> existingDeps, EventPlugin plugin, Set<String> analyzed,
             Set<String> dependencies) throws MissingDependencyException,
             DependencyCycleException {
         // Don't detect cycles again on the same plug-in.
         if (!analyzed.add(plugin.getId())) {
             return;
         }
         dependencies.add(plugin.getId());
         Set<String> pluginDependencies = plugin.getDependencies();
         if (pluginDependencies != null) {
             for (String dependencyId : pluginDependencies) {
                 EventPlugin dependentPlugin = plugins.get(dependencyId);
                 if (dependentPlugin == null) {
                     if (!existingDeps.contains(dependencyId)) {
                         throw new MissingDependencyException(plugin.getId(), dependencyId);
                     }
                 } else {
                     if (dependencies.contains(dependencyId)) {
                         throw new DependencyCycleException(dependencyId);
                     }
                     detectPluginCycles(plugins, existingDeps, dependentPlugin, analyzed, dependencies);
                 }
             }
         }
         dependencies.remove(plugin.getId());
     }
 
     /**
      * Detects any cycles in the specified plug-ins and their dependencies.
      *
      * @param plugins
      *            The plug-ins to analyze.
      * @param existingDeps
      *            Any existing dependency ids which don't trigger a
      *            {@link MissingDependencyException}.
      * @throws MissingDependencyException
      *             If a dependency is not found.
      * @throws DependencyCycleException
      *             If a cycle in the dependencies is detected.
      */
     static void detectCycles(Map<String, ? extends EventPlugin> plugins,
             Set<String> existingDeps) throws MissingDependencyException,
             DependencyCycleException {
         Set<String> analyzed = new HashSet<String>();
         for (EventPlugin plugin : plugins.values()) {
             detectPluginCycles(plugins, existingDeps, plugin, analyzed, new HashSet<String>());
         }
     }
 
     /**
      * Sorts plug-ins in the order of their dependencies, so all dependencies
      * come before the plug-in which depends on them.
      *
      * @param <T>
      *            The type of {@link EventPlugin}.
      * @param plugins
      *            Map of plug-ins keyed by the plug-in ID.
      * @return A sorted list of plug-ins in order based on their dependencies.
      */
     static <T extends EventPlugin> List<T> sortPluginsByDependencies(Map<String, T> plugins) {
         List<T> sorted = new ArrayList<T>(plugins.size());
         Map<String, T> mutablePlugins = new HashMap<String, T>(plugins);
         while (!mutablePlugins.isEmpty()) {
             for (Iterator<T> it = mutablePlugins.values().iterator(); it.hasNext();) {
                 T plugin = it.next();
                 boolean allDependenciesResolved = true;
                 final Set<String> pluginDependencies = plugin.getDependencies();
                 if (pluginDependencies != null) {
                     for (String dep : pluginDependencies) {
                         T depPlugin = mutablePlugins.get(dep);
                         if (depPlugin != null) {
                             allDependenciesResolved = false;
                             break;
                         }
                     }
                 }
                 if (allDependenciesResolved) {
                     sorted.add(plugin);
                     it.remove();
                 }
             }
         }
         return sorted;
     }
 
     private static class PluginConfig {
         private final Map<String, Map<String, String>> allPluginProperties = new HashMap<String, Map<String, String>>();
 
         public Map<String, String> getPluginProperties(String pluginId) {
             Map<String, String> pluginProps = allPluginProperties.get(pluginId);
             if (pluginProps == null) {
                 pluginProps = Collections.emptyMap();
             }
             return pluginProps;
         }
 
         public void setPluginProperty(String pluginId, String name, String value) {
             Map<String, String> pluginProps = allPluginProperties.get(pluginId);
             if (pluginProps == null) {
                 pluginProps = new HashMap<String, String>();
                 allPluginProperties.put(pluginId, pluginProps);
             }
             pluginProps.put(name, value);
         }
     }
 
     private static class PluginRepository {
         private Map<Class<? extends EventPlugin>, List<? extends EventPlugin>> plugins =
                 new HashMap<Class<? extends EventPlugin>, List<? extends EventPlugin>>();
         private Set<String> allPluginIds = new HashSet<String>();
         private final PluginConfig pluginConfig;
         private final Set<String> disabledPlugins;
 
         public PluginRepository(Properties properties) {
             this.pluginConfig = loadPluginConfig(properties);
             this.disabledPlugins = getDisabledPlugins(properties);
         }
 
         private static PluginConfig loadPluginConfig(Properties properties) {
             final PluginConfig cfg = new PluginConfig();
             final Pattern pattern = Pattern.compile("plugin\\.([^\\.]+)\\.(.+)");
             for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                 final String key = (String) entry.getKey();
                 final String val = (String) entry.getValue();
                 final Matcher matcher = pattern.matcher(key);
                 if (matcher.matches()) {
                     cfg.setPluginProperty(matcher.group(1), matcher.group(2), val);
                 }
             }
             return cfg;
         }
 
         private static Set<String> getDisabledPlugins(Properties properties) {
             final Set<String> disabledPlugins = new HashSet<String>();
             final String disabledPluginsProp = properties.getProperty("zep.plugins.disabled");
             if (disabledPluginsProp != null) {
                 String[] userPluginsArray = disabledPluginsProp.split(",");
                 for (String userPlugin : userPluginsArray) {
                     String userPluginId = userPlugin.trim();
                     if (userPluginId.length() > 0) {
                         disabledPlugins.add(userPluginId);
                     }
                 }
             }
             return disabledPlugins;
         }
 
         public <T extends EventPlugin> void loadPluginsOfType(Class<T> type, ApplicationContext context) {
             final Map<String, T> pluginsById = new HashMap<String, T>();
 
             // Load the plug-ins of the specified type from Spring
             final Collection<T> pluginsFromSpring = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, type, false,
                     true).values();
             for (T plugin : pluginsFromSpring) {
                 if (disabledPlugins.contains(plugin.getId())) {
                     logger.info("Plugin {} is disabled", plugin.getId());
                 }
                 else if (allPluginIds.contains(plugin.getId()) || pluginsById.containsKey(plugin.getId())) {
                     logger.warn("Multiple plugins with id {} found", plugin.getId());
                 }
                 else {
                     pluginsById.put(plugin.getId(), plugin);
                 }
             }
 
             // Attempt to resolve dependencies and detect dependency cycles
             boolean resolved = false;
             while (!resolved) {
                 try {
                     detectCycles(pluginsById, allPluginIds);
                     resolved = true;
                 } catch (MissingDependencyException e) {
                     logger.error("Failed to resolve dependency {} of {}, disabling", e.getDependencyId(),
                             e.getPluginId());
                     pluginsById.remove(e.getPluginId());
                 } catch (DependencyCycleException e) {
                     logger.error("Cycle detected in dependencies for {}, disabling", e.getPluginId());
                     pluginsById.remove(e.getPluginId());
                 }
             }
 
             // Sort the resulting plug-ins in order of their dependencies
             final List<T> sorted = sortPluginsByDependencies(pluginsById);
             for (T plugin : sorted) {
                 try {
                     logger.info("Starting plug-in: {}", plugin.getId());
                     plugin.start(this.pluginConfig.getPluginProperties(plugin.getId()));
                     this.allPluginIds.add(plugin.getId());
                 } catch (Exception e) {
                     logger.warn("Failed to start plug-in: " + plugin.getId(), e);
                 }
             }
             this.plugins.put(type, sorted);
         }
 
         public void shutdown() {
             for (List<? extends EventPlugin> plugins : this.plugins.values()) {
                 for (EventPlugin plugin : plugins) {
                     try {
                         logger.info("Stopping plug-in: {}", plugin.getId());
                         plugin.stop();
                     } catch (Exception e) {
                         logger.warn("Failed to stop plug-in: " + plugin.getId(), e);
                     }
                 }
             }
             this.plugins.clear();
             this.allPluginIds.clear();
         }
 
         public <T extends EventPlugin> List<T> getPluginsByType(Class<T> type) {
             List<T> existing = (List<T>) this.plugins.get(type);
             if (existing == null) {
                 existing = Collections.emptyList();
             }
             else {
                 existing = Collections.unmodifiableList(existing);
             }
             return existing;
         }
     }
 
     private void loadPlugins(ApplicationContext context) {
         // Load the plug-ins in order of execution. Post-create can depend on pre-create,
         // and post-index can depend on post-create and pre-create.
         this.pluginRepository.loadPluginsOfType(EventPreCreatePlugin.class, context);
         this.pluginRepository.loadPluginsOfType(EventPostCreatePlugin.class, context);
         this.pluginRepository.loadPluginsOfType(EventPostIndexPlugin.class, context);
 
         logger.info("Loaded plug-ins");
     }
 
     private AtomicBoolean initializedPlugins = new AtomicBoolean();
 
     @Override
     public void onApplicationEvent(ContextRefreshedEvent event) {
         if (!initializedPlugins.compareAndSet(false, true)) {
             return;
         }
         if (this.pluginClassLoader != null) {
             // Create a child ApplicationContext to use to load plug-ins with the plug-in class loader.
             final ClassLoader current = Thread.currentThread().getContextClassLoader();
             Thread.currentThread().setContextClassLoader(this.pluginClassLoader);
             try {
                 AnnotationConfigApplicationContext pluginApplicationContext = new AnnotationConfigApplicationContext();
                 pluginApplicationContext.setId("Plug-in Application Context");
                 pluginApplicationContext.setClassLoader(this.pluginClassLoader);
                 pluginApplicationContext.setParent(event.getApplicationContext());
                 pluginApplicationContext.scan("org.zenoss", "com.zenoss", "zenpacks");
                 pluginApplicationContext.refresh();
                 loadPlugins(pluginApplicationContext);
             } catch (RuntimeException e) {
                 logger.warn("Failed to configure plug-in application context", e);
                 throw e;
             } finally {
                 Thread.currentThread().setContextClassLoader(current);
             }
         }
         else {
             // Load plug-ins using the primary application context - no plug-ins were found on the classpath.
             loadPlugins(event.getApplicationContext());
         }
         this.applicationEventPublisher.publishEvent(new PluginServiceStartedEvent(this));
     }
 
     @Override
     public <T extends EventPlugin> List<T> getPluginsByType(Class<T> clazz) {
         return this.pluginRepository.getPluginsByType(clazz);
     }
 
     public void shutdown() {
         pluginRepository.shutdown();
     }
 }
