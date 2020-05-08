 package nu.nerd.SafeBuckets;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.block.BlockFadeEvent;
 import org.bukkit.event.block.BlockFromToEvent;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 
 public class SafeBucketsListener implements Listener {
 
     private final SafeBuckets plugin;
 
     SafeBucketsListener(SafeBuckets instance) {
         plugin = instance;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockPhysics(BlockPhysicsEvent event) {
         Material mat = event.getBlock().getType();
         if (mat == Material.STATIONARY_LAVA || mat == Material.STATIONARY_WATER) {
             if (plugin.isSafeLiquid(event.getBlock())) {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockDispense(BlockDispenseEvent event) {
         Material mat = event.getItem().getType();
         if (mat == Material.LAVA_BUCKET || mat == Material.WATER_BUCKET) {
         	event.setCancelled(true);
 //            for (BlockFace blockface : BlockFace.values()) {
 //                Block block = event.getBlock().getRelative(blockface);
 //                if (block.getType() == Material.WATER) {
 //                    block.setTypeId(9, false);
 //                }
 //                if (block.getType() == Material.LAVA) {
 //                    block.setTypeId(11, false);
 //                }
 //                if (block.getType() == Material.STATIONARY_LAVA || block.getType() == Material.STATIONARY_WATER) {
 //                    if (!plugin.isSafeLiquid(event.getBlock())) {
 ////                        SafeLiquid stat = new SafeLiquid();
 ////                        stat.setWorld(block.getWorld().getName());
 ////                        stat.setX(block.getX());
 ////                        stat.setY(block.getY());
 ////                        stat.setZ(block.getZ());
 ////                        plugin.table.save(stat);
 //                        plugin.addBlockToCacheAndDB(event.getBlock());
 //                    }
 //                }
 //            }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockFromTo(BlockFromToEvent event) {
         Block block = event.getBlock();
         //if (plugin.table.isSafeLiquid(event.getBlock())) {
         if (plugin.isSafeLiquid(event.getBlock())) {
             //somehow our block got changed to flowing, change it back
             if (block.getType() == Material.WATER) {
                 block.setTypeId(9, false);
             }
             if (block.getType() == Material.LAVA) {
                 block.setTypeId(11, false);
             }
 
             event.setCancelled(true);
             return;
         }
 
         if (plugin.isSafeLiquid(event.getBlock())) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockFade(BlockFadeEvent event) {
         if (event.getBlock().getType() == Material.ICE) {
             event.setCancelled(true);
         } 
    }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.getBlock().getType() == Material.ICE) {
            if (!event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                // If we are breaking the block with an enchanted pick then don't replace it with air, we want it to drop as an item
                event.getBlock().setTypeId(0);
            }
         }
     }
     
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onBlockPlace(BlockPlaceEvent event) {
         Block block = event.getBlockPlaced();
         // Actually lets not do that
         //// If we are replacing water then lets remove it to stop those annoying no flow areas
         //if (!event.getBlockReplacedState().getBlock().isLiquid()) {
         //	plugin.table.removeSafeLiquid(event.getBlockPlaced());
         //	return;
         //}
         // Someone is using liquid to replace this block, staff making it flow
         if (block.isLiquid()) {
             plugin.removeSafeLiquidFromCacheAndDB(block);
             //plugin.table.removeSafeLiquid(block);
             return;
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
         Block block = event.getBlockClicked().getRelative(event.getBlockFace());
 //        SafeLiquid stat = new SafeLiquid();
 //        stat.setWorld(block.getWorld().getName());
 //        stat.setX(block.getX());
 //        stat.setY(block.getY());
 //        stat.setZ(block.getZ());
 //        plugin.table.save(stat);
         plugin.addBlockToCacheAndDB(block);
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerBucketFill(PlayerBucketFillEvent event) {
         Material mat = event.getItemStack().getType();
         if (mat == Material.LAVA_BUCKET || mat == Material.WATER_BUCKET) {
             Block block = event.getBlockClicked().getRelative(event.getBlockFace());
             plugin.removeSafeLiquidFromCacheAndDB(block);
             //plugin.table.removeSafeLiquid(block);
         }
     }
 }
