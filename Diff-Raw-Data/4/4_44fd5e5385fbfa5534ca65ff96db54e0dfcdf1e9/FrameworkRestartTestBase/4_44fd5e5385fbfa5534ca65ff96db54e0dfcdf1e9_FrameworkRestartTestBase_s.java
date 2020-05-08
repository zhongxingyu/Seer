 package com.atlassian.plugin.osgi.performance;
 
 import com.atlassian.plugin.DefaultModuleDescriptorFactory;
 import com.atlassian.plugin.hostcontainer.DefaultHostContainer;
 import com.atlassian.plugin.osgi.hostcomponents.ComponentRegistrar;
 import com.atlassian.plugin.osgi.hostcomponents.HostComponentProvider;
 import com.atlassian.plugin.osgi.PluginInContainerTestBase;
 import com.atlassian.plugin.osgi.DummyModuleDescriptor;
 import com.atlassian.plugin.osgi.SomeInterface;
 import com.atlassian.plugin.test.PluginJarBuilder;
 
 import org.apache.commons.io.FileUtils;
 
 import java.io.IOException;
 import java.io.File;
 
 /**
  * Tests the plugin framework handling restarts correctly
  */
 public abstract class FrameworkRestartTestBase extends PluginInContainerTestBase
 {
     private static final int NUM_HOST_COMPONENTS = 200;
     private static final int NUM_PLUGINS = 50;
     HostComponentProvider prov = null;
     DefaultModuleDescriptorFactory factory = null;
 
     @Override
     public void setUp() throws Exception
     {
         super.setUp();
         factory = new DefaultModuleDescriptorFactory(new DefaultHostContainer());
         factory.addModuleDescriptor("dummy", DummyModuleDescriptor.class);
         prov = new HostComponentProvider()
         {
             public void provide(final ComponentRegistrar registrar)
             {
                 for (int x = 0; x < NUM_HOST_COMPONENTS; x++)
                 {
                     registrar.register(SomeInterface.class).forInstance(new SomeInterface()
                     {});
                 }
             }
         };
 
         for (int x = 0; x < NUM_PLUGINS; x++)
         {
             addPlugin(pluginsDir, x);
         }
 
 
         // warm up the cache
         startPluginFramework();
         pluginManager.shutdown();
     }
 
     protected abstract void addPlugin(File dir, int x) throws IOException;
 
     protected void startPluginFramework() throws Exception
     {
         initPluginManager(prov, factory);
     }
 
     @Override
     public void tearDown() throws Exception
     {
         super.tearDown();
     }
 
     public void testMultiplePlugins() throws Exception
     {
         for (int x=0; x<5; x++)
         {
        startPluginFramework();
        pluginManager.shutdown();
         }
     }
 }
