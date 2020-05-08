 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.Color;
 import com.undeadscythes.udsplugin.*;
 import java.util.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.*;
 import org.bukkit.inventory.*;
 import org.bukkit.util.Vector;
 
 /**
  * When a player interacts with a block.
  * @author UndeadScythes
  */
 public class PlayerInteract extends ListenerWrapper implements Listener {
     @EventHandler
    public final void onEvent(final PlayerInteractEvent event) {
         final Action action = event.getAction();
         final SaveablePlayer player = UDSPlugin.getOnlinePlayers().get(event.getPlayer().getName());
         final Material inHand = player.getItemInHand().getType();
         final Block block = event.getClickedBlock();
         if(action == Action.LEFT_CLICK_AIR) {
             if(inHand == Material.COMPASS && player.hasPermission(Perm.COMPASS)) {
                 compassTo(player);
                 event.setCancelled(true);
             }
         } else if(action == Action.LEFT_CLICK_BLOCK) {
             if(inHand == Material.COMPASS && player.hasPermission(Perm.COMPASS)) {
                 compassTo(player);
                 event.setCancelled(true);
             } else if(inHand == Material.STICK && player.hasPermission(Perm.WAND)) {
                 wand1(player, block);
                 event.setCancelled(true);
             } else if((block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) && !player.isSneaking()) {
                 sign(player, (Sign)block.getState());
             } else {
                 event.setCancelled(lockCheck(block, player));
             }
         } else if(action == Action.RIGHT_CLICK_AIR) {
             if(inHand == Material.PAPER && player.hasPermission(Perm.PAPER_COMPLEX)) {
                 paperComplex(player, player.getLocation());
                 event.setCancelled(true);
             } else if(inHand == Material.PAPER && player.hasPermission(Perm.PAPER_SIMPLE)) {
                 paperSimple(player, block.getLocation());
                 event.setCancelled(true);
             } else if(inHand == Material.COMPASS && player.hasPermission(Perm.COMPASS)) {
                 compassThru(player);
                 event.setCancelled(true);
             } else if(!"".equals(player.getPowertool()) && inHand.getId() == player.getPowertoolID()) {
                 powertool(player);
                 event.setCancelled(true);
             }
         } else if(action == Action.RIGHT_CLICK_BLOCK) {
             if(inHand == Material.STICK && player.hasPermission(Perm.WAND)) {
                 wand2(player, block);
                 event.setCancelled(true);
             } else if(inHand == Material.PAPER && player.hasPermission(Perm.PAPER_COMPLEX)) {
                 paperComplex(player, block.getLocation());
                 event.setCancelled(true);
             } else if(inHand == Material.PAPER && player.hasPermission(Perm.PAPER_SIMPLE)) {
                 paperSimple(player, block.getLocation());
                 event.setCancelled(true);
             } else if(inHand == Material.COMPASS) {
                 compassThru(player);
                 event.setCancelled(true);
             } else if(inHand == Material.MONSTER_EGG && block.getType() == Material.MOB_SPAWNER) {
                 setMobSpawner(block, player);
                 event.setCancelled(true);
             } else if(block.getType() == Material.WALL_SIGN || block.getType() == Material.SIGN_POST) {
                 sign(player, (Sign)block.getState());
                 event.setCancelled(true);
             } else if(!"".equals(player.getPowertool()) && inHand.getId() == player.getPowertoolID()) {
                 powertool(player);
                 event.setCancelled(true);
             } else if(inHand == Material.MINECART && UDSPlugin.getRails().contains(block.getType())) {
                 minecart(player, block.getLocation());
                 player.setItemInHand(new ItemStack(Material.AIR));
                 event.setCancelled(true);
             } else {
                 event.setCancelled(lockCheck(block, player) || bonemealCheck(block, player));
             }
         }
     }
 
     /**
      * Powertool events.
      * @param player Player using a powertool.
      */
     private void powertool(final SaveablePlayer player) {
         player.performCommand(player.getPowertool());
     }
 
     /**
      * Check before applying bonemeal effects.
      * @param block Block clicked.
      * @param player Player using bonemeal.
      * @return
      */
     private boolean bonemealCheck(final Block block, final SaveablePlayer player) {
         return player.getItemInHand().getType() == Material.INK_SACK && player.getItemInHand().getData().getData() == (byte)15 && !player.canBuildHere(block.getLocation());
     }
 
     /**
      * Check if a region is locked.
      * @param block Block the player is clicking.
      * @param player Player who is interacting.
      * @return
      */
     private boolean lockCheck(final Block block, final SaveablePlayer player) {
         final Material material = block.getType();
         if(material == Material.WOODEN_DOOR || material == Material.IRON_DOOR_BLOCK || material == Material.STONE_BUTTON || material == Material.LEVER || material == Material.TRAP_DOOR || material == Material.FENCE_GATE) {
             final Location location = block.getLocation();
             if(!player.canBuildHere(location) && hasFlag(location, RegionFlag.LOCK)) {
                 if(player.hasPermission(Perm.BYPASS)) {
                     player.sendMessage(Color.MESSAGE + "Protection bypassed.");
                 } else {
                     player.sendMessage(Color.ERROR + "You can't do that here.");
                     return true;
                 }
             }
         }
         return false;
     }
 
     /**
      * Check to change mob spawner type.
      * @param block Block the was clicked.
      * @param player Player who clicked.
      */
     @SuppressWarnings("deprecation")
     private void setMobSpawner(final Block block, final SaveablePlayer player) {
         final byte itemData = player.getItemInHand().getData().getData();
         player.setItemInHand(new ItemStack(Material.MONSTER_EGG, player.getItemInHand().getAmount() - 1, (short)0, itemData));
         ((CreatureSpawner)block.getState()).setSpawnedType(EntityType.fromId(itemData));
         player.sendMessage(Color.MESSAGE + "Spawner set.");
     }
 
     /**
      * Check if wand was used.
      * @param player Player using wand.
      * @param block Block clicked.
      * @return <code>true</code> if event needs to be cancelled.
      */
     private void wand1(final SaveablePlayer player, final Block block) {
         final Session session = player.forceSession();
         session.setV1(new Vector(block.getX(), block.getY(), block.getZ()), player.getWorld());
         player.sendMessage(Color.MESSAGE + "Point 1 set.");
         if(session.getV1() != null && session.getV2() != null) {
             player.sendMessage(Color.MESSAGE.toString() + session.getVolume() + " blocks selected.");
         }
     }
 
     /**
      *
      * @param player
      * @param block
      */
     private void wand2(final SaveablePlayer player, final Block block) {
         final Session session = player.forceSession();
         session.setV2(new Vector(block.getX(), block.getY(), block.getZ()), player.getWorld());
         player.sendMessage(Color.MESSAGE + "Point 2 set.");
         if(session.getV1() != null && session.getV2() != null) {
             player.sendMessage(Color.MESSAGE.toString() + session.getVolume() + " blocks selected.");
         }
     }
 
     /**
      * Check if player is using a compass.
      * @param player Player using compass.
      */
     private void compassTo(final SaveablePlayer player) {
         final Block block = player.getTargetBlock(UDSPlugin.getTransparentBlocks(), UDSPlugin.getConfigInt(ConfigRef.COMPASS_RANGE));
         if(block.getType().isSolid()) {
             player.move(Warp.findSafePlace(block.getLocation()));
         } else {
             player.sendMessage(Color.ERROR + "No block in range.");
         }
     }
 
     /**
      *
      * @param player
      */
     private void compassThru(final SaveablePlayer player) {
         final List<Block> los = player.getLastTwoTargetBlocks(UDSPlugin.getTransparentBlocks(), UDSPlugin.getConfigInt(ConfigRef.COMPASS_RANGE));
         if(los.size() == 1) {
             return;
         }
         final Block block = los.get(1);
         if(block.getType().isSolid()) {
             final Location location = block.getRelative(los.get(0).getFace(block)).getLocation();
             player.move(Warp.findSafePlace(location));
         } else {
             player.sendMessage(Color.ERROR + "No block in range.");
         }
     }
 
     /**
      * Check if player is using paper.
      * @param player Player using paper.
      * @param location Location of block clicked.
      */
     private void paperSimple(final SaveablePlayer player, final Location location) {
         if(regionsHere(location).isEmpty()) {
             player.sendMessage(Color.MESSAGE + "No regions here.");
         } else {
             final List<Region> testRegions = regionsHere(location);
             for(Region region : testRegions) {
                 if(region.getOwner().equals(player)) {
                     player.sendMessage(Color.MESSAGE + "You own this block.");
                 } else if(region.getMembers().contains(player)) {
                     player.sendMessage(Color.MESSAGE + "Your room mate owns this block.");
                 } else {
                     player.sendMessage(Color.MESSAGE + "Somebody else owns this block.");
                 }
             }
         }
     }
 
     /**
      *
      * @param player
      * @param location
      */
     private void paperComplex(final SaveablePlayer player, final Location location) {
         if(regionsHere(location).isEmpty()) {
             player.sendMessage(Color.MESSAGE + "No regions here.");
         } else {
             final List<Region> testRegions = regionsHere(location);
             for(Region region : testRegions) {
                 region.sendInfo(player);
             }
         }
     }
 
     /**
      * Check if player clicked a sign.
      * @param player Player using a sign.
      * @param sign Sign clicked.
      */
     private void sign(final SaveablePlayer player, final Sign sign) {
         final String cantDoThat = Color.ERROR + "You can't do that.";
         if(sign.getLine(0).equals(Color.SIGN + "[CHECKPOINT]")) {
             if(player.hasPermission(Perm.CHECK)) {
                 player.setCheckPoint(player.getLocation());
                 player.sendMessage(Color.MESSAGE + "Checkpoint set. Use /check to return here. Good luck.");
             } else {
                 player.sendMessage(cantDoThat);
             }
         } else if(sign.getLine(0).equals(Color.SIGN + "[MINECART]")) {
             if(player.hasPermission(Perm.MINECART)) {
                 final Location location = sign.getBlock().getLocation();
                 if(EntityTracker.minecartNear(location)) {
                     player.sendMessage(Color.MESSAGE + "There is a minecart already at the station.");
                 } else {
                     player.getWorld().spawnEntity(location.clone().add(0.5, -1, 0.5), EntityType.MINECART);
                 }
             } else {
                 player.sendMessage(cantDoThat);
             }
         } else if(sign.getLine(0).equals(Color.SIGN + "[PRIZE]")) {
             if(player.hasPermission(Perm.PRIZE)) {
                 if(player.hasClaimedPrize()) {
                     player.claimPrize();
                     final ItemStack item = findItem(sign.getLine(1));
                     item.setAmount(Integer.parseInt(sign.getLine(2)));
                     player.getInventory().addItem(item);
                 } else {
                     player.sendMessage(Color.ERROR + "You have already claimed a prize today.");
                 }
             } else {
                 player.sendMessage(cantDoThat);
             }
         } else if(sign.getLine(0).equals(Color.SIGN + "[ITEM]")) {
             if(player.hasPermission(Perm.ITEM)) {
                     final ItemStack item = findItem(sign.getLine(1));
                     final int owned = player.countItems(item);
                     if(owned < Integer.parseInt(sign.getLine(2))) {
                         item.setAmount(Integer.parseInt(sign.getLine(2)) - owned);
                         player.getInventory().addItem(item);
                     } else {
                         player.sendMessage(Color.ERROR + "You already have enough of that item.");
                     }
             } else {
                 player.sendMessage(cantDoThat);
             }
         } else if(sign.getLine(0).equals(Color.SIGN + "[WARP]")) {
             if(player.hasPermission(Perm.WARP)) {
                 final Warp warp = UDSPlugin.getWarps().get(sign.getLine(1));
                 if(warp == null) {
                     player.sendMessage(Color.ERROR + "This warp cannot be found.");
                 } else {
                     player.teleport(warp.getLocation());
                 }
             } else {
                 player.sendMessage(cantDoThat);
             }
         } else if(sign.getLine(0).equals(Color.SIGN + "[SPLEEF]")) {
             if(player.hasPermission(Perm.SPLEEF)) {
                 final Region region = UDSPlugin.getRegions().get(sign.getLine(1));
                 if(region == null) {
                     player.sendMessage(Color.ERROR + "No region exists to refresh.");
                 } else {
                     final Vector min = region.getV1();
                     final Vector max = region.getV2();
                     final World world = region.getWorld();
                     for(int ix = (int) min.getX(); ix <= (int) max.getX(); ix++) {
                         for(int iy = (int) min.getY(); iy <= (int) max.getY(); iy++) {
                             for(int iz = (int) min.getZ(); iz <= (int) max.getZ(); iz++) {
                                 world.getBlockAt(ix, iy, iz).setType(Material.SNOW_BLOCK);
                             }
                         }
                     }
                 }
             } else {
                 player.sendMessage(Color.ERROR + "You can't do that.");
             }
         }
     }
 
     /**
      *
      * @param player
      * @param location
      */
     private void minecart(final SaveablePlayer player, final Location location) {
         EntityTracker.tagMinecart(player, location);
     }
 }
