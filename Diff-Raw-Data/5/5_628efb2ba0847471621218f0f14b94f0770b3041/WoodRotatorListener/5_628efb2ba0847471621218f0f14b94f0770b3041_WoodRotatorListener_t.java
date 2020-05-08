 package eu.algent.WoodRotator;
 
 import org.bukkit.ChatColor;
import org.bukkit.GameMode;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class WoodRotatorListener implements Listener {
     WoodRotator plugin;
 
     public WoodRotatorListener(final WoodRotator plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler
     public void onPlayerInteract(PlayerInteractEvent event) {
         Player player = event.getPlayer();
         // Check if Axe in Hand (258, 271, 275, 279, 286)
         int id = player.getItemInHand().getTypeId();
         if (id == 258 || id == 271 || id == 275 || id == 279 || id == 286)
             return;
         // Check if left click + sneaking
         if (!(event.getAction().equals(Action.LEFT_CLICK_BLOCK) 
                 && player.isSneaking())) return;
         // Check if clicked block is wood
         Block block = event.getClickedBlock();
         if (block.getType() != Material.LOG) return;
         // Check for WorldGuard region permissions
         if (!plugin.canRotateWg(player, block)) {
             player.sendMessage( ChatColor.DARK_RED + "You don't have permission to rotate wood in this region.");
             return;
         }
         // Check player permissions
         if (!player.hasPermission("woodrotator.rotate")) return;
         Boolean barkFace = player.hasPermission("woodrotator.bark");
         // Rotate Wood
         rotateWood(block, barkFace);
        // Cancel destroy event if Creative Mode
        if (player.getGameMode().equals(GameMode.CREATIVE))
            event.setCancelled(true);
     }
 
     public void rotateWood(Block wood, Boolean barkFace) {
         Byte data = wood.getData();
         switch (wood.getData()) {
         case 0: 
             data = 4;
             break;
         case 4: 
             data = 8;
             break;
         case 8: 
             data = 12;
             if(barkFace) break;
         case 12: 
             data = 0;
             break;
         case 1: 
             data = 5;
             break;
         case 5: 
             data = 9;
             break;
         case 9: 
             data = 13;
             if(barkFace) break;
         case 13: 
             data = 1;
             break;
         case 2: 
             data = 6;
             break;
         case 6: 
             data = 10;
             break;
         case 10: 
             data = 14;
             if(barkFace) break;
         case 14: 
             data = 2;
             break;
         case 3: 
             data = 7;
             break;
         case 7: 
             data = 11;
             break;
         case 11: 
             data = 15;
             if(barkFace) break;
         case 15: 
             data = 3;
             break;
         }
         wood.setData(data);
     }
 
     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
         if (event.isCancelled()) return;
         final Block block = event.getBlockPlaced();
         if (block.getType() != Material.LOG) return;
         if (!event.getPlayer().hasPermission("woodrotatorplace.forceup")) return;
         Byte data = block.getData();
         switch (data) {
         case 0: 
         case 4: 
         case 8: 
         case 12: 
             data = 0;
             break;
         case 1: 
         case 5: 
         case 9: 
         case 13: 
             data = 1;
             break;
         case 2: 
         case 6: 
         case 10: 
         case 14: 
             data = 2;
             break;
         case 3: 
         case 7: 
         case 11: 
         case 15: 
             data = 3;
             break;
         }
         final Byte dataToSet = data;
         
         plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
             
             @Override
             public void run() {
                 block.setData(dataToSet);
             }
         });
     }
 }
