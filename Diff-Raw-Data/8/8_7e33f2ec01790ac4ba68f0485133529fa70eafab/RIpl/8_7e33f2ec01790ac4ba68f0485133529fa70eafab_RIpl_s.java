 //@author Chris Price (xCP23x)
 
 package org.xcp23x.RestockIt;
 
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.block.Dispenser;
 import org.bukkit.block.Sign;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class RIpl extends PlayerListener {
     public static RestockIt plugin;
     public RIpl(RestockIt instance) {
         plugin = instance;
     }
     
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         if (event.getAction() == Action.RIGHT_CLICK_BLOCK){
             Block chest = event.getClickedBlock();
             checkSign(chest);
         }
     }
     
     public static void checkSign(Block chest) {
         if((chest.getType() == Material.CHEST) || (chest.getType() == Material.DISPENSER)) {
             Block sign = null;
             Block m = chest.getWorld().getBlockAt(chest.getX(),chest.getY()-1,chest.getZ()); 
             Block p = chest.getWorld().getBlockAt(chest.getX(),chest.getY()+1,chest.getZ());
             
             if(p.getType() == Material.WALL_SIGN || p.getType() == Material.SIGN_POST) {
                 String line1 =((Sign)p.getState()).getLine(1);
                 if(RIbl.checkCommand(line1)) {
                     readSign(chest, p, line1);
                     return;
                 }
             }
             if (m.getType() == Material.WALL_SIGN || m.getType() == Material.SIGN_POST) {
                 String line1 =((Sign)m.getState()).getLine(1);
                 if(RIbl.checkCommand(line1)) readSign(chest, m, line1);
             }
         }
     }    
             
     public static void readSign(Block chest, Block sign, String line1){
         if(sign == null) return;
         Material item = Material.AIR;
         Inventory inventory = null;
         if (chest.getType() == Material.CHEST) {inventory = ((Chest)chest.getState()).getInventory();}
         else {inventory = ((Dispenser)chest.getState()).getInventory();}
         String line2 = ((Sign)sign.getState()).getLine(2);
         String itemID = line2;
         short damage = -1;
         if(line2.contains(":")) {
             itemID = line2.split(":")[0];
             String split = line2.split(":")[1];
             damage = Short.parseShort(split);
         }
        else if(RIbl.checkCommand(line1)){
             if (line2.equalsIgnoreCase("Incinerator")) {
                 item = Material.AIR;
             } else item = getMaterial(itemID);
             fillChest(item, inventory, damage);
        }    
     }
     

    
     public static Material getMaterial(String line2) {
         int ID = RIbl.checkID(line2);
         if(ID == 1) {
             return Material.getMaterial(Integer.parseInt(line2));
         }else if(ID == 2) {
             return Material.getMaterial(line2);
         }else return Material.AIR;
     }
     
     public static void fillChest(Material item, Inventory inv, Short damage) {
         if (item != Material.AIR) {
             int invSize = inv.getSize();
             int x = 0;
             int stackSize = item.getMaxStackSize();
             ItemStack stack = new ItemStack(item, stackSize);
             if(damage >= 0) stack.setDurability(damage);
             while(x < invSize) {
                 inv.setItem(x, stack);
                 x++;
             }
         } else inv.clear();
     }
 }
