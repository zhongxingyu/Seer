 package me.greatman.plugins.inn;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 
 public class IPlayerListener extends PlayerListener {
 	private final Inn plugin;
 
     public IPlayerListener(Inn instance) {
         plugin = instance;
     }
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.isCancelled())
             return;
 
         Player player = event.getPlayer();
         String playerName = player.getName();
         if (!plugin.getPlayerData().containsKey(playerName)) {
             plugin.getPlayerData().put(playerName, new PlayerData(plugin, playerName));
         }
 
         // If our user is select & is not holding an item, selection time
         if (plugin.getPlayerData().get(playerName).isSelecting() && player.getItemInHand().getType() == Material.AIR && event.getClickedBlock().getType() == Material.WOODEN_DOOR) {
             int x, y, z;
             Location loc = event.getClickedBlock().getLocation();
             x = loc.getBlockX();
             y = loc.getBlockY();
             z = loc.getBlockZ();
            ILogger.info(Integer.toString(event.getClickedBlock().getTypeId()));
             PlayerData pData = plugin.getPlayerData().get(playerName);
             
             if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                 int[] xyz = { x, y, z };
                 pData.setPositionA(xyz);
                 if(pData.checkSize()) {
                     player.sendMessage(ChatColor.DARK_AQUA + "Door selected");
                 
	                if(pData.getPositionA() != null && pData.getPositionB() == null) {
	                    player.sendMessage(ChatColor.DARK_AQUA + "Now, right click to select the far upper corner for the inn.");
	                } else if(pData.getPositionA() != null) {
 	                    player.sendMessage(ChatColor.DARK_AQUA + "Type " + ChatColor.WHITE + "/inn create [Price]" + ChatColor.DARK_AQUA + ", if you're happy with your selection, otherwise keep selecting!");
 	                }
                 }
         
             }
         }
     }
 }
