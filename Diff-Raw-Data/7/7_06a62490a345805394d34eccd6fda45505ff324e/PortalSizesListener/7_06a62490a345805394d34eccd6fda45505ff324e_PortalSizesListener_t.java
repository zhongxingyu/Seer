 package info.gomeow.portalsizes;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.block.BlockPhysicsEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 
 public class PortalSizesListener implements Listener {
 
     PortalSizes plugin;
 
     public PortalSizesListener(PortalSizes ps) {
         plugin = ps;
     }
 
     static enum Way {
         NORTH_SOUTH,
         EAST_WEST;
     }
 
     class HandleInteract {
 
         boolean cancel;
 
         public HandleInteract(PlayerInteractEvent event) {
             Player player = event.getPlayer();
             if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                 if(player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
                     BlockFace bf = event.getBlockFace();
                     Block block = event.getClickedBlock();
                     if(block.getType() == Material.OBSIDIAN) {
                         Block possiblePortal = block.getRelative(bf);
                         if(possiblePortal.getType() != Material.PORTAL) {
                             ArrayList<Block> blocksToSet = new ArrayList<Block>();
                             try {
                                 checkArea(possiblePortal, Way.NORTH_SOUTH, blocksToSet);
                             } catch(StackOverflowError e) {
                                 cancel = true;
                             }
                            if(blocksToSet.size() < plugin.min) {
                                cancel = true;
                            }
                             if(!cancel) {
                                 int size = blocksToSet.size();
                                 if(size != 6) {
                                     if(!player.hasPermission("variableportalsizes.abnormal")) {
                                         player.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                                         return;
                                     }
                                 }
                                 for(Block b:blocksToSet) {
                                     b.setTypeIdAndData(Material.PORTAL.getId(), (byte) 0, false);
                                 }
                             }
                             blocksToSet.clear();
                             if(cancel) {
                                 cancel = false;
                                 tryEastWest(event);
 
                             }
                         }
                     }
                 }
             }
         }
 
         public void tryEastWest(PlayerInteractEvent event) {
             Player player = event.getPlayer();
             if(event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                 if(player.getItemInHand().getType() == Material.FLINT_AND_STEEL) {
                     BlockFace bf = event.getBlockFace();
                     Block block = event.getClickedBlock();
                     if(block.getType() == Material.OBSIDIAN) {
                         Block possiblePortal = block.getRelative(bf);
                         if(possiblePortal.getType() != Material.PORTAL) {
                             ArrayList<Block> blocksToSet = new ArrayList<Block>();
                             try {
                                 checkArea(possiblePortal, Way.EAST_WEST, blocksToSet);
                             } catch(StackOverflowError e) {
                                 cancel = true;
                             }
                             if(blocksToSet.size() < plugin.min) {
                                 cancel = true;
                             }
                             if(!cancel) {
                                 int size = blocksToSet.size();
                                 if(size != 6) {
                                     if(!player.hasPermission("variableportalsizes.abnormal")) {
                                         player.sendMessage(ChatColor.RED + "You do not have permission to do that!");
                                         return;
                                     }
                                 }
                                 for(Block b:blocksToSet) {
                                     b.setTypeIdAndData(Material.PORTAL.getId(), (byte) 0, false);
                                 }
                             }
                             blocksToSet.clear();
                         }
                     }
                 }
             }
         }
 
         public void checkArea(Block block, Way w, ArrayList<Block> blocksToSet) {
             if(blocksToSet.size() >= plugin.max) {
                 cancel = true;
                return;
             }
             if(!cancel) {
                 Block side1;
                 Block side2;
                 if(w == Way.NORTH_SOUTH) {
                     side1 = block.getRelative(BlockFace.NORTH);
                     side2 = block.getRelative(BlockFace.SOUTH);
                 } else {
                     side1 = block.getRelative(BlockFace.EAST);
                     side2 = block.getRelative(BlockFace.WEST);
                 }
                 Block up = block.getRelative(BlockFace.UP);
                 Block down = block.getRelative(BlockFace.DOWN);
                 if(side1.getType() == Material.AIR && !blocksToSet.contains(side1)) {
                     blocksToSet.add(side1);
                     checkArea(side1, w, blocksToSet);
                 }
                 if(side2.getType() == Material.AIR && !blocksToSet.contains(side2)) {
                     blocksToSet.add(side2);
                     checkArea(side2, w, blocksToSet);
                 }
                 if(up.getType() == Material.AIR && !blocksToSet.contains(up)) {
                     blocksToSet.add(up);
                     checkArea(up, w, blocksToSet);
                 }
                 if(down.getType() == Material.AIR && !blocksToSet.contains(down)) {
                     blocksToSet.add(down);
                     checkArea(down, w, blocksToSet);
                 }
                 if(side1.getType() != Material.OBSIDIAN && side1.getType() != Material.AIR) {
                     cancel = true;
                 }
                 if(side2.getType() != Material.OBSIDIAN && side2.getType() != Material.AIR) {
                     cancel = true;
                 }
                 if(up.getType() != Material.OBSIDIAN && up.getType() != Material.AIR) {
                     cancel = true;
                 }
                 if(down.getType() != Material.OBSIDIAN && down.getType() != Material.AIR) {
                     cancel = true;
                 }
             }
         }
 
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onPlayerInteractEvent(PlayerInteractEvent event) {
         new HandleInteract(event);
     }
 
     class HandlePhysics {
 
         boolean cancel = false;
 
         ArrayList<Block> blocks = new ArrayList<Block>();
 
         public void killPortal(Block b, Way w) {
             Block[] sides = {b.getRelative(BlockFace.UP), b.getRelative(BlockFace.DOWN), b.getRelative((w == Way.NORTH_SOUTH) ? BlockFace.NORTH : BlockFace.EAST), b.getRelative((w == Way.NORTH_SOUTH) ? BlockFace.SOUTH : BlockFace.WEST)};
             for(Block side:sides) {
                 if(side.getType() == Material.PORTAL) {
                     side.setType(Material.AIR);
                     killPortal(side, w);
                 }
             }
         }
 
         public void checkAround(Block b, Way w) {
             if(!cancel) {
                 Block[] sides = {b.getRelative(BlockFace.UP), b.getRelative(BlockFace.DOWN), b.getRelative((w == Way.NORTH_SOUTH) ? BlockFace.NORTH : BlockFace.EAST), b.getRelative((w == Way.NORTH_SOUTH) ? BlockFace.SOUTH : BlockFace.WEST)};
                 for(Block side:sides) {
                     if(!blocks.contains(side)) {
                         if(side.getType() == Material.PORTAL) {
                             blocks.add(side);
                             checkAround(side, w);
                         }
                         if(side.getType() != Material.PORTAL && side.getType() != Material.OBSIDIAN) {
                             cancel = true;
                            killPortal(b, w);
                             return;
                         }
                     }
                 }
             }
         }
 
         public HandlePhysics(BlockPhysicsEvent event, Block b) {
             checkAround(b, Way.NORTH_SOUTH);
             if(cancel) {
                 cancel = false;
                 checkAround(b, Way.EAST_WEST);
                 if(!cancel) {
                     event.setCancelled(true);
                 }
             } else {
                 event.setCancelled(true);
             }
         }
     }
 
     @EventHandler(priority = EventPriority.HIGHEST)
     public void onBlockPhysics(BlockPhysicsEvent event) {
         Block b = event.getBlock();
         if(b.getType() == Material.PORTAL) {
             new HandlePhysics(event, b);
         }
     }
 
 }
