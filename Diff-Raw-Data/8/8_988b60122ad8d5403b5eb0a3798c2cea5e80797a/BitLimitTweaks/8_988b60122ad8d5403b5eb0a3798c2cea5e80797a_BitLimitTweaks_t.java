 package com.kolinkrewinkel.BitLimitTweaks;
 
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.Server;
 import java.util.*;
 
 public class BitLimitTweaks extends JavaPlugin {
 
     @Override
     public void onEnable() {
         new BitLimitTweaksListener(this);
 
         this.getCommand("tweaks").setExecutor(new TweaksCommandExecutor(this));
 
         class BitLimitRecurringTask implements Runnable {
             Plugin plugin;
             
             BitLimitRecurringTask(Plugin p) {
                 plugin = p;
             }
 
             public void run() {
                 World world = plugin.getServer().getWorld(plugin.getConfig().getString("world"));
                 if (!world.hasStorm()) {
                     this.plugin.getServer().broadcastMessage(ChatColor.GREEN + "Rain decremented!");
                     int weatherDuration = world.getWeatherDuration();
                    this.plugin.getServer().broadcastMessage(ChatColor.RED + Integer.toString(world.getWeatherDuration()));
                     world.setWeatherDuration(weatherDuration + 6000);
                    this.plugin.getServer().broadcastMessage(ChatColor.RED + Integer.toString(world.getWeatherDuration()));
                 } else {
                     this.plugin.getServer().broadcastMessage(ChatColor.RED + "Timer untouched, it's raining!");
                    this.plugin.getServer().broadcastMessage(ChatColor.RED + Integer.toString(world.getWeatherDuration()));
                 }
             }
         }
         this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new BitLimitRecurringTask(this), 0L, 1200L);
     }
 
     @Override
     public void onDisable() {        
         // save the configuration file, if there are no values, write the defaults.
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
     }
 }
 
