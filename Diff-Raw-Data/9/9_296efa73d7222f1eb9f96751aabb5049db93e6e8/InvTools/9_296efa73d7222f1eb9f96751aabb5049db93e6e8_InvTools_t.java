 package net.TheDgtl.InvTools;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 /**
  * InvTools for Bukkit
  *
  * @author TheDgtl
  */
 public class InvTools extends JavaPlugin {
     private Logger log;
     PluginManager pm;
     FileConfiguration newConfig;
     
     Integer toolRepairPoint;
     Integer armorRepairPoint;
     HashMap<Integer, Boolean> tools;
     HashMap<Integer, Boolean> armor; 
     
     public void onEnable() {
         pm = getServer().getPluginManager();
         log = Logger.getLogger("Minecraft");
         
         loadConfig();
         
         pm.registerEvents(new eListener(), this);
         pm.registerEvents(new pListener(), this);
         
         PluginDescriptionFile pdfFile = this.getDescription();
         log.info(pdfFile.getName() + " v." + pdfFile.getVersion() + " is enabled.");
     }
     
     public void loadConfig() {
         try {
         	reloadConfig();
             newConfig = this.getConfig();
            newConfig.options().copyDefaults(true);
             
             toolRepairPoint = newConfig.getInt("toolRepairPoint", 30);
             armorRepairPoint = newConfig.getInt("armorRepairPoint", 30);
             
             // Load tools that are invincible. Convert to integers.
             tools = new HashMap<Integer, Boolean>();
             String[] tmp = newConfig.getString("Tools", "277,278,279,293").split(",");
             for (String tool : tmp) {
                 if (tool.equals("")) continue;
                 tools.put(Integer.parseInt(tool), true);
             }
             // Load invincible armor
             armor = new HashMap<Integer, Boolean>();
             tmp = newConfig.getString("Armor", "310,311,312,313").split(",");
             for (String arm : tmp) {
                 if (arm.equals("")) continue;
                 armor.put(Integer.parseInt(arm), true);
             }
             saveConfig();
         } catch (Exception e) {
             log.log(Level.SEVERE, "Exception while loading InvTools/config.yml", e);
         }
     }
     
     public void onDisable() {
     }
     
     public class pListener implements Listener {
    	@SuppressWarnings("deprecation")
     	@EventHandler(priority = EventPriority.MONITOR)
     	public void onPlayerInteract(PlayerInteractEvent event) {
     		// Skip if not using an item
     		if (!event.hasItem()) return;
     		// Skip block place
     		if (event.isBlockInHand()) return;
     		
     		Player player = event.getPlayer();
             if (player.getItemInHand().getDurability() >= toolRepairPoint) {
                 if (hasPerm(player, "invtools.allowtools")) {
                     int itemInHand = player.getItemInHand().getTypeId();
                     if (tools.containsKey(itemInHand)) {
                         // Tool is invincible. Set damage to 0.
                         player.getItemInHand().setDurability((short)-1);
                         player.updateInventory();
                     }
                 }
             }
     		
     	}
     }
     
     public class eListener implements Listener {
    	@SuppressWarnings("deprecation")
		@EventHandler(priority = EventPriority.MONITOR)
         public void onEntityDamage(EntityDamageEvent event) {
             if (!(event.getEntity() instanceof Player)) return;
             Player player = (Player)event.getEntity();
             if (!hasPerm(player, "invtools.allowarmor")) return;
             
             for (ItemStack item : player.getInventory().getArmorContents()) {
                 if (item.getDurability() < armorRepairPoint) continue;
                 if (armor.containsKey(item.getTypeId())) {
                     item.setDurability((short)-1);
                     player.updateInventory();
                 }
             }
         }
     }
     
     /*
      * Check whether the player has the given permissions.
      */
     public boolean hasPerm(Player player, String perm) {
         return player.hasPermission(perm);
     }
 }
