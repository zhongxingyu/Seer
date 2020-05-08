 //Copyright (C) 2011-2012 Chris Price (xCP23x)
 //This software uses the GNU GPL v2 license
 //See http://github.com/xCP23x/RestockIt/blob/master/README and http://github.com/xCP23x/RestockIt/blob/master/LICENSE for details
 
 package org.xcp23x.restockit;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Sign;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 class signUtils extends RestockIt {
     
     public static Material getMaterial(String line){
         //Return material from line, else AIR
         return getType(line) > 0 ? Material.getMaterial(getType(line)) : Material.AIR;
     }
     
     public static short getDamage(String line) {
         //Return damage from line, else 0
         String dmgStr = (line.indexOf(":") >= 0 ? line : line+":0").split(":")[1];
         return Short.parseShort(dmgStr);
     }
     
     public static int getType(String line) {
         
         //Returns:
         //0 if it's an incinerator
         //-1 if it's and int, but not an ID
         //-2 if it's not an int or an ID
         //-3 if there's a bad damage value
         //Int ID of item if it's valid        
         
         if (line.equalsIgnoreCase("Incinerator")) {
             return 0;
         }
         Material t;
 	String sa[] = (line.indexOf(":") ==1 ? line : line+":0").split(":");
         
 	if ((t = Material.getMaterial(sa[0])) == null) {
             try {
             	if ((t = Material.getMaterial(Integer.parseInt(sa[0]))) == null) {
                     return -1; // it's an int, but not an valid Material ID.
             	}
             } catch (NumberFormatException ex) {
             	return -2; // it's not an int or ID.
             }
 	}
 
         try {
             Short.parseShort(sa[1]);
 	} catch (NumberFormatException ex) {
             return -3; // problem with damage value
 	}
 		
 	return t.getId();
     }
     
     //Currently Unused
     public static void setMaxItems(int items, Block block) {
         Sign sign = (Sign)block.getState();
         String part = sign.getLine(3).split(",")[1];
         sign.setLine(3, items + "," + part);
         sign.update();
     }
     
     public static int getMaxItems(String line) {
         String str = line.contains(",") ? line.split(",")[0].toLowerCase().replaceAll(" ", "") : null; //Split the line, else give null
         if(str==null) return 0;
         
         try {
             if(str.contains("i") || str.contains("items") || str.contains("item")) { //If they've specified a number of items
                 return Integer.parseInt(str.replaceAll("items", "").replaceAll("i", "").replaceAll("item", ""));
             }
             
             if(str.contains("s") || str.contains("stacks") || str.contains("stack")) {//If they've specified a number of stacks
                 return Integer.parseInt(str.replaceAll("stack", "").replaceAll("s", ""))*getMaterial(line).getMaxStackSize();
             }
             
             //Assume they mean they want x amount of items
             return Integer.parseInt(str);
         } catch(Exception ex){} //Catch anything that may have gone wrong
         
         return 0;
     }
     
     //Currently Unused
     public static void setPeriod(float period, Block block) {
         Sign sign = (Sign)block.getState();
         String part1 = sign.getLine(3).split(",")[0];
         String part2 = period%20 == 0 ? period/20 + "s" : period + "t";
         sign.setLine(3, part1 + ", " + part2);
         sign.update();
     }
     
     public static int getPeriod(String line) {
         //If they specify seconds, multiply by 20, if not, give the raw value
         try {
             String periodStr = line.contains(",") ? line.split(",")[1].replaceAll(" ", "") : "0";
            int returnInt = periodStr.contains("s") ? (int)Double.parseDouble(periodStr.replaceAll("s", ""))*20 : Integer.parseInt(periodStr);
            if(returnInt>0) return returnInt;
         } catch(Exception ex){}
         return 0;
     }
     
     public static boolean isRIsign(String line){
         //Check if line 1 says it's a RestockIt sign.
         String str = line.toLowerCase();
         return (str.contains("restockit") || str.contains("restock it") || str.contains("full chest") || str.contains("full dispenser"));
     }
     
     public static void dropSign(Block sign) {
         //Remove the sign and drop one at its location
         Location loc = sign.getWorld().getBlockAt(sign.getX(), sign.getY(), sign.getZ()).getLocation();
         sign.setType(Material.AIR);
         sign.getWorld().dropItem(loc, new ItemStack(Material.SIGN, 1));
     }
     
     public static boolean isDelayedSign(String line) {
         //If max items AND the period aren't specified, it's not a delayed sign
         if(getMaxItems(line) == 0 || getPeriod(line) == 0) return false;
         return true;
     }
     
     public static boolean isIncinerator(String line) {
         //Check if it's an incinerator
         return line.toLowerCase().contains("incinerator");
     }
     
     public static Block getSignFromChest(Block chest) {
         //Fairly simple, does what it says
         if(chestUtils.isSignAboveChest(chest)) return getSignAboveChest(chest);
         if(chestUtils.isSignBelowChest(chest)) return getSignBelowChest(chest);
         return null;
     }
     
     public static Block getSignAboveChest(Block chest) {
         return chest.getWorld().getBlockAt(chest.getX(), chest.getY() +1, chest.getZ());
     }
     
     public static Block getSignBelowChest(Block chest) {
         return chest.getWorld().getBlockAt(chest.getX(), chest.getY() -1, chest.getZ());
     }
     
     public static boolean line2hasErrors(String line, Player player) {
         
         //A frontend for getType(), this tells the player what's going on
         switch(getType(line)){
             case -3:
                 playerUtils.sendPlayerMessage(player, 5, line.split(":")[1]);
                 return true;
             case -2:
                 playerUtils.sendPlayerMessage(player, 4, line.contains(":") ? line.split(":")[0] : line);
                 return true;
             case -1:
                 playerUtils.sendPlayerMessage(player, 3, line.contains(":") ? line.split(":")[0] : line);
                 return true;
         }
         return false;
     }
 }
