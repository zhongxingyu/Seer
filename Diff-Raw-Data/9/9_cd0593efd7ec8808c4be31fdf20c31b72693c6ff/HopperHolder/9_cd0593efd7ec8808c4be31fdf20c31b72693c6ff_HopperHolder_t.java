 package com.BASeCamp.GPEnderHoppers;
 
 import java.util.HashMap;
 import java.util.logging.Level;
 
 import me.ryanhamshire.GriefPrevention.Claim;
 import nl.rutgerkok.betterenderchest.BetterEnderChest;
 import nl.rutgerkok.betterenderchest.BetterEnderChestPlugin.PublicChest;
 import nl.rutgerkok.betterenderchest.WorldGroup;
 import nl.rutgerkok.betterenderchest.chestprotection.ProtectionBridge;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.block.Block;
 import org.bukkit.block.BlockFace;
 import org.bukkit.block.Hopper;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class HopperHolder {
     private static HashMap<Hopper, HopperHolder> hoppers = new HashMap<Hopper, HopperHolder>();
     
     public static HopperHolder getHolder(Hopper hopper) {
         if(hopper == null) return null;
         if(!hoppers.containsKey(hopper)) {
             Claim claim = GPEnderHopper.gp.dataStore.getClaimAt(hopper.getLocation(), true, null);
             if(claim == null) return null;
             hoppers.put(hopper, new HopperHolder(hopper, ClaimData.getClaimData(claim)));
         }
         return hoppers.get(hopper);
        
     }
     Block in,out;
     Hopper hop;
     ClaimData claim;
     WorldGroup wg;
     String debug = null;
 
     private HopperHolder(Hopper hopper, ClaimData claimdata) {
         hop = hopper;
         claim = claimdata;
         if(GPEnderHopper.becPlugin != null) wg = GPEnderHopper.becPlugin.getWorldGroupManager().getGroupByWorld(hopper.getLocation().getWorld());
         recalc();
     }
     
     public void recalc() {
         in = getHopperInput(hop);
         if(in != null && in.getType() != Material.ENDER_CHEST) in = null;
         out = getHopperOutput(hop);
         if(out != null && out.getType() != Material.ENDER_CHEST) out = null;
     }
 
     public void handle() {
         if(hop.getBlock().isBlockIndirectlyPowered()) return;
         try {
             if(in != null && claim.getHopperPull()) {
                 if(in.getType() == Material.ENDER_CHEST) {
                     handleMove(in, false);
                 } else recalc();
             }
             if(out != null && claim.getHopperPush()) {
                 if(out.getType() == Material.ENDER_CHEST) {
                     handleMove(out, true);
                 } else recalc();
             }
             debug = null;
         } catch(Exception e) {
             GPEnderHopper.log.log(Level.SEVERE, "Exception in thread", e);
             recalc(); //This was probably an uncaught enderchest destruction - should recalc incase!
         }
     }
     
     private void handleMove(Block chest, final boolean direction) {
         debug("handling "+(direction? "output" : "input"));
         //Chests must be open-able to be hopper-able
         if(chest.getRelative(BlockFace.UP).getType().isOccluding()) {
             debug("Chest cannot be opened.");
             return;
         }
         OfflinePlayer p = Bukkit.getOfflinePlayer(claim.getClaim().getOwnerName());
         //Both methods require players to be online.
         if(!p.isOnline()) return;
         Player grabPlayer = (Player)p;
         if(GPEnderHopper.becPlugin == null) {
             //Can get the inventory straight away - let's do that.
             completeMove(grabPlayer.getEnderChest(),direction);
         } else {
             String invName = null;
             ProtectionBridge protectionBridge = GPEnderHopper.becPlugin.getProtectionBridges().getSelectedRegistration();
             if (!protectionBridge.isProtected(chest)) {
                 debug("BEC: Unprotected.");
                 // Unprotected Ender chest
                 // Get public chest
                 if (grabPlayer.hasPermission("betterenderchest.user.open.publicchest") && PublicChest.openOnOpeningUnprotectedChest) {
                     invName = BetterEnderChest.PUBLIC_CHEST_NAME;
                     debug("BEC: Public Chest.");
                 } else {
                     // Get player's name
                     if (grabPlayer.hasPermission("betterenderchest.user.open.privatechest")) {
                         invName = grabPlayer.getName();
                         debug("BEC: Own Chest.");
                     } else debug("BEC: No Permission.");
                 }
             } else {
                 debug("BEC: Protected.");
                 // Protected Ender Chest
                 if (protectionBridge.canAccess(grabPlayer, chest)) {
                     // player can access the chest
                     if (grabPlayer.hasPermission("betterenderchest.user.open.privatechest")) {
                         // and has the correct permission node
                         // Get the owner's name
                         invName = protectionBridge.getOwnerName(chest);
                     } else debug("BEC: No permission.");
                 } else debug("BEC: Chest cannot be accessed.");
             }
             if(invName == null) {
                 debug("BEC: No chest.");
                 return; // No chest! Uh-oh!
             }
             debug("BEC: "+invName);
             new ConsumerWraper(invName, wg, this, direction);
         }
     }
     
     void completeMove(Inventory invent, boolean direction) {
         debug("Got inventory");
         Inventory hopper = hop.getInventory();
         //This is now always thread-safe and in the bukkit thread.
         if(direction == false) {//input
             if(hopper.firstEmpty() == -1) {//Only need to check for these 5!
                 for(ItemStack i: hopper.getContents()) {
                     if(i.getAmount() < i.getMaxStackSize()) {
                         if(!invent.contains(i.getType())) continue;
                         ItemStack rem = i.clone();
                         rem.setAmount(1);
                         if(invent.removeItem(rem).size() != 0) continue;
                         if(hopper.addItem(rem).size() == 0) return; //Success case
                         invent.addItem(rem); //Add that item back in!
                     }
                 }
             } else {
                 for(ItemStack i: invent.getContents()) {
                     if(i == null) continue;
                     ItemStack rem = i.clone();
                     rem.setAmount(1);
                     if(invent.removeItem(rem).size() != 0) continue; //Not sure how this wouldn't work, but sure.
                     if(hopper.addItem(rem).size() == 0) return; //Success case
                     invent.addItem(rem); //Add that item back in!
                 }
             }
         } else {//output
             for(ItemStack i: hopper.getContents()) {
                 if(i == null) continue;
                 ItemStack rem = i.clone();
                 rem.setAmount(1);
                 if(hopper.removeItem(rem).size() != 0) continue; //Not sure how this wouldn't work, but sure.
                 if(invent.addItem(rem).size() == 0) return; //Success case
                 hopper.addItem(rem); //Add that item back in!
             }
             
         }
     }
     
     public void destroy() {
         //Erases all traces of this object.
         hoppers.remove(hop);
     }
     
 
     public static Block getHopperInput(Hopper source) {
         return source.getBlock().getRelative(BlockFace.UP);
     }
     
     public void debug(String message) {
         if(debug == null) return;
         Player p = Bukkit.getPlayer(debug);
         if(p == null) debug = null;
         p.sendMessage("Debug: "+message);
     }
 
     // returns the block this hopper is pointing at.
     public static Block getHopperOutput(Hopper source) {
         byte rawdata = source.getRawData();
         BlockFace face = null;
         switch(rawdata) {
             case 0:
                 face = BlockFace.DOWN;
                 break;
             case 2:
                 face = BlockFace.NORTH;
                 break;
             case 3:
                 face = BlockFace.SOUTH;
                 break;
             case 4:
                 face = BlockFace.WEST;
                 break;
             case 5:
                 face = BlockFace.EAST;
                 break;
             default:
                 return null;
         }
         return source.getBlock().getRelative(face);
         // 0 000 is straight down
         // 1 001 is SOUTH (Z+)
         // 2 010 is NORTH (Z-)
         // 4 100 is WEST (X-)
         // 5 101 is EAST (X+)
     }
 
 }
