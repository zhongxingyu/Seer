 package com.feildmaster.lib.configuration;
 
 public abstract class PluginWrapper extends org.bukkit.plugin.java.JavaPlugin {
     private EnhancedConfiguration config;
 
     // This is to reorder Enable to be on top
     public abstract void onEnable();
     public abstract void onDisable();
 
     public EnhancedConfiguration getConfig() {
         if(config == null) {
            config = new EnhancedConfiguration(this);
         }
         return config;
     }
 
     public void reloadConfig() {
        getConfig().load();
     }
 
     public void saveConfig() {
         getConfig().save();
     }
 
     public void saveDefaultConfig() {
         getConfig().saveDefaults();
     }
 }
