 package com.atlassian.plugin;
 
 import com.atlassian.plugin.loaders.PluginLoader;
 
 import java.util.*;
 import java.io.InputStream;
 
 public class DefaultPluginManager implements PluginManager
 {
     private final List pluginLoaders;
     private final PluginStateStore store;
     private ModuleDescriptorFactory moduleDescriptorFactory;
     private PluginManagerState currentState;
     private HashMap plugins;
     private HashMap pluginToPluginLoader; // will store a plugin as a key and pluginLoader as a value
 
     public DefaultPluginManager(PluginStateStore store, List pluginLoaders, ModuleDescriptorFactory moduleDescriptorFactory)
     {
         this.pluginLoaders = pluginLoaders;
         this.store = store;
         this.moduleDescriptorFactory = moduleDescriptorFactory;
         this.currentState = store.loadPluginState();
     }
 
     /**
      * Initialize all plugins the first time.
      * @throws PluginParseException
      */
     public void init() throws PluginParseException
     {
         this.plugins = new HashMap();
         this.pluginToPluginLoader = new HashMap();
 
         // retrieve all the plugins
         for (Iterator iterator = pluginLoaders.iterator(); iterator.hasNext();)
         {
             PluginLoader loader = (PluginLoader) iterator.next();
 
             for (Iterator iterator1 = loader.loadAllPlugins(moduleDescriptorFactory).iterator(); iterator1.hasNext();)
             {
                 addPlugin(loader, (Plugin) iterator1.next());
             }
         }
     }
 
    public void findNewPlugins() throws PluginParseException
     {
         for (Iterator iterator = pluginLoaders.iterator(); iterator.hasNext();)
         {
             PluginLoader loader = (PluginLoader) iterator.next();
 
             if (loader.supportsAddition())
             {
                 Collection addedPlugins = loader.addFoundPlugins(moduleDescriptorFactory);
                 for (Iterator iterator1 = addedPlugins.iterator(); iterator1.hasNext();)
                 {
                     Plugin newPlugin = (Plugin) iterator1.next();
                     addPlugin(loader, newPlugin);
                 }
             }
         }
     }
 
     /**
      * Uninstall (delete) a plugin if possible.
      * <p>
      * Be very careful when using this method, the plugin cannot be brought back.
      */
     public void uninstall(Plugin plugin) throws PluginException
     {
         if (!plugin.isUninstallable())
             throw new PluginException("Plugin is not uninstallable: " + plugin.getKey());
 
         PluginLoader loader = (PluginLoader) pluginToPluginLoader.remove(plugin);
 
         if (loader == null || !loader.supportsRemoval())
         {
             throw new PluginException("Not uninstalling plugin - could not find plugin loader, or loader doesn't allow removal. Plugin: " + plugin.getKey());
         }
 
         plugins.remove(plugin.getKey());
 
         for (Iterator it = plugin.getModuleDescriptors().iterator(); it.hasNext();)
         {
             ModuleDescriptor descriptor = (ModuleDescriptor) it.next();
 
             // if the module is StateAware, then disable it (matches enable())
             if (descriptor instanceof StateAware && isPluginModuleEnabled(descriptor.getCompleteKey()))
                 ((StateAware)descriptor).disabled();
 
             // now destroy it (matches init())
             descriptor.destroy(plugin);
         }
 
         loader.removePlugin(plugin);
     }
 
     protected void addPlugin(PluginLoader loader, Plugin plugin) throws PluginParseException
     {
         // testing to make sure plugin keys are unique
         if (plugins.containsKey(plugin.getKey()))
             throw new PluginParseException("Duplicate plugin key found: '" + plugin.getKey() + "'");
 
         plugins.put(plugin.getKey(), plugin);
 
         for (Iterator it = plugin.getModuleDescriptors().iterator(); it.hasNext();)
         {
             ModuleDescriptor descriptor = (ModuleDescriptor) it.next();
             if (descriptor instanceof StateAware && isPluginModuleEnabled(descriptor.getCompleteKey()))
                 ((StateAware)descriptor).enabled();
         }
 
         pluginToPluginLoader.put(plugin, loader);
     }
 
     private void saveState()
     {
         store.savePluginState(currentState);
     }
 
     public Collection getPlugins()
     {
         return plugins.values();
     }
 
     public Collection getEnabledPlugins()
     {
         List result = new ArrayList();
 
         for (Iterator iterator = plugins.keySet().iterator(); iterator.hasNext();)
         {
             String key = (String) iterator.next();
             Plugin p = getEnabledPlugin(key);
 
             if (p != null)
                 result.add(p);
         }
 
         return result;
     }
 
     public Plugin getPlugin(String key)
     {
         return (Plugin) plugins.get(key);
     }
 
     public Plugin getEnabledPlugin(String pluginKey)
     {
         if (isPluginEnabled(pluginKey))
             return getPlugin(pluginKey);
 
         return null;
     }
 
     public ModuleDescriptor getPluginModule(String completeKey)
     {
         ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
         final Plugin plugin = getPlugin(key.getPluginKey());
 
         if (plugin != null)
             return plugin.getModuleDescriptor(key.getModuleKey());
 
         return null;
     }
 
     public ModuleDescriptor getEnabledPluginModule(String completeKey)
     {
         ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
         if (isPluginModuleEnabled(completeKey))
         {
             return getEnabledPlugin(key.getPluginKey()).getModuleDescriptor(key.getModuleKey());
         }
 
         return null;
     }
 
     public List getEnabledModulesByClass(Class moduleClass)
     {
         List result = new LinkedList();
 
         for (Iterator iterator = plugins.values().iterator(); iterator.hasNext();)
         {
             Plugin plugin = (Plugin) iterator.next();
 
             if (isPluginEnabled(plugin.getKey()))
             {
                 for (Iterator iterator1 = plugin.getModuleDescriptors().iterator(); iterator1.hasNext();)
                 {
                     ModuleDescriptor moduleDescriptor = (ModuleDescriptor) iterator1.next();
 
                     if (!isPluginModuleEnabled(moduleDescriptor.getCompleteKey()))
                         continue;
 
                     final Class moduleDescClass = moduleDescriptor.getModuleClass();
                     if (moduleDescClass != null && moduleClass.isAssignableFrom(moduleDescClass))
                     {
                         final Object module = moduleDescriptor.getModule();
                         if (module != null)
                         {
                             result.add(module);
                         }
                     }
                 }
             }
         }
 
         return result;
     }
 
     public void enablePlugin(String key)
     {
         if (key == null)
             throw new IllegalArgumentException("You must specify a plugin key to disable.");
 
         if (plugins.containsKey(key))
         {
             Plugin plugin = (Plugin) plugins.get(key);
 
             if (!plugin.isEnabledByDefault())
                 currentState.setState(key, Boolean.TRUE);
             else
                 currentState.removeState(key);
 
             for (Iterator it = plugin.getModuleDescriptors().iterator(); it.hasNext();)
             {
                 ModuleDescriptor descriptor = (ModuleDescriptor) it.next();
                 if (descriptor instanceof StateAware && isPluginModuleEnabled(descriptor.getCompleteKey()))
                     ((StateAware)descriptor).enabled();
             }
             
             saveState();
         }
     }
 
     public void disablePlugin(String key)
     {
         if (key == null)
             throw new IllegalArgumentException("You must specify a plugin key to disable.");
 
         if (plugins.containsKey(key))
         {
             Plugin plugin = (Plugin) plugins.get(key);
 
             for (Iterator it = plugin.getModuleDescriptors().iterator(); it.hasNext();)
             {
                 ModuleDescriptor descriptor = (ModuleDescriptor) it.next();
                 if (descriptor instanceof StateAware && isPluginModuleEnabled(descriptor.getCompleteKey()))
                     ((StateAware)descriptor).disabled();
             }
 
             if (plugin.isEnabledByDefault())
                 currentState.setState(key, Boolean.FALSE);
             else
                 currentState.removeState(key);
 
 
             saveState();
         }
     }
 
     public void disablePluginModule(String completeKey)
     {
         if (completeKey == null)
             throw new IllegalArgumentException("You must specify a plugin module key to disable.");
 
         final ModuleDescriptor module = getPluginModule(completeKey);
         if (module != null)
         {
             if (module.isEnabledByDefault())
                 currentState.setState(completeKey, Boolean.FALSE);
             else
                 currentState.removeState(completeKey);
 
             if (module instanceof StateAware)
                 ((StateAware) module).disabled();
 
             saveState();
         }
     }
 
     public void enablePluginModule(String completeKey)
     {
         if (completeKey == null)
             throw new IllegalArgumentException("You must specify a plugin module key to disable.");
 
         final ModuleDescriptor module = getPluginModule(completeKey);
         if (module != null)
         {
             if (!module.isEnabledByDefault())
                 currentState.setState(completeKey, Boolean.TRUE);
             else
                 currentState.removeState(completeKey);
 
             if (module instanceof StateAware)
                 ((StateAware) module).enabled();
 
             saveState();
         }
     }
 
     public boolean isPluginModuleEnabled(String completeKey)
     {
         ModuleCompleteKey key = new ModuleCompleteKey(completeKey);
 
         final ModuleDescriptor pluginModule = getPluginModule(completeKey);
         return isPluginEnabled(key.getPluginKey()) && pluginModule != null && currentState.isEnabled(pluginModule);
     }
 
     public boolean isPluginEnabled(String key)
     {
         return plugins.containsKey(key) && currentState.isEnabled((Plugin) plugins.get(key));
     }
 
     public List getEnabledModuleDescriptorsByClass(Class descriptorClazz)
     {
         List result = new LinkedList();
 
         for (Iterator iterator = plugins.values().iterator(); iterator.hasNext();)
         {
             Plugin plugin = (Plugin) iterator.next();
 
             if (isPluginEnabled(plugin.getKey()))
             {
                 for (Iterator iterator1 = plugin.getModuleDescriptors().iterator(); iterator1.hasNext();)
                 {
                     ModuleDescriptor module = (ModuleDescriptor) iterator1.next();
 
                     if (descriptorClazz.isInstance(module) && isPluginModuleEnabled(module.getCompleteKey()))
                     {
                         result.add(module);
                     }
                 }
             }
         }
 
         return result;
     }
 
     /**
      * @throws IllegalArgumentException If the name is not a registered module descriptor
      */
     public List getEnabledModuleDescriptorsByType(String type) throws PluginParseException, IllegalArgumentException
     {
         final Class descriptorClazz = (Class) moduleDescriptorFactory.getModuleDescriptorClass(type);
 
         if (descriptorClazz == null)
             throw new IllegalArgumentException("No module descriptor of type: " + type + " found.");
 
         return getEnabledModuleDescriptorsByClass(descriptorClazz);
     }
 
     public InputStream getDynamicResourceAsStream(String name)
     {
         for (Iterator iterator = plugins.values().iterator(); iterator.hasNext();)
         {
             Plugin plugin = (Plugin) iterator.next();
             if (plugin.isResourceLoading() && isPluginEnabled(plugin.getKey()))
             {
                 InputStream is = plugin.getResourceAsStream(name);
 
                 if (is != null)
                     return is;
             }
         }
 
         return null;
     }
 }
