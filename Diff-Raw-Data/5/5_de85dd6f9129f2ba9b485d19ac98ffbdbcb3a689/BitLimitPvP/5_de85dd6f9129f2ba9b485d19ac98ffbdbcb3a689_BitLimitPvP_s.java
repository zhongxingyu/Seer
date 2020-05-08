 package com.kolinkrewinkel.BitLimitPvP;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 /*
  * This is the main class of the sample plug-in
  */
 public class BitLimitPvP extends JavaPlugin {
     /*
      * This is called when your plug-in is enabled
      */
     @Override
     public void onEnable() {
         // Create the SampleListener
        new SampleListener(this);
         
         // set the command executor for sample
        this.getCommand("BitLimitPvP").setExecutor(new SampleCommandExecutor(this));
     }
     
     /*
      * This is called when your plug-in shuts down
      */
     @Override
     public void onDisable() {        
         // save the configuration file, if there are no values, write the defaults.
         this.getConfig().options().copyDefaults(true);
         this.saveConfig();
     }
 }
