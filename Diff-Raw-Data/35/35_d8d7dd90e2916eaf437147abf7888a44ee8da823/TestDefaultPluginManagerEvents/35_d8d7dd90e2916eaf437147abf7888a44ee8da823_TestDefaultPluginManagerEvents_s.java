 package com.atlassian.plugin.manager;
 
 import com.atlassian.plugin.mock.MockVegetableModuleDescriptor;
 import junit.framework.TestCase;
 import com.atlassian.plugin.loaders.PluginLoader;
 import com.atlassian.plugin.loaders.SinglePluginLoader;
 import com.atlassian.plugin.loaders.DirectoryPluginLoader;
 import com.atlassian.plugin.loaders.classloading.DirectoryPluginLoaderUtils;
 import com.atlassian.plugin.event.PluginEventManager;
 import com.atlassian.plugin.event.events.PluginEnabledEvent;
 import com.atlassian.plugin.event.events.PluginDisabledEvent;
 import com.atlassian.plugin.event.events.PluginModuleEnabledEvent;
 import com.atlassian.plugin.event.events.PluginModuleDisabledEvent;
 import com.atlassian.plugin.event.impl.DefaultPluginEventManager;
 import com.atlassian.plugin.manager.store.MemoryPluginPersistentStateStore;
 import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
 import com.atlassian.plugin.mock.MockAnimalModuleDescriptor;
 import com.atlassian.plugin.mock.MockMineralModuleDescriptor;
 import com.atlassian.plugin.event.listeners.RecordingListener;
 import com.atlassian.plugin.factories.LegacyDynamicPluginFactory;
 import com.atlassian.plugin.factories.XmlDynamicPluginFactory;
 import com.atlassian.plugin.repositories.FilePluginInstaller;
 import com.atlassian.plugin.*;
 
 import java.util.List;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.io.File;
 
 public class TestDefaultPluginManagerEvents extends TestCase
 {
     private DefaultPluginManager manager;
     private RecordingListener listener;
 
     @Override
     protected void setUp() throws Exception
     {
         super.setUp();
 
         listener = new RecordingListener(
             PluginEnabledEvent.class,
             PluginDisabledEvent.class,
             PluginModuleEnabledEvent.class,
             PluginModuleDisabledEvent.class);
 
         manager = buildPluginManager(listener);
         manager.init();
         listener.reset();
     }
 
     private DefaultPluginManager buildPluginManager(RecordingListener listener) throws Exception
     {
         PluginEventManager pluginEventManager = new DefaultPluginEventManager();
         pluginEventManager.register(listener);
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory(new DefaultHostContainer());
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("vegetable", MockVegetableModuleDescriptor.class);
 
         File pluginTempDirectory = DirectoryPluginLoaderUtils.copyTestPluginsToTempDirectory();
         List<PluginLoader> pluginLoaders = buildPluginLoaders(pluginEventManager, pluginTempDirectory);
 
         DefaultPluginManager manager = new DefaultPluginManager(new MemoryPluginPersistentStateStore(), pluginLoaders,
             moduleDescriptorFactory, pluginEventManager);
         manager.setPluginInstaller(new FilePluginInstaller(pluginTempDirectory));
 
         return manager;
     }
 
     private List<PluginLoader> buildPluginLoaders(PluginEventManager pluginEventManager, File pluginTempDirectory)
     {
         List<PluginLoader> pluginLoaders = new ArrayList<PluginLoader>();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
         DirectoryPluginLoader directoryPluginLoader = new DirectoryPluginLoader(
             pluginTempDirectory,
             Arrays.asList(new LegacyDynamicPluginFactory(PluginAccessor.Descriptor.FILENAME),
                 new XmlDynamicPluginFactory("foo")),
             pluginEventManager);
         pluginLoaders.add(directoryPluginLoader);
         return pluginLoaders;
     }
 
     @Override
     protected void tearDown() throws Exception
     {
         // prevent resources being used until end of all tests
         manager = null;
         listener = null;
         super.tearDown();
     }
 
     public void testInitialisationEvents() throws Exception
     {
         DefaultPluginManager manager = buildPluginManager(listener);
         manager.init();
 
         assertListEquals(listener.getEventClasses(),
             PluginModuleEnabledEvent.class,
             PluginModuleEnabledEvent.class,
             PluginModuleEnabledEvent.class,
             PluginEnabledEvent.class,
             PluginModuleEnabledEvent.class,
             PluginEnabledEvent.class,
             PluginModuleEnabledEvent.class,
             PluginEnabledEvent.class);
         assertListEquals(listener.getEventPluginOrModuleKeys(),
            "test.atlassian.plugin:bear",
             "test.atlassian.plugin:gold",
             "test.atlassian.plugin:veg",
             "test.atlassian.plugin",
             "test.atlassian.plugin.classloaded:paddington",
             "test.atlassian.plugin.classloaded",
             "test.atlassian.plugin.classloaded2:pooh",
            "test.atlassian.plugin.classloaded2");
     }
 
     public void testDisablePlugin() throws Exception
     {
         manager.disablePlugin("test.atlassian.plugin");
 
         assertListEquals(listener.getEventClasses(),
             PluginModuleDisabledEvent.class,
             PluginModuleDisabledEvent.class,
             PluginModuleDisabledEvent.class,
             PluginDisabledEvent.class);
         assertListEquals(listener.getEventPluginOrModuleKeys(),
             "test.atlassian.plugin:veg",  // a  module that can't be individually disabled can still be disabled with the plugin
             "test.atlassian.plugin:gold", // modules in reverse order to enable
             "test.atlassian.plugin:bear",
             "test.atlassian.plugin");
     }
 
     public void testEnablePlugin() throws Exception
     {
         manager.disablePlugin("test.atlassian.plugin");
         listener.reset();
         manager.enablePlugins("test.atlassian.plugin");
 
         assertListEquals(listener.getEventClasses(),
             PluginModuleEnabledEvent.class,
             PluginModuleEnabledEvent.class,
             PluginModuleEnabledEvent.class,
             PluginEnabledEvent.class);
         assertListEquals(listener.getEventPluginOrModuleKeys(),
             "test.atlassian.plugin:bear",
             "test.atlassian.plugin:gold",
             "test.atlassian.plugin:veg",
             "test.atlassian.plugin");
     }
 
     public void testEnableDisabledByDefaultPlugin() throws Exception
     {
         manager.enablePlugin("test.disabled.plugin");
 
         assertListEquals(listener.getEventClasses(), PluginEnabledEvent.class);
         assertListEquals(listener.getEventPluginOrModuleKeys(), "test.disabled.plugin");
 
         listener.reset();
         manager.enablePluginModule("test.disabled.plugin:gold");
 
         assertListEquals(listener.getEventClasses(), PluginModuleEnabledEvent.class);
         assertListEquals(listener.getEventPluginOrModuleKeys(), "test.disabled.plugin:gold");
     }
 
     public void testDisableModule() throws Exception
     {
         manager.disablePluginModule("test.atlassian.plugin:bear");
 
         assertListEquals(listener.getEventClasses(), PluginModuleDisabledEvent.class);
         assertListEquals(listener.getEventPluginOrModuleKeys(), "test.atlassian.plugin:bear");
     }
 
     public void testDisableModuleWithCannotDisableDoesNotFireEvent() throws Exception
     {
         manager.disablePluginModule("test.atlassian.plugin:veg");
         assertEquals(listener.getEventClasses().size(), 0);    
     }
 
     public void testEnableModule() throws Exception
     {
         manager.disablePluginModule("test.atlassian.plugin:bear");
         listener.reset();
         manager.enablePluginModule("test.atlassian.plugin:bear");
 
         assertListEquals(listener.getEventClasses(), PluginModuleEnabledEvent.class);
         assertListEquals(listener.getEventPluginOrModuleKeys(), "test.atlassian.plugin:bear");
     }
 
     public void testInstallPlugin() throws Exception
     {
         // have to uninstall one of the directory plugins
         manager.uninstall(manager.getPlugin("test.atlassian.plugin.classloaded2"));
         listener.reset();
         File pluginJar = new File(DirectoryPluginLoaderUtils.getTestPluginsDirectory(),
              "pooh-test-plugin.jar");
         manager.installPlugin(new JarPluginArtifact(pluginJar));
 
         assertListEquals(listener.getEventClasses(),
             PluginModuleEnabledEvent.class,
             PluginEnabledEvent.class);
     }
 
     public void testUninstallPlugin() throws Exception
     {
         // have to uninstall one of the directory plugins
         manager.uninstall(manager.getPlugin("test.atlassian.plugin.classloaded2"));
 
         assertListEquals(listener.getEventClasses(),
             PluginModuleDisabledEvent.class,
             PluginDisabledEvent.class);
     }
 
     // yeah, the expected values should come first in jUnit, but varargs are so convenient...
     private static void assertListEquals(List actual, Object... expected)
     {
         String message = "Expected list was: " + Arrays.toString(expected) + ", " +
             "but actual was: " + actual;
         assertEquals(message, expected.length, actual.size());
         for (int i=0; i<actual.size(); i++)
         {
             assertEquals(message, expected[i], actual.get(i));
         }
     }
 }
