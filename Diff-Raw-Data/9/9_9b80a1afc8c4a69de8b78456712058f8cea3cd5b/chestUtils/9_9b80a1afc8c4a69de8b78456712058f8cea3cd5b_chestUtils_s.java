 //Copyright (C) 2011-2012 Chris Price (xCP23x)
 //This software uses the GNU GPL v2 license
 //See http://github.com/xCP23x/RestockIt/blob/master/README and http://github.com/xCP23x/RestockIt/blob/master/LICENSE for details
 
 package org.xcp23x.restockit;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.InventoryHolder;
 import org.bukkit.inventory.ItemStack;
 
 class chestUtils extends RestockIt {
     
     public static int getCurrentItems(Material item, Block chest) { //How many of item items are in chest chest?
         int items = 0;
         Inventory inv = getInventory(chest);
         int stacks = inv.getSize(); //How many stacks?
         for(int x=0; x<stacks; x++) {  //Check each stack
             ItemStack stack = inv.getItem(x);
             if(stack == null) continue; //Continue to next stack
             if(stack.getType() == item) {
                 items = items + stack.getAmount();
             }
         }
         return items;
     }
     
     public static Inventory getInventory(Block container){ //Returns the inventory of the InventoryHolder provided
         return ((InventoryHolder)container.getState()).getInventory(); 
     }
     
     public static boolean isSignAboveChest(Block chest) {
         if((chest.getType() == Material.CHEST) || (chest.getType() == Material.DISPENSER)) {  //Is it a container?
             Block sign = signUtils.getSignAboveChest(chest);
             if(sign.getType() == Material.WALL_SIGN || sign.getType() == Material.SIGN_POST) { //Is there a sign above?
                 String line = ((Sign)sign.getState()).getLine(1);
                 if(signUtils.isRIsign(line)) return true;  //Is it a RestockIt sign?
             }
         }
         return false;
     }
     
     public static boolean isSignBelowChest(Block chest) {
         if((chest.getType() == Material.CHEST) || (chest.getType() == Material.DISPENSER)) { //Is is a container
             Block sign = signUtils.getSignBelowChest(chest);
             if (sign.getType() == Material.WALL_SIGN || sign.getType() == Material.SIGN_POST) { //Is there a sign below
                 String line = ((Sign)sign.getState()).getLine(1);
                 if(signUtils.isRIsign(line)) return true;  //Is it a RestockIt sign?
             }
         }
         return false;
     }
     
     public static boolean isRIchest(Block chest) {
         return (isSignBelowChest(chest) || isSignAboveChest(chest));
     }
     
     public static boolean isAlreadyRIchest(Block sign) {
         //Check below the sign
         Block block = sign.getWorld().getBlockAt(sign.getX(), sign.getY()-1, sign.getZ());
         int x = sign.getX(), y = sign.getY(), z = sign.getZ();
         Block posSign = block.getWorld().getBlockAt(x, y-2, z); //Possibly a sign
         if ((block.getType() == Material.CHEST || block.getType() == Material.DISPENSER) && (posSign.getType() == Material.WALL_SIGN || posSign.getType() == Material.SIGN_POST)) {
             return (signUtils.isRIsign(((Sign)posSign.getState()).getLine(1)));
         }
         
         //Check above the sign
         block = sign.getWorld().getBlockAt(sign.getX(), sign.getY()+1, sign.getZ());
         posSign = block.getWorld().getBlockAt(x, y+2, z); //Possibly a sign
         if ((block.getType() == Material.CHEST || block.getType() == Material.DISPENSER) && (posSign.getType() == Material.WALL_SIGN || posSign.getType() == Material.SIGN_POST)) {
             return(signUtils.isRIsign(((Sign)posSign.getState()).getLine(1)));
         }
         return false;
     }
     
     public static Block getChestFromSign(Block sign) {
         //Try chest below sign first
         Block chest = sign.getWorld().getBlockAt(sign.getX(), sign.getY() - 1, sign.getZ());
         if(chest.getType() == Material.CHEST || chest.getType() == Material.DISPENSER) return chest;
         
         //Then chest above sign
         chest = sign.getWorld().getBlockAt(sign.getX(), sign.getY() + 1, sign.getZ());
         if(chest.getType() == Material.CHEST || chest.getType() == Material.DISPENSER) return chest;
         
         return null;
     }
     
     public static Block getDoubleChest(Block chest){
         //Return chest if there is one next to the block given
         if(chest.getType() != Material.CHEST) return null;
         int x = chest.getX(), y = chest.getY(), z = chest.getZ();
         World world = chest.getWorld();
         if(world.getBlockAt(x+1, y, z).getType() == Material.CHEST) return world.getBlockAt(x+1, y, z);
         if(world.getBlockAt(x-1, y, z).getType() == Material.CHEST) return world.getBlockAt(x-1, y, z);
         if(world.getBlockAt(x, y, z+1).getType() == Material.CHEST) return world.getBlockAt(x, y, z+1);
         if(world.getBlockAt(x, y, z-1).getType() == Material.CHEST) return world.getBlockAt(x, y, z-1);
         return null;
     }
     
     public static void fillChest(Block chest, String line){
         Material mat = signUtils.getMaterial(line);
         Inventory inv = getInventory(chest);
         Short damage = signUtils.getDamage(line);
         
         if (mat != Material.AIR) { //Trying to fill a chest with air will crash the client (but not the server)
             Block dc = getDoubleChest(chest); //The other chest if it's a dpuble chest
             
             int stackSize = mat.getMaxStackSize(); //Get stack size (snowball stack != stone)
             ItemStack stack = new ItemStack(mat, stackSize); //Make the ItemStack
             if(damage >= 0) stack.setDurability(damage); //Incorporate damage (remember, -1 = no damage value)
             int stacks = dc!= null ? inv.getSize() / 2 : inv.getSize();  //Get inventory size
             
             for(int x=0; x<stacks; x++) inv.setItem(x, stack);//Fill the chest
             
             if(getDoubleChest(chest)!=null) {
                 
                 if(chestUtils.isRIchest(dc)){
                     //Prepare stuff for second chest if it's different
                     Sign sign = (Sign)signUtils.getSignFromChest(dc).getState();
                     line = sign.getLine(2);
                     mat = signUtils.getMaterial(line);
                     damage = signUtils.getDamage(line);
                     stackSize = mat.getMaxStackSize();
                     stack = new ItemStack(mat, stackSize);
                 }
                 for(int x=stacks;x<stacks*2; x++) inv.setItem(x, stack); //Fill the other half
             }
             
         } else inv.clear(); //Clear inventory if air is requested (see, there WAS a reason for setting it as air)
     }
 }
