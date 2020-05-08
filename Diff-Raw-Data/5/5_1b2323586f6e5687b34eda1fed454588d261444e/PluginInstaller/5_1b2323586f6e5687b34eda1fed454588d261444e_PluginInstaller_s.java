 package ch9k.plugins;
 
 import ch9k.configuration.Storage;
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLClassLoader;
 import java.net.URLConnection;
import java.util.Map;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 import org.apache.log4j.Logger;
 
 /**
  * A class that simplifies the process of installing new plugins.
  */
 public class PluginInstaller extends URLClassLoader {
     /**
      * Logger logger logger
      * Mushroom Mushroom
      */
    private static final Logger logger =
            Logger.getLogger(PluginInstaller.class);
 
     /**
      * Hardcoded for now. Needs to be in Configuration somewhere.
      */
     private static final File INSTALL_DIRECTORY =
             new File(Storage.getStorageDirectory(), "plugins");
 
     /**
      * A reference to the plugin manager.
      */
     private PluginManager pluginManager;
 
     /**
      * Constructor.
      */
     public PluginInstaller(PluginManager pluginManager) {
         /* The superclass is an URLClassLoader with no predefined paths. */
         super(new URL[0]);
 
         /* Store a reference to the plugin manager. */
         this.pluginManager = pluginManager;
 
         /* Create the install directory if it doesn't exist yet. */
         if(!INSTALL_DIRECTORY.isDirectory())
             INSTALL_DIRECTORY.mkdirs();
 
         /* Create a file filter to search for jar's. */
         FileFilter jarFilter = new FileFilter() {
             public boolean accept(File file) {
                 return file.getName().endsWith(".jar");
             }
         };
 
         /* Get all jars in the plugin directory. */
         File[] files = INSTALL_DIRECTORY.listFiles(jarFilter);
 
         /* Add every jar to the classpath. */
         for(File file: files) {
             registerPlugin(file);
         }
     }
 
     /**
      * Register a plugin to the configuration and plugin manager.
      * This file should be located in the application directory already,
      * use installPlugin otherwise.
      * @param file File to register as plugin.
      */
     public void registerPlugin(File file) {
         logger.info("Registering plugin: " + file);
         Manifest manifest = null;
 
         try {
             JarFile jar = new JarFile(file);
             manifest = jar.getManifest();
         } catch (IOException exception) {
             // TODO: Show relevant warning.
             logger.warn(exception.toString());
         }
 
         /* Retreat, retreat! */
         if(manifest == null) return;
 
         /* Obtain the jar manifest and it's attributes. */
         Attributes attributes = manifest.getMainAttributes();
 
         /* Add the jar file to the class path. */
         try {
             addURL(file.toURI().toURL());
         } catch (MalformedURLException exception) {
             // TODO: Show relevant warning.
             return;
         }
 
         /* Find the plugin name .*/
         String pluginName = attributes.getValue("Plugin-Class");
 
         /* No plugin in this jar. */
         if(pluginName == null) return;
 
         /* Register the plugin class. */
         logger.info("Plugin found: " + pluginName);
         pluginManager.addAvailablePlugin(pluginName);
     }
 
     /**
      * Load a plugin class.
      * @param name Name of the class to load.
      * @return The plugin class.
      */
     public Class getPluginClass(String name) throws ClassNotFoundException {
         return findClass(name);
     }
 
     /**
      * Install a plugin from an URL.
      * @param url URL pointing to a plugin jar.
      */
     public void installPlugin(URL url) {
         logger.info("Installing plugin: " + url);
         try {
             /* We take the filename of the url and store the plugin there. */
             URLConnection connection = url.openConnection();
             File file = new File(url.getFile());
             installPlugin(connection.getInputStream(), file.getName());
         } catch (IOException exception) {
             // TODO: Show relevant warning.
             System.out.println(exception);
             return;
         }
     }
 
     /**
      * Install a plugin jar from an input stream. This will close the stream
      * after the read.
      * @param in Stream to get the plugin from.
      * @param fileName File name of the plugin.
      */
     public void installPlugin(InputStream in, String fileName)
             throws IOException {
         /* Open the output file. */
         File file = new File(INSTALL_DIRECTORY, fileName);
         OutputStream out = new FileOutputStream(file);
 
         /* Buffer and buffer length. */
         byte[] buffer = new byte[1024];
         int length;
 
         /* Copy the stream. */
         while((length = in.read(buffer)) > 0) out.write(buffer, 0, length);
 
         /* Close the streams. */
         in.close();
         out.close();
 
         /* Register the new plugin. */
         registerPlugin(file);
     }
 }
