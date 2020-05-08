 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.cnaude.plugin.WolfColors;
 
 import java.io.File;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.bukkit.craftbukkit.entity.CraftWolf;
 import org.bukkit.entity.AnimalTamer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author cnaude
  */
 public class WCMain extends JavaPlugin implements Listener {
     public static final String PLUGIN_NAME = "WolfColors";
     public static final String LOG_HEADER = "[" + PLUGIN_NAME + "]";
     static final Logger log = Logger.getLogger("Minecraft");     
         
     private File pluginFolder;
     private File configFile;
     
     @Override
     public void onEnable() {        
         pluginFolder = getDataFolder();
         configFile = new File(pluginFolder, "config.yml");
         createConfig();
         this.getConfig().options().copyDefaults(true);
         saveConfig();
         loadConfig();
         getServer().getPluginManager().registerEvents(this, this);         
     }
         
     @EventHandler(priority = EventPriority.NORMAL)
     public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
         Entity e = event.getRightClicked();
         Player p = event.getPlayer();
         EntityType et = e.getType();
 
         if (et.equals(EntityType.WOLF)) {
             if (p.hasPermission("wolfcolors.wolfcolors")) {
                 ItemStack item = p.getInventory().getItemInHand();
                 if (((Wolf)e).isTamed()) {
                     if (((Wolf)e).getOwner() == (AnimalTamer)p) {
                         if (item.getTypeId() == 351) {
                             ((CraftWolf)e).getHandle().setCollarColor((byte) (15 - p.getInventory().getItemInHand().getDurability()));
                             if (item.getAmount() == 1) {
                                 event.getPlayer().getInventory().remove(item);
                             } else {
                                 item.setAmount(item.getAmount() - 1);
                             }
                         }
                     }
                 }
             }
         }
     }
                 
     
     private void createConfig() {
         if (!pluginFolder.exists()) {
             try {
                 pluginFolder.mkdir();
             } catch (Exception e) {
                 logInfo("ERROR: " + e.getMessage());                
             }
         }
 
         if (!configFile.exists()) {
             try {
                 configFile.createNewFile();
             } catch (Exception e) {
                 logInfo("ERROR: " + e.getMessage());
             }
         }
     }
         
     private void loadConfig() {
     }
             
     public void logInfo(String _message) {
         log.log(Level.INFO, String.format("%s %s", LOG_HEADER, _message));
     }
 
     public void logError(String _message) {
         log.log(Level.SEVERE, String.format("%s %s", LOG_HEADER, _message));
     }
 }
