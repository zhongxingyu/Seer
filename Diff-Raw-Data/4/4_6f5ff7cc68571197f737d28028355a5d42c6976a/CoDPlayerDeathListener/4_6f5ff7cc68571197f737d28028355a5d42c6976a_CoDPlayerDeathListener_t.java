 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package gadgettvman.github.io.CordsofDeath;
 
import org.bukkit.ChatColor;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.Listener;
 import org.bukkit.entity.Player;
 
 public class CoDPlayerDeathListener implements Listener {
     private final CordsOfDeath plugin;
 
     CoDPlayerDeathListener(CordsOfDeath instance) {
         this.plugin = instance;
     }
     
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event){
         Player player = event.getEntity();
        String playerName = ChatColor.stripColor(player.getDisplayName());
         int playerDeathX = player.getLocation().getBlockX();
         int playerDeathY = player.getLocation().getBlockY();
         int playerDeathZ = player.getLocation().getBlockZ();
         String deathLocation = "X:" + playerDeathX + " Y:" + playerDeathY + " Z:" + playerDeathZ;
         if (player.hasPermission("CordsOfDeath.sendCordsOnDeath")) {
             player.sendMessage("You died at:" + " " + deathLocation);
             this.plugin.getLogger().info(playerName + " " + "died at:" + " " + deathLocation);
         }
     }
 }
