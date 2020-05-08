 package uk.co.mdjcox.sagetvcatchup.plugins;
 
 import com.google.inject.Inject;
 import com.google.inject.Singleton;
 import uk.co.mdjcox.logger.LoggerInterface;
 import uk.co.mdjcox.utils.PropertiesInterface;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.Collection;
 import java.util.LinkedHashMap;
 
 /**
  * Created with IntelliJ IDEA.
  * User: michael
  * Date: 05/04/13
  * Time: 18:41
  * To change this template use File | Settings | File Templates.
  */
 @Singleton
 public class PluginManager {
 
     private LoggerInterface logger;
     private PropertiesInterface props;
     private LinkedHashMap<String, Plugin> plugins = new  LinkedHashMap<String, Plugin>();
     @Inject
     private PluginFactory pluginFactory;
 
     @Inject
     private PluginManager(LoggerInterface logger, PropertiesInterface props) {
         this.logger = logger;
         this.props = props;
     }
 
     public Collection<Plugin> getPlugins() {
         return plugins.values();
     }
 
     public Plugin getPlugin(String sourceId) {
         return plugins.get(sourceId);
     }
 
     public void load() {
         String base = System.getProperty("user.dir");
        base = base + File.separator + "plugins" + File.separator;
 
         File dir = new File(base);
         if (!dir.isDirectory()) {
             throw new RuntimeException("Plugin directory " + base + " is not a directory");
         }
 
         if (!dir.exists()) {
             throw new RuntimeException("Plugin directory " + base + " does not exist");
         }
 
         File[] pluginDirs = dir.listFiles(new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return pathname.isDirectory();
             }
         });
 
         for (File pluginDir : pluginDirs) {
             String sourceId = pluginDir.getName();
             if (!sourceId.equals("Iplayer")) continue; // TODO take out
             Plugin plugin = pluginFactory.createPlugin(sourceId, pluginDir.getAbsolutePath());
             plugin.init();
             plugins.put(sourceId, plugin);
         }
     }
 }
