 package Listeners;
 
 import java.util.HashMap;
 
 import Commands.VanishCmd;
 import Commands.MuteCmd;
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.block.Chest;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class PlayerListener implements Listener {
 	
     @EventHandler
     public void onPlayerChat(AsyncPlayerChatEvent e) {
         if (MuteCmd.muteDb.contains(e.getPlayer().getName())) e.setCancelled(true);
     }
     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent e) {
         for (String o : VanishCmd.vanishDb) {
             if (e.getPlayer().hasPermission("ec.vanish.seehidden")) continue;
             if (Bukkit.getPlayer(o) == null) continue;
             e.getPlayer().hidePlayer(Bukkit.getPlayer(o));
         }
         if (VanishCmd.isVanished(e.getPlayer())) {
             VanishCmd.setVanished(e.getPlayer(), true);
             e.getPlayer().sendMessage("aYou logged in vanished!");
         }
     }
     
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent e) {
         if (e.getAction().equals(Action.LEFT_CLICK_AIR) || e.getAction().equals(Action.RIGHT_CLICK_AIR)) return;
         if (e.getClickedBlock().getType().equals(Material.REDSTONE_ORE) && VanishCmd.isVanished(e.getPlayer())) e.setCancelled(true);
         
         if (VanishCmd.isVanished(e.getPlayer()) && e.getAction().equals(Action.RIGHT_CLICK_BLOCK) && e.getClickedBlock().getType().equals(Material.CHEST)) {
             e.setCancelled(true);
             final Chest c = (Chest) e.getClickedBlock().getState();
             final Inventory i = Bukkit.getServer().createInventory(e.getPlayer(), c.getInventory().getSize());
             i.setContents(c.getInventory().getContents());
             e.getPlayer().openInventory(i);
             VanishCmd.silentChestOpen(e.getPlayer());
         }
     }
     
     @EventHandler
     public void onPlayerPickupItem(PlayerPickupItemEvent e) {
         if (VanishCmd.isVanished(e.getPlayer())) e.setCancelled(true);
     }
     
 }
