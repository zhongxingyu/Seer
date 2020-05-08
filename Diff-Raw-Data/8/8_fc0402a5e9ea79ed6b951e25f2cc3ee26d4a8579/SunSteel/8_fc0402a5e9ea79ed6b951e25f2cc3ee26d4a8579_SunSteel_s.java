 package com.acyrid.SunSteel;
 
 import com.acyrid.SunSteel.listeners.*;
 import com.acyrid.SunSteel.utils.SSMechanics;
 import org.bukkit.Bukkit;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import java.io.File;
 import java.util.logging.Level;
 
 public class SunSteel extends JavaPlugin{
 
     private SSPlayerListener playerListener = new SSPlayerListener(this);
     private SSDamageListener damageListener = new SSDamageListener(this);
     private SSBlockListener blockListener = new SSBlockListener(this);
     private SSEntityListener entityListener = new SSEntityListener(this);
 
     public void onDisable(){
        Bukkit.getLogger().log(Level.INFO, "Disabled!");
     }                                                    
     public void onEnable(){
         if(!new File(this.getDataFolder(), "config.yml").exists()){
             saveDefaultConfig();
         }
         getConfig();
         this.registerEvents();
         SSMechanics.init(this);
        Bukkit.getLogger().log(Level.INFO, "Disabled!");
         getConfig().getKeys(true);
     }
 
     private void registerEvents() {
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(this.playerListener, this);
         pm.registerEvents(this.blockListener, this);
         pm.registerEvents(this.damageListener, this);
         pm.registerEvents(this.entityListener, this);
 
     }
 
 
 }
