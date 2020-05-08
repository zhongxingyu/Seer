 package com.undeadscythes.udsplugin.eventhandlers;
 
 import com.undeadscythes.udsplugin.Region.RegionFlag;
 import com.undeadscythes.udsplugin.*;
 import java.util.*;
 import org.apache.commons.lang.*;
 import org.bukkit.*;
 import org.bukkit.block.*;
 import org.bukkit.entity.*;
 import org.bukkit.event.*;
 import org.bukkit.event.block.*;
 import org.bukkit.event.player.*;
 import org.bukkit.inventory.*;
 import org.bukkit.util.Vector;
 
 /**
  * Description.
  * @author UndeadScythes
  */
 public class PlayerInteract extends ListenerWrapper implements Listener {
     @EventHandler
     public void onEvent(PlayerInteractEvent event) {
         Action action = event.getAction();
         SaveablePlayer player = UDSPlugin.getOnlinePlayers().get(event.getPlayer().getName());
         Material inHand = player.getItemInHand().getType();
         Block block = event.getClickedBlock();
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
             } else {
                 event.setCancelled(lockCheck(block, player) || bonemealCheck(block, player));
             }
         }
     }
 
     /**
      * Powertool events.
      * @param player Player using a powertool.
      */
     public void powertool(final SaveablePlayer player) {
         player.performCommand(player.getPowertool());
     }
 
     /**
      * Check before applying bonemeal effects.
      * @param location Location of block clicked.
      * @param player Player using bonemeal.
      * @return
      */
     public boolean bonemealCheck(final Block block, final SaveablePlayer player) {
         return player.getItemInHand().getType() == Material.INK_SACK &&
                player.getItemInHand().getData().getData() == (byte)15 &&
                !player.canBuildHere(block.getLocation());
     }
 
     /**
      * Check if a region is locked.
      * @param block Block the player is clicking.
      * @param player Player who is interacting.
      * @return
      */
     public boolean lockCheck(final Block block, final SaveablePlayer player) {
         final Material material = block.getType();
         if(material == Material.WOODEN_DOOR
         || material == Material.IRON_DOOR_BLOCK
         || material == Material.STONE_BUTTON
         || material == Material.LEVER
         || material == Material.TRAP_DOOR
         || material == Material.FENCE_GATE) {
             final Location location = block.getLocation();
             if(!player.canBuildHere(location) && hasFlag(location, RegionFlag.LOCK)) {
                 player.sendMessage(Color.ERROR + "You can't do that here.");
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Check to change mob spawner type.
      * @param block Block the was clicked.
      * @param player Player who clicked.
      */
     public void setMobSpawner(final Block block, final SaveablePlayer player) {
         byte itemData = player.getItemInHand().getData().getData();
         player.setItemInHand(new ItemStack(Material.MONSTER_EGG, player.getItemInHand().getAmount() - 1, (short)0, itemData));
         ((CreatureSpawner)block.getState()).setSpawnedType(EntityType.fromId(itemData));
         player.sendMessage(Color.MESSAGE + "Spawner set.");
     }
 
     /**
      * Check if wand was used.
      * @param player Player using wand.
      * @param action Action type of event.
      * @param block Block clicked.
      * @return <code>true</code> if event needs to be cancelled.
      */
     public void wand1(final SaveablePlayer player, final Block block) {
         Session session = player.forceSession();
         session.setV1(new Vector(block.getX(), block.getY(), block.getZ()));
         player.sendMessage(Color.MESSAGE + "Point 1 set.");
         if(session.getV1() != null && session.getV2() != null) {
             player.sendMessage(Color.MESSAGE.toString() + session.getVolume() + " blocks selected.");
         }
     }
 
     public void wand2(final SaveablePlayer player, final Block block) {
         Session session = player.forceSession();
         session.setV2(new Vector(block.getX(), block.getY(), block.getZ()));
         player.sendMessage(Color.MESSAGE + "Point 2 set.");
         if(session.getV1() != null && session.getV2() != null) {
             player.sendMessage(Color.MESSAGE.toString() + session.getVolume() + " blocks selected.");
         }
     }
 
     /**
      * Check if player is using a compass.
      * @param action Action type of event.
      * @param player Player using compass.
      * @param block Block clicked.
      * @param blockFace Face of block clicked.
      */
     public void compassTo(final SaveablePlayer player) {
         Location location = player.getTargetBlock(null, Config.COMPASS_RANGE).getLocation();
         location.setYaw(player.getLocation().getYaw());
         location.setPitch(player.getLocation().getPitch());
        player.teleport(Warp.findSafePlace(location));
     }
 
     public void compassThru(final SaveablePlayer player) {
         List<Block> LOS =  player.getLastTwoTargetBlocks(null, 5);
         if(LOS.size() == 1) {
             return;
         }
         Location location = LOS.get(1).getRelative(LOS.get(0).getFace(LOS.get(1))).getLocation();
         location.setYaw(player.getLocation().getYaw());
         location.setPitch(player.getLocation().getPitch());
        player.teleport(Warp.findSafePlace(location));
     }
 
     /**
      * Check if player is using paper.
      * @param action Action type of event.
      * @param player Player using paper.
      * @param block Block clicked.
      */
     public void paperSimple(final SaveablePlayer player, final Location location) {
         if(!regionsHere(location).isEmpty()) {
             ArrayList<Region> testRegions = regionsHere(location);
             for(Region region : testRegions) {
                 if(region.getOwner().equals(player)) {
                     player.sendMessage(Color.MESSAGE + "You own this block.");
                 } else if(region.getMembers().contains(player)) {
                     player.sendMessage(Color.MESSAGE + "Your room mate owns this block.");
                 } else {
                     player.sendMessage(Color.MESSAGE + "Somebody else owns this block.");
                 }
             }
         } else {
             player.sendMessage(Color.MESSAGE + "No regions here.");
         }
     }
 
     public void paperComplex(final SaveablePlayer player, final Location location) {
         if(!regionsHere(location).isEmpty()) {
             ArrayList<Region> testRegions = regionsHere(location);
             for(Region region : testRegions) {
                 player.sendMessage(Color.MESSAGE + "--- Region " + region.getName() + " ---");
                 player.sendMessage(Color.TEXT + "Owner: " + region.getOwnerName());
                 player.sendMessage(Color.TEXT + "Members: " + StringUtils.join(region.getMembers().toArray(), " "));
             }
         } else {
             player.sendMessage(Color.MESSAGE + "No regions here.");
         }
     }
 
     /**
      * Check if player clicked a sign.
      * @param player Player using a sign.
      * @param block Block clicked.
      */
     public void sign(final SaveablePlayer player, final Sign sign) {
         if(sign.getLine(0).equals(Color.SIGN + "[CHECKPOINT]")) {
             if(player.hasPermission(Perm.CHECK)) {
                 player.setCheckPoint(player.getLocation());
                 player.sendMessage(Color.MESSAGE + "Checkpoint set. Use /check to return here. Good luck.");
             } else {
                 player.sendMessage(Color.ERROR + "You can't do that.");
             }
         } else if(sign.getLine(0).equals(Color.SIGN + "[MINECART]")) {
             if(player.hasPermission(Perm.MINECART)) {
                 Location location = sign.getBlock().getLocation();
                 location.setX(location.getBlockX() + 0.5);
                 location.setY(location.getBlockY() - 0.5);
                 location.setZ(location.getBlockZ() + 0.5);
                 Bukkit.getWorld(location.getWorld().getName()).spawn(location, Minecart.class);
             } else {
                 player.sendMessage(Color.ERROR + "You can't do that.");
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
                 player.sendMessage(Color.ERROR + "You can't do that.");
             }
         } else if(sign.getLine(0).equals(Color.SIGN + "[ITEM]")) {
             if(player.hasPermission(Perm.ITEM)) {
                     final ItemStack item = findItem(sign.getLine(1));
 
                     int owned = player.countItems(item);
                     if(owned < Integer.parseInt(sign.getLine(2))) {
                         item.setAmount(Integer.parseInt(sign.getLine(2)) - owned);
                         player.getInventory().addItem(item);
                     } else {
                         player.sendMessage(Color.ERROR + "You already have enough of that item.");
                     }
             } else {
                 player.sendMessage(Color.ERROR + "You can't do that.");
             }
         } else if(sign.getLine(0).equals(Color.SIGN + "[WARP]")) {
             if(player.hasPermission(Perm.WARP)) {
                     final Warp warp = UDSPlugin.getWarps().get(sign.getLine(1));
                     if(warp != null) {
                         player.teleport(warp.getLocation());
                     } else {
                     player.sendMessage(Color.ERROR + "This warp cannot be found.");
                 }
             } else {
                 player.sendMessage(Color.ERROR + "You can't do that.");
             }
         } else if(sign.getLine(0).equals(Color.SIGN + "[SPLEEF]")) {
             if(player.hasPermission(Perm.SPLEEF)) {
                     final Region region = UDSPlugin.getRegions().get(sign.getLine(1));
                     if(region != null) {
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
                     } else {
                     player.sendMessage(Color.ERROR + "No region exists to refresh.");
                 }
             } else {
                 player.sendMessage(Color.ERROR + "You can't do that.");
             }
         }
     }
 }
