 package com.atlassian.plugin.webresource;
 
 import com.atlassian.plugin.util.PluginUtils;
 
 import java.util.List;
 import java.util.Collections;
 
 /**
  * Default configuration for the plugin resource locator, for those applications that do not want to perform
  * any super-batching.
  */
 public class DefaultResourceBatchingConfiguration implements ResourceBatchingConfiguration
 {
     public static final String PLUGIN_WEBRESOURCE_BATCHING_OFF = "plugin.webresource.batching.off";
 
     public boolean isSuperBatchingEnabled()
     {
         return false;
     }
 
     public List<String> getSuperBatchModuleCompleteKeys()
     {
         return Collections.emptyList();
     }
 
     public boolean isContextBatchingEnabled()
     {
         return false;
     }
 
     public boolean isPluginWebResourceBatchingEnabled()
     {
         final String explicitSetting = System.getProperty(PLUGIN_WEBRESOURCE_BATCHING_OFF);
         if (explicitSetting != null)
         {
            return Boolean.parseBoolean(explicitSetting);
         }
         else
         {
            return Boolean.parseBoolean(System.getProperty(PluginUtils.ATLASSIAN_DEV_MODE));
         }
     }
 }
