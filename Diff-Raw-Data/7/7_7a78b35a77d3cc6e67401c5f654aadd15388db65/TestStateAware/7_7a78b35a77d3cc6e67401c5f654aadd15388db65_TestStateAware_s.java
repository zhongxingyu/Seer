 package com.atlassian.plugin;
 
 import com.atlassian.plugin.impl.StaticPlugin;
 import com.atlassian.plugin.loaders.PluginLoader;
 import com.atlassian.plugin.store.MemoryPluginStateStore;
 import com.mockobjects.dynamic.Mock;
 import junit.framework.TestCase;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 /**
  * Tests that the plugin manager properly notifies StateAware plugin modules of state
  * transitions.
  */
 public class TestStateAware extends TestCase
 {
     private Mock mockEnabling;
     private Mock mockDisabled;
     private Mock mockThwarted;
     private PluginManager manager;
     private Plugin plugin1;
 
     interface Combination extends StateAware, ModuleDescriptor{};
 
     protected void setUp() throws Exception
     {
         mockEnabling = makeMockModule(Combination.class, "key1", "enabling", true);
         mockDisabled = makeMockModule(Combination.class, "key1", "disabled", false);
         mockThwarted = makeMockModule(ModuleDescriptor.class, "key1", "thwarted", true);
 
         plugin1 = new StaticPlugin();
         plugin1.setPluginInformation(new PluginInformation());
         plugin1.setKey("key1");
         plugin1.setEnabled(true);
 
         PluginLoader pluginLoader = setupPluginLoader(plugin1);
         ArrayList pluginLoaders = new ArrayList();
         pluginLoaders.add(pluginLoader);
 
         Mock mockModuleDescriptor = new Mock(ModuleDescriptorFactory.class);
 
         manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, (ModuleDescriptorFactory) mockModuleDescriptor.proxy());
 
     }
 
     /**
      * Any StateAware plugin module that is active when the plugin manager is initialised should
      * recieve an enabled message
      */
     public void testStateAwareOnInit() throws PluginParseException
     {
         plugin1.addModuleDescriptor((ModuleDescriptor) mockEnabling.proxy());
         plugin1.addModuleDescriptor((ModuleDescriptor) mockThwarted.proxy());
         plugin1.addModuleDescriptor((ModuleDescriptor) mockDisabled.proxy());
 
         mockEnabling.expect("enabled");
         manager.init();
         verifyMocks();
     }
 
     /**
      * Any StateAware plugin moudle that is explicitly enabled or disabled through the plugin manager
      * should receive the appropriate message
      */
     public void testStateAwareOnPluginModule() throws PluginParseException
     {
         ModuleDescriptor disabledModule = (ModuleDescriptor) mockDisabled.proxy();
         plugin1.addModuleDescriptor(disabledModule);
         manager.init();
 
         mockDisabled.expectAndReturn("satisfiesMinJavaVersion", true);
         mockDisabled.expect("enabled");
         manager.enablePluginModule(disabledModule.getCompleteKey());
         mockDisabled.verify();
 
         mockDisabled.expect("disabled");
         manager.disablePluginModule(disabledModule.getCompleteKey());
         mockDisabled.verify();
     }
 
     /**
      * If a plugin is disabled, any modules that are currently enabled should be sent the disabled
      * message
      */
     public void testStateAwareOnPluginDisable() throws PluginParseException
     {
         plugin1.addModuleDescriptor((ModuleDescriptor) mockEnabling.proxy());
         plugin1.addModuleDescriptor((ModuleDescriptor) mockDisabled.proxy());
 
         mockEnabling.expect("enabled");
         manager.init();
         mockEnabling.verify();
 
         mockEnabling.expect("disabled");
         manager.disablePlugin(plugin1.getKey());
         mockEnabling.verify();
     }
 
     /**
     * If a plugin is disabled, any modules that are currently enabled should be sent the disabled
     * message
      */
     public void testDisabledModuleDescriptorsAreEnabled() throws PluginParseException
     {
         plugin1.addModuleDescriptor((ModuleDescriptor) mockEnabling.proxy());
         plugin1.addModuleDescriptor((ModuleDescriptor) mockDisabled.proxy());
         plugin1.setEnabledByDefault(false);
 
         manager.init();
 
        mockDisabled.expect("enabled");
         manager.enablePlugin(plugin1.getKey());
 
         mockEnabling.verify();
         mockDisabled.verify();
     }
 
     private void verifyMocks()
     {
         mockEnabling.verify();
         mockDisabled.verify();
         mockThwarted.verify();
     }
 
     private Mock makeMockModule(Class moduleClass, String pluginKey, String moduleKey, boolean enabledByDefault)
     {
         Mock mock = new Mock(moduleClass);
         mock.matchAndReturn("getKey", moduleKey);
         mock.matchAndReturn("getCompleteKey", pluginKey + ":" + moduleKey);
         mock.matchAndReturn("isEnabledByDefault", enabledByDefault);
         return mock;
     }
 
     private PluginLoader setupPluginLoader(final Plugin plugin1)
     {
         PluginLoader pluginLoader = new PluginLoader() { //TODO: should this loader support removal and addition?
 
             public Collection loadAllPlugins(ModuleDescriptorFactory moduleDescriptorFactory) throws PluginParseException
             {
                 ArrayList list = new ArrayList();
                 list.add(plugin1);
                 return list;
             }
 
             public boolean supportsAddition()
             {
                 return false;
             }
 
             public boolean supportsRemoval()
             {
                 return false;
             }
 
             public Collection removeMissingPlugins()
             {
                 return null;
             }
 
             public Collection addFoundPlugins(ModuleDescriptorFactory moduleDescriptorFactory)
             {
                 return null;
             }
 
             public void removePlugin(Plugin plugin) throws PluginException
             {
                 throw new PluginException("This PluginLoader does not support removal");
             }
         };
         return pluginLoader;
     }
 }
