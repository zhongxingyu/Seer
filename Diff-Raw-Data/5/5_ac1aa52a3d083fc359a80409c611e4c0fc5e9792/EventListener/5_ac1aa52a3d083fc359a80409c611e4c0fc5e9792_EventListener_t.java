 package me.limebyte.endercraftessentials;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 public class EventListener implements Listener {
 
     private static final Material LIGHT_ITEM = Material.GLOWSTONE_DUST;
     private static final String REI_PREFIX = "&0&0";
     private static final String REI_SUFFIX = "&e&f";
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (event.getItem().getType().equals(LIGHT_ITEM)) {
                 Block block = event.getClickedBlock().getRelative(event.getBlockFace());
                 int lightLevel = block.getLightLevel();
                 event.getPlayer().sendMessage(ChatColor.GOLD + "The light level of the selected block is " + lightLevel + ".");
             }
         }
     }
 
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {
         Player player = event.getPlayer();
         setDisplayName(player);
         String welcome = ChatColor.DARK_PURPLE + "Welcome to Endercraft " + player.getDisplayName() + "!";
         String message = REI_PREFIX + "&2&3" + REI_SUFFIX;
         player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
         player.sendMessage(welcome);
     }
 
     private void setDisplayName(Player player) {
         String name = player.getName();
 
        EndercraftEssentials.getInstance().log().info(name);

         if (name == "limebyte") {
             rename(player, "LimeByte");
         } else if (name == "bj2864") {
             rename(player, "BenBoy");
         } else if (name == "bg1345") {
             rename(player, "Ashpof");
         } else if (name == "tegdim") {
             rename(player, "Tegdim");
         }
     }
 
     private void rename(Player player, String name) {
         player.setDisplayName(name);
         player.setPlayerListName(name);
     }
 
 }
