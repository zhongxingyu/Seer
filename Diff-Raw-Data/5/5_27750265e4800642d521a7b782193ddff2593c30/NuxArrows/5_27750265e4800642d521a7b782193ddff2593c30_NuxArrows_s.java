 package net.n4th4.bukkit.nuxarrows;
 
 import java.util.logging.Logger;
 
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class NuxArrows extends JavaPlugin {
     private final NABlockListener  blockListener  = new NABlockListener(this);
     private final NAPlayerListener playerListener = new NAPlayerListener(this);
    public final Logger            log            = this.getServer().getLogger();
 
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.BLOCK_DISPENSE, blockListener, Priority.Normal, this);
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Priority.Normal, this);
 
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info("[NuxArrows] " + pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
     }
 
     public void onDisable() {
     }
 }
