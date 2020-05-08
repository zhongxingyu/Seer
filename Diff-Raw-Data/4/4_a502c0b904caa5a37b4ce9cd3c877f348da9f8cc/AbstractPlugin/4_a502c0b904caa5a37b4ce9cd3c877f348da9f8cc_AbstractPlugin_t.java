 package com.atlassian.plugin.impl;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
 
 import com.atlassian.plugin.*;
 import com.atlassian.plugin.elements.ResourceDescriptor;
 import com.atlassian.plugin.elements.ResourceLocation;
 import com.atlassian.plugin.util.VersionStringComparator;
 
 public abstract class AbstractPlugin implements Plugin, Comparable<Plugin>
 {
     private String name;
     private String i18nNameKey;
     private String key;
    private Map<String,ModuleDescriptor<?>> modules = new ConcurrentHashMap<String,ModuleDescriptor<?>>();
     private boolean enabledByDefault = true;
     private PluginInformation pluginInformation = new PluginInformation();
     private boolean enabled;
     private boolean system;
     private Resourced resources = Resources.EMPTY_RESOURCES;
     private int pluginsVersion = 1;
     private Date dateLoaded = new Date();
 
     public String getName()
     {
         return name;
     }
 
     public void setName(String name)
     {
         this.name = name;
     }
 
     public String getI18nNameKey()
     {
         return i18nNameKey;
     }
 
     public void setI18nNameKey(String i18nNameKey)
     {
         this.i18nNameKey = i18nNameKey;
     }
 
     public String getKey()
     {
         return key;
     }
 
     public void setKey(String aPackage)
     {
         this.key = aPackage;
     }
 
     public void addModuleDescriptor(ModuleDescriptor<?> moduleDescriptor)
     {
         modules.put(moduleDescriptor.getKey(), moduleDescriptor);
     }
 
     /**
      * Returns a copy of the module descriptors for this plugin
      * @return A copy of the internal list
      */
     public Collection<ModuleDescriptor<?>> getModuleDescriptors()
     {
         return new ArrayList<ModuleDescriptor<?>>(modules.values());
     }
 
     public ModuleDescriptor<?> getModuleDescriptor(String key)
     {
         return modules.get(key);
     }
 
     public <T> List<ModuleDescriptor<T>> getModuleDescriptorsByModuleClass(Class<T> aClass)
     {
         List<ModuleDescriptor<T>> result = new ArrayList<ModuleDescriptor<T>>();
 
         for (ModuleDescriptor moduleDescriptor : modules.values())
         {
             Class moduleClass = moduleDescriptor.getModuleClass();
             if (aClass.isAssignableFrom(moduleClass))
             {
                 result.add((ModuleDescriptor<T>) moduleDescriptor);
             }
         }
     
         return result;
     }
 
     public boolean isEnabledByDefault()
     {
         return enabledByDefault && (pluginInformation == null || pluginInformation.satisfiesMinJavaVersion());
     }
 
     public void setEnabledByDefault(boolean enabledByDefault)
     {
         this.enabledByDefault = enabledByDefault;
     }
 
     public int getPluginsVersion()
     {
         return pluginsVersion;
     }
 
     public void setPluginsVersion(int pluginsVersion)
     {
         this.pluginsVersion = pluginsVersion;
     }
 
     public PluginInformation getPluginInformation()
     {
         return pluginInformation;
     }
 
     public void setPluginInformation(PluginInformation pluginInformation)
     {
         this.pluginInformation = pluginInformation;
     }
 
     public void setResources(Resourced resources)
     {
         this.resources = resources != null ? resources : Resources.EMPTY_RESOURCES;
     }
 
     public List getResourceDescriptors()
     {
         return resources.getResourceDescriptors();
     }
 
     public List getResourceDescriptors(String type)
     {
         return resources.getResourceDescriptors(type);
     }
 
     public ResourceLocation getResourceLocation(String type, String name)
     {
         return resources.getResourceLocation(type, name);
     }
 
     /**
      * @deprecated
      */
     public ResourceDescriptor getResourceDescriptor(String type, String name)
     {
         return resources.getResourceDescriptor(type, name);
     }
 
     /**
      * @return true if the plugin has been enabled
      */
     public boolean isEnabled()
     {
         return enabled;
     }
 
     /**
      * Setter for the enabled state of a plugin. If this is set to false then the plugin will not execute.
      */
     public void setEnabled(boolean enabled)
     {
         this.enabled = enabled;
     }
 
     public boolean isSystemPlugin()
     {
         return system;
     }
 
     public boolean containsSystemModule()
     {
         for (ModuleDescriptor moduleDescriptor : modules.values())
         {
             if(moduleDescriptor.isSystemModule())
             {
                 return true;
             }
         }
         return false;
     }
 
     public void setSystemPlugin(boolean system)
     {
         this.system = system;
     }
 
     public Date getDateLoaded()
     {
         return dateLoaded;
     }
 
     /**
      * Plugins with the same key are compared by version number, using {@link VersionStringComparator}.
      * If the other plugin has a different key, this method returns <tt>1</tt>.
      *
      * @return <tt>-1</tt> if the other plugin is newer, <tt>0</tt> if equal,
      * <tt>1</tt> if the other plugin is older or has a different plugin key.
      */
     public int compareTo(Plugin otherPlugin)
     {
         // If the compared plugin doesn't have the same key, the current object is greater
         if (!otherPlugin.getKey().equals(this.getKey())) return getKey().compareTo(otherPlugin.getKey());
     
         String thisVersion = cleanVersionString(this.getPluginInformation().getVersion());
         String otherVersion = cleanVersionString(otherPlugin.getPluginInformation().getVersion());
     
         if (!VersionStringComparator.isValidVersionString(thisVersion)) return -1;
         if (!VersionStringComparator.isValidVersionString(otherVersion)) return -1;
     
         return new VersionStringComparator().compare(thisVersion, otherVersion);
     }
 
     private String cleanVersionString(String version)
     {
         if (version == null || version.trim().equals("")) return "0";
         return version.replaceAll(" ", "");
     }
 
     public String toString()
     {
         final PluginInformation info = getPluginInformation();
         return getKey() + ":" + (info == null ? "?" : info.getVersion());
     }
 }
