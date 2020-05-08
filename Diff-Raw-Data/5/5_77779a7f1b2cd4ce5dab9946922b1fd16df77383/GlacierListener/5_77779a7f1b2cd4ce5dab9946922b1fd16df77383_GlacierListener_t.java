 package at.junction.glacier;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import org.bukkit.material.Dispenser;
 
 class GlacierListener implements Listener {
 
     Glacier plugin;
 
     public GlacierListener(Glacier plugin) {
         this.plugin = plugin;
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockPlace(BlockPlaceEvent event) {
         //Only moderators should have these 4 blocks...Flow them if moderator isn't frozen
         if (event.getBlock().isLiquid()) {
             if (event.getPlayer().hasPermission("glacier.flowing") && !plugin.frozenPlayers.contains(event.getPlayer().getName())){
                 return;
             } else {
                 plugin.newFrozen(event.getBlock());
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockBreak(BlockBreakEvent event) {
         if (event.getBlock().getType() == Material.ICE) {
             if (!event.getPlayer().getItemInHand().containsEnchantment(Enchantment.SILK_TOUCH)) {
                 plugin.newFrozen(event.getBlock());
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockFromTo(BlockFromToEvent event) {
         if (plugin.frozenBlocks.get(event.getBlock().getWorld().getName()).contains(plugin.hashLocation(event.getBlock().getLocation()))) {
             event.setCancelled(true);
             return;
         }
 
         if (!plugin.canFlowInRegion(event.getBlock(), event.getToBlock())) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockPhysics(BlockPhysicsEvent event) {
         Material mat = event.getBlock().getType();
 
         if (mat == Material.STATIONARY_LAVA || mat == Material.STATIONARY_WATER) {
             if (plugin.frozenBlocks.get(event.getBlock().getWorld().getName()).contains(plugin.hashLocation(event.getBlock().getLocation()))) {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockFade(BlockFadeEvent event) {
         if (event.getBlock().getType() == Material.ICE) {
             event.setCancelled(true);
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onBlockDispense(BlockDispenseEvent event) {
         Material mat = event.getItem().getType();
         if (mat == Material.LAVA_BUCKET || mat == Material.WATER_BUCKET) {
             Dispenser dispenser = (Dispenser) event.getBlock().getState().getData();
             plugin.newFrozen(event.getBlock().getRelative(dispenser.getFacing()));
         }
     }
 
     @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
     public void onPlayerBucketFill(PlayerBucketFillEvent event) {
         if (plugin.config.DEBUG){
             plugin.getLogger().info(String.format("Filled bucket at %s", event.getBlockClicked().getRelative(event.getBlockFace()).getLocation()));
            plugin.getLogger().info(String.format("Hash %s", plugin.hashLocation(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation())));
 
         }
         Material mat = event.getItemStack().getType();
         if (mat == Material.LAVA_BUCKET || mat == Material.WATER_BUCKET) {
             plugin.delFrozen(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation());
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
     public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
         if (plugin.config.DEBUG){
             plugin.getLogger().info(String.format("Empty bucket at %s", event.getBlockClicked().getRelative(event.getBlockFace())));
            plugin.getLogger().info(String.format("Hash %s", plugin.hashLocation(event.getBlockClicked().getRelative(event.getBlockFace()).getLocation())));
         }
         if (plugin.frozenPlayers.contains(event.getPlayer().getName())){
             plugin.newFrozen(event.getBlockClicked().getRelative(event.getBlockFace()));
             return;
         }
         //If player has permissions and isn't frozen, let them place normally
         if (event.getPlayer().hasPermission("glacier.flowing")){
             return;
         }
         //If there is a region, and the player is a member of the region
         if (plugin.canPlaceFlowingLiquid(event.getBlockClicked().getRelative(event.getBlockFace()), event.getPlayer().getName())){
             //If we always freeze lava, still freeze it
             if (plugin.config.FREEZE_LAVA && event.getBucket() == Material.LAVA_BUCKET){
                 plugin.newFrozen(event.getBlockClicked().getRelative(event.getBlockFace()));
                 return;
             }
             //Flow it
             return;
         }
     }
 }
