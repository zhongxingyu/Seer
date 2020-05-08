 package com.ayan4m1.multiarrow;
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import com.ayan4m1.multiarrow.arrows.*;
 
 /**
  * MultiArrow for Bukkit
  *
  * @author ayan4m1
  */
 public class MultiArrow extends JavaPlugin {
     private final MultiArrowPlayerListener playerListener = new MultiArrowPlayerListener(this);
     private final MultiArrowEntityListener entityListener = new MultiArrowEntityListener(this);
     private final MultiArrowBlockHitDetector blockListener = new MultiArrowBlockHitDetector(this);
     private final HashMap<Player, Boolean> debugees = new HashMap<Player, Boolean>();
     
     public Logger log;
     public HashMap<String, ArrowType> activeArrowType;
     public HashMap<Arrow, CustomArrowEffect> activeArrowEffect;
 
     public MultiArrow() {
         this.log = Logger.getLogger("minecraft");
         this.activeArrowType = new HashMap<String, ArrowType>();
         this.activeArrowEffect = new HashMap<Arrow, CustomArrowEffect>();
     }
 
     public void onEnable() {
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvent(Event.Type.PLAYER_ANIMATION, playerListener, org.bukkit.event.Event.Priority.Low, this);
         pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, org.bukkit.event.Event.Priority.Low, this);
         pm.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, org.bukkit.event.Event.Priority.Low, this);
         
        this.getServer().getScheduler().scheduleAsyncRepeatingTask(this, blockListener, 10, 10);
         
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info(pdfFile.getName() + " v" + pdfFile.getVersion() + " enabled!");
     }
     
     public void onDisable() {
     	PluginDescriptionFile pdfFile = this.getDescription();
         log.info(pdfFile.getName() + " shutting down.");
        
        this.getServer().getScheduler().getActiveWorkers().clear();
     }
     
     public boolean isDebugging(final Player player) {
         if (debugees.containsKey(player)) {
             return debugees.get(player);
         } else {
             return false;
         }
     }
 
     public void setDebugging(final Player player, final boolean value) {
         debugees.put(player, value);
     }
 }
 
