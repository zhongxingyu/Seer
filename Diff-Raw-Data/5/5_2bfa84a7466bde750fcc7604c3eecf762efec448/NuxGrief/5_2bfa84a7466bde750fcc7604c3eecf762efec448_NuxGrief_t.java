 package net.n4th4.bukkit.nuxgrief;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NuxGrief extends JavaPlugin {
     private final NGPlayerListener playerListener = new NGPlayerListener(this);
     private final NGEntityListener entityListener = new NGEntityListener(this);
    public Logger                  log;
 
     public void onEnable() {
        log = this.getServer().getLogger();

         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_PICKUP_ITEM, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_TARGET, entityListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Priority.Normal, this);
 
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info("[NuxGrief] " + pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
     }
 
     public void onDisable() {
     }
 }
