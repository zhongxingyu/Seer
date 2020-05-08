 package org.wickedsource.hooked.svn;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.wickedsource.hooked.plugins.api.Plugin;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.util.Properties;
 
 /**
  * @author Tom Hombergs <tom.hombergs@gmail.com>
  */
 public class PluginPropertiesLoader {
 
     public static final String SYSTEM_PROPERTY_CONFIG_PATH = "hooked.configPath";
 
     private static final Logger logger = LoggerFactory.getLogger(PluginPropertiesLoader.class);
 
     public Properties loadPluginProperties(Class<? extends Plugin> pluginClass) {
         if (logger.isTraceEnabled()) {
             logger.trace(String.format("Loading properties for plugin %s...", pluginClass.getSimpleName()));
         }
 
         String configPath = getConfigPath();
        String propertyFileName = configPath + File.separator + pluginClass.getSimpleName() + ".properties";
 
         File propertyFile = new File(propertyFileName);
         if (!propertyFile.exists()) {
             if (logger.isTraceEnabled()) {
                 logger.trace(String.format("Property file %s not found. Configuring plugin %s with empty properties" +
                         ".", propertyFile.getAbsolutePath(), pluginClass.getSimpleName()));
             }
             return new Properties();
         } else {
             try {
                 if (logger.isTraceEnabled()) {
                     logger.trace(String.format("Loading properties from file %s", propertyFile.getAbsolutePath()));
                 }
                 Properties p = new Properties();
                 p.load(new FileInputStream(propertyFile));
                 return p;
             } catch (IOException e) {
                 throw new RuntimeException("Error loading properties file!", e);
             }
         }
     }
 
     private String getConfigPath() {
         String configPath = System.getProperty(SYSTEM_PROPERTY_CONFIG_PATH);
         if (configPath == null || "".equals(configPath)) {
             throw new RuntimeException(String.format("System property %s is not set. It should point to the conf " +
                     "folder of the SVN hook.", SYSTEM_PROPERTY_CONFIG_PATH));
         }
         return configPath;
     }
 
 }
