 package de.FlatCrafter.XRayLogger;
 
 import java.util.ArrayList;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.inventory.ItemStack;
 
 public class BlockBreakLoggerListener implements Listener {
 
     ItemStack istack;
     public XRayLoggerMain main;
     public List<String> loggedXray = new ArrayList();
     public List<String> hideXray = new ArrayList();
 
     public BlockBreakLoggerListener(XRayLoggerMain main) {
         this.main = main;
     }
 
     public String getLoggedXray() {
         String returnVal = "";
         if (loggedXray.isEmpty()) {
             returnVal = "Nothing in the logs!";
         } else {
             for (int i = 0; i < loggedXray.size(); i++) {
             int j = i+1;
             returnVal = returnVal + "[" + j + "] " + loggedXray.get(i) + "\r\n";
             }
         }
         return returnVal;
     }
 
     public String gethiddenXray() {
         String returnVal = "";
         if (hideXray.isEmpty()) {
             returnVal = "Nothing on this list";
         } else {
             for (int i = 0; i < loggedXray.size(); i++) {
                 int j = i+1;
                 returnVal = returnVal + "[" + j + "] " + hideXray.get(i) + "\r\n";
             }
         }
         return returnVal;
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent e) {
         Execute.init(this.main, this);
         if(e.getBlock().getType().equals(Material.DIAMOND_ORE) || e.getBlock().getType().equals(Material.DIAMOND_BLOCK)) {
             Execute.diamondOre(e.getPlayer());
             Execute.diamondBlock(e.getPlayer());
         }
         if(e.getBlock().getType().equals(Material.GOLD_ORE) || e.getBlock().getType().equals(Material.GOLD_BLOCK)) {
             Execute.goldOre(e.getPlayer());
             Execute.goldBlock(e.getPlayer());
         }
         if(e.getBlock().getType().equals(Material.LAPIS_ORE) || e.getBlock().getType().equals(Material.LAPIS_BLOCK)) {
             Execute.lapisOre(e.getPlayer());
             Execute.lapisBlock(e.getPlayer());
         }
     }
 
    @EventHandler(priority = EventPriority.LOWEST)
     public void onPlayerJoin(PlayerLoginEvent e) {
         Player p = e.getPlayer();
         if(p.isBanned()) {
             e.disallow(PlayerLoginEvent.Result.KICK_BANNED, ChatColor.translateAlternateColorCodes('&', main.getConfig().getString("ban.message")));
         } else {
             e.allow();
         }
     }
 }
