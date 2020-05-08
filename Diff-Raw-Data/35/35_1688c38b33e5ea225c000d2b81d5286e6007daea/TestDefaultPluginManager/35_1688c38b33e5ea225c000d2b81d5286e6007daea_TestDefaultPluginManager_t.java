 package com.atlassian.plugin;
 
 import com.atlassian.plugin.descriptors.MockUnusedModuleDescriptor;
 import com.atlassian.plugin.loaders.SinglePluginLoader;
 import com.atlassian.plugin.loaders.ClassLoadingPluginLoader;
 import com.atlassian.plugin.loaders.classloading.AbstractTestClassLoader;
 import com.atlassian.plugin.mock.MockAnimalModuleDescriptor;
 import com.atlassian.plugin.mock.MockBear;
 import com.atlassian.plugin.mock.MockMineralModuleDescriptor;
 import com.atlassian.plugin.store.MemoryPluginStateStore;
 import com.atlassian.plugin.util.FileUtils;
 import junit.framework.TestCase;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.File;
 
 public class TestDefaultPluginManager extends AbstractTestClassLoader
 {
     private ClassLoadingPluginLoader classLoadingPluginLoader;
 
     protected void tearDown() throws Exception
     {
         if (classLoadingPluginLoader != null)
         {
             classLoadingPluginLoader.shutDown();
         }
 
         super.tearDown();
     }
 
     public void testRetrievePlugins() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
 
         assertEquals(2, manager.getPlugins().size());
         assertEquals(1, manager.getEnabledPlugins().size());
         manager.enablePlugin("test.disabled.plugin");
         assertEquals(2, manager.getEnabledPlugins().size());
     }
 
     public void testEnabledDisabledRetrieval() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("bullshit", MockUnusedModuleDescriptor.class);
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
 
         // check non existant plugins don't show
         assertNull(manager.getPlugin("bull:shit"));
         assertNull(manager.getEnabledPlugin("bull:shit"));
         assertNull(manager.getPluginModule("bull:shit"));
         assertNull(manager.getEnabledPluginModule("bull:shit"));
         assertTrue(manager.getEnabledModuleDescriptorsByClass(TestCase.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByType("bullshit").isEmpty());
         assertTrue(manager.getEnabledModulesByClass(TestCase.class).isEmpty());
 
         final String pluginKey = "test.atlassian.plugin";
         final String moduleKey = pluginKey + ":bear";
 
         // retrieve everything when enabled
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(pluginKey + ":shit"));
         assertFalse(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
         assertFalse(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertEquals(new MockBear(), manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).get(0));
 
         // now only retrieve via always retrieve methods
         manager.disablePlugin(pluginKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(moduleKey));
         assertTrue(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
 
         // now enable again and check back to start
         manager.enablePlugin(pluginKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
         assertFalse(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
 
         // now let's disable the module, but not the plugin
         manager.disablePluginModule(moduleKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNull(manager.getEnabledPluginModule(moduleKey));
         assertTrue(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertTrue(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
 
         // now enable the module again
         manager.enablePluginModule(moduleKey);
         assertNotNull(manager.getPlugin(pluginKey));
         assertNotNull(manager.getEnabledPlugin(pluginKey));
         assertNotNull(manager.getPluginModule(moduleKey));
         assertNotNull(manager.getEnabledPluginModule(moduleKey));
         assertFalse(manager.getEnabledModulesByClass(com.atlassian.plugin.mock.MockBear.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class).isEmpty());
         assertFalse(manager.getEnabledModuleDescriptorsByType("animal").isEmpty());
 
     }
 
     public void testDuplicatePluginKeysAreBad() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
         pluginLoaders.add(new SinglePluginLoader("test-disabled-plugin.xml"));
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         try
         {
             manager.init();
             fail("Should have died with duplicate key exception.");
         }
         catch (PluginParseException e)
         {
             assertEquals("Duplicate plugin key found: 'test.disabled.plugin'", e.getMessage());
         }
     }
 
     public void testGetPluginAndModules() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
 
         Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertNotNull(plugin);
         assertEquals("Test Plugin", plugin.getName());
 
         ModuleDescriptor bear = plugin.getModuleDescriptor("bear");
         assertEquals(bear, manager.getPluginModule("test.atlassian.plugin:bear"));
     }
 
     public void testGetModuleByModuleClassOneFound() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
 
         Collection descriptors = manager.getEnabledModuleDescriptorsByClass(MockAnimalModuleDescriptor.class);
         assertNotNull(descriptors);
         assertEquals(1, descriptors.size());
         ModuleDescriptor moduleDescriptor = (ModuleDescriptor) descriptors.iterator().next();
         assertEquals("Bear Animal", moduleDescriptor.getName());
 
         descriptors = manager.getEnabledModuleDescriptorsByClass(MockMineralModuleDescriptor.class);
         assertNotNull(descriptors);
         assertEquals(1, descriptors.size());
         moduleDescriptor = (ModuleDescriptor) descriptors.iterator().next();
         assertEquals("Bar", moduleDescriptor.getName());
     }
 
     public void testGetModuleByModuleClassNoneFound() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
 
         final Collection descriptors = manager.getEnabledModulesByClass(java.lang.String.class);
         assertNotNull(descriptors);
         assertTrue(descriptors.isEmpty());
     }
 
     public void testGetModuleDescriptorsByType() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
 
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
 
         Collection descriptors = manager.getEnabledModuleDescriptorsByType("animal");
         assertNotNull(descriptors);
         assertEquals(1, descriptors.size());
         ModuleDescriptor moduleDescriptor = (ModuleDescriptor) descriptors.iterator().next();
         assertEquals("Bear Animal", moduleDescriptor.getName());
 
         descriptors = manager.getEnabledModuleDescriptorsByType("mineral");
         assertNotNull(descriptors);
         assertEquals(1, descriptors.size());
         moduleDescriptor = (ModuleDescriptor) descriptors.iterator().next();
         assertEquals("Bar", moduleDescriptor.getName());
 
         try
         {
             manager.getEnabledModuleDescriptorsByType("foobar");
             fail("Should have thrown exception.");
         }
         catch (IllegalArgumentException e)
         {
         }
     }
 
     public void testRetrievingDynamicResources() throws PluginParseException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         PluginManager manager = makeClassLoadingPluginManager();
 
         InputStream is = manager.getDynamicResourceAsStream("atlassian-plugin.xml");
         assertNotNull(is);
     }
 
     public void testFindingNewPlugins() throws PluginParseException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         //delete paddington for the timebeing
         File paddington = new File(pluginsTestDir, PADDINGTON_JAR);
         paddington.delete();
 
         PluginManager manager = makeClassLoadingPluginManager();
 
         assertEquals(1, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded2"));
 
         //restore paddington to test plugins dir
         FileUtils.copyDirectory(pluginsDirectory, pluginsTestDir);
 
        manager.scanForNewPlugins();
         assertEquals(2, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded2"));
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded"));
 
        manager.scanForNewPlugins();
         assertEquals(2, manager.getPlugins().size());
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded2"));
         assertNotNull(manager.getPlugin("test.atlassian.plugin.classloaded"));
     }
 
     private PluginManager makeClassLoadingPluginManager() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         classLoadingPluginLoader = new ClassLoadingPluginLoader(pluginsTestDir);
         pluginLoaders.add(classLoadingPluginLoader);
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
         return manager;
     }
 
     public void testRemovingPlugins() throws PluginException, IOException
     {
         createFillAndCleanTempPluginDirectory();
 
         PluginManager manager = makeClassLoadingPluginManager();
         assertEquals(2, manager.getPlugins().size());
         manager.uninstall(manager.getPlugin("test.atlassian.plugin.classloaded"));
         assertEquals(1, manager.getPlugins().size());
         assertNull(manager.getPlugin("test.atlassian.plugin.classloaded"));
         assertEquals(1, pluginsTestDir.listFiles().length);
     }
 
     public void testNonRemovablePlugins() throws PluginParseException
     {
         List pluginLoaders = new ArrayList();
         pluginLoaders.add(new SinglePluginLoader("test-atlassian-plugin.xml"));
         DefaultModuleDescriptorFactory moduleDescriptorFactory = new DefaultModuleDescriptorFactory();
         moduleDescriptorFactory.addModuleDescriptor("animal", MockAnimalModuleDescriptor.class);
         moduleDescriptorFactory.addModuleDescriptor("mineral", MockMineralModuleDescriptor.class);
 
         PluginManager manager = new DefaultPluginManager(new MemoryPluginStateStore(), pluginLoaders, moduleDescriptorFactory);
         manager.init();
 
         Plugin plugin = manager.getPlugin("test.atlassian.plugin");
         assertFalse(plugin.isUninstallable());
         assertFalse(plugin.isResourceLoading());
         assertNull(plugin.getResourceAsStream("anything"));
 
         try
         {
             manager.uninstall(plugin);
             fail("Where was the exception?");
         }
         catch (PluginException p)
         {
         }
     }
 
 }
