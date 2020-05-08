 package com.atlassian.plugin.loaders.classloading;
 
 import com.atlassian.plugin.mock.MockBear;
import com.atlassian.plugin.util.ClassLoaderUtils;
import com.atlassian.plugin.loaders.TestClassPathPluginLoader;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Enumeration;
 
 public class TestJarClassLoader extends AbstractTestClassLoader
 {
     /*
      * Here we try to load a specific JAR (paddington-test-plugin.jar) and get the descriptor out of it.
      */
     public void testLoader() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
     {
         // hacky way of getting to the paddington-test-plugin.jar (assume it's two levels below src/test/etc/test-disabled-plugin.xml)
        JarClassLoader loader = makeClassLoaderForPluginJarFile("paddington-test-plugin.jar");
 
         // now make sure we only got one descriptor back
         Enumeration descriptors = loader.findResources("atlassian-plugin.xml");
         assertTrue(descriptors.hasMoreElements());
         URL descriptor = (URL) descriptors.nextElement();
         assertTrue(descriptor.toExternalForm().endsWith("paddington-test-plugin.jar!atlassian-plugin.xml"));
         assertFalse(descriptors.hasMoreElements());
 
         // now try to load the plugin class and cast it to something in the global class path
         Class paddingtonClass = loader.findClass("com.atlassian.plugin.mock.MockPaddington");
         MockBear paddington = (MockBear) paddingtonClass.newInstance();
 
         // check we really did get the right class
         assertEquals("com.atlassian.plugin.mock.MockPaddington", paddington.getClass().getName());
     }
 
     /*
      * Load a jar containing a package we haven't seen before, and make sure the package is defined. (PLUG-27)
      */
     public void testLoaderWithUnknownPackage() throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException
     {
         // hacky way of getting to the paddington-test-plugin.jar (assume it's two levels below src/test/etc/test-disabled-plugin.xml)
        JarClassLoader loader = makeClassLoaderForTestJarFile("atlassian-plugins-simpletest-1.0.jar");
 
         // now try to load the plugin class and cast it to something in the global class path
         Class testClassOne = loader.findClass("com.atlassian.plugin.simpletest.TestClassOne");
 
         assertEquals("com.atlassian.plugin.simpletest", testClassOne.getPackage().getName());
     }
 
     public void testLoadingNestedClasses() throws Exception
     {
         String jarFileName = "atlassian-plugins-innertest-1.0.jar";
         // hacky way of getting to the paddington-test-plugin.jar (assume it's two levels below src/test/etc/test-disabled-plugin.xml)
        JarClassLoader loader = makeClassLoaderForTestJarFile(jarFileName);
 
         // Try to load classes from each of the different inner jars.
         testCanLoad(loader, "com.atlassian.plugin.innerjarone", "TestClassOne");
         testCanLoad(loader, "com.atlassian.plugin.innerjartwo", "TestClassTwo");
     }
 
    private JarClassLoader makeClassLoaderForPluginJarFile(String jarFileName)
     {
         File pluginsDirectory = getPluginsDirectory();
         File pluginJar = new File(pluginsDirectory, jarFileName);
 
         // make the JAR loader
         return new JarClassLoader(pluginJar, this.getClass().getClassLoader());
     }
    protected File getTestJarsDirectory()
    {
        URL url = ClassLoaderUtils.getResource("testjars", TestClassPathPluginLoader.class);
        String path = url.toExternalForm().substring(5);
    	path = path.replace('/', File.separatorChar);
        pluginsDirectory = new File(path);
        System.out.println("path = " + path);
        return pluginsDirectory;
    }

    private JarClassLoader makeClassLoaderForTestJarFile(String jarFileName)
    {
        File pluginsDirectory = getTestJarsDirectory();
        File pluginJar = new File(pluginsDirectory, jarFileName);

        // make the JAR loader
        return new JarClassLoader(pluginJar, this.getClass().getClassLoader());
    }
 
     private void testCanLoad(JarClassLoader loader, String packageName, String className)
             throws ClassNotFoundException, InstantiationException, IllegalAccessException
     {
         Class testClassOneFromInnerJar = loader.findClass(packageName + "." + className);
         assertEquals(packageName, testClassOneFromInnerJar.getPackage().getName());
 
         // Make sure it doesn't blow up when we try to do something with it.
         assertNotNull(testClassOneFromInnerJar.newInstance().toString());
     }
 }
