 //Copyright (C) 2011-2012 Chris Price (xCP23x)
 //This software uses the GNU GPL v2 license
 //See http://github.com/xCP23x/RestockIt/blob/master/README and http://github.com/xCP23x/RestockIt/blob/master/LICENSE for details
 
 package org.xcp23x.restockit;
 
 import java.util.HashMap;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.inventory.ItemStack;
 
 class scheduler extends RestockIt {
     
     private static HashMap<Block, Integer> schedules = new HashMap<Block, Integer>(); //This stores task IDs (int) with the blocks they are running on
     
     public static void startSchedule(final Block block, int period) {
         
         if (schedules.get(block) == null) schedules.put(block,0); //Prepare the HashMap if it's null
         if (schedules.get(block) == 0) { //It's not running, start it
             RestockIt.debugSched("Schedule started for block at " + getCoords(block));
             schedules.put(block, RestockIt.plugin.getServer().getScheduler().scheduleSyncRepeatingTask(RestockIt.plugin, new Runnable() {
                 @Override
                 public void run() {
                     //If no sign is there, stop (the player may have removed it mid-count)
                     if (block.getType() != Material.WALL_SIGN && block.getType() != Material.SIGN_POST) {
                         scheduler.stopSchedule(block);
                         RestockIt.debugSched("Sign removed, cancelling schedule at " + getCoords(block));
                         return;
                     }
                     
                     Block chest = chestUtils.getChestFromSign(block);
                     String line2 = ((Sign)block.getState()).getLine(2);
                     String line3 = ((Sign)block.getState()).getLine(3);
                     
                    if(chestUtils.getCurrentItems(signUtils.getMaterial(line2), chest) >= signUtils.getMaxItems(line3)) {
                         RestockIt.debugSched("Container is full at " + getCoords(block));
                         //The chest has reached its limit
                         return;
                     }
                     
                     //Add item to chest
                     ItemStack stack = new ItemStack(signUtils.getMaterial(line2), 1);
                     Short damage = signUtils.getDamage(line2);
                     if (damage >= 0) stack.setDurability(damage);
                     chestUtils.getInventory(chest).addItem(stack);
                     RestockIt.debugSched("Added " + signUtils.getMaterial(line2).toString() + " to block at " + getCoords(block));
                 }
             
             },0,period));
             if (schedules.get(block) == -1) {
                 schedules.remove(block); //If it fails to start, remove entry from HashMap
                 RestockIt.debug("Schedule FAILED to start for block at " + getCoords(block));
             }
         } else RestockIt.debugSched("Tried to start schedule for block at " + getCoords(block) + ", but it's already running");
     }
     
     public static void stopSchedule(Block block) {
         if(schedules.containsKey(block)){
             RestockIt.debugSched("Removing schedule for block at " + getCoords(block));
             int i = schedules.get(block); //Get bukkit task ID
             if (i != 0) {  //If it's 0, it's not running
                 RestockIt.plugin.getServer().getScheduler().cancelTask(i); //Cancel task
                 schedules.remove(block); //Remove entry from HashMap
             }
         } else RestockIt.debugSched("Tried to remove schedule for block at " + getCoords(block)+ ", but it doesn't exist");
     }
     
     private static String getCoords(Block block){
         return block.getX() + "," + block.getY() + "," + block.getZ();
     }
 }
