 package tmc.BetterProtected.Listeners;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.event.player.PlayerBucketEmptyEvent;
 import org.bukkit.event.player.PlayerBucketFillEvent;
 import tmc.BetterProtected.domain.BlockCoordinate;
 import tmc.BetterProtected.domain.BlockEvent;
 import tmc.BetterProtected.domain.World;
 import tmc.BetterProtected.svc.BlockEventRepository;
 
 import static org.bukkit.Material.*;
 import static tmc.BetterProtected.domain.types.BlockEventType.PLACED;
 import static tmc.BetterProtected.domain.types.BlockEventType.REMOVED;
 
 public class BlockListener implements Listener {
 
     private BlockEventRepository blockEventRepository;
 
     public BlockListener(BlockEventRepository blockEventRepository) {
         this.blockEventRepository = blockEventRepository;
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
         Player player = event.getPlayer();
         Block block = event.getBlock();
 
         if (doesPlayerHavePermissionToBreak(player, getMostRecentBlockEvent(block))) {
             blockEventRepository.save(BlockEvent.newBlockEvent(block, player.getName(), REMOVED));
         } else {
             event.setCancelled(true);
             player.sendMessage(ChatColor.DARK_RED + "You cannot break this block!");
         }
     }
 

     @EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
         Block block = event.getBlock();
         Player player = event.getPlayer();
 
         if(doesPlayerHavePermissionToPlace(player, getMostRecentBlockEvent(block))) {
             blockEventRepository.save(BlockEvent.newBlockEvent(block, player.getName(), PLACED));
         } else {
             event.setCancelled(true);
             event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot place a block here!");
         }
     }
 
     @EventHandler
     public void onBucketRemove(PlayerBucketFillEvent event) {
         Block block = event.getBlockClicked().getRelative(event.getBlockFace());
         Player player = event.getPlayer();
 
         if (doesPlayerHavePermissionToBreak(player, getMostRecentBlockEvent(block))) {
             blockEventRepository.save(BlockEvent.newBlockEvent(block, player.getName(), REMOVED));
         } else {
             event.setCancelled(true);
            player.sendMessage(ChatColor.DARK_RED + "You fill your bucket from this block!");
         }

        player.sendMessage(ChatColor.DARK_AQUA + String.format("Relative block type is %s", block.getType()));
     }
 
     @EventHandler
     public void onBucketPlace(PlayerBucketEmptyEvent event) {
         Block block = event.getBlockClicked().getRelative(event.getBlockFace());
         Player player = event.getPlayer();
 
         if(doesPlayerHavePermissionToPlace(player, getMostRecentBlockEvent(block))) {
             Material blockType = block.getType();
             if (event.getBucket() == WATER_BUCKET) {
                 blockType = STATIONARY_WATER;
             } else if (event.getBucket() == LAVA_BUCKET) {
                 blockType = STATIONARY_LAVA;
             }
 
             blockEventRepository.save(BlockEvent.newBlockEvent(block, player.getName(), PLACED, blockType));
         } else {
             event.setCancelled(true);
             event.getPlayer().sendMessage(ChatColor.DARK_RED + "You cannot pour your bucket here!");
         }
     }
 
     private BlockEvent getMostRecentBlockEvent(Block block) {
         BlockCoordinate blockCoordinate = new BlockCoordinate(block.getX(), block.getY(), block.getZ());
         World world = new World(block.getWorld().getName());
         return blockEventRepository.findMostRecent(blockCoordinate, world);
     }
 
     private boolean doesPlayerHavePermissionToBreak(Player player, BlockEvent mostRecentBlockEvent) {
         return mostRecentBlockEvent == null || player.isOp() || mostRecentBlockEvent.getBlockEventType() == REMOVED ||
                 isBlockEventOwnedByPlayer(player, mostRecentBlockEvent);
     }
 
     private boolean doesPlayerHavePermissionToPlace(Player player, BlockEvent mostRecentBlockEvent) {
         return mostRecentBlockEvent == null || mostRecentBlockEvent.getBlockEventType() == REMOVED || player.isOp();
     }
 
     private boolean isBlockEventOwnedByPlayer(Player player, BlockEvent mostRecentBlockEvent) {
         return mostRecentBlockEvent.getOwner().getUsername().equalsIgnoreCase(player.getName());
     }
 }
