 package com.atlassian.plugin;
 
 import java.util.Collection;
 import java.util.List;
 import java.io.InputStream;
 
 /**
  * A plugin manager is responsible for retrieving plugins and modules, as well as managing plugin loading and state.
  */
 public interface PluginManager
 {
     public static final String PLUGIN_DESCRIPTOR_FILENAME = "atlassian-plugin.xml";
 
     /**
      * Initialise the plugin manager. This <b>must</b> be called before anything else.
      * @throws PluginParseException If parsing the plugins failed.
      */
     void init() throws PluginParseException;
 
     /**
      * Search all loaders and add any new plugins you find.
     * @return The number of new plugins found.
      */
    int scanForNewPlugins() throws PluginParseException;
 
     /**
      * Get all of the currently installed plugins.
      */
     Collection getPlugins();
 
     /**
      * Get all of the currently enabled plugins.
      */
     Collection getEnabledPlugins();
 
     /**
      * Retrieve a given plugin (whether enabled or not).
      * @return The enabled plugin, or null if that plugin does not exist.
      */
     Plugin getPlugin(String key);
 
     /**
      * Retrieve a given plugin if it is enabled.
      * @return The enabled plugin, or null if that plugin does not exist or is disabled.
      */
     Plugin getEnabledPlugin(String pluginKey);
 
     /**
      * Retrieve any plugin module by complete module key.
      * <p>
      * Note: the module may or may not be disabled.
      */
     ModuleDescriptor getPluginModule(String completeKey);
 
     /**
      * Retrieve an enabled plugin module by complete module key.
      */
     ModuleDescriptor getEnabledPluginModule(String completeKey);
 
     /**
      * Enable a plugin by key.
      */
     void enablePlugin(String key);
 
     /**
      * Disable a plugin by key.
      */
     void disablePlugin(String key);
 
     /**
      * Enable a plugin module by key.
      */
     void enablePluginModule(String completeKey);
 
     /**
      * Disable a plugin module by key.
      */
     void disablePluginModule(String completeKey);
 
     /**
      * Whether or not a given plugin is currently enabled.
      */
     boolean isPluginEnabled(String key);
 
     /**
      * Whether or not a given plugin module is currently enabled.
      */
     boolean isPluginModuleEnabled(String completeKey);
 
     /**
      * Retrieve all plugin modules that implement or extend a specific class.
      *
      * @return List of modules that implement or extend the given class.
      */
     List getEnabledModulesByClass(Class moduleClass);
 
     /**
      * Get all enabled module descriptors that have a specific descriptor class.
      *
      * @return List of {@link ModuleDescriptor}s that implement or extend the given class.
      */
     List getEnabledModuleDescriptorsByClass(Class descriptorClazz);
 
     /**
      * Get all enabled module descriptors that have a specific descriptor type.
      *
      * @return List of {@link ModuleDescriptor}s that are of a given type.
      */
     List getEnabledModuleDescriptorsByType(String type) throws PluginParseException;
 
     /**
      * Retrieve resource as stream from currently loaded dynamic plugins.
      */
     InputStream getDynamicResourceAsStream(String name);
 
     void uninstall(Plugin plugin) throws PluginException;
 }
