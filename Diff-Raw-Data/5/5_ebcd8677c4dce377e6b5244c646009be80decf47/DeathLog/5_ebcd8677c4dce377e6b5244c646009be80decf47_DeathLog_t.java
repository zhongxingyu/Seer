 package com.djdch.bukkit.deathlog;
 
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.djdch.bukkit.deathlog.listener.DeathListener;
 import com.djdch.bukkit.util.Logger;
 
 /**
  * Main class of the <b>DeathLog</b> plugin for Bukkit.
  * <p>
  * Show death messages in the server console/log.
  * 
  * @author DjDCH
  */
 public class DeathLog extends JavaPlugin {
     /**
      * Contains the Logger instance.
      */
     protected final Logger logger = new Logger();
 
     /**
      * Contains the deathListener instance.
      */
     protected final DeathListener deathListener = new DeathListener(this);
 
     /**
      * Method execute when the plugin is enable.
      */
     public void onEnable() {
         this.logger.setName(getDescription().getName());
 
         // Register the plugin events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.ENTITY_DEATH, deathListener, Event.Priority.Monitor, this);
 
        logger.info("Version " + getDescription().getVersion() + " enable");
     }
 
     /**
      * Method execute when the plugin is disable.
      */
     public void onDisable() {
        logger.info("Version " + getDescription().getVersion() + " disable");
     }
 
     /**
      * Accessor who return the logger instance.
      * 
      * @return Logger instance.
      */
     public Logger getLogger() {
         return this.logger;
     }
 }
