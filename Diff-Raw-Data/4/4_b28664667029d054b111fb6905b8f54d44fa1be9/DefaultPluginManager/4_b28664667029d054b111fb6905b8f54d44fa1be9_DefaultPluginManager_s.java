 package com.atlassian.plugin;
 
 import static com.atlassian.plugin.util.collect.CollectionUtil.filter;
 import static com.atlassian.plugin.util.collect.CollectionUtil.toList;
 import static com.atlassian.plugin.util.collect.CollectionUtil.transform;
 
 import com.atlassian.plugin.classloader.PluginsClassLoader;
 import com.atlassian.plugin.descriptors.UnloadableModuleDescriptor;
 import com.atlassian.plugin.descriptors.UnloadableModuleDescriptorFactory;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.event.events.PluginDisabledEvent;
 import com.atlassian.plugin.event.events.PluginEnabledEvent;
 import com.atlassian.plugin.event.events.PluginFrameworkShutdownEvent;
 import com.atlassian.plugin.event.events.PluginFrameworkStartedEvent;
 import com.atlassian.plugin.event.events.PluginFrameworkStartingEvent;
 import com.atlassian.plugin.impl.UnloadablePlugin;
 import com.atlassian.plugin.impl.UnloadablePluginFactory;
 import com.atlassian.plugin.loaders.DynamicPluginLoader;
 import com.atlassian.plugin.loaders.PluginLoader;
 import com.atlassian.plugin.parsers.DescriptorParserFactory;
 import com.atlassian.plugin.predicate.EnabledModulePredicate;
 import com.atlassian.plugin.predicate.EnabledPluginPredicate;
 import com.atlassian.plugin.predicate.ModuleDescriptorOfClassPredicate;
 import com.atlassian.plugin.predicate.ModuleDescriptorOfTypePredicate;
 import com.atlassian.plugin.predicate.ModuleDescriptorPredicate;
 import com.atlassian.plugin.predicate.ModuleOfClassPredicate;
 import com.atlassian.plugin.predicate.PluginPredicate;
 import com.atlassian.plugin.util.PluginUtils;
 import com.atlassian.plugin.util.WaitUntil;
 import com.atlassian.plugin.util.collect.CollectionUtil;
 import com.atlassian.plugin.util.collect.Function;
 import com.atlassian.plugin.util.collect.Predicate;
 
 import com.atlassian.plugin.util.PluginUtils;
 import com.atlassian.plugin.util.concurrent.CopyOnWriteMap;
 import org.apache.commons.collections.Closure;
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeSet;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 /**
  * This implementation delegates the initiation and classloading of plugins to a
  * list of {@link PluginLoader}s and records the state of plugins in a {@link PluginStateStore}.
  * <p/>
  * This class is responsible for enabling and disabling plugins and plugin modules and reflecting these
  * state changes in the PluginStateStore.
  * <p/>
  * An interesting quirk in the design is that {@link #installPlugin(PluginArtifact)} explicitly stores
  * the plugin via a {@link PluginInstaller}, whereas {@link #uninstall(Plugin)} relies on the
  * underlying {@link PluginLoader} to remove the plugin if necessary.
  */
 public class DefaultPluginManager implements PluginController, PluginAccessor, PluginSystemLifecycle, PluginManager
 {
     private static final Log log = LogFactory.getLog(DefaultPluginManager.class);
     private final List<PluginLoader> pluginLoaders;
     private final PluginStateStore store;
     private final ModuleDescriptorFactory moduleDescriptorFactory;
     private final PluginsClassLoader classLoader;
    private final Map<String, Plugin> plugins = new HashMap<String, Plugin>();
     private final PluginEventManager pluginEventManager;
 
     /**
      * Installer used for storing plugins. Used by {@link #installPlugin(PluginArtifact)}.
      */
     private PluginInstaller pluginInstaller;
 
     /**
      * Stores {@link Plugin}s as a key and {@link PluginLoader} as a value.
      */
     private final Map<Plugin, PluginLoader> pluginToPluginLoader = new HashMap<Plugin, PluginLoader>();
 
     public DefaultPluginManager(final PluginStateStore store, final List<PluginLoader> pluginLoaders, final ModuleDescriptorFactory moduleDescriptorFactory, final PluginEventManager pluginEventManager)
     {
         if (store == null)
         {
             throw new IllegalArgumentException("PluginStateStore must not be null.");
         }
         if (pluginLoaders == null)
         {
             throw new IllegalArgumentException("Plugin Loaders list must not be null.");
         }
         if (moduleDescriptorFactory == null)
         {
             throw new IllegalArgumentException("ModuleDescriptorFactory must not be null.");
         }
         if (pluginEventManager == null)
         {
             throw new IllegalArgumentException("PluginEventManager must not be null.");
         }
         this.pluginLoaders = pluginLoaders;
         this.store = store;
         this.moduleDescriptorFactory = moduleDescriptorFactory;
         this.pluginEventManager = pluginEventManager;
         classLoader = new PluginsClassLoader(this);
     }
 
     /**
      * Initialize all plugins in all loaders
      *
      * @throws PluginParseException
      */
     public void init() throws PluginParseException
     {
         final long start = System.currentTimeMillis();
         log.info("Initialising the plugin system");
         pluginEventManager.broadcast(new PluginFrameworkStartingEvent(this, this));
         for (final PluginLoader loader : pluginLoaders)
         {
             if (loader == null)
             {
                 continue;
             }
 
             addPlugins(loader, loader.loadAllPlugins(moduleDescriptorFactory));
         }
         pluginEventManager.broadcast(new PluginFrameworkStartedEvent(this, this));
         final long end = System.currentTimeMillis();
         log.info("Plugin system started in " + (end - start) + "ms");
     }
 
     /**
      * Fires the shutdown event
      * @since 2.0.0
      */
     public void shutdown()
     {
         log.info("Shutting down the plugin system");
         pluginEventManager.broadcast(new PluginFrameworkShutdownEvent(this, this));
     }
 
     /**
      * Set the plugin installation strategy for this manager
      *
      * @param pluginInstaller the plugin installation strategy to use
      * @see PluginInstaller
      */
     public void setPluginInstaller(final PluginInstaller pluginInstaller)
     {
         this.pluginInstaller = pluginInstaller;
     }
 
     protected final PluginStateStore getStore()
     {
         return store;
     }
 
     public String installPlugin(final PluginArtifact pluginArtifact) throws PluginParseException
     {
         final String key = validatePlugin(pluginArtifact);
         pluginInstaller.installPlugin(key, pluginArtifact);
         scanForNewPlugins();
         return key;
     }
 
     /**
      * Validate a plugin jar.  Looks through all plugin loaders for ones that can load the plugin and
      * extract the plugin key as proof.
      *
      * @param pluginArtifact the jar file representing the plugin
      * @return The plugin key
      * @throws PluginParseException if the plugin cannot be parsed
      * @throws NullPointerException if <code>pluginJar</code> is null.
      */
     String validatePlugin(final PluginArtifact pluginArtifact) throws PluginParseException
     {
         boolean foundADynamicPluginLoader = false;
         for (final PluginLoader loader : pluginLoaders)
         {
             if (loader instanceof DynamicPluginLoader)
             {
                 foundADynamicPluginLoader = true;
                 final String key = ((DynamicPluginLoader) loader).canLoad(pluginArtifact);
                 if (key != null)
                 {
                     return key;
                 }
             }
         }
 
         if (!foundADynamicPluginLoader)
         {
             throw new IllegalStateException("Should be at least one DynamicPluginLoader in the plugin loader list");
         }
         throw new PluginParseException("Jar " + pluginArtifact.getName() + " is not a valid plugin");
     }
 
     public int scanForNewPlugins() throws PluginParseException
     {
         int numberFound = 0;
 
         for (final PluginLoader loader : pluginLoaders)
         {
             if (loader != null)
             {
                 if (loader.supportsAddition())
                 {
                     final List<Plugin> pluginsToAdd = new ArrayList<Plugin>();
                     for (Plugin plugin : loader.addFoundPlugins(moduleDescriptorFactory))
                     {
                         // Only actually install the plugin if its module descriptors support it.  Otherwise, mark it as
                         // unloadable.
                         if (!(plugin instanceof UnloadablePlugin) && PluginUtils.doesPluginRequireRestart(plugin))
                         {
                             try
                             {
                                 plugin.close();
                             }
                             catch (final RuntimeException ex)
                             {
                                 log.warn("Unable to uninstall the plugin after it was determined to require a restart", ex);
                             }
                             final UnloadablePlugin unloadablePlugin = new UnloadablePlugin("Plugin requires a restart of the application");
                             unloadablePlugin.setKey(plugin.getKey());
                             plugin = unloadablePlugin;
                         }
                         pluginsToAdd.add(plugin);
                     }
                     addPlugins(loader, pluginsToAdd);
                     numberFound = pluginsToAdd.size();
                 }
             }
         }
 
         return numberFound;
     }
 
     public void uninstall(final Plugin plugin) throws PluginException
     {
         unloadPlugin(plugin);
 
         // PLUG-13: Plugins should not save state across uninstalls.
         removeStateFromStore(getStore(), plugin);
     }
 
     protected void removeStateFromStore(final PluginStateStore stateStore, final Plugin plugin)
     {
         final PluginManagerState currentState = stateStore.loadPluginState();
         currentState.removeState(plugin.getKey());
         for (final ModuleDescriptor<?> moduleDescriptor : plugin.getModuleDescriptors())
         {
             currentState.removeState(moduleDescriptor.getCompleteKey());
         }
         stateStore.savePluginState(currentState);
     }
 
     /**
      * Unload a plugin. Called when plugins are added locally,
      * or remotely in a clustered application.
      *
      * @param plugin the plugin to remove
      * @throws PluginException if th eplugin cannot be uninstalled
      */
     protected void unloadPlugin(final Plugin plugin) throws PluginException
     {
         if (!plugin.isUninstallable())
         {
             throw new PluginException("Plugin is not uninstallable: " + plugin.getKey());
         }
 
         final PluginLoader loader = pluginToPluginLoader.get(plugin);
 
         if ((loader != null) && !loader.supportsRemoval())
         {
             throw new PluginException("Not uninstalling plugin - loader doesn't allow removal. Plugin: " + plugin.getKey());
         }
 
         if (isPluginEnabled(plugin.getKey()))
         {
             notifyPluginDisabled(plugin);
         }
 
         notifyUninstallPlugin(plugin);
         if (loader != null)
         {
             removePluginFromLoader(plugin);
         }
 
         plugins.remove(plugin.getKey());
     }
 
     private void removePluginFromLoader(final Plugin plugin) throws PluginException
     {
         if (plugin.isDeleteable())
         {
             final PluginLoader pluginLoader = pluginToPluginLoader.get(plugin);
             pluginLoader.removePlugin(plugin);
         }
 
         pluginToPluginLoader.remove(plugin);
     }
 
     protected void notifyUninstallPlugin(final Plugin plugin)
     {
         classLoader.notifyUninstallPlugin(plugin);
 
         for (final ModuleDescriptor<?> descriptor : plugin.getModuleDescriptors())
         {
             descriptor.destroy(plugin);
         }
     }
 
     protected PluginManagerState getState()
     {
         return getStore().loadPluginState();
     }
 
     /**
      * @deprecated Since 2.0.2, use {@link #addPlugins(PluginLoader,Collection<Plugin>...)} instead
      */
     @Deprecated
     protected void addPlugin(final PluginLoader loader, final Plugin plugin) throws PluginParseException
     {
         addPlugins(loader, Collections.singletonList(plugin));
     }
 
     /**
      * Update the local plugin state and enable state aware modules.
      * <p>
      * If there is an existing plugin with the same key, the version strings of the existing plugin and the plugin
      * provided to this method will be parsed and compared.  If the installed version is newer than the provided
      * version, it will not be changed.  If the specified plugin's version is the same or newer, the existing plugin
      * state will be saved and the plugin will be unloaded before the provided plugin is installed.  If the existing
      * plugin cannot be unloaded a {@link PluginException} will be thrown.
      *
      * @param loader the loader used to load this plugin
      * @param pluginsToAdd the plugins to add
      * @throws PluginParseException if the plugin cannot be parsed
      * @since 2.0.2
      */
     protected void addPlugins(final PluginLoader loader, final Collection<Plugin> pluginsToAdd) throws PluginParseException
     {
         final Set<Plugin> pluginsThatShouldBeEnabled = new HashSet<Plugin>();
         for (final Plugin plugin : new TreeSet<Plugin>(pluginsToAdd))
         {
             // testing to make sure plugin keys are unique
             if (plugins.containsKey(plugin.getKey()))
             {
                 final Plugin existingPlugin = plugins.get(plugin.getKey());
                 if (plugin.compareTo(existingPlugin) >= 0)
                 {
                     try
                     {
                         updatePlugin(existingPlugin, plugin);
                     }
                     catch (final PluginException e)
                     {
                         throw new PluginParseException(
                             "Duplicate plugin found (installed version is the same or older) and could not be unloaded: '" + plugin.getKey() + "'", e);
                     }
                 }
                 else
                 {
                     // If we find an older plugin, don't error, just ignore it. PLUG-12.
                     if (log.isDebugEnabled())
                     {
                         log.debug("Duplicate plugin found (installed version is newer): '" + plugin.getKey() + "'");
                     }
                     // and don't install the older plugin
                     continue;
                 }
             }
 
             plugins.put(plugin.getKey(), plugin);
             if (getState().isEnabled(plugin))
             {
                 try
                 {
                     plugin.setEnabled(true);
                     pluginsThatShouldBeEnabled.add(plugin);
                 }
                 catch (final RuntimeException ex)
                 {
                     log.error("Unable to enable plugin " + plugin.getKey(), ex);
                 }
             }
 
             pluginToPluginLoader.put(plugin, loader);
         }
 
         if (!plugins.isEmpty())
         {
             // Now try to enable plugins that weren't enabled before, probably due to dependency ordering issues
             WaitUntil.invoke(new WaitUntil.WaitCondition()
             {
                 public boolean isFinished()
                 {
                     for (final Iterator<Plugin> i = pluginsThatShouldBeEnabled.iterator(); i.hasNext();)
                     {
                         final Plugin plugin = i.next();
                         if (plugin.isEnabled())
                         {
                             i.remove();
                         }
                     }
                     return pluginsThatShouldBeEnabled.isEmpty();
                 }
 
                 public String getWaitMessage()
                 {
                     return "Plugins that have yet to start: " + pluginsThatShouldBeEnabled;
                 }
             }, 60);
 
             // Disable any plugins that aren't enabled by now
             if (!pluginsThatShouldBeEnabled.isEmpty())
             {
                 final StringBuilder sb = new StringBuilder();
                 for (final Plugin plugin : pluginsThatShouldBeEnabled)
                 {
                     sb.append(plugin.getKey()).append(',');
                     disablePlugin(plugin.getKey());
                 }
                 sb.deleteCharAt(sb.length() - 1);
                 log.error("Unable to start the following plugins: " + sb.toString());
             }
         }
 
         for (final Plugin plugin : pluginsToAdd)
         {
             if (plugin.isEnabled())
             {
                 // This method enables the plugin modules
                 enablePluginModules(plugin);
                 pluginEventManager.broadcast(new PluginEnabledEvent(plugin));
             }
         }
     }
 
     /**
      * Replace an already loaded plugin with another version. Relevant stored configuration for the plugin will be
      * preserved.
      *
      * @param oldPlugin Plugin to replace
      * @param newPlugin New plugin to install
      * @throws PluginException if the plugin cannot be updated
      */
     protected void updatePlugin(final Plugin oldPlugin, final Plugin newPlugin) throws PluginException
     {
         if (!oldPlugin.getKey().equals(newPlugin.getKey()))
         {
             throw new IllegalArgumentException("New plugin must have the same key as the old plugin");
         }
 
         if (log.isInfoEnabled())
         {
             log.info("Updating plugin '" + oldPlugin + "' to '" + newPlugin + "'");
         }
 
         // Preserve the old plugin configuration - uninstall changes it (as disable is called on all modules) and then
         // removes it
         final Map<String, Boolean> oldPluginState = new HashMap<String, Boolean>(getState().getPluginStateMap(oldPlugin));
 
         if (log.isDebugEnabled())
         {
             log.debug("Uninstalling old plugin: " + oldPlugin);
         }
         uninstall(oldPlugin);
         if (log.isDebugEnabled())
         {
             log.debug("Plugin uninstalled '" + oldPlugin + "', preserving old state");
         }
 
         // Build a set of module keys from the new plugin version
         final Set<String> newModuleKeys = new HashSet<String>();
         newModuleKeys.add(newPlugin.getKey());
 
         for (final ModuleDescriptor<?> moduleDescriptor : newPlugin.getModuleDescriptors())
         {
             newModuleKeys.add(moduleDescriptor.getCompleteKey());
         }
 
         // Remove any keys from the old plugin state that do not exist in the new version
         CollectionUtils.filter(oldPluginState.keySet(), new org.apache.commons.collections.Predicate()
         {
             public boolean evaluate(final Object o)
             {
                 return newModuleKeys.contains(o);
             }
         });
 
         // Restore the configuration
         final PluginManagerState currentState = getState();
         currentState.addState(oldPluginState);
         getStore().savePluginState(currentState);
     }
 
     public Collection<Plugin> getPlugins()
     {
         return plugins.values();
     }
 
     /**
      * @see PluginAccessor#getPlugins(PluginPredicate)
      * @since 0.17
      */
     public Collection<Plugin> getPlugins(final PluginPredicate pluginPredicate)
     {
         return toList(filter(getPlugins(), new Predicate<Plugin>()
         {
             public boolean evaluate(final Plugin plugin)
             {
                 return pluginPredicate.matches(plugin);
             }
         }));
     }
 
     /**
      * @see PluginAccessor#getEnabledPlugins()
      */
     public Collection<Plugin> getEnabledPlugins()
     {
         return getPlugins(new EnabledPluginPredicate(this));
     }
 
     /**
      * @see PluginAccessor#getModules(com.atlassian.plugin.predicate.ModuleDescriptorPredicate)
      * @since 0.17
      */
     public <M> Collection<M> getModules(final ModuleDescriptorPredicate<M> moduleDescriptorPredicate)
     {
         final Collection<ModuleDescriptor<M>> moduleDescriptors = getModuleDescriptors(moduleDescriptorPredicate);
         return getModules(moduleDescriptors);
     }
 
     /**
      * @see PluginAccessor#getModuleDescriptors(com.atlassian.plugin.predicate.ModuleDescriptorPredicate)
      * @since 0.17
      */
     public <M> Collection<ModuleDescriptor<M>> getModuleDescriptors(final ModuleDescriptorPredicate<M> moduleDescriptorPredicate)
     {
         final List<ModuleDescriptor<M>> moduleDescriptors = getModuleDescriptorsList(getPlugins());
         return toList(filter(moduleDescriptors, new Predicate<ModuleDescriptor<M>>()
         {
             public boolean evaluate(final ModuleDescriptor<M> input)
             {
                 return moduleDescriptorPredicate.matches(input);
             }
         }));
     }
 
     /**
      * Get the all the module descriptors from the given collection of plugins.
      * <p>
      * Be careful, this does not actually return a list of ModuleDescriptors that are M, it returns all 
      * ModuleDescriptors of all types, you must further filter the list as required.
      *
      * @param plugins a collection of {@link Plugin}s
      * @return a collection of {@link ModuleDescriptor}s
      */
     private <M> List<ModuleDescriptor<M>> getModuleDescriptorsList(final Collection<Plugin> plugins)
     {
         // hack way to get typed descriptors from plugin and keep generics happy
         final List<ModuleDescriptor<M>> moduleDescriptors = new LinkedList<ModuleDescriptor<M>>();
         for (final Plugin plugin : plugins)
         {
             final Collection<ModuleDescriptor<?>> descriptors = plugin.getModuleDescriptors();
             for (final ModuleDescriptor<?> moduleDescriptor : descriptors)
             {
                 @SuppressWarnings("unchecked")
                 final ModuleDescriptor<M> typedDescriptor = (ModuleDescriptor<M>) moduleDescriptor;
                 moduleDescriptors.add(typedDescriptor);
             }
         }
         return moduleDescriptors;
     }
 
     /**
      * Get the modules of all the given descriptor.
      *
      * @param moduleDescriptors the collection of module descriptors to get the modules from.
      * @return a {@link Collection} modules that can be any type of object.
      *         This collection will not contain any null value.
      */
     private <M> List<M> getModules(final Iterable<ModuleDescriptor<M>> moduleDescriptors)
     {
         return transform(moduleDescriptors, new Function<ModuleDescriptor<M>, M>()
         {
             public M get(final ModuleDescriptor<M> input)
             {
                 return input.getModule();
             }
         });
     }
 
     public Plugin getPlugin(final String key)
     {
         return plugins.get(key);
     }
 
     public Plugin getEnabledPlugin(final String pluginKey)
     {
         if (!isPluginEnabled(pluginKey))
         {
             return null;
         }
         return getPlugin(pluginKey);
     }
 
     public ModuleDescriptor<?> getPluginModule(final String completeKey)
     {
         final ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
         final Plugin plugin = getPlugin(key.getPluginKey());
 
         if (plugin == null)
         {
             return null;
         }
         return plugin.getModuleDescriptor(key.getModuleKey());
     }
 
     public ModuleDescriptor<?> getEnabledPluginModule(final String completeKey)
     {
         final ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
         // If it's disabled, return null
         if (!isPluginModuleEnabled(completeKey))
         {
             return null;
         }
 
         return getEnabledPlugin(key.getPluginKey()).getModuleDescriptor(key.getModuleKey());
     }
 
     /**
      * @see PluginAccessor#getEnabledModulesByClass(Class)
      */
     public <M> List<M> getEnabledModulesByClass(final Class<M> moduleClass)
     {
         return getModules(getEnabledModuleDescriptorsByModuleClass(moduleClass));
     }
 
     /**
      * @see PluginAccessor#getEnabledModulesByClassAndDescriptor(Class[], Class)
      * @deprecated since 0.17, use {@link #getModules(com.atlassian.plugin.predicate.ModuleDescriptorPredicate)} with an appropriate predicate instead.
      */
     @Deprecated
     public <M> List<M> getEnabledModulesByClassAndDescriptor(final Class<ModuleDescriptor<M>>[] descriptorClasses, final Class<M> moduleClass)
     {
         final Iterable<ModuleDescriptor<M>> moduleDescriptors = filterModuleDescriptors(getEnabledModuleDescriptorsByModuleClass(moduleClass),
             new ModuleDescriptorOfClassPredicate<M>(descriptorClasses));
 
         return getModules(moduleDescriptors);
     }
 
     /**
      * @see PluginAccessor#getEnabledModulesByClassAndDescriptor(Class, Class)
      * @deprecated since 0.17, use {@link #getModules(com.atlassian.plugin.predicate.ModuleDescriptorPredicate)} with an appropriate predicate instead.
      */
     @Deprecated
     public <M> List<M> getEnabledModulesByClassAndDescriptor(final Class<ModuleDescriptor<M>> descriptorClass, final Class<M> moduleClass)
     {
         final Iterable<ModuleDescriptor<M>> moduleDescriptors = getEnabledModuleDescriptorsByModuleClass(moduleClass);
         return getModules(filterModuleDescriptors(moduleDescriptors, new ModuleDescriptorOfClassPredicate<M>(descriptorClass)));
     }
 
     /**
      * Get all module descriptor that are enabled and for which the module is an instance of the given class.
      *
      * @param moduleClass the class of the module within the module descriptor.
      * @return a collection of {@link ModuleDescriptor}s
      */
     private <M> Collection<ModuleDescriptor<M>> getEnabledModuleDescriptorsByModuleClass(final Class<M> moduleClass)
     {
         Iterable<ModuleDescriptor<M>> moduleDescriptors = getModuleDescriptorsList(getEnabledPlugins());
         moduleDescriptors = filterModuleDescriptors(moduleDescriptors, new ModuleOfClassPredicate<M>(moduleClass));
         moduleDescriptors = filterModuleDescriptors(moduleDescriptors, new EnabledModulePredicate<M>(this));
 
         return toList(moduleDescriptors);
     }
 
     public <D extends ModuleDescriptor<?>> List<D> getEnabledModuleDescriptorsByClass(final Class<D> descriptorClazz)
     {
         return getEnabledModuleDescriptorsByClass(descriptorClazz, false);
     }
 
     /**
      * This method has been reverted to pre PLUG-40 to fix performance issues that were encountered during
      * load testing. This should be reverted to the state it was in at 54639 when the fundamental issue leading
      * to this slowdown has been corrected (that is, slowness of PluginClassLoader).
      *
      * @see PluginAccessor#getEnabledModuleDescriptorsByClass(Class)
      */
     public <D extends ModuleDescriptor<?>> List<D> getEnabledModuleDescriptorsByClass(final Class<D> descriptorClazz, final boolean verbose)
     {
         final List<D> result = new LinkedList<D>();
         for (final Plugin plugin : plugins.values())
         {
             // Skip disabled plugins
             if (!isPluginEnabled(plugin.getKey()))
             {
                 if (verbose && log.isInfoEnabled())
                 {
                     log.info("Plugin [" + plugin.getKey() + "] is disabled.");
                 }
                 continue;
             }
 
             for (final ModuleDescriptor<?> module : plugin.getModuleDescriptors())
             {
                 if (descriptorClazz.isInstance(module) && isPluginModuleEnabled(module.getCompleteKey()))
                 {
                     @SuppressWarnings("unchecked")
                     final D moduleDescriptor = (D) module;
                     result.add(moduleDescriptor);
                 }
                 else
                 {
                     if (verbose && log.isInfoEnabled())
                     {
                         log.info("Module [" + module.getCompleteKey() + "] is disabled.");
                     }
                 }
             }
         }
 
         return result;
     }
 
     /**
      * @see PluginAccessor#getEnabledModuleDescriptorsByType(String)
      * @deprecated since 0.17, use {@link #getModuleDescriptors(com.atlassian.plugin.predicate.ModuleDescriptorPredicate)} with an appropriate predicate instead.
      */
     @Deprecated
     public <M> List<ModuleDescriptor<M>> getEnabledModuleDescriptorsByType(final String type) throws PluginParseException, IllegalArgumentException
     {
         Iterable<ModuleDescriptor<M>> moduleDescriptors = getModuleDescriptorsList(getEnabledPlugins());
         moduleDescriptors = filterModuleDescriptors(moduleDescriptors, new ModuleDescriptorOfTypePredicate<M>(moduleDescriptorFactory, type));
         moduleDescriptors = filterModuleDescriptors(moduleDescriptors, new EnabledModulePredicate<M>(this));
         return toList(moduleDescriptors);
     }
 
     /**
      * Filters out a collection of {@link ModuleDescriptor}s given a predicate.
      *
      * @param moduleDescriptors         the collection of {@link ModuleDescriptor}s to filter.
      * @param moduleDescriptorPredicate the predicate to use for filtering.
      */
     private static <M> Iterable<ModuleDescriptor<M>> filterModuleDescriptors(final Iterable<ModuleDescriptor<M>> moduleDescriptors, final ModuleDescriptorPredicate<M> moduleDescriptorPredicate)
     {
         return CollectionUtil.filter(moduleDescriptors, new Predicate<ModuleDescriptor<M>>()
         {
             public boolean evaluate(final ModuleDescriptor<M> input)
             {
                 return moduleDescriptorPredicate.matches(input);
             }
         });
     }
 
     public void enablePlugin(final String key)
     {
         if (key == null)
         {
             throw new IllegalArgumentException("You must specify a plugin key to disable.");
         }
 
         if (!plugins.containsKey(key))
         {
             if (log.isInfoEnabled())
             {
                 log.info("No plugin was found for key '" + key + "'. Not enabling.");
             }
 
             return;
         }
 
         final Plugin plugin = plugins.get(key);
 
         if (!plugin.getPluginInformation().satisfiesMinJavaVersion())
         {
             log.error("Minimum Java version of '" + plugin.getPluginInformation().getMinJavaVersion() + "' was not satisfied for module '" + key + "'. Not enabling.");
             return;
         }
 
         plugin.setEnabled(true);
 
         // Only change the state if the plugin was enabled successfully
         if (WaitUntil.invoke(new PluginEnabledCondition(plugin)))
         {
             enablePluginState(plugin, getStore());
             notifyPluginEnabled(plugin);
         }
     }
 
     protected void enablePluginState(final Plugin plugin, final PluginStateStore stateStore)
     {
         final PluginManagerState currentState = stateStore.loadPluginState();
         final String key = plugin.getKey();
         if (!plugin.isEnabledByDefault())
         {
             currentState.setState(key, Boolean.TRUE);
         }
         else
         {
             currentState.removeState(key);
         }
         stateStore.savePluginState(currentState);
     }
 
     /**
      * Called on all clustered application nodes, rather than {@link #enablePlugin(String)}
      * to just update the local state, state aware modules and loaders, but not affect the
      * global plugin state.
      *
      * @param plugin the plugin being enabled
      */
     protected void notifyPluginEnabled(final Plugin plugin)
     {
         plugin.setEnabled(true);
         classLoader.notifyPluginOrModuleEnabled();
         enablePluginModules(plugin);
         pluginEventManager.broadcast(new PluginEnabledEvent(plugin));
     }
 
     /**
      * For each module in the plugin, call the module descriptor's enabled() method if the module is StateAware and enabled.
      *
      * @param plugin the plugin to enable
      */
     private void enablePluginModules(final Plugin plugin)
     {
         for (final ModuleDescriptor<?> descriptor : plugin.getModuleDescriptors())
         {
             if (!(descriptor instanceof StateAware))
             {
                 if (log.isDebugEnabled())
                 {
                     log.debug("ModuleDescriptor '" + descriptor.getName() + "' is not StateAware. No need to enable.");
                 }
                 continue;
             }
 
             if (!isPluginModuleEnabled(descriptor.getCompleteKey()))
             {
                 if (log.isDebugEnabled())
                 {
                     log.debug("Plugin module is disabled, so not enabling ModuleDescriptor '" + descriptor.getName() + "'.");
                 }
                 continue;
             }
 
             try
             {
                 if (log.isDebugEnabled())
                 {
                     log.debug("Enabling " + descriptor.getKey());
                 }
                 ((StateAware) descriptor).enabled();
             }
             catch (final Throwable exception) // catch any errors and insert an UnloadablePlugin (PLUG-7)
             {
                 log.error("There was an error loading the descriptor '" + descriptor.getName() + "' of plugin '" + plugin.getKey() + "'. Disabling.",
                     exception);
                 replacePluginWithUnloadablePlugin(plugin, descriptor, exception);
             }
         }
         classLoader.notifyPluginOrModuleEnabled();
     }
 
     public void disablePlugin(final String key)
     {
         if (key == null)
         {
             throw new IllegalArgumentException("You must specify a plugin key to disable.");
         }
 
         if (!plugins.containsKey(key))
         {
             if (log.isInfoEnabled())
             {
                 log.info("No plugin was found for key '" + key + "'. Not disabling.");
             }
 
             return;
         }
 
         final Plugin plugin = plugins.get(key);
 
         notifyPluginDisabled(plugin);
         disablePluginState(plugin, getStore());
     }
 
     protected void disablePluginState(final Plugin plugin, final PluginStateStore stateStore)
     {
         final String key = plugin.getKey();
         final PluginManagerState currentState = stateStore.loadPluginState();
         if (plugin.isEnabledByDefault())
         {
             currentState.setState(key, Boolean.FALSE);
         }
         else
         {
             currentState.removeState(key);
         }
         stateStore.savePluginState(currentState);
     }
 
     protected List<String> getEnabledStateAwareModuleKeys(final Plugin plugin)
     {
         final List<String> keys = new ArrayList<String>();
         final List<ModuleDescriptor<?>> moduleDescriptors = new ArrayList<ModuleDescriptor<?>>(plugin.getModuleDescriptors());
         Collections.reverse(moduleDescriptors);
         for (final ModuleDescriptor<?> md : moduleDescriptors)
         {
             if (md instanceof StateAware)
             {
                 if (isPluginModuleEnabled(md.getCompleteKey()))
                 {
                     keys.add(md.getCompleteKey());
                 }
             }
         }
         return keys;
     }
 
     protected void notifyPluginDisabled(final Plugin plugin)
     {
         final List<String> keysToDisable = getEnabledStateAwareModuleKeys(plugin);
 
         for (final String key : keysToDisable)
         {
             final StateAware descriptor = (StateAware) getPluginModule(key);
             descriptor.disabled();
         }
 
         // This needs to happen after modules are disabled to prevent errors 
         plugin.setEnabled(false);
         pluginEventManager.broadcast(new PluginDisabledEvent(plugin));
     }
 
     public void disablePluginModule(final String completeKey)
     {
         if (completeKey == null)
         {
             throw new IllegalArgumentException("You must specify a plugin module key to disable.");
         }
 
         final ModuleDescriptor<?> module = getPluginModule(completeKey);
 
         if (module == null)
         {
             if (log.isInfoEnabled())
             {
                 log.info("Returned module for key '" + completeKey + "' was null. Not disabling.");
             }
 
             return;
         }
         disablePluginModuleState(module, getStore());
         notifyModuleDisabled(module);
     }
 
     protected void disablePluginModuleState(final ModuleDescriptor<?> module, final PluginStateStore stateStore)
     {
         final String completeKey = module.getCompleteKey();
         final PluginManagerState currentState = stateStore.loadPluginState();
         if (module.isEnabledByDefault())
         {
             currentState.setState(completeKey, Boolean.FALSE);
         }
         else
         {
             currentState.removeState(completeKey);
         }
         stateStore.savePluginState(currentState);
     }
 
     protected void notifyModuleDisabled(final ModuleDescriptor<?> module)
     {
         if (module instanceof StateAware)
         {
             ((StateAware) module).disabled();
         }
     }
 
     public void enablePluginModule(final String completeKey)
     {
         if (completeKey == null)
         {
             throw new IllegalArgumentException("You must specify a plugin module key to disable.");
         }
 
         final ModuleDescriptor<?> module = getPluginModule(completeKey);
 
         if (module == null)
         {
             if (log.isInfoEnabled())
             {
                 log.info("Returned module for key '" + completeKey + "' was null. Not enabling.");
             }
 
             return;
         }
 
         if (!module.satisfiesMinJavaVersion())
         {
             log.error("Minimum Java version of '" + module.getMinJavaVersion() + "' was not satisfied for module '" + completeKey + "'. Not enabling.");
             return;
         }
         enablePluginModuleState(module, getStore());
         notifyModuleEnabled(module);
     }
 
     protected void enablePluginModuleState(final ModuleDescriptor<?> module, final PluginStateStore stateStore)
     {
         final String completeKey = module.getCompleteKey();
         final PluginManagerState currentState = stateStore.loadPluginState();
         if (!module.isEnabledByDefault())
         {
             currentState.setState(completeKey, Boolean.TRUE);
         }
         else
         {
             currentState.removeState(completeKey);
         }
         stateStore.savePluginState(currentState);
     }
 
     protected void notifyModuleEnabled(final ModuleDescriptor<?> module)
     {
         classLoader.notifyPluginOrModuleEnabled();
         if (module instanceof StateAware)
         {
             ((StateAware) module).enabled();
         }
     }
 
     public boolean isPluginModuleEnabled(final String completeKey)
     {
         // completeKey may be null 
         if (completeKey == null)
         {
             return false;
         }
         final ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
         final ModuleDescriptor<?> pluginModule = getPluginModule(completeKey);
         return isPluginEnabled(key.getPluginKey()) && (pluginModule != null) && getState().isEnabled(pluginModule);
     }
 
     /**
      * This method checks to see if the plugin should be enabled based on the state manager and the plugin.
      *
      * @param key The plugin key
      * @return True if the plugin is enabled
      */
     public boolean isPluginEnabled(final String key)
     {
         final Plugin plugin = plugins.get(key);
 
         return plugin != null && getState().isEnabled(plugin) && plugin.isEnabled();
     }
 
     public InputStream getDynamicResourceAsStream(final String name)
     {
         return getClassLoader().getResourceAsStream(name);
     }
 
     public Class<?> getDynamicPluginClass(final String className) throws ClassNotFoundException
     {
         return getClassLoader().loadClass(className);
     }
 
     public ClassLoader getClassLoader()
     {
         return classLoader;
     }
 
     public InputStream getPluginResourceAsStream(final String pluginKey, final String resourcePath)
     {
         final Plugin plugin = getEnabledPlugin(pluginKey);
         if (plugin == null)
         {
             log.error("Attempted to retreive resource " + resourcePath + " for non-existent or inactive plugin " + pluginKey);
             return null;
         }
 
         return plugin.getResourceAsStream(resourcePath);
     }
 
     /**
      * Disables and replaces a plugin currently loaded with an UnloadablePlugin.
      *
      * @param plugin     the plugin to be replaced
      * @param descriptor the descriptor which caused the problem
      * @param throwable  the problem caught when enabling the descriptor
      * @return the UnloadablePlugin which replaced the broken plugin
      */
     private UnloadablePlugin replacePluginWithUnloadablePlugin(final Plugin plugin, final ModuleDescriptor<?> descriptor, final Throwable throwable)
     {
         final UnloadableModuleDescriptor unloadableDescriptor = UnloadableModuleDescriptorFactory.createUnloadableModuleDescriptor(plugin,
             descriptor, throwable);
         final UnloadablePlugin unloadablePlugin = UnloadablePluginFactory.createUnloadablePlugin(plugin, unloadableDescriptor);
 
         unloadablePlugin.setUninstallable(plugin.isUninstallable());
         unloadablePlugin.setDeletable(plugin.isDeleteable());
         plugins.put(plugin.getKey(), unloadablePlugin);
 
         // Disable it
         disablePluginState(plugin, getStore());
         return unloadablePlugin;
     }
 
     public boolean isSystemPlugin(final String key)
     {
         final Plugin plugin = getPlugin(key);
         return (plugin != null) && plugin.isSystemPlugin();
     }
 
     /**
      * @deprecated Since 2.0.0.beta2
      */
     @Deprecated
     public void setDescriptorParserFactory(final DescriptorParserFactory descriptorParserFactory)
     {}
 
     private static class PluginEnabledCondition implements WaitUntil.WaitCondition
     {
         private final Plugin plugin;
 
         public PluginEnabledCondition(final Plugin plugin)
         {
             this.plugin = plugin;
         }
 
         public boolean isFinished()
         {
             return plugin.isEnabled();
         }
 
         public String getWaitMessage()
         {
             return "Waiting until plugin " + plugin + " is enabled";
         }
     }
 }
